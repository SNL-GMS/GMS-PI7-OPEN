import { defaultsDeep } from 'lodash';
import { Configuration } from '../../entities';
import { DEFAULT_CHANNEL_HEIGHT_PIXELS, DEFAULT_LABEL_WIDTH_PIXELS } from './constants';

/** Defines the default configuration for Weavess */
export const defaultConfigutation: Configuration = {
  defaultChannelHeightPx: DEFAULT_CHANNEL_HEIGHT_PIXELS,

  labelWidthPx: DEFAULT_LABEL_WIDTH_PIXELS,

  shouldRenderWaveforms: true,

  shouldRenderSpectrograms: true,

  hotKeys: {
    amplitudeScale: 'KeyS',
    amplitudeScaleSingleReset: 'Alt+KeyS',
    amplitudeScaleReset: 'Alt+Shift+KeyS',
    maskCreate: 'KeyM'
  },

  defaultChannel: {
    disableMeasureWindow: false,
    disableSignalDetectionModification: false,
    disablePreditedPhaseModification: false,
    disableMaskModification: false,
  },

  nonDefaultChannel: {
    disableMeasureWindow: false,
    disableSignalDetectionModification: false,
    disablePreditedPhaseModification: false,
    disableMaskModification: false,
  },

  colorScale: undefined
};

/** 
 * Returns the Weavess configuration based on the configuration 
 * passed in by the user and the default configuration
 */
// tslint:disable-next-line:no-unnecessary-callback-wrapper
export const getConfiguration = (
  config: Partial<Configuration> | undefined,
  defaultConfig: Configuration = defaultConfigutation): Configuration =>
   defaultsDeep(config, defaultConfig);
