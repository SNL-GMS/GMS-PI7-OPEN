
import { delay } from 'lodash';
import * as React from 'react';

/**
 * A simple utility for handling both single and double click events when using
 * mouse down/up events.
 */
export class SingleDoubleClickEvent {

  /** delay in ms for handling single click events */
  public static readonly SINGLE_CLICK_DELAY_MS: number = 600;

  /** timeout in ms to clear out the double click behavior */
  public static readonly DOUBLE_CLICK_TIMEOUT_MS: number = 2000;

  /** timer id for the double click delay */
  private doubleClickDelayTimerId: number | undefined;

  /** timer id for the single click delay */
  private singleClickDelayTimerId: number | undefined;

  /** 
   * A boolean flag that indicates if the mouse up event should be firerd.
   * Used to prevent confusion between single and double click events.
   */
  private shouldFireSingleClickEvent: boolean = true;

  /**
   * Constructor
   */
  public constructor() { /* nothing to do */ }

  /**
   * Handles a double click event.
   * 
   * @param event the event
   * @param handleDoubleClickEvent the event to be fired to handle for a double event
   */
  public readonly onDoubleClick = (
    event: React.MouseEvent<HTMLDivElement> | MouseEvent,
    handleDoubleClickEvent?: (event: React.MouseEvent<HTMLDivElement> | MouseEvent) => void
    ) => {
    // flag that a double click event is in process, to prevent handling a single click
    this.shouldFireSingleClickEvent = false;

    // clear any previous delay
    if (this.doubleClickDelayTimerId) {
      clearTimeout(this.doubleClickDelayTimerId);
      this.doubleClickDelayTimerId = undefined;
    }

    if (handleDoubleClickEvent) {
      handleDoubleClickEvent(event);
    }

    delay(
      (e: React.MouseEvent<HTMLDivElement> | MouseEvent) => {
        // clear out the double click event to allow for single clicks
        this.shouldFireSingleClickEvent = true;
      },
      // create a new event to ensure that the event is in scope for the delayed action
      SingleDoubleClickEvent.DOUBLE_CLICK_TIMEOUT_MS, new MouseEvent(event.type, event));
  }

  /**
   * Handles a single click event.
   * 
   * @param event the event
   * @param handleSingleClickEvent the event to be fired to handle for a single event
   */
  public readonly onSingleClickEvent = (
    event: React.MouseEvent<HTMLDivElement> | MouseEvent,
    handleSingleClickEvent?: (e: React.MouseEvent<HTMLDivElement> | MouseEvent) => void) => {

    // clear any previous delay
    if (this.singleClickDelayTimerId) {
      clearTimeout(this.singleClickDelayTimerId);
      this.singleClickDelayTimerId = undefined;
    }

    // delay executing a single click to ensure the user does not do a double click
    delay(
      (e: React.MouseEvent<HTMLDivElement> | MouseEvent) => {
        if (this.shouldFireSingleClickEvent && handleSingleClickEvent) {
          handleSingleClickEvent(e);
        }
      },
      // create a new event to ensure that the event is in scope for the delayed action
      SingleDoubleClickEvent.SINGLE_CLICK_DELAY_MS, new MouseEvent(event.type, event));
  }
}
