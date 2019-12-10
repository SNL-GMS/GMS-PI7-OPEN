import { SignalDetection } from '../signal-detection/model';
import { Event } from '../event/model';

/**
 * Common model definitions shared across gateway data APIs
 */

/**
 * Represents a location specified using latitude (degrees), longitude (degrees),
 * and altitude (kilometers).
 */
export interface Location {
    latDegrees: number;
    lonDegrees: number;
    elevationKm: number;
    depthKm?: number;
}

/**
 * Represents a frequency range
 */
export interface FrequencyBand {
    minFrequencyHz: number;
    maxFrequencyHz: number;
}

/**
 * Time range
 */
export interface TimeRange {
    startTime: number;
    endTime: number;
}

/**
 * Represents events and sds to publish
 */
export interface AssociationChange {
    events: Event[];
    sds: SignalDetection[];
}

/**
 * Used when we need to return the original detections mutations where performed on
 * in addition to the other data changed
 */
export interface DetectionAndAssociationChange {
    detections: SignalDetection[];
    associationChange: AssociationChange;
}

/**
 * Used when we need to return the original event mutated where performed on
 * in addition to the other data changed
 */
export interface EventAndAssociationChange {
    event: Event;
    associationChange: AssociationChange;
}

/**
 * Enumeration representing the different types of stations in the monitoring network.
 */
export enum StationType {
    Seismic3Component = 'Seismic3Component',
    Seismic1Component = 'Seismic1Component',
    SeismicArray = 'SeismicArray',
    Hydroacoustic = 'Hydroacoustic',
    HydroacousticArray = 'HydroacousticArray',
    Infrasound = 'Infrasound',
    InfrasoundArray = 'InfrasoundArray',
    Weather = 'Weather',
    UNKNOWN = 'UNKNOWN'
}

/**
 * Enumerated set of channel types
 */
export enum ChannelType {
    BroadbandHighGainVertical = 'BroadbandHighGainVertical',
    ShortPeriodLowGainVertical = 'ShortPeriodLowGainVertical',
    ShortPeriodHighGainVertical = 'ShortPeriodHighGainVertical',
    BroadbandHighGainEastWest = 'BroadbandHighGainEastWest',
    ShortPeriodLowGainEastWest = 'ShortPeriodLowGainEastWest',
    ShortPeriodHighGainEastWest = 'ShortPeriodHighGainEastWest',
    BroadbandHighGainNorthSouth = 'BroadbandHighGainNorthSouth',
    ShortPeriodLowGainNorthSouth = 'ShortPeriodLowGainNorthSouth',
    ShortPeriodHighGainNorthSouth = 'ShortPeriodHighGainNorthSouth',
    ExtremelyShortPeriodHydrophone = 'ExtremelyShortPeriodHydrophone',
    ExtremelyShortPeriodHighGainEastWest = 'ExtremelyShortPeriodHighGainEastWest',
    ExtremelyShortPeriodHighGainNorthSouth = 'ExtremelyShortPeriodHighGainNorthSouth',
    ExtremelyShortPeriodHighGainVertical = 'ExtremelyShortPeriodHighGainVertical',
    HighBroadbandHighGainEastWest = 'HighBroadbandHighGainEastWest',
    HighBroadbandHighGainNorthSouth = 'HighBroadbandHighGainNorthSouth',
    HighBroadbandHighGainVertical = 'HighBroadbandHighGainVertical',
    MidPeriodHighGainEastWest = 'MidPeriodHighGainEastWest',
    MidPeriodHighGainNorthSouth = 'MidPeriodHighGainNorthSouth',
    MidPeriodHighGainVertical = 'MidPeriodHighGainVertical'
}

/**
 * Enumerated type of the actor (e.g. analyst, system) associated with a CreationInfo object
 */
export enum CreatorType {
    Analyst = 'Analyst',
    System = 'System'
}

/**
 * Provenance information about the results of data processing completed by the System
 * and actions completed by users of the system
 */
export interface CreationInfo {
    id: string;
    creationTime: number;
    creatorId: string;
    creatorType: CreatorType;
    creatorName: string;

    // TODO
    // The processing step associated with the processing result
    // processingStepId: string;

    // TODO
    // The software component associated with the processing result
    // softwareComponentId: string!
}

/**
 * Creation info OSD representation
 */
export interface OSDCreationInfo {
    id?: string;
    creationTime: string;
    creatorId?: string;
    creatorType?: CreatorType;
    creatorName: string;

    softwareInfo: {
        name: string;
        version: string;
    };
}

/**
 * Software info
 */
export interface SoftwareInfo {
    name: string;
    version: string;
}

/**
 * Processing Context part of data structure that filtered waveform
 * control service uses to tell who/where the request came from. In our case
 * from Interactive UI user
 */
export interface ProcessingContext {
    analystActionReference: AnalystActionReference;
    processingStepReference: ProcessingStepReference;
    storageVisibility: string;
}

/**
 * Analyst action refeerence
 */
export interface AnalystActionReference {
    processingStageIntervalId: string;
    processingActivityIntervalId: string;
    analystId: string;
}

/**
 * Processing step reference
 */
export interface ProcessingStepReference {
    processingStageIntervalId: string;
    processingSequenceIntervalId: string;
    processingStepId: string;
}

/**
 * Test data paths used when reading in data
 */
export interface TestDataPaths {
    dataHome: string;
    jsonHome: string;
    fpHome: string;
    fkHome: string;
    channelsHome: string;
    additionalDataHome: string;
}

/**
 * Represents calibration information associated with a waveform
 */
export interface ProcessingCalibration {
    factor: number;
    factorError: number;
    period: number;
    timeShift: number;
}

/**
 * Represents the configured type of data source the API Gateway provides access to - values:
 * Local - The API gateway loads data from local file storage for testing purposes
 * Service - The API gateway uses services to provide access to backend (e.g. OSD) data
 */
export enum AccessorDataSource {
    Local = 'Local',
    Service = 'Service'
}

/**
 * Enumerated list of source types used to compute distances to
 */
export enum DistanceSourceType {
    Event = 'Event',
    UserDefined = 'UserDefined'
}

/**
 * Distance value's units degrees or kilometers
 */
export enum DistanceUnits {
    degrees = 'degrees',
    km = 'km'
}

/**
 * Represents a distance measurement relative to a specified source location
 */
export interface DistanceToSource {

    // The distance in kilometers or degrees
    distance: number;

    // The units distance was calculated
    distanceUnits: DistanceUnits;

    // The source location
    sourceLocation: Location;

    // The type of the source the distance is measured to (e.g. and event)
    sourceType: DistanceSourceType;

    // Optional: the unique ID of the source object
    sourceId?: string;

    // Which station distance to the source
    stationId: string;
}

/**
 * Log level used to determine when the log statements display
 */
export enum LogLevel {
  INFO = 'INFO',
  DEBUG = 'DEBUG',
  WARNING = 'WARNING',
  ERROR = 'ERROR',
  DATA = 'DATA'
}

/**
 * Client log, object that describes the client log message
 */
export interface ClientLog {
  logLevel: LogLevel;
  message: string;
  time?: string;
}
