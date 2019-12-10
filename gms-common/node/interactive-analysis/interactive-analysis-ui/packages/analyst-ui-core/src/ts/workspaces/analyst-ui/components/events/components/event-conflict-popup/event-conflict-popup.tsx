import { TimeUtil } from '@gms/ui-core-components';
import * as React from 'react';
import { findArrivalTimeFeatureMeasurementValue,
  findPhaseFeatureMeasurementValue } from '~graphql/signal-detection/utils';
import { SignalDetectionHypothesisWithStation } from '../../types';

const THOUSAND = 1000;

export interface EventConflictPopupProps {
    signalDetectionHyps: SignalDetectionHypothesisWithStation[];
}
/**
 * Displays signal detection information in tabular form
 */
export class EventConflictPopup extends React.Component<EventConflictPopupProps, {}> {

  // ***************************************
  // BEGIN REACT COMPONENT LIFECYCLE METHODS
  // ***************************************

  /**
   * Constructor.
   * 
   * @param props The initial props
   */
  public constructor(props) {
    super(props);
  }

  /**
   * Renders the component.
   */
  public render() {

    return (
        <div>
            <div>Signal Detection(s) in conflict:</div>
            <ul>
              {this.props.signalDetectionHyps.map(sdh => (
                <li
                  key={sdh.id}
                >
                 {findPhaseFeatureMeasurementValue(sdh.featureMeasurements) ?
                    findPhaseFeatureMeasurementValue(sdh.featureMeasurements).phase : ''} on&nbsp;
                  {sdh.stationName}&nbsp;
                   at {findArrivalTimeFeatureMeasurementValue(sdh.featureMeasurements) ?
                    TimeUtil.dateToHoursMinutesSeconds(
                      new Date(findArrivalTimeFeatureMeasurementValue(sdh.featureMeasurements).value * THOUSAND))
                    : ''}
                </li>
              ))}
            </ul>
        </div >
    );
  }
}
