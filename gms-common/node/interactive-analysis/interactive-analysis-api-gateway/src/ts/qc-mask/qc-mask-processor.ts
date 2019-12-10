import * as model from './model';
import * as config from 'config';
import * as qcMaskMockBackend from './qc-mask-mock-backend';
import { TimeRange, CreatorType, CreationInfo } from '../common/model';
import { HttpClientWrapper } from '../util/http-wrapper';
import { gatewayLogger as logger } from '../log/gateway-logger';
import { cloneDeep, findIndex } from 'lodash';
import * as uuid4 from 'uuid/v4';
import { toOSDTime, epochSecondsNow, toEpochSeconds } from '../util/time-utils';

// TODO replace with a more robust caching solution
/**
 * Encaps4.ulates QC mask data cached in memory
 */
interface QcMaskDataCache {
    qcMasks: model.QcMask[];
    creationInfo: CreationInfo[];
}

/**
 * API gateway processor for QC mask data APIs. This class supports:
 * - data fetching & caching from the backend service interfaces
 * - mocking of backend service interfaces based on test configuration
 * - session management
 * - GraphQL query resolution from the user interface client
 */

class QcMaskProcessor {

    /** Local configuration settings */
    private settings: any;

    /** HTTP client wrapper for communicationg with backend services */
    private httpWrapper: HttpClientWrapper;

    /** Local cache of data fetched from the backend */
    private dataCache: QcMaskDataCache = {
        qcMasks: [],
        creationInfo: []
    };

    /**
     * Constructor - initialize the processor, loading settings and initializing
     * the HTTP client wrapper.
     */
    public constructor() {

        // Load configuration settings
        this.settings = config.get('qcMask');

        // Initialize an http client
        this.httpWrapper = new HttpClientWrapper();
    }

    /**
     * Initialize the QC mask processor, setting up a mock backend if configured to do so.
     */
    public initialize(): void {

        logger.info('Initializing the QcMask processor - Mock Enable: %s', this.settings.backend.mock.enable);

        // If service mocking is enabled, initialize the mock backend
        if (this.settings.backend.mock.enable) {
            qcMaskMockBackend.initialize(this.httpWrapper.createHttpMockWrapper());
        }
    }

    /**
     * Retrieve QC Masks from the cache, filtering the results
     * down to those masks overlapping the input time range and matching an entry in
     * the input list of channel IDs.
     * 
     * @param timeRange The time range in which to retreive QC masks 
     * @param channelIds The list of channel IDs for which to retrieve QC masks
     */
    public async getQcMasks(timeRange: TimeRange, channelIds: string[]): Promise<model.QcMask[]> {

        // Handle undefined input time range
        if (!timeRange) {
            throw new Error('Unable to retrieve cached QC masks for undefined time range');
        }

        // Handle undefined input channel ID list
        if (!channelIds || channelIds.length === 0) {
            throw new Error('Unable to retrieve cached QC masks for undefined channel ID list');
        }

        logger.debug(`Get QC Masks request for time range: ${JSON.stringify(timeRange)} and channels: ${channelIds}`);

        // Retrieve the request configuration for the service call
        const requestConfig = this.settings.backend.services.masksByChannelIds.requestConfig;
        const qcMasks = [];

        // First call for cached masks then execute OSD queries for each channel id not found
        const filteredChannels = this.findCachedMasks(qcMasks, channelIds, timeRange);
        if (filteredChannels.length === 0) {
            if (qcMasks.length > 0) {
                logger.debug(`Returning qcMasks from Cache size ${qcMasks.length}`);
            }
            return qcMasks;
        }

        const query = {
            'channel-ids': channelIds,
            'start-time': toOSDTime(timeRange.startTime),
            'end-time': toOSDTime(timeRange.endTime),
        };
        logger.debug(`Calling get QC Masks query: ${JSON.stringify(query)} request: ${JSON.stringify(requestConfig)}`);
        const responseData = await this.httpWrapper.request(requestConfig, query);
        if (responseData) {
            for (const key in responseData) {
                if (!responseData.hasOwnProperty(key)) {
                    continue;
                }
                const masks: model.QcMask[] = responseData[key];
                masks.forEach(mask => {

                    // walk thru the mask to lookup the CreationInfo for each mask version
                    mask.qcMaskVersions.forEach(maskVersion => {
                        // Add CreationInfo to datacache if not already present
                        const creationInfoId = maskVersion.creationInfoId ?
                                               maskVersion.creationInfoId : uuid4().toString();
                        const creationInfo = this.dataCache.creationInfo.find(ci => ci.id ===
                            creationInfoId);
                        if (!creationInfo) {
                            const newCreationInfo = this.createCreationInfo(creationInfoId);
                            maskVersion.creationInfoId = newCreationInfo.id;
                            this.dataCache.creationInfo.push(newCreationInfo);
                        }
                    });

                    // FIXME Add new mask to cache for now PI 4 need to rework how all this works
                    // If not in dataCache list add it
                    const index = findIndex(this.dataCache.qcMasks, qcM => qcM.id === mask.id);
                    if (index >= 0) {
                        this.dataCache.qcMasks[index] = mask;
                    } else {
                        this.dataCache.qcMasks.push(mask);
                    }
                    // Add mask to list
                    qcMasks.push(mask);
                });
            }
        }
        logger.debug(`Returning qcMasks size ${qcMasks.length}`);
        return qcMasks;
    }

    /**
     * Creats QC Masks and adds them to the cache and OSD (when not mocked)
     * 
     * @param channelIds 
     * @param input 
     */
    public async createQcMasks(channelIds: string[], input: model.QcMaskInput): Promise<model.QcMask[]> {
        // Handle undefined channelIds
        if (!channelIds) {
            throw new Error('Unable to create QcMask with undefined channel ID list');
        }

        // Handle undefined input
        if (!input) {
            throw new Error('Unable to create QcMask with undefined input');
        }
        const qcMasks: model.QcMask[] = [];
        channelIds.forEach(channelId => {
            const creationInfo = this.createCreationInfo(uuid4().toString());
            this.dataCache.creationInfo.push(creationInfo);
            const qcMaskVersion: model.QcMaskVersion = {
                category: input.category,
                channelSegmentIds: [channelId],
                creationInfoId: creationInfo.id,
                startTime: toOSDTime(input.timeRange.startTime),
                endTime: toOSDTime(input.timeRange.endTime),
                parentQcMasks: [],
                rationale: input.rationale,
                type: input.type,
                version: 0,
            };
            const newMask: model.QcMask = {
                id: uuid4().toString(),
                channelId,
                qcMaskVersions: [qcMaskVersion]
            };
            // Add the mask to the collection to return in addition to the cache
            qcMasks.push(newMask);
            this.dataCache.qcMasks.push(newMask);
        });

        // Save newly created masks
        if ((await this.saveQCMasks(qcMasks))) {
            return qcMasks;
        } else {
            throw new Error('Save to OSD failed: Mask not created');
        }
    }

    /**
     * Updates the QC Mask specified by the id parameter
     * 
     * @param qcMaskId Id to update
     * @param input updated parameters
     */
    public async updateQcMask(qcMaskId: string, input: model.QcMaskInput): Promise<model.QcMask> {
        // Handle undefined mask id
        if (!qcMaskId) {
            throw new Error('Unable to update QcMask with undefined mask ID');
        }

        // Handle undefined input
        if (!input) {
            throw new Error('Unable to update QcMask with undefined input');
        }
        // Find the Mask
        const maskToUpdate: model.QcMask = this.dataCache.qcMasks.find(mask => mask.id === qcMaskId);

        // If no mask was found, throw an error
        if (!maskToUpdate) {
            throw new Error('Mask not Found');
        }

        const currentMaskVersion = maskToUpdate.qcMaskVersions[maskToUpdate.qcMaskVersions.length - 1];
        const nextMaskVersion = currentMaskVersion.version + 1;
        const creationInfo = this.createCreationInfo(uuid4().toString());
        this.dataCache.creationInfo.push(creationInfo);
        // Create a new version of the mask
        const newMaskVersion: model.QcMaskVersion = {
            ...currentMaskVersion,
            category: input.category,
            creationInfoId: creationInfo.id,
            startTime: toOSDTime(input.timeRange.startTime),
            endTime: toOSDTime(input.timeRange.endTime),
            parentQcMasks: [],
            rationale: input.rationale,
            type: input.type,
            version: nextMaskVersion,
        };

        maskToUpdate.qcMaskVersions.push(newMaskVersion);

        // Save updated masks
        if ((await this.saveQCMasks([maskToUpdate]))) {
            return maskToUpdate;
        } else {
            throw new Error('Save to OSD failed: Mask not updated');
        }
    }

    /**
     * Rejects the specified mask for the specified reason
     * 
     * @param qcMaskId Id to reject
     * @param rationale reason for the rejection
     */
    public async rejectQcMask(qcMaskId: string, rationale: string): Promise<model.QcMask> {
        // Handle undefined mask id
        if (!qcMaskId) {
            throw new Error('Unable to reject QcMask with undefined mask ID');
        }

        // Find the Mask
        const maskToUpdate: model.QcMask = this.dataCache.qcMasks.find(mask => mask.id === qcMaskId);

        // If no mask was found, throw an error
        if (!maskToUpdate) {
            throw new Error('Mask not Found');
        }

        const currentMaskVersion = maskToUpdate.qcMaskVersions[maskToUpdate.qcMaskVersions.length - 1];
        const nextMaskVersion = currentMaskVersion.version + 1;
        const creationInfo = this.createCreationInfo(uuid4().toString());
        this.dataCache.creationInfo.push(creationInfo);
        // Create a new version of the mask
        const newMaskVersion: model.QcMaskVersion = {
            ...currentMaskVersion,
            category: model.QcMaskCategory.Rejected,
            creationInfoId: creationInfo.id,
            rationale,
            version: nextMaskVersion,
        };

        const osdMaskToReject = cloneDeep(maskToUpdate);
        const osdMaskToRejectNewVersion = cloneDeep(newMaskVersion);

        delete osdMaskToRejectNewVersion.type;
        delete osdMaskToRejectNewVersion.startTime;
        delete osdMaskToRejectNewVersion.endTime;

        maskToUpdate.qcMaskVersions.push(newMaskVersion);
        osdMaskToReject.qcMaskVersions.push(osdMaskToRejectNewVersion);

        // Save rejected mask
        if ((await this.saveQCMasks([osdMaskToReject]))) {
            return maskToUpdate;
        } else {
            throw new Error('Save to OSD failed: Mask not rejected');
        }
    }

    /**
     * Get creation info by ID
     * @param creationInfoId creation info ID
     * @returns a creation info
     */
    public getCreationInfo(creationInfoId: string): CreationInfo {
        return this.dataCache.creationInfo.find(ci => ci.id === creationInfoId);
    }

    /**
     * Creates a creation info
     * @param creationInfoId creation info ID 
     * @returns a Creation info
     */
    private createCreationInfo(creationInfoId: string): CreationInfo {
        return {
            id: creationInfoId,
            creationTime: epochSecondsNow(),
            creatorId: 'Spencer',
            creatorType: CreatorType.Analyst,
            creatorName: 'Spencer'
        };
    }

    /**
     * Search the data cache to see if we already of the QcMask cached
     * 
     * @param qcMask is empty on entry to be filled by method
     * @param channelIds list of channels to search
     * @returns list of channel ids we did not find masks for (this list will be list to OSD)
     */
    private findCachedMasks = (qcMasks: model.QcMask[], channelIds: string[], timeRange: TimeRange): string[] => {
        const osdChannelIds = [];
        if (this.dataCache.qcMasks.length === 0) {
            return channelIds;
        }

        // Walk thru each channel and find masks for the time range we are interested in
        channelIds.forEach(chanId => {
            let foundOne = false;
            this.dataCache.qcMasks.forEach(qcM => {
                const currentMaskVersion = qcM.qcMaskVersions[qcM.qcMaskVersions.length - 1];
                if (qcM.channelId === chanId &&
                    toEpochSeconds(currentMaskVersion.startTime) >= timeRange.startTime &&
                    toEpochSeconds(currentMaskVersion.endTime) <= timeRange.endTime) {
                    qcMasks.push(qcM);
                    foundOne = true;
                }
            });

            // If found mask add it to list else add the chanId to OSD channel id query list
            if (!foundOne) {
                osdChannelIds.push(chanId);
            }
        });
        return osdChannelIds;
    }

    /**
     * Function to save the list of QcMasks in the OSD
     */
    private async saveQCMasks(qcMasks: model.QcMask[]) {
        logger.info(`Save QC Masks request qc mask list size of:${qcMasks.length}`);

        // Create a deep copy and remove the currentVersion (UI construct) before saving
        const masksToSave: model.QcMask[] = cloneDeep(qcMasks);
        let status = true;
        // Retrieve the request configuration for the service call
        const requestConfig = this.settings.backend.services.saveMasks.requestConfig;
        logger.debug(`Calling QC Masks save for: ${JSON.stringify(qcMasks)} request: ${JSON.stringify(requestConfig)}`);
        await this.httpWrapper.request(requestConfig, masksToSave)
            .catch(error => status = false);
        return status;
    }
}

/**
 *  Export an initialized instance of the processor
 */
export const qcMaskProcessor: QcMaskProcessor = new QcMaskProcessor();
qcMaskProcessor.initialize();
