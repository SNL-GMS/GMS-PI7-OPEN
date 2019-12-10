import { FilterableOptionList } from '@gms/ui-core-components';
import * as React from 'react';
import { CommonTypes } from '~graphql/';

/**
 * How wide to render internal elements
 */
const widthPx = 160;

export interface PhaseSelectionMenuProps {
  phase?: CommonTypes.PhaseType;
  sdPhases: CommonTypes.PhaseType[];
  prioritySdPhases?: CommonTypes.PhaseType[];

  onBlur(phase: CommonTypes.PhaseType);
  onEnterForPhases?(phase: CommonTypes.PhaseType);
  onPhaseClicked?(phase: CommonTypes.PhaseType);
}

export interface PhaseSelectionMenuState {
  phase: CommonTypes.PhaseType;
}

/**
 * Phase selection menu.
 */
export class PhaseSelectionMenu extends
  React.Component<PhaseSelectionMenuProps, PhaseSelectionMenuState> {

  private constructor(props) {
    super(props);
    this.state = {
      phase: this.props.phase ? this.props.phase : CommonTypes.PhaseType.P
    };
  }
  /**
   * React component lifecycle.
   */
  public render() {
    return (
      <div
        className="alignment-dropdown"
      >
        <FilterableOptionList
          options={this.props.sdPhases}
          onSelection={this.onPhaseSelection}
          onClick={this.onClick}
          onDoubleClick={this.onClick}
          prioriotyOptions={this.props.prioritySdPhases}
          defaultSelection={this.props.phase}
          widthPx={widthPx}
          onEnter={this.props.onEnterForPhases}
        />
      </div>
    );
  }

  /**
   * Returns current state of menu
   * 
   * @returns PhaseSelectionMenuState
   */
  public getState = (): PhaseSelectionMenuState =>
    this.state

  /**
   * On phase selection event handler.
   * 
   * @param phase the selected phase
   */
  private readonly onPhaseSelection = (phase: CommonTypes.PhaseType) => {
    this.setState({ phase });
  }

  private readonly onClick = (phase: CommonTypes.PhaseType) => {
    if (this.props.onPhaseClicked) {
        this.props.onPhaseClicked(phase);
    } else {
      this.setState({ phase });
    }
  }
}
