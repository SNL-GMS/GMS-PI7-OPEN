package gms.shared.mechanisms.configuration.constraints;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import gms.shared.mechanisms.configuration.Constraint;
import gms.shared.mechanisms.configuration.ConstraintType;
import gms.shared.mechanisms.configuration.Operator;
import gms.shared.mechanisms.configuration.Operator.Type;
import java.util.Objects;

/**
 * A {@link Constraint} with a {@link DoubleRange} value satisfied by double values.  Only uses
 * {@link Operator} with type {@link Type#IN}.  If the Operator is not negated the Constraint is
 * satisfied when the Selector value is inside the range (minimum value inclusive; maximum value
 * inclusive).  If the Operator is negated the Constraint is satisfied when the Selector value is
 * outside the range (less than the minimum value; greater than the maximum value).
 */
@AutoValue
public abstract class NumericRangeConstraint implements Constraint<DoubleRange, Double> {

  /**
   * Obtains a new {@link NumericRangeConstraint} with the provided criterion, operator, value, and
   * priority.
   *
   * @param criterion String criterion, not null
   * @param operator {@link Operator}, must have type {@link Type#IN}, not null
   * @param value {@link DoubleRange} constraint value, not null
   * @param priority priority, larger numbers take precedence over smaller numbers
   * @return {@link NumericRangeConstraint}, not null
   * @throws NullPointerException if criterion, operator, or value are null
   * @throws IllegalArgumentException if operator does not have type {@link Type#IN}
   */
  @JsonCreator
  public static NumericRangeConstraint from(
      @JsonProperty("criterion") String criterion,
      @JsonProperty("operator") Operator operator,
      @JsonProperty("value") DoubleRange value,
      @JsonProperty("priority") long priority) {

    Operator.assertValidOperatorType(operator, Type.IN);

    return new AutoValue_NumericRangeConstraint(ConstraintType.NUMERIC_RANGE, criterion, operator,
        priority, value);
  }

  /**
   * Determines whether the provided Double queryVal satisfies this {@link
   * NumericRangeConstraint#getOperator()} and {@link NumericRangeConstraint#getValue()}. If the
   * Operator is not negated the Constraint is satisfied when the queryVal is inside the range
   * (minimum value inclusive; maximum value inclusive).  If the Operator is negated the Constraint
   * is satisfied when queryVal is outside the range (less than the minimum value; greater than the
   * maximum value).
   *
   * @param queryVal double value, not null
   * @return true if queryVal satisfies this constraint and false otherwise
   * @throws NullPointerException if queryVal is null
   */
  @Override
  public boolean test(Double queryVal) {

    Objects.requireNonNull(queryVal, "queryValue can't be null");

    // Factory operation guarantees operator type is Type.IN
    if (getOperator().getType() == Type.IN) {
      return getOperator().truth(getValue().contains(queryVal));
    } else {
      return false;
    }
  }
}
