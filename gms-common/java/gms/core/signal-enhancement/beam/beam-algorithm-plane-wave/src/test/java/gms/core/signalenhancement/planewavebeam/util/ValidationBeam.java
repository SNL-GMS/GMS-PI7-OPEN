package gms.core.signalenhancement.planewavebeam.util;

public enum ValidationBeam {

  COHERENT_2D {
    @Override
    public double[] getBeam() {
      double[] beam = new double[200];
      for (int i = 0; i < 200; i++) {
        if (i == 99) {
          beam[i] = 2.0 / 21.0;
        } else if (i == 100) {
          beam[i] = 13.0 / 21.0;
        } else if (i == 101) {
          beam[i] = -11.0 / 21.0;
        } else if (i == 102) {
          beam[i] = -4.0 / 21.0;
        } else {
          beam[i] = 0.0;
        }
      }

      return beam;
    }
  },
  COHERENT_3D {
    @Override
    public double[] getBeam() {
      double[] beam = new double[200];
      for (int i = 0; i < 200; i++) {
        if (i == 100) {
          beam[i] = 1.0;
        } else if (i == 101) {
          beam[i] = -1.0;
        } else {
          beam[i] = 0.0;
        }
      }

      return beam;
    }
  },
  INCOHERENT_2D {
    @Override
    public double[] getBeam() {
      double[] beam = new double[200];
      for (int i = 0; i < 200; i++) {
        if (i == 99) {
          beam[i] = 2.0 / 21.0;
        } else if (i == 100) {
          beam[i] = 17.0 / 21.0;
        } else if (i == 101) {
          beam[i] = 19.0 / 21.0;
        } else if (i == 102) {
          beam[i] = 4.0 / 21.0;
        } else {
          beam[i] = 0.0;
        }
      }

      return beam;
    }
  },
  INCOHERENT_3D {
    @Override
    public double[] getBeam() {
      double[] beam = new double[200];
      for (int i = 0; i < 200; i++) {
        if (i == 100) {
          beam[i] = 1.0;
        } else if (i == 101) {
          beam[i] = 1.0;
        } else {
          beam[i] = 0.0;
        }
      }

      return beam;
    }
  };

  public abstract double[] getBeam();

}
