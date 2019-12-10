import { Checkbox, Tooltip } from '@blueprintjs/core';
import * as React from 'react';
import { userPreferences } from '~analyst-ui/config';
import { createDropdownItems } from '~util/display-util';
import { StatusConfigurationDirtyDotPopup } from '../status-configuration-dirty-dot-popup';
import { Acquisition, PkiInUse, ProcessingPartition, StoreOnAcquisitionPartition } from '../types';

/**
 * Creates component for acquisition table dropdown.
 */
export class AcquisitionDropdown extends React.PureComponent<any, {}> {

  public constructor(props) {
    super(props);
  }
  public render() {
    return (
      <select
        className={'list__drop-down'}
        value={this.props.data.acquisition}
        onChange={(event: React.FormEvent<HTMLSelectElement>) => {
          const targetValue = event.currentTarget.value as Acquisition;
          this.props.context.updateAcquisition(this.props.data.id, targetValue);
        }}
      >
        {createDropdownItems(Acquisition)}
      </select>
    );
  }
}

export class PkiStatusRenderer extends React.PureComponent<any, {}> {
  public constructor(props) {
    super(props);
  }
  public render() {
    return (
      <select>
        className={'list__drop-down'}
        value={this.props.data.pkiInUse}
      </select>
    );
  }
}

export class PkiInUseDropdown extends React.PureComponent<any, {}> {
  public constructor(props) {
    super(props);
  }
  public render() {
    return (
      <select
        className={'list__drop-down'}
        value={this.props.data.pkiInUse}
        onChange={(event: React.FormEvent<HTMLSelectElement>) => {
          const targetValue = event.currentTarget.value as PkiInUse;
          this.props.context.updatePkiInUse(this.props.data.id, targetValue);
        }}
      >
        {createDropdownItems(PkiInUse)}
      </select>
    );
  }
}

export class ProcessingPartitionDropdown extends React.PureComponent<any, {}> {

  public constructor(props) {
    super(props);
  }
  public render() {
    return (
      <select
        className={'list__drop-down'}
        value={this.props.data.processingPartition}
        onChange={(event: React.FormEvent<HTMLSelectElement>) => {
          const targetValue = event.currentTarget.value as ProcessingPartition;
          this.props.context.updateProcessingPartition(this.props.data.id, targetValue);
        }}
      >
        {createDropdownItems(ProcessingPartition)}
      </select>
    );
  }
}

export class StoreOnAcquisitionPartitionDropdown extends React.Component<any, {}> {

  public constructor(props) {
    super(props);
  }
  public render() {
    return (
      <select
        className={'list__drop-down'}
        value={this.props.data.storeOnAcquisitionPartition}
        onChange={(event: React.FormEvent<HTMLSelectElement>) => {
          const targetValue = event.currentTarget.value as StoreOnAcquisitionPartition;
          this.props.context.updateStoreOnAcquisitionPartition(this.props.data.id, targetValue);
        }}
      >
        {createDropdownItems(StoreOnAcquisitionPartition)}
      </select>
    );
  }
}

// tslint:disable-next-line: max-classes-per-file
export class EdcACheckbox extends React.PureComponent<any, {}> {
  public constructor(props) {
    super(props);
  }
  public render() {
    return (
      <Checkbox
        label=""
        checked={this.props.data.edcA}
        onChange={() => this.props.context.edcAToggle(this.props.data.id)}
      />
    );
  }
}

// tslint:disable-next-line: max-classes-per-file
export class EdcBCheckbox extends React.PureComponent<any, {}> {
  public constructor(props) {
    super(props);
  }
  public render() {
    return (
      <Checkbox
        label=""
        checked={this.props.data.edcB}
        onChange={() => this.props.context.edcBToggle(this.props.data.id)}

      />
    );
  }
}

// tslint:disable-next-line: max-classes-per-file
export class EdcCCheckbox extends React.PureComponent<any, {}> {
  public constructor(props) {
    super(props);
  }
  public render() {
    return (
      <Checkbox
        label=""
        checked={this.props.data.edcC}
        onChange={() => this.props.context.edcCToggle(this.props.data.id)}
      />
    );
  }
}

// tslint:disable-next-line: max-classes-per-file
export class PopoverEdcCheckbox extends React.PureComponent<any, {}> {
  public constructor(props) {
    super(props);
  }
  public render() {
    return (
      <Checkbox
        label=""
        checked={this.props.data.enabled}
        onChange={() => this.props.context.popoverEdcToggle(this.props.data.id)}
      />
    );
  }
}

/**
 * Renders the modified dot.
 */
// tslint:disable-next-line:max-classes-per-file
export class StatusConfigurationModifiedDot extends React.Component<any, {}> {

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
          <StatusConfigurationDirtyDotPopup
            statusConfiguration={this.props.data}
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
