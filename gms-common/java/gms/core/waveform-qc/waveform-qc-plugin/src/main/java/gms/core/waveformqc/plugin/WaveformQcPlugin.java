package gms.core.waveformqc.plugin;

import gms.core.waveformqc.plugin.objects.ChannelSohStatusSegment;
import gms.shared.frameworks.pluginregistry.Plugin;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMask;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegmentDescriptor;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Plugin interface for creating waveform quality control masks
 */
public interface WaveformQcPlugin extends Plugin {

  /**
   * Generates a sequence of QC masks
   * @param waveforms The collection of waveforms to be masked
   * @param sohStatusChanges The channel SOH data for the waveform data
   * @param qcMasks Previously created {@link QcMask}s that can affect processing.
   * @param parameterFieldMap
   */
  List<QcMask> generateQcMasks(ChannelSegment<Waveform> waveforms,
      Collection<ChannelSohStatusSegment> sohStatusChanges,
      Collection<QcMask> qcMasks, Map<String, Object> parameterFieldMap);


  /**
   * Creates a {@link ChannelSegmentDescriptor} the plugin needs for processing, given the input
   * request descriptor and plugin parameters.
   *
   * @param requestDescriptor The initial data descriptor to be processed
   * @param pluginParams The plugin-specific parameters needed for processing
   * @return A {@link ChannelSegmentDescriptor} representing the full range of data the plugin will
   * need in order to process the requestDescriptor
   */
  default ChannelSegmentDescriptor getProcessingDescriptor(
      ChannelSegmentDescriptor requestDescriptor,
      Map<String, Object> pluginParams) {
    return requestDescriptor;
  }


}
