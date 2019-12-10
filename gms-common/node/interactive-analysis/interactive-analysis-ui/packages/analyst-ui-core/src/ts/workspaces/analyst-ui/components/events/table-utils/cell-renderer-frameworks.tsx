
import { Button, Classes, Icon, Intent, Tooltip } from '@blueprintjs/core';
import { IconNames } from '@blueprintjs/icons';
import * as classNames from 'classnames';
import * as React from 'react';
import { analystUiConfig, userPreferences } from '~analyst-ui/config';
import { EventConflictPopup } from '../components/event-conflict-popup';
import { EventDirtyDotPopup } from '../components/event-dirty-dot-popup';

/**
 * Renders the 'Mark Complete' button for the event list
 */
export class MarkCompleteCellRenderer extends React.Component<any, {}> {

  public constructor(props) {
    super(props);
  }

  /**
   * React component lifecycle
   */
  public render() {
    if (this.props.data.status === 'Complete') {
      return (
        <div
          style={{
            color: analystUiConfig.userPreferences.colors.events.complete,
          }}
        >
          Complete
        </div>
      );
    } else {
      return (
        <Button
          className={classNames(Classes.SMALL, 'event-list__mark-complete-button')}
          text="Mark Complete"
          disabled={this.props.data.conflictingSdHyps.length > 0}
          intent={Intent.PRIMARY}
          onClick={e => this.props.context
            .markEventComplete([this.props.data.id], this.props.data.stageId)
          }
        />
      );
    }
  }
}

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
export class EventConflictMarker extends React.Component<any, {}> {

  public constructor(props) {
    super(props);
  }

  /**
   * react component lifecycle
   */
  public render() {
    return this.props.data.conflictingSdHyps.length > 0 ?
        (
          <Tooltip
            content={(
              <EventConflictPopup
                signalDetectionHyps={this.props.data.conflictingSdHyps}
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

/**
 * Renders the modified color cell for the signal detection list
 */
export class EventModifiedDot extends React.Component<any, {}> {

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
          <EventDirtyDotPopup
            event={this.props.data}
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
