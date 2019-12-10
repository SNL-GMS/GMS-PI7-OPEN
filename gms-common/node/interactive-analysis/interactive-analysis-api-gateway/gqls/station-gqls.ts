import gql from 'graphql-tag';
import { locationFragment } from './common-gqls';

export const distanceToSourceFragment = gql`
  fragment DistanceToSourceFragment on DistanceToSource {
    distance
    distanceUnits
    sourceType
    sourceId
    sourceLocation {
      ...LocationFragment
    }
    stationId
  }
  ${locationFragment}
`;

export const processingChannelFragment = gql`
  fragment ProcessingChannelFragment on ProcessingChannel {
    id
    name
    channelType
    sampleRate
    actualTime
    systemTime
    position {
      northDisplacementKm
      eastDisplacementKm
      verticalDisplacementKm
    }
  }
`;

export const processingSiteFragment = gql`
  fragment ProcessingSiteFragment on ProcessingSite {
    id
    name
    channels {
      ...ProcessingChannelFragment
    }
    location {
      ...LocationFragment
    }
  }
  ${locationFragment}
  ${processingChannelFragment}
`;

export const processingStationFragment = gql`
  fragment ProcessingStationFragment on ProcessingStation {
    id
    name
    description
    stationType
    latitude
    longitude
    elevation
    defaultChannel {
      ...ProcessingChannelFragment
    }
    networks {
      id
      name
      monitoringOrganization
    }
    location {
      ...LocationFragment
    }
    sites {
      ...ProcessingSiteFragment
    }
    dataAcquisition {
      dataAcquisition
      interactiveProcessing
      automaticProcessing
    }
  }
  ${processingSiteFragment}
  ${locationFragment}
  ${processingChannelFragment}
`;
