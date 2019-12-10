/*
FormLabel renders the formitem's label
*/
import * as React from 'react';

/**
 * FormLabel Props
 */
export interface FormLabelProps {
    text: string;
    fontSizeEm: number;
    hideColon?: boolean;
    modified: boolean;
    widthEm: number;
 }

/**
 * FormLabel component.
 */
export class FormLabel extends React.Component<FormLabelProps> {

    private constructor(props) {
        super(props);
      }

    /**
     * React component lifecycle.
     */
    public render() {
        /*
        */
        const fontSizeEm = this.props.fontSizeEm.toString() + 'em';
        const widthEm = this.props.widthEm.toString() + 'em';
        return (
            <div
                className="form-label"
                style={
                    {fontSize: (fontSizeEm), minWidth: widthEm}}
            >
                {
                    this.props.modified ?
                        this.props.text + '*'
                        : this.props.hideColon ?
                        this.props.text
                        : this.props.text + ':'
                }
            </div>
        );
    }
}
