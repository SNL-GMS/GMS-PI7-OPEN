package gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.cassandra;

import static gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.cassandra.JpaCassandraWaveformRepository.DOUBLES_PER_BLOCK;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import java.time.Instant;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class JpaCassandraWaveformRepositoryTest {

  @ParameterizedTest
  @MethodSource("breakIntoBlocksArguments")
  void testBreakIntoBlocks(Waveform waveformToBreak, List<Waveform> expectedWaveformBlocks) {

    assertEquals(expectedWaveformBlocks,
        JpaCassandraWaveformRepository.breakIntoBlocks(waveformToBreak));
  }

  private static Stream<Arguments> breakIntoBlocksArguments() {
    return Stream.of(
        //Identity
        arguments(Waveform.withoutValues(Instant.EPOCH, 1.0, DOUBLES_PER_BLOCK),
            List.of(Waveform.withoutValues(Instant.EPOCH, 1.0, DOUBLES_PER_BLOCK))),
        //Identity with different sample rate
        arguments(Waveform.withoutValues(Instant.EPOCH, 23.0, DOUBLES_PER_BLOCK),
            List.of(Waveform.withoutValues(Instant.EPOCH, 23.0, DOUBLES_PER_BLOCK))),
        //Simple case for exactly two blocks
        arguments(Waveform.withoutValues(Instant.EPOCH, 1.0, 2 * DOUBLES_PER_BLOCK),
            List.of(Waveform.withoutValues(Instant.EPOCH, 1.0, DOUBLES_PER_BLOCK),
                Waveform.withoutValues(Instant.EPOCH.plusSeconds(DOUBLES_PER_BLOCK), 1.0,
                    DOUBLES_PER_BLOCK))),
        //3-block complex case that ensures nanosecond precision
        arguments(Waveform.withoutValues(Instant.EPOCH, 3.0, 3 * DOUBLES_PER_BLOCK),
            List.of(Waveform.withoutValues(Instant.EPOCH, 3.0, DOUBLES_PER_BLOCK),
                Waveform
                    .withoutValues(Instant.EPOCH.plusNanos(DOUBLES_PER_BLOCK * 1_000_000_000 / 3),
                        3.0, DOUBLES_PER_BLOCK),
                Waveform.withoutValues(
                    Instant.EPOCH.plusNanos(2 * DOUBLES_PER_BLOCK * 1_000_000_000 / 3), 3.0,
                    DOUBLES_PER_BLOCK))),
        //Partial block case
        arguments(Waveform.withoutValues(Instant.EPOCH, 1.0, (long) (DOUBLES_PER_BLOCK * 1.5)),
            List.of(Waveform.withoutValues(Instant.EPOCH, 1.0, DOUBLES_PER_BLOCK),
                Waveform.withoutValues(Instant.EPOCH.plusSeconds(DOUBLES_PER_BLOCK), 1.0,
                    (long) (DOUBLES_PER_BLOCK * 0.5)))),
        //Single sample block
        arguments(Waveform.withoutValues(Instant.EPOCH, 1.0, DOUBLES_PER_BLOCK + 1),
            List.of(Waveform.withoutValues(Instant.EPOCH, 1.0, DOUBLES_PER_BLOCK),
                Waveform.withoutValues(Instant.EPOCH.plusSeconds(DOUBLES_PER_BLOCK), 1.0, 1)))
    );
  }
}