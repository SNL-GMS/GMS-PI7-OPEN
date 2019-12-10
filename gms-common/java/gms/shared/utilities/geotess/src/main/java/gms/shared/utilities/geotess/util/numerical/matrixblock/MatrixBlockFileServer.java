package gms.shared.utilities.geotess.util.numerical.matrixblock;

import static gms.shared.utilities.geotess.util.globals.Globals.NL;
import gms.shared.utilities.geotess.util.filebuffer.FileInputBuffer;
import gms.shared.utilities.geotess.util.filebuffer.FileOutputBuffer;
import gms.shared.utilities.geotess.util.globals.FileDirHandler;
import gms.shared.utilities.geotess.util.globals.Globals;
import gms.shared.utilities.geotess.util.propertiesplus.PropertiesPlus;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;

/**
 * Stores a set of file server paths that act as a distributed network where
 * a subset of LSINV files (mostly matrix blocks) are stored in a sub-directory
 * defined by a set of secondary file paths. Specific matrix blocks are stored
 * on specific file servers according to a block map. The 3 containers defined
 * above are outlined below:
 * 
 * 1) ArrayList<String> aServers
 * 
 *    A list containing each unique server path to a primary IODirectory.
 *    As many as desired can be defined or none may be defined (secondary paths
 *    then define the layout).
 * 
 * 2) HashMap<String, String> aSecondaryFilePath
 * 
 *    Holds a set of sub-directories defined in each server in the list aServers.
 *    The sub directories are defined as a mapping from a short tag name that
 *    is mapped to the actual directory name. The tag names for LSINV tend to
 *    be the matrix types it solves for. These include chol, fsub, ginv, covp,
 *    cov, res, and roe, and input matrix types gtg, ata, and atsa. Their
 *    corresponding mappings may be empty or simply have the type extension,
 *    (e.g. chol) or it they may be defined within in a run directory (e.g.
 *    run_5/fsub). Any sub-directory path extensions are allowed. During
 *    operation the secondary paths are appended when the short tag name is
 *    requested.
 * 
 * 3) int[] aBlockServerMap
 * 
 *    A cyclic mapping of arbitrarily ordered servers indices (indexes into the
 *    server list aServers) stored in block index order
 *    
 *        aBlockServerMap[block_index] = server_index
 *        
 *    The block index is a standard ordering of a lower-triangular matrix in
 *    LSINV given by the specification (defined in MatrixBlock)
 *    
 *        block_index = = row * (row + 1) / 2 + column;
 *        
 *    where column <= row and row and column represent a block matrix row and
 *    column index in the total matrix of which they are a partial container.
 *    
 *    The block index is cyclic in the sense that it only represents a small
 *    number of blocks which is generally much less than the total number of
 *    blocks defined in the LSINV matrix. Block indices that exceed the map
 *    length use the remainder function to find their server from the expression
 *    
 *       aBlockServerMap[block_index % aBlockServerMap.length] 
 *    
 *    The block server map is built in one of 4 ways. First (the primary method)
 *    A server fraction prescription is defined that specifies the fraction of
 *    the total matrix blocks that should be assigned to each server. For
 *    example, if 3 servers are defined, say Sa, Sb, and Sc. and Sa is known to
 *    be twice as fast as Sb and Sc then the server fractions 0.5, 0.25, 0.25
 *    might be assigned to Sa, Sb, and Sc, respectively. With this definition
 *    half of the blocks are stored on Sa, while Sb and Sc each get 25% of the
 *    blocks. When the block server map is defined this way a map of up to 100
 *    entries is created with the approximate fraction of entries for each
 *    server appearing as entries in the map dispersed in a regular fashion
 *    across the map. If a map with fewer than 100 entries can be constructed
 *    such that each server fraction is an integer multiple of the number of
 *    entries it is chosen instead of 100.
 *    
 *    The second way the map is defined is by stencil. This can reduce the size
 *    of the map and is particularly useful for whole fractions as in the above
 *    case. Using this method the actual stencil is the map and to support the
 *    fractions in the previous example the following stencil might be defined
 *    
 *       [Sa, Sb, Sa, Sc]
 *       
 *    Notice that Sa appears twice as often as Sb and Sc so that it will get
 *    half of all of the blocks while Sb and Sc will only get 25%.
 *    
 *    Finally, if no specification is defined for the block map then the number
 *    of servers is simply divided into an equal stencil. Using the 3 servers
 *    above the stencil would be defined as
 *    
 *       [Sa, Sb, Sc]
 *       
 *    where each server gets an equal share of blocks. Lastly, if no server
 *    definitions are provided then the block map is simply
 *    
 *       [0]
 *       
 *    which means all blocks go in a single directory (specified by the
 *    secondary file path).
 *    
 * 4) Functionality
 * 
 *    Most file server functionality can be classified as
 * 
 *        constructors
 *    
 *        add/remove/get server or server tag/path entries
 *        
 *        add/remove/get server or secondary tag/path entries
 *        
 *        set/get server usage fractions or stencils
 * 
 *        server index and path getters from block index, row, col arguments
 *        
 *        read/write utilities to read or write a MatrixBlockFileServer
 * 
 *        create non-existent secondary server paths
 *        
 *        validate paths and directories
 *          validate server paths
 *          validate server/secondary paths
 *          validate existence of MatrixBlockDefinition file in secondary
 *            directories
 *          validate existence of MatrixBlocks in secondary directories
 *        
 *        toString() for output
 * 
 * @author jrhipp
 *
 */
@SuppressWarnings("serial")
public class MatrixBlockFileServer implements Serializable
{  
  /**
   * Stores the server file path strings.
   */
  private ArrayList<String>            aServers                   = null;

  /**
   * Short name for each server path. Used for output purposes.
   */
  private ArrayList<String>            aServerTags                = null;

  /**
   * Stores the secondary file path strings that are appended to the server
   * string given a specific input type T.
   */
  protected HashMap<String , String>   aSecondaryFilePath         = null;

  /**
   * Stores the usage, or weight, with which the corresponding server in
   * aServers is used to write blocks. These values are used to create
   * an automated block server map stencil so that a user does not have
   * to input one. Used for input only
   */
  private ArrayList<Double>            aServerUseFractions        = null;

  /**
   * Contains the cyclic block server map that maps a specific row/column
   * block index to a specific server. Used for input only.
   */
  private ArrayList<Integer>           aBlockServerStencil        = null;

  /**
   * The automated block server map created from the input server fraction
   * set (aServerUseFraction).
   */
  private transient int[]              aBlockServerMap            = null;

  /**
   * Default constructor. Instantiates containers.
   */
  public MatrixBlockFileServer()
  {
    aServers               = new ArrayList<String>();
    aServerTags            = new ArrayList<String>();
    aServerUseFractions    = new ArrayList<Double>();
    aSecondaryFilePath     = new HashMap<String, String>();
    aBlockServerStencil    = new ArrayList<Integer>();
  }

  /**
   * Default constructor. Reads from a file.
   */
  public MatrixBlockFileServer(String fpth) throws IOException
  {
    read(fpth);
  }

  /**
   * Copy constructor.
   * 
   * @param fsp input FileServerPath from which this FileServerPath will be
   *            constructed (copied).
   */
  public MatrixBlockFileServer(MatrixBlockFileServer fsp)
  {
    aServers               = new ArrayList<String>(fsp.aServers);
    aServerTags            = new ArrayList<String>(fsp.aServerTags);
    aServerUseFractions    = new ArrayList<Double>(fsp.aServerUseFractions);
    aSecondaryFilePath     = new HashMap<String, String>(fsp.aSecondaryFilePath);
    aBlockServerStencil    = new ArrayList<Integer>(fsp.aBlockServerStencil);
  }

  /**
   * Standard constructor. Creates a new file server from an input properties
   * file using the property pthProperty. Only primary servers paths, and
   * optionally, usage fractions can be input this way. Secondary path tags
   * must be added after instantiation.
   * 
   * @param props       A properties object from which the server path (and
   *                    usage) information is read.
   * @param pthProperty The property containing the path (and usage) information.
   * @throws IOException
   */
  public MatrixBlockFileServer(PropertiesPlus props, String pthProperty)
  		   throws IOException
  {
    aServers               = new ArrayList<String>();
    aServerTags            = new ArrayList<String>();
    aServerUseFractions    = new ArrayList<Double>();
    aSecondaryFilePath     = new HashMap<String, String>();
    aBlockServerStencil    = new ArrayList<Integer>();
    createFileServerFromPathProperty(props.getProperty(pthProperty, "").trim());
  }

  /**
   * Resets the file server back to defaults (Default construction).
   */
  public void reset()
  {
    aServers.clear();
    aServerTags.clear();
    aServerUseFractions.clear();
    aSecondaryFilePath.clear();
    aBlockServerStencil.clear();
  }

  /**
   * Returns list of server paths.
   * 
   * @return List of server paths.
   */
  public ArrayList<String> getServerPaths()
  {
    return aServers;
  }

  /**
   * Returns list of server path tags.
   * 
   * @return List of server path tags.
   */
  public ArrayList<String> getServerTags()
  {
    return aServerTags;
  }

  /**
   * Adds a new server path (pth) and its associated storage use fraction
   * (useFraction).
   * 
   * @param pth The new path to add.
   * @param useFraction The associated storage use fraction.
   */
  public void addServerPathUsage(String pth, double useFraction)
  {
    addServerPath(pth);
    aServerUseFractions.add(useFraction);
  }

  /**
   * Adds a new server path string and associated tag to the server path
   * containers.
   * 
   * @param pth The string to be added to the server path container.
   */
  public void addServerPath(String pth)
  {
    aServers.add(pth);
    aServerTags.add(getPathTag(pth));
  }

  /**
   * Appends a partial path to all server paths. Note the resulting paths are
   * not checked for existence and should be validated after the fact.
   * 
   * @param appndPth The path to append to all server paths.
   */
  public void appendPathToServers(String appndPth)
  {
  	for (int i = 0; i < aServers.size(); ++i)
  		aServers.set(i,  aServers.get(i) + File.separator + appndPth);
  }

  /**
   * Creates a new file server from the input property string. Old definitions
   * are deleted. The new definition defines only server paths, and optionally,
   * usage. The format of the string should be
   * 
   *   option A:
   *     fileServerName
   * 
   *   option B:
   *     [fraction1] serverPath1;
   *     [fraction2] serverPath2;
   *     ...
   *     [fractionN] serverPathN
   * 
   * If option A is used then fileServerName is an existing MatrixFileServer
   * definition written to disk at the input path name. If option B is used
   * then each path must be separated by ";". If the optional usage fraction
   * is given it must be first (before the path string) for each path entry.
   * An error is thrown if neither of these syntax choices is adhered to, or
   * if the input string is empty.
   * 
   * @param pathProperty
   * @throws IOException
   */
  public void createFileServerFromPathProperty(String pathProperty)
         throws IOException
  {
  	String s;

  	// get all path definitions and check for none

  	String[] paths = Globals.getTokens(pathProperty, ";");
  	if ((paths == null) || (paths.length == 0))
  	{
  		s = "Error: Input path property is undefined ..." + NL;
  		throw new IOException(s);
  	}

  	// reset current definition and check to see if pathProperty is a single
  	// path/name ...

  	reset();
  	if (paths.length == 1)
  	{
  		// see if it is an existing file

  		paths[0] = PropertiesPlus.convertWinFilePathToLinux(paths[0]);
  		File f = new File(paths[0]);
  		if (f.exists() && f.isFile())
  		{
  			// this is a file server file ... read it and exit

  			read(paths[0]);
  			return;
  		}
  	}

  	// 1 or more [frac] paths are available ... read, validate and set

  	int tknCnt = 1;
  	for (int i = 0; i < paths.length; ++i)
  	{
  		String[] pthTkns = Globals.getTokens(paths[i], "\t, ");
  		if ((i == 0) && (pthTkns.length == 2)) tknCnt = 2;
  		if (pthTkns.length > 2)
  		{
  			s = "Error: Input path has more than two tokens ([fraction path]) ..." + NL +
  					"       Found: " + paths[i] + NL;
  			throw new IOException(s);
  		}
  		
  		// error if pthTkns.length != tknCnt

  		if (pthTkns.length != tknCnt)
  		{
  			s = "Error: Expected " + tknCnt + " token per path entry but found " +
  		      pthTkns.length + " in path entry:" + NL + paths[i] + NL;
  			throw new IOException(s);
  		}

  		// validate path

  		pthTkns[tknCnt-1] = PropertiesPlus.convertWinFilePathToLinux(pthTkns[tknCnt-1]);
  		File f = new File(pthTkns[tknCnt-1]);
  		if (!f.exists())
  		{
  			s = "Error: Input file server path: \"" + pthTkns[tknCnt-1] + "\"" + NL +
  					"       Does not exist ..." + NL;
  			throw new IOException(s);
  		}

  		// add server
  		
  		if (tknCnt == 2)
  			addServerPathUsage(pthTkns[1], Double.valueOf(pthTkns[0]));
  		else
  			addServerPath(pthTkns[0]);
    }
  }

  /**
   * Simple function that returns a shortened name for the input path.
   * If the path has no "\", "/", or "." characters then the tag is simply
   * the path. Otherwise, the tag is the first set of characters (excluding
   * "\", "/", or ".") found in the path. For example the input path name
   * 
   *     "\\\\fignewton.floobee.com\\test\\name"
   * 
   * has the tag name of "fignewton".
   * @param pth The input path for which the "tag" will be returned.
   * @return The tag associated with the input path.
   */
  public static String getPathTag(String pth)
  {
    String[] tokens = {pth};
    if (pth.indexOf("\\") != -1)
    {
      tokens = Globals.getTokens(pth, "\\");
    }
    else if (pth.indexOf("/") != -1)
    {
      tokens = Globals.getTokens(pth, "/");
    }

    tokens = Globals.getTokens(tokens[0], ".");
    return tokens[0];    
  }

  /**
   * Returns the ith server path.
   * 
   * @param i The index of the server path to return.
   * @return The ith server path.
   */
  public String getServerPath(int i)
  {
    return aServers.get(i);
  }

  /**
   * Returns the ith server tag.
   * 
   * @param i The index of the server tag to return.
   * @return The ith server tag.
   */
  public String getServerTag(int i)
  {
    return aServerTags.get(i);
  }

  /**
   * Returns the number of server paths stored.
   * 
   * @return The number of server paths stored.
   */
  public int getServerCount()
  {
    return aServers.size();
  }

  /**
   * Adds a new secondary file path string associated with the input
   * type to the secondary file path map.
   * 
   * @param t The type associated with the input string spth.
   * @param spth The secondary path string associated with type t which is
   *             added to the secondary file path map.
   */
  public void addSecondaryFilePath(String t, String spth)
  {
    aSecondaryFilePath.put(t, spth);
  }

  /**
   * Sets this file servers secondary file paths to the same as the input
   * file servers secondary file paths. Any existing secondary file paths are
   * overwritten.
   * 
   * @param mbfs The file server whose secondary file paths will be set into
   *             this file server overwriting any existing paths.
   */
  public void setSecondaryFilePaths(MatrixBlockFileServer mbfs)
  {
    // clear current list of secondaries and define it as equivalent to the
    // secondary path map from the input file server (mbfs).

    aSecondaryFilePath.clear();
    for (Map.Entry<String, String> e: mbfs.aSecondaryFilePath.entrySet())
    {
      aSecondaryFilePath.put(e.getKey(), e.getValue());
    }
  }

  /**
   * Modifies/Adds a new secondary file path string associated with the input
   * type to the secondary file path map.
   * 
   * @param t The type associated with the input string spth.
   * @param spth The new secondary path string associated with type t which is
   *             is set in place of the old string. If t is not found the new
   *             path is add to the seconday server paths.
   */
  public void modifySecondaryFilePath(String t, String spth)
  {
    if (aSecondaryFilePath.containsKey(t))
      aSecondaryFilePath.put(t, spth);
    else
      addSecondaryFilePath(t, spth);
  }

  /**
   * Removes the secondary file path associated with the input tag t.
   * 
   * @param t The tag of the secondary file path to be removed.
   */
  public void removeSecondaryFilePath(String t)
  {
    aSecondaryFilePath.remove(t);
  }

  /**
   * Returns the secondary file path associated with input type t or null if
   * type t is not associated with a secondary file path.
   * 
   * @param t The type associated with the returned secondary file path.
   * @return The secondary file path associated with input type t or null if
   *         type t is not associated with a secondary file path.
   */
  public String getSecondaryFilePath(String t)
  {
    return aSecondaryFilePath.get(t);
  }

  /**
   * Returns the number of secondary file paths stored.
   * 
   * @return The number of secondary file paths stored.
   */
  public int getSecondaryFilePathCount()
  {
    return aSecondaryFilePath.size();
  }

  /**
   * Returns map of secondary file paths.
   * 
   * @return Map of secondary file paths.
   */
  public HashMap<String, String> getSecondaryFilePaths()
  {
    return aSecondaryFilePath;
  }

  /**
   * Adds a new server storage use fraction (useFraction).
   * 
   * @param useFraction The associated storage use fraction.
   */
  public void addServerStorageUsageFraction(double useFraction)
  {
    aServerUseFractions.add(useFraction);
  }

  /**
   * Returns the ith server storage use fraction.
   * 
   * @param i The index of the server storage use fraction to return.
   * @return The ith server storage use fraction.
   */
  public double getServerStorageUseFraction(int i)
  {
    return aServerUseFractions.get(i);
  }

  /**
   * Returns the ith block server map entry.
   * 
   * @param i The index of the block server map entry to be returned.
   * @return The ith block server map entry.
   * @throws IOException 
   */
  public int getBlockServerMapEntry(int i) throws IOException
  {
    if (aBlockServerMap == null) buildBlockServerMap();
    return aBlockServerMap[i];
  }

  /**
   * Returns the number of block server map entries stored.
   * 
   * @return The number of block server map entries stored.
   */
  public int getBlockServerMapCount() throws IOException
  {
    if (aBlockServerMap == null) buildBlockServerMap();
    return aBlockServerMap.length;    
  }

  /**
   * Adds a new block server stencil entry to the block server stencil list.
   * This entry must be be 0 or larger but less than the size of the number
   * of servers (aServers.size()).
   * 
   * @param mpe The new block server stencil entry.
   * @throws IOException
   */
  public void addBlockServerStencilEntry(int mpe) throws IOException
  {
    if ((mpe < 0) || (mpe >= aServers.size()))
    {
      throw new IOException("Invalid block server map entry: " + mpe + NL);
    }

    aBlockServerStencil.add(mpe);
  }

  /**
   * Returns the ith block server stencil entry.
   * 
   * @param i The index of the block server stencil entry to be returned.
   * @return The ith block server stencil entry.
   */
  public int getBlockServerStencilEntry(int i)
  {
    return aBlockServerStencil.get(i);
  }

  /**
   * Returns the number of block server stencil entries.
   * 
   * @return The number of block server stencil entries.
   */
  public int getBlockServerStencilCount()
  {
    return aBlockServerStencil.size();    
  }

  /**
   * Return the first block row/column index stored in the server directory
   * specified by the input index. This is used by LSINV to validate the
   * directory.
   * 
   * @param srvrIndx The server directory index.
   * @return The first block row/column index
   */
  public int[] getFirstBlockRowColumnIndex(int srvrIndx) throws IOException
  {
    int[] rc = {0, 0};

    // build the default block server map if not yet done.
    
    if (aBlockServerMap == null) buildBlockServerMap();
    if (aBlockServerMap.length == 1) return rc;

    // break on the appropriate server index ... Throw IO exception if
    // server index is not valid

    int blkIndx;
    for (blkIndx = 0; blkIndx < aBlockServerMap.length; ++blkIndx)
      if (srvrIndx == aBlockServerMap[blkIndx]) break;
    if (blkIndx == aBlockServerMap.length)
    {
      throw new IOException("  Invalid server index (" + srvrIndx + ") ...");
    }

    // get the first row, column entry for the server at the input index

    return MatrixBlock.getBlockRowCol(blkIndx);
  }

  /**
   * Return the server index associated with the input block row and column
   * index.
   * 
   * @param row The block row index for which a server index containing the
   *            block is to be returned.
   * @param col The block column index for which a server index containing the
   *            block is to be returned.
   * @return The server index associated with the input block row and column
   *         index.
   */
  public int getServerIndex(int row, int col) throws IOException
  {
    if (aBlockServerMap == null) buildBlockServerMap();
    int blkIndx  = MatrixBlock.getBlockIndex(row, col);
    return aBlockServerMap[blkIndx % aBlockServerMap.length];
  }

  /**
   * Return the server index associated with the input block index.
   * 
   * @param blkIndex The block index for some row and column in the matrix.
   *                 (MatrixBlock.getBlockIndex(row, col)).
   * @return The server index associated with the input block row and column
   *         index.
   */
  public int getServerIndex(int blkIndex) throws IOException
  {
    if (aBlockServerMap == null) buildBlockServerMap();
    return aBlockServerMap[blkIndex % aBlockServerMap.length];
  }

  /**
   * Static function that returns the server index given an arbitrary block
   * server map and the block row and column index. If the input server map
   * is null or its size is <= 1 then 0 is returned.
   * 
   * @param blkSrvrMap The server map that is used to determine the server
   *                   index.
   * @param row The block row index for which a server index will be
   *            returned.
   * @param col The block column index for which a server index will be
   *            returned.
   * @return The server index given an arbitrary block server map and the
   *         block row and column index.
   */
  public static int getServerIndex(ArrayList<Integer> blkSrvrMap,
                                   int row, int col)
  {
    // return 0 if the server map is null or <= 1 in size

    if ((blkSrvrMap == null) || (blkSrvrMap.size() <= 1)) return 0;

    // return the server index based on the input row and column

    int blkIndx  = MatrixBlock.getBlockIndex(row, col);
    return blkSrvrMap.get(blkIndx % blkSrvrMap.size());
  }

  /**
   * Returns a path to where the type t block row,col is stored. If no server
   * paths are stored then the secondary file path defined for the input type
   * is returned. If the input type is not recognized null is returned. If one
   * server is defined then the row, col indices are ignored since all blocks
   * are stored on the same server. If more than one server is defined then
   * the server is chosen on the row, column index and the block server map.
   * 
   *  
   * @param t The type of the block for which a path will be returned.
   * @param row The row of the block for which a path will be returned.
   * @param col The column of the block for which a path will be returned.
   * @return A path to where the type t block row,col is stored. Or null if
   *         the input type t is not recognized and no servers were stored
   *         in this object.
   */
  public String getPath(String t, int row, int col) throws IOException
  {
    return getPath(t, getServerIndex(row, col));
  }

  /**
   * Returns the path for secondary type t of server entry i. If no servers
   * are present or i is out of range then the secondary path is returned
   * alone. If the types secondary flag is set then the secondary path is
   * appended with the ith servers base name.
   * 
   * @param t         The type for which the secondary path will be chosen.
   * @param srvrIndex The server index.
   * @return The path for secondary type t of server entry i.
   */
  public String getPath(String t, int srvrIndex)
  {
    // get the server path if i is valid (else "")

    String pth = "";
    if (srvrIndex < aServers.size()) pth = aServers.get(srvrIndex);

    // appends the secondary file path of type t to the server path
    // for entry i and returns the result. If the server path is empty
    // then the secondary file path is returned alone. If the secondary
    // file path flag is true only the base name of the server path is
    // used for the append.

    String secFilePath = aSecondaryFilePath.get(t);
    if (pth.equals(""))
      pth = secFilePath;
    else if ((secFilePath != null) && !secFilePath.equals(""))
      pth += File.separator + secFilePath;

    // done ... return the path

    return pth;
  }

  /**
   * Returns default MatrixBlockFileServer file name.
   * 
   * @return Default MatrixBlockFileServer file name.
   */
  public static String defaultFileServerFileName()
  {
    return "fileserver";
  }

  /**
   * Reads and initializes this file server from the definition on disk at the
   * input path.
   * 
   * @param fpth The file path of the definition to be read.
   * @throws IOException
   */
  public void read(String fpth) throws IOException
  {
    FileInputBuffer fib = new FileInputBuffer(fpth);
    
    // read file server strings and tags

    int n = fib.readInt();
    aServers    = new ArrayList<String>(n);
    aServerTags = new ArrayList<String>(n);
    for (int i = 0; i < n; ++i)
    {
    	String pth = fib.readString();
    	pth = PropertiesPlus.convertWinFilePathToLinux(pth);
      aServers.add(pth);
      aServerTags.add(fib.readString());
    }
    
    // read secondary file path map
    
    n = fib.readInt();
    aSecondaryFilePath = new HashMap<String, String>();
    for (int i = 0; i < n; ++i)
    {
      String key = fib.readString();
      String val = fib.readString();
      val = PropertiesPlus.convertWinFilePathToLinux(val);
      aSecondaryFilePath.put(key, val);
    }

    // read in block server map if defined

    n = fib.readInt();
    if (n > 0)
    {
      aBlockServerMap = new int [n];
      for (int i = 0; i < n; ++i) aBlockServerMap[i] = fib.readInt();
    }
    else 
      aBlockServerMap = null;

    // read in server use fractions if defined

    n = fib.readInt();
    if (n > 0)
    {
      aServerUseFractions = new ArrayList<Double>(n);
      for (int i = 0; i < n; ++i) aServerUseFractions.add(fib.readDouble());
    }
    else
      aServerUseFractions = new ArrayList<Double>();

    // read in block server stencil if defined

    n = fib.readInt();
    if (n > 0)
    {
      aBlockServerStencil = new ArrayList<Integer>(n);
      for (int i = 0; i < n; ++i) aBlockServerStencil.add(fib.readInt());
    }
    else
      aBlockServerStencil = new ArrayList<Integer>();
    
    fib.close();
  }

  /**
   * Writes this file server definition to each of its server paths.
   * 
   * @throws IOException
   */
  public void write() throws IOException
  {
    for (int i = 0; i < aServers.size(); ++i)
    {
      String pth = aServers.get(i);
      String pthfil = pth + File.separator + defaultFileServerFileName();
      write(pthfil);
    }
  }

  /**
   * Writes this file server to a file at the input path.
   * 
   * @param fpth The file path where the file will be written.
   * @throws IOException
   */
  public void write(String fpth) throws IOException
  {
    FileOutputBuffer fob = new FileOutputBuffer(fpth);
    
    // write file server strings and tags

    fob.writeInt(aServers.size());
    for (int i = 0; i < aServers.size(); ++i)
    {
      fob.writeString(aServers.get(i));
      fob.writeString(aServerTags.get(i));
    }
    
    // write secondary file path map
    
    fob.writeInt(aSecondaryFilePath.size());
    for (Map.Entry<String, String> e: aSecondaryFilePath.entrySet())
    {
      fob.writeString(e.getKey());
      fob.writeString(e.getValue());
    }

    // wrote block server map if defined
    
    if (aBlockServerMap != null)
    {
      fob.writeInt(aBlockServerMap.length);
      for (int i = 0; i < aBlockServerMap.length; ++i)
        fob.writeInt(aBlockServerMap[i]);
    }
    else
      fob.writeInt(0);

    // write server use fractions if defined

    fob.writeInt(aServerUseFractions.size());
    for (int i = 0; i < aServerUseFractions.size(); ++i)
      fob.writeDouble(aServerUseFractions.get(i));

    // write block server stencil if defined

    fob.writeInt(aBlockServerStencil.size());
    for (int i = 0; i < aBlockServerStencil.size(); ++i)
      fob.writeInt(aBlockServerStencil.get(i));
    
    fob.close();
  }

  /**
   * Create all secondary paths that do not yet exist.
   */
  public void createSecondaryPaths() throws IOException
  {
    // loop over all server paths

    for (int i = 0; i < aServers.size(); ++i)
    {
      // save the server path and loop over all secondary paths

      String pth = aServers.get(i);
      for (Map.Entry<String, String> e: aSecondaryFilePath.entrySet())
      {
        // make a file from the server path and secondary path and see if it
        // exists

        File f = new File(pth + File.separator + e.getValue());
        if (!f.exists())
        {
          // path does not exist ... create directory ... if failure throw
          // exception

          if (!f.mkdir())
          {
            String s = "Error: Could not create secondary path \"" +
                       e.getValue() + "\"" + NL +
                       "       For server path \"" + pth + "\" ..." + NL;
            throw new IOException(s);
          }
        }
      }
    }
  }

  /**
   * Returns true if the server defined by the input index is a valid path.
   * 
   * @param serverIndex The server index to be validated.
   * @return True if the server defined by the input index exists.
   */
  public boolean validateServerPath(int serverIndex)
  {
    File f = new File(aServers.get(serverIndex));
    if (f.exists())
      return true;
    else
      return false;
  }

  /**
   * Validate server paths for existence. If a path does not exist it is
   * created. If it cannot be created an error is thrown
   */
  public void validateCreateServerPaths() throws IOException
  {
    // create bad list and loop over all server paths

    for (int i = 0; i < aServers.size(); ++i)
      if (!validateServerPath(i))
      	FileDirHandler.createDirectory(aServers.get(i));
  }

  /**
   * Validate server paths for existence. A list is returned containing all
   * server path indices that do not exist. If empty all exist.
   */
  public ArrayList<Integer> validateServerPaths()
  {
    // create bad list and loop over all server paths

    ArrayList<Integer> badSrvrList = new ArrayList<Integer>();
    for (int i = 0; i < aServers.size(); ++i)
      if (!validateServerPath(i)) badSrvrList.add(i);

    // return the bad list

    return badSrvrList;
  }

  /**
   * Returns true if the secondary path defined by the tag secTag exists for
   * the server path defined by the serverIndex.
   * 
   * @param serverIndex The serverIndex for which the secondary path will be
   *                    validated.
   * @param secTag      The secondary path tag to be validated.
   * @return True if the secondary path for the requested server path exists.
   */
  public boolean validateSecondaryPath(int serverIndex, String secTag)
  {
    String pth = getPath(secTag, serverIndex);
    File f = new File(pth);
    if (f.exists())
      return true;
    else
      return false;
  }

  /**
   * Returns a list of server indices for which the input secondary path defined
   * by the tag secTag is not a defined path. If the list is empty all server
   * paths define the requested secondary path.
   * 
   * @param secTag      The secondary path tag to be validated.
   * @return A list of all server indices that DO NOT define the secondary path
   *         associated with the input tag.
   */
  public ArrayList<Integer> validateSecondaryPaths(String secTag)
  {
    ArrayList<Integer> badSrvrList = new ArrayList<Integer>();
    for (int i = 0; i < aServers.size(); ++i)
      if (!validateSecondaryPath(i, secTag)) badSrvrList.add(i);    

    // return the bad list

    return badSrvrList;
  }

  /**
   * Validate secondary paths for existence. A map of containing all
   * non-existent server paths for each secondary paths (tag) is returned.If
   * the map is empty all secondary paths for all server paths exist.
   * 
   * @return A map of all invalid server/secondary paths.
   */
  public HashMap<String, ArrayList<Integer>> validateSecondaryPaths()
  {
    // create the bad map and loop over all server paths

    HashMap<String, ArrayList<Integer>> badMap =
        new HashMap<String, ArrayList<Integer>>();
    for (Map.Entry<String, String> e: aSecondaryFilePath.entrySet())
    {
      ArrayList<Integer> badList = validateSecondaryPaths(e.getKey());
      if (badList.size() > 0) badMap.put(e.getKey(), badList);
    }
    
    return badMap;
  }

  /**
   * Returns true if the secondary path (specified by the secondary path tag)
   * for the server specified by the input server index contains a
   * MatrixBlockDefinition file.
   * 
   * @param serverIndex The server path to be tested.
   * @param secTag      The secondary path to be tested.
   * @return True if a MatrixBlockDefinition file is stored at the requested
   *         path.
   * @throws IOException
   */
  public boolean validateMatrixBlockDefinitionFile(int serverIndex,
                                                   String secTag)
         throws IOException
  {
    // get path to server/secondary and get default MatrixBlockDefinition file
    // name

    String pth = getPath(secTag, serverIndex);
    String blkNam = MatrixBlockDefinition.getDefaultFileName();
    
    // build file and return true if it exists

    File f = new File(pth + File.separator + blkNam);
    if (f.exists())
      return true;
    else
      return false;
  }

  /**
   * Returns true if the secondary path (specified by the secondary path tag)
   * for the server specified by the input server index contains a
   * MatrixBlockDefinition file.
   * 
   * @param secTag      The secondary path from which the matrix block
   *                    definition is extracted.
   * @return The MatrixBlockDefinition file.
   * @throws IOException
   */
  public MatrixBlockDefinition loadMatrixBlockDefinition(String secTag)
         throws IOException
  {
    // get path to server/secondary and get default MatrixBlockDefinition file
    // name

    String pth = getPath(secTag, 0);
    String blkNam = MatrixBlockDefinition.getDefaultFileName();

    // build definition and return it if it exists. Otherwise, return null

    String mbdPth = pth + File.separator + blkNam;
    File f = new File(mbdPth);
    if (f.exists())
      return new MatrixBlockDefinition(mbdPth);
    else
      return null;
  }

  /**
   * Writes the input matrix block definition to all servers under the input
   * secondary tag
   * 
   * @param mbd    The matrix block definition to be written to each server.
   * @param secTag The secondary tag under which the matrix block definition
   *               will be written.
   * @throws IOException
   */
  public void writeMatrixBlockDefinition(MatrixBlockDefinition mbd,
  		                                   String secTag) throws IOException
  {
  	for (int i = 0; i < aServers.size(); ++i)
  	{
  		String pth = getPath(secTag, i) + File.separator +
  				         MatrixBlockDefinition.getDefaultFileName();
  		mbd.write(pth);
  	}
  }

  /**
   * Returns a list of all server indices whose secondary path (associated with
   * secTag) does not contain a MatrixBlockDefinition file. If the list is
   * empty they all contain the file.
   * 
   * @param secTag The tag associated with the secondary path to be checked.
   * @return A list of all server indices that do not contain a
   *         MatrixBlockDefinition file in the requested secondary directory.
   * @throws IOException
   */
  public ArrayList<Integer> validateMatrixBlockDefinitionFiles(String secTag)
         throws IOException
  {
    ArrayList<Integer> badSrvrList = new ArrayList<Integer>();
    for (int i = 0; i < aServers.size(); ++i)
      if (!validateMatrixBlockDefinitionFile(i, secTag)) badSrvrList.add(i);    

    // return the bad list

    return badSrvrList;
  }

  /**
   * Validate the entire file server to ensure that every secondary path of
   * every server path has a MatrixFileDefintion file defined. If any do not
   * they are returned in a map. If the map is empty all secondary paths for
   * all server paths contain a MatrixFileDefintion file.
   * 
   * @return A map of all invalid server/secondary paths that do not contain a
   *         MatrixFileDefintion file.
   */
  public HashMap<String, ArrayList<Integer>> validateMatrixBlockDefinitionFiles()
         throws IOException
  {
    // create the bad list and loop over all server paths

    HashMap<String, ArrayList<Integer>> badMap =
        new HashMap<String, ArrayList<Integer>>();
    
    for (Map.Entry<String, String> e: aSecondaryFilePath.entrySet())
    {
      ArrayList<Integer> badList = validateMatrixBlockDefinitionFiles(e.getKey());
      if (badList.size() > 0) badMap.put(e.getKey(), badList);
    }
    
    return badMap;
  }

  /**
   * Returns true if the the MatrixBlock file defined by the input block index
   * is contained in the secondary directory associated with the input tag.
   * 
   * @param blockIndex The MatrixBlock index.
   * @param secTag     The secondary paths associated tag.
   * @return True if the block file was found.
   * @throws IOException
   */
  public boolean validateBlockFile(int blockIndex, String secTag)
         throws IOException
  {
    // get the server index that has the block
 
    int serverIndex = this.getServerIndex(blockIndex);
 
    // get the path to the server and secondary directory.

    String pth    = getPath(secTag, serverIndex);
    String blkNam = MatrixBlock.getFileName(secTag, blockIndex);
    
    // make a file and return true if the block file exists

    File f = new File(pth + File.separator + blkNam);
    if (f.exists())
      return true;
    else
      return false;
  }

  /**
   * Returns a list of all MatrixBlock block indices that are not defined in
   * the secondary path associated with the input tag.
   * 
   * @param secTag The tag associated with a secondary directory path.
   * @param nblks The number of block indices to check.
   * @return A list of all block indices that are not defined in the secondary
   *         path requested.
   * @throws IOException
   */
  public ArrayList<Integer> validateBlockFiles(String secTag, int nblks)
         throws IOException
  {
    ArrayList<Integer> badBlkList = new ArrayList<Integer>();
    for (int i = 0; i < nblks; ++i)
      if (!validateBlockFile(i, secTag)) badBlkList.add(i);    

    // return the bad list

    return badBlkList;
  }

  /**
   * Returns a map of all missing blocks in this file server. If the map is
   * empty all blocks are defined.
   * 
   * @param nblks The total number of blocks to be validated.
   * @return A map containing all undefined matrix blocks in this file server.
   * @throws IOException
   */
  public HashMap<String, ArrayList<Integer>> validateBlockFiles(int nblks)
         throws IOException
  {
    // create the bad list and loop over all server paths

    HashMap<String, ArrayList<Integer>> badMap =
        new HashMap<String, ArrayList<Integer>>();
    
    for (Map.Entry<String, String> e: aSecondaryFilePath.entrySet())
    {
      ArrayList<Integer> badList = validateBlockFiles(e.getKey(), nblks);
      if (badList.size() > 0) badMap.put(e.getKey(), badList);
    }

    return badMap;
  }

  /**
   * Output the description of this file server as a string.
   */
  @Override
  public String toString()
  {
    return toString("");
  }

  /**
   * Output the description of this file server as a string.
   */
  public String toString(String hdr)
  {
    // build the block server map if necessary

    try
    {
      buildBlockServerMap();
    }
    catch (Exception ex)
    {
      return ex.getMessage();
    }

    String s = "";

    // output message if no servers are assigned ... otherwise build output
    // string

    if (aServers.size() == 0)
      s = hdr + "   No Server Paths Are Defined ..." + NL;
    else
    {
      s = hdr + "   Matrix Block File Server Definition:" + NL + NL;
      
      // get maximum length of tag and path

      int maxtaglen = 0;
      int maxpthlen = 0;
      for (int i = 0; i < aServers.size(); ++i)
      {
        if (aServerTags.get(i).length() > maxtaglen)
          maxtaglen = aServerTags.get(i).length();
        if (aServers.get(i).length() > maxpthlen)
          maxpthlen = aServers.get(i).length();
      }

      // output server table header

      s += NL + hdr + "    " + Globals.centerString("Server Table",
                                 maxtaglen + maxpthlen + 19, false) + NL + NL;
      s += hdr + "     i    " +
           Globals.centerString("Tag", maxtaglen, false) +
           "   Usage(%)" +
           "   " +
           Globals.centerString("Server Path", maxpthlen, false) + NL;
      s += hdr + "    " + Globals.repeat("-", maxtaglen + maxpthlen + 19) + NL;
      
      // output server table

      String frmt = "    %3d   %" + maxtaglen + "s   %6.2f   %" + maxpthlen + "s";
      for (int i = 0; i < aServers.size(); ++i)
        s+= hdr + String.format(frmt,  i, aServerTags.get(i),
                          100.0 * aServerUseFractions.get(i), aServers.get(i)) + NL;
      
      // find maximum secondary path tag and path lengths
      
      int max2ndtaglen = 0;
      int max2ndpthlen = 0;
      for (Map.Entry<String, String> e: aSecondaryFilePath.entrySet())
      {
        if (e.getKey().length() > max2ndtaglen)
          max2ndtaglen = e.getKey().length();
        if (e.getValue().length() > max2ndpthlen)
          max2ndpthlen = e.getValue().length();
      }
      
      // output secondary path table

      s += NL + NL + hdr + "    " +
           Globals.centerString("Secondary Path Table",
                                max2ndtaglen + max2ndpthlen + 10, false) +
                                NL + NL;
      s += hdr + "      i    " +
           Globals.centerString("Tag", max2ndtaglen, false) +
           "   " +
           Globals.centerString("Path", max2ndpthlen, false) + NL;
      s += hdr + "    " +
           Globals.repeat("-", max2ndtaglen + max2ndpthlen + 10) + NL;
      
      // output secondary path table

      int i = 0;
      frmt = "    %3d   %" + max2ndtaglen + "s   %" + max2ndpthlen + "s";
      for (Map.Entry<String, String> e: aSecondaryFilePath.entrySet())
        s+= hdr + String.format(frmt,  i++, e.getKey(), e.getValue()) + NL;
      
      // output cyclic block server map header
      
      int n = aBlockServerMap.length;
      if (n > 20) n = 20;
      s += NL + NL + hdr + "    " +
           Globals.centerString("Block Server Map",
                                15 + 4 * n, false) + NL + NL;
      s += hdr + "    " + Globals.repeat("-", 15 + 4 * n) + NL;
      
      // output cyclic block server map

      i = 0;
      String s1 = "";
      String s2 = "";
      while (true)
      {
        // every 20th (or the last) out a new pair of lines to s

        if ((i % 20 == 0) || (i == aBlockServerMap.length))
        {
          // only add to s after the first accumulation

          if (i > 0)
          {
            s  += s1 + NL + s2 + NL + NL;
            s2 = s1 = "";
          }
          // make new line headers for the next set of 20

          s1 += hdr + "    block  index = ";
          s2 += hdr + "    server index = ";
        }

        // exist if were done

        if (i == aBlockServerMap.length) break;
        
        // add the ith block and server indexes to the current lines and
        // increment to the next

        s1 += String.format(" %3d", i);
        s2 += String.format(" %3d", aBlockServerMap[i]);
        ++i;
      }
    }

    // done return s

    return s;
  }

  /**
   * Builds the block server map. Only called once when aBlockServerMap is
   * requested but is still null. If no servers are defined aBlockServerMap
   * is sized to one entry containing 0. If aServers are defined and
   * server storage use fractions are input then a block server map using
   * up to 100 entries is defined. Otherwise, if the block server stencil
   * is input it becomes the block server map. Finally, if servers are
   * defined but the storage use fraction and the stencil lists are not
   * defined a default map containing each server index once and in order
   * is constructed.
   * 
   * @throws IOException
   */
  private synchronized void buildBlockServerMap() throws IOException
  {
    if (aBlockServerMap != null) return;

    // if any servers are define build a block server map ... otherwise
    // initialize to a single entry containing zero

    if (aServers.size() > 1)
    {
      // define map ... if server storage use fractions are defined then
      // build a map with approximately 100 entries ... otherwise if the
      // input block server stencil is defined then use that as the map ...
      // finally, just build a default map where each server is included
      // once

      if (aServerUseFractions.size() == aServers.size())
        buildBlockServerMap100();
      else if (aBlockServerStencil.size() > 0)
        buildBlockServerMapStencil();
      else
        buildBlockServerMapDefault();
    }
    else
      aBlockServerMap = new int [1];

    // set server use fractions

    setServerUsageFromBlockServerMap();
  }

  /**
   * Used to build an automatic block server stencil from the user input
   * server use fraction list (aServerUseFraction). This function is only
   * called if aServerUseFraction.size() == aServers.size() and the values
   * in the server fraction list are valid (not less than zero).
   */
  private void buildBlockServerMap100() throws IOException
  {
    // this function is only called if aServerUseFraction is the same size
    // as aServers and aBlockServerMap is not defined.

    // first build a normalized use fraction array f

    double[] f = new double [aServerUseFractions.size()];
    double s = 0.0;
    for (int i = 0; i < f.length; ++i)
    {
      f[i] = aServerUseFractions.get(i);
      s += f[i];
    }
    for (int i = 0; i < f.length; ++i) f[i] /= s;

    // now create a server fraction count array that specifies how many
    // entries for each server should be in approximately the first 100 blocks
    // scSum is the sum of each count which will be 100 +- 1.

    int[] SC = new int [f.length];
    for (int i = 0; i < f.length; ++i)
    {
      SC[i] = (int) Math.round((100 * f[i]));
      if (SC[i] < 1)
      {
        String es = "Error: Server \"" + aServers.get(i) + "\"" + NL +
                    "       has a block server map count that is < 1 ... " +
                    "Exiting";
        throw new IOException(es);
      }
    }
    int scSum = 0;
    for (int i = 0; i < SC.length; ++i) scSum += SC[i];

    // now make a tree map of each unique count in SC. This must be a multimap
    // since multiple servers can have the same count.

    TreeMap<Integer, HashSet<Integer>> tmap;
    tmap = new TreeMap<Integer, HashSet<Integer>>();
    for (int i = 0; i < SC.length; ++i)
    {
      HashSet<Integer> hset = tmap.get(SC[i]);
      if (hset == null)
      {
        hset = new HashSet<Integer>();
        tmap.put(SC[i], hset);
      }
      hset.add(i);
    }

    // If there is only one entry in tmap then make a simple block server map
    // that has each server in it one time and return

    if (tmap.size() == 1)
    {
      aBlockServerMap = new int [f.length];
      for (int i = 0; i < f.length; ++ i) aBlockServerMap[i] = i;
      return;
    }

    // at least one or more servers have a different block usage count
    // estimate a start index to place the first entry for each server

    int[] SCStart = new int [SC.length];
    for (int i = 0; i < SC.length; ++i)
      SCStart[i] = (int) ((double) scSum / SC[i] / 2.0);

    // See if a smaller block server map (smaller than 100) is possible.
    // if any multiple can be found such that
    //   useFrac[i] * mult - (int)(useFrac[i] * mult) == 0
    // for all servers (i) then mult is a valid multiple
    // loop over all multipliers from the number of servers to scSum

    int mult = 0;
    for (mult = SC.length; mult < scSum; ++mult)
    {
      // see if mult works ... loop over each server and test

      boolean good = true;
      for (int i = 0; i < SC.length; ++i)
      {
        // test ... if any i is invalid set boolean to false and break

        double suf = aServerUseFractions.get(i);
        if (suf * mult - (int) (suf * mult) != 0)
        {
          good = false;
          break;
        }
      }
      
      // if we made it to here and good is still true then mult is a valid
      // multiplier ... break and use it instead of scSum

      if (good) break;
    }

    // Ready ... now create the block server map ... initialize the map and two
    // temporary arrays to hold the available block indices that have not yet
    // been assigned ... loop over each entry in the tree map

    aBlockServerMap = new int [mult];
    for (int i = 0; i < aBlockServerMap.length; ++i)
      aBlockServerMap[i] = -1;
    ArrayList<Integer> avail = new ArrayList<Integer>(mult);
    for (int i = 0; i < mult; ++i) avail.add(i);
    ArrayList<Integer> tmpAvail = new ArrayList<Integer>(mult);

    // loop over all server count entries from most to least

    while(tmap.size() > 0)
    {
      // get and remove the last (largest) count and loop over each server that
      // has that entry count

      Map.Entry<Integer, HashSet<Integer>> e = tmap.pollLastEntry();
      for (Integer k: e.getValue())
      {
        int m = e.getKey() / (scSum / mult);

        // k is a server index and m is the number of entries for that server
        // calculate the step size and loop over each entry and insert it
        // into the server map. reset the available index to -1 after inserting
        // it into the server map

        double del = (double) avail.size() / m;
        for (int j = 0; j < m; ++j)
        {
          int n = (int) (del * (0.5 + j));
          aBlockServerMap[avail.get(n)] = k;
          avail.set(n,  -1);
        }

        // clear the temporary array and copy all valid entries (not -1) into
        // tmpAvail from the list avail

        tmpAvail.clear();
        for (int i = 0; i < avail.size(); ++i)
          if (avail.get(i) != -1) tmpAvail.add(avail.get(i));
        
        // swap tmpAvail with avail and continue
 
        ArrayList<Integer> tmp = avail;
        avail = tmpAvail;
        tmpAvail = tmp;
      } // end for (Integer k: e.getValue())
    } // end while(tmap.size() > 0)
  }

  /**
   * Builds a block server map from the input stencil list.
   */
  private void buildBlockServerMapStencil()
  {
    aBlockServerMap = new int [aBlockServerStencil.size()];
    for (int i = 0; i < aBlockServerStencil.size(); ++i)
      aBlockServerMap[i] = aBlockServerStencil.get(i);
  }

  /**
   * Builds a default block server map if it is not defined.
   */
  private void buildBlockServerMapDefault()
  {
    if (aServers.size() > 0)
    {
      aBlockServerMap = new int [aServers.size()];
      for (int i = 0; i < aServers.size(); ++i) aBlockServerMap[i] = i;
    }
    else
      aBlockServerMap = new int [1];
  }

  /**
   * assigns server use fractions based on the block server map prescription
   */
  private void setServerUsageFromBlockServerMap()
  {
    int[] cnt = new int [aServers.size()];
    for (int i = 0; i < aBlockServerMap.length; ++i) ++cnt[aBlockServerMap[i]];
    
    aServerUseFractions.clear();
    for (int i = 0; i < aServers.size(); ++i)
    {
      aServerUseFractions.add((double) cnt[i] / aBlockServerMap.length); 
    }
  }
}
