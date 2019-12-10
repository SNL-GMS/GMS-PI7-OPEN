package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.utility;

import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.EnumeratedMeasurementValue.PhaseTypeMeasurementValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FeatureMeasurement;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FeatureMeasurementTypes;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.InstantValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetectionHypothesis;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.dataaccessobjects.FeatureMeasurementDao;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.dataaccessobjects.InstantFeatureMeasurementDao;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.dataaccessobjects.PhaseFeatureMeasurementDao;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.dataaccessobjects.SignalDetectionDao;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.dataaccessobjects.SignalDetectionHypothesisDao;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class SignalDetectionHypothesisDaoConverter {

  private SignalDetectionHypothesisDaoConverter() {}

  public static SignalDetectionHypothesisDao toDao(SignalDetectionDao signalDetectionDao, SignalDetectionHypothesis signalDetectionHypothesis) {
    Objects.requireNonNull(signalDetectionDao, "Cannot create SignalDetectionHypothesisDao from a null SignalDetectionDao");
    Objects.requireNonNull(signalDetectionHypothesis, "Cannot create SignalDetectionHypothesisDao from a null SignalDetectionHypothesis");

    // extract the arrival time and phase measurements to provide separately.
    final FeatureMeasurement<InstantValue> arrivalTimeMeasurement
        = signalDetectionHypothesis.getFeatureMeasurement(FeatureMeasurementTypes.ARRIVAL_TIME).get();
    final FeatureMeasurement<PhaseTypeMeasurementValue> phaseMeasurement
        = signalDetectionHypothesis.getFeatureMeasurement(FeatureMeasurementTypes.PHASE).get();
    final InstantFeatureMeasurementDao arrivalTimeMeasurementDao = new InstantFeatureMeasurementDao(arrivalTimeMeasurement);
    final PhaseFeatureMeasurementDao phaseMeasurementDao = new PhaseFeatureMeasurementDao(phaseMeasurement);
    // remove the arrival time and phase measurements from the overall list (so they aren't duplicated in there)
    final List<FeatureMeasurement<?>> otherMeasurements = new ArrayList<>(signalDetectionHypothesis.getFeatureMeasurements());
    otherMeasurements.remove(arrivalTimeMeasurement);
    otherMeasurements.remove(phaseMeasurement);
    final List<FeatureMeasurementDao<?>> otherMeasurementDaos = otherMeasurements.stream()
        .map(FeatureMeasurementDao::fromCoi)
        .collect(Collectors.toList());
    return new SignalDetectionHypothesisDao(signalDetectionHypothesis.getId(), signalDetectionDao,
        signalDetectionHypothesis.isRejected(), arrivalTimeMeasurementDao, phaseMeasurementDao,
        otherMeasurementDaos, signalDetectionHypothesis.getCreationInfoId());
  }

  public static SignalDetectionHypothesis fromDao(SignalDetectionHypothesisDao signalDetectionHypothesisDao) {
    Objects.requireNonNull(signalDetectionHypothesisDao, "Cannot create SignalDetectionHypothesis from a null SignalDetectionHypothesisDao");
    // combine phase and arrival measurements into one collection
    final List<FeatureMeasurementDao<?>> featureMeasurementDaos = signalDetectionHypothesisDao.getFeatureMeasurements();
    featureMeasurementDaos.add(signalDetectionHypothesisDao.getPhaseMeasurement());
    featureMeasurementDaos.add(signalDetectionHypothesisDao.getArrivalTimeMeasurement());
    final List<FeatureMeasurement<?>> allMeasurements = featureMeasurementDaos.stream()
        .map(FeatureMeasurementDao::toCoi)
        .collect(Collectors.toList());
    return SignalDetectionHypothesis.from(signalDetectionHypothesisDao.getSignalDetectionHypothesisId(),
        signalDetectionHypothesisDao.getParentSignalDetection().getSignalDetectionId(),
        signalDetectionHypothesisDao.isRejected(),
        allMeasurements, signalDetectionHypothesisDao.getCreationInfoId());
  }
}
