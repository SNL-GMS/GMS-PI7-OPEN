package gms.shared.utilities.geotess;

import gms.shared.utilities.geotess.util.globals.DataType;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Scanner;

/**
 * A double[] with nAttributes elements will be associated with each node of the
 * model.
 * 
 * @author Sandy Ballard
 */
public class DataArrayOfDoubles extends Data
{

	double[] values;

	/**
	 * Constructor.
	 * 
	 * @param values
	 */
	public DataArrayOfDoubles(double... values)
	{
		this.values = values;
	}

	/**
	 * Constructor that reads the array of values from an ascii file.
	 * 
	 * @param input
	 * @param nAttributes
	 */
	protected DataArrayOfDoubles(Scanner input, int nAttributes)
	{
		values = new double[nAttributes];
		for (int i = 0; i < values.length; ++i)
			values[i] = input.nextDouble();
	}

	/**
	 * Constructor that reads the array of values from a binary file.
	 * 
	 * @param input
	 * @param nAttributes
	 */
	protected DataArrayOfDoubles(DataInputStream input, int nAttributes)
			throws IOException
	{
		values = new double[nAttributes];
		for (int i = 0; i < values.length; ++i)
			values[i] = input.readDouble();
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
	 * Returns true if this and other are of the same DataType, have the same
	 * number of elements, and all values are == (or both values are NaN).
	 * 
	 * @param other
	 * @return true if this and other are of the same DataType, have the same
	 *         number of elements, and all values are == (or both values are NaN).
	 */
	@Override
	public boolean equals(Object other)
	{
		if (other == null || !(other instanceof DataArrayOfDoubles)
			|| ((DataArrayOfDoubles)other).values.length != this.values.length)
			return false;
		
		for (int i = 0; i < values.length; ++i)
			if (this.values[i] != ((DataArrayOfDoubles)other).getDouble(i) 
			&& !(isNaN(i) && ((DataArrayOfDoubles)other).isNaN(i)))
				return false;
		return true;
	}

	@Override
	public int size()
	{
		return values.length;
	}

	@Override
	public double getDouble(int attributeIndex)
	{
		return values[attributeIndex];
	}

	@Override
	public float getFloat(int attributeIndex)
	{
		return (float) values[attributeIndex];
	}

	@Override
	public long getLong(int attributeIndex)
	{
		return (long) values[attributeIndex];
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
		values[attributeIndex] = value;
		return this;
	}

	@Override
	public Data setValue(int attributeIndex, float value)
	{
		values[attributeIndex] = value;
		return this;
	}

	@Override
	public Data setValue(int attributeIndex, long value)
	{
		values[attributeIndex] = value;
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
//			output.write(" " + Double.toString(values[i]));
//	}

	@Override
	public void write(DataOutputStream output) throws IOException
	{
		for (int i = 0; i < values.length; ++i)
			output.writeDouble(values[i]);
	}

	@Override
	public boolean isNaN(int attributeIndex)
	{
		return Double.isNaN(values[attributeIndex]); 
	}

	@Override
	public Data fill(Number defaultValue)
	{
		Arrays.fill(values, defaultValue.doubleValue());
		return this;
	}

	@Override
	public String toString()
	{
		StringBuffer buf = new StringBuffer();
		buf.append(Double.toString(values[0]));
		for (int i=1; i<values.length; ++i)
			buf.append(" ").append(Double.toString(values[i]));
		return buf.toString();
	}

	@Override
	public String toString(int attributeIndex)
	{
		return Double.toString(values[attributeIndex]);
	}

	@Override
	public Data copy()
	{
		return new DataArrayOfDoubles(values.clone());
	}

}
