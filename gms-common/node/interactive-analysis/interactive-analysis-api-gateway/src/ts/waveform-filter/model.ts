import { WaveformFilter } from '../waveform-filter-definition/model';
import { ChannelSegment, OSDChannelSegment } from '../channel-segment/model';
import { OSDWaveform, Waveform } from '../waveform/model';

/**
 * Calculate waveform segment input
 */
export interface CalculateWaveformSegmentInput {
    channelSegments: OSDChannelSegment<OSDWaveform>[];
    // can't serialize a map to json
    // object represents a map of channel id to output channel id
    inputToOutputChannelIds: Object;
    pluginParams: WaveformFilter;
}

/**
 * Filtered waveform channel segment which extends ChannelSegment
 */
export interface FilteredWaveformChannelSegment extends ChannelSegment<Waveform> {

// export interface FilteredChannelSegment<T extends Waveform> extends ChannelSegment<T> {
    sourceChannelId: string;
    wfFilterId: string;
}

/**
 * Data Structure to return mapping of FilterId and Channel Segment Id
 * Used by Waveform Filter processor to call (for now) for derived Filtered
 * Channel Segments for FK_BEAM and ACQUIRED.
 */
export interface DerivedFilterChannelSegmentId {
    csId: string;
    wfFiltertId: string;
}
/**
 * Raw and filter channel segments
 */
export interface RawAndFilteredChannelSegments {
    channelId: string;
    raw: ChannelSegment<Waveform>[];
    filtered: FilteredWaveformChannelSegment[];
}
