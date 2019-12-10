package gms.shared.utilities.standardtestdataset.filterdefinitionvalidator;

import gms.shared.utilities.standardtestdataset.filterdefinitionvalidator.commandline.FilterDefinitionValidatorCommandLineArgs;
import java.io.File;
import org.kohsuke.args4j.CmdLineParser;

public class Application {
  
  public static void main(String[] args) {
    boolean success = execute(args);
    System.exit(success ? 0 : -1);
  }

  /**
   * Reads the smeware json file and validate that it matches COI format
   *
   * @param args command-line args
   * @return true upon successful validation
   */
  private static boolean execute(String[] args) {
    try {
      final FilterDefinitionValidatorCommandLineArgs cmdLineArgs = new FilterDefinitionValidatorCommandLineArgs();
      new CmdLineParser(cmdLineArgs).parseArgument(args);

      String filterDefFileArg = cmdLineArgs.getFilterDefFile();
      File filterDefFile = new File(filterDefFileArg);
      if (!filterDefFile.exists()) {
        System.out.println("filterDefFile does not exist");
        return false;
      }
      final FilterDefinitionValidator filterDefinitionValidator = new FilterDefinitionValidator(filterDefFile);

    } catch (Exception e) {
      System.out.println("Error validation JSON: " + e);
      return false;
    }
    return true;
  }
}

