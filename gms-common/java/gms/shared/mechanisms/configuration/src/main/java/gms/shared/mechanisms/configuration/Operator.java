package gms.shared.mechanisms.configuration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import java.util.Objects;
import java.util.Set;

@AutoValue
public abstract class Operator {

  public enum Type {
    IN,
    EQ
  }

  @JsonCreator
  public static Operator from(
      @JsonProperty("type") Type type,
      @JsonProperty("negate") boolean negated) {

    return new AutoValue_Operator(type, negated);
  }

  public abstract Type getType();

  public abstract boolean isNegated();

  public boolean truth(boolean value) {
    return this.isNegated() ? !value : value;
  }


  /**
   * Determines whether the provided {@link Operator} is non-null and has one of the provided {@link
   * Type}s
   *
   * @param op {@link Operator}, not null
   * @param validOperatorTypes variable argument list of {@link Type}, not null
   * @throws NullPointerException if op or validOperatorTypes are nukk
   * @throws IllegalArgumentException if the operator does not have one of the validOperatorTyes
   */
  public static void assertValidOperatorType(Operator op, Type... validOperatorTypes) {
    Objects.requireNonNull(op, "Operator can't be null");
    Objects.requireNonNull(validOperatorTypes, "validOperatorTypes can't be null");

    final boolean isValidOperator = Set.of(validOperatorTypes).contains(op.getType());
    if (!isValidOperator) {
      throw new IllegalArgumentException(
          "Operator Type: " + op.getType().name() + " is not supported");
    }
  }

}