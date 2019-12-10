import * as Redux from 'redux';
import { createLogger } from 'redux-logger';
import { default as thunk } from 'redux-thunk';
import { getElectron, getElectronEnhancer } from '../util/electron-util';
import { IS_NODE_ENV_DEVELOPMENT } from '../util/environment';
import { isWindowDefined } from '../util/window-util';
import { initialAppState } from './initial-state';
import { Reducer } from './root-reducer';
import { AppState } from './types';

declare var window;

const windowIsDefined = isWindowDefined();

const windowRedux: Window & {
  __REDUX_DEVTOOLS_EXTENSION_COMPOSE__?(a: any): void;
} = windowIsDefined ? window : undefined;

const electron = getElectron();
const electronEnhancer = getElectronEnhancer();

const composeEnhancers = (windowRedux) ?
  (windowRedux as any).__REDUX_DEVTOOLS_EXTENSION_COMPOSE__ != null
    ? (windowRedux as any).__REDUX_DEVTOOLS_EXTENSION_COMPOSE__
    : Redux.compose
  : Redux.compose;

const configureStore = (initialState?: Partial<AppState> | void) => {
  let store: Redux.Store;

  const middlewares = [];
  middlewares.push(thunk);
  if (IS_NODE_ENV_DEVELOPMENT) {
    const logger = createLogger({
      collapsed: true,
      duration: true,
      timestamp: false,
      level: 'info',
      logger: console,
      logErrors: true,
      diff: false
    });
    middlewares.push(logger);
  }

  const enhancers =
    (electron && electron !== undefined && electronEnhancer) ?
      composeEnhancers(
        Redux.applyMiddleware(...middlewares),
        // Must be placed after any enhancers which dispatch
        // their own actions such as redux-thunk or redux-saga
        electronEnhancer({
          dispatchProxy: a => store.dispatch(a),
        }))
      :
      composeEnhancers(Redux.applyMiddleware(...middlewares));

  store = Redux.createStore(
    Reducer,
    initialState as any,
    enhancers);
  return store;
};

// tslint:disable-next-line:no-default-export
export const createStore = (): Redux.Store<AppState> => {
  const store = configureStore(initialAppState);
  return store;
};
