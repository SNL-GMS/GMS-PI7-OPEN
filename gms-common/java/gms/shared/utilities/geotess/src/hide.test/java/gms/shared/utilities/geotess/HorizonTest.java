package gms.shared.utilities.geotess;

import static org.junit.Assert.assertEquals;

import gms.shared.utilities.geotess.util.globals.InterpolatorType;
import gms.shared.utilities.geotess.util.numerical.polygon.GreatCircle.GreatCircleException;
import gms.shared.utilities.geotess.util.numerical.polygon.Horizon;
import gms.shared.utilities.geotess.util.numerical.polygon.HorizonDepth;
import gms.shared.utilities.geotess.util.numerical.polygon.HorizonLayer;
import gms.shared.utilities.geotess.util.numerical.polygon.HorizonRadius;
import java.io.File;
import org.junit.Before;
import org.junit.Test;

public class HorizonTest
{
	private GeoTessModel model;

	@Before
	public void setUpBeforeClass() throws Exception
	{
		System.out.println("HorizonTest");

		model = new GeoTessModel(
				new File("src/test/resources/permanent_files/unified_crust20_ak135.geotess"));

		//System.out.println(model);

	}

	@Test
	public void testHorizonRadius1() throws GreatCircleException, GeoTessException
	{
		Horizon h = new HorizonRadius(6315., 4);
		
		GeoTessPosition pos = model.getGeoTessPosition(InterpolatorType.LINEAR);
		
//		for (int i=15; i<=50; i+=5)
//		{
//			pos.set(4, i, 90., 0.);
//			System.out.printf("pos.set(4, %d., 90., 0.);\nassertEquals(%1.3f, h.getRadius(pos), 1e-3);\n\n", 
//					i, h.getRadius(pos));
//		}

		pos.set(4, 15., 90., 0.);
		assertEquals(6315.000, h.getRadius(pos.getVector(), pos.getLayerRadii()), 1e-3);

		pos.set(4, 20., 90., 0.);
		assertEquals(6315.000, h.getRadius(pos.getVector(), pos.getLayerRadii()), 1e-3);

		pos.set(4, 25., 90., 0.);
		assertEquals(6315.000, h.getRadius(pos.getVector(), pos.getLayerRadii()), 1e-3);

		pos.set(4, 30., 90., 0.);
		assertEquals(6301.656, h.getRadius(pos.getVector(), pos.getLayerRadii()), 1e-3);

		pos.set(4, 35., 90., 0.);
		assertEquals(6303.515, h.getRadius(pos.getVector(), pos.getLayerRadii()), 1e-3);

		pos.set(4, 40., 90., 0.);
		assertEquals(6315.000, h.getRadius(pos.getVector(), pos.getLayerRadii()), 1e-3);

		pos.set(4, 45., 90., 0.);
		assertEquals(6315.000, h.getRadius(pos.getVector(), pos.getLayerRadii()), 1e-3);

		pos.set(4, 50., 90., 0.);
		assertEquals(6315.000, h.getRadius(pos.getVector(), pos.getLayerRadii()), 1e-3);

	}

	@Test
	public void testHorizonRadius2() throws GreatCircleException, GeoTessException
	{
		Horizon h = new HorizonRadius(6315.);
		
		GeoTessPosition pos = model.getGeoTessPosition(InterpolatorType.LINEAR);
		
//		for (int i=15; i<=50; i+=5)
//		{
//			pos.set(4, i, 90., 0.);
//			System.out.printf("pos.set(4, %d., 90., 0.);\nassertEquals(%1.3f, h.getRadius(pos), 1e-3);\n\n", 
//					i, h.getRadius(pos));
//		}

		pos.set(4, 15., 90., 0.);
		assertEquals(6315.000, h.getRadius(pos.getVector(), pos.getLayerRadii()), 1e-3);

		pos.set(4, 20., 90., 0.);
		assertEquals(6315.000, h.getRadius(pos.getVector(), pos.getLayerRadii()), 1e-3);

		pos.set(4, 25., 90., 0.);
		assertEquals(6315.000, h.getRadius(pos.getVector(), pos.getLayerRadii()), 1e-3);

		pos.set(4, 30., 90., 0.);
		assertEquals(6315.000, h.getRadius(pos.getVector(), pos.getLayerRadii()), 1e-3);

		pos.set(4, 35., 90., 0.);
		assertEquals(6315.000, h.getRadius(pos.getVector(), pos.getLayerRadii()), 1e-3);

		pos.set(4, 40., 90., 0.);
		assertEquals(6315.000, h.getRadius(pos.getVector(), pos.getLayerRadii()), 1e-3);

		pos.set(4, 45., 90., 0.);
		assertEquals(6315.000, h.getRadius(pos.getVector(), pos.getLayerRadii()), 1e-3);

		pos.set(4, 50., 90., 0.);
		assertEquals(6315.000, h.getRadius(pos.getVector(), pos.getLayerRadii()), 1e-3);

	}

	@Test
	public void testHorizonDepth1() throws GreatCircleException, GeoTessException
	{
		Horizon h = new HorizonDepth(60., 4);
		
		GeoTessPosition pos = model.getGeoTessPosition(InterpolatorType.LINEAR);
		
//		for (int i=15; i<=50; i+=5)
//		{
//			pos.set(4, i, 90., 0.);
//			System.out.printf("pos.set(4, %d., 90., 0.);\nassertEquals(%1.3f, h.getRadius(pos), 1e-3);\n\n", 
//					i, h.getRadius(pos));
//		}

		pos.set(4, 15., 90., 0.);
		assertEquals(6316.716, h.getRadius(pos.getVector(), pos.getLayerRadii()), 1e-3);

		pos.set(4, 20., 90., 0.);
		assertEquals(6315.654, h.getRadius(pos.getVector(), pos.getLayerRadii()), 1e-3);

		pos.set(4, 25., 90., 0.);
		assertEquals(6314.344, h.getRadius(pos.getVector(), pos.getLayerRadii()), 1e-3);

		pos.set(4, 30., 90., 0.);
		assertEquals(6301.656, h.getRadius(pos.getVector(), pos.getLayerRadii()), 1e-3);

		pos.set(4, 35., 90., 0.);
		assertEquals(6303.515, h.getRadius(pos.getVector(), pos.getLayerRadii()), 1e-3);

		pos.set(4, 40., 90., 0.);
		assertEquals(6309.345, h.getRadius(pos.getVector(), pos.getLayerRadii()), 1e-3);

		pos.set(4, 45., 90., 0.);
		assertEquals(6307.490, h.getRadius(pos.getVector(), pos.getLayerRadii()), 1e-3);

		pos.set(4, 50., 90., 0.);
		assertEquals(6305.632, h.getRadius(pos.getVector(), pos.getLayerRadii()), 1e-3);

	}

	@Test
	public void testHorizonDepth2() throws GreatCircleException, GeoTessException
	{
		Horizon h = new HorizonDepth(60.);
		
		GeoTessPosition pos = model.getGeoTessPosition(InterpolatorType.LINEAR);
		
//		for (int i=15; i<=50; i+=5)
//		{
//			pos.set(4, i, 90., 0.);
//			System.out.printf("pos.set(4, %d., 90., 0.);\nassertEquals(%1.3f, h.getRadius(pos), 1e-3);\n\n", 
//					i, h.getRadius(pos));
//		}

		pos.set(4, 15., 90., 0.);
		assertEquals(6316.716, h.getRadius(pos.getVector(), pos.getLayerRadii()), 1e-3);

		pos.set(4, 20., 90., 0.);
		assertEquals(6315.654, h.getRadius(pos.getVector(), pos.getLayerRadii()), 1e-3);

		pos.set(4, 25., 90., 0.);
		assertEquals(6314.344, h.getRadius(pos.getVector(), pos.getLayerRadii()), 1e-3);

		pos.set(4, 30., 90., 0.);
		assertEquals(6312.824, h.getRadius(pos.getVector(), pos.getLayerRadii()), 1e-3);

		pos.set(4, 35., 90., 0.);
		assertEquals(6311.141, h.getRadius(pos.getVector(), pos.getLayerRadii()), 1e-3);

		pos.set(4, 40., 90., 0.);
		assertEquals(6309.345, h.getRadius(pos.getVector(), pos.getLayerRadii()), 1e-3);

		pos.set(4, 45., 90., 0.);
		assertEquals(6307.490, h.getRadius(pos.getVector(), pos.getLayerRadii()), 1e-3);

		pos.set(4, 50., 90., 0.);
		assertEquals(6305.632, h.getRadius(pos.getVector(), pos.getLayerRadii()), 1e-3);

	}

	@Test
	public void testHorizonLayer1() throws GreatCircleException, GeoTessException
	{
		Horizon h = new HorizonLayer(1., 4);
		
		GeoTessPosition pos = model.getGeoTessPosition(InterpolatorType.LINEAR);
		
//		for (int i=15; i<=50; i+=5)
//		{
//			pos.set(4, i, 90., 0.);
//			System.out.printf("pos.set(4, %d., 90., 0.);\nassertEquals(%1.3f, h.getRadius(pos), 1e-3);\n\n", 
//					i, h.getRadius(pos));
//		}

		pos.set(4, 15., 90., 0.);
		assertEquals(6354.930, h.getRadius(pos.getVector(), pos.getLayerRadii()), 1e-3);

		pos.set(4, 20., 90., 0.);
		assertEquals(6346.065, h.getRadius(pos.getVector(), pos.getLayerRadii()), 1e-3);

		pos.set(4, 25., 90., 0.);
		assertEquals(6329.592, h.getRadius(pos.getVector(), pos.getLayerRadii()), 1e-3);

		pos.set(4, 30., 90., 0.);
		assertEquals(6301.656, h.getRadius(pos.getVector(), pos.getLayerRadii()), 1e-3);

		pos.set(4, 35., 90., 0.);
		assertEquals(6303.515, h.getRadius(pos.getVector(), pos.getLayerRadii()), 1e-3);

		pos.set(4, 40., 90., 0.);
		assertEquals(6317.169, h.getRadius(pos.getVector(), pos.getLayerRadii()), 1e-3);

		pos.set(4, 45., 90., 0.);
		assertEquals(6318.084, h.getRadius(pos.getVector(), pos.getLayerRadii()), 1e-3);

		pos.set(4, 50., 90., 0.);
		assertEquals(6319.772, h.getRadius(pos.getVector(), pos.getLayerRadii()), 1e-3);

	}

	@Test
	public void testHorizonRadius3() throws GreatCircleException, GeoTessException
	{
		Horizon h = new HorizonRadius(6315., 4);
		
		GeoTessPosition pos = model.getGeoTessPosition(InterpolatorType.LINEAR);
		
		int vertex;
		
//		for (int i=15; i<=50; i+=5)
//		{
//			vertex = pos.set(8, i, 90., 0.).getIndexOfClosestVertex();
//			
//			System.out.printf("vertex = pos.set(8, %d., 90., 0.).getIndexOfClosestVertex();\n" +
//					"assertEquals(%1.3f, h.getRadius(model.getGrid().getVertex(vertex), \n" +
//					"model.getProfiles(vertex)), 1e-3);\n\n", 
//			i, h.getRadius(model.getGrid().getVertex(vertex), model.getProfiles(vertex)), 1e-3);
//
//		}
		
		vertex = pos.set(8, 15., 90., 0.).getIndexOfClosestVertex();
		assertEquals(6315.000, h.getRadius(model.getGrid().getVertex(vertex), 
		model.getLayerRadii(vertex)), 1e-3);

		vertex = pos.set(8, 20., 90., 0.).getIndexOfClosestVertex();
		assertEquals(6315.000, h.getRadius(model.getGrid().getVertex(vertex), 
		model.getLayerRadii(vertex)), 1e-3);

		vertex = pos.set(8, 25., 90., 0.).getIndexOfClosestVertex();
		assertEquals(6315.000, h.getRadius(model.getGrid().getVertex(vertex), 
		model.getLayerRadii(vertex)), 1e-3);

		vertex = pos.set(8, 30., 90., 0.).getIndexOfClosestVertex();
		assertEquals(6302.885, h.getRadius(model.getGrid().getVertex(vertex), 
		model.getLayerRadii(vertex)), 1e-3);

		vertex = pos.set(8, 35., 90., 0.).getIndexOfClosestVertex();
		assertEquals(6305.069, h.getRadius(model.getGrid().getVertex(vertex), 
		model.getLayerRadii(vertex)), 1e-3);

		vertex = pos.set(8, 40., 90., 0.).getIndexOfClosestVertex();
		assertEquals(6315.000, h.getRadius(model.getGrid().getVertex(vertex), 
		model.getLayerRadii(vertex)), 1e-3);

		vertex = pos.set(8, 45., 90., 0.).getIndexOfClosestVertex();
		assertEquals(6315.000, h.getRadius(model.getGrid().getVertex(vertex), 
		model.getLayerRadii(vertex)), 1e-3);

		vertex = pos.set(8, 50., 90., 0.).getIndexOfClosestVertex();
		assertEquals(6315.000, h.getRadius(model.getGrid().getVertex(vertex), 
		model.getLayerRadii(vertex)), 1e-3);

	}

	@Test
	public void testHorizonRadius4() throws GreatCircleException, GeoTessException
	{
		Horizon h = new HorizonRadius(6315.);
		
		GeoTessPosition pos = model.getGeoTessPosition(InterpolatorType.LINEAR);
		
		int vertex;
		
//		for (int i=15; i<=50; i+=5)
//		{
//			vertex = pos.set(8, i, 90., 0.).getIndexOfClosestVertex();
//			
//			System.out.printf("vertex = pos.set(8, %d., 90., 0.).getIndexOfClosestVertex();\n" +
//					"assertEquals(%1.3f, h.getRadius(model.getGrid().getVertex(vertex), \n" +
//					"model.getProfiles(vertex)), 1e-3);\n\n", 
//			i, h.getRadius(model.getGrid().getVertex(vertex), model.getProfiles(vertex)), 1e-3);
//
//		}
		
		vertex = pos.set(8, 15., 90., 0.).getIndexOfClosestVertex();
		assertEquals(6315.000, h.getRadius(model.getGrid().getVertex(vertex), 
		model.getLayerRadii(vertex)), 1e-3);

		vertex = pos.set(8, 20., 90., 0.).getIndexOfClosestVertex();
		assertEquals(6315.000, h.getRadius(model.getGrid().getVertex(vertex), 
		model.getLayerRadii(vertex)), 1e-3);

		vertex = pos.set(8, 25., 90., 0.).getIndexOfClosestVertex();
		assertEquals(6315.000, h.getRadius(model.getGrid().getVertex(vertex), 
		model.getLayerRadii(vertex)), 1e-3);

		vertex = pos.set(8, 30., 90., 0.).getIndexOfClosestVertex();
		assertEquals(6315.000, h.getRadius(model.getGrid().getVertex(vertex), 
		model.getLayerRadii(vertex)), 1e-3);

		vertex = pos.set(8, 35., 90., 0.).getIndexOfClosestVertex();
		assertEquals(6315.000, h.getRadius(model.getGrid().getVertex(vertex), 
		model.getLayerRadii(vertex)), 1e-3);

		vertex = pos.set(8, 40., 90., 0.).getIndexOfClosestVertex();
		assertEquals(6315.000, h.getRadius(model.getGrid().getVertex(vertex), 
		model.getLayerRadii(vertex)), 1e-3);

		vertex = pos.set(8, 45., 90., 0.).getIndexOfClosestVertex();
		assertEquals(6315.000, h.getRadius(model.getGrid().getVertex(vertex), 
		model.getLayerRadii(vertex)), 1e-3);

		vertex = pos.set(8, 50., 90., 0.).getIndexOfClosestVertex();
		assertEquals(6315.000, h.getRadius(model.getGrid().getVertex(vertex), 
		model.getLayerRadii(vertex)), 1e-3);

	}

	@Test
	public void testHorizonDepth3() throws GreatCircleException, GeoTessException
	{
		Horizon h = new HorizonDepth(55., 4);
		
		GeoTessPosition pos = model.getGeoTessPosition(InterpolatorType.LINEAR);
		
		int vertex;
		
//		for (int i=15; i<=50; i+=5)
//		{
//			vertex = pos.set(8, i, 90., 0.).getIndexOfClosestVertex();
//			
//			System.out.printf("vertex = pos.set(8, %d., 90., 0.).getIndexOfClosestVertex();\n" +
//					"assertEquals(%1.3f, h.getRadius(model.getGrid().getVertex(vertex), \n" +
//					"model.getProfiles(vertex)), 1e-3);\n\n", 
//			i, h.getRadius(model.getGrid().getVertex(vertex), model.getProfiles(vertex)), 1e-3);
//
//		}
		
		vertex = pos.set(8, 15., 90., 0.).getIndexOfClosestVertex();
		assertEquals(6321.717, h.getRadius(model.getGrid().getVertex(vertex), 
		model.getLayerRadii(vertex)), 1e-3);

		vertex = pos.set(8, 20., 90., 0.).getIndexOfClosestVertex();
		assertEquals(6320.664, h.getRadius(model.getGrid().getVertex(vertex), 
		model.getLayerRadii(vertex)), 1e-3);

		vertex = pos.set(8, 25., 90., 0.).getIndexOfClosestVertex();
		assertEquals(6319.415, h.getRadius(model.getGrid().getVertex(vertex), 
		model.getLayerRadii(vertex)), 1e-3);

		vertex = pos.set(8, 30., 90., 0.).getIndexOfClosestVertex();
		assertEquals(6302.885, h.getRadius(model.getGrid().getVertex(vertex), 
		model.getLayerRadii(vertex)), 1e-3);

		vertex = pos.set(8, 35., 90., 0.).getIndexOfClosestVertex();
		assertEquals(6305.069, h.getRadius(model.getGrid().getVertex(vertex), 
		model.getLayerRadii(vertex)), 1e-3);

		vertex = pos.set(8, 40., 90., 0.).getIndexOfClosestVertex();
		assertEquals(6314.242, h.getRadius(model.getGrid().getVertex(vertex), 
		model.getLayerRadii(vertex)), 1e-3);

		vertex = pos.set(8, 45., 90., 0.).getIndexOfClosestVertex();
		assertEquals(6312.434, h.getRadius(model.getGrid().getVertex(vertex), 
		model.getLayerRadii(vertex)), 1e-3);

		vertex = pos.set(8, 50., 90., 0.).getIndexOfClosestVertex();
		assertEquals(6310.674, h.getRadius(model.getGrid().getVertex(vertex), 
		model.getLayerRadii(vertex)), 1e-3);

	}

	@Test
	public void testHorizonDepth4() throws GreatCircleException, GeoTessException
	{
		Horizon h = new HorizonDepth(60.);
		
		GeoTessPosition pos = model.getGeoTessPosition(InterpolatorType.LINEAR);
		
		int vertex;
		
//		for (int i=15; i<=50; i+=5)
//		{
//			vertex = pos.set(8, i, 90., 0.).getIndexOfClosestVertex();
//			
//			System.out.printf("vertex = pos.set(8, %d., 90., 0.).getIndexOfClosestVertex();\n" +
//					"assertEquals(%1.3f, h.getRadius(model.getGrid().getVertex(vertex), \n" +
//					"model.getProfiles(vertex)), 1e-3);\n\n", 
//			i, h.getRadius(model.getGrid().getVertex(vertex), model.getProfiles(vertex)), 1e-3);
//
//		}
		
		vertex = pos.set(8, 15., 90., 0.).getIndexOfClosestVertex();
		assertEquals(6316.717, h.getRadius(model.getGrid().getVertex(vertex), 
		model.getLayerRadii(vertex)), 1e-3);

		vertex = pos.set(8, 20., 90., 0.).getIndexOfClosestVertex();
		assertEquals(6315.664, h.getRadius(model.getGrid().getVertex(vertex), 
		model.getLayerRadii(vertex)), 1e-3);

		vertex = pos.set(8, 25., 90., 0.).getIndexOfClosestVertex();
		assertEquals(6314.415, h.getRadius(model.getGrid().getVertex(vertex), 
		model.getLayerRadii(vertex)), 1e-3);

		vertex = pos.set(8, 30., 90., 0.).getIndexOfClosestVertex();
		assertEquals(6312.932, h.getRadius(model.getGrid().getVertex(vertex), 
		model.getLayerRadii(vertex)), 1e-3);

		vertex = pos.set(8, 35., 90., 0.).getIndexOfClosestVertex();
		assertEquals(6311.000, h.getRadius(model.getGrid().getVertex(vertex), 
		model.getLayerRadii(vertex)), 1e-3);

		vertex = pos.set(8, 40., 90., 0.).getIndexOfClosestVertex();
		assertEquals(6309.242, h.getRadius(model.getGrid().getVertex(vertex), 
		model.getLayerRadii(vertex)), 1e-3);

		vertex = pos.set(8, 45., 90., 0.).getIndexOfClosestVertex();
		assertEquals(6307.434, h.getRadius(model.getGrid().getVertex(vertex), 
		model.getLayerRadii(vertex)), 1e-3);

		vertex = pos.set(8, 50., 90., 0.).getIndexOfClosestVertex();
		assertEquals(6305.674, h.getRadius(model.getGrid().getVertex(vertex), 
		model.getLayerRadii(vertex)), 1e-3);

	}

	@Test
	public void testHorizonLayer3() throws GreatCircleException, GeoTessException
	{
		Horizon h = new HorizonLayer(1., 4);
		
		GeoTessPosition pos = model.getGeoTessPosition(InterpolatorType.LINEAR);
		
		int vertex;
		
//		for (int i=15; i<=50; i+=5)
//		{
//			vertex = pos.set(8, i, 90., 0.).getIndexOfClosestVertex();
//			
//			System.out.printf("vertex = pos.set(8, %d., 90., 0.).getIndexOfClosestVertex();\n" +
//					"assertEquals(%1.3f, h.getRadius(model.getGrid().getVertex(vertex), \n" +
//					"model.getProfiles(vertex)), 1e-3);\n\n", 
//			i, h.getRadius(model.getGrid().getVertex(vertex), model.getProfiles(vertex)), 1e-3);
//
//		}
		
		vertex = pos.set(8, 15., 90., 0.).getIndexOfClosestVertex();
		assertEquals(6355.850, h.getRadius(model.getGrid().getVertex(vertex), 
		model.getLayerRadii(vertex)), 1e-3);

		vertex = pos.set(8, 20., 90., 0.).getIndexOfClosestVertex();
		assertEquals(6346.019, h.getRadius(model.getGrid().getVertex(vertex), 
		model.getLayerRadii(vertex)), 1e-3);

		vertex = pos.set(8, 25., 90., 0.).getIndexOfClosestVertex();
		assertEquals(6331.183, h.getRadius(model.getGrid().getVertex(vertex), 
		model.getLayerRadii(vertex)), 1e-3);

		vertex = pos.set(8, 30., 90., 0.).getIndexOfClosestVertex();
		assertEquals(6302.885, h.getRadius(model.getGrid().getVertex(vertex), 
		model.getLayerRadii(vertex)), 1e-3);

		vertex = pos.set(8, 35., 90., 0.).getIndexOfClosestVertex();
		assertEquals(6305.069, h.getRadius(model.getGrid().getVertex(vertex), 
		model.getLayerRadii(vertex)), 1e-3);

		vertex = pos.set(8, 40., 90., 0.).getIndexOfClosestVertex();
		assertEquals(6317.730, h.getRadius(model.getGrid().getVertex(vertex), 
		model.getLayerRadii(vertex)), 1e-3);

		vertex = pos.set(8, 45., 90., 0.).getIndexOfClosestVertex();
		assertEquals(6318.038, h.getRadius(model.getGrid().getVertex(vertex), 
		model.getLayerRadii(vertex)), 1e-3);

		vertex = pos.set(8, 50., 90., 0.).getIndexOfClosestVertex();
		assertEquals(6319.817, h.getRadius(model.getGrid().getVertex(vertex), 
		model.getLayerRadii(vertex)), 1e-3);

	}

}
