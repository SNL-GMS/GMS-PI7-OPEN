package gms.processors.invokefk;

import java.time.Instant;
import java.util.UUID;

public class InvokeFkMeasurementDto {

  private final UUID fkPowerSpectrumChannelId;
  private final Instant startTime;
  private final Instant endTime;
  private final ProcessingContext processingContext;

  public InvokeFkMeasurementDto(UUID processingGroupId, Instant startTime, Instant endTime,
      ProcessingContext processingContext) {
    this.fkPowerSpectrumChannelId = processingGroupId;
    this.startTime = startTime;
    this.endTime = endTime;
    this.processingContext = processingContext;
  }

  public UUID getFkPowerSpectrumChannelId() {
    return fkPowerSpectrumChannelId;
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

    InvokeFkMeasurementDto that = (InvokeFkMeasurementDto) o;

    if (!fkPowerSpectrumChannelId.equals(that.fkPowerSpectrumChannelId)) {
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
    int result = fkPowerSpectrumChannelId.hashCode();
    result = 31 * result + startTime.hashCode();
    result = 31 * result + endTime.hashCode();
    result = 31 * result + processingContext.hashCode();
    return result;
  }
}
