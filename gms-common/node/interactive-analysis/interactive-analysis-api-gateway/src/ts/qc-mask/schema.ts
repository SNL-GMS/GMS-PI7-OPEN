/**
 * GraphQL schema definition for the qc mask API gateway
 */
import { resolve } from 'path';
import { readFileSync } from 'fs';

// GraphQL schema definitions
export const schema = readFileSync(resolve(process.cwd(), 'resources/graphql/qc-mask/schema.graphql')).toString();
