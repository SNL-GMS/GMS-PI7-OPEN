package gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.repository.jpa.utility;

import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.processingcontrol.commonobjects.AnalystActionReference;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.processingcontrol.commonobjects.ProcessingStepReference;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.CreationInformation;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.SoftwareComponentInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.repository.jpa.dataaccessobjects.CreationInformationDao;
import java.util.Objects;
import java.util.Optional;

/**
 * Utility class for converting {@link CreationInformation} to {@link CreationInformationDao}.
 */
public class CreationInformationDaoConverter {

  public static CreationInformationDao toDao(CreationInformation creationInformation) {
    Objects.requireNonNull(creationInformation,
        "Cannot create CreationInformationDao from a null CreationInformation");

    CreationInformationDao dao = new CreationInformationDao();
    dao.setId(creationInformation.getId());
    dao.setCreationTime(creationInformation.getCreationTime());
    if (creationInformation.getAnalystActionReference().isPresent()) {
      dao.setProcessingStageIntervalId(
          creationInformation.getAnalystActionReference().get().getProcessingStageIntervalId());
      dao.setProcessingActivityIntervalId(
          creationInformation.getAnalystActionReference().get().getProcessingActivityIntervalId());
      dao.setAnalystId(creationInformation.getAnalystActionReference().get().getAnalystId());
    }
    if (creationInformation.getProcessingStepReference().isPresent()) {
      dao.setProcessingStageIntervalId(
          creationInformation.getProcessingStepReference().get().getProcessingStageIntervalId());
      dao.setProcessingSequenceIntervalId(
          creationInformation.getProcessingStepReference().get().getProcessingSequenceIntervalId());
      dao.setProcessingStepId(
          creationInformation.getProcessingStepReference().get().getProcessingStepId());
    }
    dao.setSoftwareComponentName(creationInformation.getSoftwareInfo().getName());
    dao.setSoftwareComponentVersion(creationInformation.getSoftwareInfo().getVersion());
    return dao;
  }

  public static CreationInformation fromDao(CreationInformationDao creationInformationDao) {
    Objects.requireNonNull(creationInformationDao,
        "Cannot create CreationInformation from a null CreationInformationDao");

    Optional<AnalystActionReference> analystActionReferenceOptional =
        (creationInformationDao.getAnalystId() != null) ?
            Optional.of(AnalystActionReference.from(
                creationInformationDao.getProcessingStageIntervalId(),
                creationInformationDao.getProcessingActivityIntervalId(),
                creationInformationDao.getAnalystId()))
            : Optional.empty();

    Optional<ProcessingStepReference> processingStepReferenceOptional =
        (creationInformationDao.getProcessingStepId() != null) ?
            Optional.of(ProcessingStepReference.from(
                creationInformationDao.getProcessingStageIntervalId(),
                creationInformationDao.getProcessingSequenceIntervalId(),
                creationInformationDao.getProcessingStepId())) :
            Optional.empty();

    return CreationInformation.from(
        creationInformationDao.getId(),
        creationInformationDao.getCreationTime(),
        analystActionReferenceOptional, processingStepReferenceOptional,
        new SoftwareComponentInfo(creationInformationDao.getSoftwareComponentName(),
            creationInformationDao.getSoftwareComponentVersion()));
  }
}
