import * as fs from 'fs';

/**
 * Reads the provided source JSON file into memory
 * @param jsonFilePath The JSON filename from which to read the JSON content
 */
export function readJsonData(jsonFilePath: string): any[] {
    const fileContents = fs.readFileSync(jsonFilePath, 'utf8');
    const records = JSON.parse(fileContents);
    return records;
}
