import * as React from 'react';
import { FormTypes } from '../../';
/**
 * Props for form text display
 */
export interface FormDisplayTextProps {
    displayText: string;
    formatAs?: FormTypes.TextFormats;
    widthPx: number;
}
/**
 * Displays text for Form
 */
export class FormDisplayText extends React.Component<FormDisplayTextProps, {}> {

    private constructor(props) {
        super(props);
      }
    /**
     * Renders the component
     */
    public render() {
        const className = this.props.formatAs === FormTypes.TextFormats.Time ?
            'form-value form-value--uneditable form-value--time'
            : 'form-value form-value--uneditable';
        return (
            <div
                className={className}
                style={{width: `${this.props.widthPx}px`}}
            >
                {this.props.displayText}
            </div>
        );
    }

}
