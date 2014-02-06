package edu.umn.ao.ao_batch_analyst;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.text.SimpleDateFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.csvreader.CsvWriter;

import lombok.Setter;
import lombok.Getter;

import javax.annotation.Resource;
import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;

public class AOAggregator {
	
	private static final Logger LOG = LoggerFactory.getLogger(AOAggregator.class);
	
	@Resource @Setter private MultipleAttributePopulation origins;
	@Resource @Setter private MultipleAttributePopulation destinations;
	
	@Autowired @Setter private DepartureTimeListGenerator depTimeGenerator;
	
	@Setter @Getter private int [] thresholds;
	
	private String [] valueAttributes;
	private List<Date> depTimes;
	private double [][][][] aggregateResults;
	
	public AOAggregator() {
		
	}
	
	@PostConstruct
	public void initializeComponent() {
		valueAttributes = destinations.getValueAttributes();
		depTimes = depTimeGenerator.getDepartureTimes();
		this.aggregateResults = new double[origins.size()][depTimes.size()][thresholds.length][valueAttributes.length];
	}
	
	public AOAggregator(MultipleAttributePopulation origins, MultipleAttributePopulation destinations, int [] thresholds, List<Date> depTimes) {
		this.origins = origins;
		this.destinations = destinations;
		this.thresholds = thresholds;
		this.depTimes = depTimes;
		this.valueAttributes = destinations.getValueAttributes();
		this.aggregateResults = new double[origins.size()][depTimes.size()][thresholds.length][valueAttributes.length];
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
					int valI = 0;
					for (double value : target.values) {
						aggregateResults[originI][deptimeI][threshI][valI] += value;
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
		for (int threshI = 0; threshI < thresholds.length; threshI++) {
			for (int valI = 0; valI < valueAttributes.length; valI++) {
				aggregateResults[originI][deptimeI][threshI][valI] = aggregateValue;
			}
		}			
	}
	
    public void writeCVS(String outFileName) {
        LOG.info("Writing aggregate results to CSV: {}", outFileName);
        
        int rowSize = 3 + valueAttributes.length;
        
        String [] row = new String[rowSize];
        row[0] = "label";
        row[1] = "deptime";
        row[2] = "threshold";
        for (int v = 0; v < valueAttributes.length ;v++) {
        	row[v+3] = valueAttributes[v];
        }
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmm");
        
        try {
            CsvWriter writer = new CsvWriter(outFileName, ',', Charset.forName("UTF8"));
            writer.writeRecord(row);
            
            for (int originI=0; originI < origins.size(); originI++) {
            	row[0] = origins.getIndividuals().get(originI).label;
            	
            	for (int deptimeI=0; deptimeI < depTimes.size(); deptimeI++) {
            		row[1] = sdf.format(depTimes.get(deptimeI));
            		
            		for (int threshI=0; threshI < thresholds.length; threshI++) {
            			row[2] = Double.toString(thresholds[threshI]);
            			
            			for (int valI = 0; valI < valueAttributes.length; valI++) {
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
