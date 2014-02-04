package edu.umn.ao.ao_batch_analyst;

import java.util.Date;
import java.util.List;

import org.opentripplanner.analyst.batch.MultipleAttributeShapefilePopulation;
import org.opentripplanner.analyst.batch.ResultSet;

public class AOAggregator {
	
	private final MultipleAttributeShapefilePopulation origins;
	private final MultipleAttributeShapefilePopulation destinations;
	private final String labelAttribute;
	private final List<String> valueAttributes;
	private final List<Date> depTimes;
	private final List<Double> thresholds;
	private AggregateResultSet resultSet;
	
	public AOAggregator(MultipleAttributeShapefilePopulation origins, MultipleAttributeShapefilePopulation destinations, List<Double> thresholds, List<Date> depTimes) {
		this.origins = origins;
		this.destinations = destinations;
		this.thresholds = thresholds;
		this.depTimes = depTimes;
		this.labelAttribute = origins.getLabelAttribute();
		this.valueAttributes = destinations.getValueAttributes();
		this.resultSet = new AggregateResultSet(origins, depTimes, thresholds, valueAttributes);
	}
	
	public void computeAggregate(int originI, int deptimeI, ResultSet travelTimes) {
		int destI = 0;
		for (MultipleAttributeIndividual target : destinations)
		
	}
	
	private class AggregateResultSet {
		
		protected double [][][][] results;
		
		protected AggregateResultSet(MultipleAttributeShapefilePopulation origins, List<Date> depTimes, List<Double> thresholds, List<String> valueAttributes) {
			this.results = new double[origins.size()][depTimes.size()][thresholds.size()][valueAttributes.size()];
		}
	}
	
}
