import * as electron from 'electron';

import { SERVER_URL } from '.';

/**
 * Options used to create a popout-window
 */
export interface PopoutOptions {
    bounds?: {
        x: number;
        y: number;
    };
    config: {
        component: string;
        componentName: 'lm-react-component';
        isClosable: true;
        reorderEnabled: true;
        title: string;
        type: 'component';
    }[];
    title: string;
    url: string;
}

/**
 * Create a popout-window with the given configuration
 * @param options options
 * @param nextWindow next window
 */
export function createPopoutWindow(options: PopoutOptions, nextWindow: Electron.BrowserWindow) {
    const currentWindow = nextWindow;

    currentWindow.setTitle(options.title);

    currentWindow.on('show', () => {
        // nextWindow.setBounds({
        //     height: options.bounds.height,
        //     width: options.bounds.width,
        //     x: options.bounds.x,
        //     y: options.bounds.y,
        // });

        // set popout config  on the window so that it can pop itself back in correctly.
        // TODO figure out how to do this properly in electron (set metadata on a BrowserWindow)
        (currentWindow.webContents as any).popoutConfig = options.config[0];
        currentWindow.webContents.send('load-path', options.url.substring(options.url.indexOf('#/')));
    });

    currentWindow.show();

    // tslint:disable-next-line:no-parameter-reassignment
    nextWindow = new electron.BrowserWindow({
        autoHideMenuBar: true,
        show: false,
        backgroundColor: '#182026'
    });
    nextWindow.setMenuBarVisibility(false);

    nextWindow.loadURL(`${SERVER_URL}/#/loading`);

    return nextWindow;
}
