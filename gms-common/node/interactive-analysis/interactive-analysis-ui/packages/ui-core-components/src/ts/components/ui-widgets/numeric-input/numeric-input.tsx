import * as React from 'react';
import { NumericInputProps, NumericInputState } from './types';

const ENTER_KEY = 13;

// A numeric input for toolbar
export class NumericInput extends React.Component<NumericInputProps, NumericInputState> {

    private constructor(props) {
        super(props);
        this.state = {
            intermediateValue: props.value
        };
    }

    public componentDidUpdate(prevProps: NumericInputProps) {
        if (prevProps.value !== this.props.value) {
            this.setState({intermediateValue: this.props.value});
        }
    }
    public render() {
        return (
            <input
                className="toolbar-numeric-input"
                onMouseEnter={e => {
                    e.currentTarget.focus();
                }}
                min={this.props.minMax ? this.props.minMax.min : 0}
                max={this.props.minMax ? this.props.minMax.max : 255}
                step={this.props.step ? this.props.step : 1}
                title={this.props.tooltip}
                disabled={this.props.disabled}
                value={this.state.intermediateValue}
                type="number"
                onChange={event => {
                    if (isNaN(event.currentTarget.valueAsNumber)) {
                        return;
                    }
                    this.setState({intermediateValue: event.currentTarget.valueAsNumber});
                    if (!this.props.requireEnterForOnChange) {
                        this.props.onChange(event.currentTarget.valueAsNumber);
                    }
                }}
                onKeyDown={event => {
                    if (this.props.requireEnterForOnChange && event.keyCode === ENTER_KEY) {
                        event.stopPropagation();
                        if (!isNaN(event.currentTarget.valueAsNumber)) {
                            this.props.onChange(this.state.intermediateValue);
                        }
                    }
                }}
                width={this.props.widthPx}
            />
        );
    }

}
