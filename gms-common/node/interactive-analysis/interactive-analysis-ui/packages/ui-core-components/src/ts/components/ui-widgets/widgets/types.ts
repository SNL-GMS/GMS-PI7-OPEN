export interface WidgetProps {
    type: WidgetInputType;
    defaultValue: any;
    params?: any;
    isValid?: boolean;
    onMaybeValue(maybeValue: any | undefined);
    onValidStatus?(isValid: boolean);
}
export interface WidgetState {
    isValid: boolean;
    value: any;
}
export interface WidgetData {
    type: WidgetInputType;
    defaultValue: any;
    params?: any;
}
export enum WidgetInputType {
    DropDown = 'DropDown',
    TimePicker = 'TimePicker',
    IntervalPicker = 'IntervalPicker',
    TextArea = 'TextArea',
    FilterableOptionList = 'FilterableOptionList'
}
export interface FilterableOptionListParams {
    options: string[];
    priorityOptions?: string[];
    defaultFilter?: string;
}
export interface DropDownParams {
    dropDownItems: any;
}
export interface TimePickerParams {
    maxValueMs: number;
    minValueMs: number;
}
