package gms.core.signaldetection.association;

import gms.core.signaldetection.association.plugins.SdhStationAssociation;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.PhaseType;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.association.commonobjects.GridNode;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.association.commonobjects.NodeStation;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.association.commonobjects.PhaseInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FeatureMeasurement;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FeatureMeasurementTypes;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.InstantValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.NumericMeasurementValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetectionHypothesis;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceStation;
import gms.shared.utilities.geomath.GeoMath;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * A driver (SDH, station, and gridNode) with a set of corroborating SDHs
 */
public class CandidateEvent {

  private UUID stationId;
  private GridNode gridNode;
  private SignalDetectionHypothesis driverSdh;
  private double sigmaTime;
  private Set<SignalDetectionHypothesis> corroboratingSet;
  private Instant arrivalTime;
  private Instant originTime;


  private CandidateEvent(
      UUID stationId,
      GridNode gridNode,
      SignalDetectionHypothesis driverSdh,
      double sigmaTime,
      Set<SignalDetectionHypothesis> corroboratingSet)
      throws MissingFeatureMeasurementException, MissingNodeStationException, UnexpectedPhasesException {
    this.stationId = stationId;
    this.gridNode = gridNode;
    this.driverSdh = driverSdh;
    this.sigmaTime = sigmaTime;
    this.corroboratingSet = corroboratingSet;

    this.arrivalTime = retrieveArrivalTime();
    this.originTime = computeOriginTime(arrivalTime);
  }


  /**
   * Creates an instance of a CandidateEvent
   *
   * @param stationId UUID of the station at which the driver SDH was received
   * @param gridNode driver GridNode
   * @param driverSdh driver SignalDetectionHypothesis
   * @param sigmaTime multiplier for difference in travel time from near to far side of gridNode
   * @param corroboratingSet set of corroborating SignalDetectionHypothesis
   * @return a CandidateEvent
   */
  public static CandidateEvent from(
      UUID stationId,
      GridNode gridNode,
      SignalDetectionHypothesis driverSdh,
      double sigmaTime,
      Set<SignalDetectionHypothesis> corroboratingSet)
      throws MissingFeatureMeasurementException, MissingNodeStationException, UnexpectedPhasesException {

    Objects.requireNonNull(stationId, "Cannot create a CandidateEvent from a null stationId");
    Objects.requireNonNull(gridNode, "Cannot create a CandidateEvent from a null gridNode");
    Objects.requireNonNull(driverSdh, "Cannot create a CandidateEvent from a null driverSdh");
    if (sigmaTime < 0.0) {
      throw new IllegalArgumentException(
          "Expecting sigmaTime >= 0.0.  Saw sigmaTime = " + sigmaTime);
    }
    Objects.requireNonNull(corroboratingSet,
        "Cannot create a CandidateEvent from a null corroboratingSet");

    return new CandidateEvent(stationId, gridNode, driverSdh, sigmaTime, corroboratingSet);
  }

  /**
   * Creates an instance of a CandidateEvent with an empty corroborating set of SDHs
   *
   * @param stationId UUID of the station at which the driver SDH was received
   * @param gridNode driver GridNode
   * @param driverSdh driver SignalDetectionHypothesis
   * @param sigmaTime multiplier for difference in travel time from near to far side of gridNode
   * @return a CandidateEvent
   */
  public static CandidateEvent from(
      UUID stationId,
      GridNode gridNode,
      SignalDetectionHypothesis driverSdh,
      double sigmaTime)
      throws MissingFeatureMeasurementException, MissingNodeStationException, UnexpectedPhasesException {
    return CandidateEvent.from(stationId, gridNode, driverSdh, sigmaTime, new HashSet<>());
  }


  /**
   * Simple screening of potential corroborating SDHs based on arrival time
   *
   * @param association the SignalDetectionHypothesis to test against this driver
   * @return true if SignalDetectionHypothesis passes screening.  false otherwise.
   */
  public boolean passesTravelTimeConstraintCheck(final SdhStationAssociation association)
      throws MissingFeatureMeasurementException, MissingNodeStationException, UnexpectedPhasesException {

    // retrieve observed arrival time
    Optional<FeatureMeasurement<InstantValue>> optionalArrival = association
        .getSignalDetectionHypothesis()
        .getFeatureMeasurement(FeatureMeasurementTypes.ARRIVAL_TIME);
    if (!optionalArrival.isPresent()) {
      throw new MissingFeatureMeasurementException(
          "SignalDetectionHypothesis Arrival Time value missing.");
    }
    Instant observedArrival = optionalArrival.get().getMeasurementValue().getValue();

    // retrieve theoretical travel time from gridNode to SDH station
    NodeStation nodeStation = gridNode
        .getNodeStations()
        .stream()
        .filter(ns -> ns.getStationId().equals(association.getReferenceStation().getVersionId()))
        .findFirst()
        .orElseThrow(() -> new MissingNodeStationException(association.getReferenceStation().getVersionId()));
    List<PhaseInfo> phaseInfos = nodeStation
        .getPhaseInfos()
        .stream()
        .filter(phaseInfo -> phaseInfo.getPhaseType() == PhaseType.P)
        .collect(Collectors.toList());
    if (phaseInfos.size() != 1) {
      throw new UnexpectedPhasesException(
          "Expected exactly one P phase.  Saw " + phaseInfos.size() + ".");
    }
    double travelTimeSeconds = phaseInfos.get(0).getTravelTimeSeconds();
    Duration theoreticalTravelTime = Duration.of(Math.round(travelTimeSeconds), ChronoUnit.SECONDS);

    Instant theoreticalArrival = originTime.plus(theoreticalTravelTime);

    // consider only SDHs with arrival times later than the driver's arrival time
    if (observedArrival.isBefore(arrivalTime)) {
      return false;
    }

    double residual = (theoreticalArrival.getEpochSecond() - observedArrival.getEpochSecond()) +
        1.0e-9 * (theoreticalArrival.getNano() - observedArrival.getNano());

    // retrieve maximum travel time across grid node
    double crossGridTravelTime =
        phaseInfos.get(0).getTravelTimeMaximum() - phaseInfos.get(0).getTravelTimeMinimum();

    // retrieve error in observed arrival time
    Duration stddev = optionalArrival.get().getMeasurementValue().getStandardDeviation();
    double deltim = stddev.getSeconds() + 1.0e-9 * stddev.getNano();

    double upperLimit = crossGridTravelTime + sigmaTime * deltim;

    return residual * residual < upperLimit * upperLimit;
  }

  /**
   * Simple screening of driver events based on slowness vector
   *
   * @param referenceStation the station at which the SDH was detected
   * @param sdh candidate driver SignalDetectionHypothesis
   * @param gridNode gridNode to test as a possible driver for this SDH
   * @return true if SDH/gridNode pair pass slowness vector screening.  false otherwise.
   */
  public static boolean passesSlownessConstraintCheck(
      ReferenceStation referenceStation,
      SignalDetectionHypothesis sdh,
      double sigmaSlowness,
      GridNode gridNode
  ) throws UnexpectedPhasesException {

    // retrieve theoretical slowness
    final Optional<NodeStation> optionalNodeStation = gridNode
        .getNodeStations()
        .stream()
        .filter(nodeStation -> nodeStation.getStationId().equals(referenceStation.getVersionId()))
        .findFirst();
    if (!optionalNodeStation.isPresent()) {
      return false;
    }
    final List<PhaseInfo> phaseInfos = optionalNodeStation
        .get()
        .getPhaseInfos()
        .stream()
        .filter(phaseInfo -> phaseInfo.getPhaseType() == PhaseType.P)
        .collect(Collectors.toList());
    if (phaseInfos.size() != 1) {
      throw new UnexpectedPhasesException(
          "Expecting exactly one P phase.  Saw " + phaseInfos.size() + ".");
    }
    final double slowT = phaseInfos.get(0).getSlowness();

    // retrieve observed slowness
    final Optional<FeatureMeasurement<NumericMeasurementValue>> optionalSlowO = sdh
        .getFeatureMeasurement(FeatureMeasurementTypes.SLOWNESS);
    if (!optionalSlowO.isPresent()) {
      return false;
    }
    final double slowO = optionalSlowO.get().getMeasurementValue().getMeasurementValue().getValue();

    // retrieve theoretical azimuth
    final double azT = GeoMath
        .azimuth(referenceStation.getLatitude(), referenceStation.getLongitude(),
            gridNode.getCenterLatitudeDegrees(),
            gridNode.getCenterLongitudeDegrees());

    // retrieve observed azimuth
    Optional<FeatureMeasurement<NumericMeasurementValue>> optionalAzO = sdh
        .getFeatureMeasurement(FeatureMeasurementTypes.RECEIVER_TO_SOURCE_AZIMUTH);
    if (!optionalAzO.isPresent()) {
      return false;
    }
    final double azO = optionalAzO.get().getMeasurementValue().getMeasurementValue().getValue();

    // Using Law of Cosines to find the magnitude of the difference between
    // the theoretical and observed slowness vectors
    final double magDiffSquared =
        slowT * slowT + slowO * slowO - 2.0 * slowT * slowO * Math.cos(Math.toRadians(azT - azO));

    // The maximum difference in slowness from the center of
    // the grid node to any point in the grid node
    final double deltaCell = phaseInfos.get(0).getSlownessCellWidth();

    // slowness error in the observed slowness
    final double delslo = optionalSlowO
        .get()
        .getMeasurementValue()
        .getMeasurementValue()
        .getStandardDeviation();

    final double upperLimit = deltaCell + sigmaSlowness * delslo;

    return magDiffSquared <= upperLimit * upperLimit;
  }

  /**
   * Retrieve the arrive time of the driverSdh at the station associated with the driver.
   *
   * @return the arrival time
   */
  private Instant retrieveArrivalTime() throws MissingFeatureMeasurementException {

    Optional<FeatureMeasurement<InstantValue>> optionalArrival = driverSdh
        .getFeatureMeasurement(FeatureMeasurementTypes.ARRIVAL_TIME);
    if (!optionalArrival.isPresent()) {
      throw new MissingFeatureMeasurementException("Arrival Time value missing.");
    }

    return optionalArrival.get().getMeasurementValue().getValue();
  }


  /**
   * Compute the origin time of the event in the driverSdh.  Origin time is computed as the
   * driverSdh arrival time minus the travel time from the gridNode to the station associated with
   * the driverSdh.
   *
   * @return the origin time
   */
  private Instant computeOriginTime(Instant arrival)
      throws MissingNodeStationException, UnexpectedPhasesException {

    // retrieve travel time
    NodeStation nodeStation = gridNode
        .getNodeStations()
        .stream()
        .filter(ns -> ns.getStationId().equals(stationId))
        .findFirst()
        .orElseThrow(() -> new MissingNodeStationException(stationId));
    List<PhaseInfo> phaseInfos = nodeStation
        .getPhaseInfos()
        .stream()
        .filter(phaseInfo -> phaseInfo.getPhaseType() == PhaseType.P)
        .collect(Collectors.toList());
    if (phaseInfos.size() != 1) {
      throw new UnexpectedPhasesException(
          "Expected exactly one P phase.  Saw " + phaseInfos.size() + ".");
    }
    double travelTimeSeconds = phaseInfos.get(0).getTravelTimeSeconds();
    Duration travelTime = Duration.of(Math.round(travelTimeSeconds), ChronoUnit.SECONDS);

    return arrival.minus(travelTime);
  }


  public UUID getStationId() {
    return stationId;
  }

  public GridNode getGridNode() {
    return gridNode;
  }

  public SignalDetectionHypothesis getDriverSdh() {
    return driverSdh;
  }

  public double getSigmaTime() {
    return sigmaTime;
  }

  public Set<SignalDetectionHypothesis> getCorroboratingSet() {
    return corroboratingSet;
  }

  public Instant getOriginTime() {
    return originTime;
  }

}
