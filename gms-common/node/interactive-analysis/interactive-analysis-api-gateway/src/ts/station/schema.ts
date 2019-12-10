import { resolve } from 'path';
import { readFileSync } from 'fs';

/**
 * GraphQL schema definition for the waveform API gateway
 */

// GraphQL schema definitions
export const schema = readFileSync(resolve(process.cwd(), 'resources/graphql/station/schema.graphql')).toString();
