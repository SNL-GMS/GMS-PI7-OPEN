import { FkTypes } from '~graphql/';
import { LeadLagPairAndString } from './types';

// Width of fk properties, used to calculate size allocation for fk rendering
export const MAX_WIDTH_OF_FK_PROPERTIES_PX = 493;

// Height of fk plots, used to calcualte size allocation for fk rendering
export const MAX_HEIGHT_OF_FK_PLOTS_PX = 424;

// Width/height of y and x axis respectively
export const SIZE_OF_FK_RENDERING_AXIS_PX = 35;

// The height of the rendering needs to be 8 pixels smaller than the width
export const FK_RENDERING_HEIGHT_OFFSET = 8;

/** 
 * Hard-coded lead/lag options
 */

export const LeadLagPairsValues: FkTypes.WindowParameters[] = [
    {
      leadSeconds: 1,
      lengthSeconds: 4,
      stepSize: undefined
    },
    {
      leadSeconds: 1,
      lengthSeconds: 6,
      stepSize: undefined
    },
    {
      leadSeconds: 1,
      lengthSeconds: 9,
      stepSize: undefined
    },
    {
      leadSeconds: 1,
      lengthSeconds: 11,
      stepSize: undefined

    }
  ];
export enum LeadLagPairs {
    LEAD_1_DURATION_4 = 'Lead: 1, Dur: 4',
    LEAD_1_DURATION_6 = 'Lead: 1, Dur: 6',
    LEAD_1_DURATION_9 = 'Lead: 1, Dur: 9',
    LEAD_1_DURATION_11 = 'Lead: 1, Dur: 11',
  }
export enum LeadLagPairsAndCustom {
  LEAD_1_DURATION_4 = 'Lead: 1, Dur: 4',
  LEAD_1_DURATION_6 = 'Lead: 1, Dur: 6',
  LEAD_1_DURATION_9 = 'Lead: 1, Dur: 9',
  LEAD_1_DURATION_11 = 'Lead: 1, Dur: 11',
  CUSTOM = 'Custom'
}
export const LeadLagValuesAndDisplayString: LeadLagPairAndString[] = [{
  leadLagPairs: LeadLagPairs.LEAD_1_DURATION_4,
  windowParams: LeadLagPairsValues[0]
},
{
  leadLagPairs: LeadLagPairs.LEAD_1_DURATION_6,
  windowParams: LeadLagPairsValues[1]
},
{
  leadLagPairs: LeadLagPairs.LEAD_1_DURATION_9,
  windowParams: LeadLagPairsValues[2]
},
{
  leadLagPairs: LeadLagPairs.LEAD_1_DURATION_11,
  windowParams: LeadLagPairsValues[3]
}];
