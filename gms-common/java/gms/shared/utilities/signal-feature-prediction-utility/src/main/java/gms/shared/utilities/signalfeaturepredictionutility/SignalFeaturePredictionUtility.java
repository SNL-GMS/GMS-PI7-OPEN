package gms.shared.utilities.signalfeaturepredictionutility;

// TODO: Maybe switch to PluginConfiguration in osd-commonobjects in the future???
//import gms.shared.mechanisms.objectstoragedistribution.coi.common.PluginConfiguration;

import gms.core.featureprediction.common.objects.PluginConfiguration;
import gms.core.featureprediction.exceptions.MissingEarthModelOrPhaseException;
import gms.core.featureprediction.plugins.SignalFeaturePredictorPlugin;
import gms.core.featureprediction.plugins.implementations.signalfeaturepredictor.FeaturePredictionCorrection1dApplier;
import gms.core.featureprediction.plugins.implementations.signalfeaturepredictor.PredictionType;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.DoubleValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.PhaseType;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.DepthRestraintType;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.ElevationCorrection1dDefinition;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.EventLocation;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.FeaturePrediction;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.FeaturePredictionCorrection;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.FeaturePredictionDerivativeType;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.LocationRestraint;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.LocationSolution;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.RestraintType;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Channel;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FeatureMeasurementType;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.InstantMeasurementType;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.InstantValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Location;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.NumericMeasurementType;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.NumericMeasurementValue;
import gms.shared.mechanisms.pluginregistry.PluginInfo;
import gms.shared.mechanisms.pluginregistry.PluginRegistry;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;
import org.apache.commons.lang3.Validate;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SignalFeaturePredictionUtility {

  // Why not slf4j?
  private static final Logger logger = LogManager.getLogger(SignalFeaturePredictionUtility.class);

  private final List<SignalFeaturePredictorPlugin> plugins;

  /**
   * Constructor
   *
   * @param pluginRegistry a registry for looking up plugins that implement
   * SignalFeaturePredictorPlugin for each element of pluginInfoList. This parameter must not be
   * null.
   * @param pluginInfoList a list of PluginInfo for plugins that implement
   * SignalFeaturePredictorPlugin. This parameter must not be null or empty.
   */
  public SignalFeaturePredictionUtility(
      final PluginRegistry pluginRegistry,
      final List<PluginInfo> pluginInfoList) {

    Validate.notNull(pluginRegistry, "pluginRegistry is required");
    Validate.notEmpty(pluginInfoList, "pluginInfoList must not be null or empty");

    pluginRegistry.loadAndRegister();

    // Load up the plugins here, not in every call to predict.
    this.plugins = new ArrayList<>(pluginInfoList.size());

    for (PluginInfo pluginInfo : pluginInfoList) {
      Optional<SignalFeaturePredictorPlugin> opt = pluginRegistry
          .lookup(pluginInfo, SignalFeaturePredictorPlugin.class);
      if (opt.isPresent()) {
        SignalFeaturePredictorPlugin plugin = opt.get();
        // A little idiot check to ensure we don't have the same plugin in the list twice.
        if (!plugins.contains(plugin)) {
          plugins.add(plugin);
        }
      } else {
        // Display an error message, but don't throw an exception unless none of the
        // plugins can be located.
        logger.error("SignalFeaturePredictorPlugin could not be found in the plugin registry for: "
            + pluginInfo.toString());
      }
    }

    // If no SignalFeaturePredictorPlugins were loaded, this instance is useless as a screen door
    // on a submarine.
    if (plugins.isEmpty()) {
      throw new IllegalArgumentException("no SignalFeaturePredictorPlugins could be loaded given" +
          " the pluginInfo: " + pluginInfoList);
    }
  }

  /**
   * Default constructor.
   */
  public SignalFeaturePredictionUtility() {
    this(
        PluginRegistry.getRegistry(),
        List.of(PluginInfo.from("signalFeaturePredictor1dPlugin", "1.0.0"))
    );
  }

  /**
   * A cleanup method to remove plugins created per thread during the population of
   * a geotess model with grid nodes.
   */
  public static void freeThreadSpecificPlugins() {
    FeaturePredictionCorrection1dApplier.removeThreadSpecificPlugins();
  }

  /**
   * Return a single feature prediction for a single event, receiver, and type.
   *
   * @param type Type of prediction
   * @param sourceLocation event location
   * @param receiverLocation reciever location
   * @param phase phase
   * @param model earth model
   * @param correctionDefinitions corrections to apply
   * @param <T> used to match V, a specific parameterised FeatureMeasurementType
   * @param <V> type of measurment, extends FeatureMeasurementType<T></T>
   * @return Single feature prediction of type T
   */
  @SuppressWarnings("unchecked")
  //TODO: We should some how add static type safety to the predictor, if at all possible.
  public <T, V extends FeatureMeasurementType<T>> FeaturePrediction<T> predictSingle(
      V type,
      EventLocation sourceLocation,
      Location receiverLocation,
      PhaseType phase,
      String model,
      List<FeaturePredictionCorrection> correctionDefinitions
  ) throws Exception {
    Validate.notNull(correctionDefinitions,
        "SignalFeaturePredictionUtility.predict: correctionDefinitions is null!");

    if (phase.getFinalPhase().equals(PhaseType.UNKNOWN)) {
      correctionDefinitions = correctionDefinitions.stream()
          .filter(
              featurePredictionCorrection -> !(featurePredictionCorrection instanceof ElevationCorrection1dDefinition))
          .collect(Collectors.toList());
    }

    // Convert to an array, since that's the form the plugin expects.
    final FeaturePredictionCorrection[] correctionDefsArray = correctionDefinitions
        .toArray(new FeaturePredictionCorrection[correctionDefinitions.size()]);

    FeaturePrediction<?> featurePrediction = plugins.get(0).initialize(new PluginConfiguration())
        .predict(
            model,
            type,
            sourceLocation,
            receiverLocation,
            phase,
            correctionDefsArray
        );

    if (type.equals(featurePrediction.getPredictionType())) {
      return FeaturePrediction.from(
          featurePrediction.getId(),
          featurePrediction.getPhase(),
          Optional.ofNullable((T) featurePrediction.getPredictedValue().orElse(null)),
          featurePrediction.getFeaturePredictionComponents(),
          featurePrediction.isExtrapolated(),
          (V) featurePrediction.getPredictionType(),
          featurePrediction.getSourceLocation(),
          featurePrediction.getReceiverLocation(),
          featurePrediction.getChannelId(),
          featurePrediction.getFeaturePredictionDerivativeMap()
      );
    } else {
      throw new IllegalStateException("Returned feature prediction is of wrong type");
    }
  }

  /**
   *
   * Return a list of predictions such that the returned list matches item-by-item the given
   * list of feature measurement types.
   *
   * This overload takes a list of feature measurement types and a list of receiver locations.
   * The two lists must match in length. let T = (t_1, t_2, t_3, ... t_n) be the list of
   * feature measurement types, and let L = (l_1, l_2, l_3, ... l_n) be the list of receiver locations.
   * then each pair (t_i, l_i) will be sent to the feature predictor to get a prediction of type
   * t_i for location l_i; the prediction will be added to the output list.
   *
   * This ordering is imposed for use with the event locator.
   *
   * TODO: rename this method? this ordering is unique to this overload.
   *
   * @param types Ordered list of feature measurement types, T
   * @param sourceLocation event location
   * @param receiverLocations list of receiver locations, L
   * @param phase phase to predict for
   * @param model model to use for prediction
   * @param correctionDefinitions list of definitions to apply to the prediction values
   * @return A list of feature predictions P = (p_1, p_2, p_3, ... p_n) such that p_i is the prediction
   * for (t_i, l_i)
   * @throws Exception  TODO dont throw exception.
   *
   */
  public List<FeaturePrediction<?>> predict(
      List<FeatureMeasurementType<?>> types,
      EventLocation sourceLocation,
      List<Location> receiverLocations,
      PhaseType phase,
      String model,
      List<FeaturePredictionCorrection> correctionDefinitions) throws Exception {

    //TODO: Do this in overloaded predict method
    Validate.notNull(correctionDefinitions,
        "SignalFeaturePredictionUtility.predict: correctionDefinitions is null!");

    if (phase.getFinalPhase().equals(PhaseType.UNKNOWN)) {
      correctionDefinitions = correctionDefinitions.stream()
          .filter(
              featurePredictionCorrection -> !(featurePredictionCorrection instanceof ElevationCorrection1dDefinition))
          .collect(Collectors.toList());
    }

    // Convert to an array, since that's the form the plugin expects.
    final FeaturePredictionCorrection[] correctionDefsArray = correctionDefinitions
        .toArray(new FeaturePredictionCorrection[correctionDefinitions.size()]);

    // Call the plugins.
    List<FeaturePrediction<?>> featurePredictions = new ArrayList<>();

    //TODO: having a set of plugins may be invalid
    SignalFeaturePredictorPlugin plugin = plugins.get(0);

    //Use a for each loop so any exception can escape
    for (int i = 0; i < types.size(); i++) {
      try {
        featurePredictions.add(
            plugin.initialize(new PluginConfiguration())
                .predict(
                    model,
                    types.get(i),
                    sourceLocation,
                    receiverLocations.get(i),
                    phase,
                    PredictionType
                        .valueOf(types.get(i).getFeatureMeasurementTypeName())
                        .correctionsValid(correctionDefsArray) ? correctionDefsArray
                        : new FeaturePredictionCorrection[]{}
                ));
      } catch (Exception e) {
        logger.error("Plugin predict(..) method threw an exception: " + e.toString());
        throw e;
      }
    }

    return featurePredictions;

  }

  /**
   *
   * TODO: NOTE: The below description assumes only a single plugin. We probably only want to use
   * TODO: a single plugin, which is not how this method is currently implemented.
   *
   * Return a location solution containing a set of feature predictions, P. P contains every possible
   * combination of the prediction types and receivers. For this overload, the receivers are
   * RecieverLocations.
   *
   * Let T = {t_1, t_2, t_3, ... t_n} be the set of feature prediction types,
   * L = { l_1, l_2, l_3, ... l_m } be the set of receiver locations. Then there will be a prediction
   * for each element in T x L; the size of the feature prediction list P will be n * m.
   *
   * @param types List of feature measurement types, T
   * @param sourceLocation event location
   * @param receiverLocations List of receiver locations, L
   * @param phase phase to make predictions for
   * @param model earth model to use for predictions
   * @param correctionDefinitions set of corrections to apply to the predictions.
   * @return List of feature predictions P = {p_00, p_01, ... p_nm} where p_ij is the feature prediction
   * for (t_i, l_j)
   *
   * @throws Exception TODO: dont throw Exception
   */
  public List<FeaturePrediction<?>> predict(
      List<FeatureMeasurementType<?>> types,
      EventLocation sourceLocation,
      Set<Location> receiverLocations,
      PhaseType phase,
      String model,
      List<FeaturePredictionCorrection> correctionDefinitions) throws Exception {

    //TODO: Do this in overloaded predict method
    Validate.notNull(correctionDefinitions,
        "SignalFeaturePredictionUtility.predict: correctionDefinitions is null!");

    if (phase.getFinalPhase().equals(PhaseType.UNKNOWN)) {
      correctionDefinitions = correctionDefinitions.stream()
          .filter(
              featurePredictionCorrection -> !(featurePredictionCorrection instanceof ElevationCorrection1dDefinition))
          .collect(Collectors.toList());
    }

    // Convert to an array, since that's the form the plugin expects.
    final FeaturePredictionCorrection[] correctionDefsArray = correctionDefinitions
        .toArray(new FeaturePredictionCorrection[correctionDefinitions.size()]);

    // Call the plugins.
    List<FeaturePrediction<?>> featurePredictions = new ArrayList<>();

    for (FeatureMeasurementType featureMeasurementType : types) {
      for (Location receiverLocation : receiverLocations) {
        for (SignalFeaturePredictorPlugin plugin : plugins) {
          try {
            featurePredictions.add(
                plugin.initialize(new PluginConfiguration())
                    .predict(
                        model,
                        featureMeasurementType,
                        sourceLocation,
                        receiverLocation,
                        phase,
                        PredictionType
                            .valueOf(featureMeasurementType.getFeatureMeasurementTypeName())
                            .correctionsValid(correctionDefsArray) ? correctionDefsArray
                            : new FeaturePredictionCorrection[]{}
                    ));
          } catch (Exception e) {
            logger.error("Plugin predict(..) method threw an exception: " + e.toString());
            throw e;
          }
        }
      }
    }

    return featurePredictions;
  }

  /**
   *
   * TODO: NOTE: The below description assumes only a single plugin. We probably only want to use
   * TODO: a single plugin, which is not how this method is currently implemented.
   *
   * Return a location solution containing a set of feature predictions, P. P contains every possible
   * combination of the prediction types and receivers. For this overload, the receivers are Channels.
   *
   * Let T = {t_1, t_2, t_3, ... t_n} be the set of feature prediction types,
   * C = { c_1, c_2, c_3, ... c_m } be the set of channels. Then there will be a prediction for each
   * element in T x C; the size of the feature prediction list P will be n * m.
   *
   * @param types List of feature measurement types, T
   * @param sourceLocation event location
   * @param receivers List of receiver channels, C
   * @param phase phase to make predictions for
   * @param model earth model to use for predictions
   * @param correctionDefinitions set of corrections to apply to the predictions.
   * @return List of feature predictions P = {p_00, p_01, ... p_nm} where p_ij is the feature prediction
   * for (t_i, c_j)
   *
   * @throws Exception TODO: dont throw Excepption
   */
  public LocationSolution predict(
      List<FeatureMeasurementType<?>> types,
      LocationSolution sourceLocation,
      List<Channel> receivers,
      PhaseType phase,
      String model,
      List<FeaturePredictionCorrection> correctionDefinitions) throws Exception {

    final FeaturePredictionCorrection[] correctionDefsArray =
        correctionDefinitions != null ? correctionDefinitions.toArray(
            new FeaturePredictionCorrection[correctionDefinitions.size()]) :
            new FeaturePredictionCorrection[0];

    // Call the plugins.
    Set<FeaturePrediction<?>> featurePredictions = new HashSet<>();

    for (FeatureMeasurementType featureMeasurementType : types) {
      for (Channel channel : receivers) {

        // Extract a Location from the Channel object.
        Location receiverLocation = Location.from(
            channel.getLatitude(),
            channel.getLongitude(),
            channel.getDepth(), channel.getElevation()
        );

        for (SignalFeaturePredictorPlugin plugin : this.plugins) {
          try {

            // Retrieve the plugin result.
            FeaturePrediction<?> pluginResult =
                plugin.initialize(new PluginConfiguration())
                    .predict(
                        model,
                        featureMeasurementType,
                        sourceLocation.getLocation(),
                        receiverLocation,
                        phase,
                        PredictionType
                            .valueOf(featureMeasurementType.getFeatureMeasurementTypeName())
                            .correctionsValid(correctionDefsArray) ? correctionDefsArray
                            : new FeaturePredictionCorrection[]{}
                    );

            // Add the proper Channel ID.
            FeaturePrediction<?> featurePrediction;

            if (pluginResult.getPredictionType() instanceof NumericMeasurementType) {

              featurePrediction = FeaturePrediction.create(
                  pluginResult.getPhase(),
                  pluginResult.getPredictedValue().map(pv -> (NumericMeasurementValue) pv),
                  pluginResult.getFeaturePredictionComponents(),
                  pluginResult.isExtrapolated(),
                  (NumericMeasurementType) pluginResult.getPredictionType(),
                  pluginResult.getSourceLocation(),
                  pluginResult.getReceiverLocation(),
                  Optional.of(channel.getId()),
                  pluginResult.getFeaturePredictionDerivativeMap());
            } else if (pluginResult.getPredictionType() instanceof InstantMeasurementType) {
              featurePrediction = FeaturePrediction.create(
                  pluginResult.getPhase(),
                  pluginResult.getPredictedValue().map(pv -> (InstantValue) pv),
                  pluginResult.getFeaturePredictionComponents(),
                  pluginResult.isExtrapolated(),
                  (InstantMeasurementType) pluginResult.getPredictionType(),
                  pluginResult.getSourceLocation(),
                  pluginResult.getReceiverLocation(),
                  Optional.of(channel.getId()),
                  pluginResult.getFeaturePredictionDerivativeMap());
            } else {
              throw new IllegalStateException("Type of returned feature prediction unsupported");
            }

            featurePredictions.add(featurePrediction);

          } catch (Exception e) {
            logger.error("Plugin predict(..) method threw an exception: " + e.toString());
            throw e;
          }
        }
      }
    }

    Set<FeaturePrediction<?>> updatedFeaturePredictions = new HashSet<>(
        sourceLocation.getFeaturePredictions());
    updatedFeaturePredictions.addAll(featurePredictions);

    return LocationSolution.from(
        sourceLocation.getId(),
        sourceLocation.getLocation(),
        sourceLocation.getLocationRestraint(),
        sourceLocation.getLocationUncertainty().orElse(null),
        sourceLocation.getLocationBehaviors(),
        updatedFeaturePredictions);
  }

  /**
   * Helper method for getFFunction to call the order version of the predict method in this
   * utility.
   *
   * For a description of how valid feature predictions are ordered, see the ordered version
   * of the predict method.
   *
   * @param signalFeaturePredictionUtility specific instantiation of this class. making this
   * method static and passing in an instantiation helps with testing.
   * @param location location of event
   * @param newReceiverLocations ordered lsit of receiver locations
   * @param phase phase to make predictions for
   * @param model model to use for predictions
   * @param featureMeasurementTypes ordered list of feature measurement types
   * @param newCorrections list of corrections to apply to feature predictions
   * @return list of Optional feature predictions. For each invalid phase OR earth model, this
   * list will contain N Optional.empty objects, where N is the number of requested prediction
   * tyoes.
   * @throws Exception TODO: dont throw exception
   */
  static List<Optional<FeaturePrediction<?>>> callPredictor(
      SignalFeaturePredictionUtility signalFeaturePredictionUtility,
      EventLocation location,
      List<Location> newReceiverLocations,
      PhaseType phase,
      String model,
      List<FeatureMeasurementType<?>> featureMeasurementTypes,
      List<FeaturePredictionCorrection> newCorrections
  ) throws Exception {

    final List<Optional<FeaturePrediction<?>>> optionalFeaturePredictions = new ArrayList<>();

    try {

      signalFeaturePredictionUtility.predict(
          new ArrayList<>(featureMeasurementTypes),
          location,
          new ArrayList<>(newReceiverLocations),
          phase,
          model,
          newCorrections
      ).forEach(
          featurePrediction -> optionalFeaturePredictions.add(Optional.of(featurePrediction)));

    } catch (MissingEarthModelOrPhaseException e) {

      // Unable to make predictions for the requested Phase and/or Earth Model.  The locator
      // needs these invalid predictions to show up as NaNs in order to proceed with location
      // without failing.  Empty optionals of FeaturePredictions are later converted to NaNs by
      // the locator, so we will return a list of empty optionals.

      List<Optional<FeaturePrediction<?>>> emptyPredictions = new ArrayList<>();

      for (int i = 0; i < newReceiverLocations.size(); i++) {
        emptyPredictions.add(Optional.empty());
      }

      optionalFeaturePredictions.addAll(emptyPredictions);
    }

    return optionalFeaturePredictions;
  }

  /**
   * Create a single row of a Jacobian matrix given the set of derivatives returned by the predictor
   * for a single feature prediction. entries in the row are filtered out based on the location
   * restraint - if a parameter is restrained, the corresponding element is filtered out; the ultimate
   * result is that the row in the Jacobian matrix is filtered out, leaving a Jacobian with M-L rows,
   * where M is the number of derivatives and L is the number or restrained values.
   *
   * @param featureDerivativeMap Map of partial derivative type to value of the partial derivative
   * @param locationRestraint location restraint object representing restrained values
   * @return A single row in a Jacobian matrix, represented by a double array.
   */
  private static double[] createJacobianRowWithConstraints(
      Map<FeaturePredictionDerivativeType, DoubleValue> featureDerivativeMap,
      LocationRestraint locationRestraint) {

    return Stream.of(
        !locationRestraint.getLatitudeRestraintDegrees().isPresent()
            ? Optional.of(featureDerivativeMap.get(FeaturePredictionDerivativeType.D_DX).getValue())
            : Optional.empty(),
        !locationRestraint.getLongitudeRestraintDegrees().isPresent()
            ? Optional.of(featureDerivativeMap.get(FeaturePredictionDerivativeType.D_DY).getValue())
            : Optional.empty(),
        !locationRestraint.getDepthRestraintKm().isPresent()
            ? Optional.of(featureDerivativeMap.get(FeaturePredictionDerivativeType.D_DZ).getValue())
            : Optional.empty(),
        !locationRestraint.getTimeRestraint().isPresent()
            ? Optional.of(featureDerivativeMap.get(FeaturePredictionDerivativeType.D_DT).getValue())
            : Optional.empty()
    ).filter(Optional::isPresent).mapToDouble(optional -> (Double) optional.get()).toArray();
  }

  /**
   * Count the number of restrained values givent a location restraint object.
   *
   * @param locationRestraint LocationRestraint object that represents constraints
   * @return number of values that are restrained in some way.
   */
  private static int countRestraints(LocationRestraint locationRestraint) {
    int constraints = 0;

    if (!locationRestraint.getLongitudeRestraintType().equals(RestraintType.UNRESTRAINED)) {
      ++constraints;
    }
    if (!locationRestraint.getLatitudeRestraintType().equals(RestraintType.UNRESTRAINED)) {
      ++constraints;
    }
    if (!locationRestraint.getTimeRestraintType().equals(RestraintType.UNRESTRAINED)) {
      ++constraints;
    }
    if (!locationRestraint.getDepthRestraintType().equals(DepthRestraintType.UNRESTRAINED)) {
      ++constraints;
    }

    return constraints;
  }

  /**
   * Helper function that simply returns NaN if the given array contains at least one NaN,
   * or the given original value if it contains no nans.
   *
   * @param row array to test for nans (called 'row' because it is assumed to be a row in a matrix
   * @param orignalValue value to return if there are no NaNs
   * @return originalValue if no NaNs, NaN otherwise.
   */
  private static double nanIfRowContainsNan(double[] row, double orignalValue) {
    return DoubleStream.of(row).anyMatch(Double::isNaN) ? Double.NaN : orignalValue;
  }

  /**
   * Return the function F(m) from the SAND report. In this case, the implemenataion of F(m) calls
   * the signal feature prediction utility.
   *
   * @param model Earth model name to use.
   * @param phaseLocationMap Map of phases to location-measurment pairs
   * @param orderedDistinctPhases Ordered list of phases, to keep track of ordering of measurements
   * @param newCorrections corrections to apply
   * @param featurePredictionListConsumer Consumer of the final list of optional feature
   * predictions
   * @param errorValueNaNFilter Closure for transforming or filtering NaN values of the error/value
   * matrix
   * @param jacobianNaNFilter Closure for transforming or filtering NaN values of the Jacobian
   * matrix
   * @param <T> Output type of the two closures above. Must be a RealMatrix.
   * @return A closure which given an input (m), returns a pair of matrices where the first matrix
   * has a column for values and a column for standard deviations (errors), and the second matrix
   * contains derivatives calculated by the predictor. It is the Jacobian matrix of the F function.
   */
  public <T extends RealMatrix> Function<RealVector, Pair<T, T>> getFFunction(
      final String model,
      final Map<PhaseType, List<Pair<Location, FeatureMeasurementType<?>>>> phaseLocationMap,
      final List<PhaseType> orderedDistinctPhases,
      final List<FeaturePredictionCorrection> newCorrections,
      Consumer<List<FeaturePrediction<?>>> featurePredictionListConsumer,
      Function<RealMatrix, T> errorValueNaNFilter,
      Function<RealMatrix, T> jacobianNaNFilter,
      LocationRestraint locationRestraint) {

    return (m -> {
      EventLocation location = EventLocation.from(m.getEntry(0),
          m.getEntry(1), m.getEntry(2),
          Instant.ofEpochSecond((long) (m.getEntry(3)),
              (long) (1_000_000_000 * (m.getEntry(3) - ((long) m.getEntry(3))))));

      //
      // Feature prdictor may not be able to come up with predictions for all combinations
      // of prediction type/station; since we want to keep things consistently ordered,
      // need to use a list of optionals so that empty optionals can be placeholders for
      // missing predictions.
      //
      final List<Optional<FeaturePrediction<?>>> finalPredictions = new LinkedList<>();

      //
      // Make a set of predictions for each distinct phase.
      //
      orderedDistinctPhases.forEach(phaseType -> {

        List<Optional<FeaturePrediction<?>>> subset;
        try {
          subset = callPredictor(
              this,
              location,
              phaseLocationMap.get(phaseType).stream().map(Pair::getKey)
                  .collect(Collectors.toList()),
              phaseType,
              model,
              phaseLocationMap.get(phaseType).stream()
                  .map(Pair::getValue)
                  .collect(Collectors.toList()),
              newCorrections
          );
        } catch (Exception e) {
          // TODO: what to do with this
          throw new RuntimeException(e);
        }

        finalPredictions.addAll(subset);
      });

      //
      // Value vector and error vector
      //
      double[] pvArray = new double[finalPredictions.size()];
      double[] peArray = new double[finalPredictions.size()];

      //
      // Jacobian matrix. Number of columns = 4 - X, where X is number of restrained values.
      //
      double[][] jacobianArray =
          new double[finalPredictions.size()][4 - countRestraints(locationRestraint)];

      //
      // Fill the value and error vectors and the Jacobian matrix
      //
      for (int i = 0; i < finalPredictions.size(); ++i) {
        Optional<FeaturePrediction<?>> optionalFeaturePrediction = finalPredictions.get(i);

        if (optionalFeaturePrediction.isPresent()) {
          FeaturePrediction<?> featurePrediction = optionalFeaturePrediction.get();

          double value = 0;
          double standardDeviation = 0;

          if (featurePrediction.getPredictionType() instanceof NumericMeasurementType) {

            if (featurePrediction.getPredictedValue().isPresent()) {

              NumericMeasurementValue doubleValue = (NumericMeasurementValue) featurePrediction
                  .getPredictedValue().orElseThrow(AssertionError::new);

              value = doubleValue.getMeasurementValue().getValue();
              standardDeviation = doubleValue.getMeasurementValue().getStandardDeviation();
            } else {

              value = Double.NaN;
              standardDeviation = Double.NaN;
            }


          } else if (featurePrediction.getPredictionType() instanceof InstantMeasurementType) {

            if (featurePrediction.getPredictedValue().isPresent()) {

              InstantValue instantValue = (InstantValue) featurePrediction.getPredictedValue()
                  .orElseThrow(AssertionError::new);
              Instant instant = instantValue.getValue();
              Duration duration = instantValue.getStandardDeviation();

              value = instant.getEpochSecond() + (double) instant.getNano() / 1_000_000_000;
              standardDeviation =
                  duration.getSeconds() + (double) duration.getNano() / 1_000_000_000;
            } else {

              value = Double.NaN;
              standardDeviation = Double.NaN;
            }
          }

          Map<FeaturePredictionDerivativeType, DoubleValue> featurePredictionDerivativeMap =
              featurePrediction.getFeaturePredictionDerivativeMap();
          jacobianArray[i] = createJacobianRowWithConstraints(featurePredictionDerivativeMap,
              locationRestraint);

          //
          // Assuming that if the row in the Jacobian corresponding to the feature predictions
          // has a NaN, then the whole prediction is invalid. Need to do this to ensure dimensions
          // match up.
          //
          pvArray[i] = nanIfRowContainsNan(jacobianArray[i], value);
          peArray[i] = nanIfRowContainsNan(jacobianArray[i], standardDeviation);
        } else {
          pvArray[i] = Double.NaN;
          peArray[i] = Double.NaN;
          Arrays.fill(jacobianArray[i], Double.NaN);
        }
      }

      //
      // Let caller know what feature predictions were produced via the passed in consumer.
      //
      featurePredictionListConsumer.accept(finalPredictions.stream()
          .filter(Optional::isPresent)
          .map(Optional::get).collect(
          Collectors.toList()));

      //
      // Construct vectors and matrix.
      //
      RealMatrix valueErrorMatrix = new Array2DRowRealMatrix(
          finalPredictions.size(), 2);
      valueErrorMatrix.setColumnVector(0, new ArrayRealVector(pvArray));
      valueErrorMatrix.setColumnVector(1, new ArrayRealVector(peArray));

      return Pair.create(
          //
          // Process NaN values via the closures passed in to the method.
          //
          errorValueNaNFilter.apply(valueErrorMatrix),
          jacobianNaNFilter.apply(new Array2DRowRealMatrix(jacobianArray)));
    });
  }
}
