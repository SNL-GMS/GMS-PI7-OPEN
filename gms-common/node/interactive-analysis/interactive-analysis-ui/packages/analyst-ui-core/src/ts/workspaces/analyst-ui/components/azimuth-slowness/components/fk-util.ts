import { Colors } from '@blueprintjs/core';
import * as d3 from 'd3';
import { orderBy, sortBy } from 'lodash';
import { getFkData } from '~analyst-ui/common/utils/fk-utils';
import { systemConfig } from '~analyst-ui/config';
import { FkTypes, SignalDetectionTypes } from '~graphql/';
import { FeaturePrediction } from '~graphql/event/types';
import { FkAttributes, FkPowerSpectra } from '~graphql/fk/types';
import { FeatureMeasurementTypeName, NumericMeasurementValue, SignalDetection } from '~graphql/signal-detection/types';
import { findPhaseFeatureMeasurementValue,
         } from '~graphql/signal-detection/utils';
import { DistanceToSource } from '~graphql/station/types';
import { WaveformSortType } from '~state/analyst-workspace/types';
import { FkUnits } from '../types';
import { AnalystCurrentFk } from './fk-rendering/fk-rendering';

export const markerRadiusSize = 5;
export const digitPrecision = 3;
const CONSTANT_360 = 360;
const CONSTANT_180 = CONSTANT_360 / 2;
const CONSTANT_90 = CONSTANT_180 / 2;

/**
 * 
 * Values and labels for FK Velocity Rings
 * The smaller value is a Pn to P transition at 18degrees, the
 * large is a Pn to Pg transition at 2degrees
 * reverting back to previous values
 * Use 6, 8, 10 km/s for the slowness rings.
 * Show the labels on the plot as km/s, but plot the circles based on the slowness values.
 * Use the following slowness values: 18.5 s/deg, 13.875 s/deg, and 11.1 s/deg.
 */
const RING1 = 18.5;
const RING2 = 13.875;
const RING3 = 11.1;
const FK_VELOCITY_RADII = [RING1, RING2, RING3];
const FK_VELOCITY_RADII_LABELS = ['6 km/s', '8 km/s', '10 km/s'];

/**
 * Miscellaneous functions for rendering and processing fk data
 */

/**
 * Gets the predicted point values from the incoming signal detection
 * @param sd to get point from
 */
export const getPredictedPoint = (featurePredictions: FeaturePrediction[]): FkAttributes => {
  const predictedAzimuth = featurePredictions.find(fp =>
    fp.predictionType === FeatureMeasurementTypeName.RECEIVER_TO_SOURCE_AZIMUTH ||
    fp.predictionType === FeatureMeasurementTypeName.SOURCE_TO_RECEIVER_AZIMUTH);

  const azValue = predictedAzimuth ? predictedAzimuth.predictedValue as NumericMeasurementValue
                  : undefined;

  const predictedSlowness =
    featurePredictions.find(fp =>
      fp.predictionType === FeatureMeasurementTypeName.SLOWNESS);

  const slowValue = predictedSlowness ? predictedSlowness.predictedValue as NumericMeasurementValue
                    : undefined;

  if (azValue && slowValue) {
    return {
      slowness: slowValue.measurementValue.value,
      slownessUncertainty: slowValue.measurementValue.standardDeviation,
      azimuth: azValue.measurementValue.value,
      azimuthUncertainty: azValue.measurementValue.standardDeviation,
      peakFStat: undefined
    };
  } else {
    return undefined;
  }

};

/**
 * Returns an array of [min, max] values for a d3 scale
 * @param fkPowerSpectra the spectra to get scales from
 */
export const getScaleForFkSpectra = (fkPowerSpectra: FkPowerSpectra): number[] =>
  [fkPowerSpectra.ySlowStart, -fkPowerSpectra.ySlowStart];

/**
 * Returns an array of [min, max] values for the y axis of a spectra
 * @param fkPowerSpectra the spectra to get scales from
 */
export const getYAxisForFkSpectra = (fkPowerSpectra: FkPowerSpectra): number[] =>
  [fkPowerSpectra.ySlowStart, -fkPowerSpectra.ySlowStart];

/**
 * Returns an array of [min, max] values for the x axis of a spectra
 * @param fkPowerSpectra the spectra to get scales from
 */
export const getXAxisForFkSpectra = (fkPowerSpectra: FkPowerSpectra): number[] =>
  [fkPowerSpectra.xSlowStart, -fkPowerSpectra.xSlowStart];

/**
 * Main draw method for creating fk and dots
 * 
 * @param canvasRef The canvas element to draw on 
 * @param containerRef The canvas' containing HTML Element
 * @param imageBitmap the bitmap to draw on the canvas
 * @param fkData the fk data to be rendered
 * @param arrivalTime arrival time of the parent signal detection for the fk
 * @param predictedPoint predictedPoint as FkAttributes
 * @param paddingInput optional, how much to pad the rendering 
 * @param hideRingLabels  optional, if true, hides the labels for the rings
 * @param reduceOpacity optional, if true the drawing is set to 40% opacity
 * @param dotLocation optional, where to draw a black dot
 */
export function draw(canvasRef: HTMLCanvasElement, containerRef: HTMLElement,
  imageBitmap: ImageBitmap, fkData: FkTypes.FkPowerSpectra, predictedPoint: FkAttributes,
  arrivalTime: number, paddingInput?: number, hideRingLabels?: boolean, reduceOpacity?: boolean,
  dotLocation?: AnalystCurrentFk): CanvasRenderingContext2D {

  const ctx = canvasRef.getContext('2d');
  ctx.clearRect(0, 0, canvasRef.width, canvasRef.height);
  const transparencyPower = 0.4;
  if (reduceOpacity) {
    ctx.globalAlpha = transparencyPower;
  }
  ctx.drawImage(imageBitmap, 0, 0, canvasRef.width, canvasRef.height);
  ctx.imageSmoothingEnabled = true;
  drawFkCrossHairs(canvasRef, ctx);
  drawVelocityRings(canvasRef, ctx, fkData, hideRingLabels);
  drawMaxFk(canvasRef, ctx, fkData);
  drawPredictedFk(canvasRef, ctx, fkData, predictedPoint);
  if (dotLocation) {
    drawDot(canvasRef, ctx, dotLocation);
  }
  if (reduceOpacity) {
    ctx.globalAlpha = 1;
  }
  return ctx;
}

/**
 * Draws a circle
 * 
 * @param ctx The canvas context to draw in
 * @param x The x coordinate to start drawing
 * @param y The y coordinate to start drawing at
 * @param strokeColor The circle's color, defaults to RED
 * @param isFilled If true, fills the circle
 */
export const drawCircle = (
  ctx: CanvasRenderingContext2D,
  x: number,
  y: number,
  radii: number[],
  strokeColor: string = Colors.RED3,
  isFilled: boolean = false) => {
  ctx.strokeStyle = strokeColor;
  radii.forEach(radius => {
    ctx.beginPath();
    ctx.arc(x, y, radius, 0, Math.PI * 2);
    if (isFilled) {
      ctx.fillStyle = strokeColor;
      ctx.fill();
    } else {
      ctx.stroke();
    }
  });
};

/**
 * Draw the crosshairs.
 * 
 * @param canvasRef the canvas to draw on
 * @param ctx the canvas' drawing context
 */
export function drawFkCrossHairs(canvasRef: HTMLCanvasElement, ctx: CanvasRenderingContext2D) {

  ctx.strokeStyle = Colors.WHITE;
  ctx.beginPath();

  ctx.moveTo(canvasRef.width / 2, 0);
  ctx.lineTo(canvasRef.width / 2, canvasRef.height);
  ctx.stroke();

  ctx.moveTo(0, canvasRef.height / 2);
  ctx.lineTo(canvasRef.width, canvasRef.height / 2);
  ctx.stroke();
}

/**
 * Draw velocity radius indicators
 * 
 * @param canvasRef The canvas to draw on
 * @param ctx The canvas' context
 * @param fkData the data to render
 * @param hideRingLabels if true, hides the ring's labels
 */
export function drawVelocityRings(canvasRef: HTMLCanvasElement, ctx: CanvasRenderingContext2D,
  fkData: FkTypes.FkPowerSpectra, hideRingLabels: boolean = false) {
  const scale = d3.scaleLinear()
    .domain([0, -fkData.ySlowStart])
    .range([0, canvasRef.height / 2]);
  FK_VELOCITY_RADII.sort((a, b) => b - a);
  const radii = FK_VELOCITY_RADII;
  const scaledRadii = radii.map(scale);

  const center: any = {
    x: (canvasRef.width / 2),
    y: (canvasRef.height / 2)
  };

  // add labels for each ring
  if (!hideRingLabels) {
    scaledRadii.forEach((value: number, index) => {
      ctx.fillStyle = Colors.WHITE;
      const label = `${FK_VELOCITY_RADII_LABELS[index]}`;
      ctx.fillText(label, Number(center.x) + 3, Number(center.y) - (value + 3));
    });
  }
  drawCircle(ctx, center.x, center.y, scaledRadii, Colors.WHITE);
}

/**
 * Converts polar point to X,Y point
 * @param slowness Radius in polar coordinates
 * @param azimuth Theta in polar coordinates
 */
export const convertPolarToXY = (slowness: number, azimuth: number) => {
  const radians = (azimuth - CONSTANT_90) * (Math.PI / CONSTANT_180);
  const x = slowness * Math.cos(radians);
  const y = slowness * Math.sin(radians);

  return { x, y };
};

/**
 * Converts the incoming X,Y point to polar coordinates represented by 
 * Azimuth Degrees and Radial Slowness
 * @param x x coordinate
 * @param y y coordinate
 */
export const convertXYtoPolar = (x: number, y: number) => {
  if (x && y) {
    // converts xy to theta in degree - adjusted to have 0 degrees be North
    const adjustmentDegrees = 270;
    const theta = CONSTANT_360 -
      ((Math.atan2(y, x) * (CONSTANT_180 / Math.PI) + adjustmentDegrees) % CONSTANT_360);

    // Calculate radius from center
    const radius = Math.sqrt(x ** 2 + y ** 2);

    return {
      azimuthDeg: theta,
      radialSlowness: radius
    };
  } else {
    return {
      azimuthDeg: undefined,
      radialSlowness: undefined
    };
  }
};

/**
 * Converts a clicked x y coordinate and converts to coordinate space
 * and scales based on xy axis
 * @param x x value in graphics space
 * @param y y value in graphics space
 * @param fkData fkdata used to retrieve the slowness scale
 */
export const convertGraphicsXYtoCoordinate =
  (x: number, y: number, fkData: FkPowerSpectra, width: number, height: number) => {

  if (!fkData) {
    return undefined;
  }
  const xscale = d3.scaleLinear()
    .domain([0, width])
    .range(getXAxisForFkSpectra(fkData));
  const yscale = d3.scaleLinear()
    .domain([0, height])
    // Reversing to properly scale from graphics space to xy space
    .range(getYAxisForFkSpectra(fkData)
    .reverse());

  const scaledX = xscale(x);
  const scaledY = yscale(y);

  if (isNaN(scaledX) || isNaN(scaledY)) {
    return undefined;
  } else {
    return {
      x: scaledX,
      y: scaledY
    };
  }
};

/**
 * Creates the location needed to draw the analyst selected dot
 * @param x x coordinate
 * @param y y coordinate
 * @returns FkAttributes object with converted XYPolar
 */
export const getAnalystSelectedPoint = (x: number, y: number): FkAttributes => {
  const polar = convertXYtoPolar(x, y);
  return {
    azimuth: polar.azimuthDeg,
    azimuthUncertainty: 0,
    slowness: polar.radialSlowness,
    slownessUncertainty: 0,
    peakFStat: undefined
  };
};

/**
 * Draw the Max FK marker.
 * @param canvasRef The canvas to draw on
 * @param ctx The canvas' context
 * @param fkData the data to render
 * @param arrivalTime the arrival time
 */
export function drawMaxFk(canvasRef: HTMLCanvasElement, ctx: CanvasRenderingContext2D,
    fkData: FkTypes.FkPowerSpectra) {
  const scale = d3.scaleLinear()
    .domain(getScaleForFkSpectra(fkData))
    .range([0, canvasRef.height]);
  const point = convertPolarToXY(fkData.leadSpectrum.attributes.slowness, fkData.leadSpectrum.attributes.azimuth);

  drawCircle(
    ctx,
    scale(point.x),
    scale(point.y), [markerRadiusSize - 1], Colors.WHITE, true);
}

/**
 * Draw the predicted FK marker.
 * @param canvasRef The canvas to draw on
 * @param ctx The canvas' context
 * @param fkData the data to render
 */
export function drawPredictedFk(canvasRef: HTMLCanvasElement, ctx: CanvasRenderingContext2D,
    fkData: FkTypes.FkPowerSpectra, predictedPoint: FkAttributes, strokeColor: string = Colors.BLACK) {
  if (!fkData || !predictedPoint || !canvasRef) {
    return;
  }
  const scale = d3.scaleLinear()
    .domain(getScaleForFkSpectra(fkData))
    .range([0, canvasRef.height]);
  const point = convertPolarToXY(predictedPoint.slowness, predictedPoint.azimuth);
  const x = scale(point.x);
  const y = scale(point.y);
  drawCrosshairDot(ctx, x, y, strokeColor);
}

/**
 * Draw the predicted FK marker crosshair dot.
 * @param ctx The canvas' context
 * @param x x coordinate
 * @param y y coordinate
 * @param strokeColor the color of the cross hair dot
 * @param size (OPTIONAL) radius size uses class defined radius by default
 */
export function drawCrosshairDot(ctx: CanvasRenderingContext2D,
  x: number, y: number, strokeColor: string, size = markerRadiusSize) {

  const length = markerRadiusSize - 1;

  ctx.strokeStyle = Colors.BLACK;

  ctx.beginPath();
  ctx.moveTo(x - length, y - length);
  ctx.lineTo(x + length, y + length);

  ctx.moveTo(x + length, y - length);
  ctx.lineTo(x - length, y + length);
  ctx.stroke();

  drawCircle(ctx, x, y, [size], strokeColor, false);
}

/**
 * Draws the color scale
 * @param min The minumum frequency value
 * @param max THe mamximum frequency value
 * @returns D3 object that turns values into colors d3.ScaleSequential<d3.HSLColor>
 */
export const createColorScale: any = (min: number, max: number) => d3
  .scaleSequential(t => {
    if (t < 0 || t > 1) {
      // tslint:disable-next-line:no-parameter-reassignment
      t -= Math.floor(t);
    }
    // tslint:disable-next-line:no-magic-numbers
    const ts = Math.abs(t - 0.5);
    // map to range [240, 0] hue
    // tslint:disable-next-line:no-magic-numbers binary-expression-operand-order
    return d3.hsl(240 - (240 * t), 1.5 - 1.5 * ts, 0.8 - 0.9 * ts);
  })
  .domain([min, max]);

/**
 * Convert fk data to an ImageBitmap
 * @param fkData The data to render
 * @param min The minimum frequency value
 * @param max The maximum frequency value
 * @returns JS Promise that resovles to the FK ImageBitmap
 */
export const createFkImageBitmap = async (fkGrid: number[][], min: number, max: number): Promise<ImageBitmap> => {
  const dim = fkGrid.length;
  const size = dim * dim;
  const buffer = new Uint8ClampedArray(size * 4); // r, g, b, a for each point
  const uInt8Max = 255;

  const colorScale = createColorScale(min, max);
  for (let row = fkGrid.length - 1; row >= 0; row--) {
    for (let col = 0; col < fkGrid[0].length; col++) {
      const value = fkGrid[row][col];
      const rowPos = fkGrid.length - row;
      const pos = (rowPos * fkGrid.length + col) * 4;
      const color = d3.rgb(colorScale(value));
      buffer[pos] = color.r;
      buffer[pos + 1] = color.g;
      buffer[pos + 2] = color.b;
      buffer[pos + 3] = uInt8Max;
    }
  }

  const imgData = new ImageData(buffer, fkGrid.length, fkGrid.length);
  const imgBitmap = window.createImageBitmap(imgData);

  return imgBitmap;
};

/**
 * Create heat map color scale.
 * @param heightPx The height in px of the bitmap
 * @param widthPx The width in px of the bitmap
 * @returns JS Promise that resovles to a ColorScale ImageBitmap
 */
export const createColorScaleImageBitmap = async (heightPx: number, widthPx: number): Promise<ImageBitmap> => {
  const size = heightPx * widthPx;
  const buffer = new Uint8ClampedArray(size * 4); // r, g, b, a for each point
  const uInt8Max = 255;

  const colorScale = createColorScale(0, heightPx + 1);
  for (let row = 0; row < heightPx; row++) {
    for (let col = 0; col < widthPx; col++) {
      const pos = (row * heightPx + col) * 4;

      const color = d3.rgb(colorScale(col));
      buffer[pos] = color.r;
      buffer[pos + 1] = color.g;
      buffer[pos + 2] = color.b;
      buffer[pos + 3] = uInt8Max;
    }
  }

  const imgData = new ImageData(buffer, heightPx, widthPx);
  const imgBitmap = window.createImageBitmap(imgData);

  return imgBitmap;
};

/**
 * Finds the min/max frequency of an FK so the heatmap can be drawn.
 * @param fkData the raw fk data to find a min/max for
 * @returns An array where index 0 is a minimum fk freq and an index 1 is a maximum fk freq
 */
export const computeMinMaxFkValues = (fkData: number[][]): [number, number] => {
  let max = -Infinity;
  let min = Infinity;

  for (const row of fkData) {
    for (const val of row) {
      if (val > max) max = val;
      if (val < min) min = val;
    }
  }

  return [min, max];
};

/**
 * Does the fk need review
 * @param sd: the signal detection to check
 * @returns boolean
 */
export const fkNeedsReview = (sd: SignalDetectionTypes.SignalDetection): boolean => {

  const fkData = getFkData(sd.currentHypothesis.featureMeasurements);
  const phase = findPhaseFeatureMeasurementValue(sd.currentHypothesis.featureMeasurements).phase;
  if (!fkData) {
    return false;
  }
  if (fkData.reviewed) {
    return false;
  }
  if (systemConfig.fkNeedsReviewRuleSet.phasesNeedingReview.indexOf(phase) > -1) {
    return true;
  }
  return false;
};

/**
 * Draws a single dot on the canvas.
 * @param canvasRef the canvas reference
 * @param ctx the 2d rendering context 
 * @param coordinates the coordinates
 */
function drawDot(canvasRef: HTMLCanvasElement, ctx: CanvasRenderingContext2D, coordinates: AnalystCurrentFk) {
  drawCircle(ctx,
             coordinates.x,
             coordinates.y,
             [markerRadiusSize - 1],
             Colors.BLACK,
             true);
}

/**
 * Return the heatmap array from the FK Spectra
 * @param fkPowerSpectra: an fk power spectra
 * @returns number[][]
 */
export const getFkHeatmapArrayFromFkSpectra =
(fkPowerSpectra: FkPowerSpectra, unit: FkUnits): number[][] => {
  const leadSpectrum = fkPowerSpectra.leadSpectrum;
  return unit === FkUnits.FSTAT ? leadSpectrum.fstat : leadSpectrum.power;
};

/**
 * Calculates the fstat point based on input heatmap and az slow values
 * @param fkData as FkPowerSpectra
 * @param azimuth azimuth
 * @param slowness slowness
 * @param units units as FkUnits
 */
export const getPeakValueFromAzSlow = (
  fkData: FkPowerSpectra, azimuth: number, slowness: number, units: FkUnits) => {
  const heatMap = getFkHeatmapArrayFromFkSpectra(fkData, units);
  const xaxis = getXAxisForFkSpectra(fkData);
  const yaxis = getYAxisForFkSpectra(fkData);
  const xscale = d3.scaleLinear()
    .domain(xaxis)
    .range([0, xaxis[1] * 2]);
  const yscale = d3.scaleLinear()
    .domain(yaxis)
    .range([0, yaxis[1] * 2]);
  const xyPoint = convertPolarToXY(slowness, azimuth);
  const x = Math.floor(xscale(xyPoint.x));
  const y = Math.floor(yscale(xyPoint.y));
  // Index into heat map
  const rowLength = heatMap ? heatMap.length - 1 : 0;
  const maybeRow = heatMap ? (heatMap[rowLength - y]) : undefined;
  const maybeValue = maybeRow ? maybeRow[x] : undefined;
  return maybeValue ? maybeValue.toFixed(digitPrecision) : undefined;
};

/**
 * Return an FKInput
 */
export function createFkInput(
    signalDetection: SignalDetectionTypes.SignalDetection,
    frequencyBand: FkTypes.FrequencyBand,
    windowParams: FkTypes.WindowParameters,
    fkConfiguration: FkTypes.FkConfiguration
    ): FkTypes.FkInput {

    const fmPhase = findPhaseFeatureMeasurementValue(signalDetection.currentHypothesis.featureMeasurements);
    return {
      stationId: signalDetection.station.id,
      signalDetectionId: signalDetection.id,
      signalDetectionHypothesisId: signalDetection.currentHypothesis.id,
      phase: fmPhase.phase.toString(),
      frequencyBand: {
        maxFrequencyHz: frequencyBand.maxFrequencyHz,
        minFrequencyHz: frequencyBand.minFrequencyHz
      },
      windowParams: {
        leadSeconds: windowParams.leadSeconds,
        lengthSeconds: windowParams.lengthSeconds,
        stepSize: windowParams.stepSize,
        windowType: 'hanning',
      },
      configuration: {
        contributingChannelsConfiguration: fkConfiguration.contributingChannelsConfiguration.map(ccc => ({
          name: ccc.name,
          id: ccc.id,
          enabled: ccc.enabled
        })),
        maximumSlowness: fkConfiguration.maximumSlowness,
        mediumVelocity: fkConfiguration.mediumVelocity,
        normalizeWaveforms: fkConfiguration.normalizeWaveforms,
        numberOfPoints: fkConfiguration.numberOfPoints,
        useChannelVerticalOffset: fkConfiguration.useChannelVerticalOffset,
        leadFkSpectrumSeconds: fkConfiguration.leadFkSpectrumSeconds
      }
    };
  }

  /**
   * Returns sorted signal detections based on sort type (Distance, Alphabetical)
   */
export function getSortedSignalDetections(signalDetections: SignalDetectionTypes.SignalDetection[],
    selectedSortType: WaveformSortType, distanceToSource: DistanceToSource[]): SignalDetectionTypes.SignalDetection[] {
  // apply sort based on sort type
  let data: SignalDetection[] = [];
  // Sort by distance
  if (selectedSortType === WaveformSortType.distance) {
    data = sortBy<SignalDetectionTypes.SignalDetection>(
      signalDetections, [(sd: SignalDetectionTypes.SignalDetection) =>
        distanceToSource.find(d => d.stationId === sd.station.id).distance]);
  } else {
    // apply sort if a sort comparator is passed in
    data = selectedSortType ?
      orderBy<SignalDetectionTypes.SignalDetection>(
        signalDetections, ['station.name'],
        ['asc']) : signalDetections;
  }

  return data;
}
