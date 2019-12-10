import { Column } from '@gms/ui-core-components';
import { analystUiConfig } from '~analyst-ui/config';
import {
  AcquisitionDropdown,
  EdcACheckbox,
  EdcBCheckbox,
  EdcCCheckbox,
  PkiInUseDropdown,
  PopoverEdcCheckbox,
  ProcessingPartitionDropdown,
  StatusConfigurationModifiedDot,
  StoreOnAcquisitionPartitionDropdown
} from './cell-renderer-frameworks';

/**
 * Column Definitions for Station Information List
 */
export const columnDefs: Column[] =
  [
    {
      headerName: '',
      field: 'modified',
      cellStyle: { display: 'flex', 'justify-content': 'center', 'align-items': 'center' },
      enableCellChangeFlash: true,
      width: 20,
      cellRendererFramework: StatusConfigurationModifiedDot,
      resizable: true,
      sortable: true,
      filter: true,
    },
    {
      headerName: 'Station',
      field: 'station',
      cellStyle: { 'text-align': 'left' },
      enableCellChangeFlash: true,
      width: 100,
      resizable: true,
      sortable: true,
      filter: true
    },
    {
      headerName: 'Acquisition',
      cellStyle: { 'text-align': 'left' },
      width: 200,
      field: 'acquisition',
      enableCellChangeFlash: true,
      resizable: true,
      sortable: true,
      filter: true,
      cellRendererFramework: AcquisitionDropdown
    },
    {
      headerName: 'PKI status',
      field: 'pkiStatus',
      width: 200,
      resizable: true,
      sortable: true,
      filter: true,
      cellStyle(params) {
        let background = '';
        let textColor = '';
        const status = params.value;
        if (status === 'Nearing expiration') {
          background = analystUiConfig.userPreferences.dataAcquisition.pkiWarning;
          textColor = 'black';
        }
        if (status === 'Expired') {
          background = analystUiConfig.userPreferences.dataAcquisition.pkiStrongWarning;
          textColor = 'white';
        }
        return {
          'background-color': background,
          color: textColor,
          'text-align': 'left'
        };
      },
    },
    {
      headerName: 'PKI in use',
      field: 'pkiInUse',
      enableCellChangeFlash: true,
      width: 200,
      resizable: true,
      sortable: true,
      filter: true,
      cellRendererFramework: PkiInUseDropdown
    },
    {
      headerName: 'Processing partition',
      field: 'processingPartition',
      cellStyle: { 'text-align': 'left' },
      width: 200,
      enableCellChangeFlash: true,
      resizable: true,
      sortable: true,
      filter: true,
      cellRendererFramework: ProcessingPartitionDropdown
    }, {
      headerName: 'Store on acquisition partition',
      field: 'storeOnAcquisitionPartition',
      cellStyle: { 'text-align': 'left' },
      width: 200,
      enableCellChangeFlash: true,
      resizable: true,
      sortable: true,
      filter: true,
      cellRendererFramework: StoreOnAcquisitionPartitionDropdown
    },
    {
      headerName: 'EDC A',
      field: 'edcA',
      cellStyle: { 'text-align': 'center' },
      width: 75,
      resizable: true,
      sortable: true,
      filter: true,
      cellRendererFramework: EdcACheckbox
    }, {
      headerName: 'EDC B',
      field: 'edcB',
      cellStyle: { 'text-align': 'center' },
      width: 75,
      resizable: true,
      sortable: true,
      filter: true,
      cellRendererFramework: EdcBCheckbox
    }, {
      headerName: 'EDC C',
      field: 'edcC',
      cellStyle: { 'text-align': 'center' },
      width: 75,
      resizable: true,
      sortable: true,
      filter: true,
      cellRendererFramework: EdcCCheckbox
    }
  ];

export const popoverColumnDefs: Column[] =
  [
    {
      headerName: 'Data Center',
      field: 'dataCenter',
      cellStyle: { 'text-align': 'left' },
      width: 100,
      resizable: true,
      sortable: true,
      filter: true,

    },
    {
      headerName: 'Enabled',
      cellStyle: { 'text-align': 'left' },
      width: 200,
      field: 'enabled',
      resizable: true,
      sortable: true,
      filter: true,
      cellRendererFramework: PopoverEdcCheckbox,
    },
  ];
