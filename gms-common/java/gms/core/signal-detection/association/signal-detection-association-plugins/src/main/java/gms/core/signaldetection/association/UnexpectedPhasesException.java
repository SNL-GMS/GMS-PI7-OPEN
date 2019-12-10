package gms.core.signaldetection.association;

/**
 * A PhaseInfo Collection does not consist of the expected instances
 */
public class UnexpectedPhasesException extends Exception {

  /**
   * A PhaseInfo Collection does not consist of the expected instances.  The message String provides
   * greater detail of what was expected and/or what was found.
   */
  public UnexpectedPhasesException(String message) {
    super(message);
  }
}
