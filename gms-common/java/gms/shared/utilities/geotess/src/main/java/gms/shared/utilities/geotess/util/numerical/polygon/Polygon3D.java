package gms.shared.utilities.geotess.util.numerical.polygon;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * An extension of Polygon.java that includes the ability to limit the radial extent of a
 * polygon.
 * 
 * @author sballar
 * 
 */
public class Polygon3D extends Polygon
{

	private Horizon top;

	private Horizon bottom;

	public Polygon3D()
	{
		super();
	}

	/**
	 * Parse the records in a file that define this Polygon3D.
	 * The first record must be the String "POLYGON3D".
	 * The records must contain two records that define the top and bottom
	 * of the polygon3d.  These records can appear anywhere in the list of records
	 * but must each contain 4 tokens:
	 * <ol start=0>
	 * <li>either 'TOP' or 'BOTTOM' (case sensitive)
	 * <li>either 'layer', 'depth' or 'radius' (case insensitive)
	 * <li>either fractional radius within a layer, depth in km or radius in km
	 * <li>layer index. If layerIndex is >= 0 and < the number of layers represented in a model,
	 * then the returned radius will be constrained to be between the top and bottom of the 
	 * specified layer.  Otherwise, the radius will not be so constrained. 
	 * </ol>
	 * <p>For example:
	 * <ul>
	 * <li>"TOP layer 0 4" will place the top of 
	 * the polygon at the bottom of layer 4.  
	 * <li>"BOTTOM radius 3000 -1" will place the bottom of the polyon at 
	 * radius = 3000 km and layer boundaries will be ignored.  
	 * <li>"TOP depth 50 7" will place the top of the polygon at a depth of 
	 * 50 km in layer 7.  Wherever the bottom of layer 7 is at a depth less than  
	 * 50 km, the top of the polygon will correspond to the bottom of layer 7. 
	 * Wherever the top of layer 7 is at a depth greater than 50 km, the 
	 * top of the polygon will correspond to the top of layer 7.
	 * </ul>
	 * @param file 
	 * @throws IOException
	 */
	public Polygon3D(File file) throws IOException
	{
		if (!file.exists())
			throw new IOException(String.format("File does not exist%n%s",
					file.getCanonicalPath()));
		
		polygonFile = file;

		String fileExtension = "";
		int idx = file.getName().lastIndexOf('.');
		if (idx >= 0)
			fileExtension = file.getName().substring(idx + 1);

		if (fileExtension.equalsIgnoreCase("kml")
				|| fileExtension.equalsIgnoreCase("kmz"))
			throw new IOException(
					"Cannot construct a Polygon3D polygon from a file in '"
							+ fileExtension + "' format.");

		parseRecords(readRecords(file));
		
	}

	public Polygon3D(boolean global, Horizon bottom, Horizon top)
			throws IOException
	{
		super(global);
		this.bottom = bottom;
		this.top = top;

		if (bottom.getLayerIndex() > top.getLayerIndex())
			throw new IOException(
					"Layer index of bottom horizon is greater than layer index of top horizon");
	}

	public Polygon3D(List<double[]> points, Horizon bottom, Horizon top)
			throws IOException
	{
		super(points);
		this.bottom = bottom;
		this.top = top;

		if (bottom.getLayerIndex() > top.getLayerIndex())
			throw new IOException(
					"Layer index of bottom horizon is greater than layer index of top horizon");
	}

	public Polygon3D(double[][] points, Horizon bottom, Horizon top)
			throws IOException
	{
		super(points);
		this.bottom = bottom;
		this.top = top;

		if (bottom.getLayerIndex() > top.getLayerIndex())
			throw new IOException(
					"Layer index of bottom horizon is greater than layer index of top horizon");
	}

	/**
	 * Parse the records that define this Polygon3D.
	 * The first record must be the String "POLYGON3D".
	 * The records must contain two records that define the top and bottom
	 * of the polygon3d.  These records can appear anywhere in the list of records
	 * but must each contain 4 tokens:
	 * <ol start=0>
	 * <li>either 'TOP' or 'BOTTOM' (case sensitive)
	 * <li>either 'layer', 'depth' or 'radius' (case insensitive)
	 * <li>either fractional radius within a layer, depth in km or radius in km
	 * <li>layer index. If layerIndex is >= 0 and < the number of layers represented in a model,
	 * then the returned radius will be constrained to be between the top and bottom of the 
	 * specified layer.  Otherwise, the radius will not be so constrained. 
	 * </ol>
	 * <p>For example:
	 * <ul>
	 * <li>"TOP layer 0 4" will place the top of 
	 * the polygon at the bottom of layer 4.  
	 * <li>"BOTTOM radius 3000 -1" will place the bottom of the polyon at 
	 * radius = 3000 km and layer boundaries will be ignored.  
	 * <li>"TOP depth 50 7" will place the top of the polygon at a depth of 
	 * 50 km in layer 7.  Wherever the bottom of layer 7 is at a depth less than  
	 * 50 km, the top of the polygon will correspond to the bottom of layer 7. 
	 * Wherever the top of layer 7 is at a depth greater than 50 km, the 
	 * top of the polygon will correspond to the top of layer 7.
	 * </ul>
	 */
	@Override
	public void parseRecords(ArrayList<String> records) throws IOException
	{
		if (!records.get(0).toUpperCase().startsWith("POLYGON3D"))
			throw new IOException("\nPolygon3D files must start with 'POLYGON3D'\n");
		
		for (String record : records)
		{
			if (record.trim().startsWith("TOP"))
				top = parseHorizon(tokenize(record));
			else if (record.trim().startsWith("BOTTOM"))
				bottom = parseHorizon(tokenize(record));
			
			if (top != null && bottom != null)
				break;
		}
		
		if (top == null)
			throw new IOException(
					"\nDid not find a record defining the 'TOP' of the Polygon3D.\n\n");
		
		if (bottom == null)
			throw new IOException(
					"\nDid not find a record defining the 'BOTTOM' of the Polygon3D.\n\n");

		super.parseRecords(records);
	}

	public Polygon3D(File file, Horizon bottom, Horizon top)
			throws IOException
	{
		super(file);
		this.bottom = bottom;
		this.top = top;
	}

	public Polygon3D(double[] center, double radius, int nEdges,
			Horizon bottom, Horizon top) throws IOException
	{
		super(center, radius, nEdges);
		this.bottom = bottom;
		this.top = top;

		if (bottom.getLayerIndex() > top.getLayerIndex())
			throw new IOException(
					"Layer index of bottom horizon is greater than layer index of top horizon");
	}

	/**
	 * Expecting 4 tokens:
	 * <ol start=0>
	 * <li>either 'TOP' or 'BOTTOM' (case sensitive)
	 * <li>either 'layer', 'depth' or 'radius' (case insensitive)
	 * <li>either fractional radius within a layer, depth in km or radius in km
	 * <li>layer index. If layerIndex is >= 0 and < the number of layers represented in a model,
	 * then the returned radius will be constrained to be between the top and bottom of the 
	 * specified layer.  Otherwise, the radius will not be so constrained. 
	 * </ol>
	 * <p>For example:
	 * <ul>
	 * <li>"TOP layer 0 4" will place the top of 
	 * the polygon at the bottom of layer 4.  
	 * <li>"BOTTOM radius 3000 -1" will place the bottom of the polyon at 
	 * radius = 3000 km and layer boundaries will be ignored.  
	 * <li>"TOP depth 50 7" will place the top of the polygon at a depth of 
	 * 50 km in layer 7.  Wherever the bottom of layer 7 is at a depth less than  
	 * 50 km, the top of the polygon will correspond to the bottom of layer 7. 
	 * Wherever the top of layer 7 is at a depth greater than 50 km, the 
	 * top of the polygon will correspond to the top of layer 7.
	 * </ul>
	 * @param tokens must be of length 4.
	 * @return one of HorizonLayer, HorizonDepth or HorizonRadius
	 * @throws IOException
	 */
	private Horizon parseHorizon(String[] tokens) throws IOException
	{
		double x;
		int layer;

		try
		{
			x = Double.parseDouble(tokens[2]);
		}
		catch (java.lang.NumberFormatException ex)
		{
			throw new IOException(
					String.format(
							"\nError parsing '%s' record in POLYGON3D file.  Cannot parse token '%s' as double.\n\n",
							tokens[0], tokens[2]));
		}

		try
		{
			layer = Integer.parseInt(tokens[3]);
		}
		catch (java.lang.NumberFormatException ex)
		{
			throw new IOException(
					String.format(
							"\nError parsing '%s' record in POLYGON3D file.  Cannot parse token '%s' as int.\n\n",
							tokens[0], tokens[3]));
		}

		if (tokens[1].equalsIgnoreCase("layer"))
			return new HorizonLayer(x, layer);
		else if (tokens[1].equalsIgnoreCase("depth"))
			return  new HorizonDepth(x, layer);
		else if (tokens[1].equalsIgnoreCase("radius"))
			return new HorizonRadius(x, layer);
		
		throw new IOException(
				String.format(
						"\nError parsing '%s' record in polygon file.\n" +
						"token 1 is '%s' but must be one of [ layer | depth | radius ]\n\n",
						tokens[0], tokens[1]));
	}

	public Horizon getTop()
	{
		return top;
	}

	public Horizon getBottom()
	{
		return bottom;
	}

	public boolean contains(double[] x, double radius, int layer,
			double[] radii)
	{
		return (bottom.getLayerIndex() < 0 || layer >= bottom.getLayerIndex())
				&& (top.getLayerIndex() < 0 || layer <= top.getLayerIndex())
				&& radius > bottom.getRadius(x, radii) - 1e-4
				&& radius < top.getRadius(x, radii) + 1e-4 && contains(x);
	}

	public boolean contains(double[] x, int layer)
	{
		return (bottom.getLayerIndex() < 0 || layer >= bottom.getLayerIndex())
				&& (top.getLayerIndex() < 0 || layer <= top.getLayerIndex())
				&& contains(x);
	}
//	
//	public boolean contains(GeoTessPosition position) throws GeoTessException
//	{
//		HashMap<Integer, Double> weights = new HashMap<Integer, Double>();
//		position.getWeights(weights, 1.0);
//		return !weights.keySet().contains(-1);
//	}

	/**
	 * Returns true if this Polygon contains all of the supplied points
	 * 
	 * @param points array of unit vectors
	 * @param radii the radii of the positions of the points.  Array must have
	 * same number of elements as points.
	 * @param layers the layer index of each point.
	 * @param layerRadii the radii of the interfaces that define the layers 
	 * at each point.  This is an nPoints by nLayers+1 array, where nPoints
	 * is the number of points in the points array, and nLayers is the number
	 * of layers in the model.
	 * @return true if this Polygon contains all of the supplied points
	 * @throws PolygonException
	 */
	public boolean containsAll(ArrayList<double[]> points,
			ArrayList<Double> radii, ArrayList<Integer> layers,
			ArrayList<double[]> layerRadii)
	{
		for (int i = 0; i < points.size(); ++i)
			if (!contains(points.get(i), radii.get(i), layers.get(i),
					layerRadii.get(i)))
				return false;
		return true;
	}

	/**
	 * Returns true if this Polygon contains any of the supplied points
	 * 
	 * @param points array of unit vectors
	 * @param radii the radii of the positions of the points.  Array must have
	 * same number of elements as points.
	 * @param layers the layer index of each point.
	 * @param layerRadii the radii of the interfaces that define the layers 
	 * at each point.  This is an nPoints by nLayers+1 array, where nPoints
	 * is the number of points in the points array, and nLayers is the number
	 * of layers in the model.
	 * @return true if this Polygon contains any of the supplied points
	 * @throws PolygonException
	 */
	public boolean containsAny(ArrayList<double[]> points,
			ArrayList<Double> radii, ArrayList<Integer> layers,
			ArrayList<double[]> layerRadii)
	{
		for (int i = 0; i < points.size(); ++i)
			if (contains(points.get(i), radii.get(i), layers.get(i),
					layerRadii.get(i)))
				return true;
		return false;
	}

	/**
	 * Returns true if this Polygon contains all of the supplied points
	 * 
	 * @param points array of unit vectors
	 * @param radii the radii of the positions of the points.  Array must have
	 * same number of elements as points.
	 * @param layers the layer index of each point.
	 * @param layerRadii the radii of the interfaces that define the layers 
	 * at each point.  This is an nPoints by nLayers+1 array, where nPoints
	 * is the number of points in the points array, and nLayers is the number
	 * of layers in the model.
	 * @return true if this Polygon contains all of the supplied points
	 * @throws PolygonException
	 */
	public boolean containsAll(double[][] points, double[] radii, int[] layers,
			double[][] layerRadii)
	{
		for (int i = 0; i < points.length; ++i)
			if (!contains(points[i], radii[i], layers[i], layerRadii[i]))
				return false;
		return true;
	}

	/**
	 * Returns true if this Polygon contains any of the supplied points
	 * 
	 * @param points array of unit vectors
	 * @param radii the radii of the positions of the points.  Array must have
	 * same number of elements as points.
	 * @param layers the layer index of each point.
	 * @param layerRadii the radii of the interfaces that define the layers 
	 * at each point.  This is an nPoints by nLayers+1 array, where nPoints
	 * is the number of points in the points array, and nLayers is the number
	 * of layers in the model.
	 * @return true if this Polygon contains any of the supplied points
	 * @throws PolygonException
	 */
	public boolean containsAny(double[][] points, double[] radii, int[] layers,
			double[][] layerRadii)
	{
		for (int i = 0; i < points.length; ++i)
			if (contains(points[i], radii[i], layers[i], layerRadii[i]))
				return true;
		return false;
	}

	@Override
	public String toString()
	{
		StringBuffer buf = new StringBuffer("Polygon3D\n");
		buf.append("TOP ").append(top.toString()).append('\n');
		buf.append("BOTTOM ").append(bottom.toString()).append('\n');
		buf.append(super.toString(false, lonFirst, -180.));
		buf.append("\n");
		return buf.toString();
	}

	/**
	 * Write the current polygon to a file in ascii format. Polygon boundary points will
	 * be written in lat-lon order.
	 * 
	 * @param fileName
	 *            name of file to receive the polygon
	 * @throws IOException
	 */
	@Override
	public void write(File fileName) throws IOException
	{
		if (fileName.getName().endsWith("vtk"))
			vtk(fileName);
		else
		{
			BufferedWriter output = new BufferedWriter(new FileWriter(fileName));
			output.write("POLYGON3D\n");
			output.write("TOP " + top.toString());
			output.write("BOTTOM " + bottom.toString());
			write(output, "lat");
			output.close();
		}
	}

}
