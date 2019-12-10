import { PubSub } from 'graphql-subscriptions';
import * as model from './model';
import { ChannelSegmentProcessor } from './channel-segment-processor';
import { stationProcessor } from '../station/station-processor';
import { signalDetectionProcessor } from '../signal-detection/signal-detection-processor';
import { performanceLogger } from '../log/performance-logger';
import { findAzimthFeatureMeasurement, findArrivalTimeFeatureMeasurementValue } from '../util/signal-detection-utils';
import { getLeadFkSpectrum } from '../util/fk-utils';
import { FkFrequencyThumbnailBySDId } from './model-spectra';

/**
 * Resolvers for the signal detection API gateway
 */

// Create the publish/subscribe API for GraphQL subscriptions
export const pubsub = new PubSub();

// GraphQL Resolvers
export const resolvers = {
    // Query Resolvers
    Query: {
        // Compute the Fk Thumbnails for UI Display. Should be called from UI after a new compute Fk is called
        computeFkFrequencyThumbnails: async (_, { fkInput }): Promise<FkFrequencyThumbnailBySDId> => {
            performanceLogger.performance(
                'computeFkFrequencyThumbnails', 'enteringResolver', fkInput.signalDetectionId);
            // Compute the Thumbnail Fks
            const thumbnailsBySdID = await ChannelSegmentProcessor.Instance().computeFkFrequencyThumbnails(fkInput);
            performanceLogger.performance('computeFkFrequencyThumbnails', 'leavingResolver', fkInput.signalDetectionId);
            return thumbnailsBySdID;
        },
    },

    // Mutation resolvers
     Mutation: {
        // Set Window Lead and return newly populated leadSpectrum
        setFkWindowLead: async (_, { leadInput }) => {
            performanceLogger.performance('setLeadFkSpectrum', 'enteringResolver', leadInput.signalDetectionId);
            // Set the lead seconds on the FkPowerSpectra channel segment
            // First get the signal detection then set the lead seconds on the
            // Azimuth channel segment
            const sdList = [];
            const sd = signalDetectionProcessor.getSignalDetectionById(leadInput.signalDetectionId);
            if (sd) {
                sdList.push(sd);
                const azimuthFM = findAzimthFeatureMeasurement(sd.currentHypothesis.featureMeasurements);
                if (azimuthFM) {
                    // Should be in cache since the Azimuth/Slowness component is the client using this mutation
                    const fkCS = await ChannelSegmentProcessor.Instance().getChannelSegment(azimuthFM.channelSegmentId);
                    if (fkCS && model.isFkSpectraChannelSegment(fkCS) &&
                        fkCS.timeseries && fkCS.timeseries.length > 0) {
                        fkCS.timeseries[0].configuration.leadFkSpectrumSeconds = leadInput.leadFkSpectrumSeconds;
                        fkCS.timeseries[0].windowLead = leadInput.windowLeadSeconds;

                        // Now update the Lead FkSpectrum, but first lookup arrivalTime value
                        const arrivalTimeValue =
                            findArrivalTimeFeatureMeasurementValue(sd.currentHypothesis.featureMeasurements);
                        if (arrivalTimeValue && arrivalTimeValue.value) {
                            fkCS.timeseries[0].leadSpectrum =
                                getLeadFkSpectrum(fkCS.timeseries[0], arrivalTimeValue.value);
                        }
                    }
                }
            }
            performanceLogger.performance('setLeadFkSpectrum', 'leavingResolver', leadInput.signalDetectionId);
            return {
                events: [],
                sds: sdList
            };
        },

        // Compute new fks
        computeFks: async (_, { fkInput }) => {
            performanceLogger.performance('computeFks', 'enteringResolver', fkInput.signalDetectionId);
            // Compute a new Fk and return the modified SignalDetections
            const promises = fkInput.map(async input =>
                    await ChannelSegmentProcessor.Instance().computeFk(input)
            );
            const signalDetections = await Promise.all(promises);
            const filteredSDs = signalDetections.filter(sd => sd !== undefined);
            performanceLogger.performance('computeFks', 'leavingResolver', fkInput.signalDetectionId);
            return {
                events: [],
                sds: filteredSDs
            };
        },
        markFksReviewed: async (_, { markFksReviewedInput }) =>
            // Call channel segment processor to update review flag in the appropriate
            // Azimuth SD Feature Measurement and return the list of ones successfully updated
            ({
                events: [],
                sds: ChannelSegmentProcessor.Instance()
                .updateFkReviewedStatuses(markFksReviewedInput, markFksReviewedInput.reviewed)
            }),
        // call to calculate a new beam for the Signal Detection
        // TODO: Not sure this mutation should be called from UI. It is called whenever FK is computed.
        computeBeam: async (_, { input }) => {
            const signalDetection = signalDetectionProcessor.getSignalDetectionById(input.signalDetectionId);
            return await ChannelSegmentProcessor.Instance().computeBeam(input, signalDetection);
        }
    },

    // Field resolvers for Channel Segment
    ChannelSegment: {
        channel: async (channelSegment: model.ChannelSegment<model.TimeSeries>) =>
            stationProcessor.getChannelById(channelSegment.channelId),
        creationInfo: async (creationInfoId: string) =>
            signalDetectionProcessor.getCreationInfo(creationInfoId),
    },

    FkPowerSpectra: {
        contribChannels: async () => []
    },
    // Field resolvers for Timeseries
    Timeseries: {
        /**
         * Special interface resolver to determine the implementing type based on field content
         */
        __resolveType(obj) {
            if (obj) {
                if (obj.spectrums !== undefined) {
                    return 'FkPowerSpectra';
                } else if (obj.values !== undefined) {
                    return 'Waveform';
                }
            }
            return undefined;
        }
    }
};
