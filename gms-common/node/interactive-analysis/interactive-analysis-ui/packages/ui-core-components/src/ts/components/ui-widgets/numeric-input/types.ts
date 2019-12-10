import { MinMax } from '../toolbar/types';

// Types for Loading Spinner
export interface NumericInputProps {
    value: number;
    tooltip: string;
    widthPx?: number;
    disabled?: boolean;
    minMax?: MinMax;
    step?: number;
    requireEnterForOnChange?: boolean;
    onChange(val: number): void;

}
// Types for Loading Spinner
export interface NumericInputState {
    intermediateValue: number;
}
