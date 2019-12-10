/**
 * GraphQL schema definition for Data Acquisition
 */
import { resolve } from 'path';
import { readFileSync } from 'fs';

// GraphQL schema definitions
export const schema
    = readFileSync(resolve(process.cwd(), 'resources/graphql/data-acquisition/schema.graphql')).toString();
