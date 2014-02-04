package edu.umn.ao.ao_batch_analyst;

import org.opentripplanner.analyst.batch.Individual;
import org.opentripplanner.common.model.GenericLocation;
import org.opentripplanner.routing.core.RoutingRequest;

public class IndividualRoutingRequest extends RoutingRequest {

	public Individual originIndividual = null;
	
	public void setOriginIndividual(Individual i) {
		this.originIndividual = i;
		GenericLocation latLon = new GenericLocation(i.lat, i.lon);
		this.setFrom(latLon);
	}
	
	public IndividualRoutingRequest clone() {
		IndividualRoutingRequest clone = (IndividualRoutingRequest) super.clone();
		if (originIndividual != null) {
			clone.setOriginIndividual(originIndividual);
		}
		return clone;
	}

}
