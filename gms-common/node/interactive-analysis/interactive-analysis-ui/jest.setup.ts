import { mount, render, shallow } from 'enzyme';
import toJson from 'enzyme-to-json';
import 'jsdom-global/register';
import fetch from 'node-fetch';

// tslint:disable-next-line:no-implicit-dependencies no-var-requires no-require-imports
require('jest-canvas-mock');

// TODO: Remove this `raf` polyfill once the below issue is sorted
// https://github.com/facebookincubator/create-react-app/issues/3199#issuecomment-332842582
// @see https://medium.com/@barvysta/warning-react-depends-on-requestanimationframe-f498edd404b3
const globalAny: any = global;
export const raf = globalAny.requestAnimationFrame = cb => {
  setTimeout(cb, 0);
};

// tslint:disable-next-line:no-implicit-dependencies no-var-requires no-require-imports
const Enzyme = require('enzyme');
// tslint:disable-next-line:no-implicit-dependencies no-var-requires no-require-imports
const Adapter = require('enzyme-adapter-react-16');

// React Enzyme adapter
Enzyme.configure({ adapter: new Adapter() });

// Make Enzyme functions available in all test files without importing
globalAny.shallow = shallow;
globalAny.render = render;
globalAny.mount = mount;
globalAny.toJson = toJson;
globalAny.fetch = fetch;
