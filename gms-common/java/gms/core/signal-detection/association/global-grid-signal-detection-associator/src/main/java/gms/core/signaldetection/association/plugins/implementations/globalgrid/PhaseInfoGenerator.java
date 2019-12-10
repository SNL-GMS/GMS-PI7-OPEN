package gms.core.signaldetection.association.plugins.implementations.globalgrid;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.PhaseType;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.ElevationCorrection1dDefinition;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.EllipticityCorrection1dDefinition;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.EventLocation;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.FeaturePrediction;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.FeaturePredictionCorrection;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.association.commonobjects.PhaseInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FeatureMeasurementType;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FeatureMeasurementTypes;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.InstantValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Location;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.NumericMeasurementValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceStation;
import gms.shared.utilities.geomath.GeoMath;
import gms.shared.utilities.javautilities.generation.AbstractGenerator;
import gms.shared.utilities.javautilities.generation.GenerationException;
import gms.shared.utilities.signalfeaturepredictionutility.SignalFeaturePredictionUtility;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@code PhaseInfoGenerator} is a builder for instances of {@code PhaseInfo}.
 */
public class PhaseInfoGenerator extends AbstractGenerator<PhaseInfo> {

  private static final Logger LOGGER = LoggerFactory.getLogger(PhaseInfoGenerator.class);

  // Bounds for absolute values of latitude and longitude. The 1e-12 is added to account
  // for precision errors in computing latitudes and longitudes.
  private static final double MAX_LATITUDE = 90.0 + 1e-12;
  private static final double MAX_LONGITUDE = 180.0 + 1e-12;

  private SignalFeaturePredictionUtility predictionUtility;
  private String travelTimePredictionEarthModel;
  private String magnitudeAttenuationPredictionEarthModel;

  private double gridCylinderRadiusDegrees = Double.NaN;
  private double gridCylinderHeightKm = Double.NaN;

  private double gridPointLatDegrees = Double.NaN;
  private double gridPointLonDegrees = Double.NaN;
  private double gridPointDepthKm = Double.NaN;
  private double minimumMagnitude = Double.NaN;
  private ReferenceStation referenceStation;
  private PhaseType phaseType;

  /**
   * Constructor. Until all parameters have been set, calling {@code generatePhaseInfo()}
   * throws an {@code IllegalStateException};
   */
  public PhaseInfoGenerator() {
  }

  /**
   * If possible, generate a {@code PhaseInfo} for the specified location, depth,
   * reference station, phase type and minimum magnitude.
   * @return a PhaseInfo instance, or null if one cannot be computed
   */
  protected PhaseInfo doGenerate() throws GenerationException {

    try {

      // Create EventLocation from grid point coordinates
      EventLocation gridPointLocation = EventLocation.from(
          gridPointLatDegrees,
          gridPointLonDegrees,
          gridPointDepthKm,
          Instant.EPOCH
      );

      // Set minimum and maximum depths of grid cylinder
      final double gridCylinderMinimumDepth =
          gridPointDepthKm - this.gridCylinderHeightKm / 2;
      final double gridCylinderMaximumDepth =
          gridPointDepthKm + this.gridCylinderHeightKm / 2;

      // Calculate unit vector in the direction from the grid point to the station
      double[] gridPointToStationVector = {
          referenceStation.getLatitude() - gridPointLatDegrees,
          referenceStation.getLongitude() - gridPointLonDegrees
      };
      double magnitude = Math
          .sqrt(
              Math.pow(gridPointToStationVector[0], 2) + Math.pow(gridPointToStationVector[1], 2));
      double[] gridPointToStationUnitVector = {
          gridPointToStationVector[0] / magnitude,
          gridPointToStationVector[1] / magnitude
      };

      // Calculate point on grid circle closest to station and at the surface
      EventLocation closestGridCircleLocationSurface = EventLocation.from(
          gridPointLocation.getLatitudeDegrees() + (gridPointToStationUnitVector[0]
              * this.gridCylinderRadiusDegrees),
          gridPointLocation.getLongitudeDegrees() + (gridPointToStationUnitVector[1]
              * this.gridCylinderRadiusDegrees),
          gridCylinderMinimumDepth,
          gridPointLocation.getTime()
      );

      // Calculate point on grid circle closest to the station and at max depth of grid cylinder
      EventLocation closestGridCircleLocationMaxDepth = EventLocation.from(
          gridPointLocation.getLatitudeDegrees() + (gridPointToStationUnitVector[0]
              * this.gridCylinderRadiusDegrees),
          gridPointLocation.getLongitudeDegrees() + (gridPointToStationUnitVector[1]
              * this.gridCylinderRadiusDegrees),
          gridCylinderMaximumDepth, // Assume furthest point is at depth 0
          gridPointLocation.getTime()
      );

      // Calculate point on grid circle closest to station and at the surface
      EventLocation furthestGridCircleLocationSurface = EventLocation.from(
          gridPointLocation.getLatitudeDegrees() - (gridPointToStationUnitVector[0]
              * this.gridCylinderRadiusDegrees),
          gridPointLocation.getLongitudeDegrees() - (gridPointToStationUnitVector[1]
              * this.gridCylinderRadiusDegrees),
          gridCylinderMinimumDepth,
          gridPointLocation.getTime()
      );

      // Calculate point on grid circle closest to the station and at max depth of grid cylinder
      EventLocation furthestGridCircleLocationMaxDepth = EventLocation.from(
          gridPointLocation.getLatitudeDegrees() - (gridPointToStationUnitVector[0]
              * this.gridCylinderRadiusDegrees),
          gridPointLocation.getLongitudeDegrees() - (gridPointToStationUnitVector[1]
              * this.gridCylinderRadiusDegrees),
          gridCylinderMaximumDepth,
          gridPointLocation.getTime()
      );

      // Calculate point on grid circle at the surface directly above central grid point
      EventLocation gridPointLocationSurface = EventLocation.from(
          gridPointLocation.getLatitudeDegrees(),
          gridPointLocation.getLongitudeDegrees(),
          gridCylinderMinimumDepth,
          gridPointLocation.getTime()
      );

      // Calculate point on grid circle at the max depth directly below central grid point
      EventLocation gridPointLocationMaxDepth = EventLocation.from(
          gridPointLocation.getLatitudeDegrees(),
          gridPointLocation.getLongitudeDegrees(),
          gridCylinderMaximumDepth,
          gridPointLocation.getTime()
      );

      // Calculate point on grid circle closest to the station at the same depth as central grid point
      EventLocation closestGridPointLocation = EventLocation.from(
          gridPointLocation.getLatitudeDegrees() + (gridPointToStationUnitVector[0]
              * this.gridCylinderRadiusDegrees),
          gridPointLocation.getLongitudeDegrees() + (gridPointToStationUnitVector[1]
              * this.gridCylinderRadiusDegrees),
          gridPointLocation.getDepthKm(),
          gridPointLocation.getTime()
      );

      // Calculate point on grid circle furthest from the station at the same depth as central grid point
      EventLocation furthestGridPointLocation = EventLocation.from(
          gridPointLocation.getLatitudeDegrees() - (gridPointToStationUnitVector[0]
              * this.gridCylinderRadiusDegrees),
          gridPointLocation.getLongitudeDegrees() - (gridPointToStationUnitVector[1]
              * this.gridCylinderRadiusDegrees),
          gridPointLocation.getDepthKm(),
          gridPointLocation.getTime()
      );

      // Create Location to represent ReferenceStation location.  predict() requires Location,
      // not ReferenceStation.
      Location stationLocation = Location.from(
          referenceStation.getLatitude(),
          referenceStation.getLongitude(),
          0,  // stations are assumed to be at depth 0
          referenceStation.getElevation()
      );

      // Create PhaseInfo.Builder
      PhaseInfo.Builder phaseInfoBuilder = PhaseInfo.builder();

      // Set all values in builder that involve making feature predictions.  If any involved
      // predictions are invalid, return empty optional instead of valid PhaseInfo.
      if (
          !(
              this.setMinimumAndMaximumTravelTimes(
                  phaseInfoBuilder,
                  phaseType,
                  stationLocation,
                  closestGridCircleLocationSurface,
                  closestGridCircleLocationMaxDepth,
                  furthestGridCircleLocationSurface,
                  furthestGridCircleLocationMaxDepth
              ) && this.setRadialTravelTimeDerivative(
                  phaseInfoBuilder,
                  phaseType,
                  stationLocation,
                  furthestGridPointLocation,
                  closestGridPointLocation
              ) && this.setVerticalTravelTimeDerivative(
                  phaseInfoBuilder,
                  phaseType,
                  stationLocation,
                  gridPointLocationMaxDepth,
                  gridPointLocationSurface
              ) && this.setTravelTimeSeconds(
                  phaseInfoBuilder,
                  phaseType,
                  stationLocation,
                  gridPointLocation
              ) && this.setMagnitudeCorrection(
                  phaseInfoBuilder,
                  phaseType,
                  stationLocation,
                  gridPointLocation
              ) && this.setRadialMagnitudeCorrectionDerivative(
                  phaseInfoBuilder,
                  phaseType,
                  stationLocation,
                  furthestGridPointLocation,
                  closestGridPointLocation
              ) && this.setVerticalMagnitudeCorrectionDerivative(
                  phaseInfoBuilder,
                  phaseType,
                  stationLocation,
                  gridPointLocationMaxDepth,
                  gridPointLocationSurface
              ) && this.setSlownessCellWidth(
                  phaseInfoBuilder,
                  phaseType,
                  stationLocation,
                  closestGridPointLocation,
                  furthestGridPointLocation
              ) && this.setSlowness(
                  phaseInfoBuilder,
                  phaseType,
                  stationLocation,
                  gridPointLocation
              )
          )
      ) {

        // One or more of the feature predictions involved in calculating the PhaseInfo values
        // was invalid, so abandon creating PhaseInfo and return empty optional.
        return null;
      }

      // Set phaseType value in builder.  No predictions were involved in
      // this operation, so no need to check if the operation was successful or not.
      this.setPhaseType(phaseInfoBuilder, phaseType);

      // Set isPrimary value in builder.  Only P is a primary phase.  No predictions were involved in
      // this operation, so no need to check if the operation was successful or not.
      this.setIsPrimary(phaseInfoBuilder, phaseType);

      // Calculate and set azimuthDegrees and backAzimuthDegrees values in the builder.
      // The predictions involved in calculating these values will never be invalid, so
      // no need to check if the operations were successful or not.
      this.setAzimuthDegrees(phaseInfoBuilder, stationLocation, gridPointLocation);
      this.setBackAzimuthDegrees(phaseInfoBuilder, stationLocation, gridPointLocation);

      // Set minimum magnitude value in the builder. minimumMagnitude is a configured value,
      // so no predictions were involved, so no need to check if the operation was successful
      // or not.
      this.setMinimumMagnitude(phaseInfoBuilder, minimumMagnitude);

      // The builder has been populated with valid values, so build the PhaseInfo object
      // and return it.
      return phaseInfoBuilder.build();

    } catch (IllegalStateException ise) {

      // Sometimes thrown when one of the set calls fails above. Usually the cause is an
      // IllegalArgumentException with a message such as:
      // "Caused by: java.lang.IllegalArgumentException:
      //   DziewonskiGilbertEllipticityCorrection::correct() requires colatitude >= 0.0"

      // Show detailed stack trace info when running in debug mode
      if (LOGGER.isDebugEnabled()) {

        // Just show the stack trace for the cause if it's non-null.
        LOGGER.error("error generating PhaseInfo:", ise.getCause() != null ?
            ise.getCause() : ise);

      } else { // When not in debug mode, display a brief error message.

        // Come up with a brief message that reveals the true cause of the problem.
        String message = ise.getCause() != null ? ise.getCause().getMessage() : ise.getMessage();

        LOGGER.error("error generating PhaseInfo: " + message);

      }

      return null;

    } catch (Throwable t) {

      throw new GenerationException("error generating phaseinfo", t);

    }
  }

  // Utility method to make batch feature predictions for multiple EventLocations
  private <T> List<FeaturePrediction<T>> makePredictions(
      FeatureMeasurementType<T> predictionType,
      List<EventLocation> eventLocations,
      Location stationLocation,
      String earthModel,
      PhaseType phaseType
  ) {

    // Instantiate list to contain prediction results
    ArrayList<FeaturePrediction<T>> predictions = new ArrayList<>();

    // Instantiate list to contain prediction corrections
    ArrayList<FeaturePredictionCorrection> featurePredictionCorrections;

    // If prediction type is MAGNITUDE_CORRECTION, set no corrections because there are no
    // valid corrections for MAGNITUDE_CORRECTION predictions.  Otherwise, set all corrections.
    if (predictionType.equals(FeatureMeasurementTypes.MAGNITUDE_CORRECTION)) {

      featurePredictionCorrections = new ArrayList<>();
    } else {

      featurePredictionCorrections = new ArrayList<>();
      featurePredictionCorrections.add(ElevationCorrection1dDefinition.create(true));
      featurePredictionCorrections.add(EllipticityCorrection1dDefinition.create());
    }

    try {

      for (EventLocation loc : eventLocations) {
        predictions.add(
            this.predictionUtility.predictSingle(
                predictionType,
                loc,
                stationLocation,
                phaseType,
                earthModel,
                featurePredictionCorrections
            )
        );
      }
    } catch (Exception e) {

      throw new IllegalStateException(
          "Predictor threw exception while making predictions to populate PhaseInfo", e);
    }

    // Return list of prediction results
    return predictions;
  }

  // Utility method to return the maximum arrival time prediction
  private FeaturePrediction<InstantValue> maximumArrivalTimePrediction(
      Collection<FeaturePrediction<InstantValue>> arrivalTimePredictions) {

    // Validate that predictions are ARRIVAL_TIME predictions and that their predicted
    // value is not empty
    arrivalTimePredictions.forEach(prediction -> {
          Validate
              .isTrue(prediction.getPredictedValue().isPresent(), "Predicted value must be present");
        }
    );

    // Determine maximum arrival time prediction
    return arrivalTimePredictions.stream()
        .reduce((a, b) -> {

              InstantValue valueA = (InstantValue) a.getPredictedValue()
                  .orElseThrow(AssertionError::new);
              InstantValue valueB = (InstantValue) b.getPredictedValue()
                  .orElseThrow(AssertionError::new);

              // If a > b, return a
              // If a < b, return b
              // If a == b, return a
              return valueA.getValue().compareTo(valueB.getValue()) >= 0 ? a : b;
            }
        ).orElseThrow(AssertionError::new);
  }

  // Utility method to return the minimum arrival time prediction
  private FeaturePrediction<InstantValue> minimumArrivalTimePrediction(
      Collection<FeaturePrediction<InstantValue>> arrivalTimePredictions) {

    // Validate that predictions are ARRIVAL_TIME predictions and that their predicted
    // value is not empty
    arrivalTimePredictions.forEach(prediction -> {
          Validate
              .isTrue(prediction.getPredictedValue().isPresent(), "Predicted value must be present");
        }
    );

    // Determine maximum arrival time prediction
    return arrivalTimePredictions.stream()
        .reduce((a, b) -> {
              InstantValue valueA = (InstantValue) a.getPredictedValue()
                  .orElseThrow(AssertionError::new);
              InstantValue valueB = (InstantValue) b.getPredictedValue()
                  .orElseThrow(AssertionError::new);

              // If a > b, return b
              // If a < b, return a
              // If a == b, return a
              return valueA.getValue().compareTo(valueB.getValue()) <= 0 ? a : b;
            }
        ).orElseThrow(AssertionError::new);
  }

  // Utility method to convert ARRIVAL_TIME prediction to double (seconds)
  private double arrivalTimeToTravelTime(Instant startTime, Instant arrivalTime) {

    Duration travelTimeDuration = Duration.between(startTime, arrivalTime);

    return travelTimeDuration.toNanos() / 1_000_000_000.0;
  }


  private void setPhaseType(
      PhaseInfo.Builder phaseInfoBuilder,
      PhaseType phaseType
  ) {

    Objects.requireNonNull(phaseInfoBuilder, "Null phaseInfoBuilder");
    Objects.requireNonNull(phaseType, "Null phaseType");

    phaseInfoBuilder.setPhaseType(phaseType);
  }


  private void setIsPrimary(
      PhaseInfo.Builder phaseInfoBuilder,
      PhaseType phaseType
  ) {

    Objects.requireNonNull(phaseInfoBuilder, "Null phaseInfoBuilder");
    Objects.requireNonNull(phaseType, "Null phaseType");

    if (phaseType.equals(PhaseType.P)) {

      phaseInfoBuilder.setPrimary(true);
    } else {

      phaseInfoBuilder.setPrimary(false);
    }
  }


  /*
   * Calculates travelTimeSeconds and updates the PhaseInfo builder with this value;
   * Returns true if successful, false if not.
   *
   * PhaseInfo.Builder builder   - Modifies this builder in place. Predicts and sets the
   *                               travelTimeSeconds field.
   * PhaseType phaseType         - The PhaseType for which to predict travel time.
   * Location stationLocation    - The destination location of the signal whose travel time this
   *                               method predicts
   * EventLocation eventLocation - The origin location of the signal whose travel time this method
   *                               predicts
   *
   * If successful, this method returns true and updates the builder in place with the predicted
   * travel time value.  If unsuccessful (i.e. the requested feature prediction was invalid), the
   * builder is NOT updated and the method returns false.
   */
  private boolean setTravelTimeSeconds(
      PhaseInfo.Builder builder,
      PhaseType phaseType,
      Location stationLocation,
      EventLocation eventLocation
  ) {

    // Create List of EventLocations used to calculate arrival time for the grid point
    List<EventLocation> gridPointLocations = new ArrayList<>();
    gridPointLocations.add(eventLocation);

    // Calculate arrival time from the grid point to the ReferenceStation
    List<FeaturePrediction<InstantValue>> gridPointArrivalTimePredictions = this.makePredictions(
        FeatureMeasurementTypes.ARRIVAL_TIME,
        gridPointLocations,
        stationLocation,
        this.travelTimePredictionEarthModel,
        phaseType
    );

    // Validate we got one prediction for the grid point
    if (gridPointArrivalTimePredictions.size() != 1) {

      throw new IllegalStateException(
          "Expected one travel time prediction for center of grid cylinder");
    }

    // Extract arrival time prediction from list
    FeaturePrediction<InstantValue> gridPointArrivalTimePrediction = gridPointArrivalTimePredictions
        .get(0);

    // If prediction is invalid, return an empty optional instead of a valid PhaseInfo
    if (!gridPointArrivalTimePrediction.getPredictedValue().isPresent()) {

      return false;
    }

    // Convert arrival time to travel time
    double gridPointTravelTimePrediction = this.arrivalTimeToTravelTime(
        gridPointArrivalTimePrediction.getSourceLocation().getTime(),
        gridPointArrivalTimePrediction.getPredictedValue().orElseThrow(AssertionError::new)
            .getValue()
    );

    builder.setTravelTimeSeconds(gridPointTravelTimePrediction);
    return true;
  }


  /*
   * Calculates minimumTravelTime and maximumTravelTime and updates the PhaseInfo builder with these
   * values.  Returns true if successful, false if not.
   *
   * PhaseInfo.Builder builder    - Modifies this builder in place. Sets the minimumTravelTime and
   *                               maximumTravelTime fields.
   * PhaseType phaseType          - The PhaseType for which to predict travel times.
   * Location stationLocation     - The destination location of the signal whose travel time this
   *                               method predicts
   * EventLocation eventLocation1 - Represents a location in the grid cylinder which may be the origin
   *                                location of either the minimum or the maximum travel time values
   *                                for locations in the cylinder.
   * EventLocation eventLocation2 - Represents a location in the grid cylinder which may be the origin
   *                                location of either the minimum or the maximum travel time values
   *                                for locations in the cylinder.
   * EventLocation eventLocation3 - Represents a location in the grid cylinder which may be the origin
   *                                location of either the minimum or the maximum travel time values
   *                                for locations in the cylinder.
   * EventLocation eventLocation4 - Represents a location in the grid cylinder which may be the origin
   *                                location of either the minimum or the maximum travel time values
   *                                for locations in the cylinder.
   *
   * If successful, this method returns true and updates the builder in place with the predicted
   * minimumTravelTime and maximumTravelTime values.  If unsuccessful (i.e. one or more of the
   * requested feature prediction was invalid), the builder is NOT updated and this method returns false.
   */
  private boolean setMinimumAndMaximumTravelTimes(
      PhaseInfo.Builder builder,
      PhaseType phaseType,
      Location stationLocation,
      EventLocation eventLocation1,
      EventLocation eventLocation2,
      EventLocation eventLocation3,
      EventLocation eventLocation4
  ) {

    // Create List of EventLocations used to calculate minimum and maximum travel time values
    // for the grid point
    List<EventLocation> minAndMaxArrivalTimeLocations = new ArrayList<>();
    minAndMaxArrivalTimeLocations.add(eventLocation1);
    minAndMaxArrivalTimeLocations.add(eventLocation2);
    minAndMaxArrivalTimeLocations.add(eventLocation3);
    minAndMaxArrivalTimeLocations.add(eventLocation4);

    // Calculate List of FeaturePredictions from which to calculate the minimum and maximum travel
    // time values for the grid point
    List<FeaturePrediction<InstantValue>> minAndMaxArrivalTimePredictions = this.makePredictions(
        FeatureMeasurementTypes.ARRIVAL_TIME,
        minAndMaxArrivalTimeLocations,
        stationLocation,
        this.travelTimePredictionEarthModel,
        phaseType
    );

    // If any prediction is invalid, return an empty optional instead of a valid PhaseInfo
    for (FeaturePrediction<?> prediction : minAndMaxArrivalTimePredictions) {

      if (!prediction.getPredictedValue().isPresent()) {
        return false;
      }
    }

    // Determine maximum and minimum arrival time prediction
    FeaturePrediction<InstantValue> minimumPrediction = this
        .minimumArrivalTimePrediction(minAndMaxArrivalTimePredictions);
    FeaturePrediction<InstantValue> maximumPrediction = this
        .maximumArrivalTimePrediction(minAndMaxArrivalTimePredictions);

    // Convert maximum and minimum arrival time predictions to travel time values
    double minimumTravelTime = this
        .arrivalTimeToTravelTime(minimumPrediction.getSourceLocation().getTime(),
            minimumPrediction.getPredictedValue().orElseThrow(AssertionError::new).getValue());
    double maximumTravelTime = this
        .arrivalTimeToTravelTime(maximumPrediction.getSourceLocation().getTime(),
            maximumPrediction.getPredictedValue().orElseThrow(AssertionError::new).getValue());

    // Set minimum and maximum travel times
    builder.setTravelTimeMinimum(minimumTravelTime);
    builder.setTravelTimeMaximum(maximumTravelTime);

    return true;
  }


  /*
   * Calculates the verticalTravelTimeDerivative from eventLocation1 to eventLocation2 and updates the
   * PhaseInfo builder with that value.  Returns true if successful, false if not.
   *
   * PhaseInfo.Builder builder    - Modifies this builder in place. Sets the verticalTravelTimeDerivative
   *                                field.
   * PhaseType phaseType          - The PhaseType for which to predict travel times used to calculate
   *                                the verticalTravelTimeDerivative.
   * Location stationLocation     - The destination location of the signal whose travel time this
   *                                method predicts.
   * EventLocation eventLocation1 - Represents a location in the grid cylinder which is at the same radial
   *                                position as the center grid point, but is at the deepest depth of
   *                                the grid cylinder.
   * EventLocation eventLocation2 - Represents a location in the grid cylinder which is at the same radial
   *                                position as the center grid point, but is at the surface.
   *
   * If successful, this method returns true and updates the builder in place with the predicted
   * verticalTravelTimeDerivative value.  If unsuccessful (i.e. one or more of the requested feature
   * predictions was invalid), the builder is NOT updated and this method returns false.
   */
  private boolean setVerticalTravelTimeDerivative(
      PhaseInfo.Builder builder,
      PhaseType phaseType,
      Location stationLocation,
      EventLocation location1,
      EventLocation location2
  ) {
    double verticalTravelTimeDerivative = this.calculateInstantMeasurementValueDerivative(
        phaseType,
        stationLocation,
        location1,
        location2
    );

    if (Double.isNaN(verticalTravelTimeDerivative)) {

      return false;
    } else {

      builder.setVerticalTravelTimeDerivative(verticalTravelTimeDerivative);
      return true;
    }
  }


  /*
   * Calculates the radialTravelTimeDerivative from eventLocation1 to eventLocation2 and updates the
   * PhaseInfo builder with that value.  Returns true if successful, false if not.
   *
   * PhaseInfo.Builder builder    - Modifies this builder in place. Sets the radialTravelTimeDerivative
   *                                field.
   * PhaseType phaseType          - The PhaseType for which to predict travel times used to calculate
   *                                the radialTravelTimeDerivative.
   * Location stationLocation     - The destination location of the signal whose travel time this
   *                                method predicts.
   * EventLocation eventLocation1 - Represents a location in the grid cylinder which is at the same depth
   *                                position as the center grid point, but is at the the furthest point
   *                                on the grid cylinder to the stationLocation.
   * EventLocation eventLocation2 - Represents a location in the grid cylinder which is at the same depth
   *                                position as the center grid point, but is at the the closest point
   *                                on the grid cylinder to the stationLocation.
   *
   * If successful, this method returns true and updates the builder in place with the predicted
   * radialTravelTimeDerivative value.  If unsuccessful (i.e. one or more of the requested feature
   * predictions was invalid), the builder is NOT updated and this method returns false.
   */
  private boolean setRadialTravelTimeDerivative(
      PhaseInfo.Builder builder,
      PhaseType phaseType,
      Location stationLocation,
      EventLocation location1,
      EventLocation location2
  ) {

    double radialTravelTimeDerivative = this.calculateInstantMeasurementValueDerivative(
        phaseType,
        stationLocation,
        location1,
        location2
    );

    if (Double.isNaN(radialTravelTimeDerivative)) {

      return false;
    } else {

      builder.setRadialTravelTimeDerivative(radialTravelTimeDerivative);
      return true;
    }
  }


  /*
   * Calculates the derivative of the travel time values from eventLocation1 to eventLocation2.
   *
   * PhaseType phaseType          - The PhaseType for which to predict travel times used to calculate
   *                                the travel time derivative.
   * Location stationLocation     - The destination location of the signal whose travel time this
   *                                method predicts.
   * EventLocation eventLocation1 - Represents a location in the grid cylinder.  The travel time
   *                                derivative is calculated from eventLocation1 to eventLocation2.
   * EventLocation eventLocation2 - Represents a location in the grid cylinder.  The travel time
   *                                derivative is calculated from eventLocation1 to eventLocation2.
   *
   * If successful, this method returns the travel time derivative between eventLocation1 and
   * eventLocation2.  If unsuccessful (i.e. one or more of the requested feature predictions was
   * invalid), this method returns Double.NaN.
   */
  private double calculateInstantMeasurementValueDerivative(
      PhaseType phaseType,
      Location stationLocation,
      EventLocation location1,
      EventLocation location2
  ) {

    // Create List of EventLocations used to calculate radial travel time derivative
    // for the grid point
    List<EventLocation> travelTimeDerivativeLocations = new ArrayList<>();
    travelTimeDerivativeLocations.add(location2);
    travelTimeDerivativeLocations.add(location1);

    // Calculate List of FeaturePredictions from which to calculate the radial travel time derivative
    // for the grid point
    List<FeaturePrediction<InstantValue>> travelTimeDerivativePredictions = this
        .makePredictions(
            FeatureMeasurementTypes.ARRIVAL_TIME,
            travelTimeDerivativeLocations,
            stationLocation,
            this.travelTimePredictionEarthModel,
            phaseType
        );

    // If any prediction is invalid, return an empty optional instead of a valid PhaseInfo
    for (FeaturePrediction<?> prediction : travelTimeDerivativePredictions) {

      if (!prediction.getPredictedValue().isPresent()) {
        return Double.NaN;
      }
    }

    // Validate we got 2 predictions for radial travel time derivative
    if (travelTimeDerivativePredictions.size() != 2) {

      throw new IllegalStateException(
          "Expected 2 travel time predictions for calculating travel time prediction derivative");
    }

    // Convert predicted arrival time for closest radial travel time prediction location to travel time
    double travelTimeDerivativePredictionClosest = this.arrivalTimeToTravelTime(
        travelTimeDerivativePredictions.get(0).getSourceLocation().getTime(),
        travelTimeDerivativePredictions.get(0).getPredictedValue()
            .orElseThrow(AssertionError::new).getValue()
    );

    // Convert predicted arrival time for furthest radial travel time prediction location to travel time
    double travelTimeDerivativePredictionFurthest = this.arrivalTimeToTravelTime(
        travelTimeDerivativePredictions.get(1).getSourceLocation().getTime(),
        travelTimeDerivativePredictions.get(1).getPredictedValue()
            .orElseThrow(AssertionError::new).getValue()
    );

    // Calculate radial travel time derivative
    return
        (travelTimeDerivativePredictionClosest - travelTimeDerivativePredictionFurthest)
            / this.gridCylinderRadiusDegrees * 2;
  }


  /*
   * Calculates azimuthDegrees (grid point -> station) and updates the
   * PhaseInfo builder with that value.
   *
   * PhaseInfo.Builder builder    - Modifies this builder in place. Sets the azimuthDegrees field.
   * Location stationLocation     - This method predicts the azimuth from the eventLocation to the
   *                                stationLocation.
   * EventLocation eventLocation  - This method predicts the azimuth from the eventLocation to the
   *                                stationLocation.
   */
  private void setAzimuthDegrees(
      PhaseInfo.Builder phaseInfoBuilder,
      Location stationLocation,
      EventLocation eventLocation
  ) {

    double azimuth = GeoMath.azimuth(
        eventLocation.getLatitudeDegrees(),
        eventLocation.getLongitudeDegrees(),
        stationLocation.getLatitudeDegrees(),
        stationLocation.getLongitudeDegrees()
    );

    phaseInfoBuilder.setAzimuthDegrees(azimuth);
  }


  /*
   * Calculates backAzimuthDegrees (station -> grid point) and updates the
   * PhaseInfo builder with that value.
   *
   * PhaseInfo.Builder builder    - Modifies this builder in place. Sets the backAzimuthDegrees field.
   * Location stationLocation     - This method predicts the azimuth from the stationLocation to the
   *                                eventLocation.
   * EventLocation eventLocation  - This method predicts the azimuth from the stationLocation to the
   *                                EventLocation.
   */
  private void setBackAzimuthDegrees(
      PhaseInfo.Builder phaseInfoBuilder,
      Location stationLocation,
      EventLocation eventLocation
  ) {

    double backAzimuth = GeoMath.azimuth(
        stationLocation.getLatitudeDegrees(),
        stationLocation.getLongitudeDegrees(),
        eventLocation.getLatitudeDegrees(),
        eventLocation.getLongitudeDegrees()
    );

    phaseInfoBuilder.setBackAzimuthDegrees(backAzimuth);
  }


  /*
   * Calculates magnitude correction from eventLocation to stationLocation and updates the PhaseInfo
   * builder with this value.  Returns true if successful, false if not.
   *
   * PhaseInfo.Builder builder    - Modifies this builder in place. Sets the magnitudeCorrection
   *                                field.
   * PhaseType phaseType          - The PhaseType for which to predict magnitude correction
   * Location stationLocation     - The destination location of the signal whose magnitude correction this
   *                                method predicts.
   * EventLocation eventLocation  - The origin location of the signal whose magnitude correction this
   *                                method predicts.
   *
   * If successful, this method returns true and updates the builder in place with the predicted
   * magnitude correction value.  If unsuccessful (i.e. one or more of the requested feature
   * predictions was invalid), the builder is NOT updated and this method returns false.
   */
  private boolean setMagnitudeCorrection(
      PhaseInfo.Builder builder,
      PhaseType phaseType,
      Location stationLocation,
      EventLocation eventLocation
  ) {

    FeaturePrediction<NumericMeasurementValue> magnitudeCorrectionPrediction;

    try {

      // Make magnitude correction prediction with all corrections applied
      magnitudeCorrectionPrediction = this.predictionUtility.predictSingle(
          FeatureMeasurementTypes.MAGNITUDE_CORRECTION,
          eventLocation,
          stationLocation,
          phaseType,
          this.magnitudeAttenuationPredictionEarthModel,
          List.of()
      );
    } catch (Exception e) {

      throw new IllegalStateException(
          "Predictor threw exception while making predictions to populate PhaseInfo", e);
    }

    // Verify the prediction was valid - if the predicted value is present, the prediction was valid,
    // so extract the value and update the builder with the magnitude correction.
    // If invalid, return false and do not update the builder.
    if (magnitudeCorrectionPrediction.getPredictedValue().isPresent()) {

      double magnitudeCorrection = magnitudeCorrectionPrediction.getPredictedValue().get()
          .getMeasurementValue().getValue();
      builder.setMagnitudeCorrection(magnitudeCorrection);
      return true;
    } else {

      return false;
    }
  }


  /*
   * Calculates the verticalMagnitudeCorrectionDerivative from eventLocation1 to eventLocation2 and updates the
   * PhaseInfo builder with that value.  Returns true if successful, false if not.
   *
   * PhaseInfo.Builder builder    - Modifies this builder in place. Sets the verticalMagnitudeCorrectionDerivative
   *                                field.
   * PhaseType phaseType          - The PhaseType for which to predict travel times used to calculate
   *                                the verticalMagnitudeCorrectionDerivative.
   * Location stationLocation     - The destination location of the signal whose travel time this
   *                                method predicts.
   * EventLocation eventLocation1 - Represents a location in the grid cylinder which is at the same radial
   *                                position as the center grid point, but is at the deepest depth of
   *                                the grid cylinder.
   * EventLocation eventLocation2 - Represents a location in the grid cylinder which is at the same radial
   *                                position as the center grid point, but is at the surface.
   *
   * If successful, this method returns true and updates the builder in place with the predicted
   * verticalMagnitudeCorrectionDerivative value.  If unsuccessful (i.e. one or more of the requested feature
   * predictions was invalid), the builder is NOT updated and this method returns false.
   */
  private boolean setVerticalMagnitudeCorrectionDerivative(
      PhaseInfo.Builder builder,
      PhaseType phaseType,
      Location stationLocation,
      EventLocation location1,
      EventLocation location2
  ) {
    double verticalMagnitudeCorrectionDerivative = this.calculateNumericMeasurementValueDerivative(
        FeatureMeasurementTypes.MAGNITUDE_CORRECTION,
        phaseType,
        this.magnitudeAttenuationPredictionEarthModel,
        stationLocation,
        location1,
        location2
    );

    if (Double.isNaN(verticalMagnitudeCorrectionDerivative)) {

      return false;
    } else {

      builder.setVerticalMagnitudeCorrectionDerivative(verticalMagnitudeCorrectionDerivative);
      return true;
    }
  }


  /*
   * Calculates the radialMagnitudeCorrectionDerivative from eventLocation1 to eventLocation2 and updates the
   * PhaseInfo builder with that value.  Returns true if successful, false if not.
   *
   * PhaseInfo.Builder builder    - Modifies this builder in place. Sets the radialMagnitudeCorrectionDerivative
   *                                field.
   * PhaseType phaseType          - The PhaseType for which to predict travel times used to calculate
   *                                the radialMagnitudeCorrectionDerivative.
   * Location stationLocation     - The destination location of the signal whose travel time this
   *                                method predicts.
   * EventLocation eventLocation1 - Represents a location in the grid cylinder which is at the same depth
   *                                position as the center grid point, but is at the the furthest point
   *                                on the grid cylinder to the stationLocation.
   * EventLocation eventLocation2 - Represents a location in the grid cylinder which is at the same depth
   *                                position as the center grid point, but is at the the closest point
   *                                on the grid cylinder to the stationLocation.
   *
   * If successful, this method returns true and updates the builder in place with the predicted
   * radialMagnitudeCorrectionDerivative value.  If unsuccessful (i.e. one or more of the requested feature
   * predictions was invalid), the builder is NOT updated and this method returns false.
   */
  private boolean setRadialMagnitudeCorrectionDerivative(
      PhaseInfo.Builder builder,
      PhaseType phaseType,
      Location stationLocation,
      EventLocation location1,
      EventLocation location2
  ) {

    double radialMagnitudeCorrectionDerivative = this.calculateNumericMeasurementValueDerivative(
        FeatureMeasurementTypes.MAGNITUDE_CORRECTION,
        phaseType,
        this.magnitudeAttenuationPredictionEarthModel,
        stationLocation,
        location1,
        location2
    );

    if (Double.isNaN(radialMagnitudeCorrectionDerivative)) {

      return false;
    } else {

      builder.setRadialMagnitudeCorrectionDerivative(radialMagnitudeCorrectionDerivative);
      return true;
    }
  }


  /*
   * Calculates the slownessCellWidth from eventLocation1 to eventLocation2 and updates the
   * PhaseInfo builder with that value.  Returns true if successful, false if not.
   *
   * PhaseInfo.Builder builder    - Modifies this builder in place. Sets the slownessCellWidth
   *                                field.
   * PhaseType phaseType          - The PhaseType for which to predict slowness values used to calculate
   *                                the slownessCellWidth.
   * Location stationLocation     - The destination location of the signal whose travel time this
   *                                method predicts.
   * EventLocation eventLocation1 - Represents a location in the grid cylinder which is at the same depth
   *                                position as the center grid point, but is at the the furthest point
   *                                on the grid cylinder to the stationLocation.
   * EventLocation eventLocation2 - Represents a location in the grid cylinder which is at the same depth
   *                                position as the center grid point, but is at the the closest point
   *                                on the grid cylinder to the stationLocation.
   *
   * If successful, this method returns true and updates the builder in place with the predicted
   * slownessCellWidth value.  If unsuccessful (i.e. one or more of the requested feature
   * predictions was invalid), the builder is NOT updated and this method returns false.
   */
  private boolean setSlownessCellWidth(
      PhaseInfo.Builder builder,
      PhaseType phaseType,
      Location stationLocation,
      EventLocation location1,
      EventLocation location2
  ) {

    double radialSlownessPredictionDerivative = this.calculateNumericMeasurementValueDerivative(
        FeatureMeasurementTypes.SLOWNESS,
        phaseType,
        this.travelTimePredictionEarthModel,
        stationLocation,
        location1,
        location2
    );

    if (Double.isNaN(radialSlownessPredictionDerivative)) {
      return false;
    } else {
      builder.setSlownessCellWidth(radialSlownessPredictionDerivative);
      return true;
    }
  }


  /*
   * Calculates the slowness from eventLocation and updates the
   * PhaseInfo builder with that value.  Returns true if successful, false if not.
   *
   * PhaseInfo.Builder builder    - Modifies this builder in place. Sets the slowness
   *                                field.
   * PhaseType phaseType          - The PhaseType for which to predict slowness values used to calculate
   *                                the slowness.
   * Location stationLocation     - The destination location of the signal whose slowness this
   *                                method predicts.
   * EventLocation eventLocation  - The origin location of the signal whose slowness this
   *                                method predicts.
   *
   * If successful, this method returns true and updates the builder in place with the predicted
   * slowness value.  If unsuccessful (i.e. one or more of the requested feature
   * predictions was invalid), the builder is NOT updated and this method returns false.
   */
  private boolean setSlowness(
      PhaseInfo.Builder builder,
      PhaseType phaseType,
      Location stationLocation,
      EventLocation eventLocation
  ) {

    // Create List of EventLocations used to calculate arrival time for the grid point
    List<EventLocation> gridPointLocations = new ArrayList<>();
    gridPointLocations.add(eventLocation);

    // Calculate arrival time from the grid point to the ReferenceStation
    List<FeaturePrediction<NumericMeasurementValue>> gridPointSlownessPredictions = this
        .makePredictions(
            FeatureMeasurementTypes.SLOWNESS,
            gridPointLocations,
            stationLocation,
            this.travelTimePredictionEarthModel,
            phaseType
        );

    // Validate we got one prediction for the grid point
    if (gridPointSlownessPredictions.size() != 1) {

      throw new IllegalStateException(
          "Expected one travel time prediction for center of grid cylinder");
    }

    // Extract arrival time prediction from list
    FeaturePrediction<NumericMeasurementValue> gridPointSlownessPrediction = gridPointSlownessPredictions
        .get(0);

    // If prediction is invalid, return an empty optional instead of a valid PhaseInfo
    if (!gridPointSlownessPrediction.getPredictedValue().isPresent()) {

      return false;
    }

    builder.setSlowness(
        gridPointSlownessPrediction.getPredictedValue().orElseThrow(AssertionError::new)
            .getMeasurementValue().getValue());
    return true;
  }


  /*
   * Calculates the derivative of the magnitude correction values from eventLocation1 to eventLocation2.
   *
   * PhaseType phaseType          - The PhaseType for which to predict magnitude corrections used to calculate
   *                                the magnitude correction derivative.
   * Location stationLocation     - The destination location of the signal whose magnitude correction this
   *                                method predicts.
   * EventLocation eventLocation1 - Represents a location in the grid cylinder.  The magnitude correction
   *                                derivative is calculated from eventLocation1 to eventLocation2.
   * EventLocation eventLocation2 - Represents a location in the grid cylinder.  The magnitude correction
   *                                derivative is calculated from eventLocation1 to eventLocation2.
   *
   * If successful, this method returns the magnitude correction derivative between eventLocation1 and
   * eventLocation2.  If unsuccessful (i.e. one or more of the requested feature predictions was
   * invalid), this method returns Double.NaN.
   */
  private double calculateNumericMeasurementValueDerivative(
      FeatureMeasurementType<NumericMeasurementValue> predictionType,
      PhaseType phaseType,
      String earthModel,
      Location stationLocation,
      EventLocation location1,
      EventLocation location2
  ) {

    // Create List of EventLocations used to calculate magnitude correction derivatives
    // for the grid point
    List<EventLocation> magnitudeCorrectionDerivativeLocations = new ArrayList<>();
    magnitudeCorrectionDerivativeLocations.add(location2);
    magnitudeCorrectionDerivativeLocations.add(location1);

    // Calculate List of FeaturePredictions from which to calculate magnitude correction derivatives
    // for the grid point
    List<FeaturePrediction<NumericMeasurementValue>> magnitudeCorrectionDerivativePredictions = this
        .makePredictions(
            predictionType,
            magnitudeCorrectionDerivativeLocations,
            stationLocation,
            earthModel,
            phaseType
        );

    // If any prediction is invalid, return Double.NaN
    for (FeaturePrediction<?> prediction : magnitudeCorrectionDerivativePredictions) {

      if (!prediction.getPredictedValue().isPresent()) {
        return Double.NaN;
      }
    }

    // Validate we got 2 predictions for magnitudee correction derivative calculation
    if (magnitudeCorrectionDerivativePredictions.size() != 2) {

      throw new IllegalStateException(
          "Expected 2 magnitude correction predictions for calculating magnitude correction prediction derivative");
    }

    double magnitudeCorrectionDerivativePredictionClosest = magnitudeCorrectionDerivativePredictions
        .get(0).getPredictedValue().orElseThrow(AssertionError::new).getMeasurementValue()
        .getValue();
    double magnitudeCorrectionDerivativePredictionFurthest = magnitudeCorrectionDerivativePredictions
        .get(1).getPredictedValue().orElseThrow(AssertionError::new).getMeasurementValue()
        .getValue();

    // Calculate radial travel time derivative
    return
        (magnitudeCorrectionDerivativePredictionClosest
            - magnitudeCorrectionDerivativePredictionFurthest)
            / this.gridCylinderRadiusDegrees * 2;
  }


  private void setMinimumMagnitude(
      PhaseInfo.Builder phaseInfoBuilder,
      double minimumMagnitude
  ) {

    phaseInfoBuilder.setMinimumMagnitude(minimumMagnitude);
  }

  /**
   * Returns an array of the names of parameters that have not been set.
   * @return an array of strings, possibly empty but never null.
   */
  public String[] missingParameterNames() {
    List<String> missingParameters = new ArrayList<>();
    if (predictionUtility == null) {
      missingParameters.add("predictionUtility");
    }
    if (travelTimePredictionEarthModel == null) {
      missingParameters.add("travelTimePredictionEarthModel");
    }
    if (magnitudeAttenuationPredictionEarthModel == null) {
      missingParameters.add("magnitudeAttenuationPredictionEarthModel");
    }
    if (Double.isNaN(gridCylinderRadiusDegrees)) {
      missingParameters.add("gridCylinderRadiusDegrees");
    }
    if (Double.isNaN(gridCylinderHeightKm)) {
      missingParameters.add("gridCylinderHeightKm");
    }
    if (Double.isNaN(gridPointLatDegrees)) {
      missingParameters.add("gridPointLatDegrees");
    }
    if (Double.isNaN(gridPointLonDegrees)) {
      missingParameters.add("gridPointLonDegrees");
    }
    if (Double.isNaN(gridPointDepthKm)) {
      missingParameters.add("gridPointDepthKm");
    }
    if (Double.isNaN(minimumMagnitude)) {
      missingParameters.add("minimumMagnitude");
    }
    if (referenceStation == null) {
      missingParameters.add("referenceStation");
    }
    if (phaseType == null) {
      missingParameters.add("phaseType");
    }
    return missingParameters.toArray(new String[missingParameters.size()]);
  }

  public SignalFeaturePredictionUtility predictionUtility() {
    return predictionUtility;
  }

  public PhaseInfoGenerator predictionUtility(
      SignalFeaturePredictionUtility predictionUtility) {
    Objects.requireNonNull(predictionUtility, "Null predictionUtility");
    this.predictionUtility = predictionUtility;
    return this;
  }

  public String travelTimePredictionEarthModel() {
    return travelTimePredictionEarthModel;
  }

  public PhaseInfoGenerator travelTimePredictionEarthModel(String travelTimePredictionEarthModel) {
    Validate.notEmpty(travelTimePredictionEarthModel,
        "Null or empty travelTimePredictionEarthModel");
    this.travelTimePredictionEarthModel = travelTimePredictionEarthModel;
    return this;
  }

  public String magnitudeAttenuationPredictionEarthModel() {
    return magnitudeAttenuationPredictionEarthModel;
  }

  public PhaseInfoGenerator magnitudeAttenuationPredictionEarthModel(
      String magnitudeAttenuationPredictionEarthModel) {
    Validate.notEmpty(magnitudeAttenuationPredictionEarthModel,
        "Null or empty magnitudeAttenuationPredictionEarthModel");
    this.magnitudeAttenuationPredictionEarthModel = magnitudeAttenuationPredictionEarthModel;
    return this;
  }

  public double gridCylinderRadiusDegrees() {
    return gridCylinderRadiusDegrees;
  }

  public PhaseInfoGenerator gridCylinderRadiusDegrees(double gridCylinderRadiusDegrees) {
    if (Double.isNaN(gridCylinderRadiusDegrees) || gridCylinderRadiusDegrees <= 0.0) {
      throw new IllegalArgumentException("gridCylinderRadiusDegrees must be > 0.0: " +
          gridCylinderRadiusDegrees);
    }
    this.gridCylinderRadiusDegrees = gridCylinderRadiusDegrees;
    return this;
  }

  public double gridCylinderHeightKm() {
    return gridCylinderHeightKm;
  }

  public PhaseInfoGenerator gridCylinderHeightKm(double gridCylinderHeightKm) {
    if (Double.isNaN(gridCylinderHeightKm) || gridCylinderHeightKm <= 0.0) {
      throw new IllegalArgumentException("gridCylinderHeightKm must be > 0.0: " +
          gridCylinderHeightKm);
    }
    this.gridCylinderHeightKm = gridCylinderHeightKm;
    return this;
  }

  public double gridPointLatDegrees() {
    return gridPointLatDegrees;
  }

  public PhaseInfoGenerator gridPointLatDegrees(double gridPointLatDegrees) {
    if (Double.isNaN(gridPointLatDegrees) || Math.abs(gridPointLatDegrees) > MAX_LATITUDE) {
      throw new IllegalArgumentException("gridPointLatDegrees not in [-90 - +90]: " + gridPointLatDegrees);
    }
    this.gridPointLatDegrees = gridPointLatDegrees;
    return this;
  }

  public double gridPointLonDegrees() {
    return gridPointLonDegrees;
  }

  public PhaseInfoGenerator gridPointLonDegrees(double gridPointLonDegrees) {
    if (Double.isNaN(gridPointLonDegrees) || Math.abs(gridPointLonDegrees) > MAX_LONGITUDE) {
      throw new IllegalArgumentException("gridPointLonDegrees not in [-180 - +180]: " + gridPointLonDegrees);
    }
    this.gridPointLonDegrees = gridPointLonDegrees;
    return this;
  }

  public double gridPointDepthKm() {
    return gridPointDepthKm;
  }

  public PhaseInfoGenerator gridPointDepthKm(double gridPointDepthKm) {
    if (Double.isNaN(gridPointDepthKm)) {
      throw new IllegalArgumentException("invalid gridPointDepthKm: " + gridPointDepthKm);
    }
    this.gridPointDepthKm = gridPointDepthKm;
    return this;
  }

  public double minimumMagnitude() {
    return minimumMagnitude;
  }

  public PhaseInfoGenerator minimumMagnitude(double minimumMagnitude) {
    if (Double.isNaN(minimumMagnitude) || minimumMagnitude < 0.0) {
      throw new IllegalArgumentException("minimumMagnitude must be >= 0: " + minimumMagnitude);
    }
    this.minimumMagnitude = minimumMagnitude;
    return this;
  }

  public ReferenceStation referenceStation() {
    return referenceStation;
  }

  public PhaseInfoGenerator referenceStation(ReferenceStation referenceStation) {
    Objects.requireNonNull(referenceStation, "Null referenceStation");
    this.referenceStation = referenceStation;
    return this;
  }

  public PhaseType phaseType() {
    return phaseType;
  }

  public PhaseInfoGenerator phaseType(PhaseType phaseType) {
    Objects.requireNonNull(phaseType, "Null phaseType");
    this.phaseType = phaseType;
    return this;
  }
}
