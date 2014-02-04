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

import lombok.Getter;
import lombok.Setter;

public class IndividualRoutingRequestFactory {
	
	private final GraphService graphService;
	private final IndividualRoutingRequest protoReq;
	
	public IndividualRoutingRequestFactory(GraphService graphService, IndividualRoutingRequest protoReq) {
		this.graphService = graphService;
		this.protoReq = protoReq;		
	}
	
	public IndividualRoutingRequest getIndividualRoutingRequest(long depTime, Individual origin) {
		IndividualRoutingRequest newReq = protoReq.clone();
		newReq.setOriginIndividual(origin);
		newReq.setRoutingContext(graphService.getGraph());
		
		return newReq;
	}
	
}
