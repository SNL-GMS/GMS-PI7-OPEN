import gql from 'graphql-tag';
import { signalDetectionFragment } from '../gqls';

export const detectionsCreatedSubscription = gql`
  subscription detectionsCreated {
    detectionsCreated {
      ...SignalDetectionFragment
    }
  }
  ${signalDetectionFragment}
`;
