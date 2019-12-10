package gms.core.signaldetection.association.control;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import java.util.Objects;
import java.util.UUID;

/**
 * Claim check object containing the {@link UUID} of an {@link gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetection}
 * and the {@link UUID} of its parent {@link gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetectionHypothesis}
 */
@AutoValue
public abstract class SignalDetectionHypothesisClaimCheck {

  public abstract UUID getSignalDetectionHypothesisId();

  public abstract UUID getSignalDetectionId();

  /**
   * Given a {@link UUID} of an {@link gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetection}
   * and the {@link UUID} of its parent {@link gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetectionHypothesis},
   * return a new {@link SignalDetectionHypothesisClaimCheck}
   *
   * @param signalDetectionHypothesisId {@link UUID} of the signalDetection's parent {@link
   * gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetectionHypothesis}
   * @param signalDetectionId {@link UUID} of a {@link gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetection}
   * @return New {@link SignalDetectionHypothesisClaimCheck}.  Not Null.
   */
  @JsonCreator
  public static SignalDetectionHypothesisClaimCheck from(
      @JsonProperty("signalDetectionHypothesisId") UUID signalDetectionHypothesisId,
      @JsonProperty("signalDetectionId") UUID signalDetectionId) {
    Objects.requireNonNull(signalDetectionHypothesisId,
        "SignalDetectionHypothesisClaimCheck::from() requires non-null signalDetectionHypothesisId");
    Objects.requireNonNull(signalDetectionId,
        "SignalDetectionHypothesisClaimCheck::from() requires non-null signalDetectionId");

    return new AutoValue_SignalDetectionHypothesisClaimCheck(signalDetectionHypothesisId,
        signalDetectionId);
  }

}
