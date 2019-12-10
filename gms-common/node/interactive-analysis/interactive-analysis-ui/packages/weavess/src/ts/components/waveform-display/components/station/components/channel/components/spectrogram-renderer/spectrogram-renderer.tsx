import * as d3 from 'd3';
import { isEqual } from 'lodash';
import memoizeOne from 'memoize-one';
import * as React from 'react';
import * as THREE from 'three';
import { SpectrogramRendererProps, SpectrogramRendererState } from './types';

/**
 * Spectrogram component. Renders and displays spectrogram graphics data.
 */
export class SpectrogramRenderer extends React.PureComponent<SpectrogramRendererProps, SpectrogramRendererState> {

  /** THREE.Scene which holds the spectrograms for this channel */
  public scene: THREE.Scene;

  /** Orthographic camera used to zoom/pan around the spectogram */
  public camera: THREE.OrthographicCamera;

  /** Current min for all points in gl units */
  private glMin: number = 0;

  /** Current max for all points in gl units */
  private glMax: number = 100;

  /** 
   * A memoized function for creating the positions vertices.
   * The memoization function caches the results using 
   * the most recent argument and returns the results. 
   *
   * @param startTimeSecs the start time seconds
   * @param data the data
   * @param timeStep the time step
   * @param frequencyStep the frequency step
   * 
   * @returns a map of string ids to vertices array
   */
  private readonly memoizedCreatePositionVertices: (
    startTimeSecs: number,
    data: number[][],
    timeStep: number,
    frequencyStep: number) => Map<string, number[]>;

  /**
   * Constructor
   * 
   * @param props props as SpectrogramRendererProps
   */
  public constructor(props: SpectrogramRendererProps) {
    super(props);
    this.memoizedCreatePositionVertices = memoizeOne(this.createPositionVertices);
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

    this.renderSpectrogram();
  }

  /**
   * Called immediately after updating occurs. Not called for the initial render.
   *
   * @param prevProps the previous props
   * @param prevState the previous state
   */
  public componentDidUpdate(prevProps: SpectrogramRendererProps, prevState: SpectrogramRendererState) {
    if (this.props.displayStartTimeSecs !== prevProps.displayStartTimeSecs ||
      this.props.displayEndTimeSecs !== prevProps.displayEndTimeSecs ||
      this.props.timeStep !== prevProps.timeStep ||
      this.props.frequencyStep !== prevProps.frequencyStep ||
      !isEqual(this.props.data, prevProps.data)) {
      this.updateCameraBounds(prevProps);
      this.renderSpectrogram();
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
   * @param prevProps The previous props
   */
  private readonly updateCameraBounds = (prevProps: SpectrogramRendererProps) => {
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

  /**
   * Generates the color scale.
   * 
   * @param min The minumum frequency value
   * @param max THe mamximum frequency value
   * 
   * @returns D3 object that turns values into colors d3.ScaleSequential<d3.HSLColor>
   */
  // tslint:disable-next-line:member-ordering
  private readonly createColorScale: any = (min: number, max: number) => d3
    .scaleSequential((t): string => {
      if (t < 0 || t > 1) {
        // tslint:disable-next-line:no-parameter-reassignment
        t -= Math.floor(t);
      }
      // tslint:disable-next-line:no-magic-numbers
      const ts = Math.abs(t - 0.5);
      // map to range [240, 0] hue
      // tslint:disable-next-line:no-magic-numbers binary-expression-operand-order newline-per-chained-call
      return d3.hsl(240 - (240 * t), 1.5 - 1.5 * ts, 0.8 - 0.9 * ts).toString();
    })
    .domain([min, max])

  /**
   * Renders the spectrogram
   */
  private readonly renderSpectrogram = () => {
    if (this.props.startTimeSecs && this.props.data && this.props.timeStep && this.props.frequencyStep) {

      const buffer = this.memoizedCreatePositionVertices(
        this.props.startTimeSecs, this.props.data, this.props.timeStep, this.props.frequencyStep);

      while (this.scene.children.length > 0) {
        this.scene.remove(this.scene.children[0]);
      }

      const meshGroup: THREE.Group = new THREE.Group();
      buffer.forEach((vertices, color) => {
        const geometry = new THREE.BufferGeometry();
        geometry.addAttribute('position', new THREE.BufferAttribute(new Float32Array(vertices), 3));
        const material = new THREE.MeshBasicMaterial({ color });
        const mesh = new THREE.Mesh(geometry, material);
        meshGroup.add(mesh);
      });
      this.scene.add(meshGroup);

      this.camera.top = this.props.data[0].length * this.props.frequencyStep;
      this.camera.bottom = 0;
      this.props.setYAxisBounds(this.camera.bottom, this.camera.top);
      this.camera.updateProjectionMatrix();
    }
  }

  /**
   * Creates the position vertices.
   *
   * @param startTimeSecs the start time seconds
   * @param data the data
   * @param timeStep the time step
   * @param frequencyStep the frequency step
   * 
   * @returns a map of string ids to vertices array
   */
  private readonly createPositionVertices = (
    startTimeSecs: number,
    data: number[][],
    timeStep: number,
    frequencyStep: number): Map<string, number[]> => {

    const min = Math.min(...data.map(intensity => Math.min(...intensity)));
    const max = Math.max(...data.map(intensity => Math.max(...intensity)));
    const colorScale = this.props.colorScale ? this.props.colorScale :  this.createColorScale(min, max);

    // create a buffer that groups the vertices by color, to help increase the performance or rendering
    const buffer: Map<string, number[]> = new Map();

    const timeToGlScale = d3.scaleLinear()
      .domain([this.props.displayStartTimeSecs, this.props.displayEndTimeSecs])
      .range([this.glMin, this.glMax]);

    let time = startTimeSecs;
    for (const timeValue of data) {
      let freq = 0;
      for (const freqValue of timeValue) {
        // create a simple square shape. duplicate the top left and bottom right
        // vertices because each vertex needs to appear once per triangle.
        // tslint:disable-next-line:no-magic-numbers
        const vertices = [
          timeToGlScale(time), (freq), 0,
          timeToGlScale(time + timeStep), (freq), 0,
          timeToGlScale(time + timeStep), (freq + frequencyStep), 0,

          timeToGlScale(time + timeStep), (freq + frequencyStep), 0,
          timeToGlScale(time), (freq + frequencyStep), 0,
          timeToGlScale(time), (freq), 0,
        ];
        const color: string = colorScale(freqValue);

        if (buffer.has(color)) {
          const colorBuffer = buffer.get(color);
          if (colorBuffer) {
            colorBuffer.push(...vertices);
          } else {
            buffer.set(color, [...vertices]);
          }
        } else {
          buffer.set(color, [...vertices]);
        }
        freq += frequencyStep;
      }
      time += timeStep;
    }
    return buffer;
  }
}
