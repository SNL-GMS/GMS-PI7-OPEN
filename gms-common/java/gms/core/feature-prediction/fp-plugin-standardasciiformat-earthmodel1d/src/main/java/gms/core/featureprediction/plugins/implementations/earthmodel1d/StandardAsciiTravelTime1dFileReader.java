package gms.core.featureprediction.plugins.implementations.earthmodel1d;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.PhaseType;
import gms.shared.utilities.standardearthmodelformat.StandardEarthModelFormatUtility;
import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.Triple;


/**
 * Immutable 1D Earth Model as read from the flat file
 */
public class StandardAsciiTravelTime1dFileReader {

  private Map<PhaseType, EarthModel1DPhase> phases;

  public static StandardAsciiTravelTime1dFileReader from(String earthModelFolderName) throws IOException {
    return new StandardAsciiTravelTime1dFileReader(loadPhasesForModel(earthModelFolderName));
  }

  private StandardAsciiTravelTime1dFileReader(Map<PhaseType, EarthModel1DPhase> phases) {
    if (Objects.isNull(phases)) {
      throw new NullPointerException(
          "Map<String, EarthModel1DPhase> constructor parameter cannot be null");
    }

    this.phases = phases;
  }

  private static Map<PhaseType, EarthModel1DPhase> loadPhasesForModel(String earthModelFolderName)
      throws IOException {

    Map<PhaseType, EarthModel1DPhase> earthModel1DPhaseMap = new HashMap<>();

    for (PhaseType p : PhaseType.values()) {
      String earthModelPhaseFileName = earthModelFolderName + "/" + p.name().replace("__", "~");

      InputStream is = Thread.currentThread().getContextClassLoader()
          .getResourceAsStream(earthModelPhaseFileName);

      if (Objects.isNull(is)) {
        continue;
      }

      earthModel1DPhaseMap.put(p, EarthModel1DPhase.from(is));
    }

    if (earthModel1DPhaseMap.isEmpty()) {
      throw new IllegalArgumentException(
          "Earth model at \"" + earthModelFolderName + "\" does not contain any phases.");
    }

    return Collections.unmodifiableMap(earthModel1DPhaseMap);
  }

  //TODO: need javadoc comments
  public Set<PhaseType> getPhaseTypes() {
    return this.phases.keySet();
  }

  //TODO: need javadoc comments
  public double[] getDepthKmForPhase(PhaseType p) {
    Validate.isTrue(this.phases.containsKey(p));
    return this.phases.get(p).getDepthKm();
  }

  //TODO: need javadoc comments
  public double[] getAngleDegreesForPhase(PhaseType p) {
    Validate.isTrue(this.phases.containsKey(p));
    return this.phases.get(p).getAngleDegrees();
  }

  //TODO: need javadoc comments
  public double[][] getTravelTimesForPhase(PhaseType p) {
    Validate.isTrue(this.phases.containsKey(p));
    return this.phases.get(p).getTravelTime();
  }

  //TODO: need javadoc comments
  public Optional<double[]> getModelingErrorDistancesForPhase(PhaseType p) {
    Validate.isTrue(this.phases.containsKey(p));
    return this.phases.get(p).getModelingErrorDistances();
  }

  //TODO: need javadoc comments
  public Optional<double[]> getModelingErrorDepthsForPhase(PhaseType p) {
    Validate.isTrue(this.phases.containsKey(p));
    return this.phases.get(p).getModelingErrorDepths();
  }

  //TODO: need javadoc comments
  public Optional<double[][]> getModelingErrorValuesForPhase(PhaseType p) {
    Validate.isTrue(this.phases.containsKey(p));
    return this.phases.get(p).getModelingErrorValues();
  }

  @Override
  public String toString() {
    return "StandardAsciiTravelTime1dFileReader{" +
        "phases=" + phases +
        '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof StandardAsciiTravelTime1dFileReader)) {
      return false;
    }

    StandardAsciiTravelTime1dFileReader that = (StandardAsciiTravelTime1dFileReader) o;

    return phases.equals(that.phases);
  }

  @Override
  public int hashCode() {
    return phases.hashCode();
  }

  private static class EarthModel1DPhase {

    private final double[] depthKm;
    private final double[] angleDegrees;
    private final double[][] travelTime;

    private final double[] modelingErrorDistances;
    private final double[] modelingErrorDepths;
    private final double[][] modelingErrorValues;

    /**
     * @param depthKm 1-D array of depths corresponding to rows in the travelTime table, in km, not
     * null
     * @param angleDegrees 1-D array of distances corresponding to columns in the travelTime table,
     * in degrees, not null
     * @param travelTime 2-D array of travel times.  Used to calculate travel time given a
     * depth/distance pair.
     */
    private EarthModel1DPhase(double[] depthKm, double[] angleDegrees,
        double[][] travelTime, double[] modelingErrorDistances, double[] modelingErrorDepths,
        double[][] modelingErrorValues) {

      this.depthKm = depthKm;
      this.angleDegrees = angleDegrees;
      this.travelTime = travelTime;
      this.modelingErrorDistances = modelingErrorDistances;
      this.modelingErrorDepths = modelingErrorDepths;
      this.modelingErrorValues = modelingErrorValues;
    }

    /**
     * Returns a new EarthModel1D loaded from a file at the specified path
     *
     * @return new EarthModel1D
     */
    static EarthModel1DPhase from(InputStream is) throws IOException {

      // Holds our current line
      String line;

      double[] depthKm;
      double[] angleDegrees;
      double[][] travelTime;

      InputStreamReader inputStreamReader = new InputStreamReader(is);
      BufferedReader reader = new BufferedReader(inputStreamReader);

      // Read in the number of depth samples
      line = parseLine(reader);
      if (Objects.isNull(line)) {
        throw new EOFException("EarthModel1D file ended prematurely.");
      }

      int numDepthSamples = Integer.parseInt(line.split("\\s+")[0]);
      depthKm = new double[numDepthSamples];

      // Tracks number of depth samples read
      int readDepthCount = 0;
      while (readDepthCount < numDepthSamples) {
        line = parseLine(reader);
        if (Objects.isNull(line)) {
          throw new EOFException("EarthModel1D file ended prematurely.");
        }

        String[] values = line.split("\\s+");

        for (String value : values) {
          depthKm[readDepthCount] = Double.parseDouble(value);
          readDepthCount++;
        }
      }

      // Read in the number of distance samples
      line = parseLine(reader);
      if (Objects.isNull(line)) {
        throw new EOFException("EarthModel1D file ended prematurely.");
      }

      int numDistanceSamples = Integer.parseInt(line.split("\\s+")[0]);
      angleDegrees = new double[numDistanceSamples];

      // Tracks number of distance samples read from file
      int readDistanceCount = 0;

      while (readDistanceCount < numDistanceSamples) {
        line = parseLine(reader);
        if (Objects.isNull(line)) {
          throw new EOFException("EarthModel1D file ended prematurely.");
        }

        String[] values = line.split("\\s+");

        for (String value : values) {
          angleDegrees[readDistanceCount] = Double.parseDouble(value);
          readDistanceCount++;
        }
      }

      // Create travelTime array
      travelTime = new double[numDepthSamples][numDistanceSamples];
      for (int i = 0; i < numDepthSamples; i++) {
        for (int j = 0; j < numDistanceSamples; j++) {
          line = parseLine(reader);
          if (Objects.isNull(line)) {
            throw new EOFException("EarthModel1D file ended prematurely.");
          }

          travelTime[i][j] = Double.parseDouble(line);

          // -1.0 represents a NaN value
          if (travelTime[i][j] == -1.0) {
            travelTime[i][j] = Double.NaN;
          }
        }
      }

      // TODO: ^-- abstract the functionality above this
      Triple<double[], double[], double[][]> errorTable = StandardEarthModelFormatUtility
          .retrieveErrorsUncertainties(reader);

      // We hit the end of the file, this file doesn't have error tables
      if (Objects.isNull(errorTable)) {
        return new EarthModel1DPhase(depthKm, angleDegrees, travelTime, null, null, null);
      }

      return new EarthModel1DPhase(depthKm, angleDegrees, travelTime, errorTable.getLeft(),
          errorTable.getMiddle(), errorTable.getRight());
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

    public double[] getDepthKm() {
      return depthKm.clone();
    }

    public double[] getAngleDegrees() {
      return angleDegrees.clone();
    }

    public double[][] getTravelTime() {
      return travelTime.clone();
    }

    public Optional<double[]> getModelingErrorDistances() {
      if (Objects.isNull(modelingErrorDistances)) {
        return Optional.empty();
      }

      return Optional.of(modelingErrorDistances.clone());
    }

    public Optional<double[]> getModelingErrorDepths() {
      if (Objects.isNull(modelingErrorDepths)) {
        return Optional.empty();
      }

      return Optional.of(modelingErrorDepths.clone());
    }

    public Optional<double[][]> getModelingErrorValues() {
      if (Objects.isNull(modelingErrorValues)) {
        return Optional.empty();
      }

      return Optional.of(modelingErrorValues.clone());
    }

    @Override
    public String toString() {
      return "EarthModel1DPhase{" +
          "depthKm=" + Arrays.toString(depthKm) +
          ", angleDegrees=" + Arrays.toString(angleDegrees) +
          ", travelTime=" + Arrays.toString(travelTime) +
          ", modelingErrorDistances=" + Arrays.toString(modelingErrorDistances) +
          ", modelingErrorDepths=" + Arrays.toString(modelingErrorDepths) +
          ", modelingErrorValues=" + Arrays.toString(modelingErrorValues) +
          '}';
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof EarthModel1DPhase)) {
        return false;
      }

      EarthModel1DPhase that = (EarthModel1DPhase) o;

      if (!Arrays.equals(depthKm, that.depthKm)) {
        return false;
      }
      if (!Arrays.equals(angleDegrees, that.angleDegrees)) {
        return false;
      }
      if (!Arrays.deepEquals(travelTime, that.travelTime)) {
        return false;
      }
      if (!Arrays.equals(modelingErrorDistances, that.modelingErrorDistances)) {
        return false;
      }
      if (!Arrays.equals(modelingErrorDepths, that.modelingErrorDepths)) {
        return false;
      }
      return Arrays.deepEquals(modelingErrorValues, that.modelingErrorValues);
    }

    @Override
    public int hashCode() {
      int result = Arrays.hashCode(depthKm);
      result = 31 * result + Arrays.hashCode(angleDegrees);
      result = 31 * result + Arrays.deepHashCode(travelTime);
      result = 31 * result + Arrays.hashCode(modelingErrorDistances);
      result = 31 * result + Arrays.hashCode(modelingErrorDepths);
      result = 31 * result + Arrays.deepHashCode(modelingErrorValues);
      return result;
    }
  }
}
