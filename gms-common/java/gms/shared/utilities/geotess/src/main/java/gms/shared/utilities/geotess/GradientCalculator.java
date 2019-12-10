package gms.shared.utilities.geotess;

import gms.shared.utilities.geotess.util.globals.InterpolatorType;
import gms.shared.utilities.geotess.util.numerical.platonicsolid.PlatonicSolid;
import gms.shared.utilities.geotess.util.numerical.matrix.LUDecomposition4x4;
import gms.shared.utilities.geotess.util.numerical.vector.VectorUnit;

/**
 * An attribute gradient calculator used to linearly estimate attribute gradients
 * at arbitrary positions within a defining GeoTessModel. A small tetrahedron
 * is used to linearly interpolate the gradient of an attribute field variable
 * in a 3D since. This was taken directly from Sandy Ballard's equivalent
 * (SmallTet) defined for the old GMP GeoModel object. Some modifications were
 * made to support general attribute gradients.
 * 
 * @author jrhipp
 *
 */
public class GradientCalculator
{
	/**
	 * An internal interpolator used to interpolate the gradient at the position v.
	 */
	private GeoTessPosition pos = null;

	/**
	 * An internal vector used to store the interpolated gradient on the small
	 * tetrahedrons node and overwritten with the gradient after the solution
	 * completes.
	 */
	private double[] data = new double[4];

	/**
	 * An internal array of vector positions storing the 4 nodes of the
	 * tetrahedron whose center represents the position for which the
	 * gradient is sought. 
	 */
	private double[][] m = new double[4][4];

	/**
	 * Fast 4x4 LU decomposition solver.
	 */
	private LUDecomposition4x4 solver = new LUDecomposition4x4(m, data);

	/**
	 * The position vector set to an attribute interpolation position.
	 */
	private double[] v = new double[] {0., 0., 6350.};

	// the 4 vertices of a tetrahedron.
	private double[] vtet0;
	private double[] vtet1;
	private double[] vtet2;
	private double[] vtet3;	

	/**
	 * Creates a linear horizontal and radial interpolator (GeoTessPosition) to
	 * evaluate the gradient and sets the small tetrahedron scaled to the
	 * input size (tetSize in km).
	 * 
	 * @param model   The GeoTessModel whose attribute and grid fields are to be
	 *                used in calculating the gradient.
	 * @param tetSize The size of the small tetrahedron (km).
	 * @throws GeoTessException
	 */
	public GradientCalculator(GeoTessModel model, double tetSize) throws GeoTessException
	{
		pos = GeoTessPosition.getGeoTessPosition(model, InterpolatorType.LINEAR);
		
		vtet0 = PlatonicSolid.TETRAHEDRON.getVertex(0).clone();
		for (int j=0; j<3; ++j) vtet0[j] *= tetSize;
		vtet1 = PlatonicSolid.TETRAHEDRON.getVertex(1).clone();
		for (int j=0; j<3; ++j) vtet1[j] *= tetSize;
		vtet2 = PlatonicSolid.TETRAHEDRON.getVertex(2).clone();
		for (int j=0; j<3; ++j) vtet2[j] *= tetSize;
		vtet3 = PlatonicSolid.TETRAHEDRON.getVertex(3).clone();
		for (int j=0; j<3; ++j) vtet3[j] *= tetSize;
	}

	/**
	 * Creates an interpolator of horizontal type horizType (LINEAR or
	 * NATURAL_NEIGHBOR) used to evaluate the gradient and sets the
	 * small tetrahedron scaled to the input size (tetSize in km).
	 * 
	 * @param model     The GeoTessModel whose attribute and grid fields are to be
	 *                  used in calculating the gradient.
	 * @param tetSize   The size of the small tetrahedron (km).
	 * @param horizType The horizontal interpolator type (LINEAR or
	 *                  NATURAL_NEIGHBOR).
	 *                  
	 * @throws GeoTessException
	 */
	public GradientCalculator(GeoTessModel model, double tetSize,
			            InterpolatorType horizType) throws GeoTessException
	{
		pos = GeoTessPosition.getGeoTessPosition(model, horizType);
		
		vtet0 = PlatonicSolid.TETRAHEDRON.getVertex(0).clone();
		for (int j=0; j<3; ++j) vtet0[j] *= tetSize;
		vtet1 = PlatonicSolid.TETRAHEDRON.getVertex(1).clone();
		for (int j=0; j<3; ++j) vtet1[j] *= tetSize;
		vtet2 = PlatonicSolid.TETRAHEDRON.getVertex(2).clone();
		for (int j=0; j<3; ++j) vtet2[j] *= tetSize;
		vtet3 = PlatonicSolid.TETRAHEDRON.getVertex(3).clone();
		for (int j=0; j<3; ++j) vtet3[j] *= tetSize;
	}

	/**
	 * Calculates the attribute gradient at the input position vector.
	 * The position is constrained (if necessary) to lie within the
	 * specified layer. If reciprocal is true the inverse gradient is returned.
	 * 
	 * @param vector     The full 3-component vector where the gradient is to
	 *                   be computed.
	 * @param attribute  The attribute index for which the gradient is to be
	 *                   evaluated.
	 * @param layerId    The layer id within which the calculation is
	 *                   constrained.
   * @param reciprocal Boolean flag, that if true, causes the inverse
   *                   gradient to be returned.
	 * @param gradient   The resulting gradient.
	 * @throws GeoModelException
	 */
	public void getGradient(double[] vector,
			                    int attributeIndex, int layerId,
			                    boolean reciprocal, float[] gradient)
	            throws GeoTessException
	{
		getGradient(vector, attributeIndex, layerId, reciprocal);
		gradient[0] = (float)data[0];
		gradient[1] = (float)data[1];
		gradient[2] = (float)data[2];      
	}

	/**
	 * Calculates the attribute gradient at the input position vector.
	 * The position is constrained (if necessary) to lie within the
	 * specified layer. If reciprocal is true the inverse gradient is returned.
	 * 
	 * @param vector     The full 3-component vector where the gradient is to
	 *                   be computed.
	 * @param attribute  The attribute index for which the gradient is to be
	 *                   evaluated.
	 * @param layerId    The layer id within which the calculation is
	 *                   constrained.
   * @param reciprocal Boolean flag, that if true, causes the inverse
   *                   gradient to be returned.
	 * @param gradient   The resulting gradient.
	 * @throws GeoModelException
	 */
	public void getGradient(double[] vector,
			                    int attributeIndex, int layerId,
			                    boolean reciprocal, double[] gradient)
	            throws GeoTessException
	{
		getGradient(vector, attributeIndex, layerId, reciprocal);
		gradient[0] = data[0];
		gradient[1] = data[1];
		gradient[2] = data[2];
	}

	/**
	 * Calculates the attribute gradient at the input position vector
	 * (vunit * radius). The position radius is constrained (if necessary)
	 * to lie within the specified layer. If reciprocal is true the inverse
	 * gradient is returned.
	 * 
	 * @param vunit      The unit vector where the gradient is to be computed.
	 * @param radius     The radius of the position where the gradient
	 *                   is to be computed.
	 * @param attribute  The attribute index for which the gradient is to be
	 *                   evaluated.
	 * @param layerId    The layer id within which the calculation is
	 *                   constrained.
   * @param reciprocal Boolean flag, that if true, causes the inverse
   *                   gradient to be returned.
	 * @param gradient   The resulting gradient.
	 * @throws GeoModelException
	 */
	public void getGradient(double[] vunit, double radius,
			                    int attributeIndex, int layerId,
			                    boolean reciprocal, float[] gradient)
	            throws GeoTessException
	{
		getGradient(vunit, radius, attributeIndex, layerId, reciprocal);
		gradient[0] = (float)data[0];
		gradient[1] = (float)data[1];
		gradient[2] = (float)data[2];      
	}

	/**
	 * Calculates the attribute gradient at the input position vector
	 * (vunit * radius). The position radius is constrained (if necessary)
	 * to lie within the specified layer. If reciprocal is true the inverse
	 * gradient is returned.
	 * 
	 * @param vunit      The unit vector where the gradient is to be computed.
	 * @param radius     The radius of the position where the gradient
	 *                   is to be computed.
	 * @param attribute  The attribute index for which the gradient is to be
	 *                   evaluated.
	 * @param layerId    The layer id within which the calculation is
	 *                   constrained.
   * @param reciprocal Boolean flag, that if true, causes the inverse
   *                   gradient to be returned.
	 * @param gradient   The resulting gradient.
	 * @throws GeoModelException
	 */
	public void getGradient(double[] vunit, double radius,
			                    int attributeIndex, int layerId,
			                    boolean reciprocal, double[] gradient)
	            throws GeoTessException
	{
		getGradient(vunit, radius, attributeIndex, layerId, reciprocal);
		gradient[0] = data[0];
		gradient[1] = data[1];
		gradient[2] = data[2];
	}

	public void getTest()
	{
		double[][] tm = new double[4][4];
		double[]   tdata = new double[4];
		LUDecomposition4x4 tsolver = new LUDecomposition4x4(tm, tdata);
		tm[0][0] = vtet0[0];
		tm[0][1] = vtet0[1];
		tm[0][2] = vtet0[2];
		tm[0][3] = 1.0;

		tm[1][0] = vtet1[0];
		tm[1][1] = vtet1[1];
		tm[1][2] = vtet1[2];
		tm[1][3] = 1.0;

		tm[2][0] = vtet2[0];
		tm[2][1] = vtet2[1];
		tm[2][2] = vtet2[2];
		tm[2][3] = 1.0;

		tm[3][0] = vtet3[0];
		tm[3][1] = vtet3[1];
		tm[3][2] = vtet3[2];
		tm[3][3] = 1.0;
		
		tdata[0] = 1.0;
		tdata[1] = 1.942809041582063;
		tdata[2] = 0.5285954792089679;
		tdata[3] = 0.5285954792089679;
		
		tsolver.solve();
	}

	/**
	 * Calculates the gradient of the input position vector (vcenter)
	 * for the attribute (attributeIndex) at the specified layer index (layerId).
	 * If the input position is not defined within the input layer then the
	 * gradient is calculated at the top of the layer, if the position is above
	 * the layer, or the bottom of the layer, if the position is below the layer.
	 * The resulting gradient, or it's inverse, is stored in the "data" array.
	 *  
	 * @param vcenter    The full 3-component vector (unit vector * radius) where
	 *                   the gradient is to be computed.
	 * @param attribute  The attribute index for which the gradient is to be
	 *                   evaluated.
	 * @param layerId    The layer id.
   * @param reciprocal Boolean flag, that if true, causes the inverse
   *                   gradient to be returned.
	 * @throws GeoTessException
	 */
	private void getGradient(double[] vcenter, int attributeIndex,
			                     int layerId, boolean reciprocal)
	             throws GeoTessException
	{
		// for each of the 4 corners of small tetrahedron whose center is defined
		// as the interpolation location (vcenter) find the interpolated
		// attribute values (or their inverses) and set them into data.
		data[0] = tetNodeValue(vcenter, vtet0, m[0], layerId,
                           attributeIndex, reciprocal);  
		data[1] = tetNodeValue(vcenter, vtet1, m[1], layerId,
                           attributeIndex, reciprocal);  
		data[2] = tetNodeValue(vcenter, vtet2, m[2], layerId,
                           attributeIndex, reciprocal);  
		data[3] = tetNodeValue(vcenter, vtet3, m[3], layerId,
                           attributeIndex, reciprocal);  

		// solve and exit ... gradient result is in "data".
		solver.solve();
	}

	/**
	 * Calculates the gradient of the input position vector (vunit * radius)
	 * for the attribute (attributeIndex) at the specified layer index (layerId).
	 * If the input position is not defined within the input layer then the
	 * gradient is calculated at the top of the layer, if the position is above
	 * the layer, or the bottom of the layer, if the position is below the layer.
	 * The resulting gradient, or it's inverse, is stored in the "data" array.
	 *  
	 * @param vunit      The 3-component vector unit vector where
	 *                   the gradient is to be computed.
	 * @param radius     The radius of the position where
	 *                   the gradient is to be computed.
	 * @param attribute  The attribute index for which the gradient is to be
	 *                   evaluated.
	 * @param layerId    The layer id.
   * @param reciprocal Boolean flag, that if true, causes the inverse
   *                   gradient to be returned.
	 * @throws GeoTessException
	 */
	private void getGradient(double[] vunit, double radius,
			                     int attributeIndex, int layerId,
				                   boolean reciprocal)
	             throws GeoTessException
	{
		// for each of the 4 corners of small tetrahedron whose center is defined
		// as the interpolation location (vunit * radius) find the interpolated
		// attribute values (or their inverses) and set them into data.
		data[0] = tetNodeValue(vunit, radius, vtet0, m[0], layerId,
				                   attributeIndex, reciprocal); 
		data[1] = tetNodeValue(vunit, radius, vtet1, m[1], layerId,
				                   attributeIndex, reciprocal); 
		data[2] = tetNodeValue(vunit, radius, vtet2, m[2], layerId,
				                   attributeIndex, reciprocal); 
		data[3] = tetNodeValue(vunit, radius, vtet3, m[3], layerId,
				                   attributeIndex, reciprocal); 

		// solve and exit ... gradient result is in "data".
		solver.solve();
	}

	/**
	 * Calculates and returns the attribute value at a single node of the small
	 * tetrahedron that has been translated in the model such that the center of
	 * tetrahedron coincides with the interpolation location, vpos, for which
	 * the attribute gradient is sought.
	 * 
	 * @param vpos            The position for which the gradient is
	 *                        to be calculated.
	 * @param tetNode         A node position of the small tetrahedron.
	 * @param ma              The position vector of the input tetrahedron node
	 *                        translated to a center about vpos.
	 * @param layerId         The layer id for which the gradient will be defined.
	 * @param attributeIndex  The attribute whose gradient will be returned.
	 * @param reciprocal      Boolean flag, that if true, causes the inverse
	 *                        gradient to be returned.
	 * @return                The value at the tetrahedrons node or it's inverse.
	 * @throws GeoTessException
	 */
	private double tetNodeValue(double[] vpos, double[] tetNode, double[] ma,
			                        int layerId, int attributeIndex,
			                        boolean reciprocal)
			           throws GeoTessException
	{
		// populate v with interpolation location translated to a small
		// tetrahedron's node position (tetNode).
		v[0] = vpos[0] + tetNode[0];
		v[1] = vpos[1] + tetNode[1];
		v[2] = vpos[2] + tetNode[2];

		// return the result
		return positionValue(ma, layerId, attributeIndex, reciprocal);
	}

	/**
	 * Calculates and returns the attribute value at a single node of the small
	 * tetrahedron that has been translated in the model such that the center of
	 * tetrahedron coincides with the interpolation location, vunit * radius,
	 * for which the attribute gradient is sought.
	 * 
	 * @param vunit           The unit vector of the position for which the
	 *                        gradient is to be calculated.
	 * @param radius          The radius of the position for which the
	 *                        gradient is to be calculated.
	 * @param tetNode         A node position of the small tetrahedron.
	 * @param ma              The position vector of the input tetrahedron node
	 *                        translated to a center about vunit * radius.
	 * @param layerId         The layer id for which the gradient will be defined.
	 * @param attributeIndex  The attribute whose gradient will be returned.
	 * @param reciprocal      Boolean flag, that if true, causes the inverse
	 *                        gradient to be returned.
	 * @return                The value at the tetrahedrons node or it's inverse.
	 * @throws GeoTessException
	 */
	private double tetNodeValue(double[] vunit, double radius,
			                        double[] tetNode, double[] ma,
			                        int layerId, int attributeIndex,
			                        boolean reciprocal)
			           throws GeoTessException
	{
		// populate v with interpolation location translated to a small
		// tetrahedron's node position (tetNode).
		v[0] = vunit[0] * radius + tetNode[0];
		v[1] = vunit[1] * radius + tetNode[1];
		v[2] = vunit[2] * radius + tetNode[2];

		// return the result
		return positionValue(ma, layerId, attributeIndex, reciprocal);
	}

	/**
	 * Calculates and returns the interpolated value at field position vector v,
	 * for the specified layer id and attribute index, and sets its location into
	 * the input array ma. If the reciprocal flag is true the inverse of the
	 * interpolated value at v is returned.
	 * 
	 * @param ma              The position vector of the input tetrahedron node
	 *                        translated to a center about vunit * radius.
	 * @param layerId         The layer id for which the gradient position will
	 *                        be constrained, if necessary.
	 * @param attributeIndex  The attribute index.
	 * @param reciprocal      Boolean flag, that if true, causes the inverse
	 *                        gradient to be returned.
	 * @return                The gradient (or inverse) at the field position v.
	 * @throws GeoTessException
	 */
	private double positionValue(double[] ma, int layerId,
			                         int attributeIndex, boolean reciprocal)
			           throws GeoTessException
  {
		// set the position to tet's zero node location.
		pos.set(v, VectorUnit.normalize(v));
	
		// constrain node's radius to be within the radius limits of
		// the specified layer.
		double rad = pos.setRadiusConstrained(layerId);
	
		// copy nodes' location into M matrix.
		ma[0] = pos.getVector()[0] * rad;
		ma[1] = pos.getVector()[1] * rad;
		ma[2] = pos.getVector()[2] * rad;
		ma[3] = 1.0;
	
		// interpolate the value of the desired property at location v
		// and return its result (or inverse)
		return reciprocal ? 1./pos.getValue(attributeIndex) :
			                  pos.getValue(attributeIndex);
	}
}
