import * as GoldenLayout from '@gms/golden-layout';
import { getElectron } from '~util/electron-util';

// electron instance; undefined if not running in electon
const electron = getElectron();

export interface ComponentList {
  [componentKey: string]: {
    type: string;
    title: string;
    component: string;
  };
}

// ! CAUTION: when changing the golden-layout component name
// The route paths must match the `golden-layout` component name for popout windows
// For example, the component name `signal-detections` must have the route path of `signal-detections`
export const componentList: ComponentList = {
  waveformDisplay: {
    type: 'react-component',
    title: 'Waveforms',
    component: 'waveform-display'
  },
  events: {
    type: 'react-component',
    title: 'Events',
    component: 'events'
  },
  signalDetections: {
    type: 'react-component',
    title: 'Signal Detections',
    component: 'signal-detections'
  },
  workflow: {
    type: 'react-component',
    title: 'Workflow',
    component: 'workflow'
  },
  map: {
    type: 'react-component',
    title: 'Map',
    component: 'map'
  },
  azimuthSlowness: {
    type: 'react-component',
    title: 'Azimuth Slowness',
    component: 'azimuth-slowness'
  },
  stationInformation: {
    type: 'react-component',
    title: 'Station Information',
    component: 'station-information'
  },
  statusConfiguration: {
    type: 'react-component',
    title: 'Status Configuration',
    component: 'status-configuration'
  },
  transferGaps: {
    type: 'react-component',
    title: 'Transfer Gaps',
    component: 'transfer-gaps'
  },
  stationConfiguration: {
    type: 'react-component',
    title: 'Station Configuration',
    component: 'station-configuration'
  },
  location: {
    type: 'react-component',
    title: 'Location',
    component: 'location'
  },
  configureStationGroups: {
    type: 'react-component',
    title: 'Configure Station Groups',
    component: 'configure-station-groups'
  }
};

export const defaultGoldenLayoutConfig: GoldenLayout.Config = {
  settings: {
    showPopoutIcon: Boolean(electron),
    showMaximiseIcon: true,
    showCloseIcon: true,
  },
  content: [{
    type: 'row',
    content: [
      {
        type: 'column',
        content: [{
          ...componentList.map,
          height: 60
        }, {
          type: 'stack',
          content: [{
            ...componentList.events
          },
          {
            ...componentList.signalDetections
          },
          {
            ...componentList.azimuthSlowness
          },
          {
            ...componentList.statusConfiguration
          },
          {
            ...componentList.stationInformation
          },
          {
            ...componentList.stationConfiguration
          },
          {
            ...componentList.transferGaps
          },
          {
            ...componentList.location
          },
          {
            ...componentList.configureStationGroups
          }
        ]
        }],
        width: 60
      }, {
        type: 'column',
        content: [{
          ...componentList.workflow
        }, {
          ...componentList.waveformDisplay,
          height: 70,
        }]
      }
    ]

  }],
  dimensions: {
    borderWidth: 2,
    minItemHeight: 30,
    minItemWidth: 30,
  }
};
