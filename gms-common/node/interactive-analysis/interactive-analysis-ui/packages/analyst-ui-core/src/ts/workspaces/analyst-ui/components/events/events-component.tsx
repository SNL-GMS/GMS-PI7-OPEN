import { Intent, NonIdealState, Spinner } from '@blueprintjs/core';
import { Table, TableApi, Toolbar, ToolbarTypes } from '@gms/ui-core-components';
import * as classNames from 'classnames';
import { cloneDeep, defer, isEqual } from 'lodash';
import memoizeOne from 'memoize-one';
import * as React from 'react';
import { openEvent } from '~analyst-ui/common/actions/event-actions';
import { handleCreatedEvents } from '~analyst-ui/common/subscription-handlers/events-created-handler';
import { analystUiConfig, userPreferences } from '~analyst-ui/config';
import { EventSubscriptions,
  EventTypes,
  SignalDetectionSubscriptions,
  SignalDetectionTypes } from '~graphql/';
import { AnalystActivity, TimeInterval } from '~state/analyst-workspace/types';
import { addGlForceUpdateOnResize, addGlForceUpdateOnShow } from '~util/gl-util';
import { columnDefs } from './table-utils/column-defs';
import { EventFilters, eventFilterToColorMap,
  EventsProps, EventsRow, EventsState, SignalDetectionHypothesisWithStation } from './types';

/**
 * Displays event information in tabular form
 */
export class Events extends React.Component<EventsProps, EventsState> {

  /**
   * To interact directly with the table
   */
  private mainTable: TableApi;

  /**
   * A memoized function for generating the table rows.
   * The memoization function caches the results using 
   * the most recent argument and returns the results. 
   * 
   * @param currentTimeInterval the current time interval
   * @param openEventId the current open event id
   * @param eventsInTimeRange the events for the current time range
   * @param signalDetectionsByStation the signal detections by stations
   * @param showEventOfType map indicating event types to display
   *
   * @returns an array of event row objects
   */
  private readonly memoizedGenerateTableRows: (
    currentTimeInterval: TimeInterval,
    openEventId: string,
    eventsInTimeRange: EventTypes.Event[],
    signalDetectionsByStation: SignalDetectionTypes.SignalDetection[],
    showEventOfType: Map<EventFilters, boolean>) => EventsRow[];

  /**
   * Handlers to unsubscribe from apollo subscriptions
   */
  private readonly unsubscribeHandlers: { (): void }[] = [];

  /**
   * Convert the event data into table rows
   *
   * @param currentTimeInterval the current time interval
   * @param openEventId the current open event id
   * @param eventsInTimeRange the events for the current time range
   * @param signalDetectionsByStation the signal detections by stations
   * @param showEventOfType map indicating event types to display
   *
   * @returns an array of event row objects
   */
  private static readonly generateTableRows = (
    currentTimeInterval: TimeInterval,
    openEventId: string,
    eventsInTimeRange: EventTypes.Event[],
    signalDetectionsByStation: SignalDetectionTypes.SignalDetection[],
    showEventOfType: Map<EventFilters, boolean>): EventsRow[] => {
    const events = eventsInTimeRange.filter(event => Events.filterEvent(currentTimeInterval, event, showEventOfType));
    return events.map(event => {
      const eventHyp = event.currentEventHypothesis.eventHypothesis;
      const conflictingSdHyps: SignalDetectionHypothesisWithStation[] =
        signalDetectionsByStation ?
          event.conflictingSdHypIds.map(sdHypId => {
            const signalD = signalDetectionsByStation.find(sd =>
              sd.currentHypothesis.id === sdHypId
            );
            if (signalD) {
              return {
                ...signalD.currentHypothesis,
                stationName: signalD.station.name
              };
            }
          })
          : [];
      return {
        id: event.id,
        eventHypId: eventHyp.id,
        isOpen: event.id === openEventId,
        stageId: event.currentEventHypothesis.processingStage ?
          event.currentEventHypothesis.processingStage.id : undefined,
        lat: eventHyp.preferredLocationSolution.locationSolution.location.latitudeDegrees,
        lon: eventHyp.preferredLocationSolution.locationSolution.location.longitudeDegrees,
        depth: eventHyp.preferredLocationSolution.locationSolution.location.depthKm,
        time: eventHyp.preferredLocationSolution.locationSolution.location.time,
        modified: event.modified,
        conflictingSdHyps,
        activeAnalysts: event.activeAnalysts ? event.activeAnalysts.map(analyst => analyst.userName)
          : [],
        numDetections: eventHyp.signalDetectionAssociations.filter(assoc => !assoc.rejected).length,
        status: event.status,
        edgeEvent: Events.isEdgeEvent(currentTimeInterval, event)
      };
    });
  }

  /**
   * Determines if the event should be included based on current filter settings.
   *
   * @param currentTimeInterval the current time interval
   * @param event the event to check if it should be filtered
   * @param showEventOfType map indicating event types to display
   *
   * @returns true if the event should be displayed; false otherwise
   */
  private static readonly filterEvent = (
    currentTimeInterval: TimeInterval,
    event: EventTypes.Event, showEventOfType: Map<EventFilters, boolean>): boolean => {
    const isComplete = event && event.status === 'Complete';
    const isEdge = event && Events.isEdgeEvent(currentTimeInterval, event);

    let showInList = true;
    if (!showEventOfType.get(EventFilters.EDGE)) {
      showInList = !isEdge;
    }
    if (showInList && !showEventOfType.get(EventFilters.COMPLETED)) {
      showInList = !isComplete;
    }
    return showInList;
  }

  /**
   * Determines if the given event is an edge event.
   *
   * @param currentTimeInterval the current time interval
   * @param event the event to check if it should be filtered
   *
   * @returns true if the event is an edge event; false otherwise
   */
  private static isEdgeEvent(currentTimeInterval: TimeInterval, event: EventTypes.Event): boolean {
    const time = event.currentEventHypothesis.eventHypothesis.preferredLocationSolution.locationSolution.location.time;
    return time < currentTimeInterval.startTimeSecs || time > currentTimeInterval.endTimeSecs;
  }

  // ***************************************
  // BEGIN REACT COMPONENT LIFECYCLE METHODS
  // ***************************************

  /**
   * Constructor.
   *
   * @param props The initial props
   */
  public constructor(props: EventsProps) {
    super(props);
    this.memoizedGenerateTableRows = memoizeOne(
      Events.generateTableRows,
      /* tell memoize to use a deep comparison for complex objects */
      isEqual);
    const showEventOfType = new Map<EventFilters, boolean>();
    showEventOfType.set(EventFilters.COMPLETED, true);
    showEventOfType.set(EventFilters.EDGE, true);
    this.state = {
      currentTimeInterval: props.currentTimeInterval,
      suppressScrollOnNewData: false,
      showEventOfType
    };
  }

  /**
   * Updates the derived state from the next props.
   *
   * @param nextProps The next (new) props
   * @param prevState The previous state
   */
  public static getDerivedStateFromProps(nextProps: EventsProps, prevState: EventsState) {
    return {
      currentTimeInterval: nextProps.currentTimeInterval,
      // Always scroll to the top when the current interval changes, otherwise do not auto scroll
      suppressScrollOnNewData: (isEqual(nextProps.currentTimeInterval, prevState.currentTimeInterval))
    };
  }

  /**
   * Invoked when the component mounted.
   */
  public componentDidMount() {
    addGlForceUpdateOnShow(this.props.glContainer, this);
    addGlForceUpdateOnResize(this.props.glContainer, this);
  }

  /**
   * React component lifecycle
   */
  // tslint:disable-next-line:cyclomatic-complexity
  public componentDidUpdate(prevProps: EventsProps) {
    if (this.props.currentTimeInterval &&
      !isEqual(this.props.currentTimeInterval, prevProps.currentTimeInterval)) {
      this.setupSubscriptions(this.props);
    }

    if (this.mainTable && this.mainTable.getSortModel().length === 0) {
      this.mainTable.setSortModel([{ colId: 'time', sort: 'asc' }]);
    }

    const prevEventsInTimeRange = prevProps.eventsInTimeRangeQuery ?
      prevProps.eventsInTimeRangeQuery.eventsInTimeRange : [];

    const eventsInTimeRange = this.props.eventsInTimeRangeQuery ?
      this.props.eventsInTimeRangeQuery.eventsInTimeRange : [];

    // If the selected event has changed, select it in the table
    if ((this.props.openEventId && prevProps.openEventId !== this.props.openEventId) ||
      !isEqual(prevEventsInTimeRange, eventsInTimeRange)) {
      defer(() => {
        this.selectRowsFromProps(this.props);
        // Auto scroll to ensure the selected event is displayed when the data changes
        if (this.mainTable && this.mainTable.getSelectedRows().length !== 0) {
          this.mainTable.ensureNodeVisible(this.mainTable.getSelectedRows()[0], 'middle');
        }
      });
    }
  }

  /**
   * Invoked when the componented will unmount.
   */
  public componentWillUnmount() {
    // Unsubscribe from all current subscriptions
    this.unsubscribeHandlers.forEach(unsubscribe => unsubscribe());
    this.unsubscribeHandlers.length = 0;
  }

  /**
   * Renders the component.
   */
  public render() {
    if (this.props.eventsInTimeRangeQuery && this.props.eventsInTimeRangeQuery.loading) {
      return (
        <NonIdealState
          action={<Spinner intent={Intent.PRIMARY} />}
          title="Loading:"
          description={'Events...'}
        />
      );
    }

    const mainTableRowData = this.memoizedGenerateTableRows(
          this.props.currentTimeInterval,
          this.props.openEventId,
          this.props.eventsInTimeRangeQuery ? this.props.eventsInTimeRangeQuery.eventsInTimeRange : [],
          this.props.signalDetectionsByStationQuery ?
            this.props.signalDetectionsByStationQuery.signalDetectionsByStation : [],
          this.state.showEventOfType);
    const numEventsInTable = this.getNumEventsInInterval();
    const numCompleteEvents = this.getNumCompleteEventsInInterval();
    const toolbarLeftItems: ToolbarTypes.ToolbarItem[] = [{
      rank: 1,
      tooltip: 'Number of completed events',
      label: 'Completed',
      type: ToolbarTypes.ToolbarItemType.LabelValue,
      value: numCompleteEvents
    },
    {
      rank: 2,
      tooltip: 'Number of events to work',
      label: 'Remaining',
      type: ToolbarTypes.ToolbarItemType.LabelValue,
      value: numEventsInTable - numCompleteEvents
    }
    ];
    const toolbarItems: ToolbarTypes.ToolbarItem[] = [{
      rank: 1,
      tooltip: 'Mark selected events complete',
      label: 'Mark Complete',
      widthPx: 119,
      type: ToolbarTypes.ToolbarItemType.Button,
      onChange: () => { this.handleMarkSelectedComplete(); },
      disabled: this.shouldDisableMarkSelectedComplete()
    },
    {
      rank: 2,
      tooltip: 'Select or deselect all events',
      label: '',
      type: ToolbarTypes.ToolbarItemType.ButtonGroup,
      buttons: [ {
        label: 'Deselect All',
        tooltip: 'Deselects all items in table',
        type: ToolbarTypes.ToolbarItemType.Button,
        rank: 3,
        widthPx: 98,
        onChange: e => {this.setSelectionOnAll(false); }
      },
      {
        label: 'Select All',
        tooltip: 'Selects all items in table',
        type: ToolbarTypes.ToolbarItemType.Button,
        rank: 4,
        widthPx: 81,
        onChange: e => {this.setSelectionOnAll(true); }
      }]
    },
    {
      rank: 5,
      tooltip: 'Select which types of events to show',
      label: 'Show Events',
      type: ToolbarTypes.ToolbarItemType.CheckboxDropdown,
      dropdownOptions: EventFilters,
      valueToColorMap: eventFilterToColorMap,
      value: this.state.showEventOfType,
      widthPx: 126,
      onChange: value => {this.onFilterChecked(value); }
    }
  ];

    return (
      <div
        className={classNames('ag-theme-dark', 'table-container')}
      >
        <div
          className={'events-status-bar'}
        >
        <div className={'list-toolbar-wrapper'}>
          <Toolbar
              items={toolbarItems}
              itemsLeft={toolbarLeftItems}
              toolbarWidthPx={this.props.glContainer.width - userPreferences.list.widthOfTableMarginsPx}
              minWhiteSpacePx={userPreferences.list.minWidthPx}
          />
        </div>
        </div>
        <div className={'list-wrapper'}>
          <div className={'max'}>
            <Table
              context={{
                markEventComplete: this.markEventsComplete
              }}
              onGridReady={this.onMainTableReady}
              columnDefs={columnDefs}
              rowData={mainTableRowData}
              onRowDoubleClicked={params =>
                openEvent(
                    this.props.eventsInTimeRangeQuery.eventsInTimeRange,
                    params.data.id,
                    this.props.analystActivity,
                    this.props.updateEvents,
                    this.props.setOpenEventId
                )
              }
              onRowClicked={this.onRowClicked}
              getRowNodeId={node => node.id}
              getRowStyle={
                params =>
                  ({
                  'background-color':
                    params.data.isOpen ? analystUiConfig.userPreferences.colors.events.inProgress :
                    params.data.edgeEvent ? analystUiConfig.userPreferences.colors.events.edge : '',
                  filter: params.data.edgeEvent ? 'brightness(0.5)' : ''
                })
              }
              rowSelection="multiple"
              overlayNoRowsTemplate="No Events Loaded"
              rowDeselection={true}
              suppressScrollOnNewData={this.state.suppressScrollOnNewData}
            />
          </div>
        </div >
      </div >
    );
  }

  // *************************************
  // END REACT COMPONENT LIFECYCLE METHODS
  // *************************************

  /**
   * Selects the next open event in the list
   */
  private readonly openNextEventInList = () => {
    let firstEventNode = null;
    if (this.mainTable.getDisplayedRowCount() <= 1) {
      return;
    }
    this.mainTable.forEachNodeAfterFilterAndSort(node => {
      if (!firstEventNode &&
        node.data.edgeEvent === false &&
        (
          this.props.openEventId === node.data.id && node.data.status !== 'Complete' ||
          this.props.openEventId !== node.data.id && node.data.status !== 'Complete'
        )) {
            openEvent(this.props.eventsInTimeRangeQuery.eventsInTimeRange,
                      node.data.id,
                      this.props.analystActivity,
                      this.props.updateEvents,
                      this.props.setOpenEventId);
            firstEventNode = node;
      }
    });
  }

  /**
   * Initialize graphql subscriptions on the apollo client
   *
   * @param EventsProps props of the event
   */
  private readonly setupSubscriptions = (props: EventsProps): void => {
    if (!props.eventsInTimeRangeQuery) return;

    // First, unsubscribe from all current subscriptions
    this.unsubscribeHandlers.forEach(unsubscribe => unsubscribe());
    this.unsubscribeHandlers.length = 0;

    // Don't register subscriptions if the current time interval is undefined/null
    if (!props.currentTimeInterval) return;
    this.unsubscribeHandlers.push(
      this.props.eventsInTimeRangeQuery.subscribeToMore({
        document: EventSubscriptions.eventsCreatedSubscription,
        updateQuery: (prev: { eventsInTimeRange: EventTypes.Event[] }, cur) =>
          handleCreatedEvents(prev, cur, this.props.currentTimeInterval)
      })
    );
    this.unsubscribeHandlers.push(
      props.signalDetectionsByStationQuery.subscribeToMore({
        document: SignalDetectionSubscriptions.detectionsCreatedSubscription,
        updateQuery: (prev: { signalDetectionsByStation: SignalDetectionTypes.SignalDetection[] }, cur) => {
          const data = cur.subscriptionData.data as SignalDetectionTypes.DetectionsCreatedSubscription;

          // Merge the new signal detection into the appropriate place in the current data.
          // Most of this work is done to avoid mutating any data
          const prevSignalDetections = prev.signalDetectionsByStation;
          const newSignalDetections = [...prevSignalDetections];

          if (data) {
            data.detectionsCreated.forEach(detectionCreated => {
              // Check if the detection was already added
              if (newSignalDetections.findIndex(sd => sd.id === detectionCreated.id) < 0) {
                newSignalDetections.push(detectionCreated);
              }
            });
          }

          return {
            ...prev,
            signalDetectionsByStation: newSignalDetections
          };
        }
      })
    );
    this.unsubscribeHandlers.push(
      this.props.eventsInTimeRangeQuery.subscribeToMore({
        document: EventSubscriptions.eventsCreatedSubscription,
        updateQuery: (prev: { eventsInTimeRange: EventTypes.Event[] }, cur) =>
          handleCreatedEvents(prev, cur, this.props.currentTimeInterval)
      })
    );

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
   * Selects rows based on props
   *
   * @param EventsProps props of the event
   */
  private readonly selectRowsFromProps = (props: EventsProps) => {
    if (this.mainTable) {
      this.mainTable.deselectAll();
      this.mainTable.forEachNode(node => {
        props.selectedEventIds.forEach(eid => {
          if (node.data.id === eid) {
            node.setSelected(true);
            this.mainTable.ensureNodeVisible(node, 'middle');
          }
        });
      });
    }
  }

  /**
   * Get the total number of events in the time interval
   *
   * @returns The number of events in the interval as number
   */
  private getNumEventsInInterval() {
    return this.props.eventsInTimeRangeQuery &&
      this.props.eventsInTimeRangeQuery.eventsInTimeRange ?
      this.props.eventsInTimeRangeQuery.eventsInTimeRange.filter(
        event =>
          (event.currentEventHypothesis.eventHypothesis.preferredLocationSolution.
            locationSolution.location.time
            >= this.props.currentTimeInterval.startTimeSecs)
          && (event.currentEventHypothesis.eventHypothesis.preferredLocationSolution.
            locationSolution.location.time
            <= this.props.currentTimeInterval.endTimeSecs)
      ).length
      : 0;
  }

  /**
   * Get the number of complete events in the time interval
   *
   * @returns Number of completed events in interval as number
   */
  private getNumCompleteEventsInInterval() {
    return this.props.eventsInTimeRangeQuery &&
      this.props.eventsInTimeRangeQuery.eventsInTimeRange ?
      this.props.eventsInTimeRangeQuery.eventsInTimeRange.filter(
        event =>
          (event.currentEventHypothesis.eventHypothesis.preferredLocationSolution.
            locationSolution.location.time
            >= this.props.currentTimeInterval.startTimeSecs)
          && (event.currentEventHypothesis.eventHypothesis.preferredLocationSolution.
            locationSolution.location.time
            <= this.props.currentTimeInterval.endTimeSecs)
          && (event.status === 'Complete')
      ).length
      : 0;
  }

  private readonly onFilterChecked = (key: any) => {
    const newMap = cloneDeep(this.state.showEventOfType);
    newMap.set(key, !this.state.showEventOfType.get(key));
    this.setState({showEventOfType: newMap});
  }

  /**
   * Called when button 'Mark selected complete' is clicked
   */
  private handleMarkSelectedComplete() {
    const selectedNodes = this.mainTable.getSelectedNodes();
    if (selectedNodes.length === 0) return;
    const eventIds = selectedNodes.map(node => node.data.id);
    const stageId = selectedNodes[0].data.stageId;
    this.markEventsComplete(eventIds, stageId);
  }

  /**
   * Detemines the disabled status for 'Mark selected complete' button
   */
  private shouldDisableMarkSelectedComplete() {
    let nodesSelected = false;
    let conflictSelected = false;
    if (this.mainTable) {
      nodesSelected = this.mainTable.getSelectedNodes().length > 0;
      conflictSelected = this.mainTable.getSelectedNodes()
        .filter(rowNode => rowNode.data.conflictingSdHyps.length > 0)
        .length > 0;
    }
    return !nodesSelected || conflictSelected;
  }

  /**
   * Handle table row click
   *
   * @param event Row click event
   */
  private readonly onRowClicked = (event: any) => {
    if (this.mainTable) {
      defer(() => {
        const selectedEventIds = this.mainTable.getSelectedNodes()
          .map(node => node.data.id);
        this.props.setSelectedEventIds(selectedEventIds);
      });
    }
  }
  /**
   * Selects or deselects all in table
   * 
   * @param select If trur selects, if false deselects 
   */
  private readonly setSelectionOnAll = (select: boolean) => {
    if (this.mainTable) {
      if (select) {
        const selectedIds = [];
        this.mainTable.forEachNodeAfterFilter((node: any) => {
          selectedIds.push(node.data.id);
        });
        this.mainTable.selectAllFiltered();
        this.props.setSelectedEventIds(selectedIds);

      } else {
        this.mainTable.deselectAll();
        this.props.setSelectedEventIds([]);
      }
    }
  }
  /**
   * Execute mutation - mark event complete
   *
   * @param eventIds event ids
   * @param processingStageId processing stage id
   */
  private readonly markEventsComplete = (eventIds: string[], processingStageId: string) => {
    const variables: EventTypes.UpdateEventsMutationArgs = {
      eventIds,
      input: {
        creatorId: 'Mark',
        processingStageId,
        status: 'Complete',
        activeAnalystUserNames: []
      }
    };
    this.props.updateEvents({
      variables
    })
      .then(() => {
        if (this.props.analystActivity === AnalystActivity.eventRefinement) {
          defer(() => {
            this.openNextEventInList();
          });
        } else {
          this.props.setOpenEventId(undefined);
        }
      })
      .catch(e => window.alert(e));
  }
}
