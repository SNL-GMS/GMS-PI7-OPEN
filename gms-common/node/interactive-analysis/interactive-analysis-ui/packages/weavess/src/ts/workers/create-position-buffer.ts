import * as d3 from 'd3';

/**
 * Input required to create the position buffer
 */
export interface CreatePositionBufferParams {
  /** Minimum GL value */
  glMin: number;
  /** Maximum GL value */
  glMax: number;
  /** Array containing the vertices */
  data: Float32Array | number[];
  /** Start Time Seconds formatted for display */
  displayStartTimeSecs: number;
  /** End Time Seconds formatted for display */
  displayEndTimeSecs: number;
  /** Start Time in seconds */
  startTimeSecs: number;
  /** End Time in seconds */
  sampleRate: number;
}

/**
 * Convert number[] + startTime + sample rate into a position buffer of [x,y,z,x,y,z,...].
 * 
 * @param params [[ CreatePositionBufferParams ]]
 * 
 * @returns A Float32Array of vertices
 */
export const createPositionBuffer = (params: CreatePositionBufferParams): Float32Array => {
  const vertices: Float32Array = new Float32Array(params.data.length * 3);

  const timeToGlScale = d3.scaleLinear()
    .domain([params.displayStartTimeSecs, params.displayEndTimeSecs])
    .range([params.glMin, params.glMax]);

  let time = params.startTimeSecs;
  let i = 0;
  for (const sampleValue of params.data) {
    const x = timeToGlScale(time);
    vertices[i] = x;
    vertices[i + 1] = sampleValue;
    vertices[i + 3] = 0;

    i += 3;

    time += (1 / params.sampleRate);
  }

  return vertices;
};
