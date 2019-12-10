package gms.shared.utilities.geotess.util.numerical.polygon;


/**
 * A Horizon representing a constant radial position in the model.
 * @author sballar
 *
 */
public class HorizonRadius extends Horizon
{

	/**
	 * The radius in the model, in km.
	 */
	private double radius;
	
	/**
	 * Constructor for a Horizon object that represents a constant 
	 * radius within the Earth.  Units are km.
	 * <p>Since the layerIndex is not specified, the radius is not
	 * constrained to be within any particular layer.
	 * @param radius radius in km.
	 */
	public HorizonRadius(double radius)
	{
		this.layerIndex = -1;
		this.radius = radius;
	}

	/**
	 * Constructor for a Horizon object that represents a constant 
	 * radius in the Earth, in km.
	 * <p>Since the layerIndex is specified, the radius will be
	 * constrained to be within the specified layer.
	 * @param radius radius within the Earth, in km.
	 * @param layerIndex the index of the layer within which 
	 * the radius will be constrained.
	 */
	public HorizonRadius(double radius, int layerIndex)
	{
		this.layerIndex = layerIndex;
		this.radius = radius;
	}

	@Override
	public double getRadius(double[] position, double[] layerRadii)
	{
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
//		if (layerIndex < 0)
//			return radius;
//		double bottom = position.getRadiusBottom(layerIndex);
//		if (radius <= bottom)
//			return bottom;
//		double top = position.getRadiusTop(layerIndex);
//		if (radius >= top)
//			return top;
//		return radius;
//		
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
		return String.format("Radius %8.3f %3d", radius, layerIndex);
	}

	@Override
	public double getValue()
	{
		return radius;
	}

}
