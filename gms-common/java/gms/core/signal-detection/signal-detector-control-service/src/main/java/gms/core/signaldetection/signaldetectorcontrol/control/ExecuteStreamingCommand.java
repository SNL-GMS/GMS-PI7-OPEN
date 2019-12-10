package gms.core.signaldetection.signaldetectorcontrol.control;

import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.processingcontrol.commonobjects.ProcessingContext;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import java.time.Instant;
import java.util.Objects;

public class ExecuteStreamingCommand {

  private final ChannelSegment<Waveform> channelSegment;
  private final Instant startTime;
  private final Instant endTime;
  //TODO: to be included when SignalDetectorParameters is implemented
  //private final SignalDetectorParameters signalDetectorParameters;
  private final ProcessingContext processingContext;

  /**
   * Factory method for creating a standard ExecuteStreamingCommand
   *
   * @param channelSegment The Id to retrieve for signal detection processing
   * @param startTime The start of the signal detection processing timeframe
   * @param endTime The end of the signal detection processing timeframe
   * @param processingContext Context in which we are running signal detection
   * including processing step, analyst action, and storage visability
   * @return A standard command object used for executing signal detection
   */
  public static ExecuteStreamingCommand create(
      ChannelSegment<Waveform> channelSegment,
      Instant startTime, 
      Instant endTime,
      //TODO: to be included when SignalDetectorParameters is implemented
      //SignalDetectorParameters signalDetectorParameters,
      ProcessingContext processingContext) {

    Objects.requireNonNull(channelSegment,
        "Error creating ExecuteStreamingCommand: Channel Segment Id cannot be null");
    Objects.requireNonNull(startTime,
        "Error creating ExecuteStreamingCommand: Start Time cannot be null");
    Objects.requireNonNull(endTime,
        "Error creating ExecuteStreamingCommand: End Time cannot be null");
    //TODO: to be included when SignalDetectorParameters is implemented
    //Objects.requireNonNull(signalDetectorParameters,
    //    "Error Creating ExecuteStreamingCommand: Signal Detector Parameters cannot be null");
    Objects.requireNonNull(processingContext,
        "Error creating ExecuteStreamingCommand: Processing Context cannot be null");

    return new ExecuteStreamingCommand(channelSegment, startTime, endTime, processingContext);
  }

  private ExecuteStreamingCommand(ChannelSegment<Waveform> channelSegment,
      Instant startTime,
      Instant endTime,
      //TODO: to be included when SignalDetectorParameters is implemented
      //SignalDetectorParameters signalDetectorParameters,
      ProcessingContext processingContext) {
    this.channelSegment = channelSegment;
    this.startTime = startTime;
    this.endTime = endTime;
    //TODO: to be included when SignalDetectorParameters is implemented
    //this.signalDetectorParameters = signalDetectorParameters;
    this.processingContext = processingContext;
  }

  public ChannelSegment<Waveform> getChannelSegment() {
    return channelSegment;
  }

  public Instant getStartTime() {
    return startTime;
  }

  public Instant getEndTime() {
    return endTime;
  }

  //TODO: to be included when SignalDetectorParameters is implemented
  //public SignalDetectorParameters getSignalDetectorParameters() {
  //  return signalDetectorParameters;
  //}

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

    ExecuteStreamingCommand that = (ExecuteStreamingCommand) o;

    if (channelSegment != null ? !channelSegment.equals(that.channelSegment)
        : that.channelSegment != null) {
      return false;
    }
    if (startTime != null ? !startTime.equals(that.startTime) : that.startTime != null) {
      return false;
    }
    if (endTime != null ? !endTime.equals(that.endTime) : that.endTime != null) {
      return false;
    }
    return processingContext != null ? processingContext.equals(that.processingContext)
        : that.processingContext == null;
  }

  @Override
  public int hashCode() {
    int result = channelSegment != null ? channelSegment.hashCode() : 0;
    result = 31 * result + (startTime != null ? startTime.hashCode() : 0);
    result = 31 * result + (endTime != null ? endTime.hashCode() : 0);
    result = 31 * result + (processingContext != null ? processingContext.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "ExecuteStreamingCommand{" +
        "channelSegment=" + channelSegment +
        ", startTime=" + startTime +
        ", endTime=" + endTime +
        ", processingContext=" + processingContext +
        '}';
  }
}
