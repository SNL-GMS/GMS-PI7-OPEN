import { Switch } from '@blueprintjs/core';
import * as React from 'react';
/**
 * stateless checkbox used in the LocationHistory table
 */
// TODO move to core components
export const LocationHistoryCheckBox: React.StatelessComponent<any> = props =>
  (
    <Switch
      checked={props.data.preferred}
      onChange={() => {
        props.data.setPreferred(props.data.locationSolutionId, props.data.locationSetId);
      }}
    />
);

export const LocationSetSwitch: React.StatelessComponent<any> = props =>
  props.data.isFirstInLSSet ?
    (
      <Switch
        checked={props.data.isLocationSolutionSetPreferred}
        large={true}
        onChange={() => {
          props.data.setToSave(props.data.locationSolutionId, props.data.locationSetId);
        }}
      />
    )
    : null
;
