import { FeaturePrediction } from '~graphql/event/types';
import { InstantMeasurementValue } from '~graphql/signal-detection/types';

export interface Offset {
  channelId: string;
  offset: number;
}

/**
 * Calculate offsets based on station with earliest arrival.
 */
export const calculateOffsets = (
  featurePredictions: FeaturePrediction[], phaseToOffset: string): Offset[] => {

  if (!featurePredictions || featurePredictions.length <= 0) {
    return [];
  }
  const filteredPredictions: FeaturePrediction[] =
    featurePredictions.filter(fp => fp.phase === phaseToOffset);

  // If no FPs were found for the phase selected, return nothing
  if (filteredPredictions.length <= 0) {
    return [];
  }

  filteredPredictions.sort((a, b) => {
    const aValue = (a.predictedValue as InstantMeasurementValue).value;
    const bValue = (b.predictedValue as InstantMeasurementValue).value;
    return aValue - bValue;
  });
  const baseTime: number = (filteredPredictions[0].predictedValue as InstantMeasurementValue).value;

  const offsets: Offset[] = [];

  filteredPredictions.forEach(sortedFeaturePrediction => {
    const offset: Offset = {
      channelId: sortedFeaturePrediction.channelId,
      offset: baseTime - (sortedFeaturePrediction.predictedValue as InstantMeasurementValue).value
    };
    offsets.push(offset);
  });

  return offsets;
  };
