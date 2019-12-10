import { NonIdealState } from '@blueprintjs/core';
import * as React from 'react';
import { getFkData } from '~analyst-ui/common/utils/fk-utils';
import { SignalDetectionTypes } from '~graphql/';
import { FeaturePrediction } from '~graphql/event/types';
import { findArrivalTimeFeatureMeasurementValue,
         findPhaseFeatureMeasurementValue,
        } from '~graphql/signal-detection/utils';
import { FkUnits } from '../../types';
import { FkThumbnail } from '../fk-thumbnail';
import * as fkUtil from '../fk-util';

/**
 * Fk Thumbnail Props.
 */
export interface FkThumbnailContainerProps {
  data: SignalDetectionTypes.SignalDetection;
  signalDetectionFeaturePredictions: FeaturePrediction[];
  sizePx: number;
  selected: boolean;
  isUnassociated: boolean;
  fkUnit: FkUnits;
  showFkThumbnailMenu?(x: number, y: number): void;
  onClick?(e: React.MouseEvent<HTMLDivElement>): void;
}

/**
 * A single fk thumbnail in the thumbnail-list
 */
export class FkThumbnailContainer extends React.Component<FkThumbnailContainerProps> {

  // ***************************************
  // BEGIN REACT COMPONENT LIFECYCLE METHODS
  // ***************************************

  /**
   * React component lifecycle.
   */
  public render() {
    const fmPhase = findPhaseFeatureMeasurementValue(this.props.data.currentHypothesis.featureMeasurements);
    if (!this.props.data) {
      return (
        <NonIdealState
          visual="heat-grid"
          title="All Fks Filtered Out"
        />
      );
    }
    const needsReview = fkUtil.fkNeedsReview(this.props.data);
    const label = `${this.props.data.station.name}` +
      ` ${fmPhase.phase.toString()}`;
    const fkData = getFkData(this.props.data.currentHypothesis.featureMeasurements);
    const arrivalTime: number =
      findArrivalTimeFeatureMeasurementValue(this.props.data.currentHypothesis.featureMeasurements).value;
    const predictedPoint = fkUtil.getPredictedPoint(this.props.signalDetectionFeaturePredictions);

    return (
      <FkThumbnail
        fkData={fkData}
        label={label}
        dimFk={this.props.isUnassociated}
        highlightLabel={needsReview}
        fkUnit={this.props.fkUnit}
        arrivalTime={arrivalTime}
        sizePx={this.props.sizePx}
        onClick={this.props.onClick}
        predictedPoint={predictedPoint}
        selected={this.props.selected}
        showFkThumbnailMenu={this.props.showFkThumbnailMenu}
      />
    );
  }
}
