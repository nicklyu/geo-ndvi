package geohw.data;

import lombok.Data;
import org.geotools.coverage.grid.GridCoverage2D;

import java.awt.image.RenderedImage;

@Data
public class Band {
    private RenderedImage image;
    private GridCoverage2D coverage;
}
