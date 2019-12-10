import '@blueprintjs/core/src/blueprint.scss';
import '@blueprintjs/datetime/src/blueprint-datetime.scss';
import '@blueprintjs/icons/src/blueprint-icons.scss';
import 'cesium/Widgets/widgets.css';
import * as JQuery from 'jquery';
import * as React from 'react';
import * as ReactDom from 'react-dom';
import '../css/goldenlayout-base.scss';
import '../css/goldenlayout-blueprint-theme.scss';

// Combined scss of all components
import '../css/components/_all.scss';
// ensure that this import is last
// tslint:disable-next-line:ordered-imports
import '../css/style.scss';
// required for golden-layout
(window as any).React = React;
(window as any).ReactDOM = ReactDom;
(window as any).$ = JQuery;
(window as any).CESIUM_BASE_URL = './cesium';

import { Classes, Colors } from '@blueprintjs/core';
import { HashRouter, Route, Switch } from 'react-router-dom';
import {
  AzimuthSlowness,
  Events,
  LoadingScreen,
  Location,
  Map,
  SignalDetections,
  WaveformDisplay,
  Workflow,
} from '~analyst-ui/components';
import { ApolloProviderWrapper } from '~apollo/apollo-provider-wrapper';
import {
  ConfigureStationGroups,
  StationInformation,
  StatusConfiguration,
  TransferGaps
} from '~data-acquisition-ui/components';
import { createStore } from '~state/store';
import { getElectron } from '~util/electron-util';
import { UILogger } from '~util/log/logger';
import { Workspace } from './workspaces';

// electron instance; undefined if not running in electon
const electron = getElectron();

const App = (): any => (

  <HashRouter>
    {
      // ! CAUTION: when changing the route paths
      // The route paths must match the `golden-layout` component name for popout windows
      // For example, the component name `signal-detections` must have the route path of `signal-detections`
    }
    <Switch>
      <Route
        path="/loading"
        component={LoadingScreen}
      />
      <Route
        path="/waveform-display"
        component={props => createPopoutComponent(WaveformDisplay, props)}
      />
      <Route
        path="/events"
        component={props => createPopoutComponent(Events, props)}
      />
      <Route
        path="/signal-detections"
        component={props => createPopoutComponent(SignalDetections, props)}
      />
      <Route
        path="/workflow"
        component={props => createPopoutComponent(Workflow, props)}
      />
      <Route
        path="/map"
        component={props => createPopoutComponent(Map, props)}
      />
      <Route
        path="/azimuth-slowness"
        component={props => createPopoutComponent(AzimuthSlowness, props)}
      />
      <Route
        path="/station-information"
        component={props => createPopoutComponent(StatusConfiguration, props)}
      />
      <Route
        path="/status-configuration"
        component={props => createPopoutComponent(StationInformation, props)}
      />
      <Route
        path="/transfer-gaps"
        component={props => createPopoutComponent(TransferGaps, props)}
      />
      <Route
        path="/location"
        component={props => createPopoutComponent(Location, props)}
      />
      <Route
        path="/configure-station-groups"
        component={props => createPopoutComponent(ConfigureStationGroups, props)}
      />
      <Route
        path="/"
        component={Workspace}
      />
    </Switch>
  </HashRouter>
);

window.onload = () => {
  // log user info to the gateway
  if (navigator) {
    UILogger.info(` client connected ${navigator.userAgent.toLowerCase()}`);
  }
  if (!window.navigator.userAgent.includes('Chrome') && !window.navigator.userAgent.includes('Firefox')) {
    window.alert(`GMS Interactive Analysis currently supports
            Google Chrome > v59 and Firefox > v. You will likely experience degraded performance`);
  }
  ReactDom.render(<App />, document.getElementById('app'));
};

if (electron !== undefined && electron.ipcRenderer !== undefined) {
  electron.ipcRenderer.on('load-path', (event, newHash: string) => {
    window.location.hash = newHash;
  });
}

/**
 * Wrap the component with everything it needs to live standalone as a popout
 */
function createPopoutComponent(Component: any, props: any) {

  const store = createStore();

  const WrappedComponent: any = ApolloProviderWrapper(Component, store);

  const PopoutComponent = class extends React.Component<any, {}> {
    /**
     * Create the pop-out wrapper component
     */
    public render() {
      return (
        <div
          style={{
            width: '100%',
            height: '100%',
            backgroundColor: Colors.DARK_GRAY2
          }}
          className={Classes.DARK}
        >
          <WrappedComponent
            {...this.props}
          />
          {
            // only show pop-in button if running in electron
            electron && electron !== undefined && electron.ipcRenderer !== undefined ?
              <div
                className="lm_popin"
                title="pop-in"
                onClick={() => {
                  electron.ipcRenderer.send(
                    'popin-window', electron.remote.getCurrentWebContents().popoutConfig);
                  electron.remote.getCurrentWindow()
                    .close();
                }}
              >
                <div className="lm_icon" />
                <div className="lm_bg" />
              </div>
              : undefined
          }
        </div>
      );
    }
  };

  return (
    <PopoutComponent {...props} />
  );
}
