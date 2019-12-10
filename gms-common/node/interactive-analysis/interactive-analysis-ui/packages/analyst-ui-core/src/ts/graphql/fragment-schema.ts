/**
 * Use query below to update the schema, by running in playground and copy pasting the result into this file.
 * ! Gateway must be ran in dev mode
 * Use the linter to help resolve copy paste format issues.
 * 
 * query {
 *    __schema {
 *       types {
 *         kind
 *         name
 *         possibleTypes {
 *           name
 *         }
 *       }
 *     }
 *   }
 */

export const fragmentSchema = {
  __schema: {
    types: [
      {
        kind: 'OBJECT',
        name: 'Query',
        possibleTypes: null
      },
      {
        kind: 'SCALAR',
        name: 'Int',
        possibleTypes: null
      },
      {
        kind: 'OBJECT',
        name: 'Analyst',
        possibleTypes: null
      },
      {
        kind: 'SCALAR',
        name: 'String',
        possibleTypes: null
      },
      {
        kind: 'OBJECT',
        name: 'ProcessingStage',
        possibleTypes: null
      },
      {
        kind: 'ENUM',
        name: 'ProcessingStageType',
        possibleTypes: null
      },
      {
        kind: 'OBJECT',
        name: 'ProcessingActivity',
        possibleTypes: null
      },
      {
        kind: 'ENUM',
        name: 'ProcessingActivityType',
        possibleTypes: null
      },
      {
        kind: 'INPUT_OBJECT',
        name: 'TimeRange',
        possibleTypes: null
      },
      {
        kind: 'SCALAR',
        name: 'Float',
        possibleTypes: null
      },
      {
        kind: 'OBJECT',
        name: 'ProcessingStageInterval',
        possibleTypes: null
      },
      {
        kind: 'ENUM',
        name: 'IntervalStatus',
        possibleTypes: null
      },
      {
        kind: 'OBJECT',
        name: 'ProcessingActivityInterval',
        possibleTypes: null
      },
      {
        kind: 'OBJECT',
        name: 'ProcessingInterval',
        possibleTypes: null
      },
      {
        kind: 'OBJECT',
        name: 'ProcessingStation',
        possibleTypes: null
      },
      {
        kind: 'ENUM',
        name: 'StationType',
        possibleTypes: null
      },
      {
        kind: 'OBJECT',
        name: 'Location',
        possibleTypes: null
      },
      {
        kind: 'OBJECT',
        name: 'ProcessingSite',
        possibleTypes: null
      },
      {
        kind: 'OBJECT',
        name: 'ProcessingChannel',
        possibleTypes: null
      },
      {
        kind: 'OBJECT',
        name: 'Position',
        possibleTypes: null
      },
      {
        kind: 'OBJECT',
        name: 'QcMask',
        possibleTypes: null
      },
      {
        kind: 'OBJECT',
        name: 'QcMaskVersion',
        possibleTypes: null
      },
      {
        kind: 'OBJECT',
        name: 'CreationInfo',
        possibleTypes: null
      },
      {
        kind: 'ENUM',
        name: 'CreatorType',
        possibleTypes: null
      },
      {
        kind: 'OBJECT',
        name: 'ProcessingNetwork',
        possibleTypes: null
      },
      {
        kind: 'OBJECT',
        name: 'DataAcquisition',
        possibleTypes: null
      },
      {
        kind: 'INPUT_OBJECT',
        name: 'DistanceToSourceInput',
        possibleTypes: null
      },
      {
        kind: 'INPUT_OBJECT',
        name: 'LocationInput',
        possibleTypes: null
      },
      {
        kind: 'ENUM',
        name: 'DistanceSourceType',
        possibleTypes: null
      },
      {
        kind: 'ENUM',
        name: 'DistanceUnits',
        possibleTypes: null
      },
      {
        kind: 'OBJECT',
        name: 'DistanceToSource',
        possibleTypes: null
      },
      {
        kind: 'OBJECT',
        name: 'RawAndFilteredChannelSegments',
        possibleTypes: null
      },
      {
        kind: 'OBJECT',
        name: 'ChannelSegment',
        possibleTypes: null
      },
      {
        kind: 'ENUM',
        name: 'TimeSeriesType',
        possibleTypes: null
      },
      {
        kind: 'ENUM',
        name: 'ChannelSegmentType',
        possibleTypes: null
      },
      {
        kind: 'INTERFACE',
        name: 'Timeseries',
        possibleTypes: [
          {
            name: 'Waveform'
          },
          {
            name: 'FkPowerSpectra'
          }
        ]
      },
      {
        kind: 'OBJECT',
        name: 'FilteredChannelSegment',
        possibleTypes: null
      },
      {
        kind: 'OBJECT',
        name: 'SignalDetection',
        possibleTypes: null
      },
      {
        kind: 'OBJECT',
        name: 'SignalDetectionHypothesis',
        possibleTypes: null
      },
      {
        kind: 'SCALAR',
        name: 'Boolean',
        possibleTypes: null
      },
      {
        kind: 'OBJECT',
        name: 'FeatureMeasurement',
        possibleTypes: null
      },
      {
        kind: 'ENUM',
        name: 'FeatureMeasurementTypeName',
        possibleTypes: null
      },
      {
        kind: 'UNION',
        name: 'FeatureMeasurementValue',
        possibleTypes: [
          {
            name: 'AmplitudeMeasurementValue'
          },
          {
            name: 'InstantMeasurementValue'
          },
          {
            name: 'NumericMeasurementValue'
          },
          {
            name: 'PhaseTypeMeasurementValue'
          },
          {
            name: 'StringMeasurementValue'
          }
        ]
      },
      {
        kind: 'OBJECT',
        name: 'AmplitudeMeasurementValue',
        possibleTypes: null
      },
      {
        kind: 'OBJECT',
        name: 'DoubleValue',
        possibleTypes: null
      },
      {
        kind: 'ENUM',
        name: 'Units',
        possibleTypes: null
      },
      {
        kind: 'OBJECT',
        name: 'InstantMeasurementValue',
        possibleTypes: null
      },
      {
        kind: 'OBJECT',
        name: 'NumericMeasurementValue',
        possibleTypes: null
      },
      {
        kind: 'OBJECT',
        name: 'PhaseTypeMeasurementValue',
        possibleTypes: null
      },
      {
        kind: 'ENUM',
        name: 'PhaseType',
        possibleTypes: null
      },
      {
        kind: 'OBJECT',
        name: 'StringMeasurementValue',
        possibleTypes: null
      },
      {
        kind: 'OBJECT',
        name: 'DefiningRule',
        possibleTypes: null
      },
      {
        kind: 'ENUM',
        name: 'DefiningOperationType',
        possibleTypes: null
      },
      {
        kind: 'OBJECT',
        name: 'SignalDetectionHypothesisHistory',
        possibleTypes: null
      },
      {
        kind: 'OBJECT',
        name: 'EventHypothesis',
        possibleTypes: null
      },
      {
        kind: 'OBJECT',
        name: 'Event',
        possibleTypes: null
      },
      {
        kind: 'INPUT_OBJECT',
        name: 'ForStage',
        possibleTypes: null
      },
      {
        kind: 'OBJECT',
        name: 'PreferredEventHypothesis',
        possibleTypes: null
      },
      {
        kind: 'ENUM',
        name: 'EventStatus',
        possibleTypes: null
      },
      {
        kind: 'OBJECT',
        name: 'SignalDetectionEventAssociation',
        possibleTypes: null
      },
      {
        kind: 'OBJECT',
        name: 'LocationSolutionSet',
        possibleTypes: null
      },
      {
        kind: 'OBJECT',
        name: 'LocationSolution',
        possibleTypes: null
      },
      {
        kind: 'OBJECT',
        name: 'EventLocation',
        possibleTypes: null
      },
      {
        kind: 'OBJECT',
        name: 'FeaturePrediction',
        possibleTypes: null
      },
      {
        kind: 'OBJECT',
        name: 'FeaturePredictionComponent',
        possibleTypes: null
      },
      {
        kind: 'ENUM',
        name: 'FeaturePredictionCorrectionType',
        possibleTypes: null
      },
      {
        kind: 'OBJECT',
        name: 'LocationRestraint',
        possibleTypes: null
      },
      {
        kind: 'OBJECT',
        name: 'LocationUncertainty',
        possibleTypes: null
      },
      {
        kind: 'OBJECT',
        name: 'Ellipse',
        possibleTypes: null
      },
      {
        kind: 'OBJECT',
        name: 'Ellipsoid',
        possibleTypes: null
      },
      {
        kind: 'OBJECT',
        name: 'LocationBehavior',
        possibleTypes: null
      },
      {
        kind: 'OBJECT',
        name: 'SignalDetectionSnapshot',
        possibleTypes: null
      },
      {
        kind: 'OBJECT',
        name: 'EventSignalDetectionAssociationValues',
        possibleTypes: null
      },
      {
        kind: 'OBJECT',
        name: 'PreferredLocationSolution',
        possibleTypes: null
      },
      {
        kind: 'OBJECT',
        name: 'WaveformFilter',
        possibleTypes: null
      },
      {
        kind: 'OBJECT',
        name: 'FileGap',
        possibleTypes: null
      },
      {
        kind: 'OBJECT',
        name: 'Mutation',
        possibleTypes: null
      },
      {
        kind: 'INPUT_OBJECT',
        name: 'ClientLogInput',
        possibleTypes: null
      },
      {
        kind: 'ENUM',
        name: 'LogLevel',
        possibleTypes: null
      },
      {
        kind: 'OBJECT',
        name: 'ClientLog',
        possibleTypes: null
      },
      {
        kind: 'INPUT_OBJECT',
        name: 'IntervalStatusInput',
        possibleTypes: null
      },
      {
        kind: 'INPUT_OBJECT',
        name: 'NewDetectionInput',
        possibleTypes: null
      },
      {
        kind: 'INPUT_OBJECT',
        name: 'SignalDetectionTimingInput',
        possibleTypes: null
      },
      {
        kind: 'INPUT_OBJECT',
        name: 'AmplitudeMeasurementValueInput',
        possibleTypes: null
      },
      {
        kind: 'INPUT_OBJECT',
        name: 'DoubleValueInput',
        possibleTypes: null
      },
      {
        kind: 'OBJECT',
        name: 'AssociationChange',
        possibleTypes: null
      },
      {
        kind: 'INPUT_OBJECT',
        name: 'UpdateDetectionInput',
        possibleTypes: null
      },
      {
        kind: 'OBJECT',
        name: 'EventAndAssociationChange',
        possibleTypes: null
      },
      {
        kind: 'INPUT_OBJECT',
        name: 'UpdateEventInput',
        possibleTypes: null
      },
      {
        kind: 'INPUT_OBJECT',
        name: 'CreateEventHypothesisInput',
        possibleTypes: null
      },
      {
        kind: 'INPUT_OBJECT',
        name: 'EventLocationInput',
        possibleTypes: null
      },
      {
        kind: 'INPUT_OBJECT',
        name: 'UpdateEventHypothesisInput',
        possibleTypes: null
      },
      {
        kind: 'INPUT_OBJECT',
        name: 'LocationBehaviorInput',
        possibleTypes: null
      },
      {
        kind: 'INPUT_OBJECT',
        name: 'QcMaskInput',
        possibleTypes: null
      },
      {
        kind: 'INPUT_OBJECT',
        name: 'WaveformFilterInput',
        possibleTypes: null
      },
      {
        kind: 'INPUT_OBJECT',
        name: 'FkInput',
        possibleTypes: null
      },
      {
        kind: 'INPUT_OBJECT',
        name: 'FrequencyBandInput',
        possibleTypes: null
      },
      {
        kind: 'INPUT_OBJECT',
        name: 'WindowParametersInput',
        possibleTypes: null
      },
      {
        kind: 'INPUT_OBJECT',
        name: 'FkConfigurationInput',
        possibleTypes: null
      },
      {
        kind: 'INPUT_OBJECT',
        name: 'ContributingChannelsConfigurationInput',
        possibleTypes: null
      },
      {
        kind: 'INPUT_OBJECT',
        name: 'MarkFksReviewedInput',
        possibleTypes: null
      },
      {
        kind: 'OBJECT',
        name: 'Subscription',
        possibleTypes: null
      },
      {
        kind: 'OBJECT',
        name: '__Schema',
        possibleTypes: null
      },
      {
        kind: 'OBJECT',
        name: '__Type',
        possibleTypes: null
      },
      {
        kind: 'ENUM',
        name: '__TypeKind',
        possibleTypes: null
      },
      {
        kind: 'OBJECT',
        name: '__Field',
        possibleTypes: null
      },
      {
        kind: 'OBJECT',
        name: '__InputValue',
        possibleTypes: null
      },
      {
        kind: 'OBJECT',
        name: '__EnumValue',
        possibleTypes: null
      },
      {
        kind: 'OBJECT',
        name: '__Directive',
        possibleTypes: null
      },
      {
        kind: 'ENUM',
        name: '__DirectiveLocation',
        possibleTypes: null
      },
      {
        kind: 'SCALAR',
        name: 'Date',
        possibleTypes: null
      },
      {
        kind: 'OBJECT',
        name: 'FrequencyBand',
        possibleTypes: null
      },
      {
        kind: 'OBJECT',
        name: 'InformationSource',
        possibleTypes: null
      },
      {
        kind: 'ENUM',
        name: 'ConfigurationInfoStatus',
        possibleTypes: null
      },
      {
        kind: 'OBJECT',
        name: 'ConfigurationInfo',
        possibleTypes: null
      },
      {
        kind: 'OBJECT',
        name: 'FstatData',
        possibleTypes: null
      },
      {
        kind: 'INPUT_OBJECT',
        name: 'CreationInfoInput',
        possibleTypes: null
      },
      {
        kind: 'INPUT_OBJECT',
        name: 'SoftwareInfoInput',
        possibleTypes: null
      },
      {
        kind: 'OBJECT',
        name: 'SoftwareInfo',
        possibleTypes: null
      },
      {
        kind: 'INPUT_OBJECT',
        name: 'ProcessingContextInput',
        possibleTypes: null
      },
      {
        kind: 'INPUT_OBJECT',
        name: 'AnalystActionReferenceInput',
        possibleTypes: null
      },
      {
        kind: 'INPUT_OBJECT',
        name: 'ProcessingStepReferenceInput',
        possibleTypes: null
      },
      {
        kind: 'OBJECT',
        name: 'Alias',
        possibleTypes: null
      },
      {
        kind: 'OBJECT',
        name: 'ProcessingCalibration',
        possibleTypes: null
      },
      {
        kind: 'INPUT_OBJECT',
        name: 'ProcessingCalibrationInput',
        possibleTypes: null
      },
      {
        kind: 'INPUT_OBJECT',
        name: 'TimeseriesInput',
        possibleTypes: null
      },
      {
        kind: 'OBJECT',
        name: 'Waveform',
        possibleTypes: null
      },
      {
        kind: 'INPUT_OBJECT',
        name: 'WaveformInput',
        possibleTypes: null
      },
      {
        kind: 'OBJECT',
        name: 'ContributingChannelsConfiguration',
        possibleTypes: null
      },
      {
        kind: 'OBJECT',
        name: 'FkConfiguration',
        possibleTypes: null
      },
      {
        kind: 'OBJECT',
        name: 'FkPowerSpectra',
        possibleTypes: null
      },
      {
        kind: 'OBJECT',
        name: 'FkPowerSpectrum',
        possibleTypes: null
      },
      {
        kind: 'OBJECT',
        name: 'FkAttributes',
        possibleTypes: null
      },
      {
        kind: 'OBJECT',
        name: 'FkFrequencyThumbnailBySDId',
        possibleTypes: null
      },
      {
        kind: 'OBJECT',
        name: 'SignalDetectionEventAssociationInput',
        possibleTypes: null
      }
    ]
  }
};
