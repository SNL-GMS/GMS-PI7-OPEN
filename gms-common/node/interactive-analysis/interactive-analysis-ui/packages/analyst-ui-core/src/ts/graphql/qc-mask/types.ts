import { GraphqlQueryControls } from 'react-apollo';
import { CreationInfo, TimeRange } from '../common/types';

// ***************************************
// Mutations
// ***************************************

export interface QcMaskInput {
  timeRange: TimeRange;
  category: string;
  type: string;
  rationale: string;
}

export interface CreateQcMaskMutationArgs {
  channelIds: string[];
  input: QcMaskInput;
}

export interface RejectQcMaskMutationArgs {
  maskId: string;
  inputRationale: string;
}

export interface UpdateQcMaskMutationArgs {
  maskId: string;
  input: QcMaskInput;
}

// ***************************************
// Subscriptions
// ***************************************

export interface QcMasksCreatedSubscription {
  qcMasksCreated: QcMask[];
}

// ***************************************
// Queries
// ***************************************

export interface QcMasksByChannelIdQueryArgs {
  timeRange: TimeRange;
  channelIds: string[];
}

// tslint:disable-next-line:max-line-length interface-over-type-literal
export type QcMasksByChannelIdQueryProps =  { qcMasksByChannelIdQuery: GraphqlQueryControls<{}> & {qcMasksByChannelId: QcMask[]}};

// ***************************************
// Model
// ***************************************

export interface QcMaskVersion {
  startTime: number;
  endTime: number;
  category: string;
  type: string;
  rationale: string;
  version: string;
  channelSegmentIds: string[];
  creationInfo: CreationInfo;
}

export interface QcMask {
  id: string;
  channelId: string;
  currentVersion: QcMaskVersion;
  qcMaskVersions: QcMaskVersion[];
}
