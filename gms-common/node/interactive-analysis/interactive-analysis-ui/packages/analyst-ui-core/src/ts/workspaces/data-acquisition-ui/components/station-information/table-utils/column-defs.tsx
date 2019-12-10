import { Column } from '@gms/ui-core-components';
import {
  AutomaticProcessingDropdown,
  DataAcquisitionDropdown,
  InteractiveProcessingDropdown,
  StationInformationModifiedDot
} from './cell-renderer-frameworks';

/**
 * Column Definitions for Station Information List
 */
export const columnDefs: Column[] =
  [{
    headerName: '',
    field: 'modified',
    cellStyle: { display: 'flex', 'justify-content': 'center', 'align-items': 'center' },
    enableCellChangeFlash: true,
    width: 20,
    cellRendererFramework: StationInformationModifiedDot,
    resizable: true,
    sortable: true,
    filter: true,
  },
  {
    headerName: 'Station',
    field: 'station',
    cellStyle: { 'text-align': 'left' },
    enableCellChangeFlash: true,
    cellRenderer: 'agGroupCellRenderer',
    editable: false,
    width: 100,
    resizable: true,
    sortable: true,
    filter: true
  }, {
    headerName: 'Data Acquisition',
    cellStyle: { 'text-align': 'left' },
    width: 200,
    field: 'dataAcquisition',
    enableCellChangeFlash: true,
    cellRendererFramework: DataAcquisitionDropdown,
    resizable: true,
    sortable: true,
    filter: true
  }, {
    headerName: 'Interactive Processing',
    field: 'interactiveProcessing',
    cellStyle: { 'text-align': 'left' },
    width: 300,
    enableCellChangeFlash: true,
    cellRendererFramework: InteractiveProcessingDropdown,
    resizable: true,
    sortable: true,
    filter: true
  }, {
    headerName: 'Automatic Processing',
    field: 'automaticProcessing',
    width: 300,
    enableCellChangeFlash: true,
    cellStyle: { 'text-align': 'left' },
    cellRendererFramework: AutomaticProcessingDropdown,
    resizable: true,
    sortable: true,
    filter: true
  }
  ];

export const popoverTableColumnDefs: Column[] =
  [{
    headerName: 'Stations',
    field: 'stationName',
    cellStyle: { 'text-align': 'left' },
    width: 100,
    resizable: true,
    sortable: true,
    filter: true,
  }
  ];
