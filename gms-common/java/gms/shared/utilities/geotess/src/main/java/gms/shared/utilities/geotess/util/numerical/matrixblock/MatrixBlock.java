package gms.shared.utilities.geotess.util.numerical.matrixblock;

import static gms.shared.utilities.geotess.util.globals.Globals.NL;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.util.Date;

import gms.shared.utilities.geotess.util.filebuffer.FileInputBuffer;
import gms.shared.utilities.geotess.util.filebuffer.FileOutputBuffer;
import gms.shared.utilities.geotess.util.globals.Globals;

/**
 * The MatrixBlock object represents a single block from a matrix which is
 * subsequently decomposed further into sub-blocks. The MatrixBlock is the
 * fundamental file based entity of a full Out-Of-Core (OOC) matrix used in
 * the LSINV solution of the covariance matrix. The organizational structure
 * of the decomposition is given by the following decomposition matrix.
 * 
 *   Entries/Entries     Matrix     Block     Sub-Block  Elements
 *   
 *   Matrix               1          nmb        nmsb       nme
 *   Block                nmb        1          nbsb       nbe
 *   Sub-Block            nmsb       nbsb       1          nsbe
 *   Elements             nme        nbe        nsbe       1
 * 
 * where
 * 
 *   nmb  = number of block rows in a matrix
 *   nmsb = number of sub-block rows in a matrix
 *   nme  = number of element rows in a matrix
 * 
 *   nbsb = number of sub-block rows in a block
 *   nbe  = number of element rows in a block
 *
 *   nsbe = number of element rows in a sub-block
 * 
 * Please see the MatrixBlockDefinition object for a precise definition of how
 * a matrix is decomposed into blocks and sub-blocks.
 * 
 * The functionality provided by the matrix block includes the following\
 * capabilities:
 *
 *   // constructor
 *   
 *     construct
 *   
 *   // block instantiation and assignment
 * 
 *     create block (diagonal fill)
 *     read block
 *     read block (catch)
 *     write block
 *     write block (catch)
 *     unload
 * 
 *   // block access
 *
 *     get block
 *     get block element
 *     get sub-block
 *     get sub-block element
 *
 *   // block utilities
 *   
 *     fill lower symmetric matrix.
 *     transpose
 *     to string
 *
 *   // getter/setter
 *
 *     get matrix block definition
 *     get block row
 *     get block column
 *     get read file path
 *     set read file path
 *     get write file path
 *     set write file path
 *     get read/write fail messages
 *     get read time
 *     get write time
 *
 *  // inquiry
 *  
 *     is last sub-block row
 *     is last block row
 *     is diagonal
 *     is transposed
 *     is loaded
 *     is lower triangular filled
 *     is referenced
 *     is locked
 * 
 * The matrix block is a synchronized object and provides locking (fixed state)
 * mechanisms to avoid structural modification of the sub-block array once it
 * is referenced. All locking functionality is synchronized so that more than
 * one thread can utilize this matrix block at the same time. Creation and read
 * functionality is also synchronized so that only one thread can define the
 * block.
 * 
 * Created: July 5th, 2012
 * @author jrhipp
 *
 */
@SuppressWarnings("serial")
public class MatrixBlock implements Serializable
{
  /**
   * The maximum number of block loads encountered while a MatrixBlock was in
   * existence.
   */
  private static int                 aMaxDataLoads       = 0;

  /**
   * The total number of sub-block arrays loaded (or created). 
   */
  private static int                 aDataLoads          = 0;

	/**
	 * The matrix block definition object governing the size of this matrix
	 * block.
	 */
  private MatrixBlockDefinition      aMtrxBlkDefn        = null;

  /**
   * The matrix block row.
   */
  private int                        aBlkRow             = -1;

  /**
   * The matrix block column.
   */
  private int                        aBlkCol             = -1;

  /**
   * The matrix block read path
   */
  private String                     aSrcPath            = "";

  /**
   * The matrix block read file header
   */
  private String                     aSrcFilHdr          = "";

  /**
   * The matrix block write path
   */
  private String                     aDstPath            = "";

  /**
   * The matrix block write file header
   */
  private String                     aDstFilHdr          = "";

  /**
   * Block matrix divided into sub-blocks rows and columns and element rows
   * and columns within a sub-block. Sub-block si,sj is given by
   * 
   *   double[][] subBlkIJ = aSubBlks[si][sj];
   * 
   * The relative element i,j within the sub-block is giveen by
   * 
   *   double     subBlkIJElemIJ = subBlkIJ[i][j];
   */
  private double[][][][]             aSubBlks            = null;

  /**
   * Transposed representation of aSubBlks. This array is only created if a
   * transposed representation is needed but the current state is locked by
   * another requester. If this is the case this transposed rendition is built.
   * Otherwise aSubBlks is transposed to avoid the extra storage.
   */
  private double[][][][]             aTrnspSubBlks       = null;

  /**
   * The number of times this block was re-created by calling createBlock();
   */
  private int                        aCreationCount      = 0;

  /**+
   * The lock is incremented if any sub-block access function is called. These
   * include:
   * 
   *     getBlock(boolean transpose);
   *     getSubBlocks(boolean transpose);
   *     getSubBlock(int sbi, int sbj, boolean transpose);
   * 
   * Once a process finished using the sub-block array it must call the
   * function releaseLock(boolean transpose) to remove the lock.
   */
  private int                        aLock               = 0;

  /**
   * Same as aLock except this lock defines the requesters of aTrnspSubBlks
   * instead of aSubBlks.
   */
  private int                        aTrnspLock          = 0;

  /**
   * Current sub-block size setting. Defaults to aNumSubBlkElemRows. Can be
   * any value between 1 and aNumBlkElemRows that is an integral multiple of
   * aNumBlkElemRows (i.e. aNumBlkElemRows % aCurrSubBlockSize = 0). Only set
   * by the onwing MatrixBlockDefinition.
   */
  private int                        aCurrSubBlkSize     = 0;

  /**
   * The number of times this block has been resized.
   */
  private int                        aSubBlkResizeCount  = 0;

  /**
   * True if the block is in a transposed state.
   */
  private boolean                    aTranspose          = false;

  /**
   * The number of times that the matrix has been transposed. This excludes
   * the permanent creation of the transposed sub-blocks (aTransSubBlks).
   */
  private int                        aTransposeCount     = 0;

  /**
   * True if lower symmetric matrix has been filled.
   */
  private boolean                    aDiagBlkFilled      = false;

  /**
   * True if lower symmetric matrix has been filled.
   */
  private boolean                    aTrnspDiagBlkFilled = false;

  /**
   * The time (msec) that it took to read this block from disk.
   */
  private long                       aReadTime           = -1;
  
  /**
   * The number of times this block was read from disk (typically 0 or 1).
   */
  private int                        aReadCount          = 0;

  /**
   * The number of unsuccessful attempts to read this block.
   */
  private int                        aReadFailures       = 0;

  /**
   * The time (msec) that it took to write this block to disk.
   */
  private long                       aWriteTime          = -1;

  /**
   * The number of times this block was written to disk (typically 0 or 1).
   */
  private int                        aWriteCount         = 0;

  /**
   * The number of unsuccessful attempts to write this block.
   */
  private int                        aWriteFailures      = -1;

  /**
   * The time (msec) that it took to rename this block on disk.
   */
  private long                       aRenameTime         = -1;

  /**
   * The number of times this block was renamed on disk (typically 0 or 1).
   */
  private int                        aRenameCount        = 0;

  /**
   * The number of unsuccessful attempts to rename this block.
   */
  private int                        aRenameFailures     = 0;

  /**
   * The time (msec) that it took to delete this block from disk.
   */
  private long                       aDeleteTime         = -1;

  /**
   * The number of times this block was deleted from disk (typically 0 or 1).
   */
  private int                        aDeleteCount        = 0;

  /**
   * The number of unsuccessful attempts to delete this block.
   */
  private int                        aDeleteFailures     = 0;

  /**
   * The accumulated time waiting for a return from function requestIO().
   */
  private long                       aIOWaitTime         = 0;
  
  /**
   * If the read/write failed several times before succeeding all error
   * messages are appended to this string.
   */
  private String                     aIOFailMessg        = "";

  /**
   * Stores the current estimate of allocated memory (intrinsic) contained in
   * this MatrixBlock.
   */
  private long                       aAllocatedMem       = 0;
//
//  /**
//   * Outputs debug information if set to true.
//   */
//  private boolean                    aDebug              = false;

  /**
   * The number of times that a MatrixBlock IO operation (read, write, rename,
   * or delete) can fail before throwing an actual error message back to the
   * caller.
   */
  private static int                 aIOFailLimit        = 2;

  /**
   * The number of seconds that the read, write, rename, and delete BlockCatch
   * will sleep after failing to successfully perform the ascribed IO operation.
   */
  private static long                aIOCatchSleep       = 1;

  /**
   * Standard constructor.
   * 
   * @param blkRow The block row index.
   * @param blkCol The block column index.
   */
  public MatrixBlock(int blkRow, int blkCol, MatrixBlockDefinition mbd)
  {
    aBlkRow = blkRow;
    aBlkCol = blkCol;
    aMtrxBlkDefn = mbd;
    aAllocatedMem = this.getBaseMemoryAllocation();
  }
//
//  /**
//   * Turns the debug output facility on (true) or off (false).
//   * 
//   * @param dbg The input debug flag.
//   */
//  public void setDebug(boolean dbg)
//  {
//    aDebug = dbg;
//  }

  /**
   * Not used by Standard MatrixBlock objects. Used by the MatrixBlockManager
   * for MatrixBlockReference objects to schedule IO to avoid the sometimes
   * huge bottlenecks that develop when to many MatrixBlocks are trying to
   * read, write, rename, or delete simultaneously. This function returns the
   * amount of time that it had to wait for IO process approval (0 here).
   * @throws IOException 
   */
  public void requestIO() throws IOException
  {
    // do nothing
  }

  /**
   * Not used by Standard MatrixBlock objects. Used by the MatrixBlockManager
   * for MatrixBlockReference objects to schedule IO to avoid the sometimes
   * huge bottlenecks that develop when to many MatrixBlocks are trying to
   * read, write, rename, or delete simultaneously.
   *
   * @param op File operation ... "READ", "WRITE", "RENAME", or "DELETE".
   * @throws IOException 
   */
  public void completedIO(String op, String srvrTag) throws IOException
  {
    // does nothing
  }

  public void notifyIOAllocate(long mem, boolean load) throws IOException
  {
    // do nothing
  }

  public void notifyIOBlocking(long tim) throws IOException
  {
    // do nothing
  }
  
  /**
   * Returns the read time, or -1 if the block was never read.
   * 
   * @return The read time, or -1 if the block was never read.
   */
  public long getReadTime()
  {
    return aReadTime;
  }

  /**
   * Returns the read memory, or -1 if the block was never written.
   * 
   * @return The read memory, or -1 if the block was never written.
   */
  public long getReadMemory()
  {
    return aMtrxBlkDefn.getBlockIOMemory(aBlkRow, aBlkCol);
  }

  /**
   * Returns the number of successful reads performed by this MatrixBlock.
   * 
   * @return The number of successful reads performed by this MatrixBlock.
   */
  public int getReadCount()
  {
    return aReadCount;
  }

  /**
   * Returns the number of unsuccessful read attempts performed by this
   * MatrixBlock.
   * 
   * @return The number of unsuccessful read attempts performed by this
   *         MatrixBlock.
   */
  public int getReadFailures()
  {
    return aReadFailures;
  }

  /**
   * Returns the write time, or -1 if the block was never written.
   * 
   * @return The write time, or -1 if the block was never written.
   */
  public long getWriteTime()
  {
    return aWriteTime;
  }

  /**
   * Returns the write memory, or -1 if the block was never written.
   * 
   * @return The write memory, or -1 if the block was never written.
   */
  public long getWriteMemory()
  {
    return aMtrxBlkDefn.getBlockIOMemory(aBlkRow, aBlkCol);
  }

  /**
   * Returns the number of successful writes performed by this MatrixBlock.
   * 
   * @return The number of successful writes performed by this MatrixBlock.
   */
  public int getWriteCount()
  {
    return aWriteCount;
  }

  /**
   * Returns the number of unsuccessful write attempts performed by this
   * MatrixBlock.
   * 
   * @return The number of unsuccessful write attempts performed by this
   *         MatrixBlock.
   */
  public int getWriteFailures()
  {
    return aWriteFailures;
  }

  /**
   * Returns the rename time, or -1 if the block was never renamed.
   * 
   * @return The rename time, or -1 if the block was never renamed.
   */
  public long getRenameTime()
  {
    return aRenameTime;
  }

  /**
   * Returns the number of successful renames performed by this MatrixBlock.
   * 
   * @return The number of successful renames performed by this MatrixBlock.
   */
  public int getRenameCount()
  {
    return aRenameCount;
  }

  /**
   * Returns the number of unsuccessful rename attempts performed by this
   * MatrixBlock.
   * 
   * @return The number of unsuccessful rename attempts performed by this
   *         MatrixBlock.
   */
  public int getRenameFailures()
  {
    return aRenameFailures;
  }

  /**
   * Returns the rename time, or -1 if the block was never renamed.
   * 
   * @return The delete time, or -1 if the block was never deleted.
   */
  public long getDeleteTime()
  {
    return aDeleteTime;
  }

  /**
   * Returns the number of successful deletes performed by this MatrixBlock.
   * 
   * @return The number of successful deletes performed by this MatrixBlock.
   */
  public int getDeleteCount()
  {
    return aDeleteCount;
  }

  /**
   * Returns the number of unsuccessful delete attempts performed by this
   * MatrixBlock.
   * 
   * @return The number of unsuccessful delete attempts performed by this
   *         MatrixBlock.
   */
  public int getDeleteFailures()
  {
    return aDeleteFailures;
  }

  /**
   * Returns the read/write fail string containing any and all read/write
   * error messages accumulated during the read/write process. This string is
   * empty if no errors occurred.
   * 
   * @return The read/write fail string containing any and all read/write
   *         error messages accumulated during the read/write process.
   */
  public String getIOFailMessgString()
  {
    return aIOFailMessg;
  }
  
  /**
   * Cleares the read write fail string.
   */
  public void clearIOFailMessgString()
  {
    aIOFailMessg = "";
  }

  /**
   * Static function to set the IO operation fail limit.
   * 
   * @param rwFailLimit The new IO operation fail limit.
   */
  public static void setIOFailLimit(int ioFailLimit)
  {
    aIOFailLimit = ioFailLimit;
  }

  /**
   * Static function to set the IO operation fail thread sleep time (sec).
   * 
   * @param rwFailLimit The new IO operation fail thread sleep time (sec).
   */
  public static void setIOFailSleepTime(int ioFailSleepTime)
  {
    aIOCatchSleep = ioFailSleepTime;
  }

  /**
   * Returns the amount of time this MatrixBlock waited for an IO request to
   * complete. This will be zero unless the MatrixBlock is the derived class
   * MatrixBlockReference which is an inner class of a MatrixBlockManager.
   * 
   * @return The amount of time this MatrixBlock waited for an IO request to
   * complete.
   */
  public long getIOWaitTime()
  {
    return aIOWaitTime;
  }

  /**
   * Returns true if the transposed form of this block has been created
   * permanently in memory.
   * 
   * @return True if the transposed form of this block has been created
   *         permanently in memory.
   */
  public boolean isTransposeLoaded()
  {
    return (aTrnspSubBlks != null);
  }

  /**
   * The number of times this blocks was created (re-initialized) by calling
   * the function createBlock().
   * 
   * @return The number of times this blocks was created (re-initialized) by
   *         calling the function createBlock().
   */
  public int getCreationCount()
  {
    return aCreationCount;
  }

  /**
   * Returns the number of times this block has been transposed (excludes the
   * creation of the permanent transposed sub-blocks).
   * 
   * @return The number of times this block has been transposed
   */
  public int getTransposeCount()
  {
    return aTransposeCount;
  }

  /**
   * Returns the number of times this block has resized its sub-blocks.
   * 
   * @return The number of times this block has resized its sub-blocks.
   */
  public int getSubBlockResizeCount()
  {
    return aSubBlkResizeCount;
  }
  
  /**
   * Sets the MatrixBlock source and destination path (read and write path).
   *  
   * @param path The MatrixBlock source and destination path.
   */
  public void setPath(String path)
  {
    aSrcPath = aDstPath = path;
  }

  /**
   * Sets the MatrixBlock source and destination file header (read file header)
   * 
   * @param fileHeader The MatrixBlock source and destination file header.
   */
  public void setFileHeader(String fileHeader)
  {
    aSrcFilHdr = aDstFilHdr = fileHeader;
  }

  /**
   * Sets the MatrixBlock combined source and destination path/file header
   * (read path/file header).
   * 
   * @param pathFile The MatrixBlock combined source and destination path/file
   *                 header.
   */
  public void setPathFileHeader(String pathFile)
  {
    File f     = new File(pathFile);
    aSrcPath   = aDstPath   = f.getParent();
    aSrcFilHdr = aDstFilHdr = f.getName();
  }

  /**
   * Sets the MatrixBlock combined and destination path/file header
   * (read path/file header).
   * 
   * @param pathFile   The MatrixBlock combined source and destination path.
   * @param fileHeader The MatrixBlock combined source and destination file
   *                   header.
   */
  public void setPathFileHeader(String path, String fileHeader)
  {
    aSrcPath   = aDstPath   = path;
    aSrcFilHdr = aDstFilHdr = fileHeader;
  }
  
  /**
   * Sets the MatrixBlock source path (read path).
   *  
   * @param path The MatrixBlock source path.
   */
  public void setSourcePath(String path)
  {
    aSrcPath = path;
  }

  /**
   * Sets the MatrixBlock source file header (read file header)
   * 
   * @param fileHeader The MatrixBlock source file header.
   */
  public void setSourceFileHeader(String fileHeader)
  {
    aSrcFilHdr = fileHeader;
  }

  /**
   * Sets the MatrixBlock combined source path/file header (read path/file
   * header).
   * 
   * @param pathFile   The MatrixBlock combined source path.
   * @param fileHeader The MatrixBlock combined source file header.
   */
  public void setSourcePathFileHeader(String path, String fileHeader)
  {
    aSrcPath   = path;
    aSrcFilHdr = fileHeader;
  }

  /**
   * Sets the MatrixBlock combined source  path/file header (write path/file
   * header).
   * 
   * @param pathFilHdr The MatrixBlock combined source path/file header.
   */
  public void setSourcePathFileHeader(String pathFilHdr)
  {
    File f     = new File(pathFilHdr);
    aSrcPath   = f.getParent();
    aSrcFilHdr = f.getName();
  }

  /**
   * Returns the MatrixBlock source path (read path).
   * 
   * @return The MatrixBlock source path.
   */
  public String getSourcePath()
  {
    return aSrcPath;
  }

  /**
   * Returns the MatrixBlock "header" portion of the source path/file name
   * (read file header).
   * 
   * @return The MatrixBlock"header" portion of the source path/file name.
   */
  public String getSourceFileHeader()
  {
    return aSrcFilHdr;
  }

  /**
   * Returns the source path/file header string (without the row and column
   * index appended).
   * 
   * @return The source path/file header string (without the row and column
   *         index appended).
   */
  public String getSourcePathFileHeader()
  {
    return getPathFileHeader(aSrcPath, aSrcFilHdr);
  }

  /**
   * Returns the MatrixBlock source file name (read file name).
   * 
   * @return The MatrixBlock source file name.
   */
  public String getSourceFileName()
  {
    return getFileName(aSrcFilHdr, aBlkRow, aBlkCol);
  }

  /**
   * Returns the MatrixBlock source combined path/file name (read path/file
   * name).
   * 
   * @return The MatrixBlock source combined path/file name.
   */
  public String getSourcePathFileName()
  {
    return getPathFileName(aSrcPath, aSrcFilHdr, aBlkRow, aBlkCol);
  }

  /**
   * Returns true if the source path for this block is defined.
   *  
   * @return True if the source path for this block is defined.
   */
  public boolean isSourcePathDefined()
  {
    File f = new File(aSrcPath);
    return f.exists();
  }

  /**
   * Returns true if the source file for this block is defined.
   *  
   * @return True if the source file for this block is defined.
   */
  public boolean isSourcePathFileNameDefined()
  {
    File f = new File(getSourcePathFileName());
    return f.exists();
  }

  /**
   * Sets the MatrixBlock destination path (write path).
   *  
   * @param path The MatrixBlock destination path.
   */
  public void setDestinationPath(String path)
  {
    aDstPath = path;
  }

  /**
   * Sets the MatrixBlock destination file header (write file header)
   * 
   * @param fileHeader The MatrixBlock destination file header.
   */
  public void setDestinationFileHeader(String fileHeader)
  {
    aDstFilHdr = fileHeader;
  }

  /**
   * Sets the MatrixBlock combined destination path/file header (write path/file
   * header).
   * 
   * @param pathFilHdr The MatrixBlock combined destination path/file header.
   */
  public void setDestinationPathFileHeader(String pathFilHdr)
  {
    File f     = new File(pathFilHdr);
    aDstPath   = f.getParent();
    aDstFilHdr = f.getName();
  }

  /**
   * Sets the MatrixBlock combined destination path/file header (write path/file
   * header).
   * 
   * @param pathFile   The MatrixBlock combined destination path.
   * @param fileHeader The MatrixBlock combined destination file header.
   */
  public void setDestinationPathFileHeader(String path, String fileHeader)
  {
    aDstPath   = path;
    aDstFilHdr = fileHeader;
  }

  /**
   * Returns the MatrixBlock destination path (write path).
   * 
   * @return The MatrixBlock destination path.
   */
  public String getDestinationPath()
  {
    return aDstPath;
  }

  /**
   * Returns the MatrixBlock "header" portion of the destination path/file name
   * (write file header).
   * 
   * @return The MatrixBlock"header" portion of the destination path/file name.
   */
  public String getDestinationFileHeader()
  {
    return aDstFilHdr;
  }

  /**
   * Returns the destination path/file header string (without the row and column
   * index appended).
   * 
   * @return The destination path/file header string (without the row and column
   *         index appended).
   */
  public String getDestinationPathFileHeader()
  {
    return getPathFileHeader(aDstPath, aDstFilHdr);
  }

  /**
   * Returns the MatrixBlock destination file name (write file name).
   * 
   * @return The MatrixBlock destination file name.
   */
  public String getDestinationFileName()
  {
    return getFileName(aDstFilHdr, aBlkRow, aBlkCol);
  }

  /**
   * Returns the MatrixBlock destination combined path/file name (write
   * path/file name).
   * 
   * @return The MatrixBlock destination combined path/file name.
   */
  public String getDestinationPathFileName()
  {
    return getPathFileName(aDstPath, aDstFilHdr, aBlkRow, aBlkCol);
  }

  /**
   * Returns true if the destination path for this block is defined.
   *  
   * @return True if the destination path for this block is defined.
   */
  public boolean isDestinationPathDefined()
  {
    File f = new File(aDstPath);
    return f.exists();
  }

  /**
   * Returns true if the destination file for this block is defined.
   *  
   * @return True if the destination file for this block is defined.
   */
  public boolean isDestinationPathFileNameDefined()
  {
    File f = new File(getDestinationPathFileName());
    return f.exists();
  }

  /**
   * Static function that returns an arbitrary diagonal file name where the
   * diagonals file header and row are input.
   * 
   * @param filHdr The diagonal file name header.
   * @param row    The diagonal row (and column).
   * @return       The diagonal file name.
   */
  public static String getDiagonalFileName(String filHdr, int row)
  {
  	return filHdr + "_diag_" + row;
  }

  /**
   * Static function that returns an arbitrary block file name where the blocks
   * file header, row, and column are input.
   * 
   * @param filHdr The MatrixBlock file name header.
   * @param row    The MatrixBlock row.
   * @param col    The MatrixBlock column.
   * @return       The MatrixBlock file name.
   */
  public static String getFileName(String filHdr, int row, int col)
  {
    return filHdr + "_" + row + "_" + col;
  }

  /**
   * Returns that standard block file name given the input block type tag and
   * block index.
   * 
   * @param filHdr  The MatrixBlock file header (e.g. "chol", "fsub", etc.).
   * @param blkIndx The MatrixBlock block index.
   * @return The MatrixBlock file name.
   */
  public static String getFileName(String filHdr, int blkIndx)
  {
    int[] rc = MatrixBlock.getBlockRowCol(blkIndx);
    return getFileName(filHdr, rc[0], rc[1]);
  }

  /**
   * Return the assembled path/file header string (without block row and column)
   * appended.
   * 
   * @param pth    The MatrixBlock path.
   * @param filHdr The MatrixBlock file header.
   * @return       The assembled MatrixBlock path/file header string.
   */
  public static String getPathFileHeader(String pth, String filHdr)
  {
    return pth + File.separator + filHdr;
  }

  /**
   * Static function that returns an arbitrary block path/file name where the
   * blocks path, file header, row, and column are input.
   * 
   * @param path   The MatrixBlock path.
   * @param filHdr The MatrixBlock file name header.
   * @param row    The MatrixBlock row.
   * @param col    The MatrixBlock column.
   * @return       The MatrixBlock file name.
   */
  public static String getPathFileName(String path, String filHdr,
                                       int row, int col)
  {
    return getPathFileHeader(path, getFileName(filHdr, row, col));
  }

  /**
   * Static function that returns an arbitrary block path/file name where the
   * blocks path, file header, row, and column are input.
   * 
   * @param path    The MatrixBlock path.
   * @param filHdr  The MatrixBlock file name header.
   * @param blkIndx The MatrixBlock block index.
   * @return        The MatrixBlock file name.
   */
  public static String getPathFileName(String path, String filHdr, int blkIndx)
  {
    int[] rc = MatrixBlock.getBlockRowCol(blkIndx);
    return getPathFileName(path, filHdr, rc[0], rc[1]);
  }

  /**
   * Static function that returns an arbitrary block path/file name where the
   * blocks path, file header, row, and column are input.
   * 
   * @param pathFilHdr The MatrixBlock path/file header.
   * @param row        The MatrixBlock row.
   * @param col        The MatrixBlock column.
   * @return           The MatrixBlock file name.
   */
  public static String getPathFileName(String pathFilHdr,
                                       int row, int col)
  {
    return getFileName(pathFilHdr, row, col);
  }

  /**
   * Static function that returns an arbitrary block path/file name where the
   * blocks path, file header, row, and column are input.
   * 
   * @param pathFilHdr The MatrixBlock path/file header.
   * @param blkIndx    The MatrixBlock block index.
   * @return           The MatrixBlock file name.
   */
  public static String getPathFileName(String pathFilHdr, int blkIndx)
  {
    return getFileName(pathFilHdr, blkIndx);
  }

  /**
   * Returns the number of sub-block data loads currently in existence.
   * @return
   */
  public static int getDataSubBlockLoads()
  {
    return aDataLoads;
  }

  /**
   * Returns the largest number of data sub-blocks loaded during the existence
   * of any MatrixBlock.
   * 
   * @return The largest number of data sub-blocks loaded during the existence
   *         of any MatrixBlock.
   */
  public static int getMaxDataSubBlockLoads()
  {
    return aMaxDataLoads; 
  }

  /**
   * Synchronized method to increment data block loads.
   */
  private static synchronized void incrementDataBlockLoads()
  {
    ++aDataLoads;
    if (aMaxDataLoads < aDataLoads) aMaxDataLoads = aDataLoads;
  }

  /**
   * Synchronized method to decrement data block loads.
   */
  private static synchronized void decrementDataBlockLoads()
  {
    --aDataLoads;
  }

  /**
   * Returns the matrix block definition object.
   * 
   * @return The matrix block definition object.
   */
  public MatrixBlockDefinition getMatrixBlockDefinition()
  {
    return aMtrxBlkDefn;
  }

  /**
   * Returns the block row index.
   * 
   * @return The block row index.
   */
  public int getBlockRow()
  {
    return aBlkRow;
  }

  /**
   * Returns the block column index.
   * 
   * @return The block column index.
   */
  public int getBlockColumn()
  {
    return aBlkCol;
  }

  /**
   * Returns a single unique index for this block based on a row
   * priority ordering of blocks in the matrix.
   * 
   * @return A single unique index for this block
   */
  public int getBlockIndex()
  {
    return getBlockIndex(aBlkRow, aBlkCol);
  }

  /**
   * Static function that returns the block index given the input row and
   * column.
   *  
   * @param row Matrix block row.
   * @param col Matrix block column.
   * @return The block index given the input row and column.
   */
  public static int getBlockIndex(int row, int col)
  {
    return row * (row + 1) / 2 + col;    
  }

  /**
   * Returns the row,col pair (as an int array with 0=row and 1=column) given
   * the input block index. This is the inverse function to
   *    getBlockIndex(row, col).
   *    
   * @param blkIndex The input block index 
   * @return The row, column array.
   */
  public static int[] getBlockRowCol(int blkIndex)
  {
    // initialize to [0,0] ... return if input blkIndex <= 0

    int[] rc = {0, 0};
    if (blkIndex <= 0) return rc;

    // calcate row index within one. If it exceeds block index decrement by 1
    
    rc[0] = (int) Math.sqrt(2 * blkIndex);
    if (rc[0] * (rc[0] + 1) / 2 > blkIndex) --rc[0];
    
    // calculate column and return

    rc[1] = blkIndex - rc[0] * (rc[0] + 1) / 2;
    return rc;
  }

  /**
   * Returns true if this block is a matrix diagonal block.
   * 
   * @return True if this block is a matrix diagonal block.
   */
  public boolean isDiagonalBlock()
  {
    return (aBlkRow == aBlkCol);
  }

  /**
   * Return the definition of this matrix block as a string
   */
  @Override
  public String toString()
  {
    return toString("");
  }

  /**
   * Return the definition of this matrix block as a string.
   * 
   * @param hdr The header added to the beginning of each line
   */
  public String toString(String hdr)
  {
    String s = hdr + "Matrix Block:" + NL +
               hdr + "  Matrix Block Row, Column       = " + aBlkRow + ", " + aBlkCol + NL +
               hdr + "  Matrix Element Row Counts:" + NL +
               hdr + "    Matrix                       = " + aMtrxBlkDefn.size() + NL +
               hdr + "    Block                        = " + aMtrxBlkDefn.blockSize() + NL +
               hdr + "    Sub-Block (basis)            = " +
               hdr + aMtrxBlkDefn.subBlockSizeBasis() + NL +
               hdr + "  Current Sub-Block Size         = " + aCurrSubBlkSize + NL +
               hdr + "  Transposed                     = " +
                     (aTranspose ? "Yes" : "No") + NL +
               hdr + "  Diagonal Block Filled          = " +
                     (aDiagBlkFilled ? "Yes" : "No") + NL +
               hdr + "  Source (Read) Path             = \"" + aSrcPath + "\"" + NL +
               hdr + "  Destination (Write) Path       = \"" + aDstPath + "\"" + NL +
               hdr + "  Sub-Blocks Loaded              = " +
                     (isBlockLoaded() ? "Yes" : "No") + NL +
               hdr + "  Transpose Sub-Blocks Loaded    = " +
                     ((isTransposeLoaded()) ? "Yes" : "No") + NL +
               hdr + "  Transpose Diag Block Filled    = " +
                     (aTrnspDiagBlkFilled ? "Yes" : "No") + NL +
               hdr + "  Sub-Block Lock Count           = " + aLock + NL +
               hdr + "  Transpose Sub-Block Lock Count = " + aTrnspLock + NL + NL;

    return s;
  }

  //****************************************************************************
  //**** get block data functionality
  //****************************************************************************

  /**
   * Returns the diagonal vector of this block. If this block is not a diagonal
   * block then null is returned.
   * 
   * @return The diagonal vector of this block or null if this block is not
   *         a diagonal block.
   */
  public double[] getBlockDiagonal()
  {
  	if (isDiagonalBlock())
  	{
  		double[] diag = new double [getBlockElementRows()];
  		for (int i = 0; i < diag.length; ++i)
  			diag[i] = getBlockDiagonalElement(i);

  		return diag;
  	}
  	else
  		return null;
  }

  /**
   * Returns the ith diagonal element of this block. Note that if this block is
   * not a diagonal block then the result returned is not a diagonal element of
   * the complete matrix for which this block is just a single component.
   * 
   * @param i The diagonal element index (row/column).
   * 
   * @return The ith diagonal element.
   */
  public double getBlockDiagonalElement(int i)
  {
    int si = i / aCurrSubBlkSize;
    int ii = i % aCurrSubBlkSize;
    return aSubBlks[si][si][ii][ii];
  }

  /**
   * Returns the i,jth element of this block.
   * @param i The ith block element row.
   * @param j The jth block element column.
   * @return The i,jth element of this block.
   */
  public double getBlockElement(int i, int j)
  {
    int si = i / aCurrSubBlkSize;
    int sj = j / aCurrSubBlkSize;
    int ii = i % aCurrSubBlkSize;
    int jj = j % aCurrSubBlkSize;
    return aSubBlks[si][sj][ii][jj];
  }

  /**
   * Returns the i,jth element of the si,sjth sub-block.
   * 
   * @param si The sub-block row for which the i,jth element will be returned.
   * @param sj The sub-block column for which the i,jth element will be
   *           returned.
   * @param i  The ith row of the sub-block containing the jth element.
   * @param j  The jth column of the ith row of the element to be returned.
   * @return The i,jth element of the si,sjth sub-block.
   */
  public double getSubBlockElement(int si, int sj, int i, int j)
  {
    return aSubBlks[si][sj][i][j];
  }

  /**
   * If the matrix block is sub-divided into a single sub-block then this
   * function returns the entire matrix. Otherwise it returns the first sub-
   * block column of the first sub-block row. This is a locking function that
   * prevents the sub-block from being modified while the lock is on. The
   * caller should call releaseLock(transpose) when the returned sub-block
   * array is no longer required.
   * 
   * @return The entire matrix block if sub-divided into a single sub-block
   *         or the first sub-block column of the first sub-block row
   *         otherwise.
   * @throws IOException 
   */
  public double[][] getLockedBlock(boolean transpose) throws IOException
  {
    return getLockedSubBlock(0, 0, transpose);
  }

  /**
   * Returns the entire sub-block array. This is a locking function that
   * prevents the sub-block from being modified while the lock is on. The
   * caller should call releaseLock(transpose) when the returned sub-block
   * array is no longer required.
   * 
   * @return The entire sub-block array.
   * @throws IOException 
   */
  public double[][][][] getLockedSubBlocks(boolean transpose) throws IOException
  {
    return getLockedSubBlocks(transpose, (new Date()).getTime());
  }

  /**
   * Returns the si,sjth sub-block of this block. This is a locking function
   * that prevents the sub-block from being modified while the lock is on. The
   * caller should call releaseLock(transpose) when the returned sub-block
   * array is no longer required.
   * 
   * @param si The sub-block row.
   * @param sj The sub-block column.
   * @return The si,sjth sub-block of this block.
   * @throws IOException 
   */
  public double[][] getLockedSubBlock(int si, int sj, boolean transpose)
         throws IOException
  {
    double[][][][] subblks = getLockedSubBlocks(transpose,
                                                (new Date()).getTime());
    if (subblks == null)
      return null;
    else
      return subblks[si][sj];
  }
  
  /**
   * Returns the sub-blocks based on the requested transpose state and
   * increments the appropriate lock. If no data has been loaded yet a
   * source file load is attempted (if the file exists). If the file does not
   * exist null is returned. If the file is not loaded successfully an
   * IOException is returned. This is a low-level locking function that prevents
   * the sub-block from being modified while the lock is on. The caller should
   * call releaseLock(transpose) when the returned sub-block array is no longer
   * required.
   * 
   * @param transpose The requested transpose state.
   * @return The sub-block array determined from the input transpose flag.
   * @throws IOException 
   */
  private synchronized double[][][][]
          getLockedSubBlocks(boolean transpose, long blkTimeStrt)
          throws IOException
  {
    // see if any time was spent blocking

    long inTime = (new Date()).getTime() - blkTimeStrt; 
    if (inTime > 1) notifyIOBlocking(inTime);

    // if no data has been loaded and source exists then load ... otherwise
    // return null
    
    if (aSubBlks == null)
    {
      if (isSourcePathFileNameDefined())
      {
        requestIO();
        notifyIOAllocate(-aAllocatedMem, false);
        readBlockCatch();
        defineMemoryAllocation();
        notifyIOAllocate(aAllocatedMem, true);
        completedIO("READ", MatrixBlockFileServer.getPathTag(aSrcPath));
        if (aSubBlks == null)
        {
        	throw new IOException("Read Failed for Block " + aBlkRow +
        			                  "," + aBlkCol + " ...");
        }
      }
      else
      {
        if (aSubBlks == null)
        {
        	throw new IOException("Bad Source Path Name (" +
                                this.getSourcePathFileName() + ") for Block " +
                                aBlkRow + "," + aBlkCol + " ...");
        }
        return null;
      }
    }

    // get sub-block array and return

    return obtainFillReleaseSubBlocks("OBTAIN", transpose, (new Date()).getTime());
  }
  
  /**
   * Retrieves, releases, or fills the sub-blocks of this MatrixBlock given
   * the input mode. If retrieval is requested (mode = "OBTAIN") a lock is set
   * on return. The sub-blocks are accessed based on the requested transpose
   * state and the appropriate lock is incremented. This is a low-level locking
   * function that prevents the sub-block from being modified while the lock is
   * on. The caller should call releaseLock(transpose) when the returned
   * sub-block array is no longer required.
   * 
   * If lock release is called (mode = "RELEASE") the lock (transpose or normal)
   * is decremented. If no more locks exist and the owning MatrixBlockDefinition
   * has changed the sub-block size the function changeSubBlockSize() is called
   * to perform the action before exiting.
   * 
   * If triangular fill is called (mode = "FILL") function fillSymmetric() is
   * called to fill the empty half of the sub-block if this is a diagonal block.
   * 
   * @param  mode The requested mode ("OBTAIN", "RELEASE", or "FILL").
   * @param  transpose The requested transpose state.
   * @return The sub-block array determined from the input transpose flag if
   *         the mode is "OBTAIN", otherwise null.
   * @throws IOException If mode is not recognized.
   */
  private synchronized double[][][][]
          obtainFillReleaseSubBlocks(String mode, boolean transpose,
                                     long blkTimeStrt)
          throws IOException
  {
    // first see if any time was spent blocking

    long inTime = (new Date()).getTime() - blkTimeStrt; 
    if (inTime > 1) notifyIOBlocking(inTime);

    // see what mode is input

    if (mode.equals("OBTAIN"))
    {
      // get sub-blocks given input transpose flag, and current lock state
  
      if (transpose == aTranspose)
      {
        // requested transpose state is the same as the primary sub-blocks ...
        // increment the lock and return sub-blocks
  
        ++aLock;
        if (aSubBlks == null)
        {
        	throw new IOException("Null Sub-Blocks for " + aBlkRow +
        			                  "," + aBlkCol + " transpose = " +
        			                  transpose + " ...");
        }
        return aSubBlks;
      }
      else if (isTransposeLoaded())
      {
        // transposed sub-blocks exist ... increment the transpose sub-block
        // lock and return the transposed sub-blocks
  
        ++aTrnspLock;
        if (aTrnspSubBlks == null)
        {
        	throw new IOException("Null Sub-Blocks (aTrnspSubBlks) for " +
                                aBlkRow + "," + aBlkCol + " transpose = " +
        			                  transpose + " ...");
        }
        return aTrnspSubBlks;
      }
      else if (aLock == 0)
      {
        // no current lock on sub-blocks but the requested transpose state is
        // different and no transposed sub-blocks exist ... transpose the
        // sub-blocks, increment the lock, and return the sub-blocks.
  
        ++aLock;
        transpose();
        if (aSubBlks == null)
        {
        	throw new IOException("Null Sub-Blocks after transpose for " +
                                aBlkRow + "," + aBlkCol + " transpose = " +
        			                  transpose + " ...");
        }
        return aSubBlks;
      }
      else
      {
        // requested transpose state is different, sub-blocks have a lock, and
        // transposed sub-blocks do not exist ... create transposed sub-blocks,
        // increment transposed lock, and return transposed sub-blocks.
  
        ++aTrnspLock;
        notifyIOAllocate(-aAllocatedMem, false);
        createTransposedBlock();
        notifyIOAllocate(aAllocatedMem, true);
        if (aTrnspSubBlks == null)
        {
        	throw new IOException("Null Sub-Blocks (aTrnspSubBlks) after transpose for " +
                                aBlkRow + "," + aBlkCol + " transpose = " +
        			                  transpose + " ...");
        }
        return aTrnspSubBlks;
      }
    }
    else if (mode.equals("RELEASE"))
    {
      // release a lock

      if (transpose == aTranspose)
      {
        if (aLock > 0) --aLock;
      }
      else
      {
        if (aTrnspLock > 0) --aTrnspLock;
      }

      // if not locked and sub-block size has changed perform the change.

      if (!isLocked() && (aCurrSubBlkSize != aMtrxBlkDefn.subBlockSize()))
        changeSubBlockSize();

      return null; 
    }
    else if (mode.equals("FILL"))
    {
      // fill empty half of MatrixBlock if this is a diagonal block

      fillSymmetric();
      return null;
    }
    else
      throw new IOException("Error Unknown mode: " + mode);
  }

  /**
   * Called to fill the empty half of this MatrixBlock if it has not yet been
   * filled and it is a diagonal MatrixBlock.
   * @throws IOException
   */
  public void fillEmptyTriangular() throws IOException
  {
    obtainFillReleaseSubBlocks("FILL", false, (new Date()).getTime());
  }

  /**
   * Called by locking requesters of the sub-block data to release the lock.
   * If both aLock and aTrnspLock go to zero and the sub-block size has been
   * changed it is immediately updated.
   * 
   * @param transpose The transpose state for which the lock was requested.
   */
  public void releaseLock(boolean transpose) throws IOException
  {
    obtainFillReleaseSubBlocks("RELEASE", transpose, (new Date()).getTime());
  }

  /**
   * Returns true if any accessor holds a lock on this object.
   * 
   * @return True if any accessor holds a lock on this object.
   */
  public boolean isLocked()
  {
    return ((aLock > 0) || (aTrnspLock > 0));
  }

  //****************************************************************************
  //**** get block, sub-block, element count Functionality
  //****************************************************************************

  /**
   * Returns the total number of sub-block rows in this block.
   * 
   * @return The total number of sub-block rows in this block.
   */
  public int getBlockSubBlockRows()
  {
    return aMtrxBlkDefn.getBlockSubBlockRows(aBlkRow);
  }

  /**
   * Returns the number of sub-block element rows in sub-block sbi of this
   * block.
   * 
   * @param sbi The sub-block whose element row count will be returned.
   * @return The number of sub-block element rows in sub-block sbi of this
   *         block.
   */
  public int getBlockSubBlockElementRows(int sbi)
  {
    return aMtrxBlkDefn.getBlockSubBlockElementRows(aBlkRow, sbi);
  }

  /**
   * Returns the block element row count.
   * 
   * @return The block element row count.
   */
  public int getBlockElementRows()
  {
    return aMtrxBlkDefn.getBlockElementRows(aBlkRow);
  }

  /**
   * Returns true if this block is the last block row of the matrix.
   * 
   * @return True if this block is the last block row of the matrix.
   */
  public boolean isLastBlockRow()
  {
    return (aMtrxBlkDefn.getBlockElementRows(aBlkRow) !=
            aMtrxBlkDefn.blockSize());
  }

  /**
   * Returns true if this block is a final row of the matrix and the input
   * sub-block row is the last valid row of this block.
   * 
   * @param subBlkRow The sub-block row to be checked for last.
   * @return True if this block is a final row of the matrix and the input
   *         sub-block row is the last valid row of this block.
   */
  public boolean isLastSubBlockRow(int subBlkRow)
  {
    return (aMtrxBlkDefn.getBlockSubBlockElementRows(aBlkRow, subBlkRow) !=
            aMtrxBlkDefn.subBlockSize());
  }

  /**
   * Returns true if the sub-blocks are defined and loaded.
   * 
   * @return True if the sub-blocks are defined and loaded.
   */
  public boolean isBlockLoaded()
  {
    return (aSubBlks != null);
  }

  //****************************************************************************
  //**** fill/transpose Functionality
  //****************************************************************************

  /**
   * Transposes this block and defines it as the new block definition
   */
  public synchronized void transposePermanent()
  {
    if (isBlockLoaded())
    {
      transpose(aSubBlks);
      if (aTrnspSubBlks != null) transpose(aTrnspSubBlks);
    }
  }

  /**
   * Static function that transposes in-place the input
   * square matrix A.
   * 
   * @param A The symmetric matrix to be transposed.
   */
  public static void transpose(double[][] A)
  {
    double[] arowi, arowj;

    // get number of rows and loop over each row

    int n = A.length;
    for (int i = 1; i < n; ++i)
    {
      // get ith row and loop over all columns less than the row index
      // (lower triangular)

      arowi = A[i];
      for (int j = 0; j < i; ++j)
      {
        // swap lower triangular entry with upper triangular entry

        arowj      = A[j];
        double swp = arowi[j];
        arowi[j]   = arowj[i];
        arowj[i]   = swp;
      }
    }
  }

  /**
   * Transposes the input array aIn into the output array aOut.
   * 
   * @param aOut The output array that is the transpose of aIn.
   * @param aIn  The input array that will be transposed into aOut.
   */
  public static void assignTranspose(double[][] aOut, double[][] aIn)
  {
    double[] arowi;

    // get number of rows and loop over each row

    int n = aIn.length;
    for (int i = 0; i < n; ++i)
    {
      // get ith row and loop over all columns

      arowi = aIn[i];
      for (int j = 0; j < n; ++j)
      {
        // assign input element i,j to output element j,i
        aOut[j][i] = arowi[j];
      }
    }
  }

  /**
   * Returns true if this block is transposed.
   * 
   * @return True if this block is transposed.
   */
  public boolean isTransposed()
  {
    return aTranspose;
  }

  /**
   * Transposes the sub-blocks, flips the transpose flag, and increments the
   * transpose count.
   */
  private void transpose()
  {
    transpose(aSubBlks);

    // flip transpose flag and exit

    aTranspose = !aTranspose;
    ++aTransposeCount;
  }

  /**
   * Transposes this block in place. This function first transposes the elements
   * within each sub-block and then transposes the sub-blocks within the primary
   * container array aSubBlks. Each time this function is called the transpose
   * flag (aTranspose) is flipped.
   */
  public static void transpose(double[][][][] subBlks)
  {
    double[][] tmp;
    double[][][] subBlkRowI, subBlkRowJ;

    // loop over all sub-block rows for this block

    for (int i = 0; i < subBlks.length; ++i)
    {
      // get ith row of sub-block matrices and loop over all column entries
      // <= i.

      subBlkRowI = subBlks[i];
      for (int j = 0; j <= i; ++j)
      {
        // transpose the ith rows, jth column sub-block elements and see if
        // this is not a diagonal sub-block

        transpose(subBlkRowI[j]);
        if (j < i)
        {
          // not a diagonal sub-block ... get jth row of sub-block matrix and
          // transpose ith sub-block elements

          subBlkRowJ = subBlks[j];
          transpose(subBlkRowJ[i]);

          // swap the i,j sub-block with the j,i sub-block

          tmp = subBlkRowI[j];
          subBlkRowI[j] = subBlkRowJ[i];
          subBlkRowJ[i] = tmp;
        }
      }
    }
  }

  /**
   * Creates the transposed sub-blocks aTrnspSubBlks from the original set
   * (aSubBlks). This function is only called if the transposed set is needed
   * but the current state is locked so that aSubBlks cannot be transposed.
   */
  private void createTransposedBlock()
  {
    int nbsb = aSubBlks.length;
    int nsbe = aCurrSubBlkSize;
    aTrnspSubBlks = new double [nbsb][nbsb][nsbe][nsbe];
    for (int i = 0; i < aSubBlks.length; ++i)
    {
      double[][][] subBlksi = aSubBlks[i];
      for (int j = 0; j < aSubBlks.length; ++j)
      {
        double[][] ain  = subBlksi[j];
        double[][] aout = aTrnspSubBlks[j][i];
        assignTranspose(aout, ain);
      }
    }
    incrementDataBlockLoads();
    defineMemoryAllocation();
  }

  /**
   * Returns true if the sub-blocks exist, this is a diagonal block, and its
   * transpose has been filled.
   * 
   * @return True if this block lower triangular was filled from its upper.
   */
  public boolean isDiagonalBlockFilled()
  {
    return aDiagBlkFilled;
  }

  /**
   * Returns true if the transposed allocation is in place, this is a diagonal
   * block, and its transpose has been filled.
   * 
   * @return True if this block lower triangular was filled from its upper.
   */
  public boolean isTransposeDiagonalBlockFilled()
  {
    return aTrnspDiagBlkFilled;
  }

  /**
   * When a diagonal block is read only the upper triangular matrix is
   * populated. Sometimes it is desirable to have the entire matrix populated.
   * This function copies the upper triangular to the lower triangular elements
   * for all sub-blocks.
   */
  public synchronized void fillSymmetric()
  {
  	// only execute if this block is a diagonal block

    if (isDiagonalBlock())
    {
      if (!aDiagBlkFilled)
      {
      	// get number of block sub-blocks and sub-block elements and loop over
      	// each sub-block row of the block

        for (int i = 0; i < aSubBlks.length; ++i)
        {
        	// get the array of all sub-blocks for the ith row and fill the ith
        	// diagonal sub-blocks lower (aTranspose = false) or upper
          // (aTranspose = true) triangular elements

          double[][][] subBlki = aSubBlks[i];
          fillDiagSubBlockTriangularSymmetric(!aTranspose, subBlki[i]);

          // loop over all other sub-blocks before the diagonal sub-block and
          // fill their elements with the transpose of their upper-triangular
          // (aTranspose = false) or lower-triangular (aTranspose = true)
          // sub-block match.

          if (!aTranspose)
            for (int j = 0; j < i; ++j)
              fillSubBlockWithTranspose(subBlki[j], aSubBlks[j][i]);
          else
            for (int j = 0; j < i; ++j)
              fillSubBlockWithTranspose(aSubBlks[j][i], subBlki[j]);
        }

        aDiagBlkFilled = true;
      }

      // do the same for the transposed sub-blocks if they are defined

      if (!aTrnspDiagBlkFilled && isTransposeLoaded())
      {
        // get number of block sub-blocks and sub-block elements and loop over
        // each sub-block row of the block

        for (int i = 0; i < aTrnspSubBlks.length; ++i)
        {
          // get the array of all sub-blocks for the ith row and fill the ith
          // diagonal sub-blocks lower (aTranspose = true) or upper
          // (aTranspose = false) triangular elements

          double[][][] subBlki = aTrnspSubBlks[i];
          fillDiagSubBlockTriangularSymmetric(aTranspose, subBlki[i]);
          
          // loop over all other sub-blocks before the diagonal sub-block and
          // fill their elements with the transpose of their upper-triangular
          // (aTranspose = true) or lower-triangular (aTranspose = false)
          // sub-block match.

          if (aTranspose)
            for (int j = 0; j < i; ++j)
              fillSubBlockWithTranspose(subBlki[j], aTrnspSubBlks[j][i]);
          else
            for (int j = 0; j < i; ++j)
              fillSubBlockWithTranspose(aTrnspSubBlks[j][i], subBlki[j]);
        }

        // set fill flag and exit

        aTrnspDiagBlkFilled = true;
      }
    }
  }

  /**
   * Fills the input off-diagonal sub-block (sblk) with the transpose of the
   * "from" sub-block
   * 
   * @param sblk   The block that will be filled with the transpose of the
   *               elements from frmblk.
   * @param frmblk The block whose transpose will be used to fill sblk.
   */
  private static void fillSubBlockWithTranspose(double[][] sblk,
                                                double[][] frmblk)
  {
  	// get the length of the sub-block and loop over all rows

    int nr = sblk.length;
    for (int i = 0; i < nr; ++i)
    {
    	// get the ith row of elements and set the transpose entry of the "from"
    	// sub-block into the i,jth entry of sblk

      double[] sblki = sblk[i];
      for (int j = 0; j < nr; ++j) sblki[j] = frmblk[j][i];
    }
  }

  /**
   * Fills the lower triangular of sblk with its upper triangular entry.
   * 
   * @param sblk The sub-block whose lower triangular will be filled with its
   *             upper triangluar entries.
   */
  public static void fillDiagSubBlockLowerSymmetric(double[][] sblk)
  {
  	// get the length of the sub-block and loop over all rows

  	int nr = sblk.length;
    for (int i = 0; i < nr; ++i)
    {
    	// get the ith row of elements and set the transpose entry from the
    	// upper entry into  the i,jth entry of sblk

      double[] sblki= sblk[i];
      for (int j = 0; j < i; ++j) sblki[j] = sblk[j][i];
    }
  }

  /**
   * Fills the lower triangular of sblk with its upper triangular entry.
   * 
   * @param sblk The sub-block whose lower triangular will be filled with its
   *             upper triangluar entries.
   */
  public static void fillDiagSubBlockTriangularSymmetric(boolean lower,
                                                         double[][] sblk)
  {
    // get the length of the sub-block and loop over all rows

    int nr = sblk.length;
    for (int i = 0; i < nr; ++i)
    {
      // get the ith row of elements and set the transpose entry from the
      // upper entry into  the i,jth entry of sblk

      double[] sblki= sblk[i];
      if (lower) // fill lower
        for (int j = 0; j < i; ++j) sblki[j] = sblk[j][i];
      else // fill upper
        for (int j = 0; j < i; ++j) sblk[j][i] = sblki[j];
    }
  }

  //****************************************************************************
  //**** Read/Write/Rename/Delete/Create/Unload Functionality
  //****************************************************************************

  /**
   * A utility that assembles the diagonal entries for each sub-block written
   * across one or more file servers defined by mbfs and writes the resulting
   * total matrix diagonal at the path where matrix block 0_0 is stored.
   * The header is the string header with which each block begins.
   * 
   * @param mbfs The matrix block file server object which contains all paths
   * 						 into which the matrix blocks and their diagonals are written.
   * @param hdr  The header appended to the front of each block and diagonal file.
   * @throws IOException
   */
  public static void assembleMatrixDiagonal(MatrixBlockFileServer mbfs, String hdr) throws IOException
  {
  	// get the path where block 0_0 is stored

    int srvrIndex = mbfs.getServerIndex(0, 0);
    String fpDiag0 = mbfs.getPath(hdr, srvrIndex);

    // open trhe matrix block definition object and get the number of blocks and
    // the block size

    String fpth = fpDiag0 + File.separator + "matrixdefn";
		MatrixBlockDefinition mbd = new MatrixBlockDefinition(fpth);
		int nblks = mbd.blocks();
  	int bs = mbd.blockSize();

  	// create the diagonal vector ... loop over all diagonal blocks

		double[] diag = new double [mbd.size()];
    for (int i = 0; i < nblks; ++i)
    {
    	// get the path to diagonal block i_i

      srvrIndex = mbfs.getServerIndex(i, i);
      String fpDiag = mbfs.getPath(hdr, srvrIndex);

      // create the path/header name to the matrix block and get the ith 
    	// diagonal block file name and read in its diagonal

    	String mbpthhdr = fpDiag + File.separator + hdr;
    	String mbpth = MatrixBlock.getDiagonalFileName(mbpthhdr, i);
    	FileInputBuffer fib = new FileInputBuffer(mbpth);
    	double[] mbdiag = fib.readDoubles();
    	fib.close();

    	// define the start of the current block in the total diagonal and loop
    	// over all entries in the block diagonal and set them into the total
    	// diagonal

    	int diagstrt = bs * i;
    	for (int j = 0; j < mbdiag.length; ++j)
    		diag[diagstrt + j] = mbdiag[j];
    }

    // done assembling the matrix diagonal ... output it to the path fpDiag0
    // and exit

    FileOutputBuffer fob = new FileOutputBuffer(fpDiag0 + File.separator + hdr + "_diagonal");
    fob.writeDoubles(diag);
    fob.close();  	
  }

  /**
   * A utility that assembles the diagonal entries for each sub-block written
   * in a sub-block directory into a single diagonal vector for the complete
   * matrix. The input path gives the location of the blocks and block diagonals
   * of the matrix. The header is the string header with which each block begins.
   * Note: this method only works if a matrix is written to a single directory
   * (i.e. all blocks and diagonals are in a single directory).
   * 
   * @param pth The path to where the matrix blocks and diagonals reside.
   * @param hdr The header appended to the front of each block and diagonal file.
   * @throws IOException
   */
  public static void assembleMatrixDiagonal(String pth, String hdr) throws IOException
  {
		// read in the matrix block definition and get the number of blocks and the
		// block size

		String fpth = pth + File.separator + "matrixdefn";
		MatrixBlockDefinition mbd = new MatrixBlockDefinition(fpth);
		int nblks = mbd.blocks();
  	int bs = mbd.blockSize();

  	// create the path/header name to the matrix blocks and create the diagonal
  	// vector ... loop over all diagonal blocks

  	String mbpthhdr = pth + File.separator + hdr;
		double[] diag = new double [mbd.size()];
    for (int i = 0; i < nblks; ++i)
    {
    	// get the ith diagonal block file name and read in its diagonal

    	String mbpth = MatrixBlock.getDiagonalFileName(mbpthhdr, i);
    	FileInputBuffer fib = new FileInputBuffer(mbpth);
    	double[] mbdiag = fib.readDoubles();
    	fib.close();

    	// define the start of the current block in the total diagonal and loop
    	// over all entries in the block diagonal and set them into the total
    	// diagonal

    	int diagstrt = bs * i;
    	for (int j = 0; j < mbdiag.length; ++j)
    		diag[diagstrt + j] = mbdiag[j];
    }

    // done assembling the matrix diagonal ... output it to the path and exit

    FileOutputBuffer fob = new FileOutputBuffer(mbpthhdr + File.separator + "_diagonal");
    fob.writeDoubles(diag);
    fob.close();
  }

  /**
   * Reads this block at the source path (aSrcPath) using the error catch
   * mechanism to avoid time-out errors.
   * 
   * @throws IOException
   */
  public void readBlockCatch() throws IOException
  {
    readBlockCatch(getSourcePathFileHeader());
  }

  /**
   * Reads this block at file path pth using the error catch mechanism to avoid
   * time-out errors. If this function receives a read error while reading the
   * block it attempts to reread up to aReadWriteFailLimit times. After that an
   * IOException is thrown to the caller terminating this task.
   * 
   * @param pth The file path of the form "path/header".
   * @throws IOException
   */
  public synchronized void readBlockCatch(String pthFilHdr) throws IOException
  {
    // try reading block

    long strtTime = (new Date()).getTime();
    int ecnt = 0;
    Exception ex = null;
    while (true)
    {
      try
      {
        // read block ... if successful break out of while loop for return

        readBlock(pthFilHdr);
        break;
      }
      catch (Exception exc)
      {
        // unsuccessful ... increment count and try again ... after 
        // exceeding aReadWriteFailLimit throw error

        ++ecnt;
        ++aReadFailures;
        ex = exc;
        if (ecnt == aIOFailLimit)
        {
          throw new IOException(exc);
        }

        try
        {
          Thread.sleep(aIOCatchSleep * 1000);
        }
        catch (InterruptedException e)
        { }
      }
    }

    if (ecnt > 0)
    {
      // build error string and return

      aIOFailMessg += "MatrixBlock::readBlockCatch Failures: " + NL +
                      "  Host              = " +
                      (InetAddress.getLocalHost()).getHostName() + NL +
                      "  Host local time   = " + Globals.getTimeStamp() + NL +
                      "  Exception Count   = " + ecnt + NL +
                      "  Elapsed Time (s)  = " + 
                      (((new Date()).getTime() - strtTime) / 1000.0) + NL + 
                      "  Block Row, Column = " + aBlkRow + ", " + aBlkCol + NL +
                      "  Read Path         = \"" + pthFilHdr + "\"" + NL +
                      "  Exception: " + NL +
                      ex.toString() + NL;
    }

    // done ... set read time and exit

    aReadTime = (new Date()).getTime() - strtTime;
  }

  /**
   * Reads an old MatrixBlockInfo style block file. If this MatrixBlocks
   * MatrixBlockDefinition does not match the input MatrixBlockInfo an error
   * is thrown.
   * 
   * @param mbi The old MatrixBlockInfo object under whose prescription the old
   *            block was written.
   * @throws IOException
   */
  public void readBlock(MatrixBlockInfo mbi) throws IOException
  {
    readBlock(mbi, getSourcePathFileHeader());
  }

  /**
   * Reads an old block file written under the old input MatrixBlockInfo file
   * definition. The block is read as if it contains a single sub-block (the
   * old style was this way). After it is read it is transformed into the
   * sub-block structure set by the defining MatrixBlockDefinition object. The
   * block is read at the input file path ("path/header"). 
   * 
   * Reads this block from aSrcPath. Calls the private read block function to
   * actually perform the read.
   * 
   * @param mbi       The old MatrixBlockInfo object that defines the size of
   *                  the old style block to be read.
   * @param pthFilHdr The file path of the block of the form "path/header".
   * 
   * @throws IOException
   */
  public synchronized void readBlock(MatrixBlockInfo mbi,
                                     String pthFilHdr) throws IOException
  {
    // set read start time and path and create file input buffer

    long strtTime = (new Date()).getTime();
    String f = getPathFileName(pthFilHdr, aBlkRow, aBlkCol);
    FileInputBuffer fib = new FileInputBuffer(f);

    // assign sizes based on input MatrixBlockInfo (mbi) and set sub-block
    // size to all block elements ... call private read

    int rnme    = mbi.size();
    int rnbe    = mbi.blockSize();
    int rnsbe   = aMtrxBlkDefn.subBlockSizeBasis();
    aCurrSubBlkSize = rnbe;

    readBlock(fib, rnme, rnbe, rnsbe, strtTime);
  }

  /**
   * Reads this block from the input source path (aSrcPath).
   * 
   * @throws IOException
   */
  public void readBlock() throws IOException
  {
    readBlock(getSourcePathFileHeader());
  }

  /**
   * Reads this block from the file path pth. Calls the private read block
   * function to actually perform the read.
   * 
   * @param pthFilHdr The block file path of the form "path/header".
   * @throws IOException
   */
  public synchronized void readBlock(String pthFilHdr) throws IOException
  {
    // set read start time and path and create file input buffer

    long strtTime = (new Date()).getTime();
    FileInputBuffer fib = new FileInputBuffer(getPathFileName(pthFilHdr,
                                              aBlkRow, aBlkCol));

    // read sizes and call private read

    int rnme    = fib.readInt();
    int rnbe    = fib.readInt();
    int rnsbe   = fib.readInt();
    aCurrSubBlkSize = fib.readInt();

    readBlock(fib, rnme, rnbe, rnsbe, strtTime);
  }

  /**
   * Reads this block from aSrcPath as a transposed block matrix. They are read
   * in transposed format because LSINV generally deals with the outer index
   * in its innermost loop that way. So the transposed state is normal and the
   * transpose flag is set to false (not transposed for this state). So when
   * the block is actually transposed the true matrix is in its non-transposed
   * state. The matrix is read as a diagonal (symmetric) or off-diagonal matrix.
   * If diagonal we only represent the lower form which means we read its
   * transpose ... an upper triangular. If off-diagonal the entire block is
   * read unless this is the last block of a matrix in which case some of its
   * rows may be zero. In the transposed state this means some of its columns
   * may be zero.
   * 
   * In the code below we read the block differently depending on if it is a
   * diagonal block or not.
   * 
   * This function also adds this block to the owning MatrixBlockDefinition
   * observer list so that it will be notified of any sub-block size changes.
   * 
   * @param fib      The file input buffer opened to read this block.
   * @param rnme     The number of matrix element rows in this block. If
   *                 this value is different than the same value defined by the
   *                 MatrixBlockDefinition object that owns this block
   *                 (aMtrxBlkDefn) an error is thrown.
   * @param rnbe     The number of matrix block element rows in this block. If
   *                 this value is different than the same value defined by the
   *                 MatrixBlockDefinition object that owns this block
   *                 (aMtrxBlkDefn) an error is thrown.
   * @param rnsbe    The number of matrix block sub-block element rows (the
   *                 basis) in this block. If this value is different than the
   *                 same value defined by the MatrixBlockDefinition object
   *                 that owns this block (aMtrxBlkDefn) an error is thrown.
   * @param strtTime The start time of the beginning of the read set by the
   *                 specific public read function that called this private
   *                 read. 
   * @throws IOException
   */
  private void readBlock(FileInputBuffer fib, int rnme, int rnbe, int rnsbe,
                         long strtTime)
          throws IOException
  {
    // make sure MatrixBlockDefinition matches input sizes

    if (rnme != aMtrxBlkDefn.size())
    {
      String s = "Error: MatrixBlockDefinition size (" + aMtrxBlkDefn.size() +
                 ") is not equal to input size (" + rnme + ") ...";
      throw new IOException(s);
    }
    if (rnbe != aMtrxBlkDefn.blockSize())
    {
      String s = "Error: MatrixBlockDefinition block size (" +
                 aMtrxBlkDefn.blockSize() +
                 ") is not equal to input block size (" + rnbe + ") ...";
      throw new IOException(s);
    }
    if (rnsbe != aMtrxBlkDefn.subBlockSizeBasis())
    {
      String s = "Error: MatrixBlockDefinition sub-block size (" +
                 aMtrxBlkDefn.subBlockSizeBasis() +
                 ") is not equal to input sub-block size (" + rnsbe + ") ...";
      throw new IOException(s);
    }

    // set limits

    int nbsb     = aMtrxBlkDefn.blockSubBlocks(aCurrSubBlkSize);
    int nbsbLast = aMtrxBlkDefn.getBlockSubBlockRows(aBlkRow, aCurrSubBlkSize);
    int nsbe     = aMtrxBlkDefn.subBlockSize(aCurrSubBlkSize);

    // create sub-blocks and see if this block is a diagonal or not

    aSubBlks = new double [nbsb][nbsb][nsbe][nsbe];
    if (isDiagonalBlock())
    {
      // diagonal block ... loop over all sub-block rows (Note: only loop over
      // defined (non-zero) sub-blocks. If this block row is the final block
      // row then nbsbLast may be less than nbsb)
      //
      //   e.g. Last Block Sub-Block Definition
      //        nbsb = 6
      //        nbsbLast = 4
      //
      //   x x x x 0 0
      //   0 x x x 0 0
      //   0 0 x x 0 0
      //   0 0 0 x 0 0
      //   0 0 0 0 0 0
      //   0 0 0 0 0 0

      for (int i = 0; i < nbsbLast; ++i)
      {
        // get the ith row of sub-blocks and get the number of defined element
        // rows for that sub-block. If this is the last sub-block of the last
        // block then nsbeLast may be less than nsbe.

        double[][][] subBlki = aSubBlks[i];
        int nsbeLast = aMtrxBlkDefn.getBlockSubBlockElementRows(aBlkRow, i, aCurrSubBlkSize);

        // read the ith diagonal sub-block ... if other sub-blocks are defined
        // for this sub-block row then loop over each of those and read them
        // also

        readDiagSubBlock(subBlki[i], fib, nsbeLast);
        for (int j = i+1; j < nbsbLast; ++j)
        {
          // get the number of columns to be read and read the sub-block.
          // Usually the number of element columns in the sub-block will be nsbe
          // unless this is the last sub-block of the last block of the matrix.
          // In that case it may be less than nsbe (i.e. nsbeLast <= nsbe).

          nsbeLast = aMtrxBlkDefn.getBlockSubBlockElementRows(aBlkRow, j,
                                                              aCurrSubBlkSize);
          readOffDiagSubBlock(subBlki[j], fib, nsbe, nsbeLast);
        }
      }
    }
    else
    {
      // not a diagonal block ... loop over all sub-block rows (nbsb). (Note:
      // if this block is the last block row the number of sub-block columns
      // may be less than nbsb (nbsbLast <= nbsb). Only loop over those
      // defined sub-block columns as the empty sub-blocks are not written
      // to disk.)
      // 
      //   e.g. Last block sub-block definition
      //        nbsb = 8
      //        nbsbLast = 5
      //
      //   x x x x x 0 0 0
      //   x x x x x 0 0 0
      //   x x x x x 0 0 0
      //   x x x x x 0 0 0
      //   x x x x x 0 0 0
      //   x x x x x 0 0 0
      //   x x x x x 0 0 0
      //   x x x x x 0 0 0

      for (int i = 0; i < nbsb; ++i)
      {
        // get the ith sub-block row and loop over all remaining sub-blocks out
        // to nbsbLast which may be less than nbsb if aBlkRow is the last row
        // of the matrix.

        double[][][] subBlki = aSubBlks[i];
        for (int j = 0; j < nbsbLast; ++j)
        {
          // get the number of columns to be read and read the sub-block.
          // Usually the number of element columns in the sub-block will be nsbe
          // unless this is the last sub-block of the last block of the matrix.
          // In that case it may be less than nsbe (i.e. nsbeLast <= nsbe).

          int nsbeLast = aMtrxBlkDefn.getBlockSubBlockElementRows(aBlkRow, j,
                                                                  aCurrSubBlkSize);
          readOffDiagSubBlock(subBlki[j], fib, nsbe, nsbeLast);
        }
      }      
    }

    // close input file, set time, add block as observer of the matrix
    // block definitions, and subdivide if current sub-block size is not
    // requested.

    fib.close();
    aReadTime = (new Date()).getTime() - strtTime;
    ++aReadCount;
    aMtrxBlkDefn.addObserver(this);
    if (aCurrSubBlkSize != aMtrxBlkDefn.subBlockSize())
      changeSubBlockSize();
    
    // increment the data load and memory allocation and notify all observers
    // of the data load

    incrementDataBlockLoads();
    defineMemoryAllocation();
  }

  /**
   * Writes this blocks diagonal to the destination file path (aDstPath) using
   * the error catch mechanism to avoid time-out errors. If the block is not a
   * diagonal block then the method returns without error.
   * 
   * @throws IOException
   */
  public void writeDiagonalCatch() throws IOException
  {
  	writeDiagonalCatch(getDestinationPathFileHeader());
  }

  /**
   * Writes this blocks diagonal to the file path pthFilHdr using the error
   * catch mechanism to avoid time-out errors. If this function receives a write
   * error while writing the diagonal it attempts to rewrite up to
   * aReadWriteFailLimit times. After that an IOException is thrown to the
   * caller terminating this task. If the block is not a diagonal block then
   * the method returns without error.
   * 
   * @param pthFilHdr The file path of the form "path/header".
   * @throws IOException
   */
  public synchronized void writeDiagonalCatch(String pthFilHdr) throws IOException
  {
  	// exit if not a diagonal block
  	
  	double[] diag = getBlockDiagonal();
  	if (diag == null) return;

    // try writing diagonal

    String f = getDiagonalFileName(pthFilHdr, aBlkRow);
    long strtTime = (new Date()).getTime();
    int ecnt = 0;
    Exception ex = null;
    while (true)
    {
      try
      {
        // write diagonal ... if successful break out of while loop for return

        FileOutputBuffer fob = new FileOutputBuffer(f);
        fob.writeDoubles(diag);
        fob.close();
        break;
      }
      catch (Exception exc)
      {
        // unsuccessful ... increment count and try again ... after 
        // exceeding aReadWriteFailLimit throw error

        ++ecnt;
        ++aWriteFailures;
        ex = exc;
        if (ecnt == aIOFailLimit)
        {
          throw new IOException(exc);
        }

        try
        {
          Thread.sleep(aIOCatchSleep * 1000);
        }
        catch (InterruptedException e)
        { }
      }
    }

    if (ecnt > 0)
    {
      // build error string and return

      aIOFailMessg += "MatrixBlock::writeDiagonalCatch Failures: " + NL +
                      "  Host              = " +
                      (InetAddress.getLocalHost()).getHostName() + NL +
                      "  Host local time   = " + Globals.getTimeStamp() + NL +
                      "  Exception Count   = " + ecnt + NL +
                      "  Elapsed Time (s)  = " + 
                      (((new Date()).getTime() - strtTime) / 1000.0) + NL + 
                      "  Block Row, Column = " + aBlkRow + ", " + aBlkCol + NL +
                      "  Write Path        = \"" + pthFilHdr + "\"" + NL +
                      "  Exception: " + NL +
                      ex.toString() + NL;
    }

    // done ... exit
  }

  /**
   * Writes this blocks diagonal to the destination file path (aDstPath).
   * If the block is not a diagonal block then the method returns without
   * error.
   * 
   * @throws IOException
   */
  public void writeDiagonal() throws IOException
  {
    writeDiagonal(getDestinationPathFileHeader());
  }

  /**
   * Writes this blocks diagonal to the input path (pthFilhdr). If the block is
   * not a diagonal block the method returns without error.
   * 
   * @param pthFilHdr The file path into which this blocks diagonal will be
   * 									written (of the form "path/header").
   * @throws IOException
   */
  public synchronized void writeDiagonal(String pthFilHdr) throws IOException
  {
  	double[] diag = getBlockDiagonal();
  	if (diag == null) return;

    String f = getDiagonalFileName(pthFilHdr, aBlkRow);
    FileOutputBuffer fob = new FileOutputBuffer(f);
    fob.writeDoubles(diag);
    fob.close();
  }

  /**
   * Writes this block to the destination path (aDstPath) using the error catch
   * mechanism to avoid time-out errors.
   * 
   * @throws IOException
   */
  public void writeBlockCatch() throws IOException
  {
    writeBlockCatch(getDestinationPathFileHeader());
  }

  /**
   * Writes this block to the file path pth using the error catch mechanism to
   * avoid time-out errors. If this function receives a write error while
   * writing the block it attempts to rewrite up to aReadWriteFailLimit times.
   * After that an IOException is thrown to the caller terminating this task.
   * 
   * @param pth The file path of the form "path/header".
   * @throws IOException
   */
  public synchronized void writeBlockCatch(String pthFilHdr) throws IOException
  {
    // try writing block

    long strtTime = (new Date()).getTime();
    int ecnt = 0;
    Exception ex = null;
    while (true)
    {
      try
      {
        // write block ... if successful break out of while loop for return

        writeBlock(pthFilHdr);
        break;
      }
      catch (Exception exc)
      {
        // unsuccessful ... increment count and try again ... after 
        // exceeding aReadWriteFailLimit throw error

        ++ecnt;
        ++aWriteFailures;
        ex = exc;
        if (ecnt == aIOFailLimit)
        {
          throw new IOException(exc);
        }

        try
        {
          Thread.sleep(aIOCatchSleep * 1000);
        }
        catch (InterruptedException e)
        { }
      }
    }

    if (ecnt > 0)
    {
      // build error string and return

      aIOFailMessg += "MatrixBlock::writeBlockCatch Failures: " + NL +
                      "  Host              = " +
                      (InetAddress.getLocalHost()).getHostName() + NL +
                      "  Host local time   = " + Globals.getTimeStamp() + NL +
                      "  Exception Count   = " + ecnt + NL +
                      "  Elapsed Time (s)  = " + 
                      (((new Date()).getTime() - strtTime) / 1000.0) + NL + 
                      "  Block Row, Column = " + aBlkRow + ", " + aBlkCol + NL +
                      "  Write Path        = \"" + pthFilHdr + "\"" + NL +
                      "  Exception: " + NL +
                      ex.toString() + NL;
    }

    // done ... set write time and exit

    aWriteTime = (new Date()).getTime() - strtTime;
  }

  /**
   * Writes this block to the destination file path (aDstPath).
   * 
   * @throws IOException
   */
  public void writeBlock() throws IOException
  {
    writeBlock(getDestinationPathFileHeader());
  }

  /**
   * Writes this block to pth as a transposed block matrix. The sub-blocks
   * are written in transposed format because LSINV generally deals with the
   * outer index in its innermost loop that way. So the transposed state is
   * normal and the transpose flag is set to false (not transposed for this
   * state). So when the block is actually transposed the true matrix is in its
   * non-transposed state. The matrix is written as a diagonal (symmetric) or
   * off-diagonal matrix. If diagonal we only represent the lower form which
   * means we write its transpose ... an upper triangular. If off-diagonal the
   * entire block is written unless this is the last block of a matrix in which
   * case some of its rows may be zero. In the transposed state this means some
   * of its columns may be zero.
   * 
   * In the code below we write the block differently depending on if it is a
   * diagonal block or not.
   * 
   * @param pthFilHdr The file path into which this block will be written (of
   *                  the form "path/header").
   * @throws IOException
   */
  public synchronized void writeBlock(String pthFilHdr) throws IOException
  {
    long strtTime = (new Date()).getTime();
    String f = getPathFileName(pthFilHdr, aBlkRow, aBlkCol);
    FileOutputBuffer fob = new FileOutputBuffer(f);

    // write matrix block definition validation values

    fob.writeInt(aMtrxBlkDefn.size());
    fob.writeInt(aMtrxBlkDefn.blockSize());
    fob.writeInt(aMtrxBlkDefn.subBlockSizeBasis());
    fob.writeInt(aCurrSubBlkSize);

    // set limits

    int nbsb     = aMtrxBlkDefn.blockSubBlocks(aCurrSubBlkSize);
    int nbsbLast = aMtrxBlkDefn.getBlockSubBlockRows(aBlkRow, aCurrSubBlkSize);
    int nsbe     = aMtrxBlkDefn.subBlockSize(aCurrSubBlkSize);

    // see if this block is a diagonal or not

    if (isDiagonalBlock())
    {
      // diagonal block ... loop over all sub-block rows (Note: only loop over
      // defined (non-zero) sub-blocks. If this block row is the final block
      // row then nbsbLast may be less than nbsb)
      //
      //   e.g. Last Block Sub-Block Definition
      //        nbsb = 6
      //        nbsbLast = 4
      //
      //   x x x x 0 0
      //   0 x x x 0 0
      //   0 0 x x 0 0
      //   0 0 0 x 0 0
      //   0 0 0 0 0 0
      //   0 0 0 0 0 0

      for (int i = 0; i < nbsbLast; ++i)
      {
        // get the ith row of sub-blocks and get the number of defined element
        // rows for that sub-block. If this is the last sub-block of the last
        // block then nsbeLast may be less than nsbe.

        double[][][] subBlki = aSubBlks[i];
        int nsbeLast = aMtrxBlkDefn.getBlockSubBlockElementRows(aBlkRow, i, aCurrSubBlkSize);

        // write the ith diagonal sub-block ... if other sub-blocks are defined
        // for this sub-block row then loop over each of those and write them
        // also

        writeDiagSubBlock(subBlki[i], fob, nsbeLast);
        for (int j = i+1; j < nbsbLast; ++j)
        {
          // get the number of columns to be written and write the sub-block.
          // Usually the number of element columns in the sub-block will be nsbe
          // unless this is the last sub-block of the last block of the matrix.
          // In that case it may be less than nsbe (i.e. nsbeLast <= nsbe).

          nsbeLast = aMtrxBlkDefn.getBlockSubBlockElementRows(aBlkRow, j, aCurrSubBlkSize);
          writeOffDiagSubBlock(subBlki[j], fob, nsbe, nsbeLast);
        }
      }
    }
    else
    {
      // not a diagonal block ... loop over all sub-block rows (nbsb). (Note:
      // if this block is the last block row the number of sub-block columns
      // may be less than nbsb (nbsbLast <= nbsb). Only loop over those
      // defined sub-block columns as the empty sub-blocks are not written
      // to disk.)
      // 
      //   e.g. Last block sub-block definition
      //        nbsb = 8
      //        nbsbLast = 5
      //
      //   x x x x x 0 0 0
      //   x x x x x 0 0 0
      //   x x x x x 0 0 0
      //   x x x x x 0 0 0
      //   x x x x x 0 0 0
      //   x x x x x 0 0 0
      //   x x x x x 0 0 0
      //   x x x x x 0 0 0

      for (int i = 0; i < nbsb; ++i)
      {
        // get the ith sub-block row and loop over all remaining sub-blocks out
        // to nbsbLast which may be less than nbsb if aBlkRow is the last row
        // of the matrix.

        double[][][] subBlki = aSubBlks[i];
        for (int j = 0; j < nbsbLast; ++j)
        {
          // get the number of columns to be written and write the sub-block.
          // Usually the number of element columns in the sub-block will be nsbe
          // unless this is the last sub-block of the last block of the matrix.
          // In that case it may be less than nsbe (i.e. nsbeLast <= nsbe).

          int nsbeLast = aMtrxBlkDefn.getBlockSubBlockElementRows(aBlkRow, j,
                                                                  aCurrSubBlkSize);
          writeOffDiagSubBlock(subBlki[j], fob, nsbe, nsbeLast);
        }
      }      
    }

    // close the file, increment the write count, and set the write time.

    fob.close();
    ++aWriteCount;
    aWriteTime = (new Date()).getTime() - strtTime;
  }

  /**
   * Renames this block from oldPthFilHdr to newPthFilHdr using the error catch
   * mechanism to avoid time-out errors. If this function receives an
   * unsuccessful rename while attempting to rename the block it will continue
   * to attempt to rename the block up to aIOFailLimit times. After that an
   * IOException is thrown to the caller terminating this task.
   * 
   * @param oldPthFilHdr The existing path/file header to be renamed.
   * @param newPthFilHdr The new name of the existing path/file header.
   * @throws IOException
   */
  public void renameBlockCatch(String oldPthFilHdr, String newPthFilHdr)
         throws IOException
  {
    long strtTime        = (new Date()).getTime();
    String oldPthFilName = getPathFileName(oldPthFilHdr, aBlkRow, aBlkCol);
    String newPthFilName = getPathFileName(newPthFilHdr, aBlkRow, aBlkCol);
    renameFileCatch(oldPthFilName, newPthFilName);
    aRenameTime = (new Date()).getTime() - strtTime;
  }

  /**
   * Renames this blocks diagonal from oldPthFilHdr to newPthFilHdr using the
   * error catch mechanism to avoid time-out errors. If this function receives an
   * unsuccessful rename while attempting to rename the file it will continue
   * to attempt to rename the file up to aIOFailLimit times. After that an
   * IOException is thrown to the caller terminating this task.
   * 
   * @param oldPthFilHdr The existing path/file header to be renamed.
   * @param newPthFilHdr The new name of the existing path/file header.
   * @throws IOException
   */
  public void renameDiagonalCatch(String oldPthFilHdr, String newPthFilHdr)
         throws IOException
  {
  	// exit if this is not a diagonal block
  	
  	if (!isDiagonalBlock()) return;

    // set state to reading and try reading block

    String oldPthFilName = getDiagonalFileName(oldPthFilHdr, aBlkRow);
    String newPthFilName = getDiagonalFileName(newPthFilHdr, aBlkRow);
    renameFileCatch(oldPthFilName, newPthFilName);
  }

  /**
   * Renames this blocks diagonal from oldPthFilHdr to newPthFilHdr using the
   * error catch mechanism to avoid time-out errors. If this function receives an
   * unsuccessful rename while attempting to rename the file it will continue
   * to attempt to rename the file up to aIOFailLimit times. After that an
   * IOException is thrown to the caller terminating this task.
   * 
   * @param oldPthFilHdr The existing path/file header to be renamed.
   * @param newPthFilHdr The new name of the existing path/file header.
   * @throws IOException
   */
  private synchronized void renameFileCatch(String oldPthFilName,
                                            String newPthFilName)
         throws IOException
  {
    long strtTime        = (new Date()).getTime();
    File oldFile         = new File(oldPthFilName);
    File newFile         = new File(newPthFilName);

    // try renaming block

    int ecnt = 0;
    Exception ex = null;
    while (true)
    {
      try
      {
        // rename block ... if successful break out of while loop for return

        oldFile.renameTo(newFile);
        break;
      }
      catch (Exception exc)
      {
        // unsuccessful ... increment count and try again ... after 
        // exceeding aReadWriteFailLimit throw error

        ++ecnt;
        ++aRenameFailures;
        ex = exc;
        if (ecnt == aIOFailLimit)
        {
          throw new IOException(exc);
        }

        try
        {
          Thread.sleep(aIOCatchSleep * 1000);
        }
        catch (InterruptedException e)
        { }
      }
    }

    if (ecnt > 0)
    {
      // build error string and return

      aIOFailMessg += "MatrixBlock::renameBlockCatch Failures: " + NL +
                      "  Host              = " +
                      (InetAddress.getLocalHost()).getHostName() + NL +
                      "  Host local time   = " + Globals.getTimeStamp() + NL +
                      "  Exception Count   = " + ecnt + NL +
                      "  Elapsed Time (s)  = " + 
                      (((new Date()).getTime() - strtTime) / 1000.0) + NL + 
                      "  Block Row, Column = " + aBlkRow + ", " + aBlkCol + NL +
                      "  Old Path          = \"" + oldPthFilName + "\"" + NL +
                      "  New Path          = \"" + oldPthFilName + "\"" + NL +
                      "  Exception: " + NL +
                      ex.toString() + NL;
    }
  }

  /**
   * Deletes this block given the input path/file name header using the error
   * catch mechanism to avoid time-out errors. If this function receives a
   * unsuccessful delete message while attempting to delete the block it will
   * continue to attempt to delete the block up to aIOFailLimit times. After
   * that an IOException is thrown to the caller terminating this task.
   * 
   * @param pthFilHdr Name of the MatrixBlock file to delete.
   * @throws IOException
   */
  public synchronized void deleteBlockCatch(String pthFilHdr)
         throws IOException
  {
    // try deleting block ... exit if it does not exist

    long strtTime = (new Date()).getTime();
    String pthFilName = getPathFileName(pthFilHdr, aBlkRow, aBlkCol);
    File f = new File(pthFilName);
    if (!f.exists())
    {
    	aDeleteTime = 0;
    	return;
    }

    int ecnt = 0;
    Exception ex = null;
    while (true)
    {
      try
      {
        // delete block ... if successful break out of while loop for return

        if (f.delete()) break;
      }
      catch (Exception exc)
      {
        // unsuccessful ... increment count and try again ... after 
        // exceeding aReadWriteFailLimit throw error

        ++ecnt;
        ++aDeleteFailures;
        ex = exc;
        if (ecnt == aIOFailLimit)
        {
          throw new IOException(exc);
        }

        try
        {
          Thread.sleep(aIOCatchSleep * 1000);
        }
        catch (InterruptedException e)
        { }
      }
    }

    if (ecnt > 0)
    {
      // build error string and return

      aIOFailMessg += "MatrixBlock::deleteBlockCatch Failures: " + NL +
                      "  Host              = " +
                      (InetAddress.getLocalHost()).getHostName() + NL +
                      "  Host local time   = " + Globals.getTimeStamp() + NL +
                      "  Exception Count   = " + ecnt + NL +
                      "  Elapsed Time (s)  = " + 
                      (((new Date()).getTime() - strtTime) / 1000.0) + NL + 
                      "  Block Row, Column = " + aBlkRow + ", " + aBlkCol + NL +
                      "  Delete Path       = \"" + pthFilName + "\"" + NL +
                      "  Exception: " + NL +
                      ex.toString() + NL;
    }

    // done ... set delete time and exit

    aDeleteTime = (new Date()).getTime() - strtTime;
  }

  /**
   * Reads the upper nr x nr triangular matrix into the input sub-block sblk.
   * 
   * @param sblk The sub-block to be populated (upper triangular only) from
   *             this symmetric read.
   * @param fib  The input file buffer from where the data is read.
   * @param nr   The number of row/columns of the upper triangular matrix to
   *             read. Note that the input sub-block may have additional rows
   *             and columns which are not overwritten with zeros.
   * @throws IOException
   */
  private void readDiagSubBlock(double[][] sblk, FileInputBuffer fib, int nr)
          throws IOException
  {
    // loop over each row
    
    for (int i = 0; i < nr; ++i)
    {
      // get the ith row of elements and loop over each upper triangular
      // element and read the value from fib and set the result into the jth
      // column
      
      double[] sblki = sblk[i];
      for (int j = i; j < nr; ++j) sblki[j] = fib.readDouble();
    }
  }

  /**
   * Reads the nr x nc matrix into the input sub-block sblk.
   * 
   * @param sblk The sub-block to be populated (left most nc columns only) from
   *             this read.
   * @param fib  The input file buffer from where the data is read.
   * @param nr   The number of rows in the sub-block.
   * @param nc   The number of columns in the sub-block. Note that nc may be
   *             less than or equal to nr.
   * @throws IOException
   */
  private void readOffDiagSubBlock(double[][] sblk, FileInputBuffer fib,
                                   int nr, int nc)
          throws IOException
  {
    // loop over each row
    
    for (int i = 0; i < nr; ++i)
    {
      // get the ith row of sub-block elements and loop over each reading the
      // value from the input file buffer and setting the result into the jth
      // column
      
      double[] sblki = sblk[i];
      for (int j = 0; j < nc; ++j) sblki[j] = fib.readDouble();
    }
  }

  /**
   * Writes the upper nr x nr triangular sub-block to the file output buffer
   * fob.
   * 
   * @param sblk The sub-block to be populated (upper triangular only) from
   *             this symmetric read.
   * @param fob  The output file buffer from where the data is written.
   * @param nr   The number of row/columns of the upper triangular matrix to
   *             written. Note that the input sub-block may have additional rows
   *             and columns which are not written to disk.
   * @throws IOException
   */
  private void writeDiagSubBlock(double[][] sblk, FileOutputBuffer fob, int nr)
          throws IOException
  {
    // loop over each row
    
    for (int i = 0; i < nr; ++i)
    {
      // get the ith row of elements and loop over each upper triangular
      // element and write the value of the jth column to the output file
      // buffer
      
      double[] sblki = sblk[i];
      for (int j = i; j < nr; ++j) fob.writeDouble(sblki[j]);
    }
  }

  /**
   * Writes the nr x nc sub-block into the output file buffer fob.
   * 
   * @param sblk The sub-block to be written (left most nc columns only) to
   *             the output file buffer fob.
   * @param fob  The output file buffer from where the data is written.
   * @param nr   The number of rows in the sub-block.
   * @param nc   The number of columns in the sub-block. Note that nc may be
   *             less than or equal to nr.
   * @throws IOException
   */
  private void writeOffDiagSubBlock(double[][] sblk, FileOutputBuffer fob,
                                   int nr, int nc)
          throws IOException
  {
    // loop over each row
    
    for (int i = 0; i < nr; ++i)
    {
      // get the ith row of sub-block elements and loop over each writing the
      // value of the jth column to the output file buffer
      
      double[] sblki = sblk[i];
      for (int j = 0; j < nc; ++j) fob.writeDouble(sblki[j]);
    }
  }

  /**
   * Creates a new empty block to the specifications given by the
   * MatrixBlockDefinition object (aMtrxBlkDefn). This function adds this
   * block to the owning MatrixBlockDefinition observer list so that it will
   * be notified of any sub-block size changes.
   */
  public void createBlock()
  {
    createBlock(0.0);
  }

  /**
   * Creates a new empty block to the specifications given by the
   * MatrixBlockDefinition object (aMtrxBlkDefn). If this block is a diagonal
   * block then the diagonal values are set to the input value diagValue. This
   * function adds this block to the owning MatrixBlockDefinition observer list
   * so that it will be notified of any sub-block size changes.
   * 
   * @param diagValue The diagonal value set on the diagonal if this is a
   *                  diagonal block.
   */
  public synchronized void createBlock(double diagValue)
  {
    // set limits

    int nbsb     = aMtrxBlkDefn.blockSubBlocks();
    int nsbe     = aMtrxBlkDefn.subBlockSize();

    // create sub-blocks and see if this block is a diagonal or not

    aSubBlks = new double [nbsb][nbsb][nsbe][nsbe];
    if (isDiagonalBlock())
    {
      int nbsbLast = aMtrxBlkDefn.getBlockSubBlockRows(aBlkRow);
      for (int isb = 0; isb < nbsbLast; ++ isb)
      {
        double[][] subBlksDiag = aSubBlks[isb][isb];
        int nsbeLast = aMtrxBlkDefn.getBlockSubBlockElementRows(aBlkRow, isb);
        for (int i = 0; i < nsbeLast; ++ i) subBlksDiag[i][i] = diagValue;
      }
    }
    incrementDataBlockLoads();

    // set subdivide settings and add this block as an observer for the owning
    // matrix block definitions subdivide change event

    aCurrSubBlkSize = aMtrxBlkDefn.subBlockSize();
    ++aCreationCount;
    aMtrxBlkDefn.addObserver(this);
    defineMemoryAllocation();
  }

  /**
   * Sets the block pointer to null to effectively unload the data. This
   * function also removes this block from its owning MatrixBlockDefinition
   * observer list. This function should always be called when a block is
   * no longer needed so that it can be garbage collected.
   */
  public synchronized void unLoad() throws IllegalStateException
  {
    // throw error if locked or referenced

    if (isLocked())
    {
      String s = "Error: Can't unload sub-blocks ... ";
      if (aLock > 0)
        s += "aLock is not zero (" + aLock + ") ...";
      else
        s += "aTrnspLock is not zero (" + aTrnspLock + ") ...";
      throw new IllegalStateException(s);
    }

    if (aSubBlks != null)
    {
      aMtrxBlkDefn.removeObserver(this);
      decrementDataBlockLoads();
      aSubBlks        = null;
    }

    if (isTransposeLoaded())
    {
      decrementDataBlockLoads();
      aTrnspSubBlks        = null;
    }

    aCurrSubBlkSize = 0;
    aTranspose      = false;
    aDiagBlkFilled    = false;
    aTrnspDiagBlkFilled = false;
    defineMemoryAllocation();
  }

  public long getMemoryAllocation()
  {
    return aAllocatedMem;
  }

  /**
   * Private function called to define the actual allocated memory (in-core)
   * of this MatrixBlock. This function is only called when aSubBlks or
   * aTrnspsSubBlks are defined (or redefined).
   */
  private void defineMemoryAllocation()
  {
    // see if aSubBlks is defined ... if not set allocated memory to 0

    if (aSubBlks == null)
      aAllocatedMem = 0;
    else
    {
      // sub-blocks are defined ... add double storage

      long lng = aSubBlks.length * aSubBlks[0][0].length;
      aAllocatedMem = lng * lng;
      if (aTrnspSubBlks != null)
        aAllocatedMem *= 2;
      aAllocatedMem *= Double.SIZE / 8;

      // now add pointers

      long pntrs = (Long.SIZE / 8) *
                   aSubBlks.length * (lng + aSubBlks.length + 1);
      aAllocatedMem += pntrs;
      if (aTrnspSubBlks != null)
        aAllocatedMem += pntrs;
    }
    
    // done add base memory and exit

    aAllocatedMem += getBaseMemoryAllocation();
  }

  /**
   * Returns the small amount of base memory used by the MatrixBlock excluding
   * the sub-block and transposed sub-block memory.
   * 
   * @return The small amount of base memory used by the MatrixBlock excluding
   *         the sub-block and transposed sub-block memory.
   */
  private long getBaseMemoryAllocation()
  {
    return aMtrxBlkDefn.memoryEstimate() +
           16 * Integer.SIZE / 8 +
           6 * Long.SIZE / 8 +
           2  + // booleans
           aDstFilHdr.length() + aDstPath.length() + aSrcFilHdr.length() +
           aSrcPath.length() + aIOFailMessg.length();
  }

  //****************************************************************************
  //**** Change Sub-Block Size Functionality
  //****************************************************************************

  /**
   * Called by owning MatrixBlockDefinition object to change the sub-block size
   * of all of its instantiated MatrixBlock allocations. The purpose of this
   * function is to redefine the current sub-block size definition to that
   * requested in the MatrixBlockDefinition object. The new sub-block
   * structure is then filled from the old using one of two functions. The
   * functions differ in that in one case the new sub-blocks are larger than
   * the current and the opposite occurs for the other case.
   */
  protected void changeSubBlockSize()
  {
    // get requested sub-block element row count ...
    // exit if locked or sub-block size is equivalent to the definition

    int nsbeReq = aMtrxBlkDefn.subBlockSize();
    if (isLocked() || (aCurrSubBlkSize == nsbeReq)) return;

    // get the requested block sub-block count and create the new
    // sub-block array

    int nbsbReq = aMtrxBlkDefn.blockSubBlocks();
    double[][][][] newSubBlks = new double [nbsbReq][nbsbReq][nsbeReq][nsbeReq];
    
    // call the appropriate function based on which sub-block row count is
    // larger ... the requested or the current

    if (aCurrSubBlkSize < nsbeReq)
      // requested sub-block size is larger ... call assign larger sub-block
      //function
      assignLargerSubBlock(newSubBlks, aSubBlks,
                           aMtrxBlkDefn.blockSubBlocks(aCurrSubBlkSize),
                           aCurrSubBlkSize, nsbeReq);
    else
      // current sub-block size is larger ... call assign smaller sub-block
      // function
      assignSmallerSubBlock(aSubBlks, newSubBlks,
                            aMtrxBlkDefn.blockSubBlocks(),
                            nsbeReq, aCurrSubBlkSize);

    // done ... assign new sub-blocks and its size and increement resize count

    aCurrSubBlkSize = nsbeReq;
    aSubBlks = newSubBlks;    
    ++aSubBlkResizeCount;

    // if transposed sub-blocks existed then retranspose for the new sub-block
    // size

    if (isTransposeLoaded()) createTransposedBlock();
    defineMemoryAllocation();
  }

  /**
   * Assigns the input larger sub-block size sub-block array from the smaller
   * sub-block size sub-block array. The primary loop occurs over the smaller
   * blocks which represent the current allocation. The larger sub-blocks
   * represent the new requested allocation.
   * 
   * let the current (Smaller = sml) small block have nsbeSml elements
   * let the requested (Larger = lrg) large block have nsbeLrg elements
   * then
   * 
   *     nsbeSml < nsbeLrg
   * 
   * looping over all small sub-blocks we arrive at sub-block s0i,s0j
   * the block element row (column) for these two are given by
   * 
   *     br = si0 * nsbeSml + i0
   *     bc = sj0 * nsbeSml + j0
   * 
   * we can find the equivalent indices for the new block row and element row as
   * 
   *     si1 * nsbeLrg + i1 = br = si0 * nsbeSml + i0
   *     si1 * nsbeLrg + j1 = bc = sj0 * nsbeSml + j0
   * 
   * we care about finding the required indices over the large sub-block.
   * we can find the first and last block row (and col) from the small
   * sub-block as
   * 
   *     brFrst = si0 * nsbeSml
   *     brLast = (si0+1) * nsbeSml - 1
   *     bcFrst = sj0 * nsbeSml
   *     bcLast = (sj0+1) * nsbeSml - 1
   * 
   * the corresponding entries for the new block are
   * 
   *     si1F * nsbeLrg + i1F = brFrst  ==>
   *       si1F = brFrst / nsbeLrg;  i1F = brFrst % nsbeLrg;
   *     si1L * nsbeLrg + i1L = brLast  ==>
   *       si1L = brLast / nsbeLrg;  i1L = brLast % nsbeLrg;
   *     sj1F * nsbeLrg + j1F = bcFrst  ==>
   *       sj1F = bcFrst / nsbeLrg;  j1F = bcFrst % nsbeLrg;
   *     sj1L * nsbeLrg + j1L = bcLast  ==>
   *       sj1L = bcLast / nsbeLrg;  j1L = bcLast % nsbeLrg;
   * 
   * 4 possible loop possibilities exist
   * 
   *     1) si1F = si1L     and sj1F = sj1L
   *        for loop over i0 = 0   to nsbeSml-1 we have si1=si1F and i1 = i1F to i1L
   *        for loop over j0 = 0   to nsbeSml-1 we have sj1=sj1F and j1 = j1F to j1L
   *     2) si1F = si1L     and sj1F = sj1L - 1
   *        for loop over i0 = 0   to nsbeSml-1 we have si1=si1F and i1 = i1F to i1L
   *        for loop over j0 = 0   to j0m-1   we have sj1=sj1F and j1 = j1F to nsbeLrg - 1
   *        for loop over j0 = j0m to nsbeSml-1 we have sj1=sj1L and j1 = 0   to j1L
   *     3) si1F = si1L - 1 and sj1F = sj1L
   *        for loop over i0 = 0   to i0m-1   we have si1=si1F and i1 = i1F to nsbeLrg - 1
   *        for loop over i0 = i0m to nsbeSml-1 we have si1=si1L and i1 = 0   to i1L
   *        for loop over j0 = 0 to nsbeSml-1   we have sj1=sj1F and j1 = j1F to j1L
   *     4) si1F = si1L - 1 and sj1F = sj1L - 1
   *        for loop over i0 = 0   to i0m-1   we have si1=si1F and i1 = i1F to nsbeLrg - 1
   *        for loop over i0 = i0m to nsbeSml-1 we have si1=si1L and i1 = 0   to i1L
   *        for loop over j0 = 0   to j0m-1   we have sj1=sj1F and j1 = j1F to nsbeLrg - 1
   *        for loop over j0 = j0m to nsbeSml-1 we have sj1=sj1L and j1 = 0   to j1L
   * 
   *        in all cases assign
   * 
   *            subBlkLrg[si1][sj1][i1][j1] = subBlkSml[si0][sj0][i0][j0];
   * 
   *        where
   * 
   *            (si1+1) * nsbeLrg = si0 * nsbeSml + i0m
   *            (sj1+1) * nsbeLrg = sj0 * nsbeSml + j0m
   *            
   *  new sub-blocks are larger ... looping is set up over old sub-blocks
   *  one at a time
   * 
   * @param lrgSubBlks New sub-block structure where the sub-block size exceeds
   *                   the current sub-block size.
   * @param smlSubBlks Current sub-block structure where the sub-block size is
   *                   < than the requested sub-block size.
   * @param nbsbSml    The current block sub-block row count.
   * @param nsbeSml    The current sub-block element row count.
   * @param nsbeLrg    The requested sub-block element row count.
   */
  private void assignLargerSubBlock(double[][][][] lrgSubBlks,
                                    double[][][][] smlSubBlks,
                                    int nbsbSml, int nsbeSml, int nsbeLrg)
  {
    // loop over the small sub-blocks with si, sj, i, and j
    // first the sub-block rows si
    
    for (int si = 0; si < nbsbSml; ++si)
    {
      // calculate the first and last block element row for the small sub-block
      // from which the large block sub-block rows (si1F and si1L) and sub-
      // block element rows (i1F and i1L) are calculated
      
      int brFrst = si * nsbeSml;
      int brLast = (si+1) * nsbeSml - 1;
      int si1F   = brFrst / nsbeLrg;
      int i1F    = brFrst % nsbeLrg;
      int si1L   = brLast / nsbeLrg;
      //int i1L    = brLast % nsbeLrg;
      
      // get the small sub-block array and loop over all sub-block columns

      double[][][] sml_si = smlSubBlks[si];
      for (int sj = 0; sj < nbsbSml; ++sj)
      {
        // calculate the first and last block element column for the small
        // sub-block from which the large block sub-block column (sj1F and
        // sj1L) and sub-block element columns (j1F and j1L) are calculated

        int bcFrst = sj * nsbeSml;
        int bcLast = (sj+1) * nsbeSml - 1;
        int sj1F   = bcFrst / nsbeLrg;
        int j1F    = bcFrst % nsbeLrg;
        int sj1L   = bcLast / nsbeLrg;
        //int ijL    = bcLast % nsbeLrg;

        // get the small sub-block array and loop over all sub-block columns
        
        double[][] sml_si_sj = sml_si[sj];
        
        // i loop decision (same large sub-block or two consecutive)
        
        if (si1F == si1L)
        {
          // one loop over i = 0 to nsbe0 and i1 = i1F to i1L

          double[][][] lrg_si = lrgSubBlks[si1F];
          for (int i = 0, i1 = i1F; i < nsbeSml; ++i, ++i1)
          {
            double[] sml_si_sj_i = sml_si_sj[i];
            
            // j loop decision (same large sub-block or two consecutive)
           
            if (sj1F == sj1L)
            {
              // one loop over j = 0 nsbe0 and  j1 = j1F to j1L

              double[] lrg_si_sj_i = lrg_si[sj1F][i1];
              for (int j = 0, j1 = j1F; j < nsbeSml; ++j, ++j1)
              {
                lrg_si_sj_i[j1] = sml_si_sj_i[j];
              }
            }
            else
            {
              // two j loops
              // first j loop from j = 0 to j0m-1 and j1 = j1F to nsbe0-1
              
              int j0m = sj1L * nsbeLrg - sj * nsbeSml;
              double[] lrg_si_sj_i = lrg_si[sj1F][i1];
              for (int j = 0, j1 = j1F; j < j0m; ++j, ++j1)
              {
                lrg_si_sj_i[j1] = sml_si_sj_i[j];
              }
              
              // second j loop from j = j0m to nsbe0-1 and j1 = 0 to j1L
              
              lrg_si_sj_i = lrg_si[sj1L][i1];
              for (int j = j0m, j1 = 0; j < nsbeSml; ++j, ++j1)
              {
                lrg_si_sj_i[j1] = sml_si_sj_i[j];
              }
            }
          } // end for (int i = 0, i1 = i1F; i < nsbe0; ++i, ++i1)
        }
        else
        {
          // two i loops
          // first i loop from i = 0 to i0m-1 and i1 = i1F to nsbe0-1
          
          int i0m = si1L * nsbeLrg - si * nsbeSml;
          double[][][] lrg_si = lrgSubBlks[si1F];
          for (int i = 0, i1 = i1F; i < i0m; ++i, ++i1)
          {
            double[] sml_si_sj_i = sml_si_sj[i];

            // j loop decision (same large sub-block or two consecutive)

            if (sj1F == sj1L)
            {
              // one loop over j = 0 nsbe0 and  j1 = j1F to j1L

              double[] lrg_si_sj_i = lrg_si[sj1F][i1];
              for (int j = 0, j1 = j1F; j < nsbeSml; ++j, ++j1)
              {
                lrg_si_sj_i[j1] = sml_si_sj_i[j];
              }
            }
            else
            {
              // two j loops
              // first j loop from j = 0 to j0m-1 and j1 = j1F to nsbe0-1
              
              int j0m = sj1L * nsbeLrg - sj * nsbeSml;
              double[] lrg_si_sj_i = lrg_si[sj1F][i1];
              for (int j = 0, j1 = j1F; j < j0m; ++j, ++j1)
              {
                lrg_si_sj_i[j1] = sml_si_sj_i[j];
              }
              
              // second j loop from j = j0m to nsbe0-1 and j1 = 0 to j1L
              
              lrg_si_sj_i = lrg_si[sj1L][i1];
              for (int j = j0m, j1 = 0; j < nsbeSml; ++j, ++j1)
              {
                lrg_si_sj_i[j1] = sml_si_sj_i[j];
              }
            }
          } // end for (int i = 0, i1 = i1F; i < i0m; ++i, ++i1)
          
          // second i loop from i = i0m to nsbe0-1 and i1 = 0 to i1L

          lrg_si = lrgSubBlks[si1L];
          for (int i = i0m, i1 = 0; i < nsbeSml; ++i, ++i1)
          {
            double[] sml_si_sj_i = sml_si_sj[i];

            // j loop decision (same large sub-block or two consecutive)

            if (sj1F == sj1L)
            {
              // one loop over j = 0 nsbe0 and  j1 = j1F to j1L

              double[] lrg_si_sj_i = lrg_si[sj1F][i1];
              for (int j = 0, j1 = j1F; j < nsbeSml; ++j, ++j1)
              {
                lrg_si_sj_i[j1] = sml_si_sj_i[j];
              }
            }
            else
            {
              // two j loops
              // first j loop from j = 0 to j0m-1 and j1 = j1F to nsbe0-1
              
              int j0m = sj1L * nsbeLrg - sj * nsbeSml;
              double[] lrg_si_sj_i = lrg_si[sj1F][i1];
              for (int j = 0, j1 = j1F; j < j0m; ++j, ++j1)
              {
                lrg_si_sj_i[j1] = sml_si_sj_i[j];
              }
              
              // second j loop from j = j0m to nsbe0-1 and j1 = 0 to j1L
              
              lrg_si_sj_i = lrg_si[sj1L][i1];
              for (int j = j0m, j1 = 0; j < nsbeSml; ++j, ++j1)
              {
                lrg_si_sj_i[j1] = sml_si_sj_i[j];
              }
            }
          } // end for (int i = i0m, i1 = 0; i < nsbe0; ++i, ++i1)
        } // end else if (si1F != si1L)
      } // end for (int sj = 0; sj < nbsb0; ++sj)
    } // end for (int si = 0; si < nbsb0; ++si)
  }

  /**
   * Assigns the input smaller sub-block size sub-block array from the larger
   * sub-block size sub-block array. The primary loop occurs over the smaller
   * blocks which represent the requested allocation. The larger sub-blocks
   * represent the current allocation.
   * 
   * let the requested (Smaller = sml) small block have nsbeSml elements
   * let the current (Larger = lrg) large block have nsbeLrg elements
   * then
   * 
   *     nsbeSml < nsbeLrg
   * 
   * looping over all small sub-blocks we arrive at sub-block s0i,s0j
   * the block element row (column) for these two are given by
   * 
   *     br = si0 * nsbeSml + i0
   *     bc = sj0 * nsbeSml + j0
   * 
   * we can find the equivalent indices for the new block row and element row as
   * 
   *     si1 * nsbeLrg + i1 = br = si0 * nsbeSml + i0
   *     si1 * nsbeLrg + j1 = bc = sj0 * nsbeSml + j0
   * 
   * we care about finding the required indices over the large sub-block.
   * we can find the first and last block row (and col) from the small
   * sub-block as
   * 
   *     brFrst = si0 * nsbeSml
   *     brLast = (si0+1) * nsbeSml - 1
   *     bcFrst = sj0 * nsbeSml
   *     bcLast = (sj0+1) * nsbeSml - 1
   * 
   * the corresponding entries for the new block are
   * 
   *     si1F * nsbeLrg + i1F = brFrst  ==>
   *       si1F = brFrst / nsbeLrg;  i1F = brFrst % nsbeLrg;
   *     si1L * nsbeLrg + i1L = brLast  ==>
   *       si1L = brLast / nsbeLrg;  i1L = brLast % nsbeLrg;
   *     sj1F * nsbeLrg + j1F = bcFrst  ==>
   *       sj1F = bcFrst / nsbeLrg;  j1F = bcFrst % nsbeLrg;
   *     sj1L * nsbeLrg + j1L = bcLast  ==>
   *       sj1L = bcLast / nsbeLrg;  j1L = bcLast % nsbeLrg;
   * 
   * 4 possible loop possibilities exist
   * 
   *     1) si1F = si1L     and sj1F = sj1L
   *        for loop over i0 = 0   to nsbeSml-1 we have si1=si1F and i1 = i1F to i1L
   *        for loop over j0 = 0   to nsbeSml-1 we have sj1=sj1F and j1 = j1F to j1L
   *     2) si1F = si1L     and sj1F = sj1L - 1
   *        for loop over i0 = 0   to nsbeSml-1 we have si1=si1F and i1 = i1F to i1L
   *        for loop over j0 = 0   to j0m-1   we have sj1=sj1F and j1 = j1F to nsbeLrg - 1
   *        for loop over j0 = j0m to nsbeSml-1 we have sj1=sj1L and j1 = 0   to j1L
   *     3) si1F = si1L - 1 and sj1F = sj1L
   *        for loop over i0 = 0   to i0m-1   we have si1=si1F and i1 = i1F to nsbeLrg - 1
   *        for loop over i0 = i0m to nsbeSml-1 we have si1=si1L and i1 = 0   to i1L
   *        for loop over j0 = 0 to nsbeSml-1   we have sj1=sj1F and j1 = j1F to j1L
   *     4) si1F = si1L - 1 and sj1F = sj1L - 1
   *        for loop over i0 = 0   to i0m-1   we have si1=si1F and i1 = i1F to nsbeLrg - 1
   *        for loop over i0 = i0m to nsbeSml-1 we have si1=si1L and i1 = 0   to i1L
   *        for loop over j0 = 0   to j0m-1   we have sj1=sj1F and j1 = j1F to nsbeLrg - 1
   *        for loop over j0 = j0m to nsbeSml-1 we have sj1=sj1L and j1 = 0   to j1L
   * 
   *        in all cases assign
   * 
   *            subBlkSml[si0][sj0][i0][j0] = subBlkLrg[si1][sj1][i1][j1];
   * 
   *        where
   * 
   *            (si1+1) * nsbeLrg = si0 * nsbeSml + i0m
   *            (sj1+1) * nsbeLrg = sj0 * nsbeSml + j0m
   *            
   *  new sub-blocks are smaller ... looping is set up over current sub-blocks
   *  one at a time
   * 
   * @param lrgSubBlks New sub-block structure where the sub-block size is <
   *                   the current sub-block size.
   * @param smlSubBlks Current sub-block structure where the sub-block size is
   *                   > than the requested sub-block size.
   * @param nbsbSml    The current block sub-block row count.
   * @param nsbeSml    The current sub-block element row count.
   * @param nsbeLrg    The requested sub-block element row count.
   */
  private void assignSmallerSubBlock(double[][][][] lrgSubBlks,
                                     double[][][][] smlSubBlks,
                                     int nbsbSml, int nsbeSml, int nsbeLrg)
  {
    // loop over the small sub-blocks with si, sj, i, and j
    // first the sub-block rows si
    
    for (int si = 0; si < nbsbSml; ++si)
    {
      // calculate the first and last block element row for the small sub-block
      // from which the large block sub-block rows (si1F and si1L) and sub-
      // block element rows (i1F and i1L) are calculated
      
      int brFrst = si * nsbeSml;
      int brLast = (si+1) * nsbeSml - 1;
      int si1F   = brFrst / nsbeLrg;
      int i1F    = brFrst % nsbeLrg;
      int si1L   = brLast / nsbeLrg;
      //int i1L    = brLast % nsbeLrg;
      
      // get the small sub-block array and loop over all sub-block columns

      double[][][] sml_si = smlSubBlks[si];
      for (int sj = 0; sj < nbsbSml; ++sj)
      {
        // calculate the first and last block element column for the small
        // sub-block from which the large block sub-block column (sj1F and
        // sj1L) and sub-block element columns (j1F and j1L) are calculated

        int bcFrst = sj * nsbeSml;
        int bcLast = (sj+1) * nsbeSml - 1;
        int sj1F   = bcFrst / nsbeLrg;
        int j1F    = bcFrst % nsbeLrg;
        int sj1L   = bcLast / nsbeLrg;
        //int ijL    = bcLast % nsbeLrg;

        // get the small sub-block array and loop over all sub-block columns

        double[][] sml_si_sj = sml_si[sj];
        
        // i loop decision (same large sub-block or two consecutive)
        
        if (si1F == si1L)
        {
          // one loop over i = 0 to nsbe0 and i1 = i1F to i1L

          double[][][] lrg_si = lrgSubBlks[si1F];
          for (int i = 0, i1 = i1F; i < nsbeSml; ++i, ++i1)
          {
            double[] sml_si_sj_i = sml_si_sj[i];
            
            // j loop decision (same large sub-block or two consecutive)
           
            if (sj1F == sj1L)
            {
              // one loop over j = 0 nsbe0 and  j1 = j1F to j1L

              double[] lrg_si_sj_i = lrg_si[sj1F][i1];
              for (int j = 0, j1 = j1F; j < nsbeSml; ++j, ++j1)
              {
                sml_si_sj_i[j] = lrg_si_sj_i[j1];
              }
            }
            else
            {
              // two j loops
              // first j loop from j = 0 to j0m-1 and j1 = j1F to nsbe0-1
              
              int j0m = sj1L * nsbeLrg - sj * nsbeSml;
              double[] lrg_si_sj_i = lrg_si[sj1F][i1];
              for (int j = 0, j1 = j1F; j < j0m; ++j, ++j1)
              {
                sml_si_sj_i[j] = lrg_si_sj_i[j1];
              }
              
              // second j loop from j = j0m to nsbe0-1 and j1 = 0 to j1L
              
              lrg_si_sj_i = lrg_si[sj1L][i1];
              for (int j = j0m, j1 = 0; j < nsbeSml; ++j, ++j1)
              {
                sml_si_sj_i[j] = lrg_si_sj_i[j1];
              }
            }
          } // end for (int i = 0, i1 = i1F; i < nsbe0; ++i, ++i1)
        }
        else
        {
          // two i loops
          // first i loop from i = 0 to i0m-1 and i1 = i1F to nsbe0-1
          
          int i0m = si1L * nsbeLrg - si * nsbeSml;
          double[][][] lrg_si = lrgSubBlks[si1F];
          for (int i = 0, i1 = i1F; i < i0m; ++i, ++i1)
          {
            double[] sml_si_sj_i = sml_si_sj[i];

            // j loop decision (same large sub-block or two consecutive)

            if (sj1F == sj1L)
            {
              // one loop over j = 0 nsbe0 and  j1 = j1F to j1L

              double[] lrg_si_sj_i = lrg_si[sj1F][i1];
              for (int j = 0, j1 = j1F; j < nsbeSml; ++j, ++j1)
              {
                sml_si_sj_i[j] = lrg_si_sj_i[j1];
              }
            }
            else
            {
              // two j loops
              // first j loop from j = 0 to j0m-1 and j1 = j1F to nsbe0-1
              
              int j0m = sj1L * nsbeLrg - sj * nsbeSml;
              double[] lrg_si_sj_i = lrg_si[sj1F][i1];
              for (int j = 0, j1 = j1F; j < j0m; ++j, ++j1)
              {
                sml_si_sj_i[j] = lrg_si_sj_i[j1];
              }
              
              // second j loop from j = j0m to nsbe0-1 and j1 = 0 to j1L
              
              lrg_si_sj_i = lrg_si[sj1L][i1];
              for (int j = j0m, j1 = 0; j < nsbeSml; ++j, ++j1)
              {
                sml_si_sj_i[j] = lrg_si_sj_i[j1];
              }
            }
          } // end for (int i = 0, i1 = i1F; i < i0m; ++i, ++i1)
          
          // second i loop from i = i0m to nsbe0-1 and i1 = 0 to i1L

          lrg_si = lrgSubBlks[si1L];
          for (int i = i0m, i1 = 0; i < nsbeSml; ++i, ++i1)
          {
            double[] sml_si_sj_i = sml_si_sj[i];

            // j loop decision (same large sub-block or two consecutive)

            if (sj1F == sj1L)
            {
              // one loop over j = 0 nsbe0 and  j1 = j1F to j1L

              double[] lrg_si_sj_i = lrg_si[sj1F][i1];
              for (int j = 0, j1 = j1F; j < nsbeSml; ++j, ++j1)
              {
                sml_si_sj_i[j] = lrg_si_sj_i[j1];
              }
            }
            else
            {
              // two j loops
              // first j loop from j = 0 to j0m-1 and j1 = j1F to nsbe0-1
              
              int j0m = sj1L * nsbeLrg - sj * nsbeSml;
              double[] lrg_si_sj_i = lrg_si[sj1F][i1];
              for (int j = 0, j1 = j1F; j < j0m; ++j, ++j1)
              {
                sml_si_sj_i[j] = lrg_si_sj_i[j1];
              }
              
              // second j loop from j = j0m to nsbe0-1 and j1 = 0 to j1L
              
              lrg_si_sj_i = lrg_si[sj1L][i1];
              for (int j = j0m, j1 = 0; j < nsbeSml; ++j, ++j1)
              {
                sml_si_sj_i[j] = lrg_si_sj_i[j1];
              }
            }
          } // end for (int i = i0m, i1 = 0; i < nsbe0; ++i, ++i1)
        } // end else if (si1F != si1L)
      } // end for (int sj = 0; sj < nbsb0; ++sj)
    } // end for (int si = 0; si < nbsb0; ++si)
  }

  /**
   * Validates this block if it is a diagonal block. If it is not diagonal the
   * method immediately returns. If it is a diagonal block and a Nan, Infinity,
   * or zero (if chkZero is true) is found on the diagonal an error is thrown.
   * An additional message (appndErrMsg) is appended to the thrown error message
   * if provided.
   * 
   * @param chkZero     If true then a zero on the diagonal throws an error.
   * @param appndErrMsg An addtional error message appended to the thrown error
   *                    message.
   * @throws IOException
   */
  public void validateBlockDiagonal(boolean chkZero, String appndErrMsg)
  			 throws IOException
  {
  	// exit if not a diagonal block

  	if (!isDiagonalBlock()) return;

  	// loop over all block rows

    int nr = getBlockElementRows();
  	for (int i = 0; i < nr; ++i)
  	{
  		// get the diagonal element and check for Nan, Infinity, or zero (if
  		// requested)

  		double v = getBlockDiagonalElement(i);
  		boolean isnan = Double.isNaN(v);
  		boolean isinf = Double.isInfinite(v);
  		boolean iszero = (v == 0.0) && chkZero;
  		if (isnan || isinf || iszero)
  		{
  			// found a Nan, infinity, or zero. Output error message and throw

  			String type = "(ZERO)";
  			if (isnan)
  				type = "(NaN)";
  			else if (isinf)
  				type = "(Infinity)";

  			String msg = NL + "Error: Finalized Diagonal Block has invalid value " +
  									 type + " on element row " + i + NL +
  									 "       Block: " + getBlockRow() + ", " +
  									 getBlockColumn() + NL + appndErrMsg;
  			throw new IOException(msg);
  		}
  	}
  }
}
