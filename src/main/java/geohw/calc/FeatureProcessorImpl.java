package geohw.calc;

import geohw.data.Ndvi;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.opengis.feature.Feature;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.FeatureType;
import org.springframework.stereotype.Component;

@Component
public class FeatureProcessorImpl implements FeatureProcessor{
    @Override
    public SimpleFeatureType getUpdatedFeatureType(FeatureType featureType) {
        SimpleFeatureTypeBuilder typeBuilder = new SimpleFeatureTypeBuilder();

        typeBuilder.setName(featureType.getName());
        typeBuilder.setCRS(featureType.getCoordinateReferenceSystem());

        for (AttributeDescriptor attributeDescriptor : ((SimpleFeatureType)featureType).getAttributeDescriptors())
            typeBuilder.add(attributeDescriptor);

        typeBuilder.add("Min", Double.class);
        typeBuilder.add("Max", Double.class);
        typeBuilder.add("Average", Double.class);

        return typeBuilder.buildFeatureType();
    }

    @Override
    public Feature getUpdatedFeature(Feature feature, FeatureType featureType, Ndvi ndvi) {
        SimpleFeatureBuilder builder = new SimpleFeatureBuilder((SimpleFeatureType)featureType);

        for (Property property : feature.getProperties()) {
            builder.set(property.getName(), property.getValue());
        }

        builder.set("Min", ndvi.getMin());
        builder.set("Max", ndvi.getMax());
        builder.set("Average", ndvi.getAverage());

        return builder.buildFeature(((SimpleFeature)feature).getID());
    }
}
