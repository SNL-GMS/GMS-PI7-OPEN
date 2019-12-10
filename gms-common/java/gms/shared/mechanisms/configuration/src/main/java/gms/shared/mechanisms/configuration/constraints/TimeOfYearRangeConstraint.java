package gms.shared.mechanisms.configuration.constraints;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import gms.shared.mechanisms.configuration.Constraint;
import gms.shared.mechanisms.configuration.ConstraintType;
import gms.shared.mechanisms.configuration.Operator;
import gms.shared.mechanisms.configuration.Operator.Type;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * A {@link Constraint} with {@link TimeOfYearRange} values satisfied by {@link LocalDateTime}
 * values. Only uses {@link Operator} with type {@link Type#IN}.  If the Operator is not negated the
 * Constraint is satisfied when the Selector value is inside the range (minimum value inclusive;
 * maximum value exclusive).  If the Operator is negated the Constraint is satisfied when the
 * Selector value is outside the range (less than the minimum value; greater than or equal to the
 * maximum value).
 *
 * When the maximum time of year value occurs prior to the minimum time of year value,
 * TimeOfYearRangeConstraint assumes the time of year range begins in one year, spans the new year,
 * and ends in the following year.
 */
@AutoValue
public abstract class TimeOfYearRangeConstraint implements
    Constraint<TimeOfYearRange, LocalDateTime> {

  /**
   * Obtains a new {@link TimeOfYearRangeConstraint} from the provided criterion, operator, value,
   * and priority.
   *
   * @param criterion String criterion, not null
   * @param operator {@link Operator}, must have type {@link Type#IN}, not null
   * @param value {@link TimeOfYearRange} constraint value, not null
   * @param priority priority, larger numbers take precedence over smaller numbers
   * @return {@link TimeOfYearRangeConstraint}, not null
   * @throws NullPointerException if criterion, operator, or value are null
   * @throws IllegalArgumentException if operator does not have type {@link Type#IN}
   */
  @JsonCreator
  public static TimeOfYearRangeConstraint from(
      @JsonProperty("criterion") String criterion,
      @JsonProperty("operator") Operator operator,
      @JsonProperty("value") TimeOfYearRange value,
      @JsonProperty("priority") long priority) {

    Operator.assertValidOperatorType(operator, Type.IN);

    return new AutoValue_TimeOfYearRangeConstraint(ConstraintType.TIME_OF_YEAR_RANGE, criterion,
        operator, priority, value);
  }

  /**
   * Determines whether the provided {@link LocalDateTime} queryVal satisfies this {@link
   * TimeOfYearRangeConstraint#getOperator()} and {@link TimeOfYearRangeConstraint#getValue()}. If
   * the Operator is not negated the Constraint is satisfied when the queryVal is inside the range
   * (minimum value inclusive; maximum value exclusive).  If the Operator is negated the Constraint
   * is satisfied when queryVal is outside the range (less than the minimum value; greater than or
   * equal to the maximum value).  Ignores the year portion of the provided queryValue.  Correctly
   * handles leap years.
   *
   * @param queryVal {@link LocalDateTime} value, not null
   * @return true if queryVal satisfies this constraint and false otherwise
   * @throws NullPointerException if queryVal is null
   */
  @Override
  public boolean test(LocalDateTime queryVal) {
    Objects.requireNonNull(queryVal, "queryVal can't be null");

    if (this.getOperator().getType() == Type.IN) {
      return this.getOperator().truth(this.getValue().contains(queryVal));
    } else {
      return false;
    }
  }
}
