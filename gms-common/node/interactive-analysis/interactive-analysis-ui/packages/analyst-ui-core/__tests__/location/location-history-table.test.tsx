import * as React from 'react';
import { EventUtils } from '~analyst-ui/common/utils';
import {
  LocationHistory,
} from '~analyst-ui/components/location/components/location-history';
// tslint:disable-next-line:max-line-length
import * as LocationData from '../__data__/location-data';

// tslint:disable-next-line:no-implicit-dependencies no-var-requires no-require-imports
const Enzyme = require('enzyme');

it('Location History Table Renders a single location entry', () => {
  const wrapper = Enzyme.shallow(
    <LocationHistory
      event={LocationData.event}
      selectedLocationSolutionSetId={''}
      selectedLocationSolutionId={''}
      setSelectedLSSAndLS={undefined}
    />
  );
  expect(wrapper)
    .toMatchSnapshot();
});

// Unit test for checking that the correct, configured preferred restraints is picked
describe('When location is set to save, correct preferred location is set', async () => {
  it('The osd event hypothesis should match expected result', async () => {
    const preferredId =
      EventUtils.getPreferredLocationIdFromEventHyp(LocationData.eventHypothesisWithLocationSets);
    const hardCodedCorrectId = '3a06fac7-46ad-337e-a8da-090a1cc801a1';
    expect(preferredId)
    .toEqual(hardCodedCorrectId);
  });

});
