import * as d3 from 'd3';
import * as lodash from 'lodash';
import * as React from 'react';
import * as THREE from 'three';
import * as Entities from '../../../../../../../../entities';
import { createPositionBuffer } from '../../../../../../../../workers/create-position-buffer';
import {
  ChannelSegmentBoundries,
  Float32ArrayWithStartTime,
  WaveformRendererProps,
  WaveformRendererState
} from './types';

/**
 * Waveform component. Renders and displays waveform graphics data.
 */
export class WaveformRenderer extends React.PureComponent<WaveformRendererProps, WaveformRendererState> {

  /** Default channel props, if not provided */
  public static readonly defaultProps: Entities.ChannelDefaultConfiguration = {
    displayType: [Entities.DisplayType.LINE],
    pointSize: 2,
    color: '#4580E6'
  };

  /** THREE.Scene which holds the waveforms for this channel */
  public scene: THREE.Scene;

  /** Orthographic camera used to zoom/pan around the waveform */
  public camera: THREE.OrthographicCamera;

  /** Flag that indicates the apmplitude is being adjusted */
  public isAdjustingAmplitude: boolean = false;

  /** References to the masks drawn on the scene. */
  private readonly renderedMaskRefs: THREE.Mesh[] = [];

  /** Current min for all waveforms in gl units */
  private glMin: number = 0;

  /** Current max for all waveforms in gl units */
  private glMax: number = 100;

  /** Camera max top value for specific channel. */
  private cameraTopMax: number = -Infinity;

  /** Camera max bottom value for specific channel */
  private cameraBottomMax: number = Infinity;

  /** The amplitude adjustment that has been applied to the channel */
  private cameraAmplitudeAdjustment: number = 0;

  /** Map from waveform filter id to processed data segments */
  private readonly processedSegmentCache: Map<string, Float32ArrayWithStartTime[]> = new Map();

  /** Map from channel segment id to pre-calculated boundries */
  private readonly channelSegmentBoundries: Map<string, ChannelSegmentBoundries> = new Map();

  /**
   * Constructor
   * 
   * @param props Waveform props as WaveformRenderProps
   */
  public constructor(props: WaveformRendererProps) {
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
  public componentDidMount() {
    this.scene = new THREE.Scene();
    const cameraZDepth = 5;
    this.camera = new THREE.OrthographicCamera(this.glMin, this.glMax, 1, -1, cameraZDepth, -cameraZDepth);
    this.camera.position.z = 0;
    this.prepareWaveformData(true);
    if (this.props.masks) {
      this.renderChannelMasks(this.props.masks);
    }
  }

  /**
   * Called immediately after updating occurs. Not called for the initial render.
   *
   * @param prevProps the previous props
   * @param prevState the previous state
   */
  public componentDidUpdate(prevProps: WaveformRendererProps, prevState: WaveformRendererState) {
    // Recieved data for the first time
    if (prevProps.channelSegmentId !== this.props.channelSegmentId) {
      this.prepareWaveformData(false);
    } else if (
      !lodash.isEqual(prevProps.channelSegments, this.props.channelSegments)
      || prevProps.displayStartTimeSecs !== this.props.displayStartTimeSecs
      || prevProps.displayEndTimeSecs !== this.props.displayEndTimeSecs) {
      this.updateCameraBounds(prevProps);
      this.prepareWaveformData(true);
    }

    if (this.props.masks) {
      this.renderChannelMasks(this.props.masks);
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
   * Scales the amplitude of the single waveform.
   * 
   * @param e The mouse event
   */
  public readonly beginScaleAmplitudeDrag = (e: React.MouseEvent<HTMLDivElement>): void => {
    // prevent propagation of these events so that the underlying channel click doesn't register
    let previousPos = e.clientY;
    let currentPos = e.clientY;
    let diff = 0;

    const onMouseMove = (e2: MouseEvent) => {
      currentPos = e2.clientY;
      diff = previousPos - currentPos;
      previousPos = currentPos;

      const currentCameraRange = Math.abs(this.camera.top - this.camera.bottom);

      // calculate the amplitude adjustment
      const percentDiff = 0.05;
      const amplitudeAdjustment: number = (currentCameraRange * percentDiff);
      // apply the any ampliitude adjustment to the camera
      if (diff > 0) {
        this.camera.top -= amplitudeAdjustment;
        this.camera.bottom += amplitudeAdjustment;
        this.cameraAmplitudeAdjustment += amplitudeAdjustment;
      } else if (diff < 0) {
        this.camera.top += amplitudeAdjustment;
        this.camera.bottom -= amplitudeAdjustment;
        this.cameraAmplitudeAdjustment -= amplitudeAdjustment;
      }

      this.setYAxisBounds(this.camera.bottom, this.camera.top);
      this.camera.updateProjectionMatrix();
      this.props.renderWaveforms();
    };

    const onMouseUp = (e2: MouseEvent) => {
      document.body.removeEventListener('mousemove', onMouseMove);
      document.body.removeEventListener('mouseup', onMouseUp);
    };

    document.body.addEventListener('mousemove', onMouseMove);
    document.body.addEventListener('mouseup', onMouseUp);
  }

  /**
   * Reset the amplitude to the default.
   */
  public resetAmplitude = () => {
    // Check that the amplitude needs resetting
    if (this.camera.top !== this.cameraTopMax ||
      this.camera.bottom !== this.cameraBottomMax) {
      if (this.processedSegmentCache.size !== 0) {
        // reset the amplitude to the window default for this channel
        this.camera.top = this.cameraTopMax;
        this.camera.bottom = this.cameraBottomMax;
        this.cameraAmplitudeAdjustment = 0;

        this.setYAxisBounds(this.camera.bottom, this.camera.top);
        this.camera.updateProjectionMatrix();
        this.props.renderWaveforms();
      }
    }
  }

  /**
   * Update the min,max in gl units where we draw waveforms, if the view bounds have changed.
   * 
   * @param prevProps The previous waveform props
   */
  private readonly updateCameraBounds = (prevProps: WaveformRendererProps) => {
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
   * Prepares the waveform display for rendering.
   * 
   * @param refreshVerticeCache True if the cache should be refereshed, false otherwise
   */
  private readonly prepareWaveformData = (refreshVerticeCache: boolean) => {
    // Converts from array of floats to an array of vertices
    if (refreshVerticeCache) {
      this.convertDataToVerticeArray();
    }

    // Create ThreeJS scene from vertice data
    this.setupThreeJSFromVertices();
  }

  /**
   * Iterates through cached vertice data in the float32 array format
   * and creates ThreeJS objects and adds them to the
   * ThreeJS scene
   */
  // tslint:disable-next-line: cyclomatic-complexity
  private readonly setupThreeJSFromVertices = () => {

    while (this.scene.children.length > 0) {
      this.scene.remove(this.scene.children[0]);
    }

    const channelSegment = this.props.channelSegments &&
      this.props.channelSegments.has(this.props.channelSegmentId) ?
      this.props.channelSegments.get(this.props.channelSegmentId) : undefined;

    if (this.props.channelSegmentId) {
      const processedData = this.processedSegmentCache.get(this.props.channelSegmentId);
      if (processedData && channelSegment) {
        processedData.forEach(float32ArrayWithStartTime => {
          // removed old three js objects from scene
          const float32Array = float32ArrayWithStartTime.float32Array;
          const geometry = new THREE.BufferGeometry();
          geometry.addAttribute('position', new THREE.BufferAttribute(float32Array, 3));
          (float32ArrayWithStartTime.displayType || WaveformRenderer.defaultProps.displayType).forEach(displayType => {
            const color: string = float32ArrayWithStartTime.color || WaveformRenderer.defaultProps.color;
            if (displayType === Entities.DisplayType.LINE) {
              const lineMaterial = new THREE.LineBasicMaterial({ color, linewidth: 1 });
              const line = new THREE.Line(geometry, lineMaterial);
              this.scene.add(line);
            } else if (displayType === Entities.DisplayType.SCATTER) {
              const pointsMaterial = new THREE.PointsMaterial({
                color,
                size: float32ArrayWithStartTime.pointSize || WaveformRenderer.defaultProps.pointSize,
                sizeAttenuation: false
              });
              const points = new THREE.Points(geometry, pointsMaterial);
              this.scene.add(points);
            }
          });
        });
        if (this.props.channelSegments) {
          if (this.props.channelSegmentId) {
            const channelSegmentPrime =
              this.props.channelSegments.get(this.props.channelSegmentId);
            if (channelSegmentPrime && channelSegment.dataSegments) {
              const boundries = this.channelSegmentBoundries.get(this.props.channelSegmentId);
              if (boundries) {
                const amplitudeMin = Math.min(boundries.bottomMax, boundries.topMax);
                const amplitudeMax = Math.max(boundries.bottomMax, boundries.topMax);
                // Set channel average and set default camera top/bottom based on average
                // calculate the average using the unloaded data segments
                // and the previous loaded segments
                const yAvg = boundries.channelAvg / channelSegment.dataSegments.length;
                // Set axis offset and default view but account for the zero (empty channel)
                const axisOffset = boundries.offset !== 0 ? boundries.offset : 1;

                // account for the amplitude if it is all positive or all negative
                if (amplitudeMin < 0 && amplitudeMax > 0) {
                  this.cameraTopMax = yAvg + axisOffset;
                  this.cameraBottomMax = yAvg - axisOffset;
                } else {
                  this.cameraTopMax = amplitudeMax;
                  this.cameraBottomMax = amplitudeMin;
                }

                if (this.cameraTopMax !== -Infinity && this.cameraBottomMax !== Infinity) {
                  // update the camera and apply the any ampliitude adjustment to the camera
                  this.camera.top = this.cameraTopMax - this.cameraAmplitudeAdjustment;
                  this.camera.bottom = this.cameraBottomMax + this.cameraAmplitudeAdjustment;

                  // set amplitude for label
                  this.setYAxisBounds(this.cameraBottomMax, this.cameraTopMax);
                  this.camera.updateProjectionMatrix();
                }
              }
            }
          }
        }
      }
    }
  }

  /**
   * Converts waveform data into useable vertices
   */
  private readonly convertDataToVerticeArray = () => {
    // determine the new data segments that need to be added to the scene
    if (this.props.channelSegments && this.props.channelSegments.size > 0) {
      this.props.channelSegments.forEach((channelSegment: Entities.ChannelSegment, key: string) => {
        const processedSegments: Float32ArrayWithStartTime[] = [];
        // processed segment cache is internal to channel
        if (channelSegment.dataSegments) {
          channelSegment.dataSegments.forEach(dataSegment => {
            const dataToProcess = {
              data: dataSegment.data,
              displayStartTimeSecs: this.props.displayStartTimeSecs,
              displayEndTimeSecs: this.props.displayEndTimeSecs,
              glMax: this.glMax,
              glMin: this.glMin,
              sampleRate: dataSegment.sampleRate,
              startTimeSecs: dataSegment.startTimeSecs,
            };
            const float32Array = createPositionBuffer(dataToProcess);
            const arrayWithStartTime: Float32ArrayWithStartTime = {
              startTimeSecs: dataSegment.startTimeSecs,
              sampleRate: dataSegment.sampleRate,
              color: dataSegment.color,
              displayType: dataSegment.displayType,
              pointSize: dataSegment.pointSize,
              float32Array,
            };
            processedSegments.push(arrayWithStartTime);
          });
        }
        if (processedSegments.length > 0) {
          // if all processed segments are empty don't set cache
          for (const float32ArrayWithStartTimeIt of processedSegments) {
            if (float32ArrayWithStartTimeIt.float32Array.length > 0) {
              this.processedSegmentCache.set(key, processedSegments);
              break;
            }
          }
        }
        if (channelSegment && channelSegment.dataSegments) {
          let topMax = -Infinity;
          let bottomMax = Infinity;
          let channelAvg = 0;
          let offset = 0;
          channelSegment.dataSegments.forEach(dataSegment => {
            if (dataSegment.data.length > 0) {
              if (dataSegment.data.length === 0) {
                // When there is no data in the channel set offset to 1 (to avoid infinity)
                this.cameraTopMax = 1;
                this.cameraBottomMax = -1;
                return;
              }
              let segmentTopMax = -Infinity;
              let segmentBottomMax = Infinity;
              let segmentAvg = 0;
              // tslint:disable-next-line:prefer-for-of
              for (let i = 0; i < dataSegment.data.length; i++) {
                const sample = dataSegment.data[i];
                segmentAvg += sample;
                if (sample > segmentTopMax) segmentTopMax = sample;
                if (sample < segmentBottomMax) segmentBottomMax = sample;
              }
              segmentAvg = segmentAvg / dataSegment.data.length;
              topMax = Math.max(topMax, segmentTopMax);
              bottomMax = Math.min(bottomMax, segmentBottomMax);
              channelAvg += segmentAvg;
              offset = Math.max(Math.abs(topMax), Math.abs(bottomMax));
            }
          });
          const boundries: ChannelSegmentBoundries = {
            topMax,
            bottomMax,
            channelAvg,
            offset,
            channelSegmentId: key
          };
          this.channelSegmentBoundries.set(key, boundries);
          }
        });
    }
  }

  /**
   * Render the Masks to the display.
   * 
   * @param masks The masks (as Mask[]) to render
   */
  private readonly renderChannelMasks = (masks: Entities.Mask[]) => {
    // clear out any existing masks
    this.renderedMaskRefs.forEach(m => this.scene.remove(m));
    this.renderedMaskRefs.length = 0; // delete all references

    // if we're being passed empty data, don't try to add masks
    if (this.props.channelSegments &&
      this.props.channelSegments.size === 0) return;

    const timeToGlScale = d3.scaleLinear()
      .domain([this.props.displayStartTimeSecs, this.props.displayEndTimeSecs])
      .range([this.glMin, this.glMax]);

    // TODO move sorting to happen elsewhere and support re-sorting when new masks are added
    // TODO consider passing comparator for mask sorting as an argument to weavess
    lodash.sortBy(masks, (mask: Entities.Mask) => mask.endTimeSecs - mask.startTimeSecs)
      .forEach((mask, i, arr) => {
        const halfSecond = 0.5;
        let maskStartTime = mask.startTimeSecs;
        let maskEndTime = mask.endTimeSecs;
        if (mask.endTimeSecs - mask.startTimeSecs < 1) {
          maskStartTime -= halfSecond;
          maskEndTime += halfSecond;
        }
        const width = timeToGlScale(maskEndTime) - timeToGlScale(maskStartTime);
        const midoint = timeToGlScale(maskStartTime + (maskEndTime - maskStartTime) / 2);
        const planeGeometry = new THREE.PlaneBufferGeometry(width, this.cameraTopMax * 2);
        const planeMaterial = new THREE.MeshBasicMaterial({
          color: new THREE.Color(mask.color),
          side: THREE.DoubleSide,
          transparent: true
        });
        planeMaterial.blending = THREE.CustomBlending;
        planeMaterial.blendEquation = THREE.AddEquation;
        planeMaterial.blendSrc = THREE.DstAlphaFactor;
        planeMaterial.blendDst = THREE.SrcColorFactor;
        planeMaterial.depthFunc = THREE.NotEqualDepth;

        const plane: THREE.Mesh = new THREE.Mesh(planeGeometry, planeMaterial);
        const depth = -2;
        plane.position.x = midoint;
        plane.position.z = depth;
        plane.renderOrder = i / arr.length;

        this.renderedMaskRefs.push(plane);
      });

    if (this.renderedMaskRefs.length > 0) {
      this.scene.add(...this.renderedMaskRefs);
    }
  }

  /**
   * set the y-axis bounds for a particular channel
   * 
   * @param min The y minimum axis value
   * @param max The y maximum axis value
   */
  private readonly setYAxisBounds = (min: number, max: number) => {
    this.props.setYAxisBounds(min, max);
  }

// tslint:disable-next-line:max-file-line-count
}
