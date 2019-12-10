package gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects;

import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.InformationSource;
import java.nio.charset.StandardCharsets;
import java.security.InvalidParameterException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

/**
 * As with the manufacturer-provided calibration information, this calibration information is stored
 * in the Calibration and Response classes. Calibration information is used to convert the output of
 * the instrument (e.g., volts, counts) into the phenomenon that the instrument is measuring (e.g.,
 * seismic ground displacement). The Calibration class includes information about when an instrument
 * was calibrated (actual change time) and what the response (calibration factor) was for a
 * particular calibration period (i.e., inverse of frequency). Response includes the full response
 * function across a range of periods/frequencies. The actual change time attribute in both the
 * Calibration and Response classes captures when the calibration was actually performed, and both
 * classes also include system change time as attributes, in order to track when the response
 * information was available for use by the System.
 */
public final class ReferenceResponse {

  private final UUID id;
  private final UUID channelId;
  private final String responseType;
  private final byte[] responseData;
  private final String units;
  private final Instant actualTime;
  private final Instant systemTime;
  private final InformationSource informationSource;
  private final String comment;

  /**
   * Create a new ReferenceResponse.
   *
   * @param responseType The response type.
   * @param responseData The response data.
   * @param units The response's units.
   * @param actualTime The date and time the information was originally generated.
   * @param systemTime The date and time time the information was entered into the system
   * @param informationSource The source of this information.
   * @param comment A comment.
   * @return A new ReferenceResponse object.
   */
  public static ReferenceResponse create(UUID channelId, String responseType,
      byte[] responseData, String units, Instant actualTime, Instant systemTime,
      InformationSource informationSource, String comment) {

    return new ReferenceResponse(channelId, responseType, responseData, units,
        actualTime, systemTime, informationSource, comment);
  }

  /**
   * Create a new ReferenceResponse.
   *
   * @param responseType The response type.
   * @param responseData The response data.
   * @param units The response's units.
   * @param actualTime The date and time the information was originally generated.
   * @param systemTime The date and time the information was entered into the system.
   * @param informationSource The source of this information.
   * @param comment A comment.
   * @return A new ReferenceResponse object.
   */
  public static ReferenceResponse from(UUID id, UUID channelId, String responseType,
      byte[] responseData, String units, Instant actualTime, Instant systemTime,
      InformationSource informationSource, String comment) {
    return new ReferenceResponse(id, channelId, responseType, responseData, units,
        actualTime, systemTime, informationSource, comment);
  }

  private ReferenceResponse(UUID channelId, String responseType, byte[] responseData, String units,
      Instant actualTime, Instant systemTime, InformationSource informationSource, String comment)
      throws NullPointerException, InvalidParameterException {

    this.channelId = Objects.requireNonNull(channelId);
    this.responseType = Objects.requireNonNull(responseType);
    this.responseData = Objects.requireNonNull(responseData);
    this.units = Objects.requireNonNull(units);
    this.actualTime = Objects.requireNonNull(actualTime);
    this.systemTime = Objects.requireNonNull(systemTime);
    this.informationSource = Objects.requireNonNull(informationSource);
    this.comment = Objects.requireNonNull(comment);
    this.id = UUID.nameUUIDFromBytes(
        (this.channelId + responseType + new String(responseData)
        + this.units + this.actualTime + this.systemTime)
            .getBytes(StandardCharsets.UTF_16LE));
  }

  /**
   * Private constructor.
   */
  private ReferenceResponse(UUID id, UUID channelId, String responseType, byte[] responseData,
      String units,
      Instant actualTime, Instant systemTime, InformationSource informationSource, String comment)
      throws NullPointerException, InvalidParameterException {

    this.id = Objects.requireNonNull(id);
    this.channelId = Objects.requireNonNull(channelId);
    this.responseType = Objects.requireNonNull(responseType);
    this.responseData = Objects.requireNonNull(responseData);
    this.units = Objects.requireNonNull(units);
    this.actualTime = Objects.requireNonNull(actualTime);
    this.systemTime = Objects.requireNonNull(systemTime);
    this.informationSource = Objects.requireNonNull(informationSource);
    this.comment = Objects.requireNonNull(comment);
  }

  public UUID getId() {
    return id;
  }

  public UUID getChannelId() {
    return channelId;
  }

  public String getResponseType() {
    return responseType;
  }

  public byte[] getResponseData() {
    return responseData;
  }

  public String getUnits() {
    return units;
  }

  public Instant getActualTime() {
    return actualTime;
  }

  public Instant getSystemTime() {
    return systemTime;
  }

  public InformationSource getInformationSource() {
    return informationSource;
  }

  public String getComment() {
    return comment;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    ReferenceResponse that = (ReferenceResponse) o;

    if (id != null ? !id.equals(that.id) : that.id != null) {
      return false;
    }
    if (channelId != null ? !channelId.equals(that.channelId) : that.channelId != null) {
      return false;
    }
    if (responseType != null ? !responseType.equals(that.responseType)
        : that.responseType != null) {
      return false;
    }
    if (!Arrays.equals(responseData, that.responseData)) {
      return false;
    }
    if (units != null ? !units.equals(that.units) : that.units != null) {
      return false;
    }
    if (actualTime != null ? !actualTime.equals(that.actualTime) : that.actualTime != null) {
      return false;
    }
    if (systemTime != null ? !systemTime.equals(that.systemTime) : that.systemTime != null) {
      return false;
    }
    if (informationSource != null ? !informationSource.equals(that.informationSource)
        : that.informationSource != null) {
      return false;
    }
    return comment != null ? comment.equals(that.comment) : that.comment == null;
  }

  @Override
  public int hashCode() {
    int result = id != null ? id.hashCode() : 0;
    result = 31 * result + (channelId != null ? channelId.hashCode() : 0);
    result = 31 * result + (responseType != null ? responseType.hashCode() : 0);
    result = 31 * result + Arrays.hashCode(responseData);
    result = 31 * result + (units != null ? units.hashCode() : 0);
    result = 31 * result + (actualTime != null ? actualTime.hashCode() : 0);
    result = 31 * result + (systemTime != null ? systemTime.hashCode() : 0);
    result = 31 * result + (informationSource != null ? informationSource.hashCode() : 0);
    result = 31 * result + (comment != null ? comment.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "ReferenceResponse{" +
        "id=" + id +
        ", channelId=" + channelId +
        ", responseType='" + responseType + '\'' +
        ", responseDataLength=" + responseData.length +
        ", units='" + units + '\'' +
        ", actualTime=" + actualTime +
        ", systemTime=" + systemTime +
        ", informationSource=" + informationSource +
        ", comment='" + comment + '\'' +
        '}';
  }
}
