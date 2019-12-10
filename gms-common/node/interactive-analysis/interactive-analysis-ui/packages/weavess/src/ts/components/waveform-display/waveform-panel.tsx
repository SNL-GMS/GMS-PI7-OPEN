import { IconName, Intent, Popover, Position } from '@blueprintjs/core';
import * as d3 from 'd3';
import * as elementResizeEvent from 'element-resize-event';
import { defer, delay, isEqual, range } from 'lodash';
import memoizeOne from 'memoize-one';
import * as moment from 'moment';
import 'moment-precise-range-plugin';
import * as React from 'react';
import * as THREE from 'three';
import { RpcProvider } from 'worker-rpc';
import * as Entities from '../../entities';
import { isHotKeyCommandSatisfied } from '../../util/hot-key-util';
import { Station, TimeAxis } from './components';
import { createMoveableMarkers, createSelectionWindowMarkers, createVerticalMarkers } from './components/markers';
import {
  DEFAULT_LABEL_WIDTH_PIXELS,
  DEFAULT_XAXIS_HEIGHT_PIXELS,
  SCROLLBAR_WIDTH_PIXELS
} from './constants';
import { SingleDoubleClickEvent } from './events/single-double-click-event';
import { Messages } from './messages';
import './style.scss';
import { BrushType, WaveformPanelProps, WaveformPanelState } from './types';

declare var require;
const WeavessWorker = require('worker-loader?inline&fallback=false!../../workers'); // tslint:disable-line

// create web workers responsible for creating line geometries
const defaultNumWorkers = 4;
const workerRpcs = range(window.navigator.hardwareConcurrency || defaultNumWorkers)
  .map(_ => {
    const worker = new WeavessWorker();
    const workerRpc = new RpcProvider(
      (message, transfer) => worker.postMessage(message, transfer),
    );
    worker.onmessage = e => workerRpc.dispatch(e.data);
    return workerRpc;
  });

/**
 * Waveform Panel component. Contains a TimeAxis and Stations
 */
export class WaveformPanel extends React.PureComponent<WaveformPanelProps, WaveformPanelState> {

  /** Refs to each station component */
  private stationComponentRefs: Station[] | null;

  /** Ref to the root element of weavess */
  private weavessRootRef: HTMLDivElement | null;

  /** Ref to the viewport where waveforms are rendered */
  private waveformsViewportRef: HTMLDivElement | null;

  /** Ref to the container where waveforms are held, directly within the viewport */
  private waveformsContainerRef: HTMLDivElement | null;

  /** Ref to the element where we display the current time range. Updated manually for performance reasons */
  private timeRangeRef: HTMLSpanElement | null;

  /** Ref to the translucent selection brush-effect region, which is updated manually for performance reasons */
  private selectionAreaRef: HTMLDivElement | null;

  /** Ref to the TimeAxis component, which is updated manually for performance reasons */
  private timeAxisRef: TimeAxis | null;

  /** Ref to the vertical crosshair indicator element */
  private crosshairRef: HTMLDivElement | null;

  /** Ref to the primary canvas element where the waveforms are drawn */
  private canvasRef: HTMLCanvasElement | null;

  /** THREE.js WebGLRenderer used to draw waveforms */
  private renderer: THREE.WebGLRenderer;

  /** A list of active web workers */
  private readonly workerRpcs: any[];

  /** If the brush has just started to be used */
  private startOfBrush: boolean = true;

  /** Flag to indicate whether or not the mouse button is pressed down */
  private isMouseDown: { clientX: number; clientY: number } | undefined = undefined;

  /** The start of the brush effect in [0,1] where 0 = this.viewRange.left and 1 = this.viewRange.right */
  private selectionStart: number | undefined;

  /** The type of brush used on the channel */
  private brushType: BrushType | undefined = undefined;

  /** The type of brush used on the channel */
  private needToDeselect: boolean = false;

  /** Reference to the popover component for displaying the current time */
  private timePopoverRef: Popover | null;

  /** Reference to the popover content for displaying the current time */
  private timePopoverContentRef: HTMLDivElement | null;

  /** The left position in pixels for the time popover */
  private timePopoverLeftPosition: number = 0;

  /** The top position in pixels for the time popover */
  private timePopoverTopPosition: number = 0;

  /** The unique id for delaying the time popover to be displayed  */
  private timePopoverId: number | undefined = undefined;

  /** If hotkeys are satisfied for key+click events, disable brushing */
  private brushesDisabled: boolean = false;

  /**
   * A tuple with each element in [0,1] of form [start, end]
   * 0 = this.props.startTimeSecs
   * 1 = this.props.endTimeSecs
   */
  private viewRange: [number, number] = [0, 1];

  /** handler for handling single and double click events */
  private readonly handleSingleDoubleClick: SingleDoubleClickEvent = new SingleDoubleClickEvent();

  /** 
   * A memoized function for creating all stations
   * The memoization function caches the results using 
   * the most recent argument and returns the results. 
   *
   * @param props the waveform panel props
   * 
   * @returns an array JSX elements
   */
  private readonly memoizedCreateStationsJsx: (props: WaveformPanelProps) => JSX.Element[];

  /** 
   * A memoized function for creating all of the markers.
   * The memoization function caches the results using 
   * the most recent argument and returns the results. 
   *
   * @param props the waveform panel props
   * 
   * @returns an array JSX elements
   */
  private readonly memoizedcreateAllMarkers: (props: WaveformPanelProps) => JSX.Element[];

  /**
   * Constructor.
   * 
   * @param props Waveform Panel props as WaveformPanelProps
   */
  public constructor(props: WaveformPanelProps) {
    super(props);
    this.state = {
      timePopoverPosition: Position.TOP
    };
    this.workerRpcs = workerRpcs;
    this.memoizedCreateStationsJsx = memoizeOne(
      this.createStationsJsx,
      /* tell memoize to use a deep comparison for complex objects */
      isEqual);
    this.memoizedcreateAllMarkers = memoizeOne(
      this.createAllMarkers,
      /* tell memoize to use a deep comparison for complex objects */
      isEqual);
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
  public static getDerivedStateFromProps(nextProps: WaveformPanelProps, prevState: WaveformPanelState) {
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
    console.error(`Waveform Panel Error: ${error} : ${info}`);
  }

  /**
   * Called immediately after a compoment is mounted. 
   * Setting state here will trigger re-rendering.
   */
  public componentDidMount() {
    if (!this.canvasRef) {
      console.error('Weavess error - canvas not present at mount time'); // tslint:disable-line
      return;
    }

    this.renderer = new THREE.WebGLRenderer({ alpha: true, antialias: true, canvas: this.canvasRef });
    elementResizeEvent(this.waveformsViewportRef, () => {
      this.renderWaveforms();
      if (this.timeAxisRef) this.timeAxisRef.update();
    });

    if (this.props.initialZoomWindow) {
      this.zoomToTimeWindow(
        this.props.initialZoomWindow.startTimeSecs,
        this.props.initialZoomWindow.endTimeSecs);
    } else {
      this.renderWaveforms();
      this.displayCurrentTimeRange();
    }
  }

  /**
   * Called immediately after updating occurs. Not called for the initial render.
   *
   * @param prevProps the previous props
   * @param prevState the previous state
   */
  public componentDidUpdate(prevProps: WaveformPanelProps, prevState: WaveformPanelState) {
    if (this.props.initialZoomWindow &&
      !isEqual(this.props.initialZoomWindow, prevProps.initialZoomWindow)) { {
        this.zoomToTimeWindow(
          this.props.initialZoomWindow.startTimeSecs,
          this.props.initialZoomWindow.endTimeSecs);
        }
    } else {
      this.renderWaveforms();
      this.displayCurrentTimeRange();
    }
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

  /**
   * React component lifecycle
   */
  public render() {
    const waveformComponents = this.memoizedCreateStationsJsx(this.props);
    const markers = this.memoizedcreateAllMarkers(this.props);
    const weavessRootStyle = this.createRootStyle();

    return (
      <div
        className="weavess-wp"
        ref={ref => this.weavessRootRef = ref}
        style={weavessRootStyle}
        onMouseLeave={e => {
          // hide the time poppver on mouse leave
          this.showHideTimePopover(e, undefined);
        }}
        onDoubleClick={this.onDoubleClick}
      >
        <canvas
          className="weavess-wp-canvas"
          ref={canvas => { this.canvasRef = canvas; }}
          style={{
            width: `calc(100% - (${this.props.configuration.labelWidthPx}px + ${SCROLLBAR_WIDTH_PIXELS}px))`,
            height: `calc(100% - (${DEFAULT_XAXIS_HEIGHT_PIXELS}px))`,
            left: `${this.props.configuration.labelWidthPx}px`
          }}
        />
        <div className="weavess-wp-container">
          <div className="weavess-wp-container-1">
            <div className="weavess-wp-container-2">
              <div className="weavess-wp-container-3">
                <div
                  className="weavess-wp-container-viewport"
                  ref={ref => { this.waveformsViewportRef = ref; }}
                  onWheel={this.onMouseWheel}
                  onScroll={e => {
                    if (!this.waveformsContainerRef
                      || !this.waveformsViewportRef
                      || !this.canvasRef
                      || !this.stationComponentRefs
                      || !this.timeAxisRef) return;

                    const viewport = this.waveformsViewportRef;
                    const viewportContentContainer = this.waveformsContainerRef;

                    // tslint:disable-next-line:max-line-length
                    const labelWidthPx = this.props.configuration.labelWidthPx || DEFAULT_LABEL_WIDTH_PIXELS;
                    const timeRangeLeft = viewport.scrollLeft / (viewportContentContainer.clientWidth - labelWidthPx);
                    // tslint:disable-next-line:max-line-length
                    const timeRangeRight = (viewport.scrollLeft + this.canvasRef.clientWidth) / (viewportContentContainer.clientWidth - labelWidthPx);
                    this.viewRange = [timeRangeLeft, timeRangeRight];

                    this.timeAxisRef.update();
                    this.displayCurrentTimeRange();
                    this.renderWaveforms();
                  }}
                  onKeyDown={this.onKeyDown}
                  onKeyUp={this.onKeyUp}
                >
                  <div
                    className="weavess-wp-container-viewport-content"
                    ref={waveformsContainer => this.waveformsContainerRef = waveformsContainer}
                  >
                    {waveformComponents}
                    <div
                      className="weavess-wp-container-viewport-content-markers"
                      style={{
                        width: `calc(100% - ${this.props.configuration.labelWidthPx}px)`,
                        left: `${this.props.configuration.labelWidthPx}px`
                      }}
                    >
                      {markers}
                    </div>
                  </div>
                </div>
              </div>
            </div>
            <div
              className="weavess-wp-container-overlay"
              style={{
                width: `calc(100% - (${this.props.configuration.labelWidthPx}px + ${SCROLLBAR_WIDTH_PIXELS}px))`,
              }}
            >
              <Popover
                ref={ref => { this.timePopoverRef = ref; }}
                content={
                  <div
                    className="weavess-wp-time-poppver-content"
                    ref={ref => { this.timePopoverContentRef = ref; }}
                  />}
                isOpen={false}
                usePortal={false}
                minimal={false}
                position={this.state.timePopoverPosition}
              >
                <div />
              </Popover>
              <div
                className="weavess-wp-container-overlay-cross-hair"
                ref={ref => { this.crosshairRef = ref; }}
              />
              <div
                className="weavess-wp-container-overlay-selection-area"
                ref={ref => { this.selectionAreaRef = ref; }}
              />
            </div>
          </div>
          {
            this.props.stations.length > 0 ?
              <TimeAxis
                ref={ref => this.timeAxisRef = ref}
                startTimeSecs={this.props.startTimeSecs}
                endTimeSecs={this.props.endTimeSecs}
                borderTop={true}
                labelWidthPx={this.props.configuration.labelWidthPx || DEFAULT_LABEL_WIDTH_PIXELS}
                scrolbarWidthPx={SCROLLBAR_WIDTH_PIXELS}
                getViewRange={this.getViewRange}
              />
              : []
          }
        </div>
        <div
          className="weavess-wp-time-range"
          style={{
            left: `${this.props.configuration.labelWidthPx}px`
          }}
        >
          <span
            ref={ref => this.timeRangeRef = ref}
          />
        </div>
      </div>
    );
  }

  /**
   * Exposed primarily for non-react users.
   * Force a refresh and redraw of the waveforms.
   */
  public refresh = (): void => {
    // fresh the zoom state, to ensure proper zoom if window has resized
    this.zoom(this.viewRange[0], this.viewRange[1]);
    if (this.timeAxisRef) this.timeAxisRef.update();
    this.renderWaveforms();
    this.displayCurrentTimeRange();
  }

  /**
   * get the currently displayed viewTimeInterval
   * (the startTime and endTime of the currently displayed view of the waveforms)
   */
  public getCurrentViewRangeInSeconds = (): Entities.TimeRange => {
    const waveformDataRange = this.props.endTimeSecs - this.props.startTimeSecs;
    const calculatedStartTime = (waveformDataRange * this.viewRange[0]) + this.props.startTimeSecs;
    const calculatedEndTime = (waveformDataRange * this.viewRange[1]) + this.props.startTimeSecs;
    return { startTimeSecs: calculatedStartTime, endTimeSecs: calculatedEndTime };
  }

  /**
   * Computes the time in seconds for the mouse x position.
   * 
   * @param mouseXPosition the mouse x position to compute the time on
   * 
   * @returns The computed time in seconds
   */
  public readonly computeTimeSecsForMouseXPosition = (mouseXPosition: number): number => {
    const timeRangeSecs = this.props.endTimeSecs - this.props.startTimeSecs;
    const left = this.props.startTimeSecs + (timeRangeSecs * this.viewRange[0]);
    const right = this.props.startTimeSecs + (timeRangeSecs * this.viewRange[1]);
    const scale = d3.scaleLinear()
      .domain([0, 1])
      .range([left, right]);
    return scale(mouseXPosition);
  }

  /**
   * Removes the brush div, is public so it be hit with weavess reference
   */
  public clearBrushStroke = () => {
    if (!this.selectionAreaRef) {
      return;
    }
    this.selectionAreaRef.style.display = 'none';
    this.selectionStart = undefined;
    this.brushType = undefined;
    this.startOfBrush = true;
  }

  /**
   * Removes/hides the measure window selections for all of the stations.
   */
  public removeMeasureWindowSelection = () => {
    if (this.stationComponentRefs) {
      this.stationComponentRefs.forEach(station => {
          station.removeMeasureWindowSelection();
      });
    }
  }

  /**
   * Zooms to the provided time range [startTimeSecs, endTimeSecs].
   * 
   * @param startTimeSecs the start time in seconds
   * @param endTimeSecs the end time in seconds
   */
  public readonly zoomToTimeWindow = (startTimeSecs: number, endTimeSecs: number) => {
    const scale = d3.scaleLinear()
      .domain([this.props.startTimeSecs, this.props.endTimeSecs])
      .range([0, 1])
      .clamp(true);
    defer(() => this.zoom(scale(startTimeSecs), scale(endTimeSecs)));
  }

  /**
   * return the current view range in [0,1]
   * where 0 = this.props.startTimeSecs
   * and 1 = this.props.endTimeSecs
   */
  private readonly getViewRange = () => this.viewRange;

  /**
   * Zoom in on mouse wheel
   */
  private readonly onMouseWheel = (e: React.WheelEvent<HTMLDivElement>) => {

    if (!this.canvasRef) return;

    const modPercent = 0.4;

    if (e.ctrlKey && !e.shiftKey) {
      e.preventDefault();

      // compute current x position in [0,1] and zoom to that point
      const xFrac = (e.clientX - this.canvasRef.getBoundingClientRect().left)
        / (this.canvasRef.getBoundingClientRect().width);

      // zoom out
      if (e.deltaY > 0) {
        this.zoomByPercentageToPoint(modPercent, xFrac);
      } else { // zoom in
        if (!this.hasReachedMaxZoomLevel()) {
          this.zoomByPercentageToPoint(-modPercent, xFrac);
        } else {
          this.props.toast(Messages.maxZoom);
        }
      }
      this.renderWaveforms();
    } else if (e.ctrlKey && e.shiftKey) {
      e.preventDefault();
      if (e.deltaY > 0) {
        // pan left
        this.panByPercentage(-modPercent);
      } else {
        // pan right
        this.panByPercentage(modPercent);
      }
      this.renderWaveforms();
    }
  }

  /**
   * onKeyDown event handler
   */
  private readonly onKeyDown = (e: React.KeyboardEvent<HTMLDivElement>) => {
    const amplitudeScaleResetHotKey = this.props.configuration.hotKeys.amplitudeScaleReset;
    if (amplitudeScaleResetHotKey) {
      if (isHotKeyCommandSatisfied(e.nativeEvent, amplitudeScaleResetHotKey)) {
        if (this.stationComponentRefs) {
          this.stationComponentRefs.forEach(station => station.resetAmplitude());
        }
      }
    }
    const amplitudeScaleSingleResetHotKey = this.props.configuration.hotKeys.amplitudeScaleSingleReset;
    if (amplitudeScaleSingleResetHotKey) {
      if (isHotKeyCommandSatisfied(e.nativeEvent, amplitudeScaleSingleResetHotKey)) {
        this.brushesDisabled = true;
      }
    }
    // check for mask create hot key
    const maskCreateHotKey = this.props.configuration.hotKeys.maskCreate;
    if (maskCreateHotKey) {
      if (isHotKeyCommandSatisfied(e.nativeEvent, maskCreateHotKey)) {
        this.brushType = BrushType.CreateMask;
      }
    }
  }

  /**
   * onKeyUp event handler
   */
  private readonly onKeyUp = (e: React.KeyboardEvent<HTMLDivElement>) => {
    const maskCreateHotKey = this.props.configuration.hotKeys.maskCreate;
    if (maskCreateHotKey) {
      if (maskCreateHotKey.indexOf(e.nativeEvent.code) > -1 && !this.selectionStart) {
        this.brushType = undefined;
      }
    }
    const amplitudeScaleSingleResetHotKey = this.props.configuration.hotKeys.amplitudeScaleSingleReset;
    if (amplitudeScaleSingleResetHotKey) {
      if (!isHotKeyCommandSatisfied(e.nativeEvent, amplitudeScaleSingleResetHotKey)) {
        this.brushesDisabled = false;
      }
    }
  }

  /** 
   * Creates all of the markers.
   *
   * @param props the waveform panel props
   * 
   * @returns an array JSX elements
   */
  private readonly createAllMarkers = (props: WaveformPanelProps): JSX.Element[] =>
    [...createVerticalMarkers(
      props.startTimeSecs,
      props.endTimeSecs,
      props.markers ? props.markers.verticalMarkers : undefined
    ),
    ...createMoveableMarkers(
      props.startTimeSecs,
      props.endTimeSecs,
      props.markers ? props.markers.moveableMarkers : undefined,
      this.getCurrentViewRangeInSeconds,
      () => (this.waveformsContainerRef) ? this.waveformsContainerRef.clientWidth : 0,
      () => (this.waveformsViewportRef) ? this.waveformsViewportRef.clientWidth : 0,
      props.events ?
        (marker: Entities.Marker) => {
          if (props.events.onUpdateMarker) {
            props.events.onUpdateMarker(marker);
          }
         } : undefined,
      props.configuration.labelWidthPx || DEFAULT_LABEL_WIDTH_PIXELS
    ),
    ...createSelectionWindowMarkers(
      props.startTimeSecs,
      props.endTimeSecs,
      props.markers ? props.markers.selectionWindows : undefined,
      this.getCurrentViewRangeInSeconds,
      () => this.canvasRef,
      () => (this.waveformsContainerRef) ? this.waveformsContainerRef.clientWidth : 0,
      () => (this.waveformsViewportRef) ? this.waveformsViewportRef.clientWidth : 0,
      this.computeTimeSecsForMouseXPosition,
      this.onMouseMove,
      this.onMouseDown,
      this.onMouseUp,
      props.events ?
        (selection: Entities.SelectionWindow) => {
          if (props.events.onUpdateSelectionWindow) {
            props.events.onUpdateSelectionWindow(selection);
          }
         } : undefined,
      props.events ?
        (selection: Entities.SelectionWindow, timeSecs: number) => {
          if (props.events.onClickSelectionWindow) {
            props.events.onClickSelectionWindow(selection, timeSecs);
          }
        } : undefined,
      props.configuration.labelWidthPx || DEFAULT_LABEL_WIDTH_PIXELS
    )]

  /** 
   * Creates all of the stations.
   *
   * @param props the waveform panel props
   * 
   * @returns an array JSX elements
   */
  private readonly createStationsJsx = (props: WaveformPanelProps): JSX.Element[] => {
    this.stationComponentRefs = [];
    const stationElements: JSX.Element[] = [];
    for (const station of props.stations) {
      stationElements.push(
        <Station
          // data props
          key={station.id}
          ref={stationRef => {
            if (!this.stationComponentRefs) return;
            if (stationRef) { this.stationComponentRefs.push(stationRef); }
          }}
          configuration={props.configuration}
          displayStartTimeSecs={props.startTimeSecs}
          displayEndTimeSecs={props.endTimeSecs}
          shouldRenderWaveforms={props.shouldRenderWaveforms}
          shouldRenderSpectrograms={props.shouldRenderSpectrograms}
          workerRpcs={this.workerRpcs}
          selections={props.selections ?
            props.selections :
            {
              channels: undefined,
              signalDetections: undefined,
              predictedPhases: undefined
            }
          }
          station={{ ...station }}

          // callback  props
          toast={(message: string, intent?: Intent, icon?: IconName, timeout?: number) =>
              props.toast(message, intent, icon, timeout)}
          getViewRange={this.getViewRange}
          canvasRef={() => this.canvasRef}
          renderWaveforms={this.renderWaveforms}
          getCurrentViewRangeInSeconds={this.getCurrentViewRangeInSeconds}
          computeTimeSecsForMouseXPosition={this.computeTimeSecsForMouseXPosition}
          events={props.events.stationEvents}
          onMouseMove={this.onMouseMove}
          onMouseDown={this.onMouseDown}
          onMouseUp={this.onMouseUp}
          updateMeasureWindow={
            props.updateMeasureWindow ?
              (stationId: string, channel: Entities.Channel,
                startTimeSecs: number, endTimeSecs: number, isDefaultChannel: boolean, removeSelection: () => void) => {
                if (props.updateMeasureWindow) {
                  props.updateMeasureWindow(
                    stationId, channel, startTimeSecs, endTimeSecs, isDefaultChannel, removeSelection);
                }
              }
            : undefined}
        />,
      );
    }
    return stationElements;
  }

  /**
   * return time range of current view as human-readable string
   */
  private readonly displayCurrentTimeRange = () => {
    if (!this.timeRangeRef) return;
    const scale = d3.scaleLinear()
      .domain([0, 1])
      .range([this.props.startTimeSecs, this.props.endTimeSecs]);
    const left = scale(this.viewRange[0]);
    const right = scale(this.viewRange[1]);
    this.timeRangeRef.innerHTML = `${moment.unix(left)
      .utc()
      .format('DD MMM YYYY HH:mm:ss.SSSS')}
            + ${(moment as any).preciseDiff(moment.unix(right), moment.unix(left))}`;
  }

  /**
   * If WEAVESS is contained inside of a div with flex layout, sizing it with height=100% doesn't work.
   */
  private createRootStyle(): React.CSSProperties {
    if (this.props.flex) {
      return {
        flex: '1 1 0',
        position: 'relative',
      };
    } else {
      return {
        height: '100%',
        position: 'relative',
        width: '100%',
        boxSizing: 'content-box'
      };
    }
  }

  /**
   * Render currently visible waveforms to the canvas.
   */
  private readonly renderWaveforms = (): void => {
    window.requestAnimationFrame(() => {
      // if we don't have a set size to display, abort
      if (!this.weavessRootRef ||
        !this.stationComponentRefs ||
        !this.waveformsViewportRef ||
        !this.canvasRef ||
        this.waveformsViewportRef.clientHeight === 0 ||
        this.waveformsViewportRef.clientWidth === 0) { return; }

      this.stationComponentRefs.forEach(station => {
        if (this.waveformsViewportRef) {
          station.updateScrollPosition(this.waveformsViewportRef.scrollWidth, this.waveformsViewportRef.scrollLeft);
        }
      });

      this.updateSize();

      this.renderer.setScissorTest(true);

      const boundsRect = this.canvasRef.getBoundingClientRect();

      for (const waveform of this.stationComponentRefs) {
        const channels = [waveform.defaultChannelRef];
        for (const channelId in waveform.nonDefaultChannelRefs) {
          if (waveform.nonDefaultChannelRefs.hasOwnProperty(channelId)) {
            const channel = waveform.nonDefaultChannelRefs[channelId];
            if (channel) {
              channels.push(channel);
            }
          }
        }

        channels.forEach(channel => {
          if (!channel) return;
          channel.renderScene(this.renderer, boundsRect);
        });
      }

      this.renderer.setScissorTest(false);
    });
  }

  /**
   * resize the renderer to fit the new canvas size
   */
  private updateSize() {
    if (!this.canvasRef) return;

    const width = this.canvasRef.offsetWidth;
    const height = this.canvasRef.offsetHeight;
    if (this.canvasRef.width !== width || this.canvasRef.height !== height) {
      this.renderer.setSize(width, height, false);
    }
  }

  /**
   * Handles a double click event.
   */
  private readonly onDoubleClick = (event: React.MouseEvent<HTMLDivElement>) => {
      this.handleSingleDoubleClick.onDoubleClick(
        event,
        (e: React.MouseEvent<HTMLDivElement> | MouseEvent) => {
          const defaultZoomWindow = this.props.defaultZoomWindow ?
          this.props.defaultZoomWindow
          : {
            startTimeSecs: this.props.startTimeSecs,
            endTimeSecs: this.props.endTimeSecs
          };

          // double click registered, clear mouse down state
          this.isMouseDown = undefined;
          if (this.timePopoverId) {
            clearTimeout(this.timePopoverId);
            this.timePopoverId = undefined;
          }

          if (this.waveformsViewportRef) {
            // reset the scroll bar to the start to prevent rendering issues
            this.waveformsViewportRef.scrollLeft = 0;
          }
          this.zoomToTimeWindow(defaultZoomWindow.startTimeSecs, defaultZoomWindow.endTimeSecs);
      });
  }

  /**
   * mouse down event handler
   */
  private readonly onMouseDown = (
    e: React.MouseEvent<HTMLDivElement>,
    xPct: number | undefined = undefined,
    channelId: string | undefined = undefined,
    timeSecs: number | undefined = undefined,
    isDefaultChannel: boolean | undefined = undefined) => {

    // keep track of the mouse down state
    if (timeSecs) {
      // markers do not have time seconds, only track when on the waveform
      this.isMouseDown = { clientX: e.clientX, clientY: e.clientY };
    }
    // if the amplitude scaling hotkey is in use - do not brush
    // show or hide the time popover
    this.showHideTimePopover(e, timeSecs);
    if (this.brushesDisabled) {
      return;
    }
    // check if any keys are pressed on mouse down
    // zoom mode
    if (e.ctrlKey || e.metaKey) {
      this.brushType = BrushType.Zoom;
    }

    // set the zoom start point if a brush is being used
    if (this.brushType === BrushType.Zoom) {
      this.selectionStart = xPct;
    } else if (this.brushType === BrushType.CreateMask) {
      const disableMaskModification = (isDefaultChannel) ?
        this.props.configuration.defaultChannel.disableMaskModification :
        this.props.configuration.nonDefaultChannel.disableMaskModification ;
      if (!disableMaskModification) {
        this.selectionStart = xPct;
      } else {
        this.selectionStart = undefined;
        this.brushType = undefined;
        this.props.toast(Messages.maskModificationDisabled);
      }
    }

    // Select channel if no channels selected and using CreateMask brush
    if (this.brushType === BrushType.CreateMask) {
      if (!this.props.selections || !this.props.selections.channels || this.props.selections.channels.length < 1) {
        if (this.props.selectChannel && channelId) {
          this.props.selectChannel(channelId);
          this.needToDeselect = true;
        }
      }
    }
  }

  /**
   * mouse move event handler
   */
  private readonly onMouseMove = (
    e: React.MouseEvent<HTMLDivElement>,
    xPct: number | undefined = undefined,
    timeSecs: number | undefined = undefined) => {
    if (!this.selectionAreaRef) return;

    const fracToPct = 100;
    // minimum amount the mouse must move until it begins a brush effect
    // 0.01 = 1% of the current view range
    const minMovementDeltaFrac = 0.01;

    if (!xPct) {
      const leftOffset = this.canvasRef ? this.canvasRef.getBoundingClientRect().left : 0;
      const width = this.canvasRef ? this.canvasRef.getBoundingClientRect().width : 0;
      // tslint:disable-next-line:no-parameter-reassignment
      xPct = (e.clientX - leftOffset) / width;
    }

    // show or hide the time popover
    // TODO: time seconds is not available on mouse move over selection windows
    this.showHideTimePopover(e, timeSecs);

    // move the crosshair to the current pointer location
    if (this.crosshairRef) {
      this.crosshairRef.style.left = `${xPct * fracToPct}%`;
    }

    // if the user has moved more than 1% of the viewport, consider it an operation
    // Paint !
    if (this.selectionStart &&
      Math.abs(this.selectionStart - xPct) > minMovementDeltaFrac ||
      Math.abs(xPct - (this.selectionStart as number)) > minMovementDeltaFrac) {
      if (this.startOfBrush) {
        this.selectionAreaRef.style.display = 'initial';
        this.startOfBrush = false;
      }
      const start = Math.min((this.selectionStart as number), xPct);
      const end = Math.max((this.selectionStart as number), xPct);
      const left = `${start * fracToPct}%`;
      const right = `${(1 - end) * fracToPct}%`;
      this.selectionAreaRef.style.left = left;
      this.selectionAreaRef.style.right = right;
      // tslint:disable-next-line:prefer-conditional-expression
      if (this.brushType === BrushType.CreateMask) {
        // tslint:disable-next-line:max-line-length
        this.selectionAreaRef.style.backgroundColor = 'rgba(145, 228, 151, .3)'; // ! should be set from user preferences
      } else {
        this.selectionAreaRef.style.backgroundColor = 'rgba(150,150,150,0.3)';
      }
    }
  }

  /**
   * mouse up event handler
   */
  // tslint:disable-next-line:cyclomatic-complexity
  private readonly onMouseUp = (
    event: React.MouseEvent<HTMLDivElement>,
    xPct: number | undefined = undefined,
    channelId: string | undefined = undefined,
    timeSecs: number | undefined = undefined,
    isDefaultChannel: boolean | undefined = undefined) => {

    // ignore any mouse up events if the mouse down flag is not set
    if (!this.isMouseDown) {
      return;
    }

    const mouseDown = this.isMouseDown;

    // track the mouse down state
    this.isMouseDown = undefined;
    if (this.timePopoverId) {
      clearTimeout(this.timePopoverId);
      this.timePopoverId = undefined;
    }

    // show or hide the time popover
    this.showHideTimePopover(event, timeSecs);

    // If the mouse is released *before* a brush stroke has been made
    // Cancel the stroke so as to not interfere with other mouse events
    if (this.startOfBrush) {
      if (this.selectionAreaRef) {
        this.selectionAreaRef.style.display = 'none';
      }
      this.selectionStart = undefined;
      this.brushType = undefined;
      this.startOfBrush = true;
    }
    if (!this.selectionAreaRef) return;

    if (!xPct) {
      const leftOffset = this.canvasRef ? this.canvasRef.getBoundingClientRect().left : 0;
      const width = this.canvasRef ? this.canvasRef.getBoundingClientRect().width : 0;
      // tslint:disable-next-line:no-parameter-reassignment
      xPct = (event.clientX - leftOffset) / width;
    }

    const events = isDefaultChannel ?
      (this.props.events.stationEvents &&
      this.props.events.stationEvents.defaultChannelEvents &&
      this.props.events.stationEvents.defaultChannelEvents.events) ?
      this.props.events.stationEvents.defaultChannelEvents.events : undefined
      : (this.props.events.stationEvents && this.props.events.stationEvents.nonDefaultChannelEvents &&
        this.props.events.stationEvents.nonDefaultChannelEvents.events) ?
        this.props.events.stationEvents.nonDefaultChannelEvents.events : undefined;

    // if the user is zooming, perform the zoom
    if (this.brushType && !this.startOfBrush) {
      const scale = d3.scaleLinear()
        .domain([0, 1])
        .range([this.viewRange[0], this.viewRange[1]]);
      const start = Math.min((this.selectionStart as number), xPct);
      const end = Math.max((this.selectionStart as number), xPct);
      if (this.brushType === BrushType.Zoom) {
        if (!this.hasReachedMaxZoomLevel()) {
          this.zoom(scale(start), scale(end));
        } else {
          this.props.toast(Messages.maxZoom);
        }
      } else if (this.brushType === BrushType.CreateMask) {
        const scaleTime = d3.scaleLinear()
          .domain([0, 1])
          .range([this.props.startTimeSecs, this.props.endTimeSecs]);

        if (events) {
          if (events.onMaskCreateDragEnd) {
            events.onMaskCreateDragEnd(
              event, scaleTime(scale(start)),
              scaleTime(scale(end)), this.needToDeselect);
          }
        }
        this.needToDeselect = false;
      }
    } else {
      // handle a single click event, only if the user has not moved the mouse
      if (event.clientX === mouseDown.clientX && event.clientY === mouseDown.clientY) {
        this.handleSingleDoubleClick.onSingleClickEvent(
          event,
          (e: React.MouseEvent<HTMLDivElement>) => {
            // handle onChannelClick event if not zooming or modifying a mask
            if (events) {
              if (events.onChannelClick && channelId && timeSecs) {
                events.onChannelClick(e, channelId, timeSecs);
              }
            }
          });
      }
    }

    if (this.brushType !== BrushType.CreateMask) {
      this.selectionAreaRef.style.display = 'none';
      this.selectionStart = undefined;
      this.brushType = undefined;
      this.startOfBrush = true;
    }
  }

  /**
   * Shows or hides the time popover.
   * 
   * @param e the mouse event
   * @param timeSecs the current time in seconds
   * 
   */
  private readonly showHideTimePopover = (
    e: React.MouseEvent<HTMLDivElement>,
    timeSecs: number | undefined) => {

    if (this.timePopoverRef) {
      if (this.isMouseDown && timeSecs) {
        if (this.canvasRef) {
          const canvasBoundingClientRect = this.canvasRef.getBoundingClientRect();
          // calculate the popover position based on the current mouse event
          this.timePopoverLeftPosition = e.clientX - canvasBoundingClientRect.left;
          this.timePopoverTopPosition = e.clientY - canvasBoundingClientRect.top;

          // function used to update the curret state of the time popover
          const updatePopover = () => {
            if (this.timePopoverRef && this.timePopoverRef.popoverElement &&
              this.timePopoverContentRef && this.timePopoverRef.popoverElement.parentElement) {
              defer(() => {
                if (this.timePopoverRef && this.timePopoverRef.popoverElement &&
                  this.timePopoverContentRef && this.timePopoverRef.popoverElement.parentElement) {
                  this.timePopoverRef.popoverElement.parentElement.style.left = `${this.timePopoverLeftPosition}px`;
                  this.timePopoverRef.popoverElement.parentElement.style.top = `${this.timePopoverTopPosition}px`;
                  this.timePopoverContentRef.innerHTML =
                    moment.unix(timeSecs)
                      .utc()
                      .format('DD MMM YYYY HH:mm:ss.SSSS');
                }
              });
            }
          };

          // tslint:disable-next-line:no-magic-numbers
          if ((this.canvasRef.width - this.timePopoverLeftPosition) < 80) {
            if (this.state.timePopoverPosition !== Position.TOP_RIGHT) {
              this.setState({ timePopoverPosition: Position.TOP_RIGHT });
            }
          // tslint:disable-next-line:no-magic-numbers
          } else if (this.timePopoverLeftPosition < 80) {
            if (this.state.timePopoverPosition !== Position.TOP_LEFT) {
              this.setState({ timePopoverPosition: Position.TOP_LEFT });
            }
          } else {
            if (this.state.timePopoverPosition !== Position.TOP) {
              this.setState({ timePopoverPosition: Position.TOP });
            }
          }

          if (this.timePopoverRef.state.isOpen) {
            this.timePopoverRef.setState({ isOpen: true }, updatePopover);
          } else {
            // popover is currently not open, delay opening slightly to
            // avoid conflict with a double click event
            if (!this.timePopoverId) {
              this.timePopoverId = delay(
                () => {
                  if (this.timePopoverRef && this.isMouseDown) {
                    this.timePopoverRef.setState({ isOpen: true}, updatePopover);
                  }
                  this.timePopoverId = undefined;
                },
                // typical default timing is 500 ms (half a second)
                // between clicks for a double click to register
                // tslint:disable-next-line:no-magic-numbers
                500);
              }
          }
        }
      } else {
        if (this.timePopoverContentRef) {
          this.timePopoverContentRef.innerHTML = '';
        }
        this.timePopoverRef.setState({ isOpen: false });
      }
    }
  }

  /**
   * Returns true if the max zoom level has been reached. False otherwise.
   */
  private readonly hasReachedMaxZoomLevel = (): boolean => {
    const maxZoomDiff = 0.0005;
    return parseFloat(this.viewRange[1].toFixed(4)) - parseFloat(this.viewRange[0].toFixed(4)) <= maxZoomDiff;
  }

  /**
   * zoomPct in [0,1], x in [0,1]
   */
  private readonly zoomByPercentageToPoint = (zoomPct: number, x: number) => {
    const theRange = this.viewRange[1] - this.viewRange[0];
    const zoom = (theRange * zoomPct) / 2.0; // tslint:disable-line
    const left = this.viewRange[0] - (zoom * x);
    const right = this.viewRange[1] + (zoom * (1 - x));
    this.zoom(left, right);
  }

  /**
   * pct in [0,1]
   */
  private readonly panByPercentage = (pct: number) => {
    const theRange = this.viewRange[1] - this.viewRange[0];
    const delta = theRange * pct;
    const left = this.viewRange[0] + delta;
    const right = this.viewRange[1] + delta;
    this.zoom(left, right);
  }

  /**
   * left/right are numbers between [0,1] denoting the left/right percentages of [start,end]
   */
  private readonly zoom = (start: number, end: number) => {
    if (!this.waveformsContainerRef
      || !this.canvasRef
      || !this.waveformsViewportRef
      || !this.timeAxisRef) return;
    if (start < 0) {
      // tslint:disable-next-line:no-parameter-reassignment
      start = 0;
    }
    if (end > 1) {
      // tslint:disable-next-line:no-parameter-reassignment
      end = 1;
    }
    if (end < start) {
      const minDelta = 0.001;
      // tslint:disable-next-line:no-parameter-reassignment
      end = start + minDelta;
    }

    if (start === 0 && end === 1) {
      this.waveformsContainerRef.style.width = 'initial';
    } else {
      const theRange = end - start;
      const labelWidthPx = this.props.configuration.labelWidthPx || DEFAULT_LABEL_WIDTH_PIXELS;
      const pixels = (this.canvasRef.clientWidth / theRange) + labelWidthPx;
      this.waveformsContainerRef.style.width = `${pixels}px`;
      this.waveformsViewportRef.scrollLeft = start * (pixels - labelWidthPx);
    }

    this.viewRange = [start, end];
    this.timeAxisRef.update();
    this.displayCurrentTimeRange();
    this.renderWaveforms();
  }
}
// tslint:disable-next-line:max-file-line-count
