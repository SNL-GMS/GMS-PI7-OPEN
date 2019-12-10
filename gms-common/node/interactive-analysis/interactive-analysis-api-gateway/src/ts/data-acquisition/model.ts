import { StationType } from '../common/model';

/**
 * Types of MetaData
 */
export enum MetadataType {
  RAW_STATION_DATA_FRAME = 'RAW_STATION_DATA_FRAME',
  TRANSFERRED_FILE_INVOICE = 'TRANSFERRED_FILE_INVOICE'
}

/**
 * Possible Statuses for MetaDataType
 */
export enum Status {
  RECEIVED = 'RECEIVED',
  SENT = 'SENT',
  SENT_AND_RECEIVED = 'SENT_AND_RECEIVED'
}

/**
 * Represents a Transferred File from the OSD
 */
export interface TransferredFile {
  priority: string;
  status: Status; // will filter by status==SENT
  receptionTime: string;
  transferTime: string;
  metadataType: MetadataType;
  metadata: RawStationDataFrame;
}

/**
 * Represents one of the TransferredFile metadata types
 */
export interface RawStationDataFrame {
  stationId: string;
  channelIds: string[];
  payloadStartTime: string;
  payloadEndTime: string;
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
