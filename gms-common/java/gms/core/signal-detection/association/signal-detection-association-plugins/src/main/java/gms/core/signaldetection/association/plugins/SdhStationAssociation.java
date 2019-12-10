package gms.core.signaldetection.association.plugins;

import com.google.auto.value.AutoValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetectionHypothesis;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceStation;
import java.util.Objects;

@AutoValue
public abstract class SdhStationAssociation {

  public abstract SignalDetectionHypothesis getSignalDetectionHypothesis();

  public abstract ReferenceStation getReferenceStation();

  /**
   * Creates a new instance of {@link SdhStationAssociation}, a wrapper class around a {@link
   * SignalDetectionHypothesis} and the {@link ReferenceStation} at which the {@link
   * SignalDetectionHypothesis} was detected.
   *
   * @param signalDetectionHypothesis signal detection hypothesis
   * @param referenceStation station at which hypothesised signal was detected
   * @return an SdhStationAssociation
   */
  public static SdhStationAssociation from(
      SignalDetectionHypothesis signalDetectionHypothesis,
      ReferenceStation referenceStation) {

    Objects.requireNonNull(signalDetectionHypothesis,
        "Cannot create an SdhStationAssociation from a null signal detection hypothesis");
    Objects.requireNonNull(referenceStation,
        "Cannot create an SdhStationAssociation from a null reference station");

    return new AutoValue_SdhStationAssociation(
        signalDetectionHypothesis,
        referenceStation);
  }

}
