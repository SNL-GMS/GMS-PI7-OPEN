package gms.shared.utilities.geotess;

import static gms.shared.utilities.geotess.util.globals.Globals.NL;
import static gms.shared.utilities.geotess.util.globals.Globals.readString;
import static gms.shared.utilities.geotess.util.globals.Globals.writeString;
import gms.shared.utilities.geotess.util.containers.arraylist.ArrayListInt;
import gms.shared.utilities.geotess.util.containers.hash.maps.HashMapIntegerDouble;
import gms.shared.utilities.geotess.util.containers.hash.sets.HashSetInteger;
import gms.shared.utilities.geotess.util.containers.hash.sets.HashSetInteger.Iterator;
import gms.shared.utilities.geotess.util.globals.DataType;
import gms.shared.utilities.geotess.util.globals.GMTFormat;
import gms.shared.utilities.geotess.util.md5.MD5Hash;
import gms.shared.utilities.geotess.util.numerical.platonicsolid.PlatonicSolid;
import gms.shared.utilities.geotess.util.numerical.vector.EarthShape;
import gms.shared.utilities.geotess.util.numerical.vector.VectorUnit;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Manages the geometry and topology of a multi-level triangular tessellation of
 * a unit sphere. It knows:
 * <ul>
 * <li>the positions of all the vertices,
 * <li>the connectivity information that defines how vertices are connected to
 * form triangles,
 * <li>for each triangle it knows the indexes of the 3 neighboring triangles,
 * <li>for each triangle it knows the index of the triangle which is a
 * descendant at the next higher tessellation level, if there is one.
 * <li>information about which triangles reside on which tessellation level
 * </ul>
 * <p>
 * GeoTessGrid is thread-safe in that its internal state is not modified after
 * its data has been loaded into memory, except in synchronized methods. The
 * design intention is that single instances of a GeoTessGrid object and
 * GeoTessData object can be shared among all the threads in a multi-threaded
 * application and each thread will have it's own instance of a GeoTessPosition
 * object that references the common GeoTessGrid + GeoTessData combination.
 * 
 * <p>
 * References Ballard, S., J. R. Hipp and C. J. Young, 2009, Efficient and
 * Accurate Calculation of Ray Theory Seismic Travel Time Through Variable
 * Resolution 3D Earth Models, Seismological Research Letters, v.80, n. 6 p.
 * 989-999.
 * 
 * @author Sandy Ballard
 */
public class GeoTessGrid
{
	/**
	 * an nVertices x 3 array of tessellation vertices. Each vertex is
	 * represented by a 3-component unit vector with it's origin at the center
	 * of the earth. The x-component points toward lat,lon = 0, 0. The
	 * y-component points toward lat,lon = 0, 90. The z-component points toward
	 * north pole.
	 */
	protected double[][] vertices;

	/**
	 * an nTriangles x 3 array of integers. Each triangle is represented by the
	 * indexes of the 3 vertices that form the corners of the triangle, listed
	 * in clockwise order when viewed from outside the tessellation.
	 */
	protected int[][] triangles;

	/**
	 * An n x 2 array where n is the number of tessellation levels in all the
	 * tessellations that constitute this model. For each tessellation level, the first
	 * element of the int[2] specifies the index of the first triangle in the tess level
	 * and the second element specifies the index of the last triangle on the tess level +
	 * 1.
	 * <p>
	 * A level is a single-level tessellation of a unit sphere, which is to say that it is
	 * a set of triangles that completely spans the surface of a unit sphere without gaps
	 * or overlaps.
	 */
	protected int[][] levels;

	/**
	 * tessellations is a n x 2 int array where n is the number of tessellations
	 * in the topology of the model. Each element specifies [0] the index of the
	 * first level and [1] last level + 1 that make up the tessellation.
	 * <p>
	 * A tessellation is a multi-level tessellation of a unit sphere, which is
	 * to say that it is a hierarchical set of single-level tessellations (see
	 * 'levels' above).
	 * <p>
	 * To loop over the triangles of tessellation tessid, level levelid: <br>
	 * for (int i=getFirtTriangle(tessid, levelid); i < getLastTriangle(tessid,
	 * levelid); ++i) <br>
	 * { // do something with triangles[i] }
	 */
	protected int[][] tessellations;

	/**
	 * For every triangle, its descendant is the index of a triangle on the next
	 * higher tessellation level. In the normal course of events, a triangle on
	 * tessellation level i will be subdivided into 4 triangles on tessellation level i+1.
	 * A triangle's descendant will be the central one of those 4 triangles.
	 * For triangles on the top level of a given tessellation, their descendants are -1.
	 */
	protected int[] descendants;

	/**
	 * An nTriangles by 3 array of Edge objects. For a triangle formed by vertices
	 * i, j and k, edge[i] is the edge opposite vertex i. Put another way, edge i
	 * is the edge that does not contain vertex i.  For edge(i), edge.vj and
	 * edge.vk are the other two vertices of the triangle accessed clockwise order.
	 * edge.tLeft is the index of the triangle on the left side of the edge from
	 * vj to vk (the triangle that does not contain vertex i). edge.tRight is the
	 * index of the triangle on the right side of the edge from vj to vk (the
	 * triangle that contain vertex i).  edge(i).normal is the unit vector normal
	 * to edge from vj to vk.  It was computed as edge.vk cross edge.vj, NOT normalized
	 * to unit length.
	 *
	 * <p>Note that edgeList and spokeList contain pointers to the same instantiations
	 * of Edge objects.  edgeList does not use the "Edge* next" field in the Edge objects
	 * but spokeList relies on those fields.  Hence the 'next' pointers in Edge objects
	 * should not be manipulated via edgeList.
	 */
	protected Edge[][] edgeList;

	/**
	 * An nLevels x nVertices array of Edge objects that define spokes emanating from each
	 * vertex in clockwise order. spokeList[level][vertex] returns a pointer to single Edge
	 * object which is the entry point into a circular list of Edge objects.  Given an Edge
	 * object, subsequent edges are accessed with edge.next.  This is a circular list, so
	 * you can't simply follow next forever (infinite loop).  You have to keep track of the
	 * first edge (head) and loop until edge == head.
	 *
	 * Each Spoke stores the index of the vertex, the index of the neighboring vertex,
	 * the triangle to the right of the spoke and the triangle to the left of the spoke.
	 * Following edge.next iterates over the spokes in clockwise order.
	 */
	private Edge[][] spokeList;
	private final Object spokeListLock = new Object(); 

	/**
	 * An nLevels x nVertices x n array that stores the indices of the n
	 * triangles of which each vertex is a member. Lazy evaluation is used to
	 * load these indices.
	 */
	private ArrayListInt[][] vtxTriangles;

	/**
	 * An nTriangles x 4 array that stores the circumCenters of each triangle in
	 * the Lazy evaluation is used.  The first 3 elements are the unit vector and
	 * the fourth is the cos(circumCircle radius).
	 */
	private double[][] circumCenters;
	private final Object circumCenterLock = new Object(); 

	/**
	 * An nLevels array of sets where each set contains the indices
	 * of the vertices that are connected together by triangles on the 
	 * corresponding level.
	 */
	private HashSetInteger[] connectedVertices;
	private final Object connectedVerticesLock = new Object(); 

	/**
	 * A String ID that uniquely identifies this GeoTessGrid. It must be true
	 * that two GeoTessGrid objects that have different geometry or topology
	 * also have different uniqueID values. An MD5 hash of the primary data
	 * structures (tessellations, levels, triangles and vertices) would be an
	 * excellent choice for the uniqueId, but the uniqueId can be any String
	 * that uniquely identifies the grid.
	 */
	protected String gridID;

	/**
	 * The name of the file from which the grid was loaded.
	 */
	protected File gridInputFile = null;

	/**
	 * Name and version number of the software that generated this grid.
	 */
	protected String gridSoftwareVersion = "";

	/**
	 * The date when this grid was generated. Not necessarily the same as the
	 * date that the grid file was copied or translated.
	 */
	protected String gridGenerationDate = "";

	/**
	 * The name of the file from which the grid was loaded.
	 */
	protected String gridOutputFile;

	/**
	 * Default constructor - OptimizationType will be set to SPEED.
	 */
	public GeoTessGrid()
	{
	}

	/**
	 * Default constructor - 
	 * @param gridFileName name of file containing the grid to load.
	 * @throws IOException 
	 */
	public GeoTessGrid(File gridFileName) throws IOException
	{
		loadGrid(gridFileName);
	}

	/**
	 * Default constructor 
	 * @param gridFileName name of file containing the grid to load.
	 * @throws IOException 
	 */
	public GeoTessGrid(String gridFileName) throws IOException
	{
		this(new File(gridFileName));
	}

	/**
	 * Constructor that loads a grid from a binary DataInputStream.
	 * 
	 * @param input
	 * @param optimization
	 * @throws IOException
	 */
	protected GeoTessGrid(DataInputStream input) throws IOException
	{
		loadGrid(input);
	}

	/**
	 * Constructor that loads a grid from an ascii file.
	 * 
	 * @param input
	 * @param optimization
	 * @throws IOException
	 */
	protected GeoTessGrid(Scanner input)
			throws IOException
	{
		loadGrid(input);
	}

	//	/**
	//	 * Constructor that loads a grid from a netcdf file.
	//	 * 
	//	 * @param input
	//	 * @param optimization
	//	 * @throws IOException
	//	 */
	//	protected GeoTessGrid(NetcdfFile input, OptimizationType optimization)
	//			throws IOException
	//	{
	//		this.optimization = optimization;
	//		loadGridNetcdf(input);
	//	}

	/**
	 * Parameterized constructor.
	 * @param tessellations
	 * @param levels
	 * @param triangles
	 * @param vertices
	 * @throws IOException
	 * @throws GeoTessException 
	 */
	public GeoTessGrid(int[][] tessellations,
			int[][] levels,
			int[][] triangles,
			double[][] vertices)
					throws IOException, GeoTessException
	{
		this.tessellations = tessellations;
		this.levels = levels;
		this.triangles = triangles;
		this.vertices = vertices;
		initialize();

		testGrid();

		recomputeGridID();
	}
	
	public GeoTessGrid(GeoTessGrid other)
	{
		this.gridSoftwareVersion = this.getClass().getCanonicalName();
		this.gridGenerationDate = GMTFormat.getNow();
		
		this.tessellations = new int[other.tessellations.length][];
		for (int i = 0; i < tessellations.length; ++i)
			this.tessellations[i] = other.tessellations[i].clone();
		
		this.levels = new int[other.levels.length][];
		for (int i = 0; i < levels.length; ++i)
			this.levels[i] = other.levels[i].clone();
		
		this.vertices = new double[other.vertices.length][];
		for (int i = 0; i < vertices.length; ++i)
			this.vertices[i] = other.vertices[i].clone();
		
		this.triangles = new int[other.triangles.length][];
		for (int i = 0; i < triangles.length; ++i)
			this.triangles[i] = other.triangles[i].clone();
		
		initialize();
		this.gridID = other.gridID;
	}

	public GeoTessGrid(GeoTessGrid other, double euler0, double euler1, double euler2, boolean inDegrees)
	{
		this.gridSoftwareVersion = this.getClass().getCanonicalName();
		this.gridGenerationDate = GMTFormat.getNow();
		
		this.tessellations = new int[other.tessellations.length][];
		for (int i = 0; i < tessellations.length; ++i)
			this.tessellations[i] = other.tessellations[i].clone();
		
		this.levels = new int[other.levels.length][];
		for (int i = 0; i < levels.length; ++i)
			this.levels[i] = other.levels[i].clone();
		
		this.vertices = new double[other.vertices.length][];
		for (int i = 0; i < vertices.length; ++i)
			this.vertices[i] = other.vertices[i].clone();
		
		this.triangles = new int[other.triangles.length][];
		for (int i = 0; i < triangles.length; ++i)
			this.triangles[i] = other.triangles[i].clone();
		
		if (inDegrees)
		{
			euler0 = Math.toRadians(euler0);
			euler1 = Math.toRadians(euler1);
			euler2 = Math.toRadians(euler2);
		}
		
		double[][] eulerMatrix = VectorUnit.getEulerMatrix(euler0, euler1, euler2);
		for (int i=0; i<vertices.length; ++i)
			VectorUnit.eulerRotation(vertices[i], eulerMatrix, vertices[i]);

		initialize();
		this.gridID = other.gridID;
	}

	/**
	 * Load GeoTessGrid object from a File. If the extension is 'ascii' the model is read
	 * from an ascii file, otherwise it is read from a binary file.
	 * 
	 * @param inputFile
	 *            the name of the file from which the grid is to be read.
	 * @return a reference to <i>this</i>
	 * @throws GeoTessException
	 * @throws IOException
	 */
	public GeoTessGrid loadGrid(String inputFile) throws IOException
	{
		File f = new File(inputFile);

		if (!f.exists())
			throw new IOException(String.format(
					"%nGeoTessGrid file does not exist%n%s%n", inputFile));

		setGridInputFile(f);

		if (inputFile.endsWith(".ascii"))
			loadGridAscii(inputFile);
		//		else if (inputFile.endsWith(".nc"))
		//			loadGridNetcdf(inputFile);
		else
			loadGridBinary(inputFile);

		return this;
	}


	/**
	 * Load GeoTessGrid object from a File. If the extension fileInputStream 'ascii' the model fileInputStream read
	 * from an ascii file, otherwise it fileInputStream read from a binary file.
	 *
	 * @param inputFileUrl
	 *            the name of the file from which the grid fileInputStream to be read.
	 * @return a reference to <i>this</i>
	 * @throws GeoTessException
	 * @throws IOException
	 */
	public GeoTessGrid loadGrid(String filePath, InputStream fileInputStream) throws IOException
	{
		try {
			if (filePath.endsWith(".ascii")) {
				try (Scanner input = new Scanner(fileInputStream)) {
					loadGrid(input);
				}
			} else {
				try (DataInputStream input =
						new DataInputStream(new BufferedInputStream(fileInputStream))) {
					loadGrid(input);
				}
			}
		} catch (IOException e) {
			throw e;
		}

		return this;
	}

	/**
	 * Load GeoTessGrid object from a File. If the extension is 'ascii' the model is read
	 * from an ascii file, otherwise it is read from a binary file.
	 * 
	 * @param inputFile
	 *            the name of the file from which the grid is to be read.
	 * @return a reference to <i>this</i>
	 * @throws GeoTessException
	 * @throws IOException
	 */
	public GeoTessGrid loadGrid(File inputFile) throws IOException
	{
		return loadGrid(inputFile.getCanonicalPath());
	}

	/**
	 * This grid and other grid are equal if their gridIDs are equal.
	 * If their gridIDs are not equal then their tessellations, levels,
	 * triangles, neighbors and vertices are all compared.  Vertices
	 * don't have to be ==.  It is sufficient that their dot product 
	 * is very, very close to 1.
	 * 
	 * @param other
	 * @return this.gridID.equals(other.getGridID());
	 */
	@Override
	public boolean equals(Object other)
	{
		if (other == null || !(other instanceof GeoTessGrid))
			return false;

		GeoTessGrid otherGrid = (GeoTessGrid)other;

		if (this.gridID.equals(otherGrid.gridID))
			return true;

		if (this.vertices.length != otherGrid.vertices.length)
			return false;
		if (this.triangles.length != otherGrid.triangles.length)
			return false;
		if (this.levels.length != otherGrid.levels.length)
			return false;
		if (this.tessellations.length != otherGrid.tessellations.length)
			return false;

		for (int i=0; i<tessellations.length; ++i)
			for (int j=0; j<2; ++j)
				if (this.tessellations[i][j] != otherGrid.tessellations[i][j])
					return false;

		for (int i=0; i<levels.length; ++i)
			for (int j=0; j<2; ++j)
				if (this.levels[i][j] != otherGrid.levels[i][j])
					return false;

		for (int i=0; i<triangles.length; ++i)
			for (int j=0; j<3; ++j)
				if (this.triangles[i][j] != otherGrid.triangles[i][j] || 
				!this.edgeList[i][j].equals(otherGrid.edgeList[i][j]))
					return false;

		//		for (int i=0; i<neighbors.length; ++i)
		//			for (int j=0; j<4; ++j)
		//				if (this.neighbors[i][j] != otherGrid.neighbors[i][j])
		//					return false;

		for (int i=0; i<vertices.length; ++i)
			if (GeoTessUtils.dot(this.vertices[i], otherGrid.vertices[i]) < 1.-1e-14)
				return false;

		return true;
	}

	/**
	 * A String ID that uniquely identifies this GeoTessGrid. It must be true
	 * that two GeoTessGrid objects that have different geometry or topology
	 * also have different gridID values. An MD5 hash of the primary data
	 * structures (tessellations, levels, triangles and vertices) would be an
	 * excellent choice for the gridID, but the gridID can be any String that
	 * uniquely identifies the grid.
	 * 
	 * @return String gridID
	 */
	public String getGridID()
	{
		return gridID;
	}

	/**
	 * Recompute the gridID.  This resets the value of the 
	 * internal gridID owned by this instance of GeoTessGrid.
	 * @return the gridID
	 */
	public String recomputeGridID()
	{
		MD5Hash md5 = new MD5Hash();
		md5.update(tessellations);
		md5.update(levels);
		md5.update(triangles);
		md5.update(vertices);
		gridID = md5.toString().toUpperCase();
		return gridID;
	}

	/**
	 * Retrieve the name and version number of the software that generated the
	 * contents of this grid.
	 * 
	 * @return the name and version number of the software that generated the
	 *         contents of this grid.
	 */
	public String getGridSoftwareVersion()
	{
		return gridSoftwareVersion;
	}

	/**
	 * Retrieve the date when the contents of this grid was generated. This is
	 * not necessarily the same as the date when the file was copied or
	 * translated.
	 * 
	 * @return the date when the contents of this grid was generated.
	 */
	public String getGridGenerationDate()
	{
		return gridGenerationDate;
	}

	/**
	 * Retrieve the platonic solid that was used to initiate construction of 
	 * this grid.
	 * @return one of TETRAHEDRON, OCTAHEDRON, ICOSAHEDRON or TETRAHEXAHEDRON
	 * @throws GeoTessException
	 */
	public PlatonicSolid getPlatonicSolid() throws GeoTessException
	{
		// solid is determined by number of triangles on level 0.
		switch (levels[0][1])
		{
		case 4 :
			return PlatonicSolid.TETRAHEDRON;
		case 8 :
			return PlatonicSolid.OCTAHEDRON;
		case 20 :
			return PlatonicSolid.ICOSAHEDRON;
		case 24 :
			return PlatonicSolid.TETRAHEXAHEDRON;
		default :
			throw new GeoTessException(String.format("Input model has %d triangles on tessellation level 0, "
					+ "which does not correspond to a supported PlatonicSolid", levels[0][1]));
		}

	}

	/**
	 * Retrieve the unit vector that corresponds to the specified vertex.
	 * 
	 * @param vertex
	 *            index of desired vertex
	 * @return the unit vector that corresponds to the specified vertex.
	 */
	public double[] getVertex(int vertex)
	{
		return vertices[vertex];
	}

	/**
	 * Get the index of the vertex that occupies the specified position in the
	 * hierarchy.
	 * 
	 * @param tessId
	 *            tessellation index
	 * @param level
	 *            index of a level relative to the first level of the specified
	 *            tessellation
	 * @param triangle
	 *            the i'th triangle in the specified tessellation/level
	 * @param corner
	 *            the i'th corner of the specified tessellation/level/triangle
	 * @return index of a vertex
	 */
	public int getVertexIndex(int tessId, int level, int triangle, int corner)
	{
		return triangles[levels[tessellations[tessId][0] + level][0] + triangle][corner];
	}

	/**
	 * Get the unit vector of the vertex that occupies the specified position in
	 * the hierarchy.
	 * 
	 * @param tessId
	 *            tessellation index
	 * @param level
	 *            index of a level relative to the first level of the specified
	 *            tessellation
	 * @param triangle
	 *            the i'th triangle in the specified tessellation/level
	 * @param corner
	 *            the i'th corner of the specified tessellation/level/triangle
	 * @return unit vector of a vertex
	 */
	public double[] getVertex(int tessId, int level, int triangle, int corner)
	{
		return vertices[triangles[levels[tessellations[tessId][0] + level][0]
				+ triangle][corner]];
	}

	/**
	 * Retrieve a reference to all of the vertices. Vertices consists of an
	 * nVertices x 3 array of doubles. The double[3] array associated with each
	 * vertex is the 3 component unit vector that defines the position of the
	 * vertex.
	 * <p>
	 * Users should not modify the contents of the array.
	 * 
	 * @return a reference to the vertices.
	 */
	public double[][] getVertices()
	{
		return vertices;
	}

	/**
	 * Retrieve an n x 2 array where n is the number of tessellation levels in all the
	 * tessellations that constitute this model. For each tessellation level, the first
	 * element of the int[2] specifies the index of the first triangle in the tess level
	 * and the second element specifies the index of the last triangle on the tess level +
	 * 1.
	 */
	public int[][] getLevels()
	{
		return levels;
	}

	/**
	 * Retrieve the index of the tessellation level in which the specified
	 * triangle resides.  
	 * @param triangle
	 * @return index of a level relative to all levels in all tessellations.
	 */
	public int getLevel(int triangle)
	{
		for (int level=0; level<levels.length; ++level)
			if (triangle < levels[level][1])
				return level;
		return -1;
	}

	/**
	 * Retrieve a reference to all of the tessellations.  
	 * This is an n x 2 array of tessellation level indexes where
	 * n is the number of tessellations. 
	 * @return a reference to all of the tessellations
	 */
	public int[][] getTessellations()
	{
		return tessellations;
	}

	/**
	 * Retrieve the index of the tessellation in which the specified
	 * tessellation level resides.
	 * @param level index of a level relative to all levels in all tessellations.
	 * @return a tessellation index
	 */
	public int getTessellationIndex(int level)
	{
		for (int tess=0; tess<tessellations.length; ++tess)
			if (level < tessellations[tess][1])
				return tess;
		return -1;		
	}

	/**
	 * Retrieve a set containing the indexes of all the vertices that are
	 * connected together by triangles on the specified level.
	 * 
	 * @param tessId the tessellation index
	 * @param level index of a level relative to the first level of the 
	 * specified tessellation.
	 * @return the set containing the indexes of all the vertices that are
	 *         connected together by triangles on the specified level.
	 */
	public HashSetInteger getVertexIndices(int tessId, int level)
	{ return getVertexIndices(tessellations[tessId][0]+level); }

	/**
	 * Retrieve a set containing the indexes of all the vertices that are
	 * connected together by triangles on the specified level.
	 * 
	 * @param level
	 *            index of a level relative to all levels in all tessellations.
	 * @return the set containing the indexes of all the vertices that are
	 *         connected together by triangles on the specified level.
	 */
	public HashSetInteger getVertexIndices(int level)
	{
		if (connectedVertices == null || connectedVertices[level] == null)
		{
			synchronized(connectedVerticesLock)
			{
				if (connectedVertices == null)
					connectedVertices = new HashSetInteger[levels.length];
				connectedVertices[level] = new HashSetInteger();

				HashSetInteger v = connectedVertices[level];
				int[] triangle;
				for (int i = levels[level][0]; i < levels[level][1]; ++i)
				{
					triangle = triangles[i];
					v.add(triangle[0]);
					v.add(triangle[1]);
					v.add(triangle[2]);
				}
			}		

		}
		return connectedVertices[level];
	}

	/**
	 * Retrieve a set containing the indexes of all the vertices that are
	 * connected together by triangles on the top level of the specified 
	 * tessellation.
	 * 
	 * @param tessId
	 *            tessellation index
	 * @return the set containing the indexes of all the vertices that are
	 * connected together by triangles on the top level of the specified 
	 * tessellation.
	 */
	public HashSetInteger getVertexIndicesTopLevel(int tessId)
	{ return getVertexIndices(getLastLevel(tessId)); }

	/**
	 * Retrieve a set containing the unit vectors of all the vertices that are
	 * connected together by triangles on the specified level.
	 * 
	 * @param tessId the tessellation index
	 * @param level index of a level relative to the first level of the 
	 * specified tessellation.
	 * @return the set containing the unit vectors of all the vertices that are
	 * connected together by triangles on the specified level.
	 */
	public HashSet<double[]> getVertices(int tessId, int level)
	{ return getVertices(tessellations[tessId][0]+level); }

	/**
	 * Retrieve a set containing the unit vectors of all the vertices that are
	 * connected together by triangles on the specified level.
	 * 
	 * @param level
	 *            index of a level relative to all levels of all tessellations
	 * @return the set containing the unit vectors of all the vertices that are
	 * connected together by triangles on the specified level.
	 */
	public HashSet<double[]> getVertices(int level)
	{
		HashSetInteger indices = getVertexIndices(level);

		HashSet<double[]> vectors = new HashSet<double[]>(indices.size());

		Iterator it = indices.iterator();
		while (it.hasNext())
			vectors.add(vertices[it.next()]);
		return vectors;
	}

	/**
	 * Retrieve a set containing the unit vectors of all the vertices that are
	 * connected together by triangles on the top level of the specified 
	 * tessellation.
	 * 
	 * @param tessId tessellation index
	 * @return the set containing the unit vectors of all the vertices that are
	 * connected together by triangles on the top level of the specified 
	 * tessellation.
	 */
	public HashSet<double[]> getVerticesTopLevel(int tessId)
	{
		return getVertices(getLastLevel(tessId));
	}

	/**
	 * Return the number of Vertices in the model.
	 * 
	 * @return int
	 */
	public int getNVertices()
	{
		return vertices.length;
	}

	/**
	 * Retrieve total number of multi-level tessellations defined in the model
	 * 
	 * @return total number of multi-level tessellations defined in the model
	 */
	public int getNTessellations()
	{
		return tessellations.length;
	}

	/**
	 * Retrieve the total number of tessellation levels, including all levels of
	 * all tessellations.
	 * 
	 * @return the total number of tessellation levels, including all levels of
	 *         all tessellations.
	 */
	public int getNLevels()
	{
		return levels.length;
	}

	/**
	 * Retrieve number of tessellation levels that define the specified
	 * multi-level tessellation of the model.
	 * 
	 * @param tessId tessellation index
	 * @return number of levels that comprise the specified tessellation.
	 */
	public int getNLevels(int tessId) { return tessellations[tessId][1] - tessellations[tessId][0]; }

	/**
	 * Retrieve the index of one of the levels on the specified tessellation
	 * 
	 * <p>Levels for all tessellations are stored internally in a single array of level indices.
	 * In some instances, the index of a level relative to all the levels in all tessellations
	 * is needed. In other instances, the index of a level relative to the first level of
	 * a specified tessellation is needed.  Users need to ensure they specify the correct 
	 * level index, depending on the circumstances.
	 * 
	 * @param tessId tessellation index
	 * @param level index of a level relative to the first level of the specified
	 *            tessellation
	 * @return the index of the i'th level on the specified tessellation,
	 *         relative to the list of all levels in all tessellations.
	 */
	public int getLevel(int tessId, int level) { return tessellations[tessId][0] + level; }

	/**
	 * Retrieve the index of the last level on the specified tessellation, relative to all 
	 * levels in all tessellations.
	 * 
	 * <p>Levels for all tessellations are stored internally in a single array of level indices.
	 * In some instances, the index of a level relative to all the levels in all tessellations
	 * is needed. Use this method, getLastLevel(tessId), to retrieve this index.
	 * In other instances, the index of a level relative to the first level of
	 * a specified tessellation is needed.  Use method getTopLevel(tessid) to retrieve that
	 * index.
	 * 
	 * @param tessellation
	 * @return the index of the last level on the specified tessellation
	 * relative to all levels of all tessellations.
	 */
	public int getLastLevel(int tessellation) { return tessellations[tessellation][1] - 1; }

	/**
	 * Retrieve the index of the last level on the specified tessellation, relative to first 
	 * level of the specified tessellation.
	 * 
	 * <p>Levels for all tessellations are stored internally in a single array of level indices.
	 * In some instances, the index of a level relative to the first level of
	 * a specified tessellation is needed.  Use this method, getTopLevel(tessId), to retrieve this index.
	 * In other instances, the index of a level relative to all the levels in all tessellations
	 * is needed.  Use method getLastLevel(tessid) to retrieve that index. 
	 * 
	 * @param tessellation
	 * @return the index of the last level on the specified tessellation
	 * relative to first level of the tessellation.
	 */
	public int getTopLevel(int tessellation)
	{ return tessellations[tessellation][1] - tessellations[tessellation][0] - 1; }

	/**
	 * Retrieve the total number of triangles including those on all levels of
	 * all tessellations.
	 * 
	 * @return the total number of triangles including those on all levels of
	 *         all tessellations.
	 */
	public int getNTriangles()
	{
		return triangles.length;
	}

	/**
	 * Retrieve the number of triangles that define the specified level of the
	 * specified multi-level tessellation of the model.
	 * 
	 * @param tessId
	 *            tessellation index
	 * @param level
	 *            index of a level relative to the first level of the specified
	 *            tessellation
	 * @return number of triangles on specified tessellation and level.
	 */
	public int getNTriangles(int tessId, int level)
	{
		return levels[tessellations[tessId][0] + level][1]
				- levels[tessellations[tessId][0] + level][0];
	}

	/**
	 * Retrieve the index of the i'th triangle on the specified level of the
	 * specified tessellation of the model.
	 * 
	 * @param tessId
	 *            tessellation index
	 * @param level
	 *            index of a level relative to the first level of the specified
	 *            tessellation
	 * @param triangle
	 *            the i'th triangle in the specified tessellation/level
	 * @return a triangle index
	 */
	public int getTriangle(int tessId, int level, int triangle)
	{
		return levels[tessellations[tessId][0] + level][0] + triangle;
	}

	/**
	 * Retrieve the index of the first triangle on the specified level of the
	 * specified tessellation of the model.
	 * 
	 * @param tessId
	 *            tessellation index
	 * @param level
	 *            index of a level relative to the first level of the specified
	 *            tessellation
	 * @return a triangle index
	 * @since 2.0
	 */
	public int getFirstTriangle(int tessId, int level)
	{
		return levels[tessellations[tessId][0] + level][0];
	}

	/**
	 * Retrieve the index of the last triangle on the specified level of the
	 * specified tessellation of the model.
	 * 
	 * @param tessId
	 *            tessellation index
	 * @param level
	 *            index of a level relative to the first level of the specified
	 *            tessellation
	 * @return a triangle index
	 */
	public int getLastTriangle(int tessId, int level)
	{
		return levels[tessellations[tessId][0] + level][1] - 1;
	}

	/**
	 * Retrieve a reference to the nTriangles x 3 array of int that specifies
	 * the indexes of the 3 vertices that define each triangle of the
	 * tessellation.
	 * <p>
	 * Users should not modify the contents of the array.
	 * 
	 * @return a reference to the triangles.
	 */
	public int[][] getTriangles()
	{
		return triangles;
	}

	/**
	 * Retrieve an int[3] array containing the indexes of the vertices that form
	 * the corners of the triangle with index triangleIndex.
	 * <p>
	 * Users should not modify the contents of the array.
	 * 
	 * @param triangleIndex
	 *            triangleIndex
	 * @return an int[3] array containing the indexes of the vertices that form
	 *         the corners of the specified triangle.
	 */
	public int[] getTriangleVertexIndexes(int triangleIndex)
	{
		return triangles[triangleIndex];
	}

	/**
	 * Retrieve the index of the i'th vertex (0..2) that represents one of the
	 * corners of the specified triangle.
	 * 
	 * @param triangleIndex
	 *            triangleIndex
	 * @param cornerIndex
	 *            0..2
	 * @return the index of the vertex at the specified corner of the specified
	 *         triangle
	 */
	public int getTriangleVertexIndex(int triangleIndex, int cornerIndex)
	{
		return triangles[triangleIndex][cornerIndex];
	}

	/**
	 * Retrieve the unit vector of the vertex located at one of the corners of
	 * the specified triangle.
	 * 
	 * @param triangleIndex
	 *            triangleIndex
	 * @param cornerIndex
	 *            0..2
	 * @return the unit vector of the vertex at the specified corner of the
	 *         specified triangle
	 */
	public double[] getTriangleVertex(int triangleIndex, int cornerIndex)
	{
		return vertices[triangles[triangleIndex][cornerIndex]];
	}

	/**
	 * Get the 3 verteces that form the corners of the specified triangle, in
	 * clockwise order.
	 * 
	 * @param triangle
	 *            index of the desired triangle
	 * @return 3 x 3 array of doubles with the unit vectors that define the 3
	 *         corners of the specified triangle.
	 */
	public double[][] getTriangleVertices(int triangle)
	{
		int[] corners = triangles[triangle];
		return new double[][] { vertices[corners[0]], vertices[corners[1]],
				vertices[corners[2]] };
	}

	/**
	 * Compute the circumcenters of all triangles if they have not already
	 * been computed.  This function is called from the GeoTessPositionNaturalNeighbor
	 * constructor.  The first 3 elements are the unit vector and
	 * the fourth is the cos(circumCircle radius).
	 */
	public void computeCircumCenters()
	{
		synchronized(circumCenterLock)
		{
			if (circumCenters == null)
			{
				circumCenters = new double[triangles.length][];
				for (int triangle=0; triangle<triangles.length; ++triangle)
				{
					int[] corners = triangles[triangle];
					circumCenters[triangle] = GeoTessUtils.circumCenterPlus(
							vertices[corners[0]], vertices[corners[1]], vertices[corners[2]]);
				}
			}
		}
	}

	/**
	 * Retrieve the circumCenter of the specified triangle. The circumCenter of
	 * a triangle is the center of the circle that has all three corners of the
	 * triangle on its circumference. Lazy evaluation is implemented to load
	 * these. The first 3 elements are the unit vector and
	 * the fourth is the cos(circumCircle radius).
	 * 
	 * @param triangle
	 * @return unit vector that defines circumCenter. Fourth element is the 
	 * cosine of the radius of the circumCircle.
	 */
	public double[] getCircumCenter(int triangle) { return circumCenters[triangle]; }

	/**
	 * Copy the circumCenter of the specified triangle. The circumCenter of
	 * a triangle is the center of the circle that has all three corners of the
	 * triangle on its circumference. Lazy evaluation is implemented to load
	 * these.
	 * 
	 * <p>The fourth element is the dot product of the new circumcenter 
	 * with one of the vertices.  In other words, cc[3] = cos(ccRadius).
	 * 
	 * @param triangle
	 * @param circumCenter the 4-element array into which the circumcenter will 
	 * be copied.  The first 3 elements will contain the unit vector that defines circumCenter
	 * and the fourth element will contain the cosine of the radius of the circumCircle.
	 */
	public void getCircumCenter(int triangle, double[] circumCenter)
	{
		double[] c = circumCenters[triangle];
		circumCenter[0] = c[0];
		circumCenter[1] = c[1];
		circumCenter[2] = c[2];
		circumCenter[3] = c[3];
	}

	/**
	 * Retrieve the index of one of the triangles that is a neighbor of the
	 * specified triangle. A triangle has at least 3 neighbors and usually has
	 * 4. For triangle T, neighbors 0, 1, and 2 reside on the same tessellation
	 * level as T and refer to the triangles that share an edge with T. If T has
	 * a fourth neighbor it is a descendent of T and resides on the next higher
	 * tessellation level relative to T. In other words, neighbor(3) is one of
	 * the triangles into which T was subdivided when the tessellation was
	 * constructed. If T does not have a descendant, then getNeighbor(3) will
	 * return -1. getNeighbor(i) will always return a valid triangle index for
	 * i=[0,1,2] but may or may not return a valid triangle index for i=3.
	 * 
	 * @param triangleIndex
	 *            index of the triangle whose neighbor is desired.
	 * @param neighborIndex
	 *            (0..3)
	 * @return int index of the triangle that is a neighbor of triangle.
	 */
	public int getNeighbor(int triangleIndex, int neighborIndex)
	{
		return neighborIndex == 3 ? descendants[triangleIndex] 
				: edgeList[triangleIndex][neighborIndex].tLeft;
	}

	/**
	 * Retrieve the indexes of the triangles that are neighbors of the specified
	 * triangle. A triangle has at least 3 neighbors and usually has 4. For
	 * triangle T, neighbors 0, 1, and 2 reside on the same tessellation level
	 * as T and refer to the triangles that share an edge with T. If T has a
	 * fourth neighbor it is a descendent of T and resides on the next higher
	 * tessellation level relative to T. In other words, neighbor(3) is one of
	 * the triangles into which T was subdivided when the tessellation was
	 * constructed. If T does not have a descendant, then getNeighbor(3) will
	 * return -1. getNeighbor(i) will always return a valid triangle index for
	 * i=[0,1,2] but may or may not return a valid triangle index for i=3.
	 * 
	 * @param triangleIndex
	 *            index of the triangle whose neighbors are desired.
	 * @return int[] indexes of the triangles that are neighbors of triangle.
	 */
	public int[] getNeighbors(int triangleIndex)
	{
		Edge[] e = edgeList[triangleIndex];
		return new int[] { e[0].tLeft, e[1].tLeft, e[2].tLeft, 
				descendants[triangleIndex]};
	}

	/**
	 * Retrieve the indexes of the triangles that are neighbors of the specified
	 * triangle. A triangle has at least 3 neighbors and usually has 4. For
	 * triangle T, neighbors 0, 1, and 2 reside on the same tessellation level
	 * as T and refer to the triangles that share an edge with T. If T has a
	 * fourth neighbor it is a descendent of T and resides on the next higher
	 * tessellation level relative to T. In other words, neighbor(3) is one of
	 * the triangles into which T was subdivided when the tessellation was
	 * constructed. If T does not have a descendant, then getNeighbor(3) will
	 * return -1. getNeighbor(i) will always return a valid triangle index for
	 * i=[0,1,2] but may or may not return a valid triangle index for i=3.
	 * 
	 * @param tessellation
	 *            tessellation index
	 * @param level
	 *            index of a level relative to the first level of the specified
	 *            tessellation
	 * @param triangle
	 *            the i'th triangle in the specified tessellation/level
	 * @return the indexes of the triangles that are neighbors of the specified
	 *         triangle.
	 */
	public int[] getNeighbors(int tessellation, int level, int triangle)
	{ return getNeighbors(getTriangle(tessellation, level, triangle)); }

	/**
	 * Retrieve the index of the triangle that is the i'th neighbor of the
	 * specified triangle. A triangle has at least 3 neighbors and usually has
	 * 4. For triangle T, neighbors 0, 1, and 2 reside on the same tessellation
	 * level as T and refer to the triangles that share an edge with T. If T has
	 * a fourth neighbor it is a descendent of T and resides on the next higher
	 * tessellation level relative to T. In other words, neighbor(3) is one of
	 * the triangles into which T was subdivided when the tessellation was
	 * constructed. If T does not have a descendant, then getNeighbor(3) will
	 * return -1. getNeighbor(i) will always return a valid triangle index for
	 * i=[0,1,2] but may or may not return a valid triangle index for i=3.
	 * 
	 * @param tessellation
	 *            tessellation index
	 * @param level
	 *            index of a level relative to the first level of the specified
	 *            tessellation
	 * @param triangle
	 *            the i'th triangle in the specified tessellation/level
	 * @param side
	 *            the index of the triangle side (0..2)
	 * @return the index of the triangle that is the i'th neighbor of the
	 *         specified triangle.
	 */
	public int getNeighbor(int tessellation, int level, int triangle, int side)
	{ return getNeighbor(getTriangle(tessellation, level, triangle), side); }

	/**
	 * If triangle with index tid has a neighbor with index nid, then return the
	 * index of nid in tid's neighbor array.
	 * <p>
	 * In other words, if triangle nid is a neighbor of triangle tid, i.e.,
	 * neighbors[tid][i] == nid, then this method returns i.
	 * 
	 * @param tid the index of a triangle
	 * @param nid the index of another triangle
	 * @return the index of nid in tid's array of neighbors.
	 */
	public int getNeighborIndex(int tid, int nid)
	{
		Edge[] e = edgeList[tid];
		if (e[0].tLeft == nid) return 0;
		if (e[1].tLeft == nid) return 1;
		if (e[2].tLeft == nid) return 2;
		return -1;
	}

	/**
	 * An nTriangles x 3 array of Edges.
	 * @return
	 */
	protected Edge[][] getEdgeList() { return edgeList; }

	protected Edge[] getSpokeList(int level) 
	{ computeSpokeLists(level); return spokeList[level]; }

	public int[] getDescendants() { return descendants; }

	public int getDescendant(int triangle) { return descendants[triangle]; }

	public int getDescendant(int tessId, int level, int triangle)
	{ return descendants[getTriangle(tessId, level, triangle)]; }

	/**
	 * @return summary information about this GeoTessGrid object.
	 */
	@Override
	public String toString()
	{
		StringBuffer buf = new StringBuffer();
		buf.append(String.format("GeoTessGrid:%n"));
		buf.append(String.format("gridID = %s%n", gridID));
		buf.append(String.format(
				"Input Grid  File : %s%ngenerated by %s, %s%n",
				gridInputFile == null ? "null" : gridInputFile,
						gridSoftwareVersion, gridGenerationDate));
		if (gridOutputFile != null)
			buf.append(String.format("Output Grid File : %s%n", gridOutputFile));

		buf.append("\n");

		buf.append(String
				.format("nTessellations  =%8d%n", tessellations.length));
		buf.append(String.format("nLevels         =%8d%n", levels.length));
		buf.append(String.format("nVertices       =%8d%n", vertices.length));
		buf.append(String.format("nTriangles      =%8d%n", triangles.length));
		buf.append(String.format("%n"));

		buf.append(String.format(
				"    Tess    Level  LevelID EdgeLength  NVertices  NTriangles    First   Last+1%n"));
		for (int tess = 0; tess < getNTessellations(); ++tess)
		{
			for (int level = 0; level < getNLevels(tess); ++level)
			{
				// when there is only one tesselllation and it has only one level,
				// the triangles sizes are likely variable.
				String edgeLength = levels.length == 1 ? "  variable" :
					String.format("%10.4f", GeoTessUtils.getEdgeLength(level));

				buf.append(String.format("%8d %8d %8d %10s %10d %11d %8d %8d%n", tess,
						level, tessellations[tess][0] + level,
						edgeLength,
						getVertexIndices(getLevel(tess, level)).size(),
						getNTriangles(tess, level),
						getTriangle(tess, level, 0),
						getTriangle(tess, level, getNTriangles(tess, level))));
			}
			buf.append(String.format("%n"));
		}
		return buf.toString();
	}

	public String toStringTriangle(int t)
	{
		StringBuffer buf = new StringBuffer();
		buf.append(String.format("Triangle %d%n", t));
		for (int i=0; i<3; ++i)
		{
			int v = triangles[t][i];
			buf.append(String.format("Node %6d lat-lon=%s neighbor=%6d%n", 
					v, EarthShape.WGS84.getLatLonString(vertices[v]),
					edgeList[t][i].tLeft));
		}

		for (int i=0; i<3; ++i)
		{
			int v = triangles[t][i];
			int v0 = triangles[t][(i+1)%3];
			int v1 = triangles[t][(i+2)%3];
			buf.append(String.format("Edge %6d node=%6d node=%6d dist=%6.2f az=%7.2f tleft=%6d tright=%6d%n", 
					v, v0, v1, 
					GeoTessUtils.angleDegrees(vertices[v0], vertices[v1]),
					GeoTessUtils.azimuthDegrees(vertices[v0], vertices[v1], Double.NaN),
					edgeList[t][i].tLeft, t));
		}
		buf.append(String.format("Center=%s,  Circumcenter=%s%n", 
				EarthShape.WGS84.getLatLonString(GeoTessUtils.center(getTriangleVertices(t))),
				EarthShape.WGS84.getLatLonString(getCircumCenter(t))));
		return buf.toString();
	}

	/**
	 * Perform walking triangle search to find the index of the triangle that
	 * contains position defined by vector and which has no descendant 
	 * and whose level index is less than or equal to supplied level index.
	 * 
	 * @param triangleIndex
	 *            the index of a triangle from which to start the search.
	 * @param vector
	 *            the unit vector representing the position for which to search.
	 * @param level index of a level relative to all levels in all tessellations.
	 * @return the index of the triangle containing the specified position.
	 */
	public int getTriangle(int triangleIndex, double[] vector, int level)
	{
		int currentLevel = getLevel(triangleIndex);

		while (true)
		{
			if (GeoTessUtils.dot(edgeList[triangleIndex][0].normal, vector) > -1e-15)
			{
				if (GeoTessUtils.dot(edgeList[triangleIndex][1].normal, vector) > -1e-15)
				{
					if (GeoTessUtils.dot(edgeList[triangleIndex][2].normal, vector) > -1e-15)
					{
						if (currentLevel == level || descendants[triangleIndex] < 0)
							return triangleIndex;
						else
						{
							triangleIndex = descendants[triangleIndex];
							++currentLevel;
						}
					}
					else
						triangleIndex = edgeList[triangleIndex][2].tLeft;
				}
				else
					triangleIndex = edgeList[triangleIndex][1].tLeft;
			}
			else
				triangleIndex = edgeList[triangleIndex][0].tLeft;
		}
	}

	/**
	 * Retrieve a list of the triangles a particular vertex is a member of,
	 * considering only triangles in the specified tessellation/level.
	 * <p>
	 * Lazy evaluation is used here. The list of indexes is initially empty and
	 * is computed and stored on demand. Once computed the indexes remain in
	 * memory for the next time they might be called.
	 * 
	 * @param tessId
	 *            tessellation index
	 * @param level
	 *            index of a level relative to the first level of the specified
	 *            tessellation
	 * @param vertex
	 * @return list of triangle indeces
	 */
	public ArrayListInt getVertexTriangles(int tessId, int level, int vertex)
	{
		return vtxTriangles[tessellations[tessId][0] + level][vertex];
	}

	/**
	 * Retrieve a list of the triangles a particular vertex is a member of,
	 * considering only triangles in the top level of the specified tessellation.
	 * <p>
	 * Lazy evaluation is used here. The list of indexes is initially empty and
	 * is computed and stored on demand. Once computed the indexes remain in
	 * memory for the next time they might be called.
	 * 
	 * @param tessId
	 *            tessellation index
	 * @param vertex
	 * @return list of triangle indeces
	 */
	public ArrayListInt getVertexTriangles(int tessId, int vertex)
	{
		return getVertexTriangles(tessId, getNLevels(tessId)-1, vertex);
	}

	/**
	 * Retrieve a list of the indexes of all the vertexes that are connected to
	 * the specified vertex by a single edge, considering only triangles in the
	 * specified tessellation and level. The vertices will be arranged in
	 * clockwise order when viewed from outside the unit sphere.
	 * 
	 * @param tessId
	 *            tessellation index
	 * @param level
	 *            index of a level relative to the first level of the specified
	 *            tessellation
	 * @param vertex
	 * @return list of vertex indices in clockwise order.
	 * @throws GeoTessException
	 */
	public int[] getVertexNeighborsOrdered(int tessId, int level, int vertex)
			throws GeoTessException
	{
		int lvl = getLevel(tessId, level);

		computeSpokeLists(lvl);
		Edge head = spokeList[lvl][vertex];
		if (head == null)
			return new int[0];

		int n=0;
		Edge spoke = head;
		do { ++n; spoke = spoke.next; } while (spoke != head);
		int[] neighbors = new int[n];
		for (int i=0; i<n; ++i)
		{ neighbors[i] = spoke.vk; spoke=spoke.next; }

		return neighbors;
	}

	/**
	 * Retrieve a list of the indexes of all the vertexes that are within a
	 * neighborhood of the specified vertex, excluding the specified vertex. 
	 * The neighborhood is defined by the
	 * argument "order". If order is 1, then all the vertices that are connected
	 * by a single edge to vertex are included. If order is 2, then take the
	 * order 1 neighborhood and add all the vertices that are connected to any
	 * vertex in the order-1 neighborhood by a single edge. Keep doing that to
	 * as high order as desired. Only triangles in the specified tessellation
	 * and level are considered.
	 * 
	 * @param tessId
	 *            tessellation index
	 * @param level
	 *            index of a level relative to the first level of the specified
	 *            tessellation
	 * @param vertex
	 * @param order
	 * @return set of vertex indices, excluding the supplied vertex.
	 */
	public HashSet<Integer> getVertexNeighbors(int tessId, int level, int vertex,
			int order)
			{
		HashSet<Integer> neighbors = new HashSet<Integer>();
		HashSetInteger temp = new HashSetInteger();
		Iterator it;

		neighbors.add(vertex);

		for (int o = 0; o < order; ++o)
		{
			for (Integer index : neighbors)
			{
				ArrayListInt tneighbors = getVertexTriangles(tessId, level, index);
				for (int i = 0; i < tneighbors.size(); ++i)
				{
					int t = tneighbors.get(i);
					temp.add(getTriangleVertexIndex(t, 0));
					temp.add(getTriangleVertexIndex(t, 1));
					temp.add(getTriangleVertexIndex(t, 2));
				}
			}
			it = temp.iterator();
			while (it.hasNext())
				neighbors.add(it.next());
			temp.clear();
		}
		neighbors.remove(vertex);
		return neighbors;
			}

	/**
	 * Retrieve a list of the indexes of all the vertexes that are connected by
	 * a single edge with the specified vertex. Only triangles in the specified
	 * tessellation and level are considered.
	 * 
	 * @param tessId
	 *            tessellation index
	 * @param level
	 *            index of a level relative to the first level of the specified
	 *            tessellation
	 * @param vertex
	 * @return set of vertex indices.  May be emty, but will not be null.
	 */
	public HashSet<Integer> getVertexNeighbors(int tessId, int level, int vertex)
	{
		HashSet<Integer> neighbors = new HashSet<Integer>();

		ArrayListInt tneighbors = getVertexTriangles(tessId, level, vertex);

		for (int i = 0; i < tneighbors.size(); ++i)
		{
			int t = tneighbors.get(i);
			neighbors.add(getTriangleVertexIndex(t, 0));
			neighbors.add(getTriangleVertexIndex(t, 1));
			neighbors.add(getTriangleVertexIndex(t, 2));
		}
		neighbors.remove(vertex);
		return neighbors;
	}

	/**
	 * Given an arbitrary triangle, T, in the grid, and some specified tessid and 
	 * levelid, find all the indexes of all the triangles that are completely
	 * enclosed by T on the specified level.
	 * @param triangle 
	 * @param tessid
	 * @return indices of triangles enclosed by the input triangle
	 */
	public HashSet<Integer> findEnclosedTriangles(int triangle, int tessid)
	{
		HashSet<Integer> results = new HashSet<Integer>();

		// first find the triangle at top level of tessid that contains the center of
		// triangle.
		int tcenter = getTriangle(getTriangle(tessid,0, 0), GeoTessUtils.center(getTriangleVertices(triangle)));

		searchNeighborhood(triangle, tcenter, results);

		return results;
	}

	/**
	 * Retrieve a list of all the edges of triangles that reside on 
	 * the top level of the specified tessellation.  An edge is defined by 
	 * the indices of two vertices.
	 * @param tessId tessellation index
	 * @return a list of pairs of vertex indices that define triangle edges.
	 */
	public ArrayList<int[]> getEdges(int tessId)
	{ return getEdges(tessId, getNLevels(tessId)-1); }

	/**
	 * Retrieve a list of all the edges of triangles that reside on 
	 * the specified tessellation and level.  An edge is defined by 
	 * the indices of two vertices.
	 * @param tessId tessellation index
	 * @param levelId index of a level relative to the first level of the specified
	 *            tessellation
	 * @return a list of pairs of vertex indices that define triangle edges.
	 */
	public ArrayList<int[]> getEdges(int tessId, int levelId)
	{
		HashSetInteger vSet =  getVertexIndices(getLevel(tessId, levelId));

		ArrayList<int[]> edges = new ArrayList<int[]>(vSet.size());

		HashSetInteger satisfied = new HashSetInteger(vertices.length);

		int vertex;
		HashSet<Integer> neighbors;
		Iterator it = vSet.iterator();
		while (it.hasNext())
		{
			vertex = it.next();
			neighbors = getVertexNeighbors(tessId, levelId, vertex);
			for (Integer neighbor : neighbors)
				if (!satisfied.contains(neighbor))
					edges.add(new int[] {vertex, neighbor.intValue()});
			satisfied.add(vertex);
		}
		return edges;
	}

	/**
	 * Search for triangles whose centers are enclosed by triangle1. 
	 * Recursively searches neighbors of triangles that are contained by
	 * triangle1, starting from triangle2.
	 * @param triangle1
	 * @param triangle2
	 * @param tset
	 */
	private void searchNeighborhood(int triangle1, int triangle2, HashSet<Integer> tset)
	{
		if (tset.contains(triangle2))
			return;

		if (getTriangle(triangle1, GeoTessUtils.center(getTriangleVertices(triangle2))) == triangle1)
		{
			tset.add(triangle2);
			for (int n = 0; n<3; ++n)
				searchNeighborhood(triangle1, edgeList[triangle2][n].tLeft, tset);
		}
	}
	
	/**
	 * Rotate this grid using a set of three Euler rotation angles.
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
	 * @param inDegrees if true, the euler rotations are assumed to be in degrees.
	 * If false, they are assumed to be in radians.
	 * @param a1 angle1
	 * @param a2 angle2
	 * @param a3 angle3
	 * @return returns a reference to this.
	 */
	public GeoTessGrid eulerRotation(boolean inDegrees, double a1, double a2, double a3)
	{
		if (inDegrees)
		{
			a1 = Math.toRadians(a1);
			a2 = Math.toRadians(a2);
			a3 = Math.toRadians(a3);
		}
		
		double[][] eulerMatrix = VectorUnit.getEulerMatrix(a1, a2, a3);
		for (int i=0; i<vertices.length; ++i)
			VectorUnit.eulerRotation(vertices[i], eulerMatrix, vertices[i]);
		
		initialize();
		recomputeGridID();
		return this;
	}

	/**
	 * Identify the neighbors and descendants of each triangle. This method is
	 * called during construction of a GeoTessGrid object (i.e., when it is
	 * loaded from a file). Applications should not call this method.
	 * 
	 * <p>
	 * If optimization is set to SPEED, then for each edge of a triangle the
	 * unit vector normal to the plane of the great circle containing the edge
	 * will be computed during input of the grid from file and stored in memory.
	 * With this information, the walking triangle algorithm can use dot
	 * products instead of scalar triple products when determining if a point
	 * resides inside a triangle. While much more computationally efficient, it
	 * requires alot of memory to store all those unit vectors.
	 * 
	 * @param optimization
	 * @return the neighbors of each triangle.
	 */
	protected void initialize()
	{
		vtxTriangles = new ArrayListInt[levels.length][vertices.length];
		ArrayListInt[] vtxT;

		for (int level=0; level < levels.length; ++level)
		{
			vtxT = vtxTriangles[level];
			for (int vertex=0; vertex<vertices.length; ++vertex)
				vtxT[vertex] =  new ArrayListInt(6);

			for (int t = levels[level][0]; t < levels[level][1]; ++t)
				for (int c = 0; c < 3; ++c)
				{
					int v = triangles[t][c];

					// Add the index of triangle t to the list of triangles that
					// vertex belongs to.
					vtxT[v].add(t);
				}
		}

		int[][] neighbors = new int[triangles.length][3];

		// First: find the 3 neighbors of each triangle.
		// This is an implementation of the "Triangle neighbor identification"
		// algorithm in Ballard, Hipp and Young, 2009,
		// Efficient and Accurate Calculation of Ray Theory Seismic Travel
		// Time through Variable Resolution 3D Earth Models, SRL, 80, 989-999.

		int n, c, vj, vk;
		int[] corners;

		boolean[] marked = new boolean[triangles.length]; // initial values are
		// all false

		// loop over all the levels of all tessellations
		for (int tess = 0; tess < tessellations.length; ++tess)
			for (int level = tessellations[tess][0]; level < tessellations[tess][1]; ++level)
			{
				vtxT = vtxTriangles[level];
				for (int t=levels[level][0]; t<levels[level][1]; ++t)
				{

					corners = triangles[t];

					// t is the index of a triangle and corners are the indexes
					// of the 3 vertices that reside at the corners of triangle
					// t.
					// Loop over corners of triangle t
					for (c = 0; c < 3; ++c)
					{
						vj = corners[(c + 1) % 3];
						vk = corners[(c + 2) % 3];
						// c is the index of a corner of triangle t (c is one of
						// 0,1,2).
						// vj is the index of the vertex (not corner) that is
						// found by
						// moving clockwise around t from corner c.
						// vk is the index of the vertex (not corner) that is
						// found by
						// moving clockwise around t from vj

						// indexes of the triangles of which vj is a member
						ArrayListInt tj = vtxT[vj];  

						// indexes of the triangles of which vk is a member
						ArrayListInt tk = vtxT[vk];

						// mark all the triangles of which vertex vj is a member
						for (n = 0; n < tj.size(); ++n)
							marked[tj.get(n)] = true;

						// loop over all the triangles of which vk is a member.
						// Two of them will be marked. One of the ones that is
						// marked is triangle t. The other one is the triangle
						// that resides on the other side of the edge that
						// connects
						// vertices vj and vk. That second triangle is the
						// neighbor
						// of triangle t.
						for (n = 0; n < tk.size(); ++n)
							if (marked[tk.get(n)] && tk.get(n) != t)
							{
								neighbors[t][c] = tk.get(n);
								break;
							}

						// unmark all the triangles that were recently marked.
						for (n = 0; n < tj.size(); ++n)
							marked[tj.get(n)] = false;
					}
				}
			}
		// done with marked
		marked = null;

		// compute the Edges for all the triangles.
		edgeList = new Edge[triangles.length][3];
		Edge edge;
		for (int triangle = 0; triangle < triangles.length; ++triangle)
		{
			Edge[] tedges = edgeList[triangle];

			int[] vtxIds = triangles[triangle];
			int[] nbrs = neighbors[triangle];

			for (int i=0; i<3; ++i)
			{
				int j=(i+1)%3;
				int k=(j+1)%3;
				edge = new Edge();
				edge.vj = vtxIds[j];
				edge.vk = vtxIds[k];
				edge.tRight = triangle;
				edge.tLeft = nbrs[i];
				
				if (triangles[nbrs[i]][0] == vtxIds[j]) edge.cornerj = 0;
				else if (triangles[nbrs[i]][1] == vtxIds[j]) edge.cornerj = 1;
				else if (triangles[nbrs[i]][2] == vtxIds[j]) edge.cornerj = 2;
				else edge.cornerj = -1;
				
				GeoTessUtils.cross(vertices[edge.vk], vertices[edge.vj], edge.normal);
				edge.next = null;
				tedges[i] = edge;
			}
		}
		spokeList=new Edge[levels.length][];

		neighbors = null;

		// find the descendant of each triangle at the next higher
		// tessellation level.

		// initialize all the descendants to -1.
		descendants = new int[triangles.length];
		Arrays.fill(descendants, -1);

		double len;
		double[] v0, v1, v2, x = new double[3];
		
		// loop over all but the last level of each tessellations. The
		// descendants of elements
		// on the last level of each tessellation will remain -1.
		for (int tess = 0; tess < tessellations.length; ++tess)
			for (int level=tessellations[tess][0]; level < tessellations[tess][1]-1; ++level)
			{
				int startTriangle = levels[level+1][0];

				for (int t=levels[level][0]; t<levels[level][1]; ++t)
				{
					corners = triangles[t];
					v0 = vertices[corners[0]];
					v1 = vertices[corners[1]];
					v2 = vertices[corners[2]];
					// set x.vector to a unit vector at center of triangle t
					x[0] = v0[0] + v1[0] + v2[0];
					x[1] = v0[1] + v1[1] + v2[1];
					x[2] = v0[2] + v1[2] + v2[2];
					len = Math.sqrt(x[0] * x[0] + x[1] * x[1] + x[2] * x[2]);
					x[0] /= len;
					x[1] /= len;
					x[2] /= len;
					
					//if (GeoTessUtils.getTriangleArea(v0, v1, v2) < 2e-6)
					//	throw new RuntimeException("collapsed triangle");
										
					// start from startTriangle, which is on the next higher
					// level from the
					// current triangle, and walk to the triangle that contains
					// x.vector.
					// That is the descendant of the current triangle.
					// Set startTriangle equal to the descendant so we won't
					// have to walk too
					// far when we search for the descendant of the next
					// triangle, which is likely
					// close by (this makes a huge difference).
					descendants[t] = startTriangle = getTriangle(startTriangle, x);
				}
			}
	}

	/**
	 * Perform walking triangle search to find the index of the triangle that
	 * contains position defined by vector and which has no descendant.
	 * 
	 * @param triangleIndex
	 *            the index of a triangle from which to start the search.
	 * @param vector
	 *            the unit vector representing the position for which to search.
	 * @return the index of the triangle containing the specified position.
	 */
	public int getTriangle(int triangleIndex, double[] vector)
	{
		//System.out.print("infinite loop?");
		while (true)
		{
			if (GeoTessUtils.dot(edgeList[triangleIndex][0].normal, vector) > -1e-15)
			{
				if (GeoTessUtils.dot(edgeList[triangleIndex][1].normal, vector) > -1e-15)
				{
					if (GeoTessUtils.dot(edgeList[triangleIndex][2].normal, vector) > -1e-15)
					{
						if (descendants[triangleIndex] < 0)
						{
							//System.out.println(" no");
							return triangleIndex;
						}
						else
							triangleIndex = descendants[triangleIndex];
					}
					else
						triangleIndex = edgeList[triangleIndex][2].tLeft;
				}
				else
					triangleIndex = edgeList[triangleIndex][1].tLeft;
			}
			else
				triangleIndex = edgeList[triangleIndex][0].tLeft;
		}
	}

	/**
	 * Write a GeoTessGrid object to a File. If the extension is 'ascii' the model is
	 * written to an ascii file, otherwise it is written to a binary file.
	 * 
	 * @param outputFile
	 *            the name of the file to which the data should be written.
	 * @throws GeoTessException
	 * @throws IOException
	 */
	public void writeGrid(String outputFile) throws IOException
	{
		if (outputFile.endsWith(".ascii"))
			writeGridAscii(outputFile);
		//		else if (outputFile.endsWith(".nc"))
		//		writeGridNetcdf(outputFile);
		else
			writeGridBinary(outputFile);

		gridOutputFile = outputFile;
	}

	public void writeGridKML(File outputFile, int tessId) throws IOException
	{
		writeGridKML(outputFile, tessId, getNLevels(tessId)-1);
	}

	public void writeGridKML(File outputFile, int tessId, int levelId) throws IOException
	{
		PrintStream output = null;
		ZipOutputStream zoS = null;
		ZipEntry ze = null;

		String name = outputFile.getName();
		char lastChar = name.charAt(name.length()-1);
		boolean zip = lastChar == 'z' || lastChar == 'Z';

		if (zip)
		{
			FileOutputStream fos = new FileOutputStream(outputFile);
			zoS = new ZipOutputStream(fos);     
			name = name.substring(0, name.length()-1).concat(
					lastChar == 'z' ? "l" : "L");
			ze = new ZipEntry(name);
			zoS.putNextEntry(ze);
			output = new PrintStream(zoS); 
		}
		else if (name.toLowerCase().endsWith("kml"))
			output = new PrintStream(outputFile);          
		else
			throw new IOException("\nOutput file must have extension kml or kmz\n" + 
					outputFile.getCanonicalPath());

		output.print("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
				"<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n");

		output.print("<Document>\n");
		output.print(String.format("<name>%s</name>%n", name));
		output.print("<open>0</open>\n");

		for (int[] edge : getEdges(tessId, levelId))
		{
			output.print(String.format("<Placemark><LineString><tessellate>1</tessellate><coordinates> " +
					"%1.6f,%1.6f %1.6f,%1.6f " +
					"</coordinates></LineString></Placemark>%n",
					EarthShape.WGS84.getLonDegrees(vertices[edge[0]]),
					EarthShape.WGS84.getLatDegrees(vertices[edge[0]]),
					EarthShape.WGS84.getLonDegrees(vertices[edge[1]]),
					EarthShape.WGS84.getLatDegrees(vertices[edge[1]])
					));
		}
		output.print("</Document>\n");

		output.print("</kml>\n");
		if (zip)
		{
			zoS.closeEntry(); // close KML entry
			zoS.close();
		}
		else
			output.close();
	}

	/**
	 * Write a GeoTessGrid object to a File. If the extension is 'ascii' the model is
	 * written to an ascii file, otherwise it is written to a binary file.
	 * 
	 * @param outputFile
	 *            the name of the file to which the data should be written.
	 * @throws GeoTessException
	 * @throws IOException
	 */
	public void writeGrid(File outputFile) throws IOException
	{
		writeGrid(outputFile.getCanonicalPath());
	}

	/**
	 * Load the 2D grid from a File.
	 * 
	 * @param file
	 * @throws IOException
	 */
	private GeoTessGrid loadGridBinary(String file) throws IOException
	{
		DataInputStream input = new DataInputStream(new BufferedInputStream(
				new FileInputStream(file)));
		loadGrid(input);
		input.close();
		return this;
	}

	/**
	 * Load the 2D grid from an InputStream, which is neither opened nor closed
	 * by this method.
	 * 
	 * @param input
	 * @throws IOException
	 */
	public GeoTessGrid loadGrid(DataInputStream input)
			throws IOException
			{
		// first 11 characters in the file are supposed to be 'GEOTESSGRID'
		byte[] bytes = new byte[11];
		input.read(bytes);
		String s = new String(bytes);
		if (!s.equals("GEOTESSGRID"))
			throw new IOException(String.format(
					"\nExpected file %s \nto start with GEOTESSGRID but found %s%n",
					getGridInputFile() == null ? "null" : getGridInputFile() .getCanonicalPath(),
							s));

		int gridFileFormat = input.readInt();
		if (gridFileFormat != 2)
			throw new IOException(gridFileFormat
					+ " is not a recognized file format version");

		gridSoftwareVersion = readString(input);
		gridGenerationDate = readString(input);

		gridID = readString(input);

		tessellations = new int[input.readInt()][2];
		levels = new int[input.readInt()][2];
		triangles = new int[input.readInt()][3];
		vertices = new double[input.readInt()][3];

		for (int i = 0; i < tessellations.length; ++i)
		{
			int[] a = tessellations[i];
			a[0] = input.readInt();
			a[1] = input.readInt();
		}

		for (int i = 0; i < levels.length; ++i)
		{
			int[] a = levels[i];
			a[0] = input.readInt();
			a[1] = input.readInt();
		}

		for (int i = 0; i < vertices.length; ++i)
		{
			double[] a = vertices[i];
			a[0] = input.readDouble();
			a[1] = input.readDouble();
			a[2] = input.readDouble();
		}

		for (int i = 0; i < triangles.length; ++i)
		{
			int[] a = triangles[i];
			a[0] = input.readInt();
			a[1] = input.readInt();
			a[2] = input.readInt();
		}

		initialize();

		return this;
			}

	/**
	 * Write the 2D grid to a file.
	 * 
	 * @param file
	 * @param grid
	 * @throws IOException
	 */
	private void writeGridBinary(String file) throws IOException
	{
		DataOutputStream output = new DataOutputStream(
				new BufferedOutputStream(new FileOutputStream(file)));
		writeGridBinary(output);
		output.close();
	}

	/**
	 * Write the 2D grid to a file.
	 * 
	 * @param output
	 * @throws IOException
	 */
	protected void writeGridBinary(DataOutputStream output) throws IOException
	{
		// first 11 characters in the file are supposed to be 'GEOTESSGRID'
		output.writeBytes("GEOTESSGRID");

		output.writeInt(2);

		writeString(output, gridSoftwareVersion);
		writeString(output, gridGenerationDate);

		writeString(output, gridID);

		output.writeInt(tessellations.length);
		output.writeInt(levels.length);
		output.writeInt(triangles.length);
		output.writeInt(vertices.length);

		for (int i = 0; i < tessellations.length; ++i)
		{
			output.writeInt(tessellations[i][0]);
			output.writeInt(tessellations[i][1]);
		}

		for (int i = 0; i < levels.length; ++i)
		{
			output.writeInt(levels[i][0]);
			output.writeInt(levels[i][1]);
		}

		for (int i = 0; i < vertices.length; ++i)
		{
			output.writeDouble(vertices[i][0]);
			output.writeDouble(vertices[i][1]);
			output.writeDouble(vertices[i][2]);
		}

		for (int i = 0; i < triangles.length; ++i)
		{
			output.writeInt(triangles[i][0]);
			output.writeInt(triangles[i][1]);
			output.writeInt(triangles[i][2]);
		}

		output.flush();
	}

	/**
	 * Read grid from an ascii File.
	 * 
	 * @param inputFile
	 *            File from which to read grid definition.
	 * @throws FileNotFoundException
	 *             if file not found
	 */
	private GeoTessGrid loadGridAscii(String inputFile) throws IOException
	{
		Scanner input = new Scanner(new File(inputFile));
		loadGrid(input);
		input.close();
		return this;
	}

	/**
	 * Read grid from a Scanner in ascii format. Scanner is not closed by this
	 * method.
	 * 
	 * @param input
	 *            Scanner from which to read grid definition.
	 */
	public GeoTessGrid loadGrid(Scanner input) throws IOException
	{
		String comment = input.nextLine();
		if (!comment.equals(("GEOTESSGRID")))
			throw new IOException(String.format(
					"\nExpected file %s \nto start with GEOTESSGRID but found %s%n",
					getGridInputFile() == null ? "null" : getGridInputFile() .getCanonicalPath(),
							comment));

		// read file format version number, which should be the only thing
		// on the first line of the file. Will be an int between 1 and 32767
		// inclusive.
		int gridFileFormat = input.nextInt();
		input.nextLine(); // read the end-of-line character(s)

		if (gridFileFormat != 2)
			throw new IOException("File format " + gridFileFormat
					+ " is not supported by this version of GeoTessGridAscii");

		// read software version and time stamp
		gridSoftwareVersion = input.nextLine();
		gridGenerationDate = input.nextLine();

		// read gridID
		comment = input.nextLine();
		gridID = input.nextLine();
		comment = input.nextLine();

		// read nTessellations, nLevels, nTriangles and nVertices.
		tessellations = new int[input.nextInt()][2];
		levels = new int[input.nextInt()][2];
		triangles = new int[input.nextInt()][3];
		vertices = new double[input.nextInt()][3];

		comment = input.nextLine();
		comment = input.nextLine(); // skip comment
		// System.out.println(comment);
		for (int i = 0; i < tessellations.length; ++i)
			for (int j = 0; j < 2; ++j)
				tessellations[i][j] = input.nextInt();

		comment = input.nextLine();
		comment = input.nextLine(); // skip comment
		for (int i = 0; i < levels.length; ++i)
			for (int j = 0; j < 2; ++j)
				levels[i][j] = input.nextInt();

		comment = input.nextLine();
		comment = input.nextLine(); // skip comment
		// System.out.println(comment);
		// for each vertex, read the geocentric unit vector
		for (int i = 0; i < vertices.length; ++i)
			for (int j = 0; j < 3; ++j)
				vertices[i][j] = input.nextDouble();

		comment = input.nextLine();
		comment = input.nextLine(); // skip comment
		// System.out.println(comment);
		// for each triangle read the indexes of the 3 vertices that form the
		// corners of the triangle,
		for (int i = 0; i < triangles.length; ++i)
			for (int j = 0; j < 3; ++j)
				triangles[i][j] = input.nextInt();

		input.nextLine();

		initialize();

		return this;
	}

	/**
	 * Write the grid out to an ascii file.
	 */
	private void writeGridAscii(String fileName) throws IOException
	{
		BufferedWriter output = new BufferedWriter(new FileWriter(fileName));
		writeGridAscii(output);
		output.close();
	}

	/**
	 * Write the grid out to an ascii file. File format 2.
	 */
	protected void writeGridAscii(Writer output) throws IOException
	{
		output.write(String.format("GEOTESSGRID%n2%n"));

		output.write(gridSoftwareVersion + NL);
		output.write(gridGenerationDate + NL);

		output.write(String.format("#unique Grid ID:%n%s%n", gridID));

		output.write(String
				.format("#geotess grid java: nTessellations, nLevels, nTriangles, nVertices:%n"));
		output.write(String.format("%d %d %d %d%n", tessellations.length,
				levels.length, triangles.length, vertices.length));

		output.write(String.format("#geotess grid tessellations:%n"));
		for (int i = 0; i < tessellations.length; ++i)
			output.write(String.format("%d %d%n", tessellations[i][0],
					tessellations[i][1]));

		output.write(String.format("#geotess grid levels:%n"));
		for (int i = 0; i < levels.length; ++i)
			output.write(String.format("%d %d%n", levels[i][0], levels[i][1]));

		output.write(String.format("#geotess grid vertices(unit_vectors):%n"));
		for (int i = 0; i < vertices.length; ++i)
		{
			output.write(Double.toString(vertices[i][0]));
			output.write(' ');
			output.write(Double.toString(vertices[i][1]));
			output.write(' ');
			output.write(Double.toString(vertices[i][2]));
			output.write(NL);
		}

		output.write(String.format("#geotess grid triangles:%n"));
		for (int i = 0; i < triangles.length; ++i)
			output.write(String.format("%d %d %d%n", triangles[i][0],
					triangles[i][1], triangles[i][2]));

		output.flush();
	}

	/**
	 * Open the specified file using the appropriate format, and read only
	 * enough of the file to retrieve the gridID.
	 * 
	 * @param fileName
	 * @return String gridID
	 * @throws IOException
	 */
	public static String getGridID(String fileName) throws IOException
	{
		String gridID = null;
		if (fileName.endsWith(".ascii"))
		{
			Scanner input = new Scanner(new File(fileName));
			if (!input.nextLine().equals("GEOTESSGRID"))
			{
				input.close();
				throw new IOException("\n" + fileName
						+ "\ndoes not appear to be a GeoTessGrid file "
						+ "since it does not begin with 'GEOTESSGRID'");
			}
			int gridFileFormat = input.nextInt();
			if (gridFileFormat != 2)
			{
				input.close();
				throw new IOException(gridFileFormat
						+ " is not a recognized file format version");
			}

			input.nextLine();
			input.nextLine();

			gridID = input.nextLine();
			input.close();
		}
		//		else if (fileName.endsWith(".nc"))
		//		{
		//			org.apache.log4j.BasicConfigurator.configure();
		//			org.apache.log4j.Logger.getRootLogger().setLevel(
		//					org.apache.log4j.Level.OFF);
		//
		//			NetcdfFile input = NetcdfFile.open(fileName);
		//			Attribute att = input.findGlobalAttribute("GeoTessGridFileFormat");
		//			if (att == null)
		//				throw new IOException(
		//						"\n"
		//								+ fileName
		//								+ "\ndoes not appear to be a GeoTessGrid file "
		//								+ "since it does not contain netcdf global attribute GeoTessGridFileFormat");
		//
		//			int gridFileFormat = att.getNumericValue().intValue();
		//			if (gridFileFormat != 2)
		//				throw new IOException(gridFileFormat
		//						+ " is not a recognized file format version");
		//
		//			gridID = input.findGlobalAttribute("gridID").getStringValue();
		//			input.close();
		//		}
		else
		{
			DataInputStream input = new DataInputStream(
					new BufferedInputStream(new FileInputStream(fileName)));

			// first 11 characters in the file are supposed to be 'GEOTESSGRID'
			byte[] bytes = new byte[11];
			input.read(bytes);
			String s = new String(bytes);
			if (!s.equals("GEOTESSGRID"))
			{
				input.close();
				throw new IOException("\n" + fileName
						+ "\ndoes not appear to be a GeoTessGrid file "
						+ "since it does not begin with 'GEOTESSGRID'");
			}

			int gridFileFormat = input.readInt();
			if (gridFileFormat != 2)
			{
				input.close();
				throw new IOException(gridFileFormat
						+ " is not a recognized file format version");
			}

			readString(input);  // gridSoftwareVersion
			readString(input);  // gridGenerationDate

			gridID = readString(input);
			input.close();
		}
		return gridID;
	}

	/**
	 * Test a file to see if it is a GeoTessGrid file.
	 * 
	 * @param inputFile
	 * @return true if inputFile is a GeoTessGrid file.
	 */
	public static boolean isGeoTessGrid(File inputFile)
	{
		String line = "";
		try
		{
			if (inputFile.getName().endsWith(".ascii"))
			{
				Scanner input = new Scanner(inputFile);
				line = input.nextLine();
				input.close();
			}
			//			else if (inputFile.getName().endsWith(".nc"))
			//			{
			//				org.apache.log4j.BasicConfigurator.configure();
			//				org.apache.log4j.Logger.getRootLogger().setLevel(
			//						org.apache.log4j.Level.OFF);
			//
			//				NetcdfFile ncfile = NetcdfFile.open(inputFile
			//						.getCanonicalPath());
			//
			//				Attribute att = ncfile
			//						.findGlobalAttribute("GeoTessGridFileFormat");
			//				if (att != null)
			//				{
			//					att = ncfile.findGlobalAttribute("gridID");
			//					if (att != null)
			//					{
			//						line = "GEOTESSGRID";
			//					}
			//				}
			//				ncfile.close();
			//			}
			else
			{
				DataInputStream input = new DataInputStream(
						new BufferedInputStream(new FileInputStream(inputFile)));
				byte[] bytes = new byte[12];
				input.read(bytes);
				line = new String(bytes);
				input.close();
			}
		}
		catch (Exception ex)
		{
			line = "";
		}

		return line.trim().equals("GEOTESSGRID");
	}

	/**
	 * Retreive the name of the file from which the grid was loaded. This will
	 * be the name of a GeoTessModel file if the grid was stored in the same
	 * file as the model.
	 * 
	 * @return the name of the file from which the grid was loaded.
	 */
	public File getGridInputFile()
	{
		return gridInputFile;
	}

	public void setGridInputFile(File gridInputFile) throws IOException
	{
		this.gridInputFile = gridInputFile == null ? null 
				: gridInputFile.getCanonicalFile();
	}

	/**
	 * Retrieve the name and version number of the software that generated the
	 * content of this GeoTessGrid
	 * 
	 * @return the name and version number of the software that generated the
	 *         content of this GeoTessGrid
	 */
	public String getInputGridSoftwareVersion()
	{
		return gridSoftwareVersion;
	}

	public void setInputGridSoftwareVersion(String inputGridSoftwareVersion)
	{
		this.gridSoftwareVersion = inputGridSoftwareVersion;
	}

	/**
	 * Retrieve the date that the content of this grid was generated.
	 * 
	 * @return the date that the content of this grid was generated.
	 */
	public String getInputGridGenerationDate()
	{
		return gridGenerationDate;
	}

	public void setInputGridGenerationDate(String inputGridGenerationDate)
	{
		this.gridGenerationDate = inputGridGenerationDate;
	}

	/**
	 * Retrieve the name of the file to which this grid was most recently
	 * written, or the string "null" if it has not been written.
	 * 
	 * @return the name of the file to which this grid was most recently
	 *         written, or the string "null" if it has not been written.
	 */
	public String getGridOutputFile()
	{
		return gridOutputFile == null ? "null" : gridOutputFile;
	}

	/**
	 * Tests the integrity of the grid. Visits every triangle T, and (1) checks
	 * to ensure that every neighbor of T includes T in its list of neighbors,
	 * and (2) checks that every neighbor of T shares exactly two nodes with T.
	 * 
	 * <p>Also ensures that the first triangle on each tessellation level in the 
	 * grid has its vertices specified in clockwise order when viewed from
	 * outside the unit sphere.
	 * 
	 * @throws GeoTessException
	 *             if anything is amiss.
	 */
	public void testGrid() throws GeoTessException
	{
		for (int tessId = 0; tessId < getNTessellations(); ++tessId)
			for (int level = 0; level < getNLevels(tessId); ++level)
			{
				for (int t = 0; t < getNTriangles(tessId, level); ++t)
				{
					// triangle is an index in the triangle array
					int triangle = getTriangle(tessId, level, t);

					for (int triangleSide = 0; triangleSide < 3; triangleSide++)
					{
						// neighbor and triangle share and edge
						int neighbor = getNeighbor(triangle, triangleSide);

						// the index of triangle in neighbors array of neighbors.
						int neighborSide = getNeighborIndex(neighbor, triangle);

						if (neighborSide < 0)
						{
							StringBuffer buf = new StringBuffer();
							buf.append(String
									.format("tessId=%d level=%d triangle=%d side=%d neighbor=%d%n",
											tessId, level, triangle, triangleSide, neighbor));
							throw new GeoTessException(buf.toString());
						}

						// compare the vertices.
						if (triangles[neighbor][(neighborSide+1)%3] != triangles[triangle][(triangleSide+2)%3])
							throw new GeoTessException();

						if (triangles[neighbor][(neighborSide+2)%3] != triangles[triangle][(triangleSide+1)%3])
							throw new GeoTessException();

					}

					// if not the top level then check descendants.
					if (level < getNLevels(tessId)-1)
					{
						if (descendants[triangle] <= getLastTriangle(tessId, level))
						{
							StringBuffer buf = new StringBuffer();
							buf.append(String
									.format("tessId=%d level=%d triangle=%d descendant=%d%n",
											tessId, level, triangle, descendants[triangle]));
							throw new GeoTessException(buf.toString());
						}

						if (descendants[triangle] > getLastTriangle(tessId, level+1))
						{
							StringBuffer buf = new StringBuffer();
							buf.append(String
									.format("tessId=%d level=%d triangle=%d descendant=%d%n",
											tessId, level, triangle, descendants[triangle]));
							throw new GeoTessException(buf.toString());
						}
					}
				}

				// test the first triangle on this level to ensure that the vertices are
				// specified in clockwise order when viewed from outside the sphere.
				double[][] vertices = getTriangleVertices(getTriangle(tessId, level, 0));
				if (GeoTessUtils.scalarTripleProduct(vertices[0], vertices[1], vertices[2]) > 0.)
				{
					StringBuffer buf = new StringBuffer();
					buf.append(String
							.format("Vertices of this triangle are specified in counter-clockwise order%n"
									+"%ntessId=%d level=%d triangle=%d%n" 
									+ "%s%n%s%n%s%n%n",
									tessId, level, 0,
									EarthShape.WGS84.getLatLonString(vertices[0]),
									EarthShape.WGS84.getLatLonString(vertices[1]),
									EarthShape.WGS84.getLatLonString(vertices[2])
									));
					throw new GeoTessException(buf.toString());
				}
			}
	}

	/**
	 * Retrieve the index of the vertex that is colocated with the supplied
	 * unit vector.  
	 * @return index of colocated vertex, or -1 if there is no such vertex.
	 */
	public int getVertexIndex(double[] vertex)
	{
		for (int i=tessellations.length-1; i>=0; --i)
		{
			int vid = getVertexIndex(vertex, i);
			if (vid >= 0)
				return vid;
		}
		return -1;
	}

	/**
	 * Retrieve the index of the vertex that is colocated with the supplied
	 * unit vector.  Only vertices connected at the specified tessellation 
	 * index are searched.
	 * @param vertex a unit vector
	 * @param tessId tessellation to search for the specified unit vector.
	 * @return index of colocated vertex, or -1 if there is no such vertex.
	 */
	public int getVertexIndex(double[] vertex, int tessId)
	{
		int[] t = triangles[getTriangle(getFirstTriangle(tessId, 0), vertex)];

		if (GeoTessUtils.dot(vertex, vertices[t[0]]) > Math.cos(1e-7))
			return t[0];
		if (GeoTessUtils.dot(vertex, vertices[t[1]]) > Math.cos(1e-7))
			return t[1];
		if (GeoTessUtils.dot(vertex, vertices[t[2]]) > Math.cos(1e-7))
			return t[2];
		return -1;
	}

	/**
	 * Retrieve the index of the vertex that is closest to the supplied
	 * unit vector.  Only vertices connected at the specified tessellation 
	 * index are searched.
	 * @param vertex a unit vector
	 * @param tessId tessellation to search for the specified unit vector.
	 * @return index of closest vertex.
	 */
	public int findClosestVertex(double[] vertex, int tessId)
	{
		int[] t = triangles[getTriangle(getFirstTriangle(tessId, 0), vertex)];

		int index = 0;
		double dot = GeoTessUtils.dot(vertex, vertices[t[0]]);

		double doti = GeoTessUtils.dot(vertex, vertices[t[1]]);
		if (doti > dot)
		{
			index = 1;
			dot = doti;
		}

		doti = GeoTessUtils.dot(vertex, vertices[t[2]]);
		if (doti > dot)
		{
			index = 2;
			dot = doti;
		}

		return t[index];
	}

	/**
	 * Retrieve the index of the vertex that is closest to the supplied
	 * unit vector.  Only vertices connected at the specified tessellation 
	 * index and level index are searched.
	 * @param vertex a unit vector
	 * @param tessId tessellation to search for the specified unit vector.
	 * @param level level index relative to first level of specified tessellation.
	 * @return index of closest vertex.
	 */
	public int findClosestVertex(double[] vertex, int tessId, int level)
	{
		int[] t = triangles[getTriangle(getFirstTriangle(tessId, 0), vertex, getLevel(tessId, level))];

		int index = 0;
		double dot = GeoTessUtils.dot(vertex, vertices[t[0]]);

		double doti = GeoTessUtils.dot(vertex, vertices[t[1]]);
		if (doti > dot)
		{
			index = 1;
			dot = doti;
		}

		doti = GeoTessUtils.dot(vertex, vertices[t[2]]);
		if (doti > dot)
		{
			index = 2;
			dot = doti;
		}

		return t[index];
	}

	public double getPointDensity(int vertex, int tessId) throws GeoTessException
	{
		// values are the centroids of the triangles touched by a vertex, 
		// keys are the azimuth from the vertex to the centroid.
		// This will contain the centroids sorted by azimuth.
		TreeMap<Double, double[]> centroidMap = new TreeMap<Double, double[]>();

		// get list of triangles that include this vertex
		ArrayListInt t = getVertexTriangles(tessId, getNLevels(tessId)-1, vertex);

		if (t.size() == 0)
			return Double.NaN;

		double[] centroid = null;

		for (int it=0; it<t.size(); ++it)
		{
			// find the centroid of the triangle
			centroid = GeoTessUtils.center(getTriangleVertices(t.get(it)));
			// add entry: value is the centroid, key is azimuth from vertex to centroid

			if (GeoTessUtils.isPole(vertices[vertex]))
			{
				if (vertices[vertex][2] > 0)
					centroidMap.put(-EarthShape.WGS84.getLon(centroid), centroid);
				else
					centroidMap.put(EarthShape.WGS84.getLon(centroid), centroid);
			}
			else
				centroidMap.put(GeoTessUtils.azimuth(vertices[vertex], centroid, Double.NaN), centroid);
		}
		// centroids.values() now contains the triangle centroids sorted by azimuth.

		ArrayList<double[]> centroids = new ArrayList<double[]>(centroidMap.values());
		// the centroids define the veronoi polygon surrounding the vertex.

		// duplicate the first centroid because in the next step 
		// we will loop over adjacent pairs of centroids
		centroids.add(centroids.get(0));

		// find the unit vectors normal to each edge of the veronoi polygon.
		ArrayList<double[]> cross = new ArrayList<double[]>();
		for (int i=1; i<centroids.size(); ++i)
			cross.add(GeoTessUtils.crossNormal(centroids.get(i), centroids.get(i-1)));

		// duplicate the first crossProduct because in the next step 
		// we will loop over adjacent pairs of cross products
		cross.add(cross.get(0));

		// sum the interior angles.
		double area = -(t.size()-2)*Math.PI; 
		for (int i=1; i<cross.size(); ++i)
			if (GeoTessUtils.scalarTripleProduct(cross.get(i-1), cross.get(i), centroids.get(i)) < 0.)
				area += Math.PI - GeoTessUtils.angle(cross.get(i-1), cross.get(i));
			else
				area += Math.PI + GeoTessUtils.angle(cross.get(i-1), cross.get(i));	

		if (area < 0.)
			throw new GeoTessException("area is < 0.");

		return 1./area;

	}


	public HashMapIntegerDouble getPointDensity(int tessId) throws GeoTessException
	{
		// find the indices that are connected at top level of this tessellation
		HashSetInteger indices = getVertexIndices(getLastLevel(tessId));

		// map to hold results
		HashMapIntegerDouble ptDensity = new HashMapIntegerDouble(indices.size());

		Iterator iv = indices.iterator();
		while (iv.hasNext())
		{
			int vertex = iv.next();
			ptDensity.put(vertex, getPointDensity(vertex, tessId));
		}

		return ptDensity;
	}

	public GeoTessModel getPointDensityModel(int tessId) throws Exception
	{
		// Create a MetaData object in which we can specify information
		// needed for model contruction.
		GeoTessMetaData metaData = new GeoTessMetaData();

		// Specify a description of the model. This information is not
		// processed in any way by GeoTess. It is carried around for
		// information purposes.
		metaData.setDescription(String
				.format("Point density for tessellation %d%n", tessId));

		// Specify a list of layer names. A model could have many layers,
		// e.g., ("core", "mantle", "crust"), specified in order of
		// increasing radius. This simple example has only one layer.
		metaData.setLayerNames("surface");

		// specify the names of the attributes and the units of the
		// attributes in two String arrays. This model only includes
		// one attribute.
		// If this model had two attributes, they would be specified 
		// like this: setAttributes("Distance; Depth", "degrees; km");
		metaData.setAttributes("POINT_DENSITY", "");

		// specify the DataType for the data. All attributes, in all
		// profiles, will have the same data type.
		metaData.setDataType(DataType.DOUBLE);

		// specify the name of the software that is going to generate
		// the model.  This gets stored in the model for future reference.
		metaData.setModelSoftwareVersion("GeoTessGrid.getPointDensityModel()");

		// specify the date when the model was generated.  This gets 
		// stored in the model for future reference.
		metaData.setModelGenerationDate(new Date().toString());

		// call a GeoTessModel constructor to build the model. This will
		// load the grid, and initialize all the data structures to null.
		// To be useful, we will have to populate the data structures.
		GeoTessModel model = new GeoTessModel(this, metaData);

		// generate point density data
		for (int vtx = 0; vtx <vertices.length; ++vtx)
			model.setProfile(vtx, Data.getDataDouble(getPointDensity(vtx, tessId)));

		// At this point, we have a fully functional GeoTessModel object
		// that we can work with.
		return model;

	}

	protected void computeSpokeLists(int level)
	{
		synchronized(spokeListLock)
		{
			if (spokeList[level] == null)
			{
				spokeList[level] = new Edge[vertices.length];
				Edge[] levelSpokes=spokeList[level];

				Edge edge, head, spoke, prev, next;

				// levelSpokes is an nVertices array of spokes (Edge*).  For each vertex, it contains
				// the first spoke (Edge*) that emanates from the vertex.  Each spoke has a pointer to
				// the next spoke in the circular list of spokes that emanate from the same vertex.

				for (int triangle = levels[level][0]; triangle < levels[level][1]; ++triangle)
				{
					Edge[] tedges = edgeList[triangle];
					for (int i=0; i<3; ++i)
					{
						edge = tedges[i];
						spoke = levelSpokes[edge.vj];
						if (spoke == null)
							levelSpokes[edge.vj] = edge;
						else
						{
							edge.next = spoke;
							levelSpokes[edge.vj] = edge;
						}
					}
				}

				for (int vertex=0; vertex<vertices.length; ++vertex)
				{
					head = levelSpokes[vertex];
					if (head != null)
					{
						spoke = head;
						while (spoke.next != null)
						{
							prev = spoke;
							next = spoke.next;
							while (next.tLeft != spoke.tRight)
							{
								prev = next;
								next = next.next;
							}
							prev.next = next.next;
							next.next = spoke.next;
							spoke.next = next;
							spoke = next;
						}
						spoke.next = head;

						// test!
						//					spoke = levelSpokes[vertex];
						//					for (int i=0; i<20; ++i)
						//					{
						//						System.out.printf("%6d %6 %6d %6d%n", spoke.vj, spoke.vk,
						//								spoke.tRight, spoke.tLeft);
						//						if(spoke.next.tLeft != spoke.tRight)
						//							throw new GeoTessException("edges are out of order");
						//						spoke = spoke.next;
						//					}
						//					System.out.println();
					}
				}
			}
		}
	}

	/**
	 * Compute the unit vector at the center of the specified triangle.
	 * @param triangle
	 * @return unit vector at the center of the specified triangle.
	 */
	public double[] getCenter(int triangle)
	{
		return GeoTessUtils.center(vertices[triangles[triangle][0]],
				vertices[triangles[triangle][1]], vertices[triangles[triangle][2]]);

	}

	/**
	 * Convert tessellation to a Delaunay tessellation.
	 * 
	 * @return number of changes.
	 * @throws GeoTessException
	 */
	public int delaunay() throws GeoTessException
	{
		computeCircumCenters();

		int nChanges = 0, tLeft, tRight, iLeft, jLeft, kLeft, iRight, jRight, kRight, nbr, nid;

		int[] vLeft = new int[3];
		int[] vRight = new int[3];
		int[] idx;

		Edge erk, elj, elk, erj, eli, eri;

		double[] center;

		for (int t=0; t<triangles.length; ++t)
			for (int corner=0; corner<3; ++corner)
			{
				// get the indices of the triangles to the left and right of the common edge.
				tLeft = edgeList[t][corner].tLeft;
				tRight = edgeList[t][corner].tRight;

				// retrieve the circumcenter of the triangle on the 'left'
				// side of this edge.
				center = getCircumCenter(tLeft);

				// find the index of tLeft in tRight's array of neighbors.
				// This will correspond to the corner in tRight that is not on edge.
				iRight = getNeighborIndex( tRight, tLeft);

				// if vertex at i2 is inside the circumCircle of tLeft then we
				// need to flip the edge between tLeft and tRight
				if (GeoTessUtils.dot(center, getTriangleVertex(tRight, iRight))-1e-15 > center[3])
				{
					// find the index of tRight in tLeft's array of neighbors.
					// This will correspond to the corner in tLeft that is not on edge.
					iLeft =  getNeighborIndex(tLeft, tRight);

					jLeft = (iLeft+1)%3;
					kLeft = (iLeft+2)%3;
					jRight = (iRight+1)%3;
					kRight = (iRight+2)%3;

					// get copies of the triangles to left and right.
					// These are vertex indices
					vLeft[0] = triangles[tLeft][0];
					vLeft[1] = triangles[tLeft][1];
					vLeft[2] = triangles[tLeft][2];
					vRight[0] = triangles[tRight][0];
					vRight[1] = triangles[tRight][1];
					vRight[2] = triangles[tRight][2];

					// reorganize all the edges.
					eli = edgeList[tLeft][iLeft];
					elj = edgeList[tLeft][kLeft];
					elk = edgeList[tRight][jRight];

					eri = edgeList[tRight][iRight];
					erj = edgeList[tRight][kRight];
					erk = edgeList[tLeft][jLeft];

					eli.vj = vRight[iRight];
					eli.vk = vLeft[iLeft];
					eli.tLeft = tRight;
					eli.tRight = tLeft;

					eri.vj = eli.vk;
					eri.vk = eli.vj;
					eri.tLeft = tLeft;
					eri.tRight = tRight;

					erk.tRight = tRight;
					elk.tRight = tLeft;

					edgeList[tLeft][0] = eli;
					edgeList[tLeft][1] = elj;
					edgeList[tLeft][2] = elk;
					edgeList[tRight][0] = eri;
					edgeList[tRight][1] = erj;
					edgeList[tRight][2] = erk;

					// reorganize the neighbors and their edges
					nbr = elk.tLeft;
					nid = getNeighborIndex(nbr, tRight);
					edgeList[nbr][nid].tLeft = tLeft;

					nbr = erk.tLeft;
					nid = getNeighborIndex(nbr, tLeft);
					edgeList[nbr][nid].tLeft = tRight;

					// change the vertex indices of the two triangles.
					idx = triangles[tLeft];
					idx[0] = vLeft[jLeft];
					idx[1] = vRight[iRight];
					idx[2] = vLeft[iLeft];

					GeoTessUtils.circumCenterPlus(vertices[idx[0]], vertices[idx[1]],
							vertices[idx[2]], circumCenters[tLeft]);

					idx = triangles[tRight];
					idx[0] = vRight[jRight];
					idx[1] = vLeft[iLeft];
					idx[2] = vRight[iRight];

					GeoTessUtils.circumCenterPlus(vertices[idx[0]], vertices[idx[1]],
							vertices[idx[2]], circumCenters[tRight]);

					++nChanges;
				}
			}

		//System.out.printf("GeoTessGrid.delaunay().  nChanges=%4d  totalChanges=%4d%n", nChanges, totalChanges);

		initialize();

		return nChanges;
	}

	//	/**
	//	 * Read grid definition from NetCDF files.
	//	 * 
	//	 * @param inputFile
	//	 *            name of file from which to read grid definition.
	//	 */
	//	private GeoTessGrid loadGridNetcdf(String inputFile) throws IOException
	//	{
	//		org.apache.log4j.BasicConfigurator.configure();
	//		org.apache.log4j.Logger.getRootLogger().setLevel(
	//				org.apache.log4j.Level.OFF);
	//
	//		NetcdfFile ncfile = NetcdfFile.open(inputFile);
	//		loadGrid(ncfile);
	//		ncfile.close();
	//		return this;
	//	}
	//
	//	/**
	//	 * Read grid definition from a NetCDF file.
	//	 * 
	//	 * @param ncfile
	//	 *            name of file from which to read grid definition.
	//	 * @throws IOException
	//	 */
	//	private GeoTessGrid loadGrid(NetcdfFile ncfile) throws IOException
	//	{
	//		Attribute att = ncfile.findGlobalAttribute("GeoTessGridFileFormat");
	//		if (att == null)
	//			throw new IOException(
	//					"Netcdf file does not conatin attribute 'GeoTessGridFileFormat'");
	//
	//		int gridFileFormat = att.getNumericValue().intValue();
	//		if (gridFileFormat != 2)
	//			throw new IOException(gridFileFormat
	//					+ " is not a recognized file format version");
	//
	//		att = ncfile.findGlobalAttribute("gridSoftwareVersion");
	//		gridSoftwareVersion = att.getStringValue();
	//
	//		att = ncfile.findGlobalAttribute("gridGenerationDate");
	//		gridGenerationDate = att.getStringValue();
	//
	//		vertices = (double[][]) ncfile.findVariable("vertices").read()
	//				.copyToNDJavaArray();
	//		triangles = (int[][]) ncfile.findVariable("triangles").read()
	//				.copyToNDJavaArray();
	//		levels = (int[][]) ncfile.findVariable("levels").read()
	//				.copyToNDJavaArray();
	//		tessellations = (int[][]) ncfile.findVariable("tessellations").read()
	//				.copyToNDJavaArray();
	//
	//		if (ncfile.findGlobalAttribute("gridID") == null)
	//		{
	//			throw new IOException("gridID is missing");
	//			// MD5Hash md5 = new MD5Hash();
	//			// md5.update(tessellations);
	//			// md5.update(levels);
	//			// md5.update(triangles);
	//			// md5.update(vertices);
	//			// gridID = md5.toString().toUpperCase();
	//			// System.out.println("#unique Grid ID:");
	//			// System.out.println(gridID);
	//		}
	//		else
	//			gridID = ncfile.findGlobalAttribute("gridID").getStringValue();
	//
	//		neighbors = findNeighbors(optimization);
	//		return this;
	//	}
	//
	//	/**
	//	 * Write the grid to a netcdf file.
	//	 */
	//	private void writeGridNetcdf(String file) throws IOException
	//	{
	//		org.apache.log4j.BasicConfigurator.configure();
	//		org.apache.log4j.Logger.getRootLogger().setLevel(
	//				org.apache.log4j.Level.OFF);
	//
	//		// create netcdf file for writing.
	//		NetcdfFileWriteable ncfile = NetcdfFileWriteable.createNew(file, false);
	//
	//		// write the metadata while the netcdf file is in define mode.
	//		writeGridNetcdfMetaData(ncfile);
	//		// leave define mode.
	//		ncfile.create();
	//		// write actual data, which cannot be done in define mode.
	//		writeGridNetcdfInfo(ncfile);
	//		// close the file.
	//		ncfile.close();
	//	}
	//
	//	/**
	//	 * Write netcdf metadata (Dimensions, Variables and Atributes) to a
	//	 * NetcdfFileWriteable
	//	 */
	//	protected void writeGridNetcdfMetaData(NetcdfFileWriteable ncfile)
	//			throws IOException
	//	{
	//		ncfile.addGlobalAttribute("GeoTessGridFileFormat", 2);
	//
	//		ncfile.addGlobalAttribute("gridID", gridID);
	//
	//		ncfile.addGlobalAttribute("gridSoftwareVersion", gridSoftwareVersion);
	//		ncfile.addGlobalAttribute("gridGenerationDate", gridGenerationDate);
	//
	//		Dimension nTessellations = ncfile.addDimension("nTessellations",
	//				tessellations.length);
	//		Dimension nLevels = ncfile.addDimension("nLevels", levels.length);
	//		Dimension trianglesDim = ncfile.addDimension("nTriangles",
	//				triangles.length);
	//		Dimension verticesDim = ncfile.addDimension("nVertices",
	//				vertices.length);
	//
	//		Dimension n2 = ncfile.addDimension("n2", 2);
	//		Dimension n3 = ncfile.addDimension("n3", 3);
	//
	//		ArrayList<Dimension> dims;
	//
	//		// define Variable
	//		dims = new ArrayList<Dimension>();
	//		dims.add(nTessellations);
	//		dims.add(n2);
	//		ncfile.addVariable("tessellations", ucar.ma2.DataType.INT, dims);
	//
	//		dims = new ArrayList<Dimension>();
	//		dims.add(nLevels);
	//		dims.add(n2);
	//		ncfile.addVariable("levels", ucar.ma2.DataType.INT, dims);
	//
	//		dims = new ArrayList<Dimension>();
	//		dims.add(trianglesDim);
	//		dims.add(n3);
	//		ncfile.addVariable("triangles", ucar.ma2.DataType.INT, dims);
	//
	//		dims = new ArrayList<Dimension>();
	//		dims.add(verticesDim);
	//		dims.add(n3);
	//		ncfile.addVariable("vertices", ucar.ma2.DataType.DOUBLE, dims);
	//		ncfile.addVariableAttribute("vertices", "type", "unit_vector");
	//
	//	}
	//
	//	/**
	//	 * Write the grid information, excluding metadata, to a NetcdfFileWriteable
	//	 */
	//	protected void writeGridNetcdfInfo(NetcdfFileWriteable ncfile)
	//			throws IOException
	//	{
	//		try
	//		{
	//			// instantiate netcdf variables
	//			Array array;
	//			Index index;
	//
	//			// the first and last+1 level in each tessellation
	//			array = new ArrayInt.D2(tessellations.length, 2);
	//			index = array.getIndex();
	//			for (int i = 0; i < tessellations.length; i++)
	//				for (int j = 0; j < 2; ++j)
	//					array.setInt(index.set(i, j), tessellations[i][j]);
	//			ncfile.write("tessellations", array);
	//
	//			// the first and last+1 triangle on each level
	//			array = new ArrayInt.D2(levels.length, 2);
	//			index = array.getIndex();
	//			for (int i = 0; i < levels.length; i++)
	//				for (int j = 0; j < 2; ++j)
	//					array.setInt(index.set(i, j), levels[i][j]);
	//			ncfile.write("levels", array);
	//
	//			// indeces of the 3 vertices that define the corners of
	//			// each triangle
	//			array = new ArrayInt.D2(triangles.length, 3);
	//			index = array.getIndex();
	//			for (int i = 0; i < triangles.length; i++)
	//				for (int j = 0; j < 3; j++)
	//					array.setInt(index.set(i, j), triangles[i][j]);
	//			ncfile.write("triangles", array);
	//
	//			// vertex positions as 3D, earth-centered, unit vectors
	//			array = new ArrayDouble.D2(vertices.length, 3);
	//			index = array.getIndex();
	//			for (int i = 0; i < vertices.length; i++)
	//				for (int j = 0; j < 3; j++)
	//					array.setDouble(index.set(i, j), vertices[i][j]);
	//			ncfile.write("vertices", array);
	//
	//			ncfile.flush();
	//		}
	//		catch (InvalidRangeException e)
	//		{
	//			throw new IOException(e);
	//		}
	//	}
}
