package gms.shared.utilities.geotess;

import gms.shared.utilities.geotess.util.globals.DataType;
import gms.shared.utilities.geotess.util.globals.InterpolatorType;
import gms.shared.utilities.geotess.util.numerical.vector.VectorGeo;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GeoTessExamples {

  GeoTessModel model;

  @BeforeEach
  void init() throws GeoTessException, IOException {

    // Get (local) URL of model file to load into GeoTessModel
    URL modelUrl = Thread.currentThread().getContextClassLoader()
        .getResource("permanent_files/geotess_grid_01000.geotess");

    // Validate that URL is not null
    Objects.requireNonNull(modelUrl);

    // Extract file name to load into GeoTessModel
    String gridFileName = modelUrl.getFile();

    // Create file object to pass to GeoTessModel constructor
    File gridInputFile = new File(gridFileName);

    // Validate file is not null
    Objects.requireNonNull(gridInputFile);

    // Create empty GeoTessMetaData
    GeoTessMetaData metaData = new GeoTessMetaData();

    // Populate mandatory attributes in GeoTessMetaData
    metaData.setInputModelFile(gridInputFile);
    metaData.setAttributes(new String[]{"ASDF", "FDSA"}, new String[]{"ASDF", "FDSA"});
    metaData.setLayerNames("CRUST");
    metaData.setModelSoftwareVersion("1.0.0");
    metaData.setDataType(DataType.INT);

    // Create GeoTessModel
    this.model = new GeoTessModel(gridInputFile, metaData);
  }

  @Test
  void loadModelAndData() throws GeoTessException {

    // Print some basic metadata about the GeoTessModel we generated
    System.out.println("Layers: " + this.model.getNLayers());
    System.out.println("Attributes: " + this.model.getNAttributes());
    System.out.println("Vertices: " + this.model.getNVertices());
    System.out
        .println("Layer Names: " + Arrays.stream(this.model.getMetaData().getLayerNames()).collect(
            Collectors.toList()));

    // Loop over every vertex to load in data
    for (int vertexIndex = 0; vertexIndex < this.model.getNVertices(); vertexIndex++) {

      // Get the unit vector that represents the location of the current vertex
      double[] vertexUnitVector = this.model.getGrid().getVertex(vertexIndex);

      // Get the earth radius at the current vertex
      double earthRadius = VectorGeo.getEarthRadius(vertexUnitVector);

      // Create radii at surface and at 50km depth
      float[] radii = new float[]{(float) (earthRadius - 50.0), (float) earthRadius};

      // Set data for both attributes at surface and 50km depth
      Data surfaceAttributes = new DataArrayOfInts(1, 1);
      Data subsurfaceAttributes = new DataArrayOfInts(0, 0);

      // Set profile for vertex and layer
      this.model
          .setProfile(vertexIndex, 0, radii, new Data[]{subsurfaceAttributes, surfaceAttributes});
    }

    GeoTessPosition position = new GeoTessPositionNaturalNeighbor(this.model,
        InterpolatorType.NATURAL_NEIGHBOR);

    position.set(0.0, 0.0, 0.0);

    System.out.println(position.getValue(0));
  }
}
