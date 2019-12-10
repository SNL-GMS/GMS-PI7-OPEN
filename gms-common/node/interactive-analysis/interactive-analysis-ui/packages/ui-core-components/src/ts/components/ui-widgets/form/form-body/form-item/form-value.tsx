import * as React from 'react';
import { Widget, WidgetTypes } from '../../../';

/**
 * FormValue Props
 */
export interface FormValueProps {
    value: WidgetTypes.WidgetData;
    widthPx: number;
    itemKey: string;
    onValue(valueKey: string, payload: any);
    onHoldChange(valueKey: string, holdStatus: boolean);
 }

/*
* FormValue State
*/

export interface FormValueState {
    currentValue: any;
    isOnHold: boolean;
}

/**
 * FormValue component.
 */
export class FormValue extends React.Component<FormValueProps, FormValueState> {

    private constructor(props) {
        super(props);
        this.state = {
            currentValue: this.props.value.defaultValue,
            isOnHold: false
        };
      }

    /**
     * React component lifecycle.
     */
    public render() {
        return (
            <div
                className="form-value"
                style={{width: `${this.props.widthPx}px`}}
            >
                <Widget
                    {...this.props.value}
                    onMaybeValue={this.onMaybeInput}
                    onValidStatus={this.onHoldStatus}
                    isValid={!this.state.isOnHold}
                />
            </div>
        );
    }

    public getHoldStatus = (): boolean => this.state.isOnHold;
    public getCurrentValue = (): any => this.state.currentValue;

    private readonly onMaybeInput = (input: any | undefined) => {
        if (input !== undefined) {
            this.setState({...this.state, currentValue: input});
            this.props.onValue(this.props.itemKey, input);
        }
    }
    private readonly onHoldStatus = (hold: boolean) => {
            this.setState({...this.state, isOnHold: hold});
            this.props.onHoldChange(this.props.itemKey, hold);
        }

}
