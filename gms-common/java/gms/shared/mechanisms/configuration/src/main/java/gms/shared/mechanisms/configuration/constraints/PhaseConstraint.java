package gms.shared.mechanisms.configuration.constraints;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import gms.shared.mechanisms.configuration.Constraint;
import gms.shared.mechanisms.configuration.ConstraintType;
import gms.shared.mechanisms.configuration.Operator;
import gms.shared.mechanisms.configuration.Operator.Type;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.PhaseType;
import java.util.Objects;
import java.util.Set;

/**
 * A {@link Constraint} with a Set of {@link PhaseType} for the value satisfied by a single
 * PhaseType. A PhaseConstraint can be satisfied in two ways: 1) If the value set contains a single
 * PhaseType and the Operator has {@link Type#EQ} the element is equal to a PhaseType from a
 * Selector containing a single PhaseType (or not equal to that PhaseType, depending on {@link
 * Operator#isNegated()}. 2) If the value set contains multiple PhaseType  and the Operator has
 * {@link Type#IN}, determines if the value set includes a PhaseType from a Selector containing a
 * single PhaseType (or does not include that PhaseType, depending on {@link Operator#isNegated()}.
 */
@AutoValue
public abstract class PhaseConstraint implements Constraint<Set<PhaseType>, PhaseType> {

  /**
   * Obtains a new {@link PhaseConstraint} with the provided criterion, operator, value, and
   * priority
   *
   * @param criterion String criterion, not null
   * @param operator {@link Operator}, not null
   * @param value Set of {@link PhaseType}, not null
   * @param priority priority, larger numbers take precedence over smaller numbers
   * @return {@link PhaseConstraint}, not null
   * @throws NullPointerException if criterion, operator, or value are null
   * @throws IllegalArgumentException if operator does not have type {@link Type#EQ} or {@link
   * Type#IN}
   */
  @JsonCreator
  public static PhaseConstraint from(
      @JsonProperty("criterion") String criterion,
      @JsonProperty("operator") Operator operator,
      @JsonProperty("value") Set<PhaseType> value,
      @JsonProperty("priority") long priority) {

    Operator.assertValidOperatorType(operator, Type.IN, Type.EQ);

    return new AutoValue_PhaseConstraint(ConstraintType.PHASE, criterion, operator, priority,
        value);
  }

  /**
   * Determines if the {@link PhaseType} queryVal satisfies this {@link
   * PhaseConstraint#getOperator()} and {@link PhaseConstraint#getValue()}.  1) If the value set
   * contains a single PhaseType and the Operator has {@link Type#EQ} the element is equal to a
   * PhaseType from a Selector containing a single PhaseType (or not equal to that PhaseType,
   * depending on {@link Operator#isNegated()}. 2) If the value set contains multiple PhaseType  and
   * the Operator has {@link Type#IN}, determines if the value set includes a PhaseType from a
   * Selector containing a single PhaseType (or does not include that PhaseType, depending on {@link
   * Operator#isNegated()}.
   *
   * @param queryVal {@link PhaseType} value, not null
   * @return true if the queryVal satisfies this PhaseConstraint, and false otherwise
   * @throws NullPointerException if queryVal is null
   */
  @Override
  public boolean test(PhaseType queryVal) {

    Objects.requireNonNull(queryVal, "queryVal can't be null");

    final Set<PhaseType> phases = this.getValue();
    if (getOperator().getType() == Operator.Type.EQ) {
      return getOperator().truth(phases.contains(queryVal) && phases.size() == 1);
    } else if (getOperator().getType() == Operator.Type.IN) {
      return getOperator().truth(phases.contains(queryVal));
    } else {
      return false;
    }
  }
}
