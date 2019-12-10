package gms.dataacquisition.css.processingconverter;

import com.fasterxml.jackson.databind.ObjectMapper;
import gms.dataacquisition.css.processingconverter.commandline.CssEventAndSignalDetectionConverterArguments;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.datatransferobjects.CoiObjectMapperFactory;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Objects;
import org.kohsuke.args4j.CmdLineParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Command-line application to load event and signal detection data from CSS flat files.
 */
public class Application {

  private static final Logger logger = LoggerFactory.getLogger(Application.class);

  private static final CssEventAndSignalDetectionConverterArguments cmdLineArgs
      = new CssEventAndSignalDetectionConverterArguments();

  private static final String OUTPUT_LEAF_DIR_NAME = "gms_test_data_set";
  private static final String OUTPUT_EVENTS_FILE_NAME = "events.json";
  private static final String OUTPUT_SIGNAL_DETECTIONS_FILE_NAME = "signal-detections.json";
  private static final ObjectMapper objMapper = CoiObjectMapperFactory.getJsonObjectMapper();

  public static void main(String[] args) {
    try {
      // Read command line args
      new CmdLineParser(cmdLineArgs).parseArgument(args);
      if(!cmdLineArgs.getOutputDir().endsWith(OUTPUT_LEAF_DIR_NAME)){
        throw new IllegalArgumentException(String.format("Data must be output to directory named %s. " +
                "Arg provided was %s", OUTPUT_LEAF_DIR_NAME, cmdLineArgs.getOutputDir()));
      }
      else{
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
   * @param args arguments
   */
  public static void execute(CssEventAndSignalDetectionConverterArguments args) throws Exception {
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
        final CssEventAndSignalDetectionConverter converter = new CssEventAndSignalDetectionConverter(
                args.getEventFile(), args.getOriginFile(), args.getOrigerrFile(),
                args.getArrivalFile(), args.getAssocFile(),
                args.getAmplitudeFile(),
                args.getStationsFile(), args.getAridToWfidFile());
        logger.info(String.format("Successfully read and converted %d events and %d signal detections; "
                        + "writing out to %s and %s, respectively",
                converter.getEvents().size(),
                converter.getSignalDetections().size(),
                OUTPUT_EVENTS_FILE_NAME, OUTPUT_SIGNAL_DETECTIONS_FILE_NAME));
        //Write to JSON
        final String eventOutputFile = outputDirString + OUTPUT_EVENTS_FILE_NAME;
        writeToJson(converter.getEvents(), eventOutputFile);
        logger.info("Successfully wrote events to file " + eventOutputFile);
        final String sigDetOutputFile = outputDirString + OUTPUT_SIGNAL_DETECTIONS_FILE_NAME;
        writeToJson(converter.getSignalDetections(), sigDetOutputFile);
        logger.info("Successfully wrote signal detections to file " + sigDetOutputFile);
      }catch (Exception e){
        logger.error("Error Application.execute", e);
      }
    }
  }

  private static void writeToJson(Object data, String file) throws IOException {
    final byte[] bytes = objMapper.writeValueAsBytes(data);
    Files.write(Paths.get(file), bytes, StandardOpenOption.CREATE);
  }

  /**
   * Check if any of the output files writeToJson executes exist
   * @param outputDirString
   * @return true if any of the produced output files exists
   */
  private static boolean outputFilesExist(String outputDirString){
    return new File(outputDirString + OUTPUT_EVENTS_FILE_NAME).exists() ||
            new File(outputDirString + OUTPUT_SIGNAL_DETECTIONS_FILE_NAME).exists();
  }
}
