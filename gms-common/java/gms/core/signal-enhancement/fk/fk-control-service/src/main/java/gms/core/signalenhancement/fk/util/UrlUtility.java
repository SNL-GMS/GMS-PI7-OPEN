package gms.core.signalenhancement.fk.util;

import gms.core.signalenhancement.fk.Application;
import java.net.URL;
import java.util.MissingResourceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UrlUtility {

  private static Logger logger = LoggerFactory.getLogger(UrlUtility.class);

  /**
   * Obtains a {@link URL} to the file at the provided path within this Jar's resources directory
   *
   * @param path String file path relative to this Jar's root, not null
   * @return URL to the resources file at the provided path
   */
  public static URL getUrlToResourceFile(String path) {
    final URL propFileUrl = Thread.currentThread().getContextClassLoader().getResource(path);
    if (null == propFileUrl) {
      final String message = "fkcontrol application can't find file in resources: " + path;
      logger.error(message);
      throw new MissingResourceException(message, Application.class.getName(), path);
    }
    return propFileUrl;
  }

}
