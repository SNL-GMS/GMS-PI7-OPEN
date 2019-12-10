import { CommonTypes, EventTypes } from '~graphql/';

// tslint:disable:no-magic-numbers

export const eventData: EventTypes.Event = {
    id: 'f43f58f9-6a87-40e3-95ac-44168325fc49',
    conflictingSdHypIds: [],
    status: EventTypes.EventStatus.ReadyForRefinement,
    modified: false,
    hasConflict: false,
    currentEventHypothesis: {
      processingStage: {
      id: '1'
      },
      eventHypothesis: {
      locationSolutionSets: [],
      id: '186f997b-7d7d-3151-8b4d-5609f7a8f31f',
      rejected: false,
      event: {
          id: '186f997b-7d7d-3151-8b4d-5609f7a8f31f',
          status: EventTypes.EventStatus.ReadyForRefinement,
      },
      preferredLocationSolution: {
          locationSolution: {
          id: '186f997b-7d7d-3151-8b4d-5609f7a8f31f',
          location: {
              latitudeDegrees: 67.57425,
              longitudeDegrees: 33.59468,
              depthKm: 0,
              time: 1274399850.081
          },
          snapshots: [],
          featurePredictions: [],
          locationRestraint: {
              depthRestraintType: EventTypes.DepthRestraintType.FIXED_AT_SURFACE,
              depthRestraintKm: null,
              latitudeRestraintType: EventTypes.RestraintType.UNRESTRAINED,
              latitudeRestraintDegrees: null,
              longitudeRestraintType: EventTypes.RestraintType.UNRESTRAINED,
              longitudeRestraintDegrees: null,
              timeRestraintType: EventTypes.RestraintType.UNRESTRAINED,
              timeRestraint: null
          },
          locationUncertainty: {
              xy: 163.665,
              xz: -1,
              xt: -26.6202,
              yy: 400.817,
              yz: -1,
              yt: 20.2312,
              zz: -1,
              zt: -1,
              tt: 6.6189,
              stDevOneObservation: 1.0484,
              ellipses: [
              {
                  scalingFactorType: EventTypes.ScalingFactorType.CONFIDENCE,
                  kWeight: 0,
                  confidenceLevel: 0.9,
                  majorAxisLength: '49.1513',
                  majorAxisTrend: 37.23,
                  minorAxisLength: '29.2083',
                  minorAxisTrend: -1,
                  depthUncertainty: -1,
                  timeUncertainty: 'PT4.235S'
              }
              ],
              ellipsoids: []
          },
          locationBehaviors: [
              {
              residual: 1.28,
              weight: 0.734,
              defining: false,
              featurePredictionId: '00000000-0000-0000-0000-000000000000',
              featureMeasurementId: 'c6d4c116-f3e4-4b5a-affd-e8f003f5a2d4'
              },
              {
              residual: 2.6,
              weight: 0.734,
              defining: true,
              featurePredictionId: '00000000-0000-0000-0000-000000000000',
              featureMeasurementId: '9f4698cf-dca1-45f7-94d4-cdcb49ee8815'
              },
              {
              residual: -0.971,
              weight: 0.734,
              defining: true,
              featurePredictionId: '00000000-0000-0000-0000-000000000000',
              featureMeasurementId: '96759b42-2a51-4b3e-9b61-2acf352d5c3f'
              }
          ],
          locationType: 'standard'
          },
          creationInfo: {
          id: 'dd1a0e56-4672-4ff7-8f1f-ba76e483233a',
          creationTime: 1551284809.237,
          creatorId: '1',
          creatorType: CommonTypes.CreatorType.Analyst,
          creatorName: 'Chris'
          }
      },
      associationsMaxArrivalTime: 1274399905.01,
      signalDetectionAssociations: [
          {
          id: '527562d9-027e-4b88-941e-91543763b7a4',
          rejected: false,
          eventHypothesisId: '186f997b-7d7d-3151-8b4d-5609f7a8f31f',
          signalDetectionHypothesis: {
              id: '1c8f8122-0056-3ed9-9304-ddea79de2393',
              rejected: false,
              creationInfo: {
              id: '1c8f8122-0056-3ed9-9304-ddea79de2393',
              creationTime: 1551291953.323,
              creatorId: 'creatorId',
              creatorType: CommonTypes.CreatorType.Analyst,
              creatorName: 'Matthew Carrasco'
              }
          }
          }
      ]
      }
  },
    activeAnalysts: []
};
