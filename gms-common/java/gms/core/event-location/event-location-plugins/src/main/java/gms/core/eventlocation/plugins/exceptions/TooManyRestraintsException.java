package gms.core.eventlocation.plugins.exceptions;

/**
 * Meant to be thrown when a locator algorithm does not have enough degrees of freedom to work with
 * because there are too many restrained values
 */
public class TooManyRestraintsException extends Exception {

  /**
   * Construct the exception
   *
   * @param numberOfRestraints the number of restraints that caused the exception.
   */
  public TooManyRestraintsException(int numberOfRestraints) {
    super("Cannot locate with " + numberOfRestraints + " restraints.");
  }

}
