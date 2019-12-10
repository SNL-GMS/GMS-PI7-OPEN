package gms.shared.mechanisms.configuration;

/**
 * Utility evaluating a {@link Constraint} with a {@link Selector}
 */
class ConstraintEvaluator {

  private ConstraintEvaluator() {
  }

  /**
   * Determines whether the {@link Constraint} is satisfied by the {@link Selector}.  This occurs
   * if the {@link Constraint#getCriterion()} is the same as the {@link Selector#getCriterion()} and
   * {@link Constraint#test(Object)} evaluates to true when provided the {@link Selector#getValue()}
   * @param constraint {@link Constraint}, not null
   * @param selector {@link Selector}, not null
   * @param <C> Constraint value type
   * @param <S> Selector value type
   * @return true if the Selector satisifed the Constraint and false otherwise.
   */
  static <C, S> boolean evaluate(Constraint<C, S> constraint, Selector<S> selector) {
    return (sameCriterion(selector, constraint) && constraint.test(selector.getValue()));
  }

  /**
   * Determines if the selector and constraint are for the same criterion.
   * @param selector {@link Selector}, not null
   * @param constraint {@link Constraint}, not null
   * @return true if selector and constraint have the same criterion, false otherwise
   */
  private static <C, S> boolean sameCriterion(Selector<S> selector, Constraint<C, S> constraint) {
    return selector.getCriterion().equals(constraint.getCriterion());
  }
}
