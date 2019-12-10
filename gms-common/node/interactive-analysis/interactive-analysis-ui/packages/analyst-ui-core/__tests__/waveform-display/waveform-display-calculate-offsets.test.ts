import * as Faker from 'faker';
import { calculateOffsets } from '~analyst-ui/components/waveform-display/utils';
import { FeaturePrediction } from '~graphql/event/types';
import { FeatureMeasurementTypeName } from '~graphql/signal-detection/types';

const numberOfFeaturePredictions = Faker.random.number({ min: 10, max: 12 });
const fakerWaveformDisplaySeed = 123;
Faker.seed(fakerWaveformDisplaySeed);

// function* MockArrivalTimeMeasurement() {
//   while (true) {
//     const id = Faker.random.uuid();
//     const featureMeasurementType: FeatureMeasurementType = {
//       featureMeasurementTypeName: FeatureMeasurementTypeName.ARRIVAL_TIME
//     };
//     const featureMeasurementValue: FeatureMeasurementValue = {
//       numValue: Faker.date.recent(Faker.random.number({ min: 1, max: 20 }))
//         .getSeconds()
//     };
//     const arrivalTimeMeasurement: FeatureMeasurement = {
//       id, featureMeasurementType, measurementValue: featureMeasurementValue,
//       channelSegmentId: id
//     };
//     yield arrivalTimeMeasurement;
//   }
// }

function* MockFeaturePrediction() {
  while (true) {
    const id = Faker.random.uuid();
    const predictedValue = {
      value: Faker.random.number({ min: 1, max: 20 }),
      standardDeviation: 0
    };
    const predictionType = FeatureMeasurementTypeName.ARRIVAL_TIME;
    const phase = 'P';
    const channelId = '11111111111111111';
    const featurePrediction: FeaturePrediction = {
      id, predictedValue, predictionType, phase, channelId
    };
    yield featurePrediction;
  }
}

const mockFeaturePrediction = MockFeaturePrediction();

const mockFeaturePredictionGenerator: () => FeaturePrediction = (() => () =>
  mockFeaturePrediction.next().value)();

const mockFeaturePredictions: FeaturePrediction[] = [];

for (let i = 0; i < numberOfFeaturePredictions; i++) {
  mockFeaturePredictions.push(mockFeaturePredictionGenerator());
}

describe('Waveform Display Utility Test', () => {
  describe('Calculate Offsets', () => {
    test('calculateOffsets should return a list of offsets', () => {
      // tslint:disable-next-line:no-console
      console.log(mockFeaturePredictions);

      const offsets = calculateOffsets(mockFeaturePredictions, 'P');
      // tslint:disable-next-line:no-console
      console.log(offsets);
      expect(offsets)
        .toBeDefined();
    });
  });
});
