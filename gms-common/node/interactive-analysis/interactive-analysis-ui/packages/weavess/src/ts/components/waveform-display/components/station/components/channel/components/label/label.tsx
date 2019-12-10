import * as React from 'react';
import * as Entities from '../../../../../../../../entities';
import { YAxis } from '../../../../../axes';
import './style.scss';
import { LabelProps, LabelState } from './types';

/**
 * Label component. Describes a waveform (or other graphic component) and has optional events
 */
export class Label extends React.PureComponent<LabelProps, LabelState> {

  /** The y-axis references. */
  public yAxisRefs: { [id: string]: YAxis | null } = {};

  /**
   * Constructor
   * 
   * @param props Label props as LabelProps
   */
  public constructor(props: LabelProps) {
    super(props);
    this.state = {
    };
  }

  // ******************************************
  // BEGIN REACT COMPONENT LIFECYCLE METHODS
  // ******************************************

  /**
   * Invoked right before calling the render method, both on the initial mount
   * and on subsequent updates. It should return an object to update the state,
   * or null to update nothing.
   *
   * @param nextProps the next props
   * @param prevState the previous state
   */
  public static getDerivedStateFromProps(nextProps: LabelProps, prevState: LabelState) {
   return null; /* no-op */
  }

  /**
   * Catches exceptions generated in descendant components. 
   * Unhandled exceptions will cause the entire component tree to unmount.
   * 
   * @param error the error that was caught
   * @param info the information about the error
   */
  public componentDidCatch(error, info) {
    // tslint:disable-next-line:no-console
    console.error(`Weavess Label Error: ${error} : ${info}`);  }

  /**
   * Called immediately after a compoment is mounted. 
   * Setting state here will trigger re-rendering.
   */
  public componentDidMount() {
    /* no-op */
  }

  /**
   * Called immediately after updating occurs. Not called for the initial render.
   *
   * @param prevProps the previous props
   * @param prevState the previous state
   */
  public componentDidUpdate(prevProps: LabelProps, prevState: LabelState) {
    this.refreshYAxis();
  }

  /**
   * Called immediately before a component is destroyed. Perform any necessary 
   * cleanup in this method, such as cancelled network requests, 
   * or cleaning up any DOM elements created in componentDidMount.
   */
  public componentWillUnmount() {
    /* no-op */
  }

  // ******************************************
  // END REACT COMPONENT LIFECYCLE METHODS
  // ******************************************

  public render() {
    const isSelected = this.props.selections.channels
      && this.props.selections.channels.indexOf(this.props.channel.id) > -1;

    const isSelectedStyle = {
      filter: !this.props.isDefaultChannel ? 'brightness(0.7)' : undefined,
      textShadow: isSelected ? '0px 1px 15px' : 'initial'
    };

    // Build distString use km vs degree ('\u00B0') symbol depending on distanceUnits enum
    let distString = '';
    if (this.props.distance !== 0) {
      const fixBy = (this.props.distanceUnits === Entities.DistanceUnits.degrees) ? 1 : 2;
      distString += this.props.distance.toFixed(fixBy);
      distString += this.props.distanceUnits === Entities.DistanceUnits.km ? ' km' : '\u00B0';
    }
    return (
      <div
        onContextMenu={e => {
          // Prevents chrome context menu from appearing if
          // Left mouse + control is used to summon context menu
          if (e.button === 2) return;
          if (this.props.events && this.props.events.onChannelLabelClick) {
          if (e.button === 0 && e.ctrlKey) {
            e.preventDefault();
            e.stopPropagation();
            this.props.events.onChannelLabelClick(e, this.props.channel.id);
            return false;
          }
          }
        }}
        tabIndex={0}
        className="label"
      >
        <div className="label-container">
          {
            this.props.isDefaultChannel ?
            (
              /* render parent label with expansion button */
              <div
                className="label-container-left-parent"
              >
                {
                  /* render expansion button if there are additional channels */
                  this.props.isExpandable
                    ?
                    (
                      <div
                        className="label-container-left-parent-expansion-button"
                        onClick={e => {
                          if (this.props.toggleExpansion) {
                            const isExpanded = !this.props.expanded;
                            this.props.toggleExpansion();
                            if (isExpanded) {
                              if (this.props.events && this.props.events.onChannelExpanded) {
                                  this.props.events.onChannelExpanded(this.props.channel.id);
                              }
                            } else {
                                if (this.props.events && this.props.events.onChannelCollapsed) {
                                    this.props.events.onChannelCollapsed(this.props.channel.id);
                                }
                            }
                          }
                        }}
                      >
                        {this.props.expanded ? '-' : '+'}
                      </div>
                    )
                    : null
                }
              </div>
            )
            :
            (
              /* render child label without expansion button */
              <div
                className="label-container-left-child"
              />
            )
          }
          <div
            className="label-container-content"
          >
            <div
              className="label-container-content-label"
              style={{
                ...isSelectedStyle,
                // tslint:disable-next-line:no-magic-numbers
                maxWidth: `calc(${this.props.configuration.labelWidthPx}px - ${84}px)`,
                // tslint:disable-next-line:no-magic-numbers
                width: `calc(${this.props.configuration.labelWidthPx}px - ${84}px)`,
              }}
              onClick={e => {
                if (this.props.events && this.props.events.onChannelLabelClick) {
                  this.props.events.onChannelLabelClick(e, this.props.channel.id);
                }
              }}
            >
            {this.props.configuration.customLabel ?
            (
              <this.props.configuration.customLabel
                {
                  ...this.props
                }
              />
            ) :
            (
              <span>
                {this.props.channel.name}
                <div>
                  <p>{distString}{' '}
                    <span className="label-container-content-mask-indicator">
                      {this.props.showMaskIndicator ? 'M' : null}
                    </span>
                  </p>
                </div>
              </span>
            )
           }
            </div>
            <div
              style={{
                height: '100%'
              }}
            >
              {this.props.yAxisBounds.map((yAxisBounds, index) =>
                <YAxis
                  key={`${this.props.channel.id}_yaxis_${index}`}
                  ref={ref => this.yAxisRefs[index] = ref}
                  maxAmplitude={yAxisBounds.maxAmplitude}
                  minAmplitude={yAxisBounds.minAmplitude}
                  heightInPercentage={yAxisBounds.heightInPercentage}
                />
              )}
            </div>
          </div>
        </div>
      </div>
    );
  }

  /**
   * Refreshes the y-axis for the label
   */
  public readonly refreshYAxis = () => {
    if (this.yAxisRefs) {
      Object.keys(this.yAxisRefs)
        .forEach(key => {
        const yAxis = this.yAxisRefs[key];
        if (yAxis) {
          yAxis.display();
        }
      });
    }
  }

}
