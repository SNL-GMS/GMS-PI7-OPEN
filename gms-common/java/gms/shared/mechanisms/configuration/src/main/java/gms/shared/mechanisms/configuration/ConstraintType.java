package gms.shared.mechanisms.configuration;

/**
 * Enumerates the available types of {@link Constraint}.
 */
public enum ConstraintType {
  BOOLEAN,
  DEFAULT,
  NUMERIC_RANGE,
  NUMERIC_SCALAR,
  PHASE,
  STRING,
  TIME_OF_DAY_RANGE,
  TIME_OF_YEAR_RANGE,
  WILDCARD
}
