import { BatchHttpLink } from 'apollo-link-batch-http';
import { bactchMsgPackfetcher } from './msgpack-link';

const batchInterval = 10;
const batchMax = 5;

export const BatchLink = url => {
  try {
    return new BatchHttpLink({
      uri: url,
      batchInterval,
      batchMax
    });
  } catch (error) {
    // tslint:disable-next-line:no-console
    console.log(`Failed to create Batch HTTP Link: ${error}`);
    return undefined;
  }
};

export const BatchMsgPackLink = url => {
  try {
    return new BatchHttpLink({
      uri: url,
      batchInterval,
      batchMax,
      fetch: bactchMsgPackfetcher,
      headers: {
        'Accept-Encoding': 'gzip, deflate, br',
        Accept: 'application/json, application/msgpack'
        // apollo client does not allow for overriding the content type
      }
    });
  } catch (error) {
    // tslint:disable-next-line:no-console
    console.log(`Failed to create Batch MsgPack HTTP Link: ${error}`);
    return undefined;
  }
};
