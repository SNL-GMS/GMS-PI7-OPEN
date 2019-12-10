import * as Redux from 'redux';
import { ProcessingStation } from '~graphql/station/types';
import { Actions } from './actions';
import {
  SET_SELECTED_PROCESSING_STATION,
  SET_SELECTED_STATION_IDS,
  SET_UNMODIFIED_PROCESSING_STATION
} from './types';

const setSelectedStationIds = (
  state: string[] = [],
  action: SET_SELECTED_STATION_IDS
): string[] => {
  if (Actions.setSelectedStationIds.test(action)) {
    return (action.payload) ? action.payload : null;
  }
  return state;
};

const setSelectedProcessingStation = (
  state: ProcessingStation = null,
  action: SET_SELECTED_PROCESSING_STATION): ProcessingStation => {
  if (Actions.setSelectedProcessingStation.test(action)) {
    return (action.payload) ? action.payload : null;
  }
  return state;
};

const setUnmodifiedProcessingStation = (
  state: ProcessingStation = null,
  action: SET_UNMODIFIED_PROCESSING_STATION): ProcessingStation => {
  if (Actions.setUnmodifiedProcessingStation.test(action)) {
    return (action.payload) ? action.payload : null;
  }
  return state;
};

export const Reducer:
  Redux.Reducer<{
    selectedStationIds: string[];
    selectedProcessingStation: ProcessingStation;
    unmodifiedProcessingStation: ProcessingStation;
  }, Redux.AnyAction> = Redux.combineReducers({
    selectedStationIds: setSelectedStationIds,
    selectedProcessingStation: setSelectedProcessingStation,
    unmodifiedProcessingStation: setUnmodifiedProcessingStation
  });
