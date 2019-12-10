package gms.core.eventlocation.plugins.pluginutils.seedgeneration;

import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.DepthRestraintType;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.EventLocation;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.LocationRestraint;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.RestraintType;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetectionHypothesis;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceStation;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class RestrainedSeedGeneratorTests {

  @Test
  public void testParametersAreFixed() {

    SeedGenerator fakeSeedGenerator = (defaultSeedLocation, observationStationMap) -> EventLocation
        .from(20.0, 10.0, 100.0, Instant.EPOCH);

    RestrainedSeedGenerator depthRestrainedGenerator = new RestrainedSeedGenerator(
        fakeSeedGenerator,
        new LocationRestraint.Builder()
            .setDepthRestraintAtSurface().build());

    EventLocation seedLocation = depthRestrainedGenerator.generate(null, null);

    Assertions.assertEquals(20.0, seedLocation.getLatitudeDegrees());
    Assertions.assertEquals(10.0, seedLocation.getLongitudeDegrees());
    Assertions.assertEquals(Instant.EPOCH, seedLocation.getTime());
    Assertions.assertEquals(0.0, seedLocation.getDepthKm());

    RestrainedSeedGenerator locationRestrainedGenerator = new RestrainedSeedGenerator(
        fakeSeedGenerator,
        new LocationRestraint.Builder()
            .setPositionRestraint(34.0, 31.0).build());

    seedLocation = locationRestrainedGenerator.generate(null, null);
    Assertions.assertEquals(34.0, seedLocation.getLatitudeDegrees());
    Assertions.assertEquals(31.0, seedLocation.getLongitudeDegrees());
    Assertions.assertEquals(Instant.EPOCH, seedLocation.getTime());
    Assertions.assertEquals(100.0, seedLocation.getDepthKm());

    RestrainedSeedGenerator timeRestrainedGenerator = new RestrainedSeedGenerator(
        fakeSeedGenerator,
        new LocationRestraint.Builder()
            .setTimeRestraint(Instant.MIN).build());

    seedLocation = timeRestrainedGenerator.generate(null, null);

    Assertions.assertEquals(20.0, seedLocation.getLatitudeDegrees());
    Assertions.assertEquals(10.0, seedLocation.getLongitudeDegrees());
    Assertions.assertEquals(Instant.MIN, seedLocation.getTime());
    Assertions.assertEquals(100.0, seedLocation.getDepthKm());
  }

}
