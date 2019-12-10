import { Switch } from '@blueprintjs/core';
import * as React from 'react';

interface ToolbarPopoverState {
  switch: boolean;
}
interface ToolbarPopoverProps {
  defaultValue: boolean;
  onChange(value);
}
export class ToolbarPopover extends React.Component<ToolbarPopoverProps, ToolbarPopoverState> {

  public constructor(props: ToolbarPopoverProps) {
    super(props);
    this.state = {
        switch: false
    };
  }

  /**
   * React render method
   */
  public render() {
    return (
      <div
        className="big-purple-rectangle"
      >
        <span
          style={{color: 'black'}}
        >
          This popover displays whatever it wants
        </span>
          <Switch
              defaultChecked={this.props.defaultValue}
              onChange={e => {this.onPopoverSwitch(e.currentTarget.checked); }}
          />
        <span
          style={{color: 'black'}}
        >
          Changes made are not sent to the state until the popover is closed [ but you could do it differently ]
        </span>

      </div>
    );
  }

  public componentWillUnmount() {
      this.props.onChange(this.state.switch);
  }

  /** 
   * Returns the state
   * 
   * @returns State of type ToolbarPopoverState
   */
  public getState = () => this.state;

  private readonly onPopoverSwitch = (value: boolean) => {
    this.setState({
      switch: value
    });
  }

}
