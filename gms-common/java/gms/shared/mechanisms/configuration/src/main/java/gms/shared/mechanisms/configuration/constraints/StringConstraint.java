package gms.shared.mechanisms.configuration.constraints;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import gms.shared.mechanisms.configuration.Constraint;
import gms.shared.mechanisms.configuration.ConstraintType;
import gms.shared.mechanisms.configuration.Operator;
import gms.shared.mechanisms.configuration.Operator.Type;
import java.util.Objects;
import java.util.Set;

/**
 * A {@link Constraint} with a Set of {@link String} for the value satisfied by a single String. A
 * StringConstraint can be satisfied in two ways: 1) If the value set contains a single String and
 * the Operator has {@link Type#EQ} the element is equal to a String from a Selector containing a
 * single Strubg (or not equal to that String, depending on {@link Operator#isNegated()}. 2) If the
 * value set contains multiple Strings and the Operator has {@link Type#IN}, determines if the value
 * set includes a String from a Selector containing a single String (or does not include that
 * String, depending on {@link Operator#isNegated()}.
 */
@AutoValue
public abstract class StringConstraint implements Constraint<Set<String>, String> {

  /**
   * Obtains a new {@link StringConstraint} with the provided criterion, operator, value, and
   * priority
   *
   * @param criterion String criterion, not null
   * @param operator {@link Operator}, not null
   * @param value Set of {@link String}, not null
   * @param priority priority, larger numbers take precedence over smaller numbers
   * @return {@link StringConstraint}, not null
   * @throws NullPointerException if criterion, operator, or value are null
   * @throws IllegalArgumentException if operator does not have type {@link Type#EQ} or {@link
   * Type#IN}
   */
  @JsonCreator
  public static StringConstraint from(
      @JsonProperty("criterion") String criterion,
      @JsonProperty("operator") Operator operator,
      @JsonProperty("value") Set<String> value,
      @JsonProperty("priority") long priority) {

    Operator.assertValidOperatorType(operator, Type.IN, Type.EQ);

    return new AutoValue_StringConstraint(ConstraintType.STRING, criterion, operator, priority,
        value);
  }

  /**
   * Determines if the {@link String} queryVal satisfies this {@link StringConstraint#getOperator()}
   * and {@link StringConstraint#getValue()}.  1) If the value set contains a single String and the
   * Operator has {@link Type#EQ} the element is equal to a String from a Selector containing a
   * single String (or not equal to that String, depending on {@link Operator#isNegated()}. 2) If
   * the value set contains multiple String and the Operator has {@link Type#IN}, determines if the
   * value set includes a String from a Selector containing a single String (or does not include
   * that String, depending on {@link Operator#isNegated()}.
   *
   * @param queryVal {@link String} value, not null
   * @return true if the queryVal satisfies this PhaseConstraint, and false otherwise
   * @throws NullPointerException if queryVal is null
   */
  @Override
  public boolean test(String queryVal) {
    Objects.requireNonNull(queryVal, "queryVal can't be null");

    final Set<String> phases = this.getValue();

    if (getOperator().getType() == Type.EQ) {
      return getOperator().truth(phases.contains(queryVal) && phases.size() == 1);
    } else if (getOperator().getType() == Type.IN) {
      return getOperator().truth(phases.contains(queryVal));
    } else {
      return false;
    }
  }
}
