import { createStore } from '@gms/analyst-ui-core';
import * as electron from 'electron';
import * as lodash from 'lodash';
import * as process from 'process';

import { buildApplicationMenu } from './application-menu';
import { createMainWindow } from './main-window';
import { clearLayout, loadLayout, persistLayout } from './persist-layout';
import { createPopoutWindow, PopoutOptions } from './popout-window';

// for debugging purposes, add context menu for inspecting elements;
/* tslint:disable */
// require('electron-context-menu')({});
/* tslint:enable */

// Module to control application life.
const app = electron.app;

// default url populated at compile time by webpack
declare var DEFAULT_SERVER_URL;
export const SERVER_URL: string = process.env.SERVER_URL || DEFAULT_SERVER_URL;

// set up redux in the main process
createStore();

// Keep a global reference of the window object, if you don't, the window will
// be closed automatically when the JavaScript object is garbage collected.
let mainWindow: Electron.BrowserWindow;
export let nextPopoutWindow: Electron.BrowserWindow;

/**
 * Set up the application.
 * Load layout and re-hydrate stored layout, or initialize a blank layout
 */
async function initialize() {

    electron.Menu.setApplicationMenu(buildApplicationMenu(app));

    if (process.argv.indexOf('--clear') > -1) {
        await clearLayout();
    }

    const layout = await loadLayout();
    if (lodash.isEmpty(layout)) {
        mainWindow = createMainWindow();
    } else {
        loadSavedConfiguration(layout);
    }

    mainWindow.on('close', () => {
        app.quit();
    });

    nextPopoutWindow = new electron.BrowserWindow({
        show: false,
        backgroundColor: '#182026'
    });
    nextPopoutWindow.setMenuBarVisibility(false);

    nextPopoutWindow.loadURL(`${SERVER_URL}/#/loading`);

    // pop-out events from the main window will broadcast on this channel.
    electron.ipcMain.on('popout-window', (event, args) => {
        nextPopoutWindow = createPopoutWindow(args, nextPopoutWindow);
    });

    // pop-in events from pop-out windows will broadcast on this channel.
    electron.ipcMain.on('popin-window', (event, args) => {
        // fire a popin-resolve event to the main window
        mainWindow.webContents.send('popin-window-resolve', args);
    });

    electron.ipcMain.on('state-changed', (event, args) => {
        persistLayout(nextPopoutWindow);
    });
}

/**
 * Load & rehydrate a saved configuration
 * @param layout layout
 */
function loadSavedConfiguration(layout) {
    layout.forEach(windowLayout => {
        if (windowLayout === null) return;
        const { bounds, url, title, popoutConfig } = windowLayout;
        const window = new electron.BrowserWindow({
            title,
            x: bounds.x,
            y: bounds.y,
            height: bounds.height,
            width: bounds.width,
            backgroundColor: '#182026'
        });
        window.loadURL(url);
        if (popoutConfig) {
            window.setMenuBarVisibility(false);
            (window.webContents as any).popoutConfig = popoutConfig;
        } else {
            mainWindow = window;
        }
    });
}

/**
 * Create a pop-out window 
 * @param options options
 */
export function popout(options: PopoutOptions) {
    nextPopoutWindow = createPopoutWindow(options, nextPopoutWindow);
}

// This method will be called when Electron has finished
// initialization and is ready to create browser windows.
// Some APIs can only be used after this event occurs.
app.on('ready', initialize);

app.on('browser-window-created', (event, window) => {
    if (SERVER_URL.includes('localhost')) {
        window.webContents.openDevTools();
    }
    window.on('move', e => {
        persistLayout(nextPopoutWindow);
    });
});

app.on('window-all-closed', () => {
    app.quit();
});
