import * as electron from 'electron';

import { SERVER_URL } from './';

/**
 * Create a BrowserWindow which will be the primary window (containing the analyst workspace)
 */
export function createMainWindow() {
    const window = new electron.BrowserWindow({
        width: 1500,
        height: 900,
        title: 'GMS / Interactive Analysis',
        backgroundColor: '#182026'
    });

    window.loadURL(SERVER_URL);

    return window;
}
