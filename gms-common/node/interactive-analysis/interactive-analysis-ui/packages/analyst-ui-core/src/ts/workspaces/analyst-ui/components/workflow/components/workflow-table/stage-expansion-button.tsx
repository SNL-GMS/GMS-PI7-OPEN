/*
    A button that can be clicked to expand/collapse activity intervals
    Does not render if a stage has no activity intervalss
*/

import { Button } from '@blueprintjs/core';
import * as React from 'react';

/**
 * StageExpansionButton Props
 */
export interface StageExpansionButtonProps {
  isExpanded: boolean;
  disabled: boolean;
  stageName: string;
  // called by clicks on the expansion button
  reportExpansion(stageName: string): void;
}

/**
 * StageExpansionButton State
 */
export interface StageExpansionButtonState {
  isExpanded: boolean;
}

/*
* @StageExpansionButton
* Button to expand stages when they are present
* By design, will not render if no stages are present
*/
export class StageExpansionButton extends React.Component<
  StageExpansionButtonProps,
  StageExpansionButtonState
  > {
  /**
   * Constructor.
   *
   * @param props The initial props
   */
  public constructor(props: StageExpansionButtonProps) {
    super(props);
    this.state = {
      isExpanded: this.props.isExpanded
    };
  }

  public render() {
    return !this.props.disabled ? (
      <Button
        key={this.props.stageName}
        className={'stage-row__expand-button'}
        icon={this.props.isExpanded ? 'small-minus' : 'small-plus'}
        onClick={e => this.props.reportExpansion(this.props.stageName)}
        disabled={this.props.disabled}
      />
    ) : null;
  }
}
