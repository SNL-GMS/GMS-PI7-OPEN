import { IconNames } from '@blueprintjs/icons';
import * as Gl from '@gms/golden-layout';
import { HistoryList, HistoryListTypes, TimeUtil, Toolbar, ToolbarTypes } from '@gms/ui-core-components';
import * as React from 'react';
import { ExpansionState } from '../types';
const SIZE_OF_MENU_BAR_PADDING_PX = 25;
const currentTimeId = 'Last 24 Hours';
const TWENTY_FOUR_HOURS_MS = 86400000;
/**
 * WorkflowMenuBar Props
 */
export interface WorkflowMenuBarProps {
  expansionStates: ExpansionState[];
  startTimeSecs: number;
  endTimeSecs: number;

  glContainer?: Gl.Container;
  setExpanded(expandAll: boolean);
  onNewInterval(startDate: Date, endDate: Date);
  onToast(message: string);
}
export interface IntervalHistory extends HistoryListTypes.HistoryListItem {
  startDate: Date;
  endDate: Date;
}
/**
 * WorkflowMenuBar State
 */
export interface WorkflowMenuBarState {
  startDate: Date;
  endDate: Date;
  history: IntervalHistory[];
  currentTime: {
    startDate: Date;
    endDate: Date;
  };
}

const MILLIS_SEC = 1000;

export class WorkflowMenuBar extends React.Component<
  WorkflowMenuBarProps,
  WorkflowMenuBarState
  > {
  // Lets us assign temporary id's to history entries
  private counter: number = 0;
  /**
   * Constructor.
   *
   * @param props The initial props
   */
  public constructor(props: WorkflowMenuBarProps) {
    super(props);
    const now = new Date();
    const then = new Date(now.valueOf() - TWENTY_FOUR_HOURS_MS);
    this.state = {
      startDate: new Date(this.props.startTimeSecs * MILLIS_SEC),
      endDate: new Date(this.props.endTimeSecs * MILLIS_SEC),
      history: [],
      currentTime: {startDate: then, endDate: now}
    };
  }

  public componentDidUpdate(prevProps: WorkflowMenuBarProps, prevState: WorkflowMenuBarState) {
    if (this.props.startTimeSecs !== prevProps.startTimeSecs ||
      this.props.endTimeSecs !== prevProps.endTimeSecs) {
        this.setState({
          ...this.state,
          startDate: new Date(this.props.startTimeSecs * MILLIS_SEC),
          endDate: new Date(this.props.endTimeSecs * MILLIS_SEC),
        });
      }
  }
  public render() {
    const isAnyExpanded = this.props.expansionStates.reduce(
      (prev, cur) => (cur.expanded ? cur.expanded : prev),
      false
    );
    const recentHistory: IntervalHistory[] = [{
      index: 0,
      label: 'Last 24 Hours',
      ...this.state.currentTime,
      id: currentTimeId,
    }];
    const historyDropDown = (
      <HistoryList
        items={this.state.history}
        preferredItems={recentHistory}
        onSelect={this.onHistoryItemSelect}
      />
    );
    const toolbarItems: ToolbarTypes.ToolbarItem[] = [
      {
        label: isAnyExpanded ? 'Collapse all' : 'Expand all ',
        tooltip: 'Expand / Collapse all stages',
        type: ToolbarTypes.ToolbarItemType.Button,
        widthPx: 114,
        rank: 3,
        onChange: () => {
          const isUpToDate = this.props.expansionStates.reduce(
            (prev, cur) => (cur.expanded ? cur.expanded : prev),
            false
          );
          this.props.setExpanded(!isUpToDate);
        },
        icon: isAnyExpanded ? IconNames.COLLAPSE_ALL : IconNames.EXPAND_ALL
      },
      {
        label: 'Workflow Intervals',
        tooltip: 'Set start and end of intervals to display in workflow',
        type: ToolbarTypes.ToolbarItemType.IntervalPicker,
        rank: 1,
        value: {
          startDate: this.state.startDate,
          endDate: this.state.endDate
        },
        shortFormat: true,
        onChange: (interval: any) => {
          this.onNewInterval(interval.startDate, interval.endDate);
        },
        onApply: (startDate: Date, endDate: Date) => {
          this.applyNewInterval({startDate, endDate});
        },
        defaultIntervalInHours: 24
      },
      {
        label: 'Intervals',
        tooltip: 'Shows list of previously viewed time ranges',
        type: ToolbarTypes.ToolbarItemType.Popover,
        menuLabel: 'Intervals',
        rank: 2,
        popoverContent: historyDropDown,
        widthPx: 95,
        onChange: () => {
          return;
        }
    }
    ];
    return (
      <div className="workflow__menu-bar">
        <Toolbar
          toolbarWidthPx={this.props.glContainer ? (this.props.glContainer.width - SIZE_OF_MENU_BAR_PADDING_PX) : 0}
          items={toolbarItems}
        />
      </div>
    );
  }
  /**
   * Applies new inteval, first internally and then to the gateway
   * 
   * @param value an any with a startDate and endDate. Untyped because the toolbar returns a generic value
   */
  private readonly applyNewInterval = (value: any) => {
    // If the new interval is the same as the current interval, ignore
    if (value.startDate.valueOf() / MILLIS_SEC  !== this.props.startTimeSecs ||
    value.endDate.valueOf() / MILLIS_SEC !== this.props.endTimeSecs) {
      // Try to find new interval in the history
      const historyList = this.state.history;
      const maybeEntry = historyList.find(
        his => his.startDate.valueOf() === this.props.startTimeSecs * MILLIS_SEC &&
        his.endDate.valueOf() === this.props.endTimeSecs * MILLIS_SEC
      );
      if (maybeEntry) {
        // If found, reshuffles the history entries
        const listWithoutEntry =
          historyList.filter(his => his.startDate !== maybeEntry.startDate || his.endDate !== maybeEntry.endDate);
        const newEntry: IntervalHistory = {
          ...maybeEntry,
          index: historyList.length
        };
        const newList = [newEntry, ...listWithoutEntry];
        this.setState({startDate: value.startDate, endDate: value.endDate, history: newList}, () => {
          this.props.onNewInterval(value.startDate, value.endDate);
        });
      } else {
        // Creates new history entry from the current props
        const startDate = new Date(this.props.startTimeSecs * MILLIS_SEC);
        const endDate = new Date(this.props.endTimeSecs * MILLIS_SEC);
        this.counter++;
        historyList.push({
          id: this.counter
              .toString(),
          index: historyList.length,
          label: this.generateLabelForHistory(startDate, endDate),
          startDate,
          endDate
        });
        this.setState({startDate: value.startDate, endDate: value.endDate, history: historyList}, () => {
          this.props.onNewInterval(value.startDate, value.endDate);
        });
      }
    }
  }
  /**
   * Sets state for the interval picker - does not call gaetway
   * @param startDate new start date to show in interval picker
   * @param endDate new end date to show in interval picker
   */
  private readonly onNewInterval = (startDate: Date, endDate: Date) => {
    this.setState({...this.state,
                   startDate,
                   endDate
    });
  }
  /**
   * When a history item is selected, look up the entry in the state
   * and set a set interval if needed
   * @param id the id of the entry in the state
   */
  private readonly onHistoryItemSelect = (id: string) => {
    if (id === currentTimeId) {
      this.applyNewInterval({startDate: this.state.currentTime.startDate,
                             endDate: this.state.currentTime.endDate});
    } else {
      const item = this.state.history.find(i => i.id === id);
      if (item) {
        this.applyNewInterval({startDate: item.startDate, endDate: item.endDate});
      }
    }

  }
  /**
   * Creates a label for the item in the workflow history
   * @param startDate Start date of the item
   * @param endDate End date of the item
   */
  private readonly generateLabelForHistory = (startDate: Date, endDate: Date) => {
    const reactLabel = (
      <span className="history-item">
        <span className="history-item__date-string">
          {TimeUtil.dateToShortISOString(startDate)}
        </span>
        <span
          className="history-item__text"
        >&nbsp;to&nbsp;
        </span>
        <span className="history-item__date-string">
          {TimeUtil.dateToShortISOString(endDate)}
        </span>
      </span>
    );
    return reactLabel;
  }

}
