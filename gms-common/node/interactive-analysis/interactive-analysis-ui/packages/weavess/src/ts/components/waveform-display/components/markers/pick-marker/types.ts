import { IconName, Intent } from '@blueprintjs/core';

export interface PickMarkerProps {
  /** unique id */
  id: string;

  /** Channel Id as a string */
  channelId: string;

  /** Epoch seconds start time */
  startTimeSecs: number;

  /** Epoch seconds end time */
  endTimeSecs: number;

  /** Actual physical css position as a percentage 0-100 */
  position: number;

  /** Label as a string */
  label: string;

  /** Color of the picj as a string */
  color: string;

  /** 
   * A filter provided for the pick marker
   * 
   * style.filter = "none | blur() | brightness() | contrast() | drop-shadow() | 
   *                 grayscale() | hue-rotate() | invert() | opacity() | saturate() | sepia()"
   */
  filter?: string | undefined;

  /** Epoch Time the pick is located */
  timeSecs: number;

  /** Optional isPredicted */
  predicted: boolean;

  /** Indicates if the pick marker is selected */
  isSelected: boolean;

  /** (optional) removes ability to modify the pick marker */
  disableModification?: boolean;

  /**
   * Call back for toast, which is the notification pop up
   * 
   * @param message message to be displayed as a string
   * @param intent (optional) NONE, PRIMARY, WARNING, DANGER ex NONE = "none"
   * @param icon (optional) the way it looks
   * @param timeout (optional) time before message disappears
   */
  toast(message: string, intent?: Intent, icon?: IconName, timeout?: number): void;

  /**
   * Time seconds for client x
   * 
   * @param clientX x posistion as a number
   * 
   * @returns ClientX as a number
   */
  getTimeSecsForClientX(clientX: number): number | undefined;

  /**
   * Drag indicator
   * 
   * @param show boolean etermines to show or not
   * @param color color of indicator as a string
   */
  toggleDragIndicator(show: boolean, color: string): void;

  /**
   * Position of drag indicator
   * 
   * @param clientX client x position
   */
  positionDragIndicator(clientX: number): void;

  /**
   * Click event
   * 
   * @param e mouse event as a React.MouseEvent<HTMLDivElement>
   * @param id unique id as a string of the pick
   */
  onClick?(e: React.MouseEvent<HTMLDivElement>, id: string): void;

  /**
   * Creates context menu
   * 
   * @param e mouse event as a React.MouseEvent<HTMLDivElement>
   * @param channelId channel id as a string
   * @param id unique id as a string of the pick
   */
  onContextMenu?(e: React.MouseEvent<HTMLDivElement>, channelId: string, id: string): void;

  /**
   * Drag end call back
   * 
   * @param id unique id as a string of the pick
   * @param timeSecs epoch seconds of drag end
   */
  onDragEnd?(id: string, timeSecs: number | undefined): void;
}

// tslint:disable-next-line:no-empty-interface
export interface PickMarkerState {
  position: number;
}
