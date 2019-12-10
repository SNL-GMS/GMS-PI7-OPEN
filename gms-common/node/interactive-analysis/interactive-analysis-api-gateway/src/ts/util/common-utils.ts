import * as uuid4 from 'uuid/v4';
import { CreationInfo, CreatorType } from '../common/model';
import { MILLI_SECS } from './time-utils';

// tslint:disable-next-line:no-implicit-dependencies no-var-requires no-require-imports
const random = require('math-random');

/**
 * Creates a new CreationInfo later will be own component to create/retrieve from OSD
 */
export function getCreationInfo(creationInfoId: string): CreationInfo {
    if (!creationInfoId) {
        creationInfoId =  uuid4().toString();
    } else if (typeof(creationInfoId) === 'object') {
        // !TODO for some reason creationInfoId sometimes is passed as an object
        creationInfoId = ((creationInfoId as CreationInfo).id) ?
            (creationInfoId as CreationInfo).id : uuid4().toString();
    }
    const creationInfo: CreationInfo = {
        id: creationInfoId,
        creationTime: Date.now() / MILLI_SECS,
        creatorType: CreatorType.Analyst,
        creatorId: 'creatorId',
        creatorName: 'Chris Harmon'
    };
    return creationInfo;
}

/**
 * cryptographically secure random number generation.
 * 
 * The number returned will be between 0 - 1.
 * 
 * A Cryptographically Secure Pseudo-Random Number Generator.
 * This is what produces unpredictable data that you need for security purposes.
 * use of Math.random throughout codebase is considered high criticality
 * At present, the only required use is a simple random number.
 * If additional functionality is required in the future,
 * a random number library can be created to support more
 * sophisticated usage.
 * @returns a cryptographically secure random rumber
 */
// tslint:disable-next-line:no-unnecessary-callback-wrapper
export const getSecureRandomNumber = () => random();

/**
 * Random Number Generator (used for Lat/Lon)
 * @param from lower bound
 * @param to upper bound
 * @param fixed decimal places to generate
 * @returns a secure random number
 */
export function getRandomInRange(from, to, fixed) {
    // tslint:disable-next-line: restrict-plus-operands
    return (getSecureRandomNumber() * (to - from) + from).toFixed(fixed) * 1;
}

// Constants used in random lat/lon
const LAT_RANGE = 90;
const LON_RANGE = 180;
const FIXED_DECIMAL = 3;
const OFFSET_MIN = 0.1;
const OFFSET_MAX = 2;
const RADIUS = 2;
const RES_RANGE = 4;

/**
 * Returns a random latitude from -90 to 90
 */
export function getRandomLatitude() {
    return getRandomInRange(-LAT_RANGE, LAT_RANGE, FIXED_DECIMAL);
}

/**
 * Returns a random longitude from -180 to 180
 */
export function getRandomLongitude() {
    return getRandomInRange(-LON_RANGE, LON_RANGE, FIXED_DECIMAL);
}
/**
 * Gets random residual between -4 and 4
 */
export function getRandomResidual() {
    return getRandomInRange(-RES_RANGE, RES_RANGE, FIXED_DECIMAL);
}
/**
 * Returns a small offset used in randomizing event location around a station
 */
export function getRandomOffset() {
    const sign = getSecureRandomNumber() < OFFSET_MAX ? -1 : 1;
    return getRandomInRange(OFFSET_MIN, OFFSET_MAX, FIXED_DECIMAL) * sign;
}

/**
 * Returns a point on a circle RADIUS away
 */
export function getRandomLatLonOffset() {
    const angle = getSecureRandomNumber() * Math.PI * 2;
    const x = Math.cos(angle) * RADIUS;
    const y = Math.sin(angle) * RADIUS;
    return {
        lat: x,
        lon: y
    };
}

/**
 * Checks if the object is empty by checking how many keys are present
 * @param object object to check for empty
 * @returns a boolean
 */
export function isObjectEmpty(object: any): boolean {
    return Object.keys(object).length <= 0;
}

/**
 * Walk thru the double array calling fixNaNValues for each row
 * @param values 
 */
export function fixNaNValuesDoubleArray(values: number[][]) {
    values.forEach(fixNanValues);
}

/**
 * Walks the array and replaces any NaN values with undefined
 * @param values array of numbers
 */
export function fixNanValues(values: number[]) {
    values.forEach((val, index) => {
        if (val !== undefined && isNaN(val)) {
            values[index] = undefined;
        }
    });
}
