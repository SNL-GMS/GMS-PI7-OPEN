import { WidgetTypes } from '../';

export enum TextFormats {
    Standard = 'Standard',
    Time = 'Time'
}

/** 
 * FormItem, a single row in the form
 * 
 * @param itemKey A unique key that identifies a FormItem.
 * @param labelText Text displayed as a label, by default a colon is appended
 * @param itemType Describes if the item is an input or an output
 * 
 * @param value If item is an input, it describes the defaultValue and valueType
 * @param modified Set internally by form
 * 
 * @param displayText If item is a display, this is what it displays
 * @param displayTextForm The formatting options for the text
 * 
 * @param hideLabelColon If true, there is no colon appended to the label text
 */

export interface FormItem {
    itemKey: string;
    labelText: string;
    itemType: ItemType;

    value?: WidgetTypes.WidgetData;

    displayText?: string;
    displayTextFormat?: TextFormats;

    hideLabelColon?: boolean;
}
/** 
 * Describes whether an item is a display our an input
 */

export enum ItemType {
    Display = 'Display',
    Input = 'Input',
}
/** 
 * A panel (display) within Form
 * 
 * @param formItems An optional list of form label/values to display
 * @param content An arbitrary chunk of html to display
 * @param key The name and key of the display
 */

export interface FormPanel {
    formItems?: FormItem[];
    content?: JSX.Element;
    key: string;
}

/**
 * The state of the FormItems as tracked by Form
 * 
 * @param modified Whether an item has been modified
 * @param hasHold True if input is not valid
 * @param value The latest valid input
 */
export interface FormItemState {
    modified: boolean;
    hasHold: boolean;
    value: any;
}
