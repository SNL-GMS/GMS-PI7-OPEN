import {
  WeavessTypes
} from '@gms/weavess';
import * as Immutable from 'immutable';
import * as lodash from 'lodash';
import {
  determineDetectionColor,
  filterSignalDetectionsByStationId,
  getSignalDetectionChannelSegments,
  isPeakTroughInWarning
} from '~analyst-ui/common/utils/signal-detection-util';
import { getSelectedWaveformFilter } from '~analyst-ui/common/utils/waveform-util';
import { QcMaskDisplayFilters, systemConfig, userPreferences } from '~analyst-ui/config';
import {
  ChannelSegmentTypes,
  CommonTypes,
  EventTypes,
  QcMaskTypes,
  SignalDetectionTypes,
  StationTypes,
  WaveformTypes,
} from '~graphql/';
import { FeaturePrediction } from '~graphql/event/types';
import { FeatureMeasurementTypeName, InstantMeasurementValue } from '~graphql/signal-detection/types';
import {
  findAmplitudeFeatureMeasurementValue,
  findArrivalTimeFeatureMeasurementValue,
  findPhaseFeatureMeasurementValue,
} from '~graphql/signal-detection/utils';
import { MeasurementMode, Mode, WaveformSortType } from '~state/analyst-workspace/types';
import { AlignWaveformsOn, WaveformDisplayProps, WaveformDisplayState } from './types';
import { calculateOffsets, Offset } from './utils';
import { WaveformClient } from './waveform-client';

/**
 * Interface used to bundle all of the paramters need to create the 
 * weavess stations for the waveform display.
 */
export interface CreateWeavessStationsParameters {
  defaultStations: StationTypes.ProcessingStation[];
  measurementMode: MeasurementMode;
  featurePredictions: FeaturePrediction[];
  signalDetectionsByStation: SignalDetectionTypes.SignalDetection[];
  eventsInTimeRange: EventTypes.Event[];
  qcMasksByChannelId: QcMaskTypes.QcMask[];
  channelHeight: number;
  maskDisplayFilters: QcMaskDisplayFilters;
  channelFilters: Immutable.Map<string, WaveformTypes.WaveformFilter>;
  waveformClient: WaveformClient;
  defaultWaveformFilters: WaveformTypes.WaveformFilter[];
  startTimeSecs: number;
  endTimeSecs: number;
  distanceToSourceForDefaultStations?: StationTypes.DistanceToSource[];
  currentOpenEvent?: EventTypes.Event;
  showPredictedPhases: boolean;
  offsets: Offset[];
}

/**
 * Creates CreateWeavessStationsParameters with the required fields used 
 * for to creating the weavess stations for the waveform display.
 * 
 * @param props The WaveformDisplayProps
 * @param state The WaveformDisplayState
 * @param channelHeight The height of rendered channels in weavess in px
 * @param waveformClient A reference to an instantiated WaveformClient object
 * @returns CreateWeavessStationsParameters
 */
export function populateCreateWeavessStationsParameters(
  props: WaveformDisplayProps,
  state: WaveformDisplayState,
  channelHeight: number,
  waveformClient: WaveformClient): CreateWeavessStationsParameters {
  const fpList = getFeaturePredictionsForOpenEvent(props)
    .filter(fp => fp.predictionType === FeatureMeasurementTypeName.ARRIVAL_TIME);
  const events = props.eventsInTimeRangeQuery && props.eventsInTimeRangeQuery.eventsInTimeRange ?
    props.eventsInTimeRangeQuery.eventsInTimeRange : [];

  const params: CreateWeavessStationsParameters = {
    defaultStations: props.defaultStationsQuery.defaultStations,
    measurementMode: props.measurementMode,
    featurePredictions: fpList,
    signalDetectionsByStation: props.signalDetectionsByStationQuery &&
      props.signalDetectionsByStationQuery.signalDetectionsByStation ?
      props.signalDetectionsByStationQuery.signalDetectionsByStation : [],
    eventsInTimeRange: events,
    qcMasksByChannelId: props.qcMasksByChannelIdQuery && props.qcMasksByChannelIdQuery.qcMasksByChannelId ?
      props.qcMasksByChannelIdQuery.qcMasksByChannelId : [],
    channelHeight,
    maskDisplayFilters: state.maskDisplayFilters,
    channelFilters: props.channelFilters,
    waveformClient,
    defaultWaveformFilters: props.defaultWaveformFiltersQuery.defaultWaveformFilters,
    startTimeSecs: props.currentTimeInterval.startTimeSecs,
    endTimeSecs: props.currentTimeInterval.endTimeSecs,
    distanceToSourceForDefaultStations:
      props.distanceToSourceForDefaultStationsQuery &&
        props.distanceToSourceForDefaultStationsQuery.distanceToSourceForDefaultStations ?
        props.distanceToSourceForDefaultStationsQuery.distanceToSourceForDefaultStations : undefined,
    currentOpenEvent: events.find(event => event.id === state.currentOpenEventId),
    showPredictedPhases: state.showPredictedPhases,
    offsets: state.alignWaveformsOn === AlignWaveformsOn.TIME ? [] : calculateOffsets(fpList, state.phaseToAlignOn)
  };
  return params;
}

/**
 * Filter the stations based on the mode setting.
 *
 * @param mode the mode of the waveform display
 * @param station the station
 * @param signalDetectionsByStation the signal detections for all stations
 */
function filterStationOnMode(
  mode: Mode,
  station: StationTypes.ProcessingStation,
  currentOpenEvent: EventTypes.Event,
  signalDetectionsByStation: SignalDetectionTypes.SignalDetection[]): boolean {

  if (Mode.MEASUREMENT === mode) {
    if (currentOpenEvent) {
      const associatedSignalDetectionHypothesisIds =
        currentOpenEvent.currentEventHypothesis.eventHypothesis.signalDetectionAssociations
          .map(association => association.signalDetectionHypothesis.id);

      const signalDetections = signalDetectionsByStation ?
      signalDetectionsByStation
        .filter(sd => {
          // filter out the sds for the other stations and the rejected sds
          if (sd.station.id !== station.id || sd.currentHypothesis.rejected) {
            return false;
          }

          // filter sds that are associated to the current open event
          if (lodash.includes(associatedSignalDetectionHypothesisIds, sd.currentHypothesis.id)) {
            return true;
          }

          return false;
        }) : [];
      // display the station only if sds were returned
      return signalDetections.length > 0;
    }
  }

  return true; // show all stations (DEFAULT)
}

/**
 * Returns the `green` interval markers.
 * 
 * @param startTimeSecs start time seconds for the interval start marker
 * @param endTimeSecs end time seconds for the interval end marker
 */
function getIntervalMarkers(startTimeSecs: number, endTimeSecs: number): WeavessTypes.Marker[] {
  return [
    {
      id: 'startTime',
      color: '#44EE55',
      lineStyle: WeavessTypes.LineStyle.SOLID,
      timeSecs: startTimeSecs
    },
    {
      id: 'endTime',
      color: '#44EE55',
      lineStyle: WeavessTypes.LineStyle.SOLID,
      timeSecs: endTimeSecs
    }
  ];
}

/**
 * Creates the weavess stations for the waveform display.
 * 
 * @param params CreateWeavessStationsParameters the parameters required for
 * creating the weavess stations for the waveform display.
 * 
 * @returns WeavessStation[]
 */
export function createWeavessStations(params: CreateWeavessStationsParameters): WeavessTypes.Station[] {
  const intervalMarkers: WeavessTypes.Marker[] = getIntervalMarkers(params.startTimeSecs, params.endTimeSecs);

  const weavessStations: WeavessTypes.Station[] = params.defaultStations
    // filter the stations based on the mode setting
    .filter(stationToFilterOnMode =>
      filterStationOnMode(
        params.measurementMode.mode,
        stationToFilterOnMode, params.currentOpenEvent, params.signalDetectionsByStation))
    .map(station => {
      const selectedFilter: WaveformTypes.WaveformFilter =
        getSelectedWaveformFilter(
          params.measurementMode.mode,
          station.id,
          station.defaultChannel.sampleRate,
          params.channelFilters, params.defaultWaveformFilters);
      // Build a default channel segment to use if no Signal Detections are found
      // The segment type is FK_BEAM since that is all that is drawn on the default channels
      const segmentType: ChannelSegmentTypes.ChannelSegmentType = ChannelSegmentTypes.ChannelSegmentType.FK_BEAM;

      const defaultChannelName = getChannelLabelAddition(segmentType, segmentType, false);

      const signalDetections = params.signalDetectionsByStation ?
        filterSignalDetectionsByStationId(station.id, params.signalDetectionsByStation)
        : [];

      const distanceToSource =
        params.distanceToSourceForDefaultStations ?
          params.distanceToSourceForDefaultStations.find(d => d.stationId === station.id)
          : undefined;

      const stationOffset = params.offsets.find(offset => offset.channelId === station.defaultChannel.id);

      // If there are Signal Detections populate Weavess Channel Segment from the FK_BEAM
      // else use the default channel Weavess Channel Segment built
      const channelSegments =  new Map<string, WeavessTypes.ChannelSegment>();
      if (signalDetections && signalDetections.length > 0) {
        // clone to add UNFILTERED
        const allFilters = [...params.defaultWaveformFilters, WaveformTypes.UNFILTERED_FILTER];
        allFilters.forEach(filter => {
          const signalDetectionChannelSegments = getSignalDetectionChannelSegments(signalDetections, filter);
          if (signalDetectionChannelSegments &&
            signalDetectionChannelSegments.dataSegments &&
            signalDetectionChannelSegments.dataSegments.length > 0) {
            channelSegments.set(filter.id, signalDetectionChannelSegments);
          }
        });
      }

      // populate the selection windows for each station
      const selectionWindows: WeavessTypes.SelectionWindow[] =
          lodash.flatMap(signalDetections.map(sd => {

            const associatedSignalDetectionHypothesisIds = params.currentOpenEvent ?
              params.currentOpenEvent.currentEventHypothesis.eventHypothesis.signalDetectionAssociations
                .map(association => association.signalDetectionHypothesis.id) : [];

            const arrivalTime: number =
              findArrivalTimeFeatureMeasurementValue(sd.currentHypothesis.featureMeasurements).value;

            const phase = findPhaseFeatureMeasurementValue(sd.currentHypothesis.featureMeasurements).phase;

            const isSdAssociatedToOpenEvent =
              lodash.includes(associatedSignalDetectionHypothesisIds, sd.currentHypothesis.id) &&
              // sd must have phase type that is contained in the measurement mode phase filter list
              lodash.includes(systemConfig.measurementMode.phases, phase);

            const isManualShow = [...params.measurementMode.entries.entries()]
              .filter(({ 1: v }) => v)
              .map(([k]) => k)
              .find(id => id === sd.id);

            const isManualHide = [...params.measurementMode.entries.entries()]
              .filter(({ 1: v }) => !v)
              .map(([k]) => k)
              .find(id => id === sd.id);

            const amplitudeMeasurementValue =
              findAmplitudeFeatureMeasurementValue(sd.currentHypothesis.featureMeasurements);

            const selectionStartOffset = systemConfig.measurementMode.selection.startTimeOffsetFromSignalDetection;
            const selectionEndOffset = systemConfig.measurementMode.selection.endTimeOffsetFromSignalDetection;

            // display the measurement selection windows if the sd is associated
            // to the open event and its phase is included in one of the measurement mode phases
            if ((params.measurementMode.mode === Mode.MEASUREMENT && isSdAssociatedToOpenEvent && !isManualHide) ||
                (isManualShow)) {
              const selections: WeavessTypes.SelectionWindow[] = [];
              selections.push({
                id: `${systemConfig.measurementMode.selection.id}${sd.id}`,
                startMarker: {
                  id: 'start',
                  color: systemConfig.measurementMode.selection.borderColor,
                  lineStyle: systemConfig.measurementMode.selection.lineStyle,
                  timeSecs: arrivalTime + selectionStartOffset,
                },
                endMarker: {
                  id: 'end',
                  color: systemConfig.measurementMode.selection.borderColor,
                  lineStyle: systemConfig.measurementMode.selection.lineStyle,
                  timeSecs: arrivalTime + selectionEndOffset,
                },
                isMoveable: systemConfig.measurementMode.selection.isMoveable,
                color: systemConfig.measurementMode.selection.color
              });

              if (amplitudeMeasurementValue) {
                const period = amplitudeMeasurementValue.period;
                const troughTime = amplitudeMeasurementValue.startTime;
                const peakTime = troughTime + (period / 2); // display only period/2
                const isWarning = isPeakTroughInWarning(arrivalTime, period, troughTime, peakTime);

                const isMoveable = params.measurementMode.mode === Mode.MEASUREMENT &&
                  systemConfig.measurementMode.peakTroughSelection.isMoveable;
                selections.push({
                  id: `${systemConfig.measurementMode.peakTroughSelection.id}${sd.id}`,
                  startMarker: {
                    id: 'start',
                    color: !isWarning ?
                      systemConfig.measurementMode.peakTroughSelection.borderColor :
                      systemConfig.measurementMode.peakTroughSelection.warning.borderColor,
                    lineStyle: isMoveable ? systemConfig.measurementMode.peakTroughSelection.lineStyle :
                      systemConfig.measurementMode.peakTroughSelection.nonMoveableLineStyle,
                    timeSecs: troughTime,
                    minTimeSecsConstraint: arrivalTime +
                      systemConfig.measurementMode.selection.startTimeOffsetFromSignalDetection,
                  },
                  endMarker: {
                    id: 'end',
                    color: !isWarning ?
                      systemConfig.measurementMode.peakTroughSelection.borderColor :
                      systemConfig.measurementMode.peakTroughSelection.warning.borderColor,
                    lineStyle: isMoveable ? systemConfig.measurementMode.peakTroughSelection.lineStyle :
                      systemConfig.measurementMode.peakTroughSelection.nonMoveableLineStyle,
                    timeSecs: peakTime,
                    maxTimeSecsConstraint: arrivalTime +
                      systemConfig.measurementMode.selection.endTimeOffsetFromSignalDetection,
                  },
                  isMoveable,
                  color: !isWarning ?
                    systemConfig.measurementMode.peakTroughSelection.color :
                    systemConfig.measurementMode.peakTroughSelection.warning.color
                });
              }
              return selections;
            } else {
              return [];
            }
          }));

      const weavessStation: WeavessTypes.Station = {
        id: station.id,
        name: station.name,
        distance: (distanceToSource) ? distanceToSource.distance : 0,
        distanceUnits: distanceToSource && distanceToSource.distanceUnits ?
          distanceToSource.distanceUnits : userPreferences.distanceUnits,
        defaultChannel: {
          id: station.id,
          name: defaultChannelName ? `${station.name}${defaultChannelName}` : `${station.name}`,
          height: params.channelHeight,
          timeOffsetSeconds: stationOffset ? stationOffset.offset : 0,
          channelType: segmentType,
          waveform: {
            channelSegmentId: selectedFilter ? selectedFilter.id : '',
            channelSegments,
            predictedPhases:
              params.showPredictedPhases ?
                params.featurePredictions.filter(fp => fp.stationId === station.id)
                  .map(fp => ({
                    timeSecs: (fp.predictedValue as InstantMeasurementValue).value,
                    label: fp.phase,
                    id: fp.id,
                    color: userPreferences.colors.predictedPhases.color,
                    filter: userPreferences.colors.predictedPhases.filter
                  }))
                : [],
            signalDetections: (station && signalDetections) ? signalDetections
              .map(detection => {
                const color = determineDetectionColor(detection, params.eventsInTimeRange,
                                                      params.currentOpenEvent ? params.currentOpenEvent.id : undefined);
                const arrivalTimeFeatureMeasurementValue =
                  findArrivalTimeFeatureMeasurementValue(detection.currentHypothesis.featureMeasurements);
                const fmPhase = findPhaseFeatureMeasurementValue(detection.currentHypothesis.featureMeasurements);
                return {
                  timeSecs: arrivalTimeFeatureMeasurementValue ?
                      arrivalTimeFeatureMeasurementValue.value : 0,
                  label: fmPhase.phase.toString(),
                  id: detection.id,
                  color
                };
              }) : [],
            masks: undefined,
            markers: {
              verticalMarkers: intervalMarkers,
              selectionWindows
            }
          }
        },
        nonDefaultChannels:
          // sds are only displayed on the default channel;
          // hide all non-default channels in measurement mode
          Mode.MEASUREMENT === params.measurementMode.mode ?
            [] :
            lodash.flatMap(station.sites, site => (site && site.channels) ?
              site.channels.map(channel => {
                const nonDefaultChannel =
                  getChannelSegments(
                    params.measurementMode.mode,
                    channel.id, channel.sampleRate, params.channelFilters,
                    params.waveformClient, params.defaultWaveformFilters, params.startTimeSecs);
                const nonDefaultChannelName = getChannelLabelAddition(
                  nonDefaultChannel.segmentType, channel.name, true);
                const ndc: WeavessTypes.Channel = {
                  id: channel.id,
                  name: nonDefaultChannelName ? `${nonDefaultChannelName}` : `${site.name}`,
                  timeOffsetSeconds: stationOffset ? stationOffset.offset : 0,
                  height: params.channelHeight,
                  waveform: {
                    channelSegmentId: nonDefaultChannel.channelSegmentId,
                    channelSegments: nonDefaultChannel.channelSegments,
                    // if the mask category matches the enabled masks then return the mask else skip it
                    masks: (channel) ?
                      params.qcMasksByChannelId
                        .filter(m => m.channelId === channel.id)
                        .filter(qcMask => Object.keys(params.maskDisplayFilters)
                          .find(key => qcMask.currentVersion.category === key &&
                            params.maskDisplayFilters[key].visible))
                        .map(qcMask => ({
                          id: qcMask.id,
                          startTimeSecs: qcMask.currentVersion.startTime,
                          endTimeSecs: qcMask.currentVersion.endTime,
                          color: userPreferences.colors.waveforms.
                            maskDisplayFilters[qcMask.currentVersion.category].color,
                        })) : undefined,
                    markers: {
                      verticalMarkers: intervalMarkers
                    }
                  }
                };
                return ndc;
              }) : []
            ),
      };
      // Sort non-default channels alphabetical
      weavessStation.nonDefaultChannels =
        lodash.orderBy(weavessStation.nonDefaultChannels, [chan => chan.name], ['asc']);
      return weavessStation;
  })
    .filter(weavessStation => weavessStation !== undefined);
  return weavessStations;
}

/**
 * Gets the appropriate channelSegments for the currently appied filter
 * 
 * @param mode current mode
 * @param id Id of the channel
 * @param sampleRate the sample rate of the channel
 * @param channelFilters Mapping of ids to filters
 * @param waveformClient Reference to instantiated WaveformClient object
 * @param defaultWaveformFilters A list of filters retrieved from the gateway
 * @param startTimeSecs The start time of the channel Segments
 * 
 * @returns an object containing a channelSegmentId, list of channel segments, and the type of segment
 */
function getChannelSegments(mode: Mode, id: string, sampleRate: number,
  channelFilters: Immutable.Map<string, WaveformTypes.WaveformFilter>,
  waveformClient: WaveformClient, defaultWaveformFilters: WaveformTypes.WaveformFilter[], startTimeSecs: number) {
  const selectedFilter: WaveformTypes.WaveformFilter =
    getSelectedWaveformFilter(mode, id, sampleRate, channelFilters, defaultWaveformFilters);

  const channelSegments = new Map<string, WeavessTypes.ChannelSegment>();
  const cachedEntries = waveformClient.getWaveformEntriesForChannelId(id);

  let segmentType: ChannelSegmentTypes.ChannelSegmentType;
  if (cachedEntries) {
    cachedEntries.forEach((value, key) => {
      if (key === 'unfiltered') {
        segmentType = value[0].type;
      }
      let waveformFilter: WaveformTypes.WaveformFilter =
        WaveformTypes.UNFILTERED_FILTER as WaveformTypes.WaveformFilter;
      defaultWaveformFilters.forEach(f => {
        if (f.id === key) {
          waveformFilter = f;
        }
      });

      const result = getDataSegments(value, waveformFilter, id, startTimeSecs);
      const filterSampleRate = waveformFilter.sampleRate !== undefined ? ` ${waveformFilter.sampleRate} ` : '';
      const description = `${waveformFilter.name}${filterSampleRate}`;

      channelSegments.set(key, {
        description: result.showLabel ? description : '',
        descriptionLabelColor: result.isSampleRateOk ? 'white' : 'red',
        dataSegments: result.dataSegments
      });
    });
  }
  return { channelSegmentId: selectedFilter.id, channelSegments, segmentType };
}

/**
 * Gets data segments based on what filter is being applied and the presence of raw data
 * 
 * @param cachedData result of cache get based on channel and filter
 * @param filter filter being applied
 * @param channel the filter is being applied to
 * @param startTimeSecs start of data segment
 * 
 * @returns object with list of dataSegments, isSampleRateOk (boolean), showLabel (boolean)
 */
function getDataSegments(cachedData:
  [ChannelSegmentTypes.ChannelSegment<WaveformTypes.Waveform> | WaveformTypes.FilteredChannelSegment],
  filter: WaveformTypes.WaveformFilter, channel: string,
  startTimeSecs: number) {

  // If there was no raw data and no filtered data return empty data segments
  if (!cachedData || cachedData.length < 1 || !cachedData.filter(s => s.timeseries.length > 1)) {
    return ({
      dataSegs: [{ startTimeSecs, data: [] }],
      sampleRateCheck: true, showLabel: false
    });
  }

  // This should be moved to waveform client fetch -- most likely
  const isSampleRateOk = filter.name === WaveformTypes.UNFILTERED_FILTER.name
    || (filter.validForSampleRate
      && sampleRateIsInTolerance(cachedData[0].timeseries[0].sampleRate, filter));

  const sampleRate = cachedData && cachedData[0].timeseries ? cachedData[0].timeseries[0].sampleRate :
  WaveformTypes.DEFAULT_SAMPLE_RATE;

  const dataSegments = lodash.flatten(cachedData.map(s => s.timeseries.map((t: WaveformTypes.Waveform) =>
    ({
      startTimeSecs: t.startTime,
      sampleRate,
      color: userPreferences.colors.waveforms.raw,
      displayType: [WeavessTypes.DisplayType.LINE],
      pointSize: 1,
      data: t.values,
    }))));

  return ({ dataSegments, isSampleRateOk, showLabel: true });
}

/**
 * Checks sample rate tolerance based on waveform filter
 * 
 * @param sampleRate sample rate to check
 * @param wfFilter filter defintion
 * 
 * @returns boolean
 */
function sampleRateIsInTolerance(sampleRate: number, wfFilter: WaveformTypes.WaveformFilter): boolean {
  return sampleRate > (wfFilter.sampleRate - wfFilter.sampleRateTolerance) &&
    sampleRate < (wfFilter.sampleRate + wfFilter.sampleRateTolerance);
}

/**
 * Helper function to return the correct channel label based on channel segment type
 * channel
 *
 * @param channelSegment Channel segment to get label for
 * @param channelName name of channel
 * @param isSubChannel whether or not its a sub channel
 * 
 * @returns string representing the channel label
 */
function getChannelLabelAddition(
  channelSegment: ChannelSegmentTypes.ChannelSegmentType, channelName: string, isSubChannel: boolean): string {
  // If channel segment is not defined return empty for default channel
  // or channel lable for sub-channels (otherwise looks like repeated channels)
  if (!channelSegment) {
    if (isSubChannel) {
      return channelName;
    } else {
      return '';
    }
  }

  if (channelSegment === ChannelSegmentTypes.ChannelSegmentType.FK_BEAM) {
    return '/fkb';
  } else if (!channelName) {
    return '';
  } else {
    return channelName;
  }
}

/**
 * Returns true if there is a graphql query loading; false otherwise.
 * 
 * @param props WaveformDisplayProps
 * @param state WaveformDisplayState
 * 
 * @returns boolean
 */
export function isLoading(props: WaveformDisplayProps, state: WaveformDisplayState): boolean {
  return (
    (props.defaultStationsQuery && props.defaultStationsQuery.loading) ||
    (props.defaultWaveformFiltersQuery && props.defaultWaveformFiltersQuery.loading) ||
    (props.eventsInTimeRangeQuery && props.eventsInTimeRangeQuery.loading) ||
    (props.signalDetectionsByStationQuery && props.signalDetectionsByStationQuery.loading) ||
    (props.qcMasksByChannelIdQuery && props.qcMasksByChannelIdQuery.loading) ||
    // distance to source is only reqiured when there is a open event
    // tslint:disable-next-line:max-line-length
    (props.distanceToSourceForDefaultStationsQuery && props.distanceToSourceForDefaultStationsQuery.loading && !state.currentOpenEventId) ||
    state.loadingWaveforms
  );
}

/**
 * Returns true if there is a graphql query error; false otherwise.
 * 
 * @param props WaveformDisplayProps
 * 
 * @returns boolean
 */
export function isError(props: WaveformDisplayProps): boolean {
  return (
    (props.defaultStationsQuery && props.defaultStationsQuery.error !== undefined) ||
    (props.defaultWaveformFiltersQuery && props.defaultWaveformFiltersQuery.error !== undefined) ||
    (props.eventsInTimeRangeQuery && props.eventsInTimeRangeQuery.error !== undefined) ||
    (props.signalDetectionsByStationQuery && props.signalDetectionsByStationQuery.error !== undefined) ||
    (props.qcMasksByChannelIdQuery && props.qcMasksByChannelIdQuery.error !== undefined) ||
    // tslint:disable-next-line:max-line-length
    (props.distanceToSourceForDefaultStationsQuery && props.distanceToSourceForDefaultStationsQuery.error !== undefined)
  );
}

/**
 * sort waveform list based on sort type
 * 
 * @param props WaveformDisplayProps
 * @param state WaveformDisplayStates
 * 
 * @returns sortedWaveformList
 */
export function sortWaveformList(stations: WeavessTypes.Station[],
  waveformSortType: WaveformSortType): WeavessTypes.Station[] {
  // apply sort based on sort type
  let newStations = [];
  // Sort by distance if in global scan
  if (waveformSortType === WaveformSortType.distance) {
    newStations = lodash.sortBy(stations, [station => station.distance]);
  } else {
    // For station name sort, order a-z by station config name
    if (waveformSortType === WaveformSortType.stationName) {
      newStations = lodash.orderBy(stations, [station => station.name], ['asc']);
    }
  }
  return newStations;
}

/**
 * Find the selected filter for the channel
 * 
 * @param sampleRate Sample rate of the filter
 * @param selectedFilterIndex index of the current selected filter
 * @param filterNames names of all the filters
 * @param defaultWaveformFilters list of filters
 * 
 * @returns the waveformfilter requested
 */
export function findWaveformFilter(sampleRate: number,
  selectedFilterIndex: number, filterNames: string[],
  defaultWaveformFilters: WaveformTypes.WaveformFilter[]): WaveformTypes.WaveformFilter {

  // If no filter selected, return unfiltered
  if (selectedFilterIndex === -1) {
    return WaveformTypes.UNFILTERED_FILTER as WaveformTypes.WaveformFilter;
  }
  const selectedFilterName = filterNames[selectedFilterIndex];

  const filters = defaultWaveformFilters;
  let filter = filters.find(filt => filt.name === selectedFilterName && filt.sampleRate === sampleRate);
  if (!filter) {
    filter = filters.find(filt => filt.name === selectedFilterName);
  }
  return filter;
}

/**
 * Returns Feature Predictions if there is an open event
 * @param props current waveform display props
 */
export function getFeaturePredictionsForOpenEvent(props: WaveformDisplayProps): FeaturePrediction[] {
  if (props.currentOpenEventId) {
    const openEvent = props.eventsInTimeRangeQuery.eventsInTimeRange
      .find(event => event.id === props.currentOpenEventId);
    if (openEvent) {
      return openEvent.currentEventHypothesis.eventHypothesis
        .preferredLocationSolution.locationSolution.featurePredictions;
    }
  }
  return [];
}

  /**
   * Returns a list of phases that are present for FP alignment
   */
export function getAlignablePhases(props: WaveformDisplayProps): CommonTypes.PhaseType[] {
  return systemConfig.defaultSdPhases.filter(phase => {
    const fpList = getFeaturePredictionsForOpenEvent(props);
    return (fpList.filter(fp => fp.phase === phase).length > 0);
  });
}
