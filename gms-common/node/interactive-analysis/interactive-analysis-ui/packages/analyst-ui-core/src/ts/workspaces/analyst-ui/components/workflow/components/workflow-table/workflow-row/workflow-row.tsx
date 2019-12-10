/*
 * A row of intervals in the workflow
 * May contain either activity or stage interval cells
 * This is the deepest component that knows about stages
 */

import * as React from 'react';
import { WorkflowTypes } from '~graphql/';
import { ActivityIntervalCell } from './activity-interval-cell';
import { StageIntervalCell } from './stage-interval-cell';

/**
 * WorkflowRow Props
 */
export interface WorkflowRowProps {
  activity?: WorkflowTypes.ProcessingActivity;
  stage: WorkflowTypes.ProcessingStage;
  currentlySelectedIntervalId: string | undefined;
  showStageIntervalContextMenu?(
    e: React.MouseEvent<HTMLElement>,
    stage: WorkflowTypes.ProcessingStage,
    interval: WorkflowTypes.ProcessingStageInterval,
    activityInterval: WorkflowTypes.ProcessingActivityInterval
  );
  showActivityIntervalContextMenu?(
    e: React.MouseEvent<HTMLDivElement>,
    stage: WorkflowTypes.ProcessingStage,
    interval: WorkflowTypes.ProcessingStageInterval,
    activityInterval: WorkflowTypes.ProcessingActivityInterval
  );
  markActivityInterval(
    stage: WorkflowTypes.ProcessingStage,
    interval: WorkflowTypes.ProcessingStageInterval,
    activityInterval: WorkflowTypes.ProcessingActivityInterval,
    analystUserName: string,
    status: WorkflowTypes.IntervalStatus
  );
}
/**
 * WorkflowRow State
 */
export interface WorkflowRowState {
  dummy: boolean;
}

/*
* @WorkflowRow
* Horizontal collection of individual cells that comprise a sequential view of an activity
*/
export class WorkflowRow extends React.Component<
  WorkflowRowProps,
  WorkflowRowState
  > {
  /**
   * Constructor.
   *
   * @param props The initial props
   */
  public constructor(props: WorkflowRowProps) {
    super(props);
    this.state = {
      dummy: false
    };
  }
  public render() {
    return (
      <div key={this.props.stage.name} className="workflow-row">
        {this.props.stage.intervals.map(interval => {
          // If an activity has been provided, renders a row of activity interval cells
          if (this.props.activity && this.props.activity.name) {
            // As of now, the activityIntervals are indexed such that
            // an event activity's intervals will be at index 0
            // This is quite brittle and liable to break if we ever add new types of activities
            const activityIntervalToUse =
              this.props.activity.name.indexOf('event') >= 0
                ? interval.activityIntervals[0]
                : this.props.activity.name.indexOf('global') >= 0
                  ? interval.activityIntervals[1]
                  : undefined;
            const intervalSelected =
              this.props.currentlySelectedIntervalId === activityIntervalToUse.id;
            if (activityIntervalToUse !== undefined) {
              return (
                <ActivityIntervalCell
                  key={activityIntervalToUse.id}
                  interval={interval}
                  activityInterval={activityIntervalToUse}
                  isSelected={intervalSelected}
                  triggerActivityIntervalContextMenu={
                    this.triggerActivityIntervalContextMenu
                  }
                  triggerMarkActivityInterval={this.triggerMarkActivityInterval}
                />
              );
            } else {
              return null;
            }
          } else {
            return (
              <StageIntervalCell
                interval={interval}
                key={interval.id}
                activityInterval={interval.activityIntervals[0]}
                triggerStageIntervalContextMenu={
                  this.triggerStageIntervalContextMenu
                }
                isSelected={false}
              />
            );
          }
        })}
      </div>
    );
  }

  /*
   * These 'trigger' functions are called by activity/stage cells. They supply fields (ie: stage)
   * that cells aren't allowed to know about
   */
  private readonly triggerStageIntervalContextMenu = (
    event: React.MouseEvent<HTMLElement>,
    interval: WorkflowTypes.ProcessingStageInterval,
    activityInterval: WorkflowTypes.ProcessingActivityInterval
  ): void => {
    this.props.showStageIntervalContextMenu(
      event,
      this.props.stage,
      interval,
      activityInterval
    );
  }

  private readonly triggerActivityIntervalContextMenu = (
    event: React.MouseEvent<HTMLDivElement>,
    interval: WorkflowTypes.ProcessingStageInterval,
    activityInterval: WorkflowTypes.ProcessingActivityInterval
  ): void => {
    this.props.showActivityIntervalContextMenu(
      event,
      this.props.stage,
      interval,
      activityInterval
    );
  }

  private readonly triggerMarkActivityInterval = (
    interval: WorkflowTypes.ProcessingStageInterval,
    activityInterval: WorkflowTypes.ProcessingActivityInterval,
    analystUserName: string,
    status: WorkflowTypes.IntervalStatus
  ): void => {
    this.props.markActivityInterval(
      this.props.stage,
      interval,
      activityInterval,
      analystUserName,
      status
    );
  }
}
