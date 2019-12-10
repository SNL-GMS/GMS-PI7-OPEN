package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import java.util.UUID;

/**
 * A descriptor for a {@link QcMask} with a particular {@link QcMaskVersion}
 */
@AutoValue
public abstract class QcMaskVersionDescriptor {

  public abstract UUID getQcMaskId();
  public abstract long getQcMaskVersionId();

  @JsonCreator
  public static QcMaskVersionDescriptor from(
      @JsonProperty("qcMaskId") UUID qcMaskId,
      @JsonProperty("qcMaskVersionId") long qcMaskVersionId) {

    return new AutoValue_QcMaskVersionDescriptor(qcMaskId, qcMaskVersionId);
  }
}
