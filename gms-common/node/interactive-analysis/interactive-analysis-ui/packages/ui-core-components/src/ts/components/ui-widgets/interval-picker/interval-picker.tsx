/*
 * Accepts a start and end time as input
 * Protects against cases where startTime > endTime
 * Basically two <TimePicker/>'s put in a div with an enter button
 */
import { Button } from '@blueprintjs/core';
import * as React from 'react';
import { TimePicker } from '../';
import '../css/interval-picker.scss';
import { IntervalPickerTypes } from './';
// Length of an hour is milliseconds
const HOUR_IN_MS = 3600000;
export class IntervalPicker
    extends React.Component<IntervalPickerTypes.IntervalPickerProps, IntervalPickerTypes.IntervalPickerState> {

    /*
    A constructor
    */
    private constructor(props) {
        super(props);
        this.state = {
            startDateHold: false,
            endDateHold: false,
        };
    }

    /**
     * React component lifecycle.
     */
    public render() {

        const dateOrderingHold = this.props.startDate >= this.props.endDate;
        return (
            <div
                className={
                    this.props.renderStacked ?
                    'interval-picker--stacked'
                    : 'interval-picker'
                }
            >
                <div
                    className={
                        this.props.renderStacked ?
                        'interval-picker__input_column'
                        : 'interval-picker__input-row'
                    }
                >
                    <div
                        className={
                            this.props.renderStacked ?
                            'interval-picker__input interval-picker__input--stacked'
                            : 'interval-picker__input interval-picker__input--flat'
                        }
                    >
                        <div className="interval-picker__time-picker-label">Start Time:</div>
                        <TimePicker
                            date={this.props.startDate}
                            datePickerEnabled={true}
                            onMaybeDate={this.onMaybeStartDate}
                            setHold={this.onStartHold}
                            hasHold={dateOrderingHold}
                            onEnter={this.onEnter}
                            shortFormat={this.props.shortFormat}
                        />
                    </div>
                    <div
                        className={
                            this.props.renderStacked ?
                            'interval-picker__input'
                            : 'interval-picker__input interval-picker__input--right'
                        }
                    >
                        <div className="interval-picker__time-picker-label">End Time:</div>
                        <TimePicker
                            date={this.props.endDate}
                            datePickerEnabled={true}
                            onMaybeDate={this.onMaybeEndDate}
                            setHold={this.onEndHold}
                            hasHold={dateOrderingHold}
                            onEnter={this.onEnter}
                            shortFormat={this.props.shortFormat}
                        />
                    </div>
                    <div
                        className={
                            this.props.renderStacked ?
                            'interval-picker__enter-button interval-picker__enter-button--stacked'
                            : 'interval-picker__enter-button interval-picker__enter-button--flat'
                        }
                    >
                        <Button
                            onClick={this.onApply}
                        >
                            Apply
                        </Button>
                    </div>
                </div>
            </div>

        );
    }

    private readonly onMaybeStartDate = (maybeDate: Date | undefined) => {
        if (maybeDate) {
            // If a default interval is set, sets the end date so many hours into the future
            if (this.props.defaultIntervalInHours) {
                    const newEndDate =
                        new Date(maybeDate.valueOf() + (HOUR_IN_MS * this.props.defaultIntervalInHours));
                    this.props.onNewInterval(maybeDate, newEndDate);
            } else {
                this.props.onNewInterval(maybeDate, this.props.endDate);

            }
        }
    }

    private readonly onMaybeEndDate = (maybeDate: Date | undefined) => {
        if (maybeDate) {
            this.props.onNewInterval(this.props.startDate, maybeDate);
        }
    }

    private readonly onStartHold = (hold: boolean) => {
        this.setState({...this.state, startDateHold: hold});
    }

    private readonly onEndHold = (hold: boolean) => {
        this.setState({...this.state, endDateHold: hold});

    }
    private readonly onEnter = () => {
        if ((this.props.startDate <= this.props.endDate)) {
                this.onApply();
        }
    }
    private readonly onApply = () => {
            if (this.props.startDate >= this.props.endDate) {
                if (this.props.onInvalidInterval) {
                    this.props.onInvalidInterval('Start Time must be less then End Time');
                }
            } else if (this.props.onApply) {
                this.props.onApply(this.props.startDate, this.props.endDate);
            }
    }
}
