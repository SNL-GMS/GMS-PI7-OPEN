package gms.shared.utilities.geotess.examples;

import gms.shared.utilities.geotess.GeoTessModel;
import gms.shared.utilities.geotess.GeoTessPosition;
import gms.shared.utilities.geotess.Profile;
import gms.shared.utilities.geotess.ProfileType;
import gms.shared.utilities.geotess.util.globals.InterpolatorType;
import gms.shared.utilities.geotess.util.globals.OptimizationType;
import gms.shared.utilities.geotess.util.numerical.polygon.GreatCircle;
import gms.shared.utilities.geotess.util.numerical.vector.VectorGeo;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Example application that loads a GeoTessModel that contains a
 * representation of the Crust 2.0 model of Bassin, Laske and Masters (2000).
 * The example prints out meta data about the model and then interpolates
 * model values at a point in Tibet.
 * 
 * Bassin, C., Laske, G. and Masters, G., 
 * The Current Limits of Resolution for Surface Wave 
 * Tomography in North America, EOS Trans AGU, 81, F897, 2000.
 * http://igppweb.ucsd.edu/~gabi/crust2.html
 * 
 * @author sballar
 * 
 */
public class TestCrust20
{
	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		try
		{
			if (args.length == 0)
				throw new Exception(
						"\nMust specify a single command line argument specifying " +
						"the path to the file crust20.geotess\n");

			// specify the location of the GeoTess Crust 2.0 models.
			File inputFile = new File(args[0]);

			System.out.printf("Loading model from file %s%n%n",
					inputFile.getCanonicalPath());

			// instantiate a model and load the model from file
			GeoTessModel model = new GeoTessModel(inputFile, OptimizationType.SPEED);

			// print out summary information about the model.
			System.out.println(model);

			System.out.printf("\n");
			System.out.printf("=============================================================================\n");
			System.out.printf("\n");
			System.out.printf("Interpolate Data\n");
			System.out.printf("\n");
			System.out.printf("=============================================================================\n\n");
			
			// instantiate a GeoTessPosition object which will manage the
			// geographic position of an interpolation point, the interpolation
			// coefficients, etc.
			GeoTessPosition position = model.getGeoTessPosition(InterpolatorType.LINEAR);

			// set the position in the GeoTessPosition object to 
			// latitude = 30N, longitude = 90E, and radius equal to the
			// top of layer 0.
			position.setTop(0, VectorGeo.getVectorDegrees(30., 90.));

			// regurgitate the position
			System.out.printf("Interpolated model properties at lat, lon = %1.4f, %1.4f:%n%n",
							VectorGeo.getLatDegrees(position.getVector()),
							VectorGeo.getLonDegrees(position.getVector()));

			// print a table with values interpolated from the model at the
			// specified position
			System.out.println("Layer    Depth      Thick        vP         vS     density");
			for (int layer = model.getMetaData().getNLayers() - 1; layer >= 0; --layer)
			{
				// change the radius of the position object to the radius of the 
				// top of specified layer.
				position.setTop(layer);
				
				System.out.printf("%3d %10.4f %10.4f %10.4f %10.4f %10.4f%n",
						layer, 
						position.getDepth(),
						position.getLayerThickness(), 
						position.getValue(0),
						position.getValue(1), 
						position.getValue(2));
			}

			System.out.println();

			// print out the index of the triangle in which the point resides.
			System.out.printf( "Interpolated point resides in triangle index = %d%n%n",
					position.getTriangle());

			// print out a table with information about the 3 nodes at the
			// corners of the triangle that contains the interpolation point.
			// The information output is:
			// the index of the node,
			// node latitude in degrees,
			// node longitude in degrees,
			// interpolation coefficient, and
			// distance in degrees from interpolation point.

			System.out.println(position.toString());
			
			
			System.out.println("Call position.getWeights()");
			HashMap<Integer, Double> weights = new HashMap<Integer, Double>();
			position.getWeights(weights, 1.0);
			
			System.out.printf("geoposition_getWeights() returned weights for %d point indices:\n\n", weights.size());
			System.out.printf("pointIndex     weight\n");
			double sumWeights = 0;
			for (Integer pointIndex : weights.keySet())
			{
				double weight = weights.get(pointIndex);
				
				System.out.printf("%10d %10.6f\n", pointIndex, weight);
				sumWeights += weight;
			}
			System.out.printf("\nSum of the weights is %1.6f\n", sumWeights);

			System.out.printf("\n");
			System.out.printf("=============================================================================\n");
			System.out.printf("\n");
			System.out.printf("Query Model Data\n");
			System.out.printf("\n");
			System.out.printf("=============================================================================\n\n");
			
			// now we will extract some information about model values stored
			// on grid nodes in the model.  These are not interpolated values.

			// consider just one vertex.  Vertex 57 is located in Tibet
			int vertexId = 57;

			double[] u = model.getGrid().getVertex(vertexId);

			double earthRadius = VectorGeo.getEarthRadius(u);

			System.out.printf("Vertex=%d  lat = %1.4f  lon = %1.4f  ellipsoid_radius = %1.3f\n\n", vertexId,
					VectorGeo.getLatDegrees(u),
					VectorGeo.getLonDegrees(u),
					earthRadius);

			// write out the first header line which includes the names of the attributes
			System.out.printf("        layer          profile           depth");
			for (int attribute=0; attribute < model.getNAttributes(); ++attribute)
				System.out.printf(" %8s", model.getMetaData().getAttributeName(attribute));
			System.out.printf("\n");

			// print out second header line which includes attribute units
			System.out.printf("layer    name           type              (km)  ");
			for (int attribute=0; attribute < model.getNAttributes(); ++attribute)
				System.out.printf(" %8s", model.getMetaData().getAttributeUnit(attribute));
			System.out.printf("\n");

			System.out.printf("---------------------------------------------------------------------------\n");

			// loop over the layers in reverse order (shallowest to deepest)
			for (int layer = model.getNLayers() - 1; layer >= 0; --layer)
			{
				// get the name of this layer
				String layerName = model.getMetaData().getLayerName(layer);

				// get the Profile object that spans the current layer at the current vertex.
				Profile profile = model.getProfile(vertexId, layer);

				// get the profile type: THIN, CONSTANT, NPOINT, etc.
				ProfileType pType = profile.getType();

				// loop over every node in this layer in reverse order (shallowest to deepest).
				// The upper limit can be either geoprofile_getNRadii(profile) or
				// geoprofile_getNData(profile). The resulting table will differ depending
				// on which is chosen.
				for (int node=profile.getNRadii()-1; node >= 0; --node)
				{
					// get the radius of the current node in km.
					double radius = profile.getRadius(node);

					// print layer id, layer name, profile type and radius
					System.out.printf("%3d   %-16s %-16s %8.3f", layer, layerName, pType, earthRadius-radius);

					// loop over all the attributes (vp, vs, density, etc) and print out values.
					for (int attribute=0; attribute<model.getNAttributes(); ++attribute)
					{
						double value = profile.getValue(attribute, node);
						System.out.printf(" %8.3f", value);
					}
					System.out.printf("\n");
				}
				System.out.printf("\n");
			}

			System.out.printf("\n");
			System.out.printf("=============================================================================\n");
			System.out.printf("\n");
			System.out.printf("Get Weights for a GreatCircle Raypath\n");
			System.out.printf("\n");
			System.out.printf("=============================================================================\n\n");

			// define two unit vectors, pointA and pointB.
			// A is located at 0N, 0E and B is located at 10N, 10E.
			double[] pointA = new double[3];
			double[] pointB = new double[3];
			
			VectorGeo.getVectorDegrees(0., 0., pointA);
			VectorGeo.getVectorDegrees(10., 10., pointB);

			GreatCircle greatCircle = new GreatCircle(pointA, pointB);
			
			ArrayList<double[]> rayPath = greatCircle.getPoints(31, false);

			// radii will the radius of each of the points along the rayPath.
			double[] radii = new double[rayPath.size()];
			Arrays.fill(radii, 6371.);

			// get the weights for the specified rayPaths.
			model.getWeights(rayPath, radii, null,
					InterpolatorType.LINEAR, InterpolatorType.LINEAR, weights);
			
			System.out.printf("model.getWeights() returned weights for %d point indices:\n\n",
					weights.size());

			// initialize the sum of the weights to zero.  The sum of the weights
			// should equal the length of rayPath measured in km.
			sumWeights=0;

			// loop over all the points in the model that were touched by rayPath
			// and print out the pointIndex and the associated weight.
			// Also sum up the weights.
			System.out.printf("pointIndex     weight\n");
			for (Integer pointIndex : weights.keySet())
			{
				System.out.printf("%10d %10.6f\n", pointIndex, weights.get(pointIndex));
				sumWeights += weights.get(pointIndex);
			}

			System.out.printf("\n");
			System.out.printf("sumWeights      = %1.6f km\n", sumWeights);
			System.out.printf("distance * 6371 = %1.6f km\n\n", greatCircle.getDistance() * 6371.);

			System.out.println("Done.");

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

}
