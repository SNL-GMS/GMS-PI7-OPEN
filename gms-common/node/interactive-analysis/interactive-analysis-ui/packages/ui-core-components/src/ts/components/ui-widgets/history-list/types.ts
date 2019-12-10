
/**
 * @param index number used to sort items
 * @param label the displayed text for the entry
 * @param id a unique identifier for the history entry
 */
export interface HistoryListItem {
    index: number;
    label: any;
    id: string;
}
/**
 * @param items list of items to render
 * @param listLength optional, defaults to seven
 * @param onSelect callback when an entry is selected
 */
export interface HistoryListProps {
    items: HistoryListItem[];
    preferredItems?: HistoryListItem[];
    listLength?: number;

    onSelect(id: string);
}
