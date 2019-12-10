package gms.shared.mechanisms.objectstoragedistribution.coi.common;

import java.util.function.BiPredicate;
import java.util.function.Predicate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Contains utility operations for validating parameters
 */
public class ParameterValidation {

  private final static Logger logger = LogManager.getLogger(ParameterValidation.class);

  private ParameterValidation() {
  }

  /**
   * Validation method that requires the validator to return true given the input object, otherwise
   * logs the message and throws a RuntimeException with that message.
   *
   * @param validator Predicate used to validate the input object.
   * @param object Input object to be validated.
   * @param exceptionMessage Message to log and throw in the event the object is invalid.
   * @param <T> Any class whose object needs to be validated.
   */
  public static <T> void requireTrue(Predicate<T> validator, T object, String exceptionMessage) {
    if (!validator.test(object)) {
      RuntimeException e = new IllegalArgumentException(exceptionMessage);
      logger.error(exceptionMessage, e);
      throw e;
    }
  }

  /**
   * Validation method that requires the validator to return true given the input objects, otherwise
   * logs the message and throws a RuntimeException with that message.
   *
   * @param validator Predicate used to validate the input objects. Note BiPredicate can be a method
   * reference representing {@code (o1, o2) -> method(o1, o2) or (o1, o2) -> o1.method(o2) },
   * opening up lots of possible validation methods.
   * @param object1 First input object to be validated.
   * @param object2 Second input object to be validated.
   * @param exceptionMessage Message to log and throw in the event the object is invalid.
   * @param <T> Any class whose object needs to be validated.
   */
  public static <T, U> void requireTrue(BiPredicate<T, U> validator, T object1, U object2,
      String exceptionMessage) {
    if (!validator.test(object1, object2)) {
      RuntimeException e = new IllegalArgumentException(exceptionMessage);
      logger.error(exceptionMessage, e);
      throw e;
    }

  }

  /**
   * Validation method that requires the validator to return false given the input object, otherwise
   * logs the message and throws a RuntimeException with that message.
   *
   * @param validator Predicate used to validate the input object.
   * @param object Input object to be validated.
   * @param exceptionMessage Message to log and throw in the event the object is invalid.
   * @param <T> Any class whose object needs to be validated.
   */
  public static <T> void requireFalse(Predicate<T> validator, T object, String exceptionMessage) {
    if (validator.test(object)) {
      RuntimeException e = new IllegalArgumentException(exceptionMessage);
      logger.error(exceptionMessage, e);
      throw e;
    }
  }

  /**
   * Validation method that requires the validator to return false given the input objects,
   * otherwise logs the message and throws a RuntimeException with that message.
   *
   * @param validator Predicate used to validate the input objects. Note BiPredicate can be a method
   * reference representing {@code (o1, o2) -> method(o1, o2) or (o1, o2) -> o1.method(o2) },
   * opening up lots of possible validation methods.
   * @param object1 First input object to be validated.
   * @param object2 Second input object to be validated.
   * @param exceptionMessage Message to log and throw in the event the object is invalid.
   * @param <T> Any class whose object needs to be validated.
   */
  public static <T, U> void requireFalse(BiPredicate<T, U> validator, T object1, U object2,
      String exceptionMessage) {
    if (validator.test(object1, object2)) {
      RuntimeException e = new IllegalArgumentException(exceptionMessage);
      logger.error(exceptionMessage, e);
      throw e;
    }

  }

}
