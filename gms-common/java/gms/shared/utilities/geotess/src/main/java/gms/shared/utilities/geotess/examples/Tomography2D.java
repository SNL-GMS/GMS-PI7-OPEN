package gms.shared.utilities.geotess.examples;

import gms.shared.utilities.geotess.Data;
import gms.shared.utilities.geotess.GeoTessException;
import gms.shared.utilities.geotess.GeoTessGrid;
import gms.shared.utilities.geotess.GeoTessMetaData;
import gms.shared.utilities.geotess.GeoTessModel;
import gms.shared.utilities.geotess.GeoTessModelUtils;
import gms.shared.utilities.geotess.GeoTessPosition;
import gms.shared.utilities.geotess.GeoTessUtils;
import gms.shared.utilities.geotess.util.globals.DataType;
import gms.shared.utilities.geotess.util.globals.InterpolatorType;
import gms.shared.utilities.geotess.util.numerical.polygon.GreatCircle;
import gms.shared.utilities.geotess.util.numerical.polygon.Polygon;
import gms.shared.utilities.geotess.util.numerical.vector.EarthShape;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

import static java.lang.Math.toRadians;

/**
 * This application illustrates how to use features available in GeoTessJava to
 * execute tomography on a 2D model. This application does not implement
 * tomography but merely illustrates how to call methods in GeoTessJava that one
 * would likely need to perform tomography.
 * <p>
 * This application illustrates the following tasks:
 * <ol>
 * <li>Generate 11 great circle ray paths along the surface of the WGS84
 * ellipsoid.
 * <li>Generate a 2D, global starting model consisting of values of attenuation
 * as a function of geographic position on the globe.
 * <li>Limit the application of tomography to a region of the Earth in North
 * America. This limitation is optional.
 * <li>Trace rays through the starting model, calculating the path integral of
 * the attenuation along the ray path and the weights (data kernels) of all the
 * grid nodes attributable to interpolation of points on ray paths.
 * <li>Call methods in GeoTessJava to identify the neighbors of a specified
 * node. These methods are needed to apply regularization of the tomography
 * matrix.
 * <li>Apply changes in model attribute values computed by tomographic
 * inversion.
 * <li>Compute a new GeoTessModel whose attribute values are the number of times
 * each grid node was 'touched' by one of the ray paths (hit count).
 * <li>Execute application GeoTessBuilder to generate a new grid that is more
 * refined in areas of high hit count.
 * <li>Generate a new model based on the new, refined grid generated with
 * GeoTessBuilder but containing attenuation values copied or interpolated from
 * the original model.
 * </ol>
 * 
 * @author sballar
 * 
 */
public class Tomography2D
{
	// seismic station ANMO near Albuquerque, New Mexico, USA.
	// Latitude and longitude of the station are converted to an
	// earth centered unit vector.
	private static double[] ANMO = EarthShape.WGS84_RCONST.getVectorDegrees(34.9462, -106.4567);

	/**
	 * Main program that calls a bunch of methods that actually implement the tasks
	 * outlined above.  This program should be run from directory GeoTessBuilderExamples/tomo2dTest.
	 * @param args no command line arguments are required.
	 */
	public static void main(String[] args)
	{
		// instantiate a Tomography2D object.
		Tomography2D tomo = new Tomography2D();

		try
		{
			// Generate a starting model. In this example, the starting model is
			// very simple, consisting of constant values of attenuation at each
			// node of the grid. Real applications could start from some other
			// starting model loaded from a file.
			GeoTessModel model = tomo.startingModel();

			// Generate 11 great circle ray paths for use in this example. In
			// a real application, these ray paths would surely be loaded from
			// a file.
			ArrayList<double[][]> rayPaths = tomo.generateRayPaths();

			// We will limit the active nodes in the model using a Polygon
			// object. Nodes that reside inside the polygon will be the only
			// ones modified by tomography. The others are simply carried along
			// but do not participate in tomography. The polygon that we will
			// define is a small circle with radius 38 degrees centered on
			// seismic station ANMO. For information on other ways to define
			// Polygons, see the User's Manual or GeoTess code documentation.
			// If we wanted to execute a global tomography model, we would
			// not apply this step.
			Polygon polygon = new Polygon(ANMO, toRadians(38.), 100);
			model.setActiveRegion(polygon);

			// Trace rays though our model and extract integrated attribute
			// values along the ray path and interpolation coefficient 'weights'
			// associated with each ray path.
			tomo.integrateRayPaths(model, rayPaths);

			// call a method that illustrates how to find the indices of model
			// points that are neighbors of a specified model point. These are
			// often used in tomography to perform regularization or smoothing.
			tomo.regularization(model);

			// At this point, a real tomography application would actually
			// perform tomographic inversion, resulting in a vector containing
			// changes in attenuation that should be applied at each model
			// point. For this example, we will simply specify these changes 
			// and apply them.
			float[] attributeChanges = new float[model.getNPoints()];
			Arrays.fill(attributeChanges, 0.01F);

			// apply the attenuation changes.
			tomo.applyAttributeChanges(model, 0, attributeChanges);

			// Now we will assume that the user wishes to refine the model
			// in regions of high hit count before executing the next
			// iteration of tomography. In a real tomography application
			// this involves several steps which need to be implemented in 
			// separate applications.
			// First, we build a new GeoTessModel where the attribute
			// is HIT_COUNT, write that model to a file and
			// terminate execution. Then we use the GeoTessBuilder
			// application to generate a refined grid. Then, in
			// a new application, we build a new GeoTessModel based
			// on the refined grid, which contains attenuation values
			// that are either copied or interpolated from the original
			// model.

			// Given a current model and a collection of ray paths, build
			// a new model where the attribute is HIT_COUNT.
			GeoTessModel hitCountModel = tomo.hitCount(model, rayPaths);

			// save the hitCountModel to a file
			hitCountModel.writeModel("hitcount_model_original.geotess");

			// At this point, this application should be terminated and
			// application GeoTessBuilder should be executed to build
			// a new model that has a grid that has been refined in areas of
			// high hit count. There is a sample GeoTessBuilder properties file in
			// GeoTessBuilderExamples/tomo2dTest/gridbuilder_refine_model.properties
			// that illustrates how to build model
			// hitcount_model_refined.geotess.
			// See the GeoTess User's Manual for more information about the
			// GeoTessBuilder application. After GeoTessBulder has been
			// executed, a new application should be executed that calls
			// the remaining methods. This example includes everything in
			// one application, i.e., the method calls below assumes that
			// GeoTessBuilder has already been executed.

			// Load the refined hit count model from file. This model
			// has a grid that is refined in areas of high hit count.
			// The model attribute is HIT_COUNT. Hit count attribute
			// values at points that did not exist in the original hit
			// count model are interpolated values.
			GeoTessModel hitCountModelRefined = new GeoTessModel(
					"hitcount_model_refined.geotess");

			// At this point, we have an original model and a new, refined grid
			// that has all the grid nodes of the original model but extra grid
			// nodes that are not in the original model. The next method
			// generates a new model based on the refined grid, where values for
			// missing grid nodes are generated by either copying or
			// interpolating attenuation values from the original model.
			GeoTessModel refinedModel = tomo.refineModel(model,
					hitCountModelRefined);
			
			refinedModel.writeModel("attenuation_model_refined_java.geotess");

			System.out.println("Done.");

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Generate a starting model for the Tomography2D example program. The model
	 * will have a single attribute (attenuation), and will be a 2D model, i.e.,
	 * there will be no radius associated with the nodes of the model. For this
	 * simple example, the model is populated with a single, constant value of
	 * attenuation, 0.1
	 * 
	 * @return a GeoTessModel
	 * @throws IOException
	 * @throws GeoTessException
	 */
	protected GeoTessModel startingModel() throws Exception
	{
		System.out
				.println("************************************************************");
		System.out.println("*");
		System.out.println("* startingModel()");
		System.out.println("*");
		System.out
				.println("************************************************************");
		System.out.println();

		// Load an existing GeoTessGrid file. Several grid files were
		// delivered with the GeoTess package and can be found in the
		// GeoTessModels directory. For this example, a grid consisting of
		// uniform 4 degree triangles, was specified.
		File gridFile = new File("geotess_grid_04000.geotess");
		
		if (!gridFile.exists())
			throw new Exception("\nGrid file geotess_grid_04000.geotess does not exist.\n"
					+ "This program should be run from directory GeoTessBuilderExamples/tomo2dTest\n");

		// Create a MetaData object in which we can specify information
		// needed for model contruction.
		GeoTessMetaData metaData = new GeoTessMetaData();

		// Specify a description of the model. This information is not
		// processed in any way by GeoTess. It is carried around for
		// information purposes.
		metaData.setDescription("GeoTessModel for example program Tomography2D");

		// This model will have only one layer, named 'surface'.
		metaData.setLayerNames("surface");

		// Specify one attribute: attenuation, with units of 1/km
		metaData.setAttributes("attenuation", "1/km");

		// specify the DataType for the data. All attributes, in all
		// profiles, will have the same data type.
		metaData.setDataType(DataType.FLOAT);

		// specify the name of the software that is going to generate
		// the model. This gets stored in the model for future reference.
		metaData.setModelSoftwareVersion(getClass().getCanonicalName());

		// specify the date when the model was generated. This gets
		// stored in the model for future reference.
		metaData.setModelGenerationDate(new Date().toString());

		// call a GeoTessModel constructor to build the model. This will
		// load the grid, and initialize all the data structures to null.
		// To be useful, we will have to populate the data structures.
		GeoTessModel model = new GeoTessModel(gridFile, metaData);

		// generate some data and store it in the model.
		for (int vtx = 0; vtx < model.getGrid().getNVertices(); ++vtx)
			// create ProfileSurface objects with a constant value of type
			// float.
			model.setProfile(vtx, Data.getDataFloat(0.1F));

		System.out.println(model);

		return model;

	}

	/**
	 * Generate 11 ray paths on the surface of the WGS84 ellipsoid. Each ray
	 * path is defined by two unit vector locations, one representing an event,
	 * and the other a station. All of the ray paths generated here have the
	 * same station, ANMO, located near Albuquerque, New Mexico, USA. The first
	 * ray path has zero length (the event is colocated with the station). The
	 * remaining events range in distance from 5 to 50 degrees in distance and 0
	 * to 360 in azimuth from the station.
	 * <p>
	 * There is no requirement in GeoTess that the ray paths be represented this
	 * way, this parameterization was designed for this example program. In
	 * fact, GeoTess has no concept of a ray path at all.
	 * 
	 * @return an ArrayList of raypaths. Each ray path consists of two unit
	 *         vectors, one for the event and one for the station.
	 * @throws IOException
	 * @throws GeoTessException
	 */
	protected ArrayList<double[][]> generateRayPaths() throws Exception
	{
		System.out
				.println("************************************************************");
		System.out.println("*");
		System.out.println("* generateRayPaths()");
		System.out.println("*");
		System.out
				.println("************************************************************");
		System.out.println();

		ArrayList<double[][]> rayPaths = new ArrayList<double[][]>(10);

		for (int i = 0; i <= 10; ++i)
		{
			double[] event = new double[3];
			// populate event with a unit vector obtained by moving station ANMO
			// some distance and azimuth specified in radians
			GeoTessUtils.move(ANMO, toRadians(i * 5), toRadians(i * 36), event);

			// specify a new raypath from the new event to station ANMO
			double[][] rayPath = new double[][] { event, ANMO };

			rayPaths.add(rayPath);
		}

		// the remainder of this method prints out information about the ray
		// paths.

		System.out
				.println(" id        event            station         distance    azimuth");
		for (int i = 0; i < rayPaths.size(); ++i)
		{
			double[][] rayPath = rayPaths.get(i);
			double[] event = rayPath[0];
			double[] station = rayPath[1];

			System.out.printf("%3d %s %s %10.4f %10.4f%n", i,
					EarthShape.WGS84.getLatLonString(event, 4),
					EarthShape.WGS84.getLatLonString(station, 4),
					GeoTessUtils.angleDegrees(station, event),
					GeoTessUtils.azimuthDegrees(station, event, Double.NaN));
		}
		System.out.println();

		ArrayList<double[]> points = new ArrayList<double[]>();
		for (double[][] ray : rayPaths)
		{
			GreatCircle gc = new GreatCircle(ray[0], ray[1]);
			for (double[] point : gc.getPoints(
					(int) Math.ceil(gc.getDistance() / Math.toRadians(1) + 1),
					false))
				if (!Double.isNaN(point[0]))
					points.add(point);
		}

		GeoTessModelUtils.vtkPoints(points, "ray_path_points.vtk");

		return rayPaths;
	}

	/**
	 * For every ray path, trace the ray through the model. Compute the integral
	 * of the model attribute along the ray path. Also accumulate the 'weight'
	 * associated with each grid node during interpolation of the attribute
	 * values along the ray path.
	 * 
	 * <p>
	 * The GeoTess method used to compute the required information assume that
	 * each ray path is a great circle path from event to station. The radii of
	 * the points along the ray path are assumed to coincide with the surface of
	 * the WGS84 ellipsoid.
	 * 
	 * <p>
	 * This method doesn't do anything with the results (the integrated value
	 * and the weights). This method merely serves as an example of how to
	 * extract the relevant information from a GeoTessModel. In a real
	 * tomography application, additional code would be required to transfer the
	 * information to tomographic matrices for inversion.
	 * 
	 * @param model
	 * @param rayPaths
	 * @throws GeoTessException
	 */
	protected void integrateRayPaths(GeoTessModel model, ArrayList<double[][]> rayPaths)
		throws Exception
	{
		System.out
				.println("************************************************************");
		System.out.println("*");
		System.out.println("* rayTrace()");
		System.out.println("*");
		System.out
				.println("************************************************************");
		System.out.println();

		// the index of the attribute that we want to integrate along the ray
		// paths.
		int attribute = 0;

		// approximate point spacing to use for numerical integration.
		// one tenth of a degree, converted to radians.
		double pointSpacing = toRadians(0.1);
		
		// the radius of the earth in km.  If user wishes to assume a spherical
		// earth, the radius can be specified here. By specifying a value 
		// <= 0 km, GeoTess will compute local values of earth radius assuming
		// the WGS84 ellipsoid.
		double earthRadius = -1;

		// horizontal interpolation type; either LINEAR or NATURAL_NEIGHBOR
		InterpolatorType interpType = InterpolatorType.NATURAL_NEIGHBOR;

		// weights will be a map from a model point index to the weight
		// ascribed to that point index by the integration points along the ray.
		// The sum of all the weights will equal the length of the ray path in
		// km.
		HashMap<Integer, Double> weights = new HashMap<Integer, Double>();

		// loop over the ray paths
		for (int i = 0; i < rayPaths.size(); ++i)
		{
			// each ray path is comprised of two unit vectors, one for the event
			// and one for the station.
			double[][] rayPath = rayPaths.get(i);
			double[] event = rayPath[0];
			double[] station = rayPath[1];
			
			GreatCircle greatCircle = new GreatCircle(event, station);

			// we want a set of weights for each ray path, so we need to clear
			// the map in between calls to getPathIntegral().
			weights.clear();

			// integrate the attribute of interest along the ray path.
			// Also accumulate the weights ascribed to all the grid nodes
			// 'touched' by the integration points that define the ray path. The
			// sum of all the weights will equal the length of the ray path in
			// km.
			double attenuation = model.getPathIntegral2D(attribute, 
					greatCircle, pointSpacing, earthRadius, interpType, weights);

			// the rest of the code in this method prints information about the
			// ray path and its weights to the screen. In a real tomography
			// application, we would transfer the information into other data
			// structures for use in tomography.

			// print out a bunch of information about the ray, including the
			// value of attenuation
			System.out
					.println("----------------------------------------------------------------------------");
			System.out
					.println("ray        station            event         distance    azimuth  attenuation");
			System.out.printf("%3d %s %s %10.4f %10.4f %12.5f%n%n", i,
					EarthShape.WGS84.getLatLonString(station, 4),
					EarthShape.WGS84.getLatLonString(event, 4),
					GeoTessUtils.angleDegrees(station, event),
					GeoTessUtils.azimuthDegrees(station, event, Double.NaN),
					attenuation);

			// print out information about the grid nodes and weights.
			System.out
					.println("pointId    weight |  point lat, lon, distance and azimuth from station");

			double sumWeights = 0;

			if (weights.size() == 0)
				System.out
						.println("No weights because event-station distance = 0");

			for (Entry<Integer, Double> entry : weights.entrySet())
			{
				int pointIndex = entry.getKey();
				double weight = entry.getValue();

				sumWeights += weight;

				if (pointIndex < 0)
				{
					System.out.printf("%7d %9.2f |  outside polygon%n",
							pointIndex, weight);
				}
				else
				{
					double[] gridNode = model.getPointMap().getPointUnitVector(
							pointIndex);

					System.out.printf("%7d %9.2f | %s %10.4f %10.4f%n",
							pointIndex, weight, EarthShape.WGS84
									.getLatLonString(gridNode), GeoTessUtils
									.angleDegrees(station, gridNode),
							GeoTessUtils.azimuthDegrees(station, gridNode,
									Double.NaN));
				}
			}
			System.out.println();

			System.out.printf("Sum of weights = %10.4f km %n%n", sumWeights);
		}
	}

	/**
	 * Find the indices of the model 'points' that are the neighbors of each
	 * model point. In a real tomography application, this information would be
	 * used to apply regularization. Here, the GeoTessGrid is interrogated for
	 * the required information, but nothing is done with it.
	 * 
	 * @param model
	 */
	protected void regularization(GeoTessModel model)
	{
		System.out
				.println("************************************************************");
		System.out.println("*");
		System.out.println("* regularization()");
		System.out.println("*");
		System.out
				.println("************************************************************");
		System.out.println();

		// tessellaltion index is zero because 2D models use grids that consist
		// of only one multi-level tessellation.
		int tessId = 0;

		// find the index of the last level in the multi-level tessellation.
		int level = model.getGrid().getTopLevel(tessId);

		// order specifies the maximum number of triangle edges that are to be
		// traversed when searching for neighbors. Users are invited to try
		// higher values to see what happens.
		int order = 1;

		for (int pointIndex = 0; pointIndex < model.getNPoints(); ++pointIndex)
		{
			// for 2D models, pointIndex and vertexId will be equal, except if
			// a polygon is used to down sample the set of active nodes.
			// For a discussion of the difference between points and vertices,
			// see the User's Manual.
			int vertexId = model.getPointMap().getVertexIndex(pointIndex);

			// get the unit vector of the current vertex
			double[] vertex = model.getGrid().getVertex(vertexId);

			// find the indices of the vertexes that are connected to the
			// current vertex by a single triangle edge
			HashSet<Integer> neighbors = model.getGrid().getVertexNeighbors(
					tessId, level, vertexId, order);

			// only print information about a subset of the vertices
			if (pointIndex % 100 == 0)
			{
				System.out
						.println("-------------------------------------------------------- ");
				System.out.printf("point %d, vertex %d, lat,lon %s:%n%n",
						pointIndex, vertexId,
						EarthShape.WGS84.getLatLonString(vertex, 3));

				System.out.println("neighbor  neighbor distance  azimuth");
				System.out.println("vertexid   pointid   (deg)     (deg)");
				for (Integer neighbor : neighbors)
				{
					// neighbor is the vertexId of a model vertex that is
					// a neighbor of the current vertex.
					double[] neighborVertex = model.getGrid().getVertex(
							neighbor);

					int neighborPoint = model.getPointMap().getPointIndex(
							neighbor, 0, 0);

					System.out.printf("%8d %8d %8.2f  %8.2f%n", neighbor,
							neighborPoint, GeoTessUtils.angleDegrees(vertex,
									neighborVertex), GeoTessUtils
									.azimuthDegrees(vertex, neighborVertex,
											Double.NaN));
				}
				System.out.println();
			}

		}

	}

	/**
	 * Given a model and an array of attribute changes, apply the changes to the
	 * model.
	 * 
	 * @param model
	 * @param attributeIndex
	 * @param attributeChanges
	 */
	protected void applyAttributeChanges(GeoTessModel model,
			int attributeIndex, float[] attributeChanges)
	{
		for (int pointIndex = 0; pointIndex < model.getNPoints(); ++pointIndex)
			model.setValue(pointIndex, attributeIndex,
					model.getValueFloat(pointIndex, attributeIndex)
							+ attributeChanges[pointIndex]);
	}

	/**
	 * Build a new GeoTessModel with the same grid nodes as the input model.
	 * There will a single attribute value of type int assigned to each grid
	 * node. The name of the attribute is HIT_COUNT and it is unitless.
	 * 
	 * @param inputModel
	 * @param rayPaths
	 * @return
	 * @throws GeoTessException
	 * @throws IOException
	 */
	protected GeoTessModel hitCount(GeoTessModel inputModel,
			ArrayList<double[][]> rayPaths) throws Exception
	{
		System.out
				.println("************************************************************");
		System.out.println("*");
		System.out.println("* hitCount()");
		System.out.println("*");
		System.out
				.println("************************************************************");
		System.out.println();

		// Create a MetaData object in which we can specify information
		// needed for model construction.
		GeoTessMetaData metaData = new GeoTessMetaData();

		// Specify a description of the model.
		metaData.setDescription("GeoTessModel of hit count for example program Tomography2D");

		// This model will have only one layer, named 'surface'.
		metaData.setLayerNames("surface");

		// Specify one unitless attribute
		metaData.setAttributes("HIT_COUNT", "count");

		// specify the DataType for the data.
		metaData.setDataType(DataType.INT);

		// specify the name of the software that is going to generate
		// the model. This gets stored in the model for future reference.
		metaData.setModelSoftwareVersion(getClass().getCanonicalName()
				+ " hitCount()");

		// specify the date when the model was generated. This gets
		// stored in the model for future reference.
		metaData.setModelGenerationDate(new Date().toString());

		// call a GeoTessModel constructor to build the model. Use the same grid
		// as the one used by the input model.
		GeoTessModel hitCountModel = new GeoTessModel(inputModel.getGrid(),
				metaData);

		// initialize the data value (hit count) of every node with int value zero.
		for (int vertexId = 0; vertexId < hitCountModel.getNVertices(); ++vertexId)
			hitCountModel.setProfile(vertexId, Data.getDataInt(0));

		// if the inputModel had its active nodes specified with a Polygon,
		// then apply the same polygon to the hit count model.
		hitCountModel.setActiveRegion(inputModel.getPointMap().getPolygon());

		// approximate point spacing to use to define the ray path
		double pointSpacing = toRadians(0.1);

		// horizontal interpolation type; either LINEAR or NATURAL_NEIGHBOR
		InterpolatorType interpType = InterpolatorType.LINEAR;
		
		// model has only one attribute with index 0.
		int attributeIndex = 0;

		// weights will be a map from a model point index to the weight
		// ascribed to that point index by the integration points along the ray.
		// The sum of all the weights will equal the length of the ray path in km.
		HashMap<Integer, Double> weights = new HashMap<Integer, Double>();

		// loop over the ray paths
		for (int i = 0; i < rayPaths.size(); ++i)
		{
			// each ray path is comprised of two unit vectors, one for the event
			// and one for the station.
			double[][] rayPath = rayPaths.get(i);
			double[] event = rayPath[0];
			double[] station = rayPath[1];
			GreatCircle greatCircle = new GreatCircle(event, station);

			// we want a set of weights for each ray path, so we need to clear
			// the map in between calls to getPathIntegral().
			weights.clear();

			// integrate points along the ray path. We don't care about the
			// integral, we only want the weights assigned to each model point.
			inputModel.getPathIntegral2D(-1, greatCircle,
					pointSpacing, -1., interpType, weights);

			for (Integer pointIndex : weights.keySet())
				if (pointIndex >= 0)
				{
					int hitcount = hitCountModel.getValueInt(pointIndex, attributeIndex);
					++hitcount;
					hitCountModel.setValue(pointIndex, attributeIndex, hitcount);
					// this could be done more compactly:
					// model.setValue(pointIndex, attributeIndex, model.getValueInt(pointIndex, 0)+1);
				}
		}

		// hitCountModel has been populated with the hit count of every vertex.

		// plot maps of hit count and triangle size.
		GeoTessModelUtils.vtk(hitCountModel, "hitcount_model_original.vtk", 0,
				false, new int[] { 0 });
		GeoTessModelUtils.vtkTriangleSize(hitCountModel.getGrid(), new File(
				"triangle_size_original.vtk"), 0);

		// print information about the points that have hit count > 0
		System.out.println("   point   vertex       lat      lon  distance  hitcount");
		for (int pointIndex = 0; pointIndex < hitCountModel.getNPoints(); ++pointIndex)
			if (hitCountModel.getValueInt(pointIndex, 0) > 0)
			{
				double[] u = hitCountModel.getPointMap().getPointUnitVector(
						pointIndex);

				System.out.printf(
						"%8d %8d   %s %9.2f %6d%n",
						pointIndex,
						hitCountModel.getPointMap().getVertexIndex(pointIndex),
						hitCountModel.getPointMap().getPointLatLonString(
								pointIndex, 3),
						GeoTessUtils.angleDegrees(ANMO, u),
						hitCountModel.getValueInt(pointIndex, 0));
			}
		System.out.println();

		return hitCountModel;
	}

	/**
	 * At this point, we have a new GeoTessModel that has been refined to have
	 * higher resolution (more vertices) than the old model. But the new model has 
	 * attribute value HIT_COUNT, not ATTENUATION.  We need to make a
	 * new model using the refined grid from hitCountModelRefined but using data 
	 * obtained from the old model. Where the old model has a vertex that is 
	 * colocated with the vertex in the new model, the data from the old model is 
	 * copied to the new model. For vertices in the new model that do not have 
	 * colocated vertices in the old model, data will be interpolated from the 
	 * data in the old model.
	 * 
	 * @param oldModel
	 * @param hitCountModelRefined
	 * @return
	 * @throws GeoTessException
	 */
	protected GeoTessModel refineModel(GeoTessModel oldModel,
			GeoTessModel hitCountModelRefined) throws Exception
	{
		System.out
				.println("************************************************************");
		System.out.println("*");
		System.out.println("* refineModel()");
		System.out.println("*");
		System.out
				.println("************************************************************");
		System.out.println();

		// get a reference to the refined grid in hitCountModelRefined
		GeoTessGrid refinedGrid = hitCountModelRefined.getGrid();

		// plot maps of hit count and triangle size in the refined model.
		GeoTessModelUtils.vtk(hitCountModelRefined,
				"hitcount_model_refined.vtk", 0, false, new int[] { 0 });
		GeoTessModelUtils.vtkTriangleSize(refinedGrid, new File(
				"triangle_size_refined.vtk"), 0);

		// make a new model with the refined grid and a reference to the meta
		// data from the old model.
		GeoTessModel newModel = new GeoTessModel(
				hitCountModelRefined.getGrid(), oldModel.getMetaData());

		// we will need to interpolate data from the old model at vertices in
		// the new model that do not exist in the old model. For that purpose,
		// we will need a GeoTessPosition object obtained from the old model.
		GeoTessPosition pos = oldModel.getGeoTessPosition(InterpolatorType.LINEAR);

		// both old and new models are 2D models and hence have only a single layer.
		int layerIndex = 0;

		// loop over every vertex in the new model and populate it with data.
		for (int vtx = 0; vtx < newModel.getNVertices(); ++vtx)
		{
			// find the unit vector of the vertex from the new model.
			// There may or may not be a vertex in the old model that is
			// colocated with this unit vector.
			double[] u = refinedGrid.getVertex(vtx);

			// request the index of the vertex in the old model that is
			// colocated with the new vertex. If the old model has no colocated
			// vertex, then oldVtx will be -1.
			int oldVtx = oldModel.getGrid().getVertexIndex(u);

			Data data = null;
			if (oldVtx < 0)
				// interpolate a new Data object from values in the old model.
				data = pos.set(layerIndex, u, 6371.).getData();
			else
				// retrieve a reference to the data object from the old model.
				// Note that the new and old models share references to common
				// Data objects.  Changes made to attribute values in one model
				// will also change the values in the other model.  
				data = oldModel.getProfile(oldVtx, layerIndex).getDataTop();

			// set the data in the new model.
			newModel.setProfile(vtx, data);
		}

		// print some statistics about model values in old and new models.
		System.out.println(GeoTessModelUtils.statistics(oldModel));
		System.out.println(GeoTessModelUtils.statistics(newModel));

		return newModel;
	}

}
