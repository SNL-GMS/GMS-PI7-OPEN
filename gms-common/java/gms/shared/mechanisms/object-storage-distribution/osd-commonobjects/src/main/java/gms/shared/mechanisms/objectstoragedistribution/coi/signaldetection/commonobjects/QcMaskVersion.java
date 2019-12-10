package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.auto.value.AutoValue;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

/**
 * Represents a version of the {@link QcMask} to identify where there are data quality issues, e.g.,
 * missing data, spikes, etc.
 */
@AutoValue
@JsonSerialize(as = QcMaskVersion.class)
@JsonDeserialize(builder = AutoValue_QcMaskVersion.Builder.class)
public abstract class QcMaskVersion {

  public abstract long getVersion();

  public abstract ImmutableList<QcMaskVersionDescriptor> getParentQcMasks();

  public abstract ImmutableList<UUID> getChannelSegmentIds();

  public abstract Optional<QcMaskType> getType();

  public abstract QcMaskCategory getCategory();

  public abstract String getRationale();

  public abstract Optional<Instant> getStartTime();

  public abstract Optional<Instant> getEndTime();

  public boolean isRejected() {
    return QcMaskCategory.REJECTED.equals(getCategory());
  }

  /**
   * Returns true if parentQcMasks does not equal QcMaskVersionDescriptor.noParent.
   *
   * @return Whether or not this version has any parent QcMasks
   */
  public boolean hasParent() {
    return (!getParentQcMasks().isEmpty());
  }

  public static Builder builder() {
    return new AutoValue_QcMaskVersion.Builder();
  }

  public abstract Builder toBuilder();

  @AutoValue.Builder
  @JsonPOJOBuilder(withPrefix = "set")
  public abstract static class Builder {

    public abstract Builder setVersion(long version);

    abstract Builder setParentQcMasks(ImmutableList<QcMaskVersionDescriptor> parentQcMasks);

    public Builder setParentQcMasks(Collection<QcMaskVersionDescriptor> parentQcMasks) {
      return setParentQcMasks(ImmutableList.copyOf(parentQcMasks));
    }

    abstract ImmutableList.Builder<QcMaskVersionDescriptor> parentQcMasksBuilder();

    public Builder addParentQcMask(QcMaskVersionDescriptor parentQcMask) {
      parentQcMasksBuilder().add(parentQcMask);
      return this;
    }

    public Builder addParentQcMask(UUID parentQcMaskId, long parentQcMaskVersion) {
      return addParentQcMask(QcMaskVersionDescriptor.from(parentQcMaskId, parentQcMaskVersion));
    }

    abstract Builder setChannelSegmentIds(ImmutableList<UUID> channelSegmentIds);

    public Builder setChannelSegmentIds(Collection<UUID> channelSegmentIds) {
      return setChannelSegmentIds(ImmutableList.copyOf(channelSegmentIds));
    }

    abstract ImmutableList.Builder<UUID> channelSegmentIdsBuilder();

    public Builder addChannelSegmentId(UUID channelSegmentId) {
      channelSegmentIdsBuilder().add(channelSegmentId);
      return this;
    }

    public abstract Builder setType(QcMaskType type);

    public abstract Builder setCategory(QcMaskCategory category);

    public abstract Builder setRationale(String rationale);

    public abstract Builder setStartTime(Instant startTime);

    public abstract Builder setEndTime(Instant endTime);

    abstract QcMaskVersion autoBuild();

    public QcMaskVersion build() {
      QcMaskVersion qcMaskVersion = autoBuild();

      if (QcMaskCategory.REJECTED.equals(qcMaskVersion.getCategory())) {
        //validate type, start, end were not set
        Preconditions.checkState(!qcMaskVersion.getType().isPresent(),
            "Error building QcMaskVersion: REJECTED versions must not provide a Type");

        Preconditions.checkState(!qcMaskVersion.getStartTime().isPresent(),
            "Error building QcMaskVersion: REJECTED versions must not provide a Start Time");

        Preconditions.checkState(!qcMaskVersion.getEndTime().isPresent(),
            "Error building QcMaskVersion: REJECTED versions must not provide a End Time");
      } else {
        //Preconditions plus Optional.orElseThrow handle both state checks and isPresent checks
        Preconditions
            .checkState(qcMaskVersion.getCategory().isValidType(qcMaskVersion.getType().orElseThrow(
                () -> new IllegalStateException(
                    "Error building QcMaskVersion: non-REJECTED versions must provide a Type"))),
                "Error building QcMaskVersion: Type is not valid for Category");

        Preconditions.checkState(
            !qcMaskVersion.getStartTime().orElseThrow(() -> new IllegalStateException(
                "Error building QcMaskVersion: non-REJECTED versions must provide a Start Time"))
                .isAfter(qcMaskVersion.getEndTime().orElseThrow(() -> new IllegalStateException(
                    "Error building QcMaskVersion: non-REJECTED versions must provide an End Time"))),
            "Error building QcMaskVersion: Start Time must be before or equal to End Time");
      }

      Preconditions.checkState(
          qcMaskVersion.getParentQcMasks().stream().distinct().count() == qcMaskVersion
              .getParentQcMasks().size(),
          "Error building QcMaskVersion: Parent QcMasks cannot contain duplicates");

      return qcMaskVersion;
    }

  }


  /**
   * Default factory method used to create a QcMaskVersion. Primarily used by other factory methods
   * and for serialization.
   *
   * @param version The version Identity of the created QcMaskVersion.
   * @param parentQcMasks The {@link QcMaskVersion} parentQcMasks identifier.
   * @param channelSegmentIds A list of channel segment ids for which this QcMaskVersion applies.
   * @param category Represents the category of {@link QcMask}.
   * @param type The type of {@link QcMask}.
   * @param rationale Any rationale for creating the QcMaskVersion.
   * @param startTime The start time of the QcMaskVersion.
   * @param endTime The end time of the QcMaskVersion.
   * @return The QcMaskVersion representing the input.
   */
  public static QcMaskVersion from(long version, Collection<QcMaskVersionDescriptor> parentQcMasks,
      List<UUID> channelSegmentIds, QcMaskCategory category, QcMaskType type, String rationale,
      Instant startTime, Instant endTime) {

    return builder()
        .setVersion(version)
        .setParentQcMasks(parentQcMasks)
        .setChannelSegmentIds(channelSegmentIds)
        .setCategory(category)
        .setType(type)
        .setRationale(rationale)
        .setStartTime(startTime)
        .setEndTime(endTime)
        .build();
  }

}
