/*
 * A stage interval's cell. Mostly identical to activity interval cell
 */
import * as classNames from 'classnames';
import * as React from 'react';
import { WorkflowTypes } from '~graphql/';

/**
 * Interval Props
 */
export interface StageIntervalCellProps {
  interval: WorkflowTypes.ProcessingStageInterval;
  activityInterval: WorkflowTypes.ProcessingActivityInterval;
  isSelected: boolean;
  triggerStageIntervalContextMenu(
    event: React.MouseEvent<HTMLElement>,
    interval: WorkflowTypes.ProcessingStageInterval,
    activityInterval: WorkflowTypes.ProcessingActivityInterval
  );
}

/**
 * Interval State
 */
export interface StageIntervalCellState {
  dummy?: boolean;
}

/*
* @StageIntervalCell
* Container for individual stage interval cell
*/
export class StageIntervalCell extends React.Component<
  StageIntervalCellProps,
  StageIntervalCellState
  > {
  /**
   * Constructor.
   *
   * @param props The initial props
   */
  public constructor(props: StageIntervalCellProps) {
    super(props);
    this.state = {};
  }
  public render() {
    // Chooses the correct css className based on a cell's status
    const cellClass = classNames({
      'interval-cell': true,
      'interval-cell--not-complete':
        this.props.interval.status === WorkflowTypes.IntervalStatus.NotComplete,
      'interval-cell--in-progress':
        this.props.interval.status === WorkflowTypes.IntervalStatus.InProgress,
      'interval-cell--not-started':
        this.props.interval.status === WorkflowTypes.IntervalStatus.NotStarted,
      'interval-cell--complete':
        this.props.interval.status === WorkflowTypes.IntervalStatus.Complete
    });
    return (
      <div
        key={this.props.interval.startTime}
        className={cellClass}
        onContextMenu={e => {
          this.props.triggerStageIntervalContextMenu(
            e,
            this.props.interval,
            this.props.activityInterval
          );
        }}
      >
        {this.props.interval.status !== WorkflowTypes.IntervalStatus.NotStarted
          ? this.props.interval.eventCount
          : undefined}
      </div>
    );
  }
}
