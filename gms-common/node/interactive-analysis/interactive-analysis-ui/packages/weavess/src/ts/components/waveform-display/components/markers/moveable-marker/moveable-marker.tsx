import * as React from 'react';
import * as Entities from '../../../../../entities';
import './styles.scss';
import { MoveableMarkerProps, MoveableMarkerState } from './types';

/**
 * MoveableMarker Component. Vertical or Horizontal line that is moveable
 */
export class MoveableMarker extends React.PureComponent<MoveableMarkerProps, MoveableMarkerState> {

  /** Ref to the marker container element */
  public containerRef: HTMLElement | null;

  /**
   * Constructor
   * 
   * @param props Moveable Marker props as MoveableMarkerProps
   */
  public constructor(props: MoveableMarkerProps) {
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
  public static getDerivedStateFromProps(nextProps: MoveableMarkerProps, prevState: MoveableMarkerState) {
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
    console.error(`Weavess Moveable Marker Error: ${error} : ${info}`);
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
  public componentDidUpdate(prevProps: MoveableMarkerProps, prevState: MoveableMarkerState) {
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
    return (
      <div
        className={'moveable-marker'}
        ref={ref => this.containerRef = ref}
        style={{
          left: `${this.props.percentageLocation}%`,
          border: `1px ${this.props.marker.lineStyle} ${this.props.marker.color}`
        }}
        onMouseDown={this.onMoveableMarkerClick}
      />
    );
  }

  /**
   * Returns the minimum constraint of the moveable marker.
   */
  public readonly getMinConstraint = (): number => {
    let minTimeSecsConstraint = this.props.marker.minTimeSecsConstraint;
    if (minTimeSecsConstraint) {
      if (this.props.associatedStartMarker) {
        if (this.props.associatedStartMarker.timeSecs > minTimeSecsConstraint) {
          minTimeSecsConstraint = this.props.associatedStartMarker.timeSecs;
        }
      }
    } else {
      // use the associated start marker if one exists
      minTimeSecsConstraint =
        (this.props.associatedStartMarker) ? this.props.associatedStartMarker.timeSecs : undefined;
    }

    return minTimeSecsConstraint || this.props.timeRange().startTimeSecs;
  }

  /**
   * Returns the minimum constraint percentage of the moveable marker.
   */
  public readonly getMinConstraintPercentage = (): number => {
    const smallPaddingPercent = this.getSmallPaddingPercent();

    const timeRange: Entities.TimeRange =  this.props.timeRange();
    const timeWindow = timeRange.endTimeSecs - timeRange.startTimeSecs;

    const minTimeSecsConstraint = this.getMinConstraint();
    const min = minTimeSecsConstraint - timeRange.startTimeSecs;
    const minPercent = (min / timeWindow) + smallPaddingPercent;
    // tslint:disable-next-line:no-magic-numbers
    return minPercent * 100;
  }

  /**
   * Returns the maximum constraint of the moveable marker.
   */
  public readonly getMaxConstraint = (): number => {
    let maxTimeSecsConstraint = this.props.marker.maxTimeSecsConstraint;
    if (maxTimeSecsConstraint) {
      if (this.props.associatedEndMarker) {
        if (this.props.associatedEndMarker.timeSecs < maxTimeSecsConstraint) {
          maxTimeSecsConstraint = this.props.associatedEndMarker.timeSecs;
        }
      }
    } else {
      // use the associated end marker if one exists
      maxTimeSecsConstraint =
        (this.props.associatedEndMarker) ? this.props.associatedEndMarker.timeSecs : undefined;
    }

    // tslint:disable-next-line:no-magic-numbers
    return maxTimeSecsConstraint ||  this.props.timeRange().endTimeSecs;
  }

  /**
   * Returns the maximum constraint percentage of the moveable marker.
   */
  public readonly getMaxConstraintPercentage = (): number => {
    const smallPaddingPercent = this.getSmallPaddingPercent();

    const timeRange: Entities.TimeRange =  this.props.timeRange();
    const timeWindow = timeRange.endTimeSecs - timeRange.startTimeSecs;

    const maxTimeSecsConstraint = this.getMaxConstraint();
    const max = maxTimeSecsConstraint - timeRange.startTimeSecs;
    const maxPercent = (max / timeWindow) - smallPaddingPercent;
    // tslint:disable-next-line:no-magic-numbers
    return maxPercent * 100;
  }

  /**
   * Move logic for the markers. Creates mouse move and up listeners to determine
   * Where it should be moved. Only works for pairs currently, if more than two markers
   * Depend on each other, will need to be refactored. 
   */
  private readonly onMoveableMarkerClick = (e: React.MouseEvent<HTMLDivElement>) => {
    e.stopPropagation();
    const htmlEle: HTMLDivElement = e.target as HTMLDivElement;
    const mouseXOffset = e.clientX - htmlEle.offsetLeft; // Beggining X position of waveform display
    const fracPrecentage = 100;
    const viewPortWidth = this.props.viewportClientWidth();
    const zoomRatio = viewPortWidth / (this.props.containerClientWidth() - this.props.labelWidthPx);

    const onMouseMove = (event: MouseEvent) => {
      if (!htmlEle) return;
      const mouseXPrct = (event.clientX - mouseXOffset) / viewPortWidth * zoomRatio;
      const offsetPercentGuard = 99;
      // Get the limited position based on the other moveable div (if exist)
      const timeWindow = this.props.timeRange().endTimeSecs - this.props.timeRange().startTimeSecs;
      let newPosPercent = this.determineMoveableMarkerPosition(mouseXPrct);

      // Guard to ensure stays on waveform
      newPosPercent = newPosPercent < 0 ? 0 : newPosPercent;
      newPosPercent = newPosPercent > offsetPercentGuard ? offsetPercentGuard : newPosPercent;
      htmlEle.style.left = `${newPosPercent * fracPrecentage}%`;
      const timeSecs = (newPosPercent) * timeWindow + this.props.timeRange().startTimeSecs;

      // !FIX DO NOT SET THE PROPS DIRECTLY
      this.props.marker.timeSecs = timeSecs;

      if (this.props.updateTimeWindowSelection) {
        this.props.updateTimeWindowSelection();
      }
    };

    const onMouseUp = (event: MouseEvent) => {
      if (this.props.onUpdateMarker) {
        this.props.onUpdateMarker(this.props.marker);
      }
      document.body.removeEventListener('mouseup', onMouseUp);
      document.body.removeEventListener('mousemove', onMouseMove);
    };
    document.body.addEventListener('mousemove', onMouseMove);
    document.body.addEventListener('mouseup', onMouseUp);
  }

  /** 
   * Returns an small padding percentage.
   */
  private readonly getSmallPaddingPercent = () => {
    const smallPercent = 0.001;
    return ((this.props.viewTimeRange().endTimeSecs - this.props.viewTimeRange().startTimeSecs) /
        (this.props.timeRange().endTimeSecs - this.props.timeRange().startTimeSecs)) * smallPercent;
  }

  /**
   * Returns the moveable marker position based on on a percentage provided.
   */
  private readonly determineMoveableMarkerPosition = (currentPercent: number): number => {
    const smallPaddingPercent = this.getSmallPaddingPercent();

    const timeRange: Entities.TimeRange =  this.props.timeRange();
    const timeWindow = timeRange.endTimeSecs - timeRange.startTimeSecs;

    const minTimeSecsConstraint = this.getMinConstraint();

    const min: number = (minTimeSecsConstraint && minTimeSecsConstraint < timeRange.startTimeSecs) ?
      timeRange.startTimeSecs : minTimeSecsConstraint || timeRange.startTimeSecs;
    const minTime = min - timeRange.startTimeSecs;
    const minPercent = (minTime / timeWindow) + smallPaddingPercent;

    const maxTimeSecsConstraint = this.getMaxConstraint();

    const max: number = (maxTimeSecsConstraint && maxTimeSecsConstraint > timeRange.endTimeSecs) ?
      timeRange.endTimeSecs : maxTimeSecsConstraint || timeRange.endTimeSecs;
    const maxTime = max - timeRange.startTimeSecs;
    const maxPercent = (maxTime / timeWindow) - smallPaddingPercent;

    if (currentPercent < minPercent) {
      return minPercent;
    } else if (currentPercent > maxPercent) {
      return maxPercent;
    }
    return currentPercent;
  }
}
