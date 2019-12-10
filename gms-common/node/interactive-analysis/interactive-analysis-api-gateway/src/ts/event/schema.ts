/**
 * GraphQL schema definition for the waveform API gateway
 */
import { resolve } from 'path';
import { readFileSync } from 'fs';

// GraphQL schema definitions
export const schema = readFileSync(resolve(process.cwd(), 'resources/graphql/event/schema.graphql')).toString();
