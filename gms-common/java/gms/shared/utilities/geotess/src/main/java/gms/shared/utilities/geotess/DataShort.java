package gms.shared.utilities.geotess;

import gms.shared.utilities.geotess.util.globals.DataType;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Scanner;

/**
 * A single value of type short will be associated with each node of the model.
 * 
 * @author Sandy Ballard
 */
public class DataShort extends Data
{

	short value;

	/**
	 * Constructor.
	 * 
	 * @param value
	 */
	public DataShort(short value)
	{
		this.value = value;
	}

	/**
	 * Constructor that reads a single value from an ascii file.
	 * 
	 * @param input
	 */
	protected DataShort(Scanner input)
	{
		value = input.nextShort();
	}

	/**
	 * Constructor that reads a single value from a binary file.
	 * 
	 * @param input
	 * @throws IOException
	 */
	protected DataShort(DataInputStream input) throws IOException
	{
		value = input.readShort();
	}

	/**
	 * Returns DataType.SHORT
	 * 
	 * @return DataType.SHORT
	 */
	@Override
	public DataType getDataType()
	{
		return DataType.SHORT;
	}

	/**
	 * Retrieve the size of this Data object (always 1).
	 * 
	 * @return 1
	 */
	@Override
	public int size()
	{
		return 1;
	}

	/**
	 * Returns true if this and other are of the same DataType, both have a
	 * single element and those elements are ==
	 * 
	 * @param other
	 * @return true if this and other are of the same DataType, both have a
	 *         single element and those elements are ==
	 */
	@Override
	public boolean equals(Object other)
	{
		return other != null && other instanceof DataShort
				&& this.value == ((DataShort) other).value;
	}

	/**
	 * Retrieve value
	 * 
	 * @param attributeIndex
	 * @return value
	 * @throws IndexOutOfBoundsException
	 */
	@Override
	public double getDouble(int attributeIndex)
	{
		if (attributeIndex == 0)
			return value;
		
		throw new IndexOutOfBoundsException();
	}

	/**
	 * Retrieve value
	 * 
	 * @param attributeIndex
	 * @return value
	 * @throws IndexOutOfBoundsException
	 */
	@Override
	public float getFloat(int attributeIndex)
	{
		if (attributeIndex == 0)
			return value;
		
		throw new IndexOutOfBoundsException();
	}

	/**
	 * Retrieve value
	 * 
	 * @param attributeIndex
	 * @return value
	 * @throws IndexOutOfBoundsException
	 */
	@Override
	public long getLong(int attributeIndex)
	{
		if (attributeIndex == 0)
			return value;
		
		throw new IndexOutOfBoundsException();
	}

	/**
	 * Retrieve value
	 * 
	 * @param attributeIndex
	 * @return value
	 * @throws IndexOutOfBoundsException
	 */
	@Override
	public int getInt(int attributeIndex)
	{
		if (attributeIndex == 0)
			return value;
		
		throw new IndexOutOfBoundsException();
	}

	/**
	 * Retrieve value
	 * 
	 * @param attributeIndex
	 * @return value
	 * @throws IndexOutOfBoundsException
	 */
	@Override
	public short getShort(int attributeIndex)
	{
		if (attributeIndex == 0)
			return value;
		
		throw new IndexOutOfBoundsException();
	}

	/**
	 * Retrieve value
	 * 
	 * @param attributeIndex
	 * @return value
	 * @throws IndexOutOfBoundsException
	 */
	@Override
	public byte getByte(int attributeIndex)
	{
		if (attributeIndex == 0)
			return (byte) value;
		
		throw new IndexOutOfBoundsException();
	}

	/**
	 * Set this.value equal to supplied value
	 * 
	 * @param attributeIndex
	 * @param value the new value to be set
	 * @return a reference to this
	 * @throws IndexOutOfBoundsException
	 */
	@Override
	public Data setValue(int attributeIndex, double value)
	{
		if (attributeIndex != 0) throw new IndexOutOfBoundsException();
		this.value = (short) value;
		return this;
	}

	/**
	 * Set this.value equal to supplied value
	 * 
	 * @param attributeIndex
	 * @param value the new value to be set
	 * @return a reference to this
	 * @throws IndexOutOfBoundsException
	 */
	@Override
	public Data setValue(int attributeIndex, float value)
	{
		if (attributeIndex != 0) throw new IndexOutOfBoundsException();
		this.value = (short) value;
		return this;
	}

	/**
	 * Set this.value equal to supplied value
	 * 
	 * @param attributeIndex
	 * @param value the new value to be set
	 * @return a reference to this
	 * @throws IndexOutOfBoundsException
	 */
	@Override
	public Data setValue(int attributeIndex, long value)
	{
		if (attributeIndex != 0) throw new IndexOutOfBoundsException();
		this.value = (short) value;
		return this;
	}

	/**
	 * Set this.value equal to supplied value
	 * 
	 * @param attributeIndex
	 * @param value the new value to be set
	 * @return a reference to this
	 * @throws IndexOutOfBoundsException
	 */
	@Override
	public Data setValue(int attributeIndex, int value)
	{
		if (attributeIndex != 0) throw new IndexOutOfBoundsException();
		this.value = (short) value;
		return this;
	}

	/**
	 * Set this.value equal to supplied value
	 * 
	 * @param attributeIndex
	 * @param value the new value to be set
	 * @return a reference to this
	 * @throws IndexOutOfBoundsException
	 */
	@Override
	public Data setValue(int attributeIndex, short value)
	{
		if (attributeIndex != 0) throw new IndexOutOfBoundsException();
		this.value = (short) value;
		return this;
	}

	/**
	 * Set this.value equal to supplied value
	 * 
	 * @param attributeIndex
	 * @param value the new value to be set
	 * @return a reference to this
	 * @throws IndexOutOfBoundsException
	 */
	@Override
	public Data setValue(int attributeIndex, byte value)
	{
		if (attributeIndex != 0) throw new IndexOutOfBoundsException();
		this.value = (short) value;
		return this;
	}

//	@Override
//	public void write(Writer output) throws IOException
//	{
//		output.write(" " + Short.toString(value));
//	}

	@Override
	public void write(DataOutputStream output) throws IOException
	{
		output.writeShort(value);
	}

	@Override
	public Data fill(Number defaultValue)
	{
		value = defaultValue.shortValue();
		return this;
	}

	@Override
	public String toString()
	{
		return Short.toString(value);
	}

	@Override
	public Data copy()
	{
		return new DataShort(value);
	}

}
