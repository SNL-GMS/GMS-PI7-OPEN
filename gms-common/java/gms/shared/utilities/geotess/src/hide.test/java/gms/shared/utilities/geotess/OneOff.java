package gms.shared.utilities.geotess;

import static org.junit.Assert.assertEquals;

import gms.shared.utilities.geotess.util.containers.hash.sets.HashSetInteger;
import gms.shared.utilities.geotess.util.globals.InterpolatorType;
import java.io.IOException;
import java.util.Arrays;
import org.junit.Ignore;
import org.junit.Test;

public class OneOff
{
	@Test
	@Ignore
	public void testNNGrid() throws GeoTessException, IOException
	{
		GeoTessModel model = new GeoTessModel("src/test/resources/permanent_files/variable_resolution_model_NOT_DELAUNAY.geotess");
		
		//GeoTessModel model = new GeoTessModel("/Users/sballar/work/models/tomo130521/salsa3d.2.1_tomo.geotess");
//		GeoTessModel model = new GeoTessModel("\\\\tonto2\\GNEM\\devlpool\\sballar\\GMP_testing\\models\\tomo130521\\salsa3d.2.1_tomo.geotess");
		int layer = 4;
		
		GeoTessGrid grid = model.getGrid();
		
		int nChanges = model.getGrid().delaunay();
		System.out.printf("Delaunay generated %d edge flips%n", nChanges);
		
		// for every vertex connected at tessellation that supports upper mantle, 
		// find a bunch of points very, very close to the vertex and set the 
		// interpolation position.  These are points most likely to cause issues
		// for the interpolator.
		GeoTessPosition pos = model.getGeoTessPosition(InterpolatorType.NATURAL_NEIGHBOR);
		
		pos.set(layer, new double[] {-0.05552047691792649, -0.6015009002546082, -0.7969404893940965}, 1e4);
		
		int nmax=0;
		double[] vmax = null;
		
		HashSetInteger vertices = model.getConnectedVertices(layer);
		HashSetInteger.Iterator it = vertices.iterator();
		while (it.hasNext())
		{
			double[] v = grid.getVertex(it.next());
			double factor = 3e-7;
			for (int i=0; i<100; ++i)
			{
				double x = (Math.random()*2.-1.)*factor;
				double y = (Math.random()*2.-1.)*factor;
				double z = (Math.random()*2.-1.)*factor;
				double[] u = new double[] {v[0]+x, v[1]+y, v[2]+z};
				GeoTessUtils.normalize(u);
				pos.set(layer, u, 1e4);
				if (pos.getNVertices() > nmax)
				{
					vmax = u;
					nmax = pos.getNVertices();
				}
			}
		}
		
		System.out.printf("%d %s%n", nmax, Arrays.toString(vmax));

		
		
		
		int n = 1;

		double[] latitudes = GeoTessModelUtils.getLatitudes(10., 40., n);
		double[] longitudes = GeoTessModelUtils.getLongitudes(120, 150, n, true);

		double[][][] linearValues = GeoTessModelUtils.getMapValuesLayer(model, latitudes, longitudes, layer, 1., 
				InterpolatorType.LINEAR, InterpolatorType.LINEAR, false, null);

		double[][][] nnValues = GeoTessModelUtils.getMapValuesLayer(model, latitudes, longitudes, layer, 1., 
				InterpolatorType.NATURAL_NEIGHBOR, InterpolatorType.LINEAR, false, null);

		for (int i=0; i<latitudes.length; ++i)
			for (int j=0; j<longitudes.length; ++j)
				assertEquals(nnValues[i][j][0], linearValues[i][j][0], 1e-2);
		
		
	}

}


