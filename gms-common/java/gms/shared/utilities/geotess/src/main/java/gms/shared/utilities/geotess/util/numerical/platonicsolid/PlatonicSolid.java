package gms.shared.utilities.geotess.util.numerical.platonicsolid;

/**
 * <p>
 * Definitions for the 5 Platonic solids: tetrahedron, cube, octahedron,
 * icosahedron and dodecahedron. The tetrahexahedron is also defined even though
 * it is not a platonic solid (it is a combination of a cube and an octahedron).
 * For each polyhedron, the unit vectors of each of the verteces and the
 * connectivity for each face are defined.
 * </p>
 * <p>
 * The geocentric distance between vertices, in degrees:
 * <table>
 * <tr>
 * <td align="left">tetrahedron</td>
 * <td align="right">109.471220634491</td>
 * </tr>
 * <tr>
 * <td align="left">cube</td>
 * <td align="right">70.528779365509</td>
 * </tr>
 * <tr>
 * <td align="left">octahedron</td>
 * <td align="right">90.000000000000</td>
 * </tr>
 * <tr>
 * <td align="left">dodecahedron</td>
 * <td align="right">41.810314895779</td>
 * </tr>
 * <tr>
 * <td align="left">icosahedron</td>
 * <td align="right">63.434948822922</td>
 * </tr>
 * </table>
 * <p>
 * Copyright: Copyright (c) 2008
 * </p>
 * 
 * @author Sandy Ballard
 * @version 1.0
 */
public enum PlatonicSolid {
	/**
	 * Solid composed of 4 vertexes and 4 triangular faces.
	 * <p>
	 * The geocentric distance between vertices, in degrees = 109.471220634491
	 */
	TETRAHEDRON(new double[][] {
			{ 0.000000000000000, 0.000000000000000, 1.000000000000000 },
			{ 0.942809041582063, 0.000000000000000, -0.333333333333333 },
			{ -0.471404520791032, 0.816496580927726, -0.333333333333333 },
			{ -0.471404520791032, -0.816496580927726, -0.333333333333333 } },
			new int[][] { { 1, 2, 3 }, { 0, 3, 2 }, { 0, 1, 3 }, { 0, 2, 1 } }),

	/**
	 * Solid composed of 8 vertexes and 6 square faces. All values are +/-
	 * 1/sqrt(3)
	 * <p>
	 * The geocentric distance between vertices, in degrees = 70.528779365509
	 */
	CUBE(new double[][] {
			{ 0.5773502691896258, 0.5773502691896258, 0.5773502691896258 },
			{ -0.5773502691896258, 0.5773502691896258, 0.5773502691896258 },
			{ -0.5773502691896258, -0.5773502691896258, 0.5773502691896258 },
			{ 0.5773502691896258, -0.5773502691896258, 0.5773502691896258 },
			{ 0.5773502691896258, 0.5773502691896258, -0.5773502691896258 },
			{ -0.5773502691896258, 0.5773502691896258, -0.5773502691896258 },
			{ -0.5773502691896258, -0.5773502691896258, -0.5773502691896258 },
			{ 0.5773502691896258, -0.5773502691896258, -0.5773502691896258 } },

	new int[][] { { 3, 2, 1, 0 }, { 0, 1, 5, 4 }, { 0, 4, 7, 3 },
			{ 1, 2, 6, 5 }, { 2, 3, 7, 6 }, { 4, 5, 6, 7 } }),
	/**
	 * Solid composed of 6 vertexes and 8 triangular faces.
	 * <p>
	 * The geocentric distance between vertices, in degrees = 90.000000000000
	 */
	OCTAHEDRON(new double[][] { { 0., 0., 1. }, { 0., 1., 0. }, { 1., 0., 0. },
			{ -1., 0., 0. }, { 0., -1., 0. }, { 0., 0., -1. } }, new int[][] {
			{ 0, 1, 2 }, { 0, 3, 1 }, { 0, 4, 3 }, { 0, 2, 4 }, { 5, 2, 1 },
			{ 5, 1, 3 }, { 5, 3, 4 }, { 5, 4, 2 } }),

	/**
	 * Solid composed of 12 vertexes and 20 triangular faces. The first and last
	 * points are the north and south poles, respectively. For all other points,
	 * the z-components are = +/- 1/sqrt(5)
	 * <p>
	 * The geocentric distance between vertices, in degrees = 63.434948822922
	 */
	ICOSAHEDRON(new double[][] {
			{ 0.0000000000000000, 0.0000000000000000, 1.0000000000000000 },
			{ 0.8944271909999159, 0.0000000000000000, 0.4472135954999579 },
			{ 0.2763932022500211, 0.8506508083520400, 0.4472135954999580 },
			{ -0.7236067977499789, 0.5257311121191338, 0.4472135954999581 },
			{ -0.7236067977499788, -0.5257311121191338, 0.4472135954999579 },
			{ 0.2763932022500209, -0.8506508083520401, 0.4472135954999580 },
			{ 0.7236067977499790, -0.5257311121191337, -0.4472135954999580 },
			{ 0.7236067977499792, 0.5257311121191336, -0.4472135954999580 },
			{ -0.2763932022500208, 0.8506508083520401, -0.4472135954999581 },
			{ -0.8944271909999160, -0.0000000000000002, -0.4472135954999580 },
			{ -0.2763932022500213, -0.8506508083520400, -0.4472135954999580 },
			{ 0.0000000000000000, 0.0000000000000000, -1.0000000000000000 } },
			new int[][] { { 0, 2, 1 }, { 0, 3, 2 }, { 0, 4, 3 }, { 0, 5, 4 },
					{ 0, 1, 5 }, { 2, 7, 1 }, { 3, 8, 2 }, { 4, 9, 3 },
					{ 5, 10, 4 }, { 1, 6, 5 }, { 1, 7, 6 }, { 2, 8, 7 },
					{ 3, 9, 8 }, { 4, 10, 9 }, { 5, 6, 10 }, { 7, 11, 6 },
					{ 8, 11, 7 }, { 9, 11, 8 }, { 10, 11, 9 }, { 6, 11, 10 } }),

	/**
	 * Solid composed of 20 vertexes and 12 pentagonal faces.
	 * <p>
	 * The geocentric distance between vertices, in degrees = 41.810314895779
	 */
	DODECAHEDRON(new double[][] {
			{ 0.4911234731884231, 0.3568220897730899, 0.7946544722917661 },
			{ -0.1875924740850798, 0.5773502691896257, 0.7946544722917661 },
			{ -0.6070619982066862, 0.0000000000000000, 0.7946544722917662 },
			{ -0.1875924740850799, -0.5773502691896258, 0.7946544722917661 },
			{ 0.4911234731884230, -0.3568220897730900, 0.7946544722917661 },
			{ 0.7946544722917661, 0.5773502691896257, 0.1875924740850799 },
			{ -0.3035309991033430, 0.9341723589627157, 0.1875924740850799 },
			{ -0.9822469463768460, -0.0000000000000001, 0.1875924740850799 },
			{ -0.3035309991033431, -0.9341723589627157, 0.1875924740850798 },
			{ 0.7946544722917661, -0.5773502691896258, 0.1875924740850799 },
			{ 0.9822469463768460, -0.0000000000000000, -0.1875924740850799 },
			{ 0.3035309991033433, 0.9341723589627157, -0.1875924740850799 },
			{ -0.7946544722917661, 0.5773502691896258, -0.1875924740850799 },
			{ -0.7946544722917660, -0.5773502691896258, -0.1875924740850799 },
			{ 0.3035309991033430, -0.9341723589627157, -0.1875924740850799 },
			{ 0.6070619982066864, -0.0000000000000000, -0.7946544722917661 },
			{ 0.1875924740850800, 0.5773502691896257, -0.7946544722917661 },
			{ -0.4911234731884230, 0.3568220897730899, -0.7946544722917662 },
			{ -0.4911234731884231, -0.3568220897730901, -0.7946544722917661 },
			{ 0.1875924740850798, -0.5773502691896258, -0.7946544722917661 } },
			new int[][] { { 4, 3, 2, 1, 0 }, { 5, 10, 9, 4, 0 },
					{ 4, 9, 14, 8, 3 }, { 3, 8, 13, 7, 2 }, { 2, 7, 12, 6, 1 },
					{ 1, 6, 11, 5, 0 }, { 12, 7, 13, 18, 17 },
					{ 18, 13, 8, 14, 19 }, { 19, 14, 9, 10, 15 },
					{ 15, 10, 5, 11, 16 }, { 16, 11, 6, 12, 17 },
					{ 19, 18, 17, 16, 15 } }),

	/**
	 * Solid composed of 14 vertexes and 24 triangular faces. Start with a cube.
	 * Put a vertex in the middle of each face and split the face into 4
	 * triangles. This is not a Platonic Solid but rather a combination of a
	 * cube and an octahedron.
	 */
	TETRAHEXAHEDRON(new double[][] {
			{ 0.5773502691896258, 0.5773502691896258, 0.5773502691896258 },
			{ -0.5773502691896258, 0.5773502691896258, 0.5773502691896258 },
			{ -0.5773502691896258, -0.5773502691896258, 0.5773502691896258 },
			{ 0.5773502691896258, -0.5773502691896258, 0.5773502691896258 },
			{ 0.5773502691896258, 0.5773502691896258, -0.5773502691896258 },
			{ -0.5773502691896258, 0.5773502691896258, -0.5773502691896258 },
			{ -0.5773502691896258, -0.5773502691896258, -0.5773502691896258 },
			{ 0.5773502691896258, -0.5773502691896258, -0.5773502691896258 },
			{ 0.0000000000000000, 0.0000000000000000, 1.0000000000000000 },
			{ 0.0000000000000000, 1.0000000000000000, 0.0000000000000000 },
			{ 1.0000000000000000, 0.0000000000000000, 0.0000000000000000 },
			{ -1.0000000000000000, 0.0000000000000000, 0.0000000000000000 },
			{ 0.0000000000000000, -1.0000000000000000, 0.0000000000000000 },
			{ 0.0000000000000000, 0.0000000000000000, -1.0000000000000000 } },
			new int[][] { { 8, 3, 2 }, { 8, 2, 1 }, { 8, 1, 0 }, { 8, 0, 3 },
					{ 9, 0, 1 }, { 9, 1, 5 }, { 9, 5, 4 }, { 9, 4, 0 },
					{ 10, 0, 4 }, { 10, 4, 7 }, { 10, 7, 3 }, { 10, 3, 0 },
					{ 11, 1, 2 }, { 11, 2, 6 }, { 11, 6, 5 }, { 11, 5, 1 },
					{ 12, 2, 3 }, { 12, 3, 7 }, { 12, 7, 6 }, { 12, 6, 2 },
					{ 13, 4, 5 }, { 13, 5, 6 }, { 13, 6, 7 }, { 13, 7, 4 } });

	/**
	 * A n by 3 array of doubles where n is the number of verteces in the
	 * polyhedron and 3 is the number of components in the unit vector.
	 */
	private final double[][] vertices;

	/**
	 * The connectivity for each face of the polyhedron. This is an nFaces by
	 * nVerticesPerFace array of integers where the integers are the index of
	 * the vertex in attribute vertices.
	 */
	private final int[][] faces;

	/**
	 * Constructor
	 * 
	 * @param vertices
	 *            double[][]
	 * @param faces
	 *            int[][]
	 */
	private PlatonicSolid(double[][] vertices, int[][] faces) {
		this.vertices = vertices;
		this.faces = faces;
	}

	/**
	 * Retrieve the number of verteces that define the polyhedron: 4 for
	 * tetrahedron, 8 for cube, 6 for octahedron, 12 for icosahedron and 20 for
	 * dodecahedron.
	 * 
	 * @return int
	 */
	public int getNVertices() {
		return vertices.length;
	}

	/**
	 * Retrieve the number of faces that define the polyhedron: 4 for
	 * tetrahedron, 6 for cube, 8 for octahedron, 20 for icosahedron and 12 for
	 * dodecahedron.
	 * 
	 * @return int
	 */
	public int getNFaces() {
		return faces.length;
	}

	/**
	 * Retrieve the number of verteces that define each face of the polyhedron:
	 * 3 for tetrahedron, 4 for cube, 3 for octahedron, 3 for icosahedron and 5
	 * for dodecahedron.
	 * 
	 * @return int
	 */
	public int getNVerticesPerFace() {
		return faces[0].length;
	}

	/**
	 * Retrieve and array of unit vectors, one for each vertex of the
	 * polyhedron.
	 * 
	 * @return double[][]
	 */
	public double[][] getVertices() {
		return vertices;
	}

	/**
	 * Retrieve the unit vector for the i'th vertex.
	 * 
	 * @param i
	 *            int
	 * @return double[]
	 */
	public double[] getVertex(int i) {
		return vertices[i];
	}

	/**
	 * Get the unit vector of the vertex that is the j'th vertex of the i'th
	 * face of the polyhedron. i ranges from 0 to nFaces and j ranges from 0 to
	 * nVertecesPerFace.
	 * 
	 * @param i
	 *            int
	 * @param j
	 *            int
	 * @return double[]
	 */
	public double[] getVertex(int i, int j) {
		return vertices[faces[i][j]];
	}

	/**
	 * Retrieve the connectivity map for all the faces of the polyhedron.
	 * 
	 * @return int[][] nFaces by nVerticesPerFace array of integers where the
	 *         integers are the index of the vertex.
	 */
	public int[][] getFaces() {
		return faces;
	}

	/**
	 * Retrieve the connectivity of the i'th face fo the polyhedron.
	 * 
	 * @param i
	 *            int the index of the face for which the connectivity is
	 *            desired.
	 * @return int[] the indeces of the verteces that comprise the i'th face.
	 *         The length will be 3 for tetrahedron, 4 for cube, 3 for
	 *         octahedron, 3 for icosahedron and 5 for dodecahedron.
	 */
	public int[] getFace(int i) {
		return faces[i];
	}

	/**
	 * Retrieve the length of the first edge of the first face of the solid, in
	 * radians.
	 * 
	 * @param nSubdivisions
	 *            number of times actual edgelength should be divided in half.
	 * @return the length of the first edge of the first face of the solid, in
	 *         radians.
	 */
	public double getEdgeLength(int nSubdivisions) {
		double[] u = getVertex(0, 0);
		double[] v = getVertex(0, 1);
		double length = Math.acos(u[0] * v[0] + u[1] * v[1] + u[2] * v[2]);
		for (int i = 1; i < nSubdivisions; ++i)
			length *= 0.5;
		return length;
	}

	/**
	 * Retrieve the length of the first edge of the first face of the solid, in
	 * degrees.
	 * 
	 * @param nSubdivisions
	 *            number of times actual edgelength should be divided in half.
	 * @return the length of the first edge of the first face of the solid, in
	 *         degrees.
	 */
	public double getEdgeLengthDegrees(int nSubdivisions) {
		return Math.toDegrees(getEdgeLength(nSubdivisions));
	}
}
