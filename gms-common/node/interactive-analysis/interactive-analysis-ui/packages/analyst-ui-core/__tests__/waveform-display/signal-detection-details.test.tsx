import * as React from 'react';
import { SignalDetectionDetails } from '~analyst-ui/common/dialogs';
import { signalDetectionsData } from '../__data__/signal-detections-data';

// tslint:disable-next-line:no-implicit-dependencies no-var-requires no-require-imports
const Enzyme = require('enzyme');

it('SignalDetectionDetails renders & matches snapshot', () => {
  const wrapper = Enzyme.shallow(
    <SignalDetectionDetails detection={signalDetectionsData[0]} color={'red'} />
  );
  expect(wrapper)
    .toMatchSnapshot();
});
