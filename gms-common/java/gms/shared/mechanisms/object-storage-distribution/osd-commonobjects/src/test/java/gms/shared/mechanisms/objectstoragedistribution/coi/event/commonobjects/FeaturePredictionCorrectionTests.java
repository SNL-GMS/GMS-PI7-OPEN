package gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.databind.ObjectMapper;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.datatransferobjects.CoiObjectMapperFactory;
import java.io.IOException;
import org.junit.Test;

public class FeaturePredictionCorrectionTests {

  ObjectMapper mapper;

  public FeaturePredictionCorrectionTests() {
    this.mapper = CoiObjectMapperFactory.getJsonObjectMapper();
  }

  @Test
  public void testElevationCorrectionSerializationDeserializationIntoInterface() throws IOException {
    FeaturePredictionCorrection elevationCorrection = ElevationCorrection1dDefinition.create(false);
    String elevationCorrectionJson = this.mapper.writeValueAsString(elevationCorrection);

    FeaturePredictionCorrection deserializedElevationCorrection = this.mapper
        .readValue(elevationCorrectionJson, FeaturePredictionCorrection.class);

    assertEquals(elevationCorrection.getClass(), deserializedElevationCorrection.getClass());
    assertEquals(elevationCorrection.getCorrectionType(),
        deserializedElevationCorrection.getCorrectionType());
    assertEquals(((ElevationCorrection1dDefinition) elevationCorrection).isUsingGlobalVelocity(),
        ((ElevationCorrection1dDefinition) deserializedElevationCorrection)
            .isUsingGlobalVelocity());
  }

  @Test
  public void testEllipticityCorrectionSerializationDeserializationIntoInterface() throws IOException {
    FeaturePredictionCorrection ellipticityCorrection = EllipticityCorrection1dDefinition.create();
    String ellipticityCorrectionJson = this.mapper.writeValueAsString(ellipticityCorrection);

    FeaturePredictionCorrection deserializedEllipticityCorrection = this.mapper
        .readValue(ellipticityCorrectionJson, FeaturePredictionCorrection.class);

    assertEquals(ellipticityCorrection.getClass(), deserializedEllipticityCorrection.getClass());
    assertEquals(ellipticityCorrection.getCorrectionType(),
        deserializedEllipticityCorrection.getCorrectionType());
  }

  @Test
  public void testDeserializeElevationCorrectionIntoEllipticityCorrectionFails() throws IOException {
    FeaturePredictionCorrection elevationCorrection = ElevationCorrection1dDefinition.create(false);
    String elevationCorrectionJson = this.mapper.writeValueAsString(elevationCorrection);

    assertThrows(ClassCastException.class, () -> {
      EllipticityCorrection1dDefinition actual = this.mapper
          .readValue(elevationCorrectionJson, EllipticityCorrection1dDefinition.class);
    });
  }

  @Test
  public void testDeserializeEllipticityCorrectionIntoElevationCorrectionFails() throws IOException {
    FeaturePredictionCorrection ellipticityCorrection = EllipticityCorrection1dDefinition.create();
    String ellipticityCorrectionJson = this.mapper.writeValueAsString(ellipticityCorrection);

    assertThrows(ClassCastException.class, () -> {
      ElevationCorrection1dDefinition actual = this.mapper
          .readValue(ellipticityCorrectionJson, ElevationCorrection1dDefinition.class);
    });
  }
}
