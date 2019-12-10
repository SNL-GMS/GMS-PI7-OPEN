import { shallow } from 'enzyme';
import * as React from 'react';
import * as renderer from 'react-test-renderer';
import { FkThumbnail, FkThumbnailProps } from '~analyst-ui/components/azimuth-slowness/components/fk-thumbnail';
import { FkUnits } from '~analyst-ui/components/azimuth-slowness/types';

const pixelSize: any = 200;
const mockCallBack = jest.fn();

const fkThumbProps: FkThumbnailProps = {
  // TODO useless test
  fkData: undefined, // This used to be a signal detection (without channel segments) - fk data is undefined
  predictedPoint: undefined,
  sizePx: pixelSize,
  label: 'USRK P',
  selected: false,
  fkUnit: FkUnits.FSTAT,
  dimFk: false,
  onClick: mockCallBack,
  showFkThumbnailMenu: () => {
    /** empty */
  },
};

it('FkThumbnails renders & matches snapshot', () => {
  const tree = renderer.create(<FkThumbnail {...fkThumbProps} />)
    .toJSON();

  expect(tree)
    .toMatchSnapshot();
});

it('FkThumbnails onClick fires correctly', () => {
  const thumbnail = shallow(
    <FkThumbnail
      {
      ...fkThumbProps
      }
    />
  );
  thumbnail.find('.fk-thumbnail')
  .simulate('click');
  expect(mockCallBack)
  .toBeCalled();
});
