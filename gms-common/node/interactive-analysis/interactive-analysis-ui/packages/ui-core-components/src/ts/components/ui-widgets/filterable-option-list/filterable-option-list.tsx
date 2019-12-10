import * as React from 'react';
import '../css/filterable-option-list.scss';
import { FilterableOptionListProps, FilterableOptionListState } from './types';

// Timeout to prevent double click handlers from firing in other parts of UI
export class FilterableOptionList extends React.Component<FilterableOptionListProps, FilterableOptionListState> {
    /** Internal reference to list of options */
    private optionRefs: HTMLDivElement[] = [];
    /** Internal reference to search input */
    private searchRef: HTMLInputElement | null;
    private constructor(props) {
        super(props);
        this.state = {
            currentlySelected: this.props.defaultSelection ? this.props.defaultSelection : '',
            currentFilter: this.props.defaultFilter ? this.props.defaultFilter : '',
        };
      }

    public componentWillUpdate () {
        this.optionRefs = [];
    }

    public componentDidUpdate (prevProps: FilterableOptionListProps, prevState: FilterableOptionListState) {
       if (prevProps.disabled && !this.props.disabled) {
            if (this.searchRef) {
                this.searchRef.focus();
            }
       }
       // Focus on an option if the user changed selection via the arrow keys,
       // but NOT if the selection was made by filtering the lsit
       if (prevState.currentlySelected !== this.state.currentlySelected &&
            prevState.currentFilter === this.state.currentFilter
        ) {
        const option = this.optionRefs.find(ref => ref.getAttribute('id') === this.state.currentlySelected);
        if (option) {
            option.focus();
        }
       }
    }

    /**
     * React component lifecycle.
     */
    public render() {

        const prioritySorted = this.props.prioriotyOptions ?
        [...this.props.prioriotyOptions].sort((a, b) => a.localeCompare(b, 'en', {sensitivity: 'base'})) : [];
        const filteredPriority = prioritySorted.filter(opt => opt.toLowerCase()
                                                                  .includes(this.state.currentFilter.toLowerCase()));
        const sortedOptions = [...this.props.options].sort((a, b) => a.localeCompare(b, 'en', {sensitivity: 'base'}));
        const filteredOptions = sortedOptions.filter(opt =>
             opt.toLowerCase()
                 .includes(this.state.currentFilter.toLowerCase()) && !(prioritySorted.indexOf(opt) > -1)
        );

        const widths = this.props.widthPx ? `${this.props.widthPx}px` : '';
        const altStyle = {
            width: widths,
            marginLeft: '0px'
        };
        return (
            <div
                className="filterable-option-list"
            >
                <input
                    className={
                        this.props.disabled ?
                        'filterable-option-list__search filterable-option-list__search--disabled'
                        : 'filterable-option-list__search'
                    }
                    ref={ref => this.searchRef = ref}
                    type="search"
                    disabled={this.props.disabled}
                    placeholder="Search input"
                    tabIndex={0}
                    onChange={e => {
                        this.onFilterInput(e);
                    }}
                    autoFocus={true}
                    onKeyDown={this.onKeyPress}
                    value={this.state.currentFilter}
                    style={
                        this.props.widthPx ?
                        altStyle : undefined
                    }
                />
                <div
                    className="fitlerable-option-list__list"
                    style={
                        this.props.widthPx ?
                        altStyle : undefined
                    }
                >
                    {
                        filteredPriority.length > 0 ?
                        filteredPriority.map(pOpt => this.renderOption(pOpt))
                        : null
                    }
                    {
                        filteredPriority.length > 0 ?
                        <div
                            className="fitlerable-option-list__divider"
                        />
                        : null
                    }
                    {
                        filteredOptions.map(opt => this.renderOption(opt))
                    }
                </div>
            </div>
    );
  }

    private readonly renderOption = (label: string): JSX.Element => {
    let className =
      this.state.currentlySelected === label ?
        'filterable-option-list-item__selected filterable-option-list-item'
        : 'filterable-option-list-item';
    if (this.props.disabled) {
            className = className + ' filterable-option-list-item--disabled';
        }
    return (
            <div
                className={className}
                onClick={
                    this.props.disabled ?
                        undefined :
                        e => {
                            e.preventDefault();
                            e.stopPropagation();
                            this.selectOption(label, false);
                        }}
                id={label}
                key={label}
                tabIndex={0}
                onKeyDown={this.onKeyPress}
                ref={ref => {if (ref) {this.optionRefs.push(ref); }}}
                onDoubleClick={
                    this.props.disabled ?
                    undefined :
                    e => {
                        e.preventDefault();
                        e.stopPropagation();
                        this.selectOption(label, true);
                    }
                }
            >
                {label}
            </div>
         );
    }

    private readonly onArrowUpDown = (downArrow: boolean) => {
        // Re-create the filtered lists of options
        const prioritySorted = this.props.prioriotyOptions ? [...this.props.prioriotyOptions]
        .sort((a, b) => a.localeCompare(b, 'en', {sensitivity: 'base'})) : [];
        const filteredPriority = prioritySorted.filter(opt => opt.toLowerCase()
                                                                  .includes(this.state.currentFilter.toLowerCase()));
        const sortedOptions = [...this.props.options].sort((a, b) => a.localeCompare(b, 'en', {sensitivity: 'base'}));
        const filteredOptions = sortedOptions.filter(opt =>
             opt.toLowerCase()
                 .includes(this.state.currentFilter.toLowerCase()) && !(prioritySorted.indexOf(opt) > -1)
        );
        const selectedInPriority = filteredPriority.indexOf(this.state.currentlySelected) > -1;
        const selectedInList = filteredOptions.indexOf(this.state.currentlySelected) > -1;
        const indexOfSelected = selectedInPriority ?
            filteredPriority.indexOf(this.state.currentlySelected)
            : selectedInList ?
                filteredOptions.indexOf(this.state.currentlySelected)
                : -1;
        if (downArrow) {
            // tries to navigate down in priority list
            if (selectedInPriority) {
                let provisionalIndex = indexOfSelected + 1;
                if (provisionalIndex < filteredPriority.length) {
                    // Navigates down in priority list
                    this.setState({currentlySelected: filteredPriority[provisionalIndex]});
                    this.props.onSelection(filteredPriority[provisionalIndex]);
                } else {
                    provisionalIndex = 0;
                    if (filteredOptions.length > 0) {
                        // navigates to top of regular list
                        this.setState({currentlySelected: filteredOptions[provisionalIndex]});
                        this.props.onSelection(filteredOptions[provisionalIndex]);
                    } else {
                        // loops to top of priority
                        this.setState({currentlySelected: filteredPriority[provisionalIndex]});
                        this.props.onSelection(filteredPriority[provisionalIndex]);

                    }
                }
            // tries to navigate down in regular list
            } else if (selectedInList) {
                let provisionalIndex = indexOfSelected + 1;
                if (provisionalIndex < filteredOptions.length) {
                    // Navigates down in regular list
                    this.setState({currentlySelected: filteredOptions[provisionalIndex]});
                    this.props.onSelection(filteredOptions[provisionalIndex]);
                } else {
                    provisionalIndex = 0;
                    if (filteredPriority.length > 0) {
                        // Navigates to top of priority list
                        this.setState({currentlySelected: filteredPriority[provisionalIndex]});
                        this.props.onSelection(filteredPriority[provisionalIndex]);

                    } else {
                        // Navigates to top of regular list
                        this.setState({currentlySelected: filteredOptions[provisionalIndex]});
                        this.props.onSelection(filteredOptions[provisionalIndex]);
                    }
                }
            // starts selection from top of first available list
            } else {
                const provisionalIndex = 0;
                if (filteredPriority.length > 0) {
                    // navigates to top of priority
                    this.setState({currentlySelected: filteredPriority[provisionalIndex]});
                    this.props.onSelection(filteredPriority[provisionalIndex]);

                } else {
                    // navigates to top of regular
                    this.setState({currentlySelected: filteredOptions[provisionalIndex]});
                    this.props.onSelection(filteredOptions[provisionalIndex]);
                }
            }
        } else {
            // Tries to navigate up in priority list
            if (selectedInPriority) {
                let provisionalIndex = indexOfSelected - 1;
                if (provisionalIndex >= 0) {
                    // navigate up in priority list
                    this.setState({currentlySelected: filteredPriority[provisionalIndex]});
                    this.props.onSelection(filteredPriority[provisionalIndex]);

                } else {
                    provisionalIndex = filteredOptions.length - 1;
                    if (filteredOptions.length > 0) {
                        // navigate to bottom of regular
                        this.setState({currentlySelected: filteredOptions[provisionalIndex]});
                        this.props.onSelection(filteredOptions[provisionalIndex]);

                    } else {
                        // navigate to bottom of priority
                        provisionalIndex = filteredPriority.length - 1;
                        this.setState({currentlySelected: filteredPriority[provisionalIndex]});
                        this.props.onSelection(filteredPriority[provisionalIndex]);
                    }
                }
            // tries to navigate up in regular list
            } else if (selectedInList) {
                let provisionalIndex = indexOfSelected - 1;
                if (provisionalIndex >= 0) {
                    // navigates up in regular list
                    this.setState({currentlySelected: filteredOptions[provisionalIndex]});
                    this.props.onSelection(filteredOptions[provisionalIndex]);

                } else {
                    provisionalIndex = filteredPriority.length - 1;
                    if (filteredPriority.length > 0) {
                        // navigates to bottom of priority
                        this.setState({currentlySelected: filteredPriority[provisionalIndex]});
                        this.props.onSelection(filteredPriority[provisionalIndex]);
                    } else {
                        // navigates to bottom of regular
                        provisionalIndex = filteredOptions.length - 1;
                        this.setState({currentlySelected: filteredOptions[provisionalIndex]});
                        this.props.onSelection(filteredOptions[provisionalIndex]);
                    }
                }
            // starts new selection of bottom of lowest list
            } else {
                let provisionalIndex = filteredPriority.length - 1;
                if (filteredOptions.length > 0) {
                    // starts at bottom of regular
                    provisionalIndex = filteredOptions.length - 1;
                    this.setState({currentlySelected: filteredOptions[provisionalIndex]});
                    this.props.onSelection(filteredOptions[provisionalIndex]);
                } else if (filteredPriority.length > 0) {
                    // starts at bottom of filtered
                    this.setState({currentlySelected: filteredPriority[provisionalIndex]});
                    this.props.onSelection(filteredPriority[provisionalIndex]);

                }
            }
        }
    }

    private readonly onKeyPress = (e: React.KeyboardEvent) => {
        switch (e.key) {
            // down arrow
            case 'ArrowDown':
                e.preventDefault();
                this.onArrowUpDown(true);
                break;
            // up arrow
            case 'ArrowUp':
                e.preventDefault();
                this.onArrowUpDown(false);
                break;
            // enter/return
            case 'Enter':
                e.preventDefault();
                if (this.props.onEnter) {
                    this.props.onEnter(this.state.currentlySelected);
                }
                break;
            default:
                return;
        }
    }

    private readonly onFilterInput = (e: React.ChangeEvent<HTMLInputElement>) => {
        // if the currently selected option would be excluded from the search
        // set currently selected to first visible option
        let selected = this.state.currentlySelected;
        if (!this.state.currentlySelected.includes(e.currentTarget.value)) {
            selected = this.getFirstVisibleOption(
                this.props.options,
                this.props.prioriotyOptions ? this.props.prioriotyOptions : [],
                e.currentTarget.value);
        }
        this.setState({currentFilter: e.currentTarget.value, currentlySelected: selected});
    }

    private readonly getFirstVisibleOption = (options: string[], prioriotyOptions: string[], filter: string) => {
        const sortedPriority = prioriotyOptions
            .filter(opt => opt.toLowerCase()
            .includes(filter.toLowerCase()));
        sortedPriority.sort((a, b) => a.localeCompare(b, 'en', {sensitivity: 'base'}));

        const sortedRegular = options
            .filter(opt => opt.toLowerCase()
            .includes(filter.toLowerCase()));
        sortedRegular.sort((a, b) => a.localeCompare(b, 'en', {sensitivity: 'base'}));

        if (sortedPriority.length > 0) {
            return sortedPriority[0];
        } else if (sortedRegular.length > 0) {
            return sortedRegular[0];
        } else {
            return '';
        }
    }

    private readonly selectOption = (value: string, isDoubleClick: boolean) => {
        this.setState({currentlySelected: value});
        this.props.onSelection(value);
        if (this.props.onClick && !isDoubleClick) {
            this.props.onClick(value);
        } else if (this.props.onDoubleClick && isDoubleClick) {
            this.props.onDoubleClick(value);
        }
    }
}
