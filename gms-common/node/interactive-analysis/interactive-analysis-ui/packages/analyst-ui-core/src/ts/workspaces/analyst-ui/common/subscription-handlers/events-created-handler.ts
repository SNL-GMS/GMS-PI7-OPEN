import { EventTypes } from '~graphql/';
import { TimeInterval } from '~state/analyst-workspace/types';

/**
 * Reusable function for handling events created subscription
 */
export function handleCreatedEvents(prev, cur, timeRange: TimeInterval) {
    const data = cur.subscriptionData.data as EventTypes.EventsCreatedSubscription;
    if (data) {
    const newEventList = [...prev.eventsInTimeRange];
    data.eventsCreated.forEach(event => {
        if (newEventList.findIndex(prevEvent => prevEvent.id === event.id) < 0) {
        newEventList.push(event);
        }
    });

    return {
        ...prev,
        eventsInTimeRange: newEventList
    };
    }
}
