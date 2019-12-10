package gms.dataacquisition.css.converters.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

/*
 * Used to relate waveform samples to channel segment Ids
 * Need offset and sample count to find particular subset
 * of values, because multiple waveforms can point to the same
 * .w file and use different data in there
 */
public class WfdiscSampleReference {
  private String waveformFile;
  private int sampleCount;
  private int fOff; //m8 u dnt noe mi
  private String dataType;


  /**
   * Create a new a SegmentAndSohBatch.
   *
   * @param waveformFile .w file which contains the raw samples
   * @param sampleCount number of samples to read
   * @param fOff offset into the file
   * @param dataType offset into the file
   * @return A WfdiscSampleReference object.
   */
  @JsonCreator
  public WfdiscSampleReference(
      @JsonProperty("waveformFile") String waveformFile,
      @JsonProperty("sampleCount") int sampleCount,
      @JsonProperty("fOff") int fOff,
      @JsonProperty("dataType") String dataType){

    Objects.requireNonNull(waveformFile,
        "Error creating WfdiscSampleReference, cannot create from null waveformFile");

    this.waveformFile = waveformFile;
    this.sampleCount = sampleCount;
    this.fOff = fOff;
    this.dataType = dataType;
  }

  public String getWaveformFile() {
    return waveformFile;
  }

  public int getSampleCount() {
    return sampleCount;
  }

  public int getfOff() {
    return fOff;
  }

  public String getDataType() { return dataType; }
}
