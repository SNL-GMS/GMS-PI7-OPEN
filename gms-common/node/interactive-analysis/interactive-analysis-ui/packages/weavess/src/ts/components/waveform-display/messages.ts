/**
 * Defines Messages that can be issued to the user 
 * through toasts.
 */
// tslint:disable-next-line:no-unnecessary-class
export class Messages {
  /** Message indicating max zoom has been reached */
  public static maxZoom: string = 'Max zoom reached';

  /** Message indicating that the measure window is disabled */
  public static measureWindowDisabled: string = 'Measure window disabled';

  /** Message indicating signal detection modification has been disabled */
  public static signalDetectionModificationDisabled: string = 'Signal detection modification disabled for channel';

  /** Message indicating predictive phase modification has been disabled */
  public static predictedPhaseModificationDisabled: string = 'Predicted phase modification disabled for channel';

  /** Message indicating that mask modification has been disabled */
  public static maskModificationDisabled: string = 'Mask modification disabled for channel';

}
