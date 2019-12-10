import * as React from 'react';
import { WidgetTypes } from '../';
import { TextAreaProps } from './types';

export class TextArea extends React.Component<TextAreaProps, WidgetTypes.WidgetState> {
    private constructor(props) {
        super(props);
        this.state = {
            isValid: true,
            value: this.props.defaultValue
        };
      }

    /**
     * React component lifecycle.
     */
    public render() {
        return (
            <textarea
                className="form__text-input"
                rows={4}
                onChange={
                    e => {
                        this.setState({...this.state, value: e.currentTarget.value});
                        this.props.onMaybeValue(e.currentTarget.value);
                    }
                }
                value={this.state.value}

            />
        );
    }

}
