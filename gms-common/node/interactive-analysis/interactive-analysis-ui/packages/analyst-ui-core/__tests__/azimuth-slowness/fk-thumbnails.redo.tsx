import { ReactWrapper } from 'enzyme';
import * as Immutable from 'immutable';
import * as React from 'react';
import {
  FkThumbnailList,
  FkThumbnailListProps
} from '~analyst-ui/components/azimuth-slowness/components/fk-thumbnail-list/fk-thumbnail-list';
import { FkUnits } from '~analyst-ui/components/azimuth-slowness/types';
import { FeaturePrediction } from '~graphql/event/types';
import { WaveformSortType } from '~state/analyst-workspace/types';
import { signalDetectionsData } from '../__data__/signal-detections-data';

// tslint:disable-next-line:no-implicit-dependencies no-var-requires no-require-imports
const Enzyme = require('enzyme');
// tslint:disable-next-line:no-implicit-dependencies no-var-requires no-require-imports
const Adapter = require('enzyme-adapter-react-16');

const mockProps: FkThumbnailListProps = {
  sortedSignalDetections: signalDetectionsData,
  signalDetectionIdsToFeaturePrediction: Immutable.Map<string, FeaturePrediction[]>(),
  thumbnailSizePx: 300,
  selectedSortType: WaveformSortType.distance,
  selectedSdIds: [],
  unassociatedSdIds: [],
  fkUnitsForEachSdId: Immutable.Map<string, FkUnits>(),
  clearSelectedUnassociatedFks: () => {
    /** empty */
  },
  markFksForSdIdsAsReviewed: () => {
    /** empty */
  },
  showFkThumbnailContextMenu: () => {
    /** empty */
  },
  setSelectedSdIds: (ids: string[]) => {
    /** empty */
}};

describe('FK thumbnails tests', () => {
  // enzyme needs a new adapter for each configuration
  beforeEach(() => {
    Enzyme.configure({ adapter: new Adapter() });
  });

  it('renders a snapshot', (done: jest.DoneCallback) => {
    // Mounting enzyme into the DOM
    // Using a testing DOM not real DOM
    // So a few things will be missing window.fetch, or alert etc...
    const wrapper: ReactWrapper = Enzyme.mount(
      // 3 nested components would be needed if component dependent on apollo-redux for example see workflow
      // (apollo provider, provider (redux provider), Redux-Apollo (our wrapped component)
      <FkThumbnailList {...mockProps} />
    );

    setImmediate(() => {
      wrapper.update();

      expect(wrapper.find(FkThumbnailList))
        .toMatchSnapshot();

      done();
    });
  });
});
