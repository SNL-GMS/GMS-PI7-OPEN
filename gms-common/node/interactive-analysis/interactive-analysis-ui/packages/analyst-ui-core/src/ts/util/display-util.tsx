import * as React from 'react';

/**
 * Creates the HTML for the dropdown items for a dropdown
 * 
 * @returns A list of JSX.Element drop down items
 */
export function createDropdownItems(dropdownOptions): JSX.Element[] {
  return Object.keys(dropdownOptions)
    .map(optionValue => (
      <option
        key={optionValue}
        value={dropdownOptions[optionValue]}
      >
        {dropdownOptions[optionValue]}
      </option>
    ));
}
