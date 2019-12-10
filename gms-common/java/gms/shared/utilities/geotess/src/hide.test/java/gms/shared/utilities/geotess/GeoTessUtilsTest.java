package gms.shared.utilities.geotess;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import gms.shared.utilities.geotess.util.numerical.vector.VectorGeo;
import gms.shared.utilities.geotess.util.numerical.vector.VectorUnit;

import org.junit.Test;

public class GeoTessUtilsTest
{

	@Test
	public void testRotate()
	{
		// pole of rotation
		double[] p = new double[] {1, 1, 1};
		VectorUnit.normalize(p);

		// point to be rotated
		double[] x = VectorUnit.moveNorth(p, 0.1);

		assertEquals(0.1, VectorUnit.angle(p, x), 1e-12);
		assertEquals(0., VectorUnit.azimuth(p, x, Double.NaN), 1e-12);

		// rotated point
		double[] z = new double[3];
		
		for (int angle=-360; angle <= 360; angle+=15)
		{
			// rotate x around pole p by angle with result going into z
			VectorUnit.rotate(x, p, Math.toRadians(angle), z);

			assertEquals(0.1, VectorUnit.angle(p, z), 1e-12);
			
			int az = (int)Math.round(VectorUnit.azimuthDegrees(p, z, -999999.));
			
			assertTrue(angle == az || angle == az-360 || angle == az+360);
		}
	}

	@Test
	public void testGetPlane()
	{
		double[] x = new double[] {1, 0, 0};
		double[] y = new double[] {0, 1, 0};
		double[] z = new double[] {0, 0, 1};

		double[] p = VectorUnit.getPlane(x, 1, y, 1, z, 1);

		//System.out.println(Arrays.toString(p));
		assertArrayEquals(new double[] {1,1,1}, p, 1e-15);
	}

	@Test
	public void testGetIntersection()
	{
		double[] x = new double[] {1, 0, 0};
		double[] y = new double[] {0, 1, 0};
		double[] z = new double[] {0, 0, 1};

		double[] p = VectorUnit.getPlane(x, 1000, y, 1000, z, 1000);

		//System.out.println(Arrays.toString(p));

		double[] u = new double[] {1,1,1};
		VectorUnit.normalize(u);

		double r = VectorUnit.getIntersection(p, u);

		//System.out.println(r);
		assertEquals(577.3502691896256, r, 1e-15);

		VectorGeo.getVectorDegrees( 2,  0, x);
		VectorGeo.getVectorDegrees(-2,  2, y);
		VectorGeo.getVectorDegrees(-2, -2, z);

		p = VectorUnit.getPlane(x, 1000, y, 1000, z, 1000);

		//System.out.println(Arrays.toString(p));

		u = new double[] {1,0,0};
		VectorUnit.normalize(u);

		r = VectorUnit.getIntersection(p, u);

		System.out.println(r);
		assertEquals(999.0945450444939, r, 1e-12);
	}

}
