import { Colors } from '@blueprintjs/core';
import { CommonTypes, EventTypes } from '~graphql/';
import { QcMaskCategory } from './system-config';

export interface MaskDisplayFilter {
  color: string;
  visible: boolean;
  name: string;
}

export interface QcMaskDisplayFilters {
  ANALYST_DEFINED: MaskDisplayFilter;
  CHANNEL_PROCESSING: MaskDisplayFilter;
  DATA_AUTHENTICATION: MaskDisplayFilter;
  REJECTED: MaskDisplayFilter;
  STATION_SOH: MaskDisplayFilter;
  WAVEFORM_QUALITY: MaskDisplayFilter;
}

export interface UserPreferences {
  azimuthSlowness: {
    defaultLead: number;
    defaultLength: number;
    defaultStepSize: number;
    // Minimum hiehgt and width of fk rendering
    minFkLengthPx: number;
    // Maximum height and width of fk rendering
    maxFkLengthPx: number;
    predictedLineForAzimuth: {
      color: string;
    };
    predictedLineForSlowness: {
      color: string;
    };
  };
  colors: {
    brushes: {
      zoom: string;
      qcCreateMask: string;
    };
    events: {
      toWork: string;
      inProgress: string;
      complete: string;
      noSignalDetections: string;
      edge: string;
      conflict: string;
    };
    signalDetections: {
      unassociated: string;
      newDetection: string;
    };
    predictedPhases: {
      color: string;
      filter: string;
    };
    waveforms: {
      raw: string;
      maskDisplayFilters: QcMaskDisplayFilters;
    };
    system: {
      selectionColor: string;
      evenRow: string;
      subsetSelected: string;
    };
  };
  map: {
    icons: {
      event: string;
      eventScale: number;
      station: string;
      stationScale: number;
      scaleFactor: number;
      displayDistance: number;
      pixelOffset: number;
    };
    colors: {
      openEvent: string;
      unselectedStation: string;
      selectedEvent: string;
      completeEvent: string;
      toWorkEvent: string;
      outOfIntervalEvent: string;
    };
    widths: {
      unselectedSignalDetection: number;
      selectedSignalDetection: number;
    };
    defaultTo3D: boolean;
  };
  dataAcquisition: {
    pkiWarning: string;
    pkiStrongWarning: string;
  };
  defaultSignalDetectionPhase: CommonTypes.PhaseType;
  signalDetectionList: {
    autoFilter: boolean;
    showIds: boolean;
  };
  eventList: {
    showIds: boolean;
  };
  location: {
    preferredLocationSolutionRestraintOrder: EventTypes.DepthRestraintType[];
    changedSdHighlight: string;
    historicalModeTableColor: string;
    newlyDefiningColor: string;
    newlyUndefiningColor: string;
    removedSdColor: string;
    addedSdColor: string;
  };
  list: {
    minWidthPx: number;
    widthOfTableMarginsPx: number;
  };
  distanceUnits: CommonTypes.DistanceUnits;
}
const openEventColor: string = Colors.ORANGE3;
export const userPreferences: UserPreferences = {
  azimuthSlowness: {
    defaultLead: 1,
    defaultLength: 4,
    defaultStepSize: 5,
    // Minimum hiehgt and width of fk rendering
    minFkLengthPx: 265,
    // Maximum height and width of fk rendering
    maxFkLengthPx: 500,
    predictedLineForAzimuth: {
      color: 'grey'
    },
    predictedLineForSlowness: {
      color: 'grey'
    }
  },
  dataAcquisition: {
    pkiWarning: Colors.GOLD5,
    pkiStrongWarning: '#bd2723'
  },
  colors: {
    system: {
      selectionColor: '#0e5a8a',
      evenRow: '#202b33',
      subsetSelected: '#5d7c91'
    },
    brushes: {
      zoom: 'rgba(255,51,204,0.3)',
      qcCreateMask: 'rgba(145, 228, 151, .03)'
    },
    events: {
      toWork: 'lightgrey',
      inProgress: openEventColor,
      complete: Colors.FOREST5,
      noSignalDetections: Colors.GOLD5,
      edge: Colors.DARK_GRAY2,
      conflict: Colors.RED1
    },
    signalDetections: {
      unassociated: Colors.RED3,
      newDetection: '#9b59b6'
    },
    predictedPhases: {
      color: openEventColor,
      filter: 'opacity(.5)'
    },
    waveforms: {
      raw: Colors.COBALT4,
      maskDisplayFilters: {
        ANALYST_DEFINED: {
          color: Colors.FOREST3,
          visible: true,
          name: QcMaskCategory.ANALYST_DEFINED
        },
        CHANNEL_PROCESSING: {
          color: Colors.TURQUOISE3,
          visible: true,
          name: QcMaskCategory.CHANNEL_PROCESSING
        },
        DATA_AUTHENTICATION: {
          color: Colors.INDIGO3,
          visible: true,
          name: QcMaskCategory.DATA_AUTHENTICATION
        },
        REJECTED: {
          color: Colors.RED3,
          visible: false,
          name: QcMaskCategory.REJECTED
        },
        STATION_SOH: {
          color: Colors.ORANGE3,
          visible: true,
          name: QcMaskCategory.STATION_SOH
        },
        WAVEFORM_QUALITY: {
          color: Colors.VIOLET3,
          visible: true,
          name: QcMaskCategory.WAVEFORM_QUALITY
        }
      }
    }
  },
  map: {
    icons: {
      event: 'circle-transition.png',
      eventScale: 0.07,
      station: 'outlined-triangle.png',
      stationScale: 0.12,
      scaleFactor: 1.5,
      displayDistance: 1e6,
      pixelOffset: 15
    },
    colors: {
      openEvent: openEventColor,
      unselectedStation: '#ffffff',
      selectedEvent: '#00ffff',
      completeEvent: '#00ff00',
      toWorkEvent: '#f50a37',
      outOfIntervalEvent: '#cccccc'
    },
    widths: {
      unselectedSignalDetection: 1,
      selectedSignalDetection: 3
    },
    defaultTo3D: false
  },
  defaultSignalDetectionPhase: CommonTypes.PhaseType.P,
  signalDetectionList: {
    autoFilter: true,
    showIds: false
  },
  eventList: {
    showIds: false
  },
  location: {
    preferredLocationSolutionRestraintOrder:
      [EventTypes.DepthRestraintType.UNRESTRAINED, EventTypes.DepthRestraintType.FIXED_AT_SURFACE,
      EventTypes.DepthRestraintType.FIXED_AT_DEPTH],
    changedSdHighlight: '#a07e27',
    historicalModeTableColor: '#6F6F6F',
    newlyDefiningColor: '#336941',
    newlyUndefiningColor: '#990000',
    removedSdColor: '#990000',
    addedSdColor: '#336941'
  },
  list: {
    minWidthPx: 60,
    widthOfTableMarginsPx: 16,
  },
  distanceUnits: CommonTypes.DistanceUnits.degrees
};
