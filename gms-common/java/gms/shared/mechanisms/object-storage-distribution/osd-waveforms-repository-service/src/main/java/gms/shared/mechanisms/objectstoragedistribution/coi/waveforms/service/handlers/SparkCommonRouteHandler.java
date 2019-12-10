package gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.service.handlers;

import java.time.Instant;
import org.eclipse.jetty.http.HttpStatus;

public class SparkCommonRouteHandler {

  private SparkCommonRouteHandler() {
    //private empty constructor denotes collection of functions
  }

  /**
   * State of health operation to determine if the waveforms-repository-service is running.  Returns
   * a message with the current time in plaintext.
   *
   * @return Response code 200 with a plaintext string containing the current time
   */
  public static String alive(spark.Request request, spark.Response response) {
    response.status(HttpStatus.OK_200);
    return "alive at " + Instant.now()
        .toString();
  }
}
