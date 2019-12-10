package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.dataaccessobjects;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.DoubleValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.Units;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.FeaturePredictionComponent;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.FeaturePredictionCorrectionType;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.repository.jpa.dataaccessobjects.FeaturePredictionComponentDao;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class FeaturePredictionComponentDaoTests {

  private FeaturePredictionComponent predictionComponent = FeaturePredictionComponent.from(
      DoubleValue.from(1.0, 1.0, Units.DEGREES),
      true,
      FeaturePredictionCorrectionType.BASELINE_PREDICTION
  );

  @Test
  void testFromAndToCoi() {

    FeaturePredictionComponentDao featurePredictionComponentDao = FeaturePredictionComponentDao.from(this.predictionComponent);

    Assertions.assertEquals(this.predictionComponent, featurePredictionComponentDao.toCoi());
  }

  @Test
  void testFromNullFeaturePredictionComponent() {

    Throwable exception = Assertions
        .assertThrows(NullPointerException.class, () -> FeaturePredictionComponentDao.from(null));

    Assert.assertEquals("Cannot create FeaturePredictionComponentDao from null FeaturePredictionComponent",
        exception.getMessage());
  }
}
