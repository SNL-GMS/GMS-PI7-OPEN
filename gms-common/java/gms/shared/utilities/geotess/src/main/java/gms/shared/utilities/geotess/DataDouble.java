package gms.shared.utilities.geotess;

import gms.shared.utilities.geotess.util.globals.DataType;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Scanner;

/**
 * A single value of type double will be associated with each node of the model.
 * 
 * @author Sandy Ballard
 */
public class DataDouble extends Data
{

	double value;

	/**
	 * Constructor.
	 * 
	 * @param value
	 */
	public DataDouble(double value)
	{
		this.value = value;
	}

	/**
	 * Constructor that reads a single value from an ascii file.
	 * 
	 * @param input
	 */
	protected DataDouble(Scanner input)
	{
		value = input.nextDouble();
	}

	/**
	 * Constructor that reads a single value from a binary file.
	 * 
	 * @param input
	 * @throws IOException
	 */
	protected DataDouble(DataInputStream input) throws IOException
	{
		value = input.readDouble();
	}

	/**
	 * Returns DataType.DOUBLE
	 * 
	 * @return DataType.DOUBLE
	 */
	@Override
	public DataType getDataType()
	{
		return DataType.DOUBLE;
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
	 * single element and those elements are == (or both values are NaN).
	 * 
	 * @param other
	 * @return true if this and other are of the same DataType, both have a
	 *         single element and those elements are == (or both values are NaN).
	 */
	@Override
	public boolean equals(Object other)
	{
		if (other == null || !(other instanceof DataDouble))
			return false;
		
		return this.value == ((DataDouble) other).value 
				|| (isNaN(0) && ((DataDouble) other).isNaN(0));
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
			return (float)value;
		
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
			return (long)value;
		
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
			return (int)value;
		
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
			return (short) value;
		
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
		this.value = (double) value;
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
		this.value = (double) value;
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
		this.value = (double) value;
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
		this.value = (double) value;
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
		this.value = (double) value;
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
		this.value = (double) value;
		return this;
	}

//	@Override
//	public void write(Writer output) throws IOException
//	{
//		output.write(" " + Double.toString(value));
//	}

	@Override
	public void write(DataOutputStream output) throws IOException
	{
		output.writeDouble(value);
	}

	@Override
	public boolean isNaN(int attributeIndex)
	{
		return Double.isNaN(value);
	}

	@Override
	public Data fill(Number defaultValue)
	{
		value = defaultValue.doubleValue();
		return this;
	}

	@Override
	public String toString()
	{
		return Double.toString(value);
	}

	@Override
	public Data copy()
	{
		return new DataDouble(value);
	}

}
