package gms.core.signaldetection.association.testdata;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.PhaseType;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.association.commonobjects.GridNode;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.association.commonobjects.NodeStation;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.association.commonobjects.PhaseInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FeatureMeasurement;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FeatureMeasurementTypes;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.InstantValue;
import java.time.Duration;
import java.time.Instant;
import java.util.TreeSet;
import java.util.UUID;


/**
 * The objects in this class are consistent with each other in the sense that together, they may be
 * used to build other objects (e.g., a CandidateEvent) without the constructor throwing an
 * exception.
 */
public class TestData {

  public static FeatureMeasurement<InstantValue> arrivalTimeFeatureMeasurement = FeatureMeasurement
      .create(
          UUID.randomUUID(),
          FeatureMeasurementTypes.ARRIVAL_TIME,
          InstantValue.from(Instant.EPOCH, Duration.ZERO));

  public static UUID stationId1 = UUID.randomUUID();
  public static UUID stationId2 = UUID.randomUUID();

  public static PhaseInfo phaseInfo = PhaseInfo.builder()
      .setPhaseType(PhaseType.P)
      .setPrimary(true)
      .setTravelTimeSeconds(1.0)
      .setAzimuthDegrees(1.0)
      .setBackAzimuthDegrees(1.0)
      .setTravelTimeMinimum(1.0)
      .setTravelTimeMaximum(1.0)
      .setRadialTravelTimeDerivative(1.0)
      .setVerticalTravelTimeDerivative(1.0)
      .setSlownessCellWidth(1.0)
      .setSlowness(1.0)
      .setMinimumMagnitude(1.0)
      .setMagnitudeCorrection(1.0)
      .setRadialMagnitudeCorrectionDerivative(1.0)
      .setVerticalMagnitudeCorrectionDerivative(1.0)
      .build();

  public static NodeStation nodeStation1 = NodeStation.builder()
      .setId(UUID.randomUUID())
      .setStationId(stationId1)
      .setDistanceFromGridPointDegrees(5.0)
      .setPhaseInfos(new TreeSet<>() {{
        add(phaseInfo);
      }})
      .build();

  public static NodeStation nodeStation2 = NodeStation.builder()
      .setId(UUID.randomUUID())
      .setStationId(stationId2)
      .setDistanceFromGridPointDegrees(5.1)
      .setPhaseInfos(new TreeSet<>() {{
        add(phaseInfo);
      }})
      .build();

  public static GridNode gridNode1 = GridNode.builder()
      .setId(UUID.randomUUID())
      .setCenterLatitudeDegrees(1.0)
      .setCenterLongitudeDegrees(1.0)
      .setCenterDepthKm(1.0)
      .setGridCellHeightKm(1.0)
      .setNodeStations(new TreeSet<>() {{
        add(nodeStation1);
      }})
      .build();

  public static GridNode gridNode2 = GridNode.builder()
      .setId(UUID.randomUUID())
      .setCenterLatitudeDegrees(1.1)
      .setCenterLongitudeDegrees(1.1)
      .setCenterDepthKm(1.2)
      .setGridCellHeightKm(1.3)
      .setNodeStations(new TreeSet<>() {{
        add(nodeStation2);
      }})
      .build();

}
