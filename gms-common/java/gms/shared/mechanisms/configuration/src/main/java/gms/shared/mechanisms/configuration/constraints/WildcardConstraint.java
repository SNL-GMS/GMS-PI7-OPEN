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
 * A constraint trivially satisfied by any value.
 */
@AutoValue
@JsonIgnoreProperties({"operator", "value", "priority"})
public abstract class WildcardConstraint implements Constraint<String, Object> {

  /**
   * Obtains a new {@link WildcardConstraint} for the provided criterion
   *
   * @param criterion String criterion, not null
   * @return {@link WildcardConstraint}, not null
   * @throws NullPointerException if criterion is null
   */
  @JsonCreator
  public static WildcardConstraint from(
      @JsonProperty("criterion") String criterion) {

    return new AutoValue_WildcardConstraint(ConstraintType.WILDCARD, criterion,
        Operator.from(Type.EQ, false), 0, "*");
  }

  /**
   * All values satisfy WildcardConstraint, so this operation always returns true.
   *
   * @param queryVal any object, ignored
   * @return true
   */
  @Override
  public boolean test(Object queryVal) {
    return true;
  }
}
