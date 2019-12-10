package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects;


import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

/**
 * Periodically, the instrument corresponding to a Channel is calibrated to characterize the true
 * relationship between the underlying phenomenon the instrument is measuring and the actual output
 * of the instrument. As with the manufacturer-provided calibration information, this calibration
 * information is stored in the Calibration and Response classes. Response includes the full
 * response function across a range of periods/frequencies.
 */
public class Response {

  private final UUID id;
  private final byte[] responseData;

  /**
   * Create a new Response.
   *
   * @param responseData The response data.
   * @return A new Response object.
   */
  public static Response create(byte[] responseData) {
    return new Response(UUID.randomUUID(), responseData);
  }

  /**
   * Recreates a Response given all params
   *
   * @param id the id of the response
   * @param responseData The response data.
   * @return a Response
   * @throws NullPointerException if any arg is null
   * @throws IllegalArgumentException if string arg is empty
   */
  public static Response from(UUID id, byte[] responseData) {
    return new Response(id, responseData);
  }

  /**
   * Create an instance of the class.
   *
   * @param id the identifier for this Response
   * @param responseData The response data.
   * @throws NullPointerException if any arg is null
   * @throws IllegalArgumentException if string arg is empty
   */
  private Response(UUID id, byte[] responseData) {

    this.id = Objects.requireNonNull(id);
    this.responseData = Objects.requireNonNull(responseData);
  }

  public UUID getId() { return id; }

  public byte[] getResponseData() { return responseData; }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Response response = (Response) o;

    if (getId() != null ? !getId().equals(response.getId()) : response.getId() != null) {
      return false;
    }
    return Arrays.equals(getResponseData(), response.getResponseData());
  }

  @Override
  public int hashCode() {
    int result = getId() != null ? getId().hashCode() : 0;
    result = 31 * result + Arrays.hashCode(getResponseData());
    return result;
  }

  @Override
  public String toString() {
    return "Response{" +
        "id=" + id +
        ", responseData=" + Arrays.toString(responseData) +
        '}';
  }
}
