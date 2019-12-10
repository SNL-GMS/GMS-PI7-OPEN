package gms.processors.invokeqc;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * DTO containing the request body for the Waveform Qc Control OSD Gateway loadInputData operation.
 */
public class InvokeControlDto {

  private final Set<UUID> processingChannelIds;
  private final Instant startTime;
  private final Instant endTime;
  private final ProcessingContext processingContext;
  private final Map<String, String> parameterOverrides;

  public InvokeControlDto(Set<UUID> processingChannelIds, Instant startTime,
      Instant endTime, ProcessingContext processingContext,
      Map<String, String> parameterOverrides) {
    this.processingChannelIds = processingChannelIds;
    this.startTime = startTime;
    this.endTime = endTime;
    this.processingContext = processingContext;
    this.parameterOverrides = parameterOverrides;
  }

  public Set<UUID> getProcessingChannelIds() {
    return processingChannelIds;
  }

  public Instant getStartTime() {
    return startTime;
  }

  public Instant getEndTime() {
    return endTime;
  }

  public ProcessingContext getProcessingContext() {
    return processingContext;
  }

  public Map<String, String> getParameterOverrides() {
    return parameterOverrides;
  }

  @Override
  public String toString() {
    return "InvokeControlDto{" +
        "processingChannelIds=" + processingChannelIds +
        ", startTime=" + startTime +
        ", endTime=" + endTime +
        ", processingContext=" + processingContext +
        ", parameterOverrides=" + parameterOverrides +
        '}';
  }
}
