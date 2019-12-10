import { NonIdealState } from '@blueprintjs/core';
import {
  WeavessTypes, WeavessUtils
} from '@gms/weavess';
import * as Immutable from 'immutable';
import * as React from 'react';
import { getFkData, getFkParamsForSd } from '~analyst-ui/common/utils/fk-utils';
import {
  determineDetectionColor,
  filterSignalDetectionsByStationId,
  getSignalDetectionBeams,
  getSignalDetectionChannelSegments
} from '~analyst-ui/common/utils/signal-detection-util';
import { getSelectedWaveformFilter } from '~analyst-ui/common/utils/waveform-util';
import { WeavessDisplay } from '~analyst-ui/components/weavess-display';
import { systemConfig, userPreferences } from '~analyst-ui/config';
import {
  EventTypes, FkTypes,
  SignalDetectionTypes,
  StationTypes,
  WaveformTypes
} from '~graphql/';
import {
  findArrivalTimeFeatureMeasurementValue,
  findPhaseFeatureMeasurementValue
} from '~graphql/signal-detection/utils';
import { MeasurementMode } from '~state/analyst-workspace/types';
import { UILogger } from '~util/log/logger';
import { FkParams } from '../../types';
import { getPredictedPoint } from '../fk-util';

/**
 * FkPlots Props
 */
export interface FkPlotsProps {
  defaultStations: StationTypes.ProcessingStation[];
  defaultWaveformFilters: WaveformTypes.WaveformFilter[];
  channelFilters: Immutable.Map<string, WaveformTypes.WaveformFilter>;
  eventsInTimeRange: EventTypes.Event[];
  currentOpenEvent?: EventTypes.Event;
  signalDetection: SignalDetectionTypes.SignalDetection;
  signalDetectionsByStation: SignalDetectionTypes.SignalDetection[];
  signalDetectionFeaturePredictions: EventTypes.FeaturePrediction[];
  measurementMode: MeasurementMode;
  fstatData: FkTypes.FstatData;
  windowParams: FkTypes.WindowParameters;
  contribChannels: {
    id: string;
  }[];
  changeUserInputFks(windowParams: FkTypes.WindowParameters, frequencyBand: FkTypes.FrequencyBand): void;
  onSetWindowLead(sdId: string, leadFkSpectrumSeconds: number, windowLead: number): void;
  onNewFkParams(sdId: string, fkParams: FkParams, fkConfiguration: FkTypes.FkConfiguration): Promise<void>;
}

/**
 * FkPlots State
 */
// tslint:disable-next-line: no-empty-interface
export interface FkPlotsState {
}

/** 
 * Renders the FK waveform data with Weavess
 */
export class FkPlots extends React.PureComponent<FkPlotsProps, FkPlotsState> {

  /** The precision of displayed lead/lag pair */
  private readonly digitPrecision: number = 1;

  /** Hard-coded height of the waveform panel */
  private readonly waveformPanelHeight: number = 70;

  // ***************************************
  // BEGIN REACT COMPONENT LIFECYCLE METHODS
  // ***************************************

  /**
   * Constructor.
   * 
   * @param props The initial props
   */
  public constructor(props: FkPlotsProps) {
    super(props);
    this.state = { };
  }

  /**
   * Renders the component.
   */
  public render() {
    const arrivalTime =
      findArrivalTimeFeatureMeasurementValue(this.props.signalDetection.currentHypothesis.featureMeasurements).value;

    const timePadding = systemConfig.fkPlotTimePadding;

    const sampleRate = this.props.signalDetection.station.defaultChannel.sampleRate;
    const selectedFilter: WaveformTypes.WaveformFilter =
      getSelectedWaveformFilter(
        this.props.measurementMode.mode, this.props.signalDetection.station.id,
        sampleRate, this.props.channelFilters, this.props.defaultWaveformFilters);

    const signalDetectionBeam = getSignalDetectionBeams([this.props.signalDetection], selectedFilter);

    // az, slowness, and fstat have the same rate and num sumples
    // but we need to calculate the data to send to weavess for beam
    if ((!signalDetectionBeam || signalDetectionBeam.length !== 1) ||
      this.fStatDataContainsUndefined(this.props.fstatData)) {
      return (
        <NonIdealState
          visual="timeline-line-chart"
          title="Missing waveform data"
          description="Fk plots currently not supported for analyst created SDs"
        />);
    }

    const startTimeSecs = signalDetectionBeam[0].sampleCount > 0 ?
      signalDetectionBeam[0].startTime : arrivalTime - (timePadding / 2);
    const plotEndTimePadding = signalDetectionBeam[0].sampleCount > 0 ?
      (signalDetectionBeam[0].sampleCount / signalDetectionBeam[0].sampleRate) > timePadding ?
        (signalDetectionBeam[0].sampleCount / signalDetectionBeam[0].sampleRate)
        : timePadding
      : timePadding;
    const endTimeSecs = (startTimeSecs + plotEndTimePadding) <= arrivalTime ?
      (startTimeSecs + plotEndTimePadding) + (timePadding) : (startTimeSecs + plotEndTimePadding);

    const predictedPoint = getPredictedPoint(this.props.signalDetectionFeaturePredictions);

    const signalDetectionsForStation = this.props.signalDetectionsByStation ?
      filterSignalDetectionsByStationId(this.props.signalDetection.station.id, this.props.signalDetectionsByStation)
      : [];

    // If there are Signal Detections populate Weavess Channel Segment from the FK_BEAM
    // else use the default channel Weavess Channel Segment built
    const channelSegments =  new Map<string, WeavessTypes.ChannelSegment>();
    if (signalDetectionsForStation && signalDetectionsForStation.length > 0) {
      // clone to add UNFILTERED
      const allFilters = [...this.props.defaultWaveformFilters, WaveformTypes.UNFILTERED_FILTER];
      allFilters.forEach(filter => {
        const signalDetectionChannelSegments = getSignalDetectionChannelSegments(signalDetectionsForStation, filter);
        if (signalDetectionChannelSegments &&
          signalDetectionChannelSegments.dataSegments &&
          signalDetectionChannelSegments.dataSegments.length > 0) {
          channelSegments.set(filter.id, signalDetectionChannelSegments);
        }
      });
    }

    const signalDetections = signalDetectionsForStation.map(sd => ({
      timeSecs: findArrivalTimeFeatureMeasurementValue(
        sd.currentHypothesis.featureMeasurements).value,
      id: sd.id,
      label: findPhaseFeatureMeasurementValue(sd.currentHypothesis.featureMeasurements).phase
        .toString(),
      color: determineDetectionColor(
        sd, this.props.eventsInTimeRange, this.props.currentOpenEvent ?
          this.props.currentOpenEvent.id : undefined)
    }));

    const KEY = 'data';
    const stations: WeavessTypes.Station[] = [
      // Beam
      {
        id: 'Beam',
        name: 'Beam',
        defaultChannel: {
          id: this.props.signalDetection.station.id,
          name: 'Beam',
          height: this.waveformPanelHeight,
          waveform: {
            channelSegmentId: selectedFilter.id,
            channelSegments,
            signalDetections
          }
        }
      },
      // Fstat
      {
        id: 'Fstat',
        name: 'Fstat',
        defaultChannel: {
          id: `Fstat-${this.props.signalDetection.station.id}`,
          name: 'Fstat',
          height: this.waveformPanelHeight,
          waveform: {
            channelSegmentId: KEY,
            channelSegments: new Map([
              [
                KEY,
                {
                  dataSegments: [{
                    startTimeSecs: this.props.fstatData.fstatWf.startTime,
                    color: userPreferences.colors.waveforms.raw,
                    sampleRate: this.props.fstatData.fstatWf.sampleRate,
                    displayType: [WeavessTypes.DisplayType.LINE, WeavessTypes.DisplayType.SCATTER],
                    pointSize: 2,
                    data: this.props.fstatData.fstatWf.values
                  }],
                }
              ]
            ]),
          }
        }
      },
      // Azimuth
      {
        id: 'Azimuth',
        name: 'Azimuth',
        defaultChannel: {
          id: `Azimuth-${this.props.signalDetection.station.id}`,
          name: (<div key="azimuth-name" style={{whiteSpace: 'nowrap'}}>Azimuth <sup key="sup">(&deg;)</sup></div>),
          height: this.waveformPanelHeight,
          waveform: {
            channelSegmentId: KEY,
            channelSegments: new Map([
              [
                KEY,
                {
                  dataSegments: [{
                    startTimeSecs: this.props.fstatData.azimuthWf.startTime,
                    color: userPreferences.colors.waveforms.raw,
                    sampleRate: this.props.fstatData.azimuthWf.sampleRate,
                    displayType: [WeavessTypes.DisplayType.LINE, WeavessTypes.DisplayType.SCATTER],
                    pointSize: 2,
                    data: this.props.fstatData.azimuthWf.values
                  }
                ],
                }
              ],
            ]),
          },
        }
      },
      // Slowness
      {
        id: 'Slowness',
        name: 'Slowness',
        defaultChannel: {
          id: `Slowness-${this.props.signalDetection.station.id}`,
          // tslint:disable-next-line: max-line-length
          name: (<div key="slowness-name" style={{whiteSpace: 'nowrap'}}>Slowness (<sup key="sup">s</sup>&#8725;<sub key="sub">&deg;</sub>)</div>),
          height: this.waveformPanelHeight,
          waveform: {
            channelSegmentId: KEY,
            channelSegments: new Map([
              [
                KEY,
                {
                  dataSegments: [{
                    startTimeSecs: this.props.fstatData.slownessWf.startTime,
                    color: userPreferences.colors.waveforms.raw,
                    sampleRate: this.props.fstatData.slownessWf.sampleRate,
                    displayType: [WeavessTypes.DisplayType.LINE, WeavessTypes.DisplayType.SCATTER],
                    pointSize: 2,
                    data: this.props.fstatData.slownessWf.values
                  }
                ],
                }
              ]
            ]),
          }
        }
      }
    ];

    // add the Azimuth and Slowness flat lines if the appropriate predicted value exists
    if (predictedPoint) {
      stations[2].defaultChannel.waveform.channelSegments.get(KEY).dataSegments
        .push(
          WeavessUtils.Waveform.createFlatLineDataSegment(
            startTimeSecs, endTimeSecs, predictedPoint.azimuth,
            userPreferences.azimuthSlowness.predictedLineForAzimuth.color)
        );
    }

    if (predictedPoint) {
      stations[3].defaultChannel.waveform.channelSegments.get(KEY).dataSegments
        .push(
        WeavessUtils.Waveform.createFlatLineDataSegment(
          startTimeSecs, endTimeSecs, predictedPoint.slowness,
          userPreferences.azimuthSlowness.predictedLineForSlowness.color)
      );
    }

    // Get the SD FK configure to set the start marker lead secs and
    // from there add length to get endMarker in epoch time
    const config = getFkData(this.props.signalDetection.currentHypothesis.featureMeasurements).configuration;
    const startMarkerEpoch = arrivalTime - config.leadFkSpectrumSeconds;
    const endMarkerEpoch = startMarkerEpoch + this.props.windowParams.lengthSeconds;
    return (
      <div>
        <div className="ag-dark fk-plots-wrapper-1">
          <div className="fk-plots-wrapper-2">
            <WeavessDisplay
              weavessProps={{
                startTimeSecs,
                endTimeSecs,
                defaultZoomWindow: {
                  startTimeSecs,
                  endTimeSecs
                },
                initialZoomWindow: {
                  startTimeSecs,
                  endTimeSecs
                },
                stations,
                selections: {
                  signalDetections: [this.props.signalDetection.id]
                },
                events: {
                  onUpdateSelectionWindow: this.onUpdateSelectionWindow
                },
                markers: {
                  selectionWindows:
                    [
                      {
                        id: 'selection',
                        startMarker:
                        {
                          id: 'start',
                          color: 'red',
                          lineStyle: WeavessTypes.LineStyle.DASHED,
                          timeSecs: startMarkerEpoch,
                        },
                        endMarker:
                        {
                          id: 'end',
                          color: 'red',
                          lineStyle: WeavessTypes.LineStyle.DASHED,
                          timeSecs: endMarkerEpoch,
                          minTimeSecsConstraint: startMarkerEpoch + config.leadFkSpectrumSeconds
                        },
                        isMoveable: true,
                        color: 'rgba(200,0,0,0.2)'
                      }
                    ]
                }
              }}
              defaultWaveformFilters={this.props.defaultWaveformFilters}
              defaultStations={this.props.defaultStations}
              eventsInTimeRange={this.props.eventsInTimeRange}
              signalDetectionsByStation={this.props.signalDetectionsByStation}
              qcMasksByChannelId={[]}
            />
          </div>
        </div>
      </div>
    );
  }

  // ***************************************
  // END REACT COMPONENT LIFECYCLE METHODS
  // ***************************************

  /**
   * Call back for drag and drop change of the moveable selection
   * 
   * @param verticalMarkers List of markers in the fk plot display
   */
  private readonly onUpdateSelectionWindow = async (selection: WeavessTypes.SelectionWindow) => {
    const arrivalTime =
      findArrivalTimeFeatureMeasurementValue(this.props.signalDetection.currentHypothesis.featureMeasurements).value;

    const lagTime = this.props.windowParams.lengthSeconds - this.props.windowParams.leadSeconds;
    const newLeadTime = parseFloat((arrivalTime - (selection.startMarker.timeSecs))
      .toFixed(this.digitPrecision));
    const newLagTime = parseFloat(((selection.endMarker.timeSecs) - arrivalTime)
      .toFixed(this.digitPrecision));
    const minimumDeltaSize = 0.1;
    const priorParams = getFkParamsForSd(this.props.signalDetection);
    // If duration hasn't changed update new lead seconds and update user input which sets state
    // else call computeFk via onNewFkParams
    const durationDelta = Math.abs(this.props.windowParams.lengthSeconds - (newLagTime + newLeadTime));
    if (durationDelta < minimumDeltaSize) {
      this.props.onSetWindowLead(this.props.signalDetection.id, newLeadTime, this.props.windowParams.leadSeconds);
    } else if ((newLeadTime > this.props.windowParams.leadSeconds + minimumDeltaSize ||
      newLeadTime < this.props.windowParams.leadSeconds - minimumDeltaSize) ||
      (newLagTime > lagTime + minimumDeltaSize ||
        newLagTime < lagTime - minimumDeltaSize)) {
      const newParams: FkParams = {
        ...priorParams,
        windowParams: {
          ...priorParams.windowParams,
          lengthSeconds: parseFloat((selection.endMarker.timeSecs - selection.startMarker.timeSecs).
            toFixed(this.digitPrecision))
        }
      };
      const priorConfig = getFkData(this.props.signalDetection.currentHypothesis.featureMeasurements).configuration;
      // TODO: Fix this by setting leadFkSpectrumSeconds in the state for FkPlot
      priorConfig.leadFkSpectrumSeconds = newParams.windowParams.leadSeconds;
      this.props.onNewFkParams(this.props.signalDetection.id, newParams, priorConfig)
      .catch(error => UILogger.error(`Failed onNewFkParams: ${error}`));
    }
  }

  /**
   * Checks for any undefined waveforms inside of fstat data
   * 
   * @param fstatData as FkTypes.FstatData
   * @returns boolean if defined or not
   */
  private readonly fStatDataContainsUndefined = (fstatData: FkTypes.FstatData): boolean =>
    !fstatData || !fstatData.azimuthWf || !fstatData.fstatWf || !fstatData.slownessWf

}
