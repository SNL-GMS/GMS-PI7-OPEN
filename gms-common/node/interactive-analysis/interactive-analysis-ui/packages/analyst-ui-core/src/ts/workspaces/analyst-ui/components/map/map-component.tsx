import { Checkbox, Classes, ContextMenu } from '@blueprintjs/core';
import { TimeUtil } from '@gms/ui-core-components';
import * as lodash from 'lodash';
import * as React from 'react';
import { SignalDetectionContextMenu } from '~analyst-ui/common/context-menus/signal-detection-context-menu';
import { SignalDetectionDetails } from '~analyst-ui/common/dialogs';
import { handleCreatedEvents } from '~analyst-ui/common/subscription-handlers/events-created-handler';
import { determineDetectionColor } from '~analyst-ui/common/utils/signal-detection-util';
import { analystUiConfig, environmentConfig } from '~analyst-ui/config';
import { EventSubscriptions,
  EventTypes, SignalDetectionSubscriptions, SignalDetectionTypes } from '~graphql/';
import { CESIUM_OFFLINE } from '~util/environment';
import { addGlForceUpdateOnResize, addGlForceUpdateOnShow } from '~util/gl-util';
import { UILogger } from '~util/log/logger';
import { CesiumMap } from './components/cesium-map';
import { MapAPI } from './components/map-api';
import { LayerLabels, LayerTooltips, MapProps, MapState } from './types';

/**
 * Primary map display
 */
export class Map extends React.PureComponent<MapProps, MapState> {

  /**
   * handle to the dom element we want to render Map inside of.
   */
  private containerDomElement: HTMLDivElement;

  /**
   * Handlers to unsubscribe from apollo subscriptions
   */
  private readonly unsubscribeHandlers: { (): void }[] = [];

  private readonly map: MapAPI;

  // ***************************************
  // BEGIN REACT COMPONENT LIFECYCLE METHODS
  // ***************************************

  /**
   * Constructor.
   * 
   * @param props The initial props
   */
  public constructor(props) {
    super(props);
    this.map = new CesiumMap({
      events: {
        onMapClick: this.onMapClick,
        onMapRightClick: this.onMapRightClick,
        onMapShiftClick: this.onMapShiftClick,
        onMapDoubleClick: this.onMapDoubleClick,
        onMapAltClick: this.onMapAltClick
      },
      analystUiConfig
    });
  }

  /**
   * Invoked when the componented mounted.
   */
  public componentDidMount() {
    addGlForceUpdateOnShow(this.props.glContainer, this);
    addGlForceUpdateOnResize(this.props.glContainer, this);
    this.map.initialize(this.containerDomElement);

    UILogger.info(`Cesium configured with CESIUM_OFFLINE=(${CESIUM_OFFLINE})`);
  }

  /**
   * Invoked when the componented mounted.
   * 
   * @param prevProps The previous props
   * @param prevState The previous state
   */
  // tslint:disable-next-line:cyclomatic-complexity
  public componentDidUpdate(prevProps: MapProps) {
    if (this.props.currentTimeInterval && this.props.defaultStationsQuery.defaultStations &&
      !lodash.isEqual(this.props.currentTimeInterval, prevProps.currentTimeInterval)) {
      this.setupSubscriptions(this.props);
    }

    if (!lodash.isEqual(
      this.props.defaultStationsQuery.defaultStations,
      prevProps.defaultStationsQuery.defaultStations)) {
      this.map.drawDefaultStations(
        prevProps.defaultStationsQuery.defaultStations,
        this.props.defaultStationsQuery.defaultStations);
    }

    const prevEventsInTimeRange = prevProps.eventsInTimeRangeQuery &&
      prevProps.eventsInTimeRangeQuery.eventsInTimeRange ?
      prevProps.eventsInTimeRangeQuery.eventsInTimeRange : [];

    const currentEventsInTimeRange = this.props.eventsInTimeRangeQuery &&
      this.props.eventsInTimeRangeQuery.eventsInTimeRange ?
      this.props.eventsInTimeRangeQuery.eventsInTimeRange : [];

    const prevSignalDetectionsByStation = prevProps.signalDetectionsByStationQuery &&
      prevProps.signalDetectionsByStationQuery.signalDetectionsByStation ?
      prevProps.signalDetectionsByStationQuery.signalDetectionsByStation : [];

    const currentSignalDetectionsByStation = this.props.signalDetectionsByStationQuery &&
      this.props.signalDetectionsByStationQuery.signalDetectionsByStation ?
      this.props.signalDetectionsByStationQuery.signalDetectionsByStation : [];

    if (!lodash.isEqual(currentEventsInTimeRange, prevEventsInTimeRange)
      || this.props.selectedEventIds !== prevProps.selectedEventIds) {
      this.map.drawEvents(prevProps, this.props);
    }

    const previousOpenEvent = this.props.openEventId ?
      currentEventsInTimeRange.find(e => e.id === prevProps.openEventId) : undefined;

    const currentOpenEvent = this.props.openEventId ?
        currentEventsInTimeRange.find(e => e.id === this.props.openEventId)
        : undefined;

    if (!lodash.isEqual(currentOpenEvent, previousOpenEvent)) {

      if (this.props.openEventId !== prevProps.openEventId) {
        this.map.highlightOpenEvent(
          this.props.currentTimeInterval,
          previousOpenEvent,
          currentOpenEvent,
          this.props.selectedEventIds);
      }

      this.map.drawSignalDetections(
        currentSignalDetectionsByStation, currentEventsInTimeRange, currentOpenEvent);
      this.map.drawUnAssociatedSignalDetections(
        currentSignalDetectionsByStation, currentEventsInTimeRange, currentOpenEvent);
      this.map.drawOtherAssociatedSignalDetections(
        currentSignalDetectionsByStation, currentEventsInTimeRange, currentOpenEvent);
    } else {
      if (!lodash.isEqual(currentSignalDetectionsByStation, prevSignalDetectionsByStation)) {
        this.map.drawSignalDetections(
          currentSignalDetectionsByStation, currentEventsInTimeRange, currentOpenEvent);
        this.map.drawUnAssociatedSignalDetections(
          currentSignalDetectionsByStation, currentEventsInTimeRange, currentOpenEvent);
        this.map.drawOtherAssociatedSignalDetections(
          currentSignalDetectionsByStation, currentEventsInTimeRange, currentOpenEvent);

        this.map.updateStations(
          prevSignalDetectionsByStation, previousOpenEvent,
          currentSignalDetectionsByStation, currentOpenEvent);
      }
    }

    if (!lodash.isEqual(this.props.selectedSdIds, prevProps.selectedSdIds)) {
      this.map.highlightSelectedSignalDetections(this.props.selectedSdIds);
    }

    if (!lodash.isEqual(this.props.selectedSdIds, prevProps.selectedSdIds)) {
      this.selectSignalDetectionsFromProps(this.props);
    }

    // Explicitly render a new frame
    // tslint:disable-next-line: newline-per-chained-call
    this.map.getViewer().scene.requestRender();
  }

  /**
   * Invoked when the componented will unmount.
   */
  public componentWillUnmount() {
    // unsubscribe from all current subscriptions
    this.unsubscribeHandlers.forEach(unsubscribe => unsubscribe());
    this.unsubscribeHandlers.length = 0;
  }

  /**
   * Renders the component.
   */
  public render() {
    return (
      <div className="map">
        <div className="map__column">
          {this.mapTopOptions()}
          <div className="map__row">
            <div
              className="map-layer-toggles"
            >
              {this.generateLayerToggles()}
            </div>
            <div className="map__inner-column">
              <div className="map__rendering-wrapper-1">
                <div
                  className="map__rendering-wrapper-2 max"
                  ref={ref => { if (ref) { this.containerDomElement = ref; } }}
                />
              </div>
            </div>
          </div>
        </div>
        <div className="map__status-online-indicator">
          {environmentConfig.map.online ?
            (<span className="map__status-online-indicator__online-mode">Online Mode</span>) :
            (<span className="map__status-online-indicator__offline-mode">Offline Mode</span>)
          }
        </div>
      </div>
    );
  }

  // ***************************************
  // END REACT COMPONENT LIFECYCLE METHODS
  // ***************************************

  /**
   * Initialize graphql subscriptions on the apollo client
   */
  private readonly setupSubscriptions = (props: MapProps): void => {
    if (!props.eventsInTimeRangeQuery && !props.signalDetectionsByStationQuery) return;

    // first, unsubscribe from all current subscriptions
    this.unsubscribeHandlers.forEach(unsubscribe => unsubscribe());
    this.unsubscribeHandlers.length = 0;

    // don't register subscriptions if the current time interval is undefined/null
    if (!props.currentTimeInterval) return;

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
   * Generate the sidebar with layer toggles
   */
  private readonly generateLayerToggles = (): JSX.Element[] =>
    Object.keys(this.map.getDataLayers())
      .map(id =>
        (
        <Checkbox
          key={id}
          labelElement={(<span title={LayerTooltips[id]}>{LayerLabels[id]}</span>)}
          style={{
            margin: '0.5rem'
          }}
          title={id}
          checked={this.map.getDataLayers()[id].show}
          onChange={e => this.toggleDataLayerVisibility(id)}
        />
      ))

  private readonly mapTopOptions = (): JSX.Element => (
    <div className="map-controls">
      <Checkbox
        label="Sync with user actions"
        style={{ marginBottom: '0px' }}
      />
      <label
        className={`${Classes.LABEL} ${Classes.INLINE} map-controls__label`}
      >
        Start time: <span className="map-time">{TimeUtil.dateToISOString(new Date())}</span>
      </label>
      <label
        className={`${Classes.LABEL} ${Classes.INLINE} map-controls__label`}
      >
        End time: <span className="map-time">{TimeUtil.dateToISOString(new Date())}</span>
      </label>
    </div>
  )

  /**
   * Toggle the visibility of a data source
   */
  private readonly toggleDataLayerVisibility = (id: string, show?: boolean) => {
    const layer = this.map.getDataLayers()[id];
    if (layer) {
      show == undefined ?
        layer.show = !layer.show
        : layer.show = show;
      this.forceUpdate();
    }
  }

  private readonly onMapAltClick = (clickEvent: any, entity?: any) => {
    if (entity && entity.entityType === 'sd') {
        const detection = this.props.signalDetectionsByStationQuery.signalDetectionsByStation
          .filter(sd => sd.id === entity.id)[0];
        const color =
          determineDetectionColor(
            detection,
            this.props.eventsInTimeRangeQuery.eventsInTimeRange, this.props.openEventId);
        ContextMenu.show(
          <SignalDetectionDetails
            detection={detection}
            color={color}
          />,
          { left: clickEvent.position.x, top: clickEvent.position.y }, () => {
            // Menu was closed; callback optional
          });
      }
  }
  /**
   * Handler for map click event
   */
  private readonly onMapClick = (clickEvent: any, entity?: any) => {
    if (entity && entity.entityType === 'event') {
      this.props.setSelectedEventIds([entity.id]);
    } else if (entity && entity.entityType === 'sd') {
      this.props.setSelectedSdIds([entity.id]);

    } else {
      this.props.setSelectedSdIds([]);
      this.props.setSelectedEventIds([]);
    }

  }
  /**
   * Handler for map right click event
   */
  private readonly onMapRightClick = (clickEvent: any, entity?: any) => {
    if (entity && entity.entityType === 'sd') {
      // const sdIds = this.props.selectedSdIds.indexOf(entity.id) >= 0 ?
      // [...this.props.selectedSdIds, entity.id] : [entity.id];
      const sdIds =
        this.props.selectedSdIds.length > 0 ?
          this.props.selectedSdIds.indexOf(entity.id) >= 0 ?
            this.props.selectedSdIds
            : [...this.props.selectedSdIds, entity.id]
          : [entity.id];
      const sds = this.props.signalDetectionsByStationQuery.signalDetectionsByStation
                  .filter(sd => sdIds.indexOf(sd.id) >= 0);
      const currentOpenEvent: EventTypes.Event | undefined =
        this.props.eventsInTimeRangeQuery.eventsInTimeRange.find(ev => ev.id === this.props.openEventId);
      const sdMenu = (
        <SignalDetectionContextMenu
          signalDetections={this.props.signalDetectionsByStationQuery.signalDetectionsByStation}
          selectedSds={sds}
          currentOpenEvent={currentOpenEvent}
          changeAssociation={this.props.changeSignalDetectionAssociations}
          rejectDetectionHypotheses={this.props.rejectDetectionHypotheses}
          updateDetections={this.props.updateDetections}
          setSdIdsToShowFk={this.props.setSdIdsToShowFk}
          sdIdsToShowFk={this.props.sdIdsToShowFk}
          associateToNewEvent={this.props.createEvent}
          measurementMode={this.props.measurementMode}
          setSelectedSdIds={this.props.setSelectedSdIds}
          setMeasurementModeEntries={this.props.setMeasurementModeEntries}
        />
      );
      const y = (clickEvent.position.y as number) + this.containerDomElement.getBoundingClientRect().top;
      const x = (clickEvent.position.x as number) + this.containerDomElement.getBoundingClientRect().left;

      ContextMenu.show(sdMenu, {top: y, left: x});
      this.props.setSelectedSdIds(sdIds);
    }
  }

  /**
   * Handler for map ctrl+click
   */
  private readonly onMapShiftClick = (clickEvent: any, entity?: any) => {
    if (entity && entity.entityType === 'sd') {
      this.props.setSelectedSdIds([...this.props.selectedSdIds, entity.id]);
    }
    if (entity && entity.entityType === 'event') {
      this.props.setSelectedEventIds([...this.props.selectedEventIds, entity.id]);
    }
  }

  /**
   * Handler for map double click
   */
  private readonly onMapDoubleClick = (clickEvent: any, entity?: any) => {
    if (entity && entity.entityType === 'event') {
      // TODO: fix this so completed events can't be opened again
      const filteredList = this.props.eventsInTimeRangeQuery &&
        this.props.eventsInTimeRangeQuery.eventsInTimeRange ?
        this.props.eventsInTimeRangeQuery.eventsInTimeRange.filter(event => event.id === entity.id) : [];
      filteredList.forEach(event => {
        if (event.status !== 'Complete') {
          this.props.setOpenEventId(entity.id);
        }
      });
    }
  }

  /**
   * Selects clicked signal detections
   */
  private readonly selectSignalDetectionsFromProps = (props: MapProps) => {
    this.map.highlightSelectedSignalDetections(props.selectedSdIds);
  }

}
