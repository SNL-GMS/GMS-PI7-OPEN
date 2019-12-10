import * as React from 'react';
import { Messages } from '../../../messages';
import { calculateLeftPercent } from '../../../utils';
import './styles.scss';
import { PickMarkerProps, PickMarkerState } from './types';

/**
 * An interactable marker, that is configurable, and can have specfic events.
 */
export class PickMarker extends React.PureComponent<PickMarkerProps, PickMarkerState> {

  /** container reference */
  private containerRef: HTMLDivElement | null;

  /** line reference */
  private lineRef: HTMLDivElement | null;

  /** label reference */
  private labelRef: HTMLDivElement | null;

  /**
   * Constructor
   * 
   * @param props props as PickMarkerProps
   */
  public constructor(props: PickMarkerProps) {
    super(props);
    this.state = {
      position: this.props.position
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
  public static getDerivedStateFromProps(nextProps: PickMarkerProps, prevState: PickMarkerState) {
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
    console.error(`Weavess Pick Marker Error: ${error} : ${info}`);
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
  public componentDidUpdate(prevProps: PickMarkerProps, prevState: PickMarkerState) {
    if (!this.lineRef) return;

    if (prevProps.position !== this.props.position) {
      this.setState({position: this.props.position});
    } else {
      // if the color changes, flash animation
      if (prevProps.color !== this.props.color) {
        this.lineRef.style.borderColor = this.props.color;
        setTimeout(() => {
          if (!this.lineRef) return;

          this.lineRef.style.borderColor = this.props.color;
          this.lineRef.style.transition = 'border-color 0.5s ease-in';
          setTimeout(() => {
            if (!this.lineRef) return;
            this.lineRef.style.transition = '';
            // tslint:disable-next-line:no-magic-numbers align
          }, 500);
          // tslint:disable-next-line:no-magic-numbers align
        }, 500);
      }
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

  public render() {

    // show the pointer cursor if the click or drag events are defined
    const cursor: React.CSSProperties = (this.props.onClick || this.props.onDragEnd) ?
      { cursor: 'pointer' } : { cursor: 'auto' };

    return (
      <div
        className="pick-marker"
        ref={ref => this.containerRef = ref}
        onMouseDown={this.props.onDragEnd ? this.onMouseDown : undefined}
        onMouseUp={this.props.onDragEnd ? this.onMouseUp : undefined}
      >
        <div
          className="pick-marker-pick"
          ref={ref => this.lineRef = ref}
          style={{
            borderLeft: '1.5px solid ' + this.props.color,
            bottom: this.props.predicted ? '10%' : '55%',
            // tslint:disable-next-line:no-magic-numbers
            left: `${this.state.position}%`,
            top: this.props.predicted ? '55%' : '10%',
            boxShadow: this.props.isSelected ? `0px 0px 10px 3px ${this.props.color}` : 'initial',
            filter: this.props.filter
          }}
        />
        <div
          className="pick-marker-label"
          onClick={this.props.onClick ? this.onClick : undefined}
          onContextMenu={this.props.onContextMenu ? this.onContextMenu : undefined}
          style={{
            bottom: this.props.predicted ? '10%' : 'initial',
            left: `calc(4px + ${this.state.position}%)`,
            top: this.props.predicted ? 'initial' : '10%',
            color: this.props.color,
            filter: this.props.filter,
            ...cursor
          }}
          ref={ref => this.labelRef = ref}
        >
          {this.props.label}
        </div>
      </div>
    );
  }

  /**
   * onClick event handler for signal detections 
   */
  private readonly onClick = (e: React.MouseEvent<HTMLDivElement>): void => {
    // prevent propagation of these events so that the underlying channel click doesn't register
    e.stopPropagation();
    if (this.props.onClick) {
      this.props.onClick(e, this.props.id);
    }
  }

  /**
   * onContextMenu menu event handler for signal detections 
   */
  private readonly onContextMenu = (e: React.MouseEvent<HTMLDivElement>): void => {
    e.stopPropagation();
    if (this.props.onContextMenu) {
      this.props.onContextMenu(e, this.props.channelId, this.props.id);
    }
  }

  /**
   * onMouseDown event handler for signal detections 
   */
  private readonly onMouseDown = (e: React.MouseEvent<HTMLDivElement>): void => {
    // prevent propagation of these events so that the underlying channel click doesn't register
    e.stopPropagation();
    // if context-menu, don't trigger
    if (e.button === 2) return;

    if (this.props.disableModification) {
      if (!this.props.predicted) {
        this.props.toast(Messages.signalDetectionModificationDisabled);
      } else {
        this.props.toast(Messages.predictedPhaseModificationDisabled);
      }
    } else {
      const start = e.clientX;
      let currentPos = e.clientX;
      let isDragging = false;
      let diff = 0;

      const onMouseMove = (event: MouseEvent) => {
        if (!this.containerRef) return;

        currentPos = event.clientX;
        diff = Math.abs(currentPos - start);
        // begin drag if moving more than 1 pixel
        if (diff > 1 && !isDragging) {
          isDragging = true;
          if (this.labelRef) {
            this.labelRef.style.filter = 'brightness(0.5)';
          }
          if (this.lineRef) {
            this.lineRef.style.filter = 'brightness(0.5)';
          }
          this.props.toggleDragIndicator(true, this.props.color);
        }
        if (isDragging) {
          this.props.positionDragIndicator(currentPos);
        }
      };

      const onMouseUp = (event: MouseEvent) => {
        event.stopPropagation();
        if (!this.containerRef) return;

        if (isDragging) {
          this.props.toggleDragIndicator(false, this.props.color);
          if (this.labelRef) {
            this.labelRef.style.filter = this.props.filter ? this.props.filter : 'initial';
          }
          if (this.lineRef) {
            this.lineRef.style.filter = this.props.filter ? this.props.filter : 'initial';
          }
          const time = this.props.getTimeSecsForClientX(currentPos);
          if (time) {
            this.setState(
            {
              position: calculateLeftPercent(time, this.props.startTimeSecs, this.props.endTimeSecs)
            },
            () => {
                if (this.props.onDragEnd) {
                  this.props.onDragEnd(this.props.id, time);
                }
            });
          }
        }
        document.body.removeEventListener('mousemove', onMouseMove);
        document.body.removeEventListener('mouseup', onMouseUp);
      };

      document.body.addEventListener('mousemove', onMouseMove);
      document.body.addEventListener('mouseup', onMouseUp);
    }
  }

  /**
   * onMouseUp event handler for signal detections 
   */
  private readonly onMouseUp = (e: React.MouseEvent<HTMLDivElement>): void => {
    e.stopPropagation();
  }
}
