import { onError } from 'apollo-link-error';

export const ErrorLink = onError(({ graphQLErrors, networkError }) => {
  if (graphQLErrors) {
    graphQLErrors.map(({ message, locations, path }) =>
      // tslint:disable-next-line:no-console
      console.error(
        `[GraphQL error]: Message: ${message}, Location: ${locations}, Path: ${path}`
      )
    );
  }
  // tslint:disable-next-line:no-console
  if (networkError) console.log(`[Network error]: ${networkError}`);
});
