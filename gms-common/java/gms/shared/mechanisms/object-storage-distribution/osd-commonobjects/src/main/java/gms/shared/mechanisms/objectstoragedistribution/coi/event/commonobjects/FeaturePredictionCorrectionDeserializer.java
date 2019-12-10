package gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;

public class FeaturePredictionCorrectionDeserializer extends
    JsonDeserializer<FeaturePredictionCorrection> {

  public FeaturePredictionCorrection deserialize(JsonParser p, DeserializationContext ctxt)
      throws IOException {

    ObjectCodec oc = p.getCodec();
    JsonNode node = oc.readTree(p);

    FeaturePredictionCorrectionType correctionType = FeaturePredictionCorrectionType
        .valueOf(node.get("correctionType").asText());

    if (correctionType == FeaturePredictionCorrectionType.ELLIPTICITY_CORRECTION) {
      return EllipticityCorrection1dDefinition.create();
    } else if (correctionType == FeaturePredictionCorrectionType.ELEVATION_CORRECTION) {
      boolean isUsingGlobalVelocity = Boolean.valueOf(node.get("usingGlobalVelocity").textValue());
      return ElevationCorrection1dDefinition.create(isUsingGlobalVelocity);
    } else {
      throw new IOException(
          "Could not deserialize FeaturePredictionCorrection into any implementation");
    }
  }
}
