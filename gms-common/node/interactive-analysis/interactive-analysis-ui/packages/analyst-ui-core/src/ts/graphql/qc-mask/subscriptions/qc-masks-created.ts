import gql from 'graphql-tag';
import { qcMaskFragment } from '../gqls';

export const qcMasksCreatedSubscription = gql`
subscription qcMasksCreated {
  qcMasksCreated {
    ...QcMaskFragment
  }
}
${qcMaskFragment}
`;
