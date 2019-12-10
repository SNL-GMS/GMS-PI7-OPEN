/*
    Reusable component for picking dates and times
*/

import { Button } from '@blueprintjs/core';
import { DatePicker } from '@blueprintjs/datetime';
import * as React from 'react';
import { TimeUtil } from '../../../utils';
import '../css/time-picker.scss';
import { TimePickerTypes } from './';

/**
 * TimePicker component that lets you enter time in ISO format.
 */

class TimePicker extends React.Component<TimePickerTypes.TimePickerProps, TimePickerTypes.TimePickerState> {

    private timePickerRef: HTMLDivElement;
    /*
    A constructor
    */
    private constructor(props) {
        super(props);
        this.state = {
            isValid: true,
            showDatePicker: false,
            displayString: this.props.shortFormat ?
                TimeUtil.dateToShortISOString(this.props.date)
                : TimeUtil.dateToISOString(this.props.date),
            hasHold: this.props.hasHold ? this.props.hasHold : false,
            datePickerOnBottom: false
        };
    }
    /**
     * React component lifecycle.
     */
    public render() {
        return (
            <div
                className="time-picker"
                ref={ref => { if (ref !== null) {
                    this.timePickerRef = ref;
                    }
                }}
            >
                <textarea
                    value={this.state.displayString}
                    className={
                        this.state.isValid && !this.props.hasHold ?
                        'time-picker__input'
                        : 'time-picker__input--invalid'
                    }
                    style={{
                        width: this.props.shortFormat ? '152px' : '240px'
                    }}
                    // tslint:disable-next-line:no-magic-numbers
                    cols={27}
                    rows={1}
                    // When focus leaves element, unsets holds and widget will display last valid date entered
                    onBlur={
                        e => {
                            e.stopPropagation();
                            this.setState({
                                ...this.state,
                                displayString: this.props.shortFormat ?
                                    TimeUtil.dateToShortISOString(this.props.date)
                                    : TimeUtil.dateToISOString(this.props.date),
                                isValid: true});

                            const timeout = 200;
                            if (this.props.setHold) {
                                setTimeout(() => {
                                    if (this.props.setHold) {
                                        this.props.setHold(false);
                                    }
                                },         timeout);
                            }
                        }
                    }
                    onKeyDown={
                        e => {
                            // tslint:disable-next-line:no-magic-numbers
                            if (e.nativeEvent.code === 'Escape') {
                                if (this.props.onEnter) {
                                    this.props.onEnter();
                                }
                                e.preventDefault();
                            }
                        }
                    }
                    onChange={
                        e => {
                            // Attempts to create new date from parsed string
                            // const regex = new RegExp('\d{4}\/\d{2}\/\d{2}T\d{2}:\d{2}:\d{2}\.\d{6}', 'g');
                            const regex = this.props.shortFormat ?
                                new RegExp('^\\d{4}/\\d{2}/\\d{2}T\\d{2}:\\d{2}', 'g')
                                : new RegExp('^\\d{4}/\\d{2}/\\d{2}T\\d{2}:\\d{2}:\\d{2}\.\\d{6}', 'g');

                            const validStringFormat = regex.test(e.target.value);
                            const newDate = this.props.shortFormat ?
                                TimeUtil.parseShortISOString(e.target.value) : TimeUtil.parseISOString(e.target.value);
                            // If the date is valid
                            if (validStringFormat && !isNaN(newDate.getTime())) {
                                this.setState({...this.state,
                                               isValid: true, displayString: e.target.value},
                                              () => {
                                                    this.props.onMaybeDate(newDate);
                                                }
                                               );

                            } else {
                                // If the date is not valid
                                this.setState({...this.state, isValid: false, displayString: e.target.value});
                                this.props.onMaybeDate(undefined);
                                if (this.props.setHold) {
                                    this.props.setHold(true);
                                }

                            }
                        }
                    }
                />
                {
                    this.state.showDatePicker ?
                        <DatePicker
                            className={
                                this.state.datePickerOnBottom ?
                                'time-picker__date-picker time-picker__date-picker--bottom'
                                : 'time-picker__date-picker'
                            }
                            value={this.props.date}
                            onChange={(inputDate, isUserChange) => {
                                // Creates new date from state
                                // Updates new date with values from date picker
                                if (isUserChange) {
                                    document.body.removeEventListener('click', this.hideDatePickerOnClick);
                                    document.body.removeEventListener('keydown', this.hideDatePickerOnKeydown);
                                    const newDate = this.props.date;
                                    newDate.setDate(inputDate.getDate());
                                    newDate.setMonth(inputDate.getMonth());
                                    newDate.setFullYear(inputDate.getFullYear());
                                    this.props.onMaybeDate(newDate);
                                    if (this.props.setHold) {
                                        this.props.setHold(false);
                                    }
                                    this.setState({...this.state,
                                                   isValid: true, showDatePicker: false,
                                                   displayString: this.props.shortFormat ?
                                                        TimeUtil.dateToShortISOString(newDate)
                                                        : TimeUtil.dateToISOString(newDate)
                                                });
                                }

                            }}
                        />
                        : null
                    }
                {
                    this.props.datePickerEnabled ?
                    <Button
                        icon="calendar"
                        onClick={() => {
                            this.setState({...this.state, showDatePicker: !this.state.showDatePicker});
                        }}
                        className={
                            this.state.showDatePicker ?
                            'time-picker__date-picker-button time-picker__date-picker-button--active'
                            : 'time-picker__date-picker-button'
                        }
                    />
                    : null
                }
            </div>
        );
    }

    public componentDidUpdate(prevProps: TimePickerTypes.TimePickerProps, prevState: TimePickerTypes.TimePickerState) {
            // We only check the date picker's position when it's created
            if (!prevState.showDatePicker && this.state.showDatePicker) {
                this.repositionDatePicker();
                document.body.addEventListener('click', this.hideDatePickerOnClick);
                document.body.addEventListener('keydown', this.hideDatePickerOnKeydown);

            }
            if (prevProps.date.valueOf() !== this.props.date.valueOf()) {
                this.setState({
                    ...this.state,
                    isValid: true,
                    displayString: this.props.shortFormat ?
                        TimeUtil.dateToShortISOString(this.props.date)
                        : TimeUtil.dateToISOString(this.props.date),
                });
            }
    }
    private readonly hideDatePickerOnKeydown = (e: any) => {
        if (e.nativeEvent.code === 'Escape') {
            this.hideDatePickerOnClick(e);
        }
    }
    private readonly hideDatePickerOnClick = (e: any) => {
        let parent = e.target.parentNode;
        let hideDatePicker = true;
        while (parent && hideDatePicker) {
            if (parent.classList && parent.classList.contains('time-picker__date-picker')) {
                hideDatePicker = false;
            }
            parent = parent.parentNode;
        }

        if (hideDatePicker) {
            document.body.removeEventListener('click', this.hideDatePickerOnClick);
            document.body.removeEventListener('keydown', this.hideDatePickerOnKeydown);
            e.stopPropagation();
            this.setState({showDatePicker: false});
        }
    }
    private readonly repositionDatePicker = () => {
        if (this.timePickerRef && this.state.showDatePicker) {
            const MIN_HEIGHT_OF_DATE_PICKER_PX = 233;

            const elemRect = this.timePickerRef.getBoundingClientRect();
            let container = this.timePickerRef.parentElement;

            // If the timepicker is in a normal div, then we use the golden layout component
            // to decide if it's off screen
            if (!container) {
                return;
            }
            while (container.className !== 'lm_content') {
                container = container.parentElement;
                if (!container) {
                    break;
                }
            }
            // Otherwise, we use the document body [occurs if timepicker is in context menu]
            if (!container) {
                container = document.body;
            }
            const containerRect = container.getBoundingClientRect();

            if ((elemRect.top - MIN_HEIGHT_OF_DATE_PICKER_PX) < containerRect.top) {
                this.setState({...this.state, datePickerOnBottom: true});
            }
        }
    }
}
export { TimePicker };
