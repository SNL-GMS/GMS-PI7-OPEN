import {
  Button,
  Icon,
  Intent,
  NonIdealState,
  Spinner,
} from '@blueprintjs/core';
import { Toaster } from '@gms/ui-core-components';
import {
  WeavessTypes
} from '@gms/weavess';
import * as lodash from 'lodash';
import memoizeOne from 'memoize-one';
import * as React from 'react';
import { handleCreatedEvents } from '~analyst-ui/common/subscription-handlers/events-created-handler';
import {
  isPeakTroughInWarning, sortAndOrderSignalDetections
} from '~analyst-ui/common/utils/signal-detection-util';
import { systemConfig, userPreferences } from '~analyst-ui/config';
import { MaskDisplayFilter } from '~analyst-ui/config/user-preferences';
import {
  CommonTypes,
  EventSubscriptions,
  EventTypes,
  QcMaskSubscriptions,
  QcMaskTypes,
  SignalDetectionQueries,
  SignalDetectionSubscriptions,
  SignalDetectionTypes,
  StationTypes,
  WaveformSubscriptions,
  WaveformTypes,
} from '~graphql/';
import { AmplitudeMeasurementValue,
         FeatureMeasurementTypeName,
         InstantMeasurementValue } from '~graphql/signal-detection/types';
import {
  findAmplitudeFeatureMeasurementValue,
  findArrivalTimeFeatureMeasurementValue,
  findPhaseFeatureMeasurementValue
} from '~graphql/signal-detection/utils';
import {
  AnalystActivity,
  Mode,
  TimeInterval,
  WaveformSortType
} from '~state/analyst-workspace/types';
import { addGlUpdateOnResize, addGlUpdateOnShow } from '~util/gl-util';
import { UILogger } from '~util/log/logger';
import { WeavessDisplay } from '../weavess-display';
import { WeavessDisplay as WeavessDisplayComponent } from '../weavess-display/weavess-display-component';
import { WaveformDisplayControls } from './components/waveform-display-controls';
import { DEFAULT_PANNING_PERCENT, ONE_THIRD, TWO_THIRDS_ROUNDED_UP } from './constants';
import { AlignWaveformsOn, PanType, WaveformDisplayProps, WaveformDisplayState } from './types';
import { WaveformClient } from './waveform-client';
import * as WaveformUtil from './weavess-stations-util';

/**
 * Primary waveform display component.
 */
export class WaveformDisplay extends React.PureComponent<WaveformDisplayProps, WaveformDisplayState> {
  /** 2.5 minutes in seconds */
  private static readonly twoHalfMinInSeconds: number = 150;

  /** The waveform client, used to fetch and cache waveforms. */
  public readonly waveformClient: WaveformClient;

  /** Index of currently selected filter */
  private selectedFilterIndex: number = -1;

  /** A ref handle to the waveform display controls component */
  private waveformDisplayControls: WaveformDisplayControls;

  /** A ref handle to the weavess display component */
  private weavessDisplay: WeavessDisplayComponent;

  /** Handlers to unsubscribe from apollo subscriptions */
  private readonly unsubscribeHandlers: { (): void }[] = [];

  /** A Ref to the waveform display div */
  private waveformDisplayRef: HTMLDivElement | undefined;

  /** The toaster reference for user notification pop-ups */
  private readonly toaster: Toaster = new Toaster();

  /**
   * A memoized function for determining the initial zoom range.
   * The memoization function caches the results using 
   * the most recent argument and returns the results. 
   * 
   * @param currentTimeInterval the current time interval
   * @param currentOpenEvent the current open event
   * @param analystActivity the selected analyst activity
   * @param alignWaveformsOn the selected waveform alignment
   * @param phaseToAlignOn the selected phase to align on
   * 
   * @returns a time range
   */
  private readonly memoizedGetInitialZoomWindow: (
    currentTimeInterval: TimeInterval, currentOpenEventId: string, analystActivity: AnalystActivity
    ) => WeavessTypes.TimeRange | undefined;

  // ***************************************
  // BEGIN REACT COMPONENT LIFECYCLE METHODS
  // ***************************************

  /**
   * Constructor.
   * 
   * @param props The initial props
   */
  public constructor(props: WaveformDisplayProps) {
    super(props);
    this.waveformClient = new WaveformClient(this.props.apolloClient);
    this.memoizedGetInitialZoomWindow = memoizeOne(
      this.getInitialZoomWindow,
      /* tell memoize to use a deep comparison for complex objects */
      // tslint:disable-next-line: no-unbound-method
      lodash.isEqual);
    this.state = {
      stations: [],
      loadingWaveforms: false,
      loadingWaveformsPercentComplete: 0,
      maskDisplayFilters: userPreferences.colors.waveforms.maskDisplayFilters,
      analystNumberOfWaveforms: this.props.analystActivity === AnalystActivity.eventRefinement ?
        systemConfig.eventRefinement.numberOfWaveforms :
        systemConfig.eventGlobalScan.numberOfWaveforms,
      showPredictedPhases: false,
      // the range of waveform data displayed initially
      currentTimeInterval: props.currentTimeInterval,
      alignWaveformsOn: AlignWaveformsOn.TIME,
      phaseToAlignOn: undefined,
      isMeasureWindowVisible: false,
      // the total viewable (scrollable) range of waveforms
      viewableInterval: (props.currentTimeInterval) ?
        {
          startTimeSecs: Number(props.currentTimeInterval.startTimeSecs) -
            systemConfig.initialViewableRange(props.analystActivity),
          endTimeSecs: Number(props.currentTimeInterval.endTimeSecs) +
            systemConfig.initialViewableRange(props.analystActivity)
        } : props.currentTimeInterval,
      currentOpenEventId: undefined,
    };
  }

  /**
   * Updates the derived state from the next props.
   * 
   * @param nextProps The next (new) props
   * @param prevState The previous state
   */
  public static getDerivedStateFromProps(
    nextProps: WaveformDisplayProps, prevState: WaveformDisplayState): Partial<WaveformDisplayState> {
    if (
      !lodash.isEqual(nextProps.currentTimeInterval, prevState.currentTimeInterval) ||
      !lodash.isEqual(nextProps.currentOpenEventId, prevState.currentOpenEventId)) {

      const hasTimeIntervalChanged = !lodash.isEqual(nextProps.currentTimeInterval, prevState.currentTimeInterval);

      // update current interval to the selected open interval time
      // reset the interval to the new one, overriding any extra data the user has loaded.
      return {
        stations: hasTimeIntervalChanged ? [] : prevState.stations,
        currentTimeInterval: nextProps.currentTimeInterval,
        viewableInterval: hasTimeIntervalChanged ?
          (nextProps.currentTimeInterval) ?
            {
              startTimeSecs: Number(nextProps.currentTimeInterval.startTimeSecs) -
                systemConfig.initialViewableRange(nextProps.analystActivity),
              endTimeSecs: Number(nextProps.currentTimeInterval.endTimeSecs) +
                systemConfig.initialViewableRange(nextProps.analystActivity)
            } : nextProps.currentTimeInterval
          : prevState.viewableInterval,
        currentOpenEventId: nextProps.currentOpenEventId,
        alignWaveformsOn:
          (nextProps.currentOpenEventId === null || nextProps.currentOpenEventId === '') ?
          AlignWaveformsOn.TIME : prevState.alignWaveformsOn,
        phaseToAlignOn:
          (nextProps.currentOpenEventId === null || nextProps.currentOpenEventId === '') ?
          undefined : prevState.phaseToAlignOn
      };
    }

    // return null to indicate no change to state.
    return null;
  }

  /**
   * Invoked when the componented mounted.
   */
  public componentDidMount() {
    const callback = () => {
      this.forceUpdate();
      if (this.weavessDisplay) {
        this.weavessDisplay.refresh();
      }
    };
    addGlUpdateOnShow(this.props.glContainer, callback);
    addGlUpdateOnResize(this.props.glContainer, callback);
  }

  /**
   * Invoked when the component has rendered.
   * 
   * @param prevProps The previous props
   * @param prevState The previous state
   */
  public componentDidUpdate(prevProps: WaveformDisplayProps, prevState: WaveformDisplayState) {
    if (this.props.currentTimeInterval &&
      !lodash.isEqual(this.props.currentTimeInterval, prevProps.currentTimeInterval)) {
      this.setupSubscriptions();
      this.waveformClient.stopAndClear();
    }

    // Checks the activity, and sets waveforms display amount based on result
    if (this.props.analystActivity !== prevProps.analystActivity) {
      const numWaveforms = this.props.analystActivity === AnalystActivity.eventRefinement ?
        systemConfig.eventRefinement.numberOfWaveforms :
        systemConfig.eventGlobalScan.numberOfWaveforms;
      this.setAnalystNumberOfWaveforms(numWaveforms);
    }
    this.updateWeavessStations();
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
  // tslint:disable-next-line: cyclomatic-complexity
  public render() {

    // ***************************************
    // BEGIN NON-IDEAL STATE CASES
    // ***************************************

    // ! This case must be first
    // if the golden-layout container is not visible, do not attempt to render
    // the compoent, this is to prevent JS errors that may occur when trying to
    // render the component while the golden-layout container is hidden
    if (this.props.glContainer) {
      if (this.props.glContainer.isHidden) {
        return (<NonIdealState />);
      }
    }
    if (!this.props.currentTimeInterval) {
      return (
        <NonIdealState
          visual="timeline-line-chart"
          title="No waveform data currently loaded"
        />);
    }

    if (WaveformUtil.isLoading(this.props, this.state)) {
      const loadingDescription =
        this.props.defaultStationsQuery.loading ? 'Default station set...' :
          // tslint:disable-next-line:max-line-length
          this.state.loadingWaveforms ? `Data for current interval across ${this.props.defaultStationsQuery.defaultStations.length} channels...` :
            this.props.defaultWaveformFiltersQuery.loading ? 'Default filters...' :
              this.props.eventsInTimeRangeQuery.loading ? 'Events...' :
                this.props.signalDetectionsByStationQuery.loading ? 'Signal detections...' :
                  this.props.qcMasksByChannelIdQuery.loading ? 'QC masks...' :
                    'Calculating distance to source';
      return (
        <NonIdealState
          action={
            <Spinner
              intent={Intent.PRIMARY}
              value={(this.state.loadingWaveforms) ? this.state.loadingWaveformsPercentComplete : undefined}
            />
          }
          title="Loading:"
          description={loadingDescription}
        />);
    }

    if (WaveformUtil.isError(this.props)) {
      const errorDescription =
        this.props.defaultStationsQuery.error !== undefined ? this.props.defaultStationsQuery.error :
          // tslint:disable-next-line:max-line-length
          this.props.defaultWaveformFiltersQuery.error !== undefined ? this.props.defaultWaveformFiltersQuery.error :
            this.props.eventsInTimeRangeQuery.error !== undefined ? this.props.eventsInTimeRangeQuery.error :
              // tslint:disable-next-line:max-line-length
              this.props.signalDetectionsByStationQuery.error !== undefined ? this.props.signalDetectionsByStationQuery.error :
                this.props.qcMasksByChannelIdQuery.error !== undefined ? this.props.qcMasksByChannelIdQuery.error :
                  this.props.distanceToSourceForDefaultStationsQuery.error;
      return (
        <NonIdealState
          visual="error"
          action={<Spinner intent={Intent.DANGER} />}
          title="Something went wrong!"
          description={errorDescription}
        />);
    }

    // ***************************************
    // END NON-IDEAL STATE CASES
    // ***************************************

    const stations: WeavessTypes.Station[] =
      this.displayNumberOfWaveforms(WaveformUtil.sortWaveformList(this.state.stations, this.props.waveformSortType));

    const events = this.getWeavessEvents();
    const measureWindowSelection = this.getMeasureWindowSelection();
    const customMeasureWindowLabel: React.StatelessComponent<WeavessTypes.LabelProps> =
      this.getCustomMeasureWindowLabel();

    return (
      <div
        ref={ref => this.waveformDisplayRef = ref}
        className={'waveform-display-window'}
      >
        <div className={'waveform-display-container'}>
          <WaveformDisplayControls
            ref={ref => {
              if (ref) {
                this.waveformDisplayControls = ref;
              }
            }}
            createSignalDetectionPhase={this.props.createSignalDetectionPhase}
            currentSortType={this.props.waveformSortType}
            currentOpenEventId={this.props.currentOpenEventId}
            analystNumberOfWaveforms={this.state.analystNumberOfWaveforms}
            showPredictedPhases={this.state.showPredictedPhases}
            maskDisplayFilters={this.state.maskDisplayFilters}
            alignwaveFormsOn={this.state.alignWaveformsOn}
            phaseToAlignOn={this.state.phaseToAlignOn}
            alignablePhases={WaveformUtil.getAlignablePhases(this.props)}
            glContainer={this.props.glContainer}
            measurementMode={this.props.measurementMode}
            setCreateSignalDetectionPhase={this.props.setCreateSignalDetectionPhase}
            setWaveformAlignment={this.setWaveformAlignment}
            setSelectedSortType={this.props.setSelectedSortType}
            setAnalystNumberOfWaveforms={this.setAnalystNumberOfWaveforms}
            setMaskDisplayFilters={this.setMaskDisplayFilters}
            setShowPredictedPhases={this.setShowPredictedPhases}
            setMode={(mode: Mode) => this.setMode(mode)}
            toggleMeasureWindow={this.toggleMeasureWindowVisability}
            pan={this.pan}
            onKeyPress={this.onKeyPress}
            isMeasureWindowVisible={this.state.isMeasureWindowVisible}
          />
          <WeavessDisplay
              ref={(ref: any) => {
                if (ref) {
                  let componentRef = ref;
                  // get the `wrapped` component reference; uses `{ withRef: true }`
                  while (componentRef && !(componentRef instanceof WeavessDisplayComponent)) {
                    componentRef = componentRef.getWrappedInstance();
                  }
                  this.weavessDisplay = componentRef;
                }
              }}
              weavessProps={{
                startTimeSecs: this.state.viewableInterval.startTimeSecs,
                endTimeSecs: this.state.viewableInterval.endTimeSecs,
                initialZoomWindow: this.memoizedGetInitialZoomWindow(
                  this.props.currentTimeInterval, this.props.currentOpenEventId, this.props.analystActivity),
                defaultZoomWindow: {
                  startTimeSecs:  this.props.currentTimeInterval.startTimeSecs,
                  endTimeSecs: this.props.currentTimeInterval.endTimeSecs
                },
                stations,
                measureWindowSelection,
                events,
                configuration: {
                  customMeasureWindowLabel
                }
              }}
              defaultWaveformFilters={
                this.props.defaultWaveformFiltersQuery ?
                  this.props.defaultWaveformFiltersQuery.defaultWaveformFilters : []}
              defaultStations={
                this.props.defaultStationsQuery ?
                  this.props.defaultStationsQuery.defaultStations : []
              }
              eventsInTimeRange={
                this.props.eventsInTimeRangeQuery ?
                  this.props.eventsInTimeRangeQuery.eventsInTimeRange : []
              }
              signalDetectionsByStation={
                this.props.signalDetectionsByStationQuery ?
                  this.props.signalDetectionsByStationQuery.signalDetectionsByStation : []}
              qcMasksByChannelId={
                this.props.qcMasksByChannelIdQuery ?
                  this.props.qcMasksByChannelIdQuery.qcMasksByChannelId : []
              }
          />
        </div>
      </div>
    );
  }

  // ***************************************
  // END REACT COMPONENT LIFECYCLE METHODS
  // ***************************************

  /**
   * Returns the current open event.
   */
  private readonly currentOpenEvent = (): EventTypes.Event =>
    (this.props.eventsInTimeRangeQuery.eventsInTimeRange) ?
    this.props.eventsInTimeRangeQuery.eventsInTimeRange
      .find(e => e.id === this.props.currentOpenEventId) : undefined

  /**
   * Returns the weavess event handler configuration.
   * 
   * @returns the events
   */
  private readonly getWeavessEvents = (): WeavessTypes.Events => {
    const channelEvents: WeavessTypes.ChannelEvents = {
      labelEvents: {
        onChannelExpanded: this.onChannelExpanded,
      },
      events: {
        onMeasureWindowUpdated: this.onMeasureWindowUpdated
      },
      onKeyPress: this.onKeyPress
    };

    return {
      stationEvents: {
        defaultChannelEvents: channelEvents,
        nonDefaultChannelEvents: channelEvents
      }
    };
  }

  /**
   * Returns the measure window selection based on the current `mode` and
   * the selected signal detection.
   * 
   * @returns returns the measure window selection
   */
  private readonly getMeasureWindowSelection = (): WeavessTypes.MeasureWindowSelection => {
    let measureWindowSelection: WeavessTypes.MeasureWindowSelection;
    if (this.props.measurementMode.mode === Mode.MEASUREMENT && this.props.selectedSdIds.length === 1) {
      const signalDetection: SignalDetectionTypes.SignalDetection =
        this.props.signalDetectionsByStationQuery.signalDetectionsByStation
          .find(sd => sd.id === this.props.selectedSdIds[0]);

      if (signalDetection) {
        const station =
          this.state.stations.find(s => s.defaultChannel.id === signalDetection.station.id);

        const stationContainsSd: boolean =
          this.props.signalDetectionsByStationQuery.signalDetectionsByStation
            .find(s => s.id === signalDetection.id) !== undefined;

        if (station && stationContainsSd) {
          const arrivalTime =
            findArrivalTimeFeatureMeasurementValue(signalDetection.currentHypothesis.featureMeasurements).value;

          measureWindowSelection = {
            stationId: station.id,
            channel: {
              ...station.defaultChannel,
              waveform: {
                ...station.defaultChannel.waveform,
                markers: {
                  ...station.defaultChannel.markers,
                  // only show the selection windows for the selected signal detection
                  selectionWindows: station.defaultChannel.waveform.markers &&
                    station.defaultChannel.waveform.markers.selectionWindows ?
                    station.defaultChannel.waveform.markers.selectionWindows
                      .filter(selection => selection.id.includes(this.props.selectedSdIds[0])) :
                    undefined
                }
              }
            },
            startTimeSecs: arrivalTime +
              systemConfig.measurementMode.displayTimeRange.startTimeOffsetFromSignalDetection,
            endTimeSecs: arrivalTime +
              systemConfig.measurementMode.displayTimeRange.endTimeOffsetFromSignalDetection,
            isDefaultChannel: true,
          };
        }
      }
    }
    return measureWindowSelection;
  }

  /**
   * Returns a custom measure window lobel for measurement mode.
   * 
   * @returns a custom measure window label
   */
  private readonly getCustomMeasureWindowLabel = (): React.StatelessComponent<WeavessTypes.LabelProps> =>
    this.props.measurementMode.mode === Mode.MEASUREMENT ? (props: WeavessTypes.LabelProps) => {
      const sdId = (this.props.signalDetectionsByStationQuery && this.props.selectedSdIds.length === 1) ?
        this.props.signalDetectionsByStationQuery.signalDetectionsByStation
        .map(s => s.id)
        .find(id => id === this.props.selectedSdIds[0]) : undefined;

      const sd = (sdId) ? this.props.signalDetectionsByStationQuery.signalDetectionsByStation
        .find(s => s.id === sdId) : undefined;

      const amplitudeMeasurementValue: AmplitudeMeasurementValue | undefined = (sd) ?
        findAmplitudeFeatureMeasurementValue(sd.currentHypothesis.featureMeasurements) : undefined;

      if (!sd) {
        return (
          <React.Fragment>
            {props.channel.name}
          </React.Fragment>);
      }

      const arrivalTime: number =
        findArrivalTimeFeatureMeasurementValue(sd.currentHypothesis.featureMeasurements).value;

      let amplitude: number;
      let period: number;
      let troughTime: number;
      let peakTime: number;
      let isWarning = true;

      if (amplitudeMeasurementValue) {
        amplitude = amplitudeMeasurementValue.amplitude.value;
        period = amplitudeMeasurementValue.period;
        troughTime = amplitudeMeasurementValue.startTime;
        peakTime = troughTime + (period / 2); // display only period/2
        isWarning = isPeakTroughInWarning(arrivalTime, period, troughTime, peakTime);
      }

      const amplitudeTitle = amplitudeMeasurementValue ?
        'Amplitude value' : 'Error: No measurement value available for amplitude';

      const periodTitle = amplitudeMeasurementValue ?
        !isWarning ? 'Period value' : `Warning: Period value must be between` +
        `[${systemConfig.measurementMode.peakTroughSelection.warning.min} - ` +
        `${systemConfig.measurementMode.peakTroughSelection.warning.max}]'` :
        'Error: No measurement value available for period';

      return (
        <React.Fragment>
          {props.channel.name}
          <React.Fragment>
            <br/>
            <div
              title={amplitudeTitle}
              style={{whiteSpace: 'nowrap'}}
            >
              A5/2:&nbsp;
              {amplitudeMeasurementValue ?
                // tslint:disable-next-line: no-magic-numbers
                amplitude.toFixed(3) :
                <Icon
                  title={amplitudeTitle}
                  icon="error"
                  intent={Intent.DANGER}
                />
              }
            </div>
            <div
              title={periodTitle}
              style={{whiteSpace: 'nowrap'}}
            >
              Period:
              {amplitudeMeasurementValue ?
                <span
                  title={periodTitle}
                  style={{
                    color: isWarning ?
                      systemConfig.measurementMode.peakTroughSelection.warning.textColor : undefined
                  }}
                > {amplitudeMeasurementValue.period.toFixed(3)}s&nbsp;
                  {isWarning ?
                    <Icon
                      title={periodTitle}
                      icon="warning-sign"
                      color={systemConfig.measurementMode.peakTroughSelection.warning.textColor}
                    /> :
                    undefined
                  }
                </span> :
                <Icon
                  title={periodTitle}
                  icon="error"
                  intent={Intent.DANGER}
                />
              }
            </div>
            {
              (
                <Button
                  small={true}
                  text="Next"
                  onClick={(event: React.MouseEvent<HTMLElement>) => {
                    event.stopPropagation();
                    this.selectNextAmplitudeMeasurement(sd.id);
                  }}
                />
              )
            }
          </React.Fragment>
        </React.Fragment>
      );
    } : undefined

  /**
   * Returns the initial zoom window time range.
   * 
   * @param currentTimeInterval the current time interval
   * @param currentOpenEvent the current open event
   * @param analystActivity the selected analyst activity
   * 
   * @returns a time range
   */
  private readonly getInitialZoomWindow = (
    currentTimeInterval: TimeInterval,
    currentOpenEventId: string, analystActivity: AnalystActivity
  ): WeavessTypes.TimeRange | undefined => {
    let initialZoomWindow = (this.weavessDisplay) ? this.weavessDisplay.getCurrentViewRangeInSeconds() : undefined;
    const currentOpenEvent = this.currentOpenEvent();
    if (currentOpenEvent && analystActivity === AnalystActivity.eventRefinement) {
      const hypothesis = currentOpenEvent.currentEventHypothesis.eventHypothesis;
      if (hypothesis.signalDetectionAssociations && hypothesis.signalDetectionAssociations.length > 0) {
        const paddingSecs = 60;
        initialZoomWindow = {
          startTimeSecs: hypothesis.preferredLocationSolution.locationSolution.location.time,
          endTimeSecs: hypothesis.associationsMaxArrivalTime + paddingSecs
        };
      }
    } else if (analystActivity === AnalystActivity.globalScan) {
      initialZoomWindow = {
        startTimeSecs: currentTimeInterval.startTimeSecs - WaveformDisplay.twoHalfMinInSeconds,
        endTimeSecs: currentTimeInterval.startTimeSecs + WaveformDisplay.twoHalfMinInSeconds
      };
    }

    return initialZoomWindow;
  }

  /**
   * Sets the mode.
   *
   * @param mode the mode configuration to set
   */
  private readonly setMode = (mode: Mode) => {
    this.props.setMode(mode);

    // auto select the first signal detection if switching to MEASUREMENT mode
    if (mode === Mode.MEASUREMENT) {
      const currentOpenEvent = this.currentOpenEvent();

      if (currentOpenEvent) {
        const associatedSignalDetectionHypothesisIds =
          currentOpenEvent.currentEventHypothesis.eventHypothesis.signalDetectionAssociations
            .map(association => association.signalDetectionHypothesis.id);

        const signalDetections = this.props.signalDetectionsByStationQuery.signalDetectionsByStation
          .filter(sd => {
            const phase =
              findPhaseFeatureMeasurementValue(sd.currentHypothesis.featureMeasurements).phase;
            // return if associated and a measurement phase
            return (lodash.includes(associatedSignalDetectionHypothesisIds, sd.currentHypothesis.id) &&
              lodash.includes(systemConfig.measurementMode.phases, phase));
          });

        if (signalDetections.length > 0) {
          // sort the signal detections
          const sortedEntries = sortAndOrderSignalDetections(
            signalDetections,
            this.props.waveformSortType,
            this.props.distanceToSourceForDefaultStationsQuery.distanceToSourceForDefaultStations);
          this.props.setSelectedSdIds([sortedEntries[0].id]);
          // mark the measure window as being visible; measurement mode auto shows the measure window
          this.setState({isMeasureWindowVisible: true});
        } else {
          this.props.setSelectedSdIds([]);
        }
      }
    } else {
      // leaving measurement mode; mark the measurement window as not visible
      this.setState({isMeasureWindowVisible: false});
    }
  }

  /**
   * Initialize and setup the graphql subscriptions on the apollo client.
   */
  private readonly setupSubscriptions = () => {
    // Unsubscribe from all current subscriptions
    this.unsubscribeHandlers.forEach(unsubscribe => unsubscribe());
    this.unsubscribeHandlers.length = 0;

    // Don't register subscriptions if the current time interval is undefined/null
    if (!this.props.currentTimeInterval) return;

    this.unsubscribeHandlers.push(
      this.props.defaultStationsQuery.subscribeToMore({
        document: WaveformSubscriptions.waveformSegmentsAddedSubscription,
        updateQuery: (prev: { defaultStations: StationTypes.ProcessingStation }, cur) => {
          const currentInterval = this.state.viewableInterval;
          const data = cur.subscriptionData.data as WaveformTypes.WaveformSegmentsAddedSubscription;
          // For each newly-added waveform channel segment received via subscription...
          data.waveformChannelSegmentsAdded.forEach(segmentAdded => {
            // If the new segment overlaps the current interval,
            // Retrieve the waveform samples for the segment
            if (segmentAdded.startTime < currentInterval.endTimeSecs
              && segmentAdded.endTime > currentInterval.startTimeSecs) {

              const filterIds = this.props.defaultWaveformFiltersQuery
                .defaultWaveformFilters.map(filters => filters.id);

              this.waveformClient.fetchAndCacheWaveforms(
                [segmentAdded.channel.id],
                filterIds,
                Math.max(segmentAdded.startTime, currentInterval.startTimeSecs),
                Math.min(segmentAdded.endTime, currentInterval.endTimeSecs),
                () => {
                  this.updateWaveformState();
                },
                () => {
                  this.updateWeavessStations();
                },
                1
              )
                .catch(e => {
                  // tslint:disable-next-line:no-console
                  console.error(e);
                  window.alert(e);
                });
            }
          });

          return prev;
        }
      })
    );
    this.unsubscribeHandlers.push(
      this.props.signalDetectionsByStationQuery.subscribeToMore({
        document: SignalDetectionSubscriptions.detectionsCreatedSubscription,
        updateQuery: (prev: { signalDetectionsByStation: SignalDetectionTypes.SignalDetection[] }, cur) => {
          const data = cur.subscriptionData.data as SignalDetectionTypes.DetectionsCreatedSubscription;

          if (data) {
            // merge the new signal detection into the appropriate place in the current data.
            // most of this work is done to avoid mutating any data
            const prevSignalDetections = prev.signalDetectionsByStation;
            const newSignalDetections = (prevSignalDetections) ? [...prevSignalDetections] : [];
            data.detectionsCreated.forEach(detectionCreated => {
              if (newSignalDetections.findIndex(sd => sd.id === detectionCreated.id) < 0) {
                newSignalDetections.push(detectionCreated);
              }
            });
            return {
              ...prev,
              signalDetectionsByStation: newSignalDetections
            };
          }
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
    this.unsubscribeHandlers.push(
      this.props.qcMasksByChannelIdQuery.subscribeToMore({
        document: QcMaskSubscriptions.qcMasksCreatedSubscription,
        updateQuery: (prev: { qcMasksByChannelId: QcMaskTypes.QcMask[] }, cur) => {
          const data = cur.subscriptionData.data as QcMaskTypes.QcMasksCreatedSubscription;

          if (data) {
            // Merge the new signal detection into the appropriate place in the current data.
            // most of this work is done to avoid mutating any data
            const prevQcMasks = prev.qcMasksByChannelId;
            const newQcMasks = (prevQcMasks) ? [...prevQcMasks] : [];
            data.qcMasksCreated.forEach(maskCreated => {
              // If the newly created detection is outside the current interval, don't add it
              if (maskCreated.currentVersion.startTime < this.state.viewableInterval.endTimeSecs
                && maskCreated.currentVersion.endTime > this.state.viewableInterval.startTimeSecs
                && newQcMasks.findIndex(mask => mask.id === maskCreated.id) < 0) {
                newQcMasks.push(maskCreated);
              }
            });
            return {
              qcMasksByChannelId: newQcMasks
            };
          }
        }
      })
    );
  }

  /**
   * Updates the waveform state on the controls.
   */
  private readonly updateWaveformState = () => {
    if (this.waveformDisplayControls) {
      this.waveformDisplayControls.setState({
        waveformState: this.waveformClient.state
      });
    }
  }

  /**
   * Load waveform data outside the current interval.
   * Assumes data has already been loaded, and the waveform cache has entries.
   * 
   * @param startTimeSecs the start time seconds the time range to load
   * @param endTimeSecs the end time seconds of the time range to load
   */
  private readonly fetchDataOutsideInterval =
    async (startTimeSecs: number, endTimeSecs: number): Promise<void> => {
      const channelIds = this.waveformClient.getWaveformChannelIds();
      const filterIds = this.props.defaultWaveformFiltersQuery.defaultWaveformFilters.map(filters => filters.id);

      // Retrieve waveform sample data for the channel IDs and input time range, adding the waveforms to the cache
      this.fetchSignalDetectionsOutsideInterval(startTimeSecs, endTimeSecs);
      return this.waveformClient.fetchAndCacheWaveforms(
        channelIds,
        filterIds,
        startTimeSecs,
        endTimeSecs,
        () => {
          this.updateWaveformState();
        },
        () => {
          this.setState({
            viewableInterval: {
              startTimeSecs: Math.min(this.state.viewableInterval.startTimeSecs, startTimeSecs),
              endTimeSecs: Math.max(this.state.viewableInterval.endTimeSecs, endTimeSecs)
            }
          });
        });
    }

  /**
   * Load signal detections outside the current interval.
   * 
   * @param startTimeSecs the start time seconds the time range to load
   * @param endTimeSecs the end time seconds of the time range to load
   */
  private readonly fetchSignalDetectionsOutsideInterval = (
    startTimeSecs: number,
    endTimeSecs: number) => {
    const variables: SignalDetectionTypes.SignalDetectionsByStationQueryArgs = {
      stationIds: this.props.defaultStationsQuery.defaultStations.map(station => station.id),
      timeRange: {
        startTime: startTimeSecs,
        endTime: endTimeSecs
      },
    };
    this.props.signalDetectionsByStationQuery.fetchMore({
      query: SignalDetectionQueries.signalDetectionsByStationQuery,
      variables,
      updateQuery: (prev: { signalDetectionsByStation: SignalDetectionTypes.SignalDetection[] }, cur) => {
        const data = cur.fetchMoreResult as SignalDetectionTypes.SignalDetection[];
        const prevSignalDetections = prev.signalDetectionsByStation;
        const newSignalDetections = [...prevSignalDetections];
        if (data.length > 0) {
          data.forEach(signalDetection => {
            // Add the new signal detections
            if (!prevSignalDetections.find(sd => sd.id === signalDetection.id)) {
              newSignalDetections.push(signalDetection);
            }
          });
        }
        return {
          ...prev,
          signalDetectionsByStation: newSignalDetections
        };
      }
    })
      .catch();
  }

  /**
   * Updates the weavess stations based on the current state and props.
   */
  private readonly updateWeavessStations = () => {
    if (!this.props.currentTimeInterval ||
      !this.props.defaultStationsQuery || !this.props.defaultStationsQuery ||
      !this.props.defaultWaveformFiltersQuery ||
      !this.props.defaultWaveformFiltersQuery.defaultWaveformFilters) return;
    const stationHeight = this.calculateStationHeight();
    const createWeavessStationsParameters = WaveformUtil.populateCreateWeavessStationsParameters(
      this.props, this.state, stationHeight, this.waveformClient);
    const weavessStations = WaveformUtil.createWeavessStations(createWeavessStationsParameters);
    if (!lodash.isEqual(this.state.stations, weavessStations)) {
      this.setState({
        stations: weavessStations
      });
    }
  }

  /**
   * Toggle the measure window visability within weavess.
   */
  private readonly toggleMeasureWindowVisability = () => {
    if (this.weavessDisplay) {
      this.weavessDisplay.toggleMeasureWindowVisability();
    }
  }

  /**
   * Event handler for channel expansion 
   * 
   * @param channelId a Channel Id as a string
   */
  private readonly onChannelExpanded = (channelId: string) => {
    // Get the ids of all sub-channels
    const subChannelIds: string[] = lodash.flattenDeep<string>(
      this.props.defaultStationsQuery.defaultStations.find(station => station.id === channelId)
        .sites
        .map(site => site.channels.map(channel => channel.id)));

    // Check if there are any new channel IDs whose waveform data we haven't already cached.
    const channelIdsToFetchAndCache =
      lodash.difference(subChannelIds, this.waveformClient.getWaveformChannelIds());
    if (channelIdsToFetchAndCache && channelIdsToFetchAndCache.length > 0) {
      const filterIds = this.props.defaultWaveformFiltersQuery.defaultWaveformFilters.map(filters => filters.id);
      this.waveformClient.fetchAndCacheWaveforms(
        channelIdsToFetchAndCache,
        filterIds,
        this.state.viewableInterval.startTimeSecs,
        this.state.viewableInterval.endTimeSecs,
        () => {
          this.updateWaveformState();
        },
        () => {
          this.updateWeavessStations();
        })
        .catch(e => {
          // tslint:disable-next-line:no-console
          console.error(e);
          window.alert(e);
        });
    }
  }

  /**
   * Event handler that is invoked and handled when the Measure Window is updated.
   * 
   * @param isVisible true if the measure window is updated
   * @param channelId the unique channel id of the channel that the measure window on; 
   * channel id is undefined if the measure window is not visible
   * @param startTimeSecs the start time in seconds of the measure window; 
   * start time seconds is undefined if the measure window is not visible
   * @param endTimeSecs the end time in seconds of the measure window; 
   * end time seconds is undefined if the measure window is not visible
   * @param heightPx the height in pixels of the measure window; 
   * height pixels is undefined if the measure window is not visible
   */
  private readonly onMeasureWindowUpdated = (
    isVisible: boolean, channelId?: string, startTimeSecs?: number, endTimeSecs?: number, heightPx?: number) => {
    this.setState({ isMeasureWindowVisible: isVisible });
  }

  /**
   * Event handler for when a key is pressed
   * 
   * @param e mouse event as React.MouseEvent<HTMLDivElement>
   * @param clientX x location of where the key was pressed
   * @param clientY y location of where the key was pressed
   * @param channelId a Channel Id as a string
   * @param timeSecs epoch seconds of where the key was pressed in respect to the data
   */
  private readonly onKeyPress = (e: React.KeyboardEvent<HTMLDivElement>,
    clientX: number, clientY: number,
    channelId: string, timeSecs: number) => {
    // handle the default WEAVESS onKeyPressEvents
    if (this.weavessDisplay) {
      this.weavessDisplay.onKeyPress(e, clientX, clientY, channelId, timeSecs);
    }

    if (e.key === 'Escape') {
      this.selectedFilterIndex = -1;
    } else if (e.altKey) {
      switch (e.nativeEvent.code) {
        case 'KeyN':
          this.selectNextAmplitudeMeasurement(this.props.selectedSdIds[0]);
          break;
        case 'KeyP':
          if (this.props.currentOpenEventId) {
            if (this.state.alignWaveformsOn === AlignWaveformsOn.TIME) {
              this.setWaveformAlignment(
                AlignWaveformsOn.PREDICTED_PHASE, CommonTypes.PhaseType.P);
            } else {
              this.setWaveformAlignment(AlignWaveformsOn.TIME);
            }
          } else {
            this.toaster.toastInfo('Open an event to change waveform alignment');
          }
          break;
        case 'KeyA':
          if (this.waveformDisplayControls) {
            this.waveformDisplayControls.toggleAlignmentDropdown();
          }
          break;
        default:
          return;
      }
    } else if (e.ctrlKey || e.metaKey) {
      switch (e.key) {
        case '-':
          this.setAnalystNumberOfWaveforms(this.state.analystNumberOfWaveforms + 1);
          return;
        case '=':
          this.setAnalystNumberOfWaveforms(this.state.analystNumberOfWaveforms - 1);
          return;
        case 'ArrowLeft':
          // tslint:disable-next-line:no-floating-promises
          this.pan(PanType.Left);
          e.preventDefault();
          return;
        case 'ArrowRight':
          // tslint:disable-next-line:no-floating-promises
          this.pan(PanType.Right);
          e.preventDefault();
          return;
        default:
          this.onHandleToggleFilters(e, clientX, clientY, channelId, timeSecs);
      }
    }
  }

  /**
   * Event handler for when a key is pressed
   * 
   * @param e mouse event as React.MouseEvent<HTMLDivElement>
   * @param clientX x location of where the key was pressed
   * @param clientY y location of where the key was pressed
   * @param channelId a Channel Id as a string
   * @param timeSecs epoch seconds of where the key was pressed in respect to the data
   */
  private readonly onHandleToggleFilters = (e: React.KeyboardEvent<HTMLDivElement>,
    clientX: number, clientY: number,
    channelId: string, timeSecs: number) => {
    // Cycle through Filters
    if (e.nativeEvent.code === 'ArrowUp' || e.nativeEvent.code === 'ArrowDown') {
      e.preventDefault();
      if (this.weavessDisplay) {
        const filterNames = lodash.uniq(this.props.defaultWaveformFiltersQuery.
          defaultWaveformFilters.map(filt => filt.name));

        if (filterNames.length > 0) {
          const waveformFilterLength = filterNames.length;
          let channelFilters = this.props.channelFilters;
          this.selectedFilterIndex = (e.nativeEvent.code === 'ArrowUp') ?
            this.selectedFilterIndex + 1 : this.selectedFilterIndex - 1;
          if (this.selectedFilterIndex >= waveformFilterLength) this.selectedFilterIndex = -1;
          if (this.selectedFilterIndex < -1) this.selectedFilterIndex = waveformFilterLength - 1;

          if (this.weavessDisplay.state.selectedChannels !== undefined &&
            this.weavessDisplay.state.selectedChannels.length > 0) {
            // for every id check to see if a default station matches
            // if none match, add to id list for every channel
            this.weavessDisplay.state.selectedChannels.forEach(selectedId => {
              this.props.defaultStationsQuery.defaultStations.forEach(station => {
                // if the selected id is a default station,
                // set the filter on all of its non-default stations
                if (station.id === selectedId) {
                  channelFilters = channelFilters.set(
                    station.id,
                    // tslint:disable-next-line:max-line-length
                    WaveformUtil.findWaveformFilter(
                      station.defaultChannel.sampleRate,
                      this.selectedFilterIndex, filterNames,
                      this.props.defaultWaveformFiltersQuery.defaultWaveformFilters));
                } else {
                  // check each station's child channels to see if the id
                  // matches one of them, if so, apply
                  lodash.flatMap(station.sites, site => site.channels.map(childChannel => {
                    if (childChannel.id === selectedId) {
                      channelFilters = channelFilters.set(
                        childChannel.id,
                        WaveformUtil
                          .findWaveformFilter(
                            childChannel.sampleRate,
                            this.selectedFilterIndex, filterNames,
                            this.props.defaultWaveformFiltersQuery.defaultWaveformFilters
                          ));
                    }
                  }));
                }
              });
            });
          } else {
            // no selected channels, apply filter to all
            this.props.defaultStationsQuery.defaultStations.forEach(station => {
              channelFilters = channelFilters.set(
                station.id,
                WaveformUtil
                  .findWaveformFilter(
                    station.defaultChannel.sampleRate,
                    this.selectedFilterIndex, filterNames,
                    this.props.defaultWaveformFiltersQuery.defaultWaveformFilters));
            });
          }
          this.props.setChannelFilters(channelFilters);
        }
      }
    }
  }

  /**
   * Set the mask filters selected in the qc mask legend.
   * 
   * @param key the unique key identifier
   * @param maskDisplayFilter the mask display filter
   */
  private readonly setMaskDisplayFilters = (key: string, maskDisplayFilter: MaskDisplayFilter) => {
    this.setState(
      {
        maskDisplayFilters: {
          ...this.state.maskDisplayFilters,
          [key]: maskDisplayFilter
        }
      },
      () => {
        this.updateWeavessStations();
      }
    );
  }

  /**
   * Select the next amplitude measurement when in measurement mode
   * 
   * @param signalDetectionId current selected signal detection Id
   */
  private readonly selectNextAmplitudeMeasurement = (signalDetectionId: string): void => {
    if (this.props.measurementMode.mode !== Mode.MEASUREMENT) return;

    const currentOpenEvent = this.currentOpenEvent();
    if (currentOpenEvent) {
      const associatedSignalDetectionHypothesisIds =
        currentOpenEvent.currentEventHypothesis.eventHypothesis.signalDetectionAssociations
          .map(association => association.signalDetectionHypothesis.id);

      const stationIds = this.state.stations.map(station => station.id);

      // get all of the signal dections for the viewable stations
      const signalDetections = this.props.signalDetectionsByStationQuery.signalDetectionsByStation
        .filter(sd => lodash.includes(stationIds, sd.station.id));

      // sort the signal detections
      const sortedEntries = sortAndOrderSignalDetections(
        signalDetections,
        this.props.waveformSortType,
        this.props.distanceToSourceForDefaultStationsQuery.distanceToSourceForDefaultStations);

      let nextSignalDetectionToSelect: SignalDetectionTypes.SignalDetection;
      if (sortedEntries.length > 0) {
        let index = sortedEntries.findIndex(sd => sd.id === signalDetectionId) + 1;
        if (index >= sortedEntries.length) {
            index = 0;
        }

        const isAssociatedSdAndInPhaseList = (sd: SignalDetectionTypes.SignalDetection) => {
          const phase =
            findPhaseFeatureMeasurementValue(sd.currentHypothesis.featureMeasurements).phase;
          return lodash.includes(associatedSignalDetectionHypothesisIds, sd.currentHypothesis.id) &&
            lodash.includes(systemConfig.measurementMode.phases, phase);
        };

        // ensure that the selected index is for an associated signal detection and in the
        // list of phase measurements; increment until start searching from the current index found above
        nextSignalDetectionToSelect = lodash.find(sortedEntries, isAssociatedSdAndInPhaseList, index);

        // if the signal detection id is undefined, continue searching, but at index 0
        if (!nextSignalDetectionToSelect) {
            nextSignalDetectionToSelect = lodash.find(sortedEntries, isAssociatedSdAndInPhaseList);
        }
      }
      this.props.setSelectedSdIds([nextSignalDetectionToSelect.id]);
    }
  }

  /**
   * Display the number of waveforms choosen by the analyst
   * Also updates the state variable holding the selection
   */
  private readonly displayNumberOfWaveforms = (stations: WeavessTypes.Station[]): WeavessTypes.Station[] => {
    const height = this.calculateStationHeight();

    stations.forEach(station => {
      station.defaultChannel.height = height;
      if (station.nonDefaultChannels) {
        station.nonDefaultChannels.forEach(ndc => ndc.height = height);
      }
    });
    return stations;
  }

  /**
   * Calculate height for the station based of number of display
   */
  private readonly calculateStationHeight = (): number => {
    const waveformDisplayButtonsAndAxisHeightPx = 100;
    return (!this.waveformDisplayRef || !this.waveformDisplayRef.clientHeight) ?
      systemConfig.defaultWeavessConfiguration.stationHeightPx :
      // tslint:disable-next-line:max-line-length
      (this.waveformDisplayRef.clientHeight - waveformDisplayButtonsAndAxisHeightPx) / this.state.analystNumberOfWaveforms;
  }

  /**
   * Sets the waveform alignment and adjust the sort type if necessary.
   * 
   * @param alignWaveformsOn the waveform alignment setting
   * @param phaseToAlignOn the phase to align on
   */
  private readonly setWaveformAlignment = (
    alignWaveformsOn: AlignWaveformsOn, phaseToAlignOn?: CommonTypes.PhaseType) => {
    if (alignWaveformsOn !== AlignWaveformsOn.TIME) {
      this.setState({ alignWaveformsOn, phaseToAlignOn, showPredictedPhases: true });
      this.props.setSelectedSortType(WaveformSortType.distance);
    } else {
      this.setState({ alignWaveformsOn, phaseToAlignOn });
    }
    // adjust the zoom time window for the selected alignment
    this.zoomToTimeWindowForAlignnment(alignWaveformsOn, phaseToAlignOn);
  }

  /**
   * Sets the waveform alignment zoom time window for the given alignment setting.
   * 
   * @param alignWaveformsOn the waveform alignment setting
   * @param phaseToAlignOn the phase to align on
   */
  private readonly zoomToTimeWindowForAlignnment = (
    alignWaveformsOn: AlignWaveformsOn, phaseToAlignOn?: CommonTypes.PhaseType) => {
    if (this.weavessDisplay) {
      if (alignWaveformsOn !== AlignWaveformsOn.TIME) {
        const predictedPhases = WaveformUtil.getFeaturePredictionsForOpenEvent(this.props)
          .filter(fp => fp.phase === phaseToAlignOn
            && fp.predictionType === FeatureMeasurementTypeName.ARRIVAL_TIME);
        if (predictedPhases && predictedPhases.length > 0) {
          predictedPhases.sort((a, b) => {
            const aValue = (a.predictedValue as InstantMeasurementValue).value;
            const bValue = (b.predictedValue as InstantMeasurementValue).value;
            return aValue - bValue;
          });
          const earliestTime = (predictedPhases[0].predictedValue as InstantMeasurementValue).value;
          const prevZoomInterval = this.weavessDisplay.getCurrentViewRangeInSeconds();
          const range = prevZoomInterval.endTimeSecs - prevZoomInterval.startTimeSecs;
          const initialZoomWindow = {
            startTimeSecs: earliestTime - (range * ONE_THIRD),
            endTimeSecs: earliestTime + (range * TWO_THIRDS_ROUNDED_UP)
          };
          this.weavessDisplay.zoomToTimeWindow(initialZoomWindow.startTimeSecs, initialZoomWindow.endTimeSecs);
        }
      }
    }
  }

  /**
   * Sets the number of waveforms to be displayed.
   * 
   * @param value the number of waveforms to display (number)
   * @param valueAsString the number of waveforms to display (string)
   */
  private readonly setAnalystNumberOfWaveforms = (value: number, valueAsString?: string) => {
    const base = 10;
    let analystNumberOfWaveforms = value;

    if (valueAsString) {
      // tslint:disable-next-line:no-parameter-reassignment
      valueAsString = valueAsString.replace(/e|\+|-/, '');
      analystNumberOfWaveforms = isNaN(parseInt(valueAsString, base)) ?
        this.state.analystNumberOfWaveforms : parseInt(valueAsString, base);
    }

    // Minimum number of waveforms must be 1
    if (analystNumberOfWaveforms < 1) {
      analystNumberOfWaveforms = 1;
    }

    if (this.state.analystNumberOfWaveforms !== analystNumberOfWaveforms) {
      this.setState({
        analystNumberOfWaveforms
      });
    }
  }

  /**
   * Sets the show predicted phases state.
   * 
   * @param showPredictedPhases if true shows predicted phases; false otherwise
   */
  private readonly setShowPredictedPhases = (showPredictedPhases: boolean) =>
    this.setState({ showPredictedPhases })

  /**
   * Pan the waveform display.
   * 
   * @param panDirection the pan direction
   */
  private readonly pan = async (panDirection: PanType) => {
    if (this.weavessDisplay) {
      const currentWeavessViewRange: WeavessTypes.TimeRange = this.weavessDisplay.getCurrentViewRangeInSeconds();
      const interval: number = Math.abs(currentWeavessViewRange.endTimeSecs - currentWeavessViewRange.startTimeSecs);
      const timeToPanBy: number = Math.ceil(interval * DEFAULT_PANNING_PERCENT);

      const pannedViewTimeInterval: WeavessTypes.TimeRange = (panDirection === PanType.Left) ?
        {
          startTimeSecs: Number(currentWeavessViewRange.startTimeSecs) - timeToPanBy,
          endTimeSecs: Number(currentWeavessViewRange.endTimeSecs) - timeToPanBy
        } :
        {
          startTimeSecs: Number(currentWeavessViewRange.startTimeSecs) + timeToPanBy,
          endTimeSecs: Number(currentWeavessViewRange.endTimeSecs) + timeToPanBy
        };

      const possibleRangeOfDataToLoad: WeavessTypes.TimeRange = (panDirection === PanType.Left) ?
      {
        startTimeSecs: pannedViewTimeInterval.startTimeSecs,
        endTimeSecs: this.state.viewableInterval.startTimeSecs
      } :
      {
        startTimeSecs: this.state.viewableInterval.endTimeSecs,
        endTimeSecs: pannedViewTimeInterval.endTimeSecs
      };

      // determine if we need to load data or just pan the current view
      // floor/ceil the values to minimize the chance of erronerous reloading
      if ((Math.ceil(possibleRangeOfDataToLoad.startTimeSecs) < Math.floor(this.state.viewableInterval.startTimeSecs))
        || (Math.floor(possibleRangeOfDataToLoad.endTimeSecs) > (Math.ceil(this.state.viewableInterval.endTimeSecs)))) {
        this.fetchDataOutsideInterval(possibleRangeOfDataToLoad.startTimeSecs, possibleRangeOfDataToLoad.endTimeSecs)
        .then(() =>
          this.weavessDisplay.zoomToTimeWindow(
            pannedViewTimeInterval.startTimeSecs,
            pannedViewTimeInterval.endTimeSecs))
        .catch(error => UILogger.error(`Failed to fetch data outside interval: ${error}`));
      } else {
        this.weavessDisplay.zoomToTimeWindow(pannedViewTimeInterval.startTimeSecs, pannedViewTimeInterval.endTimeSecs);
      }
    }
    return;
  }
}
// tslint:disable-next-line:max-file-line-count
