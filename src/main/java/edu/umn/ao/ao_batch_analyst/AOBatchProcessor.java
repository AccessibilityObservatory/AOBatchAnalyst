package edu.umn.ao.ao_batch_analyst;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.text.DecimalFormat;

import lombok.Setter;

import org.opentripplanner.analyst.core.GeometryIndex;
import org.opentripplanner.analyst.request.SampleFactory;
import org.opentripplanner.routing.services.GraphService;
import org.opentripplanner.routing.services.SPTService;
import org.opentripplanner.routing.algorithm.EarliestArrivalSPTService;
import org.opentripplanner.routing.spt.ShortestPathTree;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.FileSystemResource;

public class AOBatchProcessor {

	private static final Logger LOG = LoggerFactory.getLogger(AOBatchProcessor.class);
	
	@Autowired @Setter private GraphService graphService;
	@Autowired @Setter private SPTService sptService;
	@Autowired @Setter private SampleFactory sampleFactory;
	@Autowired @Setter private IndividualRoutingRequestFactory routingRequestFactory;
	@Autowired @Setter private AOAggregator aggregator;
	@Autowired @Setter private DepartureTimeListGenerator depTimeGenerator;
	
	@Resource @Setter private MultipleAttributePopulation origins;
	@Resource @Setter private MultipleAttributePopulation destinations;
	
	
	@Setter private int nThreads = Runtime.getRuntime().availableProcessors();
	@Setter private int [] thresholds = {300};
	@Setter private int cutoffSeconds = -1;
	@Setter private List<Date> depTimes = new ArrayList<Date>();
	@Setter private int logThrottleSeconds = 10;
	@Setter private String outputPath = "test.csv";
	
	private long startTime = -1;
	private long lastLogTime = 0;
	private DecimalFormat pctFormat = new DecimalFormat("###.##");
	
	public void setup() {
		sptService = new EarliestArrivalSPTService();
		sampleFactory = new SampleFactory();
		GeometryIndex gi = new GeometryIndex();
		gi.setGraphService(graphService);
		gi.initialzeComponent();
		sampleFactory.setIndex(gi);
		aggregator = new AOAggregator(origins, destinations, thresholds, depTimes);
	}
	
    public static void main(String[] args) throws IOException {
        org.springframework.core.io.Resource appContextResource;
        if( args.length == 0) {
            LOG.error("no configuration XML file specified");
            return;
        } else {
            String configFile = args[0];
            appContextResource = new FileSystemResource(configFile);
        }
        GenericApplicationContext ctx = new GenericApplicationContext();
        XmlBeanDefinitionReader xmlReader = new XmlBeanDefinitionReader(ctx);
        xmlReader.loadBeanDefinitions(appContextResource);
        ctx.refresh();
        ctx.registerShutdownHook();
        AOBatchProcessor processor = ctx.getBean(AOBatchProcessor.class);
        if (processor == null)
            LOG.error("No BatchProcessor bean was defined.");
        else
            processor.run();
    }
	
	public void run() {
		// TODO: assumes that threshold array is sorted
		cutoffSeconds = aggregator.getThresholds()[aggregator.getThresholds().length-1] + 1;
		LOG.info("Cutoff is now {}", cutoffSeconds);
		linkIntoGraph(destinations);
		
		LOG.info("Number of threads: {}", nThreads);
		ExecutorService threadPool = Executors.newFixedThreadPool(nThreads);
		CompletionService<Void> ecs = new ExecutorCompletionService<Void>(threadPool);
		
		startTime = System.currentTimeMillis();
		int nTasks = 0;
		for (MultipleAttributeIndividual oi : origins) {
			for (Date depTime : depTimeGenerator.getDepartureTimes()) {
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
		
		aggregator.writeCVS(outputPath);
		
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
            long totalMem = Runtime.getRuntime().totalMemory() / 1048576;
            long maxMem = Runtime.getRuntime().maxMemory() / 1048576;
            LOG.info("Memory usage: {}MiB out of {}MiB ({} %)", totalMem, maxMem, pctFormat.format(totalMem * 100.0 / maxMem));
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
			this.req = routingRequestFactory.getIndividualRoutingRequest(origin, depTime, cutoffSeconds);
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
