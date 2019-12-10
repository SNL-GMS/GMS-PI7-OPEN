import { IconName, Intent } from '@blueprintjs/core';
import * as Entities from '../../../../../../../../../../entities';

export interface TheoreticalPhasesProps {
  /** Configuration for weavess */
  configuration: Entities.Configuration;

  /** Station Id as string */
  stationId: string;

  /** The theoretical phase windows */
  theoreticalPhaseWindows: Entities.TheoreticalPhaseWindow[] | undefined;

  /** Boolean is default channel */
  isDefaultChannel: boolean;

  /** Epoch seconds start time */
  displayStartTimeSecs: number;

  /** Boolean is default channel */
  displayEndTimeSecs: number;

  /** (optional) callback events Ex on waveform click */
  events?: Entities.ChannelContentEvents;

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
   * Returns the time in seconds for the given clientX.
   * 
   * @param clientX The clientX
   * 
   * @returns The time in seconds; undefined if clientX is 
   * out of the channel's bounds on screen.
   */
  getTimeSecsForClientX(clientX: number): number | undefined;

  /**
   * Toggle display of the drag indicator for this channel
   * 
   * @param show True to show drag indicator
   * @param color The color of the drag indicator
   */
  toggleDragIndicator(show: boolean, color: string): void;

  /**
   * Set the position for the drag indicator
   * 
   * @param clientX The clientX 
   */
  positionDragIndicator(clientX: number): void;
}

// tslint:disable-next-line:no-empty-interface
export interface TheoreticalPhasesState {
}
