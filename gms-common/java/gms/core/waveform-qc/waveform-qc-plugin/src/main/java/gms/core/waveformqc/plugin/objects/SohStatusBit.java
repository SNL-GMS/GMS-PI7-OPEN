package gms.core.waveformqc.plugin.objects;

/**
 * Acquired status bits can be set, unset, or missing
 */
public enum SohStatusBit {
  SET, UNSET, MISSING;

  public static SohStatusBit from(boolean status) {
    return status ? SET : UNSET;
  }
}
