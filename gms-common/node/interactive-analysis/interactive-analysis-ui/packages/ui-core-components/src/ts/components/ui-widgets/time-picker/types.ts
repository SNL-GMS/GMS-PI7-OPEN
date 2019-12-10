/**
 * TimePicker Props.
 */
export interface TimePickerProps {
    date: Date;
    // If false, the date picker widget button will not be displayed
    datePickerEnabled: boolean;
    // use a format that doesn have minutes and second
    shortFormat?: boolean;
    // If true, the date picker is turned red - neccesary so hold persists after re-render
    hasHold?: boolean;
    // Callback fired when input is accepted, returns undefined if provided date isn't valid
    onMaybeDate(date: Date | undefined): void;
    // Callback fired when an invalid input is entered
    setHold?(onHold: boolean): void;
    // Optional callback
    onEnter?(): void;
}

/**
 * TimePicker State
 */
// tslint:disable-next-line:no-empty-interface
export interface TimePickerState {
    isValid: boolean;
    // Whether or not the date picker widget from blueprint is displayed
    showDatePicker: boolean;
    // The currently dislayed string in the date picker - not always a stringified date
    displayString: string;
    // If true, the picker will turn red and not accept input
    hasHold: boolean;
    // If true, show the date picker below the element
    datePickerOnBottom: boolean;
}
