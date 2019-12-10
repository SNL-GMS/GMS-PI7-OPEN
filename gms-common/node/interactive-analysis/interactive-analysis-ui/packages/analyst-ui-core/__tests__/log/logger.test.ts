import { showLogPopup, UILogger } from '~util/log/logger';
// tslint:disable: no-console
// tslint:disable: no-unbound-method

describe('UI Logger', () => {

  console.log('UI Logger');

  test('UI Logger should exist', () => {
    console.log('UI Logger should exist');
    expect(UILogger)
      .toBeDefined();
  });

  test('UI Logger should implement log level interface functions', () => {
    expect(UILogger.info)
      .toBeDefined();
    expect(UILogger.data)
      .toBeDefined();
    expect(UILogger.debug)
      .toBeDefined();
    expect(UILogger.error)
      .toBeDefined();
    expect(UILogger.log)
      .toBeDefined();
    expect(UILogger.warn)
      .toBeDefined();
  });

  test('UI Logger can be called', () => {

    const infoMock = jest.spyOn(UILogger, 'info');
    UILogger.info('test1');
    expect(infoMock)
      .toBeCalled();
    expect(infoMock)
      .toBeCalledTimes(1);
    expect(infoMock)
      .toBeCalledWith('test1');

    const warnMock = jest.spyOn(UILogger, 'warn');
    UILogger.warn('test2');
    expect(warnMock)
      .toBeCalled();
    expect(warnMock)
      .toBeCalledTimes(1);
    expect(warnMock)
      .toBeCalledWith('test2');

    const debugMock = jest.spyOn(UILogger, 'debug');
    UILogger.debug('test3');
    expect(debugMock)
      .toBeCalled();
    expect(debugMock)
      .toBeCalledTimes(1);
    expect(debugMock)
      .toBeCalledWith('test3');

    const logMock = jest.spyOn(UILogger, 'log');
    UILogger.log('test4');
    expect(logMock)
      .toBeCalled();
    expect(logMock)
      .toBeCalledTimes(1);
    expect(logMock)
      .toBeCalledWith('test4');

    const errorMock = jest.spyOn(UILogger, 'error');
    UILogger.error('test5');
    expect(errorMock)
      .toBeCalled();
    expect(errorMock)
      .toBeCalledTimes(1);
    expect(errorMock)
      .toBeCalledWith('test5');

    const dataMock = jest.spyOn(UILogger, 'data');
    UILogger.data('test6');
    expect(dataMock)
      .toBeCalled();
    expect(dataMock)
      .toBeCalledTimes(1);
    expect(dataMock)
      .toBeCalledWith('test6');
  });

  test('Log Popup Should Show when called', () => {
    // TODO: add mock single function
    showLogPopup();
    expect(showLogPopup)
      .toBeDefined();
  });
});
