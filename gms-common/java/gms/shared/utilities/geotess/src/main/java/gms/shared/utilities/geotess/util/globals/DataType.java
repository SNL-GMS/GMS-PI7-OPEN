package gms.shared.utilities.geotess.util.globals;

import java.util.EnumSet;

/**
 * An Enum of DOUBLE, FLOAT, LONG, INT, SHORT and BYTE
 * 
 * @author sballar
 * 
 */
public enum DataType
{
	/**
	 * A single value of type DOUBLE
	 */
	DOUBLE(8),

	/**
	 * A single value of type FLOAT
	 */
	FLOAT(4),

	/**
	 * A single value of type LONG
	 */
	LONG(8),

	/**
	 * A single value of type INT
	 */
	INT(4),

	/**
	 * A single value of type SHORT
	 */
	SHORT(2),

	/**
	 * A single value of type BYTE
	 */
	BYTE(1),

	/**
	 * A custom object of arbitrary size and configuration
	 */
	CUSTOM(-1);
	
	public static final EnumSet<DataType> floatingPointTypes = EnumSet.of(DataType.FLOAT, DataType.DOUBLE);
	
	public static final EnumSet<DataType> integerTypes = EnumSet.of(DataType.LONG, DataType.INT, 
			DataType.SHORT, DataType.BYTE);

	/**
	 * Number of bytes required to store a value 
	 * of the corresponding type.
	 */
	public int nbytes;
	
	DataType(int nbytes)
	{ this.nbytes = nbytes; }

	/**
	 * Returns the DataType of the input number or throws an exception if it is not
	 * one of the supported types.
	 * @param val The number whose DataType will be returned.
	 * @return The DataType of the input number val.
	 */
	public static DataType getDataType(Number val)
	{
		// return data type

		if (val.getClass() == Double.class)
			return DataType.DOUBLE;
		else if (val.getClass() == Float.class)
			return DataType.FLOAT;
		else if (val.getClass() == Long.class)
			return DataType.LONG;
		else if (val.getClass() == Integer.class)
			return DataType.INT;
		else if (val.getClass() == Short.class)
			return DataType.SHORT;
		else if (val.getClass() == Byte.class)
			return DataType.BYTE;
		else
			throw new IllegalArgumentException("\nthe type of fillValue ("
					+ val.getClass().getSimpleName()
					+ ") is not one of the data types \n"
					+ "double, float, long, int, short, byte\n");
	}
}
