package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.service;

import gms.shared.mechanisms.objectstoragedistribution.coi.event.repository.EventRepository;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.ChannelProcessingGroupRepository;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.QcMaskRepository;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.SignalDetectionRepository;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.factory.ProcessingStationReferenceFactory;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.service.configuration.Configuration;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.FkSpectraRepository;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

public class SignalDetectionRepositoryServiceTests {

  private static QcMaskRepository mockQcMaskRepository = Mockito.mock(QcMaskRepository.class);
  private static SignalDetectionRepository mockSignalDetectionRepository = Mockito
      .mock(SignalDetectionRepository.class);
  private static FkSpectraRepository mockFkRepository = Mockito
      .mock(FkSpectraRepository.class);
  private static ProcessingStationReferenceFactory mockStationReferenceFactory = Mockito
      .mock(ProcessingStationReferenceFactory.class);
  private static EventRepository mockEventRepository = Mockito
      .mock(EventRepository.class);
  private static ChannelProcessingGroupRepository mockChannelProcessingGroupRepository = Mockito
      .mock(ChannelProcessingGroupRepository.class);

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
    SignalDetectionRepositoryService.startService(
        null, mockQcMaskRepository, mockSignalDetectionRepository, mockFkRepository,
        mockChannelProcessingGroupRepository, mockEventRepository,
        mockStationReferenceFactory);
  }

  @Test(expected = NullPointerException.class)
  public void testStartServiceChecksQcMaskRepoNull() throws Exception {
    SignalDetectionRepositoryService.startService(
        config, null, mockSignalDetectionRepository, mockFkRepository,
        mockChannelProcessingGroupRepository, mockEventRepository,
        mockStationReferenceFactory);
  }

  @Test(expected = NullPointerException.class)
  public void testStartServiceChecksSigDetRepoNull() throws Exception {
    SignalDetectionRepositoryService.startService(
        config, mockQcMaskRepository,
        null, mockFkRepository, mockChannelProcessingGroupRepository,
        mockEventRepository, mockStationReferenceFactory);
  }

  @Test(expected = NullPointerException.class)
  public void testStartServiceChecksChannelProcGroupRepoNull() throws Exception {
    SignalDetectionRepositoryService.startService(
        config, mockQcMaskRepository, mockSignalDetectionRepository, mockFkRepository,
        null, mockEventRepository, mockStationReferenceFactory);
  }

  @Test(expected = NullPointerException.class)
  public void testStartServiceChecksStationRefRepoNull() throws Exception {
    SignalDetectionRepositoryService.startService(
        config, mockQcMaskRepository, mockSignalDetectionRepository, mockFkRepository,
        mockChannelProcessingGroupRepository, mockEventRepository,
        null);
  }

  @Test(expected = NullPointerException.class)
  public void testStartServiceChecksNullFkRepo() throws Exception {
    SignalDetectionRepositoryService.startService(
        config, mockQcMaskRepository,
        mockSignalDetectionRepository, null, mockChannelProcessingGroupRepository,
        mockEventRepository, mockStationReferenceFactory);
  }


}
