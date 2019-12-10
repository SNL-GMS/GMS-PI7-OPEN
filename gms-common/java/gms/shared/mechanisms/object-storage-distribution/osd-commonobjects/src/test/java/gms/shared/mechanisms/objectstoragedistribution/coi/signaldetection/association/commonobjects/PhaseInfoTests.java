package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.association.commonobjects;

import static org.mockito.BDDMockito.given;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.PhaseType;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.TestUtilities;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

public class PhaseInfoTests {

  private static final PhaseType phaseType = PhaseType.P;
  private static final boolean primary = false;
  private static final double travelTimeSeconds = 0.1;
  private static final double azimuthDegrees = 0.2;
  private static final double backAzimuthDegrees = 0.3;
  private static final double travelTimeMinimum = 0.4;
  private static final double travelTimeMaximum = 0.5;
  private static final double radialTravelTimeDerivative = 0.6;
  private static final double verticalTravelTimeDerivative = 0.7;
  private static final double slownessCellWidth = 0.8;
  private static final double slowness = 12.0;
  private static final double minimumMagnitude = 0.9;
  private static final double magnitudeCorrection = 1.0;
  private static final double radialMagnitudeCorrectionDerivative = 1.1;
  private static final double verticalMagnitudeCorrectionDerivative = 1.2;

  private static PhaseInfo phaseInfo = PhaseInfo.from(
      phaseType,
      primary,
      travelTimeSeconds,
      azimuthDegrees,
      backAzimuthDegrees,
      travelTimeMinimum,
      travelTimeMaximum,
      radialTravelTimeDerivative,
      verticalTravelTimeDerivative,
      slownessCellWidth,
      slowness,
      minimumMagnitude,
      magnitudeCorrection,
      radialMagnitudeCorrectionDerivative,
      verticalMagnitudeCorrectionDerivative
  );

  @Test
  void testSerialization() throws Exception {
    TestUtilities.testSerialization(phaseInfo, PhaseInfo.class);
  }

  @Test
  void testFrom() {
    PhaseInfo phaseInfo = PhaseInfo.from(
        phaseType,
        primary,
        travelTimeSeconds,
        azimuthDegrees,
        backAzimuthDegrees,
        travelTimeMinimum,
        travelTimeMaximum,
        radialTravelTimeDerivative,
        verticalTravelTimeDerivative,
        slownessCellWidth,
        slowness,
        minimumMagnitude,
        magnitudeCorrection,
        radialMagnitudeCorrectionDerivative,
        verticalMagnitudeCorrectionDerivative
    );

    Assertions.assertEquals(phaseType, phaseInfo.getPhaseType());
    Assertions.assertEquals(primary, phaseInfo.isPrimary());
    Assertions.assertEquals(travelTimeSeconds, phaseInfo.getTravelTimeSeconds());
    Assertions.assertEquals(azimuthDegrees, phaseInfo.getAzimuthDegrees());
    Assertions.assertEquals(backAzimuthDegrees, phaseInfo.getBackAzimuthDegrees());
    Assertions.assertEquals(travelTimeMinimum, phaseInfo.getTravelTimeMinimum());
    Assertions.assertEquals(travelTimeMaximum, phaseInfo.getTravelTimeMaximum());
    Assertions.assertEquals(radialTravelTimeDerivative, phaseInfo.getRadialTravelTimeDerivative());
    Assertions
        .assertEquals(verticalTravelTimeDerivative, phaseInfo.getVerticalTravelTimeDerivative());
    Assertions.assertEquals(slownessCellWidth, phaseInfo.getSlownessCellWidth());
    Assertions.assertEquals(slowness, phaseInfo.getSlowness());
    Assertions.assertEquals(minimumMagnitude, phaseInfo.getMinimumMagnitude());
    Assertions.assertEquals(magnitudeCorrection, phaseInfo.getMagnitudeCorrection());
    Assertions.assertEquals(radialMagnitudeCorrectionDerivative,
        phaseInfo.getRadialMagnitudeCorrectionDerivative());
    Assertions.assertEquals(verticalMagnitudeCorrectionDerivative,
        phaseInfo.getVerticalMagnitudeCorrectionDerivative());
  }

  @ParameterizedTest
  @MethodSource("testCompareToProvider")
  void testCompareTo(
      double travelTime1,
      double travelTime2,
      int expectedResult
  ) {

    PhaseInfo mockPhaseInfo1 = Mockito.mock(PhaseInfo.class);
    given(mockPhaseInfo1.getTravelTimeSeconds()).willReturn(travelTime1);

    PhaseInfo mockPhaseInfo2 = Mockito.mock(PhaseInfo.class);
    given(mockPhaseInfo2.getTravelTimeSeconds()).willReturn(travelTime2);

    given(mockPhaseInfo1.compareTo(mockPhaseInfo2)).willCallRealMethod();
    given(mockPhaseInfo2.compareTo(mockPhaseInfo1)).willCallRealMethod();

    int comparisonResult = mockPhaseInfo1.compareTo(mockPhaseInfo2);
    int transitiveComparisonResult = mockPhaseInfo2.compareTo(mockPhaseInfo1);

    Assertions.assertEquals(expectedResult, comparisonResult);
    Assertions.assertEquals(-expectedResult, transitiveComparisonResult);
  }

  static Stream<Arguments> testCompareToProvider() {

    return Stream.of(
        Arguments.of(
            1.0,
            2.0,
            -1
        ),
        Arguments.of(
            2.0,
            2.0,
            0
        ),
        Arguments.of(
            3.0,
            2.0,
            1
        )
    );
  }
}
