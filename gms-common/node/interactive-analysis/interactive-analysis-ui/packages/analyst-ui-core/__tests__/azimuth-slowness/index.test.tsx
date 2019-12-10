
import * as React from 'react';
import { ApolloProvider } from 'react-apollo';
import { Provider } from 'react-redux';
// tslint:disable-next-line:max-line-length
import { ReduxApolloAzimuthSlowness } from '~analyst-ui/components/azimuth-slowness/azimuth-slowness-container';
import { createStore } from '~state/store';

// tslint:disable-next-line:no-implicit-dependencies no-var-requires no-require-imports
const Enzyme = require('enzyme');
// tslint:disable-next-line:no-implicit-dependencies no-var-requires no-require-imports
const Adapter = require('enzyme-adapter-react-16');

jest.mock('graphql'); // graphql is now a mock constructor

const store =  createStore();

it('should render a ReduxApolloAzimuthSlowness component correctly', () => {
  Enzyme.configure({ adapter: new Adapter() });
  const wrapper = Enzyme.shallow(
    <ApolloProvider client={store.getState().apolloClient.client}>
      <Provider store={store}>
        <ReduxApolloAzimuthSlowness />
      </Provider>
    </ApolloProvider>
  );
  wrapper.dive();
  expect(wrapper)
    .toMatchSnapshot();
});
