import { TimeSeries } from '~graphql/channel-segment/types';
import { Waveform } from '~graphql/waveform/types';
import { SignalDetectionTypes } from '../';

// ***************************************
// Mutations
// ***************************************

export interface FkInput {
  stationId: string;
  signalDetectionId: string;
  signalDetectionHypothesisId: string;
  phase: string;
  frequencyBand: {
      minFrequencyHz: number;
      maxFrequencyHz: number;
  };
  windowParams: {
      windowType: string;
      leadSeconds: number;
      lengthSeconds: number;
      stepSize: number;
  };
  configuration: FkConfiguration;
}

/**
 * Params to mark fks review
 */
export interface MarkFksReviewedInput {
   signalDetectionIds: string[];
   reviewed: boolean;
 }

export interface ComputeFksMutationArgs {
  input: FkInput[];
}

export interface ComputeFrequencyFkThumbnailsInput {
  fkInput: FkInput;
}

/**
 * Arguments provided to the mark fks reviewed mutation
 */
export interface MarkFksReviewedMutationArgs {
  markFksReviewedInput: MarkFksReviewedInput;
}

export interface ComputeBeamMutationArgs {
  signalDetectionId: string;
}

/**
 * Return the Fk with
 * Lead FkSpectrum set based on window lead seconds 
 */
export interface SetFkWindowLeadMutationArgs {
  signalDetectionId: string;

  // Window Lead Seconds and Lead Fk Spectrum Select Seconds (movie)
  leadFkSpectrumSeconds: number;
  windowLeadSeconds: number; // If lead is changed thru the widget
}

// ***************************************
// Subscriptions
// ***************************************

/**
 * Data structure for detectionUpdated subscription callback
 */
export interface FksCreatedSubscription {
  fksCreated: SignalDetectionTypes.SignalDetection[];
}

// ***************************************
// Queries
// ***************************************

// ***************************************
// Model
// ***************************************

export interface FrequencyBand {
  minFrequencyHz: number;
  maxFrequencyHz: number;
}

export interface WindowParameters {
  leadSeconds: number;
  lengthSeconds: number;
  stepSize: number;
}

export interface FstatData {
  azimuthWf: Waveform;
  slownessWf: Waveform;
  fstatWf: Waveform;
}

export interface FkPowerSpectra extends TimeSeries {
  // TO ADD
  // channel segment id
  id: string;
  contribChannels: {
    id: string;
    name: string;
    site: {
      name: string;
    };
  }[];
  // TO CONSIDER
  // windowType:string = 'hanning'
  // TO KEEP
  startTime: number;
  sampleRate: number;
  sampleCount: number;
  windowLead: number;
  windowLength: number;
  stepSize: number;
  phaseType: string;
  lowFrequency: number;
  highFrequency: number;
  xSlowStart: number;
  xSlowCount: number;
  xSlowDelta: number;
  ySlowStart: number;
  ySlowCount: number;
  ySlowDelta: number;
  reviewed: boolean;
  leadSpectrum: FkPowerSpectrum;
  peakSpectrum?: FkPowerSpectrum;
  fstatData: FstatData;
  configuration: FkConfiguration;
}

export interface FkPowerSpectrum {
  power: number[][];
  fstat: number[][];
  quality: number;
  attributes: FkAttributes;
  configuration: FkConfiguration;
}

export interface FkAttributes {
  peakFStat: number;
  azimuth: number;
  slowness: number;
  azimuthUncertainty: number;
  slownessUncertainty: number;
}
/**
 * FkFrequencyThumbnail preview Fk at a preset FrequencyBand
 */
export interface FkFrequencyThumbnail {
  frequencyBand: FrequencyBand;
  fkSpectra: FkPowerSpectra;
}

/**
 * Collection of thumbnails by signal detection id
 */
export interface FkFrequencyThumbnailBySDId {
  signalDetectionId: string;
  fkFrequencyThumbnails: FkFrequencyThumbnail[];
}

/**
 * Tracks whether a channel is used to calculate fk
 */

export interface ContributingChannelsConfiguration {
  id: string;
  enabled: boolean;
  name: string;
}
/**
 * Holds the configuration used to calculate an Fk
 */
export interface FkConfiguration {
  maximumSlowness: number;
  mediumVelocity: number;
  numberOfPoints: number;
  normalizeWaveforms: boolean;
  useChannelVerticalOffset: boolean;
  leadFkSpectrumSeconds: number;
  contributingChannelsConfiguration: ContributingChannelsConfiguration[];
}
