import { Menu, MenuDivider, MenuItem } from '@blueprintjs/core';
import * as React from 'react';
import { HistoryListTypes } from './';
// Default length of history list, overridden by props.listLength
const DEFAULT_LIST_LENGTH = 7;

/**
 * Accepts a start and end time as input
 * Protects against cases where startTime > endTime
 * Basically two <TimePicker/>'s put in a div with an enter button
 */

export class HistoryList
    extends React.Component<HistoryListTypes.HistoryListProps, {}> {

    private selfRef: HTMLDivElement;

    /**
     * Contructor 
     * @param props props for component
     */
    private constructor(props) {
        super(props);
    }

    /**
     * React component lifecycle.
     */
    public render() {
        const listLength = this.props.listLength ? this.props.listLength : DEFAULT_LIST_LENGTH;
        const sortedItems =  this.props.items.filter((item, index) => index < listLength);
        sortedItems.sort((a, b) => b.index - a.index);

        return (
            <div
                ref={ref => {
                    if (ref) {
                        this.selfRef = ref;
                    }
                }}
            >
                <Menu>
                    {
                        this.props.preferredItems ?
                        <React.Fragment>
                            {
                            this.props.preferredItems.map(item => (
                                <MenuItem
                                    key={item.id}
                                    text={item.label}
                                    onClick={e => this.onClick(e, item.id)}
                                />
                                ))
                            }
                            <MenuDivider/>
                            </React.Fragment>
                            : null
                    }
                    {
                       sortedItems.map(item => (
                            <MenuItem
                                key={item.id}
                                text={item.label}
                                onClick={e => this.onClick(e, item.id)}
                            />
                        ))
                    }
                </Menu>
            </div>

        );
    }
    public componentDidMount () {
        if (this.selfRef) {
            this.selfRef.focus();
        }
    }
    private readonly onClick = (event: React.MouseEvent, itemId: string) => {
        this.props.onSelect(itemId);
    }
}
