// tslint:disable: no-magic-numbers
// tslint:disable: newline-per-chained-call
import * as Faker from 'faker';
import { WeavessTypes, WeavessUtils } from '../../src/ts/weavess';

const fakerDummyWaveformSeed = 123;
const timeInterval = 3000;
const startTimeSecs = 1527868426;
const endTimeSecs = startTimeSecs + timeInterval;
const sampleRate1 = 20;
const sampleRate2 = 40;
const sampleRate3 = 100;
const sampleRates: number[] = [ sampleRate1, sampleRate2, sampleRate3 ];

Faker.seed(fakerDummyWaveformSeed);

describe('WEAVESS Util', () => {
    describe('waveform-util', () => {

        describe('UUIDv4', () => {

            test('test that UUID does not equal', () => {
                expect(WeavessUtils.Waveform.UUIDv4()).not.toEqual(WeavessUtils.Waveform.UUIDv4());
            });
        });

        describe('createFlatLineDataSegment', () => {
            test('create flat line data segment with bad data', () => {
                expect(() => {
                    WeavessUtils.Waveform.createFlatLineDataSegment(1, 0, 5);
                  })
                  .toThrow();
            });

            test('create flat line data segment with bad sample rate', () => {
                expect(() => {
                    WeavessUtils.Waveform.createFlatLineDataSegment(1, 0, 5, 'grey', -1);
                  })
                  .toThrow();
            });

            test('create flat line data segment with no sample rate', () => {
                const amplitude = 5;
                const dataSegment = WeavessUtils.Waveform.createFlatLineDataSegment(
                    startTimeSecs, endTimeSecs, amplitude);

                const data: number[] = Array.from(dataSegment.data);
                expect(dataSegment.startTimeSecs).toEqual(startTimeSecs);
                expect(data.length).toEqual(endTimeSecs - startTimeSecs);
                expect(data.every((v: number) => v === amplitude)).toBeTruthy();

                // validate the endtime seconds
                expect(startTimeSecs + (data.length / dataSegment.sampleRate)).toEqual(endTimeSecs);

                expect(dataSegment.color).toBeUndefined();
                expect(dataSegment.displayType).toBeUndefined();
                expect(dataSegment.pointSize).toBeUndefined();
            });

            test('create flat line data segment with sample rate', () => {
                const amplitude = 3;
                const color = 'green';
                const displayType = [WeavessTypes.DisplayType.SCATTER, WeavessTypes.DisplayType.LINE];
                const pointSize = 7;
                const dataSegment = WeavessUtils.Waveform.createFlatLineDataSegment(
                    startTimeSecs, endTimeSecs, amplitude,
                    color, sampleRate1, displayType, pointSize);

                const data: number[] = Array.from(dataSegment.data);
                expect(dataSegment.startTimeSecs).toEqual(startTimeSecs);
                expect(data.length).toEqual((endTimeSecs - startTimeSecs) * sampleRate1);
                expect(data.every((v: number) => v === amplitude)).toBeTruthy();

                // validate the endtime seconds
                expect(startTimeSecs + (data.length / sampleRate1)).toEqual(endTimeSecs);

                expect(dataSegment.color).toEqual(color);
                expect(dataSegment.displayType).toEqual(displayType);
                expect(dataSegment.pointSize).toEqual(pointSize);
            });

            test('create flat line data segment with sample rate really small', () => {
                const amplitude = 3;
                const color = 'green';
                const displayType = [WeavessTypes.DisplayType.SCATTER, WeavessTypes.DisplayType.LINE];
                const pointSize = 7;
                const sampleRate = 0.01;
                const dataSegment = WeavessUtils.Waveform.createFlatLineDataSegment(
                    startTimeSecs, endTimeSecs, amplitude,
                    color, sampleRate, displayType, pointSize);

                const data: number[] = Array.from(dataSegment.data);
                expect(dataSegment.startTimeSecs).toEqual(startTimeSecs);
                expect(data.length).toEqual((endTimeSecs - startTimeSecs) * sampleRate);
                expect(data.every((v: number) => v === amplitude)).toBeTruthy();

                // validate the endtime seconds
                expect(startTimeSecs + (data.length / sampleRate)).toEqual(endTimeSecs);

                expect(dataSegment.color).toEqual(color);
                expect(dataSegment.displayType).toEqual(displayType);
                expect(dataSegment.pointSize).toEqual(pointSize);
            });
        });

        describe('createFlatLineChannelSegment', () => {

            test('create flat line channel segment with bad data', () => {
                expect(() => {
                    WeavessUtils.Waveform.createFlatLineChannelSegment(1, 0, 5);
                  })
                  .toThrow();
            });

            test('create flat line channel segment with bad sample rate', () => {
                expect(() => {
                    WeavessUtils.Waveform.createFlatLineChannelSegment(1, 0, 5, 'grey', 0);
                  })
                  .toThrow();
            });

            test('create flat line channel segment with no sample rate', () => {
                const amplitude = 5;
                const channelSegment = WeavessUtils.Waveform.createFlatLineChannelSegment(
                    startTimeSecs, endTimeSecs, amplitude);

                expect(channelSegment.dataSegments.length).toEqual(1);

                const dataSegment = channelSegment.dataSegments[0];
                const data: number[] = Array.from(dataSegment.data);
                expect(dataSegment.startTimeSecs).toEqual(startTimeSecs);
                expect(data.length).toEqual(endTimeSecs - startTimeSecs);
                expect(data.every((v: number) => v === amplitude)).toBeTruthy();

                // validate the endtime seconds
                expect(startTimeSecs + (data.length / dataSegment.sampleRate)).toEqual(endTimeSecs);

                expect(dataSegment.color).toBeUndefined();
                expect(dataSegment.displayType).toBeUndefined();
                expect(dataSegment.pointSize).toBeUndefined();
                expect(channelSegment.description).toBeUndefined();
                expect(channelSegment.descriptionLabelColor).toBeUndefined();
            });

            test('create flat line channel segment with sample rate', () => {
                const amplitude = 3;
                const color = 'green';
                const displayType = [WeavessTypes.DisplayType.SCATTER, WeavessTypes.DisplayType.LINE];
                const pointSize = 7;
                const description = 'sample flat line';
                const descriptionColor = 'purple';
                const channelSegment = WeavessUtils.Waveform.createFlatLineChannelSegment(
                    startTimeSecs, endTimeSecs, amplitude,
                    color, sampleRate2, displayType, pointSize, description, descriptionColor
                    );

                expect(channelSegment.dataSegments.length).toEqual(1);
                const dataSegment = channelSegment.dataSegments[0];
                const data: number[] = Array.from(dataSegment.data);
                expect(dataSegment.startTimeSecs).toEqual(startTimeSecs);
                expect(data.length).toEqual((endTimeSecs - startTimeSecs) * sampleRate2);
                expect(data.every((v: number) => v === amplitude)).toBeTruthy();

                // validate the endtime seconds
                expect(startTimeSecs + (data.length / sampleRate2)).toEqual(endTimeSecs);

                expect(dataSegment.color).toEqual(color);
                expect(dataSegment.displayType).toEqual(displayType);
                expect(dataSegment.pointSize).toEqual(pointSize);
                expect(channelSegment.description).toEqual(description);
                expect(channelSegment.descriptionLabelColor).toEqual(descriptionColor);
            });

        });

        describe('createDummyWaveform', () => {

            const sampleRate = Faker.random.arrayElement(sampleRates);
            const eventAmplitude: number = Faker.random.number({ min: 1, max: 30 });
            const noiseAmplitude: number = Faker.random.number({ min: 1, max: 30 });

            const dummyWaveform: WeavessTypes.Station = WeavessUtils.Waveform.createDummyWaveform(
                startTimeSecs, endTimeSecs, sampleRate,
                eventAmplitude, noiseAmplitude
            );

            test('createDummyWaveform should return a dummy waveform', () => {
                expect(dummyWaveform)
                    .toBeDefined();
            });

            test('dummyWaveform should have data', () => {
                expect(dummyWaveform)
                    .toHaveProperty('defaultChannel');
            });

        });

    });
});
