import { Menu, MenuDivider, MenuItem } from '@blueprintjs/core';
import * as React from 'react';
import { WorkflowTypes } from '~graphql/';

/**
 * function which modifies an interval status
 */
export interface IntervalMarker {
  (status: WorkflowTypes.IntervalStatus): void;
}
/**
 * Context menu for marking a stage interval
 * 
 * @param markInterval callback function which can execute over-network logic for markign stage interval
 */
export const StageIntervalBlueprintContextMenu = (
  markInterval: IntervalMarker
): JSX.Element => (
    <Menu>
      <MenuItem
        className={'menu-item-mark-stage-interval'}
        text="Mark Stage Interval Complete"
        onClick={() => markInterval(WorkflowTypes.IntervalStatus.Complete)}
      />
      <MenuDivider />
      <MenuItem text="Add Note..." disabled={true} />
    </Menu>
  );
