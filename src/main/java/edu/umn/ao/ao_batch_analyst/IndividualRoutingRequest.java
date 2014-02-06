package edu.umn.ao.ao_batch_analyst;

import org.opentripplanner.analyst.batch.Individual;
import org.opentripplanner.common.model.GenericLocation;
import org.opentripplanner.routing.core.RoutingRequest;

public class IndividualRoutingRequest extends RoutingRequest {
	
	// Use the default graph if one is not specified 
	public String routerId = null;
	
	public Individual originIndividual = null;
	
	public void setOriginIndividual(Individual i) {
		this.originIndividual = i;
		GenericLocation latLon = new GenericLocation(i.lat, i.lon);
		this.setFrom(latLon);
	}
	
	public IndividualRoutingRequest clone() {
		IndividualRoutingRequest newReq = (IndividualRoutingRequest) super.clone();
		if (originIndividual != null) {
			newReq.setOriginIndividual(originIndividual);
		}
		return newReq;
	}

}
