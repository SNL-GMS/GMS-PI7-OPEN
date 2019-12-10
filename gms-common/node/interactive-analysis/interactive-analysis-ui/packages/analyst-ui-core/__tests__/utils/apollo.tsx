import { InMemoryCache, NormalizedCacheObject } from 'apollo-cache-inmemory';
import ApolloClient, { ApolloClientOptions } from 'apollo-client';
import { SchemaLink } from 'apollo-link-schema';
import * as faker from 'faker';
import { GraphQLSchema } from 'graphql';
import { addMockFunctionsToSchema, makeExecutableSchema, MockList } from 'graphql-tools';
import { typeDefs } from './duplicated-code/graphql';
import {
    CreatorType,
    FeatureType,
    IntervalStatus,
    ProcessingActivityType,
    ProcessingStageType,
} from './duplicated-code/model';

const timestampGenerator: () => number
    = (() => {
        let currentTimestamp = 1527868426;
        // tslint:disable-next-line:no-magic-numbers
        return () => currentTimestamp += 3000;
    })();

export function makeMockApolloClient(rngSeed: number = 1066): ApolloClient<NormalizedCacheObject> {
    // Initialize the graphql schema from schema strings
    const schema: GraphQLSchema = makeExecutableSchema({
        typeDefs,
    });

    faker.seed(rngSeed);

    // Add default and provided mock functions to schema
    addMockFunctionsToSchema({
        schema,
        mocks: {
            ProcessingStage: () => ({
                id: () => faker.random.uuid(),
                name: () => faker.random.word(),
                stageType: () => faker.random.objectElement(ProcessingStageType),
                intervals: () => new MockList(faker.random.number({ min: 1, max: 3 })),
                activities: () => new MockList(faker.random.number({ min: 1, max: 3 })),
            }),
            ProcessingStageInterval: () => ({
                id: () => faker.random.uuid(),
                startTime: timestampGenerator,
                endTime: timestampGenerator,
                status: () => faker.random.objectElement(IntervalStatus),
                eventCount: () => faker.random.number({ min: 1, max: 30 }),
                activityIntervals: () => new MockList(faker.random.number({ min: 1, max: 3 })),
            }),
            Analyst: () => ({
                userName: () => faker.internet.userName(),
            }),
            ProcessingActivityInterval: () => ({
                id: () => faker.random.uuid(),
                status: () => faker.random.objectElement(IntervalStatus),
                eventCount: () => faker.random.number({ min: 1, max: 30 }),
                timeStarted: timestampGenerator,
                activeAnalysts: () => new MockList(faker.random.number({ min: 1, max: 3 })),
            }),
            ProcessingActivity: () => ({
                id: () => faker.random.uuid(),
                name: () => faker.random.word(),
                type: () => faker.random.objectElement(ProcessingActivityType),
                stageId: () => faker.random.uuid(),
                intervals: () => new MockList(faker.random.number({ min: 1, max: 3 })),
            }),
            ProcessingInterval: () => ({
                id: () => faker.random.uuid(),
                startTime: timestampGenerator,
                endTime: timestampGenerator,
                stageIntervals: () => new MockList(faker.random.number({ min: 1, max: 3 })),
            }),
            TimeRange: () => ({
                startTime: timestampGenerator,
                endTime: timestampGenerator,
            }),
            WaveformFilter: () => ({
                id: () => faker.random.uuid(),
                name: () => faker.lorem.slug(),
                description: () => faker.lorem.words(),
                filterType: () => faker.lorem.slug(),
                filterPassBandType: () => faker.lorem.slug(),
                lowFrequencyHz: () => faker.random.number({ min: 1, max: 10 }),
                highFrequencyHz: () => faker.random.number({ min: 11, max: 20 }),
                order: () => faker.random.number({ min: 1, max: 10 }),
                filterSource: () => faker.lorem.word(),
                filterCausality: () => faker.lorem.word(),
                zeroPhase: () => faker.random.arrayElement(['TRUE', 'FALSE']),
                sampleRate: () => faker.random.number({ min: 10, max: 100 }),
                sampleRateTolerance: () => faker.random.number({ min: 1, max: 5 }),
                validForSampleRate: () => faker.random.arrayElement(['TRUE', 'FALSE']),
                aCoefficients: () => new MockList(
                    faker.random.number({ min: 1, max: 3 }),
                    () => faker.random.number({ min: 10, max: 100 })
                ),
                bCoefficients: () => new MockList(
                    faker.random.number({ min: 1, max: 3 }),
                    () => faker.random.number({ min: 10, max: 100 })
                ),
                groupDelaySecs: () => faker.random.number({ min: 1, max: 5 }),
            }),
            Location: () => ({
                latDegrees: () => faker.random.number({ min: -90, max: 90 }),
                lonDegrees: () => faker.random.number({ min: 0, max: 180 }),
                elevationKm: () => faker.random.number({ min: 0, max: 10000 }),
            }),
            ProcessingSite: () => ({
                id: () => faker.random.uuid(),
                name: () => faker.lorem.slug(),
                channels: () => new MockList(faker.random.number({ min: 1, max: 5 })),
                networks: () => new MockList(faker.random.number({ min: 1, max: 3 }))
            }),
            ProcessingChannel: () => ({
                id: () => faker.random.uuid(),
                name: () => faker.lorem.slug(),
                channelType: () => faker.lorem.slug(),
                locationCode: () => faker.address.countryCode(),
                verticalAngle: () => faker.random.number({ min: 0, max: 90 }),
                horizontalAngle: () => faker.random.number({ min: 0, max: 90 }),
                sampleRate: () => faker.random.number({ min: 1, max: 30 }),
                qcMasks: () => new MockList(faker.random.number({ min: 1, max: 3 })),
            }),
            QcMask: () => ({
                id: () => faker.random.uuid(),
                channelId: () => faker.random.uuid(),
                qcMaskVersions: () => new MockList(faker.random.number({ min: 0, max: 3 })),
            }),
            QcMaskVersion: () => ({
                category: () => faker.lorem.slug(),
                channelSegmentIds: () => new MockList(
                    faker.random.number({ min: 1, max: 3 }),
                    () => faker.random.uuid(),
                ),
                startTime: timestampGenerator,
                endTime: timestampGenerator,
                parentQcMasks: () => new MockList(faker.random.number({ min: 0, max: 3 })),
                rationale: () => faker.random.words(),
                type: () => faker.lorem.slug(),
                version: () => faker.lorem.slug(),
            }),
            ProcessingNetwork: () => ({
                id: () => faker.random.uuid(),
                name: () => faker.lorem.slug(),
                monitoringOrganization: () => faker.lorem.slug(),
                stations: () => new MockList(faker.random.number({ min: 1, max: 3 })),
            }),
            ProcessingStation: () => ({
                id: () => faker.random.uuid(),
                name: () => faker.lorem.slug(),
                // stationType: () => faker.random.objectElement(),
                sites: () => new MockList(faker.random.number({ min: 1, max: 3 })),
                networks: () => new MockList(faker.random.number({ min: 1, max: 3 })),
                signalDetections: () => new MockList(faker.random.number({ min: 1, max: 3 })),
            }),
            DistanceToSource: () => ({
                distance: () => faker.random.number({ min: 1, max: 10000 }),
                sourceId: () => faker.random.uuid(),
            }),
            EventHypothesis: () => ({
                id: () => faker.random.uuid(),
                rejected: () => faker.random.boolean(),
                signalDetectionAssociations: () => new MockList(faker.random.number({ min: 0, max: 3 })),
                locationSolutions: () => new MockList(faker.random.number({ min: 0, max: 3 })),
            }),
            Event: () => ({
                id: () => faker.random.uuid(),
                monitoringOrganization: () => faker.lorem.slug(),
                preferredHistory: () => new MockList(faker.random.number({ min: 0, max: 3 })),
                preferredForStageHistory: () => new MockList(faker.random.number({ min: 0, max: 3 })),
                hypotheses: () => new MockList(faker.random.number({ min: 0, max: 3 })),
                activeAnalysts: () => new MockList(faker.random.number({ min: 0, max: 3 })),
            }),
            SignalDetectionHypothesis: () => ({
                id: () => faker.random.uuid(),
                phase: () => faker.lorem.slug(),
                rejected: () => faker.random.boolean(),
                signalDetectionAssociations: () => new MockList(faker.random.number({ min: 0, max: 3 })),
            }),
            SignalDetection: () => ({
                id: () => faker.random.uuid(),
                monitoringOrganization: () => faker.lorem.slug(),
                hypotheses: () => new MockList(faker.random.number({ min: 0, max: 3 })),
            }),
            TimeFeatureMeasurement: () => ({
                id: () => faker.random.uuid(),
                featureType: () => faker.random.objectElement(FeatureType),
                definingRules: () => new MockList(faker.random.number({ min: 0, max: 3 })),
                timeSec: timestampGenerator,
                uncertaintySec: () => faker.random.number({ min: 1, max: 30 }),
            }),
            AzSlownessFeatureMeasurement: () => ({
                id: () => faker.random.uuid(),
                featureType: () => faker.random.objectElement(FeatureType),
                azimuthDefiningRules: () => new MockList(faker.random.number({ min: 0, max: 3 })),
                slownessDefiningRules: () => new MockList(faker.random.number({ min: 0, max: 3 })),
                azimuthDeg: () => faker.random.number({ min: 1, max: 89 }),
                slownessSecPerDeg: () => faker.random.number({ min: 1, max: 10 }),
                azimuthUncertainty: () => faker.random.number({ min: 1, max: 10 }),
                slownessUncertainty: () => faker.random.number({ min: 1, max: 10 }),
            }),
            FkData: () => ({
                id: () => faker.random.uuid(),
                accepted: () => faker.random.boolean(),
                attenuation: () => faker.random.number({ min: 1, max: 10 }),
                contribChannels: () => new MockList(faker.random.number({ min: 0, max: 3 })),
                fkGrid: () => new MockList(
                    faker.random.number({ min: 1, max: 10 }),
                    () => new MockList(
                        faker.random.number({ min: 1, max: 10 }),
                        () => faker.random.number({ min: 0, max: 100 })
                    )
                ),
            }),
            FrequencyBand: () => ({
                minFrequencyHz: () => faker.random.number({ min: 1, max: 10 }),
                maxFrequencyHz: () => faker.random.number({ min: 11, max: 20 }),
            }),
            SlownessScale: () => ({
                maxValue: () => faker.random.number({ min: 10, max: 100 }),
                // tslint:disable-next-line:no-magic-numbers
                scaleValues: () => new MockList(5, () => faker.random.number({ min: 1, max: 10 })),
                // tslint:disable-next-line:no-magic-numbers
                scaleValueCount: () => 5,
            }),
            WindowParameters: () => ({
                windowType: () => faker.lorem.slug(),
                leadSeconds: () => faker.random.number({ min: 1, max: 10 }),
                lengthSeconds: () => faker.random.number({ min: 1, max: 10 }),
            }),
            FkPoint: () => ({
                xSlowness: () => faker.random.number({ min: 1, max: 10 }),
                ySlowness: () => faker.random.number({ min: 1, max: 10 }),
                azimuthDeg: () => faker.random.number({ min: 1, max: 10 }),
                radialSlowness: () => faker.random.number({ min: 1, max: 10 }),
                azimuthUncertainty: () => faker.random.number({ min: 1, max: 10 }),
                slownessUncertainty: () => faker.random.number({ min: 1, max: 10 }),
                fstat: () => faker.random.number({ min: 1, max: 10 }),
            }),
            Waveform: () => ({
                id: () => faker.random.uuid(),
                startTime: timestampGenerator,
                endTime: timestampGenerator,
                sampleRate: () => faker.random.number({ min: 1, max: 10 }),
                sampleCount: () => faker.random.number({ min: 1, max: 10 }),
                waveformSamples: () => new MockList(
                    faker.random.number({ min: 10, max: 100 }),
                    () => faker.random.number({ min: 1, max: 100 })
                ),
            }),
            ProcessingCalibration: () => ({
                factor: () => faker.random.number({ min: 1, max: 10 }),
                factorError: () => faker.random.number({ min: 1, max: 10 }),
                period: () => faker.random.number({ min: 1, max: 10 }),
                timeShift: () => faker.random.number({ min: 1, max: 10 }),
            }),
            CreationInfo: () => ({
                id: () => faker.random.uuid(),
                creationTime: () => timestampGenerator,
                creatorId: () => faker.lorem.slug(),
                creatorType: () => faker.random.objectElement(CreatorType)
            }),
            defaultStations: () => new MockList(faker.random.number({min: 1, max: 3})),
        }
    });

    // Setup the mock apollo client
    const clientOptions: ApolloClientOptions<NormalizedCacheObject> = {
        link: new SchemaLink({ schema }),
        cache: new InMemoryCache({
            addTypename: false,
            dataIdFromObject: (object: any) => object.id || null
        }),
    };
    const client: ApolloClient<NormalizedCacheObject> = new ApolloClient(clientOptions);

    return client;
}
