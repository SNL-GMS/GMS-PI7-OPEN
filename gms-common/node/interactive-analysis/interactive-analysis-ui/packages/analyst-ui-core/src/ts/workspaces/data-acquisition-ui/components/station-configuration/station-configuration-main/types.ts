import { Row } from '@gms/ui-core-components';

/**
 * Interface that defines the expected data types for
 * a Station Configuration table row.
 */
export interface StationConfigurationRow extends Row {
  siteId: string;
  name: string;
  latitude: number;
  longitude: number;
  elevation: number;
  stationType: string;
  description: string;
  northOffset: number;
  eastOffset: number;
  sampleRate: number;
}

export interface StationConfigurationRowChannel {
  channelId: string;
  type: string;
  sampleRate: string;
  systemChangeTime: string;
  actualChangeTime: string;
}
