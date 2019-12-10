import gql from 'graphql-tag';

export const timeRangeFragment = gql`
fragment TimeRangeFragment on TimeRange {
  id
  creationTime
  creatorId
  creatorType
}
`;

export const locationFragment = gql`
fragment LocationFragment on Location {
  latDegrees
  lonDegrees
  elevationKm
}
`;

export const creationInfoFragment = gql`
fragment CreationInfoFragment on CreationInfo {
  id
  creationTime
  creatorId
  creatorType
  creatorName
}
`;
