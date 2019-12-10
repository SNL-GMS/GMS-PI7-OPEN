package gms.shared.utilities.geomath;

import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;
import org.apache.commons.lang3.Validate;

/**
 *
 */
public class GlobalEarthModelProperties {

  private static double globalSlownessUncertainty;
  private static double[] globalAzimuthUncertaintyStops;
  private static double[] globalAzimuthUncertaintyValues;

  private GlobalEarthModelProperties() {
  }

  static {
    Properties p = new Properties();
    try {
      p.load(GlobalEarthModelProperties.class.getResourceAsStream("application.properties"));
    } catch (IOException e) {
      throw new IllegalArgumentException(
          "application.properties file not found for GlobalEarthModelProperties static initialization");
    }

    Validate.isTrue(p.containsKey("globalslownessuncertainty"),
        "application.properties missing globalslownessuncertainty");
    globalSlownessUncertainty = Double.parseDouble(p.getProperty("globalslownessuncertainty"));

    Validate.isTrue(p.containsKey("globalazimuthuncertaintystops"),
        "application.properties missing globalazimuthuncertaintystops");
    globalAzimuthUncertaintyStops = Arrays
        .stream(p.getProperty("globalazimuthuncertaintystops").split(","))
        .mapToDouble(Double::parseDouble)
        .toArray();

    Validate.isTrue(p.containsKey("globalazimuthuncertaintyvalues"),
        "application.properties missing globalazimuthuncertaintyvalues");
    globalAzimuthUncertaintyValues = Arrays
        .stream(p.getProperty("globalazimuthuncertaintyvalues").split(","))
        .mapToDouble(Double::parseDouble)
        .toArray();

    Validate
        .isTrue(globalAzimuthUncertaintyStops.length + 1 == globalAzimuthUncertaintyValues.length);
  }

  public static double getGlobalSlownessUncertainty() {
    return globalSlownessUncertainty;
  }

  public static double[] getGlobalAzimuthUncertaintyStops() {
    return globalAzimuthUncertaintyStops;
  }

  public static double[] getGlobalAzimuthUncertaintyValues() {
    return globalAzimuthUncertaintyValues;
  }
}
