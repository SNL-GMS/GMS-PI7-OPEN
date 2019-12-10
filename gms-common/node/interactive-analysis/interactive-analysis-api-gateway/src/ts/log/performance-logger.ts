import { gatewayLogger as logger } from '../log/gateway-logger';

/**
 * Wrapper around logger.data for performance specific formatting
 */
class PerformanceLogger {

    /**
     * Performance logger formatter
     * @param action top level action (signalDetectionsByStation, filterChannelSegment, etc)
     * @param step stop in action (enteringResolver, returningFromServer, etc)
     * @param identifier unique id for object being worked (sd id, event id, channelsegment with filterid, etc)
     */
    public performance(action: string, step: string, identifier?: string) {
        if (identifier) {
            logger.data(`Action:${action} Step:${step} ID:${identifier}`);
        } else {
            logger.data(`Action:${action} Step:${step}`);
        }
    }
}

export const performanceLogger = new PerformanceLogger();
