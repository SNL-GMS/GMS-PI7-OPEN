import { ColumnGroup } from '@gms/ui-core-components';
import { DefiningTypes,
  SdDefiningStates } from '~analyst-ui/components/location/components/location-signal-detections/types';
import { userPreferences } from '~analyst-ui/config';
import { getDiffStyleForDefining } from '../../location-sd-row-util';
import { AddedRemovedSDMarker, DefiningCheckBoxCellRenderer } from './cell-renderer-frameworks';
import { DefiningHeader } from './header-group-component';

export type DefiningCallback = (isDefining: boolean, definingType: DefiningTypes) => void;
/**
 * Generates column defs
 * @param definingCallback callback to set all sd's defining status
 */
export function generateLocationSDColumnDef(definingCallback: DefiningCallback,
  timeDefiningState: SdDefiningStates,
  aziumthDefiningState: SdDefiningStates, slownessDefiningState: SdDefiningStates, historicalMode: boolean) {
  const columnDefs: ColumnGroup[] =
  [
    {
      headerName: historicalMode ?
        'Associated Signal Detections (readonly)' : 'Associated Signal Detections',
      children: [
        {
          headerName: '',
          field: 'modified',
          cellStyle: (params: any) => ({
             display: 'flex',
             'justify-content': 'center',
             'align-items': 'center'
            }),
          width: 20,
          resizable: true,
          sortable: true,
          filter: true,
          cellRendererFramework: AddedRemovedSDMarker
        },
        {
          headerName: 'Station',
          field: 'station',
          width: 70,
          cellStyle: (params: any) => ({
            'text-align': 'left',
            color: historicalMode ? userPreferences.location.historicalModeTableColor : undefined
           }),
          resizable: true,
          sortable: true,
          filter: true,
          suppressMovable: true
        },
        {
          headerName: 'Chan',
          field: 'channel',
          width: 70,
          cellStyle: (params: any) => ({
            'text-align': 'left',
            'background-color':
              params.data.channelNameDiff ?
              userPreferences.location.changedSdHighlight : '',
            color: historicalMode ? userPreferences.location.historicalModeTableColor : undefined
           }),
          resizable: true,
          sortable: true,
          filter: true,
          suppressMovable: true
        },
        {
          headerName: 'Phase',
          field: 'phase',
          width: 70,
          cellStyle: (params: any) => ({
            'text-align': 'left',
            'background-color':
              params.data.phaseDiff ?
              userPreferences.location.changedSdHighlight : '',
            color: historicalMode ? userPreferences.location.historicalModeTableColor : undefined
           }),
          resizable: true,
          sortable: true,
          filter: true,
          suppressMovable: true

        },
        {
          headerName: 'Dist (\u00B0)',
          field: 'distance',
          width: 70,
          cellStyle: (params: any) => (
            {
              'text-align': 'right',
              'border-right':
              '1px white dotted',
              color: historicalMode ? userPreferences.location.historicalModeTableColor : undefined
            }
            ),
          resizable: true,
          sortable: true,
          filter: true,
          suppressMovable: true
        }
      ]
    },
    {
      headerName: 'Time',
      headerGroupComponentFramework: DefiningHeader,
      headerGroupComponentParams: {
        definingCallback,
        definingType: DefiningTypes.ARRIVAL_TIME,
        definingState: timeDefiningState
      },
      children: [
        {
          headerName: 'Def',
          field: 'timeDefnCB',
          width: 50,
          resizable: true,
          sortable: true,
          filter: true,
          suppressMovable: true,
          cellRendererFramework: DefiningCheckBoxCellRenderer,
          cellStyle: (params: any) =>
            ({
                display: 'flex',
                'justify-content': 'center',
                'padding-left': '9px',
                'padding-top': '3px',
                ...getDiffStyleForDefining(DefiningTypes.ARRIVAL_TIME, params.data)
            }),
          cellRendererParams: {
            padding: 25,
            definingType: DefiningTypes.ARRIVAL_TIME
          }
        },
        {
          headerName: 'Obs',
          field: 'timeObs',
          width: 210,
          cellStyle: (params: any) => ({
            'text-align': 'right',
            'background-color':
              params.data.arrivalTimeDiff ?
              userPreferences.location.changedSdHighlight : '',
            color: historicalMode ? userPreferences.location.historicalModeTableColor : undefined
           }),
          resizable: true,
          sortable: true,
          filter: true,
          suppressMovable: true

        },
        {
          headerName: 'Res (s)',
          field: 'timeRes',
          width: 70,
          cellStyle: (params: any) => ({
            'text-align': 'right',
            color: historicalMode ? userPreferences.location.historicalModeTableColor : undefined
           }),
          resizable: true,
          sortable: true,
          filter: true,
          suppressMovable: true

        },
        {
          headerName: 'Corr (s)',
          field: 'timeCorr',
          width: 70,
          cellStyle: (params: any) => ({
            'text-align': 'right',
            'border-right': '1px dotted white',
            color: historicalMode ? userPreferences.location.historicalModeTableColor : undefined
           }),
          resizable: true,
          sortable: true,
          filter: true,
          suppressMovable: true
        }
      ]
    },
    {
      headerName: 'Slowness',
      headerGroupComponentFramework: DefiningHeader,
      headerGroupComponentParams: {
        definingCallback,
        definingType: DefiningTypes.SLOWNESS,
        definingState: slownessDefiningState
      },
      children: [
        {
          headerName: 'Def',
          field: 'slownessDefnCB',
          width: 50,
          resizable: true,
          sortable: true,
          filter: true,
          suppressMovable: true,
          cellStyle: (params: any) =>
          ({
              display: 'flex',
              'justify-content': 'center',
              'padding-left': '9px',
              'padding-top': '3px',
              ...getDiffStyleForDefining(DefiningTypes.SLOWNESS, params.data)
          }),
          cellRendererFramework: DefiningCheckBoxCellRenderer,
          cellRendererParams: {
            padding: 25,
            definingType: DefiningTypes.SLOWNESS
          }
        },
        {
          headerName: 'Obs (s/\u00B0)',
          field: 'slownessObs',
          width: 75,
          cellStyle: (params: any) => ({
            'text-align': 'right',
            'background-color':
              params.data.slownessObsDiff ?
              userPreferences.location.changedSdHighlight : '',
            color: historicalMode ? userPreferences.location.historicalModeTableColor : undefined
           }),
          resizable: true,
          sortable: true,
          filter: true,
          suppressMovable: true

        },
        {
          headerName: 'Res (s/\u00B0)',
          field: 'slownessRes',
          width: 70,
          cellStyle: (params: any) => ({
            'text-align': 'right',
            color: historicalMode ? userPreferences.location.historicalModeTableColor : undefined
           }),
          resizable: true,
          sortable: true,
          filter: true,
          suppressMovable: true
        },
        {
          headerName: 'Corr (s/\u00B0)',
          field: 'slownessCorr',
          width: 75,
          cellStyle: (params: any) => ({
            'text-align': 'right',
            color: historicalMode ? userPreferences.location.historicalModeTableColor : undefined,
            'border-right': '1px white dotted'
           }),
          resizable: true,
          sortable: true,
          filter: true,
          suppressMovable: true
        }
      ]
    },
    {
      headerName: 'Azimuth',
      headerGroupComponentFramework: DefiningHeader,
      headerGroupComponentParams: {
        definingCallback,
        definingType: DefiningTypes.AZIMUTH,
        definingState: aziumthDefiningState
      },
      children: [
        {
          headerName: 'Def',
          field: 'azimuthDefnCB',
          width: 50,
          resizable: true,
          sortable: true,
          filter: true,
          suppressMovable: true,
          cellStyle: (params: any) =>
          ({
              display: 'flex',
              'justify-content': 'center',
              'padding-left': '9px',
              'padding-top': '3px',
              ...getDiffStyleForDefining(DefiningTypes.AZIMUTH, params.data)
          }),
          cellRendererFramework: DefiningCheckBoxCellRenderer,
          cellRendererParams: {
            padding: 25,
            definingType: DefiningTypes.AZIMUTH
          }
        },
        {
          headerName: 'Obs (\u00B0)',
          field: 'azimuthObs',
          width: 70,
          cellStyle: (params: any) => ({
            'text-align': 'right',
            'background-color':
            params.data.azimuthObsDiff ?
            userPreferences.location.changedSdHighlight : '',
            color: historicalMode ? userPreferences.location.historicalModeTableColor : undefined
           }),
          resizable: true,
          sortable: true,
          filter: true,
          suppressMovable: true
        },
        {
          headerName: 'Res (\u00B0)',
          field: 'azimuthRes',
          width: 70,
          cellStyle: (params: any) => ({
            'text-align': 'right',
            color: historicalMode ? userPreferences.location.historicalModeTableColor : undefined
           }),
          resizable: true,
          sortable: true,
          filter: true,
          suppressMovable: true
        },
        {
          headerName: 'Corr (\u00B0)',
          field: 'azimuthCorr',
          width: 70,
          cellStyle: (params: any) => ({
            'text-align': 'right',
            color: historicalMode ? userPreferences.location.historicalModeTableColor : undefined,
            'border-right': '1px white dotted'
           }),
          resizable: true,
          sortable: true,
          filter: true,
          suppressMovable: true
        }
      ]
    }
  ];
  return columnDefs;
}
