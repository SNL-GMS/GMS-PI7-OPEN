package gms.core.featureprediction.plugins.implementations.ellipticitycorrection;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.PhaseType;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.EventLocation;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.FeaturePredictionComponent;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Location;
import gms.shared.mechanisms.pluginregistry.PluginRegistry;
import java.time.Instant;
import java.util.Set;
import org.junit.Assert;
//import org.junit.Rule;
import org.junit.Rule;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
//import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.rules.ExpectedException;


public class DziewonskiGilbertEllipticityCorrectionsTests {

  private Set<String> earthModelNames = Set.of("ak135");

  Location receiverLocation = Location.from(
      0,
      0,
      0, 0
  );

  EventLocation sourceLocation = EventLocation.from(
      0,
      90,
      5.0,
      Instant.now()
  );

  @Test
  public void testInitialize() {
    DziewonskiGilbertEllipticityCorrection s = new DziewonskiGilbertEllipticityCorrection();
    s.initialize(this.earthModelNames);
  }

  @Test
  public void testCorrect() {
    DziewonskiGilbertEllipticityCorrection s = new DziewonskiGilbertEllipticityCorrection();
    s.initialize(this.earthModelNames);

    EventLocation sourceLocation2 = EventLocation.from(
        45,
        90,
        5,
        Instant.now()
    );

    EventLocation sourceLocation3 = EventLocation.from(
        90,
        0,
        5,
        Instant.now()
    );

    FeaturePredictionComponent correctionEl1 = s
        .correct("ak135", this.sourceLocation, this.receiverLocation, PhaseType.P);

    FeaturePredictionComponent correctionEl2 = s
        .correct("ak135", sourceLocation2, this.receiverLocation, PhaseType.P);

    Assert.assertTrue(correctionEl1.getValue().getValue() > correctionEl2.getValue().getValue());

    FeaturePredictionComponent correctionEl3 = s
        .correct("ak135", sourceLocation3, this.receiverLocation, PhaseType.P);

    Assert.assertTrue(correctionEl2.getValue().getValue() > correctionEl3.getValue().getValue());
  }

  @Test
  public void testInitializeNullConfigThrowsNullPointerException() throws NullPointerException {
    DziewonskiGilbertEllipticityCorrection a = new DziewonskiGilbertEllipticityCorrection();
    NullPointerException exception = Assertions.assertThrows(NullPointerException.class,
        () -> a.initialize(null));
    Assertions.assertEquals(
        "DziewonskiGilbertEllipticityCorrection::initialize() requires non-null config parameter.",
        exception.getMessage());
  }

  @Test
  public void testCorrectNullModelNameThrowsNullPointerException()
      throws NullPointerException {
    DziewonskiGilbertEllipticityCorrection a = new DziewonskiGilbertEllipticityCorrection();
    NullPointerException exception = Assertions.assertThrows(NullPointerException.class,
        () -> a.correct(null, this.sourceLocation, this.receiverLocation, PhaseType.P));

    Assertions.assertEquals(
        "DziewonskiGilbertEllipticityCorrection::correct() requires non-null modelName parameter",
        exception.getMessage());
  }

  @Test
  public void testCorrectNullSourceLocationThrowsNullPointerException()
      throws NullPointerException {

    DziewonskiGilbertEllipticityCorrection a = new DziewonskiGilbertEllipticityCorrection();
    NullPointerException exception = Assertions.assertThrows(NullPointerException.class,
        () -> a.correct("ak135", null, this.receiverLocation, PhaseType.P));

    Assertions.assertEquals(
        "DziewonskiGilbertEllipticityCorrection::correct() requires non-null sourceLocation parameter",
        exception.getMessage());
  }

  @Test
  public void testCorrectNullReceiverLocationThrowsNullPointerException()
      throws NullPointerException {

    DziewonskiGilbertEllipticityCorrection a = new DziewonskiGilbertEllipticityCorrection();
    NullPointerException exception = Assertions.assertThrows(NullPointerException.class,
        () -> a.correct("ak135", this.sourceLocation, null, PhaseType.P));

    Assertions.assertEquals(
        "DziewonskiGilbertEllipticityCorrection::correct() requires non-null receiverLocation parameter",
        exception.getMessage());
  }

  @Test
  public void testCorrectNullPhaseThrowsNullPointerException() throws NullPointerException {
    DziewonskiGilbertEllipticityCorrection a = new DziewonskiGilbertEllipticityCorrection();
    NullPointerException exception = Assertions.assertThrows(NullPointerException.class,
        () -> a.correct("ak135", this.sourceLocation, this.receiverLocation, null));

    Assertions.assertEquals(
        "DziewonskiGilbertEllipticityCorrection::correct() requires non-null phaseType parameter",
        exception.getMessage());
  }
}
