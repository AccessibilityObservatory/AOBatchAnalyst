package edu.umn.ao.ao_batch_analyst;

import org.opentripplanner.common.model.GenericLocation;
import org.opentripplanner.routing.core.OptimizeType;
import org.opentripplanner.routing.core.RoutingRequest;
import org.opentripplanner.routing.core.TraverseMode;
import org.opentripplanner.routing.core.TraverseModeSet;

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
