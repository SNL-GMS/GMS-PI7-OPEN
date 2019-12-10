// Types for Loading Spinner
export interface LoadingSpinnerProps {
    // How many things are being loading
    value: {
        itemsToLoad: number;
        // If not provided, the spinner will spin and the number of requested items will be displayed
        itemsLoaded?: number;
        hideTheWordLoading?: boolean;
        hideOutstandingCount?: boolean;
    };
    // String to display next to loading mumber
    label: string;
    widthPx?: number;
}
