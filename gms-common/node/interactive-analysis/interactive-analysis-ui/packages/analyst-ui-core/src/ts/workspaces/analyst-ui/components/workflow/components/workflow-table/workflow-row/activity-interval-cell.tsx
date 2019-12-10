/*
 * An activity interval's cell. Mostly identical to stage interval cell
 */
import * as classNames from 'classnames';
import * as React from 'react';
import { WorkflowTypes } from '~graphql/';

/**
 * Interval Props
 */
export interface ActivityIntervalCellProps {
  interval: WorkflowTypes.ProcessingStageInterval;
  activityInterval: WorkflowTypes.ProcessingActivityInterval;
  isSelected: boolean;
  triggerActivityIntervalContextMenu(
    event: React.MouseEvent<HTMLDivElement>,
    interval: WorkflowTypes.ProcessingStageInterval,
    activityInterval: WorkflowTypes.ProcessingActivityInterval
  );
  triggerMarkActivityInterval(
    interval: WorkflowTypes.ProcessingStageInterval,
    activityInterval: WorkflowTypes.ProcessingActivityInterval,
    analystUserName: string,
    status: WorkflowTypes.IntervalStatus
  );
}

/**
 * Interval State
 */
export interface ActivityIntervalCellState {
  dummy?: boolean;
}

export class ActivityIntervalCell extends React.Component<
  ActivityIntervalCellProps,
  ActivityIntervalCellState
  > {
  /**
   * Constructor.
   *
   * @param props The initial props
   */
  public constructor(props: ActivityIntervalCellProps) {
    super(props);
    this.state = {};
  }
  public render() {
    const cellClass = classNames({
      'interval-cell': true,
      'interval-cell--selected': this.props.isSelected,
      'interval-cell--not-complete':
        this.props.activityInterval.status === WorkflowTypes.IntervalStatus.NotComplete,
      'interval-cell--in-progress':
        this.props.activityInterval.status === WorkflowTypes.IntervalStatus.InProgress,
      'interval-cell--not-started':
        this.props.activityInterval.status === WorkflowTypes.IntervalStatus.NotStarted,
      'interval-cell--complete':
        this.props.activityInterval.status === WorkflowTypes.IntervalStatus.Complete,
      'interval-cell--activity-cell': true
    });
    return (
      <div
        key={this.props.interval.startTime}
        className={cellClass}
        onContextMenu={e => {
          this.props.triggerActivityIntervalContextMenu(
            e,
            this.props.interval,
            this.props.activityInterval
          );
        }}
        onDoubleClick={async e => {
          await this.props.triggerMarkActivityInterval(
            this.props.interval,
            this.props.activityInterval,
            'Mark',
            WorkflowTypes.IntervalStatus.InProgress
          );
        }}
        title={`${
          this.props.activityInterval.status === WorkflowTypes.IntervalStatus.Complete
            ? this.props.activityInterval.completedBy.userName
            : this.props.activityInterval.activeAnalysts
              .map(a => a.userName)
              .join(', ')
          }`}
      >
        {this.props.activityInterval.status === WorkflowTypes.IntervalStatus.Complete
          ? this.props.activityInterval.completedBy.userName
          : this.props.activityInterval.activeAnalysts
            .map(a => a.userName)
            .join(', ')}
      </div>
    );
  }
}
