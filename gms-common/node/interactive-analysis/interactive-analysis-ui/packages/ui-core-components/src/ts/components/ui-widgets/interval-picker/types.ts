/**
 * IntervalPicker Props.
 * @param defaultStartDate Initial value shown by startDate
 * @param defaultEndDate Initial value shown by endDate
 * @param shortFormat If true, shortens the time strings
 * @param defaultIntervalInHours 
 * If set, when start time is greater than end time, sets endtime to be n hours in the future
 * @param onNewInterval Callback when a new start/end date is produced
 * @param onInvalidInterval Callback when 'enter' is pressed and dates are invalid
 */
export interface IntervalPickerProps {
    startDate: Date;
    endDate: Date;
    renderStacked?: boolean;
    shortFormat?: boolean;
    defaultIntervalInHours?: number;
    onNewInterval(startDate: Date, endDate: Date);
    onApply?(startDate: Date, endDate: Date);
    onInvalidInterval?(message: string);
}

/**
 * IntervalPicker State
 */
// tslint:disable-next-line:no-empty-interface
export interface IntervalPickerState {
}
