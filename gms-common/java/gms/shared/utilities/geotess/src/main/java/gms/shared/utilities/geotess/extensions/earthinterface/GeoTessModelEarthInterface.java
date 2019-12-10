package gms.shared.utilities.geotess.extensions.earthinterface;

import java.io.File;
import java.io.IOException;

import gms.shared.utilities.geotess.GeoTessException;
import gms.shared.utilities.geotess.GeoTessGrid;
import gms.shared.utilities.geotess.GeoTessMetaData;
import gms.shared.utilities.geotess.GeoTessModel;
import gms.shared.utilities.geotess.util.globals.OptimizationType;

/**
 * An extended GeoTessModel that contains layer interface names in addition to
 * layer names that must be equivalent to and ordered by the layer interface
 * prescription given by the enum EarthInterface. This is used by applications
 * that require precise naming in order to determine Earth layers by name (e.g.
 * the Phase object used by Bender).
 * 
 * @author jrhipp
 *
 */
public class GeoTessModelEarthInterface extends GeoTessModel
{
	/**
	 * Simple return class that can be over-ridden by derived types to return
	 * extended meta-data objects.
	 * 
	 * @return new meta-data object
	 */
	@Override
	protected GeoTessMetaData getNewMetaData()
	{
		return new GeoTessMetaDataEarthInterface();
	}

	/**
	 * Construct a new GeoTessModelEarthInterface object and populate it with
	 * information from the specified file.
	 * 
	 * @param modelInputFile
	 *            name of file containing the model.
	 * @param relativeGridPath
	 *            the relative path from the directory where the model is stored
	 *            to the directory where the grid is stored. Often, the model
	 *            and grid are stored together in the same file in which case
	 *            this parameter is ignored. Sometimes, however, the grid is
	 *            stored in a separate file and only the name of the grid file
	 *            (without path information) is stored in the model file. In
	 *            this case, the code needs to know which directory to search
	 *            for the grid file. The default is "" (empty string), which
	 *            will cause the code to search for the grid file in the same
	 *            directory in which the model file resides. Bottom line is that
	 *            the default value is appropriate when the grid is stored in
	 *            the same file as the model, or the model file is in the same
	 *            directory as the model file.
	 * @param optimization
	 *            either OptimizationType.SPEED or OptimizationType.MEMORY. The
	 *            default is SPEED wherein the code will execute faster but
	 *            require more memory to run.
	 * @throws IOException
	 */
  public GeoTessModelEarthInterface(File modelInputFile, String relativeGridPath, OptimizationType optimization) 
			throws IOException
	{ 
		super(modelInputFile, relativeGridPath, optimization); 
	}
  
	/**
	 * Construct a new GeoTessModelEarthInterface object and populate it with
	 * information from the specified file.
	 * 
	 * <p>relativeGridPath is assumed to be "" (empty string), which is appropriate
	 * when the grid information is stored in the same file as the model or when
	 * the grid is stored in a separate file located in the same directory as the
	 * model file.
	 * 
	 * @param modelInputFile
	 *            name of file containing the model.
	 * @param optimization
	 *            either OptimizationType.SPEED or OptimizationType.MEMORY. The
	 *            default is SPEED wherein the code will execute faster but
	 *            require more memory to run.
	 * @throws IOException
	 */
  public GeoTessModelEarthInterface(File modelInputFile, OptimizationType optimization) throws IOException
	{ 
		super(modelInputFile, optimization); 
	}
  
	/**
	 * Construct a new GeoTessModelEarthInterface object and populate it with
	 * information from the specified file.
	 * 
	 * <p>OptimizationType will default to SPEED, as opposed to MEMORY.  With 
	 * OptimizationType.SPEED, the code will execute more quickly but will 
	 * require more memory to run.
	 * 
	 * @param modelInputFile
	 *            name of file containing the model.
	 * @param relativeGridPath
	 *            the relative path from the directory where the model is stored
	 *            to the directory where the grid is stored. Often, the model
	 *            and grid are stored together in the same file in which case
	 *            this parameter is ignored. Sometimes, however, the grid is
	 *            stored in a separate file and only the name of the grid file
	 *            (without path information) is stored in the model file. In
	 *            this case, the code needs to know which directory to search
	 *            for the grid file. The default is "" (empty string), which
	 *            will cause the code to search for the grid file in the same
	 *            directory in which the model file resides. Bottom line is that
	 *            the default value is appropriate when the grid is stored in
	 *            the same file as the model, or the model file is in the same
	 *            directory as the model file.
	 * @throws IOException
	 */
  public GeoTessModelEarthInterface(File modelInputFile, String relativeGridPath) throws IOException
	{ 
		super(modelInputFile, relativeGridPath); 
	}
	
	/**
	 * Construct a new GeoTessModelEarthInterface object and populate it with
	 * information from the specified file.
	 * 
	 * <p>relativeGridPath is assumed to be "" (empty string), which is appropriate
	 * when the grid information is stored in the same file as the model or when
	 * the grid is stored in a separate file located in the same directory as the
	 * model file.
	 * 
	 * <p>OptimizationType will default to SPEED, as opposed to MEMORY.  With 
	 * OptimizationType.SPEED, the code will execute more quickly but will 
	 * require more memory to run.
	 * 
	 * @param modelInputFile
	 *            name of file containing the model.
	 * @throws IOException
	 */  
  public GeoTessModelEarthInterface(File modelInputFile) throws IOException
	{ 
		super(modelInputFile); 
	}
	
	/**
	 * Construct a new GeoTessModelEarthInterface object and populate it with
	 * information from the specified file.
	 * 
	 * @param modelInputFile
	 *            name of file containing the model.
	 * @param relativeGridPath
	 *            the relative path from the directory where the model is stored
	 *            to the directory where the grid is stored. Often, the model
	 *            and grid are stored together in the same file in which case
	 *            this parameter is ignored. Sometimes, however, the grid is
	 *            stored in a separate file and only the name of the grid file
	 *            (without path information) is stored in the model file. In
	 *            this case, the code needs to know which directory to search
	 *            for the grid file. The default is "" (empty string), which
	 *            will cause the code to search for the grid file in the same
	 *            directory in which the model file resides. Bottom line is that
	 *            the default value is appropriate when the grid is stored in
	 *            the same file as the model, or the model file is in the same
	 *            directory as the model file.
	 * @param optimization
	 *            either OptimizationType.SPEED or OptimizationType.MEMORY. The
	 *            default is SPEED wherein the code will execute faster but
	 *            require more memory to run.
	 * @throws IOException
	 */
  public GeoTessModelEarthInterface(String modelInputFile, String relativeGridPath, OptimizationType optimization) throws IOException
	{ 
		super(modelInputFile, relativeGridPath, optimization); 
	}
  
	/**
	 * Construct a new GeoTessModelEarthInterface object and populate it with
	 * information from the specified file.
	 * 
	 * <p>relativeGridPath is assumed to be "" (empty string), which is appropriate
	 * when the grid information is stored in the same file as the model or when
	 * the grid is stored in a separate file located in the same directory as the
	 * model file.
	 * 
	 * @param modelInputFile
	 *            name of file containing the model.
	 * @param optimization
	 *            either OptimizationType.SPEED or OptimizationType.MEMORY. The
	 *            default is SPEED wherein the code will execute faster but
	 *            require more memory to run.
	 * @throws IOException
	 */
  public GeoTessModelEarthInterface(String modelInputFile, OptimizationType optimization) throws IOException
	{ 
		super(modelInputFile, optimization); 
	}
  
	/**
	 * Construct a new GeoTessModelEarthInterface object and populate it with
	 * information from the specified file.
	 * 
	 * <p>OptimizationType will default to SPEED, as opposed to MEMORY.  With 
	 * OptimizationType.SPEED, the code will execute more quickly but will 
	 * require more memory to run.
	 * 
	 * @param modelInputFile
	 *            name of file containing the model.
	 * @param relativeGridPath
	 *            the relative path from the directory where the model is stored
	 *            to the directory where the grid is stored. Often, the model
	 *            and grid are stored together in the same file in which case
	 *            this parameter is ignored. Sometimes, however, the grid is
	 *            stored in a separate file and only the name of the grid file
	 *            (without path information) is stored in the model file. In
	 *            this case, the code needs to know which directory to search
	 *            for the grid file. The default is "" (empty string), which
	 *            will cause the code to search for the grid file in the same
	 *            directory in which the model file resides. Bottom line is that
	 *            the default value is appropriate when the grid is stored in
	 *            the same file as the model, or the model file is in the same
	 *            directory as the model file.
	 * @throws IOException
	 */
  public GeoTessModelEarthInterface(String modelInputFile, String relativeGridPath) throws IOException
	{ 
		super(modelInputFile, relativeGridPath); 
	}

	/**
	 * Construct a new GeoTessModelEarthInterface object and populate it with
	 * information from the specified file.
	 * 
	 * <p>relativeGridPath is assumed to be "" (empty string), which is appropriate
	 * when the grid information is stored in the same file as the model or when
	 * the grid is stored in a separate file located in the same directory as the
	 * model file.
	 * 
	 * <p>OptimizationType will default to SPEED, as opposed to MEMORY.  With 
	 * OptimizationType.SPEED, the code will execute more quickly but will 
	 * require more memory to run.
	 * 
	 * @param modelInputFile
	 *            name of file containing the model.
	 * @throws IOException
	 */
  public GeoTessModelEarthInterface(String modelInputFile) throws IOException
	{ 
		super(modelInputFile); 
	}
	
	/**
	 * Parameterized constructor, specifying the grid and metadata for the
	 * model. The grid is constructed and the data structures are initialized
	 * based on information supplied in metadata. The data structures are not
	 * populated with any information however (all Profiles are null). The
	 * application should populate the new model's Profiles after this
	 * constructor completes.
	 * 
	 * <p>
	 * Before calling this constructor, the supplied MetaData object must be
	 * populated with required information by calling the following MetaData
	 * methods:
	 * <ul>
	 * <li>setDescription()
	 * <li>setModelLayerNames()
	 * <li>setAttributes()
	 * <li>setDataType()
	 * <li>setLayerTessIds() (only required if grid has more than one
	 * multi-level tessellation)
	 * <li>setOptimization() (optional: defaults to SPEED)
	 * </ul>
	 * 
	 * @param gridFileName
	 *            name of file from which to load the grid.
	 * @param metaData
	 *            MetaData the new GeoTessModelEarthInterface instantiates a
	 *            reference to the supplied metaData. No copy is made.
	 * @throws IOException
	 *             if metadata is incomplete.
	 * @throws IOException
	 */
  public GeoTessModelEarthInterface(File gridFileName, GeoTessMetaDataEarthInterface metaData)
  		   throws IOException
	{ 
		super(gridFileName, metaData); 
	}
  
	/**
	 * Parameterized constructor, specifying the grid and metadata for the
	 * model. The grid is constructed and the data structures are initialized
	 * based on information supplied in metadata. The data structures are not
	 * populated with any information however (all Profiles are null). The
	 * application should populate the new model's Profiles after this
	 * constructor completes.
	 * 
	 * <p>
	 * Before calling this constructor, the supplied MetaData object must be
	 * populated with required information by calling the following MetaData
	 * methods:
	 * <ul>
	 * <li>setDescription()
	 * <li>setModelLayerNames()
	 * <li>setAttributes()
	 * <li>setDataType()
	 * <li>setLayerTessIds() (only required if grid has more than one
	 * multi-level tessellation)
	 * <li>setOptimization() (optional: defaults to SPEED)
	 * </ul>
	 * 
	 * @param gridFileName
	 *            name of file from which to load the grid.
	 * @param metaData
	 *            MetaData the new GeoTessModelEarthInterface instantiates a
	 *            reference to the supplied metaData. No copy is made.
	 * @throws GeoTessException
	 *             if metadata is incomplete.
	 * @throws IOException
	 */
  public GeoTessModelEarthInterface(String gridFileName, GeoTessMetaDataEarthInterface metaData)
  		   throws IOException
	{ 
		super(gridFileName, metaData); 
	}
  
	/**
	 * Parameterized constructor, specifying the grid and metadata for the
	 * model. The grid is constructed and the data structures are initialized
	 * based on information supplied in metadata. The data structures are not
	 * populated with any information however (all Profiles are null). The
	 * application should populate the new model's Profiles after this
	 * constructor completes.
	 * 
	 * <p>
	 * Before calling this constructor, the supplied MetaData object must be
	 * populated with required information by calling the following MetaData
	 * methods:
	 * <ul>
	 * <li>setDescription()
	 * <li>setModelLayerNames()
	 * <li>setAttributes()
	 * <li>setDataType()
	 * <li>setLayerTessIds() (only required if grid has more than one
	 * multi-level tessellation)
	 * <li>setOptimization() (optional: defaults to SPEED)
	 * <li>setSoftwareVersion()
	 * <li>setGenerationDate()
	 * </ul>
	 * 
	 * @param grid
	 *            a reference to the GeoTessGrid that will support this
	 *            GeoTessModel.
	 * @param metaData
	 *            MetaData the new GeoTessModelEarthInterface instantiates a
	 *            reference to the supplied metaData. No copy is made.
	 * @throws GeoTessException
	 *             if metadata is incomplete.
	 */
  public GeoTessModelEarthInterface(GeoTessGrid grid, GeoTessMetaDataEarthInterface metaData)
  		   throws GeoTessException, IOException
	{ 
		super(grid, metaData); 
	}
}
