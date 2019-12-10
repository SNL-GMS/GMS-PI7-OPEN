
import * as Entities from '../../../../../entities';

export interface SelectionWindowProps {
  /** Selection window props as a StationWindowConfig */
  selectionWindow: Entities.SelectionWindow;

  /** Label Width in pixels */
  labelWidthPx: number;

  /** Start and end of the entire time range */
  timeRange(): Entities.TimeRange;

  /** current view port of the time range */
  viewTimeRange(): Entities.TimeRange;

  /** Ref to the html canvas element */
  canvasRef(): HTMLCanvasElement | null;

  /** Returns container client width  */
  containerClientWidth(): number;

  /** Returns the viewPort client width */
  viewportClientWidth(): number;

  /**
   * Computes the time in seconds for the mouse x position.
   * 
   * @param mouseXPosition the mouse x position to compute the time on
   * 
   * @returns The computed time in seconds
   */
  computeTimeSecsForMouseXPosition(mouseXPosition: number): number;

  /**
   * (optional) updates the location of the marker
   * 
   * @param selection the selection
   */
  onUpdateSelectionWindow?(selection: Entities.SelectionWindow): void;

  /**
   * Event handler for click events within a selection 
   * 
   * @param selection the selection
   * @param timeSecs epoch seconds of where drag ended in respect to the data
   */
  onClickSelectionWindow?(selection: Entities.SelectionWindow, timeSecs: number): void;

  /**
   * Mouse Move event
   * 
   * @param e mouse event as React.MouseEvent<HTMLDivElement>
   */
  onMouseMove(e: React.MouseEvent<HTMLDivElement>): void;

  /**
   * Mouse down event
   * 
   * @param e React.MouseEvent<HTMLDivElement>
   */
  onMouseDown(e: React.MouseEvent<HTMLDivElement>): void;

  /**
   * Mouse up event
   * 
   * @param e React.MouseEvent<HTMLDivElement>
   */
  onMouseUp(e: React.MouseEvent<HTMLDivElement>): void;
}

// tslint:disable-next-line:no-empty-interface
export interface SelectionWindowState {
}
