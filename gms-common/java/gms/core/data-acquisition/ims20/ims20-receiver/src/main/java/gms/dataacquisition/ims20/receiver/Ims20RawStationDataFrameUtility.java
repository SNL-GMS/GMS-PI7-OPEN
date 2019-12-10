package gms.dataacquisition.ims20.receiver;

import gms.shared.mechanisms.objectstoragedistribution.coi.dataacquisition.configuration.StationAndChannelId;
import gms.shared.mechanisms.objectstoragedistribution.coi.dataacquisition.configuration.StationDataAcquisitionGroup;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.CreationInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.SoftwareComponentInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.AcquiredChannelSoh;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.AcquisitionProtocol;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment.Type;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.RawStationDataFrame;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.RawStationDataFrame.AuthenticationStatus;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import gms.utilities.waveformreader.Ims20Cm6WaveformReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Ims20RawStationDataFrameUtility {

  private static Logger logger = LoggerFactory.getLogger(Ims20RawStationDataFrameUtility.class);

  private static final SoftwareComponentInfo softwareInfo = new SoftwareComponentInfo(
      Ims20RawStationDataFrameUtility.class.getSimpleName(), "version");

  // Note - it was decided by architecture not to pass the providerAddress and receiverAddress
  // strings at this time. They are used for validation, but do we need that for IMS?
  // Also, receptionTime will come from NiFi via the Flask server's JSON string since that is
  // the time that the payload entered the system.


  public static RawStationDataFrame parseAcquiredStationDataPacket(
      byte[] payloadBytes, Instant receptionTime,
      StationDataAcquisitionGroup acquisitionGroup) throws Exception {

    Objects.requireNonNull(payloadBytes, "Cannot parse null payloadBytes");
    Objects.requireNonNull(receptionTime, "Cannot parse null receptionTime");
    Objects.requireNonNull(acquisitionGroup, "Cannot parse null acquisitionGroup");

    StationAndChannelId staAndChanId = null;
    UUID stationId = null;
    Set<UUID> channelIDs = new HashSet<>();
    int numberSamples = 0;
    int numberSamplesMax = 0;
    Instant payloadStartTime = null;
    Instant payloadEndTime = null;
    String payloadStation = null;
    double sampleRateMax = 0.0;
    double sampleRateHz = 0.0;
    boolean isStationFound = false;
    boolean isSampleRateFound = false;

    List<String> wid2Blocks = getWid2Blocks(payloadBytes);

    try {
      for (String wid2Block : wid2Blocks) {
        String wid2 = wid2Block.substring(0, 4).trim();
        if (!(isWid2Block(wid2))) {
          logger.error("Block is not a WID2 block");
          throw new IllegalArgumentException("Block is not a WID2 block");
        }

        payloadStartTime = extractStartTime(wid2Block);

        // Returning both the station and the net/sta/chan from one function seems a little hacky,
        // but the two are so intertwined, it was hard to pull them apart
        Pair<String, String> stationAndNetStaChan = extractStationAndNetStaChan(wid2Block);
        String station = stationAndNetStaChan.getLeft();
        String netStaChan = stationAndNetStaChan.getRight();

        // first time through loop, set station for validation
        if (!isStationFound) {
          payloadStation = station;
          isStationFound = true;
        } else {
          if (!station.equals(payloadStation)) {
            logger.error("Different stations found in response file.");
            throw new IllegalArgumentException("Different stations found in response file.");
          }
        }

        staAndChanId = acquisitionGroup.getIdsByReceivedName().get(netStaChan);

        if (staAndChanId == null) {
          logger.error("Station " + netStaChan + " not found");
          throw new NullPointerException("Station " + netStaChan + " not found");
        } else {
          stationId = staAndChanId.getStationId();
          channelIDs.add(staAndChanId.getChannelId());
        }

        String numberSamplesString = wid2Block.substring(48, 56).trim();
        numberSamples = Integer.valueOf(numberSamplesString);

        sampleRateHz = extractSampleRate(wid2Block);

        // sample rates may not all be the same, so iteratively compare values and store max value.
        // also need to store number of samples associated with this max sample rate.
        // these values are used to calculate payload end time later on.
        // if first time through loop, set max sample rate and number samples for later comparison
        if (!isSampleRateFound) {
          sampleRateMax = sampleRateHz;
          numberSamplesMax = numberSamples;
          isSampleRateFound = true;
        } else {
          if (sampleRateHz != sampleRateMax) {
            sampleRateMax = Double.max(sampleRateHz, sampleRateMax);
            numberSamplesMax = numberSamples;
          }
        }
      }

      try {
        if (payloadStartTime == null ||
            Double.compare(sampleRateMax, 0.0) < 0) {
          logger.error("payload start time %d is null or < 0", payloadStartTime);
          throw new IllegalArgumentException("payload start time " + payloadStartTime + " is null or < 0");
        }
        // use the max numberSamples and sampleRate for the station to calculate payloadEndTime
        double calculatedSecs = (numberSamplesMax / sampleRateMax);
        payloadEndTime = payloadStartTime.plusSeconds((long) calculatedSecs);
      } catch (Exception e) {
        logger.error("Error calculating payloadEndTime", e);
        throw new ArithmeticException("Error calculating payloadEndTime");
      }

      return RawStationDataFrame.create(
          stationId, channelIDs,
          AcquisitionProtocol.IMS_WAVEFORM,
          payloadStartTime, payloadEndTime, receptionTime,
          payloadBytes,
          AuthenticationStatus.NOT_APPLICABLE,
          getCreationInfo());

    } catch (Exception e) {
      logger.error("Error parsing payload", e);
      throw new UnsupportedOperationException("Error parsing payload", e);
    }
  }

  /**
   * Extract all of the WID2 blocks from the provided byte []. This block starts with 'WID2' and
   * ends with 'CHK2 ' followed by some digits (number of digits can vary);
   *
   * @param payloadBytes bytes containing WID2 blocks
   * @return list of individual wid2 blocks
   */
  private static List<String> getWid2Blocks(byte[] payloadBytes) {
    String payloadString = new String(payloadBytes);
    // replace all newlines with space so that we can use trim()
    payloadString = payloadString.replaceAll("\n", " ");

    List<String> wid2Blocks = new ArrayList<>();
    final String patternString = "WID2.*?CHK2 \\d+";
    final Pattern pattern = Pattern.compile(patternString);
    final Matcher matcher = pattern.matcher(payloadString);

    while (matcher.find()) {
      wid2Blocks.add(matcher.group().trim());
    }
    return wid2Blocks;
  }

  /**
   * Return true if provided string equals 'WID2', false otherwise
   *
   * @param s String that is being checked for 'WID2' equality
   * @return true if provided string equals 'WID2', false otherwise
   */
  private static boolean isWid2Block(String s) {
    return s.toUpperCase().matches("WID2");
  }

  /**
   * Given a WID2 block, extract the start time
   *
   * @param wid2Block String containing WID2 data
   * @return start time in provided WID2 block
   */
  private static Instant extractStartTime(String wid2Block) {
    String payloadDateString = wid2Block.substring(5, 15).trim();
    // UTC time requires dates with "-" instead of "/"
    payloadDateString = payloadDateString.replaceAll("/", "-");

    final String payloadStartTimeString = wid2Block.substring(16, 28).trim();

    // combine the separate date and time strings into a UTC string representation
    final String payloadDateTimeUtcString = payloadDateString + "T" + payloadStartTimeString + "Z";
    return Instant.parse(payloadDateTimeUtcString);
  }

  /**
   * Given a WID2 block, extract the sample rate
   *
   * @param wid2Block String containing WID2 data
   * @return sample rate in provided WID2 block
   */
  private static double extractSampleRate(String wid2Block) {
    final String sampleRateString = wid2Block.substring(57, 68).trim();
    return Double.valueOf(sampleRateString); // sample rate in Hz
  }

  /**
   * Given a WID2 block, extract the waveform data as InputStream (bytes)
   *
   * @param wid2Block String containing WID2 data
   * @return waveform data in provided WID2 block as InputStream (bytes)
   */
  private static InputStream extractWaveformData(String wid2Block) {
    final String patternString = "DAT2 (.*?)CHK2 \\d+";
    final Pattern pattern = Pattern.compile(patternString);
    final Matcher matcher = pattern.matcher(wid2Block);
    matcher.find();
    String waveform = matcher.group(1);
    waveform = waveform.replaceAll(" ", "");
    return new ByteArrayInputStream(waveform.getBytes());
  }


  /**
   * Given a WID2 block, extract station and net/sta/chan
   *
   * @param wid2Block String containing WID2 data
   * @return pair with station (left) and net/sta/chan (right)
   */
  private static Pair<String, String> extractStationAndNetStaChan(String wid2Block) {
    final String network = wid2Block.substring(111, 121).trim();
    String station = wid2Block.substring(29, 34).trim();
    final String channel = wid2Block.substring(35, 38).trim(); // channel

    String arrayStation = null;
    String netStaChan;

    /**
     * if Network field is blank, then it's a 3-component station (not array) and the station
     * name is in the WID2 line
     * if Network has a value, then this is an array network that will have different station
     * names in the file
     * (e.g. network = KURK, Station names = KUR01, KUR02, KURBB, etc.)
     */
    // array stations populate the network field - use that value as the station for comparison
    if (!network.equals("")) {
      // save original station value for composing netStaChan string below
      arrayStation = station;
      station = network;
    }

    if (arrayStation != null) {
      netStaChan = network + "/" + arrayStation + "/" + channel;
    } else {
      // for non-array station, network is blank so use station name as replacement
      netStaChan = station + "/" + station + "/" + channel;
    }

    return Pair.of(station, netStaChan);
  }

  /**
   * Create channel segment and SOH pairs from an IMS payload.
   *
   * @param rawFramePayload the raw payload for IMS data
   *
   * @return List of channel segment and SOH pairs created from an IMS payload
   */
  public static List<Optional<Pair<ChannelSegment<Waveform>, Collection<AcquiredChannelSoh>>>>
  parseRawStationDataFrame(byte[] rawFramePayload) {
    Objects.requireNonNull(rawFramePayload, "Cannot parse null rawFrame");

    // This will stay empty since SOH does not come in at the same time as waveform data for
    // the IMS 2.0 protocol
    final Collection<AcquiredChannelSoh> parsedSoh = new ArrayList<>();
    final Ims20Cm6WaveformReader waveformReader = new Ims20Cm6WaveformReader();
    final Map<String, StationAndChannelId> stationMappings = HardCodedIms20Config.getMappings();

    List<Optional<Pair<ChannelSegment<Waveform>, Collection<AcquiredChannelSoh>>>>
        channelSegmentPairs = new ArrayList<>();

    try {
      List<String> wid2Blocks = getWid2Blocks(rawFramePayload);
      for (String wid2Block : wid2Blocks) {
        final Instant startTime = extractStartTime((wid2Block));
        final double sampleRate = extractSampleRate(wid2Block);
        final InputStream waveformIs = extractWaveformData(wid2Block);
        final double[] waveformDoubles = waveformReader.read(waveformIs, waveformIs.available(), 0);
        final Waveform wf = Waveform.withValues(startTime, sampleRate, waveformDoubles);

        // Get the segment name for this waveform
        final Pair<String, String> stationAndNetStaChan = extractStationAndNetStaChan(wid2Block);
        final String netStaChan = stationAndNetStaChan.getRight();
        // Get the channelId for this waveform
        final StationAndChannelId staChanId = stationMappings.get(netStaChan);
        final UUID channelId = staChanId.getChannelId();

        final ChannelSegment<Waveform> segment =
            ChannelSegment
                .create(channelId, netStaChan, Type.ACQUIRED, List.of(wf), getCreationInfo());
        channelSegmentPairs.add(Optional.of(Pair.of(segment, parsedSoh)));
      }

    } catch (Exception e) {
      logger.error("Error parsing waveforms from RawStationDataFrame payload", e);
      return List.of(Optional.empty());
    }
    return channelSegmentPairs;
  }

  private static CreationInfo getCreationInfo() {
    return new CreationInfo(
        Ims20RawStationDataFrameUtility.class.getSimpleName(), softwareInfo);
  }
}

