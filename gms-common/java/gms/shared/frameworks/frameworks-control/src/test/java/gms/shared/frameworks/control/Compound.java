package gms.shared.frameworks.control;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import java.util.Collection;
import java.util.Collections;

@AutoValue
public abstract class Compound<T> {

  public abstract int getInt();
  public abstract Collection<T> getGenericCollection();

  @JsonCreator
  public static <T> Compound<T> create(
      @JsonProperty("int") int anInt,
      @JsonProperty("genericCollection") Collection<T> genericCollection) {
    return new AutoValue_Compound<>(anInt, genericCollection);
  }
}
