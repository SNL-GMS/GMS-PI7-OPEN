import { graphql } from 'graphql';
import { schema } from '../src/ts/schema';
import { PhaseType } from '../src/ts/channel-segment/model-spectra';
import { getDefaultFkConfigurationForSignalDetection } from '../src/ts/util/fk-utils';

// No need for pre-test setup yet...
// beforeEach(async () => await setupTest());

// ---- Query test cases ----
// Currently there are no direct queries for FK data
// Instead, FK data are accessed as member data on
// SignalDetectionHypothesis -> azSlownessFeatureMeasurement -> fkData
// See the signal detection unit tests for Fk query UT coverage

// ---- Mutation test cases ----
// Test case - Create a new FK (mutation)
it('Creating new FK should match snapshot', async () => {
  // language=GraphQL
  const mutation = `
  mutation computeFk {
    computeFk(input:
    {
      accepted: false
      stationId: "ARCES"
      frequencyBand: {
        minFrequencyHz: 1.1
        maxFrequencyHz: 2.1
      }
      windowParams: {
        windowType: "hannig"
        leadSeconds: 1.1
        lengthSeconds: 4.4
      }
      contribChannelIds: []
    }, signalDetectionHypothesisId: "1234") {
      frequencyBand{
        minFrequencyHz
        maxFrequencyHz
      }
      slownessScale{
        maxValue
      }
    }
  }`;

  // Execute the GraphQL mutation
  const rootValue = {};
  const result = await graphql(schema, mutation, rootValue);
  const { data } = result;

  // Compare response to snapshot
  expect(data).toMatchSnapshot();
});
//--Fk utils unit test

describe('fk util getDefaultFkConfigurationForSignalDetection', () => {
  test('should return expected medium velocity for phase', () => {
    const pPhase = PhaseType.P;
    const sPhase = PhaseType.S;
    const lgPhase = PhaseType.Lg;
    const rgPhase = PhaseType.Rg;
    const otherPhase = PhaseType.N;
    const expectedForP = 5.8;
    const expectedForS = 3.6;
    const expectedForLg = 3.5;
    const expectedForRg = 3;
    const expectedForOther = 1;

    expect(getDefaultFkConfigurationForSignalDetection(pPhase, '1').mediumVelocity).toBeCloseTo(expectedForP);
    expect(getDefaultFkConfigurationForSignalDetection(sPhase, '1').mediumVelocity).toBeCloseTo(expectedForS);
    expect(getDefaultFkConfigurationForSignalDetection(lgPhase, '1').mediumVelocity).toBeCloseTo(expectedForLg);
    expect(getDefaultFkConfigurationForSignalDetection(rgPhase, '1').mediumVelocity).toBeCloseTo(expectedForRg);
    expect(getDefaultFkConfigurationForSignalDetection(otherPhase, '1').mediumVelocity).toBeCloseTo(expectedForOther);

  });

});

// tslint:disable-next-line:no-empty
afterAll(async () => {});
