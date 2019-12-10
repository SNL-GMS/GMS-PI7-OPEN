import { Table, TableApi, TimeUtil } from '@gms/ui-core-components';
import classNames from 'classnames';
import { cloneDeep, flatMap, isEqual, uniqueId } from 'lodash';
import memoizeOne from 'memoize-one';
import * as React from 'react';
import { EventUtils } from '~analyst-ui/common/utils';
import { getLatestLSS, getPreferredDefaultLocationId,
  getPreferredLocationIdFromEventHyp } from '~analyst-ui/common/utils/event-util';
import { userPreferences } from '~analyst-ui/config/user-preferences';
import { EventTypes } from '~graphql/';
import { DepthRestraintType } from '~graphql/event/types';
import { autoGroupColumnDef, columnDefs, PREFERRED_FIELD_NAME, SAVE_FIELD_NAME } from './table-utils/column-defs';
import { LocationHistoryProps, LocationHistoryRow,
  LocationHistoryState } from './types';

/**
 * Displays a history of computed locations for the event
 */
export class LocationHistory extends React.Component<LocationHistoryProps, LocationHistoryState> {

  /**
   * A memoized function for generating the table rows.
   * 
   * @param event the event
   * @param preferredLocationSolutionSetId the preferred location solution set id
   * @param preferredLocationSolutionId the preferred location solution id
   * @param selectedLocationSolutionSetId the selected location solution set id
   * @param selectedLocationSolutionId the selected location solution id
   * 
   * @returns an array of location history rows
   */
  private readonly memoizedGenerateTableRows: (
    event: EventTypes.Event,
    preferredLSSid: string, preferredLSid: string,
    selectedLocationSolutionSetId: string,
    selectedLocationSolutionId: string) => LocationHistoryRow[];

    /** The ag-grid table reference */
  private mainTable: TableApi;

  /**
   * constructor
   */
  public constructor(props: LocationHistoryProps) {
    super(props);
    this.memoizedGenerateTableRows = (typeof memoizeOne === 'function') ?
        memoizeOne(
          this.generateTableRows,
          /* tell memoize to use a deep comparison for complex objects */
          isEqual) : this.generateTableRows;
    this.state = {
      preferredLSSetId: getLatestLSS(props.event).id,
      preferredLSid: getPreferredLocationIdFromEventHyp(props.event.currentEventHypothesis.eventHypothesis)
    };
  }

  // ***************************************
  // BEGIN REACT COMPONENT LIFECYCLE METHODS
  // ***************************************

  /**
   * Renders the component.
   */
  public render() {
    const mainTableRowData =
    this.memoizedGenerateTableRows(
      this.props.event, this.state.preferredLSSetId, this.state.preferredLSid,
      this.props.selectedLocationSolutionSetId, this.props.selectedLocationSolutionId);

    return (
      <div
        className={classNames('ag-theme-dark', 'table-container')}
      >
        <div className="list-wrapper">
          <div className={'max'}>
            <Table
              context={{}}
              columnDefs={columnDefs}
              rowData={mainTableRowData}
              getRowNodeId={node => node.id}
              rowSelection="none"
              suppressContextMenu={true}
              getNodeChildDetails={this.getNodeChildDetails}
              onGridReady={this.onMainTableReady}
              onRowClicked={this.onRowClicked}
              onCellClicked={this.onCellClicked}
              getRowStyle={
                params =>
                  ({
                  'border-bottom':
                    params.data.isLastInLSSet ? '1px dotted white' : null,
                  'background-color':
                  this.props.selectedLocationSolutionSetId === params.data.locationSetId ?
                    this.props.selectedLocationSolutionId === params.data.locationSolutionId ?
                      userPreferences.colors.system.subsetSelected
                      : userPreferences.colors.system.selectionColor
                    : (params.data.count % 2 === 0) ?
                      userPreferences.colors.system.evenRow
                      : 'transparent !important',
                  cursor: 'pointer'
                })
              }
              autoGroupColumnDef={autoGroupColumnDef}
              groupMultiAutoColumn={true}
            />
          </div>
      </div>
      </div>
    );
  }

  public componentDidUpdate(prevProps: LocationHistoryProps) {
    if (this.mainTable && this.mainTable.getSortModel().length === 0) {
      this.mainTable.setSortModel([{ colId: 'locationSetId', sort: 'desc' }]);
    }
    if (prevProps.event.id !== this.props.event.id) {
      this.setState({
        preferredLSSetId: getLatestLSS(this.props.event).id,
        preferredLSid: getPreferredLocationIdFromEventHyp(this.props.event.currentEventHypothesis.eventHypothesis)});
    } else if (prevProps.event.currentEventHypothesis.eventHypothesis.locationSolutionSets.length !==
        this.props.event.currentEventHypothesis.eventHypothesis.locationSolutionSets.length
    ) {
      const latestLocationSet = getLatestLSS(this.props.event);
      this.setState({
        ...this.state,
        preferredLSSetId: latestLocationSet.id,
        preferredLSid: getPreferredDefaultLocationId(latestLocationSet)
      });
    }
  }

  // ***************************************
  // END REACT COMPONENT LIFECYCLE METHODS
  // ***************************************

  /**
   * Returns the perferred location solution id
   * 
   * @param eventHypothesisId an event hypothesis id
   * @returns the preferred location solution id
   */
  public readonly getPreferredLocationSolutionId = (eventHypothesisId: string): string =>
    this.state.preferredLSid

  /**
   * Generates the table rows.
   * 
   * @param event the event
   * @param preferredLocationSolutionSetId the preferred location solution set id
   * @param preferredLocationSolutionId the preferred location solution id
   * @param selectedLocationSolutionSetId the selected location solution set id
   * @param selectedLocationSolutionId the selected location solution id
   * 
   * @returns an array of location history rows
   */
  private readonly generateTableRows = (
    event: EventTypes.Event,
    preferredLocationSolutionSetId: string, preferredLocationSolutionId: string,
    selectedLocationSolutionSetId: string, selectedLocationSolutionId: string): LocationHistoryRow[] => {
    // Walk through the location solution sets adding them to the history table. There
    // should be one entry in the history table per set.

    if (!event || !event.currentEventHypothesis || !event.currentEventHypothesis.eventHypothesis ||
      !event.currentEventHypothesis.eventHypothesis.locationSolutionSets) {
        return [];
    }

    // Reverse is ok on recieved props
    const locationSolutionSets =
      event.currentEventHypothesis.eventHypothesis.locationSolutionSets;
    locationSolutionSets.sort((lssA, lssB) => lssB.count - lssA.count);
    const rowData: LocationHistoryRow[] = flatMap(locationSolutionSets, ((lsSet, index) =>
        this.generateChildRows(
          lsSet, event.currentEventHypothesis.eventHypothesis.id,
          preferredLocationSolutionId, selectedLocationSolutionSetId)
    ));

    return rowData;
  }

  /**
   * Generates the the child location history rows.
   * 
   * @param locationSolutionSet the location solution set
   * @param eventHypothesisId the event hypotheis id
   * @param preferredLocationId the preferred location solution id
   * @param selectedLocationSolutionSetId the selected location solution set id
   * 
   * @returns an location history rows
   */
  private readonly generateChildRows = (
    locationSolutionSet: EventTypes.LocationSolutionSet, eventHypothesisId: string,
    preferredLocationId: string, selectedLocationSolutionSetId: string): LocationHistoryRow[] =>  {
    const childRows: LocationHistoryRow[] = flatMap(Object.keys(DepthRestraintType), key => {
      const maybeLS: EventTypes.LocationSolution | undefined = locationSolutionSet.locationSolutions.find(ls =>
        ls.locationRestraint.depthRestraintType === key
      );
      if (maybeLS) {
        return this.generateChildRow(locationSolutionSet, maybeLS, preferredLocationId, selectedLocationSolutionSetId);
      }
    })
    .filter(maybeChildRow => maybeChildRow !== undefined);
    const areAnyPreferred = childRows
      .reduce((accumulator, currentValue) => accumulator || currentValue.preferred, false);
    const childRowsPrime =
      cloneDeep(childRows)
      .map((row, index) => ({
        ...row,
        isLastInLSSet: index === childRows.length - 1,
        isFirstInLSSet: index === 0,
        isLocationSolutionSetPreferred: areAnyPreferred
      }));
    return childRowsPrime;
  }

  /**
   * Generates a single location history row.
   * 
   * @param locationSolutionSet the location solution set
   * @param locationSolution the location solution
   * @param preferredLocationId the preferred location solution id
   * @param selectedLocationSolutionSetId the selected location solution set id
   * 
   * @returns an location history row
   */
  private readonly generateChildRow = (
    locationSolutionSet: EventTypes.LocationSolutionSet,
    locationSolution: EventTypes.LocationSolution,
    preferredLocationId: string,
    selectedLocationSolutionSetId: string): LocationHistoryRow => {
      const location = locationSolution.location;
      const ellipse = locationSolution.locationUncertainty && locationSolution.locationUncertainty.ellipses.length > 0 ?
      locationSolution.locationUncertainty.ellipses[0] : undefined;
      const majorAxis = ellipse ? parseFloat(ellipse.majorAxisLength)
        .toFixed(3) : '-';
      const minorAxis = ellipse ? parseFloat(ellipse.minorAxisLength)
        .toFixed(3) : '-';
      const capitalizedType =
        locationSolution.locationType.charAt(0)
        .toUpperCase() + locationSolution.locationType.slice(1);
      const locationData: LocationHistoryRow = {
        id:  uniqueId(),
        locationSolutionId: locationSolution.id,
        locationSetId: locationSolutionSet.id,
        locType: capitalizedType,
        lat: location.latitudeDegrees.toFixed(3),
        lon: location.longitudeDegrees.toFixed(3),
        depth: location.depthKm.toFixed(3),
        time: TimeUtil.dateToISOString(new Date(locationSolution.location.time * 1000)),
        restraint: LocationHistory.humanReadableDepthRestraint(locationSolution.locationRestraint.depthRestraintType),
        smajax: majorAxis,
        sminax: minorAxis,
        strike: ellipse ? ellipse.majorAxisTrend.toFixed(3) : '-',
        stdev: locationSolution.locationUncertainty ?
          locationSolution.locationUncertainty.stDevOneObservation.toFixed(3) : '-',
        preferred: preferredLocationId === locationSolution.id,
        count: locationSolutionSet.count,
        setPreferred: this.setPreferred,
        setToSave: this.setToSave,
        depthRestraintType: locationSolution.locationRestraint.depthRestraintType,
        selectedLocationSolutionSetId
      };
      return locationData;
  }

  /**
   * Returns the details for a ag-grid node child
   * 
   * @param rowItem the ag-grid row item
   */
  private readonly getNodeChildDetails = (rowItem: LocationHistoryRow):
    { group: boolean;
      children: LocationHistoryRow[];
      key: string; } => {
    if (rowItem.locationGroup) {
      return {
        group: true,
        children: rowItem.locationGroup,
        key: rowItem.restraint
      };
    } else {
      return null;
    }
  }

  /**
   * Sets the preferred location solution id.
   * 
   * @param locationSolutionId  the location solution id
   * @param locationSolutionSetId the location solution set id
   */
  private readonly setPreferred = (locationSolutionId: string, locationSolutionSetId: string) => {
    this.setState({...this.state, preferredLSid: locationSolutionId, preferredLSSetId: locationSolutionSetId});
  }

  /**
   * Sets the location solution to save
   * 
   * @param locationSolutionId  the location solution id
   * @param locationSolutionSetId the location solution set id
   */
  private readonly setToSave = (locationSolutionId: string, locationSolutionSetId: string) => {
    const locationSolutionSet =
      this.props.event.currentEventHypothesis.eventHypothesis.locationSolutionSets.
      find(lss =>
        lss.id === locationSolutionSetId
      );
    const newPreferredId = EventUtils.getPreferredDefaultLocationId(locationSolutionSet);
    this.setState({...this.state, preferredLSSetId: locationSolutionSetId, preferredLSid: newPreferredId});
  }

  /**
   * Ag-grid onRowClicked event handler
   * 
   * @params params the event params passed by ag-grid
   */
  private readonly onRowClicked = (params: any) => {
    this.props.setSelectedLSSAndLS(params.data.locationSetId, params.data.locationSolutionId);
  }

  /**
   * Ag-grid onCellClicked event handler
   * 
   * @params params the event params passed by ag-grid
   */
  private readonly onCellClicked = (params: any) => {
    if (params && params.column) {
      // handle cell click events for the columns with custom cell renderers
      // that contain a blueprintjs switch component
      // invoke the switch onChange event handler when these cells are clicked
      if (params.colDef.field === PREFERRED_FIELD_NAME) {
        const locationSolutionId = params.data.locationSolutionId;
        const locationSetId = params.data.locationSetId;
        this.setPreferred(locationSolutionId, locationSetId);
      } else if (params.colDef.field === SAVE_FIELD_NAME) {
        const isFirstInLSSet = params.data.isFirstInLSSet;
        if (isFirstInLSSet) {
          const locationSolutionId = params.data.locationSolutionId;
          const locationSetId = params.data.locationSetId;
          this.setToSave(locationSolutionId, locationSetId);
        }
      }
    }
  }

  /**
   * Event handler for ag-gird that is fired when the table is ready
   * 
   * @param event event of the table action
   */
  private readonly onMainTableReady = (event: any) => {
    this.mainTable = event.api;
    this.mainTable.setSortModel([{ colId: 'count', sort: 'desc' }]);
  }

  /**
   * Translate DepthRestraintType to human readable form if not found return the
   * untranslated DepthRestraint
   * @param restraint DepthRestraintType
   * @returns The human readable string
   */
  private static readonly humanReadableDepthRestraint = (restraint: DepthRestraintType): string | DepthRestraintType =>
    restraint === DepthRestraintType.FIXED_AT_DEPTH ? 'Fixed at Depth' :
    restraint === DepthRestraintType.FIXED_AT_SURFACE ? 'Fixed at Surface' :
    restraint === DepthRestraintType.UNRESTRAINED ? 'Unrestrained' : restraint
}
