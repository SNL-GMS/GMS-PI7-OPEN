export interface DropDownProps {
    value: string;
    dropDownItems: any;
    widthPx?: number;
    disabled?: boolean;
    title?: string;
    onMaybeValue(value: any);
}
