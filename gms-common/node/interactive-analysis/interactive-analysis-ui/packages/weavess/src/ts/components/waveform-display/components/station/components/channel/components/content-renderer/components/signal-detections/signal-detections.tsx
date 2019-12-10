import { isEqual } from 'lodash';
import memoizeOne from 'memoize-one';
import * as React from 'react';
import { calculateLeftPercent } from '../../../../../../../../utils';
import {
  PickMarker
} from '../../../../../../../markers';
import { SignalDetectionsProps, SignalDetectionsState } from './types';

export class SignalDetections extends React.PureComponent<SignalDetectionsProps, SignalDetectionsState> {

  /** 
   * A memoized function for creating the signal detection elements.
   * The memoization function caches the results using 
   * the most recent argument and returns the results. 
   *
   * @param props the signal detection props
   * 
   * @returns an array JSX elements
   */
  private readonly memoizedCreateSignalDetectionElements: (props: SignalDetectionsProps) => JSX.Element[];

  /**
   * Constructor
   * 
   * @param props Waveform props as SignalDetectionsProps
   */
  public constructor(props: SignalDetectionsProps) {
    super(props);
    this.memoizedCreateSignalDetectionElements = memoizeOne(
      SignalDetections.createSignalDetectionElements,
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
  public static getDerivedStateFromProps(nextProps: SignalDetectionsProps) {
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
        {this.memoizedCreateSignalDetectionElements(this.props)}
      </React.Fragment>
    );
  }

  /**
   * Creates SignalDetection components
   * 
   * @param props the signal detection props
   * 
   * @returns an array of signal detection elements as JSX.Element
   */
  private static readonly createSignalDetectionElements = (props: SignalDetectionsProps): JSX.Element[] => {
    if (!props.signalDetections) return [];

    return props.signalDetections.map(signalDetection => {

      const signalDetectionPosition =
        calculateLeftPercent(signalDetection.timeSecs, props.displayStartTimeSecs, props.displayEndTimeSecs);

      const isSelected: boolean = props.selectedSignalDetections ?
        props.selectedSignalDetections.indexOf(signalDetection.id) > -1 : false;

      return (
        <PickMarker
          key={signalDetection.id}
          channelId={props.channelId}
          predicted={false}
          isSelected={isSelected}
          startTimeSecs={props.displayStartTimeSecs}
          endTimeSecs={props.displayEndTimeSecs}
          {...signalDetection}
          position={signalDetectionPosition}
          disableModification={props.isDefaultChannel ?
            props.configuration.defaultChannel.disableSignalDetectionModification :
            props.configuration.nonDefaultChannel.disableSignalDetectionModification
          }
          // tslint:disable:no-unbound-method
          toast={props.toast}
          getTimeSecsForClientX={props.getTimeSecsForClientX}
          // tslint:disable:no-unbound-method
          onClick={props.events && props.events.onSignalDetectionClick ?
            props.events.onSignalDetectionClick : undefined}
          onContextMenu={props.events && props.events.onSignalDetectionContextMenu ?
            props.events.onSignalDetectionContextMenu : undefined}
          // tslint:disable:no-unbound-method
          onDragEnd={props.events && props.events.onSignalDetectionDragEnd ?
            props.events.onSignalDetectionDragEnd : undefined}
          toggleDragIndicator={props.toggleDragIndicator}
          positionDragIndicator={props.positionDragIndicator}
        />
      );
    });
  }

}
