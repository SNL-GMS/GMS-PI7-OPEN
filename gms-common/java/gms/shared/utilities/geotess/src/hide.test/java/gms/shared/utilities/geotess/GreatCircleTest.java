package gms.shared.utilities.geotess;

import static java.lang.Math.PI;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import gms.shared.utilities.geotess.util.numerical.polygon.GreatCircle;
import gms.shared.utilities.geotess.util.numerical.polygon.GreatCircle.GreatCircleException;
import gms.shared.utilities.geotess.util.numerical.vector.VectorGeo;
import org.junit.Before;
import org.junit.Test;

public class GreatCircleTest
{
	
	/**
	 * GreatCircle from +x to +y the short way.
	 */
	private GreatCircle gcShort;

	/**
	 * GreatCircle from +x to +y the long way.
	 */
	private GreatCircle gcLong;

	@Before
	public void setUpBeforeClass() throws Exception
	{

		gcShort = new GreatCircle(
				VectorGeo.getVectorDegrees(0, 0),
				VectorGeo.getVectorDegrees(0, 90));
		
		gcLong = new GreatCircle(
				VectorGeo.getVectorDegrees(0, 0),
				VectorGeo.getVectorDegrees(0, 90), false);
	}

	@Test
	public void testGreatCircleDoubleArrayDoubleArray() throws GreatCircleException
	{
		GreatCircle gc = new GreatCircle(
				VectorGeo.getVectorDegrees(0, 20),
				VectorGeo.getVectorDegrees(0, 90));
		
		assertEquals(70, gc.getDistanceDegrees(), 1e-6);		
	}

	@Test
	public void testGreatCircleDoubleArrayDoubleArrayBoolean() throws GreatCircleException
	{
		GreatCircle gc = new GreatCircle(
				VectorGeo.getVectorDegrees(0, 20),
				VectorGeo.getVectorDegrees(0, 90), true);
		
		assertEquals(70, gc.getDistanceDegrees(), 1e-6);		
		
		assertEquals(90, VectorGeo.getLonDegrees(gc.getLast()), 1e-6);
		
		gc = new GreatCircle(
				VectorGeo.getVectorDegrees(0, 20),
				VectorGeo.getVectorDegrees(0, 90), false);
		
		assertEquals(290, gc.getDistanceDegrees(), 1e-6);		

		assertEquals(90, VectorGeo.getLonDegrees(gc.getLast()), 1e-6);
		
}

	@Test
	public void testGreatCircleFirstMiddleLast() throws GreatCircleException
	{
		GreatCircle gc = new GreatCircle(
				VectorGeo.getVectorDegrees(0, 20), null,
				VectorGeo.getVectorDegrees(0, 200), true);
		
		assertEquals(180, gc.getDistanceDegrees(), 1e-6);		

		gc = new GreatCircle(
				VectorGeo.getVectorDegrees(0, 20), null,
				VectorGeo.getVectorDegrees(0, 20));
		
		assertEquals(0, gc.getDistanceDegrees(), 1e-6);		

		gc = new GreatCircle(
				VectorGeo.getVectorDegrees(0, 20), null,
				VectorGeo.getVectorDegrees(0, 20), false);
		
		assertEquals(360, gc.getDistanceDegrees(), 1e-6);		

		gc = new GreatCircle(
				VectorGeo.getVectorDegrees(0, 20), new double[] {1., 0., 0.},
				VectorGeo.getVectorDegrees(0, 200));
		
		assertEquals(180, gc.getDistanceDegrees(), 1e-6);		

		gc = new GreatCircle(
				new double[] {1., 0., 0.}, new double[] {1., 0., 0.},
				new double[] {-1., 0., 0.});
		
		assertEquals(180, gc.getDistanceDegrees(), 1e-6);		
	
		gc = new GreatCircle(
				new double[] {1., 0., 0.}, new double[] {0., 1., 0.},
				new double[] {-1., 0., 0.});
		
		assertEquals(180, gc.getDistanceDegrees(), 1e-6);		
	
		gc = new GreatCircle(
				new double[] {1., 0., 0.}, new double[] {0., 0., 1.},
				new double[] {-1., 0., 0.});
		
		assertEquals(180, gc.getDistanceDegrees(), 1e-6);		
	
		gc = new GreatCircle(
				new double[] {1., 0., 0.}, new double[] {1., 0., 0.},
				new double[] {1., 0., 0.});
		
		assertEquals(0, gc.getDistanceDegrees(), 1e-6);		
	
		gc = new GreatCircle(
				new double[] {1., 0., 0.}, new double[] {0., 1., 0.},
				new double[] {1., 0., 0.});
		
		assertEquals(0, gc.getDistanceDegrees(), 1e-6);		
	
		gc = new GreatCircle(
				new double[] {1., 0., 0.}, new double[] {0., 0., 1.},
				new double[] {1., 0., 0.});
		
		assertEquals(0, gc.getDistanceDegrees(), 1e-6);		
	
		boolean err = false;
		try
		{
			// throws error because firstPoint is zero length
			gc = new GreatCircle(
					new double[] {0., 0., 0.}, new double[] {0., 0., 1.},
					new double[] {1., 0., 0.});
		}
		catch (Error e)
		{
			err = true;
		}

		assertTrue(err);		

	}

	@Test
	public void testGreatCircleDoubleArrayDoubleDouble() throws GreatCircleException
	{
		GreatCircle gc = new GreatCircle(
				VectorGeo.getVectorDegrees(0, 0), PI/2, PI/2);
		
		assertEquals(PI/2, gc.getDistance(), 1e-6);	
		
		assertEquals(90, VectorGeo.getLonDegrees(gc.getLast()), 1e-6);
		
		gc = new GreatCircle(
				VectorGeo.getVectorDegrees(0, 0), 3*PI/2, PI/2);
		
		assertEquals(3*PI/2, gc.getDistance(), 1e-6);	
		
		assertEquals(-90, VectorGeo.getLonDegrees(gc.getLast()), 1e-6);
	}

	@Test
	public void testGetFirst() throws GreatCircleException
	{
		GreatCircle gc = new GreatCircle(
				VectorGeo.getVectorDegrees(10, 20),
				VectorGeo.getVectorDegrees(0, 90));
		
		assertArrayEquals(VectorGeo.getVectorDegrees(10, 20), gc.getFirst(), 1e-6);		
	}

	@Test
	public void testGetLast() throws GreatCircleException
	{
		GreatCircle gc = new GreatCircle(
				VectorGeo.getVectorDegrees(0, 20),
				VectorGeo.getVectorDegrees(-10, 80));
		
		assertArrayEquals(VectorGeo.getVectorDegrees(-10, 80), gc.getLast(), 1e-6);		
	}

	@Test
	public void testGetPointDoubleDoubleArray() throws GreatCircleException
	{
		double[] u = new double[3];
		gcShort.getPoint(PI/6, u);
		
		assertEquals(PI/6, gcShort.getDistance(u), 1e-6);
	}

	@Test
	public void testGetPointDouble() throws GreatCircleException
	{
		double[] u = gcShort.getPoint(PI/6);
		assertEquals(30, VectorGeo.getLonDegrees(u), 1e-6);
		assertEquals(0, VectorGeo.getLatDegrees(u), 1e-6);
		
		u = gcShort.getPoint(11*PI/6);
		assertEquals(-30, VectorGeo.getLonDegrees(u), 1e-6);
		assertEquals(0, VectorGeo.getLatDegrees(u), 1e-6);
		
		u = gcLong.getPoint(PI/6);
		assertEquals(-30, VectorGeo.getLonDegrees(u), 1e-6);
		assertEquals(0, VectorGeo.getLatDegrees(u), 1e-6);
		
		u = gcLong.getPoint(11*PI/6);
		assertEquals(30, VectorGeo.getLonDegrees(u), 1e-6);
		assertEquals(0, VectorGeo.getLatDegrees(u), 1e-6);
		
	}

	@Test
	public void testGetNormal() throws GreatCircleException
	{
		assertArrayEquals(new double[] {0., 0., 1.}, gcShort.getNormal(), 1e-6);
		assertArrayEquals(new double[] {0., 0., -1.}, gcLong.getNormal(), 1e-6);
	}

	@Test
	public void testGetIntersection() throws GreatCircleException
	{
		GreatCircle gc = new GreatCircle(
				VectorGeo.getVectorDegrees(10, 20), 
				VectorGeo.getVectorDegrees(-10, 20));
		
		double[] u = gcShort.getIntersection(gc, true);
		
		assertEquals(0., VectorGeo.getLatDegrees(u), 1e-6);
		assertEquals(20., VectorGeo.getLonDegrees(u), 1e-6);

		u = gcShort.getIntersection(gc, false);
		
		assertEquals(0., VectorGeo.getLatDegrees(u), 1e-6);
		assertEquals(20., VectorGeo.getLonDegrees(u), 1e-6);
				
		u = gcLong.getIntersection(gc, false);
		
		assertEquals(0., VectorGeo.getLatDegrees(u), 1e-6);
		assertEquals(-160., VectorGeo.getLonDegrees(u), 1e-6);

		u = gcLong.getIntersection(gc, true);
		
		assertNull(u);
				
	}

	@Test
	public void testGetDistanceDoubleArray() throws GreatCircleException
	{
		double[] u = VectorGeo.getVectorDegrees(0., 20.);
		
		assertEquals(Math.toRadians(20.), gcShort.getDistance(u), 1e-6);
		assertEquals(Math.toRadians(340.), gcLong.getDistance(u), 1e-6);
	}

	@Test
	public void testGetDistanceDegreesDoubleArray() throws GreatCircleException
	{
		double[] u = VectorGeo.getVectorDegrees(0., 20.);
		
		assertEquals(20., gcShort.getDistanceDegrees(u), 1e-6);
		assertEquals(340., gcLong.getDistanceDegrees(u), 1e-6);
	}

	@Test
	public void testTransformDoubleArray() throws GreatCircleException
	{
		double[] u = VectorGeo.getVectorDegrees(0., 45.);
		
//		System.out.println(Arrays.toString(gcShort.transform(u)));
//		System.out.println(Arrays.toString(gcLong.transform(u)));
		
		assertArrayEquals(new double[] {0, 1, 0}, gcShort.transform(u), 1e-15);
		
		assertArrayEquals(new double[] {0, -1, 0}, gcLong.transform(u), 1e-15);
		
	}

	@Test
	public void testTransformDoubleArrayDoubleArray() throws GreatCircleException
	{
		double[] u = VectorGeo.getVectorDegrees(0., 75.);
		
		double[] t = new double[3];
		
		gcShort.transform(u, t);
		
		assertArrayEquals(new double[] {0.5, 0.8660254037844386, 0.0}, t, 1e-15);
	}

}
