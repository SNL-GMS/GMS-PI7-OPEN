package gms.shared.utilities.geotess;

import gms.shared.utilities.geotess.util.globals.DataType;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Scanner;

/**
 * A single value of type byte will be associated with each node of the model.
 * 
 * @author Sandy Ballard
 */
public class DataByte extends Data
{

	byte value;

	/**
	 * Constructor.
	 * 
	 * @param value
	 */
	public DataByte(byte value)
	{
		this.value = value;
	}

	/**
	 * Constructor that reads a single value from an ascii file.
	 * 
	 * @param input
	 */
	protected DataByte(Scanner input)
	{
		value = input.nextByte();
	}

	/**
	 * Constructor that reads a single value from a binary file.
	 * 
	 * @param input
	 * @throws IOException
	 */
	protected DataByte(DataInputStream input) throws IOException
	{
		value = input.readByte();
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
		return other != null && other instanceof DataByte 
				&& this.value == ((DataByte) other).value;
	}

	/**
	 * Returns DataType.BYTE
	 * 
	 * @return DataType.BYTE
	 */
	@Override
	public DataType getDataType()
	{
		return DataType.BYTE;
	}

	/**
	 * Retrieve value cast from byte to double
	 * 
	 * @param attributeIndex
	 * @return value cast from byte to double
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
	 * Retrieve value cast from byte to float
	 * 
	 * @param attributeIndex
	 * @return value cast from byte to float
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
	 * Retrieve value cast from byte to long
	 * 
	 * @param attributeIndex
	 * @return value cast from byte to long
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
	 * Retrieve value cast from byte to int
	 * 
	 * @param attributeIndex
	 * @return value cast from byte to int
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
	 * Retrieve value cast from byte to short
	 * 
	 * @param attributeIndex
	 * @return value cast from byte to short
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
	 * Retrieve the value stored in this Data object
	 * 
	 * @param attributeIndex
	 * @return the value stored in this Data object
	 * @throws IndexOutOfBoundsException
	 */
	@Override
	public byte getByte(int attributeIndex)
	{
		if (attributeIndex == 0)
			return value;
		
		throw new IndexOutOfBoundsException();
	}

	/**
	 * Set this.value equal to supplied value cast to byte.
	 * 
	 * @param attributeIndex
	 * @param value
	 *            the new value to be set
	 * @return a reference to this
	 * @throws IndexOutOfBoundsException
	 */
	@Override
	public Data setValue(int attributeIndex, double value)
	{
		if (attributeIndex != 0) throw new IndexOutOfBoundsException();
		this.value = (byte) value;
		return this;
	}

	/**
	 * Set this.value equal to supplied value cast to byte.
	 * 
	 * @param attributeIndex
	 * @param value
	 *            the new value to be set
	 * @return a reference to this
	 * @throws IndexOutOfBoundsException
	 */
	@Override
	public Data setValue(int attributeIndex, float value)
	{
		if (attributeIndex != 0) throw new IndexOutOfBoundsException();
		this.value = (byte) value;
		return this;
	}

	/**
	 * Set this.value equal to supplied value cast to byte.
	 * 
	 * @param attributeIndex
	 * @param value
	 *            the new value to be set
	 * @return a reference to this
	 * @throws IndexOutOfBoundsException
	 */
	@Override
	public Data setValue(int attributeIndex, long value)
	{
		if (attributeIndex != 0) throw new IndexOutOfBoundsException();
		this.value = (byte) value;
		return this;
	}

	/**
	 * Set this.value equal to supplied value cast to byte.
	 * 
	 * @param attributeIndex
	 * @param value
	 *            the new value to be set
	 * @return a reference to this
	 * @throws IndexOutOfBoundsException
	 */
	@Override
	public Data setValue(int attributeIndex, int value)
	{
		if (attributeIndex != 0) throw new IndexOutOfBoundsException();
		this.value = (byte) value;
		return this;
	}

	/**
	 * Set this.value equal to supplied value cast to byte.
	 * 
	 * @param attributeIndex
	 * @param value
	 *            the new value to be set
	 * @return a reference to this
	 * @throws IndexOutOfBoundsException
	 */
	@Override
	public Data setValue(int attributeIndex, short value)
	{
		if (attributeIndex != 0) throw new IndexOutOfBoundsException();
		this.value = (byte) value;
		return this;
	}

	/**
	 * Set this.value equal to supplied value
	 * 
	 * @param attributeIndex
	 * @param value
	 *            the new value to be set
	 * @return a reference to this
	 * @throws IndexOutOfBoundsException
	 */
	@Override
	public Data setValue(int attributeIndex, byte value)
	{
		if (attributeIndex != 0) throw new IndexOutOfBoundsException();
		this.value = (byte) value;
		return this;
	}

//	@Override
//	public void write(Writer output) throws IOException
//	{
//		output.write(" " + Byte.toString(value));
//	}

	@Override
	public void write(DataOutputStream output) throws IOException
	{
		output.writeByte(value);
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

	@Override
	public Data fill(Number defaultValue)
	{
		value = defaultValue.byteValue();
		return this;
	}

	@Override
	public String toString()
	{
		return Byte.toString(value);
	}

	@Override
	public Data copy()
	{
		return new DataByte(value);
	}

}
