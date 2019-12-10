import * as d3 from 'd3';
import { mean } from 'lodash';
import * as React from 'react';
import './styles.scss';
import { YAxisProps, YAxisState } from './types';

/**
 * Y axis for an individual waveform
 */
export class YAxis extends React.Component<YAxisProps, YAxisState> {

  /** Handle to the axis wrapper HTMLElement */
  private axisRef: HTMLElement | null;

  /** Handle to the d3 svg selection, where the axis will be created. */
  private svgAxis: d3.Selection<Element | d3.EnterElement | Document | Window | null, {}, null, undefined>;

  /**
   * Constructor
   * 
   * @param props Y Axis props as YAxisProps
   */
  public constructor(props: YAxisProps) {
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
  public static getDerivedStateFromProps(nextProps: YAxisProps, prevState: YAxisState) {
    return null; /* no-op */
  }

  /**
   * React lifecycle
   * 
   * @param nextProps props for the axis of type YAxisProps
   * 
   * @returns boolean
   */
  public shouldComponentUpdate(nextProps: YAxisProps) {
    const hasChanged =
      !(this.props.maxAmplitude === nextProps.maxAmplitude &&
        this.props.minAmplitude === nextProps.minAmplitude &&
        this.props.heightInPercentage === nextProps.heightInPercentage);
    return hasChanged;
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
    console.error(`Weavess YAxis Error: ${error} : ${info}`);
  }

  /**
   * Called immediately after a compoment is mounted. 
   * Setting state here will trigger re-rendering.
   */
  public componentDidMount() {
    if (!this.axisRef) return;

    const svg = d3.select(this.axisRef)
      .append('svg');
    svg.attr('height', this.axisRef.clientHeight)
      // tslint:disable-next-line:no-magic-numbers
      .attr('width', 50);
    this.svgAxis = svg.append('g')
      .attr('class', 'y-axis-axis')
      // tslint:disable-next-line:no-magic-numbers
      .attr('transform', `translate(${49},0)`);
    this.display();
  }

  /**
   * Called immediately after updating occurs. Not called for the initial render.
   *
   * @param prevProps the previous props
   * @param prevState the previous state
   */
  public componentDidUpdate(prevProps: YAxisProps, prevState: YAxisState) {
    this.display();
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
        className="y-axis"
        ref={axisRef => { this.axisRef = axisRef; }}
        style={{
          height: `${this.props.heightInPercentage}%`
        }}
      />
    );
  }

  /**
   * Draw the axis
   */
  public readonly display = () => {
    if (!this.axisRef) return;

    const totalTicks = 3;
    const paddingPx = 6;
    const heightPx = this.axisRef.clientHeight;

    d3.select(this.axisRef)
      .select('svg')
      .attr('height', heightPx)
      // tslint:disable-next-line:no-magic-numbers
      .attr('width', 50);

    // determine the number of degress amplitude per pixel
    const amplitidePerPx = (this.props.maxAmplitude - this.props.minAmplitude) / heightPx;

    // adjust the min/max witha pixel padding to ensure that all of the number are visible
    const min = this.props.minAmplitude + (paddingPx * amplitidePerPx);
    const max = this.props.maxAmplitude - (paddingPx * amplitidePerPx);

    const yAxisScale = d3.scaleLinear()
      .domain([min, max])
      .range([heightPx - paddingPx, paddingPx]);
    const tickValues = [min, mean([min, max]),  max];

    const yAxis = d3.axisLeft(yAxisScale)
      .ticks(totalTicks)
      .tickFormat((value: number) => value.toFixed(1))
      .tickSizeOuter(0)
      .tickValues(tickValues);

    this.svgAxis.call(yAxis);

  }
}
