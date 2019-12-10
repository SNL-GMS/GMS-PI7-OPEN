package gms.shared.utilities.geotess;

import static java.lang.Math.max;
import gms.shared.utilities.geotess.util.containers.arraylist.ArrayListDouble;
import gms.shared.utilities.geotess.util.containers.arraylist.ArrayListInt;
import gms.shared.utilities.geotess.util.containers.hash.maps.HashMapIntegerDouble;
import gms.shared.utilities.geotess.util.globals.InterpolatorType;
import gms.shared.utilities.geotess.util.numerical.vector.EarthShape;
import gms.shared.utilities.geotess.util.numerical.vector.Vector3D;
import gms.shared.utilities.geotess.util.numerical.vector.VectorUnit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages information about a single point at an arbitrary position in a
 * GeoTessModel. It supports interpolation of Data at the position it
 * represents, using either linear or higher order interpolation. When
 * interpolating data, the data in a number of surrounding Profiles are first
 * interpolated in the radial dimension at each of the vertices. Then those
 * values are interpolated in the geographic dimensions. Both linear and higher
 * order interpolation schemes are supported. With linear interpolation the
 * vertices of the triangle containing the position and the 3 interpolation
 * coefficients calculated during the triangle walking algorithm are used to
 * interpolate data. For higher order interpolation, cubic splines are used to
 * interpolate data along radial profies and then the Natural Neighbor
 * interpolation algorithm (Sibson, 1980, 19821) is implemented to interpolate
 * data in the geographic dimensions.
 * 
 * <p>
 * GeoTessPosition is not thread-safe in that it's internal state is mutable.
 * The design intention is that single instances of a GeoTessGrid object and
 * GeoTessData object can be shared among all the threads in a multi-threaded
 * application and each thread will have it's own instance of a GeoTessPosition
 * object that references the common GeoTessGrid + GeoTessData combination.
 * 
 * <p>
 * Sibson, R., (1980) A Vector Identity for the Dirichlet Tessellation, Proc.
 * Cambridge Philosophical Society, 87, 151-155.
 * 
 * <p>
 * Sibson, R. (1981).
 * "A brief description of natural neighbor interpolation (Chapter 2)". In V.
 * Barnett. Interpreting Multivariate Data. Chichester: John Wiley. pp. 21-36.
 * 
 * @author Sandy Ballard
 * 
 */
public abstract class GeoTessPosition
{
	private static int nextIndex;
	public final int classCount;

	/**
	 * A user specified layer index with which this GeoTessPosition is associated.
	 */
	private int index = -1;

	/**
	 * The earth-centered unit vector that corresponds to current position.
	 */
	protected double[] unitVector;

	/**
	 * Radius of current position, in km.
	 */
	protected double radius;

	/**
	 * The earth-centered unit vector that corresponds to current position.
	 */
	protected double[] threeDVector;

	/**
	 * Radius of the earth's ellipsoid at the current position.
	 */
	protected double earthRadius;

	/**
	 * An array of length nLayers+1, where layerRadii[i] is the radius of the
	 * bottom of layer i. The last (extra) element is the radius of the top of
	 * the last layer.
	 * <p>
	 * Lazy evaluation is used here. Values are set to -1 whenever the
	 * geographic position is modified and then computed once when a value is
	 * requested.
	 */
	protected ArrayListDouble layerRadii;

	/**
	 * Index of the current layer
	 */
	protected int layerId;

	/**
	 * Used to track the last layer index used to update the radial interpolation
	 * coefficients.
	 */
	private int radialCoeffUpdateLayerId;
	
	/**
	 * The index of the tessellation of the current position.
	 */
	protected int tessid;

	/**
	 * tessellation level, relative to the first tessellation level in the
	 * current tessellation. Usually the top level of the current tessellation,
	 * but not if maxTessLevel is smaller. There is a separate tessLevel stored
	 * for each tessellation in the model.
	 */
	private int tessLevels[];

	/**
	 * Maximum tessellation level, relative to the first tessellation level in
	 * the current tessellation. The walking triangle algorithm will search no
	 * higher than this level. Defaults to Integer.MAX_VALUE but applications
	 * can set this to something less than the largest tessellation level to
	 * limit the search.
	 */
	private int[] maxTessLevel;

	/**
	 * The index of the triangles in which current position resides. There is a
	 * separate triangle for each tessellation in the model. This variable is
	 * used to flag which tessellations are currently up-to-date and which need
	 * to be updated.
	 */
	private int[] triangle;

	/**
	 * The interpolation coefficients that apply to the containing triangles.
	 * nTessellations x 3. For each tessellation, the 3 elements are normalized
	 * to 1.
	 */
	protected ArrayList<ArrayListDouble> linearCoefficients;

	/**
	 * The indexes of vertices in the 2D grid that will be involved in the
	 * interpolation of data. There is a separate set of vertices for each
	 * tessellation in the model. When linear interpolation is used each
	 * ArrayListInt will generally have 3 vertex indexes, corresponding to the 
	 * indices of the corners of the enclosing triangle.  If the 
	 * interpolation point corresponds almost exactly with a vertex, then
	 * the list will only contain one element.  When higher order 
	 * interpolation is used (natural neighbor), then each list may
	 * have more than 3 vertex indices.
	 */
	protected ArrayList<ArrayListInt> vertices;

	/**
	 * The interpolation coefficients that will be involved in the interpolation
	 * of data. hcoefficients and vertices must be the same size. The values
	 * stored in hcoefficients[i] must be normalized such that they sum to 1.
	 */
	protected ArrayList<ArrayListDouble> hCoefficients;
	
	/**
	 * An nVertices x nNodes array where nVertices is the number of vertices
	 * involved in horizontal interpolation and nRadii is the number of nodes
	 * in the current Profile that are involved in the radial interpolation.
	 * Each element is the index of the node in the Profile that should be
	 * queried to find a data value.
	 */
	protected ArrayList<ArrayListInt> radialIndexes;

	/**
	 * An nVertices x nNodes array where nVertices is the number of vertices
	 * involved in horizontal interpolation and nRadii is the number of nodes
	 * in the current Profile that are involved in the radial interpolation.
	 * Each element is the radial interpolation coefficient that should be
	 * applied to the nodes in the current Profile.  Note that these 
	 * coefficients sum to one in each Profile.  They must be multiplied
	 * by the horizontal interpolation coefficient for the vertex to get
	 * the total weight of each node.
	 */
	protected ArrayList<ArrayListDouble> radialCoefficients;
	
	protected InterpolatorType radialInterpolatorType;

	/**
	 * A reference to the 3D model that holds the model information that this
	 * position object will interrogate.
	 */
	protected GeoTessModel model;

	/**
	 * A reference to the GeoTessGrid.
	 */
	protected GeoTessGrid grid;

	/**
	 * Get references to grid objects that will be used a lot in the triangle
	 * walking algorithm.
	 */
	protected Profile[][] profiles;
	protected double[][] gridVertices;
	protected int[][] gridTriangles;
	protected Edge[][] gridEdges;
	protected int[] gridDescendants;
	protected int[] layerTessIds;
	protected int nLayers;

	/**
	 * This is the value returned when an invalid interpolation result is
	 * obtained.
	 */
	private double errorValue = Double.NaN;
	
	/**
	 * If this is true, and a radius is specified that is below bottom layer or
	 * above top layer, then values at the bottom of bottom layer or top of top
	 * layer are returned. If false, errorValue is returned.
	 */
	protected boolean radiusOutOfRangeAllowed;

	/**
	 * Static factory method that returns a GeoTessPosition object that
	 * uses linear interpolatorin in both the horizontal and radial dimensions.
	 * 
	 * @param model
	 * @return a GeoTessPosition object.
	 * @throws GeoTessException
	 */
	public static GeoTessPosition getGeoTessPosition(GeoTessModel model) throws GeoTessException
	{
		return getGeoTessPosition(model, InterpolatorType.LINEAR, InterpolatorType.LINEAR );
	}

	/**
	 * Static factory method that returns the desired type of GeoTessPosition
	 * object.  If the horizontal InterpolationType is LINEAR, then the radial
	 * interpolation type will be linear as well.  If the horizontal InterpolationType
	 * is NATURAL_NEIGHBOR, then the radial interpolation type will be CUBIC_SPLINE.
	 * 
	 * @param model
	 *            the GeoTessModel from which values will be extracted
	 * @param horizontalType either InterpolatorType.LINEAR or InterpolatorType.NATURAL_NEIGHBOR
	 * @return a GeoTessPosition object.
	 * @throws GeoTessException
	 */
	public static GeoTessPosition getGeoTessPosition(GeoTessModel model,
			InterpolatorType horizontalType) throws GeoTessException
	{
		return getGeoTessPosition(model, horizontalType, 
				horizontalType == InterpolatorType.LINEAR ? InterpolatorType.LINEAR 
						: InterpolatorType.CUBIC_SPLINE);
	}

	/**
	 * Static factory method that returns the desired type of GeoTessPosition
	 * object.
	 * 
	 * @param model
	 *            the GeoTessModel from which values will be extracted
	 * @param horizontalType either InterpolatorType.LINEAR or InterpolatorType.NATURAL_NEIGHBOR
	 * @param radialType either InterpolatorType.LINEAR or InterpolatorType.CUBIC_SPLINE
	 * @return a GeoTessPosition object.
	 * @throws GeoTessException
	 */
	public static GeoTessPosition getGeoTessPosition(GeoTessModel model,
			InterpolatorType horizontalType, InterpolatorType radialType) throws GeoTessException
	{
		switch (horizontalType)
		{
		case LINEAR:
			return new GeoTessPositionLinear(model, radialType);
		case NATURAL_NEIGHBOR:
			return new GeoTessPositionNaturalNeighbor(model, radialType);
		default:
			throw new GeoTessException(horizontalType + " is not supported.  \n"
					+ "Must specify either InterpolatorType.LINEAR, \n"
					+ "or InterpolatorType.NATURAL_NEIGHBOR\n");
		}
	}

	/**
	 * Static factory method that returns a new GeoTessPosition of the same
	 * horizontal and radial interpolation types using the same GeoTessModel.
	 * Note that the position of the new interpolator is not set and must be
	 * set by the caller.
	 * 
	 * @param gtp The GeoTessPosition object for which a new one is created of
	 *            the same interpolation types and using the same GeoTessModel.
	 * @return A new GeoTessPosition of the same horizontal and radial
	 *         interpolation types using the same GeoTessModel.
	 * @throws GeoTessException
	 */
	public static GeoTessPosition getGeoTessPosition(GeoTessPosition gtp) throws GeoTessException
	{
		return getGeoTessPosition(gtp.model, gtp.getInterpolatorType(), gtp.radialInterpolatorType);
	}

	/**
	 * Protected constructor that takes a reference to the 3D model that is to
	 * be interrogated by this GeoTessPosition3D object. Applications should
	 * obtain a GeoTessPosition object by calling
	 * GeoTessModel.getGeoTessPosition(InterpolatorType)
	 * 
	 * @param model
	 * @param radialType 
	 * @throws GeoTessException
	 */
	protected GeoTessPosition(GeoTessModel model, InterpolatorType radialType) throws GeoTessException
	{
		classCount = nextIndex++;

		this.model = model;
		this.radialInterpolatorType = radialType;
		layerTessIds = model.getMetaData().getLayerTessIds();
		nLayers = model.getMetaData().getNLayers();

		layerRadii = new ArrayListDouble(nLayers + 1);
		for (int i = 0; i <= nLayers; ++i)
			layerRadii.add(-1);

		profiles = model.getProfiles();

		this.grid = model.getGrid();
		gridVertices = grid.getVertices();
		gridTriangles = grid.getTriangles();
		gridDescendants = grid.getDescendants();
		gridEdges = grid.getEdgeList();

		radiusOutOfRangeAllowed = true;
		radialIndexes = new ArrayList<ArrayListInt>(8);
		radialCoefficients = new ArrayList<ArrayListDouble>(8);
		
		unitVector = new double[3];
		threeDVector = new double[3];
		radius = -1.;

		tessid = -1;

		int ntess = grid.getNTessellations();

		this.triangle = new int[ntess];
		Arrays.fill(this.triangle, -1);
		this.tessLevels = new int[ntess];

		this.maxTessLevel = new int[ntess];
		Arrays.fill(maxTessLevel, Integer.MAX_VALUE-1);

		double[] temp = new double[3];
		linearCoefficients = new ArrayList<ArrayListDouble>(ntess);
		for (int i = 0; i < ntess; ++i)
			linearCoefficients.add(new ArrayListDouble(temp));

	}

	public abstract GeoTessPosition deepClone() throws GeoTessException;

	public abstract void copy(GeoTessPosition gtp);

	/**
	 * Perform a deep copy of the contents of gtp into this. This method is
	 * called by deepCopy and copy() methods of the derived type to copy the
	 * GeoTessPosition fields of the input (gtp) into this GeoTessPosition.
	 * 
	 * @param gtp The GeoTessPosition to be copied into this GeoTessPosition
	 */
   protected void setCopy(GeoTessPosition gtp)
   {
  	 index = gtp.index;
  	 earthRadius = gtp.earthRadius;
  	 errorValue = gtp.errorValue;

  	 model = gtp.model;
  	 profiles = gtp.profiles;

  	 grid = gtp.grid;
  	 gridDescendants = gtp.gridDescendants;
  	 gridEdges = gtp.gridEdges;
  	 gridTriangles = gtp.gridTriangles;
  	 gridVertices = gtp.gridVertices;
  	 layerTessIds = gtp.layerTessIds;
  	 nLayers = gtp.nLayers;

  	 layerId = gtp.layerId;
  	 radialCoeffUpdateLayerId = gtp.radialCoeffUpdateLayerId;
  	 
  	 tessid = gtp.tessid;
  	 layerRadii = (ArrayListDouble) gtp.layerRadii.clone();
  	 maxTessLevel = gtp.maxTessLevel.clone();

  	 radialIndexes = new ArrayList<ArrayListInt>(Math.max(8, gtp.radialIndexes.size()));
  	 for (int i = 0; i < gtp.radialIndexes.size(); ++i)
  		 radialIndexes.add((ArrayListInt) gtp.radialIndexes.get(i).clone());

  	 radialCoefficients = new ArrayList<ArrayListDouble>(Math.max(8, gtp.radialCoefficients.size()));
  	 for (int i = 0; i < gtp.radialCoefficients.size(); ++i)
  		 radialCoefficients.add((ArrayListDouble) gtp.radialCoefficients.get(i).clone());
  	 
  	 linearCoefficients = new ArrayList<ArrayListDouble>(gtp.linearCoefficients.size());
  	 for (int i = 0; i < gtp.linearCoefficients.size(); ++i)
  		 linearCoefficients.add((ArrayListDouble) gtp.linearCoefficients.get(i).clone());

  	 hCoefficients = new ArrayList<ArrayListDouble>(gtp.hCoefficients.size());
  	 for (int i = 0; i < gtp.hCoefficients.size(); ++i)
  		 hCoefficients.add((ArrayListDouble) gtp.hCoefficients.get(i).clone());

  	 vertices = new ArrayList<ArrayListInt>(gtp.vertices.size());
  	 for (int i = 0; i < gtp.vertices.size(); ++i)
  		 vertices.add((ArrayListInt) gtp.vertices.get(i).clone());

  	 triangle = gtp.triangle.clone();
  	 
  	 radialInterpolatorType = gtp.radialInterpolatorType;
  	 radiusOutOfRangeAllowed = gtp.radiusOutOfRangeAllowed;
  	 tessLevels = gtp.tessLevels.clone();
  	 
  	 radius = gtp.radius;
  	 threeDVector = gtp.threeDVector.clone();
  	 unitVector = gtp.unitVector.clone();
   }
 
	/**
	 * Update the 2D vertices and horizontal interpolation coefficients.
	 * Different types of interpolators will handle this differently.
	 * 
	 * @throws GeoTessException
	 */
	abstract protected void update2D(int tessid) throws GeoTessException;

	/**
	 * Retrieve the type of interpolation that this GeoTessPosition object is
	 * configured to perform. Either InterpolatorType.LINEAR or
	 * InterpolatorType.NATURAL_NEIGHBOR
	 * 
	 * @return the type of interpolation that this GeoTessPosition object is
	 *         configured to perform. Either InterpolatorType.LINEAR or
	 *         InterpolatorType.NATURAL_NEIGHBOR
	 */
	abstract public InterpolatorType getInterpolatorType();

	/**
	 * Retrieve the type of interpolation that this GeoTessPosition object is
	 * configured to perform in the radial dimension. Either InterpolatorType.LINEAR or
	 * InterpolatorType.CUBIC_SPLINE
	 * 
	 * @return the type of interpolation that this GeoTessPosition object is
	 *         configured to perform in the radial dimension. Either InterpolatorType.LINEAR or
	 *         InterpolatorType.CUBIC_SPLINE
	 */
	public InterpolatorType getInterpolatorTypeRadial()
	{
		return radialInterpolatorType;
	}
	
	/**
	 * Retrieve a Data object populated with values interpolated at the current position.
	 * @return a Data object populated with values interpolated at the current position.
	 * @throws GeoTessException
	 */
	public Data getData() throws GeoTessException
	{
		Data data = Data.getData(model.getMetaData().getDataType(), model.getMetaData().getNAttributes());
		for (int i=0; i<data.size(); ++i)
			data.setValue(i, getValue(i));
		return data;
	}
	
	/**
	 * Retrieve an interpolated gradient for the specified model attribute. If
	 * the gradient has not been pre-computed for any profiles contributing to
	 * the interpolation result, then they are evaluated here using the reciprocal
	 * flag setting. If they were pre-computed then the reciprocal flag is ignored.
	 * 
	 * @param attribute The attribute index for which the gradient is interpolated.
	 * @param reciprocal The reciprocal flag used to evaluate any undefined gradients
	 *                   for profiles used in the interpolation.
	 * @param gradient   The interpolated gradient is written here.
	 * @return The interpolated gradient of the specified model attribute.
	 * @throws GeoTessException
	 */
	public void getGradient(int attribute, boolean reciprocal, double[] gradient)
			   throws GeoTessException
	{
		gradient[0] = gradient[1] = gradient[2] = 0.;

		// exit with a zero gradient if the layer thickness is negligible.
		if (getLayerThickness() < 1e-9) return;
		
		if (radialInterpolatorType == InterpolatorType.CUBIC_SPLINE)
			throw new GeoTessException("\nCannot compute radial coefficients for InterpolatorType.CUBIC_SPLINE");

		// get the contributing vertices and their horizontal interpolation
		// coefficients
		int[] v = vertices.get(tessid).getArray();
		double[] h = hCoefficients.get(tessid).getArray();

		// update radial coefficients if necessary and loop over each contributing
		// vertex
		updateRadialCoefficients(layerId, tessid);
		for (int i = 0; i < vertices.get(tessid).size(); ++i)
		{
			// get the profile for the ith vertex at the interpolation layer id.
			// retrieve all radial indexes and their interpolation coefficients
			// that participate in the interpolation.
			Profile p = profiles[v[i]][layerId];
			ArrayListInt    radii =  radialIndexes.get(i);
			ArrayListDouble coeff =  radialCoefficients.get(i);
			
			// compute the gradients if the profile gradient array is not defined
			if (!p.isGradientSet(attribute))
				p.computeGradients(model, attribute, model.getGrid().getVertex(v[i]),
													 layerId, reciprocal);
			
			// loop over each radial contributor and add the gradient contribution
			// to the gradient array
			for (int j = 0; j < radii.size(); ++j)
				p.addToGradient(attribute, radii.get(j), h[i] * coeff.get(j), gradient);
		}
	}
	
	/**
	 * Retrieve an interpolated gradient for the specified model attribute. If
	 * the gradient has not been pre-computed for any profiles contributing to
	 * the interpolation result, then they are evaluated here using the reciprocal
	 * flag setting. If they were pre-computed then the reciprocal flag is ignored.
	 * 
	 * @param attribute The attribute index for which the gradient is interpolated.
	 * @param majorLayerIndex The layer id for which the gradient is set.
	 * @param reciprocal The reciprocal flag used to evaluate any undefined gradients
	 *                   for profiles used in the interpolation.
	 * @param gradient   The interpolated gradient is written here.
	 * @return The interpolated gradient of the specified model attribute.
	 * @throws GeoTessException
	 */
	public void getGradient(int attribute, int majorLayerIndex,
			                    boolean reciprocal, double[] gradient)
			   throws GeoTessException
	{
		gradient[0] = gradient[1] = gradient[2] = 0.;

		// exit with a zero gradient if the layer thickness is negligible.
		if (getLayerThickness(majorLayerIndex) < 1e-9) return;

		//**T
		int tid = layerTessIds[majorLayerIndex];
		checkTessellation(tid);
		
		if (radialInterpolatorType == InterpolatorType.CUBIC_SPLINE)
			throw new GeoTessException("\nCannot compute radial coefficients for InterpolatorType.CUBIC_SPLINE");

		// get the contributing vertices and their horizontal interpolation
		// coefficients
		int[] v = vertices.get(tid).getArray();
		double[] h = hCoefficients.get(tid).getArray();

		// update radial coefficients if necessary and loop over each contributing
		// vertex
		updateRadialCoefficients(majorLayerIndex, tid);
		for (int i = 0; i < vertices.get(tid).size(); ++i)
		{
			// get the profile for the ith vertex at the interpolation layer id.
			// retrieve all radial indexes and their interpolation coefficients
			// that participate in the interpolation.
			Profile p = profiles[v[i]][majorLayerIndex];
			ArrayListInt    radii =  radialIndexes.get(i);
			ArrayListDouble coeff =  radialCoefficients.get(i);
			
			// compute the gradients if the profile gradient array is not defined
			if (!p.isGradientSet(attribute))
				p.computeGradients(model, attribute, model.getGrid().getVertex(v[i]),
													 majorLayerIndex, reciprocal);
			
			// loop over each radial contributor and add the gradient contribution
			// to the gradient array
			for (int j = 0; j < radii.size(); ++j)
				p.addToGradient(attribute, radii.get(j), h[i] * coeff.get(j), gradient);
		}
	}
	
	/**
	 * Retrieve an interpolated value of the specified model attribute.
	 * 
	 * @param attribute
	 * @return interpolated value of the specified model attribute.
	 * @throws GeoTessException
	 */
	public double getValue(int attribute) throws GeoTessException
	{
		int[] v = vertices.get(tessid).getArray();
		double[] h = hCoefficients.get(tessid).getArray();

		double value = 0;
		if (radialInterpolatorType == InterpolatorType.CUBIC_SPLINE)
			for (int i = 0; i < vertices.get(tessid).size(); ++i)
				value += profiles[v[i]][layerId].getValue(radialInterpolatorType, attribute, radius, radiusOutOfRangeAllowed) * h[i];
		else
		{
			updateRadialCoefficients(layerId, tessid);
			for (int i = 0; i < vertices.get(tessid).size(); ++i)
				value += profiles[v[i]][layerId].getValue(radialIndexes.get(i), radialCoefficients.get(i), attribute) * h[i];
		}

		return Double.isNaN(value) ? getErrorValue() : value;
	}
	
	/**
	 * Retrieve an interpolated value of the specified model attribute.
	 * 
	 * @param attribute
	 * @param layer The desired layer for which the value is returned.
	 * @return interpolated value of the specified model attribute.
	 * @throws GeoTessException
	 */
	public double getValue(int attribute, int layer) throws GeoTessException
	{
		//**T
		int tid = layerTessIds[layer];
		checkTessellation(tid);
		
		int[] v = vertices.get(tid).getArray();
		double[] h = hCoefficients.get(tid).getArray();

		double value = 0;
		if (radialInterpolatorType == InterpolatorType.CUBIC_SPLINE)
			for (int i = 0; i < vertices.get(tid).size(); ++i)
				value += profiles[v[i]][layer].getValue(radialInterpolatorType, attribute, radius, radiusOutOfRangeAllowed) * h[i];
		else
		{
			updateRadialCoefficients(layer, tid);
			for (int i = 0; i < vertices.get(tid).size(); ++i)
				value += profiles[v[i]][layer].getValue(radialIndexes.get(i), radialCoefficients.get(i), attribute) * h[i];
		}

		return Double.isNaN(value) ? getErrorValue() : value;
	}

	/**
	 * Returns the interpolated attribute value at the top of input majorLayerIndex.
	 * 
	 * @param attribute       The attribute to be interpolated.
	 * @param majorLayerIndex The major layer index at which the interpolation
	 *                        is performed.
	 * @return The interpolated attribute value at the top of the input majorLayerIndex.
	 * @throws GeoTessException
	 */
	public double getValueTop(int attribute, int majorLayerIndex)
	       throws GeoTessException
	{
		if (getLayerThickness(majorLayerIndex) < 1e-9)
			return 0.;

		//**T
		int tid = layerTessIds[majorLayerIndex];
		checkTessellation(tid);

		int[] v = vertices.get(tid).getArray();
		double[] h = hCoefficients.get(tid).getArray();
		double value = 0;
		for (int i = 0; i < vertices.get(tid).size(); ++i)
		  value += profiles[v[i]][majorLayerIndex].getValueTop(attribute) * h[i];
    return value;		
	}

	/**
	 * Returns the interpolated attribute value at the bottom of input majorLayerIndex.
	 * 
	 * @param attribute       The attribute to be interpolated.
	 * @param majorLayerIndex The major layer index at which the interpolation
	 *                        is performed.
	 * @return The interpolated attribute value at the bottom of input majorLayerIndex.
	 * @throws GeoTessException
	 */
	public double getValueBottom(int attribute, int majorLayerIndex)
	       throws GeoTessException
	{
		if (getLayerThickness(majorLayerIndex) < 1e-9)
			return 0.;

		//**T
		int tid = layerTessIds[majorLayerIndex];
		checkTessellation(tid);

		int[] v = vertices.get(tid).getArray();
		double[] h = hCoefficients.get(tid).getArray();
		double value = 0;
		for (int i = 0; i < vertices.get(tid).size(); ++i)
		  value += profiles[v[i]][majorLayerIndex].getValueBottom(attribute) * h[i];
    return value;		
	}

	/**
	 * Returns the influencing node weights at the current position.
	 * 
	 * @param dkm The step size about the position (km).
	 * @param weights The weight list that associates the active point index with
	 *                its determined weight. Note this map is not cleared.
	 *                Calculated weights are added to any previously existing
	 *                values.
	 * @throws GeoTessException 
	 */
	public void getWeights(double dkm,	HashMap<Integer, Double> weights) throws GeoTessException
	{
		getWeights(weights, dkm, radius, layerId, radialInterpolatorType);
	}

	/**
	 * Returns the influencing node weights at the current position.
	 * 
	 * @param dkm The step size about the position (km).
	 * @param weights The weight list that associates the active point index with
	 *                its determined weight. Note this map is not cleared.
	 *                Calculated weights are added to any previously existing
	 *                values.
	 * @throws GeoTessException 
	 */
	public void getWeights(double dkm,	Map<Integer, Double> weights) throws GeoTessException
	{
		getWeights(weights, dkm, radius, layerId, radialInterpolatorType);
	}

	/**
	 * Returns the influencing node weights at the current lateral position
	 * using the input layer index and radius.
	 * 
	 * @param dkm The step size about the position (km).
	 * @param weights The weight list that associates the active point index with
	 *                its determined weight. Note this map is not cleared.
	 *                Calculated weights are added to any previously existing
	 *                values.
	 * @param radius           The radius at which the interpolation is performed.
	 * @param majorLayerIndex  The major layer index for which the interpolation
	 *                         is performed.
	 * @param radialInterpType The radial interpolation type.
	 * @throws GeoTessException 
	 */
	public void getWeights(HashMap<Integer, Double> weights, double dkm, double radius, int majorLayerIndex, InterpolatorType radialInterpType) throws GeoTessException
	{
		//**T
		int tid = layerTessIds[majorLayerIndex];
		checkTessellation(tid);

		int[] v = vertices.get(tid).getArray();
		double[] h = hCoefficients.get(tid).getArray();

		for (int i = 0; i < vertices.get(tid).size(); ++i)
			profiles[v[i]][majorLayerIndex].getWeights(weights, dkm, radius, h[i], radialInterpType);
	}

	/**
	 * Returns the influencing node weights at the current lateral position
	 * using the input layer index and radius.
	 * 
	 * @param dkm The step size about the position (km).
	 * @param weights The weight list that associates the active point index with
	 *                its determined weight. Note this map is not cleared.
	 *                Calculated weights are added to any previously existing
	 *                values.
	 * @param radius           The radius at which the interpolation is performed.
	 * @param majorLayerIndex  The major layer index for which the interpolation
	 *                         is performed.
	 * @param radialInterpType The radial interpolation type.
	 * @throws GeoTessException 
	 */
	public void getWeights(Map<Integer, Double> weights, double dkm, double radius, int majorLayerIndex, InterpolatorType radialInterpType) throws GeoTessException
	{
		//**T
		int tid = layerTessIds[majorLayerIndex];
		checkTessellation(tid);

		int[] v = vertices.get(tid).getArray();
		double[] h = hCoefficients.get(tid).getArray();

		for (int i = 0; i < vertices.get(tid).size(); ++i)
			profiles[v[i]][majorLayerIndex].getWeights(weights, dkm, radius, h[i], radialInterpType);
	}

	/**
	 * Creates an outward pointing normal vector at the top of the current layer
	 * for the current lateral position of this GeoTessPosition and returns the
	 * result.
	 * 
	 * @return The outward pointing normal vector at the top of the current layer
	 *         for the current lateral position of this GeoTessPosition. 
	 * @throws GeoTessException 
	 */
	public double[] getLayerNormal() throws GeoTessException
	{
		double[] normal = new double [3];
		getLayerNormal(normal);
		return normal;
	}

	/**
	 * Creates an outward pointing normal vector at the top of the input layer
	 * for the current lateral position of this GeoTessPosition and returns the
	 * result.
	 * 
	 * @param  layr The index of the layer for which the normal will be returned.
	 * @return The outward pointing normal vector at the top of the input layer
	 *         for the current lateral position of this GeoTessPosition. 
	 * @throws GeoTessException 
	 */
	public double[] getLayerNormal(int layr) throws GeoTessException
	{
		double[] normal = new double [3];
		getLayerNormal(layr, normal);
		return normal;
	}

	/**
	 * Creates an outward pointing normal vector at the top of the current layer
	 * for the current lateral position of this GeoTessPosition and returns the
	 * result in the input normal vector.
	 * 
	 * @param normal The outward pointing normal vector at the top of the current
	 *               layer for the current lateral position of this
	 *               GeoTessPosition. 
	 * @throws GeoTessException 
	 */
	public void getLayerNormal(double[] normal) throws GeoTessException
	{
		getLayerNormal(layerId, normal);
	}

	/**
	 * Creates an outward pointing normal vector at the top of the input layer
	 * index for the current lateral position of this GeoTessPosition and returns
	 * the result in the input normal vector.
	 * 
	 * @param layr   The index of the layer for which the normal will be returned.
	 * @param normal The outward pointing normal vector at the top of the input
	 *               layer for the current lateral position of this
	 *               GeoTessPosition. 
	 * @throws GeoTessException 
	 */
	public void getLayerNormal(int layr, double[] normal) throws GeoTessException
	{
		// initialize the normal and get the interpolation vertices and coefficients
		normal[0] = normal[1] = normal[2] = 0.0;

		//**T
		int tid = layerTessIds[layr];
		checkTessellation(tid);
		
		int[] v = vertices.get(tid).getArray();
		double[] h = hCoefficients.get(tid).getArray();

		// loop over each vertex
		for (int i = 0; i < vertices.get(tid).size(); ++i)
		{
			// get the vertex/layer profile and get its normal ... if null calculate
			// the normal for this profile
			Profile p = profiles[v[i]][layr];
			double[] layrNormal = p.getLayerNormal();
			if (layrNormal == null)
				layrNormal = model.getLayerNormal(v[i], layr);
			
			// sum the interpolation weight times the normal into the normal array
			normal[0] += layrNormal[0] * h[i];
			normal[1] += layrNormal[1] * h[i];
			normal[2] += layrNormal[2] * h[i];
		}

		// done ... normalize the result and return it
		GeoTessUtils.normalize(normal);
	}

	/**
	 * Replace the model that currently supports this GeoTessPosition object
	 * with a new model. For this to work, the new model and the current model
	 * must use the same grid.
	 * 
	 * <p>
	 * The benefit of calling this method is that if the application needs to
	 * interpolate a value at the same geographic position in multiple models
	 * that share the same grid, then the walking triangle algorithm and the
	 * calculation of geographic interpolation coefficients do not have to be
	 * repeated.
	 * 
	 * @param newModel
	 *            model that is to replace the currently supported model.
	 * @return reference to this GeoTessPosition object.
	 * @throws GeoTessException
	 *             if the new model and current model do not have GeoTessGrids
	 *             that have the same gridID.
	 */
	public boolean setModel(GeoTessModel newModel) throws GeoTessException
	{
		if (!newModel.getGrid().getGridID().equals(this.grid.getGridID()))
			throw new GeoTessException(
					"Specified model and current model use different grids.");

		this.model = newModel;
		profiles = model.getProfiles();
		
		layerTessIds = model.getMetaData().getLayerTessIds();
		nLayers = model.getMetaData().getNLayers();

		double r = this.radius;

		layerRadii.clear();
		this.radius = -1.;
		for (int i = 0; i <= nLayers; ++i)
			layerRadii.add(-1);

		setRadius(layerId, r);
		
		return true;
	}

	/**
	 * Set the interpolation point to specified latitude and and longitude in
	 * degrees and depth in km below the surface of the WGS84 ellipsoid. This method will perform a
	 * walking triangle search for the triangle in which the specified position
	 * is located and compute the associated interpolation coefficients.
	 * <p>
	 * This method is pretty expensive compared to the other version of
	 * setPosition where the position is specified as a unit vector and a
	 * radius.
	 * <p>
	 * Assumes WGS84 ellipsoid.
	 * 
	 * @param lat
	 *            in degrees.
	 * @param lon
	 *            in degrees.
	 * @param depth
	 *            below the surface of the WGS84 ellipsoid in km.
	 * @return reference to this GeoTessPosition object.
	 * @throws GeoTessException
	 */
	public GeoTessPosition set(double lat, double lon, double depth)
			throws GeoTessException
	{
		double[] uVector = model.getEarthShape().getVectorDegrees(lat, lon);
		double newRadius = model.getEarthShape().getEarthRadius(uVector) - depth;
		set(uVector, newRadius);
		
		return this;
	}

	/**
	 * Set the interpolation point. This method will perform a walking triangle
	 * search for the triangle in which the specified position is located and
	 * compute the associated 2D and radial interpolation coefficients.
	 * 
	 * @param uVector
	 *            the Earth-centered unit vector that defines the position that
	 *            is to be set.
	 * @param newRadius
	 *            the radius of the position, in km.
	 * @return reference to this GeoTessPosition object.
	 * @throws GeoTessException
	 */
	public GeoTessPosition set(double[] uVector, double newRadius) throws GeoTessException
	{
		updatePosition2D(nLayers-1, uVector);

		int lid = getLayerId(newRadius);

		updatePosition2D(lid, uVector);
		
		updateRadius(lid, newRadius);

		return this;
	}

	/**
	 * Set the interpolation point to specified latitude and and longitude in
	 * degrees and depth in km below the surface of the WGS84 ellipsoid. This method will perform a
	 * walking triangle search for the triangle in which the specified position
	 * is located and compute the associated interpolation coefficients.
	 * <p>
	 * This method is pretty expensive compared to the other version of
	 * setPosition where the position is specified as a unit vector and a
	 * radius.
	 * <p>
	 * Assumes WGS84 ellipsoid.
	 * 
	 * @param layerId
	 *            the index of the layer of the model in which the position is
	 *            located.
	 * @param lat
	 *            in degrees.
	 * @param lon
	 *            in degrees.
	 * @param depth
	 *            below the surface of the WGS84 ellipsoid in km.
	 * @return reference to this GeoTessPosition object.
	 * @throws GeoTessException
	 */
	public GeoTessPosition set(int layerId, double lat, double lon, double depth)
			throws GeoTessException
	{
		if (layerId < 0)
			set(lat, lon, depth);
		else
		{
			double[] uVector = model.getEarthShape().getVectorDegrees(lat, lon);
			updatePosition2D(layerId, uVector);
			updateRadius(layerId, model.getEarthShape().getEarthRadius(uVector) - depth);
		}
		
		return this;
	}

	/**
	 * Set the interpolation point. This method will perform a walking triangle
	 * search for the triangle in which the specified position is located and
	 * compute the associated 2D and radial interpolation coefficients.
	 * 
	 * @param layerId
	 *            the index of the layer of the model in which the position is
	 *            located.
	 * @param uVector
	 *            the Earth-centered unit vector that defines the position that
	 *            is to be set.
	 * @param radius
	 *            the radius of the position, in km.
	 * @return reference to this GeoTessPosition object.
	 * @throws GeoTessException
	 */
	public GeoTessPosition set(int layerId, double[] uVector, double radius)
			throws GeoTessException
	{
		if (layerId < 0)
			set(uVector, radius);
		else
		{
			updatePosition2D(layerId, uVector);
			if (radius >= 0.)
				updateRadius(layerId, radius);
		}
		
		return this;
	}

	/**
	 * Set the 2D position to uVector and radius to the radius of the top of the
	 * specified layer.
	 * 
	 * @param layerId
	 *            the index of the layer of the model in which the position is
	 *            located.
	 * @param uVector
	 *            the Earth-centered unit vector that defines the position that
	 *            is to be set.
	 * @return reference to this GeoTessPosition object.
	 * @throws GeoTessException
	 */
	public GeoTessPosition setTop(int layerId, double[] uVector) throws GeoTessException
	{
		updatePosition2D(layerId, uVector);
		updateRadius(layerId, getRadiusTop(layerId));
		
		return this;
	}

	/**
	 * Set the 2D position to uVector and radius to the radius of the bottom of
	 * the specified layer.
	 * 
	 * @param layerId
	 *            the index of the layer of the model in which the position is
	 *            located.
	 * @param uVector
	 *            the Earth-centered unit vector that defines the position that
	 *            is to be set.
	 * @return reference to this GeoTessPosition object.
	 * @throws GeoTessException
	 */
	public GeoTessPosition setBottom(int layerId, double[] uVector)
			throws GeoTessException
	{
		updatePosition2D(layerId, uVector);
		updateRadius(layerId, getRadiusBottom(layerId));
		
		return this;
	}

	/**
	 * Change the current layer and radius without changing the geographic
	 * position.
	 * 
	 * @param layerId
	 *            the index of the layer of the model in which the position is
	 *            located.
	 * @param radius
	 *            the radius of the position, in km.
	 * @return reference to this GeoTessPosition object.
	 * @throws GeoTessException
	 */
	public GeoTessPosition setRadius(int layerId, double radius) throws GeoTessException
	{
		if (tessid < 0)
			throw new GeoTessException(
					"geographic position has not been specified.");
		
		return updateRadius(layerId, radius);
	}

	/**
	 * Change the current radius without changing the geographic
	 * position.
	 * 
	 * @param newRadius
	 *            the radius of the position, in km.
	 * @return reference to this GeoTessPosition object.
	 * @throws GeoTessException
	 */
	public GeoTessPosition setRadius(double newRadius) throws GeoTessException
	{
		if (tessid < 0)
			throw new GeoTessException(
					"geographic position has not been specified.");

		return updateRadius(getLayerId(newRadius), newRadius);
	}

	/**
	 * Change the current layer and/or depth without changing the geographic
	 * position.
	 * 
	 * @param layerId
	 *            the index of the layer of the model in which the position is
	 *            located.
	 * @param depth
	 *            the depth of the position, in km.
	 * @return reference to this GeoTessPosition object.
	 * @throws GeoTessException
	 */
	public GeoTessPosition setDepth(int layerId, double depth) throws GeoTessException
	{
		return setRadius(layerId, getEarthRadius()-depth);
	}

	/**
	 * Change the current layer and/or depth without changing the geographic
	 * position.
	 * 
	 * @param depth depth in km below surface of WGS84 ellipsoid
	 * @return reference to this GeoTessPosition object.
	 * @throws GeoTessException
	 */
	public GeoTessPosition setDepth(double depth) throws GeoTessException
	{
		return setRadius(getEarthRadius()-depth);
	}

	/**
	 * Set the radius to the radius of the top of the specified layer.
	 * Geographic position remains unchanged.
	 * 
	 * @param layerId
	 *            the index of the layer of the model in which the position is
	 *            located.
	 * @return reference to this GeoTessPosition object.
	 * @throws GeoTessException
	 */
	public GeoTessPosition setTop(int layerId) throws GeoTessException
	{
		if (tessid < 0)
			throw new GeoTessException(
					"geographic position has not been specified.");
		tessid = layerTessIds[layerId];
		checkTessellation(tessid);
		updateRadius(layerId, getRadiusTop(layerId));
		
		return this;
	}

	/**
	 * Set the radius to the radius of the bottom of the specified layer.
	 * Geographic position remains unchanged.
	 * 
	 * @param layerId
	 *            the index of the layer of the model in which the position is
	 *            located.
	 * @return reference to this GeoTessPosition object.
	 * @throws GeoTessException
	 */
	public GeoTessPosition setBottom(int layerId) throws GeoTessException
	{
		if (tessid < 0)
			throw new GeoTessException(
					"geographic position has not been specified.");
		tessid = layerTessIds[layerId];
		checkTessellation(tessid);
		updateRadius(layerId, getRadiusBottom(layerId));
		
		return this;
	}

	/**
	 * Set the 2D position and ensure that the triangle, vertices and 2D
	 * interpolation coefficients for the tessellation that supports layerid
	 * have been updated. Sets radius = -1. Must call updateRadius after this
	 * call to ensure that radius is set properly.
	 * 
	 * @param newLayerId
	 * @param uVector
	 * @throws GeoTessException
	 */
	private void updatePosition2D(int newLayerId, double[] uVector)
			throws GeoTessException
	{
		tessid = layerTessIds[newLayerId];

		if (triangle[tessid] < 0 || unitVector[0] != uVector[0]
				|| unitVector[1] != uVector[1] || unitVector[2] != uVector[2])
		{
			// the vector position has changed. Update everything.

			// nullify the triangle index for all tessellations other than the
			// current one.
			for (int tess = 0; tess < triangle.length; ++tess)
				if (tess != tessid)
					triangle[tess] = -1;

			// 0.961261696 is cos(16 degrees)
			// if new position is more than 16 degrees away from current
			// position then start walk from triangle zero, otherwise,
			// start walk from current triangle
			if (triangle[tessid] < 0
					|| GeoTessUtils.dot(uVector, unitVector) < 0.961261696)
			{
				triangle[tessid] = grid.getTriangle(tessid, 0, 0);
				tessLevels[tessid] = 0;
			}

			unitVector[0] = uVector[0];
			unitVector[1] = uVector[1];
			unitVector[2] = uVector[2];
			threeDVector[0] = unitVector[0] * radius;
			threeDVector[1] = unitVector[1] * radius;
			threeDVector[2] = unitVector[2] * radius;
			
			// perform walking triangle algorith, which will set
			// triangle[tessid], linearCoefficients[tessid] and tessLevels[tessid]
			getContainingTriangle(tessid);

			// determine vertices and coefficients for interpolation in
			// geographic dimensions. This is an abstract method so the
			// results depend on the interpolator type.
			update2D(tessid);
			
			// nullify previously computed earth radius.
			earthRadius = -1;
			
			// set current radius to -1, forcing recalculation of radial
			// interpolation coefficients after this method is done.
			this.radius = -1.;

			// nullify all previously computed layer radii (layer boundaries).
			for (int lid = 0; lid < layerRadii.size(); ++lid)
				layerRadii.set(lid, -1);
			
			clearRadialCoefficients();
		}
		else
			// the 2D position did not change but the layerId/tessid might have.
			checkTessellation(tessid);
	}

	/**
	 * Ensure that vertices and coefficients have been computed for the
	 * specified tessellation.
	 * 
	 * <p>
	 * This method won't do anything if the containing triangle in the specified
	 * tessellation has already been computed. What can happen however, is that
	 * layerId and unitVector are changed with a call to setPosition2D(layerid,
	 * uVector). That will update all the information about vertices and
	 * coefficients for the tessid that supports layerid but will nullify that
	 * information for other tessids. This method can be called to ensure that
	 * vertices and coefficients are up-to-date for any tessid.
	 * 
	 * @param tessid
	 * @throws GeoTessException
	 */
	private void checkTessellation(int tessid) throws GeoTessException
	{
		if (triangle[tessid] < 0)
		{
			tessLevels[tessid] = 0;
			triangle[tessid] = grid.getTriangle(tessid, 0, 0);
			getContainingTriangle(tessid);
			update2D(tessid);
		}
	}

	/**
	 * Update the radius, layerId and tessid of this position.
	 * 
	 * @param layerId
	 * @param radius
	 * @return reference to this
	 * @throws GeoTessException
	 */
	private GeoTessPosition updateRadius(int layerId, double radius)
			throws GeoTessException
	{
		if (this.radius < 0. || radius != this.radius || layerId != this.layerId)
		{
			this.radius = radius;
			threeDVector[0] = unitVector[0] * radius;
			threeDVector[1] = unitVector[1] * radius;
			threeDVector[2] = unitVector[2] * radius;
      
			this.layerId = layerId;
			this.radialCoeffUpdateLayerId = layerId;

			tessid = layerTessIds[layerId];

			checkTessellation(tessid);

			clearRadialCoefficients();

		}
		return this;
	}
	
	private void clearRadialCoefficients()
	{
		for (int i=0; i<radialIndexes.size(); ++i)
		{
			radialIndexes.get(i).clear();
			radialCoefficients.get(i).clear();
		}		
	}
//
//	/**
//	 * Update the radial interpolation indexes and coefficients. 
//	 * This method should be called whenever the position is modified.  
//	 * Assumes that the proper tessid, layerId and radius are in effect.
//	 * 
//	 */
//	private void updateRadialCoefficients() 
//	{
//		// make sure dimensions of radialIndexes and radialCoefficients
//		// are at least as big as the number of vertices involved in interpolation.
//		while (radialIndexes.size() < vertices.get(tessid).size())
//		{
//			radialIndexes.add(new ArrayListInt(2));
//			radialCoefficients.add(new ArrayListDouble(2));			
//		}
//		
//		if (radialIndexes.get(0).size() == 0)
//		{
//			int[] v = vertices.get(tessid).getArray();
//			for (int i = 0; i < vertices.get(tessid).size(); ++i)
//				profiles[v[i]][layerId].setInterpolationCoefficients(radialInterpolatorType, 
//						radialIndexes.get(i), radialCoefficients.get(i), 
//						radius, radiusOutOfRangeAllowed);
//		}
//	}

	/**
	 * Update the radial interpolation indexes and coefficients for the input
	 * layer (this may or may not be layerid). This method should be called
	 * whenever the position is modified. Assumes that the proper radius is set.
	 */
	private void updateRadialCoefficients(int layer, int tid) 
	{
		// make sure dimensions of radialIndexes and radialCoefficients
		// are at least as big as the number of vertices involved in interpolation.
		while (radialIndexes.size() < vertices.get(tid).size())
		{
			radialIndexes.add(new ArrayListInt(2));
			radialCoefficients.add(new ArrayListDouble(2));			
		}
		
		if (radialCoeffUpdateLayerId != layer) clearRadialCoefficients();
		if (radialIndexes.get(0).size() == 0)
		{
			int[] v = vertices.get(tid).getArray();
			for (int i = 0; i < vertices.get(tid).size(); ++i)
				profiles[v[i]][layer].setInterpolationCoefficients(radialInterpolatorType, 
						radialIndexes.get(i), radialCoefficients.get(i), 
						radius, radiusOutOfRangeAllowed);
			radialCoeffUpdateLayerId = layer;
		}
	}

	/**
	 * Find the index of the triangle that contains position. Also computes the
	 * 3 linear interpolation coefficients that can be used to interpolate data stored
	 * on the vertices of the returned triangle.
	 * <p>
	 * A GeoTessPosition object has an attribute maxTessLevel that defaults to
	 * Integer.MAX_VALUE-1. The search is limited to that tessellation level. So
	 * the triangle identified by this method will reside either on
	 * x.maxTessLevel, or the largest tessellation level of the tessellation,
	 * whichever is smaller.
	 */
	private void getContainingTriangle(int tessid)
	{
		int t = triangle[tessid];
		int tessLevel = tessLevels[tessid];
		double[] c = linearCoefficients.get(tessid).getArray();
		int maxTess = maxTessLevel[tessid];

		while (true)
		{
			c[0] = GeoTessUtils.dot(gridEdges[t][0].normal, unitVector);
			if (c[0] > -1e-15)
			{
				c[1] = GeoTessUtils.dot(gridEdges[t][1].normal, unitVector);
				if (c[1] > -1e-15)
				{
					c[2] = GeoTessUtils.dot(gridEdges[t][2].normal, unitVector);
					if (c[2] > -1e-15)
					{
						if (c[2] > -1e-15)
						{
							if (gridDescendants[t] < 0
									|| tessLevel >= maxTess)
							{
								// the correct triangle has been found.
								// Normalize the coefficients
								// such that they sum to one.
								double sum = c[0] + c[1] + c[2];
								c[0] /= sum;
								c[1] /= sum;
								c[2] /= sum;
								triangle[tessid] = t;
								tessLevels[tessid] = tessLevel;
								return;
							}
							else
							{
								++tessLevel;
								t = gridDescendants[t];
							}
						}
					}
					else
						t = gridEdges[t][2].tLeft;
				}
				else
					t = gridEdges[t][1].tLeft;
			}
			else
				t = gridEdges[t][0].tLeft;
		}
	}

	/**
	 * Retrieve the index of the major layer in which radius resides.
	 * @throws GeoTessException
	 */
	public int getInterfaceIndex() throws GeoTessException
	{
		return getInterfaceIndex(getRadius());
	}

	/**
	 * Retrieve the index of the major layer in which radius resides.
	 * If radius < radius of first layer, returns 0.  
	 * If radius >= the surface of the model, returns the index 
	 * of the outermost layer that has finite thickness.
	 * @param radius
	 * @return int
	 * @throws GeoTessException
	 */
	public int getInterfaceIndex(double radius) throws GeoTessException
	{
		int i;
		int bot = -1;
		int top = nLayers-1;
		while (top - bot > 1)
		{
			i = (top + bot) / 2;
			if (radius > getRadiusTop(i))
				bot = i;
			else
				top = i;
		}

		if (top == nLayers-1)
			top = previousLayer(top, 1e-6);

		return top;
	}

	/**
	 * Returns an int[] of length = to number of major layers.  Each element 
	 * is the number of evenly spaced nodes required in that layer in order for 
	 * the node spacing to be <= maxSpacing (in km).    
	 * The first element is at the smallest radius (near center of earth) and the last
	 * element is near the surface.  Within a layer, there will be a node at 
	 * both the top and bottom of the layer.  This means that there will be 
	 * two nodes for each major layer interface. 
	 * <p>Layers below the layer containing radius0 will have value 0. Layer
	 * that contains radius0 will take into account that nodes will start at
	 * radius0.
	 * @throws GeoTessException 
	 */
	public int[] getInterfacesPerLayer(double radius0, double maxSpacing) throws GeoTessException 
	{
		int[] nodesPerLayer = new int[getNLayers()];
		int layerIndex = getInterfaceIndex(radius0);
		nodesPerLayer[layerIndex] = max(2, 1+(int) Math.ceil(
				(getRadiusTop(layerIndex)-radius0) / maxSpacing));
		    //***(getInterfaceRadius(layerIndex)-radius0) / maxSpacing));
		for (int i = layerIndex+1; i < getNLayers(); ++i)
			nodesPerLayer[i] = max(2, 1+(int) Math.ceil(getLayerThickness(i) / maxSpacing));
		return nodesPerLayer;
	}

	/**
	 * Retrieve an interpolated value of the radius of the top of the specified
	 * layer, in km.
	 * 
	 * @param layer
	 * @return interpolated value of the radius of the top of the specified
	 *         layer, in km.
	 * @throws GeoTessException
	 */
	public double getRadiusTop(int layer) throws GeoTessException
	{
		if (layerRadii.get(layer + 1) < 0)
		{
			int tid = layerTessIds[layer];
			
			checkTessellation(tid);
			
			if (layer < nLayers-1 && layerTessIds[layer+1] != tid)
			{
				// the next layer above the current layer is in a different
				// multi-level tessellation.  The containing triangle in the next layer
				// may be smaller and provide a more accurate estimate of 
				// the radius at current position.  Have to evaluate this.

				int tid2 = layerTessIds[layer+1];
				
				int t1 = getTriangle(tid);
				int t2 = getTriangle(tid2);
				if (biggerTriangle(t1, t2) == t1)
					// triangle on next layer is smaller than triangle on current layer
					tid = tid2;
			}
			
			int[] v = vertices.get(tid).getArray();
			double[] h = hCoefficients.get(tid).getArray();
			double r=0.;
			for (int i = 0; i < vertices.get(tid).size(); ++i)
				r += profiles[v[i]][layer].getRadiusTop() * h[i];
		
			layerRadii.set(layer + 1, r);
		}
		return Double.isNaN(layerRadii.get(layer + 1)) ? getErrorValue()
				: layerRadii.get(layer + 1);
	}

	/**
	 * Retrieve an interpolated value of the radius of the bottom of the
	 * specified layer, in km.
	 * 
	 * @param layer
	 * @return interpolated value of the radius of the bottom of the specified
	 *         layer, in km.
	 * @throws GeoTessException
	 */
	public double getRadiusBottom(int layer) throws GeoTessException
	{
		if (layerRadii.get(layer) < 0)
		{
			int tid = layerTessIds[layer];
			
			checkTessellation(tid);
			
			if (layer > 0 && layerTessIds[layer-1] != tid)
			{
				// the layer below the current layer is in a different
				// multi-level tessellation.  The containing triangle in the previous layer
				// may be smaller and provide a more accurate estimate of 
				// the radius at current position.  Have to evaluate this.

				int tid2 = layerTessIds[layer-1];
				
				int t1 = getTriangle(tid);
				int t2 = getTriangle(tid2);
				if (biggerTriangle(t1, t2) == t1)
					// triangle on previous layer is smaller than triangle on current layer
					tid = tid2;
			}
			
			int[] v = vertices.get(tid).getArray();
			double[] h = hCoefficients.get(tid).getArray();
			double r=0.;
			for (int i = 0; i < vertices.get(tid).size(); ++i)
				r += profiles[v[i]][layer].getRadiusBottom() * h[i];
		
			layerRadii.set(layer, r);
		}
		return Double.isNaN(layerRadii.get(layer)) ? getErrorValue()
				: layerRadii.get(layer);
	}
	
	/**
	 * A metric that can be used to evaluate relative triangle size.  
	 * Returns the inverse of the sum of the dot products of the 
	 * triangle's edges.  
	 * @param tIndex
	 * @return the inverse of the sum of the dot products of the 
	 * triangle's edges.  
	 */
	private int biggerTriangle(int t1, int t2)
	{
		int[] tv = grid.getTriangleVertexIndexes(t1);
		double dot1 = GeoTessUtils.dot(grid.getVertex(tv[0]), grid.getVertex(tv[1]))
				+ GeoTessUtils.dot(grid.getVertex(tv[1]), grid.getVertex(tv[2]))
				+ GeoTessUtils.dot(grid.getVertex(tv[2]), grid.getVertex(tv[0]));

		tv = grid.getTriangleVertexIndexes(t2);
		double dot2 = GeoTessUtils.dot(grid.getVertex(tv[0]), grid.getVertex(tv[1]))
				+ GeoTessUtils.dot(grid.getVertex(tv[1]), grid.getVertex(tv[2]))
				+ GeoTessUtils.dot(grid.getVertex(tv[2]), grid.getVertex(tv[0]));
		
		return dot2 > dot1 ? t1 : t2;

	}

	/**
	 * Retrieve the radius of the Earth at this position, in km. Assumes WGS84
	 * ellipsoid.
	 * 
	 * @return the radius of the Earth at this position, in km.
	 */
	public double getEarthRadius()
	{
		if (earthRadius < 0.)
			earthRadius = model.getEarthShape().getEarthRadius(getVector());
		return earthRadius;
	}

	/**
	 * Returns the model Earth shape for this GeoTessPosition.
	 * 
	 * @return The model Earth shape for this GeoTessPosition.
	 */
	public EarthShape getEarthShape()
	{
		return model.getEarthShape();
	}

	/**
	 * Retrieve a reference to the 3 component unit vector that corresponds to
	 * the current position. Do not modify the values of this array.
	 * 
	 * @return a reference to the 3 component unit vector that corresponds to
	 *         the current position
	 */
	public double[] getVector()
	{
		return unitVector;
	}

	/**
	 * Retrieve a reference to the 3 component 3D vector that corresponds to
	 * the current position. Do not modify the values of this array.
	 * 
	 * @return a reference to the 3 component 3D vector that corresponds to
	 *         the current position
	 */
	public double[] get3DVector()
	{
		return threeDVector;
	}

	/**
	 * Retrieve the index of the triangle within which the current position is
	 * located
	 * 
	 * @return the index of the triangle within which the current position is
	 *         located
	 */
	public int getTriangle()
	{
		return triangle[tessid];
	}

	/**
	 * Retrieve the index of the triangle in the specified tessellation
	 * within which the current position is located 
	 * 
	 * @return the index of the triangle in the specified tessellation
	 * within which the current position is located 
	 * @throws GeoTessException 
	 */
	protected int getTriangle(int tessid) throws GeoTessException
	{
		checkTessellation(tessid);
		return triangle[tessid];
	}

	/**
	 * Retrieve the number of vertices involved in the interpolation of data.
	 * When interpolationType is LINEAR, this will always return 3. When
	 * interpolationType is natural_neighbor, there could be anywhere from 1 to
	 * as many as 6 or more.
	 * 
	 * @return the number of vertices involved in the interpolation of data.
	 */
	public int getNVertices()
	{
		return vertices.get(tessid).size();
	}

	/**
	 * Returns the number of layers in the model.
	 * 
	 * @return The number of layers in the model.
	 */
	public int getNLayers()
	{
		return nLayers;
	}

	/**
	 * Retrieve a copy of the horizontal interpolation coefficients associated with
	 * the vertices of the tessellation used to interpolate data.
	 * 
	 * @return a copy of the horizontal interpolation coefficients associated with the
	 *         vertices of the tessellation used to interpolate data.
	 */
	public double[] getHorizontalCoefficients()
	{
		return hCoefficients.get(tessid).toArray();
	}

	/**
	 * Retrieve the interpolation coefficient associated with one of the
	 * vertices of the tessellation used to interpolate data.
	 * 
	 * @param index
	 *            the index of the desired coefficient (0..2)
	 * @return one of the interpolation coefficients
	 */
	public double getHorizontalCoefficient(int index)
	{
		return hCoefficients.get(tessid).get(index);
	}

	/**
	 * Retrieve a copy of the indexes of the vertices used to interpolate
	 * data.
	 * 
	 * @return a copy of the indexes of the vertices used to interpolate
	 *         data.
	 */
	public int[] getVertices()
	{
		return vertices.get(tessid).toArray();
	}

	/**
	 * Retrieve the index of the vertex with the highest interpolation 
	 * coefficient.
	 * 
	 * @return the index of the vertex with the highest interpolation 
	 * coefficient.
	 */
	public int getIndexOfClosestVertex()
	{
		int index = -1;
		ArrayListDouble c = hCoefficients.get(tessid);
		for (int i = 0; i < c.size(); ++i)
			if (i == 0 || c.get(i) > c.get(index))
				index = i;
		return vertices.get(tessid).get(index);
	}

	/**
	 * Retrieve the unit vector of the vertex with the highest interpolation 
	 * coefficient.
	 * 
	 * @return the unit vector of the vertex with the highest interpolation 
	 * coefficient.
	 */
	public double[] getClosestVertex()
	{
		// used to return the index (0..2) of interpolation vertex with
		// highest coefficient.  Now returns the unit vector of the 
		// vertex with highest coefficient.
		return grid.getVertex(getIndexOfClosestVertex());
	}

	/**
	 * Return the index of one of the vertices used to interpolate data.
	 * 
	 * @param index
	 *            the index of the desired coefficient (0..2)
	 * @return the index of one of the vertices used to interpolate data.
	 */
	public int getVertex(int index)
	{
		return vertices.get(tessid).get(index);
	}

	/**
	 * Set the maximum tessellation level such that the triangle that is found
	 * during a walking triangle search will be on a tessellation level that is
	 * no higher than the specified value. Default value is Integer.MAX_VALUE-1.
	 * 
	 * @param layerId
	 * @param maxTessLevel
	 * @throws GeoTessException
	 */
	public void setMaxTessLevel(int layerId, int maxTessLevel)
			throws GeoTessException
	{
		this.maxTessLevel[layerTessIds[layerId]] = maxTessLevel;
		triangle[layerTessIds[layerId]] = -1;
		if (tessid >= 0)
			checkTessellation(tessid);
	}

	/**
	 * Retrieve the current value of maxTessLevel, which is the maximum
	 * tessellation level such that the triangle that is found during a walking
	 * triangle search will be on a tessellation level that is no higher than
	 * the specified value. Default value is Integer.MAX_VALUE-1.
	 * 
	 * @param layerId
	 * @return current value of maxTessLevel
	 */
	public int getMaxTessLevel(int layerId)
	{
		return maxTessLevel[layerTessIds[layerId]];
	}

	/**
	 * Returns an int[] of length = to number of major layers.  Each element 
	 * is the number of evenly spaced nodes required in that layer in order for 
	 * the node spacing to be <= maxSpacing (in km).
	 * 
	 * The first element is at the smallest radius (near center of earth) and
	 * the last element is near the surface.  Within a layer, there will be a node 
	 * at both the top and bottom of the layer.  This means that there will be 
	 * two nodes for each major layer interface. 
	 * @throws GeoTessException 
	 */
	public int[] getLayerDiscretization(double maxSpacing) throws GeoTessException
	{
		int[] nodesPerLayer = new int[getNLayers()];
		for (int i = 0; i < nodesPerLayer.length; ++i)
			nodesPerLayer[i] = Math.max(2, 1 + (int) Math.ceil(getLayerThickness(i) /
					                        maxSpacing));
		return nodesPerLayer;
	}

	// ////////////////////////////////////////////////////////////////////////////

	/**
	 * Retrieve the index of the tessellation level of the triangle that was
	 * found the last time that the walking triangle algorithm was executed.
	 * 
	 * @return index of current tessellation level, relative to the first
	 *         tessellation level in the current tessellation
	 */
	public int getTessLevel()
	{
		return tessLevels[tessid];
	}

	/**
	 * Retrieve the index of the tessellation level of the triangle that was
	 * found the last time that the walking triangle algorithm was executed.
	 * 
	 * @return index of current tessellation level, relative to the first
	 *         tessellation level in the current tessellation
	 */
	protected int getTessLevel(int tessId)
	{
		return tessLevels[tessId];
	}

	/**
	 * Retrieve an interpolated value of the radius of the top of the current
	 * layer.
	 * 
	 * @return interpolated value of the radius of the top of the specified
	 *         layer.
	 * @throws GeoTessException
	 */
	public double getRadiusTop() throws GeoTessException
	{
		return getRadiusTop(layerId);
	}

	/**
	 * Retrieve an interpolated value of the radius of the bottom of the current
	 * layer.
	 * 
	 * @return an interpolated value of the radius of the bottom of the
	 *         specified layer.
	 * @throws GeoTessException
	 */
	public double getRadiusBottom() throws GeoTessException
	{
		return getRadiusBottom(layerId);
	}

	/**
	 * Retrieve an interpolated value of the depth of the top of the current
	 * layer. Assumes WGS84 ellipsoid.
	 * 
	 * @return interpolated value of the depth of the top of the current layer.
	 * @throws GeoTessException
	 */
	public double getDepthTop() throws GeoTessException
	{
		return getEarthRadius() - getRadiusTop(layerId);
	}

	/**
	 * Retrieve an interpolated value of the depth of the bottom of the current
	 * layer. Assumes WGS84 ellipsoid.
	 * 
	 * @return interpolated value of the depth of the bottom of the current
	 *         layer.
	 * @throws GeoTessException
	 */
	public double getDepthBottom() throws GeoTessException
	{
		return getEarthRadius() - getRadiusBottom(layerId);
	}

	/**
	 * Retrieve an interpolated value of the depth of the top of the current
	 * layer. Assumes WGS84 ellipsoid.
	 * 
	 * @param layer
	 * @return interpolated value of the depth of the top of the current layer.
	 * @throws GeoTessException
	 */
	public double getDepthTop(int layer) throws GeoTessException
	{
		return getEarthRadius() - getRadiusTop(layer);
	}

	/**
	 * Retrieve an interpolated value of the depth of the bottom of the current
	 * layer. Assumes WGS84 ellipsoid.
	 * 
	 * @param layer
	 * @return interpolated value of the depth of the bottom of the current
	 *         layer.
	 * @throws GeoTessException
	 */
	public double getDepthBottom(int layer) throws GeoTessException
	{
		return getEarthRadius() - getRadiusBottom(layer);
	}

	/**
	 * Retrieve the thickness of specified layer, in km.
	 * 
	 * @param layer
	 *            layer index
	 * @return the thickness of specified layer, in km.
	 * @throws GeoTessException
	 */
	public double getLayerThickness(int layer) throws GeoTessException
	{
		return getRadiusTop(layer) - getRadiusBottom(layer);
	}

	/**
	 * Retrieve the thickness of current layer, in km.
	 * 
	 * @return the thickness of current layer, in km.
	 * @throws GeoTessException
	 */
	public double getLayerThickness() throws GeoTessException
	{
		return getRadiusTop() - getRadiusBottom();
	}
	
	/**
	 * Retrieve the radii of all the layer interfaces in km.
	 * The returned array has nLayers+1 elements.  The first 
	 * element is the radius of the bottom of the deepest layer
	 * and the last element is the radius of the top of the 
	 * shallowest layer.
	 * @return the radii of all the layer interfaces in km.
	 * @throws GeoTessException
	 */
	public double[] getLayerRadii() throws GeoTessException
	{
		double[] layerRadii = new double[nLayers+1];
		layerRadii[0] = getRadiusBottom(0);
		for (int i=0; i<nLayers; ++i)
			layerRadii[i+1] = getRadiusTop(i);
		return layerRadii;
	}

	/**
	 * Retrieve the radius of the current position, in km.
	 * This is the radius that was specified in the most recent call to one of the set() methods.
	 * 
	 * @return the radius of the current position, in km.
	 */
	public double getRadius()
	{
		return radius;
	}

	/**
	 * Retrieve the depth of the current position in km. 
	 * This is the depth that corresponds to the radius that was specified in the 
	 * most recent call to one of the set() methods.
	 * Assumes WGS84 ellipsoid.
	 * 
	 * @return the depth of the current position in km.
	 */
	public double getDepth()
	{
		return getEarthRadius() - radius;
	}

	/**
	 * Retrieve the radius of the current position, in km.
	 * If radius is constrained to a specific layer, and the 
	 * radius specified in most recent call to a set() method
	 * was outside the range of the specified layer, then
	 * the radius of the top or bottom of the layer is returned.
	 * 
	 * @return the radius of the current position, in km.
	 * @throws GeoTessException 
	 */
	public double getRadiusConstrained() throws GeoTessException
	{
		if (!radiusOutOfRangeAllowed) return radius;
		
		if (radius < getRadiusBottom())
			return getRadiusBottom();
		if (radius > getRadiusTop())
			return getRadiusTop();
		
		return radius;
	}

	/**
	 * Resets the radius of the current position, in km, if out of range
	 * of the input layer. If radius is constrained to a specific layer, and the 
	 * radius specified in most recent call to a set() method was outside the
	 * range of the specified layer, then the radius of the top or bottom of
	 * the layer is set as the current radius. The new position (or old,
	 * if not set) is returned on exit.
	 * 
	 * @param  layer The input layer into which the radius is contrained.
	 * @return the radius of the current position, in km.
	 * @throws GeoTessException 
	 */
	public double setRadiusConstrained(int layer) throws GeoTessException
	{
		if (!radiusOutOfRangeAllowed) return radius;
		
		if (radius < getRadiusBottom(layer))
			setRadius(layer, getRadiusBottom(layer));
		else if (radius > getRadiusTop(layer))
			setRadius(layer, getRadiusTop(layer));
		
		return radius;
	}

	/**
	 * Retrieve the depth of the current position in km. 
	 * If radius is constrained to a specific layer, and the 
	 * radius specified in most recent call to a set() method
	 * was outside the range of the specified layer, then
	 * the depth of the top or bottom of the layer is returned.
	 * Assumes WGS84 ellipsoid.
	 * 
	 * @return the depth of the current position in km.
	 * @throws GeoTessException 
	 */
	public double getDepthConstrained() throws GeoTessException
	{
		return getEarthRadius() - getRadiusConstrained();
	}

	/**
	 * Retrieve the index of the first layer above the specified majorLayerIndex
	 * that has finite thickness. If there is no such layer, the value of
	 * nLayers is returned.
	 * 
	 * @param majorLayerIndex int
	 * @param minThick double
	 * @return int
	 * @throws GeoTessException
	 */
	public int nextLayer(int majorLayerIndex, double minThick)
	       throws GeoTessException
	{
		for (;;)
			if (++majorLayerIndex >= nLayers
					|| getLayerThickness(majorLayerIndex) >= minThick)
				return majorLayerIndex;
	}

	/**
	 * Retrieve the index of the first layer below the specified majorLayerIndex
	 * that has finite thickness. If there is no such layer, -1 is returned.
	 * 
	 * @param majorLayerIndex int
	 * @param minThick double
	 * @return int
	 * @throws GeoTessException
	 */
	public int previousLayer(int majorLayerIndex, double minThick)
	       throws GeoTessException
	{
		for (;;)
			if (majorLayerIndex < 0
					|| getLayerThickness(majorLayerIndex) >= minThick)
				return majorLayerIndex;
			else
				--majorLayerIndex;
	}

	/**
	 * Return the 3D vector difference between this GeoTessPosition and the input
	 * GeoTessPosition (gtp) in the vector x.
	 * 
	 * @param gtp The input GeoTessPosition.
	 * @param x The difference between this GeoTessPosition and the input
	 *          GeoTessPosition (gtp).
	 */
	public void minus(GeoTessPosition gtp, double[] x)
	{
		x[0] = threeDVector[0] - gtp.threeDVector[0];
		x[1] = threeDVector[1] - gtp.threeDVector[1];
		x[2] = threeDVector[2] - gtp.threeDVector[2];
	}

	/**
	 * Return the 3D vector difference between this GeoTessPosition and the input
	 * GeoTessPosition (gtp).
	 * 
	 * @param gtp The input GeoTessPosition.
	 * @return The difference between this GeoTessPosition and the input
	 *         GeoTessPosition (gtp).
	 */
	public double[] minus(GeoTessPosition gtp)
	{
		double[] x = threeDVector.clone();
		x[0] -= gtp.threeDVector[0];
		x[1] -= gtp.threeDVector[1];
		x[2] -= gtp.threeDVector[2];
		return x;
	}

	/**
	 * @return true if the radius of this node is greater than the 
	 * radius of the surface of the solid earth as represented in this
	 * node.
	 * 
	 * @throws GeoTessException
	 */
	public boolean isAboveModel() throws GeoTessException
	{
		return radius > getRadiusTop(nLayers-1);
	}

	/**
	 * Returns the radius to the surface at the current location.
	 * 
	 * @return The radius to the surface at the current location.
	 * @throws GeoTessException
	 */
	public double getSurfaceRadius() throws GeoTessException
	{
		return getRadiusTop(nLayers - 1);
	}

	/**
	 * Returns the depth to the surface at the current location.
	 * 
	 * @return The depth to the surface at the current location.
	 * @throws GeoTessException
	 */
	public double getSurfaceDepth() throws GeoTessException
	{
		return getDepthTop(nLayers - 1);
	}

	@Override
	public String toString()
	{
		StringBuffer buf = new StringBuffer();
		buf.append(String.format(
				"Triangle %1d layer %d tess %d pos: %1.6f, %1.6f, %1.3f%n",
				triangle[tessid], layerId, tessid,
				model.getEarthShape().getLatDegrees(unitVector),
				model.getEarthShape().getLonDegrees(unitVector), getDepth()));

		buf.append("  Node     Lat        Lon       Coeff  Distance (deg)\n");

		int[] v = vertices.get(tessid).getArray();
		double[] h = hCoefficients.get(tessid).getArray();

		// InterpolatorType radialInterpolator = getInterpolatorType() ==
		// InterpolatorType.NATURAL_NEIGHBOR
		// ? InterpolatorType.SPLINE : InterpolatorType.LINEAR;

		for (int i = 0; i < vertices.get(tessid).size(); ++i)
			buf.append(String.format("%6d %s %10.6f %7.3f%n", v[i],
					model.getEarthShape().getLatLonString(grid.getVertex(v[i])), h[i],
					GeoTessUtils.angleDegrees(unitVector, grid.getVertex(v[i]))));

		buf.append(String.format("%n"));
		return buf.toString();
	}

	/**
	 * Returns the position string as "lat lon depth".
	 * @return The position string.
	 */
	public String getPositionString()
	{
		return String.format("%s %8.3f",
   				model.getEarthShape().getLatLonString(getVector()), getDepth());
	}

	/**
	 * Returns the positions latitude in radians.
	 * 
	 * @return The positions latitude in radians.
	 */
	public double getLatitude()
	{
		return model.getEarthShape().getLat(getVector());
	}

	/**
	 * Returns the positions latitude in degrees.
	 * 
	 * @return The positions latitude in degrees.
	 */
	public double getLatitudeDegrees()
	{
		return model.getEarthShape().getLatDegrees(getVector());
	}

	/**
	 * Returns the positions longitude in radians.
	 * 
	 * @return The positions longitude in radians.
	 */
	public double getLongitude()
	{
		return model.getEarthShape().getLon(getVector());
	}

	/**
	 * Returns the positions longitude in degrees.
	 * 
	 * @return The positions longitude in degrees.
	 */
	public double getLongitudeDegrees()
	{
		return model.getEarthShape().getLonDegrees(getVector());
	}

	/**
	 * @return the model
	 */
	public GeoTessModel getModel()
	{
		return model;
	}

	/**
	 * Retrieve the index of the tessellation that supports the layer specified
	 * in the last call to setPosition.
	 * 
	 * @return the index of the tessellation that supports the layer specified
	 *         in the last call to setPosition.
	 */
	public int getTessId()
	{
		return tessid;
	}

	/**
	 * Retrieve the index of the layer specified in the last call to
	 * setPosition.
	 * 
	 * @return the index of the layer specified in the last call to setPosition.
	 */
	public int getLayerId()
	{
		return layerId;
	}

	/**
	 * Retrieve the index of the layer that contains the specified radius. If
	 * radius is less than bottom of model, returns 0. If radius greater than
	 * top of model, returns nLayers-1.  If all layers have zero thickness, 
	 * returns nLayers-1.
	 * 
	 * @param radius
	 *            in km
	 * @return the index of the layer that contains the specified radius.
	 * @throws GeoTessException
	 */
	public int getLayerId(double radius) throws GeoTessException
	{
		for (int i = 0; i < nLayers; ++i)
			if (radius <= getRadiusTop(i))
				return i;
		
		for (int i=nLayers-1; i>=0; --i)
			if (getLayerThickness(i) > 0)
				return i;

		return nLayers - 1;
	}

	/**
	 * If any calculated value is Double.NaN, then functions like getValue() or
	 * getRadiusTop() or getRadiusBottom() will return this errorValue. The
	 * default is NaN, but it can be set by calling setErrorValue();
	 * 
	 * @return current value of the errorValue.
	 */
	public double getErrorValue()
	{
		return errorValue;
	}

	/**
	 * If any calculated value is NaN, then functions like getValue() or
	 * getRadiusTop() or getRadiusBottom() will return this value. The default
	 * is NaN, but it can be set by calling this function.
	 * 
	 * @param errorValue
	 */
	public void setErrorValue(double errorValue)
	{
		this.errorValue = errorValue;
	}

	/**
	 * If the position of this GeoTessPosition obect is currently set to a
	 * location that coincides with one of the grid vertices, return the index
	 * of that vertex. Otherwise return -1.  Colocation includes a small 
	 * tolerance, i.e., the interpolation coefficient > 0.999999999.
	 * 
	 * @return index of colocated vertex or -1.
	 */
	public int getVertexIndex()
	{
		for (int v = 0; v < vertices.get(tessid).size(); ++v)
			if (hCoefficients.get(tessid).get(v) > 0.999999999)
				return vertices.get(tessid).get(v);
		return -1;

	}

	/**
	 * Find the point index of the point that is closest to the interpolation point.
	 * @return point index
	 * @throws GeoTessException
	 */
	public int getClosestPoint() throws GeoTessException
	{
		if (radialInterpolatorType == InterpolatorType.CUBIC_SPLINE)
			throw new GeoTessException("\nCannot compute radial coefficients for InterpolatorType.CUBIC_SPLINE");

		model.getPointMap();

		updateRadialCoefficients(layerId, tessid);

		Profile p;
		int[] v = vertices.get(tessid).getArray();
		double[] h = hCoefficients.get(tessid).getArray();
		int[] ri;
		double[] ci;
		int closestPoint = -1;
		double c, cmax = Double.NEGATIVE_INFINITY;

		for (int i = 0; i < vertices.get(tessid).size(); ++i)
		{
			p = profiles[v[i]][layerId];
			ri = radialIndexes.get(i).getArray();
			ci = radialCoefficients.get(i).getArray();
			for (int j=0; j<radialIndexes.get(i).size(); ++j)
			{
				c = ci[j]*h[i];
				if (c > cmax) { cmax = c; closestPoint = p.getPointIndex(ri[j]); }
			}
		}
		return closestPoint;
	}

	/**
	 * Retrieve a map from pointIndex to interpolation coefficient. The 
	 * returned coefficients sum to one.
	 * @return HashMap<Integer, Double> from pointIndex to interpolation coefficient.
	 * @throws GeoTessException 
	 */
	public HashMap<Integer, Double> getCoefficients() throws GeoTessException
	{
		HashMap<Integer, Double> coefficients = new HashMap<Integer, Double>(12);
		getWeights(coefficients, 1.);
		return coefficients;
	}

	/**
	 * Add entries to supplied map from pointIndex to interpolation coefficient.  
	 * Supplied map is cleared prior to population. The 
	 * returned coefficients sum to one.
	 * @param coefficients map from pointIndex to interpolation coefficient
	 * @throws GeoTessException 
	 */
	public void getCoefficients(HashMap<Integer, Double> coefficients) throws GeoTessException
	{
		if (radialInterpolatorType == InterpolatorType.CUBIC_SPLINE)
			throw new GeoTessException("\nCannot compute radial coefficients for InterpolatorType.CUBIC_SPLINE");

		coefficients.clear();
		
		model.getPointMap();

		updateRadialCoefficients(layerId, tessid);

		Profile p;
		int[] v = vertices.get(tessid).getArray();
		double[] h = hCoefficients.get(tessid).getArray();
		int[] ri;
		double[] ci;

		for (int i = 0; i < vertices.get(tessid).size(); ++i)
		{
			p = profiles[v[i]][layerId];
			ri = radialIndexes.get(i).getArray();
			ci = radialCoefficients.get(i).getArray();
			for (int j=0; j<radialIndexes.get(i).size(); ++j)
				coefficients.put(p.getPointIndex(ri[j]), ci[j]*h[i]);
		}
	}

	public void getWeights(HashMap<Integer, Double> weights, double dkm) throws GeoTessException
	{
		if (radialInterpolatorType == InterpolatorType.CUBIC_SPLINE)
			throw new GeoTessException("\nCannot compute radial coefficients for InterpolatorType.CUBIC_SPLINE");

		model.getPointMap();

		Profile p;
		Double w;
		int pt;

		int[] v = vertices.get(tessid).getArray();
		double[] h = hCoefficients.get(tessid).getArray();
		int[] ri;
		double[] ci;

		updateRadialCoefficients(layerId, tessid);
		for (int i = 0; i < vertices.get(tessid).size(); ++i)
		{
			p = profiles[v[i]][layerId];
			ri = radialIndexes.get(i).getArray();
			ci = radialCoefficients.get(i).getArray();
			for (int j=0; j<radialIndexes.get(i).size(); ++j)
			{
				pt = p.getPointIndex(ri[j]);
				w = weights.get(pt);
				weights.put(pt, w == null ? dkm*ci[j]*h[i] : w+dkm*ci[j]*h[i]);
			}
		}
	}
 
	public void getWeights(HashMapIntegerDouble weights, double dkm) throws GeoTessException
	{
		if (radialInterpolatorType == InterpolatorType.CUBIC_SPLINE)
			throw new GeoTessException("\nCannot compute radial coefficients for InterpolatorType.CUBIC_SPLINE");

		model.getPointMap();
		
		Profile p;
		double w;
		int pt;

		int[] v = vertices.get(tessid).getArray();
		double[] h = hCoefficients.get(tessid).getArray();
		int[] ri;
		double[] ci;

		updateRadialCoefficients(layerId, tessid);
		for (int i = 0; i < vertices.get(tessid).size(); ++i)
		{
			p = profiles[v[i]][layerId];
			ri = radialIndexes.get(i).getArray();
			ci = radialCoefficients.get(i).getArray();
			for (int j=0; j<radialIndexes.get(i).size(); ++j)
			{
				pt = p.getPointIndex(ri[j]);
				w = weights.get(pt);
				weights.put(pt, w == Double.MIN_VALUE ? dkm*ci[j]*h[i] : w+dkm*ci[j]*h[i]);
			}
		}
	}
 
	/**
	 * Returns true if the radius-out-of-range-allowed flag is true.
	 * 
	 * @return True if the radius-out-of-range-allowed flag is true.
	 */
	public boolean isRadiusOutOfRangeAllowed()
	{
		return radiusOutOfRangeAllowed;
	}

	/**
	 * Sets the radius-out-of-range-allowed flag to the input value.
	 * 
	 * @param radiusOutOfRangeAllowed The new radius-out-of-range-allowed flag
	 *                                setting.
	 */
	public void setRadiusOutOfRangeAllowed(boolean radiusOutOfRangeAllowed)
	{
		if (this.radiusOutOfRangeAllowed != radiusOutOfRangeAllowed)
			clearRadialCoefficients();

		this.radiusOutOfRangeAllowed = radiusOutOfRangeAllowed;
	}

	/**
	 *  Returns the 3D linear distance (km) between this position and the input
	 *  position.
	 *  
	 * @param pos The input position for which the 3D distance between it and this
	 *            position is returned (km).
	 * @return The 3D linear distance (km) between this position and the input
	 *         position.
	 */
	public double getDistance3D(GeoTessPosition pos)
	{
		double[] posv = pos.get3DVector();
		double x = threeDVector[0] - posv[0];
		double y = threeDVector[1] - posv[1];
		double z = threeDVector[2] - posv[2];
		return Math.sqrt(x*x + y*y + z*z);
	}

	/**
	 * Returns the angular distance (radians) between this GeoTessPosition and
	 * the input GeoTessPosition.
	 * 
	 * @param pos The input position.
	 * @return The angular distance (radians) between this GeoTessPosition and
	 *         the input GeoTessPosition.
	 */
	public double distance(GeoTessPosition pos)
	{
		return VectorUnit.angle(unitVector, pos.unitVector);
	}

	/**
	 * Returns the angular distance (degrees) between this GeoTessPosition and
	 * the input GeoTessPosition.
	 * 
	 * @param pos The input position.
	 * @return The angular distance (degrees) between this GeoTessPosition and
	 *         the input GeoTessPosition.
	 */
	public double distanceDegrees(GeoTessPosition pos)
	{
		return Math.toDegrees(VectorUnit.angle(unitVector, pos.unitVector));
	}

	/**
	 * Returns the azimuth (radians clockwise from north) between this
	 * GeoTessPosition and the input GeoTessPosition (pos).
	 * 
	 * @param pos The input GeoTessPosition.
	 * @return The azimuth (radians clockwise from north) between this
	 *         GeoTessPosition and the input GeoTessPosition (pos).
	 */
	public double azimuth(GeoTessPosition pos)
	{
		return VectorUnit.azimuth(unitVector, pos.unitVector, errorValue);
	}

	/**
	 * Creates and returns a new GeoTessPosition object whose coordinates are set
	 * to the fractional distance f on a line from gtp0 to gtp1. The new
	 * GeoTessPosition uses the same GeoTessModel and interpolator types as the
	 * input arguments.
	 * 
	 * @param gtp0 The first GeoTessPosition.
	 * @param gtp1 The second GeoTessPosition.
	 * @param f    A fraction defining on 3D point on a line that passes through
	 *             gtp0 and gtp1. The new point is measured from gtp0.
	 * @return A new GeoTessPosition object whose coordinates are set to the
	 *         fractional distance f on a line from gtp0 to gtp1.
	 * @throws GeoTessException
	 */
	static public GeoTessPosition getGeoTessPosition(GeoTessPosition gtp0,
			                                      GeoTessPosition gtp1, double f)
			   throws GeoTessException
	{
		// create a new GeoTessPosition using the model and interpolator types of
		// gtp0 ... set its position ... and return it
		GeoTessPosition gtpf = GeoTessPosition.getGeoTessPosition(gtp0);
		gtpf.setIntermediatePosition(gtp0, gtp1, f);
		return gtpf;
	}

	/**
	 * Sets this GeoTessPosition to the fractional distance f on a line from
	 * gtp0 to gtp1.
	 * 
	 * @param threeDVector A 3D vector acting as the second point from this
	 *                     positions location.
	 * @param f    A fraction defining a 3D point on a line that passes through
	 *             this node and the input 3D vector. The new position is measured
	 *             from this position.
	 * @throws GeoTessException
	 */
	public void setIntermediatePosition(double[] threeDVector, double f)
			   throws GeoTessException
  {
    double[] v = get3DVector().clone();
    v[0] += (threeDVector[0] - v[0]) * f;
    v[1] += (threeDVector[1] - v[1]) * f;
    v[2] += (threeDVector[2] - v[2]) * f;
    set(v, GeoTessUtils.normalize(v));
  }

	/**
	 * Sets this GeoTessPosition to the fractional distance f on a line from
	 * gtp0 to gtp1.
	 * 
	 * @param gtp0 The first GeoTessPosition.
	 * @param gtp1 The second GeoTessPosition.
	 * @param f    A fraction defining on 3D point on a line that passes through
	 *             gtp0 and gtp1. The new position is measured from gtp0.
	 * @throws GeoTessException
	 */
	public void setIntermediatePosition(GeoTessPosition gtp0,
			                                GeoTessPosition gtp1, double f)
			   throws GeoTessException
  {
    double[] v = gtp0.get3DVector().clone();
    v[0] += (gtp1.get3DVector()[0] - v[0]) * f;
    v[1] += (gtp1.get3DVector()[1] - v[1]) * f;
    v[2] += (gtp1.get3DVector()[2] - v[2]) * f;
    set(v, GeoTessUtils.normalize(v));
  }

	/**
	 * Creates and returns a new GeoTessPosition object whose coordinates are set
	 * to the fractional unit vector position f between the positions gtp0 and
	 * gtp1 (f=0 ==> unit vector = gtp0 unit vector). The radii are set in a
	 * similar fashion and the new position is set into this GeoTessPosition.
	 * 
	 * @param gtp0 The first GeoTessPosition.
	 * @param gtp1 The second GeoTessPosition.
	 * @param f    A fraction defining the amount of direction and radius to
	 *             apply from gtp0 to gtp1.
	 * @throws GeoTessException
	 */
	static public GeoTessPosition getGeoTessUnitVectorPosition(GeoTessPosition gtp0,
			                                      GeoTessPosition gtp1, double f)
			   throws GeoTessException
	{
		// create a new GeoTessPosition using the model and interpolator types of
		// gtp0 ... set its position ... and return it
		GeoTessPosition gtpf = GeoTessPosition.getGeoTessPosition(gtp0);
		gtpf.setIntermediateUnitVectorPosition(gtp0, gtp1, f);
		return gtpf;
	}

	/**
	 * Sets this GeoTessPosition to the fractional unit vector position f between
	 * the positions gtp0 and gtp1 (f=0 ==> unit vector = gtp0 unit vector). The
	 * radii are set in a similar fashion and the new position is set into this
	 * GeoTessPosition. 
	 * 
	 * @param gtp0 The first GeoTessPosition.
	 * @param gtp1 The second GeoTessPosition.
	 * @param f    A fraction defining the amount of direction and radius to
	 *             apply from gtp0 to gtp1.
	 * @throws GeoTessException
	 */
	public void setIntermediateUnitVectorPosition(GeoTessPosition gtp0,
			                                          GeoTessPosition gtp1, double f)
			        throws GeoTessException
  {
    double[] v = gtp0.getVector().clone();
    v[0] += (gtp1.getVector()[0] - v[0]) * f;
    v[1] += (gtp1.getVector()[1] - v[1]) * f;
    v[2] += (gtp1.getVector()[2] - v[2]) * f;
    GeoTessUtils.normalize(v);
    double r = (gtp1.getRadius() - gtp0.getRadius()) * f + gtp0.getRadius();
    set(v, r);
  }
	
	/**
	 * Sets this GeoTessPosition to the fractional distance f on a line from
	 * gtp0 to gtp1, and then moves the radius to the top of the layer given by
	 * layerId.
	 * 
	 * @param gtp0    The first GeoTessPosition.
	 * @param gtp1    The second GeoTessPosition.
	 * @param f       A fraction defining on 3D point on a line that passes
	 *                through gtp0 and gtp1. The new position is measured from
	 *                gtp0.
	 *        layerId The layer that the radius of this GeoTessPosition will be
	 *                set.
	 * @throws GeoTessException
	 */
	public void setIntermediatePosition(GeoTessPosition gtp0,
			                                GeoTessPosition gtp1, double f,
			                                int layerId)
			   throws GeoTessException
  {
		setIntermediatePosition(gtp0, gtp1, f);
		setRadius(layerId, getRadiusTop(layerId));
  }

	/**
	 * Move this gtp's unit vector in the direction of vtp by
	 * distance y. Then rotate the result around vtp by angle x.
	 * Set the position of this GeoTessPosition to the new position.
	 * Then set the radius to the radius of the specified layerId.
	 * 
	 * @param gtp 
	 * @param vtp
	 * @param x
	 * @param y
	 * @param layerId
	 * @throws GeoTessException
	 */
	public void move(GeoTessPosition gtp, double[] vtp, double x, double y,
			             int layerId) throws GeoTessException
  {
		double[] u = new double[3];
		Vector3D.move(gtp.getVector(), vtp, x, y, u);
    setTop(layerId, u);
  }

	/**
	 * Compute a unit vector that is distance d radians due north 
	 * of this GeoVector and return it in vtp. The current position is not  
	 * modified.
	 * 
	 * @param d    The distance North to move (radians).
	 * @param vtp  The new position
	 * @return true if operation successful, false if this is already at north
	 *         or south pole.
	 */
	public boolean move_north(double d, double[] vtp)
	{
		return VectorUnit.moveNorth(unitVector, d, vtp);
	}

	/**
	 * Sets this GeoTessPosition index to i.
	 *
	 * @param i New index value.
	 */
	public void setIndex(int i)
	{
		index = i;
	}

	/**
	 * Returns this GeoTessPosition index setting.
	 *
	 * @return The GeoTessPosition index setting.
	 */
	public int getIndex()
	{
		return index;
	}
}
