package gms.shared.utilities.geotess.util.numerical.polygon;

import static java.lang.Math.PI;
import static java.lang.Math.abs;
import static java.lang.Math.ceil;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.toRadians;
import gms.shared.utilities.geotess.util.numerical.polygon.GreatCircle.GreatCircleException;
import gms.shared.utilities.geotess.util.numerical.vector.VectorGeo;
import gms.shared.utilities.geotess.util.numerical.vector.VectorUnit;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * An ordered list of points on the surface of a unit sphere that define a closed polygon.
 * Polygons have the ability to test whether or not an arbitrary test point on the sphere
 * is inside or outside the polygon.
 * 
 * <p>
 * Besides the set of points that define the boundary of the polgon, a Polygon object has
 * a single private instance of a reference point which is known to be either 'inside' or
 * 'outside' the polygon. By default, the reference point is computed from the supplied
 * boundary points in the following manner:
 * <ul>
 * <li>Compute the normalized vector mean of all the points that define the polygon and
 * call it <i>center</i>.
 * <li>set the referencePoint to the anti-pode of <i>center</i>.
 * <li>set referencePointIn to false
 * <li>test <i>center</i> to see if it is inside or outside the polygon and set the value
 * of <i>referencePointIn</i> to this value.
 * <li>change the value of referencePoint to <i>center</i>
 * </ul>
 * This method will work fine provided that polygons are 'smaller' than approximately a
 * hemisphere. It is possible to override this behavior. The first method is to call the
 * invert() method which simply reverses the value of whether the reference point is
 * 'inside' or 'outside' the polygon. There are also methods that allow applications to
 * specify both the position of the reference point and whether it is 'inside' or
 * 'outside' the polygon.
 * 
 * <p>
 * A convenient mechanism for generating polygons is to use Google Earth. It has the
 * ability to interactively generate polygons and store them in kmz or kml files. Polygon
 * has facilities to read (but not write) these files. Polygon can also read and write
 * polygons to an ascii file. An advantage of the ascii file format is that the position
 * of the reference point and whether the reference point is inside or outside the polygon
 * are stored in the file and therefore can be manipulated by users. For example, if a
 * user wishes to generate a polygon that is larger than a hemisphere, they could define
 * the polygon boundary points with Google Earth but the resulting polygon would be
 * inverted (the inside would be outside and the outside would be inside). They could use
 * Polygon to write the polygon to an ascii file,and manually edit the file to modify the
 * reference point definition.
 * 
 * <p>
 * A test point that is located very close to a polygon boundary point is deemed to be
 * 'inside' the polygon. This means that if two adjacent, non-overlapping polygons share a
 * boundary point, a test point near that boundary point will be deemed to be 'inside'
 * both polygons. In this context two points are 'very close' if they are separated by
 * less than 1e-7 radians or 5.7e-6 degrees. For a sphere with the radius of the Earth
 * (6371 km), this corresponds to a linear distance of about 60 cm.
 * 
 * @author sballar
 */
public class Polygon implements Cloneable, Callable<Polygon>
{
	/**
	 * A GreatCircle object for each edge of the polygon.
	 */
	protected ArrayList<GreatCircle> edges;

	/**
	 * A point on the surface of the unit sphere that is used as a reference point. The
	 * status of this point relative to the polygon is known, i.e., it is known if this
	 * point is inside or outside the polygon.
	 */
	protected double[] referencePoint;

	/**
	 * true if the referencePoint is inside the polygon.
	 */
	protected boolean referenceIn;
	
	/**
	 * The area of the polygon, assuming a unit sphere.  Area will range from 
	 * zero to 4*PI.  Lazy evaluation is used.  Area is computed the first time
	 * the area is requested.
	 */
	protected double area = Double.NaN;

	/**
	 * Tolerance value in radians used when comparing locations of two points.
	 */
	protected static final double TOLERANCE = 1e-7;

	/**
	 * If global is true this polygon encompasses the entire Earth and method contains()
	 * will always return the value of referenceIn.
	 */
	protected boolean global;

	/**
	 * When reading/writing lat,lon data, should order be lat,lon or lon,lat.
	 */
	protected boolean lonFirst;

	/**
	 * Some unspecified information that applications can attach to this polygon. This
	 * information is not processed in anyway by Polygon.
	 */
	public Object attachment;
	
	private static final int pointsPerTask=1000;
	int taskId;
	Map<double[], Boolean> taskPointMap;
	List<double[]> taskPoints;
	List<Boolean> taskContained;

	protected File polygonFile = null;

	/**
	 * Default constructor. Does nothing.
	 */
	public Polygon()
	{
	}

	/**
	 * Load a Polygon from a File. 
	 * See section "Polygons" in the GeoTess User's Manual
	 * 
	 * @param file
	 *            name of the file containing the polygon definition
	 * @throws PolygonException
	 * @throws IOException
	 */
	public Polygon(File file) throws IOException
	{
		if (!file.exists())
			throw new IOException(String.format("File does not exist%n%s",
					file.getCanonicalPath()));
		polygonFile = file;

		String fileExtension = "";
		int idx = file.getName().lastIndexOf('.');
		if (idx >= 0)
			fileExtension = file.getName().substring(idx + 1);

		if (fileExtension.equalsIgnoreCase("kml")
				|| fileExtension.equalsIgnoreCase("kmz"))
			readKMLZ(file);
		else
			parseRecords(readRecords(file));
	}

	/**
	 * Create a global polygon. Method constains(x) will always return the value of global
	 * that is specified in this constructor.
	 * 
	 * @param global
	 *            boolean
	 */
	public Polygon(boolean global)
	{
		this.global = true;
		referenceIn = global;
	}

	/**
	 * Constructor that accepts a list of unit vectors that define the polygon. The
	 * polygon will be closed, i.e., if the first point and last point are not coincident
	 * then an edge will be created between them.
	 * 
	 * <p>
	 * The referencePoint will be the anti-pode of the normalized vector sum of the
	 * supplied points and will be deemed to be 'outside' the polygon.
	 * 
	 * @param points
	 *            Collection
	 * @throws PolygonException
	 */
	public Polygon(List<double[]> points) throws IOException
	{
		setup(points);
	}

	/**
	 * Constructor that accepts a list of unit vectors that define the polygon. The
	 * polygon will be closed, i.e., if the first point and last point are not coincident
	 * then an edge will be created between them.
	 * 
	 * @param points
	 *            Collection
	 * @throws PolygonException
	 */
	public Polygon(double[][] points) throws IOException
	{
		ArrayList<double[]> aa = new ArrayList<double[]>(points.length);
		for (double[] a : points)
			aa.add(a);
		setup(aa);
	}

	/**
	 * Constructor that builds a circular polygon of a specified horizontal radius
	 * centered on position center.
	 * 
	 * <p>
	 * The referencePoint will be the anti-pode of the normalized vector sum of the
	 * supplied points and will be be deemed to be 'outside' the polygon.
	 * 
	 * @param center
	 *            unit vectors
	 * @param radius
	 *            double angular radius of the polygon, in radians.
	 * @param nEdges
	 *            int
	 * @throws PolygonException
	 */
	public Polygon(double[] center, double radius, int nEdges)
			throws IOException
	{
		ArrayList<double[]> points = new ArrayList<double[]>(nEdges);
		double[] firstPoint = new double[3];
		if (!VectorUnit.isPole(center))
			VectorUnit.moveNorth(center, radius, firstPoint);
		else
			VectorUnit.rotate(center, new double[] { 0., 1., 0. }, radius,
					firstPoint);
		points.add(firstPoint);
		for (int i = 1; i < nEdges; ++i)
		{
			double[] point = new double[3];
			VectorUnit.rotate(firstPoint, center, i * 2. * PI
					/ (double) nEdges, point);
			points.add(point);
		}
		setup(points);
	}

	/** 
	 * Read all the records from the file into a list of Strings 
	 * ignoring empty records and records that start with '#'.
	 * Trim the lines and convert to upper case.
	 * Parse a bunch of records that define a polygon.  
	 * See section "Polygons" in the GeoTess User's Manual.
	 * @param file
	 * @return list of records
	 * @throws IOException
	 */
	static public ArrayList<String> readRecords(File file) throws IOException
	{
		Scanner input = new Scanner(file);
		ArrayList<String> records = new ArrayList<String>(200);
		String record;
		while (input.hasNext())
		{
			record = input.nextLine().trim();
			if (record.length() > 0 && !record.startsWith("#"))
				records.add(record.toUpperCase());
		}
		input.close();
		return records;
	}
	
	/**
	 * Parse a bunch of records that define a polygon.  
	 * See section "Polygons" in the GeoTess User's Manual
	 * @param records
	 * @throws IOException
	 */
	public void parseRecords(ArrayList<String> records) throws IOException
	{
		String[] refpoint = null;

		ArrayList<double[]> points = new ArrayList<double[]>();

		for (String record : records)
		{
			String[] tokens = tokenize(record);

			if (tokens.length > 0)
			{
				if (tokens[0].equals("global"))
				{
					global = true;

					referenceIn = tokens.length < 2 || tokens[1].equals("in");

					referencePoint = new double[] { 1, 0, 0 };
					edges = new ArrayList<GreatCircle>();
					return;
				}
				else if (tokens[0].startsWith("lat"))
					lonFirst = false;
				else if (tokens[0].startsWith("lon"))
					lonFirst = true;
				else if (tokens[0].startsWith("reference"))
					refpoint = tokens;
				else if (tokens.length == 2)
				{
					try
					{
						double[] vals = new double[] { Double.parseDouble(tokens[0]),
								Double.parseDouble(tokens[1]) };
						points.add(vals);
					}
					catch (NumberFormatException ex)
					{ /* ignore errors */
					}
				}
			}
		}

		if (lonFirst)
			for (int i = 0; i < points.size(); ++i)
				points.set( i, VectorGeo.getVectorDegrees(points.get(i)[1],
								points.get(i)[0]));
		else
			for (int i = 0; i < points.size(); ++i)
				points.set( i, VectorGeo.getVectorDegrees(points.get(i)[0],
								points.get(i)[1]));

		if (refpoint != null && refpoint.length == 4)
		{
			if (lonFirst)
				referencePoint = VectorGeo.getVectorDegrees(
						Double.valueOf(refpoint[2]), Double.valueOf(refpoint[1]));
			else
				referencePoint = VectorGeo.getVectorDegrees(
						Double.valueOf(refpoint[1]), Double.valueOf(refpoint[2]));
			referenceIn = refpoint[3].startsWith("in");
		}

		setup(points);
	}
	
	/**
	 * Split on any number of commas and/or spaces.  
	 * Convert to lower case.  Ignore comment lines that start 
	 * with '#'.
	 * @param record
	 * @return tokenized record
	 */
	static public String[] tokenize(String record)
	{
		record = record.trim();

		if (record.length() == 0 || record.startsWith("#"))
			return new String[0];

		record = record.replaceAll(",", " ");
		while (record.contains("  "))
			record = record.replaceAll("  ", " ");

		return record.toLowerCase().split(" ");
	}

	public Polygon readKMLZ(File file) throws IOException
	{
		Scanner input = null;
		ArrayList<double[]> points = new ArrayList<double[]>();

		if (file.getName().toLowerCase().endsWith("kml"))
			input = new Scanner(file);
		else
		{
			ZipFile zf = new ZipFile(file);
			ZipEntry zip = zf.entries().nextElement();
			InputStream is = zf.getInputStream(zip);
			input = new Scanner(is);
		}

		StringBuffer contents = new StringBuffer();
		while (input.hasNext())
			contents.append(input.next().toLowerCase()).append(" ");
		input.close();

		int i0 = contents.indexOf("<polygon>");
		if (i0 < 0)
			throw new IOException("String <polygon> not found in file.");
		int i1 = contents.indexOf("<coordinates>", i0);
		if (i1 < 0)
			throw new IOException("String <coordinates> not found in file.");
		int i2 = contents.indexOf("</coordinates>");
		if (i2 < 0)
			throw new IOException("String </coordinates> not found in file.");

		input = new Scanner(contents.toString().substring(i1 + 13, i2)
				.replaceAll(",", " "));

		double lat, lon;
		while (input.hasNextDouble())
		{
			lon = input.nextDouble();
			lat = input.nextDouble();
			input.nextDouble();
			points.add(VectorGeo.getVectorDegrees(lat, lon));
		}
		input.close();

		setup(points);
		return this;
	}

	/**
	 * Write the current polygon to a file.
	 * If the file extension is 'vtk' then the file is written in 
	 * vtk format.  If the file extension is 'kml' or 'kmz' the 
	 * file is written in a format compatible with Google Earth.
	 * Otherwise the file is written in ascii format with boundary
	 * points writtein in lat-lon order.
	 * 
	 * @param fileName
	 *            name of file to receive the polygon
	 * @throws IOException
	 */
	public void write(File fileName) throws IOException
	{
		polygonFile = fileName;
		String name = fileName.getName().toLowerCase();
		if (name.endsWith("vtk"))
			vtk(fileName);
		else if (name.endsWith("kml") || name.endsWith("kmz"))
			toKML(fileName);
		else
		{
			BufferedWriter output = new BufferedWriter(new FileWriter(fileName));
			output.write("POLYGON\n");
			write(output, "lat");
			output.close();
		}
	}

	/**
	 * Returns the current polygon file setting (null if the polygon was never
	 * set from a standard file.
	 * 
	 * @return The current polygon file setting.
	 */
	public File getPolygonFile()
	{
		return polygonFile;
	}

	/**
	 * Write the current polygon to a file in ascii format.
	 * 
	 * @param fileName
	 *            name of file to receive the polygon
	 * @param latLon
	 *            if this String starts with 'lon' the polygon boundary points will be
	 *            written in lon-lat order otherwise they will be written in lat-lon
	 *            order.
	 * @throws IOException
	 */
	protected void write(BufferedWriter output, String latLon)
			throws IOException
	{
		lonFirst = latLon.trim().toLowerCase().startsWith("lon");

		output.append(toString(false, lonFirst, -180.));
	}

	/**
	 * This just writes the grid to the vtk file. Only includes the geometry and topology
	 * of the specified tessellation.
	 * 
	 * @param grid
	 * @param tessid
	 * @param output
	 * @return the indices of the vertices used in the plot.
	 * @throws IOException
	 */
	protected void vtk(File fileName) throws IOException
	{
		DataOutputStream output = new DataOutputStream(
				new BufferedOutputStream(new FileOutputStream(fileName)));

		output.writeBytes(String.format("# vtk DataFile Version 2.0%n"));
		output.writeBytes(String.format("Polygon%n"));
		output.writeBytes(String.format("BINARY%n"));

		output.writeBytes(String.format("DATASET POLYDATA%n"));

		double[][] points = getPoints(false, toRadians(1.));

		output.writeBytes(String.format("POINTS %d double%n", points.length));

		// iterate over all the polygon vertices and write out their position
		for (int i = 0; i < points.length; ++i)
		{
			output.writeDouble(points[i][0]);
			output.writeDouble(points[i][1]);
			output.writeDouble(points[i][2]);
		}

		// write out node connectivity
		output.writeBytes(String.format("POLYGONS 1 %d%n", points.length + 1));

		output.writeInt(points.length);
		for (int i = 0; i < points.length; ++i)
			output.writeInt(i);

		output.close();
	}

	/**
	 * Invert the current polygon. What used to be in will be out and what used to be out
	 * will be in.
	 */
	public void invert()
	{
		referenceIn = !referenceIn;
	}

	/**
	 * Specify the reference point for this polygon and whether or not the specified point
	 * is inside or outside the polygon.
	 * 
	 * @param referencePoint
	 * @param referenceIn
	 */
	public void setReferencePoint(double[] referencePoint, boolean referenceIn)
	{
		this.referencePoint = referencePoint.clone();
		this.referenceIn = referenceIn;
	}

	/**
	 * Specify the reference point for this polygon and whether or not the specified point
	 * is inside or outside the polygon.
	 * 
	 * @param lat
	 *            geographic latitude in degrees
	 * @param lon
	 *            longitude in degrees
	 * @param referenceIn
	 */
	public void setReferencePoint(double lat, double lon, boolean referenceIn)
	{
		referencePoint = VectorGeo.getVectorDegrees(lat, lon);
		this.referenceIn = referenceIn;
	}

	/**
	 * @param points
	 *            an array of double[3]
	 * @throws IOException
	 */
	private void setup(List<double[]> points) throws IOException
	{
		if (points.size() < 2)
			throw new IOException("Cannot create a polygon with only "
					+ points.size() + " point(s).");

		try
		{
			// there will be a GreatCircle edge for every vertex
			edges = new ArrayList<GreatCircle>(points.size() + 1);

			Iterator<double[]> it = points.iterator();
			double[] next, previous = it.next();
			GreatCircle gc;
			while (it.hasNext())
			{
				// create a GreatCircle from previous to next and add to list of
				// edges.
				next = it.next();
				gc = new GreatCircle(previous, next);
				if (gc.getDistance() >= TOLERANCE)
				{
					edges.add(gc);
					previous = next;
				}
			}

			// if last point != first point, add another edge to close the
			// polygon
			next = points.iterator().next();

			if (VectorUnit.angle(previous, next) > TOLERANCE)
				// create a GreatCircle from previous to next and add to list of
				// edges.
				edges.add(new GreatCircle(previous, next));

			if (referencePoint == null)
			{
				referencePoint = new double[3];
				referenceIn = false;

				// find the location of the vector sum of all the points.
				double[] center = VectorUnit.center(points
						              .toArray(new double[points.size()][3]));

				// deal with degenerate case where the vector sum of the points is zero.
				// One way this can happen is if all the points are evenly distributed along
				// a great circle that encircles the globe (pretty unlikely). In this case,
				// the selection of referencePoint and referencePointIn is arbitrary.
				if (center[0] == 0. && center[1] == 0. && center[1] == 0.)
				{
					center[0] = center[1] = center[2] = 0.1;
					do
					{
						referencePoint[0] = center[0];
						referencePoint[1] = center[1] *= 2;
						referencePoint[2] = center[2] *= 4;
						VectorUnit.normalize(referencePoint);
					}
					while (onBoundary(referencePoint));
				}
				else
				{

					// flip the referencePoint so that it is on the opposite side of the Earth
					referencePoint[0] = -center[0];
					referencePoint[1] = -center[1];
					referencePoint[2] = -center[2];

					if (!onBoundary(center))
					{
						referenceIn = contains(center);
						referencePoint[0] = center[0];
						referencePoint[1] = center[1];
						referencePoint[2] = center[2];
					}
				}
			}
			
			//crossCheck();
		}
		catch (Exception e)
		{
			throw new IOException(e);
		}
	}

	/**
	 * Returns true if this Polygon contains any of the supplied unit vectors
	 * 
	 * @param points
	 *            array of unit vectors
	 * @return true if this Polygon contains any of the supplied unit vectors
	 * @throws PolygonException
	 */
	public boolean containsAny(double[]... points)
	{
		for (double[] point : points)
			if (contains(point))
				return true;
		return false;
	}

	/**
	 * Returns true if this Polygon contains any of the supplied unit vectors
	 * 
	 * @param points Collection of unit vectors
	 * @return true if this Polygon contains any of the supplied unit vectors
	 * @throws PolygonException
	 */
	public boolean containsAny(Collection<double[]> points)
	{
		for (double[] point : points)
			if (contains(point))
				return true;
		return false;
	}

	/**
	 * Returns true if this Polygon contains all of the supplied unit vectors
	 * 
	 * @param points
	 *            double[][3], the unit vectors to check
	 * @return true if this Polygon contains all of the supplied unit vectors
	 * @throws PolygonException
	 */
	public boolean containsAll(double[]... points)
	{
		for (double[] point : points)
			if (!contains(point))
				return false;
		return true;
	}

	/**
	 * Returns the number of points that are contained within this Polygon
	 * 
	 * @param points Collection of unit vectors to check
	 * @return the number of points that are contained within this Polygon
	 * @throws PolygonException
	 */
	public ArrayList<Boolean> contains(List<double[]> points)
	{
		ArrayList<Boolean> contained = new ArrayList<Boolean>(points.size());
		for (double[] point : points)
			contained.add(contains(point));
		return contained;
	}

	/**
	 * Returns the number of points that are contained within this Polygon
	 * 
	 * @param points
	 *            double[][3], the unit vectors to check
	 * @return the number of points that are contained within this Polygon
	 * @throws PolygonException
	 */
	public boolean[] contains(double[]... points)
	{
		boolean[] contained = new boolean[points.length];
		for (int i=0; i<points.length; ++i)
			contained[i] = contains(points[i]);
		return contained;
	}

	/**
	 * Returns true if this Polygon contains all of the supplied unit vectors
	 * 
	 * @param points Collection of unit vectors to check
	 * @return true if this Polygon contains all of the supplied unit vectors
	 * @throws PolygonException
	 */
	public boolean containsAll(Collection<double[]> points)
	{
		for (double[] point : points)
			if (!contains(point))
				return false;
		return true;
	}

	/**
	 * return true if point x is located inside the polygon
	 * 
	 * @param x
	 *            GeoVector
	 * @return boolean
	 * @throws PolygonException
	 */
	public boolean contains(double[] x)
	{
		if (global)
			return referenceIn;

		if (VectorUnit.dot(referencePoint, x) > cos(TOLERANCE))
			return referenceIn;

		GreatCircle gcRef = new GreatCircle(referencePoint, x);

		if (onBoundary(gcRef))
			return true;

		int ncrossings = edgeCrossings(gcRef);

		return (ncrossings % 2 == 0) == referenceIn;
	}

	/**
	 * 
	 * @param x
	 *            double[3] unit vector
	 * @return int
	 * @throws PolygonException
	 * @throws GreatCircleException
	 */
	private int edgeCrossings(GreatCircle gcRef)
	{
		// for every edge that has its last point on gcRef, we need to know the index
		// of the next edge that does not have its last point on gcRef.

		int[] nextEdge = new int[edges.size()];

		// for every edge[i], set nextEdge[i] = -1 if edge[i].getLast() is not on gcRef.
		for (int i = 0; i < edges.size(); ++i)
			if (abs(VectorUnit.dot(edges.get(i).getLast(), gcRef.getNormal())) > sin(TOLERANCE))
				nextEdge[i] = -1;

		int j;

		// for every edge[i] that has its last point on gcRef, set nextEdge[i]
		// equal to the index of the next edge[j] such that edge[j].getLast()
		// is not on gcRef.
		for (int i = 0; i < nextEdge.length; ++i)
			if (nextEdge[i] == 0)
			{
				j = (i + 1) % edges.size();
				while (nextEdge[j] >= 0)
					j = (j + 1) % edges.size();
				nextEdge[i] = j;
			}

		int ncrossings = 0;

		boolean[] checked = new boolean[edges.size()];

		// find j0 such that edge[j0].getFirst() is not on gcRef
		int firstj = 0;
		for (int i = 0; i < edges.size(); ++i)
			if (abs(VectorUnit.dot(edges.get(i).getFirst(), gcRef.getNormal())) > sin(TOLERANCE))
			{
				firstj = i;
				break;
			}

		// loop over all the edges, in order, starting from edge[firstj];
		j = firstj;
		do
		{
			if (nextEdge[j] >= 0)
			{
				// the last point of edge[j] is on gcRef.
				// Find edge[k] such that edge[k].getLast() is not on gcRef.
				int k = nextEdge[j];

				// figure out if there is a crossing here. If edge[j] and edge[k]
				// are on different sides of gcRef, and the distance from reference
				// point to evaluation point is greater than the distance from the
				// reference point to edge[k].getFirst(), then a crossing occurred.
				if (VectorUnit.dot(edges.get(j).getFirst(), gcRef.getNormal())
						* VectorUnit.dot(edges.get(k).getLast(),
								gcRef.getNormal()) < 0
						&& gcRef.getDistance() >= gcRef.getDistance(edges
								.get(k).getFirst()))
					++ncrossings;

				// all the edges from edge[j] to edge[k] inclusive, have been checked
				// for crossings. Record that fact.
				checked[j] = true;
				while (j != k)
				{
					j = (j + 1) % edges.size();
					checked[j] = true;
				}
			}
			else
				j = (j + 1) % edges.size();
		}
		while (j != firstj);

		// if referencePoint is inside and number of edge crossings is even,
		// or if referencePoint is outside and number of crossings is odd,
		// then x is inside the polygon, otherwise, it is outside.

		// loop over all the edges and count the intersections.
		for (int i = 0; i < edges.size(); ++i)
			if (!checked[i]
					&& gcRef.getIntersection(edges.get(i), true) != null)
				++ncrossings;

		return ncrossings;
	}

	/**
	 * Return true if evaluation point is very close to being on the boundary of the
	 * polygon.
	 * 
	 * @param x
	 *            the evaluation point.
	 * @return true if x is very close to being on the boundary of the polygon.
	 * @throws PolygonException
	 */
	public boolean onBoundary(double[] x)
	{
		return onBoundary(new GreatCircle(referencePoint, x));
	}

	/**
	 * Return true if evaluation point is very close to being on the boundary of the
	 * polygon.
	 * 
	 * @param gcRef
	 *            the great circle from the reference point to the evaluation point.
	 * @return true if evaluation point is very close to being on the boundary of the
	 *         polygon.
	 * @throws PolygonException
	 */
	public boolean onBoundary(GreatCircle gcRef)
	{
		// point being evaluated is gcRef.getLast()
		for (GreatCircle edge : edges)
		{
			// if point is very close to a polygon point, return true
			if (VectorUnit.dot(gcRef.getLast(), edge.getFirst()) >= cos(TOLERANCE))
				return true;

			// if gcRef and edge are coincident, i.e., their normals are parallel or
			// anti-parallel, and the point being evaluated is between first and last
			// point of the edge, then return true;
			if (abs(VectorUnit.dot(gcRef.getNormal(), edge.getNormal())) >= cos(TOLERANCE)
					&& edge.getDistance(gcRef.getLast()) <= edge.getDistance())
				return true;
		}
		return false;
	}

	/**
	 * Retrieve a deep copy of the points on the polygon.
	 * 
	 * @param repeatFirstPoint
	 *            if true, last point will be reference to the same point as the first
	 *            point.
	 * @return a deep copy of the points on the polygon.
	 */
	public double[][] getPoints(boolean repeatFirstPoint)
	{
		double[][] points = new double[edges.size()
				+ (repeatFirstPoint ? 1 : 0)][];
		for (int i = 0; i < edges.size(); ++i)
			points[i] = edges.get(i).getFirst().clone();
		if (repeatFirstPoint)
			points[points.length - 1] = points[0];
		return points;
	}

	/**
	 * Retrieve a deep copy of the points on the polygon.
	 * 
	 * @param repeatFirstPoint
	 *            if true, last point will be reference to the same point as the first
	 *            point.
	 * @param maxSpacing max distance between points, in radians
	 * @return a deep copy of the points on the polygon.
	 */
	public double[][] getPoints(boolean repeatFirstPoint, double maxSpacing)
	{
		ArrayList<double[]> points = new ArrayList<double[]>(edges.size());
		for (int i = 0; i < edges.size(); ++i)
		{
			int n = (int) ceil(edges.get(i).getDistance() / maxSpacing);
			double dx = edges.get(i).getDistance() / n;
			for (int j = 0; j < n; ++j)
				points.add(edges.get(i).getPoint(j * dx));
		}
		if (repeatFirstPoint)
			points.add(edges.get(0).getFirst());

		return points.toArray(new double[points.size()][]);
	}

	/**
	 * Retrieve a reference to one point on the polygon boundary
	 * 
	 * @return a reference to the first point on the polygon
	 */
	public double[] getPoint(int index)
	{
		return edges.get(index).getFirst();
	}

	/**
	 * Retrieve the area of this polygon. This is the unitless area (radians squared). It
	 * must be multiplied by R^2 where R is the radius of the sphere.
	 * <p>
	 * The area is computed assuming that the points on the polygon are listed in
	 * clockwise order when viewed from outside the unit sphere. If the compliment of this
	 * area is desired, simply subtract the reported area from the surface area of the
	 * entire sphere (4*PI).
	 * 
	 * @return the area of this polygon.
	 */
	public double getArea()
	{
		if (Double.isNaN(area))
		{
			area = 0;
			double a;
			GreatCircle edge, previous = edges.get(edges.size() - 1);
			for (int i = 0; i < edges.size(); ++i)
			{
				edge = edges.get(i);
				a = PI - VectorUnit.angle(previous.getNormal(), edge.getNormal());

				if (VectorUnit.scalarTripleProduct(previous.getNormal(),
						edge.getNormal(), edge.getFirst()) < 0)
					area += a;
				else
					area += 2 * PI - a;

				previous = edge;
			}
			area -= (edges.size() - 2) * PI;
		}
		return area;
	}

	/**
	 * Retrieve the area of this polygon. This is the unitless area (radians squared). It
	 * must be multiplied by R^2 where R is the radius of the sphere.
	 * <p>
	 * The area is computed assuming that the polygon area is less than half the area of
	 * the entire sphere. If the compliment of this area is desired, simply subtract the
	 * reported area from the surface area of the entire sphere (4*PI).
	 * 
	 * @return the area of this polygon.
	 */
	public double getAreaSmall()
	{
		double area = getArea();
		return area <= 2 * PI ? area : 4 * PI - area;
	}

	/**
	 * Returns a String containing all the points that define the polygon with one lon,
	 * lat pair per record. lats and lons are in degrees.
	 * <p>
	 * If longitudeFirst is true, points are listed as lon, lat. If false, order is lat,
	 * lon.
	 * <p>
	 * Longitudes will be adjusted so that they fall in the range minLongitude to
	 * (minLongitude+360).
	 * 
	 * @param repeatFirstPoint
	 * 
	 * @return String
	 * @param longitudeFirst
	 *            boolean
	 * @param minLongitude
	 *            double
	 */
	public String toString(boolean repeatFirstPoint, boolean longitudeFirst,
			double minLongitude)
	{
		StringBuffer buf = new StringBuffer();
		if (global)
		{
			buf.append("global");
			if (!referenceIn)
				buf.append(" out");
			buf.append("\n");
		}
		else
		{
			buf.append(longitudeFirst ? "lon-lat\n" : "lat-lon\n");
			buf.append("referencePoint "
					+ getReferencePointString(longitudeFirst)
					+ (referenceIn ? " in" : " out") + '\n');
			double lat, lon;
			for (double[] point : getPoints(repeatFirstPoint))
			{
				lat = VectorGeo.getLatDegrees(point);
				lon = VectorGeo.getLonDegrees(point);
				while (lon < minLongitude)
					lon += 360.;
				while (lon >= minLongitude + 360)
					lon -= 360.;

				if (longitudeFirst)
					buf.append(String.format("%11.6f %10.6f%n", lon, lat));
				else
					buf.append(String.format("%10.6f %11.6f%n", lat, lon));
			}
		}
		return buf.toString();
	}

	/**
	 * Returns a String containing all the points that define the polygon with one lon,
	 * lat pair per record. lats and lons are in degrees. Longitudes range from -180 to
	 * 180 degrees.
	 * 
	 * @return String
	 */
	@Override
	public String toString()
	{
		return toString(true, false, -180.);
	}

	/**
	 * Returns the number of edges that define the polygon. Equals the number of unique
	 * GeoVectors that define the polygon.
	 * 
	 * @return the number of edges that define the polygon. Equals the number of unique
	 *         GeoVectors that define the polygon.
	 */
	public int size()
	{
		if (edges == null)
			return 0;
		else
		  return edges.size();
	}

	/**
	 * Retrieve the tolerance value in radians used when comparing locations of two
	 * points.
	 */
	static public double getTolerance()
	{
		return TOLERANCE;
	}

	/**
	 * Retrieve a reference to the referencePoint.
	 * 
	 * @return a reference to the referencePoint.
	 */
	public double[] getReferencePoint()
	{
		return referencePoint;
	}

	public String getReferencePointString(boolean longitudeFirst)
	{
		if (longitudeFirst)
			return String.format("%12.6f %12.6f",
					VectorGeo.getLonDegrees(referencePoint),
					VectorGeo.getLatDegrees(referencePoint));
		return String.format("%12.6f %12.6f",
				VectorGeo.getLatDegrees(referencePoint),
				VectorGeo.getLonDegrees(referencePoint));

	}

	public boolean getReferencePointIn()
	{
		return referenceIn;
	}
	
//	private void crossCheck() throws PolygonException
//	{
//		for (int i=0; i<edges.size(); i++)
//			for (int n=i+2; n < i+edges.size()-1; ++n)
//			{
//				int j = n % edges.size();
//				
//				System.out.printf("%d %d %d%n", i, n, j);
//				
//				double[] u = edges.get(i).getIntersection(edges.get(j), true);
//				if (u != null && VectorGeo.dot(u, edges.get(i).getLast()) < cos(1e-7) 
//						&& VectorGeo.dot(u, edges.get(j).getLast()) < cos(1e-7))
//				{
//					StringBuffer buf = new StringBuffer();
//					buf.append(String.format("%nCrossCheck failed. Edges %d and %d intersect:%n", i, j));
//					for (int k : new int[] {i,j})
//						buf.append(String.format("Edge %3d extends from %s to %s%n", k,
//								VectorGeo.getLatLonString(edges.get(k).getFirst()),
//								VectorGeo.getLatLonString(edges.get(k).getLast())));
//					buf.append(String.format("Point of intersection %s%n",
//							VectorGeo.getLatLonString(u)));
//					throw new PolygonException(buf.toString());
//				}
//			}
//	}
	
	public void toKML(File outputFile) throws IOException
	{
		if (outputFile.getName().toLowerCase().endsWith("kml"))
		{
			BufferedWriter output = new BufferedWriter(new FileWriter(outputFile));

			output.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
			output.write("<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n");
			output.write("<Document>\n");
			output.write(String.format("<name>%s</name>%n", outputFile.getName()));
			output.write("<open>0</open>\n");
			output.write("<Style id=\"polystyle\"> <PolyStyle> <fill>0</fill> </PolyStyle> </Style>\n");
			output.write("<Placemark> <styleUrl>#polystyle</styleUrl> <Polygon> <outerBoundaryIs> <LinearRing> <coordinates>\n");

			for (GreatCircle edge : edges)
				output.write(String.format("%1.6f,%1.6f,0%n",
						VectorGeo.getLonDegrees(edge.getFirst()),
						VectorGeo.getLatDegrees(edge.getFirst())));

			output.write(String.format("%1.6f,%1.6f,0%n",
					VectorGeo.getLonDegrees(edges.get(0).getFirst()),
					VectorGeo.getLatDegrees(edges.get(0).getFirst())));

			output.write("</coordinates></LinearRing></outerBoundaryIs></Polygon></Placemark>\n");
			output.write("</Document>\n");
			output.write("</kml>\n");
			output.close();
		}
		else if (outputFile.getName().toLowerCase().endsWith("kmz"))
		{
			FileOutputStream fos = new FileOutputStream(outputFile);
			ZipOutputStream zoS = new ZipOutputStream(fos);
			String name = outputFile.getName();
			name = name.substring(0, name.length()-1).concat("l");
			ZipEntry ze = new ZipEntry(name);
			zoS.putNextEntry(ze);
			PrintStream output = new PrintStream(zoS);          

			output.print("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
			output.print("<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n");
			output.print("<Document>\n");
			output.print(String.format("<name>%s</name>%n", name));
			output.print("<open>0</open>\n");
			output.print("<Style id=\"polystyle\"> <PolyStyle> <fill>0</fill> </PolyStyle> </Style>\n");
			output.print("<Placemark> <styleUrl>#polystyle</styleUrl> <Polygon> <outerBoundaryIs> <LinearRing> <coordinates>\n");

			for (GreatCircle edge : edges)
				output.print(String.format("%1.6f,%1.6f%n",
						VectorGeo.getLonDegrees(edge.getFirst()),
						VectorGeo.getLatDegrees(edge.getFirst())));

			output.print(String.format("%1.6f,%1.6f%n",
					VectorGeo.getLonDegrees(edges.get(0).getFirst()),
					VectorGeo.getLatDegrees(edges.get(0).getFirst())));

			output.print("</coordinates></LinearRing></outerBoundaryIs></Polygon></Placemark>\n");
			output.print("</Document>\n");
			output.print("</kml>\n");
			zoS.closeEntry(); // close KML entry
			zoS.close();
		}
	}
	
	/**
	 * Add new edges to the Polygon to ensure that the separation of any two
	 * points that define the boundary is no greater than maxSpacing (radians).
	 * @param maxSpacing in radians.
	 */
	public void densifyEdges(double maxSpacing)
	{
		ArrayList<GreatCircle> newEdges = new ArrayList<GreatCircle>();
		while (!edges.isEmpty())
		{
			GreatCircle edge = edges.remove(0);
			int n = (int) Math.ceil(edge.getDistance()/maxSpacing);
			if (n <= 1)
				newEdges.add(edge);
			else
			{
				double dx = edge.getDistance()/n;
				double[] next, previous = edge.getFirst();
				for (int i=1; i<=n; ++i)
				{
					next = i == n ? edge.getLast() : edge.getPoint(i*dx);
					newEdges.add(new GreatCircle(previous, next));
					previous = next;
				}
			}
		}
		edges.addAll(newEdges);
	}

	/**
	 * Determine if a list of unit vectors is contained within this polygon. 
	 * Concurrency is used to process batches of points in parallel.
	 * @param points list of points to evaluate
	 * @param nProcessors  number of processors to use in concurrent mode.  
	 * If nProcessors < 2, points are evaluated in sequential mode.
	 * @return ArrayList<Boolean> of length equal to input points.
	 * @throws IOException
	 */
	public ArrayList<Boolean> contains(List<double[]> points, int nProcessors) throws IOException
	{
		ArrayList<Boolean> contained = new ArrayList<Boolean>(points.size());
		
		int nTasks = (int) ceil(points.size()/(double)pointsPerTask);
		
		if (nProcessors < 2 || nTasks < 1)
		{
			for (int i=0; i < points.size(); ++i)
				contained.add(contains(points.get(i)));
		}
		else
		{
			try 
			{
				for (int i=0; i<points.size(); ++i) contained.add(null);

				// set up the thread pool.
				ThreadPoolExecutor threadPool = (ThreadPoolExecutor) Executors
						.newFixedThreadPool(nProcessors);

				CompletionService<Polygon> queue = 
						new ExecutorCompletionService<Polygon>(threadPool);

				// submit all the tasks at once.
				for (int taskId = 0; taskId < nTasks; ++taskId)
				{
					Polygon task = (Polygon) this.clone();

					task.taskId = taskId;
					task.taskPoints = points;
					task.taskContained = contained;
					queue.submit(task); 
				}

				// pause until all the tasks are complete.
				for (int taskId=0; taskId<nTasks; ++taskId)  queue.take().get();

				threadPool.shutdown();

			} 
			catch (Exception e) 
			{
				throw new IOException(e);
			}
		}
		return contained;
	}

	/**
	 * Determine if a set of unit vectors is contained within this polygon. 
	 * Concurrency is used to process batches of points in parallel.
	 * @param points set of points to evaluate
	 * @param nProcessors  number of processors to use in concurrent mode.  
	 * If nProcessors < 2, points are evaluated in sequential mode.
	 * @return HashMap<double[], Boolean> of length equal to input points.
	 * @throws IOException
	 */
	public HashMap<double[], Boolean> contains(Set<double[]> points, int nProcessors) throws IOException
	{
		HashMap<double[], Boolean> contained = new HashMap<double[], Boolean>(points.size());
		
		int nTasks = (int) ceil(points.size()/(double)pointsPerTask);
		
		if (nProcessors < 2 || nTasks < 2)
		{
			for (double[] point : points)
				contained.put(point, contains(point));
		}
		else
		{
			try 
			{
				// set up the thread pool.
				ThreadPoolExecutor threadPool = (ThreadPoolExecutor) Executors
						.newFixedThreadPool(nProcessors);

				CompletionService<Polygon> queue = 
						new ExecutorCompletionService<Polygon>(threadPool);

				Iterator<double[]> it = points.iterator();
				for (int taskId = 0; taskId < nTasks; ++taskId)
				{
					Polygon task = (Polygon) this.clone();

					task.taskPointMap = new HashMap<double[], Boolean>(pointsPerTask);
					for (int i=0; i<pointsPerTask && it.hasNext(); ++i)
						task.taskPointMap.put(it.next(), null);
					
					queue.submit(task);
				}

				// pause until all the tasks are complete.
				for (int taskId=0; taskId<nTasks; ++taskId)
				{
					Polygon task = (Polygon) queue.take().get();
					for (Map.Entry<double[], Boolean> entry : task.taskPointMap.entrySet())
						contained.put(entry.getKey(), entry.getValue());
				}

				threadPool.shutdown();

			} 
			catch (Exception e) 
			{
				e.printStackTrace();
				throw new IOException(e);
			}
		}
		return contained;
	}

	/**
	 * Determine if a set of unit vectors is contained within this polygon. 
	 * Concurrency is used to process batches of points in parallel.
	 * @param points map from unit vector to boolean containing points to evaluate.
	 * Only points where getValue() == null are evaluated.  If a point has 
	 * getValue() = true or false, it is assumed that the point has already
	 * been evaluated and it is not evaluated again.
	 * @param nProcessors  number of processors to use in concurrent mode.  
	 * If nProcessors < 2, points are evaluated in sequential mode.
	 * @throws IOException
	 */
	public void contains(Map<double[], Boolean> points, int nProcessors) throws IOException
	{
		int nTasks = (int) ceil(points.size()/(double)pointsPerTask);
		
		if (nProcessors < 2 || nTasks < 2)
		{
			for (Entry<double[], Boolean> point : points.entrySet())
				if (point.getValue() == null)
					point.setValue(contains(point.getKey()));
		}
		else
		{
			try 
			{
				// set up the thread pool.
				ThreadPoolExecutor threadPool = (ThreadPoolExecutor) Executors
						.newFixedThreadPool(nProcessors);

				CompletionService<Polygon> queue = 
						new ExecutorCompletionService<Polygon>(threadPool);

				Iterator<Map.Entry<double[], Boolean>> it = points.entrySet().iterator();
				for (int taskId = 0; taskId < nTasks; ++taskId)
				{
					Polygon task = (Polygon) this.clone();

					task.taskPointMap = new HashMap<double[], Boolean>(pointsPerTask);
					for (int i=0; i<pointsPerTask && it.hasNext(); ++i)
					{
						Map.Entry<double[], Boolean> entry = it.next();
						task.taskPointMap.put(entry.getKey(), entry.getValue());
					}
					
					queue.submit(task);
				}

				// pause until all the tasks are complete.
				for (int taskId=0; taskId<nTasks; ++taskId)
				{
					Polygon task = (Polygon) queue.take().get();
					for (Map.Entry<double[], Boolean> entry : task.taskPointMap.entrySet())
						points.put(entry.getKey(), entry.getValue());
				}

				threadPool.shutdown();

			} 
			catch (Exception e) 
			{
				e.printStackTrace();
				throw new IOException(e);
			}
		}
	}

	@Override
	public Polygon call()
	{
		if (taskPoints != null)
		{
			for (int i=taskId*pointsPerTask; i<(taskId+1)*pointsPerTask && i < taskPoints.size(); ++i)
				taskContained.set(i, contains(taskPoints.get(i)));
		}
		if (taskPointMap != null)
		{
			for (Map.Entry<double[], Boolean> entry : taskPointMap.entrySet())
				if (entry.getValue() == null)
					entry.setValue(contains(entry.getKey()));
			
		}
		return this;
	}

}
