package gms.core.signaldetection.association.plugins.implementations.redundancy;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;

import gms.core.signaldetection.association.CandidateEvent;
import gms.core.signaldetection.association.eventredundancy.plugins.ArrivalQualityEventCriterionDefinition;
import gms.core.signaldetection.association.eventredundancy.plugins.EventRedundancyRemovalDefinition;
import gms.core.signaldetection.association.eventredundancy.plugins.WeightedEventCriteriaCalculation;
import gms.core.signaldetection.association.eventredundancy.plugins.WeightedEventCriteriaCalculationDefinition;
import gms.core.signaldetection.association.testdata.TestData;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.DoubleValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.Units;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetectionHypothesis;
import gms.shared.mechanisms.pluginregistry.PluginInfo;
import gms.shared.mechanisms.pluginregistry.PluginRegistry;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.internal.util.reflection.FieldSetter;

public class DefaultRedundancyRemovalDelegateTests {

  private static WeightedEventCriteriaCalculation mockCalculation = Mockito
      .mock(WeightedEventCriteriaCalculation.class);

  private static WeightedEventCriteriaCalculationDefinition weightDefinition = WeightedEventCriteriaCalculationDefinition
      .create(
          15,
          15,
          15,
          15,
          15,
          25,
          0
      );

  private static ArrivalQualityEventCriterionDefinition arrivalDefinition = ArrivalQualityEventCriterionDefinition
      .create(
          1.0,
          2.0,
          3.0,
          4.0
      );

  private static EventRedundancyRemovalDefinition definition = EventRedundancyRemovalDefinition
      .create(
          weightDefinition,
          arrivalDefinition
      );

  private static double sigmaTime = 2.0;

  @BeforeEach
  void initializeSingleTest() {
    Mockito.reset(mockCalculation);

    //
    // Fake out the weighted calculation - we are not testing it here. This calculation
    // gives a higher "quality" to CandidateEvents with more corroborating SDHs.
    //
    Mockito.when(mockCalculation.calculate(any(), any())).thenAnswer(invocation ->
        DoubleValue
            .from(((CandidateEvent) invocation.getArgument(0)).getCorroboratingSet().size(), 0,
                Units.UNITLESS)
    );
  }

  @Test
  void testInitializeCreatesCorrectPlugin() throws Exception {

    DefaultRedundancyRemovalDelegate delegate = testInitializeAndReturn();

    SignalDetectionHypothesis driverSdh = SignalDetectionHypothesis.builder(
        UUID.randomUUID(),
        UUID.randomUUID(),
        false,
        UUID.randomUUID())
        .addMeasurement(TestData.arrivalTimeFeatureMeasurement)
        .build();

    CandidateEvent eventLarger = CandidateEvent.from(
        TestData.stationId1,
        TestData.gridNode1,
        driverSdh,
        sigmaTime,
        IntStream.range(0, 5).mapToObj(i ->
            SignalDetectionHypothesis
                .builder(
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    false,
                    UUID.randomUUID())
                .addMeasurement(TestData.arrivalTimeFeatureMeasurement)
                .build())
            .collect(Collectors.toSet())
    );

    CandidateEvent eventSmaller = CandidateEvent.from(
        TestData.stationId2,
        TestData.gridNode2,
        driverSdh,
        sigmaTime,
        IntStream.range(0, 2).mapToObj(i ->
            SignalDetectionHypothesis
                .builder(
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    false,
                    UUID.randomUUID())
                .addMeasurement(TestData.arrivalTimeFeatureMeasurement)
                .build())
            .collect(Collectors.toSet())
    );

    delegate.reduce(List.of(eventLarger, eventSmaller), definition);

    Mockito.verify(mockCalculation, times(2)).calculate(any(), any());
  }

  @Test
  void testTwoEventsSameSdh() throws Exception {
    final int NUM_LARGER_SDHs = 5;
    final int NUM_SMALLER_SDHs = 2;

    SignalDetectionHypothesis driverSdh = SignalDetectionHypothesis.builder(
        UUID.randomUUID(),
        UUID.randomUUID(),
        false,
        UUID.randomUUID())
        .addMeasurement(TestData.arrivalTimeFeatureMeasurement)
        .build();

    CandidateEvent eventLarger = CandidateEvent.from(
        TestData.stationId1,
        TestData.gridNode1,
        driverSdh,
        sigmaTime,
        IntStream.range(0, NUM_LARGER_SDHs).mapToObj(i ->
            SignalDetectionHypothesis
                .builder(
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    false,
                    UUID.randomUUID())
                .addMeasurement(TestData.arrivalTimeFeatureMeasurement)
                .build())
            .collect(Collectors.toSet())
    );

    CandidateEvent eventSmaller = CandidateEvent.from(
        TestData.stationId2,
        TestData.gridNode2,
        driverSdh,
        sigmaTime,
        IntStream.range(0, NUM_SMALLER_SDHs).mapToObj(i ->
            SignalDetectionHypothesis
                .builder(
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    false,
                    UUID.randomUUID())
                .addMeasurement(TestData.arrivalTimeFeatureMeasurement)
                .build())
            .collect(Collectors.toSet())
    );

    DefaultRedundancyRemovalDelegate delegate = testInitializeAndReturn();

    Set<CandidateEvent> reducedEvents = delegate.reduce(List.of(
        eventSmaller, eventLarger), definition);

    Assertions.assertEquals(1, reducedEvents.size());
    Assertions.assertEquals(eventLarger.getStationId(),
        reducedEvents.stream().findFirst().get().getStationId());
  }

  @Test
  void testFourEventsTwoDifferentSdhs() throws Exception {
    final int NUM_LARGER_SDHs_1 = 5;
    final int NUM_SMALLER_SDHs_1 = 2;

    final int NUM_LARGER_SDHs_2 = 20;
    final int NUM_SMALLER_SDHs_2 = 5;

    SignalDetectionHypothesis driverSdh1 = SignalDetectionHypothesis.builder(
        UUID.randomUUID(),
        UUID.randomUUID(),
        false,
        UUID.randomUUID())
        .addMeasurement(TestData.arrivalTimeFeatureMeasurement)
        .build();

    CandidateEvent eventLarger1 = CandidateEvent.from(
        TestData.stationId1,
        TestData.gridNode1,
        driverSdh1,
        sigmaTime,
        IntStream.range(0, NUM_LARGER_SDHs_1).mapToObj(i ->
            SignalDetectionHypothesis.builder(
                UUID.randomUUID(),
                UUID.randomUUID(),
                false,
                UUID.randomUUID()
            ).build()
        ).collect(Collectors.toSet()));

    CandidateEvent eventSmaller1 = CandidateEvent.from(
        TestData.stationId1,
        TestData.gridNode1,
        driverSdh1,
        sigmaTime,
        IntStream.range(0, NUM_SMALLER_SDHs_1).mapToObj(i ->
            SignalDetectionHypothesis.builder(
                UUID.randomUUID(),
                UUID.randomUUID(),
                false,
                UUID.randomUUID()
            ).build()
        ).collect(Collectors.toSet()));

    SignalDetectionHypothesis driverSdh2 = SignalDetectionHypothesis.builder(
        UUID.randomUUID(),
        UUID.randomUUID(),
        false,
        UUID.randomUUID())
        .addMeasurement(TestData.arrivalTimeFeatureMeasurement)
        .build();

    CandidateEvent eventLarger2 = CandidateEvent.from(
        TestData.stationId2,
        TestData.gridNode2,
        driverSdh2,
        sigmaTime,
        IntStream.range(0, NUM_LARGER_SDHs_2).mapToObj(i ->
            SignalDetectionHypothesis.builder(
                UUID.randomUUID(),
                UUID.randomUUID(),
                false,
                UUID.randomUUID()
            ).build()
        ).collect(Collectors.toSet()));

    CandidateEvent eventSmaller2 = CandidateEvent.from(
        TestData.stationId2,
        TestData.gridNode2,
        driverSdh2,
        sigmaTime,
        IntStream.range(0, NUM_SMALLER_SDHs_2).mapToObj(i ->
            SignalDetectionHypothesis.builder(
                UUID.randomUUID(),
                UUID.randomUUID(),
                false,
                UUID.randomUUID()
            ).build()
        ).collect(Collectors.toSet()));

    DefaultRedundancyRemovalDelegate delegate = testInitializeAndReturn();

    Set<CandidateEvent> reducedEvents = delegate.reduce(List.of(
        eventSmaller1, eventLarger2, eventSmaller2, eventLarger1
    ), definition);

    Assertions.assertEquals(2, reducedEvents.size());
    Assertions.assertTrue(reducedEvents.contains(eventLarger2));
    Assertions.assertTrue(reducedEvents.contains(eventLarger1));

  }

  /**
   * @return A mocked plugin registry that insures lookup() is called with expected arguments.
   * arguments passed to the registry are based on configuration.
   */
  private PluginRegistry getMockedVerifyingPluginRegistry() {
    PluginRegistry registry = Mockito.mock(PluginRegistry.class);

    Mockito.when(registry.lookup(any(), any())).thenAnswer(invocation -> {
      PluginInfo pluginInfo = invocation.getArgument(0);

      Assertions.assertEquals(PluginInfo.from(
          //
          // Currently, should match application.properties.
          //
          "myCrazyWeightCalculationPlugin",
          "41.12.13"
      ), pluginInfo);

      Class<?> clazz = invocation.getArgument(1);

      Assertions.assertEquals(WeightedEventCriteriaCalculation.class, clazz);

      return Optional.of(mockCalculation);
    });

    return registry;
  }

  /**
   * Create a delegate that uses a mocked plugin registry and return it; but not before testing that
   * initialize() worked as expected.
   *
   * @return delegate to perform further testing on.
   */
  private DefaultRedundancyRemovalDelegate testInitializeAndReturn() throws Exception {
    PluginRegistry registry = getMockedVerifyingPluginRegistry();

    DefaultRedundancyRemovalDelegate delegate = new DefaultRedundancyRemovalDelegate();

    FieldSetter
        .setField(delegate, delegate.getClass().getDeclaredField("pluginRegistry"), registry);

    delegate.initialize();

    Mockito.verify(registry, times(1)).loadAndRegister();
    Mockito.verify(registry, times(1)).lookup(any(), any());

    return delegate;
  }
}
