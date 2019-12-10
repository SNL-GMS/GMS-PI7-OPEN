import { GraphqlQueryControls } from 'react-apollo';

// ***************************************
// Mutations
// ***************************************

/**
 * Input data to mark activity interval complete
 */
export interface IntervalStatusInput {
  status: IntervalStatus;
  analystUserName: string;
}

export interface MarkActivityIntervalMutationArgs {
  activityIntervalId: string;
  input: IntervalStatusInput;
}

export interface MarkStageIntervalMutationArgs {
  stageIntervalId: string;
  input: IntervalStatusInput;
}

export interface SetTimeIntervalMutationArgs {
  startTimeSec: number;
  endTimeSec: number;
}

// ***************************************
// Subscriptions
// ***************************************

export interface StagesChangedSubscription {
  stages: ProcessingStage[];
}

// ***************************************
// Queries
// ***************************************

// tslint:disable-next-line:max-line-length interface-over-type-literal
export type StagesQueryProps =  { stagesQuery: GraphqlQueryControls<{}> & {stages: ProcessingStage[]}};

// ***************************************
// Model
// ***************************************

export enum IntervalStatus {
  NotStarted = 'NotStarted',
  InProgress = 'InProgress',
  Complete = 'Complete',
  NotComplete = 'NotComplete'
}

export enum ProcessingStageType {
  WorkflowAutomatic = 'WorkflowAutomatic',
  WorkflowInteractive = 'WorkflowInteractive',
  NonWorkflow = 'NonWorkflow'
}

export enum ProcessingActivityType {
  Scan = 'Scan',
  EventReview = 'EventReview',
  Auto = 'Auto'
}

export interface ProcessingInterval {
  id: string;
  startTime: number;
  endTime: number;
  stageIntervals: ProcessingStageInterval[];
}

export interface ProcessingStage {
  id: string;
  name: string;
  stageType: ProcessingStageType;
  activities: ProcessingActivity[];
  intervals: ProcessingStageInterval[];
}

export interface ProcessingActivity {
  id: string;
  name: string;
  activityType: ProcessingActivityType;
}

export interface Analyst {
  userName: string;
}

export interface ProcessingStageInterval {
  id: string;
  startTime: number;
  endTime: number;
  status: IntervalStatus;
  eventCount: number;
  completedBy: Analyst;
  activityIntervals: ProcessingActivityInterval[];
}

export interface ProcessingActivityInterval {
  id: string;
  activeAnalysts: Analyst[];
  activity: ProcessingActivity;
  completedBy: Analyst;
  status: IntervalStatus;
  eventCount: number;
  timeStarted: number;
}
