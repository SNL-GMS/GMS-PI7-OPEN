import * as React from 'react';
import './styles.scss';
import {
  TheoreticalPhaseWindowProps,
  TheoreticalPhaseWindowState
} from './types';

/**
 * Displays a window of time on a channel where a phase may theoretically exist.
 */
export class TheoreticalPhaseWindow extends
  React.PureComponent<TheoreticalPhaseWindowProps, TheoreticalPhaseWindowState> {

  /**
   * Constructor
   * 
   * @param Theoretical Phase Window Props
   */
  public constructor(props: TheoreticalPhaseWindowProps) {
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
  public static getDerivedStateFromProps(
      nextProps: TheoreticalPhaseWindowProps, prevState: TheoreticalPhaseWindowState) {
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
    console.error(`Weavess Theoretical Phase Window Error: ${error} : ${info}`);
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
  public componentDidUpdate(prevProps: TheoreticalPhaseWindowProps, prevState: TheoreticalPhaseWindowState) {
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
        className="theoretical-phase-window"
      >
        <div
          className="theoretical-phase-window-selection"
          style={{
            backgroundColor: this.props.color,
            left: `${this.props.left}%`,
            right: `${this.props.right}%`,
          }}
        />
        <div
          className="theoretical-phase-window-label"
          style={{
            color: this.props.color,
            left: `${this.props.left}%`,
            right: `${this.props.right}%`,
          }}
        >
          {this.props.label}
        </div>
      </div>
    );
  }
}
