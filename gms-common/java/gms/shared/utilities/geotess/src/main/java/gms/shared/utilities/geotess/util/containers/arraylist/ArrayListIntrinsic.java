package gms.shared.utilities.geotess.util.containers.arraylist;

import gms.shared.utilities.geotess.util.filebuffer.FileInputBuffer;
import gms.shared.utilities.geotess.util.filebuffer.FileOutputBuffer;

import java.io.IOException;
import java.io.Serializable;

@SuppressWarnings("serial")
public abstract class ArrayListIntrinsic implements Serializable
{

  /**
   * The size of the ArrayList (the number of elements it contains).
   */
  protected int       aldSize = 0;

  /**
   * Increases the capacity of this <tt>ArrayList</tt> instance, if
   * necessary, to ensure that it can hold at least the number of elements
   * specified by the minimum capacity argument.
   *
   * @param   minCapacity   the desired minimum capacity
   */
  public void ensureCapacity(int minCapacity)
  {
    if (minCapacity > capacity())
    {
      int newCapacity = (int) (((long) capacity() * 3)/2 + 1);
      if (newCapacity < minCapacity) newCapacity = minCapacity;
      copyOf(newCapacity);
    }
  }

  /**
   * Returns the number of elements in this list.
   *
   * @return the number of elements in this list
   */
  public int size()
  {
    return aldSize;
  }

  /**
   * Removes all of the elements from this list.  The list will
   * be empty after this call returns.
   */
  public void clear()
  {
    aldSize = 0;
  }

  /**
   * Returns <tt>true</tt> if this list contains no elements.
   *
   * @return <tt>true</tt> if this list contains no elements
   */
  public boolean isEmpty()
  {
    return (aldSize == 0);
  }

  /**
   * Sets the size equal to the input value sze. The array is ensured to be of
   * the proper capacity before setting the size. The new elements (if any)
   * are not initialized.
   */
  public void setSize(int sze)
  {
    if (sze > 0)
    {
      ensureCapacity(sze);
      aldSize = sze;
    }
  }

  public abstract int capacity();
  protected abstract void copyOf(int newCapacity);
  protected abstract int intrinsicSize();

  /**
   * Fills this array list with the contents of the input file (filename).
   * 
   * @param filename The input file that will be loaded into this array list.
   * @throws IOException
   */
  public void read(String filename) throws IOException
  {
  	FileInputBuffer fib = new FileInputBuffer(filename);
  	read(fib);
  	fib.close();
  }

  /**
   * Reads data from the input file buffer and fills this ArrayList with the
   * contents.
   * 
   * @param fib The FileInputBuffer from which this array list is filled.
   * @throws IOException
   */
  public abstract void read(FileInputBuffer fib) throws IOException;

  /**
   * Writes this huge array list to the input file (filename).
   * 
   * @param filename The output file where this array list will be written.
   * @throws IOException
   */
  public void write(String filename) throws IOException
  {
    FileOutputBuffer fob = new FileOutputBuffer(filename);
    write(fob);
    fob.close();
  }

  /**
   * Writes this array list to the input file output buffer.
   * 
   * @param fob The output file buffer containing the location where this 
   *        array list will be written.
   * @throws IOException
   */
  public abstract void write(FileOutputBuffer fob) throws IOException;
}
