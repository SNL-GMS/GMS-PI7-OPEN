import { Classes, NumericInput, Position } from '@blueprintjs/core';
import { IconNames } from '@blueprintjs/icons';
import { PopoverButton, Row, Table } from '@gms/ui-core-components';
import * as classNames from 'classnames';
import { flatMap } from 'lodash';
import * as React from 'react';
import { getFkData, getFkParamsForSd } from '~analyst-ui/common/utils/fk-utils';
import { FkTypes, SignalDetectionTypes } from '~graphql/';
import { FeaturePrediction } from '~graphql/event/types';
import { ContributingChannelsConfiguration, FkConfiguration,
         FkFrequencyThumbnail } from '~graphql/fk/types';
import { findPhaseFeatureMeasurementValue } from '~graphql/signal-detection/utils';
import { ProcessingChannel } from '~graphql/station/types';
import { UILogger } from '~util/log/logger';
import { FkConfigurationWithUnits, FkParams, FkUnits } from '../../types';
import { AnalystCurrentFk } from '../fk-rendering/fk-rendering';
import {
  digitPrecision, getAnalystSelectedPoint, getPeakValueFromAzSlow, getPredictedPoint
} from '../fk-util';
import { FkConfigurationPopover } from './fk-configuration-popover';
import { FkFrequencyThumbnails } from './fk-frequency-thumbnails';

/** Frequency filter options */
const FrequencyFilters: FkTypes.FrequencyBand[] = [
  {
    minFrequencyHz: 0.5,
    maxFrequencyHz: 2
  },
  {
    minFrequencyHz: 1,
    maxFrequencyHz: 2.5
  },
  {
    minFrequencyHz: 1.5,
    maxFrequencyHz: 3
  },
  {
    minFrequencyHz: 2,
    maxFrequencyHz: 4
  },
  {
    minFrequencyHz: 3,
    maxFrequencyHz: 6
  }
];

/** Hard-coded columns for table */
// TODO: create a table-utiles/ to follow other table patterns
const columnDefs = [
  {
    headerName: '',
    field: 'key',
    width: 175,
    cellRenderer: params =>
      // Slowness is a special case because of how we display the units all others can just return
      params.value === 'Slowness' ?
      `<span style="position: relative;top: -0.4em;">${params.value} (<sup>s</sup>/<sub>°</sub>)</span>` : params.value,
  },
  {
    headerName: 'Peak',
    field: 'peak',
    width: 130,
    cellStyle: { 'text-align': 'right' },
    resizable: true,
    sortable: true,
    filter: true,
  },
  {
    headerName: 'Predicted',
    field: 'predicted',
    width: 130,
    cellStyle: { 'text-align': 'right' },
    resizable: true,
    sortable: true,
    filter: true,
  },
  {
    headerName: 'Selected',
    field: 'selected',
    cellStyle: { 'text-align': 'right' },
    width: 90,
    resizable: true,
    sortable: true,
    filter: true,
  }
];

/**
 * FkProperties Props
 */
export interface FkPropertiesProps {
  signalDetection: SignalDetectionTypes.SignalDetection;
  signalDetectionFeaturePredictions: FeaturePrediction[];
  analystCurrentFk: AnalystCurrentFk;
  userInputFkWindowParameters: FkTypes.WindowParameters;
  userInputFkFrequency: FkTypes.FrequencyBand;
  fkUnitDisplayed: FkUnits;
  fkFrequencyThumbnails: FkFrequencyThumbnail[];
  onNewFkParams(sdId: string, fkParams: FkParams, fkConfiguration: FkConfiguration): Promise<void>;
  onFkConfigurationChange(fkConfigurationWithUnits: FkConfigurationWithUnits);
}

/**
 * FkProperties State
 */
export interface FkPropertiesState {
  presetFrequency: boolean;
}

/**
 * Creates a table of FK properties
 */
export class FkProperties extends React.Component<FkPropertiesProps, FkPropertiesState> {

  private lowFreqControl: NumericInput;

  private highFreqControl: NumericInput;

  private configurationPopoverRef: PopoverButton;

  private thumbnailPopoverRef: PopoverButton;

  // ***************************************
  // BEGIN REACT COMPONENT LIFECYCLE METHODS
  // ***************************************

  /**
   * Constructor.
   * 
   * @param props The initial props
   */
  public constructor(props: FkPropertiesProps) {
    super(props);
    const fkData = getFkData(props.signalDetection.currentHypothesis.featureMeasurements);
    this.state = {
      presetFrequency: FkProperties.isPresetFrequency([fkData.lowFrequency, fkData.highFrequency])
    };
  }

  /**
   * Updates the derived state from the next props.
   * 
   * @param nextProps The next (new) props
   * @param prevState The previous state
   */
  public static getDerivedStateFromProps(nextProps: FkPropertiesProps, prevState: FkPropertiesState) {
    const fkData = getFkData(nextProps.signalDetection.currentHypothesis.featureMeasurements);
    return {
      presetFrequency: FkProperties.isPresetFrequency([fkData.lowFrequency, fkData.highFrequency])
    };
  }

  /**
   * Renders the component.
   */
  public render() {
    const stationName = this.props.signalDetection.station.name;
    const fmPhase = findPhaseFeatureMeasurementValue(this.props.signalDetection.currentHypothesis.featureMeasurements);
    const fkData = getFkData(this.props.signalDetection.currentHypothesis.featureMeasurements);
    const totalAvailableChannels = flatMap(this.props.signalDetection.station.sites, (site => site.channels));
    const trackers = this.getChannelConfigTrackers(fkData.configuration, totalAvailableChannels);
    const defaultStepSize = 0.1;
    const minorStepSize = 0.01;
    const fkConfigurationPopover = (
      <FkConfigurationPopover
        contributingChannelsConfiguration={trackers}
        maximumSlowness={fkData.configuration.maximumSlowness}
        numberOfPoints={fkData.configuration.numberOfPoints}
        mediumVelocity={fkData.configuration.mediumVelocity}
        normalizeWaveforms={fkData.configuration.normalizeWaveforms}
        useChannelVerticalOffset={fkData.configuration.useChannelVerticalOffset}
        fkUnitDisplayed={this.props.fkUnitDisplayed}
        leadFkSpectrumSeconds={fkData.configuration.leadFkSpectrumSeconds}
        applyFkConfiguration={fkConfig => {
          this.configurationPopoverRef.togglePopover();
          this.props.onFkConfigurationChange(fkConfig);
        }}
        close={() => {
          this.configurationPopoverRef.togglePopover();
        }}
      />
    );
    const fkFrequencyThumbnails = (
      <FkFrequencyThumbnails
        fkFrequencySpectra={this.props.fkFrequencyThumbnails ? this.props.fkFrequencyThumbnails : []}
        fkUnit={this.props.fkUnitDisplayed}
        onThumbnailClick={this.onThumbnailClick}
      />
    );
    return (
      <div
        className="ag-theme-dark fk-properties"
      >
        <div
          className="fk-properties__column"
        >
          <div
            className="fk-properties-label-row"
          >
            <div
              className="fk-properties-label-row__left"
            >
              <div>
                Station:
                <span
                  className="fk-properties__label"
                >
                  {stationName}
                </span>
              </div>
              <div>
                Phase:
                <span
                  className="fk-properties__label"
                >
                  {fmPhase.phase.toString()}
                </span>
              </div>
            </div>
            <div
              className="fk-properties-label-row__right"
            >
              <PopoverButton
                label="Configure..."
                tooltip="Opens configuration options for continous fk"
                popupContent={fkConfigurationPopover}
                onPopoverDismissed={() => { return; }}
                onlyShowIcon={true}
                icon={IconNames.COG}
                ref={ref => {
                  if (ref) {
                    this.configurationPopoverRef = ref;
                  }
                }}
              />
            </div>
          </div>
          <div
            className="fk-properties__table"
          >
            <div className="max">
              <Table
                columnDefs={columnDefs}
                rowData={this.getRowData(this.props)}
                getRowNodeId={node => node.id}
                deltaRowDataMode={true}
                overlayNoRowsTemplate="No data available"
              />
            </div>
          </div>
          <div
            className="fk-controls"
          >
            <div>
              <div
                className="grid-container fk-control__grid"
              >
                <div className="grid-item">Frequency:</div>
                <div
                  className={classNames(
                    'fk-properties__frequency-low-high-inputs',
                    `grid-item ${Classes.SELECT} ${Classes.FILL}`)}
                  style={{
                    display: 'flex'
                  }}
                >
                  <select
                    value={this.state.presetFrequency ?
                      this.frequencyBandToString([fkData.lowFrequency, fkData.highFrequency]) : 'Custom'}
                    onChange={this.onClickFrequencyMenu}
                  >
                    {this.generateFrequencyBandOptions()}
                  </select>
                  <div
                    style={{marginLeft: '4px'}}
                  >
                    <PopoverButton
                      ref={ref => {
                        if (ref) {
                          this.thumbnailPopoverRef = ref;
                        }
                      }}
                      label="FK Frequency Thumbanils"
                      onlyShowIcon={true}
                      popupContent={fkFrequencyThumbnails}
                      onPopoverDismissed={() => {return; }}
                      tooltip="Preview thumbnails of the fk for configured frequency sk"
                      icon={IconNames.COMPARISON}
                    />
                  </div>
                </div>
                <div className="grid-item">Low:</div>
                <div className={classNames('fk-properties__frequency-low-high-inputs', 'grid-item')}>
                  <NumericInput
                    ref={ref => this.lowFreqControl = ref}
                    className={Classes.FILL}
                    allowNumericCharactersOnly={true}
                    buttonPosition={Position.RIGHT}
                    value={this.props.userInputFkFrequency.minFrequencyHz}
                    onValueChange={this.onChangeLowFrequency}
                    selectAllOnFocus={true}
                    stepSize={defaultStepSize}
                    minorStepSize={minorStepSize}
                    majorStepSize={1}
                  />
                </div>
                <div className="grid-item">High:</div>
                <div className={classNames('fk-properties__frequency-low-high-inputs', 'grid-item')}>
                  <NumericInput
                    ref={ref => this.highFreqControl = ref}
                    className={Classes.FILL}
                    allowNumericCharactersOnly={true}
                    buttonPosition={Position.RIGHT}
                    value={this.props.userInputFkFrequency.maxFrequencyHz}
                    onValueChange={this.onChangeHighFrequency}
                    selectAllOnFocus={true}
                    stepSize={defaultStepSize}
                    minorStepSize={minorStepSize}
                    majorStepSize={1}
                  />
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    );
  }

  // ***************************************
  // END REACT COMPONENT LIFECYCLE METHODS
  // ***************************************

  /**
   * Checks if the passed in frequency is in the list of preset filters
   * @param freq frequency to check if it is in the preset list
   */
  private static isPresetFrequency(freq: number[]) {
    return FrequencyFilters.filter(freqs =>
      freqs.minFrequencyHz === freq[0] &&
      freqs.maxFrequencyHz === freq[1]).length > 0;
  }

  /**
   * Creates menu options for frequency bands
   */
  private generateFrequencyBandOptions(): JSX.Element[] {
    const items = [];
    FrequencyFilters.forEach(frequency => {
      items.push(
        <option
          key={this.frequencyBandToString([frequency.minFrequencyHz, frequency.maxFrequencyHz])}
          value={this.frequencyBandToString([frequency.minFrequencyHz, frequency.maxFrequencyHz])}
        >
          {this.frequencyBandToString([frequency.minFrequencyHz, frequency.maxFrequencyHz])}
        </option>
      );
    });
    items.push(
      <option
        key={'Custom'}
        value={'Custom'}
        hidden={true}
      >
        {`Custom`}
      </option>
    );
    return items;
  }

  /**
   * Validates numeric entries in the numeric control
   */
  private readonly validNumericEntry = (valueAsString: string, prevValue: number, controlReference: NumericInput) => {
    if (valueAsString === '') {
      // tslint:disable-next-line:no-parameter-reassignment
      valueAsString = String(prevValue);
    }

    // tslint:disable-next-line:no-parameter-reassignment
    valueAsString = valueAsString.replace(/e|\+/, '');

    controlReference.setState((prev: any) => ({
      value: valueAsString
    }));

    const newValue = isNaN(parseFloat(valueAsString)) ?
      prevValue : parseFloat(valueAsString);
    return ({
      valid: (!valueAsString.endsWith('.')
        && !isNaN(parseFloat(valueAsString))
        && newValue !== prevValue),
      value: newValue
    });
  }

  /**
   * Changes the high end of the frequency when the input changes
   */
  private readonly onChangeHighFrequency = async (highFreq: number, numberAsString: string) => {
    const fkData = getFkData(this.props.signalDetection.currentHypothesis.featureMeasurements);
    if (!fkData) {
      return;
    }

    const currentHigh = fkData.highFrequency;
    const result = this.validNumericEntry(numberAsString, currentHigh, this.highFreqControl);

    if (result.valid) {
      const currentParams = getFkParamsForSd(this.props.signalDetection);
      const priorConfig = getFkData(this.props.signalDetection.currentHypothesis.featureMeasurements).configuration;
      this.props.onNewFkParams(
        this.props.signalDetection.id,
        {
          ...currentParams,
          frequencyPair: {
            maxFrequencyHz: result.value,
            minFrequencyHz: currentParams.frequencyPair.minFrequencyHz
          },
        },
        priorConfig
      )
      .catch(error => UILogger.error(`Failed onNewFkParams: ${error}`));
    }
  }

  /**
   * Changes the low end of the frequency when the input changes
   */
  private readonly onChangeLowFrequency = async (lowFreq: number, numberAsString: string) => {
    const fkData = getFkData(this.props.signalDetection.currentHypothesis.featureMeasurements);
    if (!fkData) {
      return;
    }

    const currentLow = fkData.lowFrequency;
    const result = this.validNumericEntry(numberAsString, currentLow, this.lowFreqControl);
    if (result.valid) {
      const currentParams = getFkParamsForSd(this.props.signalDetection);
      const priorConfig = getFkData(this.props.signalDetection.currentHypothesis.featureMeasurements).configuration;

      this.props.onNewFkParams(
        this.props.signalDetection.id,
        {
          windowParams: currentParams.windowParams,
          frequencyPair: {
            maxFrequencyHz: currentParams.frequencyPair.maxFrequencyHz,
            minFrequencyHz: result.value
          }
        },
        priorConfig
      )
      .catch(error => UILogger.error(`Failed onNewFkParams: ${error}`));
    }
  }

  /**
   * Updates frequency bands from their menu
   */
  private readonly onClickFrequencyMenu = async (event: React.ChangeEvent<HTMLSelectElement>) => {
    const newFreq = FrequencyFilters.filter(pair =>
      this.frequencyBandToString([pair.minFrequencyHz, pair.maxFrequencyHz]) === event.currentTarget.value)[0];
    if (newFreq) {
      const currentParams = getFkParamsForSd(this.props.signalDetection);
      const priorConfig = getFkData(this.props.signalDetection.currentHypothesis.featureMeasurements).configuration;
      this.props.onNewFkParams(
        this.props.signalDetection.id,
        {
          ...currentParams,
          frequencyPair: {
            maxFrequencyHz: newFreq.maxFrequencyHz,
            minFrequencyHz: newFreq.minFrequencyHz
          }
        },
        priorConfig
      )
      .catch(error => UILogger.error(`Failed onNewFkParams: ${error}`));
    }
  }

  /**
   * Gets the row data for the tables from the props
   */
  private readonly getRowData = (props: FkPropertiesProps): Row[] => {
    const fkData = getFkData(this.props.signalDetection.currentHypothesis.featureMeasurements);
    const leadFk = fkData.leadSpectrum;

    // CONVERTS XY TO POLAR - doesn't seem quite right
    const x = props.analystCurrentFk ? props.analystCurrentFk.x : undefined;
    const y = props.analystCurrentFk ? props.analystCurrentFk.y : undefined;
    let analystedSelectedPoint;
    let selectedFkValue;
    if (x && y) {
      analystedSelectedPoint = getAnalystSelectedPoint(x, y);
      selectedFkValue = getPeakValueFromAzSlow(fkData, analystedSelectedPoint.azimuth,
                                               analystedSelectedPoint.slowness, props.fkUnitDisplayed);
    }

    const predictedPoint = getPredictedPoint(this.props.signalDetectionFeaturePredictions);
    const predictedFkValue = predictedPoint ?
    getPeakValueFromAzSlow(fkData, predictedPoint.azimuth, predictedPoint.slowness,
                           props.fkUnitDisplayed)
      : undefined;
    const peakFkValue = getPeakValueFromAzSlow(
      fkData, leadFk.attributes.azimuth, leadFk.attributes.slowness,
      props.fkUnitDisplayed);
    const dataRows: any[] = [];
    // Azimuth Row
    dataRows.push({
          id: 'Azimuth',
          key: 'Azimuth (°)',
          peak: this.formatValueUncertaintyPair(leadFk.attributes.azimuth, leadFk.attributes.azimuthUncertainty),
          predicted: predictedPoint ?
            this.formatValueUncertaintyPair(predictedPoint.azimuth,
                                            predictedPoint.azimuthUncertainty)
            : '',
          selected: analystedSelectedPoint ?
                      analystedSelectedPoint.azimuth.toFixed(digitPrecision)
                      : '-'
    });
    // Slowness Row
    dataRows.push({
      id: 'Slowness',
      key: 'Slowness' ,
      peak: this.formatValueUncertaintyPair(leadFk.attributes.slowness, leadFk.attributes.slownessUncertainty),
      predicted: predictedPoint ?
        this.formatValueUncertaintyPair(predictedPoint.slowness,
                                        predictedPoint.slownessUncertainty)
        : '-',
      selected: analystedSelectedPoint ?
        analystedSelectedPoint.slowness.toFixed(digitPrecision)
        : '-'
    });
    // Fstat or Power Row
    dataRows.push({
      id: this.props.fkUnitDisplayed === FkUnits.FSTAT ? 'Fstat' : 'Power',
      key: this.props.fkUnitDisplayed === FkUnits.FSTAT ? 'Fstat' : 'Power (dB)',
      peak: peakFkValue ? peakFkValue : '-',
      predicted: predictedFkValue ? predictedFkValue : '-',
      selected: selectedFkValue ? selectedFkValue : '-'
    });
    return dataRows;
  }

  /**
   * Number formattter
   * @param value Number to format
   * @param uncertainty Uncertainty value ot format
   */
  private formatValueUncertaintyPair(value: number, uncertainty: number) {
    return `${value.toFixed(digitPrecision)} (\u00B1 ${uncertainty.toFixed(digitPrecision)})`;
  }

  /**
   * Formats a frequency band into a string for the drop down
   * @param band Frequency band to format
   */
  private readonly frequencyBandToString = (band: number[]): string => `${band[0]} - ${band[1]} Hz`;

  /**
   * Merges the enabled channel trackers returned by the gateway with the full list of channels
   * From the sites/channels in the SD
   * @param fkConfiguration the fk configuration from the gateway
   * @param allAvailableChannels the processing channels for all sites for the sd
   */
  private readonly getChannelConfigTrackers =
    (fkConfiguration: FkConfiguration, allAvailableChannels: ProcessingChannel[]) => {
      const allChannelsAsTrackers: ContributingChannelsConfiguration[] = allAvailableChannels.map(channel => ({
        id: channel.id,
        name: channel.name,
        enabled: false
      }));
      const mergedTrackers = allChannelsAsTrackers.map(channelTracker => {
        const maybeMatchedChannelFromConfig =
          fkConfiguration.contributingChannelsConfiguration.find(ccc => ccc.id === channelTracker.id);
        if (maybeMatchedChannelFromConfig) {
          return maybeMatchedChannelFromConfig;
        } else {
          return channelTracker;
        }
      });
      return mergedTrackers;
    }

  private readonly onThumbnailClick = async (minFrequency: number, maxFrequency: number) => {
    if (this.thumbnailPopoverRef && this.thumbnailPopoverRef.isExpanded) {
      this.thumbnailPopoverRef.togglePopover();
    }
    const fk = getFkData(this.props.signalDetection.currentHypothesis.featureMeasurements);

    const newParams: FkParams = {
      windowParams: {
        leadSeconds: fk.windowLead,
        lengthSeconds: fk.windowLength,
        stepSize: fk.stepSize
      },
      frequencyPair: {
        minFrequencyHz: minFrequency,
        maxFrequencyHz: maxFrequency
      }
    };
    this.props.onNewFkParams(this.props.signalDetection.id, newParams, fk.configuration)
    .catch(error => UILogger.error(`Failed onNewFkParams: ${error}`));
  }
}
