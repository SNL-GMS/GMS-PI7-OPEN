import * as Entities from '../../../../../entities';

export interface MoveableMarkerProps {
  /** The marker config */
  marker: Entities.Marker;

  /** The associated start marker (usually only for a selection window) */
  associatedStartMarker?: Entities.Marker;

  /** The associated end marker (usually only for a selection window) */
  associatedEndMarker?: Entities.Marker;

  /** Percentage Location 0-100 as number */
  percentageLocation: number;

  /** Label Width in px */
  labelWidthPx: number;

  /** Start and end of the entire time range */
  timeRange(): Entities.TimeRange;

  /** current view port of the time range */
  viewTimeRange(): Entities.TimeRange;

  /** Returns container client width */
  containerClientWidth(): number;

  /** Returns the viewPort client width */
  viewportClientWidth(): number;

  /**
   * (optional) updates the location of the marker
   * 
   * @param marker the marker
   */
  onUpdateMarker?(marker: Entities.Marker): void;

  /**  */
  updateTimeWindowSelection?(): void;

}

// tslint:disable-next-line:no-empty-interface
export interface MoveableMarkerState {
}
