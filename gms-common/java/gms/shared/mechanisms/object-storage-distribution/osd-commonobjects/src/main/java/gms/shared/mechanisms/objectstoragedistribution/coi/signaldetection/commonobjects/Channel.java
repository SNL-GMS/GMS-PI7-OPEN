package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects;

import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ChannelDataType;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ChannelType;
import java.util.Objects;
import java.util.UUID;
import org.apache.commons.lang3.Validate;


/**
 * Represents a limited set of channel information used during the acquisition and
 * processing of data streams.
 */
public final class Channel {

  private final UUID id;
  private final String name;
  private final ChannelType channelType;
  private final ChannelDataType dataType;
  private final double latitude;
  private final double longitude;
  private final double elevation;
  private final double depth;
  private final double verticalAngle;
  private final double horizontalAngle;
  private final double sampleRate;

  /**
   * Create an instance of the class.
   *
   * @param name The name of the channel, not empty or null.
   * @param channelType the type of the channel
   * @return a ProcessingChannel
   * @throws NullPointerException if any arg is null
   * @throws IllegalArgumentException if string arg is empty
   */
  public static Channel create(String name,
      ChannelType channelType, ChannelDataType dataType,
      double latitude, double longitude, double elevation, double depth,
      double verticalAngle, double horizontalAngle, double sampleRate) {

    return new Channel(UUID.randomUUID(), name, channelType, dataType,
        latitude, longitude, elevation, depth, verticalAngle, horizontalAngle,
        sampleRate);
  }

  /**
   * Recreates a ProcessingChannel given all params
   *
   * @param id the id of the channel
   * @param name The name of the channel, not empty or null.
   * @param channelType the type of the channel
   * @return a ProcessingChannel
   * @throws NullPointerException if any arg is null
   * @throws IllegalArgumentException if string arg is empty
   */
  public static Channel from(UUID id, String name,
      ChannelType channelType, ChannelDataType dataType,
      double latitude, double longitude, double elevation, double depth,
      double verticalAngle, double horizontalAngle, double sampleRate) {
    return new Channel(id, name, channelType, dataType,
        latitude, longitude, elevation, depth, verticalAngle, horizontalAngle,
        sampleRate);
  }

  /**
   * Create an instance of the class.
   *
   * @param id the identifier for this ProcessingChannel
   * @param name The name of the channel, not empty or null.
   * @param channelType the type of the channel
   * @throws NullPointerException if any arg is null
   * @throws IllegalArgumentException if string arg is empty
   */
  private Channel(UUID id, String name,
      ChannelType channelType, ChannelDataType dataType,
      double latitude, double longitude, double elevation, double depth,
      double verticalAngle, double horizontalAngle, double sampleRate) {

    Validate.notBlank(name);
    this.id = Objects.requireNonNull(id);
    this.name = Objects.requireNonNull(name);
    this.channelType = Objects.requireNonNull(channelType);
    this.dataType = Objects.requireNonNull(dataType);
    this.latitude = latitude;
    this.longitude = longitude;
    this.elevation = elevation;
    this.depth = depth;
    this.verticalAngle = verticalAngle;
    this.horizontalAngle = horizontalAngle;
    this.sampleRate = sampleRate;
  }

  public UUID getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public ChannelType getChannelType() {
    return channelType;
  }

  public ChannelDataType getDataType() {
    return dataType;
  }

  public double getLatitude() {
    return latitude;
  }

  public double getLongitude() {
    return longitude;
  }

  public double getElevation() {
    return elevation;
  }

  public double getDepth() {
    return depth;
  }

  public double getVerticalAngle() {
    return verticalAngle;
  }

  public double getHorizontalAngle() {
    return horizontalAngle;
  }

  public double getSampleRate() {
    return sampleRate;
  }

  @Override
  public String toString() {
    return "Channel{" +
        "id=" + id +
        ", name='" + name + "'" +
        ", channelType=" + channelType +
        ", dataType=" + dataType +
        ", latitude=" + latitude +
        ", longitude=" + longitude +
        ", elevation=" + elevation +
        ", depth=" + depth +
        ", verticalAngle=" + verticalAngle +
        ", horizontalAngle=" + horizontalAngle +
        ", sampleRate=" + sampleRate +
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
    Channel channel = (Channel) o;
    return Double.compare(channel.latitude, latitude) == 0 &&
        Double.compare(channel.longitude, longitude) == 0 &&
        Double.compare(channel.elevation, elevation) == 0 &&
        Double.compare(channel.depth, depth) == 0 &&
        Double.compare(channel.verticalAngle, verticalAngle) == 0 &&
        Double.compare(channel.horizontalAngle, horizontalAngle) == 0 &&
        Double.compare(channel.sampleRate, sampleRate) == 0 &&
        Objects.equals(id, channel.id) &&
        Objects.equals(name, channel.name) &&
        channelType == channel.channelType &&
        dataType == channel.dataType;
  }

  @Override
  public int hashCode() {
    return Objects
        .hash(id, name, channelType, dataType, latitude, longitude, elevation, depth, verticalAngle,
            horizontalAngle, sampleRate);
  }
}
