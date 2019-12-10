import * as Immutable from 'immutable';
import { createApolloClient } from '../apollo';
import { PhaseType } from '../graphql/common/types';
import { WaveformFilter } from '../graphql/waveform/types';
import { AnalystWorkspaceTypes } from './analyst-workspace';
import { Mode } from './analyst-workspace/types';
import { AppState } from './types';

export const initialAppState: AppState = {
  apolloClient: createApolloClient(),
  analystWorkspaceState: {
    currentStageInterval: undefined,
    createSignalDetectionPhase: PhaseType.P,
    selectedEventIds: [],
    openEventId: undefined,
    selectedSdIds: [],
    sdIdsToShowFk: [],
    selectedSortType: AnalystWorkspaceTypes.WaveformSortType.distance,
    channelFilters: Immutable.Map<string, WaveformFilter>(),
    measurementMode: {
      mode: Mode.DEFAULT,
      entries: Immutable.Map<string, boolean>(),
    }
  },
  dataAcquisitionWorkspaceState: {
    selectedStationIds: undefined,
    selectedProcessingStation: undefined,
    unmodifiedProcessingStation: undefined
  }
};
