package gms.shared.utilities.geotess.util.propertiesplus;

import gms.shared.utilities.geotess.util.containers.arraylist.ArrayListDouble;
import gms.shared.utilities.geotess.util.containers.arraylist.ArrayListFloat;
import gms.shared.utilities.geotess.util.containers.arraylist.ArrayListInt;
import gms.shared.utilities.geotess.util.exceptions.GMPException;
import gms.shared.utilities.geotess.util.globals.GMTFormat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Scanner;
import java.util.SimpleTimeZone;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.TreeSet;


/**
 * <p>Title: </p>
 *
 * <p>Extends java Properties with methods to retrieve values of type double,
 * int, boolean, File, GeoVector, time and Date. </p>
 *
 * <p>Copyright: Copyright (c) 2008</p>
 *
 * @author Sandy Ballard
 * @version 1.0
 */
public class PropertiesPlus extends Properties implements Serializable
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1167328205528987593L;

	/**
	 * The file from which the properties were loaded, if any.
	 */
	private File propertyFile = null;

	static final public Calendar calendar = new GregorianCalendar(
			new SimpleTimeZone(0, "GMT"));

	static final public DateFormat dateFormatGMT =
		new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

	static final public DateFormat dateFormatGMT_MS =
		new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");

	/**
	 * A multi-map from a property key to the set of values that are associated
	 * with that key.  Includes only properties that are actually requested.
	 * Properties that are set but never requested are not included.  Default
	 * values are included, i.e., if a property is not initially set but is 
	 * later requested and a default value is returned, then the property 
	 * and default value are included in requestedProperties.
	 */
	protected LinkedHashMap<String, LinkedHashSet<String>> requestedProperties =
		new LinkedHashMap<String, LinkedHashSet<String>>();

	/**
	 * Empty set of properties
	 */
	public PropertiesPlus()
	{
		super();
		dateFormatGMT.setTimeZone(TimeZone.getTimeZone("GMT"));
		dateFormatGMT_MS.setTimeZone(TimeZone.getTimeZone("GMT"));
	}

	/**
	 * Initialize with specified properties
	 * @param properties Properties
	 */
	public PropertiesPlus(Properties properties)
	{
		super(properties);
		dateFormatGMT.setTimeZone(TimeZone.getTimeZone("GMT"));
		dateFormatGMT_MS.setTimeZone(TimeZone.getTimeZone("GMT"));
	}

	/**
	 * Parse properties from specified String
	 * @param properties Properties
	 */
	public PropertiesPlus(String properties)
	{
		super();
		dateFormatGMT.setTimeZone(TimeZone.getTimeZone("GMT"));
		dateFormatGMT_MS.setTimeZone(TimeZone.getTimeZone("GMT"));
		if (properties.startsWith("<properties>"))
			parseXML(properties);
		else
			parseString(properties);
	}
	
	public PropertiesPlus(InputStream inputStream) 
	{
		super();
		dateFormatGMT.setTimeZone(TimeZone.getTimeZone("GMT"));
		dateFormatGMT_MS.setTimeZone(TimeZone.getTimeZone("GMT"));
		if (inputStream != null)
		{
			Scanner input = new Scanner(inputStream);
			while (input.hasNext())
			{
				// split the input string on form feed character.
				String line = input.nextLine().trim();
				if (line.length() > 0 && !line.startsWith("#"))
				{
					int i = line.indexOf('=');
					if (i > 0)
						setProperty(line.substring(0, i).trim(), line.substring(i+1).trim());					
				}
			}
			input.close();
		}
	}

	/**
	 * Load properties from specified file.
	 * @param propertiesFile
	 * @throws IOException 
	 */
	public PropertiesPlus(File propertiesFile) throws IOException
	{
		super();
		propertyFile = propertiesFile; 
		dateFormatGMT.setTimeZone(TimeZone.getTimeZone("GMT"));
		dateFormatGMT_MS.setTimeZone(TimeZone.getTimeZone("GMT"));
		load(new FileReader(propertiesFile));
	}

	/**
	 * Override Properties.load(Reader) in order to be able to 
	 * load file and path names with single backslashes instead of 
	 * double backslashes.
	 * <p>Super class Properties.load(Reader) considers a single backslash
	 * to be an escape character.  This meant that in order to 
	 * read a windows path name from a properties file you had to 
	 * double up the backslashes, eg., to specify c:\dir\filename
	 * you add to actually put c:\\dir\\filename.  This method
	 * overrides Property.load(Reader) and correctly interprets
	 * single backslashes.
	 * <p>To provide backward compatibility with old GMP property
	 * files, two special cases are considered.  If a value 
	 * starts with 4 backslashes, or starts with a single character
	 * followed by ':\\', then all double backslashes are replaced
	 * with single backslashes. 
	 */
	@Override
	public void load(Reader reader)
	{
		String os = System.getProperty("os.name", "?").toLowerCase();
		if (os.startsWith("linux"))
			os = "linux";
		else if (os.startsWith("windows"))
			os = "windows";
		else if (os.startsWith("sun"))
			os = "sun";
		else if (os.startsWith("mac"))
			os = "mac";
		
		
		String line, key, value;
		int i,j;
		Scanner input = new Scanner(reader);
		while (input.hasNext())
		{
			line = getNextLine(input);
			if (line.length() > 0)
			{
				while (line.endsWith(" \\") || line.endsWith("\t\\"))
					line = line.substring(0, line.length()-1) + getNextLine(input);
				
				// find index of the first occurrence of either '=' or ':'
				i=line.indexOf('=');
				j=line.indexOf(':');
				if (j >= 0 && j < i)
					i = j;
				
				// split line into key and value substrings.
				// Note: key must have at least one character.
				if (i > 0)
				{
					key = line.substring(0, i).trim();
					
					boolean skip = false;
					for (String op : new String[] {"linux", "windows", "sun", "mac"})
					{
						if (key.startsWith("<"+op+">"))
						{ 
							if (os.equals(op))
							{
								key = key.substring(("<"+op+">").length()).trim();
								skip = false;
								break;
							}
							else 
								skip = true;
						}
					}
					if (skip)
						continue;
					
					
					value = line.substring(i+1).trim();
				
					// The following two if statements provide backward compatibility for old GMP property files
					// that added double '\' characters in file names and directory names.  As soon as all of 
					// our property files no longer contain double backslash characters, we should get rid of this.
					// if value starts with 4 backslash characters, eg., \\\\fig2\\GMPSys\\filename
					// then replace all double backslashes with single backslashes, eg., \\fig2\GMPSys\filename
					if (value.startsWith("\\\\\\\\"))
					{
						i=value.indexOf("\\\\");
						while (i >= 0)
						{
							value = value.substring(0,i)+value.substring(i+1);
							i=value.indexOf("\\\\");
						}
						value = "\\"+value;
					}

					// if value starts with a character followed by ":\\", eg., c:\\GMPSys\\filename
					// then replace all double backslashes with single backslashes, eg., c:\GMPSys\filename
					if (value.length() >= 4 && value.charAt(1)==':' && value.charAt(2) == '\\' && value.charAt(3) == '\\')
					{
						i=value.indexOf("\\\\");
						while (i >= 0)
						{
							value = value.substring(0,i)+value.substring(i+1);
							i=value.indexOf("\\\\");
						}
					}

					setProperty(key, value);
				}
			}
		}
		
		if (getProperty("includePropertyFile") != null)
		{
			try
			{
				File includeFile = getFile("includePropertyFile");
				remove("includePropertyFile");
				load(new FileReader(includeFile));
			} 
			catch (PropertiesPlusException e)
			{
				e.printStackTrace();
				System.exit(1);
			} 
			catch (FileNotFoundException e)
			{
				e.printStackTrace();
				System.exit(1);
			}
		}
	}
	
	/**
	 * Retrieve the next trimmed line in input that a) has non-zero length, b) does not start 
	 * with '#', c) does not start with '!'. If there are no more lines, returns an empty string.
	 * @param input
	 * @return the next trimmed line in input that a) has non-zero length, b) does not start 
	 * with '#', c) does not start with '!'. If there are no more lines, returns an empty string.
	 */
	private String getNextLine(Scanner input)
	{
		String line = input.nextLine().trim();
		while ((line.length() == 0 || line.startsWith("#") || line.startsWith("!")) && input.hasNext())
			line = input.nextLine().trim();
		if (line.startsWith("#") || line.startsWith("!"))
			line = "";
		return line;
	}
	
	/**
	 * Calls super.getProperty with property.trim()
	 * and trims the result before returning.
	 * 
	 * <p>If the result contains substring <property:xxx>
	 * then <property:xxx> is replaced with 
	 * the result of getProperty(xxx).
	 * For example, if the property file contains:
	 * <br>rootDir = c:\\directory\\
	 * <br>path = <property:rootDir>filename
	 * <br>then calling getProperty("path") will
	 * return 'c:\\directory\\filename'
	 * 
	 * <p>If the result contains substring <env:xxx>
	 * then <env:xxx> is replaced with 
	 * the result of System.getenv(xxx).
	 * For example, if the environment variable 'username' is defined then
	 * <br>UserName = <env:username>
	 * <br>will return the value of environment variable 'username'
	 */
	@Override
	public String getProperty(String property)
	{
		String value = super.getProperty(property.trim());
		if (value == null)
			return null;
		
		addRequestedProperty(property, value);
				
		int i1 = value.indexOf("<property:");
		while (i1 >= 0)
		{
			int i2 = value.substring(i1).indexOf(">")+i1;
			if (i2 < 0)
				i2 = value.length();
			
			if (i2 > i1+10)
			{
				String p = value.substring(i1+10, i2);
				if (super.containsKey(p))
				{
					String v = super.getProperty(p);
					value = value.substring(0,i1)+v+value.substring(i2+1);
				} 
				else
					return "ERROR: Property "+p+" is not specified in the properties file";
			}
			i1 = value.indexOf("<property:");
		}

		i1 = value.indexOf("<env:");
		while (i1 >= 0)
		{
			int i2 = value.indexOf(">");
			if (i2 > i1+5)
			{
				String p = value.substring(i1+5, i2);
				String v = System.getenv(p);
				if (v != null)
					value = value.substring(0,i1)+v+value.substring(i2+1);
				else
					return "ERROR: Property "+p+" is not specified in the user's environment";
			}
			i1 = value.indexOf("<env:");
		}

		return value.trim();
	}

	/**
	 * Calls super.getProperty with property.trim()
	 * and trims the result before returning.
	 */
	@Override
	public String getProperty(String property, String defaultValue)
	{
		String value = super.getProperty(property.trim(), defaultValue);
		
		if (value == null) return null;
		
		addRequestedProperty(property, value);
		
		int i1 = value.indexOf("<property:");
		if (i1 >= 0)
		{
			int i2 = value.indexOf(">");
			if (i2 > i1+10)
			{
				String p = value.substring(i1+10, i2);
				if (super.containsKey(p))
				{
					String v = super.getProperty(p);
					value = value.substring(0,i1)+v+value.substring(i2+1);
				}
			}
		}

		i1 = value.indexOf("<env:");
		while (i1 >= 0)
		{
			int i2 = value.indexOf(">");
			if (i2 > i1+5)
			{
				String p = value.substring(i1+5, i2);
				String v = System.getenv(p);
				if (v != null)
					value = value.substring(0,i1)+v+value.substring(i2+1);
				else
					return "ERROR: Property "+p+" is not specified in the properties file";
			}
			i1 = value.indexOf("<env:");
		}

		return value.trim();
	}

	/**
	 * Retrieve the value of specified property as a double
	 * @param property String
	 * @return double
	 * @throws PropertiesPlusException if property is not specified or if
	 * the value cannot be converted to a double
	 */
	public double getDouble(String property)
	throws PropertiesPlusException
	{
		String value = getProperty(property);
		if (value == null)
			throw new PropertiesPlusException(property+" is not defined.");

		try
		{
			return Double.valueOf(value);
		}
		catch (NumberFormatException ex)
		{
			throw new PropertiesPlusException(String.format(
					"%s = %s cannot be converted to type double", property, value));
		}
	}

	/**
	 * Retrieve the value of specified property as a double.
	 * @param property String
	 * @param defaultValue double returns this value if property is not defined.
	 * @return double
	 * @throws PropertiesPlusException if value cannot be converted to a double
	 */
	public double getDouble(String property, double defaultValue)
	throws PropertiesPlusException
	{
		String value = getProperty(property);
		
		if (value == null)
		{
			addRequestedProperty(property, Double.toString(defaultValue));
			return defaultValue;
		}
		
		try
		{
			return Double.valueOf(value);
		}
		catch (NumberFormatException ex)
		{
			throw new PropertiesPlusException(String.format(
					"%s = %s cannot be converted to type double", property, value));
		}
	}

	/**
	 * Retrieve the value of specified property as a double.
	 * @param property String
	 * @param defaultValue double returns this value if property is not defined.
	 * @return double
	 * @throws PropertiesPlusException if value cannot be converted to a double
	 */
	public float getFloat(String property, float defaultValue)
	throws PropertiesPlusException
	{
		String value = getProperty(property);
		
		if (value == null)
		{
			addRequestedProperty(property, Float.toString(defaultValue));
			return defaultValue;
		}
		
		try
		{
			return Float.valueOf(value);
		}
		catch (NumberFormatException ex)
		{
			throw new PropertiesPlusException(String.format(
					"%s = %s cannot be converted to type float", property, value));
		}
	}

	/**
	 * Retrieve the value of specified property as a double
	 * @param property String
	 * @return double
	 * @throws PropertiesPlusException if property is not specified or if
	 * the value cannot be converted to a double
	 */
	public float getFloat(String property)
	throws PropertiesPlusException
	{
		String value = getProperty(property);
		if (value == null)
			throw new PropertiesPlusException(property+" is not defined.");

		try
		{
			return Float.valueOf(value);
		}
		catch (NumberFormatException ex)
		{
			throw new PropertiesPlusException(String.format(
					"%s = %s cannot be converted to type double", property, value));
		}
	}

	public void setProperty(String property, double value)
	{
		setProperty(property, String.format("%f", value));
	}

	public void setProperty(String property, double value, String format)
	{
		setProperty(property, String.format(format, value));
	}

	/**
	 * Retrieve the value of specified property as an int.
	 * @param property String
	 * @return int
	 * @throws PropertiesPlusException if property is not specified or if
	 * the value cannot be converted to an int.
	 */
	public int getInt(String property)
	throws PropertiesPlusException
	{
		String value = getProperty(property);
		if (value == null)
			throw new PropertiesPlusException(property+" is not defined.");
		try
		{
			return Integer.valueOf(value);
		}
		catch (NumberFormatException ex)
		{
			throw new PropertiesPlusException(String.format(
					"%s = %s cannot be converted to type int", property, value));
		}
	}

	/**
	 * Retrieve the value of specified property as an int.
	 * @param property String
	 * @param defaultValue int returns this value if property is not defined.
	 * @return int
	 * @throws PropertiesPlusException if value cannot be converted to an int.
	 */
	public int getInt(String property, int defaultValue)
	throws PropertiesPlusException
	{
		String value = getProperty(property);
		
		if (value == null)
		{
			addRequestedProperty(property, Integer.toString(defaultValue));
			return defaultValue;
		}
		
		try
		{
			return Integer.valueOf(value);
		}
		catch (NumberFormatException ex)
		{
			throw new PropertiesPlusException(String.format(
					"%s = %s cannot be converted to type int", property, value));
		}
	}

	/**
	 * Retrieve the value of specified property as an int[].
	 * @param property String
	 * @return int[]
	 * @throws PropertiesPlusException if property is not specified or if
	 * the value cannot be converted to an int.
	 */
	public int[] getIntArray(String property)
	throws PropertiesPlusException
	{
		String value = getProperty(property);
		if (value == null)
			return null;
		
		try
		{
			Scanner in = new Scanner(value.trim().replaceAll(",", " "));
			ArrayListInt ints = new ArrayListInt();
			while (in.hasNext())
				ints.add(in.nextInt());
			in.close();

			return ints.toArray();
		}
		catch (NumberFormatException ex)
		{
			throw new PropertiesPlusException(String.format(
					"%s = %s cannot be converted to type int[]", property, value));
		}
	}

	/**
	 * Retrieve the value of specified property as an int.
	 * @param property String
	 * @param defaultValue int returns this value if property is not defined.
	 * @return int
	 * @throws PropertiesPlusException if value cannot be converted to an int.
	 */
	public int[] getIntArray(String property, int[] defaultValue)
	throws PropertiesPlusException
	{
		String value = getProperty(property);
		if (value == null)
		{
			if (defaultValue == null)
			{
				addRequestedProperty(property, "null");
				return null;
			}
			addRequestedProperty(property, Arrays.toString(defaultValue));
			return defaultValue;
		}
		
		try
		{
			Scanner in = new Scanner(value.trim().replaceAll(",", " "));
			ArrayListInt ints = new ArrayListInt();
			while (in.hasNext())
				ints.add(in.nextInt());
			in.close();

			return ints.toArray();
		}
		catch (NumberFormatException ex)
		{
			throw new PropertiesPlusException(String.format(
					"%s = %s cannot be converted to type int[]", property, value));
		}
	}

	/**
	 * Retrieve the value of specified property as an double[].
	 * @param property String
	 * @return double[]
	 * @throws PropertiesPlusException if property is not specified or if
	 * the value cannot be converted to an double[].
	 */
	public double[] getDoubleArray(String property)
	throws PropertiesPlusException
	{
		String value = getProperty(property);
		if (value == null)
			return null;
		try
		{
			Scanner in = new Scanner(value.trim().replaceAll(",", " "));
			ArrayListDouble dbl = new ArrayListDouble();
			while (in.hasNext())
				dbl.add(in.nextDouble());
			in.close();

			return dbl.toArray();
		}
		catch (NumberFormatException ex)
		{
			throw new PropertiesPlusException(String.format(
					"%s = %s cannot be converted to type double[]", property, value));
		}
	}

	/**
	 * Retrieve the value of specified property as an double[].
	 * @param property String
	 * @param defaultValue double[] returns this value if property is not defined.
	 * @return double[]
	 * @throws PropertiesPlusException if value cannot be converted to an double[].
	 */
	public double[] getDoubleArray(String property, double[] defaultValue)
	throws PropertiesPlusException
	{
		String value = getProperty(property);
		if (value == null)
		{
			if (defaultValue == null)
			{
				addRequestedProperty(property, "null");
				return null;
			}
			addRequestedProperty(property, Arrays.toString(defaultValue));
			return defaultValue;
		}
		try
		{
			Scanner in = new Scanner(value.trim().replaceAll(",", " "));
			ArrayListDouble dbl = new ArrayListDouble();
			while (in.hasNext())
				dbl.add(in.nextDouble());
			in.close();

			return dbl.toArray();
		}
		catch (NumberFormatException ex)
		{
			throw new PropertiesPlusException(String.format(
					"%s = %s cannot be converted to type double[]", property, value));
		}
	}

	/**
	 * Retrieve the value of specified property as an double[].
	 * @param property String
	 * @return double[]
	 * @throws PropertiesPlusException if property is not specified or if
	 * the value cannot be converted to an double[].
	 */
	public float[] getFloatArray(String property)
	throws PropertiesPlusException
	{
		String value = getProperty(property);
		if (value == null)
			return null;
		try
		{
			Scanner in = new Scanner(value.trim().replaceAll(",", " "));
			ArrayListFloat flt = new ArrayListFloat();
			while (in.hasNext())
				flt.add(in.nextFloat());
			in.close();

			return flt.toArray();
		}
		catch (NumberFormatException ex)
		{
			throw new PropertiesPlusException(String.format(
					"%s = %s cannot be converted to type double[]", property, value));
		}
	}

	/**
	 * Retrieve the value of specified property as an double[].
	 * @param property String
	 * @param defaultValue double[] returns this value if property is not defined.
	 * @return double[]
	 * @throws PropertiesPlusException if value cannot be converted to an double[].
	 */
	public float[] getFloatArray(String property, float[] defaultValue)
	throws PropertiesPlusException
	{
		String value = getProperty(property);
		if (value == null)
		{
			if (defaultValue == null)
			{
				addRequestedProperty(property, "null");
				return null;
			}
			addRequestedProperty(property, Arrays.toString(defaultValue));
			return defaultValue;
		}
		try
		{
			Scanner in = new Scanner(value.trim().replaceAll(",", " "));
			ArrayListFloat flt = new ArrayListFloat();
			while (in.hasNext())
				flt.add(in.nextFloat());
			in.close();

			return flt.toArray();
		}
		catch (NumberFormatException ex)
		{
			throw new PropertiesPlusException(String.format(
					"%s = %s cannot be converted to type float[]", property, value));
		}
	}

	/**
	 *
	 * @param property String
	 * @param value int
	 */
	public void setProperty(String property, int value)
	{
		setProperty(property, String.format("%d", value));
	}

	/**
	 *
	 * @param property String
	 * @param values int[]
	 */
	public void setProperty(String property, int[] values)
	{
		if (values.length == 0)
			setProperty(property, "");
		StringBuffer buf = new StringBuffer();
		buf.append(values[0]);
		for (int i=1; i<values.length; ++i)
			buf.append(',').append(values[i]);
		setProperty(property, buf.toString());
	}

	/**
	 * Retrieve the value of specified property as an boolean.
	 * @param property String
	 * @return boolean
	 * @throws PropertiesPlusException if property is not specified or if
	 * the value cannot be converted to an boolean.
	 */
	public boolean getBoolean(String property)
	throws PropertiesPlusException
	{
		String value = getProperty(property);
		if (value == null)
			throw new PropertiesPlusException(property+" is not defined.");
		
		value = value.trim();
		if (value.equalsIgnoreCase("true"))
			return true;
		
		if (value.equalsIgnoreCase("false"))
			return false;
		
		throw new PropertiesPlusException(String.format(
					"%s = %s cannot be converted to type boolean", property, value));
	}

	/**
	 * Retrieve the value of specified property as an boolean.
	 * @param property String
	 * @param defaultValue int returns this value if property is not defined.
	 * @return boolean
	 * @throws PropertiesPlusException if value cannot be converted to an boolean.
	 */
	public boolean getBoolean(String property, boolean defaultValue)
	throws PropertiesPlusException
	{
		String value = getProperty(property);
		if (value == null)
		{
			addRequestedProperty(property, defaultValue ? "true" : "false");
			return defaultValue;
		}

		value = value.trim();
		if (value.equalsIgnoreCase("true"))
			return true;
		
		if (value.equalsIgnoreCase("false"))
			return false;
		
		throw new PropertiesPlusException(String.format(
					"%s = %s cannot be converted to type boolean", property, value));
	}

	public void setProperty(String property, boolean value)
	{
		setProperty(property, (value ? "true" : "false"));
	}
	
	/**
	 * keyValuePair is split into two parts based on index of first '=' sign.
	 * @param keyValuePair
	 * @throws PropertiesPlusException
	 */
	public void setProperty(String keyValuePair) throws PropertiesPlusException
	{
		keyValuePair = keyValuePair.trim();
		int i = keyValuePair.indexOf('=');
		if (i < 1)
			throw new PropertiesPlusException(String.format(
					"%nError parsing %s%nExpecting a String containing one '=' sign%n",
					keyValuePair));
		
		String key = keyValuePair.substring(0, i).trim();
		String value = i == keyValuePair.length()-1 ? "" : keyValuePair.substring(i+1).trim();
		setProperty(key, value);
	}

	/**
	 * Retrieve the value of the specified property as time in seconds
	 * since January 1, 1970.
	 * If the string can be converted to an integer, it is interpreted
	 * to be a julian date in GMT time zone and returned.
	 * Otherwise, if the string can be converted to a double, it is
	 * interpreted as an epoch time (seconds since 1970) and returned.
	 * Otherwise, all '/' are replaced with '-' and an attempt is made
	 * to parse the string using any of:
	 * <br>yyyy-mm-dd hh:mm:ss.SSS Z
	 * <br>yyyy-mm-dd hh:mm:ss.SSS
	 * <br>yyyy-mm-dd hh:mm:ss Z
	 * <br>yyyy-mm-dd hh:mm:ss
	 * <br>If Z is not specified, then the GMT timezone is assumed.
	 *
	 * @param property String
	 * @return double
	 * @throws PropertiesPlusException
	 */
	public double getTime(String property)
	throws PropertiesPlusException
	{
		String value = getProperty(property);
		if (value == null)
			throw new PropertiesPlusException(property+" is not defined.");
		
		try
		{
			return GMTFormat.getEpochTime(getProperty(property));
		} catch (GMPException e)
		{
			throw new PropertiesPlusException(e);
		}
	}

	/**
	 * Retrieve the value of the specified property as time in milliseconds
	 * since January 1, 1970 GMT.
	 * If the value can be converted to a integer, then it is assumed
	 * to represent a jdate object (YYYYDDD).
	 * Otherwise, if the value can be converted to a double, then
	 * the value is assumed to represent epochtime (seconds since jan 1, 1970 GMT).
	 * Otherwise, if the property's value can be parsed as a Date object using
	 * formats yyyy/mm/dd hh:mm:ss or yyyy-mm-dd hh:mm:ss or
	 * yyyy/mm/dd hh:mm:ss.SSS or yyyy-mm-dd hh:mm:ss.SSS then the epochtime is
	 * extracted from the Date (the Date is assumed to reflect the GMT time zone).
	 * Otherwise, a PropertiesPlusException is thrown.
	 *
	 * @param property String
	 * @param defaultValue double
	 * @return double
	 * @throws PropertiesPlusException
	 */
	public double getTime(String property, String defaultValue)
	throws PropertiesPlusException
	{
		String value = getProperty(property);
		if (value == null)
		{
			if (defaultValue == null)
				defaultValue = "NULL";
			addRequestedProperty(property, defaultValue);
			value = defaultValue;
		}

		try
		{
			return GMTFormat.getEpochTime(getProperty(property));
		} 
		catch (GMPException e)
		{
			throw new PropertiesPlusException(e);
		}
	}
	
	/**
	 * Retrieve the value of the specified property as a Date object.
	 * If the value can be converted to a integer, then it is assumed
	 * to represent a jdate object (YYYYDDD) in the GMT time zone.
	 * Otherwise, if the value can be converted to a double, then the value is
	 * assumed to represent epochtime (seconds since Jan 1, 1970 GMT).
	 * Otherwise, if the property's value can be parsed as a Date object using
	 * formats yyyy/mm/dd hh:mm:ss or yyyy-mm-dd hh:mm:ss or
	 * yyyy/mm/dd hh:mm:ss.SSS or yyyy-mm-dd hh:mm:ss.SSS then the Date is
	 * returned (GMT time zone is assumed).
	 * Otherwise, a PropertiesPlusException is thrown.
	 * <p>Returns null if property is not defined.
	 *
	 * @param property String
	 * @return Date
	 * @throws PropertiesPlusException
	 */
	public Date getDate(String property)
	throws PropertiesPlusException
	{
		String value = getProperty(property);		
		if (value == null)
			return null;

		try
		{
			return GMTFormat.getDate(value);
		} 
		catch (GMPException e)
		{
			throw new PropertiesPlusException(e);
		}
	}

	/**
	 * Retrieve the value of the specified property as a Date object.
	 * If the value can be converted to a integer, then it is assumed
	 * to represent a jdate object (YYYYDDD) in the GMT time zone.
	 * Otherwise, if the value can be converted to a double, then the value is
	 * assumed to represent epochtime (seconds since Jan 1, 1970 GMT).
	 * Otherwise, if the property's value can be parsed as a Date object using
	 * formats yyyy/mm/dd hh:mm:ss or yyyy-mm-dd hh:mm:ss or
	 * yyyy/mm/dd hh:mm:ss.SSS or yyyy-mm-dd hh:mm:ss.SSS then the Date is
	 * returned (GMT time zone is assumed).
	 * Otherwise, a PropertiesPlusException is thrown.
	 *
	 * @param property String
	 * @param defaultValue Date
	 * @return Date
	 * @throws PropertiesPlusException
	 */
	public Date getDate(String property, Date defaultValue)
	throws PropertiesPlusException
	{
		String value = getProperty(property);		
		if (value == null)
		{
			if (defaultValue == null)
				return null;
			addRequestedProperty(property, GMTFormat.GMT_MS_Z.format(defaultValue));
			return defaultValue;
		}

		try
		{
			return GMTFormat.getDate(value);
		} 
		catch (GMPException e)
		{
			throw new PropertiesPlusException(e);
		}
	}

	/**
	 * Retrieve the value of the specified property as a File object.
	 *
	 * @param property String
	 * @param defaultValue File
	 * @return File
	 */
	public File getFile(String property, File defaultValue)
	throws PropertiesPlusException
	{
		String value = getProperty(property);
		if (value == null)
		{
			if (defaultValue == null)
				return null;
			try
			{
				addRequestedProperty(property, defaultValue.getCanonicalPath());
			}
			catch (IOException e)
			{
				throw new PropertiesPlusException(e);
			}
			return defaultValue;
		}
		if (value.startsWith("ERROR"))
			throw new PropertiesPlusException(value);
		return new File(convertWinFilePathToLinux(value));
	}

	/**
	 * Retrieve the value of the specified property as a File object.
	 * On all platforms except Windows, Windows path names are converted
	 * to Unix style path names.  Most importantly, paths like
	 * "\\bikinifire.floobee.com\index\devl\gmp\Runs\Test\MyOutputFile.txt"
     * becomes "/index/devl/gmp/Runs/Test/MyOutputFile.txt"
     * 
	 * @param property String
	 * @return File
	 * @throws PropertiesPlusException
	 */
	public File getFile(String property) throws PropertiesPlusException
	{
		String value = getProperty(property);
		if (value == null)
			return null;
		if (value.startsWith("ERROR"))
			throw new PropertiesPlusException(value);
		return new File(convertWinFilePathToLinux(value));
	}

	/**
	 * Retrieve a String containing all the key/value pairs sorted by key.  
	 * Format is key = value\n
	 * @return String
	 */
	@Override
	public String toString()
	{
		TreeSet<String> set = new TreeSet<String>();
		for (Entry<Object, Object> entry : this.entrySet())
			set.add(String.format("%s = %s%n",
					entry.getKey(), entry.getValue()));

		StringBuffer buf = new StringBuffer();
		for (String s : set)
			buf.append(s);
		return buf.toString();
	}

	/**
	 * Retrieve a String containing all the key/value pairs.  Format is
	 * key=value%f
	 * @return String
	 */
	public String toOutputString()
	{
		StringBuffer buf = new StringBuffer();
		for (Entry<Object, Object> entry : this.entrySet())
			buf.append(String.format("%s=%s%c",
					entry.getKey(), entry.getValue(), 12));
		return buf.toString();
	}

	public void parseString(String properties)
	{
		// split the input string on form feed character.
		String[] entries = properties.split((""+'\f'));
		int i;
		for (String entry : entries)
		{
			i = entry.indexOf('=');
			if (i > 0)
				setProperty(entry.substring(0, i).trim(), entry.substring(i+1));
		}
	}
	
	/**
	 * Add a property key-value pair to the multi-map of properties
	 * that have been requested by the application that owns this
	 * PropertiesPlus object.
	 * @param key
	 * @param value
	 */
	protected void addRequestedProperty(String key, String value)
	{
		LinkedHashSet<String> values = requestedProperties.get(key);
		if (values == null)
		{
			values = new LinkedHashSet<String>();
			requestedProperties.put(key.trim(), values);
		}
		values.add(value == null ? "null" : value.trim());
	}
	
	/**
	 * Retrieve a reference to the map of requested properties.  These include
	 * only properties that have actually been requested.  It includes requests
	 * that returned default values.  Properties in the initial property file
	 * that were never requested are not included.
	 * @return LinkedHashMap<String, LinkedHashSet<String>>
	 */
	public LinkedHashMap<String, LinkedHashSet<String>> getRequestedProperties()
	{
		return requestedProperties;
	}
	
	/**
	 * Retrieve a String representation of all the requested properties.
	 * @param sortAlphabetically if true, requested properties are sorted
	 * alphabetically, otherwise requested properties are reported in the 
	 * order in which they were requested.
	 * @return requested properties
	 */
	public String getRequestedPropertiesString(boolean sortAlphabetically)
	{
		Map<String, LinkedHashSet<String>> map = requestedProperties;
		if (sortAlphabetically)
			map = new TreeMap<String, LinkedHashSet<String>>(requestedProperties);

		StringBuffer buf = new StringBuffer();
		for (Map.Entry<String, LinkedHashSet<String>> entry : map.entrySet())
			for (String value : entry.getValue())
				buf.append(String.format("%s = %s%n", entry.getKey(), value));
		
		return buf.toString();
	}
	
	/**
	 * Returns a list of properties that were specified in the properties file
	 * but whose values were never requested since this PropertiesPlus object
	 * was populated.
	 * @return a list of properties that were specified in the properties file
	 * but whose values were never requested.
	 */
	public ArrayList<String> getUnRequestedProperties()
	{
		ArrayList<String> urp = new ArrayList<String>();
		for (Object property : super.keySet())
			if (!requestedProperties.containsKey(((String)property).trim()))
				urp.add(((String)property).trim());
		Collections.sort(urp);
		return urp;
	}
	
	public void parseXML(String xml)
	{
		String key, value;
		Scanner input = new Scanner(xml);
		while (input.hasNext())
		{
			input.nextLine();
			key = input.findInLine("<entry key=\".*?\">");
			value = input.findInLine(".*?</entry>");
			if (key != null && value != null)
			{
				key = key.substring(12, key.length()-2).trim();
				value = value.substring(0, value.length()-8).trim();
				setProperty(key, value);
			}
		}
		input.close();
	}

	public String toXML()
	{
		StringBuffer buf = new StringBuffer("<properties>\n");
		for (Object key : this.keySet())
			buf.append(String.format("<entry key=\"%s\">%s</entry>%n",
					key, getProperty((String)key)));
		buf.append("</properties>\n");
		return buf.toString();
	}
	
	
	/**
	 * Retrieve the value of the specified property as a String.
	 * It is assumed to be a file path that will be converted to its
	 * OS-specific format.
     * 
	 * @param property String
	 * @return File
	 * @throws PropertiesPlusException
	 */
	public String getPropertyPath(String property, String defaultValue)
	{
		String value = this.getProperty(property,defaultValue);
		if (value == null)
			return null;
		return convertWinFilePathToLinux(value);
	}


	/**
	 * Convert file path from Windows to Linux/Unix only if native OS is
	 * non-Windows. Example: \\ server\actualFilePath
	 * If the native OS is not Windows, this function will prepend
	 * "nfs", keep only the lowest level domain part of the server name ("server"
	 * in "server.floobee.com"), keep "actualFilePath" and convert all "\" into "/".
	 * In the above example, the output would be: /nfs/server/actualFilePath.
	 * NOTE: IP addresses are NOT supported!
	 * NOTE: There is a special case for thummper; the "GNEM" subpath is
	 * stripped out:
	 * Example input: \\indexpool\devl\gmp
	 * Example output: /nfs/thummper/indexpool/devl/gmp
	 * @param inputFilePath - Windows file path to convert, if necessary
	 */
	public static String convertWinFilePathToLinux(String inputFilePath) {

		if (inputFilePath == null) return null;
		if (inputFilePath.isEmpty()) return "";

		// Only convert file path if OS is NOT Windows
		if(!System.getProperty("os.name").toLowerCase().contains("win")) 
		{
			// Only convert file path if OS is NOT Windows
			inputFilePath = inputFilePath.trim();

			// Only convert file path if it starts with \\
			if (!inputFilePath.startsWith("\\\\")) return inputFilePath;

			// Remove leading \\ from inputFilePath
			inputFilePath = inputFilePath.substring(2);

			// Extract server name from inputFilePath
			String server_name = inputFilePath.substring(0,inputFilePath.indexOf("\\"));
			inputFilePath = inputFilePath.substring(inputFilePath.indexOf("\\")+1,inputFilePath.length());
			int index = server_name.indexOf(".");
			if (index > 0) {
				server_name = server_name.substring(0,index);
			}
			server_name = server_name.toLowerCase(); // all lower case in server name

			// Replace all \ with /
			inputFilePath = inputFilePath.replace('\\', '/');

			// SPECIAL CASE: Remove "gnem" from thummper paths
			if (server_name.equals("thummper") || server_name.equals("tonto2")) {
				String gnem_string = inputFilePath.substring(0,inputFilePath.indexOf("/"));
				if (gnem_string.equalsIgnoreCase("gnem")) {
					inputFilePath = inputFilePath.substring(inputFilePath.indexOf("/")+1,inputFilePath.length());
				}
			}

			// Prepend /nfs/ -- where all of our network shares are mounted
			inputFilePath = "/nfs/" + server_name + "/" + inputFilePath;

			return inputFilePath;


			/* OLD
            // find index of third "\" by finding first instance of "\" after 
            // skipping the first 2 characters ("\\")
            int thirdSlashIndex = inputFilePath.indexOf('\\', 2);

            // only convert if the first part of the string also contains 
            // a "." character.  This is necessary as this function may
            // get called more than once for the same string, and we only
            // want to modify it if the first portion contains
            // "server.floobee.com or "123.456.789.yyy".
            if(inputFilePath.startsWith("\\\\") && thirdSlashIndex >= 2
                && inputFilePath.substring(0,thirdSlashIndex).contains(".")){
                // create substring starting from the third slash 
                // (this drops "\\server" from the string)
                inputFilePath = inputFilePath.substring(thirdSlashIndex);
            }

            // replace all "\" with "/"
            inputFilePath = inputFilePath.replace('\\', '/'); 
			 */
		}

		return inputFilePath;  
	}

	/**
	 * If this PropertiesPlusGMP object was constructed with a File argument, 
	 * they this method returns a reference to the File object.  
	 * Otherwise returns null.
	 * @return the propertyFile
	 */
	public File getPropertyFile() { return propertyFile; }
	
}
