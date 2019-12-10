package gms.core.featureprediction.plugins.implementations.ellipticitycorrection;

public class TestFixtures {

  public static final double[] PKPbcDepthValues = new double[]{145.0, 150.0, 155.0};

  public static final double[] PKPbcDistanceValues = new double[]{0.0, 100.0, 200.0, 300.0, 500.0,
      700.0};

  public static final double[][] PKPbcTau0Values = new double[][]{
      {-1.9159, -1.8872, -1.8518, -1.8184, -1.7544, -1.6994},
      {-2.1611, -2.1105, -2.0696, -2.0297, -1.9559, -1.8946},
      {-2.3189, -2.2708, -2.2285, -2.1884, -2.1155, -2.0521}
  };

  public static final double[][] PKPbcTau1Values = new double[][]{
      {1.0228, 1.0328, 1.0369, 1.0387, 1.0436, 1.0489},
      {0.9992, 0.9959, 0.9937, 0.9923, 0.9919, 0.9902},
      {0.8872, 0.8852, 0.8837, 0.8824, 0.8818, 0.8825}
  };

  public static final double[][] PKPbcTau2Values = new double[][]{
      {-0.3988, -0.3903, -0.3878, -0.3852, -0.3822, -0.3791},
      {-0.2831, -0.2844, -0.2841, -0.2842, -0.2852, -0.2844},
      {-0.2054, -0.2056, -0.2058, -0.2059, -0.2062, -0.2063}
  };
}
