package gms.dataacquisition.stationreceiver.cd11.dataprovider;

import gms.dataacquisition.stationreceiver.cd11.dataprovider.configuration.Cd11DataProviderConfig;
import gms.dataacquisition.stationreceiver.cd11.dataprovider.configuration.Cd11DataProviderConfigurationLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Runs the Data Provider socket service.
 */
public class Application {

  private static Logger logger = LoggerFactory.getLogger(Application.class);

  public static void main(String[] args) {
    try {
      // Start up the Data Provider thread.
      Cd11DataProviderConfig config = Cd11DataProviderConfigurationLoader.load();
      Cd11DataProvider dataProvider = new Cd11DataProvider(config);
      dataProvider.start();

      // Wait until the thread stops.
      dataProvider.waitUntilThreadStops();

      // Check for an error message.
      if (dataProvider.hasErrorMessage()) {
        logger.error(dataProvider.getErrorMessage());
        System.exit(1);
      }
    } catch (Exception e) {
      logger.error("Data Provider threw an exception in main(): ", e);
      System.exit(1);
    }
  }
}
