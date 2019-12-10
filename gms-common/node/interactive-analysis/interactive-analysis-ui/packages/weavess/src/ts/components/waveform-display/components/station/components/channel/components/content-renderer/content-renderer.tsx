import { isEqual } from 'lodash';
import memoizeOne from 'memoize-one';
import * as React from 'react';
import * as Entities from '../../../../../../../../entities';
import {
  createMoveableMarkers,
  createSelectionWindowMarkers,
  createVerticalMarkers,
} from '../../../../../markers';
import { PredictedPhases, SignalDetections, TheoreticalPhases } from './components';
import { ContentRendererProps, ContentRendererState } from './types';

import './styles.scss';

/**
 * Content renderer component responsible for rendering the main content of a channel.
 */
export class ContentRenderer extends React.PureComponent<ContentRendererProps, ContentRendererState> {

  /** Default channel props, if not provided */
  public static readonly defaultProps: Entities.ChannelDefaultConfiguration = {
    displayType: [Entities.DisplayType.LINE],
    pointSize: 2,
    color: '#4580E6'
  };

  /** Ref to the element where this channel will be rendered */
  public containerRef: HTMLElement | null;

  /** Ref to the element where this description label will be rendered */
  public descriptionLabelRef: HTMLElement | null;

  /** Ref to drag indicator element */
  private dragIndicatorRef: HTMLDivElement | null;

  /** Ref to the translucent selection brush-effect region, which is updated manually for performance reasons */
  public measureWindowSelectionAreaRef: HTMLDivElement | null;

  /** 
   * A memoized function for creating all of the markers.
   * The memoization function caches the results using 
   * the most recent argument and returns the results. 
   *
   * @param props the content renderer props
   * 
   * @returns an array JSX elements
   */
  private readonly memoizedcreateAllMarkers: (props: ContentRendererProps) => JSX.Element[];

  /**
   * Constructor
   * 
   * @param props props as ContentRendererProps
   */
  public constructor(props: ContentRendererProps) {
    super(props);
    this.memoizedcreateAllMarkers = memoizeOne(
      this.createAllMarkers,
      /* tell memoize to use a deep comparison for complex objects */
      isEqual);
    this.state = {
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
  public static getDerivedStateFromProps(nextProps: ContentRendererProps, prevState: ContentRendererState) {
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
    /* no-op */
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
  public componentDidUpdate(prevProps: ContentRendererProps, prevState: ContentRendererState) {
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
    const isSelected = this.props.selections.channels &&
      this.props.selections.channels.indexOf(this.props.channelId) > -1;

    return (
      <div
        className="contentrenderer"
        style={{
          backgroundColor: isSelected ? 'rgba(150,150,150,0.2)' : 'initial'
        }}
        ref={ref => { if (ref) { this.containerRef = ref; } }}
        tabIndex={0}
        onKeyDown={this.props.onKeyDown}
        onMouseEnter={this.onMouseEnter}
        onMouseLeave={this.onMouseLeave}
        onContextMenu={this.props.onContextMenu}
        onMouseMove={this.props.onMouseMove}
        onMouseDown={this.props.onMouseDown}
        onMouseUp={this.props.onMouseUp}
        onMouseOver={this.onMouseEnter}
      >
        {this.props.children}
        <div
          className="contentrenderer-content"
          style={{
           }}
        >
          {(this.props.description) ?
            <div
              ref={ref => this.descriptionLabelRef = ref}
              className="contentrenderer-content-description-label"
              style={{
                right: '6px',
                color: this.props.descriptionLabelColor
              }}
            >
              {this.props.description}
            </div>
            : undefined}
          <div
            ref={ref => this.dragIndicatorRef = ref}
            className="contentrenderer-content-drag-indicator"
          />
          <div
            ref={ref => { this.measureWindowSelectionAreaRef = ref; }}
            className="contentrenderer-measure-window-selection"
            onMouseDown={e => this.props.onMeasureWindowClick(e)}
          />
          <div className="contentrenderer-content-markers">
            {this.memoizedcreateAllMarkers(this.props)}
          </div>
          <SignalDetections
            configuration={this.props.configuration}
            stationId={this.props.stationId}
            channelId={this.props.channelId}
            signalDetections={this.props.signalDetections}
            isDefaultChannel={this.props.isDefaultChannel}
            displayStartTimeSecs={this.props.displayStartTimeSecs}
            displayEndTimeSecs={this.props.displayEndTimeSecs}
            selectedSignalDetections={this.props.selections.signalDetections}
            events={this.props.events}
            toast={this.props.toast}
            getTimeSecsForClientX={this.getTimeSecsForClientX}
            toggleDragIndicator={this.toggleDragIndicator}
            positionDragIndicator={this.positionDragIndicator}
          />
          <PredictedPhases
            configuration={this.props.configuration}
            stationId={this.props.stationId}
            channelId={this.props.channelId}
            predictedPhases={this.props.predictedPhases}
            isDefaultChannel={this.props.isDefaultChannel}
            displayStartTimeSecs={this.props.displayStartTimeSecs}
            displayEndTimeSecs={this.props.displayEndTimeSecs}
            selectedPredictedPhases={this.props.selections.predictedPhases}
            events={this.props.events}
            toast={this.props.toast}
            getTimeSecsForClientX={this.getTimeSecsForClientX}
            toggleDragIndicator={this.toggleDragIndicator}
            positionDragIndicator={this.positionDragIndicator}
          />
          <TheoreticalPhases
            configuration={this.props.configuration}
            stationId={this.props.stationId}
            theoreticalPhaseWindows={this.props.theoreticalPhaseWindows}
            isDefaultChannel={this.props.isDefaultChannel}
            displayStartTimeSecs={this.props.displayStartTimeSecs}
            displayEndTimeSecs={this.props.displayEndTimeSecs}
            events={this.props.events}
            toast={this.props.toast}
            getTimeSecsForClientX={this.getTimeSecsForClientX}
            toggleDragIndicator={this.toggleDragIndicator}
            positionDragIndicator={this.positionDragIndicator}
          />
        </div>
      </div >
    );
  }

  /** 
   * Creates all of the markers.
   *
   * @param props the content renderer props
   * 
   * @returns an array JSX elements
   */
  private readonly createAllMarkers = (props: ContentRendererProps): JSX.Element[] =>
    [...createVerticalMarkers(
      props.displayStartTimeSecs,
      props.displayEndTimeSecs,
      props.markers ? props.markers.verticalMarkers : undefined,
    ),
    ...createMoveableMarkers(
      props.displayStartTimeSecs,
      props.displayEndTimeSecs,
      props.markers ? props.markers.moveableMarkers : undefined,
      // tslint:disable-next-line: no-unbound-method
      props.getCurrentViewRangeInSeconds,
      () => (this.containerRef) ? this.containerRef.clientWidth : 0,
      () => (this.containerRef) ? this.containerRef.clientWidth : 0,
      props.events ?
        (marker: Entities.Marker) => {
          if (props.events && props.events.onUpdateMarker) {
            props.events.onUpdateMarker(props.channelId, marker);
          }
         } : undefined,
      0
    ),
    ...createSelectionWindowMarkers(
      props.displayStartTimeSecs,
      props.displayEndTimeSecs,
      props.markers ? props.markers.selectionWindows : undefined,
      // tslint:disable-next-line: no-unbound-method
      props.getCurrentViewRangeInSeconds,
      () => props.canvasRef(),
      () => (this.containerRef) ? this.containerRef.clientWidth : 0,
      () => (this.containerRef) ? this.containerRef.clientWidth : 0,
      // tslint:disable-next-line: no-unbound-method
      props.computeTimeSecsForMouseXPosition,
      // tslint:disable-next-line: no-unbound-method
      props.onMouseMove,
      // tslint:disable-next-line: no-unbound-method
      props.onMouseDown,
      // tslint:disable-next-line: no-unbound-method
      props.onMouseUp,
      props.events ?
        (selection: Entities.SelectionWindow) => {
          if (props.events && props.events.onUpdateSelectionWindow) {
            props.events.onUpdateSelectionWindow(props.channelId, selection);
          }
         } : undefined,
      props.events ?
        (selection: Entities.SelectionWindow, timeSecs: number) => {
          if (props.events && props.events.onClickSelectionWindow) {
            props.events.onClickSelectionWindow(props.channelId, selection, timeSecs);
          }
        } : undefined,
      0
    )
    ]

  /**
   * Returns the time in seconds for the given clientX.
   * 
   * @param clientX The clientX
   * 
   * @returns The time in seconds; undefined if clientX is 
   * out of the channel's bounds on screen.
   */
  private readonly getTimeSecsForClientX = (clientX: number): number | undefined => {
    const canvasRef = this.props.canvasRef();

    if (!this.containerRef || !canvasRef) return;

    const offset = canvasRef.getBoundingClientRect();
    if (clientX < offset.left && clientX > offset.right) return undefined;

    // position in [0,1] in the current channel bounds.
    const position = (clientX - offset.left) / (offset.width);
    const time = this.props.computeTimeSecsForMouseXPosition(position);
    return time;
  }

  /**
   * onMouseEnter event handler
   */
  private readonly onMouseEnter = (): void => {
    if (!this.containerRef) return;
    this.containerRef.focus();
  }

  /**
   * onMouseLeave event handler
   */
  private readonly onMouseLeave = (): void => {
    if (!this.containerRef) return;
  }

  /**
   * Toggle display of the drag indicator for this channel
   * 
   * @param show True to show drag indicator
   * @param color The color of the drag indicator
   */
  private readonly toggleDragIndicator = (show: boolean, color: string): void => {
    if (!this.dragIndicatorRef) return;

    this.dragIndicatorRef.style.borderColor = color;
    this.dragIndicatorRef.style.display = show ? 'initial' : 'none';
  }

  /**
   * Set the position for the drag indicator
   * 
   * @param clientX The clientX 
   */
  private readonly positionDragIndicator = (clientX: number): void => {
    if (!this.containerRef || !this.dragIndicatorRef) return;

    const fracToPct = 100;
    const boundingRect = this.containerRef.getBoundingClientRect();
    // position in [0,1] in the current channel bounds.
    const position = (clientX - boundingRect.left) / boundingRect.width;
    this.dragIndicatorRef.style.left = `${position * fracToPct}%`;
  }

}
