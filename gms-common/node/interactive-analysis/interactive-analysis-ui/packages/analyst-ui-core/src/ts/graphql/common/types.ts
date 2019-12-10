// ***************************************
// Mutations
// ***************************************

// ***************************************
// Subscriptions
// ***************************************

// ***************************************
// Queries
// ***************************************

// ***************************************
// Model
// ***************************************

/**
 * Creation Type, reflects system change or analyst change
 */
export enum CreatorType {
  Analyst = 'Analyst',
  System = 'System'
}

/**
 * Distance value's units degrees or kilometers
 */
export enum DistanceUnits {
  degrees = 'degrees',
  km = 'km'
}

/**
 * Distance to source type
 */
export enum DistanceSourceType {
  Event = 'Event',
  UserDefined = 'UserDefined'
}

/**
 * Time range in epoch seconds
 */
export interface TimeRange {
  startTime: number;
  endTime: number;
}

/**
 * Location information
 */
export interface Location {
  latDegrees: number;
  lonDegrees: number;
  elevationKm: number;
}

/**
 * Position information relative to a location
 */
export interface Position {
  northDisplacementKm: number;
  eastDisplacementKm: number;
  verticalDisplacementKm: number;
}

/**
 * Log Level to determine different levels
 */
export enum LogLevel {
  INFO = 'INFO',
  DEBUG = 'DEBUG',
  WARNING = 'WARNING',
  ERROR = 'ERROR',
  DATA = 'DATA'
}

/**
 * Client Log Input
 */
export interface ClientLogInput {
  logLevel: LogLevel;
  message: string;
  time?: string;
}

/**
 * Client log mutation arguments
 */
export interface ClientLogMutationArgs {
  input: ClientLogInput;
}

/**
 * Creation Info
 */
export interface CreationInfo {
  id: string;
  creationTime: number;
  creatorId: string;
  creatorType: CreatorType;
  creatorName: string;
}

/**
 * Units used in DoubleValue part of feature prediction
 */
export enum Units {
    DEGREES = 'DEGREES',
    SECONDS= 'SECONDS',
    SECONDS_PER_DEGREE = 'SECONDS_PER_DEGREE',
    UNITLESS = 'UNITLESS'
}

/**
 * ENUM list of phase types
 */
export enum PhaseType {
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
