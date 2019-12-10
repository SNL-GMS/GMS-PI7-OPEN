package gms.shared.utilities.geotess;

// practice merge

import gms.shared.utilities.geotess.util.globals.DataType;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Writer;
import java.util.Scanner;

/**
 * Abstract class that manages the data values attached to single grid node in
 * the model. Data objects have no information about the position in the model
 * where the data is located.
 * 
 * <p>
 * There is no public constructor. Call the constructor of one of the derived
 * classes to obtain an instance.
 * 
 * @author Sandy Ballard
 * 
 */
public abstract class Data 
{
	/**
	 * Default constructor.
	 */
	protected Data()
	{
	}
	
	/**
	 * Retrieve either a DataDouble or a DataArrayOfDoubles, 
	 * depending on the number of supplied values.
	 * @param data
	 * @return DataDouble or a DataArrayOfDoubles
	 */
	public static Data getDataDouble(double ... data)
	{
		switch (data.length)
		{
		case 0:
			return null;
		case 1:
			return new DataDouble(data[0]);
		default:
			return new DataArrayOfDoubles(data);
		}
	}

	/**
	 * Retrieve either a DataFloat or a DataArrayOfFloat, 
	 * depending on the number of supplied values.
	 * @param data
	 * @return DataFloat or a DataArrayOfFloats
	 */
	public static Data getDataFloat(float ... data)
	{
		switch (data.length)
		{
		case 0:
			return null;
		case 1:
			return new DataFloat(data[0]);
		default:
			return new DataArrayOfFloats(data);
		}
	}

	/**
	 * Retrieve either a DataLong or a DataArrayOfLongs, 
	 * depending on the number of supplied values.
	 * @param data
	 * @return DataLong or a DataArrayOfLongs
	 */
	public static Data getDataLong(long ... data)
	{
		switch (data.length)
		{
		case 0:
			return null;
		case 1:
			return new DataLong(data[0]);
		default:
			return new DataArrayOfLongs(data);
		}
	}

	/**
	 * Retrieve either a DataInt or a DataArrayOfInts, 
	 * depending on the number of supplied values.
	 * @param data
	 * @return DataInt or a DataArrayOfInts
	 */
	public static Data getDataInt(int ... data)
	{
		switch (data.length)
		{
		case 0:
			return null;
		case 1:
			return new DataInt(data[0]);
		default:
			return new DataArrayOfInts(data);
		}
	}

	/**
	 * Retrieve either a DataShort or a DataArrayOfShorts, 
	 * depending on the number of supplied values.
	 * @param data
	 * @return DataShort or a DataArrayOfShorts
	 */
	public static Data getDataShort(short ... data)
	{
		switch (data.length)
		{
		case 0:
			return null;
		case 1:
			return new DataShort(data[0]);
		default:
			return new DataArrayOfShorts(data);
		}
	}

	/**
	 * Retrieve either a DataByte or a DataArrayOfBytes, 
	 * depending on the number of supplied values.
	 * @param data
	 * @return DataByte or a DataArrayOfBytes
	 */
	public static Data getDataByte(byte ... data)
	{
		switch (data.length)
		{
		case 0:
			return null;
		case 1:
			return new DataByte(data[0]);
		default:
			return new DataArrayOfBytes(data);
		}
	}

	/**
	 * Factory method that will return a Data of the specified DataType
	 * with all values initialized to 0. Note: This static method cannot be
	 * called directly for DataCustom types as the method has no way of knowing
	 * which concrete instance of DataCustom to instantiate. Use the static
	 * method defining AttributeDataDefinitions or GeoTessMetaData below instead.
	 * 
	 * @param dataType the type of the requested Data object.
	 * @param nAttributes
	 * @return a Data object of the correct derived type
	 * @throws GeoTessException
	 */
	public static Data getData(DataType dataType, int nAttributes)
	{
		switch (dataType)
		{
		case DOUBLE:
			return (nAttributes == 1 ? new DataDouble(0)
					: new DataArrayOfDoubles(new double[nAttributes]));
		case FLOAT:
			return (nAttributes == 1 ? new DataFloat(0)
					: new DataArrayOfFloats(new float[nAttributes]));
		case LONG:
			return (nAttributes == 1 ? new DataLong(0)
					: new DataArrayOfLongs(new long[nAttributes]));
		case INT:
			return (nAttributes == 1 ? new DataInt(0)
					: new DataArrayOfInts(new int[nAttributes]));
		case SHORT:
			return (nAttributes == 1 ? new DataShort((short)0)
					: new DataArrayOfShorts(new short[nAttributes]));
		case BYTE:
			return (nAttributes == 1 ? new DataByte((byte)0)
					: new DataArrayOfBytes(new byte[nAttributes]));
		case CUSTOM:
			throw new IllegalArgumentException(dataType.toString()
					+ " CUSTGOM data type is not supported by this method");
		}
		throw new IllegalArgumentException(dataType.toString()
				+ " is not a recognized data type");
	}

	/**
	 * Factory method that will return a Data object with all values initialized
	 * to 0 given the input GeoTessMetaData object which provides the data
	 * specification.
	 * 
	 * @param dataType The type of the requested Data object (This is redundant).
	 * @param metaData GeoTessMetaData providing the data specification.
	 * @return A Data object of the correct derived type
	 * @throws GeoTessException
	 */
	public static Data getData(DataType dataType, GeoTessMetaData metaData)
	{
		return getData(metaData.getNodeAttributes());
	}
	
	/**
	 * Factory method that will return a Data object with all values initialized
	 * to 0 given the input AttributeDataDefinitions object which provides the
	 * data specification.
	 * 
	 * @param attrDef AttributeDataDefinitions providing the data specification.
	 * @return A Data object of the correct derived type
	 * @throws GeoTessException
	 */
	public static Data getData(AttributeDataDefinitions attrDef)
	{
		int nAttributes = attrDef.getNAttributes();
		switch (attrDef.getDataType())
		{
		case DOUBLE:
			return (nAttributes == 1 ? new DataDouble(0)
					: new DataArrayOfDoubles(new double[nAttributes]));
		case FLOAT:
			return (nAttributes == 1 ? new DataFloat(0)
					: new DataArrayOfFloats(new float[nAttributes]));
		case LONG:
			return (nAttributes == 1 ? new DataLong(0)
					: new DataArrayOfLongs(new long[nAttributes]));
		case INT:
			return (nAttributes == 1 ? new DataInt(0)
					: new DataArrayOfInts(new int[nAttributes]));
		case SHORT:
			return (nAttributes == 1 ? new DataShort((short)0)
					: new DataArrayOfShorts(new short[nAttributes]));
		case BYTE:
			return (nAttributes == 1 ? new DataByte((byte)0)
					: new DataArrayOfBytes(new byte[nAttributes]));
		case CUSTOM:
			return (nAttributes == 1 ?
					attrDef.getCustomDataType().getNew()
			: new DataArrayOfCustoms(
					attrDef.getCustomDataType().getNew(nAttributes)));
		}
		throw new IllegalArgumentException(attrDef.getDataType().toString()
				+ " is not a recognized data type");
	}

	/**
	 * Factory method that will return a Data of the correct derived type as
	 * specified by the input GeoTessMetaData object. The data are read from a
	 * Scanner object which is assumed to be at the correct position in the
	 * underlying ASCII stream.
	 * 
	 * @param input    The scanner from which the data is read.
	 * @param metaData The GeoTessMetaData object containing the data
	 *                 specification.
	 * @return A Data object of the correct derived type.
	 * @throws GeoTessException
	 */
	protected static Data getData(Scanner input, GeoTessMetaData metaData)
			throws IOException
	{
		return getData(input, metaData.getNodeAttributes());
	}
	
	/**
	 * Factory method that will return a Data of the correct derived type as
	 * specified by the input AttributeDataDefinitions object. The data are read
	 * from a Scanner object which is assumed to be at the correct position in the
	 * underlying ASCII stream.
	 * 
	 * @param input    The Scanner from which the data is read.
	 * @param metaData The AttributeDataDefinitions object containing the data
	 *                 specification.
	 * @return A Data object of the correct derived type.
	 * @throws GeoTessException
	 */
	public static Data getData(Scanner input, AttributeDataDefinitions attrDef)
			throws IOException
	{
		switch (attrDef.getDataType())
		{
		case DOUBLE:
			return (attrDef.getNAttributes() == 1 ? new DataDouble(input)
					: new DataArrayOfDoubles(input, attrDef.getNAttributes()));
		case FLOAT:
			return (attrDef.getNAttributes() == 1 ? new DataFloat(input)
					: new DataArrayOfFloats(input, attrDef.getNAttributes()));
		case LONG:
			return (attrDef.getNAttributes() == 1 ? new DataLong(input)
					: new DataArrayOfLongs(input, attrDef.getNAttributes()));
		case INT:
			return (attrDef.getNAttributes() == 1 ? new DataInt(input)
					: new DataArrayOfInts(input, attrDef.getNAttributes()));
		case SHORT:
			return (attrDef.getNAttributes() == 1 ? new DataShort(input)
					: new DataArrayOfShorts(input, attrDef.getNAttributes()));
		case BYTE:
			return (attrDef.getNAttributes() == 1 ? new DataByte(input)
					: new DataArrayOfBytes(input, attrDef.getNAttributes()));
		case CUSTOM:
			return (attrDef.getNAttributes() == 1 ?
					attrDef.getCustomDataType().read(input, attrDef) :
					new DataArrayOfCustoms(input, attrDef));
		}
		throw new IOException(attrDef.getDataType().toString()
				+ " is not a recognized data type");
	}

	/**
	 * Factory method that will return a Data of the correct derived type as
	 * specified by the input GeoTessMetaData object. The data are read from a
	 * DataInputStream object which is assumed to be at the correct position in
	 * the underlying ASCII stream.
	 * 
	 * @param input    The DataInputStream from which the data is read.
	 * @param metaData The GeoTessMetaData object containing the data
	 *                 specification.
	 * @return A Data object of the correct derived type.
	 * @throws GeoTessException
	 */
	protected static Data getData(DataInputStream input,
			GeoTessMetaData metaData) throws GeoTessException, IOException
	{
		return getData(input, metaData.getNodeAttributes());
	}
	
	/**
	 * Factory method that will return a Data of the correct derived type as
	 * specified by the input AttributeDataDefinitions object. The data are read
	 * from a DataInputStream object which is assumed to be at the correct
	 * position in the underlying ASCII stream.
	 * 
	 * @param input    The DataInputStream from which the data is read.
	 * @param metaData The AttributeDataDefinitions object containing the data
	 *                 specification.
	 * @return A Data object of the correct derived type.
	 * @throws GeoTessException
	 */
	public static Data getData(DataInputStream input,
			AttributeDataDefinitions attrDef) throws IOException
	{
		switch (attrDef.getDataType())
		{
		case DOUBLE:
			return (attrDef.getNAttributes() == 1 ? new DataDouble(input)
					: new DataArrayOfDoubles(input, attrDef.getNAttributes()));
		case FLOAT:
			return (attrDef.getNAttributes() == 1 ? new DataFloat(input)
					: new DataArrayOfFloats(input, attrDef.getNAttributes()));
		case LONG:
			return (attrDef.getNAttributes() == 1 ? new DataLong(input)
					: new DataArrayOfLongs(input, attrDef.getNAttributes()));
		case INT:
			return (attrDef.getNAttributes() == 1 ? new DataInt(input)
					: new DataArrayOfInts(input, attrDef.getNAttributes()));
		case SHORT:
			return (attrDef.getNAttributes() == 1 ? new DataShort(input)
					: new DataArrayOfShorts(input, attrDef.getNAttributes()));
		case BYTE:
			return (attrDef.getNAttributes() == 1 ? new DataByte(input)
					: new DataArrayOfBytes(input, attrDef.getNAttributes()));
		case CUSTOM:
			return (attrDef.getNAttributes() == 1 ?
					attrDef.getCustomDataType().read(input, attrDef) :
					new DataArrayOfCustoms(input, attrDef));			
		}
		throw new IOException(attrDef.getDataType().toString()
				+ " is not a recognized data type");
	}

	/**
	 * Read the attribute values for this Data object from the supplied
	 * Scanner in ASCII format into this Data object. This method needs to be
	 * overridden by a concrete class that handles the specific type read.
	 *  
	 * @param input    An ASCII Scanner stream containing the input to be read.
	 * @param metaData GeoTessMetaData object describing the data to be
	 *                 read.
	 * @return Reference to Data object read from input.
	 */
	public Data read(Scanner input, GeoTessMetaData metaData)
	{ return read(input, metaData.getNodeAttributes()); }

	/**
	 * Read the attribute values for this Data object from the supplied
	 * binary DataInputStream into this Data object. This method needs to be
	 * overridden by a concrete class that handles the specific type read.
	 *  
	 * @param input    A DataInputStream containing the input to be read.
	 * @param metaData GeoTessMetaData object describing the data to be
	 *                 read.
	 * @return Reference to Data object read from input.
	 */
	public Data read(DataInputStream input, GeoTessMetaData metaData) throws IOException
	{ return read(input, metaData.getNodeAttributes()); }

	/**
	 * Read the attribute values for this Data object from the supplied
	 * Scanner in ASCII format into this Data object. This method needs to be
	 * overridden by a concrete class that handles the specific type read.
	 *  
	 * @param input   An ASCII Scanner stream containing the input to be read.
	 * @param attrDef AttributeDataDefinitions object describing the data to be
	 *                read.
	 * @return Reference to Data object read from input.
	 */
	public Data read(Scanner input, AttributeDataDefinitions attrDef)
	{ return null; }

	/**
	 * Read the attribute values for this Data object from the supplied
	 * binary DataInputStream into this Data object. This method needs to be
	 * overridden by a concrete class that handles the specific type read.
	 *  
	 * @param input   A DataInputStream containing the input to be read.
	 * @param attrDef AttributeDataDefinitions object describing the data to be
	 *                read.
	 * @return Reference to Data object read from input.
	 */
	public Data read(DataInputStream input, AttributeDataDefinitions attrDef) throws IOException
	{ return null; }

	/**
	 * Retrieve the DataType, one of [ DOUBLE, FLOAT, LONG, INT, SHORT, BYTE, CUSTOM ].
	 * 
	 * @return DataType, one of [ DOUBLE, FLOAT, LONG, INT, SHORT, BYTE, CUSTOM ].
	 */
	public abstract DataType getDataType();

	/**
	 * Retrieve the number of attribute values stored.
	 * @return the number of attribute values stored.
	 */
	public abstract int size();

	/**
	 * Return true if the value of the specified attribute is NaN.
	 * Data objects of type byte, short, int and long always return
	 * false.  Only data objects of type float and double can 
	 * return true.
	 * @param attributeIndex
	 * @return true if the value of the specified attribute is NaN.
	 */
	public boolean isNaN(int attributeIndex)
	{
		// for bytes, shorts, ints and longs, always return false.
		// This method will be overridden for floats and doubles
		return false;
	}

	/**
	 * Retrieve the value of the specified attribute, cast to type double
	 * if necessary.
	 * @param attributeIndex
	 * @return the value of the specified attribute, cast to type double
	 * if necessary.
	 */
	public abstract double getDouble(int attributeIndex);

	/**
	 * Retrieve the value of the specified attribute, cast to type float
	 * if necessary.
	 * @param attributeIndex
	 * @return the value of the specified attribute, cast to type float
	 * if necessary.
	 */
	public abstract float getFloat(int attributeIndex);

	/**
	 * Retrieve the value of the specified attribute, cast to type long
	 * if necessary.
	 * @param attributeIndex
	 * @return the value of the specified attribute, cast to type long
	 * if necessary.
	 */
	public abstract long getLong(int attributeIndex);

	/**
	 * Retrieve the value of the specified attribute, cast to type int
	 * if necessary.
	 * @param attributeIndex
	 * @return the value of the specified attribute, cast to type int
	 * if necessary.
	 */
	public abstract int getInt(int attributeIndex);

	/**
	 * Retrieve the value of the specified attribute, cast to type short
	 * if necessary.
	 * @param attributeIndex
	 * @return the value of the specified attribute, cast to type short
	 * if necessary.
	 */
	public abstract short getShort(int attributeIndex);

	/**
	 * Retrieve the value of the specified attribute, cast to type byte
	 * if necessary.
	 * @param attributeIndex
	 * @return the value of the specified attribute, cast to type byte
	 * if necessary.
	 */
	public abstract byte getByte(int attributeIndex);

	/**
	 * Set the value of the specified attribute to the specified value.
	 * The value will be cast to the DataType stored in this Data object,
	 * if necessary.
	 * @param attributeIndex
	 * @param value
	 * @return a reference to this.
	 */
	public abstract Data setValue(int attributeIndex, double value);

	/**
	 * Set the value of the specified attribute to the specified value.
	 * The value will be cast to the DataType stored in this Data object,
	 * if necessary.
	 * @param attributeIndex
	 * @param value
	 * @return a reference to this.
	 */
	public abstract Data setValue(int attributeIndex, float value);

	/**
	 * Set the value of the specified attribute to the specified value.
	 * The value will be cast to the DataType stored in this Data object,
	 * if necessary.
	 * @param attributeIndex
	 * @param value
	 * @return a reference to this.
	 */
	public abstract Data setValue(int attributeIndex, long value);

	/**
	 * Set the value of the specified attribute to the specified value.
	 * The value will be cast to the DataType stored in this Data object,
	 * if necessary.
	 * @param attributeIndex
	 * @param value
	 * @return a reference to this.
	 */
	public abstract Data setValue(int attributeIndex, int value);

	/**
	 * Set the value of the specified attribute to the specified value.
	 * The value will be cast to the DataType stored in this Data object,
	 * if necessary.
	 * @param attributeIndex
	 * @param value
	 * @return a reference to this.
	 */
	public abstract Data setValue(int attributeIndex, short value);

	/**
	 * Set the value of the specified attribute to the specified value.
	 * The value will be cast to the DataType stored in this Data object,
	 * if necessary.
	 * @param attributeIndex
	 * @param value
	 * @return a reference to this.
	 */
	public abstract Data setValue(int attributeIndex, byte value);
	
	/**
	 * Set all the attribute values stored in this Data object to the 
	 * specified value.
	 * @param fillValue
	 * @return a reference to this.
	 */
	public abstract Data fill(Number fillValue);

	/**
	 * Write the data to the specified destination in ascii format.
	 * 
	 * @param output
	 *            the ascii output file
	 * @throws IOException
	 */
	public void write(Writer output) throws IOException
	{
		output.write(toString());
	}

	/**
	 * Write the data to the specified binary file.
	 * 
	 * @param output
	 * @throws IOException
	 */
	public abstract void write(DataOutputStream output) throws IOException;

	@Override
	public abstract String toString();
	
	/**
	 * Convert the data value at index attributeIndex to a String
	 * using full precision.
	 * @param attributeIndex
	 * @return String representation of attribute value using full precision.
	 */
	public String toString(int attributeIndex)
	{
		// all the derived classes that are array-based will override this method.
		// For derived classes with only one element, this will work.
		return toString();
	}

	/**
	 * Retrieve a deep copy of this data object.
	 * @return a deep copy of this data object.
	 */
	public abstract Data copy();

}
