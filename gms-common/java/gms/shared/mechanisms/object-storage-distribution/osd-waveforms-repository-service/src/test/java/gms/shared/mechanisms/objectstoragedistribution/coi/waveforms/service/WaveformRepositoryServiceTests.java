package gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.service;

import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.BeamRepositoryInterface;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.FkSpectraRepository;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.RawStationDataFrameRepositoryInterface;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.StationSohRepositoryInterface;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.WaveformRepository;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.cassandra.JpaCassandraWaveformRepository;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.jpa.RawStationDataFrameRepositoryJpa;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.jpa.StationSohRepositoryJpa;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.service.configuration.Configuration;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;


public class WaveformRepositoryServiceTests {

  private static WaveformRepository mockWaveformRepository = Mockito
      .mock(JpaCassandraWaveformRepository.class);

  private static StationSohRepositoryInterface mockStationSohRepository = Mockito
      .mock(StationSohRepositoryJpa.class);

  private static RawStationDataFrameRepositoryInterface mockFrameRepository = Mockito
      .mock(RawStationDataFrameRepositoryJpa.class);

  private static BeamRepositoryInterface mockBeamRepository = Mockito.mock(
      BeamRepositoryInterface.class);

  private static FkSpectraRepository mockFkRepository = Mockito.mock(
      FkSpectraRepository.class);

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
    WaveformRepositoryService.startService(
        null, mockWaveformRepository,
        mockStationSohRepository, mockFrameRepository, mockBeamRepository, mockFkRepository);
  }

  @Test(expected = NullPointerException.class)
  public void testStartServiceChecksNullWaveformRepository() throws Exception {
    WaveformRepositoryService.startService(
        config, null,
        mockStationSohRepository, mockFrameRepository, mockBeamRepository, mockFkRepository);
  }

  @Test(expected = NullPointerException.class)
  public void testStartServiceChecksNullSohRepository() throws Exception {
    WaveformRepositoryService.startService(
        config, mockWaveformRepository,
        null, mockFrameRepository, mockBeamRepository, mockFkRepository);
  }

  @Test(expected = NullPointerException.class)
  public void testStartServiceChecksNullFrameRepository() throws Exception {
    WaveformRepositoryService.startService(
        config, mockWaveformRepository,
        mockStationSohRepository, null, mockBeamRepository, mockFkRepository);
  }

  @Test(expected = NullPointerException.class)
  public void testStartServiceChecksNullSdRepository() throws Exception {
    WaveformRepositoryService.startService(
        config, mockWaveformRepository,
        mockStationSohRepository, mockFrameRepository, null, mockFkRepository);
  }

  @Test(expected = NullPointerException.class)
  public void testStartServiceChecksNullFkRepository() throws Exception {
    WaveformRepositoryService.startService(
        config, mockWaveformRepository,
        mockStationSohRepository, mockFrameRepository, mockBeamRepository, null);
  }

}
