package edu.umn.ao.ao_batch_analyst;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.text.SimpleDateFormat;

import org.opentripplanner.analyst.batch.Individual;
import org.opentripplanner.analyst.batch.ResultSet;
import org.opentripplanner.analyst.batch.ShapefilePopulation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.csvreader.CsvWriter;

public class AOAggregator {
	
	private static final Logger LOG = LoggerFactory.getLogger(ShapefilePopulation.class);
	
	private final MultipleAttributeShapefilePopulation origins;
	private final MultipleAttributeShapefilePopulation destinations;
	private final String labelAttribute;
	private final List<String> valueAttributes;
	private final List<Date> depTimes;
	private final List<Integer> thresholds;
	private AggregateResultSet resultSet;
	
	public AOAggregator(MultipleAttributeShapefilePopulation origins, MultipleAttributeShapefilePopulation destinations, List<Integer> thresholds, List<Date> depTimes) {
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
		for (MultipleAttributeIndividual target : destinations) {
			double time = travelTimes.results[destI];
			int threshI = 0;
			for (Double threshold : thresholds) {
				if (time >= 0 && time <= threshold) {
					int valI = 0;
					for (String value : valueAttributes) {
						resultSet.results[originI][deptimeI][threshI][valI] += target.values[valI];
						valI++;
					}
				}
				threshI++;
			}
			destI ++;				
		}
		
	}
	
    public void writeCVS(String outFileName) {
        LOG.debug("Writing aggregate results to CSV: {}", outFileName);
        
        List<String> headers = new ArrayList<String>();
        headers.add("label");
        headers.add("deptime");
        headers.add("threshold");
        for (String valueAttribute : valueAttributes) {
        	headers.add(valueAttribute);
        }
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        
        try {
            CsvWriter writer = new CsvWriter(outFileName, ',', Charset.forName("UTF8"));
            writer.writeRecord(headers.toArray(new String[1]));
            
            for (int originI=0; originI < origins.size(); originI++) {
            	String label = origins.getIndividuals().get(originI).label;
            	
            	for (int deptimeI=0; deptimeI < depTimes.size(); deptimeI++) {
            		String deptime = sdf.format(depTimes.get(deptimeI));
            		
            		for (int threshI=0; threshI < thresholds.size(); threshI++) {
            			String threshold = thresholds.get(threshI).toString();
            			
            			List<String> row = new ArrayList<String>();
            			row.add(origins.getIndividuals().get(originI).label)
            			for () {
            				
            			}
            		}
            	}
            }
            
            
            int i = 0;
            int j = 0;
            // using internal list rather than filtered iterator
            for (Individual indiv : this.individuals) {
                if ( ! this.skip[i]) {
                    String[] entries = new String[] { 
                            indiv.label, Double.toString(indiv.lat), Double.toString(indiv.lon), 
                            Double.toString(indiv.input), Double.toString(results.results[j]) 
                    };
                    writer.writeRecord(entries);
                    j++;
                }
                i++;
            }
            writer.close(); // flush writes and close
        } catch (Exception e) {
            LOG.error("Error while writing to CSV file: {}", e.getMessage());
            return;
        }
        LOG.debug("Done writing population to CSV at {}.", outFileName);
    }
	
	private class AggregateResultSet {
		
		protected double [][][][] results;
		
		protected AggregateResultSet(MultipleAttributeShapefilePopulation origins, List<Date> depTimes, List<Integer> thresholds, List<String> valueAttributes) {
			this.results = new double[origins.size()][depTimes.size()][thresholds.size()][valueAttributes.size()];
		}
	}
	
}
