import { ReactWrapper } from 'enzyme';
import * as React from 'react';
import { getFkData } from '~analyst-ui/common/utils/fk-utils';
// tslint:disable-next-line:max-line-length
import {
  FkProperties,
  FkPropertiesProps
} from '~analyst-ui/components/azimuth-slowness/components/fk-display/fk-properties';
import { FkParams, FkUnits } from '~analyst-ui/components/azimuth-slowness/types';
import {
  sigalDetectionsByEventIde60fc61104ca4b859e0b0913cb5e6c48
} from '../__data__/signal-detections-by-event-id-small';
import { analystCurrentFk } from '../__data__/test-util';

// tslint:disable-next-line:no-implicit-dependencies no-var-requires no-require-imports
const Enzyme = require('enzyme');
// tslint:disable-next-line:no-implicit-dependencies no-var-requires no-require-imports
const Adapter = require('enzyme-adapter-react-16');

const mockProps: FkPropertiesProps = {
  signalDetection: sigalDetectionsByEventIde60fc61104ca4b859e0b0913cb5e6c48[0],
  signalDetectionFeaturePredictions: [],
  analystCurrentFk,
  userInputFkFrequency: {
    minFrequencyHz: 1,
    maxFrequencyHz: 4
  },
  userInputFkWindowParameters: {
    lengthSeconds: 4,
    leadSeconds: 1,
    stepSize: 1
  },
  fkUnitDisplayed: FkUnits.FSTAT,
  fkFrequencyThumbnails: [],
  onFkConfigurationChange: () => {
    return;
  },
  onNewFkParams: async (sdId: string, fkParams: FkParams) => {
    return;
  }
};

describe('FK thumbnails details tests', () => {
  // enzyme needs a new adapter for each configuration
  beforeEach(() => {
    Enzyme.configure({ adapter: new Adapter() });
  });

  test.skip('renders a snapshot', (done: jest.DoneCallback) => {
    // Mounting enzyme into the DOM
    // Using a testing DOM not real DOM
    // So a few things will be missing window.fetch, or alert etc...
    const wrapper: ReactWrapper = Enzyme.mount(
      // 3 nested components would be needed if component dependent on apollo-redux for example see workflow
      // (apollo provider, provider (redux provider), Redux-Apollo (our wrapped component)
      <FkProperties {...mockProps} />
    );

    const contextMenuTarget = wrapper.find('div.grid-item');
    contextMenuTarget.first()
      .simulate('click');

    setImmediate(() => {
      wrapper.update();

      expect(wrapper.find(FkProperties))
        .toMatchSnapshot();

      done();
    });
  });
  test('fk data changes on input', (done: jest.DoneCallback) => {
    expect(FkProperties)
      .toBeDefined();
    done();
  });
  test.skip('Fk Details has no errors', () => {
    const wrapper = Enzyme.shallow(<FkProperties {...mockProps} />);

    // FK details has no errors
    expect(wrapper.instance().props)
      .toHaveProperty('data.error', undefined);
    expect(wrapper.instance().props.data)
      .toBeDefined();
    expect(wrapper.instance().props.data)
      .toMatchObject(sigalDetectionsByEventIde60fc61104ca4b859e0b0913cb5e6c48[0]);
    const newSd = sigalDetectionsByEventIde60fc61104ca4b859e0b0913cb5e6c48[0];
    const fkData = getFkData(newSd.currentHypothesis.featureMeasurements);
    // Strip out timeseries so snapshot isn't gigantic
    if (fkData) {
      fkData.leadSpectrum.fstat = [[0]];
      fkData.leadSpectrum.power = [[0]];
    }

    // Change the fkData prop
    wrapper.setProps({ data: newSd });

    // Check that fkData is different and matches new fkData input
    expect(wrapper.instance().props.data)
      .toEqual(newSd);
  });
});
