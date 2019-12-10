import { IconName, Intent } from '@blueprintjs/core';
import * as Entities from '../../../../../../entities';

export interface ChannelProps {
  /** The index of the channel in relation to the station */
  index: number;

  /** Height of channel */
  height: number;

  /** true if waveforms should be rendered; false otherwise */
  shouldRenderWaveforms: boolean;

  /** true if spectrograms should be rendered; false otherwise */
  shouldRenderSpectrograms: boolean;

  /** Web Workers */
  workerRpcs: any[];

  /** Configuration for weavess */
  configuration: Entities.Configuration;

  /** Station Id as string */
  stationId: string;

  /** Channel configuration (Holds the Data) */
  channel: Entities.Channel;

  /** Epoch seconds start time */
  displayStartTimeSecs: number;

  /** Epoch seconds end time */
  displayEndTimeSecs: number;

  /** Boolean is default channel */
  isDefaultChannel: boolean;

  /** Does have sub channels */
  isExpandable: boolean;

  /** Displaying sub channels */
  expanded: boolean;

  /** The selections */
  selections: Entities.Selections;

  /** Toggles red M when mask(s) is in view */
  showMaskIndicator: boolean;

  /** Distance */
  distance: number;

  /** Distance units */
  distanceUnits: Entities.DistanceUnits;

  /** (optional) callback events Ex on label click */
  events?: Entities.ChannelEvents;

  // callbacks

  /** Ref to the html canvas element */
  canvasRef(): HTMLCanvasElement | null;

  /**
   * Call back for toast, which is the notification pop up
   * 
   * @param message message to be displayed as a string
   * @param intent (optional) NONE, PRIMARY, WARNING, DANGER ex NONE = "none"
   * @param icon (optional) the way it looks
   * @param timeout (optional) time before message disappears
   */
  toast(message: string, intent?: Intent, icon?: IconName, timeout?: number): void;

  /** Toggles display of sub channels */
  toggleExpansion?(): void;

  /**
   * @returns current view range as [0,1]
   */
  getViewRange(): [number, number];

  /** Issues a re render */
  renderWaveforms(): void;

  /**
   * get the currently displayed viewTimeInterval
   * (the startTime and endTime of the currently displayed view of the waveforms)
   */
  getCurrentViewRangeInSeconds(): Entities.TimeRange;

  /**
   * Computes the time in seconds for the mouse x position.
   * 
   * @param mouseXPosition the mouse x position to compute the time on
   * 
   * @returns The computed time in seconds
   */
  computeTimeSecsForMouseXPosition(mouseXPosition: number): number;

  /**
   * Mouse move event
   * 
   * @param e mouse event as React.MouseEvent<HTMLDivElement>
   * @param xPct percentage location x of mouse as a number
   * @param timeSecs the time in seconds
   */
  onMouseMove(e: React.MouseEvent<HTMLDivElement>, xPct: number, timeSecs: number): void;

  /**
   * Mouse down event
   * 
   * @param e mouse event as React.MouseEvent<HTMLDivElement>
   * @param xPct percentage location x of mouse as a number
   * @param channelId channel Id as a string
   * @param timeSecs epoch seconds of mouse down 
   * @param isDefaultChannel boolean
   */
  onMouseDown(e: React.MouseEvent<HTMLDivElement>, xPct: number, channelId: string,
    timeSecs: number, isDefaultChannel: boolean): void;

  /**
   * Mouse up event
   * 
   * @param e mouse event as React.MouseEvent<HTMLDivElement>
   * @param xPct percentage location x of mouse as a number
   * @param channelId channelId channel Id as a string
   * @param timeSecs timeSecs epoch seconds of mouse down 
   * @param isDefaultChannel boolean
   */
  onMouseUp(e: React.MouseEvent<HTMLDivElement>, xPct: number, channelId: string,
    timeSecs: number, isDefaultChannel: boolean): void;

  /**
   * (optional) context menu creation
   * 
   * @param e mouse event as React.MouseEvent<HTMLDivElement>
   * @param channelId channelId channel Id as a string
   * @param sdId station id as a string
   */
  onContextMenu?(e: React.MouseEvent<HTMLDivElement>, channelId: string, sdId?: string): void;

  /**
   * (optional) Updates the measure window
   * 
   * @param stationId station id as a string
   * @param channel channel config as a Channel
   * @param startTimeSecs epoch start time secs
   * @param endTimeSecs epoch end time secs
   * @param isDefaultChannel boolean
   * @param removeSelection removed measure window selection div
   */
  updateMeasureWindow?(stationId: string, channel: Entities.Channel,
    startTimeSecs: number, endTimeSecs: number, isDefaultChannel: boolean, removeSelection: () => void): void;
}

export interface ChannelState {
/** Waveform y-axis bounds */
waveformYAxisBounds: Entities.YAxisBounds;

/** Spectrogram y-axis bounds */
spectrogramYAxisBounds: Entities.YAxisBounds;

}
