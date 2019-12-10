package gms.core.waveformqc.waveformqccontrol.configuration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import java.util.List;

@AutoValue
public abstract class QcParametersFile {

  public abstract List<QcParameters> getQcParameters();

  @JsonCreator
  public static QcParametersFile from(
      @JsonProperty("qcParameters") List<QcParameters> qcParameters) {
    return new AutoValue_QcParametersFile(qcParameters);
  }
}
