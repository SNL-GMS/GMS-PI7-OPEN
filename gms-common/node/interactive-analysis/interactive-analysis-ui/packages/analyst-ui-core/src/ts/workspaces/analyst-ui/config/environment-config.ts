import { CESIUM_OFFLINE } from '~util/environment';

export interface EnvironmentConfig {
  map: {
    online: boolean;
    offlineImagery: {
      url: string;
      maxResolutionLevel: number;
    };
  };
}

export const environmentConfig: EnvironmentConfig = {
  map: {
    online: !CESIUM_OFFLINE,
    offlineImagery: {
      url: 'Assets/Textures/NaturalEarthII',
      maxResolutionLevel: 2
    },
  },
};
