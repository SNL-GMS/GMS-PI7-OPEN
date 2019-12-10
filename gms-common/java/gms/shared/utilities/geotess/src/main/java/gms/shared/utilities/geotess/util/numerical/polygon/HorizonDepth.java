package gms.shared.utilities.geotess.util.numerical.polygon;

import gms.shared.utilities.geotess.util.numerical.vector.VectorGeo;


/**
 * A Horizon representing a constant depth in the model.
 * @author sballar
 *
 */
public class HorizonDepth extends Horizon
{
	
	/**
	 * The depth of this Horizon object relative to the surface of the 
	 * GRS80 ellipsoid, in km.
	 */
	private double depth;
	
	/**
	 * Constructor for a Horizon object that represents a constant 
	 * depth beneath the surface of the GRS80 ellipsoid.  Units are km.
	 * <p>Since the layerIndex is not specified, the depth is not
	 * constrained to be within any particular layer.
	 * @param depth depth below the surface of the GRS80 ellipsoid, in km.
	 */
	public HorizonDepth(double depth)
	{
		this.layerIndex = -1;
		this.depth = depth;
	}

	/**
	 * Constructor for a Horizon object that represents a constant 
	 * depth beneath the surface of the GRS80 ellipsoid.  Units are km.
	 * <p>Since the layerIndex is specified, the depth will be
	 * constrained to be within the specified layer.
	 * @param depth depth below the surface of the GRS80 ellipsoid, in km.
	 * @param layerIndex the index of the layer within which 
	 * the depth will be constrained.
	 */
	public HorizonDepth(double depth, int layerIndex)
	{
		this.layerIndex = layerIndex;
		this.depth = depth;
	}

	@Override
	public double getRadius(double[] position, double[] layerRadii)
	{
		double radius = VectorGeo.getEarthRadius(position)-depth;
		if (layerIndex < 0)
			return radius;
		double bottom = layerRadii[layerIndex];
		if (radius <= bottom)
			return bottom;
		double top = layerRadii[layerIndex+1];
		if (radius >= top)
			return top;
		return radius;
	}

//	@Override
//	public double getRadius(double[] position, Profile[] profiles)
//	{
//		double radius = GeoTessUtils.getEarthRadius(position)-depth;
//		if (layerIndex < 0)
//			return radius;
//		double bottom = profiles[layerIndex].getRadiusBottom();
//		if (radius <= bottom)
//			return bottom;
//		double top = profiles[layerIndex].getRadiusTop();
//		if (radius >= top)
//			return top;
//		return radius;
//	}
//
//	@Override
//	public double getRadius(GeoTessPosition position) throws GeoTessException
//	{
//		double radius = position.getEarthRadius()-depth;
//		if (layerIndex < 0)
//			return radius;
//		double bottom = position.getRadiusBottom(layerIndex);
//		if (radius <= bottom)
//			return bottom;
//		double top = position.getRadiusTop(layerIndex);
//		if (radius >= top)
//			return top;
//		return radius;
//	}

	@Override
	public int getLayerIndex()
	{
		return layerIndex;
	}
	
	@Override
	public
	String toString()
	{
		return String.format("Depth %8.3f %3d", depth, layerIndex);
	}

	@Override
	public double getValue()
	{
		return depth;
	}

}
