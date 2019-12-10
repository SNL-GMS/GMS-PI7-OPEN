import {
  Button,
  ButtonGroup,
  Classes,
  NonIdealState
} from '@blueprintjs/core';
import { IconNames } from '@blueprintjs/icons';
import { Table, TimeUtil } from '@gms/ui-core-components';
import classNames from 'classnames';
import * as lodash from 'lodash';
import * as React from 'react';
import { createDropdownItems } from '~util/display-util';
import { associatedStationsDefs } from './associated-stations/table-utils/column-defs';
import { availableStationsDefs } from './available-stations/table-utils/column-defs';
import { selectNetworkDefs } from './select-network/table-utils/column-defs';
import { ConfigureStationGroupsProps, ConfigureStationGroupsState, FilterType,
   NetworkRow, StationsRow, StationsSelectionState } from './types';

export class ConfigureStationGroups extends
  React.PureComponent<ConfigureStationGroupsProps, ConfigureStationGroupsState> {

  private rowId: number = 0;
  private gridApiNetworks: any;
  private gridApiAssoc: any;
  private gridApiAvailable: any;

  public constructor(props: ConfigureStationGroupsProps) {
    super(props);
    this.state = {
      networkOriginalTableData: [],
      networkTableData: [],
      currentNetworkID: '',
      networkToStationsMap: new Map([
        [
          `default_${this.rowId}`,
          {
            associatedStationsOriginalTableData: [],
            associatedStationsTableData:  ConfigureStationGroups.generateTableData(this.props),
            availableStationsOriginalTableData: [],
            availableStationsTableData: []
          }
        ]
      ]),
      showStationsTables: false,
      selectedFilter: FilterType.ACTIVE
    };
  }

  public static generateTableData = (props: ConfigureStationGroupsProps): StationsRow[] => {
    let availableStationListData = [];
    // get all defaultStations information
    const defaultStations = props.defaultStationsQuery ?
      props.defaultStationsQuery.defaultStations : undefined;
    if (defaultStations) {
      defaultStations.forEach(station => {
        const tempAvailableStationRow: StationsRow = {
          id: station.id,
          stations: station.name,
        };
        availableStationListData.push(tempAvailableStationRow);
      });
      availableStationListData = lodash.sortBy(availableStationListData, 'stations');
    }
    return availableStationListData;
  }

  public render() {
    const selectedNetworkData = this.getStationsSelectionState(this.state.currentNetworkID);
    return (
      <div
        className={classNames('ag-theme-dark', 'table-container-hide-overflow')}
      >
        <div className={'list-wrapper'}>
          <div className={'configure-station-groups-select-network'}>
            <div className={classNames(Classes.SELECT, Classes.FILL, 'list-filter-select')} >
              <select
                value={this.state.selectedFilter}
                onChange={this.handleFilterChange}
              >
                {createDropdownItems(FilterType)}
              </select>
            </div>
            <div className={'configure-station-groups-select-network-table'}>
              <Table
                context={{}}
                columnDefs={selectNetworkDefs}
                getRowNodeId={node => node.id}
                deltaRowDataMode={true}
                onCellValueChanged={this.onRowClicked}
                onGridReady={this.onNetworkGridReady}
                onSelectionChanged={this.onRowClicked}
                rowData={this.filterTableData(this.state.networkTableData)}
                rowSelection="multiple"
                rowDeselection={true}
                suppressContextMenu={true}
              />
            </div>
          </div>
          <div
            className={'configure-station-groups-no-network-selected'}
            style={{ visibility: (this.state.showStationsTables ? 'hidden' : 'visible') }}
          >
            <NonIdealState
              visual="heat-grid"
              title="Select a single network to edit its stations"
            />
          </div>
          <div
            className={'configure-station-groups-edit-stations'}
            style={{ visibility: (this.state.showStationsTables ? 'visible' : 'hidden') }}
          >
            <div className={'configure-station-groups-associated-stations'}>
              <Table
                context={{}}
                columnDefs={associatedStationsDefs}
                getRowNodeId={node => node.id}
                deltaRowDataMode={true}
                onGridReady={this.onAssocGridReady}
                rowData={selectedNetworkData && selectedNetworkData.associatedStationsTableData ?
                  selectedNetworkData.associatedStationsTableData : []}
                rowSelection="multiple"
                rowDeselection={true}
                suppressContextMenu={true}
              />
            </div>
            <ButtonGroup className={'configure-station-groups-arrows'} vertical={true}>
              < Button
                icon={IconNames.DOUBLE_CHEVRON_RIGHT}
                onClick={e => this.unassociateStations()}
              />
              <div className={'list__button-vertical-divider'} />
              <Button
                icon={IconNames.DOUBLE_CHEVRON_LEFT}
                onClick={e => this.associateStations()}
              />
            </ButtonGroup>
            <div className={'configure-station-groups-available-stations'}>
              <Table
                context={{}}
                columnDefs={availableStationsDefs}
                getRowNodeId={node => node.id}
                deltaRowDataMode={true}
                floatingFilter={true}
                onGridReady={this.onAvailableGridReady}
                rowData={selectedNetworkData && selectedNetworkData.availableStationsTableData ?
                  selectedNetworkData.availableStationsTableData : []}
                rowSelection="multiple"
                rowDeselection={true}
                suppressContextMenu={true}
              />
            </div>
          </div>
        </div>
        <ButtonGroup>
          <Button
            small={true}
            text="Add group"
            onClick={e => this.newNetwork()}
          />
          <div className={'list__button-row-divider-small'} />
          <Button
            small={true}
            text="Deactivate group"
            onClick={e => this.deactivateNetwork()}
          />
          <div className={'list__button-row-divider-large'} />
          <Button
            small={true}
            text="Save"
            onClick={e => this.saveState()}
          />
          <div className={'list__button-row-divider-small'} />
          <Button
            small={true}
            text="Discard"
            onClick={e => this.discardState()}
          />
        </ButtonGroup>
      </div >
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
    this.setState((prevState: ConfigureStationGroupsState) => ({
      selectedFilter: newFilter,
    }));
  }

  /**
   * Uses a switch statement to determine the appropriate filtering option.
   *
   * @param data an array of NetworkRows
   *
   * @returns filtered list based off filter type
   */
  private readonly filterTableData = (data: NetworkRow[]): NetworkRow[] => {
    let filteredList = [];
    switch (this.state.selectedFilter) {
      case 'All stations':
        filteredList = data;
        break;
      case 'Active':
        filteredList = data.filter(row =>
          row.status === 'Active');
        break;
      case 'Inactive':
        filteredList = data.filter(row =>
          row.status === 'Inactive');
        break;
      default:
    }
    return filteredList;
  }

  // tslint:disable-next-line: typedef
  private readonly onNetworkGridReady = params => {
    this.gridApiNetworks = params.api;
  }

  // tslint:disable-next-line: typedef
  private readonly onAssocGridReady = params => {
    this.gridApiAssoc = params.api;
  }

  // tslint:disable-next-line: typedef
  private readonly onAvailableGridReady = params => {
    this.gridApiAvailable = params.api;
  }

  /**
   * Handle table row click
   *
   * @param event Row click event
   */
  private readonly onRowClicked = (event: any) => {
    const nodes = this.gridApiNetworks.getSelectedNodes();
    const selectedStationIds: string[] = nodes.map(node => node.data.id);
    if (selectedStationIds.length === 1) {
      // One network selected, populate its stations
      const mapState = lodash.cloneDeep(this.state.networkToStationsMap);
      const selectedId: string = selectedStationIds[0];
      // Update the map state
      mapState.set(selectedId, this.state.networkToStationsMap.get(selectedId));
      this.setState({
        currentNetworkID:  selectedId,
        networkToStationsMap: mapState,
        showStationsTables: true
      },            () => {
        this.updateAssocHeader();
      });
    } else {
      // Multiple networks selected, hide the stations display
      this.setState({
        showStationsTables: false
      });
    }
  }

  /**
   * Sets the modified field for a network row.
   * Used to determine where to show a dirty dot
   *
   * @param value What to set the modified value to
   */
  private readonly setModified = (id: string, value: boolean) => {
    this.gridApiNetworks.getRowNode(id)
      .setDataValue('modified', value);
  }

 /**
  * Helper method to get StationsSelectionState
  *  If there is a currently selected network, grab its associated stations,
  * If not set to default (empty)
  */
  private readonly getStationsSelectionState = (id: string): StationsSelectionState => {
  const selectedNetworkData: StationsSelectionState =
    this.state.networkToStationsMap.has(id) ?
      this.state.networkToStationsMap.get(id) : {
        associatedStationsOriginalTableData:  [],
        associatedStationsTableData: [],
        availableStationsOriginalTableData: [],
        availableStationsTableData: []
      };
  return selectedNetworkData;
}

 /**
  * Updates the header of the Associated Stations Table with
  * the name of the selected network, and its station count
  */
  private readonly updateAssocHeader = () => {
    // Update the Assoc Table Header
    const curNetId: string = this.state.currentNetworkID ? this.state.currentNetworkID : `default_${this.rowId}`;
    if (this.gridApiNetworks.getDisplayedRowCount() > 0 &&
      this.gridApiNetworks.getRowNode(curNetId)) {
      const newNetworkName: string = this.gridApiNetworks.getRowNode(curNetId).data.network;
      const rowCount: number = this.gridApiAssoc.getDisplayedRowCount();
      const stationHeader: string = rowCount === 1 ? 'station' : 'stations';
      this.gridApiAssoc.getColumnDef('associated_stations').headerName = (newNetworkName +
        `: ${rowCount} ${stationHeader}`);
      this.gridApiAssoc.refreshHeader();
    } else {
      this.gridApiAssoc.getColumnDef('associated_stations').headerName = ('No Network Selected');
      this.gridApiAssoc.refreshHeader();
    }
  }

 /**
  * Handles functionality of the right arrow
  * This unassociates stations
  */
  private readonly unassociateStations = () => {
    const selectedNetworkStationStates: StationsSelectionState =
      this.getStationsSelectionState(this.state.currentNetworkID);
    // Get the slected rows
    const selectedRows = this.gridApiAssoc.getSelectedRows();
    // Append the selected rows to the list of already available stations
    let updatedAvailableTableData: StationsRow[] =
      [...selectedNetworkStationStates.availableStationsTableData, ...selectedRows];
    updatedAvailableTableData = lodash.sortBy(updatedAvailableTableData, 'stations');
    // Remove selected rows from associated stations
    const updatedAssociatedTableData: StationsRow[] = selectedNetworkStationStates
      .associatedStationsTableData
      .filter(row => !lodash.includes(updatedAvailableTableData, row));
    // Create the stations state
    const newStationsState: StationsSelectionState = {
      associatedStationsOriginalTableData:  selectedNetworkStationStates.associatedStationsOriginalTableData,
      associatedStationsTableData: updatedAssociatedTableData,
      availableStationsOriginalTableData: selectedNetworkStationStates.availableStationsOriginalTableData,
      availableStationsTableData: updatedAvailableTableData
    };
    const mapState = lodash.cloneDeep(this.state.networkToStationsMap);
    mapState.set(this.state.currentNetworkID, newStationsState);

    // Now update the state
    this.setState({
      networkToStationsMap:  mapState
    },            () => {
      this.updateAssocHeader();
      this.setModified(this.state.currentNetworkID, true);
    });
  }

 /**
  * Handles functionality of the left arrow
  * This associates stations
  */
  private readonly associateStations = () => {
    const selectedNetworkStationStates: StationsSelectionState =
      this.getStationsSelectionState(this.state.currentNetworkID);
    // Get the slected rows
    const selectedRows = this.gridApiAvailable.getSelectedRows();
    // Append the selected rows to the list of already associated stations
    let updatedAssociatedTableData: StationsRow[] =
      [...selectedNetworkStationStates.associatedStationsTableData, ...selectedRows];
    updatedAssociatedTableData = lodash.sortBy(updatedAssociatedTableData, 'stations');
    const updatedAvailableTableData: StationsRow[] = selectedNetworkStationStates.availableStationsTableData
      .filter(row => !lodash.includes(updatedAssociatedTableData, row));
    // Create the stations state
    const newStationsState: StationsSelectionState = {
      associatedStationsOriginalTableData:  selectedNetworkStationStates.associatedStationsOriginalTableData,
      associatedStationsTableData: updatedAssociatedTableData,
      availableStationsOriginalTableData: selectedNetworkStationStates.availableStationsOriginalTableData,
      availableStationsTableData: updatedAvailableTableData
    };
    const mapState = lodash.cloneDeep(this.state.networkToStationsMap);
    mapState.set(this.state.currentNetworkID, newStationsState);

    // Update the Assoc Table Header
    this.updateAssocHeader();

    // Now update the state
    this.setState({
      networkToStationsMap:  mapState
    },            () => {
      this.updateAssocHeader();
      this.setModified(this.state.currentNetworkID, true);
    });
  }

 /**
  * Handles functionality of the "Add group" button
  * This creates a new network group
  */
  private readonly newNetwork = () => {
    const defaultRow: NetworkRow = {
      id:  `default_${this.rowId++}`,
      newAndUnsaved: true,
      modified: true,
      network: 'New Network',
      status: 'Active',
      prevStatus: 'Active',
      modifiedTime: TimeUtil.dateToISOString(new Date())
    };
    const defaultStations: StationsRow[] = ConfigureStationGroups.generateTableData(this.props);

    // Create the stations state
    const newStationsState: StationsSelectionState = {
      associatedStationsOriginalTableData:  [],
      associatedStationsTableData: [],
      availableStationsOriginalTableData: defaultStations,
      availableStationsTableData: defaultStations
    };

    // Update the Assoc Table Header
    this.gridApiAssoc.getColumnDef('associated_stations').headerName = (defaultRow.network + ': 0 stations');
    this.gridApiAssoc.refreshHeader();

    this.setState({
      networkTableData:  [defaultRow, ...this.state.networkTableData],
      currentNetworkID: defaultRow.id,
      networkToStationsMap: this.state.networkToStationsMap.set(defaultRow.id, newStationsState)
    });
  }

 /**
  * Handles functionality of the Deactivate button
  * This sets the selected groups as deactivated
  */
  private readonly deactivateNetwork = () => {
    // Get the slected rows
    const selectedRows: NetworkRow[] = this.gridApiNetworks.getSelectedRows();

    const newNetworkRows: NetworkRow[] = lodash.cloneDeep(this.state.networkTableData);
    selectedRows.forEach(row => {
      const index: number = newNetworkRows.findIndex(origRow => origRow.id === row.id);

      const newRow: NetworkRow = {
        id: row.id,
        newAndUnsaved: row.newAndUnsaved,
        modified: true,
        network: row.network,
        status: 'Inactive',
        prevStatus: row.status,
        modifiedTime: row.modifiedTime
      };

      newNetworkRows.splice(index, 1, newRow);

    });
    // Now update the state
    this.setState({
      networkTableData: newNetworkRows
    });
  }

 /**
  * Handles functionality of the Save button
  * This saves the current state, any reversions after this will
  * revert to the state defined here
  */
  private readonly saveState = () => {
    const selectedRows: NetworkRow[] = this.gridApiNetworks.getSelectedRows();
    const mapState: Map<string, StationsSelectionState> = lodash.cloneDeep(this.state.networkToStationsMap);
    const networkState: NetworkRow[] = lodash.cloneDeep(this.state.networkTableData);
    selectedRows.forEach(row => {
      const selectedNetworkStationStates: StationsSelectionState =
        this.getStationsSelectionState(row.id);
      // Create the stations state
      const newStationsState: StationsSelectionState = {
        associatedStationsOriginalTableData: selectedNetworkStationStates.associatedStationsTableData,
        associatedStationsTableData: selectedNetworkStationStates.associatedStationsTableData,

        availableStationsOriginalTableData: selectedNetworkStationStates.availableStationsTableData,
        availableStationsTableData: selectedNetworkStationStates.availableStationsTableData
      };
      mapState.set(row.id, newStationsState);

      // Create a new newtork state with saved changes
      const index: number = networkState.findIndex(origRow => origRow.id === row.id);
      const newRow: NetworkRow = {
        id: row.id,
        newAndUnsaved: false,
        modified: false,
        network: row.network,
        status: row.status,
        prevStatus: row.status,
        modifiedTime: TimeUtil.dateToISOString(new Date())
      };
      networkState.splice(index, 1, newRow);
    });

    this.setState({
      networkOriginalTableData:  networkState,
      networkTableData: networkState,
      networkToStationsMap: mapState
    });
  }

  /**
   * Handles functionality of the Discard button
   * This reverts to the last saved state or original state
   * if there was none prior
   */
  private readonly discardState = () => {
    const selectedRows: NetworkRow[] = this.gridApiNetworks.getSelectedRows();
    const mapState: Map<string, StationsSelectionState> = lodash.cloneDeep(this.state.networkToStationsMap);
    const networkState: NetworkRow[] = lodash.cloneDeep(this.state.networkTableData);
    selectedRows.forEach(row => {
      const selectedNetworkStationStates: StationsSelectionState =
        this.getStationsSelectionState(row.id);
      // Create the stations state
      const newStationsState: StationsSelectionState = {
        associatedStationsOriginalTableData: selectedNetworkStationStates.associatedStationsOriginalTableData,
        associatedStationsTableData: selectedNetworkStationStates.associatedStationsOriginalTableData,

        availableStationsOriginalTableData: selectedNetworkStationStates.availableStationsOriginalTableData,
        availableStationsTableData: selectedNetworkStationStates.availableStationsOriginalTableData
      };
      mapState.set(row.id, newStationsState);

      // Set back to previous status if deactivated but changes discarded
      const index: number = networkState.findIndex(origRow => origRow.id === row.id);
      const newRow: NetworkRow = {
        id: row.id,
        newAndUnsaved: row.newAndUnsaved,
        modified: row.newAndUnsaved,
        network: row.network,
        status: row.prevStatus,
        prevStatus: row.prevStatus,
        modifiedTime: row.modifiedTime
      };
      networkState.splice(index, 1, newRow);

      // Delete rows that are new and unsaved from the networks table
      if (networkState[index] ? networkState[index].newAndUnsaved : false) {
        networkState.splice(index, 1);
      }

    });
    this.setState({
      networkTableData: networkState,
      networkToStationsMap: mapState
    },            () => {
      selectedRows.forEach(row => {
        if (this.gridApiNetworks.getRowNode(row.id) != null) {
          this.setModified(row.id, false);
        }
      });
    });
  }
}
