import * as React from 'react';
import { SignalDetection } from '~graphql/signal-detection/types';

export interface SDDirtyDotPopupProps {
    signalDetection: SignalDetection;
}
/**
 * Displays signal detection information in tabular form
 */
export class SDDirtyDotPopup extends React.Component<SDDirtyDotPopupProps, {}> {

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
                this.props.signalDetection.modified ?
                    (<div>Signal Detection has unsaved changes</div>)
                    : null
            }
            {/* In the future, I'd like to list which events were modified */}
            {
                this.props.signalDetection.associationModified ?
                    (<div>Signal Detection associations are unsaved</div>)
                    : null
            }
        </div >
    );
  }
}
