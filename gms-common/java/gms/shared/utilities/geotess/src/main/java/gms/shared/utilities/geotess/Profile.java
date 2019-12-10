package gms.shared.utilities.geotess;

import gms.shared.utilities.geotess.util.containers.arraylist.ArrayListDouble;
import gms.shared.utilities.geotess.util.containers.arraylist.ArrayListInt;
import gms.shared.utilities.geotess.util.containers.hash.maps.HashMapIntegerDouble;
import gms.shared.utilities.geotess.util.globals.InterpolatorType;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;

/**
 * A Profile defines the distribution of Data along a radial profile through a
 * single layer in a model. While there is a Profile object for each vertex in
 * the 2D grid, and for each layer in the model, a Profile object does not have
 * any information about the geographic position where it is located, nor about
 * which layer it spans. A Profile does manage a list of Data objects and radius
 * values that together define the radial distribution along the Profile. Radii
 * are always listed in monotonically increasing order.
 * <p>
 * Profile is abstract with five derived classes:
 * <ul>
 * <li>ProfileEmpty : two radii and no Data
 * <li>ProfileThin : one radius and one Data
 * <li>ProfileConstant : two radii and one Data
 * <li>ProfileNPoint : two or more radii and an equal number of Data
 * <li>ProfileSurface : no radii and one Data </ol>
 * <p>
 * <p>
 * There is no public constructor. Call the constructor of one of the derived
 * classes to obtain an instance.
 * 
 * @author Sandy Ballard
 */
public abstract class Profile
{
	protected Profile()
	{
	}
	
	public static Profile newProfile(float[] radii, Data[] data) throws GeoTessException
	{
		if (radii.length == 2 && data.length == 0)
			// EMPTY layer defined by two radii and no data
			return new ProfileEmpty(radii[0], radii[1]);
		if (radii.length == 1 && data.length == 1)
			// THIN layer defined by one radius and one data
			return new ProfileThin(radii[0], data[0]);
		if (radii.length == 2 && data.length == 1)
			// CONSTANT layer defined by two radii and one data object
			return new ProfileConstant(radii[0], radii[1], data[0]);
		if (radii.length >= 2 && data.length == radii.length)
			// NPOINT layer with 2 or more radii and one data object for each
			// radius
			return new ProfileNPoint(radii, data);
		if (radii.length == 0 && data.length == 1)
			// SURFACE layer with 0 radii and one data object
			return new ProfileSurface(data[0]);
		if (radii.length == 0 && data.length == 0)
			// SURFACE_EMPTy layer with 0 radii and 0 data
			return new ProfileSurfaceEmpty();

		throw new GeoTessException(String.format("%nERROR in Profile::newProfile%n"
					+"Cannot construct a Profile object with %d radii and %d Data objects. "
					+"Options are (nRadii, nData) = (2,0), (1,1), (2,1), (0,1), (n>1, m=n)",
					radii.length, data.length));
	}

	public static Profile newProfile(float[] radii, double[][] values) throws GeoTessException
	{
		Data[] data = new Data[values.length];
		for (int i=0; i<data.length; ++i) data[i] = Data.getDataDouble(values[i]);
		return newProfile(radii, data);
	}

	public static Profile newProfile(float[] radii, float[][] values) throws GeoTessException
	{
		Data[] data = new Data[values.length];
		for (int i=0; i<data.length; ++i) data[i] = Data.getDataFloat(values[i]);
		return newProfile(radii, data);
	}

	public static Profile newProfile(float[] radii, long[][] values) throws GeoTessException
	{
		Data[] data = new Data[values.length];
		for (int i=0; i<data.length; ++i) data[i] = Data.getDataLong(values[i]);
		return newProfile(radii, data);
	}

	public static Profile newProfile(float[] radii, int[][] values) throws GeoTessException
	{
		Data[] data = new Data[values.length];
		for (int i=0; i<data.length; ++i) data[i] = Data.getDataInt(values[i]);
		return newProfile(radii, data);
	}

	public static Profile newProfile(float[] radii, short[][] values) throws GeoTessException
	{
		Data[] data = new Data[values.length];
		for (int i=0; i<data.length; ++i) data[i] = Data.getDataShort(values[i]);
		return newProfile(radii, data);
	}

	public static Profile newProfile(float[] radii, byte[][] values) throws GeoTessException
	{
		Data[] data = new Data[values.length];
		for (int i=0; i<data.length; ++i) data[i] = Data.getDataByte(values[i]);
		return newProfile(radii, data);
	}

	/**
	 * Static factory method that loads a new Profile object of the appropriate
	 * type directly from an ascii file.
	 * 
	 * @param input
	 * @param metadata
	 * @return a new Profile object
	 * @throws GeoTessException
	 */
	protected static Profile newProfile(Scanner input, GeoTessMetaData metadata)
			throws GeoTessException, IOException
	{
		switch (input.nextInt())
		{
		case 0:
			// EMPTY layer defined by two radii and no data
			return new ProfileEmpty(input);
		case 1:
			// THIN layer defined by one radius and one data
			return new ProfileThin(input, metadata);
		case 2:
			// CONSTANT layer defined by two radii and one data object
			return new ProfileConstant(input, metadata);
		case 3:
			// NPOINT layer with 2 or more radii and one data object for each
			// radius
			return new ProfileNPoint(input, metadata);
		case 4:
			// SURFACE layer with 0 radii and one data object
			return new ProfileSurface(input, metadata);
		case 5:
			// SURFACE_EMPTY layer with 0 radii and one data object
			return new ProfileSurfaceEmpty();
		default:
			throw new GeoTessException("Unrecognized ProfileType");
		}
	}

	/**
	 * Static factory method that loads a new Profile object of the appropriate
	 * type directly from a binary file.
	 * 
	 * @param input
	 * @param metadata
	 * @return a new Profile object
	 * @throws GeoTessException
	 * @throws IOException
	 */
	protected static Profile newProfile(DataInputStream input,
			GeoTessMetaData metadata) throws GeoTessException, IOException
	{
		int profileType = input.readByte();
		switch (profileType)
		{
		case 0:
			// EMPTY layer defined by two radii and no data
			return new ProfileEmpty(input);
		case 1:
			// THIN layer defined by one radius and one data
			return new ProfileThin(input, metadata);
		case 2:
			// CONSTANT layer defined by two radii and one data object
			return new ProfileConstant(input, metadata);
		case 3:
			// NPOINT layer with 2 or more radii and one data object for each
			// radius
			return new ProfileNPoint(input, metadata);
		case 4:
			// SURFACE layer with 0 radii and one data object
			return new ProfileSurface(input, metadata);
		case 5:
			// SURFACE_EMPTY layer with 0 radii and one data object
			return new ProfileSurfaceEmpty();
		default:
			throw new GeoTessException(profileType
					+ " is not a recognized ProfileType");
		}
	}

	/**
	 * One of EMPTY, THIN, CONSTANT, NPOINT, SURFACE
	 * 
	 * @return ProfileType
	 */
	abstract public ProfileType getType();

	/**
	 * Return true if the specified Data value is NaN.  
	 * For doubles and floats, this means not NaN.
	 * For bytes, shorts, ints and longs, always returns false
	 * since there is no value that is NaN
	 * @param nodeIndex
	 * @param attributeIndex
	 * @return true if the specified Data value is valid.  
	 */
	abstract public boolean isNaN(int nodeIndex, int attributeIndex);

	/**
	 * Retrieve the value of the specified attribute interpolated at the
	 * specified radius. If radius is less than radiusBottom then the first Data
	 * value is returned. If radius is greater than radiusTop then the last Data
	 * value is returned.
	 * 
	 * @param interpType
	 *            either InterolatorType.LINEAR or InterpolatorType.CUBIC_SPLINE.
	 * @param attributeIndex
	 *            the index of the attribute whose value is to be computed
	 * @param radius
	 *            the radius of the interpolation point, in km.
	 * @return interpolated value.
	 * @throws GeoTessException
	 */
	public double getValue(InterpolatorType interpType, int attributeIndex,
			double radius, boolean allowRadiusOutOfRange) 
	{
		if (!allowRadiusOutOfRange && (radius < getRadiusBottom() || radius > getRadiusTop()))
			return Double.NaN;
		
		// default behavior is to simply return the data value for the first
		// data object. This works for all the Profile classes that only
		// support a single Data object like ProfileConstant, ProfileSurface,
		// ProfileThin, ProfileSurface, etc. Profile classes that support many
		// Data objects, like ProfileNPoints, need to override this method.
		return getData(0).getDouble(attributeIndex);
	}

	/**
	 * Retrieve the value of the specified attribute computed using
	 * the specified node indexes and interpolation coefficients.
	 * @param coefficients map from nodeIndex to interpolationCoefficient.
	 * @param attribute attribute index.
	 * @return the value of the specified attribute computed using
	 * the specified node indexes and interpolation coefficients.
	 */
	public double getValue(HashMapIntegerDouble coefficients, int attribute)
	{
		double value = 0;
		HashMapIntegerDouble.Entry e;
		HashMapIntegerDouble.Iterator it = coefficients.iterator();
		while (it.hasNext())
		{
			e = it.nextEntry();
			value += getValue(attribute, e.getKey())*e.getValue();
		}
		return value;
	}

	public double getValue(ArrayListInt nodeIds, ArrayListDouble coefficients, int attribute)
	{
		double value = 0;
		for (int i=0; i<nodeIds.size(); ++i)
			value += getValue(attribute, nodeIds.get(i))*coefficients.get(i);
		
		return value;
	}

	/**
	 * Retrieve the value of the specified attributes at the specified
	 * node index.  Note that the number of nodes and number of radii
	 * are sometimes, but not always the same.  
	 * @param attributeIndex
	 * @param nodeIndex 
	 * @return the value of the specified attribute at the specified
	 * node index.
	 */
	abstract public double getValue(int attributeIndex, int nodeIndex);
	
	/**
	 * Retrieve an array containing all the data values arranged from 
	 * deepest to shallowest.
	 * @param attributeIndex
	 * @return an array containing all the data values arranged from 
	 * deepest to shallowest.
	 */
	public double[] getValues(int attributeIndex)
	{
		double[] values = new double[getNData()];
		for (int i=0; i<values.length; ++i)
			values[i] = getValue(attributeIndex, i);
		return values;
	}

	/**
	 * Retrieve the value of the specified attribute at the top 
	 * of the layer.
	 * @param attributeIndex
	 * @return the value of the specified attribute at the top 
	 * of the layer.
	 */
	abstract public double getValueTop(int attributeIndex);

	/**
	 * Retrieve the value of the specified attribute at the bottom 
	 * of the layer.
	 * @param attributeIndex
	 * @return the value of the specified attribute at the bottom 
	 * of the layer.
	 */
	public double getValueBottom(int attributeIndex)
	{
		return getValue(attributeIndex, 0);
	}

	/**
	 * Get the i'th radius value in this profile in km. Radii are in order of
	 * increasing radius.
	 * <p>
	 * Behavior when i is out-of-bounds:
	 * <ul>
	 * <li>ProfileEmpty - i == 0 ? radii[0] : radii[1]
	 * <li>ProfileThin - always returns radii[0]
	 * <li>ProfileConstant - i == 0 ? radii[0] : radii[1]
	 * <li>ProfileNPoint - throws IndexOutOfBoundsException if i is out of range
	 * <li>ProfileSurface - always return Double.NaN
	 * </ul>
	 * 
	 * @param i
	 * @return the i'th radius value in this profile in km.
	 */
	abstract public double getRadius(int i);

	abstract public void setRadius(int i, float d);
	
	/**
	 * Retrieve a shallow copy of the array of Data objects associated with 
	 * this profile.  The number of elements depends on ProfileType:
	 * <ul>
	 * <li>EMPTY -- 0
	 * <li>THIN  -- 1
	 * <li>CONSTANT -- 1
	 * <li>NPOINT        -- N
	 * <li>SURFACE       -- 1
	 * <li>SURFACE_EMPTY -- 0
	 * </ul>
	 * 
	 * @return Data[]
	 */
	abstract public Data[] getData();

	/**
	 * Retrieve a reference the i'th Data object.
	 * <ul>
	 * <li>ProfileEmpty - returns null
	 * <li>ProfileThin - i is ignored. Always returns Data[0]
	 * <li>ProfileConstant - i is ignored. Always returns Data[0]
	 * <li>ProfileNPoint - throws IndexOutOfBoundsException if i is out of range
	 * <li>ProfileSurface - i is ignored. Always returns Data[0]
	 * <li>ProfileSurfaceEmpty - returns null
	 * </ul>
	 * 
	 * @param i
	 * @return Data
	 */
	abstract public Data getData(int i);

	/**
	 * Replace the Data currently associated with this Profile with new Data
	 * 
	 * @param data
	 * @throws GeoTessException
	 */
	abstract public void setData(Data... data);

	/**
	 * Replace one of the Data objects currently associated with this Profile
	 * 
	 * @param index
	 * @param data
	 * @throws ArrayIndexOutOfBoundsException
	 */
	abstract public void setData(int index, Data data);

	/**
	 * Get the radius at the top of the profile, in km.
	 * 
	 * @return the radius at the top of the profile, in km.
	 */
	abstract public double getRadiusTop();

	/**
	 * Retrieve a reference to the Data object at the top of the profile.
	 * 
	 * @return the Data object at the top of the profile.
	 */
	abstract public Data getDataTop();

	/**
	 * Get the radius at the bottom of the profile, in km.
	 * 
	 * @return the radius at the bottom of the profile, in km.
	 */
	abstract public double getRadiusBottom();

	/**
	 * Retrieve a reference to the Data object at the bottom of the profile.
	 * 
	 * @return the Data object at the bottom of the profile.
	 */
	abstract public Data getDataBottom();

	/**
	 * Get the number of radii that comprise this profile.
	 * 
	 * @return the number of radii that comprise this profile.
	 */
	abstract public int getNRadii();

	/**
	 * Get the number of Data objects that comprise this profile.
	 * 
	 * @return the number of Data objects that comprise this profile.
	 */
	abstract public int getNData();

	/**
	 * Get the radii values in km. For ProfileNPoint object, returns a reference
	 * to the profiles radii array. The other profile classes return a new
	 * float[] that contains the available radius values. ProfileSurface returns
	 * new float[0].
	 * 
	 * @return the radii values in km.
	 */
	abstract public float[] getRadii();

	/**
	 * Return the thickness of the layer in km.
	 * 
	 * @return thickness of the layer in km.
	 */
	public double getThickness()
	{
		return getRadiusTop() - getRadiusBottom();
	}

	/**
	 * Write the radii and data values to ascii file.
	 * 
	 * @param output
	 * @throws IOException
	 */
	abstract protected void write(Writer output) throws IOException;

	/**
	 * Write the radii and data values to binary file.
	 * 
	 * @param output
	 * @throws IOException
	 */
	abstract protected void write(DataOutputStream output) throws IOException;

	/**
	 * Find index i such that x is >= xx[i] and < xx[i+1]. 
	 * If x <  xx[0] returns -1. 
	 * If x == xx[xx.length-1] return xx.length-2
	 * If x >  xx[xx.length-1] return xx.length-1
	 * <p>
	 * This method is translation from Numerical Recipes in C++.
	 * 
	 * @param radius
	 * @return index i such that radius is >= radii[i] and < radii[i+1].
	 */
	public int getRadiusIndex(double radius)
	{
		return radius < getRadiusBottom() ? -1 : radius > getRadiusTop() ? getNRadii()-1 : 0;
	}

	public void setInterpolationCoefficients(InterpolatorType interpType, 
			ArrayListInt nodeIndexes, ArrayListDouble coefficients, 
			double radius, boolean allowOutOfRange)
	{
		// this code works for Profiles constant, thin and surface.  
		// ProfileNPoint and ProfileEmpty will override it.
		nodeIndexes.add(0);
		if (!allowOutOfRange && (radius < getRadiusBottom() || radius > getRadiusTop()))
			coefficients.add(Double.NaN);
		else
			coefficients.add(1.);

	}

	@Override
	public String toString()
	{
		return String.format("%s radiusRange= %1.3f to %1.3f; thick = %1.3f",
				getType(), getRadiusBottom(), getRadiusTop(), getRadiusTop()
						- getRadiusBottom());
	}

	/**
	 * Find the node index of the radius in this Profile that has radius closest
	 * to the supplied radius.
	 * 
	 * @param radius
	 * @return node index of closest radius
	 */
	public abstract int findClosestRadiusIndex(double radius);
	
	/**
	 * Set the pointIndex that corresponds to the supplied nodeIndex.  
	 * <p>There is a node index for each Data object in a profile and they are indexed from 0 to 
	 * the number of Data objects managed by a Profile.  There is a pointIndex for every 
	 * Data object in the entire model, indexed from 0 to the number of Data objects in the 
	 * model. 
	 * @param nodeIndex
	 * @param pointIndex
	 */
	public abstract void setPointIndex(int nodeIndex, int pointIndex);
	
	/**
	 * Get the pointIndex that corresponds to the supplied nodeIndex.  
	 * <p>There is a node index for each Data object in a profile and they are indexed from 0 to 
	 * the number of Data objects managed by a Profile.  There is a pointIndex for every 
	 * Data object in the entire model, indexed from 0 to the number of Data objects in the 
	 * model. 
	 * @param nodeIndex
	 * @return poitnIndex
	 */
	public abstract int getPointIndex(int nodeIndex);
	
	/**
	 * Find the point indeces of the nodes above and below the specified radius and add them
	 * to the supplied list of points.  Point index values of -1 are not added to the set.
	 * The set is not cleared before addition.
	 * @param radius
	 * @param points
	 */
	public void getPointIndices(float radius, HashSet<Integer> points)
	{
		// this works for Profile types Constant, Thin and Surface since they only have a single node 
		// in the profile.  It also works for ProfileEmpty and ProfileSurfaceEmpty since the point indeces are -1.
		// It does not work for ProfileNPoint it overrides this method.
		if (getPointIndex(0) >= 0)
			points.add(getPointIndex(0));
	}

	public void getWeights(HashMap<Integer, Double> weights, double dkm, double radius, 
			double hcoefficient, InterpolatorType radialInterpType)
	{
		// this works for Profile types Constant, Thin and Surface since they only have a single node 
		// in the profile.  It does not work for ProfileNPoint and ProfileEmpty so they override this method.
		
		// get the point index of the one-and-only node.
		int index = getPointIndex(0);
		// find the current weight of the point, if it exists.
		Double w = weights.get(index);
		
		// either set the weight of pointIndex (if it does not already exist), 
		// or add the new weight to the existing weight.
		if (w == null)
			weights.put(index, dkm*hcoefficient);
		else
			weights.put(index, w+dkm*hcoefficient);
	}

	public void getWeights(Map<Integer, Double> weights, double dkm, double radius, 
			double hcoefficient, InterpolatorType radialInterpType)
	{
		// this works for Profile types Constant, Thin and Surface since they only have a single node 
		// in the profile.  It does not work for ProfileNPoint and ProfileEmpty so they override this method.
		
		// get the point index of the one-and-only node.
		int index = getPointIndex(0);
		// find the current weight of the point, if it exists.
		Double w = weights.get(index);
		
		// either set the weight of pointIndex (if it does not already exist), 
		// or add the new weight to the existing weight.
		if (w == null)
			weights.put(index, dkm*hcoefficient);
		else
			weights.put(index, w+dkm*hcoefficient);
	}

	/**
	 * Return a deep copy of this Profile
	 * @return a deep copy of this Profile
	 * @throws GeoTessException 
	 */
	abstract public Profile copy() throws GeoTessException;

	abstract public void resetPointIndices();


	/**
	 * Performs Trapezoidal integration of the requested attribute, or it's
	 * reciprocal over the depth of the profile. The result is returned on exit.
	 * Zero is returned for profiles with no thickness. This method is overridden
   * for profiles with defined depth.

	 * @param attributeIndex The index of the attribute whose integral is returned.
	 * @param reciprocal     If true the reciprocal attribute is integrated over
	 *                       the profile depth.
	 * @param Returns the integration result.
	 */
	public double integrate(int attributeIndex, boolean reciprocal) { return 0.; }
	
	/**
	 * Synchronized method to compute the gradients if needed. Does nothing if
	 * gradients are already computed, or if gradients are undefined for the
	 * profile type (the implementation below).
	 * 
	 * @param model           GeoTessModel for which gradients are evaluated.
	 * @param attributeIndex  The attribute for which gradients are calculated.
	 * @param LayerId The layer for which gradients are determined.
	 * @param reciprocal      A boolean flag, that if true, stores the calculates
	 *                        gradient.
	 * @throws GeoModelException
	 */
	protected synchronized void computeGradients(GeoTessModel model, int attributeIndex,
			                            						 double[] vertexUnitVector,
			                            						 int layerId, boolean reciprocal)
	               throws GeoTessException
	{
	  // do nothing
	}

	protected void addToGradient(int attributeIndex, double radius,
			                         double coefficient, double[] gradient)
	{
		// do nothing
	}
	
	protected void addToGradient(int attributeIndex, int nodeIndex,
                               double coefficient, double[] gradient)
  {
		// do nothing
  }

	protected void getGradient(int nodeIndex, int attributeIndex,
	                           double[] gradient)
	{
		gradient[0] = gradient[1] = gradient[2] = Double.NaN;
	}

	protected void getGradient(int attributeIndex, double radius, double[] gradient)
	{
		gradient[0] = gradient[1] = gradient[2] = Double.NaN;
	}
	
	protected void getGradientTop(int attributeIndex, double[] gradient)
	{
		gradient[0] = gradient[1] = gradient[2] = Double.NaN;
	}

	protected void getGradientBottom(int attributeIndex, double[] gradient)
	{
		gradient[0] = gradient[1] = gradient[2] = Double.NaN;
	}
	
	protected double[] getGradient(int nodeIndex, int attributeIndex)
	{
		double[] gradient = {Double.NaN, Double.NaN, Double.NaN};
		return gradient;
	}

  protected boolean isGradientSet(int attributeIndex)
  {
    return false;
  }

  protected boolean getGradientReciprocalFlag(int attributeIndex)
  {
    return false;
  }

  /**
   * Writes a formated gradient string. Used by Profile toString() methods that
   * define gradients.
   * @param g          The gradient to returned as a formatted string.
   * @param reciprocal If true the reciprocal attribute gradient is output and
   *                   the identifier "(Inv)" is added to the output string.
   * @return The formatted gradient string.
   */
	protected String gradientString(double[] g, boolean reciprocal)
	{
		if (reciprocal)
			return vectorString(g) + " (Inv)";
		else
			return vectorString(g);
	}

	/**
	 * Returns the input vector as a formatted string of the form "[v[0] v[1] v[2]".
	 * Used by Profile toString() methods that define gradients or layer normals.
	 * 
	 * @param v The vector to be formatted as astring.
	 * @return The formatted vector.
	 */
	protected String vectorString(double[] v)
	{
		if (v == null)
			return "[undefined]";
		else
			return String.format("[%14.7g %14.7g %14.7g]", v[0], v[1], v[2]);
	}

  protected void setLayerNormal(double[] layrNormal)
  {
    // do nothing
  }
  
  protected double[] getLayerNormal()
  {
    return null;
  }

}
