import { SignalDetectionQueries } from '~graphql/';

it('should be the correct query', () => {
  expect(SignalDetectionQueries.signalDetectionsByStationQuery)
    .toMatchSnapshot();
});
