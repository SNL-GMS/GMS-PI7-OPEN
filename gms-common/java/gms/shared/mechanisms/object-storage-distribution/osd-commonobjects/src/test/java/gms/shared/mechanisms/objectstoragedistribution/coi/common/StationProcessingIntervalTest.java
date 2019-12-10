package gms.shared.mechanisms.objectstoragedistribution.coi.common;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class StationProcessingIntervalTest {

  @Test
  void testFromValidation() {
    UUID defaultId = new UUID(0, 0);
    Instant defaultStart = Instant.EPOCH;
    Instant defaultEnd = defaultStart.plusSeconds(30);

    Assertions.assertAll("From method validation",
        () -> Assertions.assertThrows(IllegalArgumentException.class,
            () -> StationProcessingInterval
                .from(defaultId, defaultId, List.of(), defaultStart, defaultEnd),
            "Expected validation of non-empty processingIds"),
        () -> Assertions.assertThrows(IllegalArgumentException.class,
            () -> StationProcessingInterval.from(defaultId, defaultId, List.of(defaultId),
                defaultEnd, defaultStart),
            "Expected validation of start time before end time"));
  }
}
