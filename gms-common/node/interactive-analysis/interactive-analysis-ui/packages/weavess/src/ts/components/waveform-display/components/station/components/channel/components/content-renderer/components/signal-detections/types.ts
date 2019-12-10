import { IconName, Intent } from '@blueprintjs/core';
import * as Entities from '../../../../../../../../../../entities';

export interface SignalDetectionsProps {
  /** Configuration for weavess */
  configuration: Entities.Configuration;

  /** Station Id as string */
  stationId: string;

  /** Channel Id as string */
  channelId: string;

  /** The signal detections */
  signalDetections: Entities.PickMarker[] | undefined;

  /** Boolean is default channel */
  isDefaultChannel: boolean;

  /** Epoch seconds start time */
  displayStartTimeSecs: number;

  /** Boolean is default channel */
  displayEndTimeSecs: number;

  /** Selected signal detections as a string[] of SD ids */
  selectedSignalDetections: string[] | undefined;

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
export interface SignalDetectionsState {
}
