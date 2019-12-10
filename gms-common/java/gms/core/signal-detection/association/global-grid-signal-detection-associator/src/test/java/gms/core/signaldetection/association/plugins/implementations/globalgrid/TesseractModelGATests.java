package gms.core.signaldetection.association.plugins.implementations.globalgrid;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.association.commonobjects.GridNode;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.association.commonobjects.NodeStation;
import gms.shared.utilities.geotess.Data;
import gms.shared.utilities.geotess.GeoTessMetaData;
import gms.shared.utilities.geotess.Profile;
import gms.shared.utilities.geotess.ProfileThin;
import gms.shared.utilities.geotess.util.globals.DataType;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;


public class TesseractModelGATests {

  private static NodeStation nodeStationMock1 = Mockito.mock(NodeStation.class);
  private static NodeStation nodeStationMock2 = Mockito.mock(NodeStation.class);
  private static NodeStation nodeStationMock3 = Mockito.mock(NodeStation.class);
  private static GridNode gridNodeMock1 = Mockito.mock(GridNode.class);
  private static GridNode gridNodeMock2 = Mockito.mock(GridNode.class);
  private static ProfileThin profileThinMock11 = Mockito.mock(ProfileThin.class);
  private static ProfileThin profileThinMock21 = Mockito.mock(ProfileThin.class);

  @BeforeAll

  private static void init() {

    // setup for Station #1

    TreeSet<NodeStation> treeSet1 = new TreeSet<>();
    treeSet1.add(nodeStationMock1);
    treeSet1.add(nodeStationMock2);
    treeSet1.add(nodeStationMock3);
    Mockito.when(gridNodeMock1.getNodeStations()).thenReturn(treeSet1);

    GridNodeData gridNodeDataMock1 = Mockito.mock(GridNodeData.class);
    Mockito.when(gridNodeDataMock1.getGridNode()).thenReturn(gridNodeMock1);

    Data[] dataArray1 = new Data[]{gridNodeDataMock1};
    Mockito.when(profileThinMock11.getData()).thenReturn(dataArray1);

    // setup for Station #2

    TreeSet<NodeStation> treeSet2 = new TreeSet<>();
    treeSet2.add(nodeStationMock1);
    Mockito.when(gridNodeMock2.getNodeStations()).thenReturn(treeSet2);

    GridNodeData gridNodeDataMock2 = Mockito.mock(GridNodeData.class);
    Mockito.when(gridNodeDataMock2.getGridNode()).thenReturn(gridNodeMock2);

    Data[] dataArray2 = new Data[]{gridNodeDataMock2};
    Mockito.when(profileThinMock21.getData()).thenReturn(dataArray2);

    // setup for node ids

    Mockito.when(nodeStationMock1.getStationId()).thenReturn(UUID.randomUUID());
    Mockito.when(nodeStationMock2.getStationId()).thenReturn(UUID.randomUUID());
  }

  private TesseractModelGA getTesseractModelGAWithBehavior(File geoTessFile,
      GeoTessMetaData geoTessMetaData) throws IOException {
    return new TesseractModelGA(geoTessFile, geoTessMetaData) {

      @Override
      public int getNVertices() {
        return 2;
      }

      @Override
      public int getNLayers() {
        return 1;
      }

      @Override
      public Profile getProfile(int vertex, int layer) {
        if (vertex == 1) {
          return profileThinMock11;
        }
        return profileThinMock21;
      }

    };
  }

  @Test
  public void testInitializeFirstArrivalMap() throws Exception {
    // Create a map with two stations. Station #1 maps to a set containing Nodes #1 and #2.
    // Station #2 maps to a set containing Node #1 only.

    // define geoTessFile
    File geoTessFile = new File(Thread.currentThread().getContextClassLoader()
        .getResource("tesseract-models/geotess_grid_01000.geotess").getFile());

    // define geoTessMetaData
    GeoTessMetaData geoTessMetaData = new GeoTessMetaData();
    geoTessMetaData.setAttributes("data names", "none");
    geoTessMetaData.setDataType(DataType.CUSTOM);
    geoTessMetaData.setModelSoftwareVersion("0.0.0");
    geoTessMetaData.setLayerNames("layer names");

    // create the map
    TesseractModelGA tesseractModelGAMock = this
        .getTesseractModelGAWithBehavior(geoTessFile, geoTessMetaData);
    tesseractModelGAMock.initializeFirstArrivalMap(2);
    Map<UUID, Set<GridNode>> map = tesseractModelGAMock.getFirstArrivalMap();

    Assertions.assertTrue(map.get(nodeStationMock1.getStationId()).contains(gridNodeMock1));
    Assertions.assertTrue(map.get(nodeStationMock1.getStationId()).contains(gridNodeMock2));

    Assertions.assertTrue(map.get(nodeStationMock2.getStationId()).contains(gridNodeMock1));

  }

  @Test
  void testReadPopulatedModelFromResource() throws Exception {

    InputStream is = this.getClass().getResourceAsStream(
        "/tesseract-models/geotess_grid_populated_geotess_grid_08000.geotess"
    );

    assertNotNull(is);

    try (BufferedInputStream bis = new BufferedInputStream(is);
         DataInputStream dis = new DataInputStream(bis)) {
      TesseractModelGA model = new TesseractModelGA(dis);
      assertNotNull(model);
    }
  }

}
