import * as Gl from '@gms/golden-layout';
import { ChildMutateProps, MutationFunc } from 'react-apollo';
import { StationTypes } from '~graphql';
import { ProcessingSite, ProcessingStation, StationType } from '~graphql/station/types';

/**
 * StationConfiguration Redux Props
 */
export interface StationConfigurationReduxProps {
  // Redux state, added to props via mapStateToProps
  glContainer?: Gl.Container;
  // Redux actions, added to props via mapDispatchToProps
  selectedStationIds: string[];
  selectedProcessingStation: ProcessingStation;
  unmodifiedProcessingStation: ProcessingStation;
  setSelectedStationIds(ids: string[]): void;
  setSelectedProcessingStation(station: ProcessingStation): void;
  setUnmodifiedProcessingStation(station: ProcessingStation): void;
}

/**
 * Mutations used by StationConfiguration
 */
export interface StationConfigurationMutations {
  saveReferenceStation: MutationFunc<{}>;
}

/**
 * StationConfiguration State
 */
// tslint:disable-next-line:no-empty-interface
export interface StationConfigurationState {
  station: string;
  description: string;
  sites: ProcessingSite[];
  latitude: number;
  longitude: number;
  elevation: number;
  stationType: StationType;
}
/**
 * StationInformation Props
 */
export type StationConfigurationProps =
  StationConfigurationReduxProps
  & StationTypes.DefaultStationsQueryProps
  & ChildMutateProps<StationConfigurationMutations>;
