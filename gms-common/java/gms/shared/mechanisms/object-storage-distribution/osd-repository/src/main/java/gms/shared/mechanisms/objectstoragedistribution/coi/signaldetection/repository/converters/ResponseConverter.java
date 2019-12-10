package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.converters;

import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Response;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceResponse;
import java.util.Objects;

/**
 * Converts ReferenceResponse to Response.
 */
public class ResponseConverter {

  /**
   * Converts a ReferenceResponse to a Response.
   * @param r the reference response
   * @return a Response
   */
  public static Response from(ReferenceResponse r) {
    Objects.requireNonNull(r);

    return Response.from(r.getId(), r.getResponseData());
  }

}
