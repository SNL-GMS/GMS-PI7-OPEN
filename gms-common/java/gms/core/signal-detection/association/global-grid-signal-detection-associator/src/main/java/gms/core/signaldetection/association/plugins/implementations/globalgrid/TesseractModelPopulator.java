package gms.core.signaldetection.association.plugins.implementations.globalgrid;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.PhaseType;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.association.commonobjects.GridNode;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceStation;
import gms.shared.utilities.geotess.Data;
import gms.shared.utilities.geotess.GeoTessException;
import gms.shared.utilities.geotess.GeoTessGrid;
import gms.shared.utilities.geotess.GeoTessMetaData;
import gms.shared.utilities.geotess.GeoTessUtils;
import gms.shared.utilities.geotess.Profile;
import gms.shared.utilities.geotess.util.numerical.vector.VectorGeo;
import gms.shared.utilities.javautilities.generation.GenerationException;
import gms.shared.utilities.signalfeaturepredictionutility.SignalFeaturePredictionUtility;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Instances of {@code TesseractModelPopulator} are used to create
 * populated instances of {@code TesseractModelGA}. This class
 * implements {@code Callable<TesseractModelGA} because populating a
 * new {@code TesseractModelGA} with {@code GridNodeData} objects is
 * generally a very time-consuming process. To speed the process,
 * supply a list of {@code SignalFeaturePredictionUtility} instances
 * equal to the number of threads desired to populate the model.
 */
public class TesseractModelPopulator implements Callable<TesseractModelGA> {

  private static final Logger LOGGER = LoggerFactory.getLogger(TesseractModelPopulator.class);

  private final GeoTessGrid grid;
  private final List<ReferenceStation> stations;
  private final double gridCircleRadiusDegrees;
  private final List<PhaseType> phaseTypes;
  private final double minimumMagnitude;
  private final double centerDepthKm;
  private final double gridCylinderHeightKm;
  private final List<SignalFeaturePredictionUtility> predictionUtilities;
  private final String layerName;
  private final String travelTimePredictionEarthModel;
  private final String magnitudeAttenuationPredictionEarthModel;
  private final ExecutorService threadPool;

  /**
   * Private constructor -- use the builder.
   * @param grid
   * @param predictionUtilities
   * @param stations
   * @param phaseTypes
   * @param gridCircleRadiusDegrees
   * @param minimumMagnitude
   * @param centerDepthKm
   * @param gridCylinderHeightKm
   * @param layerName
   * @param travelTimePredictionEarthModel
   * @param magnitudeAttenuationPredictionEarthModel
   * @param threadPool
   */
  private TesseractModelPopulator(
      GeoTessGrid grid,
      List<SignalFeaturePredictionUtility> predictionUtilities,
      Set<ReferenceStation> stations,
      Set<PhaseType> phaseTypes,
      double gridCircleRadiusDegrees,
      double minimumMagnitude,
      double centerDepthKm,
      double gridCylinderHeightKm,
      String layerName,
      String travelTimePredictionEarthModel,
      String magnitudeAttenuationPredictionEarthModel,
      ExecutorService threadPool
  ) {
    this.grid = grid;
    this.predictionUtilities = predictionUtilities;
    this.stations = new ArrayList<>(stations);
    this.gridCircleRadiusDegrees = gridCircleRadiusDegrees;
    this.phaseTypes = new ArrayList<>(phaseTypes);
    this.minimumMagnitude = minimumMagnitude;
    this.centerDepthKm = centerDepthKm;
    this.gridCylinderHeightKm = gridCylinderHeightKm;
    this.layerName = layerName;
    this.travelTimePredictionEarthModel = travelTimePredictionEarthModel;
    this.magnitudeAttenuationPredictionEarthModel = magnitudeAttenuationPredictionEarthModel;
    this.threadPool = threadPool;
  }

  /**
   * Returns a new builder used to instantiate an instance of
   * {@code TesseractModelPopulator}
   * @return
   */
  public static Builder newBuilder() {
    return new Builder();
  }

  /**
   * Instantiates a new {@code TesseractModelGA} instance and
   * populates it with {@code GridNodeData} objects, one for each
   * vertex. The number of threads used by this time-consuming call
   * is determined by the length of the prediction utilities list
   * supplied to the builder.
   *
   * @return
   * @throws TesseractModelPopulatorException
   */
  @Override
  public TesseractModelGA call() throws TesseractModelPopulatorException {

    // Before the model can be instantiated, must instantiate the metadata

    // You have to set this before calling
    // metaData.setDataType(GridNodeData.getSingletonReader().getDataTypeString()) so
    // the singleton reader will be in the static map.
    GeoTessMetaData.addCustomDataType(GridNodeData.getSingletonReader());

    // Create empty GeoTessMetaData
    GeoTessMetaData metaData = new GeoTessMetaData();


    TesseractModelGA tmpModel = null;

    // Populate mandatory attributes in GeoTessMetaData
    try {

      // The number of names has to be the same as the number of units. These are fairly
      // meaningless, but they have to be the same length.
      metaData.setAttributes(new String[]{"gridNodeData"}, new String[]{"gridNodeData"});
      // One layer using the highest tessellation in the grid
      metaData.setLayerNames(layerName);
      metaData.setLayerTessIds(new int[]{grid.getTessellations().length - 1});
      metaData.setModelSoftwareVersion("1.0.0");

      // Don't set it to DataType.CUSTOM, but to the data type string returned by the
      // custom data type set above. Yes, this is weird. Since it doesn't translate to a
      // DataType enum, it generates an exception that sets the data type to DataType.CUSTOM and
      // loads the customDataInitializer from a static map.
      metaData.setDataType(GridNodeData.getSingletonReader().getDataTypeString());

      tmpModel = new TesseractModelGA(this.grid, metaData);

    } catch (IOException|GeoTessException e) {

      throw new TesseractModelPopulatorException("error configuring metadata", e);

    }

    // Need it to be a final for the workers to use it.
    final TesseractModelGA model = tmpModel;

    final int nVertices = model.getNVertices();

    // For keeping track of the number of vertices to which TesseractModelGA.EMPTY_GRID_NODE_DATA
    // is assigned.
    final AtomicInteger emptyVertexCount = new AtomicInteger();

    // No use having more threads than vertices.
    final int nThreads = Math.min(this.predictionUtilities.size(), nVertices);

    final int[] verticesPerThread = new int[nThreads];
    Arrays.fill(verticesPerThread, nVertices/nThreads);

    // In case nVertices is not cleanly divisible by nThreads.
    final int leftOver = nVertices%nThreads;
    for (int i=0; i<leftOver; i++) {
      verticesPerThread[i]++;
    }

    List<Callable<Void>> workers = new ArrayList<>(nThreads);
    int startVertex = 0;

    for (int i=0; i<nThreads; i++) {

      final SignalFeaturePredictionUtility signalFeaturePredictionUtility =
          this.predictionUtilities.get(i);
      final int sv = startVertex;
      final int ev = sv + verticesPerThread[i];

      workers.add(new Callable<Void> () {

        @Override
        public Void call() throws TesseractModelPopulatorException {

          // Set up a grid node generator with all parameters set but the latitude and
          // longitude.
          GridNodeGenerator gridNodeGenerator = new GridNodeGenerator()
              .gridPointDepthKm(centerDepthKm)
              .gridCylinderHeightKm(gridCylinderHeightKm)
              .gridCylinderRadiusDegrees(gridCircleRadiusDegrees)
              .minimumMagnitude(minimumMagnitude)
              .gridCylinderRadiusDegrees(gridCircleRadiusDegrees)
              .gridCylinderHeightKm(gridCylinderHeightKm)
              .referenceStations(stations)
              .phaseTypes(phaseTypes)
              .predictionUtility(signalFeaturePredictionUtility)
              .travelTimePredictionEarthModel(travelTimePredictionEarthModel)
              .magnitudeAttenuationPredictionEarthModel(magnitudeAttenuationPredictionEarthModel);

          for (int v=sv; v<ev; v++) {

            // Get the unit vector for the vertex
            double[] unitVec = model.getGrid().getVertex(v);
            // Compute the earth radius at that vector location
            double earthRadius = VectorGeo.getEarthRadius(unitVec);
            // Create a radius gridPointDepthKm below the surface.
            float[] radii = new float[] { (float) (earthRadius - centerDepthKm) };

            // This results in the Profile being of type ProfileThin which has 1 radius
            // and 1 piece of data.
            Optional<GridNode> opt = null;
            try {
              opt = gridNodeGenerator
                  .gridPointLatDegrees(GeoTessUtils.getLatDegrees(unitVec))
                  .gridPointLonDegrees(GeoTessUtils.getLonDegrees(unitVec))
                  .generate();
            } catch (GenerationException e) {
              throw new TesseractModelPopulatorException(
                  "error generating gridnode for vertex " + v , e);
            }

            GridNodeData gridNodeData = null;

            if (opt.isPresent()) {
              gridNodeData = new GridNodeData(opt.get());
            } else {
              // Can't have a null profile and save to a file, so play the sentinal vallue in
              // the profile. getGridNode() of TesseractModelGA will return an empty optional
              // for this node.
              gridNodeData = GridNodeData.EMPTY_GRID_NODE_DATA;
              emptyVertexCount.incrementAndGet();
            }

            try {
              model.setProfile(v, 0,
                  Profile.newProfile(radii, new Data[] { gridNodeData }));
            } catch (GeoTessException e) {
              throw new TesseractModelPopulatorException(
                  "error setting gridNodeData in profile of vertex " + v,
                  e);
            }

          }

          SignalFeaturePredictionUtility.freeThreadSpecificPlugins();

          return null;
        }

      });

      startVertex = ev;
    }

    List<Future<Void>> futures = null;

    if (nThreads == 1) {
      // Single-threaded case.
      if (this.threadPool != null) {
        try {
          // But if the threadPool has been set, use it.
          futures = this.threadPool.invokeAll(workers);
        } catch (InterruptedException e) {
          throw new TesseractModelPopulatorException(
              "interruption during population of TesseractModelGA");
        }
      } else {
        try {
          // Otherwise, make a direct call.
          workers.get(0).call();
        } catch (Exception e) {
          if (e instanceof TesseractModelPopulatorException) {
            throw (TesseractModelPopulatorException) e;
          } else {
            throw new TesseractModelPopulatorException("error populating TesseractModelGA", e);
          }
        }
      }
    } else {
      // Multi-threaded case.
      boolean doShutdown = this.threadPool == null;
      ExecutorService tp = this.threadPool != null ? this.threadPool :
          Executors.newFixedThreadPool(nThreads);
      try {
        // This blocks until all workers have finished. Any exceptions thrown by the workers
        // are suppressed until calling get() on the futures.
        futures = tp.invokeAll(workers);
      } catch (InterruptedException e) {
        throw new TesseractModelPopulatorException(
            "interruption during population of TesseractModelGA");
      } finally {
        // Only call shutdown() on the thread pool if it's one instantiated here. Don't call
        // shutdown on one that was passed in. It may be used for other things!
        if (doShutdown) {
          // Clean up the thread pool.
          tp.shutdown();
        }
      }
    }

    // If the futures are non-null from using a threadPool, iterate through them to detect problems.
    //
    if (futures != null) {

      // The 1st populator exception from one of the workers. If multiple workers
      // throw one, only the first is thrown by this method.
      TesseractModelPopulatorException populatorException = null;
      // If no workers throw populator exceptions, but something else goes wrong,
      // set this and then throw it as a the cause of a populator exception.
      Throwable cause = null;

      // futures.size() should be nThreads.
      for (int i=0; i<futures.size(); i++) {
        try {
          // This will trigger a propagation of the exception thrown by the callable wrapped
          // by an ExecutionException
          futures.get(i).get();
        } catch (InterruptedException e) {
          // Won't happen since invokeAll() was used. But handle anyway.
          throw new TesseractModelPopulatorException(
              "interruption during population of TesseractModelGA");
        } catch (ExecutionException e) {
          // Log a message. If multiple workers fail, at least we'll see the log message from
          // all of them. But only throw the exception from the first.
          LOGGER.error("error populating TesseractModelGA on thread " + i, e);
          if (e.getCause() instanceof TesseractModelPopulatorException) {
            if (populatorException == null) {
              populatorException = (TesseractModelPopulatorException) e.getCause();
            }
          } else { // Shouldn't happen, but trap just in case.
            if (cause == null ) {
              cause = e.getCause();
            }
          }
        }
      }

      if (populatorException != null) {
        throw populatorException;
      } else if (cause != null) {
        throw new TesseractModelPopulatorException("error populating TesseractModelGA", cause);
      }
    }

    // This probably indicates something silly such as using an earth model that doesn't exist.
    if (emptyVertexCount.get() == nVertices) {
      throw new TesseractModelPopulatorException(
          "gridnode data could not be computed for any vertices"
      );
    }

    return model;
  }

  public GeoTessGrid getGrid() {
    return grid;
  }

  public List<ReferenceStation> getStations() {
    return stations;
  }

  public double getGridCircleRadiusDegrees() {
    return gridCircleRadiusDegrees;
  }

  public List<PhaseType> getPhaseTypes() {
    return phaseTypes;
  }

  public double getMinimumMagnitude() {
    return minimumMagnitude;
  }

  public List<SignalFeaturePredictionUtility> getPredictionUtilities() {
    return predictionUtilities;
  }

  public double getCenterDepthKm() {
    return centerDepthKm;
  }

  public String getLayerName() {
    return layerName;
  }

  public String getTravelTimePredictionEarthModel() {
    return travelTimePredictionEarthModel;
  }

  public String getMagnitudeAttenuationPredictionEarthModel() {
    return magnitudeAttenuationPredictionEarthModel;
  }

  public ExecutorService getThreadPool() {
    return threadPool;
  }

  public double getGridCylinderHeightKm() {
    return gridCylinderHeightKm;
  }

  /**
   * Builder for the TesseractModelPopulator.
   */
  public static class Builder {

    private String gridFile;
    private GeoTessGrid grid;
    private Set<ReferenceStation> stations;
    private double gridCircleRadiusDegrees = Double.NaN;
    private Set<PhaseType> phaseTypes;
    private double minimumMagnitude = Double.NaN;
    private double gridCylinderHeightKm = 100.0;
    private double centerDepthKm = 50.0;
    private List<SignalFeaturePredictionUtility> predictionUtilities;
    private String layerName = "CRUST";
    private String travelTimePredictionEarthModel = "ak135";
    private String magnitudeAttenuationPredictionEarthModel = "VeithClawson72";
    private ExecutorService threadPool;

    private Builder() {}

    public Builder gridFile(String gridFile) {
      Objects.requireNonNull(gridFile, "Null gridFile");
      this.gridFile = gridFile;
      return this;
    }

    public Builder grid(GeoTessGrid grid) {
      Objects.requireNonNull(grid, "Null grid");
      this.grid = grid;
      return this;
    }

    public Builder stations(Set<ReferenceStation> stations) {
      Objects.requireNonNull(stations, "Null stations");
      Validate.notEmpty(stations, "Empty stations");
      this.stations = stations;
      return this;
    }

    public Builder gridCircleRadiusDegrees(double gridCircleRadiusDegrees) {
      if (Double.isNaN(gridCircleRadiusDegrees) || gridCircleRadiusDegrees <= 0.0) {
        throw new IllegalArgumentException("invalid gridCircleRadiusDegrees: " +
            gridCircleRadiusDegrees);
      }
      this.gridCircleRadiusDegrees = gridCircleRadiusDegrees;
      return this;
    }

    public Builder phaseTypes(Set<PhaseType> phaseTypes) {
      Objects.requireNonNull(phaseTypes, "Null phaseTypes");
      Validate.notEmpty(phaseTypes, "Empty phaseTypes");
      this.phaseTypes = phaseTypes;
      return this;
    }

    public Builder minimumMagnitude(double minimumMagnitude) {
      if (Double.isNaN(minimumMagnitude) || minimumMagnitude < 0.0) {
        throw new IllegalArgumentException("invalid minimumMagnitude: " + minimumMagnitude);
      }
      this.minimumMagnitude = minimumMagnitude;
      return this;
    }

    public Builder gridCylinderHeightKm(double gridCylinderHeightKm) {
      if (Double.isNaN(gridCylinderHeightKm) || gridCylinderHeightKm < 0.0) {
        throw new IllegalArgumentException("invalid gridCylinderHeightKm: " + gridCylinderHeightKm);
      }
      this.gridCylinderHeightKm = gridCylinderHeightKm;
      return this;
    }

    public Builder centerDepthKm(double centerDepthKm) {
      if (Double.isNaN(centerDepthKm) || centerDepthKm < 0.0) {
        throw new IllegalArgumentException("invalid gridPointDepthKm: " + centerDepthKm);
      }
      this.centerDepthKm = centerDepthKm;
      return this;
    }

    public Builder predictionUtilities(List<SignalFeaturePredictionUtility> predictionUtilities) {
      Objects.requireNonNull(predictionUtilities, "Null predictionUtilities");
      Validate.notEmpty(predictionUtilities, "Empty predictionUtilities");
      this.predictionUtilities = predictionUtilities;
      return this;
    }

    public Builder layerName(String layerName) {
      Objects.requireNonNull(layerName, "Null layerName");
      Validate.notEmpty(layerName, "Empty layerName");
      this.layerName = layerName;
      return this;
    }

    public Builder travelTimePredictionEarthModel(String travelTimePredictionEarthModel) {
      Objects.requireNonNull(travelTimePredictionEarthModel,
          "Null travelTimePredictionEarthModel");
      Validate.notEmpty(travelTimePredictionEarthModel,
          "Empty travelTimePredictionEarthModel");
      this.travelTimePredictionEarthModel = travelTimePredictionEarthModel;
      return this;
    }

    public Builder magnitudeAttenuationPredictionEarthModel(
        String magnitudeAttenuationPredictionEarthModel) {
      Objects.requireNonNull(magnitudeAttenuationPredictionEarthModel,
          "Null magnitudeAttenuationPredictionEarthModel");
      Validate.notEmpty(magnitudeAttenuationPredictionEarthModel,
          "Empty magnitudeAttenuationPredictionEarthModel");
      this.magnitudeAttenuationPredictionEarthModel = magnitudeAttenuationPredictionEarthModel;
      return this;
    }

    /**
     * For setting a thread pool ({@code ExecutorService}) to use for populating the model.
     * If no thread pool is set, a temporary one will be instantiated and used for populating
     * the model.
     * @param threadPool
     * @return
     */
    public Builder threadPool(ExecutorService threadPool) {
      this.threadPool = threadPool;
      return this;
    }

    /**
     * Build a new instantiation of {@code TesseractModelPopulator}
     * @return a new instance of {@code TesseractModelPopulator}
     * @throws IllegalStateException if required fields have not been set
     *   or if settings do not make sense in combination.
     * @throws TesseractModelPopulatorException if a grid cannot be loaded
     *   from a grid file.
     */
    public TesseractModelPopulator build() throws TesseractModelPopulatorException {

      String[] errors = checkFields();

      // If required args have not been supplied or they do not make sense in combination, that's
      // a programming error. The builder is in an invalid state for the operation, so
      // an IllegalStateException is appropriate.
      if (errors.length > 0) {
        throw new IllegalStateException(
            "required fields have not been set: " +
                String.join(", ", errors)
        );
      }

      if (grid == null && gridFile != null) {
        // Attempt to load GeoTessGrid
        try {
          grid = new GeoTessGrid();
          InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(gridFile);
          grid.loadGrid(gridFile, is);
        } catch (IOException e) {
          throw new TesseractModelPopulatorException(
              String.format("Failed to load grid \"%s\"", gridFile), e);
        }
      }

      return new TesseractModelPopulator(
          grid,
          predictionUtilities,
          stations,
          phaseTypes,
          gridCircleRadiusDegrees,
          minimumMagnitude,
          centerDepthKm,
          gridCylinderHeightKm,
          layerName,
          travelTimePredictionEarthModel,
          magnitudeAttenuationPredictionEarthModel,
          threadPool
      );
    }

    /**
     * Checks that everything has been set prior to a build.
     * @return an array of error messages for the fields that have not been set.
     */
    private String[] checkFields() {
      List<String> errorList = new ArrayList<>();
      if (gridFile == null && grid == null) {
        errorList.add("neither gridFile or grid set");
      }
      if (stations == null) {
        errorList.add("stations not set");
      }
      if (Double.isNaN(gridCircleRadiusDegrees)) {
        errorList.add("gridCircleRadiusDegress not set");
      }
      if (phaseTypes == null) {
        errorList.add("phaseTypes not set");
      }
      if (Double.isNaN(minimumMagnitude)) {
        errorList.add("minimumMagnitude not set");
      }
      if (predictionUtilities == null) {
        errorList.add("predictionUtilities not set");
      }
      return errorList.toArray(new String[errorList.size()]);
    }
  }
}
