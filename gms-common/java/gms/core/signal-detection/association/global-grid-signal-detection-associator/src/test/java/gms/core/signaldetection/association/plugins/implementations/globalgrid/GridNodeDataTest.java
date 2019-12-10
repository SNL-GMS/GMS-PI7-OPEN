package gms.core.signaldetection.association.plugins.implementations.globalgrid;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.PhaseType;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.InformationSource;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.association.commonobjects.GridNode;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.association.commonobjects.NodeStation;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.association.commonobjects.PhaseInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceStation;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.StationType;
import gms.shared.utilities.geotess.Data;
import gms.shared.utilities.geotess.GeoTessException;
import gms.shared.utilities.geotess.GeoTessGrid;
import gms.shared.utilities.geotess.GeoTessMetaData;
import gms.shared.utilities.geotess.GeoTessModel;
import gms.shared.utilities.geotess.GeoTessUtils;
import gms.shared.utilities.geotess.Profile;
import gms.shared.utilities.geotess.util.numerical.vector.VectorGeo;
import gms.shared.utilities.javautilities.generation.GenerationException;
import gms.shared.utilities.signalfeaturepredictionutility.SignalFeaturePredictionUtility;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.SortedSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class GridNodeDataTest {

  @Test
  void testCreateModelAndLoadWithData() throws Exception {
    // Make a smaller grid containing fewer vertices so the test doesn't take
    // hours.
    GeoTessGrid smallGrid = loadGridAndShrink(
        "tesseract-models/geotess_grid_01000.geotess",
        20);

    // You have to set this before calling
    // metaData.setDataType(GridNodeData.getSingletonReader().getDataTypeString()) so
    // the singleton reader will be in the static map.
    GeoTessMetaData.addCustomDataType(GridNodeData.getSingletonReader());

    // Create empty GeoTessMetaData
    GeoTessMetaData metaData = new GeoTessMetaData();

    // Populate mandatory attributes in GeoTessMetaData

    // The number of names has to be the same as the number of units. These are fairly
    // meaningless, but they have to be the same length.
    metaData.setAttributes(new String[]{"gridNodeData"}, new String[]{"gridNodeData"});
    metaData.setLayerNames("CRUST");
    metaData.setModelSoftwareVersion("1.0.0");
    // Don't set it to DataType.CUSTOM, but to the data type string returned by the
    // custom data type set above. Yes, this is weird. Since it doesn't translate to a
    // DataType enum, it generates an exception that sets the data type to DataType.CUSTOM and
    // loads the customDataInitializer from a static map.
    metaData.setDataType(GridNodeData.getSingletonReader().getDataTypeString());

    // Create a GeoTessModel from the small grid and the metadata
    GeoTessModel model = new GeoTessModel(smallGrid, metaData);

    final int numVertices = model.getNVertices();

    final long msec = populateModel(model, 1);

    System.out.printf("******* Time in msec to populate %d gridNodes: %d\n",
        numVertices, msec);

    File tempFile = File.createTempFile("geotessmodel_", ".geotess");

    model.writeModel(tempFile);

    GeoTessModel model2 = new GeoTessModel(tempFile);

    checkEqualData(model, model2, false);
  }

  @Test
  @Disabled // Because this test would substantially increase build times.
  void testSingleThreadedVsMultithreaded() throws Exception {

    // Make a smaller grid containing fewer vertices so the test doesn't take
    // hours.
    GeoTessGrid smallGrid = loadGridAndShrink(
        "tesseract-models/geotess_grid_01000.geotess", 500);

    // You have to set this before calling
    // metaData.setDataType(GridNodeData.getSingletonReader().getDataTypeString()) so
    // the singleton reader will be in the static map.
    GeoTessMetaData.addCustomDataType(GridNodeData.getSingletonReader());

    // Create empty GeoTessMetaData
    GeoTessMetaData metaData = new GeoTessMetaData();

    // Populate mandatory attributes in GeoTessMetaData

    // The number of names has to be the same as the number of units. These are fairly
    // meaningless, but they have to be the same length.
    metaData.setAttributes(new String[]{"gridNodeData"}, new String[]{"gridNodeData"});
    metaData.setLayerNames("CRUST");
    metaData.setModelSoftwareVersion("1.0.0");
    // Don't set it to DataType.CUSTOM, but to the data type string returned by the
    // custom data type set above. Yes, this is weird. Since it doesn't translate to a
    // DataType enum, it generates an exception that sets the data type to DataType.CUSTOM and
    // loads the customDataInitializer from a static map.
    metaData.setDataType(GridNodeData.getSingletonReader().getDataTypeString());

    // Create a GeoTessModel from the small grid and the metadata
    GeoTessModel model1 = new GeoTessModel(smallGrid, metaData);
    GeoTessModel model2 = new GeoTessModel(smallGrid, metaData);

    final int numThreads = Runtime.getRuntime().availableProcessors();

    final long msecSingleThreaded = populateModel(model1, 1);
    final long msecMultiThreaded = populateModel(model2, numThreads);

    System.out.printf("******* %d msec single-threaded, %d msec with %d threads\n",
        msecSingleThreaded, msecMultiThreaded, numThreads);

    checkEqualData(model1, model2, true);
  }

  /**
   * Utility method for reading a GeoTessGrid from a file and then creating a smaller
   * grid if necessary with a maximum number of vertices.
   * @param urlPath
   * @param maxVertices
   * @return
   * @throws IOException
   * @throws GeoTessException
   */
  public static GeoTessGrid loadGridAndShrink(final String urlPath, final int maxVertices)
      throws IOException, GeoTessException {

    // Load up a grid from the geotess area with many vertices.
    URL modelUrl = Thread.currentThread().getContextClassLoader().getResource(urlPath);

    if (modelUrl == null) {
      throw new IOException("no resource named " + urlPath);
    }

    GeoTessGrid largeGrid = new GeoTessGrid(new File(modelUrl.getFile()));

    // Make a smaller grid containing fewer vertices so the test doesn't take
    // hours.
    return smallGridOutOfLargeGrid(largeGrid, maxVertices);
  }

  public static void checkEqualData(
      GeoTessModel model1, GeoTessModel model2, boolean disregardIDs) {
    assertEquals(model1.getNVertices(), model2.getNVertices());
    assertEquals(model1.getNLayers(), model2.getNLayers());
    for (int v = 0; v < model1.getNVertices(); v++) {
      for (int layer = 0; layer < model1.getNLayers(); layer++) {
        Profile profile1 = model1.getProfile(v, layer);
        Profile profile2 = model2.getProfile(v, layer);

        Data[] data1 = profile1.getData();
        Data[] data2 = profile2.getData();

        int len1 = data1 != null ? data1.length : -1;
        int len2 = data2 != null ? data2.length : -1;

        assertEquals(1, len1);
        assertEquals(len1, len2);

        assertTrue(equal((GridNodeData) data1[0],
            (GridNodeData) data2[0], disregardIDs),
            "data unequal for vertex " + v + ", layer " + layer);
      }
    }
  }

  /**
   * Checks whether to GridNodeData instances are equal.
   * @param gridNodeData1
   * @param gridNodeData2
   * @param disregardIDs if true, disregards UUIDs.
   * @return
   */
  private static boolean equal(GridNodeData gridNodeData1, GridNodeData gridNodeData2,
      boolean disregardIDs) {
    if (disregardIDs) {
      GridNode gridNode1 = gridNodeData1.getGridNode();
      GridNode gridNode2 = gridNodeData2.getGridNode();
      if (gridNode1 == null && gridNode2 != null || gridNode1 != null && gridNode2 == null) {
        return false;
      }
      if (gridNode1 != null) {
        if (Double.doubleToLongBits(gridNode1.getCenterLatitudeDegrees()) !=
            Double.doubleToLongBits(gridNode2.getCenterLatitudeDegrees())) {
          return false;
        }
        if (Double.doubleToLongBits(gridNode1.getCenterLongitudeDegrees()) !=
            Double.doubleToLongBits(gridNode2.getCenterLongitudeDegrees())) {
          return false;
        }
        if (Double.doubleToLongBits(gridNode1.getCenterDepthKm()) !=
            Double.doubleToLongBits(gridNode2.getCenterDepthKm())) {
          return false;
        }
        if (Double.doubleToLongBits(gridNode1.getGridCellHeightKm()) !=
            Double.doubleToLongBits(gridNode2.getGridCellHeightKm())) {
          return false;
        }
        SortedSet<NodeStation> nodeStations1 = gridNode1.getNodeStations();
        SortedSet<NodeStation> nodeStations2 = gridNode2.getNodeStations();
        if (nodeStations1.size() != nodeStations2.size()) {
          return false;
        }
        NodeStation[] stations1 = nodeStations1.toArray(new NodeStation[nodeStations1.size()]);
        NodeStation[] stations2 = nodeStations2.toArray(new NodeStation[nodeStations2.size()]);
        for (int i=0; i<stations1.length; i++) {
          if (!almostEqual(stations1[i], stations2[i])) {
            return false;
          }
        }
      }
      return true;
    } else {
      return Objects.equals(gridNodeData1, gridNodeData2);
    }
  }

  /**
   * Checks whether two node stations are equal disregarding UUIDs.
   * @param station1
   * @param station2
   * @return
   */
  private static boolean almostEqual(NodeStation station1, NodeStation station2) {
    if (station1 == null && station2 != null || station1 != null && station2 == null) {
      return false;
    }
    if (station1 != null) {
      if (Double.doubleToLongBits(station1.getDistanceFromGridPointDegrees()) !=
        Double.doubleToLongBits(station2.getDistanceFromGridPointDegrees())) {
        return false;
      }
      SortedSet<PhaseInfo> phaseInfos1 = station1.getPhaseInfos();
      SortedSet<PhaseInfo> phaseInfos2 = station2.getPhaseInfos();
      if (phaseInfos1.size() != phaseInfos2.size()) {
        return false;
      }
      PhaseInfo[] infos1 = phaseInfos1.toArray(new PhaseInfo[phaseInfos1.size()]);
      PhaseInfo[] infos2 = phaseInfos2.toArray(new PhaseInfo[phaseInfos2.size()]);
      for (int i=0; i<infos1.length; i++) {
        if (!Objects.equals(infos1[i], infos2[i])) {
          return false;
        }
      }
    }
    return true;
  }

  private static Optional<GridNode> generateGridNode(
      GridNodeGenerator gridNodeGenerator,
      double[] unitVector) {
    try {
      double latitudeDegrees = GeoTessUtils.getLatDegrees(unitVector);
      double longitudeDegrees = GeoTessUtils.getLonDegrees(unitVector);

      return gridNodeGenerator.gridPointLatDegrees(GeoTessUtils.getLatDegrees(unitVector))
          .gridPointLonDegrees(GeoTessUtils.getLonDegrees(unitVector))
          .generate();

    } catch (Exception e) {
      return Optional.empty();
    }
  }

  /**
   * Populates every vertex of a model with a profile containing a GridNodeData.
   * @param model
   * @param numThreads
   * @return the number of milliseconds taken to populate the model.
   * @throws Exception
   */
  private static long populateModel(final GeoTessModel model, int numThreads)
    throws Exception {

    final long startMs = System.currentTimeMillis();

    assertTrue(numThreads >= 1);
    final int numVertices = model.getNVertices();

    numThreads = Math.min(numThreads, numVertices);

    final int[] verticesEachThread = new int[numThreads];
    if (numThreads == 1) {
      verticesEachThread[0] = numVertices;
    } else {
      Arrays.fill(verticesEachThread, numVertices/numThreads);
      int leftOver = numVertices%numThreads;
      for (int i=0; i<leftOver; i++) {
        verticesEachThread[i]++;
      }
      int sum = 0;
      for (int i=0; i<numThreads; i++) {
        sum += verticesEachThread[i];
      }
      assertEquals(numVertices, sum);
    }
    int start = 0;
    final List<Callable<Void>> workers = new ArrayList<>(numThreads);
    for (int i=0; i<numThreads; i++) {

      final boolean multithreaded = numThreads > 1;
      final int threadNum = i;
      final int startVertex = start;
      final int endVertex = start + verticesEachThread[i];

      workers.add(new Callable<Void>() {

        @Override
        public Void call() {

          SignalFeaturePredictionUtility signalFeaturePredictionUtility =
              new SignalFeaturePredictionUtility();

//          if (multithreaded) {
//            System.out.printf("\n%%%%%% Worker %d populating gridnodes for vertices [%d - %d]\n\n",
//                threadNum, startVertex, endVertex - 1);
//          }

          // Set up a grid node generator with all parameters but the latitude and longitude.
          GridNodeGenerator gridNodeGenerator = new GridNodeGenerator()
              .gridPointDepthKm(50.0)
              .predictionUtility(signalFeaturePredictionUtility)
              .travelTimePredictionEarthModel("ak135")
              .magnitudeAttenuationPredictionEarthModel("VeithClawson72")
              .minimumMagnitude(0.0)
              .gridCylinderRadiusDegrees(0.25)
              .gridCylinderHeightKm(10.0)
              .referenceStations(List.of(ReferenceStation.create(
                  "MKAR",
                  "Never put bread up your nose ...",
                  StationType.SeismicArray,
                  InformationSource
                      .create("Because even after ...", Instant.EPOCH,
                          "you take it out ..."),
                  "it still feels like it's in there.",
                  0.0, 20.0, 0.0,
                  Instant.EPOCH, Instant.EPOCH,
                  List.of()
              )))
              .phaseTypes(List.of(PhaseType.P));


          for (int v = startVertex; v < endVertex; v++) {

            // Get the unit vector for the vertex
            double[] unitVec = model.getGrid().getVertex(v);
            // Compute the earth radius at that vector location
            double earthRadius = VectorGeo.getEarthRadius(unitVec);
            // Create radii at 50 Km down and at the surface
            float[] radii = new float[]{
                (float) (earthRadius - 50.0)
            };

            // This results in the Profile being of type ProfileConstant which has 2 radii and 1 piece
            // of data.
            try {

              Optional<GridNode> opt = gridNodeGenerator
                  .gridPointLatDegrees(GeoTessUtils.getLatDegrees(unitVec))
                  .gridPointLonDegrees(GeoTessUtils.getLonDegrees(unitVec))
                  .generate();

              GridNodeData gridNodeData = opt.isPresent() ? new GridNodeData(opt.get()) :
                  GridNodeData.EMPTY_GRID_NODE_DATA;

              model.setProfile(v, 0, Profile.newProfile(radii, new Data[]{ gridNodeData }));

            } catch (GeoTessException | GenerationException e) {
              throw new RuntimeException(e);
            }
          }

          return null;
        }
      });

      start = endVertex;
    }

    if (numThreads == 1) {
      // Do a direct call
      workers.get(0).call();
    } else {
      // Create a threadpool and submit all the workers.
      ExecutorService threadPool = Executors.newFixedThreadPool(numThreads);
      try {
        // invokeAll() doesn't return until they're all finished.
        List<Future<Void>> futures = threadPool.invokeAll(workers);
      } finally {
        // Send a request for the threadpool to gracefully shutdown.
        threadPool.shutdown();
      }
    }

    return System.currentTimeMillis() - startMs;
  }

  /**
   * Utility method for making a small grid out of a large one.
   */
  public static GeoTessGrid smallGridOutOfLargeGrid(GeoTessGrid largeGrid, int maxVertices)
      throws GeoTessException, IOException {

    int[][] triangles = largeGrid.getTriangles();
    int maxTriangles = -1;
    for (int t = 0; t < triangles.length; t++) {
      if (Collections.max(Arrays.asList(ArrayUtils.toObject(triangles[t]))) > maxVertices) {
        maxTriangles = t;
        break;
      }
    }

    assertTrue(maxTriangles >= 0);

    int[][] levels = largeGrid.getLevels();

    int topLevel = -1;
    for (int level = levels.length - 1; level >= 0; level--) {
      if (levels[level][1] <= maxTriangles) {
        topLevel = level;
        break;
      }
    }

    assertTrue(topLevel >= 0);

    int[][] tessellations = largeGrid.getTessellations();
    int topTessellation = -1;
    for (int tess = tessellations.length - 1; tess >= 0; tess--) {
      if (tessellations[tess][0] <= topLevel && tessellations[tess][1] > topLevel) {
        topTessellation = tess;
        break;
      }
    }

    assertTrue(topTessellation >= 0);

    final int numLevelsSmall = topLevel + 1;
    final int numTessSmall = topTessellation + 1;

    int[][] newTessellations = new int[numTessSmall][2];
    for (int tess = 0; tess < numTessSmall; tess++) {
      newTessellations[tess][0] = tessellations[tess][0];
      newTessellations[tess][1] = (tess < (numTessSmall - 1) ? tessellations[tess][1]
          : numLevelsSmall);
    }

    int[][] newLevels = new int[numLevelsSmall][2];
    for (int level = 0; level < numLevelsSmall; level++) {
      newLevels[level][0] = levels[level][0];
      newLevels[level][1] = levels[level][1];
    }

    assertEquals(0, newLevels[0][0]);

    final int numTrianglesSmall = newLevels[numLevelsSmall - 1][1];

    int[][] newTriangles = new int[numTrianglesSmall][3];

    int minVertexIndex = Integer.MAX_VALUE;
    int maxVertexIndex = Integer.MIN_VALUE;

    for (int t = 0; t < numTrianglesSmall; t++) {
      newTriangles[t][0] = triangles[t][0];
      newTriangles[t][1] = triangles[t][1];
      newTriangles[t][2] = triangles[t][2];
      int min = Collections.min(Arrays.asList(ArrayUtils.toObject(newTriangles[t])));
      int max = Collections.max(Arrays.asList(ArrayUtils.toObject(newTriangles[t])));
      if (min < minVertexIndex) {
        minVertexIndex = min;
      }
      if (max > maxVertexIndex) {
        maxVertexIndex = max;
      }
    }

    assertEquals(0, minVertexIndex);

    final int numVertices = maxVertexIndex + 1;

    double[][] newVertices = new double[numVertices][3];
    double[][] vertices = largeGrid.getVertices();

    for (int v = 0; v < numVertices; v++) {
      System.arraycopy(vertices[v], 0, newVertices[v], 0, 3);
    }

    GeoTessGrid smallGrid = new GeoTessGrid(newTessellations, newLevels, newTriangles, newVertices);

    return smallGrid;
  }

}