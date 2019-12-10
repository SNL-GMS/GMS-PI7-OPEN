package gms.core.waveformqc.waveformqccontrol;

import gms.core.waveformqc.waveformqccontrol.control.WaveformQcControl;
import gms.shared.frameworks.control.ControlFactory;

/**
 * Application for running the waveform qc service.
 */

public class Application {

  private Application() {
  }

  public static void main(String[] args) {
    ControlFactory.runService(WaveformQcControl.class);
  }
}
