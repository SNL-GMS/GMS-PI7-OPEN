export interface XAxisProps {
  /** Epoch seconds end */
  endTimeSecs: number;

  /** Epoch seconds start */
  startTimeSecs: number;

  /** Add border to top */
  borderTop: boolean;

  /** Label width in px */
  labelWidthPx: number;

  /** Scrollbar width in px */
  scrolbarWidthPx: number;

  /** Call back to get the current view range */
  getViewRange(): [number, number];
}

// tslint:disable-next-line:no-empty-interface
export interface XAxisState {
}
