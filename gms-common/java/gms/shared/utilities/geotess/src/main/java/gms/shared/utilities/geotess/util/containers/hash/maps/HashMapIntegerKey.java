package gms.shared.utilities.geotess.util.containers.hash.maps;

import gms.shared.utilities.geotess.util.containers.hash.HashIntrinsic;

import java.io.Serializable;
import java.util.NoSuchElementException;

/**
 * Space saving intrinsic integer map. Saves space by storing entries using an
 * integer for the key and a next linked-list pointer to the next entry object.
 * This is a total of 4 bytes for the key, 8 bytes for the next reference, and 8
 * bytes for the value object (64-bit). Contrast this with the heavy duty Java
 * HashMap interface which stores an Integer object to save the key (8 byte
 * address with a 4 byte value), an 8 byte value reference (8 byte address), a
 * next entry pointer (8 bytes), and an integer to store the hash code (4
 * bytes). This is a total of 8+4+8+8+4 = 32 bytes for the Java HashSet as
 * compared to 20 bytes for this memory efficient version.
 * 
 * <p>
 * In terms of performance the HashMapIntegerKey is generally an improvement
 * also in that most of the object mechanics have been removed in element
 * testing which should produce much faster add/remove/contains tests. An
 * exception to this rule occurs when a table resize is necessary. In the Java
 * HashMap version the Entry.hash is used to identify the index of each entry
 * from the old table into the new extended table. In this implementation the
 * hash code must be recalculated for each entry transfer from old to new. This
 * means the intrinsic resize operation is somewhat slower than the Java HashMap
 * version. Whenever possible the HashMapIntegerKey should be created using the
 * final or close to final size.
 * 
 * @author jrhipp
 * 
 */
@SuppressWarnings("serial")
public class HashMapIntegerKey<V> extends HashIntrinsic
{
  /**
   * Internal Entry class which holds each integer entry in the hash map. The
   * Entry object also holds a single reference to the next Entry object in a
   * singly-linked list of collisions that have the same hash code (occurs when
   * different input integers generate the same hash code). The Entry object has
   * a constructor and getKey(), getValue(), setValue(), equals(int keytst), and
   * toString functions.
   * 
   * @author Jim Hipp
   * 
   */
  public static class Entry<V> implements Serializable
  {
    /**
     * The integer key for this particular entry.
     */
    private final int key;

    /**
     * A reference to value associated with the key.
     */
    private V         value;

    /**
     * A reference to the next Entry object in the singly-linked collision list.
     */
    private Entry<V>  next;

    /**
     * Standard constructor.
     * 
     * @param i
     *          The integer key to be stored in the HashMap.
     * @param val
     *          The value associated with the key.
     * @param n
     *          The next entry in the collision list which may be null if this
     *          is the first entry.
     */
    public Entry(int i, V val, Entry<V> n)
    {
      key = i;
      next = n;
      value = val;
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
     * The value for this entry.
     * 
     * @return Value for this entry.
     */
    public final V getValue()
    {
      return value;
    }

    /**
     * Sets the value of this entry and returns the old value.
     * 
     * @param newValue
     *          The new value to be set.
     * @return The old value of the entry.
     */
    public final V setValue(V newValue)
    {
      V oldValue = value;
      value = newValue;
      return oldValue;
    }

    /**
     * Returns true if the key equals the input object o.
     * 
     * @param o
     *          The input entry to be tested for equality.
     * @return True if the input entry and this entry are equal.
     */
    @SuppressWarnings("unchecked")
    @Override
    public final boolean equals(Object o)
    {
      if (!(o instanceof HashMapIntegerKey.Entry)) return false;
      Entry<V> e = (Entry<V>) o;
      if (key == e.key)
      {
        Object v1 = getValue();
        Object v2 = e.getValue();
        if (v1 == v2 || (v1 != null && v1.equals(v2))) return true;
      }
      return false;
    }

    /**
     * Returns the entry as a string.
     * 
     * @return The entry as a string.
     */
    @Override
    public final String toString()
    {
      return key + " = " + value;
    }
  }

  /**
   * The table, resized as necessary. Length MUST Always be a power of two.
   */
  private Entry<V>[] table;

  /**
   * Constructs an empty <tt>HashSetLng</tt> with the specified initial capacity
   * and load factor.
   * 
   * @param initialCapacity
   *          the initial capacity
   * @param loadFactor
   *          the load factor
   * @throws IllegalArgumentException
   *           if the initial capacity is negative or the load factor is
   *           nonpositive
   */
  public HashMapIntegerKey(int initialCapacity, float loadFactor)
  {
    super(initialCapacity, loadFactor);
    table = createTable(capMinus1 + 1);
  }

  /**
   * Constructs an empty <tt>HashSetLng</tt> with the specified initial capacity
   * and the default load factor (0.75).
   * 
   * @param initialCapacity
   *          the initial capacity.
   * @throws IllegalArgumentException
   *           if the initial capacity is negative.
   */
  public HashMapIntegerKey(int initialCapacity)
  {
    this(initialCapacity, DEFAULT_LOAD_FACTOR);
  }

  /**
   * Constructs an empty <tt>HashSetLng</tt> with the default initial capacity
   * (16) and the default load factor (0.75).
   */
  public HashMapIntegerKey()
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
    for (Entry<V> e = table[i]; e != null; e = e.next)
      if (e.key == key) return true;

    // returns false if not found

    return false;
  }

  /**
   * Returns <tt>true</tt> if this map maps one or more keys to the specified
   * value.
   * 
   * @param value
   *          value whose presence in this map is to be tested
   * @return <tt>true</tt> if this map maps one or more keys to the specified
   *         value
   */
  public boolean containsValue(Object value)
  {
    // if value is null test for contains null value.

    if (value == null) return containsNullValue();

    // loop over all map entries and see if value is contained

    for (int i = 0; i < table.length; i++)
      for (Entry<V> e = table[i]; e != null; e = e.next)
        if (value.equals(e.value)) return true;

    // not found ... return false

    return false;
  }

  /**
   * Special-case code for containsValue with null argument
   */
  private boolean containsNullValue()
  {
    // return true if map contains a null

    for (int i = 0; i < table.length; i++)
      for (Entry<V> e = table[i]; e != null; e = e.next)
        if (e.value == null) return true;

    // no null ... return false.

    return false;
  }

  /**
   * Returns the value to which the specified key is mapped, or {@code null} if
   * this map contains no mapping for the key.
   * 
   * <p>
   * More formally, if this map contains a mapping from a key {@code k} to a
   * value {@code v} such that {@code (key == k)}, then this method returns
   * {@code v}; otherwise it returns {@code null}. (There can be at most one
   * such mapping.)
   */
  public V get(int key)
  {
    // get the table index from the key and find it, if it exists, in the
    // indexes collision list and return the associated value.

    int i = tableIndex(key, capMinus1);
    for (Entry<V> e = table[i]; e != null; e = e.next)
      if (key == e.key) return e.value;

    // return null if the key is not mapped

    return null;
  }

  /**
   * Returns the value to which the specified key is mapped, or {@code
   * null} if this map contains no mapping for the key.
   * 
   * <p>
   * More formally, if this map contains a mapping from a key {@code k} to a
   * value {@code v} such that {@code key.equals(k))}, then this method returns
   * {@code v}; otherwise it returns {@code null}. (There can be at most
   * one such mapping.)
   */
  public Entry<V> getEntry(int key)
  {
    // get the table index from the key and find it, if it exists, in the
    // indexes collision list and return the associated value.

    int i = tableIndex(key, capMinus1);
    for (Entry<V> e = table[i]; e != null; e = e.next)
      if (key == e.key) return e;

    // return null if the key is not mapped

    return null;
  }

  /**
   * Associates the specified value with the specified key in this map. If the
   * map previously contained a mapping for the key, the old value is replaced.
   * 
   * @param key
   *          key with which the specified value is to be associated
   * @param value
   *          value to be associated with the specified key
   * @return the previous value associated with <tt>key</tt>, or <tt>null</tt>
   *         if there was no mapping for <tt>key</tt>. (A <tt>null</tt> return
   *         can also indicate that the map previously associated <tt>null</tt>
   *         with <tt>key</tt>.)
   */
  public V put(int key, V value)
  {
    // Get the table index from the input key and see if the key exists in the
    // table.

    int i = tableIndex(key, capMinus1);
    for (Entry<V> e = table[i]; e != null; e = e.next)
    {
      if (key == e.key)
      {
        // key exists ... save old value for return and assign new value

        V oldValue = e.value;
        e.value = value;
        return oldValue;
      }
    }

    // key not found ... create a new entry at table index i assigning the
    // input key to the input value ... return null

    addEntry(key, value, i);
    return null;
  }

  /**
   * Adds a new entry with the specified key to the specified table index. It is
   * the responsibility of this method to resize the table if appropriate.
   * 
   * @param key
   *          The new key to be added to the HashSet.
   * @param value
   *          The value to be associated with the input key.
   * @param index
   *          The table index into which the key/value pair will be inserted.
   */
  private void addEntry(int key, V value, int index)
  {
    // get the current head of the collision list for this table index and
    // create a new entry that will reside at the head of the list. Resize
    // the list if necessary.

    Entry<V> e = table[index];
    table[index] = new Entry<V>(key, value, e);
    if (size++ >= threshold) resize(2 * table.length);
  }

  /**
   * Rehashes the contents of this map into a new array with a larger capacity.
   * This method is called automatically when the number of keys in this map
   * reaches its threshold.
   * 
   * If current capacity is MAXIMUM_CAPACITY, this method does not resize the
   * map, but sets threshold to Integer.MAX_VALUE. This has the effect of
   * preventing future calls.
   * 
   * @param newCapacity
   *          the new capacity, MUST be a power of two; must be greater than
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

    Entry<V>[] newTable = createTable(newCapacity);
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
  private void transfer(Entry<V>[] newTable)
  {
    // loop over each entry of the old table and move them to the new table

    for (int j = 0; j < table.length; j++)
    {
      // get the jth entry of the old table and see if it is null

      Entry<V> e = table[j];
      if (e != null)
      {
        // the entry is not null ... set the old table entry to null for the
        // garbage collector and loop over each entry in the collision list

        table[j] = null;
        do
        {
          // get the next entry in the list and insert the current entry (e)
          // into the new table

          Entry<V> next = e.next;
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
   * Removes and returns the entry associated with the specified key in the
   * HashMap. Returns null if the HashMap contains no mapping for this key.
   * 
   * @param key
   *          The key to be removed from the HashMap.
   */
  public final Entry<V> remove(int key)
  {
    // get the table index for the input key and save the head entry into the
    // local variables prev and e

    int i = tableIndex(key, capMinus1);
    Entry<V> prev = table[i];
    Entry<V> e = prev;

    // loop over each entry in the collision list searching for the entry to be
    // removed

    while (e != null)
    {
      // save the next entry and test that the current entry (e) is the one to
      // be removed

      Entry<V> next = e.next;
      if (key == e.key)
      {
        // found entry to be removed ... decrement size and set table[i] to next
        // if e is the first entry in the list or prev.next to next if otherwise
        // ... return

        size--;
        if (prev == e)
          table[i] = next;
        else
          prev.next = next;
        return e;
      }

      // e is not the entry to be removed ... set prev to e and e to the next
      // entry and try again

      prev = e;
      e = next;
    }

    // return the value associated with the entry that was removed.

    return e;
  }

  /**
   * Removes all of the mappings from this map. The map will be empty after this
   * call returns.
   */
  @Override
  public void clear()
  {
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
  @SuppressWarnings("unchecked")
  private Entry<V>[] createTable(int capacity)
  {
    return new Entry[capacity];
  }

  /**
   * Returns an estimate of the bulk memory size used by this object. The
   * input pointer size (ptrsize) should be 8 for 64-bit and 4 for 32-bit.
   * Note that the size of V is not included in the estimate. This would
   * have to be added, times the size of this map, to the total.
   * 
   * @param ptrsize The pointer size set to 8 for 64-bit and 4 for 32-bit.
   * @return The bulk memory estimate in bytes.
   */
  public long memoryEstimate(int ptrsize)
  {
    return (long) ptrsize * (capMinus1 + size + 1) +
                  size * (ptrsize + Integer.SIZE / 8);
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
    Entry<V> next;

    /**
     * The current index in the hash table.
     */
    int      index;

    /**
     * The current entry.
     */
    Entry<V> current;

    /**
     * Default constructor sets the next entry to the first non-null entry in
     * the HashMap.
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
     * Returns the next entry in the HashMap.
     * 
     * @return The next entry in the HashMap.
     */
    public Entry<V> nextEntry()
    {
      // if the next entry is null throw error.

      Entry<V> e = next;
      if (e == null) throw new NoSuchElementException();

      // if the next entry is null find the next non-null entry in the table

      if ((next = e.next) == null)
      {
        while (index < table.length && (next = table[index++]) == null);
      }

      // set the current entry and return the key

      current = e;
      return e;
    }

    /**
     * Sets the next entry and return its associated value.
     * 
     * @return The next entries associated value.
     */
    public V next()
    {
      return nextEntry().value;
    }

    /**
     * Removes the current entry.
     */
    public void remove()
    {
      if (current == null) throw new IllegalStateException();
      int k = current.key;
      current = null;
      HashMapIntegerKey.this.remove(k);
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
