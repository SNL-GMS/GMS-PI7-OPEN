/*
 * The marker between days in the workflow table
 * It is kept in synch with the workflow table through its onScroll callback
*/

import * as d3 from 'd3';
import * as moment from 'moment';
import * as React from 'react';

/**
 * DayBoundaryIndicator Props
 */
export interface DayBoundaryIndicatorProps {
  startTimeSecs: number;
  endTimeSecs: number;
  pixelsPerHour: number;
}

const SECONDS_PER_HOUR = 3600;

/*
* @DayBoundaryIndicator
* Line indicating the transition from one day from the next
*/
export class DayBoundaryIndicator extends React.Component<
  DayBoundaryIndicatorProps,
  {}
  > {

  private dayIndicators: HTMLDivElement[] = [];

  /**
   * Constructor.
   *
   * @param props The initial props
   */
  public constructor(props: DayBoundaryIndicatorProps) {
    super(props);
  }

  public render() {
    const totalDurationSecs = moment
      .unix(this.props.endTimeSecs)
      .diff(moment.unix(this.props.startTimeSecs), 'seconds');
    const rowWidthPx = Math.ceil(
      totalDurationSecs / SECONDS_PER_HOUR * this.props.pixelsPerHour
    );

    const dividers = d3.utcDay.every(1)
      .range(
        moment
          .unix(this.props.startTimeSecs)
          // tslint:disable-next-line:no-magic-numbers
          .subtract(10, 's')
          .toDate(),
        moment.unix(this.props.endTimeSecs)
          .toDate()
      );
    this.dayIndicators = [];
    return dividers.map(date => {
      const widthPx = 4;
      // calculation that sets the initial offset of the day indicator
      const left = `calc(${(date.valueOf() / 1000 - this.props.startTimeSecs) /
        (this.props.endTimeSecs - this.props.startTimeSecs) *
        rowWidthPx}px + ${widthPx / 4}px)`;
      // The divider's base offset from the workflow is set to the 'left' style - it is based
      // on the timeSecs of the day boundary and does not change.

      return (
        <div
          key={date.valueOf()}
          className="workflow-day-divider"
          style={{
            left
          }}
          ref={ref => {
            this.dayIndicators.push(ref);
          }}
        />
      );
    });
  }
  /**
   * Synchronizes the day indicator with the main display
   * 
   * @param scrollTo pixel value reflecting how scrolled the workflow is
   */
  public readonly scrollDayIndicator = (scrollTo: number) => {
    // hard-coded value to account for distance between the workflow table and the edge of the workflow display
    const marginOfIntervalTable = 31;
    // removes any references that have become invalid (which happens every rerender)
    this.dayIndicators = this.dayIndicators.filter(
      di => di !== undefined && di !== null
    );
    // To enable scrolling, we change the marginLeft of the interval boundary. The 'left' position property
    // Remains static
    this.dayIndicators.forEach(
      di =>
        (di.style.marginLeft =
          (-scrollTo + marginOfIntervalTable).toString() + 'px')
    );
  }
}
