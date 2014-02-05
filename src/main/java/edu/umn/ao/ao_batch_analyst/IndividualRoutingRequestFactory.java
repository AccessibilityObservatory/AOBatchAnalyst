package edu.umn.ao.ao_batch_analyst;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Date;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.opentripplanner.analyst.batch.Individual;
import org.opentripplanner.routing.core.RoutingRequest;
import org.opentripplanner.routing.core.TraverseModeSet;
import org.opentripplanner.routing.services.GraphService;
import org.opentripplanner.routing.error.TransitTimesException;
import org.opentripplanner.routing.error.VertexNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.Getter;
import lombok.Setter;

public class IndividualRoutingRequestFactory {
	
	private static final Logger LOG = LoggerFactory.getLogger(IndividualRoutingRequest.class);
	
	private final GraphService graphService;
	private final IndividualRoutingRequest protoReq;
	
	public IndividualRoutingRequestFactory(GraphService graphService, IndividualRoutingRequest protoReq) {
		this.graphService = graphService;
		this.protoReq = protoReq;		
	}
	
	public IndividualRoutingRequest getIndividualRoutingRequest(Date depTime, Individual origin) {
		return getIndividualRoutingRequest(depTime, origin, -1);
	}
	
	public IndividualRoutingRequest getIndividualRoutingRequest(Date depTime, Individual origin, int cutoffSeconds) {
		IndividualRoutingRequest newReq = protoReq.clone();
		
		newReq.setDateTime(depTime);
		if (cutoffSeconds > 0)
			newReq.worstTime = newReq.dateTime + cutoffSeconds;
		
		newReq.setOriginIndividual(origin);
		
		try {
			newReq.setRoutingContext(graphService.getGraph());
		} catch (VertexNotFoundException vnf) {
			LOG.debug("No vertex for individual {} labeled {} with coordinates {} {}", origin, origin.label, origin.lat, origin.lon);
			return null;
		}
		
		return newReq;
	}
	
}
