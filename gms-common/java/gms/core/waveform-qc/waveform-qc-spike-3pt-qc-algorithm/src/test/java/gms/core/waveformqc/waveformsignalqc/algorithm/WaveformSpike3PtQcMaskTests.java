package gms.core.waveformqc.waveformsignalqc.algorithm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

public class WaveformSpike3PtQcMaskTests {

  @Test
  public void testCreate() {
    Instant spikeTime = Instant.ofEpochSecond(2);

    UUID channelUuid = UUID.randomUUID();
    UUID channelSegmentUuid = UUID.randomUUID();
    WaveformSpike3PtQcMask mask = WaveformSpike3PtQcMask
        .create(channelUuid, channelSegmentUuid, Instant.ofEpochSecond(2));

    assertEquals(spikeTime, mask.getStartTime());
    assertEquals(spikeTime, mask.getEndTime());
    assertEquals(channelUuid, mask.getChannelId());
    assertEquals(channelSegmentUuid, mask.getChannelSegmentId());
  }

  @Test
  public void testNullMaskParameterExceptionChannelId() {
    assertNullPointer("WaveformSpike3PtQcMask cannot have null channelId",
        () -> WaveformSpike3PtQcMask
            .create(null, UUID.randomUUID(), Instant.ofEpochSecond(2)));
  }

  @Test
  public void testNullMaskParameterExceptionChannelSegmentId() {
    assertNullPointer("WaveformSpike3PtQcMask cannot have null channelSegmentId",
        () -> WaveformSpike3PtQcMask
            .create(UUID.randomUUID(), null, Instant.ofEpochSecond(2)));
  }

  @Test
  public void testNullMaskParameterExceptionSpikeTime() {
    assertNullPointer("WaveformSpike3PtQcMask cannot have null spikeTime",
        () -> WaveformSpike3PtQcMask
            .create(UUID.randomUUID(), UUID.randomUUID(), null));
  }

  private static void assertNullPointer(String msg, Executable exec) {
    assertEquals(msg, assertThrows(NullPointerException.class, exec).getMessage());
  }
}
