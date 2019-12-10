import { ApolloLink } from 'apollo-link';
import apolloLogger from 'apollo-link-logger';
import { IS_NODE_ENV_DEVELOPMENT } from '../../util/environment';
import { UILogger } from '../../util/log/logger';

// this logger catches a apollo operation before the ajax call and after the ajax call
// and logs the type, name, and time ellapsed of the call
const gatewayLogger = new ApolloLink((operation: any, forward: any) => {
  const startTime = new Date().getTime();
  const operationType = operation.query.definitions[0].operation;
  UILogger.performance(operation.operationName, 'request');
  return forward(operation)
  .map(result => {
    const ellapsed = new Date().getTime() - startTime;
    UILogger.performance(operation.operationName, 'returned');
    UILogger.data(`${operationType} ${operation.operationName} (in ${ellapsed} ms)`);
    return result;
  });
});

// orphaned function that takes an operation and returns it for the chainning
// of ApolloLinks
const passThrough = new ApolloLink((operation: any, forward: any) =>
  forward(operation));

// ApolloLink concatinator function that turns multiple ApolloLinks into 1
export const LoggerLink = ApolloLink.from([gatewayLogger,
  // disable in production
  IS_NODE_ENV_DEVELOPMENT ? apolloLogger : passThrough]);
