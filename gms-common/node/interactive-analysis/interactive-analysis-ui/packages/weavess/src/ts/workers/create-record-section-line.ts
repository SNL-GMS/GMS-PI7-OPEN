// other than getting it basically working

const logicalCanvasHeight = 36000;
const logicalWaveformHeight = 800;

/**
 * Input to create a line for record section display
 */
export interface CreateRecordSectionLineParams {
  /** Array representing data. */
  data: number[];
  /** Displacement of waveform */
  distance: number;
  /** Range of x values for camera */
  cameraXRange: number;
  /** Left value of camera */
  defaultCameraLeft: number;
}

/** Creates a record section position buffer */
export const createRecordSectionPositionBuffer = (params: CreateRecordSectionLineParams): Float32Array => {
  const vertices: number[] = [];

  // Convert the raw waveform coordinates to record section coordinates.
  const waveformCanvasY = convertWaveformYToCanvasY(params.data, kilometersToDegrees(params.distance));

  // Draw the waveform.
  for (let j = 0; j < waveformCanvasY.yArr.length; j++) {
    const t = params.defaultCameraLeft + j * params.cameraXRange / (waveformCanvasY.yArr.length - 1);
    vertices.push(t);
    vertices.push(waveformCanvasY.yArr[j]);
    vertices.push(0);
  }

  return new Float32Array(vertices);
};

/**
 * Convert waveform y to canvas y
 * 
 * @param waveformYArray Array of numbers representing waveform
 * @param distanceDegrees Distance in degrees of waveform
 * 
 * @returns an object containing the corrected y-values
 */
function convertWaveformYToCanvasY(waveformYArray: number[], distanceDegrees: number) {
  // Create a sorted version of the array to get the min, median, and max
  const sortedYArray = waveformYArray.slice(0, waveformYArray.length)
    .sort(numericSortAsc);
  const yMin = sortedYArray[0];
  const yMax = sortedYArray[sortedYArray.length - 1];
  const yMedian = sortedYArray[Math.floor(sortedYArray.length / 2)];

  const yRange = yMax - yMin;

  // Based on the distance from the origin, get the y offset for where the waveform should be placed. At 0 degrees
  // (ie. the station is right at the origin), the yOffset is the very top of the canvas. At 180 degrees (ie. the
  // station is on the opposite side of the world), the yOffset is the very bottom.
  // tslint:disable-next-line:no-magic-numbers
  const yOffset = logicalCanvasHeight * (1 - (distanceDegrees / 180));

  // Find the min, median, and max y values and convert them to their corresponding y coordinate on the canvas.
  // This median is used as the center of the signal detection line so that the line appears to be closely aligned
  // with the median of the waveform.
  const [correctedYMin, correctedYMedian, correctedYMax] = [yMin, yMedian, yMax]
    .map(val => yOffset + ((val - yMedian) / yRange) * logicalWaveformHeight);

  // For each value in the array, translate it so that the midpoint is at 0, scale it so that the values range
  // between -400 and 400, then translate it to its appropriate canvas y position.
  const correctedY = waveformYArray
    .map(y => (((y - yMedian) / yRange) * logicalWaveformHeight) + yOffset);

  return {
    yArr: correctedY,
    yMax: correctedYMax,
    yMedian: correctedYMedian,
    yMin: correctedYMin,
  };
}

/**
 * Sort ascending
 * 
 * @param a first number
 * @param b second number
 * 
 * @returns first - second
 */
function numericSortAsc(a: number, b: number) {
  return a - b;
}

/**
 * Rudimentarily convert kilometers to degrees
 * 
 * @param km kilometer as a number to convert to degrees
 * 
 * @returns kilometer to degrees result
 */
function kilometersToDegrees(km: number) {
  const converstionFactor = 6371e3;
  const halfCircleDegs = 180;
  return (km / (Math.PI * converstionFactor)) * halfCircleDegs;
}
