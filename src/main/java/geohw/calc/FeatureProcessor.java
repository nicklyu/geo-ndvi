package geohw.calc;

import geohw.data.Ndvi;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.FeatureType;

public interface FeatureProcessor {
    SimpleFeatureType getUpdatedFeatureType(FeatureType featureType);
    Feature getUpdatedFeature(Feature feature, FeatureType featureType, Ndvi ndvi);
}
