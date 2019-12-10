import * as d3 from 'd3';
import * as lodash from 'lodash';
import * as React from 'react';
import * as THREE from 'three';
import * as Entities from '../../../../../../entities';
import { isHotKeyCommandSatisfied } from '../../../../../../util/hot-key-util';
import { DEFAULT_LABEL_WIDTH_PIXELS, PERCENT_100 } from '../../../../constants';
import { Messages } from '../../../../messages';
import { ContentRenderer, Label, SpectrogramRenderer, WaveformRenderer } from './components';
import { EmptyRenderer } from './components/empty-renderer';
import { ChannelProps, ChannelState } from './types';

import './style.scss';

/**
 * Channel Component. Contains a Label, a Waveform (or other graphic component) and optional events
 */
export class Channel extends React.PureComponent<ChannelProps, ChannelState> {

  /** The label container reference. */
  public labelContainerRef: HTMLElement;

  /** The label reference. */
  public labelRef: Label;

  /** The empty container reference. */
  private emptyContainerRef: HTMLElement;

  /** The empty renderer reference. */
  private emptyRendererRef: EmptyRenderer;

  /** The waveform container reference. */
  private waveformContainerRef: HTMLElement;

  /** The wavefrom content reference. */
  private waveformContentRef: ContentRenderer;

  /** The waveform renderer reference. */
  private waveformRendererRef: WaveformRenderer;

  /** The spectrogram container reference. */
  private spectrogramContainerRef: HTMLElement;

  /** The sepectrogram content reference. */
  private spectrogramContentRef: ContentRenderer;

  /** The spectrogram renderer reference. */
  private spectrogramRendererRef: SpectrogramRenderer;

  /** Flag that indicates the apmplitude is being adjusted */
  private isAdjustingAmplitude: boolean = false;

  /** If true, enable this.props.events...onMaskClick */
  private maskCreateHotKeyPressed: boolean = false;

  /** Current mouse position in [0,1] */
  private mouseXPosition: number = 0;

  /** Current mouse position in pixels from the left of the window */
  private mousePosition: Entities.MousePosition;

  /**
   * Constructor
   * 
   * @param props Channel props as ChannelProps
   */
  public constructor(props: ChannelProps) {
    super(props);
    this.state = {
      waveformYAxisBounds: {
        minAmplitude: -1,
        maxAmplitude: 1,
        heightInPercentage: PERCENT_100 / 2
      },
      spectrogramYAxisBounds: {
        minAmplitude: -1,
        maxAmplitude: 1,
        heightInPercentage: PERCENT_100 / 2
      }
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
  public static getDerivedStateFromProps(nextProps: ChannelProps, prevState: ChannelState) {
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
    console.error(`Channel Error: ${error} : ${info}`);
  }

  /**
   * Called immediately after a compoment is mounted. 
   * Setting state here will trigger re-rendering.
   */
  public componentDidMount() {
    // set the initial mouse position
    const canvas = this.props.canvasRef();
    if (canvas) {
      this.mousePosition = {
        clientX: canvas.getBoundingClientRect().left,
        clientY: canvas.getBoundingClientRect().top
      };
      this.mouseXPosition = 0;
    }
  }

  /**
   * Called immediately after updating occurs. Not called for the initial render.
   *
   * @param prevProps the previous props
   * @param prevState the previous state
   */
  public componentDidUpdate(prevProps: ChannelProps, prevState: ChannelState) {
    const { numberOfRenderers } = this.getContent();

    const waveformYAxisBounds: Entities.YAxisBounds = {
      ...this.state.waveformYAxisBounds,
      heightInPercentage: PERCENT_100 / numberOfRenderers
    };

    const spectrogramYAxisBounds: Entities.YAxisBounds = {
      ...this.state.spectrogramYAxisBounds,
      heightInPercentage: PERCENT_100 / numberOfRenderers
    };

    if (!lodash.isEqual(waveformYAxisBounds, prevState.waveformYAxisBounds) ||
      !lodash.isEqual(spectrogramYAxisBounds, prevState.spectrogramYAxisBounds)) {
      this.setState({
        waveformYAxisBounds,
        spectrogramYAxisBounds
      });
    }
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

  // tslint:disable-next-line:cyclomatic-complexity
  public render() {
    return (
    <div
      className="channel"
      onKeyDown={this.onKeyDown}
      style={{
        height: `${this.props.height}px`
      }}
    >
      {this.renderLabel()}
      {this.renderContent()}
    </div>);
  }

  /**
   * Returns the current mouse position.
   * 
   * @returns the mouse position
   */
  public readonly getMousePosition = (): Entities.MousePosition => this.mousePosition;

  /**
   * Returns the time in seconds for the current mouse x position
   * 
   * @returns the time in seconds
   */
  public readonly getTimeSecs = (): number =>
    this.props.computeTimeSecsForMouseXPosition(this.mouseXPosition)

  /**
   * Render the scene of the channel.
   */
  public renderScene = (
    renderer: THREE.WebGLRenderer,
    boundsRect: ClientRect | DOMRect) => {

    if (this.waveformContainerRef && this.waveformRendererRef) {
      this.internalRenderScene(
        renderer,
        boundsRect,
        this.waveformRendererRef.scene,
        this.waveformRendererRef.camera,
        this.waveformContainerRef);
    }

    if (this.spectrogramContainerRef && this.spectrogramRendererRef) {
      this.internalRenderScene(
        renderer,
        boundsRect,
        this.spectrogramRendererRef.scene,
        this.spectrogramRendererRef.camera,
        this.spectrogramContainerRef);
    }

    if (this.emptyContainerRef && this.emptyRendererRef) {
        this.internalRenderScene(
          renderer,
          boundsRect,
          this.emptyRendererRef.scene,
          this.emptyRendererRef.camera,
          this.emptyContainerRef);
      }
  }

  /**
   * Updates the channel scroll position. Forces the
   * label to always be in view and alighned to the left.
   */
  public readonly updateScrollPosition = (scrollWidth: number, scrollLeft: number) => {
    if (this.labelContainerRef) {
      this.labelContainerRef.style.left = `${scrollLeft}px`;
    }

    const canvasRef = this.props.canvasRef();
    const clientWidth: number = (canvasRef) ? canvasRef.clientWidth : 0;
    const descriptionLabelRightPosition =
    `calc(${scrollWidth}px - ${clientWidth}px - ${scrollLeft}px - ${DEFAULT_LABEL_WIDTH_PIXELS}px + 6px)`;

    if (this.waveformContentRef && this.waveformContentRef.descriptionLabelRef) {
      this.waveformContentRef.descriptionLabelRef.style.right = descriptionLabelRightPosition;
    }

    if (this.spectrogramContentRef && this.spectrogramContentRef.descriptionLabelRef) {
      this.spectrogramContentRef.descriptionLabelRef.style.right = descriptionLabelRightPosition;
    }
  }

  /**
   * Reset the amplitude of the waveform.
   */
  public resetAmplitude = () => {
    if (this.waveformRendererRef) {
      this.waveformRendererRef.resetAmplitude();
    }
  }

  /**
   * Removes/hides the measure window for the channel.
   * Sets the measure window selection div display to none.
   */
  public removeMeasureWindowSelection = () => {
    if (this.waveformContentRef && this.waveformContentRef.measureWindowSelectionAreaRef) {
      this.waveformContentRef.measureWindowSelectionAreaRef.style.display = 'none';
    }

    if (this.spectrogramContentRef && this.spectrogramContentRef.measureWindowSelectionAreaRef) {
      this.spectrogramContentRef.measureWindowSelectionAreaRef.style.display = 'none';
    }
  }

  /**
   * Renders the label of the channel
   */
  private readonly renderLabel = (): React.ReactFragment => {
    const {waveform, channelSegment, spectrogram } = this.getContent();

    const yAxisBounds: Entities.YAxisBounds[] = [];
    if (waveform && channelSegment && channelSegment) {
      yAxisBounds.push(this.state.waveformYAxisBounds);
    }
    if (spectrogram) {
      yAxisBounds.push(this.state.spectrogramYAxisBounds);
    }

    return (
      <React.Fragment>
        <div
          className="channel-label-container"
          ref={ref => { if (ref) { this.labelContainerRef = ref; } }}
          style={{
            height: `${this.props.height}px`,
            width: `${this.props.configuration.labelWidthPx}px`
          }}
        >
          <Label
            ref={ref => { if (ref) { this.labelRef = ref; } }}
            {
              ...this.props
            }
            events={this.props.events ? this.props.events.labelEvents : undefined}
            yAxisBounds={yAxisBounds}
          />
        </div>
      </React.Fragment>
    );
  }

  /**
   * Get the content information of the channel
   */
  private readonly getContent = () => {
    const waveform = this.props.shouldRenderWaveforms ? this.props.channel.waveform : undefined;
    const spectrogram = this.props.shouldRenderSpectrograms ? this.props.channel.spectrogram : undefined;
    const channelSegment = waveform && waveform.channelSegments &&
      waveform.channelSegments.has(waveform.channelSegmentId) ?
      waveform.channelSegments.get(waveform.channelSegmentId) : undefined;
    const numberOfRenderers = ((waveform && channelSegment) && spectrogram) ? 2 : 1;
    return {
      waveform,
      channelSegment,
      spectrogram,
      numberOfRenderers
    };
  }

  /**
   * Renders the content of the channel
   */
  private readonly renderContent = (): React.ReactFragment => {
    const {waveform, spectrogram } = this.getContent();

    return (
      (waveform || spectrogram) ?
        (
          <React.Fragment>
            {this.renderWaveform()}
            {this.renderSpectrogram()}
          </React.Fragment>
        )
        :
        (
          <React.Fragment>
            {this.renderNoGraphics()}
          </React.Fragment>
        )
    );
  }

  /**
   * Renders the channel content with no graphics
   */
  private readonly renderNoGraphics = (): React.ReactFragment =>
    (
      <React.Fragment>
        <div
          className="channel-content-container"
          ref={ref => { if (ref) { this.emptyContainerRef = ref; } }}
          style={{
            height: `${this.props.height}px`,
            width: `calc(100% - ${this.props.configuration.labelWidthPx}px)`,
            left: `${this.props.configuration.labelWidthPx}px`
          }}
        >
          <ContentRenderer
            {
              ...this.props
            }
            channelId={this.props.channel.id}
            description={undefined}
            descriptionLabelColor={undefined}
            signalDetections={undefined}
            predictedPhases={undefined}
            theoreticalPhaseWindows={undefined}
            markers={undefined}
            events={this.props.events ? this.props.events.events : undefined}
            onContextMenu={this.onWaveformContextMenu}
            onMouseMove={this.onMouseMove}
            onMouseDown={this.onMouseDown}
            onMouseUp={this.onWaveformMouseUp}
            onKeyDown={this.onWaveformKeyDown}
            onMeasureWindowClick={this.onMeasureWindowClick}
            setYAxisBounds={this.setWaveformYAxisBounds}
            toast={this.props.toast}
          >
            <EmptyRenderer
              ref={ref => { if (ref) { this.emptyRendererRef = ref; } }}
              {
                ...this.props
              }
            />
          </ContentRenderer>
        </div>
      </React.Fragment>
    )

  /**
   * Renders the waveform content of the channel
   */
  private readonly renderWaveform = (): React.ReactFragment => {
    const {waveform, channelSegment, numberOfRenderers } = this.getContent();

    return (
      <React.Fragment>
        {waveform ?
          <div
            className="channel-content-container"
            ref={ref => { if (ref) { this.waveformContainerRef = ref; } }}
            style={{
              height: `${this.props.height / numberOfRenderers}px`,
              width: `calc(100% - ${this.props.configuration.labelWidthPx}px)`,
              left: `${this.props.configuration.labelWidthPx}px`
            }}
          >
            <ContentRenderer
              ref={ref => { if (ref) { this.waveformContentRef = ref; } }}
              {
                ...this.props
              }
              channelId={this.props.channel.id}
              description={channelSegment ? channelSegment.description : undefined}
              descriptionLabelColor={channelSegment ? channelSegment.descriptionLabelColor : undefined}
              signalDetections={waveform ? waveform.signalDetections : undefined}
              predictedPhases={waveform ? waveform.predictedPhases : undefined}
              theoreticalPhaseWindows={waveform ? waveform.theoreticalPhaseWindows : undefined}
              markers={waveform ? waveform.markers : undefined}
              events={this.props.events ? this.props.events.events : undefined}
              onContextMenu={this.onWaveformContextMenu}
              onMouseMove={this.onMouseMove}
              onMouseDown={this.onMouseDown}
              onMouseUp={this.onWaveformMouseUp}
              onKeyDown={this.onWaveformKeyDown}
              onMeasureWindowClick={this.onMeasureWindowClick}
              setYAxisBounds={this.setWaveformYAxisBounds}
              toast={this.props.toast}
            >
              <WaveformRenderer
                ref={ref => { if (ref) { this.waveformRendererRef = ref; } }}
                {
                  ...this.props
                }
                channelSegmentId={waveform ? waveform.channelSegmentId : ''}
                channelSegments={waveform ? waveform.channelSegments : new Map()}
                masks={waveform ? waveform.masks : undefined}
                setYAxisBounds={this.setWaveformYAxisBounds}
              />
            </ContentRenderer>
          </div>
          : undefined }
      </React.Fragment>
    );
  }

  /**
   * Renders the spectrogram content of the channel
   */
  private readonly renderSpectrogram = (): React.ReactFragment => {
    const {waveform, channelSegment, spectrogram, numberOfRenderers } = this.getContent();

    return (
      <React.Fragment>
        {spectrogram ?
          <div
            className="channel-content-container"
            ref={ref => { if (ref) { this.spectrogramContainerRef = ref; } }}
            style={{
              height: `${this.props.height / numberOfRenderers}px`,
              width: `calc(100% - ${this.props.configuration.labelWidthPx}px)`,
              left: `${this.props.configuration.labelWidthPx}px`,
              // tslint:disable-next-line:max-line-length
              top: !waveform && !channelSegment ? '0px' : `${(this.props.height / numberOfRenderers) + ((this.props.height / numberOfRenderers) * this.props.index)}px`,
              borderTop: waveform && channelSegment ? `1px solid` : ''
            }}
          >
            <ContentRenderer
              ref={ref => { if (ref) { this.spectrogramContentRef = ref; } }}
              {
              ...this.props
              }
              channelId={this.props.channel.id}
              description={spectrogram ? spectrogram.description : undefined}
              descriptionLabelColor={spectrogram ? spectrogram.descriptionLabelColor : undefined}
              signalDetections={spectrogram ? spectrogram.signalDetections : undefined}
              predictedPhases={spectrogram ? spectrogram.predictedPhases : undefined}
              theoreticalPhaseWindows={spectrogram ? spectrogram.theoreticalPhaseWindows : undefined}
              markers={spectrogram ? spectrogram.markers : undefined}
              events={this.props.events ? this.props.events.events : undefined}
              onContextMenu={this.onSpectrogramContextMenu}
              onMouseMove={this.onMouseMove}
              onMouseDown={this.onMouseDown}
              onMouseUp={this.onSpectrogramMouseUp}
              onKeyDown={this.onSpectrogramKeyDown}
              onMeasureWindowClick={this.onMeasureWindowClick}
              setYAxisBounds={this.setSpectrogramYAxisBounds}
              toast={this.props.toast}
            >
              <SpectrogramRenderer
                ref={ref => { if (ref) { this.spectrogramRendererRef = ref; } }}
                {
                ...this.props
                }
                startTimeSecs={spectrogram.startTimeSecs}
                timeStep={spectrogram.timeStep}
                frequencyStep={spectrogram.frequencyStep}
                data={spectrogram.data}
                setYAxisBounds={this.setSpectrogramYAxisBounds}
                // tslint:disable:no-unbound-method
                colorScale={this.props.configuration.colorScale}
              />
            </ContentRenderer>
          </div>
          : undefined }
      </React.Fragment>
    );
  }

  /**
   * onMeasureWindowClick event handler
   * 
   * @param e The mouse event
   */
  private readonly onMeasureWindowClick = (e: React.MouseEvent<HTMLDivElement>): void => {
    if (e.button === 2 || e.altKey || e.ctrlKey || e.metaKey) return;
    e.stopPropagation();
    const startClientX = e.clientX;
    const start = this.mouseXPosition;
    let isDragging = false;

    const wMeasureWindowSelectionAreaRef =
      this.waveformContentRef && this.waveformContentRef.measureWindowSelectionAreaRef ?
      this.waveformContentRef.measureWindowSelectionAreaRef : undefined;

    const sMeasureWindowSelectionAreaRef =
      this.spectrogramContentRef && this.spectrogramContentRef.measureWindowSelectionAreaRef ?
      this.spectrogramContentRef.measureWindowSelectionAreaRef : undefined;

    const startDivLeft =
      wMeasureWindowSelectionAreaRef && wMeasureWindowSelectionAreaRef.style.left ?
        parseFloat(wMeasureWindowSelectionAreaRef.style.left) :
        sMeasureWindowSelectionAreaRef && sMeasureWindowSelectionAreaRef.style.left ?
        parseFloat(sMeasureWindowSelectionAreaRef.style.left) : 0;

    const startDivRight =
      wMeasureWindowSelectionAreaRef && wMeasureWindowSelectionAreaRef.style.right ?
        parseFloat(wMeasureWindowSelectionAreaRef.style.right) :
        sMeasureWindowSelectionAreaRef && sMeasureWindowSelectionAreaRef.style.right ?
        parseFloat(sMeasureWindowSelectionAreaRef.style.right) : 0;

    const onMouseMove = (event: MouseEvent) => {
      if (!wMeasureWindowSelectionAreaRef && !sMeasureWindowSelectionAreaRef) return;

      const diff = Math.abs(startClientX - event.clientX);
      // begin drag if moving more than 1 pixel
      if (diff > 1 && !isDragging) {
        isDragging = true;
      }
      if (isDragging) {
        // current mouse position in [0,1]
        const canvasRef = this.props.canvasRef();
        const leftOffset = canvasRef ? canvasRef.getBoundingClientRect().left : 0;
        const width = canvasRef ? canvasRef.getBoundingClientRect().width : 0;
        const currentMouseXFrac = (event.clientX - leftOffset) / (width);
        const mouseStartEndDifference = start - currentMouseXFrac;
        const scale = this.props.getViewRange()[1] - this.props.getViewRange()[0];
        const diffPct = PERCENT_100 * mouseStartEndDifference * scale;

        if (wMeasureWindowSelectionAreaRef) {
          wMeasureWindowSelectionAreaRef.style.left = `${startDivLeft - diffPct}%`;
          wMeasureWindowSelectionAreaRef.style.right = `${startDivRight + diffPct}%`;
        }

        if (sMeasureWindowSelectionAreaRef) {
          sMeasureWindowSelectionAreaRef.style.left = `${startDivLeft - diffPct}%`;
          sMeasureWindowSelectionAreaRef.style.right = `${startDivRight + diffPct}%`;
        }
      }
    };

    const onMouseUp = (event: MouseEvent) => {
      event.stopPropagation();
      if (!wMeasureWindowSelectionAreaRef && !sMeasureWindowSelectionAreaRef) return;
      isDragging = false;

      const curDivLeft =
        wMeasureWindowSelectionAreaRef && wMeasureWindowSelectionAreaRef.style.left ?
          parseFloat(wMeasureWindowSelectionAreaRef.style.left) :
          sMeasureWindowSelectionAreaRef && sMeasureWindowSelectionAreaRef.style.left ?
          parseFloat(sMeasureWindowSelectionAreaRef.style.left) : 0;

      const curDivRight =
        wMeasureWindowSelectionAreaRef && wMeasureWindowSelectionAreaRef.style.right ?
          parseFloat(wMeasureWindowSelectionAreaRef.style.right) :
          sMeasureWindowSelectionAreaRef && sMeasureWindowSelectionAreaRef.style.right ?
          parseFloat(sMeasureWindowSelectionAreaRef.style.right) : 0;

      const scale = d3.scaleLinear()
        .range([0, 1])
        .domain([this.props.getViewRange()[0], this.props.getViewRange()[1]]);
      const left = scale(curDivLeft / PERCENT_100);
      const right = scale(1 - curDivRight / PERCENT_100);
      const startTimeSecs = this.props.computeTimeSecsForMouseXPosition(left);
      const endTimeSecs = this.props.computeTimeSecsForMouseXPosition(right);
      if (this.props.updateMeasureWindow) {
        this.props.updateMeasureWindow(
         this.props.stationId, this.props.channel, startTimeSecs,
         endTimeSecs, this.props.isDefaultChannel, this.removeMeasureWindowSelection);
      }
      document.body.removeEventListener('mousemove', onMouseMove);
      document.body.removeEventListener('mouseup', onMouseUp);
    };

    document.body.addEventListener('mousemove', onMouseMove);
    document.body.addEventListener('mouseup', onMouseUp);
  }

  /**
   * onWaveformContextMenu event handler
   * 
   * @param e mouse event as React.MouseEvent<HTMLDivElement>
   */
  private readonly onWaveformContextMenu = (e: React.MouseEvent<HTMLDivElement>) => {
    e.preventDefault();

    if (this.waveformContentRef && this.waveformRendererRef && this.props.channel.waveform) {
      const masks = this.determineIfMaskIsClicked();
      if (masks.length > 0) {
        if (this.props.events && this.props.events.events &&
          this.props.events.events.onMaskContextClick) {
            this.props.events.events.onMaskContextClick(e, this.props.channel.id, masks);
        }
      } else {
        if (this.props.onContextMenu) {
          this.props.onContextMenu(e, this.props.channel.id, undefined);
        }
      }
    }
  }

  /**
   * onSpectrogramContextMenu event handler
   * 
   * @param e mouse event as React.MouseEvent<HTMLDivElement>
   */
  private readonly onSpectrogramContextMenu = (e: React.MouseEvent<HTMLDivElement>) => {
    e.preventDefault();

    if (this.spectrogramContentRef && this.spectrogramRendererRef && this.props.channel.spectrogram) {
      if (this.props.onContextMenu) {
        this.props.onContextMenu(e, this.props.channel.id, undefined);
      }
    }
  }

  /**
   * onMouseMove event handler
   * 
   * @param e The mouse event
   */
  private readonly onMouseMove = (e: React.MouseEvent<HTMLDivElement>) => {
    const canvasRef = this.props.canvasRef();
    const leftOffset = canvasRef ? canvasRef.getBoundingClientRect().left : 0;
    const width = canvasRef ? canvasRef.getBoundingClientRect().width : 0;
    this.mouseXPosition = (e.clientX - leftOffset) / width;
    this.mousePosition = ({
      clientX: e.clientX,
      clientY: e.clientY
    });
    this.props.onMouseMove(e, this.mouseXPosition, this.getTimeSecs());
  }

  /**
   * onWaveformMouseUp event handler
   * 
   * @param e mouse event as React.MouseEvent<HTMLDivElement>
   */
  private readonly onWaveformMouseUp = (e: React.MouseEvent<HTMLDivElement>) => {
    const timeForMouseXPosition = this.getTimeSecs();
    this.props.onMouseUp(
      e, this.mouseXPosition, this.props.channel.id, timeForMouseXPosition, this.props.isDefaultChannel);
    if (this.props.channel.waveform && this.props.channel.waveform.masks &&
      this.props.events && this.props.events.events &&
      this.props.events.events.onMaskClick && !e.metaKey && !e.ctrlKey) {

      const masks = this.determineIfMaskIsClicked();

      if (masks.length > 0) {
       this.props.events.events.onMaskClick(e, this.props.channel.id, masks, this.maskCreateHotKeyPressed);
      }
    }
  }

  /**
   * Determines if a mask has been clicked. If a mask is shorter than a second
   * A buffer of 0.5secs to the start and end time is added so that it can be seen
   * visually and a users can click it.
   */
  private readonly determineIfMaskIsClicked = (): string[] => {
    if (!this.props.channel.waveform || !this.props.channel.waveform.masks) {
      return [];
    }

    // determine if any masks were click
    const timeForMouseXPosition = this.getTimeSecs();
    const halfSecond = 0.5;
    const masks: string[] = lodash.sortBy(
     this.props.channel.waveform.masks,
     (m: Entities.Mask) => m.endTimeSecs - m.startTimeSecs)
     // A mask with less than one second, isn't clickable, thus adding a second to make sure it is clickable
     .filter(m => m.endTimeSecs - m.startTimeSecs < 1 ?
        // tslint:disable-next-line: max-line-length
        (m.startTimeSecs - halfSecond <= timeForMouseXPosition && timeForMouseXPosition <= m.endTimeSecs + halfSecond) :
        (m.startTimeSecs <= timeForMouseXPosition && timeForMouseXPosition <= m.endTimeSecs))
     .map(m => m.id);

    return masks;
  }

  /**
   * onSpectrogramMouseUp event handler
   * 
   * @param e mouse event as React.MouseEvent<HTMLDivElement>
   */
  private readonly onSpectrogramMouseUp = (e: React.MouseEvent<HTMLDivElement>) => {
    this.props.onMouseUp(
      e, this.mouseXPosition, this.props.channel.id, this.getTimeSecs(), this.props.isDefaultChannel);
  }

  /**
   * onMouseDown event handler, may have to move the measureWindow logic to keydown
   * to distingush between command click and regular click
   * 
   * @param e The mouse event
   */
  private readonly onMouseDown = (e: React.MouseEvent<HTMLDivElement>): void => {
    // Prevent propagation of these events so that the underlying channel click doesn't register
    e.stopPropagation();

    if (e.button === 2) return;
    const timeSecs = this.getTimeSecs();

    if (this.waveformRendererRef && this.isAdjustingAmplitude) {
      this.waveformRendererRef.beginScaleAmplitudeDrag(e);

    } else if (e.altKey) {
      this.props.onMouseDown(e, this.mouseXPosition, this.props.channel.id, timeSecs, this.props.isDefaultChannel);

      const isMeasureWindowDisabled = (this.props.isDefaultChannel) ?
        this.props.configuration.defaultChannel.disableMeasureWindow :
        this.props.configuration.nonDefaultChannel.disableMeasureWindow;
      if (isMeasureWindowDisabled) {
        this.props.toast(Messages.measureWindowDisabled);
      } else {
        if (this.props.updateMeasureWindow) {
          const start = this.mouseXPosition;
          const startClientX = e.clientX;
          let isDragging = false;
          const scale = d3.scaleLinear()
            .domain([0, 1])
            .range(
              [this.props.getViewRange()[0], this.props.getViewRange()[1]]);

          const wMeasureWindowSelectionAreaRef =
            this.waveformContentRef && this.waveformContentRef.measureWindowSelectionAreaRef ?
            this.waveformContentRef.measureWindowSelectionAreaRef : undefined;

          const sMeasureWindowSelectionAreaRef =
            this.spectrogramContentRef && this.spectrogramContentRef.measureWindowSelectionAreaRef ?
            this.spectrogramContentRef.measureWindowSelectionAreaRef : undefined;

          const onMouseMove = (event: MouseEvent) => {
            if (!wMeasureWindowSelectionAreaRef && !sMeasureWindowSelectionAreaRef) return;
            const diff = Math.abs(startClientX - event.clientX);
            // begin drag if moving more than 1 pixel
            if (diff > 1 && !isDragging) {
              isDragging = true;
              if (wMeasureWindowSelectionAreaRef) {
                wMeasureWindowSelectionAreaRef.style.display = 'initial';
              }
              if (sMeasureWindowSelectionAreaRef) {
                sMeasureWindowSelectionAreaRef.style.display = 'initial';
              }
            }
            if (isDragging) {
              const left = scale(Math.min(start, this.mouseXPosition));
              const right = scale(Math.max(start, this.mouseXPosition));
              if (wMeasureWindowSelectionAreaRef) {
                wMeasureWindowSelectionAreaRef.style.left = `${left * PERCENT_100}%`;
                wMeasureWindowSelectionAreaRef.style.right = `${(1 - right) * PERCENT_100}%`;
              }
              if (sMeasureWindowSelectionAreaRef) {
                sMeasureWindowSelectionAreaRef.style.left = `${left * PERCENT_100}%`;
                sMeasureWindowSelectionAreaRef.style.right = `${(1 - right) * PERCENT_100}%`;
              }
            }
          };

          const onMouseUp = (event: MouseEvent) => {
            if ((!this.waveformContentRef || !this.waveformContentRef.measureWindowSelectionAreaRef) &&
              (!this.spectrogramContentRef || !this.spectrogramContentRef.measureWindowSelectionAreaRef)) {
              return;
            }

            isDragging = false;
            const startTimeSecs = this.props.computeTimeSecsForMouseXPosition(Math.min(start, this.mouseXPosition));
            const endTimeSecs = this.props.computeTimeSecsForMouseXPosition(Math.max(start, this.mouseXPosition));
            if (this.props.updateMeasureWindow) {
              this.props.updateMeasureWindow(
               this.props.stationId, this.props.channel,
               startTimeSecs, endTimeSecs, this.props.isDefaultChannel, this.removeMeasureWindowSelection);
            }
            document.body.removeEventListener('mousemove', onMouseMove);
            document.body.removeEventListener('mouseup', onMouseUp);
          };

          document.body.addEventListener('mousemove', onMouseMove);
          document.body.addEventListener('mouseup', onMouseUp);
        }
      }
    } else {
      this.props.onMouseDown(e, this.mouseXPosition, this.props.channel.id, timeSecs, this.props.isDefaultChannel);
    }
  }

  /**
   * onWaveformKeyDown event handler
   * 
   * @param e mouse event as React.KeyboardEvent<HTMLDivElement>
   */
  private readonly onWaveformKeyDown = (e: React.KeyboardEvent<HTMLDivElement>): void => {
    if (!e.repeat) {
      if (this.props.configuration.hotKeys.amplitudeScaleSingleReset) {
        if (isHotKeyCommandSatisfied(e.nativeEvent, this.props.configuration.hotKeys.amplitudeScaleSingleReset)) {
          this.resetAmplitude();
        }
      }

      if (this.props.configuration.hotKeys.amplitudeScale) {
        if (isHotKeyCommandSatisfied(e.nativeEvent, this.props.configuration.hotKeys.amplitudeScale)) {
          const onKeyUp = (e2: KeyboardEvent) => {
            if (this.props.configuration.hotKeys.amplitudeScale) {
              if (isHotKeyCommandSatisfied(e2, this.props.configuration.hotKeys.amplitudeScale)) {
                this.isAdjustingAmplitude = false;
                document.removeEventListener('keyup', onKeyUp);
              }
            }
          };
          this.isAdjustingAmplitude = true;
          document.addEventListener('keyup', onKeyUp, true);
        }
      }
    }

    if (this.props.configuration.hotKeys.maskCreate) {
      if (isHotKeyCommandSatisfied(e.nativeEvent, this.props.configuration.hotKeys.maskCreate)) {
        this.maskCreateHotKeyPressed = true;
        const onKeyUpMask = (e2: KeyboardEvent) => {
          if (this.props.configuration.hotKeys.maskCreate) {
            if (e2.code === this.props.configuration.hotKeys.maskCreate) {
              this.maskCreateHotKeyPressed = false;
            }
          } else {
            this.maskCreateHotKeyPressed = false;
          }
        };
        document.addEventListener('keyup', onKeyUpMask, true);
      }
    }
  }

  /**
   * onSpectrogramKeyDown event handler
   * 
   * @param e mouse event as React.KeyboardEvent<HTMLDivElement>
   */
  private readonly onSpectrogramKeyDown = (e: React.KeyboardEvent<HTMLDivElement>): void => {
      // no-op
  }

  /**
   * onKeyPress event handler
   */
  private readonly onKeyDown = (e: React.KeyboardEvent<HTMLDivElement>) => {
    if (!e.repeat) {
      if (this.waveformContentRef) { // ! TODO handle waveform and spectrogram
        if (this.props.events) {
          if (this.props.events.onKeyPress) {
            const mousePosition = this.getMousePosition();
            const timeSecs = this.getTimeSecs();
            this.props.events.onKeyPress(
              e, mousePosition.clientX, mousePosition.clientY, this.props.channel.id, timeSecs);
          }
        }
      }
    }
  }

  /**
   * Set the waveform y-axis bounds for the channel.
   */
  private readonly setWaveformYAxisBounds = (min: number, max: number) => {
    if (this.state.waveformYAxisBounds) {
      if (this.state.waveformYAxisBounds.minAmplitude !== min &&
          this.state.waveformYAxisBounds.maxAmplitude !== max) {
        this.setState({
          waveformYAxisBounds: {
            ...this.state.waveformYAxisBounds,
            minAmplitude: min,
            maxAmplitude: max
          }
        });
      }
    }
  }

  /**
   * Set the spectrogram y-axis bounds for the channel.
   */
  private readonly setSpectrogramYAxisBounds = (min: number, max: number) => {
    if (this.state.spectrogramYAxisBounds) {
      if (this.state.spectrogramYAxisBounds.minAmplitude !== min &&
          this.state.spectrogramYAxisBounds.maxAmplitude !== max) {
        this.setState({
          spectrogramYAxisBounds: {
            ...this.state.spectrogramYAxisBounds,
            minAmplitude: min,
            maxAmplitude: max
          }
        });
      }
    }
  }

  private readonly internalRenderScene = (
    renderer: THREE.WebGLRenderer,
    boundsRect: ClientRect | DOMRect,
    scene: THREE.Scene,
    camera: THREE.OrthographicCamera,
    container: HTMLElement
  ) => {
    if (
      !renderer ||
      !boundsRect ||
      !scene ||
      !camera ||
      !container) return;

    // get its position relative to the page's viewport
    const rect = container.getBoundingClientRect();

    // check if it's out of bounds. If so skip it
    if (rect.bottom < boundsRect.top || rect.top > boundsRect.bottom) {
      return;  // it's out of bounds
    }

    // set the viewport
    const width = boundsRect.width;
    const height = rect.height;
    const x = rect.left - boundsRect.left;
    const y = rect.top - boundsRect.top;

    renderer.setViewport(0, y, width, height);

    // adjust the camera view and offset
    camera.setViewOffset(
      container.clientWidth,
      container.clientHeight,
      Math.abs(x), 0, boundsRect.width, container.clientHeight);

    renderer.setScissor(x, y, container.clientWidth, height);
    renderer.render(scene, camera);

  }
}
