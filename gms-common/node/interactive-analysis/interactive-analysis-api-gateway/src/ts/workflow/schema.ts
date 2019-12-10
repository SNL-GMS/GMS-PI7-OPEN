import { resolve } from 'path';
import { readFileSync } from 'fs';

/**
 * GraphQL schema definition for the workflow API gateway
 */

// GraphQL schema definitions
export const schema = readFileSync(resolve(process.cwd(), 'resources/graphql/workflow/schema.graphql')).toString();
