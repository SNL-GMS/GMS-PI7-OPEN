import '@blueprintjs/core/src/blueprint.scss';
import '@blueprintjs/datetime/src/blueprint-datetime.scss';
import '@blueprintjs/icons/src/blueprint-icons.scss';
import './style.scss';

import {
  Button,
  ButtonGroup,
  Classes,
  Colors
} from '@blueprintjs/core';
import { IconNames } from '@blueprintjs/icons';
import * as React from 'react';
import * as ReactDOM from 'react-dom';
import { HashRouter, Link, Route } from 'react-router-dom';
import { EventsExample } from './example-events';
import { MultipleDisplaysExample } from './example-multiple-displays';
import { RecordSectionExample } from './example-record-section';
import { WeavessExample } from './example-weavess';
import { Home } from './home';

(window as any).React = React;
(window as any).ReactDOM = ReactDOM;

const App = (): any => (
  <div id="app-content">
    <HashRouter>
      <div
        className={Classes.DARK}
        style={{
          height: '100%',
          width: '100%',
          padding: '0.5rem',
          color: Colors.GRAY4
        }}
      >
        <ButtonGroup minimal={true}>
          <Button icon={IconNames.HOME}>
            <Link to="/">Home</Link>
          </Button>
          <Button icon={IconNames.CHART}>
            <Link to="/WeavessExample"> Weavess Example</Link>
          </Button>
          <Button icon={IconNames.MULTI_SELECT}>
            <Link to="/MultipleDisplaysExample"> Multiple Displays Example</Link>
          </Button>
          <Button icon={IconNames.TIMELINE_EVENTS}>
            <Link to="/EventsExample"> Events</Link>
          </Button>
          <Button icon={IconNames.RECORD}>
            <Link to="/RecordSectionExample"> Record Section Example</Link>
          </Button>
        </ButtonGroup>

        <hr />
        <Route exact={true} path="/" component={Home} />
        <Route exact={true} path="/WeavessExample" component={WeavessExample} />
        <Route
          exact={true}
          path="/MultipleDisplaysExample"
          component={MultipleDisplaysExample}
        />
        <Route exact={true} path="/EventsExample" component={EventsExample} />
        <Route
          exact={true}
          path="/RecordSectionExample"
          component={RecordSectionExample}
        />
      </div>
    </HashRouter>
  </div>
);

window.onload = () => {
  ReactDOM.render(<App />, document.getElementById('app'));
};
