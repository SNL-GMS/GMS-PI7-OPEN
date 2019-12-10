import { ProcessingChannel } from '../station/model';

/**
 * Represents a Waveform Filter
 */
export interface WaveformFilter {
    id: string;
    name: string;
    description: string;
    filterType: string; // FIR_HAMMING
    filterPassBandType: string; // BAND_PASS, HIGH_PASS
    lowFrequencyHz: number;
    highFrequencyHz: number;
    order: number;
    filterSource: string; // SYSTEM
    filterCausality: string; // CAUSAL
    zeroPhase: boolean;
    sampleRate: number;
    sampleRateTolerance: number;
    validForSampleRate: boolean;
    aCoefficients: number[] | null;
    bCoefficients: number[] | null;
    groupDelaySecs: number | null;
}

/**
 * Filter processing channel
 */
export interface FilterProcessingChannel extends ProcessingChannel {
    sourceChannelId: string;
    filterParamsId: string;
}
