package gms.shared.mechanisms.objectstoragedistribution.coi.emerging.processingcontrol;

import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.processingcontrol.commonobjects.AnalystActionReference;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.processingcontrol.commonobjects.ProcessingContext;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.processingcontrol.commonobjects.ProcessingStepReference;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.processingcontrol.commonobjects.StorageVisibility;
import java.util.Optional;
import java.util.UUID;

/**
 * Defines static testing objects
 */
public class TestFixtures {

  private static final UUID procStageIntervalId = UUID.randomUUID();

  // AnalystActionReference
  private static final UUID procActivityIntervalId = UUID.randomUUID();
  private static final UUID analystId = UUID.randomUUID();

  public static final AnalystActionReference analystActionRef = AnalystActionReference
      .from(procStageIntervalId, procActivityIntervalId, analystId);

  // ProcessingStepReference
  private final static UUID procSeqIntervalId = UUID.randomUUID();
  private final static UUID procStepId = UUID.randomUUID();
  public static final ProcessingStepReference processingStepRef = ProcessingStepReference
      .from(procStageIntervalId, procSeqIntervalId, procStepId);

  // ProcessingContext
  public static final ProcessingContext processingContext = ProcessingContext
      .from(Optional.of(analystActionRef), Optional.of(processingStepRef),
          StorageVisibility.PRIVATE);
}
