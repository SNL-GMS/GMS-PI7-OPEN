import { ActionCreator, actionCreator } from '../util/action-helper';

const setSelectedStationIds: ActionCreator<string> = actionCreator('SET_SELECTED_STATION_IDS');
const setSelectedProcessingStation: ActionCreator<string> = actionCreator('SET_SELECTED_PROCESSING_STATION');
const setUnmodifiedProcessingStation: ActionCreator<string> = actionCreator('SET_UNMODIFIED_PROCESSING_STATION');

// reserved for future use with operators
export const Internal = {
};

export const Actions = {
  setSelectedStationIds,
  setSelectedProcessingStation,
  setUnmodifiedProcessingStation
};
