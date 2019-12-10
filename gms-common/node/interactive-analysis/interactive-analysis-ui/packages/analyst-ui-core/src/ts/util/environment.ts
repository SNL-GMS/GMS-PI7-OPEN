import { isWindowDefined } from './window-util';

const windowIsDefined = isWindowDefined();

/**
 * The NODE_ENV environment varaiable.
 */
export const NODE_ENV = process.env.NODE_ENV;

/**
 * True if NODE_ENV is set to development.
 */
export const IS_NODE_ENV_DEVELOPMENT = process.env.NODE_ENV === 'development';

/**
 * True if NODE_ENV is set to production.
 */
export const IS_NODE_ENV_PRODUCTION = process.env.NODE_ENV === 'production';

/**
 * The GRAPHQL_PROXY_URI environment varaiable (or the default value if not set).
 */
export const GRAPHQL_PROXY_URI =
  windowIsDefined ?
    process.env.GRAPHQL_PROXY_URI || `${window.location.protocol}//${window.location.host}` : undefined;

/**
 * The SUBSCRIPTIONS_PROXY_URI environment varaiable (or the default value if not set).
 */
export const SUBSCRIPTIONS_PROXY_URI =
  windowIsDefined ?
    process.env.SUBSCRIPTIONS_PROXY_URI || `ws://${window.location.host}` : undefined;

/**
 * The CESIUM_OFFLINE environment varaiable.
 */
export const CESIUM_OFFLINE = (process.env.CESIUM_OFFLINE) ?
  !(process.env.CESIUM_OFFLINE === 'null' ||
  process.env.CESIUM_OFFLINE === 'undefined' ||
  process.env.CESIUM_OFFLINE === 'false') : false;
