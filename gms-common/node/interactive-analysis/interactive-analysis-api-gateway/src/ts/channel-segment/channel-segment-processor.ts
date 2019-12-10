import * as config from 'config';
import { isEmpty } from 'lodash';
import * as channelSegmentMockBackend from './channel-segment-mock-backend';
import * as waveformMockBackend from '../waveform/waveform-mock-backend';
import { gatewayLogger as logger } from '../log/gateway-logger';
import { HttpClientWrapper } from '../util/http-wrapper';
import { ChannelSegment, OSDChannelSegment, ChannelSegmentInput, ChannelSegmentByIdInput,
         TimeSeries, OSDTimeSeries, ChannelSegmentType, isWaveformChannelSegment,
         isFkSpectraChannelSegment,
         isFkSpectraChannelSegmentOSD,
         BeamFormingInput } from './model';
import { Waveform } from '../waveform/model';
import { convertChannelSegmentFromOSDToAPI,
         convertChannelSegmentFromAPIToOSD,
         truncateChannelSegmentTimeseries } from '../util/channel-segment-utils';
import { toOSDTime, setDurationTime, calculateStartTimeForFk } from '../util/time-utils';
import { FkPowerSpectra, FkInput, ComputeFkInput, FkPowerSpectraOSD,
         PhaseType, FkConfiguration, MarkFksReviewedInput,
         FkFrequencyThumbnail, FkFrequencyThumbnailBySDId } from './model-spectra';
import { signalDetectionProcessor } from '../signal-detection/signal-detection-processor';
import { findArrivalTimeFeatureMeasurement,
         findAzimthFeatureMeasurement,
         findSlownessFeatureMeasurement,
         findPhaseFeatureMeasurementValue,
         findArrivalTimeFeatureMeasurementValue } from '../util/signal-detection-utils';
import { SignalDetection, InstantMeasurementValue } from '../signal-detection/model';
import { stationProcessor } from '../station/station-processor';
import { AssociationChange, FrequencyBand } from '../common/model';
import { configProcessor } from '../config/config-processor';
import { WaveformProcessor } from '../waveform/waveform-processor';
import { performanceLogger } from '../log/performance-logger';
import { convertOSDFk, kmToDegreesApproximate, isEmptyReturnFromFkService } from '../util/fk-utils';
// import { compareFkSpectrumGrids } from '../util/debug-utils';

/**
 * Interface for reporting the found and missed channel segments in a cache lookup. The 
 * interface is used in multiple contexts where misses could be channel ids or channel segment ids.
 */
interface CacheResults {
    hits: ChannelSegment<TimeSeries>[];
    misses: string[];
}

/**
 * The channel segment processor which fetches channel segments for feature measurements
 * associated to signal detections which, use the channnel segments ID as a claim check
 * Also returns raw channel segments associated to a reference channel for a time interval.
 */
export class ChannelSegmentProcessor {

    /** The singleton instance */
    private static instance: ChannelSegmentProcessor;

    /**
     * Returns the singleton instance of the waveform filter processor.
     */
    public static Instance(): ChannelSegmentProcessor {
        if (ChannelSegmentProcessor.instance === undefined) {
            ChannelSegmentProcessor.instance = new ChannelSegmentProcessor();
            ChannelSegmentProcessor.instance.initialize();
        }
        return ChannelSegmentProcessor.instance;
    }

    /** Local configuration settings for URLs, mocking, and other values. */
    private settings: any;

    /**
     * HTTP client wrapper for communicating with the OSD - used for easy mocking of URLs
     * when using mock backend.
     */
    private httpWrapper: HttpClientWrapper;

    /** Local cache of channel segments received from the OSD. */
    private cache: Map<string, ChannelSegment<TimeSeries>>;

    private constructor() {
        this.settings = config.get('channel-segment');
        this.httpWrapper = new HttpClientWrapper();
        this.cache = new Map();
    }

    /**
     * Retrieve the channel segment data matching the channel segment id input. If the optional
     * type of the channel segment is passed in the cache will not be checked for other types, 
     * so leave it blank if uncertain.
     * 
     * @param channelSegmentId the id of the channel segment to return
     * @param sd optional signal detection, 
     *  used to populate default fk configuration correctly if getting az slow channel segment
     */
    public async getChannelSegment(channelSegmentId: string,
                                   sd?: SignalDetection): Promise<ChannelSegment<TimeSeries>> | undefined {
        if (!channelSegmentId) {
            return undefined;
        }
        // check cache for channel segment
        const cachedChannelSegment: ChannelSegment<TimeSeries> | undefined =
            this.getInCacheChannelSegmentById(channelSegmentId);
        if (cachedChannelSegment) {
            return cachedChannelSegment;
        }

        // Cached segment not found, request from OSD
        const requestConfig = this.settings.backend.services.channelSegments.requestConfig;
        const query: ChannelSegmentByIdInput = {
            ids: [channelSegmentId],
            'with-waveforms': true
        };
        // tslint:disable-next-line:max-line-length
        logger.debug(`Sending service request: ${JSON.stringify(requestConfig, undefined, 2)} query: ${JSON.stringify(query, undefined, 2)}`);
        const channelSegmentOSDMap: any =
            await this.httpWrapper.request(requestConfig, query);
        if (channelSegmentOSDMap && isFkSpectraChannelSegmentOSD(channelSegmentOSDMap)) {
            // Raw from osd probably shouldn't include a real fk configuration...
            if (sd) {
                return convertOSDFk(channelSegmentOSDMap, sd);
            } else {
                logger.error('Must include signal detectionas part of call for Fk Spectra Channel segment',
                             channelSegmentId);
            }
        }
        if (channelSegmentOSDMap && Object.keys(channelSegmentOSDMap).length > 0) {
            const channelSegmentOSD: OSDChannelSegment<OSDTimeSeries> =
            channelSegmentOSDMap[Object.keys(channelSegmentOSDMap)[0]];
            let channelSegment: ChannelSegment<TimeSeries> | undefined;
            if (channelSegmentOSD && !isEmpty(channelSegmentOSD)) {
                // Push the returned channel segment into the cache
                channelSegment = convertChannelSegmentFromOSDToAPI(channelSegmentOSD);
                this.addOrUpdateToCache(channelSegment);
                return channelSegment;
            }
        } else {
            // tslint:disable-next-line:max-line-length
            logger.debug('Could not retrieve channel segment with id %s from OSD', channelSegmentId);
            return undefined;
        }
    }

    /**
     * Retrieve all channel segments for each given channel id that are within the requested time range.
     * If the optional type of the channel segments is passed in the cache will not be checked 
     * for other types, so leave it blank if uncertain.
     * 
     * @param timeRange the time range the channel segments should be in
     * @param channelIds list of channel ids to request channel segments for
     * @param type optional type of the channel segment for quicker cache lookup
     */
    public async getChannelSegmentsByChannels(
        startTime: number,
        endTime: number,
        channelIds: string[],
        type: ChannelSegmentType
    ): Promise<ChannelSegment<TimeSeries>[]> {
        // Retrieve the request configuration for the service call
        const requestConfig = this.settings.backend.services.channelSegments.requestConfig;

        // Check cache for channel segments
        const cacheResults: CacheResults
            = this.checkCacheByChannels(startTime, endTime, channelIds, type);

        // tslint:disable-next-line:max-line-length
        logger.debug(`Channel segment cache results(${channelIds.length}): hits=${cacheResults.hits.length} misses=${cacheResults.misses.length}`);
        if (cacheResults.misses.length === 0) {
            return cacheResults.hits;
        }

        // Cached segments not all fulfilled, query OSD
        const query: ChannelSegmentInput = {
            'channel-ids': cacheResults.misses,
            'start-time': toOSDTime(startTime),
            'end-time': toOSDTime(endTime),
            'with-waveforms': true
        };
        // tslint:disable-next-line:max-line-length
        logger.debug(`Sending service request: ${JSON.stringify(requestConfig, undefined, 2)} query: ${JSON.stringify(query, undefined, 2)}`);
        return this.httpWrapper.request(requestConfig, query)
            .then<ChannelSegment<TimeSeries>[]>(async (responseData: any) => {

                // Cache filters after request
                const channelSegmentsToReturn: ChannelSegment<TimeSeries>[] = cacheResults.hits;
                if (responseData) {
                    for (const key in responseData) {
                        if (!responseData.hasOwnProperty(key)) {
                            continue;
                        }
                        // Convert OSD channel segment to API
                        const channelSegmentOSD: OSDChannelSegment<OSDTimeSeries> = responseData[key];
                        const channelSegment: ChannelSegment<TimeSeries>
                            = convertChannelSegmentFromOSDToAPI(channelSegmentOSD);

                        // Sometimes the OSD sends back undefined or NaN, so we hack fix
                        channelSegment.timeseries = channelSegment.timeseries
                            .map((timeseries: Waveform) =>
                                ({ // Dangerous cast, might explode
                                    ...timeseries,
                                    values: timeseries.values.map(value => value !== undefined ? value : 0),
                                }));
                        // Add to processor cache
                        this.addOrUpdateToCache(channelSegment);

                        // If it match type requested completed translation of channel segment to return
                        if (type === channelSegment.type) {
                            channelSegmentsToReturn.push(channelSegment);
                        }
                    }
                }
                return channelSegmentsToReturn;
            })
            .catch(error => {
                logger.error(`Failed to request/fetch channel segments: ${error}`);
                return [];
            });
        }

    /**
     * Add to cache the new FKPowerSpectra
     * @param newFkPowerSpectra 
     */
    public updateFkDataById(newFkPowerSpectra: ChannelSegment<FkPowerSpectra>) {
        if (newFkPowerSpectra && isFkSpectraChannelSegment(newFkPowerSpectra)) {
            this.addOrUpdateToCache(newFkPowerSpectra);
        }
    }

    /**
     * Updae each FK power spectra channel segments with reviewed status
     * @param fksReviewed
     * @returns List of Signal Detections successfully updated
     */
    public updateFkReviewedStatuses(markFksReviewedInput: MarkFksReviewedInput, reviewed: boolean): SignalDetection[] {
        // Go thru each input
        const updatedSignalDetections: SignalDetection[] = markFksReviewedInput.signalDetectionIds.map(sdId => {
            // Lookup the Azimuth feature measurement and get the fkDataId (channel segment id)
            const signalDetection = signalDetectionProcessor.getSignalDetectionById(sdId);
            // Lookup the Channel Segment FK Power Spectra from the Az or Slow feature measurement
            const azimuthMeasurement =
                findAzimthFeatureMeasurement(signalDetection.currentHypothesis.featureMeasurements);
            if (!azimuthMeasurement || !azimuthMeasurement.channelSegmentId) {
                logger.warn(`Failed to Update an Fk Azimuth Feature Measurement is not defined.`);
                return undefined;
            }
            const azimuthChannelSegment = this.getInCacheChannelSegmentById(azimuthMeasurement.channelSegmentId);
            if (azimuthChannelSegment && isFkSpectraChannelSegment(azimuthChannelSegment)) {
                azimuthChannelSegment.timeseries[0].reviewed = reviewed;
            } else {
                logger.warn(`Failed to Update Fk associated channel segment is not defined.`);
                return undefined;
            }
            return signalDetection;
        })
        .filter(sd => sd !== undefined);
        return updatedSignalDetections;
    }

    /**
     * Adds or updates the cache for the given channel segment.
     * 
     * @param channelSegment the channel segments to add to the cache
     */
    public addOrUpdateToCache(channelSegment: ChannelSegment<TimeSeries>) {
        if (channelSegment && channelSegment.id) {
            this.cache.set(channelSegment.id, channelSegment);
        }
    }

    /**
     * Checks the cache for a channel segment matching the channel segment id. 
     * 
     * @param channelSegmentId the channel segment id to check cache for
     */
    public getInCacheChannelSegmentById(channelSegmentId: string): ChannelSegment<TimeSeries> | undefined {
        return this.cache.get(channelSegmentId);
    }

    /**
     * Build a new FkData object, using the input parameters
     * @param input parameters
     * @returns SignalDetection that was modified
     */
    public async computeFk(input: FkInput): Promise<SignalDetection> {
        // Lookup the Azimuth feature measurement and get the fkDataId (channel segment id)
        const sd = signalDetectionProcessor.getSignalDetectionById(input.signalDetectionId);

        // Creates the input argument for computeFk arg false means sample rate won't be for the Thumbnail Fks
        const computeFkInput: ComputeFkInput = this.createComputeFkInput(input, sd, false);

        // Call the computeFk endpoint
        const fkChannelSegment = await this.callComputeFk(computeFkInput, input, sd);

        // Update the Az and Slow FM channel segment id with new Channel Segment
        // Lookup the Channel Segment FK Power Spectra from the Az or Slow feature measurement
        const azimuthMeasurement = findAzimthFeatureMeasurement(sd.currentHypothesis.featureMeasurements);
        if (!azimuthMeasurement || !azimuthMeasurement.channelSegmentId) {
            logger.warn(`Failed to Create or Update an Fk either the Arrival Time or " +
                "Azimuth Feature Measurements is not defined.`);
            return undefined;
        }
        azimuthMeasurement.channelSegmentId = fkChannelSegment.id;
        const slownessMeasurement =
            findSlownessFeatureMeasurement(sd.currentHypothesis.featureMeasurements);
        if (slownessMeasurement) {
            slownessMeasurement.channelSegmentId = fkChannelSegment.id;
        }
        // Before returning Fk compute a new Fk beam
        await this.computeBeam(input, sd);

        // Add it to the cache for claim check
        this.addOrUpdateToCache(fkChannelSegment);

        // TODO: Remove when done with debug
        // Print comparisons where the fkSpectrums have the same FstatGrid sequentially
        // compareFkSpectrumGrids(fkChannelSegment.timeseries[0].spectrums);
        // Return the Signal Detection with new Fk
        return sd;
    }

    /**
     * TODO: Should we move to Signal Detection Processor
     * Compute a new Beam part of continuous FK
     * @param detectionId 
     * @returns AssociationChange populated with updated SignalDetection
     */
    public async computeBeam(input: FkInput, signalDetection: SignalDetection): Promise<AssociationChange> {
        const associationChange: AssociationChange = {
            events: [],
            sds: []
        };

        // Push the SD that will be updated with new beam
        if (!signalDetection) {
            logger.warn(`Failed to find Signal Detection: ${input.signalDetectionId} no new beam could be calculated!`);
            return associationChange;
        }
        associationChange.sds.push(signalDetection);
        const requestConfig = this.settings.backend.services.computeBeam.requestConfig;

        // Call method to build the Beam Input parameters
        const beamInput = await this.buildBeamInput(signalDetection);

        // Check beam input was create
        if (!beamInput || !signalDetection) {
            return associationChange;
        }

        // await eventProcessor.getEventsInTimeRange(timeRange);
        performanceLogger.performance('beamChannelSegment', 'requestedFromService', beamInput.outputChannelId);
        const channelSegmentsOSDs = await this.httpWrapper.request(requestConfig, beamInput);
        performanceLogger.performance('beamChannelSegment', 'returnedFromService', beamInput.outputChannelId);
        // See if we get a legit response
        if (channelSegmentsOSDs && channelSegmentsOSDs.length > 0) {
            // Convert and push the returned channel segment into the cache
            const newBeamChannelSegment = convertChannelSegmentFromOSDToAPI(channelSegmentsOSDs[0]);
            // Add it to the Channel Segment cache
            this.addOrUpdateToCache(newBeamChannelSegment);
            const arrivalTimeFM =
                findArrivalTimeFeatureMeasurement(signalDetection.currentHypothesis.featureMeasurements);
            // Replace the arrival time channel segment with new one
            arrivalTimeFM.channelSegmentId = newBeamChannelSegment.id;

            // Recalculate the filtered beams using the newly computed beam
            await signalDetectionProcessor.populateFilterBeams(arrivalTimeFM);
        } else {
            logger.warn(`Compute Beam service call returned undefined result. No new beam was computed!`);
        }
        return associationChange;
    }

    /**
     * Build the Beam Input parameters for computeBeam streaming call
     * @param sd SignalDetection
     * @returns BeamFormInput populated based on the Signal Detection
     */
    public async buildBeamInput(sd: SignalDetection): Promise<BeamFormingInput> {
        // TODO: Get rid of ConfigUI beam input when know how to populate
        // Get the default configured BeamFormingInput from ConfigUi.json
        // Get the Locate Event Parameters used by locateEvent call (Event Definition Type)
        const beamInput: BeamFormingInput = configProcessor.getConfigByKey('computeBeamArg');
        beamInput.outputChannelId = sd.stationId;
        const beamDef = beamInput.beamDefinition;

        // Get Peak Spectrum to get Az/Slow values
        // TODO: Maybe should look at both Az CS and Slow CS for FkPowerSpectra Channel Segment
        const arrivalFMV = findArrivalTimeFeatureMeasurementValue(sd.currentHypothesis.featureMeasurements);
        const azFM = findAzimthFeatureMeasurement(sd.currentHypothesis.featureMeasurements);
        const azFkSpectraCS = await this.getChannelSegment(azFM.channelSegmentId, sd);

        const azFkSpectra = azFkSpectraCS && isFkSpectraChannelSegment(azFkSpectraCS) && azFkSpectraCS.timeseries &&
            azFkSpectraCS.timeseries.length > 0 ? azFkSpectraCS.timeseries[0] : undefined;
        if (azFkSpectra) {
            const degreeToKmFactor = 111;
            beamDef.azimuth = azFkSpectra.leadSpectrum.attributes.azimuth;
            beamDef.slowness = (1 / azFkSpectra.leadSpectrum.attributes.slowness) * degreeToKmFactor;
            beamDef.slowness = 1 / beamDef.slowness;
        }
        const phaseFMV = findPhaseFeatureMeasurementValue(sd.currentHypothesis.featureMeasurements);
        if (phaseFMV && phaseFMV.phase) beamDef.phaseType = phaseFMV.phase;

        // Beam Location is the Station Location with channel positions relative to it
        const station = stationProcessor.getStationById(sd.stationId);
        beamDef.beamPoint = station.location;

        // Waveforms for each channel
        let waveforms: ChannelSegment<Waveform>[] = [];
        // Build the relative positions of the channels
        const channels = stationProcessor.getChannelsByStation(sd.stationId);
        if (channels && channels.length > 0) {
            // Set the Waveform Sample Rate based on first channel
            // TODO: figure out if they are all the same as the first and if this is right
            beamDef.nominalWaveformSampleRate = channels[0].sampleRate;

            // Using the arrival time, set the start and end time of the waveform to retreive
            const leadBeamSeconds: number = configProcessor.getConfigByKey('leadBeamSeconds');
            const lagBeamSeconds: number = configProcessor.getConfigByKey('lagBeamSeconds');
            const startTime = arrivalFMV.value - leadBeamSeconds;
            const endTime = arrivalFMV.value + lagBeamSeconds;

            // Also populate waveforms for all the Station channels
            const channelIds = channels.map(chan => chan.id);
            waveforms = await WaveformProcessor.Instance().
                getRawWaveformSegmentsByChannels(startTime, endTime, channelIds);
            // Trucate them down to only be in the amount requested
            beamInput.waveforms = waveforms.map(wf =>
                convertChannelSegmentFromAPIToOSD(truncateChannelSegmentTimeseries(wf, startTime, endTime)));

            const map = new Map();
            channels.forEach(channel => map.set(channel.id, channel.position));

            // Now create the object for Relative Positions
            // If the channel has no corresponding waveform then omit it
            const relativePos = {};
            for (const entry of map.entries()) {
                if (waveforms.find(wf => wf.channelId === entry[0]) !== undefined) {
                    relativePos[entry[0]] = entry[1];
                }
            }
            beamDef.relativePositionsByChannelId = relativePos;
        }

        // Check if waveform were found as part of the input to compute beam
        if (!waveforms || waveforms.length === 0) {
            logger.warn('No waveforms were found to compute beeam a new beam will not be computed.');
            return undefined;
        }
        return beamInput;
    }

    /**
     * Compute a list of Fk Thumbnails for the new Fk
     *
     * @returns List of FkFrequencyThumbnails
     */
    public async computeFkFrequencyThumbnails(input: FkInput): Promise<FkFrequencyThumbnailBySDId> {
        const frequencyBands: FrequencyBand[] = configProcessor.getConfigByKey('defaultFrequencyBands');

        const sd = signalDetectionProcessor.getSignalDetectionById(input.signalDetectionId);
        // Creates the input argument for computeFk value true means the step size
        // is set to only compute one Spectrum for the thumbnail to display
        const computeFkInput: ComputeFkInput = this.createComputeFkInput(input, sd, true);
        const promises = frequencyBands.map(async fb => {
            computeFkInput.highFrequency = fb.maxFrequencyHz;
            computeFkInput.lowFrequency = fb.minFrequencyHz;

            const fkCS = await this.callComputeFk(computeFkInput, input, sd);
            const thumbnail: FkFrequencyThumbnail = {
                frequencyBand: fb,
                fkSpectra: fkCS.timeseries[0]
            };
            return thumbnail;
        });
        const thumbnails =  await Promise.all(promises);
        return {
            signalDetectionId: sd.id,
            fkFrequencyThumbnails: thumbnails
        };
    }

    /**
     * Helper function that builds the ComputeFk Input object. Shared by computeFk and computeFkFrequencyThumbnails
     * @param input FkInput sent by UI
     * @param areThumbnails (Modifies sample rate so Thumbnails only returns one spectrum in fk)
     * @returns 
     */
    private createComputeFkInput(input: FkInput, sd: SignalDetection, areThumbnails: boolean): ComputeFkInput {
        // Get arrivalTime segment to figure out length in secs
        // TODO: Add null pointer exception guards
        // Lookup the Azimuth feature measurement and get the fkDataId (channel segment id)
        const arrivalFM = findArrivalTimeFeatureMeasurement(sd.currentHypothesis.featureMeasurements);
        const arrivalFMV = (arrivalFM.measurementValue as InstantMeasurementValue).value;
        const arrivalSegment = this.getInCacheChannelSegmentById(arrivalFM.channelSegmentId);

        // TODO we are only allowing SHZ contrib channels to be sent to FK service. This seems wrong
        const shz = input.configuration.contributingChannelsConfiguration
            .filter(ccc => ccc.name.includes('SHZ') && ccc.enabled);
        const shzIds = shz.map(cet => cet.id);
        // TODO: Replace with real conversion
        // tslint:disable-next-line: no-magic-numbers
        const maximumSlownessInSPerKm = kmToDegreesApproximate(input.configuration.maximumSlowness);
        const offsetStartTime = calculateStartTimeForFk(arrivalSegment.startTime, arrivalFMV,
                                                        input.windowParams.leadSeconds, input.windowParams.stepSize);
        // const offsetStartTime = arrivalFMV - input.windowParams.leadSeconds;
        // Sample rate inverse of stepsize. If thumbnail set rate so we only get one spectrum back from service
        const sampleRate = areThumbnails ? 1 / (arrivalSegment.endTime - offsetStartTime) :
            1 / input.windowParams.stepSize;

        // const endTime = arrivalSegment.startTime + (arrivalSegment.timeseries[0].sampleCount / sampleRate);
        // Compute sample count if thumbnail only want one spectrum
        const timeSpanAvailable = arrivalSegment.endTime - arrivalSegment.startTime;
        const sampleCount = areThumbnails ? 1 : Math.floor(timeSpanAvailable / input.windowParams.stepSize);
        return {

            startTime: toOSDTime(offsetStartTime),
            sampleRate,
            channelIds: shzIds,
            windowLead: setDurationTime(input.windowParams.leadSeconds),
            windowLength: setDurationTime(input.windowParams.lengthSeconds),
            lowFrequency: input.frequencyBand.minFrequencyHz,
            highFrequency: input.frequencyBand.maxFrequencyHz,
            useChannelVerticalOffset: input.configuration.useChannelVerticalOffset,
            phaseType: input.phase,
            normalizeWaveforms: input.configuration.normalizeWaveforms,
            outputChannelId: input.stationId,
            slowCountX: Math.floor(input.configuration.numberOfPoints),
            slowCountY: Math.floor(input.configuration.numberOfPoints),
            slowStartX: -maximumSlownessInSPerKm,
            slowStartY: -maximumSlownessInSPerKm,
            slowDeltaX: (maximumSlownessInSPerKm * 2 / input.configuration.numberOfPoints),
            slowDeltaY: (maximumSlownessInSPerKm * 2 / input.configuration.numberOfPoints),
            sampleCount
        };
    }
    /**
     * Call compute Fk endpoint service with Compute Fk Input
     * @param computeFkInput 
     * @param fkConfigInput 
     * @param sd 
     * @param shzIds 
     * @returns FkPowerSpectra from service call
     */
    private async callComputeFk(inputToService: ComputeFkInput, inputFromClient: FkInput,
                                sd: SignalDetection): Promise<ChannelSegment<FkPowerSpectra>> {
        const requestConfig = this.settings.backend.services.computeFk.requestConfig;
        const query = {
            ...inputToService
        };

        // tslint:disable-next-line:max-line-length
        logger.debug(`Sending service request: ${JSON.stringify(requestConfig, undefined, 2)} query: ${JSON.stringify(query, undefined, 2)}`);
        performanceLogger.performance('fkChannelSegment', 'requestedFromService', inputToService.startTime);
        const fkOSD = await this.httpWrapper.request(requestConfig, query) as OSDChannelSegment<FkPowerSpectraOSD>;
        performanceLogger.performance('fkChannelSegment', 'returnedFromService', inputToService.startTime);
        if (isEmptyReturnFromFkService(fkOSD)) {
            logger.error(`Compute FK: Failed to compute FK for signal detection id: ${sd.id}`);
            return;
        }
        const fkChannelSegment = convertOSDFk(fkOSD[0], sd);
        // if (nansInFkGrid(fkChannelSegment)) {
        //     logger.error(`Compute FK: NaN values in fk for signal detection id: ${sd.id}`);
        //     return;
        // }
        fkChannelSegment.timeseries[0].reviewed = false;
        fkChannelSegment.timeseries[0].phaseType = PhaseType[inputFromClient.phase];
        fkChannelSegment.timeseries[0].lowFrequency = inputFromClient.frequencyBand.minFrequencyHz;
        fkChannelSegment.timeseries[0].highFrequency = inputFromClient.frequencyBand.maxFrequencyHz;
        fkChannelSegment.timeseries[0].windowLead = inputFromClient.windowParams.leadSeconds;
        fkChannelSegment.timeseries[0].windowLength = inputFromClient.windowParams.lengthSeconds;
        fkChannelSegment.timeseries[0].stepSize = inputFromClient.windowParams.stepSize;

        fkChannelSegment.timeseries[0].xSlowCount = inputFromClient.configuration.numberOfPoints;
        fkChannelSegment.timeseries[0].ySlowCount = inputFromClient.configuration.numberOfPoints;
        fkChannelSegment.timeseries[0].xSlowDelta =
            inputFromClient.configuration.maximumSlowness * 2 / inputFromClient.configuration.numberOfPoints;
        fkChannelSegment.timeseries[0].ySlowDelta =
            inputFromClient.configuration.maximumSlowness * 2 / inputFromClient.configuration.numberOfPoints;
        fkChannelSegment.timeseries[0].xSlowStart = -inputFromClient.configuration.maximumSlowness;
        fkChannelSegment.timeseries[0].ySlowStart = -inputFromClient.configuration.maximumSlowness;
        const shz = inputFromClient.configuration.contributingChannelsConfiguration
            .filter(ccc => ccc.name.includes('SHZ') && ccc.enabled);
        const shzIds = shz.map(cet => cet.id);

        const newConfig: FkConfiguration = {
            contributingChannelsConfiguration:
            inputFromClient.configuration.contributingChannelsConfiguration.map(ccc => ({
                id: ccc.id,
                name: ccc.name,
                enabled: ccc.enabled && (shzIds.indexOf(ccc.id) > -1)
            })),
            leadFkSpectrumSeconds: fkChannelSegment.timeseries[0].windowLead,
            maximumSlowness: inputFromClient.configuration.maximumSlowness,
            mediumVelocity: inputFromClient.configuration.mediumVelocity,
            normalizeWaveforms: inputFromClient.configuration.normalizeWaveforms,
            numberOfPoints: inputFromClient.configuration.numberOfPoints,
            useChannelVerticalOffset: inputFromClient.configuration.useChannelVerticalOffset
        };
        fkChannelSegment.timeseries[0].configuration = newConfig;
        return fkChannelSegment;
    }

    /**
     * Initialize the channel segment processor, setting up the mock backend if enabled in configuration.
     */
    private initialize(): void {
        logger.info('Initializing the ChannelSegment processor - Mock Enable: %s', this.settings.backend.mock.enable);

        const mockWrapper = this.httpWrapper.createHttpMockWrapper();

        // If service mocking is enabled, initialize the mock backend
        const backendConfig = config.get('channel-segment.backend');

        if (backendConfig.mock.enable) {
            channelSegmentMockBackend.initialize(mockWrapper);
        }

        // TODO: this is the wrong place to mock waveforms
        waveformMockBackend.initialize(mockWrapper);
    }

    /**
     * Checks the cache for channel segments that have the given channel id and
     * the requested time range in their segment. 
     * See isHit(...) for definition of a hit. 
     * 
     * @param timeRange the time range the channel segments should be in
     * @param channelIds list of channel ids to request channel segments for
     * @param type optional type of the channel segment for quicker cache lookup
     */
    private checkCacheByChannels(
        startTime: number,
        endTime: number,
        channelIds: string[],
        type: ChannelSegmentType
    ): CacheResults {
        const channelSegments: ChannelSegment<TimeSeries>[] = [];
        this.cache.forEach((channelSegment, id, map) => {
            if (this.isHit(channelSegment, startTime, endTime, channelIds, type)) {
                channelSegments.push(channelSegment);
            }
        });

        const cachedWindowedChannelSegments = channelSegments.map((channelSegment: ChannelSegment<TimeSeries>) => {
            if (isWaveformChannelSegment(channelSegment)) {
                return truncateChannelSegmentTimeseries(channelSegment, startTime, endTime);
            } else {
                return channelSegment;
            }
        }
        );

        // Compute misses as channels that have no matching channel segments
        return {
            hits: cachedWindowedChannelSegments,
            misses: channelIds.filter((channelId: string) =>
                cachedWindowedChannelSegments.every(
                    channelSegment => channelSegment.channelId !== channelId
                )
            )
        };
    }

    /**
     * Checks if the channel segment is a hit for the requested channel ids and time range.
     * A hit for time range is calculated differently for different ChannelSegmentType's. 
     * For a beam type (FK_BEAM, DETECTION_BEAM) the beam is considered a hit if any part 
     * of the beam is _within_ the time range. For any other type (RAW, ACQUIRED, FILTER)
     * the waveform is considered a hit if the requested time frame is completely within a 
     * single cached waveform.
     * 
     * Beams that are partially in the time range are windowed to be completely in the 
     * time range. The other type may need to be split up when further 
     * ChannelSegmentType's are supported.
     * 
     * Any hit, regardless of type, means the cache sufficed for the requested channel which
     * means that the OSD _will not_ be called for that channel.
     * 
     * Caching strategy does not support partial misses or stitching of any form for the 
     * other type (RAW, ACQUIRED, FILTER) - this may be implemented in the future.
     * 
     * @param channelSegment channel segment to check
     * @param channelIds list of channel ids to check channel segment is in
     * @param timeRange the time range the channel segments should be in
     */
    private isHit(
        channelSegment: ChannelSegment<TimeSeries>,
        startTime: number,
        endTime: number,
        channelIds: string[],
        type: ChannelSegmentType
    ): boolean {
        return startTime >= channelSegment.startTime && endTime <= channelSegment.endTime;
    }
}
// Initialize at startup
ChannelSegmentProcessor.Instance();
