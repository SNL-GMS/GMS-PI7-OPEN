import { Colors } from '@blueprintjs/core';
import * as Immutable from 'immutable';
import * as React from 'react';
import * as renderer from 'react-test-renderer';
import { AzimuthSlowness } from '~analyst-ui/components/azimuth-slowness';
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
  measurementMode: {
    entries: Immutable.Map(),
    mode: Mode.DEFAULT
  },
  currentStageInterval: stageInterval,
  currentTimeInterval: timeInterval,
  selectedSdIds: signalDetectionsIds,
  openEventId: eventId,
  selectedSortType: WaveformSortType.stationName,
  sdIdsToShowFk: [],
  analystActivity: AnalystActivity.eventRefinement,
  channelFilters: Immutable.Map<string, WaveformTypes.WaveformFilter>(),
  client: undefined,
  computeFkFrequencyThumbnails: undefined,
  setSelectedSdIds: (ids: string[]) => {
    /* no-op */
  },
  setSdIdsToShowFk: (ids: string[]) => {
    /* no-op */
  },
  computeFks: undefined,
  setWindowLead: undefined,
  markFksReviewed: undefined

};

it('AzimuthSlowness renders & matches snapshot', () => {
  const tree = renderer
    .create(
      <div
        style={{
          border: `1px solid ${Colors.GRAY3}`,
          resize: 'both',
          overflow: 'auto',
          height: '700px',
          width: '1000px'
        }}
      >
        <AzimuthSlowness
          {...azSlowReduxProps}
        />
      </div>
    )
    .toJSON();

  expect(tree)
    .toMatchSnapshot();
});
