package gms.shared.utilities.geotess;

import static gms.shared.utilities.geotess.util.globals.Globals.NL;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Writer;
import java.util.Scanner;

/**
 * A Profile defined by two radii and a single Data object.
 * 
 * @author Sandy Ballard
 * 
 */
public class ProfileConstant extends Profile
{
	private float radiusBottom, radiusTop;

	private Data data;

	private double[] layerNormal = null;

	/**
	 * The gradients of the GeoAttributes.  Constant with respect to radius.
	 * nAttributes x 3.
	 */
	protected double[][] gradients;
	
	/**
	 * The reciprocal flag for each stored gradient. If true and the gradient
	 * for attribute i is defined (gradients[i] != null), then the stored
	 * gradient is reciprocal for the defined attribute (e.g. the Reciprocal of
	 * SSLOWNESS is the S wave velocity).
	 */
  protected boolean[] gradientReciprocal;
  
	private int pointIndex = -1;

	/**
	 * Parameterized constructor that takes two radius values and one Data
	 * object.  
	 * This object keeps a reference to the supplied Data object (no copy is made).
	 * 
	 * @param radiusBottom
	 * @param radiusTop
	 * @param data
	 * @throws GeoTessException
	 */
	public ProfileConstant(float radiusBottom, float radiusTop, Data data) throws GeoTessException
	{
		if (radiusTop < radiusBottom)
			throw new GeoTessException(String.format("%nradiusTop %1.3f must be > radiusBottom %1.3f%n",
					radiusTop, radiusBottom));
		this.radiusBottom = radiusBottom;
		this.radiusTop = radiusTop;
		this.data = data;
	}

	/**
	 * Constructor that loads required information from an ascii file.
	 * 
	 * @param input
	 * @param metaData
	 * @throws GeoTessException
	 */
	protected ProfileConstant(Scanner input, GeoTessMetaData metaData)
			throws GeoTessException, IOException
			{
		this(input.nextFloat(), input.nextFloat(), Data
				.getData(input, metaData));
			}

	//	/**
	//	 * Constructor that loads required information from netcdf Iterator objects.
	//	 * 
	//	 * @param nPoints
	//	 * @param itRadii
	//	 * @param itValues
	//	 * @param metaData
	//	 */
	//	protected ProfileConstant(IndexIterator nPoints, IndexIterator itRadii,
	//			IndexIterator itValues, GeoTessMetaData metaData)
	//			throws GeoTessException
	//	{
	//		this(itRadii.getFloatNext(), itRadii.getFloatNext(), Data.getData(
	//				itValues, metaData));
	//	}

	/**
	 * Constructor that loads required information from a binary file.
	 * 
	 * @param input
	 * @param metaData
	 * @throws GeoTessException
	 * @throws IOException
	 */
	protected ProfileConstant(DataInputStream input, GeoTessMetaData metaData)
			throws GeoTessException, IOException
			{
		this(input.readFloat(), input.readFloat(), Data
				.getData(input, metaData));
			}

	@Override
	protected void write(DataOutputStream output) throws IOException
	{
		output.writeByte((byte) getType().ordinal());
		output.writeFloat(radiusBottom);
		output.writeFloat(radiusTop);
		data.write(output);
	}

	@Override
	protected void write(Writer output) throws IOException
	{
		output.write(String.format("%d %s %s %s%n", getType().ordinal(),
				Float.toString(radiusBottom), Float.toString(radiusTop), data));
	}

	//	@Override
	//	protected void write(IndexIterator nPoints, IndexIterator radii,
	//			IndexIterator values)
	//	{
	//		radii.setFloatNext(radiusBottom);
	//		radii.setFloatNext(radiusTop);
	//		data.write(values);
	//	}

	@Override
	public ProfileType getType()
	{
		return ProfileType.CONSTANT;
	}

	@Override
	public boolean equals(Object other)
	{
		if (other == null || !(other instanceof ProfileConstant))
			return false;

		return this.radiusBottom == ((ProfileConstant)other).getRadiusBottom()
				&& this.radiusTop == ((ProfileConstant)other).getRadiusTop()
				&& this.data.equals(((ProfileConstant)other).getData(0));
	}

	@Override
	public double getRadius(int node)
	{
		return node == 0 ? radiusBottom : radiusTop;
	}

	@Override
	public void setRadius(int node, float radius) {
		if (node == 0) radiusBottom = radius;
		else if (node == 1) radiusTop = radius;
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
		return nodeIndex <= 1 ? data.getDouble(attributeIndex)
				: Double.NaN;
	}

	/**
	 * Retrieve the value of the specified attribute at the top 
	 * of the layer.
	 * @param attributeIndex
	 * @return the value of the specified attribute at the top 
	 * of the layer.
	 */
	public double getValueTop(int attributeIndex)
	{ return data.getDouble(attributeIndex); }
	
	/**
	 * Return true if the specified Data value is NaN.  
	 * For doubles and floats, this means not NaN.
	 * For bytes, shorts, ints and longs, always returns false
	 * since there is no value that is NaN
	 * @param nodeIndex
	 * @param attributeIndex
	 * @return true if the specified Data value is valid.  
	 */
	public boolean isNaN(int nodeIndex, int attributeIndex)
	{
		return nodeIndex == 0 ? data.isNaN(attributeIndex) : true;
	}

	@Override
	public Data[] getData()
	{
		return new Data[] { data };
	}

	@Override
	public void setData(Data... data)
	{
		this.data = data[0];
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
		if (index == 0)
			this.data = data;
		else 
			throw new ArrayIndexOutOfBoundsException();
	}

	@Override
	public Data getData(int node)
	{
		return data;
	}

	@Override
	public double getRadiusTop()
	{
		return radiusTop;
	}

	@Override
	public Data getDataTop()
	{
		return data;
	}

	@Override
	public double getRadiusBottom()
	{
		return radiusBottom;
	}

	@Override
	public Data getDataBottom()
	{
		return data;
	}

	@Override
	public int getNRadii()
	{
		return 2;
	}

	@Override
	public int getNData()
	{
		return 1;
	}

	@Override
	public float[] getRadii()
	{
		return new float[] { radiusBottom, radiusTop };
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
		return Math.abs(radiusTop - radius) < Math.abs(radiusBottom - radius) ? 1
				: 0;
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
		this.pointIndex = pointIndex;
	}

	@Override
	public void resetPointIndices()
	{ this.pointIndex = -1; }

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
		if (nodeIndex == 0)
			return pointIndex;
		throw new ArrayIndexOutOfBoundsException();
	}

	/**
	 * Outputs this Profile as a formatted string.
	 */
	@Override
	public String toString()
	{
		StringBuffer buf = new StringBuffer();
    buf.append("  Type: " + getType().name() + NL);
    buf.append("    Radii:" + NL + "      Bottom: " +
               String.format("%9.4f", radiusBottom) + NL +
               "      Top:    " +
               String.format("%9.4f", radiusTop) + NL);
    buf.append("    Point Index: " + pointIndex + NL);
    buf.append("    Data: " + data.toString() + NL);
    buf.append("    Layer Normal: " + vectorString(layerNormal) + NL);
    if (gradients == null)
      buf.append("    Gradients: [undefined]" + NL);
    else
    {
      buf.append("    Gradients: ");
    	for (int i = 0; i < gradients.length; ++ i)
    	{
        buf.append(gradientString(gradients[i], gradientReciprocal[i]));
    		if (i < gradients.length-1) buf.append(", ");
    	}
      buf.append(NL);
    }
	  return buf.toString();
	}

	/**
	 * Returns an independent deep copy of this profile.
	 */
	@Override
	public Profile copy() throws GeoTessException
	{
		ProfileConstant pc = new ProfileConstant(radiusBottom, radiusTop,
				                                     data.copy());
		pc.pointIndex = pointIndex;

		if (layerNormal != null)
			pc.layerNormal = layerNormal.clone();
		
		if (gradients != null)
		{
			pc.gradientReciprocal = gradientReciprocal.clone();
			pc.gradients = gradients.clone();
			for (int i=0; i < gradients.length; ++i)
			{
				if (gradients[i] != null)
					pc.gradients[i] = gradients[i].clone();
			}
		}
		return pc;
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
		return reciprocal ? (radiusTop-radiusBottom)/data.getDouble(attributeIndex) 
				: (radiusTop-radiusBottom)*data.getDouble(attributeIndex);
	}
	
	/**
	 * Synchronized method to compute the gradient of a constant layer. Does
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
			gradients = new double[data.size()][];
			gradientReciprocal = new boolean [data.size()];
		}

		// create requested attribute entry if null or the reciprocal flag has
		// changed ... otherwise exit
		if ((gradients[attributeIndex] == null) ||
		    (gradientReciprocal[attributeIndex] != reciprocal))
		{
			gradients[attributeIndex] = new double [3];
			gradientReciprocal[attributeIndex] = reciprocal;
		}
		else
			return;

		// get attribute gradient reference and unit vector for point to be evaluated
		double[] g = gradients[attributeIndex];

		// retrieve gradient calculator, get average radius and compute gradient
		GradientCalculator gc = model.getGradientCalculator();
		double radius = 0.5 * (radiusBottom + radiusTop);
	  gc.getGradient(unitVector, radius,	attributeIndex,
	  		            layerId, reciprocal, g);
		model.returnGradientCalculator(gc);
  }
	
	@Override
	protected void addToGradient(int attributeIndex, int nodeIndex,
                               double coefficient, double[] gradient)
  {
    double[] gai  = gradients[attributeIndex];
    gradient[0] += coefficient * gai[0];
    gradient[1] += coefficient * gai[1];
    gradient[2] += coefficient * gai[2];
  }

	@Override
	protected void addToGradient(int attributeIndex, double radius,
			                         double coefficient, double[] gradient)
	{
		double[] g = gradients[attributeIndex];
		gradient[0] += coefficient * g[0];
		gradient[1] += coefficient * g[1];
		gradient[2] += coefficient * g[2];
	}

	@Override
	protected void getGradient(int attributeIndex, double radius, double[] gradient)
	{
		double[] g = gradients[attributeIndex];
		gradient[0] = g[0];
		gradient[1] = g[1];
		gradient[2] = g[2];
	}

	@Override
	protected void getGradientBottom(int attributeIndex, double[] gradient)
	{
		double[] g = gradients[attributeIndex];
		gradient[0] = g[0];
		gradient[1] = g[1];
		gradient[2] = g[2];
	}

	@Override
	protected void getGradientTop(int attributeIndex, double[] gradient)
	{
		double[] g = gradients[attributeIndex];
		gradient[0] = g[0];
		gradient[1] = g[1];
		gradient[2] = g[2];
	}

	@Override
	protected 	void getGradient(int nodeIndex, int attributeIndex, double[] gradient)
	{
		double[] gai = gradients[attributeIndex];
		gradient[0] = gai[0];
		gradient[1] = gai[1];
		gradient[2] = gai[2];
	}
	
	@Override
	protected double[] getGradient(int nodeIndex, int attributeIndex)
	{
		return gradients[attributeIndex];
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
