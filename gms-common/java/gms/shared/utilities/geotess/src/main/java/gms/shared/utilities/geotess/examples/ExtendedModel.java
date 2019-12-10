package gms.shared.utilities.geotess.examples;

import gms.shared.utilities.geotess.GeoTessModel;

import java.io.File;

public class ExtendedModel
{

	/**
	 * An example of how to implement an extension of a GeoTessModel.
	 * The definition of the extended model is contained in file
	 * GeoTessModelExtended.java.  This method instantiates
	 * an instance of the model and queries it for the extra data.
	 * 
	 * @param args must supply one argument: the path to the GeoTessModels directory.
	 */
	public static void main(String[] args)
	{
		try
		{
			if (args.length == 0)
				throw new Exception(
						"\nMust specify a single command line argument specifying " +
						"the path to the GeoTessModels diretory\n");

			File baseModelFile = new File(new File(args[0]), "crust20.geotess");
			
			System.out.println("Example that illustrates how to use GeoTessModelExtended that " +
					"extends a regular GeoTessModel base class.");
			System.out.println();
			
			// load a regular GeoTessModel.  This is not the extended model.
			GeoTessModel baseModel = new GeoTessModel(baseModelFile);
			
			// construct an instance of a GeoTessModelExtended that
			// has MetaData, Grid and Profile information copied from
			// the base class.  The extra data accessible only from the
			// derived class is initialized in the constructor to 
			// "extraData initialized in GeoTessModelExtended.initializeData()".
			GeoTessModelExtended extModel = new GeoTessModelExtended(baseModel);
			
			// Note that instead of using a copy constructor to instantiate a 
			// new GeoTessModelExtended object, we could have used one of the 
			// methods that demonstrated in populateModel2D or populateModel3D.
			
			// in this trivial example, the extended model implements a single
			// string called 'extraData'.  It is initialized to 'default value'
			// in the GeoTessModelExtended constructor.  A getter() and setter()
			// are implemented to allow applications to retrieve and modify the
			// value.  Likely, real classes that extend GeoTessModel
			// would involve more complicated structures.

			// print the extraData to the screen.  This is the default value
			// assigned by the GeoTessModelExtended constructor.
			System.out.println(extModel.getExtraData());
			
			// change the extraData to a new string.
			extModel.setExtraData("modified value");

			// retrieve the modified value of extraData.
			System.out.println(extModel.getExtraData());
			
			System.out.println("\nDone.");
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}

	}

}
