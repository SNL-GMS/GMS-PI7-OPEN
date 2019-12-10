import { IconName } from '@blueprintjs/core';
export interface ToolbarProps {
    items: ToolbarItem[];
    toolbarWidthPx: number;
    itemsLeft?: ToolbarItem[];
    minWhiteSpacePx?: number;
    spaceBetweenItemsPx?: number;
    hidden?: boolean;
    overflowIcon?: IconName;
}
export interface ToolbarState {
    indicesToOverflow: number[];
    leftIndicesToOverlow: number[];
    whiteSpaceAllotmentPx: number;
    checkSizeOnNextDidMountOrDidUpdate: boolean;
}
export interface ToolbarItem {
    // Required for all items
    label: string;
    tooltip: string;
    type: ToolbarItemType;
    rank: number;
    // Required for most items
    value?: any;
    // Required for Popover's
    popoverContent?: JSX.Element;
    // Required for NumericInput's
    minMax?: MinMax;
    step?: number;
    requireEnterForOnChange?: boolean;
    // Required for DropDown's
    dropdownOptions?: any;
    // Required for ButtonGroup
    buttons?: ToolbarItem[];
    // Optional for interval picker
    shortFormat?: boolean;
    defaultIntervalInHours?: number;
    // Optional for checkbox dropdown
    valueToColorMap?: Map<any, string>;
    // Optional for label-value
    valueColor?: string;
    // Optional for all
    widthPx?: number;
    labelRight?: string;
    disabled?: boolean;
    icon?: IconName;
    onlyShowIcon?: boolean;
    menuLabel?: string;
    // Mandatory for all
    onChange?(value: any);
    onApply?(startDate: Date, endDate: Date);
}
export interface MinMax {
    min: number;
    max: number;
}
export enum ToolbarItemType {
    Switch = 'Switch',
    Popover = 'Popover',
    Dropdown = 'Dropdown',
    NumericInput = 'NumericInput',
    Button = 'Button',
    IntervalPicker = 'IntervalPicker',
    ButtonGroup = 'ButtonGroup',
    LabelValue = 'LabelValue',
    CheckboxDropdown = 'CheckoxDropdown',
    LoadingSpinner = 'LoadingSpinner',
}
