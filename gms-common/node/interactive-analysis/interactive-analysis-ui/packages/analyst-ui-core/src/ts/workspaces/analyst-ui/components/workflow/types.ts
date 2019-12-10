import * as Gl from '@gms/golden-layout';
import { ChildProps, MutationFunc } from 'react-apollo';
import { WorkflowTypes } from '~graphql/';
import { StageInterval } from '~state/analyst-workspace/types';

/**
 * Mutations used by the workflow display
 */
export interface WorkflowMutations {
  // {} because we don't care about mutation results for now, handling that through subscriptions
  markActivityInterval: MutationFunc<{}>;
  markStageInterval: MutationFunc<{}>;
  setTimeInterval: MutationFunc<{}>;
}

/**
 * Props mapped in from Redux
 */
export interface WorkflowReduxProps {
  // passed in from golden-layout
  glContainer?: Gl.Container;
  currentStageInterval: StageInterval;
  // redux callbacks
  setCurrentStageInterval(stageInterval: StageInterval);
}

export interface ExpansionState {
  stageName: string;
  expanded: boolean;
}
/**
 * State for the workflow display
 */
export interface WorkflowState {
  // in seconds
  startTimeSecs: number;
  // in seconds
  endTimeSecs: number;
  // in seconds
  intervalDurationSecs: number;
  expansionStates: ExpansionState[];
}

export type WorkflowProps = WorkflowReduxProps
  & ChildProps<WorkflowMutations>
  & WorkflowTypes.StagesQueryProps;
