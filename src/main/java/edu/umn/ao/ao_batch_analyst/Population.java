package edu.umn.ao.ao_batch_analyst;

import java.util.List;

public interface Population extends Iterable<Individual>{

    /** 
     * @return a list of all Individuals in this Population, including those that have been 
     * marked as rejected by the filter chain.
     */
    public List<Individual> getIndividuals();

    public void addIndividual(Individual individual);

    public void clearIndividuals(List<Individual> individuals);

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

    /**
     * Save the output data in this population to a file, using a format that is appropriate for the 
     * specific class of population. For example, a population loaded from an image file or 
     * generated on a regular grid will be saved as a Geotiff raster. A population of points that 
     * are not known to be aligned on a regular grid in some CRS will be saved as a CSV file.
     */
    public void writeAppropriateFormat(String fileName, ResultSet results);
    
}
