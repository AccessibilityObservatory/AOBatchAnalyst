package edu.umn.ao.ao_batch_analyst;

import org.opentripplanner.analyst.batch.Individual;

public class MultipleAttributeIndividual extends Individual {

	public final int id;
	private final double [] values;
	
	public MultipleAttributeIndividual(int id, String label, double lon, double lat, double [] values) {
		this.id = id;
		this.label = label;
		this.lon = lon;
		this.lat = lat;
		this.values = values;
	}

}
