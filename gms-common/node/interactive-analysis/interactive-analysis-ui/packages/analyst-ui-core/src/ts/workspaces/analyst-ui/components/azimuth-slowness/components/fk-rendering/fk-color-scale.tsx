import * as d3 from 'd3';
import * as React from 'react';
import { FkUnits } from '../../types';
import { createColorScaleImageBitmap } from '../fk-util';

/**
 * FkColorScale Props
 */
export interface FkColorScaleProps {
  minSlow: number;
  maxSlow: number;
  fkUnits: FkUnits;
}

/**
 * FkColorScale State
 */
// tslint:disable-next-line:no-empty-interface
export interface FkColorScaleState {
}

/**
 * The color scale size.
 */
export interface ColorScaleSize {
  width: number;
  height: number;
}

/**
 * FkColorScale Component
 */
export class FkColorScale extends React.Component<FkColorScaleProps, FkColorScaleState> {

  public static readonly padding: number = 25;

  /** The color scale size. */
  private static readonly colorScaleSize: ColorScaleSize = { width: 80, height: 240 };

  /** Reference to the canvas to draw the color scale. */
  private canvasRef: HTMLCanvasElement | undefined;

  /** Canvas rendering context used to draw the color scale. */
  private ctx: CanvasRenderingContext2D;

  /** The current color scale represented as an ImageBitmap. */
  private currentImage: ImageBitmap | undefined;

  /** The x-axis div container. */
  private xAxisContainerRef: HTMLDivElement | undefined;

  /**
   * constructor
   */
  public constructor(props: FkColorScaleProps) {
    super(props);
  }

  // ***************************************
  // BEGIN REACT COMPONENT LIFECYCLE METHODS
  // ***************************************

  /**
   * React component lifecycle
   */
  public render() {
    return (
      <div
        className="fk-color-scale"
      >
        <div
          className="fk-color-scale__xaxis"
          ref={ref => this.xAxisContainerRef = ref}
        />
        <canvas
          className="fk-color-scale__canvas"
          ref={ref => this.canvasRef = ref}
        />
        {
          this.props.fkUnits === FkUnits.POWER ?
            <div className="fk-color-scale__units">
              (db)
            </div>
            : null
        }
      </div>
    );
  }

  /**
   * React component lifecycle
   */
  public async componentDidMount() {
    await this.updateBitmap();
  }

  /**
   * React component lifecycle
   */
  public async componentDidUpdate(prevProps: FkColorScaleProps) {
    await this.updateBitmap();
  }

  /**
   * sets parameters and updates bitmap 
   */
  private async updateBitmap() {
    this.ctx = this.canvasRef.getContext('2d');
    this.ctx.imageSmoothingEnabled = true;
    this.currentImage = await createColorScaleImageBitmap(
        FkColorScale.colorScaleSize.width,
        FkColorScale.colorScaleSize.height);
    this.draw();
  }

  /**
   * Draws the image to the context
   */
  private draw() {
    if (this.canvasRef) {
      const height = 50;
      this.canvasRef.width = this.xAxisContainerRef.clientWidth - (FkColorScale.padding * 2);
      this.canvasRef.height = height;
      this.ctx.drawImage(this.currentImage, 0, 0, this.canvasRef.width, height);

      this.createXAxis();
    }
  }

  /**
   * Create and draw the x-axis.
   */
  private createXAxis() {
    if (!this.xAxisContainerRef) return;
    this.xAxisContainerRef.innerHTML = '';

    const svg = d3.select(this.xAxisContainerRef)
      .append('svg')
      .attr('width', this.xAxisContainerRef.clientWidth)
      .attr('height', this.xAxisContainerRef.clientHeight)
      .style('fill', '#ddd');

    const svgAxis = svg.append('g')
      .attr('class', 'fk-axis');
    const x =
      this.props.fkUnits === FkUnits.FSTAT ?
        d3.scaleLinear()
        .domain([this.props.minSlow, this.props.maxSlow])
        .range([FkColorScale.padding, this.xAxisContainerRef.clientWidth - FkColorScale.padding])
        : d3.scaleLog()
        .domain([this.props.minSlow, this.props.maxSlow])
        .range([FkColorScale.padding, this.xAxisContainerRef.clientWidth - FkColorScale.padding]);
    const range = this.props.maxSlow - this.props.minSlow;
    const tickSize = 7;
    const rangeOfScaleInRealPx = this.xAxisContainerRef.clientWidth - FkColorScale.padding - FkColorScale.padding;
    const logarithmicHalfOfScale =
      (rangeOfScaleInRealPx) / 2;
    const logarithmicQuarterOfScale =
      (rangeOfScaleInRealPx) / 4;
    const logarithmicThreeQuarterOfScale =
      (rangeOfScaleInRealPx) * 3 / 4;

    const xAxis = this.props.fkUnits === FkUnits.FSTAT ?
      d3.axisBottom(x)
        .tickSize(tickSize)
        .tickValues([
          this.props.minSlow,
          this.props.minSlow + range / 4,
          this.props.minSlow + range / 2,
          (this.props.minSlow + (range * 3 / 4)),
          this.props.maxSlow
        ])
        .tickFormat(d3.format('.2'))
      : d3.axisBottom(x)
      .tickSize(tickSize)
      .tickValues([
        x.invert(FkColorScale.padding),
        x.invert(FkColorScale.padding + logarithmicQuarterOfScale),
        x.invert(FkColorScale.padding + logarithmicHalfOfScale),
        x.invert(FkColorScale.padding + logarithmicThreeQuarterOfScale),
        x.invert(this.xAxisContainerRef.clientWidth - FkColorScale.padding)
      ])
      .tickFormat(d3.format('.2'));
    svgAxis.call(xAxis);
  }
}
