package gms.dataacquisition.stationreceiver.cd11.dataframeparser;

import gms.dataacquisition.stationreceiver.cd11.common.enums.CompressionFormat;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11ByteFrame;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11ChannelSubframe;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11DataFrame;
import gms.dataacquisition.stationreceiver.osdclient.StationReceiverOsdClientInterface;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.CreationInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.SoftwareComponentInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.AcquiredChannelSoh;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.AcquiredChannelSoh.AcquiredChannelSohType;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.AcquiredChannelSohAnalog;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.AcquiredChannelSohBoolean;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.RawStationDataFrame;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import gms.utilities.waveformreader.WaveformReader;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Cd11RawStationDataFrameReader {

  private static Logger logger = LoggerFactory.getLogger(Cd11RawStationDataFrameReader.class);

  private static final String CREATOR_NAME = Cd11RawStationDataFrameReader.class.getSimpleName();

  /**
   * Takes a RawStationDataFrame, converts it to a CD1.1 Data Frame for easy parsing,
   * and parses out the channel segments and state of health data.
   *
   * @param rsdf the RawStationDataFrame to be parsed for channel segment data
   * @param osdClient OSD client
   * @return a pair of Channel segments and SOH's
   */
  public static Pair<List<ChannelSegment<Waveform>>, List<AcquiredChannelSoh>> read(
      RawStationDataFrame rsdf, StationReceiverOsdClientInterface osdClient) throws Exception {
    Validate.notNull(rsdf);
    Validate.notNull(osdClient);

    ByteArrayInputStream input = new ByteArrayInputStream(rsdf.getRawPayload());

    DataInputStream rawPayloadInputStream = new DataInputStream(input);

    Cd11ByteFrame bf = new Cd11ByteFrame(rawPayloadInputStream, () -> false);
    Cd11DataFrame df = new Cd11DataFrame(bf);

    List<ChannelSegment<Waveform>> channelSegments = new ArrayList<>();
    List<AcquiredChannelSoh> statesOfHealth = new ArrayList<>();
    for (int i = 0; i < df.channelSubframes.length; i++) {
      Cd11ChannelSubframe sf = df.channelSubframes[i];

      //Retrieve ChanId, only process subframes where we can find the channel ID
      Optional<UUID> chanIdOptional = osdClient.getChannelId(sf.siteName, sf.channelName);
      if (chanIdOptional.isPresent()) {

        //Calculate sample rate
        double samples = sf.samples;

        //Grab channel data, call waveform reader, which returns and int[] so convert it to double[]
        InputStream waveformData = new ByteArrayInputStream(sf.channelData);
        double[] waveformValues;
        //No Compression, use what is in data type field
        if (sf.compressionFormat == CompressionFormat.NONE) {
          waveformValues = WaveformReader.readSamples(waveformData, sf.dataType.name(),(int)samples,0);
        }
        //Canadian Compression, ignore data type field
        else if (sf.compressionFormat == CompressionFormat.CANADIAN_BEFORE_SIGNATURE
            || sf.compressionFormat == CompressionFormat.CANADIAN_AFTER_SIGNATURE) {
          waveformValues = WaveformReader.readSamples(waveformData, "cc", (int)samples,0);
        } else {
          throw new Exception("Unsupported compression format: " + sf.compressionFormat);
        }

        Waveform waveform = Waveform
            .withValues(sf.timeStamp, sf.sampleRate, waveformValues);
        TreeSet<Waveform> wfs = new TreeSet<>(Set.of(waveform));

        String channelSegmentName = String.format("%s/%s %s", sf.siteName, sf.channelName,
            ChannelSegment.Type.ACQUIRED);

        UUID chanId = chanIdOptional.get();
        channelSegments.add(ChannelSegment.create(chanId, channelSegmentName,
            ChannelSegment.Type.ACQUIRED, wfs, new CreationInfo(CREATOR_NAME, SoftwareComponentInfo.DEFAULT)));

        // Parse the  channel status bits and then save to the OSD.
        statesOfHealth.addAll(toChannelStatusList(chanId, sf.channelStatusData,
            sf.timeStamp, sf.endTime, new CreationInfo(CREATOR_NAME, SoftwareComponentInfo.DEFAULT)));
      } else {
        logger.error(String.format(
            "Could not find channel ID by site: %s, channel name: %s, time: %s.",
            sf.siteName,
            sf.channelName, sf.timeStamp));
      }
    }
    return Pair.of(channelSegments, statesOfHealth);
  }

    /**
     * Parse the channel status fields (SOH) and create a set of AcquiredChannelSoh objects.
     *
     * @param chanId The channel UUID to associate the SOH with.
     * @param fields A byte array containing the status bytes.
     * @param startTime The start time of when this data was generated.
     * @param endTime The end time of when this data was generated.
     * @return A set of AcquiredChannelSoh objects.  It may be an empty set.
     */
    private static List<AcquiredChannelSoh> toChannelStatusList(UUID chanId, byte[] fields,
        Instant startTime, Instant endTime, CreationInfo ci) throws Exception {

      List<AcquiredChannelSoh> stateOfHealthSet = new ArrayList<>();
      int idx = 0;

      if (fields.length < 32) {
        throw new IllegalArgumentException("Expected CD1.1 status fields to contain 32 bytes, "
            + "but found " + fields.length);
      }

      // If the first byte is equal to one, then the CD1.1 status format is expected.
      if (fields[idx] == 1) {

        // Data status byte
        idx = 1;
        stateOfHealthSet.add(AcquiredChannelSohBoolean.create(chanId,
            AcquiredChannelSohType.DEAD_SENSOR_CHANNEL,
            startTime, endTime, isSet(fields[idx], 0), ci));

        stateOfHealthSet.add(AcquiredChannelSohBoolean.create(chanId,
            AcquiredChannelSohType.ZEROED_DATA,
            startTime, endTime, isSet(fields[idx], 1), ci));

        stateOfHealthSet.add(AcquiredChannelSohBoolean.create(chanId,
            AcquiredChannelSohType.CLIPPED,
            startTime, endTime, isSet(fields[idx], 2), ci));

        stateOfHealthSet.add(AcquiredChannelSohBoolean.create(chanId,
            AcquiredChannelSohType.CALIBRATION_UNDERWAY,
            startTime, endTime, isSet(fields[idx], 3), ci));

        // Channel security byte
        idx = 2;
        stateOfHealthSet.add(AcquiredChannelSohBoolean.create(chanId,
            AcquiredChannelSohType.EQUIPMENT_HOUSING_OPEN,
            startTime, endTime, isSet(fields[idx], 0), ci));

        stateOfHealthSet.add(AcquiredChannelSohBoolean.create(chanId,
            AcquiredChannelSohType.DIGITIZING_EQUIPMENT_OPEN,
            startTime, endTime, isSet(fields[idx], 1), ci));

        stateOfHealthSet.add(AcquiredChannelSohBoolean.create(chanId,
            AcquiredChannelSohType.VAULT_DOOR_OPENED,
            startTime, endTime, isSet(fields[idx], 2), ci));

        stateOfHealthSet.add(AcquiredChannelSohBoolean.create(chanId,
            AcquiredChannelSohType.AUTHENTICATION_SEAL_BROKEN,
            startTime, endTime, isSet(fields[idx], 3), ci));

        stateOfHealthSet.add(AcquiredChannelSohBoolean.create(chanId,
            AcquiredChannelSohType.EQUIPMENT_MOVED,
            startTime, endTime, isSet(fields[idx], 4), ci));

        // Miscellaneous status byte
        idx = 3;
        stateOfHealthSet.add(AcquiredChannelSohBoolean.create(chanId,
            AcquiredChannelSohType.CLOCK_DIFFERENTIAL_TOO_LARGE,
            startTime, endTime, isSet(fields[idx], 0), ci));

        stateOfHealthSet.add(AcquiredChannelSohBoolean.create(chanId,
            AcquiredChannelSohType.GPS_RECEIVER_OFF,
            startTime, endTime, isSet(fields[idx], 1), ci));

        stateOfHealthSet.add(AcquiredChannelSohBoolean.create(chanId,
            AcquiredChannelSohType.GPS_RECEIVER_UNLOCKED,
            startTime, endTime, isSet(fields[idx], 2), ci));

        stateOfHealthSet.add(AcquiredChannelSohBoolean.create(chanId,
            AcquiredChannelSohType.DIGITIZER_ANALOG_INPUT_SHORTED,
            startTime, endTime, isSet(fields[idx], 3), ci));

        stateOfHealthSet.add(AcquiredChannelSohBoolean.create(chanId,
            AcquiredChannelSohType.DIGITIZER_CALIBRATION_LOOP_BACK,
            startTime, endTime, isSet(fields[idx], 4), ci));

        // Voltage indicator byte
        idx = 4;
        stateOfHealthSet.add(AcquiredChannelSohBoolean.create(chanId,
            AcquiredChannelSohType.MAIN_POWER_FAILURE,
            startTime, endTime, isSet(fields[idx], 0), ci));

        stateOfHealthSet.add(AcquiredChannelSohBoolean.create(chanId,
            AcquiredChannelSohType.BACKUP_POWER_UNSTABLE,
            startTime, endTime, isSet(fields[idx], 1), ci));

        // Clock differential in microseconds.
        idx = 28;
        ByteBuffer bb = ByteBuffer.wrap(fields, idx, 4);
        stateOfHealthSet.add(AcquiredChannelSohAnalog.create(chanId,
            AcquiredChannelSohType.CLOCK_DIFFERENTIAL_IN_MICROSECONDS_OVER_THRESHOLD,
            startTime, endTime, (double) bb.getInt(), ci));

      }
      // Unexpected status format.
      else {
        logger.warn("Unexpected CD1.1 channel status format for channel segment ID %s", chanId);
      }

      return stateOfHealthSet;
    }

  /**
   * Check whether a bit is set in the given byte.  The index starts at zero for the first bit.
   *
   * @return boolean
   */
  private static boolean isSet(byte field, int idx) {
    if (idx > 7) {
      return false;
    }
    return ((field >>> idx) & 0x01) == 0x01;
  }

}
