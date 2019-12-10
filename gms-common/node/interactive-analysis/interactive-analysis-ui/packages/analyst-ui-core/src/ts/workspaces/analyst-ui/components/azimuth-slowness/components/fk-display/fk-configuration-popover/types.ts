import { CheckboxListTypes } from '@gms/ui-core-components';
import { FkConfigurationWithUnits, FkUnits } from '~analyst-ui/components/azimuth-slowness/types';
import { FkConfiguration } from '~graphql/fk/types';

export enum FkConfigurationPopoverPanel {
    DEFAULT, ADVANCED
}

export interface FkConfigurationPopoverState {
    openPanel: FkConfigurationPopoverPanel;
    fkUnits: FkUnits;
    normalizeWaveforms: boolean;
    mediumVelocity: number;
    maximumSlowness: number;
    numberOfPoints: number;
    channelCheckboxes: CheckboxListTypes.CheckboxItem[];
    useVerticalChannelOffsets: boolean;
}
export interface FkConfigurationPopoverProps extends FkConfiguration {
    fkUnitDisplayed: FkUnits;
    applyFkConfiguration(configuration: FkConfigurationWithUnits);
    close();
}
