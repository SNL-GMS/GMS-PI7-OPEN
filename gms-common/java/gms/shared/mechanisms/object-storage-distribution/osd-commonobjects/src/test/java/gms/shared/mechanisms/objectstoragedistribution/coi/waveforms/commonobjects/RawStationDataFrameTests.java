package gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.TestUtilities;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.CreationInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.TestFixtures;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.RawStationDataFrame.AuthenticationStatus;
import org.junit.Test;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public class RawStationDataFrameTests {

  private final UUID id = UUID.randomUUID();
  private final UUID stationId = UUID.fromString("46947cc2-8c86-4fa1-a764-c9b9944614b7");
  private final Set<UUID> channelIds = Set.of(UUID.fromString("00000000-0000-0000-0000-000000000000"));
  private final String stationName = "staName";
  private final Instant payloadDataStartTime = Instant.EPOCH;
  private final Instant payloadDataEndTime = Instant.EPOCH.plusMillis(2000);
  private final Instant receptionTime = Instant.EPOCH.plusSeconds(10);
  private final byte[] rawPayload = new byte[50];
  private final AuthenticationStatus authenticationStatus = AuthenticationStatus.AUTHENTICATION_SUCCEEDED;
  private final CreationInfo creationInfo = CreationInfo.DEFAULT;

  @Test
  public void testSerialization() throws Exception {
    TestUtilities.testSerialization(TestFixtures.rawStationDataFrame,
        RawStationDataFrame.class);
  }

  @Test
  public void testEqualsAndHashcode() {
    TestUtilities.checkClassEqualsAndHashcode(RawStationDataFrame.class);
  }

  @Test
  public void testRawStationDataFrameCreateChecksNullArguments() throws Exception {
    TestUtilities.checkStaticMethodValidatesNullArguments(
        RawStationDataFrame.class, "create",
            stationId, channelIds, AcquisitionProtocol.CD11, payloadDataStartTime,
        payloadDataEndTime, receptionTime,
        rawPayload, authenticationStatus, creationInfo);
  }

  @Test
  public void testRawStationDataFrameFromChecksNullArguments() throws Exception {
    TestUtilities.checkStaticMethodValidatesNullArguments(
        RawStationDataFrame.class, "from",
            id, stationId, channelIds, AcquisitionProtocol.CD11, payloadDataStartTime,
        payloadDataEndTime, receptionTime,
        rawPayload, authenticationStatus, creationInfo);
  }

}
