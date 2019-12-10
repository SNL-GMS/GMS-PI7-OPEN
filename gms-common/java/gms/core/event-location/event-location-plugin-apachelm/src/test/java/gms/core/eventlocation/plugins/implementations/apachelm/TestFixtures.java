package gms.core.eventlocation.plugins.implementations.apachelm;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.PhaseType;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.EnumeratedMeasurementValue.PhaseTypeMeasurementValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FeatureMeasurement;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FeatureMeasurementTypes;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.InstantValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetectionHypothesis;
import gms.shared.utilities.geomath.RowFilteredRealMatrix;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealMatrixChangingVisitor;

public class TestFixtures {

  // List of SignalDetectionHypotheses used when creating the Event object in this class
  public static final List<SignalDetectionHypothesis> signalDetectionHypotheses = List.of(
      SignalDetectionHypothesis.from(
          UUID.randomUUID(),
          UUID.randomUUID(),
          false,
          List.of(
              FeatureMeasurement.create(
                  UUID.randomUUID(),
                  FeatureMeasurementTypes.ARRIVAL_TIME,
                  InstantValue.from(Instant.EPOCH, Duration.ofMillis(0))
              ),
              FeatureMeasurement.create(
                  UUID.randomUUID(),
                  FeatureMeasurementTypes.PHASE,
                  PhaseTypeMeasurementValue.from(
                      PhaseType.P,
                      0.0)
              )
          ),
          UUID.randomUUID()
      )
  );


  public static final RealMatrixChangingVisitor replaceWithZeroVisitor = new RealMatrixChangingVisitor() {
    @Override
    public void start(int rows, int columns, int startRow, int endRow, int startColumn,
        int endColumn) {

    }

    @Override
    public double visit(int row, int column, double value) {
      return Double.isNaN(value) ? 0 : value;
    }

    @Override
    public double end() {
      return 0;
    }
  };

  public static final Function<RealMatrix, RealMatrix> replaceWithZeroFilter =
      matrix -> {
        matrix.walkInOptimizedOrder(replaceWithZeroVisitor);
        return matrix;
      };
}
