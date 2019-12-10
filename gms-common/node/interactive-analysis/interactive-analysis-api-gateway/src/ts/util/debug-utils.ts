import { FkPowerSpectrum } from '../channel-segment/model-spectra';
import { gatewayLogger } from '../log/gateway-logger';
import { isEqual } from 'lodash';

/**
 * Compare the FstatGrids in each spectrum to the next to see if there
 * are duplicated Grids being returned sequentially.
 * @param fkSpectrums 
 */
export function compareFkSpectrumGrids(fkSpectrums: FkPowerSpectrum[]) {
    // Walk the list comparing when different and printing the index changes
    fkSpectrums.forEach((fkSpectrum, index) => {
        if (fkSpectrums.length - 1 > index + 1) {
            if (isEqual(fkSpectrums[index].fstat, fkSpectrums[index + 1].fstat)) {
                 gatewayLogger.warn(`Duplicate fstat grid found at indices ${index} - ${index + 1}`);
            }
        }
    });
}
