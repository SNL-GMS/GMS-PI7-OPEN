import { Location, StationType } from '../common/model';

/**
 * Model definitions for the processing network/station/site/channel data API
 */

/**
 * Represents a group of stations used for monitoring.
 * This is a simplified interface used for processing. A richer interface modeling
 * station reference information is provided in the station reference API.
 */
export interface ProcessingNetwork {
    id: string;
    name: string;
    monitoringOrganization: string;
    stationIds: string[];
}

/**
 * Represents the station information fields, current place holder
 * may be moved or renamed in future.
 */
export interface DataAcquisition {
    dataAcquisition: string;
    interactiveProcessing: string;
    automaticProcessing: string;
}

/**
 * Represents an installation of monitoring sensors for the purposes of processing.
 * Multiple sensors can be installed at the same station.
 * This is a simplified interface used for processing. A richer interface modeling
 * station reference information is provided in the station reference API.
 */
export interface ProcessingStation {
    id: string;
    name: string;
    description: string;
    stationType: StationType;
    location: Location;
    siteIds: string[];
    networkIds: string[];
    dataAcquisition: DataAcquisition;
    latitude: number;
    longitude: number;
    elevation: number;
}

/**
 * Represents a physical installation (e.g., building, underground vault, borehole)
 * containing a collection of Instruments that produce Raw Channel waveform data.
 * This is a simplified interface used for processing. A richer interface modeling
 * station reference information is provided in the station reference API.
 */
export interface ProcessingSite {
    id: string;
    name: string;
    location: Location;
    stationId: string;
    channelIds: string[];
}

/**
 * Represents a source for unprocessed (raw) or processed (derived) time series data
 * from a seismic, hydroacoustic, or infrasonic sensor.
 * This is a simplified interface used for processing. A richer interface modeling
 * station reference information is provided in the station reference API.
 */
export interface ProcessingChannel {
    id: string;
    name: string;
    channelType: string;
    locationCode: string;
    siteId: string;
    // TODO consider removing site name once channel IDs are stable in the OSD
    siteName: string;
    dataType: string;
    latitude: number;
    longitude: number;
    elevation: number;
    verticalAngle: number;
    horizontalAngle: number;
    position: Position;
    actualTime: string;
    systemTime: string;
    sampleRate: number;
    depth: number;
}

/**
 * Represents information needed by the API gateway to retrieve the stations included
 * in the default set configured for the analyst user interface, as well as a default
 * channel to show by default for each station.
 */
export interface DefaultStationInfo {
    stationId: string;
    channelId: string;
}

/**
 * The OSD objects below differ from API Gateway model
 * because the location elements are not grouped into an object
 * and the OSD objects are missing parent pointers
 */

/**
 * Network definition returned by the OSD
 */
export interface OSDNetwork {
    id: string;
    name: string;
    organization: string;
    region: string;
    stations: OSDStation[];
}

/**
 * Station definition returned by the OSD
 */
export interface OSDStation {
    id: string;
    name: string;
    description: string;
    stationType: string;
    latitude: number;
    longitude: number;
    elevation: number;
    sites: OSDSite[];
}

/**
 * Site definition returned by the OSD
 */
export interface OSDSite {
    id: string;
    name: string;
    latitude: number;
    longitude: number;
    elevation: number;
    channels: OSDChannel[];
}

/**
 * Channel definition returned by the OSD
 */
export interface OSDChannel {
    id: string;
    name: string;
    channelType: string;
    dataType: string;
    latitude: number;
    longitude: number;
    elevation: number;
    depth: number;
    verticalAngle: number;
    horizontalAngle: number;
    position: Position;
    actualTime: string;
    systemTime: string;
    sampleRate: number;
}

/**
 * Position information relative to a location
 */
export interface Position {
    northDisplacementKm: number;
    eastDisplacementKm: number;
    verticalDisplacementKm: number;
}
