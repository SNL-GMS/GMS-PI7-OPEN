import { AgGridReact, AgGridReactProps } from 'ag-grid-react';
import * as React from 'react';

import './css/ag-grid-overrides.scss';

/**
 * Table component that wraps AgGrid React.
 */
export class Table extends React.PureComponent<AgGridReactProps, {}> {

    /**
     * Currently, just wrapping a Ag-grid table.
     */
    public render() {
        return (
            <AgGridReact
                {...this.props}
                suppressContextMenu={true}
                suppressAsyncEvents={true}
            />
        );
    }

}
