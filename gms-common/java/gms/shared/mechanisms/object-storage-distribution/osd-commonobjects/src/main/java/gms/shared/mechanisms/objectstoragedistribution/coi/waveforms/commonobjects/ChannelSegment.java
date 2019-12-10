package gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects;

import com.google.common.collect.Range;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.CreationInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.utility.TimeseriesUtility;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.apache.commons.lang3.Validate;

/**
 * Represents a segment of  data from a Channel.
 */
public class ChannelSegment<T extends Timeseries> implements Comparable<ChannelSegment<T>> {

  private final UUID id;
  private final UUID channelId;
  private final String name;
  private final Type type;
  private final Timeseries.Type timeseriesType;
  private final Instant startTime;
  private final Instant endTime;
  private final List<T> timeseries;
  private final CreationInfo creationInfo;

  /**
   * The type of the channel segment.
   */
  public enum Type {
    ACQUIRED, RAW, DETECTION_BEAM, FK_BEAM, FK_SPECTRA, FILTER
  }

  /**
   * Creates a ChannelSegment anew.
   *
   * @param channelId the id of the processing channel the segment is from.
   * @param name Name of the ChannelSegment.
   * @param type The type of the ChannelSegment, e.g. Type.RAW.
   * @param series The the data of the ChannelSegment.
   * @param creationInfo metadata about when this object was created and by what/whom.
   * @throws NullPointerException if any arg is null
   * @throws IllegalArgumentException if string arg is empty
   */
  public static <T extends Timeseries> ChannelSegment<T> create(
      UUID channelId, String name, Type type,
      Collection<T> series, CreationInfo creationInfo) {

    return new ChannelSegment<>(UUID.randomUUID(), channelId, name,
        type, series, creationInfo);
  }

  /**
   * Creates a ChannelSegment for a collection of {@link Timeseries}.
   *
   * @param id the identifier for this segment
   * @param channelId the id of the processing channel the segment is from.
   * @param name Name of the ChannelSegment.
   * @param type The type of the ChannelSegment, e.g. Type.RAW.
   * @param series The data of the ChannelSegment.
   * @param creationInfo metadata about when this object was created and by what/whom.
   * @throws NullPointerException if any arg is null
   * @throws IllegalArgumentException if string arg is empty
   */
  public static <T extends Timeseries> ChannelSegment<T> from(
      UUID id, UUID channelId, String name, Type type,
      Collection<T> series, CreationInfo creationInfo) {

    return new ChannelSegment<>(id, channelId, name,
        type, series, creationInfo);
  }

  /**
   * Creates a ChannelSegment from all params.  NOTE: This method is only here to support Jackson's
   * JSON deserialization and the passed in timeseriesType value is not used, but rather this value
   * is derived from the actual class type of the timeseries.
   *
   * @param id the identifier for this segment
   * @param channelId the id of the processing channel the segment is from.
   * @param name Name of the ChannelSegment.
   * @param type The type of the ChannelSegment, e.g. ChannelSegment.Type.RAW.
   * @param series The data of the ChannelSegment.
   * @param timeseriesType The type of the timeseries data
   * @param creationInfo metadata about when this object was created and by what/whom.
   * @throws NullPointerException if any arg is null
   * @throws IllegalArgumentException if string arg is empty
   */
  public static <T extends Timeseries> ChannelSegment<T> from(
      UUID id, UUID channelId, String name, ChannelSegment.Type type,
      Timeseries.Type timeseriesType, Collection<T> series, CreationInfo creationInfo) {

    return new ChannelSegment<>(id, channelId, name,
        type, series, creationInfo);
  }

  /**
   * Creates a ChannelSegment.
   *
   * @param id The UUID assigned to this object.
   * @param channelId the id of the processing channel the segment is from.
   * @param name Name of the ChannelSegment.
   * @param type The type of the ChannelSegment, e.g. Type.RAW.
   * @param timeseries The data of the ChannelSegment.
   * @param creationInfo metadata about when this object was created and by what/whom.
   * @throws NullPointerException if any arg is null
   * @throws IllegalArgumentException if string arg is empty
   */
  private ChannelSegment(UUID id, UUID channelId, String name,
      Type type, Collection<T> timeseries, CreationInfo creationInfo) {

    Validate.notBlank(name, "ChannelSegment requires a non-blank name");
    Validate.notEmpty(timeseries, "ChannelSegment requires at least one timeseries");
    this.id = Objects.requireNonNull(id);
    this.channelId = Objects.requireNonNull(channelId);
    this.name = Objects.requireNonNull(name);
    this.type = Objects.requireNonNull(type);
    List<T> sortedSeries = new ArrayList<>(timeseries);
    Collections.sort(sortedSeries);
    Validate.isTrue(TimeseriesUtility.noneOverlapped(sortedSeries),
        "ChannelSegment cannot have overlapping timeseries");
    this.timeseries = Collections.unmodifiableList(sortedSeries);
    final Range<Instant> timeRange = TimeseriesUtility.computeSpan(this.timeseries);
    this.startTime = timeRange.lowerEndpoint();
    this.endTime = timeRange.upperEndpoint();
    this.creationInfo = Objects.requireNonNull(creationInfo);

    timeseriesType = timeseries.iterator().next().getType();
    Validate.notNull(timeseriesType, "Unsupported Timeseries type: "
        + timeseries.iterator().next().getClass());
  }

  /**
   * Gets the id
   *
   * @return the id
   */
  public UUID getId() {
    return id;
  }

  /**
   * Gets the processing channel id
   *
   * @return the id
   */
  public UUID getChannelId() {
    return this.channelId;
  }

  /**
   * Gets the name of this segment
   *
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * Gets the type of this segment
   *
   * @return the type of the segment
   */
  public Type getType() {
    return type;
  }

  /**
   * Gets the type of this segment
   *
   * @return the type of the segment
   */
  public Timeseries.Type getTimeseriesType() {
    return timeseriesType;
  }

  /**
   * Gets the start time of the segment
   *
   * @return the start time
   */
  public Instant getStartTime() {
    return startTime;
  }

  /**
   * Gets the end time of this segment
   *
   * @return the end time
   */
  public Instant getEndTime() {
    return endTime;
  }

  /**
   * Gets the timeseries that this segment contains. The returned list is sorted and immutable.
   *
   * @return {@link List} of T, not null
   */
  public List<T> getTimeseries() {
    return timeseries;
  }

  /**
   * Gets the creation info of this segment
   *
   * @return the creation info
   */
  public CreationInfo getCreationInfo() {
    return creationInfo;
  }

  /**
   * Compares the state of this object against another.
   *
   * @param otherSegment the object to compare against
   * @return true if this object and the provided one have the same state, i.e. their values are
   * equal except for entity ID.  False otherwise.
   */
  public final boolean hasSameState(ChannelSegment otherSegment) {
    return otherSegment != null &&
        Objects.equals(this.getChannelId(), otherSegment.getChannelId()) &&
        Objects.equals(this.getName(), otherSegment.getName()) &&
        Objects.equals(this.getType(), otherSegment.getType()) &&
        Objects.equals(this.getTimeseriesType(), otherSegment.getTimeseriesType()) &&
        Objects.equals(this.getStartTime(), otherSegment.getStartTime()) &&
        Objects.equals(this.getEndTime(), otherSegment.getEndTime()) &&
        Objects.equals(this.getTimeseries(), otherSegment.getTimeseries()) &&
        Objects.equals(this.getCreationInfo(), otherSegment.getCreationInfo());
  }

  @Override
  public final boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    ChannelSegment<?> that = (ChannelSegment<?>) o;

    return Objects.equals(this.id, that.id) &&
        hasSameState(that);
  }

  @Override
  public final int hashCode() {
    int result = id != null ? id.hashCode() : 0;
    result = 31 * result + (channelId != null ? channelId.hashCode() : 0);
    result = 31 * result + (name != null ? name.hashCode() : 0);
    result = 31 * result + (type != null ? type.hashCode() : 0);
    result = 31 * result + (timeseriesType != null ? timeseriesType.hashCode() : 0);
    result = 31 * result + (startTime != null ? startTime.hashCode() : 0);
    result = 31 * result + (endTime != null ? endTime.hashCode() : 0);
    result = 31 * result + (timeseries != null ? timeseries.hashCode() : 0);
    result = 31 * result + (creationInfo != null ? creationInfo.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "ChannelSegment{" +
        "id=" + id +
        ", channelId=" + channelId +
        ", name='" + name + '\'' +
        ", type=" + type +
        ", timeseriesType=" + timeseriesType +
        ", startTime=" + startTime +
        ", endTime=" + endTime +
        ", timeseries=" + timeseries +
        ", creationInfo=" + creationInfo +
        '}';
  }

  /**
   * Compares two ChannelSegments by their start time. However, if their times are equal than 1 is
   * returned. This is done to avoid weird behavior in collections such as SortedSet.
   *
   * @param cs the segment to compare this one to
   * @return int
   */
  @Override
  public int compareTo(ChannelSegment<T> cs) {
    if (!getStartTime().equals(cs.getStartTime())) {
      return getStartTime().compareTo(cs.getStartTime());
    }
    if (!getEndTime().equals(cs.getEndTime())) {
      return getEndTime().compareTo(cs.getEndTime());
    }
    return this.equals(cs) ? 0 : 1;
  }
}
