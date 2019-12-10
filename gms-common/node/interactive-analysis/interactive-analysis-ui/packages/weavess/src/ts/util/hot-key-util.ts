import { isEqual } from 'lodash';

/** Constant string representing ht key seperator */
const HotKeySeparator = '+';

/** Constant string representing the `Meta` key */
const Meta = 'Meta';

/** Constant string representing the `Control` key */
const Control = 'Control';

/** Constant string representing the `Alt` key */
const Alt = 'Alt';

/** Constant string representing the `Shift` key */
const Shift = 'Shift';

/**
 * The Hot Key array based on the KeyboardEvent.
 * 
 * @param event the keyboard event as KeyboardEvent
 * 
 * @returns The Hot Key array
 */
export const getHotKeyArray = (event: KeyboardEvent) => {
  const hotKeyArray: string[] = [];

  if (event.metaKey) {
    hotKeyArray.push(Meta);
  }

  if (event.ctrlKey) {
    hotKeyArray.push(Control);
  }

  if (event.altKey) {
    hotKeyArray.push(Alt);
  }

  if (event.shiftKey) {
    hotKeyArray.push(Shift);
  }

  // add non-control characters
  // see: https://developer.mozilla.org/en-US/docs/Web/API/KeyboardEvent/key/Key_Values
  if (event.key !== Meta && event.key !== Control && event.key !== Alt && event.key !== Shift) {
    hotKeyArray.push(event.code);
  }

  return hotKeyArray;
};

/**
 * Hot Key string based on the KeyboardEvent.
 * 
 * @param event the keyboard event
 * 
 * @returns Hot Key string
 */
export const getHotKeyString = (event: KeyboardEvent) => getHotKeyString(event)
  .join(HotKeySeparator);

/**
 * Is hot key satisifed
 * 
 * @param event the keyboard event
 * @param hotKeyCommand the hotkey command
 * 
 * @returns true if the hotkey command is satisfied. False otherwise.
 */
export const isHotKeyCommandSatisfied = (event: KeyboardEvent, hotKeyCommand: string) => {
  if (!event) {
    return false;
  }

  if (!hotKeyCommand) {
    return false;
  }

  // remove all whitespace
  const noWhiteSpaceCommand = hotKeyCommand.replace(/\s/g, '');

  const hotKeyArray = getHotKeyArray(event);
  const commandArray = noWhiteSpaceCommand.split(HotKeySeparator);

  return isEqual(hotKeyArray.sort(), commandArray.sort());
};
