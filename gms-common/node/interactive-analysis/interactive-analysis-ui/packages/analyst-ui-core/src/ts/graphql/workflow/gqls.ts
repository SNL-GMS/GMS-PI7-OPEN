import gql from 'graphql-tag';

export const processingStageFragment = gql`
  fragment ProcessingStageFragment on ProcessingStage {
    id
    stageType
    name
    activities {
      id
      name
      activityType
    }
    intervals {
      id
      startTime
      endTime
      status
      eventCount
      completedBy {
        userName
      }
      activityIntervals {
        id
        activeAnalysts {
          userName
        }
        activity {
          id
          activityType
          name
        }
        completedBy {
          userName
        }
        status
        eventCount
        timeStarted
      }
    }
  }
`;
