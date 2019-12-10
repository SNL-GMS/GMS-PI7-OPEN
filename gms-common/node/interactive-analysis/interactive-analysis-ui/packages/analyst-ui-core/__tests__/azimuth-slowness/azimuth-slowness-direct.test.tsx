import * as Immutable from 'immutable';
import * as React from 'react';
import { AzimuthSlowness } from '~analyst-ui/components/azimuth-slowness/azimuth-slowness-component';
import { AzimuthSlownessProps } from '~analyst-ui/components/azimuth-slowness/types';
import { WaveformTypes } from '~graphql/';
import { AnalystActivity, Mode, WaveformSortType } from '~state/analyst-workspace/types';
import { signalDetectionsData } from '../__data__/signal-detections-data';
import {
  eventId,
  signalDetectionsIds,
  stageInterval,
  timeInterval
} from '../__data__/test-util';

// tslint:disable-next-line:no-implicit-dependencies no-var-requires no-require-imports
const Enzyme = require('enzyme');

const azSlowReduxProps: AzimuthSlownessProps = {
  defaultStationsQuery: {
    defaultStations: [],
    error: undefined,
    loading: false,
    networkStatus: undefined,
    fetchMore: undefined,
    refetch: undefined,
    startPolling: undefined,
    stopPolling: undefined,
    subscribeToMore: () => () => {
      /**/
    },
    updateQuery: undefined,
    variables: undefined
  },
  defaultWaveformFiltersQuery: {
    defaultWaveformFilters: [],
    error: undefined,
    loading: false,
    networkStatus: undefined,
    fetchMore: undefined,
    refetch: undefined,
    startPolling: undefined,
    stopPolling: undefined,
    subscribeToMore: () => () => {
      /**/
    },
    updateQuery: undefined,
    variables: undefined
  },
  signalDetectionsByStationQuery: {
    signalDetectionsByStation: signalDetectionsData,
    error: undefined,
    loading: false,
    networkStatus: undefined,
    fetchMore: undefined,
    refetch: undefined,
    startPolling: undefined,
    stopPolling: undefined,
    subscribeToMore: () => () => {
      /**/
    },
    updateQuery: undefined,
    variables: undefined
  },
  distanceToSourceForDefaultStationsQuery: {
    distanceToSourceForDefaultStations: [],
    error: undefined,
    loading: false,
    networkStatus: undefined,
    fetchMore: undefined,
    refetch: undefined,
    startPolling: undefined,
    stopPolling: undefined,
    subscribeToMore: () => () => {
      /**/
    },
    updateQuery: undefined,
    variables: undefined
  },
  eventsInTimeRangeQuery: {
    eventsInTimeRange: [],
    error: undefined,
    loading: false,
    networkStatus: undefined,
    fetchMore: undefined,
    refetch: undefined,
    startPolling: undefined,
    stopPolling: undefined,
    subscribeToMore: () => () => {
      /**/
    },
    updateQuery: undefined,
    variables: undefined
  },
  currentStageInterval: stageInterval,
  currentTimeInterval: timeInterval,
  selectedSdIds: signalDetectionsIds,
  openEventId: eventId,
  selectedSortType: WaveformSortType.distance,
  sdIdsToShowFk: [],
  analystActivity: AnalystActivity.eventRefinement,
  measurementMode: {
    entries: Immutable.Map(),
    mode: Mode.DEFAULT
  },
  channelFilters: Immutable.Map<string, WaveformTypes.WaveformFilter>(),
  client: undefined,
  computeFkFrequencyThumbnails: undefined,
  setSelectedSdIds: (ids: string[]) => {
    /* no-op */
  },
  computeFks: undefined,
  setWindowLead: undefined,
  setSdIdsToShowFk: (ids: string[]) => {
    /* no-op */
  },
  markFksReviewed: undefined
};

describe('AzimuthSlowness Direct', () => {
  test('AzimuthSlowness renders directly with data correctly', () => {
    const wrapper = Enzyme.shallow(<AzimuthSlowness {...azSlowReduxProps} />);
    expect(wrapper)
      .toMatchSnapshot();
  });
});
