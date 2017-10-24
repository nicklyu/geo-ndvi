package geohw.calc;

import org.geotools.feature.FeatureCollection;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.operation.TransformException;

import java.io.IOException;

public interface Calculator {
    void Initialize() throws IOException;
    FeatureCollection<SimpleFeatureType, SimpleFeature> calculateUpdatedFeatures() throws TransformException;

}
