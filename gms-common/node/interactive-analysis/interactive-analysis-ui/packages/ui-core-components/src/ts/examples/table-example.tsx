import * as React from 'react';
import { Row, Table } from '../components';

/**
 * Example displaying how to use the Table component.
 */
export class TableExample extends React.Component<{}, {}> {
  public constructor(props: {}) {
    super(props);
  }

  /**
   * React render method
   */
  /* tslint:disable:no-magic-numbers */
  public render() {
    const columnDefs = [
      {
          headerName: '',
          field: 'key',
          width: 75
      },
      {
          headerName: 'Column A',
          field: 'columnA',
          width: 200
      },
      {
          headerName: 'Column B',
          field: 'columnB',
          width: 200
      },
      {
          headerName: 'Column C',
          field: 'columnC',
          width: 200
      }
  ];

    return (
      <div
        className="ag-dark"
        style={{
          flex: '1 1 auto',
          position: 'relative',
          height: '250px',
          width: '700px',
        }}
      >
        <div
          style={{
            position: 'absolute',
            top: '0px',
            right: '0px',
            bottom: '0px',
            left: '0px',
          }}
        >
        <Table
          columnDefs={columnDefs}
          rowData={this.getRowData()}
          getRowNodeId={node => node.id}
          deltaRowDataMode={true}
          overlayNoRowsTemplate="No data available"
        />
        </div>
      </div>
    );
  }

  /**
   * Generate dummy data for the table
   */
  private readonly getRowData = (): Row[] => {
    const dataRows: Row[] = [];
    for (let i = 0; i < 10; i++) {
      const row: any = {
        id: i,
        key: i,
        columnA: `Column A Sample Data ${i}`,
        columnB: `Column B Sample Data ${i}`,
        columnC: `Column C Sample Data ${i}`
      };
      dataRows.push(row);
    }

    return dataRows;
  }
}
