import { getSecureRandomNumber } from '../src/ts/util/common-utils';

describe('gms random number generator', () => {
  test('should return random number', () => {
    const randomNumber = getSecureRandomNumber();
    expect(randomNumber).toBeDefined();
    expect(randomNumber).toBeGreaterThanOrEqual(0);
    expect(randomNumber).toBeLessThan(1);
  });

});
