/**
 * Data repesentation for the station reference data from the standard test data set JSON files
 */

/**
 * Network definition in the Standard Test Data set
 */
export interface Network {
    entityId: string;
    versionId: string;
    name: string;
    description: string;
    organization: string;
    region: string;
    source: Source;
    comment: string;
    actualChangeTime: string;
    systemChangeTime: string;
  }

/**
 * Station definition in the Standard Test Data set
 */
export interface Station {
  entityId: string;
  versionId: string;
  name: string;
  description: string;
  stationType: string;
  source: Source;
  comment: string;
  latitude: number;
  longitude: number;
  elevation: number;
  actualChangeTime: string;
  systemChangeTime: string;
  aliases: any[];
}

/**
 * Site definition in the Standard Test Data set
 */
export interface Site {
  entityId: string;
  versionId: string;
  name: string;
  description: string;
  source: Source;
  comment: string;
  latitude: number;
  longitude: number;
  elevation: number;
  actualChangeTime: string;
  systemChangeTime: string;
  position: Position;
  aliases: any[];
}

/**
 * Channel definition in the Standard Test Data set
 */
export interface Channel {
  entityId: string;
  versionId: string;
  name: string;
  type: string;
  dataType: string;
  locationCode: number;
  latitude: number;
  longitude: number;
  elevation: number;
  depth: number;
  verticalAngle: number;
  horizontalAngle: number;
  nominalSampleRate: number;
  actualChangeTime: string;
  systemChangeTime: string;
  source: Source;
  comment: string;
  position: Position;
  actualTime: string;
  systemTime: string;
  aliases: any[];
}

/**
 * Position definition in the Standard Test Data set
 */
export interface Position {
  northDisplacementKm: number;
  eastDisplacementKm: number;
  verticalDisplacementKm: number;
}

/**
 * Source definition in the Standard Test Data set
 */
export interface Source {
    originatingOrganization: string;
    informationTime: string;
    reference: string;
}

/**
 * Membership definition in the Standard Test Data set
 */
export interface Membership {
  id: string;
  comment: string;
  actualChangeTime: string;
  systemChangeTime: string;
  parentId: string;
  childId: string;
  status: string;
}
