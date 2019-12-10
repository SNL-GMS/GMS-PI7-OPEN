package gms.core.signaldetection.signaldetectorcontrol.configuration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import java.util.List;

@AutoValue
public abstract class SignalDetectionParametersFile {
  public abstract List<SignalDetectionParameters> getSignalDetectionParameters();

  @JsonCreator
  public static SignalDetectionParametersFile from(
      @JsonProperty("signalDetectionParameters") List<SignalDetectionParameters> signalDetectionParameters) {
    return new AutoValue_SignalDetectionParametersFile(signalDetectionParameters);
  }
}
