package gms.core.eventlocation.plugins.pluginutils.seedgeneration;

import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.EventLocation;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.LocationRestraint;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetectionHypothesis;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceStation;
import java.time.Instant;
import java.util.Map;

/**
 * Wraps another seed generator and uses a LocationConstraint to fix one or more of the values
 * returned by the seed generator to a specific value.
 */
public class RestrainedSeedGenerator implements SeedGenerator {

  private SeedGenerator wrappedSeedGenerator;
  private LocationRestraint locationRestraint;

  public RestrainedSeedGenerator(SeedGenerator wrappedSeedGenerator,
      LocationRestraint locationRestraint) {
    this.wrappedSeedGenerator = wrappedSeedGenerator;
    this.locationRestraint = locationRestraint;
  }

  @Override
  public EventLocation generate(EventLocation defaultSeedLocation,
      Map<SignalDetectionHypothesis, ReferenceStation> observationStationMap) {
    EventLocation preliminaryLocation = wrappedSeedGenerator.generate(defaultSeedLocation,
        observationStationMap);

    double preliminaryDepth = preliminaryLocation.getDepthKm();
    double preliminaryLatitude = preliminaryLocation.getLatitudeDegrees();
    double preliminaryLongitude = preliminaryLocation.getLongitudeDegrees();

    double newDepth = locationRestraint.getDepthRestraintType().getValue(preliminaryDepth,
        locationRestraint.getDepthRestraintKm().orElse(preliminaryDepth));

    double newLatitude;
    double newLongitude;

    //TODO: git rid of ugly switch/if-else?
    switch (locationRestraint.getLatitudeRestraintType()) {
      case UNRESTRAINED:
        newLatitude = preliminaryLatitude;
        break;
      case FIXED:
        newLatitude = locationRestraint.getLatitudeRestraintDegrees().orElse(preliminaryLatitude);
        break;
      default:
        throw new IllegalStateException(locationRestraint.getLatitudeRestraintType()
            + " not a supported latitude restraint type");
    }

    //TODO: git rid of ugly switch/if-else?
    switch (locationRestraint.getLongitudeRestraintType()) {
      case UNRESTRAINED:
        newLongitude = preliminaryLongitude;
        break;
      case FIXED:
        newLongitude = locationRestraint.getLongitudeRestraintDegrees()
            .orElse(preliminaryLongitude);
        break;
      default:
        throw new IllegalStateException(locationRestraint.getLongitudeRestraintType()
            + " not a supported longitude restraint type");
    }

    Instant preliminaryTime = preliminaryLocation.getTime();
    Instant newTime;

    //TODO: git rid of ugly switch/if-else?
    switch (locationRestraint.getTimeRestraintType()) {
      case UNRESTRAINED:
        newTime = preliminaryTime;
        break;
      case FIXED:
        newTime = locationRestraint.getTimeRestraint().orElse(preliminaryTime);
        break;
      default:
        throw new IllegalStateException(locationRestraint.getTimeRestraintType()
            + " not a supported time restraint type");
    }

    return EventLocation.from(
        newLatitude,
        newLongitude,
        newDepth,
        newTime
    );
  }
}
