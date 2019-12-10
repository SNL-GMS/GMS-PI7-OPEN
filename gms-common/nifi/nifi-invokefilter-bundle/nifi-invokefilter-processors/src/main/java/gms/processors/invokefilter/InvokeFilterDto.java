package gms.processors.invokefilter;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public class InvokeFilterDto {

  private final UUID channelProcessingStepId;
  private final Map<UUID, UUID> inputToOutputChannelIds;
  private final Instant startTime;
  private final Instant endTime;
  private final ProcessingContext processingContext;

  public InvokeFilterDto(UUID channelProcessingStepId,
      Map<UUID, UUID> inputToOutputChannelIds, Instant startTime, Instant endTime,
      ProcessingContext processingContext) {
    this.channelProcessingStepId = channelProcessingStepId;
    this.inputToOutputChannelIds = inputToOutputChannelIds;
    this.startTime = startTime;
    this.endTime = endTime;
    this.processingContext = processingContext;
  }

  public UUID getChannelProcessingStepId() {
    return channelProcessingStepId;
  }

  public Map<UUID, UUID> getInputToOutputChannelIds() {
    return inputToOutputChannelIds;
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
}
