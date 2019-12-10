package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.utility;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.ParameterValidation;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMask;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskVersion;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.dataaccessobjects.QcMaskDao;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.dataaccessobjects.QcMaskVersionDao;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Utility class for converting {@link QcMask} to {@link QcMaskDao}.
 */
public class QcMaskDaoConverter {

  public static QcMaskDao toDao(QcMask qcMask) {
    Objects.requireNonNull(qcMask, "Cannot create QcMaskDao from a null QcMask");

    QcMaskDao dao = new QcMaskDao();
    dao.setId(qcMask.getId());
    dao.setChannelId(qcMask.getChannelId());
    return dao;
  }

  public static QcMask fromDao(QcMaskDao qcMaskDao, QcMaskVersionDao qcMaskVersionDao) {
    Objects.requireNonNull(qcMaskDao, "Cannot create QcMask from a null QcMaskDao");
    Objects.requireNonNull(qcMaskVersionDao, "Cannot create QcMask from a null QcMaskVersionDao");

    return QcMask.from(qcMaskDao.getId(),
        qcMaskDao.getChannelId(),
        Collections.singletonList(QcMaskVersionDaoConverter.fromDao(qcMaskVersionDao)));
  }

  public static QcMask fromDao(QcMaskDao qcMaskDao, List<QcMaskVersionDao> qcMaskVersionDaos) {
    Objects.requireNonNull(qcMaskDao, "Cannot create QcMask from a null QcMaskDao");
    Objects.requireNonNull(qcMaskVersionDaos, "Cannot create QcMask from null QcMaskVersionDaos");
    ParameterValidation.requireFalse(List::isEmpty, qcMaskVersionDaos,
        "Cannot create QcMask from an empty QcMaskVersionDaos");

    List<QcMaskVersion> qcMaskVersions = qcMaskVersionDaos.stream()
        .map(QcMaskVersionDaoConverter::fromDao).collect(Collectors.toList());

    return QcMask.from(qcMaskDao.getId(), qcMaskDao.getChannelId(), qcMaskVersions);
  }
}
