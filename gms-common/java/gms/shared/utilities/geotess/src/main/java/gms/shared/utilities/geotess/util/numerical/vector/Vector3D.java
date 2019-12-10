package gms.shared.utilities.geotess.util.numerical.vector;

import static java.lang.Math.sqrt;
import static java.lang.Math.abs;
import static java.lang.Math.cos;
import static java.lang.Math.sin;

public class Vector3D extends VectorGeo
{
	/**
	 * Copies the input vector (vin) to the output vector (vout).
	 * @param vin The input vector to be copied to the output vector vout.
	 * @param vout The output vector which contains vin on exit.
	 */
	public static void copy(double[] vin, double[] vout)
	{
		vout[0] = vin[0];
		vout[1] = vin[1];
		vout[2] = vin[2];
	}

	/**
	 * Changes the direction of the input vector by reversing the sign
	 * of all three components.
	 * @param v0 the vector whose sign will be reversed.
	 */
	public static void negate(double[] v0)
	{
		v0[0] = -v0[0];
		v0[1] = -v0[1];
		v0[2] = -v0[2];
	}

	/**
	 * Increment the result vector by the contents of v0.
	 * @param rslt On exits contains rslt = rslt + v0;
	 * @param v0 The point added to rslt.
	 */
	public static void increment(double[] rslt, double[] v0)
	{
		rslt[0] += v0[0];
		rslt[1] += v0[1];
		rslt[2] += v0[2];
	}

	/**
	 * Decrement the result vector by the contents of v0.
	 * @param rslt On exits contains rslt = rslt - v0;
	 * @param v0 The point subtracted from rslt.
	 */
	public static void decrement(double[] rslt, double[] v0)
	{
		rslt[0] -= v0[0];
		rslt[1] -= v0[1];
		rslt[2] -= v0[2];
	}

	/**
	 * Adds v0 to v1 and returns the result.
	 * @param v0 The first point added to the result.
	 * @param v1 The second point added to the result.
	 * @return The result v0 + v1.
	 */
	public static double [] add(double[] v0, double[] v1)
	{
		double[] rslt = new double [3];
		add(rslt, v0, v1);
		return rslt;
	}

	/**
	 * Assigns the sum of v0 and v1 to the input rslt vector.
	 *
	 * @param rslt double[]
	 * @param v0 The first point added to the result.
	 * @param v1 The second point added to the result.
	 */
	public static void add(double[] rslt, double[] v0, double[] v1)
	{
		rslt[0] = v0[0] + v1[0];
		rslt[1] = v0[1] + v1[1];
		rslt[2] = v0[2] + v1[2];
	}

	/**
	 * Subtracts v2 from v1 and places the result in rslt.
	 *
	 * @param rslt The result of v1 - v2.
	 * @param v1 The point from which v2 will be subtracted and placed in rslt.
	 * @param v2 The point subtracted from v1 and placed in rslt.
	 */
	public static void subtract(double[] rslt, double[] v1, double[] v2)
	{
		rslt[0] = v1[0] - v2[0];
		rslt[1] = v1[1] - v2[1];
		rslt[2] = v1[2] - v2[2];
	}

	/**
	 * Subtracts v2 from v1 and returns the result as a double [].
	 * @param v1 The point from which v2 will be subtracted and placed in rslt.
	 * @param v2 The point subtracted from v1 and placed in rslt.
	 * @return The result of v1 - v2.
	 */
	public static double[] subtract(double[] v1, double[] v2)
	{
		double[] rslt = new double[3];
		rslt[0] = v1[0] - v2[0];
		rslt[1] = v1[1] - v2[1];
		rslt[2] = v1[2] - v2[2];
		return rslt;
	}

	/**
	 * Scales the input rslt vector by m0.
	 * @param rslt The input vector to be scaled by m0.
	 * @param m0 the scale factor
	 */
	public static void scale(double[] rslt, double m0)
	{
		rslt[0] *= m0;
		rslt[1] *= m0;
		rslt[2] *= m0;
	}

	/**
	 * Multiplies the scalar m0 by the vector v0 and returns the result as
	 * a double[].
	 * @param m0 The scale factor used to scale v0.
	 * @param v0 The vector to be scaled by m0.
	 * @return The result of m0 * v0.
	 */
	public static double[] mult(double m0, double[] v0)
	{
		double[] rslt = new double [3];
		mult(rslt, m0, v0);
		return rslt;
	}

	/**
	 * Multiplies the scalar m0 by the vector v0 and returns the result
	 * in rslt.
	 * @param rslt = m0 * v0.
	 * @param m0 The scale factor used to scale v0.
	 * @param v0 The vector to be scaled by m0.
	 */
	public static void mult(double[] rslt, double m0, double[] v0)
	{
		rslt[0] = m0 * v0[0];
		rslt[1] = m0 * v0[1];
		rslt[2] = m0 * v0[2];
	}

	/**
	 * Multiplies the scalar m0 by the vector v0 and add it to the result
	 * in rslt.
	 * 
	 * @param rslt += m0 * v0.
	 * @param m0 The scale factor used to scale v0.
	 * @param v0 The vector to be scaled by m0.
	 */
	public static void multIncrement(double[] rslt, double m0, double[] v0)
	{
		rslt[0] += m0 * v0[0];
		rslt[1] += m0 * v0[1];
		rslt[2] += m0 * v0[2];
	}

	/**
	 * Performs the operation m0 * v0 + m1 and returns the result as a double[].
	 * Here m0 and m1 are scalars and v0 is a vector.
	 *
	 * @param m0 The scale factor used to scale v0.
	 * @param v0 The vector to be scaled by m0.
	 * @param m1 The additive increment summed to the returned result.
	 * @return double[]
	 */
	public static double[] addMult(double m0, double[] v0, double m1)
	{
		double[] rslt = new double [3];
		addMult(rslt, m0, v0, m1);
		return rslt;
	}

	/**
	 * Performs the operation rslt = m0 * v0 + m1.
	 * ere m0 and m1 are scalars and v0 and rslt are vectors.
	 * @param rslt = m0 * v0 + m1.
	 * @param m0 The scale factor used to scale v0.
	 * @param v0 The vector to be scaled by m0.
	 * @param m1 The additive increment summed to the returned result.
	 */
	public static void addMult(double[] rslt, double m0, double[] v0, double m1)
	{
		rslt[0] = m0 * v0[0] + m1;
		rslt[1] = m0 * v0[1] + m1;
		rslt[2] = m0 * v0[2] + m1;
	}

	/**
	 * Performs the operation m0 * v0 + v1 and returns the result as a double[].
	 * Here m0 is a scalar and v0 and v1 are vectors.
	 *
	 * @param m0 The scale factor used to scale v0.
	 * @param v0 The vector to be scaled by m0.
	 * @param v1 The additive vector summed to the returned result.
	 * @return double[]
	 */
	public static double[] addMult(double m0, double[] v0, double[] v1)
	{
		double[] rslt = new double [3];
		addMult(rslt, m0, v0, v1);
		return rslt;
	}

	/**
	 * Performs the operation rslt = m0 * v0 + v1.
	 * ere m0 is a scalar and v0, v1, and rslt are vectors.
	 * @param rslt = m0 * v0 + v1.
	 * @param m0 The scale factor used to scale v0.
	 * @param v0 The vector to be scaled by m0.
	 * @param v1 The additive vector summed to rslt.
	 */
	public static void addMult(double[] rslt, double m0, double[] v0,
			double[] v1)
	{
		rslt[0] = m0 * v0[0] + v1[0];
		rslt[1] = m0 * v0[1] + v1[1];
		rslt[2] = m0 * v0[2] + v1[2];
	}

	/**
	 * Performs the operation m0 * v0 + m1 * v1 and returns the result as a
	 * double[]. Here m0 and m1 are scalars and v0 and v1 are vectors.
	 *
	 * @param m0 The scale factor used to scale v0.
	 * @param v0 The vector to be scaled by m0.
	 * @param m1 The scale factor used to scale v1.
	 * @param v1 The vector to be scaled by m1.
	 * @return double[]
	 */
	public static double[] addMult(double m0, double[] v0,
			double m1, double[] v1)
	{
		double[] rslt = new double [3];
		addMult(rslt, m0, v0, m1, v1);
		return rslt;
	}

	/**
	 * Performs the operation rslt = m0 * v0 + m1 * v1.
	 * ere m0 and m1 are scalars and v0, v1, and rslt are vectors.
	 * @param rslt = m0 * v0 + m1 * v1.
	 * @param m0 The scale factor used to scale v0.
	 * @param v0 The vector to be scaled by m0.
	 * @param m1 The scale factor used to scale v1.
	 * @param v1 The vector to be scaled by m1.
	 */
	public static void addMult(double[] rslt, double m0, double[] v0,
			double m1, double[] v1)
	{
		rslt[0] = m0 * v0[0] + m1 * v1[0];
		rslt[1] = m0 * v0[1] + m1 * v1[1];
		rslt[2] = m0 * v0[2] + m1 * v1[2];
	}

	/**
	 * Calculates the midpoint of the two input vectors, v0 and v1, and returns
	 * the result as a double[]. Returns (v0 + v1) / 2.
	 *
	 * @param v0 An end-point of the line containing the midpoint.
	 * @param v1 An end-point of the line containing the midpoint.
	 * @return double[]
	 */
	public static double[] midPoint(double[] v0, double[] v1)
	{
		double[] rslt = new double [3];
		midPoint(rslt, v0, v1);
		return rslt;
	}

	/**
	 * Calculates the midpoint of the two input vectors, v0 and v1, and
	 * returns the result in rslt.
	 * @param rslt = (v0 + v1) / 2.
	 * @param v0 An end-point of the line containing the midpoint.
	 * @param v1 An end-point of the line containing the midpoint.
	 */
	public static void midPoint(double[] rslt, double[] v0, double[] v1)
	{
		rslt[0] = 0.5 * (v0[0] + v1[0]);
		rslt[1] = 0.5 * (v0[1] + v1[1]);
		rslt[2] = 0.5 * (v0[2] + v1[2]);
	}

	/**
	 * Returns the straight-line distance in vector units from v1 to v2.
	 * @param v1 The point from which the distance to v2 will be returned.
	 * @param v2 The point to which the distance from v1 will be returned.
	 * @return double straight-line distance from v1 to v2 in degrees.
	 */
	public static double distance3D(double[] v1, double[] v2)
	{
		double [] temp = subtract(v1, v2);
		return length(temp);
	}

	/**
	 * Returns the straight-line distance squared in vector units from v1 to v2.
	 * @param v1 The point from which the squared distance to v2 will be returned.
	 * @param v2 The point to which the squared distance from v1 will be returned.
	 * @return double straight-line distance squared from v1 to v2 in degrees.
	 */
	public static double distance3DSquared(double[] v1, double[] v2)
	{
		double [] temp = subtract(v1, v2);
		return lengthSquared(temp);
	}

	/**
	 * Find the length of a 3-element vector.
	 * @param u double[]
	 * @return the length of the vector.  Guaranteed to be >= 0.
	 */
	public static double length(double[] u)
	{
		double l = u[0]*u[0]+u[1]*u[1]+u[2]*u[2];
		if (l > 0.)
			return sqrt(l);
		return 0.;
	}

	/**
	 * Find the squared length of a 3-element vector.
	 * @param u double[]
	 * @return The squared length of the vector.
	 */
	public static double lengthSquared(double[] u)
	{
		return (u[0] * u[0] + u[1] * u[1] + u[2] * u[2]);
	}

	/**
	 * Interpolates a point on the line between v0 and v1 and places
	 * the result in rslt.
	 * @param v0 Beginning point on line.
	 * @param v1 Ending point on line
	 * @param f Fractional distance between v0 (f=0) and v1 (f=1).
	 * @param rslt Result of interpolation (rslt = (v1 - v0) * f + v0).
	 */
	public static void interpolate(double[] v0, double[] v1,
			double f, double[] rslt)
	{
		rslt[0] = (v1[0] - v0[0]) * f + v0[0];
		rslt[1] = (v1[1] - v0[1]) * f + v0[1];
		rslt[2] = (v1[2] - v0[2]) * f + v0[2];
	}

	/**
	 * Interpolates a position in the triangle defined by points v0, v1, and v2.
	 * The result in placed in rslt for return. The position is defined by the
	 * two fractions s and t where s + t < 1 (if the result is to lie within the
	 * triangle bounds).
	 * @param v0 First triangle point.
	 * @param v1 Second triangle point.
	 * @param v2 Third triangle point.
	 * @param s Fractional distance from v0 to v2.
	 * @param t Fractional distance from v0 to v1.
	 * @param rslt Result of interpolation where
	 *            (rslt = (v2 - v0) * s + (v1 - v0) * t + v0).
	 */
	public static void interpolate(double[] v0, double[] v1, double[] v2,
			double s, double t, double[] rslt)
	{
		rslt[0] = (v2[0] - v0[0]) * s + (v1[0] - v0[0]) * t + v0[0];
		rslt[1] = (v2[1] - v0[1]) * s + (v1[1] - v0[1]) * t + v0[1];
		rslt[2] = (v2[2] - v0[2]) * s + (v1[2] - v0[2]) * t + v0[2];
	}

	/**
	 * Returns true if v0 and v1 have identical components.
	 * 
	 * @param v0 Vector to be tested with v1 for equality.
	 * @param v1 Vector to be tested with v0 for equality.
	 * @return true if v0 and v1 have identical components.
	 */
	public static boolean equals(double[] v0, double[] v1)
	{
		return ((v0[0] == v1[0]) && (v0[1] == v1[1]) && (v0[2] == v1[2])) ?
				true : false;
	}

	/**
	 * Calculate the scalar triple product (vp-v0) dot ((v2-v0) x (v1-v0)).
	 * @param v0 Base point of local coordinate system.
	 * @param v1 First point of plane in local system as v1-v0.
	 * @param v2 Second point of plane in local system as v2-v0.
	 * @param vp Point to be dotted in local system (relative to v0).
	 * @return double Scalar triple product (vp-v0) dot ((v2-v0) x (v1-v0))
	 */
	public static double scalarTripleProduct(double[] v0, double[] v1, double[] v2, double[] vp)
	{
		double[] v10 = subtract(v1, v0);
		double[] v20 = subtract(v2, v0);
		double[] vp0 = subtract(vp, v0);
		return VectorUnit.scalarTripleProduct(v20, v10, vp0);
	}

	/**
	 * Compute the normalized vector triple product ((v1-v0) x (v2-v0)) x (v1-v0)
	 * and store the result in rslt.
	 * @param v0 Base point of local coordinate system.
	 * @param v1 First point defined in local system as v1-v0.
	 * @param v2 Second point defined in local system as v2-v0.
	 * @param rslt = ((v1-v0) x (v2-v0)) x (v1-v0)
	 * @return true if rslt has finite length, false if length(rslt) is zero.
	 */
	public static boolean vectorTripleProduct(double[] v0, double[] v1, double[] v2, double[] rslt)
	{
		// rslt = ((v1-v0) x (v2-v0)) x (v1-v0)
		double[] v10 = subtract(v1, v0);
		double[] v20 = subtract(v2, v0);
		return VectorUnit.vectorTripleProduct(v10, v20, rslt);
	}

	/**
	 * Dot product of v1 and v2 in the v0 local space (v1-v0)dot(v2-v0).
	 * @param v0 Origen in local space.
	 * @param v1 First point to be dotted with v2 in v0 local space.
	 * @param v2 Second point to be dotted with v1 in v0 local space.
	 * @return dot product = (v1-v0)dot(v2-v0).
	 */
	public static double dot(double[] v0, double[] v1, double[] v2)
	{
		double[] v10 = subtract(v1, v0);
		double[] v20 = subtract(v2, v0);
		return VectorUnit.dot(v10, v20);
	}

	/**
	 * Cross product of v1 with v2 in the v0 local space (v1-v0)x(v2-v0).
	 * @param v0 Origen in local space. Result is not normalized.
	 * @param v1 First point to be crossed with v2 in v0 local space.
	 * @param v2 Second point to be crossed with v1 in v0 local space.
	 * @param rslt cross product = (v1-v0)x(v2-v0).
	 */
	public static void cross(double[] v0, double[] v1, double[] v2, double[] rslt)
	{
		double[] v10 = subtract(v1, v0);
		double[] v20 = subtract(v2, v0);
		VectorUnit.cross(v10, v20, rslt);
	}

	/**
 * Move unit vector w in direction of vtp by distance y. Then rotate the
 * result by angle x around vtp. Normalize result and return in u.
 *
 * @param w double[]
 * @param vtp double[]
 * @param x double
 * @param y double
 * @param u double[]
 */
public static void move(double[] w, double[] vtp, double x, double y, double[] u)
{
	double cosa, sina;
	// first move in direction vtp by distance y.
	if (y == 0.)
	{
		// if distance to move is zero, set u = starting point
		u[0] = w[0];
		u[1] = w[1];
		u[2] = w[2];
	}
	else if (abs(VectorUnit.dot(w, vtp)) < 1e-7)
	{
		// if  pole of rotation (vtp) is orthogonal to w, then proceed with move.
		cosa = cos(y);
		sina = sin(y);
		u[0] = cosa * w[0] + sina * vtp[0];
		u[1] = cosa * w[1] + sina * vtp[1];
		u[2] = cosa * w[2] + sina * vtp[2];
	}
	else
	{
		// if pole of rotation is not orthogonal to starting point w, then find
		// point 90 degrees away from w, in direction of vtp.  Use that pole
		// to make the move.
		double[] vtp2 = new double[3];
		VectorUnit.vectorTripleProduct(w, vtp, vtp2);
		cosa = cos(y);
		sina = sin(y);
		u[0] = cosa * w[0] + sina * vtp2[0];
		u[1] = cosa * w[1] + sina * vtp2[1];
		u[2] = cosa * w[2] + sina * vtp2[2];
	}

	if (abs(x) > 0.)
	{
		// rotate u around vtp by angle x
		double d = u[0] * vtp[0] + u[1] * vtp[1] + u[2] * vtp[2]; // dot product
		// if w and vtp are parallel, x needs no rotation.
		if (abs(d) >= 1.)
			return;

		cosa = cos(x);
		sina = sin(x);
		d *= (1 - cosa);
		double u0 = cosa * u[0] + d * vtp[0] + sina * (vtp[1] * u[2] - vtp[2] * u[1]);
		double u1 = cosa * u[1] + d * vtp[1] + sina * (vtp[2] * u[0] - vtp[0] * u[2]);
		double u2 = cosa * u[2] + d * vtp[2] + sina * (vtp[0] * u[1] - vtp[1] * u[0]);
		d = sqrt(u0 * u0 + u1 * u1 + u2 * u2);
		u[0] = u0/d;
		u[1] = u1/d;
		u[2] = u2/d;
	}
}
}
