package gms.shared.utilities.geotess.util.numerical.matrixblock;

import static gms.shared.utilities.geotess.util.globals.Globals.NL;
import gms.shared.utilities.geotess.util.filebuffer.FileInputBuffer;
import gms.shared.utilities.geotess.util.filebuffer.FileOutputBuffer;
import gms.shared.utilities.geotess.util.globals.Globals;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Defines a square matrix as a set of square blocks used by the Out-Of-Core
 * (OOC) Cholesky decomposition and inversion object LSINV. This object
 * maintains the definition and is used by applications and the MatrixBlock
 * object to manage a decomposed matrix.
 * 
 * The matrix definition decomposes a symmetric lower triangular matrix into a
 * set of blocks each with a subset of rows and columns from the original
 * matrix. The blocks are further decomposed into a set of sub-blocks, which
 * contain the matrix elements. The numbers of each within each sub-entity is
 * given by the following decomposition matrix.
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
 * Given these definitions the total number of elements in the lower triangular
 * matrix is given by
 * 
 *   nme * (nme + 1) / 2
 *   
 * If nmb is an integral divisor of nme then
 * 
 *   nme = nmb * nbe
 *   
 * However, for an arbitrary matrix size (which cannot generally be controlled)
 * we find that nmb is rarely an integral divisor of nme. In that case the last
 * block usually has fewer element rows than all other blocks
 * 
 *   nbeLast = nme - (nmb - 1) * nbe = nme % nbe
 *   nmb     = ceil(nme / nbe)
 * 
 * The number of total blocks in the matrix is given by
 * 
 *   nmb * (nmb + 1) / 2
 * 
 * where the last row (block[nmb-1][j]) may have fewer rows (nbeLast).
 * 
 * Unfortunately, while it is desirable to make blocks as large as possible so
 * as to minimize the number of times that they need to be read, it has the
 * opposite impact on in-core numerical performance because of CPU cache
 * limitations. Large blocks do not cache well. Instead it is desirable to
 * decompose blocks into sub-blocks where the sub-block size is chosen so that
 * cache issues are alleviated or removed and numerical throughput is maximized.
 * So, besides decomposing the matrix into blocks we also decompose the blocks
 * into sub-blocks.
 *   
 * Like the case for matrix element rows in a block we require that all
 * previous blocks have exactly the same number of sub-blocks defining them
 * (with the exception of the last block which may have fewer). This means that
 * 
 *   nbe = nbsb * nsbe
 * 
 * exactly. Of course, as was the case for the last block containing possibly
 * fewer elements than the previous blocks, so to the last block may contain
 * fewer sub-blocks than the previous blocks. Also, The last sub-block of the
 * last block may have fewer element rows than all other sub-blocks in the last
 * block and all other previous blocks. If we define the total number of
 * sub-blocks in the matrix (as opposed to the number in a single block) we have
 * 
 *   nmsb = ceil(nme / nsbe)
 *   
 * and the last sub-block has an element count of
 * 
 *   nsbeLast = nme - (nmsb - 1) * nsbe = nme % nsbe
 * 
 * From this we find that the number of sub-blocks in the last block is
 * 
 *   nbsbLast = nmsb - (nmb - 1) * nbsb = nmsb % nbsb
 *
 * With these definitions one will find the following identities with the
 * source code attributes given below
 * 
 *   nme      = aNumMtrxElemRows
 *   nbe      = aNumBlkElemRows
 *   nsbe     = aNumSubBlkElemRows
 *   nmb      = aNumMtrxBlkRows
 *   nmsb     = aNumMtrxSubBlkRows
 *   nbsb     = aNumBlkSubBlkRows
 *   nbeLast  = aLastBlkElemRows
 *   nbsbLast = aLastBlkSubBlkRows
 *   nsbeLast = aLastSubBlkElemRows
 * 
 * This object allows for the reformulation of the sub-block structure into
 * as many different sub-block size and block sub-block discretizations as are
 * possible given the fixed block size (nbe). So, any integer multiple of nbe
 * can be a valid sub-block size and corresponding block sub-block count. All
 * that is required is
 * 
 *   nbe = nbsb * nsbe
 *   
 * as was defined earlier. So valid sub-block sizes (nsbe) range from 1 to nbe
 * inclusive as long as nbe % nsbe = 0.
 * 
 * This object can be created with one of three constructors. The first inputs
 * nme and nbe and assumes a default value for nsbe (256). The value for nbe
 * is only a guess and the constructor will find the closest value to the input
 * nbe such that the number of sub-block elements (nsbe) and the number of block
 * sub-blocks is integral with the number of block elements (nbe = nbsb * nsbe
 * from above). The actual value assigned to nbe will never be less than the
 * input value minus nsbe-1.
 * 
 * The second constructor allows nsbe to be defined but nbe is still a guess
 * relative to the matrix size and the input value of nsbe. The value of nsbe
 * used to construct the block discretization is referred to as the basis sub-
 * block size. It is guaranteed to be a valid sub-block size. The valid sub-
 * block size being used at any point is referred to as the current sub-block
 * size.
 * 
 * Finally, the third constructor reads the definition from disk. After a
 * MatrixBlockDefinition is constructed it can be written to disk.
 * 
 * In addition to construction, read, and write functionality, this object can
 * dump it self to a console for review. The overridden toString() function
 * performs one of these services but there are several more.
 *  
 * Lastly, this object provides a large set of getters to obtain all of the
 * information described above using externally defined sub-blocks sizes or
 * or the internal settings (aCurrSubBlkSize). Also the basis settings
 * (aCurrSubBlkSize == aNumSubBlkElemRows) can be returned.
 * 
 * Functional Outline:
 * 
 *   static bestSize(nme, nsbe, nbeMax, nmbMin, nmbMax, maxBestSolns)
 *   static showSize(nme, nbe, nsbe)
 * 
 *          MatrixBlockDefinition(nme, nbe)
 *          MatrixBlockDefinition(nme, nbe, nsbe)
 *          MatrixBlockDefinition(filepath)
 * 
 *          size()
 * 
 *          blocks()
 *          blockSize()
 *          blockSubBlocks(size)
 *          blockSubBlocks()
 *          blockSubBlocksBasis()
 *          blockElementRow(matrixElementRow)
 *          blockRow(matrixElementRow)
 *          blockRowPlust1(matrixElementRow)
 *          blockSubBlockElementRow(blockElementRow, size)
 *          blockSubBlockElementRow(blockElementRow)
 *          blockSubBlockElementRowBasis(blockElementRow)
 *          blockSubBlockRow(blockElementRow, size)
 *          blockSubBlockRow(blockElementRow)
 *          blockSubBlockRow(blockElementRow)
 *          blockSubBlockRowFromMatrixRow(mtrxElemRow, size)
 *          blockSubBlockRowFromMatrixRow(mtrxElemRow)
 *          blockSubBlockRowFromMatrixRowBasis(mtrxElemRow)
 * 
 *          subBlocks(size)
 *          subBlocks()
 *          subBlocksBasis()
 *          subBlockSize(size)
 *          subBlockSize()
 *          subBlockSizeBasis()
 *          subBlockRow(mtrxElemRow, size)
 *          subBlockRow(mtrxElemRow)
 *          subBlockRowBasis(mtrxElemRow)
 *          subBlockElementRow(matrixElementRow, size)
 *          subBlockElementRow(matrixElementRow)
 *          subBlockElementRowBasis(matrixElementRow)
 * 
 *          getBlockElementRows(blockRow)
 * 
 *          getBlockSubBlockElementRows(block, subblock, size)
 *          getBlockSubBlockElementRows(block, subblock)
 *          getBlockSubBlockElementRowsBasis(block, subblock)
 *          getBlockSubBlockRows(block, size)
 *          getBlockSubBlockRows(block)
 *          getBlockSubBlockRowsBasis(block)
 * 
 *          getLastBlockElementRows()
 *          getLastBlockIndex()
 *          getLastBlockSubBlockRows()
 *          getLastBlockSubBlockRows(size)
 *          getLastBlockSubBlockRows()
 *          getLastBlockSubBlockRowsBasis()
 * 
 *          getLastSubBlockElementRows(size)
 *          getLastSubBlockElementRows()
 *          getLastSubBlockElementRowsBasis()
 *          getLastSubBlockIndex(size)
 *          getLastSubBlockIndex()
 *          getLastSubBlockIndexBasis()
 * 
 *          isSubBlockSizeValid(size)
 *          resetSubBlockSize()
 *          setNewSubBlockSize(size)
 *          setSubBlockSizeToBlockSize()
 * 
 *          showAllDefinitions()
 *          showBasisDefinition()
 *          showCurrentDefinition()
 *          showDefinition(size)
 *          toString()
 *          
 *          read(filepath)
 *          write(filepath)
 * 
 *          symmMatrixBlockCount()
 *          symmMatrixElementCount()
 *          symmMatrixSubBlockCount(size)
 *          symmMatrixSubBlockCount()
 *          symmMatrixSubBlockCountBasis()
 * 
 *          memoryStorage()
 * 
 * Lastly, in addition to all of the above public functionality, this object
 * supports an observer pattern. So that all observers (MatrixBlock objects)
 * associated with this MatrixBlockDefinition (the subject) change their
 * sub-block size when any of following three functions are called
 * 
 *     setNewSubBlockSize(size);
 *     setSubBlockSizeToBlockSize();
 *     resetSubBlockSize();
 *
 * If the call results in a new current sub-block size then all observers
 * will automatically change their sub-block size to the new size.
 * 
 * @author jrhipp
 *
 */
@SuppressWarnings("serial")
public class MatrixBlockDefinition implements Serializable
{
  /**
   * The number of matrix element rows (the matrix size).
   */
  private int                              aNumMtrxElemRows     = 0;
  
  /**
   * The number of block element rows (the block size).
   */
  private int                              aNumBlkElemRows      = 0;
  
  /**
   * The basis number of sub-block rows (the sub-block size). The actual number
   * returned is a function of aN and aSubDivide. Changing aN and/or aSubDivide
   * to other than default values (1, true) will produce sub-block subdivision
   * or merger which increases or decreases the number of sub-block element rows
   * accordingly. 
   */
  private int                              aNumSubBlkElemRows   = 256;

  /**
   * The number of matrix sub-block rows. The actual number returned is a 
   * function of aN and aSubDivide. Changing aN and/or aSubDivide to other than
   * default values (1, true) will produce sub-block subdivision or merger
   * which increases or decreases the number of sub-block element rows
   * accordingly.  
   */
  private int                              aNumMtrxSubBlkRows   = 0;

  /**
   * The number of block sub-block rows. The actual number returned is a 
   * function of aN and aSubDivide. Changing aN and/or aSubDivide to other than
   * default values (1, true) will produce sub-block subdivision or merger
   * which decreases or increases the number of sub-block element rows
   * accordingly.
   */
  private int                              aNumBlkSubBlkRows    = 0;

  /**
   * The number of matrix block rows.
   */
  private int                              aNumMtrxBlkRows      = 0;

  /**
   * The number of element rows in the last block.
   */
  private int                              aLastBlkElemRows     = 0;

  /**
   * The number of element rows in the last sub-block. The actual number
   * returned is a function of aN and aSubDivide. Changing aN and/or aSubDivide
   * to other than default values (1, true) will produce sub-block subdivision
   * or merger which increases or decreases the number of sub-block element rows
   * accordingly. 
   */
  private int                              aLastSubBlkElemRows  = 0;

  /**
   * The number of sub-blocks in the last block. The actual number returned is a 
   * function of aN and aSubDivide. Changing aN and/or aSubDivide to other than
   * default values (1, true) will produce sub-block subdivision or merger
   * which decreases or increases the number of sub-block element rows
   * accordingly.
   */
  private int                              aLastBlkSubBlkRows   = 0;

  /**
   * The last block index.
   */
  private int                              aLastBlkIndx         = 0;
  
  /**
   * The last block sub-block index. The actual number returned is a function
   * of aN and aSubDivide. Changing aN and/or aSubDivide to other than default
   * values (1, true) will produce sub-block subdivision or merger which
   * increases or decreases the number of sub-block element rows accordingly. 
   */
  private int                              aLastSubBlkIndx      = 0;

  /**
   * Current sub-block size setting. Defaults to aNumSubBlkElemRows. Can be
   * any value between 1 and aNumBlkElemRows that is an integral multiple of
   * aNumBlkElemRows (i.e. aNumBlkElemRows % aCurrSubBlockSize = 0).
   */
  private int                              aCurrSubBlkSize    = 0;

  /**
   * The set of all currently registered MatrixBlocks (their sub-block matrix
   * is instantiated).
   */
  private transient HashSet<MatrixBlock>   aBlkObsrvrs          = null;

  /**
   * Builds a new matrix definition with the input matrix size and requested
   * block size. Note that the actual block element row count will be defined
   * so that an integral number of sub-blocks, based on the sub-block element
   * row count, will fit within it. The actual block size will be equal to or
   * less than the input request but no more less than the number of sub-block
   * element rows - 1. This constructor assumes the default size for the number
   * of sub-block element rows (256).
   * 
   * @param mtrxElemRows The matrix size.
   * @param blkElemRows  The requested block size.
   */
  public MatrixBlockDefinition(int mtrxElemRows, int blkElemRows)
  {
    setBlockInfo(mtrxElemRows, blkElemRows);
  }

  /**
   * Builds a new matrix definition with the input matrix size, requested
   * block size, and input sub-block size. Note that the actual block element
   * row count will be defined so that an integral number of sub-blocks (whose
   * size = subBlkElemRows) will fit within it. The actual block size will be
   * equal to or less than the input request but no more less than
   * subBlkElemRows - 1.
   * 
   * @param mtrxElemRows   The matrix size.
   * @param blkElemRows    The requested block size.
   * @param subBlkElemRows The sub-block size.
   */
  public MatrixBlockDefinition(int mtrxElemRows, int blkElemRows,
                               int subBlkElemRows)
  {
    aNumSubBlkElemRows = subBlkElemRows;
    setBlockInfo(mtrxElemRows, blkElemRows);
  }

  /**
   * Creates a new MatrixBlockInfo object from the file definition contained
   * in the file at fPath.
   * 
   * @param fPath The file that contains the new definition for this
   *              MatrixBlockInfo object.
   * 
   * @throws IOException
   */
  public MatrixBlockDefinition(String fPath) throws IOException
  {
    read(fPath);
  }

  /**
   * @param args array of three integers
   *        [0] Matrix size (rows).
   *        [1] Block size (rows).
   *        [2] Sub-block size (rows).
   */
  public static void main(String[] args)
  {
    if (args.length == 3)
      MatrixBlockDefinition.showSize(Integer.valueOf(args[0]),
                                     Integer.valueOf(args[1]),
                                     Integer.valueOf(args[2]));
    else
      System.out.println("Call requires three arguments " +
                         "(matrix size, block size, sub-block size) ...");
  }

  /**
   * Called by a MatrixBlock that is owned by this MatrixBlockDefinition object
   * when it instantiates it's internal sub-block matrix. This will make it
   * aware of any changes to the sub-block size parameters aN and aSubDivide.
   * 
   * @param mb The MatrixBlock wishing to observe this MatrixBlockDefinition for
   *           changes in the sub-block size parameters aN and aSubDivide.
   */
  protected void addObserver(MatrixBlock mb)
  {
    if (aBlkObsrvrs != null)
    {
      synchronized (aBlkObsrvrs) {aBlkObsrvrs.add(mb);}
    }
  }

  /**
   * Called by a MatrixBlock that is owned by this MatrixBlockDefinition object
   * when it nullifies it's internal sub-block matrix. This removes the
   * MatrixBlock from any sub-block size parameter change events.
   * 
   * @param mb The MatrixBlock wishing to observe this MatrixBlockDefinition for
   *           changes in the sub-block size parameters aN and aSubDivide.
   */
  protected void removeObserver(MatrixBlock mb)
  {
    if (aBlkObsrvrs != null)
    {
      synchronized (aBlkObsrvrs) {aBlkObsrvrs.remove(mb);}
    }
  }

  /**
   * A utility function for determining if a desired sub-block subdivision or
   * merger request is valid or not. The changeSubBlockSize(...) function takes
   * the same arguments and actually performs the change. If, however, the
   * inputs are invalid it will throw an IOException. This function simply
   * returns true if they are valid and false otherwise.
   * 
   * @param subDivide The subdivision/merger flag setting to be tested.
   * @param n         The subdivision/merger size factor to be tested.
   * @return True if the inputs are valid ... false otherwise.
   */
  public boolean isSubBlockSizeValid(int sbsize)
  {
    if ((sbsize < 1) || (sbsize > aNumBlkElemRows)) return false;
    if (aNumBlkElemRows % sbsize != 0) return false;

    return true;
  }

  /**
   * Returns an estimate of the memory size of this MatrixBlockDefinition
   * object.
   * 
   * @return An estimate of the memory size of this MatrixBlockDefinition
   *         object.
   */
  public long memoryEstimate()
  {
    long mem = 12 * Integer.SIZE / 8;
    if (aBlkObsrvrs != null)
    {
      synchronized (aBlkObsrvrs) {mem += 8 * (aBlkObsrvrs.size() + 1);}
    }
    return mem;
  }

  /**
   * Resets the sub-block size parameters back to default settings. This
   * function also calls any registered MatrixBlocks to notify them of the
   * size change.
   */
  public void resetSubBlockSize()
  {
    aCurrSubBlkSize = aNumSubBlkElemRows;
    changeSubBlockSize();
  }

  /**
   * Sets the sub-block size parameters equal to the block size. This defines
   * a single sub-block that contains the entire block. This function also
   * calls any registered MatrixBlocks to notify them of the size change.
   */
  public void setSubBlockSizeToBlockSize()
  {
    aCurrSubBlkSize = aNumBlkElemRows;
    changeSubBlockSize();
  }

  /**
   * This function changes the sub-block size given the input parameter values.
   * If the inputs are an invalid combination then an IOException is thrown.
   * Otherwise, the parameters are accepted and all registered MatrixBlocks are
   * notified of the change.
   * 
   * @param subDivide The new subdivision/merger flag setting.
   * @param n         The new subdivision/merger size factor.
   * @throws IOException
   */
  public void setNewSubBlockSize(int sbsize)
         throws IOException
  {
    // throw error if request is out-of-range or not an integral multiple of the
    // sub-block element row size or block element row size.

    if ((sbsize < 1) || (sbsize > aNumBlkElemRows))
    {
      // sub-block size request is invalid ... size must be between 1 and
      // number of block element rows inclusive ... throw error

      String s = "Error: Sub-Block size must be between 1 and " +
                 aNumBlkElemRows + " inclusive ...";
      throw new IOException(s);
    }

    if (aNumBlkElemRows % sbsize != 0)
    {
      // sub-block size request is invalid ... size must be an integral multiple
      // of block element rows ... throw error

      String s = "Error: Sub-Block size request (" + sbsize +
                 ") is not an integral multiplier of the basis " +
                 "block element row count (" + aNumBlkElemRows + ") ...";
      throw new IOException(s);
    }

    // valid request ... set new size and notify all observing matrix
    // blocks of the new state.

    aCurrSubBlkSize = sbsize;
    changeSubBlockSize();
  }

  /**
   * Sets the new sub-block size and notifies all registered MatrixBlock
   * objects.
   * 
   * @param subDivide The new subdivision/merge flag.
   * @param n         The new sub-block expansion/shrinkage factor.
   */
  private synchronized void changeSubBlockSize()
  {
    if (aBlkObsrvrs != null)
    {
      synchronized (aBlkObsrvrs)
      {
        for (MatrixBlock mb: aBlkObsrvrs) mb.changeSubBlockSize();
      }
    }
  }

  /**
   * Returns the matrix size (element rows/columns square of the entire matrix).
   * 
   * @return The matrix size (element rows/columns square of the entire matrix).
   */
  public int size()
  {
    return aNumMtrxElemRows;
  }

  /**
   * Returns the block size (element rows/columns square of one matrix block).
   * 
   * @return The block size (element rows/columns square of one matrix block).
   */
  public int blockSize()
  {
    return aNumBlkElemRows;
  }

  /**
   * Returns the number of blocks (block rows/columns square).
   * 
   * @return The number of blocks (block rows/columns square).
   */
  public int blocks()
  {
    return aNumMtrxBlkRows;
  }

  /**
   * Returns the total number of defined elements in the symmetric square matrix.
   * 
   * @return The total number of defined elements in the symmetric square matrix.
   */
  public long symmMatrixElementCount()
  {
    return (long) aNumMtrxElemRows * (aNumMtrxElemRows + 1) / 2;
  }

  /**
   * Returns the total number of defined blocks in the symmetric square matrix.
   * 
   * @return The total number of defined blocks in the symmetric square matrix.
   */
  public int symmMatrixBlockCount()
  {
    return aNumMtrxBlkRows * (aNumMtrxBlkRows + 1) / 2;
  }

  /**
   * Returns the total memory storage in bytes on disk for this blocked
   * matrix.
   * 
   * @return The total memory storage in bytes on disk for this blocked
   *         matrix.
   */
  public long memoryStorage()
  {
    long mem = 4 * aNumMtrxElemRows * (aNumMtrxElemRows + 1);
    return mem;
  }

  /**
   * Returns the size of a block in core memory ... always square.
   * 
   * @return The size of a block in core memory ... always square.
   */
  public long coreMemoryStorage()
  {
  	return 8 * aLastBlkElemRows * aNumBlkElemRows;
  }

  /**
   * Returns the actual number of bytes read or written to disk for a block
   * defined by this definition.
   * 
   * @param row  The block row.
   * @param col  The block column.
   * @return The actual number of bytes read or written to disk for this block.
   */
  public long getBlockIOMemory(int row, int col)
  {
    if (row == aNumBlkElemRows - 1)
    {
      if (row == col)
        return 4 * aLastBlkElemRows * (aLastBlkElemRows + 1);
      else
        return 8 * aLastBlkElemRows * aNumBlkElemRows;
    }
    else
    {
      if (row == col)
        return 4 * aNumBlkElemRows * (aNumBlkElemRows + 1);
      else
        return 8 * aNumBlkElemRows * aNumBlkElemRows;
    }
  }

  /**
   * Returns the default file name specification.
   * 
   * @return The default file name specification.
   */
  public static String getDefaultFileName()
  {
    return "matrixdefn";
  }

  /**
   * Returns the default path/file name specification given the input path.
   * 
   * @param pth The input path onto which the default file name will be
   *            appended and returned.
   * @return The default path/file name specification given the input path.
   */
  public static String getDefaultPathFileName(String pth)
  {
    return pth + File.separator + "matrixdefn";
  }

  /**
   * Reads this MatrixBlockInfo object from the file fPath.
   * 
   * @param fPath The file path/name containing the MatrixBlockInfo data
   *              that will be used to define this MatrixBlockInfo object.
   * 
   * @throws IOException
   */
  public void read(String fPath) throws IOException
  {
    FileInputBuffer fib = new FileInputBuffer(fPath);
    int nme    = fib.readInt();
    int nbe    = fib.readInt();
    int nsbe   = fib.readInt();
    int curSBS = fib.readInt();
    fib.close();

    aNumSubBlkElemRows = nsbe;
    setBlockInfo(nme, nbe);
    aCurrSubBlkSize = curSBS;
  }

  /**
   * Writes this MatrixBlockInfo object to the file fPath.
   * 
   * @param fPath The file path/name that will contain this MatrixBlockInfo
   *              objects data.
   * @throws IOException
   */
  public void write(String fPath) throws IOException
  {
    FileOutputBuffer fob = new FileOutputBuffer(fPath);
    fob.writeInt(aNumMtrxElemRows);   // nme
    fob.writeInt(aNumBlkElemRows);    // nme
    fob.writeInt(aNumSubBlkElemRows); // nsbe
    fob.writeInt(aCurrSubBlkSize); // nsbeCurr
    fob.close();
  }

  /**
   * Returns the number of element rows in the last block. This value will be
   * equal to or less than the standard number of block element rows
   * (aNumBlkElemRows).
   * 
   * @return The number of element rows in the last block.
   */
  public int getLastBlockElementRows()
  {
    return aLastBlkElemRows;
  }

  /**
   * Returns the number of element rows in the input block. If blk is less than
   * the last block index then aNumBlkElemRows is returned. If blk is the last
   * block index then aLastBlkElemRows is returned.
   * 
   * @param blk The input block index for which the number of element rows is
   *            returned.
   * @return The number of element rows in the input block.
   */
  public int getBlockElementRows(int blk)
  {
    if (blk == aLastBlkIndx)
      return aLastBlkElemRows;
    else
      return aNumBlkElemRows;
  }

  /**
   * Returns the last block index.
   * 
   * @return The last block index.
   */
  public int getLastBlockIndex()
  {
    return aLastBlkIndx;
  }
  
  /**
   * Returns the block row index containing the input matrix element row index.
   * If the input matrix element row index exceeds the number of entries
   * (aNumMtrxElemRows) then the last valid block index + 1 is returned.
   * 
   * @param mtrxElemRow The matrix element row index for which the corresponding
   *                    block row index will be returned. Valid values are any
   *                    number => 0.
   * @return The block row index containing the input matrix element row index.
   *         If the input matrix element row index exceeds the number of entries
   *         (aNumMtrxElemRows) then the last valid block index + 1 is returned.
   */
  public int blockRowPlus1(int mtrxElemRow)
  {
    if (mtrxElemRow >= aNumMtrxElemRows)
      return aNumMtrxBlkRows;
    else
      return mtrxElemRow / aNumBlkElemRows;
  }
  
  /**
   * Returns the block row index containing the input matrix element row index.
   * 
   * @param mtrxElemRow The matrix element row index for which the corresponding
   *                    block row index will be returned. Valid values range
   *                    from 0 to aNumMtrxElemRows - 1.
   * @return The block row index containing the input matrix element row index.
   */
  public int blockRow(int mtrxElemRow)
  {
    return mtrxElemRow / aNumBlkElemRows;
  }

  /**
   * Returns the block element offset row index containing the input matrix
   * element row index.
   *  
   * @param mtrxElemRow The matrix element row index for which the corresponding
   *                    block element offset row index will be returned. Valid
   *                    values range from 0 to aNumMtrxElemRows - 1.
   * @return The block element offset row index containing the input matrix
   *         element row index.
   */
  public int blockElementRow(int mtrxElemRow)
  {
    return mtrxElemRow % aNumBlkElemRows;
  }

  /**
   * Defines this MatrixBlockInfo object given an input matrix size (square)
   * and the number of rows/columns in each block (the block size).
   * 
   * @param size The total number of rows/columns in a square matrix.
   * @param blksize The total number of rows/columns in a square block of a
   *                matrix (blksize <= size).
   */
  private void setBlockInfo(int mtrxElemRows, int blkElemRows)
  {
    // nme
    aNumMtrxElemRows   = mtrxElemRows;
    // nbsb
    aNumBlkSubBlkRows  = blkElemRows / aNumSubBlkElemRows;
    // nbe
    aNumBlkElemRows    = aNumSubBlkElemRows * aNumBlkSubBlkRows;
    // nmsb
    aNumMtrxSubBlkRows = (int) Math.ceil((double) aNumMtrxElemRows /
                                         aNumSubBlkElemRows);
    // nbsb * nmb - nmsb >= 0  --> nmb >= nmsb/nbsb
    aNumMtrxBlkRows    = aNumMtrxSubBlkRows / aNumBlkSubBlkRows;
    if (aNumMtrxBlkRows * aNumBlkSubBlkRows < aNumMtrxSubBlkRows)
      ++aNumMtrxBlkRows;

    // for speed save the last block index, the last block sub-block row count,
    // and the last block total element row count

    //nbsb' = nmsb - (nmb - 1)*nbsb
    aLastBlkIndx       = aNumMtrxBlkRows - 1;
    aLastBlkSubBlkRows = aNumMtrxSubBlkRows -
                         (aNumMtrxBlkRows - 1) * aNumBlkSubBlkRows;  
    //nbe'  = nme - (nmb - 1)*nbe
    aLastBlkElemRows    = aNumMtrxElemRows - 
                          (aNumMtrxBlkRows - 1) * aNumBlkElemRows;

    // also save the last sub-block index (of the last block) and the last
    // sub-block element row count

    //nsbe' = nme - (nmsb - 1)*nsbe
    aLastSubBlkIndx = aLastBlkSubBlkRows - 1; 
    aLastSubBlkElemRows = aNumMtrxElemRows -
                          (aNumMtrxSubBlkRows - 1) * aNumSubBlkElemRows;
    
    // define observer set and exit

    aBlkObsrvrs = new HashSet<MatrixBlock>();
    aCurrSubBlkSize = aNumSubBlkElemRows;
  }

  /**
   * Called by task IOMIFactory objects to ensure that the MatrixBlockDefintion
   * creates these transient objects which are lost in the serialization
   * process.
   */
  public void setBlockObserversOn()
  {
    if (aBlkObsrvrs == null) aBlkObsrvrs = new HashSet<MatrixBlock>();
  }

  /**
   * Called by task IOMIFactory objects to ensure that the MatrixBlockDefintion
   * creates these transient objects which are lost in the serialization
   * process.
   */
  public void setBlockObserversOff()
  {
    if (aBlkObsrvrs != null) aBlkObsrvrs = null;
  }

  /**
   * Overrides toString() to show all valid definitions for this
   * MatrixBlockDefinition object.
   */
  @Override
  public String toString()
  {
    return showAllDefinitions("");
  }

  /**
   * Shows all valid definitions for this MatrixBlockDefinition object.
   */  
  public String showAllDefinitions(String hdr)
  {
    return showDefinition(hdr, -1);
  }

  /**
   * Shows the basis sub-block definitin for this MatrixBlockDefinition object.
   */  
  public String showBasisDefinition(String hdr)
  {
    return showDefinition(hdr, aNumSubBlkElemRows);
  }

  /**
   * Shows the current sub-block definitin for this MatrixBlockDefinition object.
   */  
  public String showCurrentDefinition(String hdr)
  {
    return showDefinition(hdr, aCurrSubBlkSize);
  }

  /**
   * Outputs this MatrixBlockDefinition as descriptive string for the input
   * sub-block size request (subBlkSize). If subBlkSize = -1 all valid sub-block
   * sizes are output using the following prescription:
   * 
   * Matrix, Block, Sub-Block Discretization Definitions:
   * 
   *     Matrix Element
   *         Row Count {nme}                 = ###,###
   *         Total Entries {nme*(nme+1)/2}   = ###,###,###,###
   *         Storage (GB)                    = #,###.##
   * 
   *     Block Element
   *         Row Count {nbe}                 = #,###
   *         Total Entries {nbe*nbe}         = ###,###,###
   *         Storage (MB)                    = #,###.##
   *         Last Row Count {nbeLast}        = #,###
   *         Last Fill Fraction (%)          = ###.##
   * 
   *     Matrix Block
   *         Row Count {nmb}                 = ###
   *         Total Entries {nmb*(nmb+1)/2}   = #,###
   * 
   *                                          Valid Sub-Block Table
   * 
   *  B = Basis Sub-Block Size
   *  C = Current Sub-Block Size
   *  
   *             Sub-Block Element                  |           Block Sub-Block             |    Matrix Sub-Block
   *                                                |                                       |
   *                                 Last    Last   |                       Last     Last   |
   *  Row      Total                  Row    Fill   |   Row     Total        Row     File   |   Row        Total
   * count    Entries      Storage   Count   Frctn  |  Count    Entries     Count    Frctn  |  Count      Entries
   * (nsbe) (nsbe*nsbe)      (KB)  (nsbeLast) (%)   |  (nbsb) (nbsb*nbsb) (nbsbLast)  (%)   |  (nmsb) (nmsb*(nmsb+1)/2)
   * ------------------------------------------------------------------------------------------------------------------
   * #,###  ###,###,###  ###,###.##  #,###  ###.##     #,###  ###,###,###   #,###    ###.##    #,###  ###, ###,###,###
   * ...
   * 
   * If a specific valid sub-block size is requested the following output
   * prescription is used:
   * 
   * Matrix, Block, Sub-Block Discretization Definition:
   * 
   *     Matrix Element
   *         Row Count {nme}                 = ###,###
   *         Total Entries {nme*(nme+1)/2}   = ###,###,###,###
   *         Storage (GB)                    = #,###.##
   * 
   *     Block Element
   *         Row Count {nbe}                 = #,###
   *         Total Entries {nbe*nbe}         = ###,###,###
   *         Storage (MB)                    = #,###.##
   *         Last Row Count {nbeLast}        = #,###
   *         Last Fill Fraction (%)          = ###.##
   * 
   *     Sub-Block Element
   *         Row Count {nsbe}                = #,###
   *         Total Entries {nsbe*nsbe}       = ###,###,###
   *         Storage (KB)                    = #,###.##
   *         Last Row Count {nsbeLast}       = #,###
   *         Last Fill Fraction (%)          = ###.##
   * 
   *     Matrix Sub-Block
   *         Row Count {nmsb}                = #,###
   *         Total Entries {nmsb*(nmsb+1)/2} = ###,###,###
   * 
   *     Block Sub-Block
   *         Row Count {nbsb}                = #,###
   *         Total Entries {nbsb*nbsb}       = ###,###,###
   *         Last Row Count {nbsbLast}       = #,###
   *         Last Fill Fraction (%)          = ###.##
   * 
   *     Matrix Block
   *         Row Count {nmb}                 = ###
   *         Total Entries {nmb*(nmb+1)/2}   = #,###
   * 
   * @param subBlkSize The sub-block size request for which the definition will
   *                   be output. If equal to -1 then all valid sub-block sizes
   *                   are output.
   * @return The requested definition string.
   * 
   */
  public String showDefinition(String hdr, int subBlkSize)
  {
    // output header

    String s = hdr + "Matrix, Block, Sub-Block Discretization ";
    if (subBlkSize == -1)
      s += "Definitions:" + NL + NL;
    else if ((subBlkSize == aNumSubBlkElemRows) &&
             (subBlkSize == aCurrSubBlkSize))
      s += "Basis (Current) Definition:" + NL + NL;
    else if (subBlkSize == aNumSubBlkElemRows) 
      s += "Basis Definition:" + NL + NL;
    else if (subBlkSize == aCurrSubBlkSize)
      s += "Current Definition:" + NL + NL;
    else
      s += "(Sub-Block Size = " + subBlkSize + ") Definition:" + NL + NL;

    // output matrix element information

    s += hdr + "        Matrix Element" + NL;
    s += hdr + "            Row Count {nme}                 = " +
         String.format("%-,8d", aNumMtrxElemRows) + NL;
    s += hdr + "            Total Entries {nme*(nme+1)/2}   = " +
         String.format("%-,13d", symmMatrixElementCount()) + NL;
    s += hdr + "            Storage (GB)                    = " +
         String.format("%-,7.2f", (double) 8.0 * symmMatrixElementCount() / 1024 / 1024 / 1024) + NL + NL;

    // output block element information

    s += hdr + "        Block Element" + NL;
    s += hdr + "            Row Count {nbe}                 = " +
         String.format("%-,6d", aNumBlkElemRows) + NL;
    s += hdr + "            Total Entries {nbe*nbe}         = " +
         String.format("%-,10d", aNumBlkElemRows * aNumBlkElemRows) + NL;
    s += hdr + "            Storage (MB)                    = " +
         String.format("%-,7.2f", (double) 8.0 * aNumBlkElemRows * aNumBlkElemRows / 1024 / 1024) + NL;
    s += hdr + "            Last Row Count {nbeLast}        = " +
         String.format("%-,6d", aLastBlkElemRows) + NL;
    s += hdr + "            Last Fill Fraction (%)          = " +
         String.format("%-,6.2f", 100.0 * aLastBlkElemRows / aNumBlkElemRows) +
         NL + NL;

    // see if a specific sub-block size was requested

    if (subBlkSize != -1)
    {
      // if requested sub-block size is invalid say so and return

      if (!isSubBlockSizeValid(subBlkSize))
      {
        s += hdr + "        INVALID Sub-Block Size Request (" + subBlkSize +
             ") ... Cannot Show ..." + NL + NL;
      }
      else
      {
        // output sub-block element information
  
        s += hdr + "        Sub-Block Element" + NL;
        s += hdr + "            Row Count {nsbe}                = " +
             String.format("%-,6d", subBlkSize) + NL;
        s += hdr + "            Total Entries {nsbe*nsbe}       = " +
             String.format("%-,10d", subBlkSize * subBlkSize) + NL;
        s += hdr + "            Storage (KB)                    = " +
             String.format("%-,7.2f", (double) 8.0 * subBlkSize * subBlkSize / 1024) + NL;
        s += hdr + "            Last Row Count {nsbeLast}       = " +
             String.format("%-,6d", getLastSubBlockElementRows(subBlkSize)) + NL;
        s += hdr + "            Last Fill Fraction (%)          = " +
             String.format("%-,6.2f", 100.0 * getLastSubBlockElementRows(subBlkSize) /
                                      subBlkSize) +
             NL + NL;
  
        // output matrix sub-block information
  
        s += hdr + "        Matrix Sub-Block" + NL;
        s += hdr + "            Row Count {nmsb}                = " +
             String.format("%-,6d", subBlocks(subBlkSize)) + NL;
        s += hdr + "            Total Entries {nmsb*(nmsb+1)/2} = " +
             String.format("%-,10d", symmMatrixSubBlockCount(subBlkSize)) +
             NL + NL;
  
        // output block sub-block information
  
        s += hdr + "        Block Sub-Block" + NL;
        s += hdr + "            Row Count {nbsb}                = " +
             String.format("%-,6d", blockSubBlocks(subBlkSize)) + NL;
        s += hdr + "            Total Entries {nbsb*nbsb}       = " +
             String.format("%-,10d", blockSubBlocks(subBlkSize) *
                                     blockSubBlocks(subBlkSize)) + NL;
        s += hdr + "            Last Row Count {nbsbLast}       = " +
             String.format("%-,6d", getLastBlockSubBlockRows(subBlkSize)) + NL;
        s += hdr + "            Last Fill Fraction (%)          = " +
             String.format("%-,6.2f", 100.0 * getLastBlockSubBlockRows(subBlkSize) /
                                      blockSubBlocks(subBlkSize)) +
             NL + NL;
      }
    } // end if (subBlkSize != -1)

    // output matrix block information

    s += hdr + "        Matrix Block" + NL;
    s += hdr + "            Row Count {nmb}                 = " +
         String.format("%-,6d", aNumMtrxBlkRows) + NL;
    s += hdr + "            Total Entries {nmb*(nmb+1)/2}   = " +
         String.format("%-,10d", symmMatrixBlockCount()) + NL + NL;

    // output valid sub-block size table if no specific sub -block size
    // (-1) was requested

    if (subBlkSize == -1)
    {
      // output sub-block table header
  
      s += hdr + "                                                   Valid Sub-Block Table" +
           NL + NL;
      s += hdr + "  B = Basis Sub-Block Size" + NL +
           hdr + "  C = Current Sub-Block Size" + NL + NL;
      s += hdr + "                  Sub-Block Element                |" +
           hdr + "            Block Sub-Block             |       Matrix Sub-Block" + NL;
      s += hdr + "                                                   |" +
           hdr + "                                        |" + NL;
      s += hdr + "                                    Last    Last   |" +
           hdr + "                        Last     Last   |" + NL;
      s += hdr + "     Row      Total                  Row    Fill   |" +
           hdr + "   Row     Total         Row     Fill   |     Row         Total" + NL;
      s += hdr + "    Count    Entries     Storage    Count   Frctn  |" +
           hdr + "  Count    Entries      Count    Frctn  |    Count       Entries" + NL;    
      s += hdr + "   (nsbe)  (nsbe*nsbe)     (KB)  (nsbeLast)  (%)   |" +
           hdr + " (nbsb)  (nbsb*nbsb)  (nbsbLast)  (%)   |   (nmsb)  (nmsb*(nmsb+1)/2)" + NL;
      s += hdr + "  " + Globals.repeat("-", 119) + NL;
      
      // loop over all valid sub-block sizes
  
      for (int i = 1; i <= aNumBlkElemRows; ++i)
      {
        if (isSubBlockSizeValid(i))
        {
          // output row for valid sub-block size i ... append B and/or C if
          // this is the Basis and/or current sub-block size
  
          if ((i == aNumSubBlkElemRows) && (i == aCurrSubBlkSize))
            s += hdr + "  BC";
          else if (i == aNumSubBlkElemRows)
            s += hdr + "  B ";
          else if (i == aCurrSubBlkSize)
            s += hdr + "  C ";
          else
            s += hdr + "    ";

          // output sub-block element discretization

          s += String.format("%,5d", i);
          s += String.format("  %,10d", i * i);
          s += String.format(" %,11.2f", (double) 8.0 * i * i / 1024);
          s += String.format("   %,5d", getLastSubBlockElementRows(i));
          s += String.format("  %,6.2f  |", 100.0 * getLastSubBlockElementRows(i) /
                                            i);
  
          // output block sub-block discretization
  
          s += String.format("  %,5d", blockSubBlocks(i));
          s += String.format("  %,10d", blockSubBlocks(i) * blockSubBlocks(i));
          s += String.format("     %,5d", getLastBlockSubBlockRows(i));
          s += String.format("  %,6.2f   |", 100.0 * getLastBlockSubBlockRows(i) /
                                            blockSubBlocks(i));

          // output matrix sub-block discretization

          s += String.format("  %,8d", subBlocks(i));
          s += String.format("  %,11d", symmMatrixSubBlockCount(i)) + NL;
        } // end if (isSubBlockSizeValid(i))
      } // end for (int i = 1; i <= aNumBlkElemRows; ++i)
    } // end if (subBlkSize == -1)

    // done ... return string

    s += NL + NL;
    return s;
  }

  /**
   * Shows the block discretization given the input size entries.
   * 
   * @param mtrxElemRows   The matrix size (element row count).
   * @param blkElemRows    The block size (element row count). This is an
   *                       estimate. The constructor calculates the closes
   *                       valid number 
   * @param subBlkElemRows The sub-block size (element row count).
   */
  public static void showSize(int mtrxElemRows, int blkElemRows,
                              int subBlkElemRows)
  {
    MatrixBlockDefinition mbd = new MatrixBlockDefinition(mtrxElemRows,
                                                          blkElemRows,
                                                          subBlkElemRows);
    System.out.println(mbd.toString());
  }

  /**
   * Outputs good block and sub-block sizes to minimize the amount of wasted
   * space in the last block.
   * 
   * @param nme     The matrix size (number of element rows).
   * @param nsbe    The sub-block size (number of element rows).
   * @param nbeMax  The maximum allowed block size (number of element rows).
   * @param nmbMin  The minimum allowed number of blocks (rows).
   * @param nmbMax  The maximum allowed number of blocks (rows).
   * @param maxBestSolns The maximum number of discovered solutions to output.
   *                     These are sorted on discrepancy between the number of
   *                     sub-blocks in the last block with the number in all
   *                     previous blocks. Zero is the best. 
   */
  public static void bestSize(int nme, int nsbe, int nbeMax, int nmbMin,
                              int nmbMax, int maxBestSolns)
  {
    HashSet<Integer> nbsbSet;
    HashMap<Integer, HashSet<Integer>> nmbMap;
    TreeMap<Double, HashMap<Integer, HashSet<Integer>>> map;
    
    // create sorted map (tree map) to store all entries based on the final
    // blocks sub-block fill fraction (ie. if 100% then all sub-blocks in the
    // last block are used)

    map = new TreeMap<Double, HashMap<Integer, HashSet<Integer>>>();

    // calculate the number of matrix sub-blocks and the maximum allowed
    // sub-block count in a block.

    int nmsb = (int) Math.ceil((double) nme / nsbe);
    int nbsbMax = nbeMax / nsbe;

    // loop over all allowed matrix block counts from the input minimum to the
    // input maximum

    for (int nmb = nmbMin; nmb <= nmbMax; ++nmb)
    {
      // loop over all allowed block sub-block counts from 1 to the maximum
      // allowed

      for (int nbsb = 1; nbsb <= nbsbMax; ++nbsb)
      {
        // calculate the final block sub-block unused count and test that it is
        // valid (>= 0 and < than the current block sub-block count (nbsb).
        
        int f = nmb * nbsb - nmsb;
        if ((f >= 0) && (f < nbsb))
        {
          // valid final block sub-block unused count ... calculate the last
          // block sub-block fill fraction

          int nbsbLast = nmsb - (nmb - 1) * nbsb;
          double lstBlkSubBlkFill = 100.0 * nbsbLast / nbsb;

          // get the map associated with the fill fraction ... if it hasn't
          // been added yet then add it now

          nmbMap = map.get(lstBlkSubBlkFill);
          if (nmbMap == null)
          {
            nmbMap = new HashMap<Integer, HashSet<Integer>>();
            map.put(lstBlkSubBlkFill, nmbMap);
          }
          
          // get the set of nbsb results associated with nmb. If it hasn't been
          // added yet then add it now

          nbsbSet = nmbMap.get(nmb);
          if (nbsbSet == null)
          {
            nbsbSet = new HashSet<Integer>();
            nmbMap.put(nmb, nbsbSet);
          }

          // add nbsb to the set ... if the number of entries in the map exceeds
          // the requested solution size then get rid of the last entry

          nbsbSet.add(nbsb);
          if (map.size() == maxBestSolns + 1)
            map.pollLastEntry();
        }
      }
    }
    
    // done ... output all remaining entries in the map

    if (map.size() > 0)
    {
      // print the header including the input matrix size, the calculated
      // number of matrix sub-blocks, and the maximum allowed sub-blocks per
      // block

      System.out.println("");
      System.out.println("Matrix Discretization Best Size Solutions");
      System.out.println("  Input");
      System.out.println("    Matrix Element Row Count     = " + nme);
      System.out.println("    Sub-Block Element Row Count  = " + nsbe);
      System.out.println("    Max. Block Element Row Count = " + nbeMax);
      System.out.println("    Min. Block Count             = " + nmbMin);
      System.out.println("    Max. Block Count             = " + nmbMax);
      System.out.println("");

      System.out.println("  Solution");
      System.out.println("    Matrix element rows = " + nme);
      System.out.println("    Matrix sub-block rows = " + nmsb);
      System.out.println("    Maximum allowed sub-blocks per block = " +
                         nbsbMax);

      // calculate the number of elements in the last sub-block and the last
      // sub-block element fill fraction and output the results

      int nsbeLast = nme - (nmsb - 1) * nsbe;
      double lstSubBlkElemFill = 100.0 * nsbeLast / nsbe;
      System.out.println("    Sub-Block element rows = " + nsbe +
                         ", LAST = " + nsbeLast + ", fill (%) = " +
                         lstSubBlkElemFill);
      System.out.println("");

      // loop over all entries in the map

      Set<Double> st = map.descendingKeySet();
      for (Double lstBlkSubBlkFill: st)
      {
        // get the next entry in the map belonging to the current last block
        // sub-block use fraction and loop over all entries in that nmb map

        nmbMap = map.get(lstBlkSubBlkFill);
        for (Map.Entry<Integer, HashSet<Integer>> enmb: nmbMap.entrySet())
        {
          // get nmb and the set of associated nbsb values ... loop over each
          // entry in the set

          int nmb = enmb.getKey();
          nbsbSet = enmb.getValue();
          for (Integer nbsbI: nbsbSet)
          {
            // get nbsb and output the number of matrix block rows

            int nbsb = nbsbI;
            System.out.println("    Matrix block rows = " + nmb);
            
            // calculate the number of elements in each block, the number of
            // element in the last block, and the element fill fraction of the
            // last block and print those results

            int nbe = nbsb*nsbe;
            int nbeLast = nme - (nmb - 1) * nbe;
            double lstBlkElemFill = 100.0 * nbeLast / nbe;
            System.out.println("      Block element rows = " + (nbsb*nsbe) +
                               ", LAST = " + nbeLast + ", fill (%) = " +
                               lstBlkElemFill);
            
            // calculate the number of sub-blocks in the last block and print
            // it along with the last block sub-block fill fraction

            int nbsbLast = nmsb - (nmb - 1) * nbsb;
            System.out.println("      Block sub-block rows = " + nbsb +
                               ", LAST = " + nbsbLast + ", fill (%) = " +
                               lstBlkSubBlkFill);
          }
        }
      }
    }    
  }

  /**
   * Returns the sub-block element offset row index containing the input block
   * element row index given the input sub-block size.
   *  
   * @param blkElemRow  The block element row index for which the
   *                    corresponding sub-block row element offset index will
   *                    be returned. Valid values range from 0 to
   *                    aNumBlkElemRows - 1.
   * @param subBlkSize  The requested sub-block size.
   * @return The sub-block element offset row index containing the input block
   *         element row index.
   */
  public int blockSubBlockElementRow(int blkElemRow, int subBlkSize)
  {
    return blkElemRow % subBlockSize(subBlkSize);
  }

  /**
   * Returns the sub-block element offset row index containing the input block
   * element row index given the current subdivide/merge specification.
   *  
   * @param blkElemRow The block element row index for which the corresponding
   *                   sub-block row element offset index will be returned.
   *                   Valid values range from 0 to aNumBlkElemRows - 1.
   * @return The sub-block element offset row index containing the input block
   *         element row index.
   */
  public int blockSubBlockElementRow(int blkElemRow)
  {
    return blkElemRow % subBlockSize(aCurrSubBlkSize);
  }

  /**
   * Returns the sub-block element offset row index containing the input block
   * element row index with no subdivision/merge operations (the basis).
   *  
   * @param blkElemRow The block element row index for which the corresponding
   *                   sub-block row element offset index will be returned.
   *                   Valid values range from 0 to aNumBlkElemRows - 1.
   * @return The basis sub-block element offset row index containing the input
   *         block element row index.
   */
  public int blockSubBlockElementRowBasis(int blkElemRow)
  {
    return blkElemRow % aNumSubBlkElemRows;
  }

  /**
   * Returns the sub-block size (element rows/columns square of one matrix
   * sub-block) given the input sub-block size (simple re-return).
   * 
   * @param subBlkSize  The requested sub-block size.
   * @return The input sub-block size.
   */
  public int subBlockSize(int subBlkSize)
  {
    return subBlkSize;
  }

  /**
   * Returns the sub-block size (element rows/columns square of one matrix
   * sub-block) given the current subdivide/merge specification.
   * 
   * @return The sub-block size (element rows/columns square of one matrix
   *         sub-block).
   */
  public int subBlockSize()
  {
    return aCurrSubBlkSize;
  }

  /**
   * Returns the number of sub-block element rows (sub-block rows/columns
   * square) with no subdivision/merge operations (the basis).
   * 
   * @return The sub-block size (element rows/columns square of one matrix
   *         sub-block).
   */
  public int subBlockSizeBasis()
  {
    return aNumSubBlkElemRows;
  }

  /**
   * Returns the number of block sub-blocks (sub-block rows/columns square of
   * a single block) given the input sub-block size.
   * 
   * @param subBlkSize  The requested sub-block size.
   * @return Returns the number of block sub-blocks (sub-block rows/columns square of
   *         a single block)
   */
  public int blockSubBlocks(int subBlkSize)
  {
    return aNumBlkElemRows / subBlkSize;
  }

  /**
   * Returns the number of block sub-blocks (sub-block rows/columns square of
   * a single block) given the current subdivide/merge specification.
   * 
   * @return The number of block sub-blocks (sub-block rows/columns square of
   *         a single block).
   */
  public int blockSubBlocks()
  {
    return blockSubBlocks(aCurrSubBlkSize);
  }

  /**
   * Returns the number of block sub-blocks (sub-block rows/columns square of
   * a single block) with no subdivision/merge operations (the basis).
   * 
   * @return The basis number of block sub-blocks (sub-block rows/columns square
   *         of a single block).
   */
  public int blockSubBlocksBasis()
  {
    return aNumBlkSubBlkRows;
  }

  /**
   * Returns the number of sub-blocks (sub-block rows/columns square) given the
   * input sub-block size.
   * 
   * @param subBlkSize  The requested sub-block size.
   * @return The number of sub-blocks (sub-block rows/columns square).
   */
  public int subBlocks(int subBlkSize)
  {
    return (int) Math.ceil((double) aNumMtrxElemRows / subBlkSize);
  }

  /**
   * Returns the number of sub-blocks (sub-block rows/columns square) given the
   * current subdivide/merge specification.
   * 
   * @return The number of sub-blocks (sub-block rows/columns square).
   */
  public int subBlocks()
  {
    return subBlocks(aCurrSubBlkSize);
  }

  /**
   * Returns the number of sub-blocks (sub-block rows/columns square) with no
   * subdivision/merge operations (the basis).
   * 
   * @return The basis number of sub-blocks (sub-block rows/columns square).
   */
  public int subBlocksBasis()
  {
    return aNumMtrxSubBlkRows;
  }
  
  /**
   * Returns the sub-block row index containing the input matrix element row
   * index given the input sub-block size. The sub-block row
   * returned is as if the entire matrix was composed of sub-blocks and not
   * blocks.
   * 
   * @param mtrxElemRow The matrix element row index for which the
   *                    corresponding sub-block row index will be returned.
   *                    Valid values range from 0 to aNumMtrxElemRows - 1.
   * @param subBlkSize  The requested sub-block size.
   * @return The sub-block row index containing the input matrix element row
   *         index.
   */
  public int subBlockRow(int mtrxElemRow, int subBlkSize)
  {
    return mtrxElemRow / subBlockSize(subBlkSize);
  }
  
  /**
   * Returns the sub-block row index containing the input matrix element row
   * index given the current subdivide/merge specification. The sub-block row
   * returned is as if the entire matrix was composed of sub-blocks and not
   * blocks.
   * 
   * @param mtrxElemRow The matrix element row index for which the corresponding
   *                    sub-block row index will be returned. Valid values
   *                    range from 0 to aNumMtrxElemRows - 1.
   * @return The sub-block row index containing the input matrix element row
   *         index.
   */
  public int subBlockRow(int mtrxElemRow)
  {
    return mtrxElemRow / subBlockSize(aCurrSubBlkSize);
  }
  
  /**
   * Returns the sub-block row index containing the input matrix element row
   * index with no subdivision/merge operations (the basis). The sub-block row
   * returned is as if the entire matrix was composed of sub-blocks and not
   * blocks.
   * 
   * @param mtrxElemRow The matrix element row index for which the corresponding
   *                    sub-block row index will be returned. Valid values
   *                    range from 0 to aNumMtrxElemRows - 1.
   * @return The sub-block row index containing the input matrix element row
   *         index.
   */
  public int subBlockRowBasis(int mtrxElemRow)
  {
    return mtrxElemRow / aNumSubBlkElemRows;
  }

  /**
   * Returns the sub-block element row offset index containing the input matrix
   * element row index given the input sub-block size.
   * 
   * @param mtrxElemRow The matrix element row index for which the
   *                    corresponding sub-block row element offset index will
   *                    be returned. Valid values range from 0 to
   *                    aNumMtrxElemRows - 1.
   * @param subBlkSize  The requested sub-block size.
   * @return The sub-block row element offset index containing the input matrix
   *         element row index.
   */
  public int subBlockElementRow(int mtrxElemRow, int subBlkSize)
  {
    return mtrxElemRow % subBlockSize(subBlkSize);
  }

  /**
   * Returns the sub-block element row offset index containing the input matrix
   * element row index given the current subdivide/merge specification.
   * 
   * @param mtrxElemRow The matrix element row index for which the corresponding
   *                    sub-block row element offset index will be returned.
   *                    Valid values range from 0 to aNumMtrxElemRows - 1.
   * @return The sub-block row element offset index containing the input matrix
   *         element row index.
   */
  public int subBlockElementRow(int mtrxElemRow)
  {
    return mtrxElemRow % subBlockSize(aCurrSubBlkSize);
  }

  /**
   * Returns the sub-block element row offset index containing the input matrix
   * element row index with no subdivision/merge operations (the basis).
   * 
   * @param mtrxElemRow The matrix element row index for which the corresponding
   *                    sub-block row element offset index will be returned.
   *                    Valid values range from 0 to aNumMtrxElemRows - 1.
   * @return The sub-block row element offset index containing the input matrix
   *         element row index.
   */
  public int subBlockElementRowBasis(int mtrxElemRow)
  {
    return mtrxElemRow % aNumSubBlkElemRows;
  }

  /**
   * Returns the sub-block row index within the block that contains the input
   * matrix element row index given the input sub-block size.
   * 
   * @param mtrxElemRow The matrix element row index for which the
   *                    corresponding sub-block row index, offset into its
   *                    containing block, will be returned. Valid values range
   *                    from 0 to aNumBlkSubBlkRows - 1.
   * @param subBlkSize  The requested sub-block size.
   * @return The block sub-block row index containing the input matrix element
   *         row index.
   */
  public int blockSubBlockRowFromMatrixRow(int mtrxElemRow, int subBlkSize)
  {
    return blockElementRow(mtrxElemRow) / blockSubBlocks(subBlkSize);
  }

  /**
   * Returns the sub-block row index within the block that contains the input
   * matrix element row index given the current subdivide/merge specification.
   * 
   * @param mtrxElemRow The matrix element row index for which the corresponding
   *                    sub-block row index, offset into its containing block,
   *                    will be returned. Valid values range
   *                    from 0 to aNumBlkSubBlkRows - 1.
   * @return The block sub-block row index containing the input matrix element
   *         row index.
   */
  public int blockSubBlockRowFromMatrixRow(int mtrxElemRow)
  {
    return blockElementRow(mtrxElemRow) / blockSubBlocks(aCurrSubBlkSize);
  }

  /**
   * Returns the sub-block row index within the block that contains the input
   * matrix element row index with no subdivision/merge operations (the basis).
   * 
   * @param mtrxElemRow The matrix element row index for which the corresponding
   *                    sub-block row index, offset into its containing block,
   *                    will be returned. Valid values range
   *                    from 0 to aNumBlkSubBlkRows - 1.
   * @return The block sub-block row index containing the input matrix element
   *         row index.
   */
  public int blockSubBlockRowFromMatrixRowBasis(int mtrxElemRow)
  {
    return blockElementRow(mtrxElemRow) / aNumBlkSubBlkRows;
  }

  /**
   * Returns the sub-block row index containing the input block element row
   * index given the input sub-block size.
   * 
   * @param blkElemRow  The block element row index for which the
   *                    corresponding sub-block row index will be returned.
   *                    Valid values range from 0 to aNumBlkElemRows - 1.
   * @param subBlkSize  The requested sub-block size.
   * @return The sub-block row index containing the input block element row
   *         index.
   */
  public int blockSubBlockRow(int blkElemRow, int subBlkSize)
  {
    return blkElemRow / subBlockSize(subBlkSize);
  }

  /**
   * Returns the sub-block row index containing the input block element row
   * index given the current subdivide/merge specification.
   * 
   * @param blkElemRow The block element row index for which the corresponding
   *                   sub-block row index will be returned. Valid values range
   *                   from 0 to aNumBlkElemRows - 1.
   * @return The sub-block row index containing the input block element row
   *         index.
   */
  public int blockSubBlockRow(int blkElemRow)
  {
    return blkElemRow / subBlockSize(aCurrSubBlkSize);
  }

  /**
   * Returns the sub-block row index containing the input block element row
   * index with no subdivision/merge operations (the basis).
   * 
   * @param blkElemRow The block element row index for which the corresponding
   *                   sub-block row index will be returned. Valid values range
   *                   from 0 to aNumBlkElemRows - 1.
   * @return The sub-block row index containing the input block element row
   *         index.
   */
  public int blockSubBlockRowBasis(int blkElemRow)
  {
    return blkElemRow / aNumSubBlkElemRows;
  }

  /**
   * Returns the number of sub-blocks in the last block. This value will
   * be equal to or less than the standard number of sub-block in a block
   * (aNumBlkSubBlkRows) given the input sub-block size.
   * 
   * @param subBlkSize  The requested sub-block size.
   * @return The number of element rows in the last sub-block.
   */
  public int getLastBlockSubBlockRows(int subBlkSize)
  {
    return subBlocks(subBlkSize) - (aNumMtrxBlkRows - 1) *
           blockSubBlocks(subBlkSize);
  }

  /**
   * Returns the number of sub-blocks in the last block. This value will
   * be equal to or less than the standard number of sub-block in a block
   * (aNumBlkSubBlkRows) given the current subdivide/merge specification.
   * 
   * @return The number of element rows in the last sub-block.
   */
  public int getLastBlockSubBlockRows()
  {
    return getLastBlockSubBlockRows(aCurrSubBlkSize);
  }

  /**
   * Returns the number of sub-blocks in the last block. This value will
   * be equal to or less than the standard number of sub-block in a block
   * (aNumBlkSubBlkRows) with no subdivision/merge operations (the basis).
   * 
   * @return The number of element rows in the last sub-block.
   */
  public int getLastBlockSubBlockRowsBasis()
  {
    return aLastBlkSubBlkRows;
  }

  /**
   * Returns the number of sub-block rows in the input block given the input
   * sub-block size. If blk is less than the last block index then
   * aNumBlkSubBlkRows is returned. If blk is the last block index then
   * aLastBlkSubBlkRows is returned.
   * 
   * @param blk         The input block index for which the number of
   *                    sub-block rows is returned.
   * @param subBlkSize  The requested sub-block size.
   * @return The number of sub-block rows in the input block.
   */
  public int getBlockSubBlockRows(int blk, int subBlkSize)
  {
    if (blk == aLastBlkIndx)
      return getLastBlockSubBlockRows(subBlkSize);
    else
      return blockSubBlocks(subBlkSize);
  }

  /**
   * Returns the number of sub-block rows in the input block given the current
   * subdivide/merge specification. If blk is less than the last block index
   * then aNumBlkSubBlkRows is returned. If blk is the last block index then
   * aLastBlkSubBlkRows is returned.
   * 
   * @param blk The input block index for which the number of sub-block rows is
   *            returned.
   * @return The number of sub-block rows in the input block.
   */
  public int getBlockSubBlockRows(int blk)
  {
    if (blk == aLastBlkIndx)
      return getLastBlockSubBlockRows(aCurrSubBlkSize);
    else
      return blockSubBlocks(aCurrSubBlkSize);
  }

  /**
   * Returns the number of sub-block rows in the input block with no
   * subdivision/merge operations (the basis). If blk is less than the last
   * block index then aNumBlkSubBlkRows is returned. If blk is the last block
   * index then aLastBlkSubBlkRows is returned.
   * 
   * @param blk The input block index for which the number of sub-block rows is
   *            returned.
   * @return The number of sub-block rows in the input block.
   */
  public int getBlockSubBlockRowsBasis(int blk)
  {
    if (blk == aLastBlkIndx)
      return aLastBlkSubBlkRows;
    else
      return aNumBlkSubBlkRows;
  }

  /**
   * Returns the number of element rows in the last sub-block given the input
   * sub-block size. This value will be equal to or less than the
   * standard number of sub-block element rows (aNumSubBlkElemRows).
   * 
   * @param subBlkSize  The requested sub-block size.
   * @return The number of element rows in the last sub-block.
   */
  public int getLastSubBlockElementRows(int subBlkSize)
  {
    return aNumMtrxElemRows -
           (subBlocks(subBlkSize) - 1) * subBlockSize(subBlkSize);
  }

  /**
   * Returns the number of element rows in the last sub-block given the current
   * subdivide/merge specification. This value will be equal to or less than
   * the standard number of sub-block element rows (aNumSubBlkElemRows).
   * 
   * @return The number of element rows in the last sub-block.
   */
  public int getLastSubBlockElementRows()
  {
    return getLastSubBlockElementRows(aCurrSubBlkSize);
  }

  /**
   * Returns the number of element rows in the last sub-block with no
   * subdivision/merge operations (the basis). This value will be equal to or
   * less than the standard number of sub-block element rows
   * (aNumSubBlkElemRows).
   * 
   * @return The number of element rows in the last sub-block.
   */
  public int getLastSubBlockElementRowsBasis()
  {
    return aLastSubBlkElemRows;
  }

  /**
   * Returns the last sub-block index given the input sub-block size.
   * 
   * @param subBlkSize  The requested sub-block size.
   * @return The last sub-block index.
   */
  public int getLastSubBlockIndex(int subBlkSize)
  {
    return getLastBlockSubBlockRows(subBlkSize) - 1;
  }

  /**
   * Returns the last sub-block index given the current subdivide/merge
   * specification.
   * 
   * @return The last sub-block index.
   */
  public int getLastSubBlockIndex()
  {
    return getLastBlockSubBlockRows(aCurrSubBlkSize) - 1;
  }

  /**
   * Returns the last sub-block index with no subdivision/merge operations
   * (the basis).
   * 
   * @return The last sub-block index.
   */
  public int getLastSubBlockIndexBasis()
  {
    return aLastSubBlkIndx;
  }

  /**
   * Returns the number of element rows in the input blocks sub-block given the
   * input sub-block size. If blk is less than the last block index or
   * if blk is equal to the last block index and subblk is less than the last
   * sub-block index, then aNumSubBlkElemRows is returned. If, however, blk is
   * equal to the last block index and subblk is equal to the last sub-block
   * index then aLastSubBlkElemRows is returned.
   * 
   * @param blk         The input block index for which the number of
   *                    sub-block element rows is returned.
   * @param subblk      The input sub-block index for which the number of
   *                    sub-block element rows is returned.
   * @param subBlkSize  The requested sub-block size.
   * @return The number of sub-block element rows in the input block /
   *         sub-block.
   */
  public int getBlockSubBlockElementRows(int blk, int subblk, int subBlkSize)
  {
    if ((blk == aLastBlkIndx) &&
        (subblk == getLastSubBlockIndex(subBlkSize)))
      return getLastSubBlockElementRows(subBlkSize);
    else
      return subBlockSize(subBlkSize);
  }

  /**
   * Returns the number of element rows in the input blocks sub-block given the
   * current subdivide/merge specification. If blk is less than the last block
   * index or if blk is equal to the last block index and subblk is less than
   * the last sub-block index, then aNumSubBlkElemRows is returned. If, however,
   * blk is equal to the last block index and subblk is equal to the last
   * sub-block index then aLastSubBlkElemRows is returned.
   * 
   * @param blk    The input block index for which the number of sub-block
   *               element rows is returned.
   * @param subblk The input sub-block index for which the number of sub-block
   *               element rows is returned.
   * @return The number of sub-block element rows in the input block /
   *         sub-block.
   */
  public int getBlockSubBlockElementRows(int blk, int subblk)
  {
    if ((blk == aLastBlkIndx) &&
        (subblk == getLastSubBlockIndex(aCurrSubBlkSize)))
      return getLastSubBlockElementRows(aCurrSubBlkSize);
    else
      return subBlockSize(aCurrSubBlkSize);
  }

  /**
   * Returns the number of element rows in the input blocks sub-block with no
   * subdivision/merge operations (the basis). If blk is less than the last
   * block index or if blk is equal to the last block index and subblk is less
   * than the last sub-block index, then aNumSubBlkElemRows is returned. If,
   * however, blk is equal to the last block index and subblk is equal to the
   * last sub-block index then aLastSubBlkElemRows is returned.
   * 
   * @param blk    The input block index for which the number of sub-block
   *               element rows is returned.
   * @param subblk The input sub-block index for which the number of sub-block
   *               element rows is returned.
   * @return The number of sub-block element rows in the input block /
   *         sub-block.
   */
  public int getBlockSubBlockElementRowsBasis(int blk, int subblk)
  {
    if ((blk == aLastBlkIndx) && (subblk == aLastSubBlkIndx))
      return aLastSubBlkElemRows;
    else
      return aNumSubBlkElemRows;
  }

  /**
   * Returns the total number of defined sub-blocks in the symmetric square
   * matrix given the input sub-block size.
   * 
   * @param subBlkSize  The requested sub-block size.
   * @return The total number of defined sub-blocks in the symmetric square
   *         matrix.
   */
  public long symmMatrixSubBlockCount(int subBlkSize)
  {
    int sb = subBlocks(subBlkSize);
    return (long) sb * (sb + 1) / 2;
  }

  /**
   * Returns the total number of defined sub-blocks in the symmetric square
   * matrix given the current subdivide/merge specification.
   * 
   * @return The total number of defined sub-blocks in the symmetric square
   *         matrix.
   */
  public long symmMatrixSubBlockCount()
  {
    int sb = subBlocks(aCurrSubBlkSize);
    return (long) sb * (sb + 1) / 2;
  }

  /**
   * Returns the total number of defined sub-blocks in the symmetric square
   * matrix with no subdivision/merge operations (the basis).
   * 
   * @return The total number of defined sub-blocks in the symmetric square
   *         matrix.
   */
  public long symmMatrixSubBlockCountBasis()
  {
    return (long) aNumMtrxSubBlkRows * (aNumMtrxSubBlkRows + 1) / 2;
  }
}
