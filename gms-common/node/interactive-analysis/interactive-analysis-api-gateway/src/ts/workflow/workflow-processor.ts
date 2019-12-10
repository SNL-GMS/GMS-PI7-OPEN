import * as config from 'config';
import { configProcessor } from '../config/config-processor';
import { find, filter } from 'lodash';
import * as uuid4 from 'uuid/v4';
import { HttpClientWrapper } from '../util/http-wrapper';
import * as workflowBackend from './workflow-mock-backend';
import { gatewayLogger as logger, gatewayLogger } from '../log/gateway-logger';
import { intervalCreated } from './resolvers';
import * as model from './model';
import { epochSecondsNow } from '../util/time-utils';
import { eventProcessor } from '../event/event-processor';
import { signalDetectionProcessor } from '../signal-detection/signal-detection-processor';
import { TimeRange } from '../common/model';

/**
 * API gateway processor for workflow data APIs. This class supports:
 * - data fetching & caching from the backend service interfaces
 * - mocking of backend service interfaces based on test configuration
 * - session management
 * - GraphQL query resolution from the user interface client
 */
class WorkflowProcessor {

    /** Local configuration settings */
    private settings: any;

    /** Contains the data structures for Workflow */
    private workflowCache: model.WorkflowDataCache;

    /** HTTP client wrapper for communicationg with backend services */
    private httpWrapper: HttpClientWrapper;

    /** Interval time values for creation of new intervals */
    private intervalCreationStartTimeSec: number;

    /** How many seconds is the interval duration (default 2 hrs) */
    private intervalDurationSec: number = 7200;

    /** Interval Elapsed secs */
    private intervalElapsedSecs: number = 0;

    /** How often to create new workflow interval (default 2 hrs) */
    private createIntervalDurationSec: number = 7200;

    /** Id string of the open actiivity if one is open */
    private currentlyOpenActivity: model.ProcessingActivityInterval = undefined;

    /** Current start time */
    private currentStartTime: number = 0;

    /** Current end time */
    private currentEndTime: number = 1;

    /**
     * Constructor - initialize the processor, loading settings and initializing
     * the HTTP client wrapper.
     */
    public constructor() {

        // Load configuration settings
        this.settings = config.get('workflow');

        // Initialize an http client
        this.httpWrapper = new HttpClientWrapper();

        this.workflowCache = {
            stages: [],
            activities: [],
            intervals: [],
            stageIntervals: [],
            analysts: [],
            activityIntervals: []
        };
    }

    /**
     * Initialize the class' http wrapper, start the create interval timer and populate the workflow cache
     */
    public async initialize(): Promise<void> {

        // If service mocking is enabled, initialize the mock backend
        if (this.settings.backend.mock.enable) {
            workflowBackend.initialize(this.httpWrapper.createHttpMockWrapper());
        }
        // Load service configuration settings
        const serviceConfig = config.get('workflow.intervalService');
        this.intervalCreationStartTimeSec = serviceConfig.intervalCreationStartTimeSec;
        this.intervalDurationSec = serviceConfig.intervalDurationSec;
        this.createIntervalDurationSec = serviceConfig.createIntervalDurationSec;

        // Call to populate workflowCache
        const requestConfig = this.settings.backend.services.workflowData.requestConfig;
        this.workflowCache = await this.httpWrapper.request(requestConfig);

        this.intervalCreationStartTimeSec = serviceConfig.intervalCreationStartTimeSec;
        this.intervalElapsedSecs = epochSecondsNow();
        const normalized =
            this.normalizeStartEndTimes(serviceConfig.mockedIntervalStartTimeSec,
                                        this.intervalCreationStartTimeSec);
        this.currentStartTime = normalized.startTime;
        this.currentEndTime = normalized.endTime;

        /* Starts the interval service stub, starting a timer to create
        and publish new ProcessingStageIntervals periodically.
        Set a timer to create stage intervals periodically based on the configured interval span */
        setInterval(this.createInterval, serviceConfig.intervalCreationFrequencyMillis);
    }

    /**
     * Sets the new time range
     * @param startTimeSec the start time in epoch seconds
     * @param endTimeSec the end time in epoch seconds
     */
    public setNewTimeRange = (startTimeSec: number, endTimeSec: number) => {
            logger.info(`Setting new Start Time: ${startTimeSec} and End Time: ${endTimeSec}`);
            const normalizedTimes = this.normalizeStartEndTimes(startTimeSec, endTimeSec);
            this.currentStartTime = normalizedTimes.startTime;
            this.currentEndTime = normalizedTimes.endTime;

            let intervalStartSec = normalizedTimes.startTime;
            let intervalEndSec = normalizedTimes.endTime;
            const totalDuration = normalizedTimes.endTime - normalizedTimes.startTime;
            intervalEndSec = normalizedTimes.startTime + this.intervalDurationSec;
            for (let i = 0; i < (Math.trunc(totalDuration / this.intervalDurationSec) + 1); i++) {
                // check to see if interval exists in  given time range
                if (!this.workflowCache.intervals.find(interval =>
                    interval.startTime >= intervalStartSec && interval.endTime <= intervalEndSec
                )) {
                    this.populateInterval(intervalStartSec, intervalEndSec);
                }
                intervalStartSec += this.intervalDurationSec;
                intervalEndSec += this.intervalDurationSec;
            }
    }

    /**
     * Retrieves current open activity Id.
     * @returns the current open activity's id or empty string if none open
     */
    public getCurrentOpenActivityId(): string {
        if (this.currentlyOpenActivity) {
            return this.currentlyOpenActivity.id;
        }
        return '';
    }

    /**
     * Retrieves current open stage interval Id.
     * @returns the current open stage interval's id or empty string if none open
     */
    public getCurrentStageIntervalId(): string {
        if (this.currentlyOpenActivity) {
            return this.currentlyOpenActivity.stageIntervalId;
        }
        return '';
    }

    /**
     * Retrieves current open stage Id.
     * @returns the current open stage interval's id or empty string if none open
     */
    public getCurrentStageId(): string {
        if (this.currentlyOpenActivity) {
            return this.getStage(this.currentlyOpenActivity.stageIntervalId).id;
        }
        return '';
    }

    /**
     * Returns the current time interval
     * @returns a TimeRange
     */
    public getCurrentTimeInterval(): TimeRange {
        return {
            startTime: this.currentStartTime,
            endTime: this.currentEndTime
        };
    }

    /**
     * Retrieves the list of analysts defined for the interactive analysis.
     * @returns Analyst as promise
     */
    public async getAllAnalysts(): Promise<model.Analyst[]> {
        return this.workflowCache.analysts;
    }

    /**
     * Retrieves the list of analysts whose user names are in the provided list.
     * @param analystUserNames The list of user names for which to retrieve analysts
     * @returns an Analyst[] as a promise
     */
    public async getAnalysts(analystUserNames: string[]): Promise<model.Analyst[]> {
        if (!analystUserNames) {
            return undefined;
        }
        return filter(this.workflowCache.analysts, (analyst: model.Analyst) =>
            analystUserNames.indexOf(analyst.userName) > -1);
    }

    /**
     * Retrieves the analyst with the provided user name.
     * @param userName The unique username of the analyst to retrieve
     * @returns an Analyst as a promise
     */
    public async getAnalyst(userName: string): Promise<model.Analyst> {
        return find(this.workflowCache.analysts, { userName });
    }

    /**
     * Retrieves the list of processing stages defined for the interactive analysis.
     * @returns a ProcessingStage[] as a promise
     */
    public async getStages(): Promise<model.ProcessingStage[]> {
        logger.debug(`Getting stages from ${this.currentStartTime} to ${this.currentEndTime}`);
        return this.workflowCache.stages;
    }

    /**
     * Retrieves the processing stage with the provided ID string.
     * @param id The unique ID of the processing stage to retrieve
     * @returns a ProcessingStage
     */
    public getStage(id: string): model.ProcessingStage {
        const stage = find(this.workflowCache.stages, { id });
        // TODO this should be looked at again once we meaning to our stages and intervals (backend integration)
        return stage ? stage : this.workflowCache.stages[0];
    }

    /**
     * Retrieves the processing stage with the provided name string.
     * @param name The name of the processing stage to retrieve
     * @returns a ProcessingStage as a promoise
     */
    public async getStageByName(name: string): Promise<model.ProcessingStage> {
        return find(this.workflowCache.stages, { name });
    }

    /**
     * Retrieves the processing intervals in the provided time range.
     * @param startTime The start time of the range for which to retrieve intervals
     * @param endTime The end time of the range for which to retrieve intervals
     * @returns a ProcessingInterval[] as a promise
     */
    public async getIntervalsInRange(startTime: number, endTime: number):
        Promise<model.ProcessingInterval[]> {
        const normalizedTime = this.normalizeStartEndTimes(startTime, endTime);
        const intervals = filter(this.workflowCache.intervals, interval =>
            (interval.startTime >= normalizedTime.startTime &&
                interval.endTime <= normalizedTime.endTime));
        return intervals;
    }

    /**
     * Retrieves the processing interval with the provided ID string.
     * @param id The unique ID of the processing interval to retrieve
     * @returns a ProcessingInterval as a promise
     */
    public async getInterval(id: string): Promise<model.ProcessingInterval> {
        const found = find(this.workflowCache.intervals, { id });
        return found;
    }

    /**
     * Retrieves the list of processing stage intervals which are within the workflows time range
     * @returns a ProcessingStageInterval[] as a promise
     */
    public async getStageIntervals(): Promise<model.ProcessingStageInterval[]> {
        return filter(this.workflowCache.stageIntervals, stageInterval =>
            (stageInterval.startTime >= this.currentStartTime && stageInterval.endTime <= this.currentEndTime))
            .sort((a, b) => (a.startTime - b.endTime));
    }

    /**
     * Retrieves the processing stage interval with the provided ID string.
     * @param id The unique ID of the processing stage interval to retrieve
     * @returns a ProcessingStageInterval as a promise
     */
    public async getStageInterval(id: string): Promise<model.ProcessingStageInterval> {
        return find(this.workflowCache.stageIntervals, { id });
    }

    /**
     * Retrieves the list of processing activities defined for interactive processing.
     * @returns a ProcessingActivity[] as a promise
     */
    public async getActivities(): Promise<model.ProcessingActivity[]> {
        return this.workflowCache.activities;
    }

    /**
     * Retrieves the processing activity with the provided ID string.
     * @param id The unique ID of the processing activity to retrieve
     * @returns a ProcessingActivity as a promise
     */
    public async getActivity(id: string): Promise<model.ProcessingActivity> {
        return find(this.workflowCache.activities, { id });
    }

    /**
     * Retrieves the list of processing activity intervals.
     * @returns a ProcessingActivityInterval[] as a promise
     */
    public async getActivityIntervals(): Promise<model.ProcessingActivityInterval[]> {
        return this.workflowCache.activityIntervals;
    }

    /**
     * Retrieves the processing activity interval with the provided ID string.
     * @param id The unique ID of the processing activity interval to retrieve
     * @returns a ProcessingActivityInterval as a promise
     */
    public async getActivityInterval(id: string): Promise<model.ProcessingActivityInterval> {
        return find(this.workflowCache.activityIntervals, { id });
    }

    /**
     * Retrieves the processing stage intervals in the provided time range.
     * @param startTime The start time of the range for which to retrieve stage intervals
     * @param endTime The end time of the range for which to retrieve stage intervals
     * @returns a ProcessingStageInterval[] as a promise
     */
    public async getStageIntervalsInRange(
        startTime: number, endTime: number): Promise<model.ProcessingStageInterval[]> {
        const normalizedTime = this.normalizeStartEndTimes(startTime, endTime);
        return filter(this.workflowCache.stageIntervals, stageInterval =>
            (stageInterval.startTime >= normalizedTime.startTime && stageInterval.endTime <= normalizedTime.endTime));
    }

    /**
     * Updates the processing stage interval object with the provided unique ID
     * to reflect the status information in the input parameter, including
     * interval status and the id of the Analyst requesting the update.
     * @param stageIntervalId The unique ID of the stage interval to mark
     * @param input The marking input to apply to the stage interval
     * @returns a ProcessingStageInterval as promise
     */
    public async markStageInterval(stageIntervalId: string, input: any): Promise<model.ProcessingStageInterval> {
        const stageInterval = find(this.workflowCache.stageIntervals, { id: stageIntervalId });
        if (!stageInterval) {
            throw new Error(`Couldn't find Processing Stage Interval with ID ${stageIntervalId}`);
        }
        // Check that the status update is valid for the provided ProcessingStageInterval and
        // associated ProcessingActivityIntervals (throw an error otherwise)
        this.validateStageIntervalStatus(stageInterval, input.status);

        // If the status is InProgress, update the status of each activity interval and
        // Add the provided analyst user name to the list of active analysts (if not already in the list)
        if (input.status === model.IntervalStatus.InProgress) {
            filter(this.workflowCache.activityIntervals, { stageIntervalId: stageInterval.id })
                .forEach((currentValue, index) => {
                    this.updateActivityIntervalStatus(currentValue, input.status, input.analystUserName);
                });
        }

        // Update the stage status
        this.updateStageIntervalStatus(stageInterval, input.status, input.analystUserName);
        return stageInterval;
    }

    /**
     * Updates the processing activity interval object with the provided unique ID
     * to reflect the status information in the input parameter, including
     * interval status and the id of the Analyst requesting the update.
     * @param stageIntervalId The unique ID of the stage interval to mark
     * @param input The marking input to apply to the stage interval
     * @returns a ProcessingActivityInterval as a promise
     */
    public async markActivityInterval(
        activityIntervalId: string, input: any): Promise<model.ProcessingActivityInterval> {
        const activityInterval = find(this.workflowCache.activityIntervals, { id: activityIntervalId });
        if (!activityInterval) {
            throw new Error(`Couldn't find Processing Activity Interval with ID ${activityIntervalId}`);
        }

        // Check that the transition to the input status is valid for the provided ProcessingActivityInterval
        this.validateActivityIntervalStatus(activityInterval, input.status);

        // Update the activity interval status
        this.updateActivityIntervalStatus(activityInterval, input.status, input.analystUserName);

        if (input.status === model.IntervalStatus.InProgress) {

            // Find the parent stage and update its status
            const stage = find(this.workflowCache.stageIntervals, { id: activityInterval.stageIntervalId });
            this.updateStageIntervalStatus(stage, input.status, input.analystUserName);
            this.currentlyOpenActivity = activityInterval;
        }

        return activityInterval;
    }

    /**
     * Loads SDs and Events for the interval passed in.
     * @param activityInterval Interval to load data for
     * @returns a ProcessingActivityInterval as a promise
     */
    public async loadSDsAndEvents(activityInterval: model.ProcessingActivityInterval) {
        const stage = find(this.workflowCache.stageIntervals, { id: activityInterval.stageIntervalId });
        const startTimePadding: number = configProcessor.getConfigByKey('extraLoadingTime');
        const eventPadding = configProcessor.getConfigByKey('extraEventLoadingTime');
        const sdTimeRange = {
            startTime: stage.startTime - startTimePadding,
            endTime: stage.endTime + startTimePadding
        };
        const eventTimeRange = {
            startTime: stage.startTime - eventPadding,
            endTime: stage.endTime + startTimePadding
        };
        logger.info(`Loading signal detections for activity ${activityInterval.status}`);
        await signalDetectionProcessor.getSignalDetectionsForDefaultStations(sdTimeRange);
        logger.info(`Loading events for activity ${activityInterval.status}`);
        await eventProcessor.getEventsInTimeRange(eventTimeRange);
        logger.info(`Finished loading signal detections and events for activity ${activityInterval.status}`);
    }

    /**
     * Retrieves the processing activities for the processing stage with the provided unique ID.
     * @param stageId The unique ID of the processing stage to retrieve activities for
     * @returns a ProcessingActivity as a promise
     */
    public async getActivitiesByStage(stageId: string): Promise<model.ProcessingActivity[]> {
        return filter(this.workflowCache.activities, { stageId });
    }

    /**
     * Retrieves the processing stage intervals for the processing stage with the provided unique ID.
     * If provided, the optional timeRange parameter constrains the results to those
     * intervals falling between timeRange.startTime (inclusive) and timeRange.endTime (exclusive)
     * @param stageId The unique ID of the stage to retrieve intervals for
     * @param timeRange The time range object for which to retrieve intervals
     * @returns a ProcessingStageInterval[] as a promise
     */
    public async getIntervalsByStage(stageId: string, timeRange: any): Promise<model.ProcessingStageInterval[]> {
        if (timeRange) {
            return filter(this.workflowCache.stageIntervals, (stageInterval: model.ProcessingStageInterval) =>
                (stageInterval.stageId === stageId &&
                    stageInterval.startTime >= timeRange.startTime &&
                    stageInterval.endTime < timeRange.endTime));
        } else {
            const intervals = this.workflowCache.stageIntervals.filter(interval =>
                (interval.stageId === stageId &&
                    (interval.startTime >= this.currentStartTime &&
                        interval.endTime <= this.currentEndTime)));
            return intervals;
        }
    }

    /**
     * Retrieves the activity intervals for the processing activity with the provided unique ID.
     * @param activityId: The unique ID of the processing activity to retrieve activity intervals for
     * @returns ProcessingActivityInterval[] as a promise
     */
    public async getIntervalsByActivity(activityId: string): Promise<model.ProcessingActivityInterval[]> {
        return filter(this.workflowCache.activityIntervals, { activityId });
    }

    /**
     * Retrieves the processing stage intervals for the provided interval ID
     * @param intervalId The unique ID of the interval for which to retrieve processing intervals
     * @returns a ProcessingStageInterval[] as a promise
     */
    public async getStageIntervalsByInterval(intervalId: string): Promise<model.ProcessingStageInterval[]> {
        return filter(this.workflowCache.stageIntervals, { intervalId });
    }

    /**
     * Retrieves the processing activity intervals for the provided processing stage interval
     * with the provided unique ID.
     * @param stageIntervalId The unique ID of the processing stage interval for which to retrieve activity intervals
     * @returns a ProcessingActivityInterval[] as a promise
     */
    public async getActivityIntervalsByStageInterval(
        stageIntervalId: string): Promise<model.ProcessingActivityInterval[]> {
        return filter(this.workflowCache.activityIntervals, { stageIntervalId });
    }

    /**
     * This method enforces status transition rules for ProcessingStageIntervals and associated
     * ProcessingActivityIntervals, throwing an Error for invalid transitions, which include:
     * ProcessingStageInterval:
     *  - NotStarted -> Complete
     * ProcessingActivityInterval
     *  - NotStarted -> Complete
     *  - NotStarted -> NotComplete
     *  - Complete -> NotComplete
     *  - NotComplete -> Complete
     * @param stageInterval The ProcessingStageInterval the status update would be applied to
     * @param status The ProcessingStageInterval the status update would be applied to
     */
    public validateStageIntervalStatus(
        stageInterval: model.ProcessingStageInterval,
        status: model.IntervalStatus) {

        // Prevent all stage transitions to NotStarted (only valid for activity intervals)
        if (status === model.IntervalStatus.NotComplete) {
            throw new Error(`Invalid stage status transition (* to NotComplete) ` +
                `for stage with ID: ${stageInterval.id}`);

            // Prevent status transitions from NotStarted directly to Complete
        } else if (status === model.IntervalStatus.Complete) {
            if (stageInterval.status === model.IntervalStatus.NotStarted) {
                throw new Error(`Invalid stage status transition (NotStarted to Complete) ` +
                    `for stage with ID: ${stageInterval.id}`);
            }

            // Prevent stage status transitions to Complete if any of the associated activities
            // has a status other than Complete or NotComplete (i.e. InProgress or NotStarted)
            filter(this.workflowCache.activityIntervals, { stageIntervalId: stageInterval.id })
                .forEach(currentValue => {
                    if (currentValue.status !== model.IntervalStatus.Complete &&
                        currentValue.status !== model.IntervalStatus.NotComplete) {
                        const activity = this.workflowCache.activities.find(act => act.id === currentValue.activityId);
                        throw new Error(`Cannot transition stage to Complete because ${activity.name} associated ` +
                            `with the stage is not complete (${currentValue.status})` +
                            `\nActivity ID ${currentValue.id}`);
                    }
                });

            // Validate the status transition for each associated ProcessingActivityInterval
        } else if (status === model.IntervalStatus.InProgress) {
            filter(this.workflowCache.activityIntervals, { stageIntervalId: stageInterval.id })
                .forEach(currentValue => {
                    this.validateActivityIntervalStatus(currentValue, status);
                });
        }
    }

    /**
     * Update the status of the provided ProcessingStageInterval to the provided status
     * @param stageInterval The ProcessingStageInterval to update
     * @param status The new status to apply to the ProcessingStageInterval
     * @param analystUserName The username of the Analyst to associate with the status update
     */
    public updateStageIntervalStatus(
        stageInterval: model.ProcessingStageInterval,
        status: model.IntervalStatus,
        analystUserName: string) {

        // Set the completed by field if the input status is Complete
        if (status === model.IntervalStatus.Complete) {
            stageInterval.completedByUserName = analystUserName;
        }

        // Update the status
        stageInterval.status = status;
    }

    /**
     * Update the status of the provided ProcessingActivityInterval to the provided status
     * @param activityInterval The ProcessingActivityInterval to update
     * @param status The new status to apply to the ProcessingActivityInterval
     * @param analystUserName The username of the Analyst to associate with the status update
     */
    public updateActivityIntervalStatus
        (activityInterval: model.ProcessingActivityInterval, status: model.IntervalStatus, analystUserName: string) {
        if (status === model.IntervalStatus.Complete ||
            status === model.IntervalStatus.NotComplete) {
            // Set the completed by field to the input analyst user name
            // Note: NotComplete is an alternative for activities where Complete doesn't make sense,
            // so set the completed by field for this case too
            activityInterval.completedByUserName = analystUserName;

        } else if (status === model.IntervalStatus.InProgress) {
            // Add the provided analyst user name to the list of active analysts (if not already in the list)
            if (activityInterval.activeAnalystUserNames.indexOf(analystUserName) === -1) {
                activityInterval.activeAnalystUserNames.push(analystUserName);
            }
        }

        // Update the status
        activityInterval.status = status;
    }

    /**
     * This method enforces status transition rules, throwing an Error for invalid transitions, which include:
     * NotStarted -> Complete
     * NotStarted -> NotComplete
     * Complete -> NotComplete
     * NotComplete -> Complete
     * @param activityInterval The ProcessingActivityInterval the status update would be applied to
     * @param status The ProcessingActivityInterval the status update would be applied to
     */
    private validateActivityIntervalStatus
        (activityInterval: model.ProcessingActivityInterval, status: model.IntervalStatus) {

        // Prevent status transitions from NotStarted or NotComplete to Complete
        if (status === model.IntervalStatus.Complete) {
            if (activityInterval.status === model.IntervalStatus.NotStarted ||
                activityInterval.status === model.IntervalStatus.NotComplete) {
                throw new Error(`Invalid activity status transition from ${activityInterval.status} ` +
                    `to ${status} for activity with ID: ${activityInterval.id}`);
            }

            // Prevent status transitions from NotStarted or Complete to NotComplete
        } else if (status === model.IntervalStatus.NotComplete) {
            if (activityInterval.status === model.IntervalStatus.NotStarted ||
                activityInterval.status === model.IntervalStatus.Complete) {
                throw new Error(`Invalid activity status transition from ${activityInterval.status} ` +
                    `to ${status} for activity with ID: ${activityInterval.id}`);
            }
        }
    }

    /**
     * Creates a new stage interval ending at the current date/time spanning the time range defined
     * in the configuration.
     * @param startTime in epoch seconds
     * @param endTime in epoch seconds
     * @returns a ProcessingInterval
     */
    private populateInterval = (startTime: number, endTime: number): model.ProcessingInterval => {
        // Create a new ProcessingInterval with the start and end time
        const interval = {
            id: uuid4().toString(),
            startTime,
            endTime,
            stageIntervalIds: []
        };
        this.workflowCache.intervals.push(interval);

        // Create a new ProcessingStageInterval for each stage defined, and add it to the canned data array
        this.workflowCache.stages.forEach(stage => {
            const stageInterval = {
                id: uuid4().toString(),
                startTime,
                endTime,
                completedByUserName: '',
                stageId: stage.id,
                intervalId: interval.id,
                eventCount: 0,
                status: model.IntervalStatus.NotStarted,
                activityIntervalIds: []
            };

            this.workflowCache.stageIntervals.push(stageInterval);
            interval.stageIntervalIds.push(stageInterval.id);

            logger.info(`Created new processing stage interval with ID: ${stageInterval.id}, for stage: ${stage.name}`);

            // Create a new ProcessingActivityInterval for each activity associated with the stage (by ID), add it
            // to the canned data array, and update the stage interval array of activity interval IDs
            stage.activityIds.forEach(activityId => {
                const activityIntervalId = uuid4().toString();

                this.workflowCache.activityIntervals.push({
                    id: activityIntervalId,
                    activeAnalystUserNames: [],
                    completedByUserName: '',
                    timeStarted: undefined,
                    eventCount: 0,
                    status: model.IntervalStatus.NotStarted,
                    activityId,
                    stageIntervalId: stageInterval.id
                });

                logger.info(`Created new processing activity interval with ID: ${activityIntervalId}`);

                stageInterval.activityIntervalIds.push(activityIntervalId);
            });
        });

        return interval;
    }

    /**
     * Creates a new stage interval ending at the current date/time spanning the time range defined
     * in the configuration.
     */
    private createInterval = () => {

        // Is it time for a new interval?
        const elapsedTimeSecs = epochSecondsNow() - this.intervalElapsedSecs;
        if (elapsedTimeSecs < this.createIntervalDurationSec) {
            return;
        }
        // Determine the new interval start and end time (based on configured interval span)
        const startTime = this.intervalCreationStartTimeSec;
        const endTime = this.intervalCreationStartTimeSec + this.intervalDurationSec;

        const newInterval = this.populateInterval(startTime, endTime);

        // Start the next interval at the current end time
        this.intervalCreationStartTimeSec = endTime;

        logger.info(`Creating stage and activity intervals for the time span: ${startTime} to ${endTime}`);

        // Publish the created interval to GraphQL subscribers
        intervalCreated(newInterval);

        // Reset the elapsed time for next interval creation
        this.intervalElapsedSecs = epochSecondsNow();
    }

    /**
     * Normailzes the start and end times of the inputed parameters
     * @param startTimeSec start time in epoch seconds
     * @param endTimeSec end time in epoch seconds
     * @returns a TimeRange
     */
    private normalizeStartEndTimes = (startTimeSec: number, endTimeSec: number): TimeRange => {

            /**
             * Intervals are discrete, two hour segments of time
             * Ie there is an interval from 12:00am to 2:00am, and from 2:00am to 4:00am
             * But not from 1:00am to 3:00am
             * 
             * So when we populate intervals, first we normalize to our two hour
             * chunks
             * 
             * Because interval creation is cheap, we overestimate needs
             */

            let normalizedStartSec = startTimeSec;
            normalizedStartSec = normalizedStartSec - (normalizedStartSec % this.intervalDurationSec);
            // An end time secs 'snapped' to a 2 hour time interval
            const normalizedEndSec =
                endTimeSec % this.intervalDurationSec === 0 ?
                endTimeSec
                : endTimeSec + (this.intervalDurationSec - (endTimeSec % this.intervalDurationSec));
            logger.debug(`Start: ${startTimeSec} Normalized: ${normalizedStartSec}`);
            logger.debug(`End: ${endTimeSec} Normalized: ${normalizedEndSec}`);
            logger.debug(`End: ${endTimeSec} Normalized: ${normalizedEndSec}`);
            return {startTime: normalizedStartSec, endTime: normalizedEndSec};

    }
}

// Export an initialized instance of the processor
export const workflowProcessor: WorkflowProcessor = new WorkflowProcessor();
workflowProcessor.initialize()
    .catch(e => gatewayLogger.warn(e));
