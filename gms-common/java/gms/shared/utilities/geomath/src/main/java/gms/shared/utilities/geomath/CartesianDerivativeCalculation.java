package gms.shared.utilities.geomath;

public enum CartesianDerivativeCalculation {

  /**
   * Changes distance, depth, azimuth coordinates to cartesian and returns dAz/dx, dAz/dy, dAz/dz,
   * dAz/dt where Az is azimuth
   *
   * @return dAz/dx, dAz/dy, dAz/dz, dAz/dt
   */
  OF_AZIMUTH() {
    @Override
    public double[] calculate(double[] derivatives, double distance, double depth, double azimuth) {
      return new double[] {
          -Math.cos(Math.toRadians(azimuth)) / Math.sin(Math.toRadians(distance)),
          Math.sin(Math.toRadians(azimuth)) / Math.sin(Math.toRadians(distance)),
          0.0, 0.0};
    }
  },

  /**
   * Changes distance, depth, azimuth coordinates to cartesian and returns dT/dx, dT/dy, dT/dz,
   * dT/dt, where T is travel time
   *
   * @return dT/dx, dT/dy, dT/dz, dT/dt
   */
  OF_TRAVEL_TIME() {
    @Override
    public double[] calculate(double[] derivatives, double distance, double depth, double azimuth) {
      return new double[] {
          -derivatives[1] * Math.sin(Math.toRadians(azimuth)),
          -derivatives[1] * Math.cos(Math.toRadians(azimuth)),
          derivatives[3],
          1.0
      };
    }
  },

  /**
   * Changes distance, depth, azimuth coordinates to cartesian and returns dSh/dx, dSh/dy, dSh/dz,
   * dSh/dt where Sh is horizontal slowness
   *
   * @return dSh/dx, dSh/dy, dSh/dz, dSh/dt
   */
  OF_SLOWNESS() {
    @Override
    public double[] calculate(double[] derivatives, double distance, double depth, double azimuth) {
      return new double[] {
          -derivatives[2] * Math.sin(Math.toRadians(azimuth)),
          -derivatives[2] * Math.cos(Math.toRadians(azimuth)),
          derivatives[4],
          0.0
      };
    }
  };

  /**
   * Returns a function that computes the cartesian derivatives of depth/distance based travel time
   * functions.
   *
   * @return new derivatives
   */
  public abstract double[] calculate(double[] depthDistanceDerivatives, double distance,
      double depth, double azimuth);

}
