import { GraphqlQueryControls } from 'react-apollo';
import { StationType } from '~graphql/station/types';

// ***************************************
// Mutations
// ***************************************

export interface SaveReferenceStationMutationArgs {
  input: ReferenceStation;
}

/**
 * Represents a Reference Station
 */
export interface ReferenceStation {
  name: string;
  description: string;
  stationType: StationType;
  comment: string;
  source: InformationSource;
  latitude: number;
  longitude: number;
  elevation: number;
  actualChangeTime: string;
  systemChangeTime: string;
  aliases: ReferenceAlias[];
}

/**
 * Represents an InformationSource
 */
export interface InformationSource {
  originatingOrganization: string;
  informationTime: string;
  reference: string;
}

/**
 * Represents a ReferenceAlias
 */
export interface ReferenceAlias {
  id: string;
  name: string;
  status: StatusType;
  comment: string;
  actualChangeTime: string;
  systemChangeTime: string;
}

/**
 * Status Types
 */
export enum StatusType {
  Inactive = 'INACTIVE',
  Active = 'ACTIVE'
}

// ***************************************
// Subscriptions
// ***************************************

// ***************************************
// Queries
// ***************************************

// tslint:disable-next-line:max-line-length interface-over-type-literal
export type  TransferredFilesByTimeRangeQueryProps =  {  transferredFilesByTimeRangeQuery: GraphqlQueryControls<{}> & {transferredFilesByTimeRange: FileGap[]}};

/** Arguments for Transferred Files By Time Range Query */
export interface TransferredFilesByTimeRangeQueryArgs {
  timeRange: {
    startTime: number;
    endTime: number;
  };
}

// ***************************************
// Model
// ***************************************

/**
 * Represents the transfer gap fields, current place holder
 * may be moved or renamed in future
 */
export interface FileGap {
  stationName: string;
  channelNames: string[];
  startTime: string;
  endTime: string;
  duration: string;
  location: string;
  priority: string;
}

export interface StatusConfiguration {
  modified: boolean;
}

export interface StationInformation {
  modified: boolean;
}
