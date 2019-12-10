import { Alignment, Button, ContextMenu, Icon, MenuItem } from '@blueprintjs/core';
import { IconNames } from '@blueprintjs/icons';
import * as React from 'react';
import { PopoverProps, PopoverState } from './types';
/**
 * Renders button in toolbar that creates and dismisses popovers
 * Not for external use
 */
export class PopoverButtonComponent extends React.Component<PopoverProps, PopoverState> {
  /** Internal reference to the button itself */
  private internalRef: HTMLDivElement;

  private constructor(props) {
      super(props);
      this.state = {
          isExpanded: false
      };
    }

  /**
   * React component lifecycle.
   */
  public render() {
    const widthStr = this.props.widthPx ? `${this.props.widthPx}px` : undefined;
    const element = (
      <div ref={ref => { if (ref) { this.internalRef = ref; }}}>
          {
            this.props.renderAsMenuItem ?
            (
              <MenuItem
                disabled={this.props.disabled}
                icon={IconNames.MENU_OPEN}
                text={this.props.label}
                label={'opens dialog'}
                onClick={event => {event.stopPropagation() ; this.togglePopover(); }}
              />
            )
          : (
              <Button
                title={this.props.tooltip}
                disabled={this.props.disabled}
                onClick={() => {this.togglePopover(); }}
                active={this.state.isExpanded}
                style={{width: widthStr}}
                alignText={this.props.onlyShowIcon ? Alignment.CENTER : Alignment.LEFT}
                className={this.props.onlyShowIcon ? 'toolbar-button--icon-only' : 'toolbar-button'}
              >
                <span>
                  {this.props.onlyShowIcon ? null : this.props.label}
                </span>
                <Icon
                  title={false}
                  icon={this.props.icon ? this.props.icon : IconNames.CHEVRON_DOWN}
                />
              </Button>
          )
          }
      </div>
    );
    return element;
  }
  /**
   * Returns if the popover is expanded
   * @returns boolean
   */
  public isExpanded = () => this.state.isExpanded;
  /**
   * Toggles the popuover
   * 
   * @param leftOff left offset to render popover
   * @param topSet top offset to render popover
   */
  public togglePopover = (leftOffset?: number, topOffset?: number) => {
    if (this.state.isExpanded) {
      ContextMenu.hide();
      this.setState({ isExpanded: false });
    } else {
      const left = this.props.renderAsMenuItem ?
      this.internalRef.getBoundingClientRect().left + this.internalRef.scrollWidth
      : this.internalRef.getBoundingClientRect().left;
      // The plus four is a chosen offset - has no real world meaning
      const top = this.props.renderAsMenuItem ?
          this.internalRef.getBoundingClientRect().top
          : this.internalRef.getBoundingClientRect().top + this.internalRef.scrollHeight + 4;
      ContextMenu.hide();
      ContextMenu.show(
          this.props.popupContent,
          {
              left: leftOffset ? leftOffset : left,
              top: topOffset ? topOffset : top
          },
          () => {
              // TODO need to onblur for the popup itself
              this.props.onPopoverDismissed();
              this.setState({ isExpanded: false });
          });
      this.setState({ isExpanded: true });
    }
  }

}
