/**
 * Model definitions for the Workflow User Interface API
 */

 /**
  * Interval status included in both the ProcessingStageInterval and ProcessingActivityInterval
  */
export enum IntervalStatus {
    NotStarted = 'NotStarted',
    InProgress = 'InProgress',
    Complete = 'Complete',
    NotComplete = 'NotComplete'
}

/**
 * Processing stage type enum
 */
export enum ProcessingStageType {
    WorkflowAutomatic = 'WorkflowAutomatic',
    WorkflowInteractive = 'WorkflowInteractive',
    NonWorkflow = 'NonWorkflow'
}

/**
 * Processing Stage (general information across intervals)
 */
export interface ProcessingStage {
    id: string;
    name: string;
    stageType: ProcessingStageType;
    activityIds: string[];
}

/**
 * Information for a processing interval (e.g. time range, associated stage intervals)
 */
export interface ProcessingInterval {
    id: string;
    startTime: number;
    endTime: number;
    stageIntervalIds: string[];
}

/**
 * Interval-specific processing stage information
 */
export interface ProcessingStageInterval {
    id: string;
    completedByUserName: string;
    startTime: number;
    endTime: number;
    eventCount: number;
    status: IntervalStatus;
    stageId: string;
    intervalId: string;
    activityIntervalIds: string[];
}

/**
 * Processing activity type enum
 */
export enum ProcessingActivityType {
    Scan = 'Scan',
    EventReview = 'EventReview',
    Auto = 'EventReview'
}

/**
 * Processing activity (general information across intervals)
 */
export interface ProcessingActivity {
    id: string;
    name: string;
    activityType: ProcessingActivityType;
    stageId: string;
}

/**
 * Analyst information
 */
export interface Analyst {
    userName: string;
}

/**
 * Interval-specific processing activity information
 */
export interface ProcessingActivityInterval {
    id: string;
    activeAnalystUserNames: string[];
    completedByUserName: string;
    timeStarted: number;
    eventCount: number;
    status: IntervalStatus;
    activityId: string;
    stageIntervalId: string;
}

/**
 * Workflow data structure to encapsulate the various
 * values, for now loaded by the mock backend
 */
export interface WorkflowDataCache {
    stages: ProcessingStage[];
    activities: ProcessingActivity[];
    intervals: ProcessingInterval[];
    stageIntervals: ProcessingStageInterval[];
    analysts: Analyst[];
    activityIntervals: ProcessingActivityInterval[];
}
