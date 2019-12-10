package gms.shared.utilities.geotess;

import gms.shared.utilities.geotess.util.globals.DataType;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.HashMap;
import java.util.Scanner;

/**
 * A simple class used to associate a set of attribute names and units, and
 * their data type, with an access index. This class is used by GeoTessModel to
 * define the names,units, and type of the data entries stored at each unique
 * model node. It can be used in general to link attribute names, units, and
 * types to a storage index that are associated with arbitrary kinds of objects.
 * This class provides all getters/setters and read/write functionality
 * necessary to operate in the GeoTessModel object collection.
 * 
 * This class also defines the static custom data object map containing any
 * type of object implementing the DataCustom interface that can be stored in
 * GeoTess Data objects. An application must add the DataCustom derivative to
 * the map before it/they can be used (addCustomData(...)).
 * 
 * @author jrhipp
 *
 */
public class AttributeDataDefinitions
{
	/**
	 * One of DOUBLE, FLOAT, LONG, INT, SHORTINT, BYTE, or CUSTOM; All of the
	 * data stored in Data Objects using this attribute definition will use this
	 * data type.
	 */
	private DataType dataType;

	/**
	 * The number of attributes names (and associated unit strings) assigned to
	 * this AttributeDataDefinitions object. This value is only set by the method
	 * 
	 *    void setAttributes(String[] names, String[] units);
	 */
	private int nAttributes = -1;

	/**
	 * The names of the attributes stored by this AttributeDataDefinitions object.
	 * The length of this array must (will) be equal to nAttributes.
	 */
	private String[] attributeNames = null;

	/**
	 * The units that correspond to the attributes stored in the model. The
	 * length of this array must (will) be equal to nAttributes.
	 */
	private String[] attributeUnits = null;

	/**
	 * The inverse map associating the attribute names to their storage index.
	 */
	private HashMap<String, Integer> attributeIndexMap = null;

	/**
	 * Static map of DataCustom objects of which one can by a GeoTessMetaData
	 * object for populating the GeoTessModel for which the GeoTessMetaData is
	 * a member. Use of DataCustom objects is optional.
	 */
	private static HashMap<String, DataCustom> customDataObjectMap = null;

	/**
	 * This meta-data objects customDataInitializer if not null. Set by a call
	 * to function setDataType(String dataType) where dataType is a string
	 * defined as a key in DataObjectMap.
	 */
	private DataCustom customDataInitializer = null;
	
	/**
	 * Default constructor leaving the contents in an unassigned state.
	 */
	public AttributeDataDefinitions()
	{
		// no code
	}

	/**
	 * Copy constructor.
	 * 
	 * @param attrnames The input AttributeDataDefinitions from which this new
	 *                  object is constructed.
	 */
	public AttributeDataDefinitions(AttributeDataDefinitions attrnames)
	{
		dataType = attrnames.dataType;
		setAttributes(attrnames.getAttributeNames(), attrnames.getAttributeUnits());
	}

	/**
	 * DataInputStream constructor.
	 * 
	 * @param input The DataInputStream from which this object is constructed.
	 * @throws IOException
	 */
	public AttributeDataDefinitions(DataInputStream input) throws IOException
	{
		read(input);
	}

	/**
	 * ASCII Scanner constructor.
	 * 
	 * @param input The Scanner from which this object is constructed.
	 * @throws IOException
	 */
	public AttributeDataDefinitions(Scanner input) throws IOException
	{
		read(input);
	}

	/**
	 * Standard constructor that sets the attribute names and units. The lengths
	 * of the two String[] arrays must be equal.

	 * @param names The attribute name array.
	 * @param units The attribute unit array.
	 */
	public AttributeDataDefinitions(String[] names, String[] units, DataType dataType)
	{
		setAttributes(names, units);
		this.dataType = dataType;
	}

	/**
	 * Standard constructor that sets the attribute names and units. The input
	 * string names and units are separated by ";" and must have the same number
	 * of entries.

	 * @param names The string of attribute names separated by ";".
	 * @param units The string of attribute units separated by ";".
	 */
	public AttributeDataDefinitions(String names, String units, DataType dataType)
	{
		setAttributes(names, units);
		this.dataType = dataType;
	}

	/**
	 * Set the attribute names and units. The lengths of the two String[] must
	 * be equal.
	 * 
	 * @param names
	 * @param units
	 * @throws IllegalArgumentException
	 */
	public void setAttributes(String[] names, String[] units)
	{
		if (names.length != units.length)
			throw new IllegalArgumentException(String.format(
					"Number of attributeUnits (%d) is not equal to "
							+ "number of attributeNames (%d)", units.length,
					names.length));

		nAttributes = names.length;

		attributeNames = new String[nAttributes];
		attributeUnits = new String[nAttributes];

		attributeIndexMap = new HashMap<String, Integer>();
		for (int i = 0; i < nAttributes; ++i)
		{
			attributeNames[i] = names[i].trim();
			attributeUnits[i] = units[i].trim();
			attributeIndexMap.put(attributeNames[i],  i);
		}
	}

	/**
	 * Set the attribute names and units. After parsing, the number of names and
	 * units must be equal.
	 * 
	 * @param names
	 *            a single string with attribute names separated by ';'
	 * @param units
	 *            a single string with attribute units separated by ';'
	 * @throws GeoTessException
	 */
	public void setAttributes(String names, String units)
	{
		setAttributes(names.split(";"), units.split(";"));
	}

	/**
	 * Retrieve the number of attributes supported by the model.
	 * 
	 * @return the number of attributes supported by the model.
	 */
	public int getNAttributes()
	{
		return nAttributes;
	}

	/**
	 * For every attribute supported by this model, add attribute
	 * to the supplied collection of attributes.
	 * The supplied collection of attributes is not cleared before
	 * the addition.
	 * 
	 * @return a reference to the supplied collection of attributes.
	 */
	public Collection<String> getAttributeNames(Collection<String> attributes)
	{
		for (String attributeName : attributeNames)
			attributes.add(attributeName);
		return attributes;
	}

	/**
	 * For every attribute unit supported by this model, add unit
	 * to the supplied collection of unit names.
	 * The supplied collection of unit names is not cleared before
	 * the addition.
	 * 
	 * @return a reference to the supplied collection of attributes.
	 */
	public Collection<String> getAttributeUnits(Collection<String> units)
	{
		for (String attributeUnit : attributeUnits)
			units.add(attributeUnit);
		return units;
	}

	/**
	 * Retrieve the index of the specified attribute name, or -1 if the
	 * specified attribute does not exist. Case sensitive.
	 * 
	 * @param name The attribute name whose index will be returned.
	 * @return The index of the specified attribute name, or -1 if the specified
	 *         attribute does not exist.
	 */
	public int getAttributeIndex(String name)
	{
		Integer i = attributeIndexMap.get(name);
		return (i == null) ? -1 : i;
	}

	/**
	 * Returns true if the input attribute name is defined.
	 * 
	 * @param name The attribute name to be tested.
	 * @return True if the input attribute name is defined.
	 */
	public boolean isAttributeDefined(String name)
	{
		return (attributeIndexMap.get(name) == null ? false : true);
	}

	/**
	 * Retrieve a string containing all the attribute names separated by ';'
	 * 
	 * @return A string containing all the attribute names separated by ';'
	 */
	public String getAttributeNamesString()
	{
		String s = attributeNames[0];
		for (int i = 1; i < attributeNames.length; ++i)
			s = s + "; " + attributeNames[i];
		return s;
	}

	/**
	 * Retrieve a string containing all the attribute units separated by ';'
	 * 
	 * @return A string containing all the attribute units separated by ';'
	 */
	public String getAttributeUnitsString()
	{
		String s = attributeUnits[0];
		for (int i = 1; i < attributeUnits.length; ++i)
			s = s + "; " + attributeUnits[i];
		return s;
	}

	/**
	 * Retrieve a copy of the array containing the names of the attributes
	 * supported by the model.
	 * 
	 * @return A copy of the array containing the names of the attributes
	 *         supported by the model.
	 */
	public String[] getAttributeNames()
	{
		return attributeNames.clone();
	}

	/**
	 * Retrieve a copy of the array containing the units of the attributes
	 * supported by the model.
	 * 
	 * @return A copy of the array containing the units of the attributes
	 *         supported by the model.
	 */
	public String[] getAttributeUnits()
	{
		return attributeUnits.clone();
	}

	/**
	 * Retrieve the name of the i'th attribute supported by the model.
	 * 
	 * @param i The index of the attribute name to be returned.
	 * @return The name of the i'th attribute supported by the model.
	 */
	public String getAttributeName(int i)
	{
		return attributeNames[i];
	}

	/**
	 * Retrieve the units of the i'th attribute supported by the model.
	 * 
	 * @param i The index of the attribute unit to be returned.
	 * @return The units of the i'th attribute supported by the model.
	 */
	public String getAttributeUnit(int i)
	{
		return attributeUnits[i];
	}
	
	/**
	 * Fills the input buffer with the undefined components if this
	 * AttributeDataDefinitions object is undefined. No action is taken if this
	 * AttributeDataDefinitions object is defined.
	 * 
	 * @param buf The input StringBuffer containing the appended undefined
	 *            component description if this AttributeDataDefinitions object is
	 *            not defined. 
	 */
	public void checkComplete(StringBuffer buf)
	{
		if (!isDefined())
		{
			if (dataType == null)
				buf.append(GeoTessUtils.NL).append(
						"dataType has not been specified.");
			if (attributeNames == null)
				buf.append(GeoTessUtils.NL).append(
						"attributeNames has not been specified.");
			else if (attributeUnits == null)
				buf.append(GeoTessUtils.NL).append(
						"attributeUnits has not been specified.");
			else if (nAttributes < 0)
				buf.append(GeoTessUtils.NL).append("nAttributes < 0.");
		}
	}

	/**
	 * Return the type of all the data stored in the model; Will be one of
	 * DOUBLE, FLOAT, LONG, INT, SHORTINT, BYTE.
	 * 
	 * @return the dataType
	 */
	public DataType getDataType()
	{
		return dataType;
	}

	/**
	 * Specify the type of the data that is stored in the model.
	 * <ul>
	 * One of
	 * <li>DataType.DOUBLE
	 * <li>DataType.FLOAT
	 * <li>DataType.LONG
	 * <li>DataType.INT
	 * <li>DataType.SHORT
	 * <li>DataType.BYTE
	 * </ul>
	 * 
	 * @param dataType
	 *            the dataType to set
	 */
	public void setDataType(DataType dataType)
	{
		this.dataType = dataType;
	}

	/**
	 * Specify the type of the data that is stored in the model; Must be one of
	 * DOUBLE, FLOAT, LONG, INT, SHORT, BYTE, name of a CUSTOM type saved into
	 * the dataObjectMap.
	 * 
	 * @param dataType The dataType to set.
	 * @throws Exception 
	 */
	public void setDataType(String dataType) throws IOException
	{
		try
		{
			this.dataType = DataType.valueOf(dataType.trim().toUpperCase());
		}
		catch (Exception ex)
		{
			if(customDataObjectMap == null || !customDataObjectMap.containsKey(dataType))
				throw new IOException (ex);
			
			this.dataType = DataType.CUSTOM;
			customDataInitializer = customDataObjectMap.get(dataType);
		}
	}

	public static void addCustomDataType(DataCustom dc)
	{
		if (customDataObjectMap == null)
			customDataObjectMap = new HashMap<String, DataCustom>();
		
		customDataObjectMap.put(dc.getDataTypeString(),  dc);
	}

	public static void removeCustomDataType(String dataType)
	{
		if (customDataObjectMap != null) customDataObjectMap.remove(dataType);
	}

	public static void clearCustomDataTypes()
	{
		if (customDataObjectMap != null) customDataObjectMap.clear();
	}

	public DataCustom getCustomDataType()
	{
		return customDataInitializer;
	}
	
	/**
	 * This reads an all set-able fields for this AttributeDataDefinition which
	 * includes the data type. Legacy uses read names and units at different
	 * position in the input stream than where the data type was read. This
	 * method is the correct choice for new use cases.
	 * 
	 * @param input The DataInputStream from which this AttributeDataDefinitions
	 *              object is read.
	 * @throws IOException
	 */
	public void readAll(DataInputStream input) throws IOException
	{
		read(input);
		setDataType(GeoTessUtils.readString(input));
	}
	
	/**
	 * Reads this AttributeDataDefinitions object from the input DataInputStream.
	 * Only names and units are read.
	 * 
	 * @param input The DataInputStream from which this AttributeDataDefinitions
	 *              object is read.
	 * @throws IOException
	 */
	public void read(DataInputStream input) throws IOException
	{
		setAttributes(GeoTessUtils.readString(input).split(";"),
				          GeoTessUtils.readString(input).split(";"));
	}

	/**
	 * Writes all of this AttributeDataDefinitions fields including the data type.
	 * Legacy uses of this method wrote names and units at different locations
	 * in the output stream than data type. Future uses of "write" methods should
	 * use this method.
	 * 
	 * @param output The DataOutputStream object into which this
	 *               AttributeDataDefinitions object is written.
	 * @throws IOException
	 */
	public void writeAll(DataOutputStream output) throws IOException
	{
		write(output);

		if (dataType == DataType.CUSTOM)
			GeoTessUtils.writeString(output, customDataInitializer.getDataTypeString());
		else
		    GeoTessUtils.writeString(output, dataType.toString());
	}

	/**
	 * Writes this AttributeDataDefinitions object contents to the input
	 * DataOutputStream object.
	 * 
	 * @param output The DataOutputStream object into which this
	 *               AttributeDataDefinitions object is written.
	 * @throws IOException
	 */
	public void write(DataOutputStream output) throws IOException
	{
		GeoTessUtils.writeString(output, getAttributeNamesString());
		GeoTessUtils.writeString(output, getAttributeUnitsString());
	}
	
	/**
	 * This reads an all set-able fields for this AttributeDataDefinition which
	 * includes the data type. Legacy uses read names and units at different
	 * position in the input stream than where the data type was read. This
	 * method is the correct choice for new use cases.
	 * 
	 * @param input The DataInputStream from which this AttributeDataDefinitions
	 *              object is read.
	 * @throws IOException
	 */
	public void readAll(Scanner input) throws IOException
	{
		read(input);
		setDataType(input.nextLine());
	}

	/**
	 * Reads this AttributeDataDefinitions object from the input Scanner.
	 * 
	 * @param input The Scanner from which this AttributeDataDefinitions object
	 *              is read.
	 * @throws IOException
	 */
	public void read(Scanner input) throws IOException
	{
		String attributes = input.nextLine();
		if (!attributes.startsWith("attributes:"))
			throw new IOException(
					String.format(
							"Expected to read string starting with 'attributes:' but found '%s'",
							attributes));
	
		String units = input.nextLine();
		if (!units.startsWith("units:"))
			throw new IOException(
					String.format(
							"Expected to read string starting with 'units:' but found '%s'",
							units));
	
		setAttributes(attributes.substring(11), units.substring(6));
	}

	/**
	 * Writes all of this AttributeDataDefinitions fields including the data type.
	 * Legacy uses of this method wrote names and units at different locations
	 * in the output stream than data type. Future uses of "write" methods should
	 * use this method.
	 * 
	 * @param output The Writer object into which this AttributeDataDefinitions
	 *               object is written.
	 * @throws IOException
	 */
	public void writeAll(Writer output) throws IOException
	{
		write(output);

		if (dataType == DataType.CUSTOM)
			output.write(String.format("%s%n", customDataInitializer.getDataTypeString()));
		else
			output.write(String.format("%s%n", dataType.toString()));
	}

	/**
	 * Writes this AttributeDataDefinitions object contents to the input Writer
	 * object.
	 * 
	 * @param output The Writer object into which this AttributeDataDefinitions
	 *               object is written.
	 * @throws IOException
	 */
	public void write(Writer output) throws IOException
	{
		output.write(String.format("attributes: %s%n", getAttributeNamesString()));
		output.write(String.format("units: %s%n", getAttributeUnitsString()));
	}

	/**
	 * Returns true if this AttributeDataDefinitions object is properly defined.
	 * 
	 * @return True if this AttributeDataDefinitions object is properly defined.
	 */
	public boolean isDefined()
	{
		return nAttributes > 0;
	}

	/**
	 * Returns the contents of this AttributeDataDefinitions object as a String.
	 */
	@Override
	public String toString()
	{
		StringBuffer buf = new StringBuffer();

		buf.append("DataType: ").append(dataType.toString())
		   .append(GeoTessUtils.NL).append(GeoTessUtils.NL);

		buf.append("Attributes:").append(GeoTessUtils.NL);
		for (int i = 0; i < nAttributes; ++i)
			buf.append(String.format("%4d: %s (%s)%n", i,
					       attributeNames[i], attributeUnits[i]));
		buf.append(GeoTessUtils.NL);

		return buf.toString();
	}
}
