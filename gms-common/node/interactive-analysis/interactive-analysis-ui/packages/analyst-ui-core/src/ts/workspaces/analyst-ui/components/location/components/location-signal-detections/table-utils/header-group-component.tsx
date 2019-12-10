import { Checkbox } from '@blueprintjs/core';
import * as React from 'react';
import { DefiningTypes, SdDefiningStates } from '../types';

/**
 * Renders the header for the various defining types
 */
export class DefiningHeader extends React.Component<any> {
    public constructor(props) {
        super(props);

    }
    public render() {
        const definingState: SdDefiningStates = this.props.definingState;
        const definingType: DefiningTypes = this.props.definingType;
        return (
            <div
                className="location-sd-header"
            >
                <div>
                    {
                        definingType === DefiningTypes.ARRIVAL_TIME ?
                            'Time'
                            : definingType === DefiningTypes.AZIMUTH ?
                                'Azimuth'
                                : 'Slowness'
                    }
                </div>
                <div
                     className="location-sd-subdivder"
                >
                    Def All:
                    <Checkbox
                        checked={definingState === SdDefiningStates.ALL}
                        onClick={() => {this.props.definingCallback(true, definingType); }}
                        className="location-sd-checkbox"
                    />
                    None:
                    <Checkbox
                        checked={definingState === SdDefiningStates.NONE}
                        onClick={() => {this.props.definingCallback(false, definingType); }}
                        className="location-sd-checkbox"
                    />
                </div>
            </div>
        );
    }

}
