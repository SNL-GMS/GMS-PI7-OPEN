package gms.shared.utilities.geotess.util.numerical.matrixblock;

import gms.shared.utilities.geotess.util.filebuffer.FileInputBuffer;
import gms.shared.utilities.geotess.util.filebuffer.FileOutputBuffer;

import java.io.IOException;
import java.io.Serializable;

/**
 * Defines a square matrix as a set of square blocks used by the Out-Of-Core
 * (OOC) Cholesky decomposition and inversion object LSINV. This object
 * maintains the definition and provides static input/output functionality
 * to read and write the blocks from/to a file.
 * 
 * @author jrhipp
 *
 */
@SuppressWarnings("serial")
public class MatrixBlockInfo implements Serializable
{
  /**
   * The size of the matrix (rows/columns square) of the MatrixBlockInfo
   * object.
   */
  private int        aSize    = 0;

  /**
   * The number of blocks (row/column square) of this MatrixBlockInfo object.
   */
  private int        aNumBlks = 0;

  /**
   * The number of matrix rows (row/column square) stored in a single block
   * of the matrix defined by this MatrixBlockInfo object.
   */
  private int        aBlkSize = 0;

  /**
   * The last valid block index (aNumBlks - 1).
   */
  private int        aLstBlkIndx = 0;

  /**
   * The number of rows (row/column square) in the last block of the matrix
   * defined by this MatrixBlockInfo object. This value can range from 1 to
   * aBlkSize depending on whether aBlkSize is an integer multiple of aSize
   * or not.
   */
  private int        aLstBlkSize = 0;

  /**
   * Creates a new MatrixBlockInfo object for a matrix of row/column size
   * (square) with blocks of blksize rows/columns (note blksize <= size).
   * 
   * @param size The size of the matrix (rows/columns square) represented by
   *             this new MatrixBlockInfo object.
   * @param blksize The size of the matrix (rows/columns square) contained in
   *                a single block of the matrix.
   */
  public MatrixBlockInfo(int size, int blksize)
  {
    setBlockInfo(size, blksize);
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
  public MatrixBlockInfo(String fPath) throws IOException
  {
    read(fPath);
  }

  /**
   * Returns the number of rows/columns saved in the block with index blkrow.
   * This value is always aBlkSize, except when blkrow equals the last block
   * row index, in which case it can range from 1 to aBlkSize depending on if
   * the number of matrix rows is an integer multiple of aSize.
   * 
   * @param blkrow The block index for which the number of rows in the block
   *               will be returned.
   * @return The number of matrix rows/columns contained in the block whose
   *         row index is blkrow.
   */
  public int getMaxBlockRows(int blkrow)
  {
    //int nr = aBlkSize;
    //if ((blkrow + 1) * aBlkSize > aSize) nr = aSize - blkrow * aBlkSize;
    //return nr;

    if (blkrow == aLstBlkIndx)
      return aLstBlkSize;
    else
      return aBlkSize;
  }

  /**
   * Returns the number of rows/columns saved in the block with index blkrow.
   * This value is always aBlkSize, except when blkrow equals the last block
   * row index, in which case it can range from 1 to aBlkSize depending on if
   * the number of matrix rows is an integer multiple of aSize.
   * 
   * @param blkrow The block index for which the number of rows in the block
   *               will be returned.
   * @return The number of matrix rows/columns contained in the block whose
   *         row index is blkrow.
   */
  public int getMaxLastBlockRows()
  {
    //int nr = aBlkSize;
    //if ((blkrow + 1) * aBlkSize > aSize) nr = aSize - blkrow * aBlkSize;
    //return nr;

    return aLstBlkSize;
  }

  /**
   * Returns the block index that contains the input matrix row/column.
   * 
   * @param mtrxrow The input matrix row for which its containing block
   *                index will be returned.
   * @return The block index that contains the input matrix row/column index.
   */
  public int getBlockIndex(int mtrxrow)
  {
    return (mtrxrow / aBlkSize);
  }

  /**
   * Returns the block index that contains the input matrix row/column.
   * If the input row/column exceeds the number of entries then the number
   * of available blocks is returned (which is one larger than the last valid
   * block index).
   * 
   * @param mtrxrow The input matrix row for which its containing block
   *                index will be returned.
   * @return The block index that contains the input matrix row/column index.
   *         If the input row/column exceeds the number of entries then the
   *         number of available blocks is returned (which is one larger than
   *         the last valid block index).
   */
  public int getBlockIndexPlus1(int mtrxrow)
  {
    if (mtrxrow >= aSize)
      return aNumBlks;
    else
      return (mtrxrow / aBlkSize);
  }

  /**
   * Returns the matrix size (rows/columns square) defined by this
   * MatrixBlockInfo object.
   * 
   * @return The matrix size (rows/columns square) defined by this
   *         MatrixBlockInfo object.
   */
  public int size()
  {
    return aSize;
  }

  /**
   * Returns the block size defined by this MatrixBlockInfo object.
   * 
   * @return The block size defined by this MatrixBlockInfo object.
   */
  public int blockSize()
  {
    return aBlkSize;
  }

  /**
   * Returns the number of blocks defined by this MatrixBlockInfo object.
   * 
   * @return The number of blocks defined by this MatrixBlockInfo object.
   */
  public int blocks()
  {
    return aNumBlks;
  }

  /**
   * Returns the total number of defined blocks in the symmetric square matrix.
   * 
   * @return The total number of defined blocks in the symmetric square matrix.
   */
  public int symmMatrixBlockCount()
  {
    return aNumBlks * (aNumBlks + 1) / 2;
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
    long mem = 4 * aSize * (aSize + 1);
    return mem;
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
    int sze = fib.readInt();
    int nb  = fib.readInt();
    fib.close();
    setBlockInfo(sze, nb);
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
    fob.writeInt(aSize);
    fob.writeInt(aBlkSize);
    fob.close();
  }

  /**
   * Defines this MatrixBlockInfo object given an input matrix size (square)
   * and the number of rows/columns in each block (the block size).
   * 
   * @param size The total number of rows/columns in a square matrix.
   * @param blksize The total number of rows/columns in a square block of a
   *                matrix (blksize <= size).
   */
  private void setBlockInfo(int size, int blksize)
  {
    // set the size and block size

    aSize    = size;
    aBlkSize = blksize;

    // calculate the number of block rows/columns (square) required to store
    // the entire matrix

    aNumBlks = aSize / aBlkSize;
    if (aNumBlks * aBlkSize < aSize) ++ aNumBlks;

    // for speed save the last block index and the last block size. The last
    // block size is always <= aBlkSize

    aLstBlkIndx = aNumBlks - 1;
    aLstBlkSize = aSize - aLstBlkIndx * aBlkSize;
  }

  /**
   * Reads and returns the matrix block stored in the file fPath_iB_jB, where
   * fPath, iB, and jB are filled with their input string equivalents.
   * The integers iB and jB represent the block row and column index.
   * The input MatrixBlockInfo object (mbi) contains the size information
   * of the total matrix being input from fPath. This function reads the
   * block in transposed fashion so as not to have to iterate over the row
   * index in the inner loop.
   * 
   * @param iB The block row index of the block to be read.
   * @param jB The block column index of the block to be read.
   * @param mbi The MatrixBlockInfo object containing the size information
   *            of the total matrix.
   * @param fPath The path from which the matrix block will be read with
   *              a final file name of fPath_iB_jB.
   * @param trnsps Read the matrix in transpose fashion if true.
   * 
   * @throws IOException
   */
  public static double[][] readBlock(int iB, int jB,
                                     MatrixBlockInfo mbi, String fPath,
                                     boolean trnsps)
                throws IOException
  {
    // open file for read
    
    String f = fPath + "_" + Integer.toString(iB) + "_" + Integer.toString(jB);
    FileInputBuffer fib = new FileInputBuffer(f);

    // set limits

    int nr = mbi.getMaxBlockRows(iB);
    int bs = mbi.blockSize();

    int nrow = nr;
    int jStrt = 0;
    int ncol = nr;
    if ((iB != jB) && trnsps) nrow = bs; 
    
    // create the block to be returned and loop over each row

    double[][] blk = new double [bs][bs];
    for (int i = 0; i < nrow; ++i)
    {
      // set ith row and set inner loop limits

      double[] blki = blk[i];
      if (iB == jB)
      {
        if (trnsps)
          jStrt = i;
        else
          ncol = i + 1;
      }
      else if (!trnsps)
        ncol = bs;

      // read in row data

      for (int j = jStrt; j < ncol; ++j) blki[j] = fib.readDouble(); 
    }

    // close file and return input block

    fib.close();
    return blk;
  }

  /**
   * Writes the input matrix block (blk) to a file fPath_iB_jB, where
   * fPath, iB, and jB are filled with their input string equivalents.
   * The integers iB and jB represent the block row and column index.
   * The input MatrixBlockInfo object (mbi) contains the size information
   * of the total matrix being output to fPath. This function writes
   * the input block in transposed fashion so as not to have to
   * iterate over the row index in the inner loop.
   * 
   * @param blk The matrix grid block to be output.
   * @param iB The block row index of the block to be written.
   * @param jB The block column index of the block to be written.
   * @param mbi The MatrixBlockInfo object containing the size information
   *            of the total matrix.
   * @param fPath The path to where the matrix block will be written with
   *              a final file name of fPath_iB_jB.
   * @param trnsps Write the matrix in transpose fashion if true.
   * 
   * @throws IOException
   */
  public static void writeBlock(double[][] blk, int iB, int jB,
                                MatrixBlockInfo mbi, String fPath,
                                boolean trnsps)
                throws IOException
  {
    // open file for output

    String f = fPath + "_" + Integer.toString(iB) + "_" + Integer.toString(jB);
    FileOutputBuffer fob = new FileOutputBuffer(f);

    // set limits

    int nr = mbi.getMaxBlockRows(iB);
    int bs = mbi.blockSize();

    int nrow = nr;
    int jStrt = 0;
    int ncol = nr;
    if ((iB != jB) && trnsps) nrow = bs; 

    // loop over all rows

    for (int i = 0; i < nrow; ++i)
    {
      // set ith row and inner loop limits

      double[] blki = blk[i];
      if (iB == jB)
      {
        if (trnsps)
          jStrt = i;
        else
          ncol = i + 1;
      }
      else if (!trnsps)
        ncol = bs;

      // write out row data

      for (int j = jStrt; j < ncol; ++j) fob.writeDouble(blki[j]); 
    }

    // close file and exit

    fob.close();
  }
}
