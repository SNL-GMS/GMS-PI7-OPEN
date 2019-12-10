package gms.core.signalenhancement.fk.control;

import com.google.auto.value.AutoValue;
import com.google.common.base.Preconditions;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.StationProcessingInterval;
import java.util.List;
import java.util.UUID;

/**
 * Wrapper class containing all needed data in order to execute {@link FkControl}
 * via claim check.
 */
@AutoValue
public abstract class FkAnalysisCommand {

  public abstract UUID getStationId();
  public abstract List<UUID> getSignalDetectionHypothesisIds();

  public static FkAnalysisCommand from(StationProcessingInterval interval) {
    Preconditions.checkNotNull(interval);
    return new AutoValue_FkAnalysisCommand(interval.getStationId(), interval.getProcessingIds());
  }

}
