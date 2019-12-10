package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects;

import java.time.Instant;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;


public final class ChannelProcessingGroup {

  private final UUID id;
  private final ChannelProcessingGroupType type;
  private final Set<UUID> channelIds;
  private final Instant actualChangeTime;
  private final Instant systemChangeTime;
  private final String status;
  private final String comment;

  public static ChannelProcessingGroup create(
      ChannelProcessingGroupType type, Set<UUID> channelIds, Instant actualChangeTime,
      Instant systemChangeTime, String status, String comment) {

    Objects.requireNonNull(type,
        "ChannelProcessingGroup expects non-null channel processing type.");
    Objects.requireNonNull(channelIds,
        "ChannelProcessingGroup expects non-null set of channel ids.");
    Objects.requireNonNull(actualChangeTime,
        "ChannelProcessingGroup expects non-null actual change time.");
    Objects.requireNonNull(systemChangeTime,
        "ChannelProcessingGroup expects non-null system change time.");
    Objects.requireNonNull(status, "ChannelProcessingGroup expects non-null status.");
    Objects.requireNonNull(comment, "ChannelProcessingGroup expects non-null comment.");

    return new ChannelProcessingGroup(UUID.randomUUID(), type, channelIds,
        actualChangeTime, systemChangeTime, status, comment);
  }

  public static ChannelProcessingGroup from(UUID id,
      ChannelProcessingGroupType type, Set<UUID> channelIds, Instant actualChangeTime,
      Instant systemChangeTime, String status, String comment) {

    Objects.requireNonNull(id,
        "ChannelProcessingGroup expects non-null channel processing id.");
    Objects.requireNonNull(type,
        "ChannelProcessingGroup expects non-null channel processing type.");
    Objects.requireNonNull(channelIds,
        "ChannelProcessingGroup expects non-null set of channel ids.");
    Objects.requireNonNull(actualChangeTime,
        "ChannelProcessingGroup expects non-null actual change time.");
    Objects.requireNonNull(systemChangeTime,
        "ChannelProcessingGroup expects non-null system change time.");
    Objects.requireNonNull(status,
        "ChannelProcessingGroup expects non-null status.");
    Objects.requireNonNull(comment,
        "ChannelProcessingGroup expects non-null comment.");

    return new ChannelProcessingGroup(id, type, channelIds,
        actualChangeTime, systemChangeTime, status, comment);
  }

  private ChannelProcessingGroup(UUID id,
      ChannelProcessingGroupType type, Set<UUID> channelIds, Instant actualChangeTime,
      Instant systemChangeTime, String status, String comment) {
    this.id = id;
    this.type = type;
    this.channelIds = channelIds;
    this.actualChangeTime = actualChangeTime;
    this.systemChangeTime = systemChangeTime;
    this.status = status;
    this.comment = comment;
  }

  public UUID getId() {
    return id;
  }

  public ChannelProcessingGroupType getType() {
    return type;
  }

  public Set<UUID> getChannelIds() {
    return channelIds;
  }

  public Instant getActualChangeTime() {
    return actualChangeTime;
  }

  public Instant getSystemChangeTime() {
    return systemChangeTime;
  }

  public String getStatus() {
    return status;
  }

  public String getComment() {
    return comment;
  }

  @Override
  public String toString() {
    return "ChannelProcessingGroup{" +
        "id=" + id +
        ", type=" + type +
        ", channelIds=" + channelIds +
        ", actualChangeTime=" + actualChangeTime +
        ", systemChangeTime=" + systemChangeTime +
        ", status='" + status + "'" +
        ", comment='" + comment + "'" +
        '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ChannelProcessingGroup that = (ChannelProcessingGroup) o;
    return Objects.equals(id, that.id) &&
        type == that.type &&
        Objects.equals(channelIds, that.channelIds) &&
        Objects.equals(actualChangeTime, that.actualChangeTime) &&
        Objects.equals(systemChangeTime, that.systemChangeTime) &&
        Objects.equals(status, that.status) &&
        Objects.equals(comment, that.comment);
  }

  @Override
  public int hashCode() {

    return Objects
        .hash(id, type, channelIds, actualChangeTime, systemChangeTime, status, comment);
  }
}
