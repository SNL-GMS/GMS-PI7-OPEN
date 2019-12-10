import * as model from './model';
import * as osdModel from './model-osd';
import * as config from 'config';
import * as eventMockBackend from './event-mock-backend';
import { HttpClientWrapper } from '../util/http-wrapper';
import { gatewayLogger as logger } from '../log/gateway-logger';
import { cloneDeep, find, includes, uniqBy } from 'lodash';
import * as uuid4 from 'uuid/v4';
import { TimeRange, CreationInfo, AssociationChange, EventAndAssociationChange } from '../common/model';
import { workflowProcessor } from '../workflow/workflow-processor';
import { PhaseType } from '../channel-segment/model-spectra';
import { FeatureMeasurementTypeName, SignalDetectionHypothesis, SignalDetection } from '../signal-detection/model';
import { ProcessingChannel } from '../station/model';
import { stationProcessor } from '../station/station-processor';
import { configProcessor } from '../config/config-processor';
import { signalDetectionProcessor } from '../signal-detection/signal-detection-processor';
import { toOSDTime } from '../util/time-utils';
import { getCreationInfo, isObjectEmpty } from '../util/common-utils';
import { convertEventHypothesisToOSD,
         convertLocationSolutionFromOSD,
         convertLocationSolutionToOSD,
         convertFeaturePredictionsFromOSD,
         convertEventHypothesisFromOSD,
         findPrefLocSolutionUsingEventHypo,
         findPreferredLocationSolutionSet,
         createLocationSolutionSet,
         getNewRandomEventLocation,
         makeSignalDetectionSnapshots,
        } from '../util/event-utils';
import { convertOSDChannel } from '../util/station-utils';
import { performanceLogger } from '../log/performance-logger';
import { convertSDtoOSD } from '../util/signal-detection-utils';

/**
 * Event processor obtains events by IDs or time range. Handles location event, and creating a 
 * new event location. Modifies and creates existing events and feature predictions. 
 */
class EventProcessor {
    /** Settings for the event processor */
    private settings: any;

    /** Axios http wrapper  */
    private httpWrapper: HttpClientWrapper;

    /** Event cache */
    private eventCache: Map<string, model.Event>;

    /** Signal detection to event hypotheses map */
    private sdhToEventHypothesesMap: Map<string, model.EventHypothesis[]>;

    /** Default phases */
    private defaultPhases: string[] = [];

    /** Preferred Location solution restraint order */
    private preferredLocationSolutionRestraintOrder: string[] = [];
    public constructor() {
        this.settings = config.get('event');
        this.httpWrapper = new HttpClientWrapper();
    }

    /**
     * Initialize the event processor, setting up a mock backend if configured to do so.
     */
    public initialize(): void {
        logger.info('Initializing the Event processor - Mock Enable: %s', this.settings.backend.mock.enable);

        // Init the maps
        this.eventCache = new Map<string, model.Event>();
        this.sdhToEventHypothesesMap = new Map<string, model.EventHypothesis[]>();

        // Override the OSD methods if in mock mode and set default phases for mock
        if (this.settings.backend.mock.enable) {
            eventMockBackend.initialize(this.httpWrapper.createHttpMockWrapper());
        }
        // Retrieve default Phases from the config processor
        this.defaultPhases = configProcessor.getConfigByKey('defaultSdPhases');

        if (!this.settings.backend.mock.enable) {
            this.defaultPhases = ['PKP', 'PKPbc', 'S', 'P', 'Pn', 'PcP', 'Sn']; // broken list: 'pP', LR', 'Lg',
        }

        // Retrieve the order of the DepthRestraintType for Preferred Location Solution
        this.preferredLocationSolutionRestraintOrder = configProcessor.
            getConfigByKey('preferredLocationSolutionRestraintOrder');

        // TODO: Remove when Configuratrion service is truly up and running
        // If not found in the Config Processor
        if (!this.defaultPhases || this.defaultPhases.length < 1) {
            this.defaultPhases = [
                'P', 'Pn', 'PKP', 'PKPbc', 'PcP', 'pP',
                'S', 'Sn', 'LR', 'Lg'
              ];
            logger.warn(`Could not find any default Phases in` +
                ` config processors setting default list to ${this.defaultPhases}`);
        }
    }

    /**
     * Locate Event streaming call to calculate the location solutions for the given EventHypothesis
     * @param eventHypothesisId Id to find event hypothesis to send
     * @param preferredLocationSolutionId which prSending service requeferred location solution to send
     * @param locationBehaviors to send in the event hypothesis this tells streaming call
     *        which feature measurements to use
     * 
     * @returns EventHypothesis with updated Location Solutions
     */
    public async locateEvent(eventHypothesisId: string, preferredLocationSolutionId: string,
                             locationBehaviors: model.LocationBehavior[]): Promise<model.EventHypothesis> {
        const cachedEventHypo = this.getEventHypothesisById(eventHypothesisId);
        if (!cachedEventHypo) {
            logger.warn(`Could not find Event Hypothesis Id: ${eventHypothesisId} returning empty array.`);
            return undefined;
        }

        // Clone the eventHyp so the data changed as the input argument is not reflected in the event cache
        const eventHypo = cloneDeep(cachedEventHypo);
        eventHypo.preferredLocationSolution.locationSolution.locationBehaviors = locationBehaviors;
        // Update event hypo with location solution behavoirs preferred location solution
        // TODO: Need to work thru updating the cache EventHypo vs a local version
        if (preferredLocationSolutionId &&
            eventHypo.preferredLocationSolution.locationSolution.id !== preferredLocationSolutionId) {
            const preferredLocationSolution = findPrefLocSolutionUsingEventHypo(eventHypo);
            if (preferredLocationSolution) {
                eventHypo.preferredLocationSolution.locationSolution = preferredLocationSolution;
            }
        }

        // Update the preferred location solution location behaviors and remove all but preferred
        // location solution from the location solution list. We will need this when we send
        // the three location solutions (surface, depth and unrestrained) not just the one
        eventHypo.preferredLocationSolution.locationSolution.locationBehaviors = locationBehaviors;
        eventHypo.locationSolutionSets = [findPreferredLocationSolutionSet(eventHypo)];

        // Convert OSD compatiable event hypothesis to populate
        const osdEventHypo: osdModel.EventHypothesisOSD = convertEventHypothesisToOSD(eventHypo);
        // Get signal detections associated and convert them to OSD
        const signalDetections: SignalDetection[] =
            (await signalDetectionProcessor.getSignalDetectionsByEventId(osdEventHypo.eventId))
            .map(convertSDtoOSD);

        // Create the three DepthRestrained params
        // Unrestrained
        const unrestrainedParams: model.LocateEventParameter =
            this.buildEventLocationParameters(model.DepthRestraintType.UNRESTRAINED, undefined);
        let locationSolutions = await this.computeLocationSolution(osdEventHypo, signalDetections, unrestrainedParams);

        // Compute Surface and concat the location solutions
        const surfaxeParams: model.LocateEventParameter =
            this.buildEventLocationParameters(model.DepthRestraintType.FIXED_AT_SURFACE, 0);
        locationSolutions = locationSolutions.concat(
            await this.computeLocationSolution(osdEventHypo, signalDetections, surfaxeParams));

        // Compute Depth and concat the location solutions
        const eventLoc = eventHypo.preferredLocationSolution.locationSolution.location;
        const depthParams: model.LocateEventParameter =
            this.buildEventLocationParameters(model.DepthRestraintType.FIXED_AT_DEPTH, eventLoc.depthKm);
        locationSolutions = locationSolutions.concat(
                await this.computeLocationSolution(osdEventHypo, signalDetections, depthParams));

        // If the location solutions list is empty then all three calls failed. So raise an exception
        if (locationSolutions.length === 0) {
            throw new Error(`Locate event failed for event ${osdEventHypo.eventId}`);
        }
        // Create location solution set to add to cachedEventHypo
        const locationSolutionSet = createLocationSolutionSet(cachedEventHypo, locationSolutions);

        // Set the preferred location solution based on the DepthRestraintType preferrence order
        // Call find in the restraint order till we find the Location Solution to set
        let pls: model.LocationSolution;
        this.preferredLocationSolutionRestraintOrder.forEach(depthRestraintString => {
            if (!pls) {
                pls = locationSolutionSet.locationSolutions.find(ls =>
                ls.locationRestraint.depthRestraintType === depthRestraintString);
            }
        });

        // Set the preferred location solution and add the set
        if (pls) {
            cachedEventHypo.preferredLocationSolution.locationSolution = pls;
        }
        cachedEventHypo.locationSolutionSets.push(locationSolutionSet);
        return cachedEventHypo;
    }

    /**
     * Requests events from the (mock?) backend and returns a list
     * Events are added to the cache 
     * 
     * @param timeRange 
     */
    public async getEventsInTimeRange(timeRange: TimeRange): Promise<model.Event[]> {
        // TODO: Add cache checks before calling OSD
        const requestConfig = this.settings.backend.services.getEventsByTimeAndLatLong.requestConfig;
        const query = {
            startTime: toOSDTime(timeRange.startTime),
            endTime: toOSDTime(timeRange.endTime)
        };
        const eventOSDs: osdModel.EventOSD[] = await this.httpWrapper.request(requestConfig, query);
        if (!eventOSDs) {
            return [];

        } else {
            eventOSDs.forEach(async eventOSD => {
                // Convert time values
                const event = this.convertEventOSD(eventOSD);

                // Aligning preferred event hypothesis history and the hypotheses collection to refer to the same
                // event hypothesis object
                // TODO handle final event hypothesis history as well
                if (event.preferredEventHypothesisHistory && event.preferredEventHypothesisHistory.length > 0) {
                    event.preferredEventHypothesisHistory.forEach((prefEventHyp, index) => {
                        const matchHyp = event.hypotheses.find(hyp => hyp.id === prefEventHyp.eventHypothesis.id);
                        if (matchHyp) {
                            prefEventHyp.eventHypothesis = matchHyp;
                        } else {
                            event.hypotheses.push(prefEventHyp.eventHypothesis);
                        }
                    });
                }
                event.currentEventHypothesis.eventHypothesis = event.hypotheses.
                find(hyp => hyp.id === event.currentEventHypothesis.eventHypothesis.id);
                // Loop through all hypotheses and check that the sdhypid refers to a SD that we loaded
                // TODO This is necessary due to a change in the 'demo' network and stations were removed
                // remove when old test data is replaced with Standard Test Data Set
                event.hypotheses.forEach(hyp => {
                    const associationList = [];
                    hyp.associations.forEach(assoc => {
                        const sdhFound = signalDetectionProcessor
                                            .getSignalDetectionHypothesisById(assoc.signalDetectionHypothesisId);
                        if (sdhFound) {
                                associationList.push(assoc);
                        }
                    });
                    hyp.associations = associationList;
                });
                if (!this.eventCache.has(event.id)) {
                    this.eventCache.set(event.id, event);
                }
            });

            // Rebuild Signal Detection Hypo to Event Hypo map each time new events each time called
            this.rebuildSignalDetectionHypoToEventHypoMap();
            // Returns all events in cache that are in the provided time range
            return [...this.eventCache.values()].filter(event => {
                const eventTime =
                event.currentEventHypothesis.eventHypothesis.preferredLocationSolution.locationSolution.location.time;
                return eventTime >= timeRange.startTime && eventTime <= timeRange.endTime;
            });
        }
    }

    /**
     * Retrieve the event hypotheses with the provided ID.
     * @param hypothesisId The ID of the event hypotheses to retrieve
     * TODO: this needs to come from the cache! 
     * TODO: Currently it expects a large time range param and then to filter down.
     * TODO: There is no feature guidance for NFIG at this time to provide an endpoint that grabs
     * TODO: EventHypothesis by Id.
     */
    public getEventHypothesisById(hypothesisId: string): model.EventHypothesis {
        // get the events by time range
        const events = Array.from(this.eventCache.values());
        const hypotheses = [];
        events.forEach(event => {
            event.hypotheses.forEach(hypo => hypotheses.push(hypo));
        });
        const hyp = hypotheses
        .find(hypothesis => hypothesis.id === hypothesisId);
        if (!hyp) {
            logger.warn('Event Hypothesis Not found: ' + hypothesisId);
        }
        return hyp;
    }

    /**
     * Retrieve the current (modified?) event hypothesis for the event with the provided ID.
     * If not set then set the current event hypothesis
     * @param eventId The ID of the event to locate the current hypothesis
     * 
     * @returns current PreferredEventHypotheis
     */
    public getCurrentEventHypothesisByEventId(eventId: string): model.PreferredEventHypothesis {

        // Check cache for event with given ID
        if (!this.eventCache.get(eventId)) {
            return undefined;
        }

        // Find the event and rerturn current event hypothesis if set
        const event = this.eventCache.get(eventId);
        if (event.currentEventHypothesis) {
            return event.currentEventHypothesis;
        }
        const historyAndPreferred = this.getCurrentEventHypothesis(event.preferredEventHypothesisHistory);

        event.currentEventHypothesis = historyAndPreferred.currentPrefEventHypo;
        event.preferredEventHypothesisHistory = historyAndPreferred.preferredEventHypothesisHistory;
        return event.currentEventHypothesis;
    }

    /**
     * Retrieve the event hypotheses associated with the event whose ID matches the parameter
     * @param id The ID of the event to retrieve the hypotheses for
     */
    public async getHypothesesForEvent(id: string): Promise<model.EventHypothesis[]> {
        const event = await this.getEventById(id);
        return event.hypotheses;
    }

    /**
     * Retrieve the event with the provided ID.
     * @param eventId The ID of the event to retrieve
     */
    public async getEventById(eventId: string): Promise<model.Event> {
        const cachedEvent = this.getCachedEventById(eventId);
        if (!cachedEvent) {
            const requestConfig = this.settings.backend.services.getEventsByIds.requestConfig;
            const query = {
                    ids: [eventId]
            };

            const responseData = await this.httpWrapper.request(requestConfig, query);
            const eventOSD: osdModel.EventOSD = responseData[0];
            const event: model.Event = this.convertEventOSD(eventOSD);

            // Add it to the cache
            this.eventCache.set(event.id, event);
            return event;
        }
        return cachedEvent;
    }

    /**
     * Returns the event from the cache
     * @param eventId id of the event to retrieve
     */
    public getCachedEventById(eventId: string): model.Event {
        return this.eventCache.get(eventId);
    }

    /**
     * Retrieve the event with the provided hypothesis ID.
     * @param eventHypId The ID of the event hyp to retrieve
     */
    public getEventByHypId(eventHypId: string): model.Event {
        let eventToReturn;
        this.eventCache.forEach((event: model.Event) => {
            if (event.hypotheses.findIndex(eventHyp => eventHyp.id === eventHypId) >= 0) {
                eventToReturn  = event;
            }
        });
        return eventToReturn;
    }

/**
 * Creates an empty event and then associates the sd hyps based on input list
 * @param sdHypIds list of ids linked to the hypotheses to associate 
 */
public createEvent(sdHypIds: string[]): EventAndAssociationChange {
    // Create a new event hypothesis
    const emptyHyp = this.createNewEventHypothesis();
    const emptyPreferredHyp = {
        processingStageId: workflowProcessor.getCurrentStageId(),
        eventHypothesis: emptyHyp
    };

    // Create the event object
    const event: model.Event = {
        id: uuid4().toString(),
        activeAnalystUserNames: [],
        currentEventHypothesis: emptyPreferredHyp,
        hypotheses: [emptyHyp],
        modified: true,
        monitoringOrganization: 'TEST',
        preferredEventHypothesisHistory: [emptyPreferredHyp],
        finalEventHypothesisHistory: [],
        status: model.EventStatus.ReadyForRefinement
    };

    // Set the link between new event and new event hyp and set it in the cache
    emptyHyp.eventId = event.id;
    this.eventCache.set(event.id, event);

    // If sdHyps are populated, associate the sd hyps to the newly created event
    let associationChanges: AssociationChange = {
        events: [],
        sds: []
    };
    this.setNewEventHypothesisLocation(event, sdHypIds);
    if (sdHypIds && sdHypIds.length > 0) {
         associationChanges = this.changeSignalDetectionAssociations(emptyHyp.id, sdHypIds, true);
    }

    // Set location for new event based on associations
    return {
        event,
        associationChange: associationChanges
    };
}

/**
 * Updates the events with the provided IDs using the provided input parameters. If no updates parameters are
 * included, this method will throw an error
 */
public async updateEvents(eventIds: string[], input: model.UpdateEventInput): Promise<model.Event[]> {
    return await Promise.all(eventIds.map(async eventId => (this.updateEvent(eventId, input))));
}

/**
 * Updates the event with the provided ID using the provided input parameters. If no updates parameters are
 * included, this method will throw an error
 * @param eventId: The ID of the vent to update
 * @param input: The input parameters to update in the event
 */
public async updateEvent(eventId: string, input: model.UpdateEventInput): Promise<model.Event> {
    // Try to retrieve the event with the provided ID; throw an error if it is missing
    const event = await this.getEventById(eventId);

    if (!event) {
        throw new Error(`Attempt to update a missing event with ID ${eventId}`);
    }

    // For now call computeFeaturePredictions this is the first time
    // TODO: Rework compute FP as part of AzSlow Compute work
    // This is first opportunity to populate based on an individual event loading on UI
    if (input.status === model.EventStatus.OpenForRefinement) {
        await this.computeFeaturePredictions(event);
    }

    // Track whether any updates have been made
    let update = false;
    // Update the events status
    if (input.status) {
        event.status = input.status;
        update = true;
    }
    // Update the preferred hypothesis if provided in the input
    if (input.preferredHypothesisId) {
        event.preferredEventHypothesisHistory.push({
            processingStageId: input.processingStageId,
            eventHypothesis: {id: input.preferredHypothesisId,
                rejected: false,
                eventId: event.id,
                parentEventHypotheses: [],
                locationSolutionSets: [],
                preferredLocationSolution: undefined,
                associations: []},
        });
        update = true;
    }

    // TODO: active analyst user names field DNE in OSD
    // Update the list of active analyst user names if provided in the input
    if (input.activeAnalystUserNames) {
        event.activeAnalystUserNames = input.activeAnalystUserNames;
        update = true;
    }

    // Throw an error if no updates were made (invalid input), since we don't want to publish a subscription callback
    // for no-op updates
    if (!update) {
        throw new Error(`No valid input provided to update event with ID: ${eventId}`);
    }
    logger.info(`Updating event ${eventId}`);
    return event;
}

/**
 * Creates a new event hypothesis from the input, and associates it with the event with the provided ID.
 * @param eventId The ID of the event to update
 * @param input The input used to create a new event hypothesis
 */
public async createEventHypotheses(eventId: string, input: model.CreateEventHypothesisInput)
    : Promise<model.EventHypothesis[]> {

    // Try to retrieve the event with the provided ID; throw an error if it is missing
    const event = await this.getEventById(eventId);

    // Create a creation info object for the new hypothesis from the input
    const creationInfo: CreationInfo = getCreationInfo(input.creatorId);
    const locationBehaviors = [{
        residual: 4,
        weight: 5,
        defining: false,
        featurePredictionId: '21111111-1111-1111-1111-111111111111',
        featureMeasurementId: '31111111-1111-1111-1111-111111111111'
    }];
    // Create a location solution for the new hypothesis from the input
    const locationSolution: model.LocationSolution = {
        id: uuid4.toString(),
        location : {
            latitudeDegrees: input.eventLocation.latitudeDegrees,
            longitudeDegrees: input.eventLocation.longitudeDegrees,
            depthKm: input.eventLocation.depthKm,
            time: input.eventLocation.time
        },
        featurePredictions: [], // TODO need to populate!
        locationRestraint: {
            depthRestraintType: model.DepthRestraintType.UNRESTRAINED,
            depthRestraintKm: 10,
            latitudeRestraintType: model.RestraintType.UNRESTRAINED,
            latitudeRestraintDegrees: 11,
            longitudeRestraintType: model.RestraintType.UNRESTRAINED,
            longitudeRestraintDegrees: 12,
            timeRestraintType: model.RestraintType.FIXED,
            timeRestraint: '2018-01-01T01:15:50Z'
        },
        locationUncertainty: undefined,
        locationBehaviors,
        snapshots: makeSignalDetectionSnapshots(event.currentEventHypothesis.eventHypothesis.associations,
                                                locationBehaviors)
    };

    const hypothesisId = uuid4().toString();
    const associations: model.SignalDetectionEventAssociation[] =
        input.associatedSignalDetectionIds.map(sdId => ({
            id: uuid4().toString(),
            eventHypothesisId: hypothesisId,
            signalDetectionHypothesisId: sdId,
            rejected: false,
        }));
    // Create the new hypothesis
    // TODO Eventually will need to create 3 LS (Depth, Surface and Unrestrined)
    const hypothesis: model.EventHypothesis = {
        id: hypothesisId,
        rejected: false,
        eventId: event.id,
        parentEventHypotheses: [],
        locationSolutionSets: [],
        associations,
        preferredLocationSolution: {
            locationSolution,
            creationInfo
        }
    };
    hypothesis.locationSolutionSets.push(createLocationSolutionSet(hypothesis, [locationSolution]));

    // Add the new hypothesis to the data set
    event.hypotheses.push(hypothesis);

    // Update the SDH to EH List map
    this.addEventHypothesisToSDHypoMap(hypothesis);
    return event.hypotheses;
}

/**
 * Creates a new event hypothesis from the input, and associates it with the event with the provided ID.
 * @param eventId The ID of the event to create hypothesis for
 */
public createNewEventHypothesis(event?: model.Event, associations?: model.SignalDetectionEventAssociation[])
    : model.EventHypothesis {

    // Check for optional event parameter
    if (!event) {
        // Create new hypothesis not linked to an event
        return {
            id: uuid4().toString(),
            associations: [],
            eventId: undefined,
            parentEventHypotheses: [],
            rejected: false,
            locationSolutionSets: [],
            preferredLocationSolution: undefined
        };
    }

    this.getCurrentEventHypothesisByEventId(event.id);
    const newHyp = cloneDeep(event.currentEventHypothesis);
    const newHypId = uuid4().toString();
    newHyp.eventHypothesis.id = newHypId;
    const newAssociations = [];
    const associationsToUpdate = associations ? associations :
                                 event.currentEventHypothesis.eventHypothesis.associations;
    associationsToUpdate.forEach(assoc => {
        if (!assoc.rejected) {
            newAssociations.push({
                ...assoc,
                id: uuid4().toString(),
                eventHypothesisId: newHypId
            });
        }
    });
    newHyp.eventHypothesis.associations = newAssociations;

    event.hypotheses.push(newHyp.eventHypothesis);
    // TODO should we be changing the preferred when we create a new even hyp
    event.preferredEventHypothesisHistory.push(newHyp);
    event.currentEventHypothesis = newHyp;
    event.modified = true;

    return newHyp.eventHypothesis;
}

/**
 * Updates the event hypothesis with the provided ID using the provided input values.
 * Only the fields that are defined in the input will be applied.
 * @param hypothesisId The ID of the hypothesis to update
 * @param input The input values to apply to the hypothesis fields
 */
public updateEventHypothesis(hypothesisId: string, input: model.UpdateEventHypothesisInput): model.EventHypothesis {

    // Find the event hypothesis to update; throw an error if it can't be found
    const hypothesis = this.getEventHypothesisById(hypothesisId);
    if (!hypothesis) {
        throw new Error(`Attempt to update a missing hypothesis with ID ${hypothesisId}`);
    }

    // Track whether any updates have been made
    let update = false;

    // Set the rejected flag if it is defined in the input
    if (input.rejected != undefined) {
        hypothesis.rejected = input.rejected;
        update = true;
    }

    // TODO: Not sure why this is part of an updated to Event Hypo????
    // Add the signal detection associations if they are defined in the input
    // if (input.associatedSignalDetectionIds && input.associatedSignalDetectionIds.length > 0) {
    //     signalDetectionProcessor.addDetectionAssociations(input.associatedSignalDetectionIds, hypothesis.id);
    //     update = true;
    // }

    if (input.eventLocation) {
        // Create a location solution for the new hypothesis from the input
        // and add it to the hypothesis' list
        const locationBehaviors = [{
            residual: 4,
            weight: 5,
            defining: false,
            featurePredictionId: '21111111-1111-1111-1111-111111111111',
            featureMeasurementId: '31111111-1111-1111-1111-111111111111'
        }];
        const locationSolution: model.LocationSolution = {
            id: uuid4.toString(),
            location : {
                latitudeDegrees: input.eventLocation.latitudeDegrees,
                longitudeDegrees: input.eventLocation.longitudeDegrees,
                depthKm: input.eventLocation.depthKm,
                time: input.eventLocation.time
            },
            featurePredictions: [], // TODO need to populate!
            locationRestraint: {
                depthRestraintType: model.DepthRestraintType.UNRESTRAINED,
                depthRestraintKm: 10,
                latitudeRestraintType: model.RestraintType.UNRESTRAINED,
                latitudeRestraintDegrees: 11,
                longitudeRestraintType: model.RestraintType.UNRESTRAINED,
                longitudeRestraintDegrees: 12,
                timeRestraintType: model.RestraintType.FIXED,
                timeRestraint: '2018-01-01T01:15:50Z'
            },
            locationUncertainty: undefined,
            locationBehaviors,
            snapshots: makeSignalDetectionSnapshots(hypothesis.associations, locationBehaviors)
        };

        // Create a creation info object for the new hypothesis from the input
        const creationInfo: CreationInfo = getCreationInfo(input.creatorId);

        // Set the new location solution as preferred
        hypothesis.preferredLocationSolution = {
            locationSolution,
            creationInfo
        };
        update = true;
    }

    // Throw an error if no updates were made (invalid input), since we don't want to publish a subscription callback
    // for no-op updates
    if (!update) {
        throw new Error(`No valid input provided to update event hypothesis with ID: ${hypothesisId}`);
    }

    return hypothesis;
}

    /**
     * Change event hypothesis SD associations to associate or unassociate based on associate flag
     * @param eventHypothesisId The ID of the event hypothesis being changed
     * @param signalDetectionHypoIds The list SD Hypo Ids to (un)associate
     * @param associate The boolean to unassociate or associate
     * 
     * @returns all events changed by the association changes
     */
    public changeSignalDetectionAssociations(
        eventHypothesisId: string, signalDetectionHypoIds: string[], associate: boolean): AssociationChange {
        const eventHypo = this.getEventHypothesisById(eventHypothesisId);
        if (!eventHypo) {
            return undefined;
        }

        // Find the event associated to the event hypo; this is being returned
        const event = this.getCachedEventById(eventHypo.eventId);
        let associations = cloneDeep(eventHypo.associations);
        const removedAssociations: model.SignalDetectionEventAssociation[]  = [];
        const sdHypIdsToPublish: string[] = [];

        // Walk the list of signal detection ids to found the association and modified reject flag.
        // If we don't find the association add it if we are to associate else complain
        signalDetectionHypoIds.forEach(sdHypoId => {
            const sdHypo = signalDetectionProcessor.getSignalDetectionHypothesisById(sdHypoId);
            // If the Signal Detection Hypo is in the SD Processor cache then add the association
            if (associate) {
                if (sdHypo !== undefined) {
                    associations.push({
                        id: uuid4().toString(),
                        eventHypothesisId,
                        rejected: false,
                        signalDetectionHypothesisId: sdHypoId
                    });
                    sdHypIdsToPublish.push(sdHypoId);
                    signalDetectionProcessor.setSignalDetectionAssociationChanged(sdHypoId, true);

                } else {
                    logger.warn(`Unable unassociate Signal Detection Hypothesis Id ${sdHypoId} : does not exist.`);
                }
            } else {
                const assocIndex = associations.findIndex(assoc => assoc.signalDetectionHypothesisId === sdHypoId);
                if (assocIndex !== -1) {
                    // Association exists so check if we are unassociated and splice it
                    const removedAssocs = associations.splice(assocIndex, 1)[0]; // will always return 1
                    removedAssocs.rejected = true;
                    removedAssociations.push(removedAssocs);
                    sdHypIdsToPublish.push(sdHypoId);
                    signalDetectionProcessor.setSignalDetectionAssociationChanged(sdHypoId, true);
                }
            }
            // Update the location behaviors
            if (sdHypo) {
                // Add location behaviors to the event from preferred solution if it exists
                // Will be skipped when event is first created
                if (event.currentEventHypothesis.eventHypothesis.preferredLocationSolution) {
                    const locBehaviors = event.currentEventHypothesis.eventHypothesis.
                        preferredLocationSolution.locationSolution.locationBehaviors;
                    event.currentEventHypothesis.eventHypothesis.preferredLocationSolution.
                    locationSolution.locationBehaviors = this.updateLocationBehavoirs(sdHypo, locBehaviors, associate);
                }
            }
        });

        // Check if changes were made and update the hypothesis and event as necessary
        let sdsToPublish = [];
        if (sdHypIdsToPublish.length > 0) {
            if (event.modified) {
                eventHypo.associations = associations;
            } else {
                const newHypo = this.createNewEventHypothesis(event, associations);
                associations = newHypo.associations;
            }
            sdsToPublish = signalDetectionProcessor.getSdsToPublish(sdHypIdsToPublish);
        }

        // Publish removed association events before rebuilding the map
        const eventsWithRemovedAssoc = this.calculateEventsToPublish(removedAssociations);

        // Rebuild the map and then publish new associations
        this.rebuildSignalDetectionHypoToEventHypoMap();
        const eventsWithNewAssoc =
            this.calculateEventsToPublish(
                event.currentEventHypothesis.eventHypothesis.associations.filter(
                    a => includes(sdHypIdsToPublish, a.signalDetectionHypothesisId)));

        return {
            events: uniqBy([...eventsWithRemovedAssoc, ...eventsWithNewAssoc, event], e => e.id),
            sds: sdsToPublish
        };
    }

    /**
     * Retrieve the preferred event hypothesis for the provided processing stage and
     * event with the provided ID.
     * @param eventId The ID of the event to locate the preferred hypothesis for
     * @param stageId The ID of the processing stage to locate the preferred hypothesis for
     */
    public async getPreferredHypothesisForStage(
        eventId: string, stageId: string): Promise<model.PreferredEventHypothesis> {
        // Find the preferred hypothesis object with the provided stage ID for the event with the provided ID
        const event = await this.getEventById(eventId);
        if (event) {
            return find(event.preferredEventHypothesisHistory, { processingStageId: stageId });
        }
        return undefined;
    }

    /**
     * Retrieve all EventHypotheses that have an association to the Signal Detection Hypothesis
     * @param sdHypothesisId The ID of the Signal Detection Hypothesis
     * 
     * @returns List of EventHypotheses
     */
    public getAssociatedEventHypotheses(sdHypothesisId: string): model.EventHypothesis[] {
        return this.sdhToEventHypothesesMap.get(sdHypothesisId) || [];
    }

    /**
     * Add a new SignalDetectionHypothesis association to the event hypothesis.
     */
    public updateSignalDetectionAssociation(
        newSignalDetectionHypoId: string, prevSignalDetectionHypoId: string): model.Event[] {
        // Update the new SDHypo Id in each association
        const eventHypoList: model.EventHypothesis[] =
            this.sdhToEventHypothesesMap.get(prevSignalDetectionHypoId) || [];
        const events = [];
        eventHypoList.forEach(eventHyp => {
            let eventHypToUpdate = eventHyp;
            const event = this.getCachedEventById(eventHyp.eventId);
            if (!event.modified) {
                eventHypToUpdate = this.createNewEventHypothesis(event);
            }
            eventHypToUpdate.associations.forEach(assoc => {
                if (assoc.signalDetectionHypothesisId === prevSignalDetectionHypoId) {
                    assoc.signalDetectionHypothesisId = newSignalDetectionHypoId;
                }
            });
            events.push(event);
        });

        // Update the sdhToEventHypothesesMap to remove old SDHypo entry with new
        this.rebuildSignalDetectionHypoToEventHypoMap();
        return events;
    }

    /**
     * Rejects the associations for the sd hyp that has been rejected
     * @param sdHypId Signal detection hypothesis to reject
     */
    public rejectAssociationForSDHyp(sdHypId: string): model.Event[] {
        const eventHyps = this.sdhToEventHypothesesMap.get(sdHypId);
        if (!eventHyps || eventHyps.length <= 0) {
            return [];
        }
        const eventsChanged = [];
        eventHyps.forEach(eventHyp => {
            const foundAssoc = eventHyp.associations.find(assoc => assoc.signalDetectionHypothesisId === sdHypId);
            if (foundAssoc) {
                foundAssoc.rejected = true;
                const eventChanged = this.getEventByHypId(foundAssoc.eventHypothesisId);
                this.createNewEventHypothesis(eventChanged);
                eventsChanged.push(eventChanged);
            }
        });
        this.rebuildSignalDetectionHypoToEventHypoMap();
        return eventsChanged;
    }

    /**
     * Requests the Feature Predictions in the LocationSolutions be computed by
     * Signal Detection streaming service
     * @param Event to be processed
     * 
     * @returns Event with updated feature predictions for each default phase
     */
    public async computeFeaturePredictions(event: model.Event): Promise<model.Event> {
        const currentPrefEventHypo = this.getCurrentEventHypothesisByEventId(event.id);
        // Lookup the sourceLocation
        const sourceLocationOSD: osdModel.LocationSolutionOSD = convertLocationSolutionToOSD(
            currentPrefEventHypo.eventHypothesis.preferredLocationSolution.locationSolution);

        // Lookup all channels for default stations
        // TODO fix this - this is a hack for testing
        const channels: ProcessingChannel[] = convertOSDChannel(stationProcessor.getDefaultChannels());

        const featurePredictionInput: model.FeaturePredictionStreamingInput = {
            featureMeasurementTypes: [
                FeatureMeasurementTypeName.ARRIVAL_TIME,
                FeatureMeasurementTypeName.SLOWNESS,
                FeatureMeasurementTypeName.RECEIVER_TO_SOURCE_AZIMUTH
            ],
            sourceLocation: {
                ...sourceLocationOSD,
                locationRestraint: {
                    depthRestraintType: model.DepthRestraintType.UNRESTRAINED,
                    depthRestraintKm: 10,
                    latitudeRestraintType: model.RestraintType.UNRESTRAINED,
                    latitudeRestraintDegrees: 11,
                    longitudeRestraintType: model.RestraintType.UNRESTRAINED,
                    longitudeRestraintDegrees: 12,
                    timeRestraintType: model.RestraintType.FIXED,
                    timeRestraint: '2018-01-01T01:15:50Z'
                },
                locationUncertainty: undefined,
                locationBehaviors: [{
                    residual: 4,
                    weight: 5,
                    defining: false,
                    featurePredictionId: '21111111-1111-1111-1111-111111111111',
                    featureMeasurementId: '31111111-1111-1111-1111-111111111111'
                }],
            },
            receiverLocations: channels,
            phase: PhaseType[PhaseType.P],
            model: 'ak135',
            corrections: [
                // TODO most phases break the service with these corrections. Add once fixed
                // {
                //     correctionType: model.FeaturePredictionCorrectionType.ELLIPTICITY_CORRECTION,
                //     usingGlobalVelocity: true
                // },
                // {
                //     correctionType: model.FeaturePredictionCorrectionType.ELEVATION_CORRECTION,
                //     usingGlobalVelocity: false
                // }
            ],
            processingContext: {
                analystActionReference: {
                    processingStageIntervalId: workflowProcessor.getCurrentStageIntervalId(),
                    processingActivityIntervalId: workflowProcessor.getCurrentOpenActivityId(),
                    analystId: 'ca6ea2b2-03e4-4571-a51a-347c6fcb75b2'
                },
                processingStepReference: undefined,
                storageVisibility: 'PRIVATE'
            }
        };

        // Call compute FP helper function
        // TODO: At some point do we need to call FP for each
        // TODO: of the LS types (surface, depth, unrestrained)?
        const locationSolutions: model.LocationSolution[] = [];

        // tslint:disable-next-line: forin no-for-in-array
        for (const index in this.defaultPhases) {
            const sdPhase = this.defaultPhases[index];
            featurePredictionInput.phase = sdPhase;
            const locSolution = await this.computeFeaturePrediction(
                featurePredictionInput, event.currentEventHypothesis.eventHypothesis);
            // TODO: we need to be better about not erasing fields
            if (locSolution) locationSolutions.push(locSolution);
        }

        // Set all the FPs from each compute call into one FP list and set it on the preffered LS
        const fpList: model.FeaturePrediction[] = [];
        locationSolutions.forEach(ls => ls.featurePredictions.forEach(fp => fpList.push(fp)));
        currentPrefEventHypo.eventHypothesis.preferredLocationSolution.locationSolution.featurePredictions = fpList;
        return event;
    }

    /**
     * Check for conflicts based on event
     * @param event event to check for conflicts
     */
    public checkForEventConflict(event: model.Event): boolean {
        let eventHyp = event.currentEventHypothesis;
        if (!eventHyp) {
            eventHyp = this.getCurrentEventHypothesisByEventId(event.id);
        }
        const sdHypIds = eventHyp.eventHypothesis.associations
            .map(assoc => assoc.signalDetectionHypothesisId);
        return this.checkForConflict(sdHypIds);
    }

    /**
     * Returns list of conflicting sd hypotheses
     * @param event event to check for conflicts
     */
    public getConflictingSdHyps(event: model.Event): string[] {
        let eventHyp = event.currentEventHypothesis;
        if (!eventHyp) {
            eventHyp = this.getCurrentEventHypothesisByEventId(event.id);
        }
        const sdHypIds = eventHyp.eventHypothesis.associations
            .map(assoc => assoc.signalDetectionHypothesisId);
        return this.getConflicts(sdHypIds);
    }

    /**
     * Check for conflicts based on SD Hyp Id
     * @param sdHyp sd hyp to check for conflicts
     */
    public checkForSDHypConflict(sdHypId: string): boolean {
        return this.checkForConflict([sdHypId]);
    }

    /**
     * Returbs the current event hypotesis
     * @param preferredEventHypothesisHistory 
     * @returns PreferredEventHypothesis[] 
     */
    private getCurrentEventHypothesis(preferredEventHypothesisHistory: model.PreferredEventHypothesis[]):
    model.PreferredEventHypothesisHistoryAndHypothesis {
       // Lookup the preferred event for the current stage id.
       // If not found copy the previous stage's preferred (still todo for now copy last in list)
       const stageId = workflowProcessor.getCurrentStageId();
       let currentPrefEventHypo: model.PreferredEventHypothesis =
           preferredEventHypothesisHistory[preferredEventHypothesisHistory.length - 1];
       // If not set then not processing a new interval
       if (stageId) {
           const stagePrefHypothesis: model.PreferredEventHypothesis = preferredEventHypothesisHistory.
               find(preHypo => preHypo.processingStageId === stageId);
           if (stagePrefHypothesis) {
               currentPrefEventHypo = stagePrefHypothesis;
           } else {
               currentPrefEventHypo = {
                   ...currentPrefEventHypo,
                   processingStageId: stageId
               };
               preferredEventHypothesisHistory.push(currentPrefEventHypo);
           }
       }
       return {preferredEventHypothesisHistory, currentPrefEventHypo};
    }

    /**
     * Compute new LocationSolution
     * @param osdEventHypo 
     * @param parameters
     * @returns LocationSolution list
     */
    private async computeLocationSolution(osdEventHypo: osdModel.EventHypothesisOSD,
                                          signalDetections: SignalDetection[], parameters: model.LocateEventParameter):
            Promise<model.LocationSolution[]> {
        const requestConfig = this.settings.backend.services.locateEvent.requestConfig;
        const query = {
            eventHypotheses: [osdEventHypo],
            signalDetections,
            parameters
        };

        try {
            const responseData = await this.httpWrapper.request(requestConfig, query);
            // If the call is empty then it failed (caught by HttpWrapper)
            if (responseData && isObjectEmpty(responseData)) {
                logger.warn(`LocateEvent endpoint call failed no results returned.`);
                // tslint:disable-next-line:max-line-length
                logger.debug(`Location Solution sending service request: ${JSON.stringify(requestConfig, undefined, 2)} query: ${JSON.stringify(query, undefined, 2)}`);
                return [];
            }

            if (!responseData) return [];
            // Only process first map entry (should be only one map entry with one LocationSolution)
            for (const key in responseData) {
                if (!responseData.hasOwnProperty(key)) {
                    continue;
                }

                // Convert OSD Location Solutions
                const locationSolutions: model.LocationSolution[] = [];
                const locationSolutionOSDs: osdModel.LocationSolutionOSD[] = responseData[key];
                if (locationSolutionOSDs) {
                    locationSolutionOSDs.forEach(losOSD => {
                        const locationSolution: model.LocationSolution =
                            convertLocationSolutionFromOSD(losOSD, osdEventHypo.associations);
                        locationSolutions.push(locationSolution);
                    });
                }
                return locationSolutions;
            }
        } catch (e) {
            logger.warn(`Problem calling Locate Event error ${e}`);
        }
        return [];
    }

    /**
     * Helper that gets the default populated Locate Event Parameters from the Configuration
     * and returns it ready for Locate Event call.
     * @param depthRestraintType 
     * @param depth
     * @returns the populated Locate Event parameters for compute call
     */
    private buildEventLocationParameters(depthRestraintType: model.DepthRestraintType, depth: number):
        model.LocateEventParameter {
        // Get the Locate Event Parameters used by locateEvent call (Event Definition Type)
        const eventLocationParam: model.LocateEventParameter =
            configProcessor.getConfigByKey(this.settings.eventLocationParameters);
        // TODO don't use hardcoded zero element here - although currently this is all the service looks at
        eventLocationParam.eventLocationDefinition.locationRestraints[0].depthRestraintType = depthRestraintType;
        eventLocationParam.eventLocationDefinition.locationRestraints[0].depthRestraintKm =
            // tslint:disable-next-line:no-null-keyword
            depth !== undefined ? depth : null;
        return eventLocationParam;
     }
    /**
     * Return an updated set of Location Behaviors based on SD association/disassociation
     * @param sdHypo 
     * @param locBehaviors 
     * @param associate 
     * 
     * @returns The updated location behaviors list to replace new on
     */
    private updateLocationBehavoirs(sdHypo: SignalDetectionHypothesis, locBehaviors: model.LocationBehavior[],
                                    associate: boolean): model.LocationBehavior[] {
        const updatedLocationBehaviors: model.LocationBehavior[] = cloneDeep(locBehaviors);
        // For each Arrival, Azimuth or Slowness find the FM. If we need to associate and not already
        // in the list create a new entry. If remove then don't add to new list (might deep copy and remove)
        sdHypo.featureMeasurements.forEach(fm => {
            if (fm.featureMeasurementType === FeatureMeasurementTypeName.ARRIVAL_TIME ||
            fm.featureMeasurementType === FeatureMeasurementTypeName.RECEIVER_TO_SOURCE_AZIMUTH ||
            fm.featureMeasurementType === FeatureMeasurementTypeName.SOURCE_TO_RECEIVER_AZIMUTH ||
            fm.featureMeasurementType === FeatureMeasurementTypeName.SLOWNESS) {
                // Find the location behavior index. If associate and not in list add it,
                // else if disassociate and in the list remove it.
                const behaviorIndex = updatedLocationBehaviors.findIndex(
                    locBeh => locBeh.featureMeasurementId === fm.id);
                if (behaviorIndex === -1 && associate) {
                    const newLoc: model.LocationBehavior = {
                        defining: false,
                        featureMeasurementId: fm.id,
                        featurePredictionId: uuid4().toString(), // TODO: sould we find/call for FP?
                        residual: 0, // TODO: what should this be?
                        weight: 0 // TODO: what should this be?
                    };
                    updatedLocationBehaviors.push(newLoc);
                } else if (behaviorIndex >= 0 && !associate) {
                    updatedLocationBehaviors.splice(behaviorIndex, 1);
                }
            }
        });
        return updatedLocationBehaviors;
    }

    /**
     *  Helper function that converts OSD event to a gateway/UI event
     */
    private convertEventOSD(eventOSDs: osdModel.EventOSD): model.Event {
        const eventHyps = eventOSDs.hypotheses.map(convertEventHypothesisFromOSD);

        // hydrate finaleventgist
        const finalEventHypothesisHistory = eventOSDs.finalEventHypothesisHistory.
            map(feh => eventHyps.find(eHyp => eHyp && eHyp.id === feh.eventHypothesisId));
        let preferredEventHypothesisHistory: model.PreferredEventHypothesis[] =
            eventOSDs.preferredEventHypothesisHistory.
            map(pEventHyp => {
                const eventHypothesis = eventHyps.find(eHyp => eHyp.id === pEventHyp.eventHypothesisId);
                return {
                    eventHypothesis,
                    processingStageId: pEventHyp.processingStageId
                };
            });
        // give it a status
        const status = model.EventStatus.ReadyForRefinement;
        // give it empy analyst suer names
        const activeAnalystUserNames = [];
        const currentAndPreferred = this.getCurrentEventHypothesis(preferredEventHypothesisHistory);
        preferredEventHypothesisHistory = currentAndPreferred.preferredEventHypothesisHistory;
        const currentEventHypothesis = currentAndPreferred.currentPrefEventHypo;
        const event: model.Event = {
            id: eventOSDs.id,
            monitoringOrganization: eventOSDs.monitoringOrganization,
            preferredEventHypothesisHistory,
            finalEventHypothesisHistory,
            hypotheses: eventHyps,
            status,
            activeAnalystUserNames,
            currentEventHypothesis,
            modified: false
        };
        return event;
    }

    /**
     * Helper function to retrieve event hyps for each SD hyp to check for conflicts
     * @param sdHypIds list of SD hyps to check if there are conflicts for
     */
    private checkForConflict(sdHypIds: string[]): boolean {
        let conflictFound = false;
        sdHypIds.forEach(hypId => {
            const eventsForSD = this.sdhToEventHypothesesMap.get(hypId);
            if (eventsForSD && eventsForSD.length > 1) {
                conflictFound = true;
            }
        });
        return conflictFound;
    }
    /**
     * Helper function to retrieve event hyps for each SD hyp to check for conflicts
     * @param sdHypIds list of SD hyps to check if there are conflicts for
     */
    private getConflicts(sdHypIds: string[]): string[] {
        const conflictingHyps = [];
        sdHypIds.forEach(hypId => {
            const eventsForSD = this.sdhToEventHypothesesMap.get(hypId);
            if (eventsForSD && eventsForSD.length > 1) {
                conflictingHyps.push(hypId);
            }
        });
        return conflictingHyps;
    }
    /**
     * Set Location for current event hyp for a newly created event
     * @param event event to set location for
     * @param sdHypIds ids of sds that will be associated to the event
     */
    private setNewEventHypothesisLocation(event: model.Event, sdHypIds: string[]) {
        // TODO this will eventually call a location service or mock backend
        const eventLocation = getNewRandomEventLocation(
            sdHypIds, workflowProcessor.getCurrentTimeInterval().startTime + 1);

        // Create the location solution and preferred location solution objects
        const locationSolution: model.LocationSolution = {
            id: uuid4().toString(),
            featurePredictions: [],
            location: eventLocation,
            locationBehaviors: [],
            locationRestraint: {
                depthRestraintType: model.DepthRestraintType.UNRESTRAINED,
                depthRestraintKm: 0,
                latitudeRestraintType: model.RestraintType.UNRESTRAINED,
                latitudeRestraintDegrees: 0,
                longitudeRestraintType: model.RestraintType.UNRESTRAINED,
                longitudeRestraintDegrees: 0,
                timeRestraintType: model.RestraintType.UNRESTRAINED,
                timeRestraint: ''
            },
            snapshots: makeSignalDetectionSnapshots(event.currentEventHypothesis.eventHypothesis.associations, [])
        };
        const preferredLocationSolution: model.PreferredLocationSolution = {
            creationInfo: getCreationInfo('1'),
            locationSolution
        };

        // Set preferred location solution and add solution to the list
        // TODO Use Locate Event to create full solution set
        event.currentEventHypothesis.eventHypothesis.locationSolutionSets
            .push(createLocationSolutionSet(event.currentEventHypothesis.eventHypothesis, [locationSolution]));
        event.currentEventHypothesis.eventHypothesis.preferredLocationSolution = preferredLocationSolution;
    }

    /**
     * Helper method makes each individual FP Service call
     * to Compute FeaturePrediction called by computeFeaturePredictions public method
     * 
     * @param requestConfig HttpWrapper Configuration
     * @param input FP Service Request Body
     * 
     * @returns The location solution with feature prediction list populated
     */
    private async computeFeaturePrediction(input: model.FeaturePredictionStreamingInput,
                                           eventHyp: model.EventHypothesis
                                          ): Promise<model.LocationSolution> {
        const requestConfig = this.settings.backend.services.computeFeaturePredictions.requestConfig;
        // tslint:disable-next-line:max-line-length
        logger.debug(`ComputeFP sending service request: ${JSON.stringify(requestConfig, undefined, 2)} query: ${JSON.stringify(input, undefined, 2)}`);
        performanceLogger.performance('computeFeaturePredictions', 'requestedFromService', input.sourceLocation.id);
        const locationSolutionOSD = await this.httpWrapper.request(requestConfig, input);
        performanceLogger.performance('computeFeaturePredictions',
                                      'returnedFromService', input.sourceLocation.id);
        let locationSolution: model.LocationSolution;
        const emptyLocationSolution = !locationSolutionOSD || isObjectEmpty(locationSolutionOSD);
        if (!emptyLocationSolution) {
            locationSolution = convertLocationSolutionFromOSD(locationSolutionOSD, eventHyp.associations);
            locationSolution.featurePredictions =
                convertFeaturePredictionsFromOSD(locationSolutionOSD.featurePredictions);
        } else {
            logger.warn(`LocationSolution returned from FP Service is not defined!`);
        }
        return locationSolution;
    }

    /**
     * Rebuild Signal Detection Hypothesis to Event Hypothesis Map
     */
    private rebuildSignalDetectionHypoToEventHypoMap() {
        // Clear the map to repopulate
        this.sdhToEventHypothesesMap.clear();

        // Walk thru each eventHypothesis' associations and add the eventHypothesis
        // to the entry's list for the Signal Detection Hypo's Id.
        this.eventCache.forEach((event: model.Event) => {
            this.getCurrentEventHypothesisByEventId(event.id);
            this.addEventHypothesisToSDHypoMap(event.currentEventHypothesis.eventHypothesis);
        });
    }

    /**
     * sdhToEventHypothesesMap Helper (can be used from multiple places) to check and add the map 
     * entries based on the SD associations
     * @param The EventHypothesis to process
     */
    private addEventHypothesisToSDHypoMap(eventHypo: model.EventHypothesis) {
        eventHypo.associations.forEach(assoc => {
            let eventHypoList = this.sdhToEventHypothesesMap.get(assoc.signalDetectionHypothesisId);

            // If event hypo list not found add it to the map as an empty array
            if (!eventHypoList) {
                eventHypoList = [];
                this.sdhToEventHypothesesMap.set(assoc.signalDetectionHypothesisId, eventHypoList);
            }

            // Lookup EventHypothesis and check if not in the map entry for the SD Hypo
            if (!eventHypoList.find(evth => evth.id === eventHypo.id)) {
                eventHypoList.push(eventHypo);
            }
        });
    }

    /**
     * Calculates the events to publish 
     * @param associations associations to the event
     * @returns an event[]
     */
    private calculateEventsToPublish(associations: model.SignalDetectionEventAssociation []): model.Event[] {
        // Check updated event to publish other events effected

        const eventsToPublish = [];
        associations.forEach(assoc => {
            const eventHyps = this.sdhToEventHypothesesMap.get(assoc.signalDetectionHypothesisId);
            if (eventHyps && eventHyps.length > 1) {
                eventHyps.forEach(hyp => {
                    const event = this.getCachedEventById(hyp.eventId);
                    eventsToPublish.push(event);
                });
            }
        });

        return eventsToPublish;
    }
}

/**
 * Export an initialized instance of the Event Processor.
 */
export const eventProcessor: EventProcessor = new EventProcessor();
eventProcessor.initialize();

// tslint:disable-next-line:max-file-line-count
