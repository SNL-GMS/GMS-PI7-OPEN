package gms.core.signalenhancement.waveformfiltering.configuration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import java.util.List;

@AutoValue
public abstract class FilterParametersFile {

  public abstract List<FilterParameters> getFilterParameters();

  @JsonCreator
  public static FilterParametersFile from(
      @JsonProperty("filterParameters") List<FilterParameters> qcParameters) {
    return new AutoValue_FilterParametersFile(qcParameters);
  }
}
