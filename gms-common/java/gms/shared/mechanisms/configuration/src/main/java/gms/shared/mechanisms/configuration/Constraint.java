package gms.shared.mechanisms.configuration;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import gms.shared.mechanisms.configuration.constraints.BooleanConstraint;
import gms.shared.mechanisms.configuration.constraints.DefaultConstraint;
import gms.shared.mechanisms.configuration.constraints.NumericRangeConstraint;
import gms.shared.mechanisms.configuration.constraints.NumericScalarConstraint;
import gms.shared.mechanisms.configuration.constraints.PhaseConstraint;
import gms.shared.mechanisms.configuration.constraints.StringConstraint;
import gms.shared.mechanisms.configuration.constraints.TimeOfDayRangeConstraint;
import gms.shared.mechanisms.configuration.constraints.TimeOfYearRangeConstraint;
import gms.shared.mechanisms.configuration.constraints.WildcardConstraint;
import java.util.function.Predicate;

/**
 * Constraints contain a criterion (a label describing the constraint), a value of type C, and an
 * operator which compares a value of type S against the Constraint's value (S values typically come
 * from {@link Selector}s).  A {@link Constraint#test(Object)} when this comparison evaluates to
 * true.  Constraints also have a {@link ConstraintType} and a priority used as a tiebreaker during
 * configuration resolution (see e.g. {@link ConfigurationResolver}).  Larger priority numbers have
 * higher priority than smaller priority number (i.e. 10 takes precendence over 1)
 *
 * @param <C> constraint's value type
 * @param <S> type of value that can satisfy this constraint.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = As.EXISTING_PROPERTY, property = "constraintType")
@JsonSubTypes({
    @Type(value = BooleanConstraint.class, name = "BOOLEAN"),
    @Type(value = DefaultConstraint.class, name = "DEFAULT"),
    @Type(value = NumericRangeConstraint.class, name = "NUMERIC_RANGE"),
    @Type(value = PhaseConstraint.class, name = "PHASE"),
    @Type(value = StringConstraint.class, name = "STRING"),
    @Type(value = TimeOfDayRangeConstraint.class, name = "TIME_OF_DAY_RANGE"),
    @Type(value = TimeOfYearRangeConstraint.class, name = "TIME_OF_YEAR_RANGE"),
    @Type(value = NumericScalarConstraint.class, name = "NUMERIC_SCALAR"),
    @Type(value = WildcardConstraint.class, name = "WILDCARD")
})
@JsonPropertyOrder({"constraintType", "criterion", "operator", "value", "priority"})
public interface Constraint<C, S> extends Predicate<S> {

  ConstraintType getConstraintType();

  String getCriterion();

  Operator getOperator();

  long getPriority();

  C getValue();
}
