import * as React from 'react';
import * as THREE from 'three';
import { RpcProvider } from 'worker-rpc';

import { RecordSectionLabels } from './labels';
declare var require;
const myWorker = require('worker-loader?inline&fallback=false!../../workers'); // tslint:disable-line
import { WorkerOperations } from '../../workers/operations';

export interface RecordSectionState {
  /** Bottom Value as number */
  bottomVal: number;

  /** Options as any */
  options: any;

  /** Phases as any */
  phases: any;

  /** Top value as number */
  topVal: number;

  /** Rendering as boolean */
  rendering: boolean;

  /** Loaded as boolean */
  loaded: boolean;
}

const workerRpc = (() => {
  const worker = new myWorker();
  const rpc = new RpcProvider(
    (message, transfer) => worker.postMessage(message, transfer),
  );
  worker.onmessage = e => rpc.dispatch(e.data);

  return rpc;
})();

/**
 * RecordSection
 */
export class RecordSection extends React.Component<{}, RecordSectionState> {

  public displayName: string = 'RecordSection';

  /** Canvas reference */
  public canvasRef: HTMLCanvasElement | null;

  /** Three web gl render */
  public renderer: THREE.WebGLRenderer;

  /** Three Scene */
  public scene: THREE.Scene;

  /** Three.OrthographicCamera */
  public camera: THREE.OrthographicCamera;

  private readonly containerStyle: React.CSSProperties = {
    height: '100%',
    position: 'relative',
    width: '100%',
  };

  private readonly recordSectionStyle: React.CSSProperties = {
    alignItems: 'center',
    fontFamily: 'sans-serif',
    fontSize: 'large',
    height: '100%',
    justifyContent: 'center',
    width: '100%',
  };

  private readonly canvasStyle: React.CSSProperties = {
    height: '100%',
    position: 'absolute',
    width: '100%',
    zIndex: 0,
  };

  /** Constant for 180 degrees */
  private readonly ONE_HUNDRED_EIGHTY_DEGREES: number = 180;

  /** Magic 200 */
  private readonly MAGIC_TWO_HUNDRED: number = 200;

  /** The pixel height of the canvas known to the render/painting, not the height of the actual canvas div. */
  private readonly logicalCanvasHeight: number = this.ONE_HUNDRED_EIGHTY_DEGREES * this.MAGIC_TWO_HUNDRED;

  /** Each waveform has the Y axis quantized and scaled to fit within 800 logical pixels. */
  private readonly logicalWaveformHeight: number = 800;

  /** Default camera left */
  private readonly defaultCameraLeft: number = 0;

  /**  Defsult camera right */
  private readonly defaultCameraRight: number = 20;

  /** Web worker */
  private readonly workerRpc: RpcProvider;

  /**
   * Constructor
   * 
   * @param props RecordSection props
   */
  public constructor(props) {
    super(props);
    this.state = {
      bottomVal: 0,
      loaded: false,
      options: { data: [] },
      phases: [],
      rendering: true,
      topVal: 0,
    };
    this.workerRpc = workerRpc;
  }

  public render() {
    return (
      <div className="record-section-container" style={this.containerStyle}>
        <canvas
          ref={canvas => { this.canvasRef = canvas; }}
          style={this.canvasStyle}
        />
        <RecordSectionLabels
          key="key"
          topVal={this.state.topVal}
          bottomVal={this.state.bottomVal}
          phases={this.state.phases}
        />
        <div
          className="record-section"
          style={this.recordSectionStyle}
        />
      </div>
    );
  }

  public componentDidMount() {
    if (!this.canvasRef) return;

    // TODO remove this listener on unmount
    window.addEventListener('resize', this.animate.bind(this));
    this.renderer = new THREE.WebGLRenderer({ alpha: true, antialias: true, canvas: this.canvasRef });
    this.initialize();
    this.animate();
  }

  public initialize() {
    this.scene = new THREE.Scene();
    // tslint:disable-next-line:no-magic-numbers
    this.camera = new THREE.OrthographicCamera(this.defaultCameraLeft, this.defaultCameraRight, 1, -1, -5, 5);
    // tslint:disable-next-line:no-magic-numbers
    this.camera.position.z = -5;
  }

  /**
   * Update the display. If 'clear' is false, then the data in this.state.options.data is painted on the canvas.
   * If 'clear' is true, then the canvas is cleared.
   * 
   * @param clear clear flag
   */
  public update(clear: boolean) {
    this.scene = new THREE.Scene();
    if (!clear && this.state.options.data.length !== 0) {
      this.loadData(this.state.options.data)
        .catch();
    } else {
      // this.state.topVal = 0;
      // this.state.bottomVal = 0;
      // this.state.phases = [];
      // this.setState(this.state);
      this.setState({
        bottomVal: 0,
        phases: [],
        topVal: 0,
      });
      window.requestAnimationFrame(this.animate.bind(this));
    }
  }

  public numericSortAsc = (a: number, b: number): number => a - b;

  /**
   * Given an array of integers that correspond to the Y values of a waveform and the degree distance from the origin,
   * convert the original Y values into new record section Y coordinate.
   * 
   * @param waveformYArray waveform Y array
   * @param distanceDegrees distance degrees
   * 
   * @returns Object {yArr, yMax, yMedian, Ymin}
   */
  public convertWaveformYToCanvasY(waveformYArray: number[], distanceDegrees: number) {
    // Create a sorted version of the array to get the min, median, and max
    const sortedYArray = waveformYArray.slice(0, waveformYArray.length)
      .sort(this.numericSortAsc);
    const yMin = sortedYArray[0];
    const yMax = sortedYArray[sortedYArray.length - 1];
    const yMedian = sortedYArray[Math.floor(sortedYArray.length / 2)];

    const yRange = yMax - yMin;

    // Based on the distance from the origin, get the y offset for where the waveform should be placed. At 0 degrees
    // (ie. the station is right at the origin), the yOffset is the very top of the canvas. At 180 degrees (ie. the
    // station is on the opposite side of the world), the yOffset is the very bottom.
    // tslint:disable-next-line:no-magic-numbers
    const yOffset = this.logicalCanvasHeight * (1 - (distanceDegrees / 180));

    // Find the min, median, and max y values and convert them to their corresponding y coordinate on the canvas.
    // This median is used as the center of the signal detection line so that the line appears to be closely aligned
    // with the median of the waveform.
    const [correctedYMin, correctedYMedian, correctedYMax] = [yMin, yMedian, yMax]
      .map(val => yOffset + ((val - yMedian) / yRange) * this.logicalWaveformHeight);

    // For each value in the array, translate it so that the midpoint is at 0, scale it so that the values range
    // between -400 and 400, then translate it to its appropriate canvas y position.
    const correctedY = waveformYArray
      .map(y => (((y - yMedian) / yRange) * this.logicalWaveformHeight) + yOffset);

    return {
      yArr: correctedY,
      yMax: correctedYMax,
      yMedian: correctedYMedian,
      yMin: correctedYMin,
    };
  }

  /**
   * Kilometers to Degrees
   * 
   * @param km kilometer to convert to degrees
   * 
   * @returns result of kilometers to degrees
   */
  public kilometersToDegrees(km: number) {
    // tslint:disable-next-line:no-magic-numbers
    return (km / (Math.PI * 6371e3)) * 180;
  }

  /**
   * Min array returner
   * 
   * @param arr input array
   * 
   * @returns min value in array
   */
  public arrayMin(arr: number[]) {
    return arr.reduce((prev, curr) => curr < prev ? curr : prev, Infinity);
  }

  /**
   * Max array returner
   * 
   * @param arr input array
   * 
   * @returns max value of array
   */
  public arrayMax(arr: number[]) {
    return arr.reduce((prev, curr) => curr > prev ? curr : prev, -Infinity);
  }

  /**
   * Load data
   * 
   * @param waveformArray  data
   */
  public async loadData(waveformArray: any[]) {
    let maxY = -Infinity;
    let minY = Infinity;

    const phases: any = [];
    const cameraXRange = this.defaultCameraRight - this.defaultCameraLeft;

    for (const waveform of waveformArray) {
      if (!waveform.distance) {
        continue;
      }

      const waveformCanvasY = this.convertWaveformYToCanvasY(
        waveform.data, this.kilometersToDegrees(waveform.distance));

      // @ts-ignore
      const float32Array: Float32Array =
        await this.workerRpc.rpc(WorkerOperations.CREATE_RECORD_SECTION_POSITION_BUFFER, {
          cameraXRange,
          data: waveform.data,
          defaultCameraLeft: this.defaultCameraLeft,
          distance: waveform.distance,
        });

      const lineGeometry = new THREE.BufferGeometry();
      lineGeometry.addAttribute('position', new THREE.BufferAttribute(float32Array, 3));
      const lineMaterial = new THREE.LineBasicMaterial({ color: this.state.options.color, linewidth: 1 });
      const signalDetectGeometry = new THREE.Geometry();
      const signalDetectMaterial = new THREE.LineBasicMaterial({ color: '#ff0000', linewidth: 1 });

      this.scene.add(new THREE.Line(lineGeometry, lineMaterial));

      // Draw the signal detection.
      const duration = (waveform.data.length / waveform.sampleRate);
      const detectionTimeSecs = waveform.signalDetection[0].time.getTime() / 1000;
      const detectX = ((detectionTimeSecs - waveform.startTime) / duration) * cameraXRange;
      const detectY = waveformCanvasY.yMedian;
      // tslint:disable-next-line:no-magic-numbers
      signalDetectGeometry.vertices.push(new THREE.Vector3(detectX, detectY + 400, 0));
      // tslint:disable-next-line:no-magic-numbers
      signalDetectGeometry.vertices.push(new THREE.Vector3(detectX, detectY - 400, 0));
      this.scene.add(new THREE.Line(signalDetectGeometry, signalDetectMaterial));

      // Collect information about where the phase labels should be placed as divs.
      phases.push({
        detectY,
        // tslint:disable-next-line:no-magic-numbers
        percentX: (detectX / cameraXRange) * 100,
        percentY: 0,
        phase: waveform.phase,
      });
      maxY = Math.max(waveformCanvasY.yMax, maxY);
      minY = Math.min(waveformCanvasY.yMin, minY);
    }

    // tslint:disable-next-line:no-magic-numbers
    const margin = (maxY - minY) * 0.02;
    maxY += margin;
    minY -= margin;
    // tslint:disable-next-line:no-magic-numbers
    phases.map(val => { val.percentY = (1 - (val.detectY - minY) / (maxY - minY)) * 100; });

    // tslint:disable-next-line:no-magic-numbers
    const topVal = (1 - (maxY / this.logicalCanvasHeight)) * 180;
    // tslint:disable-next-line:no-magic-numbers
    const bottomVal = (1 - (minY / this.logicalCanvasHeight)) * 180;
    const rendering = true;
    this.setState(this.state);
    this.setState({
      bottomVal,
      topVal,
      rendering,
      phases,
    });

    this.camera.top = maxY;
    this.camera.bottom = minY;
    this.camera.updateProjectionMatrix();
    window.requestAnimationFrame(this.animate.bind(this));
  }

  public animate() {
    if (this.state.rendering) {
      this.updateSize();
      this.renderer.render(this.scene, this.camera);
    }
  }

  public updateSize() {
    if (!this.canvasRef) return;
    const width = this.canvasRef.offsetWidth;
    const height = this.canvasRef.offsetHeight;

    if (this.canvasRef.width !== width || this.canvasRef.height !== height) {
      this.renderer.setSize(width, height, false);
    }
  }

  /**
   * Adds waveform data, used for late arriving data
   * 
   * @param options configurations as any[]
   * @param delayed isDelayed as boolean
   */
  public addWaveformArray(options: any[], delayed: boolean) {
    const defaultWaveformOptions = {
      color: '#4580E6',
      data: options,
    };

    this.setState(
      {
        loaded: false,
        options: defaultWaveformOptions,
      },
      () => {
        if (!delayed && options && options.length !== 0) {
          this.update(false);
          // this.state.loaded = true;
          this.setState({
            loaded: true,
          });
        } else {
          this.update(true);
        }
      });
  }

  public stopRender() {
    // this.state.rendering = false;
    this.setState({
      rendering: false,
    });
  }

  public resumeRender() {
    // this.state.rendering = true;
    this.setState({
      rendering: true,
    });
    if (!this.state.loaded) {
      this.update(false);
      this.setState({
        loaded: true,
      });
      // this.state.loaded = true;
    } else {
      window.requestAnimationFrame(this.animate.bind(this));
    }
  }

}
