package edu.umn.ao.ao_batch_analyst;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.Query;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.referencing.CRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opentripplanner.analyst.batch.ShapefilePopulation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public class MultipleAttributeShapefilePopulation extends MultipleAttributeBasicPopulation {

	private static final Logger LOG = LoggerFactory.getLogger(ShapefilePopulation.class);
	
	@Setter @Getter String labelAttribute;
	@Setter @Getter List<String> valueAttributes;
	
	public MultipleAttributeShapefilePopulation() {
		
	}
	
	public MultipleAttributeShapefilePopulation(String sourceFilename, String labelAttribute, List<String> valueAttributes) {
		this.sourceFilename = sourceFilename;
		this.labelAttribute = labelAttribute;
		this.valueAttributes = valueAttributes;
		this.setup();
	}
	
	@Override
	public void createIndividuals() {
        String filename = this.sourceFilename;
        LOG.debug("Loading population from shapefile {}", filename);
        LOG.debug("Feature attributes: values in {}, labeled with {}", valueAttributes, labelAttribute);
        try {
            File file = new File(filename);
            FileDataStore store = FileDataStoreFinder.getDataStore(file);
            SimpleFeatureSource featureSource = store.getFeatureSource();

            CoordinateReferenceSystem sourceCRS = featureSource.getInfo().getCRS();
            CoordinateReferenceSystem WGS84 = CRS.decode("EPSG:4326", true);

            Query query = new Query();
            query.setCoordinateSystem(sourceCRS);
            query.setCoordinateSystemReproject(WGS84);
            SimpleFeatureCollection featureCollection = featureSource.getFeatures(query);

            SimpleFeatureIterator it = featureCollection.features();
            int i = 0;
            while (it.hasNext()) {
                SimpleFeature feature = it.next();
                Geometry geom = (Geometry) feature.getDefaultGeometry();
                Point point = null;
                if (geom instanceof Point) {
                    point = (Point) geom;
                } else if (geom instanceof Polygon) {
                    point = ((Polygon) geom).getCentroid();
                } else if (geom instanceof MultiPolygon) {
                    point = ((MultiPolygon) geom).getCentroid();
                } else {
                    throw new RuntimeException("Shapefile must contain either points or polygons.");
                }
                String label;
                if (labelAttribute == null) {
                    label = Integer.toString(i);
                } else {
                    label = feature.getAttribute(labelAttribute).toString();
                }
                
                double [] values = new double[valueAttributes.size()];
                if (valueAttributes != null) {
	                for (int v=0; v < valueAttributes.size(); v++) {
	                	try {
	                		Number n = (Number) feature.getAttribute(valueAttributes.get(v));
	                		values[v] = n.doubleValue();
	                	} catch (NullPointerException e) {
	                		LOG.debug("Null value for individual {} attribute {}, defaulting to 0.0", label, valueAttributes.get(v));
	                		values[v] = 0.0;
	                	}
	                }
                }
                
                MultipleAttributeIndividual individual = new MultipleAttributeIndividual(i, label, point.getX(), point.getY(), values);
                this.addIndividual(individual);
                i += 1;
            }
            LOG.debug("loaded {} features", i);
            it.close();
        } catch (Exception ex) {
            LOG.error("Error loading population from shapefile: {}", ex.getMessage());
            throw new RuntimeException(ex);
        }
        LOG.debug("Done loading shapefile.");
	}

}

