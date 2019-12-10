package gms.shared.mechanisms.configuration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

// TODO: might need to handle generic deserialization for the UI case.
@AutoValue
public abstract class Selector<S> {

  @JsonCreator
  public static <S> Selector<S> from(
      @JsonProperty("criterion") String criterion,
      @JsonProperty("value") S value) {

    return new AutoValue_Selector<>(criterion, value);
  }

  public abstract String getCriterion();

  public abstract S getValue();
}
