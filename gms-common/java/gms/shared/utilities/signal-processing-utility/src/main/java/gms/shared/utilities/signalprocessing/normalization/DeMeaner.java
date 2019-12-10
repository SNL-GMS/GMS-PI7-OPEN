package gms.shared.utilities.signalprocessing.normalization;

import java.util.Arrays;
import java.util.Objects;
import java.util.OptionalDouble;
import org.apache.commons.lang3.Validate;

public class DeMeaner {

  private DeMeaner() {
    // prevent instantiation
  }

  /**
   * Demeans (centers the mean on zero) a data set
   * @param data the data set to demean
   * @return the demeaned data
   */
  public static double[] demean(double[] data) {
    Objects.requireNonNull(data, "Cannot demean data from a null dataset");

    Validate.isTrue(data.length > 0, "Cannot demean data for an empty dataset");

    OptionalDouble possibleMean = Arrays.stream(data).average();

    if (possibleMean.isPresent()) {
      double mean = possibleMean.getAsDouble();

      return Arrays.stream(data)
          .map(value -> value - mean)
          .toArray();
    } else {
      throw new IllegalArgumentException("Cannot demean data when an average cannot be calculated");
    }
  }

}
