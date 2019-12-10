package gms.shared.utilities.geotess.examples;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.Scanner;

import gms.shared.utilities.geotess.GeoTessException;
import gms.shared.utilities.geotess.GeoTessGrid;
import gms.shared.utilities.geotess.GeoTessMetaData;
import gms.shared.utilities.geotess.GeoTessModel;
import gms.shared.utilities.geotess.GeoTessUtils;

/**
 * This class is an example of a class that extends GeoTessModel.
 * It inherits all the functionality of GeoTessModel but adds an extra
 * data item to the model.  In this example, the extra data is 
 * just a simple String, but in real models that extend 
 * GeoTessModel, it could be anything.
 * 
 * <p>Classes that extend GeoTessModel should provide 
 * implementations of all the GeoTessModel constructors and 
 * perform the following initialization functions (order is important!):
 * <ul>
 * <li>perform any initialization functions required by the
 * derived class.
 * <li>call one of the super class loadModel() methods,
 * if appropriate.
 * </ul>
 * <p>In addition, classes that extend GeoTessModel should 
 * override 4 IO functions: loadModelBinary(), writeModelBinary(), 
 * loadModelAscii() and writeModelAscii(). 
 * See examples below.
 * <p>The first thing that these methods do is call the super
 * class implementations to read/write the standard
 * GeoTessModel information.  After that, the methods
 * may read/write the application specific data from/to
 * the end of the standard GeoTessModel file.
 * @author sballar
 *
 */
public class GeoTessModelExtended extends GeoTessModel
{
		/**
		 * This string is just an example that represents whatever 
		 * extra data users application may require.
		 * <br>Do not initialize extraData!
		 */
		protected String extraData = "default value"; 
		
		public GeoTessModelExtended()
		{
			super();
			initializeData();
		}
		
		/**
		 * Construct a new GeoTessModel object and populate it with information from
		 * the specified file.
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
		public GeoTessModelExtended(File modelInputFile, String relativeGridPath) throws IOException
		{ 
			this();
			loadModel(modelInputFile, relativeGridPath);
		}
		
		/**
		 * Construct a new GeoTessModel object and populate it with information from
		 * the specified file.
		 * 
		 * <p>relativeGridPath is assumed to be "" (empty string), which is appropriate
		 * when the grid information is stored in the same file as the model or when
		 * the grid is stored in a separate file located in the same directory as the
		 * model file.
		 * 
		 * @param modelInputFile
		 *            name of file containing the model.
		 * @throws IOException
		 */
		public GeoTessModelExtended(File modelInputFile) throws IOException
		{ 
			this();
			loadModel(modelInputFile, "");
		}
		
		/**
		 * Construct a new GeoTessModel object and populate it with information from
		 * the specified file.
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
		public GeoTessModelExtended(String modelInputFile, String relativeGridPath) throws IOException
		{ 
			this();
			loadModel(modelInputFile, relativeGridPath);
		}
		
		/**
		 * Construct a new GeoTessModel object and populate it with information from
		 * the specified file.
		 * 
		 * <p>relativeGridPath is assumed to be "" (empty string), which is appropriate
		 * when the grid information is stored in the same file as the model or when
		 * the grid is stored in a separate file located in the same directory as the
		 * model file.
		 * 
		 * @param modelInputFile
		 *            name of file containing the model.
		 * @throws IOException
		 */
		public GeoTessModelExtended(String modelInputFile) throws IOException
		{ 
			this();
			loadModel(modelInputFile, "");
		}
		
		/**
		 * Construct a new GeoTessModelExtended object and populate it with information from
		 * the specified DataInputStream.  The GeoTessGrid will be read directly from
		 * the inputStream as well.
		 * @param inputStream
		 * @throws GeoTessException
		 * @throws IOException
		 */
		public GeoTessModelExtended(DataInputStream inputStream) throws GeoTessException, IOException
		{
			this();
			loadModelBinary(inputStream, null, "*");	
		}

		/**
		 * Construct a new GeoTessModelExtended object and populate it with information from
		 * the specified Scanner.  The GeoTessGrid will be read directly from
		 * the inputScanner as well.
		 * @param inputScanner
		 * @throws GeoTessException
		 * @throws IOException
		 */
		public GeoTessModelExtended(Scanner inputScanner) throws GeoTessException, IOException
		{
			this();
			loadModelAscii(inputScanner, null, "*");
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
		 * <li>setLayerNames()
		 * <li>setAttributes()
		 * <li>setDataType()
		 * <li>setLayerTessIds() (only required if grid has more than one
		 * multi-level tessellation)
		 * </ul>
		 * 
		 * @param gridFileName
		 *            name of file from which to load the grid.
		 * @param metaData
		 *            MetaData the new GeoTessModel instantiates a reference to the
		 *            supplied metaData. No copy is made.
		 * @throws GeoTessException
		 *             if metadata is incomplete.
		 * @throws IOException
		 */
		public GeoTessModelExtended(String gridFileName, GeoTessMetaData metaData) throws IOException
		{ 
			super(gridFileName, metaData); 
			initializeData();
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
		 * <li>setLayerNames()
		 * <li>setAttributes()
		 * <li>setDataType()
		 * <li>setLayerTessIds() (only required if grid has more than one
		 * multi-level tessellation)
		 * <li>setSoftwareVersion()
		 * <li>setGenerationDate()
		 * </ul>
		 * 
		 * @param grid
		 *            a reference to the GeoTessGrid that will support this
		 *            GeoTessModel.
		 * @param metaData
		 *            MetaData the new GeoTessModel instantiates a reference to the
		 *            supplied metaData. No copy is made.
		 * @throws GeoTessException
		 *             if metadata is incomplete.
		 */
		public GeoTessModelExtended(GeoTessGrid grid, GeoTessMetaData metaData) throws GeoTessException, IOException
		{ 
			super(grid, metaData); 
			initializeData();
		}
		
		/**
		 * Construct a new GeoTessModelExtended with all the structures from the supplied
		 * baseModel.  The new GeoTessModelExtended will be built with references to the 
		 * GeoTessMetaData, GeoTessGrid and all the Profiles in the baseModel.  
		 * No copies are made. Changes to one will be reflected in the other.  
		 * All of the extraData will be set to default values.
		 * @param baseModel
		 * @throws GeoTessException
		 */
		public GeoTessModelExtended(GeoTessModel baseModel) throws GeoTessException
		{
			super(baseModel.getGrid(), baseModel.getMetaData());
			for (int i = 0; i < baseModel.getNVertices(); ++i)
				for (int j=0; j<baseModel.getNLayers(); ++j)
					setProfile(i,j,baseModel.getProfile(i, j));
			
			initializeData();
		}
		
		/**
		 * Protected method to initialize extraData.  
		 */
		protected void initializeData()
		{
			extraData = "extraData initialized in GeoTessModelExtended.initializeData()";
		}

		/**
		 * Getter.
		 * @return extraData
		 */
		public String getExtraData()
		{
			return extraData;
		}
		
		/**
		 * Setter
		 * @param extraData
		 */
		public void setExtraData(String extraData)
		{
			this.extraData = extraData;
		}
		
		@Override
		public boolean equals(Object other)
		{
			if (!(other instanceof GeoTessModelExtended)) return false;
			GeoTessModelExtended otherModel = (GeoTessModelExtended) other;
			return super.equals(otherModel) && this.extraData.equals(otherModel.extraData);
		}

		/**
		 * Overridden IO method.
		 */
		@Override
		protected void loadModelBinary(DataInputStream input,
				String inputDirectory, String relGridFilePath)
				throws GeoTessException, IOException
		{
			// call super class to load model data from binary file.
			super.loadModelBinary(input, inputDirectory, relGridFilePath);
			
			// it is good practice, but not required, to store the class
			// name as the first thing added by the extending class.
			String className = GeoTessUtils.readString(input);
			if (!className.equals(this.getClass().getSimpleName()))
				throw new IOException("Found class name "+className
						+" but expecting "
			+this.getClass().getSimpleName());
			
			// it is good practice, but not required, to store a format 
			// version number as the second thing added by the extending class.
			// With this information, if the format changes in a future release
			// it may be possible to make the class backward compatible.
			int formatVersion = input.readInt();
			
			if (formatVersion == 1)
				initializeData();
			else
				throw new IOException("Format version "+formatVersion+" is not supported.");
			
			// load application specific data in binary format.
			extraData = GeoTessUtils.readString(input);
			
		}

		/**
		 * Overridden IO method.
		 */
		@Override
		protected void writeModelBinary(DataOutputStream output, String gridFileName)
				throws IOException
		{
			// call super class to write standard model information to binary file.
			super.writeModelBinary(output, gridFileName);

			// it is good practice, but not required, to store the class
			// name as the first thing added by the extending class.
			GeoTessUtils.writeString(output, this.getClass().getSimpleName());
			
			// it is good practice, but not required, to store a format 
			// version number as the second thing added by the extending class.
			// With this information, if the format changes in a future release
			// it may be possible to make the class backward compatible.
			output.writeInt(1);
			
			// now output the extraData
			GeoTessUtils.writeString(output, extraData);
		}

		/**
		 * Overridden IO method.
		 */
		@Override
		protected void loadModelAscii(Scanner input, String inputDirectory,
				String relGridFilePath) throws GeoTessException, IOException
		{
			super.loadModelAscii(input, inputDirectory, relGridFilePath);
			
			// it is good practice, but not required, to store the class
			// name as the first thing added by the extending class.
			String className = input.nextLine();
			if (!className.equals(this.getClass().getSimpleName()))
				throw new IOException("Found class name "+className
						+" but expecting "
			+this.getClass().getSimpleName());
			
			// it is good practice, but not required, to store a format 
			// version number as the second thing added by the extending class.
			// With this information, if the format changes in a future release
			// it may be possible to make the class backward compatible.
			int formatVersion = input.nextInt();
			input.nextLine();
			
			if (formatVersion == 1)
				initializeData();
			else
				throw new IOException("Format version "+formatVersion+" is not supported.");
			
			// load application specific data in binary format.
			extraData = input.nextLine();
			
		}

		/**
		 * Overridden IO method.
		 */
		@Override
		protected void writeModelAscii(Writer output, String gridFileName)
				throws IOException
		{
			// call super class to write standard model information to ascii file.
			super.writeModelAscii(output, gridFileName);

			// it is good practice, but not required, to store the class
			// name and a format version number as the first things added 
			// by the extending class.
			// With this information, if the format changes in a future release
			// it may be possible to make the class backward compatible.
			output.write(String.format("%s%n%d%n", this.getClass().getSimpleName(), 1));
			
			// write application specific data in ascii format.
			output.write(extraData);
			output.write("\n");
			output.flush();
		}

}
