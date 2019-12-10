package gms.shared.utilities.geotess.util.numerical.vector;

public class VectorGeo extends VectorUnit
{
	private static final EarthShape earthShape = EarthShape.WGS84;
	
	/**
	 *  Retrieve current shape of the Earth that is used to convert between geocentric and
	 *  geographic latitude and between depth and radius.  The default is WGS84.
	 * <ul>
	 * <li>SPHERE - Geocentric and geographic latitudes are identical and
	 * conversion between depth and radius assume the Earth is a sphere
	 * with constant radius of 6371 km.
	 * <li>GRS80 - Conversion between geographic and geocentric latitudes, and between depth
	 * and radius are performed using the parameters of the GRS80 ellipsoid.
	 * <li>GRS80_RCONST - Conversion between geographic and geocentric latitudes are performed using
	 * the parameters of the GRS80 ellipsoid.  Conversions between depth and radius
	 * assume the Earth is a sphere with radius 6371.
	 * <li>WGS84 - Conversion between geographic and geocentric latitudes, and between depth
	 * and radius are performed using the parameters of the WGS84 ellipsoid.
	 * <li>WGS84_RCONST - Conversion between geographic and geocentric latitudes are performed using
	 * the parameters of the WGS84 ellipsoid.  Conversions between depth and radius
	 * assume the Earth is a sphere with radius 6371.
	 * <li>IERS2003 - Conversion between geographic and geocentric latitudes, and between depth
	 * and radius are performed using the parameters of the IERS2003 ellipsoid.
	 * <li>IERS2003_RCONST - Conversion between geographic and geocentric latitudes are performed using
	 * the parameters of the IERS2003 ellipsoid.  Conversions between depth and radius
	 * assume the Earth is a sphere with radius 6371.
	 * </ul>
	 * @return one of SPHERE, GRS80, GRS80_RCONST, WGS84, WGS84_RCONST, IERS2003, IERS2003_RCONST
	 */
	static public EarthShape getEarthShape() { return earthShape; }

	/**
	 *  Define the shape of the Earth that is to be used to convert between geocentric and
	 *  geographic latitude and between depth and radius.  The default is WGS84.
	 * <ul>
	 * <li>SPHERE - Geocentric and geographic latitudes are identical and
	 * conversion between depth and radius assume the Earth is a sphere
	 * with constant radius of 6371 km.
	 * <li>GRS80 - Conversion between geographic and geocentric latitudes, and between depth
	 * and radius are performed using the parameters of the GRS80 ellipsoid.
	 * <li>GRS80_RCONST - Conversion between geographic and geocentric latitudes are performed using
	 * the parameters of the GRS80 ellipsoid.  Conversions between depth and radius
	 * assume the Earth is a sphere with radius 6371.
	 * <li>WGS84 - Conversion between geographic and geocentric latitudes, and between depth
	 * and radius are performed using the parameters of the WGS84 ellipsoid.
	 * <li>WGS84_RCONST - Conversion between geographic and geocentric latitudes are performed using
	 * the parameters of the WGS84 ellipsoid.  Conversions between depth and radius
	 * assume the Earth is a sphere with radius 6371.
	 * <li>IERS2003 - Conversion between geographic and geocentric latitudes, and between depth
	 * and radius are performed using the parameters of the IERS2003 ellipsoid.
	 * <li>IERS2003_RCONST - Conversion between geographic and geocentric latitudes are performed using
	 * the parameters of the IERS2003 ellipsoid.  Conversions between depth and radius
	 * assume the Earth is a sphere with radius 6371.
	 * </ul>
	 * @param earthShape one of SPHERE, GRS80, GRS80_RCONST, WGS84, WGS84_RCONST, IERS2003, IERS2003_RCONST
	 * @return 
	 */
	//static public void setEarthShape(EarthShape earthShape) { VectorGeo.earthShape = earthShape; }
	
	public static void setApproximateLatitudes(boolean approximateLatitudes) 
	{ earthShape.approximateLatitudes = approximateLatitudes; }
	
	/**
	 * Retrieve the radius of the Earth in km at the position specified by an
	 * Earth-centered unit vector.
	 * 
	 * @param vector
	 *            Earth-centered unit vector
	 * @return radius of the Earth in km at specified position.
	 */
	public static double getEarthRadius(double[] vector)
	{ return earthShape.getEarthRadius(vector); }

	/**
	 * Convert a 3-component unit vector to geographic latitude, in radians.
	 * 
	 * @param vector
	 *            3-component unit vector
	 * @return geographic latitude in radians.
	 */
	public static double getLat(double[] vector)
	{ return earthShape.getLat(vector); }

	/**
	 * Convert a 3-component unit vector to a longitude, in radians.
	 * 
	 * @param vector
	 *            3 component unit vector
	 * @return longitude in radians.
	 */
	public static double getLon(double[] vector)
	{ return earthShape.getLon(vector); }

	/**
	 * Convert a 3-component unit vector to geographic latitude, in degrees.
	 * 
	 * @param vector
	 *            3-component unit vector
	 * @return geographic latitude in degrees.
	 */
	public static double getLatDegrees(double[] vector)
	{ return earthShape.getLatDegrees(vector); }

	/**
	 * Convert a 3-component unit vector to a longitude, in degrees.
	 * 
	 * @param vector
	 *            3 component unit vector
	 * @return longitude in degrees.
	 */
	public static double getLonDegrees(double[] vector)
	{ return earthShape.getLonDegrees(vector); }

	/**
	 * Convert a unit vector to a String representation of lat, lon formated
	 * with "%9.5f %10.5f"
	 * 
	 * @param vector
	 * @return a String of lat,lon in degrees formatted with "%9.5f %10.5f"
	 */
	public static String getLatLonString(double[] vector)
	{
		return String.format("%9.5f %10.5f", earthShape.getLatDegrees(vector),
				earthShape.getLonDegrees(vector));
	}

	public static String getLatLonString(double[] vector, int precision) 
	{
		return getLatLonString(vector, String.format("%%%d.%df %%%d.%df", 
				(precision+4), precision, (precision+5), precision));
	}

	public static String getLatLonString(double[] vector, String format) 
	{
		return String.format(format, earthShape.getLatDegrees(vector),
				earthShape.getLonDegrees(vector));
	}

	/**
	 * Convert geographic lat, lon into a geocentric unit vector. The
	 * x-component points toward lat,lon = 0, 0. The y-component points toward
	 * lat,lon = 0, 90. The z-component points toward north pole.
	 * 
	 * @param lat
	 *            geographic latitude in degrees.
	 * @param lon
	 *            longitude in degrees.
	 * @return 3 component unit vector.
	 */
	public static double[] getVectorDegrees(double lat, double lon)
	{ return earthShape.getVectorDegrees(lat,  lon); }

	/**
	 * Convert geographic lat, lon into a geocentric unit vector. The
	 * x-component points toward lat,lon = 0, 0. The y-component points toward
	 * lat,lon = 0, 90. The z-component points toward north pole.
	 * 
	 * @param lat
	 *            geographic latitude in degrees.
	 * @param lon
	 *            longitude in degrees.
	 * @param vector
	 *            3 component unit vector.
	 */
	public static void getVectorDegrees(double lat, double lon, double[] vector)
	{ earthShape.getVectorDegrees(lat, lon, vector); }

	/**
	 * Convert geographic lat, lon into a geocentric unit vector. The
	 * x-component points toward lat,lon = 0, 0. The y-component points toward
	 * lat,lon = 0, PI/2. The z-component points toward north pole.
	 * 
	 * @param lat
	 *            geographic latitude in radians.
	 * @param lon
	 *            longitude in radians.
	 * @return 3 component unit vector.
	 */
	public static double[] getVector(double lat, double lon)
	{ return earthShape.getVector(lat, lon); }

	/**
	 * Convert geographic lat, lon into a geocentric unit vector. The
	 * x-component points toward lat,lon = 0, 0. The y-component points toward
	 * lat,lon = 0, PI/2 The z-component points toward north pole.
	 * 
	 * @param lat
	 *            geographic latitude in radians.
	 * @param lon
	 *            longitude in radians.
	 * @param vector
	 *            3 component unit vector.
	 */
	public static void getVector(double lat, double lon, double[] vector)
	{ earthShape.getVector(lat, lon, vector); }
	
	/**
	 * Return geocentric latitude given a geographic latitude
	 * 
	 * @param lat
	 *            geographic latitude in radians
	 * @return geocentric latitude in radians
	 */
	public static double getGeoCentricLatitude(double lat)
	{ return earthShape.getGeocentricLat(lat); }

	/**
	 * Return geographic latitude given a geocentric latitude
	 * 
	 * @param lat
	 *            geocentric latitude in radians
	 * @return geographic latitude in radians
	 */
	public static double getGeoGraphicLatitude(double lat)
	{ return earthShape.getGeographicLat(lat); }

	/**
	 * Compute points that define an ellipse centered at a specified point.
	 * @param latCenter latitude of center of ellipse
	 * @param lonCenter longiitude of center of ellipse
	 * @param majax the length of the major axis of the ellipse, in km.
	 * @param minax the length of the minor axis of the ellipse, in km.
	 * @param trend the orientation relative to north of the major axis of the 
	 * ellipse.
	 * @param npoints the number of points to define the ellipse
	 * @param inDegrees if true, centerLat, centerLon, trend and all return 
	 * values have units of degrees, otherwise, the units are radians.
	 * @return an array with dimensions npoints x 2 containing the latitude and 
	 * longitude of points that define the ellipse.
	 */
	public static double[][] getEllipse(double latCenter, double lonCenter, double majax, double minax, double trend, 
			int npoints, boolean inDegrees)
	{ return earthShape.getEllipse(latCenter, lonCenter, majax, minax, trend, npoints, inDegrees); }

}
