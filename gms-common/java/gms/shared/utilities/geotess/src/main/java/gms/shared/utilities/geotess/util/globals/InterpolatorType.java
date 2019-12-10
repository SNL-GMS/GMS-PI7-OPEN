package gms.shared.utilities.geotess.util.globals;

/**
 * An enum list of all the different types of interpolation that GeoTess
 * knows how to perform. Includes both 2D interpolation algorithms that will
 * be applied to vertices in the 2D grid, and 1D interpolation algorithms
 * that will be applied to nodes distributed along radial profiles.
 * 
 * @author Sandy Ballard
 * 
 */
public enum InterpolatorType {
	/**
	 * Use linear interpolation. Applies to both 2D interpolation in
	 * triangles and 1D interpolation in radial profiles.
	 */
	LINEAR,

	/**
	 * Use natural neighbor interpolation in 2D, geographic interpolation.
	 */
	NATURAL_NEIGHBOR,
	
	/**
	 * Use 2D cubic spline interpolation in radial profiles.
	 */
	CUBIC_SPLINE
}
