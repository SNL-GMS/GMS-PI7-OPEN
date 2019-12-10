/**
 * GraphQL schema definition for the waveform API gateway
 */
import { resolve } from 'path';
import { readFileSync } from 'fs';
import { GraphQLScalarType } from 'graphql';
import { Kind } from 'graphql/language';

const SECONDS_TO_MILLIS = 1000;

// GraphQL schema definitions
export const schema = readFileSync(resolve(process.cwd(), 'resources/graphql/common/schema.graphql')).toString();

// GraphQL custom Date scalar to convert between typescript Date and seconds integer
export const gqlDate = new GraphQLScalarType({
    name: 'Date',
    description: 'Date custom scalar type',
    /**
     * parse date value as number from client
     * @param value 
     */
    parseValue(value: number) {
        // Input is expected in seconds; convert to milliseconds to
        // satisfy the Javascript Date API
        return new Date(value * SECONDS_TO_MILLIS);
    },
    /**
     * Serialize Date object to milliseconds to send to client
     * @param value 
     */
    serialize(value: Date) {
        // Output is expected in seconds; convert from milliseconds to
        // which is used in the Javascript Date API
        return value.getTime() / SECONDS_TO_MILLIS;
    },
    /**
     * Parse the ast node
     * @param ast - should be an INT or FLOAT
     */
    parseLiteral(ast) {
        if (ast.kind === Kind.INT) {
            const base = 10;
            return parseInt(ast.value, base);
        } else if (ast.kind === Kind.FLOAT) {
            return parseFloat(ast.value);
        }
        return undefined;
    },
});
