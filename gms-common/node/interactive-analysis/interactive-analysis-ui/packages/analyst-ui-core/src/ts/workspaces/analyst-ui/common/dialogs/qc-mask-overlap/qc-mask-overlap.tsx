import { flatten } from 'lodash';
import * as React from 'react';
import { QcMaskCategory, QcMaskType } from '~analyst-ui/config/system-config';
import { userPreferences } from '~analyst-ui/config/user-preferences';
import { QcMaskTypes } from '~graphql/';
import { renderOverlappingMaskTable } from '../qc-mask-form/utils';
import { QcMaskDialogBoxType, QcMaskHistoryRow } from '../types';
import { QcMaskOverlapProps, QcMaskOverlapState } from './types';

/**
 * QcMaskDetails Component
 */
export class QcMaskOverlap extends React.Component<QcMaskOverlapProps, QcMaskOverlapState> {

  /**
   * Constructor
   */
  public constructor(props: QcMaskOverlapProps) {
    super(props);
    if (props.masks.length === 1) {
      this.state = {
        selectedMask: props.masks[0]
      };
    } else {
      this.state = {
        selectedMask: undefined
      };
    }

  }

  /**
   * React component lifecycle
   */
  public render() {
    return (
      <div
        className="qc-mask-modify"
      >
        {this.renderMultipleOverlappingMasks()}
      </div>
    );
  }

  private readonly renderMultipleOverlappingMasks = (): JSX.Element => (
    <div>
      <h6 className="qc-mask-modify__label">
        QC Masks
      </h6>
      {renderOverlappingMaskTable({
        rowData: this.generateMaskTableRows(this.props.masks),
        overlayNoRowsTemplate: 'No Masks',
        onRowDoubleClicked: params => {
          this.doubleClicked(params);
        }
      })}
    </div>
  )

  /**
   * Generate the table row data for masks.
   * 
   * @param masks array of masks of type QcMaskTypes.QcMask
   * 
   * @returns array of QcMaskHistoryRow
   */
  private readonly generateMaskTableRows = (masks: QcMaskTypes.QcMask[]): QcMaskHistoryRow[] =>
    flatten(masks.map(m => ({
      id: m.id,
      versionId: m.currentVersion.version,
      color: userPreferences.colors.waveforms.maskDisplayFilters[m.currentVersion.category].color,
      creationTime: m.currentVersion.creationInfo.creationTime,
      author: m.currentVersion.creationInfo.creatorId,
      category: userPreferences.colors.waveforms.maskDisplayFilters[m.currentVersion.category].name,
      type: QcMaskType[m.currentVersion.type],
      startTime: m.currentVersion.startTime,
      endTime: m.currentVersion.endTime,
      channelSegmentIds: m.currentVersion.channelSegmentIds.join(', '),
      rationale: m.currentVersion.rationale,
      modify: {
        onClick: this.maskClicked,
        disabled: QcMaskCategory[m.currentVersion.category] === QcMaskCategory.REJECTED,
        qcDialogType:
          QcMaskCategory[m.currentVersion.category] === QcMaskCategory.REJECTED ?
            QcMaskDialogBoxType.View
            : QcMaskDialogBoxType.Modify
      },
      reject: {
        onClick: this.maskClicked,
        disabled: QcMaskCategory[m.currentVersion.category] === QcMaskCategory.REJECTED,
        qcDialogType: QcMaskDialogBoxType.Reject
      },
      select: {
        onClick: this.selectMask,
        disabled: QcMaskCategory[m.currentVersion.category] === QcMaskCategory.REJECTED,
      }
    })))

  private readonly maskClicked = (x: number, y: number, params: any): void => {
    // If contextMenuCoordinates are set, use them to position new context menu
    if (this.props.contextMenuCoordinates) {
      this.props.openNewContextMenu(
        this.props.contextMenuCoordinates.xPx, this.props.contextMenuCoordinates.yPx,
        this.props.masks.find(m => m.id === params.data.id),
        params.value.qcDialogType);

    } else {
      this.props.openNewContextMenu(
        x, y,
        this.props.masks.find(m => m.id === params.data.id),
        params.value.qcDialogType);
    }
  }

  private readonly selectMask = (x: number, y: number, params: any): void => {
    this.props.selectMask(this.props.masks.find(m => m.id === params.data.id));
  }

  private readonly doubleClicked = (params: any): void => {
    const dialogType = QcMaskCategory[params.data.category.toUpperCase()] === QcMaskCategory.REJECTED ?
      QcMaskDialogBoxType.View
      : QcMaskDialogBoxType.Modify;
    // If contextMenuCoordinates are set, use them to position new context menu
    if (this.props.contextMenuCoordinates) {
      this.props.openNewContextMenu(
        this.props.contextMenuCoordinates.xPx, this.props.contextMenuCoordinates.yPx,
        this.props.masks.find(m => m.id === params.data.id),
        dialogType);

    } else {
      this.props.openNewContextMenu(
        params.event.clientX, params.event.clientY,
        this.props.masks.find(m => m.id === params.data.id),
        dialogType);
    }
  }
}
