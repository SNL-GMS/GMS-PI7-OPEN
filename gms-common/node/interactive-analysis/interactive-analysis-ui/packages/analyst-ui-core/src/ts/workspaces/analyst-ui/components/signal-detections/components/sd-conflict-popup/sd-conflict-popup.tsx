import { TimeUtil } from '@gms/ui-core-components';
import * as React from 'react';
import { Event } from '~graphql/event/types';

const THOUSAND = 1000;
export interface SDConflictPopupProps {
    events: Event[];
}
/**
 * Displays signal detection information in tabular form
 */
export class SDConflictPopup extends React.Component<SDConflictPopupProps, {}> {

  // ***************************************
  // BEGIN REACT COMPONENT LIFECYCLE METHODS
  // ***************************************

  /**
   * Constructor.
   * 
   * @param props The initial props
   */
  public constructor(props) {
    super(props);
  }

  /**
   * Renders the component.
   */
  public render() {

    return (
        <div>
            <div>Conflicts exist with events at:</div>
            <ul>
              {this.props.events.map(event => (
                <li
                  key={event.id}
                >
                  {TimeUtil.dateToHoursMinutesSeconds(
                    new Date(event.currentEventHypothesis
                    .eventHypothesis.preferredLocationSolution.locationSolution.location.time * THOUSAND))}
                </li>
              ))}
            </ul>
        </div >
    );
  }
}
