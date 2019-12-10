package gms.shared.utilities.geotess;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import gms.shared.utilities.geotess.util.containers.hash.sets.HashSetInteger;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import org.junit.Before;
import org.junit.Test;


public class PointMapTest
{
	private GeoTessModel model;
	
	private PointMap pointMap;
	
	@Before
	public void setUpBeforeClass() throws Exception
	{

		model = new GeoTessModel(
				new File("src/test/resources/permanent_files/unified_crust20_ak135.geotess"));
		
		//System.out.println(model);
		
		pointMap = model.getPointMap();

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
		int[] expected = new int[] {4050, 29532, 12198, 1926, 2568, 30114, 30114, 30114, 30114};
		
		int[] actual = model.getLayerCount(true);
		
		assertArrayEquals(expected, actual);
	}

	@Test
	public void testGetVertexIndex()
	{
		assertEquals(340, pointMap.getVertexIndex(29960));
	}

	@Test
	public void testGetTessId()
	{
		assertEquals(1, pointMap.getTessId(29960));
	}

	@Test
	public void testGetLayerIndex()
	{
		assertEquals(4, pointMap.getLayerIndex(29960));
	}

	@Test
	public void testGetNodeIndex()
	{
		assertEquals(2, pointMap.getNodeIndex(29960));
	}

	@Test
	public void testGetPointIndices()
	{
		//System.out.println(Arrays.toString(pointMap.getPointIndices(100000)));
		assertArrayEquals(new int[] {340, 4, 2}, pointMap.getPointIndices(29960));
	}

	@Test
	public void testGetPointIndex()
	{
		assertEquals(29960, pointMap.getPointIndex(340, 4, 2));
	}

	@Test
	public void testGetPointIndexLast()
	{
		assertEquals(29961, pointMap.getPointIndexLast(340, 4));
	}

	@Test
	public void testGetPointIndexFirst()
	{
		assertEquals(29958, pointMap.getPointIndexFirst(340, 4));
	}

	@Test
	public void testSetPointValue()
	{
		int pointIndex = 29960;
		
		double value = pointMap.getPointValueDouble(pointIndex, 0);
		
		//System.out.println(value);
		
		assertEquals(0.12360798567533493, value, 1e-12);
		
		pointMap.setPointValue(pointIndex, 0, 99.);
		
		assertEquals(99., pointMap.getPointValueDouble(pointIndex, 0), 1e-12);
		
		pointMap.setPointValue(pointIndex, 0, value);
		
		assertEquals(value, pointMap.getPointValueDouble(pointIndex, 0), 1e-12);
		
		
	}

	@Test
	public void testSetPointData()
	{
		int pointIndex = 29960;

		// get a Data object
		Data original = pointMap.getPointData(pointIndex);
		
		// extract the value of attribute 0
		float value = original.getFloat(0);
		
		//System.out.println(value);
		
		// check the value
		assertTrue(0.123607986F == value);
		//assertEquals(0.123607986F, value, 0.F);
		
		// make a new data object with value 999
		Data newData = new DataFloat(999.F);
		
		// replace the data object 
		pointMap.setPointData(pointIndex, newData);
		
		// ensure the retrieved data object has the new value
		assertTrue(999.F == pointMap.getPointData(pointIndex).getFloat(0));
		//assertEquals(999.F, pointMap.getPointData(pointIndex).getFloat(0), 0F);
		
		// restore the original data object
		pointMap.setPointData(pointIndex, original);
		
		// ensure the original data object was properly restored.
		assertTrue(value == pointMap.getPointData(pointIndex).getFloat(0));
		//assertEquals(value, pointMap.getPointData(pointIndex).getFloat(0), 0F);
		
	}

	@Test
	public void testGetPointValue()
	{
		assertEquals(0.123607986F, pointMap.getPointValueFloat(29960, 0), 0.F);
	}

	@Test
	public void testIsNaN()
	{
		assertFalse(pointMap.isNaN(29960, 0));
	}

	@Test
	public void testGetPointVector()
	{
		//System.out.println(Arrays.toString(pointMap.getPointVector(29960)));
		assertArrayEquals(new double[] {-411.41961673428034, 5689.026744105083, 2453.220957664492},
				pointMap.getPointVector(29960), 1e-12);
	}

	@Test
	public void testGetPointUnitVector()
	{
		//System.out.println(Arrays.toString(pointMap.getPointUnitVector(29960)));
		assertArrayEquals(new double[] {-0.06626103977851575, 0.9162441751912158, 0.3951026272193577},
				pointMap.getPointUnitVector(29960), 1e-15);
	}

	@Test
	public void testGetPointRadius()
	{
		//System.out.println(pointMap.getPointRadius(29960));
		assertEquals(6209.072, pointMap.getPointRadius(29960), 1e-3);
	}

	@Test
	public void testGetPointDepth()
	{
		//System.out.println(pointMap.getPointDepth(29960));
		assertEquals(165.711, pointMap.getPointDepth(29960), 1e-3);
	}

	@Test
	public void testGetDistance3D()
	{
		//System.out.println(pointMap.getDistance3D(20612, 29960));
		assertEquals(979.963, pointMap.getDistance3D(20612, 29960), 1e-3);
	}

	@Test
	public void testGetPointNeighbors()
	{
		// the points with these indexes are all withing 
		// 1010 km of the point with index 29960.
		HashSetInteger expected = new HashSetInteger();
		expected.add(20612);
		expected.add(29884);
		expected.add(28668);
		expected.add(30036);
		expected.add(5852);
		expected.add(8781);
		
		HashSet<Integer> actual = pointMap.getPointNeighbors(29960);
		
		assertEquals(expected.size(), actual.size());
		
		for (Integer i : actual)
		{
			//System.out.printf("expected.add(%d);%n", i);
			assertTrue(expected.contains(i));
			
			// assert that the distance in km from test point to 
			// neighbor point is less than 1010 km.
			assertTrue(pointMap.getDistance3D(29960, i) < 1010.);
		}
	}

	@Test
	public void testGetPointLatLonString()
	{
		//System.out.println(pointMap.getPointLatLonString(29960));
		assertEquals(" 23.412381   94.136321", pointMap.getPointLatLonString(29960));
	}

}
