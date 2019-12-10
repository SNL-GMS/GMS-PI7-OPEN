package gms.core.signalenhancement.fk.control.configuration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import java.util.List;

@AutoValue
public abstract class FkAttributesParametersFile {

  public abstract List<FkAttributesParameters> getFkAttributesParameters();

  @JsonCreator
  public static FkAttributesParametersFile from(
      @JsonProperty("fkAttributesParameters") List<FkAttributesParameters> fkAttributesParameters) {
    return new AutoValue_FkAttributesParametersFile(fkAttributesParameters);
  }

}
