import DefaultClient from 'apollo-boost';
import * as log4javascript from 'log4javascript';
import { clientLog } from '../../graphql/common/mutations';
import { ClientLogMutationArgs, LogLevel } from '../../graphql/common/types';
import { GRAPHQL_PROXY_URI } from '../environment';

// Create a PopUpAppender with default options
const popUpAppender = new log4javascript.PopUpAppender();
// Change the desired configuration options
popUpAppender.setNewestMessageAtTop(true);
popUpAppender.setComplainAboutPopUpBlocking(true);
popUpAppender.setUseOldPopUp(true);
popUpAppender.setReopenWhenClosed(true);
popUpAppender.setScrollToLatestMessage(true);
popUpAppender.setFocusPopUp(false);
popUpAppender.setInitiallyMinimized(true);

export const showLogPopup = () => {
  popUpAppender.show();
};
popUpAppender.hide();
/**
 * Logger class, used throught the UI for logging
 * Logs are sent to the API gateway.
 */
class Logger {
  private static INSTANCE: Logger;
  private readonly apolloClient: DefaultClient<any> | undefined;
  private readonly internalLogger: log4javascript.Logger;

  public static getInstance() {
    return this.INSTANCE || (this.INSTANCE = new Logger());
  }

  private constructor() {
    // The graphql URL

    const graphqlProxyUri = GRAPHQL_PROXY_URI;

    // TODO: consider only enabling when in development
    this.apolloClient = new DefaultClient({
      uri: graphqlProxyUri ? `${graphqlProxyUri}/graphql` : undefined
    });
    this.internalLogger = log4javascript.getLogger('logger');
    this.internalLogger.addAppender(popUpAppender);
    this.internalLogger.setLevel(log4javascript.Level.ALL);
  }

  /**
   * General log
   * @param message type string message to be logged
   */
  public log(message: string) {
    this.logToServer(message, LogLevel.INFO);
    this.internalLogger.info(message);
  }

  /**
   * Info log
   * @param message type string message to be logged
   */
  public info(message: string) {
    this.logToServer(message, LogLevel.INFO);
    this.internalLogger.info(message);
  }

  /**
   * Debug log
   * @param message type string message to be logged
   */
  public debug(message: string) {
    this.logToServer(message, LogLevel.DEBUG);
    this.internalLogger.debug(message);
  }

  /**
   * Warning log
   * @param message type string message to be logged
   */
  public warn(message: string) {
    this.logToServer(message, LogLevel.WARNING);
    this.internalLogger.warn(message);
  }

  /**
   * Error log
   * @param message type string message to be logged
   */
  public error(message: string) {
    this.logToServer(message, LogLevel.ERROR);
    this.internalLogger.error(message);
  }

  /**
   * Data log
   * @param message type string message to be logged
   */
  public data(message: string) {
    this.logToServer(message, LogLevel.DATA);
    this.internalLogger.info(message);
  }

  /**
   * Performance logger formatter
   * @param action top level action (signalDetectionsByStation, filterChannelSegment, etc)
   * @param step stop in action (enteringResolver, returningFromServer, etc)
   * @param identifier unique id for object being worked (sd id, event id, channelsegment with filterid, etc)
   */
  public performance(action: string, step: string, identifier?: string) {
    const message = identifier ? `Action:${action} Step:${step} ID:${identifier}`
                    : `Action:${action} Step:${step}`;
    this.logToServer(message, LogLevel.DATA);
    this.internalLogger.info(message);
  }

  /**
   * Handles the mutation to the GraphQL Server
   * @param message message to log
   * @param logLevel log level
   */
  private logToServer(message: string, logLevel: LogLevel) {
    if (this.apolloClient) {
      const variables: ClientLogMutationArgs = {
        input: {
          logLevel,
          message,
          time: new Date().toISOString()
        }
      };
      // This is a fix to downcast it to a true apollo client
      // tslint:disable-next-line
      clientLog(this.apolloClient as any, variables)
      .catch(e => window.alert(e));
    } else {
      this.internalLogger.error('Apollo client is undefined cant log to server');
    }
  }
}

export const UILogger = Logger.getInstance();
