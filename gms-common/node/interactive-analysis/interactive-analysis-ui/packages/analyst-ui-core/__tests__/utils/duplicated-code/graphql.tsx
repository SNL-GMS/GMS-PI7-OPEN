import { readFileSync } from 'fs';

// tslint:disable-next-line:no-implicit-dependencies no-var-requires no-require-imports
const path = require('path');

const commonSchema
    = readFileSync(path.resolve(__dirname, '../../../resources/graphql/common/schema.graphql'))
        .toString();

const eventSchema
    = readFileSync(path.resolve(__dirname, '../../../resources/graphql/event/schema.graphql'))
        .toString();

const fkSchema
    = readFileSync(path.resolve(__dirname, '../../../resources/graphql/fk/schema.graphql'))
        .toString();

const qcMaskSchema
    = readFileSync(path.resolve(__dirname, '../../../resources/graphql/qc-mask/schema.graphql'))
        .toString();

const signalDetectionSchema
    = readFileSync(path.resolve(__dirname, '../../../resources/graphql/signal-detection/schema.graphql'))
        .toString();

const stationSchema
    = readFileSync(path.resolve(__dirname, '../../../resources/graphql/station/schema.graphql'))
        .toString();

// const stationReferenceSchema
//     = readFileSync(path.resolve(__dirname, '../../../resources/graphql/station-reference/schema.graphql'))
//         .toString();

const waveformSchema
    = readFileSync(path.resolve(__dirname, '../../../resources/graphql/waveform/schema.graphql'))
        .toString();

const waveformFilterSchema
    = readFileSync(path.resolve(__dirname, '../../../resources/graphql/waveform-filter/schema.graphql'))
        .toString();

const workflowSchema
    = readFileSync(path.resolve(__dirname, '../../../resources/graphql/workflow/schema.graphql'))
        .toString();

export const typeDefs: string[]
    = [
        commonSchema,
        eventSchema,
        fkSchema,
        qcMaskSchema,
        signalDetectionSchema,
        stationSchema,
        // stationReferenceSchema, // file does not compile
        waveformSchema,
        waveformFilterSchema,
        workflowSchema,
    ];
