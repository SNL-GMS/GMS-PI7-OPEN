
export enum ChannelSegmentType {
    ACQUIRED = 'ACQUIRED',
    RAW = 'RAW',
    DETECTION_BEAM = 'DETECTION_BEAM',
    FK_BEAM = 'FK_BEAM',
    FILTER = 'FILTER'
}

export enum TimeSeriesType {
    WAVEFORM = 'WAVEFORM',
    FK_SPECTRA = 'FK_SPECTRA',
    DETECTION_FEATURE_MAP = 'DETECTION_FEATURE_MAP'
}

export interface TimeSeries {
    startTime: number;
    sampleRate: number;
    sampleCount: number;
}

export interface ChannelSegment<T extends TimeSeries> {
    id: string;
    channelId: string;
    name?: string;
    type: ChannelSegmentType;
    timeseriesType: TimeSeriesType;
    startTime: number;
    endTime: number;
    timeseries: T[];
  }
