/**
 * Renders stations on the map
 */
import { userPreferences } from '~analyst-ui/config';
import { EventTypes, SignalDetectionTypes, StationTypes } from '~graphql/';
// tslint:disable-next-line:no-implicit-dependencies no-var-requires no-require-imports
const stationPng = require('../img/' + userPreferences.map.icons.station);
/**  Proportion to scale images by */
const imageScale = userPreferences.map.icons.stationScale;

declare var Cesium;

/**  Color to be used for selected stations */
const selectedStationColor = Cesium.Color.fromCssColorString(userPreferences.map.colors.openEvent);
/**  Color to be used for stations */
const stationColor = Cesium.Color.fromCssColorString(userPreferences.map.colors.unselectedStation);

/**
 * Draw stations on to the map
 * 
 * @param dataSource Source of map data
 * @param currentDefaultStations list of current default stations
 * @param nextdefaultStations list of incoming default stations from prop change
 */
export function draw(dataSource: any,
  currentDefaultStations: StationTypes.ProcessingStation[],
  nextdefaultStations: StationTypes.ProcessingStation[]) {
  addDefaultStations(dataSource, nextdefaultStations);

  // TODO handle modified & removed stations
}

/**
 * create new map entities for a list of events
 * 
 * @param dataSource data source
 * @param defaultStations default stations
 */
function addDefaultStations(dataSource: any, defaultStations: StationTypes.ProcessingStation[]) {
  const pixelOffset = 15;
  const sitePixelOffset = -15;
  const defaultZoomDistance = 1e7;
  const closeLabelZoomDistance = 1.5e3;
  const closeBillboardZoomDistance = 1e4;

  if (!defaultStations) {
    return;
  }

  defaultStations.forEach(defaultStation => {
    const siteLon = defaultStation.location.lonDegrees;
    const siteLat = defaultStation.location.latDegrees;
    const siteElev = defaultStation.location.elevationKm;

    dataSource.entities.add({
      name: 'station: ' + defaultStation.name,
      position: Cesium.Cartesian3.fromDegrees(siteLon, siteLat),
      id: defaultStation.id,
      billboard: {
        image: stationPng,
        scale: imageScale,
        color: stationColor
      },
      label: {
        text: defaultStation.name,
        font: '14px sans-serif',
        outlineColor: Cesium.Color.BLACK,
        pixelOffset: new Cesium.Cartesian2(0, pixelOffset),
        distanceDisplayCondition: new Cesium.DistanceDisplayCondition(0, defaultZoomDistance),
      },
      description: `<ul>
                            <li>Name: ${defaultStation.name}</li>
                            <li>Networks: ${defaultStation.networks.map(network => network.name)
          .join(' ')}</li>
                            <li>Latitude: ${siteLat.toFixed(3)}</li>
                            <li>Lognitude: ${siteLon.toFixed(3)}</li>
                            <li>Elevation: ${siteElev.toFixed(3)}</li></ul>`,
    });

    const stationSites = defaultStation.sites;
    const siteStationID = defaultStation.id;

    stationSites.forEach(stationSite => {
      const stationSiteID: string = stationSite.id;
      const stationSiteLon = stationSite.location.lonDegrees;
      const stationSiteLat = stationSite.location.latDegrees;
      const stationSiteElev = stationSite.location.elevationKm;
      const stationSiteName = defaultStation.name;

      const siteEntity = new Cesium.Entity({
        name: 'site: ' + stationSiteID,
        position: Cesium.Cartesian3.fromDegrees(stationSiteLon, stationSiteLat),
        id: stationSiteID,
        billboard: {
          image: stationPng,
          scale: imageScale,
          distanceDisplayCondition: new Cesium.DistanceDisplayCondition(0, closeBillboardZoomDistance),
          color: stationColor
        },
        label: {
          text: stationSite.name,
          font: '12px sans-serif',
          outlineColor: Cesium.Color.BLACK,
          pixelOffset: new Cesium.Cartesian2(0, sitePixelOffset),
          distanceDisplayCondition: new Cesium.DistanceDisplayCondition(0, closeLabelZoomDistance),
        },
        description: `<ul>
                                    <li>Name: ${stationSiteID}</li>
                                    <li>Station ID: ${siteStationID}</li>
                                    <li>Station: ${stationSiteName}</li>
                                    <li>Latitude: ${stationSiteLat.toFixed(3)}</li>
                                    <li>Lognitude: ${stationSiteLon.toFixed(3)}</li>
                                    <li>Elevation: ${stationSiteElev.toFixed(3)}</li></ul>`,
      });

      // Apparently, there are duplicate site id's in the data set so
      // we don't want to add it if it exists
      // Cesium will complain LOUDLY
      // May need to handle in future?
      if (!dataSource.entities.getById(stationSiteID)) {
        dataSource.entities.add(siteEntity);
      }
    });

  });
}

/**
 * Updates stations on the map
 * 
 * @param dataSource source of data for the map
 * @param currentSignalDetections list of map signal detections
 * @param currentOpenEventId currently open event id
 * @param nextSignalDetections list of incoming map signal detections from prop change
 * @param nextOpenEventId incoming open event if
 */
export function update(dataSource: any,
  currentSignalDetections: SignalDetectionTypes.SignalDetection[],
  currentOpenEvent: EventTypes.Event,
  nextSignalDetections: SignalDetectionTypes.SignalDetection[],
  nextOpenEvent: EventTypes.Event) {

  // Reset stations from last open event
  if (currentOpenEvent && currentSignalDetections) {
    const eventSDAssoc = currentOpenEvent.currentEventHypothesis.eventHypothesis.signalDetectionAssociations;
    currentSignalDetections.filter(sd =>
        eventSDAssoc.find(evtSDAccoc =>
          evtSDAccoc.signalDetectionHypothesis.id === sd.currentHypothesis.id))
      .forEach(signalDetection => {
        const eventEntity = dataSource.entities.getById(signalDetection.station.id);
        eventEntity.billboard.color = stationColor;
        eventEntity.billboard.scale = imageScale;
      });
  }

  // Update new stations for next event
  if (nextSignalDetections && nextOpenEvent) {
    const eventSDAssoc = nextOpenEvent.currentEventHypothesis.eventHypothesis.signalDetectionAssociations;
    nextSignalDetections.filter(sd =>
        eventSDAssoc.find(evtSDAccoc =>
          evtSDAccoc.signalDetectionHypothesis.id === sd.currentHypothesis.id))
      .forEach(signalDetection => {
        const eventEntity = dataSource.entities.getById(signalDetection.station.id);
        eventEntity.billboard.color = selectedStationColor;
        eventEntity.billboard.scale = imageScale;
      });
  }
}
/**
 * Resets the station colors on the map
 * 
 * @param defaultStations list of default stations
 * @param dataSource the source of map data
 */
export function resetView(defaultStations: any[], dataSource: any) {
  defaultStations.forEach(station => {
    dataSource.entities.getById(station.id).billboard.color = stationColor;
  });
}
