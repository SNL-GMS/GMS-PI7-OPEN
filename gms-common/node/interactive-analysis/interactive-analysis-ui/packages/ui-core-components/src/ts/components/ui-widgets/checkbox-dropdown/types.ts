export interface CheckboxDropdownProps {
    checkboxEnum: any;
    enumToCheckedMap: Map<any, boolean>;
    enumToColorMap?: Map<any, string>;
    onChange(value: any);
    // setMaskDisplayFilters(key: string, maskDisplayFilter: MaskDisplayFilter);
}
