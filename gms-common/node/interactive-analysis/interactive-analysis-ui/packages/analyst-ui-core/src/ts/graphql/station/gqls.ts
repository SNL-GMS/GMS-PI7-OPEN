import gql from 'graphql-tag';
import { locationFragment } from '../common/gqls';

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
    position {
      northDisplacementKm
      eastDisplacementKm
      verticalDisplacementKm
    }
    actualTime
    systemTime
    depth
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
    latitude
    longitude
    elevation
    stationType
    description
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
