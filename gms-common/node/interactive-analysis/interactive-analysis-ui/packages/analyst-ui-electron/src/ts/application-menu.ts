import * as electron from 'electron';
import { popout, SERVER_URL } from './';
import { clearLayout } from './persist-layout';
import { PopoutOptions } from './popout-window';

/**
 * create a popout-window of the appropriate component.
 * @param component component
 * @param title title
 */
function createPopoutFromApplicationMenu(component: string, title: string) {
  const options: PopoutOptions = {
    config: [{
      component,
      componentName: 'lm-react-component',
      isClosable: true,
      reorderEnabled: true,
      title,
      type: 'component'
    }],
    title,
    url: `${SERVER_URL}/#/${component}`
  };

  popout(options);
}

export const buildApplicationMenu = (app: Electron.App) => {
  const template = [
    {
      label: 'Open Window',
      submenu: [
        {
          label: 'Analysis...',
          submenu: [
            {
              label: 'Workflow',
              click: () => {
                createPopoutFromApplicationMenu('workflow', 'Workflow');
              }
            }, {
              label: 'Signal Detection List',
              click: () => {
                createPopoutFromApplicationMenu('signal-detection-list', 'Signal Detection List');
              }
            }, {
              label: 'Event List',
              click: () => {
                createPopoutFromApplicationMenu('event-list', 'Event List');
              }
            }, {
              label: 'Map',
              click: () => {
                createPopoutFromApplicationMenu('map', 'Map');
              }
            }, {
              label: 'Waveform Display',
              click: () => {
                createPopoutFromApplicationMenu('waveform-display', 'Waveform Display');
              }
            },
            {
              label: 'Azimuth Slowness',
              click: () => {
                createPopoutFromApplicationMenu('azimuth-slowness', 'Azimuth Slowness');
              }
            }
          ]
        }, {
          label: 'Monitoring...'
        }
      ]
    }, {
      label: 'Layout',
      submenu: [
        {
          label: 'Clear Electron Layout',
          click: async () => {
            await clearLayout();
            app.exit(0);
          }
        }
      ]
    }
  ];

  if (process.platform === 'darwin') {
    template.unshift({
      label: 'GMS',
      submenu: []
    });
  }

  return electron.Menu.buildFromTemplate(template);
};
