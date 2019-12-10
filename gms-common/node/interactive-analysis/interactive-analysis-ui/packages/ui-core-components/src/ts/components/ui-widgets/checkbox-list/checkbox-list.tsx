import { Checkbox } from '@blueprintjs/core';
import * as React from 'react';
import { CheckboxListProps, CheckboxListState } from './types';
// Renders a list of checkboxes that can be scrolled through
export class CheckboxList extends React.Component<CheckboxListProps, CheckboxListState> {

    private constructor(props) {
        super(props);
        this.state = {
            currentFilter: '',
        };
      }
    public render () {
        return (
            <div
                className="checkbox-list"
                style={{
                    height: this.props.maxHeightPx ? `${this.props.maxHeightPx}px` : '200px'
                }}
            >
                <input
                    className={
                      'checkbox-list__search'
                    }
                    type="search"
                    placeholder="Search input"
                    tabIndex={0}
                    onChange={e => {
                        this.onFilterInput(e);
                    }}
                    autoFocus={true}
                    value={this.state.currentFilter}
                />
                {
                    this.props.items.map(item =>
                        item.name.toLowerCase()
                                .includes(this.state.currentFilter.toLowerCase()) ?
                            (
                            <div
                                className="checkbox-list-item"
                                key={`checkbox-list${item.id}`}
                            >
                                <Checkbox
                                    checked={item.checked}
                                    onChange={() => {
                                        this.props.onCheckboxChecked(item.id, !item.checked);
                                    }}
                                />
                                <span
                                    className="checkbox-list-item__label"
                                >
                                    {item.name}
                                </span>
                            </div>
                            )
                        : null
                    )
                }
            </div>
        );
    }
    private readonly onFilterInput = (e: React.ChangeEvent<HTMLInputElement>) => {
        this.setState({currentFilter: e.currentTarget.value});
    }
}
