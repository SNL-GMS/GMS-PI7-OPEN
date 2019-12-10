package gms.shared.utilities.geotess;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import gms.shared.utilities.geotess.examples.GeoTessModelExtended;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;
import org.junit.Test;

public class GeoTessModelExtendedTest
{

	@Test
	public void testExtendedModel()
	{
		try
		{
			String resource = "/permanent_files/unified_crust20_ak135.geotess";
			
			InputStream inp = GeoTessModelExtendedTest.class.getResourceAsStream(resource);
			
			if (inp == null)
				throw new IOException("Cannot find resource "+resource);
			
			GeoTessModel baseModel = new GeoTessModel(new DataInputStream(inp));

			GeoTessModelExtended model = new GeoTessModelExtended(baseModel);
			
			assertEquals(model.getExtraData(), "extraData initialized in GeoTessModelExtended.initializeData()");
			
			model.setExtraData("modified value");
						
			assertEquals(model.getExtraData(), "modified value");
			
			// write model to byte array in binary format
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			model.writeModelBinary(baos);
			
			GeoTessModelExtended newModel = new GeoTessModelExtended(new DataInputStream(
					new ByteArrayInputStream(baos.toByteArray())));

			assertTrue(newModel.equals(model));
			
			// now test the ascii version
			baos = new ByteArrayOutputStream();
			model.writeModelAscii(baos);
			
			newModel = new GeoTessModelExtended(new Scanner(new ByteArrayInputStream(baos.toByteArray())));
			
			assertTrue(newModel.equals(model));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

}
