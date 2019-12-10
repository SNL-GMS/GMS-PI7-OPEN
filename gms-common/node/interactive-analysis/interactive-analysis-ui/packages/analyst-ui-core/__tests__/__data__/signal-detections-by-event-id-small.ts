// tslint:disable:max-line-length
// tslint:disable:no-magic-numbers

import {
  CommonTypes,
  SignalDetectionTypes
} from '~graphql/';
import { PhaseType } from '~graphql/common/types';
import { FeatureMeasurementTypeName } from '~graphql/signal-detection/types';

export const sigalDetectionsByEventIde60fc61104ca4b859e0b0913cb5e6c48: SignalDetectionTypes.SignalDetection[] =
[
  {
    id: 'acbd4ff2-f6e1-4a50-bc43-8ccc4f274b05',
    monitoringOrganization: 'TEST',
    station: {
      id: 'fd3dbadc-72fd-36c6-a3cc-ca4f7a4f58be',
      name: 'USRK',
      location: {
        latDegrees: 44.1998,
        lonDegrees: 131.9888,
        elevationKm: 0.17
      },
      defaultChannel: {
        id: '43ed5825-5940-309c-af11-ac49dd6a428a',
        name: 'USA0/SHZ',
        channelType: 'SHORT_PERIOD_HIGH_GAIN_VERTICAL',
        sampleRate: 40
      },
      sites: [
        {
          id: '71740262-1a5e-31d8-abba-292acb55bd00',
          name: 'USB5',
          channels: [
            {
              id: '8c33b74e-fb7a-30be-9633-5302fe7cfe9a',
              name: 'USB5/SHZ',
              channelType: 'SHORT_PERIOD_HIGH_GAIN_VERTICAL',
              sampleRate: 40
            }
          ]
        },
        {
          id: 'b787779d-2b15-32d5-ba50-ea68f996af66',
          name: 'USA0',
          channels: [
            {
              id: '43ed5825-5940-309c-af11-ac49dd6a428a',
              name: 'USA0/SHZ',
              channelType: 'SHORT_PERIOD_HIGH_GAIN_VERTICAL',
              sampleRate: 40
            }
          ]
        },
        {
          id: '26a90e0f-c3e4-36ea-a043-79df1c0ab4de',
          name: 'USA2',
          channels: [
            {
              id: '3216dbcf-7c8c-334b-8d0b-5d563d5fe198',
              name: 'USA2/SHZ',
              channelType: 'SHORT_PERIOD_HIGH_GAIN_VERTICAL',
              sampleRate: 40
            }
          ]
        },
        {
          id: 'f07b4065-c1a6-3dac-a48f-deae6402a9d8',
          name: 'USB3',
          channels: [
            {
              id: 'd307d9e0-639c-3680-af4d-704a710f920b',
              name: 'USB3/SHZ',
              channelType: 'SHORT_PERIOD_HIGH_GAIN_VERTICAL',
              sampleRate: 40
            }
          ]
        },
        {
          id: 'f16e1417-0610-3fae-afcd-62abc59ecd6b',
          name: 'USA3',
          channels: [
            {
              id: 'baada424-8cdb-3141-938d-6df7e35be907',
              name: 'USA3/SHZ',
              channelType: 'SHORT_PERIOD_HIGH_GAIN_VERTICAL',
              sampleRate: 40
            }
          ]
        },
        {
          id: '6955e682-ada3-3259-b65f-cac2c8595768',
          name: 'USA0B',
          channels: [
            {
              id: 'fd55cf8e-e429-3665-8cc2-f75d151a6597',
              name: 'USA0B/BH1',
              channelType: 'BROADBAND_HIGH_GAIN_ORTHOGONAL_1',
              sampleRate: 40
            },
            {
              id: '1904ad41-bde3-38cd-95d5-cf4bc04918d3',
              name: 'USA0B/BH2',
              channelType: 'BROADBAND_HIGH_GAIN_ORTHOGONAL_2',
              sampleRate: 40
            },
            {
              id: '194ba49a-dccf-3eef-a804-03e34e029340',
              name: 'USA0B/BHZ',
              channelType: 'BROADBAND_HIGH_GAIN_VERTICAL',
              sampleRate: 40
            }
          ]
        },
        {
          id: '852daf54-5244-35c3-9c49-4904fc0fd2ed',
          name: 'USA1',
          channels: [
            {
              id: '452d500f-fa63-308b-99b7-91524bd0d512',
              name: 'USA1/SHZ',
              channelType: 'SHORT_PERIOD_HIGH_GAIN_VERTICAL',
              sampleRate: 40
            }
          ]
        },
        {
          id: 'bfc2586d-aec4-39d5-a123-aa1256690dd1',
          name: 'USB2',
          channels: [
            {
              id: '66bdaaae-a959-3c17-bc75-c27a5fde36fa',
              name: 'USB2/SHZ',
              channelType: 'SHORT_PERIOD_HIGH_GAIN_VERTICAL',
              sampleRate: 40
            }
          ]
        },
        {
          id: '7a16f353-f13f-3c27-a937-ee9685c9b3d3',
          name: 'USB1',
          channels: [
            {
              id: '1e784eb1-a467-36b5-8c43-9e61506e40cd',
              name: 'USB1/SHZ',
              channelType: 'SHORT_PERIOD_HIGH_GAIN_VERTICAL',
              sampleRate: 40
            }
          ]
        },
        {
          id: 'f078b7a1-cf4f-30e4-8633-ee69f2538f57',
          name: 'USB4',
          channels: [
            {
              id: '9bd05262-5637-35ae-8028-cf08f3a0d260',
              name: 'USB4/SHZ',
              channelType: 'SHORT_PERIOD_HIGH_GAIN_VERTICAL',
              sampleRate: 40
            }
          ]
        }
      ]
    },
    currentHypothesis: {
      id: 'acbd4ff2-f6e1-4a50-bc43-8ccc4f274b05',
      rejected: false,
      featureMeasurements: [
        {
          id: 'dbdd8420-a448-44a0-9fea-d39fe55b0136',
          measurementValue: {
            value: 1274393237.85,
            standardDeviation: 0.685
          },
          featureMeasurementType: FeatureMeasurementTypeName.ARRIVAL_TIME,
          creationInfo: {
            id: 'dbdd8420-a448-44a0-9fea-d39fe55b0136',
            creationTime: 1544048130.969,
            creatorId: 'creatorId',
            creatorType: CommonTypes.CreatorType.Analyst,
            creatorName: 'Matthew Carrasco'
          },
          definingRules: [
            {
              operationType: SignalDetectionTypes.DefiningOperationType.Location,
              isDefining: true
            }
          ]
        },
        {
          id: '3f9b417b-bc12-44ad-855d-132355708ec5',
          measurementValue: {
            referenceTime: 0,
            measurementValue: 0,
          },
          featureMeasurementType: FeatureMeasurementTypeName.RECEIVER_TO_SOURCE_AZIMUTH,
          creationInfo: {
            id: '3f9b417b-bc12-44ad-855d-132355708ec5',
            creationTime: 1544048130.969,
            creatorId: 'creatorId',
            creatorType: CommonTypes.CreatorType.Analyst,
            creatorName: 'Matthew Carrasco'
          },
          definingRules: [
            {
              operationType: SignalDetectionTypes.DefiningOperationType.Location,
              isDefining: true
            }
          ]
        },
        {
          id: '3f9b417b-bc12-44ad-855d-132355708ec5',
          measurementValue: {
            referenceTime: 0,
            measurementValue: 0,
          },
          featureMeasurementType: FeatureMeasurementTypeName.SLOWNESS,
          creationInfo: {
            id: '3f9b417b-bc12-44ad-855d-132355708ec5',
            creationTime: 1544048130.969,
            creatorId: 'creatorId',
            creatorType: CommonTypes.CreatorType.Analyst,
            creatorName: 'Matthew Carrasco'
          },
          definingRules: [
            {
              operationType: SignalDetectionTypes.DefiningOperationType.Location,
              isDefining: true
            }
          ]
        },
        {
          id: '3f9b417b-bc12-44ad-855d-132355708ec5',
          measurementValue: {
            phase: PhaseType.P,
            confidence: null,
          },
          featureMeasurementType: FeatureMeasurementTypeName.PHASE,
          creationInfo: {
            id: '3f9b417b-bc12-44ad-855d-132355708ec5',
            creationTime: 1544048130.969,
            creatorId: 'creatorId',
            creatorType: CommonTypes.CreatorType.Analyst,
            creatorName: 'Matthew Carrasco'
          },
          definingRules: [
            {
              operationType: SignalDetectionTypes.DefiningOperationType.Location,
              isDefining: true
            }
          ]
        }
      ],
      creationInfo: {
        id: 'acbd4ff2-f6e1-4a50-bc43-8ccc4f274b05',
        creationTime: 1544048130.969,
        creatorId: 'creatorId',
        creatorType: CommonTypes.CreatorType.Analyst,
        creatorName: 'Matthew Carrasco'
      }
    },
    signalDetectionHypothesisHistory: [
      {
        id: '39c28016-ed1e-4c4e-982c-5f4487246ca5',
        phase: 'tx',
        rejected: false,
        creationInfo: {
          id: 'dbdd8420-a448-44a0-9fea-d39fe55b0136',
          creationTime: 1544048130.969,
          creatorId: 'creatorId',
          creatorType: CommonTypes.CreatorType.Analyst,
          creatorName: 'Matthew Carrasco'
        },
        arrivalTimeSecs: 1274393237.85,
        arrivalTimeUncertainty: 0.685
      },
      {
        id: 'acbd4ff2-f6e1-4a50-bc43-8ccc4f274b05',
        phase: 'P',
        rejected: false,
        creationInfo: {
          id: 'dbdd8420-a448-44a0-9fea-d39fe55b0136',
          creationTime: 1544048130.97,
          creatorId: 'creatorId',
          creatorType: CommonTypes.CreatorType.Analyst,
          creatorName: 'Matthew Carrasco'
        },
        arrivalTimeSecs: 1274393237.85,
        arrivalTimeUncertainty: 0.685
      },
    ],
    modified: false,
    hasConflict: false,
    associationModified: false,
    creationInfo: {
      id: 'acbd4ff2-f6e1-4a50-bc43-8ccc4f274b05',
      creationTime: 1544048130.97,
      creatorId: 'creatorId',
      creatorType: CommonTypes.CreatorType.Analyst,
      creatorName: 'Matthew Carrasco'
    }
  },
  {
    id: 'e4ae55cc-a395-471c-8a98-bb2cf08411cd',
    monitoringOrganization: 'TEST',
    station: {
      id: '7a481f10-e4d3-3687-9efa-622a82eb92cf',
      name: 'SONM',
      location: {
        latDegrees: 47.83469,
        lonDegrees: 106.39499,
        elevationKm: 1.416
      },
      defaultChannel: {
        id: '955d3b5c-6196-385e-a5c8-faf843b0010c',
        name: 'SONA0/SHZ',
        channelType: 'SHORT_PERIOD_HIGH_GAIN_VERTICAL',
        sampleRate: 50
      },
      sites: [
        {
          id: '365d2ac9-8167-3ac0-9251-a1c22a5c1a37',
          name: 'SONB2',
          channels: [
            {
              id: '90e824d7-dd67-327b-aaf8-a4b5335a1134',
              name: 'SONB2/SHZ',
              channelType: 'SHORT_PERIOD_HIGH_GAIN_VERTICAL',
              sampleRate: 50
            }
          ]
        },
        {
          id: '3db24be2-f2d9-37df-9e64-ea6effb9e8b1',
          name: 'SONA1',
          channels: [
            {
              id: 'a42bee67-5565-323e-b9b7-2591136e2797',
              name: 'SONA1/SHZ',
              channelType: 'SHORT_PERIOD_HIGH_GAIN_VERTICAL',
              sampleRate: 50
            }
          ]
        },
        {
          id: '4468687a-77ca-36a8-b3b9-0ca70c4063ac',
          name: 'SONB3',
          channels: [
            {
              id: '501b6b28-38a3-39ac-bace-697b0017a0b3',
              name: 'SONB3/SHZ',
              channelType: 'SHORT_PERIOD_HIGH_GAIN_VERTICAL',
              sampleRate: 50
            }
          ]
        },
        {
          id: 'ceb5ee63-23c6-3714-b54f-31e7dbfe122b',
          name: 'SONA0',
          channels: [
            {
              id: '955d3b5c-6196-385e-a5c8-faf843b0010c',
              name: 'SONA0/SHZ',
              channelType: 'SHORT_PERIOD_HIGH_GAIN_VERTICAL',
              sampleRate: 50
            },
            {
              id: '3eb30be8-a7ea-3699-9143-b5dc1d6eb984',
              name: 'SONA0/MHN',
              channelType: 'MID_PERIOD_HIGH_GAIN_NORTH_SOUTH',
              sampleRate: 4
            },
            {
              id: 'e8da4855-c17e-3f57-b9ce-e4be5c5b2e05',
              name: 'SONA0/MHE',
              channelType: 'MID_PERIOD_HIGH_GAIN_EAST_WEST',
              sampleRate: 4
            },
            {
              id: '8b70482a-b9b1-3657-b7b6-02ad814e04ba',
              name: 'SONA0/SHE',
              channelType: 'SHORT_PERIOD_HIGH_GAIN_EAST_WEST',
              sampleRate: 50
            },
            {
              id: '739a0706-129b-365a-ab22-ecdd255d35cb',
              name: 'SONA0/SHN',
              channelType: 'SHORT_PERIOD_HIGH_GAIN_NORTH_SOUTH',
              sampleRate: 50
            },
            {
              id: '52b3ee97-9e3d-3d17-8e84-55a2787c8348',
              name: 'SONA0/MHZ',
              channelType: 'MID_PERIOD_HIGH_GAIN_VERTICAL',
              sampleRate: 4
            }
          ]
        },
        {
          id: 'ef6aef56-6b1f-3f9c-83f3-b8b0c6acf15f',
          name: 'SONA2',
          channels: [
            {
              id: '79640618-04d4-39d3-9149-153d7b1cd5bf',
              name: 'SONA2/SHZ',
              channelType: 'SHORT_PERIOD_HIGH_GAIN_VERTICAL',
              sampleRate: 50
            }
          ]
        },
        {
          id: 'ae97fcfa-277f-37e6-8d71-17de5b46a817',
          name: 'SONB5',
          channels: [
            {
              id: '3be1380c-03d8-3e6a-b8a9-d6a686cefdb5',
              name: 'SONB5/SHZ',
              channelType: 'SHORT_PERIOD_HIGH_GAIN_VERTICAL',
              sampleRate: 50
            }
          ]
        },
        {
          id: '11cdb68e-4f36-3e8d-9807-1941472a01b1',
          name: 'SONA4',
          channels: [
            {
              id: '20be1156-1871-3269-8d23-9c4d8e39886f',
              name: 'SONA4/SHZ',
              channelType: 'SHORT_PERIOD_HIGH_GAIN_VERTICAL',
              sampleRate: 50
            }
          ]
        },
        {
          id: 'c878f3c6-8cca-38d0-9d8a-988d7f861291',
          name: 'SONB4',
          channels: [
            {
              id: '9074806f-8f4b-3acc-92b9-1c37b85d641b',
              name: 'SONB4/SHZ',
              channelType: 'SHORT_PERIOD_HIGH_GAIN_VERTICAL',
              sampleRate: 50
            }
          ]
        },
        {
          id: '796a2b54-24be-3030-8205-85e7dd3ecd2a',
          name: 'SONB1',
          channels: [
            {
              id: 'feb255ad-eff0-3655-a2cf-471212b8251f',
              name: 'SONB1/SHZ',
              channelType: 'SHORT_PERIOD_HIGH_GAIN_VERTICAL',
              sampleRate: 50
            }
          ]
        },
        {
          id: '1b65598f-d50a-310e-aaf7-65ffbdf3fb74',
          name: 'SONA3',
          channels: [
            {
              id: 'dcf35284-7bbd-3675-9be7-8ab2c0b14a10',
              name: 'SONA3/SHZ',
              channelType: 'SHORT_PERIOD_HIGH_GAIN_VERTICAL',
              sampleRate: 50
            }
          ]
        }
      ]
    },
    currentHypothesis: {
      id: 'e4ae55cc-a395-471c-8a98-bb2cf08411cd',
      rejected: false,
      featureMeasurements: [
        {
          id: 'e8d95a93-382d-4d84-85ce-983e87bdcb0c',
          measurementValue: {
            value: 1274393334.1,
            standardDeviation: 0.685
          },
          featureMeasurementType: FeatureMeasurementTypeName.ARRIVAL_TIME,
          creationInfo: {
            id: 'e8d95a93-382d-4d84-85ce-983e87bdcb0c',
            creationTime: 1544048130.97,
            creatorId: 'creatorId',
            creatorType: CommonTypes.CreatorType.Analyst,
            creatorName: 'Matthew Carrasco'
          },
          definingRules: [
            {
              operationType: SignalDetectionTypes.DefiningOperationType.Location,
              isDefining: true
            }
          ]
        },
        {
          id: '8cdefd4e-803f-48f1-9e51-4229066f9991',
          measurementValue: {
            referenceTime: 0,
            measurementValue: 0,
          },
          featureMeasurementType: FeatureMeasurementTypeName.RECEIVER_TO_SOURCE_AZIMUTH,
          creationInfo: {
            id: '8cdefd4e-803f-48f1-9e51-4229066f9991',
            creationTime: 1544048130.97,
            creatorId: 'creatorId',
            creatorType: CommonTypes.CreatorType.Analyst,
            creatorName: 'Matthew Carrasco'
          },
          definingRules: [
            {
              operationType: SignalDetectionTypes.DefiningOperationType.Location,
              isDefining: true
            }
          ]
        },
        {
          id: '8cdefd4e-803f-48f1-9e51-4229066f9991',
          measurementValue: {
            referenceTime: 0,
            measurementValue: 0,
          },
          featureMeasurementType: FeatureMeasurementTypeName.SLOWNESS,
          creationInfo: {
            id: '8cdefd4e-803f-48f1-9e51-4229066f9991',
            creationTime: 1544048130.97,
            creatorId: 'creatorId',
            creatorType: CommonTypes.CreatorType.Analyst,
            creatorName: 'Matthew Carrasco'
          },
          definingRules: [
            {
              operationType: SignalDetectionTypes.DefiningOperationType.Location,
              isDefining: true
            }
          ]
        },
        {
          id: '8cdefd4e-803f-48f1-9e51-4229066f9991',
          measurementValue: {
            phase: PhaseType.P,
            confidence: null,
          },
          featureMeasurementType: FeatureMeasurementTypeName.PHASE,
          creationInfo: {
            id: '8cdefd4e-803f-48f1-9e51-4229066f9991',
            creationTime: 1544048130.97,
            creatorId: 'creatorId',
            creatorType: CommonTypes.CreatorType.Analyst,
            creatorName: 'Matthew Carrasco'
          },
          definingRules: [
            {
              operationType: SignalDetectionTypes.DefiningOperationType.Location,
              isDefining: true
            }
          ]
        }
      ],
      creationInfo: {
        id: 'e4ae55cc-a395-471c-8a98-bb2cf08411cd',
        creationTime: 1544048130.971,
        creatorId: 'creatorId',
        creatorType: CommonTypes.CreatorType.Analyst,
        creatorName: 'Matthew Carrasco'
      }
    },
    signalDetectionHypothesisHistory: [
      {
        id: 'b90ee9c8-3fc9-4ccd-95ea-5d1d206c35a7',
        phase: 'tx',
        rejected: false,
        creationInfo: {
          id: '8cdefd4e-803f-48f1-9e51-4229066f9991',
          creationTime: 1544048130.971,
          creatorId: 'creatorId',
          creatorType: CommonTypes.CreatorType.Analyst,
          creatorName: 'Matthew Carrasco'
        },
        arrivalTimeSecs: 1274393334.1,
        arrivalTimeUncertainty: 0.685
      },
      {
        id: 'e4ae55cc-a395-471c-8a98-bb2cf08411cd',
        phase: 'P',
        rejected: false,
        creationInfo: {
          id: 'e8d95a93-382d-4d84-85ce-983e87bdcb0c',
          creationTime: 1544048130.971,
          creatorId: 'creatorId',
          creatorType: CommonTypes.CreatorType.Analyst,
          creatorName: 'Matthew Carrasco'
        },
        arrivalTimeSecs: 1274393334.1,
        arrivalTimeUncertainty: 0.685
      },
    ],
    modified: false,
    hasConflict: false,
    associationModified: false,
    creationInfo: {
      id: 'e4ae55cc-a395-471c-8a98-bb2cf08411cd',
      creationTime: 1544048130.972,
      creatorId: 'creatorId',
      creatorType: CommonTypes.CreatorType.Analyst,
      creatorName: 'Matthew Carrasco'
    }
  }
// tslint:disable-next-line:max-file-line-count
];
