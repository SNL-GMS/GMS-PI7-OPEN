// tslint:disable:max-line-length
import { readJsonData } from '@gms/ui-core-components';
import ApolloClient from 'apollo-client';
import * as Immutable from 'immutable';
import * as path from 'path';
import { createApolloClient } from '~apollo/client';
import { WaveformTypes } from '~graphql/';
import { MeasurementMode, Mode } from '~state/analyst-workspace/types';
import { WaveformClient } from '../../src/ts/workspaces/analyst-ui/components/waveform-display/waveform-client';
import {
    createWeavessStations,
    CreateWeavessStationsParameters
} from '../../src/ts/workspaces/analyst-ui/components/waveform-display/weavess-stations-util';

const apolloClient: ApolloClient<any> | undefined = createApolloClient().client;

describe('Waveform display unit tests', () => {

    it('When switching to measurement mode, should show only waveforms/channels with associated SD', () => {
        const basePath = path.resolve(__dirname, './__data__');
        const expectedResultMeasureMode = readJsonData(path.resolve(basePath, 'weavessStationMeasureModeResult.json'));
        const expectedResultDefaultMode = readJsonData(path.resolve(basePath, 'weavessStationDefaultModeResult.json'));
        const currentOpenEvent = readJsonData(path.resolve(basePath, 'currentOpenEvent.json'))[0];
        const defaultStations = readJsonData(path.resolve(basePath, 'defaultStations.json'));
        const defaultWaveformFilters = readJsonData(path.resolve(basePath, 'defaultWaveformFilters.json'));
        const distanceToSourceForDefaultStations = readJsonData(path.resolve(basePath, 'distanceToSourceForDefaultStations.json'));
        const eventsInTimeRange = [currentOpenEvent];
        const featurePredictions = readJsonData(path.resolve(basePath, 'featurePredictions.json'))[0];
        const maskDisplayFilters = readJsonData(path.resolve(basePath, 'maskDisplayFilters.json'))[0];
        const measurementMode: MeasurementMode = {
            mode: Mode.MEASUREMENT,
            entries: Immutable.Map<string, boolean>()
        };
        const defaultMode: MeasurementMode = {mode: Mode.DEFAULT, entries: Immutable.Map()};
        const qcMasksByChannelId = readJsonData(path.resolve(basePath, 'qcMasksByChannelId.json'));
        const signalDetectionsByStation = readJsonData(path.resolve(basePath, 'signalDetectionsByStation.json'));
        const waveformClient = new WaveformClient(apolloClient);
        const channelFilters = Immutable.Map<string, WaveformTypes.WaveformFilter>();
        const waveformUtilParams: CreateWeavessStationsParameters = {
            channelFilters,
            channelHeight: 24.8,
            currentOpenEvent,
            defaultStations,
            defaultWaveformFilters,
            distanceToSourceForDefaultStations,
            endTimeSecs: 1274400000,
            eventsInTimeRange,
            featurePredictions,
            maskDisplayFilters,
            measurementMode: defaultMode,
            offsets: [],
            qcMasksByChannelId,
            showPredictedPhases: false,
            signalDetectionsByStation,
            startTimeSecs: 1274392801,
            waveformClient
        };

        let result = createWeavessStations(waveformUtilParams);
        result.forEach(station => {
            delete station.defaultChannel.waveform.channelSegments;
            station.nonDefaultChannels.forEach(channel => delete channel.waveform.channelSegments);
        });

        expect(result)
            .toEqual(expectedResultDefaultMode);

        waveformUtilParams.measurementMode = measurementMode;

        result = createWeavessStations(waveformUtilParams);
        result.forEach(station => {
            delete station.defaultChannel.waveform.channelSegments;
            station.nonDefaultChannels.forEach(channel => delete channel.waveform.channelSegments);
        });
        expect(result)
            .toEqual(expectedResultMeasureMode);
    });
});
