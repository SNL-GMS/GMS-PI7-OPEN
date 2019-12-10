package gms.shared.utilities.javautilities.generation;

/**
 * Thrown by implementations of {@code Generator} to indicate generation problems
 */
public class GenerationException extends Exception {

  private static final long serialVersionUID = -2489195974609656569L;

  public GenerationException(String message, Throwable cause) {
    super(message, cause);
  }

  public GenerationException(String message) {
    super(message);
  }

  public GenerationException(Throwable cause) {
    super(cause);
  }

  public GenerationException() {}

}
