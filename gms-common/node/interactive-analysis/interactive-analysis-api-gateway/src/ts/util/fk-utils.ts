import {
    FkPowerSpectra,
    FkPowerSpectrum,
    FkPowerSpectraOSD,
    FkConfiguration,
    PhaseType,
    FstatData,
} from '../channel-segment/model-spectra';
import { OSDWaveform, Waveform } from '../waveform/model';
import { toEpochSeconds, getDurationTime } from './time-utils';
import { OSDChannelSegment, ChannelSegment } from '../channel-segment/model';
import { SignalDetection } from '../signal-detection/model';
import { findPhaseFeatureMeasurementValue, findArrivalTimeFeatureMeasurementValue } from './signal-detection-utils';
import { stationProcessor } from '../station/station-processor';
import { fixNaNValuesDoubleArray } from './common-utils';

/**
 * Converts the OSD Waveform into a API Gateway compatiable Waveform
 * @param osdWaveform 
 * @returns Waveform converted OSD Waveform to API Gateway compatiable
 */
export function convertOSDWaveformToWaveform(osdWaveform: OSDWaveform): Waveform {
    return {
        ...osdWaveform,
        startTime: toEpochSeconds(osdWaveform.startTime)
    };
}

/**
 * Returns the FkSpectrum based on the window lead
 * @param fkPowerSpectra the spectra to extract a spectrum from
 * @param arrivalTime the arrival time of the parent signal detection
 * @returns the FkSpectrum closest to the lead sections set in the display
 */
export const getLeadFkSpectrum =
  (fkSpectra: FkPowerSpectra, arrivalTime: number): FkPowerSpectrum => {
  // If the channel segment is populated at the top properly
  if (!fkSpectra) {
      return undefined;
  }

//   const offsetStartTime = calculateStartTimeForFk(
//       fkSpectra.startTime, arrivalTime, fkSpectra.windowLead, fkSpectra.stepSize);
  // Figure out what to with multiple timeseries
  let epochLeadSpectrumTime = arrivalTime - fkSpectra.configuration.leadFkSpectrumSeconds;
  if (epochLeadSpectrumTime < fkSpectra.startTime) {
    epochLeadSpectrumTime = fkSpectra.startTime;
  }

  const sampleRate = 1 / fkSpectra.stepSize;
  // number seconds to offset into spectrums
  let position = (epochLeadSpectrumTime - fkSpectra.startTime) * sampleRate;
  // Round the position to figure which step (index position) we should use
  position = Math.round(position);

  if (position > fkSpectra.spectrums.length - 1) {
      position = fkSpectra.spectrums.length - 1;
  }

  // console.log("Position of lead spectrum is: " + position);

  // Now set the windowLead (snap) to the spectrum chosen
  // Should be the leadSpectrum startTime - arrivalTime
//   const arrivOffsetFromStart = (arrivalTime - fkSpectra.startTime);
//   const positionOffsetFromStart = (position / fkSpectra.sampleRate);
//   const windowLead = arrivOffsetFromStart - positionOffsetFromStart;
//   fkSpectra.configuration.leadFkSpectrumSeconds = windowLead;
  fkSpectra.leadSpectrum = fkSpectra.spectrums[position];
  return fkSpectra.spectrums[position];
};

/**
 * Convert the OSD Fk Power Spectra to API Gateway compatiable Fk Power Spectra
 * @param fkChannelSegment Fk Power Spectra Channel Segment
 * @param sd signal detection
 * @returns a FkPowerSpectra ChannelSegment
 */
export function convertOSDFk(
    fkChannelSegment: OSDChannelSegment<FkPowerSpectraOSD>, sd: SignalDetection): ChannelSegment<FkPowerSpectra> {
    const fk: ChannelSegment<FkPowerSpectra> = {
        ...fkChannelSegment,
        startTime: toEpochSeconds(fkChannelSegment.startTime),
        endTime:
            (toEpochSeconds(fkChannelSegment.startTime)
            + (fkChannelSegment.timeseries[0].sampleRate * fkChannelSegment.timeseries[0].sampleCount)),
        timeseries: fkChannelSegment.timeseries.map(ts => {
            // Copy the values to be converted to spectrums and remove values entry
            const values = ts.values;
            const spectrums = values.map(value => ({
                ...value,
                attributes: value.attributes[0]
            }));
            delete ts.values;
            // Get the arrival time to set leadSpectrum and powerSpectrum
            const arrivalTimeFMValue =
                findArrivalTimeFeatureMeasurementValue(sd.currentHypothesis.featureMeasurements);
            // Create the timeseries [0] entry
            const tsEntry: FkPowerSpectra = ({
                ...ts,
                reviewed: false, // Since new from OSD mark as not reviewed by analyst
                windowLead: getDurationTime(ts.windowLead),
                windowLength: getDurationTime(ts.windowLength),
                stepSize: 1,
                startTime: toEpochSeconds(ts.startTime),
                spectrums,
                leadSpectrum: undefined,
                fstatData: undefined,
                configuration: getDefaultFkConfigurationForSignalDetection(
                    findPhaseFeatureMeasurementValue(sd.currentHypothesis.featureMeasurements).phase,
                    sd.stationId)
            });
            // Check FstatGrid and PowerGrid for NaN
            tsEntry.spectrums.forEach(spectrum => {
                fixNaNValuesDoubleArray(spectrum.fstat);
                fixNaNValuesDoubleArray(spectrum.power);
            });
            // Populate fstatData and leadSpectrum based now the FkPowerSpectra is created
            setFstatData(tsEntry);
            tsEntry.leadSpectrum = getLeadFkSpectrum(tsEntry, arrivalTimeFMValue.value);
            return tsEntry;
        }),
        creationInfo: {
            creationTime: toEpochSeconds(fkChannelSegment.creationInfo.creationTime),
            creatorId: fkChannelSegment.creationInfo.creatorId,
            creatorName: fkChannelSegment.creationInfo.creatorName,
            creatorType: fkChannelSegment.creationInfo.creatorType,
            id: fkChannelSegment.creationInfo.id
        }
    };
    return fk;
}
/**
 * Returns an empty FK Spectrum configuration. The values are NOT default values,
 * but instead values that will make it obvious within the UI that a correct
 * configuration was never added to the FK
 * @returns a FKConfiguration
 */
const defaultFkConfiguration: FkConfiguration = {
        contributingChannelsConfiguration: [],
        maximumSlowness: 40,
        mediumVelocity: 1,
        normalizeWaveforms: false,
        numberOfPoints: 81,
        useChannelVerticalOffset: false,
        leadFkSpectrumSeconds: 1
};

/**
 * Returns an Fk Configuration for the correct phase
 */
export function getDefaultFkConfigurationForSignalDetection(phase: PhaseType, stationId: string) {
    const phaseAsString = PhaseType[phase];
    const channels = stationProcessor.getChannelsByStation(stationId);
    const contributingChannelsConfiguration = channels.map(channel => ({
        name: channel.name,
        id: channel.id,
        enabled: true
    }));
    let mediumVelocity = 0;
    if (phaseAsString.toLowerCase().startsWith('p') || phaseAsString.toLowerCase().endsWith('p')) {
    // tslint:disable-next-line: no-magic-numbers
        mediumVelocity = 5.8;
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
 * Approximate conversion between km and degrees
 */
export function kmToDegreesApproximate(km: number) {
    const DEGREES_IN_CIRCLE = 360;
    const RAD_EARTH = 6371;
    const TWO_PI = Math.PI * 2;
    return km * ((DEGREES_IN_CIRCLE) / (RAD_EARTH * TWO_PI));
}

/**
 * Create the FstatData for use by the Fk Plots in the Az/Slow component
 * @param signalDetHypo Signal Detection Hypothesis
 * @param fkSpectra FkPowerData
 * 
 * @returns FK Stat Data or undefined if not able to create
 */
export function setFstatData(fkSpectra: FkPowerSpectra) {
    const fstatData = convertToPlotData(fkSpectra);
    // Cache it in the FK Power Spectra
    fkSpectra.fstatData = fstatData;
}

/**
 * Convert a FkSpectra (received from COI or Streaming Service) into an FstatData representation.
 * @param fkSpectra: FkPowerSpectra from COI/Streaming Service
 * @param beamWaveform: beam from the SD Arrival Time Featuremeasurement Channel Segment
 * @param arrivalTime: arrival time value
 * 
 * @returns FK Stat Data or undefined if not able to create
 */
function convertToPlotData(fkSpectra: FkPowerSpectra): FstatData | undefined {
    // If the channel segment is populated at the top properly
    if (!fkSpectra) {
        return undefined;
    }
    const fstatData: FstatData = {
            azimuthWf: createFkWaveform(fkSpectra),
            fstatWf: createFkWaveform(fkSpectra),
            slownessWf: createFkWaveform(fkSpectra)
    };

    // Populate fstatData waveforms beams was a parameter
    if (fkSpectra && fkSpectra.spectrums) {
        fkSpectra.spectrums.forEach(((fkSpectrum: FkPowerSpectrum) => {
            fstatData.azimuthWf.values.push(fkSpectrum.attributes.azimuth);
            fstatData.fstatWf.values.push(fkSpectrum.attributes.peakFStat);
            fstatData.slownessWf.values.push(fkSpectrum.attributes.slowness);
        }));
    }
    return fstatData;
}

/**
 * Helper method to create the FkData waveforms (azimthWf, fstatWf, slownessWf)
 * @param fkSpectra 
 */
function createFkWaveform(fkSpectra: FkPowerSpectra): Waveform {
    const waveform = {
        sampleRate: fkSpectra.sampleRate,
        sampleCount: fkSpectra.sampleCount,
        startTime: fkSpectra.startTime + (fkSpectra.windowLead),
        values: []
    };
    return waveform;
}
/**
 * Checks if the returned value from a service doesn't contain any data
 * @param maybeFkOsd
 */
export function isEmptyReturnFromFkService(maybeFkOsd: any): boolean {
    if (!maybeFkOsd) {
        return true;
    }
    if (!maybeFkOsd[0] || maybeFkOsd[0] === null) {
        return true;
    }
    return false;
}
/**
 * Checks a channel segment for NaNs
 * @param fkChannelSegment 
 */
export function nansInFkGrid(fkChannelSegment: ChannelSegment<FkPowerSpectra>): boolean {
    let areNansPresent = false;
    fkChannelSegment.timeseries[0].spectrums.forEach(spectrum => {
        // Check if there was an issue in the previous spectrum
        if (areNansPresent) {
            return true;
        }
        spectrum.fstat.forEach(fstatRow => fstatRow.forEach(fstatEntry => {
            if (fstatEntry !== undefined && isNaN(fstatEntry)) {
                areNansPresent = true;
            }
        }));
        // Check if there was an issue in the spectrum
        if (areNansPresent) {
            return true;
        }
        spectrum.power.forEach(powerRow => powerRow.forEach(powerEntry => {
            if (powerEntry !== undefined && isNaN(powerEntry)) {
                areNansPresent = true;
            }
        }));
    });
    return areNansPresent;
}
