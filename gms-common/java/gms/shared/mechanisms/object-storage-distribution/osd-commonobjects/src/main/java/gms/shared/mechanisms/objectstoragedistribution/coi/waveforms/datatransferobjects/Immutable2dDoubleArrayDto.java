package gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.datatransferobjects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Immutable2dDoubleArray;

public interface Immutable2dDoubleArrayDto {

  @JsonCreator
  static Immutable2dDoubleArray from(
      @JsonProperty("values") double[][] values) {

    return Immutable2dDoubleArray.from(values);
  }

  @JsonProperty("values")
  double[][] copyOf();

}
