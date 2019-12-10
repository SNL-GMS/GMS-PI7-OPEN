/*
 * The label for a stage or activity
 * They are absolutely positioned s.t. they float above and to the right of interval cells
 */

import * as classNames from 'classnames';
import * as React from 'react';

/**
 * WorkflowRowLabel Props
 */
export interface WorkflowRowLabelProps {
  isExpanded: boolean;
  label: string;
  isActivityRow: boolean;
}

/**
 * WorkflowRowLabel State
 */
export interface WorkflowRowLabelState {
  isExpanded: boolean;
}

/*
* @WorkflowRowLabel
* Label for the stage or activity in the WorkFlowRow
*/
export class WorkflowRowLabel extends React.Component<
  WorkflowRowLabelProps,
  WorkflowRowLabelState
  > {
  /**
   * Constructor.
   *
   * @param props The initial props
   */
  public constructor(props: WorkflowRowLabelProps) {
    super(props);
    this.state = {
      isExpanded: this.props.isExpanded
    };
  }
  public render() {
    return (
      <div
        key={this.props.label}
        className={classNames('workflow-table-label', {
          'workflow-table-label--activity': this.props.isActivityRow
        })}
      // style={{left: leftPx}}
      >
        <div>{this.props.label}</div>
      </div>
    );
  }
}
