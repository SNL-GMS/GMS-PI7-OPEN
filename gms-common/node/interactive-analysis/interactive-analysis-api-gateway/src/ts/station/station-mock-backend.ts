import { uniq } from 'lodash';
import * as path from 'path';
import * as config from 'config';
import { readJsonData, resolveTestDataPaths } from '../util/file-parse';
import { gatewayLogger as logger } from '../log/gateway-logger';
import { HttpMockWrapper } from '../util/http-wrapper';
import * as STDSModel from './stds-model';
import { OSDNetwork, OSDStation, OSDChannel } from './model';
import { epochSecondsNow, toEpochSeconds } from '../util/time-utils';

/**
 * Mock backend HTTP services providing access to processing station data. If mock services are enabled in the
 * configuration file, this module loads a test data set specified in the configuration file and configures
 * mock HTTP interfaces for the API gateway backend service calls.
 */

/**
 * Encapsulates the query parameters used to retrieve a network by name.
 */
interface NetworkByNameQuery {
    name: string;
    time: string;
}

/**
 * Used to read in Data from membership
 */
interface ParentToChildObject {
    parentId: string;
    object: any;
}

/**
 * Encapsulates backend data supporting retrieval by the API gateway.
 */
interface StationDataStore {
    networkData: STDSModel.Network[];
    networkMembershipData: STDSModel.Membership[];
    stationData: STDSModel.Station[];
    stationMembershipData: STDSModel.Membership[];
    siteData: STDSModel.Site[];
    siteMembershipData: STDSModel.Membership[];
    channelData: STDSModel.Channel[];
    network?: OSDNetwork;
}

// Declare a backend data store for the mock station backend
let dataStore: StationDataStore;

/**
 * Configure mock HTTP interfaces for a simulated set of station-related backend services.
 * @param httpMockWrapper The HTTP mock wrapper used to configure mock backend service interfaces
 */
export function initialize(httpMockWrapper: HttpMockWrapper) {

    logger.info('Initializing mock backend for station data');

    if (!httpMockWrapper) {
        throw new Error('Cannot initialize mock station services with undefined HTTP mock wrapper');
    }

    // Load test data from the configured data set
    dataStore = loadTestData();

    // Load the station backend service config settings
    const backendConfig = config.get('station.backend');

    httpMockWrapper.onMock(backendConfig.services.networkByName.requestConfig.url, getNetworkByName);
    httpMockWrapper.onMock(backendConfig.services.stationsByIds.requestConfig.url, getStationsByIds);
    httpMockWrapper.onMock(backendConfig.services.channelsByIds.requestConfig.url, getChannelsByIds);
}

/**
 * Get stations by IDs
 * @returns a OSD representation station[] 
 */
export function getStationsByIds(): OSDStation[] {
    return dataStore.network.stations;
}

/**
 * Get channels by IDs
 * @returns a OSD representation channels[]
 */
export function getChannelsByIds(): OSDChannel[] {
    return dataStore.network.stations[0].sites[0].channels;
}

/**
 * Retrieve a collection of processing station data valid at the provided effective time,
 * for the provided perocessing network name.
 * @param input The query parameters used to retrieve processing stations by processing
 * network name and effective time.
 * @returns OSDNetwork
 */
export function getNetworkByName(input: NetworkByNameQuery): OSDNetwork {
    // Handle undefined input
    if (!input || !input.name) {
        throw new Error('Unable to retrieve stations for undefined network name');
    }

    // Handle uninitialized data store
    handleUnitializedDataStore();

    // Ignore the effective time parameter in the mock backend. All data are assumed to be
    // valid for the current time

    // Find the network matching the provided name
    // const newwork = dataStore.networkData(newWork => newWork.name === input.name);
    const network: STDSModel.Network = dataStore.networkData.find(nd => nd.name === input.name);

    // If no network match is found, throw an error
    if (!network) {
        throw new Error(`No processing network found matching name: ${input.name}`);
    }

    // Loops through network memberships and stations to find the correct active ones
    const stationMap: Map<string, ParentToChildObject> = new Map();
    filterMembershipDataAndGetEntityData(
        dataStore.networkMembershipData, network, dataStore.stationData, stationMap);

    // Loops through station memberships and sites to find the correct active ones
    const siteMap: Map<string, ParentToChildObject> = new Map();
    [...stationMap.values()].forEach(station => {
        filterMembershipDataAndGetEntityData(
            dataStore.stationMembershipData, station.object, dataStore.siteData, siteMap);
    });

    // Loops through site memberships and channels to find the correct active ones
    const channelMap: Map<string, ParentToChildObject> = new Map();
    [...siteMap.values()].forEach(site => {
        filterMembershipDataAndGetEntityData(
            dataStore.siteMembershipData, site.object, dataStore.channelData, channelMap);
    });

    // create the network from the maps created above
    const networkToReturn: OSDNetwork = {
        id: network.versionId,
        name: network.name,
        organization: network.organization,
        region: network.region,
        stations: [...stationMap.values()].filter(stationObject => stationObject.parentId === network.versionId)
        .map(stationObject => stationObject.object)
            .map(station => ({
                id: station.versionId,
                name: station.name,
                description: station.description,
                stationType: station.stationType,
                latitude: station.latitude,
                longitude: station.longitude,
                elevation: station.elevation,
                sites: [...siteMap.values()].filter(siteObject => siteObject.parentId === station.versionId)
                .map(siteObject => siteObject.object)
                    .map(site => ({
                        id: site.versionId,
                        name: site.name,
                        latitude: site.latitude,
                        longitude: site.longitude,
                        elevation: site.elevation,
                        channels: [...channelMap.values()].filter(chanObject => chanObject.parentId === site.versionId)
                        .map(chanObject => chanObject.object)
                            .map(channel => ({
                                id: channel.versionId,
                                name: channel.name,
                                channelType: channel.type,
                                dataType: channel.dataType,
                                latitude: channel.latitude,
                                longitude: channel.longitude,
                                elevation: channel.elevation,
                                depth: channel.depth,
                                verticalAngle: channel.verticalAngle,
                                horizontalAngle: channel.horizontalAngle,
                                position: channel.position,
                                actualTime: channel.actualTime,
                                systemTime: channel.systemTime,
                                sampleRate: channel.nominalSampleRate
                            })
                        )
                    })
                )
            })
        )
    };

    dataStore.network = networkToReturn;

    return networkToReturn;
}

/**
 * Load test data into the mock backend data store from the configured test data set.
 * @returns StationDataStore
 */
function loadTestData(): StationDataStore {

    const dataPath = resolveTestDataPaths().jsonHome;

    logger.info(`Loading station test data from path: ${dataPath}`);

    const stationReferenceConfig = config.get('testData.standardTestDataSet.stationReference');

    // Read the processing network definitions from the configured test set
    let networkData: STDSModel.Network[] = [];
    let networkMembershipData: any[] = [];
    try {
        networkData =
            readJsonData(dataPath.concat(path.sep).concat(stationReferenceConfig.networkFileName));
        networkMembershipData =
            readJsonData(dataPath.concat(path.sep).concat(stationReferenceConfig.networkMembershipFileName));
        // Set membership Ids to standard parent/child for GetNetworkByName
        networkMembershipData.forEach(membership => {
            membership.parentId = membership.networkId;
            membership.childId = membership.stationId;
        });
    } catch (e) {
        logger.error(`Failed to read network data from files: ` +
            `${stationReferenceConfig.networkFileName} and ${stationReferenceConfig.networkMembershipFileName}`);
    }

    let stationData: STDSModel.Station[] = [];
    let stationMembershipData: any[] = [];
    try {
        // Read the processing station definitions from the configured test set
        stationData =
            readJsonData(dataPath.concat(path.sep).concat(stationReferenceConfig.stationFileName));
        stationMembershipData =
            readJsonData(dataPath.concat(path.sep).concat(stationReferenceConfig.stationMembershipFileName));
        // Set membership Ids to standard parent/child for GetNetworkByName
        stationMembershipData.forEach(membership => {
            membership.parentId = membership.stationId;
            membership.childId = membership.siteId;
        });
    } catch (e) {
        logger.error(`Failed to read station data from files: ` +
            `${stationReferenceConfig.stationFileName} and ${stationReferenceConfig.stationMembershipFileName}`);
    }

    let siteData: STDSModel.Site[] = [];
    let siteMembershipData: any[] = [];
    try {
        // Read the processing site definitions from the configured test set
        siteData =
            readJsonData(dataPath.concat(path.sep).concat(stationReferenceConfig.siteFileName));
        siteMembershipData =
            readJsonData(dataPath.concat(path.sep).concat(stationReferenceConfig.siteMembershipFileName));
        // Set membership Ids to standard parent/child for GetNetworkByName
        siteMembershipData.forEach(membership => {
            membership.parentId = membership.siteId;
            membership.childId = membership.channelId;
        });
    } catch (e) {
        logger.error(`Failed to read site data from files: ` +
            `${stationReferenceConfig.siteFileName} and ${stationReferenceConfig.siteMembershipFileName}`);
    }

    let channelData: any[] = [];
    try {
        // Read the processing channel definitions from the configured test set
        channelData =
            readJsonData(dataPath.concat(path.sep).concat(stationReferenceConfig.channelFileName));
        // Change names of variables to match reusable code used in GetNetworkByName
        channelData.forEach(channel => {
            channel.source = channel.informationSource;
            channel.actualChangeTime = channel.actualTime;
            channel.systemChangeTime = channel.systemTime;
        });
    } catch (e) {
        logger.error(`Failed to read channel data from file: ${stationReferenceConfig.channelFileName}`);
    }

    return {
        networkData,
        networkMembershipData,
        stationData,
        stationMembershipData,
        siteData,
        siteMembershipData,
        channelData
    };
}

/**
 * Handle cases where the data store has not been initialized.
 */
function handleUnitializedDataStore() {
    // If the data store is uninitialized, throw an error
    if (!dataStore) {
        throw new Error('Mock backend station processing data store has not been initialized');
    }
}

/**
 * Repeated code for getting the proper data out of STDS files
 * @param parent parent object (network for stations, station for site, and site for channels)
 * @param entityIds list of ids of child entities for the passed in parent
 * @param entityData JSON data for entities to filter down
 * @param entityMap Map used to save unique proper data for loading
 */
function filterMembershipDataAndGetEntityData(membershipData: any[], parent: any,
                                              entityData: any[], entityMap: Map<any, any>) {
    uniq(membershipData
        .filter(membership => membership.parentId === parent.entityId && membership.status === 'ACTIVE')
        .map(m => m.childId))
   .forEach(entityId => {
        const foundEntities = entityData.filter(entity => entity.entityId === entityId);
        foundEntities.forEach(entity => {
            // Looks for offdate to to determine if this is still active
            // format is "OffDate: 2009-12-09T00:00:00Z, Loaded from CSS file"
            const referenceString = entity.source.reference.split(',');
            const offDate = referenceString && referenceString.length > 0 ?
                referenceString[0].replace('OffDate: ', '').trim() : undefined;
            const offDateSecs = toEpochSeconds(offDate);

            const mappedStation = entityMap.get(entity.entityId);
            if (mappedStation) {
                if (toEpochSeconds(mappedStation.object.actualChangeTime) < toEpochSeconds(entity.actualChangeTime)) {
                    if (offDateSecs > epochSecondsNow()) {
                        entityMap.set(entity.entityId, {parentId: parent.versionId, object: entity});
                    }
                }
            } else {
                if (offDateSecs > epochSecondsNow()) {
                    entityMap.set(entity.entityId, {parentId: parent.versionId, object: entity});
                }
            }
        });
    });
}

/**
 * Return Processing Channel based on its Id
 * @param id referene channel id
 */
export function getChannelById(id: string): STDSModel.Channel {
    return dataStore.channelData.find(chan => chan.versionId === id);
}
