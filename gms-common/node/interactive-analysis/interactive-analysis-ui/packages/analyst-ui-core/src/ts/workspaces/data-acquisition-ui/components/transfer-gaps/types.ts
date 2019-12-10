import { DataAcquisitionTypes } from '~graphql/';

export interface TransferGapsRow {
  id: string;
  name: string;
  channels: TransferGapRowChannel[];
  priority: string;
  gapStartTime: string;
  gapEndTime: string;
  duration: string;
  location: string;
}

export interface TransferGapRowChannel {
  id: string;
  name: string;
  priority: string;
  gapStartTime: string;
  gapEndTime: string;
  duration: string;
  location: string;
}

/**
 * TransferGaps State
 */
export interface TransferGapsState {
  getNodeChildDetails: any;
}

/**
 * TransferGaps Props
 */
export type TransferGapsProps = DataAcquisitionTypes.TransferredFilesByTimeRangeQueryProps;
