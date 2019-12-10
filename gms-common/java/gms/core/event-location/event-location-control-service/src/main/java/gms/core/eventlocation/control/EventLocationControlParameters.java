package gms.core.eventlocation.control;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import gms.core.eventlocation.plugins.EventLocationDefinition;
import gms.shared.mechanisms.pluginregistry.PluginInfo;
import java.util.Objects;
import java.util.Optional;

/**
 * Specifies an EventLocatorPlugin implementation by name and version
 */
@AutoValue
public abstract class EventLocationControlParameters {

  @JsonCreator
  public static EventLocationControlParameters create(
      @JsonProperty("pluginInfo") PluginInfo pluginInfo,
      @JsonProperty("eventLocationDefinition") EventLocationDefinition eventLocationDefinition) {

    Optional<PluginInfo> optionalPluginInfo =
        Objects.nonNull(pluginInfo) ? Optional.of(pluginInfo) : Optional.empty();

    Optional<EventLocationDefinition> optionalEventLocationDefinition =
        Objects.nonNull(eventLocationDefinition) ? Optional.of(eventLocationDefinition)
            : Optional.empty();

    return new AutoValue_EventLocationControlParameters(optionalPluginInfo,
        optionalEventLocationDefinition);
  }


  public static EventLocationControlParameters create(
      @JsonProperty("pluginInfo") PluginInfo pluginInfo
  ) {

    Objects.requireNonNull(pluginInfo, "Null pluginInfo");

    return new AutoValue_EventLocationControlParameters(Optional.of(pluginInfo), Optional.empty());
  }


  public static EventLocationControlParameters create(
      @JsonProperty("eventLocationDefinition") EventLocationDefinition eventLocationDefinition
  ) {

    Objects.requireNonNull(eventLocationDefinition, "Null eventLocationDefinition");

    return new AutoValue_EventLocationControlParameters(Optional.empty(),
        Optional.of(eventLocationDefinition));
  }

  public abstract Optional<PluginInfo> getPluginInfo();

  public abstract Optional<EventLocationDefinition> getEventLocationDefinition();
}
