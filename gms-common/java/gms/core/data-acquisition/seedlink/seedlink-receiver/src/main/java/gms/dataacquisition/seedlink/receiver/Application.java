package gms.dataacquisition.seedlink.receiver;

import gms.shared.mechanisms.objectstoragedistribution.coi.dataacquisition.ReceivedStationDataPacket;
import gms.shared.mechanisms.objectstoragedistribution.coi.dataacquisition.configuration.StationAndChannelId;
import gms.shared.mechanisms.objectstoragedistribution.coi.dataacquisition.configuration.StationDataAcquisitionGroup;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.AcquisitionProtocol;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.RawStationDataFrame;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class Application {

  public static void main(String[] args) {
    final String providerAddress = "rtserve.iris.washington.edu";
    final int providerPort = 18000;
    final Instant now = Instant.now();
    final List<String> requests = List.of("STATION KDAK II");
    final StationDataAcquisitionGroup sdag = StationDataAcquisitionGroup.create(
        requests, AcquisitionProtocol.SEEDLINK,
        providerAddress, providerPort,
        now, now, Map.of(), true, "");
    new SeedlinkConnectionUtility(sdag, now, Application::printPacket).run();
  }

  private static void printPacket(ReceivedStationDataPacket p) {
    final Map<String, StationAndChannelId> m = Map.of(
        "II/KDAK/BH1/00", randomIds(),
        "II/KDAK/BH2/00", randomIds(),
        "II/KDAK/BHZ/00", randomIds(),
        "II/KDAK/BDF/00", randomIds());
    String s = packetString(p);
    final RawStationDataFrame rsdf;
    try {
      rsdf = MiniSeedRawStationDataFrameUtility
          .parseAcquiredStationDataPacket(p.getPacket(), p.getReceptionTime(), m);
      s += String.format( ", start time = %s, end time = %s",
          rsdf.getPayloadDataStartTime(), rsdf.getPayloadDataEndTime());
    } catch (Exception e) {
    }
    System.out.println("Received packet: " + s);
  }

  private static String packetString(ReceivedStationDataPacket p) {
    return String.format("sequence # %s from station %s at %s",
        p.getSequenceNumber(), p.getStationIdentifier(), p.getReceptionTime());
  }

  private static StationAndChannelId randomIds() {
    return StationAndChannelId.from(UUID.randomUUID(), UUID.randomUUID());
  }

}
