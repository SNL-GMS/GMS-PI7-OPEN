export interface FilterableOptionListProps {
    options: string[];
    prioriotyOptions?: string[];
    defaultSelection?: string;
    defaultFilter?: string;
    disabled?: boolean;
    widthPx?: number;
    onSelection(itemSelected: string);
    onEnter?(currentlySelected: string);
    onClick?(itemClicked: string);
    onDoubleClick?(itemClicked: string);
}

export interface FilterableOptionListState {
  currentlySelected: string;
  currentFilter: string;
}
