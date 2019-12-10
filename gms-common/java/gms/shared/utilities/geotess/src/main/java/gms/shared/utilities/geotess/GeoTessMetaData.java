package gms.shared.utilities.geotess;

import gms.shared.utilities.geotess.util.containers.arraylist.ArrayListInt;
import gms.shared.utilities.geotess.util.globals.DataType;
import gms.shared.utilities.geotess.util.globals.OptimizationType;
import gms.shared.utilities.geotess.util.numerical.vector.EarthShape;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.Date;
import java.util.Scanner;

/**
 * GeoTessMetaData stores basic information about a GeoTessModel. Each
 * GeoTessModel has a single instance of MetaData that it passes around to
 * wherever the information is needed.
 * 
 * @author Sandy Ballard
 * 
 */
public class GeoTessMetaData
{
	/**
	 * A description of the contents of the model.
	 */
	private String description = "";

	/**
	 * The EarthShape used by the model that owns this meta data.
	 */
	private EarthShape earthShape = EarthShape.WGS84;

	/**
	 * Contains the model type name (.e.g. GeoTessModel);
	 */
	private String modelType = "";

	/**
	 * The names of each layer in the model, in order from the deepest to
	 * shallowest layer (increasing radius).
	 */
	protected String[] layerNames;

	/**
	 * An array of length nLayers where each element is the index of the
	 * tessellation that supports the corresponding layer. Tessellation indexes
	 * are managed by the grid object.
	 */
	protected int[] layerTessIds;

	/**
	 * It is assumed that every Data object attached to a node has the same
	 * size, i.e., number of attributes. The names and units of those attributes,
	 * associated with their storage index, are stored here.
	 */
	private AttributeDataDefinitions nodeAttributes = new AttributeDataDefinitions();
	
	/**
	 * Applications should obtain the value of nVertices from the GeoTessGrid
	 * object.  This copy is obtained from the input files during read operations
	 * before the grid is loaded. Only accessible via a protected accessor.
	 */
	private int nVertices;

	/**
	 * Execution can be optimized either for speed or memory. If optimization is
	 * set to SPEED, then the following optimization strategies will be
	 * implemented:
	 * <ul>
	 * <li>for each edge of a triangle the unit vector normal to the plane of
	 * the great circle containing the edge will be computed during input of the
	 * grid from file and stored in memory. With this information, the walking
	 * triangle algorithm can use dot products instead of scalar triple products
	 * when determining if a point resides inside a triangle. While much more
	 * computationally efficient, it requires a lot of memory to store all those
	 * unit vectors.
	 * <li>when performing natural neighbor interpolation, lazy evaluation will
	 * be used to store the circumcenters of triangles that are computed during
	 * interpolation.
	 * </ul>
	 */
	private OptimizationType optimization = OptimizationType.SPEED;

	/**
	 * Name of the file from which model was loaded, or "none".
	 */
	private File inputModelFile = null;

	/**
	 * Time, in seconds, required to read the model, or -1.
	 */
	private double loadTimeModel = -1;

	/**
	 * Name of file to which the model was written, or "none".
	 */
	private String outputModelFile;

	/**
	 * Time in second required to write the model to a file, or -1 if model has
	 * not been written to a file.
	 */
	private double writeTimeModel = -1;

	/**
	 * If true grid reuse is turned on for the model using this meta data.
	 */
	private boolean reuseGrids = true;

	/**
	 * Name and version number of the software that generated this model.
	 */
	private String modelSoftwareVersion;
	
	/**
	 * The date when this model was generated. Not necessarily the same
	 * as the date that the model file was copied or translated.
	 */
	private String modelGenerationDate = new Date().toString();
	
	/**
	 * The format version number of the input model file.
	 */
	private int inputModelFileFormat;
	
	/**
	 * When called upon to create GradientCalculator object used to compute
	 * attribute field gradients, this is the size of the small tetrahedron used
	 * by the GradientCalculator to estimate attribute gradients. Units are km.
	 */
	private double tetSize = 10;

	/**
	 * If true then model layer normals are evaluated using triangle area
	 * weighting applied to the facet normals shared by the vertex for which the
	 * layer normal is being calculated. If false (the default), unit weighting
	 * is applied to the shared facet normals.
	 */
	private boolean layerNormalAreaWeight = false;
	
	/**
	 * Default constructor.  
	 * 
	 * <p>During construction of a GeoTessModel object,
	 * the following methods should be called to make the MetaData object 
	 * complete.
	 * <ul>
	 * <li>setDescription()
	 * <li>setLayerNames()
	 * <li>setAttributes()
	 * <li>setDataType()
	 * <li>setLayerTessIds() (only required if grid has more than one
	 * multi-level tessellation)
	 * <li>setOptimization() (optional: defaults to SPEED)
	 * <li>setModelSoftwareVersion()
	 * <li>setModelGenerationDate()
	 * </ul>
	 */
	public GeoTessMetaData() {}

	/**
	 * Copy constructor.  Make deep copies of all the values
	 * in md.  Values are copied from md to this.
	 * @param md the other GeoTessMetaData object.
	 */
	public GeoTessMetaData(GeoTessMetaData md)
	{
		this.nodeAttributes = new AttributeDataDefinitions(md.nodeAttributes);
		this.description = md.description;
		this.inputModelFile = md.inputModelFile;
		this.layerNames = md.layerNames.clone();
		this.layerTessIds = md.layerTessIds.clone();
		this.loadTimeModel = md.loadTimeModel;
		this.modelGenerationDate = md.modelGenerationDate;
		this.modelSoftwareVersion = md.modelSoftwareVersion;
		this.optimization = md.optimization;
		this.outputModelFile = md.outputModelFile;
		this.reuseGrids = md.reuseGrids;
		this.writeTimeModel = md.writeTimeModel;
	}

	/**
	 * Sets the model type name.
	 * 
	 * @param modelTypeName The input model type name.
	 */
	public void setModelType(String modelTypeName)
	{
		modelType = modelTypeName;
	}

	/**
	 * Returns the model type name.
	 * 
	 * @return The model type name.
	 */
	public String getModelType()
	{
		return modelType;
	}

	/**
	 * Writes this GeoTessMetaData information to a binary file.
	 * The file is neither opened nor closed by this method.
	 * 
	 * @param output DataOutputStream
	 * @param nVertices The number of grid vertices stored by this model.
	 * @throws IOException
	 */
  protected void writeModelBinary(DataOutputStream output, int nVertices)
			      throws IOException
	{
		output.writeBytes("GEOTESSMODEL");

		output.writeInt(2); // dataFileFormat
		GeoTessUtils.writeString(output, getModelSoftwareVersion());
		GeoTessUtils.writeString(output, getModelGenerationDate());
		
		GeoTessUtils.writeString(output, getEarthShape().toString());

		GeoTessUtils.writeString(output, getDescription());

		getNodeAttributes().write(output);
		GeoTessUtils.writeString(output, getModelLayerString());

		if (getDataType() == DataType.CUSTOM)
			GeoTessUtils.writeString(output, getCustomDataType().getDataTypeString());
		else
		    GeoTessUtils.writeString(output, getDataType().toString());

		output.writeInt(nVertices);

		// tessellation ids
		for (int i = 0; i < getNLayers(); ++i)
			output.writeInt(getTessellation(i));
	}

	/**
	 * Writes this GeoTessMetaData information to an ASCII file.
	 * The file is neither opened nor closed by this method.
	 * 
	 * @param output    Writer
	 * @param nVertices The number of grid vertices stored by this model.
	 * @throws IOException
	 */
	protected void writeModelAscii(Writer output, int nVertices)
			           throws IOException
	{
		// write string that identifies this file as a GeoTessModel file.
		output.write("GEOTESSMODEL" + GeoTessUtils.NL);

		// write the dataFileFormat for the data part of the grid.
		output.write("2" + GeoTessUtils.NL);

		output.write(getModelSoftwareVersion() + GeoTessUtils.NL);
		output.write(getModelGenerationDate() + GeoTessUtils.NL);

		output.write(getEarthShape().toString() + GeoTessUtils.NL);

		output.write(String.format(
				"<model_description>%n%s%n</model_description>%n",
				getDescription()));

		getNodeAttributes().write(output);
		output.write(String.format(getModelLayerScannerHeader(),
				                       getModelLayerString()));

		if (getDataType() == DataType.CUSTOM)
			output.write(String.format("%s%n", getCustomDataType().getDataTypeString()));
		else
			output.write(String.format("%s%n", getDataType().toString()));

		output.write(String.format("%d%n", nVertices));

		for (int i = 0; i < getNLayers(); ++i)
			output.write(String.format(" %1d", getTessellation(i)));
		output.write(GeoTessUtils.NL);
	}

	/**
	 * Returns the model layer scanner header. The default is "layers: %s%n".
	 * Other meta data types may supply a different header if this method is
	 * overridden.
	 * 
	 * @return The model layer scanner header. The default is "layers: %s%n".
	 *         Other meta data types may supply a different header if this method
	 *         is overridden.
	 */
	protected String getModelLayerScannerHeader()
	{
		return "layers: %s%n";
	}

	/**
	 * Returns the model layer string. By default this contains the ";" separated
	 * string of layer names. Other model types may include additional or
	 * different information if the method is overridden.
	 * 
	 * @return The model layer string. By default this contains the ";" separated
	 *         string of layer names. Other model types may include additional or
	 *         different information if the method is overridden.
	 */
	protected String getModelLayerString()
	{
		return getLayerNamesString();
	}

	/**
	 * Loads GeoTessMetaData information from a binary file.
	 * The file is neither opened nor closed by this method.
	 * 
	 * @param input DataInputStream
	 * @throws IOException
	 */
	protected GeoTessMetaData load(DataInputStream input) throws IOException
	{
		byte[] bytes = new byte[12];
		input.read(bytes);
		String line = new String(bytes);
		if (!line.equals(("GEOTESSMODEL")))
			throw new IOException(
					"\nExpected file to start with GEOTESSMODEL but found\n"
							+ line + "\n"+inputModelFile.getCanonicalPath()+"\n");

		// get the fileFormatVersion. Only recognized value right now is 1.
		inputModelFileFormat = input.readInt();
		if (inputModelFileFormat < 1 || inputModelFileFormat > 2)
			throw new IOException(String.format("%nThis version of GeoTessJava (%s) "
					+ "cannot read GeoTess files written in file format %d. %n"
					+ "Please update GeoTessJava to the latest version%n",
					GeoTessUtils.getVersion(), inputModelFileFormat));

		setModelSoftwareVersion(GeoTessUtils.readString(input));
		setModelGenerationDate(GeoTessUtils.readString(input));
		
		if (inputModelFileFormat == 1)
			setEarthShape(EarthShape.WGS84);
		else 
			setEarthShape(EarthShape.valueOf(GeoTessUtils.readString(input)));

		setDescription(GeoTessUtils.readString(input));

		nodeAttributes = new AttributeDataDefinitions(input);

		setModelLayers(GeoTessUtils.readString(input).split(";"));

		setDataType(GeoTessUtils.readString(input));

		nVertices = input.readInt();

		// an array of length nLayers where each element is the
		// index of the tessellation that supports that layer.
		int[] tessellations = new int[getNLayers()];
		int nTess = 0;
		for (int i = 0; i < getNLayers(); ++i)
		{
			tessellations[i] = input.readInt();
			if (tessellations[i] > nTess)
				nTess = tessellations[i];
		}
		++nTess;
		setLayerTessIds(tessellations);
		return this;
	}

	/**
	 * Sets the model layers. This default simply calls the method setLayerNames.
	 * Other types of model may set additional information if this method is
	 * overridden.
	 * 
	 * @param layers The input layers string array.
	 * @throws IOException
	 */
	public void setModelLayers(String ... layers) throws IOException
	{
		setLayerNames(layers);
	}
	
	/**
	 * Retrieve a new GeoTessMetaData object that is a deep copy of the contents of this.
	 * @return a deep copy of this.
	 */
	public GeoTessMetaData copy()
	{
		return new GeoTessMetaData(this);
	}
	
	/**
	 * Loads GeoTessMetaData information from an ascii file.
	 * The file is neither opened nor closed by this method.
	 * 
	 * @param input Scanner
	 * @throws IOException
	 */
	protected GeoTessMetaData load(Scanner input) throws IOException
	{
		String line = input.nextLine();
		if (!line.equals(("GEOTESSMODEL")))
			throw new IOException(
					"\nExpected first line of file to be GEOTESSMODEL but found\n"
							+ line + "\n");

		// get the dataFileFormat. Only recognized value right now is 1.
		inputModelFileFormat = input.nextInt();
		if (inputModelFileFormat < 1 || inputModelFileFormat > 2)
			throw new IOException(String.format("%nThis version of GeoTessJava (%s) "
					+ "cannot read GeoTess files written in file format %d. %n"
					+ "Please update GeoTessJava to the latest version%n",
					GeoTessUtils.getVersion(), inputModelFileFormat));


		// read the end-of-line
		input.nextLine();

		// read software version and time stamp

		setModelSoftwareVersion(input.nextLine());
		setModelGenerationDate(input.nextLine());
		
		if (inputModelFileFormat == 1)
			setEarthShape(EarthShape.WGS84);
		else 
			setEarthShape(EarthShape.valueOf(input.nextLine()));

		line = input.nextLine();
		if (!line.equals("<model_description>"))
			throw new IOException(
					String.format(
							"Expected to read string 'model_description' but found '%s'",
							line));
		String description = "";
		line = input.nextLine();
		while (!line.equals("</model_description>"))
		{
			description += line + GeoTessUtils.NL;
			line = input.nextLine();
		}
		// strip off the last GeoTessUtils.NL
		description = description.substring(0, description.length()
				- GeoTessUtils.NL.length());

		setDescription(description);

		nodeAttributes = new AttributeDataDefinitions(input);

		setModelLayers(input);
		
		setDataType(input.nextLine());

		nVertices = input.nextInt();

		// an array of length nLayers where each element is the
		// index of the tessellation that supports that layer.
		int[] tessellations = new int[getNLayers()];
		int nTess = 0;
		for (int i = 0; i < getNLayers(); ++i)
		{
			tessellations[i] = input.nextInt();
			if (tessellations[i] > nTess)
				nTess = tessellations[i];
		}
		++nTess;
		setLayerTessIds(tessellations);

		input.nextLine();
		
		return this;
	}

	/**
	 * Sets the input model layer information from the input scanner. Other model
	 * types may set additional information if this default version is overridden.
	 * 
	 * @param input The input scanner from which the model layer information is
	 *              read.
	 * @throws IOException
	 */
	protected void setModelLayers(Scanner input) throws IOException
	{
		String layers = input.nextLine();
		if (!layers.startsWith("layers:"))
			throw new IOException(
					String.format(
							"Expected to read string starting with 'interfaces/layers:' but found '%s'",
							layers));

		setLayerNames(layers.substring(7).split(";"));
	}

	/**
	 * Read only the GeoTessMetaData from a file.
	 * 
	 * @param inputFile
	 *            name of file containing the model.
	 * @return a new GeoTessMetaData object.
	 * @throws IOException
	 */
	public static GeoTessMetaData getMetaData(String inputFile)
			throws GeoTessException, IOException
	{
		return getMetaData(new File(inputFile));
	}

	/**
	 * Read only the GeoTessMetaData from a file.
	 * 
	 * @param inputFile
	 *            name of file containing the model.
	 * @return a new GeoTessMetaData object.
	 * @throws IOException
	 */
	public static GeoTessMetaData getMetaData(File inputFile)
			throws IOException
	{
		GeoTessMetaData metaData = null;

		if (inputFile.getName().endsWith(".ascii"))
		{
			Scanner input = new Scanner(inputFile);
			metaData = new GeoTessMetaData().load(input);
			input.close();
		}
		else
		{
			DataInputStream input = new DataInputStream(new BufferedInputStream(
					new FileInputStream(inputFile)));
			metaData = new GeoTessMetaData().load(input);
			input.close();
		}

		metaData.setInputModelFile(inputFile);

		return metaData;

	}

	/**
	 * Check to ensure that this MetaData object contains all the information
	 * needed to construct a new GeoTessModel. To be complete, the following
	 * information is required:
	 * <ul>
	 * <li>description
	 * <li>layer names
	 * <li>data type
	 * <li>attribute names and
	 * <li>attribute units.
	 * <li>model-population software name and version number
	 * <li>model generation date
	 * <li>if the model grid has more than one tessellation, then layerTessIds
	 * must also be specified.
	 * </ul>
	 * 
	 * @throws GeoTessException
	 *             if incomplete.
	 */
	public void checkComplete() throws GeoTessException
	{
		StringBuffer buf = new StringBuffer();
		if (description == null)
			buf.append(GeoTessUtils.NL).append(
					"description has not been specified.");

		if (layerNames == null)
			buf.append(GeoTessUtils.NL).append(
					"layerNames has not been specified.");
		else if (layerTessIds == null)
			buf.append(GeoTessUtils.NL).append(
					"layerTessIds has not been specified.");
		else if (layerTessIds.length != layerNames.length)
			buf.append(GeoTessUtils.NL).append(
					"layerTessIds.length != layerNames.length");

 	  nodeAttributes.checkComplete(buf);

		if (modelSoftwareVersion == null)
			buf.append(GeoTessUtils.NL).append(
					"modelSoftwareVersion has not been specified.");

		if (modelGenerationDate == null)
			buf.append(GeoTessUtils.NL).append(
					"modelGenerationDate has not been specified.");

		if (buf.length() > 0)
			throw new GeoTessException("MetaData is not complete."
					+ buf.toString());

	}

	/**
	 * Returns the models attribute name and unit specification defined by this
	 * model.
	 * 
	 * @return The models attribute name and unit specification defined by this
	 *         model.
	 */
	public AttributeDataDefinitions getNodeAttributes()
	{
		return nodeAttributes;
	}

	/**
	 * Return true if this and other have the same number of layers, the same
	 * number of attributes and all their layerTessIds are equal.
	 * 
	 * @param other
	 * 
	 * @return true if this and other have the same number of layers, the same
	 *         number of attributes and all their layerTessIds are equal.
	 */
	@Override
	public boolean equals(Object other)
	{
		if (other == null || !(other instanceof GeoTessMetaData))
			return false;
		
		if (this.getNLayers() != ((GeoTessMetaData)other).getNLayers()
				|| this.getNAttributes() != ((GeoTessMetaData)other).getNAttributes())
			return false;

		for (int i = 0; i < getNLayers(); ++i)
			if (layerTessIds[i] != ((GeoTessMetaData)other).getLayerTessIds()[i])
				return false;
		return true;
	}

	/**
	 * Retrieve the name of the file from which the model was loaded, or "none".
	 * 
	 * @return the name of the file from which the model was loaded, or "none".
	 */
	public File getInputModelFile()
	{
		return inputModelFile;
	}

	/**
	 * Specify the name of the file from which the model was loaded.
	 * 
	 * @param inputModelFile
	 *            the name of the file from which the model was loaded.
	 */
	public void setInputModelFile(File inputModelFile)
	{
		this.inputModelFile = inputModelFile;
	}

	/**
	 * Retrieve the amount of time, in seconds, required to load the model, or
	 * -1.
	 * 
	 * @return the amount of time, in seconds, required to load the model, or
	 *         -1.
	 */
	public double getLoadTimeModel()
	{
		return loadTimeModel;
	}

	/**
	 * Set the amount of time, in seconds, required to load the model.
	 * 
	 * @param loadTimeModel
	 *            the amount of time, in seconds, required to load the model.
	 */
	protected void setLoadTimeModel(double loadTimeModel)
	{
		this.loadTimeModel = loadTimeModel;
	}

	/**
	 * Retrieve the name of the file to which the model was most recently
	 * written, or "none".
	 * 
	 * @return the name of the file to which the model was most recently
	 *         written, or "none".
	 */
	public String getOutputModelFile()
	{
		return outputModelFile;
	}

	/**
	 * Set the name of the file to which the model has been written
	 * 
	 * @param outputModelFile
	 *            the name of the file to which the model has been written
	 */
	protected void setOutputModelFile(String outputModelFile)
	{
		this.outputModelFile = outputModelFile;
	}

	/**
	 * Retrieve the amount of time, in seconds, required to write the model to
	 * file, or -1.
	 * 
	 * @return the amount of time, in seconds, required to write the model to
	 *         file, or -1.
	 */
	public double getWriteTimeModel()
	{
		return writeTimeModel;
	}

	/**
	 * Specify the amount of time, in seconds, required to write the model to
	 * file.
	 * 
	 * @param writeTimeModel
	 *            the amount of time, in seconds, required to write the model to
	 *            file.
	 */
	protected void setWriteTimeModel(double writeTimeModel)
	{
		this.writeTimeModel = writeTimeModel;
	}

	/**
	 * Set the attribute names and units. The lengths of the two String[] must
	 * be equal.
	 * 
	 * @param names
	 * @param units
	 * @throws GeoTessException
	 */
	public void setAttributes(String[] names, String[] units)
	{
		nodeAttributes.setAttributes(names, units);
	}

	/**
	 * Set the attribute names and units. After parsing, the number of names and
	 * units must be equal.
	 * 
	 * @param names
	 *            a single string with attribute names separated by ';'
	 * @param units
	 *            a single string with attribute units separated by ';'
	 * @throws GeoTessException
	 */
	public void setAttributes(String names, String units)
	{
		nodeAttributes.setAttributes(names, units);
	}

	/**
	 * Retrieve a string containing all the attribute names separated by ';'
	 * 
	 * @return a string containing all the attribute names separated by ';'
	 */
	public String getAttributeNamesString()
	{
		return nodeAttributes.getAttributeNamesString();
	}

	/**
	 * Retrieve a string containing all the attribute units separated by ';'
	 * 
	 * @return a string containing all the attribute units separated by ';'
	 */
	public String getAttributeUnitsString()
	{
		return nodeAttributes.getAttributeUnitsString();
	}

	/**
	 * Retrieve the layer names in a single, semicolon delimited string.
	 * 
	 * @return the layer names in a single, semicolon delimited string.
	 */
	public String getLayerNamesString()
	{
		String s = layerNames[0];
		for (int i = 1; i < layerNames.length; ++i)
			s += ";" + layerNames[i];
		return s;
	}

	/**
	 * Retrieve the number of attributes supported by the model.
	 * 
	 * @return the number of attributes supported by the model.
	 */
	public int getNAttributes()
	{
		return nodeAttributes.getNAttributes();
	}

	/**
	 * Retrieve a copy of the array containing the names of the attributes
	 * supported by the model.
	 * 
	 * @return a copy of the array containing the names of the attributes
	 *         supported by the model.
	 */
	public String[] getAttributeNames()
	{
		return nodeAttributes.getAttributeNames().clone();
	}

	/**
	 * For every attribute supported by this model, add attribute
	 * to the supplied collection of attributes.
	 * The supplied collection of attributes is not cleared before
	 * the addition.
	 * 
	 * @return a reference to the supplied collection of attributes.
	 */
	public Collection<String> getAttributeNames(Collection<String> attributes)
	{
		return nodeAttributes.getAttributeNames(attributes);
	}

	/**
	 * Retrieve the index of the specified attribute name, or -1 if the
	 * specified attribute does not exist. Case sensitive.
	 * 
	 * @param name
	 * @return the index of the specified attribute name, or -1 if the specified
	 *         attribute does not exist.
	 */
	public int getAttributeIndex(String name)
	{
		return nodeAttributes.getAttributeIndex(name);
	}

	/**
	 * Retrieve a copy of the array containing the units of the attributes
	 * supported by the model.
	 * 
	 * @return a copy of the array containing the units of the attributes
	 *         supported by the model.
	 */
	public String[] getAttributeUnits()
	{
		return nodeAttributes.getAttributeUnits().clone();
	}

	/**
	 * Retrieve the name of the i'th attribute supported by the model.
	 * 
	 * @param i
	 * @return the name of the i'th attribute supported by the model.
	 */
	public String getAttributeName(int i)
	{
		return nodeAttributes.getAttributeName(i);
	}

	/**
	 * Retrieve the units of the i'th attribute supported by the model.
	 * 
	 * @param i
	 * @return the units of the i'th attribute supported by the model.
	 */
	public String getAttributeUnit(int i)
	{
		return nodeAttributes.getAttributeUnit(i);
	}

	/**
	 * Retrieve the description of the model.
	 * 
	 * @return the description of the model.
	 */
	public String getDescription()
	{
		return description;
	}

	/**
	 * Set the description of the model.
	 * 
	 * @param description
	 *            the description of the model.
	 */
	public void setDescription(String description)
	{
		this.description = description;
	}

	/**
	 * Retrieve the number of layers represented in the model.
	 * 
	 * @return number of layers represented in the model.
	 */
	public int getNLayers()
	{
		return layerNames == null ? 0 : layerNames.length;
	}

	/**
	 * Retrieve the name of the layer with specified index.
	 * 
	 * @param layer index of layer
	 * @return the name of the layer with specified index.
	 */
	public String getLayerName(int layer)
	{
		return layerNames[layer];
	}

	/**
	 * Retrieve a reference to the array of names of the layers supported by the
	 * model.
	 * 
	 * @return a reference to the array of names of the layers supported by the
	 *         model.
	 */
	public String[] getLayerNames()
	{
		return layerNames;
	}

	/**
	 * Specify the names of the layers supported by the model.
	 * 
	 * @param layerNames
	 *            the names of the layers separated by a ;.
	 * @throws IOException 
	 */
	public void setLayerNames(String layerNames) throws IOException
	{
		setLayerNames(layerNames.split(";"));
	}

	/**
	 * Specify the names of the layers supported by the model.
	 * 
	 * @param layerNames
	 *            the names of the layers supported by the model.
	 */
	public void setLayerNames(String... layerNames) throws IOException
	{
		this.layerNames = layerNames;
		for (int i = 0; i < layerNames.length; ++i)
			layerNames[i] = layerNames[i].trim();

		// if layertessids have not been assigned then set
		// a default value where it is assumed that there
		// will only be one multi-level tessellation in the
		// model. Associate all layers with tessellation 0.
		if (layerTessIds == null)
			layerTessIds = new int[layerNames.length];
	}
	
	/**
	 * Retrieve the index of the layer that has the specified name, or -1. Case
	 * sensitive.
	 * 
	 * @param layerName
	 *            name of layer for which to search
	 * @return index of layer, or -1.
	 */
	public int getLayerIndex(String layerName)
	{
		for (int i = 0; i < layerNames.length; ++i)
			if (layerNames[i].equals(layerName))
				return i;
		return -1;
	}

	/**
	 * Retrieve a reference to layerTessIds; an int[] with an entry for each
	 * layer specifying the index of the tessellation that supports that layer.
	 * 
	 * @return a reference to layerTessIds
	 */
	public int[] getLayerTessIds()
	{
		return layerTessIds;
	}
	
	/**
	 * Set layerTessIds; an int[] with an entry for each layer specifying the
	 * index of the tessellation that supports that layer. For models that
	 * require only a single multi-level tessellation, this should be set to a
	 * an int array of length nLayers populated with all zeroes.
	 * 
	 * @param layerTessIds
	 *            the layerTessIds to set
	 * @throws IOException
	 *             if layerTessIds.length != layerNames.length
	 */
	public void setLayerTessIds(int[] layerTessIds) throws IOException
	{
		if (layerNames != null && layerTessIds.length != layerNames.length)
			throw new IOException(String.format(
					"%nN layerTessIds(%d) != N layerNames(%d)%n",
					layerTessIds.length, layerNames.length));

		this.layerTessIds = layerTessIds.clone();
	}

	/**
	 * Retrieve a list of all the layer indexes that are associated
	 * with a specific tessellation index.
	 * @param tessId tessellation index
	 * @return a list of all the layer indexes that are associated
	 * with a specific tessellation index.
	 */
	public int[] getLayers(int tessId)
	{
		ArrayListInt layids = new ArrayListInt();
		for (int i=0; i<layerTessIds.length; ++i)
			if (layerTessIds[i] == tessId)
				layids.add(i);
		return layids.toArray();
	}

	/**
	 * Retrieve the index of the last layer that is associated
	 * with a specific tessellation index.
	 * @param tessId tessellation index
	 * @return the index of the last layer that is associated
	 * with a specific tessellation index.
	 */
	public int getLastLayer(int tessId)
	{
		for (int i=layerTessIds.length-1; i >= 0; --i)
			if (layerTessIds[i] == tessId)
				return i;
		return -1;
	}

	/**
	 * Retrieve the index of the first layer that is associated
	 * with a specific tessellation index.
	 * @param tessId tessellation index
	 * @return the index of the first layer that is associated
	 * with a specific tessellation index.
	 */
	public int getFirstLayer(int tessId)
	{
		for (int i=0; i<layerTessIds.length; ++i)
			if (layerTessIds[i] == tessId)
				return i;
		return -1;
	}

	/**
	 * Retrieve the index of the tessellation that supports the specified layer.
	 * 
	 * @param layer
	 * @return the index of the tessellation that supports the specified layer.
	 */
	public int getTessellation(int layer)
	{
		return getLayerTessIds()[layer];
	}

	/**
	 * Return the type of all the data stored in the model; Will be one of
	 * DOUBLE, FLOAT, LONG, INT, SHORTINT, BYTE.
	 * 
	 * @return the dataType
	 */
	public DataType getDataType()
	{
		return nodeAttributes.getDataType();
	}

	/**
	 * Specify the type of the data that is stored in the model.
	 * <ul>
	 * One of
	 * <li>DataType.DOUBLE
	 * <li>DataType.FLOAT
	 * <li>DataType.LONG
	 * <li>DataType.INT
	 * <li>DataType.SHORT
	 * <li>DataType.BYTE
	 * </ul>
	 * 
	 * @param dataType
	 *            the dataType to set
	 */
	public void setDataType(DataType dataType)
	{
		nodeAttributes.setDataType(dataType);
	}

	/**
	 * Specify the type of the data that is stored in the model; Must be one of
	 * DOUBLE, FLOAT, LONG, INT, SHORT, BYTE.
	 * 
	 * @param dataType
	 *            the dataType to set
	 * @throws Exception 
	 */
	public void setDataType(String dataType) throws IOException
	{
		nodeAttributes.setDataType(dataType);
	}

	/**
	 * Adds a new custom data type.
	 * 
	 * @param dc The new custom data type to be added to the map of available
	 *           custom data types.
	 */
	public static void addCustomDataType(DataCustom dc)
	{
		AttributeDataDefinitions.addCustomDataType(dc);
	}

	/**
	 * Removes the specified custom data type.
	 * 
	 * @param dataType The custom data type to be removed.
	 */
	public static void removeCustomDataType(String dataType)
	{
		AttributeDataDefinitions.removeCustomDataType(dataType);
	}

	/**
	 * Clears all custom data type settings.
	 */
	public static void clearCustomDataTypes()
	{
		AttributeDataDefinitions.clearCustomDataTypes();
	}

	/**
	 * Returns the current custom data type if set (null otherwise).
	 * 
	 * @return The current custom data type if set (null otherwise).
	 */
	public DataCustom getCustomDataType()
	{
		return nodeAttributes.getCustomDataType();
	}

	/**
	 * Execution can be optimized either for speed or memory. If optimization is
	 * set to SPEED, then the following optimization strategies will be
	 * implemented:
	 * <ul>
	 * <li>for each edge of a triangle the unit vector normal to the plane of
	 * the great circle containing the edge will be computed during input of the
	 * grid from file and stored in memory. With this information, the walking
	 * triangle algorithm can use dot products instead of scalar triple products
	 * when determining if a point resides inside a triangle. While much more
	 * computationally efficient, it requires a lot of memory to store all those
	 * unit vectors.
	 * <li>when performing natural neighbor interpolation, lazy evaluation will
	 * be used to store the circumcenters of triangles that are computed during
	 * interpolation.
	 * <li>when interpolating along radial profiles, every profile will record
	 * the index of the radius that is discovered. That index will be the
	 * starting point for the binary search the next time binary search is
	 * implemented. Each GeoTessPosition object will store 2d array of shorts,
	 * short[nVertices][nlayers] to record this information. Might be ~1MB per
	 * GeoTessPosition object (they could share references to the same short[][]
	 * as long as they don't break concurrency.
	 * </ul>
	 * 
	 * @return the optimization
	 */
	public OptimizationType getOptimization()
	{
		return optimization;
	}

	/**
	 * Execution can be optimized either for speed or memory. If optimization is
	 * set to SPEED, then the following optimization strategies will be
	 * implemented:
	 * <ul>
	 * <li>for each edge of a triangle the unit vector normal to the plane of
	 * the great circle containing the edge will be computed during input of the
	 * grid from file and stored in memory. With this information, the walking
	 * triangle algorithm can use dot products instead of scalar triple products
	 * when determining if a point resides inside a triangle. While much more
	 * computationally efficient, it requires a lot of memory to store all those
	 * unit vectors.
	 * <li>when performing natural neighbor interpolation, lazy evaluation will
	 * be used to store the circumcenters of triangles that are computed during
	 * interpolation.
	 * <li>when interpolating along radial profiles, every profile will record
	 * the index of the radius that is discovered. That index will be the
	 * starting point for the binary search the next time binary search is
	 * implemented. Each GeoTessPosition object will store 2d array of shorts,
	 * short[nVertices][nlayers] to record this information. Might be ~1MB per
	 * GeoTessPosition object (they could share references to the same short[][]
	 * as long as they don't break concurrency.
	 * </ul>
	 * <p>If supplied value is null optimization is set to SPEED.
	 * 
	 * @param optimization
	 *            either Optimization.SPEED or Optimization.MEMORY
	 */
	public void setOptimization(OptimizationType optimization)
	{
		if (optimization == null)
			this.optimization = OptimizationType.SPEED;
		else
			this.optimization = optimization;
	}

	/**
	 * Standard toString method.
	 */
	@Override
	public String toString()
	{
		StringBuffer buf = new StringBuffer();
		
		// output the model type and version string.

		if (modelType.equals(""))
  		buf.append(this.getClass().getSimpleName() + ".").append(GeoTessUtils.getVersion()).append(GeoTessUtils.NL);
		else
  		buf.append(modelType + ".").append(GeoTessUtils.getVersion()).append(GeoTessUtils.NL);

		// Output the OS name.

		buf.append(String.format("OS: %s%n", GeoTessUtils.getOS()));
		if (nodeAttributes.getDataType() == null)
			return buf.toString();

		//buf.append(String.format("Optimized for %s%n%n", getOptimization().toString().toLowerCase()));
		
		buf.append(String.format("Input Model File : %s%n", 
				getInputModelFile() == null ? "null" : getInputModelFile().getAbsolutePath()));
								
		buf.append(String.format("generated by %s, %s%n", 
				getModelSoftwareVersion() == null ? "null" : getModelSoftwareVersion(),
								getModelGenerationDate() == null ? "null" : getModelGenerationDate()));
								
		buf.append(String.format("Model load time : %1.3f seconds%n", getLoadTimeModel()));
		
		if (outputModelFile != null)
		{
			buf.append(GeoTessUtils.NL);
			buf.append("Output Model File: ").append(outputModelFile)
					.append(GeoTessUtils.NL);
			buf.append(String.format("Model write time: %1.3f seconds%n",
					getWriteTimeModel()));
		}
		
		buf.append("Model Description:").append(GeoTessUtils.NL);
		buf.append(description).append(GeoTessUtils.NL);
		buf.append("<end description>\n\n");
		
		buf.append("EarthShape: ").append(getEarthShape().toString()).append(GeoTessUtils.NL);	
		
		buf.append(nodeAttributes.toString());
		toStringLayerNames(buf);

		return buf.toString();
	}

	/**
	 * Outputs the layer name information to the input string buffer. Different
	 * model types may output different information if this method is overridden.
	 * 
	 * @param buf The input string buffer into which the layer information is
	 *            written.
	 */
	protected void toStringLayerNames(StringBuffer buf)
	{
		buf.append("Layers:").append(GeoTessUtils.NL);
		buf.append("  Index  TessId   Name").append(GeoTessUtils.NL);
		for (int i = layerNames.length - 1; i >= 0; --i)
			buf.append(String.format("  %3d  %6d     %-1s%n", i,
					layerTessIds[i], layerNames[i]));
		buf.append(GeoTessUtils.NL);
	}

	/**
	 * Return true if reuseGrids is turned on. GeoTessModel maintains a map from
	 * gridID -> GeoTessGrid object. If reuseGrids is on, then every time a
	 * model is loaded, the map is checked to see if that grid has been loaded
	 * already. If so, the new model gets a reference to the existing grid
	 * instead of loading and instantiating a new grid.
	 * <p>
	 * By default, reuseGrids it true.
	 * 
	 * @return true if reuseGrids is turned on.
	 */
	public boolean isGridReuseOn()
	{
		return reuseGrids;
	}

	/**
	 * GeoTessModel maintains a map from gridID -> GeoTessGrid object. If
	 * reuseGrids is on, then every time a model is loaded, the map is checked
	 * to see if that grid has been loaded already. If so, the new model gets a
	 * reference to the existing grid instead of loading and instantiating a new
	 * grid.
	 * <p>
	 * By default, reuseGrids it true.
	 * 
	 * @param reuseGrids
	 *            specify true or false to turn reuseGrids on or off.
	 */
	public void setReuseGrids(boolean reuseGrids)
	{
		this.reuseGrids = reuseGrids;
	}

	/**
	 * Retrieve the name and version number of the software that generated
	 * the contents of this model.
	 * @return the name and version number of the software that generated
	 * the contents of this model.
	 */
	public String getModelSoftwareVersion()
	{
		return modelSoftwareVersion;
	}

	/**
	 * Set the name and version number of the software that generated
	 * the contents of this model.
	 * @param modelSoftwareVersion
	 */
	public void setModelSoftwareVersion(String modelSoftwareVersion)
	{
		this.modelSoftwareVersion = modelSoftwareVersion;
	}

	/**
	 * Retrieve the date when the contents of this model was generated.
	 * This is not necessarily the same as the date when the file was
	 * copied or translated.
	 * @return the date when the contents of this model was generated.
	 */
	public String getModelGenerationDate()
	{
		return modelGenerationDate;
	}

	/**
	 * Set the date when this model was generated.  
	 * This is not necessarily the same as the date when the file was
	 * copied or translated.
	 * @param modelGenerationDate
	 */
	public void setModelGenerationDate(String modelGenerationDate)
	{
		this.modelGenerationDate = modelGenerationDate;
	}
	
	/**
	 * This is protected because applications should obtain the number of 
	 * vertices from the GeoTessGrid object. 
	 * @return number of vertices in the 2D grid.
	 */
	protected int getNVertices()
	{
		return nVertices;
	}
	
	/**
	 * Retrieve the format version retrieved from the input 
	 * model file.
	 * @return  the format version retrieved from the input 
	 * model file.
	 */
	protected int getInputModelFileFormat()
	{
		return inputModelFileFormat;
	}

	/**
	 *  Retrieve a reference to the ellipsoid that is currently being used to convert between geocentric and
	 *  geographic latitude and between depth and radius.  The following EarthShapes are supported:
	 * <ul>
	 * <li>SPHERE - Geocentric and geographic latitudes are identical and
	 * conversion between depth and radius assume the Earth is a sphere
	 * with constant radius of 6371 km.
	 * <li>GRS80 - Conversion between geographic and geocentric latitudes, and between depth
	 * and radius are performed using the parameters of the GRS80 ellipsoid.
	 * <li>GRS80_RCONST - Conversion between geographic and geocentric latitudes are performed using
	 * the parameters of the GRS80 ellipsoid.  Conversions between depth and radius
	 * assume the Earth is a sphere with radius 6371.
	 * <li>WGS84 - Conversion between geographic and geocentric latitudes, and between depth
	 * and radius are performed using the parameters of the WGS84 ellipsoid.
	 * <li>WGS84_RCONST - Conversion between geographic and geocentric latitudes are performed using
	 * the parameters of the WGS84 ellipsoid.  Conversions between depth and radius
	 * assume the Earth is a sphere with radius 6371.
	 * <li>IERS2003 - Conversion between geographic and geocentric latitudes, and between depth
	 * and radius are performed using the parameters of the IERS2003 ellipsoid.
	 * <li>IERS2003_RCONST - Conversion between geographic and geocentric latitudes are performed using
	 * the parameters of the IERS2003 ellipsoid.  Conversions between depth and radius
	 * assume the Earth is a sphere with radius 6371.
	 * </ul>
	 * @return a reference to the EarthShape currently in use.
	 */
	public EarthShape getEarthShape()
	{
		return earthShape;
	}

	/**
	 *  Set the EarthShape object that is to be used to convert between geocentric and
	 *  geographic latitude and between depth and radius.  The following EarthShapes are supported:
	 * <ul>
	 * <li>SPHERE - Geocentric and geographic latitudes are identical and
	 * conversion between depth and radius assume the Earth is a sphere
	 * with constant radius of 6371 km.
	 * <li>GRS80 - Conversion between geographic and geocentric latitudes, and between depth
	 * and radius are performed using the parameters of the GRS80 ellipsoid.
	 * <li>GRS80_RCONST - Conversion between geographic and geocentric latitudes are performed using
	 * the parameters of the GRS80 ellipsoid.  Conversions between depth and radius
	 * assume the Earth is a sphere with radius 6371.
	 * <li>WGS84 - Conversion between geographic and geocentric latitudes, and between depth
	 * and radius are performed using the parameters of the WGS84 ellipsoid.
	 * <li>WGS84_RCONST - Conversion between geographic and geocentric latitudes are performed using
	 * the parameters of the WGS84 ellipsoid.  Conversions between depth and radius
	 * assume the Earth is a sphere with radius 6371.
	 * <li>IERS2003 - Conversion between geographic and geocentric latitudes, and between depth
	 * and radius are performed using the parameters of the IERS2003 ellipsoid.
	 * <li>IERS2003_RCONST - Conversion between geographic and geocentric latitudes are performed using
	 * the parameters of the IERS2003 ellipsoid.  Conversions between depth and radius
	 * assume the Earth is a sphere with radius 6371.
	 * </ul>
	 * @param a reference to the EarthShape that is to be used
	 */
	public void setEarthShape(EarthShape earthShape)
	{
		this.earthShape = earthShape;
	}

	/**
	 * When called upon to create a GradientCalculator object used to compute
	 * attribute field gradients, this is the size of the small tetrahedron used
	 * by the GradientCalculator to estimate attribute gradients. Units are km.
	 * 
	 * @return The size of the GradientCalculator small tetrahedron.
	 */
	public double getGradientCalculatorTetSize()
	{
		return tetSize;
	}

	/**
	 * When called upon to create a GradientCalculator object used to compute
	 * attribute field gradients, this value will be the size of the small
	 * tetrahedron used by the GradientCalculator to estimate the gradients.
	 * Units are km.
	 * 
	 * @param tetSize The size of the GradientCalculator small tetrahedron.
	 */
	public void setGradientCalculatorTetSize(double tetSize)
	{
		this.tetSize = tetSize;
	}

	/**
	 * Returns true if layer normals are to be evaluated using facet area weighting
	 * for the facets that share a vertex at a layer boundary for which the layer
	 * normal is to be evaluated. If false, the facet normals contributing to the
	 * layer normal are averaged directly without weights.
	 * 
	 * @return True if layer normal area weighting is on.
	 */
	public boolean useLayerNormalAreaWeight()
	{
		return layerNormalAreaWeight;
	}

	/**
	 * Sets the layer normal area weight flag. True implies adjacent facet normals
	 * shared by a common vertex for which the layer normal is to be evaluated
	 * are weighted by the facet area before summing and normalizing to produce
	 * the layer normal at the vertex.
	 * 
	 * @param lnaw The new setting of the layer normal area weight flag.
	 */
	public void setLayerNormalAreaWeight(boolean lnaw)
	{
		layerNormalAreaWeight = lnaw;
	}

  /**
   * Retrieve the index of the interface with the specified name.
   * If majorInterfaces can be parsed to an integer, then that value is
   * returned.  If not, then the index of the interface with the specified
   * name is returned.
   * <p>If more than one name is supplied, the first one that can be successfully
   * interpreted as either an integer index or a valid interface name is
   * returned.
   * <p>The ability to supply alternative names is useful.  For example, some
   * models call the top of the crust "CRUST" while in other models the
   * top of the crust is called "UPPER_CRUST".  If both are requested using
   * this method, the correct index will be returned.
   * @param majorInterfaces String
   * @return int
   */
  public int getInterfaceIndex(String ...majorInterfaces)
  {
    Integer index = null;
    for (String majorInterface : majorInterfaces)
    {
      try
      {
        // if layer can be parsed to an integer, interpret it to be major layer index.
        index = Integer.parseInt(majorInterface.trim());
      }
      catch (NumberFormatException ex)
      {
        index = getLayerIndex(majorInterface.trim());
      }
      if (index != -1) return index;
    }
    return -1;
  }
}
