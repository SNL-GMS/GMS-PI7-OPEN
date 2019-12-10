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
 * Creates a context menu for marking intervals
 * 
 * @param markInterval A function which can make a graphql call to update the workflow
 */
export const ActivityIntervalBlueprintContextMenu = (
  markInterval: IntervalMarker
): JSX.Element => (
    <Menu>
      <MenuItem
        className={'menu-item-open-activity-interval'}
        text="Open Activity"
        onClick={() => markInterval(WorkflowTypes.IntervalStatus.InProgress)}
      />
      <MenuItem
        className={'menu-item-mark-activity-interval'}
        text="Mark Activity Complete"
        onClick={() => markInterval(WorkflowTypes.IntervalStatus.Complete)}
      />
      <MenuItem
        className={'menu-item-unmark-activity-interval'}
        text="Mark Activity Not Complete"
        onClick={() => markInterval(WorkflowTypes.IntervalStatus.NotComplete)}
      />
      <MenuDivider />
      <MenuItem
        className={'menu-item-subdivide-activity-interval'}
        text="Subdivide Activity..."
        disabled={true}
      />
      <MenuItem
        className={'menu-item-add-note-activity-interval'}
        text="Add Note..."
        disabled={true}
      />
    </Menu>
  );
