package edu.umn.ao.ao_batch_analyst;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import lombok.Getter;
import lombok.Setter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.csvreader.CsvWriter;

public class BasicPopulation implements Population {
    private static final Logger LOG = LoggerFactory.getLogger(BasicPopulation.class);
    
    @Setter 
    public String sourceFilename;
    
    @Setter @Getter 
    public List<Individual> individuals = new ArrayList<Individual>(); 
    

    private boolean[] skip = null;
    
    public BasicPopulation() {  }

    public BasicPopulation(Individual... individuals) {
        this.individuals = Arrays.asList(individuals);
    }
    
    public BasicPopulation(Collection<Individual> individuals) {
        this.individuals = new ArrayList<Individual>(individuals);
    }

    public void addIndividual(Individual individual) {
        this.individuals.add(individual);
    }

    public Iterator<Individual> iterator() {
        return new PopulationIterator();
    }

    public void clearIndividuals(List<Individual> individuals) {
        this.individuals.clear();
    }

    public void createIndividuals() {
        // nothing to do in the basic population case
    }

    public int size() {
        return this.individuals.size();
    }
        
    protected void writeCsv(String outFileName, ResultSet results) {
        LOG.debug("Writing population to CSV: {}", outFileName);
        try {
            CsvWriter writer = new CsvWriter(outFileName, ',', Charset.forName("UTF8"));
            writer.writeRecord( new String[] {"label", "lat", "lon", "input", "output"} );
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
    
    protected void writeCsv(String outFileName, MultipleResultSet results) {
    	LOG.debug("Writing results to CSV: {}", outFileName);
    	try {
    		CsvWriter writer = new CsvWriter(outFileName, ',', Charset.forName("UTF8"));
    		writer.writeRecord( new String[] {"label", "key", "input"});
    		int i = 0;
    		for (Individual indiv : this.individuals) {
    			double [] iresults = results.results[i];
    			int k = 0;
    			for (double key : iresults) {
    				writer.writeRecord(new String [] {	indiv.label,
    													Double.toString(results.keys[k]),
    													Double.toString(results.results[i][k])	});
    				k++;
    			}
    			i++;
    		}
    		writer.close();
    	} catch (Exception e){
    		LOG.error("Error while writing to CSV file: {}", e.getMessage());
            return;
    	}
    	LOG.debug("Done writing results to CSV file {}", outFileName);
    }

    public void writeAppropriateFormat(String outFileName, ResultSet results) {
        // as a default, save to CSV. override this method in subclasses when more is known about data structure.
        this.writeCsv(outFileName, results);
    }
    
    public void setup() {
        // call the subclass-specific file loading method
        this.createIndividuals();
    }

    class PopulationIterator implements Iterator<Individual> {

        int i = 0;
        int n = individuals.size();
        Iterator<Individual> iter = individuals.iterator();
        
        public boolean hasNext() {
            while (i < n && skip[i]) {
                //LOG.debug("in iter, i = {}", i);
                if (! iter.hasNext())
                    return false;
                i += 1;
                iter.next();
            }
            //LOG.debug("done skipping at {}", i);
            return iter.hasNext();
        }
        
        public Individual next() {
            if (this.hasNext()) {
                Individual ret = iter.next();
                i += 1;
                return ret;
            } else {
                throw new NoSuchElementException();
            }
        }

        public void remove() {
            throw new UnsupportedOperationException(); 
        }
        
    }

}
