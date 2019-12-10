import { WebSocketLink } from 'apollo-link-ws';
import { SubscriptionClient } from 'subscriptions-transport-ws';

export const WsClient = (ws: string): SubscriptionClient | undefined => {
  try {
    return new SubscriptionClient(ws, {
      reconnect: true,
      // 100000ms or 100s
      timeout: 100000
    });
  } catch (error) {
    // tslint:disable-next-line:no-console
    console.log(`Failed to create WS Client: ${error}`);
    return undefined;
  }
};

export const WsLink = (
  wsClient: SubscriptionClient
): WebSocketLink | undefined => {
  try {
    return new WebSocketLink(wsClient);
  } catch (error) {
    // tslint:disable-next-line:no-console
    console.log(`Failed to create WS Link: ${error}`);
    return undefined;
  }
};
