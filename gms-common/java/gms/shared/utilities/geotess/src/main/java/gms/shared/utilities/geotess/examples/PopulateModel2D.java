package gms.shared.utilities.geotess.examples;

import gms.shared.utilities.geotess.Data;
import gms.shared.utilities.geotess.GeoTessMetaData;
import gms.shared.utilities.geotess.GeoTessModel;
import gms.shared.utilities.geotess.GeoTessPosition;
import gms.shared.utilities.geotess.util.globals.DataType;
import gms.shared.utilities.geotess.util.globals.InterpolatorType;
import gms.shared.utilities.geotess.util.numerical.vector.VectorGeo;
import gms.shared.utilities.geotess.util.numerical.vector.VectorUnit;

import java.io.File;
import java.util.Date;

/**
 * An example of how to generate a GeoTessModel and populate it with data.
 * At every node in the 4 degree tessellation:
 * <ul>
 * <li>populate a new model with some simple data. The data consists of the
 * distance in radians from every grid node to the geographic location of
 * seismic station ANMO located at Latitude: 34.9462N Longitude: 106.4567W.
 * <li>modify the data by converting it from radians to degrees.
 * <li>interpolate a value from the grid and print the result to the screen.
 * </ul>
 * <p>
 * 
 * @author sballar
 * 
 */
public class PopulateModel2D
{
	/**
	 * An example of how to generate a GeoTessModel and populate it with data.
	 * At every node in the 4 degree tessellation:
	 * <ul>
	 * <li>populate a new model with some simple data. The data consists of the
	 * distance in radians from every grid node to the geographic location of
	 * seismic station ANMO located at Latitude: 34.9462N Longitude: 106.4567W.
	 * <li>modify the data by converting it from radians to degrees.
	 * <li>interpolate a value from the grid and print the result to the screen.
	 * </ul>
	 * <p>
	 * 
	 * @param args path to file geotess_grid_04000.geotess
	 */
	public static void main(String[] args)
	{
		try
		{
			if (args.length == 0)
				throw new Exception(
						"\nMust specify a single command line argument specifying " +
						"the path to the file geotess_grid_04000.geotess\n");
			
			System.out.println("Start simple example");
			System.out.println();

			// Create a MetaData object in which we can specify information
			// needed for model contruction.
			GeoTessMetaData metaData = new GeoTessMetaData();

			// Specify a description of the model. This information is not
			// processed in any way by GeoTess. It is carried around for
			// information purposes.
			metaData.setDescription(String
					.format("Simple example of a GeoTess model,%n"
							+ "storing the distance from station ANMO %n"
							+ "near Albuquerque, New Mexico, USA%n"
							+ "Lat, lon = 34.9462, -106.4567 degrees.%n"));

			// Specify a list of layer names. A model could have many layers,
			// e.g., ("core", "mantle", "crust"), specified in order of
			// increasing radius. This simple example has only one layer.
			metaData.setLayerNames("surface");
			
			// Set layerID equal to the index of the one-and-only layer 
			// in this model.
			int layerID = 0;

			// specify the names of the attributes and the units of the
			// attributes in two String arrays. This model only includes
			// one attribute.
			// If this model had two attributes, they would be specified 
			// like this: setAttributes("Distance; Depth", "degrees; km");
			metaData.setAttributes("Distance", "degrees");

			// specify the DataType for the data. All attributes, in all
			// profiles, will have the same data type.
			metaData.setDataType(DataType.FLOAT);
			
			// specify the name of the software that is going to generate
			// the model.  This gets stored in the model for future reference.
			metaData.setModelSoftwareVersion("TestSimpleExample 1.0.0");
			
			// specify the date when the model was generated.  This gets 
			// stored in the model for future reference.
			metaData.setModelGenerationDate(new Date().toString());

			// specify the path to the file containing the grid to be used for
			// this test.  This information was passed in as a command line
			// argument.  Grids were included in the software delivery and
			// are available from the GeoTess website.
			String gridFile = new File(args[0]).getCanonicalPath();

			// call a GeoTessModel constructor to build the model. This will
			// load the grid, and initialize all the data structures to null.
			// To be useful, we will have to populate the data structures.
			GeoTessModel model = new GeoTessModel(gridFile, metaData);

			// Each grid vertex will be assigned a single data value consisting
			// of the epicentral distance in degrees from the location of the 
			// grid vertex to seismic station ANMO near Albuquerque, NM.
			// Get unit vector representation of position of station ANMO.
			double[] anmo = VectorGeo.getVectorDegrees(34.9462, -106.4567);

			// generate some data and store it in the model. The data consists
			// of the angular distance in degrees from each vertex of the model
			// grid to station ANMO near Albuquerque, NM, USA.
			for (int vtx = 0; vtx < model.getGrid().getNVertices(); ++vtx)
			{
				// retrieve the unit vector corresponding to the i'th vertex of
				// the grid.
				double[] vertex = model.getGrid().getVertex(vtx);

				// compute the distance from the vertex to station ANMO.
				float distance = (float) VectorUnit.angleDegrees(anmo, vertex);
				
				// Construct a new Data object that holds a single value of 
				// type float. Data.getData() can be called with multiple values
				// (all of the same type), or an array of values.  In this 
				// very simple example, there is only one value: distance.
				Data data = Data.getDataFloat(distance);
				
				// associate the Data object with the specified vertex of the model.  
				// This instance of setProfile always creates a ProfileSurface object.
				model.setProfile(vtx, data);
			}

			// At this point, we have a fully functional GeoTessModel object
			// that we can work with.

			// print a bunch of information about the model to the screen.
			System.out.println(model.toString());

			// Obtain a GeoTessPosition object from the model. This object
			// can be used to interpolate data from arbitrary points in the
			// model. Specify which type of interpolation is to be used:
			// linear or natural neighbor.
			GeoTessPosition position = model
					.getGeoTessPosition(InterpolatorType.LINEAR);

			// set the latitude and longitude of the GeoTessPosition object.
			// This is the position on the Earth where we want to interpolate
			// some data. This is the epicenter of the Sumatra-Andaman
			// earthquake of 2004.
			double lat = 3.316;
			double lon = 95.854;
			position.setTop(layerID, VectorGeo.getVectorDegrees(lat, lon));

			System.out.printf(
					"Interpolation lat, lon = %7.3f deg, %7.3f deg%n%n",
					VectorGeo.getLatDegrees(position.getVector()),
					VectorGeo.getLonDegrees(position.getVector()));

			// retrieve the interpolated distance value at the most recent
			// location specified in the GeoTessPostion object.
			double distance = position.getValue(0);

			// Output the interpolated distance from the position specified in
			// the GeoTessPosition object to station ANMO, in degrees.
			System.out.printf("Interpolated distance from station ANMO = %1.3f degrees%n%n",
							distance);

			// compute actual distance from ANMO to the position of interest.
			double actualDistance = VectorUnit.angle(anmo,
					position.getVector());

			System.out.printf("Actual distance from station ANMO       = %1.3f degrees%n",
							Math.toDegrees(actualDistance));

			System.out.println();

			// print out the index of the triangle in which point resides.
			System.out.printf("Interpolated point resides in triangle index = %d%n%n",
					position.getTriangle());

			// print out a table with the node indexes, node lat, node lon and
			// interpolation coefficients for the nodes of the triangle that
			// contains the point.
			System.out.println("  Node        Lat        Lon      Coeff");

			// get the indexes of the vertices that contribute to the
			// interpolation.
			int[] x = position.getVertices();

			// get the interpolation coefficients used in interpolation.
			double[] coef = position.getHorizontalCoefficients();

			for (int j = 0; j < x.length; ++j)
			{
				int vtx = x[j];
				System.out.printf(
						"%6d %10.4f %10.4f %10.6f%n",
						vtx,
						VectorGeo.getLatDegrees(model.getGrid().getVertex(vtx)),
						VectorGeo.getLonDegrees(model.getGrid().getVertex(vtx)), 
						coef[j]);
			}
			System.out.printf("%nSimple example completed successfully%n%n");

		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}

	}

}
