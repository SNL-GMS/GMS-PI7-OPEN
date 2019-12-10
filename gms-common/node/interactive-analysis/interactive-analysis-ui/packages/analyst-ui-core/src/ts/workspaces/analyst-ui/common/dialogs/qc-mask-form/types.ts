import { QcMaskTypes } from '~graphql/';
import { QcMaskDialogBoxType } from '../types';

/**
 * QcMaskDialogBox Props.
 */
export interface QcMaskDialogBoxProps {
  // Type of dialog box
  qcMaskDialogBoxType: QcMaskDialogBoxType;
  // Start and end time are required for qcMaskCreate
  startTimeSecs?: number;
  endTimeSecs?: number;
  // mask is required for reject and modify
  mask?: QcMaskTypes.QcMask;
  applyChanges(QcMaskDialogBoxType, maskId, QcMaskDialogBoxState): void;
}

/**
 * QcMaskDialogBox State
 */
export interface QcMaskDialogBoxState {
  // Fields are derived either from direct props or props.mask.currentVersion
  startDate: Date;
  endDate: Date;
  rationale: string;
  type: string;
  category: string;
  // True if the history is currently being displayed
  showHistory: boolean;
  // Must be set for reject and modify
  mask?: QcMaskTypes.QcMask;
  // 'Holds' reflect if a timepicker element has an invalid input and
  // the review/reject button should be disabled
  startTimeOnHold: boolean;
  endTimeOnHold: boolean;
}

export interface CoordinatesPx {
  xPx: number;
  yPx: number;
}
