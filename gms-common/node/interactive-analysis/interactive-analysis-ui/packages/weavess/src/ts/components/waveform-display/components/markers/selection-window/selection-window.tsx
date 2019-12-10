import * as React from 'react';
import * as Entities from '../../../../../entities';
import { SingleDoubleClickEvent } from '../../..//events/single-double-click-event';
import { calculateLeftPercent } from '../../..//utils';
import { MoveableMarker } from '../moveable-marker';
import { VerticalMarker } from '../vertical-marker';
import './styles.scss';
import { SelectionWindowProps, SelectionWindowState } from './types';

/**
 * SelectionWindow Component. Contains two moveable markers.
 */
export class SelectionWindow extends React.PureComponent<SelectionWindowProps, SelectionWindowState> {

  /** Ref to the time window selection */
  private timeWindowSelectionRef: HTMLDivElement | null;

  /** Ref to the lead marker */
  private leadBorderRef: VerticalMarker | MoveableMarker | null;

  /** Ref to the lag marker */
  private endBorderRef: VerticalMarker | MoveableMarker | null;

  /** indicates if the mouse is down */
  private mouseDown: boolean = false;

  /** indicates if the mouse is dragging */
  private isDragging: boolean = false;

  /** handler for handling single and double click events */
  private readonly handleSingleDoubleClick: SingleDoubleClickEvent = new SingleDoubleClickEvent();

  /**
   * Constructor
   * 
   * @param props Selection Window props as SelectionWindowProps
   */
  public constructor(props: SelectionWindowProps) {
    super(props);
    this.state = {};
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
  public static getDerivedStateFromProps(nextProps: SelectionWindowProps, prevState: SelectionWindowState) {
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
    console.error(`Weavess Selection Window Error: ${error} : ${info}`);
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
  public componentDidUpdate(prevProps: SelectionWindowProps, prevState: SelectionWindowState) {
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
    const percent100 = 100;

    const leftPercent = calculateLeftPercent(
      this.props.selectionWindow.startMarker.timeSecs,
      this.props.timeRange().startTimeSecs, this.props.timeRange().endTimeSecs);
    const rightPercent = (percent100 -
      calculateLeftPercent(
        this.props.selectionWindow.endMarker.timeSecs,
        this.props.timeRange().startTimeSecs, this.props.timeRange().endTimeSecs));
    return (
      <div
        className="selection-window"
        onMouseDown={this.onMouseDown}
        onMouseMove={this.onMouseMove}
        onMouseUp={this.onMouseUp}
      >
        <div
          ref={ref => this.timeWindowSelectionRef = ref}
          className="selection-window-selection"
          style={{
            backgroundColor: `${this.props.selectionWindow.color}`,
            left: `${leftPercent}%`,
            right: `${rightPercent}%`,
            cursor: (this.props.selectionWindow.isMoveable) ? 'move' : 'auto',
          }}
          onMouseDown={e => this.onSelectionWindowClick(e)}
          onDoubleClick={this.handleSingleDoubleClick.onDoubleClick}
        />
        {this.createMarkers()}
      </div>
    );
  }

  /**
   * Selection window on click logic, creates mouse move and mouse down
   * Listeners to determine where to move the window and the markers. 
   */
  private readonly onSelectionWindowClick = (event: React.MouseEvent<HTMLDivElement>) => {
    if (event.button === 2 || event.altKey || event.ctrlKey || event.metaKey) return;

    event.stopPropagation();

    if (!this.mouseDown) {
      this.mouseDown = true;

      const htmlEle: HTMLDivElement = event.target as HTMLDivElement;
      const timeRange = (this.props.timeRange().endTimeSecs - this.props.timeRange().startTimeSecs);
      const mouseXOffset = event.clientX - htmlEle.offsetLeft;
      const viewPortWidth = this.props.viewportClientWidth();
      const precentFrac = 100;
      let startXPercent = (event.clientX - mouseXOffset) / viewPortWidth;

      // calculate initial start time
      const timeSecs = this.getTimeSecsForClientX(event.clientX);

      const onMouseMove = (mouseMoveEvent: MouseEvent) => {
        if (this.mouseDown) {
          const currentXPercent = (mouseMoveEvent.clientX - mouseXOffset) / viewPortWidth;

          let diffPct = startXPercent - currentXPercent;

          const diffTimeSecs = timeRange * diffPct;

          // the mouse is considered to be dragging if the user has moved greater than 50ms
          const mouseMoveConstraint = 0.05; // represents 50 ms

          if (!this.isDragging && Math.abs(diffTimeSecs) > mouseMoveConstraint) {
            this.isDragging = true;
          }

          diffPct *= precentFrac;

          if (this.isDragging) {
            if (htmlEle && htmlEle.style.left && htmlEle.style.right) {
              let divLeftPercent = htmlEle.style.left ? parseFloat(htmlEle.style.left) - diffPct : 0;
              let divRightPercent = htmlEle.style.right ? parseFloat(htmlEle.style.right) + diffPct : 0;

              if (this.leadBorderRef && this.leadBorderRef.containerRef &&
                this.leadBorderRef.containerRef.style.left && this.endBorderRef &&
                this.endBorderRef.containerRef && this.endBorderRef.containerRef.style.left) {

                const leadPosition = parseFloat(this.leadBorderRef.containerRef.style.left);
                const lagPosition = parseFloat(this.endBorderRef.containerRef.style.left);
                let leadPositionPercent = leadPosition - diffPct;
                let lagPositionPercent = lagPosition - diffPct;

                // Guard to ensure stays on waveform
                // Guard to ensure stays with min and max contraints
                if (leadPositionPercent < (this.leadBorderRef as MoveableMarker).getMinConstraintPercentage() ||
                    lagPositionPercent > (this.endBorderRef as MoveableMarker).getMaxConstraintPercentage()) {
                  leadPositionPercent = leadPosition;
                  lagPositionPercent = lagPosition;
                  divLeftPercent = parseFloat(htmlEle.style.left);
                  divRightPercent = parseFloat(htmlEle.style.right);
                } else {
                  this.props.selectionWindow.startMarker.timeSecs -= diffTimeSecs;
                  this.props.selectionWindow.endMarker.timeSecs -= diffTimeSecs;
                  startXPercent = currentXPercent;
                }

                htmlEle.style.left = `${divLeftPercent}%`;
                htmlEle.style.right = `${divRightPercent}%`;
                this.leadBorderRef.containerRef.style.left = `${leadPositionPercent}%`;
                this.endBorderRef.containerRef.style.left = `${lagPositionPercent}%`;
              }
            }
          }
        }
      };

      const onMouseUp = (mouseUpEvent: MouseEvent) => {
        document.body.removeEventListener('mousemove', onMouseMove);
        document.body.removeEventListener('mouseup', onMouseUp);

        if (this.isDragging) {
          // only update if the selection window is moveable; no false updates
          if (this.props.selectionWindow.isMoveable && this.props.onUpdateSelectionWindow) {
            this.props.onUpdateSelectionWindow(this.props.selectionWindow);
          }
        } else {
          // handle a sigle click event
          this.handleSingleDoubleClick.onSingleClickEvent(
            mouseUpEvent,
            (e: React.MouseEvent<HTMLDivElement> | MouseEvent) => {
              if (this.props.onClickSelectionWindow && timeSecs) {
                this.props.onClickSelectionWindow(this.props.selectionWindow, timeSecs);
              }
          });
        }

        this.isDragging = false;
        this.mouseDown = false;

      };

      document.body.addEventListener('mousemove', onMouseMove);
      document.body.addEventListener('mouseup', onMouseUp);
    }
  }

  /**
   * Create boarder markers
   */
  private readonly createMarkers = (): JSX.Element[] => {
    if (!this.props.selectionWindow) return [];
    const borderMarkers: JSX.Element[] = [];
    borderMarkers.push(
      this.props.selectionWindow.isMoveable ?
      (
        <MoveableMarker
          ref={ref => this.leadBorderRef = ref}
          key={`moveable-marker-start`}
          marker={this.props.selectionWindow.startMarker}
          associatedEndMarker={this.props.selectionWindow.endMarker}
          labelWidthPx={this.props.labelWidthPx}
          percentageLocation={
            calculateLeftPercent(
              this.props.selectionWindow.startMarker.timeSecs,
              this.props.timeRange().startTimeSecs, this.props.timeRange().endTimeSecs)}
          containerClientWidth={this.props.containerClientWidth}
          viewportClientWidth={this.props.viewportClientWidth}
          updateTimeWindowSelection={this.updateTimeWindowSelection}
          timeRange={this.props.timeRange}
          viewTimeRange={this.props.viewTimeRange}
          onUpdateMarker={this.onUpdateMarker}
        />
      ) :
      (
        <VerticalMarker
          key={`vertical-marker-start`}
          color={this.props.selectionWindow.startMarker.color}
          lineStyle={this.props.selectionWindow.startMarker.lineStyle}
          percentageLocation={
            calculateLeftPercent(
              this.props.selectionWindow.startMarker.timeSecs,
              this.props.timeRange().startTimeSecs, this.props.timeRange().endTimeSecs)}
        />
      )
    );

    borderMarkers.push(
      this.props.selectionWindow.isMoveable ?
      (
        <MoveableMarker
          ref={ref => this.endBorderRef = ref}
          key={`moveable-marker-end`}
          labelWidthPx={this.props.labelWidthPx}
          marker={this.props.selectionWindow.endMarker}
          associatedStartMarker={this.props.selectionWindow.startMarker}
          percentageLocation={
            calculateLeftPercent(
              this.props.selectionWindow.endMarker.timeSecs,
              this.props.timeRange().startTimeSecs, this.props.timeRange().endTimeSecs)}
          containerClientWidth={this.props.containerClientWidth}
          viewportClientWidth={this.props.viewportClientWidth}
          updateTimeWindowSelection={this.updateTimeWindowSelection}
          timeRange={this.props.timeRange}
          viewTimeRange={this.props.viewTimeRange}
          onUpdateMarker={this.onUpdateMarker}
        />
      ) :
      (
        <VerticalMarker
          key={`vertical-marker-end`}
          color={this.props.selectionWindow.startMarker.color}
          lineStyle={this.props.selectionWindow.startMarker.lineStyle}
          percentageLocation={
            calculateLeftPercent(
              this.props.selectionWindow.endMarker.timeSecs,
              this.props.timeRange().startTimeSecs, this.props.timeRange().endTimeSecs)}
        />
      )
    );
    return borderMarkers;
  }

  /**
   * Handles the on update marker event and updates the selection
   */
  private readonly onUpdateMarker = (marker: Entities.Marker) => {
    // only update if the selection window is moveable; no false updates
    if (this.props.selectionWindow.isMoveable && this.props.onUpdateSelectionWindow) {
      this.props.onUpdateSelectionWindow(this.props.selectionWindow);
    }
  }

  /**
   * update time window div based on vertical markers moving
   */
  private readonly updateTimeWindowSelection = () => {
    if (!this.timeWindowSelectionRef || !this.endBorderRef || !this.leadBorderRef ||
      !this.leadBorderRef.containerRef || !this.endBorderRef.containerRef) return;

    const percent100 = 100;
    if (this.leadBorderRef.containerRef.style.left && this.endBorderRef.containerRef.style.left) {
      this.timeWindowSelectionRef.style.left =
        `${(parseFloat(this.leadBorderRef.containerRef.style.left))}%`;
      this.timeWindowSelectionRef.style.right =
        `${(percent100 - parseFloat(this.endBorderRef.containerRef.style.left)) }%`;
    }
  }

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

    if (!canvasRef) return;

    const offset = canvasRef.getBoundingClientRect();
    if (clientX < offset.left && clientX > offset.right) return undefined;

    // position in [0,1] in the current channel bounds.
    const position = (clientX - offset.left) / (offset.width);
    const time = this.props.computeTimeSecsForMouseXPosition(position);
    return time;
  }

  /**
   * onMouseDown event handler.
   * 
   * @param e mouse event as React.MouseEvent<HTMLDivElement>
   */
  private readonly onMouseDown = (e: React.MouseEvent<HTMLDivElement>) => {
    if (!this.timeWindowSelectionRef) return;
    this.props.onMouseDown(e);
  }

  /**
   * onMouseMove event handler.
   * 
   * @param e mouse event as React.MouseEvent<HTMLDivElement>
   */
  private readonly onMouseMove = (e: React.MouseEvent<HTMLDivElement>) => {
    if (!this.timeWindowSelectionRef) return;
    this.props.onMouseMove(e);
  }

  /**
   * onMouseUp event handler.
   * 
   * @param e mouse event as React.MouseEvent<HTMLDivElement>
   */
  private readonly onMouseUp = (e: React.MouseEvent<HTMLDivElement>) => {
    if (!this.timeWindowSelectionRef) return;
    this.props.onMouseUp(e);
  }
}
