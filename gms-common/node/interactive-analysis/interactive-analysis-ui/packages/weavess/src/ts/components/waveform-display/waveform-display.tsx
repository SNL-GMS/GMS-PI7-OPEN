import { IconName, Intent, NonIdealState, Position, Toaster } from '@blueprintjs/core';
import { IconNames } from '@blueprintjs/icons';
import * as lodash from 'lodash';
import * as React from 'react';
import * as Entities from '../../entities';
import { getConfiguration } from './configuration';
import './style.scss';
import { WaveformDisplayProps, WaveformDisplayState } from './types';
import { WaveformPanel } from './waveform-panel';

/**
 * Parent container for weavess. Contains a Waveform Panel for the main display
 * and the measure window. 
 */
export class WaveformDisplay extends React.PureComponent<WaveformDisplayProps, WaveformDisplayState> {

  /** Reference to the waveform panel. */
  public waveformPanelRef: WaveformPanel | null;

  /** Reference to the measure window container. */
  public measureWindowContainerRef: HTMLDivElement | null;

  /** Reference to the measure window panel. */
  public measureWindowPanelRef: WaveformPanel | null;

  /** Toaster: For Notifications */
  public toaster: Toaster;

  /**
   * Constructor
   * 
   * @param props Waveform Display props as WaveformDisplayProps
   */
  public constructor(props: WaveformDisplayProps) {
    super(props);
    const configuration = getConfiguration(this.props.configuration);
    this.state = {
      configuration,
      mode: Entities.Mode.DEFAULT,
      showMeasureWindow: false,
      measureWindowHeightPx: 200,
      measureWindowSelection: undefined,
      prevMeasureWindowSelectionFromProps: undefined,
      shouldRenderWaveforms: configuration.shouldRenderWaveforms,
      shouldRenderSpectrograms: configuration.shouldRenderSpectrograms,
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
  public static getDerivedStateFromProps(nextProps: WaveformDisplayProps, prevState: WaveformDisplayState) {
    let derivedState = {};
    let hasStateChanged = false;

    // check if the mode has changed; if so update the state
    // if the mode is 'MEASUREMENT' -> show the measure window by default
    if (nextProps.mode !== prevState.mode) {
      derivedState = {
        ...derivedState,
        mode: nextProps.mode,
        showMeasureWindow: nextProps.mode === Entities.Mode.MEASUREMENT
      };
    }

    // if the measure window is visible; update the measure window selection with the
    // new props coming into the waveform display component
    if (prevState.showMeasureWindow && prevState.measureWindowSelection) {
      const prevStation = prevState.measureWindowSelection;
      const newStation = nextProps.stations.find(station => station.id === prevStation.stationId);
      let updatedChannel;
      if (newStation) {
        if (newStation.defaultChannel.id === prevState.measureWindowSelection.channel.id) {
          updatedChannel = newStation.defaultChannel;
        } else {
          if (newStation.nonDefaultChannels) {
            updatedChannel = newStation.nonDefaultChannels.find(channel => channel.id === prevStation.channel.id);
          }
        }
      }
      derivedState = {
        ...derivedState,
        measureWindowSelection: updatedChannel ? {
          ...prevState.measureWindowSelection,
          channel: {...updatedChannel},
        } : undefined
      };
      hasStateChanged = true;
    }

    // check if the props specify and define the measure window selection
    if (!lodash.isEqual(nextProps.measureWindowSelection, prevState.prevMeasureWindowSelectionFromProps) ||
      lodash.isEqual(prevState.measureWindowSelection, prevState.prevMeasureWindowSelectionFromProps)
    ) {

      // clear out any existing measure window selection
      if (prevState.measureWindowSelection && prevState.measureWindowSelection.removeSelection) {
        prevState.measureWindowSelection.removeSelection();
      }

      derivedState = {
        ...derivedState,
        measureWindowSelection: nextProps.measureWindowSelection,
        prevMeasureWindowSelectionFromProps: nextProps.measureWindowSelection
      };
    }

    // update the configuartion from the updated props
    const configuration = getConfiguration(nextProps.configuration);
    if (lodash.isEqual(nextProps.configuration, configuration)) {
      derivedState = {
        ...derivedState,
        configuration,
      };
      hasStateChanged = true;
    }

    if (hasStateChanged) {
      return derivedState;
    }
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
    console.error(`Waveform Display Error: ${error} : ${info}`);
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
  public componentDidUpdate(prevProps: WaveformDisplayProps, prevState: WaveformDisplayState) {
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

  // tslint:disable-next-line:cyclomatic-complexity
  public render() {

    // calculate the min and max time offsets of all the channels
    const minOffset = Math.min(
      ...this.props.stations.map(s =>
      s.defaultChannel.timeOffsetSeconds ? s.defaultChannel.timeOffsetSeconds : 0),
      ...this.props.stations.map(s => s.nonDefaultChannels ?
        Math.min(...s.nonDefaultChannels.map(c => c.timeOffsetSeconds ? c.timeOffsetSeconds : 0))
          : 0));

    const maxOffset = Math.max(
      ...this.props.stations.map(s =>
      s.defaultChannel.timeOffsetSeconds ? s.defaultChannel.timeOffsetSeconds : 0),
      ...this.props.stations.map(s => s.nonDefaultChannels ?
        Math.max(...s.nonDefaultChannels.map(c => c.timeOffsetSeconds ? c.timeOffsetSeconds : 0))
          : 0));

    return (
      <div
        className="weavess"
      >
        {
          (this.state.showMeasureWindow) ?
            (
            <div
              className="weavess-measure-window"
              ref={ref => this.measureWindowContainerRef = ref}
              style={{
                height: `${this.state.measureWindowHeightPx}px`,
              }}
            >
              {
                (this.state.measureWindowSelection) ?
                (
                  <WaveformPanel
                    ref={ref => this.measureWindowPanelRef = ref}
                    {...this.props}
                    events={{
                      ...this.props.events,
                      stationEvents: {
                        defaultChannelEvents:
                          (this.state.measureWindowSelection.isDefaultChannel) ?
                            (this.props.events.stationEvents) ?
                              this.props.events.stationEvents.defaultChannelEvents : undefined :
                            (this.props.events.stationEvents) ?
                              this.props.events.stationEvents.nonDefaultChannelEvents : undefined,
                      }
                    }}
                    configuration={{
                      ...this.state.configuration,
                      defaultChannel: {
                        disableMeasureWindow: true,
                        disableSignalDetectionModification:
                          (this.state.measureWindowSelection.isDefaultChannel) ?
                          this.props.configuration && this.props.configuration.defaultChannel &&
                            this.props.configuration.defaultChannel.disableSignalDetectionModification :
                          this.props.configuration && this.props.configuration.nonDefaultChannel &&
                            this.props.configuration.nonDefaultChannel.disableSignalDetectionModification,
                        disableMaskModification:
                        (this.state.measureWindowSelection.isDefaultChannel) ?
                          this.props.configuration && this.props.configuration.defaultChannel &&
                            this.props.configuration.defaultChannel.disableMaskModification :
                          this.props.configuration && this.props.configuration.nonDefaultChannel &&
                            this.props.configuration.nonDefaultChannel.disableMaskModification,
                      },
                      // use the custom label for the measure window
                      customLabel: this.props.configuration ?
                        this.props.configuration.customMeasureWindowLabel : undefined
                    }}
                    toast={this.toast}
                    stations={[{
                      id: this.state.measureWindowSelection.stationId,
                      name: ``,
                      defaultChannel: {
                        ...this.state.measureWindowSelection.channel,
                        timeOffsetSeconds: 0, // always show true time in the measure window
                        // tslint:disable-next-line:no-magic-numbers
                        height: this.state.measureWindowHeightPx - 55,
                      }
                    }]}
                    startTimeSecs={this.state.measureWindowSelection.startTimeSecs}
                    endTimeSecs={this.state.measureWindowSelection.endTimeSecs}
                    defaultZoomWindow={{
                      startTimeSecs: this.state.measureWindowSelection.startTimeSecs,
                      endTimeSecs: this.state.measureWindowSelection.endTimeSecs
                    }}
                    initialZoomWindow={{
                      startTimeSecs: this.state.measureWindowSelection.startTimeSecs,
                      endTimeSecs: this.state.measureWindowSelection.endTimeSecs
                    }}
                    shouldRenderWaveforms={this.state.shouldRenderWaveforms}
                    shouldRenderSpectrograms={this.state.shouldRenderSpectrograms}
                  />
                )
              :
                (
                  <NonIdealState
                    visual="timeline-line-chart"
                    title="No Measure Window Data Selected"
                  />
                )
              }
              <div
                className="weavess-measure-window-divider"
                onMouseDown={this.onMeasureWindowDividerDrag}
              >
                <div className="weavess-measure-window-divider-div" />
              </div>
            </div>)
            :
            undefined
        }
        <div
          className="weavess-waveform-display"
          style={{
            height: (this.state.showMeasureWindow) ? `calc(100% - ${this.state.measureWindowHeightPx}px)` : '100%'
          }}
        >
          <WaveformPanel
            ref={ref => this.waveformPanelRef = ref}
            {...this.props}
            configuration={this.state.configuration}
            toast={this.toast}
            updateMeasureWindow={this.updateMeasureWindow}
            startTimeSecs={this.props.startTimeSecs + minOffset}
            endTimeSecs={this.props.endTimeSecs + maxOffset}
            shouldRenderWaveforms={this.state.shouldRenderWaveforms}
            shouldRenderSpectrograms={this.state.shouldRenderSpectrograms}
          />
        </div>
        <Toaster
          usePortal={false}
          position={Position.BOTTOM_RIGHT}
          canEscapeKeyClear={true}
          ref={ref => {
            if (ref) {
              this.toaster = ref;
            }
          }}
        />
      </div>);
  }

  /**
   * Exposed primarily for non-react users.
   */
  public refresh = (): void => {
    if (!this.waveformPanelRef) {
      return;
    }
    this.waveformPanelRef.refresh();
    if (this.measureWindowPanelRef) {
      this.measureWindowPanelRef.refresh();
    }
  }

  /**
   * Returns the currently displayed viewTimeInterval
   * The start time seconds and end time seconds of the 
   * currently displayed view of the waveforms.
   * 
   * @returns the current viewable timerange
   */
  public getCurrentViewRangeInSeconds = (): Entities.TimeRange => {
    if (!this.waveformPanelRef) {
      return {
        startTimeSecs: 0,
        endTimeSecs: 0
      };
    }
    return this.waveformPanelRef.getCurrentViewRangeInSeconds();
  }

  /**
   * Returns true if the measure window is visible; false otherwise.
   * 
   * @returns true if visible; false otherwise
   */
  public isMeasureWindowVisible = (): boolean => this.state.showMeasureWindow;

  /**
   * Removes the selection div that spans all stations
   */
  public clearBrushStroke = () => {
    if (!this.waveformPanelRef) {
      return;
    }
    this.waveformPanelRef.clearBrushStroke();
    if (this.measureWindowPanelRef) {
      this.measureWindowPanelRef.clearBrushStroke();
    }
  }

  /**
   * Toggle the measure window visiblilty.
   */
  public toggleMeasureWindowVisability = () => {
    if (this.state.measureWindowSelection) {
      if (this.state.measureWindowSelection.removeSelection) {
        this.state.measureWindowSelection.removeSelection();
      }
    }

    if (this.state.measureWindowSelection &&
        this.state.measureWindowSelection.isDefaultChannel) {
      if (this.props.events && this.props.events.stationEvents
        && this.props.events.stationEvents.defaultChannelEvents
        && this.props.events.stationEvents.defaultChannelEvents.events
        && this.props.events.stationEvents.defaultChannelEvents.events.onMeasureWindowUpdated) {
          this.props.events.stationEvents.defaultChannelEvents.events.onMeasureWindowUpdated(
            !this.state.showMeasureWindow
          );
      }
    } else {
      if (this.props.events && this.props.events.stationEvents
        && this.props.events.stationEvents.nonDefaultChannelEvents
        && this.props.events.stationEvents.nonDefaultChannelEvents.events
        && this.props.events.stationEvents.nonDefaultChannelEvents.events.onMeasureWindowUpdated) {
          this.props.events.stationEvents.nonDefaultChannelEvents.events.onMeasureWindowUpdated(
            !this.state.showMeasureWindow
          );
      }
    }

    this.setState({
      showMeasureWindow: !this.state.showMeasureWindow,
      measureWindowSelection: undefined
    });
  }

  /**
   * Zooms to the provided time range  [startTimeSecs, endTimeSecs].
   * 
   * @param startTimeSecs the start time in seconds
   * @param endTimeSecs the end time in seconds
   */
  public readonly zoomToTimeWindow = (startTimeSecs: number, endTimeSecs: number) => {
    if (this.waveformPanelRef) {
      this.waveformPanelRef.zoomToTimeWindow(startTimeSecs, endTimeSecs);
    }
  }

  /** 
   * Toggles whether or not waveforms or spectrograms shold be rendered
   * 
   * Toggle Order (repeat): 
   *   * render: waveforms and spectrograms
   *   * render: waveforms 
   *   * render: spectrograms
   */
  public toggleRenderingContent = () => {
    if (this.state.shouldRenderWaveforms && this.state.shouldRenderSpectrograms) {
      this.setState({
        shouldRenderWaveforms: true,
        shouldRenderSpectrograms: false
      });
    } else if (this.state.shouldRenderWaveforms && !this.state.shouldRenderSpectrograms) {
      this.setState({
        shouldRenderWaveforms: false,
        shouldRenderSpectrograms: true
      });
    } else {
      this.setState({
        shouldRenderWaveforms: true,
        shouldRenderSpectrograms: true
      });
    }
  }

  /** Toggles whether or not waveforms should be rendered */
  public toggleShouldRenderWaveforms = () => {
    this.setState({
      shouldRenderWaveforms: !this.state.shouldRenderWaveforms
    });
  }

  /** Toggles whether or not spectrograms should be rendered */
  public toggleShouldRenderSpectrograms = () => {
    this.setState({
      shouldRenderSpectrograms: !this.state.shouldRenderSpectrograms
    });
  }

  /**
   * Display a toast message.
   */
  public readonly toast = (message: string,
    intent?: Intent, icon?: IconName, timeout?: number) => {
    if (this.toaster) {
      // TODO check for unique message
      if (this.toaster.getToasts().length === 0) {
        this.toaster.show({
          message,
          intent: ((intent) ? intent : Intent.NONE),
          icon: ((icon) ? icon : IconNames.INFO_SIGN),
          // tslint:disable-next-line:no-magic-numbers
          timeout: ((!timeout) ? 4000 : timeout)
        });
      }
    }
  }

  /**
   * Display a INFO toast message.
   */
  public readonly toastInfo = (message: string, timeout?: number) => {
      this.toast(message, Intent.NONE, IconNames.INFO_SIGN, timeout);
  }

  /**
   * Display a WARNING toast message.
   */
  public readonly toastWarn = (message: string, timeout?: number) => {
    this.toast(message, Intent.WARNING, IconNames.WARNING_SIGN, timeout);
  }

  /**
   * Display a ERROR toast message.
   */
  public readonly toastError = (message: string, timeout?: number) => {
    this.toast(message, Intent.DANGER, IconNames.ERROR, timeout);
  }

  /**
   * Update measure window
   */
  private readonly updateMeasureWindow = (
    stationId: string, channel: Entities.Channel,
    startTimeSecs: number, endTimeSecs: number,
    isDefaultChannel: boolean, removeSelection: () => void) => {

    if (this.state.measureWindowSelection &&
      this.state.measureWindowSelection.channel.id !== channel.id) {
      if (this.state.measureWindowSelection.removeSelection) {
        this.state.measureWindowSelection.removeSelection();
      }
    }

    this.setState(
      {
        showMeasureWindow: true,
        measureWindowSelection: {
          stationId,
          channel,
          startTimeSecs,
          endTimeSecs,
          isDefaultChannel,
          removeSelection
        }
      }
    );

    if (isDefaultChannel) {
      if (this.props.events && this.props.events.stationEvents
        && this.props.events.stationEvents.defaultChannelEvents
        && this.props.events.stationEvents.defaultChannelEvents.events
        && this.props.events.stationEvents.defaultChannelEvents.events.onMeasureWindowUpdated) {
          this.props.events.stationEvents.defaultChannelEvents.events.onMeasureWindowUpdated(
            true,
            channel.id,
            startTimeSecs,
            endTimeSecs,
            this.state.measureWindowHeightPx
          );
      }
    } else {
      if (this.props.events && this.props.events.stationEvents
        && this.props.events.stationEvents.nonDefaultChannelEvents
        && this.props.events.stationEvents.nonDefaultChannelEvents.events
        && this.props.events.stationEvents.nonDefaultChannelEvents.events.onMeasureWindowUpdated) {
          this.props.events.stationEvents.nonDefaultChannelEvents.events.onMeasureWindowUpdated(
            true,
            channel.id,
            startTimeSecs,
            endTimeSecs,
            this.state.measureWindowHeightPx
          );
      }
    }
  }

  /**
   * Update size of the measure window
   */
  private readonly onMeasureWindowDividerDrag = (e: React.MouseEvent<HTMLDivElement>) => {
    let prevPosition = e.clientY;
    let currentPos = e.clientY;
    let diff = 0;
    const minHeightPx = 200;
    const maxHeightPx = 500;
    const onMouseMove = (e2: MouseEvent) => {
      if (this.measureWindowContainerRef) {
        currentPos = e2.clientY;
        diff = currentPos - prevPosition;
        prevPosition = currentPos;
        const heightPx = this.measureWindowContainerRef.clientHeight + diff;
        if (heightPx >= minHeightPx && heightPx <= maxHeightPx) {
          this.setState({
            measureWindowHeightPx: heightPx
          });
        }
      }
    };

    const onMouseUp = (e2: MouseEvent) => {
      document.body.removeEventListener('mousemove', onMouseMove);
      document.body.removeEventListener('mouseup', onMouseUp);

      if (this.state.measureWindowSelection) {
        if (this.state.measureWindowSelection.isDefaultChannel) {
          if (this.props.events && this.props.events.stationEvents
            && this.props.events.stationEvents.defaultChannelEvents
            && this.props.events.stationEvents.defaultChannelEvents.events
            && this.props.events.stationEvents.defaultChannelEvents.events.onMeasureWindowUpdated) {
              this.props.events.stationEvents.defaultChannelEvents.events.onMeasureWindowUpdated(
                true,
                this.state.measureWindowSelection.channel.id,
                this.state.measureWindowSelection.startTimeSecs,
                this.state.measureWindowSelection.endTimeSecs,
                this.state.measureWindowHeightPx
              );
          }
        } else {
          if (this.props.events && this.props.events.stationEvents
            && this.props.events.stationEvents.nonDefaultChannelEvents
            && this.props.events.stationEvents.nonDefaultChannelEvents.events
            && this.props.events.stationEvents.nonDefaultChannelEvents.events.onMeasureWindowUpdated) {
              this.props.events.stationEvents.nonDefaultChannelEvents.events.onMeasureWindowUpdated(
                true,
                this.state.measureWindowSelection.channel.id,
                this.state.measureWindowSelection.startTimeSecs,
                this.state.measureWindowSelection.endTimeSecs,
                this.state.measureWindowHeightPx
              );
          }
        }
      }

    };

    document.body.addEventListener('mousemove', onMouseMove);
    document.body.addEventListener('mouseup', onMouseUp);
  }
}
