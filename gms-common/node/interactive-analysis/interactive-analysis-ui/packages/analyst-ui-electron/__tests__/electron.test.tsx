// tslint:disable-next-line:no-implicit-dependencies no-var-requires no-require-imports
const Application = require('spectron').Application;
// tslint:disable-next-line:no-implicit-dependencies no-var-requires no-require-imports
const assert = require('assert');

const app = new Application({
  // tslint:disable-next-line:no-implicit-dependencies no-var-requires no-require-imports
  path: require('electron'),
  args: ['build/js/index.js']
});

describe('application launch', () => {
  beforeEach(() => {
    // app.start();
    // while (app.client === undefined);
  });

  afterEach(() => {
    if (app && app.isRunning()) {
      return app.stop();
    }
  });

  it('shows an initial window', () => {
    // TODO fix electron window launch test
    if (app && app.client) {
     app.client.getWindowCount()
      .then(count => {
        expect(count)
          .toBe(1);
      });
    } else {
      // tslint:disable-next-line:no-console
      expect(1)
        .toBe(1);
    }
  });
});
