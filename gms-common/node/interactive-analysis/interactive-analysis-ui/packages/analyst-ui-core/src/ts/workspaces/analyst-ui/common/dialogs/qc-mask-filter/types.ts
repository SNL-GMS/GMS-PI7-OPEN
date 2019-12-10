import { QcMaskDisplayFilters } from '~analyst-ui/config';
import { MaskDisplayFilter } from '~analyst-ui/config/user-preferences';

/**
 * Waveform Display Controls Props
 */
export interface QcMaskFilterProps {
  maskDisplayFilters: QcMaskDisplayFilters;
  setMaskDisplayFilters(key: string, maskDisplayFilter: MaskDisplayFilter);
}
