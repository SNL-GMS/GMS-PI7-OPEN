package gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.service;

import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.ProcessingStationReferenceFactoryInterface;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.repository.StationReferenceRepositoryInterface;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.service.configuration.Configuration;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;


public class StationReferenceCoiServiceTests {

  private static StationReferenceRepositoryInterface stationRefRepo = Mockito
      .mock(StationReferenceRepositoryInterface.class);

  private static ProcessingStationReferenceFactoryInterface stationRefFactory = Mockito
      .mock(ProcessingStationReferenceFactoryInterface.class);

  private static Configuration config;

  @BeforeClass
  public static void setup() throws Exception {
    config = Configuration.builder().build();
  }

  // Note: you cannot use TestUtilities to check the null parameter validation
  // for this operation because that will actually start the HTTP service
  // when it makes the first call with all 'good' params, then leave it running!
  // That's why it's done tediously one at a time below.

  @Test(expected = NullPointerException.class)
  public void testStartServiceChecksConfigNull() throws Exception {
    StationReferenceCoiService.startService(null, stationRefRepo, stationRefFactory);
  }

  @Test(expected = NullPointerException.class)
  public void testStartServiceChecksRefRepoNull() throws Exception {
    StationReferenceCoiService.startService(config, null, stationRefFactory);
  }

  @Test(expected = NullPointerException.class)
  public void testStartServiceChecksFactoryNull() throws Exception {
    StationReferenceCoiService.startService(config, stationRefRepo, null);
  }

}
