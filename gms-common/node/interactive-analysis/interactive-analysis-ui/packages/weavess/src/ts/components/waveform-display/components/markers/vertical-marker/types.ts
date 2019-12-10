
import * as Entities from '../../../../../entities';

export interface VerticalMarkerProps {
  /** Color as a string */
  color: string;

  /** Line style as a LineStyle */
  lineStyle: Entities.LineStyle;

  /** Percentage Location 0-100 as a number */
  percentageLocation: number;
}

// tslint:disable-next-line:no-empty-interface
export interface VerticalMarkerState {
}
