package gms.shared.utilities.geotess.util.mapprojection;

import static java.lang.Math.abs;
import static java.lang.Math.signum;

import gms.shared.utilities.geotess.util.exceptions.GMPException;
import gms.shared.utilities.geotess.util.numerical.polygon.GreatCircle;
import gms.shared.utilities.geotess.util.numerical.polygon.GreatCircle.GreatCircleException;
import gms.shared.utilities.geotess.util.numerical.vector.VectorGeo;
import gms.shared.utilities.geotess.util.numerical.vector.VectorUnit;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Implements the Robinson map projection for use in GeoTessModelUtils
 * and GeoTessExplorer.
 * @author sballar
 *
 */
public class RobinsonProjection
{

	final static private double[][] data = new double[][] {
		{-90, 0.5322, -0.50720},
		{-85, 0.5722, -0.49508},
		{-80, 0.6213, -0.47646},
		{-75, 0.6732, -0.45323},
		{-70, 0.7186, -0.42782},
		{-65, 0.7597, -0.40084},
		{-60, 0.7986, -0.37259},
		{-55, 0.8350, -0.34332},
		{-50, 0.8679, -0.31325},
		{-45, 0.8962, -0.28256},
		{-40, 0.9216, -0.25147},
		{-35, 0.9427, -0.22012},
		{-30, 0.9600, -0.18868},
		{-25, 0.9730, -0.15723},
		{-20, 0.9822, -0.12579},
		{-15, 0.9900, -0.09434},
		{-10, 0.9954, -0.06289},
		{-5, 0.9986, -0.03145},
		{0, 1.0000, 0.00000},
		{5, 0.9986, 0.03145},
		{10, 0.9954, 0.06289},
		{15, 0.9900, 0.09434},
		{20, 0.9822, 0.12579},
		{25, 0.9730, 0.15723},
		{30, 0.9600, 0.18868},
		{35, 0.9427, 0.22012},
		{40, 0.9216, 0.25147},
		{45, 0.8962, 0.28256},
		{50, 0.8679, 0.31325},
		{55, 0.8350, 0.34332},
		{60, 0.7986, 0.37259},
		{65, 0.7597, 0.40084},
		{70, 0.7186, 0.42782},
		{75, 0.6732, 0.45323},
		{80, 0.6213, 0.47646},
		{85, 0.5722, 0.49508},
		{90, 0.5322, 0.50720}
	};

	/**
	 *  longitude of center of map, in degrees
	 */
	final private double centerLon;

	// a GreatCircle from north pole to south pole that passes though the 
	// anti-pode of centerLon. This GreatCircle will describe the two edges of the map.
	private GreatCircle greatCircle;

	/**
	 * 
	 * @param centerLonDegrees longitude of the center of the map, in degrees
	 * @throws Exception 
	 * @throws GreatCircleException 
	 */
	public RobinsonProjection(double centerLonDegrees) throws Exception
	{
		this.centerLon = centerLonDegrees;
	}

	/**
	 * Project a lon, lat pair into x,y coordinates.
	 * @param lat
	 * @param lon
	 * @param inDegrees if true, lat and lon are assumed to be in degrees,
	 * otherwise radians.
	 * @param xy
	 */
	public boolean project(double lat, double lon, boolean inDegrees, double[] xy)
	{
		if (!inDegrees)
		{
			lat = Math.toDegrees(lat);
			lon = Math.toDegrees(lon);
		}
		while (lon < centerLon-180.)
			lon += 360.;
		while (lon > centerLon+180.)
			lon -= 360.;

		double f = (lat+90)/5;
		int i= Math.min(data.length-2,(int)Math.floor(f));
		f -= i;
		xy[0] = data[i][1] + f*(data[i+1][1]-data[i][1]);
		xy[1] = data[i][2] + f*(data[i+1][2]-data[i][2]);
		xy[0] *= (lon-centerLon)/180.;
		return true;
	}

	/**
	 * Project a lon, lat pair into x,y coordinates.
	 * @param lat
	 * @param lon
	 * @param inDegrees if true, lat and lon are assumed to be in degrees,
	 * otherwise radians.
	 * @return 2-element array containing x,y coordinates.
	 */
	public double[] project(double lat, double lon, boolean inDegrees)
	{
		double[] xy = new double[2];
		project(lat, lon, inDegrees, xy);
		return xy;
	}

	/**
	 * Project a unit vector into x,y coordinates.
	 * @param unit_vector
	 * @return 2-element array containing x,y coordinates.
	 */
	public double[] project(double[] unit_vector)
	{
		return project(VectorGeo.getLatDegrees(unit_vector), VectorGeo.getLonDegrees(unit_vector), true);
	}

	/**
	 * Project a unit vector into x,y coordinates.
	 * @param unit_vector
	 * @param xy projected x,y coordinates
	 */
	public boolean project(double[] unit_vector, double[] xy)
	{
		return project(VectorGeo.getLatDegrees(unit_vector), VectorGeo.getLonDegrees(unit_vector), true, xy);
	}
	
	/**
	 * Given input array of lines, each of which consists of a sequence of points that define a 
	 * path along the surface of the earth, project each point. Original paths may be broken into 
	 * several paths in order to ensure that no path crosses the  edge of the map.  
	 * Add all the points to a single big array of points, and put the number of points on each
	 * path into a separate array.
	 * @param lines array of paths, each composed of an array of points.
	 * @param points a single collection of points.
	 * @param nPoints an array with the number of points in each path.
	 * @throws Exception
	 */
	public void project(ArrayList<List<double[]>> lines, 
			ArrayList<double[]> paths, ArrayList<Integer> nPoints) throws Exception
	{
		for (int i = 0; i < lines.size(); ++i)
		{
				for (ArrayList<double[]> pnts : project(lines.get(i)))
				{
					nPoints.add(pnts.size());
					paths.addAll(pnts);
				}
		}
	}

	/**
	 * Project a 1D array of unit vectors into a Robinson projection, ensuring that
	 * lines do not cross the edge of the map.
	 * This method will return a 2D array of projected points, where
	 * each array of points is guaranteed not to wrap around the edge of the map.
	 * @param geovectors a 1D array of double[]s
	 * @return 2D array of double[]s
	 * @throws GreatCircleException 
	 * @throws GMPException 
	 */
	public ArrayList<ArrayList<double[]>> project(List<double[]> geovectors) throws Exception
	{

		ArrayList<ArrayList<double[]>> lines = new ArrayList<ArrayList<double[]>>();

		ArrayList<double[]> limbo = new ArrayList<double[]>();

		if (geovectors.size() > 0)
		{
			double[] xy, xyNext, xyPrevious, next=null, previous=null, midPoint = new double[3];
			double dotNext, dotPrevious=0;
			boolean onCircle = true;
			getGreatCircle();

			ArrayList<double[]> line = new ArrayList<double[]>();
			lines.add(line);

			Iterator<double[]> it = geovectors.iterator();
			while (it.hasNext() && onCircle)
			{
				previous = it.next();
				onCircle = greatCircle.onCircle(previous);
				if (onCircle)
					limbo.add(previous);
			}

			dotPrevious = getDot(previous);
			xyPrevious = project(previous);

			for (int i=0; i<limbo.size(); ++i)
			{
				xy = project(limbo.get(i));
				if (dotPrevious * xy[0] < 0.)
					xy[0] = -xy[0];
				line.add(xy);
			}
			line.add(xyPrevious);
			limbo.clear();



			while (it.hasNext())
			{
				// gather points in limbo until a point is found that is 
				// not on the edge of the map or no more points
				onCircle = true;
				while (it.hasNext() && onCircle)
				{
					next = it.next();
					onCircle = greatCircle.onCircle(next);
					if (it.hasNext() && onCircle) limbo.add(next);
				}

				dotNext = getDot(next);
				xyNext = project(next);

				// check to see if next and previous are on opposite sides of the 
				// great circle plane that contains edge of map.  
				if (dotPrevious * dotNext < 0.)
				{
					// next and previous are on opposite sides of the great circle 
					// plane that contains edge of map. Find great circle containing 
					// previous point and next point, and then find intersection of that 
					// great circle and the great circle that represents the edge of map.
					midPoint = limbo.size() > 0 ? limbo.get(0)
							: (new GreatCircle(previous, next)).getIntersection(greatCircle, true);

					if (midPoint != null)
					{
						// midPoint resides at the intersection of 2 great circles: one from
						// previous point to next point, and the other from north to south pole
						// through the antipode of centerLon of the map.  It is exactly on the
						// edge of the map.

						// project midpoint into map coordinates
						xy = project(midPoint);

						// ensure that if previous point was near the left edge of the map
						// then xy is on left edge. If previous point was on the right edge
						// then ensure that xy is also on the right edge.
						if (xy[0] * xyPrevious[0] < 0)
							xy[0] = -xy[0];

						// If xy is not extremely close to xyPrevious then add a copy of xy 
						// to the current line.  This will be the last point on the current line.
						if (abs(xy[0]-xyPrevious[0]) > 1e-9 || abs(xy[1]-xyPrevious[1]) > 1e-9)
							line.add(xy.clone());

						// instantiate a new line and add it to the list of lines.
						line = new ArrayList<double[]>();
						lines.add(line);

						if (limbo.size() > 1)
							xy = project(limbo.get(limbo.size()-1));

						if (xy[0] * xyNext[0] < 0)
							xy[0] = -xy[0];

						// add xy to the new line.  It will be the first point.
						line.add(xy);
						xyPrevious = xy;
					}
				}
				else
					for (int i=0; i<limbo.size(); ++i)
					{
						xy = project(limbo.get(i));
						if (xy[0] * xyPrevious[0] < 0)
							xy[0] = -xy[0];
						line.add(xy);
					}

				if (onCircle && xyNext[0] * xyPrevious[0] < 0)
					xyNext[0] = -xyNext[0];

				// if xyNext and xyPrevious are significantly separated from each other
				// add xyNext to the current line.
				//if (abs(xyNext[0]-xyPrevious[0]) > 1e-9 || abs(xyNext[1]-xyPrevious[1]) > 1e-9)
					line.add(xyNext);

				dotPrevious = dotNext;
				previous = next;
				xyPrevious = xyNext;
				limbo.clear();
			}

			// search for lines that have only a single point
			boolean found = false;
			for (int i=0; i<lines.size(); ++i)
				if (lines.get(i).size() < 2)
				{
					found = true;
					break;
				}
			if (found)
			{
				// remove lines that have only a single point
				ArrayList<ArrayList<double[]>> copy = new ArrayList<ArrayList<double[]>>(lines.size());
				for (int i=0; i<lines.size(); ++i)
					if (lines.get(i).size() >= 2)
						copy.add(lines.get(i));
				lines = copy;
			}
		}
		return lines;
	}

	private double getDot(double[] x)
	{
		double dot = VectorUnit.dot(x, greatCircle.getNormal());
		if (abs(dot) < 1e-15) dot = 0.;
		return dot;
	}
	
	public GreatCircle getGreatCircle()
	{
		// GreatCircle from south pole to north pole that passes though the 
		// anti-pode of centerLon. This GreatCircle will describe the two edges of the map.
		if (greatCircle == null)
			greatCircle = new GreatCircle(new double[]{0,0,-1}, 
					VectorGeo.getVectorDegrees(0., centerLon+180.), 
					new double[]{0,0,1});
		return greatCircle;
	}

	/**
	 * Get a bunch of projected points that define the boundary of the map.
	 * @return an array of 2-element arrays containing projected points that define the boundary of the map.
	 */
	public static double[][] getEdge()
	{
		double[][] edge = new double[2*data.length+1][2];
		for (int i=0; i < data.length; ++i)
		{
			edge[i][0] = -data[i][1];
			edge[i][1] =  data[i][2];

			edge[2*data.length-1-i][0] = data[i][1];
			edge[2*data.length-1-i][1] = data[i][2];
		}

		edge[2*data.length][0] = -data[0][1];
		edge[2*data.length][1] =  data[0][2];

		return edge;
	}

	/**
	 * @return the centerLon in degrees
	 */
	public double getCenterLon()
	{
		return centerLon;
	}

	/**
	 * If a triangle spans the edge of the map, divide it into two cells, one on each side
	 * of the edge.  A cell is an array of unit vectors. The input cell is a triangle composed of 
	 * 3 unit vectors.  Most often the triangle does not span the edge, in which case it is simply added 
	 * to the  output array of cells.  If the triangle does span the edge, it is divided into two cells.
	 * If any one of the vertices of the triangle is on the edge, then the original triangle is divided 
	 * into two new triangles.  If none of the vertices lies on the edge, then the triangle is divided 
	 * into a triangle and a quadrilateral.
	 * 
	 * <p>The vertices of the input triangle consisted of just 3 elements, the usual 3 component unit 
	 * vector.  On output, all the vertices in the cells array have 5 components with the last two being
	 * the x and y coordinates of the point in map coordinates (Robinson projection). Note that in the 
	 * output, vertices of the original GeoTessGrid that lie on the edge of the map will appear in the
	 * output twice: once on one edge of the map and the other on the other side of the map.  
	 * Also, the output will contain new vertices that were not vertices of the original GeoTessGrid.
	 * These vertices lie on the edge of the map and were introduced during division of a single triangle
	 * into 2 cells.
	 * 
	 * @param vertex a 3x3 array containing the 3 unit vectors that define the corners of the triangle
	 * @param cells (output) an array of cells into which the new cells constructed by this method
	 * are added.  The vertices of the cells are 5 element arrays: the first 3 elements are the 3
	 * components of the unit vector as usual.  Elements 3 and 4 are the x and y coordinates of the point
	 * in projected Robinson coordinates.
	 * @throws GreatCircleException
	 */
	public void projectTriangle(double[][] vertex, ArrayList<ArrayList<double[]>> cells) throws Exception 
	{
		ArrayList<double[]> cell;
		
		getGreatCircle();
		
		double[] pole = new double[] {0,0,1};
		// check for north and south poles
		for (int i=0; i<3; ++i)
		{
			if (VectorUnit.isPole(vertex[i]))
			{
				// vertex[v] is either north or south pole.
				double[] v = vertex[i];
				double[] u = vertex[(i+2)%3];
				double[] w = vertex[(i+1)%3];
				
				double xpole = 0.5322;
				double ypole = 0.5072 * v[2];
				
				double[] p = null;
				if (greatCircle.onCircle(u))
					// u is on the edge of the map
					p = u; 
				else if (greatCircle.onCircle(w))
					// w is on the edge of the map
					p = w; 
				else
				{
					// find the intersection of the greatcircle from w to u with the edge of the map
					p = new GreatCircle(w, u).getIntersection(greatCircle, true);
					// if intersection found and it is a pole, then just exit.  
					if (p != null && VectorUnit.isPole(p)) return;
				}
				
				double[][] pts = new double[4][2];

				if (p == null)
				{
					// the great circle from w to u does not intersect the edge of the map.  
					// Make a single quadrilateral.
					project(u, pts[0]);

					pts[1][0] = pts[0][0] * xpole/abs(project(VectorGeo.getLatDegrees(u), centerLon+180, true)[0]);
					pts[1][1] = ypole;

					project(w, pts[3]);
					
					pts[2][0] = pts[3][0] * xpole/abs(project(VectorGeo.getLatDegrees(w), centerLon+180, true)[0]);
					pts[2][1] = ypole;

					cell = new ArrayList<double[]> (4);						
					cell.add(combine(u, pts[0]));
					cell.add(combine(v, pts[1]));
					cell.add(combine(v, pts[2]));
					cell.add(combine(w, pts[3]));
					cells.add(cell);

				}
				else
				{
					project(u, pts[0]);
					pts[1][0] = pts[0][0] * xpole/abs(project(VectorGeo.getLatDegrees(u), centerLon+180, true)[0]);
					pts[1][1] = ypole;
					pts[2][0] = signum(pts[0][0]) * xpole;
					pts[2][1] = ypole;
					project(p, pts[3]);
					pts[3][0] = signum(pts[0][0]) * abs(pts[3][0]);

					cell = new ArrayList<double[]> (4);						
					cell.add(combine(u, pts[0]));
					cell.add(combine(v, pts[1]));
					cell.add(combine(v, pts[2]));
					cell.add(combine(p, pts[3]));
					cells.add(cell);

					pts = new double[4][2];
					project(w, pts[0]);
					project(p, pts[1]);
					pts[1][0] = signum(pts[0][0]) * abs(pts[1][0]);
					pts[2][0] = signum(pts[0][0]) * xpole;
					pts[2][1] = ypole;
					pts[3][0] = pts[0][0] * xpole/abs(project(VectorGeo.getLatDegrees(w), centerLon+180, true)[0]);
					pts[3][1] = ypole;

					cell = new ArrayList<double[]> (4);						
					cell.add(combine(w, pts[0]));
					cell.add(combine(p,         pts[1]));
					cell.add(combine(v, pts[2]));
					cell.add(combine(v, pts[3]));
					cells.add(cell);
				}

				return;
			}
		} // end of section dealing with north and south pole

		if (VectorUnit.scalarTripleProduct(vertex[1], vertex[0], pole) > -1e-15 
				&& VectorUnit.scalarTripleProduct(vertex[2], vertex[1], pole) > -1e-15  
				&& VectorUnit.scalarTripleProduct(vertex[0], vertex[2], pole) > -1e-15 )
		{
			// The north pole is located in the interior of the triangle.
			// Break the triangle up into 3 subtriangles
			// and recursively call this method with each of the subtriangles.
			projectTriangle(new double[][] {pole, vertex[0], vertex[1]}, cells);
			projectTriangle(new double[][] {pole, vertex[1], vertex[2]}, cells);
			projectTriangle(new double[][] {pole, vertex[2], vertex[0]}, cells);
			return;
		}
		
		pole[2] = -1;
		if (VectorUnit.scalarTripleProduct(vertex[1], vertex[0], pole) > -1e-15 
				&& VectorUnit.scalarTripleProduct(vertex[2], vertex[1], pole) > -1e-15  
				&& VectorUnit.scalarTripleProduct(vertex[0], vertex[2], pole) > -1e-15 )
		{
			// The south pole is located in the interior of the triangle.
			// Break the triangle up into 3 subtriangles
			// and recursively call this method with each of the subtriangles.
			projectTriangle(new double[][] {pole, vertex[0], vertex[1]}, cells);
			projectTriangle(new double[][] {pole, vertex[1], vertex[2]}, cells);
			projectTriangle(new double[][] {pole, vertex[2], vertex[0]}, cells);
			return;
		}
		
		// build a 4-element line that encircles the triangle.  The last 
		// element is the same as the first.
		ArrayList<double[]> line = new ArrayList<double[]>(4);
		for (double[] v : vertex) line.add(v);
		line.add(vertex[0]);
		
		// project the 4 points that define the line into Robinson coordinates.
		// The returned array will have 1, 2 or 3 lines of vertices, depending
		// on whether or not the line crossed the edge of the map and whether or 
		// not any elements of the line were located on the edge of the map.
		ArrayList<ArrayList<double[]>> points = project(line);
		
		// build a code out of the results.  Number of digit equals number of 
		// sublines into which lines was divided (points.size()).  Each digits
		// is equal to points[digit].size().
		int code = 0;
		for (int i=0; i<points.size(); ++i)
			code = code*10 + points.get(i).size();
		
		if (code == 4)
		{
			// triangle did not span the edge.  Project the original 3 points
			// and copy the results to the cells array.
			cell = new ArrayList<double[]> (3);
			cell.add(combine(vertex[0], points.get(0).get(0)));
			cell.add(combine(vertex[1], points.get(0).get(1)));
			cell.add(combine(vertex[2], points.get(0).get(2)));
			cells.add(cell);
			return;
		}

		if (code == 33)
		{
			// triangle vertex 0 (i.e., triangle[0]), lies on the edge of the map.  Divide the 
			// original triangle into two new ones using a new vertex located on the triangle edge
			// between triangle[1] and triangle[2].
			double[] p = new GreatCircle(vertex[1], vertex[2]).getIntersection(greatCircle, true);

			cell = new ArrayList<double[]> (3);
			cell.add(combine(vertex[0], points.get(0).get(0)));
			cell.add(combine(vertex[1], points.get(0).get(1)));
			cell.add(combine(p, points.get(0).get(2)));
			cells.add(cell);

			cell = new ArrayList<double[]> (3);
			cell.add(combine(p, points.get(1).get(0)));
			cell.add(combine(vertex[2], points.get(1).get(1)));
			cell.add(combine(vertex[0], points.get(1).get(2)));
			cells.add(cell);
			return;
		}


		if (points.size() == 3)
		{
			// find new vertices on the 3 edges of the triangle.  Some of these will be null because
			// the triangle edge does not intersect the map edge.
			double[] p0 = new GreatCircle(vertex[0], vertex[1]).getIntersection(greatCircle, true);
			double[] p1 = new GreatCircle(vertex[1], vertex[2]).getIntersection(greatCircle, true);
			double[] p2 = new GreatCircle(vertex[2], vertex[0]).getIntersection(greatCircle, true);

			if (code == 232)
			{
				if (greatCircle.onCircle(vertex[2]))
				{
					// vertex 2 lies on the edge of the map and there is a new vertex
					// on the triangle edge between vertices 0 and 1.
					cell = new ArrayList<double[]> (3);
					cell.add(combine(vertex[0], points.get(0).get(0)));
					cell.add(combine(p0, points.get(0).get(1)));
					cell.add(combine(vertex[2], points.get(2).get(0)));
					cells.add(cell);

					cell = new ArrayList<double[]> (3);
					cell.add(combine(p0, points.get(1).get(0)));
					cell.add(combine(vertex[1], points.get(1).get(1)));
					cell.add(combine(vertex[2], points.get(1).get(2)));
					cells.add(cell);
					return;
				}

				if (greatCircle.onCircle(vertex[1]))
				{
					// vertex 1 lies on the edge of the map and there is a new vertex
					// on the triangle edge between vertices 0 and 2.
					cell = new ArrayList<double[]> (3);
					cell.add(combine(vertex[0], points.get(0).get(0)));
					cell.add(combine(vertex[1], points.get(0).get(1)));
					cell.add(combine(p2, points.get(2).get(0)));
					cells.add(cell);

					cell = new ArrayList<double[]> (3);
					cell.add(combine(vertex[1], points.get(1).get(0)));
					cell.add(combine(vertex[2], points.get(1).get(1)));
					cell.add(combine(p2, points.get(1).get(2)));
					cells.add(cell);
					return;
				}
			}
			
			if (code == 332)
			{
				// vertices 0 and 1 are on one side of the map edge and vertex 2 is on the other.
				cell = new ArrayList<double[]> (4);
				cell.add(combine(vertex[0], points.get(0).get(0)));
				cell.add(combine(vertex[1], points.get(0).get(1)));
				cell.add(combine(p1, points.get(0).get(2)));
				cell.add(combine(p2, points.get(2).get(0)));
				cells.add(cell);

				cell = new ArrayList<double[]> (3);
				cell.add(combine(p1, points.get(1).get(0)));
				cell.add(combine(vertex[2], points.get(1).get(1)));
				cell.add(combine(p2, points.get(1).get(2)));
				cells.add(cell);
				return;
			}

			if (code == 233)
			{
				// vertices 0 and 2 are on one side of the map edge and vertex 1 is on the other.
				cell = new ArrayList<double[]> (4);
				cell.add(combine(vertex[0], points.get(0).get(0)));
				cell.add(combine(p0, points.get(0).get(1)));
				cell.add(combine(p1, points.get(2).get(0)));
				cell.add(combine(vertex[2], points.get(2).get(1)));
				cells.add(cell);

				cell = new ArrayList<double[]> (3);
				cell.add(combine(p0, points.get(1).get(0)));
				cell.add(combine(vertex[1], points.get(1).get(1)));
				cell.add(combine(p1, points.get(1).get(2)));
				cells.add(cell);
				return;
			}

			if (code == 242)
			{
				// vertices 1 and 2 are on one side of the map edge and vertex 0 is on the other.
				cell = new ArrayList<double[]> (3);
				cell.add(combine(vertex[0], points.get(0).get(0)));
				cell.add(combine(p0, points.get(0).get(1)));
				cell.add(combine(p2, points.get(2).get(0)));
				cells.add(cell);

				cell = new ArrayList<double[]> (4);
				cell.add(combine(p0, points.get(1).get(0)));
				cell.add(combine(vertex[1], points.get(1).get(1)));
				cell.add(combine(vertex[2], points.get(1).get(2)));
				cell.add(combine(p2, points.get(1).get(3)));
				cells.add(cell);
				return;
			}

		}
		
		// if we end up here, it means that a triangle did not get processed when it should have
		// The following statements may help to figure out what happened.
		
		printDebug("\n\nError in RobinsonProjection.projectTriangle()  a triangle has been missed.", vertex, points);
		
		System.out.printf("%1.16f %1.16f %1.16f%n", 
				VectorUnit.scalarTripleProduct(vertex[1], vertex[0], pole),
				VectorUnit.scalarTripleProduct(vertex[2], vertex[1], pole) ,
				VectorUnit.scalarTripleProduct(vertex[0], vertex[2], pole));
		

	}

	/**
	 * return a new 5 element array with all the elements of the input arrays v and xy.
	 * @param v 3 element array
	 * @param xy 2 element array
	 * @return 5 element array composed of the elements of v followed by the elements of xy.
	 */
	private double[] combine(double[] v, double[] xy)
	{ return new double[] {v[0], v[1], v[2], xy[0], xy[1]};  }
	
	private void printDebug(String message, double[][] vertex, ArrayList<ArrayList<double[]>> points)
	{
		System.out.println("debug in projectTriangle "+message);
		for (int i=0; i<3; ++i)
			System.out.printf("triangle[%d] = VectorGeo.getVectorDegrees(%1.6f, %1.6f);%n", 
					i, VectorGeo.getLatDegrees(vertex[i]), VectorGeo.getLonDegrees(vertex[i]));

		System.out.println();

		for (int i=0; i<points.size(); ++i)
		{
			ArrayList<double[]> xy = points.get(i);
			for (int j=0; j<xy.size(); ++j)
				System.out.printf("%3d %3d %8.4f %8.4f%n", i,j, xy.get(j)[0], xy.get(j)[1]);
			System.out.println();
		}
		System.out.println();
		
	}

}
