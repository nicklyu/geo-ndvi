package geohw.data;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Ndvi {
    private double min;
    private double max;
    private double average;
}
