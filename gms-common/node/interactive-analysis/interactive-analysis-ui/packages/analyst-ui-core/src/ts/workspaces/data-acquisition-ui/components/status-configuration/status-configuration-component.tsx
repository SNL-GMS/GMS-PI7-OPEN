import {
  Button,
  ButtonGroup,
  Classes,
  Intent,
  NonIdealState,
  Popover,
  PopoverInteractionKind,
  Position,
  Spinner
} from '@blueprintjs/core';
import { Table, TableApi } from '@gms/ui-core-components';
import * as classNames from 'classnames';
import { defer, sortBy } from 'lodash';
import * as React from 'react';
import { createDropdownItems } from '~util/display-util';
import { columnDefs, popoverColumnDefs } from './table-utils/column-defs';
import {
  Acquisition,
  FilterType,
  PkiInUse,
  PopoverEdcRow,
  ProcessingPartition,
  StatusConfigurationRow,
  StatusConfigurationsProps,
  StatusConfigurationsState,
  StoreOnAcquisitionPartition
} from './types';
import {
  generateAcquisition,
  generateDataCenter,
  generatePkiInUse,
  generatePkiStatus,
  generatePopoverEdcTableData,
  generateProcessingPartition,
  generateStoreOnAcquisitionPartition
} from './util/data-generation';

export class StatusConfigurations extends React.PureComponent<StatusConfigurationsProps, StatusConfigurationsState> {
  /**
   * To interact directly with the table
   */
  private mainTable: TableApi;

  public constructor(props: StatusConfigurationsProps) {
    super(props);
    this.state = {
      originalTableData: [],
      tableData: [],
      popupIsOpen: false,
      selectedFilter: FilterType.ALL_STATIONS,
      disableEditSelected: true,
      popover: {
        acquisitionValue: Acquisition.ACQUIRE,
        pkiInUseValue: PkiInUse.ENABLED,
        processingPartitionValue: ProcessingPartition.UPLOAD,
        storeOnAcquisitionParitionValue: StoreOnAcquisitionPartition.STORE,
        edcTableData: generatePopoverEdcTableData()
      }
    };
  }

  // ***************************************
  // BEGIN REACT COMPONENT LIFECYCLE METHODS
  // ***************************************

  /**
   * Generate StatusConfigurationRow[] based off of stations returned from query.
   *
   * @param defaultStations default stations
   *
   * @returns StatusConfigurationRow[]
   */
  private static readonly generateTableData = (props: StatusConfigurationsProps): StatusConfigurationRow[] => {
    // get all defaultStations information
    const defaultStations = props.defaultStationsQuery ?
      props.defaultStationsQuery.defaultStations : undefined;
    let statusConfigurationListData = [];
    if (defaultStations) {

      defaultStations.forEach(station => {
        const tempStatusConfigurationRow: StatusConfigurationRow = {
          id: station.id,
          station: station.name,
          acquisition: generateAcquisition(),
          pkiStatus: generatePkiStatus(),
          pkiInUse: generatePkiInUse(),
          processingPartition: generateProcessingPartition(),
          storeOnAcquisitionPartition: generateStoreOnAcquisitionPartition(),
          edcA: generateDataCenter(),
          edcB: generateDataCenter(),
          edcC: generateDataCenter(),
          color: 'pink',
          modified: false
        };
        statusConfigurationListData.push(tempStatusConfigurationRow);
      });
      statusConfigurationListData = sortBy(statusConfigurationListData, 'station');
    }
    return statusConfigurationListData;
  }

  /**
   * Updates the derived state from the next props.
   * Generates the table data only if it
   * hasn't already been generated.
   *
   * @param nextProps The next (new) props
   * @param prevState The previous state
   */
  public static getDerivedStateFromProps(nextProps: StatusConfigurationsProps, prevState: StatusConfigurationsState) {
    if (nextProps.defaultStationsQuery && nextProps.defaultStationsQuery.defaultStations) {
      if (prevState.tableData.length === 0) {
        const data = StatusConfigurations.generateTableData(nextProps);
        return {
          tableData: data,
          originalTableData: data
        };
      }
    }
    return null;
  }

  public render() {
    if (!this.state.selectedFilter) {
      return (
        <NonIdealState
          action={<Spinner intent={Intent.DANGER} />}
          title="Error"
          description={'No filter available.'}
        />
      );
    }
    if (this.props.defaultStationsQuery.loading) {
      return (
        <NonIdealState
          action={<Spinner intent={Intent.PRIMARY} />}
          title="Loading:"
          description={'Status Configuration...'}
        />
      );
    }

    return (
      <div className={classNames('ag-theme-dark', 'table-container-hide-overflow')}>
        <div className={classNames(Classes.SELECT, Classes.FILL, 'list-filter-select')} >
          <select
            value={this.state.selectedFilter}
            onChange={this.handleFilterChange}
          >
            {createDropdownItems(FilterType)}
          </select>
        </div>

        <div className={'list-wrapper'}>
          <div className={'max'}>
            <Table
              context={{
                updateAcquisition: this.updateAcquisition,
                updatePkiInUse: this.updatePkiInUse,
                updateProcessingPartition: this.updateProcessingPartition,
                updateStoreOnAcquisitionPartition: this.updateStoreOnAcquisitionPartition,
                edcAToggle: this.edcAToggle,
                edcBToggle: this.edcBToggle,
                edcCToggle: this.edcCToggle
              }}
              onGridReady={this.onMainTableReady}
              columnDefs={columnDefs}
              rowData={this.filterTableData(this.state.tableData)}
              getRowNodeId={node => node.id}
              deltaRowDataMode={true}
              rowSelection="multiple"
              rowDeselection={true}
              onRowSelected={e => this.onRowSelected(e)}
            />
          </div>
        </div>
        <p />
        <ButtonGroup>
          <Popover
            content={
              <div className={'data-acquisition-popover'}>
                <label>Acquisition<br /></label>
                {this.popoverDropdown(this.state.popover.acquisitionValue, Acquisition,
                                      this.popoverAcquisitionOnChange)}
                <label>PKI in use<br /></label>
                {this.popoverDropdown(this.state.popover.pkiInUseValue, PkiInUse, this.popoverPkiInUseOnChange)}
                <label>Processing partition<br /></label>
                {this.popoverDropdown(this.state.popover.processingPartitionValue, ProcessingPartition,
                                      this.popoverProcessingPartitionOnChange)}
                <label>Store on acquisition partition<br /></label>
                {this.popoverDropdown(this.state.popover.storeOnAcquisitionParitionValue,
                                      StoreOnAcquisitionPartition, this.popoverStoreOnAcquisitionParitionOnChange)}
                <br />
                {this.popoverEDCTable()}
                <div
                  style={{
                    display: 'flex',
                    justifyContent: 'flex-end'
                  }}
                >
                  <ButtonGroup>
                    <div className={Classes.POPOVER_DISMISS}>
                      <Button
                        small={true}
                        text="Apply"
                        onClick={e => this.updateTableWithDropdownValues()}
                      />
                    </div>
                    <div className={Classes.POPOVER_DISMISS}>
                      <Button
                        small={true}
                        text="Discard"
                        onClick={e => this.discardPopoverChanges(e)}
                      />
                    </div>
                  </ButtonGroup><br />
                </div>
              </div>}
            interactionKind={PopoverInteractionKind.CLICK}
            isOpen={this.state.popupIsOpen}
            onInteraction={state => this.handlePopoverInteraction(state)}
            position={Position.TOP_RIGHT}
            onClose={this.discardPopoverChanges}
          >
            <Button
              small={true}
              text="Edit selected..."
              disabled={this.state.disableEditSelected}
            />
          </Popover>
          <div className={'list__button-row-divider-large'} />
          <Button
            small={true}
            text="Select all"
            onClick={e => this.mainTable.selectAll()}
          />
          <div className={'list__button-row-divider-small'} />
          <Button
            small={true}
            text="Deselect all"
            onClick={e => this.mainTable.deselectAll()}
          />
          <div className={'list__button-row-divider-small'} />
          <Button
            small={true}
            text="Save"
            onClick={e => this.saveChanges()}
          />
          <Button
            small={true}
            text="Discard"
            onClick={e => this.discardChanges()}
          />
        </ButtonGroup>
      </div>
    );
  }

  /**
   * Handles the filter dropdown change event.
   *
   * @param event react form event
   *
   * Updates the state with the new filter type
   */
  private readonly handleFilterChange = (event: React.FormEvent<HTMLSelectElement>) => {
    const newFilter = event.currentTarget.value as FilterType;
    this.setState((prevState: StatusConfigurationsState) => ({
      selectedFilter: newFilter,
    }));
  }

  /**
   * Check if multiple rows are selected; disable Edit Selected... button if not
   */
  private readonly onRowSelected = (event: any) => {
    if (this.mainTable) {
      const multipleNodesSelected = this.mainTable.getSelectedNodes().length > 1;
      this.setState({ disableEditSelected: !multipleNodesSelected });
    }
  }

  /**
   * Uses a switch statement to determine the appropriate filtering option.
   *
   * @param data an array of station information table rows
   *
   * @returns filtered list based off filter type
   */
  private readonly filterTableData = (data: StatusConfigurationRow[]): StatusConfigurationRow[] => {
    let filteredList = [];
    switch (this.state.selectedFilter) {
      case 'All stations':
        filteredList = data;
        break;
      case 'Acquire':
        filteredList = data.filter(statusConfiguration =>
          statusConfiguration.acquisition === 'Acquire');
        break;
      case 'Don\'t Acquire':
        filteredList = data.filter(statusConfiguration =>
          statusConfiguration.acquisition === 'Don\'t Acquire');
        break;
      case 'PKI in use':
        filteredList = data.filter(statusConfiguration =>
          statusConfiguration.pkiInUse === 'Enabled');
        break;
      case 'PKI not in use':
        filteredList = data.filter(statusConfiguration =>
          statusConfiguration.pkiInUse === 'Disabled');
        break;
      case 'Upload to Processing Partition':
        filteredList = data.filter(statusConfiguration =>
          statusConfiguration.processingPartition === 'Upload');
        break;
      case 'Don\'t Upload to Processing Partition':
        filteredList = data.filter(statusConfiguration =>
          statusConfiguration.processingPartition === 'Don\'t upload');
        break;
      case 'Store on Acquisition Partition':
        filteredList = data.filter(statusConfiguration =>
          statusConfiguration.storeOnAcquisitionPartition === 'Store');
        break;
      case 'Don\'t Store on Acquisition Partition':
        filteredList = data.filter(statusConfiguration =>
          statusConfiguration.storeOnAcquisitionPartition === 'Don\'t store');
        break;
      default:
    }
    return filteredList;
  }

  /**
   * Deselects and removes dirty dot from all rows.
   */
  private readonly cleanupAllRows = () => {
    this.mainTable.deselectAll();
    this.mainTable.forEachNode(node => {
      node.setDataValue('modified', false);
    });
  }

  /**
   * Saves the current table data to the state and cleans up rows.
   */
  private readonly saveChanges = () => {
    this.cleanupAllRows();
    this.setState({ originalTableData: this.state.tableData });
  }

  /**
   * Discard all changes made to the table and revert to previous table state
   */
  private readonly discardChanges = () => {
    this.cleanupAllRows();
    this.setState({ tableData: this.state.originalTableData });
  }

  /**
   * Set Acqusition state value when Acquisition dropdown is selected
   *
   * @param stationId id of the station row being updated
   * @param newValue new dropdown value
   */
  private readonly updateAcquisition = (stationId: string, newValue: Acquisition) => {
    const indexedValue = this.state.tableData.find(r => r.id === stationId);
    const td: StatusConfigurationRow[] = this.state.tableData.map(a => ({ ...a }));
    td.find(a => a.id === indexedValue.id).acquisition = newValue;
    td.find(a => a.id === indexedValue.id).modified = true;
    defer(() => this.setState({ tableData: td }));
  }

  /**
   * Set PkiInUse state value when PkiInUse dropdown is selected
   *
   * @param stationId id of the station row being updated
   * @param newValue new dropdown value
   */
  private readonly updatePkiInUse = (stationId: string, newValue: PkiInUse) => {
    const indexedValue = this.state.tableData.find(r => r.id === stationId);
    const td: StatusConfigurationRow[] = this.state.tableData.map(a => ({ ...a }));
    td.find(a => a.id === indexedValue.id).pkiInUse = newValue;
    td.find(a => a.id === indexedValue.id).modified = true;
    defer(() => this.setState({ tableData: td }));
  }

  /**
   * Set ProcessingPartition state value when ProcessingPartition dropdown is selected
   *
   * @param stationId id of the station row being updated
   * @param newValue new dropdown value
   */
  private readonly updateProcessingPartition = (stationId: string, newValue: ProcessingPartition) => {
    const indexedValue = this.state.tableData.find(r => r.id === stationId);
    const td: StatusConfigurationRow[] = this.state.tableData.map(a => ({ ...a }));
    td.find(a => a.id === indexedValue.id).processingPartition = newValue;
    td.find(a => a.id === indexedValue.id).modified = true;
    defer(() => this.setState({ tableData: td }));
  }

  /**
   * Set StoreOnAcquisitionPartition state value when StoreOnAcquisitionPartition dropdown is selected
   *
   * @param stationId id of the station row being updated
   * @param newValue new dropdown value
   */
  private readonly updateStoreOnAcquisitionPartition = (stationId: string, newValue: StoreOnAcquisitionPartition) => {
    const indexedValue = this.state.tableData.find(r => r.id === stationId);
    const td: StatusConfigurationRow[] = this.state.tableData.map(a => ({ ...a }));
    td.find(a => a.id === indexedValue.id).storeOnAcquisitionPartition = newValue;
    td.find(a => a.id === indexedValue.id).modified = true;
    defer(() => this.setState({ tableData: td }));
  }

  /**
   * Toggle EDCA state value when EDCA checkbox clicked
   *
   * @param stationId id of the station row being updated
   */
  private readonly edcAToggle = (stationId: string) => {
    const indexedValue = this.state.tableData.find(r => r.id === stationId);
    const td: StatusConfigurationRow[] = this.state.tableData.map(a => ({ ...a }));
    const currentValue = td.find(a => a.id === indexedValue.id).edcA;
    td.find(a => a.id === indexedValue.id).edcA = !currentValue;
    td.find(a => a.id === indexedValue.id).modified = true;
    defer(() => this.setState({ tableData: td }));
  }

  /**
   * Toggle EDCB state value when EDCA checkbox clicked
   *
   * @param stationId id of the station row being updated
   */
  private readonly edcBToggle = (stationId: string) => {
    const indexedValue = this.state.tableData.find(r => r.id === stationId);
    const td: StatusConfigurationRow[] = this.state.tableData.map(a => ({ ...a }));
    const currentValue = td.find(a => a.id === indexedValue.id).edcB;
    td.find(a => a.id === indexedValue.id).edcB = !currentValue;
    td.find(a => a.id === indexedValue.id).modified = true;
    defer(() => this.setState({ tableData: td }));
  }

  /**
   * Toggle EDCC state value when EDCA checkbox clicked
   *
   * @param stationId id of the station row being updated
   */
  private readonly edcCToggle = (stationId: string) => {
    const indexedValue = this.state.tableData.find(r => r.id === stationId);
    const td: StatusConfigurationRow[] = this.state.tableData.map(a => ({ ...a }));
    const currentValue = td.find(a => a.id === indexedValue.id).edcC;
    td.find(a => a.id === indexedValue.id).edcC = !currentValue;
    td.find(a => a.id === indexedValue.id).modified = true;
    defer(() => this.setState({ tableData: td }));
  }
  /**
   * Set class members when main table is ready
   *
   * @param event event of the table action
   */
  private readonly onMainTableReady = (event: any) => {
    this.mainTable = event.api;
  }

  /**
   * Update selected rows in the table with values set in the popover
   */
  private readonly updateTableWithDropdownValues = () => {
    if (this.mainTable) {
      const td = this.state.tableData.map(a => ({ ...a }));
      const popoverTd: PopoverEdcRow[] = this.state.popover.edcTableData.map(a => ({ ...a }));
      const edcAEnabled = popoverTd.find(a => a.id === 'edcA').enabled;
      const edcBEnabled = popoverTd.find(a => a.id === 'edcB').enabled;
      const edcCEnabled = popoverTd.find(a => a.id === 'edcC').enabled;
      this.mainTable.getSelectedNodes()
        .forEach(rowNode => {
          const indexedRow = this.state.tableData.find(r => r.id === rowNode.id);
          td.find(a => a.id === indexedRow.id).acquisition = this.state.popover.acquisitionValue;
          td.find(a => a.id === indexedRow.id).pkiInUse = this.state.popover.pkiInUseValue;
          td.find(a => a.id === indexedRow.id).processingPartition = this.state.popover.processingPartitionValue;
          td.find(a => a.id === indexedRow.id).modified = true;
          td.find(a => a.id === indexedRow.id).storeOnAcquisitionPartition =
            this.state.popover.storeOnAcquisitionParitionValue;
          td.find(a => a.id === indexedRow.id).edcA = edcAEnabled;
          td.find(a => a.id === indexedRow.id).edcB = edcBEnabled;
          td.find(a => a.id === indexedRow.id).edcC = edcCEnabled;
        });
      defer(() => this.setState({ tableData: td }));
    }
  }

  // ***************************************
  // Popover
  // ***************************************

  /**
   * Called when the Edit Selected popover button is clicked. A popover will open if one or more nodes are selected
   * in the main table
   *
   * @param nextOpenState next open state if popup should be opened/closed
   */
  private readonly handlePopoverInteraction = (nextOpenState: boolean): boolean => {
    if (this.mainTable) {
      const nodes = this.mainTable.getSelectedNodes();
      if (nodes.length > 1) {
        this.setState({ popupIsOpen: nextOpenState });
        return true;
      } else {
        this.setState({ popupIsOpen: false });
        return false;
      }
    }
  }

  /**
   * Remove dirty dots, deselect rows, and reset popover state when Discard button on popover is clicked
   *
   * @param event Button click event
   */
  private readonly discardPopoverChanges = (event: any) => {
    if (this.mainTable) {
      const nodes = this.mainTable.getSelectedNodes();
      nodes.forEach(tableNode => {
        tableNode.setDataValue('modified', false);
      });
    }
    this.setState({
      popover:
      {
        acquisitionValue: Acquisition.ACQUIRE,
        pkiInUseValue: PkiInUse.ENABLED,
        processingPartitionValue: ProcessingPartition.UPLOAD,
        storeOnAcquisitionParitionValue: StoreOnAcquisitionPartition.STORE,
        edcTableData: this.state.popover.edcTableData
      }
    });
  }

  /**
   * Creates a dropdown box to use in the popover
   *
   * @param selectValue the dropdown's selected value
   * @param dropdownItems the items to put in the dropdown box
   * @param dropdownOnChange the function to call when the dropdown selection changes
   *
   * @return a dropdown box to use in the popover
   */
  private readonly popoverDropdown = (selectValue: any, dropdownItems: any,
    dropdownOnChange: any) =>
    (
      <div className={classNames(Classes.SELECT, Classes.FILL, 'list-filter-select')}>
        <select
          value={selectValue}
          onChange={dropdownOnChange}
        >
          {createDropdownItems(dropdownItems)}
        </select>
      </div>
    )

  /**
   * Creates a external data center table to use in the popover
   *
   * @return an external data center table to use in the popover
   */
  private readonly popoverEDCTable = () => (
    <div
      className={classNames('ag-theme-dark', 'table-container-hide-overflow')}
      style={{
        padding: 0,
        marginBottom: '8px'
      }}
    >
      <Table
        context={{ popoverEdcToggle: this.popoverEdcToggle }}
        columnDefs={popoverColumnDefs}
        rowData={this.state.popover.edcTableData}
        getRowNodeId={node => node.id}
        deltaRowDataMode={true}
      />
    </div>
  )

  /**
   * Set popover Acquisition state value when popover Acquisition dropdown selected
   *
   * @param event dropdown select event
   */
  private readonly popoverAcquisitionOnChange = (event: React.FormEvent<HTMLSelectElement>) => {
    const targetValue = event.currentTarget.value as Acquisition;
    const newPopover = { ...this.state.popover };
    newPopover.acquisitionValue = targetValue;
    this.setState({ popover: newPopover });
  }

  /**
   * Set popover PkiInUse state value when popover PkiInUse dropdown selected
   *
   * @param event dropdown select event
   */
  private readonly popoverPkiInUseOnChange = (event: React.FormEvent<HTMLSelectElement>) => {
    const targetValue = event.currentTarget.value as PkiInUse;
    const newPopover = { ...this.state.popover };
    newPopover.pkiInUseValue = targetValue;
    this.setState({ popover: newPopover });
  }

  /**
   * Set popover ProcessingPartition state value when popover ProcessingPartition dropdown selected
   *
   * @param event dropdown select event
   */
  private readonly popoverProcessingPartitionOnChange = (event: React.FormEvent<HTMLSelectElement>) => {
    const targetValue = event.currentTarget.value as ProcessingPartition;
    const newPopover = { ...this.state.popover };
    newPopover.processingPartitionValue = targetValue;
    this.setState({ popover: newPopover });
  }

  /**
   * Set popover StoreOnAcquisitionPartition state value when popover StoreOnAcquisitionPartition dropdown selected
   *
   * @param event dropdown select event
   */
  private readonly popoverStoreOnAcquisitionParitionOnChange = (event: React.FormEvent<HTMLSelectElement>) => {
    const targetValue = event.currentTarget.value as StoreOnAcquisitionPartition;
    const newPopover = { ...this.state.popover };
    newPopover.storeOnAcquisitionParitionValue = targetValue;
    this.setState({ popover: newPopover });
  }

  /**
   * Toggle the value of an EDC checkbox in the popover
   *
   * @param rowId The id of the row whose EDC checkbox is being toggled
   */
  private readonly popoverEdcToggle = (rowId: string) => {
    const indexedValue = this.state.popover.edcTableData.find(r => r.id === rowId);
    const td: PopoverEdcRow[] = this.state.popover.edcTableData.map(a => ({ ...a }));
    const currentValue = td.find(a => a.id === indexedValue.id).enabled;
    td.find(a => a.id === indexedValue.id).enabled = !currentValue;
    const newPopover = { ...this.state.popover };
    newPopover.edcTableData = td;
    defer(() => this.setState({ popover: newPopover }));
  }
}
