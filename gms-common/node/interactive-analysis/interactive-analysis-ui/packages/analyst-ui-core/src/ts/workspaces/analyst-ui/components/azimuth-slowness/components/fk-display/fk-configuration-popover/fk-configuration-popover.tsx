import { Button, ButtonGroup } from '@blueprintjs/core';
import { cloneDeep } from 'apollo-utilities';
import * as React from 'react';
import { FkConfigurationWithUnits } from '~analyst-ui/components/azimuth-slowness/types';
import { FkConfigurationAdvancedPanel } from './fk-configuration-advanced-panel';
import { FkConfigurationDefaultPanel } from './fk-configuration-default-panel';
import { FkConfigurationPopoverPanel, FkConfigurationPopoverProps,
  FkConfigurationPopoverState } from './types';

/**
 * FkConfigurationPopover component.
 */
export class FkConfigurationPopover extends React.Component<FkConfigurationPopoverProps, FkConfigurationPopoverState> {

  private constructor(props: FkConfigurationPopoverProps) {
    super(props);
    this.state = {
        openPanel: FkConfigurationPopoverPanel.DEFAULT,
        fkUnits: props.fkUnitDisplayed,
        normalizeWaveforms: props.normalizeWaveforms,
        channelCheckboxes: props.contributingChannelsConfiguration.map(channel => ({
            id: channel.id,
            name: channel.name ? channel.name : '',
            checked: channel.enabled,
        })),
        useVerticalChannelOffsets: props.useChannelVerticalOffset,
        mediumVelocity: props.mediumVelocity,
        maximumSlowness: props.maximumSlowness,
        numberOfPoints: props.numberOfPoints

    };
  }

  /**
   * React component lifecycle.
   */
  public render() {
    return (
      <div
        className="continous-fk-popover"
      >
        <div
          className="form__header"
        >
          FK Configuration
        </div>
        <div
            className="popover-button-group"
        >
            <ButtonGroup>
                <Button
                    onClick={this.openDefaultPanel}
                    active={this.state.openPanel === FkConfigurationPopoverPanel.DEFAULT}
                    text="Options"
                />
                <Button
                    onClick={this.openAdvancedPanel}
                    active={this.state.openPanel === FkConfigurationPopoverPanel.ADVANCED}
                    text="Advanced"
                />
            </ButtonGroup>
        </div>
        <div>
            {
                this.state.openPanel === FkConfigurationPopoverPanel.DEFAULT ?
                    <FkConfigurationDefaultPanel
                        normalizeWaveforms={this.state.normalizeWaveforms}
                        channelCheckboxes={this.state.channelCheckboxes}
                        fkUnits={this.state.fkUnits}
                        setNormalizeWaveforms={val => {
                            this.setState({normalizeWaveforms: val});
                        }}
                        handleFkUnitsChange={this.handleFkUnitsChange}
                        setChannelEnabledById={(id: string, val: boolean) => {
                          const newChannelState = cloneDeep(this.state.channelCheckboxes);
                          const changedChannelIndex = newChannelState.findIndex(chn => chn.id === id);
                          newChannelState[changedChannelIndex].checked = val;
                          this.setState({channelCheckboxes: newChannelState});
                        }}
                    />
                    : <FkConfigurationAdvancedPanel
                        mediumVelocity={this.state.mediumVelocity}
                        maximumSlowness={this.state.maximumSlowness}
                        numberOfPoints={this.state.numberOfPoints}
                        useVerticalChannelOffsets={this.state.useVerticalChannelOffsets}
                        setVerticalChannelOffsets={val => {
                          this.setState({useVerticalChannelOffsets: val});
                        }}
                        setMediumVelocity={val => {
                          this.setState({mediumVelocity: val});
                        }}
                        setMaximumSlowness={val => {
                          this.setState({maximumSlowness: val});
                        }}
                        setNumberOfPoints={val => {
                          this.setState({numberOfPoints: val});
                        }}
                    />
            }
        </div>
        <div
          className="continous-fk-popover-apply-cancel-row"
        >
          <Button
            text="Apply"
            title="Request new FK spectrum with given parameters"
            onClick={() => {
              const fkConfigWithUnits: FkConfigurationWithUnits = {
                fkUnitToDisplay: this.state.fkUnits,
                maximumSlowness: this.state.maximumSlowness,
                mediumVelocity: this.state.mediumVelocity,
                numberOfPoints: this.state.numberOfPoints,
                contributingChannelsConfiguration: this.state.channelCheckboxes.map(cc => ({
                  id: cc.id,
                  name: cc.name,
                  enabled: cc.checked
                })),
                normalizeWaveforms: this.state.normalizeWaveforms,
                useChannelVerticalOffset: this.state.useVerticalChannelOffsets,
                leadFkSpectrumSeconds: this.props.leadFkSpectrumSeconds
              };
              this.props.applyFkConfiguration(fkConfigWithUnits);
            }}
            className="continous-fk-popover-apply"
          />
          <Button
            text="Cancel"
            title="Discard changes"
            onClick={() => {this.props.close(); }}
            className="continous-fk-popover-cancel"

          />
        </div>
      </div>
    );
  }

  private readonly openDefaultPanel = () => {
    this.setState({
        openPanel: FkConfigurationPopoverPanel.DEFAULT
    });
  }

  private readonly openAdvancedPanel = () => {
    this.setState({
        openPanel: FkConfigurationPopoverPanel.ADVANCED
    });
  }

  private readonly handleFkUnitsChange = (val: any) => {
      this.setState({fkUnits: val});
  }
}
