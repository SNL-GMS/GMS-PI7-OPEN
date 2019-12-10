package gms.shared.utilities.geotess;

import java.util.Arrays;

/**
 * An Edge stores information about the connection between two adjacent vertices which
 * separates two neighboring triangles.  These Edge objects are used in two contexts:
 * (1) every triangle in the grid has an array of three Edges, stored in variable
 * edgeList.  (2) at every level of the grid, each vertex has a circular linked
 * list of Edges which define the Edges emanating from the vertex, in clockwise order.
 * These Edges are stored in variable spokeLists.  These two structures store
 * references to the same set of Edge objects.
 *
 * edgeList - For a triangle formed by vertices i, j and k, edge[i] is the
 * edge opposite vertex i. Put another way, edge[i] is the edge that does not contain
 * vertex i.  edge[i].vj and edge[i].vk are the indices of the other two vertices of
 * the triangle accessed, in clockwise order.
 * edge.tLeft is the index of the triangle on the left side of the edge from
 * vj to vk (the triangle that does not contain vertex i). edge.tRight is the
 * index of the triangle on the right side of the edge from vj to vk (the
 * triangle that contains vertex i).  edge(i).normal is the unit vector normal
 * to edge from vj to vk, pointing toward vertex i (edge.vk cross edge.vj),
 * edge.normal is NOT normalized to unit length.
 *
 * <p>Note that edgeList and spokeList contain pointers to the same instantiations
 * of Edge objects.  edgeList does not use the "Edge* next" field in the Edge objects
 * but spokeList relies on those fields.  Hence the 'next' pointers in Edge objects
 * should not be manipulated via edgeList.
 */
class Edge
{
	/**
	 * vertex index j
	 */
	int vj;

	/**
	 * vertex index k
	 */
	int vk;
	
	int cornerj;

	/**
	 * lndex of triangle to the left of edge from vj to vk
	 */
	int tLeft;

	/**
	 * index of triangle to the right of edge from vj to vk
	 */
	int tRight;
	
	/**
	 * vertex k cross vertex j, not normalized to unit length
	 */
	double[] normal = new double[3];

	/**
	 * pointer to next edge in circular list of edges emanating from vertex vj.
	 * Used by spokeList but not by edgeList.
	 */
	Edge next;
	
	Edge() { vj=vk=tLeft=tRight=-1; next = null; }
	
	@Override
	public boolean equals(Object other)
	{
		return vj==((Edge)other).vj && vk==((Edge)other).vk 
				&& tLeft==((Edge)other).tLeft && tRight==((Edge)other).tRight;
	}
	
	@Override
	public String toString()
	{ 
		return String.format("vj=%5d vk=%5d tLeft=%5d tRight=%5d normal=%s", 
				vj, vk, tLeft, tRight, Arrays.toString(normal)); 
	}

	public String toStringAll()
	{
		StringBuffer buf = new StringBuffer();
		Edge spoke = this;
		do
		{
			buf.append(String.format("vj=%5d vk=%5d tLeft=%5d tRight=%5d normal=%s%n", 
					spoke.vj, spoke.vk, spoke.tLeft, spoke.tRight, Arrays.toString(spoke.normal)));
			spoke = spoke.next;
		}
		while  (spoke != this);
		return buf.toString();
	}
}

