package gms.core.featureprediction.plugins.implementations.signalfeaturepredictor;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.PhaseType;
import gms.shared.utilities.standardearthmodelformat.StandardEarthModelFormatUtility;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * Azimuth uncertainty (i.e., standard deviation).  Uncertainty values are interpolated (bicubic
 * spline interpolation) from table of uncertainty values for a specific phase of a specific 1D
 * earth model.  This object reads and stores uncertainty tables for all the phases associated with
 * a specific 1D earth model.
 */
public class AzimuthUncertaintyModel1D {

  private static Logger logger = LogManager.getLogger(AzimuthUncertaintyModel1D.class);

  private Map<PhaseType, AzimuthUncertaintyTable> phaseMap;

  static AzimuthUncertaintyModel1D from(String azimuthUncertaintyTableFolderName)
      throws IOException {
    return new AzimuthUncertaintyModel1D(
        loadAzimuthUncertaintyTables(azimuthUncertaintyTableFolderName));
  }

  private AzimuthUncertaintyModel1D(Map<PhaseType, AzimuthUncertaintyTable> phaseMap) {
    if (Objects.isNull(phaseMap)) {
      throw new NullPointerException(
          "Map<PhaseType, AzimuthUncertaintyTable> constructor parameter cannot be null");
    }

    this.phaseMap = phaseMap;
  }

  private static Map<PhaseType, AzimuthUncertaintyTable> loadAzimuthUncertaintyTables(
      String earthModelFolderName) throws IOException {
    Map<PhaseType, AzimuthUncertaintyTable> phaseMap = new HashMap<>();

    for (PhaseType p : PhaseType.values()) {
      String azimuthUncertaintyTableFileName =
          earthModelFolderName + "/" + p.name().replace("__", "~");

      InputStream is = AzimuthUncertaintyModel1D.class
          .getResourceAsStream(azimuthUncertaintyTableFileName);

      if (Objects.isNull(is)) {
        continue;
      }

      phaseMap.put(p, AzimuthUncertaintyTable.from(is));
    }

    if (phaseMap.isEmpty()) {
      String msg = "Earth model at \"" + earthModelFolderName
          + "\" does not contain any slowness uncertainty table files.";
      logger.warn(msg);
    }

    return Collections.unmodifiableMap(phaseMap);
  }

  /**
   * Retrieves the phases for which uncertainty tables are available for this earth model
   *
   * @return a set of phase types
   */
  public Set<PhaseType> getPhaseTypes() {
    return this.phaseMap.keySet();
  }

  /**
   * Retrieves the distance values for rows in the uncertainty table
   *
   * @param p phase type
   * @return array of distance values in degrees
   */
  public double[] getDistanceDegreesForPhase(PhaseType p) {
    Validate.isTrue(this.phaseMap.containsKey(p));
    return this.phaseMap.get(p).getDistanceDegrees();
  }

  /**
   * Retrieves uncertainty values from table. Should contain just one column of data.
   *
   * @param p phase type
   * @return array of uncertainty data
   */
  public double[] getAzimuthUncertaintyForPhase(PhaseType p) {
    Validate.isTrue(this.phaseMap.containsKey(p));
    return this.phaseMap.get(p).getAzimuthUncertainty();
  }


  /*
   * Inner class to read the uncertainty table for a single phase type
   */
  private static class AzimuthUncertaintyTable {

    private final double[] distanceDegrees;
    private final double[] azimuthUncertainty;

    /**
     * @param distanceDegrees 1-D array of distances corresponding to columns in the
     * azimuthUncertainty table, in degrees, not null
     * @param azimuthUncertainty 1-D array of azimuth uncertainties.  Used to calculate travel time
     * given a distance.
     */
    private AzimuthUncertaintyTable(double[] distanceDegrees, double[] azimuthUncertainty) {
      this.distanceDegrees = distanceDegrees;
      this.azimuthUncertainty = azimuthUncertainty;
    }

    /**
     * Returns a new AzimuthUncertaintyTable loaded from a file at the specified path
     *
     * @return new AzimuthUncertaintyTable
     */
    static AzimuthUncertaintyTable from(InputStream is) throws IOException {
      BufferedReader reader = new BufferedReader(new InputStreamReader(is));

      Triple<double[], double[], double[][]> triple = StandardEarthModelFormatUtility
          .retrieveErrorsUncertainties(reader);
      // Null depth array indicates that uncertainty is a function only of distance.  No other case is handled here.
      Validate.isTrue(Objects.isNull(triple.getMiddle()));

      double[] distanceDegrees =
          Objects.requireNonNull(triple.getLeft(), "Uncertainty distance array cannot be null");
      double[] azimuthUncertainty = triple.getRight()[0];

      return new AzimuthUncertaintyTable(distanceDegrees, azimuthUncertainty);
    }

    public double[] getDistanceDegrees() {
      return distanceDegrees.clone();
    }

    public double[] getAzimuthUncertainty() {
      return azimuthUncertainty.clone();
    }

  }
}
