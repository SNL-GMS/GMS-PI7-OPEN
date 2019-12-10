export enum IntervalStatus {
    NotStarted = 'NotStarted',
    InProgress = 'InProgress',
    Complete = 'Complete',
    NotComplete = 'NotComplete'
}

export enum ProcessingActivityType {
    Scan = 'Scan',
    EventReview = 'EventReview',
    Auto = 'EventReview'
}

export enum ProcessingStageType {
    WorkflowAutomatic = 'WorkflowAutomatic',
    WorkflowInteractive = 'WorkflowInteractive',
    NonWorkflow = 'NonWorkflow'
}

export enum CreatorType {
    Analyst = 'Analyst',
    System = 'System'
}

export enum ChannelSegmentType {
    Acquired = 'Acquired',
    Raw = 'Raw',
    DetectionBeam = 'DetectionBeam',
    FkBean = 'FkBean',
    Filter = 'Filter'
}

export enum FeatureType {
    ArrivalTime = 'ArrivalTime',
    AzimuthSlowness = 'AzimuthSlowness',
    Amplitude = 'Amplitude'
}
