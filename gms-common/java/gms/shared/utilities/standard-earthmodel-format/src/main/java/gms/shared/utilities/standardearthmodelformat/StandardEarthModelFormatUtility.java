package gms.shared.utilities.standardearthmodelformat;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.util.Objects;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.commons.math3.analysis.interpolation.SplineInterpolator;

//TODO: need javadoc comments
/**
 *
 */
public class StandardEarthModelFormatUtility {

  /**
   * Retrieve uncertainties and errors from the standard format earthmodel file (via a buffered
   * reader)
   *
   * @param reader reader that reads the data
   * @return table of uncertainty/error values with axis values
   */
  public static Triple<double[], double[], double[][]> retrieveErrorsUncertainties(
      BufferedReader reader)
      throws IOException {
    String line = parseLine(reader);

    if (Objects.isNull(line)) {
      return null;
    }

    int nummodelingErrorDistances = Integer.parseInt(line.split("\\s+")[0]);
    int nummodelingErrorDepths = Integer.parseInt(line.split("\\s+")[1]);

    double[] modelingErrorDistances;

    if (nummodelingErrorDistances == 1) {
      modelingErrorDistances = null;
    } else {
      modelingErrorDistances = new double[nummodelingErrorDistances];

      // Tracks number of depth samples read
      int modelingErrorDistancesRead = 0;
      while (modelingErrorDistancesRead < nummodelingErrorDistances) {
        line = parseLine(reader);
        if (Objects.isNull(line)) {
          throw new EOFException("EarthModel1D file ended prematurely.");
        }

        String[] values = line.split("\\s+");

        for (String value : values) {
          modelingErrorDistances[modelingErrorDistancesRead] = Double.parseDouble(value);
          modelingErrorDistancesRead++;
        }
      }
    }

    double[] modelingErrorDepths;

    if (nummodelingErrorDepths == 1) {
      modelingErrorDepths = null;
    } else {
      modelingErrorDepths = new double[nummodelingErrorDepths];

      // Tracks number of depth samples read
      int modelingErrorDepthsRead = 0;
      while (modelingErrorDepthsRead < nummodelingErrorDepths) {
        line = parseLine(reader);
        if (Objects.isNull(line)) {
          throw new EOFException("EarthModel1D file ended prematurely.");
        }

        String[] values = line.split("\\s+");

        for (String value : values) {
          modelingErrorDepths[modelingErrorDepthsRead] = Double.parseDouble(value);
          modelingErrorDepthsRead++;
        }
      }
    }

    // Create modelingErrorValues array
    double[][] modelingErrorValues = new double[nummodelingErrorDepths][nummodelingErrorDistances];
    for (int i = 0; i < nummodelingErrorDepths; i++) {
      for (int j = 0; j < nummodelingErrorDistances; j++) {
        line = parseLine(reader);
        if (Objects.isNull(line)) {
          throw new EOFException("EarthModel1D file ended prematurely.");
        }

        modelingErrorValues[i][j] = Double.parseDouble(line);
      }
    }

    return Triple.of(modelingErrorDistances, modelingErrorDepths, modelingErrorValues);
  }

  //TODO: need javadoc comments
  /**
   *
   * @param distance
   * @param distances
   * @param uncertainties
   * @return
   */
  public static double interpolateUncertainties(double distance, double[] distances,
      double[] uncertainties) {
    return new SplineInterpolator().interpolate(distances, uncertainties).value(distance);
  }

  // Returns the next non-commented line as a String.  This is a utility method to make it
  // easier to ignore commented-out lines in earth model files.
  private static String parseLine(BufferedReader reader) throws IOException {

    String line = reader.readLine();
    if (Objects.nonNull(line)) {
      line = line.trim();
    } else {
      return null;
    }

    while (line.charAt(0) == '#') {
      line = reader.readLine();

      if (Objects.nonNull(line)) {
        line = line.trim();
      } else {
        return null;
      }
    }

    return line;
  }

}
