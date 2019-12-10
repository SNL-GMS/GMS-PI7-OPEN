package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.utility;

import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskVersionDescriptor;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.dataaccessobjects.QcMaskVersionDescriptorDao;

public class QcMaskVersionDescriptorDaoConverter {

  private QcMaskVersionDescriptorDaoConverter() {
  }

  public static QcMaskVersionDescriptorDao toDao(QcMaskVersionDescriptor qcMaskVersionDescriptor) {
    return new QcMaskVersionDescriptorDao(
        qcMaskVersionDescriptor.getQcMaskId(),
        qcMaskVersionDescriptor.getQcMaskVersionId());
  }

  public static QcMaskVersionDescriptor fromDao(
      QcMaskVersionDescriptorDao qcMaskVersionDescriptorDao) {
    return QcMaskVersionDescriptor
        .from(qcMaskVersionDescriptorDao.getQcMaskId(),
            qcMaskVersionDescriptorDao.getQcMaskVersionId());

  }

}
