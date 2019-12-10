package gms.core.featureprediction.plugins.implementations.signalfeaturepredictor;

import static org.junit.jupiter.api.Assertions.*;

import gms.core.featureprediction.exceptions.MissingEarthModelOrPhaseException;
import gms.core.featureprediction.plugins.DepthDistance1dModelSet;
import gms.core.featureprediction.plugins.Distance1dModelSet;
import gms.core.featureprediction.plugins.implementations.signalfeaturepredictor.PredictionType.PredictionDefinition;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.PhaseType;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.EventLocation;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FeatureMeasurementType;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FeatureMeasurementTypes;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Location;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;

public class PredictionTypeTests {

  //TODO and NOTE:
  //This is being tested through the various signal feature prediction tests, but may be worth
  //adding more isolated testing. Testing that missing earthmodel/phase types throws exceptions
  //for now.

  private static final String EXTANT_MODEL = "weirdModel";
  private static final PhaseType EXTANT_PHASE = PhaseType.I;

  private static final String NON_EXTANT_MODEL = "weirdModel1";
  private static final PhaseType NON_EXTANT_PHASE = PhaseType.LQ;

  DepthDistance1dModelSet<double[], double[][]> depthDistance1dModelSet = new DepthDistance1dModelSet<>() {
    @Override
    public void initialize(Set<String> earthModelNames) throws IOException {

    }

    @Override
    public Set<String> getEarthModelNames() {
      return Set.of(EXTANT_MODEL);
    }

    @Override
    public Set<PhaseType> getPhaseTypes(String earthModelName) {
      return Set.of(EXTANT_PHASE);
    }

    @Override
    public double[] getDepthsKm(String earthModelName, PhaseType phase) {
      return new double[0];
    }

    @Override
    public double[] getDistancesDeg(String earthModelName, PhaseType phase) {
      return new double[0];
    }

    @Override
    public double[][] getValues(String earthModelName, PhaseType phase) {
      return new double[0][];
    }

    @Override
    public Optional<double[]> getDepthModelingErrors(String earthModelName, PhaseType phase) {
      return Optional.empty();
    }

    @Override
    public Optional<double[]> getDistanceModelingErrors(String earthModelName, PhaseType phase) {
      return Optional.empty();
    }

    @Override
    public Optional<double[][]> getValueModelingErrors(String earthModelName, PhaseType phase) {
      return Optional.empty();
    }
  };

  Distance1dModelSet distance1dModelSet = new Distance1dModelSet() {
    @Override
    public void initialize(Set<String> earthModelNames) throws IOException {

    }

    @Override
    public Set<String> getEarthModelNames() {
      return null;
    }

    @Override
    public Set<PhaseType> getPhaseTypes(String earthModelName) {
      return null;
    }

    @Override
    public double[] getDistancesDeg(String earthModelName, PhaseType phase) {
      return new double[0];
    }

    @Override
    public double[] getValues(String earthModelName, PhaseType phase) {
      return new double[0];
    }

    @Override
    public Optional<double[]> getDistanceModelingErrors(String earthModelName, PhaseType phase) {
      return Optional.empty();
    }

    @Override
    public Optional<double[]> getValueModelingErrors(String earthModelName, PhaseType phase) {
      return Optional.empty();
    }
  };

  @Test
  public void testMissingEarthModelOrPhaseThrowsMEMOPE() {
    PredictionDefinition definition = new PredictionDefinition(
        depthDistance1dModelSet,
        distance1dModelSet,
        FeatureMeasurementTypes.SLOWNESS,
        NON_EXTANT_MODEL,
        EventLocation.from(
            0.0,
            0.0,
            0.0,
            Instant.EPOCH
        ),
        Location.from(
            0.0,
            0.0,
            0.0,
            0.0
        ),
        NON_EXTANT_PHASE,
        false
    );

    List.of(
        PredictionType.SLOWNESS,
        PredictionType.ARRIVAL_TIME,
        PredictionType.MAGNITUDE_CORRECTION
    ).forEach(predictionType ->
        assertThrows(MissingEarthModelOrPhaseException.class,
            () -> PredictionType.SLOWNESS.predict(definition)));

  }

}
