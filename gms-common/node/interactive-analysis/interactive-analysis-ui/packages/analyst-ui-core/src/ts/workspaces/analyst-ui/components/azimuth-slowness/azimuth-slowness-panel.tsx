import { ContextMenu } from '@blueprintjs/core';
import * as React from 'react';
import { getFkData } from '~analyst-ui/common/utils/fk-utils';
import { FkTypes } from '~graphql';
import { FkConfiguration } from '~graphql/fk/types';
import { UILogger } from '~util/log/logger';
import { FkThumbnailBlueprintContextMenu } from './components/context-menus/fk-context-menu';
import { FkDisplay } from './components/fk-display/fk-display';
import { FkThumbnailList } from './components/fk-thumbnail-list/fk-thumbnail-list';
import { FkThumbnailsControls } from './components/fk-thumbnail-list/fk-thumbnails-controls';
import { createFkInput, fkNeedsReview, getSortedSignalDetections } from './components/fk-util';
import { AzimuthSlownessPanelProps, FkParams } from './types';

/**
 * An intermediary between AzimuthSlownessComponent and the other components so that event handling is simpler 
 */
export class AzimuthSlownessPanel extends React.Component<AzimuthSlownessPanelProps, {}> {
  /** Used to constrain the max width of the thumbnail drag resize */
  private azimuthSlownessContainer: HTMLDivElement;

  /** Used to drag & resize this element */
  private fkThumbnailsContainer: HTMLDivElement;

  /** The inner container for the thumbnail */
  private fkThumbnailsInnerContainer: HTMLDivElement;

  /**
   * Invoked when the component mounted.
   */
  public componentDidMount() {
        this.props.adjustFkInnerContainerWidth(this.fkThumbnailsContainer, this.fkThumbnailsInnerContainer);
    }

  public componentDidUpdate(prevProps: AzimuthSlownessPanelProps) {
      if (this.props.fkThumbnailColumnSizePx !== prevProps.fkThumbnailColumnSizePx) {
        this.props.adjustFkInnerContainerWidth(this.fkThumbnailsContainer, this.fkThumbnailsInnerContainer);
      }
    }

  public render () {
      const anyDislayedFksNeedReview = this.getReviewableSds().length > 0;
      return (
        <div
          ref={ref => this.azimuthSlownessContainer = ref}
          className="azimuth-slowness-container"
          tabIndex={0}
          onKeyDown={this.onKeyDown}
        >
        <div
          ref={ref => this.fkThumbnailsContainer = ref}
          className="azimuth-slowness-thumbnails"
          style={{ width: `${this.props.fkThumbnailColumnSizePx}px` }}
        >
          <div
            className="azimuth-slowness-thumbnails__control-container"
          >
            <FkThumbnailsControls
              updateFkThumbnail={this.props.updateFkThumbnailSize}
              updateFkFilter={this.props.updateFkFilter}
              anyDislayedFksNeedReview={anyDislayedFksNeedReview}
              onlyOneFkIsSelected={this.props.selectedSdIds.length === 1}
              widthPx={this.props.fkThumbnailColumnSizePx}
              nextFk={() => {this.nextFk(); }}
              currentFilter={this.props.filterType}
              clearSelectedUnassociatedFks={this.clearSelectedUnassociatedFks}
            />
          </div>
          <div
            className="azimuth-slowness-thumbnails__wrapper-1"
          >
            <div
              ref={ref => this.fkThumbnailsInnerContainer = ref}
              className="azimuth-slowness-thumbnails__wrapper-2"
            >
              <FkThumbnailList
                thumbnailSizePx={this.props.fkThumbnailSizePx}
                sortedSignalDetections={this.props.signalDetectionsToDraw}
                unassociatedSdIds={
                  this.props.signalDetectionsToDraw
                  .filter(sdToDraw => !this.props.associatedSignalDetections.find(aSd => aSd.id === sdToDraw.id))
                  .map(sd => sd.id)
                }
                signalDetectionIdsToFeaturePrediction={this.props.signalDetectionsIdToFeaturePredictions}
                distanceToSource={this.props.distanceToSource}
                selectedSdIds={this.props.selectedSdIds}
                setSelectedSdIds={this.props.setSelectedSdIds}
                selectedSortType={this.props.selectedSortType}
                clearSelectedUnassociatedFks={this.clearSelectedUnassociatedFks}
                fkUnitsForEachSdId={this.props.fkUnitsForEachSdId}
                markFksForSdIdsAsReviewed={this.markFksForSdIdsAsReviewedIfTheyNeedToBeAccepted}
                showFkThumbnailContextMenu={this.showFkThumbnailMenu}
              />
            </div>
          </div>
        </div>
          {/* drag handle divider */}
          <div
            className="azimuth-slowness-divider"
            onMouseDown={this.onThumbnailDividerDrag}
          >
            <div
              className="azimuth-slowness-divider__spacer"
            />
          </div>
          <FkDisplay
            measurementMode={this.props.measurementMode}
            defaultStations={this.props.defaultStations}
            defaultWaveformFilters={this.props.defaultWaveformFilters}
            channelFilters={this.props.channelFilters}
            eventsInTimeRange={this.props.eventsInTimeRange}
            currentOpenEvent={this.props.openEvent}
            signalDetection={this.props.displayedSignalDetection}
            signalDetectionsByStation={this.props.signalDetectionsByStation}
            signalDetectionFeaturePredictions={this.props.featurePredictionsForDisplayedSignalDetection}
            widthPx={this.props.fkDisplayWidthPx}
            numberOfOutstandingComputeFkMutations={this.props.numberOfOutstandingComputeFkMutations}
            heightPx={this.props.fkDisplayHeightPx}
            multipleSelected={this.props.selectedSdIds && this.props.selectedSdIds.length > 1}
            anySelecteced={this.props.selectedSdIds && this.props.selectedSdIds.length > 0}
            userInputFkFrequency={this.props.userInputFkFrequency}
            fkUnit={this.props.fkUnitForDisplayedSignalDetection}
            userInputFkWindowParameters={this.props.userInputFkWindowParameters}
            onNewFkParams={this.onNewFkParams}
            changeUserInputFks={this.props.changeUserInputFks}
            setFkUnitForSdId={this.props.setFkUnitForSdId}
            onSetWindowLead={this.props.setWindowLead}
            fkFrequencyThumbnails={this.props.fkFrequencyThumbnails}
          />
        </div>
      );
    }

  /**
   * Selects the next fk that needs review
   */
  private readonly nextFk = () => {
    if (!this.props.displayedSignalDetection) {
      throw Error('Selected Signal Detection not found in client cache');
    }
    const reviewableSds = this.getReviewableSds();
    if (reviewableSds.length === 1 &&
      reviewableSds.find(sd => sd.id === this.props.displayedSignalDetection.id)) {
      this.props.setSelectedSdIds([]);
      this.markFksForSdIdsAsReviewedIfTheyNeedToBeAccepted([this.props.displayedSignalDetection.id]);

    } else {
      const needsReviewSds = reviewableSds
                      .filter(sd => sd.id !== this.props.displayedSignalDetection.id);
      const sortedNeedsReviewSds =
        getSortedSignalDetections(needsReviewSds, this.props.selectedSortType, this.props.distanceToSource);
      this.props.setSelectedSdIds(
        [sortedNeedsReviewSds[0].id]
      );
      this.markFksForSdIdsAsReviewedIfTheyNeedToBeAccepted([this.props.displayedSignalDetection.id]);
    }
  }

  /**
   * Handles keypresses on az slow
   * @param e keyboard event
   */
  private readonly onKeyDown = (e: React.KeyboardEvent<HTMLDivElement>) => {
      if (e.nativeEvent.code === 'KeyN' && (e.metaKey || e.ctrlKey)) {
        if (this.getReviewableSds().length > 0 && this.props.selectedSdIds.length === 1) {
            this.nextFk();
        }
      }
    }

  /**
   * Clear selected UnassociatedFks
   */
  private readonly clearSelectedUnassociatedFks = (): void => {
    const selectedAndUnnasociatedSdIds = this.props.selectedSdIds.filter(sdId =>
      !this.props.associatedSignalDetections.find(assocSd => assocSd.id === sdId)
    );
    const selectedAndAsociatedSdIds = this.props.selectedSdIds.filter(sdId =>
      this.props.associatedSignalDetections.find(assocSd => assocSd.id === sdId)
    );
    const sdsIdsToShowFk =
      this.props.sdIdsToShowFk.filter(sdId => !selectedAndUnnasociatedSdIds.find(unSdId => unSdId === sdId));
    this.props.setSelectedSdIds(selectedAndAsociatedSdIds);
    this.props.setSdIdsToShowFk(sdsIdsToShowFk);
  }

  /**
   * Shows the fk thumbnail menu
   * 
   * @param x offset from left
   * @param y offset from top
   */
  private readonly showFkThumbnailMenu = (x: number, y: number) => {
      const selectedAndUnnasociatedSdIds = this.props.selectedSdIds.filter(sdId =>
        !this.props.associatedSignalDetections.find(assocSd => assocSd.id === sdId)
      );
      const stageIntervalContextMenu = FkThumbnailBlueprintContextMenu(
      this.clearSelectedUnassociatedFks,
      selectedAndUnnasociatedSdIds.length > 0);
      ContextMenu.show(
      stageIntervalContextMenu, {
        left: x,
        top: y
      });
    }

  /**
   * Start a drag on mouse down on the divider
   */
  private readonly onThumbnailDividerDrag = (e: React.MouseEvent<HTMLDivElement>) => {
    let prevPosition = e.clientX;
    let currentPos = e.clientX;
    let diff = 0;
    const maxWidthPct = 0.8;
    const maxWidthPx = this.azimuthSlownessContainer.clientWidth * maxWidthPct;

    const onMouseMove = (e2: MouseEvent) => {
      currentPos = e2.clientX;
      diff = currentPos - prevPosition;
      prevPosition = currentPos;
      const widthPx = this.fkThumbnailsContainer.clientWidth + diff;
      if (widthPx < maxWidthPx) {
          this.props.setFkThumbnailColumnSizePx(widthPx);
      }
    };

    const onMouseUp = (e2: MouseEvent) => {
      document.body.removeEventListener('mousemove', onMouseMove);
      document.body.removeEventListener('mouseup', onMouseUp);
    };

    document.body.addEventListener('mousemove', onMouseMove);
    document.body.addEventListener('mouseup', onMouseUp);
    }

  /**
   * Handles new FK Request when frequency and/or window params change
   */
  private readonly onNewFkParams = async (
    sdId: string, fkParams: FkParams, fkConfiguration: FkConfiguration): Promise<void> => {
    const fkData = getFkData(this.props.displayedSignalDetection.currentHypothesis.featureMeasurements);
    if (fkData) {
      const fkInput: FkTypes.FkInput = createFkInput(
        this.props.displayedSignalDetection,
        fkParams.frequencyPair,
        fkParams.windowParams,
        fkConfiguration
      );
      this.props.computeFkAndUpdateState(fkInput)
      .catch(error => UILogger.error(`Failed onNewFkParams: ${error}`));
    }
  }

  /**
   * @param sdIds a list of candidate sd ids
   */
  private readonly markFksForSdIdsAsReviewedIfTheyNeedToBeAccepted
    = (sdIds: string[]) => {

      const sdIdsThatCanBeReviewed = this.getReviewableSds()
        .map(sd => sd.id);
      const sdIdsToReview =
        sdIds.filter(sdId => sdIdsThatCanBeReviewed.find(sdReviewableId => sdReviewableId === sdId));
      if (sdIdsToReview.length > 0) {
        this.props.markFksForSdIdsAsReviewed(sdIdsToReview);
      }
  }
  /**
   * Gets the list of fks that need review and are associated to the currently open event
   */
  private readonly getReviewableSds = () => this.props.signalDetectionsToDraw.filter(
    sdToDraw =>
      this.props.associatedSignalDetections.find(aSd => aSd.id === sdToDraw.id) &&
      fkNeedsReview(sdToDraw)
    )
}
