package gms.shared.utilities.geotess.extensions.earthinterface;

import java.io.IOException;
import java.util.HashMap;

/**
 * A specification of ordered Earth Interfaces used by the
 * GeoTessModelEarthInterface extension of the GeoTessModel. This fixes the
 * nameing convention and order of Earth interface layer names which is
 * necessary for applications that must determine layers given other types of
 * information such as seismic phase definitions (e.g. The Bender Phase object).
 * 
 * @author jrhipp
 *
 */
public enum EarthInterface
{
	// ordered set of layer interface names (in order of smallest radius to
	// largest radius).
	
	ICB("Inner Core", "Inner Core Boundary"),
	CMB("Outer Core", "Core-Mantle Boundary"),
	M660("Lower Mantle", "Lower mantle boundary generally located at approximately 660 km depth."),
	M410("Mantle Transition Zone", "Middle mantle boundary generally located at approximately 410 km depth."),
	M210("Athenosphere", "Upper mantle boundary generally located at approximately 210 km depth."),
	MOHO("Lower Lithosphere", "Mohorovicic discontinuity or the boundary between the Earth's crust and mantle."),
	LOWER_CRUST_TOP("Lower Crust", "Lower Crust Top boundary."),
	MIDDLE_CRUST_TOP("Middle Crust", "Middle Crust Top boundary."),
	MIDDLE_CRUST_G("Thin", "Middle Crust Top G boundary a infintesimally thin layer below the upper crust."),
	UPPER_CRUST_TOP("Upper Crust", "Upper Crust Top boundary."),
	CRUST_TOP ("Crust", "Crust Top boundary. Generally used to group all crustal layers into a single layer."),
	SEDIMENTARY_LAYER_5_TOP("Sedimentary Layer 5", "Sedimentary Layer 5 Top Boundary."),
	SEDIMENTARY_LAYER_4_TOP("Sedimentary Layer 4", "Sedimentary Layer 4 Top Boundary."),
	SEDIMENTARY_LAYER_3_TOP("Sedimentary Layer 3", "Sedimentary Layer 3 Top Boundary."),
	SEDIMENTARY_LAYER_2_TOP("Sedimentary Layer 2", "Sedimentary Layer 2 Top Boundary."),
	SEDIMENTARY_LAYER_1_TOP("Sedimentary Layer 1", "Sedimentary Layer 1 Top Boundary."),
	SEDIMENTARY_LAYER_TOP("Sedimentary Layer", "Sedimentary Layer Top Boundary. Generally used to group all sedimentary layers into a single layer."),
	SURFACE("", "Top Layer of Earth (Topography)"),
	WATER_TOP("Water", "Water Surface");

	/**
	 * Descriptive String.
	 */
	private String description;

	/**
	 * Default layer name associated with each boundary.
	 */
	private String defaultLayerName;
	
	/**
	 * HashMap containing an association of old interface name keys associated
	 * with the matching EarthInterface name. This map is used to help convert
	 * base class GeoTessMetaData layer names to the equivalent EarthInterface
	 * name.
	 */
	private static HashMap<String, String> oldInterfaceNameRepairMap =
			           new HashMap<String, String>();
	static
	{
		oldInterfaceNameRepairMap.put("CRUST", EarthInterface.CRUST_TOP.name());
		oldInterfaceNameRepairMap.put("LOWER_CRUST",  EarthInterface.LOWER_CRUST_TOP.name());
		oldInterfaceNameRepairMap.put("MIDDLE_CRUST", EarthInterface.MIDDLE_CRUST_TOP.name());
		oldInterfaceNameRepairMap.put("UPPER_CRUST",  EarthInterface.UPPER_CRUST_TOP.name());
		oldInterfaceNameRepairMap.put("SEDIMENTS",    EarthInterface.SEDIMENTARY_LAYER_TOP.name());
  }

	/**
	 * Standard constructor that sets the layer name and description associated
	 * with a layer interface.
	 * 
	 * @param layerName   The layer name of the interface.
	 * @param description The layer description.
	 */
	private EarthInterface(String layerName, String description)
	{
		this.description = description;
		this.defaultLayerName   = layerName;
	}
	
	/**
	 * Returns the layer interface description.
	 * @return
	 */
	public String getDescription()
	{
		return description;
	}
	
	/**
	 * Returns the default layer name of the interface.
	 * 
	 * @return The default layer name of the interface.
	 */
	public String getDefaultLayerName()
	{
		return defaultLayerName;
	}

	/**
	 * Throws an error if the input EarthInterface array is out of order 
	 * (must be deepest as the first entry and the most shallow as the last
	 *  entry).
	 *  
	 * @param interfaces The array of EarthInterface's to be order tested.
	 * @throws IOException
	 */
	static public void validateInterfaceOrder(EarthInterface[] interfaces)
			          throws IOException
	{
		for (int i = 1; i < interfaces.length; ++i)
		{
			if (interfaces[i].ordinal() <= interfaces[i-1].ordinal())
				throw new IOException("Error: EarthInterface Input Order was INVALID between" +
						                  " interface " + interfaces[i].name() + " (later) and interface " +
						                  interfaces[i-1].name() + " (earlier) ....");
    }
	}

	/**
	 * Static method that can be called in method
	 * GeoTessMetaData.setEarthInterfaceLayers(String ...) to replace old model
	 * type names with their corresponding new EarthInterface name.
	 * 
	 * @param name The name to be replaced (if not a valid name).
	 * @return The input name if it is already defined in EarthInterface, or the
	 *         Correct name if the input name matches one of the names checked
	 *         for in the repair map, or the original name if the name is an
	 *         error and a match cannot be found. In the last case the method
	 *         setEarthInterfaceLayers will throw an error indicating that the
	 *         name is invalid.
	 */
	static public String repair(String name)
	{
		try
		{
			// if name is valid return it

			EarthInterface.valueOf(name);
		}
		catch (Exception ex)
		{
			// invalid interface name. See if name is defined in the repair map.
			// If so return the valid name. Otherwise, the input name is returned.

			String newName = oldInterfaceNameRepairMap.get(name.toUpperCase());
			if (newName != null) return newName;
		}

		// return input name

		return name;
	}
}
