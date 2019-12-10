import * as React from 'react';
import { Event } from '~graphql/event/types';

export interface EventDirtyDotPopupProps {
    event: Event;
}
/**
 * Displays signal detection information in tabular form
 */
export class EventDirtyDotPopup extends React.Component<EventDirtyDotPopupProps, {}> {

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
            {
                this.props.event.modified ?
                    (<div>Event has unsaved changes</div>)
                    : null
            }
        </div >
    );
  }
}
