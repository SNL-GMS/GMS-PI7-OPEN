import * as Entities from '../../../../../../../../entities';

export interface WaveformRendererProps {
  /** Epoch seconds start time */
  displayStartTimeSecs: number;

  /** Boolean is default channel */
  displayEndTimeSecs: number;

  /** Id of channel segment */
  channelSegmentId: string;

  /** Collection of channel segments */
  channelSegments: Map<string, Entities.ChannelSegment>;

  /** Collection of masks */
  masks?: Entities.Mask[];

  /** Web Workers */
  workerRpcs: any[];

  // Callbacks

  /**
   * Sets the Y axis bounds
   * 
   * @param min minimum bound as a number
   * @param max Maximum bound as a number
   */
  setYAxisBounds(min: number, max: number);

  /** Issues a re render */
  renderWaveforms(): void;
}

// tslint:disable-next-line:no-empty-interface
export interface WaveformRendererState {
}

/** Channel Segment Boundries */
export interface ChannelSegmentBoundries {
  /** Maximum value of top */
  topMax: number;

  /** Maximum value of bottome */
  bottomMax: number;

  /** Average of channel */
  channelAvg: number;

  /** Offset of channel */
  offset: number;

  /** Channel segment id */
  channelSegmentId: string;
}

export interface Float32ArrayWithStartTime {
  /** Epoch seconds start time of data */
  startTimeSecs: number;

  /** Sample Rate */
  sampleRate: number;

  /** Color */
  color?: string;

  /** Display type */
  displayType?: Entities.DisplayType[];

  /** Point size */
  pointSize?: number;

  /** Waveform data */
  float32Array: Float32Array;
}
