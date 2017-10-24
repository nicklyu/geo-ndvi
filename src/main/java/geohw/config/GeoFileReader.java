package geohw.config;

import geohw.data.Band;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.feature.FeatureCollection;
import org.geotools.gce.geotiff.GeoTiffReader;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

@Component
public class GeoFileReader {

    public Band readTiffFile(String path) throws IOException {
        GeoTiffReader reader = new GeoTiffReader(new File(path));
        Band nir = new Band();
        nir.setCoverage(reader.read(null));
        nir.setImage(nir.getCoverage().getRenderedImage());
        return nir;
    }
    public FeatureCollection<SimpleFeatureType, SimpleFeature> readShapeFile(String path) throws IOException {
        Map<String, Object> map = new TreeMap<>();
        map.put("url", new File(path).toURI().toURL());
        DataStore dataStore = DataStoreFinder.getDataStore(map);
        return dataStore.getFeatureSource(dataStore.getTypeNames()[0]).getFeatures();
    }
}
