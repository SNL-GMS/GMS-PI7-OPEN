import {
  AmplitudeMeasurementValue,
  FeatureMeasurement,
  FeatureMeasurementTypeName,
  InstantMeasurementValue,
  NumericMeasurementValue,
  PhaseTypeMeasurementValue,
  StringMeasurementValue
} from '~graphql/signal-detection/types';

/**
 * Searches Feature Measurements for the desired Feature Measurement
 * @param featureMeasurements List of feature measurements
 * @param featureMeasurementType Enum of desired Feature Measurement desired
 * 
 * @returns FeatureMeasurement or undefined if not found
 */
export function findFeatureMeasurement(
  featureMeasurements: FeatureMeasurement[], featureMeasurementType: FeatureMeasurementTypeName):
  FeatureMeasurement | undefined {
  if (featureMeasurements && featureMeasurementType) {
    return featureMeasurements.find(fm => fm.featureMeasurementType === featureMeasurementType);
  }
  return undefined;
}

/**
 * Searches Feature Measurements for the ArrivalTime Feature Measurement
 * @param featureMeasurements List of feature measurements
 * 
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
 * 
 * @returns ArrivalTime FeatureMeasurementValue or undefined if not found
 */
export function findArrivalTimeFeatureMeasurementValue(
  featureMeasurements: FeatureMeasurement[]): InstantMeasurementValue | undefined {
  const fm = findArrivalTimeFeatureMeasurement(featureMeasurements);
  return (fm) ? fm.measurementValue as InstantMeasurementValue : undefined;
}

/**
 * Searches Feature Measurements for the Azimuth Feature Measurement
 * @param featureMeasurements List of feature measurements
 * 
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
 * 
 * @returns Azimuth FeatureMeasurementValue or undefined if not found
 */
export function findAzimthFeatureMeasurementValue(
  featureMeasurements: FeatureMeasurement[]): NumericMeasurementValue | undefined {
  const fm = findAzimthFeatureMeasurement(featureMeasurements);
  return (fm) ? fm.measurementValue as NumericMeasurementValue : undefined;
}

/**
 * Searches Feature Measurements for the Slowness Feature Measurement
 * @param featureMeasurements List of feature measurements
 * 
 * @returns Slowness FeatureMeasurement or undefined if not found
 */
export function findSlownessFeatureMeasurement(
  featureMeasurements: FeatureMeasurement[]): FeatureMeasurement | undefined {
  return findFeatureMeasurement(featureMeasurements, FeatureMeasurementTypeName.SLOWNESS);
}

/**
 * Searches Feature Measurements for the Slowness Feature Measurement Value
 * @param featureMeasurements List of feature measurements
 * 
 * @returns Slowness FeatureMeasurementValue or undefined if not found
 */
export function findSlownessFeatureMeasurementValue(
  featureMeasurements: FeatureMeasurement[]): NumericMeasurementValue | undefined {
  const fm = findSlownessFeatureMeasurement(featureMeasurements);
  return (fm) ? fm.measurementValue as NumericMeasurementValue : undefined;
}

/**
 * Searches Feature Measurements for the Amplitude Feature Measurement
 * @param featureMeasurements List of feature measurements
 * 
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
 * 
 * @returns Phase FeatureMeasurementValue or undefined if not found
 */
export function findAmplitudeFeatureMeasurementValue(
  featureMeasurements: FeatureMeasurement[]): AmplitudeMeasurementValue | undefined {
  const fm = findAmplitudeFeatureMeasurement(featureMeasurements);
  return (fm) ? fm.measurementValue as AmplitudeMeasurementValue : undefined;
}

/**
 * Searches Feature Measurements for the Phase Feature Measurement
 * @param featureMeasurements List of feature measurements
 * 
 * @returns Phase FeatureMeasurement or undefined if not found
 */
export function findPhaseFeatureMeasurement(
  featureMeasurements: FeatureMeasurement[]): FeatureMeasurement | undefined {
  return findFeatureMeasurement(featureMeasurements, FeatureMeasurementTypeName.PHASE);
}

/**
 * Searches Feature Measurements for the Phase Feature Measurement Value
 * @param featureMeasurements List of feature measurements
 * 
 * 
 * @returns Phase FeatureMeasurementValue or undefined if not found
 */
export function findPhaseFeatureMeasurementValue(
  featureMeasurements: FeatureMeasurement[]): PhaseTypeMeasurementValue | undefined {
  const fm = findPhaseFeatureMeasurement(featureMeasurements);
  return (fm) ? fm.measurementValue as PhaseTypeMeasurementValue : undefined;
}

/**
 * Searches Feature Measurements for the Phase Feature Measurement Value
 * @param featureMeasurements List of feature measurements
 * 
 * @returns Phase FeatureMeasurementValue or undefined if not found
 */
export function findFilteredBeamFeatureMeasurement(
  featureMeasurements: FeatureMeasurement[], filterDefinitionId: string): FeatureMeasurement| undefined {
    if (featureMeasurements) {
      const filteredBeamFM = featureMeasurements.find(fm =>
        fm.featureMeasurementType === FeatureMeasurementTypeName.FILTERED_BEAM &&
        (fm.measurementValue as StringMeasurementValue).strValue === filterDefinitionId);
      return filteredBeamFM;
    }
    return undefined;
}
