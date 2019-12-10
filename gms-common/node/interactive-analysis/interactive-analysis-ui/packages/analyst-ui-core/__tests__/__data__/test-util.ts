import { NetworkStatus } from 'apollo-client';
import * as Immutable from 'immutable';
import * as _ from 'lodash';
// tslint:disable-next-line:max-line-length
import { AnalystCurrentFk } from '~analyst-ui/components/azimuth-slowness/components/fk-rendering/fk-rendering';
import {
  FilterType,
  FkThumbnailSize
  // tslint:disable-next-line:max-line-length
} from '~analyst-ui/components/azimuth-slowness/components/fk-thumbnail-list/fk-thumbnails-controls';
import {
  AzimuthSlownessReduxProps,
  AzimuthSlownessState,
  FkUnits
  // tslint:disable-next-line:max-line-length
} from '~analyst-ui/components/azimuth-slowness/types';
import { CommonTypes, WaveformTypes } from '~graphql/';
import {
  AnalystActivity,
  Mode,
  StageInterval,
  TimeInterval,
  WaveformSortType,
} from '~state/analyst-workspace/types';
import {
  AppState
} from '~state/types';
import { eventData } from './event-data';
import { signalDetectionsData } from './signal-detections-data';

export const fkThumbnailSize = FkThumbnailSize.MEDIUM;
export const fkFilterType = FilterType.firstP;

// 11:59:59 05/19/2010
export const startTimeSeconds = 1274313599;

// 02:00:01 05/20/2010
export const endTimeSeconds = 1274320801;

// time block 2 hours = 7200 seconds
export const timeBlock = 7200;

export const timeInterval: TimeInterval = {
  startTimeSecs: startTimeSeconds,
  endTimeSecs: endTimeSeconds
};

export const stageInterval: StageInterval = {
  id: '1',
  name: '2',
  interval: {
    id: '3',
    timeInterval,
    activityInterval: {
      id: '1',
      name: '2',
      analystActivity: AnalystActivity.eventRefinement
    }
  }
};

export const currentProcStageIntId = '3';

export const networkStatus = NetworkStatus.ready;

export const analystCurrentFk: AnalystCurrentFk = {
  x: 10,
  y: 11
};

export const eventIds = _.uniq(
  _.map([eventData], 'id')
);

export const eventId = _.uniqBy(
  [eventData],
  'eventHypothesisId'
)[0].id;

export const singleEvent = _.uniqBy(
  [eventData],
  'eventHypothesisId'
)[0];

const sdIdsFullMap: string[] = signalDetectionsData.map(
  sd => sd.id
);

export const signalDetectionsIds = _.uniq(sdIdsFullMap);

export const selectedSignalDetectionID = signalDetectionsIds[0];

export const analystAppState: AppState = {
  analystWorkspaceState: {
    currentStageInterval: stageInterval,
    createSignalDetectionPhase: CommonTypes.PhaseType.P,
    selectedEventIds: eventIds,
    openEventId: eventId,
    selectedSdIds: signalDetectionsIds,
    sdIdsToShowFk: [],
    selectedSortType: WaveformSortType.stationName,
    channelFilters: Immutable.Map<string, WaveformTypes.WaveformFilter>(),
    measurementMode: {
      mode: undefined,
      entries: Immutable.Map<string, boolean>()
    }
  },
  apolloClient: undefined,
  dataAcquisitionWorkspaceState: {
    selectedStationIds: undefined,
    selectedProcessingStation: undefined,
    unmodifiedProcessingStation: undefined
  }

};

export const azSlowProps: AzimuthSlownessReduxProps = {
  currentStageInterval: stageInterval,
  currentTimeInterval: timeInterval,
  selectedSdIds: signalDetectionsIds,
  selectedSortType: WaveformSortType.stationName,
  openEventId: eventId,
  sdIdsToShowFk: [],
  analystActivity: AnalystActivity.eventRefinement,
  measurementMode: {
    entries: Immutable.Map(),
    mode: Mode.DEFAULT
  },
  channelFilters: Immutable.Map<string, WaveformTypes.WaveformFilter>(),
  client: undefined,
  setSelectedSdIds: () => {
    // tslint:disable-next-line:no-console
    console.log('azSlowProps - setSelectedSdIds');
  },
  setSdIdsToShowFk: () => {
    // tslint:disable-next-line:no-console
    console.log('azSlowProps - setSdIdsToShowFk');
  }
};

export const AzSlowState: AzimuthSlownessState = {
  fkThumbnailSizePx: fkThumbnailSize,
  filterType: FilterType.all,
  userInputFkFrequency: {
    minFrequencyHz: 1,
    maxFrequencyHz: 4
  },
  fkUnitsForEachSdId: Immutable.Map<string, FkUnits>(),
  userInputFkWindowParameters: {
    lengthSeconds: 4,
    leadSeconds: 1,
    stepSize: 1
  },
  fkInnerContainerWidthPx: 200,
  numberOfOutstandingComputeFkMutations: 1,
  fkThumbnailColumnSizePx: 255,
  fkFrequencyThumbnails: undefined
};
