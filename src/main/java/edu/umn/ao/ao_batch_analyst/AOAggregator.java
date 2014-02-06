package edu.umn.ao.ao_batch_analyst;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.text.SimpleDateFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.csvreader.CsvWriter;

public class AOAggregator {
	
	private static final Logger LOG = LoggerFactory.getLogger(AOAggregator.class);
	
	private final MultipleAttributePopulation origins;
	private final MultipleAttributePopulation destinations;
	private final String labelAttribute;
	private final List<String> valueAttributes;
	private final List<Date> depTimes;
	private final List<Integer> thresholds;
	private double [][][][] aggregateResults;
	
	public AOAggregator(MultipleAttributePopulation origins, MultipleAttributePopulation destinations, List<Integer> thresholds, List<Date> depTimes) {
		this.origins = origins;
		this.destinations = destinations;
		this.thresholds = thresholds;
		this.depTimes = depTimes;
		this.labelAttribute = origins.getLabelAttribute();
		this.valueAttributes = destinations.getValueAttributes();
		this.aggregateResults = new double[origins.size()][depTimes.size()][thresholds.size()][valueAttributes.size()];
	}
	
	public void computeAggregate(MultipleAttributeIndividual origin, Date depTime, ResultSet travelTimes) {
		computeAggregate(origin.id, depTimes.indexOf(depTime), travelTimes);
	}
	
	public void computeAggregate(int originI, int deptimeI, ResultSet travelTimes) {
		int destI = 0;
		for (MultipleAttributeIndividual target : destinations) {
			double time = travelTimes.results[destI];
			int threshI = 0;
			for (Integer threshold : thresholds) {
				if (time >= 0 && time <= threshold) {
					for (int valI = 0; valI < valueAttributes.size(); valI++) {
						aggregateResults[originI][deptimeI][threshI][valI] += target.values[valI];
						valI++;
					}
				}
				threshI++;
			}
			destI ++;				
		}
		
	}
	
	public void setAggregate(MultipleAttributeIndividual origin, Date depTime, double aggregateValue) {
		setAggregate(origin.id, depTimes.indexOf(depTime), aggregateValue);
	}
	
	public void setAggregate(int originI, int deptimeI, double aggregateValue) {
		int threshI = 0;
		for (Integer threshold : thresholds) {
			int valI = 0;
			for (String value : valueAttributes) {
				aggregateResults[originI][deptimeI][threshI][valI] = aggregateValue;
				valI++;
			}
			threshI++;
		}			
	}
	
    public void writeCVS(String outFileName) {
        LOG.info("Writing aggregate results to CSV: {}", outFileName);
        
        int rowSize = 3 + valueAttributes.size();
        
        String [] row = new String[rowSize];
        row[0] = "label";
        row[1] = "deptime";
        row[2] = "threshold";
        for (int v = 0; v < valueAttributes.size() ;v++) {
        	row[v+3] = valueAttributes.get(v);
        }
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmm");
        
        try {
            CsvWriter writer = new CsvWriter(outFileName, ',', Charset.forName("UTF8"));
            writer.writeRecord(row);
            
            for (int originI=0; originI < origins.size(); originI++) {
            	row[0] = origins.getIndividuals().get(originI).label;
            	
            	for (int deptimeI=0; deptimeI < depTimes.size(); deptimeI++) {
            		row[1] = sdf.format(depTimes.get(deptimeI));
            		
            		for (int threshI=0; threshI < thresholds.size(); threshI++) {
            			row[2] = thresholds.get(threshI).toString();
            			
            			for (int valI = 0; valI < valueAttributes.size(); valI++) {
            				row[valI+3] = Double.toString(aggregateResults[originI][deptimeI][threshI][valI]);
            			}
            			writer.writeRecord(row);
            		}
            	}
            }
            writer.close();
        } catch (Exception e) {
            LOG.error("Error while writing to CSV file: {}", e.getMessage());
            return;
        }
        LOG.info("Done writing population to CSV: {}.", outFileName);
    }
}
