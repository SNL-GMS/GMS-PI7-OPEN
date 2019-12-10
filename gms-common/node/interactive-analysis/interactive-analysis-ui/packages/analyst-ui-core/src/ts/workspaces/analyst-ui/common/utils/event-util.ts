import { flatMap } from 'lodash';
import { userPreferences } from '~analyst-ui/config/user-preferences';
import { EventTypes } from '~graphql/';
import { SignalDetection } from '~graphql/signal-detection/types';

/**
 * Get the current open event
 * @param openEventId Id of open event
 * @param eventsInTimeRangeQuery eventsInTimeRangeQuery
 */
export function getOpenEvent(openEventId: string | undefined, eventsInTimeRangeQuery): EventTypes.Event | undefined {
  return openEventId && eventsInTimeRangeQuery.eventsInTimeRange ?
    eventsInTimeRangeQuery.eventsInTimeRange.find(e => e.id === openEventId) : undefined;
}
/**
 * Gets the signal detections associated to the given event
 * 
 * @param event the open even to get sd's for
 * @param signalDetections signalDetections to look through
 */
export function getAssocSds(event: EventTypes.Event, signalDetections: SignalDetection[]) {
  return flatMap(
    event.currentEventHypothesis.eventHypothesis.signalDetectionAssociations,
    assocSD => {
      const maybeSD = signalDetections.find(sd =>
        assocSD.signalDetectionHypothesis.id === sd.currentHypothesis.id);
      if (maybeSD) {
          return maybeSD;
      }
    }
  )
  .filter(assocSD => assocSD !== undefined);
}

/**
 * 
 */
export function getLatestLSS(event: EventTypes.Event) {
    return getLatestLSSForHyp(event.currentEventHypothesis.eventHypothesis);
  }
/**
 * 
 */
export function getLatestLSSForHyp(hyp: EventTypes.EventHypothesis) {
    return hyp.locationSolutionSets.reduce(
      (prev, curr) => {
      if (curr.count > prev.count) {
        return curr;
      } else {
        return prev;
      }
    },
      hyp.locationSolutionSets[0]);
  }
/**
 * Gets the deault preferred location id for an event based off the config
 * Or if not found returns the id for the preferred location solution
 * 
 * @param locationSolutionSet Location Solution Set to  get default preferred location solution from
 */
export const getPreferredDefaultLocationId
    = (locationSolutionSet: EventTypes.LocationSolutionSet): string | undefined => {

    if (locationSolutionSet.locationSolutions.length < 1) {
        return undefined;
    } else {
        let toReturn: string;
        // A for loop is used so we can break
    // tslint:disable-next-line: prefer-for-of
        for (let i = 0;
            i < userPreferences.location.preferredLocationSolutionRestraintOrder.length; i++) {
                const dr = userPreferences.location.preferredLocationSolutionRestraintOrder[i];
                const maybeLS = locationSolutionSet.locationSolutions.find(ls =>
                    ls.locationRestraint.depthRestraintType === dr
                );
                if (maybeLS) {
                    toReturn = maybeLS.id;
                    break;
                }
        }
        if (toReturn) {
            return toReturn;
        } else {
            return locationSolutionSet[0].id;
        }
    }
};
/**
 * Gets the deault preferred location id for an event based off the config
 * Or if not found returns the id for the preferred location solution
 * 
 * @param eventHypothesis Event hypothesis to get default preferred location solution from
 */
export const getPreferredLocationIdFromEventHyp = (eventHypothesis: EventTypes.EventHypothesis): string => {
  if (eventHypothesis.locationSolutionSets.length < 1) {
      return undefined;
  }
  const set = getLatestLSSForHyp(eventHypothesis);
  return getPreferredDefaultLocationId(set);
};
