import { TimeSeries, OSDTimeSeries } from './model';
import { FrequencyBand } from '../common/model';
import { Waveform } from '../waveform/model';

// These interfaces match exactly the OSD commonobjects definitions which will be useful to unify on as
// the OSD gets hardened.

/**
 * Phase type list
 */
export enum PhaseType {
    // TODO: need to elaborate with full set of phase labels
    P = 'P',
    S = 'S',
    P3KPbc = 'P3KPbc',
    P4KPdf_B = 'P4KPdf_B',
    P7KPbc = 'P7KPbc',
    P7KPdf_D = 'P7KPdf_D',
    PKiKP = 'PKiKP',
    PKKSab = 'PKKSab',
    PKP2bc = 'PKP2bc',
    PKP3df_B = 'PKP3df_B',
    PKSab = 'PKSab',
    PP_1 = 'PP_1',
    pPKPbc = 'pPKPbc',
    PS = 'PS',
    Rg = 'Rg',
    SKiKP = 'SKiKP',
    SKKSac = 'SKKSac',
    SKPdf = 'SKPdf',
    SKSdf = 'SKSdf',
    sPdiff = 'sPdiff',
    SS = 'SS',
    sSKSdf = 'sSKSdf',
    Lg = 'Lg',
    P3KPbc_B = 'P3KPbc_B',
    P5KPbc = 'P5KPbc',
    P7KPbc_B = 'P7KPbc_B',
    Pb = 'Pb',
    PKKP = 'PKKP',
    PKKSbc = 'PKKSbc',
    PKP2df = 'PKP2df',
    PKPab = 'PKPab',
    PKSbc = 'PKSbc',
    PP_B = 'PP_B',
    pPKPdf = 'pPKPdf',
    PS_1 = 'PS_1',
    SKKP = 'SKKP',
    SKKSac_B = 'SKKSac_B',
    SKS = 'SKS',
    SKSSKS = 'SKSSKS',
    sPKiKP = 'sPKiKP',
    SS_1 = 'SS_1',
    SSS = 'SSS',
    nNL = 'nNL',
    P3KPdf = 'P3KPdf',
    P5KPbc_B = 'P5KPbc_B',
    P7KPbc_C = 'P7KPbc_C',
    PcP = 'PcP',
    PKKPab = 'PKKPab',
    PKKSdf = 'PKKSdf',
    PKP3 = 'PKP3',
    PKPbc = 'PKPbc',
    PKSdf = 'PKSdf',
    pPdiff = 'pPdiff',
    PPP = 'PPP',
    pSdiff = 'pSdiff',
    Sb = 'Sb',
    SKKPab = 'SKKPab',
    SKKSdf = 'SKKSdf',
    SKS2 = 'SKS2',
    Sn = 'Sn',
    sPKP = 'sPKP',
    SS_B = 'SS_B',
    SSS_B = 'SSS_B',
    NP = 'NP',
    P3KPdf_B = 'P3KPdf_B',
    P5KPdf = 'P5KPdf',
    P7KPdf = 'P7KPdf',
    PcS = 'PcS',
    PKKPbc = 'PKKPbc',
    PKP = 'PKP',
    PKP3ab = 'PKP3ab',
    PKPdf = 'PKPdf',
    Pn = 'Pn',
    pPKiKP = 'pPKiKP',
    PPP_B = 'PPP_B',
    pSKS = 'pSKS',
    ScP = 'ScP',
    SKKPbc = 'SKKPbc',
    SKP = 'SKP',
    SKS2ac = 'SKS2ac',
    SnSn = 'SnSn',
    sPKPab = 'sPKPab',
    sSdiff = 'sSdiff',
    NP_1 = 'NP_1',
    P4KPbc = 'P4KPbc',
    P5KPdf_B = 'P5KPdf_B',
    P7KPdf_B = 'P7KPdf_B',
    Pdiff = 'Pdiff',
    PKKPdf = 'PKKPdf',
    PKP2 = 'PKP2',
    PKP3bc = 'PKP3bc',
    PKPPKP = 'PKPPKP',
    PnPn = 'PnPn',
    pPKP = 'pPKP',
    PPS = 'PPS',
    pSKSac = 'pSKSac',
    ScS = 'ScS',
    SKKPdf = 'SKKPdf',
    SKPab = 'SKPab',
    SKS2df = 'SKS2df',
    SP = 'SP',
    sPKPbc = 'sPKPbc',
    sSKS = 'sSKS',
    P4KPdf = 'P4KPdf',
    P5KPdf_C = 'P5KPdf_C',
    P7KPdf_C = 'P7KPdf_C',
    Pg = 'Pg',
    PKKS = 'PKKS',
    PKP2ab = 'PKP2ab',
    PKP3df = 'PKP3df',
    PKS = 'PKS',
    PP = 'PP',
    pPKPab = 'pPKPab',
    PPS_B = 'PPS_B',
    pSKSdf = 'pSKSdf',
    Sdiff = 'Sdiff',
    SKKS = 'SKKS',
    SKPbc = 'SKPbc',
    SKSac = 'SKSac',
    SP_1 = 'SP_1',
    sPKPdf = 'sPKPdf',
    sSKSac = 'sSKSac',
    Sx = 'Sx',
    tx = 'tx',
    N = 'N',
    Px = 'Px',
    PKhKP = 'PKhKP',
    UNKNOWN = 'UNKNOWN'
}

/**
 * Fk power spectras
 */
export interface FkPowerSpectra extends TimeSeries {
    windowLead: number;
    windowLength: number;
    phaseType: PhaseType;
    lowFrequency: number;
    highFrequency: number;
    stepSize: number;
    xSlowStart: number;
    xSlowCount: number;
    xSlowDelta: number;
    ySlowStart: number;
    ySlowCount: number;
    ySlowDelta: number;
    reviewed: boolean;
    spectrums: FkPowerSpectrum[];
    leadSpectrum: FkPowerSpectrum;
    fstatData: FstatData;
    configuration: FkConfiguration;
}

/**
 * Fk power spectra OSD representation
 */
export interface FkPowerSpectraOSD extends OSDTimeSeries {
    windowLead: string;
    windowLength: string;
    phaseType: PhaseType;
    lowFrequency: number;
    highFrequency: number;
    xSlowStart: number;
    xSlowCount: number;
    xSlowDelta: number;
    ySlowStart: number;
    ySlowCount: number;
    ySlowDelta: number;
    values: FkPowerSpectrumOSD[];
}

/**
 * FstatData for plots in UI and so we don't return the spectrum list
 */
export interface FstatData {
    azimuthWf: Waveform;
    slownessWf: Waveform;
    fstatWf: Waveform;
}

/**
 * Fk power spectrum part of the FkPowerSpectra definition
 */
export interface FkPowerSpectrum {
    power: number[][];
    fstat: number[][];
    quality: number;
    attributes: FkAttributes;
}

/**
 * Fk power spectrum OSD representation
 */
export interface FkPowerSpectrumOSD {
    power: number[][];
    fstat: number[][];
    quality: number;
    attributes: FkAttributesOSD[];
}

/**
 * Fk attributes OSD representation
 */
export interface FkAttributesOSD {
    peakFStat: number;
    xSlow: number;
    ySlow: number;
    azimuth: number;
    slowness: number;
    azimuthUncertainty: number;
    slownessUncertainty: number;
}

/**
 * Fk attributes part of the FkPowerSpectrum definition
 */
export interface FkAttributes {
    peakFStat: number;
    azimuth: number;
    slowness: number;
    azimuthUncertainty: number;
    slownessUncertainty: number;
}

/**
 * Represents the time window parameters used in the Fk cacluation
 */
export interface WindowParameters {
    windowType: string;
    leadSeconds: number;
    lengthSeconds: number;
    stepSize: number;
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
 * Input type for creating new Fks
 */
export interface FkInput {
    stationId: string;
    signalDetectionId: string;
    signalDetectionHypothesisId: string;
    phase: string;
    frequencyBand: FrequencyBand;
    windowParams: WindowParameters;
    configuration: FkConfiguration;
}

/**
 * Input type for creating new Beam
 */
export interface BeamInput {
    signalDetectionId: string;
    windowParams: WindowParameters;
}

/**
 * Input type for UI to API Gateway to set
 * Fk as reviewed or not
 */
export interface MarkFksReviewedInput {
    signalDetectionIds: string[];
    reviewed: boolean;
}

/**
 * Input type for Compute FK service call. This input
 * is compatiable with the OSD input i.e. start/end are strings
 */
export interface ComputeFkInput {
        startTime: string;
        sampleRate: number;
        channelIds: string[];
        windowLead: string;
        windowLength: string;
        lowFrequency: number;
        highFrequency: number;
        useChannelVerticalOffset: boolean;
        phaseType: string;
        normalizeWaveforms: boolean;
        outputChannelId: string;
        // Optional fields
        slowStartX?: number;
        slowDeltaX?: number;
        slowCountX?: number;
        slowStartY?: number;
        slowDeltaY?: number;
        slowCountY?: number;
        sampleCount: number;
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
    contributingChannelsConfiguration: ContributingChannelsConfiguration[];
    leadFkSpectrumSeconds: number;
}
