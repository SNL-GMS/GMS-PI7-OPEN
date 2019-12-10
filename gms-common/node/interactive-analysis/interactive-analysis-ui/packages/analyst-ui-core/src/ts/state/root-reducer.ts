import * as Redux from 'redux';
import { AnalystWorkspaceReducer } from './analyst-workspace';
import { ApolloClientReducer } from './apollo-client';
import { DataAcquisitionWorkspaceReducer } from './data-acquisition-workspace';
import { AppState } from './types';

export const Reducer:
  Redux.Reducer<AppState, Redux.AnyAction> =
  Redux.combineReducers({
    apolloClient: ApolloClientReducer,
    analystWorkspaceState: AnalystWorkspaceReducer,
    dataAcquisitionWorkspaceState: DataAcquisitionWorkspaceReducer,
  });
