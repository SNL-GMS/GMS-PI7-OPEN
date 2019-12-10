import { environmentConfig, EnvironmentConfig } from './environment-config';
import { systemConfig, SystemConfig } from './system-config';
import { userPreferences, UserPreferences } from './user-preferences';

export interface AnalystUiConfig {
  userPreferences: UserPreferences;
  environment: EnvironmentConfig;
  systemConfig: SystemConfig;
}

export const analystUiConfig: AnalystUiConfig = {
  userPreferences,
  environment: environmentConfig,
  systemConfig
};

export { userPreferences, UserPreferences, QcMaskDisplayFilters } from './user-preferences';
export { environmentConfig, EnvironmentConfig } from './environment-config';
export { systemConfig, SystemConfig } from './system-config';
