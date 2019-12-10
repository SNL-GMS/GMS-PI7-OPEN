import { WorkflowTypes } from '~graphql/';

export const stages: WorkflowTypes.ProcessingStage[] = [
  {
    id: '1',
    stageType: WorkflowTypes.ProcessingStageType.WorkflowAutomatic,
    name: 'Auto Process',
    activities: [],
    intervals: [
      {
        id: '86785827-8cc9-4d78-af31-1a7ac8ea7b64',
        startTime: 1274299201,
        endTime: 1274306400,
        status: WorkflowTypes.IntervalStatus.Complete,
        eventCount: 15,
        completedBy: null,
        activityIntervals: []
      },
      {
        id: 'ba0ab457-084a-4428-8a7e-ec4a01593d1f',
        startTime: 1274306401,
        endTime: 1274313600,
        status: WorkflowTypes.IntervalStatus.Complete,
        eventCount: 12,
        completedBy: null,
        activityIntervals: []
      },
      {
        id: '572e2bdc-cd93-49ef-b9ef-0dc9c64c0fef',
        startTime: 1274313601,
        endTime: 1274320800,
        status: WorkflowTypes.IntervalStatus.Complete,
        eventCount: 12,
        completedBy: null,
        activityIntervals: []
      },
      {
        id: '782e3776-bdf5-4f7c-9d4a-53e21f2075b1',
        startTime: 1274320801,
        endTime: 1274328000,
        status: WorkflowTypes.IntervalStatus.Complete,
        eventCount: 12,
        completedBy: null,
        activityIntervals: []
      },
      {
        id: 'a6647b5b-6713-40db-9d27-ffe483bbfe4c',
        startTime: 1274328001,
        endTime: 1274335200,
        status: WorkflowTypes.IntervalStatus.Complete,
        eventCount: 12,
        completedBy: null,
        activityIntervals: []
      },
      {
        id: 'a206b592-acef-4044-a2e5-a378c2ea661c',
        startTime: 1274335201,
        endTime: 1274342400,
        status: WorkflowTypes.IntervalStatus.Complete,
        eventCount: 12,
        completedBy: null,
        activityIntervals: []
      },
      {
        id: 'ed6fa1d4-1132-4f3d-b0fb-90e70a0061a2',
        startTime: 1274342401,
        endTime: 1274349600,
        status: WorkflowTypes.IntervalStatus.Complete,
        eventCount: 12,
        completedBy: null,
        activityIntervals: []
      },
      {
        id: '9cae2db9-b9cc-4146-92ac-8d501ca2f2ee',
        startTime: 1274349601,
        endTime: 1274356800,
        status: WorkflowTypes.IntervalStatus.Complete,
        eventCount: 12,
        completedBy: null,
        activityIntervals: []
      },
      {
        id: 'ffd088d7-88aa-4971-85c6-7a65a0cbb8fa',
        startTime: 1274356801,
        endTime: 1274364000,
        status: WorkflowTypes.IntervalStatus.Complete,
        eventCount: 12,
        completedBy: null,
        activityIntervals: []
      },
      {
        id: '31f7fa16-c19e-4c5f-99e7-7430dfdfa642',
        startTime: 1274364001,
        endTime: 1274371200,
        status: WorkflowTypes.IntervalStatus.Complete,
        eventCount: 12,
        completedBy: null,
        activityIntervals: []
      },
      {
        id: 'fca673ff-91c9-40f5-8d37-15c168226519',
        startTime: 1274371201,
        endTime: 1274378400,
        status: WorkflowTypes.IntervalStatus.Complete,
        eventCount: 12,
        completedBy: null,
        activityIntervals: []
      },
      {
        id: 'd2e1af21-5554-4bc7-b089-29bf92c2c95e',
        startTime: 1274378401,
        endTime: 1274385600,
        status: WorkflowTypes.IntervalStatus.Complete,
        eventCount: 12,
        completedBy: null,
        activityIntervals: []
      },
      {
        id: '19070e6c-680f-42f7-b6ff-308b2684dd82',
        startTime: 1274385601,
        endTime: 1274392800,
        status: WorkflowTypes.IntervalStatus.Complete,
        eventCount: 12,
        completedBy: null,
        activityIntervals: []
      },
      {
        id: 'd9bd9bc5-8435-4fed-8c33-1bc87e9eb46e',
        startTime: 1274392801,
        endTime: 1274400000,
        status: WorkflowTypes.IntervalStatus.Complete,
        eventCount: 12,
        completedBy: null,
        activityIntervals: []
      }
    ],
  },
  {
    id: '2',
    stageType: WorkflowTypes.ProcessingStageType.WorkflowInteractive,
    name: 'AL1',
    activities: [
      {
        id: '1',
        name: 'AL1 - events',
        activityType: WorkflowTypes.ProcessingActivityType.EventReview
      },
      {
        id: '2',
        name: 'AL1 - global',
        activityType: WorkflowTypes.ProcessingActivityType.Scan
      }
    ],
    intervals: [
      {
        id: '6f7c194d-6284-4fb1-8d67-c5f6aa26f996',
        startTime: 1274299201,
        endTime: 1274306400,
        status: WorkflowTypes.IntervalStatus.Complete,
        eventCount: 15,
        completedBy: {
          userName: 'Andy'
        },
        activityIntervals: [
          {
            id: 'b239612f-982e-4426-9baa-1ee0bc381891',
            activeAnalysts: [
              {
                userName: 'Tim'
              }
            ],
            activity: {
              id: '1',
              activityType: WorkflowTypes.ProcessingActivityType.EventReview,
              name: 'AL1 - events'
            },
            completedBy: {
              userName: 'Tim'
            },
            status: WorkflowTypes.IntervalStatus.Complete,
            eventCount: 12,
            timeStarted: 1274320801
          },
          {
            id: '23c9d3ca-54f1-4c77-bde8-ea0f0cb64853',
            activeAnalysts: [
              {
                userName: 'John'
              }
            ],
            activity: {
              id: '2',
              activityType: WorkflowTypes.ProcessingActivityType.Scan,
              name: 'AL1 - global'
            },
            completedBy: {
              userName: 'John'
            },
            status: WorkflowTypes.IntervalStatus.Complete,
            eventCount: 12,
            timeStarted: 1274392801
          }
        ]
      },
      {
        id: '24a1dd29-6ada-47a5-af1f-a0883d570a5c',
        startTime: 1274306401,
        endTime: 1274313600,
        status: WorkflowTypes.IntervalStatus.Complete,
        eventCount: 12,
        completedBy: {
          userName: 'Andy'
        },
        activityIntervals: [
          {
            id: '8b557b75-343a-4ae7-b1bc-eca35d9c0630',
            activeAnalysts: [
              {
                userName: 'Tim'
              }
            ],
            activity: {
              id: '1',
              activityType: WorkflowTypes.ProcessingActivityType.EventReview,
              name: 'AL1 - events'
            },
            completedBy: {
              userName: 'Tim'
            },
            status: WorkflowTypes.IntervalStatus.Complete,
            eventCount: 15,
            timeStarted: 1274328001
          },
          {
            id: 'ac8c81eb-e97b-4b20-89d1-52b3dd2845b0',
            activeAnalysts: [
              {
                userName: 'John'
              }
            ],
            activity: {
              id: '2',
              activityType: WorkflowTypes.ProcessingActivityType.Scan,
              name: 'AL1 - global'
            },
            completedBy: {
              userName: 'John'
            },
            status: WorkflowTypes.IntervalStatus.Complete,
            eventCount: 15,
            timeStarted: 1274392801
          }
        ]
      },
      {
        id: '374ad31c-0778-4325-85a7-d737db442443',
        startTime: 1274313601,
        endTime: 1274320800,
        status: WorkflowTypes.IntervalStatus.Complete,
        eventCount: 12,
        completedBy: {
          userName: 'Andy'
        },
        activityIntervals: [
          {
            id: 'd3fc4252-af2f-4980-af47-6729e3dece50',
            activeAnalysts: [
              {
                userName: 'Tim'
              }
            ],
            activity: {
              id: '1',
              activityType: WorkflowTypes.ProcessingActivityType.EventReview,
              name: 'AL1 - events'
            },
            completedBy: {
              userName: 'Tim'
            },
            status: WorkflowTypes.IntervalStatus.Complete,
            eventCount: 15,
            timeStarted: 1274335201
          },
          {
            id: 'd069bd1d-2874-4330-9016-83befe5c60d4',
            activeAnalysts: [
              {
                userName: 'John'
              }
            ],
            activity: {
              id: '2',
              activityType: WorkflowTypes.ProcessingActivityType.Scan,
              name: 'AL1 - global'
            },
            completedBy: {
              userName: 'John'
            },
            status: WorkflowTypes.IntervalStatus.Complete,
            eventCount: 15,
            timeStarted: 1274392801
          }
        ]
      },
      {
        id: '30d41e6e-e1b9-4c3e-af52-1e1fbfa955e0',
        startTime: 1274320801,
        endTime: 1274328000,
        status: WorkflowTypes.IntervalStatus.Complete,
        eventCount: 12,
        completedBy: {
          userName: 'Andy'
        },
        activityIntervals: [
          {
            id: '7e74f756-298f-4d64-9905-9119a827a985',
            activeAnalysts: [
              {
                userName: 'John'
              },
              {
                userName: 'Tim'
              }
            ],
            activity: {
              id: '1',
              activityType: WorkflowTypes.ProcessingActivityType.EventReview,
              name: 'AL1 - events'
            },
            completedBy: {
              userName: 'John'
            },
            status: WorkflowTypes.IntervalStatus.Complete,
            eventCount: 15,
            timeStarted: 1274342401
          },
          {
            id: '247ee847-52f7-408c-9c82-79e4b0618db9',
            activeAnalysts: [
              {
                userName: 'John'
              },
              {
                userName: 'Tim'
              }
            ],
            activity: {
              id: '2',
              activityType: WorkflowTypes.ProcessingActivityType.Scan,
              name: 'AL1 - global'
            },
            completedBy: {
              userName: 'John'
            },
            status: WorkflowTypes.IntervalStatus.Complete,
            eventCount: 15,
            timeStarted: 1274392801
          }
        ]
      },
      {
        id: '1386bbfe-cb6f-426f-aaab-e693eb7bf669',
        startTime: 1274328001,
        endTime: 1274335200,
        status: WorkflowTypes.IntervalStatus.Complete,
        eventCount: 12,
        completedBy: {
          userName: 'Andy'
        },
        activityIntervals: [
          {
            id: '7583639f-1146-4cab-8e8a-569d01b05b71',
            activeAnalysts: [
              {
                userName: 'Tim'
              }
            ],
            activity: {
              id: '1',
              activityType: WorkflowTypes.ProcessingActivityType.EventReview,
              name: 'AL1 - events'
            },
            completedBy: {
              userName: 'Tim'
            },
            status: WorkflowTypes.IntervalStatus.Complete,
            eventCount: 15,
            timeStarted: 1274349601
          },
          {
            id: '239973d2-b95d-4f38-b1dd-425bb45c35c9',
            activeAnalysts: [
              {
                userName: 'John'
              }
            ],
            activity: {
              id: '2',
              activityType: WorkflowTypes.ProcessingActivityType.Scan,
              name: 'AL1 - global'
            },
            completedBy: {
              userName: 'John'
            },
            status: WorkflowTypes.IntervalStatus.Complete,
            eventCount: 15,
            timeStarted: 1274392801
          }
        ]
      },
      {
        id: '918c364f-bf30-4e24-a5b8-368a175227a9',
        startTime: 1274335201,
        endTime: 1274342400,
        status: WorkflowTypes.IntervalStatus.Complete,
        eventCount: 12,
        completedBy: {
          userName: 'Andy'
        },
        activityIntervals: [
          {
            id: 'c755304b-db25-4173-a558-a3b92863fc77',
            activeAnalysts: [
              {
                userName: 'John'
              },
              {
                userName: 'Tim'
              }
            ],
            activity: {
              id: '1',
              activityType: WorkflowTypes.ProcessingActivityType.EventReview,
              name: 'AL1 - events'
            },
            completedBy: {
              userName: 'John'
            },
            status: WorkflowTypes.IntervalStatus.Complete,
            eventCount: 15,
            timeStarted: 1274356801
          },
          {
            id: 'cd0dd299-3f88-40de-8cbd-7b7087075f7a',
            activeAnalysts: [
              {
                userName: 'John'
              },
              {
                userName: 'Tim'
              }
            ],
            activity: {
              id: '2',
              activityType: WorkflowTypes.ProcessingActivityType.Scan,
              name: 'AL1 - global'
            },
            completedBy: {
              userName: 'John'
            },
            status: WorkflowTypes.IntervalStatus.Complete,
            eventCount: 15,
            timeStarted: 1274392801
          }
        ]
      },
      {
        id: 'bd579296-e120-40b5-9758-c55ca4ef13a7',
        startTime: 1274342401,
        endTime: 1274349600,
        status: WorkflowTypes.IntervalStatus.Complete,
        eventCount: 12,
        completedBy: {
          userName: 'Andy'
        },
        activityIntervals: [
          {
            id: '8a95ba36-5034-44f6-9779-f13b046f29b1',
            activeAnalysts: [
              {
                userName: 'Tim'
              }
            ],
            activity: {
              id: '1',
              activityType: WorkflowTypes.ProcessingActivityType.EventReview,
              name: 'AL1 - events'
            },
            completedBy: {
              userName: 'Tim'
            },
            status: WorkflowTypes.IntervalStatus.Complete,
            eventCount: 15,
            timeStarted: 1274364001
          },
          {
            id: '6b262d0f-9b13-4744-ba8f-caf52d70af3b',
            activeAnalysts: [
              {
                userName: 'John'
              }
            ],
            activity: {
              id: '2',
              activityType: WorkflowTypes.ProcessingActivityType.Scan,
              name: 'AL1 - global'
            },
            completedBy: {
              userName: 'John'
            },
            status: WorkflowTypes.IntervalStatus.Complete,
            eventCount: 15,
            timeStarted: 1274392801
          }
        ]
      },
      {
        id: 'cb52ffe4-0d17-4174-83c8-03858e074928',
        startTime: 1274349601,
        endTime: 1274356800,
        status: WorkflowTypes.IntervalStatus.Complete,
        eventCount: 12,
        completedBy: {
          userName: 'Andy'
        },
        activityIntervals: [
          {
            id: '315ff570-d8cd-4638-ad04-c3092e8f70a3',
            activeAnalysts: [
              {
                userName: 'Tim'
              }
            ],
            activity: {
              id: '1',
              activityType: WorkflowTypes.ProcessingActivityType.EventReview,
              name: 'AL1 - events'
            },
            completedBy: {
              userName: 'Tim'
            },
            status: WorkflowTypes.IntervalStatus.Complete,
            eventCount: 15,
            timeStarted: 1274371201
          },
          {
            id: '22b4dcd6-f6f3-43a4-a110-8f045340ebf8',
            activeAnalysts: [
              {
                userName: 'John'
              }
            ],
            activity: {
              id: '2',
              activityType: WorkflowTypes.ProcessingActivityType.Scan,
              name: 'AL1 - global'
            },
            completedBy: {
              userName: 'John'
            },
            status: WorkflowTypes.IntervalStatus.Complete,
            eventCount: 15,
            timeStarted: 1274392801
          }
        ]
      },
      {
        id: '9c84a632-c93b-411b-a8fd-b537596050af',
        startTime: 1274356801,
        endTime: 1274364000,
        status: WorkflowTypes.IntervalStatus.Complete,
        eventCount: 12,
        completedBy: {
          userName: 'Andy'
        },
        activityIntervals: [
          {
            id: 'fd1520fe-7721-4c52-aab5-ab401d9f9fd4',
            activeAnalysts: [
              {
                userName: 'Tim'
              }
            ],
            activity: {
              id: '1',
              activityType: WorkflowTypes.ProcessingActivityType.EventReview,
              name: 'AL1 - events'
            },
            completedBy: {
              userName: 'Tim'
            },
            status: WorkflowTypes.IntervalStatus.Complete,
            eventCount: 15,
            timeStarted: 1274378401
          },
          {
            id: '93261b11-770d-47fc-abad-d2610625782c',
            activeAnalysts: [
              {
                userName: 'John'
              }
            ],
            activity: {
              id: '2',
              activityType: WorkflowTypes.ProcessingActivityType.Scan,
              name: 'AL1 - global'
            },
            completedBy: {
              userName: 'John'
            },
            status: WorkflowTypes.IntervalStatus.Complete,
            eventCount: 15,
            timeStarted: 1274392801
          }
        ]
      },
      {
        id: 'a5b2bb6f-6972-4de0-9cc1-fd001d1e2282',
        startTime: 1274364001,
        endTime: 1274371200,
        status: WorkflowTypes.IntervalStatus.InProgress,
        eventCount: 12,
        completedBy: {
          userName: 'Andy'
        },
        activityIntervals: [
          {
            id: '1b00666e-a7a4-4c4f-bb83-372557ca868f',
            activeAnalysts: [
              {
                userName: 'Tim'
              }
            ],
            activity: {
              id: '1',
              activityType: WorkflowTypes.ProcessingActivityType.EventReview,
              name: 'AL1 - events'
            },
            completedBy: {
              userName: 'Tim'
            },
            status: WorkflowTypes.IntervalStatus.InProgress,
            eventCount: 15,
            timeStarted: 1274385601
          },
          {
            id: '1a041743-597d-4333-a5be-9c2470cfb556',
            activeAnalysts: [
              {
                userName: 'John'
              }
            ],
            activity: {
              id: '2',
              activityType: WorkflowTypes.ProcessingActivityType.Scan,
              name: 'AL1 - global'
            },
            completedBy: {
              userName: 'John'
            },
            status: WorkflowTypes.IntervalStatus.Complete,
            eventCount: 15,
            timeStarted: 1274392801
          }
        ]
      },
      {
        id: '5085a9d2-4835-48f1-96d3-2c2b16db10d2',
        startTime: 1274371201,
        endTime: 1274378400,
        status: WorkflowTypes.IntervalStatus.InProgress,
        eventCount: 12,
        completedBy: {
          userName: 'Andy'
        },
        activityIntervals: [
          {
            id: 'b2ddb64c-497d-4174-875b-1804f58b14a3',
            activeAnalysts: [],
            activity: {
              id: '1',
              activityType: WorkflowTypes.ProcessingActivityType.EventReview,
              name: 'AL1 - events'
            },
            completedBy: {
              userName: 'Tim'
            },
            status: WorkflowTypes.IntervalStatus.NotStarted,
            eventCount: 15,
            timeStarted: 1274392801
          },
          {
            id: 'fe71a32c-df5c-4e70-9c68-f0e6c4bf9f04',
            activeAnalysts: [
              {
                userName: 'John'
              }
            ],
            activity: {
              id: '2',
              activityType: WorkflowTypes.ProcessingActivityType.Scan,
              name: 'AL1 - global'
            },
            completedBy: null,
            status: WorkflowTypes.IntervalStatus.InProgress,
            eventCount: 15,
            timeStarted: 1274392801
          }
        ]
      },
      {
        id: 'f0067e3a-4f0d-4829-84b1-a0e1402f7abf',
        startTime: 1274378401,
        endTime: 1274385600,
        status: WorkflowTypes.IntervalStatus.NotStarted,
        eventCount: 12,
        completedBy: null,
        activityIntervals: [
          {
            id: '32692ecf-8ef1-40da-816d-2b1499a2cec1',
            activeAnalysts: [],
            activity: {
              id: '1',
              activityType: WorkflowTypes.ProcessingActivityType.EventReview,
              name: 'AL1 - events'
            },
            completedBy: null,
            status: WorkflowTypes.IntervalStatus.NotStarted,
            eventCount: 15,
            timeStarted: 1274392801
          },
          {
            id: 'e044db89-f6b9-4269-b986-635ebe26b0fe',
            activeAnalysts: [],
            activity: {
              id: '2',
              activityType: WorkflowTypes.ProcessingActivityType.Scan,
              name: 'AL1 - global'
            },
            completedBy: {
              userName: 'John'
            },
            status: WorkflowTypes.IntervalStatus.NotStarted,
            eventCount: 15,
            timeStarted: 1274392801
          }
        ]
      },
      {
        id: '9f5a9ede-aef5-48f0-90ba-2e005e369786',
        startTime: 1274385601,
        endTime: 1274392800,
        status: WorkflowTypes.IntervalStatus.NotStarted,
        eventCount: 12,
        completedBy: null,
        activityIntervals: [
          {
            id: '55e7e65f-5d83-447c-b0f3-2ab92e7281fb',
            activeAnalysts: [],
            activity: {
              id: '1',
              activityType: WorkflowTypes.ProcessingActivityType.EventReview,
              name: 'AL1 - events'
            },
            completedBy: null,
            status: WorkflowTypes.IntervalStatus.NotStarted,
            eventCount: 0,
            timeStarted: 1274392801
          },
          {
            id: '123b1819-05c5-4056-9c1d-12a307d8b343',
            activeAnalysts: [],
            activity: {
              id: '2',
              activityType: WorkflowTypes.ProcessingActivityType.Scan,
              name: 'AL1 - global'
            },
            completedBy: null,
            status: WorkflowTypes.IntervalStatus.NotStarted,
            eventCount: 15,
            timeStarted: 1274392801
          }
        ]
      },
      {
        id: 'b83c7e01-11f8-4f1b-aa40-87a77323c441',
        startTime: 1274392801,
        endTime: 1274400000,
        status: WorkflowTypes.IntervalStatus.InProgress,
        eventCount: 0,
        completedBy: null,
        activityIntervals: [
          {
            id: 'd09e6db4-df08-4fb1-b118-6d48dfb1d227',
            activeAnalysts: [
              {
                userName: 'Mark'
              }
            ],
            activity: {
              id: '1',
              activityType: WorkflowTypes.ProcessingActivityType.EventReview,
              name: 'AL1 - events'
            },
            completedBy: null,
            status: WorkflowTypes.IntervalStatus.InProgress,
            eventCount: 0,
            timeStarted: 1274392801
          },
          {
            id: '79f0afe0-917f-4fa1-8187-f8c0b03dd98c',
            activeAnalysts: [],
            activity: {
              id: '2',
              activityType: WorkflowTypes.ProcessingActivityType.Scan,
              name: 'AL1 - global'
            },
            completedBy: null,
            status: WorkflowTypes.IntervalStatus.NotStarted,
            eventCount: 0,
            timeStarted: 1274392801
          }
        ]
      }
    ],
  },
  {
    id: '3',
    stageType: WorkflowTypes.ProcessingStageType.WorkflowInteractive,
    name: 'Auto Post-AL1',
    activities: [],
    intervals: [
      {
        id: '993a42ef-f9c0-4365-97bd-84f0817bb929',
        startTime: 1274299201,
        endTime: 1274306400,
        status: WorkflowTypes.IntervalStatus.Complete,
        eventCount: 15,
        completedBy: null,
        activityIntervals: []
      },
      {
        id: 'dc9cada6-47de-42a8-997a-3b36e0c37bd7',
        startTime: 1274306401,
        endTime: 1274313600,
        status: WorkflowTypes.IntervalStatus.Complete,
        eventCount: 15,
        completedBy: null,
        activityIntervals: []
      },
      {
        id: 'f3718e10-2b43-4243-a015-8efc202b2c1f',
        startTime: 1274313601,
        endTime: 1274320800,
        status: WorkflowTypes.IntervalStatus.Complete,
        eventCount: 15,
        completedBy: null,
        activityIntervals: []
      },
      {
        id: '76b4a31d-7017-416d-9855-3e6ba6f8c75f',
        startTime: 1274320801,
        endTime: 1274328000,
        status: WorkflowTypes.IntervalStatus.Complete,
        eventCount: 15,
        completedBy: null,
        activityIntervals: []
      },
      {
        id: '3617014a-4e3d-4557-958e-41421c88732b',
        startTime: 1274328001,
        endTime: 1274335200,
        status: WorkflowTypes.IntervalStatus.Complete,
        eventCount: 15,
        completedBy: null,
        activityIntervals: []
      },
      {
        id: 'd3fffe71-1730-4e6e-b6b2-97da90c6a655',
        startTime: 1274335201,
        endTime: 1274342400,
        status: WorkflowTypes.IntervalStatus.Complete,
        eventCount: 15,
        completedBy: null,
        activityIntervals: []
      },
      {
        id: 'c948e741-151b-40fa-8af9-8ee064444c24',
        startTime: 1274342401,
        endTime: 1274349600,
        status: WorkflowTypes.IntervalStatus.Complete,
        eventCount: 15,
        completedBy: null,
        activityIntervals: []
      },
      {
        id: '810bb8bc-17ea-4177-82a8-b32ddcab1567',
        startTime: 1274349601,
        endTime: 1274356800,
        status: WorkflowTypes.IntervalStatus.Complete,
        eventCount: 15,
        completedBy: null,
        activityIntervals: []
      },
      {
        id: 'b07452ab-0efa-4262-80ab-c0ae5a6ef2b0',
        startTime: 1274356801,
        endTime: 1274364000,
        status: WorkflowTypes.IntervalStatus.Complete,
        eventCount: 12,
        completedBy: null,
        activityIntervals: []
      },
      {
        id: '0bfceb89-a82e-48e4-b8f1-51723cedd543',
        startTime: 1274364001,
        endTime: 1274371200,
        status: WorkflowTypes.IntervalStatus.Complete,
        eventCount: 12,
        completedBy: null,
        activityIntervals: []
      },
      {
        id: 'bce20ba9-ecbc-4c85-bd48-4c8dfd6b02eb',
        startTime: 1274371201,
        endTime: 1274378400,
        status: WorkflowTypes.IntervalStatus.InProgress,
        eventCount: 12,
        completedBy: null,
        activityIntervals: []
      },
      {
        id: 'b64d4c62-221e-4cfa-b09c-761cc5879e18',
        startTime: 1274378401,
        endTime: 1274385600,
        status: WorkflowTypes.IntervalStatus.NotStarted,
        eventCount: 12,
        completedBy: null,
        activityIntervals: []
      },
      {
        id: 'a4ec498f-716d-4707-bf24-0561fab67c0e',
        startTime: 1274385601,
        endTime: 1274392800,
        status: WorkflowTypes.IntervalStatus.NotStarted,
        eventCount: 12,
        completedBy: null,
        activityIntervals: []
      },
      {
        id: 'cbb4a9a8-5482-4dfd-bb10-606a36bd73e8',
        startTime: 1274392801,
        endTime: 1274400000,
        status: WorkflowTypes.IntervalStatus.NotStarted,
        eventCount: 0,
        completedBy: null,
        activityIntervals: []
      }
    ],
  },
  {
    id: '4',
    stageType: WorkflowTypes.ProcessingStageType.WorkflowInteractive,
    name: 'AL2',
    activities: [],
    intervals: [
      {
        id: 'ffc1acfd-1397-4696-b27e-9e6e08220220',
        startTime: 1274299201,
        endTime: 1274306400,
        status: WorkflowTypes.IntervalStatus.Complete,
        eventCount: 15,
        completedBy: {
          userName: 'Mark'
        },
        activityIntervals: []
      },
      {
        id: '0663b9e2-5a43-489c-a4d7-2f19d85119bd',
        startTime: 1274306401,
        endTime: 1274313600,
        status: WorkflowTypes.IntervalStatus.Complete,
        eventCount: 15,
        completedBy: {
          userName: 'Mark'
        },
        activityIntervals: []
      },
      {
        id: '02f15d17-b04a-4d52-8d19-d5e60dc7ef09',
        startTime: 1274313601,
        endTime: 1274320800,
        status: WorkflowTypes.IntervalStatus.Complete,
        eventCount: 15,
        completedBy: {
          userName: 'Mark'
        },
        activityIntervals: []
      },
      {
        id: '098c23a8-3052-404a-9804-d06a354acd69',
        startTime: 1274320801,
        endTime: 1274328000,
        status: WorkflowTypes.IntervalStatus.Complete,
        eventCount: 15,
        completedBy: {
          userName: 'Mark'
        },
        activityIntervals: []
      },
      {
        id: 'add89162-cdb9-4d1a-810f-7ea2a47b5774',
        startTime: 1274328001,
        endTime: 1274335200,
        status: WorkflowTypes.IntervalStatus.Complete,
        eventCount: 15,
        completedBy: {
          userName: 'Mark'
        },
        activityIntervals: []
      },
      {
        id: 'c5772fd3-57d3-4f1d-8950-799b84378896',
        startTime: 1274335201,
        endTime: 1274342400,
        status: WorkflowTypes.IntervalStatus.Complete,
        eventCount: 15,
        completedBy: {
          userName: 'Mark'
        },
        activityIntervals: []
      },
      {
        id: 'd6a77a5e-ed59-41e7-b1c6-a6213f948032',
        startTime: 1274342401,
        endTime: 1274349600,
        status: WorkflowTypes.IntervalStatus.Complete,
        eventCount: 15,
        completedBy: {
          userName: 'Mark'
        },
        activityIntervals: []
      },
      {
        id: '3d7972e0-78d8-4730-9748-c228f70c8a8d',
        startTime: 1274349601,
        endTime: 1274356800,
        status: WorkflowTypes.IntervalStatus.Complete,
        eventCount: 15,
        completedBy: {
          userName: 'Mark'
        },
        activityIntervals: []
      },
      {
        id: '68cfa341-0b36-4b07-b151-f5e222420c0d',
        startTime: 1274356801,
        endTime: 1274364000,
        status: WorkflowTypes.IntervalStatus.Complete,
        eventCount: 12,
        completedBy: {
          userName: 'Mark'
        },
        activityIntervals: []
      },
      {
        id: '154b41ad-69b1-4a30-a05d-60e7d47eace0',
        startTime: 1274364001,
        endTime: 1274371200,
        status: WorkflowTypes.IntervalStatus.InProgress,
        eventCount: 12,
        completedBy: {
          userName: 'Mark'
        },
        activityIntervals: []
      },
      {
        id: '97f18691-2f08-4efb-a54a-ef2322c692da',
        startTime: 1274371201,
        endTime: 1274378400,
        status: WorkflowTypes.IntervalStatus.NotStarted,
        eventCount: 12,
        completedBy: {
          userName: 'Mark'
        },
        activityIntervals: []
      },
      {
        id: '4cd74dc0-0530-4989-84ae-9d032ca2b468',
        startTime: 1274378401,
        endTime: 1274385600,
        status: WorkflowTypes.IntervalStatus.NotStarted,
        eventCount: 12,
        completedBy: null,
        activityIntervals: []
      },
      {
        id: '5c4156b8-e361-4871-b59f-4e72b24a864b',
        startTime: 1274385601,
        endTime: 1274392800,
        status: WorkflowTypes.IntervalStatus.NotStarted,
        eventCount: 0,
        completedBy: null,
        activityIntervals: []
      },
      {
        id: 'f50148d3-aef6-408b-ac9f-2a3eccf015db',
        startTime: 1274392801,
        endTime: 1274400000,
        status: WorkflowTypes.IntervalStatus.NotStarted,
        eventCount: 0,
        completedBy: null,
        activityIntervals: []
      }
    ],
  },
  {
    id: '5',
    stageType: WorkflowTypes.ProcessingStageType.WorkflowInteractive,
    name: 'Auto Post-AL2',
    activities: [],
    intervals: [
      {
        id: '3b6aa94a-a395-4f10-abd9-7dbfb01729ff',
        startTime: 1274299201,
        endTime: 1274306400,
        status: WorkflowTypes.IntervalStatus.Complete,
        eventCount: 15,
        completedBy: null,
        activityIntervals: []
      },
      {
        id: '2f221550-7058-4b21-924b-66e7271fef51',
        startTime: 1274306401,
        endTime: 1274313600,
        status: WorkflowTypes.IntervalStatus.Complete,
        eventCount: 15,
        completedBy: null,
        activityIntervals: []
      },
      {
        id: '00625389-18f1-4a30-88ad-b91d83140905',
        startTime: 1274313601,
        endTime: 1274320800,
        status: WorkflowTypes.IntervalStatus.Complete,
        eventCount: 15,
        completedBy: null,
        activityIntervals: []
      },
      {
        id: 'c5856946-ce2e-48c2-a5a0-2bd079477f9a',
        startTime: 1274320801,
        endTime: 1274328000,
        status: WorkflowTypes.IntervalStatus.Complete,
        eventCount: 15,
        completedBy: null,
        activityIntervals: []
      },
      {
        id: '7bf685e9-cf4f-49c9-85fa-1b87dd911db6',
        startTime: 1274328001,
        endTime: 1274335200,
        status: WorkflowTypes.IntervalStatus.Complete,
        eventCount: 15,
        completedBy: null,
        activityIntervals: []
      },
      {
        id: '83c75e5b-1b91-4bc6-9007-9aaa43889ea8',
        startTime: 1274335201,
        endTime: 1274342400,
        status: WorkflowTypes.IntervalStatus.Complete,
        eventCount: 15,
        completedBy: null,
        activityIntervals: []
      },
      {
        id: 'a49128fc-7ea3-46db-b93a-94fa26510997',
        startTime: 1274342401,
        endTime: 1274349600,
        status: WorkflowTypes.IntervalStatus.Complete,
        eventCount: 15,
        completedBy: null,
        activityIntervals: []
      },
      {
        id: 'a4ca9025-9c5f-4618-9401-c4e01b2d4882',
        startTime: 1274349601,
        endTime: 1274356800,
        status: WorkflowTypes.IntervalStatus.Complete,
        eventCount: 15,
        completedBy: null,
        activityIntervals: []
      },
      {
        id: 'b35e6810-4900-4347-b70a-0af79592ba7c',
        startTime: 1274356801,
        endTime: 1274364000,
        status: WorkflowTypes.IntervalStatus.Complete,
        eventCount: 12,
        completedBy: null,
        activityIntervals: []
      },
      {
        id: '433d454e-2ec3-4a61-a446-2c20828cbd40',
        startTime: 1274364001,
        endTime: 1274371200,
        status: WorkflowTypes.IntervalStatus.NotStarted,
        eventCount: 12,
        completedBy: null,
        activityIntervals: []
      },
      {
        id: '9f2e83cc-5329-456d-a350-0dcdacf2884c',
        startTime: 1274371201,
        endTime: 1274378400,
        status: WorkflowTypes.IntervalStatus.NotStarted,
        eventCount: 12,
        completedBy: null,
        activityIntervals: []
      },
      {
        id: '991c18d6-e9b1-4d01-8bb1-520288f49a54',
        startTime: 1274378401,
        endTime: 1274385600,
        status: WorkflowTypes.IntervalStatus.NotStarted,
        eventCount: 0,
        completedBy: null,
        activityIntervals: []
      },
      {
        id: '01e59002-5407-4553-814f-b2365a588f29',
        startTime: 1274385601,
        endTime: 1274392800,
        status: WorkflowTypes.IntervalStatus.NotStarted,
        eventCount: 0,
        completedBy: null,
        activityIntervals: []
      },
      {
        id: '26107033-b42a-4672-84df-3171fc1ab5dd',
        startTime: 1274392801,
        endTime: 1274400000,
        status: WorkflowTypes.IntervalStatus.NotStarted,
        eventCount: 0,
        completedBy: null,
        activityIntervals: []
      }
    ],
  },
  {
    id: '6',
    stageType: WorkflowTypes.ProcessingStageType.WorkflowInteractive,
    name: 'AL3',
    activities: [],
    intervals: [
      {
        id: '56dad873-bff7-439d-807d-ec4b27d9bc61',
        startTime: 1274299201,
        endTime: 1274306400,
        status: WorkflowTypes.IntervalStatus.Complete,
        eventCount: 15,
        completedBy: {
          userName: 'Jamie'
        },
        activityIntervals: []
      },
      {
        id: '28267633-cb4b-4a31-b171-07740e9578a9',
        startTime: 1274306401,
        endTime: 1274313600,
        status: WorkflowTypes.IntervalStatus.Complete,
        eventCount: 15,
        completedBy: {
          userName: 'Jamie'
        },
        activityIntervals: []
      },
      {
        id: '2e3452c3-ad21-4966-8a7b-e794339636f4',
        startTime: 1274313601,
        endTime: 1274320800,
        status: WorkflowTypes.IntervalStatus.Complete,
        eventCount: 15,
        completedBy: {
          userName: 'Jamie'
        },
        activityIntervals: []
      },
      {
        id: 'd69dd2a8-d0fe-44d1-bcc2-ed596df1f593',
        startTime: 1274320801,
        endTime: 1274328000,
        status: WorkflowTypes.IntervalStatus.Complete,
        eventCount: 15,
        completedBy: {
          userName: 'Jamie'
        },
        activityIntervals: []
      },
      {
        id: 'c6056cfe-5cd4-45d2-9f7b-44ddb54fa31a',
        startTime: 1274328001,
        endTime: 1274335200,
        status: WorkflowTypes.IntervalStatus.Complete,
        eventCount: 15,
        completedBy: {
          userName: 'Jamie'
        },
        activityIntervals: []
      },
      {
        id: 'de33ba6a-9722-44af-ba1c-45ea5da8ee40',
        startTime: 1274335201,
        endTime: 1274342400,
        status: WorkflowTypes.IntervalStatus.Complete,
        eventCount: 15,
        completedBy: {
          userName: 'Jamie'
        },
        activityIntervals: []
      },
      {
        id: 'b1ee4025-ec0c-432a-80de-17d9cec63b3d',
        startTime: 1274342401,
        endTime: 1274349600,
        status: WorkflowTypes.IntervalStatus.Complete,
        eventCount: 15,
        completedBy: {
          userName: 'Jamie'
        },
        activityIntervals: []
      },
      {
        id: 'b68d348d-1cec-40f6-a529-ead52698e285',
        startTime: 1274349601,
        endTime: 1274356800,
        status: WorkflowTypes.IntervalStatus.Complete,
        eventCount: 15,
        completedBy: {
          userName: 'Jamie'
        },
        activityIntervals: []
      },
      {
        id: '2b8764fa-8e47-4f94-8227-7b4c1d00824c',
        startTime: 1274356801,
        endTime: 1274364000,
        status: WorkflowTypes.IntervalStatus.NotStarted,
        eventCount: 12,
        completedBy: {
          userName: 'Jamie'
        },
        activityIntervals: []
      },
      {
        id: 'f7d9ab5b-9f8e-4064-a545-0716c41d0eb4',
        startTime: 1274364001,
        endTime: 1274371200,
        status: WorkflowTypes.IntervalStatus.NotStarted,
        eventCount: 12,
        completedBy: {
          userName: 'Jamie'
        },
        activityIntervals: []
      },
      {
        id: 'bfc5d3a5-0afc-4caf-97f9-1f664c90fe27',
        startTime: 1274371201,
        endTime: 1274378400,
        status: WorkflowTypes.IntervalStatus.NotStarted,
        eventCount: 0,
        completedBy: null,
        activityIntervals: []
      },
      {
        id: '093f73e9-41eb-4ff5-8dfc-df289e7e5f7e',
        startTime: 1274378401,
        endTime: 1274385600,
        status: WorkflowTypes.IntervalStatus.NotStarted,
        eventCount: 0,
        completedBy: null,
        activityIntervals: []
      },
      {
        id: 'faa1bc25-13be-4931-a47c-45624ad582e9',
        startTime: 1274385601,
        endTime: 1274392800,
        status: WorkflowTypes.IntervalStatus.NotStarted,
        eventCount: 0,
        completedBy: null,
        activityIntervals: []
      },
      {
        id: 'f182ab7b-b37b-4a6d-85a8-9727b6348406',
        startTime: 1274392801,
        endTime: 1274400000,
        status: WorkflowTypes.IntervalStatus.NotStarted,
        eventCount: 0,
        completedBy: null,
        activityIntervals: []
      }
    ],
  }
// tslint:disable-next-line:max-file-line-count
];
