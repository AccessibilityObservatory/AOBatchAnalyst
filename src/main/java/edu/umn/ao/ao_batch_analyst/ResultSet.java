package edu.umn.ao.ao_batch_analyst;

import org.opentripplanner.analyst.core.Sample;
import org.opentripplanner.routing.spt.ShortestPathTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResultSet {

    private static final Logger LOG = LoggerFactory.getLogger(ResultSet.class);

    public MultipleAttributePopulation population;
    public double[] results;
    
    public static ResultSet forTravelTimes(MultipleAttributePopulation population, ShortestPathTree spt) {
        double[] results = new double[population.size()];
        int i = 0;
        for (MultipleAttributeIndividual indiv : population) {
            Sample s = indiv.sample;
            long t = Long.MAX_VALUE;
            if (s == null)
                t = -2;
            else
                t = s.eval(spt);
            if (t == Long.MAX_VALUE)
                t = -1;
            results[i] = t;
            i++;
        }
        return new ResultSet(population, results);
    }
    
    public ResultSet(MultipleAttributePopulation population, double[] results) {
        this.population = population;
        this.results = results;
    }
    
    protected ResultSet(MultipleAttributePopulation population) {
        this.population = population;
        this.results = new double[population.size()];
    }
    
}
