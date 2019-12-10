package gms.shared.mechanisms.configuration.constraints;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import gms.shared.mechanisms.configuration.Constraint;
import gms.shared.mechanisms.configuration.ConstraintType;
import gms.shared.mechanisms.configuration.Operator;
import gms.shared.mechanisms.configuration.Operator.Type;

/**
 * {@link Constraint} with boolean value and queryValue.  It exclusively uses the non-negated {@link
 * Operator} type {@link Type#EQ}
 */
@AutoValue
@JsonIgnoreProperties({"operator"})
public abstract class BooleanConstraint implements Constraint<Boolean, Boolean> {

  /**
   * Obtain a new {@link BooleanConstraint} with the provided criterion, value, and priority.  The
   * BooleanConstraint is satisfied with boolean values equal to the provided value.
   *
   * @param criterion String constraint criterion name, not null
   * @param value constraint's boolean value
   * @param priority constraint's priority
   * @return {@link BooleanConstraint}, not null
   */
  @JsonCreator
  public static BooleanConstraint from(
      @JsonProperty("criterion") String criterion,
      @JsonProperty("value") boolean value,
      @JsonProperty("priority") long priority) {
    return new AutoValue_BooleanConstraint(ConstraintType.BOOLEAN, criterion,
        Operator.from(Type.EQ, false), priority, value);
  }

  /**
   * Determine wheter this BooleanConstraint is satisfied by the provided query value.  Implements a
   * boolean equality against {@link BooleanConstraint#getValue()}.  Is not satisfied by null.
   *
   * @param queryVal boolean query value
   * @return true if the provided query value is not null and matches {@link
   * BooleanConstraint#getValue()}, false otherwise.
   */
  @Override
  public boolean test(Boolean queryVal) {
    return getOperator().truth(this.getValue().equals(queryVal));
  }
}