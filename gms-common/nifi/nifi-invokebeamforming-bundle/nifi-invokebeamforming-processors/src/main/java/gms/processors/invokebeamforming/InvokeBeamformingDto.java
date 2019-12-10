package gms.processors.invokebeamforming;

import java.time.Instant;
import java.util.UUID;

public class InvokeBeamformingDto {

  private final UUID processingGroupId;
  private final Instant startTime;
  private final Instant endTime;
  private final ProcessingContext processingContext;

  public InvokeBeamformingDto(UUID processingGroupId,
      Instant startTime, Instant endTime,
      ProcessingContext processingContext) {
    this.processingGroupId = processingGroupId;
    this.startTime = startTime;
    this.endTime = endTime;
    this.processingContext = processingContext;
  }

  public UUID getProcessingGroupId() {
    return processingGroupId;
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

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    InvokeBeamformingDto that = (InvokeBeamformingDto) o;

    if (!processingGroupId.equals(that.processingGroupId)) {
      return false;
    }
    if (!startTime.equals(that.startTime)) {
      return false;
    }
    if (!endTime.equals(that.endTime)) {
      return false;
    }
    return processingContext.equals(that.processingContext);
  }

  @Override
  public int hashCode() {
    int result = processingGroupId.hashCode();
    result = 31 * result + startTime.hashCode();
    result = 31 * result + endTime.hashCode();
    result = 31 * result + processingContext.hashCode();
    return result;
  }
}
