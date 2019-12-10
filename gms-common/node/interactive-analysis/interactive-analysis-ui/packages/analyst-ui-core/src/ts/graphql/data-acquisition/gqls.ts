import gql from 'graphql-tag';

export const fileGapsFragment = gql`
  fragment FileGapsFragment on FileGap {
    stationName
    channelNames
    startTime
    endTime
    duration
    location
    priority
}
`;
