import { ApolloLink, split } from 'apollo-link';
import { WebSocketLink } from 'apollo-link-ws';
import { getMainDefinition } from 'apollo-utilities';
import { OperationDefinitionNode } from 'graphql';
import { isWindowDefined } from '../../util/window-util';

// we can't initialize the websocket client if we aren't running in the browser or a renderer process.
// this shouldn't run in the main electron process.
const windowIsDefined = isWindowDefined();

export const SplitLink = (wsLink: WebSocketLink, httpLink: any): ApolloLink =>
  split(
    ({ query }: any) => {
      const node = getMainDefinition(query);
      if (node.kind as string === 'OperationDefinition') {
          const defNode = node as OperationDefinitionNode;
          return (defNode.operation as string) === 'subscription';
      } else {
          return false;
      }
    },
    windowIsDefined && wsLink ? wsLink : httpLink,
    httpLink
  );
