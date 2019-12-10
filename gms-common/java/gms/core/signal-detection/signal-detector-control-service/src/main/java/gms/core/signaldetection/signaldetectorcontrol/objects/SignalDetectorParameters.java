package gms.core.signaldetection.signaldetectorcontrol.objects;


import com.google.auto.value.AutoValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.RegistrationInfo;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * Signal Detector Control Plugin Configuration data object. This object is used to control which
 * signalDetectorPlugins are used for a specific processing channel requested by the
 * SignalDetectorControl object.
 */
@AutoValue
public abstract class SignalDetectorParameters {

  /**
   * Factory method to create new SignalDetectorParameters objects.
   *
   * @param processingChannelId The processing channel id for this configuration (not null).
   * @param signalDetectorPlugins The list of plugin {@link RegistrationInfo} objects defined by
   */
  public static SignalDetectorParameters create(UUID processingChannelId,
      List<RegistrationInfo> signalDetectorPlugins,
      RegistrationInfo onsetTimeUncertaintyPlugin,
      Optional<RegistrationInfo> onsetTimeRefinementPlugin) {

    return new AutoValue_SignalDetectorParameters(processingChannelId,
        signalDetectorPlugins,
        onsetTimeUncertaintyPlugin,
        onsetTimeRefinementPlugin);
  }

  public abstract UUID getProcessingChannelId();

  /**
   * Return a stream of {@link RegistrationInfo} objects (Plugin identities) defined by this
   * configuration.
   *
   * @return A stream of {@link RegistrationInfo} objects.
   */
  public Stream<RegistrationInfo> signalDetectorPlugins() {
    return getSignalDetectorPlugins().stream();
  }

  /**
   * Return the list of {@link RegistrationInfo} objects (Plugin Identities) defined by this
   * configuration.
   *
   * @return A list of {@link RegistrationInfo} objects.
   */
  public abstract List<RegistrationInfo> getSignalDetectorPlugins();

  public abstract RegistrationInfo getOnsetTimeUncertaintyPlugin();

  public abstract Optional<RegistrationInfo> getOnsetTimeRefinementPlugin();
}