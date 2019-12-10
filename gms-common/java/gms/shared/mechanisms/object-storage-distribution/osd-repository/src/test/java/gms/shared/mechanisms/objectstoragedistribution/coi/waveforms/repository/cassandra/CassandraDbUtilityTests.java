package gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.cassandra;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.time.Instant;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class CassandraDbUtilityTests {

  @ParameterizedTest
  @MethodSource("toEpochNanoArguments")
  void testToEpochNano(Instant inputInstant, long expectedNano) {
    assertEquals(expectedNano, CassandraDbUtility.toEpochNano(inputInstant));
  }

  private static Stream<Arguments> toEpochNanoArguments() {
    return Stream.of(
        arguments(Instant.EPOCH, 0L),
        arguments(Instant.ofEpochSecond(0, 1), 1L),
        arguments(
            Instant.ofEpochSecond(Long.MAX_VALUE / 1_000_000_000, Long.MAX_VALUE % 1_000_000_000),
            Long.MAX_VALUE),
        arguments(
            Instant.ofEpochSecond(Long.MIN_VALUE / 1_000_000_000, Long.MIN_VALUE % 1_000_000_000),
            Long.MIN_VALUE)
    );
  }

  @ParameterizedTest
  @MethodSource("fromEpochNanoArguments")
  void testFromEpochNano(long inputNano, Instant expectedInstant) {
    assertEquals(expectedInstant, CassandraDbUtility.fromEpochNano(inputNano));
  }

  private static Stream<Arguments> fromEpochNanoArguments() {
    return Stream.of(
        arguments(0L, Instant.EPOCH),
        arguments(1L, Instant.ofEpochSecond(0, 1)),
        arguments(
            Long.MAX_VALUE,
            Instant.ofEpochSecond(Long.MAX_VALUE / 1_000_000_000, Long.MAX_VALUE % 1_000_000_000)),
        arguments(
            Long.MIN_VALUE,
            Instant.ofEpochSecond(Long.MIN_VALUE / 1_000_000_000, Long.MIN_VALUE % 1_000_000_000))
    );
  }


}