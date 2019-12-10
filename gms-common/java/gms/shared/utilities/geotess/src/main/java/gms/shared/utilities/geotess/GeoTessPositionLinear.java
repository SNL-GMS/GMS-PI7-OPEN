package gms.shared.utilities.geotess;

import gms.shared.utilities.geotess.util.containers.arraylist.ArrayListDouble;
import gms.shared.utilities.geotess.util.containers.arraylist.ArrayListInt;
import gms.shared.utilities.geotess.util.globals.InterpolatorType;

import java.util.ArrayList;

/**
 * Perform linear interpolation for an arbitrary point in the 3D model. First
 * visits every profile at a vertex of current triangle and finds linear,
 * radial, interpolation coefficients at radial nodes. Then uses the 6
 * coefficients to compute interpolated values.
 * <p>
 * There is no public constructor. Call
 * GeoTessModel.getPosition(InterpolatorType.LINEAR) to obtain an instance.
 * 
 * @author Sandy Ballard
 * 
 */
public class GeoTessPositionLinear extends GeoTessPosition
{
	/**
	 * @param model
	 * @param radialType 
	 * @throws GeoTessException
	 */
	protected GeoTessPositionLinear(GeoTessModel model, InterpolatorType radialType) throws GeoTessException
	{
		super(model, radialType);

		vertices = new ArrayList<ArrayListInt>(grid.getNTessellations());
		hCoefficients = new ArrayList<ArrayListDouble>(grid.getNTessellations());

		for (int i = 0; i < grid.getNTessellations(); ++i)
		{
			vertices.add(new ArrayListInt(3));
			hCoefficients.add(new ArrayListDouble(3));
		}
	}
	
	/**
	 * Creates a deep copy of this GeoTessPositionLinear object and
	 * returns it to the caller.
	 *  
	 * @return A deep copy of this GeoTessPositionLinear object.
	 * @throws GeoTessException
	 */
	@Override
	public GeoTessPosition deepClone() throws GeoTessException
	{
		GeoTessPositionLinear newGTP = (GeoTessPositionLinear)
				                           GeoTessPosition.getGeoTessPosition(this);
		newGTP.copy(this);
		return newGTP;
	}

	/**
	 * Copies the input GeoTessPositionLinear into this GeoTessPositionLinear
	 * object. At exit this object is an exact (deep) replica of gtpl.
	 * 
	 * @param gtpl The object whose state will be copied into this object.
	 */
	@Override
	public void copy(GeoTessPosition gtpl)
	{
		super.setCopy(gtpl);
	}

	@Override
	public InterpolatorType getInterpolatorType()
	{
		return InterpolatorType.LINEAR;
	}

	/**
	 * Set vertices to the 3-element array that stores the corners of the
	 * triangle identified during the triangle walk algorithm. Horizontal
	 * coefficients are similarly set to the coefficients identified during
	 * triangle walk.
	 * 
	 * @throws GeoTessException
	 */
	@Override
	protected void update2D(int tessid) throws GeoTessException
	{
		// get references to the vertices and coefficients involved in interpolation.
		// These are owned by the super class.
		ArrayListInt vertexes = vertices.get(tessid);
		ArrayListDouble coeff = hCoefficients.get(tessid);
		
		vertexes.clear();
		coeff.clear();
		
		// the index of the triangle that contains the interpolation point.  This was 
		// discovered by the super class using walking triangle algorithm.
		int triangle = getTriangle(tessid);
		
		// find the indices of the 3 vertices at the corners of triangle
		int[] corners = grid.triangles[triangle];

		// iterate over the indices of the 3 vertices at the corners of the 
		// containing triangle.
		for (int vertex : corners)
			// if the interpolation point falls on a grid node:
			if (GeoTessUtils.dot(unitVector, grid.vertices[vertex]) > Math.cos(1e-7))
			{
				// the interpolation point coincides with one of the corners of
				// the triangle in which the interpolation point resides.
				// Set the list of interpolation vertices to include only
				// the identified vertex, and the interpolation coefficient = 1.
				vertexes.add(vertex);
				coeff.add(1.);
				return;
			}

		ArrayListDouble lc = linearCoefficients.get(tessid);
		vertexes.add(corners[0]);
		coeff.add(lc.get(0));
		vertexes.add(corners[1]);
		coeff.add(lc.get(1));
		vertexes.add(corners[2]);
		coeff.add(lc.get(2));

	}

}
