import { getSecureRandomNumber } from '../src/ts/utils/random-number-util';

const testValue = 20;

describe('GMS Random', () => {
  test('should return random number', () => {
    const randomNumber = getSecureRandomNumber();
    // tslint:disable-next-line:no-console
    console.log('randomNumber', randomNumber);

    expect(randomNumber)
        .toBeDefined();
    expect(randomNumber)
        .toBeGreaterThan(0);
    expect(randomNumber)
        .toBeGreaterThanOrEqual(0);
    expect(randomNumber)
        .toBeLessThan(1);
    expect(randomNumber)
        .toBeLessThanOrEqual(1);
  });

  const randomArray: number[] = [];
  for (let i = 0; i < testValue; i++) {
    const r: number = getSecureRandomNumber();
    randomArray.push(r);
  }
  // tslint:disable-next-line:no-console
  console.log(randomArray);

});
