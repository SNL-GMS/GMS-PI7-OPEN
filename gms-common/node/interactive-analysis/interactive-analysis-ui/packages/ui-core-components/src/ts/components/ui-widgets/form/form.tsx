/*
 Form is a reuable component for creating forms and dialogue boxes
 It takes care of the details of layout
 It accepts formItems of the type FormItem
 When the user clicks on the submit button, it will return data from all FormItems that are editable
 in a map of type <string, any>
*/

import { Button, ButtonGroup } from '@blueprintjs/core';
import * as lodash from 'lodash';
import * as React from 'react';
import { FormTypes } from '../';
import '../css/form-body.scss';
import { FormBody } from './form-body/form-body';

const DEFAULT_LABEL_FONT_SIZE_EM = 1;

 /**
  * Form Props
  * 
  * @param header Header text for the form
  * @param headerDecoration Option JSX Element to be place on right edge of header
  * @param defaultPanel Default content to display
  * @param extraPanels Optional list of extra panels of content
  * @param submitButtonText Text for the button which submits the form
  * @param disableSubmit If true, then no submit button is shown. OnSubmit() is still required
  * @param requiresModificationForSubmit If true, a field must be modified before the form can be submitted
  * @param onSubmit Callback when for is submitted, returns map from entry.value.valueKey to response data
  * @param onCancel Callback for when form is cancelled via the 'cancel' button
  * 
  */

export interface FormProps {
    header: string;
    headerDecoration?: JSX.Element;
    extraPanels?: FormTypes.FormPanel[];
    defaultPanel: FormTypes.FormPanel;
    submitButtonText?: string;
    disableSubmit?: boolean;
    requiresModificationForSubmit?: boolean;
    onSubmit?(data: any);
    onCancel();
 }

export interface FormState {
     openPanel: FormTypes.FormPanel;
     formItemStates: Map<string, FormTypes.FormItemState>;
 }
/**
 * Form component.
 */
export class Form extends React.Component<FormProps, FormState> {

    // The constructor checks the FormItems to check that all required fields are supplied
    // Ie, all DropDowns have DropDownItems
    private constructor(props) {
        super(props);
        if (this.props.extraPanels) {
            this.props.extraPanels.forEach(entry => {
                if (!entry.content && ! entry.formItems) {
                    throw new Error(`FormPanels must have either 'content' or 'FormItems'`);
                }
            });
        }
        if (!this.props.disableSubmit && !this.props.onSubmit) {
            throw new Error(`FormPanels must have onSubmit when disableSubmit is false or undefined'`);

        }
        const formItemStates = new Map<string, FormTypes.FormItemState>();
        if (this.props.defaultPanel.formItems)  {
            this.props.defaultPanel.formItems.forEach(item => {
                formItemStates.set(item.itemKey, {
                    modified: false,
                    value: item.value ? item.value
                        : undefined,
                    hasHold: false
                });
                }
            );
        }
        this.state = {
            formItemStates,
            openPanel: this.props.defaultPanel
        };
      }

    /**
     * React component lifecycle.
     */
    public render() {

        let maxLabelWidthEm = 1;
        let maxLengthString = 0;
        if (this.props.defaultPanel.formItems) {
            this.props.defaultPanel.formItems.forEach(item => {
                const lengthOfLabel = item.hideLabelColon ? item.labelText.length : item.labelText.length + 1;
                if (lengthOfLabel > maxLengthString) {
                    maxLengthString = lengthOfLabel;
                }
            });
        }
        const fontSizeEm = DEFAULT_LABEL_FONT_SIZE_EM;
        // Because fonts are as wide as their fontSize, we use this is estimate the width of the label
        maxLabelWidthEm = maxLengthString * fontSizeEm;
        //  * (fontSizeEm - heuristicForFontWidth);
        const DEFAULT_VALUE_WIDTH_PX = 280;
        const EM_TO_PX = 16;
        const FORM_MIN_WIDTH = DEFAULT_VALUE_WIDTH_PX + (maxLabelWidthEm * EM_TO_PX);
        const PADDING_IN_PX = 24;

        let isAnythingModified = false;
        this.state.formItemStates.forEach(itemState =>
            isAnythingModified = itemState.modified || isAnythingModified
        );

        return (
            <div
                className="form"
                style={{width: FORM_MIN_WIDTH.toString() + 'px'}}
            >
                <div className="form__header">
                    <div>
                        {
                            isAnythingModified ?
                            this.props.header + '*'
                            : this.props.header
                        }
                    </div>
                    {
                        this.props.headerDecoration ?
                        <div className="form__header-decoration">
                            {this.props.headerDecoration}
                        </div>
                        : null
                    }
                </div>
                <div
                    className="form__panel-selector"
                >
                    {

                        this.props.extraPanels && this.props.extraPanels.length > 0 ?
                            <ButtonGroup>
                                <Button
                                    value="default"
                                    onClick={this.onPanelButtonClick}
                                    active={this.props.defaultPanel.key === this.state.openPanel.key}
                                >
                                    {this.props.defaultPanel.key}
                                </Button>
                                {
                                    this.props.extraPanels.map(panel =>
                                        (
                                            <Button
                                                value={panel.key}
                                                onClick={this.onPanelButtonClick}
                                                active={panel.key === this.state.openPanel.key}
                                                key={panel.key}
                                            >
                                                {panel.key}
                                            </Button>
                                        ))
                                }
                            </ButtonGroup>
                            : null
                    }
                </div>
                {
                    this.state.openPanel.formItems ?
                    <FormBody
                        formItems={this.state.openPanel.formItems}
                        formItemStates={this.state.formItemStates}
                        labelFontSizeEm={DEFAULT_LABEL_FONT_SIZE_EM}
                        onHoldChange={this.onHoldChange}
                        onValue={this.onValue}
                        maxLabelWidthEm={maxLabelWidthEm}
                    />
                    : null
                }
                {
                    this.state.openPanel.content ?
                    <div className="form__content" style={{width: (FORM_MIN_WIDTH - PADDING_IN_PX) .toString() + 'px'}}>
                        {this.state.openPanel.content}
                      </div>
                    : null
                }
                <div className="form__buttons" >
                    <div className="form__buttons--right">
                        {
                            !this.props.disableSubmit ?
                            <Button
                                className="form__button"
                                text={this.props.submitButtonText ? this.props.submitButtonText : 'Submit'}
                                onClick={() => {
                                    if (this.props.onSubmit) {
                                        const dataToReturn = {};
                                        this.state.formItemStates.forEach((itemState, key) => {
                                            if (itemState.value !== undefined) {
                                                dataToReturn[key] = itemState.value.defaultValue;
                                            }
                                        });
                                        this.props.onSubmit(dataToReturn);
                                    }
                                }}
                                disabled={this.areThereAnyHolds() ||
                                    (!isAnythingModified && this.props.requiresModificationForSubmit)
                                }
                                key={'submit-button'}
                                title={!isAnythingModified && this.props.requiresModificationForSubmit ?
                                     'Must modify a field to save' : undefined}
                            />
                            : null
                        }
                        <Button
                            className="form__button"
                            text={this.props.disableSubmit ? 'Close' : 'Cancel'}
                            onClick={this.props.onCancel}
                            key={'cancet-button'}
                        />

                    </div>
                </div>
            </div>
        );
    }

    private readonly onPanelButtonClick = (event: React.MouseEvent<HTMLElement>) => {
        const value = event.currentTarget.getAttribute('value');
        if (!this.props.extraPanels) {
            return;
        }
        if (this.props.extraPanels.find(panel => panel.key === value)) {
            const openPanel = this.props.extraPanels.find(panel => panel.key === value);
            if (openPanel) {
                this.setState({...this.state, openPanel});
            }
        } else {
            this.setState({...this.state, openPanel: this.props.defaultPanel});
        }
    }

    private readonly onHoldChange = (valueKey: string, hold: boolean) => {
        if (this.state.formItemStates.get(valueKey)) {
            const itemState = this.state.formItemStates.get(valueKey);
            if (itemState) {
                itemState.hasHold = hold;
                const newItemStates = this.state.formItemStates;
                newItemStates.set(valueKey, itemState);
                this.setState({...this.state, formItemStates: newItemStates});
            }
        }
    }

    private readonly areThereAnyHolds = (): boolean => {
        let isAHold = false;
        this.state.formItemStates.forEach(entry => {
            isAHold = isAHold || entry.hasHold;
        });
        return isAHold;
    }

    private readonly onValue = (valueKey: string, value: any) => {
        if (this.state.formItemStates.get(valueKey)) {
            const itemState = this.state.formItemStates.get(valueKey);
            if (itemState && this.props.defaultPanel.formItems) {
                const itemProp = this.props.defaultPanel.formItems.find(item => item.itemKey === valueKey);
                if (itemProp && itemProp.value) {
                    itemState.modified = !lodash.isEqual(value, itemProp.value.defaultValue);
                    itemState.value = {
                        ...itemProp.value,
                        defaultValue: value
                    };
                    const newItemStates = this.state.formItemStates;
                    newItemStates.set(valueKey, itemState);
                    this.setState({...this.state, formItemStates: newItemStates});
                }
            }
        }
    }
}
