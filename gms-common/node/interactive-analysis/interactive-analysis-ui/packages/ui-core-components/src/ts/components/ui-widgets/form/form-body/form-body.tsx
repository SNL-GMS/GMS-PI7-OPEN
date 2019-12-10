/*
The FormBody iterates through the FormItems and renders them
*/

import * as React from 'react';
import { FormTypes } from '../';
import { FormItem } from '../types';
import { FormDisplayText } from './form-item/form-display-text';
import { FormLabel } from './form-item/form-label';
import { FormValue } from './form-item/form-value';
/**
 * FormBody Props
 */
export interface FormBodyProps {
    labelFontSizeEm: number;
    formItems: FormTypes.FormItem[];
    hasHold?: boolean;
    maxLabelWidthEm: number;
    formItemStates: Map<string, FormTypes.FormItemState>;
    onHoldChange(valueKey: string, holdStatus: boolean);
    onValue(valueKey: string, payload: any);
 }

/**
 * FormBody state
 */
export interface FormBodyState {
    dummy?: boolean;
    hasHold: boolean;
    value: any;
}

/**
 * FormBody component.
 */
export class FormBody extends React.Component<FormBodyProps, FormBodyState> {

    private constructor(props) {
        super(props);
      }

    /**
     * React component lifecycle.
     */
    public render() {
        /*
        * So that the FormLabels are all the same rendered width, we do a tricky trick
        * The 'em' unit is the width of a capitol M, the widesth character
        * So we set the width of each FormLabel to the width of the widest possible label
        * May need to be adjusted in the future with a better heuristic
        */
        const DEFAULT_VALUE_WIDTH_PX = 280;
        return (
            <div className="form-body">
                {
                    this.props.formItems.map(item =>
                        (
                            <div className="form-item" key={item.labelText}>
                                <FormLabel
                                    fontSizeEm={
                                        this.props.labelFontSizeEm
                                    }
                                    text={item.labelText}
                                    hideColon={item.hideLabelColon}
                                    modified={this.isModified(item)}
                                    widthEm={this.props.maxLabelWidthEm}
                                />
                                {
                                    item.itemType === FormTypes.ItemType.Input &&
                                    item.value !== undefined ?
                                        <FormValue
                                            value={this.getValueForItemKey(item)}
                                            itemKey={item.itemKey}
                                            onHoldChange={
                                                this.props.onHoldChange
                                            }
                                            onValue={
                                                this.props.onValue
                                            }
                                            widthPx={
                                                DEFAULT_VALUE_WIDTH_PX
                                            }
                                        />
                                        : <FormDisplayText
                                            displayText={item.displayText !== undefined ? item.displayText : ''}
                                            widthPx={DEFAULT_VALUE_WIDTH_PX}
                                            formatAs={item.displayTextFormat !== undefined ?
                                                item.displayTextFormat : undefined}
                                        />
                                }
                         </div>
                        ))
                }
            </div>
        );
    }
    private readonly getValueForItemKey = (item: FormItem) => {
        const formItemState = this.props.formItemStates.get(item.itemKey);
        if (formItemState !== undefined) {
            return formItemState.value ?
            formItemState.value
            : item.value;
        } else {
            return item.value;
        }
    }
    private readonly isModified = (item: FormTypes.FormItem): boolean => {
        const itemState = this.props.formItemStates.get(item.itemKey);
        if (itemState) {
            return itemState.modified;
        } else {
            return false;
        }
    }

}
