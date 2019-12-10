package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.datatransferobjects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Response;
import java.util.UUID;

/**
 * Create class to allow transformation to and from JSON.
 */
public interface ResponseDto {

  @JsonCreator
  static Response from(
      @JsonProperty("id") UUID id,
      @JsonProperty("responseData") byte[] responseData) {
    return Response.from(id, responseData);
  }
}
