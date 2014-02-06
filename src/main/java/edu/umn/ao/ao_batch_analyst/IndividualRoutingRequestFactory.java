package edu.umn.ao.ao_batch_analyst;

import java.util.Date;

import org.opentripplanner.analyst.batch.Individual;
import org.opentripplanner.routing.services.GraphService;
import org.opentripplanner.routing.error.VertexNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.Setter;

import javax.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;

public class IndividualRoutingRequestFactory {
	
	private static final Logger LOG = LoggerFactory.getLogger(IndividualRoutingRequest.class);
	
	@Autowired @Setter private GraphService graphService;
	@Autowired @Setter private IndividualRoutingRequest prototypeRoutingRequest;
	
	@Resource @Setter private MultipleAttributePopulation origins;
	
	public IndividualRoutingRequestFactory(GraphService graphService, IndividualRoutingRequest protoReq) {
		this.graphService = graphService;
		this.prototypeRoutingRequest = protoReq;		
	}
	
	public IndividualRoutingRequestFactory() {
		
	}
	
	public IndividualRoutingRequest getIndividualRoutingRequest(Individual origin, Date depTime) {
		return getIndividualRoutingRequest(origin, depTime, -1);
	}
	
	public IndividualRoutingRequest getIndividualRoutingRequest(Individual origin, Date depTime, int cutoffSeconds) {
		IndividualRoutingRequest newReq = prototypeRoutingRequest.clone();
		
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
