import { ContextMenu, NonIdealState } from '@blueprintjs/core';
import { Form, FormTypes, Table, TimeUtil } from '@gms/ui-core-components';
import * as classNames from 'classnames';
import { flatten } from 'lodash';
import * as React from 'react';
import { SignalDetectionTypes } from '~graphql/';
import { FeatureMeasurementTypeName } from '~graphql/signal-detection/types';
import {
  findArrivalTimeFeatureMeasurement,
  findArrivalTimeFeatureMeasurementValue,
  findPhaseFeatureMeasurementValue
} from '~graphql/signal-detection/utils';
import {
  SIGNAL_DETECTION_HISTORY_COLUMN_DEFINITIONS
} from './constants';
import {
  SignalDetectionDetailsProps,
  SignalDetectionDetailsState,
  SignalDetectionHistoryRow
} from './types';
import { formatUncertainty } from './utils';

/**
 * SignalDetectionDetails Component
 */
export class SignalDetectionDetails extends React.Component<SignalDetectionDetailsProps, SignalDetectionDetailsState> {

  /**
   * Constructor
   */
  public constructor(props: SignalDetectionDetailsProps) {
    super(props);
    this.state = {
      showHistory: false
    };
  }

  /**
   * React component lifecycle
   */
  public render() {
    if (!this.props.detection) {
      return (
        <NonIdealState />
      );
    } else {
      const formItems: FormTypes.FormItem[] = [];
      const detection = this.props.detection;
      const arrivalTimeFM =
        findArrivalTimeFeatureMeasurement(detection.currentHypothesis.featureMeasurements);
      const arrivalTimeFeatureMeasurementValue =
        findArrivalTimeFeatureMeasurementValue(detection.currentHypothesis.featureMeasurements);
      const fmPhase = findPhaseFeatureMeasurementValue(detection.currentHypothesis.featureMeasurements);
      formItems.push({
        itemKey: 'Phase',
        labelText: 'Phase',
        displayText: fmPhase.phase.toString(),
        itemType: FormTypes.ItemType.Display
      });
      formItems.push({
        itemKey: 'Detection time',
        labelText: 'Detection time',
        itemType: FormTypes.ItemType.Display,
        displayText:
          TimeUtil.timesecsToISOString(
            TimeUtil.toDate(arrivalTimeFeatureMeasurementValue.value)),
        displayTextFormat: FormTypes.TextFormats.Time
      });
      formItems.push({
        itemKey: 'Time uncertainty',
        labelText: 'Time uncertainty',
        itemType: FormTypes.ItemType.Display,
        displayText:
          formatUncertainty(arrivalTimeFM.uncertainty),
      });
      formItems.push({
        itemKey: 'Author',
        labelText: 'Author',
        itemType: FormTypes.ItemType.Display,
        displayText: detection.currentHypothesis.creationInfo.creatorId,
      });
      formItems.push({
        itemKey: 'Creation time',
        labelText: 'Creation time',
        itemType: FormTypes.ItemType.Display,
        displayText:
        TimeUtil.timesecsToISOString(
          TimeUtil.toDate(detection.currentHypothesis.creationInfo.creationTime)),
        displayTextFormat: FormTypes.TextFormats.Time
      });
      formItems.push({
        itemKey: 'Rejected',
        labelText: 'Rejected',
        itemType: FormTypes.ItemType.Display,
        displayText: (detection.currentHypothesis.rejected) ? 'Yes' : 'No',
      });

      const defaultPanel: FormTypes.FormPanel = {
        formItems,
        key: 'Current Version'
      };
      const extraPanels: FormTypes.FormPanel[] = [
        {
          key: 'All Versions',
          content: this.renderTable(
            {
              rowData: this.generateDetectionHistoryTableRows(detection),
              overlayNoRowsTemplate: 'No Verions',
              rowClassRules: {
                'versions-table__row--first-in-table': params => {
                  if (params.data['first-in-table']) {
                    return true;
                  } else {
                    return false;
                  }
                }
              }
            })
        }
      ];

      return (
        <div>
          <Form
            header={'Signal Detection'}
            headerDecoration={
              (
                <div
                  className="signal-detection-swatch"
                  style={{ backgroundColor: this.props.color }}
                />
              )
            }
            defaultPanel={defaultPanel}
            disableSubmit={true}
            onCancel={() => { ContextMenu.hide(); }}
            extraPanels={extraPanels}
          />
        </div>
      );
    }

  }

  /**
   * Render the Detection table.
   */
  private readonly renderTable = (tableProps: {}) => (
    <div
      className={classNames('ag-theme-dark', 'signal-detection-details-versions-table')}
    >
      <div className={'max'}>
        <Table
          columnDefs={SIGNAL_DETECTION_HISTORY_COLUMN_DEFINITIONS}
          getRowNodeId={node => node.id}
          rowSelection="single"
          {...tableProps}
        />
      </div>
    </div>
  )

  /**
   * Generate the table row data for the detection hisory.
   */
  private readonly generateDetectionHistoryTableRows =
    (detection: SignalDetectionTypes.SignalDetection): SignalDetectionHistoryRow[] => {
      const rows = flatten(detection.signalDetectionHypothesisHistory.map(detectionHistory =>
        ({
          id: detectionHistory.id,
          versionId: detectionHistory.id,
          phase: detectionHistory.phase,
          rejected: detectionHistory.rejected,
          arrivalTimeMeasurementFeatureType: FeatureMeasurementTypeName.ARRIVAL_TIME,
          arrivalTimeMeasurementTimestamp: detectionHistory.arrivalTimeSecs,
          arrivalTimeMeasurementUncertaintySec: detectionHistory.arrivalTimeUncertainty,
          creationTime: detectionHistory.creationInfo.creationTime,
          author: detectionHistory.creationInfo.creatorId
        }))
      .sort((a, b) => b.creationTime - a.creationTime)
      );
      rows[0]['first-in-table'] = true;
      return rows;
    }
}
