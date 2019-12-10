import { ApolloLink } from 'apollo-link';
import { DedupLink } from 'apollo-link-dedup';
import { HttpLink } from 'apollo-link-http';
import { BatchLink, BatchMsgPackLink } from './batch-link';
import { ErrorLink } from './error-link';
import { LoggerLink } from './logger-link';
import { MsgPackLink } from './msgpack-link';

export const Link = (
  url: string,
  batchMode: boolean = false,
  allowMsgPack: boolean = false
): ApolloLink | undefined => {
  try {
    return ApolloLink.from([
      LoggerLink,
      // ignore duplicates requests
      new DedupLink(),
      ErrorLink,
      // createPersistedQueryLink({ useGETForHashedQueries: true }),
      batchMode
        ? allowMsgPack
          ? BatchMsgPackLink(url)
          : BatchLink(url)
        : allowMsgPack
          ? MsgPackLink(url)
          : new HttpLink({
              uri: url,
              headers: {
                'Accept-Encoding': 'gzip, deflate, br',
                Accept: 'application/json'
              },
              fetchOptions: {
                follow: 50, // maximum redirect count. 0 to not follow redirect
                timeout: 0, // req/res timeout in ms, it resets on redirect. 0 to disable (OS limit applies)
                compress: true, // support gzip/deflate content encoding. false to disable
                size: 0 // maximum response body size in bytes. 0 to disable
              }
            })
    ]);
  } catch (error) {
    // tslint:disable-next-line:no-console
    console.log(`Failed to create HTTP Link: ${error}`);
    return undefined;
  }
};
