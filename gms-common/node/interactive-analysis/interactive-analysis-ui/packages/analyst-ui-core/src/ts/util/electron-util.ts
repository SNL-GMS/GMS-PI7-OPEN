import { isWindowDefined } from './window-util';

declare var require;

/**
 * Returns true if running in Electron; false otherwise.
 */
export const isElectron = () => {
  // Renderer process
  if (isWindowDefined() &&
    typeof (window as any).process === 'object' &&
    (window as any).process.type === 'renderer') {
    return true;
  }

  // Main process
  if (typeof process !== 'undefined' &&
    typeof process.versions === 'object' &&
    !!(process.versions as any).electron) {
    return true;
  }

  // Detect the user agent when the `nodeIntegration` option is set to true
  if (typeof navigator === 'object' &&
    typeof navigator.userAgent === 'string' &&
    navigator.userAgent.indexOf('Electron') >= 0) {
    return true;
  }

  return false;
};

/**
 * Returns the electron instance; undefined if not running in electron.
 */
export const getElectron = () => {
  try {
    if (isElectron()) {
      // tslint:disable-next-line:no-implicit-dependencies no-var-requires no-require-imports
      const electron = require('electron');
      if (typeof electron !== undefined) {
        return electron;
      }
    }
  } catch (error) {
    // failed to require electron
  }
  return undefined;
};

/**
 * Returns the electron enhancer instance; undefined if not running in electron.
 */
export const getElectronEnhancer = () => {
  try {
    if (isElectron()) {
      // tslint:disable-next-line:no-implicit-dependencies no-var-requires no-require-imports
      const electronEnhancer = require('redux-electron-store').electronEnhancer;
      if (typeof electronEnhancer !== undefined) {
        return electronEnhancer;
      }
    }
  } catch (error) {
    // failed to require electron enhancer
  }
  return undefined;
};
