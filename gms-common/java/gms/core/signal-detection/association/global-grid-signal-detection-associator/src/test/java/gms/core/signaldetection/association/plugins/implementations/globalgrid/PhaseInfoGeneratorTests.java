package gms.core.signaldetection.association.plugins.implementations.globalgrid;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.PhaseType;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.InformationSource;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.association.commonobjects.PhaseInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceStation;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.StationType;
import gms.shared.utilities.signalfeaturepredictionutility.SignalFeaturePredictionUtility;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

public class PhaseInfoGeneratorTests {

  @Test
  void testPhaseInfoGeneratorConfiguration() throws Exception {
    // Tests that the generator continues to throw IllegalStateExceptions until
    // all required parameters have been set.

    PhaseInfoGenerator generator = new PhaseInfoGenerator();

    assertThrows(IllegalStateException.class, () -> {
      Optional<PhaseInfo> opt = generator.generate();
    });

    SignalFeaturePredictionUtility predictionUtility = new SignalFeaturePredictionUtility();
    generator.predictionUtility(predictionUtility);
    assertTrue(predictionUtility == generator.predictionUtility());

    assertThrows(IllegalStateException.class, () -> {
      Optional<PhaseInfo> opt = generator.generate();
    });

    String ttpEarthModel = "ak135";
    generator.travelTimePredictionEarthModel(ttpEarthModel);
    assertEquals(ttpEarthModel, generator.travelTimePredictionEarthModel());

    assertThrows(IllegalStateException.class, () -> {
      Optional<PhaseInfo> opt = generator.generate();
    });

    String mapEarthModel = "VeithClawson72";
    generator.magnitudeAttenuationPredictionEarthModel(mapEarthModel);
    assertEquals(mapEarthModel, generator.magnitudeAttenuationPredictionEarthModel());

    assertThrows(IllegalStateException.class, () -> {
      Optional<PhaseInfo> opt = generator.generate();
    });

    generator.gridCylinderRadiusDegrees(2.0);
    assertEquals(2.0, generator.gridCylinderRadiusDegrees());

    assertThrows(IllegalStateException.class, () -> {
      Optional<PhaseInfo> opt = generator.generate();
    });

    generator.gridCylinderHeightKm(50.0);
    assertEquals(50.0, generator.gridCylinderHeightKm());

    assertThrows(IllegalStateException.class, () -> {
      Optional<PhaseInfo> opt = generator.generate();
    });

    generator.gridPointLatDegrees(20.0);
    assertEquals(20.0, generator.gridPointLatDegrees());

    assertThrows(IllegalStateException.class, () -> {
      Optional<PhaseInfo> opt = generator.generate();
    });

    generator.gridPointLonDegrees(75.0);
    assertEquals(75.0, generator.gridPointLonDegrees());

    assertThrows(IllegalStateException.class, () -> {
      Optional<PhaseInfo> opt = generator.generate();
    });

    generator.gridPointDepthKm(10.0);
    assertEquals(10.0, generator.gridPointDepthKm());

    assertThrows(IllegalStateException.class, () -> {
      Optional<PhaseInfo> opt = generator.generate();
    });

    ReferenceStation referenceStation = ReferenceStation.create(
        "MKAR",
        "WHAT??",
        StationType.SeismicArray,
        InformationSource.create("WHO??", Instant.EPOCH, "NO!"),
        "NO!!",
        0.0,
        10.0,
        0.0,
        Instant.EPOCH,
        Instant.EPOCH,
        List.of()
    );

    generator.referenceStation(referenceStation);
    assertEquals(referenceStation, generator.referenceStation());

    assertThrows(IllegalStateException.class, () -> {
      Optional<PhaseInfo> opt = generator.generate();
    });

    generator.phaseType(PhaseType.P);
    assertEquals(PhaseType.P, generator.phaseType());

    assertThrows(IllegalStateException.class, () -> {
      Optional<PhaseInfo> opt = generator.generate();
    });

    generator.minimumMagnitude(0.5);
    assertEquals(0.5, generator.minimumMagnitude());

    Optional<PhaseInfo> opt = generator.generate();
  }

  @Test
  void testGeneratePhaseInfo() throws Exception {

    final PhaseInfoGenerator phaseInfoBuilder = new PhaseInfoGenerator()
        .predictionUtility(new SignalFeaturePredictionUtility())
        .travelTimePredictionEarthModel("ak135")
        .magnitudeAttenuationPredictionEarthModel("VeithClawson72")
        .gridCylinderRadiusDegrees(3.5)
        .gridCylinderHeightKm(0.25)
        .gridPointLatDegrees(0.0)
        .gridPointLonDegrees(0.0)
        .gridPointDepthKm(50.0)
        .referenceStation(
            ReferenceStation.create(
                "MKAR",
                "WHAT??",
                StationType.SeismicArray,
                InformationSource.create("WHO??", Instant.EPOCH, "NO!"),
                "NO!!",
                0.0,
                10.0,
                0.0,
                Instant.EPOCH,
                Instant.EPOCH,
                List.of()
            )
        ).phaseType(PhaseType.P)
        .minimumMagnitude(3.5);

    PhaseInfo phaseInfo = phaseInfoBuilder.generate().orElseThrow(() ->
        new AssertionError("Expected a valid PhaseInfo, got empty Optional")
    );

    // Assert PhaseInfoGenerator produced correct phase type
    assertEquals(PhaseType.P, phaseInfo.getPhaseType());

    // Assert PhaseInfoGenerator produced correct "is primary"
    assertTrue(phaseInfo.isPrimary());

    // Extract minimum, maximum, and grid point travel time from built PhaseInfo
    double minTravelTime = phaseInfo.getTravelTimeMinimum();
    double maxTravelTime = phaseInfo.getTravelTimeMaximum();
    double gridPointTravelTime = phaseInfo.getTravelTimeSeconds();

    // Assert that gridPointTravelTime is between min and max travel times
    assertTrue(gridPointTravelTime > minTravelTime && gridPointTravelTime < maxTravelTime);

    // Assert that minimum travel time is less that maximum travel time
    assertTrue(minTravelTime < maxTravelTime);

    // Assert PhaseInfoGenerator produced correct azimuth
    assertEquals(90.0, phaseInfo.getAzimuthDegrees());

    // Assert PhaseInfoGenerator produced correct back azimuth
    assertEquals(270.0, phaseInfo.getBackAzimuthDegrees());

    // Assert radial travel time derivative is negative
    assertTrue(phaseInfo.getRadialTravelTimeDerivative() < 0);

    // Assert vertical travel time derivative is positive
    assertTrue(phaseInfo.getVerticalTravelTimeDerivative() > 0);

    // Assert slowness cell width is positive
    assertTrue(phaseInfo.getSlownessCellWidth() > 0);

    // Assert PhaseInfoGenerator produced correct minimum magnitude
    assertEquals(3.5, phaseInfo.getMinimumMagnitude());

    // Assert magnitude correction is positive
    assertTrue(phaseInfo.getMagnitudeCorrection() > 0);

    // Assert radial magnitude correctino derivative is negative
    assertTrue(phaseInfo.getRadialTravelTimeDerivative() < 0);

    // Assert vertical magnitude correction derivative is positive
    assertTrue(phaseInfo.getVerticalTravelTimeDerivative() > 0);
  }
}
