import { Intent, NonIdealState, Spinner } from '@blueprintjs/core';
import { Table } from '@gms/ui-core-components';
import classNames from 'classnames';
import * as lodash from 'lodash';
import memoizeOne from 'memoize-one';
import * as React from 'react';
import { DataAcquisitionTypes } from '~graphql/';
import { columnDefs } from './table-utils/column-defs';
import { TransferGapRowChannel, TransferGapsProps, TransferGapsRow, TransferGapsState } from './types';

export class TransferGaps extends React.Component<TransferGapsProps, TransferGapsState> {

  /** 
   * A memoization function that caches the results using the most recent argument and returns
   * the TransferGaps data. 
   */
  private readonly memoizedTransferGapsData: (fileGaps: DataAcquisitionTypes.FileGap[])
  => TransferGapsRow[];

  private idPefix: number = 1;

  /**
   * constructor
   */
  public constructor(props: TransferGapsProps) {
    super(props);
    this.memoizedTransferGapsData = memoizeOne(this.generateTableData);

    this.state = {
      getNodeChildDetails: function getNodeChildDetails(rowItem) {
        if (rowItem.channels) {
          return {
            group: true,
            children: rowItem.channels,
            key: rowItem.station
          };
        } else {
          return null;
        }
      }
    };
  }

  // ***************************************
  // BEGIN REACT COMPONENT LIFECYCLE METHODS
  // ***************************************

  /**
   * Renders the component.
   */
  public render() {
    if (this.props.transferredFilesByTimeRangeQuery.loading) {
      return (
        <NonIdealState
          action={<Spinner intent={Intent.PRIMARY} />}
          title="Loading:"
          description={'Transfer Gaps...'}
        />
      );
    }

    // Generates the table row data, if no props have chnaged, uses cached value.
    const tableData = this.memoizedTransferGapsData(this.props.transferredFilesByTimeRangeQuery ?
      this.props.transferredFilesByTimeRangeQuery.transferredFilesByTimeRange : undefined);

    return (
      <div
        className={classNames('ag-theme-dark', 'table-container')}
      >
        <div className={'list-wrapper'}>
          <div className={'max'}>
            <Table
              context={{}}
              columnDefs={columnDefs}
              rowData={tableData}
              getRowNodeId={node => node.id}
              deltaRowDataMode={true}
              rowSelection="multiple"
              rowDeselection={true}
              suppressContextMenu={true}
              getNodeChildDetails={this.state.getNodeChildDetails}
            />
          </div>
        </div>
      </div >
    );
  }
  // ***************************************
  // END REACT COMPONENT LIFECYCLE METHODS
  // ***************************************

  /**
   * Generate TransferGapsRow[] based of stations returned from query.
   *  
   * @param fileGaps file gaps
   * 
   * @returns TransferGapsRow[]
   */
  private readonly generateTableData = (fileGaps: DataAcquisitionTypes.FileGap[]): TransferGapsRow[] => {
    let transferGapsListData = [];
    if (fileGaps) {
      fileGaps.forEach(gap => {
        const chan = this.getChannelsForSites(gap);
        const tempTransferGapsRow: TransferGapsRow = {
          id: `${this.idPefix}${gap.stationName}${gap.duration}`,
          name: gap.stationName,
          channels: chan,
          priority: gap.priority,
          gapStartTime: gap.startTime,
          gapEndTime: gap.endTime,
          duration: gap.duration + 'ms',
          location: gap.location
        };
        transferGapsListData.push(tempTransferGapsRow);
      });
      transferGapsListData = lodash.sortBy(transferGapsListData, 'name');
    }

    return transferGapsListData;
  }

  /**
   * Converts channel Names to row object
   * 
   * @param gap a file gap object
   */
  private readonly getChannelsForSites = (gap: DataAcquisitionTypes.FileGap): TransferGapRowChannel[] => {
    let channels = [];
    gap.channelNames.forEach(channelName => {
      const channelRow = {
        id: `${++this.idPefix}`,
        name: channelName,
        priority: gap.priority,
        gapStartTime: gap.startTime,
        gapEndTime: gap.endTime,
        duration: gap.duration + 'ms',
        location: gap.location
      };
      channels.push(channelRow);
    });
    return channels = lodash.sortBy(channels, 'name');
  }
}
