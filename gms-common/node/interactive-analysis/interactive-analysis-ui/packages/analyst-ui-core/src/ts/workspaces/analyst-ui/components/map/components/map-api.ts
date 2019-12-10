/**
 * Interfaces for GMS Map
 * Maintained so that we can switch map libraries if the need arises
 * Currently cesium-map.ts implements this interface
 */

import { AnalystUiConfig } from '~analyst-ui/config';
import { EventTypes, SignalDetectionTypes, StationTypes } from '~graphql/';
import { TimeInterval } from '~state/analyst-workspace/types';
import { MapProps } from '../types';

/**
 * State for the map API
 */
export interface MapAPIState {
  layers: {
    Events: any;
    Stations: any;
    Assoc: any;
    OtherAssoc: any;
    UnAssociated: any;
  };
}
/**
 * Parameters for configuring the map API
 */
export interface MapAPIOptions {
  analystUiConfig: AnalystUiConfig;
  events: {
    onMapClick(e: any, entity?: any): void;
    onMapRightClick(e: any, entity?: any): void;
    onMapShiftClick(e: any, entity?: any): void;
    onMapDoubleClick(e: any, entity?: any): void;
    onMapAltClick(e: any, entity?: any): void;
  };
}

/**
 * Interface/API for GMS Map. Currently, at a minimum a map must implement the methods below
 */
export interface MapAPI {
  getViewer();
  getDataLayers();
  initialize(containerElement: any);
  drawSignalDetections(signalDetections: SignalDetectionTypes.SignalDetection[],
    events: EventTypes.Event[], nextOpenEvent: EventTypes.Event);
  drawOtherAssociatedSignalDetections(signalDetections: SignalDetectionTypes.SignalDetection[],
    events: EventTypes.Event[], currentOpenEvent: EventTypes.Event);
  drawUnAssociatedSignalDetections(
    signalDetections: SignalDetectionTypes.SignalDetection[],
    events: EventTypes.Event[], currentOpenEvent: EventTypes.Event);
  highlightSelectedSignalDetections(selectedSignalDetection: string[]);
  drawDefaultStations(currentStations: StationTypes.ProcessingStation[],
    nextStations: StationTypes.ProcessingStation[]);
  updateStations(currentSignalDetections: SignalDetectionTypes.SignalDetection[], currentOpenEvent: EventTypes.Event,
    nextSignalDetections: SignalDetectionTypes.SignalDetection[], nextOpenEvent: EventTypes.Event);
  drawEvents(currenProps: MapProps, nextProps: MapProps);
  highlightOpenEvent(currentTimeInterval: TimeInterval, currentOpenEvent: EventTypes.Event,
    nextOpenEvent: EventTypes.Event, selectedEventIds: string[]);
}
