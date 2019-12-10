import * as React from 'react';

// tslint:disable-next-line:no-implicit-dependencies no-var-requires no-require-imports
const Enzyme = require('enzyme');
// tslint:disable-next-line:no-implicit-dependencies no-var-requires no-require-imports
const Adapter = require('enzyme-adapter-react-16');

Enzyme.configure({ adapter: new Adapter() });

describe('Test should run', () => {
  describe('Test Environment is available', () => {
    describe('enzyme should be available', () => {
      test('render a label', () => {
        const wrapper = Enzyme.shallow(<label>Hello Jest!</label>);
        expect(wrapper)
          .toMatchSnapshot();
      });
      test('render a div', () => {
        const wrapper = Enzyme.shallow(<div>Hello Jest!</div>);
        expect(wrapper)
          .toMatchSnapshot();
      });
      test('render an h1', () => {
        const wrapper = Enzyme.shallow(<h1>Hello Jest!</h1>);
        expect(wrapper)
          .toMatchSnapshot();
      });
    });
  });
});
