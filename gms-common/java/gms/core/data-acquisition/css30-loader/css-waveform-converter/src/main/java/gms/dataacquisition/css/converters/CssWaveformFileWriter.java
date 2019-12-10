package gms.dataacquisition.css.converters;

import com.fasterxml.jackson.databind.ObjectMapper;
import gms.dataacquisition.css.converters.data.CssBatchFileWriterCommandLineArgs;
import gms.dataacquisition.css.converters.data.SegmentAndSohBatch;
import gms.dataacquisition.css.converters.data.WfdiscSampleReference;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.datatransferobjects.CoiObjectMapperFactory;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.AcquiredChannelSohBoolean;
import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.*;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Command-line application to load data from CSS flat files.
 */
public class CssWaveformFileWriter {

  private static final Logger logger = LoggerFactory.getLogger(CssWaveformFileWriter.class);

  private static final String OUTPUT_LEAF_DIR_NAME = "gms_test_data_set";
  private static final String SEGMENTS_AND_SOH_DIR_NAME = "segments-and-soh/";

  private static final CssBatchFileWriterCommandLineArgs cmdLineArgs
          = new CssBatchFileWriterCommandLineArgs();

  public static void main(String[] args) {
    try {
      // Read command line args
      CmdLineParser parser = new CmdLineParser(cmdLineArgs);
      parser.parseArgument(args);
      boolean validated = validateArgs(parser, cmdLineArgs);
      //Check Prams for validity
      if (!validated) {
        logger.error("Invalid command-line argument(s) received.");
        System.exit(1);
      }
      else{
        //Check if outputDirectory exists
        if(!cmdLineArgs.getOutputDir().endsWith(OUTPUT_LEAF_DIR_NAME)){
          throw new IllegalArgumentException(String.format("Data must be output to directory named %s. " +
                  "Arg provided was %s", OUTPUT_LEAF_DIR_NAME, cmdLineArgs.getOutputDir()));
        }
        //All checks passed, can convert waveforms now
        else{
          int exit = execute(cmdLineArgs);
          System.exit(exit);
        }
      }

    } catch (Exception ex) {
      logger.error("Error in Application.execute", ex);
      System.exit(1);
    }
  }

  /**
   * Performs the load.  Implemented as an instance method since the fields that are annotated with
   *
   * @param args command-line args
   * @return exit code is number of waveforms loaded, and value less than 0 is error
   * @Option can be instance also.
   */
  private static int execute(CssBatchFileWriterCommandLineArgs args) {
    Objects.requireNonNull(args, "Cannot take null arguments");
    //Check if the directory already exists, refuse to write new files if it does, for security
    String outputDirString = args.getOutputDir();
    if (!outputDirString.endsWith(File.separator)) {
      outputDirString += File.separator;
    }
    // add 'segments-and-soh' folder to end of output directory
    String segAndSohOutputDir = outputDirString + SEGMENTS_AND_SOH_DIR_NAME;
    if(new File(segAndSohOutputDir).exists()){
      throw new IllegalArgumentException(String.format("Cannot create sub directory %s " +
              "in base directory %s. %s already exists. Please specify a new base " +
              "directory or remove the sub directory", SEGMENTS_AND_SOH_DIR_NAME,
              outputDirString, segAndSohOutputDir));
    }
    else{
      try{
        new File(segAndSohOutputDir).mkdir();
        boolean includeValues = cmdLineArgs.getIncludeSamples();

        // Get arguments and convert into proper formats.
        List<String> stationList = null;
        List<String> channelList = null;
        Instant time = null;
        Instant endtime = null;

        String stationsArg = cmdLineArgs.getStations();
        if (stationsArg != null && stationsArg.length() > 0) {
          stationList = Arrays.asList(stationsArg.trim().split(","));
        }
        String channelsArg = cmdLineArgs.getChannels();
        if (channelsArg != null && channelsArg.length() > 0) {
          channelList = Arrays.asList(channelsArg.trim().split(","));
        }
        long timeEpochArg = cmdLineArgs.getTimeEpoch();
        String timeDateArg = cmdLineArgs.getTimeDate();
        if (timeEpochArg > -1) {
          time = Instant.ofEpochSecond(timeEpochArg);
        } else if (timeDateArg != null && timeDateArg.length() > 0) {
          time = Instant.parse(timeDateArg);
        }
        long endtimeEpochArg = cmdLineArgs.getEndtimeEpoch();
        String endtimeDateArg = cmdLineArgs.getEndtimeDate();
        if (endtimeEpochArg > -1) {
          endtime = Instant.ofEpochSecond(endtimeEpochArg);
        } else if (endtimeDateArg != null && endtimeDateArg.length() > 0) {
          endtime = Instant.parse(endtimeDateArg);
        }

        // Load the WF Disc file.
        CssWfdiscReader cssWfdiscReader = new CssWfdiscReader(
                cmdLineArgs.getWfdiscFile(), cmdLineArgs.getChannelsFile(),
                cmdLineArgs.getBatchSize(), stationList, channelList, time, endtime,
                includeValues);

        // check if anything was loaded.  If not, log a warning and exit.
        if (cssWfdiscReader.size() == 0) {
          logger.warn("No records loaded; exiting");
          return 0;
        }

        final ObjectMapper objMapper = CoiObjectMapperFactory.getJsonObjectMapper();
        int batchNumber = 1;
        final Set<AcquiredChannelSohBoolean> sohs = new HashSet<>();
        final Map<UUID, WfdiscSampleReference> idToWs = new HashMap<>();
        while (cssWfdiscReader.nextBatchExists()) {
          final SegmentAndSohBatch batch = cssWfdiscReader.readNextBatch();
          sohs.addAll(batch.getSohs());
          if(!includeValues){
            idToWs.putAll(batch.getIdToW());
          }
          if (batch.getSegments().isEmpty()) {
            continue;
          }
          // write the segment batch to an output file
          objMapper.writeValue(createFile(segAndSohOutputDir + "segments-" + batchNumber++ + ".json"),
                  batch.getSegments());
        }
        // write the SOH's into a single JSON file
        objMapper.writeValue(createFile(segAndSohOutputDir + "state-of-health.json"), sohs);
        //write associations between channel segment ids and w files
        if(!includeValues){
          objMapper.writeValue(createFile(segAndSohOutputDir + "chan-seg-id-to-w.json"), idToWs);
        }

        logger.info("Processed " + cssWfdiscReader.size() + " wfdisc records");
        return 0;
      } catch (Exception e) {
        logger.error("Failed to execute:", e);
        return -1;
      }
    }
  }

  /**
   * Validate arguments, then print usage and exit if found any problems.
   *
   * @param parser the command-line arguments parser
   * @return true = arguments validated, false = there is an error.
   */
  private static boolean validateArgs(CmdLineParser parser,
      CssBatchFileWriterCommandLineArgs cmdLineArgs) {
    if (cmdLineArgs.getBatchSize() < 1) {
      printUsage(parser, "The batchSize value must be greater than zero.");
      return false;
    }
    if ((cmdLineArgs.getTimeEpoch() > -1) &&
        (cmdLineArgs.getTimeDate() != null && cmdLineArgs.getTimeDate().length() > 0)) {
      printUsage(parser, "Cannot use both timeEpoch and timeDate in same call");
      return false;
    }
    if ((cmdLineArgs.getEndtimeEpoch() > -1) &&
        (cmdLineArgs.getEndtimeDate() != null && cmdLineArgs.getEndtimeDate().length() > 0)) {
      printUsage(parser, "Cannot use both endtimeEpoch and endtimeDate in same call");
      return false;
    }

    if (cmdLineArgs.getTimeDate() != null && cmdLineArgs.getTimeDate().length() > 0) {
      try {
        Instant.parse(cmdLineArgs.getTimeDate());
      } catch (Exception e) {
        printUsage(parser, "Invalid format for timeDate: " + e.getLocalizedMessage());
        return false;
      }
    }

    if (cmdLineArgs.getEndtimeDate() != null && cmdLineArgs.getEndtimeDate().length() > 0) {
      try {
        Instant.parse(cmdLineArgs.getEndtimeDate());
      } catch (Exception e) {
        printUsage(parser, "Invalid format for endtimeDate: " + e.getLocalizedMessage());
        return false;
      }
    }

    String stations = cmdLineArgs.getStations();
    if (stations != null && stations.length() > 0) {
      if ((stations.length() > 6) && (stations.indexOf(",") <= 0)) {
        logger.warn("'stations' is unusually long without any commas, "
            + "but assuming user knows what they are doing and continuing anyaways.");
      }
    }
    String channels = cmdLineArgs.getChannels();
    if (channels != null) {
      if ((channels.length() > 8) && (channels.indexOf(",") <= 0)) {
        logger.warn("'channels' is unusually long without any commas, "
            + "but assuming user knows what they are doing and continuing anyways.");
      }
    }
    return true;
  }

  /**
   * Prints out usage information for this application.
   *
   * @param parser the command-line arguments parser
   * @param msg the error msg
   */
  private static void printUsage(CmdLineParser parser, String msg) {
    logger.error(msg);

    System.err.println(System.lineSeparator() +
        "Error - invalid argument: " + msg +
        System.lineSeparator());
    System.err.print("Usage: java " + CssWaveformFileWriter.class + " ");
    parser.printSingleLineUsage(System.err);
    System.err.println();
    parser.printUsage(System.err);
  }

  private static File createFile(String path) throws IOException {
    logger.info("Creating file " + path);
    final File f = new File(path);
    if (!f.createNewFile()) {
      throw new RuntimeException("Could not create file " + path);
    }
    return f;
  }
}
