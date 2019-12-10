package gms.processors.invokefk;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public class InvokeFkSpectrumDto {

  private final UUID processingGroupId;
  private final Map<String, UUID> phaseToOutputChannel;
  private final Instant startTime;
  private final int sampleCount;
  private final ProcessingContext processingContext;

  public InvokeFkSpectrumDto(UUID processingGroupId,
      Map<String, UUID> phaseToOutputChannel, Instant startTime, int sampleCount,
      ProcessingContext processingContext) {
    this.processingGroupId = processingGroupId;
    this.phaseToOutputChannel = phaseToOutputChannel;
    this.startTime = startTime;
    this.sampleCount = sampleCount;
    this.processingContext = processingContext;
  }

  public UUID getProcessingGroupId() {
    return processingGroupId;
  }

  public Map<String, UUID> getPhaseToOutputChannel() {
    return phaseToOutputChannel;
  }

  public Instant getStartTime() {
    return startTime;
  }

  public int getSampleCount() {
    return sampleCount;
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

    InvokeFkSpectrumDto that = (InvokeFkSpectrumDto) o;

    if (sampleCount != that.sampleCount) {
      return false;
    }
    if (!processingGroupId.equals(that.processingGroupId)) {
      return false;
    }
    if (!phaseToOutputChannel.equals(that.phaseToOutputChannel)) {
      return false;
    }
    if (!startTime.equals(that.startTime)) {
      return false;
    }
    return processingContext.equals(that.processingContext);
  }

  @Override
  public int hashCode() {
    int result = processingGroupId.hashCode();
    result = 31 * result + phaseToOutputChannel.hashCode();
    result = 31 * result + startTime.hashCode();
    result = 31 * result + sampleCount;
    result = 31 * result + processingContext.hashCode();
    return result;
  }
}
