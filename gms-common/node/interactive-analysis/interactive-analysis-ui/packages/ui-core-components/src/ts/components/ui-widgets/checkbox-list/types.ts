// Assorted types useful for the checkbox list
export interface CheckboxItem {
    id: string;
    name: string;
    checked: boolean;
}

export interface CheckboxListProps {
    items: CheckboxItem[];
    maxHeightPx?: number;
    onCheckboxChecked(id: string, checked: boolean);
}

export interface CheckboxListState {
    currentFilter: string;
}
