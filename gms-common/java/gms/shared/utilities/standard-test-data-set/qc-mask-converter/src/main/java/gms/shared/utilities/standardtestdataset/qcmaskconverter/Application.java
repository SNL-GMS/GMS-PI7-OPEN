package gms.shared.utilities.standardtestdataset.qcmaskconverter;

import com.fasterxml.jackson.databind.ObjectMapper;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.datatransferobjects.CoiObjectMapperFactory;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMask;
import gms.shared.utilities.standardtestdataset.qcmaskconverter.commandline.QcMaskConverterCommandLineArgs;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Objects;
import org.kohsuke.args4j.CmdLineParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Application {

  private static final Logger logger = LoggerFactory.getLogger(Application.class);
  private static final ArrayList<QcMask> successfullyConvertedQcMasks = new ArrayList<>();
  private static final QcMaskConverterCommandLineArgs cmdLineArgs = new QcMaskConverterCommandLineArgs();
  private static final String OUTPUT_LEAF_DIR_NAME = "gms_test_data_set";
  private static final String OUTPUT_QC_MASK_FILE_NAME = "converted-qc-masks.json";
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
        boolean exit = execute(cmdLineArgs);
        System.exit(exit ? 0 : 1);
      }
    } catch (Exception ex) {
      logger.error("Error in Application.execute", ex);
      System.exit(1);
    }
  }

  /**
   * Reads all the files in the input directory(qcDir) and calls convertJsonToCOI on each file to
   * create a single output file containing all the input JSONS converted to GMS COI formatted JSONS
   * representing Qc Masks
   *
   * @param args command-line args
   * @return true upon successful conversion and write to JSON file - doesn't have to be successful
   * for every sub JSON
   */
  private static boolean execute(QcMaskConverterCommandLineArgs args) {
    Objects.requireNonNull(args, "Cannot take null arguments");
    //Check if the directory already exists, refuse to write new files if it does, for security
    String outputDirString = args.getOutputDir();
    if (!outputDirString.endsWith(File.separator)) {
      outputDirString += File.separator;
    }
    String outputQcMaskString = outputDirString + OUTPUT_QC_MASK_FILE_NAME;
    if (new File(outputQcMaskString).exists()) {
      throw new IllegalArgumentException(String.format("Cannot create %s " +
                      "in output directory %s, file already exists. Please specify a new output " +
                      "directory or remove the file", outputQcMaskString,
              outputDirString));
    } else {
      try {
        //Iterate through every file in directory, for each parse all the sub jsons, add them
        //to the global arraylist of qcmasks
        String qcDirArg = cmdLineArgs.getQcDir();
        File qcDir = new File(qcDirArg);
        if (!qcDir.exists()) {
          logger.error("qcDir does not exist");
          return false;
        }
        for (final File inputJsonFile : Objects.requireNonNull(qcDir.listFiles())) {
          final QcMaskConverter converter = new QcMaskConverter(qcDirArg + inputJsonFile.getName(),
                  cmdLineArgs.getChannelsFile());
          successfullyConvertedQcMasks.addAll(converter.getConvertedMasks());
        }

        //Now take the QC mask list which contains all the subj sons from every file in the
        //directory and write it to file
        logger.info(String.format("Writing into %s.", outputQcMaskString));
        final byte[] coiJson = objMapper.writeValueAsBytes(successfullyConvertedQcMasks);
        Files.write(Paths.get(outputQcMaskString), coiJson, StandardOpenOption.CREATE);
      } catch (Exception e) {
        logger.error("Error in writing to JSON: ", e);
        return false;
      }
    }
    return true;
  }
}
