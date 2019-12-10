package gms.shared.mechanisms.configuration.constraints;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import gms.shared.mechanisms.configuration.Constraint;
import gms.shared.mechanisms.configuration.ConstraintType;
import gms.shared.mechanisms.configuration.Operator;
import gms.shared.mechanisms.configuration.Operator.Type;
import java.time.LocalTime;
import java.util.Objects;

/**
 * A {@link Constraint} with {@link TimeOfDayRange} values satisfied by {@link LocalTime} values.
 * Only uses {@link Operator} with type {@link Type#IN}.  If the Operator is not negated the
 * Constraint is satisfied when the Selector value is inside the range (minimum value inclusive;
 * maximum value exclusive).  If the Operator is negated the Constraint is satisfied when the
 * Selector value is outside the range (less than the minimum value; greater than or equal to the
 * maximum value).
 *
 * When the maximum time of day value occurs prior to the minimum time of day value,
 * TimeOfDayRangeConstraint assumes the time range begins on one day, spans midnight, and ends on
 * the following day.
 */
@AutoValue
public abstract class TimeOfDayRangeConstraint implements Constraint<TimeOfDayRange, LocalTime> {

  /**
   * Obtains a new {@link TimeOfDayRangeConstraint} from the provided criterion, operator, value,
   * and priority.
   *
   * @param criterion String criterion, not null
   * @param operator {@link Operator}, must have type {@link Type#IN}, not null
   * @param value {@link TimeOfDayRange} constraint value, not null
   * @param priority priority, larger numbers take precedence over smaller numbers
   * @return {@link TimeOfDayRangeConstraint}, not null
   * @throws NullPointerException if criterion, operator, or value are null
   * @throws IllegalArgumentException if operator does not have type {@link Type#IN}
   */
  @JsonCreator
  public static TimeOfDayRangeConstraint from(
      @JsonProperty("criterion") String criterion,
      @JsonProperty("operator") Operator operator,
      @JsonProperty("value") TimeOfDayRange value,
      @JsonProperty("priority") long priority) {

    Operator.assertValidOperatorType(operator, Type.IN);

    return new AutoValue_TimeOfDayRangeConstraint(ConstraintType.TIME_OF_DAY_RANGE, criterion,
        operator, priority, value);
  }

  /**
   * Determines whether the provided {@link LocalTime} queryVal satisfies this {@link
   * TimeOfDayRangeConstraint#getOperator()} and {@link TimeOfDayRangeConstraint#getValue()}. If the
   * Operator is not negated the Constraint is satisfied when the queryVal is inside the range
   * (minimum value inclusive; maximum value exclusive).  If the Operator is negated the Constraint
   * is satisfied when queryVal is outside the range (less than the minimum value; greater than or
   * equal to the maximum value).
   *
   * @param queryVal {@link LocalTime} value, not null
   * @return true if queryVal satisfies this constraint and false otherwise
   * @throws NullPointerException if queryVal is null
   */
  @Override
  public boolean test(LocalTime queryVal) {
    Objects.requireNonNull(queryVal, "queryVal can't be null");

    if (this.getOperator().getType() == Type.IN) {
      return this.getOperator().truth(this.getValue().contains(queryVal));
    } else {
      return false;
    }
  }
}
