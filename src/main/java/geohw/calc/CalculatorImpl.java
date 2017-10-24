package geohw.calc;

import geohw.config.GeoFileReader;
import geohw.data.Band;
import geohw.data.Ndvi;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridCoverageFactory;
import org.geotools.coverage.processing.CoverageProcessor;
import org.geotools.data.DataUtilities;
import org.geotools.data.FeatureSource;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.referencing.CRS;
import org.opengis.coverage.Coverage;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.FeatureType;
import org.opengis.geometry.Envelope;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.operation.TransformException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;
import javax.media.jai.operator.FormatDescriptor;
import java.awt.*;
import java.awt.image.DataBuffer;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class CalculatorImpl implements Calculator{

    private final String NIR_BAND = "nir.path";
    private final String RED_BAND = "red.path";
    private final String SHAPE = "shape.path";

    private Band nirBand;
    private Band redBand;
    private FeatureCollection<SimpleFeatureType, SimpleFeature> shape;

    @Resource
    private Environment ev;

    private GeoFileReader reader;
    private FeatureProcessor processor;

    public CalculatorImpl(GeoFileReader reader, FeatureProcessor processor) {
        this.reader = reader;
        this.processor = processor;
    }

    @Override
    public void Initialize() throws IOException {
        nirBand = reader.readTiffFile(ev.getRequiredProperty(NIR_BAND));
        redBand = reader.readTiffFile(ev.getRequiredProperty(RED_BAND));
        shape = reader.readShapeFile(ev.getRequiredProperty(SHAPE));
    }

    @Override
    public FeatureCollection<SimpleFeatureType, SimpleFeature> calculateUpdatedFeatures() throws TransformException {

        List<SimpleFeature> updatedFeatures = new ArrayList<>();
        FeatureType featureType = processor.getUpdatedFeatureType(shape.getSchema());

        try(FeatureIterator<SimpleFeature> iterator = shape.features()){
            while (iterator.hasNext()){
                SimpleFeature feature = iterator.next();
                Coverage boundaryNdviCoverage = getBoundaryNdviCoverage(feature.getBounds());
                Ndvi ndvi = getNdviData(boundaryNdviCoverage);
                updatedFeatures.add((SimpleFeature)processor.getUpdatedFeature(feature,featureType, ndvi));
            }
        }
        return DataUtilities.collection(updatedFeatures);
    }

    private Coverage getBoundaryNdviCoverage(Envelope envelope) throws TransformException {
        Coverage boxedNirCoverage = cropCoverage(nirBand.getCoverage(), envelope),
                 boxedRedCoverage = cropCoverage(redBand.getCoverage(), envelope);

        RenderedImage boxedNirImage = ((GridCoverage2D)boxedNirCoverage).getRenderedImage(),
                      boxedRedImage = ((GridCoverage2D)boxedRedCoverage).getRenderedImage();

        ParameterBlock pbSubtracted = new ParameterBlock();
        pbSubtracted.addSource(boxedNirImage);
        pbSubtracted.addSource(boxedRedImage);
        RenderedOp subtractedImage = JAI.create("subtract", pbSubtracted);

        ParameterBlock pbAdded = new ParameterBlock();
        pbAdded.addSource(boxedNirImage);
        pbAdded.addSource(boxedRedImage);
        RenderedOp addedImage = JAI.create("add", pbAdded);

        RenderedOp typeAdd = FormatDescriptor.create(addedImage, DataBuffer.TYPE_DOUBLE, null);
        RenderedOp typeSub = FormatDescriptor.create(subtractedImage, DataBuffer.TYPE_DOUBLE, null);
        ParameterBlock pbNDVI = new ParameterBlock();
        pbNDVI.addSource(typeSub);
        pbNDVI.addSource(typeAdd);

        RenderedOp NDVIop = JAI.create("divide", pbNDVI);
        GridCoverageFactory gridCoverageFactory = new GridCoverageFactory();
        return gridCoverageFactory.create("Raster", NDVIop, redBand.getCoverage().getEnvelope());



    }

    private Coverage cropCoverage(Coverage coverage, Envelope envelope) throws TransformException {
        CoverageProcessor processor = CoverageProcessor.getInstance();
        GeneralEnvelope transformedEnvelope = CRS.transform(envelope, coverage.getCoordinateReferenceSystem());
        final ParameterValueGroup param = processor.getOperation("CoverageCrop").getParameters();
        param.parameter("Source").setValue(coverage);
        param.parameter("Envelope").setValue(transformedEnvelope);
        return processor.doOperation(param);
    }

    private Ndvi getNdviData(Coverage coverage){
        RenderedImage image = ((GridCoverage2D) coverage).getRenderedImage();
        double min = Double.MAX_VALUE, max = Double.MIN_VALUE, average = 0;
        double[] ndviValues = image.getData().getPixels(image.getMinX(), image.getMinY(), image.getData().getWidth(), image.getData().getHeight(), (double[]) null);
        for (double ndvi : ndviValues) {
            if (ndvi < min)
                min = ndvi;
            if (ndvi > max)
                max = ndvi;
            average += ndvi;
        }
        average /= ndviValues.length;
        return Ndvi.builder().min(min).max(max).average(average).build();
    }


}
