package gms.core.featureprediction.service;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.PhaseType;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.processingcontrol.commonobjects.ProcessingContext;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.processingcontrol.commonobjects.StorageVisibility;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.EllipticityCorrection1dDefinition;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.EventLocation;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.LocationRestraint;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.LocationSolution;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Channel;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FeatureMeasurementTypes;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Location;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ChannelDataType;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ChannelType;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class TestFixtures {

  public static StreamingFeaturePredictionsForSourceAndReceiverLocations
      streamingFeaturePredictionsForSourceAndReceiverLocationsArrivalTimeEdgeCase =
      StreamingFeaturePredictionsForSourceAndReceiverLocations.from(
          List.of(FeatureMeasurementTypes.ARRIVAL_TIME),
          EventLocation.from(0.0, 0.0, 90.0, Instant.EPOCH),
          List.of(
              Location.from(0.0, 113.726363, 0.0, 0.6273),  // high spike
              Location.from(0.0, 113.125695, 0.0, 0.6273)),  // Nans
          PhaseType.P,
          "ak135",
          new ArrayList<>(),
          ProcessingContext.createAutomatic(
              UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
              StorageVisibility.PUBLIC));

  public static StreamingFeaturePredictionsForSourceAndReceiverLocations
      streamingFeaturePredictionsForSourceAndReceiverLocationsArrivalTime =
      StreamingFeaturePredictionsForSourceAndReceiverLocations.from(
          List.of(FeatureMeasurementTypes.ARRIVAL_TIME, FeatureMeasurementTypes.SLOWNESS),
          EventLocation.from(90.0, 80.0, 70.0, Instant.EPOCH),
          List.of(
              Location.from(10.0, 10.0, 10.0, 10.0),
              Location.from(20.0, 20.0, 20.0, 20.0),
              Location.from(30.0, 30.0, 30.0, 30.0)),
          PhaseType.P,
          "ak135",
          new ArrayList<>(),
          ProcessingContext.createAutomatic(
              UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
              StorageVisibility.PUBLIC));

  public static StreamingFeaturePredictionsForSourceAndReceiverLocations
      streamingFeaturePredictionsForSourceAndReceiverLocationsSlowness =
      StreamingFeaturePredictionsForSourceAndReceiverLocations.from(
          List.of(FeatureMeasurementTypes.SLOWNESS),
          EventLocation.from(90.0, 80.0, 70.0, Instant.EPOCH),
          List.of(
              Location.from(10.0, 10.0, 10.0, 10.0),
              Location.from(20.0, 20.0, 20.0, 20.0),
              Location.from(30.0, 30.0, 30.0, 30.0)),
          PhaseType.P,
          "ak135",
          new ArrayList<>(),
          ProcessingContext.createAutomatic(
              UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
              StorageVisibility.PUBLIC));

  public static StreamingFeaturePredictionsForSourceAndReceiverLocations
      featureCoi =
      StreamingFeaturePredictionsForSourceAndReceiverLocations.from(
          List.of(FeatureMeasurementTypes.SLOWNESS),
          EventLocation.from(10.0, 110.0, 70.0, Instant.EPOCH),
          List.of(
              Location.from(-23.665134, 133.905261, 0.0, 0.6273)),
          PhaseType.P,
          "ak135",
          new ArrayList<>(),
          ProcessingContext.createAutomatic(
              UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
              StorageVisibility.PUBLIC));

  public static StreamingFeaturePredictionsForSourceAndReceiverLocations
      featureCoi2 =
      StreamingFeaturePredictionsForSourceAndReceiverLocations.from(
          List.of(FeatureMeasurementTypes.ARRIVAL_TIME),
          EventLocation.from(10.0, 110.0, 70.0, Instant.EPOCH),
          List.of(
              Location.from(-23.665134, 133.905261, 0.0, 0.6273)),
          PhaseType.P,
          "ak135",
          List.of(EllipticityCorrection1dDefinition.create()),
          ProcessingContext.createAutomatic(
              UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
              StorageVisibility.PUBLIC));

  /**
   * All distances should be out of range [5 - 95]. These are intentionally out of range in order to
   * trigger an OutOfRangeException in the ellipcity correction. This results in a FeaturePrediction
   * value of NaN
   */
  public static StreamingFeaturePredictionsForSourceAndReceiverLocations
      streamingFeaturePredictionsForSlownessDistanceOutOfRange =
      StreamingFeaturePredictionsForSourceAndReceiverLocations.from(
          List.of(FeatureMeasurementTypes.ARRIVAL_TIME, FeatureMeasurementTypes.SLOWNESS),
          EventLocation.from(90.0, 80.0, 9999999.0, Instant.EPOCH),
          List.of(
              // Distance of 0.0
              Location.from(90.0, 80.0, 0.0, 0.0),
              // Distance of 125
              Location.from(-35.0, -100.0, 0.0, 0.0)),
          PhaseType.P,
          "ak135",
          Arrays.asList(EllipticityCorrection1dDefinition.create()),
          ProcessingContext.createAutomatic(
              UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
              StorageVisibility.PUBLIC));

  /**
   * DepthKm for the event location should be in range [0 - 700], but this is intentionally out of
   * range to trigger an OutOfRangeException during the ellipticity correction.
   */
  public static StreamingFeaturePredictionsForSourceAndReceiverLocations
      streamingFeaturePredictionsForArrivalTimeDepthKmOutOfRange =
      StreamingFeaturePredictionsForSourceAndReceiverLocations.from(
          List.of(FeatureMeasurementTypes.ARRIVAL_TIME, FeatureMeasurementTypes.SLOWNESS),
          // Just out of range.
          EventLocation.from(90.0, 80.0, 701.0, Instant.EPOCH),
          List.of(
              Location.from(10.0, 10.0, 10.0, 10.0),
              Location.from(20.0, 20.0, 20.0, 20.0),
              Location.from(30.0, 30.0, 30.0, 30.0)),
          PhaseType.P,
          "ak135",
          Arrays.asList(EllipticityCorrection1dDefinition.create()),
          ProcessingContext.createAutomatic(
              UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
              StorageVisibility.PUBLIC));

  public static StreamingFeaturePredictionsForLocationSolutionAndChannel
      streamingFeaturePredictionsForLocationSolutionAndChannel =
      StreamingFeaturePredictionsForLocationSolutionAndChannel.from(
          List.of(FeatureMeasurementTypes.ARRIVAL_TIME, FeatureMeasurementTypes.SLOWNESS),
          LocationSolution.withLocationAndRestraintOnly(
              EventLocation.from(90.0, 80.0, 701.0, Instant.EPOCH),
              new LocationRestraint.Builder().build()
          ),
          List.of(Channel.create(
              "x",
              ChannelType.HIGH_BROADBAND_HIGH_GAIN_VERTICAL,
              ChannelDataType.HYDROACOUSTIC_ARRAY,
              1.1,
              1.2,
              1.3,
              1.4,
              1.5,
              1.6,
              1.7
          )),
          PhaseType.P,
          "ak135",
          List.of(),
          ProcessingContext.createInteractive(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), StorageVisibility.PRIVATE)
      );
}

