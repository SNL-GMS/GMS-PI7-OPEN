package gms.shared.utilities.geotess;

import gms.shared.utilities.geotess.util.globals.DataType;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Scanner;

/**
 * A short[] with nAttributes elements will be associated with each node of the
 * model.
 * 
 * @author Sandy Ballard
 */
public class DataArrayOfShorts extends Data
{

	short[] values;

	/**
	 * Constructor.
	 * 
	 * @param values
	 */
	public DataArrayOfShorts(short... values)
	{
		this.values = values;
	}

	/**
	 * Constructor that reads the array of values from an ascii file.
	 * 
	 * @param input
	 * @param nAttributes
	 */
	protected DataArrayOfShorts(Scanner input, int nAttributes)
	{
		values = new short[nAttributes];
		for (int i = 0; i < values.length; ++i)
			values[i] = input.nextShort();
	}

	/**
	 * Constructor that reads the array of values from a binary file.
	 * 
	 * @param input
	 * @param nAttributes
	 */
	protected DataArrayOfShorts(DataInputStream input, int nAttributes)
			throws IOException
	{
		values = new short[nAttributes];
		for (int i = 0; i < values.length; ++i)
			values[i] = input.readShort();
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
		if (other == null || !(other instanceof DataArrayOfShorts)
				|| ((DataArrayOfShorts)other).values.length != this.values.length)
			return false;
		
		for (int i = 0; i < values.length; ++i)
			if (this.values[i] != ((DataArrayOfShorts)other).values[i])
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
		return values[attributeIndex];
	}

	@Override
	public short getShort(int attributeIndex)
	{
		return values[attributeIndex];
	}

	@Override
	public byte getByte(int attributeIndex)
	{
		return (byte) values[attributeIndex];
	}

	@Override
	public Data setValue(int attributeIndex, double value)
	{
		values[attributeIndex] = (short) value;
		return this;
	}

	@Override
	public Data setValue(int attributeIndex, float value)
	{
		values[attributeIndex] = (short) value;
		return this;
	}

	@Override
	public Data setValue(int attributeIndex, long value)
	{
		values[attributeIndex] = (short) value;
		return this;
	}

	@Override
	public Data setValue(int attributeIndex, int value)
	{
		values[attributeIndex] = (short) value;
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
//			output.write(" " + Short.toString(values[i]));
//	}

	@Override
	public void write(DataOutputStream output) throws IOException
	{
		for (int i = 0; i < values.length; ++i)
			output.writeShort(values[i]);
	}

	@Override
	public int size()
	{
		return values.length;
	}

	@Override
	public Data fill(Number defaultValue)
	{
		Arrays.fill(values, defaultValue.shortValue());
		return this;
	}

	@Override
	public String toString()
	{
		StringBuffer buf = new StringBuffer();
		buf.append(Short.toString(values[0]));
		for (int i=1; i<values.length; ++i)
			buf.append(" ").append(Short.toString(values[i]));
		return buf.toString();
	}

	@Override
	public String toString(int attributeIndex)
	{
		return Short.toString(values[attributeIndex]);
	}

	@Override
	public Data copy()
	{
		return new DataArrayOfShorts(values.clone());
	}

}
