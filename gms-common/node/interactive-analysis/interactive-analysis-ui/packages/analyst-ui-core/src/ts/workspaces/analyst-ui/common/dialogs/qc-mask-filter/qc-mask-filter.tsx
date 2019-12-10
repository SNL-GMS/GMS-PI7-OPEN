import { Checkbox } from '@blueprintjs/core';
import * as React from 'react';
import { MaskDisplayFilter } from '~analyst-ui/config/user-preferences';
import { QcMaskFilterProps } from './types';

export class QcMaskFilter extends React.Component<QcMaskFilterProps> {

  public constructor(props: QcMaskFilterProps) {
    super(props);
  }

  public render() {
    return (
      <div>
          <table className="qc-mask-legend__body">
            <tbody>
              {
                Object.keys(this.props.maskDisplayFilters)
                  .map(key => (
                    <tr key={key}>
                      <td>
                        <Checkbox
                          className={'qc-mask-legend-table__checkbox'}
                          defaultChecked={this.props.maskDisplayFilters[key].visible}
                          onChange={() =>
                            this.onChange(key, this.props.maskDisplayFilters[key])}
                        />
                      </td>
                      <td>
                        {this.props.maskDisplayFilters[key].name}
                      </td>
                      <td>
                        <div
                          className={'qc-mask-legend-table__legend-box'}
                          style={{
                            backgroundColor: this.props.maskDisplayFilters[key].color
                          }}
                        />
                      </td>
                    </tr>
                  ))
              }
            </tbody>
          </table>
      </div>
    );
  }

  private readonly onChange = (key: string, maskDisplayFilter: MaskDisplayFilter) => {
    maskDisplayFilter.visible = !maskDisplayFilter.visible;
    this.props.setMaskDisplayFilters(key, maskDisplayFilter);
  }
}
