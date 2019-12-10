import { Switch } from '@blueprintjs/core';
import * as React from 'react';
import { systemConfig } from '~analyst-ui/config';

export interface FkConfigurationAdvancedPanelProps {
    mediumVelocity: number;
    maximumSlowness: number;
    numberOfPoints: number;

    useVerticalChannelOffsets: boolean;
    setVerticalChannelOffsets(val: boolean);
    setMediumVelocity(val: number);
    setNumberOfPoints(val: number);
    setMaximumSlowness(val: number);

}

/**
 * FkConfigurationDefaultPanel component.
 */
export const FkConfigurationAdvancedPanel: React.StatelessComponent<FkConfigurationAdvancedPanelProps> = props =>
    (
      <div
        className="continous-fk-popover-panel"
      >
        <div
          className="popover-section"
        >
          <div
            className="popover-header"
          >
            <div
              className="popover-header__text"
            >
              Use Vertical Channel Offsets
            </div>
            <div
              className="popover-header__widget"
            >
              <Switch
                checked={props.useVerticalChannelOffsets}
                onChange={() => {
                  props.setVerticalChannelOffsets(!props.useVerticalChannelOffsets);
                }}
              />
            </div>
          </div>
          <div
            className="popover-body-text"
          >
            Controls whether the FK calculation accounts for elevation differences
            between the array element positions
          </div>
        </div>
        <div
          className="popover-section"
          style={{
            width: '376px',
            marginLeft: '24px'
          }}
        >
          <div
            className="popover-header"
          >
            <div
              className="popover-header__text"
            >
              Medium Velocity (km/s)
            </div>
            <div
              className="popover-header__widget"
            >
              <input
                min={systemConfig.continousFkConfiguration.minMediumVelocity}
                max={systemConfig.continousFkConfiguration.maxMediumVelocity}
                // tslint:disable-next-line: no-magic-numbers
                step={0.1}
                title="Medium velocity"
                style={{
                  width: '54px'
                }}
                value={props.mediumVelocity}
                type="number"
                className={props.useVerticalChannelOffsets ? 'toolbar-numeric-input' : 'toolbar-numeric-input disabled'}
                disabled={!props.useVerticalChannelOffsets}
                onChange={event => {
                  if (!isNaN(event.currentTarget.valueAsNumber)) {
                    props.setMediumVelocity(event.currentTarget.valueAsNumber);
                  }
                }}
                onKeyDown={event => {
                  event.stopPropagation();
                  }
                }
              />
            </div>
          </div>
          <div
            className="popover-body-text"
          >
            Used to convert horizontal slowness to vertical slowness to determine delays due to elevation differences.
          </div>
        </div>
        <div
          className="popover-section"
        >
          <div
            className="popover-header"
          >
            <div
              className="popover-header__text"
            >
              FK Grid
            </div>
          </div>
          <div
            className="popover-body-text"
          >
            Controls the scale and resolution of the FK grid
          </div>
          <div
            className="popover-section"
            style={{
              width: '376px',
              marginLeft: '24px'
            }}
          >
          <div
            className="popover-header"
          >
            <div
              className="popover-header__text"
            >
              Maximum Slowness (s/°)
            </div>
            <div
              className="popover-header__widget"
            >
              <input
                min={systemConfig.continousFkConfiguration.minMaximumSlowness}
                max={systemConfig.continousFkConfiguration.maxMaximumSlowness}
                // tslint:disable-next-line: no-magic-numbers
                step={0.1}
                title="Maximum Slowness "
                value={props.maximumSlowness}
                type="number"
                style={{
                  width: '54px',
                  margin: '3px'
                }}
                className="toolbar-numeric-input"
                onChange={event => {
                  if (!isNaN(event.currentTarget.valueAsNumber)) {
                    props.setMaximumSlowness(event.currentTarget.valueAsNumber);
                  }
                }}
                onKeyDown={event => {
                  event.stopPropagation();
                  }
                }
              />
            </div>
          </div>
          <div
            className="popover-header"
          >
            <div
              className="popover-header__text"
            >
              Number of Points
            </div>
            <div
              className="popover-header__widget"
            >
              <input
                min={systemConfig.continousFkConfiguration.minNumberOfPoints}
                max={systemConfig.continousFkConfiguration.maxNumberOfPoints}
                step={1}
                style={{
                  width: '54px',
                  margin: '3px'
                }}
                title="Medium velocity"
                value={props.numberOfPoints}
                type="number"
                className="toolbar-numeric-input"
                onChange={event => {
                  if (!isNaN(event.currentTarget.valueAsNumber)) {
                    props.setNumberOfPoints(event.currentTarget.valueAsNumber);
                  }
                }}
                onKeyDown={event => {
                  event.stopPropagation();
                  }
                }
              />
            </div>
          </div>
          </div>
        </div>
      </div>
    );
