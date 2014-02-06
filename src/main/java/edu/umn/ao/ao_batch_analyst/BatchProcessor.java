package edu.umn.ao.ao_batch_analyst;

import java.util.ArrayList;
import java.util.List;
import java.util.Date;
import java.util.Collections;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import lombok.Setter;

import org.opentripplanner.analyst.core.GeometryIndex;
import org.opentripplanner.analyst.request.SampleFactory;
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
	@Setter private MultipleAttributePopulation origins;
	@Setter private MultipleAttributePopulation destinations;
	@Setter private AOAggregator aggregator;
	@Setter private int nThreads = Runtime.getRuntime().availableProcessors();
	@Setter private List<Integer> thresholds = new ArrayList<Integer>();
	@Setter private int cutoffSeconds = -1;
	@Setter private List<Date> depTimes = new ArrayList<Date>();
	@Setter private int logThrottleSeconds = 4;
	@Setter private String outputFileName = "test.csv";
	
	private long startTime = -1;
	private long lastLogTime = 0;
	
	public BatchProcessor(GraphService graphService, MultipleAttributePopulation origins, MultipleAttributePopulation destinations, IndividualRoutingRequestFactory routingRequestFactory) {
		this.graphService = graphService;
		this.origins = origins;
		this.destinations = destinations;
		this.routingRequestFactory = routingRequestFactory;
	}
	
	private void setup() {
		sptService = new EarliestArrivalSPTService();
		sampleFactory = new SampleFactory();
		GeometryIndex gi = new GeometryIndex();
		gi.setGraphService(graphService);
		gi.initialzeComponent();
		sampleFactory.setIndex(gi);
		aggregator = new AOAggregator(origins, destinations, thresholds, depTimes);
	}
	
	public void run() {
		setup();
		linkIntoGraph(destinations);
		
		LOG.info("Number of threads: {}", nThreads);
		ExecutorService threadPool = Executors.newFixedThreadPool(nThreads);
		CompletionService<Void> ecs = new ExecutorCompletionService<Void>(threadPool);
		
		startTime = System.currentTimeMillis();
		int nTasks = 0;
		for (MultipleAttributeIndividual oi : origins) {
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
		
		aggregator.writeCVS(outputFileName);
		
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
	
	private void linkIntoGraph(MultipleAttributePopulation p) {
		LOG.info("Linking population {} to the graph...", p);
		int n = 0;
		int nonNull = 0;
		for (MultipleAttributeIndividual i : p) {
			i.sample = sampleFactory.getSample(i.lon, i.lat);
			n++;
			if (i.sample != null)
				nonNull++;
		}
		LOG.info("successfully linked {} individuals out of {}", nonNull, n);
	}
	
	private class BatchAnalystTask implements Runnable {
		
		private final MultipleAttributeIndividual origin;
		private final Date depTime;
		protected final IndividualRoutingRequest req;
		
		public BatchAnalystTask(MultipleAttributeIndividual origin, Date depTime) {
			this.origin = origin;
			this.depTime = depTime;
			this.req = routingRequestFactory.getIndividualRoutingRequest(depTime, origin, Collections.max(thresholds)+1);
		}
		
		public void run() {
			if (req != null) {
				ShortestPathTree spt = sptService.getShortestPathTree(req);
				ResultSet travelTimes = ResultSet.forTravelTimes(destinations, spt);
				req.cleanup();
				
				aggregator.computeAggregate(origin, depTime, travelTimes);
			} else {
				aggregator.setAggregate(origin, depTime, -1);
			}
		}
	}
}
