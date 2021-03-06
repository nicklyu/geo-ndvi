package geohw;

import geohw.calc.Calculator;
import geohw.config.Config;
import geohw.config.GeoFileSaver;
import geohw.config.SaveMode;
import org.geotools.feature.FeatureCollection;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.operation.TransformException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.IOException;

public class Application {
    public static void main(String[] args){
        ApplicationContext context = new AnnotationConfigApplicationContext(Config.class);
        Calculator calculator = context.getBean(Calculator.class);
        try {
            calculator.Initialize();
            FeatureCollection<SimpleFeatureType, SimpleFeature> featureCollection = calculator.calculateUpdatedFeatures();
            GeoFileSaver.save(featureCollection, SaveMode.GEOJSON);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TransformException e) {
            e.printStackTrace();
        }
    }
}
