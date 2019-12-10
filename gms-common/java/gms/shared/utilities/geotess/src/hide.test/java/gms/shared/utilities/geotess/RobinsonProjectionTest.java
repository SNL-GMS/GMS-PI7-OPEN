package gms.shared.utilities.geotess;

import static org.junit.Assert.assertEquals;

import gms.shared.utilities.geotess.util.mapprojection.RobinsonProjection;
import gms.shared.utilities.geotess.util.numerical.vector.VectorGeo;
import java.util.ArrayList;
import org.junit.Before;
import org.junit.Test;

public class RobinsonProjectionTest
{

	RobinsonProjection map;

	int verbosity;

	@Before
	public void setUpBeforeClass() throws Exception
	{

		map = new RobinsonProjection(30);

		verbosity = 0;
	}

	@Test
	public void testProject()
	{
		double[] xy = map.project(0, 120, true);

		assertEquals(0.5, xy[0], 0.);

		assertEquals(0., xy[1], 0.);

	}


	@Test
	public void testProjectArray() throws Exception
	{
		ArrayList<double[]> transect = new ArrayList<double[]>();

		transect.add(VectorGeo.getVectorDegrees(10, -165));
		transect.add(VectorGeo.getVectorDegrees(10, -155));
		transect.add(VectorGeo.getVectorDegrees(10, -145));
		transect.add(VectorGeo.getVectorDegrees(10, -135));

		ArrayList<ArrayList<double[]>> xy = map.project(transect);

		assertEquals(2, xy.size());
		assertEquals(3, xy.get(0).size());
		assertEquals(3, xy.get(1).size());

		assertEquals(xy.get(0).get(2)[0], -xy.get(1).get(0)[0], 1e-12);

	}


	@Test
	public void testProjectArray2() throws Exception
	{
		ArrayList<double[]> transect = new ArrayList<double[]>();

		transect.add(VectorGeo.getVectorDegrees(10, -165));
		transect.add(VectorGeo.getVectorDegrees(10, -155));
		transect.add(VectorGeo.getVectorDegrees(10, -150));
		transect.add(VectorGeo.getVectorDegrees(10, -145));
		transect.add(VectorGeo.getVectorDegrees(10, -135));

		ArrayList<ArrayList<double[]>> xy = map.project(transect);

		if (verbosity > 0)
			System.out.println("testProjectArray2");

		if (verbosity > 1)
		{
			for (int i=0; i<xy.size(); ++i)
				for (int j=0; j<xy.get(i).size(); ++j)
					System.out.printf("%3d %3d %12.8f %12.8f%n", i, j,
							xy.get(i).get(j)[0], xy.get(i).get(j)[1]);
			System.out.println();
		}

		assertEquals(2, xy.size());
		assertEquals(3, xy.get(0).size());
		assertEquals(3, xy.get(1).size());

		assertEquals(xy.get(0).get(2)[0], -xy.get(1).get(0)[0], 1e-12);
		assertEquals(xy.get(0).get(2)[1], xy.get(1).get(0)[1], 1e-12);

	}


	@Test
	public void testProjectArray3() throws Exception
	{
		ArrayList<double[]> transect = new ArrayList<double[]>();

		transect.add(VectorGeo.getVectorDegrees(10, -165));
		transect.add(VectorGeo.getVectorDegrees(10, -155));
		transect.add(VectorGeo.getVectorDegrees(10, -150));
		transect.add(VectorGeo.getVectorDegrees(20, -150));
		transect.add(VectorGeo.getVectorDegrees(20, -145));
		transect.add(VectorGeo.getVectorDegrees(20, -135));

		ArrayList<ArrayList<double[]>> xy = map.project(transect);

		if (verbosity > 0)
			System.out.println("testProjectArray3");

		if (verbosity > 1)
		{
			for (int i=0; i<xy.size(); ++i)
				for (int j=0; j<xy.get(i).size(); ++j)
					System.out.printf("%3d %3d %12.8f %12.8f%n", i, j,
							xy.get(i).get(j)[0], xy.get(i).get(j)[1]);
			System.out.println();
		}

		assertEquals(2, xy.size());
		assertEquals(3, xy.get(0).size());
		assertEquals(3, xy.get(1).size());

	}

	@Test
	public void testProjectArray4() throws Exception
	{
		ArrayList<double[]> transect = new ArrayList<double[]>();

		transect.add(VectorGeo.getVectorDegrees(20, -150));
		transect.add(VectorGeo.getVectorDegrees(20, -145));
		transect.add(VectorGeo.getVectorDegrees(20, -135));

		ArrayList<ArrayList<double[]>> xy = map.project(transect);

		if (verbosity > 0)
			System.out.println("testProjectArray4");

		if (verbosity > 1)
		{
			for (int i=0; i<xy.size(); ++i)
				for (int j=0; j<xy.get(i).size(); ++j)
					System.out.printf("%3d %3d %12.8f %12.8f%n", i, j,
							xy.get(i).get(j)[0], xy.get(i).get(j)[1]);
			System.out.println();
		}

		assertEquals(1, xy.size());
		assertEquals(3, xy.get(0).size());
	}


	@Test
	public void testProjectArray5() throws Exception
	{
		ArrayList<double[]> transect = new ArrayList<double[]>();

		transect.add(VectorGeo.getVectorDegrees(20, -135));
		transect.add(VectorGeo.getVectorDegrees(20, -145));
		transect.add(VectorGeo.getVectorDegrees(20, -150));
		transect.add(VectorGeo.getVectorDegrees(10, -150));

		ArrayList<ArrayList<double[]>> xy = map.project(transect);

		if (verbosity > 0)
			System.out.println("testProjectArray5");

		if (verbosity > 1)
		{
			for (int i=0; i<xy.size(); ++i)
				for (int j=0; j<xy.get(i).size(); ++j)
					System.out.printf("%3d %3d %12.8f %12.8f%n", i, j,
							xy.get(i).get(j)[0], xy.get(i).get(j)[1]);
			System.out.println();
		}

		assertEquals(1, xy.size());
		assertEquals(4, xy.get(0).size());
	}


	@Test
	public void testProjectArray6() throws Exception
	{
		ArrayList<double[]> transect = new ArrayList<double[]>();

		transect.add(VectorGeo.getVectorDegrees(20, -135));
		transect.add(VectorGeo.getVectorDegrees(20, -145));
		transect.add(VectorGeo.getVectorDegrees(20, -150));

		ArrayList<ArrayList<double[]>> xy = map.project(transect);

		if (verbosity > 0)
			System.out.println("testProjectArray6");

		if (verbosity > 1)
		{
			for (int i=0; i<xy.size(); ++i)
				for (int j=0; j<xy.get(i).size(); ++j)
					System.out.printf("%3d %3d %12.8f %12.8f%n", i, j,
							xy.get(i).get(j)[0], xy.get(i).get(j)[1]);
			System.out.println();
		}

		assertEquals(1, xy.size());
		assertEquals(3, xy.get(0).size());
	}


	@Test
	public void testProjectArray7() throws Exception
	{
		ArrayList<double[]> transect = new ArrayList<double[]>();

		// edge of map is at longitude = -150
		//transect.add(VectorGeo.getVectorDegrees(-20, -135));
		transect.add(VectorGeo.getVectorDegrees(-20, -145));
		transect.add(VectorGeo.getVectorDegrees(-20, -150));
		transect.add(VectorGeo.getVectorDegrees(  0, -150));
		transect.add(VectorGeo.getVectorDegrees( 20, -150));
		transect.add(VectorGeo.getVectorDegrees( 20, -145));
		//transect.add(VectorGeo.getVectorDegrees(20, -135));

		ArrayList<ArrayList<double[]>> xy = map.project(transect);

		if (verbosity > 0)
			System.out.println("testProjectArray7");

		if (verbosity > 1)
		{
			for (int i=0; i<xy.size(); ++i)
				for (int j=0; j<xy.get(i).size(); ++j)
					System.out.printf("%3d %3d %12.8f %12.8f%n", i, j,
							xy.get(i).get(j)[0], xy.get(i).get(j)[1]);
			System.out.println();
		}

		assertEquals(1, xy.size());
		assertEquals(5, xy.get(0).size());
	}

	@Test
	public void testProjectArray8() throws Exception
	{
		ArrayList<double[]> transect = new ArrayList<double[]>();

		// edge of map is at longitude = -150
		transect.add(VectorGeo.getVectorDegrees(-20, -135));
		transect.add(VectorGeo.getVectorDegrees(-20, -145));
		transect.add(VectorGeo.getVectorDegrees(-20, -150));
		transect.add(VectorGeo.getVectorDegrees(  0, -150));
		transect.add(VectorGeo.getVectorDegrees( 20, -150));
		transect.add(VectorGeo.getVectorDegrees( 20, -155));
		transect.add(VectorGeo.getVectorDegrees( 20, -165));

		ArrayList<ArrayList<double[]>> xy = map.project(transect);

		if (verbosity > 0)
			System.out.println("testProjectArray8");

		if (verbosity > 1)
		{
			for (int i=0; i<xy.size(); ++i)
				for (int j=0; j<xy.get(i).size(); ++j)
					System.out.printf("%3d %3d %12.8f %12.8f%n", i, j,
							xy.get(i).get(j)[0], xy.get(i).get(j)[1]);
			System.out.println();
		}

		assertEquals(2, xy.size());
		assertEquals(3, xy.get(0).size());
		assertEquals(3, xy.get(1).size());
	}

	@Test
	public void testProjectTriangle1() throws Exception
	{
		if (verbosity > 0)
			System.out.println("testProjectTriangle1");

		double[][] triangle = new double[3][];

		triangle[0] = VectorGeo.getVectorDegrees(0, 10);
		triangle[1] = VectorGeo.getVectorDegrees(10,10);
		triangle[2] = VectorGeo.getVectorDegrees(0, 20);

		ArrayList<ArrayList<double[]>> cells = new ArrayList<ArrayList<double[]>>();

		new RobinsonProjection(180).projectTriangle(triangle, cells);

		if (verbosity > 1)
		{
			for (int i=0; i<cells.size(); ++i)
			{
				for (int j=0; j<cells.get(i).size(); ++j)
					System.out.printf("%3d %s  %8.4f %8.4f%n", i, 
							VectorGeo.getLatLonString(cells.get(i).get(j)),
							cells.get(i).get(j)[3], cells.get(i).get(j)[4]);
				System.out.println();
			}
			System.out.println();
		}

		assertEquals(1, cells.size());

	}

	@Test
	public void testProjectTriangle2() throws Exception
	{
		if (verbosity > 0)
			System.out.println("testProjectTriangle2");

		double[][] triangle = new double[3][];

		triangle[0] = VectorGeo.getVectorDegrees(0, -10);
		triangle[1] = VectorGeo.getVectorDegrees(10, -10);
		triangle[2] = VectorGeo.getVectorDegrees(0, 10);

		ArrayList<ArrayList<double[]>> cells = new ArrayList<ArrayList<double[]>>();

		new RobinsonProjection(180).projectTriangle(triangle, cells);

		if (verbosity > 1)
		{
			for (int i=0; i<cells.size(); ++i)
			{
				for (int j=0; j<cells.get(i).size(); ++j)
					System.out.printf("%3d %s  %8.4f %8.4f%n", i, 
							VectorGeo.getLatLonString(cells.get(i).get(j)),
							cells.get(i).get(j)[3], cells.get(i).get(j)[4]);
				System.out.println();
			}
			System.out.println();
		}

		assertEquals(2, cells.size());
		
		assertEquals(4, cells.get(0).size());
		
		assertEquals(3, cells.get(1).size());
	}

	@Test
	public void testProjectTriangle3() throws Exception
	{
		if (verbosity > 0)
			System.out.println("testProjectTriangle3");

		double[][] triangle = new double[3][];

		triangle[0] = VectorGeo.getVectorDegrees(0, 0);
		triangle[1] = VectorGeo.getVectorDegrees(10, 0);
		triangle[2] = VectorGeo.getVectorDegrees(0, 10);

		ArrayList<ArrayList<double[]>> cells = new ArrayList<ArrayList<double[]>>();
		
		new RobinsonProjection(180).projectTriangle(triangle, cells);

		if (verbosity > 1)
		{
			for (int i=0; i<cells.size(); ++i)
			{
				for (int j=0; j<cells.get(i).size(); ++j)
					System.out.printf("%3d %s  %8.4f %8.4f%n", i, 
							VectorGeo.getLatLonString(cells.get(i).get(j)),
							cells.get(i).get(j)[3], cells.get(i).get(j)[4]);
				System.out.println();
			}
			System.out.println();
		}

		assertEquals(1, cells.size());
		
		assertEquals(3, cells.get(0).size());
		
	}



}
