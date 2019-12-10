/**
 * Renders events onto the map
 */

import * as lodash from 'lodash';
import * as moment from 'moment';
import { userPreferences } from '~analyst-ui/config';
import { EventTypes } from '~graphql/';
import { TimeInterval } from '~state/analyst-workspace/types';
import { MapProps } from '../types';

// tslint:disable-next-line:no-implicit-dependencies no-var-requires no-require-imports
const eventPng = require('../img/' + userPreferences.map.icons.event);
const imageScale = userPreferences.map.icons.eventScale;

declare var Cesium;

/** How much to scale the dislay of events by */
const scaleFactor = userPreferences.map.icons.scaleFactor;
/** How far away the camera should be to render the label */
const displayDistance = userPreferences.map.icons.displayDistance;
/** Label's offset from the event */
const pixelOffset = userPreferences.map.icons.pixelOffset;

/**
 * Draws events on map
 * 
 * @param dataSource Data to add drawn events to
 * @param currentProps Previous props for the map
 * @param nextProps Incoming props for the map
 */
export function draw(dataSource: any, currentProps: MapProps, nextProps: MapProps) {
  const nextEvents = nextProps.eventsInTimeRangeQuery &&
    nextProps.eventsInTimeRangeQuery.eventsInTimeRange ? nextProps.eventsInTimeRangeQuery.eventsInTimeRange : [];
  const currentEvents = currentProps.eventsInTimeRangeQuery &&
    currentProps.eventsInTimeRangeQuery.eventsInTimeRange ?
    currentProps.eventsInTimeRangeQuery.eventsInTimeRange : [];
  const currentTimeInterval = nextProps.currentTimeInterval;
  const nextOpenEventId = nextProps.openEventId;
  const selectedEventIds = nextProps.selectedEventIds;
  const prevSelectedEventIds = currentProps.selectedEventIds;

  const newEvents = lodash.differenceBy(nextEvents, currentEvents, 'id');
  addEvents(dataSource, newEvents, currentTimeInterval, nextOpenEventId, selectedEventIds);

  const modifiedEvents =
    (lodash.intersectionBy(nextEvents, currentEvents, 'id'));
  updateEvents(dataSource, modifiedEvents, currentTimeInterval, nextOpenEventId, selectedEventIds);

  const selectedEvents = lodash.intersectionBy(nextEvents, currentEvents, 'id')
    .filter(selectionEvent => selectedEventIds.
      find(eid => eid === selectionEvent.id) || prevSelectedEventIds.
        find(eid => eid === selectionEvent.id));
  updateEvents(dataSource, selectedEvents, currentTimeInterval, nextOpenEventId, selectedEventIds);

  const removedEvents = lodash.differenceBy(
    currentEvents,
    nextEvents, 'id');
  removeEvents(dataSource, removedEvents);
}

/**
 * Highlight Open Event when event is opened on the map or list
 * @param dataSource event datasource
 * @param currentTimeInterval current time interval opened by the analyst
 * @param currentOpenEvent open event that has been open
 * @param nextOpenEvent event being opened
 * @param selectedEventIds other selected event ids
 */
export function highlightOpenEvent(dataSource: any,
  currentTimeInterval: TimeInterval,
  currentOpenEvent: EventTypes.Event,
  nextOpenEvent: EventTypes.Event,
  selectedEventIds: string[]) {
  // attempt to re-color the currently selected event entity to its default state.
  if (currentOpenEvent) {
    const currentEventEntity = dataSource.entities.getById(currentOpenEvent.id);
    // If a next open event exists pass in the ID of that event,
    // in the case of selecting 'mark complete' on the current
    // open event nextopenevent is null/undefined.
    if (nextOpenEvent) {
      currentEventEntity.billboard.color = computeColorForEvent(
        currentOpenEvent, currentTimeInterval, nextOpenEvent.id, selectedEventIds);
    } else {
      currentEventEntity.billboard.color = computeColorForEvent(
        currentOpenEvent, currentTimeInterval, undefined, selectedEventIds);
    }
  }
  if (nextOpenEvent) {
    const nextEventEntity = dataSource.entities.getById(nextOpenEvent.id);
    nextEventEntity.billboard.color = computeColorForEvent(
      nextOpenEvent, currentTimeInterval, nextOpenEvent.id, selectedEventIds);
  }
}

/**
 * create new map entities for a list of events
 * 
 * @param dataSource - source of event data
 * @param events - list of map events
 * @param currentTimeInterval - currently open time interval
 * @param nextOpenEventId - incoming open event id
 * @param selectedEventIds - list of selected event ids
 */
function addEvents(dataSource: any, events: EventTypes.Event[],
  currentTimeInterval: TimeInterval,
  nextOpenEventId: string, selectedEventIds: string[]) {

  // Walk thru the event list and add each event entity to the dataSource
  events.forEach(event => {
    dataSource.entities.add(createEventEntity(event, currentTimeInterval, nextOpenEventId, selectedEventIds));
  });
}

/**
 * Update the map entities for a list of events
 * 
 * @param dataSource - source of event data
 * @param events - list of map events 
 * @param currentTimeInterval - currently open TimeInterval
 * @param nextOpenEventId - incoming id of open event
 * @param selectedEventIds - list of selected event ids
 */
function updateEvents(dataSource: any, events: EventTypes.Event[],
  currentTimeInterval: TimeInterval,
  nextOpenEventId: string, selectedEventIds: string[]) {

  events.forEach(event => {
    const eventEntity = dataSource.entities.getById(event.id);
    // Update location and description values if this is the selected event
    if (event.id === nextOpenEventId) {
      // Create a replacement event entity in case the event location has changed
      const newEventEntity = createEventEntity(event, currentTimeInterval, nextOpenEventId, selectedEventIds);
      eventEntity.position = newEventEntity.position;
      eventEntity.description = newEventEntity.description;
      eventEntity.label = newEventEntity.label;
    } else {
      // Not the selected event so update association colores
      const isSelected = selectedEventIds.find(eid => eid === event.id);
      eventEntity.billboard.color = computeColorForEvent(
        event, currentTimeInterval, nextOpenEventId, selectedEventIds);
      eventEntity.billboard.scale = isSelected ? imageScale * scaleFactor : imageScale;
    }
  });
}

/**
 * Creates the Event entity for the map. Used by addEvent and updateEvent if event is currentOpenEvet
 * @param the reference event
 * @param currentTimeInterval selected
 * @param openEventId current open event id
 * @param selectedEventIds event list
 * @returns Event Entity (map entity entry) for rendering event on map
 */
function createEventEntity(event: EventTypes.Event, currentTimeInterval: TimeInterval,
  openEventId: string, selectedEventIds: string[]): any {
  const eventHypothesis = event.currentEventHypothesis.eventHypothesis;
  const eventLon = eventHypothesis.preferredLocationSolution.locationSolution.location.longitudeDegrees;
  const eventLat = eventHypothesis.preferredLocationSolution.locationSolution.location.latitudeDegrees;
  const eventElev = eventHypothesis.preferredLocationSolution.locationSolution.location.depthKm;

  const eventTime = eventHypothesis.preferredLocationSolution.locationSolution.location.time;
  const eventTimeFormatted = moment.unix(eventTime)
    .utc()
    .format('HH:mm:ss');
  const eventDateTimeFormatted = moment.unix(eventTime)
    .utc()
    .format('MM/DD/YYYY HH:mm:ss');

  const eventDescription =
    `<ul>
      <li>ID: ${event.id}</li>
      <li>Time: ${eventDateTimeFormatted}</li>
      <li>Latitude: ${eventLat.toFixed(3)}</li>
      <li>Lognitude: ${eventLon.toFixed(3)}</li>
      <li>Elevation: ${eventElev.toFixed(3)}</li></ul>`;
  return {
    name: 'Event: ' + event.id,
    position: Cesium.Cartesian3.fromDegrees(eventLon, eventLat),
    id: event.id,
    billboard: {
      image: eventPng,
      color: computeColorForEvent(event, currentTimeInterval, openEventId, selectedEventIds),
      scale: imageScale
    },
    label: {
      text: eventTimeFormatted,
      font: '12px sans-serif',
      outlineColor: Cesium.Color.BLACK,
      pixelOffset: new Cesium.Cartesian2(0, pixelOffset),
      distanceDisplayCondition: new Cesium.DistanceDisplayCondition(0, displayDistance),
    },
    description: eventDescription,
    entityType: 'event'
  };
}
/**
 * Remove events from datasource
 * 
 * @param dataSource event datasource 
 * @param eventHypotheses events to be removed
 */
function removeEvents(dataSource: any, events: EventTypes.Event[]) {
  events.forEach(event => {
    dataSource.entities.removeById(event.id);
  });
}

/**
 * Compute the proper png to use for an event star
 * 
 * @param event event to draw
 * @param currentTimeInterval currently open time interval
 * @param openEventId string event id
 * @param selectedEventIds  list of selected events
 */
function computeColorForEvent(event: EventTypes.Event,
  currentTimeInterval: TimeInterval,
  openEventId: string,
  selectedEventIds: string[]) {
  const eventTime = event.currentEventHypothesis.eventHypothesis.preferredLocationSolution.
    locationSolution.location.time;
  const isInTimeRange = (eventTime > currentTimeInterval.startTimeSecs)
    && (eventTime < currentTimeInterval.endTimeSecs);

  return isInTimeRange ?
    event.status === 'Complete' ?
      Cesium.Color.fromCssColorString(userPreferences.map.colors.completeEvent)
      : event.id === openEventId ?
        Cesium.Color.fromCssColorString(userPreferences.map.colors.openEvent)
        : selectedEventIds.find(eid => eid === event.id) ?
          Cesium.Color.fromCssColorString(userPreferences.map.colors.selectedEvent)
          : Cesium.Color.fromCssColorString(userPreferences.map.colors.toWorkEvent)
    : Cesium.Color.fromCssColorString(userPreferences.map.colors.outOfIntervalEvent);
}
