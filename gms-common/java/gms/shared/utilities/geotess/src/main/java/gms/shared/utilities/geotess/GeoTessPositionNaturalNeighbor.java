package gms.shared.utilities.geotess;

import static gms.shared.utilities.geotess.util.numerical.vector.VectorUnit.dot;
import gms.shared.utilities.geotess.util.containers.arraylist.ArrayListDouble;
import gms.shared.utilities.geotess.util.containers.arraylist.ArrayListInt;
import gms.shared.utilities.geotess.util.containers.hash.sets.HashSetInteger;
import gms.shared.utilities.geotess.util.globals.InterpolatorType;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.BitSet;

/**
 * Implements the Natural Neighbor Interpolation algorithm of Sibson (1980,
 * 1981). This technique interpolates values stored on a Delaunay triangulation.
 * Returned values are continuous everywhere and smooth everywhere except the
 * vertices of the triangulation. Implementation of the algorithm is described
 * in detail in Hipp et al. (1999).
 * 
 * <p>
 * Hipp, J. R., C. J. Young, S. G. Moore, E. R. Shepherd, C. A. Schultz and S.
 * C. Myers (1999).
 * 
 * <p>
 * Sibson, R., (1980) A Vector Identity for the Dirichlet Tessellation, Proc.
 * Cambridge Philosophical Society, 87, 151-155.
 * 
 * <p>
 * Sibson, R. (1981).
 * "A brief description of natural neighbor interpolation (Chapter 2)". In V.
 * Barnett. Interpreting Multivariate Data. Chichester: John Wiley. pp. 21-36.
 * 
 * <p>
 * There is no public constructor. Call
 * GeoTessModel.getPosition(InterpolatorType.NATURAL_NEIGHBOR) to obtain an
 * instance.
 * 
 * @author Sandy Ballard
 * 
 */
public class GeoTessPositionNaturalNeighbor extends GeoTessPosition
{
	
	// NOTE: GeoTessPositionNaturalNeighbor is not thread-safe!
	
	private BitSet marked;
	private ArrayListInt nnTriangles;
	
	private ArrayList<Edge> edges;

	boolean[] neighborIn = new boolean[3]; 

	double[] ip1 = new double[3];
	double[] ip2 = new double[4];
	double[] ip3 = new double[4];
	
	private Edge[] firstSpoke = new Edge[3];

	/**
	 * Constructor takes a GeoTessModel object which allows it access to all
	 * necessary information about the values and geometry of the 3D model
	 * 
	 * @param model
	 * @param radialType 
	 * @throws GeoTessException
	 */
	protected GeoTessPositionNaturalNeighbor(GeoTessModel model, InterpolatorType radialType)
			throws GeoTessException
	{
		super(model, radialType);

		grid.computeCircumCenters();

		vertices = new ArrayList<ArrayListInt>(grid.getNTessellations());
		hCoefficients = new ArrayList<ArrayListDouble>(grid.getNTessellations());

		for (int i = 0; i < grid.getNTessellations(); ++i)
		{
			vertices.add(new ArrayListInt(6));
			hCoefficients.add(new ArrayListDouble(6));
		}
		
		marked = new BitSet(grid.getNTriangles()+1);
		marked.set(grid.getNTriangles());
		//marked = new boolean[grid.getNTriangles()];
		
		nnTriangles = new ArrayListInt(64);
		edges=new ArrayList<Edge>(64);
		
		//gridVertices = grid.getVertices();
	}
	
	/**
	 * Creates a deep copy of this GeoTessPositionNaturalNeighbor object and
	 * returns it to the caller.
	 *  
	 * @return A deep copy of this GeoTessPositionNaturalNeighbor object.
	 * @throws GeoTessException
	 */
	@Override
	public GeoTessPosition deepClone() throws GeoTessException
	{
		GeoTessPositionNaturalNeighbor newGTP = (GeoTessPositionNaturalNeighbor)
				                                   GeoTessPosition.getGeoTessPosition(this);
		newGTP.copy(this);
		return newGTP;
	}

	/**
	 * Copies the input GeoTessPositionNaturalNeighbor into this
	 * GeoTessPositionNaturalNeighbor object. At exit this object is an exact
	 * (deep) replica of gtpnn.
	 * 
	 * @param gtpnn The object whose state will be copied into this object.
	 */
	@Override
	public void copy(GeoTessPosition gtpnn)
	{
		super.setCopy(gtpnn);
		
		// Note: all of fields defined in GeoTessPositionNaturalNeighbor are created
		// at construction and do not need to be copied as they are only used during
		// interpolation, during which, they are initialized, utilized, and cleared.
	}

	@Override
	public InterpolatorType getInterpolatorType() { return InterpolatorType.NATURAL_NEIGHBOR; }

	/**
	 * Update the vertices and their associated interpolation coefficients, that
	 * will be used to interpolate new values. Uses the natural neighbor
	 * interpolation algorithm originally proposed by Sibson, R. (1980), A
	 * Vector Identity For Dirichlet Tessellation, Proc. Cambridge Philosophical
	 * Society, 87, 151-155. The algorithm is described in detail in Hipp, J.
	 * R., C. J. Young, S. G. Moore, E. R. Shepherd, C. A. Schultz and S. C.
	 * Myers (1999).
	 */
	@Override
	protected void update2D(int tessid) throws GeoTessException
	{
		// get references to the vertices and coefficients involved in interpolation.
		// These are owned by the super class.
		ArrayListInt vertexes = vertices.get(tessid);
		ArrayListDouble coeff = hCoefficients.get(tessid);

		vertexes.clear();
		coeff.clear();

		// the index of the triangle that contains the interpolation point.  This was 
		// discovered by the super class using walking triangle algorithm.
		int triangle = getTriangle(tessid);

		// find the indices of the 3 vertices at the corners of triangle
		int[] triangleVertices = grid.triangles[triangle];

		// iterate over the indices of the 3 vertices at the corners of the 
		// containing triangle.
		for (int vertex : triangleVertices)
			// if the interpolation point falls on a grid node:
			if (GeoTessUtils.dot(unitVector, gridVertices[vertex]) > Math.cos(1e-7))
			{
				// the interpolation point coincides with one of the corners of
				// the triangle in which the interpolation point resides.
				// Set the list of interpolation vertices to include only
				// the identified vertex, and the interpolation coefficient = 1.
				vertexes.add(vertex);
				coeff.add(1.);
				
				return;
			}

		// The interpolation point does not coincide with a grid vertex.
		// Carry on.

		// get the tessellation level, relative to the first tessellation
		// level of the current tessellation, that was discovered the last
		// time triangle walk algorithm was run.
		int tessLevel = getTessLevel(tessid);
		// find the corresponding level relative to all levels in the grid.
		int level = grid.getLevel(tessid, tessLevel);
		boolean leftIn, rightIn;
		
//		ArrayList<Integer> allTriangles = new ArrayList<Integer>(100);
//		ArrayList<double[]> vtkCircumCenters=new ArrayList<double[]>(100);

		Edge[] gridSpokeList = grid.getSpokeList(level);
		Edge spoke;
		
		//marked[triangle] = true;
		marked.set(triangle);
		nnTriangles.add(triangle);

		// iterate over the indices of the 3 vertices at the corners of the
		// containing triangle and determine whether or not the 3 neighboring
		// triangles are natural neighbor triangles.
		for (int vi = 0; vi < 3; ++vi)
		{
			// access a random spoke emanating from vertex.
			spoke = gridSpokeList[triangleVertices[vi]];
			
			// iterate clockwise over the circular list of spokes until triangleLeft == triangle
			while (spoke.tLeft != triangle) spoke = spoke.next;
			
			neighborIn[vi] = isNNTriangle(spoke.tRight);
			firstSpoke[vi] = spoke;
		}

		// iterate over the indices of the 3 vertices at the corners of the 
		// containing triangle.
		for (int vi = 0; vi < 3; ++vi)
		{			
			spoke = firstSpoke[vi];
			rightIn = neighborIn[vi];
			
			if (rightIn)
			{
				//marked[spoke.tRight] = true;
				marked.set(spoke.tRight);
				nnTriangles.add(spoke.tRight);
			}
			else
				edges.add(grid.getEdgeList()[triangle][(vi+1)%3]);

			int nk = grid.getNeighbor(triangle, (vi+2)%3);

			while(true)
			{
				spoke = spoke.next;
				leftIn = rightIn;

				if (spoke.tRight == nk)
				{
					rightIn = neighborIn[(vi+1)%3];

					if (leftIn && !rightIn)
						edges.add(grid.getEdgeList()[spoke.tLeft][(spoke.cornerj+1)%3]);
					else if (!leftIn && rightIn)
						edges.add(grid.getEdgeList()[spoke.tRight][(spoke.next.cornerj+2)%3]);

					break;
				}

				rightIn = isNNTriangle(spoke.tRight);

				if (leftIn && !rightIn)
					edges.add(grid.getEdgeList()[spoke.tLeft][(spoke.cornerj+1)%3]);
				else if (!leftIn && rightIn)
					edges.add(grid.getEdgeList()[spoke.tRight][(spoke.next.cornerj+2)%3]);

				if (rightIn)
				{
					//marked[spoke.tRight] = true;
					marked.set(spoke.tRight);
					nnTriangles.add(spoke.tRight);
					edges.add(grid.getEdgeList()[spoke.tRight][spoke.next.cornerj]);
				}
			}
		}
		
		int prev = edges.get(0).vj;
		for (int e=edges.size()-1; e >= 0; --e)
		{
			if (edges.get(e).vk != prev) throw new GeoTessException("edges are out of order");
			prev = edges.get(e).vj;
		}
			
		double weight, totalWeight = 0.;
		int vertex;

		Edge preEdge = edges.get(0);
		for (int e=edges.size()-1; e >= 0; --e)
		{
			vertex = preEdge.vj;
			weight = 0.;

			//System.out.println(preEdge+"  *");

			// set ip1 to the virtual veronoi vertex of the triangle formed by interpolationPoint and preEdge
			GeoTessUtils.circumCenter(unitVector, gridVertices[vertex], gridVertices[preEdge.vk], ip1);

			// access a random spoke emanating from vertex.
			spoke = gridSpokeList[vertex];

			// iterate over the circular list of spokes until vertex neighbor is equal to preEdge.vk
			while (spoke.vk != preEdge.vk) spoke = spoke.next;

			// spoke is the first surrounding edge and corresponds to a reversed version of preEdge.
			// set ip2 to the circumCenter of the nnTriangle that is to the right of spoke
			grid.getCircumCenter(spoke.tRight, ip2);

			while (true)
			{
				// find the next spoke in clockwise direction
				spoke = spoke.next;

				//if (marked[spoke.tRight])
				if (marked.get(spoke.tRight))
				{
					// this is not the last spoke.
					
					// set ip3 to the circumcenter of the triangle to the right of the current edge.
					grid.getCircumCenter(spoke.tRight, ip3);
					weight += GeoTessUtils.getTriangleArea(ip1, ip2, ip3);

				}
				else
				{
					// this is the last spoke
					
					// set ip3 to the virtual veronoi vertex of the triangle formed by interpolationPoint and spoke
					GeoTessUtils.circumCenter(unitVector, gridVertices[spoke.vk], gridVertices[vertex], ip3);
					weight += GeoTessUtils.getTriangleArea(ip1, ip2, ip3);
					break;

				}

				ip2[0] = ip3[0];
				ip2[1] = ip3[1];
				ip2[2] = ip3[2];
			}

			// sum coefficient to total weight, get next edge, and continue
			totalWeight += weight;
			
			coeff.add(weight);
			vertexes.add(vertex);

			preEdge = edges.get(e);
		}

		// normalize the interpolation coefficients.
		for (int i = 0; i < coeff.size(); ++i) coeff.set(i, coeff.get(i) / totalWeight);
		
		for (int i=0; i<nnTriangles.size(); ++i)
			//marked[nnTriangles.get(i)] = false;
			marked.clear(nnTriangles.get(i));
		nnTriangles.clear();
		edges.clear();

	}
	
	private boolean isNNTriangle(int triangle)
	{
		// Get the circumcenter of triangle on the right.
		double[] center = grid.getCircumCenter(triangle);	
		return dot(center, unitVector) > center[3];
	}

	@SuppressWarnings("unused")
	private void vtk(double[] unitVector, ArrayList<Integer> allTriangles, 
			HashSetInteger nnTriangles, ArrayList<Edge> edges, ArrayList<double[]> circumCenters) throws GeoTessException
	{
		try
		{
			File out = new File("allTriangles.vtk");

			System.out.println("Writing vtk file to "+out.getAbsolutePath());

			DataOutputStream output = new DataOutputStream(
					new BufferedOutputStream(new FileOutputStream(out)));

			output.writeBytes(String.format("# vtk DataFile Version 2.0%n"));
			output.writeBytes(String.format("NaturalNeighborInterpolation%n"));
			output.writeBytes(String.format("BINARY%n"));

			output.writeBytes(String.format("DATASET UNSTRUCTURED_GRID%n"));

			output.writeBytes(String.format("POINTS %d double%n", grid.getNVertices()));

			for (int i=0; i < grid.getNVertices(); ++i)
			{
				double[] v = grid.getVertex(i);
				output.writeDouble(v[0]);
				output.writeDouble(v[1]);
				output.writeDouble(v[2]);
			}

			ArrayList<Integer> cellTypes = new ArrayList<Integer>(allTriangles.size()+nnTriangles.size()+edges.size());
			// write out all triangle
			output.writeBytes(String.format("CELLS %d %d%n", allTriangles.size(), allTriangles.size()*4));
			for (int triangle : allTriangles)
			{
				int[] t = grid.getTriangleVertexIndexes(triangle);
				output.writeInt(3);
				output.writeInt(t[0]);
				output.writeInt(t[1]);
				output.writeInt(t[2]);
				cellTypes.add(5); // triangles
			}

			// write out cellTypes
			output.writeBytes(String.format("CELL_TYPES %d%n", cellTypes.size()));
			for (int ct : cellTypes)
				output.writeInt(ct);

			output.close();
		}
		catch (Exception e)
		{
			throw new GeoTessException(e);
		}

		try
		{
			File out = new File("nnTriangles.vtk");

			System.out.println("Writing vtk file to "+out.getAbsolutePath());

			DataOutputStream output = new DataOutputStream(
					new BufferedOutputStream(new FileOutputStream(out)));

			output.writeBytes(String.format("# vtk DataFile Version 2.0%n"));
			output.writeBytes(String.format("NaturalNeighborInterpolation%n"));
			output.writeBytes(String.format("BINARY%n"));

			output.writeBytes(String.format("DATASET UNSTRUCTURED_GRID%n"));

			output.writeBytes(String.format("POINTS %d double%n", grid.getNVertices()));

			for (int i=0; i < grid.getNVertices(); ++i)
			{
				double[] v = grid.getVertex(i);
				output.writeDouble(v[0]);
				output.writeDouble(v[1]);
				output.writeDouble(v[2]);
			}

			ArrayList<Integer> cellTypes = new ArrayList<Integer>(nnTriangles.size());
			// write out all triangle
			output.writeBytes(String.format("CELLS %d %d%n", nnTriangles.size(), nnTriangles.size()*4));
			HashSetInteger.Iterator it = nnTriangles.iterator();
			while (it.hasNext())
			{
				int[] t = grid.getTriangleVertexIndexes(it.next());
				output.writeInt(3);
				output.writeInt(t[0]);
				output.writeInt(t[1]);
				output.writeInt(t[2]);
				cellTypes.add(5); // triangles
			}

			// write out cellTypes
			output.writeBytes(String.format("CELL_TYPES %d%n", cellTypes.size()));
			for (int ct : cellTypes)
				output.writeInt(ct);

			output.close();
		}
		catch (Exception e)
		{
			throw new GeoTessException(e);
		}

		try
		{
			File out = new File("edges.vtk");

			System.out.println("Writing vtk file to "+out.getAbsolutePath());

			DataOutputStream output = new DataOutputStream(
					new BufferedOutputStream(new FileOutputStream(out)));

			output.writeBytes(String.format("# vtk DataFile Version 2.0%n"));
			output.writeBytes(String.format("NaturalNeighborInterpolation%n"));
			output.writeBytes(String.format("BINARY%n"));

			output.writeBytes(String.format("DATASET UNSTRUCTURED_GRID%n"));

			output.writeBytes(String.format("POINTS %d double%n", grid.getNVertices()));

			for (int i=0; i < grid.getNVertices(); ++i)
			{
				double[] v = grid.getVertex(i);
				output.writeDouble(v[0]);
				output.writeDouble(v[1]);
				output.writeDouble(v[2]);
			}

			ArrayList<Integer> cellTypes = new ArrayList<Integer>(edges.size());
			// write out all triangle
			output.writeBytes(String.format("CELLS %d %d%n", edges.size(), edges.size()*3));
			for (Edge e : edges)
			{
				output.writeInt(2);
				output.writeInt(e.vj);
				output.writeInt(e.vk);
				cellTypes.add(3); // triangles
			}

			// write out cellTypes
			output.writeBytes(String.format("CELL_TYPES %d%n", cellTypes.size()));
			for (int ct : cellTypes)
				output.writeInt(ct);

			output.close();
		}
		catch (Exception e)
		{
			throw new GeoTessException(e);
		}


		try
		{
			File out = new File("intepolation_point.vtk");

			System.out.println("Writing vtk file to "+out.getAbsolutePath());

			DataOutputStream output = new DataOutputStream(
					new BufferedOutputStream(new FileOutputStream(out)));

			output.writeBytes(String.format("# vtk DataFile Version 2.0%n"));
			output.writeBytes(String.format("NaturalNeighborInterpolation%n"));
			output.writeBytes(String.format("BINARY%n"));

			output.writeBytes(String.format("DATASET UNSTRUCTURED_GRID%n"));

			output.writeBytes(String.format("POINTS 1 double%n"));
			output.writeDouble(unitVector[0]);
			output.writeDouble(unitVector[1]);
			output.writeDouble(unitVector[2]);

			// write out all triangle
			output.writeBytes(String.format("CELLS 1 2%n"));
			output.writeInt(1);
			output.writeInt(0);

			// write out cellTypes
			output.writeBytes(String.format("CELL_TYPES 1%n"));
			output.writeInt(1);

			output.close();
		}
		catch (Exception e)
		{
			throw new GeoTessException(e);
		}

		try
		{
			File out = new File("circum_centers.vtk");

			System.out.println("Writing vtk file to "+out.getAbsolutePath());

			DataOutputStream output = new DataOutputStream(
					new BufferedOutputStream(new FileOutputStream(out)));

			output.writeBytes(String.format("# vtk DataFile Version 2.0%n"));
			output.writeBytes(String.format("NaturalNeighborInterpolation%n"));
			output.writeBytes(String.format("BINARY%n"));

			output.writeBytes(String.format("DATASET UNSTRUCTURED_GRID%n"));

			output.writeBytes(String.format("POINTS %d double%n", circumCenters.size()));
			for (double[] v : circumCenters)
			{
				output.writeDouble(v[0]);
				output.writeDouble(v[1]);
				output.writeDouble(v[2]);
			}

			// write out all triangle
			output.writeBytes(String.format("CELLS %d %d%n", circumCenters.size(), circumCenters.size()*2));
			for (int i=0; i<circumCenters.size(); ++i)
			{
				output.writeInt(1);
				output.writeInt(i);
			}

			// write out cellTypes
			output.writeBytes(String.format("CELL_TYPES %d%n", circumCenters.size()));
			for (int i=0; i<circumCenters.size(); ++i)
				output.writeInt(1);

			output.close();
		}
		catch (Exception e)
		{
			throw new GeoTessException(e);
		}

	}

}
