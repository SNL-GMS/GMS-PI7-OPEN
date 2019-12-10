package gms.shared.utilities.geotess.util.containers;

import java.io.Serializable;

/**
 * Standard 2 parameter tuple object.
 * 
 * @author jrhipp
 *
 * @param <T1> First parameter type.
 * @param <T2> Second parameter type.
 */
@SuppressWarnings("serial")
public class Tuple<T1, T2> implements Serializable 
{
  /**
   * First parameter.
   */
  public T1 first;

  /**
   * Second parameter.
   */
  public T2 second;

  /**
   * Standard constructor.
   * 
   * @param first First parameter.
   * @param second Second parameter.
   */
  public Tuple(T1 first, T2 second)
  {
    this.first = first;
    this.second = second;
  }
}
