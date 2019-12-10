package gms.dataacquisition.css.stationrefconverter;

import com.fasterxml.jackson.databind.ObjectMapper;
import gms.dataacquisition.css.stationrefconverter.commandline.StationRefConverterCommandLineArgs;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.datatransferobjects.CoiObjectMapperFactory;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Objects;

import org.kohsuke.args4j.CmdLineParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Command-line application to load data from CSS flat files.
 */
public class Application {

  private static final Logger logger = LoggerFactory.getLogger(Application.class);

  private static final ObjectMapper objMapper = CoiObjectMapperFactory.getJsonObjectMapper();

  private static final String NETWORK_FILE_NAME = "network.json";
  private static final String STATION_FILE_NAME = "station.json";
  private static final String SITE_FILE_NAME = "site.json";
  private static final String CHANNEL_FILE_NAME = "channel.json";
  private static final String CALIBRATION_FILE_NAME = "calibration.json";
  private static final String RESPONSE_FILE_NAME = "response.json";
  private static final String SENSOR_FILE_NAME = "sensor.json";
  private static final String NETWORK_MEMBERSHIPS_FILE_NAME = "network-memberships.json";
  private static final String STATION_MEMBERSHIPS_FILE_NAME = "station-memberships.json";
  private static final String SITE_MEMBERSHIPS_FILE_NAME = "site-memberships.json";


  private static final StationRefConverterCommandLineArgs cmdLineArgs
      = new StationRefConverterCommandLineArgs();

  public static void main(String[] args) {
    try {
      // Read command line args
      new CmdLineParser(cmdLineArgs).parseArgument(args);
      File targetDir = new File(cmdLineArgs.getOutputDir());
      if(targetDir.exists()){
          throw new IllegalArgumentException(String.format("Target directory %s already exists.",
                  cmdLineArgs.getOutputDir()));
      }
      else {
          try {
              targetDir.mkdirs();
          } catch (SecurityException ex) {
              logger.error("Error creating directory", ex);
              System.exit(1);
          }
          execute(cmdLineArgs);
          System.exit(0);
      }
    } catch (Exception ex) {
        logger.error("Error in Application.execute", ex);
        System.exit(1);
    }
  }

  /**
   * Performs the load.
   *
   * @param args command-line args
   */
  public static void execute(StationRefConverterCommandLineArgs args) {
    Objects.requireNonNull(args, "Cannot take null arguments");
    //Check if the directory already exists, refuse to write new files if it does, for security
    String outputDirString = args.getOutputDir();
    if (!outputDirString.endsWith(File.separator)) {
      outputDirString += File.separator;
    }
    if(outputFilesExist(outputDirString)){
      throw new IllegalArgumentException("Cannot create new files in " + outputDirString +
              ", some of the files to be created already exist. " +
              "Please specify a new directory or remove the files.");
    }
    else {
      try {
        // read the files and convert the objects
        final CssReferenceReader refReader = new CssReferenceReader(
                args.getAffiliationFile(),
                args.getInstrumentFile(),
                args.getNetworkFile(),
                args.getSensorFile(),
                args.getSiteFile(),
                args.getSiteChanFile());

        writeToJson(refReader.getReferenceNetworks(), outputDirString, NETWORK_FILE_NAME);
        writeToJson(refReader.getReferenceStations(), outputDirString, STATION_FILE_NAME);
        writeToJson(refReader.getReferenceSites(), outputDirString, SITE_FILE_NAME);
        writeToJson(refReader.getReferenceChannels(), outputDirString, CHANNEL_FILE_NAME);
        writeToJson(refReader.getCalibrations(), outputDirString, CALIBRATION_FILE_NAME);
        writeToJson(refReader.getResponses(), outputDirString, RESPONSE_FILE_NAME);
        writeToJson(refReader.getSensors(), outputDirString, SENSOR_FILE_NAME);
        writeToJson(refReader.getReferenceNetworkMemberships(), outputDirString, NETWORK_MEMBERSHIPS_FILE_NAME);
        writeToJson(refReader.getReferenceStationMemberships(), outputDirString, STATION_MEMBERSHIPS_FILE_NAME);
        writeToJson(refReader.getReferenceSiteMemberships(), outputDirString, SITE_MEMBERSHIPS_FILE_NAME);
      } catch (Exception e) {
        logger.error("Error Application.execute", e);

      }
    }
  }

  private static <T> void writeToJson(Collection<T> data, String outputDir, String dataName)
      throws IOException {
    final String outputFileName = outputDir + dataName;
    logger.info(String.format("Writing %d %s into %s.", data.size(), dataName, outputFileName));
    objMapper.writeValue(new File(outputFileName), data);
  }

  /**
   * Check if any of the output files writeToJson executes exist
   * @param outputDirString
   * @return true if any of the produced output files exists
   */
  private static boolean outputFilesExist(String outputDirString){
    return new File(outputDirString + NETWORK_FILE_NAME).exists() ||
            new File(outputDirString + STATION_FILE_NAME).exists() ||
            new File(outputDirString + SITE_FILE_NAME).exists() ||
            new File(outputDirString + CHANNEL_FILE_NAME).exists() ||
            new File(outputDirString + CALIBRATION_FILE_NAME).exists() ||
            new File(outputDirString + RESPONSE_FILE_NAME).exists() ||
            new File(outputDirString + SENSOR_FILE_NAME).exists() ||
            new File(outputDirString + NETWORK_MEMBERSHIPS_FILE_NAME).exists() ||
            new File(outputDirString + STATION_MEMBERSHIPS_FILE_NAME).exists() ||
            new File(outputDirString + SITE_MEMBERSHIPS_FILE_NAME).exists();
  }
}
