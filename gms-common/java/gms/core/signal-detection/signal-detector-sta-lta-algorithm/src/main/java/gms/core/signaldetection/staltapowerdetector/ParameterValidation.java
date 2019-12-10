package gms.core.signaldetection.staltapowerdetector;

import java.util.function.Predicate;

/**
 * Utility operations to validate STA/LTA algorithm parameters
 */
public class ParameterValidation {

  /**
   * Checks the STA and LTA window lengths are both positive.
   *
   * @param staLength STA window length
   * @param ltaLength LTA window length
   * @param isPositive {@link Predicate} evaluating to true when {@code T is > 0 }
   * @param <T> staLength and ltaLength type
   * @throws IllegalArgumentException when: {@code if staLength <= 0; if ltaLength <= 0; }
   */
  public static <T> void validateWindowLengths(T staLength,
      T ltaLength, Predicate<T> isPositive) {

    if (!isPositive.test(staLength)) {
      throw new IllegalArgumentException("STA window must have positive length");
    }

    if (!isPositive.test(ltaLength)) {
      throw new IllegalArgumentException("LTA window must have positive length");
    }
  }

  /**
   * {@code Checks the trigger and detrigger thresholds are both > 0 }
   * @param triggerThreshold STA/LTA trigger threshold
   * @param detriggerThreshold STA/LTA detrigger threshold
   * @throws IllegalArgumentException if {@code triggerThreshold or detriggerThreshold are <= 0 }
   */
  public static void validateTriggerThresholds(double triggerThreshold, double detriggerThreshold) {
    if(triggerThreshold <= 0) {
      throw new IllegalArgumentException("STA/LTA trigger threshold must be positive");
    }

    if(detriggerThreshold <= 0) {
      throw new IllegalArgumentException("STA/LTA detrigger threshold must be positive");
    }
  }
}
