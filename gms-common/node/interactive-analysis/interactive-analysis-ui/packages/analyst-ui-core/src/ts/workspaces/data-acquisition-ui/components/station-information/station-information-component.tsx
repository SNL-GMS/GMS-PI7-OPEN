import {
  Button,
  ButtonGroup,
  Classes,
  FileInput,
  Intent,
  NonIdealState,
  Popover,
  PopoverInteractionKind,
  Position,
  Spinner
} from '@blueprintjs/core';
import { Table, TableApi } from '@gms/ui-core-components';
import * as classNames from 'classnames';
import * as lodash from 'lodash';
import * as React from 'react';
import { ProcessingStation } from '~graphql/station/types';
import { createDropdownItems } from '~util/display-util';
import { addGlForceUpdateOnResize, addGlForceUpdateOnShow } from '~util/gl-util';
import { createProcessingStations, readUploadedFileAsText } from './parsing-utils/parse-css';
import { columnDefs, popoverTableColumnDefs } from './table-utils/column-defs';
import {
  AutomaticProcessingStatus,
  DataAcquisitionStatus,
  FilterType,
  InteractiveProcessingStatus,
  PopoverStationNamesRow,
  StationInformationProps,
  StationInformationRow,
  StationInformationState
} from './types';

/**
 * The StationInformation component displays current stations and provides various ways to edit and import them.
 */
export class StationInformation extends React.PureComponent<StationInformationProps, StationInformationState> {
  private static tableRowId: number = 0;
  private rowId: number = 0;

  /**
   * Generate StationInformationRow[] based of stations returned from query.
   *
   * @param defaultStations default stations
   *
   * @returns StationInformationRow[]
   */

  public static generateTableData = (props: StationInformationProps): StationInformationRow[] => {
    let stationInformationListData = [];
    // get all defaultStations information
    const defaultStations = props.defaultStationsQuery ?
      props.defaultStationsQuery.defaultStations : undefined;
    if (defaultStations) {
      defaultStations.forEach(station => {
        const tempStationInformationRow: StationInformationRow = {
          id: `${StationInformation.tableRowId++}`,
          stationId: station.id,
          station: station.name,
          modified: false,
          dataAcquisition: station.dataAcquisition.dataAcquisition,
          interactiveProcessing: station.dataAcquisition.interactiveProcessing,
          automaticProcessing: station.dataAcquisition.automaticProcessing,
          configure: 'Configure...',
          color: 'pink'
        };
        stationInformationListData.push(tempStationInformationRow);
      });
      stationInformationListData = lodash.sortBy(stationInformationListData, 'station');
    }
    return stationInformationListData;
  }

  public static getDerivedStateFromProps(nextProps: StationInformationProps, prevState: StationInformationState) {
    if (nextProps.defaultStationsQuery && nextProps.defaultStationsQuery.defaultStations) {
      if (prevState.tableData.length === 0) {
        const data = StationInformation.generateTableData(nextProps);
        return {
          tableData: data,
          originalTableData: data
        };
      }
    }
    return null;
  }

  /**
   * To interact directly with the table
   */
  private mainTable: TableApi;
  private popoverTable: TableApi;

  /**
   * Constructor, setting filter to acquired by default.
   */
  public constructor(props: StationInformationProps) {
    super(props);
    this.state = {
      originalTableData: [],
      tableData: [],
      selectedFilter: FilterType.ACQUIRED,
      disableEditSelected: true,
      batchEditPopupIsOpen: false,
      rowId: 0,
      popoverDataAcquisitionStatusValue: DataAcquisitionStatus.ENABLED,
      popoverInteractiveProcessingStatusValue: InteractiveProcessingStatus.BY_DEFAULT,
      popoverAutomaticProcessingStatusValue: AutomaticProcessingStatus.STATION_ONLY,
      importCssPopupIsOpen: false,
      siteFilename: '',
      siteFile: null,
      sitechanFilename: '',
      sitechanFile: null,
      importedStationNames: [],
      stationsFromFiles: [],
      selectedStationFromFiles: undefined
    };
  }

  /**
   * Invoked when the component mounted.
   */
  public componentDidMount() {
    addGlForceUpdateOnShow(this.props.glContainer, this);
    addGlForceUpdateOnResize(this.props.glContainer, this);
  }

  // ***************************************
  // BEGIN REACT COMPONENT LIFECYCLE METHODS
  // ***************************************

  /**
   * Renders the component.
   */
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
          description={'Station information...'}
        />
      );
    }

    return (
      <div
        className={classNames('ag-theme-dark', 'table-container-hide-overflow')}
      >
        <div
          className={classNames(Classes.SELECT, Classes.FILL, 'list-filter-select')}
        >
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
              onGridReady={this.onMainTableReady}
              context={{
                updateDataAcquisition: this.updateDataAcquisition,
                updateInteractiveProcessing: this.updateInteractiveProcessing,
                updateAutomaticProcessing: this.updateAutomaticProcessing,
                updateTableWithDropdownValues: this.updateTableWithDropdownValues
              }}
              columnDefs={columnDefs}
              rowData={this.filterTableData(this.state.tableData)}
              onRowSelected={this.onRowClicked}
              getRowNodeId={node => node.id}
              deltaRowDataMode={true}
              rowSelection="multiple"
              rowDeselection={true}
            />
          </div>
        </div>
        <p />
        <ButtonGroup>
          <Button
            small={true}
            text="Add..."
            onClick={e => this.generateDefaultRow()}
          />
          <Popover
            content={
              <div className={'data-acquisition-import-css-popover-with-table'}>
                <br />
                <label> Select site and sitechan CSS files </label>
                <br /> <br />
                <label> Site </label><br />
                <FileInput
                  inputProps={{
                    onChange: this.siteOnChange,
                  }
                  }
                  text={this.state.siteFilename}
                  onInputChange={event => {
                    const targetValue = event.currentTarget.value;
                    const filepath = targetValue.split('\\');
                    this.setState({

                      siteFilename: filepath[filepath.length - 1]
                    });
                  }}
                />
                <br /><br />
                <label> Sitechan </label> <br />
                <FileInput
                  text={this.state.sitechanFilename}
                  inputProps={{
                    onChange: this.sitechanOnChange,
                    label: 'Sitechan'
                  }
                  }
                  onInputChange={event => {
                    const targetValue = event.currentTarget.value;
                    const filepath = targetValue.split('\\');
                    this.setState({
                      sitechanFilename: filepath[filepath.length - 1]
                    });
                  }}
                /> <br /><br />
                <div
                  style={{
                    display: 'flex',
                    justifyContent: 'flex-end'
                  }}
                >
                  <ButtonGroup>
                    <div className={Classes.DISABLED}>
                      <Button
                        small={true}
                        text="Import files"
                        onClick={async e => this.importCssFiles(e)}
                      />
                    </div>
                    <div className={Classes.POPOVER_DISMISS}>
                      <Button
                        small={true}
                        text="Cancel"
                        onClick={e => this.onImportCancel(e)}
                      />
                    </div>
                  </ButtonGroup><br />
                  <br /> <br />
                </div>
                <label> Select station to import </label>
                <br /><br />
                {this.popoverStationNamesTable()}
              </div>}
            interactionKind={PopoverInteractionKind.CLICK}
            isOpen={this.state.importCssPopupIsOpen}
            onInteraction={state => this.handleImportCssPopupInteraction(state)}
            position={Position.TOP_RIGHT}
          >
            <Button
              small={true}
              text="Import CSS"
            />
          </Popover>
          <Popover
            content={
              <div className={'data-acquisition-popover'}>
                <br />
                <label> Data Acquisition </label>
                <br />
                <div className={classNames(Classes.SELECT, Classes.FILL, 'list-filter-select')}>
                  <select
                    value={this.state.popoverDataAcquisitionStatusValue}
                    onChange={(event: React.FormEvent<HTMLSelectElement>) => {
                      const targetValue = event.currentTarget.value as DataAcquisitionStatus;
                      this.setState({ popoverDataAcquisitionStatusValue: targetValue });
                    }}
                  >
                    {createDropdownItems(DataAcquisitionStatus)}
                  </select>
                </div>
                <label> Interactive Processing  <br /></label>
                <div className={classNames(Classes.SELECT, Classes.FILL, 'list-filter-select')}>
                  <select
                    value={this.state.popoverInteractiveProcessingStatusValue}
                    onChange={(event: React.FormEvent<HTMLSelectElement>) => {
                      const targetValue = event.currentTarget.value as InteractiveProcessingStatus;
                      this.setState({ popoverInteractiveProcessingStatusValue: targetValue });
                    }}
                  >
                    {createDropdownItems(InteractiveProcessingStatus)}
                  </select>
                </div>
                <label> Automatic Processing  <br /></label>
                <div className={classNames(Classes.SELECT, Classes.FILL, 'list-filter-select')}>
                  <select
                    value={this.state.popoverAutomaticProcessingStatusValue}
                    onChange={(event: React.FormEvent<HTMLSelectElement>) => {
                      const targetValue = event.currentTarget.value as AutomaticProcessingStatus;
                      this.setState({ popoverAutomaticProcessingStatusValue: targetValue });
                    }}
                  >
                    {createDropdownItems(AutomaticProcessingStatus)}
                  </select>
                </div>
                <br /> <br /> <br />
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
                  </ButtonGroup> <br />
                </div>
              </div>}
            interactionKind={PopoverInteractionKind.CLICK}
            isOpen={this.state.batchEditPopupIsOpen}
            onInteraction={state => this.handleInteraction(state)}
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
            onClick={e => this.cleanupAllRows()}
          />
          <div className={'list__button-row-divider-small'} />
          <Button
            small={true}
            text="Save"
            onClick={e => this.saveChanges()}
          />
          <div className={'list__button-row-divider-small'} />
          <Button
            small={true}
            text="Discard"
            onClick={e => this.discardChanges()}
          />
        </ButtonGroup >
      </div >
    );
  }

  // ***************************************
  // END REACT COMPONENT LIFECYCLE METHODS
  // ***************************************
  /**
   * Clears the file name and the Redux store for the selected station when CSS import is canceled
   * by clicking the Cancel button
   *
   * @param e the click event
   */
  private onImportCancel(e: any) {
    this.setState(
      {
        siteFilename: '',
        sitechanFilename: '',
        importedStationNames: [],
        stationsFromFiles: [],
      });
    this.props.setSelectedProcessingStation(undefined);
    this.onRowClicked(e);
  }

  /**
   * Sets the open status of the import css popup to nextOpenState
   *
   * @param nextOpenState open status of the import css pop up
   */
  private handleImportCssPopupInteraction(nextOpenState: boolean) {
    this.setState({ importCssPopupIsOpen: nextOpenState });
  }

  /**
   * Sets the open status of the batch edit popup to nextOpenState
   *
   * @param nextOpenState open status of the batch edit pop up
   */

  private handleInteraction(nextOpenState: boolean) {
    if (this.props.selectedStationIds.length > 0) {
      this.setState({ batchEditPopupIsOpen: nextOpenState });
    } else {
      this.setState({ batchEditPopupIsOpen: false });
    }
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
    this.setState((prevState: StationInformationState) => ({
      selectedFilter: newFilter,
    }));
  }

  /**
   * Uses a switch statement to determine the appropriate filtering option.
   *
   * @param data an array of station information table rows
   *
   * @returns filtered list based off filter type
   */
  private readonly filterTableData = (data: StationInformationRow[]): StationInformationRow[] => {
    let filteredList = [];
    switch (this.state.selectedFilter) {
      case 'Enabled':
        filteredList = data.filter(stationInformation =>
          stationInformation.dataAcquisition === 'enabled');
        break;
      case 'Disabled':
        filteredList = data.filter(stationInformation =>
          stationInformation.dataAcquisition === 'disabled');
        break;
      case 'All Stations':
        filteredList = data;
        break;
      case 'Interactive Processing - Available by default':
        filteredList = data.filter(stationInformation =>
          stationInformation.interactiveProcessing === 'default');
        break;
      case 'Interactive Processing - Available by request':
        filteredList = data.filter(stationInformation =>
          stationInformation.interactiveProcessing === 'request');
        break;
      case 'Station Processing stations':
        filteredList = data.filter(stationInformation =>
          stationInformation.automaticProcessing ===
          'station');
        break;
      case 'Network Processing stations':
        filteredList = data.filter(stationInformation =>
          stationInformation.automaticProcessing ===
          'network');
        break;
      case 'Disabled Processing stations':
        filteredList = data.filter(stationInformation =>
          stationInformation.automaticProcessing ===
          'disabled');
        break;
      default:
    }
    return filteredList;
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
   * Set class members when main table is ready
   *
   * @param event event of the table action
   */
  private readonly onPopoverReady = (event: any) => {
    this.popoverTable = event.api;
  }
  /**
   * Handle table row click
   *
   * @param event Row click event
   */
  private readonly onRowClicked = (event: any) => {
    if (this.mainTable) {
      const nodes = this.mainTable.getSelectedNodes();
      const selectedStationIds = nodes.map(node => node.data.stationId);
      this.props.setSelectedStationIds(selectedStationIds);
      const multipleNodesSelected = nodes.length > 1;
      this.setState({ disableEditSelected: !multipleNodesSelected });
      const allStationData: ProcessingStation[] =
        (this.props.defaultStationsQuery ?
          this.props.defaultStationsQuery.defaultStations : undefined);
      const selectedStation: ProcessingStation = allStationData ? allStationData
        .filter(station => station.id === this.props.selectedStationIds[0])[0] : undefined;
      if (selectedStation) {
        this.props.setSelectedProcessingStation({
          ...this.props.selectedProcessingStation,
          description: selectedStation.description,
          latitude: selectedStation.latitude,
          longitude: selectedStation.longitude,
          name: selectedStation.name,
          elevation: selectedStation.elevation,
          sites: selectedStation.sites,
          stationType: selectedStation.stationType
        });
        this.props.setUnmodifiedProcessingStation({
          ...this.props.unmodifiedProcessingStation,
          description: selectedStation.description,
          latitude: selectedStation.latitude,
          longitude: selectedStation.longitude,
          name: selectedStation.name,
          elevation: selectedStation.elevation,
          stationType: selectedStation.stationType,
          sites: selectedStation.sites
        });
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
      popoverDataAcquisitionStatusValue: DataAcquisitionStatus.ENABLED,
      popoverInteractiveProcessingStatusValue: InteractiveProcessingStatus.BY_DEFAULT,
      popoverAutomaticProcessingStatusValue: AutomaticProcessingStatus.STATION_ONLY
    });
  }
  /**
   * Generate a blank row with default values and clear the props.
   */
  private readonly generateDefaultRow = () => {
    const defaultRow: StationInformationRow = {
      id: `default_${this.rowId++}`,
      stationId: '',
      station: '',
      modified: true,
      dataAcquisition: 'disabled',
      interactiveProcessing: 'default',
      automaticProcessing: 'network',
      configure: '',
      color: 'pink'
    };
    this.props.setSelectedProcessingStation(undefined);
    this.setState({ tableData: [defaultRow, ...this.state.tableData] });
  }

  /**
   * Deselects and removes dirty dot from all rows.
   */
  private readonly cleanupAllRows = () => {
    this.mainTable.deselectAll();
    this.props.setSelectedProcessingStation(undefined);
  }

  /**
   * Saves the current table data to the state and cleans up rows.
   */
  private readonly saveChanges = () => {
    this.cleanupAllRows();
    this.setState({ originalTableData: this.state.tableData });
    this.mainTable.forEachNode(node => {
      node.setDataValue('modified', false);
    });
  }

  /**
   * Discard all changes made to the table and revert to previous table state
   */
  private readonly discardChanges = () => {
    this.cleanupAllRows();
    this.setState({ tableData: this.state.originalTableData });
    this.props.setSelectedProcessingStation(undefined);
  }

  /**
   * Creates a external data center table to use in the popover
   *
   * @return an external data center table to use in the popover
   */
  private readonly popoverStationNamesTable = () => (
    <div
      className={classNames('ag-theme-dark', 'table-container-hide-overflow')}
      style={{
        padding: 0,
        marginBottom: '8px'
      }}
    >
      <Table
        context={{}}
        columnDefs={popoverTableColumnDefs}
        rowData={
          this.generatePopoverTableData()
        }
        getRowNodeId={node => node.id}
        deltaRowDataMode={true}
        suppressNoRowsOverlay={true}
        rowSelection={'single'}
        onGridReady={this.onPopoverReady}
        onRowSelected={e => this.getSelectedStation(e)}
      />
    </div>
  )

  // tslint:disable-next-line:typedef
  private readonly getSelectedStation = (e: any) => {
    const nodes = this.popoverTable.getSelectedNodes();
    const selectedStationName: string = nodes.map(node => node.data.stationName)[0];
    const matchingStation: ProcessingStation =
      this.state.stationsFromFiles.find(a => a.id === selectedStationName);
    this.setState({

      selectedStationFromFiles: matchingStation
    });
    this.props.setSelectedProcessingStation(this.state.selectedStationFromFiles);
    this.props.setUnmodifiedProcessingStation(this.state.selectedStationFromFiles);
  }

  private readonly generatePopoverTableData = (): PopoverStationNamesRow[] => {
    const stationNames = [];
    this.state.importedStationNames.forEach(name => {
      const stationNameRow: PopoverStationNamesRow = {
        id: name,
        stationName: name
      };
      stationNames.push(stationNameRow);
    });
    return stationNames;
  }

  /**
   *  Set DataAcqusition state value when Acquisition dropdown is selected
   * @param id id of the row being updated
   * @param newValue new dropdown value
   */
  private readonly updateDataAcquisition = (id: string, newValue: DataAcquisitionStatus) => {
    const indexedValue = this.state.tableData.find(r => r.id === id);
    const td: StationInformationRow[] = this.state.tableData.map(a => ({ ...a }));
    td.find(a => a.id === indexedValue.id).dataAcquisition = newValue;
    td.find(a => a.id === indexedValue.id).modified = true;
    lodash.defer(() => this.setState({ tableData: td }));
  }

  /**
   * Set Interactive Processing state value when Interactive Processing dropdown is selected
   * @param id id of the row being updated
   * @param newValue new dropdown value
   */
  private readonly updateInteractiveProcessing = (id: string, newValue: InteractiveProcessingStatus) => {
    const indexedValue = this.state.tableData.find(r => r.id === id);
    const td: StationInformationRow[] = this.state.tableData.map(a => ({ ...a }));
    td.find(a => a.id === indexedValue.id).interactiveProcessing = newValue;
    td.find(a => a.id === indexedValue.id).modified = true;
    lodash.defer(() => this.setState({ tableData: td }));
  }

  /**
   * Set Automatic Processing state value when Automatic Processing dropdown is selected
   * @param id id of the row being updated
   * @param newValue new dropdown value
   */
  private readonly updateAutomaticProcessing = (id: string, newValue: AutomaticProcessingStatus) => {
    const indexedValue = this.state.tableData.find(r => r.id === id);
    const td: StationInformationRow[] = this.state.tableData.map(a => ({ ...a }));
    td.find(a => a.id === indexedValue.id).automaticProcessing = newValue;
    td.find(a => a.id === indexedValue.id).modified = true;
    lodash.defer(() => this.setState({ tableData: td }));
  }

  private readonly updateTableWithDropdownValues = () => {
    const td = this.state.tableData.map(a => ({ ...a }));
    this.props.selectedStationIds.forEach(stationId => {
      const indexedRow = this.state.tableData.find(r => r.stationId === stationId);
      td.find(a => a.id === indexedRow.id).dataAcquisition = this.state.popoverDataAcquisitionStatusValue;
      td.find(a => a.id === indexedRow.id).interactiveProcessing = this.state.popoverInteractiveProcessingStatusValue;
      td.find(a => a.id === indexedRow.id).automaticProcessing = this.state.popoverAutomaticProcessingStatusValue;
      td.find(a => a.id === indexedRow.id).modified = true;
    });
    lodash.defer(() => this.setState({ tableData: td }));
  }

  private readonly siteOnChange = (event: any) => {
    // handle to the file
    this.setState({
      siteFile: event.target.files[0]
    });
  }

  private readonly sitechanOnChange = (event: any) => {
    // handle to the file
    this.setState({
      sitechanFile: event.target.files[0]
    });
  }

  /**
   * Read in contents of site and sitechan files in the state and create ProcessingStations from
   * that information.
   */
  private readonly importCssFiles = async (event: any) => {
    const siteFileContents = await readUploadedFileAsText(this.state.siteFile) as string[];
    const sitechanFileContents = await readUploadedFileAsText(this.state.sitechanFile) as string[];

    const assembledStations = createProcessingStations(siteFileContents, sitechanFileContents);
    const stationsList = lodash.map(assembledStations, 'name')
      .sort((a, b) => a.localeCompare(b, 'en'));

    lodash.defer(() =>
      this.setState({
        stationsFromFiles: assembledStations,
        importedStationNames: stationsList
      }));
  }
}
