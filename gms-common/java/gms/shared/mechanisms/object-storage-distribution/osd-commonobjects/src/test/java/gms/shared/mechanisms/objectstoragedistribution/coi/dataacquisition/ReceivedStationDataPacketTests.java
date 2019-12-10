package gms.shared.mechanisms.objectstoragedistribution.coi.dataacquisition;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.TestUtilities;
import java.io.IOException;
import java.time.Instant;
import org.junit.Test;

public class ReceivedStationDataPacketTests {

  @Test
  public void serializationTest() throws IOException {
    TestUtilities.testSerialization(
        ReceivedStationDataPacket.from(new byte[] {(byte) 1},
            Instant.now(), 5L, "station"),
        ReceivedStationDataPacket.class);
  }

}
