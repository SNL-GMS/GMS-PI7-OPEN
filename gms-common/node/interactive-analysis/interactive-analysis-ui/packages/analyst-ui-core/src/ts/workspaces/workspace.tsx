import { Classes, Colors, Intent, NonIdealState, Spinner } from '@blueprintjs/core';
import * as GoldenLayout from '@gms/golden-layout';
import * as elementResizeEvent from 'element-resize-event';
import * as lodash from 'lodash';
import * as React from 'react';
import * as Redux from 'redux';
import { ApolloProviderWrapper } from '~apollo/apollo-provider-wrapper';
import { uiConfig } from '~config/';
import { DataAcquisitionUiComponents } from '~data-acquisition-ui/';
import { createStore } from '~state/store';
import { getElectron } from '~util/electron-util';
import { showLogPopup } from '~util/log/logger';
import { AnalystUiComponents } from './analyst-ui';
import { WorkspaceState } from './types';
// electron instance; undefined if not running in electon
const electron = getElectron();

// tslint:disable-next-line:no-implicit-dependencies no-var-requires no-require-imports
const logo = require('~resources/gms-logo.png');

/**
 * Primary analyst workspace component. Uses golden-layout to create a configurable display of multiple
 * sub-components.
 */
export class Workspace extends React.Component<{}, WorkspaceState> {

  /**
   * Handle to the dom element where we will render the golden-layout workspace
   */
  private glContainerRef: HTMLDivElement;

  private gl: GoldenLayout;

  private readonly store: Redux.Store<any>;

  public constructor(props) {
    super(props);
    this.store = createStore();
    this.state = {
      wsConnected: true
    };
  }

  /**
   * Create the analyst workspace
   */
  public render() {
    return (
      <div
        style={{
          display: 'flex',
          height: '100%',
          width: '100%',
          WebkitUserSelect: 'none',
          flexDirection: 'column',
        }}
        className={Classes.DARK}
      >
        <nav
          className={`${Classes.NAVBAR} .modifier`}
          style={{
            backgroundColor: Colors.DARK_GRAY2,
            borderBottom: `2px solid ${Colors.BLACK}`,
            height: '35px'
          }}
        >
          <div
            className={`${Classes.NAVBAR_GROUP} ${Classes.ALIGN_LEFT}`}
            style={{ height: '33px' }}
          >
            <img
              src={logo}
              alt=""
              // tslint:disable-next-line:no-magic-numbers
              height={33}
              style={{ filter: 'invert(100%)' }}
            />
            <span style={{ marginLeft: '0.25rem' }}>GMS</span>
          </div>
          <div
            className={`${Classes.NAVBAR_GROUP} ${Classes.ALIGN_RIGHT}`}
            style={{ height: '35px' }}
          >
            <button
              className={`${Classes.BUTTON} ${Classes.MINIMAL}`}
              onClick={showLogPopup}
            >
              Logs
            </button>
            <button
              className={`${Classes.BUTTON} ${Classes.MINIMAL}`}
              onClick={() => { localStorage.removeItem('gms-analyst-ui-layout'); location.reload(); }}
            >
              Clear stored layout
            </button>
          </div>
        </nav>
        <div
          style={{
            display: 'flex',
            flex: '1 1 auto',
            height: '100%',
            width: '100%',
            maxWidth: '100%',
            position: 'relative',
          }}
          className={Classes.DARK}
        >
          {
            !this.state.wsConnected ?
              (
                <div
                  style={{
                    position: 'absolute',
                    top: '0px',
                    right: '0px',
                    bottom: '0px',
                    left: '0px',
                    zIndex: 100,
                    backgroundColor: 'rgba(0,0,0,0.85)',
                    display: 'flex',
                    justifyContent: 'center',
                    alignItems: 'center'
                  }}
                >
                  <NonIdealState
                    visual="error"
                    action={<Spinner intent={Intent.DANGER} />}
                    className={Classes.INTENT_DANGER}
                    title="No connection to server..."
                    description="Attempting to connect..."
                  />
                </div>
              )
              : null
          }
          <div
            style={{
              width: 'calc(100% - 50px)',
              maxWidth: '100%',
              flex: '1 1 auto',
            }}
            ref={ref => { this.glContainerRef = ref; }}
          />
        </div>
      </div>
    );
  }

  /**
   * On mount, initialize the golden-layout workspace
   */
  public componentDidMount() {
    this.configureGoldenLayout();
    this.registerWsClientEvents();
  }

  /**
   * configure & initialize the golden-layout workspace
   */
  private configureGoldenLayout() {
    if (this.gl) {
      this.destroyGl();
    }

    const savedConfig = localStorage.getItem('gms-analyst-ui-layout');
    if (savedConfig) {
      try {
        this.gl = new GoldenLayout(JSON.parse(savedConfig), this.glContainerRef);
        // if an update has changed the names of components, for example, need to start at default again
      } catch (e) {
        this.gl = new GoldenLayout(uiConfig.workspace, this.glContainerRef);
      }
    } else {
      this.gl = new GoldenLayout(uiConfig.workspace, this.glContainerRef);
    }

    const resizeDebounceMillis = 100;

    elementResizeEvent(this.glContainerRef, lodash.debounce(
      () => {
        this.gl.updateSize();
      },
      resizeDebounceMillis));

    this.registerComponents();

    (this.gl as any).on('stateChanged', () => {
      if (electron !== undefined && electron.ipcRenderer !== undefined) {
        electron.ipcRenderer.send('state-changed');
      }
      if (this.gl.isInitialised) {
        const state = JSON.stringify(this.gl.toConfig());
        localStorage.setItem('gms-analyst-ui-layout', state);
      }
    });
  }

  private readonly registerComponents = () => {
    try {
      this.gl.registerComponent(
        uiConfig.components.waveformDisplay.component,
        ApolloProviderWrapper(AnalystUiComponents.WaveformDisplay, this.store));
      this.gl.registerComponent(
        uiConfig.components.events.component,
        ApolloProviderWrapper(AnalystUiComponents.Events, this.store));
      this.gl.registerComponent(
        uiConfig.components.signalDetections.component,
        ApolloProviderWrapper(AnalystUiComponents.SignalDetections, this.store));
      this.gl.registerComponent(
        uiConfig.components.workflow.component,
        ApolloProviderWrapper(AnalystUiComponents.Workflow, this.store));
      this.gl.registerComponent(
        uiConfig.components.map.component,
        ApolloProviderWrapper(AnalystUiComponents.Map, this.store));
      this.gl.registerComponent(
        uiConfig.components.azimuthSlowness.component,
        ApolloProviderWrapper(AnalystUiComponents.AzimuthSlowness, this.store));
      this.gl.registerComponent(
        uiConfig.components.location.component,
        ApolloProviderWrapper(AnalystUiComponents.Location, this.store));
      this.gl.registerComponent(
        uiConfig.components.stationInformation.component,
        ApolloProviderWrapper(DataAcquisitionUiComponents.StationInformation, this.store));
      this.gl.registerComponent(
          uiConfig.components.statusConfiguration.component,
          ApolloProviderWrapper(DataAcquisitionUiComponents.StatusConfiguration, this.store));
      this.gl.registerComponent(
        uiConfig.components.stationConfiguration.component,
        ApolloProviderWrapper(DataAcquisitionUiComponents.StationConfiguration, this.store));
      this.gl.registerComponent(
        uiConfig.components.transferGaps.component,
        ApolloProviderWrapper(DataAcquisitionUiComponents.TransferGaps, this.store));
      this.gl.registerComponent(
          uiConfig.components.configureStationGroups.component,
          ApolloProviderWrapper(DataAcquisitionUiComponents.ConfigureStationGroups, this.store));
      this.gl.init();
      this.gl.updateSize();
    } catch (e) {
      // tslint:disable-next-line:no-console
      console.log('Golden Layout saved config out of date - Resetting to default');
      this.gl = new GoldenLayout(uiConfig.workspace, this.glContainerRef);
      this.registerComponents();
    }
  }

  private readonly destroyGl = () => {
    this.gl.destroy();
    // tslint:disable-next-line:newline-per-chained-call
    this.store.getState().apolloClient.client.resetStore()
      .catch();
  }

  private readonly registerWsClientEvents = () => {
    // tslint:disable-next-line:newline-per-chained-call
    this.store.getState().apolloClient.wsClient.on('disconnected', () => {
      this.setState({
        wsConnected: false
      });
    });
    // tslint:disable-next-line:newline-per-chained-call
    this.store.getState().apolloClient.wsClient.on('reconnected', () => {
      this.setState({
        wsConnected: true
      });
      // TODO be smarter about this, try and maintain user state & reload as necessary to make up for lost
      // subscription time
      window.location.reload();
      // this.configureGoldenLayout()
    });
  }
}
