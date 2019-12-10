import * as GoldenLayout from '@gms/golden-layout';

import { componentList, ComponentList, defaultGoldenLayoutConfig } from './golden-layout-config';

export interface UiConfig {
  components: ComponentList;
  workspace: GoldenLayout.Config;
}

export const uiConfig: UiConfig = {
  components: componentList,
  workspace: defaultGoldenLayoutConfig,
};
