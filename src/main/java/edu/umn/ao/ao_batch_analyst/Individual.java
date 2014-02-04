package edu.umn.ao.ao_batch_analyst;

import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;

import org.opentripplanner.analyst.core.Sample;

@ToString
public class Individual {
    @Setter public String label;
    @Setter public double lon;
    @Setter public double lat;
    @Setter @NonNull public double input;  // not final to allow clamping and scaling by filters
    public Sample sample= null; // not final, allowing sampling to occur after filterings
    
    public Individual(String label, double lon, double lat, double input) {
        this.label = label;
        this.lon = lon;
        this.lat = lat;
        this.input = input;
    }

    public Individual() { }
 
    // public boolean rejected;

}
