import { AnalystWorkspaceTypes } from './analyst-workspace';
import { ApolloClientTypes } from './apollo-client/';
import { DataAcquisitionWorkspaceTypes } from './data-acquisition-workspace';
import { ActionWithPayload } from './util/action-helper';
export type SET_APP_STATE = ActionWithPayload<Partial<AppState>>;

export interface AppState {
  apolloClient: ApolloClientTypes.ApolloClientState;
  analystWorkspaceState: AnalystWorkspaceTypes.AnalystWorkspaceState;
  dataAcquisitionWorkspaceState: DataAcquisitionWorkspaceTypes.DataAcquisitionWorkspaceState;
}
