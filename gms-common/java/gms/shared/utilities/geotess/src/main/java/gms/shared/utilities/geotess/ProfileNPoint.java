package gms.shared.utilities.geotess;

import static gms.shared.utilities.geotess.util.globals.Globals.NL;
import gms.shared.utilities.geotess.util.containers.arraylist.ArrayListDouble;
import gms.shared.utilities.geotess.util.containers.arraylist.ArrayListInt;
import gms.shared.utilities.geotess.util.globals.InterpolatorType;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;

/**
 * A Profile defined by two or more radii and an equal number of Data objects.
 * 
 * @author Sandy Ballard
 * 
 */
public class ProfileNPoint extends Profile
{
	/**
	 * nPoints array containing the radii of the nodes on this profile.
	 */
	protected float[] radii;

	/**
	 * nPoints array containing the Data objects associated with each node on
	 * this profile.
	 */
	protected Data[] data;

	/**
	 * nAttributes x nPoints array containing the second derivatives at the node
	 * points. Used when doing cubic spline interpolation. Lazy evaluation is
	 * used, so elements of this variable are only instantiated when requested
	 * the first time, then they are permanently stored.
	 */
	private double[][] y2;
	
	/**
	 * nAttributes x nPoints x 3 array containing the gradients of each attribute
	 * for each point index. Only attributes for which gradients are requested
	 * are stored in the array (i.e. gradient[attributeIndex] may equal NULL if
	 * attributeIndex was not requested for gradient computation). 
	 */
	private double[][][] gradients;
	
	/**
	 * The reciprocal flag for each stored gradient. If true and the gradient
	 * for attribute i is defined (gradients[i] != null), then the stored
	 * gradient is reciprocal for the defined attribute (e.g. the Reciprocal of
	 * SSLOWNESS is the S wave velocity).
	 */
  protected boolean[] gradientReciprocal;
	
	/**
	 * Map from nodeIndex to pointIndex.
	 * <p>There is a node index for each Data object in a profile and they are indexed from 0 to 
	 * the number of Data objects managed by a Profile.  There is a pointIndex for every 
	 * Data object in the entire model, indexed from 0 to the number of Data objects in the 
	 * model. 
	 */
	private int[] pointIndices;

	/**
	 * The layer normal at the top radius of this profile.
	 */
	private double[] layerNormal = null;

	/**
	 * Parameterized constructor that takes an array of radius values and an
	 * equal-size array of Data objects.  This Profile keeps a reference to
	 * the supplied radii, and Data objects (no copy is made).
	 * 
	 * @param radii
	 *            2 or more monotonically increasing radius values
	 * @param data
	 *            array of Data objects. Length must equal number of radii.
	 * @throws GeoTessException
	 *             if radii.length < 2 or radii.length != data.length or radii
	 *             are not monotonically increasing.
	 */
	public ProfileNPoint(float[] radii, Data[] data) throws GeoTessException
	{
		if (radii.length < 2 || radii.length != data.length)
			throw new GeoTessException(
					String.format(
							"radii.length=%d, data.length=%d but they must be equal and >= 2",
							radii.length, data.length));

		if (radii[0] > radii[radii.length - 1])
			throw new GeoTessException(
					String.format("%nradii are not monotonically increasing%n"));

		this.radii = radii;
		this.data = data;
	}

	/**
	 * Constructor that loads required information from an ascii file.
	 * 
	 * @param input
	 * @param metaData
	 * @throws GeoTessException
	 */
	protected ProfileNPoint(Scanner input, GeoTessMetaData metaData)
			throws GeoTessException, IOException
	{
		// layer with 2 or more radii and one data object for each radius
		radii = new float[input.nextInt()];
		data = new Data[radii.length];
		for (int k = 0; k < radii.length; ++k)
		{
			radii[k] = input.nextFloat();
			data[k] = Data.getData(input, metaData);
		}
	}

	/**
	 * Constructor that loads required information from a binary file.
	 * 
	 * @param input
	 * @param metaData
	 * @throws GeoTessException
	 * @throws IOException
	 */
	protected ProfileNPoint(DataInputStream input, GeoTessMetaData metaData)
			throws GeoTessException, IOException
	{
		// layer with 2 or more radii and one data object for each radius
		radii = new float[input.readInt()];
		data = new Data[radii.length];
		for (int k = 0; k < radii.length; ++k)
		{
			radii[k] = input.readFloat();
			data[k] = Data.getData(input, metaData);
		}
	}

	@Override
	protected void write(Writer output) throws IOException
	{
		output.write(String.format("%d %d%n", getType().ordinal(), radii.length));
		for (int i = 0; i < radii.length; ++i)
			output.append(Float.toString(radii[i])).append(" ").append(data[i].toString()).append('\n');
	}

	@Override
	protected void write(DataOutputStream output) throws IOException
	{
		output.writeByte((byte) getType().ordinal());
		output.writeInt(radii.length);
		for (int i = 0; i < radii.length; ++i)
		{
			output.writeFloat(radii[i]);
			data[i].write(output);
		}
	}

	@Override
	public ProfileType getType()
	{
		return ProfileType.NPOINT;
	}

	@Override
	public boolean equals(Object other)
	{
		if (other == null || !(other instanceof ProfileNPoint))
			return false;
		
		if (radii.length != ((ProfileNPoint)other).radii.length
				|| data.length != ((ProfileNPoint)other).data.length)
			return false;
		
		for (int i = 0; i < radii.length; ++i)
			if (radii[i] != ((ProfileNPoint)other).radii[i]
					|| !data[i].equals(((ProfileNPoint)other).data[i]))
				return false;
		return true;
	}

	@Override
	public Data[] getData()
	{
		return data.clone();
	}

	@Override
	public Data getData(int i)
	{
		return data[i];
	}

	/**
	 * Replace the Data[] currently associated with this ProfileNPoint with a
	 * new Data[]
	 * 
	 * @param data
	 *            a data array of size equal to the number of radii
	 * @throws GeoTessException
	 *             if the size of the supplied Data[] is not equal to the number
	 *             of radii supported by this ProfileNPoint
	 */
	@Override
	public void setData(Data... data)
	{
		if (data.length != radii.length)
			throw new IllegalArgumentException("data.length != radii.length");
		this.data = data;
	}
	
	/**
	 * Replace one of the Data objects currently associated with this Profile
	 * 
	 * @param index
	 * @param data
	 * @throws ArrayIndexOutOfBoundsException
	 */
	@Override
	public void setData(int index, Data data) 
	{
		if (index < this.data.length)
			this.data[index] = data;
		else 
			throw new java.lang.ArrayIndexOutOfBoundsException();
	}

	@Override
	public double getRadius(int index)
	{
		return radii[index];
	}

	@Override
	public void setRadius(int node, float radius) {
		if (node >= 0 && node < radii.length) 
			radii[node] = radius;
	}
	
	@Override
	public double getRadiusTop()
	{
		return radii[radii.length - 1];
	}

	@Override
	public Data getDataTop()
	{
		return data[data.length - 1];
	}

	@Override
	public double getRadiusBottom()
	{
		return radii[0];
	}

	@Override
	public Data getDataBottom()
	{
		return data[0];
	}

	@Override
	public int getNRadii()
	{
		return radii.length;
	}

	@Override
	public int getNData()
	{
		return data.length;
	}

	@Override
	public float[] getRadii()
	{
		return radii;
	}

	/**
	 * Retrieve the value of the specified attribute interpolated at the
	 * specified radius. If radius is less than radiusBottom then the first Data
	 * value is returned. If radius is greater than radiusTop then the last Data
	 * value is returned.
	 * 
	 * @param interpType
	 *            either InterolatorType.LINEAR or InterpolatorType.SPLINE.
	 * @param attributeIndex
	 *            the index of the attribute whose value is to be computed
	 * @param radius
	 *            the radius of the interpolation point, in km.
	 * @return interpolated value.
	 */
	@Override
	public double getValue(InterpolatorType interpType, int attributeIndex,
			double radius, boolean allowOutOfRange) 
	{
		if (!allowOutOfRange && (radius < (double)radii[0] || radius > (double)radii[radii.length-1]))
			return Double.NaN;
		
		int index = getRadiusIndex(radius);
		
		if (index < 0)
			return data[0].getDouble(attributeIndex);

		if (index >= radii.length-1)
			return data[radii.length-1].getDouble(attributeIndex);

		double r0 = radii[index];
		double v0 = data[index].getDouble(attributeIndex);

		double r1 = radii[index + 1];
		double v1 = data[index + 1].getDouble(attributeIndex);

		if (radius >= r1)
			return v1;

		double a = (r1 - radius) / (r1 - r0);

		double v = a * v0 + (1 - a) * v1;

		switch (interpType)
		{
		case LINEAR:
			return v;
		case CUBIC_SPLINE:
			check(attributeIndex);
			double b = 1. - a;
			// implement splint()
			return v
					+ ((a * a * a - a) * y2[attributeIndex][index] 
					+  (b * b * b - b) * y2[attributeIndex][index + 1]) 
					* (r1 - r0) * (r1 - r0) / 6.0;

		default:
			throw new IllegalArgumentException(
					interpType.toString()
							+ " cannot be applied to a Profile.  "
							+ "Must specify one of InterolatorType.LINEAR or InterpolatorType.CUBIC_SPLINE");
		}
	}

  /**
   * Retrieve the value of the specified attributes at the specified
   * radius index.
   * @param attributeIndex
   * @param nodeIndex
   * @return the value of the specified attributes at the specified
   * radius index.
   */
	@Override
	public double getValue(int attributeIndex, int nodeIndex)
	{
		return nodeIndex >= 0 && nodeIndex < data.length 
				? data[nodeIndex].getDouble(attributeIndex)
				: Double.NaN;
	}

	@Override
	public boolean isNaN(int nodeIndex, int attributeIndex)
	{
		return data[nodeIndex].isNaN(attributeIndex);
	}

	/**
	 * Retrieve the value of the specified attribute at the top 
	 * of the layer.
	 * @param attributeIndex
	 * @return the value of the specified attribute at the top 
	 * of the layer.
	 */
	public double getValueTop(int attributeIndex)
	{ return data[data.length-1].getDouble(attributeIndex); }
	
	/**
	 * Retrieve a new Data object of the same DataType and with the same 
	 * number of attributes, with values interpolated from this Profile 
	 * at the specified radius.
	 * @param interpType
	 * @param radius
	 * @param allowOutOfRange if true and radius is out of range, values at top
	 * or bottom of the Profile will be returned.
	 * @return a new Data object of the same DataType and with the same 
	 * number of attributes, with values interpolated from this Profile 
	 * at the specified radius.
	 * @throws GeoTessException
	 */
	public Data getData(InterpolatorType interpType, double radius, boolean allowOutOfRange) throws GeoTessException
	{
		Data newData = Data.getData(data[0].getDataType(), data[0].size());
		for (int i=0; i<data[0].size(); ++i)
			newData.setValue(i, getValue(interpType, i, radius, allowOutOfRange));
		
		return newData;
	}

	@Override
	public void setInterpolationCoefficients(InterpolatorType interpType, 
			ArrayListInt nodeIndexes, ArrayListDouble coefficients, 
			double radius, boolean allowOutOfRange)
	{
		//TODO:  need this method to work for cubic spline interpolator.  It currently does not.

		int index = getRadiusIndex((float)radius);
		if (index < 0)
		{
			nodeIndexes.add(0);
			coefficients.add(allowOutOfRange ? 1.0 : Double.NaN);
		}
		else if (index >= radii.length-1)
		{
			nodeIndexes.add(radii.length-1);
			coefficients.add(allowOutOfRange ? 1.0 : Double.NaN);
		}
		else
		{
			double c = ((double)radii[index + 1] - radius) / 
					((double)radii[index + 1] - (double)radii[index]);
			nodeIndexes.add(index);
			coefficients.add(c);
			if (c < 1.)
			{
				nodeIndexes.add(index+1);
				coefficients.add(1.-c);
			}
		}
	}

	/**
	 * Find index i such that x is >= xx[i] and < xx[i+1]. 
	 * If x <  xx[0] returns -1. 
	 * If x == xx[xx.length-1] return xx.length-2
	 * If x >  xx[xx.length-1] return xx.length-1
	 * <p>
	 * This method is translation from Numerical Recipes in C++.
	 * 
	 * @param radius
	 */
	public int getRadiusIndex(float radius)
	{
		int ju,jm,jl;
		//boolean ascnd=(radii[n-1] >= radii[0]);

		jl=-1;
		ju=radii.length;
		while (ju-jl > 1) 
		{
			jm=(ju+jl) >> 1;
		if (radius >= radii[jm]) // == ascnd)
			jl=jm;
		else
			ju=jm;
		}
		if (radius == radii[0]) 
			return 0;
		else if (radius == radii[radii.length-1]) 
			return radii.length-2;
		return jl;
	}

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
	@Override
	public int getRadiusIndex(double radius)
	{
		return getRadiusIndex((float)radius);
	}

	synchronized private void check(int attributeIndex)
	{
		if (y2 == null)
			y2 = new double[data[0].size()][];
		if (y2[attributeIndex] == null)
			y2[attributeIndex] = spline(radii, data, attributeIndex, 1e30, 1e30);
	}

	private double[] spline(float[] x, Data[] y, int attributeIndex,
			double yp1, double ypn)
	{
		int i, k;
		double p, qn, sig, un;

		int n = x.length;
		double[] y2 = new double[n];
		double[] u = new double[n - 1];
		if (yp1 > 0.99e30)
			y2[0] = u[0] = 0.;
		else
		{
			y2[0] = -0.5;
			u[0] = (3.0 / (x[1] - x[0]))
					* ((y[1].getDouble(attributeIndex) - y[0]
							.getDouble(attributeIndex)) / (x[1] - x[0]) - yp1);
		}
		for (i = 1; i < n - 1; i++)
		{
			sig = (x[i] - x[i - 1]) / (x[i + 1] - x[i - 1]);
			p = sig * y2[i - 1] + 2.0;
			y2[i] = (sig - 1.0) / p;
			u[i] = (y[i + 1].getDouble(attributeIndex) - y[i]
					.getDouble(attributeIndex))
					/ (x[i + 1] - x[i])
					- (y[i].getDouble(attributeIndex) - y[i - 1]
							.getDouble(attributeIndex)) / (x[i] - x[i - 1]);
			u[i] = (6.0 * u[i] / (x[i + 1] - x[i - 1]) - sig * u[i - 1]) / p;
		}
		if (ypn > 0.99e30)
			qn = un = 0.0;
		else
		{
			qn = 0.5;
			un = (3.0 / (x[n - 1] - x[n - 2]))
					* (ypn - (y[n - 1].getDouble(attributeIndex) - y[n - 2]
							.getDouble(attributeIndex)) / (x[n - 1] - x[n - 2]));
		}
		y2[n - 1] = (un - qn * u[n - 2]) / (qn * y2[n - 2] + 1.0);
		for (k = n - 2; k >= 0; --k)
			y2[k] = y2[k] * y2[k + 1] + u[k];
		return y2;
	}

	/**
	 * Find the index of the node in this Profile that has radius closest to the
	 * supplied radius.
	 * 
	 * @param radius in km
	 * @return the index of the node in this Profile that has radius closest to the
	 * supplied radius.
	 */
	public int findClosestRadiusIndex(double radius)
	{
		int i = getRadiusIndex((float)radius);
		if (i < 0)
			return 0;
		if (i >= radii.length-1)
			return i;
		return Math.abs(radii[i+1] - radius) < Math.abs(radius - radii[i]) ? i+1 : i;
	}

	/**
	 * Set the pointIndex that corresponds to the supplied nodeIndex.  
	 * <p>There is a node index for each Data object in a profile and they are indexed from 0 to 
	 * the number of Data objects managed by a Profile.  There is a pointIndex for every 
	 * Data object in the entire model, indexed from 0 to the number of Data objects in the 
	 * model. 
	 * @param nodeIndex
	 * @param pointIndex
	 */
	public void setPointIndex(int nodeIndex, int pointIndex)
	{
		if (pointIndices == null)
		{
			if (pointIndex < 0)
				return;
			
			pointIndices = new int[data.length];
			Arrays.fill(pointIndices, -1);
		}
		pointIndices[nodeIndex] = pointIndex;
	}
	
	@Override
	public void resetPointIndices()
	{ this.pointIndices = null; }

	/**
	 * Get the pointIndex that corresponds to the supplied nodeIndex.  
	 * <p>There is a node index for each Data object in a profile and they are indexed from 0 to 
	 * the number of Data objects managed by a Profile.  There is a pointIndex for every 
	 * Data object in the entire model, indexed from 0 to the number of Data objects in the 
	 * model. 
	 * @param nodeIndex
	 * @return poitnIndex
	 */
	public int getPointIndex(int nodeIndex)
	{
		return pointIndices == null ? -1 : pointIndices[nodeIndex];
	}

	@Override
	public void getPointIndices(float radius, HashSet<Integer> points)
	{
		int index = getRadiusIndex((float)radius);
		if (index < 0)
		{ 
			if (getPointIndex(0) > 0)
				points.add(getPointIndex(0));
		}
		else if (index >= radii.length-1)
		{
			if (getPointIndex(radii.length-1) > 0)
				points.add(getPointIndex(radii.length-1));
		}
		else 
		{
			if (getPointIndex(index) > 0)
				points.add(getPointIndex(index));
			if (radius > radii[index] && getPointIndex(index+1) > 0)
				points.add(getPointIndex(index+1));
		}
	}

	@Override
	public void getWeights(HashMap<Integer, Double> weights, double dkm, double radius, double hcoefficient, InterpolatorType radialInterpType)
	{
		//TODO:  need getInterpolationCoefficient to work for cubic spline interpolator.  It currently does not.

		ArrayListInt indexes = new ArrayListInt();
		ArrayListDouble coefficients = new ArrayListDouble();
		
		setInterpolationCoefficients(radialInterpType, indexes, coefficients, radius, true);
		Double w;
		int pt;
		
		for (int i=0; i<indexes.size(); ++i)
		{
			pt = getPointIndex(indexes.get(i));
			w = weights.get(pt);
			weights.put(pt, w == null ? dkm*hcoefficient*coefficients.get(i) 
					: w+dkm*hcoefficient*coefficients.get(i));
		}
	}

	@Override
	public void getWeights(Map<Integer, Double> weights, double dkm, double radius, double hcoefficient, InterpolatorType radialInterpType)
	{
		//TODO:  need getInterpolationCoefficient to work for cubic spline interpolator.  It currently does not.

		ArrayListInt indexes = new ArrayListInt();
		ArrayListDouble coefficients = new ArrayListDouble();
		
		setInterpolationCoefficients(radialInterpType, indexes, coefficients, radius, true);
		Double w;
		int pt;
		
		for (int i=0; i<indexes.size(); ++i)
		{
			pt = getPointIndex(indexes.get(i));
			w = weights.get(pt);
			weights.put(pt, w == null ? dkm*hcoefficient*coefficients.get(i) 
					: w+dkm*hcoefficient*coefficients.get(i));
		}
	}

	/**
	 * Outputs this Profile as a formatted string.
	 */
	@Override
	public String toString()
	{
		StringBuffer buf = new StringBuffer();
    buf.append("  Type: " + getType().name() + NL);
    buf.append("    Radii:" + NL);
    for (int i = 0; i < radii.length; ++i)
      buf.append("      " + String.format("%3d  %9.4f", i, radii[i]) + NL);
    buf.append("    Point Indices: " + NL);
    for (int i = 0; i < radii.length; ++i)
      buf.append("      " + String.format("%3d  %7d", i, pointIndices[i]) + NL);
    buf.append("    Data: " + NL);
    for (int i = 0; i < radii.length; ++i)
      buf.append("      " + String.format("%3d  %s", i, data[i].toString()) + NL);
    buf.append("    Layer Normal: " + vectorString(layerNormal) + NL);
    if (gradients == null)
      buf.append("    Gradients: [undefined]" + NL);
    else
    {
      buf.append("    Gradients: ");
    	for (int i = 0; i < radii.length; ++ i)
    	{
        buf.append("      " + String.format("%3d : ", i));
    		for (int j = 0; j < gradients.length; ++j)
	    	{
	        buf.append(gradientString(gradients[j][i], gradientReciprocal[j]));
	    		if (j < gradients.length-1) buf.append(", ");
	    	}
        buf.append(NL);
    	}
    }
	  return buf.toString();
	}

	/**
	 * Returns an independent deep copy of this profile.
	 */
	@Override
	public Profile copy() throws GeoTessException
	{
		Data[] d = new Data[data.length];
		for (int i=0; i<d.length; ++i)
			d[i] = data[i].copy();
		ProfileNPoint pnp = new ProfileNPoint(radii.clone(), d);
		
		if (pointIndices != null)
		  pnp.pointIndices = pointIndices.clone();
		
		if (layerNormal != null)
			pnp.layerNormal = layerNormal.clone();

		if (gradients != null)
		{
			pnp.gradientReciprocal = gradientReciprocal.clone();
			pnp.gradients = gradients.clone();
			for (int i=0; i < gradients.length; ++i)
			{
				if (gradients[i] != null)
					pnp.gradients[i] = gradients[i].clone();
			}
		}
		
		return pnp;
	}

	/**
	 * Performs Trapezoidal integration of the requested attribute, or it's
	 * reciprocal over the depth of the profile. The result is returned on exit.
	 * 
	 * @param attributeIndex The index of the attribute whose integral is returned.
	 * @param reciprocal     If true the reciprocal attribute is integrated over
	 *                       the profile depth.
	 * @param Returns the integration result.
	 */
	@Override
	public double integrate(int attributeIndex, boolean reciprocal)
	{
		double integral = 0;
		if (reciprocal)
		{
			for (int i=1; i<radii.length; ++i)
				integral += (radii[i]-radii[i-1])/(data[i].getDouble(attributeIndex)+data[i-1].getDouble(attributeIndex));
			return integral*2;
		}
		else
		{
			for (int i=1; i<radii.length; ++i)
				integral += (radii[i]-radii[i-1])*(data[i].getDouble(attributeIndex)+data[i-1].getDouble(attributeIndex));
			return integral/2;
		}
	}
	
	/**
	 * Synchronized method to compute the gradient of a NPoint layer. Does
	 * nothing if gradient is already computed.
	 * 
	 * @param model           GeoTessModel for which gradients are evaluated.
	 * @param attributeIndex  The attribute for which gradients are calculated.
	 * @param LayerId         The layer for which gradients are determined.
	 * @param reciprocal      A boolean flag, that if true, stores the gradient
	 *                        of the inverse attribute field.
	 * @throws GeoModelException
	 */
	@Override
	protected synchronized void computeGradients(GeoTessModel model, int attributeIndex,
																							 double[] unitVector, int layerId,
																							 boolean reciprocal) 
                 throws GeoTessException
  {
		// create gradients array for each attribute if null
		if (gradients == null)
		{
			gradients = new double[data[0].size()][][];
			gradientReciprocal = new boolean [data[0].size()];
		}

		// create requested attribute entry if null or the reciprocal flag has
		// changed ... otherwise exit
		if ((gradients[attributeIndex] == null) ||
		    (gradientReciprocal[attributeIndex] != reciprocal))
		{
			gradients[attributeIndex] = new double[radii.length][3];
			gradientReciprocal[attributeIndex] = reciprocal;
		}
		else
			return;

		// get attribute gradient reference and unit vector for point to be evaluated
		double[][] g = gradients[attributeIndex];

		// retrieve gradient calculator, and loop over each radii and compute gradient
		GradientCalculator gc = model.getGradientCalculator();
		for (int i=0; i<radii.length; ++i)
			gc.getGradient(unitVector, radii[i],	attributeIndex,
					            layerId, reciprocal, g[i]);
		model.returnGradientCalculator(gc);
  }
	
	@Override
	protected void addToGradient(int attributeIndex, int nodeIndex,
                               double coefficient, double[] gradient)
  {
    double[] gai  = gradients[attributeIndex][nodeIndex];
    gradient[0] += coefficient * gai[0];
    gradient[1] += coefficient * gai[1];
    gradient[2] += coefficient * gai[2];
  }

	@Override
	protected void addToGradient(int attributeIndex, double radius,
			                         double coefficient, double[] gradient)
	{
		if (radius <= radii[0])
		{
			double[] gai  = gradients[attributeIndex][0];
			gradient[0] += coefficient * gai[0];
			gradient[1] += coefficient * gai[1];
			gradient[2] += coefficient * gai[2];
		}
		else if (radius >= radii[radii.length-1])
		{
			double[] gai  = gradients[attributeIndex][radii.length-1];
			gradient[0] += coefficient * gai[0];
			gradient[1] += coefficient * gai[1];
			gradient[2] += coefficient * gai[2];
		}
		else
		{
			//int i = getSubLayerIndex(radius, jlo[0]);
			int i = getRadiusIndex(radius);
			//jlo[0] = i;
			double f = (radius - radii[i]) / (radii[i+1] - radii[i]);
			double[] gai  = gradients[attributeIndex][i];
			double[] gai1 = gradients[attributeIndex][i+1];
			gradient[0] += coefficient * (gai[0] + f * (gai1[0] - gai[0]));
			gradient[1] += coefficient * (gai[1] + f * (gai1[1] - gai[1]));
			gradient[2] += coefficient * (gai[2] + f * (gai1[2] - gai[2]));
		}
	}

	@Override
	protected void getGradient(int attributeIndex, double radius, double[] gradient)
	{
		if (radius <= radii[0])
			getGradientBottom(attributeIndex, gradient);
		else if (radius >= radii[radii.length-1])
			getGradientTop(attributeIndex, gradient);
		else
		{
			int i = getRadiusIndex(radius);
			double f = (radius - radii[i]) / (radii[i+1] - radii[i]);
			double[] gai  = gradients[attributeIndex][i];
			double[] gai1 = gradients[attributeIndex][i+1];
			gradient[0] = gai[0] + f * (gai1[0] - gai[0]);
			gradient[1] = gai[1] + f * (gai1[1] - gai[1]);
			gradient[2] = gai[2] + f * (gai1[2] - gai[2]);
		}
	}

	@Override
	protected void getGradientTop(int attributeIndex, double[] gradient)
	{
		double[] gai  = gradients[attributeIndex][radii.length-1];
		gradient[0] = gai[0];
		gradient[1] = gai[1];
		gradient[2] = gai[2];
	}

	@Override
	protected void getGradientBottom(int attributeIndex, double[] gradient)
	{
		double[] gai = gradients[attributeIndex][0];
		gradient[0] = gai[0];
		gradient[1] = gai[1];
		gradient[2] = gai[2];
	}

	@Override
	protected 	void getGradient(int nodeIndex, int attributeIndex,
			                         double[] gradient)
	{
		double[] gai = gradients[attributeIndex][nodeIndex];
		gradient[0] = gai[0];
		gradient[1] = gai[1];
		gradient[2] = gai[2];
	}

	@Override
	protected double[] getGradient(int nodeIndex, int attributeIndex)
	{
		return gradients[attributeIndex][nodeIndex];
	}

	@Override
  protected boolean isGradientSet(int attributeIndex)
  {
    return ((gradients == null) || (gradients[attributeIndex] == null)) ?
    		   false : true;
  }

	@Override
  protected boolean getGradientReciprocalFlag(int attributeIndex)
  {
		return isGradientSet(attributeIndex) ? gradientReciprocal[attributeIndex] :
			     super.getGradientReciprocalFlag(attributeIndex);
  }

	@Override
  protected void setLayerNormal(double[] layrNormal)
  {
    layerNormal = layrNormal;
  }
  
	@Override
  protected double[] getLayerNormal()
  {
    return layerNormal;
  }
}
