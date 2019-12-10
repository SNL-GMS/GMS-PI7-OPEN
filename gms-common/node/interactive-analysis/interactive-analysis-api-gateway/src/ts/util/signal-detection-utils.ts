import {
    FeatureMeasurement,
    FeatureMeasurementTypeName,
    FeatureMeasurementValue,
    InstantMeasurementValue,
    NumericMeasurementValue,
    PhaseTypeMeasurementValue,
    AmplitudeMeasurementValue,
    SignalDetection
} from '../signal-detection/model';
import { cloneDeep } from 'apollo-utilities';
import { toOSDTime } from './time-utils';

/**
 * Searches Feature Measurements for the desired Feature Measurement and returns the feature measurement.
 * @param featureMeasurements List of feature measurements
 * @param featureMeasurementType Enum of desired Feature Measurement desired
 * @returns FeatureMeasurement or undefined if not found
 */
export function findFeatureMeasurement(
    featureMeasurements: FeatureMeasurement[],
    featureMeasurementType: FeatureMeasurementTypeName): FeatureMeasurement | undefined {
    if (featureMeasurements && featureMeasurementType) {
        return featureMeasurements.find(
            f => f.featureMeasurementType === featureMeasurementType);
    }
    return undefined;
}

/**
 * Searches Feature Measurements for the desired Feature Measurement and returns the 
 * feature measurement value.
 * @param featureMeasurements List of feature measurements
 * @param featureMeasurementType Enum of desired Feature Measurement desired
 * @returns FeatureMeasurementValue or undefined if not found
 */
export function findFeatureMeasurementValue<T extends FeatureMeasurementValue>(
    featureMeasurements: FeatureMeasurement[],
    featureMeasurementType: FeatureMeasurementTypeName): T | undefined {
    if (featureMeasurements && featureMeasurementType) {
        const fm = featureMeasurements.find(
            f => f.featureMeasurementType === featureMeasurementType);
        return fm ? fm.measurementValue as T : undefined;
    }
    return undefined;
}

/**
 * Searches Feature Measurements for the ArrivalTime Feature Measurement
 * @param featureMeasurements List of feature measurements
 * @returns ArrivalTime FeatureMeasurement or undefined if not found
 */
export function findArrivalTimeFeatureMeasurement(
    featureMeasurements: FeatureMeasurement[]):
    FeatureMeasurement | undefined {
    return findFeatureMeasurement(featureMeasurements, FeatureMeasurementTypeName.ARRIVAL_TIME);
}

/**
 * Searches Feature Measurements for the ArrivalTime Feature Measurement Value
 * @param featureMeasurements List of feature measurements
 * @returns ArrivalTime FeatureMeasurementValue or undefined if not found
 */
export function findArrivalTimeFeatureMeasurementValue(
    featureMeasurements: FeatureMeasurement[]):
    InstantMeasurementValue | undefined {
    return findArrivalTimeFeatureMeasurement(featureMeasurements).measurementValue as InstantMeasurementValue;
}

/**
 * Searches Feature Measurements for the Azimuth Feature Measurement
 * @param featureMeasurements List of feature measurements
 * @returns Azimuth FeatureMeasurement or undefined if not found
 */
export function findAzimthFeatureMeasurement(
    featureMeasurements: FeatureMeasurement[]):
    FeatureMeasurement | undefined {
    const azimuthList: FeatureMeasurementTypeName[] = [FeatureMeasurementTypeName.RECEIVER_TO_SOURCE_AZIMUTH,
        FeatureMeasurementTypeName.SOURCE_TO_RECEIVER_AZIMUTH];
    // Search FeatureMeasurements to find which type of Azimuth was supplied
    return featureMeasurements.find(fm => azimuthList.find(
        azTypeName => azTypeName === fm.featureMeasurementType) !== undefined);
}

/**
 * Searches Feature Measurements for the Azimuth Feature Measurement Value
 * @param featureMeasurements List of feature measurements
 * @returns Azimuth FeatureMeasurementValue or undefined if not found
 */
export function findAzimthFeatureMeasurementValue(
    featureMeasurements: FeatureMeasurement[]):
    NumericMeasurementValue | undefined {
    return findAzimthFeatureMeasurement(featureMeasurements).measurementValue as NumericMeasurementValue;
}

/**
 * Searches Feature Measurements for the Slowness Feature Measurement
 * @param featureMeasurements List of feature measurements
 * @returns Slowness FeatureMeasurement or undefined if not found
 */
export function findSlownessFeatureMeasurement(
    featureMeasurements: FeatureMeasurement[]):
    FeatureMeasurement | undefined {
    return findFeatureMeasurement(featureMeasurements, FeatureMeasurementTypeName.SLOWNESS);
}

/**
 * Searches Feature Measurements for the Slowness Feature Measurement Value
 * @param featureMeasurements List of feature measurements
 * @returns Slowness FeatureMeasurementValue or undefined if not found
 */
export function findSlownessFeatureMeasurementValue(
    featureMeasurements: FeatureMeasurement[]):
    NumericMeasurementValue | undefined {
    return findSlownessFeatureMeasurement(featureMeasurements).measurementValue as NumericMeasurementValue;
}

/**
 * Searches Feature Measurements for the Amplitude Feature Measurement
 * @param featureMeasurements List of feature measurements
 * @returns Phase FeatureMeasurement or undefined if not found
 */
export function findAmplitudeFeatureMeasurement(
    featureMeasurements: FeatureMeasurement[]): FeatureMeasurement | undefined {
    const amplitudeList: FeatureMeasurementTypeName[] = [FeatureMeasurementTypeName.AMPLITUDE,
    FeatureMeasurementTypeName.AMPLITUDE_A5_OVER_2, FeatureMeasurementTypeName.AMPLITUDE_ALR_OVER_2,
    FeatureMeasurementTypeName.AMPLITUDE_ANL_OVER_2, FeatureMeasurementTypeName.AMPLITUDEh_ALR_OVER_2];

    // Search FeatureMeasurements to find which type of Amplitude was supplied
    return featureMeasurements.find(fm => amplitudeList.find(
      ampTypeName => ampTypeName === fm.featureMeasurementType) !== undefined);
  }

/**
 * Searches Feature Measurements for the Amplitude Feature Measurement Value
 * @param featureMeasurements List of feature measurements
 * @returns Phase FeatureMeasurementValue or undefined if not found
 */
export function findAmplitudeFeatureMeasurementValue(
    featureMeasurements: FeatureMeasurement[]):
    AmplitudeMeasurementValue | undefined {
    return findAmplitudeFeatureMeasurement(featureMeasurements).measurementValue as AmplitudeMeasurementValue;
}

/**
 * Searches Feature Measurements for the Phase Feature Measurement
 * @param featureMeasurements List of feature measurements
 * @returns Phase FeatureMeasurement or undefined if not found
 */
export function findPhaseFeatureMeasurement(
    featureMeasurements: FeatureMeasurement[]):
    FeatureMeasurement | undefined {
    return findFeatureMeasurement(featureMeasurements, FeatureMeasurementTypeName.PHASE);
}

/**
 * Searches Feature Measurements for the Phase Feature Measurement Value
 * @param featureMeasurements List of feature measurements
 * @returns Phase FeatureMeasurementValue or undefined if not found
 */
export function findPhaseFeatureMeasurementValue(
    featureMeasurements: FeatureMeasurement[]):
    PhaseTypeMeasurementValue | undefined {
    return findPhaseFeatureMeasurement(featureMeasurements).measurementValue as PhaseTypeMeasurementValue;
}

/**
 * Determine if FeatureMeasurement Value structure is a NumericMeasurementValue
 * By looking if the reference time and measurement value are populated
 * @param fmv FeatureMeasurementValue (generic)
 * @return boolean
 */
export function isNumericFeatureMeasurementValue(fmv: FeatureMeasurementValue): fmv is NumericMeasurementValue {
    const referenceTime = (fmv as NumericMeasurementValue).referenceTime;
    const measurementValue = (fmv as NumericMeasurementValue).measurementValue;
    return referenceTime !== undefined && measurementValue !== undefined;
}

/**
 * Determine if FeatureMeasurement Value structure is a NumericMeasurementValue
 * By looking if the reference time and measurement value are populated
 * @param fmv FeatureMeasurementValue (generic)
 * @return boolean
 */
export function isAmplitudeFeatureMeasurementValue(fmv: FeatureMeasurementValue): fmv is AmplitudeMeasurementValue {
    const amplitude = (fmv as AmplitudeMeasurementValue).amplitude;
    const period = (fmv as AmplitudeMeasurementValue).period;
    const startTime = (fmv as AmplitudeMeasurementValue).startTime;
    return amplitude !== undefined && period !== undefined && startTime !== undefined;
}

/**
 * Wraps flat fm type to object
 * @param fmTypeNames fm type names to wrap to send to OSD
 */
export function wrapFeatureMeasurement(fmTypeName: FeatureMeasurementTypeName) {
    return {
        featureMeasurementTypeName: fmTypeName
    };
}

/**
 * Converts API Gateway SD to OSD format to send to OSD
 * @param sdToConvert 
 */
export function convertSDtoOSD(sdToConvert: SignalDetection): SignalDetection {
    const sd = cloneDeep(sdToConvert);
    sd.signalDetectionHypotheses.forEach(sdh => {
        const fms = [];
        sdh.featureMeasurements.forEach(fm => {
            delete fm.definingRules;
            if (fm.featureMeasurementType ===
                FeatureMeasurementTypeName.ARRIVAL_TIME) {
                const epochSeconds = (fm.measurementValue as InstantMeasurementValue).value;
                const value = toOSDTime(epochSeconds);
                const newFMValue = {
                    ...fm.measurementValue,
                    value
                };
                fm.measurementValue = newFMValue;
                fms.push(fm);
            } else if (fm.featureMeasurementType ===
                FeatureMeasurementTypeName.PHASE) {
                const value = (fm.measurementValue as PhaseTypeMeasurementValue).phase;
                const newFMValue = {
                    ...fm.measurementValue,
                    value
                };
                fm.measurementValue = newFMValue;
                fms.push(fm);
            } else if (fm.featureMeasurementType !== FeatureMeasurementTypeName.FILTERED_BEAM) {
                fms.push(fm);
            }
        });
        sdh.featureMeasurements = fms;
    });
    delete sd.modified;
    delete sd.associationModified;
    delete sd.currentHypothesis;
    return sd;
}
