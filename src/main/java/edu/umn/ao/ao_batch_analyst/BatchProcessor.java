package edu.umn.ao.ao_batch_analyst;

import java.io.IOException;
import java.util.List;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import lombok.Setter;

import org.opentripplanner.analyst.core.Sample;
import org.opentripplanner.analyst.core.GeometryIndex;
import org.opentripplanner.analyst.request.SampleFactory;
import org.opentripplanner.analyst.batch.Individual;
import org.opentripplanner.analyst.batch.Population;
import org.opentripplanner.analyst.batch.ResultSet;
import org.opentripplanner.common.model.GenericLocation;
import org.opentripplanner.routing.core.TraverseMode;
import org.opentripplanner.routing.error.TransitTimesException;
import org.opentripplanner.routing.error.VertexNotFoundException;
import org.opentripplanner.routing.services.GraphService;
import org.opentripplanner.routing.services.SPTService;
import org.opentripplanner.routing.algorithm.EarliestArrivalSPTService;
import org.opentripplanner.routing.spt.ShortestPathTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BatchProcessor {

	private static final Logger LOG = LoggerFactory.getLogger(BatchProcessor.class);
	
	@Setter private GraphService graphService;
	@Setter private SPTService sptService;
	@Setter private SampleFactory sampleFactory;
	@Setter private IndividualRoutingRequestFactory routingRequestFactory;
	@Setter private Population origins;
	@Setter private Population destinations;
	@Setter private AOAggregator aggregator;
	@Setter private int nThreads = Runtime.getRuntime().availableProcessors();
	@Setter private List<Date> depTimes;
	@Setter private int logThrottleSeconds = 4;
	
	private long startTime = -1;
	private long lastLogTime = 0;
	
	public BatchProcessor(GraphService graphService, Population origins, Population destinations, IndividualRoutingRequestFactory routingRequestFactory) {
		this.graphService = graphService;
		this.origins = origins;
		this.destinations = destinations;
		this.routingRequestFactory = routingRequestFactory;
		this.setup();
	}
	
	private void setup() {
		sptService = new EarliestArrivalSPTService();
		sampleFactory = new SampleFactory();
		GeometryIndex gi = new GeometryIndex();
		gi.setGraphService(graphService);
		sampleFactory.setIndex(gi);
	}
	
	private void run() {
		linkIntoGraph(destinations);
		
		LOG.info("Number of threads: {}", nThreads);
		ExecutorService threadPool = Executors.newFixedThreadPool(nThreads);
		CompletionService<Void> ecs = new ExecutorCompletionService<Void>(threadPool);
		
		startTime = System.currentTimeMillis();
		int nTasks = 0;
		for (Individual oi : origins) {
			for (Date depTime : depTimes) {
				ecs.submit(new BatchAnalystTask(oi, depTime), null);
				LOG.debug("Submitted task for origin {}, departure time {}", oi.label, depTime.toString());
				++nTasks;
			}
		}
		LOG.info("Created {} tasks", nTasks);
		
		int nCompleted = 0;
		try {
			while (nCompleted < nTasks) {
				try {
					ecs.take().get();
				} catch (ExecutionException e) {
					LOG.error("Exception in thread task: {}", e);
				}
				++nCompleted;
				projectRunTime(nCompleted, nTasks);
			}
		} catch (InterruptedException e) {
			LOG.warn("Run was interrupted after {} tasks", nCompleted);
		}
		threadPool.shutdown();
		
		LOG.info("Done.");
	}

    private void projectRunTime(int current, int total) {
        long currentTime = System.currentTimeMillis();

        if (currentTime > lastLogTime + logThrottleSeconds * 1000) {
            lastLogTime = currentTime;
            double runTimeMin = (currentTime - startTime) / 1000.0 / 60.0;
            double projectedMin = (total - current) * (runTimeMin / current);
            LOG.info("Received {} results out of {}", current, total);
            LOG.info("Running {} min, {} min remaining (projected)", (int)runTimeMin, (int)projectedMin);
        }
    }
	
	private void linkIntoGraph(Population p) {
		LOG.info("Linking population {} to the graph...", p);
		int n = 0;
		int nonNull = 0;
		for (Individual i : p) {
			i.sample = sampleFactory.getSample(i.lon, i.lat);
			n++;
			if (i.sample != null)
				nonNull++;
		}
		LOG.info("successfully linked {} individuals out of {}", nonNull, n);
	}
	
	private class BatchAnalystTask implements Runnable {
		
		protected final IndividualRoutingRequest req;
		
		public BatchAnalystTask(Individual origin, Date depTime) {
			this.req = routingRequestFactory.getIndividualRoutingRequest((depTime.getTime()/1000), origin);
		}
		
		public void run() {
			ShortestPathTree spt = sptService.getShortestPathTree(req);
			ResultSet travelTimes = ResultSet.forTravelTimes(destinations, spt);
			req.cleanup();
			
			aggregator.computeAggregate();
		}
	}
}
