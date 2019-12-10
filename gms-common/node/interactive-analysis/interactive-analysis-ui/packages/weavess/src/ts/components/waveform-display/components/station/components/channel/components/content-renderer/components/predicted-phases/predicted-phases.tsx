import { isEqual } from 'lodash';
import memoizeOne from 'memoize-one';
import * as React from 'react';
import { calculateLeftPercent } from '../../../../../../../../utils';
import {
  PickMarker
} from '../../../../../../../markers';
import { PredictedPhasesProps, PredictedPhasesState } from './types';

export class PredictedPhases extends React.PureComponent<PredictedPhasesProps, PredictedPhasesState> {

  /** 
   * A memoized function for creating the predicted phases.
   * The memoization function caches the results using 
   * the most recent argument and returns the results. 
   *
   * @param props the predicted phase props
   * 
   * @returns an array JSX elements
   */
  private readonly memoizedCreatePredictivePhaseElements: (props: PredictedPhasesProps) => JSX.Element[];

  /**
   * Constructor
   * 
   * @param props Waveform props as PredictedPhasesProps
   */
  public constructor(props: PredictedPhasesProps) {
    super(props);
    this.memoizedCreatePredictivePhaseElements = memoizeOne(
      PredictedPhases.createPredictivePhaseElements,
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
  public static getDerivedStateFromProps(nextProps: PredictedPhasesProps) {
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
        {this.memoizedCreatePredictivePhaseElements(this.props)}
      </React.Fragment>
    );
  }

  /**
   * Creates Pedictive phase components
   * 
   * @returns an array of predictive phase elements as JSX.Element
   */
  private static readonly createPredictivePhaseElements = (props: PredictedPhasesProps): JSX.Element[] => {
    if (!props.predictedPhases) return [];

    return props.predictedPhases.map(predictivePhase => {

      const predictivePhasePosition =
        calculateLeftPercent(predictivePhase.timeSecs, props.displayStartTimeSecs, props.displayEndTimeSecs);

      const isSelected: boolean = props.selectedPredictedPhases ?
        props.selectedPredictedPhases.indexOf(predictivePhase.id) > -1 : false;

      return (
        <PickMarker
          key={predictivePhase.id}
          channelId={props.channelId}
          predicted={true}
          isSelected={isSelected}
          startTimeSecs={props.displayStartTimeSecs}
          endTimeSecs={props.displayEndTimeSecs}
          {...predictivePhase}
          position={predictivePhasePosition}
          disableModification={props.isDefaultChannel ?
            props.configuration.defaultChannel.disablePreditedPhaseModification :
            props.configuration.nonDefaultChannel.disablePreditedPhaseModification
          }
          // tslint:disable:no-unbound-method
          toast={props.toast}
          getTimeSecsForClientX={props.getTimeSecsForClientX}
          // tslint:disable:no-unbound-method
          onClick={props.events && props.events.onPredictivePhaseClick ?
            props.events.onPredictivePhaseClick : undefined}
          onContextMenu={props.events && props.events.onPredictivePhaseContextMenu ?
            props.events.onPredictivePhaseContextMenu : undefined}
          // tslint:disable:no-unbound-method
          onDragEnd={props.events && props.events.onPredictivePhaseDragEnd ?
            props.events.onPredictivePhaseDragEnd : undefined}
          toggleDragIndicator={props.toggleDragIndicator}
          positionDragIndicator={props.positionDragIndicator}
        />
      );
    });
  }

}
