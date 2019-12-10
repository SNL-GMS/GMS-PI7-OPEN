import { GraphqlQueryControls } from 'react-apollo';
import { CommonTypes } from '../';

// ***************************************
// Mutations
// ***************************************

// ***************************************
// Subscriptions
// ***************************************

// ***************************************
// Queries
// ***************************************

// tslint:disable-next-line:max-line-length interface-over-type-literal
export type DefaultStationsQueryProps = { defaultStationsQuery: GraphqlQueryControls<{}> & { defaultStations: ProcessingStation[] } };

export interface DistanceToSourceInput {
  sourceType?: string;
  sourceId?: string;
  sourceLocation?: CommonTypes.Location;
  distanceUnits?: CommonTypes.DistanceUnits;
}

export interface DistanceToSourceForDefaultStationsQueryArgs {
  distanceToSourceInput: DistanceToSourceInput;
}

// tslint:disable-next-line:max-line-length interface-over-type-literal
export type DistanceToSourceForDefaultStationsQueryProps = { distanceToSourceForDefaultStationsQuery: GraphqlQueryControls<{}> & { distanceToSourceForDefaultStations: DistanceToSource[] } };

// ***************************************
// Model
// ***************************************

export interface DataAcquisition {
  dataAcquisition: string;
  interactiveProcessing: string;
  automaticProcessing: string;
  acquisition: string;
  pkiStatus: string;
  pkiInUse: string;
  processingPartition: string;
  storeOnDataAcquisitionPartition: string;
}

export interface DistanceToSource {
  distance: number;
  distanceUnits: CommonTypes.DistanceUnits;
  sourceType: CommonTypes.DistanceSourceType;
  sourceId: string;
  sourceLocation: CommonTypes.Location;
  stationId: string;
}

export interface ProcessingChannel {
  id: string;
  name?: string;
  channelType: string;
  sampleRate: number;
  position?: CommonTypes.Position;
  actualTime?: string;
  systemTime?: string;
  depth?: number;
}

export interface ProcessingSite {
  id: string;
  name?: string;
  channels: ProcessingChannel[];
  location: CommonTypes.Location;
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

export interface ProcessingStation {
  id: string;
  name?: string;
  stationType: StationType;
  description: string;
  defaultChannel: ProcessingChannel;
  networks: {
    id: string;
    name: string;
    monitoringOrganization: string;
  }[];
  modified: boolean;
  location: CommonTypes.Location;
  sites: ProcessingSite[];
  dataAcquisition: DataAcquisition;
  latitude: number;
  longitude: number;
  elevation: number;
}
