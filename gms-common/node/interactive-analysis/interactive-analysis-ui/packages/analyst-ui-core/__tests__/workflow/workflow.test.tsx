import {
  Classes,
  ContextMenu,
  NonIdealState,
  Spinner
} from '@blueprintjs/core';
import { NetworkStatus } from 'apollo-client';
import { ReactWrapper } from 'enzyme';
import { GraphQLError } from 'graphql';
import * as React from 'react';
import { ApolloProvider } from 'react-apollo';
import { Provider } from 'react-redux';
import { WorkflowProps } from '~analyst-ui/components/workflow/types';
import { Workflow } from '~analyst-ui/components/workflow/workflow-component';
import { ReduxApolloWorkflowContainer } from '~analyst-ui/components/workflow/workflow-container';
import {
  AnalystActivity,
  StageInterval
} from '~state/analyst-workspace/types';
import { createStore } from '~state/store';
import { makeMockApolloClient } from '../utils/apollo';

// tslint:disable-next-line:no-implicit-dependencies no-var-requires no-require-imports
const Enzyme = require('enzyme');
// tslint:disable-next-line:no-implicit-dependencies no-var-requires no-require-imports
const Adapter = require('enzyme-adapter-react-16');

const mockWorkflowProps: WorkflowProps = {
  stagesQuery: {
    loading: false,
    networkStatus: NetworkStatus.ready,
    stages: [],
    variables: {},

    refetch: async () =>
      Promise.resolve({
        data: {},
        loading: false,
        stale: false,
        networkStatus: NetworkStatus.ready
      }),
    fetchMore: async () =>
      Promise.resolve({
        data: {},
        loading: false,
        stale: false,
        networkStatus: NetworkStatus.ready
      }),
    startPolling: () => {
      /* empty */
    },
    stopPolling: () => {
      /* empty */
    },
    subscribeToMore: () => () => {
      /* empty */
    },
    updateQuery: () => () => {
      /* empty */
    }
  },

  currentStageInterval: {
    id: 'mockdata',
    name: 'Mock Stage Interval 1',
    interval: {
      id: 'mockintervalid',
      timeInterval: {
        startTimeSecs: 200,
        endTimeSecs: 250
      },
      activityInterval: {
        id: 'mockactivityintervalid',
        analystActivity: AnalystActivity.eventRefinement,
        name: 'Mock Activity Interval 1'
      }
    }
  },

  markActivityInterval: async () =>
    Promise.resolve({
      data: {},
      loading: false,
      stale: false,
      networkStatus: NetworkStatus.ready
    }),
  markStageInterval: async () =>
    Promise.resolve({
      data: {},
      loading: false,
      stale: false,
      networkStatus: NetworkStatus.ready
    }),
  setTimeInterval: async () =>
    Promise.resolve({
      data: {},
      loading: false,
      stale: false,
      networkStatus: NetworkStatus.ready
    }),
  setCurrentStageInterval: async (stageInterval: StageInterval) =>
    Promise.resolve({
      data: {
        loading: false,
        stale: false,
        networkStatus: NetworkStatus.ready
      }
    })
};

describe('workflow tests', () => {
  beforeEach(() => {
    Enzyme.configure({ adapter: new Adapter() });
  });

  // it('renders a snapshot', (done: jest.DoneCallback) => {
  //   const wrapper: ReactWrapper = Enzyme.mount(
  //     <ApolloProvider client={makeMockApolloClient()}>
  //       <Provider store={createStore()}>
  //         <ReduxApolloWorkflowContainer />
  //       </Provider>
  //     </ApolloProvider>
  //   );

  //   setImmediate(() => {
  //     wrapper.update();

  //     expect(wrapper.find(Workflow))
  //       .toMatchSnapshot();

  //     done();
  //   });
  // });

  it('displays spinner when loading', () => {
    const wrapper = Enzyme.shallow(<Workflow {...mockWorkflowProps} />);

    // Check that Workflow is not loading
    expect(wrapper.instance().props)
      .toHaveProperty('stagesQuery.loading', false);
    expect(wrapper.find(Spinner))
      .toHaveLength(0);

    // Change the loading prop
    wrapper.setProps({ stagesQuery: { ...mockWorkflowProps.stagesQuery, loading: true } });

    // Check that Workflow is loading
    expect(wrapper.instance().props)
      .toHaveProperty('stagesQuery.loading', true);
    expect(wrapper.find(Spinner))
      .toHaveLength(1);
  });

  it('displays non ideal state on error', () => {
    const wrapper = Enzyme.shallow(<Workflow {...mockWorkflowProps} />);

    // Check that Workflow has no error
    expect(wrapper.instance().props)
      .toHaveProperty('stagesQuery.error', undefined);
    expect(wrapper.find(NonIdealState))
      .toHaveLength(0);

    // Change the error prop
    const message = 'I am now non ideal';
    wrapper.setProps({
      stagesQuery: { ...mockWorkflowProps.stagesQuery, error: new GraphQLError(message) }
    });

    // Check that Workflow has error
    expect(wrapper.instance().props.stagesQuery.error)
      .toBeDefined();
    expect(wrapper.find(NonIdealState))
      .toHaveLength(1);
    expect(wrapper.find(NonIdealState)
      .hasClass(Classes.INTENT_DANGER));
    expect(wrapper.find(NonIdealState)
      .prop('description'))
      .toBe(message);
  });

  it.skip('can open a context menu on right click', (done: jest.DoneCallback) => {
    const wrapper: ReactWrapper = Enzyme.mount(
      <ApolloProvider client={makeMockApolloClient()}>
        <Provider store={createStore()}>
          <ReduxApolloWorkflowContainer />
        </Provider>
      </ApolloProvider>
    );

    setImmediate(() => {
      wrapper.update();

      // Check context menu is not open
      expect(ContextMenu.isOpen())
        .toBe(false);

      // Exercise double click
      const contextMenuTarget = wrapper.find(
        'div.gms-workflow-context-menu-target'
      );
      contextMenuTarget.first()
        .simulate('contextmenu');

      // Check context menu opened
      expect(ContextMenu.isOpen())
        .toBe(true);

      done();
    });
  });
});
