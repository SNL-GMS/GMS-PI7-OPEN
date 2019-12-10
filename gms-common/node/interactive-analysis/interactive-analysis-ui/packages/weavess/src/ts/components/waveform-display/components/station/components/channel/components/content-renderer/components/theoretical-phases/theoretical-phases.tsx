import { isEqual } from 'lodash';
import memoizeOne from 'memoize-one';
import * as React from 'react';
import { calculateLeftPercent, calculateRightPercent } from '../../../../../../../../utils';
import {
  TheoreticalPhaseWindow
} from '../../../../../../../theoretical-phase-window';
import { TheoreticalPhasesProps, TheoreticalPhasesState } from './types';

export class TheoreticalPhases extends React.PureComponent<TheoreticalPhasesProps, TheoreticalPhasesState> {

  /** 
   * A memoized function for creating the theoretical phase window elements.
   * The memoization function caches the results using 
   * the most recent argument and returns the results. 
   *
   * @param props the theoretical phase props
   * 
   * @returns an array JSX elements
   */
  private readonly memoizedCreateTheoreticalPhaseWindowElements: (props: TheoreticalPhasesProps) => JSX.Element[];

  /**
   * Constructor
   * 
   * @param props Waveform props as TheoreticalPhasesProps
   */
  public constructor(props: TheoreticalPhasesProps) {
    super(props);
    this.memoizedCreateTheoreticalPhaseWindowElements = memoizeOne(
      TheoreticalPhases.createTheoreticalPhaseWindowElements,
      /* tell memoize to use a deep comparison for complex objects */
      isEqual);
    this.state = { };
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
  public static getDerivedStateFromProps(nextProps: TheoreticalPhasesProps) {
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
      <React.Fragment>
        {this.memoizedCreateTheoreticalPhaseWindowElements(this.props)}
      </React.Fragment>
    );
  }

  /**
   * Creates the theoretical phase window elements.
   * 
   * @param props the theoretical phase props
   * 
   * @returns an array of theoretical phase elements as JSX.Element
   */
  private static readonly createTheoreticalPhaseWindowElements = (props: TheoreticalPhasesProps): JSX.Element[] => {
    if (!props.theoreticalPhaseWindows) return [];

    return props.theoreticalPhaseWindows.map(theoreticalPhaseWindow => {
      const leftPos =
        calculateLeftPercent(
          theoreticalPhaseWindow.startTimeSecs,
          props.displayStartTimeSecs, props.displayEndTimeSecs);

      const rightPos =
        calculateRightPercent(
          theoreticalPhaseWindow.endTimeSecs,
          props.displayStartTimeSecs, props.displayEndTimeSecs);

      return (
        <TheoreticalPhaseWindow
          id={theoreticalPhaseWindow.id}
          key={theoreticalPhaseWindow.id}
          color={theoreticalPhaseWindow.color}
          left={leftPos}
          right={rightPos}
          label={theoreticalPhaseWindow.label}
        />
      );
    });
  }

}
