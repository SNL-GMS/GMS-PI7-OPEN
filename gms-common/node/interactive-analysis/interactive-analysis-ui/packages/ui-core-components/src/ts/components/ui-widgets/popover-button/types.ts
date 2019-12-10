import { IconName } from '@blueprintjs/core';

export interface PopoverProps {
  label: string;
  popupContent: JSX.Element;
  renderAsMenuItem?: boolean;
  disabled?: boolean;
  tooltip: string;
  widthPx?: number;
  onlyShowIcon?: boolean;
  icon?: IconName;
  onPopoverDismissed();
}
export interface PopoverState {
  isExpanded: boolean;
}
