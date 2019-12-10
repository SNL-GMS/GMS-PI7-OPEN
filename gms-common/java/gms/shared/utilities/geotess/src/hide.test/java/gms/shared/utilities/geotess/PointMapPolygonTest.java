package gms.shared.utilities.geotess;

import static java.lang.Math.toRadians;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import gms.shared.utilities.geotess.util.containers.hash.sets.HashSetInteger;
import gms.shared.utilities.geotess.util.numerical.polygon.HorizonLayer;
import gms.shared.utilities.geotess.util.numerical.polygon.Polygon3D;
import gms.shared.utilities.geotess.util.numerical.vector.VectorGeo;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import org.junit.Before;
import org.junit.Test;

public class PointMapPolygonTest
{

	private GeoTessModel model;
	
	private PointMap pointMap;
	
	/**
	 * Center of the polygon.
	 */
	private double[] polygonCenter = VectorGeo.getVectorDegrees(30., 90.);
	
	/**
	 * Radius of the polygon in radians.
	 */
	private double polygonRadius = toRadians(30.);
	
	private Polygon3D polygon;
	
	@Before
	public void setup() throws Exception
	{
		
		model = new GeoTessModel(
				new File("src/test/resources/permanent_files/unified_crust20_ak135.geotess"));
		
		polygon = new Polygon3D(polygonCenter, polygonRadius,
				100, new HorizonLayer(0., 2), new HorizonLayer(1., 4));
		
		model.setActiveRegion(polygon);
		
		pointMap = model.getPointMap();

	}
	
	@Test
	public void testSize()
	{
		assertEquals(1092, pointMap.size());
	}

	@Test
	public void testEqualsObject()
	{		
		try
		{
			assertTrue(pointMap.equals(pointMap));
			
			PointMap other = new GeoTessModel(
					new File("src/test/resources/permanent_files/crust20.geotess")).getPointMap();

			assertFalse(pointMap.equals(other));
			
		}
		catch (IOException e)
		{
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	@Test
	public void testLayerCount()
	{
		int[] expected = new int[] {0, 0, 798, 126, 168, 0, 0, 0, 0};
		
		int[] actual = model.getLayerCount(true);
		
		assertArrayEquals(expected, actual);
	}

	@Test
	public void testInPolygon()
	{
		int[] map;
		int vertexId, layerId, nodeId;
		double[] vertex;
		Profile[] profiles;
		Profile profile;
		double radius;
		
		// iterate over all the points that are in the Polygon3D
		for (int pointIndex=0; pointIndex<pointMap.size(); ++pointIndex)
		{
			// map[0]:vertexId, map[1]:layerId, map[2]:nodeId
			map = pointMap.getPointIndices(pointIndex);
			
			vertexId = map[0];
			layerId = map[1];
			nodeId = map[2];
			
			vertex = model.getGrid().getVertex(vertexId);
			profiles = model.getProfiles(vertexId);
			profile = profiles[layerId];
			radius = profile.getRadius(nodeId);
			
			// assert that the polygon contains the point.
			assertTrue(polygon.contains(vertex, layerId));
			
			// assert that the pointIndex stored in the Profile object
			// is equal to the one used to request the point.
			assertEquals(pointIndex, profile.getPointIndex(nodeId));
		}

	}

	@Test
	public void testOutPolygon()
	{
		double[] vertex;
		Profile profile;
		
		// loop over every vertex in the model
		for (int v=0; v<model.getNVertices(); ++v)
		{
			vertex = model.getGrid().getVertex(v);
			// loop over every layer at the current vertex
			for (int layer=0; layer<=4; ++layer)
			{
				// get the profile
				profile = model.getProfile(v, layer);
				
				// loop over every node in the current profile
				for (int n=0; n<profile.getNData(); ++n)
				{
					// assert that either the point is in the polygon and the pointIndex is
					// >= 0, or the point is out of the polygon and the pointIndex is < 0.
					assertEquals(polygon.contains(vertex, layer),
							profile.getPointIndex(n) != -1);
				}
			}
		}
	}

	@Test
	public void testGetVertexIndex()
	{
		assertEquals(197, pointMap.getVertexIndex(500));
	}

	@Test
	public void testGetTessId()
	{
		assertEquals(1, pointMap.getTessId(500));
	}

	@Test
	public void testGetLayerIndex()
	{
		assertEquals(2, pointMap.getLayerIndex(500));
	}

	@Test
	public void testGetNodeIndex()
	{
		assertEquals(6, pointMap.getNodeIndex(500));
	}

	@Test
	public void testGetPointIndices()
	{
		for (int i=0; i<pointMap.size(); ++i)
		{
			int[] map = pointMap.getPointIndices(i);
			Profile p = model.getProfile(map[0], map[1]);
			assertEquals(i, p.getPointIndex(map[2]));
		}

		//System.out.println(Arrays.toString(pointMap.getPointIndices(500)));
		assertArrayEquals(new int[] {197, 2, 6}, pointMap.getPointIndices(500));
	}

	@Test
	public void testGetPointIndex()
	{
		assertEquals(500, pointMap.getPointIndex(197, 2, 6));
	}

	@Test
	public void testGetPointIndexLast()
	{
		assertEquals(961, pointMap.getPointIndexLast(340, 4));
	}

	@Test
	public void testGetPointIndexFirst()
	{
		assertEquals(958, pointMap.getPointIndexFirst(340, 4));
	}

	@Test
	public void testSetPointValue()
	{
		int pointIndex = 500;
		
		double value = pointMap.getPointValueDouble(pointIndex, 0);
		
		//System.out.println(value);
		
		assertEquals(0.07710897922515869, value, 1e-12);
		
		pointMap.setPointValue(pointIndex, 0, 99.);
		
		assertEquals(99., pointMap.getPointValueDouble(pointIndex, 0), 1e-12);
		
		pointMap.setPointValue(pointIndex, 0, value);
		
		assertEquals(value, pointMap.getPointValueDouble(pointIndex, 0), 1e-12);
		
		
	}

	@Test
	public void testSetPointData()
	{
		int pointIndex = 500;

		// get a Data object
		Data original = pointMap.getPointData(pointIndex);
		
		// extract the value of attribute 0
		float value = original.getFloat(0);
		
//		System.out.println(Float.toString(value));		
//		System.out.println(value-0.07710898F);
		
		// check the value
		assertTrue(Math.abs(value-0.07710898F) == 0F);
		//assertEquals(value, value, 0F);
		
		// make a new data object with value 999
		Data newData = new DataFloat(999.F);
		
		// replace the data object 
		pointMap.setPointData(pointIndex, newData);
		
		// ensure the retrieved data object has the new value
		assertTrue(Math.abs(pointMap.getPointData(pointIndex).getFloat(0)-999.F) == 0F);
		//assertEquals(999.F, pointMap.getPointData(pointIndex).getFloat(0), 0F);
		
		// restore the original data object
		pointMap.setPointData(pointIndex, original);
		
		// ensure the original data object was properly restored.
		assertTrue(Math.abs(value - pointMap.getPointData(pointIndex).getFloat(0)) == 0F);
		//assertEquals(value, pointMap.getPointData(pointIndex).getFloat(0), 0F);
		
	}

	@Test
	public void testGetPointValue()
	{
		assertEquals(0.07710897922515869F, pointMap.getPointValueFloat(500, 0), 0.F);
	}

	@Test
	public void testIsNaN()
	{
		assertFalse(pointMap.isNaN(500, 0));
	}

	@Test
	public void testGetPointVector()
	{
		//System.out.println(Arrays.toString(pointMap.getPointVector(500)));
		
		assertArrayEquals(new double[] {234.08611189648695, 2536.0651849768465, 3360.0827145063718},
				pointMap.getPointVector(500), 1e-12);
	
//		double[] u = pointMap.getPointVector(500);
//		double radius = GeoTessUtils.normalize(u);
//		System.out.println(GeoTessUtils.getLatLonString(u)+"  "+radius);
//		System.out.println(GeoTessUtils.angleDegrees(polygonCenter, u));

	}

	@Test
	public void testGetPointUnitVector()
	{
		//System.out.println(Arrays.toString(pointMap.getPointUnitVector(500)));
		assertArrayEquals(new double[] {0.05552026844334666, 0.6015009550075457, 0.7969404625924004},
				pointMap.getPointUnitVector(500), 1e-15);
	}

	@Test
	public void testGetPointRadius()
	{
		//System.out.println(pointMap.getPointRadius(500));
		assertEquals(4216.228, pointMap.getPointRadius(500), 1e-3);
	}

	@Test
	public void testGetPointDepth()
	{
		//System.out.println(pointMap.getPointDepth(500));
		assertEquals(2148.302, pointMap.getPointDepth(500), 1e-3);
	}

	@Test
	public void testGetDistance3D()
	{
		//System.out.println(pointMap.getDistance3D(300, 500));
		assertEquals(1723.446, pointMap.getDistance3D(300, 500), 1e-3);
	}

	@Test
	public void testGetPointNeighbors()
	{
		// the points with these indexes are all within
		// 700 km of the point with index 500.
		HashSetInteger expected = new HashSetInteger();
		expected.add(136);
		expected.add(370);
		expected.add(604);
		expected.add(422);
		
		HashSet<Integer> actual = pointMap.getPointNeighbors(500);
		
		assertEquals(expected.size(), actual.size());
		
		for (Integer i : actual)
		{
			//System.out.printf("expected.add(%d);%n", i);
			assertTrue(expected.contains(i));
			
			// assert that the distance in km from test point to 
			// neighbor point is less than 700 km.
			assertTrue(pointMap.getDistance3D(500, i) < 700.);
		}
	}

	@Test
	public void testGetPointLatLonString()
	{
		//System.out.println(pointMap.getPointLatLonString(500));
		assertEquals(" 53.024020   84.726378", pointMap.getPointLatLonString(500));
	}

}
