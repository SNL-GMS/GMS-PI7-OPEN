import { StageIntervalBlueprintContextMenu } from '~analyst-ui/components/workflow/components';
import { WorkflowTypes } from '~graphql/';

// tslint:disable-next-line:no-implicit-dependencies no-var-requires no-require-imports
const Enzyme = require('enzyme');
// tslint:disable-next-line:no-implicit-dependencies no-var-requires no-require-imports
const Adapter = require('enzyme-adapter-react-16');

describe('workflow context menu stage interval tests', () => {
  beforeEach(() => {
    Enzyme.configure({ adapter: new Adapter() });
  });

  it('can mark stage interval on click', () => {
    const fn = jest.fn();

    const wrapper = Enzyme.shallow(StageIntervalBlueprintContextMenu(fn));

    wrapper.find('.menu-item-mark-stage-interval')
      .simulate('click');

    expect(fn)
      .toHaveBeenCalledWith(WorkflowTypes.IntervalStatus.Complete);
  });
});
