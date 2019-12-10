package gms.shared.utilities.geotess.util.globals;

import static gms.shared.utilities.geotess.util.globals.Globals.NL;
import gms.shared.utilities.geotess.util.logmanager.ScreenWriterOutput;
import gms.shared.utilities.geotess.util.propertiesplus.PropertiesPlus;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.nio.channels.FileChannel;

/**
 * Static functions used to manipulate driver application property files
 * and paths. These functions create, delete, copy, and manipulate
 * file/directories.
 * 
 * @author jrhipp
 *
 */
@SuppressWarnings("serial")
public class FileDirHandler implements Serializable
{
  /**
   * File buffer copy size.
   */
  private static int         aCopyBufferSize = 1 * 1024 * 1024;

  /** Set the file buffer copy size to cbs MB.
   * 
   * @param cbs The new file buffer copy size (in MB).
   */
  public static void setCopyBufferSize(int cbs)
  {
    aCopyBufferSize = cbs * 1024 * 1024;
  }

  /**
   * Returns the ending directory name extension of the form "_#" if one
   * exists. If not "" is returned.
   * 
   * @param dir The input directory name for which the ending extension will
   *            be returned. If one is not found "" is returned.
   * @return The ending extension of the input directory (dir) or "" if one is
   *         not found.
   */
  public static String directoryExtension(String dir)
  {
    // initialize the extension to "" and search for the last "_"

    String ext = "";
    int usPos = dir.lastIndexOf("_");

    // if "_" exists then assign extension ... return the result

    if ((usPos >= 0) && (usPos < dir.length())) ext = dir.substring(usPos);
    return ext;
  }

  /**
   * Finds the first occurrence of a file with the input extension. If none
   * are found an empty string is returned. If more than one are present the
   * first one discovered is returned.
   * 
   * @param inFile The input path to search for a file with extension ext.
   * @param ext The extension of the file to return from the input
   *            directory.
   * @return The path/file if found or empty ("") if not.
   */
  public static String findFile(String inFile, String ext)
  {
    File f = new File(inFile);
    String[] files = f.list();
    if (files != null)
    {
      for (String ff: files)
      {
        if (ff.substring(ff.lastIndexOf(".")+1).equalsIgnoreCase(ext))
        {
          inFile += File.separator + ff;
          return inFile;
        }
      }
    }

    return "";
  }

  /**
   * Finds the first non-existing directory, which may be "startDir", or
   * "startDir_#", where # is some integer 1 to filNumLimit; and returns the
   * result to the caller. The input "startPath" is prepended to "startDir"
   * with a file separator unless it is null or empty.
   * 
   * @param startPath The starting path to the first non-existing directory
   *                  to be discovered.
   * @param startDir  The starting guess for the first non-existing directory
   *                  to be discovered.
   * @param filNumLimit The maximum allowed file name number limit.
   * @return The first discovered non-existing directory startDir_#
   */
  public static String newDirectory(String startPath, String startDir,
                                    int filNumLimit)
  {
    File outDir;
    String icd;

    // if startPath is null or empty just use startDir

    if ((startPath == null) || (startPath.equals("")))
      icd = startDir;
    else
      icd = startPath + File.separator + startDir;

    // if directory does not exist use startDir as directory

    outDir = new File(icd);
    if (!outDir.exists()) return startDir;

    // if icd ends in "_0" remove it so name doesn't end in "_0_#"

    if (icd.substring(icd.length() - 2).equals("_0"))
      icd = icd.substring(0, icd.length() - 2);

    // otherwise find the first "_#" directory that does not exist and use it

    String newICD = icd;
    for (int i = 1; i < filNumLimit; i++)
    {
      // create new directory name and see if it exists

      newICD = icd + "_" + i;
      outDir = new File(newICD);

      // if directory does not exist return startDir_# as result.

      if (!outDir.exists()) return icd + "_" + i;
    }

    // exceeded filNumLimit directories ... return empty string

    return "";
  }

  /**
   * Creates the directory defined by outputLogFilePath and copies the
   * input properties file into that directory.
   * 
   * @param outputLogFilePath The output file path directory to be created.
   * @param propertiesFileName The name of the input properties file that
   *                           will be copied into the directory.
   * @throws IOException
   */
  public static void createOutDir(String outputLogFilePath,
                                  String propertiesFileName)
                     throws IOException
  {
    // create the output file directory

    createDirectory(outputLogFilePath);

    // ensure any leading path information is stripped from the input
    // properties file name

    String outPropertiesFileName = propertiesFileName;
    int index = outPropertiesFileName.lastIndexOf("/");
    if (index >= 0)
      outPropertiesFileName = outPropertiesFileName.substring(index);
    index = outPropertiesFileName.lastIndexOf("\\");
    if (index >= 0)
      outPropertiesFileName = outPropertiesFileName.substring(index);

    // copy the input properties file to the output directory

    copyFile(propertiesFileName,
             outputLogFilePath + File.separator + outPropertiesFileName);
  }

  /**
   * Creates the input directory dirPath. The function throws a
   * IOException if the path already exists or could not be
   * created.
   * 
   * @param dirPath The directory to be created.
   * @throws IOException
   */
  public static void createDirectory(String dirPath)
                throws IOException
  {
    // make a File object using the directory path

    File aDir = new File(dirPath);

    // throw an error if the path exists

    if (aDir.exists())
    {
      String s = "Output directory exists ... aborting run ..." + NL +
                 "    \"" + dirPath + "\"";
      throw new IOException(s);
    }
    else
    {
      // else create the directory ... throw an error if the directory could
      // not be created

      if (!aDir.mkdirs())
      {
        String s = "Error Creating Output Directory ... aborting run ..." + NL +
                   "    \"" + dirPath + "\"";
        throw new IOException(s);
      }
    }
  }

  /**
   * Copy file from inFilePath to outFilePath, overwriting the destination
   * file if it exists.
   *
   * @param inFilePath
   *            input file
   * @param outFilePath
   *            output file
   * @return true on success
   * @throws IOException
   */
  public static File copyFile(String inFilePath, String outFilePath)
                     throws IOException
  {
    FileChannel source = null;
    FileChannel destination = null;

    // throw error and return null if inFilePath or outFilePath is not
    // defined

    if (inFilePath == null)
      throw new IOException("Null input file path \"inFilePath\"");
    if (outFilePath == null)
      throw new IOException("Null output file path \"outFilePath\"");

    // create inFile and outFile

    File inFile = new File(inFilePath);
    File outFile = new File(outFilePath);
    FileInputStream fis = null;
    FileOutputStream fos = null;

    // error if input file does not exist

    if (!inFile.isFile())
      throw new IOException("Input file does not exist: " + inFilePath);

    // create output file, or delete it if it already exists

    if (!outFile.exists())
    {
      try
      {
        outFile.createNewFile();
      }
      catch (IOException e)
      {
        throw new IOException("Failed to create output file: " + outFilePath, e);
      }
    }
    else if (!outFile.delete())
    {
      throw new IOException("Failed to delete existing output file: " + outFilePath);
    }

    // transfer from input to output file

    try
    {
      fis = new FileInputStream(inFile);
      source = fis.getChannel();
      fos = new FileOutputStream(outFile);
      destination = fos.getChannel();

      // to avoid "insufficient resource" exceptions when copying large files,
      // must break up the transfer into smaller chunks. A Google search
      // recommended 1 MByte as a reasonable transfer size (20 works better).

      int position = 0;
      while (position < source.size())
      {
        destination.transferFrom(source, position, aCopyBufferSize);
        position += aCopyBufferSize;
      }
    }
    catch (IOException ex)
    {
      throw new IOException("Exception occurred while attempting" +
                            "to copy input file ..." + NL +
                            "(" + inFilePath + ")" + NL +
                            "... to output file ..." + NL +
                            "(" + outFilePath + ")", ex);
    }
    finally
    {
      try
      {
    	if (fis != null) fis.close();
        if (source != null) source.close();
        
        if (fos != null) fos.close();
        if (destination != null) destination.close();      
      }
      catch (IOException e)
      {
        // ignore exception on close
      }
    }

    return new File(outFilePath);
  }

  /**
   * Defines, initializes, and sets the "ioDirectory" property.
   * 
   * @param basePath The input IO directory base path.
   * @param props    The properties file.
   * @param scrnWrtr The screen writer
   * @param appnd    Opens to log file for append if true.
   * @return         The new "ioDirectory" path.
   * @throws IOException
   */
  public static String initializeDirectory(String basePath,
                                           PropertiesPlus props,
                                           ScreenWriterOutput scrnWrtr,
                                           boolean... appnd)
                throws IOException
  {
    return initializeDirectory(basePath, "", props, scrnWrtr, appnd);
  }

  /**
   * Defines, initializes, and sets the "ioDirectory" property.
   * 
   * @param basePath The input IO directory base path.
   * @param props    The properties file.
   * @param scrnWrtr The screen writer
   * @param appnd    Opens to log file for append if true.
   * @return         The new "ioDirectory" path.
   * @throws IOException
   */
  public static String initializeDirectory(String basePath,
  		                                     String subPath,
                                           PropertiesPlus props,
                                           ScreenWriterOutput scrnWrtr,
                                           boolean... appnd)
                throws IOException
  {
    String s;

    // define the primary output directory ioDirectory as read from the
    // properties file

    String ioDirectory = props.getPropertyPath("ioDirectory", "").trim();
    ioDirectory = prependDate(ioDirectory);

    // see if it is new or if a number extension is required to avoid
    // overwriting an existing directory of that name

    String newDir = FileDirHandler.newDirectory(basePath, ioDirectory, 1000);
    if (newDir.equals(""))
    {
      s = NL + "  Could not create a new directory: " + ioDirectory + "_#" +
          NL + "# exceeded 1000 limit ..." + NL;
      throw new IOException(s);
    }
    if (!newDir.equals(ioDirectory))
    {
      // found new iodirectory ... set it

      System.out.println("Changing ioDirectory because the ioDirectory " +
                         "already exists: " + ioDirectory);
      System.out.println("Setting new ioDirectory to: " + newDir);
    }

    // create output dir(s) and place a copy of the properties file in the
    // output dir

    if (!basePath.equals("") && (newDir.indexOf(basePath,  0) == -1))
      newDir = basePath + File.separator + newDir;
    if (!subPath.equals(""))
    	newDir += File.separator + subPath;
    props.setProperty("ioDirectory", newDir);
    ioDirectory = newDir;
    FileDirHandler.createOutDir(ioDirectory, props.getProperty("propertiesFileName"));

    // create output file for screen writer if scrnWrtr is defined

    if (scrnWrtr != null)
    {
      FileDirHandler.createOutputLogFileWriter(ioDirectory, scrnWrtr, appnd);
      setOutputWriterMode(props, scrnWrtr);
    }

    return ioDirectory;
  }

  /**
   * Prepends the current date string "yyyy_MM_dd" to the front of the
   * input string replacing the occurrence "(DATE)" if it is present.
   * Otherwise the input string is simply returned.
   * 
   * @param inFName The input string for which "(DATE)" will be replaced
   *                with the current date.
   * @return The new input string with "(DATE)" replaced with the current
   *         date string.
   */
  public static String prependDate(String inFName)
  {
    // see if "(DATE)" is present

    int k = inFName.toUpperCase().lastIndexOf("(DATE)");
    if (k > -1)
    {
      // get current date string and replace with current date

      String DATE_FORMAT = "yyyy_MM_dd";
      inFName = inFName.substring(0, k) +
                Globals.getTimeStamp(DATE_FORMAT) +
                inFName.substring(k + 6);
    }

    // return modified (possibly) input string

    return inFName;
  }

  /**
   * Sets the screen/file output modes for the GeoTomography, IODB, and LSQR
   * writers based on the output model property
   * 
   * @param props The properties object from which the "outputMode" is read.
   * @param scrnWrtr The ScreenWriterOutput object whose output mode is set.
   * 
   * @throws IOException
   */
  public static void setOutputWriterMode(PropertiesPlus props,
                                         ScreenWriterOutput scrnWrtr)
                throws IOException
  {
    String outputMode = props.getProperty("outputMode", "").trim()
                             .toLowerCase();
    if (outputMode.equalsIgnoreCase("none"))        // turn off all output
      scrnWrtr.setOutputOff();
    else if (outputMode.equalsIgnoreCase("screen")) // turn on screen output only
    {
      scrnWrtr.setScreenOutputOn();
      scrnWrtr.setWriterOutputOff();
    }
    else if (outputMode.equalsIgnoreCase("file"))   // turn on file output only
    {
      scrnWrtr.setWriterOutputOn();
      scrnWrtr.setScreenOutputOff();
    }
    else // default to both on                      // turn on file and screen output
    {
      scrnWrtr.setScreenAndWriterOutputOn();
    }
  }

  /**
   * Creates the output file "out.txt" in the directory outPath. The output
   * file is assigned to the input ScreenWriter object scrnWrtr. 
   * 
   * @param outPath The output file path where the "out.txt" file will be
   *                written.
   * @paraM scrnWrtr The ScreenWriterOutput object that will write to the
   *                 new "out.txt" file.
   * @param appnd If true append to an existing file.
   * 
   * @throws IOException
   */
  public static void createOutputLogFileWriter(String outPath,
                                               ScreenWriterOutput scrnWrtr,
                                               boolean... appnd)
                     throws IOException
  {
    boolean ap = false;
    if (appnd.length > 0) ap = appnd[0];

    // create output log file writer

    File outFile = new File(outPath + File.separator + "out.txt");
    if (outFile.exists())
    {
      if (!ap)
      {
        outFile.delete();
        outFile.createNewFile();
      }
    }

    // assign the file to a buffered writer and set into aScrnWrtr

    FileWriter fw = null;
    if (ap)
      fw = new FileWriter(outFile, ap);
    else
      fw = new FileWriter(outFile);
    BufferedWriter outFileWriter = new BufferedWriter(fw);
    scrnWrtr.setWriter(outFileWriter);
  }

  /**
   * Copy an entire directory to a new location under the given
   * parent archive directory. This is used to copy results from a temporary
   * location to a stable, backed up location. For example, copying
   * results from "eel" to "thummper".
   * 
   * @param fromDir directory to copy over
   * @param newParentDir parent directory to put a copy of fromDir underneath
   * @return true on success, false on failure
   * @throws IOException
   */
  public static boolean copyDirToArchive(File fromDir, File newParentDir) {
  
    if(fromDir.getParent().equals(newParentDir.getPath())) {
  	  System.err.println("Error in copyDirToArchive(): The source "
  			  + "directory already exists under the specified "
  			  + " new parent directory. Source = " 
  			  + fromDir.getPath() + ", New Parent = "
  			  + newParentDir.getPath());
  	  return false;
    }
  
    if(newParentDir.getPath().trim().length() == 0) {
  	  System.err.println("Error in copyDirToArchive(): The archive "
  			  + "directory path is empty.");
  	  return false;
    }
  
    // target is a directory or file with the same name as fromDir, but
    // copied underneath newParentDir
  
    File target = new File(newParentDir, fromDir.getName());
    if (fromDir.isDirectory()) {
  	  // copy directory
  
  	  // create parent and target directories if they do not exist
  	  if (!newParentDir.exists()) {
  		  System.out.println("Creating archive parent directory: " 
  				  + newParentDir.getPath());
  		  if(!newParentDir.mkdirs()) {
  			  System.err.println("Error in creating parent directory.  Aborting archive...");
  			  return false; 
  		  }
  	  }
  	  
  	  if (!target.exists()) {
  		  System.out.println("Creating archive target directory: " 
  				  + target.getPath());
  		  if(!target.mkdirs()) {
  			  System.err.println("Error in creating target directory.  Aborting archive...");
  			  return false; 
  		  }
  	  }
  	  
  	  // copy each item under fromDir recursively
  	  String[] children = fromDir.list();
  	  for (int i=0; i < children.length; i++) {
  		  copyDirToArchive(new File(fromDir, children[i]),
  				  target);
  	  }
    } 
    else {
  	  // copy file
  
  	  System.out.println("Copying file (" + fromDir.getPath() 
  			  + ") to archive (" + target.getPath() + ")");
  	  FileInputStream in = null;
  	  FileOutputStream out = null;
  	  try {
  		  in = new FileInputStream(fromDir);
  		  out = new FileOutputStream(target);
  	  } catch (FileNotFoundException e) {
  		  System.err.println("Error in copyDirToArchive(): File not found exception. "
  				  + e.getMessage());
  		  e.printStackTrace();
  		  return false;
  	  }
  	  byte[] buf = new byte[1024];
  	  int len;
  	  try {
  		  while ((len = in.read(buf)) > 0) {
  			  out.write(buf, 0, len);
  		  }
  		  in.close();
  		  out.close();
  	  } 
  	  catch (IOException e) {
  		  System.err.println("Error in copyDirToArchive(): "
  				  + "IOException during file copy. "
  				  + e.getMessage());
  				  e.printStackTrace();
  		  return false;
  	  }
    }
    return true;
  }

  /**
   * Recursive file/directory delete function. If the input directory is a file
   * it is simply deleted and success or failure (true or false) is returned to
   * the caller. If it is a directory then all of its content files are
   * deleted and any content directories are recursively deleted by recalling
   * this function for each. If a delete fails an IOException is thrown.
   * 
   * @param ddir The input file/directory to be recursively deleted.
   * @return True if delete was successful.
   * 
   * @throws IOException
   */
  public static boolean deleteRecursive(File ddir) throws IOException
  {
    // see if the directory/file exists

    if (ddir.exists())
    {
      // it exists ... see if it is just a file ... if so then delete it and
      // return success or failure

      if (ddir.isFile())
        return ddir.delete();
      else
      {
        // ddir is a directory ... get all of its directory/file contents and
        // recursively delete each

        File[] files = ddir.listFiles();
        for (File f: files)
        {
          // recursively delete File f ... if it is a directory this will
          // delete all of its files and directories. If the delete is
          // unsuccessful then an IOException is thrown.

          if (!deleteRecursive(f))
          {
            throw new IOException("Error: Could not delete: " + f);
          }
        }
      }
    }

    // ddir deleted successfully ... return true

    return true;
  }
  
  /**
   * Extract a filename from filepathname, with possible extension
   * @param filePathName
   * @return
   */
  public static String extractFileName( String filePathName )
  {
	  if ( filePathName == null ) return null;

	  int dotPos = filePathName.lastIndexOf(".");
	  int slashPos = filePathName.lastIndexOf(File.separator);
    
	  if (dotPos > slashPos)
	  {
		  return filePathName.substring( slashPos > 0 ? slashPos + 1 : 0, dotPos );
	  }

	  return filePathName.substring( slashPos > 0 ? slashPos + 1 : 0 );
  }

  
}
