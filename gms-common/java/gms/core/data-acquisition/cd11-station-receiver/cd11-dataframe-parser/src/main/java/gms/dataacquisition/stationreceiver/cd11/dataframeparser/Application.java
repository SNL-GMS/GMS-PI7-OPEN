package gms.dataacquisition.stationreceiver.cd11.dataframeparser;

import gms.dataacquisition.stationreceiver.cd11.dataframeparser.configuration.DataframeParserConfig;
import gms.dataacquisition.stationreceiver.cd11.dataframeparser.configuration.DataframeParserConfigurationLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Runs the Dataframe Parser.
 */
public class Application {

  private static Logger logger = LoggerFactory.getLogger(Application.class);

  public static void main(String[] args) {
    try {
      Runtime.getRuntime().addShutdownHook(new Thread(() -> {
        logger.info("JVM shutdown hook invoked");
      }));
      // Start up the Dataframe Parser thread.
      DataframeParserConfig config = DataframeParserConfigurationLoader.load();
      DataframeParser dataframeParser = new DataframeParser(config, new SystemControllerNotifier());
      dataframeParser.blockingMonitor();
      logger.info("Data frame parser thread stopped");
    } catch (Exception e) {
      logger.error("Dataframe Parser threw an exception in main(): ", e);
      System.exit(1);
    }
  }
}
