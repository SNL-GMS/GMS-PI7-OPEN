package gms.core.signaldetection.signaldetectorcontrol.http;

import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.processingcontrol.commonobjects.ProcessingContext;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import java.time.Instant;

/**
 * Data Transfer Object for the request body used in claim check invocations of {@link
 * gms.core.signaldetection.signaldetectorcontrol.control.SignalDetectorControl} via {@link
 * SignalDetectorControlRouteHandler#streaming(ContentType, byte[], ContentType)}
 */
public class StreamingDto {

  private ChannelSegment<Waveform> channelSegment;
  private Instant startTime;
  private Instant endTime;
  //TODO: to be included when SignalDetectorParameters is implemented
  //private SignalDetectorParameters signalDetectorParameters;
  private ProcessingContext processingContext;

  public StreamingDto() {
  }

  public ChannelSegment<Waveform> getChannelSegment() {
    return channelSegment;
  }

  public void setChannelSegment(ChannelSegment<Waveform> channelSegment) {
    this.channelSegment = channelSegment;
  }

  public Instant getStartTime() {
    return startTime;
  }

  public void setStartTime(Instant startTime) {
    this.startTime = startTime;
  }

  public Instant getEndTime() {
    return endTime;
  }

  public void setEndTime(Instant endTime) {
    this.endTime = endTime;
  }

  //TODO: to be included when SignalDetectorParameters is implemented
  //public SignalDetectorParameters getSignalDetectorParameters() {
  //  return signalDetectorParameters;
  //}

  //public void setSignalDetectorParameters(
  //    SignalDetectorParameters signalDetectorParameters) {
  //  this.signalDetectorParameters = signalDetectorParameters;
  //}

  public ProcessingContext getProcessingContext() {
    return processingContext;
  }

  public void setProcessingContext(
      ProcessingContext processingContext) {
    this.processingContext = processingContext;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof StreamingDto)) {
      return false;
    }

    StreamingDto that = (StreamingDto) o;

    return (channelSegment != null ? channelSegment
        .equals(that.channelSegment) : that.channelSegment == null) && (
        startTime != null ? startTime.equals(that.startTime) : that.startTime == null) && (
        endTime != null ? endTime.equals(that.endTime) : that.endTime == null) && (
        //TODO: to be included when SignalDetectorParameters is implemented
        //signalDetectorParameters != null ? signalDetectorParameters.equals(that.signalDetectorParameters) : that.signalDetectorParameters == null) && (
        processingContext != null ? processingContext.equals(that.processingContext)
            : that.processingContext == null);
  }

  @Override
  public int hashCode() {
    int result = getChannelSegment().hashCode();
    result = 31 * result + getStartTime().hashCode();
    result = 31 * result + getEndTime().hashCode();
    //TODO: to be included when SignalDetectorParameters is implemented
    //result = 31 * result + getSignalDetectorParameters().hashCode();
    result = 31 * result + getProcessingContext().hashCode();
    return result;
  }
}