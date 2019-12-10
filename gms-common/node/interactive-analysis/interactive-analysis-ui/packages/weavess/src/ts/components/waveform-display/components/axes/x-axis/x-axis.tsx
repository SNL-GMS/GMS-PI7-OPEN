import * as d3 from 'd3';
import * as moment from 'moment';
import * as React from 'react';
import { DEFAULT_XAXIS_HEIGHT_PIXELS, MILLISECONDS_TO_SECONDS } from '../../../constants';
import './styles.scss';
import { XAxisProps, XAxisState } from './types';

/**
 * A D3-based Time Axis component
 */
export class XAxis extends React.PureComponent<XAxisProps, XAxisState> {

  /** A handle to the axis wrapper HTML element */
  public axisRef: HTMLElement | null;

  /** A handle to the svg selection d3 returns, where the axis will be created */
  private svgAxis: d3.Selection<Element | d3.EnterElement | Document | Window | null, {}, null, undefined>;

  /**
   * Constructor
   * 
   * @param props X Axis props as XAxisProps
   */
  public constructor(props: XAxisProps) {
    super(props);
    this.state = {};
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
  public static getDerivedStateFromProps(nextProps: XAxisProps, prevState: XAxisState) {
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
    console.error(`Weavess XAxis Error: ${error} : ${info}`);
  }

  /**
   * Called immediately after a compoment is mounted. 
   * Setting state here will trigger re-rendering.
   */
  public componentDidMount() {
    const svg = d3.select(this.axisRef)
      .append('svg')
      .attr('width', '100%')
      // tslint:disable-next-line:no-magic-numbers
      .attr('height', DEFAULT_XAXIS_HEIGHT_PIXELS)
      .style('fill', '#ddd');

    this.svgAxis = svg.append('g')
      .attr('class', 'x-axis-axis');
    this.update();
  }

  /**
   * Called immediately after updating occurs. Not called for the initial render.
   *
   * @param prevProps the previous props
   * @param prevState the previous state
   */
  public componentDidUpdate(prevProps: XAxisProps, prevState: XAxisState) {
    this.update();
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
    return (
      <div
        className="x-axis"
        ref={axis => { this.axisRef = axis; }}
        style={{
          height: `${DEFAULT_XAXIS_HEIGHT_PIXELS}px`,
          borderTop: this.props.borderTop ? '1px solid' : undefined,
        }}
      />
    );
  }

  /**
   * Re-draw the axis based on new parameters
   * Not a react life cycle method. Used to manually update the time axis
   * This is done to keep it performant, and not have to rerender the DOM 
   */
  public readonly update = () => {
    if (!this.axisRef) return;

    const durationSecs = this.props.endTimeSecs - this.props.startTimeSecs;
    const axisStart = this.props.startTimeSecs + (durationSecs * this.props.getViewRange()[0]);
    const axisEnd = this.props.startTimeSecs + (durationSecs * this.props.getViewRange()[1]);
    const x = d3.scaleUtc()
      .domain([new Date(axisStart * MILLISECONDS_TO_SECONDS), new Date(axisEnd * MILLISECONDS_TO_SECONDS)])
      .range([this.props.labelWidthPx, (this.axisRef.clientWidth - this.props.scrolbarWidthPx)]);

    // adding in some time axis date label formatting
    const tickFormatter = (date: Date) => moment.utc(date)
      .format('HH:mm:ss.SS');

    const spaceBetweenTicksPx = 150;
    const numTicks = Math.floor((this.axisRef.clientWidth - this.props.labelWidthPx) / spaceBetweenTicksPx);
    const tickSize = 7;
    const xAxis = d3.axisBottom(x)
      .ticks(numTicks)
      .tickSize(tickSize)
      .tickFormat(tickFormatter);
    this.svgAxis.call(xAxis);
  }
}
