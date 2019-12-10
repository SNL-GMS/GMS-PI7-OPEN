package gms.shared.frameworks.client;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import java.util.Objects;

// just a complex object for testing that has Jackson annotations
@AutoValue
public abstract class SomeObject<T> {

  public abstract double getNum();
  public abstract String getName();
  public abstract T getGeneric();

  @JsonCreator
  public static <T> SomeObject<T> create(
      @JsonProperty("num") double n,
      @JsonProperty("name") String s,
      @JsonProperty("generic") T generic) {
    return new AutoValue_SomeObject<>(n, s, generic);
  }
}
