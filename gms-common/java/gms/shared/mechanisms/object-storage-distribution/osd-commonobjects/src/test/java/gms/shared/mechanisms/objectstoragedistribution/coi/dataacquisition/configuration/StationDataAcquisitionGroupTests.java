package gms.shared.mechanisms.objectstoragedistribution.coi.dataacquisition.configuration;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.TestUtilities;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.AcquisitionProtocol;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.Test;

public class StationDataAcquisitionGroupTests {

  // also tests that StationAndChannelId deserializes because this object has a reference to one
  @Test
  public void serializationTest() throws IOException {
    TestUtilities.testSerialization(
        StationDataAcquisitionGroup.create(List.of("request"),
            AcquisitionProtocol.CD11, "127.0.0.1", 4000,
            Instant.EPOCH, Instant.EPOCH,
            Map.of("foo", StationAndChannelId.from(UUID.randomUUID(), UUID.randomUUID())),
            true, "comment"),
        StationDataAcquisitionGroup.class);
  }

}
