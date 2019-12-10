import { IconName, Intent } from '@blueprintjs/core';
import * as Entities from '../../../../../../../../entities';

export interface ContentRendererProps {
  /** Configuration for weavess */
  configuration: Entities.Configuration;

  /** Station Id as string */
  stationId: string;

  /** The description */
  description?: string;

  /** The description label color */
  descriptionLabelColor?: string;

  /** Channel Id as string */
  channelId: string;

  /** Boolean is default channel */
  isDefaultChannel: boolean;

  /** Epoch seconds start time */
  displayStartTimeSecs: number;

  /** Boolean is default channel */
  displayEndTimeSecs: number;

  /** Web Workers */
  workerRpcs: any[];

  /** The signal detections */
  signalDetections: Entities.PickMarker[] | undefined;

  /** The predicted phases */
  predictedPhases: Entities.PickMarker[] | undefined;

  /** The theoretical phase windows */
  theoreticalPhaseWindows: Entities.TheoreticalPhaseWindow[] | undefined;

  /** Collection of markers */
  markers?: Entities.Markers | undefined;

  /** The selections */
  selections: Entities.Selections;

  /** (optional) callback events */
  events?: Entities.ChannelContentEvents;

  // Callbacks

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

  /**
   * Sets the Y axis bounds
   * 
   * @param min minimum bound as a number
   * @param max Maximum bound as a number
   */
  setYAxisBounds(min: number, max: number);

  /**
   * @returns current view range as [0,1]
   */
  getViewRange(): [number, number];

  /** Issues a rerender of the graphics */
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
   */
  onMouseMove(e: React.MouseEvent<HTMLDivElement>): void;

  /**
   * Mouse down event
   * 
   * @param e mouse event as React.MouseEvent<HTMLDivElement>
   */
  onMouseDown(e: React.MouseEvent<HTMLDivElement>): void;

  /**
   * onMouseUp event handler
   * 
   * @param e mouse event as React.MouseEvent<HTMLDivElement>
   */
  onMouseUp(e: React.MouseEvent<HTMLDivElement>): void;

  /**
   * onContextMenu event handler
   * 
   * @param e mouse event as React.MouseEvent<HTMLDivElement>
   */
  onContextMenu(e: React.MouseEvent<HTMLDivElement>): void;

  /**
   * onKeyDown event handler
   * 
   * @param e mouse event as React.MouseEvent<HTMLDivElement>
   * @param timeForMouseXPosition the time for the current mouse position X
   */
  onKeyDown(e: React.KeyboardEvent<HTMLDivElement>): void;

  /**
   * onMeasureWindowClick event handler
   * 
   * @param e The mouse event
   */
  onMeasureWindowClick(e: React.MouseEvent<HTMLDivElement>): void;

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

// tslint:disable-next-line:no-empty-interface
export interface ContentRendererState {
}
