import { calculateStartTimeForFk } from '../../src/ts/util/time-utils';
// tslint:disable: no-magic-numbers

describe('fk start time utility', () => {
  test('tests simple examples', () => {
    // spks = spectrum start time
    //      spks      spks      spks      spks      spks      spks       spks
    //  |---|---------|---------|---------|---------|---------|------|---|
    //  wfst                                                         arrivalTime
    let startTime = calculateStartTimeForFk(0, 60, 7, 10);
    expect(startTime).toEqual(3);

    // test for when no steps can be done before arrival
    startTime = calculateStartTimeForFk(0, 8, 7, 10);
    expect(startTime).toEqual(1);

    // test with negative lead time
    startTime = calculateStartTimeForFk(0, 60, -2, 10);
    expect(startTime).toEqual(2);
  });

  test('tests a more realistic example', () => {
    const wfStartTime = 1274385600;
    const arrivalTime = wfStartTime + 60;
    const startTime = calculateStartTimeForFk(wfStartTime, arrivalTime, 1, 2);
    expect(startTime).toEqual(1274385601);
  });

  test('tests bad inputs', () => {
    const wfStartTime = 1274385600;
    const arrivalTime = wfStartTime + 60;
    let startTime = calculateStartTimeForFk(wfStartTime, arrivalTime, 1, 2);
    expect(startTime).toEqual(1274385601);

    // With a start time and arrival time both the same, we should get undefined
    startTime = calculateStartTimeForFk(0, 0, 7, 10);
    expect(startTime).toBeUndefined();

    // start and end time are less than the lead time apart
    startTime = calculateStartTimeForFk(0, 6, 7, 10);
    expect(startTime).toBeUndefined();

    // test undefined inputs
    startTime = calculateStartTimeForFk(undefined, 60, 7, 10);
    expect(startTime).toBeUndefined();

    startTime = calculateStartTimeForFk(0, undefined, 7, 10);
    expect(startTime).toBeUndefined();

    startTime = calculateStartTimeForFk(0, 60, undefined, 10);
    expect(startTime).toBeUndefined();

    startTime = calculateStartTimeForFk(0, 60, 7, undefined);
    expect(startTime).toBeUndefined();
  });
});
