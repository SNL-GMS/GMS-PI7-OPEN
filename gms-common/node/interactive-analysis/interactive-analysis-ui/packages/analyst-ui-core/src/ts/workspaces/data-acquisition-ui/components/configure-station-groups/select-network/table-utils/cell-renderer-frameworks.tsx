import { Tooltip } from '@blueprintjs/core';
import * as React from 'react';
import { userPreferences } from '~analyst-ui/config';

/**
 * Renders the modified dot.
 */
// tslint:disable-next-line:max-classes-per-file
export class NetworkModifiedDot extends React.Component<any, {}> {

  public constructor(props) {
    super(props);
  }

  /**
   * react component lifecycle
   */
  public render() {
    return (
      <Tooltip
        className="dirty-dot-wrapper"
      >
        <div
          style={{
            backgroundColor: this.props.data.modified ?
              userPreferences.colors.signalDetections.unassociated
              : 'transparent',
          }}
          className="list-entry-dirty-dot"
        />
      </Tooltip>
    );
  }
}
