package gms.shared.utilities.geotess.util.numerical.polygon;


/**
 * A Horizon representing a constant fractional position with a specified layer
 * of the model.
 * @author sballar
 *
 */
public class HorizonLayer extends Horizon
{
	/**
	 * The fractional depth within a specified layer.  
	 * 0 will correspond to the bottom of the layer and
	 * 1 will correspond to the top of the layer.
	 */
	private double fraction;
	
	/**
	 * Constructor specifying a fractional radius within a layer of the model.
	 * 0 will correspond to the bottom of the layer and 1 to the top of the layer.
	 * @param fraction fractional position within a layer
	 * @param layerIndex the layer within which the radius will be constrained
	 */
	public HorizonLayer(double fraction, int layerIndex)
	{
		this.layerIndex = layerIndex;
		this.fraction = fraction < 0. ? 0. : fraction > 1. ? 1. : fraction;
	}

	@Override
	public double getRadius(double[] position, double[] layerRadii)
	{
		double bottom = layerRadii[layerIndex];
		if (fraction <= 0. )
			return bottom;
		double top = layerRadii[layerIndex+1];
		if (fraction >= 1.)
			return top;
		return bottom + fraction*(top-bottom);
	}

//	@Override
//	public double getRadius(double[] position, Profile[] profiles)
//	{
//		double bottom = profiles[layerIndex].getRadiusBottom();
//		if (fraction <= 0. )
//			return bottom;
//		double top = profiles[layerIndex].getRadiusTop();
//		if (fraction >= 1.)
//			return top;
//		return bottom + fraction*(top-bottom);
//	}
//
//	@Override
//	public double getRadius(GeoTessPosition position) throws GeoTessException
//	{
//		double bottom = position.getRadiusBottom(layerIndex);
//		if (fraction <= 0. )
//			return bottom;
//		double top = position.getRadiusTop(layerIndex);
//		if (fraction >= 1.)
//			return top;
//		return bottom + fraction*(top-bottom);
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
		return String.format("Layer %8.3f %3d", fraction, layerIndex);
	}

	@Override
	public double getValue()
	{
		return fraction;
	}

}
