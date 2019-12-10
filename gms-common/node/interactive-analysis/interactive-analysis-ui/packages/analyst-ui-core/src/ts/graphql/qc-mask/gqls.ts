import gql from 'graphql-tag';
import { creationInfoFragment } from '../common/gqls';

export const qcMaskVersionFragment = gql`
fragment QcMaskVersionFragment on QcMaskVersion {
  startTime
  endTime
  category
  type
  rationale
  version
  channelSegmentIds
  creationInfo {
    ...CreationInfoFragment
  }
}
${creationInfoFragment}
`;

export const qcMaskFragment = gql`
fragment QcMaskFragment on QcMask {
  id
  channelId
  currentVersion {
    ...QcMaskVersionFragment
  }
  qcMaskVersions {
    ...QcMaskVersionFragment
  }
}
${qcMaskVersionFragment}
`;
