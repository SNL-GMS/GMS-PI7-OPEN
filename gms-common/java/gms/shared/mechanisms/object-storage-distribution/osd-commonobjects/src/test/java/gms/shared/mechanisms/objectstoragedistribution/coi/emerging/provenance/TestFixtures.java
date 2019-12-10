package gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance;

import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.processingcontrol.commonobjects.AnalystActionReference;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.processingcontrol.commonobjects.ProcessingStepReference;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.CreationInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.CreationInformation;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.InformationSource;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.SoftwareComponentInfo;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Defines testing objects
 */
public class TestFixtures {

  // SoftwareComponentInfo
  public static final SoftwareComponentInfo softwareComponentInfo = new SoftwareComponentInfo(
      "unit test component name", "unit test component version");

  // CreationInfo
  public static final CreationInfo creationInfo = new CreationInfo("unit test creator name",
      softwareComponentInfo);

  // CreationInformation
  private static final UUID procStageIntervalId = UUID.randomUUID();
  private static final ProcessingStepReference procStepRef = ProcessingStepReference
      .from(procStageIntervalId, UUID.randomUUID(), UUID.randomUUID());
  private static final AnalystActionReference analystActionRef = AnalystActionReference
      .from(procStageIntervalId, UUID.randomUUID(), UUID.randomUUID());

  public static final CreationInformation creationInformation = CreationInformation.create(Optional.of(
      analystActionRef), Optional.of(procStepRef), softwareComponentInfo);

  // InformationSource
  public static final InformationSource informationSource = InformationSource.create(
      "Source", Instant.now(), "Unit test.");
}
