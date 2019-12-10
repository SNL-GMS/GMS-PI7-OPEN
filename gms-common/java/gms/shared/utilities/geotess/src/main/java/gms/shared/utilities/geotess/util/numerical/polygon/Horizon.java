package gms.shared.utilities.geotess.util.numerical.polygon;

/**
 * Horizon is an abstract class that represents a single "surface" within a
 * model. This might be a surface of constant radius, constant depth, the top or
 * bottom of a layer, etc. The surface can be constrained to a specified layer,
 * or can cross layer boundaries.  There are derived classes for each of these, 
 * HorizonRadius, HorizonDepth, and HorizonLayer.  A Horizon class implements
 * a single basic function, getRadius().  That method can take either a 
 * GeoTessPosition object or a vertex position and the 1D array of 
 * Profiles associated with that vertex.
 * 
 * @author sballar
 * 
 */
public abstract class Horizon
{
	/**
	 * If layerIndex is >= 0 and < the number of layers represented in a model,
	 * then the returned radius will be constrained to be between the top and
	 * bottom of the specified layer.  Otherwise, the radius will not be 
	 * so constrained. 
	 */
	protected int layerIndex;

//	/**
//	 * Return the radius of the Horizon at the specified geographic position
//	 * and constrained by the specified array of Profiles, all of which are
//	 * assumed to reside at the specified position.
//	 * @param position the unit vector representing the position where the 
//	 * radius is to be determined.  This should correspond to the position
//	 * of the supplied array of Profiles.  Used only by HorizonDepth objects
//	 * to determine the radius of the Earth at the position of the Profiles.
//	 * Not used by HorizonLayer or HorizonRadius objects.
//	 * @param profiles a 1D array of profiles at the specified position.
//	 * The number of elements must be equal to the number of layers in the 
//	 * model with the first layer being the deepest (closest to the center
//	 * of the Earth) and the last layer being the shallowest (farthest from
//	 * the center of the Earth).
//	 * @return the radius of the Horizon at the specified position and 
//	 * perhaps constrained to reside in the specified layer.  Units are km.
//	 */
//	public abstract double getRadius(double[] position, Profile[] profiles);
//	
//	/**
//	 * Return the radius of the Horizon at the position of the specified 
//	 * GeoTessPosition object.
//	 * @param position
//	 * @return the radius of the Horizon at the specified position and 
//	 * perhaps constrained to reside in the specified layer.  Units are km.
//	 * @throws GeoTessException
//	 */
//	public abstract double getRadius(GeoTessPosition position) throws GeoTessException;

	/**
	 * Return the radius of the Horizon at the specified geographic position
	 * and constrained by the specified array of radii, all of which are
	 * assumed to reside at the specified position.
	 * @param position the unit vector representing the position where the 
	 * radius is to be determined.  This should correspond to the position
	 * of the supplied array of Profiles.  Used only by HorizonDepth objects
	 * to determine the radius of the Earth at the position of the Profiles.
	 * Not used by HorizonLayer or HorizonRadius objects.
	 * @param layerRadii a 1D array of radius values, in km, that specify the radii
	 * of the interfaces between layers, determined at the specified position.
	 * The number of elements must be equal to one plus the number of layers in the 
	 * model.  The first value is the radius of the bottom of the deepest layer
	 * (closest to the center of the Earth) and the last value is the radius of the 
	 * top of the last layer (farthest from the center of the Earth).
	 * @return the radius of the Horizon at the specified position and 
	 * perhaps constrained to reside in the specified layer.  Units are km.
	 */
	public abstract double getRadius(double[] position, double[] layerRadii);
	
	/**
	 * Retrieve the index of the layer that was specified at construction.  
	 * If >= 0 and < the number of layers in the model then the
	 * radius of this Horizon object will be constrained to be within the radii of 
	 * the top and bottom of this layer.  
	 * @return layer index, or -1.
	 */
	public abstract int getLayerIndex();

	public abstract double getValue();
	
}
