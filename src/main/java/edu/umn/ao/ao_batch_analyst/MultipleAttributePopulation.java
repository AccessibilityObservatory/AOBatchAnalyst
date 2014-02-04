package edu.umn.ao.ao_batch_analyst;

import java.util.List;

import org.opentripplanner.analyst.batch.Individual;
import org.opentripplanner.analyst.batch.MultipleResultSet;
import org.opentripplanner.analyst.batch.ResultSet;

public interface MultipleAttributePopulation extends Iterable<MultipleAttributeIndividual>{

    /** 
     * @return a list of all Individuals in this Population, including those that have been 
     * marked as rejected by the filter chain.
     */
    public List<MultipleAttributeIndividual> getIndividuals();

    public void addIndividual(MultipleAttributeIndividual individual);

    public void clearIndividuals(List<MultipleAttributeIndividual> individuals);

    /** @return the number of individuals in this population. */
    public int size();

    /**
     * Prepare the population for use. This includes loading or generating the individuals, 
     * filtering them, but not sampling (linking them into the graph) because origin populations
     * do not need to be permanently linked.
     */
    public void setup();

    /**
     * Subclass-specific method to load the individuals from a file or create them based on other 
     * properties. This method should fill in all fields of each individual except the sample, 
     * since sampling will be carried out after the filter chain is applied.
     */
    public void createIndividuals();

}

