import { EventTypes, SignalDetectionTypes, StationTypes } from '~graphql/';
import { TimeInterval } from '~state/analyst-workspace/types';
import { UILogger } from '~util/log/logger';
import { MapProps } from '../types';
import { MapAPI, MapAPIOptions, MapAPIState } from './map-api';
import * as EventRenderer from './map-event-renderer';
import * as OtherAssociatedRenderer from './map-other-association-sd-renderer';
import * as SignalDetectionRenderer from './map-sd-renderer';
import * as UnAssociatedRenderer from './map-sd-unassociated-renderer';
import * as StationsRenderer from './map-stations-renderer';

// API for wrapping all Map calls which would allow for easier replacement of map tech stack

declare var Cesium;

// Cesium.Ion.defaultAccessToken = null;
// TODO find a real map server on premise or something
Cesium.BingMapsApi.defaultKey = 'AsdrA8Fa6E_pOjFiQLbMDqdYpvKBlhZMvHvpJCzRJb6iIJDYvp2xvcNoXjT7zDD_';

// Set Default Camera View
Cesium.Camera.DEFAULT_VIEW_RECTANGLE = new Cesium.Rectangle(-Math.PI, -Math.PI / 2, Math.PI, Math.PI / 2);

/**  Mapping from cesium's left-click to our space */
const LEFT_CLICK = Cesium.ScreenSpaceEventType.LEFT_CLICK;

/**  Mapping from cesium's left-click to our space */
const RIGHT_CLICK = Cesium.ScreenSpaceEventType.RIGHT_CLICK;

/**  Mapping from cesium's CRTL KEY to our space */
const CTRL_KEY = Cesium.KeyboardEventModifier.CTRL;

/**  Mapping from cesium's ALT KEY to our space */
const ALT_KEY = Cesium.KeyboardEventModifier.ALT;

/**  Mapping from cesium's double-click to our space */
const LEFT_DOUBLE_CLICK = Cesium.ScreenSpaceEventType.LEFT_DOUBLE_CLICK;

/**
 * Implmentation of MapAPI Interface
 */
export class CesiumMap implements MapAPI {

  private readonly state: MapAPIState;

  private readonly options: MapAPIOptions;

  /**
   * handle to the Map.Viewer object.
   */
  private viewer: any;

  public constructor(options: MapAPIOptions) {
    this.state = {
      layers: {
        Events: this.createDataSource('Events'),
        Stations: this.createDataSource('Stations'),
        Assoc: this.createDataSource('Open Assoc'),
        OtherAssoc: this.createDataSource('Other Assoc'),
        UnAssociated: this.createDataSource('Unassociated')
      }
    };
    this.options = options;
  }

  /**
   * Initialize Map viewer
   * 
   * @param containerDomElement Element which contains the map
   */
  public initialize = (containerDomElement: HTMLDivElement) => {
    this.viewer = this.createMapViewer(containerDomElement);
    this.viewer.screenSpaceEventHandler.setInputAction(this.onMapClick, LEFT_CLICK);
    this.viewer.screenSpaceEventHandler.setInputAction(this.onMapRightClick, RIGHT_CLICK);
    this.viewer.screenSpaceEventHandler.setInputAction(this.onMapCtrlClick, LEFT_CLICK, CTRL_KEY);
    this.viewer.screenSpaceEventHandler.setInputAction(this.onMapAltClick, LEFT_CLICK, ALT_KEY);
    this.viewer.screenSpaceEventHandler.setInputAction(this.onMapDoubleClick, LEFT_DOUBLE_CLICK);
    this.viewer.camera.flyHome(0);
    this.setupLayers(this.viewer);
    this.getDataLayers().OtherAssoc.show = false;
    this.getDataLayers().UnAssociated.show = false;
  }

  /**
   * Public accessor to the cesium viewer
   * 
   * @returns the map viewer
   */
  public getViewer = () => this.viewer;

  /**
   * Public accessor for the data layers/datasources
   * 
   * @returns MapAPIState.layers {Events: any, stations: any, SDs: any}
   */
  public getDataLayers = () => this.state.layers;

  /**
   * Draws unassociated signal detections on the map
   * @param signalDetections List of signal detections
   * @param currentOpenEvent currently open event
   */
  public drawOtherAssociatedSignalDetections = (
    signalDetections: SignalDetectionTypes.SignalDetection[],
    events: EventTypes.Event[], currentOpenEvent: EventTypes.Event
  ) => {
    OtherAssociatedRenderer.draw(this.state.layers.OtherAssoc, signalDetections, events, currentOpenEvent);
  }

  /**
   * Draws unassociated signal detections on the map
   * @param signalDetections List of signal detections
   * @param currentOpenEvent currently open event
   */
  public drawUnAssociatedSignalDetections = (
    signalDetections: SignalDetectionTypes.SignalDetection[],
    events: EventTypes.Event[],
    currentlyOpenEvent: EventTypes.Event
  ) => {
    UnAssociatedRenderer.draw(this.state.layers.UnAssociated, signalDetections, events, currentlyOpenEvent);
  }

  /**
   * Draws signal detections on the map
   * @param signalDetections List of signal detections to draw
   * @param nextOpenEventId ID for the next open event Hypothesis
   */
  public drawSignalDetections = (
    signalDetections: SignalDetectionTypes.SignalDetection[],
    events: EventTypes.Event[],
    nextOpenEvent: EventTypes.Event) => {
    SignalDetectionRenderer.draw(this.state.layers.Assoc, signalDetections, nextOpenEvent);
  }

  /**
   * Highlights selected signal detections
   * 
   * @param selectedSignalDetections List of selected detections
   */
  public highlightSelectedSignalDetections(selectedSignalDetections: string[]) {
    SignalDetectionRenderer.highlightSelectedSignalDetections(
      this.state.layers.Assoc,
      selectedSignalDetections);
    OtherAssociatedRenderer.highlightSelectedSignalDetections(
      this.state.layers.OtherAssoc,
      selectedSignalDetections
    );
    UnAssociatedRenderer.highlightSelectedSignalDetections(this.state.layers.UnAssociated, selectedSignalDetections);
  }

  /**
   * Draw default stations on the the map
   * 
   * @param currentDefaultStations list of stations previously drawn
   * @param nextdefaultStations list of next stations
   */
  public drawDefaultStations = (currentDefaultStations: StationTypes.ProcessingStation[],
    nextdefaultStations: StationTypes.ProcessingStation[]) => {
    StationsRenderer.draw(this.state.layers.Stations, currentDefaultStations, nextdefaultStations);
  }

  /**
   * Updates stations when data or selections change
   * 
   * @param currentSignalDetections list of current signal detections
   * @param currentOpenEventId current open event
   * @param nextSignalDetections next detections to be drawn
   * @param nextOpenEventId next open event
   */
  public updateStations = (currentSignalDetections: SignalDetectionTypes.SignalDetection[],
    currentOpenEvent: EventTypes.Event,
    nextSignalDetections: SignalDetectionTypes.SignalDetection[],
    nextOpenEvent: EventTypes.Event) => {
    StationsRenderer.update(
      this.state.layers.Stations,
      currentSignalDetections,
      currentOpenEvent,
      nextSignalDetections,
      nextOpenEvent);
  }

  /**
   * Draws events on the map
   * @param currentProps currentprops - passed in entire props to use data
   * @param nextProps nextprops - passed in entire props to use data
   */
  public drawEvents(currentProps: MapProps, nextProps: MapProps) {
    EventRenderer.draw(this.state.layers.Events, currentProps, nextProps);
  }

  /**
   * Highlights the current open event
   * @param currentTimeInterval current open time interval
   * @param currentOpenEvent current open event
   * @param nextOpenEvent next open event
   * @param selectedEventIds currently selected event IDs
   */
  public highlightOpenEvent = (currentTimeInterval: TimeInterval,
    currentOpenEvent: EventTypes.Event,
    nextOpenEvent: EventTypes.Event,
    selectedEventIds: string[]) => {
    EventRenderer.highlightOpenEvent(
      this.state.layers.Events,
      currentTimeInterval,
      currentOpenEvent,
      nextOpenEvent,
      selectedEventIds);

    // fly to the event, maintaining current camera height
    if (nextOpenEvent) {
      const loc = nextOpenEvent.currentEventHypothesis.eventHypothesis.
        preferredLocationSolution.locationSolution;
      const currentCameraHeight = this.viewer.camera.positionCartographic.height;
      this.viewer.camera.flyTo({
        destination: Cesium.Cartesian3.fromDegrees(
          loc.location.longitudeDegrees, loc.location.latitudeDegrees, currentCameraHeight),
        duration: 2
      });
    }
  }

  /**
   * Creates a Map Datasource with the passed in name
   * @param name of the datasource being started
   */
  private readonly createDataSource = (name: string) => new Cesium.CustomDataSource(name);

  /**
   * Creates a Map View
   * 
   * @param containerElement element in which the map will be displayed
   * 
   * @returns Cesium.Viewer
   */
  private readonly createMapViewer = (containerElement: any) => {

    const baseViewerSettings = {
      sceneMode: this.options.analystUiConfig.userPreferences.map.defaultTo3D ?
        Cesium.SceneMode.SCENE3D : Cesium.SceneMode.SCENE2D,
      animation: false,
      baseLayerPicker: true,
      fullscreenButton: false,
      fullscreenElement: false,
      geocoder: false,
      homeButton: true,
      infoBox: true,
      sceneModePicker: true,
      selectionIndicator: true,
      targetFrameRate: 60,
      timeline: false,
      navigationHelpButton: true,
      requestRenderMode: true,
      maximumRenderTimeChange : Infinity
    };

    const imageryProvider = Cesium.createTileMapServiceImageryProvider({
      url: Cesium.buildModuleUrl(this.options.analystUiConfig.environment.map.offlineImagery.url),
      maximumLevel: this.options.analystUiConfig.environment.map.offlineImagery.maxResolutionLevel
    });

    const imageryProviderViewModels = [new Cesium.ProviderViewModel({
      name: 'Default',
      category: 'Default',
      tooltip: 'The default layout',
      iconUrl: Cesium.buildModuleUrl('Widgets/Images/ImageryProviders/naturalEarthII.png'),
      creationFunction: () =>
        Cesium.createTileMapServiceImageryProvider({
          url: Cesium.buildModuleUrl('Assets/Textures/NaturalEarthII'),
          maximumLevel: 2
        })
    })];

    const viewerSettings = this.options.analystUiConfig.environment.map.online ?
      baseViewerSettings :
      {
        ...baseViewerSettings,
        imageryProvider,
        imageryProviderViewModels,
        baseLayerPicker: false,
        geocoder: false
      };

    const viewer = new Cesium.Viewer(containerElement, viewerSettings);
    viewer.scene.sunBloom = false;
    viewer.scene.skyAtmosphere.show = false;
    viewer.scene.fog.enabled = false;
    viewer.scene.shadowMap.enabled = false;
    viewer.scene.fxaa = false;
    viewer.shadows = false;
    viewer.terrainShadows = false;
    viewer.scene.screenSpaceCameraController.enableTilt = false;
    viewer.scene.screenSpaceCameraController.enableLook = false;
    viewer.scene.screenSpaceCameraController.enableCollisionDetection = false;

    this.setupFrameRateMonitor(viewer);

    return viewer;
  }

  /**
   * Add datasources to the viewer held by the map/index
   * @param viewer viewer created by CreateMapViewer that is owned by map/index
   */
  private readonly setupLayers = (viewer: any) => {
    viewer.dataSources.add(this.state.layers.Events);
    viewer.dataSources.add(this.state.layers.Stations);
    viewer.dataSources.add(this.state.layers.Assoc);
    viewer.dataSources.add(this.state.layers.OtherAssoc);
    viewer.dataSources.add(this.state.layers.UnAssociated);
  }

  /**
   * Sets up a monitor for the cesium frame rate.
   * 
   * @param viewer the cesium viewer
   */
  private readonly setupFrameRateMonitor = (viewer: any) => {
    const frameRateMonitor = Cesium.FrameRateMonitor.fromScene(viewer.scene);
    // tslint:disable-next-line: number-literal-format
    const oneHundredPercentResolutionScale = 1.0;
    const seventyPercentResolutionScale = 0.7;
    const tenPercentResolutionScaleReduction = 0.9;
    frameRateMonitor.lowFrameRate.addEventListener(() => {
      UILogger.info('Low FPS - Lowering Resolution Scale');
      const scale = viewer.resolutionScale;
      if (scale <= seventyPercentResolutionScale) {
        return;
      }
      viewer.resolutionScale = scale * tenPercentResolutionScaleReduction;
    });

    frameRateMonitor.nominalFrameRate.addEventListener(() => {
      if (viewer.resolutionScale < oneHundredPercentResolutionScale) {
        UILogger.info('Nominal FPS - Restoring Resolution');
        viewer.resolutionScale = oneHundredPercentResolutionScale;
      }
    });
    return frameRateMonitor;
  }

  /**
   * Handles map single click
   */
  private readonly onMapClick = (e: any) => {
    const entityWrapper = this.viewer.scene.pick(e.position);
    if (Cesium.defined(entityWrapper)) {
      this.options.events.onMapClick(e, entityWrapper.id);
    } else {
      this.options.events.onMapClick(e, undefined);
    }
  }

  /**
   * Handles map Context click
   */
  private readonly onMapRightClick = (e: any) => {
    const entityWrapper = this.viewer.scene.pick(e.position);
    if (Cesium.defined(entityWrapper)) {
      this.options.events.onMapRightClick(e, entityWrapper.id);
    } else {
      this.options.events.onMapRightClick(e, undefined);
    }
  }

  /**
   * Handles map ctrl+click
   */
  private readonly onMapCtrlClick = (e: any) => {
    const entityWrapper = this.viewer.scene.pick(e.position);
    if (Cesium.defined(entityWrapper)) {
      this.options.events.onMapShiftClick(e, entityWrapper.id);
    } else {
      this.options.events.onMapShiftClick(e, undefined);
    }
  }

  /**
   * Handles map alt+click
   */
  private readonly onMapAltClick = (e: any) => {
    const entityWrapper = this.viewer.scene.pick(e.position);
    if (Cesium.defined(entityWrapper)) {
      this.options.events.onMapAltClick(e, entityWrapper.id);
    } else {
      this.options.events.onMapAltClick(e, undefined);
    }
  }

  /**
   * Handles map double click
   */
  private readonly onMapDoubleClick = (e: any) => {
    const entityWrapper = this.viewer.scene.pick(e.position);
    if (Cesium.defined(entityWrapper)) {
      this.options.events.onMapDoubleClick(e, entityWrapper.id);
    } else {
      this.options.events.onMapDoubleClick(e, undefined);
    }
  }
}
