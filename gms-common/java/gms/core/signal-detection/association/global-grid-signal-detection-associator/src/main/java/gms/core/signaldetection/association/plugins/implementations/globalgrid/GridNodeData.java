package gms.core.signaldetection.association.plugins.implementations.globalgrid;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.PhaseType;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.association.commonobjects.GridNode;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.association.commonobjects.NodeStation;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.association.commonobjects.PhaseInfo;
import gms.shared.utilities.geotess.AttributeDataDefinitions;
import gms.shared.utilities.geotess.Data;
import gms.shared.utilities.geotess.DataCustom;
import gms.shared.utilities.geotess.GeoTessUtils;
import gms.shared.utilities.geotess.util.globals.DataType;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Writer;
import java.util.Objects;
import java.util.Scanner;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;

/**
 * {@code GridNodeData} is a subclass of {@code DataCustom} that wraps an instance of {@code
 * GridNode} so it can be stored in a {@code GeoTessModel} profile. The goodies in this class
 * cannot be accessed via the usual {@code Data} methods such as {@code getDouble(n)}, but must
 * be extracted from the wrapped {@code GridNode} which is available via {@code getGridNode()}.
 */
public class GridNodeData extends DataCustom {

  private static final GridNodeData SINGLETON_READER = new GridNodeData();

  /**
   * A {@code GridNodeData} instance that holds nothing of value. Used as a placeholder
   * when a grid node cannot be computed.
   */
  public static final GridNodeData EMPTY_GRID_NODE_DATA = new GridNodeData(
      GridNode.from(
          new UUID(0L, 0L),
          Double.NaN,
          Double.NaN,
          Double.NaN,
          Double.NaN,
          new TreeSet<>())
  );

  private GridNode gridNode;

  /**
   * Constructor
   *
   * @param gridNode the {@code GridNode} to be wrapped, which must not be null.
   */
  public GridNodeData(GridNode gridNode) {
    Objects.requireNonNull(gridNode, "gridNode must not be null");
    this.gridNode = gridNode;
  }

  /**
   * Only used by the singleton instance which reads data from an input stream.
   */
  private GridNodeData() {
  }

  /**
   * Get the {@code GridNode} holding all the important data.
   */
  public GridNode getGridNode() {
    return gridNode;
  }

  /**
   * Returns a singleton instance which can be used for instantiating new instances from an input
   * stream. This instance itself holds a null grid node.
   */
  public static GridNodeData getSingletonReader() {
    return SINGLETON_READER;
  }

  @Override
  public Data getNew() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Data[] getNew(int n) {
    throw new UnsupportedOperationException();
  }

  /**
   * Since this is a data custom extension, this is what to set the data type to
   * in the {@code GeoTessMetaData}. Do not set it to {@code Data.CUSTOM} even though
   * that is what it ends up being. And, before doing that, call the static method
   * in {@code AttributeDataDefinitions} to register the singleton reader as
   * a data custom initializer. (Yes, it is convoluted.)
   * @return
   */
  @Override
  public String getDataTypeString() {
    return this.getClass().getName();
  }

  @Override
  public DataType getDataType() {
    return DataType.CUSTOM;
  }

  /**
   * The only attribute is the grid node itself.
   * @return
   */
  @Override
  public int size() {
    return 1;
  }

  @Override
  public double getDouble(int attributeIndex) {
    return Double.NaN;
  }

  @Override
  public float getFloat(int attributeIndex) {
    return (float) getDouble(attributeIndex);
  }

  @Override
  public long getLong(int attributeIndex) {
    return Math.round(getDouble(attributeIndex));
  }

  @Override
  public int getInt(int attributeIndex) {
    return (int) getLong(attributeIndex);
  }

  @Override
  public short getShort(int attributeIndex) {
    return (short) getLong(attributeIndex);
  }

  @Override
  public byte getByte(int attributeIndex) {
    return (byte) getLong(attributeIndex);
  }

  @Override
  public Data setValue(int attributeIndex, double value) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Data setValue(int attributeIndex, float value) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Data setValue(int attributeIndex, long value) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Data setValue(int attributeIndex, int value) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Data setValue(int attributeIndex, short value) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Data setValue(int attributeIndex, byte value) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Data fill(Number fillValue) {
    throw new UnsupportedOperationException();
  }

  @Override
  public GridNodeData read(Scanner input, AttributeDataDefinitions attrDef) {
    // If it's equal to the empty grid node UUID, just return
    // the static instance.
    GridNode gridNode = readGridNode(input);
    if (Objects.equals(EMPTY_GRID_NODE_DATA.gridNode.getId(), gridNode.getId())) {
      return EMPTY_GRID_NODE_DATA;
    }
    return new GridNodeData(gridNode);
  }

  @Override
  public void write(Writer writer) throws IOException {
    writeGridNode(this.gridNode, writer);
  }

  @Override
  public GridNodeData read(DataInputStream input, AttributeDataDefinitions attrDef)
      throws IOException {
    // If it's equal to the empty grid node UUID, just return
    // the static instance.
    GridNode gridNode = readGridNode(input);
    if (Objects.equals(EMPTY_GRID_NODE_DATA.gridNode.getId(), gridNode.getId())) {
      return EMPTY_GRID_NODE_DATA;
    }
    return new GridNodeData(gridNode);
  }

  @Override
  public void write(DataOutputStream output) throws IOException {
    writeGridNode(this.gridNode, output);
  }

  private static GridNode readGridNode(Scanner input) {
    String uuidString = input.nextLine();
    UUID uuid = uuidString.isEmpty() ? null : UUID.fromString(input.nextLine());
    double centerLatitudeDegrees = input.nextDouble();
    input.nextLine();
    double centerLongitudeDegrees = input.nextDouble();
    input.nextLine();
    double centerDepthKm = input.nextDouble();
    input.nextLine();
    double gridCellHeightKm = input.nextDouble();
    input.nextLine();
    int numNodeStations = input.nextInt();
    SortedSet<NodeStation> nodeStations = new TreeSet<>();
    for (int i = 0; i < numNodeStations; i++) {
      nodeStations.add(readNodeStation(input));
    }
    return GridNode.from(uuid, centerLatitudeDegrees, centerLongitudeDegrees,
        centerDepthKm, gridCellHeightKm, nodeStations);
  }

  private static void writeGridNode(GridNode gridNode, Writer writer) throws IOException {
    String idString = gridNode.getId() != null ? gridNode.getId().toString() : "";
    writer.write(idString + GeoTessUtils.NL);
    writer.write(String.valueOf(gridNode.getCenterLatitudeDegrees()) + GeoTessUtils.NL);
    writer.write(String.valueOf(gridNode.getCenterLongitudeDegrees()) + GeoTessUtils.NL);
    writer.write(String.valueOf(gridNode.getCenterDepthKm()) + GeoTessUtils.NL);
    writer.write(String.valueOf(gridNode.getGridCellHeightKm()) + GeoTessUtils.NL);
    Set<NodeStation> nodeStations = gridNode.getNodeStations();
    final int numNodeStations = nodeStations.size();
    writer.write(String.valueOf(numNodeStations) + GeoTessUtils.NL);
    for (NodeStation nodeStation : nodeStations) {
      writeNodeStation(nodeStation, writer);
    }
  }

  private static NodeStation readNodeStation(Scanner input) {
    String idString = input.nextLine();
    UUID id = idString.isEmpty() ? null : UUID.fromString(idString);
    idString = input.nextLine();
    UUID stationId = idString.isEmpty() ? null : UUID.fromString(input.nextLine());
    double distanceFromGridPointDegrees = input.nextDouble();
    input.nextLine();
    int numPhaseInfos = input.nextInt();
    input.nextLine();
    SortedSet<PhaseInfo> phaseInfos = new TreeSet<>();
    for (int i = 0; i < numPhaseInfos; i++) {
      phaseInfos.add(readPhaseInfo(input));
    }
    return NodeStation.from(id, stationId, distanceFromGridPointDegrees, phaseInfos);
  }

  private static void writeNodeStation(NodeStation nodeStation, Writer writer) throws IOException {
    writer.write((nodeStation.getId() != null ? nodeStation.getId().toString() : "")
        + GeoTessUtils.NL);
    writer.write(
        (nodeStation.getStationId() != null ? nodeStation.getStationId().toString() : "")
            + GeoTessUtils.NL);
    writer.write(String.valueOf(nodeStation.getDistanceFromGridPointDegrees()) + GeoTessUtils.NL);
    SortedSet<PhaseInfo> phaseInfos = nodeStation.getPhaseInfos();
    writer.write(String.valueOf(phaseInfos.size()) + GeoTessUtils.NL);
    for (PhaseInfo phaseInfo : phaseInfos) {
      writePhaseInfo(phaseInfo, writer);
    }
  }

  private static PhaseInfo readPhaseInfo(Scanner input) {
    PhaseType phaseType = PhaseType.valueOf(input.nextLine());
    boolean primary = input.nextBoolean();
    input.nextLine();
    double travelTimeSeconds = input.nextDouble();
    input.nextLine();
    double azimuthDegrees = input.nextDouble();
    input.nextLine();
    double backAzimuthDegrees = input.nextDouble();
    input.nextLine();
    double travelTimeMinimum = input.nextDouble();
    input.nextLine();
    double travelTimeMaximum = input.nextDouble();
    input.nextLine();
    double radialTravelTimeDerivative = input.nextDouble();
    input.nextLine();
    double verticalTravelTimeDerivative = input.nextDouble();
    input.nextLine();
    double slownessCellWidth = input.nextDouble();
    input.nextLine();
    double slowness = input.nextDouble();
    input.nextLine();
    double minimumMagnitude = input.nextDouble();
    input.nextLine();
    double magnitudeCorrection = input.nextDouble();
    input.nextLine();
    double radialMagnitudeCorrectionDerivative = input.nextDouble();
    input.nextLine();
    double verticalMagnitudeCorrectionDerivative = input.nextDouble();
    input.nextLine();
    return PhaseInfo.from(
        phaseType, primary, travelTimeSeconds, azimuthDegrees, backAzimuthDegrees,
        travelTimeMinimum, travelTimeMaximum, radialTravelTimeDerivative,
        verticalTravelTimeDerivative, slownessCellWidth, slowness, minimumMagnitude,
        magnitudeCorrection, radialMagnitudeCorrectionDerivative,
        verticalMagnitudeCorrectionDerivative);

  }

  private static void writePhaseInfo(PhaseInfo phaseInfo, Writer writer) throws IOException {
    writer.write(phaseInfo.getPhaseType().toString() + GeoTessUtils.NL);
    writer.write(String.valueOf(phaseInfo.isPrimary()) + GeoTessUtils.NL);
    writer.write(String.valueOf(phaseInfo.getTravelTimeSeconds()) + GeoTessUtils.NL);
    writer.write(String.valueOf(phaseInfo.getAzimuthDegrees()) + GeoTessUtils.NL);
    writer.write(String.valueOf(phaseInfo.getBackAzimuthDegrees()) + GeoTessUtils.NL);
    writer.write(String.valueOf(phaseInfo.getTravelTimeMinimum()) + GeoTessUtils.NL);
    writer.write(String.valueOf(phaseInfo.getTravelTimeMaximum()) + GeoTessUtils.NL);
    writer.write(String.valueOf(phaseInfo.getRadialTravelTimeDerivative()) + GeoTessUtils.NL);
    writer.write(String.valueOf(phaseInfo.getVerticalTravelTimeDerivative()) + GeoTessUtils.NL);
    writer.write(String.valueOf(phaseInfo.getSlownessCellWidth()) + GeoTessUtils.NL);
    writer.write(phaseInfo.getSlowness() + GeoTessUtils.NL);
    writer.write(String.valueOf(phaseInfo.getMinimumMagnitude()) + GeoTessUtils.NL);
    writer.write(String.valueOf(phaseInfo.getMagnitudeCorrection()) + GeoTessUtils.NL);
    writer.write(
        String.valueOf(phaseInfo.getRadialMagnitudeCorrectionDerivative()) + GeoTessUtils.NL);
    writer.write(
        String.valueOf(phaseInfo.getVerticalMagnitudeCorrectionDerivative()) + GeoTessUtils.NL);
  }

  private static GridNode readGridNode(DataInputStream input) throws IOException {
    String idString = GeoTessUtils.readString(input);
    UUID uuid = idString.isEmpty() ? null : UUID.fromString(idString);
    double centerLatitudeDegrees = input.readDouble();
    double centerLongitudeDegrees = input.readDouble();
    double centerDepthKm = input.readDouble();
    double gridCellHeightKm = input.readDouble();
    int numNodeStations = input.readInt();
    SortedSet<NodeStation> nodeStations = new TreeSet<>();
    for (int i = 0; i < numNodeStations; i++) {
      nodeStations.add(readNodeStation(input));
    }
    return GridNode.from(uuid, centerLatitudeDegrees, centerLongitudeDegrees,
        centerDepthKm, gridCellHeightKm, nodeStations);
  }

  private static void writeGridNode(GridNode gridNode, DataOutputStream output) throws IOException {
    GeoTessUtils.writeString(output, gridNode.getId() != null ? gridNode.getId().toString() : "");
    output.writeDouble(gridNode.getCenterLatitudeDegrees());
    output.writeDouble(gridNode.getCenterLongitudeDegrees());
    output.writeDouble(gridNode.getCenterDepthKm());
    output.writeDouble(gridNode.getGridCellHeightKm());
    Set<NodeStation> nodeStations = gridNode.getNodeStations();
    output.writeInt(nodeStations.size());
    for (NodeStation nodeStation : nodeStations) {
      writeNodeStation(nodeStation, output);
    }
  }

  private static NodeStation readNodeStation(DataInputStream input) throws IOException {
    UUID id = UUID.fromString(GeoTessUtils.readString(input));
    UUID stationId = UUID.fromString(GeoTessUtils.readString(input));
    double distanceFromGridPointDegrees = input.readDouble();
    int numPhaseInfos = input.readInt();
    SortedSet<PhaseInfo> phaseInfos = new TreeSet<>();
    for (int i = 0; i < numPhaseInfos; i++) {
      phaseInfos.add(readPhaseInfo(input));
    }
    return NodeStation.from(id, stationId, distanceFromGridPointDegrees, phaseInfos);
  }

  private static void writeNodeStation(NodeStation nodeStation, DataOutputStream output)
      throws IOException {
    String idString = nodeStation.getId() != null ? nodeStation.getId().toString() : "";
    GeoTessUtils.writeString(output, idString);
    idString = nodeStation.getStationId() != null ? nodeStation.getStationId().toString() : "";
    GeoTessUtils.writeString(output, idString);
    output.writeDouble(nodeStation.getDistanceFromGridPointDegrees());
    SortedSet<PhaseInfo> phaseInfos = nodeStation.getPhaseInfos();
    output.writeInt(phaseInfos.size());
    for (PhaseInfo phaseInfo : phaseInfos) {
      writePhaseInfo(phaseInfo, output);
    }
  }

  private static PhaseInfo readPhaseInfo(DataInputStream input) throws IOException {
    PhaseType phaseType = PhaseType.valueOf(GeoTessUtils.readString(input));
    boolean primary = input.readBoolean();
    double travelTimeSeconds = input.readDouble();
    double azimuthDegrees = input.readDouble();
    double backAzimuthDegrees = input.readDouble();
    double travelTimeMinimum = input.readDouble();
    double travelTimeMaximum = input.readDouble();
    double radialTravelTimeDerivative = input.readDouble();
    double verticalTravelTimeDerivative = input.readDouble();
    double slownessCellWidth = input.readDouble();
    double slowness = input.readDouble();
    double minimumMagnitude = input.readDouble();
    double magnitudeCorrection = input.readDouble();
    double radialMagnitudeCorrectionDerivative = input.readDouble();
    double verticalMagnitudeCorrectionDerivative = input.readDouble();
    return PhaseInfo.from(
        phaseType, primary, travelTimeSeconds, azimuthDegrees, backAzimuthDegrees,
        travelTimeMinimum, travelTimeMaximum, radialTravelTimeDerivative,
        verticalTravelTimeDerivative, slownessCellWidth, slowness, minimumMagnitude,
        magnitudeCorrection, radialMagnitudeCorrectionDerivative,
        verticalMagnitudeCorrectionDerivative);
  }

  private static void writePhaseInfo(PhaseInfo phaseInfo, DataOutputStream output)
      throws IOException {
    GeoTessUtils.writeString(output, phaseInfo.getPhaseType().toString());
    output.writeBoolean(phaseInfo.isPrimary());
    output.writeDouble(phaseInfo.getTravelTimeSeconds());
    output.writeDouble(phaseInfo.getAzimuthDegrees());
    output.writeDouble(phaseInfo.getBackAzimuthDegrees());
    output.writeDouble(phaseInfo.getTravelTimeMinimum());
    output.writeDouble(phaseInfo.getTravelTimeMaximum());
    output.writeDouble(phaseInfo.getRadialTravelTimeDerivative());
    output.writeDouble(phaseInfo.getVerticalTravelTimeDerivative());
    output.writeDouble(phaseInfo.getSlownessCellWidth());
    output.writeDouble(phaseInfo.getSlowness());
    output.writeDouble(phaseInfo.getMinimumMagnitude());
    output.writeDouble(phaseInfo.getMagnitudeCorrection());
    output.writeDouble(phaseInfo.getRadialMagnitudeCorrectionDerivative());
    output.writeDouble(phaseInfo.getVerticalMagnitudeCorrectionDerivative());
  }

  @Override
  public Data copy() {
    return new GridNodeData(this.gridNode);
  }

  @Override
  public String toString() {
    return "GridNodeData(" + String.valueOf(this.gridNode) + ")";
  }

  @Override
  public int hashCode() {
    return this.gridNode.hashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (o != null && o.getClass() == this.getClass()) {
      GridNodeData that = (GridNodeData) o;
      return this.gridNode.equals(that.gridNode);
    }
    return false;
  }
}
