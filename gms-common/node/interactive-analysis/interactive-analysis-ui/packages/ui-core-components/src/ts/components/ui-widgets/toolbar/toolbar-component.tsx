import { Alignment, Button, ContextMenu, Icon, Menu, MenuItem, Switch } from '@blueprintjs/core';
import { IconNames } from '@blueprintjs/icons';
import { isEqual } from 'lodash';
import * as React from 'react';
import { LabelValue } from '..';
import { PopoverButton } from '../';
import { CheckboxDropdown } from '../checkbox-dropdown/checkbox-dropdown';
import '../css/toolbar.scss';
import { DropDown } from '../drop-down';
import { IntervalPicker } from '../interval-picker';
import { LoadingSpinner } from '../loading-spinner';
import { NumericInput } from '../numeric-input/numeric-input';
import { ToolbarTypes } from './';
import { ToolbarItem } from './types';

const WIDTH_OF_OVERFLOW_BUTTON_PX = 46;
const AMOUNT_OF_SPACE_TO_RESERVE_PX = 16;
export class ToolbarComponent extends React.Component<ToolbarTypes.ToolbarProps, ToolbarTypes.ToolbarState> {
    private toolbarItemRefs: HTMLElement[] = [];
    private toolbarItemLeftRefs: HTMLElement[] = [];
    private overflowButtonRef: HTMLElement;
    private popoverButtonMap: Map<number, PopoverButton>;
    private toolbarRef: HTMLElement;
    private constructor(props) {
        super(props);
        this.props.items.forEach((item, index) => {
            this.props.items.forEach((itemB, indexB) => {
                if (index !== indexB && item.rank === itemB.rank) {
                    console.warn('Toolbar Error: Item ranks must be unique - change item ranks to be unique');
                }
            });
        });
        this.state = {
            checkSizeOnNextDidMountOrDidUpdate: true,
            indicesToOverflow: [],
            leftIndicesToOverlow: [],
            whiteSpaceAllotmentPx: this.props.minWhiteSpacePx ? this.props.minWhiteSpacePx : 0
        };
        this.popoverButtonMap = new Map<number, PopoverButton>();
      }

    public componentDidMount () {
        if (this.state.checkSizeOnNextDidMountOrDidUpdate) {
            this.handleResize();
        }
    }
    public componentDidUpdate (prevProps: ToolbarTypes.ToolbarProps, prevState) {
        const haveLeftItemsChanged =
        (prevProps.itemsLeft && this.props.itemsLeft) && !isEqual(prevProps.itemsLeft, this.props.itemsLeft);
        const haveRightItemsChanged = !isEqual(prevProps.items, this.props.items);
        if (this.state.checkSizeOnNextDidMountOrDidUpdate
            || prevProps.toolbarWidthPx !== this.props.toolbarWidthPx
            || haveRightItemsChanged
            || haveLeftItemsChanged) {
            this.handleResize();
        }
    }

    /**
     * React component lifecycle.
     */
    public render() {
        const sortedItems = [...this.props.items].sort((a, b) => a.rank - b.rank);
        this.toolbarItemRefs = [];
        this.toolbarItemLeftRefs = [];
        this.popoverButtonMap = new Map<number, PopoverButton>();
        return (
            <div
                className="toolbar"
                ref={ref => {if (ref) {this.toolbarRef = ref; }}}
                style={{
                    width: `${this.props.toolbarWidthPx}px`
                }}
            >
                <div className="toolbar__left-group">
                    {
                        this.props.itemsLeft ?
                            this.props.itemsLeft.map((item, index) => {
                                if (this.state.leftIndicesToOverlow.indexOf(index) < 0) {
                                    return (
                                        <div
                                            key={item.rank}
                                            className="toolbar-item toolbar-item__left"
                                            ref={ref => {
                                                if (ref) {
                                                    this.toolbarItemLeftRefs.push(ref);
                                                }
                                            }}
                                        >
                                            {this.renderItem(item)}
                                        </div>
                                    );
                                }
                            })
                            : null
                    }
                </div>
                <div className="toolbar__center-group">
                    <div
                        className="toolbar__whitespace"
                        style={{
                            width: `${this.state.whiteSpaceAllotmentPx}px`
                        }}
                    />
                </div>
                <div className="toolbar__right-group">
                {
                    sortedItems.map((item: ToolbarTypes.ToolbarItem, index: number) => {
                        if (this.state.indicesToOverflow.indexOf(index) < 0) {
                            return (
                                <div
                                    key={item.rank}
                                    className="toolbar-item"
                                    ref={ref => {if (ref) {
                                        this.toolbarItemRefs.push(ref);
                                    }
                                    }}
                                >
                                    {this.renderItem(item)}
                                </div>
                            );
                        }
                    })
                }
                {
                    this.state.indicesToOverflow.length > 0 ?
                        <div
                            ref={ref => {if (ref) {this.overflowButtonRef = ref; }}}
                        >
                            <Button
                                icon={this.props.overflowIcon ?
                                    this.props.overflowIcon : IconNames.DOUBLE_CHEVRON_RIGHT}
                                className="toolbar-overflow-menu-button"
                                style={{width: '30px'}}
                                onClick={e => {this.showOverflowMenu(); }}
                            />
                        </div>
                        : null

                }
                </div>
            </div>
        );
    }
    /**
     * hides active toolbar popup
     */
    public readonly hidePopup = () =>  ContextMenu.hide();

     /**
      * Enables the display of a specific popover in the toolbar -or- in the overflow menu
      * 
      * @param rank the rank of the item to toggle. If the item isn't a popover nothing happens
      * @param left if given sets a manual position for the popup
      * @param right if given sets a manual position for the popup
      */
    public readonly togglePopover = (rank: number) => {
        const maybeItem: ToolbarItem | undefined = this.props.items.find(i => i.rank === rank);
        if (maybeItem !== undefined) {
            if (maybeItem.type === ToolbarTypes.ToolbarItemType.Popover) {
                const popoverButton = this.popoverButtonMap.get(maybeItem.rank);
                if (popoverButton) {
                  ContextMenu.hide();
                  if (!popoverButton.isExpanded()) {
                    popoverButton.togglePopover();
                  }
                } else {
                  ContextMenu.hide();
                  if (maybeItem.popoverContent) {
                    this.toolbarRef.getBoundingClientRect();
                    ContextMenu.show(maybeItem.popoverContent, {
                      left: this.toolbarRef.getBoundingClientRect().right,
                      top: this.toolbarRef.getBoundingClientRect().bottom
                    });
                  }
                }
            }
        }
    }
    // TODO split this out into its own component
    private readonly showOverflowMenu = () => {
        const sortedItems = [...this.props.items].sort((a, b) => a.rank - b.rank);
        const overflowItems = sortedItems.filter(
            (item, index) => this.state.indicesToOverflow.indexOf(index) >= 0);
        const overFlowMenu = (
            <Menu>
                {overflowItems.map((item, index) => this.renderMenuItem(item, index))}
            </Menu>
        );
        if (this.overflowButtonRef) {
            const left = this.overflowButtonRef.getBoundingClientRect().right;
            const top = this.overflowButtonRef.getBoundingClientRect().top + this.overflowButtonRef.scrollHeight + 4;
            ContextMenu.show(overFlowMenu, {left, top});
        }
    }
    private readonly renderMenuItem = (item: ToolbarTypes.ToolbarItem, key: number): JSX.Element => {
        const itemTypes = ToolbarTypes.ToolbarItemType;
        switch (item.type) {
            case itemTypes.NumericInput:
                const renderedNumeric = this.renderItem(item, undefined, true);
                return (
                    <MenuItem
                        text={item.label}
                        icon={item.icon}
                        key={key}
                    >
                            {renderedNumeric}
                    </MenuItem>
                );
            case itemTypes.IntervalPicker: {
                return (
                    <MenuItem
                        text={item.label}
                        icon={item.icon}
                        key={key}
                    >
                        <IntervalPicker
                            renderStacked={true}
                            startDate={item.value ? item.value.startDate : 0}
                            endDate={item.value ? item.value.endDate : 1}
                            shortFormat={item.shortFormat}
                            onNewInterval={(startDate, endDate) =>
                                item.onChange ? item.onChange({startDate, endDate}) : undefined}
                            onApply={(startDate: Date, endDate: Date) => {
                                if (item.onApply) {
                                    item.onApply(startDate, endDate);
                                }
                                ContextMenu.hide();
                            }}
                            defaultIntervalInHours={item.defaultIntervalInHours}
                        />
                    </MenuItem>
                );
            }
            case itemTypes.Popover:
                return (
                    <MenuItem
                        text={item.menuLabel ? item.menuLabel : item.label}
                        icon={item.icon}
                        key={key}
                    >
                        {item.popoverContent ? item.popoverContent : (<div/>)}
                    </MenuItem>
                     );
            case itemTypes.Button:
                return (
                    <MenuItem
                        text={item.label}
                        icon={item.icon}
                        onClick={e =>
                            item.onChange ? item.onChange(e.currentTarget.value) : undefined}
                        key={key}
                    />
                );
                break;
            case itemTypes.Switch:
                const label = item.menuLabel ?
                    item.menuLabel
                    : item.label;
                return (
                    <MenuItem
                        text={label}
                        icon={item.icon}
                        key={key}
                        onClick={e => item.onChange ? item.onChange(!item.value) : undefined}
                    />
                    );
                break;
            case itemTypes.Dropdown:
                    return (
                        <MenuItem
                            text={item.label}
                            icon={item.icon}
                            key={key}
                            disabled={item.disabled}
                        >
                            {
                                item.dropdownOptions ?
                                    Object.keys(item.dropdownOptions)
                                    .map(ekey =>
                                        (<MenuItem
                                            text={item.dropdownOptions[ekey]}
                                            key={ekey}
                                            onClick={e =>
                                                item.onChange ? item.onChange(item.dropdownOptions[ekey]) : undefined}
                                            icon={item.value === item.dropdownOptions[ekey] ?
                                                IconNames.TICK : undefined}
                                        />)
                                    )
                                    : null
                            }
                        </MenuItem>
                    );
                    break;
                case itemTypes.ButtonGroup: {
                    return (
                        <MenuItem
                            text={item.label}
                            icon={item.icon}
                            key={key}
                            disabled={item.disabled}
                        >
                            {
                                item.buttons ?
                                    item.buttons.map(button => (
                                        <MenuItem
                                            text={button.label}
                                            icon={button.icon}
                                            key={button.label}
                                            disabled={button.disabled}
                                            onClick={e => {if (button.onChange) {button.onChange(e); }}}
                                        />
                                    ))
                                    : null
                            }
                        </MenuItem>
                    );
                }
                case itemTypes.LabelValue: {
                    return (
                        <MenuItem
                            key={key}
                            text={`${item.label}: ${item.value}`}
                        />
                    );
                }
                case itemTypes.CheckboxDropdown: {
                    return (
                        <MenuItem
                            text={item.menuLabel ? item.menuLabel : item.label}
                            icon={item.icon}
                            key={key}
                        >
                        <CheckboxDropdown
                            enumToCheckedMap={item.value}
                            enumToColorMap={item.valueToColorMap}
                            checkboxEnum={item.dropdownOptions}
                            onChange={value => item.onChange ? item.onChange(value) : undefined}
                        />
                        </MenuItem>
                    );
                }
                case itemTypes.LoadingSpinner: {
                    const displayString = `Loading ${item.value.howManyToLoad} ${item.label}`;
                    return (
                    <MenuItem
                        key={key}
                        text={displayString}
                    />);
                }
            default:
                // tslint:disable-next-line:no-console
                console.error('Invalid type for menu item');
        }
        return (<MenuItem/>);
    }
  /**
   * Generate the items for the toolbar
   * 
   * @param item Item to be rendered as widget
   * @param marginRightPx a set margin
   * @param menuItem Whether or not the item is being rendered in the overflow menu
   */
    // tslint:disable-next-line:cyclomatic-complexity
    private readonly renderItem = (item: ToolbarTypes.ToolbarItem,
        marginRightPx?: number, menuItem?: boolean): JSX.Element => {
        const itemTypes = ToolbarTypes.ToolbarItemType;
        switch (item.type) {
            case itemTypes.Dropdown: {
                return(
                    <DropDown
                        key={item.rank}
                        onMaybeValue={value => item.onChange ? item.onChange(value) : undefined}
                        value={item.value}
                        dropDownItems={item.dropdownOptions}
                        disabled={item.disabled}
                        widthPx={item.widthPx}
                        title={item.tooltip}
                    />
                );
            }
            // TODO make numeric input core component
            case itemTypes.NumericInput: {
                return (
                    <span key={item.rank}>
                    {
                        !item.labelRight ?
                        <span className="toolbar-numeric__label toolbar-numeric__label-left">
                            {item.label}
                        </span> : null
                    }
                    <NumericInput
                        value={item.value}
                        minMax={item.minMax}
                        disabled={item.disabled}
                        onChange={val => {
                            if (item.onChange) {
                            item.onChange(val);
                         }}}
                        widthPx={item.widthPx}
                        requireEnterForOnChange={item.requireEnterForOnChange}
                        step={item.step}
                        tooltip={item.tooltip}
                    />
                    {
                        item.labelRight ?
                            <span className="toolbar-numeric__label">
                                {item.labelRight}
                            </span> : null
                    }
                    </span>
                );
                break;
            }
            case itemTypes.IntervalPicker: {
                return (
                    <div className="toolbar-button-capsule" key={item.rank}>
                        <IntervalPicker
                            shortFormat={item.shortFormat}
                            startDate={item.value ? item.value.startDate : 0}
                            endDate={item.value ? item.value.endDate : 1}
                            onNewInterval={(startDate, endDate) =>
                                item.onChange ? item.onChange({startDate, endDate}) : undefined}
                            onApply={(startDate: Date, endDate: Date) =>
                                item.onApply ? item.onApply(startDate, endDate) : undefined}
                            defaultIntervalInHours={item.defaultIntervalInHours}
                        />
                    </div>
                );
                break;
            }
            case itemTypes.Popover: {
                return (
                    <PopoverButton
                        label={item.label}
                        tooltip={item.tooltip}
                        key={item.rank}
                        icon={item.icon}
                        onlyShowIcon={item.onlyShowIcon}
                        disabled={item.disabled}
                        popupContent={item.popoverContent ? item.popoverContent : (<div/>)}
                        onPopoverDismissed={() => item.onChange ? item.onChange(null) : undefined}
                        widthPx={item.widthPx}
                        ref={ref => {if (ref) {this.popoverButtonMap.set(item.rank, ref); }}}
                    />);
                break;
            }
            case itemTypes.Switch: {
                return (
                    <div
                        className="toolbar-switch"
                        title={item.tooltip}
                        key={item.rank}
                    >
                        <div>{`${item.label}:`}</div>
                        <Switch
                            title={item.tooltip}
                            disabled={item.disabled}
                            className={'toolbar-switch__blueprint'}
                            checked={item.value}
                            large={true}
                            onChange={e => item.onChange ? item.onChange(e.currentTarget.checked) : undefined}
                        />
                    </div>
                );
                break;
            }
            // TODO build a core component button
            // Use spread operator
            case itemTypes.Button: {
                const widthAsString = item.widthPx ? `${item.widthPx}px` : undefined;
                // TODO pull out button width of 30px into constants/config file
                const width = widthAsString ?
                                widthAsString
                                : item.onlyShowIcon ?
                                  '30px'
                                  : undefined;
                return (
                    <Button
                        disabled={item.disabled}
                        key={item.rank}
                        alignText={item.onlyShowIcon ? Alignment.CENTER : Alignment.LEFT}
                        onClick={e => item.onChange ? item.onChange(e.currentTarget) : undefined}
                        title={item.tooltip}
                        style={{
                            width,
                            marginRight: marginRightPx ? `${marginRightPx}px` : undefined
                        }}
                        className={item.onlyShowIcon ? 'toolbar-button--icon-only' : 'toolbar-button'}
                    >
                        <span>
                        {
                            item.onlyShowIcon ? undefined : item.label
                        }
                        </span>
                        {
                            item.icon ?
                            (<Icon
                                icon={item.icon}
                                title={false}
                            />)
                            : null
                        }
                    </Button>
                );
                break;
            }
            case itemTypes.ButtonGroup: {
                const indexOfLastButton = item.buttons ? item.buttons.length - 1 : 0;
                return (
                    <div className="toolbar-button-group" key={item.rank}>
                        {
                            item.buttons ?
                                item.buttons.map((button, index) => this.renderItem(
                                    button,
                                    index !== indexOfLastButton ?
                                        2 : undefined))
                                : null
                        }
                    </div>
                );
            }
            case itemTypes.LabelValue: {
                return (
                    <div className="toolbar-label-value">
                        <LabelValue
                            label={item.label}
                            value={item.value}
                            valueColor={item.valueColor ? item.valueColor : undefined}
                        />
                    </div>
                );
            }
            case itemTypes.CheckboxDropdown:
                return (
                    <PopoverButton
                        label={item.label}
                        tooltip={item.tooltip}
                        key={item.rank}
                        disabled={item.disabled}
                        popupContent={
                            (<CheckboxDropdown
                                enumToCheckedMap={item.value}
                                enumToColorMap={item.valueToColorMap}
                                checkboxEnum={item.dropdownOptions}
                                onChange={value =>
                                    item.onChange ? item.onChange(value) : undefined }
                            />)
                        }
                        onPopoverDismissed={() => item.onChange ? item.onChange(null) : undefined}
                        widthPx={item.widthPx}
                        ref={ref => {if (ref) {this.popoverButtonMap.set(item.rank, ref); }}}
                    />);
            case itemTypes.LoadingSpinner:
                return (
                    <LoadingSpinner
                        value={item.value}
                        label={item.label}
                        widthPx={item.widthPx}
                        key={item.rank}
                    />
                );
            default: {
                console.warn(`default error`);
            }
        }
        return (<div/>);
  }

    private getSizeOfItems () {
        const widthPx = this.toolbarItemRefs.length > 0 ?
            this.toolbarItemRefs.map(ref =>
                ref.getBoundingClientRect().width
            )
            .reduce((accumulator, currentValue) => accumulator + currentValue)
            : 0;
        return widthPx;
    }

    private getSizeOfLeftItems () {
        const widthPx = this.toolbarItemLeftRefs.length > 0 ?
            this.toolbarItemLeftRefs.map(ref =>
                ref.getBoundingClientRect().width
            )
            .reduce((accumulator, currentValue) => accumulator + currentValue)
            : 0;
        return widthPx;
    }

    private getSizeOfAllRenderedItems () {
        const totalWidth = this.getSizeOfLeftItems() +
            this.state.whiteSpaceAllotmentPx
            + this.getSizeOfItems();
        return totalWidth;
    }

  /* Handles toolbar re-sizing to ensure elements are always accessible */
    private readonly handleResize = () => {
        // Calculate the width of all rendered elements in the toolbar - our 'pixel debt'
        const totalWidth = this.getSizeOfAllRenderedItems();
        // Check to see how many pixels "overbudget" the toolbar is
        let overflowWidthPx = totalWidth - this.props.toolbarWidthPx + AMOUNT_OF_SPACE_TO_RESERVE_PX;
        let reduceWhiteSpaceTo = this.props.minWhiteSpacePx ? this.props.minWhiteSpacePx : 0;

        if (overflowWidthPx > 0) {
                // The first priority is to sacrifice whitespace, until the whitespace allocation === minWhiteSpacePx
            if (this.state.whiteSpaceAllotmentPx > reduceWhiteSpaceTo) {
                // The maximum amount of whitepsace we can get rid of
                const reducableWhiteSpacePx = this.state.whiteSpaceAllotmentPx - reduceWhiteSpaceTo;
                const reduceWhiteSpaceByPx =
                    reducableWhiteSpacePx <= overflowWidthPx ?
                        reducableWhiteSpacePx
                        : overflowWidthPx;
                overflowWidthPx -= reduceWhiteSpaceByPx;
                reduceWhiteSpaceTo = this.state.whiteSpaceAllotmentPx - reduceWhiteSpaceByPx;
                this.setState({
                    whiteSpaceAllotmentPx: reduceWhiteSpaceTo,
                    checkSizeOnNextDidMountOrDidUpdate: true});
            } else {
                // The next priority is to overflow right-aligned menu items into an overflow button
                // When we create an overflow button, it also takes up space, so we account for that
                overflowWidthPx += WIDTH_OF_OVERFLOW_BUTTON_PX;
                const indicesToOverflow: number[] = this.state.indicesToOverflow;
                // Loop backwards through our toolbar (higher rank = lower priority to render)
                for (let i = this.toolbarItemRefs.length - 1; i >= 0; i--) {
                    // If the item is already overflowed, then removing it won't reduce our 'debt'
                    if (this.state.indicesToOverflow.indexOf(i) >= 0) {
                        continue;
                    }
                    const item = this.toolbarItemRefs[i];
                    overflowWidthPx = overflowWidthPx - item.getBoundingClientRect().width;
                    // Push item to overflow list
                    indicesToOverflow.push(i);
                    if (overflowWidthPx <= 0) {
                        break;
                    } else {
                        continue;
                    }
                }
                this.setState({indicesToOverflow,
                               checkSizeOnNextDidMountOrDidUpdate: false});
            }
        } else {
            // If nothing is overflowed and the overflow is negative, add whitespace
            if (overflowWidthPx < 0 && this.state.indicesToOverflow.length === 0) {
                // If we have excess overflow to start, then we add to whitespace and end
                const surplus = Math.floor(Math.abs(overflowWidthPx));
                const increaseWhiteSpaceTo = this.state.whiteSpaceAllotmentPx + surplus;
                this.setState({ indicesToOverflow: [],
                                whiteSpaceAllotmentPx: increaseWhiteSpaceTo,
                                checkSizeOnNextDidMountOrDidUpdate: false});
            } else if (overflowWidthPx !== 0) {
                const resizeNextTime = !this.state.checkSizeOnNextDidMountOrDidUpdate;
                this.setState({ indicesToOverflow: [],
                                whiteSpaceAllotmentPx: reduceWhiteSpaceTo,
                                checkSizeOnNextDidMountOrDidUpdate: resizeNextTime});
            } else {
                this.setState({checkSizeOnNextDidMountOrDidUpdate: false});
            }
        }
    }
}
