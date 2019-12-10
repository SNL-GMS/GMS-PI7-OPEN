
import {
  gqls as CommonGqls,
  mutations as CommonMutations,
  types as CommonTypes
} from './common';
export { CommonGqls, CommonMutations, CommonTypes };

import {
  gqls as ChannelSegmentGqls,
  types as ChannelSegmentTypes
} from './channel-segment';
export { ChannelSegmentGqls, ChannelSegmentTypes };

import {
  gqls as EventGqls,
  mutations as EventMutations,
  queries as EventQueries,
  subscriptions as EventSubscriptions,
  types as EventTypes
} from './event';
export { EventGqls,
  EventMutations,
  EventQueries,
  EventSubscriptions,
  EventTypes };

import {
  gqls as FkGqls,
  mutations as FkMutations,
  queries as FkQueries,
  types as FkTypes
} from './fk';
export { FkGqls,
  FkQueries,
  FkMutations,
  FkTypes };

import {
  gqls as QcMaskGqls,
  mutations as QcMaskMutations,
  queries as QcMaskQueries,
  subscriptions as QcMaskSubscriptions,
  types as QcMaskTypes
} from './qc-mask';
export { QcMaskGqls,
  QcMaskMutations,
  QcMaskQueries,
  QcMaskSubscriptions,
  QcMaskTypes };

import {
  gqls as SignalDetectionGqls,
  mutations as SignalDetectionMutations,
  queries as SignalDetectionQueries,
  subscriptions as SignalDetectionSubscriptions,
  types as SignalDetectionTypes
} from './signal-detection';
export {
  SignalDetectionGqls,
  SignalDetectionMutations,
  SignalDetectionQueries,
  SignalDetectionSubscriptions,
  SignalDetectionTypes };

import {
  gqls as StationGqls,
  queries as StationQueries,
  types as StationTypes
} from './station';
export {
  StationGqls,
  StationQueries,
  StationTypes };

import {
  gqls as WaveformGqls,
  queries as WaveformQueries,
  subscriptions as WaveformSubscriptions,
  types as WaveformTypes
} from './waveform';
export { WaveformGqls, WaveformQueries, WaveformSubscriptions, WaveformTypes };

import {
  gqls as WorkflowGqls,
  mutations as WorkflowMutations,
  queries as WorkflowQueries,
  subscriptions as WorkflowSubscriptions,
  types as WorkflowTypes
} from './workflow';
export { WorkflowGqls, WorkflowMutations, WorkflowQueries, WorkflowSubscriptions, WorkflowTypes };

import {
  gqls as DataAcquisitionGqls,
  mutations as DataAcquisitionMutations,
  queries as DataAcquisitionQueries,
  types as DataAcquisitionTypes
} from './data-acquisition';
export { DataAcquisitionGqls, DataAcquisitionMutations, DataAcquisitionQueries, DataAcquisitionTypes };
