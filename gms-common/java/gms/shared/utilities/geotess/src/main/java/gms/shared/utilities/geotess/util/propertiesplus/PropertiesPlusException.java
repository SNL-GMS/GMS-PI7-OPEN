package gms.shared.utilities.geotess.util.propertiesplus;

import gms.shared.utilities.geotess.util.exceptions.GMPException;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2008</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
@SuppressWarnings("serial")
public class PropertiesPlusException extends GMPException
{
  public PropertiesPlusException()
  {
    super();
  }

  public PropertiesPlusException(String string)
  {
    super(string);
  }

  public PropertiesPlusException(String string, Throwable throwable)
  {
    super(string, throwable);
  }

  public PropertiesPlusException(Throwable throwable)
  {
    super(throwable);
  }
}
