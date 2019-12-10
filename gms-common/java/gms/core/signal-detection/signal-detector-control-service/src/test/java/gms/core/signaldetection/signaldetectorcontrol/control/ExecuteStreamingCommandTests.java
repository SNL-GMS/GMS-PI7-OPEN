package gms.core.signaldetection.signaldetectorcontrol.control;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import gms.core.signaldetection.signaldetectorcontrol.TestFixtures;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.processingcontrol.commonobjects.ProcessingContext;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.processingcontrol.commonobjects.StorageVisibility;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import java.time.Instant;
import java.util.UUID;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ExecuteStreamingCommandTests {

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  @Test
  public void testCreateNullChannelSegmentIdExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage(
        "Error creating ExecuteStreamingCommand: Channel Segment Id cannot be null");
    ExecuteStreamingCommand.create(null,
        Instant.EPOCH,
        Instant.EPOCH.plusSeconds(100),
        //TODO: Fix SignalDetectorParameters create() once SignalDetectorParameters is implemented
        //SignalDetectorParameters.create(XXX),
        ProcessingContext.createInteractive(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
        StorageVisibility.PRIVATE));
  }

  @Test
  public void testCreateNullStartTimeExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage(
        "Error creating ExecuteStreamingCommand: Start Time cannot be null");
    ExecuteStreamingCommand.create(TestFixtures.randomChannelSegment(),
        null,
        Instant.EPOCH.plusSeconds(100),
        //TODO: Fix SignalDetectorParameters create() once SignalDetectorParameters is implemented
        //SignalDetectorParameters.create(XXX),
        ProcessingContext.createInteractive(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
            StorageVisibility.PRIVATE));
  }


  @Test
  public void testCreateNullEndTimeExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage(
        "Error creating ExecuteStreamingCommand: End Time cannot be null");
    ExecuteStreamingCommand.create(TestFixtures.randomChannelSegment(),
        Instant.EPOCH,
        null,
        //TODO: Fix SignalDetectorParameters create() once SignalDetectorParameters is implemented
        //SignalDetectorParameters.create(XXX),
        ProcessingContext.createInteractive(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
            StorageVisibility.PRIVATE));
  }

  //TODO: to be included when SignalDetectorParameters is implemented
  //@Test
  //public void testCreateNullSignalDetectorParametersExpectNullPointerException() {
  //  exception.expect(NullPointerException.class);
  //  exception.expectMessage(
  //      "Error Creating ExecuteStreamingCommand: Signal Detector Parameters cannot be null");
  //  ExecuteStreamingCommand.create(UUID.randomUUID(),
  //      Instant.EPOCH,
  //      Instant.EPOCH.plusSeconds(100),
  //      null,
  //      ProcessingContext.createInteractive(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
  //          StorageVisibility.PRIVATE));
  //}

  @Test
  public void testCreateNullProcessingContextExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage(
        "Error creating ExecuteStreamingCommand: Processing Context cannot be null");
    ExecuteStreamingCommand.create(TestFixtures.randomChannelSegment(),
        Instant.EPOCH,
        Instant.EPOCH.plusSeconds(100),
        //TODO: Fix SignalDetectorParameters create() once SignalDetectorParameters is implemented
        //SignalDetectorParameters.create(XXX),
        null);
  }

  @Test
  public void testCreate() {
    ChannelSegment<Waveform> channelSegment = TestFixtures.randomChannelSegment();
    Instant startTime = Instant.EPOCH;
    Instant endTime = Instant.EPOCH.plusSeconds(100);
    //TODO: Fix SignalDetectorParameters create() once SignalDetectorParameters is implemented
    //SignalDetectorParameters signalDetectorParameters = SignalDetectorParameters.create(XXX);
    ProcessingContext processingContext = ProcessingContext
        .createInteractive(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
            StorageVisibility.PRIVATE);
    //TODO: Include SignalDetectorParameters once SignalDetectorParameters is implemented
    ExecuteStreamingCommand command = ExecuteStreamingCommand
        .create(channelSegment,  startTime, endTime, processingContext);

    assertNotNull(command);
    assertEquals(channelSegment, command.getChannelSegment());
    assertEquals(startTime, command.getStartTime());
    assertEquals(endTime, command.getEndTime());
    //TODO: to be included when SignalDetectorParameters is implemented
    //assertEquals(signalDetectorParameters, command.getSignalDetectorParameters());
    assertEquals(processingContext, command.getProcessingContext());
  }
}
