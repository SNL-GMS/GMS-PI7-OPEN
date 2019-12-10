import { delayExecution } from '../../../src/ts/util/delay-execution';
import { objectToGraphQLString } from '../../../src/ts/util/object-to-graphql';
import { graphql } from 'graphql';
import { schema } from '../../../src/ts/schema';
import { configProcessor } from '../../../src/ts/config/config-processor';
import { WaveformFilter } from '../../../src/ts/waveform-filter-definition/model';

const delayMs = 500;

describe('waveform-filter query', () => {
    let filterData: WaveformFilter[];
    test('should have a waveform filter query and mutation', async () => {
        const numberOfDefaultFilters = 6;
        const newSampleRate = 555;
        const waveformFilterIds: string[] = configProcessor.getConfigByKey('waveformFilterIds');

        expect(waveformFilterIds).toBeDefined();
        expect(waveformFilterIds).toHaveLength(numberOfDefaultFilters);

        const query = `
        query waveformFilterQuery {
            defaultWaveformFilters{
                id
                name
                description
                filterType
                filterPassBandType
                lowFrequencyHz
                highFrequencyHz
                order
                filterSource
                filterCausality
                zeroPhase
                sampleRate
                sampleRateTolerance
                groupDelaySecs
                validForSampleRate
            }
        }
        `;

        const rootValue = {};
        const result = await delayExecution(() =>  graphql(schema, query, rootValue), delayMs);
        // const { data } = result;
        filterData = result.data.defaultWaveformFilters;

        // expect(waveformFilterIds.length).toEqual(data.length);
        expect(waveformFilterIds.length).toEqual(filterData.length);
        // Compare response to snapshot
        expect(filterData).toMatchSnapshot();

        filterData[0].sampleRate = newSampleRate;

        const mutation = `
        mutation updateWfFilters {
            updateWfFilter (input: {${objectToGraphQLString(filterData[0], '')}}) {
                id
                name
                description
                filterType
                filterPassBandType
                lowFrequencyHz
                highFrequencyHz
                order
                filterSource
                filterCausality
                zeroPhase
                sampleRate
                sampleRateTolerance
                groupDelaySecs
                validForSampleRate
            }
          }
        `;

        const mutationRootValue = {};
        const mutationResult = await delayExecution(() =>  graphql(schema, mutation, mutationRootValue), delayMs);

        // Compare mutation return to the expected change made
        expect(mutationResult.data.updateWfFilter[0].sampleRate).toEqual(newSampleRate);
    });
});
