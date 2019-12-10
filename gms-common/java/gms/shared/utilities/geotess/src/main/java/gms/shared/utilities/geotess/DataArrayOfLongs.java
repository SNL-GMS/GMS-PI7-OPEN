package gms.shared.utilities.geotess;

import gms.shared.utilities.geotess.util.globals.DataType;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Scanner;

/**
 * A long[] with nAttributes elements will be associated with each node of the
 * model.
 * 
 * @author Sandy Ballard
 */
public class DataArrayOfLongs extends Data
{

	long[] values;

	/**
	 * Constructor.
	 * 
	 * @param values
	 */
	public DataArrayOfLongs(long... values)
	{
		this.values = values;
	}

	/**
	 * Constructor that reads the array of values from an ascii file.
	 * 
	 * @param input
	 * @param nAttributes
	 */
	protected DataArrayOfLongs(Scanner input, int nAttributes)
	{
		values = new long[nAttributes];
		for (int i = 0; i < values.length; ++i)
			values[i] = input.nextLong();
	}

	/**
	 * Constructor that reads the array of values from a binary file.
	 * 
	 * @param input
	 * @param nAttributes
	 */
	protected DataArrayOfLongs(DataInputStream input, int nAttributes)
			throws IOException
	{
		values = new long[nAttributes];
		for (int i = 0; i < values.length; ++i)
			values[i] = input.readLong();
	}

	/**
	 * Returns DataType.LONG
	 * 
	 * @return DataType.LONG
	 */
	@Override
	public DataType getDataType()
	{
		return DataType.LONG;
	}

	/**
	 * Returns true if this and other are of the same DataType, have the same
	 * number of elements, and all values are ==
	 * 
	 * @param other
	 * @return true if this and other are of the same DataType, have the same
	 *         number of elements, and all values are ==
	 */
	@Override
	public boolean equals(Object other)
	{
		if (other == null || !(other instanceof DataArrayOfLongs)
				|| ((DataArrayOfLongs)other).values.length != this.values.length)
			return false;
		
		for (int i = 0; i < values.length; ++i)
			if (this.values[i] != ((DataArrayOfLongs)other).values[i])
				return false;
		return true;
	}

	@Override
	public double getDouble(int attributeIndex)
	{
		return values[attributeIndex];
	}

	@Override
	public float getFloat(int attributeIndex)
	{
		return values[attributeIndex];
	}

	@Override
	public long getLong(int attributeIndex)
	{
		return values[attributeIndex];
	}

	@Override
	public int getInt(int attributeIndex)
	{
		return (int) values[attributeIndex];
	}

	@Override
	public short getShort(int attributeIndex)
	{
		return (short) values[attributeIndex];
	}

	@Override
	public byte getByte(int attributeIndex)
	{
		return (byte) values[attributeIndex];
	}

	@Override
	public Data setValue(int attributeIndex, double value)
	{
		values[attributeIndex] = (int) value;
		return this;
	}

	@Override
	public Data setValue(int attributeIndex, float value)
	{
		values[attributeIndex] = (int) value;
		return this;
	}

	@Override
	public Data setValue(int attributeIndex, long value)
	{
		values[attributeIndex] = (int) value;
		return this;
	}

	@Override
	public Data setValue(int attributeIndex, int value)
	{
		values[attributeIndex] = value;
		return this;
	}

	@Override
	public Data setValue(int attributeIndex, short value)
	{
		values[attributeIndex] = value;
		return this;
	}

	@Override
	public Data setValue(int attributeIndex, byte value)
	{
		values[attributeIndex] = value;
		return this;
	}

//	@Override
//	public void write(Writer output) throws IOException
//	{
//		for (int i = 0; i < values.length; ++i)
//			output.write(" " + Long.toString(values[i]));
//	}

	@Override
	public void write(DataOutputStream output) throws IOException
	{
		for (int i = 0; i < values.length; ++i)
			output.writeLong(values[i]);
	}

	@Override
	public int size()
	{
		return values.length;
	}

	@Override
	public Data fill(Number defaultValue)
	{
		Arrays.fill(values, defaultValue.longValue());
		return this;
	}

	@Override
	public String toString()
	{
		StringBuffer buf = new StringBuffer();
		buf.append(Long.toString(values[0]));
		for (int i=1; i<values.length; ++i)
			buf.append(" ").append(Long.toString(values[i]));
		return buf.toString();
	}
	
	@Override
	public String toString(int attributeIndex)
	{
		return Long.toString(values[attributeIndex]);
	}

	@Override
	public Data copy()
	{
		return new DataArrayOfLongs(values.clone());
	}

}
