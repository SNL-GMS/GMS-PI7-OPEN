
import * as Immutable from 'immutable';
import { findIndex, findLastIndex } from 'lodash';
import * as React from 'react';
import { getFkUnitForSdId } from '~analyst-ui/common/utils/fk-utils';
import { SignalDetectionTypes, StationTypes } from '~graphql/';
import { FeaturePrediction } from '~graphql/event/types';
import { WaveformSortType } from '~state/analyst-workspace/types';
import { FkUnits } from '../../types';
import { fkNeedsReview } from '../fk-util';
import { FkThumbnailContainer } from './fk-thumbnail-container';

/**
 * Fk Thumbnails Props
 */
export interface FkThumbnailListProps {
  sortedSignalDetections: SignalDetectionTypes.SignalDetection[];
  unassociatedSdIds: string[];
  signalDetectionIdsToFeaturePrediction: Immutable.Map<string, FeaturePrediction[]>;
  distanceToSource?: StationTypes.DistanceToSource[];
  thumbnailSizePx: number;
  selectedSortType: WaveformSortType;
  selectedSdIds: string[];
  fkUnitsForEachSdId: Immutable.Map<string, FkUnits>;
  clearSelectedUnassociatedFks(): void;
  setSelectedSdIds(ids: string[]): void;
  markFksForSdIdsAsReviewed(sdIds: string[]): void;
  showFkThumbnailContextMenu(x: number, y: number): void;

}

/**
 * List of fk thumbnails with controls for filtering them
 */
export class FkThumbnailList extends React.Component<FkThumbnailListProps> {

  /** The last selected signal detection */
  private lastSelectedSd: string[] = [];

  /**
   * Constructor.
   * 
   * @param props The initial props
   */
  public constructor(props: FkThumbnailListProps) {
    super(props);
  }

  // ***************************************
  // BEGIN REACT COMPONENT LIFECYCLE METHODS
  // ***************************************

  /**
   * Invoked when the componented mounted.
   */
  public componentDidMount() {
    this.setSelectedThumbnail();
  }

  /**
   * Invoked when the componented mounted.
   * 
   * @param prevProps The previous props
   * @param prevState The previous state
   */
  public componentDidUpdate() {
    this.setSelectedThumbnail();
  }

  /**
   * Renders the component.
   */
  public render() {
    // HACK for testing set the selected list to latest
    if (this.props.selectedSdIds && this.props.selectedSdIds.length > 0) {
      this.lastSelectedSd = this.props.selectedSdIds;
    }
    return (
      <div
        className="azimuth-slowness-thumbnails__wrapper-3"
        onKeyDown={this.onKeyDown}
        tabIndex={0}
      >
        {
          this.props.sortedSignalDetections.map(sd => (
            <FkThumbnailContainer
              key={sd.id}
              data={sd}
              signalDetectionFeaturePredictions={this.props.signalDetectionIdsToFeaturePrediction.has(sd.id) ?
                  this.props.signalDetectionIdsToFeaturePrediction.get(sd.id) : []}
              sizePx={this.props.thumbnailSizePx}
              selected={this.props.selectedSdIds.indexOf(sd.id) >= 0}
              onClick={(e: React.MouseEvent<HTMLDivElement>) =>
                this.onThumbnailClick(e, sd.id)}
              fkUnit={
                getFkUnitForSdId(sd.id, this.props.fkUnitsForEachSdId)}
              isUnassociated={this.props.unassociatedSdIds.indexOf(sd.id) > -1}
              showFkThumbnailMenu={this.props.showFkThumbnailContextMenu}
            />
          ))
        }
      </div>
    );
  }

  // ***************************************
  // END REACT COMPONENT LIFECYCLE METHODS
  // ***************************************

  /**
   * Automatically selects an fk when an event is loaded
   */
  private readonly setSelectedThumbnail = (): void => {
    // If no SD are selected set it to the first needs review sdFkDataToDraw
    if (!this.props.selectedSdIds || this.props.selectedSdIds.length === 0) {
      // Walk thru the list to find first that needs review
      const firstNeedsReviewSd = this.props.sortedSignalDetections
                                .filter(sd => !this.props.unassociatedSdIds.find(unassocId => sd.id === unassocId))
                                .find(sd => fkNeedsReview(sd)
                                      && this.lastSelectedSd.indexOf(sd.id) === -1);
      // If found a new one set the selected id
      if (firstNeedsReviewSd) {
        this.props.setSelectedSdIds([firstNeedsReviewSd.id]);
      }
    }
  }

  /**
   * onKeyDown event handler for selecting multiple fk at once
   */
  private readonly onKeyDown = (e: React.KeyboardEvent<HTMLDivElement>): void => {
    if (!e.repeat) {
      if (e.shiftKey && (e.ctrlKey || e.metaKey) && (e.key === 'a' || e.key === 'A')) {
        // Shift + CmrOrCtrl + a ==> add all to current selection
        const selectedIds: string[] = [...this.props.selectedSdIds];
        this.props.sortedSignalDetections
          .forEach(sd => {
            if (selectedIds.indexOf(sd.id) === -1) {
              selectedIds.push(sd.id);
            }
          });
        this.props.setSelectedSdIds(selectedIds);
      } else if ((e.ctrlKey || e.metaKey) && (e.key === 'a' || e.key === 'A')) {
        // CmrOrCtrl + a ==> select all
        const selectedIds: string[] = [...this.props.sortedSignalDetections
          .map(sd => sd.id)];
        this.props.setSelectedSdIds(selectedIds);
      } else if (e.key === 'Escape') {
        // Escape ==> deselect all
        this.props.setSelectedSdIds([]);
      }
    }
  }

  /**
   * Selects a slicked thumbnail
   */
  private readonly onThumbnailClick = (e: React.MouseEvent<HTMLDivElement>, id: string): void => {
    let selectedIds: string[];
    if (e.shiftKey && this.props.selectedSdIds.length > 0) {
      // shift range selection
      selectedIds = [...this.props.selectedSdIds];
      const fkIds: string[] = this.props.sortedSignalDetections
        .map(sd => (sd.id));
      const selectedIndex: number = fkIds.indexOf(id);
      const minIndex: number = findIndex(fkIds, i => (selectedIds.indexOf(i) !== -1));
      const maxIndex: number = findLastIndex(fkIds, i => (selectedIds.indexOf(i) !== -1));
      fkIds.forEach(i => {
        if (selectedIds.indexOf(i) === -1) {
          const index: number = fkIds.indexOf(i);
          if (index >= selectedIndex && index < minIndex) {
            selectedIds.push(i);
          } else if (index > maxIndex && index <= selectedIndex) {
            selectedIds.push(i);
          }
        }
      });

    } else if (e.ctrlKey || e.metaKey) {
      // add to current selection
      selectedIds = [...this.props.selectedSdIds];
      if (selectedIds.indexOf(id) >= 0) {
        selectedIds.splice(selectedIds.indexOf(id), 1);
      } else {
        selectedIds.push(id);
      }
    } else {
      if (this.props.selectedSdIds.length === 1 && this.props.selectedSdIds[0] !== id) {
        if (!this.props.unassociatedSdIds.find(uSdId => uSdId === this.props.selectedSdIds[0])) {
          this.props.markFksForSdIdsAsReviewed(this.props.selectedSdIds);

        }
      }
      selectedIds = [id];
    }
    this.props.setSelectedSdIds(selectedIds);
  }
}
