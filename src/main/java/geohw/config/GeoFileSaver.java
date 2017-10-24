package geohw.config;

import org.geotools.data.DefaultTransaction;
import org.geotools.data.Transaction;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.feature.FeatureCollection;
import org.geotools.geojson.feature.FeatureJSON;
import org.geotools.kml.KML;
import org.geotools.kml.KMLConfiguration;
import org.geotools.xml.Encoder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class GeoFileSaver {
    public static void save(FeatureCollection<SimpleFeatureType, SimpleFeature> featureCollection, SaveMode mode) throws IOException {
        switch (mode){
            case SHAPE:{
                saveShapeFile(featureCollection);
            }
            case KML:{
                saveKmlFile(featureCollection);
            }
            case GEOJSON:{
                saveGeojsonFile(featureCollection);
            }
            case ALL:{
                saveShapeFile(featureCollection);
                saveKmlFile(featureCollection);
                saveGeojsonFile(featureCollection);
            }
    }
}

    private static void saveShapeFile(FeatureCollection<SimpleFeatureType, SimpleFeature> featureCollection) throws IOException {
        File resultFile = new File("result.shp");
        if(!resultFile.exists())
            resultFile.createNewFile();

        Map<String, Serializable> params = new HashMap<>();
        params.put("url", resultFile.toURI().toURL());
        params.put("create spatial index", Boolean.TRUE);
        ShapefileDataStore newDataStore = (ShapefileDataStore) new ShapefileDataStoreFactory().createNewDataStore(params);

        newDataStore.createSchema(featureCollection.getSchema());
        SimpleFeatureStore featureStore = (SimpleFeatureStore) newDataStore.getFeatureSource(newDataStore.getTypeNames()[0]);
        Transaction transaction = new DefaultTransaction();
        try {
            featureStore.addFeatures(featureCollection);
            transaction.commit();
        } catch (IOException ex) {
            transaction.rollback();
        } finally {
            transaction.close();
        }
    }

    private static void saveKmlFile(FeatureCollection<SimpleFeatureType, SimpleFeature> featureCollection) throws IOException {
        File resultFile = new File("result.kml");
        if(!resultFile.exists())
            resultFile.createNewFile();

        Encoder encoder = new Encoder(new KMLConfiguration());
        encoder.setIndenting(true);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        encoder.encode(featureCollection, KML.kml,  bos);
        try(FileWriter writer = new FileWriter(resultFile)) {
            writer.write(bos.toString());
        }
    }

    private static void saveGeojsonFile(FeatureCollection<SimpleFeatureType, SimpleFeature> featureCollection) throws IOException {
        File resultFile = new File("result.geojson");
        if(!resultFile.exists())
            resultFile.createNewFile();

        FeatureJSON featureJSON = new FeatureJSON();
        featureJSON.writeFeatureCollection(featureCollection, resultFile);
    }
}
