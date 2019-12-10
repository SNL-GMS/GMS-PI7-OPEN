package gms.shared.utilities.geotess;

import gms.shared.utilities.geotess.util.containers.arraylist.ArrayListInt;
import gms.shared.utilities.geotess.util.globals.InterpolatorType;
import gms.shared.utilities.geotess.util.numerical.polygon.Horizon;
import gms.shared.utilities.geotess.util.numerical.polygon.Polygon;
import gms.shared.utilities.geotess.util.numerical.polygon.Polygon3D;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;

/**
 * PointMap facilitates access to information in the model by providing a map
 * between point IDs, vertex IDs, layer IDs and node IDs.
 * <ul>
 * First some definitions:
 * <li><i>vertex</i> refers to a position in the 2D tessellation. They are 2D
 * positions represented by unit vectors on a unit sphere. Vertices are managed
 * in the GeoTessmodel.getGrid() object which can be accessed via model.getGrid().
 * <li><i>layer</i> refers to one of the layers in the model, such as the core,
 * mantle or crust. Layers are defined in the GeoTessMetaData object which can
 * be accessed from model.getMetaData().
 * <li><i>node</i> refers to a Data object on a radial Profile associated with a
 * vertex and a layer in the model. Node indexes are unique only within a given
 * Profile (all Profile have a node with index 0 for example).
 * <li><i>point</i> refers to all the nodes in all the Profile of the model.
 * There is only one 'point' in the model with index 0.
 * </ul>
 * 
 * PointMap is introduced to help manage all these different indexes. It is
 * fundamentally a 2D array of ints with dimensions nPoints x 3. For each point,
 * the 3 ints are (0) the vertex index, (1) the layer index, and (2) the node
 * index.
 * <p>
 * It is important to remember that the number of nodes in each Profile
 * corresponds to the number of Data objects associated with the Profile, not
 * the number of radii.
 * <ul>
 * Number of nodes in each type of Profile:
 * <li>empty: 0
 * <li>thin:1
 * <li>constant: 1
 * <li>npoint: n
 * <li>surface: 1
 * </ul>
 * 
 * @author sballar
 * 
 */
public class PointMap
{
//	private GeoTessMetaData metaData;
//
//	private GeoTessmodel.getGrid() model.getGrid();
//
//	private Profile[][] model.getProfiles();
	
	private GeoTessModel model;

	private boolean populated;

	/**
	 * <ol start=0>For every point in the model, pointMap contains an int[3]
	 * where the elements are 
	 * <li>vertexIndex
	 * <li>layerIndex
	 * <li>nodeIndex
	 * </ol>
	 */
	private ArrayList<int[]> pointMap;

	/**
	 * Map of entry index in pointMap to its global point index
	 */
	private ArrayListInt      globalPointMap  = null;

	/**
	 * Set to true if the point map was filled using all model.getGrid() points (GLOBAL).
	 * This is set for a global active region to avoid creating the
	 * globalPointMap container which would simply set the ith entry to i.
	 */
	private boolean           populatedGlobal = false;

	private Polygon polygon;

	/**
	 * Constructor.  PointMap is initialized but not populated by 
	 * this method.
	 */
	protected PointMap(GeoTessModel model)
	{
		this.model = model;
		pointMap = new ArrayList<int[]>();
		populated = false;
		polygon = null;
	}

	private void clear()
	{
		pointMap.clear();

		polygon = null;

		populated = populatedGlobal = false;
		if (globalPointMap != null) globalPointMap.clear();

		Profile[] pp;
		for (int vertex = 0; vertex < model.getNVertices(); ++vertex)
		{
			pp = model.getProfiles()[vertex];
			for (int layer = 0; layer < model.getMetaData().getNLayers(); ++layer)
				pp[layer].resetPointIndices();
		}
	}

	/**
	 * Populates the PointMap such that every node in the entire
	 * model is within the active region.  
	 */
	protected void setActiveRegion()
	{
		clear();

		Profile[] pp;
		Profile p;
		for (int vertex = 0; vertex < model.getNVertices(); ++vertex)
		{
			pp = model.getProfiles()[vertex];
			for (int layer = 0; layer < model.getMetaData().getNLayers(); ++layer)
			{
				p = pp[layer];
				for (int node = 0; node < p.getNData(); ++node)
				{
					p.setPointIndex(node, pointMap.size());
					pointMap.add(new int[] {vertex, layer, node});
				}
			}
		}
		populated = populatedGlobal = true;
	}

	protected void setActiveRegion(File polygonFile) throws IOException
	{
		if (polygonFile.getName().toLowerCase().endsWith(".kml") || 
				polygonFile.getName().toLowerCase().endsWith(".kmz"))
			setActiveRegion(new Polygon(polygonFile));
		else 
		{
			Scanner input = new Scanner(polygonFile);
			String firstLine = input.nextLine();
			input.close();
			if (firstLine.equalsIgnoreCase("polygon3d"))
				setActiveRegion(new Polygon3D(polygonFile));
			else if (firstLine.equalsIgnoreCase("polygon"))
				setActiveRegion(new Polygon(polygonFile));
			else 
				throw new IOException("\nFile "+polygonFile.getCanonicalPath()
						+"\ndoes not appear to be a polygon file.");
		}

	}

	/**
	 * Populate the PointMap such that all nodes associated with
	 * model.getProfiles() attached to model.getGrid() vertices that are within the 
	 * 2D polygon are active.
	 * @param polygon a 2D Polygon object
	 * @throws PolygonException
	 */
	protected void setActiveRegion(Polygon polygon)
	{
		if (polygon == null)
		{
			setActiveRegion();
			return;
		}
		
		clear();

		this.polygon = polygon;

		Profile[] pp;
		Profile p;

		if (polygon instanceof Polygon3D)
		{
			Horizon bottom = ((Polygon3D)polygon).getBottom();
			Horizon top = ((Polygon3D)polygon).getTop();
			double rBottom, rTop;
			double[] layerRadii = new double[model.getMetaData().getNLayers()+1];
			for (int vertex = 0; vertex < model.getGrid().getNVertices(); ++vertex)
				if (polygon.contains(model.getGrid().getVertex(vertex)))
				{
					pp = model.getProfiles()[vertex];
					layerRadii[0] = pp[0].getRadiusBottom();
					for (int l=0; l<model.getMetaData().getNLayers(); ++l)
						layerRadii[l+1] = pp[l].getRadiusTop();
					rBottom = bottom.getRadius(model.getGrid().getVertex(vertex), layerRadii);
					rTop = top.getRadius(model.getGrid().getVertex(vertex), layerRadii);
					for (int layer = bottom.getLayerIndex(); layer <= top.getLayerIndex(); ++layer)
					{
						p = pp[layer];
						for (int node = 0; node < p.getNData(); ++node)
							if (p.getRadius(node) >= rBottom && p.getRadius(node) <= rTop)
							{
								p.setPointIndex(node, pointMap.size());
								pointMap.add(new int[] {vertex, layer, node});
							}
					}
				}
		}
		else
		{
			for (int vertex = 0; vertex < model.getGrid().getNVertices(); ++vertex)
				if (polygon.contains(model.getGrid().getVertex(vertex)))
				{
					pp = model.getProfiles()[vertex];
					for (int layer = 0; layer < model.getMetaData().getNLayers(); ++layer)
					{
						p = pp[layer];
						for (int node = 0; node < p.getNData(); ++node)
						{
							p.setPointIndex(node, pointMap.size());
							pointMap.add(new int[] {vertex, layer, node});
						}
					}
				}
		}
		populated = true;
	}

	/**
	 * Populate the PointMap such that nodes located within the 
	 * 3D Polygon are active and all others are inactive.
	 * @param polygon a 3D Polygon object
	 * @throws PolygonException
	 */
	protected void setActiveRegion(Polygon3D polygon)
	{
		clear();

		this.polygon = polygon;

		Profile[] pp;
		Profile p;
		double rBottom, rTop;
		double[] layerRadii = new double[model.getMetaData().getNLayers()+1];
		for (int vertex = 0; vertex < model.getGrid().getNVertices(); ++vertex)
		{
			if (polygon.contains(model.getGrid().getVertex(vertex)))
			{
				pp = model.getProfiles()[vertex];
				layerRadii[0] = pp[0].getRadiusBottom();
				for (int l=0; l<model.getMetaData().getNLayers(); ++l)
					layerRadii[l+1] = pp[l].getRadiusTop();
				rBottom = polygon.getBottom().getRadius(model.getGrid().getVertex(vertex), layerRadii);
				rTop = polygon.getTop().getRadius(model.getGrid().getVertex(vertex), layerRadii);
				for (int layer = polygon.getBottom().getLayerIndex(); layer <= polygon.getTop().getLayerIndex(); ++layer)
				{
					p = pp[layer];
					for (int node = 0; node < p.getNData(); ++node)
						if (p.getRadius(node) >= rBottom && p.getRadius(node) <= rTop)
						{
							p.setPointIndex(node, pointMap.size());
							pointMap.add(new int[] {vertex, layer, node});
						}
				}
			}
		}
		populated = true;
	}

	/**
	 * Retrieve the number of points supported by this model.
	 * If PointMap has not been populated, then it is populated
	 * with all points in the model.
	 * 
	 * @return the number of points supported by this model.
	 */
	public int size()
	{
		if (!populated)
			synchronized(this)
			{
				if (!populated)
					setActiveRegion();
			}
		return pointMap.size();
	}

	/**
	 * Returns true if this PointMap and otherPointMap are equal.
	 * To be equal, the model.getGrid()s on which they rely must be equal and
	 * all the radii and data values on every Profile must be equal
	 * as well.
	 * @param otherPointMap 
	 * @return true if they are equal.
	 * @throws Error if other PointMap is not populated.
	 */
	@Override
	public boolean equals(Object otherPointMap)
	{
		if (otherPointMap != null && otherPointMap instanceof PointMap)
		{
			PointMap pm = (PointMap)otherPointMap;

			if (!pm.populated)
				throw new java.lang.Error("\nother is not populated.\n" +
						"Call other.setActiveRegion() to populate it.");

			if (!populated) setActiveRegion();

			if (pm.model.getGrid().equals(model.getGrid()) && pm.model.getProfiles()[0].length == model.getProfiles()[0].length)
			{
				for (int vertex=0; vertex<model.getProfiles().length; ++vertex)
					for (int layer=0; layer<model.getProfiles()[vertex].length; ++layer)
						if (!pm.model.getProfiles()[vertex][layer].equals(model.getProfiles()[vertex][layer]))
							return false;
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Find the pointIndex of the point in this PointMap that is closest to the 
	 * supplied location.  Will return -1 if the closest [vertex, layerIndex, nodeIndex]
	 * is not in the current pointMap.
	 * @param location
	 * @param radius 
	 * @param layerIndex
	 * @return the pointIndex of the point in this PointMap that is closest to the 
	 * supplied location. Will return -1 if the closest [vertex, layerIndex, nodeIndex]
	 * is not in the current pointMap.
	 * @throws GeoTessException 
	 */
	public int findClosestPoint(double[] location, double radius, int layerIndex) throws GeoTessException
	{
		GeoTessPosition pos = model.getGeoTessPosition(InterpolatorType.LINEAR, InterpolatorType.LINEAR);
		pos.set(layerIndex, location, radius);
		HashMap<Integer, Double> weights = new HashMap<Integer, Double>();
		pos.getWeights(weights, 1.);
		int closestPoint = -1;
		double maxWeight = -1;
		for (Map.Entry<Integer, Double> entry : weights.entrySet())
			if (entry.getValue() > maxWeight)
			{
				maxWeight = entry.getValue();
				closestPoint = entry.getKey();
			}
		return closestPoint;
	}

	/**
	 * Retrieve the index of the vertex that corresponds to the specified
	 * pointIndex.
	 * 
	 * @param pointIndex
	 * @return the index of the vertex that corresponds to the specified
	 *         pointIndex.
	 */
	public int getVertexIndex(int pointIndex)
	{
		return pointMap.get(pointIndex)[0];
	}

	/**
	 * Retrieve the index of the tessellation that corresponds to the specified
	 * pointIndex.
	 * 
	 * @param pointIndex
	 * @return the index of the tessellation that corresponds to the specified
	 *         pointIndex.
	 */
	public int getTessId(int pointIndex)
	{
		return model.getMetaData().getTessellation(pointMap.get(pointIndex)[1]);
	}

	/**
	 * Retrieve the index of the layer that corresponds to the specified
	 * pointIndex.
	 * 
	 * @param pointIndex
	 * @return the index of the layer that corresponds to the specified
	 *         pointIndex.
	 */
	public int getLayerIndex(int pointIndex)
	{
		return pointMap.get(pointIndex)[1];
	}

	/**
	 * Retrieve the index of the node that corresponds to the specified
	 * pointIndex.
	 * 
	 * @param pointIndex
	 * @return the index of the node that corresponds to the specified
	 *         pointIndex.
	 */
	public int getNodeIndex(int pointIndex)
	{
		return pointMap.get(pointIndex)[2];
	}

	/**
	 * Retrieve a reference to the index map for the specified pointIndex. This
	 * is a 3-element array consisting of 0:vertexIndex, 1:layerIndex,
	 * 2:nodeIndex.
	 * 
	 * @param pointIndex
	 * @return the index map for the specified pointIndex.
	 */
	public int[] getPointIndices(int pointIndex)
	{
		return pointMap.get(pointIndex);
	}

	/**
	 * Retrieve the pointIndex of the point that corresponds to the specified
	 * vertex, layer and node.
	 * 
	 * @param vertex
	 * @param layer
	 * @param node
	 * @return the pointIndex of the point that corresponds to the specified
	 *         vertex, layer and node.
	 */
	public int getPointIndex(int vertex, int layer, int node)
	{
		if (vertex < 0 || layer < 0 || node < 0) return -1;
		if (!populated) setActiveRegion();
		return model.getProfiles()[vertex][layer].getPointIndex(node);
	}

	/**
	 * Retrieve the pointIndex of the point that corresponds to the last node
	 * in profile[vertex][layer].  The last node is the one with the largest
	 * radius (i.e., the shallowest node).
	 * 
	 * @param vertex
	 * @param layer
	 * @return the pointIndex of the point that corresponds to the last node
	 * in profile[vertex][layer].
	 */
	public int getPointIndexLast(int vertex, int layer)
	{
		if (vertex < 0 || layer < 0) return -1;
		if (!populated) setActiveRegion();
		Profile p = model.getProfiles()[vertex][layer];
		return p.getPointIndex(p.getNData()-1);
	}

	/**
	 * Returns the number of point indexes defined by profile[vertex][layer].
	 * 
	 * @param vertex
	 * @param layer
	 * @return The number of point indexes defined by profile[vertex][layer].
	 */
	public int getPointIndexCount(int vertex, int layer)
	{
		if (vertex < 0 || layer < 0) return -1;
		if (!populated) setActiveRegion();
		return model.getProfiles()[vertex][layer].getNData();
	}

	/**
	 * Retrieve the pointIndex of the point that corresponds to the first node
	 * in profile[vertex][layer].  The first node is the one with the smallest
	 * radius (i.e., the deepest node).
	 * 
	 * @param vertex
	 * @param layer
	 * @return the pointIndex of the point that corresponds to the first node
	 * in profile[vertex][layer].
	 */
	public int getPointIndexFirst(int vertex, int layer)
	{
		if (vertex < 0 || layer < 0) return -1;
		if (!populated) setActiveRegion();
		return model.getProfiles()[vertex][layer].getPointIndex(0);
	}

	/**
	 * Returns the global point index given the input point index. The global
	 * point index is the point index that would have been assigned to this
	 * point if the active region had been set to global. If the current active
	 * region is global then the input pointIndex is returned. 
	 * 
	 * @param pointIndex The current point index for which the global point
	 *                   index is returned.
	 * @return The equivalent global point index of the input point index.
	 */
    public int getGlobalPointIndex(int pointIndex)
    {
    	if (!populated) setActiveRegion();
    	if (!populatedGlobal)
    	{
		    if ((globalPointMap == null) || (globalPointMap.size() == 0))
		        buildGlobalPointMap();
		    return globalPointMap.get(pointIndex);
    	}
    	return pointIndex;
    }

    /**
     * Constructs the point index to global point index map. Each entry in the
     * global point index map (globalPointMap) contains its associated global
     * point index (i.e. the index the point would have had assigned if the
     * active region were set to global. If the current active region is global
     * this map is not constructed (redundant).
     */
    private void buildGlobalPointMap()
    {
		Profile[] pp;
		Profile p;

		// create a global point map if it is not instantiated and set its size
		// to the number of entries in the point map

		if (globalPointMap == null)
			globalPointMap = new ArrayListInt(pointMap.size());
		globalPointMap.setSize(pointMap.size());

		// set the global index counter to zero and loop over all points
		// (vertices, layers, and nodes)

		int globalIndex = 0;
		for (int vertex = 0; vertex < model.getGrid().getNVertices(); ++vertex)
		{
			pp = model.getProfiles()[vertex];
			for (int layer = 0; layer < model.getMetaData().getNLayers(); ++layer)
			{
				p = pp[layer];
				for (int node = 0; node < p.getNData(); ++node)
				{
					// get the point index and see if it was previously defined
					// if true, then set its entry in the global map to the
					// global index

					int pntIndex = p.getPointIndex(node);
					if (pntIndex >= 0)
						globalPointMap.set(pntIndex,  globalIndex);

					// increment the global index and continue

					++globalIndex;
				}
			}
		}
    }

	/**
	 * Set the value of the specified attribute at the specified point to the
	 * specified value.
	 * 
	 * @param pointIndex
	 * @param attributeIndex
	 * @param value
	 */
	public void setPointValue(int pointIndex, int attributeIndex, double value)
	{
		int[] map = pointMap.get(pointIndex);
		model.getProfiles()[map[0]][map[1]].getData(map[2]).setValue(attributeIndex, value);
	}

	/**
	 * Set the value of the specified attribute at the specified point to the
	 * specified value.
	 * 
	 * @param pointIndex
	 * @param attributeIndex
	 * @param value
	 */
	public void setPointValue(int pointIndex, int attributeIndex, float value)
	{
		int[] map = pointMap.get(pointIndex);
		model.getProfiles()[map[0]][map[1]].getData(map[2]).setValue(attributeIndex, value);
	}

	/**
	 * Set the value of the specified attribute at the specified point to the
	 * specified value.
	 * 
	 * @param pointIndex
	 * @param attributeIndex
	 * @param value
	 */
	public void setPointValue(int pointIndex, int attributeIndex, long value)
	{
		int[] map = pointMap.get(pointIndex);
		model.getProfiles()[map[0]][map[1]].getData(map[2]).setValue(attributeIndex, value);
	}

	/**
	 * Set the value of the specified attribute at the specified point to the
	 * specified value.
	 * 
	 * @param pointIndex
	 * @param attributeIndex
	 * @param value
	 */
	public void setPointValue(int pointIndex, int attributeIndex, int value)
	{
		int[] map = pointMap.get(pointIndex);
		model.getProfiles()[map[0]][map[1]].getData(map[2]).setValue(attributeIndex, value);
	}

	/**
	 * Set the value of the specified attribute at the specified point to the
	 * specified value.
	 * 
	 * @param pointIndex
	 * @param attributeIndex
	 * @param value
	 */
	public void setPointValue(int pointIndex, int attributeIndex, short value)
	{
		int[] map = pointMap.get(pointIndex);
		model.getProfiles()[map[0]][map[1]].getData(map[2]).setValue(attributeIndex, value);
	}

	/**
	 * Set the value of the specified attribute at the specified point to the
	 * specified value.
	 * 
	 * @param pointIndex
	 * @param attributeIndex
	 * @param value
	 */
	public void setPointValue(int pointIndex, int attributeIndex, byte value)
	{
		int[] map = pointMap.get(pointIndex);
		model.getProfiles()[map[0]][map[1]].getData(map[2]).setValue(attributeIndex, value);
	}

	/**
	 * Replace the Data object associated with the specified point
	 * 
	 * @param pointIndex
	 * @param data
	 */
	public void setPointData(int pointIndex, Data data)
	{
		int[] map = pointMap.get(pointIndex);
		model.getProfiles()[map[0]][map[1]].setData(map[2], data);
	}

	/**
	 * Retrieve the value of the specified attribute at the specified point.
	 * 
	 * @param pointIndex
	 * @param attributeIndex
	 * @return the value of the specified attribute at the specified point.
	 * @deprecated use one of getPointValueDouble, getPointValueFloat, 
	 * getPointValueLong, getPointValueInt, getPointValueShort or 
	 * getPointValueByte instead.
	 */
	@Deprecated
	public double getPointValue(int pointIndex, int attributeIndex)
	{
		int[] map = pointMap.get(pointIndex);
		return model.getProfiles()[map[0]][map[1]].getData(map[2]).getDouble(
				attributeIndex);
	}

	/**
	 * Retrieve the value of the specified attribute at the specified point.
	 * 
	 * @param pointIndex
	 * @param attributeIndex
	 * @return the value of the specified attribute at the specified point.
	 */
	public double getPointValueDouble(int pointIndex, int attributeIndex)
	{
		if (pointIndex < 0) return Double.NaN;
		int[] map = pointMap.get(pointIndex);
		return model.getProfiles()[map[0]][map[1]].getData(map[2]).getDouble(
				attributeIndex);
	}

	/**
	 * Retrieve the value of the specified attribute at the specified point.
	 * 
	 * @param pointIndex
	 * @param attributeIndex
	 * @return the value of the specified attribute at the specified point.
	 */
	public float getPointValueFloat(int pointIndex, int attributeIndex)
	{
		if (pointIndex < 0) return Float.NaN;
		int[] map = pointMap.get(pointIndex);
		return model.getProfiles()[map[0]][map[1]].getData(map[2]).getFloat(
				attributeIndex);
	}

	/**
	 * Retrieve the value of the specified attribute at the specified point.
	 * 
	 * @param pointIndex
	 * @param attributeIndex
	 * @return the value of the specified attribute at the specified point.
	 */
	public long getPointValueLong(int pointIndex, int attributeIndex)
	{
		int[] map = pointMap.get(pointIndex);
		return model.getProfiles()[map[0]][map[1]].getData(map[2]).getLong(
				attributeIndex);
	}

	/**
	 * Retrieve the value of the specified attribute at the specified point.
	 * 
	 * @param pointIndex
	 * @param attributeIndex
	 * @return the value of the specified attribute at the specified point.
	 */
	public int getPointValueInt(int pointIndex, int attributeIndex)
	{
		int[] map = pointMap.get(pointIndex);
		return model.getProfiles()[map[0]][map[1]].getData(map[2]).getInt(
				attributeIndex);
	}

	/**
	 * Retrieve the value of the specified attribute at the specified point.
	 * 
	 * @param pointIndex
	 * @param attributeIndex
	 * @return the value of the specified attribute at the specified point.
	 */
	public short getPointValueShort(int pointIndex, int attributeIndex)
	{
		int[] map = pointMap.get(pointIndex);
		return model.getProfiles()[map[0]][map[1]].getData(map[2]).getShort(
				attributeIndex);
	}

	/**
	 * Retrieve the value of the specified attribute at the specified point.
	 * 
	 * @param pointIndex
	 * @param attributeIndex
	 * @return the value of the specified attribute at the specified point.
	 */
	public byte getPointValueByte(int pointIndex, int attributeIndex)
	{
		int[] map = pointMap.get(pointIndex);
		return model.getProfiles()[map[0]][map[1]].getData(map[2]).getByte(
				attributeIndex);
	}

	/**
	 * Retrieve the Data object associated with the specified point.
	 * @param pointIndex
	 * @return the Data object associated with the specified point.
	 */
	public Data getPointData(int pointIndex)
	{
		int[] map = pointMap.get(pointIndex);
		return model.getProfiles()[map[0]][map[1]].getData(map[2]);
	}

	/**
	 * Return true if the value of the specified attribute at the specified
	 * point is NaN.
	 * 
	 * @param pointIndex
	 * @param attributeIndex
	 * @return true if the value of the specified attribute at the specified
	 *         point is NaN.
	 */
	public boolean isNaN(int pointIndex, int attributeIndex)
	{
		int[] map = pointMap.get(pointIndex);
		return model.getProfiles()[map[0]][map[1]].isNaN(map[2], attributeIndex);
	}

	/**
	 * Retrieve a vector representation of the specified point (not a unit
	 * vector). The length of the vector is in km. This is a new double[], not a
	 * reference to an existing variable.
	 * 
	 * @param pointIndex
	 * @return a vector representation of the specified point
	 */
	public double[] getPointVector(int pointIndex)
	{
		int[] map = pointMap.get(pointIndex);
		double[] v = model.getGrid().getVertex(map[0]).clone();
		double r = model.getProfiles()[map[0]][map[1]].getRadius(map[2]);
		v[0] *= r;
		v[1] *= r;
		v[2] *= r;
		return v;
	}

	/**
	 * Retrieve a reference to the unit vector for the specified point.
	 * 
	 * @param pointIndex
	 * @return a reference to the unit vector for the specified point.
	 */
	public double[] getPointUnitVector(int pointIndex)
	{
		return model.getGrid().getVertex(pointMap.get(pointIndex)[0]);
	}

	/**
	 * Retrieve the radius of the specified point.
	 * 
	 * @param pointIndex
	 * @return radius of specified point, in km.
	 */
	public double getPointRadius(int pointIndex)
	{
		int[] map = pointMap.get(pointIndex);
		return model.getProfiles()[map[0]][map[1]].getRadius(map[2]);
	}

	/**
	 * Retrieve the radius of the specified point.
	 * 
	 * @param pointIndex
	 * @return radius of specified point, in km.
	 */
	public double getPointDepth(int pointIndex)
	{
		int[] map = pointMap.get(pointIndex);
		return model.getEarthShape().getEarthRadius(model.getGrid().getVertex(pointMap.get(pointIndex)[0]))
				-model.getProfiles()[map[0]][map[1]].getRadius(map[2]);
	}

	/**
	 * Retrieve the straight-line distance between two points in km.
	 * 
	 * @param pointIndex1
	 * @param pointIndex2
	 * @return the straight-line distance between two points in km.
	 */
	public double getDistance3D(int pointIndex1, int pointIndex2)
	{
		int[] m1 = pointMap.get(pointIndex1);
		int[] m2 = pointMap.get(pointIndex2);
		return GeoTessUtils.getDistance3D(model.getGrid().getVertex(m1[0]),
				model.getProfiles()[m1[0]][m1[1]].getRadius(m1[2]), model.getGrid().getVertex(m2[0]),
				model.getProfiles()[m2[0]][m2[1]].getRadius(m2[2]));
	}

	/**
	 * Find all the points that are first-order neighbors of the specified
	 * point. First, find all the vertices that are first order neighbors of the
	 * vertex of the supplied point (vertices connected by a single triangle
	 * edge). For each of those vertices, find the Profile that occupies the
	 * same layer and find the index of the radius in that Profile that is
	 * closest to the radius of the supplied point. Build the set of all such
	 * node index values. Finally, convert the node indexes to point indexes.
	 * There will generally be 6 such points, but that number is not guaranteed.
	 * 
	 * @param pointIndex
	 * @return the pointIndexes that are first order neighbors of the vertex of
	 *         the supplied point.
	 */
	public HashSet<Integer> getPointNeighbors(int pointIndex)
	{
		// find the vertexID, layerID and nodeID of point in question
		int[] map = pointMap.get(pointIndex);
		int vertex = map[0];
		int layer = map[1];
		int node = map[2];

		// find the vertex neighbors and radius of point in question
		
		HashSet<Integer> vertexNeighbors = getVertexNeighbors(vertex, layer);
		double radius = model.getProfiles()[vertex][layer].getRadius(node);

		HashSet<Integer> pointNeighbors = new HashSet<Integer>();

		Profile p;
		int ptId;
		for (Integer vtx : vertexNeighbors)
		{
			p = model.getProfiles()[vtx][layer];
			ptId = p.getPointIndex(p.findClosestRadiusIndex(radius));
			if (ptId > 0)
				pointNeighbors.add(ptId);
		}
		return pointNeighbors;
	}

	/**
	 * Find the indexes of all the points in the neighborhodd of the specified
	 * point.  In the most general case, for a point with 6 vertex neighbors, 
	 * there will be 14 such points.  The points are added to the suppled set,
	 * without clearing the set first.
	 * 
	 * @param pointIndex
	 * @param neighborhood the set of point indeces to which the neighborhood
	 * points are to be added.
	 */
	public void getPointNeighborhood(int pointIndex, HashSet<Integer> neighborhood)
	{
		// find the vertexID, layerID and nodeID of point in question
		int[] map = pointMap.get(pointIndex);
		int vertex = map[0];
		int layer = map[1];
		int node = map[2];
		
		Profile p = model.getProfile(vertex, layer);
		if (node > 0 && p.getPointIndex(node-1) > 0)
			neighborhood.add(p.getPointIndex(node-1));
		if (node < p.getNData()-1 && p.getPointIndex(node+1) > 0)
			neighborhood.add(p.getPointIndex(node+1));

		// find the vertex neighbors and radius of point in question
		HashSet<Integer> vertexNeighbors = getVertexNeighbors(vertex, layer);
		float radius = model.getProfiles()[vertex][layer].getRadii()[node];
		
		for (Integer vtx : vertexNeighbors)
			model.getProfiles()[vtx][layer].getPointIndices(radius, neighborhood);
	}

	/**
	 * Return all the vertices that are first-order neighbors of the specified
	 * point. There will generally be 6 such points, but that number is not
	 * guaranteed.
	 * 
	 * @param pointIndex
	 * @return the vertex Indexes that are first order neighbors of the vertex
	 *         of the input point.
	 */
	public HashSet<Integer> getVertexNeighbors(int pointIndex)
	{
		// find the vertexID, layerID and nodeID of point in question
		int[] map = pointMap.get(pointIndex);
		int vertex = map[0];
		int layer = map[1];

		return getVertexNeighbors(vertex, layer);
	}

	/**
	 * Return all the vertices that are first-order neighbors of the specified
	 * vertex for the specified layer. There will generally be 6 such points,
	 * but that number is not guaranteed.
	 * 
	 * @param vertex
	 * @param layer
	 * @return the vertex Indexes that are first order neighbors of the vertex
	 *         of the input point.
	 */
	public HashSet<Integer> getVertexNeighbors(int vertex, int layer)
	{
		// find the tessID and levelID
		int tessid = model.getMetaData().getTessellation(layer);
		int level = model.getGrid().getNLevels(tessid) - 1;

		// return the vertex neighbors
		return model.getGrid().getVertexNeighbors(tessid, level, vertex);
	}

	/**
	 * Retrieve nicely formated string with lat, lon of the point in degrees.
	 * @param pointIndex
	 * @return string with lat, lon in degrees.
	 */
	public String getPointLatLonString(int pointIndex)
	{
		return model.getEarthShape().getLatLonString(getPointUnitVector(pointIndex));
	}

	/**
	 * Returns the points latitude in degrees.
	 * 
	 * @param pointIndex
	 * @return The points latitude in degrees.
	 */
	public double getPointLatitudeDegrees(int pointIndex)
	{
		return model.getEarthShape().getLatDegrees(getPointUnitVector(pointIndex));
	}

	/**
	 * Returns the points longitude in degrees.
	 * 
	 * @param pointIndex
	 * @return The points longitude in degrees.
	 */
	public double getPointLongitudeDegrees(int pointIndex)
	{
		return model.getEarthShape().getLonDegrees(getPointUnitVector(pointIndex));
	}

	/**
	 * Retrieve nicely formated string with lat, lon of the point in degrees.
	 * @param pointIndex
	 * @param precision number of digits to right of decimal point
	 * @return string with lat, lon in degrees.
	 */
	public String getPointLatLonString(int pointIndex, int precision)
	{
		return model.getEarthShape().getLatLonString(getPointUnitVector(pointIndex), precision);
	}

	/**
	 * Retrieve a nicely formated string with lat, lon, depth of the point in degrees and km.
	 * @param pointIndex
	 * @return string with lat, lon, depth
	 */
	public String toString(int pointIndex)
	{
		return String.format("%s %8.3f", model.getEarthShape().getLatLonString(getPointUnitVector(pointIndex)),
				getPointDepth(pointIndex));
	}

	/**
	 * Determine whether or not this PointMap is populated.
	 * @return true if populated
	 */
	public boolean isPopulated()
	{
		return populated;
	}

	/**
	 * Retrieve a reference to the current Polygon, or null if no Polygon has been set.
	 * @return a reference to the current Polygon, or null if no Polygon has been set.
	 */
	public Polygon getPolygon()
	{
		return polygon;
	}

}
