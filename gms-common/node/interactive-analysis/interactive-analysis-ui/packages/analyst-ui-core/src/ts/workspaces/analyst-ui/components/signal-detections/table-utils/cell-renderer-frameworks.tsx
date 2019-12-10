import { Icon, Intent, Tooltip } from '@blueprintjs/core';
import { IconNames } from '@blueprintjs/icons';
import * as React from 'react';
import { userPreferences } from '~analyst-ui/config/user-preferences';
import { SDConflictPopup } from '../components/sd-conflict-popup';
import { SDDirtyDotPopup } from '../components/sd-dirty-dot-popup';

/**
 * Renders the Detection color cell for the signal detection list
 */
export class DetectionColorCellRenderer extends React.Component<any, {}> {

  public constructor(props) {
    super(props);
  }

  /**
   * react component lifecycle
   */
  public render() {
    return (
      <div
        style={{
          height: '20px',
          width: '20px',
          backgroundColor: this.props.data.color,
        }}
      />
    );
  }
}

/**
 * Renders the modified color cell for the signal detection list
 */
export class ModifiedDot extends React.Component<any, {}> {

  public constructor(props) {
    super(props);
  }

  /**
   * react component lifecycle
   */
  public render() {
    return (
      <Tooltip
        content={(
          <SDDirtyDotPopup
            signalDetection={this.props.data}
          />
        )}
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

/**
 * Renders the modified color cell for the signal detection list
 */
export class SignalDetectionConflictMarker extends React.Component<any, {}> {

  public constructor(props) {
    super(props);
  }
  /**
   * react component lifecycle
   */
  public render() {
    return this.props.data.possiblyConflictingEvents.length > 1 ?
        (
          <Tooltip
            content={(
              <SDConflictPopup
                events={this.props.data.possiblyConflictingEvents}
              />
            )}
          >
            <Icon
              icon={IconNames.ISSUE}
              intent={Intent.DANGER}
            />
          </Tooltip>
        )
        : null;

  }
}
