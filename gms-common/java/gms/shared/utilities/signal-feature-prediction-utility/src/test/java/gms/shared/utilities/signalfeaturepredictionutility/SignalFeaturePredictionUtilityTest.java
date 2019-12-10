package gms.shared.utilities.signalfeaturepredictionutility;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.google.common.collect.Streams;
import gms.core.featureprediction.exceptions.MissingEarthModelOrPhaseException;
import gms.core.featureprediction.plugins.SignalFeaturePredictorPlugin;
import gms.core.featureprediction.plugins.implementations.signalfeaturepredictor.PredictionType;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.DoubleValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.PhaseType;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.Units;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.DepthRestraintType;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.EllipticityCorrection1dDefinition;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.EventLocation;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.FeaturePrediction;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.FeaturePredictionCorrection;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.FeaturePredictionDerivativeType;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.LocationRestraint;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.LocationSolution;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.RestraintType;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Channel;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FeatureMeasurementType;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FeatureMeasurementTypes;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.InstantValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Location;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ChannelDataType;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ChannelType;
import gms.shared.mechanisms.pluginregistry.PluginInfo;
import gms.shared.mechanisms.pluginregistry.PluginRegistry;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.util.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.mockito.Mockito;


public class SignalFeaturePredictionUtilityTest {

  @Test
  public void testCorrectNumberOfReturnValues() throws Exception {
    SignalFeaturePredictionUtility signalFeaturePredictionUtility = new SignalFeaturePredictionUtility();

    List<FeaturePrediction<?>> featurePredictions =
        signalFeaturePredictionUtility.predict(
            TestFixtures.types, TestFixtures.sourceLocation, TestFixtures.receiverLocationsSet,
            TestFixtures.phase, TestFixtures.model, TestFixtures.correctionDefinitions);

    // Check that the correct number of feature predictions were returned.
    int totalReceiverLocations = TestFixtures.receiverLocationsSet.size();
    int totalFeatureMeasurementTypes = TestFixtures.types.size();
    int totalPlugins = 1;
    assertEquals(
        totalReceiverLocations * totalFeatureMeasurementTypes * totalPlugins,
        featurePredictions.size());
  }

  @Test
  public void testGetFFunctionSetsMissingModelOrPhaseToNaN() throws Exception {

    // Define input for predict()
    String earthModel = "ak135";
    FeatureMeasurementType measurementType = FeatureMeasurementTypes.ARRIVAL_TIME;
    EventLocation eventLocation = EventLocation.from(0.0, 0.0, 0.0, Instant.EPOCH);
    Location stationLocation = Location.from(90.0, 0.0, 0.0, 0.0);
    PhaseType phaseType = PhaseType.P;
    FeaturePredictionCorrection[] corrections = new FeaturePredictionCorrection[]{};

    // Mock plugin so that we can force MissingEarthModelOrPhaseException to be thrown
    SignalFeaturePredictorPlugin mockPlugin = Mockito.mock(SignalFeaturePredictorPlugin.class);

    // Mock that predict() throws MissingEarthModelOrPhaseException when called with our defined input
    when(mockPlugin.predict(any(), any(), any(), any(), any(),
        any())).thenThrow(MissingEarthModelOrPhaseException.class);

    // Mock that initialize returns mock plugin
    when(mockPlugin.initialize(any())).thenReturn(mockPlugin);

    PluginRegistry mockPluginRegistry = Mockito.mock(PluginRegistry.class);
    PluginInfo fakePluginInfo = PluginInfo.from("FAKE", "FAKE");

    // Mock that mockPluginRegistry returns the mock plugin
    when(mockPluginRegistry.lookup(fakePluginInfo, SignalFeaturePredictorPlugin.class))
        .thenReturn(Optional.of(mockPlugin));

    // Create SignalFeaturePredictionUtility with mock plugin registry and fake plugin info
    SignalFeaturePredictionUtility predictionUtility = new SignalFeaturePredictionUtility(
        mockPluginRegistry, List.of(fakePluginInfo));

    // Define input for getFFunction()

    FeatureMeasurementType<?> measurementTypeFFunction = FeatureMeasurementTypes.ARRIVAL_TIME;

    Map<PhaseType, List<Pair<Location, FeatureMeasurementType<?>>>> phaseLocationMap = Map
        .ofEntries(
            Map.entry(phaseType, List.of(Pair.create(stationLocation, measurementTypeFFunction)))
        );

    List<PhaseType> orderedDistinctPhases = List.of(phaseType);
    List<FeaturePredictionCorrection> emptyCorrections = List.of();

    // Define NaN filters that do nothing, leaving NaNs in place
    Function<RealMatrix, RealMatrix> errorValueNanFilter = valueErrorMatrix -> valueErrorMatrix;
    Function<RealMatrix, RealMatrix> jacobianNanFilter = jacobianMatrix -> jacobianMatrix;

    LocationRestraint restraint = LocationRestraint.from(
        RestraintType.UNRESTRAINED,
        null,
        RestraintType.UNRESTRAINED,
        null,
        DepthRestraintType.UNRESTRAINED,
        null,
        RestraintType.UNRESTRAINED,
        Instant.EPOCH
    );

    // Get fFunction for defined input
    Function<RealVector, Pair<RealMatrix, RealMatrix>> fFunction = predictionUtility.getFFunction(
        earthModel,
        phaseLocationMap,
        orderedDistinctPhases,
        emptyCorrections,
        featurePredictions -> {
        },
        errorValueNanFilter,
        jacobianNanFilter,
        restraint
    );

    // Create bogus vector to pass fFunction
    RealVector point = new ArrayRealVector(new double[]{1.2, 3.4, 5.6, 7.8});

    // apply fFunction and verify we get correct output
    Pair<RealMatrix, RealMatrix> valuesWithErrors = fFunction.apply(point);

    RealMatrix left = valuesWithErrors.getFirst();
    RealMatrix right = valuesWithErrors.getSecond();

    // validate left matrix
    for (int i = 0; i < left.getRowDimension(); i++) {
      double[] row = left.getRow(i);

      for (int j = 0; j < row.length; j++) {

        Assertions.assertTrue(Double.isNaN(row[j]));
      }
    }

    // validate right matrix
    for (int i = 0; i < right.getRowDimension(); i++) {
      double[] row = right.getRow(i);

      for (int j = 0; j < row.length; j++) {

        Assertions.assertTrue(Double.isNaN(row[j]));
      }
    }
  }

  @Test
  public void testProperCorrectionsPassedToPluginReceiverLocations() throws Exception {
    PluginRegistry mockPluginRegistry = Mockito.mock(PluginRegistry.class);
    PluginInfo pluginInfo = PluginInfo.from("FAKE", "FAKE");

    SignalFeaturePredictorPlugin mockPlugin = Mockito.mock(SignalFeaturePredictorPlugin.class);

    when(mockPluginRegistry.lookup(any(), any())).thenReturn(Optional.of(mockPlugin));

    when(mockPlugin.initialize(any())).thenReturn(mockPlugin);

    Map<FeatureMeasurementType, FeaturePredictionCorrection[]> typesChecked = new HashMap<>();

    when(mockPlugin.predict(any(), any(), any(), any(), any(), any())).thenAnswer(invocation -> {
      FeatureMeasurementType type = invocation.getArgument(1);

      FeaturePredictionCorrection[] corrections = invocation.getArgument(5);

      typesChecked.put(type, corrections);

      Assertions.assertTrue(PredictionType.valueOf(type.getFeatureMeasurementTypeName())
          .correctionsValid(corrections));

      return null;
    });

    SignalFeaturePredictionUtility signalFeaturePredictionUtility = new SignalFeaturePredictionUtility(
        mockPluginRegistry, List.of(pluginInfo));

    signalFeaturePredictionUtility.predict(
        TestFixtures.typesAll, TestFixtures.sourceLocation, TestFixtures.receiverLocationsSet,
        TestFixtures.phase, TestFixtures.model, TestFixtures.correctionDefinitionsAll);

    Assertions.assertTrue(typesChecked.keySet().containsAll(TestFixtures.typesAll));

    typesChecked.entrySet().forEach(entry -> {
      Assertions.assertTrue(TestFixtures.typesAll.contains(entry.getKey()));

      Assertions.assertTrue(PredictionType.valueOf(entry.getKey().getFeatureMeasurementTypeName())
          .correctionsValid(entry.getValue()));
    });
  }

  @Test
  public void testProperCorrectionsPassedToPluginReceiverChannels() throws Exception {
    PluginRegistry mockPluginRegistry = Mockito.mock(PluginRegistry.class);
    PluginInfo pluginInfo = PluginInfo.from("FAKE", "FAKE");

    SignalFeaturePredictorPlugin mockPlugin = Mockito.mock(SignalFeaturePredictorPlugin.class);

    when(mockPluginRegistry.lookup(any(), any())).thenReturn(Optional.of(mockPlugin));

    when(mockPlugin.initialize(any())).thenReturn(mockPlugin);

    Map<FeatureMeasurementType, FeaturePredictionCorrection[]> typesChecked = new HashMap<>();

    when(mockPlugin.predict(any(), any(), any(), any(), any(), any())).thenAnswer(invocation -> {
      FeatureMeasurementType type = invocation.getArgument(1);

      FeaturePredictionCorrection[] corrections = invocation.getArgument(5);

      typesChecked.put(type, corrections);

      Assertions.assertTrue(PredictionType.valueOf(type.getFeatureMeasurementTypeName())
          .correctionsValid(corrections));

      return FeaturePrediction.from(
          UUID.randomUUID(),
          PhaseType.P,
          Optional.of(InstantValue.from(Instant.EPOCH, Duration.ZERO)),
          Set.of(),
          false,
          FeatureMeasurementTypes.ARRIVAL_TIME,
          EventLocation.from(40, 40, 40, Instant.EPOCH),
          Location.from(50, 50, 50, 50),
          Optional.empty(),
          Map.of()
      );
    });

    SignalFeaturePredictionUtility signalFeaturePredictionUtility = new SignalFeaturePredictionUtility(
        mockPluginRegistry, List.of(pluginInfo));

    signalFeaturePredictionUtility.predict(
        TestFixtures.typesAll, TestFixtures.sourceLocationSolution, TestFixtures.receiverChannelList,
        TestFixtures.phase, TestFixtures.model, TestFixtures.correctionDefinitionsAll);

    Assertions.assertTrue(typesChecked.keySet().containsAll(TestFixtures.typesAll));

    typesChecked.entrySet().forEach(entry -> {
      Assertions.assertTrue(TestFixtures.typesAll.contains(entry.getKey()));

      Assertions.assertTrue(PredictionType.valueOf(entry.getKey().getFeatureMeasurementTypeName())
          .correctionsValid(entry.getValue()));
    });
  }

  @Test
  public void testProperCorrectionsBasedOnPhasePassedToPlugin() throws Exception {
    PluginRegistry mockPluginRegistry = Mockito.mock(PluginRegistry.class);
    PluginInfo pluginInfo = PluginInfo.from("FAKE", "FAKE");

    SignalFeaturePredictorPlugin mockPlugin = Mockito.mock(SignalFeaturePredictorPlugin.class);

    when(mockPluginRegistry.lookup(any(), any())).thenReturn(Optional.of(mockPlugin));

    when(mockPlugin.initialize(any())).thenReturn(mockPlugin);

    Map<FeatureMeasurementType, FeaturePredictionCorrection[]> typesChecked = new HashMap<>();

    when(mockPlugin.predict(any(), any(), any(), any(), any(), any())).thenAnswer(invocation -> {
      PhaseType type = invocation.getArgument(4);

      FeaturePredictionCorrection[] corrections = invocation.getArgument(5);

      Assertions.assertEquals(PhaseType.Lg, type);

      Assertions.assertEquals(1, corrections.length);

      Assertions.assertTrue(corrections[0] instanceof EllipticityCorrection1dDefinition);

      return null;
    });

    SignalFeaturePredictionUtility signalFeaturePredictionUtility = new SignalFeaturePredictionUtility(
        mockPluginRegistry, List.of(pluginInfo));

    signalFeaturePredictionUtility.predict(
        TestFixtures.types, TestFixtures.sourceLocation, TestFixtures.receiverLocationsSet,
        PhaseType.Lg, TestFixtures.model, TestFixtures.correctionDefinitionsAll);
  }

  @Test
  public void testFFunctionRemovesJacobianColumns() {
    SignalFeaturePredictionUtility signalFeaturePredictionUtility = new SignalFeaturePredictionUtility();

    RealVector mSeed;

    final double ORIGIN_LAT = 10.0;  // South China Sea (between Vietnam and Brunei)
    final double ORIGIN_LON = 110.0;
    final double ORIGIN_DEPTH = 70.0;
    final double EVENT_TIME = 1546300800.0;  // 01-JAN-2019 00:00:00 GMT

    mSeed = new ArrayRealVector(
//        new double[]{ORIGIN_LAT, ORIGIN_LON, ORIGIN_DEPTH, EVENT_TIME});
        new double[]{ORIGIN_LAT + 5.0, ORIGIN_LON + 5.0, ORIGIN_DEPTH, EVENT_TIME});
//    new double[]{ORIGIN_LAT + 10.0, ORIGIN_LON + 10.0, ORIGIN_DEPTH, EVENT_TIME});


    /*
     * observations matrix
     */

    //
    // pcalc data
    //
    //  0: site_lat
    //  1: site_lon
    //  2: site_elev
    //  3: origin_lat
    //  4: origin_lon
    //  5: origin_depth
    //  6: distance_degrees
    //  7: travel_time
    //  8: tt_model_uncertainty
    //  9: tt_elevation_correction
    // 10: tt_ellipticity_correction
    // 11: slowness_degrees
    // 12: slowness_model_uncertainty_degrees
    // 13: azimuth_degrees
    // 14: azimuth_model_uncertainty_degrees
    final double[] AS10 = new double[]{-23.695526, 133.915193, 0.624, 10.0, 110.0, 70.0,
        41.011643, 456.567567, 1.160000, 0.097215, 0.295708, 8.212651, 2.500000, 322.527671,
        10.000000};
    final double[] AS31 = new double[]{-23.665134, 133.905261, 0.6273, 10.0, 110.0, 70.0,
        40.981989, 456.324740, 1.160000, 0.097722, 0.295949, 8.215254, 2.500000, 322.518690,
        10.000000};
    final double[] WB10 = new double[]{-19.7671, 134.3928, 0.3621, 10.0, 110.0, 70.0,
        38.249498, 433.616740, 1.160000, 0.056121, 0.329138, 8.399207, 2.500000, 318.931528,
        10.000000};
    final double[] WR7 = new double[]{-19.9552, 134.476, 0.3548, 10.0, 110.0, 70.0,
        38.442749,
        435.236839, 1.160000, 0.055008, 0.328383, 8.387003, 2.500000, 318.984783, 10.000000};
    final double[] AS06 = new double[]{-23.646206, 133.972511, 0.6813, 10.0, 110.0, 70.0,
        41.004503, 456.518592, 1.160000, 0.106140, 0.296444, 8.213150, 2.500000, 322.422230,
        10.000000};
    final double[] LBTBB = new double[]{-25.015124, 25.596598, 1.1483, 10.0, 110.0, 70.0,
        89.220487, 768.885873, 1.199971, 0.191977, 0.596213, 4.686213, 2.500000, 78.580567,
        10.000000};
    final double[] KUR08 = new double[]{50.56317, 78.5108, 0.1986, 10.0, 110.0, 70.0,
        48.120499, 512.785516, 1.160000, 0.031352, -0.027926, 7.708201, 2.500000, 136.299409,
        10.000000};
    final double[] MK07 = new double[]{46.753431, 82.315664, 0.6398, 10.0, 110.0, 70.0,
        43.616330, 477.456071, 1.160000, 0.100164, 0.023972, 8.031469, 2.500000, 138.450168,
        10.000000};

    double[][] matrix = {
        {EVENT_TIME + AS10[7], AS10[8]},
        {EVENT_TIME + KUR08[7], KUR08[8]},
        {EVENT_TIME + WB10[7], WB10[8]},
        {EVENT_TIME + WR7[7], WR7[8]},
        {MK07[13], MK07[14]},
        {MK07[11], MK07[12]},
        {LBTBB[13], LBTBB[14]},
        {LBTBB[11], LBTBB[12]}
    };

    /*
     * prediction function
     */

    List<FeatureMeasurementType<?>> featureMeasurementTypes = new ArrayList<>();
    featureMeasurementTypes.add(FeatureMeasurementTypes.ARRIVAL_TIME);
    featureMeasurementTypes.add(FeatureMeasurementTypes.ARRIVAL_TIME);
    featureMeasurementTypes.add(FeatureMeasurementTypes.ARRIVAL_TIME);
    featureMeasurementTypes.add(FeatureMeasurementTypes.ARRIVAL_TIME);
    featureMeasurementTypes.add(FeatureMeasurementTypes.SOURCE_TO_RECEIVER_AZIMUTH);
    featureMeasurementTypes.add(FeatureMeasurementTypes.SLOWNESS);
    featureMeasurementTypes.add(FeatureMeasurementTypes.SOURCE_TO_RECEIVER_AZIMUTH);
    featureMeasurementTypes.add(FeatureMeasurementTypes.SLOWNESS);

    List<Location> receiverLocations = new ArrayList<>() {{
      add(Location.from(AS10[0], AS10[1], 0.0, AS10[2]));
      add(Location.from(KUR08[0], KUR08[1], 0.0, KUR08[2]));
      add(Location.from(WB10[0], WB10[1], 0.0, WB10[2]));
      add(Location.from(WR7[0], WR7[1], 0.0, WR7[2]));
      add(Location.from(MK07[0], MK07[1], 0.0, MK07[2]));
      add(Location.from(MK07[0], MK07[1], 0.0, MK07[2]));
      add(Location.from(LBTBB[0], LBTBB[1], 0.0, LBTBB[2]));
      add(Location.from(LBTBB[0], LBTBB[1], 0.0, LBTBB[2]));
    }};

    Map<PhaseType, List<Pair<Location, FeatureMeasurementType<?>>>> phaseLocationMap = new HashMap<>();
    List<Pair<Location, FeatureMeasurementType<?>>> pairs = new LinkedList<>();

    for (int i = 0; i < featureMeasurementTypes.size(); i++) {
      pairs.add(Pair.create(receiverLocations.get(i), featureMeasurementTypes.get(i)));
    }

    phaseLocationMap.put(PhaseType.P, pairs);

    Function<RealVector, Pair<RealMatrix, RealMatrix>> referenceFunction = signalFeaturePredictionUtility
        .getFFunction(
            "ak135",
            phaseLocationMap,
            List.of(PhaseType.P),
            List.of(),
            featurePredictions -> {
            },
            TestFixtures.replaceWithZeroFilter,
            TestFixtures.replaceWithZeroFilter,
            new LocationRestraint.Builder().build()
        );

    Pair<RealMatrix, RealMatrix> referenceResult = referenceFunction.apply(mSeed);
    assertEquals(referenceResult.getSecond().getColumnDimension(), 4);

    testLocationRestraints(mSeed, referenceResult.getSecond(), phaseLocationMap,
        signalFeaturePredictionUtility,
        List.of(
            new LocationRestraint.Builder().setDepthRestraintAtSurface().build(),
            new LocationRestraint.Builder().setPositionRestraint(4, 4).build(),
            new LocationRestraint.Builder().setTimeRestraint(Instant.MIN).build(),
            new LocationRestraint.Builder().setLatitudeRestraint(4).build(),
            new LocationRestraint.Builder().setLongitudeRestraint(4).build()
        ),
        List.of(
            Pair.create(new int[]{0, 1, 3}, new int[]{0, 1, 2}),
            Pair.create(new int[]{2, 3}, new int[]{0, 1}),
            Pair.create(new int[]{0, 1, 2}, new int[]{0, 1, 2}),
            Pair.create(new int[]{1, 2, 3}, new int[]{0, 1, 2}),
            Pair.create(new int[]{0, 2, 3}, new int[]{0, 1, 2})
        )
    );
  }

  @Test
  //No reason to use TreeSet with a comparator that compares hashes, when HashSet is available.
  //Test that swithching to HashSet within the predict code fixes this problem.
  public void testChannelLocateDoesNotThrowComparableCastException() throws Exception {

    PluginRegistry mockPluginRegistry = Mockito.mock(PluginRegistry.class);
    PluginInfo pluginInfo = PluginInfo.from("FAKE", "FAKE");

    SignalFeaturePredictorPlugin mockPlugin = Mockito.mock(SignalFeaturePredictorPlugin.class);

    when(mockPluginRegistry.lookup(any(), any())).thenReturn(Optional.of(mockPlugin));

    when(mockPlugin.initialize(any())).thenReturn(mockPlugin);

    Map<FeatureMeasurementType, FeaturePredictionCorrection[]> typesChecked = new HashMap<>();

    when(mockPlugin.predict(any(), any(), any(), any(), any(), any())).thenAnswer(invocation -> {
      PhaseType type = invocation.getArgument(4);

      FeaturePredictionCorrection[] corrections = invocation.getArgument(5);

      return FeaturePrediction.create(
          PhaseType.P,
          Optional.of(InstantValue.from(Instant.EPOCH, Duration.ZERO)),
          Set.of(),
          false,
          FeatureMeasurementTypes.ARRIVAL_TIME,
          EventLocation.from(1.0, 1.0, 1.0, Instant.EPOCH),
          Location.from(10, 10, 10, 10),
          Optional.empty(),
          Map.of()
      );
    });

    SignalFeaturePredictionUtility signalFeaturePredictionUtility = new SignalFeaturePredictionUtility(
        mockPluginRegistry, List.of(pluginInfo));

    List<LocationSolution> locationSolutions = new ArrayList<>();

    Assertions.assertDoesNotThrow(() ->
        locationSolutions.add(
            signalFeaturePredictionUtility.predict(
                List.of(FeatureMeasurementTypes.ARRIVAL_TIME),
                LocationSolution.withLocationAndRestraintOnly(
                    EventLocation.from(1.0, 2.0, 0.0, Instant.EPOCH),
                    new LocationRestraint.Builder().build()
                ),
                List.of(
                    Channel.create(
                        "x",
                        ChannelType.HIGH_BROADBAND_HIGH_GAIN_VERTICAL,
                        ChannelDataType.UNKNOWN,
                        36,
                        138,
                        0.8,
                        0.045,
                        0,
                        -1,
                        80
                    ),
                    Channel.create(
                        "x",
                        ChannelType.HIGH_BROADBAND_HIGH_GAIN_VERTICAL,
                        ChannelDataType.UNKNOWN,
                        36,
                        138,
                        0.8,
                        0.045,
                        0,
                        -1,
                        80
                    )),
                PhaseType.P,
                "ak135",
                List.of()
            )));

    LocationSolution locationSolution = locationSolutions.get(0);

    Assertions.assertEquals(2, locationSolution.getFeaturePredictions().size());

  }

  @Test
  public void testSinglePredictor() throws Exception {
    UUID featurePredictionUUID = UUID.randomUUID();

    FeaturePrediction<InstantValue> featurePredictionRef = FeaturePrediction.from(
        featurePredictionUUID,
        PhaseType.P,
        Optional.of(InstantValue.from(
            Instant.EPOCH,
            Duration.ZERO
        )),
        Set.of(),
        false,
        FeatureMeasurementTypes.ARRIVAL_TIME,
        EventLocation.from(
            90.0,
            90.0,
            0,
            Instant.EPOCH
        ),
        Location.from(
            30.0,
            30.0,
            0,
            0
        ),
        Optional.empty(),
        Map.of(
            FeaturePredictionDerivativeType.D_DX, DoubleValue.from(1.0, 0.1, Units.SECONDS),
            FeaturePredictionDerivativeType.D_DY, DoubleValue.from(2.0, 0.2, Units.UNITLESS),
            FeaturePredictionDerivativeType.D_DZ, DoubleValue.from(3.0, 0.3, Units.SECONDS),
            FeaturePredictionDerivativeType.D_DT, DoubleValue.from(4.0, 0.4, Units.SECONDS)
        )
    );

    PluginRegistry mockPluginRegistry = Mockito.mock(PluginRegistry.class);
    PluginInfo pluginInfo = PluginInfo.from("FAKE", "FAKE");

    SignalFeaturePredictorPlugin mockPlugin = Mockito.mock(SignalFeaturePredictorPlugin.class);

    when(mockPluginRegistry.lookup(any(), any())).thenReturn(Optional.of(mockPlugin));

    when(mockPlugin.initialize(any())).thenReturn(mockPlugin);

    //Make sure we are actually checking all arguments
    List<Boolean> sanityCheckArgumentTestCounter = new ArrayList<>();

    //Check that predictor plugin is being called with all expected arguments
    when(mockPlugin.predict(any(), any(), any(), any(), any(), any())).thenAnswer(invocation -> {
      Assertions
          .assertEquals(((FeaturePredictionCorrection[]) invocation.getArgument(5)).length, 0);

      sanityCheckArgumentTestCounter.add(true);

      return Streams.zip(List.of(
          "ak135",
          FeatureMeasurementTypes.ARRIVAL_TIME,
          EventLocation.from(
              90.0,
              90.0,
              0,
              Instant.EPOCH
          ),
          Location.from(
              30.0,
              30.0,
              0,
              0
          ),
          PhaseType.P).stream(),
          Arrays.stream(invocation.getArguments()),
          (expectedArgument, actualArgument) -> {
            Assertions.assertEquals(expectedArgument, actualArgument);
            sanityCheckArgumentTestCounter.add(true);
            return null;
          }).reduce((a, b) -> featurePredictionRef).get();

    });

    SignalFeaturePredictionUtility signalFeaturePredictionUtility = new SignalFeaturePredictionUtility(
        mockPluginRegistry, List.of(pluginInfo));

    FeaturePrediction<InstantValue> featurePredictionTest = signalFeaturePredictionUtility
        .predictSingle(
            FeatureMeasurementTypes.ARRIVAL_TIME,
            EventLocation.from(
                90.0,
                90.0,
                0,
                Instant.EPOCH
            ),
            Location.from(
                30.0,
                30.0,
                0,
                0
            ),
            PhaseType.P,
            "ak135",
            List.of()
        );

    Assertions.assertEquals(SignalFeaturePredictorPlugin.class
            .getDeclaredMethod("predict", String.class, FeatureMeasurementType.class,
                EventLocation.class, Location.class, PhaseType.class,
                FeaturePredictionCorrection[].class).getParameterCount(),
        sanityCheckArgumentTestCounter.size());

    Assertions.assertEquals(featurePredictionRef, featurePredictionTest);
  }

  private void testLocationRestraints(
      RealVector seed,
      RealMatrix reference,
      Map<PhaseType, List<Pair<Location, FeatureMeasurementType<?>>>> phaseLocationMap,
      SignalFeaturePredictionUtility signalFeaturePredictionUtility,
      List<LocationRestraint> locationRestraints, List<Pair<int[], int[]>> indices) {

    List<Boolean> dummy = Streams
        .zip(locationRestraints.stream(), indices.stream(), (locationRestraint, indexPair) -> {
          RealMatrix result = signalFeaturePredictionUtility.getFFunction(
              "ak135",
              phaseLocationMap,
              List.of(PhaseType.P),
              List.of(),
              featurePredictions -> {
              },
              TestFixtures.replaceWithZeroFilter,
              TestFixtures.replaceWithZeroFilter,
              locationRestraint
          ).apply(seed).getSecond();

          int[] referenceIndices = indexPair.getFirst();
          int[] testIndices = indexPair.getSecond();
          for (int i = 0; i < referenceIndices.length; i++) {
            assertEquals(reference.getColumnVector(referenceIndices[i]),
                result.getColumnVector(testIndices[i]));
          }
          return true;
        }).collect(Collectors.toList());

  }
}
