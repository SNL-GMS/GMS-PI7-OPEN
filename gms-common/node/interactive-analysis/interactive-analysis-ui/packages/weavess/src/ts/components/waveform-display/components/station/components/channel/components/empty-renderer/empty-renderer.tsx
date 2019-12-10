import * as d3 from 'd3';
import * as React from 'react';
import * as THREE from 'three';
import { EmptyRendererProps, EmptyRendererState } from './types';

/**
 * Empty component. Renders and displays an empty graphics data.
 */
export class EmptyRenderer extends React.PureComponent<EmptyRendererProps, EmptyRendererState> {

  /** THREE.Scene for this channel */
  public scene: THREE.Scene;

  /** Orthographic camera used to zoom/pan around the spectogram */
  public camera: THREE.OrthographicCamera;

  /** Current min in gl units */
  private glMin: number = 0;

  /** Current max in gl units */
  private glMax: number = 100;

  /**
   * Constructor
   * 
   * @param props props as SpectrogramRendererProps
   */
  public constructor(props: EmptyRendererProps) {
    super(props);
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
  public static getDerivedStateFromProps() {
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
  public async componentDidMount() {
    this.scene = new THREE.Scene();
    const cameraZDepth = 5;
    this.camera = new THREE.OrthographicCamera(this.glMin, this.glMax, 1, -1, cameraZDepth, -cameraZDepth);
    this.camera.position.z = 0;
  }

  /**
   * Called immediately after updating occurs. Not called for the initial render.
   *
   * @param prevProps the previous props
   * @param prevState the previous state
   */
  public componentDidUpdate(prevProps: EmptyRendererProps, prevState: EmptyRendererState) {
    if (prevProps.displayStartTimeSecs !== this.props.displayStartTimeSecs ||
        prevProps.displayEndTimeSecs !== this.props.displayEndTimeSecs) {
      this.updateCameraBounds(prevProps);
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
    return (null);
  }

  /**
   * Update the min,max in gl units where we draw the spectrogram, if the view bounds have changed.
   * 
   * @param prevProps The previous waveform props
   */
  private readonly updateCameraBounds = (prevProps: EmptyRendererProps) => {
    const scale = d3.scaleLinear()
      .domain([prevProps.displayStartTimeSecs, prevProps.displayEndTimeSecs])
      .range([this.glMin, this.glMax]);

    const min = scale(this.props.displayStartTimeSecs);
    const max = scale(this.props.displayEndTimeSecs);
    this.glMin = min;
    this.glMax = max;
    this.camera.left = this.glMin;
    this.camera.right = this.glMax;
  }

}
