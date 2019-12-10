package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects;


import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.TestUtilities;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FilterDefinition.Builder;
import java.io.IOException;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class FilterDefinitionTests {

  private static final String name = "Detection filter";
  private static final String description = "Detection low pass filter";
  private static final FilterType type = FilterType.FIR_HAMMING;
  private static final FilterPassBandType passBandType = FilterPassBandType.LOW_PASS;
  private static final double low = 0.0;
  private static final double high = 5.0;
  private static final int order = 1;
  private static final FilterSource source = FilterSource.SYSTEM;
  private static final FilterCausality causality = FilterCausality.CAUSAL;
  private static final boolean isZeroPhase = true;
  private static final double sampleRate = 40.0;
  private static final double sampleRateTolerance = 3.14;
  private static final double[] aCoeffs = new double[]{6.7, 7.8};
  private static final double[] bCoeffs = new double[]{3.4, 4.5};
  private static final double groupDelay = 1.5;

  private static Builder defaultBuilder() {
    return FilterDefinition.builder()
        .setName(name)
        .setDescription(description)
        .setFilterType(type)
        .setFilterPassBandType(passBandType)
        .setLowFrequencyHz(low)
        .setHighFrequencyHz(high)
        .setOrder(order)
        .setFilterSource(source)
        .setFilterCausality(causality)
        .setZeroPhase(isZeroPhase)
        .setSampleRate(sampleRate)
        .setSampleRateTolerance(sampleRateTolerance)
        .setaCoefficients(aCoeffs)
        .setbCoefficients(bCoeffs)
        .setGroupDelaySecs(groupDelay);
  }

  @Test
  void testSerialization() throws IOException {
    TestUtilities.testSerialization(defaultBuilder().build(),
        FilterDefinition.class);
  }

  @Test
  void testFirBuilderSetsFirSpecificFields() {
    FilterDefinition filter = FilterDefinition
        .firBuilder()
        .setName(name)
        .setDescription(description)
        .setFilterPassBandType(passBandType)
        .setLowFrequencyHz(low)
        .setHighFrequencyHz(high)
        .setOrder(order)
        .setFilterSource(source)
        .setFilterCausality(causality)
        .setZeroPhase(isZeroPhase)
        .setSampleRate(sampleRate)
        .setSampleRateTolerance(sampleRateTolerance)
        .setbCoefficients(bCoeffs)
        .setGroupDelaySecs(groupDelay)
        .build();

    assertEquals(FilterType.FIR_HAMMING, filter.getFilterType());
    assertArrayEquals(new double[]{1.0}, filter.getaCoefficients());
  }

  @ParameterizedTest
  @MethodSource("builderIllegalArguments")
  void testBuilderIllegalArguments(Builder builder, String expectedMessage) {
    IllegalStateException actualException = assertThrows(IllegalStateException.class,
        builder::build);
    assertEquals(expectedMessage, actualException.getMessage());
  }

  private static Stream<Arguments> builderIllegalArguments() {
    return Stream.of(
        arguments(defaultBuilder().setLowFrequencyHz(2.0).setHighFrequencyHz(2.0),
            "FilterDefinition requires low frequency < high frequency"),
        arguments(defaultBuilder().setLowFrequencyHz(2.0).setHighFrequencyHz(1.9),
            "FilterDefinition requires low frequency < high frequency"),
        arguments(defaultBuilder().setLowFrequencyHz(-1.0),
            "FilterDefinition requires low frequency >= 0.0"),
        arguments(defaultBuilder().setHighFrequencyHz(-1.0),
            "FilterDefinition requires high frequency >= 0.0"),
        arguments(defaultBuilder().setOrder(0),
            "FilterDefinition requires order > 0"),
        arguments(defaultBuilder().setOrder(-1),
            "FilterDefinition requires order > 0"),
        arguments(defaultBuilder().setSampleRate(0.0),
            "FilterDefinition requires sampleRate > 0"),
        arguments(defaultBuilder().setSampleRate(-1.0),
            "FilterDefinition requires sampleRate > 0"),
        arguments(defaultBuilder().setSampleRateTolerance(-1.0),
            "FilterDefinition requires sampleRateTolerance >= 0"),
        arguments(defaultBuilder().setaCoefficients(new double[0]),
            "FilterDefinition requires at least 1 aCoefficient"),
        arguments(defaultBuilder().setbCoefficients(new double[0]),
            "FilterDefinition requires at least 1 bCoefficient"));
  }
}
