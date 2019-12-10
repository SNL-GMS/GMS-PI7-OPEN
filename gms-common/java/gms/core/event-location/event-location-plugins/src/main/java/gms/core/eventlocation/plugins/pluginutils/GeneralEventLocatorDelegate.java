package gms.core.eventlocation.plugins.pluginutils;

import static com.google.common.collect.Streams.zip;

import gms.core.eventlocation.plugins.EventLocationDefinition;
import gms.core.eventlocation.plugins.exceptions.TooManyRestraintsException;
import gms.core.eventlocation.plugins.pluginutils.LocatorAlgorithm.Builder;
import gms.core.eventlocation.plugins.pluginutils.seedgeneration.RestrainedSeedGenerator;
import gms.core.eventlocation.plugins.pluginutils.seedgeneration.SimpleSeedGenerator;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.DoubleValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.PhaseType;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.DepthRestraintType;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.ElevationCorrection1dDefinition;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.Ellipse;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.EllipticityCorrection1dDefinition;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.EventLocation;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.FeaturePrediction;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.FeaturePredictionCorrection;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.LocationBehavior;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.LocationRestraint;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.LocationSolution;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.LocationUncertainty;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.PreferredLocationSolution;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.RestraintType;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.ScalingFactorType;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.EnumeratedMeasurementValue.PhaseTypeMeasurementValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FeatureMeasurement;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FeatureMeasurementType;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FeatureMeasurementTypes;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.InstantValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Location;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.NumericMeasurementValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetectionHypothesis;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceStation;
import gms.shared.utilities.geomath.GeoMath;
import gms.shared.utilities.geomath.StatUtil;
import gms.shared.utilities.signalfeaturepredictionutility.SignalFeaturePredictionUtility;
import gms.core.eventlocation.plugins.pluginutils.seedgeneration.SeedGenerator;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.util.Pair;

public class GeneralEventLocatorDelegate<T extends RealMatrix> {

  private SeedGenerator seedGenerator;
  private LocatorAlgorithm.Builder<T> algorithmBuilder;
  private SignalFeaturePredictionUtility signalFeaturePredictionUtility;

  public void initialize(SeedGenerator seedGenerator,
      LocatorAlgorithm.Builder<T> algorithmBuilder,
      SignalFeaturePredictionUtility signalFeaturePredictionUtility) {
    this.seedGenerator = seedGenerator;
    this.signalFeaturePredictionUtility = signalFeaturePredictionUtility;
    this.algorithmBuilder = algorithmBuilder;
  }

  /**
   * Perform event location via Geigers uing givin observations, stations, andp paramaeters
   *
   * @param observations Observersions in the form of signal detection hypotheses
   * @param stations stations (for receiver locations)
   * @param parameters parameters defining behavior of algorithm7
   * @return A location solution representing an estimate for the location of the event and the
   * errors (error ellipse, etc) associated with the estimation
   */
  public List<LocationSolution> locate(List<SignalDetectionHypothesis> observations,
      List<ReferenceStation> stations, EventLocationDefinition parameters)
      throws TooManyRestraintsException {

    Map<SignalDetectionHypothesis, ReferenceStation> observationStationMap = new HashMap<>();

    zip(observations.stream(), stations.stream(), Pair::create)
        .forEach(pair -> observationStationMap.put(pair.getKey(), pair.getValue()));

    Map<PhaseType, List<Pair<Location, FeatureMeasurement<?>>>> phaseLocationMeasurementMap =
        getPhaseLocationMeasurementMap(
            getPhaseTypeSignalDetectionHypothesisMap(observations), observationStationMap);

    Map<PhaseType, List<Pair<Location, FeatureMeasurementType<?>>>> phaseLocationMeasurementTypeMap
        = new EnumMap<>(PhaseType.class);

    for (PhaseType phaseType : phaseLocationMeasurementMap.keySet()) {
      for (Pair<Location, FeatureMeasurement<?>> pair : phaseLocationMeasurementMap
          .get(phaseType)) {
        phaseLocationMeasurementTypeMap.computeIfAbsent(phaseType, p -> new LinkedList<>());
        phaseLocationMeasurementTypeMap.get(phaseType).add(Pair.create(pair.getKey(),
            pair.getValue().getFeatureMeasurementType()));
      }
    }

    List<PhaseType> deterministicallyOrderedPhaseList = phaseLocationMeasurementMap.keySet()
        .stream().sorted().collect(Collectors.toList());

    List<FeatureMeasurement<?>> validFeatureMeasurements = new LinkedList<>();

    deterministicallyOrderedPhaseList.forEach(phaseType ->
        validFeatureMeasurements.addAll(phaseLocationMeasurementMap.get(phaseType).stream()
            .map(pair -> pair.getValue()).collect(Collectors.toList())));

    if (validFeatureMeasurements.size() < parameters.getMinimumNumberOfObservations()) {
      throw new IllegalArgumentException(
          "Cannot locate with less than " + parameters.getMinimumNumberOfObservations()
              + " observations");
    }

    List<Pair<Double, Double>> pairs = getValueErrorPairs(validFeatureMeasurements);

    double[] vectorValues = new double[pairs.size()];
    double[] errorValues = new double[pairs.size()];

    for (int i = 0; i < vectorValues.length; i++) {
      vectorValues[i] = pairs.get(i).getKey();
      errorValues[i] = pairs.get(i).getValue();
    }

    RealMatrix observationsAndErrors = new Array2DRowRealMatrix(pairs.size(), 2);
    observationsAndErrors.setColumnVector(0, new ArrayRealVector(vectorValues));
    observationsAndErrors.setColumnVector(1, new ArrayRealVector(errorValues));

    //TODO: Return error ellipses, f-statistic, etc

    return buildLocationSolutions(parameters, observationsAndErrors, validFeatureMeasurements,
        phaseLocationMeasurementTypeMap, deterministicallyOrderedPhaseList, observationStationMap);

  }

  private List<LocationSolution> buildLocationSolutions(
      EventLocationDefinition parameters, RealMatrix observationsAndErrors,
      List<FeatureMeasurement<?>> validFeatureMeasurements,
      Map<PhaseType, List<Pair<Location, FeatureMeasurementType<?>>>> phaseLocationMeasurementTypeMap,
      List<PhaseType> deterministicallyOrderedPhaseList,
      Map<SignalDetectionHypothesis, ReferenceStation> observationStationMap
  ) throws TooManyRestraintsException {

    List<LocationSolution> locationSolutions = new ArrayList<>();

    for (LocationRestraint locationRestraint : parameters.getLocationRestraints()) {
      EventLocation seedLocation =
          new RestrainedSeedGenerator(seedGenerator, locationRestraint).generate(
              null,
              observationStationMap
          );
      if (!GeoMath.isNormalizedLatLon(seedLocation.getLatitudeDegrees(),
          seedLocation.getLongitudeDegrees())) {
        throw new IllegalStateException("Encountered non-normalized lat/lon");
      }

      List<FeaturePrediction<?>> outFeaturePredictions = new ArrayList<>();

      LocatorAlgorithm<T> algorithm = restrainBuilder(algorithmBuilder,
          locationRestraint).build();

      Triple<RealVector, RealMatrix, RealMatrix> finalAnswerTriple = algorithm.locate(
          new ArrayRealVector(new double[]{
              seedLocation.getLatitudeDegrees(),
              seedLocation.getLongitudeDegrees(),
              seedLocation.getDepthKm(),
              seedLocation.getTime().getEpochSecond() +
                  (seedLocation.getTime().getNano() / 1_000_000_000.0)
          }),
          observationsAndErrors,
          getProcessedFFunction(
              signalFeaturePredictionUtility,
              algorithm,
              parameters.getEarthModel(),
              phaseLocationMeasurementTypeMap,
              deterministicallyOrderedPhaseList,
              parameters.isApplyTravelTimeCorrections() ? List.of(
                  ElevationCorrection1dDefinition.create(true),
                  EllipticityCorrection1dDefinition.create()
              ) : List.of(),
              locationRestraint,
              featurePredictions -> {
                outFeaturePredictions.clear();
                outFeaturePredictions.addAll(featurePredictions);
              })
      );

      RealVector finalLocation = finalAnswerTriple.getLeft();

      EventLocation eventLocation = EventLocation.from(
          finalLocation.getEntry(0),
          finalLocation.getEntry(1),
          finalLocation.getEntry(2),
          Instant.ofEpochSecond((long) finalLocation.getEntry(3),
              (long) (1_000_000_000 * (finalLocation.getEntry(3) - ((long) finalLocation
                  .getEntry(3)))))
      );

    Stream<Pair<FeatureMeasurement<?>, FeaturePrediction<?>>> definingFeaturePairsStream =
        zip(validFeatureMeasurements.stream(), outFeaturePredictions.stream(), Pair::create)
            .filter(pair -> pair.getValue().getPredictedValue().isPresent())
            .map(pair -> Pair.create(pair.getKey(), pair.getValue()));

    List<Pair<FeatureMeasurement<?>, FeaturePrediction<?>>> definingFeaturePairs
        = definingFeaturePairsStream.collect(Collectors.toList());

    List<FeaturePrediction<?>> featurePredictions = definingFeaturePairs.stream().map(Pair::getValue)
        .collect(Collectors.toList());

      Set<LocationBehavior> locationBehaviors = constructLocationBehaviors(
          finalAnswerTriple.getRight(), definingFeaturePairs);

      RealVector weightedResiduals =
          finalAnswerTriple.getRight().getColumnVector(0).ebeMultiply(
              finalAnswerTriple.getRight().getColumnVector(1));

      double scalingFactor = StatUtil.kappas(
          parameters.getScalingFactorType(),
          parameters.getkWeight(),
          weightedResiduals.getDimension(),
          finalLocation.getDimension(),
          weightedResiduals.dotProduct(weightedResiduals),
          parameters.getUncertaintyProbabilityPercentile(),
          parameters.getAprioriVariance())[1];

      RealMatrix covarianceMatrix = fillCovarianceMatrix(
          finalAnswerTriple.getMiddle().scalarMultiply(scalingFactor),
          getExclusionBitsForRestraint(locationRestraint));

      double[] ellipseAxes = Util.compute2dEllipse(covarianceMatrix, 0, 1);

      double timeUncertainty = Math.sqrt(covarianceMatrix.getEntry(3, 3));

      locationSolutions.add(LocationSolution.create(
          eventLocation,
          locationRestraint,
          LocationUncertainty.from(
              covarianceMatrix.getEntry(0, 0),
              covarianceMatrix.getEntry(0, 1),
              covarianceMatrix.getEntry(0, 2),
              covarianceMatrix.getEntry(0, 3),
              covarianceMatrix.getEntry(1, 1),
              covarianceMatrix.getEntry(1, 2),
              covarianceMatrix.getEntry(1, 3),
              covarianceMatrix.getEntry(2, 2),
              covarianceMatrix.getEntry(2, 3),
              covarianceMatrix.getEntry(3, 3),
              0.0,
              Set.of(Ellipse.from(
                  ScalingFactorType.CONFIDENCE,
                  parameters.getkWeight(),
                  parameters.getUncertaintyProbabilityPercentile(),
                  ellipseAxes[0],
                  ellipseAxes[2],
                  ellipseAxes[1],
                  ellipseAxes[3],
                  Math.sqrt(covarianceMatrix.getEntry(2, 2)),
                  Duration.ofSeconds(
                      (long) timeUncertainty,
                      (long) (1_000_000_000 * (timeUncertainty - ((long) timeUncertainty)))))
              ),

              //TODO: Populate ellisoids?
              Set.of()),
          locationBehaviors, new HashSet<>(featurePredictions)));
    }

    return locationSolutions;
  }

  private static <V extends RealMatrix> Builder<V> restrainBuilder(Builder<V> builder,
      LocationRestraint locationRestraint) {
    return builder
        .withLatitudeParameterConstrainedToSeededValue(
            locationRestraint.getLatitudeRestraintType().equals(RestraintType.FIXED))
        .withLongitudeParameterConstrainedToSeededValue(
            locationRestraint.getLongitudeRestraintType().equals(RestraintType.FIXED))
        .withDepthParameterConstrainedToSeededValue(
            locationRestraint.getDepthRestraintType()
                .equals(DepthRestraintType.FIXED_AT_SURFACE)
                || locationRestraint.getDepthRestraintType()
                .equals(DepthRestraintType.FIXED_AT_DEPTH))
        .withTimeParameterConstrainedToSeededValue(
            locationRestraint.getTimeRestraintType().equals(RestraintType.FIXED));
  }

  /**
   * Construct a set of location behaviours from a matrix containing residuals and weights and a
   * list of FeatureMeasurement, FeaturePrediction pairs
   */
  private static Set<LocationBehavior> constructLocationBehaviors(RealMatrix valueInfoMatrix,
      List<Pair<FeatureMeasurement<?>, FeaturePrediction<?>>> featurePredictionPairs) {
    //double[] values = valueInfoMatrix.getColumn(0);
    double[] residuals = valueInfoMatrix.getColumn(0);
    double[] weights = valueInfoMatrix.getColumn(1);

    Set<LocationBehavior> locationBehaviors = new HashSet<>();

    for (int i = 0; i < featurePredictionPairs.size(); i++) {
      locationBehaviors.add(LocationBehavior.from(
          residuals[i],
          weights[i],
          true,

          //TODO: Uses actual objects????
          featurePredictionPairs.get(i).getValue().getId(),
          featurePredictionPairs.get(i).getKey().getId()
      ));
    }
    return locationBehaviors;
  }

  private static Map<PhaseType, List<SignalDetectionHypothesis>> getPhaseTypeSignalDetectionHypothesisMap
      (List<SignalDetectionHypothesis> signalDetectionHypotheses) {

    Map<PhaseType, List<SignalDetectionHypothesis>> map = new EnumMap<>(PhaseType.class);

    signalDetectionHypotheses.forEach(signalDetectionHypothesis -> {
      List<FeatureMeasurement<?>> featureMeasurements =
          new ArrayList<>(signalDetectionHypothesis.getFeatureMeasurements());

      PhaseType phaseType = getPhaseTypeMeasurement(featureMeasurements);

      map.computeIfAbsent(phaseType, p -> new LinkedList<>());
      map.get(phaseType).add(signalDetectionHypothesis);
    });

    return map;

  }

  private static Map<PhaseType, List<Pair<Location, FeatureMeasurement<?>>>> getPhaseLocationMeasurementMap(
      Map<PhaseType, List<SignalDetectionHypothesis>> phaseSignalDetectionMap,
      Map<SignalDetectionHypothesis, ReferenceStation> observationStationMap
  ) {

    Map<PhaseType, List<Pair<Location, FeatureMeasurement<?>>>> map
        = new EnumMap<>(PhaseType.class);

    phaseSignalDetectionMap.keySet().forEach(phaseType -> {
      map.computeIfAbsent(phaseType, p -> new LinkedList<>());

      phaseSignalDetectionMap.get(phaseType).forEach(signalDetectionHypothesis -> {
        Location location = Location.from(
            observationStationMap.get(signalDetectionHypothesis).getLatitude(),
            observationStationMap.get(signalDetectionHypothesis).getLongitude(),
            0.0,
            observationStationMap.get(signalDetectionHypothesis).getElevation());
        signalDetectionHypothesis.getFeatureMeasurements().stream()
            .filter(featureMeasurement ->
                featureMeasurement.getFeatureMeasurementType()
                    .equals(FeatureMeasurementTypes.ARRIVAL_TIME)
                    || featureMeasurement.getFeatureMeasurementType()
                    .equals(FeatureMeasurementTypes.RECEIVER_TO_SOURCE_AZIMUTH)
                    || featureMeasurement.getFeatureMeasurementType()
                    .equals(FeatureMeasurementTypes.SLOWNESS))
            .forEach(featureMeasurement ->
                map.get(phaseType).add(Pair.create(
                    location, featureMeasurement)));
      });
    });
    return map;
  }

  /**
   * Extract value/error pairs from signal detections
   */
  private static List<Pair<Double, Double>> getValueErrorPairs(
      List<FeatureMeasurement<?>> validFeatureMeasurements) {

    return validFeatureMeasurements.stream()
        .map(fm -> {
          double value;
          double standardDeviation;
          if (fm.getFeatureMeasurementType().equals(FeatureMeasurementTypes.ARRIVAL_TIME)) {
            InstantValue instantValue = ((InstantValue) fm.getMeasurementValue());
            value = instantValue.getValue().getEpochSecond()
                + instantValue.getValue().getNano() / 1_000_000_000.0;
            Duration error = instantValue.getStandardDeviation();
            standardDeviation = error.getSeconds() + error.getNano() / 1_000_000_000.0;
          } else if (fm.getFeatureMeasurementType().equals(FeatureMeasurementTypes.SLOWNESS)
              || fm.getFeatureMeasurementType()
              .equals(FeatureMeasurementTypes.RECEIVER_TO_SOURCE_AZIMUTH)) {
            DoubleValue doubleValue =
                ((NumericMeasurementValue) fm.getMeasurementValue()).getMeasurementValue();
            value = doubleValue.getValue();
            standardDeviation = doubleValue.getStandardDeviation();
          } else {
            throw new IllegalArgumentException("Invalid observation type");
          }
          return Pair.create(value, standardDeviation);
        }).collect(Collectors.toList());
  }

  private static PhaseType getPhaseTypeMeasurement(
      List<FeatureMeasurement<?>> featureMeasurements) {

    List<FeatureMeasurement<PhaseTypeMeasurementValue>> phaseTypeMeasurments = featureMeasurements
        .stream()
        .filter(featureMeasurement -> featureMeasurement.getFeatureMeasurementType()
            .equals(FeatureMeasurementTypes.PHASE))
        .map(
            featureMeasurement -> (FeatureMeasurement<PhaseTypeMeasurementValue>) featureMeasurement)
        .collect(Collectors.toList());

    if (phaseTypeMeasurments.size() > 1) {
      throw new IllegalArgumentException("More than one phase type measurement found");
    } else if (phaseTypeMeasurments.isEmpty()) {
      throw new IllegalArgumentException("No phase type measurement in set of measurements");
    }

    return phaseTypeMeasurments.get(0).getMeasurementValue().getValue();
  }

  private static <T extends RealMatrix> Function<RealVector, Pair<T, T>> getProcessedFFunction(
      SignalFeaturePredictionUtility signalFeaturePredictionUtility,
      LocatorAlgorithm<T> algorithm,
      final String model,
      final Map<PhaseType, List<Pair<Location, FeatureMeasurementType<?>>>> phaseLocationMap,
      final List<PhaseType> orderedDistinctPhases,
      final List<FeaturePredictionCorrection> newCorrections,
      final LocationRestraint locationRestraint,
      Consumer<List<FeaturePrediction<?>>> featurePredictionConsumer) {

    return signalFeaturePredictionUtility.getFFunction(
        model,
        phaseLocationMap,
        orderedDistinctPhases,
        newCorrections,
        featurePredictionConsumer,
        algorithm.getErrorValueNaNProcessor(),
        algorithm.getJacobianNaNProcessor(),
        locationRestraint
    );

  }

  private static BitSet getExclusionBitsForRestraint(LocationRestraint locationRestraint) {
    BitSet exclusionBits = new BitSet(4);

    exclusionBits.set(0, locationRestraint.getLatitudeRestraintDegrees().isPresent());
    exclusionBits.set(1, locationRestraint.getLongitudeRestraintDegrees().isPresent());
    exclusionBits.set(2, locationRestraint.getDepthRestraintKm().isPresent());
    exclusionBits.set(3, locationRestraint.getTimeRestraint().isPresent());

    return exclusionBits;
  }

  static RealMatrix fillCovarianceMatrix(RealMatrix covarianceMatrix, BitSet restrainedBitSet) {
    restrainedBitSet.stream();

    if (covarianceMatrix.getColumnDimension() == 4) {
      return covarianceMatrix;
    }

    RealMatrix columnSwitchedCovarianceMatrix = new Array2DRowRealMatrix(4, 4);
    RealMatrix zeroesCovarianceMatrix = new Array2DRowRealMatrix(4, 4);
    RealMatrix newCovarianceMatrix = new Array2DRowRealMatrix(4, 4);
    zeroesCovarianceMatrix.setSubMatrix(covarianceMatrix.getData(), 0, 0);

    BitSet unrestrainedBitSet = restrainedBitSet.get(0, 4);
    unrestrainedBitSet.flip(0, 4);

    int[] indexes = unrestrainedBitSet.stream().toArray();

    for (int i = 0; i < indexes.length; i++) {
      columnSwitchedCovarianceMatrix
          .setColumnVector(indexes[i], zeroesCovarianceMatrix.getColumnVector(i));
    }

    for (int i = 0; i < indexes.length; i++) {
      newCovarianceMatrix.setRowVector(indexes[i], columnSwitchedCovarianceMatrix.getRowVector(i));
    }

    return newCovarianceMatrix;
  }

  public static SeedGenerator getDefaultSeedGenerator(Optional<PreferredLocationSolution> start) {
    return start.map(
        preferredLocationSolution -> (SeedGenerator) (defaultSeedLocation, observationStationMap) ->
            preferredLocationSolution.getLocationSolution().getLocation())
        .orElseGet(SimpleSeedGenerator::new);
  }
}
