import { resolve } from 'path';
import { readFileSync } from 'fs';

/**
 * GraphQL schema definition for the waveform filter API gateway
 */

// GraphQL schema definitions
export const schema = readFileSync
    (resolve(process.cwd(), 'resources/graphql/waveform-filter-definition/schema.graphql')).toString();
