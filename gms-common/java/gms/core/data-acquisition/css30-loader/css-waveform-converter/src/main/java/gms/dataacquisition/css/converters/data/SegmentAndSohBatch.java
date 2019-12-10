package gms.dataacquisition.css.converters.data;


import com.google.auto.value.AutoValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.AcquiredChannelSohBoolean;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Ridiculously simple class that groups a Set of ChannelSegment and a Set of
 * AcquiredChannelSohBoolean. Both are immutable - using Collections.unmodifiableSet. The only
 * purpose of the class is to group these two things (both can come from a Wfdisc row) for a
 * convenient return type. Created by jwvicke on 10/19/17.
 */
@AutoValue
public abstract class SegmentAndSohBatch {

  public abstract Set<ChannelSegment<Waveform>> getSegments();
  public abstract Set<AcquiredChannelSohBoolean> getSohs();
  public abstract Map<UUID, WfdiscSampleReference> getIdToW();

  /**
   * Create a new a SegmentAndSohBatch.
   *
   * @param segments Waveform channel segment set
   * @param sohs State of health set
   * @param idToW Map from ID to waveform file (w) via WfdiscSampleReference object
   * @return A SegmentAndSohBatch object.
   */
  public static SegmentAndSohBatch from(Set<ChannelSegment<Waveform>> segments, 
      Set<AcquiredChannelSohBoolean> sohs, Map<UUID, WfdiscSampleReference> idToW) {
    return new AutoValue_SegmentAndSohBatch(
        Collections.unmodifiableSet(segments), 
        Collections.unmodifiableSet(sohs),
        Collections.unmodifiableMap(idToW));
  }
}
