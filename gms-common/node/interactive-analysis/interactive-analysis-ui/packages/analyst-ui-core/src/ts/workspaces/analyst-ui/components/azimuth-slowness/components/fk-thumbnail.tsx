import * as React from 'react';
import { FkPowerSpectra } from '~graphql/fk/types';
import { FkUnits } from '../types';
import * as fkUtil from './fk-util';

/**
 * Fk Thumbnail Props.
 */
export interface FkThumbnailProps {
  fkData: FkPowerSpectra;
  sizePx: number;
  label: string;
  fkUnit: FkUnits;

  dimFk: boolean;
  highlightLabel?: boolean;
  predictedPoint?: any;
  arrivalTime?: number;
  selected?: boolean;
  showFkThumbnailMenu?(x: number, y: number): void;
  onClick?(e: React.MouseEvent<HTMLDivElement>): void;
}

/**
 * Fk Thumbnail State
 */
// tslint:disable-next-line:no-empty-interface
export interface FkThumbnailState {
  currentFkDisplayData: number[][];
}

/**
 * A single fk thumbnail in the thumbnail-list
 */
export class FkThumbnail extends React.Component<FkThumbnailProps, FkThumbnailState> {

  /** destination to draw the fk. */
  private canvasRef: HTMLCanvasElement | undefined;

  /** Used to resize the canvas to fit the container. */
  private containerRef: HTMLDivElement;

  /** The current fk represented as an ImageBitmap. */
  private currentImage: ImageBitmap;

  public constructor(props: FkThumbnailProps) {
    super(props);
    this.state = {
      currentFkDisplayData: [] // Init empty (double array)
    };
  }

  // ***************************************
  // BEGIN REACT COMPONENT LIFECYCLE METHODS
  // ***************************************

  /**
   * React component lifecycle.
   */
  public render() {
    const classNames = [
      'fk-thumbnail',
      this.props.selected ? 'selected' : undefined].join(' ');
    return (
      <div
        ref={ref => this.containerRef = ref}
        className={classNames}
        style={{
          width: `${this.props.sizePx}px`,
          height: `${this.props.sizePx}px`
        }}
        onClick={this.props.onClick ? this.props.onClick : undefined}
        onContextMenu={e => this.showThumbnailContextMenu(e)}
      >
        <div
          className={!this.props.highlightLabel || this.props.dimFk ?
            'fk-thumbnail__label--reviewed' : 'fk-thumbnail__label'}
        >
          {this.props.label}
        </div>
        <canvas
          className="fk-thumbnail__canvas"
          height={this.props.sizePx}
          width={this.props.sizePx}
          ref={ref => this.canvasRef = ref}
        />
      </div>
    );
  }

  /**
   * React component lifecycle.
   */
  public async componentDidMount() {
    const fkDisplayData = fkUtil.getFkHeatmapArrayFromFkSpectra(this.props.fkData, this.props.fkUnit);
    const [min, max] = fkUtil.computeMinMaxFkValues(fkDisplayData);
    this.currentImage = await fkUtil.createFkImageBitmap(fkDisplayData, min, max);
    fkUtil.draw(
      this.canvasRef, this.containerRef, this.currentImage, this.props.fkData,
      this.props.predictedPoint, this.props.arrivalTime, 0, true, this.props.dimFk);
    this.setState({
      currentFkDisplayData: fkDisplayData
    });
  }

  /**
   * React component lifecycle.
   */
  public async componentDidUpdate(prevProps: FkThumbnailProps) {
    const fkDisplayData = fkUtil.getFkHeatmapArrayFromFkSpectra(this.props.fkData, this.props.fkUnit);

    if (this.state.currentFkDisplayData !== fkDisplayData ||
       this.props.fkUnit !== prevProps.fkUnit || this.props.dimFk !== prevProps.dimFk) {
      const [min, max] = fkUtil.computeMinMaxFkValues(fkDisplayData);
      this.currentImage = await fkUtil.createFkImageBitmap(fkDisplayData, min, max);
      fkUtil.draw(
        this.canvasRef, this.containerRef, this.currentImage, this.props.fkData,
        this.props.predictedPoint, this.props.arrivalTime, 0, true, this.props.dimFk);
      this.setState({
        currentFkDisplayData: fkDisplayData
      });
    } else if (prevProps.sizePx !== this.props.sizePx) {
      fkUtil.draw(
        this.canvasRef, this.containerRef, this.currentImage, this.props.fkData,
        this.props.predictedPoint, this.props.arrivalTime, 0, true, this.props.dimFk);
    }
  }

  /**
   * Displays a context menu for reviewing/clearing an fk
   */
  private showThumbnailContextMenu(e: React.MouseEvent<HTMLDivElement>) {
    e.preventDefault();
    if (this.props.showFkThumbnailMenu) {
      this.props.showFkThumbnailMenu(e.clientX, e.clientY);
    }
  }
}
