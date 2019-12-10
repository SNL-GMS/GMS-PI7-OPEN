import {
  Button,
  Checkbox,
  Classes,
  Colors,
  Intent,
  Label,
  NumericInput,
  Position,
  Toaster
} from '@blueprintjs/core';
import { IconNames } from '@blueprintjs/icons';
import * as lodash from 'lodash';
import * as moment from 'moment';
import * as React from 'react';
import { Weavess, WeavessTypes, WeavessUtils } from '../weavess';
import {
  rainbowSpectrogramData,
  rainbowSpectrogramFrequencyStep,
  rainbowSpectrogramTimeStep,
  spectrogramData,
  spectrogramFrequencyStep,
  spectrogramTimeStep
} from './sample-data/spectrum-data';

export interface WeavessExampleProps {
  showExampleControls: boolean;
}

export interface WeavessExampleState {
  toggleShowContent: string;

  stations: WeavessTypes.Station[];

  offset: number;

  isOnChannelExpandedEnabled: boolean;
  isOnChannelCollapsedEnabled: boolean;
  isOnContextMenuEnabled: boolean;
  isOnChannelLabelClickEnabled: boolean;
  isOnChannelClickEnabled: boolean;
  isOnSignalDetectionClickEnabled: boolean;
  isOnSignalDetectionDragEndEnabled: boolean;
  isOnSignalDetectionContextMenuEnabled: boolean;
  isOnPredictivePhaseClickEnabled: boolean;
  isOnPredictivePhaseDragEndEnabled: boolean;
  isOnPredictivePhaseContextMenuEnabled: boolean;
  isOnKeyPressEnabled: boolean;
  isOnMaskClickEnabled: boolean;
  isUpdateMarkersEnabled: boolean;
  isUpdateSelectionWindowsEnabled: boolean;
  isOnClickSelectionWindowsEnabled: boolean;
  isOnMeasureWindowUpdatedEnabled: boolean;

  selectedSignalDetections: string[];
  selectedPredictedPhases: string[];
}

export class WeavessExample extends React.Component<WeavessExampleProps, WeavessExampleState> {
  public static defaultProps: WeavessExampleProps = {
    showExampleControls: true
  };

  public static SAMPLE_RATE: number = 40;

  // tslint:disable-next-line:no-magic-numbers
  public static  NUM_SAMPLES: number = WeavessExample.SAMPLE_RATE * 600; // 10 minutes of data

  // tslint:disable-next-line:no-magic-numbers
  public static  startTimeSecs: number = 1507593600; // Tue, 10 Oct 2017 00:00:00 GMT

  // tslint:disable-next-line:no-magic-numbers
  public static  endTimeSecs: number = WeavessExample.startTimeSecs + 1800; // + 30 minutes

  public toaster: Toaster;

  public weavess: Weavess;

  public constructor(props: WeavessExampleProps) {
    super(props);
    this.state = {
      toggleShowContent: '',
      stations: [],
      offset: 0,
      isOnChannelExpandedEnabled: false,
      isOnChannelCollapsedEnabled: false,
      isOnContextMenuEnabled: false,
      isOnChannelLabelClickEnabled: false,
      isOnChannelClickEnabled: false,
      isOnSignalDetectionClickEnabled: false,
      isOnSignalDetectionDragEndEnabled: false,
      isOnSignalDetectionContextMenuEnabled: false,
      isOnPredictivePhaseClickEnabled: false,
      isOnPredictivePhaseDragEndEnabled: false,
      isOnPredictivePhaseContextMenuEnabled: false,
      isOnKeyPressEnabled: false,
      isOnMaskClickEnabled: false,
      isUpdateMarkersEnabled: false,
      isUpdateSelectionWindowsEnabled: false,
      isOnClickSelectionWindowsEnabled: false,
      isOnMeasureWindowUpdatedEnabled: false,
      selectedSignalDetections: [],
      selectedPredictedPhases: []
    };
  }

  public componentDidMount() {
   this.setState({
     toggleShowContent: this.getToggleContentLabel(),
     stations: this.generateDummyData()
   });
  }

  /* tslint:disable:no-magic-numbers */
// tslint:disable-next-line: cyclomatic-complexity
  public render() {

    const styleFlexItem = {
      width: '315px'
    };

    const styleToolbar: React.CSSProperties = {
      display: 'flex',
      justifyItems: 'right',
      textAlign: 'right'
    };

    const styleToolbarItem: React.CSSProperties = {
      margin: '6px',
      whiteSpace: 'nowrap',
    };

    const labelEvents: WeavessTypes.LabelEvents = {
      onChannelCollapsed: this.state.isOnChannelCollapsedEnabled
        ? (channelId: string) => {
            this.toast(
              `onChannelCollapsed: channelId:${channelId}`
            );
          }
        : undefined,
      onChannelExpanded: this.state.isOnChannelExpandedEnabled
        ? (channelId: string) => {
            this.toast(
              `onChannelExpanded: channelId:${channelId}`
            );
          }
        : undefined,
      onChannelLabelClick: this.state.isOnChannelLabelClickEnabled
        ? (
            e: React.MouseEvent<HTMLDivElement>,
            channelId: string
          ) => {
            this.toast(
              `onChannelLabelClick: channelId:${channelId}`
            );
          }
        : undefined,
    };

    const waveformEvents: WeavessTypes.ChannelContentEvents = {
      onContextMenu: this.state.isOnContextMenuEnabled
        ? (
            e: React.MouseEvent<HTMLDivElement>,
            channelId: string
          ) => {
            this.toast(
              `onContextMenu: channelId:${channelId}`
            );
          }
        : undefined,
      onChannelClick: this.state.isOnChannelClickEnabled
        ? (
            e: React.MouseEvent<HTMLDivElement>,
            channelId: string,
            timeSecs: number
          ) => {
            this.toast(
              `onChannelClick: channelId:${channelId} timeSecs:${timeSecs}`
            );
          }
        : undefined,
      onSignalDetectionContextMenu: this.state.isOnSignalDetectionContextMenuEnabled
      ? (
          e: React.MouseEvent<HTMLDivElement>,
          channelId: string,
          sdId?: string
        ) => {
          this.toast(
            `onSignalDetectionContextMenu: channelId:${channelId} sdId:${sdId}`
          );
        }
      : undefined,
      onSignalDetectionClick: this.state
        .isOnSignalDetectionClickEnabled
        ? (e: React.MouseEvent<HTMLDivElement>, sdId: string) => {
            this.toast(`onSignalDetectionClick: sdId:${sdId}`);
            this.setState({
              selectedSignalDetections: [sdId]
            });
          }
        : undefined,
      onSignalDetectionDragEnd: this.state
        .isOnSignalDetectionDragEndEnabled
        ? (sdId: string, timeSecs: number) => {
            this.toast(
              `onSignalDetectionDragEnd: sdId:${sdId} timeSecs:${timeSecs}`
            );
          }
        : undefined,
      onPredictivePhaseContextMenu: this.state.isOnPredictivePhaseContextMenuEnabled
        ? (
            e: React.MouseEvent<HTMLDivElement>,
            channelId: string,
            id?: string
          ) => {
            this.toast(
              `onPredictivePhaseContextMenu: channelId:${channelId} id:${id}`
            );
          }
        : undefined,
      onPredictivePhaseClick: this.state
          .isOnPredictivePhaseClickEnabled
          ? (e: React.MouseEvent<HTMLDivElement>, id: string) => {
              this.toast(`onPredictivePhaseClick: id:${id}`);
              this.setState({
                selectedPredictedPhases: [id]
              });
            }
          : undefined,
      onPredictivePhaseDragEnd: this.state
          .isOnPredictivePhaseDragEndEnabled
          ? (id: string, timeSecs: number) => {
              this.toast(
                `onPredictivePhaseDragEnd: id:${id} timeSecs:${timeSecs}`
              );
            }
          : undefined,
      onMaskClick: this.state.isOnMaskClickEnabled
        ? (
            event: React.MouseEvent<HTMLDivElement>,
            channelId: string,
            maskId: string[]
          ) => {
            this.toast(
              `onMaskClick: channelId:${channelId} maskId:${maskId}`
            );
          }
        : undefined,
      onMeasureWindowUpdated: this.state
        .isOnMeasureWindowUpdatedEnabled
        ? (
          isVisible: boolean, channelId?: string,
          mStartTimeSecs?: number, mEndTimeSecs?: number, heightPx?: number) => {
            this.toast(
              // tslint:disable-next-line:max-line-length
              `onMeasureWindowUpdated: isVisible:${isVisible} channelId:${channelId} startTimeSecs:${mStartTimeSecs} endTimeSecs:${mEndTimeSecs} heightPx:${heightPx}`
            );
          }
        : undefined,
      onUpdateMarker: this.state.isUpdateMarkersEnabled
        ? (channelId: string, marker: WeavessTypes.Marker) => {
            const markerStr = `channelId: ${channelId} :: ${moment.unix(marker.timeSecs)
              .utc()
              .format('DD MMM YYYY HH:mm:ss')}`;
            this.toast(
              `onUpdateMarker: marker:${markerStr}`
            );
          }
        : undefined,

      onUpdateSelectionWindow: this.state.isUpdateSelectionWindowsEnabled
        ? (channelId: string, selection: WeavessTypes.SelectionWindow) => {
            const selectionStr = `channelId: ${channelId} :: start: ${moment.unix(selection.startMarker.timeSecs)
              .utc()
              .format('DD MMM YYYY HH:mm:ss')} end: ${moment.unix(selection.endMarker.timeSecs)
                .utc()
                .format('DD MMM YYYY HH:mm:ss')}`;
            this.toast(
              `onUpdateSelectionWindow: selection:${selectionStr}`
            );
          }
        : undefined,

      onClickSelectionWindow: this.state.isOnClickSelectionWindowsEnabled
        ? (channelId: string, selection: WeavessTypes.SelectionWindow, timeSecs: number) => {
            this.toast(
              `onClickSelectionWindow: channelId: ${channelId} :: timeSecs: ${timeSecs}`
            );
          }
        : undefined,
    };

    const onKeyPress = this.state.isOnKeyPressEnabled
    ? (
        e: React.KeyboardEvent<HTMLDivElement>,
        clientX: number,
        clientY: number,
        channelId: string,
        timeSecs: number
      ) => {
        this.toast(
          // tslint:disable-next-line:max-line-length
          `onKeyPress: clientX:${clientX} clientY:${clientY} channelId:${channelId} timeSecs:${timeSecs}`
        );
      }
    : undefined;

    const events: WeavessTypes.Events = {
      stationEvents: {
        defaultChannelEvents: {
          labelEvents,
          events: waveformEvents,
          onKeyPress
        },
        nonDefaultChannelEvents: {
          labelEvents,
          events: waveformEvents,
          onKeyPress
        }
      },
      onUpdateMarker: this.state.isUpdateMarkersEnabled
        ? (marker: WeavessTypes.Marker) => {
            const markerStr = `${moment.unix(marker.timeSecs)
              .utc()
              .format('DD MMM YYYY HH:mm:ss')}`;
            this.toast(
              `onUpdateMarker: marker:${markerStr}`
            );
          }
        : undefined,

      onUpdateSelectionWindow: this.state.isUpdateSelectionWindowsEnabled
        ? (selection: WeavessTypes.SelectionWindow) => {
            const selectionStr = `start: ${moment.unix(selection.startMarker.timeSecs)
              .utc()
              .format('DD MMM YYYY HH:mm:ss')} end: ${moment.unix(selection.endMarker.timeSecs)
                .utc()
                .format('DD MMM YYYY HH:mm:ss')}`;
            this.toast(
              `onUpdateSelectionWindow: selection:${selectionStr}`
            );
          }
        : undefined,

      onClickSelectionWindow: this.state.isOnClickSelectionWindowsEnabled
        ? (selection: WeavessTypes.SelectionWindow, timeSecs: number) => {
            this.toast(
              `onClickSelectionWindow: timeSecs: ${timeSecs}`
            );
          }
        : undefined,
    };

    return (
      <div
        className={Classes.DARK}
        style={{
          height: '90%',
          width: '100%',
          padding: '0.5rem',
          color: Colors.GRAY4,
          display: 'flex',
          justifyContent: 'center',
          alignItems: 'center'
        }}
      >
        <Toaster
          position={Position.BOTTOM_LEFT}
          usePortal={false}
          ref={ref => {
            if (ref) {
              this.toaster = ref;
            }
          }}
        />
        <div
          className={Classes.DARK}
          style={{
            height: '100%',
            width: '100%',
          }}
        >
          <div
            style={{
              height: '100%',
              width: '100%',
              display: 'flex',
              flexDirection: 'column'
            }}
          >
            {this.props.showExampleControls ? (
              <div
                style={{
                  flex: '0 0 auto',
                  display: 'flex',
                  flexWrap: 'wrap',
                  justifyItems: 'left',
                  marginBottom: '0.5rem'
                }}
              >
                <div style={styleFlexItem}>
                <Checkbox
                  label={'OnChannelExpandedEnabled'}
                  onChange={() =>
                    this.setState({
                      isOnChannelExpandedEnabled: !this.state
                        .isOnChannelExpandedEnabled
                    })
                  }
                />
                </div>
                <div style={styleFlexItem}>
                <Checkbox
                  label={'OnChannelCollapsedEnabled'}
                  onChange={() =>
                    this.setState({
                      isOnChannelCollapsedEnabled: !this.state
                        .isOnChannelCollapsedEnabled
                    })
                  }
                />
                </div>
                <div style={styleFlexItem}>
                <Checkbox
                  label={'OnContextMenuEnabled'}
                  onChange={() =>
                    this.setState({
                      isOnContextMenuEnabled: !this.state
                        .isOnContextMenuEnabled
                    })
                  }
                />
                </div>
                <div style={styleFlexItem}>
                <Checkbox
                  label={'OnChannelLabelClickEnabled'}
                  onChange={() =>
                    this.setState({
                      isOnChannelLabelClickEnabled: !this.state
                        .isOnChannelLabelClickEnabled
                    })
                  }
                />
                </div>
                <div style={styleFlexItem}>
                <Checkbox
                  label={'OnChannelClickEnabled'}
                  onChange={() =>
                    this.setState({
                      isOnChannelClickEnabled: !this.state
                        .isOnChannelClickEnabled
                    })
                  }
                />
                </div>
                <div style={styleFlexItem}>
                <Checkbox
                  label={'OnSignalDetectionClickEnabled'}
                  onChange={() =>
                    this.setState({
                      isOnSignalDetectionClickEnabled: !this.state
                        .isOnSignalDetectionClickEnabled
                    })
                  }
                />
                </div>
                <div style={styleFlexItem}>
                <Checkbox
                  label={'OnSignalDetectionDragEndEnabled'}
                  onChange={() =>
                    this.setState({
                      isOnSignalDetectionDragEndEnabled: !this.state
                        .isOnSignalDetectionDragEndEnabled
                    })
                  }
                />
                </div>
                <div style={styleFlexItem}>
                <Checkbox
                  label={'OnSignalDetectionContextMenuEnabled'}
                  onChange={() =>
                    this.setState({
                      isOnSignalDetectionContextMenuEnabled: !this.state
                        .isOnSignalDetectionContextMenuEnabled
                    })
                  }
                />
                </div>
                <div style={styleFlexItem}>
                <Checkbox
                  label={'OnPredictivePhaseClickEnabled'}
                  onChange={() =>
                    this.setState({
                      isOnPredictivePhaseClickEnabled: !this.state
                        .isOnPredictivePhaseClickEnabled
                    })
                  }
                />
                </div>
                <div style={styleFlexItem}>
                <Checkbox
                  label={'OnPredictivePhaseDragEndEnabled'}
                  onChange={() =>
                    this.setState({
                      isOnPredictivePhaseDragEndEnabled: !this.state
                        .isOnPredictivePhaseDragEndEnabled
                    })
                  }
                />
                </div>
                <div style={styleFlexItem}>
                <Checkbox
                  label={'OnPredictivePhaseContextMenuEnabled'}
                  onChange={() =>
                    this.setState({
                      isOnPredictivePhaseContextMenuEnabled: !this.state
                        .isOnPredictivePhaseContextMenuEnabled
                    })
                  }
                />
                </div>
                <div style={styleFlexItem}>
                <Checkbox
                  label={'OnKeyPressEnabled'}
                  onChange={() =>
                    this.setState({
                      isOnKeyPressEnabled: !this.state
                        .isOnKeyPressEnabled
                    })
                  }
                />
                </div>
                <div style={styleFlexItem}>
                <Checkbox
                  label={'OnMaskClickEnabled'}
                  onChange={() =>
                    this.setState({
                      isOnMaskClickEnabled: !this.state
                        .isOnMaskClickEnabled
                    })
                  }
                />
                </div>
                <div style={styleFlexItem}>
                <Checkbox
                  label={'UpdateMarkersEnabled'}
                  onChange={() =>
                    this.setState({
                      isUpdateMarkersEnabled: !this.state
                        .isUpdateMarkersEnabled
                    })
                  }
                />
                <Checkbox
                  label={'UpdateSelectionWindowsEnabled'}
                  onChange={() =>
                    this.setState({
                      isUpdateSelectionWindowsEnabled: !this.state
                        .isUpdateSelectionWindowsEnabled
                    })
                  }
                />
                <Checkbox
                  label={'OnClickSelectionWindowsEnabled'}
                  onChange={() =>
                    this.setState({
                      isOnClickSelectionWindowsEnabled: !this.state
                        .isOnClickSelectionWindowsEnabled
                    })
                  }
                />
                </div>
                <div style={styleFlexItem}>
                <Checkbox
                  label={'OnMeasureWindowUpdated'}
                  onChange={() =>
                    this.setState({
                      isOnMeasureWindowUpdatedEnabled: !this.state
                        .isOnMeasureWindowUpdatedEnabled
                    })
                  }
                />
                </div>
              </div>
            ) : (
              undefined
            )}
            <div style={{...styleToolbar}} >
              <div style={{...styleToolbarItem}} >
                <Label inline={true} text="Offset Step Increment:">
                  <NumericInput
                    className={Classes.INPUT}
                    // allowNumericCharactersOnly={true}
                    buttonPosition="none"
                    value={this.state.offset}
                    onValueChange={this.onOffsetChange}
                    selectAllOnFocus={true}
                    // tslint:disable-next-line:no-magic-numbers
                    stepSize={1}
                    // tslint:disable-next-line:no-magic-numbers
                    minorStepSize={1}
                    majorStepSize={1}
                  />
                </Label>
              </div>
              <div style={{...styleToolbarItem}} >
                <Button
                  text="Measure Window"
                  onClick={ () => {
                      if (this.weavess) {
                        this.weavess.toggleMeasureWindowVisability();
                      }
                    }
                  }
                />
              </div>
              <div style={{...styleToolbarItem}} >
                <Button
                  text={this.state.toggleShowContent}
                  onClick={ () => {
                      if (this.weavess) {
                        this.weavess.toggleRenderingContent();
                        lodash.defer(() => this.setState({toggleShowContent: this.getToggleContentLabel()}));
                      }
                    }
                  }
                />
              </div>
            </div>
            <div
              style={{
                flex: '1 1 auto',
                position: 'relative'
              }}
            >
              <div
                style={{
                  position: 'absolute',
                  top: '0px',
                  bottom: '0px',
                  left: '0px',
                  right: '0px'
                }}
              >
                <Weavess
                  ref={ref => {
                    if (ref) {
                      this.weavess = ref;
                    }
                  }}
                  mode={WeavessTypes.Mode.DEFAULT}
                  startTimeSecs={WeavessExample.startTimeSecs}
                  endTimeSecs={WeavessExample.endTimeSecs}
                  stations={this.state.stations}
                  selections={{
                    channels: undefined,
                    signalDetections: this.state.selectedSignalDetections,
                    predictedPhases: this.state.selectedPredictedPhases
                  }}
                  events={events}
                  markers={{
                    verticalMarkers: [{
                      id: 'marker',
                      color: 'pink',
                      lineStyle: WeavessTypes.LineStyle.DASHED,
                      timeSecs: WeavessExample.startTimeSecs + 1200
                    }],
                    selectionWindows: [
                      {
                        id: 'selection',
                        startMarker: {
                          id: 'marker',
                          color: 'rgba(64, 255, 0, 1)',
                          lineStyle: WeavessTypes.LineStyle.DASHED,
                          timeSecs: WeavessExample.startTimeSecs + 600
                        },
                        endMarker: {
                          id: 'marker',
                          color: 'rgba(64, 255, 0, 1)',
                          lineStyle: WeavessTypes.LineStyle.DASHED,
                          timeSecs: WeavessExample.startTimeSecs + 800
                        },
                        isMoveable: true,
                        color: 'rgba(64, 255, 0, 0.2)'
                      },
                    ]
                  }}
                />
              </div>
            </div>
          </div>
        </div>
      </div>
    );
  }

  private readonly getToggleContentLabel = (): string => {
    if (this.weavess) {
      if (this.weavess.state.shouldRenderWaveforms && this.weavess.state.shouldRenderSpectrograms) {
        return 'Show only waveforms';
      } else if (this.weavess.state.shouldRenderWaveforms &&
        !this.weavess.state.shouldRenderSpectrograms) {
        return 'Show only spectrograms';
      } else {
        return 'Show waveforms and spectrograms';
      }
    }
    return 'Toggle Contnent';
  }

  private readonly generateDummyData = () => {
    const stations: WeavessTypes.Station[] = [];

    stations.push({
      id: 'test',
      name: 'test station',
      defaultChannel: {
        id: 'BHZ',
        name: 'BHZ',
        height: 50,
        waveform: {
          channelSegmentId: 'data',
          channelSegments: new Map<string, WeavessTypes.ChannelSegment>([
            [
              'data',
              {
                description: 'test waveform data',
                dataSegments: [
                  {
                    startTimeSecs: WeavessExample.startTimeSecs,
                    sampleRate: 1,
                    color: 'dodgerblue',
                    displayType: [WeavessTypes.DisplayType.LINE],
                    pointSize: 2,
                    data: Array.from({length: 300}, () =>
                      Math.floor(WeavessUtils.RandomNumber.getSecureRandomNumber() * 15)),
                  }
                ]
              }
            ]
          ]),
          signalDetections: [
            {
              id: `sd`,
              timeSecs: WeavessExample.startTimeSecs + 500,
              color: 'red',
              label: 'P',
              filter: 'brightness(1)'
            }
          ],
          predictedPhases: [
            {
              id: `predictive`,
              timeSecs: WeavessExample.startTimeSecs + 515,
              color: 'red',
              label: 'P',
              filter: 'opacity(.6)'
            }
          ],
          theoreticalPhaseWindows: [
            {
              id: 'theoretical-phase',
              startTimeSecs: WeavessExample.startTimeSecs + 60,
              endTimeSecs: WeavessExample.startTimeSecs + 120,
              color: 'red',
              label: 'TP'
            }
          ],
          markers: {
            verticalMarkers: [{
              id: 'marker',
              color: 'lime',
              lineStyle: WeavessTypes.LineStyle.DASHED,
              timeSecs: WeavessExample.startTimeSecs + 5
            }],
            moveableMarkers: [{
              id: 'marker',
              color: 'RED',
              lineStyle: WeavessTypes.LineStyle.DASHED,
              timeSecs: WeavessExample.startTimeSecs + 75,
              minTimeSecsConstraint: WeavessExample.startTimeSecs + 50,
              maxTimeSecsConstraint: WeavessExample.startTimeSecs + 100,
            }],
            selectionWindows: [
              {
                id: 'selection',
                startMarker: {
                  id: 'marker',
                  color: 'purple',
                  lineStyle: WeavessTypes.LineStyle.DASHED,
                  timeSecs: WeavessExample.startTimeSecs + 200
                },
                endMarker: {
                  id: 'marker',
                  color: 'purple',
                  lineStyle: WeavessTypes.LineStyle.DASHED,
                  timeSecs: WeavessExample.startTimeSecs + 400
                },
                isMoveable: false,
                color: 'rgba(200,0,0,0.2)'
              },
              {
                id: 'selection',
                startMarker: {
                  id: 'marker',
                  color: 'yellow',
                  lineStyle: WeavessTypes.LineStyle.DASHED,
                  timeSecs: WeavessExample.startTimeSecs + 280,
                  minTimeSecsConstraint: WeavessExample.startTimeSecs + 200,
                },
                endMarker: {
                  id: 'marker',
                  color: 'yellow',
                  lineStyle: WeavessTypes.LineStyle.DASHED,
                  timeSecs: WeavessExample.startTimeSecs + 320,
                  maxTimeSecsConstraint: WeavessExample.startTimeSecs + 400,
                },
                isMoveable: true,
                color: 'rgba(255,255,0,0.4)'
              },
            ]
          },
        },
      },
      nonDefaultChannels: [
        {
          id: 'BHE',
          name: 'BHE',
          height: 50,
          waveform: {
            channelSegmentId: 'data',
            channelSegments: new Map<string, WeavessTypes.ChannelSegment>([
              [
                'data',
                {
                  dataSegments: [
                    {
                      startTimeSecs: WeavessExample.startTimeSecs,
                      sampleRate: 1,
                      color: 'dodgerblue',
                      displayType: [WeavessTypes.DisplayType.SCATTER],
                      pointSize: 2,
                      data: Array.from({length: 300}, () =>
                        Math.floor(WeavessUtils.RandomNumber.getSecureRandomNumber() * 15)),
                    }
                  ]
                }
              ]
            ]),
            masks: [
              {
                id: `mask_1`,
                startTimeSecs: WeavessExample.startTimeSecs + 20,
                endTimeSecs: WeavessExample.startTimeSecs + 40,
                color: 'green'
              }
            ],
            markers: {
              verticalMarkers: [{
                id: 'marker',
                color: 'lime',
                lineStyle: WeavessTypes.LineStyle.DASHED,
                timeSecs: WeavessExample.startTimeSecs + 5
              }],
            },
          }
        }
      ]
    });

    stations.push({
      id: 'waveform spectrogram',
      name: 'waveform spectrogram',
      defaultChannel: {
        id: 'waveform spectrogram',
        name: 'waveform spectrogram',
        height: 100,
        waveform: {
          channelSegmentId: 'data',
          channelSegments: new Map<string, WeavessTypes.ChannelSegment>([
            [
              'data',
              {
                description: 'test waveform data',
                dataSegments: [
                  {
                    startTimeSecs: WeavessExample.startTimeSecs,
                    sampleRate: 1,
                    color: 'dodgerblue',
                    displayType: [WeavessTypes.DisplayType.LINE],
                    pointSize: 2,
                    data: Array.from({length: 300}, () =>
                      Math.floor(WeavessUtils.RandomNumber.getSecureRandomNumber() * 15)),
                  }
                ]
              }
            ]
          ]),
          signalDetections: [
            {
              id: `sd`,
              timeSecs: WeavessExample.startTimeSecs + 500,
              color: 'red',
              label: 'P',
              filter: 'brightness(1)'
            }
          ],
          predictedPhases: [
            {
              id: `predictive`,
              timeSecs: WeavessExample.startTimeSecs + 515,
              color: 'red',
              label: 'P',
              filter: 'opacity(.6)'
            }
          ],
          theoreticalPhaseWindows: [
            {
              id: 'theoretical-phase',
              startTimeSecs: WeavessExample.startTimeSecs + 60,
              endTimeSecs: WeavessExample.startTimeSecs + 120,
              color: 'red',
              label: 'TP'
            }
          ],
          markers: {
            verticalMarkers: [{
              id: 'marker',
              color: 'lime',
              lineStyle: WeavessTypes.LineStyle.DASHED,
              timeSecs: WeavessExample.startTimeSecs + 5
            }],
            moveableMarkers: [{
              id: 'marker',
              color: 'RED',
              lineStyle: WeavessTypes.LineStyle.DASHED,
              timeSecs: WeavessExample.startTimeSecs + 50
            }],
            selectionWindows: [
              {
                id: 'selection',
                startMarker: {
                  id: 'marker',
                  color: 'purple',
                  lineStyle: WeavessTypes.LineStyle.DASHED,
                  timeSecs: WeavessExample.startTimeSecs + 200
                },
                endMarker: {
                  id: 'marker',
                  color: 'purple',
                  lineStyle: WeavessTypes.LineStyle.DASHED,
                  timeSecs: WeavessExample.startTimeSecs + 400
                },
                isMoveable: true,
                color: 'rgba(200,0,0,0.2)'
              },
            ]
          },
        },
        spectrogram: {
          description: 'test spectogram data',
          descriptionLabelColor: 'black',
          startTimeSecs: WeavessExample.startTimeSecs,
          timeStep: rainbowSpectrogramTimeStep,
          frequencyStep: rainbowSpectrogramFrequencyStep,
          data: rainbowSpectrogramData,
          signalDetections: [
            {
              id: `sd`,
              timeSecs: WeavessExample.startTimeSecs + 500,
              color: 'red',
              label: 'P',
              filter: 'brightness(1)'
            }
          ],
          predictedPhases: [
            {
              id: `predictive`,
              timeSecs: WeavessExample.startTimeSecs + 515,
              color: 'red',
              label: 'P',
              filter: 'opacity(.6)'
            }
          ],
          theoreticalPhaseWindows: [
            {
              id: 'theoretical-phase',
              startTimeSecs: WeavessExample.startTimeSecs + 60,
              endTimeSecs: WeavessExample.startTimeSecs + 120,
              color: 'red',
              label: 'TP'
            }
          ],
          markers: {
            verticalMarkers: [{
              id: 'marker',
              color: 'lime',
              lineStyle: WeavessTypes.LineStyle.DASHED,
              timeSecs: WeavessExample.startTimeSecs + 5
            }],
            moveableMarkers: [{
              id: 'marker',
              color: 'RED',
              lineStyle: WeavessTypes.LineStyle.DASHED,
              timeSecs: WeavessExample.startTimeSecs + 50
            }],
            selectionWindows: [
              {
                id: 'selection',
                startMarker: {
                  id: 'marker',
                  color: 'purple',
                  lineStyle: WeavessTypes.LineStyle.DASHED,
                  timeSecs: WeavessExample.startTimeSecs + 200
                },
                endMarker: {
                  id: 'marker',
                  color: 'purple',
                  lineStyle: WeavessTypes.LineStyle.DASHED,
                  timeSecs: WeavessExample.startTimeSecs + 400
                },
                isMoveable: true,
                color: 'rgba(200,0,0,0.2)'
              },
            ]
          },
        }
      },
    });

    stations.push({
      id: 'waveform no data',
      name: 'waveform no data',
      defaultChannel: {
        id: 'waveform no data',
        name: 'waveform no data',
        height: 50,
        waveform: {
          channelSegmentId: 'data',
          channelSegments: new Map<string, WeavessTypes.ChannelSegment>([
            [
              'data',
              {
                description: 'test waveform no data',
                dataSegments: []
              }
            ]
          ]),
        }
      },
    });

    stations.push({
      id: 'spectrogram',
      name: 'spectrogram',
      defaultChannel: {
        id: 'spectrogram',
        name: 'spectrogram',
        height: 50,
        spectrogram: {
          description: 'test spectogram data',
          descriptionLabelColor: 'black',
          startTimeSecs: WeavessExample.startTimeSecs,
          timeStep: spectrogramTimeStep,
          frequencyStep: spectrogramFrequencyStep,
          data: spectrogramData,
          signalDetections: [
            {
              id: `sd`,
              timeSecs: WeavessExample.startTimeSecs + 500,
              color: 'red',
              label: 'P',
              filter: 'brightness(1)'
            }
          ],
          predictedPhases: [
            {
              id: `predictive`,
              timeSecs: WeavessExample.startTimeSecs + 515,
              color: 'red',
              label: 'P',
              filter: 'opacity(.6)'
            }
          ],
          theoreticalPhaseWindows: [
            {
              id: 'theoretical-phase',
              startTimeSecs: WeavessExample.startTimeSecs + 60,
              endTimeSecs: WeavessExample.startTimeSecs + 120,
              color: 'red',
              label: 'TP'
            }
          ],
          markers: {
            verticalMarkers: [{
              id: 'marker',
              color: 'lime',
              lineStyle: WeavessTypes.LineStyle.DASHED,
              timeSecs: WeavessExample.startTimeSecs + 5
            }],
            moveableMarkers: [{
              id: 'marker',
              color: 'RED',
              lineStyle: WeavessTypes.LineStyle.DASHED,
              timeSecs: WeavessExample.startTimeSecs + 50
            }],
            selectionWindows: [
              {
                id: 'selection',
                startMarker: {
                  id: 'marker',
                  color: 'purple',
                  lineStyle: WeavessTypes.LineStyle.DASHED,
                  timeSecs: WeavessExample.startTimeSecs + 200
                },
                endMarker: {
                  id: 'marker',
                  color: 'purple',
                  lineStyle: WeavessTypes.LineStyle.DASHED,
                  timeSecs: WeavessExample.startTimeSecs + 400
                },
                isMoveable: true,
                color: 'rgba(200,0,0,0.2)'
              },
            ]
          },
        }
      },
    });

    stations.push({
      id: 'spectrogram no data',
      name: 'spectrogram no data',
      defaultChannel: {
        id: 'spectrogram no data',
        name: 'spectrogram no data',
        height: 50,
        spectrogram: {
          description: 'test spectogram no data',
          startTimeSecs: WeavessExample.startTimeSecs,
          timeStep: 0,
          frequencyStep: 0,
          data: []
        }
      },
    });

    stations.push({
      id: 'no data',
      name: 'no data',
      defaultChannel: {
        id: 'no data',
        name: 'no data',
        height: 50,
      },
    });

    // create channels w/ random noise as data
    for (let i = 0; i < 50; i++) {
      const sampleData1 = new Float32Array(WeavessExample.NUM_SAMPLES);
      for (let samp = 0; samp < WeavessExample.NUM_SAMPLES; samp++) {
        sampleData1[samp] = Math.pow(WeavessUtils.RandomNumber.getSecureRandomNumber() - 0.5, 3) * 4;
      }

      const sampleData2 = new Float32Array(WeavessExample.NUM_SAMPLES);
      for (let samp = 0; samp < WeavessExample.NUM_SAMPLES; samp++) {
        sampleData2[samp] = Math.pow(WeavessUtils.RandomNumber.getSecureRandomNumber() - 0.5, 3) * 4;
      }

      stations.push({
        id: String(i),
        name: `station ${i}`,
        defaultChannel: {
          height: 50,
          id: String(i),
          name: `channel ${i}`,
          waveform: {
            channelSegmentId: 'data',
            channelSegments: new Map<string, WeavessTypes.ChannelSegment>([
              [
                'data',
                {
                  dataSegments: [
                    {
                      startTimeSecs: WeavessExample.startTimeSecs,
                      sampleRate: WeavessExample.SAMPLE_RATE,
                      color: 'dodgerblue',
                      displayType: [WeavessTypes.DisplayType.LINE],
                      pointSize: 2,
                      data: sampleData1,
                    },
                    {
                      startTimeSecs: WeavessExample.startTimeSecs + 900,
                      sampleRate: WeavessExample.SAMPLE_RATE,
                      color: 'dodgerblue',
                      displayType: [WeavessTypes.DisplayType.LINE],
                      pointSize: 2,
                      data: sampleData2,
                    }
                  ]
                }
              ]
            ]),
            signalDetections: [
              {
                id: `sd${i}`,
                timeSecs: WeavessExample.startTimeSecs + 450,
                color: 'red',
                label: 'P',
                filter: 'brightness(1)'
              }
            ],
            predictedPhases: [
              {
                id: `predictive${i}`,
                timeSecs: WeavessExample.startTimeSecs + 450,
                color: 'red',
                label: 'P',
                filter: 'opacity(.6)'
              }
            ],
            masks: [
              {
                id: `mask_1_${i}`,
                startTimeSecs: WeavessExample.startTimeSecs + 60,
                endTimeSecs: WeavessExample.startTimeSecs + 400,
                color: 'yellow'
              },
              {
                id: `mask_2_${i}`,
                startTimeSecs: WeavessExample.startTimeSecs + 100,
                endTimeSecs: WeavessExample.startTimeSecs + 200,
                color: 'green'
              }
            ],
            markers: {
              verticalMarkers: [{
                id: 'marker',
                color: 'lime',
                lineStyle: WeavessTypes.LineStyle.DASHED,
                timeSecs: WeavessExample.startTimeSecs + 5
              }],
            },
          }
        },
        nonDefaultChannels: []
      });
    }
    return stations;
  }

  private readonly onOffsetChange = (offset: number, str: string): void => {
    const stations = this.state.stations;
    for (let i = 0; i < stations.length; i++) {
      stations[i].defaultChannel.timeOffsetSeconds = offset * i;
    }
    this.setState(
      {
        offset,
        stations,
      });
  }

  private readonly toast = (message: string) => {
    if (this.toaster) {
      this.toaster.show({
        message,
        intent: Intent.PRIMARY,
        icon: IconNames.INFO_SIGN,
        timeout: 2000
      });
    }
  }
  // tslint:disable-next-line:max-file-line-count
}
