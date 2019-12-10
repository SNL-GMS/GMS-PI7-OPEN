import gql from 'graphql-tag';
import { amplitudeMeasurementValueFragment,
         instantMeasurementValueFragment,
         numericMeasurementValueFragment,
         phaseTypeMeasurementValueFragment,
         signalDetectionFragment,
         stringMeasurementValueFragment
        } from '~graphql/signal-detection/gqls';
import { creationInfoFragment } from '../common/gqls';

export const signalDetectionEventAssociationFragment = gql`
  fragment SignalDetectionEventAssociationFragment on SignalDetectionEventAssociation {
    id
    rejected
    eventHypothesisId
    signalDetectionHypothesis {
      id
      rejected
      creationInfo {
        ...CreationInfoFragment
      }
    }
  }
  ${creationInfoFragment}
`;
export const eventLocationFragment = gql`
  fragment EventLocationFragment on EventLocation {
    latitudeDegrees
    longitudeDegrees
    depthKm
    time
  }
`;

export const locationSolutionFragment = gql`
fragment LocationSolutionFragment on LocationSolution {
  id
  locationType
  location {
    ...EventLocationFragment
  }
  featurePredictions {
    id
    predictedValue {
      ...AmplitudeMeasurementValueFragment
      ...InstantMeasurementValueFragment
      ...NumericMeasurementValueFragment
      ...PhaseTypeMeasurementValueFragment
      ...StringMeasurementValueFragment
    }
    predictionType
    phase
    channelId
    stationId
  }
  locationRestraint {
    depthRestraintType
    depthRestraintKm
    latitudeRestraintType
    latitudeRestraintDegrees
    longitudeRestraintType
    longitudeRestraintDegrees
    timeRestraintType
    timeRestraint
  }
  locationUncertainty {
    xy
    xz
    xt
    yy
    yz
    yt
    zz
    zt
    tt
    stDevOneObservation
    ellipses {
      scalingFactorType
      kWeight
      confidenceLevel
      majorAxisLength
      majorAxisTrend
      minorAxisLength
      minorAxisTrend
      depthUncertainty
      timeUncertainty
    }
    ellipsoids {
      scalingFactorType
      kWeight
      confidenceLevel
      majorAxisLength
      majorAxisTrend
      majorAxisPlunge
      intermediateAxisLength
      intermediateAxisTrend
      intermediateAxisPlunge
      minorAxisLength
      minorAxisTrend
      minorAxisPlunge
      depthUncertainty
      timeUncertainty
    }
  }
  locationBehaviors {
    residual
    weight
    defining
    featurePredictionId
    featureMeasurementId
  }
  snapshots {
    signalDetectionId
    signalDetectionHypothesisId
    stationId
    stationName
    channelName
    phase
    time {
      defining
      observed
      residual
      correction
    }
    slowness {
      defining
      observed
      residual
      correction
    }
    azimuth {
      defining
      observed
      residual
      correction
    }
  }
}
${eventLocationFragment}
${numericMeasurementValueFragment}
${instantMeasurementValueFragment}
${amplitudeMeasurementValueFragment}
${phaseTypeMeasurementValueFragment}
${stringMeasurementValueFragment}
`;

export const locationSolutionSetFragment = gql`
fragment LocationSolutionSetFragment on LocationSolutionSet {
  id
  count
  locationSolutions {
    ...LocationSolutionFragment
  }
}
${locationSolutionFragment}
`;

export const preferredLocationSolutionFragment = gql`
fragment PreferredLocationSolutionFragment on PreferredLocationSolution {
  locationSolution {
    ...LocationSolutionFragment
  }
  creationInfo {
    ...CreationInfoFragment
  }
}
${locationSolutionFragment}
${creationInfoFragment}
`;

export const eventHypothesisFragment = gql`
fragment EventHypothesisFragment on EventHypothesis {
  id
  rejected
  event {
    id
    status
    modified
    hasConflict
  }
  preferredLocationSolution {
    ...PreferredLocationSolutionFragment
  }
  associationsMaxArrivalTime
  signalDetectionAssociations {
    ...SignalDetectionEventAssociationFragment
  }
  locationSolutionSets {
    ...LocationSolutionSetFragment
  }
}
${signalDetectionEventAssociationFragment}
${locationSolutionSetFragment}
${preferredLocationSolutionFragment}
`;

export const preferredHypothesisFragment = gql`
fragment PreferredEventHypothesisFragment on PreferredEventHypothesis {
  processingStage {
    id
  }
  eventHypothesis {
    ...EventHypothesisFragment
  }
}
${eventHypothesisFragment}
`;

export const eventFragment = gql`
fragment EventFragment on Event {
  id
  status
  modified
  hasConflict
  currentEventHypothesis {
    ...PreferredEventHypothesisFragment
  }
  activeAnalysts {
      userName
  }
  conflictingSdHypIds
}
${preferredHypothesisFragment}
`;

export const associationChangeFragment = gql`
fragment AssociationChangeFragment on AssociationChange {
  events {
    ...EventFragment
  }
  sds {
    ...SignalDetectionFragment
  }
}
${eventFragment}
${signalDetectionFragment}
`;
