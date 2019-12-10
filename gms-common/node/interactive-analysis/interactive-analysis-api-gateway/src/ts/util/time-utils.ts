import * as moment from 'moment';
import { gatewayLogger as logger } from '../log/gateway-logger';

export const MILLI_SECS = 1000;

/**
 * Helper function to convert OSD compatible ISO formatted date string to epoch seconds.
 * @param dateString date string in ISO format
 * @returns an epoch seconds representation of the input date spring
 */
export function toEpochSeconds(dateString: string | undefined): number {
  if (dateString === undefined) {
    return 0;
  }

  return new Date(dateString).getTime() / MILLI_SECS;
}

/**
 * Helper function to get the current time in epoch seconds.
 * @returns an epoch seconds for time now
 */
export function epochSecondsNow(): number {
  return Date.now() / MILLI_SECS;
}

/**
 * Helper function to convert epoch seconds to OSD compatible ISO formatted date string.
 * @param epochSeconds seconds since epoch
 * @returns a New Date string in OSD format
 */
export function toOSDTime(epochSeconds: number): string {
  if (isNaN(epochSeconds)) {
    return toOSDTime(0);
  }
  return new Date(epochSeconds * MILLI_SECS).toISOString();
}

/**
 * Helper function to convert a Moment string and return epoch seconds as a number.
 * @param duration string of duration i.e. 'PT1.60S' returns 1.6 seconds
 * @returns a number
 */
export function getDurationTime(duration: string): number {
  // Using millisecs since asSeconds loses precision
  return moment.duration(duration).asMilliseconds() / MILLI_SECS;
}

/**
 * Helper function to format a seconds into duration format.
 * @param duration number of 1.6 seconds returns 'PT1.60S'
 * @returns a string
 */
export function setDurationTime(seconds: number): string {
  // Return formatted string
  return `PT${seconds}S`;
}

/**
 * Calculates start time for fk service
 * @param wfStartTime start of the signal detection beam
 * @param arrivalTime arrival time of the signal detection
 * @param leadTime lead time for fk calculation
 * @param stepSize step size for fk calculation
 * 
 * @return epoch seconds representing the start time for fk calculation
 */
export function calculateStartTimeForFk(
  wfStartTime: number, arrivalTime: number, leadTime: number, stepSize: number): number {
    if (wfStartTime === undefined || arrivalTime === undefined ||
        leadTime === undefined || stepSize === undefined) {
      logger.warn('Cannot calculate fk start time with undefined parameters');
      return undefined;
    }
    const stepTime = (arrivalTime - wfStartTime) - leadTime;
    const numberOfSteps = Math.floor(stepTime / stepSize);
    if (numberOfSteps < 0) {
      logger.warn('Cannot calculate fk start time. Wf start time is not far enough before arrival time');
      return undefined;
    }
    const timeBeforeArrival = (stepSize * numberOfSteps) + leadTime;
    return arrivalTime - timeBeforeArrival;
  }
