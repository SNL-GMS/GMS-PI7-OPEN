package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.datatransferobjects;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMask;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskVersion;
import java.util.List;
import java.util.UUID;

/**
 * DTO for {@link QcMask}
 *
 * Created by jrhipp on 9/7/17.
 */
public interface QcMaskDto {

  @JsonCreator
  static QcMask from(
      @JsonProperty("id") UUID id,
      @JsonProperty("channelId") UUID channelId,
      @JsonProperty("qcMaskVersions") List<QcMaskVersion> qcMaskVersions) {
    return QcMask.from(id, channelId, qcMaskVersions);
  }

  @JsonIgnore
  QcMaskVersion getCurrentQcMaskVersion();
}
