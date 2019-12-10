package gms.shared.utilities.geotess.extensions.earthinterface;

import static gms.shared.utilities.geotess.util.globals.Globals.NL;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import gms.shared.utilities.geotess.GeoTessException;
import gms.shared.utilities.geotess.GeoTessMetaData;
import gms.shared.utilities.geotess.GeoTessUtils;

/**
 * An extension of the base class GeoTessMetaData object that contains an
 * additional container that holds the EarthInterface layer interfaces used by
 * any model referencing this meta data object. This derived type forces all
 * layer interface names to be precisely named and ordered according to the
 * EarthInteface enum specification.
 *  
 * @author jrhipp
 *
 */
public class GeoTessMetaDataEarthInterface extends GeoTessMetaData
{
	/**
	 * The Earth interface boundaries utilized in the model, in order from the
	 * deepest to the shallowest interface (increasing radius). These can only
	 * be set by means of a parsed string (see setEarthInterfaceLayers(...)).
	 * The interface names must match an interface name in the EarthInterface
	 * enum. Their order must also be maintained such that given any three
	 * consecutive interface names (i-1, i, i+1) their enum ordinal must be such
	 * that
	 * 
	 *    interfaceName[i-1].ordinal() < interfaceName[i].ordinal(), and
	 *    interfaceName[i].ordinal()   < interfaceName[i+1].ordinal()
	 */
	private EarthInterface[] layerInterfaces;

	/**
	 * If true then old GeoTess layer names are converted to the new
	 * EarthInterface names if possible. If the old layer name is not defined in
	 * the map provided in EarthInterface then an error is thrown. If it is
	 * defined in the the EarthInterface repair map then the old name is
	 * converted to the new name. This is a private member with no associated
	 * method to change it's value. It is only set to true in the the method
	 * setEarthInterfaceLayers(String... interfaceLayerNames) if the method fails
	 * to read the input string array properly. If it does fail then this flag
	 * is set to true and the method is recalled to attempt any conversion. If it
	 * fails again the error is thrown.
	 */
	private boolean repairOldLayerInterfaceNames = false;

	/**
	 * Default constructor.
	 */
	public GeoTessMetaDataEarthInterface()
	{
		super();
	}

	/**
	 * Copy constructor.  Make deep copies of all the values
	 * in md.  Values are copied from md to this.
	 * @param md the other GeoTessMetaData object.
	 */
	public GeoTessMetaDataEarthInterface(GeoTessMetaDataEarthInterface md)
	{
		super(md);
		this.layerInterfaces = md.layerInterfaces.clone();
	}
	
	/**
	 * Retrieve a new GeoTessMetaData object that is a deep copy of the contents of this.
	 * @return a deep copy of this.
	 */
	@Override
	public GeoTessMetaData copy()
	{
		return new GeoTessMetaDataEarthInterface(this);
	}

	/**
	 * Read only the GeoTessMetaDataEarthInterface from a file.
	 * 
	 * @param inputFile
	 *            name of file containing the model.
	 * @return a new GeoTessMetaDataEarthInterface object.
	 * @throws IOException
	 */
	public static GeoTessMetaDataEarthInterface getMetaData(String inputFile)
			throws GeoTessException, IOException
	{
		return getMetaData(new File(inputFile));
	}

	/**
	 * Read only the GeoTessMetaDataEarthInterface from a file.
	 * 
	 * @param inputFile
	 *            name of file containing the metadata.
	 * @return a new GeoTessMetaDataEarthInterface object.
	 * @throws IOException
	 */
	public static GeoTessMetaDataEarthInterface getMetaData(File inputFile)
			   throws IOException
	{
		GeoTessMetaDataEarthInterface metaData = new GeoTessMetaDataEarthInterface();

		if (inputFile.getName().endsWith(".ascii"))
		{
			Scanner input = new Scanner(inputFile);
			metaData.load(input);
			input.close();
		}
		else
		{
			DataInputStream input = new DataInputStream(new BufferedInputStream(
					new FileInputStream(inputFile)));
			metaData.load(input);
			input.close();
		}

		metaData.setInputModelFile(inputFile);

		return metaData;
	}

	/**
	 * Specify the names of the interfaces, and optionally, the layers supported
	 * by the model. Interface names must match entries in the EarthInterface
	 * enum. Layer names can be defined preceding each interface name if desired.
	 * If layer names are not input they are automatically assigned to the string
	 * interface name + "_LAYER". If the interface names are not ordered from the
	 * deepest interface to the shallowest and IOException is thrown. 
	 * 
	 * @param interfaceLayerNames The names of each interface supported by this
	 *                            model, and optionally, each layer name. If
	 *                            provided, the layer names must precede the
	 *                            interface names. Only interface names that match
	 *                            the interfaces names defined in the EarthInterface
	 *                            enum are allowed.
	 * @throws IOException 
	 */
	public void setEarthInterfaceLayers(String interfaceLayerNames) throws IOException
	{
		setEarthInterfaceLayers(interfaceLayerNames.split(";"));
	}

	/**
	 * Specify the names of the interfaces, and optionally, the layers supported
	 * by the model. Interface names must match entries in the EarthInterface
	 * enum. Layer names can be defined preceding each interface name if desired.
	 * If layer names are not input they are automatically assigned to the string
	 * interface name + "_LAYER". If the interface names are not ordered from the
	 * deepest interface to the shallowest and IOException is thrown. 
	 * 
	 * @param interfaceLayerNames The names of each interface supported by this
	 *                            model, and optionally, each layer name. If
	 *                            provided, the layer names must precede the
	 *                            interface names. Only interface names that match
	 *                            the interfaces names defined in the EarthInterface
	 *                            enum are allowed.
	 * @throws IOException 
	 */
	public void setEarthInterfaceLayers(String... interfaceLayerNames)
         throws IOException
  {
    ArrayList<EarthInterface> interfaces = new ArrayList<EarthInterface>(interfaceLayerNames.length);
    ArrayList<String> layers             = new ArrayList<String>(interfaceLayerNames.length);
    
  	String lastLayerName = "";
  	String currentLayerName = "";
    for (int i = 0; i < interfaceLayerNames.length; ++i)
    {
    	// if the next input interface layer name is not an EarthInterface then
    	// throw and catch an error to retrieve the layer name
    	try
    	{
    		// get interface name (throw error if it is not an interface name)
    		
    		currentLayerName  = interfaceLayerNames[i].trim();
    		if (repairOldLayerInterfaceNames)
    			currentLayerName  = EarthInterface.repair(currentLayerName);
    		EarthInterface ei = EarthInterface.valueOf(currentLayerName);

    		// add new interface. If last name was a layer name assign that layer
    		// name with this interface name. Otherwise, use the interface name
    		// appended with "_Layer" as the layer name.

    		interfaces.add(ei);
    		if (lastLayerName.equals(""))
    			layers.add(ei.getDefaultLayerName());
    		else
    		{
    			layers.add(lastLayerName);
    		  lastLayerName = "";
    		}
    	}
    	catch (Exception ex)
    	{
    		// last parsed name was not an interface name so it must be a layer
    		// name. Throw an error if this is the second consecutive layer name.

    		if (!lastLayerName.equals(""))
    		{
    			if (!repairOldLayerInterfaceNames)
    			{
    				// if this is a string full of EarthInterface layer interface names,
    				// but one was mistyped; or if this is a base class layer name
    				// prescription, and one of the layer names is not in the
    				// EarthInterface repair map, then this won't help and the error
    				// will still be thrown. If, however, this is a base class naming
    				// convention and all of the layer names are either in the repair
    				// map or are identical matches with an EarthInterface layer
    				// interface name, then they will be converted (when necessary) to
    				// an EarthInterface label and the set will succeed.

    				repairOldLayerInterfaceNames = true;
    				setEarthInterfaceLayers(interfaceLayerNames);
    				return;
    			}
    			else
    			  throw new IOException("Error: Two consecutive Layer Names (assumed," +
    		                          " as they are NOT VALID Interface Names) \"" +
    		                          lastLayerName + "\" and \"" + currentLayerName +
    		                          "\" were discovered ..." + NL +
    		                          "       Only 1 Layer Name per Interface Name " +
    		                          "is allowed ...");
    		}

    		// assign layer name and continue

    		lastLayerName = currentLayerName;
    	}
    }

    // make sure no trailing layer name was left in the input list

		if (!lastLayerName.equals(""))
		{
			throw new IOException("Error: The input interface/layer name list ended " +
					                  "with a layer name (" + lastLayerName + ") without " +
					                  "an associated interface name ...");
		}

    // save interfaces and layer names to local fields

    layerInterfaces = new EarthInterface [interfaces.size()];
    layerNames      = new String [interfaces.size()];
    for (int i = 0; i < interfaces.size(); ++i)
    {
    	layerInterfaces[i] = interfaces.get(i);
    	layerNames[i]      = layers.get(i);
    }

    // now validate interface names. Throw an error if the interface names are
    // not ordered from deepest to shallowest.   
    
    EarthInterface.validateInterfaceOrder(layerInterfaces);

		// if layerTessIds have not been assigned then set
		// a default value where it is assumed that there
		// will only be one multi-level tessellation in the
		// model. Associate all layers with tessellation 0.

		if (layerTessIds == null)
			layerTessIds = new int[layerInterfaces.length];
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
	@Override
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
        index = getInterfaceIndex(majorInterface.trim());
      }
      if (index != -1) return index;
    }
    return -1;
  }

	/**
	 * Returns the interface index associated with the input EarthInterface name.
	 * If the input EarthInterface name is not supported by this model then -1 is
	 * returned.
	 * 
	 * @param interface The EarthInterface for which its order index is returned to
	 *                 the caller.
	 * 	 * @return The index associated with the input EarthInterface.
	 */
	public int getInterfaceIndex(String interfaceName)
	{
		for (int i = 0; i < layerInterfaces.length; ++i)
			if (layerInterfaces[i].name().equals(interfaceName))
				return i;
		return -1;
	}

	/**
	 * Returns the interface index associated with the input EarthInterface. If
	 * the input EarthInterface is not supported by this model then -1 is returned.
	 * 
	 * @param intrface The EarthInterface for which its order index is returned to
	 *                 the caller.
	 * 	 * @return The index associated with the input EarthInterface.
	 */
	public int getInterfaceIndex(EarthInterface intrface)
	{
		for (int i = 0; i < layerInterfaces.length; ++i)
			if (layerInterfaces[i] == intrface)
				return i;
		return -1;
	}

	/**
	 * Check to ensure that this MetaData object contains all the information
	 * needed to construct a new GeoTessModel. To be complete, the base class
	 * GeoTessMetaData must be complete as-well-as the layerInterfaces field
	 * of this derived class.
	 * 
	 * @throws GeoTessException
	 *             if incomplete.
	 */
	@Override
	public void checkComplete() throws GeoTessException
	{
		((GeoTessMetaData) this).checkComplete();
		
		if (layerInterfaces == null)
		{
			StringBuffer buf = new StringBuffer();
			buf.append(GeoTessUtils.NL).append(
					"layerInterfaces has not been specified.");
			throw new GeoTessException("MetaData is not complete."
					+ buf.toString());
		}
	}

	/**
	 * Retrieve the interface names in a single, semicolon delimited string.
	 * 
	 * @return The interface names in a single, semicolon delimited string.
	 */
	public String getInterfaceNamesString()
	{
		String s = layerInterfaces[0].name();
		for (int i = 1; i < layerInterfaces.length; ++i)
			s += ";" + layerInterfaces[i].name();
		return s;
	}

	/**
	 * Retrieve the layer/interface names in a single, semicolon delimited string.
	 * 
	 * @return The layer/interface names in a single, semicolon delimited string.
	 */
	public String getInterfaceLayerNamesString()
	{
		String s = layerNames[0];
		s = ";" + layerInterfaces[0].name();
		for (int i = 1; i < layerInterfaces.length; ++i)
			s += ";" + layerNames[i] + ";" + layerInterfaces[i].name();
		return s;
	}

	/**
	 * A generic layer name set function for any type of extended GeoTessMetaData
	 * object. This method is over-ridden by the derived class to fill in specific
	 * types of layer name information. In this case the layer names must be
	 * prescribed so as to adhere to the ordered structure of the EarthInterface
	 * enum list.  
	 */
	@Override
	public void setModelLayers(String ... layers) throws IOException
	{
    setEarthInterfaceLayers(layers);
	}

	/**
	 * A generic layer name set function for any type of extended GeoTessMetaData
	 * object that sets the layer name information that is provided by the input
	 * Scanner object. This method is over-ridden by the derived class to fill in
	 * specific types of layer name information. In this case the layer names must
	 * be prescribed so as to adhere to the ordered structure of the
	 * EarthInterface enum list.
	 */
	@Override
	protected void setModelLayers(Scanner input) throws IOException
	{
		String layers = input.nextLine();
		String layerStr = "";

		if (layers.startsWith("layers:"))
		{
			layerStr = layers.substring(7);
		}
		else if (layers.startsWith("interfaces/layers:"))
		  layerStr = layers.substring(18);
		else
			throw new IOException(String.format(
							              "Expected to read string starting with " +
			                      "'interfaces/layers:' or 'layers:' but " +
							              "found '%s'", layers));

		setEarthInterfaceLayers(layerStr.split(";"));
	}

	/**
	 * Returns the model layer name header for asci files for this meta data
	 * definition. This call overrides the base class call to provide specific
	 * information required by this derived type.
	 */
	@Override
	protected String getModelLayerScannerHeader()
	{
		return "interfaces/layers: %s%n";
	}
	
	/**
	 * Returns the GeoTessMetaDataEarthInterface model layer string. This call
	 * overrides the base class implementation to return a ';' separated string
	 * that contains both the interface and layer names of this meta data object.
	 */
	@Override
	protected String getModelLayerString()
	{
		return getInterfaceLayerNamesString();
	}

	/**
	 * Retrieve the EarthInterface associated with the specified index.
	 * 
	 * @param i The index of the interface for which its name is returned.
	 * @return The EarthInterface associated with the specified index.
	 */
	public EarthInterface getInterface(int i)
	{
		return layerInterfaces[i];
	}

	/**
	 * Retrieve the name of the interface associated with the specified index.
	 * 
	 * @param i The index of the interface for which its name is returned.
	 * @return The name of the interface associated with the specified index.
	 */
	public String getInterfaceName(int i)
	{
		return layerInterfaces[i].name();
	}

	/**
	 * Retrieve a reference to the array of EarthInterface objects supported by the
	 * model.
	 * 
	 * @return A reference to the array of EarthInterface objects supported by the
	 *         model.
	 */
	public EarthInterface[] getInterfaces()
	{
		return layerInterfaces;
	}

	/**
	 * Retrieve an array containing all interface names supported by the model.
	 * model.
	 * 
	 * @return An array containing all interface names supported by the model.
	 */
	public String[] getInterfaceNames()
	{
		String[] interfaceNames = new String [layerInterfaces.length];
		for (int i = 0; i < layerInterfaces.length; ++i)
			interfaceNames[i] = layerInterfaces[i].name();
		return interfaceNames;
	}

	/**
	 * Specify the names of the layers supported by the model.
	 * 
	 * @param layerNames
	 *            the names of the layers supported by the model.
	 * @throws IOException 
	 */
	@Override
	public void setLayerNames(String... layerNames) throws IOException
	{
		if (layerInterfaces == null)
		{
			throw new IOException("Error: Layer Interfaces must be assigned before " +
		                        "or simultaneously with layer name assignement.");
		}
		else if (layerNames.length != layerInterfaces.length)
		{
			throw new IOException("Error: Input layer names array length (" +
		                        layerNames.length + ") must be the same as the " +
		                        "assigned interfaces name array length (" +
		                        layerInterfaces.length);
		}

		this.layerNames = layerNames;
		for (int i = 0; i < layerNames.length; ++i)
			layerNames[i] = layerNames[i].trim();
	}
	
	/**
	 * Returns a toString containing just the interface/layer name information
	 * appended to the input StringBuffer object buf.
	 */
	@Override
	protected void toStringLayerNames(StringBuffer buf)
	{
		buf.append("Layers:").append(GeoTessUtils.NL);
		buf.append("  Index  TessId   Layer  Interface").append(GeoTessUtils.NL);
		for (int i = layerNames.length - 1; i >= 0; --i)
			buf.append(String.format("  %3d  %6d     %-1s    %-1s%n", i,
					layerTessIds[i], layerNames[i], layerInterfaces[i].name()));
		buf.append(GeoTessUtils.NL);
	}
}
