import { Checkbox } from '@blueprintjs/core';
import * as React from 'react';
import { CheckboxDropdownProps } from '.';

export class CheckboxDropdown extends React.Component<CheckboxDropdownProps> {

  public constructor(props: CheckboxDropdownProps) {
    super(props);
  }
  public render() {
    return (
      <div>
          <table className="checkbox-dropdown__body">
            <tbody>
              {
                Object.keys(this.props.checkboxEnum)
                  .map(key => (
                    <tr key={key}>
                      <td>
                        <Checkbox
                          className={'checkbox-dropdown__checkbox'}
                          defaultChecked={this.props.enumToCheckedMap.get(this.props.checkboxEnum[key])}
                          onChange={() =>
                            this.onChange(this.props.checkboxEnum[key])}
                        />
                      </td>
                      <td>
                        {this.props.checkboxEnum[key]}
                      </td>
                      {
                          this.props.enumToColorMap ?
                            <td>
                                <div
                                    className={'checkbox-dropdown__legend-box'}
                                    style={{
                                        backgroundColor: this.props.enumToColorMap[key]
                                    }}
                                />
                            </td>
                            : null
                      }
                    </tr>
                  ))
              }
            </tbody>
          </table>
      </div>
    );
  }

  private readonly onChange = (value: any) => {
    this.props.onChange(value);
  }
}
