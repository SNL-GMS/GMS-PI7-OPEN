
package gms.core.signalenhancement.waveformfiltering;

import com.google.common.base.Preconditions;
import gms.core.signalenhancement.waveformfiltering.plugin.FilterPlugin;
import gms.shared.mechanisms.configuration.util.ObjectSerialization;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FilterDefinition;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.utility.WaveformUtility;
import gms.shared.utilities.signalprocessing.filter.Filter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A component wrapping a {@link LinearWaveformFilterPlugin} by implementing the {@link
 * FilterPlugin} interface. Provides a name and version number but defers logic to the
 * LinearWaveformFilterPlugin.
 */
public class LinearWaveformFilterPlugin implements FilterPlugin {

  private static final Logger logger = LoggerFactory.getLogger(LinearWaveformFilterPlugin.class);

  private static final String PLUGIN_NAME = "linearWaveformFilterPlugin";

  /**
   * Obtains this plugin component's name
   *
   * @return String, not null
   */
  @Override
  public String getName() {
    return PLUGIN_NAME;
  }

  @Override
  public Collection<Waveform> filter(ChannelSegment<Waveform> channelSegment,
      Map<String, Object> pluginParams) {

    Preconditions.checkNotNull(channelSegment,
        "LinearWaveformFilterPlugin cannot filter with null channelSegment");
    Preconditions.checkNotNull(pluginParams,
        "LinearWaveformFilterPlugin cannot filter with null pluginParams");

    FilterDefinition filterDefinition = ObjectSerialization
        .fromFieldMap(pluginParams, FilterDefinition.class);

    List<Waveform> filteredWaveforms = new ArrayList<>();
    for (Waveform waveform : WaveformUtility
        .mergeWaveforms(channelSegment.getTimeseries(), 1.0e-7, 0.5)) {

      // If one waveform errors out, then it will be skipped and keep processing
      try {
        filteredWaveforms.add(Filter.filter(waveform, filterDefinition));
      } catch (Exception e) {
        logger.error("Error filtering waveform", e);
      }
    }

    return filteredWaveforms;
  }
}