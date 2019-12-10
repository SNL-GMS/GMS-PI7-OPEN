package gms.dataacquisition.stationreceiver.cd11.connman;

import gms.dataacquisition.stationreceiver.cd11.connman.configuration.Cd11ConnectionManagerConfig;
import gms.dataacquisition.stationreceiver.cd11.connman.configuration.Cd11ConnectionManagerConfigurationLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Runs the Connection Manager socket service.
 */
public class Application {

  private static Logger logger = LoggerFactory.getLogger(Application.class);

  public static void main(String[] args) {
    try {
      // Start up the Connection Manager.
      Cd11ConnectionManagerConfig config = Cd11ConnectionManagerConfigurationLoader.load();
      Cd11ConnectionManager connMan = new Cd11ConnectionManager(config);
      connMan.start();

      // Wait until the thread stops.
      connMan.waitUntilThreadStops();

      // Check for an error message.
      if (connMan.hasErrorMessage()) {
        logger.error(connMan.getErrorMessage());
        System.exit(1);
      }
    } catch (Exception e) {
      logger.error("ConnMan threw an exception in main(): ", e);
      System.exit(1);
    }
  }
}
