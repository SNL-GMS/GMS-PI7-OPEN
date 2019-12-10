
export interface SpectrogramRendererProps {
  /** Epoch seconds start time */
  displayStartTimeSecs: number;

  /** Boolean is default channel */
  displayEndTimeSecs: number;

  /** Epoch start time in seconds */
  startTimeSecs: number;

  /** The time step of the spectrogram data (x-axis) */
  timeStep: number;

  /** The frequency step of the spectrogram data (y-axis) */
  frequencyStep: number;

  /**
   * The spectrogram data (time x frequency)
   * Provides the powers or intensity of the spectrogram
   */
  data: number[][];

  /**
   * Sets the Y axis bounds
   * 
   * @param min minimum bound as a number
   * @param max Maximum bound as a number
   */
  setYAxisBounds(min: number, max: number);

  /**
   * Custom color scale. Retruns a color
   * as a string for the given value.
   */
  colorScale?(value: number): string;
}

// tslint:disable-next-line:no-empty-interface
export interface SpectrogramRendererState {
}
