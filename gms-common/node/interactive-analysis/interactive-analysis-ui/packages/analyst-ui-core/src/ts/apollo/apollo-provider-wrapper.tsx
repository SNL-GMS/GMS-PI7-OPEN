import * as React from 'react';
import { ApolloProvider } from 'react-apollo';
import { Provider } from 'react-redux';
import * as Redux from 'redux';
import { AppState } from '~state/types';

/**
 * Wraps the provided componment with ApolloProvider
 *
 * @param Component the component
 * @param store the redux store
 */
export const ApolloProviderWrapper = (
  Component: React.ComponentClass,
  store: Redux.Store<AppState>
) =>
  class extends React.Component<any, any> {
    /**
     * Wrap the component in an apollo provider
     */
    public render() {
      return (
        <ApolloProvider
          client={
            // tslint:disable-next-line:newline-per-chained-call
            store.getState().apolloClient.client
          }
        >
          <Provider store={store}>
            <Component {...this.props} />
          </Provider>
        </ApolloProvider>
      );
    }
  };
