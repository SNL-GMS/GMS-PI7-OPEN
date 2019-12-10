package gms.shared.utilities.geotess;

import static java.lang.Math.PI;
import static java.lang.Math.toRadians;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import gms.shared.utilities.geotess.util.containers.hash.maps.HashMapIntegerDouble;
import gms.shared.utilities.geotess.util.containers.hash.maps.HashMapIntegerDouble.Entry;
import gms.shared.utilities.geotess.util.containers.hash.maps.HashMapIntegerDouble.Iterator;
import gms.shared.utilities.geotess.util.containers.hash.sets.HashSetInteger;
import gms.shared.utilities.geotess.util.globals.DataType;
import gms.shared.utilities.geotess.util.globals.InterpolatorType;
import gms.shared.utilities.geotess.util.globals.OptimizationType;
import gms.shared.utilities.geotess.util.numerical.polygon.GreatCircle;
import gms.shared.utilities.geotess.util.numerical.polygon.Polygon;
import gms.shared.utilities.geotess.util.numerical.polygon.Polygon3D;
import gms.shared.utilities.geotess.util.numerical.vector.VectorGeo;
import gms.shared.utilities.geotess.util.numerical.vector.VectorUnit;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class GeoTessModelTest
{
	/**
	 * resources/permanent_files/unified_crust20_ak135.geotess
	 */
	private GeoTessModel model;

	/**
	 * resources/permanent_files/smallModel.geotess
	 */
	private GeoTessModel small_model;

	private GeoTessModel model2d;

	@Before
	public void setUpBeforeClass() throws Exception
	{

		model = new GeoTessModel(new File("src/test/resources/permanent_files/unified_crust20_ak135.geotess"));

		small_model = new GeoTessModel(new File("src/test/resources/permanent_files/small_model.ascii"));
		
		
		GeoTessMetaData metaData = new GeoTessMetaData();
		metaData.setDescription("2D velocity model");
		metaData.setLayerNames("surface");
		metaData.setAttributes("PSLOWNESS; PVELOCITY", "sec/km; km/sec");
		metaData.setDataType(DataType.FLOAT);
		metaData.setModelSoftwareVersion("junittests.GeoTessModelTest");
		metaData.setModelGenerationDate(new Date().toString());
		model2d = new GeoTessModel(small_model.getGrid(), metaData);
		for (int vtx = 0; vtx < model2d.getGrid().getNVertices(); ++vtx)
			model2d.setProfile(vtx, Data.getDataFloat(0.125F, 8F));

	}
	
	@Test
	public void testConstructorInputStream() throws Exception
	{
		// write model to byte array in binary format
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		small_model.writeModelBinary(baos);
		
		//System.out.println(small_model);

		GeoTessModel model = new GeoTessModel(new DataInputStream(new ByteArrayInputStream(baos.toByteArray())));
		
		//System.out.println(model);
		
		assertTrue(small_model.equals(model));
		
		// write model to byte array in ascii format
		baos = new ByteArrayOutputStream();
		small_model.writeModelAscii(baos);
		
		model = new GeoTessModel(new Scanner(new ByteArrayInputStream(baos.toByteArray())));
		
		//System.out.println(model);
		
		assertTrue(small_model.equals(model));
	}
	
	@Test
	public void testGetClosestPoint() throws GeoTessException
	{
		double[] location = model.getEarthShape().getVectorDegrees(30., 90.);
		double radius = 5000;
		int layer = 2;
		
		int pt = model.getClosestPoint(location, radius, layer);
		
		//System.out.println(model.getPointMap().getPointLatLonString(pt));
		
		int[] ptmap = model.getClosestNode(location, radius, layer);
		
		int pt2 = model.getPointMap().getPointIndex(ptmap[0], ptmap[1], ptmap[2]);
		
		assertEquals(pt, pt2);

		double distance = GeoTessUtils.getDistance3D(location, radius, 
				model.getPointMap().getPointUnitVector(pt), model.getPointMap().getPointRadius(pt));
		
		assertEquals(67.514, distance, 0.1);
		
		radius = 7000;
		layer = 2;
		
		pt = model.getClosestPoint(location, radius, layer);
		
		//System.out.println(model.getPointMap().getPointLatLonString(pt));
		
		ptmap = model.getClosestNode(location, radius, layer);
		
		pt2 = model.getPointMap().getPointIndex(ptmap[0], ptmap[1], ptmap[2]);
		
		assertEquals(pt, pt2);

		distance = GeoTessUtils.getDistance3D(location, radius, 
				model.getPointMap().getPointUnitVector(pt), model.getPointMap().getPointRadius(pt));
		
		assertEquals(1289.53, distance, 0.1);
		
	}
	
	@Test
	public void testGetPathIntegralWeights() throws GeoTessException
	{
		// test weights but no layerIds
		
		// define two unit vectors, pointA and pointB.
		// A is located at 0N, 0E and B is located at 30N, 30E.
		double[] pointA = new double[3];
		double[] pointB = new double[3];
		
		VectorGeo.getVectorDegrees(0., 0., pointA);
		VectorGeo.getVectorDegrees(0., 30., pointB);

		GreatCircle greatCircle = new GreatCircle(pointA, pointB);
		
		ArrayList<double[]> rayPath = greatCircle.getPoints(31, false);

		// radii will the radius of each of the points along the rayPath.
		double[] radii = new double[rayPath.size()];
		Arrays.fill(radii, 6300.);
		
//		ArrayList<Double> radii = new ArrayList<Double>(rayPath.size());
//		while (radii.size() < rayPath.size())
//			radii.add(6300.);
		
		HashMap<Integer, Double> weights = new HashMap<Integer, Double>();
		
		double dkm = model.getPathIntegral(-1, rayPath, radii, null,
				InterpolatorType.NATURAL_NEIGHBOR, InterpolatorType.LINEAR, weights);
		
		assertEquals(greatCircle.getDistance()*6300., dkm, 0.1);
		
		// initialize the sum of the weights to zero.  The sum of the weights
		// should equal the length of rayPath measured in km.
		double sumWeights = 0;

		for (Double w : weights.values()) sumWeights += w;

		assertEquals(greatCircle.getDistance()*6300., sumWeights, 0.1);

		double tt = model.getPathIntegral(0, rayPath, radii, null,
				InterpolatorType.NATURAL_NEIGHBOR, InterpolatorType.LINEAR, weights);
		
		assertEquals(8.08, dkm/tt, 0.01);
	
	}

	@Test
	public void testGetPathIntegral() throws GeoTessException
	{
		// test no weights and no layerIds
		
		// define two unit vectors, pointA and pointB.
		// A is located at 0N, 0E and B is located at 30N, 30E.
		double[] pointA = new double[3];
		double[] pointB = new double[3];
		
		VectorGeo.getVectorDegrees(0., 0., pointA);
		VectorGeo.getVectorDegrees(0., 30., pointB);

		GreatCircle greatCircle = new GreatCircle(pointA, pointB);
		
		ArrayList<double[]> rayPath = greatCircle.getPoints(31, false);

		// radii will the radius of each of the points along the rayPath.
		double[] radii = new double[rayPath.size()];
		Arrays.fill(radii, 6300.);
		
		double dkm = model.getPathIntegral(-1, rayPath, radii, null,
				InterpolatorType.NATURAL_NEIGHBOR, InterpolatorType.LINEAR);
		
		assertEquals(greatCircle.getDistance()*6300., dkm, 0.1);
	
		double tt = model.getPathIntegral(0, rayPath, radii, null,
				InterpolatorType.NATURAL_NEIGHBOR, InterpolatorType.LINEAR);
		
		assertEquals(408.216, tt, 0.1);
	
	}

	@Test
	public void testGetPathIntegralLayerIdsAndWeights() throws GeoTessException
	{
		// test layerIds and weights
		
		// define two unit vectors, pointA and pointB.
		// A is located at 0N, 0E and B is located at 30N, 30E.
		double[] pointA = new double[3];
		double[] pointB = new double[3];
		
		VectorGeo.getVectorDegrees(0., 0., pointA);
		VectorGeo.getVectorDegrees(0., 30., pointB);

		GreatCircle greatCircle = new GreatCircle(pointA, pointB);
		
		ArrayList<double[]> rayPath = greatCircle.getPoints(31, false);

		// radii will the radius of each of the points along the rayPath.
		double[] radii = new double[rayPath.size()];
		int[] layerIds = new int[rayPath.size()];
		
		Arrays.fill(radii, 6371.);
		Arrays.fill(layerIds, -1);
		
		HashMap<Integer, Double> weights = new HashMap<Integer, Double>();
		
		double dkm = model.getPathIntegral(-1, rayPath, radii, layerIds,
				InterpolatorType.NATURAL_NEIGHBOR, InterpolatorType.LINEAR, weights);
		
		assertEquals(greatCircle.getDistance()*6371., dkm, 0.1);
		
		// initialize the sum of the weights to zero.  The sum of the weights
		// should equal the length of rayPath measured in km.
		double sumWeights = 0;

		for (Double w : weights.values()) sumWeights += w;

		assertEquals(greatCircle.getDistance()*6371., sumWeights, 0.1);

		Arrays.fill(layerIds, 4);

		double tt = model.getPathIntegral(0, rayPath, radii, layerIds,
				InterpolatorType.NATURAL_NEIGHBOR, InterpolatorType.LINEAR, weights);
		
		assertEquals(8.04, dkm/tt, 0.01);
	
		Arrays.fill(layerIds, 5);
		
		tt = model.getPathIntegral(0, rayPath, radii, layerIds,
				InterpolatorType.NATURAL_NEIGHBOR, InterpolatorType.LINEAR, weights);
		
		assertEquals(7.158, dkm/tt, 0.01);
	
	}

	@Test
	public void testGetPathIntegralLayerIdsAndWeights2() throws GeoTessException
	{
		// test layerIds and weights
		
		// define two unit vectors, pointA and pointB.
		// A is located at 0N, 0E and B is located at 30N, 30E.
		double[] pointA = new double[3];
		double[] pointB = new double[3];
		
		VectorGeo.getVectorDegrees(0., 0., pointA);
		VectorGeo.getVectorDegrees(0., 30., pointB);

		GreatCircle greatCircle = new GreatCircle(pointA, pointB);
		
		ArrayList<double[]> rayPath = greatCircle.getPoints(31, false);

		// radii will the radius of each of the points along the rayPath.
		double[] radii = new double[rayPath.size()];
		int[] layerIds = new int[rayPath.size()];
		
		Arrays.fill(radii, 6371.);
		Arrays.fill(layerIds, -1);
		
		HashMapIntegerDouble weights = new HashMapIntegerDouble();
		
		double dkm = model.getPathIntegral(-1, rayPath, radii, layerIds,
				InterpolatorType.NATURAL_NEIGHBOR, InterpolatorType.LINEAR, weights);
		
		assertEquals(greatCircle.getDistance()*6371., dkm, 0.1);
		
		// initialize the sum of the weights to zero.  The sum of the weights
		// should equal the length of rayPath measured in km.
		double sumWeights = 0;

		Iterator it = weights.iterator();
		while (it.hasNext())
		{
			Entry e = it.nextEntry();
			sumWeights += e.getValue();
		}

		assertEquals(greatCircle.getDistance()*6371., sumWeights, .1);

		Arrays.fill(layerIds, 4);

		double tt = model.getPathIntegral(0, rayPath, radii, layerIds,
				InterpolatorType.NATURAL_NEIGHBOR, InterpolatorType.LINEAR, weights);
		
		assertEquals(8.04, dkm/tt, 0.01);
	
		Arrays.fill(layerIds, 5);
		
		tt = model.getPathIntegral(0, rayPath, radii, layerIds,
				InterpolatorType.NATURAL_NEIGHBOR, InterpolatorType.LINEAR, weights);
		
		assertEquals(7.158, dkm/tt, 0.01);
	
	}

	@Test
	public void testGetPathIntegral2D() throws GeoTessException
	{
		double[] firstPoint = new double[] {1,0,0};
		double[] lastPoint = model.getEarthShape().getVectorDegrees(0., 30.);
		
		GreatCircle greatCircle = new GreatCircle(firstPoint, lastPoint);
		
		double dkm = model2d.getPathIntegral2D(-1, 
				greatCircle, Math.toRadians(0.1), -1., InterpolatorType.NATURAL_NEIGHBOR);
		
		double tt0 = model2d.getPathIntegral2D(0, 
				greatCircle, Math.toRadians(0.1), -1., InterpolatorType.NATURAL_NEIGHBOR);
		
		assertEquals(GeoTessUtils.angle(firstPoint, lastPoint)*6378., dkm, 0.1);
		
		assertEquals(417.448, tt0, 1e-3);
	}

	@Test
	public void testGetNVertices()
	{
		assertEquals(30114, model.getNVertices());
	}

	@Test
	public void testGetNLayers()
	{
		assertEquals(9, model.getNLayers());
	}

	@Test
	public void testGetNPoints()
	{
		assertEquals(170730, model.getNPoints());
	}

	@Test
	public void testEquals() throws IOException
	{
		GeoTessModel model2 = new GeoTessModel(
				new File("src/test/resources/permanent_files/unified_crust20_ak135.geotess"));

		assertNotSame(model, model2);

		assertEquals(model, model2);

		assertFalse(model.equals(new GeoTessModel(
				new File("src/test/resources/permanent_files/crust20.geotess"))));
	}

	@Test
	public void testCopy() throws GeoTessException
	{
		GeoTessModel other = model.copy();

		assertNotSame(model, other);

		assertTrue(model.equals(other));

	}

	@Test
	public void testGetPointMap()
	{
		PointMap pm = model.getPointMap();

		assertEquals(model.getNPoints(), pm.size());
	}

	@Test
	public void testGetProfile()
	{
		Profile p = model.getProfile(340, 4);

		//System.out.println(Arrays.toString(p.getRadii()));
		assertArrayEquals(new float[] {5964.7847F, 6086.9287F, 6209.0728F, 6331.217F}, p.getRadii(), 0.001F);
	}

	@Test
	public void testGetWeights() throws GeoTessException, IOException
	{
		// get the origin of the ray path
		double[] u = VectorGeo.getVectorDegrees(20., 90.);

		// construct raypth: unit vectors and radii.
		ArrayList<double[]> v = new ArrayList<double[]>();

		// add a bunch of points along a great circle path at 
		// a constant radius.
		v.clear();
		double[][] gc = VectorUnit.getGreatCircle(u, Math.PI/2);

		double angle = PI/6;
		double radius = 5350;
		int n = 100;
		
		double[] r = new double[n];
		Arrays.fill(r, radius);

		double len = angle /(n-1.);
		for (int i=0; i<n; ++i)
			v.add(VectorUnit.getGreatCirclePoint(gc, i* len));

		// get weights from the model.
		HashMap<Integer, Double> w = new HashMap<Integer, Double>(2*n);
				
		model.getWeights(v, r, null, InterpolatorType.LINEAR, InterpolatorType.LINEAR, w);

		double sum=0;
		for (Map.Entry<Integer, Double> e : w.entrySet())
			sum += e.getValue();

		StringBuffer buf = new StringBuffer();
		// print out the weights, the locations of the points.
		buf.append("\nPt Index    weight   layer     lat         lon    depth\n");
		for (Map.Entry<Integer, Double> e : w.entrySet())
			buf.append(String.format("%6d  %10.4f   %3d %s%n", e.getKey(), e.getValue(),  
					model.getPointMap().getLayerIndex(e.getKey()),
					model.getPointMap().toString(e.getKey())));

		// compute the length of the great circle path in km.
		buf.append(String.format("%nActual length of great circle path = %1.4f km%n%n", angle*radius));

		// sum of the weights should equal length of great circle path.
		buf.append(String.format("Size = %d   Sum weights = %1.4f km%n", w.size(), sum));

		assertEquals(buf.toString(), sum, angle*radius, 0.01);

	}

//	@Test
//	public void testGetWeights() throws GeoTessException, IOException
//	{
//		// get the origin of the ray path
//		double[] u = VectorGeo.getVectorDegrees(20., 90.);
//
//		// construct raypth: unit vectors and radii.
//		ArrayList<double[]> v = new ArrayList<double[]>();
//		ArrayList<Double> r = new ArrayList<Double>();
//
//		// add a bunch of points along a great circle path at 
//		// a constant radius.
//		v.clear();
//		r.clear();
//		double[][] gc = VectorUnit.getGreatCircle(u, Math.PI/2);
//
//		double angle = PI/6;
//		double radius = 5350;
//		int n = 100;
//
//		double len = angle /(n-1.);
//		for (int i=0; i<n; ++i)
//		{
//			v.add(VectorUnit.getGreatCirclePoint(gc, i* len));
//			r.add(radius);
//		}
//
//		// get weights from the model.
//		HashMap<Integer, Double> w = model.getWeights(v, r, InterpolatorType.LINEAR, 
//				InterpolatorType.LINEAR);
//
//		double sum=0;
//		for (Map.Entry<Integer, Double> e : w.entrySet())
//			sum += e.getValue();
//
//		StringBuffer buf = new StringBuffer();
//		// print out the weights, the locations of the points.
//		buf.append("\nPt Index    weight   layer     lat         lon    depth\n");
//		for (Map.Entry<Integer, Double> e : w.entrySet())
//			buf.append(String.format("%6d  %10.4f   %3d %s%n", e.getKey(), e.getValue(),  
//					model.getPointMap().getLayerIndex(e.getKey()),
//					model.getPointMap().toString(e.getKey())));
//
//		// compute the length of the great circle path in km.
//		buf.append(String.format("%nActual length of great circle path = %1.4f km%n%n", angle*radius));
//
//		// sum of the weights should equal length of great circle path.
//		buf.append(String.format("Size = %d   Sum weights = %1.4f km%n", w.size(), sum));
//
//		assertEquals(buf.toString(), sum, angle*radius, 0.01);
//
//	}

	@Test
	public void testTestModelIntegrity() throws GeoTessException
	{
		model.testTestModelIntegrity();
	}

	@Test
	public void testIsGeoTessModel()
	{
		assertTrue(GeoTessModel.isGeoTessModel(
				new File("src/test/resources/permanent_files/crust20.geotess")));

		assertFalse(GeoTessModel.isGeoTessModel(
				new File("src/test/resources/permanent_files/geotess_grid_04000.geotess")));
	}

	/**
	 * GeoTessModel maintains a map from gridID to a reference to a GeoTessGrid object.
	 * Whenever a new model is loaded, GeoTessModel checks to see if it refers to a 
	 * GeoTessGrid object which is already in memory.  If so, it uses the reference
	 * to the existing Grid instead of loading a new copy.  This test tests some
	 * of this functionality
	 * @throws IOException 
	 */
	@Test
	public void testClearReuseGridMap() throws IOException
	{
		// get another copy of the model.  model and other will share a reference
		// to the same grid, because of gridReuse.
		GeoTessModel other = new GeoTessModel(
				new File("src/test/resources/permanent_files/unified_crust20_ak135.geotess"));

		// equal and same reference
		assertEquals(model.getGrid(), other.getGrid());
		assertSame(model.getGrid(), other.getGrid());

		// clear the grid reuse map.
		GeoTessModel.clearReuseGridMap();

		// ensure that GeoTessModel's grid reuse map is empty.
		assertEquals(0, GeoTessModel.getReuseGridMapSize());

		// load another copy of model.  This time the grid references will not be
		// equal since the grid map is empty.
		other = new GeoTessModel(
				new File("src/test/resources/permanent_files/unified_crust20_ak135.geotess"));

		// equal but not the same reference
		assertEquals(model.getGrid(), other.getGrid());
		assertNotSame(model.getGrid(), other.getGrid());

		assertEquals(1, GeoTessModel.getReuseGridMapSize());



		// repeat this process with crust20 model (grid is stored externally; not 
		// in same file with the model data).
		GeoTessModel model1 = new GeoTessModel(
				new File("src/test/resources/permanent_files/crust20.geotess"));

		// grid reuse map should contain two grids, one for unified_crust20_ak135
		// model and one for crust20.
		assertEquals(2, GeoTessModel.getReuseGridMapSize());

		// load another copy of crust20
		other = new GeoTessModel(
				new File("src/test/resources/permanent_files/crust20.geotess"));

		// still have only 2 grids in the reuse map
		assertEquals(2, GeoTessModel.getReuseGridMapSize());

		// equal and same reference
		assertEquals(model1, other);
		assertSame(model1.getGrid(), other.getGrid());

		// clear the grid reuse map.
		GeoTessModel.clearReuseGridMap();

		// ensure it is empty
		assertEquals(0, GeoTessModel.getReuseGridMapSize());

		// load another copy of model.  This time the grid references will not be
		// equal since the grid map is empty.
		other = new GeoTessModel(
				new File("src/test/resources/permanent_files/crust20.geotess"));

		assertEquals(1, GeoTessModel.getReuseGridMapSize());

		// equal but not the same reference
		assertEquals(model1, other);
		assertNotSame(model1.getGrid(), other.getGrid());

	}


	@Test
	public void testGridReuse() throws IOException
	{
		// load two models that share the same GeoTessGrid but have different stored values

		GeoTessModel.clearReuseGridMap();

		// load asar
		GeoTessModel asar = new GeoTessModel(new File("src/test/resources/permanent_files/asar.libcorr"));

		assertEquals(1, GeoTessModel.getReuseGridMapSize());

		// change the value of gridSoftwareVersion
		asar.getGrid().setInputGridSoftwareVersion("grid1");
		assertEquals("grid1", asar.getGrid().getGridSoftwareVersion());

		// load wra.  Since it uses the same grid, it will share a reference to
		// the same GeoTessGrid object as asar.
		GeoTessModel wra = new GeoTessModel(new File("src/test/resources/permanent_files/wra.libcorr"));

		// ensure that gridSoftwareVersion is equal to "grid1"
		assertEquals("grid1", wra.getGrid().getGridSoftwareVersion());

		// even though two models were loaded, the number of stored grids only
		// increased by one.
		assertEquals(1, GeoTessModel.getReuseGridMapSize());

		// change the value of gridSoftwareVersion to "grid2".  Should affect
		// both wra and asar.
		wra.getGrid().setInputGridSoftwareVersion("grid2");
		assertEquals("grid2", wra.getGrid().getGridSoftwareVersion());
		assertEquals("grid2", asar.getGrid().getGridSoftwareVersion());

		// even though two models were loaded, the number of stored grids only
		// increased by one.
		assertEquals(1, GeoTessModel.getReuseGridMapSize());
	}

	@Test
	public void testInitializeData() throws GeoTessException
	{
		GeoTessModel newModel = model.copy();

		newModel.initializeData("vp; vs; density".split(";"), 
				"km/sec; km/sec; g/cc".split(";"), 
				999.);

		assertFalse(newModel.equals(model));

		//System.out.println(newModel);
	}

	@Test
	public void testSetActiveRegion2D() throws GeoTessException, IOException
	{
		// set active region to all points.
		model.setActiveRegion();
		assertEquals(170730, model.getPointMap().size());

		// define a 2D polygon with small circle.
		double[] polygonCenter = VectorGeo.getVectorDegrees(30., 90.);

		double polygonRadius = toRadians(30.);

		Polygon polygon = new Polygon(polygonCenter, polygonRadius, 100);

		model.setActiveRegion(polygon);
		assertEquals(13834, model.getPointMap().size());

		// check every point in the point map to ensure that it is within the polygon
		for (int i=0; i<model.getPointMap().size(); ++i)
		{
			double[] point = model.getPointMap().getPointUnitVector(i);
			assertTrue(VectorUnit.angle(polygonCenter, point) < polygonRadius);
		}

		// check every node in the model.  if the node has point index < 0 then
		// ensure that it is outside the polygon.  if the node has point index 
		// >= 0, then it must be within the polygon.
		for (int vertex=0; vertex<model.getGrid().getVertices().length; ++vertex)
		{
			double distance = VectorUnit.angle(polygonCenter, model.getGrid().getVertex(vertex));
			Profile[] parray = model.getProfiles(vertex);
			for (int layer=0; layer<model.getNLayers(); ++layer)
			{
				Profile profile = parray[layer];
				for (int n=0; n<profile.getNData(); ++n)
				{
					if (profile.getPointIndex(n) < 0)
						assertTrue(distance >= polygonRadius);
					else
						assertTrue(distance <= polygonRadius);
				}
			}
		}

		model.setActiveRegion();
		assertEquals(170730, model.getPointMap().size());

	}

	@Test
	public void testSetActiveRegion3DLayers() throws GeoTessException, IOException
	{
		// set active region to all points.
		small_model.setActiveRegion();
		assertEquals(3714, small_model.getPointMap().size());

		//		// define a 3D polygon with small circle and 3 layers.
		//		double[] polygonCenter = VectorGeo.getVectorDegrees(30., 90.);
		//
		//		double polygonRadius = toRadians(30.);
		//
		//		Polygon polygon = new Polygon3D(polygonCenter, polygonRadius,
		//				100, new HorizonLayer(0., 2), new HorizonLayer(1., 4));

		Polygon3D polygon = new Polygon3D(new File("src/test/resources/permanent_files/polygon_small_circle_layers.ascii"));
		double[] polygonCenter = polygon.getReferencePoint();
		double polygonRadius = VectorUnit.angle(polygonCenter, polygon.getPoint(0));

		int topLayer = polygon.getTop().getLayerIndex();
		int bottomLayer = polygon.getBottom().getLayerIndex();

		// set the active region to 3D polygon
		small_model.setActiveRegion(polygon);
		assertEquals(183, small_model.getPointMap().size());

		// check every point in the point map to ensure that it is within the polygon
		for (int i=0; i<small_model.getPointMap().size(); ++i)
		{
			double[] point = small_model.getPointMap().getPointUnitVector(i);
			boolean ok = VectorUnit.angle(polygonCenter, point) < polygonRadius 
					&& small_model.getPointMap().getLayerIndex(i) >= bottomLayer 
					&& small_model.getPointMap().getLayerIndex(i) <= topLayer;

					if (!ok)
						System.out.printf("layer=%d polygonRadius = %f  distance = %f",
								small_model.getPointMap().getLayerIndex(i),
								polygonRadius, VectorUnit.angle(polygonCenter, point));

					assertTrue(ok);
		}

		// check every node in the small_model.  if the node has point index < 0 then
		// ensure that it is outside the polygon.  if the node has point index 
		// >= 0, then it must be within the polygon.
		for (int vertex=0; vertex<small_model.getGrid().getVertices().length; ++vertex)
		{
			double distance = VectorUnit.angle(polygonCenter, small_model.getGrid().getVertex(vertex));
			Profile[] parray = small_model.getProfiles(vertex);
			for (int layer=0; layer<small_model.getNLayers(); ++layer)
			{
				Profile profile = parray[layer];
				for (int n=0; n<profile.getNData(); ++n)
				{
					if (profile.getPointIndex(n) < 0)
						assertTrue(distance >= polygonRadius || layer < bottomLayer || layer > topLayer);
					else
						assertTrue(distance <= polygonRadius && layer >= bottomLayer && layer <= topLayer);
				}
			}
		}

		small_model.setActiveRegion();
		assertEquals(3714, small_model.getPointMap().size());		

	}

	@Test
	public void testSetActiveRegion3DRadius() throws GeoTessException, IOException
	{
		// set active region to all points.
		small_model.setActiveRegion();
		assertEquals(3714, small_model.getPointMap().size());

		//		// define a 3D polygon with small circle and 3 layers.
		//		double[] polygonCenter = VectorGeo.getVectorDegrees(30., 90.);
		//
		//		double polygonRadius = toRadians(30.);
		//
		//		double radiusTop = 6371 - 55;
		//		double radiusBottom = 4000.;
		//
		//		Horizon top = new HorizonRadius(radiusTop, 4);
		//		Horizon bottom = new HorizonRadius(radiusBottom, 2);
		//
		//		Polygon polygon = new Polygon3D(polygonCenter, polygonRadius,
		//				100, bottom, top);

		Polygon3D polygon = new Polygon3D(new File("src/test/resources/permanent_files/polygon_small_circle_radii.ascii"));
		double[] polygonCenter = polygon.getReferencePoint();
		double polygonRadius = VectorUnit.angle(polygonCenter, polygon.getPoint(0));

		double radiusTop = polygon.getTop().getValue();
		double radiusBottom = polygon.getBottom().getValue();

		int topLayer = polygon.getTop().getLayerIndex();
		int bottomLayer = polygon.getBottom().getLayerIndex();

		// set the active region to 3D polygon
		small_model.setActiveRegion(polygon);
		assertEquals(144, small_model.getPointMap().size());

		// check every point in the point map to ensure that it is within the polygon
		for (int i=0; i<small_model.getPointMap().size(); ++i)
		{
			double[] point = small_model.getPointMap().getPointUnitVector(i);
			assertTrue(VectorUnit.angle(polygonCenter, point) < polygonRadius 
					&& small_model.getPointMap().getLayerIndex(i) >= bottomLayer 
					&& small_model.getPointMap().getLayerIndex(i) <= topLayer
					&& small_model.getPointMap().getPointRadius(i) >= radiusBottom 
					&& small_model.getPointMap().getPointRadius(i) <= radiusTop
					);
		}

		// check every node in the small_model.  if the node has point index < 0 then
		// ensure that it is outside the polygon.  if the node has point index 
		// >= 0, then it must be within the polygon.
		boolean passed = true;
		for (int vertex=0; vertex<small_model.getGrid().getVertices().length; ++vertex)
		{
			double distance = VectorUnit.angle(polygonCenter, small_model.getGrid().getVertex(vertex));
			Profile[] pp = small_model.getProfiles(vertex);
			for (int layer=0; layer<small_model.getNLayers(); ++layer)
			{
				Profile p = pp[layer];
				for (int n=0; n<p.getNData(); ++n)
				{
					boolean inpolygon = distance < polygonRadius
							&& layer >= bottomLayer
							&& layer <= topLayer
							&& p.getRadius(n) > radiusBottom
							&& p.getRadius(n) < radiusTop
							;

							boolean positive = p.getPointIndex(n) >= 0;

							if (inpolygon != positive) passed = false;

							if (inpolygon != positive)
								System.out.printf("testSetActiveRegion3DRadii  ptIndex=%d dist=%1.3f layer=%d radius=%1.3f%n",
										p.getPointIndex(n),
										Math.toDegrees(distance), 
										layer,
										p.getRadius(n));
				}
			}
		}
		assertTrue(passed);

		small_model.setActiveRegion();
		assertEquals(3714, small_model.getPointMap().size());		

	}

	@Test
	public void testSetActiveRegion3DDepth() throws GeoTessException, IOException
	{

		// set active region to all points.
		small_model.setActiveRegion();
		assertEquals(3714, small_model.getPointMap().size());

		//		// define a 3D polygon with small circle and 3 layers.
		//		double[] polygonCenter = VectorGeo.getVectorDegrees(30., 90.);
		//
		//		double polygonRadius = toRadians(30.);
		//
		//		double depthTop = 55;
		//		double depthBottom = 1000.;
		//
		//		Horizon top = new HorizonDepth(depthTop, 4);
		//		Horizon bottom = new HorizonDepth(depthBottom, 2);
		//
		//		Polygon polygon = new Polygon3D(polygonCenter, polygonRadius,
		//				100, bottom, top);

		Polygon3D polygon = new Polygon3D(new File("src/test/resources/permanent_files/polygon_small_circle_depths.ascii"));
		double[] polygonCenter = polygon.getReferencePoint();
		double polygonRadius = VectorUnit.angle(polygonCenter, polygon.getPoint(0));

		double depthTop = polygon.getTop().getValue();
		double depthBottom = polygon.getBottom().getValue();

		int topLayer = polygon.getTop().getLayerIndex();
		int bottomLayer = polygon.getBottom().getLayerIndex();

		// set the active region to 3D polygon
		small_model.setActiveRegion(polygon);
		assertEquals(63, small_model.getPointMap().size());

		// check every point in the point map to ensure that it is within the polygon
		for (int i=0; i<small_model.getPointMap().size(); ++i)
		{
			double[] point = small_model.getPointMap().getPointUnitVector(i);
			double depth = small_model.getPointMap().getPointDepth(i);
			assertTrue(VectorUnit.angle(polygonCenter, point) < polygonRadius 
					&& depth <= depthBottom
					&& depth >= depthTop
					&& small_model.getPointMap().getLayerIndex(i) >= bottomLayer 
					&& small_model.getPointMap().getLayerIndex(i) <= topLayer);
		}

		// check every node in the small_model.  if the node has point index < 0 then
		// ensure that it is outside the polygon.  if the node has point index 
		// >= 0, then it must be within the polygon.
		boolean passed = true;
		for (int vertex=0; vertex<small_model.getGrid().getVertices().length; ++vertex)
		{
			double R = VectorGeo.getEarthRadius(small_model.getGrid().getVertex(vertex));
			double distance = VectorUnit.angle(polygonCenter, small_model.getGrid().getVertex(vertex));
			Profile[] pp = small_model.getProfiles(vertex);
			for (int layer=0; layer<small_model.getNLayers(); ++layer)
			{
				Profile p = pp[layer];
				for (int n=0; n<p.getNData(); ++n)
				{
					double depth = R-p.getRadius(n);

					boolean inpolygon = distance < polygonRadius
							&& layer >= bottomLayer
							&& layer <= topLayer
							&& depth <= depthBottom
							&& depth >= depthTop
							;

							boolean positive = p.getPointIndex(n) >= 0;

							if (inpolygon != positive) passed = false;

							if (inpolygon != positive)
								System.out.printf("testSetActiveRegion3DDepth  ptIndex=%d dist=%1.3f layer=%d radius=%1.3f%n",
										p.getPointIndex(n),
										Math.toDegrees(distance), 
										layer,
										p.getRadius(n));
				}
			}
		}
		assertTrue(passed);

		small_model.setActiveRegion();
		assertEquals(3714, small_model.getPointMap().size());		

	}

	/**
	 * Build a model with a grid that has layer 0 with 8 deg triangle
	 * and layer 1 with 16 degree triangles.  Layer 2 also has 16 deg
	 * triangles but is thin everywhere.  Used to test that radii
	 * are interpolated properly when higher layer has lower resolution
	 * than deeper layer.
	 * @throws IOException
	 * @throws GeoTessException
	 */
	@Test
	public void invertedModel() throws IOException, GeoTessException
	{
		GeoTessMetaData metaData = new GeoTessMetaData();
		metaData.setAttributes("speed", "furlongs/fortnight");
		metaData.setLayerNames("hires; lores; thin");
		metaData.setDataType("byte");
		metaData.setModelSoftwareVersion("GeoTessModelTest 1.0");
		metaData.setModelGenerationDate(new Date().toString());

		metaData.setLayerTessIds(new int[] {0, 1, 1});

		GeoTessModel imodel = new GeoTessModel("src/test/resources/permanent_files/invertedGrid.geotess", metaData);

		Profile p;
		float r1, r2;
		for (int layer=0; layer<imodel.getNLayers(); ++layer)
		{
			int tessid = imodel.getMetaData().getTessellation(layer);

			HashSetInteger connectedVertices = imodel.getGrid().getVertexIndicesTopLevel(tessid);

			r1 = (layer+1)*1000F;
			r2 = (layer+2)*1000F;

			for (int vtx=0; vtx<imodel.getNVertices(); ++vtx)
			{
				if (connectedVertices.contains(vtx))
				{
					if (layer == imodel.getNLayers()-1)
						p = new ProfileThin(r1, Data.getDataByte(((byte)layer)));
					else
						p = new ProfileConstant(r1, r2,	Data.getDataByte(((byte)layer)));
				}
				else
					p = new ProfileEmpty(r1, r2);

				imodel.setProfile(vtx, layer, p);
			}
		}

		//System.out.println(imodel);

		GeoTessPosition pos = imodel.getGeoTessPosition();

		pos.set(new double[] {0., 0., 1.}, 1500.);

		//		double[][] borehole = GeoTessModelUtils.getBorehole(pos, 1e30, 0, 1000, false, false, new int[] {0});
		//		for (int i=0; i<borehole.length; ++i)
		//			System.out.printf("%10.4f %2.0f%n", borehole[i][0], borehole[i][1]);

		assertEquals(0, pos.getLayerId());

		pos.setRadius(10000.);

		assertEquals(1, pos.getLayerId());

		assertEquals(2000., pos.getRadiusTop(0), 1e-12);

	}

	/**
	 * Big test. Create a model that has all 6 ProfileTypes represented. For
	 * every DataType, and for nAttributes = 1 and 2 (12 combinations) generate
	 * a model. For each output format (nc, ascii and bin) write the model to a
	 * file, read the model back into a new Model object and compare the loaded
	 * model with the original model. If they are not equal, report that they
	 * are not equal.
	 * 
	 * @param args
	 * @throws GeoTessException
	 * @throws IOException
	 */
	@Test
	@Ignore
	public void bigTest() throws GeoTessException, IOException
	{
		File gridFile = new File("src/test/resources/permanent_files/geotess_grid_16000.geotess");

		File dir = new File("bigTest/cpp");
		dir.mkdirs();

		dir = new File("bigTest/java");
		dir.mkdirs();

		// load the grid that will be used for all models generated during this
		// test.
		GeoTessGrid grid = new GeoTessGrid()
		.loadGrid(gridFile.getCanonicalPath());

		// specify some radii that will be used
		float radiusBottom = 6000F;
		float radiusTop = 6300F;
		float[] radii = new float[] { 6000F, 6100F, 6200F, 6300F };

		// build an array of Profiles that are independent of any model.
		// These profiles have no Data since the data will be replaced below.
		// They just have a ProfileType and some radii.
		Profile[][] profiles = new Profile[grid.getNVertices()][1];
		for (int vertex = 0; vertex < grid.getNVertices(); ++vertex)
		{
			// loop over the vertices. Get the latitude of the vertex and
			// determine the ProfileType based on the latitude. This will
			// results in 5 bands of different ProfileTypes.
			double lat = VectorGeo.getLatDegrees(grid.getVertex(vertex));

			int band = (int) Math.floor((lat + 90) / 180. * 3.999999);

			ProfileType pType = ProfileType.values()[band];

			switch (pType)
			{
				case CONSTANT:
					profiles[vertex][0] = new ProfileConstant(radiusBottom,
							radiusTop, null);
					break;
				case EMPTY:
					profiles[vertex][0] = new ProfileEmpty(radiusBottom, radiusTop);
					break;
				case NPOINT:
					profiles[vertex][0] = new ProfileNPoint(radii, getDoubles(lat,
							radii.length, 1));
					break;
				case THIN:
					profiles[vertex][0] = new ProfileThin(radiusTop, null);
					break;
				default:
					throw new GeoTessException(band
							+ " is not a recognized ProfileType");
			}

		}

		HashMap<String, Long> timers = new HashMap<String, Long>();
		//timers.put("nc", 0L);
		timers.put("ascii", 0L);
		timers.put("geotess", 0L);

		// for each data type (double, float, etc) and for single values or
		// array of values
		for (DataType dataType : DataType.values())
			if (dataType != DataType.CUSTOM)
				for (int nAttributes = 1; nAttributes <= 2; ++nAttributes)
				{
					// figure out the file name of this test.
					String name = (nAttributes > 1 ? "ArrayOf"
							+ dataType.toString() + "s" : dataType.toString())
							.toLowerCase();

					//System.out.println(name);

					// set up the metadata for the model
					GeoTessMetaData md = new GeoTessMetaData();
					md.setDescription("name");
					md.setLayerNames("testLayer");
					md.setDataType(dataType);
					if (nAttributes == 1)
						md.setAttributes("value1", "na");
					else
						md.setAttributes("value1;value2", "na1;na2");
					md.setModelSoftwareVersion("TestAll 1.0.0");
					md.setModelGenerationDate(new Date().toString());

					// build the model with the specified DataType, single value
					// or array.
					GeoTessModel model = new GeoTessModel(grid, md);

					// Specify the Profiles in the model. These are references
					// to the independent Profiles defined outside this loop, 
					// only with the Data objects replaced.
					for (int vertex = 0; vertex < model.getNVertices(); ++vertex)
					{
						double[] u = model.getGrid().getVertex(vertex);
						double value = u[0] * u[1] * u[2];
						//value = value > 0 ? 1-value : -1-value;

						Profile p = profiles[vertex][0];
						switch (dataType)
						{
							case DOUBLE:
								profiles[vertex][0].setData(getDoubles(value,
										p.getNData(), nAttributes));
								break;
							case FLOAT:
								profiles[vertex][0].setData(getFloats(value,
										p.getNData(), nAttributes));
								break;
							case LONG:
								profiles[vertex][0].setData(getLongs(value,
										p.getNData(), nAttributes));
								break;
							case INT:
								profiles[vertex][0].setData(getInts(value,
										p.getNData(), nAttributes));
								break;
							case SHORT:
								profiles[vertex][0].setData(getShorts(value,
										p.getNData(), nAttributes));
								break;
							case BYTE:
								profiles[vertex][0].setData(getBytes(value,
										p.getNData(), nAttributes));
								break;
						}
						// set the Profile in the model to a reference to the
						// computed value.
						model.setProfile(vertex, 0, p);
					}

					// now for each output file format write the model to a
					// file,read in the model back in to a new model, and 
					// compare them.
					for (String frmt : new String[] { "geotess", "ascii" /*, "nc" */ })
					{
						// figure out the file name for the output file
						File outFile = new File(dir, String.format("%s.%s",
								name, frmt));

						try
						{
							// save the model to file
							model.writeModel(outFile, gridFile.getName());

							long timer = System.nanoTime();

							// read the model back in
							GeoTessModel test_model = new GeoTessModel(outFile, "../", OptimizationType.MEMORY);

							timer = System.nanoTime() - timer;
							timers.put(frmt, timers.get(frmt) + timer);

							assertTrue(test_model.equals(model));

						}
						catch (Exception e)
						{
							e.printStackTrace();
						}
					}
				}
	}

	/**
	 * Big test. Create a model that has all 6 ProfileTypes represented. For
	 * every DataType, and for nAttributes = 1 and 2 (12 combinations) generate
	 * a model. For each output format (nc, ascii and bin) write the model to a
	 * file, read the model back into a new Model object and compare the loaded
	 * model with the original model. If they are not equal, report that they
	 * are not equal.
	 * 
	 * @param args
	 * @throws GeoTessException
	 * @throws IOException
	 */
	@Test
  @Ignore
	public void IOSurfaceTest() throws GeoTessException, IOException
	{
		File gridFile = new File("src/test/resources/permanent_files/geotess_grid_16000.geotess");

		File dir = new File("bigTest/cpp");
		dir.mkdirs();

		dir = new File("bigTest/java");
		dir.mkdirs();

		// load the grid that will be used for all models generated during this
		// test.
		GeoTessGrid grid = new GeoTessGrid()
		.loadGrid(gridFile.getCanonicalPath());

		HashMap<String, Long> timers = new HashMap<String, Long>();
		//timers.put("nc", 0L);
		timers.put("ascii", 0L);
		timers.put("geotess", 0L);

		// for each data type (double, float, etc) and for single values or
		// array of values
		for (DataType dataType : DataType.values())
			if (dataType != DataType.CUSTOM)
				for (int nAttributes = 1; nAttributes <= 2; ++nAttributes)
				{
					// figure out the file name of this test.
					String name = (nAttributes > 1 ? "ArrayOf"
							+ dataType.toString() + "s" : dataType.toString())
							.toLowerCase();

					name = name + "_surface";

					//System.out.println(name);

					// set up the metadata for the model
					GeoTessMetaData md = new GeoTessMetaData();
					md.setDescription("name");
					md.setLayerNames("testLayer");
					md.setDataType(dataType);
					if (nAttributes == 1)
						md.setAttributes("value1", "na");
					else
						md.setAttributes("value1;value2", "na1;na2");
					md.setModelSoftwareVersion("TestAll 1.0.0");
					md.setModelGenerationDate(new Date().toString());

					// build the model with the specified DataType, single value
					// or array.
					GeoTessModel model = new GeoTessModel(grid, md);

					Profile p;

					// Specify the Profiles in the model. These are references
					// to the independent Profiles defined outside this loop, 
					// only with the Data objects replaced.
					for (int vertex = 0; vertex < model.getNVertices(); ++vertex)
					{
						double[] u = model.getGrid().getVertex(vertex);
						double value = u[0] * u[1] * u[2];

						if (VectorGeo.getLat(u) < 0)
							model.setProfile(vertex);
						else
							switch (dataType)
							{
								case DOUBLE:
									model.setProfile(vertex, getDoubles(value, 1, nAttributes)[0]);
									break;
								case FLOAT:
									model.setProfile(vertex, getFloats(value, 1, nAttributes)[0]);
									break;
								case LONG:
									model.setProfile(vertex, getLongs(value, 1, nAttributes)[0]);
									break;
								case INT:
									model.setProfile(vertex, getInts(value, 1, nAttributes)[0]);
									break;
								case SHORT:
									model.setProfile(vertex, getShorts(value, 1, nAttributes)[0]);
									break;
								case BYTE:
									model.setProfile(vertex, getBytes(value, 1, nAttributes)[0]);
									break;
							}						
					}

					// now for each output file format write the model to a
					// file,read in the model back in to a new model, and 
					// compare them.
					for (String frmt : new String[] { "ascii", "geotess" })
					{
						// figure out the file name for the output file
						File outFile = new File(dir, String.format("%s.%s",
								name, frmt));

						//System.out.println(name);

						// save the model to file
						model.writeModel(outFile, gridFile.getName());

						long timer = System.nanoTime();

						// read the model back in
						GeoTessModel test_model = new GeoTessModel(outFile, "../", 
								OptimizationType.MEMORY);

						timer = System.nanoTime() - timer;
						timers.put(frmt, timers.get(frmt) + timer);

						assertTrue(test_model.equals(model));

					}
				}
	}

	/**
	 * Performs GeoTessModel gradient calculation at a specific grid node
	 * location and validates the result against a previously validated true
	 * result. All three argument forms are tested.
	 * 
	 * @throws GeoTessException
	 */
	@Test
	public void testGetGradients() throws GeoTessException
	{
		// set the active region and get a set of layers for which gradients are to
		// be calculated
		int[] layers = {2, 3, 4, 5, 6, 7, 8};
		model.setActiveRegion();

		// set grid node location indices and compute the gradients
		int     attributeIndex = 0;
		boolean reciprocal     = true; 
    int layerId = 4;
    int nodeIndex = 1;
    int vertexIndex = 0;
    int pointIndex = 94;
    double radius  = 6002.0;
		model.computeGradients(attributeIndex, reciprocal, layers);

		// set the previously evaluated truth for the gradients at the requested
		// location and create the vectors to hold the gradients
		double[] gradRadiusTrue = {1.9839115843623915E-6, -4.022375220857291E-6, -0.003656604048942139};
		double[] gradNodeTrue   = {4.756547818175395E-6, -9.62857379398759E-6, -0.0033950085275608875};
		double[] gradPointTrue  = {4.756547818175395E-6, -9.62857379398759E-6, -0.0033950085275608875};
    
		double[] gradRadius = new double [3];
		double[] gradNode   = new double [3];
		double[] gradPoint  = new double [3];

		// perform the vertexIndex/layerId/nodeIndex gradient method and test for
		// the true result
		model.getGradient(vertexIndex, layerId, nodeIndex, attributeIndex, reciprocal, gradNode);
		assertEquals(gradNode[0], gradNodeTrue[0], 1e-12);
		assertEquals(gradNode[1], gradNodeTrue[1], 1e-12);
		assertEquals(gradNode[2], gradNodeTrue[2], 1e-12);

		// perform the pointIndex gradient method and test for
		// the true result
		model.getPointGradient(pointIndex, attributeIndex, reciprocal, gradPoint);
		assertEquals(gradPoint[0], gradPointTrue[0], 1e-12);
		assertEquals(gradPoint[1], gradPointTrue[1], 1e-12);
		assertEquals(gradPoint[2], gradPointTrue[2], 1e-12);

		// perform the vertexIndex/layerId/radius gradient method and test for
		// the true result
		model.getGradient(vertexIndex, layerId, radius, attributeIndex, reciprocal, gradRadius);
		assertEquals(gradRadius[0], gradRadiusTrue[0], 1e-12);
		assertEquals(gradRadius[1], gradRadiusTrue[1], 1e-12);
		assertEquals(gradRadius[2], gradRadiusTrue[2], 1e-12);
	}

	/**
	 * Simple test to evaluate the layer normals at a position grid node index
	 * and verify that it has not changed from the true result. Both unit and
	 * area facet weighting are tested.
	 * 
	 * @throws GeoTessException
	 */
	@Test
	public void testGetLayerNormals() throws GeoTessException
	{
		// set global active region
		model.setActiveRegion();

		// pick a location
    int layerId = 4;
    int vertexIndex = 0;
    int pointIndex = 94;

    // compute unit weighted normals ... validate against the true result
		model.computeLayerNormals(false);
		double[] normTrue = {0.003866655017722743, -0.009437102685152304, 0.9999479936836134};
		double[] normp = model.getLayerNormal(vertexIndex, layerId);
		double[] normvl = model.getLayerNormal(pointIndex);
		assertEquals(normp[0], normTrue[0], 1e-12);
		assertEquals(normp[0], normvl[0], 1e-12);

    // compute area weighted normals ... validate against the true result
		model.computeLayerNormals(true);
		double[] normTrueA = {0.003844847699526512, -0.00938031231004458, 0.9999486121232097};
		normp = model.getLayerNormal(vertexIndex, layerId);
		normvl = model.getLayerNormal(pointIndex);
		assertEquals(normp[0], normTrueA[0], 1e-12);
		assertEquals(normp[0], normvl[0], 1e-12);
	}

	/**
	 * This test looks for GeoTessModel files in bigTest/java and 
	 * bigTest/cpp subdirectories that have the same name and tests
	 * them for equality.  
	 * @throws IOException 
	 */
	@Test
  @Ignore
	public void compareCPPFiles() throws IOException 
	{
		File javaDir = new File("bigTest/java");
		File cppDir = new File("bigTest/cpp");

		javaDir.mkdirs();
		cppDir.mkdirs();

		int nCpp = 0;
		for (File f : cppDir.listFiles())
			if (f.isFile() && GeoTessModel.isGeoTessModel(f))
				++nCpp;

		ArrayList<String> files = new ArrayList<String>(100);

		for (File javaFile : javaDir.listFiles())
			if (javaFile.isFile() && GeoTessModel.isGeoTessModel(javaFile))
				files.add(javaFile.getCanonicalPath());

		Collections.sort(files);

		int nCompared = 0;

		for (String s : files)
		{
			File javaFile = new File(s);
			File cppFile = new File(cppDir, javaFile.getName());
			
			//System.out.println(javaFile.getCanonicalPath());
			
			if (cppFile.exists())
			{
				GeoTessModel javamodel = new GeoTessModel(javaFile, "../../resources/permanent_files/",
						OptimizationType.MEMORY);

				GeoTessModel cppmodel = new GeoTessModel(cppFile, "../../resources/permanent_files/",
						OptimizationType.MEMORY);

				boolean eq = javamodel.equals(cppmodel);

				//System.out.printf("Comparing files %s and %s%n", javaFile.getCanonicalFile(), cppFile.getCanonicalPath());

				if (!eq)
					System.out.printf("File %s are not equal%n", s);

				assertTrue(eq);

				++nCompared;
			}

		}

//		System.out.printf("    compareCPPFiles(): Njava=%d Ncpp=%d Ncomparisons=%d%n", 
//				files.size(), nCpp, nCompared);
	}

	/**
	 * Get either a DataDouble[nPoints] or DataArrayOfDoubles[nPoints] where the
	 * array has 2 elements. The values are random values between -1 and 1.
	 * 
	 * @param npoints
	 * @param nattributes
	 * @return
	 */
	private Data[] getDoubles(double value, int npoints, int nattributes)
	{
		Data[] data = new Data[npoints];
		for (int i = 0; i < npoints; i++)
			data[i] = nattributes == 1 ? new DataDouble(value)
		: new DataArrayOfDoubles(new double[] { value, value });
			return data;
	}

	/**
	 * Get either a DataFloat[nPoints] or DataArrayOfFloats[nPoints] where the
	 * array has 2 elements. The values are random values between -1 and 1.
	 * 
	 * @param npoints
	 * @param nattributes
	 * @return
	 */
	private Data[] getFloats(double value, int npoints, int nattributes)
	{
		Data[] data = new Data[npoints];
		for (int i = 0; i < npoints; i++)
			data[i] = nattributes == 1 ? new DataFloat((float) value)
		: new DataArrayOfFloats(new float[] { (float) value,
				(float) value });
			return data;
	}

	/**
	 * Get either a DataLong[nPoints] or DataArrayOfLongs[nPoints] where the
	 * array has 2 elements. The values are random values between min_value and
	 * max_value.
	 * 
	 * @param npoints
	 * @param nattributes
	 * @return
	 */
	private Data[] getLongs(double value, int npoints, int nattributes)
	{
		Data[] data = new Data[npoints];
		long val = (long) (value * Long.MAX_VALUE);
		for (int i = 0; i < npoints; i++)
			data[i] = nattributes == 1 ? new DataLong(val)
		: new DataArrayOfLongs(new long[] { val, val });
			return data;
	}

	/**
	 * Get either a DataInt[nPoints] or DataArrayOfIntss[nPoints] where the
	 * array has 2 elements. The values are random values between min_value and
	 * max_value.
	 * 
	 * @param npoints
	 * @param nattributes
	 * @return
	 */
	private Data[] getInts(double value, int npoints, int nattributes)
	{
		Data[] data = new Data[npoints];
		int val = (int) (value * Integer.MAX_VALUE);
		for (int i = 0; i < npoints; i++)
			data[i] = nattributes == 1 ? new DataInt(val)
		: new DataArrayOfInts(new int[] { val, val });
			return data;
	}

	/**
	 * Get either a DataShort[nPoints] or DataArrayOfShorts[nPoints] where the
	 * array has 2 elements. The values are random values between min_value and
	 * max_value.
	 * 
	 * @param npoints
	 * @param nattributes
	 * @return
	 */
	private Data[] getShorts(double value, int npoints, int nattributes)
	{
		Data[] data = new Data[npoints];
		short val = (short) (value * Short.MAX_VALUE);
		for (int i = 0; i < npoints; i++)
			data[i] = nattributes == 1 ? new DataShort(val)
		: new DataArrayOfShorts(new short[] { val, val });
			return data;
	}

	/**
	 * Get either a DataByte[nPoints] or DataArrayOfBytes[nPoints] where the
	 * array has 2 elements. The values are random values between min_value and
	 * max_value.
	 * 
	 * @param npoints
	 * @param nattributes
	 * @return
	 */
	private Data[] getBytes(double value, int npoints, int nattributes)
	{
		Data[] data = new Data[npoints];
		byte val = (byte) (value * Byte.MAX_VALUE);
		for (int i = 0; i < npoints; i++)
			data[i] = nattributes == 1 ? new DataByte(val)
		: new DataArrayOfBytes(new byte[] { val, val });
			return data;
	}

}
