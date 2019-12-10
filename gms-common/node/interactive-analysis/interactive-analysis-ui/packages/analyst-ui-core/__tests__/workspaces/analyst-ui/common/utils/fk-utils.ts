import { frequencyBandToString } from '~analyst-ui/common/utils/fk-utils';
import { FrequencyBand } from '~graphql/fk/types';

/**
 * Tests the ability to check if the peak trough is in warning
 */
describe('frequencyBandToString', () => {
    test('correctly creates frequency band string', () => {
        const band: FrequencyBand = {
            maxFrequencyHz: 5,
            minFrequencyHz: 1
        };
        const testString = '1 - 5 Hz';
        expect(frequencyBandToString(band))
        .toEqual(testString);
    });
});
