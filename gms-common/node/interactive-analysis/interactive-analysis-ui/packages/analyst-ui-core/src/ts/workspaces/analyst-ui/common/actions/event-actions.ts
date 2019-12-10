import { MutationFunc } from 'react-apollo';
import { EventTypes } from '~graphql/';
import { EventStatus } from '~graphql/event/types';
import { AnalystActivity, TimeInterval } from '~state/analyst-workspace/types';
/**
 * Updates and marks and event as opened.
 * 
 * @param events the available events
 * @param openEventId the event id to open
 * @param analystActivity the current analyst activity
 * @param setOpenEventId the function to set the open event id
 * @param updateEvents the function to update the event
 */
export const openEvent = (
  events: EventTypes.Event[],
  openEventId: string,
  analystActivity: AnalystActivity,
  updateEvents: MutationFunc<{}>,
  setOpenEventId: (event: EventTypes.Event) => void
) => {
  const event = events.find(e => e.id === openEventId);
  if (event !== undefined) {
    const processingStageId = event.currentEventHypothesis.processingStage ?
      event.currentEventHypothesis.processingStage.id : undefined;
    const activeAnalysts = event.activeAnalysts ? event.activeAnalysts.map(analyst => analyst.userName) : [];
    if (analystActivity === AnalystActivity.eventRefinement &&
      processingStageId &&
      event.status !== EventStatus.OpenForRefinement) {
      const variables: EventTypes.UpdateEventsMutationArgs = {
        eventIds: [openEventId],
        input: {
          creatorId: 'Mark',
          processingStageId,
          status: EventStatus.OpenForRefinement,
          activeAnalystUserNames: [...activeAnalysts, 'Mark']
        }
      };
      if (updateEvents !== undefined) {
        updateEvents({
          variables
        })
        .catch();
      }
      setOpenEventId(event);
    } else {
      setOpenEventId(event);
    }
  }
};

/**
 * Action that auto opens the first non completed event within
 * the provided time interval.
 * 
 * @param data the available events
 * @param currentTimeInterval the current time interval
 * @param openEventId the current open event id
 * @param analystActivity the current analyst activity
 * @param setOpenEventId the function to set the open event id
 * @param updateEvents the function to update the event
 */
export const autoOpenEvent = (
  events: EventTypes.Event[],
  currentTimeInterval: TimeInterval,
  openEventId: string,
  analystActivity: AnalystActivity,
  setOpenEventId: (event: EventTypes.Event) => void,
  updateEvents: MutationFunc<{}>
): void => {
  if (events &&
    !openEventId && analystActivity === AnalystActivity.eventRefinement) {
    events.sort((a, b) => {
      const timeA = a.currentEventHypothesis.eventHypothesis.preferredLocationSolution.
        locationSolution.location.time;
      const timeB = b.currentEventHypothesis.eventHypothesis.preferredLocationSolution.
        locationSolution.location.time;
      return timeA - timeB;
    });
    const event = events.find(e => {
        const time = e.currentEventHypothesis.eventHypothesis.preferredLocationSolution.
          locationSolution.location.time;
        return (time >= currentTimeInterval.startTimeSecs &&
          time <= currentTimeInterval.endTimeSecs &&
          e.status !== EventTypes.EventStatus.Complete);
      });
    if (event) {
      openEvent(events, event.id, analystActivity, updateEvents, setOpenEventId);
    }
  }
};
