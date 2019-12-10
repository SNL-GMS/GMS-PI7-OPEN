import { Checkbox, Icon, Tooltip } from '@blueprintjs/core';
import { IconNames } from '@blueprintjs/icons';
import * as React from 'react';
import { DefiningTypes } from '~analyst-ui/components/location/components/location-signal-detections/types';

/**
 * When the user changes the checkbox by calling the location SD table's component
 */

export function onCheckboxChange(
  definingType: DefiningTypes, signalDetectionId: string, setDefining: boolean, props: any) {
  props.data.updateIsDefining(definingType, signalDetectionId, setDefining);
}

export const DefiningCheckBoxCellRenderer: React.StatelessComponent<any> = props => {
  const definingType = props.colDef.cellRendererParams.definingType;
  let isDefining = props.data.arrivalTimeDefining;
  if (definingType === DefiningTypes.SLOWNESS) {
    isDefining = props.data.slownessDefining;
  } else if (definingType === DefiningTypes.AZIMUTH) {
    isDefining = props.data.azimuthDefining;
  }
  return (
    <Checkbox
      label=""
      checked={isDefining}
      disabled={props.data.historicalMode || props.data.rejectedOrUnnassociated}
      onChange={() => {onCheckboxChange(definingType, props.data.signalDetectionId, !isDefining, props); }}
    />
  );
};

/**
 * Renders the modified color cell for the signal detection list
 */
export const AddedRemovedSDMarker: React.StatelessComponent<any> = props => {
  if (!props.data.rejectedOrUnnassociated && !props.data.isAssociatedDiff) {
    return null;
  }
  const tooltip = props.data.rejectedOrUnnassociated ?
    'SD Rejected or Unnasociated since last locate'
    : 'SD Associated or Created since last locate';
  return (
    <Tooltip
      content={(
        <div>
          {tooltip}
        </div>
      )}
      className={'dirty-dot-wrapper'}
    >
      <Icon
        icon={props.data.rejectedOrUnnassociated ? IconNames.GRAPH_REMOVE : IconNames.NEW_OBJECT}
      />
    </Tooltip>
  );
};
