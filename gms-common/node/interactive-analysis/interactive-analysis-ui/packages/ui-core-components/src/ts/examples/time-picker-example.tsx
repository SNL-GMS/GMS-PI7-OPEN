/**
 * Example of using the form that actually accepts input
 */

import * as React from 'react';
import { TimePicker } from '../components';
import { TimeUtil } from '../utils';
interface TimePickerExampleState {
  date: Date;
  hold: boolean;
}
/**
 * Example displaying how to use the Table component.
 */
export class TimePickerExample extends React.Component<{}, TimePickerExampleState> {
  public constructor(props: {}) {
    super(props);
    // tslint:disable-next-line:no-magic-numbers
    this.state = {date: new Date(1182038443000), hold: false};
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
        <TimePicker
          onMaybeDate={this.onMaybeDate}
          date={this.state.date}
          datePickerEnabled={true}
        />
        <div style={{color: '#D7B740', fontFamily: 'monospace'}}>
          {`Date: ${TimeUtil.dateToISOString(this.state.date)}`}
          <br/>
        </div>
      </div>
    );
  }

  private readonly onMaybeDate = (date: Date | undefined) => {
    if (date !== undefined) {
      this.setState({date, hold: false});
    } else {
      this.setState({...this.state, hold: true});
    }
  }
}
