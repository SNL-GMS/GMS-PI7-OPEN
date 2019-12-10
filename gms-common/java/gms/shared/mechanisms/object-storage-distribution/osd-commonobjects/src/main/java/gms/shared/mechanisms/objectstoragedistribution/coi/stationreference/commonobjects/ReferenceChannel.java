package gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.auto.value.AutoValue;
import com.google.auto.value.extension.memoized.Memoized;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.InformationSource;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.apache.commons.lang3.Validate;


/**
 * Define a class to represent an instrument channel. A Channel is an identifier for a data stream
 * from a sensor measuring a particular aspect of some physical phenomenon (e.g., ground motion or
 * air pressure). A Channel has metadata, such as a name (e.g., "BHZ" is broadband ground motion in
 * the vertical direction), on time and off time, and a channel type that encodes the type of data
 * recorded by that sensor. There are different conventions for Channel naming, so a Channel can
 * have Aliases. The Channel class also includes information about how the sensor was placed and
 * oriented: depth (relative to the elevation of the associated Site), horizontal angle, and
 * vertical angle.
 */
@AutoValue
@JsonSerialize(as = ReferenceChannel.class)
@JsonDeserialize(builder = AutoValue_ReferenceChannel.Builder.class)
public abstract class ReferenceChannel {

  public abstract String getName();
  public abstract ChannelType getType();
  public abstract ChannelDataType getDataType();
  public abstract String getLocationCode();
  public abstract double getLatitude();
  public abstract double getLongitude();
  public abstract double getElevation();
  public abstract double getDepth();
  public abstract double getVerticalAngle();
  public abstract double getHorizontalAngle();
  public abstract double getNominalSampleRate();
  public abstract Instant getActualTime();
  public abstract Instant getSystemTime();
  public abstract InformationSource getInformationSource();
  public abstract String getComment();
  public abstract RelativePosition getPosition();
  public abstract List<ReferenceAlias> getAliases();
  
  /**
   * Sets defaults, for non required fields. Properties
   * not listed here are required and if not provided
   * build() will throw an IllegalStateException
   * @return ReferenceChannel
   */
  public static Builder builder() {
    return new AutoValue_ReferenceChannel.Builder()
        .setInformationSource(InformationSource.create(
            "UNKNOWN", Instant.EPOCH, "UNKNOWN"))
        .setComment("")
        .setSystemTime(Instant.EPOCH)
        .setPosition(RelativePosition.from(0, 0, 0))
        .setAliases(List.of());
  }

  public abstract Builder toBuilder();

  /**
   * Create a new ReferenceChannel.
   *
   * @param name The name for the channel, must be unique, and not empty.
   * @param type The channel type.
   * @param dataType The channel data type.
   * @param locationCode The channel's location code.
   * @param latitude The channel's latitude.
   * @param longitude The channel's longitude.
   * @param elevation The channel's elevation.
   * @param depth The channel's depth
   * @param verticalAngle The channel's vertical orientation.
   * @param horizontalAngle The channel's horizontal orientation.
   * @param nominalSampleRate The channel's nominal sample rate.
   * @param actualTime The date and time the information was originally generated.
   * @param informationSource The source of this information.
   * @param comment A comment.
   * @param position This channel's relative position.
   * @param aliases The list of this channel's aliases.
   * @return A new ReferenceChannel object.
   */
  public static ReferenceChannel create(String name, ChannelType type,
      ChannelDataType dataType, String locationCode,
      double latitude, double longitude, double elevation, double depth,
      double verticalAngle, double horizontalAngle, double nominalSampleRate,
      Instant actualTime, Instant systemTime,
      InformationSource informationSource, String comment,
      RelativePosition position, List<ReferenceAlias> aliases) {

    Validate.notEmpty(name);

    return new AutoValue_ReferenceChannel(
        name.trim(),
        Objects.requireNonNull(type),
        Objects.requireNonNull(dataType),
        Objects.requireNonNull(locationCode),
        latitude,
        longitude,
        elevation,
        depth,
        verticalAngle,
        horizontalAngle,
        nominalSampleRate,
        Objects.requireNonNull(actualTime),
        Objects.requireNonNull(systemTime),
        Objects.requireNonNull(informationSource),
        Objects.requireNonNull(comment),
        Objects.requireNonNull(position),
        Objects.requireNonNull(aliases));
  }


  @Memoized
  public UUID getEntityId() {
    return UUID.nameUUIDFromBytes((this.getName())
        .getBytes(StandardCharsets.UTF_16LE));
  }

  @Memoized
  public UUID getVersionId() {
    return UUID.nameUUIDFromBytes(
        (this.getName() + this.getType() + this.getDataType() + this.getLocationCode()
            + this.getLatitude() + this.getLongitude() + this.getElevation()
            + this.getDepth() + this.getVerticalAngle() + this.getHorizontalAngle()
            + this.getNominalSampleRate() + this.getActualTime())
            .getBytes(StandardCharsets.UTF_16LE));
  }

  @AutoValue.Builder
  @JsonPOJOBuilder(withPrefix = "set")
  public abstract static class Builder {

    public abstract Builder setName(String name);

    abstract String getName();

    public abstract Builder setType(ChannelType type);

    public abstract Builder setDataType(ChannelDataType dataType);

    public abstract Builder setLocationCode(String locationCode);

    public abstract Builder setLatitude(double latitude);

    public abstract Builder setLongitude(double longitude);

    public abstract Builder setElevation(double elevation);
    
    public abstract Builder setDepth(double depth);

    public abstract Builder setVerticalAngle(double verticalAngle);

    public abstract Builder setHorizontalAngle(double horizontalAngle);

    public abstract Builder setNominalSampleRate(double nominalSampleRate);

    public abstract Builder setActualTime(Instant actualTime);

    public abstract Builder setSystemTime(Instant systemTime);

    public abstract Builder setInformationSource(InformationSource informationSource);

    public abstract Builder setComment(String comment);

    public abstract Builder setPosition(RelativePosition position);
    
    public abstract Builder setAliases(List<ReferenceAlias> aliases);

    abstract ReferenceChannel autoBuild();

    public ReferenceChannel build() {
      setName(getName().trim());
      return autoBuild();
    }
  }
}
