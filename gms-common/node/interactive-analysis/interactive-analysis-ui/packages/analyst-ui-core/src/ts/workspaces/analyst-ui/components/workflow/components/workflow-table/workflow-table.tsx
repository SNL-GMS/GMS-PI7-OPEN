/*
    The root component of the interval table in the workflow
    Handles the display of stages, activities, expansion buttons, and their labels
*/

import * as React from 'react';
import { WorkflowTypes } from '~graphql/';
import { ExpansionState } from '../../types';
import { DayBoundaryIndicator } from './day-boundary-indicator';
import { WorkflowTableStage } from './workflow-table-stage';

/**
 * WorkflowTable Props
 */
export interface WorkflowTableProps {
  stages: WorkflowTypes.ProcessingStage[];
  currentlySelectedIntervalId: string | undefined;
  expansionStates: ExpansionState[];
  startTimeSecs: number;
  endTimeSecs: number;
  pixelsPerHour: number;
  maxWidth: string;
  // onScroll handler, which can be used to synchronize scrolling of the workflow table with other elements
  onScroll(e: React.UIEvent<HTMLDivElement>): void;
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

/**
 * WorkflowTable State
 */
export interface WorkflowTableState {
  dummy?: boolean;
}

/*
* @WorkflowTable
* Main container for Workflow Table to wrap table rows and interval stages
*/
export class WorkflowTable extends React.Component<
  WorkflowTableProps,
  WorkflowTableState
  > {
  private dayDividersRef: DayBoundaryIndicator;
  private intervalTableRef: HTMLDivElement;

  /**
   * Constructor.
   *
   * @param props The initial props
   */
  public constructor(props: WorkflowTableProps) {
    super(props);
    this.state = {
      dummy: true
    };
  }
  public render() {
    return (
      <div
        className="workflow-scroll-wrapper"
        style={{ maxWidth: this.props.maxWidth }}
      >
        <div className="interval-table__curtain-left" />

        <div
          className="interval-table-wrapper-wrapper"
        >
          <DayBoundaryIndicator
            startTimeSecs={this.props.startTimeSecs}
            endTimeSecs={this.props.endTimeSecs}
            pixelsPerHour={this.props.pixelsPerHour}
            key={'day-boundary-indicator'}
            ref={ref => {
              this.dayDividersRef = ref;
            }}
          />
          {/* The curtain is set s.t.
                        it prevents the day boundary indicator from displaying off the edges of the workflow table
                */}
          <div
            className="interval-table-scroll-wrapper"
            onScroll={
              e => {
                this.intervalTableRef.scrollTop = e.currentTarget.scrollTop;
              }
            }
          />
          <div
            className={'interval-table-wrapper'}
            onScroll={e => {
              this.dayDividersRef.scrollDayIndicator(e.currentTarget.scrollLeft);
              this.props.onScroll(e);
            }}
            ref={ref => {
              this.intervalTableRef = ref;
            }}
          >
            {this.props.stages.map(stage => (
              <WorkflowTableStage
                currentlySelectedIntervalId={
                  this.props.currentlySelectedIntervalId
                }
                stage={stage}
                reportStageExpansion={this.props.reportStageExpansion}
                showStageIntervalContextMenu={
                  this.props.showStageIntervalContextMenu
                }
                showActivityIntervalContextMenu={
                  this.props.showActivityIntervalContextMenu
                }
                markActivityInterval={this.props.markActivityInterval}
                key={stage.id}
                isExpanded={
                  this.props.expansionStates.find(
                    es => es.stageName === stage.name
                  ).expanded
                }
              />
            ))}
          </div>
          {/*
                The curtain is set s.t. it prevents the day boundary indicator
                from displaying off the edges of the workflow table
            */}
        </div>
        <div className="interval-table__curtain-right" />
      </div>
    );
  }

  public componentDidMount() {
    // Because this will be re-written, to scroll to the end of the workflow we just set the scroll
    const scrollTo = this.intervalTableRef.scrollWidth;
    this.intervalTableRef.scrollLeft = scrollTo;
    if (this.dayDividersRef) {
      this.dayDividersRef.scrollDayIndicator(scrollTo);

    }
  }
  public componentDidUpdate() {
    if (this.dayDividersRef) {
      this.dayDividersRef.scrollDayIndicator(this.intervalTableRef.scrollLeft);

    }
  }
}
