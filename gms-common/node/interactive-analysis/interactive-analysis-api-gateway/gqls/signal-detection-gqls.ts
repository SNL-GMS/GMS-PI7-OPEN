import gql from 'graphql-tag';
import { creationInfoFragment, locationFragment } from './common-gqls';
import { processingChannelFragment } from './station-gqls';

/**
 * Represents gql fragment for the Feature Measurement Value for a amplitude type.
 */
export const amplitudeMeasurementValueFragment = gql`
  fragment AmplitudeMeasurementValueFragment on AmplitudeMeasurementValue {
    startTime
    period
    amplitude {
        value
        standardDeviation
        units
    }
}
`;

/**
 * Represents gql fragment for the Feature Measurement Value for a instant type.
 */
export const instantMeasurementValueFragment = gql`
  fragment InstantMeasurementValueFragment on InstantMeasurementValue {
    value
    standardDeviation
}
`;

/**
 * Represents gql fragment for the Feature Measurement Value for a numeric type.
 */
export const numericMeasurementValueFragment = gql`
  fragment NumericMeasurementValueFragment on NumericMeasurementValue {
    referenceTime
    measurementValue {
        value
        standardDeviation
        units
    }
}
`;

/**
 * Represents gql fragment for the Feature Measurement Value for a phase type.
 */
export const phaseTypeMeasurementValueFragment = gql`
  fragment PhaseTypeMeasurementValueFragment on PhaseTypeMeasurementValue {
  phase
  confidence
}
`;

export const featureMeasurementFragment = gql`
  # union FeatureMeasurementValueUnion = FeatureMeasurementStringValue | FeatureMeasurementNumberValue
  fragment FeatureMeasurementFragment on FeatureMeasurement {
    id
    featureMeasurementType
    measurementValue {
      ...AmplitudeMeasurementValueFragment
      ...InstantMeasurementValueFragment
      ...NumericMeasurementValueFragment
      ...PhaseTypeMeasurementValueFragment
    }
    creationInfo {
      ...CreationInfoFragment
    }
    definingRules {
      operationType
      isDefining
    }
  }
  ${creationInfoFragment}
  ${numericMeasurementValueFragment}
  ${instantMeasurementValueFragment}
  ${amplitudeMeasurementValueFragment}
  ${phaseTypeMeasurementValueFragment}
`;

export const signalDetectionHypothesisFragment = gql`
  fragment SignalDetectionHypothesisFragment on SignalDetectionHypothesis {
    id
    phase
    rejected
    featureMeasurements {
        ...FeatureMeasurementFragment
    }
    associatedEventHypotheses {
      id
      rejected
      event {
        id
        status
      }
      preferredLocationSolution {
        locationSolution {
          location {
            latitudeDegrees
            longitudeDegrees
            depthKm
            time
          }
        }
      }
      associationsMaxArrivalTime
    }
    creationInfo {
      ...CreationInfoFragment
    }
  }
  ${featureMeasurementFragment}
  ${creationInfoFragment}
`;

export const signalDetectionFragment = gql`
  fragment SignalDetectionFragment on SignalDetection {
    id
    monitoringOrganization
    station {
      id
      name
      location {
        ...LocationFragment
      }
      defaultChannel {
        ...ProcessingChannelFragment
      }
      sites {
        id
        name
        channels {
            ...ProcessingChannelFragment
        }
      }
    }
    currentHypothesis {
      ...SignalDetectionHypothesisFragment
    }
    signalDetectionHypotheses {
      ...SignalDetectionHypothesisFragment
    }
    modified,
    hasConflict,
    associationModified,
    creationInfo {
      ...CreationInfoFragment
    }
  }
  ${processingChannelFragment}
  ${signalDetectionHypothesisFragment}
  ${locationFragment}
  ${creationInfoFragment}
`;
