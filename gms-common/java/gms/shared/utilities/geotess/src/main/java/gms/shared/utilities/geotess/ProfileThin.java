package gms.shared.utilities.geotess;

import static gms.shared.utilities.geotess.util.globals.Globals.NL;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Writer;
import java.util.Scanner;

//import ucar.ma2.IndexIterator;

/**
 * A Profile defined by a single radius and a single Data object.
 * 
 * @author Sandy Ballard
 */
public class ProfileThin extends Profile
{

	private float radius;

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
	 * Parameterized constructor that takes one radius value and one Data object
	 * This object keeps a reference to the supplied Data object (no copy is made).
	 * 
	 * @param radius
	 * @param data
	 * @throws GeoTessException
	 */
	public ProfileThin(float radius, Data data) throws GeoTessException
	{
		this.radius = radius;
		this.data = data;
	}

	/**
	 * Constructor that loads required information from an ascii file.
	 * 
	 * @param input
	 * @param metaData
	 * @throws GeoTessException
	 */
	protected ProfileThin(Scanner input, GeoTessMetaData metaData)
			throws GeoTessException, IOException
	{
		radius = input.nextFloat();
		data = Data.getData(input, metaData);
	}

//	/**
//	 * Constructor that loads required information from netcdf Iterator objects.
//	 * 
//	 * @param itRadii
//	 * @param itValues
//	 * @param metaData
//	 * @throws GeoTessException
//	 */
//	protected ProfileThin(IndexIterator itRadii, IndexIterator itValues,
//			GeoTessMetaData metaData) throws GeoTessException
//	{
//		radius = itRadii.getFloatNext();
//		data = Data.getData(itValues, metaData);
//	}

	/**
	 * Constructor that loads required information from a binary file.
	 * 
	 * @param input
	 * @param metaData
	 * @throws GeoTessException
	 * @throws IOException
	 */
	protected ProfileThin(DataInputStream input, GeoTessMetaData metaData)
			throws GeoTessException, IOException
	{
		radius = input.readFloat();
		data = Data.getData(input, metaData);
	}

	@Override
	protected void write(Writer output) throws IOException
	{
		output.write(String.format("%d %s %s%n", getType().ordinal(),
				Float.toString(radius), data.toString()));
	}

	@Override
	protected void write(DataOutputStream output) throws IOException
	{
		output.writeByte((byte) getType().ordinal());
		output.writeFloat(radius);
		data.write(output);
	}

//	@Override
//	protected void write(IndexIterator nPoints, IndexIterator radii,
//			IndexIterator values)
//	{
//		radii.setFloatNext(radius);
//		data.write(values);
//	}

	@Override
	public ProfileType getType()
	{
		return ProfileType.THIN;
	}

	@Override
	public boolean equals(Object other)
	{
		if (other == null || !(other instanceof ProfileThin))
			return false;
		
		return this.radius == ((ProfileThin)other).radius
				&& this.data.equals(((ProfileThin)other).getData(0));
		
	}

	@Override
	public double getRadius(int node)
	{
		return radius;
	}

	@Override
	public void setRadius(int node, float radius)  { this.radius = radius; }
	
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
		return nodeIndex == 0 ? data.getDouble(attributeIndex)
				: Double.NaN;
	}

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

	/**
	 * Retrieve the value of the specified attribute at the top 
	 * of the layer.
	 * @param attributeIndex
	 * @return the value of the specified attribute at the top 
	 * of the layer.
	 */
	public double getValueTop(int attributeIndex)
	{ return data.getDouble(attributeIndex); }
	
	@Override
	public Data[] getData()
	{
		return new Data[] { data };
	}

	@Override
	public Data getData(int node)
	{
		return data;
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
	public double getRadiusTop()
	{
		return radius;
	}

	@Override
	public Data getDataTop()
	{
		return data;
	}

	@Override
	public double getRadiusBottom()
	{
		return radius;
	}

	@Override
	public Data getDataBottom()
	{
		return data;
	}

	@Override
	public int getNRadii()
	{
		return 1;
	}

	@Override
	public int getNData()
	{
		return 1;
	}

	@Override
	public float[] getRadii()
	{
		return new float[] { radius };
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
		return 0;
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
    buf.append("    Radius:" +
               String.format("%9.4f", radius) + NL);
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
		ProfileThin pt = new ProfileThin(radius, data.copy());
		pt.pointIndex = pointIndex;
		
		if (layerNormal != null)
			pt.layerNormal = layerNormal.clone();
		
		if (gradients != null)
		{
			pt.gradientReciprocal = gradientReciprocal.clone();
			pt.gradients = gradients.clone();
			for (int i=0; i < gradients.length; ++i)
			{
				if (gradients[i] != null)
					pt.gradients[i] = gradients[i].clone();
			}
		}
		return pt;
	}
	
	/**
	 * Synchronized method to compute the gradient of a thin layer. Does
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
			gradients = new double[data.size()][3];
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

		// retrieve gradient calculator and compute gradient
		GradientCalculator gc = model.getGradientCalculator();
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
	protected 	void getGradient(int nodeIndex, int attributeIndex,
			                         double[] gradient)
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
