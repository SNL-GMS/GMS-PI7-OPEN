import { Toolbar, ToolbarTypes } from '@gms/ui-core-components';
import { cloneDeep, isEqual } from 'lodash';
import * as React from 'react';
import { SignalDetectionContextMenu } from '~analyst-ui/common/context-menus/signal-detection-context-menu';
import { SignalDetectionDetails } from '~analyst-ui/common/dialogs';
import { handleCreatedEvents } from '~analyst-ui/common/subscription-handlers/events-created-handler';
import { getAssocSds, getLatestLSS, getOpenEvent } from '~analyst-ui/common/utils/event-util';
import { systemConfig, userPreferences } from '~analyst-ui/config';
import { EventSubscriptions, EventTypes,
  SignalDetectionSubscriptions, SignalDetectionTypes } from '~graphql/';
import { LocationBehavior } from '~graphql/event/types';
import { SignalDetection } from '~graphql/signal-detection/types';
import { addGlForceUpdateOnResize, addGlForceUpdateOnShow } from '~util/gl-util';
import { ContextMenu, Intent, IToaster, NonIdealState, Position,
  Spinner, Toaster } from '../../../../../../node_modules/@blueprintjs/core';
import { LocationHistory } from './components/location-history';
import { convertSignalDetectionToSnapshot,
  generateFalseDiffs,
  getLocationBehavior,
  getNewDefiningForSD,
  getSnapshotsWithDiffs,
  initializeSDDiffs,
  removeTypeName } from './components/location-sd-row-util';
import { LocationSignalDetections } from './components/location-signal-detections';
import { DefiningChange, DefiningTypes } from './components/location-signal-detections/types';
import { DefiningStatus, LocateButtonTooltipMessage, LocationDataState, LocationProps,
  LocationState, SignalDetectionSnapshotWithDiffs, SignalDetectionTableRowChanges } from './types';

const MAX_DEPTH_KM = 6371;
const MAX_LAT_DEGREES = 90;
const MAX_LON_DEGREES = 180;

export class Location extends React.PureComponent<LocationProps, LocationState> {

  /** Handlers to unsubscribe from apollo subscriptions */
  private readonly unsubscribeHandlers: { (): void }[] = [];

  /** Reference to the location display as a whole */
  private locationDisplay: HTMLDivElement;

  /** Reference to LocationHistory table to retreive selected PreferredLocationSolutionId */
  private locationHistory: LocationHistory;

  /** The toaster reference for user notification pop-ups */
  private toaster: IToaster;

  /**
   * constructor
   */
  public constructor(props: LocationProps) {
    super(props);
    const sds =
      props.signalDetectionsByStationQuery && props.signalDetectionsByStationQuery.signalDetectionsByStation ?
        props.signalDetectionsByStationQuery.signalDetectionsByStation : [];
    this.state = {
      topTableHeightPx: 200,
      selectedLocationSolutionSetId: undefined,
      selectedLocationSolutionId: undefined,
      sdDefiningChanges: initializeSDDiffs(sds),
      outstandingLocateCall: false,
    };
  }

  // ***************************************
  // BEGIN REACT COMPONENT LIFECYCLE METHODS
  // ***************************************

  /**
   * React component lifecycle
   *
   * @param prevProps The previous properties available to this react component
   */
  public componentDidUpdate(prevProps: LocationProps) {
    // If new tim einterval, setup subscriptions
    if (this.props.currentTimeInterval &&
      !isEqual(this.props.currentTimeInterval, prevProps.currentTimeInterval)) {
      this.setupSubscriptions(this.props);
    }
    // If the open event has changed, or if a new locate has come in, set state to default
    const maybePrevEvent =
      prevProps.openEventId && prevProps.eventsInTimeRangeQuery.eventsInTimeRange ?
        prevProps.eventsInTimeRangeQuery.eventsInTimeRange.find(event => event.id === prevProps.openEventId)
        : undefined;
    const maybeOpenEvent =
      this.props.openEventId && this.props.eventsInTimeRangeQuery.eventsInTimeRange ?
        this.props.eventsInTimeRangeQuery.eventsInTimeRange.find(event => event.id === this.props.openEventId)
        : undefined;
    if ((maybeOpenEvent && !maybePrevEvent) ||
        (maybeOpenEvent && maybePrevEvent
          && ((maybeOpenEvent.id !== maybePrevEvent.id) ||
             (maybeOpenEvent.currentEventHypothesis.eventHypothesis.locationSolutionSets.length !==
        maybePrevEvent.currentEventHypothesis.eventHypothesis.locationSolutionSets.length)))) {
        const currentLSS = getLatestLSS(maybeOpenEvent);
        const sds =
          this.props.signalDetectionsByStationQuery &&
            this.props.signalDetectionsByStationQuery.signalDetectionsByStation ?
              this.props.signalDetectionsByStationQuery.signalDetectionsByStation : [];
        this.setState({
          selectedLocationSolutionSetId: currentLSS.id,
          selectedLocationSolutionId: currentLSS.locationSolutions[0].id,
          sdDefiningChanges: initializeSDDiffs(sds)
        });
        return;
    }
    if (!(prevProps.signalDetectionsByStationQuery &&
         prevProps.signalDetectionsByStationQuery.signalDetectionsByStation)
         && (this.props.signalDetectionsByStationQuery &&
          this.props.signalDetectionsByStationQuery.signalDetectionsByStation)) {
          const sds =
              this.props.signalDetectionsByStationQuery.signalDetectionsByStation;
          this.setState({sdDefiningChanges: initializeSDDiffs(sds)});
    }
  }

  /**
   * Invoked when the component will unmount.
   */
  public componentWillUnmount() {
    // Unsubscribe from all current subscriptions
    this.unsubscribeHandlers.forEach(unsubscribe => unsubscribe());
    this.unsubscribeHandlers.length = 0;
  }

  /**
   * Renders the component.
   */
  // tslint:disable-next-line: cyclomatic-complexity
  public render() {

    // no spinner if queries haven't been issued
    const dataState: LocationDataState =
      (!this.props.eventsInTimeRangeQuery || !this.props.signalDetectionsByStationQuery) ?
        LocationDataState.NO_INTERVAL
        : (this.props.eventsInTimeRangeQuery && this.props.eventsInTimeRangeQuery.loading) ?
          LocationDataState.NO_EVENTS
          : (this.props.signalDetectionsByStationQuery.loading ||
            !this.props.signalDetectionsByStationQuery.signalDetectionsByStation) ?
            LocationDataState.NO_SDS
            : (!this.props.openEventId) ?
              LocationDataState.NO_EVENT_OPEN
              : LocationDataState.READY;
    if (dataState !== LocationDataState.READY) {
      return (
        <NonIdealState
         action={
          dataState === LocationDataState.NO_SDS || dataState === LocationDataState.NO_EVENTS ?
          <Spinner intent={Intent.PRIMARY} />
          : null
        }
         title={
           dataState === LocationDataState.NO_SDS || dataState === LocationDataState.NO_EVENTS ?
          'Loading:'
          : dataState === LocationDataState.NO_INTERVAL ?
            'No events loaded'
            : dataState === LocationDataState.NO_EVENT_OPEN ?
              'No event selected'
              : null
        }
         description={
          dataState === LocationDataState.NO_SDS ?
          'Signal Detections...'
          : dataState === LocationDataState.NO_EVENTS ?
            'Events...'
            : dataState === LocationDataState.NO_INTERVAL ?
              'Open interval to load events'
              : dataState === LocationDataState.NO_EVENT_OPEN ?
                'Select an event to refine location'
                : null
       }
        />
        );
    }

    const openEvent = getOpenEvent(this.props.openEventId, this.props.eventsInTimeRangeQuery);
    if (!openEvent) {
      return (
        <NonIdealState
          title="Selected Event Not Found"
          description={'Refresh the Page and Cross Your Fingers'}
        />
      );
    }
    const assocSDs: SignalDetectionTypes.SignalDetection[] =
      getAssocSds(openEvent, this.props.signalDetectionsByStationQuery.signalDetectionsByStation);
    const disableLocate: {isDisabled: boolean; reason: LocateButtonTooltipMessage | string} = this.disableLocate();
    const toolbarItems: ToolbarTypes.ToolbarItem[] = [{
        label: 'Locate',
        tooltip: disableLocate.reason,
        type: ToolbarTypes.ToolbarItemType.Button,
        rank: 1,
        widthPx: 60,
        menuLabel: 'Locate',
        disabled: disableLocate.isDisabled,
        onChange: () => {this.locate(); }
      }];
    const toolbarLeftItems: ToolbarTypes.ToolbarItem[] =
      [{
        tooltip: 'Displays if a locate call is in progress',
        label: 'Locating...',
        type: ToolbarTypes.ToolbarItemType.LoadingSpinner,
        rank: 1,
        value: {
          itemsToLoad: this.state.outstandingLocateCall ? 1 : 0,
          hideTheWordLoading: true,
          hideOutstandingCount: false
        },
        widthPx: 100
      }];
    const distanceToStations = this.props.distanceToSourceForDefaultStationsQuery ?
      this.props.distanceToSourceForDefaultStationsQuery.distanceToSourceForDefaultStations : [];
    const height = this.state.topTableHeightPx.toString() + 'px';
    // If the latest locationsolutionset is NOT selected, enabled historical mode
    const historicalMode = getLatestLSS(openEvent).id !== this.state.selectedLocationSolutionSetId;
    const snapshots = this.getSnapshots(historicalMode, openEvent, assocSDs);
    return (
      <div
        className="location-wrapper"
        onKeyDown={this.onKeyPress}
        tabIndex={-1}
        onMouseEnter={e => {
          e.currentTarget.focus();
        }}
      >
        <div
          className={'table-container'}
          ref={ref => {if (ref) {
            this.locationDisplay = ref;
          }}}
        >
          <div className={'location__toolbar'}>
            <Toolbar
              items={toolbarItems}
              itemsLeft={toolbarLeftItems}
              toolbarWidthPx={this.props.glContainer ?
                this.props.glContainer.width - userPreferences.list.widthOfTableMarginsPx : 0}
            />
          </div>
          <div
            className={'location-table'}
            style={{height}}
          >
            <LocationHistory
              ref={ref => {
                if (ref) {
                  this.locationHistory = ref;
                }
              }}
              event={openEvent}
              setSelectedLSSAndLS={this.setSelectedLSAndLSS}
              selectedLocationSolutionId={this.state.selectedLocationSolutionId}
              selectedLocationSolutionSetId={this.state.selectedLocationSolutionSetId}
            />
          </div>
          {/* drag handle divider */}
          <div
            className="location-divider"
            onMouseDown={this.onThumbnailDividerDrag}
          >
            <div
              className="location-divider__spacer"
            />
          </div>
          <div
            className={'location-table-bottom'}
          >
            <LocationSignalDetections
              event={openEvent}
              distanceToStations={distanceToStations}
              signalDetectionDiffSnapshots={snapshots}
              historicalMode={historicalMode}
              changeSignalDetectionAssociations={this.props.changeSignalDetectionAssociations}
              rejectDetectionHypotheses={this.props.rejectDetectionHypotheses}
              updateDetections={this.props.updateDetections}
              createEvent={this.props.createEvent}
              setSdIdsToShowFk={this.props.setSdIdsToShowFk}
              selectedSdIds={this.props.selectedSdIds}
              setSelectedSdIds={this.props.setSelectedSdIds}
              showSDContextMenu={this.showSDContextMenu}
              showSDDetails={this.showSDDetails}
              updateIsDefining={this.updateIsDefining}
              setDefining={this.setDefiningForColumn}
              setMeasurementModeEntries={this.props.setMeasurementModeEntries}
              toast={this.toast}
            />
          </div>
        </div>
      </div>

    );
  }

  public componentDidMount() {
    addGlForceUpdateOnShow(this.props.glContainer, this);
    addGlForceUpdateOnResize(this.props.glContainer, this);
  }

  // ***************************************
  // END REACT COMPONENT LIFECYCLE METHODS
  // ***************************************

  private readonly onKeyPress = (e: React.KeyboardEvent<HTMLDivElement>) => {
    if (e.metaKey || e.ctrlKey) {
      if (e.nativeEvent.code === 'KeyL') {
        if (!this.disableLocate()) {
          this.locate();
        }
      }
    }
  }

  /**
   * Start a drag on mouse down on the divider
   * 
   * @param e mouse event
   */
  private readonly onThumbnailDividerDrag = (e: React.MouseEvent<HTMLDivElement>) => {

    const maxHPct = 0.8;
    const maxHeightPx = this.locationDisplay.clientHeight * maxHPct;
    const minH = 100;
    const HEIGHT_OF_DIVIDER = 7;
    const onMouseMove = (e2: MouseEvent) => {
      const curPos = e2.clientY - this.locationDisplay.getBoundingClientRect().top - HEIGHT_OF_DIVIDER;
      if (curPos < maxHeightPx && curPos > minH) {
        this.setState({topTableHeightPx: curPos});
      }
    };

    const onMouseUp = (e2: MouseEvent) => {
      document.body.removeEventListener('mousemove', onMouseMove);
      document.body.removeEventListener('mouseup', onMouseUp);
    };

    document.body.addEventListener('mousemove', onMouseMove);
    document.body.addEventListener('mouseup', onMouseUp);
  }

  // ***************************************
  // BEGIN Helper functions, please move to a util when possible
  // ***************************************

  /**
   * Determines if the locate button can be used
   */
  private readonly disableLocate = (): {isDisabled: boolean; reason: LocateButtonTooltipMessage | string} => {
    if (!this.props.signalDetectionsByStationQuery ||
      !this.props.signalDetectionsByStationQuery.signalDetectionsByStation ||
      !this.props.openEventId) {
      return {isDisabled: true, reason: LocateButtonTooltipMessage.InvalidData};
    }

    if (!this.isLastLocationSetValid()) {
      return {isDisabled: true, reason: LocateButtonTooltipMessage.BadLocationAttributes};
    }

    const numberOfRequiredBehaviors = systemConfig.numberOfDefiningLocationBehaviorsRequiredForLocate;
    const locationBehaviors = this.getLocationBehaviors(
      this.props.signalDetectionsByStationQuery.signalDetectionsByStation);
    const definingList = locationBehaviors.map(lb => lb.defining);
    const definingCount = definingList.reduce(
      (prev, cur) =>  cur ? prev + 1 : prev
    , 0);
    return definingCount < systemConfig.numberOfDefiningLocationBehaviorsRequiredForLocate ?
     {isDisabled: true, reason:
      `${numberOfRequiredBehaviors} ${LocateButtonTooltipMessage.NotEnoughDefiningBehaviors}`} :
     {isDisabled: false, reason: LocateButtonTooltipMessage.Correct};
  }

  /**
   * Helper function to get the correct snapshots for the rendering state
   */
  private readonly getSnapshots = (historicalMode: boolean,
    openEvent: EventTypes.Event,
    assocSDs: SignalDetectionTypes.SignalDetection[]): SignalDetectionSnapshotWithDiffs[] => {
    if (historicalMode) {
      const maybeSelectedLSS =
        openEvent.currentEventHypothesis.eventHypothesis.locationSolutionSets
        .find(lss => lss.id === this.state.selectedLocationSolutionSetId);
      if (!maybeSelectedLSS) {
        return [];
      } else {
        const location =
          maybeSelectedLSS.locationSolutions.find(ls => ls.id === this.state.selectedLocationSolutionId);
        if (!location) {
          return [];
        }
        return generateFalseDiffs(location.snapshots);
      }
    } else {
      const latestLSS = getLatestLSS(openEvent);
      const locationSolution =
        this.state.selectedLocationSolutionId ?
          latestLSS.locationSolutions.find(ls => ls.id === this.state.selectedLocationSolutionId) ?
            latestLSS.locationSolutions.find(ls => ls.id === this.state.selectedLocationSolutionId)
            : openEvent.currentEventHypothesis.eventHypothesis.preferredLocationSolution.locationSolution
          : openEvent.currentEventHypothesis.eventHypothesis.preferredLocationSolution.locationSolution;

      const locationBehaviors = locationSolution.locationBehaviors;

      const lastCalculatedLSS = getLatestLSS(openEvent);
      if (!lastCalculatedLSS) {
        return [];
      }
      const signalDetectionSnapshots = locationSolution.snapshots;
      const assocSDSnapshots = assocSDs.map(sd => {
        if (sd) {
          return convertSignalDetectionToSnapshot(
            sd,
            locationBehaviors,
            this.getDefiningStatusForSdId(sd.id)
          );
        }
      }
      );
      const mergedSnapshots = getSnapshotsWithDiffs(assocSDSnapshots, signalDetectionSnapshots);
      return mergedSnapshots;
    }

  }

  /**
   * @param sdId id of signal detection to look up
   */
  private readonly getDefiningStatusForSdId = (sdId: string):
    DefiningStatus => {
      const maybeRow = this.state.sdDefiningChanges.find(sdr => sdr.signalDetectionId === sdId);
      if (maybeRow) {
        return {
          arrivalTimeDefining: maybeRow.arrivalTimeDefining,
          slownessDefining: maybeRow.slownessDefining,
          azimuthDefining: maybeRow.azimuthDefining
        };
      } else {
        return {
          arrivalTimeDefining: DefiningChange.NO_CHANGE,
          slownessDefining: DefiningChange.NO_CHANGE,
          azimuthDefining: DefiningChange.NO_CHANGE
        };
      }
  }

  /**
   * Retreive the Location Behaviors from the current SignalDetectionAssociations.
   * Used by Locate Event Mutation
   *
   * @param signalDetections list of sd's to get location behaviors from
   * @returns List of LocationBehaviors
   */
  private readonly getLocationBehaviors = (signalDetections: SignalDetection[]): LocationBehavior[] => {
    const locationBehaviors: LocationBehavior[] = [];
    // If no open event then return empty list
    if (!this.props.openEventId) {
      return [];
    }
    // For each SD find the SD Row for the defining values.
    // Change the location behavior defining values according.
    const openEvent = this.props.openEventId && this.props.eventsInTimeRangeQuery.eventsInTimeRange ?
    this.props.eventsInTimeRangeQuery.eventsInTimeRange.find(e => e.id === this.props.openEventId) : undefined;
    if (!openEvent) {
      return [];
    }

    const historicalMode = getLatestLSS(openEvent).id !== this.state.selectedLocationSolutionSetId;
    const assocSDs: SignalDetectionTypes.SignalDetection[] =
      getAssocSds(openEvent, this.props.signalDetectionsByStationQuery.signalDetectionsByStation);
    this.getSnapshots(historicalMode, openEvent, assocSDs)
      .forEach(sdsnap => {
        if (!sdsnap.rejectedOrUnnassociated) {
          const prevLocationBehaviors = cloneDeep(
            openEvent.currentEventHypothesis.eventHypothesis
            .preferredLocationSolution.locationSolution.locationBehaviors);
          const sdTableRowChange = this.state.sdDefiningChanges
            .find(sdRC => sdRC.signalDetectionId === sdsnap.signalDetectionId);
          const sd = signalDetections.find(sdreal => sdreal.id === sdsnap.signalDetectionId);
          const arrivalLoc = getLocationBehavior(DefiningTypes.ARRIVAL_TIME, sd, prevLocationBehaviors);
          const azimuthLoc = getLocationBehavior(DefiningTypes.AZIMUTH, sd, prevLocationBehaviors);
          const slowLoc = getLocationBehavior(DefiningTypes.SLOWNESS, sd, prevLocationBehaviors);
          if (sdTableRowChange) {
            if (arrivalLoc) {
              arrivalLoc.defining =
                sdTableRowChange.arrivalTimeDefining === DefiningChange.CHANGED_TO_FALSE ?
                  false
                  : sdTableRowChange.arrivalTimeDefining === DefiningChange.CHANGED_TO_TRUE ?
                    true
                    : arrivalLoc.defining;
            }
            if (azimuthLoc) {
              azimuthLoc.defining =
                sdTableRowChange.azimuthDefining === DefiningChange.CHANGED_TO_FALSE ?
                  false
                  : sdTableRowChange.azimuthDefining === DefiningChange.CHANGED_TO_TRUE ?
                    true
                    : azimuthLoc.defining;
            }
            if (slowLoc)  {
              slowLoc.defining =
                sdTableRowChange.slownessDefining === DefiningChange.CHANGED_TO_FALSE ?
                  false
                  : sdTableRowChange.slownessDefining === DefiningChange.CHANGED_TO_TRUE ?
                    true
                    : slowLoc.defining;
            }
          }
          if (arrivalLoc) locationBehaviors.push(removeTypeName(arrivalLoc));
          if (azimuthLoc) locationBehaviors.push(removeTypeName(azimuthLoc));
          if (slowLoc) locationBehaviors.push(removeTypeName(slowLoc));
        }
    });
    return locationBehaviors;
}

// ***************************************
// BEGIN Callbacks functions for child components
// ***************************************

/**
 * Displays a toast on invalid input
 * 
 * @param message the message to show
 */
  private readonly toast = (message: string) => {
    if (!this.toaster) {
      this.toaster = Toaster.create({ position: Position.BOTTOM_RIGHT });
    }
    this.toaster.show({ message });
  }

  /**
   * Callback to set the selected location solution set
   * 
   * @param locationSolutionSetId the id of the locationsolutionset
   */
  private readonly setSelectedLSAndLSS =
  (locationSolutionSetId: string, locationSolutionId: string) =>
    this.setState(
      {selectedLocationSolutionSetId: locationSolutionSetId,
       selectedLocationSolutionId: locationSolutionId
      })

  /**
   * Sets new sd rows to state
   * 
   * @param isDefining whether the new row is defining
   * @param definingType which fm will be set
   */
  private readonly setDefiningForColumn = (isDefining: boolean, definingType: DefiningTypes) => {
    const signalDetections =
      getAssocSds(
        getOpenEvent(this.props.openEventId, this.props.eventsInTimeRangeQuery),
        this.props.signalDetectionsByStationQuery.signalDetectionsByStation);
    const currentSdIds = signalDetections.map(sd => sd.id);
    const rowsWithNullEntriesFilled =
      currentSdIds.map(sdId =>
        this.state.sdDefiningChanges.find(sdc => sdc.signalDetectionId === sdId) ?
          this.state.sdDefiningChanges.find(sdc => sdc.signalDetectionId === sdId) :
          {
              arrivalTimeDefining: DefiningChange.NO_CHANGE,
              azimuthDefining: DefiningChange.NO_CHANGE,
              slownessDefining: DefiningChange.NO_CHANGE,
              signalDetectionId: sdId
            }
    );
    const openEvent = getOpenEvent(this.props.openEventId, this.props.eventsInTimeRangeQuery);
    const newRows = rowsWithNullEntriesFilled.map(row =>
      getNewDefiningForSD(
        definingType, isDefining,
        signalDetections.find(sd => sd.id === row.signalDetectionId), row, openEvent));
    this.setState({
      ...this.state,
      sdDefiningChanges: newRows
    });
  }

  /**
   * Shows signal detection details
   * 
   * @param sdId Id of signal detection
   * @param x offset left for context menu
   * @param y offset top for context menu
   */
  private readonly showSDDetails = (sdId: string, x: number, y: number) => {
    // Display information of the signal detection
    const detection = this.props.signalDetectionsByStationQuery.signalDetectionsByStation
    .filter(sd => sd.id === sdId);
    ContextMenu.show(
    <SignalDetectionDetails
      detection={detection[0]}
      color={userPreferences.colors.events.inProgress}
    />,
    { left: x, top: y }, () => {
      // Menu was closed; callback optional
    });
  }
  private readonly showSDContextMenu = (selectedSdIds: string[], x: number, y: number) => {
    const sds: SignalDetection[]
      = this.props.signalDetectionsByStationQuery.signalDetectionsByStation.filter(
      sd => selectedSdIds.indexOf(sd.id) >= 0);
    const currentlyOpenEvent: EventTypes.Event =
      this.props.openEventId && this.props.eventsInTimeRangeQuery.eventsInTimeRange ?
      this.props.eventsInTimeRangeQuery.eventsInTimeRange.find(e => e.id === this.props.openEventId) : undefined;

    const context =
      (
        <SignalDetectionContextMenu
          signalDetections={this.props.signalDetectionsByStationQuery.signalDetectionsByStation}
          selectedSds={sds}
          sdIdsToShowFk={this.props.sdIdsToShowFk}
          currentOpenEvent={currentlyOpenEvent}
          changeAssociation={this.props.changeSignalDetectionAssociations}
          rejectDetectionHypotheses={this.props.rejectDetectionHypotheses}
          updateDetections={this.props.updateDetections}
          setSdIdsToShowFk={this.props.setSdIdsToShowFk}
          associateToNewEvent={this.props.createEvent}
          measurementMode={this.props.measurementMode}
          setSelectedSdIds={this.props.setSelectedSdIds}
          setMeasurementModeEntries={this.props.setMeasurementModeEntries}
        />
      );
    ContextMenu.show(
      context, {
        left: x,
        top: y
    });
  }

  /**
   * Update the Signal Detection isDefining value(checkbox) in the state
   * 
   * @param definingType which isDefing value to update Arrival Time, Slowness or Azimuth
   * @param signalDetectionHypothesisId which Signal Detection hypothesis to update
   * @param setDefining if true sets defining to true, otherwise false
   */
  private readonly updateIsDefining = (definingType: DefiningTypes,
    signalDetectionId: string, setDefining: boolean): void => {
    const openEvent = getOpenEvent(this.props.openEventId, this.props.eventsInTimeRangeQuery);
    const signalDetection =
      getAssocSds(
        openEvent,
        this.props.signalDetectionsByStationQuery.signalDetectionsByStation)
      .find(sd => sd.id === signalDetectionId);
    // If we do not find the signal detection then can't update
    if (!signalDetection) {
      return;
    }
    const sdRowChanges: SignalDetectionTableRowChanges =
      this.state.sdDefiningChanges
        .find(row => row.signalDetectionId === signalDetection.id) ?
          this.state.sdDefiningChanges
          .find(row => row.signalDetectionId === signalDetection.id)
        : {
          arrivalTimeDefining: DefiningChange.NO_CHANGE,
          azimuthDefining: DefiningChange.NO_CHANGE,
          slownessDefining: DefiningChange.NO_CHANGE,
          signalDetectionId: signalDetection.id
        };
    const newSdRow = getNewDefiningForSD(definingType, setDefining, signalDetection, sdRowChanges, openEvent);
    const newDefining =
    [...this.state.sdDefiningChanges.filter(sdc => sdc.signalDetectionId !== signalDetection.id), newSdRow];
    this.setState({sdDefiningChanges: newDefining});
  }

  /**
   * Sends location mutation to the gateway
   */
  private readonly locate = () => {
    const openEvent = this.props.openEventId && this.props.eventsInTimeRangeQuery.eventsInTimeRange ?
      this.props.eventsInTimeRangeQuery.eventsInTimeRange.find(e => e.id === this.props.openEventId) : undefined;
    const eventHypothesisId = openEvent.currentEventHypothesis.eventHypothesis.id;
    const preferredLocationSolutionId = this.locationHistory.getPreferredLocationSolutionId(eventHypothesisId);
    const locationBehaviors = this.getLocationBehaviors(
      this.props.signalDetectionsByStationQuery.signalDetectionsByStation);

    // Call the mutation the return is the updated EventHypothesis
    // which magically updates in Apollo cache
    const variables: EventTypes.LocateEventMutationArgs = {
      eventHypothesisId,
      preferredLocationSolutionId,
      locationBehaviors
     };
    this.setState({outstandingLocateCall: true});
    this.props.locateEvent({
       variables
     })
     .then(() => {
      const updateFpVar: EventTypes.UpdateFeaturePredictionsMutationArgs = {
        eventId: openEvent.id
      };
      this.props.updateFeaturePredictions({variables: updateFpVar})
      .then(() => {
        this.setState({outstandingLocateCall: false});
      })
      .catch(e => window.alert(e));
     })
     .catch(e => window.alert(e));
  }

  private readonly isLocationValid = (location: EventTypes.LocationSolution): boolean => {
    let valid = true;
    valid = valid && location.location.depthKm <= MAX_DEPTH_KM && location.location.depthKm >= 0;
    valid = valid && Math.abs(location.location.latitudeDegrees) <= MAX_LAT_DEGREES;
    valid =  valid && Math.abs(location.location.longitudeDegrees) <= MAX_LON_DEGREES;
    return valid;
  }

  private readonly isLastLocationSetValid = (): boolean => {
    const openEvent = this.props.openEventId && this.props.eventsInTimeRangeQuery.eventsInTimeRange ?
      this.props.eventsInTimeRangeQuery.eventsInTimeRange.find(e => e.id === this.props.openEventId) : undefined;
    const preferred = getLatestLSS(openEvent);
    let valid = true;
    preferred.locationSolutions.forEach(l => {
      valid = valid && this.isLocationValid(l);
    });

    return valid;
  }

  // ***************************************
  // BEGIN GraphQL Wiring
  // ***************************************

  /**
   * Initialize graphql subscriptions on the apollo client
   * 
   * @param LocationProps props of the event
   */
  private readonly setupSubscriptions = (props: LocationProps): void => {
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
}
