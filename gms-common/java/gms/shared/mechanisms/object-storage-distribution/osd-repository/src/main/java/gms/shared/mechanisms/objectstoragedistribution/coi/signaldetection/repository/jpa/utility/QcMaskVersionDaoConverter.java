package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.utility;

import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskCategory;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskVersion;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskVersion.Builder;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskVersionDescriptor;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.dataaccessobjects.QcMaskDao;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.dataaccessobjects.QcMaskVersionDao;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Utility class for converting {@link QcMaskVersion} to {@link QcMaskVersionDao}.
 */
public class QcMaskVersionDaoConverter {

  public static QcMaskVersionDao toDao(QcMaskDao qcMaskDao, QcMaskVersion qcMaskVersion) {
    Objects.requireNonNull(qcMaskDao, "Cannot create QcMaskVersionDao from a null QcMask");
    Objects.requireNonNull(qcMaskVersion,
        "Cannot create QcMaskVersionDao from a null QcMaskVersion");

    QcMaskVersionDao dao = new QcMaskVersionDao();
    dao.setOwnerQcMask(qcMaskDao);
    dao.setVersion(qcMaskVersion.getVersion());

    dao.setParentQcMasks(qcMaskVersion.getParentQcMasks().stream()
        .map(QcMaskVersionDescriptorDaoConverter::toDao)
        .collect(Collectors.toList()));

    dao.setChannelSegmentIds(new ArrayList<>(qcMaskVersion.getChannelSegmentIds()));

    dao.setCategory(qcMaskVersion.getCategory());
    dao.setRationale(qcMaskVersion.getRationale());
    if (!dao.getCategory().equals(QcMaskCategory.REJECTED)) {

      qcMaskVersion.getType().ifPresent(dao::setType);
      qcMaskVersion.getStartTime().ifPresent(dao::setStartTime);
      qcMaskVersion.getEndTime().ifPresent(dao::setEndTime);
    }

    return dao;
  }

  public static QcMaskVersion fromDao(QcMaskVersionDao qcMaskVersionDao) {
    Objects.requireNonNull(qcMaskVersionDao,
        "Cannot create QcMaskVersion from a null QcMaskVersionDao");

    List<QcMaskVersionDescriptor> parentQcMasks = qcMaskVersionDao.getParentQcMasks().stream()
        .map(QcMaskVersionDescriptorDaoConverter::fromDao)
        .collect(Collectors.toList());

    List<UUID> channelSegmentIds = qcMaskVersionDao.getChannelSegmentIds();

    Builder versionBuilder = QcMaskVersion.builder();

    versionBuilder
        .setVersion(qcMaskVersionDao.getVersion())
        .setParentQcMasks(parentQcMasks)
        .setChannelSegmentIds(channelSegmentIds)
        .setCategory(qcMaskVersionDao.getCategory())
        .setRationale(qcMaskVersionDao.getRationale());

    //setting nullable parameters
    if (qcMaskVersionDao.getType() != null) {
      versionBuilder.setType(qcMaskVersionDao.getType());
    }

    if (qcMaskVersionDao.getStartTime() != null) {
      versionBuilder.setStartTime(qcMaskVersionDao.getStartTime());
    }

    if (qcMaskVersionDao.getEndTime() != null) {
      versionBuilder.setEndTime(qcMaskVersionDao.getEndTime());
    }

    return versionBuilder.build();
  }
}
