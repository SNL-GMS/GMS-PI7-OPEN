package gms.shared.utilities.geotess.util.numerical.vector;

import static java.lang.Math.PI;
import static java.lang.Math.abs;
import static java.lang.Math.acos;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;
import static java.lang.Math.toDegrees;

import java.io.IOException;

public class VectorUnit
{
	/**
	 * Find the azimuth from unit vector v1 to unit vector v2. 
	 * Result will be between -PI and PI radians.
	 * 
	 * @param v1
	 *            The point from which the azimuth will be directed toward v2.
	 * @param v2
	 *            The point to which the azimuth will be directed from v1.
	 * @param errorValue
	 *            if v1 and v2 are parallel, or if v1 is either the north or
	 *            south pole, then return errorValue
	 * @return the azimuth from v1 to v2, in radians clockwise from north, or
	 *         errorValue.  Range is -PI to PI
	 */
	public static double azimuth(double[] v1, double[] v2, double errorValue)
	{
		double[] temp0 = new double[3];
		double[] temp1 = new double[3];

		// set temp0 = the cross product of v1 x v2.
		if (crossNormal(v1, v2, temp0) > 0.)

		// if v1 cross v2 has zero length then the two vectors are
		// coincident (do nothing in that case; returns errorValue).
		{
			// set temp1 = v1 x north pole
			// if the cross product has zero length then v1 == north_pole
			// or south pole and azimuth is indeterminant.
			if (crossNorth(v1, temp1) > 0.)
			{
				// set azimuth to the angle between 
				// (v1 x north pole) and (v1 x v2).
				errorValue = angle(temp1, temp0);
				// if the dot product of (v1 x v2) . northPole < 0
				if (temp0[2] < 0.)
					errorValue = -errorValue;
			}
		}
		return errorValue;
	}

	/**
	 * Find the azimuth from unit vectors v1 to v2. Result will be between -180
	 * and 180 degrees
	 * 
	 * @param v1
	 *            The point from which the azimuth will be directed toward v2.
	 * @param v2
	 *            The point to which the azimuth will be directed from v1.
	 * @param errorValue
	 *            if v1 and v2 are parallel, or if v1 is either the north or
	 *            south pole, then return errorValue
	 * @return the azimuth from v1 to v2, in degrees clockwise from north, or
	 *         errorValue
	 */
	public static double azimuthDegrees(double[] v1, double[] v2,
			                                double errorValue)
	{
		double[] temp0 = new double[3];
		double[] temp1 = new double[3];

		// set temp0 = the cross product of v1 x v2.
		if (crossNormal(v1, v2, temp0) > 0.)

		// if v1 cross v2 has zero length then the two vectors are
		// coincident (do nothing in that case; returns errorValue).
		{
			// set temp1 = v1 x north pole
			// if the cross product has zero length then v1 == north_pole
			// or south pole and azimuth is indeterminant.
			if (crossNorth(v1, temp1) > 0.)
			{
				// set azimuth to the angle between 
				// (v1 x north pole) and (v1 x v2).
				errorValue = angleDegrees(temp1, temp0);
				// if the dot product of (v1 x v2) . northPole < 0
				if (temp0[2] < 0.)
					errorValue = -errorValue;
			}
		}
		return errorValue;
	}

	/**
	 * A great circle is defined by two unit vectors that are 90 degrees apart.
	 * A great circle is stored in a double[2][3] array, which is the structure
	 * returned by this method. A great circle can be passed to the method
	 * getGreatCirclePoint() to retrieve a unit vector that is on the great
	 * circle and located some distance from the first point of the great
	 * circle.
	 * <p>
	 * This method returns a great circle that is computed from two unit vectors
	 * that are not necessarily 90 degrees apart.
	 * 
	 * @param v0
	 *            the first point on the great circle
	 * @param v1
	 *            some other point that is also on the great circle but which is
	 *            not necessarily 90 degrees away from v0.
	 * @return a 2 x 3 array specifying two unit vectors. The first one is a
	 *         clone of unit vector v0 passed as first argument to this method.
	 *         The second is located 90 degrees away from v0.
	 * @throws GeoTessException
	 *             if v0 and v1 are parallel.
	 */
	public static double[][] getGreatCircle(double[] v0, double[] v1)
			throws IOException
	{
		if (parallel(v0, v1))
			throw new IOException(
					"Cannot create a GreatCicle with two vectors that are parallel.");

		double[][] greatCircle = new double[2][3];
		greatCircle[0][0] = v0[0];
		greatCircle[0][1] = v0[1];
		greatCircle[0][2] = v0[2];
		vectorTripleProduct(v0, v1, v0, greatCircle[1]);
		return greatCircle;
	}

	/**
	 * A great circle is defined by two unit vectors that are 90 degrees apart.
	 * A great circle is stored in a double[2][3] array, which is the structure
	 * returned by this method. A great circle can be passed to the method
	 * getGreatCirclePoint() to retrieve a unit vector that is on the great
	 * circle and located some distance from the first point of the great
	 * circle.
	 * <p>
	 * This method returns a great circle that is defined by an initial point
	 * and an azimuth.
	 * 
	 * @param v
	 *            a unit vector that will be the first point on the great
	 *            circle.
	 * @param azimuth
	 *            a direction, in radians, in which to move relative to v in
	 *            order to define the great circle
	 * @return a 2 x 3 array specifying two unit vectors. The first one is a
	 *         clone of unit vector v passed as an argument to this method. The
	 *         second is located 90 degrees away from v in the direction
	 *         specified by azimuth.
	 * @throws GeoTessException
	 *             if v is located at north or south pole.
	 */
	public static double[][] getGreatCircle(double[] v, double azimuth)
			throws IOException
	{
		if (isPole(v))
			throw new IOException(
					"Cannot create a GreatCicle with north/south pole and an azimuth.");

		double[][] greatCircle = new double[2][3];
		greatCircle[0][0] = v[0];
		greatCircle[0][1] = v[1];
		greatCircle[0][2] = v[2];
		moveNorth(v, PI * 0.5, greatCircle[1]);
		rotate(greatCircle[1], v, azimuth, greatCircle[1]);
		return greatCircle;
	}

	/**
	 * Return the angular distance in radians between two unit vectors.
	 * 
	 * @param v0
	 *            a 3 component unit vector
	 * @param v1
	 *            a 3 component unit vector
	 * @return angular distance in radians.
	 */
	public static double angle(double[] v0, double[] v1)
	{
		double dot = v0[0] * v1[0] + v0[1] * v1[1] + v0[2] * v1[2];
		if (dot >= 1.)
			return 0.;
		else if (dot <= -1.)
			return PI;
		else
			return acos(dot);
	}

	/**
	 * Return the angular distance in degrees between two unit vectors.
	 * 
	 * @param v0
	 *            a 3 component unit vector
	 * @param v1
	 *            a 3 component unit vector
	 * @return angular distance in degrees.
	 */
	public static double angleDegrees(double[] v0, double[] v1)
	{
		double dot = v0[0] * v1[0] + v0[1] * v1[1] + v0[2] * v1[2];
		if (dot >= 1.)
			return 0.;
		else if (dot <= -1.)
			return 180.;
		else
		  return toDegrees(acos(dot));
	}

	/**
	 * Given two unit vectors and their radii, return the straight line
	 * separation between their tips. Assuming that the radii are in km, the
	 * result will also be in km.
	 * 
	 * @param v0
	 *            unit vector
	 * @param r0
	 *            length of v0, in km
	 * @param v1
	 *            unit vector
	 * @param r1
	 *            length of v1, in km
	 * @return distance between tip of v0*r0 and v1*r1, in km.
	 */
	public static double getDistance3D(double[] v0, double r0, double[] v1,
			double r1)
	{
		return length(new double[] { v0[0] * r0 - v1[0] * r1,
   				                       v0[1] * r0 - v1[1] * r1,
   				                       v0[2] * r0 - v1[2] * r1 });
	}

	/**
	 * A great circle is defined by two unit vectors that are 90 degrees apart.
	 * A great circle is stored in a double[2][3] array and one can be obtained
	 * by calling one of the getGreatCircle() methods.
	 * <p>
	 * In this method, a great circle and a distance are specified and a point
	 * is returned which is on the great circle path and is the specified
	 * distance away from the first point of the great circle.
	 * 
	 * @param greatCircle
	 *            a great circle structure
	 * @param distance
	 *            distance in radians from first point of great circle
	 * @return unit vector of point which is on great circle and located
	 *         specified distance away from first point of great circle.
	 */
	public static double[] getGreatCirclePoint(double[][] greatCircle,
			double distance)
	{
		double[] v = new double[3];
		getGreatCirclePoint(greatCircle, distance, v);
		return v;
	}

	/**
	 * A great circle is defined by two unit vectors that are 90 degrees apart.
	 * A great circle is stored in a double[2][3] array and one can be obtained
	 * by calling one of the getGreatCircle() methods.
	 * <p>
	 * In this method, a great circle and a distance are specified and a point
	 * is returned which is on the great circle path and is the specified
	 * distance away from the first point of the great circle.
	 * 
	 * @param greatCircle
	 *            a great circle structure
	 * @param distance
	 *            distance in radians from first point of great circle
	 * @param v
	 *            unit vector of point which is on great circle and located
	 *            specified distance away from first point of great circle.
	 */
	public static void getGreatCirclePoint(double[][] greatCircle,
			double distance, double[] v)
	{
		double cosa = cos(distance);
		double sina = sin(distance);
		v[0] = cosa * greatCircle[0][0] + sina * greatCircle[1][0];
		v[1] = cosa * greatCircle[0][1] + sina * greatCircle[1][1];
		v[2] = cosa * greatCircle[0][2] + sina * greatCircle[1][2];
	}

	/**
	 * Move unit vector w specified distance in direction given by azimuth.
	 * If w is north or south pole, returns null.
	 * 
	 * @param w double[] unit vector of starting position
	 * @param distance distance to move in radians
	 * @param azimuth direction to move in radians
	 * @return  unit vector. If w is north or south pole, returns null.
	 */
	public static double[] move(double[] w, double distance, double azimuth)
	{
		double[] n = new double[3];
		if (move(w, distance, azimuth, n)) return n;
		return null;
	}

	/**
	 * Move unit vector w specified distance in direction given by azimuth and
	 * return the result in u. If w is north or south pole, u will be equal to
	 * the same pole and method returns false.
	 * 
	 * @param w
	 *            double[] unit vector of starting position
	 * @param distance
	 *            distance to move in radians
	 * @param azimuth
	 *            direction to move in radians
	 * @param u
	 *            double[] unit vector of resulting position.
	 * @return true if successful, false if w is north or south pole
	 */
	public static boolean move(double[] w, double distance, double azimuth,
			double[] u)
	{
		double[] n = new double[3];
		if (moveNorth(w, distance, n))
		{
			rotate(n, w, azimuth, u);
			return true;
		}
		u[0] = w[0];
		u[1] = w[1];
		u[2] = w[2];
		return false;
	}

	/**
	 * Return a unit vector that is distance radians due north of positon x. If
	 * x is the north or south pole, then z is set equal to x.
	 * 
	 * @param x
	 *            the position to be moved.
	 * @param distance
	 *            the distance, in radians, that x is to be moved toward the
	 *            north.
	 * @return
	 *            the 3-element unit vector representing the position after
	 *            having moved distance north, or null if x is north or south pole.
	 */
	public static double[] moveNorth(double[] x, double distance)
	{
		double[] z = new double[3];
		if (moveNorth(x, distance, z))
			return z;
		return null;
	}

	/**
	 * Return a unit vector that is distance radians due north of positon x. If
	 * x is the north or south pole, then z is set equal to x.
	 * 
	 * @param x
	 *            the position to be moved.
	 * @param distance
	 *            the distance, in radians, that x is to be moved toward the
	 *            north.
	 * @param z
	 *            the 3-element unit vector representing the position after
	 *            having moved distance north.
	 * @return true if operation successful, false if x is north or south pole.
	 */
	public static boolean moveNorth(double[] x, double distance, double[] z)
	{
		double[] vtp = new double[3];
		if (vectorTripleProductNorthPole(x, vtp))
		{
			move(x, vtp, distance, z);
			return true;
		}

		z[0] = x[0];
		z[1] = x[1];
		z[2] = x[2];
		return false;
	}

	/**
	 * Rotate unit vector x clockwise around unit vector p, by angle a. Angle a
	 * is in radians and rotation is clockwise when viewed from outside the unit
	 * sphere.
	 * <p>
	 * x and z may be references to the same array.
	 * 
	 * @param x
	 *            vector to be rotated
	 * @param p
	 *            pole about which rotation is to occur.
	 * @param a
	 *            the amount of rotation, in radians.
	 * @param z
	 *            the rotated vector, normalized to unit length.
	 */
	public static void rotate(double[] x, double[] p, double a, double[] z)
	{
		if (abs(a) < 1e-15)
		{
			z[0] = x[0];
			z[1] = x[1];
			z[2] = x[2];
			return;
		}

		double d = x[0] * p[0] + x[1] * p[1] + x[2] * p[2]; // dot product
		// if x and p are parallel, x needs no rotation.
		if (abs(d) > 1. - 1e-15)
		{
			z[0] = x[0];
			z[1] = x[1];
			z[2] = x[2];
			return;
		}

		double cosa = cos(a);
		double sina = sin(a);
		d *= (1 - cosa);
		double z0 = cosa * x[0] + d * p[0] - sina * (p[1] * x[2] - p[2] * x[1]);
		double z1 = cosa * x[1] + d * p[1] - sina * (p[2] * x[0] - p[0] * x[2]);
		double z2 = cosa * x[2] + d * p[2] - sina * (p[0] * x[1] - p[1] * x[0]);
		double len = sqrt(z0 * z0 + z1 * z1 + z2 * z2);
		z[0] = z0 / len;
		z[1] = z1 / len;
		z[2] = z2 / len;
	}

	/**
	 * Rotate unit vector p0 toward unit vector p1 by fractional angle f and store
	 * result in z. If f = 0 then z = p0. If f = 1 then z = p1.
	 * 
	 * @param p0
	 *            Start vector to be rotated toward p1 by fraction a. 
	 * @param p1
	 *            End vector toward which p0 is rotated by angle a.
	 * @param f
	 *            The fractional angle (between p0 and p1) to be rotated.
	 * @param z
	 *            The rotated vector of unit length.
	 */
	public static void rotatePlane(double[] p0, double[] p1, double f, double[] z)
	{
		double[] y = {0.0, 0.0, 0.0};
		vectorTripleProduct(p0, p1, p0, y);
		normalize(y);

		double ang = f * acos(dot(p0, p1));
		rotateVector(p0, y, ang, z);
	}

	/**
	 * Rotate unit vector p0 toward unit vector y by angle ang and store
	 * result in z. Standard rotation in a plane if y dot p0 is 1 (90 deg).
	 * 
	 * @param p0
	 *            Start vector to be rotated toward y by angle ang. 
	 * @param y
	 *            End vector orthogonal to p0 in some plane.
	 * @param f
	 *            The rotation angle (from p0 toward y).
	 * @param z
	 *            The rotated vector of unit length.
	 */
	public static void rotateVector(double[] p0, double[] y, double ang, double[] z)
	{
		double cosa = cos(ang);
		double sina = sin(ang);
		
		z[0] = p0[0] * cosa + y[0] * sina;
		z[1] = p0[1] * cosa + y[1] * sina;
		z[2] = p0[2] * cosa + y[2] * sina;
		normalize(z);
	}

	/**
	 * Compute the normalized vector triple product (v0 x v1) x v2 and store
	 * result in rslt. It is ok if rslt is a reference to one of the input
	 * vectors. Local variables are used to ensure memory is not corrupted.
	 * 
	 * @param v0
	 *            double[]
	 * @param v1
	 *            double[]
	 * @param v2
	 *            double[]
	 * @param rslt
	 *            double[]
	 * @return true if rslt has finite length, false if length(rslt) is zero.
	 */
	public static boolean vectorTripleProduct(double[] v0, double[] v1,
			double[] v2, double[] rslt)
	{
		// set q = v0 cross v1
		double q0 = v0[1] * v1[2] - v0[2] * v1[1];
		double q1 = v0[2] * v1[0] - v0[0] * v1[2];
		double q2 = v0[0] * v1[1] - v0[1] * v1[0];

		// set w = q cross v2
		double w0 = q1 * v2[2] - q2 * v2[1];
		double w1 = q2 * v2[0] - q0 * v2[2];
		double w2 = q0 * v2[1] - q1 * v2[0];

		// set rslt = w
		rslt[0] = w0;
		rslt[1] = w1;
		rslt[2] = w2;

		// normalize rslt to unit length. if the length
		// of v1 or v2 is zero or they are nearly parallel then
		// rslt will = {0,0,0} and the function will return false;
		return normalize(rslt) != 0.;
	}

	/**
	 * Compute the normalized vector triple product (v0 x v1) x v0 and
	 * and store result in rslt.  It is ok if rslt is a reference to 
	 * v1 or v2.  Local variables are used to ensure memory is not 
	 * stepped on.
	 * 
	 * @param v0 double[]
	 * @param v1 double[]
	 * @param rslt double[]
	 * @return true if rslt has finite length, false if length(rslt) is zero.
	 */
	public static boolean vectorTripleProduct(double[] v0, double[] v1, double[] rslt)
	{
		double q0, q1, q2, w0, w1, w2;

		// set q = v1 cross v2
		q0 = v0[1] * v1[2] - v0[2] * v1[1];
		q1 = v0[2] * v1[0] - v0[0] * v1[2];
		q2 = v0[0] * v1[1] - v0[1] * v1[0];

		// set w = q cross v1
		w0 = q1 * v0[2] - q2 * v0[1];
		w1 = q2 * v0[0] - q0 * v0[2];
		w2 = q0 * v0[1] - q1 * v0[0];

		// set rslt = w
		rslt[0] = w0;
		rslt[1] = w1;
		rslt[2] = w2;

		// normalize rslt to unit length.  if the length
		// of v1 or v2 is zero or they are nearly parallel then
		// rslt will = {0,0,0} and the function will return false;
		return normalize(rslt) != 0.;
	}

	/**
	 * Compute the normalized vector triple product (u x northPole) x u and
	 * store result in w. Returns false is u is north or south pole.
	 * 
	 * @param u
	 *            double[]
	 * @param w
	 *            double[]
	 * @return true if w has finite length, false if length(w) is zero.
	 */
	public static boolean vectorTripleProductNorthPole(double[] u, double[] w)
	{
		w[0] = -u[0] * u[2];
		w[1] = -u[1] * u[2];
		w[2] = u[1] * u[1] + u[0] * u[0];
		return normalize(w) != 0.;
	}

	/**
	 * Given three unit vectors, v0, v1 and v2, find the circumcenter, vs. The
	 * circumcenter is the unit vector of the center of a small circle that has
	 * all three unit vectors on its circumference.
	 * 
	 * @param v0
	 * @param v1
	 * @param v2
	 * @param vs
	 * @return true if successful, false if the three input vectors do not
	 *         define a triangle.
	 */
	public static boolean circumCenter(double[] v0, double[] v1, double[] v2,
			double[] vs)
	{
		vs[0] = v0[1] * (v2[2] - v1[2]) + v2[1] * (v1[2] - v0[2]) +
				    v1[1] * (v0[2] - v2[2]);
		vs[1] = v0[2] * (v2[0] - v1[0]) + v2[2] * (v1[0] - v0[0]) +
				    v1[2] * (v0[0] - v2[0]);
		vs[2] = v0[0] * (v2[1] - v1[1]) + v2[0] * (v1[1] - v0[1]) +
				    v1[0] * (v0[1] - v2[1]);

		return normalize(vs) > 0.;
	}

	/**
	 * Given three unit vectors, v0, v1 and v2, find the circumcenter, vs. The
	 * circumcenter is the unit vector of the center of a small circle that has
	 * all three unit vectors on its circumference. 
	 * 
	 * <p>The fourth element of vs is the dot product of the new circumcenter 
	 * with one of the vertices.  In other words, cc[3] = cos(ccRadius).
	 * 
	 * @param v0
	 * @param v1
	 * @param v2
	 * @param vs 4-element array populated with results.
	 */
	public static void circumCenterPlus(double[] v0, double[] v1, double[] v2,
			double[] vs)
	{
		vs[0] = v0[1] * (v2[2] - v1[2]) + v2[1] * (v1[2] - v0[2]) +
				    v1[1] * (v0[2] - v2[2]);
		vs[1] = v0[2] * (v2[0] - v1[0]) + v2[2] * (v1[0] - v0[0]) +
				    v1[2] * (v0[0] - v2[0]);
		vs[2] = v0[0] * (v2[1] - v1[1]) + v2[0] * (v1[1] - v0[1]) +
				    v1[0] * (v0[1] - v2[1]);
		double len = vs[0] * vs[0] + vs[1] * vs[1] + vs[2] * vs[2];
		len = sqrt(len);
		vs[0] /= len;
		vs[1] /= len;
		vs[2] /= len;
		vs[3] = dot(vs, v0);
	}

	/**
	 * Given three unit vectors, v0, v1 and v2, find the circumcenter, vs. The
	 * circumcenter is the unit vector of the center of a small circle that has
	 * all three unit vectors on its circumference. Vectors must be specified in
	 * clockwise order.
	 * 
	 * @param v0
	 * @param v1
	 * @param v2
	 * @return the circumCenter (a unit vector).
	 */
	public static double[] circumCenter(double[] v0, double[] v1, double[] v2)
	{
		double[] vs = new double[3];
		vs[0] = v0[1] * (v2[2] - v1[2]) + v2[1] * (v1[2] - v0[2]) +
				    v1[1] * (v0[2] - v2[2]);
		vs[1] = v0[2] * (v2[0] - v1[0]) + v2[2] * (v1[0] - v0[0]) +
				    v1[2] * (v0[0] - v2[0]);
		vs[2] = v0[0] * (v2[1] - v1[1]) + v2[0] * (v1[1] - v0[1]) +
				    v1[0] * (v0[1] - v2[1]);

		double len = vs[0] * vs[0] + vs[1] * vs[1] + vs[2] * vs[2];
		len = sqrt(len);
		vs[0] /= len;
		vs[1] /= len;
		vs[2] /= len;
		return vs;
	}

	/**
	 * Given three unit vectors, v0, v1 and v2, find the circumcenter, vs. The
	 * circumcenter is the unit vector of the center of a small circle that has
	 * all three unit vectors on its circumference. Vectors must be specified in
	 * clockwise order.
	 * 
	 * <p>The fourth element of vs is the dot product of the new circumcenter 
	 * with one of the vertices.  In other words, cc[3] = cos(ccRadius).
	 * 
	 * @param v0
	 * @param v1
	 * @param v2
	 * @return the circumCenter: a unit vector and cosine of circumCircle radius.
	 */
	public static double[] circumCenterPlus(double[] v0, double[] v1, double[] v2)
	{
		double[] vs = new double[4];
		vs[0] = v0[1] * (v2[2] - v1[2]) + v2[1] * (v1[2] - v0[2]) +
				    v1[1] * (v0[2] - v2[2]);
		vs[1] = v0[2] * (v2[0] - v1[0]) + v2[2] * (v1[0] - v0[0]) +
				    v1[2] * (v0[0] - v2[0]);
		vs[2] = v0[0] * (v2[1] - v1[1]) + v2[0] * (v1[1] - v0[1]) +
				    v1[0] * (v0[1] - v2[1]);

		double len = vs[0] * vs[0] + vs[1] * vs[1] + vs[2] * vs[2];
		len = sqrt(len);
		vs[0] /= len;
		vs[1] /= len;
		vs[2] /= len;
		vs[3] = dot(vs, v0);
		return vs;
	}

	/**
	 * Given the three unit vectors, t[0], t[1] and t[2], find the circumcenter,
	 * vs. The circumcenter is the unit vector of the center of a small circle
	 * that has all three unit vectors on its circumference.
	 * 
	 * @param t
	 *            a 3 x 3 array of doubles that contains the three unit vectors
	 *            of a triangle.
	 * @param vs
	 * @return true if successful, false if the three input vectors do not
	 *         define a triangle.
	 */
	public static boolean circumCenter(double[][] t, double[] vs)
	{
		return circumCenter(t[0], t[1], t[2], vs);
	}

	/**
	 * Move unit vector w in direction of vtp by distance a and store result in
	 * u. vtp is assumed to be a unit vector normal to w on input.
	 * <p>
	 * If u and w are references to the same unit vector, then contents of w
	 * will be replaced with new location.
	 * 
	 * @param w
	 *            double[]
	 * @param vtp
	 *            double[]
	 * @param a
	 *            double
	 * @param u
	 *            double[]
	 */
	public static void move(double[] w, double[] vtp, double a, double[] u)
	{
		double cosa = cos(a);
		double sina = sin(a);
		u[0] = cosa * w[0] + sina * vtp[0];
		u[1] = cosa * w[1] + sina * vtp[1];
		u[2] = cosa * w[2] + sina * vtp[2];
	}

	/**
	 * Normalized cross product of two 3-component unit vectors.
	 * 
	 * @param u
	 *            vector one.
	 * @param v
	 *            vector two.
	 * @return Normalized cross product of u x v. Will be [0,0,0] if u and v are
	 *         parallel.
	 */
	public static double[] crossNormal(double[] u, double[] v)
	{
		double[] w = new double[3];
		crossNormal(u, v, w);
		return w;
	}

	/**
	 * Normalized cross product of two 3-component vectors.
	 * 
	 * @param u
	 *            vector one.
	 * @param v
	 *            vector two.
	 * @param w
	 *            set to u cross v, normalized to unit length. If u cross v has
	 *            zero length, w will equal (0,0,0).
	 * @return the length of u cross v prior to normalization. Guaranteed >= 0.
	 */
	public static double crossNormal(double[] u, double[] v, double[] w)
	{
		w[0] = u[1] * v[2] - u[2] * v[1];
		w[1] = u[2] * v[0] - u[0] * v[2];
		w[2] = u[0] * v[1] - u[1] * v[0];
		return normalize(w);
	}

	/**
	 * Normalized cross product of a 3-component unit vector with the north
	 * pole.
	 * 
	 * @param u
	 *            vector<double> vector one.
	 * @param w
	 *            set to u cross north, normalized to unit length. If u cross
	 *            north has zero length, w will equal (0,0,0).
	 * @return the length of u cross north prior to normalization. Guaranteed >=
	 *         0.
	 */
	public static double crossNorth(double[] u, double[] w)
	{
		double len = u[0] * u[0] + u[1] * u[1];
		if (len <= 0.)
		{
			len = w[0] = w[1] = w[2] = 0.;
		}
		else
		{
			len = sqrt(len);
			w[0] = u[1] / len;
			w[1] = -u[0] / len;
			w[2] = 0.;
		}
		return len;
	}

	/**
	 * @param v0 a unit vector at a corner of the triangle
	 * @param v1 a unit vector at a corner of the triangle
	 * @param v2 a unit vector at a corner of the triangle
	 * @return the area of a triangle defined by three 3-component vectors
	 */
	public static double getTriangleArea(double[] v0, double[] v1, double[] v2)
	{
		double v10 = v1[0] - v0[0];
		double v11 = v1[1] - v0[1];
		double v12 = v1[2] - v0[2];
		double v20 = v2[0] - v0[0];
		double v21 = v2[1] - v0[1];
		double v22 = v2[2] - v0[2];
		
		double u0 = v11 * v22 - v12 * v21; 
		double u1 = v12 * v20 - v10 * v22; 
		double u2 = v10 * v21 - v11 * v20; 
		
		return sqrt(u0 * u0 + u1 * u1 + u2 * u2) / 2.;
//		double[] v10 = new double[] { v1[0] - v0[0], v1[1] - v0[1], v1[2] - v0[2] };
//		double[] v20 = new double[] { v2[0] - v0[0], v2[1] - v0[1], v2[2] - v0[2] };
//		double[] u = new double[] { v10[1] * v20[2] - v10[2] * v20[1], v10[2] * v20[0]
//				- v10[0] * v20[2], v10[0] * v20[1] - v10[1] * v20[0] };
//		return sqrt(u[0] * u[0] + u[1] * u[1] + u[2] * u[2]) / 2.;
	}

	/**
	 * returns true if unit vector u is very close to [0, 0, +/- 1]
	 * 
	 * @param u
	 *            unit vector
	 * @return true if unit vector u is very close to [0, 0, +/- 1]
	 */
	public static boolean isPole(double[] u)
	{
		return (u[0] * u[0] + u[1] * u[1]) < 1e-15;
	}

	/**
	 * returns true if unit vector u and v are parallel or very close to it
	 * 
	 * @param u
	 *            a unit vector
	 * @param v
	 *            another unit vector
	 * @return 1.-abs(dot(u,v)) < 2e-15
	 */
	public static boolean parallel(double[] u, double[] v)
	{
		return 1. - abs(u[0] * v[0] + u[1] * v[1] + u[2] * v[2]) < 2e-15;
	}

	/**
	 * Normalizes the input vector to unit length. Returns the length of the
	 * vector prior to normalization.
	 * 
	 * @param u
	 *            vector<double>
	 * @return length of the vector prior to normalization ( >= 0.)
	 */
	public static double normalize(double[] u)
	{
		double len = u[0] * u[0] + u[1] * u[1] + u[2] * u[2];
		if (len > 1e-30)
		{
			len = sqrt(len);
			u[0] /= len;
			u[1] /= len;
			u[2] /= len;
		}
		else
		{
			len = u[0] = u[1] = u[2] = 0.0;
		}
		return len;
	}

	/**
	 * Find the length of a 3-element vector.
	 * 
	 * @param u
	 *            double[]
	 * @return the length of the vector. Guaranteed to be >= 0.
	 */
	public static double length(double[] u)
	{
		double l = u[0] * u[0] + u[1] * u[1] + u[2] * u[2];
		if (l > 0.)
			return sqrt(l);
		else
		  return 0.;
	}

	/**
	 * Return the normalized vector sum of the supplied unit vectors.
	 * 
	 * @param v
	 *            one or more unit vectors
	 * @return the normalized vector sum of the supplied unit vectors.
	 */
	public static double[] center(double[]... v)
	{
		double[] rslt = new double[3];
		if (v.length == 1)
		{
			rslt[0] = v[0][0]; rslt[1] = v[0][1]; rslt[2] = v[0][2];
		}
		else if (v.length > 1)
		{
			for (int i = 0; i < v.length; ++i)
			{
				rslt[0] += v[i][0];
				rslt[1] += v[i][1];
				rslt[2] += v[i][2];
			}
			normalize(rslt);
		}
		return rslt;
	}

	/**
	 * Transform is a 3 x 3 matrix such that when a vector is multiplied by
	 * transform, the vector will be projected onto the plane of the
	 * GreatCircle from u to v. The z direction will point out of the plane of the great
	 * circle in the direction of the observer (v cross u; parallel to normal).
	 * The y direction will correspond to the mean of u and v. The x direction
	 * will correspond to y cross z, forming a right handed coordinate system.
	 * 
	 * @param u
	 *            left hand unit vector
	 * @param v
	 *            right hand unit vector
	 * @return 3 x 3 transformation matrix.
	 * 
	 * @throws GeoTessException
	 */
	public static double[][] getTranform(double[] u, double[] v)
			throws IOException
	{
		double[][] t = new double[3][3];

		// t[0] will be x direction -- observer's right
		// t[1] will be y direction -- observer's up
		// t[2] will be z direction -- points toward the observer

		// set t[2] equal to unit vector normal to plan containing u and v
		if (crossNormal(v, u, t[2]) == 0.)
			throw new IOException("u and v are parallel: |v x u| == 0");

		// set t[1] to mean of vectors u and v, normalized to unit length.
		t[1][0] = u[0] + v[0];
		t[1][1] = u[1] + v[1];
		t[1][2] = u[2] + v[2];
		if (normalize(t[1]) == 0.)
			throw new IOException("u and v are anti-parallel");

		// set t[0] equal to t[1] cross t[2]
		crossNormal(t[1], t[2], t[0]);
		return t;
	}

	/**
	 * Project vector x onto the plane of a great circle. Consider a great
	 * circle defined by two unin vectors, u and v. Find the transform of x by
	 * calling t = getTransform(u, v). Then call this method: transform(x, t,
	 * g), which will calculate unit vector g such that
	 * <ul>
	 * <li>g[2] is the z direction, i.e., the component of x that points out of
	 * the plane of the great circle, toward the observer (v cross u).
	 * <li>g[1] is the y direction, i.e., the mean of u and v, and
	 * <li>g[0] is the x direction, i.e, g[1] cross g2.
	 * </ul>
	 * 
	 * @param x
	 *            double[] the 3 element array containing the vector to be
	 *            projected.
	 * @param transform
	 *            the 3 x 3 transform matrix obtained by calling
	 *            <i>getTransform(u,v)</i>
	 * @param g
	 *            double[] the projection of x onto plane of this GreatCircle
	 * @throws GeoTessException
	 */
	public static void transform(double[] x, double[][] transform, double[] g)
	{
		g[0] = x[0] * transform[0][0] + x[1] * transform[0][1] +
				   x[2] * transform[0][2];
		g[1] = x[0] * transform[1][0] + x[1] * transform[1][1] +
				   x[2] * transform[1][2];
		if (g.length > 2)
			g[2] = x[0] * transform[2][0] + x[1] * transform[2][1] +
			       x[2] * transform[2][2];
	}

	/**
	 * Return the dot product of two vectors.
	 * 
	 * @param v0
	 *            a 3 component vector
	 * @param v1
	 *            a 3 component vector
	 * @return dot product
	 */
	public static double dot(double[] v0, double[] v1)
	{
		return v0[0] * v1[0] + v0[1] * v1[1] + v0[2] * v1[2];
	}

	/**
	 * Calculate the scalar triple product of 3 3-component vectors: (v0 cross
	 * v1) dot v2
	 * 
	 * @param v0
	 *            double[]
	 * @param v1
	 *            double[]
	 * @param v2
	 *            double[]
	 * @return scalar triple product (v0 cross v1) dot v2
	 */
	public static double scalarTripleProduct(double[] v0, double[] v1,
			double[] v2)
	{
		return v0[0] * v1[1] * v2[2] + v1[0] * v2[1] * v0[2] +
				   v2[0] * v0[1] * v1[2] - v2[0] * v1[1] * v0[2] -
				   v0[0] * v2[1] * v1[2] - v1[0] * v0[1] * v2[2];
	}

	/**
	 * Cross product of two 3-component vectors. Result is not normalized.
	 * 
	 * @param v1
	 *            vector<double> vector one.
	 * @param v2
	 *            vector<double> vector two.
	 * @return v1 cross v2 Result is not a unit vector.
	 */
	public static double[] cross(double[] v1, double[] v2)
	{
		double[] n = new double[3];
		cross(v1, v2, n);
		return n;
	}

	/**
	 * Cross product of two 3-component vectors. Result is not normalized.
	 * 
	 * @param v1
	 *            vector<double> vector one.
	 * @param v2
	 *            vector<double> vector two.
	 * @param rslt
	 *            set to v1 cross v2 Result is not a unit vector.
	 */
	public static void cross(double[] v1, double[] v2, double[] rslt)
	{
		rslt[0] = v1[1] * v2[2] - v1[2] * v2[1];
		rslt[1] = v1[2] * v2[0] - v1[0] * v2[2];
		rslt[2] = v1[0] * v2[1] - v1[1] * v2[0];
	}
	
	/*
	 * Find the equation of the plane defined by 3 vectors.  The equation of the plane is of the form
	 * ax + by + cz = 1.  The 3 coefficients a, b and c are returnd in the supplied 3-element array 'plane'.
	 * If the 3 points are colinear, plane is populated with NaN.
	 */
	public static double[] getPlane(double[] u0, double r0, double[] u1, double r1, double[] u2, double r2)
	{
		double[] plane = new double[3];
		getPlane(u0, r0, u1, r1, u2, r2, plane);
		return plane;
	}

	/*
	 * Find the equation of the plane defined by 3 vectors.  The equation of the plane is of the form
	 * ax + by + cz = 1.  The 3 coefficients a, b and c are returnd in the supplied 3-element array 'plane'.
	 * If the 3 points are colinear, plane is populated with NaN.
	 */
	public static void getPlane(double[] u0, double r0, double[] u1, double r1, double[] u2, double r2, double[] plane)
	{
		double[] v = new double[] { u0[0]*r0, u0[1]*r0, u0[2]*r0 };

		// take the cross product of u2-u0 X u1-u0
		cross(new double[] {u2[0]*r2-v[0], u2[1]*r2-v[1], u2[2]*r2-v[2] },
				  new double[] {u1[0]*r1-v[0], u1[1]*r1-v[1], u1[2]*r1-v[2] },
				  plane);

		double d = dot(plane, v);

		if (d == 0)
			plane[0] = plane[1] = plane[2] = Double.NaN;
		else
		{
			plane[0] /= d;
			plane[1] /= d;
			plane[2] /= d;
		}
  }

	/*
	 * Find the intersection of a line and a plane.  Given a unit vector, u,
	 * find r such that u*r will lie in the plane P.
	 */
	public static double getIntersection(double[] plane, double[] u)
	{
		// equation of plane is p[0]x + p[1]y + p[2]z = 1
		
		double d = dot(plane, u);

		return d == 0 ? Double.NaN : 1./d;
	}

	/**
	 * Given three Euler angles in radians, retreive the Euler rotation matrix.
	 * 
	 * <p>Euler rotation angles:
	 * <p>Given two coordinate systems xyz and XYZ with common origin,
	 * starting with the axis z and Z overlapping, the position of
	 * the second can be specified in terms of the first using
	 * three rotations with angles A, B, C as follows:
	 * 
	 * <ol>
	 * <li>Rotate the xyz-system about the z-axis by A.
	 * <li>Rotate the xyz-system again about the now rotated x-axis by B. 
	 * <li>Rotate the xyz-system a third time about the new z-axis by C.
	 * </ol>
	 * 
	 * <p>Clockwise rotations, when looking in direction of vector, are positive. 
	 * <p>Reference: http://mathworld.wolfram.com/EulerAngles.html
	 * @param e double[3] the 3 Euler rotation angles, phi, theta and psi, in radians
	 * @return double[3][3] Euler rotation matrix
	 */
	public static double[][] getEulerMatrix(double ... e)
	{
		double[] a0 = new double[3];
		double[] a1 = new double[3];
		double[] a2 = new double[3];
		double cose0 = cos(e[0]); double sine0 = sin(e[0]);
		double cose1 = cos(e[1]); double sine1 = sin(e[1]);
		double cose2 = cos(e[2]); double sine2 = sin(e[2]);

		a0[0] =  cose2 * cose0 - cose1 * sine0 * sine2;
		a0[1] =  cose2 * sine0 + cose1 * cose0 * sine2;
		a0[2] =  sine2 * sine1;
		a1[0] = -sine2 * cose0 - cose1 * sine0 * cose2;
		a1[1] = -sine2 * sine0 + cose1 * cose0 * cose2;
		a1[2] =  cose2 * sine1;
		a2[0] =  sine1 * sine0;
		a2[1] = -sine1 * cose0;
		a2[2] =  cose1;
		return new double[][] {a0, a1, a2};
	}

	/**
	 * Given an Euler rotation matrix computed from 3 Euler rotation angles
	 * using method getEulerMatrix(), apply the rotation to 3-component 
	 * unit vector u and return the result in a new 3-component unit vector.
	 * <p>Reference: http://mathworld.wolfram.com/EulerAngles.html
	 * @param u 3-component unit vector to be rotated
	 * @param euler 3x3 euler rotation matrix.
	 * @return rotated 3-component unit vector 
	 */
	public static double[] eulerRotation(double[] u, double[][] euler)
	{
		return new double[] {
				u[0]*euler[0][0]+u[1]*euler[1][0]+u[2]*euler[2][0],
				u[0]*euler[0][1]+u[1]*euler[1][1]+u[2]*euler[2][1],
				u[0]*euler[0][2]+u[1]*euler[1][2]+u[2]*euler[2][2]
		};
	}

	/**
	 * Given an Euler rotation matrix computed from 3 Euler rotation angles
	 * using method getEulerMatrix(), apply the rotation to 3-component 
	 * unit vector u and return the result in unit vector v.  u and v
	 * can be references to the same array.
	 * <p>Reference: http://mathworld.wolfram.com/EulerAngles.html
	 * @param u 3-component unit vector to be rotated
	 * @param euler 3x3 euler rotation matrix.
	 * @param v rotated 3-component unit vector 
	 */
	public static void eulerRotation(double[] u, double[][] euler, double[] v)
	{
		double u0 = u[0];
		double u1 = u[1];
		double u2 = u[2];
		v[0] = u0*euler[0][0]+u1*euler[1][0]+u2*euler[2][0];
		v[1] = u0*euler[0][1]+u1*euler[1][1]+u2*euler[2][1];
		v[2] = u0*euler[0][2]+u1*euler[1][2]+u2*euler[2][2];
	}
	
	/**
	 * Compute points that define an small circle at a specified point.
	 * @param center the unit vector representing the center of the ellipse
	 * @param the radius of the small circle, in radians
	 * @param npoints the number of points to define the circle
	 * @return an array of length npoints containing the unit vectors that 
	 * define the small circle.
	 */
	public static double[][] getSmallCircle(double[] center, double radius, int npoints)
	{
		double[][] points = new double[npoints][3];
		VectorUnit.moveNorth(center, radius, points[0]);
		
		for (int i=1; i<npoints; ++i)
			VectorUnit.rotate(points[0], center, i * 2*PI/(npoints-1), points[i]);
		return points;
	}
	
	/**
	 * Compute points that define an ellipse at a specified point.
	 * @param center the unit vector representing the center of the ellipse
	 * @param major the length of the major axis of the ellipse, in radians.
	 * @param minor the length of the minor axis of the ellipse, in radians.
	 * @param trend the orientation relative to north of the major axis of the 
	 * ellipse, in radians.
	 * @param npoints the number of points to define the ellipse
	 * @return an array of length npoints containing the unit vectors that 
	 * define the ellipse.
	 */
	public static double[][] getEllipse(double[] center, double major, double minor, double trend, int npoints)
	{
		double[][] points = new double[npoints][3];
		double a,b, r, theta, da = 2*PI/(npoints-1);
		for (int i=0; i<npoints; ++i)
		{
			theta = i*da;
			a = major*sin(theta);
			b = minor*cos(theta);
			r = major*minor/sqrt(a*a+b*b);
			move(center, r, trend+theta, points[i]);
		}
		return points;
	}
	
	/**
	 * Return true if the specified point resides within the specified ellipse.
	 * @param center the unit vector representing the center of the ellipse
	 * @param major the length of the major axis of the ellipse, in radians.
	 * @param minor the length of the minor axis of the ellipse, in radians.
	 * @param trend the orientation relative to north of the major axis of the 
	 * ellipse, in radians.
	 * @param point the unit vector of the point to be tested.
	 * @return true if the specified point resides within the specified ellipse.
	 */
	public static boolean inEllipse(double[] center, double major, double minor, double trend, double[] point)
	{
		double theta = trend - azimuth(center, point, 0.);
		double a = major*sin(theta);
		double b = minor*cos(theta);
		double r = major*minor/sqrt(a*a+b*b);
		return angle(center, point) < r;
	}
}
