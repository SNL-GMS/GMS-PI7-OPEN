import { Classes } from '@blueprintjs/core';
import * as React from 'react';
import { WidgetTypes } from '../';
import { DropDownProps } from './types';
/**
 * Drop Down menu
 */
export class DropDown extends React.Component<DropDownProps, WidgetTypes.WidgetState> {
    private constructor(props) {
        super(props);
        this.state = {
            value: this.props.value,
            isValid: true
        };
      }
    /**
     * React component lifecycle.
     */
    public render() {
        const className = `${Classes.SELECT} form__select`;
        const minWidth = `${this.props.widthPx}px`;
        const altStyle = {
            minWidth,
            width: minWidth
        };
        return (
            <div
                className={className}
                title={this.props.title}
                style={
                    this.props.widthPx !== undefined ?
                    altStyle : undefined
                }
            >
                <select
                    className="form__select"
                    title={this.props.title}
                    disabled={this.props.disabled}
                    style={
                        this.props.widthPx !== undefined ?
                        altStyle : undefined
                    }
                    onChange={
                        e => {
                            const input = e.target.value;
                            this.props.onMaybeValue(input);
                        }
                    }
                    value={this.props.value}
                >
                    {this.createDropdownItems(this.props.dropDownItems)}
                </select>
            </div>
        );
    }

    /**
     * Creates the HTML for the dropwdown items for the type input
     * 
     */
    private readonly createDropdownItems = (enumOfOptions: any): JSX.Element[] => {
        const items: any[] = [];
        Object.keys(enumOfOptions)
            .forEach(type => {
                items.push(<option key={type} value={enumOfOptions[type]}>{enumOfOptions[type]}</option>);
            });
        return items;
    }
}
