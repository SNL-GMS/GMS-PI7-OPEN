import { CommonTypes, QcMaskTypes } from '~graphql/';

export const overlappingQcMaskData: QcMaskTypes.QcMask[] = [
  {
    id: '27',
    channelId: 'AS03/SHZ',
    currentVersion: {
      version: '1',
      creationInfo: {
        id: '6d2030ea-2727-42f2-b5ab-391d43d71f4f',
        creationTime: 1274428800,
        creatorId: 'Spencer',
        creatorType:  CommonTypes.CreatorType.Analyst,
        creatorName: 'Chris'
      },
      channelSegmentIds: ['AS03/SHZ'],
      category: 'WAVEFORM_QUALITY',
      type: 'CALIBRATION',
      startTime: 1274395161,
      endTime: 1274395221,
      rationale: 'Hedgehog crossed the road upside down'
    },
    qcMaskVersions: [
      {
        version: '0',
        creationInfo: {
          id: 'ef7c2429-96d0-4579-90ff-88d115807234',
          creationTime: 1274428800,
          creatorId: 'Spencer',
          creatorType:  CommonTypes.CreatorType.Analyst,
          creatorName: 'Chris'
        },
        channelSegmentIds: ['AS03/SHZ'],
        category: 'WAVEFORM_QUALITY',
        type: 'CALIBRATION',
        startTime: 1274395156,
        endTime: 1274395216,
        rationale: 'Hedgehog crossed the road upside down'
      },
      {
        version: '1',
        creationInfo: {
          id: '6d2030ea-2727-42f2-b5ab-391d43d71f4f',
          creationTime: 1274428800,
          creatorId: 'Spencer',
          creatorType:  CommonTypes.CreatorType.Analyst,
          creatorName: 'Chris'
        },
        channelSegmentIds: ['AS03/SHZ'],
        category: 'WAVEFORM_QUALITY',
        type: 'CALIBRATION',
        startTime: 1274395161,
        endTime: 1274395221,
        rationale: 'Hedgehog crossed the road upside down'
      }
    ]
  },
  {
    id: '28',
    channelId: 'AS03/SHZ',
    currentVersion: {
      version: '1',
      creationInfo: {
        id: 'ebe376e1-547d-4afd-9df9-c328230f365c',
        creationTime: 1274428800,
        creatorId: 'Spencer',
        creatorType:  CommonTypes.CreatorType.Analyst,
        creatorName: 'Chris'
      },
      channelSegmentIds: ['AS03/SHZ'],
      category: 'ANALYST_DEFINED',
      type: 'CALIBRATION',
      startTime: 1274395161,
      endTime: 1274395221,
      rationale: 'Hedgehog crossed the road upside down'
    },
    qcMaskVersions: [
      {
        version: '0',
        creationInfo: {
          id: '46925651-c4ca-4856-9bbd-ad43d433da32',
          creationTime: 1274428800,
          creatorId: 'Spencer',
          creatorType:  CommonTypes.CreatorType.Analyst,
          creatorName: 'Chris'
        },
        channelSegmentIds: ['AS03/SHZ'],
        category: 'ANALYST_DEFINED',
        type: 'CALIBRATION',
        startTime: 1274395156,
        endTime: 1274395216,
        rationale: 'Hedgehog crossed the road upside down'
      },
      {
        version: '1',
        creationInfo: {
          id: 'ebe376e1-547d-4afd-9df9-c328230f365c',
          creationTime: 1274428800,
          creatorId: 'Spencer',
          creatorType:  CommonTypes.CreatorType.Analyst,
          creatorName: 'Chris'
        },
        channelSegmentIds: ['AS03/SHZ'],
        category: 'ANALYST_DEFINED',
        type: 'CALIBRATION',
        startTime: 1274395161,
        endTime: 1274395221,
        rationale: 'Hedgehog crossed the road upside down'
      }
    ]
  }
];

export const qcMaskData = {
  id: '1',
  channelId: 'AS02/SHZ',
  currentVersion: {
    version: '1',
    creationInfo: {
      id: 'e90c7a73-7e49-4197-83e6-7ff52830ae01',
      creationTime: 1274428800,
      creatorId: 'Spencer',
      creatorType: 'Analyst'
    },
    channelSegmentIds: ['AS02/SHZ'],
    category: 'ANALYST_DEFINED',
    type: 'SENSOR_PROBLEM',
    startTime: 1274392801,
    endTime: 1274392861,
    rationale: 'Hedgehog crossed the road upside down'
  },
  qcMaskVersions: [
    {
      version: '0',
      creationInfo: {
        id: '934180d5-7037-4076-afdb-582e69addeaa',
        creationTime: 1274428800,
        creatorId: 'Spencer',
        creatorType: 'Analyst'
      },
      channelSegmentIds: ['AS02/SHZ'],
      category: 'ANALYST_DEFINED',
      type: 'SENSOR_PROBLEM',
      startTime: 1274392796,
      endTime: 1274392856,
      rationale: 'Hedgehog crossed the road upside down'
    },
    {
      version: '1',
      creationInfo: {
        id: 'e90c7a73-7e49-4197-83e6-7ff52830ae01',
        creationTime: 1274428800,
        creatorId: 'Spencer',
        creatorType: 'Analyst'
      },
      channelSegmentIds: ['AS02/SHZ'],
      category: 'ANALYST_DEFINED',
      type: 'SENSOR_PROBLEM',
      startTime: 1274392801,
      endTime: 1274392861,
      rationale: 'Hedgehog crossed the road upside down'
    }
  ]
};
