import { find, filter, merge } from 'lodash';
import * as geolib from 'geolib';
import * as model from './model';
import * as config from 'config';
import * as stationMockBackend from './station-mock-backend';
import { Location, DistanceToSource, DistanceSourceType, DistanceUnits } from '../common/model';
import { HttpClientWrapper } from '../util/http-wrapper';
import { gatewayLogger as logger, gatewayLogger } from '../log/gateway-logger';
import { eventProcessor } from '../event/event-processor';
import { Event } from '../event/model';
import { getSecureRandomNumber } from '../util/common-utils';

// TODO replace with a more robust caching solution
/**
 * Encapsulates station-related data cached in memory
 */
interface StationDataCacheEntry {
    networks: model.ProcessingNetwork[];
    stations: model.ProcessingStation[];
    defaultStationInfo: model.DefaultStationInfo[];
    sites: model.ProcessingSite[];
    channels: model.ProcessingChannel[];
}

/**
 * API gateway processor for station-related data APIs. This class supports:
 * - data fetching & caching from the backend service interfaces
 * - mocking of backend service interfaces based on test configuration
 * - session management
 * - GraphQL query resolution from the user interface client
 */
class StationProcessor {

    /** Conversion km to degrees. 1 degree = 6371*pi/180 km = 111.1949266 km. */
    private KM_TO_DEGREES: number = 111.1949266;

    /** The default network that is set and configured in the configuration settings */
    private readonly defaultNetwork: string;

    /** Local configuration settings */
    private settings: any;

    /** HTTP client wrapper for communicationg with backend services */
    private httpWrapper: HttpClientWrapper;

    /** Local cache of data fetched from the backend */
    private stationDataCache: Map<string, StationDataCacheEntry> = new Map();

    /**
     * Constructor - initialize the processor, loading settings and initializing the HTTP client wrapper.
     */
    public constructor() {

        this.defaultNetwork = config.get('defaultNetwork');

        // Load configuration settings
        this.settings = config.get('station');

        // Initialize an http client
        this.httpWrapper = new HttpClientWrapper();
    }

    /**
     * Initialize the station processor, fetching station data from the backend.
     * This function sets up a mock backend if configured to do so.
     */
    public initialize(): void {

        logger.info('Initializing the station processor - Mock Enable: %s', this.settings.backend.mock.enable);

        // If service mocking is enabled, initialize the mock backend
        if (this.settings.backend.mock.enable) {
            stationMockBackend.initialize(this.httpWrapper.createHttpMockWrapper());
        }
        // Cache station-related data needed to support the interactive analysis UI
        this.getDefaultStations().catch(e => gatewayLogger.warn(e));
    }

    /**
     * Retrieve a collection of processing channels for the provided processing
     * station ID. If the provided station ID is undefined or does not match any
     * processing channel entries, this function returns empty list.
     * @param stationId the ID of the processing station to retrieve processing channels for
     * @returns ProcessingChannel[]
     */
    public getChannelsByStation(stationId: string): model.ProcessingChannel[] {
        // Throw and error if uninitialized
        this.handleUninitializedCache();
        // Get the sites associated to the station
        const station = this.getStationById(stationId);
        // If nothing there return empty list
        if (!station || !station.siteIds || station.siteIds.length === 0) {
            return [];
        }

        // Loop through sites adding channels
        let processingChannels: model.ProcessingChannel[] = [];
        const processingChannelLists = station.siteIds.map(siteId => this.getChannelsBySite(siteId));
        processingChannelLists.forEach(channelList => processingChannels = processingChannels.concat(channelList));
        return processingChannels;
    }

    /**
     * Retrieve a collection of processing channels for the provided processing
     * site ID. If the provided site ID is undefined or does not match any
     * processing channel entries, this function returns undefined.
     * @param siteId the ID of the processing name to retrieve processing channels for
     * @param networkName optional will use default network if none is provided
     * @returns ProcessingChannel[]
     */
    public getChannelsBySite(siteId: string, networkName: string = this.defaultNetwork):
        model.ProcessingChannel[] {
        // Throw and error if uninitialized
        this.handleUninitializedCache();
        return filter(this.stationDataCache.get(networkName).channels, { siteId });
    }

    /**
     * Retrieve a processing channel= for the provided site and channel name.
     * If the provided site and channel names do not match any processing
     * channel entries, this function returns undefined.
     * @param siteName the name of the site associated with the processing channel to retrieve
     * @param channelName the name of the processing channel to retrieve
     * @returns ProcessingChannel
     */
    public getChannelBySiteAndChannelName(siteName: string, channelName: string, networkName: string =
        this.defaultNetwork): model.ProcessingChannel {
        // Throw and error if uninitialized
        this.handleUninitializedCache();

        return find(this.stationDataCache.get(networkName).channels, channel =>
            channel.siteName.toLowerCase() === siteName.toLowerCase()
            && channel.name.toLowerCase() === channelName.toLowerCase());
    }

    /**
     * Get channels by Ids
     * @param ids channel ids
     * @returns a processing channel[] as a promise
     */
    public async getChannelsByIds(ids: string[]): Promise<model.ProcessingChannel[]> {
        // Throw and error if uninitialized
        this.handleUninitializedCache();

        if (!ids) {
            return undefined;
        }

        return ids.map(id => this.getChannelById(id));
    }

    /**
     * Retrieve the processing channel with the provided ID.
     * If the provided ID is undefined or does not match any processing
     * channel entries, the function returns undefined.
     * @param id The ID of the processing channel to retrieve
     * @param networkName optional will use default network if none is provided
     * @returns ProcessingChannel
     */
    public getChannelById(id: string, networkName: string = this.defaultNetwork): model.ProcessingChannel {
        const requestConfig = this.settings.backend.services.channelsByIds.requestConfig;
        // Throw and error if uninitialized
        this.handleUninitializedCache();
        const channel = find(this.stationDataCache.get(networkName).channels, { id });

        if (channel) {
            return channel;
        } else {
            const query = {
                id
            };
            logger.debug(`Calling get channels by ids query: ${JSON.stringify(query)}
                          request: ${JSON.stringify(requestConfig)}`);

        }
    }

    /**
     * Retrieve the processing channel with the provided ID.
     * If the provided ID is undefined or does not match any processing
     * channel entries, the function returns undefined.
     * @param id The ID of the processing channel to retrieve
     * @param networkName optional will use default network if none is provided
     * @returns ProcessingChannel as a Promise
     */
    public async getChannelByVersionId(id: string, networkName: string = this.defaultNetwork):
        Promise<model.ProcessingChannel> {
        const requestConfig = this.settings.backend.services.channelsByIds.requestConfig;
        // Throw and error if uninitialized
        this.handleUninitializedCache();
        logger.debug(`Calling get channels by ids query: ${JSON.stringify([id])}
                        request: ${JSON.stringify(requestConfig)}`);
        const channel = this.stationDataCache.has(networkName) ?
            find(this.stationDataCache.get(networkName).channels, { id }) : undefined;
        if (channel) {
            return channel;
        } else {
            // Call the service and process the response data
            return await this.httpWrapper.request(requestConfig, [id])
                .then(responseData =>
                    this.processChannelData(responseData[0]))
                .catch(e => {
                    logger.error(`processChannelData error: ${e}`);
                    return undefined;
                }
                );
        }
    }

    /**
     * Retrieve the processing station with the provided channel ID.
     * If the provided ID is undefined or does not match any processing
     * channel entries, the function returns undefined.
     * @param channelId The ID of the processing channel to retrieve
     * @returns ProcessingStation
     */
    public getStationByChannelId(channelId: string): model.ProcessingStation {
        // Throw and error if uninitialized
        this.handleUninitializedCache();
        if (!channelId) {
            return undefined;
        }
        const channel = this.getChannelById(channelId);
        if (!channel) {
            return undefined;
        }
        const site: model.ProcessingSite = this.getSiteById(channel.siteId);
        if (!site) {
            return undefined;
        }
        return this.getStationById(site.stationId);
    }

    /**
     * Retrieve the processing channels matching the provided list of IDs.
     * If the provided list of IDs is undefined or does not match any processing
     * channel entries, the function returns undefined.
     * @param ids The list of IDs to retrieve processing channels for
     * @param networkName optional will use default network if none is provided
     * @returns ProcessingChannel[]
     */
    public getChannelsById(ids: string[], networkName: string =
        this.defaultNetwork): model.ProcessingChannel[] {
        // Throw and error if uninitialized
        this.handleUninitializedCache();
        return ids.map(id => find(this.stationDataCache.get(networkName).channels, { id }));
    }

    /**
     * Retrieve the configured default list of processing stations to display
     * on the interactive analysis displays. If the default station configuration
     * is uninitialized, this function returns undefined.
     * @param networkName optional will use default network if none is provided
     * @returns ProcessingStation[] as Promise
     */
    public async getDefaultStations(
        networkName: string = this.defaultNetwork): Promise<model.ProcessingStation[]> {
        if (this.stationDataCache && !this.stationDataCache.get(networkName)) {
            await this.fetchStationData(this.defaultNetwork).then(stationDataCache => {
                if (stationDataCache && stationDataCache.stations) {
                    stationDataCache.stations.forEach(station => {
                        const site = station.siteIds.length > 0 ?
                            this.getSiteById(station.siteIds[0]) : undefined;
                        const channel = site && site.channelIds.length > 0 ?
                            this.getChannelById(site.channelIds[0]) : undefined;
                        if (station && channel) {
                            this.stationDataCache.get(networkName).defaultStationInfo.push({
                                stationId: station.id,
                                channelId: channel.id
                            });
                        }
                    });
                }
            });
        }

        // Throw and error if uninitialized
        this.handleUninitializedCache();

        // Filter the cached station data based on the default station ID list
        const dataCacheEntryForNetwork = this.stationDataCache.get(networkName);
        let stations = [];
        if (dataCacheEntryForNetwork && dataCacheEntryForNetwork.stations) {
            stations =
                filter(dataCacheEntryForNetwork.stations, station =>
                    this.stationDataCache.get(networkName).defaultStationInfo.map(
                        defaultStation => defaultStation.stationId).indexOf(station.id) > -1);
        }
        return stations;
    }

    /**
     * Creates a list of ProcessingChannels using the default channel for each
     * station in the network
     * @param networkName optional will use default network if none is provided
     * @returns ProcessingChannel[] loaded from default stations network
     */
    public getDefaultChannels(networkName: string = this.defaultNetwork): model.ProcessingChannel[] {
        return this.getChannelsById(
            this.stationDataCache.get(networkName).defaultStationInfo.map(info => info.channelId));
    }

    /**
     * Retrieve the processing station with the provided ID.
     * If the provided ID is undefined or does not match any processing
     * station entries, the function returns undefined.
     * @param id The ID of the processing station to retrieve
     * @param networkName optional will use default network if none is provided
     * @returns ProcessingStation
     */
    public getStationById(id: string, networkName: string = this.defaultNetwork): model.ProcessingStation {
        const requestConfig = this.settings.backend.services.stationsByIds.requestConfig;
        // Throw and error if uninitialized
        this.handleUninitializedCache();
        const station = find(this.stationDataCache.get(networkName).stations, { id });

        if (station) {
            return station;
        } else {
            const query = {
                id
            };
            logger.debug(`Calling get stations by ids query: ${JSON.stringify(query)}
                          request: ${JSON.stringify(requestConfig)}`);
        }
    }

    /**
     * Retrieve the processing station with the provided ID.
     * If the provided ID is undefined or does not match any processing
     * station entries, the function returns undefined.
     * @param id The ID of the processing station to retrieve
     * @param networkName optional will use default network if none is provided
     * @returns ProcessingStation as a promise
     */
    public async getStationByVersionId(id: string, networkName: string = this.defaultNetwork):
        Promise<model.ProcessingStation> {
        const requestConfig = this.settings.backend.services.stationsByIds.requestConfig;
        // Throw and error if uninitialized
        this.handleUninitializedCache();
        logger.debug(`Calling get stations by ids query: ${JSON.stringify([id])}
                        request: ${JSON.stringify(requestConfig)}`);
        const station = this.stationDataCache.has(networkName) ?
            find(this.stationDataCache.get(networkName).stations, { id }) : undefined;
        if (station) {
            return station;
        } else {
            // Call the service and process the response data
            return await this.httpWrapper.request(requestConfig, [id])
                .then(responseData =>
                    this.processStationData(responseData[0]).station)
                .catch(e => {
                    logger.error(`processStationData error: ${e}`);
                    return undefined;
                }
                );
        }
    }

    /**
     * Retrieve the processing station with the provided name.
     * If the provided name is undefined or does not match any processing
     * station entries, the function returns undefined.
     * @param name The name of the processing station to retrieve
     * @param networkName optional will use default network if none is provided
     * @returns ProcessingStation
     */
    public getStationByName(name: string, networkName: string = this.defaultNetwork): model.ProcessingStation {
        // Throw and error if uninitialized
        this.handleUninitializedCache();
        return find(this.stationDataCache.get(networkName).stations, { name });
    }

    /**
     * Retrieve the processing site for the provided ID.
     * If the provided ID is undefined or does not match any processing
     * site entries, the function returns undefined.
     * @param Id The id of the processing site to retrieve
     * @param networkName optional will use default network if none is provided
     * @returns ProcessingSite
     */
    public getSiteById(id: string, networkName: string = this.defaultNetwork): model.ProcessingSite {
        // Throw and error if uninitialized
        this.handleUninitializedCache();
        return find(this.stationDataCache.get(networkName).sites, { id });
    }

    /**
     * Retrieve the processing sites for the provided processing station ID.
     * If the provided ID is undefined or does not match any processing
     * site entries, the function returns undefined.
     * @param stationId The processing station ID to retrieve processing sites for
     * @param networkName optional will use default network if none is provided
     * @returns ProcessingSite[]
     */
    public getSitesByStation(stationId: string, networkName: string =
        this.defaultNetwork): model.ProcessingSite[] {
        // Throw and error if uninitialized
        this.handleUninitializedCache();
        return filter(this.stationDataCache.get(networkName).sites, { stationId });
    }

    /**
     * Retrieve the default processing channel for the processing station with the
     * provided ID. If the provided processing station ID is undefined or does not
     * match any default channel entries, the function returns undefined.
     * @param stationId The ID of the processing station to retrieve the default processing channel for
     * @param networkName optional will use default network if none is provided
     * @returns ProcessingChannel
     */
    public getDefaultChannelForStation(stationId: string, networkName: string =
        this.defaultNetwork): model.ProcessingChannel {
        // Throw and error if uninitialized
        this.handleUninitializedCache();

        const defaultInfo = find(this.stationDataCache.get(networkName).defaultStationInfo, { stationId });

        if (!defaultInfo) {
            throw new Error(`No default station info found for station with ID: ${stationId}`);
        }

        return find(this.stationDataCache.get(networkName).channels, { id: defaultInfo.channelId });
    }

    /**
     * Retrieve the processing networks for the provided list of IDs.
     * If the provided list of IDs is undefined or does not match any processing
     * network entries, the function returns undefined.
     * @param ids The list of IDs to retrieve processing networks for
     * @param networkName optional will use default network if none is provided
     * @returns ProcessingNetwork[]
     */
    public getNetworksByIdList(ids: string[], networkName: string =
        this.defaultNetwork): model.ProcessingNetwork[] {
        // Throw and error if uninitialized
        this.handleUninitializedCache();
        return filter(this.stationDataCache.get(networkName).networks, network => ids.indexOf(network.id) > -1);
    }

    /**
     * Retrieve the processing stations for the provided processing nework ID.
     * If the provided network ID is undefined or does not match any processing
     * station entries, the function returns undefined.
     * @param networkId The ID of the processing network to retrieve processing stations for
     * @param networkName optional will use default network if none is provided
     * @returns ProcessingStation[]
     */
    public getStationsByNetworkId(networkId: string, networkName: string =
        this.defaultNetwork): model.ProcessingStation[] {
        // Throw and error if uninitialized
        this.handleUninitializedCache();
        return filter(
            this.stationDataCache.get(networkName).stations, station => station.networkIds.indexOf(networkId) > -1);
    }

    /**
     * Retrieve the processing stations for the provided processing nework name.
     * If the provided network name is undefined or does not match any processing
     * station entries, the function returns undefined.
     * @param networkName The name of the processing network to retrieve processing stations for
     * @returns ProcessingStation[]
     */
    public getStationsByNetworkName(networkName: string): model.ProcessingStation[] {
        // Throw and error if uninitialized
        this.handleUninitializedCache();

        const network: model.ProcessingNetwork =
            find(this.stationDataCache.get(networkName).networks, { name: networkName });

        if (!network) {
            return undefined;
        }

        return filter(
            this.stationDataCache.get(networkName).stations, station => network.stationIds.indexOf(station.id) > -1);
    }

    /**
     * Populate the distance to source list using the default stations
     * @param dTSInput distance to source input
     * @returns DistanceToSouce[] as Promise
     */
    public async getDTSForDefaultStations(dTSInput: DistanceToSource): Promise<DistanceToSource[]> {
        const distanceToSourceList: DistanceToSource[] = [];
        const stations = await this.getDefaultStations();
        stations.forEach(station => {
            this.getDTSWithDistance(dTSInput, station).
                then(distanceToSource => {
                    if (distanceToSource) {
                        distanceToSourceList.push(distanceToSource);
                    }
                }).catch(e => logger.warn(e));
        });
        return distanceToSourceList;
    }

    /**
     * Populate the distance to source object with the distance
     * @param dTSInput distance to source input
     * @param station ProcessingStation
     * @returns DistanceToSource as Promise
     */
    public async getDTSWithDistance(
        dTSInput: DistanceToSource, station: model.ProcessingStation): Promise<DistanceToSource> {
        const dTSReturn: DistanceToSource = {
            ...dTSInput,
            distance: undefined,
            stationId: station.id
        };

        // If no source location set and this is an source type of event
        // with the sourceId (eventHypId) set then look up the source loc
        // TODO: this will return a hypothesis and not an event. Will need to get the hypothesis that matches id
        // TODO: by getting all events, then get the hypothesis' eventId field, then search for the
        // TODO: target event by id for this logic to work
        if (!dTSReturn.sourceLocation && dTSReturn.sourceType === DistanceSourceType.Event &&
            dTSReturn.sourceId) {
            // Find the event
            //
            const event: Event = await eventProcessor.getEventById(dTSInput.sourceId);
            if (event && event.hypotheses) {
                const eventHypothesis = event.hypotheses[event.hypotheses.length - 1];
                dTSReturn.sourceLocation = {
                    latDegrees: eventHypothesis.preferredLocationSolution.locationSolution.location.latitudeDegrees,
                    lonDegrees: eventHypothesis.preferredLocationSolution.locationSolution.location.longitudeDegrees,
                    elevationKm: eventHypothesis.preferredLocationSolution.locationSolution.location.depthKm
                };
            }
        }
        if (!dTSReturn.sourceLocation) {
            if (dTSReturn.sourceId) {
                logger.warn(`For source id ${dTSReturn.sourceId}` +
                    `source location is not set cannot compute distance for station ${station.id}.`);
            }
            return undefined;
        }
        dTSReturn.distance = this.getDistanceToSource(dTSReturn.sourceLocation, dTSReturn.distanceUnits, station);
        if (!dTSReturn.distance) {
            logger.warn(`For source id ${dTSReturn.sourceId} Distance is undefined for station ${station.id}.`);
        }
        return dTSReturn;
    }

    /**
     * Calculate the distance in kilometers between a provided source location and processing station.
     * @param sourceLoc The source location for which to calculate distance to the provided station
     * @param station The station for which to calculate distance to the provided source location
     * @returns calculated distance in kilometers
     */
    public getDistanceToSource(sourceLoc: Location, units: DistanceUnits, station: model.ProcessingStation): number {
        const accuracy = 1000;
        const kmPrecision = 0;
        const degreePrecision = 1000;
        const KM = 1000;
        const dist: number = geolib.getDistance(
            { latitude: station.location.latDegrees, longitude: station.location.lonDegrees },
            { latitude: sourceLoc.latDegrees, longitude: sourceLoc.lonDegrees }, accuracy, kmPrecision
        );
        const km = dist / KM;
        // Determin by units if to return in KM or Degrees
        if (units === DistanceUnits.km) {
            return km;
        } else {
            return Math.round(km / this.KM_TO_DEGREES * degreePrecision) / degreePrecision;
        }
    }

    /**
     * Transforms the Station response from the OSD to a ProcessingStation with sites and channels
     * @param stationResponse respone from the endpont
     * @param networkId optional networkId
     * @returns {station, sites, channels} ProcessingStation, ProcessingSite[], ProcessingChannel[]
     */
    public processStationData(stationResponse: any, networkId?: string): {
        station: model.ProcessingStation;
        sites: model.ProcessingSite[];
        channels: model.ProcessingChannel[];
    } {
        if (!stationResponse) return undefined;
        const stationId: string = stationResponse.id;
        const sites: model.ProcessingSite[] = [];
        const channels: model.ProcessingChannel[] = [];
        const station: model.ProcessingStation = {
            id: stationId,
            name: stationResponse.name,
            description: stationResponse.description,
            stationType: stationResponse.stationType,
            latitude: stationResponse.latitude,
            longitude: stationResponse.longitude,
            elevation: stationResponse.elevation,
            location: {
                latDegrees: stationResponse.latitude,
                lonDegrees: stationResponse.longitude,
                elevationKm: stationResponse.elevation
            },
            siteIds: [],
            networkIds: [networkId ? networkId : undefined],
            dataAcquisition: {
                dataAcquisition: this.randomEnumSelector('dataAcquisition'),
                interactiveProcessing: this.randomEnumSelector('interactiveProcessing'),
                automaticProcessing: this.randomEnumSelector('automaticProcessing')
            }
        };

        if (stationResponse.sites) {
            // For each station, parse the list of sites from the response
            stationResponse.sites.forEach(siteResponse => {
                const siteData = this.processSiteData(siteResponse, station);
                channels.push(...siteData.channels);
                sites.push(siteData.site);
                station.siteIds.push(siteResponse.id);
            });
        }

        return { station, sites, channels };
    }

    /**
     * Fetch station-related data from backend services for the provided network name.
     * This is an asynchronous function.
     * This function propagates errors from the underlying HTTP call.
     * Fetched data include processing networks, stations, sites, and channels.
     * @param networkName The name of the network to retrieve station-related data for
     * optional will use default network if none is provided
     * @returns StataionDataCacheEntry as Promise
     */
    private async fetchStationData(networkName: string = this.defaultNetwork):
        Promise<StationDataCacheEntry> {
        if (this.stationDataCache.get(networkName)) {
            return this.stationDataCache.get(networkName);
        }
        logger.info(`Fetching processing station data for network with name: ${networkName}`);

        // Build the query to be encoded as query string parameters
        const query = {
            name: networkName
        };

        // Retrieve the request configuration for the service call
        const requestConfig = this.settings.backend.services.networkByName.requestConfig;

        // Call the service and process the response data
        const promise = this.httpWrapper.request(requestConfig, query)
            .then((responseData: model.OSDNetwork) => {
                const networkData: StationDataCacheEntry = this.processNetworkData(responseData);
                // If the member station data cache is uninitialized, set it to the parsed response data;
                // otherwise merge the parsed response data into the existing member cache instance
                this.stationDataCache.set(networkName, this.stationDataCache.get(networkName) ?
                    merge(this.stationDataCache.get(networkName), networkData) :
                    networkData);
            })
            .then(() => {
                const noDataString = this.stationDataCache.get(networkName) ? '' : '- No data loaded';
                logger.info(`Station data ${this.settings.backend.mock.enable ? 'mock' : 'OSD'} ` +
                    `fetch complete ${noDataString}`);
            })
            .catch(error => logger.error(error));
        await promise;
        return this.stationDataCache.get(networkName);
    }

    /**
     * Transforms site response from the OSD to a ProcessingSite with channels
     * @param siteResponse response from endpoint
     * @param station ProcessingStation used to get station id to link site to station
     * @returns {site, channels} ProcessingSite, ProcessingChannel[]
     */
    private processSiteData(
        siteResponse: any, station: model.ProcessingStation): {
            site: model.ProcessingSite;
            channels: model.ProcessingChannel[];
        } {

        if (!siteResponse) return undefined;
        const channels: model.ProcessingChannel[] = [];
        const site: model.ProcessingSite = {
            id: siteResponse.id,
            name: siteResponse.name,
            location: {
                latDegrees: siteResponse.latitude,
                lonDegrees: siteResponse.longitude,
                elevationKm: siteResponse.elevation
            },
            stationId: station.id,
            channelIds: []
        };

        if (siteResponse.channels) {
            // For each site, parse the list of channels from the response
            siteResponse.channels.forEach(channelResponse => {
                const channel = this.processChannelData(channelResponse, site);
                site.channelIds.push(channel.id);
                channels.push(channel);
            });
        }
        return { site, channels };
    }

    /**
     * Transforms channel response from OSD to a ProcessingChannel
     * @param channelResponse response from endpoint
     * @param site optional ProcessingSite used to link channel to site and provide site name
     * @returns ProcessingChannel
     */
    private processChannelData(
        channelResponse: model.ProcessingChannel, site?: model.ProcessingSite): model.ProcessingChannel {
        if (!channelResponse) return undefined;
        const channelId: string = channelResponse.id;
        const channel = {
            id: channelId,
            name: channelResponse.name,
            channelType: channelResponse.channelType,
            // TODO backend channel has a location, rather than a location code;
            // update the model and populate
            locationCode: 'Invalid',
            siteId: site ? site.id : undefined,
            siteName: site ? site.name : undefined,
            dataType: channelResponse.dataType,
            latitude: channelResponse.latitude,
            longitude: channelResponse.longitude,
            elevation: channelResponse.elevation,
            verticalAngle: channelResponse.verticalAngle,
            horizontalAngle: channelResponse.horizontalAngle,
            position: channelResponse.position,
            actualTime: channelResponse.actualTime,
            systemTime: channelResponse.systemTime,
            sampleRate: channelResponse.sampleRate,
            depth: channelResponse.depth
        };
        // TODO: Remove this check when the OSD start returning the channel position
        if (channel && !channel.position) {
            channel.position = {
                eastDisplacementKm: 0,
                northDisplacementKm: 0,
                verticalDisplacementKm: 0
            };
        }

        // Return processed channel
        return channel;
    }

    /**
     * Process station-related data response from the backend service call. Specifically,
     * parse the response JSON into model entities (processing networks, stations, sites & channels),
     * and store the parsed data in the cache
     * @param networkResponse The JSON station data response received from a backend service to be processed
     * @returns StationDataCacheEntry
     */
    private processNetworkData(networkResponse: model.OSDNetwork): StationDataCacheEntry {
        // If the response data is valid, parse it into model entities and merge into the member cache
        if (networkResponse) {

            const stationDataCache: StationDataCacheEntry = {
                networks: [],
                stations: [],
                defaultStationInfo: [],
                sites: [],
                channels: []
            };

            const stations: model.ProcessingStation[] = [];
            const sites: model.ProcessingSite[] = [];
            const channels: model.ProcessingChannel[] = [];
            // Parse the network from the response
            const network: model.ProcessingNetwork = {
                id: networkResponse.id,
                name: networkResponse.name,
                monitoringOrganization: networkResponse.organization,
                stationIds: []
            };

            networkResponse.stations.forEach(stationResponse => {
                const stationData = this.processStationData(stationResponse, network.id);
                stationData.sites.forEach(site => sites.push(site));
                stationData.channels.forEach(channel => channels.push(channel));
                network.stationIds.push(stationData.station.id);
                stations.push(stationData.station);
            });

            stationDataCache.networks.push(network);
            stationDataCache.stations = stations;
            stationDataCache.sites = sites;
            stationDataCache.channels = channels;

            logger.debug('Updated station data cache following fetch from the backend:', `\n${JSON.stringify(
                this.stationDataCache, undefined, 2)}`);

            return stationDataCache;
        }
    }

    /**
     * Handle cases where the data cache has not yet been initialized.
     */
    private handleUninitializedCache(): void {
        if (!this.stationDataCache) {
            logger.error('Attempt to access uninitialized station cache.');
            throw new Error('The station processor has not been initialized');
        }
    }

    /**
     * Tool to generate more realistic mock data, selects a random enum attribute
     * @param enumType enum type indentifier (dataAcquisition, interactiveProcessing, automaticProcessing)
     * @returns a random selection of the enumType passed in
     */
    private readonly randomEnumSelector = (enumType: string): any => {
        const min = 1;
        const max = 5;
        const base = 10;
        const bound = 4;
        let randomNumber = min + getSecureRandomNumber() * (max - min);
        let enumTypeValue: string;
        randomNumber = parseInt(randomNumber.toFixed(), base);

        if (enumType === 'dataAcquisition') {
            randomNumber % 2 ? enumTypeValue = 'enabled' : enumTypeValue = 'disabled';
        } else if (enumType === 'interactiveProcessing') {
            randomNumber % 2 ? enumTypeValue = 'default' : enumTypeValue = 'request';
        } else {
            if (randomNumber >= bound) {
                enumTypeValue = 'network';
            } else {
                randomNumber % 2 ? enumTypeValue = 'station' : enumTypeValue = 'disabled';
            }
        }

        return enumTypeValue;
    }
}

// Export an initialized instance of the processor
export const stationProcessor: StationProcessor = new StationProcessor();
stationProcessor.initialize();
