import { IconName, Intent, IToaster, Position, Toaster as BlueprintToaster } from '@blueprintjs/core';
import { IconNames } from '@blueprintjs/icons';

/**
 * Wrapper around blueprint js toaster. Used for warning, errors, and messaages in the UI
 */
export class Toaster {

  /** The toaster reference for user notification pop-ups */
  private readonly toaster: IToaster;

  /**
   * constructor
   * 
   * @param position blueprint js position, default is Position.BOTTOM_RIGHT
   */
  public constructor(
    position: Position.TOP_LEFT| Position.TOP | Position.TOP_RIGHT |
              Position.BOTTOM_RIGHT | Position.BOTTOM | Position.BOTTOM_LEFT | undefined = Position.BOTTOM_RIGHT) {
    this.toaster = BlueprintToaster.create({position});
  }

  /**
   * Display a toast message.
   * 
   * @param message string to be toasted
   * @param intent blueprint js intent color scheme
   * @param icon blueprint js icon to be shown in toast
   * @param timeout miliseconds message is displayed
   */
  public readonly toast = (message: string,
    intent?: Intent, icon?: IconName, timeout?: number) => {
    if (this.toaster) {
      // TODO check for unique message
      if (this.toaster.getToasts().length === 0) {
        this.toaster.show({
          message,
          intent: ((intent) ? intent : Intent.NONE),
          icon: ((icon) ? icon : IconNames.INFO_SIGN),
          // tslint:disable-next-line:no-magic-numbers
          timeout: ((!timeout) ? 4000 : timeout)
        });
      }
    }
  }

  /**
   * Display a INFO toast message.
   * 
   * @param message string to be toasted
   * @param timeout miliseconds message is displayed
   */
  public readonly toastInfo = (message: string, timeout?: number) => {
    this.toast(message, Intent.NONE, IconNames.INFO_SIGN, timeout);
  }

  /**
   * Display a WARNING toast message.
   * 
   * @param message string to be toasted
   * @param timeout miliseconds message is displayed
   */
  public readonly toastWarn = (message: string, timeout?: number) => {
    this.toast(message, Intent.WARNING, IconNames.WARNING_SIGN, timeout);
  }

  /**
   * Display a ERROR toast message.
   * 
   * @param message string to be toasted
   * @param timeout miliseconds message is displayed
   */
  public readonly toastError = (message: string, timeout?: number) => {
    this.toast(message, Intent.DANGER, IconNames.ERROR, timeout);
  }
}
