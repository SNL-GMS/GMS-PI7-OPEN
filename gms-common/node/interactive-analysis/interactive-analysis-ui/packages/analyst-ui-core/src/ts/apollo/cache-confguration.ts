import { Hermes } from 'apollo-cache-hermes';
import {
  defaultDataIdFromObject,
  InMemoryCache,
  IntrospectionFragmentMatcher
} from 'apollo-cache-inmemory';
import { fragmentSchema } from '../graphql/fragment-schema';

// tslint:disable-next-line:no-implicit-dependencies no-var-requires no-require-imports
// const hash = require('object-hash');

export const fragmentMatcher = new IntrospectionFragmentMatcher({
  introspectionQueryResultData: {
    ...fragmentSchema
  }
});

const retrieveId = (object: any) => {
  if (object.__typename) {
    if (object.id || object._id) {
      return defaultDataIdFromObject(object); // fall back to default id handling
    } else {
      // TODO: consider handling special cases here
      // no id exists on this type
      // create a unique id by hashing the objects
      // return `${object.__typename}:${hash(object, {respectType: false})}`;
    }
  }
  return null;
};

export const inMemoryCacheConfiguration: InMemoryCache = new InMemoryCache({
  addTypename: true,
  fragmentMatcher,
  dataIdFromObject: retrieveId,
  cacheRedirects: {
    Query: {}
  }
});

export const cacheHermesConfigutation: Hermes = new Hermes({
  addTypename: true,
  entityIdForNode: retrieveId,
  freeze: false
});
