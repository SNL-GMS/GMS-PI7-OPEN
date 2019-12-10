/*
 * Renders the stage intervals, expand button, and label of a stage
 * If the stage has activity intervals, it also renders themå
*/

import * as React from 'react';
import { WorkflowTypes } from '~graphql/';
import { StageExpansionButton } from './stage-expansion-button';
import { WorkflowRow } from './workflow-row/workflow-row';
import { WorkflowRowLabel } from './workflow-row/workflow-row-label';
/**
 * IntervalTable Props
 */
export interface WorkflowTableStageProps {
  stage: WorkflowTypes.ProcessingStage;
  currentlySelectedIntervalId: string | undefined;
  isExpanded: boolean;
  // called by clicks on the expansion buttons
  reportStageExpansion(stageName: string): void;
  // called by a right click on a stageIntervalCell
  showStageIntervalContextMenu(
    e: React.MouseEvent<HTMLElement>,
    stage: WorkflowTypes.ProcessingStage,
    interval: WorkflowTypes.ProcessingStageInterval,
    activityInterval: WorkflowTypes.ProcessingActivityInterval
  );
  // called by a right click on an activityIntervalCell
  showActivityIntervalContextMenu(
    e: React.MouseEvent<HTMLDivElement>,
    stage: WorkflowTypes.ProcessingStage,
    interval: WorkflowTypes.ProcessingStageInterval,
    activityInterval: WorkflowTypes.ProcessingActivityInterval
  );
  // called by a double click on an activityIntervalCell
  markActivityInterval(
    stage: WorkflowTypes.ProcessingStage,
    interval: WorkflowTypes.ProcessingStageInterval,
    activityInterval: WorkflowTypes.ProcessingActivityInterval,
    analystUserName: string,
    status: WorkflowTypes.IntervalStatus
  );
}

/*
* WorkflowTableStage
* @WorkflowTableStage
* Top level container for individual table row that will wrap interval elements
*/
export class WorkflowTableStage extends React.Component<
  WorkflowTableStageProps
  > {
  /**
   * Constructor.
   *
   * @param props The initial props
   */
  public constructor(props: WorkflowTableStageProps) {
    super(props);
  }

  public render() {
    return (
      <div className={'stage-row'}>
        <StageExpansionButton
          isExpanded={this.props.isExpanded}
          disabled={this.props.stage.activities.length === 0}
          stageName={this.props.stage.name}
          reportExpansion={this.props.reportStageExpansion}
        />
        <div>
          <div className="stage-row__sub-row">
            <WorkflowRow
              stage={this.props.stage}
              currentlySelectedIntervalId={
                this.props.currentlySelectedIntervalId
              }
              showStageIntervalContextMenu={
                this.props.showStageIntervalContextMenu
              }
              markActivityInterval={this.props.markActivityInterval}
            />
            <WorkflowRowLabel
              label={this.props.stage.name}
              isActivityRow={false}
              isExpanded={false}

            />
          </div>
          {this.props.stage.activities.length > 0 && this.props.isExpanded
            ? this.props.stage.activities.map(activity => (
              <div
                key={activity.name + this.props.stage.id}
                className="stage-row__sub-row"
              >
                <WorkflowRow
                  stage={this.props.stage}
                  currentlySelectedIntervalId={
                    this.props.currentlySelectedIntervalId
                  }
                  showActivityIntervalContextMenu={
                    this.props.showActivityIntervalContextMenu
                  }
                  activity={activity}
                  markActivityInterval={this.props.markActivityInterval}
                />
                <WorkflowRowLabel
                  label={activity.name}
                  isActivityRow={true}
                  isExpanded={false}
                />
              </div>
            ))
            : undefined}
        </div>
      </div>
    );
  }
}
