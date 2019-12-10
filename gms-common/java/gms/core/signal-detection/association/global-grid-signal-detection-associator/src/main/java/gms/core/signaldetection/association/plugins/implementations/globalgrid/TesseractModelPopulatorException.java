package gms.core.signaldetection.association.plugins.implementations.globalgrid;

/**
 * Checked exception that signals that {@link TesseractModelPopulator} has failed to initialize.
 */
public class TesseractModelPopulatorException extends Exception {

  private static final long serialVersionUID = -5320296740904396968L;

  /**
   * Creates a new {@link TesseractModelPopulatorException} with the provided message.
   *
   * @param message Message describing the reason for throwing this {@link
   * TesseractModelPopulatorException}.
   */
  public TesseractModelPopulatorException(String message) {
    super(message);
  }

  /**
   * Creates a new {@link TesseractModelPopulatorException} with the provided message
   * and the {@link Throwable} that caused this exception to be thrown.
   *
   * @param message Message describing the reason for throwing this {@link *
   * TesseractModelPopulatorException}.
   * @param cause {@link Throwable} that caused this exception to be thrown.
   */
  public TesseractModelPopulatorException(String message, Throwable cause) {
    super(message, cause);
  }
}
