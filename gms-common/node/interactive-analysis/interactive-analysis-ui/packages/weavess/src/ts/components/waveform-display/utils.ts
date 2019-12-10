import * as d3 from 'd3';
import { PERCENT_100 } from './constants';

/**
 * Calculates the left percentage for a given time based on the provided start and end times.
 * 
 * @param timeSeconds The time to calculate the left percentage on
 * @param startTimeSeconds The start time in seconds
 * @param endTimeSeconds The end time in seconds
 * 
 * @returns left percentage as a number
 */
export const calculateLeftPercent = (timeSeconds: number, startTimeSeconds: number, endTimeSeconds): number => {
  const scale = d3.scaleLinear()
    .domain([startTimeSeconds, endTimeSeconds])
    .range([0, 1]);
  return scale(timeSeconds) * PERCENT_100;
};

/**
 * Calculates the right percentage for a given time based on the provided start and end times.
 * 
 * @param timeSeconds The time to calculate the left percentage on
 * @param startTimeSeconds The start time in seconds
 * @param endTimeSeconds The end time in seconds
 * 
 * @returns right percentage as a number
 */
export const calculateRightPercent = (timeSeconds: number, startTimeSeconds: number, endTimeSeconds): number =>
  PERCENT_100 - calculateLeftPercent(timeSeconds, startTimeSeconds, endTimeSeconds);
