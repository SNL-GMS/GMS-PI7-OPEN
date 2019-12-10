package gms.shared.utilities.geotess;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import gms.shared.utilities.geotess.util.globals.DataType;
import gms.shared.utilities.geotess.util.globals.OptimizationType;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import org.junit.Before;
import org.junit.Test;

public class GeoTessMetaDataTest
{
	private GeoTessModel modelCrust20;

	private GeoTessMetaData crust20;

	private GeoTessMetaData unified;

	@Before
	public void setUpBeforeClass() throws Exception
	{

		modelCrust20 = new GeoTessModel(new File("src/test/resources/permanent_files/crust20.geotess"));

		crust20 = modelCrust20.getMetaData();

		unified = new GeoTessModel(new File("src/test/resources/permanent_files/unified_crust20_ak135.geotess")).getMetaData();

	}

	@Test
	public void testCopy()
	{
		GeoTessMetaData other = crust20.copy();

		assertTrue(other.equals(crust20));
	}

	@Test
	public void testGetMetaDataFile() throws IOException 
	{
		GeoTessMetaData other = GeoTessMetaData.getMetaData(
				new File("src/test/resources/permanent_files/crust20.geotess"));

		assertTrue(other.equals(crust20));
	}

	@Test
	public void testCheckComplete() throws GeoTessException
	{
		crust20.checkComplete();
	}

	@Test
	public void testEquals()
	{
		GeoTessMetaData other = crust20.copy();

		assertNotSame(crust20, other);

		assertEquals(crust20, other);

		assertFalse(crust20.equals(null));

		assertFalse(crust20.equals(new Object()));

		assertFalse(crust20.equals(unified));

	}

	@Test
	public void testGetInputModelFile() throws IOException
	{
		assertTrue(crust20.getInputModelFile().getCanonicalPath().endsWith("crust20.geotess"));
	}

	@Test
	public void testGetLoadTimeModel()
	{
		assertTrue(crust20.getLoadTimeModel() > 0. && crust20.getLoadTimeModel() < 1000.);
	}

	@Test
	public void testGetOutputModelFile() throws IOException
	{

		File output = new File("src/test/resources/permanent_files/junk.deleteme");

		modelCrust20.writeModel(output);

		assertEquals(output.getCanonicalPath(), crust20.getOutputModelFile());

		output.delete();

		assertTrue(crust20.getWriteTimeModel() > 0. && crust20.getWriteTimeModel() < 1000.);

	}

	@Test
	public void testSetAttributesStringArrayStringArray()
	{
		GeoTessMetaData other = new GeoTessMetaData();

		other.setAttributes("vp; vs; density".split(";"), "km/sec; km/sec; g/cc".split(";"));

		assertEquals("vp; vs; density", other.getAttributeNamesString());

		assertEquals("km/sec; km/sec; g/cc", other.getAttributeUnitsString());

		assertEquals(3, other.getNAttributes());
	}

	@Test(expected=IllegalArgumentException.class)
	public void testSetAttributesStringArrayStringArrayFails()
	{
		GeoTessMetaData other = new GeoTessMetaData();

		// throws exception because number of names != number of units.
		other.setAttributes("vp; vs; density".split(";"), "km/sec; km/sec".split(";"));	
	}

	@Test
	public void testSetAttributesStringString()
	{
		GeoTessMetaData other = new GeoTessMetaData();

		other.setAttributes("vp; vs; density", "km/sec; km/sec; g/cc");

		assertEquals("vp; vs; density", other.getAttributeNamesString());

		assertEquals("km/sec; km/sec; g/cc", other.getAttributeUnitsString());

		assertEquals(3, other.getNAttributes());
	}

	@Test(expected=IllegalArgumentException.class)
	public void testSetAttributesStringStringFails()
	{
		new GeoTessMetaData().setAttributes("vp; vs; density", "km/sec; km/sec");	
	}

	@Test
	public void testGetAttributeNamesString()
	{
		assertEquals("vp; vs; density", crust20.getAttributeNamesString());
	}

	@Test
	public void testGetAttributeUnitsString()
	{
		assertEquals("km/sec; km/sec; g/cc", crust20.getAttributeUnitsString());
	}

	@Test
	public void testGetLayerNamesString()
	{
		assertEquals("mantle;lower_crust;middle_crust;upper_crust;hard_sediments;soft_sediments;ice", 
				crust20.getLayerNamesString());
	}

	@Test
	public void testGetNAttributes()
	{
		assertEquals(3, crust20.getNAttributes());
	}

	@Test
	public void testGetAttributeNames()
	{
		assertArrayEquals(new String[] {"vp", "vs", "density"}, crust20.getAttributeNames());
	}

	@Test
	public void testGetAttributeNamesCollectionOfString()
	{
		HashSet<String> attributes = new HashSet<String>();

		attributes.add("junk");

		crust20.getAttributeNames(attributes);

		assertEquals(4, attributes.size());

		assertTrue(attributes.contains("junk"));

		assertTrue(attributes.contains("vp"));
		assertTrue(attributes.contains("vs"));
		assertTrue(attributes.contains("density"));

		assertFalse(attributes.contains("not an attribute"));

	}

	@Test
	public void testGetAttributeIndex()
	{
		assertEquals(0, crust20.getAttributeIndex("vp"));
		assertEquals(1, crust20.getAttributeIndex("vs"));
		assertEquals(2, crust20.getAttributeIndex("density"));

		assertEquals(-1, crust20.getAttributeIndex("not an attribute"));
	}

	@Test
	public void testGetAttributeUnits()
	{
		//System.out.println(Arrays.toString(metaData.getAttributeUnits()));
		assertArrayEquals(new String[] {"km/sec", "km/sec", "g/cc"}, crust20.getAttributeUnits());
	}

	@Test
	public void testGetAttributeName()
	{
		assertEquals("vp", crust20.getAttributeName(0));
		assertEquals("vs", crust20.getAttributeName(1));
		assertEquals("density", crust20.getAttributeName(2));
	}

	@Test(expected=ArrayIndexOutOfBoundsException.class)
	public void testGetAttributeNameFails1()
	{
		crust20.getAttributeName(-1);	
	}

	@Test(expected=ArrayIndexOutOfBoundsException.class)
	public void testGetAttributeNameFails2()
	{
		crust20.getAttributeName(3);	
	}

	@Test
	public void testGetAttributeUnit()
	{
		assertEquals("km/sec", crust20.getAttributeUnit(0));
		assertEquals("km/sec", crust20.getAttributeUnit(1));
		assertEquals("g/cc", crust20.getAttributeUnit(2));
	}

	@Test(expected=ArrayIndexOutOfBoundsException.class)
	public void testGetAttributeUnitFails1()
	{
		crust20.getAttributeUnit(-1);	
	}

	@Test(expected=ArrayIndexOutOfBoundsException.class)
	public void testGetAttributeUnitFails2()
	{
		crust20.getAttributeUnit(3);	
	}

	@Test
	public void testGetDescription()
	{
		assertTrue(crust20.getDescription().length() > 0);
	}

	@Test
	public void testSetDescription()
	{
		String description = crust20.getDescription();

		crust20.setDescription("junk");

		assertEquals("junk", crust20.getDescription());

		crust20.setDescription(description);

		assertEquals(description, crust20.getDescription());
	}

	@Test
	public void testGetNLayers()
	{
		assertEquals(7, crust20.getNLayers());
	}

	@Test
	public void testGetLayerNames()
	{
		//System.out.println(Arrays.toString(metaData.getLayerNames()));
		assertArrayEquals(new String[] {"mantle", "lower_crust", "middle_crust", 
				"upper_crust", "hard_sediments", "soft_sediments", "ice"},
				crust20.getLayerNames());
	}

	@Test
	public void testSetLayerNamesString()
	{
		//System.out.println(metaData.getLayerNamesString());
		assertEquals("mantle;lower_crust;middle_crust;upper_crust;hard_sediments;soft_sediments;ice",
				crust20.getLayerNamesString());
	}

	@Test
	public void testSetLayerNamesStringArray() throws IOException
	{
		GeoTessMetaData other = new GeoTessMetaData();
		other.setLayerNames(new String[] {"layer0", "layer1", "layer2"});

		assertEquals(3, other.getNLayers());

		assertEquals("layer0", other.getLayerNames()[0]);
		assertEquals(0, other.getLayerIndex("layer0"));

		assertEquals("layer1", other.getLayerNames()[1]);
		assertEquals(1, other.getLayerIndex("layer1"));

		assertEquals("layer2", other.getLayerNames()[2]);
		assertEquals(2, other.getLayerIndex("layer2"));

	}

	@Test
	public void testGetLayerIndex()
	{
		assertEquals(0, crust20.getLayerIndex("mantle"));
		assertEquals(1, crust20.getLayerIndex("lower_crust"));
		assertEquals(2, crust20.getLayerIndex("middle_crust"));
		assertEquals(3, crust20.getLayerIndex("upper_crust"));
		assertEquals(4, crust20.getLayerIndex("hard_sediments"));
		assertEquals(5, crust20.getLayerIndex("soft_sediments"));
		assertEquals(6, crust20.getLayerIndex("ice"));
	}

	@Test
	public void testGetLayerTessIds()
	{
		//System.out.println(Arrays.toString(metaData.getLayerTessIds()));
		assertArrayEquals(new int[7], crust20.getLayerTessIds());
	}

	@Test
	public void testSetLayerTessIds() throws IOException
	{
		int[] ids = new int[] {0, 0, 1, 1, 2, 2, 3};

		GeoTessMetaData other = new GeoTessMetaData();

		other.setLayerTessIds(ids.clone());

		assertArrayEquals(ids.clone(), other.getLayerTessIds());
	}

	@Test
	public void testGetLayers()
	{
		assertArrayEquals(new int[] {0}, unified.getLayers(0));
		assertArrayEquals(new int[] {1,2,3,4}, unified.getLayers(1));
		assertArrayEquals(new int[] {5,6,7,8}, unified.getLayers(2));

		assertArrayEquals(new int[] {}, unified.getLayers(-1));
		assertArrayEquals(new int[] {}, unified.getLayers(3));

	}

	@Test
	public void testGetLastLayer()
	{
		assertEquals(0, unified.getLastLayer(0));
		assertEquals(4, unified.getLastLayer(1));
		assertEquals(8, unified.getLastLayer(2));

		assertEquals(-1, unified.getLastLayer(-1));
		assertEquals(-1, unified.getLastLayer(3));
	}

	@Test
	public void testGetFirstLayer()
	{
		assertEquals(0, unified.getFirstLayer(0));
		assertEquals(1, unified.getFirstLayer(1));
		assertEquals(5, unified.getFirstLayer(2));

		assertEquals(-1, unified.getFirstLayer(-1));
		assertEquals(-1, unified.getFirstLayer(3));
	}

	@Test
	public void testGetTessellation()
	{
		for (int layer=0; layer<7; ++layer)
			assertEquals(0, crust20.getTessellation(layer));

		assertEquals(0, unified.getTessellation(0));
		assertEquals(1, unified.getTessellation(1));
		assertEquals(1, unified.getTessellation(2));
		assertEquals(1, unified.getTessellation(3));
		assertEquals(1, unified.getTessellation(4));
		assertEquals(2, unified.getTessellation(5));
		assertEquals(2, unified.getTessellation(6));
		assertEquals(2, unified.getTessellation(7));
		assertEquals(2, unified.getTessellation(8));
	}

	@Test
	public void testGetDataType()
	{
		assertEquals("FLOAT", crust20.getDataType().toString());
		assertEquals("FLOAT", unified.getDataType().toString());
	}

	@Test
	public void testSetDataTypeDataType()
	{
		GeoTessMetaData other = new GeoTessMetaData();

		other.setDataType(DataType.DOUBLE);
		assertEquals("DOUBLE", other.getDataType().toString());

		other.setDataType(DataType.FLOAT);
		assertEquals("FLOAT", other.getDataType().toString());

		other.setDataType(DataType.LONG);
		assertEquals("LONG", other.getDataType().toString());

		other.setDataType(DataType.INT);
		assertEquals("INT", other.getDataType().toString());

		other.setDataType(DataType.SHORT);
		assertEquals("SHORT", other.getDataType().toString());

		other.setDataType(DataType.BYTE);
		assertEquals("BYTE", other.getDataType().toString());

	}

	@Test
	public void testSetDataTypeString() throws IOException
	{
		GeoTessMetaData other = new GeoTessMetaData();

		other.setDataType("DOUBLE");
		assertEquals("DOUBLE", other.getDataType().toString());

		other.setDataType("FLOAT");
		assertEquals("FLOAT", other.getDataType().toString());

		other.setDataType("LONG");
		assertEquals("LONG", other.getDataType().toString());

		other.setDataType("INT");
		assertEquals("INT", other.getDataType().toString());

		other.setDataType("SHORT");
		assertEquals("SHORT", other.getDataType().toString());

		other.setDataType("BYTE");
		assertEquals("BYTE", other.getDataType().toString());

		other.setDataType("byte");
		assertEquals("BYTE", other.getDataType().toString());
	}

	@Test(expected=Exception.class)
	public void testSetDataTypeStringFails() throws IOException
	{
		GeoTessMetaData other = new GeoTessMetaData();
		other.setDataType("JUNK");
	}

	@Test
	public void testOptimization() throws GeoTessException
	{
		GeoTessMetaData other = new GeoTessMetaData();
		other.setOptimization(OptimizationType.SPEED);
		assertEquals(OptimizationType.SPEED, other.getOptimization());
		other.setOptimization(OptimizationType.MEMORY);
		assertEquals(OptimizationType.MEMORY, other.getOptimization());
	}

	@Test
	public void testGridReuse()
	{
		GeoTessMetaData other = new GeoTessMetaData();
		assertTrue(other.isGridReuseOn());

		other.setReuseGrids(false);
		assertFalse(other.isGridReuseOn());

		other.setReuseGrids(true);
		assertTrue(other.isGridReuseOn());

	}

	@Test
	public void testModelSoftwareVersion()
	{
		//System.out.println(crust20.getModelSoftwareVersion());
		assertEquals("GeoModel 7.0.1", crust20.getModelSoftwareVersion());

		GeoTessMetaData other = new GeoTessMetaData();
		other.setModelSoftwareVersion("GeoTessMetaDataTest");
		assertEquals("GeoTessMetaDataTest", other.getModelSoftwareVersion());

	}

	@Test
	public void testGetModelGenerationDate()
	{
		//System.out.println(crust20.getModelGenerationDate());
		assertEquals("Wed April 18 15:21:51 2012", crust20.getModelGenerationDate());

		GeoTessMetaData other = new GeoTessMetaData();
		other.setModelGenerationDate("tuesday");
		assertEquals("tuesday", other.getModelGenerationDate());

	}

}
