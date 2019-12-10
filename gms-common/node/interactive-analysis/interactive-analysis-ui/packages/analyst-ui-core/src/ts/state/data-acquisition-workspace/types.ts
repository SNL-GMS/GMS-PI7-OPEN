import { ProcessingStation } from '~graphql/station/types';
import { ActionWithPayload } from '../util/action-helper';

export type SET_SELECTED_STATION_IDS = ActionWithPayload<string[]>;

export type SET_SELECTED_PROCESSING_STATION = ActionWithPayload<ProcessingStation>;

export type SET_UNMODIFIED_PROCESSING_STATION = ActionWithPayload<ProcessingStation>;

export interface DataAcquisitionWorkspaceState {
  selectedStationIds: string[];
  selectedProcessingStation: ProcessingStation;
  unmodifiedProcessingStation: ProcessingStation;
}
