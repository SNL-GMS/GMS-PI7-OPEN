import {
  Acquisition,
  PkiInUse,
  PkiStatus,
  PopoverEdcRow,
  ProcessingPartition,
  StoreOnAcquisitionPartition
} from '../types';

// TODO: Some data for the status-configuration component is being randomly generated
// TODO: here in the UI.
// TODO: This is not a permanent solution.
// TODO: Repeated code for random number generation in each function is intentional;
// TODO: using a global random value for all generation functions will result in
// TODO: uniform values across columns

/**
 * Generate data for the Acquisition column.
 */
export function generateAcquisition(): Acquisition {
  const min = 1;
  const max = 5;
  const base = 10;
  // tslint:disable-next-line:newline-per-chained-call
  const randomNumber = parseInt(Math.floor(Math.random() * (max - min + 1) + min).toFixed(), base);
  return randomNumber % 2 ? Acquisition.ACQUIRE : Acquisition.DONT_ACQUIRE;
}

/**
 * Generate data for the PkiStatus column.
 */
export function generatePkiStatus(): PkiStatus {
  const min = 1;
  const max = 5;
  const base = 10;
  const bound = 4;
  // tslint:disable-next-line:newline-per-chained-call
  const randomNumber = parseInt(Math.floor(Math.random() * (max - min + 1) + min).toFixed(), base);
  let pkiStatusValue;
  if (randomNumber === bound) {
    pkiStatusValue = PkiStatus.EXPIRED;
  } else if (randomNumber > bound) {
    pkiStatusValue = PkiStatus.NEARING_EXPIRATION;
  } else randomNumber % 2 ? pkiStatusValue = PkiStatus.INSTALLED : pkiStatusValue = PkiStatus.NOT_INSTALLED;
  return pkiStatusValue;
}

/**
 * Generate data for the PkiInUse column.
 */
export function generatePkiInUse(): PkiInUse {
  const min = 1;
  const max = 5;
  const base = 10;
  // tslint:disable-next-line:newline-per-chained-call
  const randomNumber = parseInt(Math.floor(Math.random() * (max - min + 1) + min).toFixed(), base);
  return randomNumber % 2 ? PkiInUse.ENABLED : PkiInUse.DISABLED;
}

/**
 * Generate data for the ProcessingPartition column.
 */
export function generateProcessingPartition(): ProcessingPartition {
  const min = 1;
  const max = 5;
  const base = 10;
  // tslint:disable-next-line:newline-per-chained-call
  const randomNumber = parseInt(Math.floor(Math.random() * (max - min + 1) + min).toFixed(), base);
  return randomNumber % 2 ? ProcessingPartition.UPLOAD : ProcessingPartition.DONT_UPLOAD;
}

/**
 * Generate data for the StoreOnAcquisitionPartition column.
 */
export function generateStoreOnAcquisitionPartition(): StoreOnAcquisitionPartition {
  const min = 1;
  const max = 5;
  const base = 10;
  // tslint:disable-next-line:newline-per-chained-call
  const randomNumber = parseInt(Math.floor(Math.random() * (max - min + 1) + min).toFixed(), base);
  return randomNumber % 2 ? StoreOnAcquisitionPartition.STORE : StoreOnAcquisitionPartition.DONT_STORE;
}

/**
 * Generate data for the externalDataCenter checkboxes.
 * 
 * @return boolean indicating whether it's checked or not
 */
export function generateDataCenter(): boolean {
  // tslint:disable-next-line: no-magic-numbers
  const rand = Math.random() < 0.5;
  return rand;
}

/**
 * Generate data for the edc table rows in the popover
 */
export function generatePopoverEdcTableData(): PopoverEdcRow[] {
  const dataCenters = [
    {
      id: 'edcA',
      name: 'EDC A'
    },
    {
      id: 'edcB',
      name: 'EDC B'
    },
    {
      id: 'edcC',
      name: 'EDC C'
    }
  ];
  const dataCenterRows = [];
  dataCenters.forEach(dc => {
    const dataCenterRow: PopoverEdcRow = {
      id: dc.id,
      dataCenter: dc.name,
      // tslint:disable-next-line: no-magic-numbers
      enabled: Math.random() < 0.5
    };
    dataCenterRows.push(dataCenterRow);
  });
  return dataCenterRows;
}
