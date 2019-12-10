package gms.shared.utilities.geotess.util.globals;

/**
 * Execution can be optimized either for speed or memory. If optimization is
 * set to SPEED, then the following optimization strategies will be
 * implemented:
 * <ul>
 * <li>for each edge of a triangle the unit vector normal to the plane of
 * the great circle containing the edge will be computed during input of the
 * grid from file and stored in memory. With this information, the walking
 * triangle algorithm can use dot products instead of scalar triple products
 * when determining if a point resides inside a triangle. While much more
 * computationally efficient, it requires a lot of memory to store all those
 * unit vectors.
 * <li>when performing natural neighbor interpolation, lazy evaluation will
 * be used to store the circumcenters of triangles that are computed during
 * interpolation.
 * <li>when interpolating along radial profiles, every profile will record
 * the index of the radius that is discovered. That index will be the
 * starting point for the binary search the next time binary search is
 * implemented. Each GeoTessPosition object will store 2d array of shorts,
 * short[nVertices][nlayers] to record this information. Might be ~1MB per
 * GeoTessPosition object (they could share references to the same short[][]
 * as long as they don't break concurrency.
 * </ul>
 */
public enum OptimizationType
{
	/**
	 * Model is optimized for speed.
	 */
	SPEED,

	/**
	 * Model is optimized for memory.
	 */
	MEMORY
};
