import { Intent, Spinner } from '@blueprintjs/core';
import * as React from 'react';
import { LoadingSpinnerProps } from './types';
// A loading spinner widget to be used in toolbars the world over
export const LoadingSpinner: React.StatelessComponent<LoadingSpinnerProps> = props =>
(
    <div
        className="loading-spinner__container"
        style={{
            minWidth: `${props.widthPx}px`
        }}
    >
        {
            props.value.itemsToLoad > 0 ?
                <span>
                    <Spinner
                        intent={Intent.PRIMARY}
                        small={true}
                        value={props.value.itemsLoaded ? props.value.itemsLoaded / props.value.itemsToLoad : undefined}
                    />
                    <span>
                        {props.value.hideTheWordLoading ? '' : 'Loading'} 
                        {props.value.hideOutstandingCount ? props.value.itemsToLoad : ''} {props.label}...
                    </span>
                </span>
            : null
        }
    </div>
);
