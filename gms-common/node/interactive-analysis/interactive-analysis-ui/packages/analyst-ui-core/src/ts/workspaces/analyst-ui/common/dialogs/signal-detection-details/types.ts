import { Row } from '@gms/ui-core-components';
import { SignalDetectionTypes } from '~graphql/';

/**
 * SignalDetectionDetails Props
 */
export interface SignalDetectionDetailsProps {
  detection: SignalDetectionTypes.SignalDetection;
  color: string;
}

/**
 * SignalDetectionDetails State
 */
export interface SignalDetectionDetailsState {
  showHistory: boolean;
}

/**
 * Interface that describes the Detection history
 * information.
 */
export interface SignalDetectionHistoryRow extends Row {
  id: string;
  versionId: string;
  phase: string;
  rejected: boolean;
  arrivalTimeMeasurementFeatureType: string;
  arrivalTimeMeasurementTimestamp: number;
  arrivalTimeMeasurementUncertaintySec: number;
  author: string;
  creationTime: number;
}
