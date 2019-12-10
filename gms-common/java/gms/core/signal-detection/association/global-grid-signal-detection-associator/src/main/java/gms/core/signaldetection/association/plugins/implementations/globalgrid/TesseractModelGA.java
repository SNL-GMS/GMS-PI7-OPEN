package gms.core.signaldetection.association.plugins.implementations.globalgrid;

import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.association.commonobjects.GridNode;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.association.commonobjects.NodeStation;
import gms.shared.utilities.geotess.Data;
import gms.shared.utilities.geotess.GeoTessException;
import gms.shared.utilities.geotess.GeoTessGrid;
import gms.shared.utilities.geotess.GeoTessMetaData;
import gms.shared.utilities.geotess.GeoTessModel;
import gms.shared.utilities.geotess.Profile;
import gms.shared.utilities.geotess.ProfileThin;
import gms.shared.utilities.geotess.util.globals.OptimizationType;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * A specialized extension of {@code GeoTessModel} that stores {@code GridNodeData} instances,
 * an extension of {@code DataCustom} in the profiles associated with the vertices and layers.
 * Each such profile must contain only one {@code GridNodeData} which wraps a {@code GridNode}.
 */
public class TesseractModelGA extends GeoTessModel {

  /**
   * Keys are statiion ids. Values are sets of gridnodes than contain those stations.
   */
  private Map<UUID, Set<GridNode>> firstArrivalMap;

  protected TesseractModelGA(File geoTessGridFile, GeoTessMetaData geoTessMetaData)
      throws IOException {
    super(geoTessGridFile, geoTessMetaData);
  }

  public TesseractModelGA(GeoTessGrid grid, GeoTessMetaData metaData) throws GeoTessException {
    super(grid, metaData);
  }

  /**
   * Construct a new GeoTessModel object and populate it with information from the specified file.
   *
   * <p>
   * relativeGridPath is assumed to be "" (empty string), which is appropriate when the grid
   * information is stored in the same file as the model or when the grid is stored in a separate
   * file located in the same directory as the model file.
   *
   * <p>
   * OptimizationType will default to SPEED, as opposed to MEMORY. With OptimizationType.SPEED, the
   * code will execute more quickly but will require more memory to run.
   *
   * @param modelInputFile name of file containing the model.
   */
  public TesseractModelGA(File modelInputFile) throws IOException {
    super(modelInputFile, "", OptimizationType.SPEED);
  }

  /**
   * constructor using an InputStream
   * @param inputStream InputStream instance
   * @throws IOException
   * @throws GeoTessException
   */
  public TesseractModelGA(DataInputStream inputStream) throws IOException, GeoTessException {
    super(inputStream);
  }

  @Override
  protected void loadModelBinary(DataInputStream input,
      String inputDirectory, String relGridFilePath)
      throws GeoTessException, IOException {
    GeoTessMetaData.addCustomDataType(GridNodeData.getSingletonReader());
    super.loadModelBinary(input, inputDirectory, relGridFilePath);
  }

  @Override
  protected void loadModelAscii(Scanner input, String inputDirectory,
      String relGridFilePath) throws GeoTessException, IOException {
    GeoTessMetaData.addCustomDataType(GridNodeData.getSingletonReader());
    super.loadModelAscii(input, inputDirectory, relGridFilePath);
  }

  /**
   * Set the profile, but only accept profiles containing a single {@code GridNodeData} instance.
   * @param vertex
   * @param layer
   * @param profile
   * @throws GeoTessException
   * @throws IllegalArgumentException if the profile does not contain a single
   *   {@code GridNodeData} instance.
   */
  @Override
  public void setProfile(int vertex, int layer, Profile profile) throws GeoTessException {
    Data[] data = profile.getData();
    if (data.length != 1 || !(data[0] instanceof GridNodeData)) {
      throw new IllegalArgumentException(
          "profiles containing a single GridNodeData instance are required");
    }
    super.setProfile(vertex, layer, profile);
  }

  /**
   * Creates a Map from a Station UUID to a Set of GridNodes such that the Station is a
   * first-arrival Station for every GridNode in that Set.
   *
   * @param numFirstSta maximum size of the Set of GridNodes
   */
  public synchronized void initializeFirstArrivalMap(final int numFirstSta) {

    // Synchronized this method and getFirstArrivalMap() so firstArrivalMap() would block while
    // the map is being computed.

    final Map<UUID, Set<GridNode>> map = new HashMap<>();
    final int nVertices = getNVertices();
    final int nLayers = getNLayers();

    for (int v = 0; v < nVertices; v++) {
      for (int l = 0; l < nLayers; l++) {
        Optional<GridNode> opt = getGridNode(v, l);

        opt.ifPresent(gridNode -> {
          gridNode.getNodeStations()
              .stream()
              .limit(numFirstSta)
              .forEach(station -> {
                map.compute(station.getStationId(), (id, set) -> {
                  if (set == null) {
                    set = new HashSet<>();
                  }
                  set.add(gridNode);
                  return set;
                });
              });
        });
      }
    }

    this.firstArrivalMap = map;
  }

  /**
   * Get the grid node for the specified vertex and layer.
   * @param vertex
   * @param layer
   * @return an optional wrapping the grid node, or empty if a grid node could not be
   *   computed for the vertex and layer.
   */
  public Optional<GridNode> getGridNode(int vertex, int layer) {
    Profile profile = getProfile(vertex, layer);
    if (profile != null) {
      // Don't need to check the type here, because the setProfile() method
      // enforces these rules.
      GridNodeData gridNodeData = (GridNodeData) profile.getData()[0];
      // When the model is populated, all vertices get a grid node data, but ones for which
      // the grid node couldn't be computed use the sentinal EMPTY_GRID_NODE_DATA.
      if (gridNodeData != GridNodeData.EMPTY_GRID_NODE_DATA) {
        return Optional.of(gridNodeData.getGridNode());
      }
    }
    return Optional.empty();
  }

  /**
   * Synchronized method to obtain the first arrival map. Since synchronized, do not call within
   * a loop. Call it once outside of the loop.
   * @return
   */
  public synchronized Map<UUID, Set<GridNode>> getFirstArrivalMap() {
    return firstArrivalMap;
  }
}
