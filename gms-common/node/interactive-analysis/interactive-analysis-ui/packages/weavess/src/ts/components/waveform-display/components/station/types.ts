import { IconName, Intent } from '@blueprintjs/core';
import * as Entities from '../../../../entities';

export interface StationProps {
  /** Configuration for weavess */
  configuration: Entities.Configuration;

  /** Station configuration (holds the data) */
  station: Entities.Station;

  /** Epoch Seconds start */
  displayStartTimeSecs: number;

  /** Epoch Seconds end */
  displayEndTimeSecs: number;

  /** true if waveforms should be rendered; false otherwise */
  shouldRenderWaveforms: boolean;

  /** true if spectrograms should be rendered; false otherwise */
  shouldRenderSpectrograms: boolean;

  /** Web workers */
  workerRpcs: any[];

  /** The selections */
  selections: Entities.Selections;

  /** (optional) callbacks for events EX on station click */
  events?: Entities.StationEvents;

  // callbacks
  /** Ref to the html canvas element */
  canvasRef(): HTMLCanvasElement | null;

  /**
   * @returns current view range as [0,1]
   */
  getViewRange(): [number, number];

  /**
   * Call back for toast, which is the notification pop up
   * 
   * @param message message to be displayed as a string
   * @param intent (optional) NONE, PRIMARY, WARNING, DANGER ex NONE = "none"
   * @param icon (optional) the way it looks
   * @param timeout (optional) time before message disappears
   */
  toast(message: string, intent?: Intent, icon?: IconName, timeout?: number): void;

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

export interface StationState {
  /** Toggles nondefault channels */
  expanded: boolean;

  /** Toggles red M on station when masks are in view */
  showMaskIndicator: boolean | false;
}
