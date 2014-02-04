package edu.umn.ao.ao_batch_analyst;

import java.io.IOException;
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
import org.opentripplanner.analyst.batch.Population;
import org.opentripplanner.common.model.GenericLocation;
import org.opentripplanner.routing.core.RoutingRequest;
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
	@Setter private RoutingRequestFactory routingRequestFactory;
	@Setter private Population origins;
	@Setter private Population destinations;
	@Setter private AOAggregator aggregator;
	@Setter private int nThreads = Runtime.getRuntime().availableProcessors();
	
	public BatchProcessor(GraphService graphService, Population origins, Population destinations, RoutingRequestFactory routingRequestFactory) {
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
	}
}
