package gms.shared.utilities.geotess.util.containers.hash.sets;

import gms.shared.utilities.geotess.util.containers.hash.HashIntrinsic;

import java.io.Serializable;
import java.util.NoSuchElementException;

/**
 * Space saving intrinsic integer set. Saves space by storing entries using an
 * integer for the key and a next linked-list pointer to the next entry object.
 * This is a total of 4 bytes for the key and 8 bytes for the next pointer
 * (64-bit). Contrast this with the heavy duty Java HashSet interface which uses
 * a HashMap to back the set. The HashMap entry object stores an Integer object
 * to save the key (8 byte address with a 4 byte value), a redundant object
 * value which is not used by the set (8 byte address), a next entry pointer (8
 * bytes), and an integer to store the hash code (4 bytes). This is a total of
 * 8+4+8+8+4 = 32 bytes for the Java HashSet as compared to 12 bytes for this
 * memory efficient version.
 * 
 * <p>
 * In terms of performance the HashSetInteger is generally an improvement also
 * in that most of the object mechanics have been removed in element testing
 * which should produce much faster add/remove/contains tests. An exception to
 * this rule occurs when a table resize is necessary. In the HashMap version the
 * Entry.hash is used to identify the index of each entry from the old table
 * into the new extended table. In this implementation the hash code must be
 * recalculated for each entry transfer from old to new. This means the
 * intrinsic resize operation is somewhat slower than the Java HashMap version.
 * Whenever possible the HashSetInteger should be created using the final or
 * close to final size.
 * 
 * @author jrhipp
 * 
 */
@SuppressWarnings("serial")
public class HashSetInteger extends HashIntrinsic
{
  /**
   * Internal Entry class which holds each integer entry in the hash set. The
   * Entry object also holds a single reference to the next Entry object in a
   * singly-linked list of collisions that have the same hash code (occurs when
   * different input integers generate the same hash code). The Entry object has
   * a constructor and getKey(), equals(int keytst), and toString functions.
   * 
   * @author Jim Hipp
   * 
   */
  private static class Entry implements Serializable
  {
    /**
     * The integer key for this particular entry.
     */
    private final int key;

    /**
     * A reference to the next Entry object in the singly-linked collision list.
     */
    private Entry     next;

    /**
     * Standard constructor.
     * 
     * @param i
     *          The integer key to be stored in the HashSet.
     * @param n
     *          The next entry in the collision list which may be null if this
     *          is the first entry.
     */
    public Entry(int i, Entry n)
    {
      key = i;
      next = n;
    }

    /**
     * The key for this entry.
     * 
     * @return Key for this entry.
     */
    public final int getKey()
    {
      return key;
    }

    /**
     * Returns true if the key equals the input key keytst.
     * 
     * @param keytst
     *          The input key to be tested for equality.
     * @return True if the input key and this.key are equal.
     */
    public final boolean equals(int keytst)
    {
      if (keytst == key)
        return true;
      else
        return false;
    }

    /**
     * Returns the key as a string.
     * 
     * @return The key as a string.
     */
    @Override
    public final String toString()
    {
      return String.valueOf(key);
    }
  }

  /**
   * The table, resized as necessary. Length MUST Always be a power of two.
   */
  private Entry[] table;

  /**
   * Constructs an empty <tt>HashSetIntegereger</tt> with the specified initial
   * capacity and load factor.
   * 
   * @param initialCapacity
   *          the initial capacity
   * @param loadFactor
   *          the load factor
   * @throws IllegalArgumentException
   *           if the initial capacity is negative or the load factor is
   *           nonpositive
   */
  public HashSetInteger(int initialCapacity, float loadFactor)
  {
    super(initialCapacity, loadFactor);
    table = createTable(capMinus1 + 1);
  }

  /**
   * Constructs an empty <tt>HashSetInteger</tt> with the specified initial
   * capacity and the default load factor (0.75).
   * 
   * @param initialCapacity
   *          the initial capacity.
   * @throws IllegalArgumentException
   *           if the initial capacity is negative.
   */
  public HashSetInteger(int initialCapacity)
  {
    this(initialCapacity, DEFAULT_LOAD_FACTOR);
  }

  /**
   * Constructs an empty <tt>HashSetInteger</tt> with the default initial
   * capacity (16) and the default load factor (0.75).
   */
  public HashSetInteger()
  {
    this(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR);
  }

  /**
   * Returns true if the input key is contained in the set.
   */
  public final boolean contains(int key)
  {
    // get the table index from the key and find it, if it exists, in the
    // indexes collision list

    int i = tableIndex(key, capMinus1);
    for (Entry e = table[i]; e != null; e = e.next)
      if (e.key == key) return true;

    // returns false if not found

    return false;
  }

  /**
   * Adds the input key into the hash set. The key is only added if it is not
   * already in the list.
   * 
   * @param key
   *          The key to be inserted into the HashSetInteger.
   */
  public void add(int key)
  {
    // Get the table index from the input key and see if the key exists in the
    // table.

    int i = tableIndex(key, capMinus1);
    for (Entry e = table[i]; e != null; e = e.next)
      if (e.key == key) return;

    // new key ... add it into the HashSet

    addEntry(key, i);
  }

  /**
   * Adds a new entry with the specified key to the specified table index. It is
   * the responsibility of this method to resize the table if appropriate.
   * 
   * @param key
   *          The new key to be added to the HashSet.
   * @param index
   *          The table index into which the key will be inserted.
   */
  private void addEntry(int key, int index)
  {
    // get the current head of the collision list for this table index and
    // create a new entry that will reside at the head of the list. Resize
    // the list if necessary.

    Entry e = table[index];
    table[index] = new Entry(key, e);
    if (size++ >= threshold) resize(2 * table.length);
  }

  /**
   * Rehashes the contents of this set into a new array with a larger capacity.
   * This method is called automatically when the number of keys in this map
   * reaches its threshold.
   * 
   * If current capacity is MAXIMUM_CAPACITY, this method does not resize the
   * map, but sets threshold to Integer.MAX_VALUE. This has the effect of
   * preventing future calls.
   * 
   * @param newCapacity
   *          The new capacity, MUST be a power of two; must be greater than
   *          current capacity unless current capacity is MAXIMUM_CAPACITY (in
   *          which case value is irrelevant).
   */
  private void resize(int newCapacity)
  {
    // if the current capacity is the maximum allowed then set the threshold to
    // Integer.MAX_VALUE and return

    int oldCapacity = table.length;
    if (oldCapacity == MAXIMUM_CAPACITY)
    {
      threshold = Integer.MAX_VALUE;
      return;
    }

    // create a new table ... transfer the entries to the new table and set the
    // new table as current. Re-adjust the threshold and capacity values.

    Entry[] newTable = createTable(newCapacity);
    capMinus1 = newCapacity - 1;
    transfer(newTable);
    table = newTable;
    threshold = (int) (newCapacity * loadFactor);
  }

  /**
   * Transfers all entries from current table to newTable.
   * 
   * @param newTable
   *          The newTable that is twice the size of the old table.
   */
  private void transfer(Entry[] newTable)
  {
    // loop over each entry of the old table and move them to the new table

    for (int j = 0; j < table.length; j++)
    {
      // get the jth entry of the old table and see if it is null

      Entry e = table[j];
      if (e != null)
      {
        // the entry is not null ... set the old table entry to null for the
        // garbage collector and loop over each entry in the collision list

        table[j] = null;
        do
        {
          // get the next entry in the list and insert the current entry (e)
          // into the new table

          Entry next = e.next;
          int i = tableIndex(e.key, capMinus1);
          e.next = newTable[i];
          newTable[i] = e;

          // set the next entry as current and continue with the remaining
          // entries in the list

          e = next;
        } while (e != null);
      }
    }
  }

  /**
   * Removes the entry associated with the specified key in the HashSet. No
   * action is taken if HashSet contains no mapping for this key.
   * 
   * @param key
   *          The key to be removed from the HashSet.
   */
  public final void remove(int key)
  {
    // get the table index for the input key and save the head entry into the
    // local variables prev and e

    int i = tableIndex(key, capMinus1);
    Entry prev = table[i];
    Entry e = prev;

    // loop over each entry in the collision list searching for the entry to be
    // removed

    while (e != null)
    {
      // save the next entry and test that the current entry (e) is the one to
      // be removed

      Entry next = e.next;
      if (e.key == key)
      {
        // found entry to be removed ... decrement size and set table[i] to next
        // if e is the first entry in the list or prev.next to next if otherwise
        // ... return

        size--;
        if (prev == e)
          table[i] = next;
        else
          prev.next = next;
        return;
      }

      // e is not the entry to be removed ... set prev to e and e to the next
      // entry and try again

      prev = e;
      e = next;
    }
  }

  /**
   * Removes all of the mappings from this map. The map will be empty after this
   * call returns.
   */
  @Override
  public void clear()
  {
    // Set each table entry to null and the size to zero

    for (int i = 0; i < table.length; i++)
      table[i] = null;
    size = 0;
  }

  /**
   * Creates a new entry array (table) of size equal to the input capacity.
   * 
   * @param capacity
   *          The size of the entry array.
   * @return A reference to the new entry array.
   */
  private Entry[] createTable(int capacity)
  {
    return new Entry[capacity];
  }

  /**
   * Returns an estimate of the bulk memory size used by this object. The
   * input pointer size (ptrsize) should be 8 for 64-bit and 4 for 32-bit.
   * 
   * @param ptrsize The pointer size set to 8 for 64-bit and 4 for 32-bit.
   * @return The bulk memory estimate in bytes.
   */
  public long memoryEstimate(int ptrsize)
  {
    return (long) ptrsize * (capMinus1 + size + 1) + size * Integer.SIZE / 8;
  }

  /**
   * A built in iterator class used to iterate over all elements of the set.
   * 
   * @author Jim Hipp
   * 
   */
  public class Iterator
  {
    /**
     * The next entry in the HashSet to return.
     */
    Entry next;

    /**
     * The current index in the hash table.
     */
    int   index;

    /**
     * The current entry.
     */
    Entry current; // current entry

    /**
     * Default constructor sets the next entry to the first non-null entry in
     * the HashSet.
     */
    Iterator()
    {
      if (size > 0)
      {
        // advance to first entry

        while (index < table.length && (next = table[index++]) == null);
      }
    }

    /**
     * Returns true if the next element is not null.
     * 
     * @return True if the next element is not null.
     */
    public final boolean hasNext()
    {
      return next != null;
    }

    /**
     * Sets the current entry to the next entry and returns the key.
     * 
     * @return The next entry key.
     */
    public final int next()
    {
      // if the next entry is null throw error.

      Entry e = next;
      if (e == null) throw new NoSuchElementException();

      // if the next entry is null find the next non-null entry in the table

      if ((next = e.next) == null)
      {
        while (index < table.length && (next = table[index++]) == null);
      }

      // set the current entry and return the key

      current = e;
      return e.key;
    }

    /**
     * Removes the current entry.
     */
    public void remove()
    {
      if (current == null) throw new IllegalStateException();
      int k = current.key;
      current = null;
      HashSetInteger.this.remove(k);
    }
  }

  /**
   * Returns an instance of the iterator class.
   * 
   * @return An instance of the iterator class.
   */
  public Iterator iterator()
  {
    return new Iterator();
  }
}
