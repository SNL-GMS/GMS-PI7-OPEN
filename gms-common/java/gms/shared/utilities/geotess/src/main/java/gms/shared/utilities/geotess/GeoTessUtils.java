package gms.shared.utilities.geotess;


import static java.lang.Math.exp;
import static java.lang.Math.log;
import gms.shared.utilities.geotess.util.numerical.vector.EarthShape;
import gms.shared.utilities.geotess.util.numerical.vector.VectorUnit;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;

/**
 * Collection of static functions to manipulate geographic information.
 * <p>
 * There is no public constructor because all methods are static. Methods can be
 * called like "GeoTessUtils.getVector(lat, lon)"
 * 
 * @author Sandy Ballard
 */
public class GeoTessUtils extends VectorUnit
{
	/**
	 * If true, then an approximate algorithm will be used to
	 * convert back and forth between geocentric and geographic latitudes.
	 * The approximation incurs an error of about 0.1 meters in latitude 
	 * calculations but is much faster than the correct calculation.
	 * <p>Note that the existence of this static, mutable variable technically
	 * violates thread-safety of this class. But given the assumption that 
	 * the approximation is just as good as the true conversions, this is 
	 * not a significant violation. 
	 */
	public static boolean approximateLatitudes = false;
	

	protected GeoTessUtils()
	{
	}

	/**
	 * End-of-line character(s). Value depends on operating system.
	 */
	public static String NL = System.getProperty("line.separator");

	/**
	 * Retrieve the code version: First number indicates major revisions, second
	 * number indicates minor revisions that involve interface or file format
	 * changes, and the third number indicates bug fixes or performance
	 * improvements that do not involve changes in basic functionality,
	 * interface changes, or file format changes.
	 * 
	 * @return code version
	 */
	public static String getVersion() { return "2.2.3"; }

	/**
	 * @author sballar
	 * 
	 */
	public enum OSType
	{
		WINDOWS, MACOSX, SUNOS, LINUX,  UNIX, UNRECOGNIZED;
		
		public String toString()
		{
			switch (this)
			{
			case WINDOWS:
				return "Windows";
			case MACOSX:
				return "MacOSX";
			case SUNOS:
				return "SunOS";
			case LINUX:
				return "Linux";
			case UNIX:
				return "Unix";
			default:
				return "Unrecognized";
			}
		}
	};

	/**
	 * <ul>
	 * Return the operating system on which GeoTess is currently running:
	 * <li>OS.WINDOWS
	 * <li>OS.MAC
	 * <li>OS.UNIX (includes all flavors of solaris and sunos)
	 * <li>OS.LINUX
	 * <li>OS.UNRECOGNIZED
	 * </ul>
	 * <p>
	 * Here is a pretty comprehensive list of possible os.name values:
	 * http://lopica.sourceforge.net/os.html
	 * 
	 * @return current operating system
	 */
	public static OSType getOS()
	{
		String os = System.getProperty("os.name").toLowerCase();
		if (os.contains("win"))
			return OSType.WINDOWS;
		else if (os.contains("mac"))
			return OSType.MACOSX;
		else if (os.contains("linux"))
			return OSType.LINUX;
		else if (os.contains("unix"))
			return OSType.UNIX;
		else if (os.contains("sun") || os.contains("solaris"))
			return OSType.SUNOS;

		return OSType.UNRECOGNIZED;
	}

	/**
	 * Read a String from a binary file. First, read the length of the String
	 * (number of characters), then read that number of characters into the
	 * String.
	 * 
	 * @param input
	 *            DataInputStream
	 * @return String
	 * @throws IOException
	 */
	public static String readString(DataInputStream input) throws IOException
	{
		int n=input.readInt();
		if (n == 0) return "";
		byte[] bytes = new byte[n];
		input.read(bytes);
		return new String(bytes);
	}

	/**
	 * Write a String to a binary file. First write the length of the String
	 * (int number of characters) then write that many characters.
	 * 
	 * @param output
	 *            DataOutputStream
	 * @param s
	 *            String
	 * @throws IOException
	 */
	public static void writeString(DataOutputStream output, String s)
			throws IOException
	{
		output.writeInt(s.length());
		if (s.length() > 0)
			output.writeBytes(s);
	}

	/**
	 * Write an array of strings to a binary file. The Strings are first
	 * concatenated together with ';' as a separator. Then the combined String
	 * is written to the file using method 'writeString()' described above.
	 * 
	 * @param output
	 *            DataOutputStream
	 * @param strings
	 *            String[]
	 * @throws IOException
	 */
	public static void writeString(DataOutputStream output, String[] strings)
			throws IOException
	{
		String s = strings[0];
		for (int i = 1; i < strings.length; ++i)
			s = s + ";" + strings[i];
		writeString(output, s);
	}

	/**
	 * Return the approximate edge length in degrees for triangles on 
	 * the specified tessellation level.  Assumes that first
	 * level is icosahedron with triangle edgelengths of 
	 * approximately 64 degrees.
	 * @param level
	 * @return the approximate edge length in degrees for triangles on 
	 * the specified tessellation level.
	 */
	public static double getEdgeLength(int level)
	{
		return exp(log(64)-level*log(2));
	}
	
	/**
	 * Convert edgeLength in degrees to tessellation level.
	 * Returns round(log_2(64./edgeLength)
	 * @param edgeLength
	 * @return round(log_2(64./edgeLength)
	 */
	public static int getTessLevel(double edgeLength)
	{
		return (int) Math.round(Math.log(64./edgeLength)/Math.log(2));
	}
	
	/**
	 * Retrieve the stack trace of an exception as a String
	 * @param exception
	 * @return the stack trace of an exception as a String
	 */
	public static String getStackTrace(Exception exception)
	{
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		exception.printStackTrace(pw);
		return sw.toString(); 
	}
	
	/**
	 * Convert an ArrayList into an array.
	 * @param data
	 * @return an ArrayList converted into an array.
	 */
	public static int[] toArrayOfInts(ArrayList<Integer> data)
	{
		// If there is a better way to do this I don't know what it is.
		int[] a = new int[data.size()];
		for (int i=0; i<data.size(); ++i) a[i] = data.get(i).intValue();
		return a;
	}
	
	/**
	 * Convert an ArrayList into an array.
	 * @param data
	 * @return an ArrayList converted into an array.
	 */
	public static double[] toArrayOfDoubles(ArrayList<Double> data)
	{
		// If there is a better way to do this I don't know what it is.
		double[] a = new double[data.size()];
		for (int i=0; i<data.size(); ++i) a[i] = data.get(i).doubleValue();
		return a;
	}
	
	/**
	 * Convert an ArrayList into an array.
	 * @param data
	 * @return an ArrayList converted into an array.
	 */
	public static float[] toArrayOfFloats(ArrayList<Float> data)
	{
		// If there is a better way to do this I don't know what it is.
		float[] a = new float[data.size()];
		for (int i=0; i<data.size(); ++i) a[i] = data.get(i).floatValue();
		return a;
	}

	/**
	 * Retrieve the radius of the Earth at the point defined by unit vector v.
	 * 
	 * @param v unit vector of the geographic position where earth radius is requested
	 * @return earth radius in km
	 */
	public static double getEarthRadius(double[] v)
	{
		return EarthShape.WGS84.getEarthRadius(v);
	}

	/**
	 * Retrieve the radius of the Earth (km) at specified geographic latitude (radians).
	 * 
	 * @param latitude latitude in radians
	 * @return earth radius in km
	 */
	public static double getEarthRadius(double latitude)
	{
		return EarthShape.WGS84.getEarthRadius(latitude);
	}

	/**
	 * Retrieve the radius of the Earth (km) at specified geographic latitude (degrees).
	 * 
	 * @param latitude latitude in degrees
	 * @return earth radius in km
	 */
	public static double getEarthRadiusDegrees(double latitude)
	{
		return EarthShape.WGS84.getEarthRadiusDegrees(latitude);
	}

	/**
	 * Retrieve the longitude of the point defined by unit vector v, in radians
	 * 
	 * @param v double[]
	 * @return double longitude in radians.  Values range from -PI to PI.
	 */
	public static double getLon(double[] v)
	{
		return EarthShape.WGS84.getLon(v);
	}

	/**
	 * Retrieve the longitude of the point defined by unit vector v, in degrees
	 * 
	 * @param v double[]
	 * @return double longitude in radians.  Values range from -180 to 180.
	 */
	public static double getLonDegrees(double[] v)
	{
		return EarthShape.WGS84.getLonDegrees(v);
	}

	/**
	 * Retrieve the geographic latitude of the point defined by unit vector v, in radians.
	 * 
	 * @param v double[]
	 * @return  the geographic latitude of the point defined by unit vector v, in radians.
	 */
	public static double getLat(double[] v)
	{
		return EarthShape.WGS84.getLat(v);
	}

	/**
	 * Retrieve the geographic latitude of the point defined by unit vector v.
	 * 
	 * @param v double[]
	 * @return  the geographic latitude of the point defined by unit vector v, in degrees.
	 */
	public static double getLatDegrees(double[] v)
	{
		return EarthShape.WGS84.getLatDegrees(v);
	}

	/**
	 * Retrieve the geocentric latitude of the point defined by unit vector v.
	 * 
	 * @param v double[]
	 * @return double
	 */
	public static double getGeocentricLat(double[] v)
	{
		return EarthShape.WGS84.getGeocentricLat(v);
	}

	/**
	 * Retrieve the geocentric latitude of the point defined by unit vector v.
	 * 
	 * @param v double[]
	 * @return double
	 */
	public static double getGeocentricLatDegrees(double[] v)
	{
		return EarthShape.WGS84.getGeocentricLatDegrees(v);
	}

	/**
	 * Get a unit vector corresponding to a point on the Earth
	 * with the specified latitude and longitude.
	 * 
	 * @param lat the geographic latitude, in radians.
	 * @param lon the geographic longitude, in radians.
	 * @return The returned unit vector.
	 */
	public static double[] getVector(double lat, double lon)
	{
		return EarthShape.WGS84.getVector(lat, lon);
	}

	/**
	 * Get a unit vector corresponding to a point on the Earth
	 * with the specified latitude and longitude.
	 * 
	 * @param lat the geographic latitude, in degrees.
	 * @param lon the geographic longitude, in degrees.
	 * @return The returned unit vector.
	 */
	public static double[] getVectorDegrees(double lat, double lon)
	{
		return EarthShape.WGS84.getVectorDegrees(lat, lon);
	}

	/**
	 * Get a unit vector corresponding to a point on the Earth
	 * with the specified latitude and longitude.
	 * 
	 * @param lat the geographic latitude, in degrees.
	 * @param lon the geographic longitude, in degrees.
	 * @param v the 3-element array into which the unit vector will be copied.
	 */
	public static void getVectorDegrees(double lat, double lon, double[] v)
	{
		EarthShape.WGS84.getVectorDegrees(lat, lon, v);
	}

	/**
	 * Get a unit vector corresponding to a point on the Earth
	 * with the specified latitude and longitude.
	 * 
	 * @param lat the geographic latitude, in degrees.
	 * @param lon the geographic longitude, in degrees.
	 * @param v the 3-element array into which the unit vector will be copied.
	 */
	public static void getVector(double lat, double lon, double[] v)
	{
		EarthShape.WGS84.getVector(lat, lon, v);
	}

	/**
	 * Convert a unit vector to a String representation of lat, lon formated
	 * with "%9.5f %10.5f"
	 * 
	 * @param vector
	 * @return a String of lat,lon in degrees formatted with "%9.5f %10.5f"
	 */
	public static String getLatLonString(double[] vector)
	{
		return EarthShape.WGS84.getLatLonString(vector);
	}

	public static String getLatLonString(double[] vector, int precision) 
	{
		return EarthShape.WGS84.getLatLonString(vector, precision);
	}

	public static String getLatLonString(double[] vector, String format) 
	{
		return EarthShape.WGS84.getLatLonString(vector, format);
	}

	public static String getLonLatString(double[] vector)
	{
		return EarthShape.WGS84.getLonLatString(vector);
	}

}
