package gms.shared.utilities.geotess;

import static java.lang.Math.PI;
import static java.lang.Math.abs;
import static java.lang.Math.ceil;
import static java.lang.Math.max;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Scanner;

import gms.shared.utilities.geotess.util.colormap.ColorMap;
import gms.shared.utilities.geotess.util.colormap.SimpleColorMap;
import gms.shared.utilities.geotess.util.containers.Tuple;
import gms.shared.utilities.geotess.util.containers.arraylist.ArrayListDouble;
import gms.shared.utilities.geotess.util.containers.arraylist.ArrayListInt;
import gms.shared.utilities.geotess.util.containers.hash.sets.HashSetInteger;
import gms.shared.utilities.geotess.util.containers.hash.sets.HashSetInteger.Iterator;
import gms.shared.utilities.geotess.util.globals.DataType;
import gms.shared.utilities.geotess.util.globals.InterpolatorType;
import gms.shared.utilities.geotess.util.mapprojection.RobinsonProjection;
import gms.shared.utilities.geotess.util.numerical.polygon.GreatCircle;
import gms.shared.utilities.geotess.util.numerical.polygon.GreatCircle.GreatCircleException;
import gms.shared.utilities.geotess.util.numerical.vector.EarthShape;
import gms.shared.utilities.geotess.util.numerical.vector.Vector3D;
import gms.shared.utilities.geotess.util.numerical.vector.VectorUnit;

/**
 * A collection of static utilities that extract organized information from a
 * GeoTessModel. There are utilities to retrieve:
 * <ul>
 * <li>a map of attribute values at a constant specified depth on a regular
 * latitude-longitude grid.
 * <li>a map of attribute values at top or bottom of a layer on a regular
 * latitude-longitude grid.
 * <li>a map of the depth of the top or bottom of a specified layer on a regular
 * latitude-longitude grid.
 * <li>attribute values interpolated on a vertical slice through a model.
 * <li>attribute values along a radial 'borehole' at a specified position.
 * </ul>
 * 
 * @author sballar
 * 
 */
public class GeoTessModelUtils
{
	/**
	 * Class that represents a single point on the earth as both a unit vector
	 * and as an xy point projected using a Robinson projection. This class
	 * implements methods equals() and hashCode() making it suitable for storing
	 * in Set and Map objects.
	 * 
	 * @author sballar
	 * 
	 */
	public static class Point
	{
		public int vertexIndex;
		/**
		 * The first 3 elements store the components of the unit vector. If the
		 * length is 5, then elements 3 and 4 store the xy values from a
		 * Robinson projection.
		 */
		public double[] v;
		String equalityString;
		int hashcode;

		Point(double[] point)
		{
			this.v = point;
			this.vertexIndex = -1;

			equalityString = "";
			for (int i = 0; i < point.length; ++i)
				equalityString += String.format(" %19.16f", point[i]);

			hashcode = equalityString.hashCode();
		}

		@Override
		public boolean equals(Object other)
		{
			return other instanceof Point
					&& ((Point) other).equalityString.equals(equalityString);
		}

		@Override
		public int hashCode()
		{
			return hashcode;
		}

		@Override
		public String toString()
		{
			return v.length == 3 ? EarthShape.WGS84.getLatLonString(v) : String
					.format("%s %8.4f %8.4f",
							EarthShape.WGS84.getLatLonString(v), v[3], v[4]);
		}

	}

	/**
	 * Retrieve a map of attribute values at a constant specified depth on a
	 * regular latitude-longitude grid.
	 * <p>
	 * If layerId < 0, then interpolation points are not constrained to layer
	 * boundaries. An attempt to interpolate a value at a depth which is
	 * below the bottom of the bottom layer, or above the top of the top layer,
	 * will return Double.NaN.
	 * <p>
	 * If layerId is >= 0 then for every point on the map, if the specified
	 * depth is deeper than the depth of the bottom of the specified layer, then
	 * attribute values are interpolated at the bottom of the specified layer.
	 * Similarly, if the specified depth is shallower than the depth of the top
	 * of the specified layer, then attribute values at the top of the specified
	 * layer are interpolated.
	 * 
	 * @param model
	 *            the GeoTessModel to be interrogated.
	 * @param latitudes
	 *            array of latitude values in degrees.
	 * @param longitudes
	 *            array of longitude values in degrees.
	 * @param layerId
	 *            layer index
	 * @param depth
	 *            the depth at which samples should be interpolated, in km.
	 * @param horizontalType
	 *            either InterpolatorType.LINEAR or
	 *            InterpolatorType.NATURAL_NEIGHBOR
	 * @param radialType
	 *            either InterpolatorType.LINEAR or
	 *            InterpolatorType.CUBIC_SPLINE
	 * @param reciprocal
	 *            if false, return value; if true, return 1./value.
	 * @param attributes
	 *            indexes of the attributes to include.
	 * @return double[nlat][nlon][nAttributes]
	 * @throws GeoTessException
	 */
	static public double[][][] getMapValuesDepth(GeoTessModel model,
			double[] latitudes, double[] longitudes, int layerId, double depth,
			InterpolatorType horizontalType, InterpolatorType radialType,
			boolean reciprocal, int[] attributes) throws GeoTessException
	{
		int nlat = latitudes.length;
		int nlon = longitudes.length;
		if (attributes == null)
		{
			attributes = new int[model.getMetaData().getNAttributes()];
			for (int i = 1; i < attributes.length; ++i)
				attributes[i] = i;
		}
		double[][][] map = new double[nlat][nlon][attributes.length];

		GeoTessPosition pos = GeoTessPosition.getGeoTessPosition(model,
				horizontalType, radialType);

		if (layerId >= 0)
			for (int i = 0; i < nlat; ++i)
				for (int j = 0; j < nlon; ++j)
				{
					pos.set(layerId, latitudes[i], longitudes[j], depth);
					for (int k = 0; k < attributes.length; ++k)
						map[i][j][k] = reciprocal ? 1. / pos
								.getValue(attributes[k]) : pos
								.getValue(attributes[k]);
				}
		else
		{
			for (int i = 0; i < nlat; ++i)
				for (int j = 0; j < nlon; ++j)
				{
					pos.set(latitudes[i], longitudes[j], depth);
					double radius = pos.getEarthRadius()-depth;
					if (radius < pos.getRadiusBottom(0) || radius > pos.getRadiusTop(pos.getNLayers()-1))
						for (int k = 0; k < attributes.length; ++k)
							map[i][j][k] = Double.NaN;
					else
						for (int k = 0; k < attributes.length; ++k)
							map[i][j][k] = reciprocal ? 1. / pos
									.getValue(attributes[k]) : pos
									.getValue(attributes[k]);
				}
		}
		return map;
	}

	/**
	 * Retrieve a map of attribute values at top or bottom of a layer on a
	 * regular latitude-longitude grid.
	 * 
	 * @param model
	 *            the GeoTessModel to be interrogated.
	 * @param latitudes
	 *            array of latitude values in degrees.
	 * @param longitudes
	 *            array of longitude values in degrees.
	 * @param layerId
	 *            layer index
	 * @param fractionalRadius
	 *            the fractional radius within the layer at which samples should
	 *            be interpolated. Fractional radius <= 0.0 will return values
	 *            at the bottom of the layer and values >= 1.0 will return
	 *            values at the top of the layer.
	 * @param horizontalType
	 *            either InterpolatorType.LINEAR or
	 *            InterpolatorType.NATURAL_NEIGHBOR
	 * @param radialType
	 *            either InterpolatorType.LINEAR or
	 *            InterpolatorType.CUBIC_SPLINE
	 * @param reciprocal
	 *            if false, return value; if true, return 1./value.
	 * @param attributes
	 *            indexes of the attributes to include.
	 * @return double[nlat][nlon][nAttributes]
	 * @throws GeoTessException
	 */
	static public double[][][] getMapValuesLayer(GeoTessModel model,
			double[] latitudes, double[] longitudes, int layerId,
			double fractionalRadius, InterpolatorType horizontalType,
			InterpolatorType radialType, boolean reciprocal, int[] attributes)
					throws GeoTessException
	{
		int nlat = latitudes.length;
		int nlon = longitudes.length;
		if (attributes == null)
		{
			attributes = new int[model.getMetaData().getNAttributes()];
			for (int i = 1; i < attributes.length; ++i)
				attributes[i] = i;
		}
		double[][][] map = new double[nlat][nlon][attributes.length];

		GeoTessPosition pos = GeoTessPosition.getGeoTessPosition(model,
				horizontalType, radialType);
		for (int i = 0; i < nlat; ++i)
			for (int j = 0; j < nlon; ++j)
			{
				pos.set(layerId, latitudes[i], longitudes[j], 0.);
				pos.setRadius(layerId, pos.getRadiusBottom()
						+ (float) (fractionalRadius * pos.getLayerThickness()));
				for (int k = 0; k < attributes.length; ++k)
					map[i][j][k] = reciprocal ? 1. / pos
							.getValue(attributes[k]) : pos
							.getValue(attributes[k]);
			}
		return map;
	}

	/**
	 * Retrieve a 3D block of attribute values on a regular lat-lon-radius grid.
	 * 
	 * @param model
	 *            the GeoTessModel to be interrogated.
	 * @param latitudes
	 *            array of latitude values in degrees.
	 * @param longitudes
	 *            array of longitude values in degrees.
	 * @param firstLayer
	 *            index of deepest layer
	 * @param lastLayer
	 *            index of shallowest layer
	 * @param radialDimension
	 *            specifies what values to put in the radialDimension: radius,
	 *            depth, or layerIndex
	 * @param maxRadialSpacing
	 *            radial spacing of points in the output will be no larger than
	 *            this value. The actual radial spacing of points will likely be
	 *            less so that the number of radii in each layer will be
	 *            constant.
	 * @param horizontalType
	 *            either InterpolatorType.LINEAR or
	 *            InterpolatorType.NATURAL_NEIGHBOR
	 * @param radialType
	 *            either InterpolatorType.LINEAR or
	 *            InterpolatorType.CUBIC_SPLINE
	 * @param reciprocal
	 *            if false, return value; if true, return 1./value.
	 * @param attributes
	 *            indexes of the attributes to include.
	 * @return double[nlon][nlat][nradii][nAttributes+1] a 4D block of attribute
	 *         values at the nodes of the 3D lon-lat-radius block. The first
	 *         element of the attribute array is either the depth, radius, or
	 *         fractional layerIndex of the node. The remaining elements are the
	 *         values of the requested attributes.
	 * @throws GeoTessException
	 */
	static public double[][][][] getValues3D(GeoTessModel model,
			double[] latitudes, double[] longitudes, int firstLayer,
			int lastLayer, String radialDimension, double maxRadialSpacing,
			InterpolatorType horizontalType, InterpolatorType radialType,
			boolean reciprocal, int[] attributes) throws GeoTessException
	{
		if (attributes == null)
		{
			attributes = new int[model.getMetaData().getNAttributes()];
			for (int i = 1; i < attributes.length; ++i)
				attributes[i] = i;
		}

		int nlat = latitudes.length;
		int nlon = longitudes.length;
		int nradii = 0;
		int[] pointsPerLayer = new int[model.getNLayers()];

		// convert all the latitudes and longitudes to unit vectors.
		double[][][] map = new double[nlon][nlat][];
		for (int i = 0; i < nlon; ++i)
			for (int j = 0; j < nlat; ++j)
				map[i][j] = model.getEarthShape().getVectorDegrees(
						latitudes[j], longitudes[i]);

		GeoTessPosition pos = GeoTessPosition.getGeoTessPosition(model,
				horizontalType, radialType);

		int rdim = -1;
		if (radialDimension.toLowerCase().startsWith("lay"))
			rdim = 0;
		else if (radialDimension.toLowerCase().startsWith("dep"))
			rdim = 1;
		else if (radialDimension.toLowerCase().startsWith("radi"))
			rdim = 2;
		else
			throw new GeoTessException(
					"\n"
							+ radialDimension
							+ " is not a recognized value for parameter radialDimension.\n"
							+ "Must be one of radius, depth or layerIndex");

		if (rdim == 0)
		{
			nradii = 0;
			int n = (int) Math.ceil(1. / maxRadialSpacing) + 1;
			for (int i = firstLayer; i <= lastLayer; ++i)
			{
				pointsPerLayer[i] = n;
				nradii += n;
			}
		}
		else
			for (int i = 0; i < nlon; ++i)
				for (int j = 0; j < nlat; ++j)
				{
					pos.setTop(model.getNLayers() - 1, map[i][j]);
					nradii = updatePointsPerLayer(pos, firstLayer, lastLayer,
							maxRadialSpacing, pointsPerLayer);
				}

		double[][][][] values = new double[nlon][nlat][nradii][attributes.length + 1];
		double[][][] vlon;
		double[][] vlat;
		double[] vradii;

		double dr, rbot;
		for (int i = 0; i < nlon; ++i)
		{
			vlon = values[i];
			for (int j = 0; j < nlat; ++j)
			{
				vlat = vlon[j];

				nradii = 0;
				for (int layer = firstLayer; layer <= lastLayer; ++layer)
				{

					pos.set(layer, map[i][j], 6371.);

					rbot = pos.getRadiusBottom(layer);
					dr = (pos.getRadiusTop(layer) - rbot)
							/ (pointsPerLayer[layer] - 1);
					for (int k = 0; k < pointsPerLayer[layer]; ++k)
					{
						vradii = vlat[nradii++];

						pos.set(layer, map[i][j], rbot + k * dr);

						switch (rdim)
						{
						case 0:
							// set radial dimension value to fractional layer
							// index.
							vradii[0] = layer + ((double) k)
							/ (pointsPerLayer[layer] - 1);
							break;
						case 1:
							// set radial dimension value to depth in km.
							vradii[0] = pos.getDepth();
							break;
						default:
							// set radial dimension value to radius in km.
							vradii[0] = pos.getRadius();
							break;
						}

						for (int a = 0; a < attributes.length; ++a)
							vradii[a + 1] = reciprocal ? 1. / pos
									.getValue(attributes[a]) : pos
									.getValue(attributes[a]);
					}
				}
			}
		}
		return values;
	}

	/**
	 * Retrieve a map of the combined thickness of a set of specified layers, in
	 * km. The reported thicknesses will be the sum of the thicknesses of all
	 * the layers between the bottom of the first layer and the top of the last
	 * layer, inclusive. Results are returned in an nLat x nLon array with
	 * longitude dimension varying fastest.
	 * 
	 * @param model
	 *            the GeoTessModel to be interrogated.
	 * @param latitudes
	 *            array of latitude values in degrees.
	 * @param longitudes
	 *            array of longitude values in degrees.
	 * @param firstLayer
	 *            the index of the deepest layer
	 * @param lastLayer
	 *            the index of the shallowest layer.
	 * @param horizontalType
	 *            either InterpolatorType.LINEAR or
	 *            InterpolatorType.NATURAL_NEIGHBOR
	 * @return map of layer thicknesses in an nLat x nLon array.
	 * @throws GeoTessException
	 */
	public static double[][] getMapLayerThickness(GeoTessModel model,
			double[] latitudes, double[] longitudes, int firstLayer,
			int lastLayer, InterpolatorType horizontalType)
					throws GeoTessException
	{
		int nlat = latitudes.length;
		int nlon = longitudes.length;

		if (lastLayer >= model.getNLayers())
			lastLayer = model.getNLayers() - 1;

		double[][] map = new double[nlat][nlon];

		GeoTessPosition pos = GeoTessPosition.getGeoTessPosition(model,
				horizontalType);

		for (int i = 0; i < nlat; ++i)
			for (int j = 0; j < nlon; ++j)
			{
				pos.setTop(model.getNLayers() - 1, model.getEarthShape()
						.getVectorDegrees(latitudes[i], longitudes[j]));
				map[i][j] = pos.getRadiusTop(lastLayer)
						- pos.getRadiusBottom(firstLayer);
			}
		return map;
	}

	/**
	 * Retrieve a map of the depth or radius of the top or bottom of a specified
	 * layer on a regular latitude-longitude grid.
	 * 
	 * @param model
	 *            the GeoTessModel to be interrogated.
	 * @param latitudes
	 *            array of latitude values in degrees.
	 * @param longitudes
	 *            array of longitude values in degrees.
	 * @param layerId
	 *            layer index
	 * @param top
	 *            if true, depth of top of layer is reported. Otherwise, depth
	 *            of bottom of layer.
	 * @param convertToDepth
	 *            if true, depths are reported, otherwise radii.
	 * @param horizontalType
	 *            either InterpolatorType.LINEAR or
	 *            InterpolatorType.NATURAL_NEIGHBOR
	 * @return double[nlat][nlon]
	 * @throws GeoTessException
	 */
	static public double[][] getMapLayerBoundary(GeoTessModel model,
			double[] latitudes, double[] longitudes, int layerId, boolean top,
			boolean convertToDepth, InterpolatorType horizontalType)
					throws GeoTessException
	{
		int nlat = latitudes.length;
		int nlon = longitudes.length;
		double[][] map = new double[nlat][nlon];

		GeoTessPosition pos = GeoTessPosition.getGeoTessPosition(model,
				horizontalType);
		for (int i = 0; i < nlat; ++i)
			for (int j = 0; j < nlon; ++j)
			{
				pos.set(layerId, latitudes[i], longitudes[j], 0);
				if (convertToDepth)
					map[i][j] = top ? pos.getDepthTop() : pos.getDepthBottom();
					else
						map[i][j] = top ? pos.getRadiusTop() : pos
								.getRadiusBottom();
			}
		return map;
	}

	/**
	 * Retrieve attribute values interpolated on a vertical slice through a
	 * model.
	 * 
	 * @param model
	 *            the GeoTessModel from which slice will be extracted
	 * @param greatCircle
	 *            the greatCircle that defines the slice
	 * @param nx
	 *            number of points along great circle path
	 * @param maxRadialSpacing
	 *            radial spacing of points will be less than or equal to this
	 *            value (km).
	 * @param firstLayer
	 *            index of the first layer to include (deepest)
	 * @param lastLayer
	 *            index of the last layer to include (shallowest)
	 * @param horizontalType
	 *            either InterpolatorType.LINEAR or
	 *            InterpolatorType.NATURAL_NEIGHBOR
	 * @param radialType
	 *            either InterpolatorType.LINEAR or
	 *            InterpolatorType.CUBIC_SPLINE
	 * @param spatialCoordinates
	 *            coordinate values to be output along with requested model
	 *            attributes. A comma delineated String containing a subset of
	 *            the following strings in any order:
	 *            <ol start=0>
	 *            <li>distance -- distance in degrees from x0
	 *            <li>depth -- depth in km
	 *            <li>radius -- radius in km
	 *            <li>x -- observer's 'right' in km
	 *            <li>y -- observer's 'up' in km
	 *            <li>z -- direction pointing toward the observer, in km
	 *            <li>lat -- latitude in degrees
	 *            <li>lon -- longitude in degrees
	 *            </ol>
	 * 
	 * @param reciprocal
	 *            if false, return value; if true, return 1./value.
	 * @param attributes
	 *            indexes of the attributes to include.
	 * @return double[nx][nPoints][spatialCoordinates.length + nAttributes]. The
	 *         values of spatial coordinates will be output first, followed
	 *         attribute values. Points will be evenly spaced radially within
	 *         each layer, with two points on each layer boundary, one
	 *         associated with values for the top of the layer below the
	 *         boundary and the other associated with values for the bottom of
	 *         the layer above the boundary. The first element of each attribute
	 *         array is either the radius or depth of the corresponding point.
	 *         Subsequent elements are interpolated values of the attributes.
	 * @throws GeoTessException
	 */
	static public double[][][] getSlice(GeoTessModel model,
			GreatCircle greatCircle, int nx, double maxRadialSpacing,
			int firstLayer, int lastLayer, InterpolatorType horizontalType,
			InterpolatorType radialType, String spatialCoordinates,
			boolean reciprocal, int[] attributes) throws GeoTessException
	{

		if (attributes == null)
		{
			attributes = new int[model.getMetaData().getNAttributes()];
			for (int i = 1; i < attributes.length; ++i)
				attributes[i] = i;
		}

		if (lastLayer >= model.getMetaData().getNLayers())
			lastLayer = model.getMetaData().getNLayers() - 1;

		String[] coordinates = spatialCoordinates.split(",");

		int[] pointsPerLayer = new int[model.getMetaData().getNLayers()];

		// delta is total distance in radians between x0 and x1
		// double delta = greatCircle.getDistance();

		// dx is path increment that produces nx equally spaced points
		// along the great circle (radians).
		double dx = greatCircle.getDistance() / (nx - 1);

		// find great circle, which consists of two unit vectors.
		// First one is a copy of x0, and second one is on the
		// great circle defined by x0 and x1 but located PI/2 radians
		// away from x0.
		// double[][] greatCircle = GeoTessUtils.getGreatCircle(x0, x1);

		boolean flipOrder = true;
		for (int i = 0; i < coordinates.length; ++i)
		{
			String coord = coordinates[i].toLowerCase().trim();
			if (coord.equals("radius"))
			{
				flipOrder = false;
				break;
			}
		}

		double[][] transform = null;
		double[] g = null;
		for (int i = 0; i < coordinates.length; ++i)
		{
			String coord = coordinates[i].toLowerCase().trim();
			if (coord.equals("x") || coord.equals("y") || coord.equals("z"))
			{
				transform = greatCircle.getTransform();
				g = new double[3];
				break;
			}
		}

		// intantiate a GeoTessPosition object to use for interpolation.
		GeoTessPosition pos = GeoTessPosition.getGeoTessPosition(model,
				horizontalType, radialType);

		// loop over points along great circle and figure out how many
		// nodes are required in each layer so that (1) the number of
		// nodes in a given layer will be constant along the slice, and
		// (2) the radial node spacing in a given layer will not exceed
		// maxSpacing.
		double[] u = new double[3];
		for (int i = 0; i < nx; ++i)
		{
			// find unit vector for current point.
			greatCircle.getPoint(i * dx, u);

			// loop over the requested layers
			for (int j = firstLayer; j <= lastLayer; ++j)
			{
				// set the interpolation point
				pos.setTop(j, u);

				// update pointsPerLayer
				updatePointsPerLayer(pos, j, j, maxRadialSpacing,
						pointsPerLayer);
			}
		}

		ArrayListDouble output = new ArrayListDouble();
		double[][][] transect = new double[nx][][];
		int layerid = model.getMetaData().getNLayers() - 1;

		// loop over all the points along the great circle and populate the
		// data values.
		for (int i = 0; i < nx; ++i)
		{
			double distance = i * dx;

			// find unit vector for current point.
			greatCircle.getPoint(distance, u);

			// set the interpolation point
			pos.setTop(layerid, u);

			// get borehole at this position. First element is radius,
			// followed by attribute values.
			transect[i] = getBorehole(pos, pointsPerLayer, false, reciprocal,
					attributes);

			if (flipOrder)
				for (int j = 0; j < transect[i].length / 2; ++j)
				{
					int k = transect[i].length - 1 - j;
					double[] tmp = transect[i][j];
					transect[i][j] = transect[i][k];
					transect[i][k] = tmp;
				}

			for (int j = 0; j < transect[i].length; ++j)
			{
				output.clear();
				pos.setRadius(layerid, transect[i][j][0]);

				if (transform != null)
				{
					double[] xx = pos.getVector().clone();
					for (int k = 0; k < 3; ++k)
						xx[k] *= pos.getRadius();
					GeoTessUtils.transform(xx, transform, g);
				}

				for (int k = 0; k < coordinates.length; ++k)
				{
					String coord = coordinates[k].toLowerCase().trim();
					if (coord.equals("x"))
						output.add(g[0]);
					else if (coord.equals("y"))
						output.add(g[1]);
					else if (coord.equals("z"))
						output.add(g[2]);
					else if (coord.equals("distance"))
						output.add(Math.toDegrees(distance));
					else if (coord.equals("depth"))
						output.add(pos.getDepth());
					else if (coord.equals("radius"))
						output.add(pos.getRadius());
					else if (coord.equals("lat"))
						output.add(model.getEarthShape().getLatDegrees(
								pos.getVector()));
					else if (coord.equals("lon"))
						output.add(model.getEarthShape().getLonDegrees(
								pos.getVector()));
					else
						output.add(Double.NaN);
				}
				for (int k = 1; k < transect[i][j].length; ++k)
					output.add(transect[i][j][k]);

				transect[i][j] = output.toArray();
			}

		}
		return transect;
	}

	/**
	 * Retrieve interpolated attribute values along a radial 'borehole' at the
	 * specified position.
	 * 
	 * @param pos
	 *            the geographic position where the profile is desired.
	 * @param maxSpacing
	 *            maximum radial spacing in km of points along the radial
	 *            profile. Actual radial spacing will generally be somewhat less
	 *            than the requested value so that there will be an integral
	 *            number of equally spaced points along the profile.
	 * @param firstLayer
	 *            index of first layer to be evaluated.
	 * @param lastLayer
	 *            index of last layer to be evaluated, plus 1.
	 * @param convertToDepth
	 *            if true, radii are converted to depth
	 * @param reciprocal
	 *            if false, return value; if true, return 1./value.
	 * @param attributes
	 *            indexes of the attributes to include.
	 * @return double[nPoints][nAttributes+1]. Points will be evenly spaced
	 *         within each layer, with two points on each layer boundary, one
	 *         associated with values for the top of the layer below the
	 *         boundary and the other associated with values for the bottom of
	 *         the layer above the boundary. The first element of each attribute
	 *         array is either the radius or depth of the corresponding point.
	 *         Subsequent elements are interpolated values of the attributes.
	 * @throws GeoTessException
	 */
	static public String getBoreholeString(GeoTessPosition pos,
			double maxSpacing, int firstLayer, int lastLayer,
			boolean convertToDepth, boolean reciprocal, int[] attributes)
					throws GeoTessException
	{
		StringBuffer buf = new StringBuffer();
		double[][] borehole = getBorehole(pos, maxSpacing, firstLayer,
				lastLayer, convertToDepth, reciprocal, attributes);

		for (int i = 0; i < borehole.length; ++i)
			if (borehole[i].length > 0)
			{
				buf.append(String.format(" %9.3f", borehole[i][0]));
				for (int j = 1; j < borehole[i].length; ++j)
					buf.append(String.format(" %10.7g", borehole[i][j]));
				buf.append(String.format("%n"));
			}

		return buf.toString();
	}

	static public String profileToString(GeoTessModel model, int vertex)
	{
		return profileToString(model, vertex, 0, model.getNLayers() - 1);
	}

	static public String profileToString(GeoTessModel model, int vertex,
			int firstLayer, int lastLayer)
	{
		StringBuffer buf = new StringBuffer();
		double[] u = model.getGrid().getVertex(vertex);
		buf.append(String.format(
				"Profile for vertex %d  lat,lon: %s   Earth radius (%s): %1.3f %n%n",
				vertex,
				model.getEarthShape().getLatLonString(u), 
				model.getEarthShape().toString(),
				model.getEarthShape().getEarthRadius(u)));
		buf.append(String.format(" Layer    Depth"));
		for (int i = 0; i < model.getMetaData().getNAttributes(); ++i)
			buf.append(String.format(" %10s", model.getMetaData()
					.getAttributeName(i)));
		buf.append(GeoTessUtils.NL);
		for (int layer = model.getNLayers() - 1; layer >= 0; --layer)
			if (layer >= firstLayer && layer <= lastLayer)
				buf.append(profileToString(model, vertex, layer));
		return buf.toString();
	}

	static public String profileToString(GeoTessModel model, int vertex,
			int layer)
	{
		StringBuffer buf = new StringBuffer();
		Profile p = model.getProfile(vertex, layer);
		double re = model.getEarthShape().getEarthRadius(
				model.getGrid().getVertex(vertex));
		for (int i = p.getNRadii() - 1; i >= 0; --i)
		{
			buf.append(String.format("%6s %8.3f",layer, re - p.getRadius(i)));
			for (int j = 0; j < model.getMetaData().getNAttributes(); ++j)
				buf.append(String.format(
						" %10.7g",
						p.getType() == ProfileType.EMPTY ? Double.NaN : i < p
								.getNData() ? p.getValue(j, i) : p.getValue(j,
										0)));
			buf.append('\n');
		}
		return buf.toString();
	}

	/**
	 * Retrieve interpolated attribute values along a radial 'borehole' at the
	 * specified position.
	 * 
	 * @param pos
	 *            GeoTessPosition object where attribute values are to be
	 *            interpolated
	 * @param maxSpacing
	 *            maximum radial spacing in km of points along the radial
	 *            profile. Actual radial spacing will generally be somewhat less
	 *            than the requested value so that there will be an integral
	 *            number of equally spaced points along the profile.
	 * @param firstLayer
	 *            index of deepest layer
	 * @param lastLayer
	 *            index of shallowest layer. If greater than nLayers-1, the
	 *            nLayers-1 is used instead.
	 * @param convertToDepth
	 *            if true radii are converted to depth
	 * @param reciprocal
	 *            if false, return value; if true, return 1./value.
	 * @param attributes
	 *            indexes of the attributes to include.
	 * @return double[nPoints][nAttributes+1]. Points will be evenly spaced
	 *         within each layer, with two points on each layer boundary, one
	 *         associated with values for the top of the layer below the
	 *         boundary and the other associated with values for the bottom of
	 *         the layer above the boundary. The first element of each attribute
	 *         array is either the radius or depth of the corresponding point.
	 *         Subsequent elements are interpolated values of the attributes.
	 * @throws GeoTessException
	 */
	static public double[][] getBorehole(GeoTessPosition pos,
			double maxSpacing, int firstLayer, int lastLayer,
			boolean convertToDepth, boolean reciprocal, int[] attributes)
					throws GeoTessException
	{
		int[] ppl = new int[pos.getModel().getMetaData().getNLayers()];
		updatePointsPerLayer(pos, firstLayer, lastLayer, maxSpacing, ppl);
		return getBorehole(pos, ppl, convertToDepth, reciprocal, attributes);
	}

	/**
	 * Retrieve interpolated attribute values along a radial 'borehole' at the
	 * specified position.
	 * 
	 * @param pos
	 *            GeoTessPosition object where attribute values are to be
	 *            interpolated
	 * @param pointsPerLayer
	 *            an int[] of length nLayers where each element specifies the
	 *            number of nodes that are to be generated in that layer.
	 * @param convertToDepth
	 *            if false, the radii are retured. If true, depths are returned.
	 * @param reciprocal
	 *            if false, return value; if true, return 1./value.
	 * @param attributes
	 *            indexes of the attributes to include.
	 * @return double[nPoints][nAttributes+1]. Points will be evenly spaced
	 *         within each layer, with two points on each layer boundary, one
	 *         associated with values for the top of the layer below the
	 *         boundary and the other associated with values for the bottom of
	 *         the layer above the boundary. The first element of each attribute
	 *         array is either the radius or depth of the corresponding point.
	 *         Subsequent elements are interpolated values of the attributes.
	 * @throws GeoTessException
	 */
	static public double[][] getBorehole(GeoTessPosition pos,
			int[] pointsPerLayer, boolean convertToDepth, boolean reciprocal,
			int[] attributes) throws GeoTessException
	{
		int originalLayer = pos.getLayerId();
		double originalRadius = pos.getRadius();

		int size = 0;
		for (int i : pointsPerLayer)
			size += i;

		if (attributes == null)
		{
			attributes = new int[pos.getModel().getMetaData().getNAttributes()];
			for (int i = 1; i < attributes.length; ++i)
				attributes[i] = i;
		}

		double[][] profile = new double[size][attributes.length + 1];

		int index = 0;
		double dr;
		for (int layer = 0; layer < pos.getModel().getMetaData().getNLayers(); ++layer)
			if (pointsPerLayer[layer] > 0)
			{
				if (pointsPerLayer[layer] < 2)
				{
					pos.setRadius(layer, pos.getRadiusTop(layer));
					profile[index][0] = convertToDepth ? pos.getDepth() : pos.getRadius();
					for (int a = 0; a < attributes.length; ++a)
						profile[index][a + 1] = reciprocal ? 1.0 / pos.getValue(attributes[a]) : pos.getValue(attributes[a]);
						++index;
				}
				else
				{
					dr = pos.getLayerThickness(layer) / (pointsPerLayer[layer] - 1);

					for (int p = 0; p < pointsPerLayer[layer]; ++p)
					{
						pos.setRadius(layer, p * dr + pos.getRadiusBottom(layer));
						profile[index][0] = convertToDepth ? pos.getDepth() : pos
								.getRadius();
						for (int a = 0; a < attributes.length; ++a)
							profile[index][a + 1] = reciprocal ? 1.0 / pos
									.getValue(attributes[a]) : pos
									.getValue(attributes[a]);
									++index;
					}
				}
			}

		if (convertToDepth)
		{
			for (int i = 0; i < profile.length / 2; ++i)
			{
				int j = profile.length - 1 - i;
				double[] tmp = profile[i];
				profile[i] = profile[j];
				profile[j] = tmp;
			}
		}

		pos.setRadius(originalLayer, originalRadius);

		return profile;
	}

	/**
	 * Evaluates maximum number of nodes per layer. On input, pointsPerLayer is
	 * an array of length nLayers where each element contains a current estimate
	 * of the number of nodes that must be deployed on the corresponding layer
	 * so that the node spacing will be no greater than maxSpacing (in km). The
	 * values in pointsPerLayer will be evaluated at the specified position and
	 * increased if necessary. Only layers between firstLayer and lastLayer,
	 * inclusive, will be evaluated.
	 * 
	 * @param pos
	 *            GeoTessPosition object
	 * 
	 * @param firstLayer
	 *            first layer to be considered
	 * @param lastLayer
	 *            last layer to be considered
	 * @param maxSpacing
	 *            maximum radial node spacing in km
	 * @param pointsPerLayer
	 *            an array of length model.getNLayers() containing current
	 *            estimate of the number of nodes per layer such that the node
	 *            spacing will be no larger than maxSpacing.
	 * @return returns the total number of points in all layers, i.e., the sum
	 *         of the elements of pointsPerLayer.
	 * @throws GeoTessException
	 */
	static public int updatePointsPerLayer(GeoTessPosition pos, int firstLayer,
			int lastLayer, double maxSpacing, int[] pointsPerLayer)
					throws GeoTessException
	{
		int n, nTotal = 0;
		for (int i = 0; i < pos.getModel().getNLayers(); ++i)
		{
			if (i >= firstLayer && i <= lastLayer)
			{
				if (maxSpacing <= 0)
					n = 1;
				else
					n = max(2,
							1 + (int) Math.ceil(pos.getLayerThickness(i) / maxSpacing));

				if (n > pointsPerLayer[i])
					pointsPerLayer[i] = n;
			}
			nTotal += pointsPerLayer[i];
		}
		return nTotal;
	}

	/**
	 * Retrieve array of latitude values.
	 * 
	 * @param first
	 *            first latitude (degrees or radians)
	 * @param last
	 *            last latitude (degrees or radians)
	 * @param n
	 *            number of latitudes
	 * @return array of latitudes (degrees or radians)
	 */
	public static double[] getLatitudes(double first, double last, int n)
	{
		return getLatitudes(first, last, n, false);
	}

	/**
	 * Retrieve array of latitude values.
	 * 
	 * @param first
	 *            first latitude (degrees or radians)
	 * @param last
	 *            last latitude (degrees or radians)
	 * @param dlat
	 *            desired spacing. Actual spacing will generally be less in
	 *            order for there to be an integer number of equally spaced
	 *            values (degrees or radians)
	 * @return array of latitudes (degrees or radians)
	 */
	public static double[] getLatitudes(double first, double last, double dlat)
	{
		return getLatitudes(first, last, dlat, false);
	}

	/**
	 * Convert strings to doubles (or ints) and then generate latitudes[]
	 * 
	 * @param s1
	 *            first first latitude (degrees or radians)
	 * @param s2
	 *            last last latitude (degrees or radians)
	 * @param ds
	 *            maybe dlat, maybe nlat
	 * @return array of latitudes (degrees or radians)
	 */
	public static double[] getLatitudes(String s1, String s2, String ds)
	{
		return getLatitudes(s1, s2, ds, false);
	}

	/**
	 * Retrieve array of latitude values.
	 * 
	 * @param first
	 *            first latitude (degrees or radians)
	 * @param last
	 *            last latitude (degrees or radians)
	 * @param n
	 *            number of latitudes
	 * @param onCenters
	 *            if false, first value will correspond to x0, last value with
	 *            x1, and remaining values will be equally spaced. If true,
	 *            there will n intervals of width dx with a coordinate value in
	 *            the center of each interval.
	 * @return array of latitudes (degrees or radians)
	 */
	public static double[] getLatitudes(double first, double last, int n,
			boolean onCenters)
	{
		return getXArray(first, last, n, onCenters);
	}

	/**
	 * Retrieve array of latitude values.
	 * 
	 * @param first
	 *            first latitude (degrees or radians)
	 * @param last
	 *            last latitude (degrees or radians)
	 * @param dlat
	 *            desired spacing. Actual spacing will generally be less in
	 *            order for there to be an integer number of equally spaced
	 *            values (degrees or radians)
	 * @param onCenters
	 *            if false, first value will correspond to x0, last value with
	 *            x1, and remaining values will be equally spaced. If true,
	 *            there will n intervals of width dx with a coordinate value in
	 *            the center of each interval.
	 * @return array of latitudes (degrees or radians)
	 */
	public static double[] getLatitudes(double first, double last, double dlat,
			boolean onCenters)
	{
		return getXArray(first, last, dlat, onCenters);
	}

	/**
	 * Convert strings to doubles (or ints) and then generate latitudes[]
	 * 
	 * @param s1
	 *            first first latitude (degrees or radians)
	 * @param s2
	 *            last last latitude (degrees or radians)
	 * @param ds
	 *            maybe dlat, maybe nlat
	 * @param onCenters
	 *            if false, first value will correspond to x0, last value with
	 *            x1, and remaining values will be equally spaced. If true,
	 *            there will n intervals of width dx with a coordinate value in
	 *            the center of each interval.
	 * @return array of latitudes (degrees or radians)
	 */
	public static double[] getLatitudes(String s1, String s2, String ds,
			boolean onCenters)
	{
		double lat1 = Double.parseDouble(s1);
		double lat2 = Double.parseDouble(s2);
		if (ds.contains("."))
		{
			double dlat = Double.parseDouble(ds);
			return getLatitudes(lat1, lat2, dlat);
		}
		else
		{
			int nlat = Integer.parseInt(ds);
			return getLatitudes(lat1, lat2, nlat, onCenters);
		}
	}

	/**
	 * Retrieve array of monotonically increasing longitude values.
	 * 
	 * @param first
	 *            first longitude
	 * @param last
	 *            last longitude
	 * @param n
	 *            number of longitudes
	 * @param inDegrees
	 *            specify true if using degrees, false if using radians
	 * @return array of longitudes
	 */
	public static double[] getLongitudes(double first, double last, int n,
			boolean inDegrees)
	{
		return getLongitudes(first, last, n, inDegrees, false);
	}

	/**
	 * Retrieve array of monotonically increasing longitude values.
	 * 
	 * @param first
	 *            first longitude
	 * @param last
	 *            last longitude
	 * @param dlon
	 *            desired spacing. Actual spacing will generally be less in
	 *            order for there to be an integer number of equally spaced
	 *            values.
	 * @param inDegrees
	 *            if true, arguments are interpreted in degrees, otherwise
	 *            radians
	 * @return array of longitudes
	 */
	public static double[] getLongitudes(double first, double last,
			double dlon, boolean inDegrees)
	{
		return getLongitudes(first, last, dlon, inDegrees, false);
	}

	/**
	 * Convert strings to doubles (or ints) and then generate longitudes[]
	 * 
	 * @param s1
	 *            first longitude
	 * @param s2
	 *            last longitude
	 * @param ds
	 *            maybe dlon, maybe nlon
	 * @param inDegrees
	 *            true or false
	 * @return array of longitudes
	 */
	public static double[] getLongitudes(String s1, String s2, String ds,
			String inDegrees)
	{
		return getLongitudes(s1, s2, ds, inDegrees, false);
	}

	/**
	 * Retrieve array of monotonically increasing longitude values.
	 * 
	 * @param first
	 *            first longitude
	 * @param last
	 *            last longitude
	 * @param n
	 *            number of longitudes
	 * @param inDegrees
	 *            specify true if using degrees, false if using radians
	 * @param onCenters
	 *            if false, first value will correspond to x0, last value with
	 *            x1, and remaining values will be equally spaced. If true,
	 *            there will n intervals of width dx with a coordinate value in
	 *            the center of each interval.
	 * @return array of longitudes
	 */
	public static double[] getLongitudes(double first, double last, int n,
			boolean inDegrees, boolean onCenters)
	{
		double pi = inDegrees ? 180. : PI;
		while (last <= first)
			last += 2 * pi;
		while (last > first + 2 * pi)
			last -= 2 * pi;

		while (last > 2 * pi)
		{
			first -= 2 * pi;
			last -= 2 * pi;
		}

		while (first < -pi)
		{
			first += 2 * pi;
			last += 2 * pi;
		}

		return getXArray(first, last, n, onCenters);
	}

	/**
	 * Retrieve array of monotonically increasing longitude values.
	 * 
	 * @param first
	 *            first longitude
	 * @param last
	 *            last longitude
	 * @param dlon
	 *            desired spacing. Actual spacing will generally be less in
	 *            order for there to be an integer number of equally spaced
	 *            values.
	 * @param inDegrees
	 *            if true, arguments are interpreted in degrees, otherwise
	 *            radians
	 * @param onCenters
	 *            if false, first value will correspond to x0, last value with
	 *            x1, and remaining values will be equally spaced. If true,
	 *            there will n intervals of width dx with a coordinate value in
	 *            the center of each interval.
	 * @return array of longitudes
	 */
	public static double[] getLongitudes(double first, double last,
			double dlon, boolean inDegrees, boolean onCenters)
	{
		double pi = inDegrees ? 180. : PI;
		while (last <= first)
			last += 2 * pi;
		while (last > first + 2 * pi)
			last -= 2 * pi;

		while (last > 2 * pi)
		{
			first -= 2 * pi;
			last -= 2 * pi;
		}

		while (first < -pi)
		{
			first += 2 * pi;
			last += 2 * pi;
		}

		return getXArray(first, last, dlon, onCenters);
	}

	/**
	 * Convert strings to doubles (or ints) and then generate longitudes[]
	 * 
	 * @param s1
	 *            first longitude
	 * @param s2
	 *            last longitude
	 * @param ds
	 *            maybe dlon, maybe nlon
	 * @param inDegrees
	 *            true or false
	 * @param onCenters
	 *            if false, first value will correspond to x0, last value with
	 *            x1, and remaining values will be equally spaced. If true,
	 *            there will n intervals of width dx with a coordinate value in
	 *            the center of each interval.
	 * @return array of longitudes
	 */
	public static double[] getLongitudes(String s1, String s2, String ds,
			String inDegrees, boolean onCenters)
	{
		double l1 = Double.parseDouble(s1);
		double l2 = Double.parseDouble(s2);
		boolean b = Boolean.parseBoolean(inDegrees);
		if (ds.contains("."))
		{
			double dl = Double.parseDouble(ds);
			return getLongitudes(l1, l2, dl, b, onCenters);
		}
		else
		{
			int nl = Integer.parseInt(ds);
			return getLongitudes(l1, l2, nl, b, onCenters);
		}
	}

	/**
	 * Retrieve array of equally spaced values.
	 * 
	 * @param x0
	 *            first value
	 * @param x1
	 *            last value
	 * @param dx
	 *            desired spacing. Actual spacing will generally be less in
	 *            order for there to be an integer number of equally spaced
	 *            values.
	 * @param onCenters
	 *            if false, first value will correspond to x0, last value with
	 *            x1, and remaining values will be equally spaced. If true,
	 *            there will n intervals of width dx with a coordinate value in
	 *            the center of each interval.
	 * @return array of equally spaced values
	 */
	public static double[] getXArray(double x0, double x1, double dx,
			boolean onCenters)
	{
		int n = (int) ceil(abs(x1 - x0) / dx) + (onCenters ? 0 : 1);
		return getXArray(x0, x1, n, onCenters);
	}

	/**
	 * Retrieve array of monotonically increasing, equally spaced values.
	 * 
	 * @param x0
	 *            first value
	 * @param x1
	 *            last value
	 * @param nx
	 *            number of points
	 * @param onCenters
	 *            if false, first value will correspond to x0, last value with
	 *            x1, and remaining values will be equally spaced. If true,
	 *            there will nx intervals of equal width with a coordinate value
	 *            in the center of each interval.
	 * @return array of equally spaced values
	 */
	public static double[] getXArray(double x0, double x1, int nx,
			boolean onCenters)
	{
		if (onCenters)
		{
			if (nx == 0)
				return new double[0];

			if (x1 < x0)
				return null;

			if (nx == 1)
				return new double[] { (x0 + x1) / 2. };

			double dx = (x1 - x0) / nx;

			double[] x = new double[nx];
			x0 += 0.5 * dx;
			for (int i = 0; i < nx; ++i)
				x[i] = x0 + i * dx;
			return x;
		}
		else
		{
			if (nx == 0)
				return new double[0];

			if (nx == 1)
				return new double[] { x0 };

			if (x1 < x0)
				return null;

			if (nx == 2)
				return new double[] { x0, x1 };

			double dx = (x1 - x0) / (nx - 1);

			double[] x = new double[nx];
			for (int i = 0; i < nx; ++i)
				x[i] = x0 + i * dx;
			return x;
		}
	}

	public static String expandFileName(String fileName, String subString)
	{
		int i = fileName.lastIndexOf('.');
		return i < 0 ? fileName : fileName.substring(0, i) + subString
				+ fileName.substring(i);
	}

	/**
	 * Construct a new GeoTessModel that has Data values that are a function of
	 * the Data values in the two supplied GeoTessModels.
	 * 
	 * @param function
	 *            <ol start=0>
	 *            One of the following functions:
	 *            <li>x1 - x0; // simple difference
	 *            <li>1/x1 - 1/x0; // difference of reciprocals
	 *            <li>100 * (x1 - x0) / x0; // % change
	 *            <li>100.*(1/x1 - 1/x0) / (1/x0); // % change of reciprocals
	 *            </ol>
	 *            If function is anything else, new model will be populated with
	 *            NaN.
	 * @param m0
	 *            GeoTessModel zero. Not modified by this function.
	 * @param attribute0
	 *            the index of the attribute in model 0 that will be used in the
	 *            function.
	 * @param m1
	 *            GeoTessModel one. Not modified by this function.
	 * @param attribute1
	 *            the index of the attribute in model 1 that will be used in the
	 *            function.
	 * @param pointModel
	 *            the GeoTessModel that has the geometry and topology that will
	 *            be reproduced in the new GeoTessModel. This can be the same as
	 *            m0 or m1. Not modified by this function.
	 * @param attributeName
	 *            the attribute name that will be assigned in the new model
	 * @param attributeUnits
	 *            the attribute units that will be assigned in the new model
	 * @param horizontalType
	 *            either InterpolatorType.LINEAR or
	 *            InterpolatorType.NATURAL_NEIGHBOR
	 * @param radialType
	 *            either InterpolatorType.LINEAR or
	 *            InterpolatorType.CUBIC_SPLINE
	 * @return a new model with the geometry and topology of pointModel but with
	 *         a single attribute value computed from the values in m0 and m1.
	 * @throws GeoTessException
	 * @throws IOException
	 */
	public static GeoTessModel function(int function, GeoTessModel m0,
			int attribute0, GeoTessModel m1, int attribute1,
			GeoTessModel pointModel, String attributeName,
			String attributeUnits, InterpolatorType horizontalType,
			InterpolatorType radialType) throws GeoTessException, IOException
	{
		GeoTessModel newModel = pointModel.copy();

		GeoTessMetaData md = newModel.getMetaData();

		newModel.getGrid().setGridInputFile(null);

		md.setAttributes(attributeName, attributeUnits);

		GeoTessPosition p0 = m0.getGeoTessPosition(horizontalType, radialType);
		GeoTessPosition p1 = m1.getGeoTessPosition(horizontalType, radialType);

		PointMap pointMap = newModel.getPointMap();
		double[][] vertices = newModel.getGrid().getVertices();
		int vertex, layer;
		double v1, v0, v;

		for (int pointIndex = 0; pointIndex < newModel.getNPoints(); ++pointIndex)
		{
			vertex = pointMap.getVertexIndex(pointIndex);
			layer = pointMap.getLayerIndex(pointIndex);

			// set the interpolation positions in the two input models.
			p0.set(layer, vertices[vertex], pointMap.getPointRadius(pointIndex));
			p1.set(layer, vertices[vertex], pointMap.getPointRadius(pointIndex));

			// get attribute values from the two input models
			v0 = p0.getValue(attribute0);
			v1 = p1.getValue(attribute1);

			// apply the function. v = function(v0, v1)
			switch (function)
			{
			case 0:
				// simple difference
				v = v1 - v0;
				break;
			case 1:
				// difference of reciprocals
				v = 1 / v1 - 1 / v0;
				break;
			case 2:
				// % change of values.
				v = 100. * (v1 - v0) / v0;
				break;
			case 3:
				// % change of reciprocals
				v = 100. * (1 / v1 - 1 / v0) / (1 / v0);
				break;
			default:
				v = Double.NaN;
			}

			switch (md.getDataType())
			{
			case DOUBLE:
				newModel.getPointMap().setPointData(pointIndex,
						new DataDouble((double) v));
				break;
			case FLOAT:
				newModel.getPointMap().setPointData(pointIndex,
						new DataFloat((float) v));
				break;
			case LONG:
				newModel.getPointMap().setPointData(pointIndex,
						new DataLong((long) v));
				break;
			case INT:
				newModel.getPointMap().setPointData(pointIndex,
						new DataInt((int) v));
				break;
			case SHORT:
				newModel.getPointMap().setPointData(pointIndex,
						new DataShort((short) v));
				break;
			case BYTE:
				newModel.getPointMap().setPointData(pointIndex,
						new DataByte((byte) v));
				break;
			case CUSTOM:
				break;
			default:
				break;
			}
		}

		return newModel;
	}

	/**
	 * Generate a VTK file of the geometry, topology and data values at the top
	 * of the specified layer. There will be separate file for each layer of the
	 * model.
	 * 
	 * @param model
	 *            a reference to the model
	 * @param fileName
	 *            the name of the file to which output should be written. If the
	 *            fileName contains '%d' then the layer number is inserted into
	 *            that position of the fileName. If fileName contains '%s' then
	 *            the layer name is inserted into that position of the fileName.
	 * @param firstLayer
	 *            the first (innermost) layer to include
	 * @param lastLayer
	 *            the last (outermost) layer to include. If value specified is
	 *            greater than last layer of the model then value is replaced
	 *            with the index of the last layer in the model.
	 * @param reciprocal
	 *            if true then plot 1/attribute value instead of attribute
	 *            value.
	 * @param attributes
	 *            indexes of the attributes to include. If null, all values are
	 *            output.
	 * @throws GeoTessException
	 * @throws IOException
	 */
	public static void vtk(GeoTessModel model, String fileName, int firstLayer,
			int lastLayer, boolean reciprocal, int[] attributes)
					throws GeoTessException, IOException
	{
		for (int layer = 0; layer < model.getMetaData().getNLayers(); ++layer)
			if (layer >= firstLayer && layer <= lastLayer)
			{
				if (fileName.contains("%d"))
					vtk(model, String.format(fileName, layer), layer,
							reciprocal, attributes);
				else if (fileName.contains("%s"))
					vtk(model, String.format(fileName, model.getMetaData()
							.getLayerNames()[layer]), layer, reciprocal,
							attributes);
				else
					throw new GeoTessException(
							"\nOutput vtk file name ("
									+ fileName
									+ ") must contain either '%d' or '%s.\n"
									+ "%d is replaced with layer number, %s with layer name.\n");
			}
	}

	/**
	 * Write the model to a binary VTK file which can be viewed using ParaView
	 * (http://www.paraview.org).
	 * <p>
	 * If layerId < 0, then interpolation points are not constrained to layer
	 * boundaries.
	 * <p>
	 * If layerId is >= 0 then for every point on the map, if the specified
	 * depth is deeper than the depth of the bottom of the specified layer, then
	 * attribute values are interpolated at the bottom of the specified layer.
	 * Similarly, if the specified depth is shallower than the depth of the top
	 * of the specified layer, then attribute values at the top of the specified
	 * layer are interpolated.
	 * 
	 * @param model
	 *            a reference to the model
	 * @param fileName
	 *            the name of the file to which output should be written. Must
	 *            end with extension 'vtk'.
	 * @param horizontalType
	 *            either InterpolatorType.LINEAR or
	 *            InterpolatorType.NATURAL_NEIGHBOR
	 * @param radialType
	 *            either InterpolatorType.LINEAR or
	 *            InterpolatorType.CUBIC_SPLINE
	 * @param layerId
	 * @param depths
	 * @param reciprocal
	 *            if true, 1/value is returned instead of value.
	 * @param attributes
	 *            indexes of the attributes to include (all if null)
	 * @throws IOException
	 * @throws GeoTessException
	 */
	public static void vtkDepths(GeoTessModel model, String fileName,
			InterpolatorType horizontalType, InterpolatorType radialType,
			int layerId, double[] depths, boolean reciprocal, int[] attributes)
					throws IOException, GeoTessException
	{
		if (!fileName.toLowerCase().trim().endsWith(".vtk"))
			throw new IOException("\nOutput file name must have .vtk extension");

		if (attributes == null)
		{
			attributes = new int[model.getMetaData().getNAttributes()];
			for (int i = 1; i < attributes.length; ++i)
				attributes[i] = i;
		}

		DataOutputStream output = new DataOutputStream(
				new BufferedOutputStream(new FileOutputStream(
						new File(fileName))));

		int tessid = layerId < 0 ? model.getGrid().getNTessellations() - 1
				: model.getMetaData().getTessellation(layerId);

		int level = model.getGrid().getNLevels(tessid) - 1;

		int[] vertices = vtkGrid(model.getGrid(), tessid, level, output);

		output.writeBytes(String.format("POINT_DATA %d%n", vertices.length));

		GeoTessPosition pos = model.getGeoTessPosition(horizontalType,
				radialType);

		for (int a = 0; a < attributes.length; ++a)
		{
			String attributeName = vtkName(model, attributes[a], reciprocal);

			for (int z = 0; z < depths.length; ++z)
			{
				output.writeBytes(String.format(
						"SCALARS %s_%1.0f_km float 1%n", attributeName,
						depths[z]));
				output.writeBytes(String.format("LOOKUP_TABLE default%n"));

				for (int i = 0; i < vertices.length; ++i)
				{
					pos.set(layerId,
							model.getGrid().getVertex(vertices[i]),
							model.getEarthShape().getEarthRadius(
									model.getGrid().getVertex(vertices[i]))
							- depths[z]);

					output.writeFloat((float) (reciprocal ? 1 / pos
							.getValue(attributes[a]) : pos
							.getValue(attributes[a])));
				}
			}
		}
		output.close();
	}

	public static void vtkDepths(GeoTessModel model, String outputFile,
			InterpolatorType interpTypeHoriz,
			InterpolatorType interpTypeRadial, int layerid, double firstDepth,
			double lastDepth, double depthSpacing, boolean reciprocal,
			int[] attributes) throws IOException, GeoTessException
	{
		if (firstDepth > lastDepth)
		{
			double z = firstDepth;
			firstDepth = lastDepth;
			lastDepth = z;
		}

		int nDepths = 1;
		if (depthSpacing != 0.)
			nDepths = (int) Math.ceil((lastDepth - firstDepth) / depthSpacing) + 1;
		depthSpacing = (lastDepth - firstDepth) / (nDepths - 1);
		double[] depths = new double[nDepths];
		for (int i = 0; i < nDepths; ++i)
			depths[i] = firstDepth + i * depthSpacing;

		vtkDepths(model, outputFile, interpTypeHoriz, interpTypeRadial,
				layerid, depths, reciprocal, attributes);
	}

	/**
	 * Write either the depth or elevation of the top of each layer.
	 * 
	 * @param model
	 * @param fileName
	 * @param z
	 *            either "depth" or "elevation"
	 * @param horizontalType
	 * @throws IOException
	 * @throws GeoTessException
	 */
	public static void vtkLayerBoundary(GeoTessModel model, String fileName,
			String z, InterpolatorType horizontalType) throws IOException,
	GeoTessException
	{
		if (!fileName.toLowerCase().trim().endsWith(".vtk"))
			throw new IOException("\nOutput file name must have .vtk extension");

		DataOutputStream output = new DataOutputStream(
				new BufferedOutputStream(new FileOutputStream(
						new File(fileName))));

		int tessid = model.getMetaData()
				.getTessellation(model.getNLayers() - 1);

		int level = model.getGrid().getNLevels(tessid) - 1;

		int[] vertices = vtkGrid(model.getGrid(), tessid, level, output);

		double[] vertex;

		output.writeBytes(String.format("POINT_DATA %d%n", vertices.length));

		GeoTessPosition pos = model.getGeoTessPosition(horizontalType);

		int sign = z.toLowerCase().startsWith("e") ? -1 : 1;

		for (int layer = -1; layer < model.getNLayers(); ++layer)
		{
			output.writeBytes(String
					.format("SCALARS %s_%d float 1\n", z, layer));

			output.writeBytes(String.format("LOOKUP_TABLE default%n"));

			for (int i = 0; i < vertices.length; ++i)
			{
				vertex = model.getGrid().getVertex(vertices[i]);
				pos.setTop(model.getNLayers() - 1, vertex);

				output.writeFloat((float) (sign * layer < 0 ? pos
						.getDepthBottom(0) : pos.getDepthTop(layer)));
			}
		}

		output.close();
	}

	/**
	 * Write the model to a binary VTK file which can be viewed using ParaView
	 * (http://www.paraview.org).
	 * 
	 * @param model
	 *            a reference to the model
	 * @param fileName
	 *            the name of the file to which output should be written. Must
	 *            end with extension 'vtk'.
	 * @param firstLayer
	 *            index of first layer
	 * @param lastLayer
	 *            index of last layer. . Thickness will include first through
	 *            last layer, inclusive
	 * @param horizontalType
	 *            InterpolatorType.LINEAR or InterpolatorType.NATURAL_NEIGHBOR.
	 * @throws IOException
	 * @throws GeoTessException
	 */
	public static void vtkLayerThickness(GeoTessModel model, String fileName,
			int firstLayer, int lastLayer, InterpolatorType horizontalType)
					throws IOException, GeoTessException
	{
		if (!fileName.toLowerCase().trim().endsWith(".vtk"))
			throw new IOException("\nOutput file name must have .vtk extension");

		if (lastLayer >= model.getNLayers())
			lastLayer = model.getNLayers() - 1;

		DataOutputStream output = new DataOutputStream(
				new BufferedOutputStream(new FileOutputStream(
						new File(fileName))));

		int tessid = model.getMetaData()
				.getTessellation(model.getNLayers() - 1);

		int level = model.getGrid().getNLevels(tessid) - 1;

		int[] vertices = vtkGrid(model.getGrid(), tessid, level, output);

		double[] vertex;

		output.writeBytes(String.format("POINT_DATA %d%n", vertices.length));

		GeoTessPosition pos = model.getGeoTessPosition(horizontalType);

		output.writeBytes("SCALARS Layer_Thickness float 1\n");
		output.writeBytes(String.format("LOOKUP_TABLE default%n"));

		for (int i = 0; i < vertices.length; ++i)
		{
			vertex = model.getGrid().getVertex(vertices[i]);
			pos.setTop(model.getNLayers() - 1, vertex);

			output.writeFloat((float) (pos.getRadiusTop(lastLayer) - pos
					.getRadiusBottom(firstLayer)));
		}

		output.close();
	}

	/**
	 * Generate a VTK file of the geometry, topology and data values at the top
	 * of the specified layer.
	 * 
	 * @param model
	 *            a reference to the model
	 * @param fileName
	 *            the name of the file to which output should be written. Must
	 *            end with extension 'vtk'.
	 * @param layerId
	 *            the id of the layer to be represented
	 * @param reciprocal
	 *            if true, 1/value is returned instead of value.
	 * @param attributes
	 *            indexes of the attributes to include. If null, all values are
	 *            output.
	 * @throws IOException
	 * @throws GeoTessException
	 */
	public static void vtk(GeoTessModel model, String fileName, int layerId,
			boolean reciprocal, int[] attributes) throws IOException,
	GeoTessException
	{
		if (!fileName.toLowerCase().trim().endsWith(".vtk"))
			throw new IOException("\nOutput file name must have .vtk extension");

		if (attributes == null)
		{
			attributes = new int[model.getMetaData().getNAttributes()];
			for (int i = 1; i < attributes.length; ++i)
				attributes[i] = i;
		}

		DataOutputStream output = new DataOutputStream(
				new BufferedOutputStream(new FileOutputStream(
						new File(fileName))));

		int tessid = model.getMetaData().getTessellation(layerId);

		int level = model.getGrid().getNLevels(tessid) - 1;

		int[] vertices = vtkGrid(model.getGrid(), tessid, level, output);

		output.writeBytes(String.format("POINT_DATA %d%n", vertices.length));

		for (int a = 0; a < attributes.length; ++a)
		{
			String attributeName = vtkName(model, attributes[a], reciprocal);

			output.writeBytes(String.format("SCALARS %s float 1%n",
					attributeName));
			output.writeBytes(String.format("LOOKUP_TABLE default%n"));

			for (int i = 0; i < vertices.length; ++i)
			{
				Profile p = model.getProfile(vertices[i], layerId);
				if (p.getType() == ProfileType.EMPTY
						|| p.getType() == ProfileType.SURFACE_EMPTY)
					output.writeFloat(Float.NaN);
				else
					output.writeFloat(reciprocal ? 1F / p.getDataTop()
							.getFloat(attributes[a]) : p.getDataTop().getFloat(
									attributes[a]));
			}
		}
		output.close();
	}

	/**
	 * Generate a VTK file of the geometry, topology and data values at the top
	 * of the specified layer.
	 * 
	 * @param model
	 *            a reference to the model
	 * @param outputFile
	 *            the name of the file to which output should be written. Must
	 *            end with extension 'vtk'.
	 * @param layerId
	 *            the id of the layer to be represented
	 * @param level
	 *            tessellation level relative to only the levels in the tessellation
	 *            that supports the specified layer.
	 * @param reciprocal
	 *            if true, 1/value is returned instead of value.
	 * @param attributes
	 *            indexes of the attributes to include. If null, all values are
	 *            output.
	 * @throws IOException
	 * @throws GeoTessException
	 */
	public static void vtkLevel(GeoTessModel model, File outputFile, int layerId, int level,
			boolean reciprocal, int[] attributes) throws IOException,
	GeoTessException
	{
		if (!outputFile.getName().toLowerCase().trim().endsWith(".vtk"))
			throw new IOException("\nOutput file name must have .vtk extension");

		if (attributes == null)
		{
			attributes = new int[model.getMetaData().getNAttributes()];
			for (int i = 1; i < attributes.length; ++i)
				attributes[i] = i;
		}

		int tessid = model.getMetaData().getTessellation(layerId);

		if (level >= model.getGrid().getNLevels(tessid)) level = model.getGrid().getNLevels(tessid)-1;

		DataOutputStream output = new DataOutputStream(
				new BufferedOutputStream(new FileOutputStream(
						outputFile)));

		int[] vertices = vtkGrid(model.getGrid(), tessid, level, output);

		output.writeBytes(String.format("POINT_DATA %d%n", vertices.length));

		for (int a = 0; a < attributes.length; ++a)
		{
			String attributeName = vtkName(model, attributes[a], reciprocal);

			output.writeBytes(String.format("SCALARS %s float 1%n",
					attributeName));
			output.writeBytes(String.format("LOOKUP_TABLE default%n"));

			for (int i = 0; i < vertices.length; ++i)
			{
				Profile p = model.getProfile(vertices[i], layerId);
				if (p.getType() == ProfileType.EMPTY
						|| p.getType() == ProfileType.SURFACE_EMPTY)
					output.writeFloat(Float.NaN);
				else
					output.writeFloat(reciprocal ? 1F / p.getDataTop()
							.getFloat(attributes[a]) : p.getDataTop().getFloat(
									attributes[a]));
			}
		}
		output.close();
	}

	/**
	 * Generate a VTK file of the geometry, topology and data values at the top
	 * level of the specified tessellation.
	 * 
	 * @param model
	 *            a reference to the model
	 * @param fileName
	 *            the name of the file to which output should be written. Must
	 *            end with extension 'vtk'.
	 * @param tessId
	 *            the index of the tessellation to consider. Triangles on the
	 *            last level of this tessellation will be considered.
	 * @param triangleValues
	 *            an array of size equal to the total number of triangles in the
	 *            model. Only values from the specified tessellation and level
	 *            will be used.
	 * @throws IOException
	 * @throws GeoTessException
	 */
	public static void vtkTriangleValues(GeoTessModel model, String fileName,
			int tessId, float[] triangleValues) throws IOException,
	GeoTessException
	{
		vtkTriangleValues(model, fileName, tessId, model.getGrid()
				.getLastLevel(tessId), triangleValues);
	}

	/**
	 * Generate a VTK file of the geometry, topology and data values at the
	 * specified tessellation and level.
	 * 
	 * @param model
	 *            a reference to the model
	 * @param fileName
	 *            the name of the file to which output should be written. Must
	 *            end with extension 'vtk'.
	 * @param tessId
	 *            tessellation index
	 * @param level
	 *            index of the level relative to the first level of the
	 *            specified tessellation.
	 * @param triangleValues
	 *            an array of size equal to the total number of triangles in the
	 *            model. Only values from the specified tessellation and level
	 *            will be used.
	 * @throws IOException
	 * @throws GeoTessException
	 */
	public static void vtkTriangleValues(GeoTessModel model, String fileName,
			int tessId, int level, float[] triangleValues) throws IOException,
	GeoTessException
	{
		if (!fileName.toLowerCase().trim().endsWith(".vtk"))
			throw new IOException("\nOutput file name must have .vtk extension");

		DataOutputStream output = new DataOutputStream(
				new BufferedOutputStream(new FileOutputStream(
						new File(fileName))));

		GeoTessGrid grid = model.getGrid();

		output.writeBytes(String.format("CELL_DATA %d%n",
				grid.getNTriangles(tessId, level)));

		output.writeBytes(String.format("SCALARS Triangle_values float 1%n"));
		output.writeBytes(String.format("LOOKUP_TABLE default%n"));

		for (int t = grid.getFirstTriangle(tessId, level); t <= grid
				.getLastTriangle(tessId, level); ++t)
			output.writeFloat(triangleValues[t]);
		output.close();
	}

	/**
	 * This just writes the grid to the vtk file. Only includes the geometry and
	 * topology of the specified tessellation.
	 * 
	 * @param grid
	 * @param tessid
	 * @param output
	 * @return the indices of the vertices used in the plot.
	 * @throws IOException
	 */
	public static int[] vtkGrid(GeoTessGrid grid, int tessid,
			DataOutputStream output) throws IOException
	{
		return vtkGrid(grid, tessid, grid.getTopLevel(tessid), output);
	}

	/**
	 * This just writes the grid to the vtk file. Only includes the geometry and
	 * topology of the specified tessellation.
	 * 
	 * @param grid
	 * @param tessid
	 * @param level index of level relative to first level of the specified 
	 * tessellation.
	 * @param output
	 * @return the indices of the vertices used in the plot.
	 * @throws IOException
	 */
	public static int[] vtkGrid(GeoTessGrid grid, int tessid, int level,
			DataOutputStream output) throws IOException
	{
		// get the indexes of the vertices on desired level.
		HashSetInteger s = grid.getVertexIndices(tessid, level);
		int[] vertices = new int[s.size()];
		{
			int n = 0;
			Iterator it = s.iterator();
			while (it.hasNext())
				vertices[n++] = it.next();
		}

		double[] vertex;

		// build a map from vertexIndex to index in the new vertices array.
		int[] vmap = new int[grid.getNVertices()];
		Arrays.fill(vmap, -1);
		for (int i = 0; i < vertices.length; ++i)
			vmap[vertices[i]] = i;

		output.writeBytes(String.format("# vtk DataFile Version 2.0%n"));
		output.writeBytes(String.format("GeoTessLayerThickness%n"));
		output.writeBytes(String.format("BINARY%n"));

		output.writeBytes(String.format("DATASET UNSTRUCTURED_GRID%n"));

		output.writeBytes(String.format("POINTS %d double%n", vertices.length));

		// iterate over all the grid vertices and write out their position
		for (int i = 0; i < vertices.length; ++i)
		{
			vertex = grid.getVertex(vertices[i]);
			output.writeDouble(vertex[0]);
			output.writeDouble(vertex[1]);
			output.writeDouble(vertex[2]);
		}

		// write out node connectivity
		int nTriangles = grid.getNTriangles(tessid, level);
		output.writeBytes(String.format("CELLS %d %d%n", nTriangles,
				nTriangles * 4));

		for (int t = grid.getFirstTriangle(tessid, level); t <= grid
				.getLastTriangle(tessid, level); ++t)
		{
			int[] triangle = grid.getTriangles()[t];
			output.writeInt(3);
			output.writeInt(vmap[triangle[0]]);
			output.writeInt(vmap[triangle[1]]);
			output.writeInt(vmap[triangle[2]]);
		}

		output.writeBytes(String.format("CELL_TYPES %d%n", nTriangles));
		for (int t = 0; t < nTriangles; ++t)
			output.writeInt(5); // vtk_triangle

		return vertices;
	}

	//	/**
	//	 * This just writes the grid to the vtk file. Only includes the geometry and
	//	 * topology of the specified tessellation.
	//	 * 
	//	 * @param grid
	//	 * @param tessid
	//	 * @param level index of level relative to first level of the specified 
	//	 * tessellation.
	//	 * @param output
	//	 * @return the indices of the vertices used in the plot.
	//	 * @throws IOException
	//	 */
	//	public static int[] vtkGrid(GeoTessGrid grid, int tessid, int level,
	//			DataOutputStream output) throws IOException
	//	{
	//		// get the indexes of the vertices on desired level.
	//		HashSetInteger s = grid.getVertexIndicesTopLevel(tessid);
	//		int[] vertices = new int[s.size()];
	//		{
	//			int n = 0;
	//			Iterator it = s.iterator();
	//			while (it.hasNext())
	//				vertices[n++] = it.next();
	//		}
	//
	//		double[] vertex;
	//
	//		// build a map from vertexIndex to index in the new vertices array.
	//		int[] vmap = new int[grid.getNVertices()];
	//		Arrays.fill(vmap, -1);
	//		for (int i = 0; i < vertices.length; ++i)
	//			vmap[vertices[i]] = i;
	//
	//		output.writeBytes(String.format("# vtk DataFile Version 2.0%n"));
	//		output.writeBytes(String.format("GeoTessLayerThickness%n"));
	//		output.writeBytes(String.format("BINARY%n"));
	//
	//		output.writeBytes(String.format("DATASET UNSTRUCTURED_GRID%n"));
	//
	//		output.writeBytes(String.format("POINTS %d double%n", vertices.length));
	//
	//		// iterate over all the grid vertices and write out their position
	//		for (int i = 0; i < vertices.length; ++i)
	//		{
	//			vertex = grid.getVertex(vertices[i]);
	//			output.writeDouble(vertex[0]);
	//			output.writeDouble(vertex[1]);
	//			output.writeDouble(vertex[2]);
	//		}
	//
	//		// write out node connectivity
	//		int nTriangles = grid.getNTriangles(tessid, level);
	//		output.writeBytes(String.format("CELLS %d %d%n", nTriangles,
	//				nTriangles * 4));
	//
	//		for (int t = grid.getFirstTriangle(tessid, level); t <= grid
	//				.getLastTriangle(tessid, level); ++t)
	//		{
	//			int[] triangle = grid.getTriangles()[t];
	//			output.writeInt(3);
	//			output.writeInt(vmap[triangle[0]]);
	//			output.writeInt(vmap[triangle[1]]);
	//			output.writeInt(vmap[triangle[2]]);
	//		}
	//
	//		output.writeBytes(String.format("CELL_TYPES %d%n", nTriangles));
	//		for (int t = 0; t < nTriangles; ++t)
	//			output.writeInt(5); // vtk_triangle
	//
	//		return vertices;
	//	}

	/**
	 * Write the model to a binary VTK file which can be viewed using ParaView
	 * (http://www.paraview.org).
	 * 
	 * @param model
	 *            a reference to the model
	 * @param fileName
	 *            the name of the output file. Must have vtk extension.
	 * @param maxSpacing
	 *            maximum radial spacing in km of points along the radial
	 *            profile. Actual radial spacing will generally be somewhat less
	 *            than the requested value so that there will be an integral
	 *            number of equally spaced points along the profile. 50 km is
	 *            typically adequate.
	 * @param firstLayerIndex
	 *            the first (innermost) layer to include
	 * @param lastLayerIndex
	 *            the last (outermost) layer to include. If value specified is
	 *            greater than last layer of the model then value is replaced
	 *            with the index of the last layer in the model.
	 * @param horizontalType
	 *            either InterpolatorType.LINEAR or
	 *            InterpolatorType.NATURAL_NEIGHBOR
	 * @param radialType
	 *            either InterpolatorType.LINEAR or
	 *            InterpolatorType.CUBIC_SPLINE
	 * @param reciprocal
	 *            if false, plot attribute value; if true, plot 1./value.
	 * @param attributes
	 *            indexes of the attributes to include.
	 * @throws GeoTessException
	 * @throws IOException
	 */
	public static void vtkSolid(GeoTessModel model, String fileName,
			double maxSpacing, int firstLayerIndex, int lastLayerIndex,
			InterpolatorType horizontalType, InterpolatorType radialType,
			boolean reciprocal, int[] attributes) throws GeoTessException,
	IOException
	{
		if (!fileName.toLowerCase().trim().endsWith(".vtk"))
			throw new IOException("\nOutput file name must have .vtk extension");

		if (attributes == null)
		{
			attributes = new int[model.getMetaData().getNAttributes()];
			for (int i = 0; i < attributes.length; ++i)
				attributes[i] = i;
		}

		int v0, v1, v2, nLayers = model.getMetaData().getNLayers();

		if (lastLayerIndex >= nLayers)
			lastLayerIndex = nLayers - 1;

		File outputFile = new File(fileName);

		GeoTessGrid grid = model.getGrid();

		// points are the 3D positions of all the points in the model.
		ArrayList<double[]> points = new ArrayList<double[]>();
		// values are the values at the points. Points and values will
		// be the same size. values only includes attributes in the
		// attribute list. Reciprocal has been applied.
		ArrayList<float[]> values = new ArrayList<float[]>();

		// the indexes of the points that form the edges of the wedges.
		// The number of wedges will be wedges.size()/6
		ArrayListInt wedges = new ArrayListInt();

		for (int layerId = firstLayerIndex; layerId <= lastLayerIndex; ++layerId)
		{

			int tessid = model.getMetaData().getLayerTessIds()[layerId];
			int level = grid.getNLevels(tessid) - 1;
			int nPoints = points.size();

			// get the indexes of the vertices on desired level.
			HashSetInteger s = grid.getVertexIndicesTopLevel(tessid);
			int[] vertices = new int[s.size()];
			{
				int n = 0;
				Iterator it = s.iterator();
				while (it.hasNext())
					vertices[n++] = it.next();
			}

			// build a map from vertexIndex in real grid to index in the new
			// vertices array.
			int[] vmap = new int[grid.getNVertices()];
			Arrays.fill(vmap, -1);
			for (int i = 0; i < vertices.length; ++i)
				vmap[vertices[i]] = i;

			int n, nr = 2;
			for (int i = 0; i < model.getGrid().getNVertices(); ++i)
			{
				Profile profile = model.getProfile(i, layerId);
				if (profile == null)
					throw new GeoTessException("profile is null");
				n = 1 + (int) Math.ceil((profile.getRadiusTop() - profile
						.getRadiusBottom()) / maxSpacing);
				if (n > nr)
					nr = n;
			}

			for (int i = 0; i < vertices.length; ++i)
			{
				Profile p = model.getProfile(vertices[i], layerId);
				double r, r0 = p.getRadiusBottom();
				double dr = ((double) p.getRadiusTop() - r0) / (nr - 1);
				double[] vertex = model.getGrid().getVertex(vertices[i]);
				for (int j = 0; j < nr; ++j)
				{
					r = r0 + j * dr;
					points.add(new double[] { vertex[0] * r, vertex[1] * r,
							vertex[2] * r });

					float[] vals = new float[attributes.length];
					for (int k = 0; k < attributes.length; ++k)
						vals[k] = (float) (reciprocal ? 1. / p.getValue(
								radialType, attributes[k], r, true) : p
								.getValue(radialType, attributes[k], r, true));
					values.add(vals);
				}
			}

			// determine point connectivity (wedges).
			for (int t = grid.getFirstTriangle(tessid, level); t <= grid
					.getLastTriangle(tessid, level); ++t)
			{
				int[] triangle = grid.getTriangles()[t];
				v0 = vmap[triangle[0]];
				v1 = vmap[triangle[1]];
				v2 = vmap[triangle[2]];

				for (int j = 0; j < nr - 1; ++j)
				{
					wedges.add(nPoints + v0 * nr + j);
					wedges.add(nPoints + v1 * nr + j);
					wedges.add(nPoints + v2 * nr + j);
					wedges.add(nPoints + v0 * nr + j + 1);
					wedges.add(nPoints + v1 * nr + j + 1);
					wedges.add(nPoints + v2 * nr + j + 1);
				}
			}
		}

		DataOutputStream output = new DataOutputStream(
				new BufferedOutputStream(new FileOutputStream(outputFile)));

		output.writeBytes(String.format("# vtk DataFile Version 2.0%n"));
		output.writeBytes(String.format("GeoTess%n"));
		output.writeBytes(String.format("BINARY%n"));

		output.writeBytes(String.format("DATASET UNSTRUCTURED_GRID%n"));

		output.writeBytes(String.format("POINTS %d double%n", points.size()));

		// iterate over all the grid vertices and write out their position
		for (int i = 0; i < points.size(); ++i)
		{
			double[] vertex = points.get(i);
			output.writeDouble(vertex[0]);
			output.writeDouble(vertex[1]);
			output.writeDouble(vertex[2]);
		}

		int nWedges = wedges.size() / 6;
		output.writeBytes(String.format("CELLS %d %d%n", nWedges, nWedges * 7));
		int w = 0;
		for (int j = 0; j < nWedges; ++j)
		{
			output.writeInt(6);
			output.writeInt(wedges.get(w++));
			output.writeInt(wedges.get(w++));
			output.writeInt(wedges.get(w++));
			output.writeInt(wedges.get(w++));
			output.writeInt(wedges.get(w++));
			output.writeInt(wedges.get(w++));
		}

		output.writeBytes(String.format("CELL_TYPES %d%n", nWedges));
		for (int t = 0; t < nWedges; ++t)
			output.writeInt(13);

		output.writeBytes(String.format("POINT_DATA %d%n", values.size()));

		for (int a = 0; a < attributes.length; ++a)
		{
			String attributeName = vtkName(model, attributes[a], reciprocal);
			output.writeBytes(String.format("SCALARS %s float 1%n",
					attributeName));

			output.writeBytes(String.format("LOOKUP_TABLE default%n"));

			for (int i = 0; i < values.size(); ++i)
				output.writeFloat(values.get(i)[a]);
		}
		output.close();
	}

	/**
	 * Generate a vtk file of attribute values interpolated on a vertical slice
	 * through a model.
	 * 
	 * @param model
	 *            the GeoTessModel from which slice will be extracted
	 * @param fileName
	 *            the name of the file to receive the vtk output. Must have
	 *            'vtk' extension. Also must contain the substring '%d' twice,
	 *            the first one will be replaced with the tessellation index and
	 *            the second with the level index.
	 * @param greatCircle
	 *            the greatCircle that defines the slice to be extracted
	 * @param nx
	 *            number of points along great circle path
	 * @param maxRadialSpacing
	 *            radial spacing of points will be less than or equal to this
	 *            value (km).
	 * @param firstLayer
	 *            index of the first layer to include (deepest)
	 * @param lastLayer
	 *            index of the last layer to include (shallowest)
	 * @param horizontalType
	 *            either InterpolatorType.LINEAR or
	 *            InterpolatorType.NATURAL_NEIGHBOR
	 * @param radialType
	 *            either InterpolatorType.LINEAR or
	 *            InterpolatorType.CUBIC_SPLINE
	 * @param reciprocal
	 *            if false, return value; if true, return 1./value.
	 * @param attributes
	 *            indexes of the attributes to include.
	 * @throws GeoTessException
	 * @throws IOException
	 */
	static public void vtkSlice(GeoTessModel model, String fileName,
			GreatCircle greatCircle, int nx, double maxRadialSpacing,
			int firstLayer, int lastLayer, InterpolatorType horizontalType,
			InterpolatorType radialType, boolean reciprocal, int[] attributes)
					throws GeoTessException, IOException
	{
		File outputFile = new File(fileName);

		if (attributes == null)
		{
			attributes = new int[model.getMetaData().getNAttributes()];
			for (int i = 0; i < attributes.length; ++i)
				attributes[i] = i;
		}

		double[][][] slice = GeoTessModelUtils.getSlice(model, greatCircle, nx,
				maxRadialSpacing, firstLayer, lastLayer, horizontalType,
				radialType, "x,y", reciprocal, attributes);

		if (!fileName.toLowerCase().trim().endsWith(".vtk"))
			throw new IOException("\nOutput file name must have .vtk extension");

		DataOutputStream output = new DataOutputStream(
				new BufferedOutputStream(new FileOutputStream(
						outputFile)));

		output.writeBytes(String.format("# vtk DataFile Version 2.0%n"));
		output.writeBytes(String.format("GeoTess%n"));
		output.writeBytes(String.format("BINARY%n"));

		output.writeBytes(String.format("DATASET UNSTRUCTURED_GRID%n"));

		int ny = slice[0].length;
		int ncells = (nx - 1) * (ny - 1);
		// int nAttributes = slice[0][0].length; // includes spatial coordinates

		output.writeBytes(String.format("POINTS %d double%n", nx * ny));

		// iterate over all the grid vertices and write out their position
		for (int i = 0; i < slice.length; ++i)
			for (int j = 0; j < slice[i].length; ++j)
			{
				output.writeDouble(slice[i][j][0]);
				output.writeDouble(slice[i][j][1]);
				output.writeDouble(0.);
			}

		// write out node connectivity
		output.writeBytes(String.format("CELLS %d %d%n", ncells, ncells * 5));

		for (int i = 0; i < nx - 1; ++i)
			for (int j = 0; j < ny - 1; ++j)
			{
				output.writeInt(4);
				output.writeInt(i * ny + j);
				output.writeInt(i * ny + j + 1);
				output.writeInt((i + 1) * ny + j + 1);
				output.writeInt((i + 1) * ny + j);
			}

		output.writeBytes(String.format("CELL_TYPES %d%n", ncells));
		for (int t = 0; t < ncells; ++t)
			output.writeInt(9); // vtk_quad (counter clockwise)

		output.writeBytes(String.format("POINT_DATA %d%n", nx * ny));

		for (int a = 0; a < attributes.length; ++a)
		{
			String attributeName = vtkName(model, attributes[a], reciprocal);

			output.writeBytes(String.format("SCALARS %s float 1%n",
					attributeName));
			output.writeBytes(String.format("LOOKUP_TABLE default%n"));

			for (int i = 0; i < slice.length; ++i)
				for (int j = 0; j < slice[i].length; ++j)
					output.writeFloat((float) slice[i][j][a + 2]);
			// skipped the first two attributes, which are x and y,
			// not model attributes.
		}
		output.close();


		output = new DataOutputStream(
				new BufferedOutputStream(new FileOutputStream(
						new File(outputFile.getParentFile(), "slice_outline.vtk"))));

		output.writeBytes(String.format("# vtk DataFile Version 2.0%n"));
		output.writeBytes(String.format("GeoTess%n"));
		output.writeBytes(String.format("BINARY%n"));

		output.writeBytes(String.format("DATASET POLYDATA%n"));

		int npoints = 2*nx+2;

		output.writeBytes(String.format("POINTS %d double%n", npoints));

		// iterate over all the grid vertices and write out their position
		for (int i = 0; i < nx; ++i)
		{
			output.writeDouble(slice[i][0][0]);
			output.writeDouble(slice[i][0][1]);
			output.writeDouble(0.);
		}

		output.writeDouble(slice[nx-1][ny-1][0]);
		output.writeDouble(slice[nx-1][ny-1][1]);
		output.writeDouble(0.);

		for (int i = nx-1; i>=0; --i)
		{
			output.writeDouble(slice[i][ny-1][0]);
			output.writeDouble(slice[i][ny-1][1]);
			output.writeDouble(0.);
		}

		output.writeDouble(slice[0][0][0]);
		output.writeDouble(slice[0][0][1]);
		output.writeDouble(0.);

		// write out node connectivity
		output.writeBytes(String.format("LINES %d %d%n", 1, npoints+1));
		output.writeInt(npoints);
		for (int i=0; i<npoints; ++i) output.writeInt(i);
		output.close();
	}

	/**
	 * Write the grid to a vtk file for viewing with Paraview. Output file name
	 * should contain the substring '%d' which will be replaced with
	 * tessellation id.
	 * 
	 * @param grid
	 *            the GeoTessGrid object to plot.
	 * @param fileName
	 *            name of output vtk file.
	 * @throws IOException
	 * @throws GeoTessException
	 */
	public static void vtkGrid(GeoTessGrid grid, String fileName)
			throws IOException, GeoTessException
	{
		if (!fileName.toLowerCase().trim().endsWith(".vtk"))
			throw new IOException("\nOutput file name must have .vtk extension");

		for (int tessid = 0; tessid < grid.getNTessellations(); ++tessid)
		{
			int level = grid.getNLevels(tessid) - 1;

			// get the indexes of the vertices on desired level.
			HashSetInteger s = grid.getVertexIndicesTopLevel(tessid);
			int[] vertices = new int[s.size()];
			{
				int n = 0;
				Iterator it = s.iterator();
				while (it.hasNext())
					vertices[n++] = it.next();
			}

			// build a map from vertexIndex to index in the new vertices array.
			int[] vmap = new int[grid.getNVertices()];
			Arrays.fill(vmap, -1);
			for (int i = 0; i < vertices.length; ++i)
				vmap[vertices[i]] = i;

			String fname = fileName;
			if (fname.contains("%d") || fname.contains("%0d")
					|| fname.contains("%00d"))
				fname = String.format(fname, tessid);
			else if (grid.getNTessellations() > 1)
				throw new IOException(
						"nTessellations is > 1 and vtk fileName does not contain substring '%d'");

			DataOutputStream output = new DataOutputStream(
					new BufferedOutputStream(new FileOutputStream(new File(
							fname))));

			output.writeBytes(String.format("# vtk DataFile Version 2.0%n"));
			output.writeBytes(String.format("GeoTessGrid%n"));
			output.writeBytes(String.format("BINARY%n"));

			output.writeBytes(String.format("DATASET UNSTRUCTURED_GRID%n"));

			output.writeBytes(String.format("POINTS %d double%n",
					vertices.length));

			// iterate over all the grid vertices and write out their position
			double[] vertex;
			double radius = 1;
			for (int i = 0; i < vertices.length; ++i)
			{
				vertex = grid.getVertex(vertices[i]);
				// radius = model.getProfile(i, layerId).getRadiusTop();
				output.writeDouble(vertex[0] * radius);
				output.writeDouble(vertex[1] * radius);
				output.writeDouble(vertex[2] * radius);
			}

			// write out node connectivity
			int nTriangles = grid.getNTriangles(tessid, level);
			output.writeBytes(String.format("CELLS %d %d%n", nTriangles,
					nTriangles * 4));

			for (int t = grid.getFirstTriangle(tessid, level); t <= grid
					.getLastTriangle(tessid, level); ++t)
			{
				int[] triangle = grid.getTriangles()[t];
				output.writeInt(3);
				output.writeInt(vmap[triangle[0]]);
				output.writeInt(vmap[triangle[1]]);
				output.writeInt(vmap[triangle[2]]);
			}

			output.writeBytes(String.format("CELL_TYPES %d%n", nTriangles));
			for (int t = 0; t < nTriangles; ++t)
				output.writeInt(5); // vtk_triangle

			output.close();
		}
	}

	/**
	 * Retrieve a 3D block of attribute values on a regular lat-lon-radius grid.
	 * 
	 * @param model
	 *            the GeoTessModel to be interrogated.
	 * @param outputFile
	 *            file to receive vtk output. Must end with extension vtk.
	 * @param latitudes
	 *            array of latitude values in degrees.
	 * @param longitudes
	 *            array of longitude values in degrees.
	 * @param firstLayer
	 *            index of deepest layer
	 * @param lastLayer
	 *            index of shallowest layer
	 * @param radialDimension
	 *            specifies what values to put in the radialDimension: radius,
	 *            depth, or layerIndex
	 * @param maxRadialSpacing
	 *            radial spacing of points in the output will be no larger than
	 *            this value. The actual radial spacing of points will likely be
	 *            less so that the number of radii in each layer will be
	 *            constant.
	 * @param horizontalType
	 *            either InterpolatorType.LINEAR or
	 *            InterpolatorType.NATURAL_NEIGHBOR
	 * @param radialType
	 *            either InterpolatorType.LINEAR or
	 *            InterpolatorType.CUBIC_SPLINE
	 * @param reciprocal
	 *            if false, return value; if true, return 1./value.
	 * @param attributes
	 *            indexes of the attributes to include.
	 * @throws GeoTessException
	 * @throws IOException
	 */
	static public void vtk3DBlock(GeoTessModel model, String outputFile,
			double[] latitudes, double[] longitudes, int firstLayer,
			int lastLayer, String radialDimension, double maxRadialSpacing,
			InterpolatorType horizontalType, InterpolatorType radialType,
			boolean reciprocal, int[] attributes) throws GeoTessException,
	IOException
	{
		if (!outputFile.endsWith(".vtk"))
			throw new IOException("outputFile " + outputFile
					+ " must end with .vtk");

		if (attributes == null)
		{
			attributes = new int[model.getMetaData().getNAttributes()];
			for (int i = 0; i < attributes.length; ++i)
				attributes[i] = i;
		}

		double[][][][] values = getValues3D(model, latitudes, longitudes,
				firstLayer, lastLayer, radialDimension, maxRadialSpacing,
				horizontalType, radialType, reciprocal, attributes);

		int nlon = values.length;
		int nlat = values[0].length;
		int nradii = values[0][0].length;
		int npoints = nlat * nlon * nradii;

		// search for the maximum depth values
		double[][][] vi;
		double[][] vj;
		double[] vk;

		if (radialDimension.equals("depth"))
			for (int i = 0; i < values.length; ++i)
			{
				vi = values[i];
				for (int j = 0; j < vi.length; ++j)
				{
					vj = vi[j];
					for (int k = 0; k < vj.length; ++k)
					{
						vk = vj[k];
						vk[0] = -vk[0];
					}
				}
			}

		DataOutputStream output = new DataOutputStream(
				new BufferedOutputStream(new FileOutputStream(new File(
						outputFile))));

		output.writeBytes(String.format("# vtk DataFile Version 2.0%n"));
		output.writeBytes(String.format("GeoTessBlock3D%n"));
		output.writeBytes(String.format("BINARY%n"));

		output.writeBytes(String.format("DATASET UNSTRUCTURED_GRID%n"));

		output.writeBytes(String.format("POINTS %d double%n", npoints));

		for (int k = 0; k < nradii; ++k)
			for (int j = 0; j < nlat; ++j)
				for (int i = 0; i < nlon; ++i)
				{
					output.writeDouble(longitudes[i]);
					output.writeDouble(latitudes[j]);
					output.writeDouble(values[i][j][k][0]);
				}

		// write out node connectivity
		int ncells = (nlat - 1) * (nlon - 1) * (nradii - 1);
		output.writeBytes(String.format("CELLS %d %d%n", ncells, ncells * 9));

		int pointsPerPlane = nlat * nlon;
		int pointsPerRow = nlon;

		// iterate over all the grid vertices and write out their indexes
		for (int k = 0; k < nradii - 1; ++k)
			for (int j = 0; j < nlat - 1; ++j)
				for (int i = 0; i < nlon - 1; ++i)
				{
					output.writeInt(8);
					output.writeInt(k * pointsPerPlane + j * pointsPerRow + i);
					output.writeInt(k * pointsPerPlane + j * pointsPerRow + i
							+ 1);
					output.writeInt(k * pointsPerPlane + (j + 1) * pointsPerRow
							+ i + 1);
					output.writeInt(k * pointsPerPlane + (j + 1) * pointsPerRow
							+ i);
					output.writeInt((k + 1) * pointsPerPlane + j * pointsPerRow
							+ i);
					output.writeInt((k + 1) * pointsPerPlane + j * pointsPerRow
							+ i + 1);
					output.writeInt((k + 1) * pointsPerPlane + (j + 1)
							* pointsPerRow + i + 1);
					output.writeInt((k + 1) * pointsPerPlane + (j + 1)
							* pointsPerRow + i);
				}

		output.writeBytes(String.format("CELL_TYPES %d%n", ncells));
		for (int t = 0; t < ncells; ++t)
			output.writeInt(12); // vtk_hexahedron

		output.writeBytes(String.format("POINT_DATA %d%n", npoints));

		for (int a = 0; a < attributes.length; ++a)
		{
			String attributeName = vtkName(model, attributes[a], reciprocal);

			output.writeBytes(String.format("SCALARS %s float 1%n",
					attributeName));
			output.writeBytes(String.format("LOOKUP_TABLE default%n"));

			// iterate over all the grid vertices and write out their position
			for (int k = 0; k < nradii; ++k)
				for (int j = 0; j < nlat; ++j)
					for (int i = 0; i < nlon; ++i)
						output.writeFloat((float) values[i][j][k][a + 1]);
		}
		output.close();
	}

	/**
	 * Generate a contour map of some data values on a Robinson projection of
	 * the Earth.
	 * <p>
	 * Also generates another file that contains the outlines of the continents
	 * plotted on the same map projection. The file is located in the same
	 * directory as the outputFile, with the name
	 * 'map_coastlines_centerLon_%d.vtk' where %d is replaced with the longitude
	 * of the center of the map rounded to the nearest degree.
	 * 
	 * @param model
	 *            the model containing the grid and data to be plotted.
	 * @param outputFile
	 *            the name of the file to which to write the output. Must end
	 *            with extension 'vtk'.
	 * @param centerLonDegrees
	 *            the longitude of the center of the map in degrees.
	 * @param depth
	 *            the depth in the Earth where the data is to be interpolated.
	 * @param layer
	 *            the index of the layer in which depth resides.
	 * @param radiusOutOfRangeAllowed
	 *            if true and depth is above the top of layer or below bottom of
	 *            layer then the values at the top or bottom of layer are
	 *            plotted. If false and depth is above the top of layer or below
	 *            bottom of layer then NaN is plotted.
	 * @param radialInterpType
	 *            InterpolatorType.LINEAR or InterpolatorType.CUBIC_SPLINE
	 * @param reciprocal
	 *            if true, then the reciprocal values are plotted, otherwise
	 *            unmodified model values are plotted.
	 * @param attributes
	 *            array of attribute indeces to plot (if null, all are plotted).
	 * @throws IOException
	 */
	static public void vtkRobinson(GeoTessModel model, File outputFile,
			double centerLonDegrees, double depth, int layer,
			boolean radiusOutOfRangeAllowed, InterpolatorType radialInterpType,
			boolean reciprocal, int[] attributes) throws IOException
	{
		int tessId = model.getMetaData().getTessellation(layer);
		int level = model.getGrid().getNLevels(tessId)-1;

		vtkRobinson(model, outputFile, centerLonDegrees, depth, layer, level,
				radiusOutOfRangeAllowed, radialInterpType,
				reciprocal, attributes);
	}

	/**
	 * Generate a contour map of some data values on a Robinson projection of
	 * the Earth.
	 * <p>
	 * Also generates another file that contains the outlines of the continents
	 * plotted on the same map projection. The file is located in the same
	 * directory as the outputFile, with the name
	 * 'map_coastlines_centerLon_%d.vtk' where %d is replaced with the longitude
	 * of the center of the map rounded to the nearest degree.
	 * 
	 * @param model
	 *            the model containing the grid and data to be plotted.
	 * @param outputFile
	 *            the name of the file to which to write the output. Must end
	 *            with extension 'vtk'.
	 * @param centerLonDegrees
	 *            the longitude of the center of the map in degrees.
	 * @param depth
	 *            the depth in the Earth where the data is to be interpolated.
	 * @param layer
	 *            the index of the layer in which depth resides.
	 * @param level
	 *            the index of the level relative to the levels of tessellation
	 *            that supports the specified layer. If out-of-range, defaults 
	 *            to last layer in the tessellation that supports the specified layer.           
	 * @param radiusOutOfRangeAllowed
	 *            if true and depth is above the top of layer or below bottom of
	 *            layer then the values at the top or bottom of layer are
	 *            plotted. If false and depth is above the top of layer or below
	 *            bottom of layer then NaN is plotted.
	 * @param radialInterpType
	 *            InterpolatorType.LINEAR or InterpolatorType.CUBIC_SPLINE
	 * @param reciprocal
	 *            if true, then the reciprocal values are plotted, otherwise
	 *            unmodified model values are plotted.
	 * @param attributes
	 *            array of attribute indeces to plot (if null, all are plotted).
	 * @throws IOException
	 */
	static public void vtkRobinson(GeoTessModel model, File outputFile,
			double centerLonDegrees, double depth, int layer, int level,
			boolean radiusOutOfRangeAllowed, InterpolatorType radialInterpType,
			boolean reciprocal, int[] attributes) throws IOException
	{
		if (!outputFile.getName().endsWith(".vtk"))
			throw new IOException("outputFile " + outputFile.getCanonicalPath()
			+ " must end with .vtk");

		try
		{
			if (attributes == null)
			{
				attributes = new int[model.getMetaData().getNAttributes()];
				for (int i = 0; i < attributes.length; ++i)
					attributes[i] = i;
			}

			GeoTessGrid grid = model.getGrid();
			int tessId = model.getMetaData().getTessellation(layer);
			if (level < 0 || level > grid.getNLevels(tessId)-1)
				level = grid.getNLevels(tessId)-1;

			RobinsonProjection map = new RobinsonProjection(centerLonDegrees);

			ArrayList<Point> vertices = new ArrayList<Point>(
					grid.getNVertices());
			ArrayList<int[]> iCells = new ArrayList<int[]>(grid.getNTriangles(
					tessId, level));

			DataOutputStream output = new DataOutputStream(
					new BufferedOutputStream(new FileOutputStream(outputFile)));

			vtkRobinsonGrid(map, grid, tessId, level, output, vertices, iCells);

			output.writeBytes(String.format("POINT_DATA %d%n", vertices.size()));

			GeoTessPosition pos = model.getGeoTessPosition(
					InterpolatorType.LINEAR, radialInterpType);
			pos.setRadiusOutOfRangeAllowed(radiusOutOfRangeAllowed);

			float[][] values = new float[attributes.length + 1][vertices.size()];

			// iterate over all the grid vertices and extract the data values.
			for (int i = 0; i < vertices.size(); ++i)
			{
				double[] vertex = vertices.get(i).v;
				double radius = model.getEarthShape().getEarthRadius(vertex)-depth;
				int vertexIndex = vertices.get(i).vertexIndex;

				if (vertices.get(i).vertexIndex < 0)
				{
					pos.set(layer, vertex, radius);
					for (int a = 0; a < attributes.length; ++a)
						values[a][i] = (float) (reciprocal ? 
								1. / pos.getValue(attributes[a]) 
								: pos.getValue(attributes[a]));

					// elevation of current position, constrained to specified layer.
					values[attributes.length][i] = (float) -pos.getDepthConstrained();
				}
				else
				{
					Profile p = model.getProfile(vertexIndex, layer);
					double value;
					for (int a=0; a < attributes.length; ++a)
					{
						value = p.getValue(radialInterpType, attributes[a], radius, radiusOutOfRangeAllowed);
						values[a][i] = (float) (reciprocal ?  1. / value : value);
					}
					if (radiusOutOfRangeAllowed)
						value = radius;
					else if (radius <= p.getRadiusBottom())
						value  = p.getRadiusBottom();
					else if (radius >= p.getRadiusTop())
						p.getRadiusTop();
					else 
						value = radius;

					values[attributes.length][i] = (float) (radius - model.getEarthShape().getEarthRadius(vertex));
				}
			}

			for (int a = 0; a < attributes.length; ++a)
			{
				String attributeName = "";
				if (model.getNLayers() > 1)
					attributeName = model.getMetaData().getLayerName(layer)
					+ "_";
				attributeName += model.getMetaData().getAttributeName(attributes[a]);
				if (model.getMetaData().getAttributeUnit(attributes[a]).trim().length() > 0)
					attributeName += "_"
							+ model.getMetaData().getAttributeUnit(attributes[a]);

				attributeName = attributeName.trim().replaceAll(" ", "_");

				output.writeBytes(String.format("SCALARS %s float 1%n",
						attributeName));
				output.writeBytes(String.format("LOOKUP_TABLE default%n"));

				// iterate over all the grid vertices and write out their
				// position
				for (int i = 0; i < vertices.size(); ++i)
					output.writeFloat(values[a][i]);
			}
			output.close();

			// write the coastlines and map edge to files in the output
			// directory
			vtkRobinsonCoastlines(outputFile.getParentFile(), map);
			vtkRobinsonMapEdge(outputFile.getParentFile());
		}
		catch (Exception e)
		{
			throw new IOException(e);
		}
	}

	/**
	 * Generate a contour map of some data values on a Robinson projection of
	 * the Earth.
	 * <p>
	 * Also generates another file that contains the outlines of the continents
	 * plotted on the same map projection. The file is located in the same
	 * directory as the outputFile, with the name
	 * 'map_coastlines_centerLon_%d.vtk' where %d is replaced with the longitude
	 * of the center of the map rounded to the nearest degree.
	 * 
	 * @param model
	 *            the model containing the grid and data to be plotted.
	 * @param outputFile
	 *            the name of the file to which to write the output. Must end
	 *            with extension 'vtk'.
	 * @param centerLonDegrees
	 *            the longitude of the center of the map in degrees.
	 * @throws IOException
	 */
	static public void vtkRobinsonLayerDepths(GeoTessModel model, File outputFile,
			double centerLonDegrees) throws IOException
	{
		if (!outputFile.getName().endsWith(".vtk"))
			throw new IOException("outputFile " + outputFile.getCanonicalPath()
			+ " must end with .vtk");

		try
		{
			GeoTessGrid grid = model.getGrid();
			int tessId = grid.getNTessellations()-1;
			int level = grid.getNLevels(tessId) - 1;

			RobinsonProjection map = new RobinsonProjection(centerLonDegrees);

			ArrayList<Point> vertices = new ArrayList<Point>(grid.getNVertices());
			ArrayList<int[]> iCells = new ArrayList<int[]>(grid.getNTriangles(
					tessId, level));

			DataOutputStream output = new DataOutputStream(
					new BufferedOutputStream(new FileOutputStream(outputFile)));

			vtkRobinsonGrid(map, grid, tessId, level, output, vertices, iCells);

			output.writeBytes(String.format("POINT_DATA %d%n", vertices.size()));

			GeoTessPosition pos = model.getGeoTessPosition(
					InterpolatorType.LINEAR, InterpolatorType.LINEAR);

			double[][] values = new double[vertices.size()][];

			double[] earthRadius = new double[vertices.size()];

			// iterate over all the grid vertices and extract the data values.
			for (int i = 0; i < vertices.size(); ++i)
			{
				earthRadius[i] = model.getEarthShape().getEarthRadius(vertices.get(i).v);
				values[i] = pos.set(vertices.get(i).v, 1e4).getLayerRadii();
			}

			for (int a = 0; a <= model.getNLayers(); ++a)
			{

				String name = a == 0 ? "bottom" 
						: "Depth to top of "+model.getMetaData().getLayerName(a-1);

				name = name.trim().replaceAll(" ", "_");

				output.writeBytes(String.format("SCALARS %s float 1%n", name));
				output.writeBytes(String.format("LOOKUP_TABLE default%n"));

				// iterate over all the grid vertices and write out their
				// position
				for (int i = 0; i < vertices.size(); ++i)
				{
					output.writeFloat((float)(earthRadius[i]-values[i][a]));
				}
			}
			output.close();

			// write the coastlines and map edge to files in the output
			// directory
			vtkRobinsonCoastlines(outputFile.getParentFile(), map);
			vtkRobinsonMapEdge(outputFile.getParentFile());
		}
		catch (Exception e)
		{
			throw new IOException(e);
		}
	}

	/**
	 * Generate a contour map of layer thickness on a Robinson projection of
	 * the Earth.
	 * <p>
	 * Also generates another file that contains the outlines of the continents
	 * plotted on the same map projection. The file is located in the same
	 * directory as the outputFile, with the name
	 * 'map_coastlines_centerLon_%d.vtk' where %d is replaced with the longitude
	 * of the center of the map rounded to the nearest degree.
	 * 
	 * @param model
	 *            the model containing the grid and data to be plotted.
	 * @param outputFile
	 *            the name of the file to which to write the output. Must end
	 *            with extension 'vtk'.
	 * @param centerLonDegrees
	 *            the longitude of the center of the map in degrees.
	 * @param firstLayer
	 *            index of first layer
	 * @param lastLayer
	 *            index of last layer. . Thickness will include first through
	 *            last layer, inclusive
	 * @param horizontalType
	 *            InterpolatorType.LINEAR or InterpolatorType.NATURAL_NEIGHBOR.
	 * @throws IOException
	 */
	static public void vtkRobinsonLayerThickness(GeoTessModel model, File outputFile,
			double centerLonDegrees, int firstLayer, int lastLayer, 
			InterpolatorType horizontalType) throws IOException
	{
		if (!outputFile.getName().endsWith(".vtk"))
			throw new IOException("outputFile " + outputFile.getCanonicalPath()
			+ " must end with .vtk");

		try
		{
			GeoTessGrid grid = model.getGrid();
			int tessId = model.getMetaData().getTessellation(lastLayer);
			int level = grid.getNLevels(tessId) - 1;

			RobinsonProjection map = new RobinsonProjection(centerLonDegrees);

			ArrayList<Point> vertices = new ArrayList<Point>(
					grid.getNVertices());
			ArrayList<int[]> iCells = new ArrayList<int[]>(grid.getNTriangles(
					tessId, level));

			DataOutputStream output = new DataOutputStream(
					new BufferedOutputStream(new FileOutputStream(outputFile)));

			vtkRobinsonGrid(map, grid, tessId, level, output, vertices, iCells);

			output.writeBytes(String.format("POINT_DATA %d%n", vertices.size()));

			GeoTessPosition pos = model.getGeoTessPosition( horizontalType, InterpolatorType.LINEAR);

			output.writeBytes(String.format("SCALARS Thickness_of_layers_%d_through_%d float 1%n", 
					firstLayer, lastLayer));
			output.writeBytes(String.format("LOOKUP_TABLE default%n"));

			// iterate over all the grid vertices and extract the data values.
			for (int i = 0; i < vertices.size(); ++i)
			{
				pos.set(lastLayer, vertices.get(i).v, 1e4);
				output.writeFloat( (float) (pos.getRadiusTop(lastLayer)-pos.getRadiusBottom(firstLayer)));

			}

			output.close();

			// write the coastlines and map edge to files in the output
			// directory
			vtkRobinsonCoastlines(outputFile.getParentFile(), map);
			vtkRobinsonMapEdge(outputFile.getParentFile());
		}
		catch (Exception e)
		{
			throw new IOException(e);
		}
	}

	/**
	 * Generate a map of the size of the triangles in a grid. This size of the
	 * triangles is the edgelength of an equilateral triangle with the same
	 * area, in degrees.
	 * <p>
	 * Also generates another file that contains the outlines of the continents
	 * plotted on the same map projection. The file is located in the same
	 * directory as the outputFile, with the name 'continents_centerLon_%d.vtk'
	 * where %d is replaced with the longitude of the center of the map rounded
	 * to the nearest degree.
	 * 
	 * @param grid
	 *            the grid to be plotted.
	 * @param outputFile
	 *            the name of the file to which to write the output. Must end
	 *            with extension 'vtk'.
	 * @param centerLonDegrees
	 *            the longitude of the center of the map in degrees.
	 * @param tessId
	 *            the index of the tessellation to be interrogated.
	 * @throws IOException
	 * @throws GeoTessException
	 */
	static public void vtkRobinsonTriangleSize(GeoTessGrid grid,
			File outputFile, double centerLonDegrees, int tessId)
					throws Exception
	{
		if (!outputFile.getName().endsWith(".vtk"))
			throw new IOException("outputFile " + outputFile
					+ " must end with .vtk");

		int level = grid.getNLevels(tessId) - 1;

		GeoTessMetaData md = new GeoTessMetaData();
		md.setDescription("");
		md.setAttributes("", "");
		md.setDataType(DataType.BYTE);
		md.setModelGenerationDate("today");
		md.setModelSoftwareVersion("v1");
		String[] layerNames = new String[grid.getNTessellations()];
		int[] layerTessIds = new int[layerNames.length];
		for (int i = 0; i < grid.getNTessellations(); ++i)
		{
			layerNames[i] = "layer" + i;
			layerTessIds[i] = i;
		}
		md.setLayerNames(layerNames);
		md.setLayerTessIds(layerTessIds);
		GeoTessModel model = new GeoTessModel(grid, md);

		int layer = tessId;

		RobinsonProjection map = new RobinsonProjection(centerLonDegrees);

		ArrayList<Point> vertices = new ArrayList<Point>(grid.getNVertices());
		ArrayList<int[]> iCells = new ArrayList<int[]>(grid.getNTriangles(
				tessId, level));

		DataOutputStream output = new DataOutputStream(
				new BufferedOutputStream(new FileOutputStream(outputFile)));

		try
		{
			vtkRobinsonGrid(map, grid, tessId, level, output, vertices, iCells);
		}
		catch (GreatCircleException e)
		{
			throw new GeoTessException(e);
		}

		output.writeBytes(String.format("CELL_DATA %d%n", iCells.size()));

		output.writeBytes(String.format("SCALARS Edge_length_deg float 1%n"));
		output.writeBytes(String.format("LOOKUP_TABLE default%n"));

		GeoTessPosition pos = model.getGeoTessPosition();

		double[] center = new double[3];
		double[][] tv;
		for (int k = 0; k < iCells.size(); ++k)
		{
			int[] cell = iCells.get(k);
			center[0] = center[1] = center[2] = 0.;
			for (int i = 0; i < cell.length; ++i)
			{
				center[0] += vertices.get(cell[i]).v[0];
				center[1] += vertices.get(cell[i]).v[1];
				center[2] += vertices.get(cell[i]).v[2];
			}
			GeoTessUtils.normalize(center);

			pos.set(layer, center, 6371.);
			tv = grid.getTriangleVertices(pos.getTriangle());

			double area = GeoTessUtils.getTriangleArea(tv[0], tv[1], tv[2]);
			// convert square radians to square degrees
			area = Math.toDegrees(Math.toDegrees(area));

			// convert area to edgelength of equilateral triangle with that
			// area.
			output.writeFloat((float) Math.sqrt(4 * area / Math.sqrt(3)));
		}

		output.close();

		// write the coastlines and map edge to files in the output directory
		vtkRobinsonCoastlines(outputFile.getParentFile(), map);
		vtkRobinsonMapEdge(outputFile.getParentFile());

	}

	static public void vtkRobinsonTriangleData(GeoTessGrid grid,
			File outputFile, double centerLonDegrees, int tessId,
			double[] triangleData) throws Exception
	{
		if (!outputFile.getName().endsWith(".vtk"))
			throw new IOException("outputFile " + outputFile
					+ " must end with .vtk");

		int level = grid.getNLevels(tessId) - 1;

		GeoTessMetaData md = new GeoTessMetaData();
		md.setDescription("");
		md.setAttributes("", "");
		md.setDataType(DataType.BYTE);
		md.setModelGenerationDate("today");
		md.setModelSoftwareVersion("v1");
		String[] layerNames = new String[grid.getNTessellations()];
		int[] layerTessIds = new int[layerNames.length];
		for (int i = 0; i < grid.getNTessellations(); ++i)
		{
			layerNames[i] = "layer" + i;
			layerTessIds[i] = i;
		}
		md.setLayerNames(layerNames);
		md.setLayerTessIds(layerTessIds);
		GeoTessModel model = new GeoTessModel(grid, md);

		int layer = tessId;

		RobinsonProjection map = new RobinsonProjection(centerLonDegrees);

		ArrayList<Point> vertices = new ArrayList<Point>(grid.getNVertices());
		ArrayList<int[]> iCells = new ArrayList<int[]>(grid.getNTriangles(
				tessId, level));

		DataOutputStream output = new DataOutputStream(
				new BufferedOutputStream(new FileOutputStream(outputFile)));

		try
		{
			vtkRobinsonGrid(map, grid, tessId, level, output, vertices, iCells);
		}
		catch (GreatCircleException e)
		{
			throw new GeoTessException(e);
		}

		output.writeBytes(String.format("CELL_DATA %d%n", iCells.size()));

		output.writeBytes(String.format("SCALARS Edge_length_deg float 1%n"));
		output.writeBytes(String.format("LOOKUP_TABLE default%n"));

		GeoTessPosition pos = model.getGeoTessPosition();

		double[] center = new double[3];
		int firstTriangle = grid.getFirstTriangle(tessId, level);
		for (int k = 0; k < iCells.size(); ++k)
		{
			int[] cell = iCells.get(k);
			center[0] = center[1] = center[2] = 0.;
			for (int i = 0; i < cell.length; ++i)
			{
				center[0] += vertices.get(cell[i]).v[0];
				center[1] += vertices.get(cell[i]).v[1];
				center[2] += vertices.get(cell[i]).v[2];
			}
			GeoTessUtils.normalize(center);

			pos.set(layer, center, 6371.);
			output.writeFloat((float) triangleData[pos.getTriangle()
			                                       - firstTriangle]);
		}

		output.close();

		// write the coastlines and map edge to files in the output directory
		vtkRobinsonCoastlines(outputFile.getParentFile(), map);
		vtkRobinsonMapEdge(outputFile.getParentFile());

	}

	static public void vtkRobinsonCells(GeoTessModel model, File outputFile,
			double centerLonDegrees, int layer, int[] attributes)
					throws Exception
	{
		if (!outputFile.getName().endsWith(".vtk"))
			throw new IOException("outputFile " + outputFile
					+ " must end with .vtk");

		if (attributes == null)
		{
			attributes = new int[model.getMetaData().getNAttributes()];
			for (int i = 0; i < attributes.length; ++i)
				attributes[i] = i;
		}

		GeoTessGrid grid = model.getGrid();

		int tessId = model.getMetaData().getTessellation(layer);
		int level = grid.getNLevels(tessId) - 1;

		RobinsonProjection map = new RobinsonProjection(centerLonDegrees);

		ArrayList<Point> vertices = new ArrayList<Point>(grid.getNVertices());
		ArrayList<int[]> iCells = new ArrayList<int[]>(grid.getNTriangles(
				tessId, level));

		DataOutputStream output = new DataOutputStream(
				new BufferedOutputStream(new FileOutputStream(outputFile)));

		try
		{
			vtkRobinsonGrid(map, grid, tessId, level, output, vertices, iCells);
		}
		catch (GreatCircleException e)
		{
			throw new GeoTessException(e);
		}

		GeoTessPosition pos = model.getGeoTessPosition();

		output.writeBytes(String.format("CELL_DATA %d%n", iCells.size()));

		float[][] values = new float[model.getMetaData().getNAttributes()][iCells
		                                                                   .size()];
		for (int t = 0; t < iCells.size(); ++t)
		{
			int[] cell = iCells.get(t);
			for (int a = 0; a < attributes.length; ++a)
			{
				for (int v = 0; v < cell.length; ++v)
				{
					pos.setTop(layer, vertices.get(cell[v]).v);
					values[a][t] += pos.getValue(a);
				}
				values[a][t] /= cell.length;
			}

		}

		for (int a = 0; a < attributes.length; ++a)
		{
			float[] vals = values[a];

			String attributeName = vtkName(model, attributes[a], false);

			output.writeBytes(String.format("SCALARS %s float 1%n",
					attributeName));
			output.writeBytes(String.format("LOOKUP_TABLE default%n"));

			for (int i = 0; i < vals.length; ++i)
				output.writeFloat(vals[i]);
		}
		output.close();

		// write the coastlines and map edge to files in the output directory
		vtkRobinsonCoastlines(outputFile.getParentFile(), map);
		vtkRobinsonMapEdge(outputFile.getParentFile());

	}

	/**
	 * Read point data from a file and generate a vtk file.
	 * 
	 * @param inputFile
	 *            ascii file with lat lon in degrees on each record.
	 * @param outputFile
	 * @throws GeoTessException
	 * @throws IOException
	 */
	static public void vtkPoints(File inputFile, File outputFile)
			throws GeoTessException, IOException
	{
		try
		{
			Scanner input = new Scanner(inputFile);
			ArrayList<double[]> points = new ArrayList<double[]>();
			while (input.hasNext())
			{
				Scanner line = new Scanner(input.nextLine());
				try
				{
					points.add(Vector3D.getVectorDegrees(line.nextDouble(),
							line.nextDouble()));
					line.close();
				}
				catch (Exception ex)
				{
				}
			}
			input.close();
			vtkPoints(points, outputFile.getCanonicalPath());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	static public void vtkPoints(ArrayList<double[]> points, File outputFile)
			throws GeoTessException, IOException
	{
		vtkPoints(points, outputFile.getCanonicalPath());
	}

	static public void vtkPoints(ArrayList<double[]> points, String outputFile)
			throws GeoTessException, IOException
	{
		BufferedWriter output = new BufferedWriter(new FileWriter(outputFile));
		output.write(String.format("# vtk DataFile Version 2.0%n"));
		output.write(String.format("GeoTessPoints%n"));
		output.write(String.format("ASCII%n"));

		output.write(String.format("DATASET POLYDATA%n"));

		output.write(String.format("POINTS %1d double%n", points.size()));

		// iterate over all the grid nodes and write out their position
		for (double[] point : points)
		{
			output.write(String.format("%1.8f %1.8f %1.8f%n", point[0],
					point[1], point[2]));
		}

		output.write(String.format("VERTICES %d %d%n%d%n", points.size(),
				points.size() + 1, points.size()));
		String line = "";
		for (int i = 0; i < points.size(); ++i)
		{
			line += String.format("%d ", i);
			if (line.length() > 120)
			{
				output.write(line);
				output.newLine();
				line = "";
			}
		}
		if (line.length() > 0)
		{
			output.write(line);
			output.newLine();
			line = "";
		}

		output.close();
	}

	static public void vtkPoints(ArrayList<double[]> points, double[] data,
			String outputFile) throws GeoTessException, IOException
	{
		vtkPoints(points, data, new File(outputFile));
	}

	static public void vtkPoints(ArrayList<double[]> points, double[] data,
			File outputFile) throws GeoTessException, IOException
	{
		DataOutputStream output = new DataOutputStream(
				new BufferedOutputStream(new FileOutputStream(outputFile)));

		output.writeBytes(String.format("# vtk DataFile Version 2.0%n"));
		output.writeBytes(String.format("GeoTessPoints%n"));
		output.writeBytes(String.format("BINARY%n"));

		output.writeBytes(String.format("DATASET POLYDATA%n"));

		output.writeBytes(String.format("POINTS %1d double%n", points.size()));

		// iterate over all the grid nodes and write out their position
		for (double[] point : points)
		{
			output.writeDouble(point[0]);
			output.writeDouble(point[1]);
			output.writeDouble(point[2]);
		}

		output.writeBytes(String.format("VERTICES %d %d%n", points.size(),
				points.size() + 1));
		output.writeInt(points.size());
		for (int i = 0; i < points.size(); ++i)
			output.writeInt(i);

		output.writeBytes(String.format("POINT_DATA %d%n", data.length));

		output.writeBytes(String.format("SCALARS %s double 1%n", "testdata"));
		output.writeBytes(String.format("LOOKUP_TABLE default%n"));

		for (double d : data)
			output.writeDouble(d);
		output.close();
	}

	/**
	 * Read point data from a file and generate a vtk file.
	 * 
	 * @param inputFile
	 *            ascii file with lat lon in degrees on each record.
	 * @param outputFile
	 * @param centerLonDegrees
	 *            longitude of center of map, in degrees
	 * @throws IOException
	 */
	static public void vtkRobinsonPoints(File inputFile, File outputFile,
			double centerLonDegrees) throws IOException
	{
		try
		{
			Scanner input = new Scanner(inputFile);
			ArrayList<double[]> points = new ArrayList<double[]>();
			while (input.hasNext())
			{
				Scanner line = new Scanner(input.nextLine());
				try
				{
					points.add(Vector3D.getVectorDegrees(line.nextDouble(),
							line.nextDouble()));
					line.close();
				}
				catch (Exception ex)
				{
				}
			}
			input.close();
			vtkRobinsonPoints(centerLonDegrees, points, outputFile);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	static public void vtkRobinsonPoints(double centerLon,
			List<double[]> points, File outputFile) throws Exception
	{
		vtkRobinsonPoints(centerLon, points, null, outputFile);
	}

	static public void vtkRobinsonPoints(double centerLon,
			List<double[]> points, double[] data, File outputFile)
					throws Exception
	{
		RobinsonProjection map = new RobinsonProjection(centerLon);
		double[] xy = new double[2];

		DataOutputStream output = new DataOutputStream(
				new BufferedOutputStream(new FileOutputStream(outputFile)));

		output.writeBytes(String.format("# vtk DataFile Version 2.0%n"));
		output.writeBytes(String.format("GeoTessPoints%n"));
		output.writeBytes(String.format("BINARY%n"));

		output.writeBytes(String.format("DATASET POLYDATA%n"));

		output.writeBytes(String.format("POINTS %1d double%n", points.size()));

		// iterate over all the grid nodes and write out their position
		for (double[] point : points)
		{
			map.project(point, xy);
			output.writeDouble(xy[0]);
			output.writeDouble(xy[1]);
			output.writeDouble(0.);
		}

		output.writeBytes(String.format("VERTICES 1 %d%n", points.size() + 1));
		output.writeInt(points.size());
		for (int i = 0; i < points.size(); ++i)
			output.writeInt(i);

		if (data != null)
		{
			output.writeBytes(String.format("POINT_DATA %d%n", data.length));

			output.writeBytes(String
					.format("SCALARS %s double 1%n", "testdata"));
			output.writeBytes(String.format("LOOKUP_TABLE default%n"));

			for (double d : data)
				output.writeDouble(d);
		}
		output.close();

		// write the coastlines and map edge to files in the output directory
		vtkRobinsonCoastlines(outputFile.getParentFile(), map);
		vtkRobinsonMapEdge(outputFile.getParentFile());
	}

	// static public void vtkRobinsonPoints(double centerLon,
	// ArrayList<double[]> points, double[] data, File outputFile)
	// throws Exception
	// {
	// RobinsonProjection map = new RobinsonProjection(centerLon);
	// double[] xy = new double[2];
	//
	// BufferedWriter output = new BufferedWriter(new FileWriter(outputFile));
	//
	// output.write(String.format("# vtk DataFile Version 2.0%n"));
	// output.write(String.format("GeoTessPoints%n"));
	// output.write(String.format("ASCII%n"));
	//
	// output.write(String.format("DATASET POLYDATA%n"));
	//
	// output.write(String.format("POINTS %1d double%n", points.size()));
	//
	// // iterate over all the grid nodes and write out their position
	// for (double[] point : points)
	// {
	// map.project(point, xy);
	// output.write(String.format("%1.12f %1.12f 0%n", xy[0], xy[1]));
	// }
	//
	// output.write(String.format(String.format("VERTICES 1 %d%n", points.size()
	// + 1)));
	// output.write(String.format("%d%n", points.size()));
	// for (int i = 0; i < points.size(); ++i)
	// output.write(String.format("%d%n",i));
	//
	// if (data != null)
	// {
	// output.write(String.format(String.format("POINT_DATA %d%n",
	// data.length)));
	//
	// output.write(String.format(String.format("SCALARS %s double 1%n",
	// "testdata")));
	// output.write(String.format(String.format("LOOKUP_TABLE default%n")));
	//
	// for (double d : data)
	// output.write(String.format("%f%n",d));
	// }
	// output.close();
	//
	// // write the coastlines and map edge to files in the output directory
	// vtkRobinsonCoastlines(outputFile.getParentFile(), map);
	// vtkRobinsonMapEdge(outputFile.getParentFile());
	// }

	public static void vtkRobinsonGrid(RobinsonProjection map,
			GeoTessGrid grid, int tessId, int level, DataOutputStream output,
			ArrayList<Point> vertices, ArrayList<int[]> iCells)
					throws Exception
	{
		ArrayList<ArrayList<double[]>> cells = new ArrayList<ArrayList<double[]>>();

		for (int t = grid.getFirstTriangle(tessId, level); t <= grid
				.getLastTriangle(tessId, level); ++t)
			map.projectTriangle(grid.getTriangleVertices(t), cells);

		Point point;
		Integer index;
		ArrayList<double[]> cell;
		HashMap<Point, Integer> points = new HashMap<Point, Integer>();

		iCells.clear();
		int cellCount = 0;
		for (int i = 0; i < cells.size(); ++i)
		{
			cell = cells.get(i);
			int[] c = new int[cell.size()];
			iCells.add(c);
			for (int j = 0; j < cells.get(i).size(); ++j)
			{
				point = new Point(cells.get(i).get(j));
				index = points.get(point);
				if (index == null)
				{
					index = points.size();
					points.put(point, index);
				}
				c[j] = index;
			}
			cellCount += cell.size();
		}

		vertices.clear();
		vertices.ensureCapacity(points.size());
		for (int i = 0; i < points.size(); ++i)
			vertices.add(null);
		for (Entry<Point, Integer> e : points.entrySet())
			vertices.set(e.getValue(), e.getKey());

		points = null;

		{
			// for every Point vertex, see if it coincides with a vertex of the 
			// current grid. If so, set the vertexIndex in the Point object to the
			// grid.vertex index.  Otherwise, leave it at -1.
			int t=0;
			int[] triangle;
			double[] vertex;
			for (int i = 0; i < vertices.size(); ++i)
			{
				vertex = vertices.get(i).v;
				t = grid.getTriangle(t, vertex);
				triangle = grid.getTriangles()[t];
				for (int j=0; j<3; ++j)
					if (VectorUnit.dot(vertex, grid.getVertex(triangle[j])) > 1.0-1e-7)
					{
						vertices.get(i).vertexIndex = triangle[j];
						break;
					}
			}
		}



		output.writeBytes(String.format("# vtk DataFile Version 2.0%n"));
		output.writeBytes(String.format("GeoTessRobinsonGrid%n"));
		output.writeBytes(String.format("BINARY%n"));

		output.writeBytes(String.format("DATASET UNSTRUCTURED_GRID%n"));

		output.writeBytes(String.format("POINTS %d double%n", vertices.size()));

		for (int i = 0; i < vertices.size(); ++i)
		{
			output.writeDouble(vertices.get(i).v[3]);
			output.writeDouble(vertices.get(i).v[4]);
			output.writeDouble(0.);
		}

		// write out node connectivity
		output.writeBytes(String.format("CELLS %d %d%n", iCells.size(),
				cellCount + iCells.size()));

		for (int i = 0; i < iCells.size(); ++i)
		{
			int[] c = iCells.get(i);
			output.writeInt(c.length);
			for (int j = 0; j < c.length; ++j)
				output.writeInt(c[j]);
		}

		output.writeBytes(String.format("CELL_TYPES %d%n", iCells.size()));
		for (int t = 0; t < iCells.size(); ++t)
			switch (iCells.get(t).length)
			{
			case 3:
				output.writeInt(5); // triangle
				break;
			case 4:
				output.writeInt(9); // quad
				break;
			default:
				throw new GeoTessException("cell size = "
						+ iCells.get(t).length + " is invalid");
			}
	}

	public static File mostRecentCoastLinesFile = null;

	public static void vtkRobinsonCoastlines(File outputDir,
			RobinsonProjection mapProjection) throws Exception
	{
		if (outputDir == null)
			outputDir = new File(".");

		if (!outputDir.exists())
			throw new IOException("\noutput directory "
					+ outputDir.getCanonicalPath() + "\ndoes not exist\n");

		if (!outputDir.isDirectory())
			throw new IOException("\n" + outputDir.getCanonicalPath()
			+ "\nis not a directory\n");

		mostRecentCoastLinesFile = new File(outputDir, String.format(
				"map_coastlines_centerLon_%1.0f.vtk",
				mapProjection.getCenterLon()));

		Tuple<double[][], int[][]> lines = loadContinentBoundaries(); // vtkExtract(coastLines);

		if (lines != null)
		{
			ArrayList<ArrayList<double[]>> points = new ArrayList<ArrayList<double[]>>();

			int n;
			for (int i = 0; i < lines.second.length; ++i)
			{
				ArrayList<double[]> line = new ArrayList<double[]>(
						lines.second[i].length);
				for (int j = 0; j < lines.second[i].length; ++j)
				{
					n = lines.second[i][j];
					line.add(lines.first[n]);
				}

				for (ArrayList<double[]> segment : mapProjection.project(line))
					if (segment.size() > 1)
						points.add(segment);
			}

			BufferedWriter output = new BufferedWriter(new FileWriter(
					mostRecentCoastLinesFile));
			output.write(String.format("# vtk DataFile Version 2.0%n"));
			output.write(String.format("Continent boundaries%n"));
			output.write(String.format("ASCII%n"));

			output.write(String.format("DATASET UNSTRUCTURED_GRID%n"));

			int nPoints = 0;
			for (ArrayList<double[]> segment : points)
				nPoints += segment.size();

			output.write(String.format("POINTS %1d double%n", nPoints));

			// iterate over all the grid nodes and write out their position
			for (ArrayList<double[]> segment : points)
				for (double[] point : segment)
					output.write(String.format("%s %s 0%n",
							Float.toString((float) point[0]),
							Float.toString((float) point[1])));

			int count = 0;
			for (ArrayList<double[]> segment : points)
				count += segment.size() + 1;

			// write out node connectivity
			output.write(String.format("CELLS %d %d%n", points.size(), count));

			n = 0;
			for (ArrayList<double[]> segment : points)
			{
				output.write(String.format("%d%n", segment.size()));
				for (int i = 0; i < segment.size(); ++i)
					output.write(String.format(" %d%n", n++));
			}

			output.write(String.format("CELL_TYPES %d%n", points.size()));

			for (int i = 0; i < points.size(); ++i)
				output.write(String.format("4%n"));

			output.close();
		}
	}

	public static File mostRecentMapEdgeFile = null;

	public static void vtkRobinsonMapEdge(File outputDir) throws IOException
	{
		if (outputDir == null)
			outputDir = new File(".");

		if (!outputDir.exists())
			throw new IOException("\noutput directory "
					+ outputDir.getCanonicalPath() + "\ndoes not exist\n");

		if (!outputDir.isDirectory())
			throw new IOException("\n" + outputDir.getCanonicalPath()
			+ "\nis not a directory\n");

		mostRecentMapEdgeFile = new File(outputDir,
				String.format("map_edge.vtk"));

		BufferedWriter output = new BufferedWriter(new FileWriter(
				mostRecentMapEdgeFile));
		output.write(String.format("# vtk DataFile Version 2.0%n"));
		output.write(String.format("Edge of Robinson Projection%n"));
		output.write(String.format("ASCII%n"));

		output.write(String.format("DATASET UNSTRUCTURED_GRID%n"));

		double[][] points = RobinsonProjection.getEdge();

		output.write(String.format("POINTS %1d double%n", points.length));

		// iterate over all the grid nodes and write out their position
		for (double[] point : points)
			output.write(String.format("%s %s 0%n",
					Float.toString((float) point[0]),
					Float.toString((float) point[1])));

		// write out node connectivity
		output.write(String.format("CELLS %d %d%n", 1, points.length + 1));

		output.write(String.format("%d%n", points.length));
		for (int i = 0; i < points.length; ++i)
			output.write(String.format(" %d%n", i));

		output.write(String.format("CELL_TYPES 1%n4%n"));

		output.close();
	}

	/**
	 * Copy the continent boundaries as unit vectors to a vtk file called
	 * 'continent_boundaries.vtk' in the specified directory.
	 * 
	 * @param outputDirectory
	 * @throws IOException
	 *             if outputDirectory is not an existing directory.
	 */
	public static void copyContinentBoundaries(File outputDirectory)
			throws IOException
	{
		if (outputDirectory == null)
			throw new IOException("\noutputDirectory is null.");

		if (!outputDirectory.isDirectory())
			throw new IOException(
					"\noutputDirectory does not specify an existing directory.");

		File outputFile = new File(outputDirectory, "continent_boundaries.vtk");

		InputStream inputStream = GeoTessModelUtils.class
				.getResourceAsStream("/resources/continent_boundaries.vtk");

		if (inputStream != null)
		{
			Scanner input = new Scanner(inputStream);
			BufferedWriter output = new BufferedWriter(new FileWriter(
					outputFile));
			while (input.hasNext())
			{
				output.write(input.nextLine());
				output.newLine();
			}
			output.close();
			input.close();
		}
	}

	/**
	 * Read a VTK file and extract the points.
	 * 
	 * @return Tuple double[npoints][3], int[nlines][npoints_on_line]
	 * @throws IOException
	 */
	public static Tuple<double[][], int[][]> loadContinentBoundaries()
			throws IOException
	{
		InputStream inp = GeoTessModelUtils.class.getResourceAsStream("/continent_boundaries.vtk");
		/*if (inp == null)
		{
			inp = GeoTessModelUtils.class.getResourceAsStream("/resources/continent_boundaries.vtk");
			if (inp == null)
			{
				inp = GeoTessModelUtils.class.getResourceAsStream("/geo-tess-java/resources/continent_boundaries.vtk");
				if (inp == null)
				{
					inp = new FileInputStream(PropertiesPlus.convertWinFilePathToLinux("\\\\tonto2\\GNEM\\devlpool\\sballar\\public\\GeoTess\\continent_boundaries.vtk"));
					System.out.println("Retrieved "+PropertiesPlus.convertWinFilePathToLinux("\\\\tonto2\\GNEM\\devlpool\\sballar\\public\\GeoTess\\continent_boundaries.vtk"));
				}
				else
					System.out.println("Retrieved /geo-tess-java/resources/continent_boundaries.vtk");
			}
			else
				System.out.println("Retrieved /resources/continent_boundaries.vtk");
		}
		else
			System.out.println("Retrieved /continent_boundaries.vtk");*/

		Scanner input = new Scanner(inp);

		input.nextLine();
		input.nextLine();
		input.nextLine();
		input.nextLine();
		int npoints = Integer.parseInt(input.nextLine()
				.replace("POINTS", "").replace("double", "")
				.replaceAll(" ", ""));
		double[][] points = new double[npoints][3];
		for (int i = 0; i < points.length; ++i)
			for (int j = 0; j < 3; ++j)
				points[i][j] = input.nextDouble();
		input.nextLine();
		input.next();
		int nlines = input.nextInt();
		input.nextLine();

		int[][] lines = new int[nlines][];
		for (int i = 0; i < lines.length; ++i)
		{
			lines[i] = new int[input.nextInt()];
			for (int j = 0; j < lines[i].length; ++j)
				lines[i][j] = input.nextInt();
		}

		input.close();
		return new Tuple<double[][], int[][]>(points, lines);


	}

	/**
	 * Generate a map of the size of the triangles in a grid. This size of the
	 * triangles is the edgelength of an equilateral triangle with the same
	 * area, in degrees.
	 * 
	 * @param grid
	 *            the grid to be plotted.
	 * @param outputFile
	 *            the name of the file to which to write the output. Must end
	 *            with extension 'vtk'.
	 * @param tessId
	 *            the index of the tessellation to be interrogated.
	 * @throws IOException
	 * @throws GeoTessException
	 */
	static public void vtkTriangleSize(GeoTessGrid grid, File outputFile,
			int tessId) throws IOException, GeoTessException
	{
		if (!outputFile.getName().endsWith(".vtk"))
			throw new IOException("outputFile " + outputFile
					+ " must end with .vtk");

		GeoTessMetaData md = new GeoTessMetaData();
		md.setDescription("");
		md.setAttributes("", "");
		md.setDataType(DataType.BYTE);
		md.setModelGenerationDate("today");
		md.setModelSoftwareVersion("v1");
		String[] layerNames = new String[grid.getNTessellations()];
		int[] layerTessIds = new int[layerNames.length];
		for (int i = 0; i < grid.getNTessellations(); ++i)
		{
			layerNames[i] = "layer" + i;
			layerTessIds[i] = i;
		}
		md.setLayerNames(layerNames);
		md.setLayerTessIds(layerTessIds);
		GeoTessModel model = new GeoTessModel(grid, md);

		DataOutputStream output = new DataOutputStream(
				new BufferedOutputStream(new FileOutputStream(outputFile)));

		int level = grid.getNLevels(tessId) - 1;

		vtkGrid(grid, tessId, level, output);

		int firstTriangle = grid.getFirstTriangle(tessId, level);
		int lastTriangle = grid.getLastTriangle(tessId, level);
		int nTriangles = grid.getLastTriangle(tessId, level) - firstTriangle
				+ 1;

		output.writeBytes(String.format("CELL_DATA %d%n", nTriangles));

		output.writeBytes(String.format("SCALARS Edge_length_deg float 1%n"));
		output.writeBytes(String.format("LOOKUP_TABLE default%n"));

		GeoTessPosition pos = model.getGeoTessPosition();

		double[][] tv;
		for (int t = firstTriangle; t <= lastTriangle; ++t)
		{
			pos.set(tessId, GeoTessUtils.center(grid.getTriangleVertices(t)),
					6371.);
			tv = grid.getTriangleVertices(pos.getTriangle());

			double area = GeoTessUtils.getTriangleArea(tv[0], tv[1], tv[2]);
			// convert square radians to square degrees
			area = Math.toDegrees(Math.toDegrees(area));

			// convert area to edgelength of equilateral triangle with that
			// area.
			output.writeFloat((float) Math.sqrt(4 * area / Math.sqrt(3)));
		}

		output.close();

	}

	/**
	 * Find attributeName for attributeIndex. If reciprocal is true convert
	 * 'slowness' to 'velocity' while maintaining correct case. Replace all
	 * spaces with underscores.
	 * 
	 * @param model
	 * @param attributeIndex
	 * @param reciprocal
	 * @return attributeName
	 */
	protected static String vtkName(GeoTessModel model, int attributeIndex,
			boolean reciprocal)
	{
		String name = model.getMetaData().getAttributeName(attributeIndex)
				.replaceAll(" ", "_");
		if (reciprocal)
		{
			if (name.contains("slowness"))
				name = name.replace("slowness", "velocity");

			else if (name.contains("Slowness"))
				name = name.replace("Slowness", "Velocity");

			else if (name.contains("SLOWNESS"))
				name = name.replace("SLOWNESS", "VELOCITY");

		}
		return name;
	}

	public static String statistics(GeoTessModel model)
	{
		StringBuffer out = new StringBuffer();

		int layerNameLength = "Layer ".length();
		for (int layer = 0; layer < model.getNLayers(); ++layer)
			if (model.getMetaData().getLayerNames()[layer].length() > layerNameLength)
				layerNameLength = model.getMetaData().getLayerNames()[layer]
						.length();

		int attributeNameLength = "Attribute".length();
		for (int a = 0; a < model.getMetaData().getNAttributes(); ++a)
		{
			int len = String.format("%s (%s)",
					model.getMetaData().getAttributeName(a),
					model.getMetaData().getAttributeUnit(a)).length();

			if (len > attributeNameLength)
				attributeNameLength = len;
		}

		String format = String.format("%%-%ds   %%-%ds", layerNameLength,
				attributeNameLength);

		out.append(String.format(format, "Layer", "Attribute"));
		out.append(String.format(
				" %8s %8s %8s %8s %15s %15s %15s %15s %15s %15s%n", "nVertex",
				"nNodes", "nNaN", "nValid", "Min  ", "Max  ", "Mean  ",
				"Median ", "StdDev  ", "MAD   "));

		ArrayList<Double> values = new ArrayList<Double>(10000);
		HashSetInteger vertices;
		Iterator vtx;
		double value;
		for (int layer = model.getNLayers() - 1; layer >= 0; --layer)
		{
			vertices = model.getConnectedVertices(layer);
			for (int attribute = 0; attribute < model.getMetaData()
					.getNAttributes(); ++attribute)
			{
				// values includes all non-NaN attribute values.
				values.clear();
				int nNodes = 0;
				int nNaN = 0;
				double sum = 0;
				double min = Double.POSITIVE_INFINITY;
				double max = Double.NEGATIVE_INFINITY;
				vtx = vertices.iterator();
				while (vtx.hasNext())
				{
					Profile p = model.getProfile(vtx.next(), layer);
					nNodes += p.getNData();
					for (int node = 0; node < p.getNData(); ++node)
					{
						value = p.getValue(attribute, node);

						if (Double.isNaN(value))
							++nNaN;
						else
						{
							values.add(value);
							sum += value;
							min = value < min ? value : min;
							max = value > max ? value : max;
						}
					}
				}

				double median = 0;
				double mean = sum / values.size();
				double std = 0.;
				double mad = 0.;

				Collections.sort(values);
				median = values.size() == 0 ? Double.NaN : values.get(values
						.size() / 2);
				for (int i = 0; i < values.size(); ++i)
				{
					value = values.get(i) - mean;
					std += value * value;
				}
				std = values.size() == 0 ? Double.NaN : Math.sqrt(std
						/ (values.size() - 1));

				for (int i = 0; i < values.size(); ++i)
				{
					value = Math.abs(values.get(i) - median);
					values.set(i, value);
				}
				Collections.sort(values);
				mad = values.size() == 0 ? Double.NaN : values.get(values
						.size() / 2);

				out.append(String.format(format, model.getMetaData()
						.getLayerNames()[layer], String.format("%s (%s)", model
								.getMetaData().getAttributeName(attribute), model
								.getMetaData().getAttributeUnit(attribute))));

				out.append(String
						.format(" %8d %8d %8d %8d %15.7g %15.7g %15.7g %15.7g %15.7g %15.7g%n",
								vertices.size(), nNodes, nNaN, values.size(),
								min, max, mean, median, std, mad));
			}

		}

		return out.toString();
	}

	public static void vtkGreatCircle(File outputFile, List<double[]> vector)
			throws IOException
	{
		vtkGreatCircle(outputFile, vector.toArray(new double[vector.size()][]));
	}

	public static void vtkGreatCircle(File outputFile, double[]... vector)
			throws IOException
	{
		DataOutputStream output = new DataOutputStream(
				new BufferedOutputStream(new FileOutputStream(outputFile)));

		output.writeBytes(String.format("# vtk DataFile Version 2.0%n"));
		output.writeBytes(String.format("GreatCircles%n"));
		output.writeBytes(String.format("BINARY%n"));

		output.writeBytes(String.format("DATASET POLYDATA%n"));

		int nGreatCircles = vector.length / 2;

		ArrayList<double[]> points = new ArrayList<double[]>(1000);
		ArrayList<Integer> npoints = new ArrayList<Integer>(nGreatCircles);
		for (int i = 0; i < nGreatCircles; ++i)
		{
			GreatCircle gc = new GreatCircle(vector[2 * i], vector[2 * i + 1]);
			int n = (int) Math.ceil(gc.getDistanceDegrees());
			if (n > 0)
			{
				n *= 2;
				npoints.add(n);
				points.addAll(gc.getPoints(n, false));
			}
		}

		nGreatCircles = npoints.size();

		output.writeBytes(String.format("POINTS %d double%n", points.size()));

		for (int i = 0; i < points.size(); ++i)
		{
			double[] u = points.get(i);
			output.writeDouble(u[0]);
			output.writeDouble(u[1]);
			output.writeDouble(u[2]);
		}

		// write out node connectivity
		output.writeBytes(String.format("LINES %d %d%n", nGreatCircles,
				nGreatCircles + points.size()));
		int i = 0;
		for (int n : npoints)
		{
			output.writeInt(n);
			for (int j = 0; j < n; ++j)
				output.writeInt(i++);
		}

		output.close();
	}

	/**
	 * Creates an image of the model with minimum parameters.  Uses default lat/lon ([-90,90] and [-180,180])
	 * and a Color[] for which values in the model will be divided up into ranges and interpolated between those Colors.
	 * @param model - GeoTessModel, whose values will be used to compute the
	 *                colors of pixels in the resulting image.
	 * @param fracRadius
	 *            - the fractional radius within the layer at which samples should
	 *              be interpolated. Fractional radius <= 0.0 will return values
	 *              at the bottom of the layer and values >= 1.0 will return
	 *              values at the top of the layer.
	 * @param attributeIndex
	 *            - index of GeoTessModel attribute at which to retrieve data
	 *              values from the model.
	 * @param reciprocal
	 *            - Passed to the call to getMapValuesLayer() to read the model data.  From that function: 
	 *              "if false, return value; if true, return 1./value".
	 * @param layerID
	 *            - layerID to access
	 * @param numOfPixelsHorizontal - width of image in pixels.  The height of the image will be calculated
	 *        using the aspect ratio between the latitude range and longitude range.  720 is a reasonable value for this parameter,
	 *        which will create a 360x720 pixel BufferedImage.
	 * @param colors
	 *            - Color[] representing a color gradient on which to map the
	 *              values of the model.  Two common ways to make one:
	 *              1.) Construct one manually with Color constructors.  For example, 
	 *                  "Color[] colors = new Color[] {Color.RED, Color.WHITE, Color.BLUE};"
	 *                  There are other ways to construct a Java.awt.Color object, see the API for more details.
	 *              2.) Use the gms.shared.utilities.gmp.util.colormap package from project Utils, which has
	 *                  an enum for some Color arrays.  For example,
	 *                  "Color[] colors = SimpleColorMap.ColorGradient.RED_WHITE_BLUE.colors;"
	 * @return - BufferedImage rendering of the values in the model on a color
	 *           gradient specified by the input color array.
	 * @throws GeoTessException - possibly from reading GeoTessModel
	 */
	public static BufferedImage getImage(GeoTessModel model, double fracRadius,
			int attributeIndex, boolean reciprocal, int layerID, int numOfPixelsHorizontal, Color[] colors) throws GeoTessException
	{
		final double minLat = -90, maxLat = 90, minLon = -180, maxLon = 180;    // degrees, whole earth.
		return getImage(model, minLat, maxLat, minLon, maxLon, fracRadius,
				attributeIndex, reciprocal, layerID, numOfPixelsHorizontal, colors);
	}

	/**
	 * Creates an image while specifying bounds for latitude and longitude.  The minimum and maximum
	 * values in the model will be found directly (since they're not specified) and used 
	 * to compute Colors for each value.
	 * 
	 * @param model
	 *            - GeoTessModel, whose values will be used to compute the
	 *              colors of pixels in the resulting image.
	 * @param minLat
	 *            - minimum latitude at which to draw the model in degrees
	 * @param maxLat
	 *            - maximum latutude at which to draw the model in degrees
	 * @param minLon
	 *            - minimum longitude at which to draw the model in degrees
	 * @param maxLon
	 *            - maximum longitude at which to draw the model in degrees
	 * @param fracRadius 
	 *            - the fractional radius within the layer at which samples should
	 *              be interpolated. Fractional radius <= 0.0 will return values
	 *              at the bottom of the layer and values >= 1.0 will return
	 *              values at the top of the layer.
	 * @param attributeIndex
	 *            - index of GeoTessModel attribute at which to retrieve data
	 *              values from the model.
	 * @param reciprocal
	 *            - Passed to the call to getMapValuesLayer() to read the model data.  From that function: 
	 *              "if false, return value; if true, return 1./value".
	 * @param layerID
	 *            - layerID to access
	 * @param numOfPixelsHorizontal - width of image in pixels.  The height of the image will be calculated
	 *              using the aspect ratio between the latitude range and longitude range.  720 is a reasonable value 
	 *              for this parameter, which will create a 360x720 pixel BufferedImage.
	 * @param colors
	 *            - Color[] representing a color gradient on which to map the
	 *              values of the model.  Two common ways to make one:
	 *              1.) Construct one manually with Color constructors.  For example, 
	 *                  "Color[] colors = new Color[] {Color.RED, Color.WHITE, Color.BLUE};"
	 *                  There are other ways to construct a Java.awt.Color object, see the API for more details.
	 *              2.) Use the gms.shared.utilities.gmp.util.colormap package from project Utils, which has
	 *                  an enum for some Color arrays.  For example,
	 *                  "Color[] colors = SimpleColorMap.ColorGradient.RED_WHITE_BLUE.colors;"
	 * @return BufferedImage rendering of the values in the model on a color
	 *         gradient specified by the input color array.
	 * @throws GeoTessException - possibly from reading a GeoTessModel
	 */
	public static BufferedImage getImage(GeoTessModel model, double minLat,
			double maxLat, double minLon, double maxLon, double fracRadius, int attributeIndex, boolean reciprocal,
			int layerID, int numOfPixelsHorizontal, Color[] colors) throws GeoTessException
	{ 
		// pass NaN for min and max value of ColorMap, will will prompt the function to find them from the model data.
		return getImage(model, minLat, maxLat, minLon, maxLon, fracRadius, attributeIndex, reciprocal, 
				layerID, numOfPixelsHorizontal, new SimpleColorMap(colors, Double.NaN, Double.NaN));
	}

	/**
	 * Makes an image with custom defined color ranges given by a Color[] and matching double[].  This allows the caller
	 * to define color ranges/spacings that are of nonuniform size by specifying exactly which data values map to which colors
	 * (and thus implicitly the ranges of values for which color values will be interpolated).
	 * For example, specifying colors = {Color.RED, Color.WHITE, Color.BLUE} and values = [5, 10, 15]
	 * will set values <= 5 to RED, values that == 10 to WHITE, and values >= 15 to BLUE.
	 * Values in (5,10) will be interpolated linearly between RED and WHITE, 
	 * and in (10,15) will be interpolated linearly between WHITE and BLUE.
	 * 
	 * @param model
	 *            - GeoTessModel, whose values will be used to compute the
	 *              colors of pixels in the resulting image.
	 * @param minLat
	 *            - minimum latitude at which to draw the model in degrees
	 * @param maxLat
	 *            - maximum latutude at which to draw the model in degrees
	 * @param minLon
	 *            - minimum longitude at which to draw the model in degrees
	 * @param maxLon
	 *            - maximum longitude at which to draw the model in degrees
	 * @param fracRadius 
	 *            - the fractional radius within the layer at which samples should
	 *              be interpolated. Fractional radius <= 0.0 will return values
	 *              at the bottom of the layer and values >= 1.0 will return
	 *              values at the top of the layer.
	 * @param attributeIndex
	 *            - index of GeoTessModel attribute at which to retrieve data
	 *              values from the model.
	 * @param reciprocal
	 *            - Passed to the call to getMapValuesLayer() to read the model data.  From that function: 
	 *              "if false, return value; if true, return 1./value".
	 * @param layerID
	 *            - layerID to access
	 * @param numOfPixelsHorizontal - width of image in pixels.  The height of the image will be calculated
	 *              using the aspect ratio between the latitude range and longitude range.  720 is a reasonable value 
	 *              for this parameter, which will create a 360x720 pixel BufferedImage.
	 * @param colors
	 *            - Color[] representing a color gradient on which to map the
	 *              values of the model.  Two common ways to make one:
	 *              1.) Construct one manually with Color constructors.  For example, 
	 *                  "Color[] colors = new Color[] {Color.RED, Color.WHITE, Color.BLUE};"
	 *                  There are other ways to construct a Java.awt.Color object, see the API for more details.
	 *              2.) Use the gms.shared.utilities.gmp.util.colormap package from project Utils, which has
	 *                  an enum for some Color arrays.  For example,
	 *                  "Color[] colors = SimpleColorMap.ColorGradient.RED_WHITE_BLUE.colors;"
	 * @param colorValues
	 *            - defines the locations along the value line to which the colors will map to.  In other words,
	 *              specifying this parameter changes the ranges of values that get mapped to colors.
	 * @return BufferedImage rendering of the values in the model on a color
	 *         gradient specified by the input color array.
	 * @throws GeoTessException - possibly from reading a GeoTessModel
	 */
	public static BufferedImage getImage(GeoTessModel model, double minLat,
			double maxLat, double minLon, double maxLon, double fracRadius, int attributeIndex, boolean reciprocal, 
			int layerID, int numOfPixelsHorizontal, Color[] colors, double[] colorValues) throws GeoTessException
	{
		/* Make ColorMap from Color[] and double[]. */
		ColorMap cm = new SimpleColorMap(colors, colorValues);
		/**********************************************************************/

		return getImage(model, minLat, maxLat, minLon, maxLon, fracRadius, 
				attributeIndex, reciprocal, layerID, numOfPixelsHorizontal, cm);
	}

	/**
	 * Makes an image with defined color ranges given by a Color[].  Uses 
	 * the default lat/lon bounds of lat[-90, 90] and lon[-180,180].  This method also allows the color to specify the 
	 * min and max values of the data model for which to interpolate colors - data values outside of [minValue, maxValue]
	 * will be set to the min and max colors, respectively (colors[0] and colors[colors.length-1]).
	 * 
	 * @param model
	 *            - GeoTessModel, whose values will be used to compute the
	 *              colors of pixels in the resulting image.
	 * @param fracRadius 
	 *            - the fractional radius within the layer at which samples should
	 *              be interpolated. Fractional radius <= 0.0 will return values
	 *              at the bottom of the layer and values >= 1.0 will return
	 *              values at the top of the layer.
	 * @param minValue 
	 *            - minimum value of model to consider.  Any data value in the model less than minValue will be given
	 *              the color colors[0].
	 * @param maxValue
	 *            - maximum value of model to consider.  Any data value in the model greater than maxValue will be given
	 *              the color colors[colors.length-1].
	 * @param attributeIndex
	 *            - index of GeoTessModel attribute at which to retrieve data
	 *              values from the model.
	 * @param reciprocal
	 *            - Passed to the call to getMapValuesLayer() to read the model data.  From that function: 
	 *              "if false, return value; if true, return 1./value".
	 * @param layerID
	 *            - layerID to access
	 * @param numOfPixelsHorizontal - width of image in pixels.  The height of the image will be calculated
	 *              using the aspect ratio between the latitude range and longitude range.  720 is a reasonable value 
	 *              for this parameter, which will create a 360x720 pixel BufferedImage.
	 * @param colors
	 *            - Color[] representing a color gradient on which to map the
	 *              values of the model.  Two common ways to make one:
	 *              1.) Construct one manually with Color constructors.  For example, 
	 *                  "Color[] colors = new Color[] {Color.RED, Color.WHITE, Color.BLUE};"
	 *                  There are other ways to construct a Java.awt.Color object, see the API for more details.
	 *              2.) Use the gms.shared.utilities.gmp.util.colormap package from project Utils, which has
	 *                  an enum for some Color arrays.  For example,
	 *                  "Color[] colors = SimpleColorMap.ColorGradient.RED_WHITE_BLUE.colors;"
	 * @throws GeoTessException - possibly from reading a GeoTessModel
	 */
	public static BufferedImage getImage(GeoTessModel model, double fracRadius, double minValue, double maxValue, 
			int attributeIndex, boolean reciprocal, int layerID, int numOfPixelsHorizontal, Color[] colors) throws GeoTessException
	{   
		final double minLat = -90, maxLat = 90, minLon = -180, maxLon = 180;    // degrees, whole earth.

		return getImage(model, minLat, maxLat, minLon, maxLon, fracRadius, minValue, maxValue, 
				attributeIndex, reciprocal, layerID, numOfPixelsHorizontal, colors);
	}

	/**
	 * Makes an image with specified lat/lon ranges, min and max values of the model to consider for color interpolation,
	 * and a Color[] defining the colors to use.
	 * 
	 * @param model
	 *            - GeoTessModel, whose values will be used to compute the
	 *              colors of pixels in the resulting image.
	 * @param minLat
	 *            - minimum latitude at which to draw the model in degrees
	 * @param maxLat
	 *            - maximum latutude at which to draw the model in degrees
	 * @param minLon
	 *            - minimum longitude at which to draw the model in degrees
	 * @param maxLon
	 *            - maximum longitude at which to draw the model in degrees
	 * @param fracRadius 
	 *            - the fractional radius within the layer at which samples should
	 *              be interpolated. Fractional radius <= 0.0 will return values
	 *              at the bottom of the layer and values >= 1.0 will return
	 *              values at the top of the layer
	 * @param minValue 
	 *            - minimum value of model to consider.  Any data value in the model less than minValue will be given
	 *              the color colors[0].
	 * @param maxValue
	 *            - maximum value of model to consider.  Any data value in the model greater than maxValue will be given
	 *              the color colors[colors.length-1].
	 * @param attributeIndex
	 *            - index of GeoTessModel attribute at which to retrieve data
	 *              values from the model.
	 * @param reciprocal
	 *            - Passed to the call to getMapValuesLayer() to read the model data.  From that function: 
	 *              "if false, return value; if true, return 1./value".
	 * @param layerID
	 *            - layerID to access
	 * @param numOfPixelsHorizontal - width of image in pixels.  The height of the image will be calculated
	 *              using the aspect ratio between the latitude range and longitude range.  720 is a reasonable value 
	 *              for this parameter, which will create a 360x720 pixel BufferedImage.
	 * @param colors
	 *            - Color[] representing a color gradient on which to map the
	 *              values of the model.  Two common ways to make one:
	 *              1.) Construct one manually with Color constructors.  For example, 
	 *                  "Color[] colors = new Color[] {Color.RED, Color.WHITE, Color.BLUE};"
	 *                  There are other ways to construct a Java.awt.Color object, see the API for more details.
	 *              2.) Use the gms.shared.utilities.gmp.util.colormap package from project Utils, which has
	 *                  an enum for some Color arrays.  For example,
	 *                  "Color[] colors = SimpleColorMap.ColorGradient.RED_WHITE_BLUE.colors;"
	 * @throws GeoTessException - possibly from reading a GeoTessModel
	 */
	public static BufferedImage getImage(GeoTessModel model, double minLat,
			double maxLat, double minLon, double maxLon, double fracRadius, double minValue, double maxValue, 
			int attributeIndex, boolean reciprocal, int layerID, int numOfPixelsHorizontal, Color[] colors) throws GeoTessException
	{   
		/* Make a ColorMap using the given Color[] and min/max data values.  */
		ColorMap cm = new SimpleColorMap(colors, minValue, maxValue);

		return getImage(model, minLat, maxLat, minLon, maxLon, fracRadius, 
				attributeIndex, reciprocal, layerID, numOfPixelsHorizontal, cm);
	}


	/**
	 * Creates a BufferedImage representation of the values of a GeoTessModel.
	 * 
	 * @param model
	 *            - GeoTessModel, whose values will be used to compute the
	 *              colors of pixels in the resulting image.
	 * @param minLat
	 *            - minimum latitude at which to draw the model in degrees
	 * @param maxLat
	 *            - maximum latutude at which to draw the model in degrees
	 * @param minLon
	 *            - minimum longitude at which to draw the model in degrees
	 * @param maxLon
	 *            - maximum longitude at which to draw the model in degrees
	 * @param fracRadius 
	 *            - the fractional radius within the layer at which samples should
	 *              be interpolated. Fractional radius <= 0.0 will return values
	 *              at the bottom of the layer and values >= 1.0 will return
	 *              values at the top of the layer.
	 * @param attributeIndex
	 *            - index of GeoTessModel attribute at which to retrieve data
	 *              values from the model.
	 * @param reciprocal
	 *            - Passed to the call to getMapValuesLayer() to read the model data.  From that function: 
	 *              "if false, return value; if true, return 1./value".
	 * @param layerID
	 *            - layerID to access
	 * @param numOfPixelsHorizontal - width of image in pixels.  The height of the image will be calculated
	 *              using the aspect ratio between the latitude range and longitude range.  720 is a reasonable value 
	 *              for this parameter, which will create a 360x720 pixel BufferedImage.
	 * @param cm
	 *            - ColorMap to use for interpolation.  If the min and max values of the ColorMap are NaN,
	 *              the minimum and maximum values of the model will be found and used to make a new ColorMap.
	 *              See the gms.shared.utilities.gmp.util.colormap package in the Utils project for more info.
	 * @return BufferedImage rendering of the values in the model on a color
	 *         gradient specified by the input color array.
	 * @throws GeoTessException - possibly from reading a GeoTessModel
	 */
	public static BufferedImage getImage(GeoTessModel model, double minLat,
			double maxLat, double minLon, double maxLon, double fracRadius, int attributeIndex, boolean reciprocal,
			int layerID, int numOfPixelsHorizontal, ColorMap cm) throws GeoTessException
	{
		/* Correct longitude and latitude if necessary. */
		while (maxLon <= minLon)
			maxLon += 360;
		if (maxLat < minLat)   // swap
		{  double temp = maxLat;
		maxLat = minLat;
		minLat = temp;
		}
		/**********************************************************************/

		/* Check if latitude is out of range, throw exception if so.  This is done after
		 * min and max latitudes have been corrected above to make the check more concise/robust. */
		if (minLat < -90 || maxLat > 90)
			throw new GeoTessException("GeoTessModelUtils.getImage: Either min or max latitude is out of the range [-90,90] degrees");
		/**********************************************************************/

		/* Initialize heatmap parameters. */
		final double aspectRatio = (maxLon - minLon) / (maxLat - minLat);
		final int numOfPixelsVertical = (int) (Math.round(numOfPixelsHorizontal / aspectRatio));
		/**********************************************************************/

		/* Get GeoTessModel data. */
		final int attributes[] = { attributeIndex };
		final double[] lats = getLatitudes(minLat, maxLat, numOfPixelsVertical, true);
		final double[] lons = getLongitudes(minLon, maxLon, numOfPixelsHorizontal, true, true);

		double[][][] mapValues = getMapValuesLayer(model, lats, lons, layerID,
				fracRadius, InterpolatorType.LINEAR,
				InterpolatorType.LINEAR, reciprocal, attributes);
		/**********************************************************************/

		/* Go through data points and find the minimum and maximum if the ColorMap has NaN for them.
		 * This occurs when the caller wants this function to find the min and max and make the ColorMap for them. */
		if (Double.isNaN(cm.getMinValue()) || Double.isNaN(cm.getMaxValue()))
		{
			double minValue = findMin(mapValues, 0, lats.length, lons.length);
			double maxValue = findMax(mapValues, 0, lats.length, lons.length);
			cm = new SimpleColorMap(cm.getColors(), minValue, maxValue);
		}
		/**********************************************************************/

		return makeImage(mapValues, 0, cm);
	}

	public static double findMin(double[][][] mapValues, int attrIndex, double width, double height)
	{
		double minValue = Double.POSITIVE_INFINITY;
		for (int lat = 0; lat < width; lat++)
		{
			for (int lon = 0; lon < height; lon++)
			{
				double value = mapValues[lat][lon][attrIndex];
				if (Double.isNaN(value)) continue;
				if (value < minValue)
					minValue = value;
			}
		}
		return minValue;
	}

	public static double findMax(double[][][] mapValues, int attrIndex, double width, double height)
	{
		double maxValue = Double.NEGATIVE_INFINITY;
		for (int lat = 0; lat < width; lat++)
		{
			for (int lon = 0; lon < height; lon++)
			{
				double value = mapValues[lat][lon][attrIndex];
				if (Double.isNaN(value)) continue;
				if (value > maxValue)
					maxValue = value;
			}
		}
		return maxValue;
	}

	/**
	 * Goes through model values at the attribute index and set pixels on image accordingly using the
	 * Utils.gms.shared.utilities.gmp.util.ColorMap package.  This method is normally called by getImage() to
	 * ensure that the mapValues array is reasonably constructed.
	 * @param mapValues - data model values extracted from a GeoTessModel via getMapValuesLayer()
	 * @param attributeIndex - index at which to pull data values (third index of 3D array)
	 * @param cm - ColorMap that defines a mapping from data values to Colors
	 */
	private static BufferedImage makeImage(double[][][] mapValues, int attributeIndex, ColorMap cm)
	{ int nlat = mapValues.length;
	int nlon = mapValues[0].length;
	BufferedImage bi = new BufferedImage(nlon, nlat, BufferedImage.TYPE_INT_ARGB);		
	for (int lat = 0; lat < nlat; lat++)
	{
		for (int lon = 0; lon < nlon; lon++)
		{
			/* Get value from model, check if NaN.  */
			double value = mapValues[lat][lon][attributeIndex];

			if (Double.isNaN(value))
				continue;
			/*******************************************/

			/*
			 * Get Color from ColorMap gradient, paint corresponding pixel
			 * in image.  lat is corrected to flip since top-left is (0,0) and 
			 * account for index being from [0, height]
			 */
			bi.setRGB(lon, nlat - lat - 1, cm.getRGB(value));
			/*******************************************/
		}
	}

	return bi;
	}

	public static void vtkRobinsonGreatCircle(File outputFile,
			double centerLonDegrees, List<double[]> vector) throws IOException
	{
		vtkRobinsonGreatCircle(outputFile, centerLonDegrees,
				vector.toArray(new double[vector.size()][]));
	}

	public static void vtkRobinsonGreatCircle(File outputFile,
			double centerLonDegrees, double[]... vector) throws IOException
	{
		try
		{
			RobinsonProjection map;
			map = new RobinsonProjection(centerLonDegrees);
			DataOutputStream output = new DataOutputStream(
					new BufferedOutputStream(new FileOutputStream(outputFile)));

			output.writeBytes(String.format("# vtk DataFile Version 2.0%n"));
			output.writeBytes(String.format("GreatCircles%n"));
			output.writeBytes(String.format("BINARY%n"));

			output.writeBytes(String.format("DATASET POLYDATA%n"));

			int nGreatCircles = vector.length / 2;

			ArrayList<double[]> points = new ArrayList<double[]>(1000);
			ArrayList<Integer> npoints = new ArrayList<Integer>(nGreatCircles);
			for (int i = 0; i < nGreatCircles; ++i)
			{
				GreatCircle gc = new GreatCircle(vector[2 * i],
						vector[2 * i + 1]);
				int n = (int) Math.ceil(gc.getDistanceDegrees());
				if (n > 0)
				{
					n *= 2;

					for (ArrayList<double[]> pnts : map.project(gc.getPoints(n,
							false)))
					{
						npoints.add(pnts.size());
						points.addAll(pnts);
					}
				}
			}

			nGreatCircles = npoints.size();

			output.writeBytes(String.format("POINTS %d double%n", points.size()));

			for (int i = 0; i < points.size(); ++i)
			{
				double[] u = points.get(i);
				output.writeDouble(u[0]);
				output.writeDouble(u[1]);
				output.writeDouble(0);
			}

			// write out node connectivity
			output.writeBytes(String.format("LINES %d %d%n", nGreatCircles,
					nGreatCircles + points.size()));
			int i = 0;
			for (int n : npoints)
			{
				output.writeInt(n);
				for (int j = 0; j < n; ++j)
					output.writeInt(i++);
			}

			output.close();

			// write the coastlines and map edge to files in the output
			// directory
			vtkRobinsonCoastlines(outputFile.getParentFile(), map);
			vtkRobinsonMapEdge(outputFile.getParentFile());
		}
		catch (Exception e)
		{
			throw new IOException(e);
		}

	}

	/**
	 * Generate a VTK file of the
	 * 
	 * @param model
	 *            a reference to the model
	 * @param fileName
	 *            the name of the file to which output should be written. If the
	 *            fileName contains '%d' then the layer number is inserted into
	 *            that position of the fileName. If fileName contains '%s' then
	 *            the layer name is inserted into that position of the fileName.
	 * @param firstLayer
	 *            the first (innermost) layer to include
	 * @param lastLayer
	 *            the last (outermost) layer to include. If value specified is
	 *            greater than last layer of the model then value is replaced
	 *            with the index of the last layer in the model.
	 * @param reciprocal
	 *            if true then plot 1/attribute value instead of attribute
	 *            value.
	 * @param attributes
	 *            indexes of the attributes to include. If null, all values are
	 *            output.
	 * @throws GeoTessException
	 * @throws IOException
	 */
	public static void vtkIntegral(GeoTessModel model, String fileName,
			int firstLayer, int lastLayer, boolean reciprocal, int[] attributes)
					throws IOException, GeoTessException
	{
		if (!fileName.toLowerCase().trim().endsWith(".vtk"))
			throw new IOException("\nOutput file name must have .vtk extension");

		if (lastLayer >= model.getNLayers())
			lastLayer = model.getNLayers() - 1;

		if (attributes == null)
		{
			attributes = new int[model.getMetaData().getNAttributes()];
			for (int i = 1; i < attributes.length; ++i)
				attributes[i] = i;
		}
		DataOutputStream output = new DataOutputStream(
				new BufferedOutputStream(new FileOutputStream(
						new File(fileName))));

		int tessid = model.getMetaData()
				.getTessellation(model.getNLayers() - 1);

		int level = model.getGrid().getNLevels(tessid) - 1;

		int[] vertices = vtkGrid(model.getGrid(), tessid, level, output);

		output.writeBytes(String.format("POINT_DATA %d%n", vertices.length));

		for (int a = 0; a < attributes.length; ++a)
		{
			String attributeName = vtkName(model, attributes[a], reciprocal);

			output.writeBytes(String.format("SCALARS %s float 1%n",
					attributeName));
			output.writeBytes(String.format("LOOKUP_TABLE default%n"));

			for (int i = 0; i < vertices.length; ++i)
				output.writeFloat((float) integrate(model, attributes[a],
						reciprocal, firstLayer, lastLayer, vertices[i]));
		}
		output.close();
	}

	public static double integrate(GeoTessModel model, int attributeIndex,
			boolean reciprocal, int firstLayer, int lastLayer, int vertex)
	{
		if (lastLayer >= model.getNLayers())
			lastLayer = model.getNLayers() - 1;

		double integral = 0;
		Profile[] profiles = model.getProfiles(vertex);
		for (int layer = firstLayer; layer <= lastLayer; ++layer)
			integral += profiles[layer].integrate(attributeIndex, reciprocal);
		return integral;
	}

	public static void vtkEllipse(File outputFile, double[] center,
			double major, double minor, double trend, int npoints)
					throws IOException
	{
		DataOutputStream output = new DataOutputStream(
				new BufferedOutputStream(new FileOutputStream(outputFile)));

		output.writeBytes(String.format("# vtk DataFile Version 2.0%n"));
		output.writeBytes(String.format("Ellipse%n"));
		output.writeBytes(String.format("BINARY%n"));

		output.writeBytes(String.format("DATASET POLYDATA%n"));

		double[][] points = VectorUnit.getEllipse(center, major, minor, trend,
				npoints);

		output.writeBytes(String.format("POINTS %d double%n", npoints));

		for (int i = 0; i < npoints; ++i)
		{
			double[] u = points[i];
			output.writeDouble(u[0]);
			output.writeDouble(u[1]);
			output.writeDouble(u[2]);
		}

		// write out node connectivity
		output.writeBytes(String.format("LINES %d %d%n", 1, 1 + npoints));
		output.writeInt(npoints);
		for (int j = 0; j < npoints; ++j)
			output.writeInt(j);

		output.close();
	}

	public static void vtkRobinsonEllipse(File outputFile,
			double centerLonDegrees, double[] center, double major,
			double minor, double trend, int npoints) throws IOException
	{
		try
		{
			RobinsonProjection map = new RobinsonProjection(centerLonDegrees);
			DataOutputStream output = new DataOutputStream(
					new BufferedOutputStream(new FileOutputStream(outputFile)));

			output.writeBytes(String.format("# vtk DataFile Version 2.0%n"));
			output.writeBytes(String.format("Ellipse%n"));
			output.writeBytes(String.format("BINARY%n"));

			output.writeBytes(String.format("DATASET POLYDATA%n"));

			double[][] ellipse = VectorUnit.getEllipse(center, major, minor,
					trend, npoints);

			ArrayList<ArrayList<double[]>> lines = map.project(Arrays
					.asList(ellipse));

			int np = 0;
			for (ArrayList<double[]> points : lines)
				np += points.size();

			output.writeBytes(String.format("POINTS %d double%n", np));

			for (ArrayList<double[]> points : lines)
				for (double[] point : points)
				{
					output.writeDouble(point[0]);
					output.writeDouble(point[1]);
					output.writeDouble(0);
				}

			// write out node connectivity
			output.writeBytes(String.format("LINES %d %d%n", lines.size(),
					lines.size() + np));
			int i = 0;
			for (ArrayList<double[]> points : lines)
			{
				output.writeInt(points.size());
				for (int j = 0; j < points.size(); ++j)
					output.writeInt(i++);
			}

			output.close();

			// write the coastlines and map edge to files in the output
			// directory
			vtkRobinsonCoastlines(outputFile.getParentFile(), map);
			vtkRobinsonMapEdge(outputFile.getParentFile());
		}
		catch (Exception e)
		{
			throw new IOException(e);
		}

	}

}
