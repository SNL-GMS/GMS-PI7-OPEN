// TODO csv-parse does not seem to work without require-style import
// tslint:disable-next-line:no-implicit-dependencies no-var-requires no-require-imports
import parseSync = require('csv-parse/lib/sync');
import * as fs from 'fs';
import * as config from 'config';
import * as path from 'path';
import { gatewayLogger as logger } from '../log/gateway-logger';
import { TestDataPaths } from '../common/model';

/**
 * Utility functions for handling CSV files and other file related utils
 */

/**
 * Reads the provided source CSV file into memory
 * @param filename The CSV filename from which to read the CSV content
 */
export function readCsvData(csvFilePath: string): any[] {
    const fileContents = fs.readFileSync(csvFilePath, 'utf8');
    const records = parseSync(fileContents, {columns: true, delimiter: '\t'});
    return records;
}

/**
 * Reads the provided source JSON file into memory
 * @param jsonFilePath The JSON filename from which to read the JSON content
 */
export function readJsonData(jsonFilePath: string): any[] {
    const fileContents = fs.readFileSync(jsonFilePath, 'utf8');
    const records = JSON.parse(fileContents);
    return records;
}

/**
 * Resolves the home value in the config and returns the path
 */
export function resolveHomeDataPath(configString: any): string[] {
    // Resolve the ${HOME} value
    return [configString.replace(/\$\{([^\}]+)\}/g, (_, v) => process.env[v])];
}

/**
 * Resolves the paths to the test data based of a yaml config
 * @returns Test data paths as TestDataPaths
 */
export function resolveTestDataPaths(): TestDataPaths {
    const testDataConfig = config.get('testData.standardTestDataSet');
    const dataHome = resolveHomeDataPath(testDataConfig.stdsDataHome)[0];
    const jsonHome = dataHome.concat(path.sep).concat(testDataConfig.stdsJsonDir);
    const fpHome = dataHome.concat(path.sep).concat(testDataConfig.featurePredictions);
    const fkHome = dataHome.concat(path.sep).concat(testDataConfig.fk.fkDataPath);
    const channelsHome = jsonHome.concat(path.sep).concat(testDataConfig.channelSegment.channelSegmentSubDir);
    const additionalDataHome = config.get('testData.additionalTestData.dataPath');

    logger.debug(`STDS Home:     ${dataHome}`);
    logger.debug(`STDS Jsons:    ${jsonHome}`);
    logger.debug(`STDS FP:       ${fpHome}`);
    logger.debug(`STDS Fk:       ${fkHome}`);
    logger.debug(`STDS Channel:  ${channelsHome}`);
    logger.debug(`Non-STDS Data: ${additionalDataHome}`);

    return {
        dataHome,
        jsonHome,
        fpHome,
        fkHome,
        channelsHome,
        additionalDataHome
    };
}
