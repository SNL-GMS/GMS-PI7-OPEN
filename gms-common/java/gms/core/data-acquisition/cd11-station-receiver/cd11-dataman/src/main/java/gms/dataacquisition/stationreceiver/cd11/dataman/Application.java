package gms.dataacquisition.stationreceiver.cd11.dataman;

import gms.dataacquisition.stationreceiver.cd11.dataman.configuration.DataManConfig;
import gms.dataacquisition.stationreceiver.cd11.dataman.configuration.DataManConfigurationLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Runs the Data Consumer Manager.
 */
public class Application {

  private static Logger logger = LoggerFactory.getLogger(Application.class);

  public static void main(String[] args) {
    try {
      // Start up the Data Consumer Manager.
      DataManConfig dataManConfig = DataManConfigurationLoader.load();
      DataMan dataMan = new DataMan(dataManConfig);
      dataMan.start();

      // Wait until the thread stops.
      dataMan.waitUntilThreadStops();

      // Check for an error message.
      if (dataMan.hasErrorMessage()) {
        logger.error(dataMan.getErrorMessage());
        System.exit(1);
      }
    } catch (Exception e) {
      logger.error("DataMan threw an exception in main(): ", e);
      System.exit(1);
    }
  }
}
