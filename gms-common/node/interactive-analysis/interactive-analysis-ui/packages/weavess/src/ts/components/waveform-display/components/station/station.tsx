import * as React from 'react';
import * as Entities from '../../../../entities';
import { DEFAULT_CHANNEL_HEIGHT_PIXELS } from '../../constants';
import { Channel } from './components';
import './styles.scss';
import { StationProps, StationState } from './types';

/**
 * Station Component. Contains channels, and optional events.
 */
export class Station extends React.PureComponent<StationProps, StationState> {

  /** The reference to the default channel. */
  public defaultChannelRef: Channel | null;

  /** The reference to the non-default channels. */
  public nonDefaultChannelRefs: { [id: string]: Channel | null } = {};

  /**
   * Constructor
   * 
   * @param props Station props as StationProps
   */
  public constructor(props: StationProps) {
    super(props);

    // check to see if there are any masks on the default channel or any of its non-default channels
    const showMaskIndicator =
        Boolean((this.props.station.nonDefaultChannels && this.props.station.nonDefaultChannels
            .map(channel => (channel.waveform &&
              (channel.waveform.masks !== undefined) && (channel.waveform.masks.length > 0)))
            .reduce((c1, c2) => c1 || c2, false)));

    this.state = {
        expanded: false,
        showMaskIndicator,
    };
  }

  // ******************************************
  // BEGIN REACT COMPONENT LIFECYCLE METHODS
  // ******************************************

  /**
   * Invoked right before calling the render method, both on the initial mount
   * and on subsequent updates. It should return an object to update the state,
   * or null to update nothing.
   *
   * @param nextProps the next props
   * @param prevState the previous state
   */
  public static getDerivedStateFromProps(nextProps: StationProps, prevState: StationState) {
    return null; /* no-op */
  }

  /**
   * Catches exceptions generated in descendant components. 
   * Unhandled exceptions will cause the entire component tree to unmount.
   * 
   * @param error the error that was caught
   * @param info the information about the error
   */
  public componentDidCatch(error, info) {
    // tslint:disable-next-line:no-console
    console.error(`Weavess Station Error: ${error} : ${info}`);
  }

  /**
   * Called immediately after a compoment is mounted. 
   * Setting state here will trigger re-rendering.
   */
  public componentDidMount() {
    /* no-op */
  }

  /**
   * Called immediately after updating occurs. Not called for the initial render.
   *
   * @param prevProps the previous props
   * @param prevState the previous state
   */
  public componentDidUpdate(prevProps: StationProps, prevState: StationState) {
    /* no-op */
  }

  /**
   * Called immediately before a component is destroyed. Perform any necessary 
   * cleanup in this method, such as cancelled network requests, 
   * or cleaning up any DOM elements created in componentDidMount.
   */
  public componentWillUnmount() {
    /* no-op */
  }

  // ******************************************
  // END REACT COMPONENT LIFECYCLE METHODS
  // ******************************************

  public render() {
    // calculate and determine the individual row heights
    const rowHeights: number[] = [];
    rowHeights.push(this.props.station.defaultChannel.height ||
        this.props.configuration.defaultChannelHeightPx || DEFAULT_CHANNEL_HEIGHT_PIXELS);

    if (this.props.station.nonDefaultChannels) {
      this.props.station.nonDefaultChannels.forEach(channel => {
        rowHeights.push(channel.height ||
            this.props.configuration.defaultChannelHeightPx || DEFAULT_CHANNEL_HEIGHT_PIXELS);
      });
    }

    const totalRowHeight = this.state.expanded && this.props.station.nonDefaultChannels ?
      rowHeights.map(rowHeight => rowHeight + 1)
        .reduce((a, b) => a + b, 0)
      : (rowHeights[0] + 1);

    const defaultChannelTimeOffsetSeconds = this.props.station.defaultChannel.timeOffsetSeconds || 0;

    return (
      <div
          className="station"
          style={{
              height: totalRowHeight,
          }}
      >
          <Channel // default channel
              key={`station-default-channel-${this.props.station.defaultChannel.id}`}
              ref={ref => this.defaultChannelRef = ref}
              index={0}
              height={rowHeights[0]}
              shouldRenderWaveforms={this.props.shouldRenderWaveforms}
              shouldRenderSpectrograms={this.props.shouldRenderSpectrograms}
              workerRpcs={this.props.workerRpcs}
              configuration={this.props.configuration}
              stationId={this.props.station.id}
              channel={this.mapChannelConfigToOffset(this.props.station.defaultChannel)}
              displayStartTimeSecs={this.props.displayStartTimeSecs}
              displayEndTimeSecs={this.props.displayEndTimeSecs}
              isDefaultChannel={true}
              isExpandable={
                !(!this.props.station.nonDefaultChannels ||
                  this.props.station.nonDefaultChannels.length === 0)}
              expanded={this.state.expanded}
              selections={this.props.selections}
              showMaskIndicator={this.state.showMaskIndicator}
              distance={this.props.station.distance ?
                    this.props.station.distance : 0}
              distanceUnits={this.props.station.distanceUnits ?
                  this.props.station.distanceUnits : Entities.DistanceUnits.degrees}
              events={this.props.events && this.props.events.defaultChannelEvents ?
                this.mapEventsToOffset(
                  this.props.station.defaultChannel,
                  this.props.events.defaultChannelEvents) : undefined}
              canvasRef={this.props.canvasRef}
              toast={this.props.toast}
              toggleExpansion={this.toggleExpansion}
              getViewRange={this.props.getViewRange}
              renderWaveforms={this.props.renderWaveforms}
              getCurrentViewRangeInSeconds={this.props.getCurrentViewRangeInSeconds}
              computeTimeSecsForMouseXPosition={this.props.computeTimeSecsForMouseXPosition}
              onMouseMove={
                (e: React.MouseEvent<HTMLDivElement>, xPct: number, timeSecs: number) =>
                  this.props.onMouseMove(e, xPct, timeSecs - defaultChannelTimeOffsetSeconds)}
              onMouseDown={(e: React.MouseEvent<HTMLDivElement>, xPct: number,
                  channelId: string, timeSecs: number, isDefaultChannel: boolean) =>
                this.props.onMouseDown(
                  e, xPct, channelId, timeSecs - defaultChannelTimeOffsetSeconds, isDefaultChannel)}
              onMouseUp={(e: React.MouseEvent<HTMLDivElement>, xPct: number,
                channelId: string, timeSecs: number, isDefaultChannel: boolean) =>
              this.props.onMouseUp(
                e, xPct, channelId, timeSecs - defaultChannelTimeOffsetSeconds, isDefaultChannel)}
              onContextMenu={this.props.onContextMenu}
              updateMeasureWindow={this.props.updateMeasureWindow ?
                (stationId: string, channel: Entities.Channel,
                 startTimeSecs: number, endTimeSecs: number,
                 isDefaultChannel: boolean, removeSelection: () => void) =>
                    this.updateMeasureWindow(
                      stationId, channel, startTimeSecs,
                      endTimeSecs, isDefaultChannel, removeSelection)
              : undefined}
          />
          {
            this.state.expanded && this.props.station.nonDefaultChannels ?
            (
              this.props.station.nonDefaultChannels.map((channelchannel, index) => {
                const timeOffsetSeconds = channelchannel.timeOffsetSeconds || 0;
                return (
                  <Channel // Channel (for non-default channels)
                    key={`station-nondefault-channel-${channelchannel.id}`}
                    ref={ref => this.nonDefaultChannelRefs[channelchannel.id] = ref}
                    index={(index + 1) * 2}
                    height={rowHeights[index + 1]}
                    shouldRenderWaveforms={this.props.shouldRenderWaveforms}
                    shouldRenderSpectrograms={this.props.shouldRenderSpectrograms}
                    workerRpcs={this.props.workerRpcs}
                    configuration={this.props.configuration}
                    stationId={this.props.station.id}
                    channel={this.mapChannelConfigToOffset(channelchannel)}
                    displayStartTimeSecs={this.props.displayStartTimeSecs}
                    displayEndTimeSecs={this.props.displayEndTimeSecs}
                    isDefaultChannel={false}
                    isExpandable={false}
                    expanded={false}
                    selections={this.props.selections}

                    showMaskIndicator={false}
                    distance={0}
                    distanceUnits={this.props.station.distanceUnits ?
                        this.props.station.distanceUnits : Entities.DistanceUnits.degrees}
                    events={this.props.events && this.props.events.nonDefaultChannelEvents ?
                      this.mapEventsToOffset(
                        channelchannel,
                        this.props.events.nonDefaultChannelEvents) : undefined}

                    canvasRef={this.props.canvasRef}
                    toast={this.props.toast}
                    toggleExpansion={this.toggleExpansion}
                    getViewRange={this.props.getViewRange}
                    renderWaveforms={this.props.renderWaveforms}
                    getCurrentViewRangeInSeconds={this.props.getCurrentViewRangeInSeconds}
                    computeTimeSecsForMouseXPosition={this.props.computeTimeSecsForMouseXPosition}
                    onMouseMove={
                      (e: React.MouseEvent<HTMLDivElement>, xPct: number, timeSecs: number) =>
                        this.props.onMouseMove(e, xPct, timeSecs - timeOffsetSeconds)}
                    onMouseDown={(e: React.MouseEvent<HTMLDivElement>, xPct: number,
                        channelId: string, timeSecs: number, isDefaultChannel: boolean) =>
                      this.props.onMouseDown(
                        e, xPct, channelId, timeSecs - timeOffsetSeconds, isDefaultChannel)}
                    onMouseUp={(e: React.MouseEvent<HTMLDivElement>, xPct: number,
                      channelId: string, timeSecs: number, isDefaultChannel: boolean) =>
                    this.props.onMouseUp(
                      e, xPct, channelId, timeSecs - timeOffsetSeconds, isDefaultChannel)}
                    onContextMenu={this.props.onContextMenu}
                    updateMeasureWindow={this.props.updateMeasureWindow ?
                      (stationId: string, channel: Entities.Channel,
                       startTimeSecs: number, endTimeSecs: number,
                       isDefaultChannel: boolean, removeSelection: () => void) =>
                          this.updateMeasureWindow(
                            stationId, channel, startTimeSecs,
                            endTimeSecs, isDefaultChannel, removeSelection)
                    : undefined}
                  />
                );
              }
              )
            )
            : []
          }
      </div>
    );
  }

  public resetAmplitude = () => {
    if (this.defaultChannelRef) {
      this.defaultChannelRef.resetAmplitude();
    }

    if (this.nonDefaultChannelRefs) {
      Object.keys(this.nonDefaultChannelRefs)
        .forEach(key => {
        const channel = this.nonDefaultChannelRefs[key];
        if (channel) {
          channel.resetAmplitude();
        }
      });
    }
  }

  /**
   * Updates the channels scroll position for the station. Forces the
   * label to always be in view and alighned to the left.
   */
  public readonly updateScrollPosition = (scrollWidth: number, scrollLeft: number) => {
    if (this.defaultChannelRef) {
      this.defaultChannelRef.updateScrollPosition(scrollWidth, scrollLeft);
    }

    if (this.nonDefaultChannelRefs) {
      Object.keys(this.nonDefaultChannelRefs)
        .forEach(key => {
        const channel = this.nonDefaultChannelRefs[key];
        if (channel) {
          channel.updateScrollPosition(scrollWidth, scrollLeft);
        }
      });
    }

    this.updateMaskLabels();
  }

  /**
   * Removes/hides the measure window selections for the station.
   */
  public removeMeasureWindowSelection = () => {
    if (this.defaultChannelRef) {
      this.defaultChannelRef.removeMeasureWindowSelection();
    }

    if (this.nonDefaultChannelRefs) {
      Object.keys(this.nonDefaultChannelRefs)
        .forEach(key => {
        const channel = this.nonDefaultChannelRefs[key];
        if (channel) {
          channel.removeMeasureWindowSelection();
        }
      });
    }
  }

  /**
   * Updates the measure window 
   * @param stationId station id being updated
   * @param channel the channel being updated
   * @param startTimeSecs startTime as epoch seconds
   * @param endTimeSecs endtime as epoch seconds
   * @param isDefaultChannel flag to know if default channel
   * @param removeSelection void function to remove the current selected channel
   */
  private readonly updateMeasureWindow = (stationId: string, channel: Entities.Channel,
    startTimeSecs: number, endTimeSecs: number,
    isDefaultChannel: boolean, removeSelection: () => void) => {
      const defaultChannelTimeOffsetSeconds = this.props.station.defaultChannel.timeOffsetSeconds || 0;

      if (this.props.updateMeasureWindow) {
        this.props.updateMeasureWindow(
          stationId, channel, startTimeSecs - defaultChannelTimeOffsetSeconds,
          endTimeSecs - defaultChannelTimeOffsetSeconds, isDefaultChannel, removeSelection);
      }
    }

  /**
   * Maps the channel data to the provided time offset in seconds.
   */
  // tslint:disable-next-line:cyclomatic-complexity
  private readonly mapChannelConfigToOffset = (channel: Entities.Channel): Entities.Channel => {
    if (!channel.timeOffsetSeconds) {
      return channel;
    }

    const timeOffsetSeconds = channel.timeOffsetSeconds;
    // map the time seconds to the offset time seconds
    const waveformChannelSegments: Map<string, Entities.ChannelSegment> = new Map();
    if (channel.waveform) {
      channel.waveform.channelSegments.forEach((value, key) => {
        waveformChannelSegments.set(
          key,
          {
            ...value,
            dataSegments: value.dataSegments.map(d =>
              ({
                ...d,
                startTimeSecs: d.startTimeSecs + timeOffsetSeconds
              }))
          }
        );
      });
    }

    const waveformMasks: Entities.Mask[] | undefined =
      channel.waveform && channel.waveform.masks ? channel.waveform.masks
      .map(m =>
        ({
          ...m,
          startTimeSecs: m.startTimeSecs + timeOffsetSeconds,
          endTimeSecs: m.endTimeSecs + timeOffsetSeconds
        }
      )) : undefined;

    const waveformSignalDetections: Entities.PickMarker[] | undefined =
      channel.waveform && channel.waveform.signalDetections ? channel.waveform.signalDetections
      .map(s =>
        ({
          ...s,
          timeSecs: s.timeSecs + timeOffsetSeconds
        }
      )) : undefined;

    const waveformPredictedPhases: Entities.PickMarker[] | undefined =
      channel.waveform && channel.waveform.predictedPhases ? channel.waveform.predictedPhases
      .map(p =>
        ({
          ...p,
          timeSecs: p.timeSecs + timeOffsetSeconds
        }
      )) : undefined;

    const waveformTheoreticalPhaseWindows: Entities.TheoreticalPhaseWindow[] | undefined =
      channel.waveform && channel.waveform.theoreticalPhaseWindows ? channel.waveform.theoreticalPhaseWindows
      .map(t =>
        ({
          ...t,
          startTimeSecs: t.startTimeSecs + timeOffsetSeconds,
          endTimeSecs: t.endTimeSecs + timeOffsetSeconds
        }
      )) : undefined;

    const waveformMarkers: Entities.Markers | undefined =
      channel.waveform && channel.waveform.markers ?
      {
        verticalMarkers: channel.waveform.markers.verticalMarkers ? channel.waveform.markers.verticalMarkers
          .map(v =>
            ({
              ...v,
              timeSecs: v.timeSecs + timeOffsetSeconds,
            }
          )) : undefined,

        moveableMarkers: channel.waveform.markers.moveableMarkers ? channel.waveform.markers.moveableMarkers
          .map(m =>
            ({
              ...m,
              timeSecs: m.timeSecs + timeOffsetSeconds,
            }
          )) : undefined,

        selectionWindows: channel.waveform.markers.selectionWindows ? channel.waveform.markers.selectionWindows
          .map(s =>
            ({
              ...s,
              startMarker: {
                ...s.startMarker,
                timeSecs: s.startMarker.timeSecs + timeOffsetSeconds,
                minTimeSecsConstraint: s.startMarker.minTimeSecsConstraint ?
                  s.startMarker.minTimeSecsConstraint + timeOffsetSeconds : s.startMarker.minTimeSecsConstraint,
                maxTimeSecsConstraint: s.startMarker.maxTimeSecsConstraint ?
                  s.startMarker.maxTimeSecsConstraint + timeOffsetSeconds : s.startMarker.maxTimeSecsConstraint,
              },
              endMarker: {
                ...s.endMarker,
                timeSecs: s.endMarker.timeSecs + timeOffsetSeconds,
                minTimeSecsConstraint: s.endMarker.minTimeSecsConstraint ?
                  s.endMarker.minTimeSecsConstraint + timeOffsetSeconds : s.endMarker.minTimeSecsConstraint,
                maxTimeSecsConstraint: s.endMarker.maxTimeSecsConstraint ?
                  s.endMarker.maxTimeSecsConstraint + timeOffsetSeconds : s.endMarker.maxTimeSecsConstraint,
              }
            }
          )) : undefined
      }
      : undefined;

    const waveform: Entities.ChannelWaveformContent | undefined = channel.waveform ? {
      ...channel.waveform,
      channelSegments: waveformChannelSegments,
      masks: waveformMasks,
      signalDetections: waveformSignalDetections,
      predictedPhases: waveformPredictedPhases,
      theoreticalPhaseWindows: waveformTheoreticalPhaseWindows,
      markers: waveformMarkers
    } : undefined;

    const spectrogramSignalDetections: Entities.PickMarker[] | undefined =
      channel.spectrogram && channel.spectrogram.signalDetections ? channel.spectrogram.signalDetections
      .map(s =>
        ({
          ...s,
          timeSecs: s.timeSecs + timeOffsetSeconds
        }
      )) : undefined;

    const spectrogramredictedPhases: Entities.PickMarker[] | undefined =
      channel.spectrogram && channel.spectrogram.predictedPhases ? channel.spectrogram.predictedPhases
      .map(p =>
        ({
          ...p,
          timeSecs: p.timeSecs + timeOffsetSeconds
        }
      )) : undefined;

    const spectrogramTheoreticalPhaseWindows: Entities.TheoreticalPhaseWindow[] | undefined =
      channel.spectrogram && channel.spectrogram.theoreticalPhaseWindows ? channel.spectrogram.theoreticalPhaseWindows
      .map(t =>
        ({
          ...t,
          startTimeSecs: t.startTimeSecs + timeOffsetSeconds,
          endTimeSecs: t.endTimeSecs + timeOffsetSeconds
        }
      )) : undefined;

    const spectrogramMarkers: Entities.Markers | undefined =
      channel.spectrogram && channel.spectrogram.markers ?
      {
        verticalMarkers: channel.spectrogram.markers.verticalMarkers ? channel.spectrogram.markers.verticalMarkers
          .map(v =>
            ({
              ...v,
              timeSecs: v.timeSecs + timeOffsetSeconds,
            }
          )) : undefined,

        moveableMarkers: channel.spectrogram.markers.moveableMarkers ? channel.spectrogram.markers.moveableMarkers
          .map(m =>
            ({
              ...m,
              timeSecs: m.timeSecs + timeOffsetSeconds,
            }
          )) : undefined,

        selectionWindows: channel.spectrogram.markers.selectionWindows ? channel.spectrogram.markers.selectionWindows
          .map(s =>
            ({
              ...s,
              startMarker: {
                ...s.startMarker,
                timeSecs: s.startMarker.timeSecs + timeOffsetSeconds,
                minTimeSecsConstraint: s.startMarker.minTimeSecsConstraint ?
                  s.startMarker.minTimeSecsConstraint + timeOffsetSeconds : s.startMarker.minTimeSecsConstraint,
                maxTimeSecsConstraint: s.startMarker.maxTimeSecsConstraint ?
                  s.startMarker.maxTimeSecsConstraint + timeOffsetSeconds : s.startMarker.maxTimeSecsConstraint,
              },
              endMarker: {
                ...s.endMarker,
                timeSecs: s.endMarker.timeSecs + timeOffsetSeconds,
                minTimeSecsConstraint: s.endMarker.minTimeSecsConstraint ?
                  s.endMarker.minTimeSecsConstraint + timeOffsetSeconds : s.endMarker.minTimeSecsConstraint,
                maxTimeSecsConstraint: s.endMarker.maxTimeSecsConstraint ?
                  s.endMarker.maxTimeSecsConstraint + timeOffsetSeconds : s.endMarker.maxTimeSecsConstraint,
              }
            }
          )) : undefined
      }
      : undefined;

    const spectrogram: Entities.ChannelSpectrogramContent | undefined = channel.spectrogram ? {
      ...channel.spectrogram,
      signalDetections: spectrogramSignalDetections,
      predictedPhases: spectrogramredictedPhases,
      theoreticalPhaseWindows: spectrogramTheoreticalPhaseWindows,
      markers: spectrogramMarkers
    } : undefined;

    const markers: Entities.Markers | undefined =
      channel && channel.markers ?
      {
        verticalMarkers: channel.markers.verticalMarkers ? channel.markers.verticalMarkers
          .map(v =>
            ({
              ...v,
              timeSecs: v.timeSecs + timeOffsetSeconds,
            }
          )) : undefined,

        moveableMarkers: channel.markers.moveableMarkers ? channel.markers.moveableMarkers
          .map(m =>
            ({
              ...m,
              timeSecs: m.timeSecs + timeOffsetSeconds,
            }
          )) : undefined,

        selectionWindows: channel.markers.selectionWindows ? channel.markers.selectionWindows
          .map(s =>
            ({
              ...s,
              startMarker: {
                ...s.startMarker,
                timeSecs: s.startMarker.timeSecs + timeOffsetSeconds,
                minTimeSecsConstraint: s.startMarker.minTimeSecsConstraint ?
                  s.startMarker.minTimeSecsConstraint + timeOffsetSeconds : s.startMarker.minTimeSecsConstraint,
                maxTimeSecsConstraint: s.startMarker.maxTimeSecsConstraint ?
                  s.startMarker.maxTimeSecsConstraint + timeOffsetSeconds : s.startMarker.maxTimeSecsConstraint,
              },
              endMarker: {
                ...s.endMarker,
                timeSecs: s.endMarker.timeSecs + timeOffsetSeconds,
                minTimeSecsConstraint: s.endMarker.minTimeSecsConstraint ?
                  s.endMarker.minTimeSecsConstraint + timeOffsetSeconds : s.endMarker.minTimeSecsConstraint,
                maxTimeSecsConstraint: s.endMarker.maxTimeSecsConstraint ?
                  s.endMarker.maxTimeSecsConstraint + timeOffsetSeconds : s.endMarker.maxTimeSecsConstraint,
              }
            }
          )) : undefined
      }
      : undefined;

    return {
      ...channel,
      waveform,
      spectrogram,
      markers
    };
  }

  /**
   * Maps the events to the real time from offset in seconds.
   */
  private readonly mapEventsToOffset = (
    channel: Entities.Channel,
    channelEvents: Entities.ChannelEvents): Entities.ChannelEvents => {
    if (!channel.timeOffsetSeconds) {
      return channelEvents;
    }

    const timeOffsetSeconds = channel.timeOffsetSeconds;

    return {
      labelEvents: channelEvents.labelEvents ? channelEvents.labelEvents : undefined,
      events: channelEvents.events ?
        {
          ...channelEvents.events,
          // map the time seconds back to the original time seconds
          onChannelClick:
            channelEvents.events && channelEvents.events.onChannelClick ?
            (e: React.MouseEvent<HTMLDivElement>, channelId: string, timeSecs: number) => {
              if (channelEvents.events && channelEvents.events.onChannelClick) {
                channelEvents.events.onChannelClick(e, channelId, timeSecs - timeOffsetSeconds);
              }
            } : undefined,

          onSignalDetectionDragEnd:
            channelEvents.events && channelEvents.events.onSignalDetectionDragEnd ?
            (sdId: string, timeSecs: number) => {
              if (channelEvents.events && channelEvents.events.onSignalDetectionDragEnd) {
                channelEvents.events.onSignalDetectionDragEnd(sdId, timeSecs - timeOffsetSeconds);
              }
            } : undefined,

          onPredictivePhaseDragEnd:
            channelEvents.events && channelEvents.events.onPredictivePhaseDragEnd ?
            (id: string, timeSecs: number) => {
              if (channelEvents.events && channelEvents.events.onPredictivePhaseDragEnd) {
                channelEvents.events.onPredictivePhaseDragEnd(id, timeSecs - timeOffsetSeconds);
              }
            } : undefined,

          onMaskCreateDragEnd:
            channelEvents.events && channelEvents.events.onMaskCreateDragEnd ?
            (e: React.MouseEvent<HTMLDivElement>, startTimeSecs: number,
              endTimeSecs: number, needToDeselect: boolean) => {
              if (channelEvents.events && channelEvents.events.onMaskCreateDragEnd) {
                channelEvents.events.onMaskCreateDragEnd(
                  e, startTimeSecs - timeOffsetSeconds, endTimeSecs - timeOffsetSeconds, needToDeselect);
                }
            } : undefined,

          onMeasureWindowUpdated:
            channelEvents.events && channelEvents.events.onMeasureWindowUpdated ?
            // tslint:disable-next-line:max-line-length
            (isVisible: boolean, channelId?: string, startTimeSecs?: number, endTimeSecs?: number, heightPx?: number) => {
              if (channelEvents.events && channelEvents.events.onMeasureWindowUpdated) {
                channelEvents.events.onMeasureWindowUpdated(
                  isVisible, channelId, startTimeSecs ? startTimeSecs - timeOffsetSeconds : undefined,
                  endTimeSecs ? endTimeSecs - timeOffsetSeconds : undefined, heightPx);
              }
            } : undefined,

          onUpdateMarker:
            channelEvents.events && channelEvents.events.onUpdateMarker ?
            // tslint:disable-next-line:max-line-length
            (channelId: string, marker: Entities.Marker) => {
              if (channelEvents.events && channelEvents.events.onUpdateMarker) {
                channelEvents.events.onUpdateMarker(
                  channelId,
                  {
                    ...marker,
                    timeSecs: marker.timeSecs - timeOffsetSeconds
                  });
              }
            } : undefined,

          onUpdateSelectionWindow:
              channelEvents.events && channelEvents.events.onUpdateSelectionWindow ?
              // tslint:disable-next-line:max-line-length
              (channelId: string, s: Entities.SelectionWindow) => {
                if (channelEvents.events && channelEvents.events.onUpdateSelectionWindow) {
                  channelEvents.events.onUpdateSelectionWindow(
                    channelId,
                    {
                      ...s,
                      startMarker: {
                        ...s.startMarker,
                        timeSecs: s.startMarker.timeSecs - timeOffsetSeconds,
                        minTimeSecsConstraint: s.startMarker.minTimeSecsConstraint ?
                          s.startMarker.minTimeSecsConstraint - timeOffsetSeconds : s.startMarker.minTimeSecsConstraint,
                        maxTimeSecsConstraint: s.startMarker.maxTimeSecsConstraint ?
                          s.startMarker.maxTimeSecsConstraint - timeOffsetSeconds : s.startMarker.maxTimeSecsConstraint,
                      },
                      endMarker: {
                        ...s.endMarker,
                        timeSecs: s.endMarker.timeSecs + timeOffsetSeconds,
                        minTimeSecsConstraint: s.endMarker.minTimeSecsConstraint ?
                          s.endMarker.minTimeSecsConstraint - timeOffsetSeconds : s.endMarker.minTimeSecsConstraint,
                        maxTimeSecsConstraint: s.endMarker.maxTimeSecsConstraint ?
                          s.endMarker.maxTimeSecsConstraint - timeOffsetSeconds : s.endMarker.maxTimeSecsConstraint,
                      }
                    });
                }
              } : undefined,

          onClickSelectionWindow:
            channelEvents.events && channelEvents.events.onClickSelectionWindow ?
            // tslint:disable-next-line:max-line-length
            (channelId: string, s: Entities.SelectionWindow, timeSecs: number) => {
              if (channelEvents.events && channelEvents.events.onClickSelectionWindow) {
                channelEvents.events.onClickSelectionWindow(
                  channelId,
                  {
                    ...s,
                    startMarker: {
                      ...s.startMarker,
                      timeSecs: s.startMarker.timeSecs - timeOffsetSeconds,
                      minTimeSecsConstraint: s.startMarker.minTimeSecsConstraint ?
                        s.startMarker.minTimeSecsConstraint - timeOffsetSeconds : s.startMarker.minTimeSecsConstraint,
                      maxTimeSecsConstraint: s.startMarker.maxTimeSecsConstraint ?
                        s.startMarker.maxTimeSecsConstraint - timeOffsetSeconds : s.startMarker.maxTimeSecsConstraint,
                    },
                    endMarker: {
                      ...s.endMarker,
                      timeSecs: s.endMarker.timeSecs + timeOffsetSeconds,
                      minTimeSecsConstraint: s.endMarker.minTimeSecsConstraint ?
                        s.endMarker.minTimeSecsConstraint - timeOffsetSeconds : s.endMarker.minTimeSecsConstraint,
                      maxTimeSecsConstraint: s.endMarker.maxTimeSecsConstraint ?
                        s.endMarker.maxTimeSecsConstraint - timeOffsetSeconds : s.endMarker.maxTimeSecsConstraint,
                    }
                  },
                  timeSecs - timeOffsetSeconds);
              }
            } : undefined,

          } : undefined,
      // tslint erroneously assumes this will cause problems
      // tslint:disable:no-unbound-method
      onKeyPress: channelEvents.onKeyPress,
    };
  }

  /**
   * Toggle the expansion state for a given stationId
   */
  private readonly toggleExpansion = () => {
      this.setState(
        {
          expanded: !this.state.expanded
        },
        () => {
          this.props.renderWaveforms();
        }
    );
  }

  /**
   * Update the mask labels based on the viewing area.
   */
  private readonly updateMaskLabels = () => {
    // update the mask labels (Red M) to be display only if the mask is within the viewing area
    if (this.defaultChannelRef) {
      const durationSecs = this.props.displayEndTimeSecs - this.props.displayStartTimeSecs;
      const axisStart = this.props.displayStartTimeSecs + (durationSecs * this.props.getViewRange()[0]);
      const axisEnd = this.props.displayStartTimeSecs + (durationSecs * this.props.getViewRange()[1]);
      // check to see if there are any masks on the default
      // channel or any of its non-default channels
      const showMaskIndicator =
          Boolean((this.props.station.nonDefaultChannels &&
              this.props.station.nonDefaultChannels
                  .map(channel => (channel.waveform &&
                      (channel.waveform.masks !== undefined) && (channel.waveform.masks.length > 0)) &&
                      // check to see if any of the masks are in the viewing area
                      (channel.waveform.masks.some(mask =>
                          (mask.startTimeSecs <= axisEnd &&
                              mask.endTimeSecs >= axisStart))))
                  .reduce((c1, c2) => c1 || c2, false)));

      if (showMaskIndicator !== this.state.showMaskIndicator) {
        this.setState({
              showMaskIndicator
        });
      }
    }
  }
}
