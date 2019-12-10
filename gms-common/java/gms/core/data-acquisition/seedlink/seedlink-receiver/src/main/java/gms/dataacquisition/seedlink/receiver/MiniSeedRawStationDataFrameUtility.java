package gms.dataacquisition.seedlink.receiver;

import gms.dataacquisition.seedlink.clientlibrary.Packet;
import gms.dataacquisition.seedlink.clientlibrary.control.DataHeader;
import gms.shared.mechanisms.objectstoragedistribution.coi.dataacquisition.configuration.StationAndChannelId;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.CreationInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.SoftwareComponentInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.AcquiredChannelSoh;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.AcquisitionProtocol;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment.Type;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.RawStationDataFrame;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.RawStationDataFrame.AuthenticationStatus;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import java.io.ByteArrayInputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MiniSeedRawStationDataFrameUtility {

  private static Logger logger = LoggerFactory.getLogger(MiniSeedRawStationDataFrameUtility.class);

  private static final SoftwareComponentInfo softwareInfo = new SoftwareComponentInfo(
      MiniSeedRawStationDataFrameUtility.class.getSimpleName(), "version");

  public static RawStationDataFrame parseAcquiredStationDataPacket(
      byte[] rawPacket, Instant receptionTime,
      Map<String, StationAndChannelId> idsByReceivedName) throws Exception {

    Objects.requireNonNull(rawPacket, "Cannot parse null rawPacket");
    Objects.requireNonNull(receptionTime, "Reception time cannot be null");
    Objects.requireNonNull(idsByReceivedName, "Need non-null idsByReceivedName");

    final Packet p = Packet.read(new ByteArrayInputStream(rawPacket));
    final Instant startTime = startTime(p);
    double nanosPerSample = 1E9 / p.getDataHeader().getSampleRate();
    final Instant endTime = startTime.plusNanos(
        (long) ((p.getDataHeader().getNumSamp() - 1) * nanosPerSample));
    final String receivedName = receivedName(p);
    final StationAndChannelId staAndChanId = idsByReceivedName.get(receivedName);
    Objects.requireNonNull(staAndChanId, "Could not find ids for received name " + receivedName);
    return RawStationDataFrame.create(staAndChanId.getStationId(),
        Set.of(staAndChanId.getChannelId()),
        AcquisitionProtocol.SEEDLINK,
        startTime, endTime, receptionTime, rawPacket,
        AuthenticationStatus.NOT_APPLICABLE, getCreationInfo());
  }

  public static Optional<Pair<ChannelSegment<Waveform>, Collection<AcquiredChannelSoh>>> parseRawStationDataFrame(
      byte[] rawFramePayload, UUID channelId) {

    Objects.requireNonNull(rawFramePayload, "Cannot parse null rawFrame");
    Objects.requireNonNull(channelId, "channelId cannot be null");

    final Collection<AcquiredChannelSoh> parsedSoh = new ArrayList<>();
    try {
      final Packet p = Packet.read(new ByteArrayInputStream(rawFramePayload));
      final Waveform wf = Waveform.withValues(startTime(p), p.getDataHeader().getSampleRate(),
          toDoubles(p.getSamples()));
      final ChannelSegment<Waveform> segment = ChannelSegment.create(channelId,
          segmentName(p), Type.ACQUIRED, List.of(wf), getCreationInfo());
      // TODO: parse SOH and add to parsedSoh; need utility for mapping Blockette's to SOH?
      return Optional.of(Pair.of(segment, parsedSoh));
    } catch (Exception e) {
      logger.error("Error parsing frame from payload", e);
      return Optional.empty();
    }
  }

  private static Instant startTime(Packet p) {
    return Instant.ofEpochMilli(p.getDataHeader().getStartTimeMillis());
  }

  private static String segmentName(Packet p) {
    final DataHeader head = p.getDataHeader();
    return head.getStationId().trim()
        + "/" + head.getChannelId().trim()
        + "/" + head.getLocationId().trim()
        + " " + Type.ACQUIRED;
  }

  private static double[] toDoubles(Number[] nums) {
    return Arrays.stream(nums).mapToDouble(Number::doubleValue).toArray();
  }

  private static String receivedName(Packet p) {
    final DataHeader head = p.getDataHeader();
    // get trimmed m8
    return String.join("/", head.getNetworkId().trim(),
        head.getStationId().trim(), head.getChannelId().trim(),
        head.getLocationId().trim()).trim();
  }

  private static CreationInfo getCreationInfo() {
    return new CreationInfo(
        MiniSeedRawStationDataFrameUtility.class.getSimpleName(),
        softwareInfo);
  }
}
