package gms.shared.utilities.geotess;

import gms.shared.utilities.geotess.util.globals.DataType;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Writer;
import java.util.Scanner;

/**
 * An array of DataCustom objects with nAttributes elements will be associated
 * with each node of the model.
 *
 * @author jrhipp
 *
 */
public class DataArrayOfCustoms extends Data
{
	Data[] values;

	/**
	 * Constructor.
	 * 
	 * @param values
	 */
	public DataArrayOfCustoms(Data... values)
	{
		this.values = values;
	}

	public void setValues(Data[] vals)
	{
		values = vals;
	}

	/**
	 * Constructor that reads the array of values from an ascii file given a
	 * GeoTessMetaData data description (Legacy).
	 * 
	 * @param input    Scanner from which the data array of custom objects is read.
	 * @param metadata GeoTessMetaData describing the attributes.
	 */
	protected DataArrayOfCustoms(Scanner input, GeoTessMetaData metaData)
	{
		this(input, metaData.getNodeAttributes());
	}

	/**
	 * Constructor that reads the array of values from an ascii file given a
	 * AttributeDataDefinitions data description.
	 * 
	 * @param input   Scanner from which the data array of custom objects is read.
	 * @param attrDef AttributeDataDefinitions describing the attributes.
	 */
	protected DataArrayOfCustoms(Scanner input, AttributeDataDefinitions attrDef)
	{
		values = new Data[attrDef.getNAttributes()];
		for (int i = 0; i < values.length; ++i)
			values[i] = (Data) attrDef.getCustomDataType().read(input, attrDef);
	}

	/**
	 * Constructor that reads the array of values from a binary file given a
	 * GeoTessMetaData data description (Legacy).
	 * 
	 * @param input    DataInputStream from which the data array of custom objects
	 *                 are read.
	 * @param metadata GeoTessMetaData describing the attributes.
	 */
	protected DataArrayOfCustoms(DataInputStream input, GeoTessMetaData metaData)
			throws IOException
	{
		this(input, metaData.getNodeAttributes());
	}

	/**
	 * Constructor that reads the array of values from a binary file given a
	 * AttributeDataDefinitions data description.
	 * 
	 * @param input   DataInputStream from which the data array of custom objects
	 *                are read.
	 * @param attrDef AttributeDataDefinitions describing the attributes.
	 */
	protected DataArrayOfCustoms(DataInputStream input, AttributeDataDefinitions attrDef)
			throws IOException
	{
		values = new Data[attrDef.getNAttributes()];
		for (int i = 0; i < values.length; ++i)
			values[i] = (Data) attrDef.getCustomDataType().read(input, attrDef);
	}

	/**
	 * Returns DataType.CUSTOM
	 * 
	 * @return DataType.CUSTOM
	 */
	@Override
	public DataType getDataType()
	{
		return DataType.CUSTOM;
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
		if (other == null || !(other instanceof DataArrayOfCustoms))
			return false;
		
		for (int i = 0; i < values.length; ++i)
			if (!values[i].equals(other)) return false;
		return true;
	}

	public Data getData(int attributeIndex)
	{
		return values[attributeIndex];
	}

	@Override
	public int size()
	{
		return values.length;
	}

	@Override
	public double getDouble(int attributeIndex)
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public float getFloat(int attributeIndex)
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long getLong(int attributeIndex)
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getInt(int attributeIndex)
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public short getShort(int attributeIndex)
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public byte getByte(int attributeIndex)
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Data setValue(int attributeIndex, double value)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Data setValue(int attributeIndex, float value)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Data setValue(int attributeIndex, long value)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Data setValue(int attributeIndex, int value)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Data setValue(int attributeIndex, short value)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Data setValue(int attributeIndex, byte value)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Data fill(Number fillValue)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void write(Writer output) throws IOException
	{
		for (int i = 0; i < values.length; ++i)
			values[i].write(output);
	}

	@Override
	public void write(DataOutputStream output) throws IOException
	{
		for (int i = 0; i < values.length; ++i)
			values[i].write(output);
	}
    @Override
    public String toString()
    {
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < values.length; ++i)
			buf.append(values[i].toString());
		return buf.toString();
    }

	@Override
	public String toString(int attributeIndex)
	{
		return values[attributeIndex].toString();
	}

	@Override
	public Data copy()
	{
		Data[] newValues = new Data[values.length];
		for (int i = 0; i < values.length; ++i)
			newValues[i] = values[i].copy();
		return new DataArrayOfCustoms(newValues);
	}
}
