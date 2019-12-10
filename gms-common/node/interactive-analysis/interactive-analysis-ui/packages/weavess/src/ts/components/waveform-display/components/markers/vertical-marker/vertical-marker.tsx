import * as React from 'react';
import './styles.scss';
import { VerticalMarkerProps, VerticalMarkerState } from './types';

/**
 * VerticalMarker Component. Is not moveable
 */
export class VerticalMarker extends React.PureComponent<VerticalMarkerProps, VerticalMarkerState> {

  /** Ref to the marker container element */
  public containerRef: HTMLElement | null;

  /**
   * Constructor
   * 
   * @param props Vertical Marker props as VerticalMarkerProps
   */
  public constructor(props: VerticalMarkerProps) {
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
  public static getDerivedStateFromProps(nextProps: VerticalMarkerProps, prevState: VerticalMarkerState) {
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
    console.error(`Weavess Vertical Marker Error: ${error} : ${info}`);
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
  public componentDidUpdate(prevProps: VerticalMarkerProps, prevState: VerticalMarkerState) {
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
        className="vertical-marker"
        ref={ref => this.containerRef = ref}
        style={{
          left: `${this.props.percentageLocation}%`,
          border: `1px ${this.props.lineStyle} ${this.props.color}`,
        }}
      />
    );
  }
}
