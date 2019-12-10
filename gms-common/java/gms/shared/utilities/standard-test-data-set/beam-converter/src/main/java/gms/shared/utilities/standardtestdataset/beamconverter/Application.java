package gms.shared.utilities.standardtestdataset.beamconverter;

import com.fasterxml.jackson.databind.ObjectMapper;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.datatransferobjects.CoiObjectMapperFactory;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.BeamDefinition;
import gms.shared.utilities.standardtestdataset.beamconverter.commandline.BeamConverterCommandLineArgs;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Objects;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kohsuke.args4j.CmdLineParser;

public class Application {

  private static final Logger logger = LogManager.getLogger(Application.class);
  private static final ArrayList<BeamDefinition> successfullyConvertedBeamDefinitions = new ArrayList<>();
  private static final BeamConverterCommandLineArgs cmdLineArgs = new BeamConverterCommandLineArgs();
  private static final String OUTPUT_LEAF_DIR_NAME = "gms_test_data_set";
  private static final String OUTPUT_BEAM_FILE_NAME = "converted-beam-definitions.json";
  public static final ObjectMapper objectMapper = CoiObjectMapperFactory.getJsonObjectMapper();

  public static void main(String[] args) {
    try {
      // Read command line args
      new CmdLineParser(cmdLineArgs).parseArgument(args);
      if(!cmdLineArgs.getOutputDir().endsWith(OUTPUT_LEAF_DIR_NAME)){
        throw new IllegalArgumentException(String.format("Data must be output to directory named %s. " +
                "Arg provided was %s", OUTPUT_LEAF_DIR_NAME, cmdLineArgs.getOutputDir()));
      }
      else{
        boolean exit = execute(cmdLineArgs);
        System.exit(exit ? 0 : 1);
      }
    } catch (Exception ex) {
      logger.error("Error in Application.execute", ex);
      System.exit(1);
    }
  }

  /**
   * Reads the Beam Definition file and calls convertJsonToCOI on that file to create an output file
   * containing the input JSON that's been converted to GMS COI formatted JSONS representing Beam
   * Definitions
   *
   * @param args command-line args
   * @return true upon successful conversion and write to JSON file - doesn't have to be successful
   * for every sub JSON
   */
  private static boolean execute(BeamConverterCommandLineArgs args) {
    Objects.requireNonNull(args, "Cannot take null arguments");
    //Check if the directory already exists, refuse to write new files if it does, for security
    String outputDirString = args.getOutputDir();
    if (!outputDirString.endsWith(File.separator)) {
      outputDirString += File.separator;
    }
    String outputBeamString = outputDirString + OUTPUT_BEAM_FILE_NAME;
    if (new File(outputBeamString).exists()) {
      throw new IllegalArgumentException(String.format("Cannot create %s " +
                      "in output directory %s, file already exists. Please specify a new output " +
                      "directory or remove the file", outputBeamString,
              outputDirString));
    } else {
      try {
        /** Iterate through every file in the directory, for each parse out the json from the Beam
         * Definition file and add it to the global arraylist  of Beam Definitions
         */
        String beamDefnDirArg = args.getBeamDefinitionDir();
        File beamDefinitionDir = new File(beamDefnDirArg);

        if (!beamDefinitionDir.exists()) {
          logger.error("beamDefinitionDir does not exist");
          return false;
        }
        for (final File inputJsonFile : Objects.requireNonNull(beamDefinitionDir.listFiles())) {
          final BeamConverter converter = new BeamConverter(beamDefnDirArg + inputJsonFile.getName());
          successfullyConvertedBeamDefinitions.addAll(converter.getConvertedBeams());
        }
        logger.info(String.format("Writing into %s.", outputBeamString));
        final byte[] coiJson = objectMapper.writeValueAsBytes(successfullyConvertedBeamDefinitions);
        Files.write(Paths.get(outputBeamString), coiJson, StandardOpenOption.CREATE);

      } catch (Exception e) {
        logger.error("Error in writing to JSON: ", e);
        return false;
      }
      return true;
    }
  }
}

