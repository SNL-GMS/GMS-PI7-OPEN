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
 * Define a class to represent a GMS remote monitoring station.
 */
@AutoValue
@JsonSerialize(as = ReferenceStation.class)
@JsonDeserialize(builder = AutoValue_ReferenceStation.Builder.class)
public abstract class ReferenceStation {

  public abstract String getName();

  public abstract String getDescription();

  public abstract StationType getStationType();

  public abstract InformationSource getSource();

  public abstract String getComment();

  public abstract double getLatitude();

  public abstract double getLongitude();

  public abstract double getElevation();

  public abstract Instant getActualChangeTime();

  public abstract Instant getSystemChangeTime();

  public abstract List<ReferenceAlias> getAliases();

  /**
   * Sets defaults, for non required fields. Properties
   * not listed here are required and if not provided
   * build() will throw an IllegalStateException
   * @return ReferenceStation
   */
  public static Builder builder() {
    return new AutoValue_ReferenceStation.Builder()
        .setDescription("")
        .setSource(InformationSource.create(
            "UNKNOWN", Instant.EPOCH, "UNKNOWN"))
        .setComment("")
        .setSystemChangeTime(Instant.EPOCH)
        .setAliases(List.of());
  }

  public abstract Builder toBuilder();

  /**
   * Create a new ReferenceStation.
   *
   * @param name The name for the station, must be unique, and not empty.
   * @param stationType The station type.
   * @param source The source of this information.
   * @param comment A comment.
   * @param latitude The station's latitude.
   * @param longitude The station's longitude.
   * @param elevation The station's elevation.
   * @param actualTime The date and time the information was originally generated.
   * @param systemTime The date and time time the information was entered into the system
   * @param aliases the aliases of the station
   * @return A new ReferenceStation object.
   */
  public static ReferenceStation create(String name, String description, StationType stationType,
      InformationSource source, String comment,
      double latitude, double longitude, double elevation,
      Instant actualTime, Instant systemTime, List<ReferenceAlias> aliases) {

    Validate.notEmpty(name);

    return new AutoValue_ReferenceStation(
        name.trim(),
        Objects.requireNonNull(description),
        Objects.requireNonNull(stationType),
        source,
        Objects.requireNonNull(comment),
        latitude,
        longitude,
        elevation,
        Objects.requireNonNull(actualTime),
        Objects.requireNonNull(systemTime),
        Objects.requireNonNull(aliases));
  }


  @Memoized
  public UUID getEntityId() {
    return UUID.nameUUIDFromBytes(this.getName().getBytes(StandardCharsets.UTF_16LE));
  }

  @Memoized
  public UUID getVersionId() {
    return UUID.nameUUIDFromBytes((this.getName() + this.getStationType()
        + this.getLatitude() + this.getLongitude() + this.getElevation()
        + this.getActualChangeTime()).getBytes(StandardCharsets.UTF_16LE));
  }

  @AutoValue.Builder
  @JsonPOJOBuilder(withPrefix = "set")
  public abstract static class Builder {

    public abstract Builder setName(String name);

    abstract String getName();

    public abstract Builder setDescription(String description);

    public abstract Builder setStationType(StationType stationType);

    public abstract Builder setSource(InformationSource source);

    public abstract Builder setComment(String comment);

    public abstract Builder setLatitude(double latitude);

    public abstract Builder setLongitude(double longitude);

    public abstract Builder setElevation(double elevation);

    public abstract Builder setActualChangeTime(Instant actualChangeTime);

    public abstract Builder setSystemChangeTime(Instant systemChangeTime);

    public abstract Builder setAliases(List<ReferenceAlias> aliases);

    abstract ReferenceStation autoBuild();

    public ReferenceStation build() {
      setName(getName().trim());
      return autoBuild();
    }
  }
}


