import * as React from 'react';
import { StationInformation } from '~graphql/data-acquisition/types';

export interface StationInformationDirtyDotPopupProps {
  stationInformation: StationInformation;
}
/**
 * Displays signal detection information in tabular form
 */
export class StationInformationDirtyDotPopup extends React.Component<StationInformationDirtyDotPopupProps, {}> {

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
          this.props.stationInformation.modified ?
            (<div>Station has unsaved changes</div>)
            : null
        }
      </div >
    );
  }
}
