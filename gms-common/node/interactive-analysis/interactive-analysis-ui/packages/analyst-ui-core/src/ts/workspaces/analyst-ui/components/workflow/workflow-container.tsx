import { compose, graphql } from 'react-apollo';
import * as ReactRedux from 'react-redux';
import { bindActionCreators } from 'redux';
import {
  WorkflowMutations,
  WorkflowQueries
} from '~graphql/';
import { AnalystWorkspaceOperations } from '~state/';
import { AppState } from '~state/types';
import { WorkflowReduxProps } from './types';
import { Workflow } from './workflow-component';

const mapStateToProps = (state: AppState): Partial<WorkflowReduxProps> => ({
  currentStageInterval: state.analystWorkspaceState.currentStageInterval,

});

const mapDispatchToProps = (dispatch): Partial<WorkflowReduxProps> =>
  bindActionCreators(
    {
      setCurrentStageInterval: AnalystWorkspaceOperations.setCurrentStageInterval
    } as any,
    dispatch
  );

export const ReduxApolloWorkflowContainer = compose(
  ReactRedux.connect(mapStateToProps, mapDispatchToProps),
  graphql(WorkflowQueries.stagesQuery, { name: 'stagesQuery' }),
  graphql(WorkflowMutations.markActivityIntervalMutation, { name: 'markActivityInterval' }),
  graphql(WorkflowMutations.markStageIntervalMutation, { name: 'markStageInterval' }),
  graphql(WorkflowMutations.setTimeIntervalMutation, { name: 'setTimeInterval' })
)(Workflow);
