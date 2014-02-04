package edu.umn.ao.ao_batch_analyst;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import lombok.Getter;
import lombok.Setter;

import org.opentripplanner.analyst.batch.IndividualFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MultipleAttributeBasicPopulation implements MultipleAttributePopulation {

    private static final Logger LOG = LoggerFactory.getLogger(MultipleAttributeBasicPopulation.class);
    
    @Setter 
    public String sourceFilename;
    
    @Setter @Getter 
    public List<MultipleAttributeIndividual> individuals = new ArrayList<MultipleAttributeIndividual>(); 
    
    @Setter @Getter 
    public List<IndividualFilter> filterChain = null; 

    private boolean[] skip = null;
    
    public MultipleAttributeBasicPopulation() {  }

    public MultipleAttributeBasicPopulation(MultipleAttributeIndividual... individuals) {
        this.individuals = Arrays.asList(individuals);
    }
    
    public MultipleAttributeBasicPopulation(Collection<MultipleAttributeIndividual> individuals) {
        this.individuals = new ArrayList<MultipleAttributeIndividual>(individuals);
    }

    public void addIndividual(MultipleAttributeIndividual individual) {
        this.individuals.add(individual);
    }

    public Iterator<MultipleAttributeIndividual> iterator() {
        return new PopulationIterator();
    }

    public void clearIndividuals(List<MultipleAttributeIndividual> individuals) {
        this.individuals.clear();
    }

    public void createIndividuals() {
        // nothing to do in the basic population case
    }

    public int size() {
        return this.individuals.size();
    }

    public void setup() {
        // call the subclass-specific file loading method
        this.createIndividuals();
    }

    class PopulationIterator implements Iterator<MultipleAttributeIndividual> {

        int i = 0;
        int n = individuals.size();
        Iterator<MultipleAttributeIndividual> iter = individuals.iterator();
        
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
        
        public MultipleAttributeIndividual next() {
            if (this.hasNext()) {
            	MultipleAttributeIndividual ret = iter.next();
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


