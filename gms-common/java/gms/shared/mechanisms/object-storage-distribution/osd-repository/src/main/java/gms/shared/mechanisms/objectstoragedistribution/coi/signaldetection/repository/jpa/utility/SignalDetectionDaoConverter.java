package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.utility;

import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetection;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetectionHypothesis;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.dataaccessobjects.SignalDetectionDao;
import java.util.List;
import java.util.Objects;

public class SignalDetectionDaoConverter {

  private SignalDetectionDaoConverter() {}

  public static SignalDetectionDao toDao(SignalDetection signalDetection) {
    Objects.requireNonNull(signalDetection, "Cannot create SignalDetectionDao from a null SignalDetection");

    SignalDetectionDao dao = new SignalDetectionDao(
        signalDetection.getId(), signalDetection.getMonitoringOrganization(),
        signalDetection.getStationId(), signalDetection.getCreationInfoId());

    return dao;
  }

  public static SignalDetection fromDao(SignalDetectionDao signalDetectionDao, List<SignalDetectionHypothesis> signalDetectionHypotheses) {
    Objects.requireNonNull(signalDetectionDao, "Cannot create SignalDetection from a null SignalDetectionDao");
    Objects.requireNonNull(signalDetectionHypotheses, "Cannot create SignalDetection from a null signalDetectionHypotheses");

    return SignalDetection.from(signalDetectionDao.getSignalDetectionId(),
        signalDetectionDao.getMonitoringOrganization(), signalDetectionDao.getStationId(),
        signalDetectionHypotheses, signalDetectionDao.getCreationInfoId());
  }
}
