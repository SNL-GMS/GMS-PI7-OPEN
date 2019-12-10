import * as React from 'react';
import { LabelValueProps } from './types';

export const LabelValue: React.StatelessComponent<LabelValueProps> = props =>
(
    <div className="label-value-container">
        <div className="label-value__label">
            {props.label}:
        </div>
        <div
            className="label-value__value"
            style={{
                color: props.valueColor ? props.valueColor : ''
            }}
        >
            {props.value}
        </div>
    </div>
);
