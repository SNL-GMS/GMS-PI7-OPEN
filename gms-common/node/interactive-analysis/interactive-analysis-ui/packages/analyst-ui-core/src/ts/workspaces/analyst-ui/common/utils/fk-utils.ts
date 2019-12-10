import * as Immutable from 'immutable';
import { flatMap } from 'lodash';
import { FkParams, FkUnits } from '~analyst-ui/components/azimuth-slowness/types';
import { systemConfig, userPreferences } from '~analyst-ui/config';
import { PhaseType } from '~graphql/common/types';
import { FkConfiguration, FkPowerSpectra, FrequencyBand } from '~graphql/fk/types';
import { FeatureMeasurement, SignalDetection } from '~graphql/signal-detection/types';
import { findAzimthFeatureMeasurement, findPhaseFeatureMeasurementValue } from '~graphql/signal-detection/utils';

/**
 * Utility functions for the Azimuth Slowness Display
 */

/**
 * Finds Azimuth Feature Measurements for the FkData object
 * @param featureMeasurements List of feature measurements
 * 
 * @returns FkData or undefined if not found
 */
export function getFkData(featureMeasurements: FeatureMeasurement[]): FkPowerSpectra | undefined {
    const azimuthFM = findAzimthFeatureMeasurement(featureMeasurements);
    if (azimuthFM && azimuthFM.channelSegment && azimuthFM.channelSegment.timeseries &&
        azimuthFM.channelSegment.timeseries.length > 0) {
        if (azimuthFM.channelSegment.timeseries[0]) {
          return azimuthFM.channelSegment.timeseries[0] as FkPowerSpectra;
        }
    }
    return undefined;
  }

export function getFkParamsForSd(sd: SignalDetection): FkParams {
    const fk = getFkData(sd.currentHypothesis.featureMeasurements);
    return {
      frequencyPair: {
        maxFrequencyHz: fk.highFrequency,
        minFrequencyHz: fk.lowFrequency
      },
      windowParams: {
        leadSeconds: fk.windowLead,
        lengthSeconds: fk.windowLength,
        stepSize: fk.stepSize
      }
    };
  }

/**
 * Returns an empty FK Spectrum configuration. The values are NOT default values,
 * but instead values that will make it obvious within the UI that a correct
 * configuration was never added to the FK
 */
const defaultFkConfiguration: FkConfiguration = {
  contributingChannelsConfiguration: [],
  maximumSlowness: systemConfig.continousFkConfiguration.defaultMaximumSlowness,
  mediumVelocity: 1,
  normalizeWaveforms: false,
  numberOfPoints: systemConfig.continousFkConfiguration.defaultNumberOfPoints,
  useChannelVerticalOffset: false,
  leadFkSpectrumSeconds: userPreferences.azimuthSlowness.defaultLead
};

/**
 * Returns an Fk Configuration for the correct phase
 */
export function getDefaultFkConfigurationForSignalDetection(sd: SignalDetection) {
  // Check and see if SD is well formed
  if (!sd || !sd.currentHypothesis || !sd.currentHypothesis.featureMeasurements) {
    return undefined;
  }
  const phase = findPhaseFeatureMeasurementValue(sd.currentHypothesis.featureMeasurements).phase;
  const phaseAsString = PhaseType[phase];
  const channels = flatMap(sd.station.sites, site =>  site.channels);
  const contributingChannelsConfiguration = channels.map(channel => ({
    name: channel.name,
    id: channel.id,
    enabled: true
  }));
  let mediumVelocity = 0;
// tslint:disable-next-line: newline-per-chained-call
  if (phaseAsString.toLowerCase().startsWith('p') || phaseAsString.toLowerCase().endsWith('p')) {
  // tslint:disable-next-line: no-magic-numbers
    mediumVelocity = 5.8;
// tslint:disable-next-line: newline-per-chained-call
  } else if (phaseAsString.toLowerCase().startsWith('s') || phaseAsString.toLowerCase().endsWith('s')) {
  // tslint:disable-next-line: no-magic-numbers
    mediumVelocity = 3.6;
  } else if (phaseAsString === PhaseType.Lg) {
  // tslint:disable-next-line: no-magic-numbers
    mediumVelocity = 3.5;
  } else if (phaseAsString === PhaseType.Rg) {
  // tslint:disable-next-line: number-literal-format
    mediumVelocity = 3.0;
  } else {
    // Cause Tx or N...undefined behavior ok
    mediumVelocity = 1;
  }
  const fkConfiguration: FkConfiguration = {
    ...defaultFkConfiguration,
    mediumVelocity,
    contributingChannelsConfiguration
  };
  return fkConfiguration;
}

/**
 * Gets the user-set fk unit for a given fk id, or returns the default unit
 * 
 * @param fkId the id of the fk
 */
export function getFkUnitForSdId(sdId: string, fkUnitsForEachSdId: Immutable.Map<string, FkUnits>): FkUnits {
  return (fkUnitsForEachSdId.has(sdId)) ? fkUnitsForEachSdId.get(sdId) : FkUnits.FSTAT;
}

/**
 * Formats a frequency band into a string for the drop down
 * @param band Frequency band to format
 */
export function frequencyBandToString(band: FrequencyBand): string {
  return `${band.minFrequencyHz} - ${band.maxFrequencyHz} Hz`;
}
