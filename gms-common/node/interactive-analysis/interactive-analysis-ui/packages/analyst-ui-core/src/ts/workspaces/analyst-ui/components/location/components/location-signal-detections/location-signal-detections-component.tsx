import { Table, TableApi, TimeUtil } from '@gms/ui-core-components';
import classNames from 'classnames';
import { defer, isEqual, uniqueId } from 'lodash';
import memoizeOne from 'memoize-one';
import * as React from 'react';
import { MILLIS_SEC } from '~analyst-ui/common/dialogs/signal-detection-details/constants';
import { userPreferences } from '~analyst-ui/config';
import { EventTypes } from '~graphql/';
import { DistanceToSource } from '~graphql/station/types';
import { SignalDetectionSnapshotWithDiffs } from '../../types';
import { stripChannelName } from '../location-sd-row-util';
import { generateLocationSDColumnDef } from './table-utils/column-defs';
import { DefiningChange,
  LocationSDRow, LocationSignalDetectionsProps, SdDefiningStates } from './types';

/**
 * Enables the analyst to select which signal detecton feature measurements are defining
 * for an event location.
 */
export class LocationSignalDetections extends React.Component<LocationSignalDetectionsProps> {

  /**
   * A memoized function for generating the table rows.
   * The memoization function caches the results using 
   * the most recent argument and returns the results. 
   *
   * @param signalDetections the signal detections (with snapshot differences)
   * @param distanceToStations distance related to the current event
   * @param event The current event
   * @param historicalMode the historical mode setting
   *
   * @returns row objects for Location Signal Detection table
   */
  private readonly memoizedGenerateTableRows: (signalDetections: SignalDetectionSnapshotWithDiffs[],
    distanceToStations: DistanceToSource[], event: EventTypes.Event, historicalMode: boolean)  => LocationSDRow[];

  /**
   * To interact directly with the table
   */
  private mainTable: TableApi;

  /**
   * constructor
   */
  public constructor(props: LocationSignalDetectionsProps) {
    super(props);
    this.memoizedGenerateTableRows = (typeof memoizeOne === 'function') ?
      memoizeOne(
        this.generateTableRows,
        /* tell memoize to use a deep comparison for complex objects */
        isEqual) : this.generateTableRows;
  }

  // ***************************************
  // BEGIN REACT COMPONENT LIFECYCLE METHODS
  // ***************************************

  /**
   * React component lifecycle
   *
   * @param prevProps The previous properties available to this react component
   */
  public componentDidUpdate(prevProps: LocationSignalDetectionsProps) {
    if (this.props &&
      !isEqual(this.props, prevProps)) {
      // If the selected event has changed, select it in the table
      if (prevProps.selectedSdIds !== this.props.selectedSdIds) {
        this.selectRowsFromProps(this.props);
      }
    }
  }

  /**
   * Renders the component.
   */
  public render() {

    const mainTableRowData = this.memoizedGenerateTableRows(
      this.props.signalDetectionDiffSnapshots,
      this.props.distanceToStations, this.props.event, this.props.historicalMode);

    const timeAllDefining =
      mainTableRowData.map(row => row.arrivalTimeDefining)
        .reduce((accumulator, currentValue) => accumulator && currentValue, true);
    const timeNoneDefining =
      mainTableRowData.map(row => row.arrivalTimeDefining)
        .reduce(((accumulator, currentValue) => (!currentValue && accumulator)), true);
    const timeDefiningState: SdDefiningStates =
      timeAllDefining ?
        SdDefiningStates.ALL
        : timeNoneDefining ?
          SdDefiningStates.NONE
          : SdDefiningStates.SOME;

    const slownessAllDefining =
      mainTableRowData.map(row => row.slownessDefining)
        .reduce((accumulator, currentValue) => accumulator && currentValue, true);
    const slownessNoneDefining =
      mainTableRowData.map(row => row.slownessDefining)
        .reduce(((accumulator, currentValue) => (!currentValue && accumulator)), true);
    const slownessDefiningState: SdDefiningStates =
      slownessAllDefining ?
        SdDefiningStates.ALL
        : slownessNoneDefining ?
          SdDefiningStates.NONE
          : SdDefiningStates.SOME;

    const azimuthAllDefining =
      mainTableRowData.map(row => row.azimuthDefining)
        .reduce((accumulator, currentValue) => accumulator && currentValue, true);
    const azimuthNoneDefining =
      mainTableRowData.map(row => row.azimuthDefining)
        .reduce(((accumulator, currentValue) => (!currentValue && accumulator)), true);
    const aziumthDefiningState: SdDefiningStates =
      azimuthAllDefining ?
        SdDefiningStates.ALL
        : azimuthNoneDefining ?
          SdDefiningStates.NONE
          : SdDefiningStates.SOME;
    return (
      <div className={classNames('ag-theme-dark', 'table-container')}>
        <div className="list-wrapper">
          <div className={'max'}>
            <Table
              context={{}}
              columnDefs={generateLocationSDColumnDef(
                this.props.setDefining,
                timeDefiningState, aziumthDefiningState, slownessDefiningState,
                this.props.historicalMode)}
              rowData={mainTableRowData}
              getRowNodeId={node => node.id}
              deltaRowDataMode={true}
              rowSelection="multiple"
              onGridReady={this.onMainTableReady}
              rowDeselection={true}
              getRowStyle={this.getDiffRowStyles}
              suppressContextMenu={true}
              onRowClicked={this.onRowClicked}
              onCellContextMenu={this.onCellContextMenu}
              overlayNoRowsTemplate="No SDs Associated to Selected Event"
            />
          </div>
      </div >
      </div>
    );
  }
  // ***************************************
  // END REACT COMPONENT LIFECYCLE METHODS
  // ***************************************

  /**
   * Convert the event data into table rows
   *
   * @param signalDetections the signal detections (with snapshot differences)
   * @param distanceToStations distance related to the current event
   * @param event The current event
   * @param historicalMode the historical mode setting
   *
   * @returns row objects for Location Signal Detection table
   */
  private readonly generateTableRows = (
    signalDetections: SignalDetectionSnapshotWithDiffs[],
    distanceToStations: DistanceToSource[], event: EventTypes.Event,
    historicalMode: boolean): LocationSDRow[] =>
    signalDetections.map(sd => {
      const distance = distanceToStations ?
        distanceToStations.find(d => d.stationId === sd.stationId).distance : undefined;
      // id is a unique string part of row used by node.id to identify unique table rows (using SDH id since unique)
      return {
        id: uniqueId(),
        signalDetectionId: sd.signalDetectionId,
        eventId: event.id,
        station: sd.stationName,
        channel: stripChannelName(sd.channelName),
        phase: sd.phase,
        distance: distance ? distance.toFixed(1) : undefined,
        timeObs: TimeUtil.timesecsToISOString(new Date(sd.time.observed * MILLIS_SEC)),
        timeRes: sd.time.residual ? sd.time.residual.toFixed(3) : undefined,
        timeCorr: sd.time.correction ? sd.time.correction.toFixed(3) : undefined,
        azimuthObs: sd.azimuth.observed ? sd.azimuth.observed.toFixed(3) : undefined,
        azimuthRes: sd.azimuth.residual ? sd.azimuth.residual.toFixed(3) : undefined,
        azimuthCorr: sd.azimuth.correction ? sd.azimuth.correction.toFixed(3) : undefined,
        slownessObs: sd.slowness.observed ? sd.slowness.observed.toFixed(3) : undefined,
        slownessRes: sd.slowness.residual ? sd.slowness.residual.toFixed(3) : undefined,
        slownessCorr: sd.slowness.correction ? sd.slowness.correction.toFixed(3) : undefined,
        updateIsDefining: this.props.updateIsDefining,
        arrivalTimeDefining: sd.rejectedOrUnnassociated ? false : sd.time.defining,
        slownessDefining: sd.rejectedOrUnnassociated ? false : sd.slowness.defining,
        azimuthDefining: sd.rejectedOrUnnassociated ? false : sd.azimuth.defining,
        isAssociatedDiff: sd.diffs.isAssociatedDiff,
        timeDefiningDiff: sd.diffs.arrivalTimeDefining !== DefiningChange.NO_CHANGE,
        azimuthDefiningDiff: sd.diffs.azimuthDefining !== DefiningChange.NO_CHANGE,
        slownessDefiningDiff: sd.diffs.slownessDefining !== DefiningChange.NO_CHANGE,
        arrivalTimeDiff: sd.diffs.arrivalTimeDiff,
        azimuthObsDiff: sd.diffs.azimuthObsDiff,
        slownessObsDiff: sd.diffs.slownessObsDiff,
        phaseDiff: sd.diffs.phaseDiff,
        channelNameDiff: sd.diffs.channelNameDiff,
        historicalMode,
        rejectedOrUnnassociated: sd.rejectedOrUnnassociated
        };
      })

  /**
   * Set class members when main table is ready
   * 
   * @param event table event
   */
  private readonly onMainTableReady = (event: any) => {
    this.mainTable = event.api;
  }
  /**
   * Select rows in the table based on the selected SD IDs in the properties.
   * 
   * @param props singal detection props
   */
  private readonly selectRowsFromProps = (props: LocationSignalDetectionsProps) => {
    if (this.mainTable) {
      this.mainTable.deselectAll();
      this.mainTable.forEachNode(node => {
        props.selectedSdIds.forEach(sdId => {
          if (node.data.signalDetectionId === sdId) {
            node.setSelected(true);
            // Must pass in null here as ag-grid expects it
            this.mainTable.ensureNodeVisible(node, null);
          }
        });
      });
    }
  }

  /**
   * Creates a context memu and displays it
   * 
   * @param params table parameters can be found in ag grid docs
   */
  private readonly onCellContextMenu = (params: any) => {
    params.event.preventDefault();
    params.event.stopPropagation();
    if (this.props.historicalMode) {
      this.props.toast('Select current location solution set to modify signal detectios');
      return;
    }
    const selectedIdsInTable = this.mainTable.getSelectedNodes()
      .map(node => node.data.signalDetectionId);
    const selectedSdIds = selectedIdsInTable.indexOf(params.node.data.signalDetectionId) < 0 ?
      [...selectedIdsInTable, params.node.data.signalDetectionId]
      : selectedIdsInTable;
    this.props.setSelectedSdIds(selectedSdIds);
    this.props.showSDContextMenu(selectedSdIds, params.event.clientX, params.event.clientY);
  }

  /**
   * Shows sd details menu.
   * 
   * @param rowParams in format which can be found in ag grid docs
   */
  private readonly onRowClicked = (rowParams: any) => {
    if (this.mainTable) {
      if (rowParams.event.altKey) {
        if (this.props.historicalMode) {
          this.props.toast('Select current location solution set to view signal detection details');
          return;
        } else {
          this.props.showSDDetails(rowParams.data.signalDetectionId, rowParams.event.clientX, rowParams.event.clientY);
        }
      } else {
        if (!this.props.historicalMode) {
          defer(() => {
            const selectedSdIds = this.mainTable.getSelectedNodes()
              .map(node => node.data.signalDetectionId);
            this.props.setSelectedSdIds(selectedSdIds);
          });
        }
      }
    }
  }

  private readonly getDiffRowStyles = (params: any) => {
    if (params.data.isAssociatedDiff) {
      if (params.data.rejectedOrUnnassociated) {
        return {
          color: `${userPreferences.location.historicalModeTableColor} !important`
        };
      } else {
        return {
          'background-color': userPreferences.location.changedSdHighlight
        };
      }
    }
    return {};
  }
}
