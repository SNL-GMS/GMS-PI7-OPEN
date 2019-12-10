package gms.shared.utilities.geotess;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import gms.shared.utilities.geotess.util.colormap.ColorScale;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;
import org.junit.Test;

public class GeoTessModelUtilsTest
{

	@Test
	public void testGetLatitudesDoubleDoubleInt()
	{
		double[] values = GeoTessModelUtils.getLatitudes(-10, 10, 11);
		
		// System.out.println(Arrays.toString(values));
		
		double[] expected = new double[] { -10.0, -8.0, -6.0, -4.0, -2.0, 0.0, 2.0, 4.0, 6.0, 8.0, 10.0 };
		
		for (int i=0; i<expected.length; ++i)
			assertEquals(expected[i], values[i], 1e-6);
	}

	@Test
	public void testGetLatitudesDoubleDoubleDouble()
	{
		double[] values = GeoTessModelUtils.getLatitudes(-10, 10, 2.);
		
		// System.out.println(Arrays.toString(values));
		
		double[] expected = new double[] { -10.0, -8.0, -6.0, -4.0, -2.0, 0.0, 2.0, 4.0, 6.0, 8.0, 10.0 };
		
		for (int i=0; i<expected.length; ++i)
			assertEquals(expected[i], values[i], 1e-6);
	}

	@Test
	public void testGetLatitudesStringStringString()
	{
		double[] values = GeoTessModelUtils.getLatitudes("-10", "10", "2.");
		
		// System.out.println(Arrays.toString(values));
		
		double[] expected = new double[] { -10.0, -8.0, -6.0, -4.0, -2.0, 0.0, 2.0, 4.0, 6.0, 8.0, 10.0 };
		
		for (int i=0; i<expected.length; ++i)
			assertEquals(expected[i], values[i], 1e-6);
	}

	@Test
	public void testGetLatitudesDoubleDoubleIntBoolean()
	{
		double[] values = GeoTessModelUtils.getLatitudes(-10, 10, 11, false);
		
		// System.out.println(Arrays.toString(values));
		
		double[] expected = new double[] { -10.0, -8.0, -6.0, -4.0, -2.0, 0.0, 2.0, 4.0, 6.0, 8.0, 10.0 };
		
		for (int i=0; i<expected.length; ++i)
			assertEquals(expected[i], values[i], 1e-6);
		
		values = GeoTessModelUtils.getLatitudes(-11, 11, 11, true);
		
		// System.out.println(Arrays.toString(values));
		
		for (int i=0; i<expected.length; ++i)
			assertEquals(expected[i], values[i], 1e-6);
	}

	@Test
	public void testGetLatitudesDoubleDoubleDoubleBoolean()
	{
		double[] values = GeoTessModelUtils.getLatitudes(-10, 10, 2., false);
		
		// System.out.println(Arrays.toString(values));
		
		double[] expected = new double[] { -10.0, -8.0, -6.0, -4.0, -2.0, 0.0, 2.0, 4.0, 6.0, 8.0, 10.0 };
		
		for (int i=0; i<expected.length; ++i)
			assertEquals(expected[i], values[i], 1e-6);
		
		values = GeoTessModelUtils.getLatitudes(-11, 11, 2., true);
		
		// System.out.println(Arrays.toString(values));
		
		for (int i=0; i<expected.length; ++i)
			assertEquals(expected[i], values[i], 1e-6);
	}

	@Test
	public void testGetLatitudesStringStringStringBoolean()
	{
		double[] values = GeoTessModelUtils.getLatitudes("-10", "10", "11", false);
		
		// System.out.println(Arrays.toString(values));
		
		double[] expected = new double[] { -10.0, -8.0, -6.0, -4.0, -2.0, 0.0, 2.0, 4.0, 6.0, 8.0, 10.0 };
		
		for (int i=0; i<expected.length; ++i)
			assertEquals(expected[i], values[i], 1e-6);
		
		values = GeoTessModelUtils.getLatitudes("-11", "11", "11", true);
		
		// System.out.println(Arrays.toString(values));
		
		for (int i=0; i<expected.length; ++i)
			assertEquals(expected[i], values[i], 1e-6);
	}

	@Test
	public void testGetLongitudesDoubleDoubleIntBoolean()
	{
		double[] values = GeoTessModelUtils.getLongitudes(-10, 10, 11, true);
		
		// System.out.println(Arrays.toString(values));
		
		double[] expected = new double[] { -10.0, -8.0, -6.0, -4.0, -2.0, 0.0, 2.0, 4.0, 6.0, 8.0, 10.0 };
		
		for (int i=0; i<expected.length; ++i)
			assertEquals(expected[i], values[i], 1e-6);
		
		
		values = GeoTessModelUtils.getLongitudes(170, -170, 11, true);
		
		// System.out.println(Arrays.toString(values));
		
		expected = new double[] { 170.0, 172.0, 174.0, 176.0, 178.0, 180.0, 182.0, 184.0, 186.0, 188.0, 190.0 };
		
		for (int i=0; i<expected.length; ++i)
			assertEquals(expected[i], values[i], 1e-6);
	}

	@Test
	public void testGetLongitudesDoubleDoubleDoubleBoolean()
	{
		double[] values = GeoTessModelUtils.getLongitudes(-10, 10, 2., true);
		
		// System.out.println(Arrays.toString(values));
		
		double[] expected = new double[] { -10.0, -8.0, -6.0, -4.0, -2.0, 0.0, 2.0, 4.0, 6.0, 8.0, 10.0 };
		
		for (int i=0; i<expected.length; ++i)
			assertEquals(expected[i], values[i], 1e-6);
		
		
		values = GeoTessModelUtils.getLongitudes(170, -170, 2., true);
		
		// System.out.println(Arrays.toString(values));
		
		expected = new double[] { 170.0, 172.0, 174.0, 176.0, 178.0, 180.0, 182.0, 184.0, 186.0, 188.0, 190.0 };
		
		for (int i=0; i<expected.length; ++i)
			assertEquals(expected[i], values[i], 1e-6);
	}

	@Test
	public void testGetLongitudesStringStringStringString()
	{
		double[] values = GeoTessModelUtils.getLongitudes("-10", "10", "11", "true");
		
		// System.out.println(Arrays.toString(values));
		
		double[] expected = new double[] { -10.0, -8.0, -6.0, -4.0, -2.0, 0.0, 2.0, 4.0, 6.0, 8.0, 10.0 };
		
		for (int i=0; i<expected.length; ++i)
			assertEquals(expected[i], values[i], 1e-6);
	}

	@Test
	public void testGetLongitudesDoubleDoubleIntBooleanBoolean()
	{
		double[] values = GeoTessModelUtils.getLongitudes(-10, 10, 11, true, false);
		
		// System.out.println(Arrays.toString(values));
		
		double[] expected = new double[] { -10.0, -8.0, -6.0, -4.0, -2.0, 0.0, 2.0, 4.0, 6.0, 8.0, 10.0 };
		
		for (int i=0; i<expected.length; ++i)
			assertEquals(expected[i], values[i], 1e-6);
		
		
		values = GeoTessModelUtils.getLongitudes(170, -170, 11, true, false);
		
		// System.out.println(Arrays.toString(values));
		
		expected = new double[] { 170.0, 172.0, 174.0, 176.0, 178.0, 180.0, 182.0, 184.0, 186.0, 188.0, 190.0 };
		
		for (int i=0; i<expected.length; ++i)
			assertEquals(expected[i], values[i], 1e-6);

		values = GeoTessModelUtils.getLongitudes(-11, 11, 11, true, true);
		
		// System.out.println(Arrays.toString(values));
		
		expected = new double[] { -10.0, -8.0, -6.0, -4.0, -2.0, 0.0, 2.0, 4.0, 6.0, 8.0, 10.0 };
		
		for (int i=0; i<expected.length; ++i)
			assertEquals(expected[i], values[i], 1e-6);
		
		
		values = GeoTessModelUtils.getLongitudes(169, -169, 11, true, true);
		
		// System.out.println(Arrays.toString(values));
		
		expected = new double[] { 170.0, 172.0, 174.0, 176.0, 178.0, 180.0, 182.0, 184.0, 186.0, 188.0, 190.0 };
		
		for (int i=0; i<expected.length; ++i)
			assertEquals(expected[i], values[i], 1e-6);
}

	@Test
	public void testGetLongitudesDoubleDoubleDoubleBooleanBoolean()
	{
		double[] values = GeoTessModelUtils.getLongitudes(-10, 10, 2., true, false);
		
		// System.out.println(Arrays.toString(values));
		
		double[] expected = new double[] { -10.0, -8.0, -6.0, -4.0, -2.0, 0.0, 2.0, 4.0, 6.0, 8.0, 10.0 };
		
		for (int i=0; i<expected.length; ++i)
			assertEquals(expected[i], values[i], 1e-6);
		
		
		values = GeoTessModelUtils.getLongitudes(170, -170, 2., true, false);
		
		// System.out.println(Arrays.toString(values));
		
		expected = new double[] { 170.0, 172.0, 174.0, 176.0, 178.0, 180.0, 182.0, 184.0, 186.0, 188.0, 190.0 };
		
		for (int i=0; i<expected.length; ++i)
			assertEquals(expected[i], values[i], 1e-6);

		values = GeoTessModelUtils.getLongitudes(-11, 11, 2., true, true);
		
		// System.out.println(Arrays.toString(values));
		
		expected = new double[] { -10.0, -8.0, -6.0, -4.0, -2.0, 0.0, 2.0, 4.0, 6.0, 8.0, 10.0 };
		
		for (int i=0; i<expected.length; ++i)
			assertEquals(expected[i], values[i], 1e-6);
		
		
		values = GeoTessModelUtils.getLongitudes(169, -169, 2., true, true);
		
		// System.out.println(Arrays.toString(values));
		
		expected = new double[] { 170.0, 172.0, 174.0, 176.0, 178.0, 180.0, 182.0, 184.0, 186.0, 188.0, 190.0 };
		
		for (int i=0; i<expected.length; ++i)
			assertEquals(expected[i], values[i], 1e-6);
	}

	@Test
	public void testGetLongitudesStringStringStringStringBoolean()
	{
		double[] values = GeoTessModelUtils.getLongitudes("-10", "10", "11", "true", false);
		
		// System.out.println(Arrays.toString(values));
		
		double[] expected = new double[] { -10.0, -8.0, -6.0, -4.0, -2.0, 0.0, 2.0, 4.0, 6.0, 8.0, 10.0 };
		
		for (int i=0; i<expected.length; ++i)
			assertEquals(expected[i], values[i], 1e-6);

	
		values = GeoTessModelUtils.getLongitudes("-11", "11", "11", "true", true);
		
		// System.out.println(Arrays.toString(values));
		
		expected = new double[] { -10.0, -8.0, -6.0, -4.0, -2.0, 0.0, 2.0, 4.0, 6.0, 8.0, 10.0 };
		
		for (int i=0; i<expected.length; ++i)
			assertEquals(expected[i], values[i], 1e-6);
}
	
	/*
	 * Tests the getImage() class of functions from GeoTessModelUtils.  Sanity checks that resulting images are of
	 * the expected size and have colors that make sense (i.e. fall in between the color ranges specified by a Color[]).
	 * Also compares a generated image to one previously made with the same parameters for a consistency check.
	 * @author jwvicke - James Vickers, Org 5563.
	 */
	@Test
	public void testGetImage() throws GeoTessException
	{ /* Read a geotess model from the resources\resources/permanent_files directory for the test.  */
		GeoTessModel model = null;
		try 
		{  
			String resource = "permanent_files/crust20.geotess";

			InputStream inp = GeoTessModelUtilsTest.class.getResourceAsStream(resource);

			if (inp == null)
				throw new IOException("Cannot find resource "+resource);

			model = new GeoTessModel(new DataInputStream(inp));
		} 
		catch (IOException e) 
		{  e.printStackTrace();
		return;
		}
	  /********************************************************************************************************/
	  
	  /* Make image using default lat/lon bounds (whole earth) and check that result 
	   * isn't null and size of image is correct.  */
	  Color[] gradient = ColorScale.BLUE_CYAN_WHITE_YELLOW_RED.getColorScale();
	  final int width = 720;
      BufferedImage img = GeoTessModelUtils.getImage(model, 1.0, 0, true, 0, width, gradient);
	  assertNotNull(img);
	  assertEquals(img.getWidth(), width);        // check that it gave the width we asked for
	  assertEquals(img.getHeight(), width / 2);   // since we gave full earth coverage, should have an aspect ratio of 2:1
	  /********************************************************************************************************/
	  
	  /* Compare the just-made image to a version from a previous point in time both by dimensions and
	   * color value of each pixel.  This part of the test could fail if the way the colors are interpolated
	   * changes (the Utils.colormap.SimpleColorMap class), in which case the comparison image would 
	   * have to be updated to make this test pass. */
	  BufferedImage knownImg;
	  try 
	  {  
			String resource = "/permanent_files/knownImgOverlay.png";

			InputStream inp = GeoTessModelUtilsTest.class.getResourceAsStream(resource);

			if (inp == null)
				throw new IOException("Cannot find resource "+resource);

		  knownImg = ImageIO.read(new DataInputStream(inp));
	  } 
	  catch (IOException e) 
	  {  e.printStackTrace();
		 return;
	  }
	  assertEquals(img.getHeight(), knownImg.getHeight());
	  assertEquals(img.getWidth(), knownImg.getWidth());
	  for (int x = 0; x < img.getWidth(); x++)
	  {  for (int y = 0; y < img.getHeight(); y++)
	     {  assertEquals(new Color(knownImg.getRGB(x, y)), new Color(img.getRGB(x, y)));
	     }
	  }
	  /********************************************************************************************************/
	  
	  /* Make another image with custom lat/lon constraints and a simpler 2-color gradient, check that pixels in
	   * image are colored 'reasonably'; namely, they fall between the two colors in the gradient. */
	  final double minLat = -90.0, maxLat = 0.0, minLon = -180.0, maxLon = 180.0;
	  Color[] blueToRed = new Color[] {Color.blue, Color.red};
	  img = GeoTessModelUtils.getImage(model, minLat, maxLat, minLon, maxLon, 1.0, 0, true, 0, width, blueToRed);
	  assertNotNull(img);
	  assertEquals(img.getWidth(), width);
	  assertEquals(img.getHeight(), width / 4);  // this time we gave 90 degrees of lat and 360 of lon, aspect ratio of 4:1
	  for (int x = 0; x < img.getWidth(); x++)
	  {  for (int y = 0; y < img.getHeight(); y++)
		  {  int rgb = img.getRGB(x, y);
		     assertNotEquals(rgb, 0);      // this should NEVER happen if we defined Colors which do not contain Color.black.
		     
		     Color c = new Color(rgb);
		     assertEquals(c.getGreen(), 0);        // green is not in the defined gradient colors, so all green values should be 0.
		     
		     // each pixel should be a mixture of red/blue, by the way the gradient was setup. 
             assertTrue(c.getBlue() > 0 || c.getRed() > 0);                                                        
		  }
	  }
	  /********************************************************************************************************/
	  
	}

}
