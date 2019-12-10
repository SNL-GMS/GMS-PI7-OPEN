package gms.core.featureprediction.plugins.implementations.signalfeaturepredictor;

import gms.core.featureprediction.plugins.DepthDistance1dModelSet;
import gms.core.featureprediction.plugins.TravelTime1dPlugin;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.PhaseType;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;

public class TestFixtures {

  //TODO: this functionality seems be getting duplicated
  public static double[][] transposeMatrix(double [][] m){
    double[][] temp = new double[m[0].length][m.length];
    for (int i = 0; i < m.length; i++)
      for (int j = 0; j < m[0].length; j++)
        temp[j][i] = m[i][j];
    return temp;
  }

  //TODO: transposing made it easier to copy values from data file directly;
  //TODO:    maybe change this later.
  public static final double[][] tableNoHoles = transposeMatrix(new double[][]
      //
      // <- depth ->
      //    ^
      //    |
      // distance
      //    |
      //    V
         {{ 442.9671, 441.4871, 439.0246, 436.5673, 431.6920, 426.9551, 422.3787 },
          { 447.1415, 445.658,  443.1895, 440.7262, 435.8388, 431.0894, 426.4999 },
          { 451.2988, 449.8117, 447.3374, 444.8681, 439.9688, 435.2069, 430.6044 },
          { 455.4391, 453.9485, 451.4683, 448.9932, 444.0819, 439.3077, 434.6922 },
          { 459.5623, 458.0683, 455.5823, 453.1014, 448.1783, 443.3916, 438.7631 },
          { 463.6686, 462.1711, 459.6794, 457.1926, 452.2575, 447.4584, 442.8169 },
          { 467.7578, 466.2568, 463.7593, 461.2666, 456.3196, 451.5080, 446.8534 }});

  public static double[] depthExtrapolatedTravelTimes = {
      417.9867,
      422.0942,
      426.1851,
      430.2591,
      434.3162,
      438.3562,
      442.3790
  };

  public static double[] distanceExtrapolatedTravelTimes =
      //TODO: check these truth values based on what original code is designed to do
      //TODO:    (does it prioritize accurate derivatives? then this may be right;
      //TODO:    if it prioritizes accurate values then this is wrong)
      { 455.4391, 453.9485, 451.4683, 448.9932, 444.0819, 439.3077, 434.6922 };
      //TODO: these may be the correct truth values:
      //     { 471.8297, 470.3253, 467.8219, 465.3234, 460.3644, 455.5403, 450.8726 };

  public static double extrapolatedDepth = 300.0;
  public static double extrapolatedDistance = 2.5;

  public static final double[][] tableWithHoles;
  public static int[][] holeIndixes = {
      {4,4}
      /*{1,1},
      {2,1},
      {2,2},
      {2,2}*/
  };

  public static final double[] depths = {35.0, 50.0, 75.0, 100.0, 150.00, 200.0, 250.0};
  public static final double[] distances = {39.0, 39.5, 40.0, 40.5, 41.0, 41.5, 42.0};

  static {

    //Enforcing that the two tables are exactly the same except for the holes
    //so that results can be compared.
    tableWithHoles = new double[tableNoHoles.length][];

    for (int i = 0; i < tableNoHoles.length; i++) {
      tableWithHoles[i] = Arrays.copyOf(tableNoHoles[i], tableNoHoles[i].length);
    }

    for (int i = 0; i < holeIndixes.length; i++) {
      tableWithHoles[holeIndixes[i][0]][holeIndixes[i][1]] = Double.NaN;
    }
  }
  /*
   * Model with no invalid values
   */
  public static DepthDistance1dModelSet<double[], double[][]> modelNoHoles =
      new DepthDistance1dModelSet<double[], double[][]>() {

    @Override
    public void initialize(Set<String> earthModelNames) throws IOException {

    }

    @Override
    public Set<String> getEarthModelNames() {
      return Set.of("ak135");
    }

    @Override
    public Set<PhaseType> getPhaseTypes(String modelName) {
      return Set.of(PhaseType.P);
    }

    @Override
    public double[] getDepthsKm(String modelName, PhaseType p) {
      return depths;
    }

    @Override
    public double[] getDistancesDeg(String modelName, PhaseType p) {
      return distances;
    }

    @Override
    public double[][] getValues(String modelName, PhaseType p) {
      return tableNoHoles;
    }

    @Override
    public Optional<double[]> getDistanceModelingErrors(String modelName,
        PhaseType p) {
      return Optional.empty();
    }

    @Override
    public Optional<double[]> getDepthModelingErrors(String modelName, PhaseType p) {
      return Optional.empty();
    }

    @Override
    public Optional<double[][]> getValueModelingErrors(String modelName, PhaseType p) {
      return Optional.empty();
    }

  };

  /*
   * Model with invalid value "island"
   */
  public static DepthDistance1dModelSet<double[], double[][]> modelInternalHoles =
      new DepthDistance1dModelSet<double[], double[][]>() {
    @Override
    public void initialize(Set<String> earthModelNames) throws IOException {

    }

    @Override
    public Set<String> getEarthModelNames() {
      return Set.of("ak135");
    }

    @Override
    public Set<PhaseType> getPhaseTypes(String modelName) {
      return Set.of(PhaseType.P);
    }

    @Override
    public double[] getDepthsKm(String modelName, PhaseType p) {
      return depths;
    }

    @Override
    public double[] getDistancesDeg(String modelName, PhaseType p) {
      return distances;
    }
    
    @Override
    public double[][] getValues(String modelName, PhaseType p) {
      return tableWithHoles;
    }

    @Override
    public Optional<double[]> getDistanceModelingErrors(String modelName,
        PhaseType p) {
      return Optional.empty();
    }

    @Override
    public Optional<double[]> getDepthModelingErrors(String modelName, PhaseType p) {
      return Optional.empty();
    }

    @Override
    public Optional<double[][]> getValueModelingErrors(String modelName, PhaseType p) {
      return Optional.empty();
    }


  };
}
