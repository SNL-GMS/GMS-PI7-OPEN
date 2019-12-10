import { IconName, Intent, Position } from '@blueprintjs/core';
import * as Entities from '../../entities';

/** Brush Type */
export enum BrushType {
  /** zoom brush type */
  Zoom = 'Zoom',

  /** create mask brush type */
  CreateMask = 'CreateMask'
}

/**
 * The display mode options for the waveform display.
 */
export enum Mode {
  DEFAULT = 'Default',
  MEASUREMENT = 'Measurement'
}

/**
 * Waveform DisplayProps
 */
export interface WaveformDisplayProps {

  /** the display mode */
  mode: Mode | 'Default' | 'Measurement';

  /** waveform display configuration */
  configuration?: Partial<Entities.Configuration>;

  /** flex or not? */
  flex?: boolean;

  /** start time in seconds */
  startTimeSecs: number;

  /** end time in seconds */
  endTimeSecs: number;

  /** stations */
  stations: Entities.Station[];

  /** events */
  events: Entities.Events;

  /** selections */
  selections?: Entities.Selections;

  /** the initial zoom window */
  initialZoomWindow?: Entities.TimeRange;

  /** default zoom window */
  defaultZoomWindow?: Entities.TimeRange;

  /** markers */
  markers?: Entities.Markers;

  /** specifies the measure window selection */
  measureWindowSelection?: Entities.MeasureWindowSelection;

  /** event handler for clearing selected channels */
  clearSelectedChannels?(): void;

  /**
   * Event handler for selecting a channel
   * @param channelId a Channel Id as a string
   */
  selectChannel?(channelId: string): void;
}

export interface WaveformDisplayState {

  /** the display mode */
  mode: Mode;

  /** Configuration for waveform display */
  configuration: Entities.Configuration;

  /** Detemines if the measure window is displayed */
  showMeasureWindow: boolean;

  /** Height of the measure window in pixels */
  measureWindowHeightPx: number;

  /** Selection info needed to render a measure window */
  measureWindowSelection: Entities.MeasureWindowSelection | undefined;

  /** 
   * the previous measure window selection passed in from props 
   * (used to ensure the measure window is only updated when expected) 
   */
  prevMeasureWindowSelectionFromProps: Entities.MeasureWindowSelection | undefined;

  /** true if waveforms should be rendered; false otherwise */
  shouldRenderWaveforms: boolean;

  /** true if spectrograms should be rendered; false otherwise */
  shouldRenderSpectrograms: boolean;
}

export interface WaveformPanelProps {

  /** the display mode */
  // mode: Entities.Mode;

  /** Configuration for Waveform Pannel */
  configuration: Entities.Configuration;

  /** true if waveforms should be rendered; false otherwise */
  shouldRenderWaveforms: boolean;

  /** true if spectrograms should be rendered; false otherwise */
  shouldRenderSpectrograms: boolean;

  /** Epoch seconds start */
  startTimeSecs: number;

  /** Epoch Seconds end */
  endTimeSecs: number;

  /** Array of Stations */
  stations: Entities.Station[];

  /** Call back events */
  events: Entities.Events;

  /** Selections */
  selections?: Entities.Selections;

  /** the initial zoom window */
  initialZoomWindow?: Entities.TimeRange;

  /** Determines where zoom out defaults too */
  defaultZoomWindow?: Entities.TimeRange;

  /** (Optional) Markers for Waveform Pannel */
  markers?: Entities.Markers;

  /** Sets as a flex display if active */
  flex?: boolean;

  /**
   * Call back for toast, which is the notification pop up
   * 
   * @param message message to be displayed as a string
   * @param intent (optional) NONE, PRIMARY, WARNING, DANGER ex NONE = "none"
   * @param icon (optional) the way it looks
   * @param timeout (optional) time before message disappears
   */
  toast(message: string, intent?: Intent, icon?: IconName, timeout?: number): void;

  /** Unselects channels */
  clearSelectedChannels?(): void;

  /** Selects channel 
   * 
   * @param channelId Channel Id as a string
   */
  selectChannel?(channelId: string): void;

  /**
   * Call back that updates the measure window
   * 
   * @param stationId Station Id as a string
   * @param channel channel config as a Channel
   * @param startTimeSecs epoch seconds start
   * @param endTimeSecs epoch seconds end
   * @param isDefaultChannel boolean
   * @param removeSelection call back to remove selection
   */
  updateMeasureWindow?(stationId: string, channel: Entities.Channel,
    startTimeSecs: number, endTimeSecs: number, isDefaultChannel: boolean, removeSelection: () => void): void;
}

// tslint:disable-next-line:no-empty-interface
export interface WaveformPanelState {
  /** The position of the time popover */
  timePopoverPosition: Position;
}
