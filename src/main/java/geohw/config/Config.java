package geohw.config;

import geohw.data.Band;
import org.geotools.data.DataSourceException;
import org.geotools.gce.geotiff.GeoTiffReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

import javax.annotation.Resource;
import java.io.IOException;

@Configuration
@ComponentScan(basePackages = "geohw")
@PropertySource("classpath:application.properties")
public class Config {
    private final String NIR_PATH = "nir.path";
    private final String RED_PATH = "red.path";

    @Resource
    private Environment ev;

    @Bean
    public GeoFileReader geoFileReader(){
        return new GeoFileReader();
    }


}
