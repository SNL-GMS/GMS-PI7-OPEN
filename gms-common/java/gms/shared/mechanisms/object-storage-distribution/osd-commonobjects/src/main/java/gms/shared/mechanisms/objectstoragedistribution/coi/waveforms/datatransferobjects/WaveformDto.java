package gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.datatransferobjects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import java.time.Instant;

/**
 * A Data Transfer Object (DTO) for the Waveform class. Annotates some properties so that Jackson
 * knows how to deserialize them - part of the reason this is necessary is that the original
 * Waveform class is immutable.  This class is a Jackson 'Mix-in annotations' class.
 */
public interface WaveformDto {

  @JsonCreator
  static Waveform from(
      @JsonProperty("startTime") Instant start,
      @JsonProperty("sampleRate") double sampleRate,
      @JsonProperty("sampleCount") long sampleCount,
      @JsonProperty("values") double[] values) {

    return Waveform.from(start, sampleRate, sampleCount, values);
  }

  // below: omit these values from serialization
  @JsonIgnore
  double getFirstSample();

  @JsonIgnore
  double getLastSample();

  @JsonIgnore
  double getSamplePeriod();

  @JsonIgnore
  Instant getEndTime();

}
