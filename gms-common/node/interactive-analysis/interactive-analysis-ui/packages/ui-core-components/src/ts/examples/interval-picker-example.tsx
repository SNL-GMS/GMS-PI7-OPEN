/**
 * Example of using the form that actually accepts input
 */

import * as React from 'react';
import { IntervalPicker } from '../components';
import { TimeUtil } from '../utils';
interface IntervalPickerExampleState {
  startDate: Date;
  endDate: Date;

}
/**
 * Example displaying how to use the Table component.
 */
export class IntervalPickerExample extends React.Component<{}, IntervalPickerExampleState> {
  public constructor(props: {}) {
    super(props);
    // tslint:disable-next-line:no-magic-numbers
    this.state = {startDate: new Date(1182038443000), endDate: new Date(1182124843000)};
  }

  /**
   * React render method
   */
  public render() {

    return (
      <div
        className="ag-dark"
        style={{
          flex: '1 1 auto',
          position: 'relative',
          width: '700px',
        }}
      >
        <IntervalPicker
          onNewInterval={this.onSubmit}
          startDate={this.state.startDate}
          endDate={this.state.endDate}
          onInvalidInterval={this.onInvalidInterval}
        />
        <div style={{color: '#D7B740', fontFamily: 'monospace'}}>
          {`Start Date: ${TimeUtil.dateToISOString(this.state.startDate)}`}
          <br/>
          {`End Date: ${TimeUtil.dateToISOString(this.state.endDate)}`}
        </div>
      </div>
    );
  }

  private readonly onSubmit = (interval: any) => {
    this.setState({startDate: interval.startDate, endDate: interval.endDate});
  }
  private readonly onInvalidInterval = (message: string) => {
    alert(message);
  }

}
