package gms.core.eventlocation.plugins.implementations.apachelm;

import gms.core.eventlocation.plugins.EventLocationDefinition;
import gms.core.eventlocation.plugins.EventLocatorPlugin;
import gms.core.eventlocation.plugins.definitions.EventLocationDefinitionApacheLm;
import gms.core.eventlocation.plugins.exceptions.TooManyRestraintsException;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.LocationSolution;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.PreferredLocationSolution;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetectionHypothesis;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceStation;
import gms.shared.mechanisms.pluginregistry.Name;
import gms.shared.mechanisms.pluginregistry.Version;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.lang3.Validate;

@Name("eventLocationApacheLmPlugin")
@Version("1.0.0")
public class EventLocationApacheLmPlugin implements EventLocatorPlugin {

  private EventLocatorApacheLmDelegate delegate;

  @Override
  public void initialize() {
    delegate = new EventLocatorApacheLmDelegate();
  }

  @Override
  public List<LocationSolution> locate(
      Optional<PreferredLocationSolution> start,
      List<SignalDetectionHypothesis> observations,
      List<ReferenceStation> stations,
      EventLocationDefinition parameters
  ) throws TooManyRestraintsException {

    Objects.requireNonNull(start, "Null start");
    Objects.requireNonNull(observations, "Null observations");
    Objects.requireNonNull(stations, "Null stations");
    Objects.requireNonNull(parameters, "Null parameters");

    if (observations.isEmpty()) {

      throw new IllegalArgumentException("Cannot locate with empty observations");
    }

    if (stations.isEmpty()) {

      throw new IllegalArgumentException("Cannot locate with empty stations");
    }

    Validate.isInstanceOf(EventLocationDefinitionApacheLm.class, parameters);
    EventLocationDefinitionApacheLm apacheLmParameters = (EventLocationDefinitionApacheLm) parameters;

    delegate.initialize(start, apacheLmParameters);

    return delegate.locate(observations, stations, parameters);
  }

}
