package gms.shared.utilities.geotess.util.numerical.polygon;

import static java.lang.Math.PI;

import gms.shared.utilities.geotess.util.numerical.vector.EarthShape;
import gms.shared.utilities.geotess.util.numerical.vector.VectorUnit;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

/**
 * The GreatCircle class manages the information about a great circle path that
 * extends from one point to another point, both or which are located on the
 * surface of a unit sphere. It supports great circles where the distance from
 * the firstPoint to the lastPoint are 0 to 2*PI radians apart, inclusive.
 * Either or both of the points may coincide with one of the poles of the Earth.
 * 
 * <p>There is a method to retrieve a point that is located on the great circle at
 * some specified distance from the first point of the great circle.
 * 
 * <p>The method getIntersection(other, inRange) will return a point that is
 * located at the intersection of two great circles. In general, two great
 * circles intersect at two points, and this method returns the one that is
 * encountered first as one moves away from the first point of the first
 * GreatCircle. If the Boolean argument <i>inRange</i> is true, then the method
 * will only return a point if the point falls within the range of both great
 * circles. In other words, the point of intersection has to reside in between
 * the first and last point of both great circles. If <i>inRange</i> is false,
 * then that constraint is not applied.
 * 
 * <p>GreatCircle has the ability to transform the coordinates of an input point so
 * that it resides in the plane of the great circle. This is useful for
 * extracting slices from a 3D model for plotting purposes. The z-coordinate of
 * the transformed point will point out of the plane of the great circle toward
 * the observer. The y-coordinate of the transformed point will be equal to the
 * normalized vector sum of the first and last point of the great circle and the
 * x-coordinate will be y cross z.
 * 
 * <p>The key to successfully defining a great circle path is successfully
 * determining the unit vector that is normal to the plane of the great circle
 * (firstPoint cross lastPoint, normalized to unit length). For great circles
 * where the distance from firstPoint to lastPoint is more than zero and less
 * than PI radians, this is straightforward. But for great circles longer than
 * PI radians, great circles of exactly zero, PI or 2*PI radians length, or
 * great circles where the first point resides on one of the poles,
 * complications arise.
 * 
 * <p>To determine the normal to the great circle, three constructors are provided
 * (besides the default constructor that does nothing).
 * 
 * <p>The first constructor is the most general. It takes four arguments:
 * firstPoint (unit vector), intermediatePoint (unit vector), lastPoint (unit
 * vector) and shortestPath (boolean). The normal is computed as firstPoint
 * cross lastPoint normalized to unit length. If the distance from firstPoint to
 * lastPoint is greater than zero and less than PI radians, then the resulting
 * normal will have finite length and will have been successfully computed. If,
 * however, the distance from firstPoint to lastPoint is exactly 0 or PI
 * radians, then normal will have zero length. In this case, a second attempt to
 * compute the normal is executed by computing firstPoint cross
 * intermediatePoint. If this is successful, the calculation proceeds. If not
 * successful, then the normal is computed as the first of: firstPoint cross Z,
 * firstPoint cross Y or firstPoint cross X, whichever produces a finite length
 * normal first. Z is the north pole, Y is (0N, 90E) and X is (0N, 0E). One of
 * these calculations is guaranteed to produce a valid normal. Once the normal
 * has been computed, then the shortestPath argument is considered. If
 * shortestPath is true, then no further action is taken, resulting in a great
 * circle with length less than or equal to PI radians. If shortestPath is false
 * then the normal is negated, effectively forcing the great circle to go the
 * long way around the globe to get from firstPoint to lastPoint. When
 * shortestPath is false the length of the great circle will be >= PI and <=
 * 2*PI. For example, when shortestPath is true, a great circle path from (10N,
 * 0E) to (30N, 0E) will proceed in a northerly direction for a distance of 20
 * degrees to get from firstPoint to lastPoint. But if shortestPath is false,
 * the great circle will proceed in a southerly direction for 340 degrees to get
 * from firstPoint to lastPoint.
 * 
 * <p>The second constructor is a simplification of the first, taking only 3
 * arguments: firstPoint (unit vector), lastPoint (unit vector) and shortestPath
 * (boolean). It calls the first constructor with intermediatePoint set to NULL.
 * This is useful in cases where the calling application is certain that great
 * circles of length exactly 0 or PI radians will not happen or is willing to
 * accept an arbitrary path if it does happens.
 * 
 * <p>There is a third constructor that takes 3 arguments: firstPoint (unit
 * vector), distance (radians) and azimuth (radians). The lastPoint of the great
 * circle is computed by moving the first point the specified distance in the
 * specified direction. This constructor can produce great circles where the
 * distance from firstPoint to lastPoint is >= 0 and <= 2*PI, inclusive. It will
 * fail, however, if firstPoint coincides with either of the poles because the
 * notion of azimuth from a pole in undetermined.
 * 
 * @author sballar
 */
public class GreatCircle
{

	/**
	 * @author sballar
	 *
	 */
	public class GreatCircleException extends Exception
	{

		private static final long serialVersionUID = -6860767931308018043L;

		/**
		 * 
		 */
		public GreatCircleException()
		{
		}

		/**
		 * @param arg0
		 */
		public GreatCircleException(String arg0)
		{
			super(arg0);
		}

		/**
		 * @param arg0
		 */
		public GreatCircleException(Throwable arg0)
		{
			super(arg0);
		}

		/**
		 * @param arg0
		 * @param arg1
		 */
		public GreatCircleException(String arg0, Throwable arg1)
		{
			super(arg0, arg1);
		}

	}

	/**
	 * Reference to the first unit vector on the great circle
	 */
	private double[] firstPoint;

	/**
	 * Reference to the last unit vector on the great circle
	 */
	private double[] lastPoint;
	
	/**
	 * The angular separation of the first and last point in radians.
	 * Measured in direction from first point to last point, which 
	 * may be > PI radians.
	 * Lazy evaluation is used for this variable.
	 */
	private double distance;

	/**
	 * The unit vector normal to the plane of this GreatCircle. Equals firstPoint
	 * cross lastPoint.
	 */
	private double[] normal;

	/**
	 * The vector triple product of (firstPoint cross lastPoint) cross
	 * firstPoint, normalized to unit length.  FirstPoint, moveDirection 
	 * and normal define a right-handed orthogonal coordinate system.
	 */
	private double[] moveDirection;

	/**
	 * Transform is a 3 x 3 matrix such that when a vector is multiplied by
	 * transform, the vector will be projected onto the plane of this
	 * GreatCircle. The z direction will point out of the plane of the great
	 * circle in the direction of the observer (lastPoint cross firstPoint;
	 * anti-parallel to normal). The y direction will correspond to the mean of
	 * firstPoint and lastPoint. The x direction will correspond to y cross z,
	 * forming a right handed coordinate system.
	 */
	private double[][] transform;
	
	/**
	 * Constructor
	 * 
	 * @param kmlFile input file in google earth kml format
	 * @throws GreatCircleException
	 * @throws Exception 
	 */
	public GreatCircle(File kmlFile) throws GreatCircleException, Exception
	{
		double lat1, lon1, lat2, lon2;
		lat1=lon1=lat2=lon2 = Double.NaN;
		
		Scanner input = new Scanner(kmlFile);
		String line = input.nextLine();
		while (input.hasNext())
		{
			if (line.contains("<coordinates>"))
			{
				Scanner sc = new Scanner(input.nextLine().replaceAll(",", " "));
				lon1 = sc.nextDouble();
				lat1 = sc.nextDouble();
				sc.next();
				lon2 = sc.nextDouble();
				lat2 = sc.nextDouble();
				sc.next();
				sc.close();
				break;
			}
			line = input.nextLine();
		}
		input.close();
		
		if (Double.isNaN(lon2))
			throw new Exception("Extracting coordinates from file failed\n"+kmlFile.getAbsolutePath());
		
		constructor(EarthShape.WGS84.getVectorDegrees(lat1, lon1), null, 
				EarthShape.WGS84.getVectorDegrees(lat2, lon2), true);
	}
	
	/**
	 * Constructor
	 * 
	 * @param firstPoint
	 *            unit vector.  GreatCircle stores a reference to this 
	 *            array.  No copy is made.
	 * @param distance
	 *            double distance to lastPoint, in radians
	 * @param direction
	 *            double direction to lastPoint, in radians
	 * @throws GreatCircleException
	 *             if firstPoint is on one of the poles.
	 */
	public GreatCircle(double[] firstPoint, double distance, double direction)
			throws GreatCircleException
	{
		this.firstPoint = firstPoint;

		moveDirection = new double[3];
		
		// first find a point that is 90 degrees away from firstPoint, in specified direction.
		if (!VectorUnit.move(firstPoint, PI/2, direction, moveDirection))
			throw new GreatCircleException("\nfirstPoint of GreatCircle is one of the poles\n");
		
		// find the unit vector normal to the plane of the great circle
		// firstPoint cross lastPoint. If firstPoint on left and lastPoint on
		// right, normal points away the observer.
		normal = VectorUnit.crossNormal(firstPoint, moveDirection);
		
		// now set last point to a point that is the correct distance from firstPoint.
		// Note that this will work, even when specified distance is >= 180 degrees.
		lastPoint = getPoint(distance);
		
		this.distance = distance;
	}
	
	/**
	 * Constructor that takes three unit vectors at the beginning, middle and end
	 * of the great circle path. Will not fail even when building GreatCircles
	 * that are 0, PI or 2PI radians long.
	 * 
	 * <p>The key is to successfully compute a valid unit vector that 
	 * is normal to the plane of the GreatCircle even when
	 * the firstPoint and lastPoint are 0 or PI radians apart.
	 * The following calculations are performed until normal has unit length.
	 * X is normalized cross product.  
	 * <ul>
	 * <li>normal = firstPoint X lastPoint.
	 * <li>intermediatePoint != null and normal = firstPoint X intermediatePoint
	 * <li>normal = firstPoint X [0., 0., 1.]
	 * <li>normal = firstPoint X [0., 1., 0.]
	 * <li>normal = firstPoint X [1., 0., 0.]
	 * </ul>
	 * Note that intermediatePoint is only relevant if firstPoint and lastPoint
	 * are 0 or PI radians apart.  Otherwise, it is ignored.
	 * 
	 * <p>GreatCircle stores references to firstPoint and lastPoint, no copies are made.
	 *
	 * @param firstPoint
	 *            unit vector of the origin of the great circle path.
	 * @param intermediatePoint
	 *            unit vector of an intermediate point on the great circle path.
	 *            If null, code will try the three cardinal directions (x, y, z).
	 * @param lastPoint
	 *            unit vector the end of the great circle path.
	 * @param shortestPath if false, normal will be negated and distance from first to last will be 
	 * >= PI.
	 */
	public GreatCircle(double[] firstPoint, double[] intermediatePoint, double[] lastPoint, boolean shortestPath)
	{ constructor(firstPoint, intermediatePoint, lastPoint, shortestPath); }

	/**
	 * Constructor that takes three unit vectors at the beginning, middle and end
	 * of the great circle path. This constructor is useful for building GreatCircles
	 * that are 0 or PI radians long.
	 * 
	 * <p>The following calculations are performed until normal has unit length.
	 * X is normalized cross product.  One of these is sure to succeed.
	 * <ul>
	 * <li>normal = firstPoint X lastPoint.
	 * <li>intermediatePoint != null and normal = firstPoint X intermediatePoint
	 * <li>normal = firstPoint X [0., 0., 1.]
	 * <li>normal = firstPoint X [0., 1., 0.]
	 * <li>normal = firstPoint X [1., 0., 0.]
	 * </ul>
	 * Note that intermediatePoint is only relevant if firstPoint and lastPoint
	 * are 0, PI or 2PI radians apart.  Otherwise, it is ignored.
	 * 
	 * <p>GreatCircle stores references to firstPoint and lastPoint, no copies are made.
	 *
	 * @param firstPoint
	 *            unit vector of the origin of the great circle path.
	 * @param intermediatePoint
	 *            unit vector of an intermediate point on the great circle path.
	 *            If null, code will try the three cardinal directions (x, y, z).
	 * @param lastPoint
	 *            unit vector the end of the great circle path.
	 */
	public GreatCircle(double[] firstPoint, double[] intermediatePoint, double[] lastPoint)
	{ 
		constructor(firstPoint, intermediatePoint, lastPoint, true);
	}

	/**
	 * Constructor that takes just the two GeoVectors at the beginning and end
	 * of the great circle path. GreatCircle stores references to these arrays;
	 * no copies are made.
	 * 
	 * <p>If firstPoint and lastPoint are 0 or PI radians apart, then the 
	 * GreatCirlce will pass through one of the following lat,lon pairs:
	 * (90N, 0E), (0N, 90E), or (0N, 0E).
	 * 
	 * @param firstPoint
	 *            unit vector the origin of the great circle path.
	 * @param lastPoint
	 *            unit vector the end of the great circle path.
	 * @param shortestPath if false, direction from first to last is reversed and
	 * the distance from first to last will be greater than 180 degrees.
	 */
	public GreatCircle(double[] firstPoint, double[] lastPoint, boolean shortestPath)
	{
		constructor(firstPoint, null, lastPoint, shortestPath);
	}

	/**
	 * Constructor that takes just the two unit vectors at the beginning and end
	 * of the great circle path. GreatCircle stores references to these arrays;
	 * no copies are made.
	 * 
	 * <p>If firstPoint and lastPoint are 0 or PI radians apart, then the 
	 * GreatCirlce will pass through one of the following lat,lon pairs:
	 * (90N, 0E), (0N, 90E), or (0N, 0E).
	 * 
	 * @param firstPoint
	 *            unit vector the origin of the great circle path.
	 * @param lastPoint
	 *            unit vector the end of the great circle path.
	 */
	public GreatCircle(double[] firstPoint, double[] lastPoint)
	{
		constructor(firstPoint, null, lastPoint, true);
	}
	
	/**
	 * Constructor that takes three unit vectors at the beginning, middle and end
	 * of the great circle path. Will not fail even when building GreatCircles
	 * that are 0, PI or 2PI radians long.
	 * 
	 * <p>The key is to successfully compute a valid unit vector that 
	 * is normal to the plane of the GreatCircle even when
	 * the firstPoint and lastPoint are 0 or PI radians apart.
	 * The following calculations are performed until normal has unit length.
	 * X is normalized cross product.  
	 * <ul>
	 * <li>normal = firstPoint X lastPoint.
	 * <li>intermediatePoint != null and normal = firstPoint X intermediatePoint
	 * <li>normal = firstPoint X [0., 0., 1.]
	 * <li>normal = firstPoint X [0., 1., 0.]
	 * <li>normal = firstPoint X [1., 0., 0.]
	 * </ul>
	 * Note that intermediatePoint is only relevant if firstPoint and lastPoint
	 * are 0 or PI radians apart.  Otherwise, it is ignored.
	 * 
	 * <p>GreatCircle stores references to firstPoint and lastPoint, no copies are made.
	 *
	 * @param firstPoint
	 *            unit vector of the origin of the great circle path.
	 * @param intermediatePoint
	 *            unit vector of an intermediate point on the great circle path.
	 *            If null, code will try the three cardinal directions (x, y, z).
	 * @param lastPoint
	 *            unit vector the end of the great circle path.
	 * @param shortestPath if false, normal will be negated and distance from first to last will be 
	 * >= PI.
	 */
	private void constructor(double[] firstPoint, double[] intermediatePoint, double[] lastPoint, boolean shortestPath)
	{
		this.firstPoint = firstPoint;
		this.lastPoint = lastPoint;
		
		// find the unit vector normal to the plane of the great circle
		// firstPoint cross lastPoint. If firstPoint on left and lastPoint on
		// right, normal points away the observer.
		normal = new double[3];
		distance = -1;
		
		if (VectorUnit.crossNormal(firstPoint, lastPoint, normal) == 0.)
		{
			// distance must be either 0 or PI.
			distance = VectorUnit.dot(firstPoint, lastPoint) > 0. ? 0. : PI;

			if (intermediatePoint == null || VectorUnit.crossNormal(firstPoint, intermediatePoint, normal) == 0.)
			{
				double[] middle = new double[] {0., 0., 1.}; 
				if (VectorUnit.crossNormal(firstPoint, middle, normal) == 0.)
				{
					middle[0] = 0.;
					middle[1] = 1.;
					middle[2] = 0.;
					if (VectorUnit.crossNormal(firstPoint, middle, normal) == 0.)
					{
						middle[0] = 1.;
						middle[1] = 0.;
						middle[2] = 0.;
						if (VectorUnit.crossNormal(firstPoint, middle, normal) == 0.)
						{
							String message = "\nUnable to determine normal to great circle path.\n";
							if (firstPoint[0]*firstPoint[0] + firstPoint[1]*firstPoint[1] +  firstPoint[2]*firstPoint[2] < 1e-6)
								message += "firstPoint is not a unit vector (length==0)!\n";
							else
								message += String.format("firstPoint=%s, intermediatePoint=%s, lastPoint=%s\n",
									Arrays.toString(firstPoint), 
									intermediatePoint == null ? "null" : Arrays.toString(intermediatePoint), 
											Arrays.toString(lastPoint));

							// throw an unchecked Exception.  Can only happen if firstPoint has zero length.
							throw new Error(message);
						}
					}
				}
			}
		}
		
		if (!shortestPath)
		{
			if (distance == 0.)
				distance = 2*PI;
			normal[0] = -normal[0];
			normal[1] = -normal[1];
			normal[2] = -normal[2];
		}
		
		moveDirection = VectorUnit.crossNormal(normal, firstPoint);
	}


	/**
	 * Two GreatCircles are equal if their firstPoints, lastPoints and normals are all ==.
	 */
	@Override
	public boolean equals(Object other)
	{
		return (other instanceof GreatCircle)
				&& firstPoint[0] == ((GreatCircle)other).firstPoint[0]
						&& firstPoint[1] == ((GreatCircle)other).firstPoint[1]
								&& firstPoint[2] == ((GreatCircle)other).firstPoint[2]
										&& lastPoint[0] == ((GreatCircle)other).lastPoint[0]
												&& lastPoint[1] == ((GreatCircle)other).lastPoint[1]
														&& lastPoint[2] == ((GreatCircle)other).lastPoint[2]
																&& normal[0] == ((GreatCircle)other).normal[0]
																		&& normal[1] == ((GreatCircle)other).normal[1]
																				&& normal[2] == ((GreatCircle)other).normal[2];
	}

	/**
	 * Retrieve the angular distance from firstPoint to lastPoint, in radians.
	 * 
	 * @return double
	 */
	public double getDistance()
	{
		if (distance < 0.)
		{
			distance = VectorUnit.angle(firstPoint, lastPoint);
			if (distance != 0. && distance != PI 
					&& VectorUnit.scalarTripleProduct(firstPoint, lastPoint, normal) < 0.)
				distance = 2*PI - distance;
		}
		return distance;
	}

	/**
	 * Retrieve the angular distance from firstPoint to lastPoint, in degrees.
	 * 
	 * @return double
	 */
	public double getDistanceDegrees()
	{
		return Math.toDegrees(getDistance());
	}

	/**
	 * Retrieve a reference to the first unit vector on this GreatCircle.
	 * 
	 * @return unit vector
	 */
	public double[] getFirst()
	{
		return firstPoint;
	}

	/**
	 * Retrieve a reference to the last unit vector on this GreatCircle.
	 * 
	 * @return unit vector
	 */
	public double[] getLast()
	{
		return lastPoint;
	}

	/**
	 * Retrieve a unit vector object located on the great circle path a specified
	 * distance from firstPoint.
	 * 
	 * @param dist
	 *            double the angular distance from firstPoint, in radians.
	 * @param location
	 *            unit vector the unit vector object that is to be populated with
	 *            the desired information. Radius is not modified.
	 * @throws GreatCircleException
	 */
	public void getPoint(double dist, double[] location)
	{
		VectorUnit.move(firstPoint, moveDirection, dist, location);
	}

	/**
	 * Retrieve a unit vector object located on the great circle path a specified
	 * distance from firstPoint.
	 * 
	 * @param dist
	 *            double the angular distance from firstPoint, in radians.
	 * @return unit vector
	 * @throws GreatCircleException
	 */
	public double[] getPoint(double dist) 
	{
		double[] location = new double[3];
		getPoint(dist, location);
		return location;
	}

	/**
	 * Retrieve a bunch of unit vectors equally spaced along the great circle
	 * between initial and final points that define the great circle.
	 *
	 * @param npoints the number of points desired.
	 * @param onCenters if true, the points are
	 * located at the centers of path increments of equal size.
	 * If onCenters is false, the first point is located at the starting point
	 * of the great circle, the last point is located at the final point of the
	 * great circle and the remaining points are equally spaced in between.
	 * @return a array of unit vectors populated
	 * with equally spaced unit vectors along the great circle.
	 */
	public ArrayList<double[]> getPoints(int npoints, boolean onCenters)
	{
		ArrayList<double[]> points = new ArrayList<double[]>(npoints);
		getPoints(points, npoints, onCenters);
		return points;
	}

	/**
	 * Retrieve a bunch of unit vectors equally spaced along the great circle
	 * between initial and final points that define the great circle.
	 *
	 * @param points a vector of unit vectors that will be cleared and populated
	 * with equally spaced unit vectors along the great circle.
	 * @param npoints the number of points desired.
	 * @param onCenters if true, the points are
	 * located at the centers of path increments of equal size.
	 * If onCenters is false, the first point is located at the starting point
	 * of the great circle, the last point is located at the final point of the
	 * great circle and the remaining points are equally spaced in between.
	 */
	public void getPoints(ArrayList<double[]> points, int npoints, boolean onCenters)
	{
		points.clear();
		double dx = onCenters ? getDistance()/npoints : getDistance()/(npoints-1);
		double dx0 = onCenters ? dx/2 : 0;
		for (int i=0; i<npoints; ++i) points.add(getPoint(dx0 + i*dx));
	}

	/**
	 * Retrieve a reference to the unit vector that is normal to the plane of this great circle
	 * (firstPoint cross lastPoint normalized to unit length). If firstPoint on
	 * left and lastPoint on right, normal points away from the observer.
	 * 
	 * @return double[]
	 */
	public double[] getNormal()
	{
		return normal;
	}

	/**
	 * Retrieve the unit vector that lies at the intersection of this GreatCircle
	 * and another GreatCircle. There are, in general, two such intersections
	 * that are 180 degrees apart. This method returns the first one that is
	 * encountered when traveling from firstPoint in the direction of lastPoint.
	 * The other intersection can be retrieved by negating every element of 
	 * the unit vector that is returned by this method.
	 * 
	 * <p>If inRange is true then the point of intersection must reside between
	 * the firstPoint and the lastPoint of both this and other GreatCircles.
	 * 
	 * @param other
	 *            GreatCircle
	 * @param inRange if true then the point of intersection must reside between
	 *        the firstPoint and the lastPoint of both this and other GreatCircles.
	 * @return unit vector. returns null if the this GreatCircle and other
	 *         GreatCircle are coincident, i.e., their normals are equal.
	 *         Also returns null if inRange is true and the point of intersection
	 *         does not reside between firstPoint and lastPoint of both 
	 *         this and other GreatCircle.
	 */
	public double[] getIntersection(GreatCircle other, boolean inRange)
	{
		double[] intersection = new double[3];
		if (VectorUnit.crossNormal(normal, other.normal, intersection) == 0.)
			return null;
		
		if (VectorUnit.scalarTripleProduct(firstPoint, intersection, normal) < 0.)
		{
			intersection[0] = -intersection[0];
			intersection[1] = -intersection[1];
			intersection[2] = -intersection[2];
		}
		
		if (inRange && (getDistance(intersection) >= getDistance() 
				|| other.getDistance(intersection) > other.getDistance()))
			return null;
		
		return intersection;
	}
	
	/**
	 * Determine if the specified unit vector lies on the great circle path. For this
	 * to be true dot(v, normal) must be very small and distance(v) must be less than
	 * distance().
	 * @param v
	 * @return true if the specified unit vector lies on the great circle path.
	 */
	public boolean onCircle(double[] v)
	{
		return Math.abs(VectorUnit.dot(v, normal)) < 1e-15 && getDistance(v) < getDistance();
	}

	/**
	 * Retrieve the distance in radians measured from firstPoint to specified
	 * unit vector, measured in direction from firstPoint to lastPoint.
	 * Range is zero to 2*PI
	 * 
	 * @param position an earth-centered unit vector
	 * @return double
	 * @throws GreatCircleException
	 */
	public double getDistance(double[] position) 
	{
		// find the shortest distance from firstPoint to unit vector
		double d = VectorUnit.angle(firstPoint, position);

		if (VectorUnit.scalarTripleProduct(firstPoint, position, normal) < 0.)
			d = 2 * Math.PI - d;

		return d;
	}

	/**
	 * Retrieve the distance in degrees measured from firstPoint to specified
	 * unit vector, measured in direction from firstPoint to lastPoint.
	 * Range is zero to 360.
	 * 
	 * @param position an earth-centered unit vector
	 * @return  the distance in degrees measured from firstPoint to specified
	 * unit vector, measured in direction from firstPoint to lastPoint
	 * @throws GreatCircleException
	 */
	public double getDistanceDegrees(double[] position)
	{ return Math.toDegrees(getDistance(position)); }

	/**
	 * 
	 * 
	 * @param position an earth-centered unit vector
	 * @return double
	 * @throws GreatCircleException
	 */
	public double getDistanceKm(EarthShape earthShape) 
	{ return getDistanceKm(earthShape, getDistance()); }

	/**
	 * Convert distance in radians to distance in km.
	 * 
	 * @param position an earth-centered unit vector
	 * @return distance converted to km
	 * @throws GreatCircleException
	 */
	public double getDistanceKm(EarthShape earthShape, double distance) 
	{
		if (earthShape.constantRadius)
			return earthShape.equatorialRadius * distance;
		int n = (int)Math.ceil(distance/Math.toRadians(1.));
		double dkm=0, dx = distance/n;
		for (int i=0; i<n; ++i)
			dkm += dx * earthShape.getEarthRadius(getPoint(dx*(i+0.5)));
		return dkm;
	}

	/**
	 * Retrieve a reference to the transform matrix owned by this GreatCircle.
	 * Transform is a 3 x 3 matrix such that when a vector is multiplied by
	 * transform, the vector will be projected onto the plane of this
	 * GreatCircle. The z direction will point out of the plane of the great
	 * circle in the direction of the observer (lastPoint cross firstPoint;
	 * parallel to normal). The y direction will correspond to the mean of
	 * firstPoint and lastPoint. The x direction will correspond to y cross z,
	 * forming a right handed coordinate system.
	 * 
	 * @return double[][]
	 * @throws GreatCircleException
	 */
	public double[][] getTransform()
	{
		if (transform == null)
		{
			/**
			 * Transform is a 3 x 3 matrix such that when a vector is multiplied
			 * by transform, the vector will be projected onto the plane of this
			 * GreatCircle. The z direction will point out of the plane of the
			 * great circle in the direction of the observer (lastPoint cross
			 * firstPoint; parallel to normal). The y direction will correspond
			 * to the mean of firstPoint and lastPoint. The x direction will
			 * correspond to y cross z, forming a right handed coordinate
			 * system.
			 */
			transform = new double[3][3];
			
			VectorUnit.rotate(firstPoint, normal, -getDistance()/2, transform[1]);
			
			transform[2][0] = -normal[0];
			transform[2][1] = -normal[1];
			transform[2][2] = -normal[2];
			
			VectorUnit.crossNormal(transform[1], transform[2], transform[0]);
		}
		return transform;
	}

	/**
	 *
	 * @param x double[]
	 * @return double[]
	 * @throws GreatCircleException 
	 */
	public double[] untransform(double[] x) throws GreatCircleException

	{
		double[] v = new double[3];
		untransform(x, v);
		return v;
	}

	/**
	 * Unproject a previously projected point back into the global coordinate
	 * system.
	 *
	 * @param point double[] 3-element vector that is a projection onto the
	 *   plane of this GreatCircle
	 * @param x double[] a 3-element vector in global coordinate system.
	 * @throws GreatCircleException
	 */
	public void untransform(double[] point, double[] x)
	throws GreatCircleException
	{
		// make sure that transform has been calculated
		getTransform();
		for (int i = 0; i < 3; i++)
		{
			x[i] = 0;
			for (int j = 0; j < point.length; j++)
				x[i] += point[j] * transform[j][i]; ;
		}
	}

	/**
	 * Project vector x onto the plane of this GreatCircle. Returns a 3 element
	 * vector g such that g[2] is the component of x that points out of the
	 * plane of the GreatCircle (toward the observer). 
	 * g[1] is the component of x parallel to the mean of firstPoint and lastPoint, 
	 * and g[0] is the remaining part of x.  
	 * For an observer viewing the great circle from the normal direction (firstPoint
	 * on the left and lastPoint on the right), g[2] will be the component of 
	 * x that points toward the observer, g[1] will be 'up' and g[0] will be 
	 * 'to the right'.  For plotting values on a great circle 'slice' through a 
	 * model, g[0] will be the x-component, g[1] will be the y-component and
	 * g[2] should be ignored.
	 * 
	 * 
	 * @param x
	 *            double[] the 3 element array containing the vector to be
	 *            projected.
	 * @return double[] the projection of x onto plane of this GreatCircle
	 * @throws GreatCircleException
	 */
	public double[] transform(double[] x) throws GreatCircleException
	{
		double[] v = new double[3];
		transform(x, v);
		return v;
	}

	/**
	 * Project vector x onto the plane of this GreatCircle. Returns a 3 element
	 * vector g such that g[2] is the component of x that points out of the
	 * plane of the GreatCircle (toward the observer). 
	 * g[1] is the component of x parallel to the mean of firstPoint and lastPoint, 
	 * and g[0] is the remaining part of x.  
	 * For an observer viewing the great circle from the normal direction (firstPoint
	 * on the left and lastPoint on the right), g[2] will be the component of 
	 * x that points toward the observer, g[1] will be 'up' and g[0] will be 
	 * 'to the right'.  For plotting values on a great circle 'slice' through a 
	 * model, g[0] will be the x-component, g[1] will be the y-component and
	 * g[2] should be ignored.
	 * 
	 * @param x
	 *            double[] the 3 element array containing the vector to be
	 *            projected.
	 * @param v
	 *            double[] the projection of x onto plane of this GreatCircle
	 * @throws GreatCircleException
	 */
	public void transform(double[] x, double[] v)
	{
		// make sure that transform has been calculated
		getTransform();
		v[0] = x[0] * transform[0][0] + x[1] * transform[0][1] + x[2]
				* transform[0][2];
		v[1] = x[0] * transform[1][0] + x[1] * transform[1][1] + x[2]
				* transform[1][2];
		if (v.length > 2)
			v[2] = x[0] * transform[2][0] + x[1] * transform[2][1] + x[2]
					* transform[2][2];
	}

}
