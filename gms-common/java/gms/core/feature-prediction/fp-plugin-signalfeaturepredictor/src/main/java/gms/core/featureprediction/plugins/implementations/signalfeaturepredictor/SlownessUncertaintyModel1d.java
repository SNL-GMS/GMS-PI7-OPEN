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


/**
 * Slowness uncertainty (i.e., standard deviation).  Uncertainty values are interpolated (bicubic
 * spline interpolation) from table of uncertainty values for a specific phase of a specific 1D
 * earth model.  This object reads and stores uncertainty tables for all the phases associated with
 * a specific 1D earth model.
 */
public class SlownessUncertaintyModel1d {

  private Map<PhaseType, SlownessUncertaintyTable> phaseMap;

  static SlownessUncertaintyModel1d from(String slownessUncertaintyTableFolderName)
      throws IOException {
    return new SlownessUncertaintyModel1d(
        loadSlownessUncertaintyTables(slownessUncertaintyTableFolderName));
  }

  private SlownessUncertaintyModel1d(Map<PhaseType, SlownessUncertaintyTable> phaseMap) {
    if (Objects.isNull(phaseMap)) {
      throw new NullPointerException(
          "Map<PhaseType, SlownessUncertaintyTable> constructor parameter cannot be null");
    }

    this.phaseMap = phaseMap;
  }

  private static Map<PhaseType, SlownessUncertaintyTable> loadSlownessUncertaintyTables(
      String earthModelFolderName) throws IOException {
    Map<PhaseType, SlownessUncertaintyTable> phaseMap = new HashMap<>();

    for (PhaseType p : PhaseType.values()) {
      String slownessUncertaintyTableFileName =
          earthModelFolderName + "/" + p.name().replace("__", "~");

      InputStream is = SlownessUncertaintyModel1d.class
          .getResourceAsStream(slownessUncertaintyTableFileName);

      if (Objects.isNull(is)) {
        continue;
      }

      phaseMap.put(p, SlownessUncertaintyTable.from(is));
    }

    if (phaseMap.isEmpty()) {
      throw new IllegalArgumentException(
          "Earth model at \"" + earthModelFolderName
              + "\" does not contain any slowness uncertainty table files.");
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
    Validate.isTrue(this.phaseMap.containsKey(p), String
        .format("PhaseType \"%s\" does not have data in slowness uncertainty table",
            p.toString()));
    return this.phaseMap.get(p).getDistanceDegrees();
  }

  /**
   * Retrieves uncertainty values from table. Should contain just one column of data.
   *
   * @param p phase type
   * @return array of uncertainty data
   */
  public double[] getSlownessUncertaintyForPhase(PhaseType p) {
    Validate.isTrue(this.phaseMap.containsKey(p), String
        .format("PhaseType \"%s\" does not have data in slowness uncertainty table",
            p.toString()));
    return this.phaseMap.get(p).getSlownessUncertainty();
  }


  /*
   * Inner class to read the uncertainty table for a single phase type
   */
  private static class SlownessUncertaintyTable {

    private final double[] distanceDegrees;
    private final double[] slownessUncertainty;

    /**
     * @param distanceDegrees 1-D array of distances corresponding to columns in the
     * slownessUncertainty table, in degrees, not null
     * @param slownessUncertainty 1-D array of slowness uncertainties.  Used to calculate travel
     * time given a distance.
     */
    private SlownessUncertaintyTable(double[] distanceDegrees, double[] slownessUncertainty) {
      this.distanceDegrees = distanceDegrees;
      this.slownessUncertainty = slownessUncertainty;
    }

    /**
     * Returns a new SlownessUncertaintyTable loaded from a file at the specified path
     *
     * @return new SlownessUncertaintyTable
     */
    static SlownessUncertaintyTable from(InputStream is) throws IOException {
      BufferedReader reader = new BufferedReader(new InputStreamReader(is));

      Triple<double[], double[], double[][]> triple = StandardEarthModelFormatUtility
          .retrieveErrorsUncertainties(reader);
      // Null depth array indicates that uncertainty is a function only of distance.  No other case is handled here.
      Validate.isTrue(Objects.isNull(triple.getMiddle()), "Depth array in slowness uncertainty table is not null");

      double[] distanceDegrees =
          Objects.requireNonNull(triple.getLeft(), "Uncertainty distance array cannot be null");
      double[] slownessUncertainty = triple.getRight()[0];

      return new SlownessUncertaintyTable(distanceDegrees, slownessUncertainty);
    }

    public double[] getDistanceDegrees() {
      return distanceDegrees.clone();
    }

    public double[] getSlownessUncertainty() {
      return slownessUncertainty.clone();
    }

  }
}
