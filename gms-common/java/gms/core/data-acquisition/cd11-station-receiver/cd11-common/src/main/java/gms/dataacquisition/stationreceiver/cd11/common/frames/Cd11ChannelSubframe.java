package gms.dataacquisition.stationreceiver.cd11.common.frames;

import gms.dataacquisition.stationreceiver.cd11.common.FrameUtilities;
import gms.dataacquisition.stationreceiver.cd11.common.enums.CompressionFormat;
import gms.dataacquisition.stationreceiver.cd11.common.enums.DataType;
import gms.dataacquisition.stationreceiver.cd11.common.enums.SensorType;
import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Channel Subframe contains a channel description, the current channel status, and the actual
 * data. The data must be in a designated data type and may be compressed or uncompressed. The data
 * are followed by a data authentication signature. The number of Channel Subframes must match the
 * number specified in the Channel Subframe Header.
 */
public class Cd11ChannelSubframe {


  // See constructor javadoc for description of the fields.
  public final int channelLength;
  public final int authOffset;
  public final boolean authenticationOn;
  public final CompressionFormat compressionFormat;
  public final SensorType sensorType;
  public final boolean isCalib;
  public final String siteName;
  public final String channelName;
  public final String locationName;
  public final DataType dataType;
  public final float calibrationFactor;
  public final float calibrationPeriod;
  public final Instant timeStamp;    // Defined in CD11 spec as 20 byte string, julian date format
  public final int subframeTimeLength;
  public final int samples;
  public final int channelStatusSize;
  public final byte[] channelStatusData;
  public final int dataSize;
  public final byte[] channelData;
  public final int subframeCount;
  public final int authKeyIdentifier;
  public final int authSize;
  public final byte[] authValue;
  public final double sampleRate;
  public final Instant endTime;

  private static final int CHANNEL_DESCRIPTION_LEN = 24;

  private static Logger logger = LoggerFactory.getLogger(Cd11ChannelSubframe.class);

  /**
   * The minimum byte array length of a subframe. This value does not include the following dynamic
   * fields: channelStatusData, channelData or authValue.
   */
  public static final int MINIMUM_FRAME_LENGTH = (Integer.BYTES * 9) +
      CHANNEL_DESCRIPTION_LEN + FrameUtilities.TIMESTAMP_LEN;

  /**
   * The Constructors make ONE SINGLE subframe, not the mutiple subframes contained within the body
   * of a data frame
   *
   * @param subframe A ByteBuffer containing the frame contents.
   */
  public Cd11ChannelSubframe(ByteBuffer subframe)
      throws IllegalArgumentException,
      BufferUnderflowException {

    // Make sure the buffer position is set to the beginning.
    // NOTE: Potential bug here - the byte buffer passed into this constructor by the Cd11DataFrame
    // contains both the header and data payload.  Therefore, rewinding it goes to start of the header.
    // subframe.rewind();

    if (subframe.remaining() < MINIMUM_FRAME_LENGTH) {
      String error = "ChannelSubframe minimum size is " + MINIMUM_FRAME_LENGTH +
          " but byte buffer only contains " + subframe.remaining() + " bytes.";
      logger.error(error);
      throw new BufferUnderflowException();
    }

    // channel length
    this.channelLength = subframe.getInt();
    // authentication offset
    this.authOffset = subframe.getInt();
    // 'Channel description' fields (byte[], parsed into individual fields)
    // byte 1: authentication
    this.authenticationOn = subframe.get() == 1;
    // byte 2: 'transformation', which is the compression format (if any)
    this.compressionFormat = CompressionFormat.of(subframe.get());
    // byte 3: 'sensor type'
    this.sensorType = SensorType.of(subframe.get());
    // byte 4: 'option flag', 1 means calibration
    this.isCalib = subframe.get() == 1;
    // bytes 5-9: site name
    this.siteName = FrameUtilities.readBytesAsString(subframe, 5);
    // bytes 10-12: channel name
    this.channelName = FrameUtilities.readBytesAsString(subframe, 3);
    // bytes 13-14: location name
    this.locationName = FrameUtilities.readBytesAsString(subframe, 2);
    // bytes 15-16: 'uncompressed data format', aka CSS 3.0 Data Type
    String dataTypeName = FrameUtilities.readBytesAsString(subframe, 2);
    this.dataType = DataType.valueOf(dataTypeName);
    // bytes 17-20: calibration factor.  Only meaningful when isCalib = true.
    this.calibrationFactor = subframe.getFloat();
    // bytes 21-24: calibration period.  Only meaningful when isCalib = true.
    this.calibrationPeriod = subframe.getFloat();
    // timestamp
    String timestampString = FrameUtilities
        .readBytesAsString(subframe, FrameUtilities.TIMESTAMP_LEN);
    this.timeStamp = FrameUtilities.jdToInstant(timestampString);
    // subframe time length
    this.subframeTimeLength = subframe.getInt();
    // (number of) samples
    this.samples = subframe.getInt();
    // 'channel status size' (unpadded length in bytes of next field
    this.channelStatusSize = subframe.getInt();
    //Channel Status Size gives us unpadded length. We must
    //pad to make Channel status size % 4 = 0
    this.channelStatusData = new byte[FrameUtilities
        .calculatePaddedLength(channelStatusSize, Integer.BYTES)];
    subframe.get(this.channelStatusData);
    // 'data size' (unpadded length in bytes of next field)
    this.dataSize = subframe.getInt();
    //Channel data must also be padded
    this.channelData = new byte[FrameUtilities.calculatePaddedLength(dataSize, Integer.BYTES)];
    subframe.get(channelData);
    // 'subframe count' (as assigned by digitizers; zero for digitizers that do not support this)
    this.subframeCount = subframe.getInt();
    // authentication key identifier
    this.authKeyIdentifier = subframe.getInt();
    // 'authentication size' (unpadded length in bytes of next field)
    this.authSize = subframe.getInt();
    // authentication value (DSS signature)
    this.authValue = new byte[FrameUtilities.calculatePaddedLength(authSize, Integer.BYTES)];
    subframe.get(this.authValue);
    this.sampleRate = computeSampleRate(samples, subframeTimeLength);
    this.endTime = computeEndTime(this.timeStamp, this.sampleRate, this.subframeTimeLength);

    validate();
  }

  private double computeSampleRate(int samples, int subframeTimeLength) {
    // time length is in milis, need to convert to seconds
    return ((double) samples) / ((double) subframeTimeLength) * 1000.0;
  }

  private Instant computeEndTime(Instant start, double sampleRate, int subframeTimeLength) {
    final int MILLION = 1_000_000, BILLION = MILLION * 1000;
    final double samplePeriodNanos = 1.0 / sampleRate * BILLION;
    // duration of frame is one sample period less than subframeTimeLength because the first
    // sample begins at zero.
    final double subframeLengthNanos = (double) subframeTimeLength * MILLION;
    final double durationNanos = subframeLengthNanos - samplePeriodNanos;
    final Duration frameDuration = Duration.ofNanos(
        (long) durationNanos);
    return start.plus(frameDuration);
  }

  /**
   * Creates data channel subframe with all arguments.
   *
   * @param channelLength length, in bytes and divisible by four of this Channel Subframe, not
   * counting this integer
   * @param authOffset byte offset from the first byte of the frame to the authentication key
   * identifier
   * @param authenticationOn indicates whether authentication is on
   * @param compressionFormat indicates compression format, if any (may be NONE)
   * @param sensorType the type of sensor, e.g. SEISMIC.
   * @param isCalib if true, this data is calibration.
   * @param siteName name of the site
   * @param channelName name of the channel
   * @param locationName name of location
   * @param dataType type of the data
   * @param calibrationFactor calibration factor
   * @param calibrationPeriod calibration period
   * @param timeStamp UTC start time for first sample of this channel
   * @param subframeTimeLength time in milliseconds spanned by this channel data
   * @param samples number of samples in Channel Subframe
   * @param channelStatusSize unpadded length in bytes of next field
   * @param channelStatusData status data for channel, padded to be divisible by four
   * @param dataSize unpadded length in bytes of next field
   * @param channelData data for channel, padded to be divisible by four
   * @param subframeCount subframe count as assigned by digitizer; zero for digitizers that do not
   * support this count
   * @param authKeyIdentifier pointer to the certificate with the public key to be used for
   * verifying the authentication value field
   * @param authSize unpadded length in bytes of next field
   * @param authValue DSS signature over the following fields: channel description, timestamp,
   * subframe time length, samples, channel status size, channel status data, data size, channel
   * data, and subframe count. This field is padded as necessary to be divisible by four.
   */
  public Cd11ChannelSubframe(int channelLength, int authOffset,
      boolean authenticationOn, CompressionFormat compressionFormat,
      SensorType sensorType, boolean isCalib, String siteName,
      String channelName, String locationName, DataType dataType,
      float calibrationFactor, float calibrationPeriod,
      Instant timeStamp, int subframeTimeLength, int samples,
      int channelStatusSize, byte[] channelStatusData, int dataSize,
      byte[] channelData, int subframeCount, int authKeyIdentifier,
      int authSize, byte[] authValue) {

    this.channelLength = channelLength;
    this.authOffset = authOffset;
    this.authenticationOn = authenticationOn;
    this.compressionFormat = compressionFormat;
    this.sensorType = sensorType;
    this.isCalib = isCalib;
    this.siteName = FrameUtilities.stripString(siteName);
    this.channelName = FrameUtilities.stripString(channelName);
    this.locationName = FrameUtilities.stripString(locationName);
    this.dataType = dataType;
    this.calibrationFactor = calibrationFactor;
    this.calibrationPeriod = calibrationPeriod;
    this.timeStamp = timeStamp;
    this.subframeTimeLength = subframeTimeLength;
    this.samples = samples;
    this.channelStatusSize = channelStatusSize;
    this.channelStatusData = channelStatusData;
    this.dataSize = dataSize;
    this.channelData = channelData;
    this.subframeCount = subframeCount;
    this.authKeyIdentifier = authKeyIdentifier;
    this.authSize = authSize;
    this.authValue = authValue;
    this.sampleRate = computeSampleRate(samples, subframeTimeLength);
    this.endTime = computeEndTime(this.timeStamp, this.sampleRate, this.subframeTimeLength);

    validate();
  }


  /**
   * The size of the channel subframe is dynamic because the data length fields are dependent upon
   * the size of the data.
   *
   * @return The size in bytes of the subframe
   */
  public int getSize() {
    return MINIMUM_FRAME_LENGTH + channelStatusData.length
        + channelData.length + authValue.length;
  }

  /**
   * Turns this data subframe into a byte[]
   *
   * @return a byte[] representation of this data subframe
   * @throws IOException if cannot write to byte array output stream, etc.
   */
  public byte[] toBytes() throws IOException {

    ByteBuffer output = ByteBuffer.allocate(getSize());

    output.putInt(channelLength);
    output.putInt(authOffset);
    output.put((byte) (authenticationOn ? 1 : 0));
    output.put(compressionFormat.code);
    output.put(sensorType.code);
    output.put((byte) (isCalib ? 1 : 0));
    output.put(FrameUtilities.padToLength(siteName, 5).getBytes());
    output.put(FrameUtilities.padToLength(channelName, 3).getBytes());
    output.put(FrameUtilities.padToLength(locationName, 2).getBytes());
    output.put(dataType.toString().getBytes());
    output.putFloat(calibrationFactor);
    output.putFloat(calibrationPeriod);
    output.put(FrameUtilities.instantToJd(timeStamp).getBytes());
    output.putInt(subframeTimeLength);
    output.putInt(samples);
    output.putInt(channelStatusSize);
    output.put(channelStatusData);
    output.putInt(dataSize);
    output.put(channelData);
    output.putInt(subframeCount);
    output.putInt(authKeyIdentifier);
    output.putInt(authSize);
    output.put(authValue);

    return output.array();
  }

  /**
   * Gets the 'channel string' for this subframe, which consists of 10 bytes: siteName (5 bytes),
   * channelName (3 bytes), locationName (2 bytes).
   *
   * @return the channel string for this subframe
   */
  public String channelString() {
    // Spec: site name is padded to 5 characters
    String siteName = FrameUtilities.padToLength(this.siteName, 5);
    // Spec: channel name is padded to 3 characters
    String channelName = FrameUtilities.padToLength(this.channelName, 3);
    // Spec: location name is two characteres
    String locationName = FrameUtilities.padToLength(this.locationName, 2);
    String channelString = siteName + channelName + locationName;
    Validate.isTrue(channelString.length() == 10);
    return channelString;
  }

  /**
   * Validates this object. Throws an exception if there are any problems with it's fields.
   */
  private void validate() throws IllegalArgumentException, NullPointerException {

    Validate.isTrue(this.channelLength >= MINIMUM_FRAME_LENGTH,
        "ChannelSubframe.ChannelLength must be > minimum frame length of " +
            MINIMUM_FRAME_LENGTH + ", but value is: " + this.channelLength);

    Validate.isTrue(this.channelLength % 4 == 0,
        "channelLength must be divisible by 4");

    Validate.isTrue(authOffset >= MINIMUM_FRAME_LENGTH,
        "ChannelSubframe.AuthOffset must be > minimum frame length of " +
            MINIMUM_FRAME_LENGTH + ", but value is: " + authOffset);

    Validate.notNull(this.compressionFormat);
    Validate.notNull(this.sensorType);
    Validate.notEmpty(this.siteName);
    Validate.notEmpty(this.channelName);

    // They currently forwards us data with null location names, so we should log the info and move on.
    if (this.locationName == null) {
      logger.debug("Null location name.");
    }

    Validate.notNull(this.dataType);
    Validate.notNull(this.timeStamp);

    Validate.isTrue(subframeTimeLength >= 0,
        "ChannelSubframe.SubframeTimeLength must be >= 0, "
            + "but value is: " + subframeTimeLength);

    Validate.isTrue(samples >= 0,
        "ChannelSubframe.Samples must be >= 0, but value is: " + samples);

    Validate.isTrue(channelStatusSize >= 0,
        "ChannelSubframe.ChannelStatusSize must be >= 0, "
            + "but value is: " + channelStatusSize);

    Validate.isTrue(dataSize >= 0,
        "ChannelSubframe.DataSize must be >= 0, "
            + "but value is: " + dataSize);

    Validate.isTrue(subframeCount >= 0,
        "ChannelSubframe.SubframeCount must be >= 0, "
            + "but value is: " + subframeCount);

    Validate.isTrue(authSize >= 0,
        "ChannelSubframe.AuthSize must be >= 0, "
            + "but value is: " + authSize);

    Validate.notNull(this.authValue);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Cd11ChannelSubframe that = (Cd11ChannelSubframe) o;

    if (channelLength != that.channelLength) {
      return false;
    }
    if (authOffset != that.authOffset) {
      return false;
    }
    if (authenticationOn != that.authenticationOn) {
      return false;
    }
    if (isCalib != that.isCalib) {
      return false;
    }
    if (Float.compare(that.calibrationFactor, calibrationFactor) != 0) {
      return false;
    }
    if (Float.compare(that.calibrationPeriod, calibrationPeriod) != 0) {
      return false;
    }
    if (subframeTimeLength != that.subframeTimeLength) {
      return false;
    }
    if (samples != that.samples) {
      return false;
    }
    if (channelStatusSize != that.channelStatusSize) {
      return false;
    }
    if (dataSize != that.dataSize) {
      return false;
    }
    if (subframeCount != that.subframeCount) {
      return false;
    }
    if (authKeyIdentifier != that.authKeyIdentifier) {
      return false;
    }
    if (authSize != that.authSize) {
      return false;
    }
    if (compressionFormat != that.compressionFormat) {
      return false;
    }
    if (sensorType != that.sensorType) {
      return false;
    }
    if (siteName != null ? !siteName.equals(that.siteName) : that.siteName != null) {
      return false;
    }
    if (channelName != null ? !channelName.equals(that.channelName) : that.channelName != null) {
      return false;
    }
    if (locationName != null ? !locationName.equals(that.locationName)
        : that.locationName != null) {
      return false;
    }
    if (dataType != that.dataType) {
      return false;
    }
    if (timeStamp != null ? !timeStamp.equals(that.timeStamp) : that.timeStamp != null) {
      return false;
    }
    if (!Arrays.equals(channelStatusData, that.channelStatusData)) {
      return false;
    }
    if (!Arrays.equals(channelData, that.channelData)) {
      return false;
    }
    return Arrays.equals(authValue, that.authValue);
  }

  @Override
  public int hashCode() {
    int result = channelLength;
    result = 31 * result + authOffset;
    result = 31 * result + (authenticationOn ? 1 : 0);
    result = 31 * result + (compressionFormat != null ? compressionFormat.hashCode() : 0);
    result = 31 * result + (sensorType != null ? sensorType.hashCode() : 0);
    result = 31 * result + (isCalib ? 1 : 0);
    result = 31 * result + (siteName != null ? siteName.hashCode() : 0);
    result = 31 * result + (channelName != null ? channelName.hashCode() : 0);
    result = 31 * result + (locationName != null ? locationName.hashCode() : 0);
    result = 31 * result + (dataType != null ? dataType.hashCode() : 0);
    result =
        31 * result + (calibrationFactor != +0.0f ? Float.floatToIntBits(calibrationFactor) : 0);
    result =
        31 * result + (calibrationPeriod != +0.0f ? Float.floatToIntBits(calibrationPeriod) : 0);
    result = 31 * result + (timeStamp != null ? timeStamp.hashCode() : 0);
    result = 31 * result + subframeTimeLength;
    result = 31 * result + samples;
    result = 31 * result + channelStatusSize;
    result = 31 * result + Arrays.hashCode(channelStatusData);
    result = 31 * result + dataSize;
    result = 31 * result + Arrays.hashCode(channelData);
    result = 31 * result + subframeCount;
    result = 31 * result + authKeyIdentifier;
    result = 31 * result + authSize;
    result = 31 * result + Arrays.hashCode(authValue);
    return result;
  }

  @Override
  public String toString() {
    StringBuilder out = new StringBuilder("Cd11ChannelSubframe { ");
    out.append("channelLength: ").append(channelLength).append(", ");
    out.append("authOffset: ").append(authOffset).append(", ");
    out.append("authenticationOn: ").append((authenticationOn) ? "true" : "false").append(", ");
    out.append("compressionFormat: ").append(compressionFormat).append(", ");
    out.append("sensorType: ").append(sensorType).append(", ");
    out.append("isCalib: ").append((isCalib) ? "true" : "false").append(", ");
    out.append("siteName: \"").append(siteName).append("\", ");
    out.append("channelName: \"").append(channelName).append("\", ");
    out.append("locationName: \"").append(locationName).append("\", ");
    out.append("dataType: \"").append(dataType).append("\", ");
    out.append("calibrationFactor: ").append(calibrationFactor).append(", ");
    out.append("calibrationPeriod: ").append(calibrationPeriod).append(", ");
    out.append("timeStamp: ").append(timeStamp).append(", ");
    out.append("subframeTimeLength: ").append(subframeTimeLength).append(", ");
    out.append("samples: ").append(samples).append(", ");
    out.append("channelStatusSize: ").append(channelStatusSize).append(", ");
    out.append("channelStatusData: \"").append(Arrays.toString(channelStatusData)).append("\", ");
    out.append("dataSize: ").append(dataSize).append(", ");
    out.append("channelData: \"").append(Arrays.toString(channelData)).append("\", ");
    out.append("subframeCount: ").append(subframeCount).append(", ");
    out.append("authKeyIdentifier: ").append(authKeyIdentifier).append(", ");
    out.append("authSize: ").append(authSize).append(", ");
    out.append("authValue: \"").append(Arrays.toString(authValue)).append("\" ");
    out.append("}");
    return out.toString();
  }
}
