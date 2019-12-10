package gms.core.signaldetection.association.plugins.implementations.globalgrid;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.PhaseType;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.InformationSource;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.association.commonobjects.GridNode;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceStation;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.StationType;
import gms.shared.utilities.geotess.Data;
import gms.shared.utilities.geotess.GeoTessGrid;
import gms.shared.utilities.geotess.Profile;
import gms.shared.utilities.geotess.ProfileThin;
import gms.shared.utilities.signalfeaturepredictionutility.SignalFeaturePredictionUtility;
import java.io.File;
import java.net.URL;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class TesseractModelPopulatorTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(TesseractModelPopulatorTest.class);

  @Test
  void testBuild() throws TesseractModelPopulatorException {

    final double gridCylinderHeightKm = 100.0;
    final double gridCircleRadiusDegrees = 10.0;
    final double minimumMagnitude = 1.0;
    final Set<PhaseType> phaseTypes = Set.of(PhaseType.P, PhaseType.S);
    final Set<ReferenceStation> stations = Set.of(ReferenceStation.create(
        "MKAR",
        "Never put bread up your nose ...",
        StationType.SeismicArray,
        InformationSource
            .create("Because even after ...", Instant.EPOCH, "you take it out ..."),
        "it still feels like it's in there.",
        0.0, 20.0, 0.0,
        Instant.EPOCH, Instant.EPOCH,
        List.of()
    ));
    final List<SignalFeaturePredictionUtility> predictionUtilities = List.of(
        new SignalFeaturePredictionUtility(),
        new SignalFeaturePredictionUtility()
    );

    TesseractModelPopulator.Builder builder = TesseractModelPopulator.newBuilder();

    assertThrows(IllegalStateException.class, () -> {
      builder.build();
    });

    // Load up a grid from the geotess area with many vertices.
    String modelUrl = "tesseract-models/geotess_grid_01000.geotess";

    builder.gridFile(modelUrl);

    assertThrows(IllegalStateException.class, () -> {
      builder.build();
    });

    builder.gridCylinderHeightKm(gridCylinderHeightKm);

    assertThrows(IllegalStateException.class, () -> {
      builder.build();
    });

    builder.gridCircleRadiusDegrees(gridCircleRadiusDegrees);

    assertThrows(IllegalStateException.class, () -> {
      builder.build();
    });

    builder.minimumMagnitude(minimumMagnitude);

    assertThrows(IllegalStateException.class, () -> {
      builder.build();
    });

    builder.phaseTypes(phaseTypes);

    assertThrows(IllegalStateException.class, () -> {
      builder.build();
    });

    builder.stations(stations);

    assertThrows(IllegalStateException.class, () -> {
      builder.build();
    });

    builder.predictionUtilities(predictionUtilities);

    TesseractModelPopulator tesseractModelBuilder = builder.build();

    assertEquals(gridCylinderHeightKm, tesseractModelBuilder.getGridCylinderHeightKm());
    assertEquals(gridCircleRadiusDegrees, tesseractModelBuilder.getGridCircleRadiusDegrees());
    assertEquals(minimumMagnitude, tesseractModelBuilder.getMinimumMagnitude());
    assertEquals(phaseTypes, new HashSet<>(tesseractModelBuilder.getPhaseTypes()));
    assertEquals(stations, new HashSet<>(tesseractModelBuilder.getStations()));
    assertEquals(predictionUtilities, tesseractModelBuilder.getPredictionUtilities());

    TesseractModelPopulator.Builder builder2 = TesseractModelPopulator.newBuilder();

    tesseractModelBuilder = builder2.gridFile(modelUrl)
        .predictionUtilities(predictionUtilities)
        .stations(stations)
        .phaseTypes(phaseTypes)
        .minimumMagnitude(minimumMagnitude)
        .gridCircleRadiusDegrees(gridCircleRadiusDegrees)
        .gridCylinderHeightKm(gridCylinderHeightKm)
        .build();

    assertEquals(gridCylinderHeightKm, tesseractModelBuilder.getGridCylinderHeightKm());
    assertEquals(gridCircleRadiusDegrees, tesseractModelBuilder.getGridCircleRadiusDegrees());
    assertEquals(minimumMagnitude, tesseractModelBuilder.getMinimumMagnitude());
    assertEquals(phaseTypes, new HashSet<>(tesseractModelBuilder.getPhaseTypes()));
    assertEquals(stations, new HashSet<>(tesseractModelBuilder.getStations()));
    assertEquals(predictionUtilities, tesseractModelBuilder.getPredictionUtilities());
  }

  @Test
  void testPopulateModel() throws Exception {

    GeoTessGrid smallGrid = GridNodeDataTest.loadGridAndShrink(
        "tesseract-models/geotess_grid_01000.geotess",
        12);

    final int numThreads = Runtime.getRuntime().availableProcessors();
    final List<SignalFeaturePredictionUtility> predictionUtilities = new ArrayList<>(numThreads);
    for (int i=0; i<numThreads; i++) {
      predictionUtilities.add(new SignalFeaturePredictionUtility());
    }

    TesseractModelPopulator tesseractModelPopulator = TesseractModelPopulator.newBuilder()
        .grid(smallGrid)
        .gridCylinderHeightKm(100.0)
        .gridCircleRadiusDegrees(10.0)
        .minimumMagnitude(0.1)
        .phaseTypes(Set.of(PhaseType.P))
        .stations(Set.of(ReferenceStation.create(
            "MKAR",
            "Never put bread up your nose ...",
            StationType.SeismicArray,
            InformationSource
                .create("Because even after ...", Instant.EPOCH, "you take it out ..."),
            "it still feels like it's in there.",
            0.0, 20.0, 0.0,
            Instant.EPOCH, Instant.EPOCH,
            List.of()
        )))
        .predictionUtilities(predictionUtilities)
        .build();

    long startMs = System.currentTimeMillis();
    TesseractModelGA model = tesseractModelPopulator.call();
    long msec = System.currentTimeMillis() - startMs;

    LOGGER.info(String.format("Grid contains %d vertices", smallGrid.getNVertices()));
    LOGGER.info(String.format("model population in %d msec", msec));

    final int nVertices = model.getNVertices();
    final int nLayers = model.getNLayers();

    assertEquals(smallGrid.getNVertices(), nVertices);
    assertEquals(1, nLayers);

    // For collecting the indexes of vertices without grid nodes.
    Set<Integer> emptyVertices = new HashSet<>();

    for (int v = 0; v < nVertices; v++) {
      Profile profile = model.getProfile(v, 0);
      assertNotNull(profile);
      assertTrue(profile instanceof ProfileThin);
      Data[] data = profile.getData();
      assertEquals(1, data.length);
      assertTrue(data[0] instanceof GridNodeData);
      // This also test TesseractModelGA.getGridNode()
      Optional<GridNode> opt = model.getGridNode(v, 0);
      if (!opt.isPresent()) {
        emptyVertices.add(v);
      }
    }

    // Write the model to a file then read it into a new model. Ascertain that
    // empty grid nodes were restored properly.
    File tempFile = File.createTempFile("tesseractModel", ".geotess");
    tempFile.deleteOnExit();

    model.writeModel(tempFile);

    // This also tests whether the read() method of GridNodeData properly returns
    // GridNodeData.EMPTY_GRID_NODE_DATA when it's supposed to.
    TesseractModelGA model2 = new TesseractModelGA(tempFile);

    assertEquals(nVertices, model2.getNVertices());
    assertEquals(nLayers, model2.getNLayers());

    for (int v = 0; v < nVertices; v++) {
      // Should be empty for the same ones as in model.
      Optional<GridNode> opt = model2.getGridNode(v, 0);
      if (opt.isPresent()) {
        assertFalse(emptyVertices.contains(v));
      } else {
        assertTrue(emptyVertices.contains(v));
      }
    }
  }

  @Test
  @Disabled
    // Ran this manually to ensure it passed, but don't want it holding up builds.
  void testPopulateModelWithExternalThreadPool() throws Exception {

    GeoTessGrid smallGrid = GridNodeDataTest.loadGridAndShrink(
        "tesseract-models/geotess_grid_01000.geotess",
        12);

    ExecutorService threadPool = Executors.newFixedThreadPool(6);

    try {
      TesseractModelPopulator tesseractModelBuilder = TesseractModelPopulator.newBuilder()
          .grid(smallGrid)
          .gridCylinderHeightKm(50.0)
          .gridCircleRadiusDegrees(10.0)
          .minimumMagnitude(0.1)
          .phaseTypes(Set.of(PhaseType.P))
          .stations(Set.of(ReferenceStation.create(
              "MKAR",
              "Never put bread up your nose ...",
              StationType.SeismicArray,
              InformationSource
                  .create("Because even after ...", Instant.EPOCH, "you take it out ..."),
              "it still feels like it's in there.",
              0.0, 20.0, 0.0,
              Instant.EPOCH, Instant.EPOCH,
              List.of()
          )))
          .predictionUtilities(List.of(
              new SignalFeaturePredictionUtility(),
              new SignalFeaturePredictionUtility(),
              new SignalFeaturePredictionUtility(),
              new SignalFeaturePredictionUtility(),
              new SignalFeaturePredictionUtility(),
              new SignalFeaturePredictionUtility()))
          .build();

      long startMs = System.currentTimeMillis();
      TesseractModelGA model = tesseractModelBuilder.call();
      long msec = System.currentTimeMillis() - startMs;

      LOGGER.info(String.format("model population in %d msec", msec));

      assertFalse(threadPool.isShutdown());

      final int nVertices = model.getNVertices();
      final int nLayers = model.getNLayers();

      assertEquals(smallGrid.getNVertices(), nVertices);
      assertEquals(1, nLayers);

      for (int v = 0; v < nVertices; v++) {
        Profile profile = model.getProfile(v, 0);
        assertNotNull(profile);
        Data[] data = profile.getData();
        assertEquals(1, data.length);
        assertTrue(data[0] instanceof GridNodeData);
      }

    } finally {
      threadPool.shutdownNow();
    }
  }

  @Test
  void testPopulateModelWithInvalidEarthModels() throws Exception {

    GeoTessGrid smallGrid = GridNodeDataTest.loadGridAndShrink(
        "tesseract-models/geotess_grid_01000.geotess",
        12);

    TesseractModelPopulator tesseractModelBuilder = TesseractModelPopulator.newBuilder()
        .grid(smallGrid)
        .gridCylinderHeightKm(100.0)
        .centerDepthKm(50.0)
        .gridCircleRadiusDegrees(10.0)
        .minimumMagnitude(0.1)
        .travelTimePredictionEarthModel("i_do_not_exist")
        .magnitudeAttenuationPredictionEarthModel("me_neither")
        .phaseTypes(Set.of(PhaseType.P))
        .stations(Set.of(ReferenceStation.create(
            "MKAR",
            "Never put bread up your nose ...",
            StationType.SeismicArray,
            InformationSource
                .create("Because even after ...", Instant.EPOCH, "you take it out ..."),
            "it still feels like it's in there.",
            0.0, 20.0, 0.0,
            Instant.EPOCH, Instant.EPOCH,
            List.of()
        )))
        .predictionUtilities(List.of(
            new SignalFeaturePredictionUtility(),
            new SignalFeaturePredictionUtility(),
            new SignalFeaturePredictionUtility(),
            new SignalFeaturePredictionUtility(),
            new SignalFeaturePredictionUtility(),
            new SignalFeaturePredictionUtility()))
        .build();

    assertThrows(TesseractModelPopulatorException.class, () -> {
      // Since the earth models made no sense, no gridnodes should have been computed.
      TesseractModelGA model = tesseractModelBuilder.call();
    });
  }
}