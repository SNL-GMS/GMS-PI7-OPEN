import * as fs from 'file-system';

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
 * Writes provided object to file stringify and pretty
 * 
 * @param object object to stringify and written to a file
 * @param fileName filename doNOT include extentsion
 */
export function writeJsonPretty(object: any, fileName: string) {
    const getCircularReplacer = () => {
    const seen = new WeakSet();
    return (key, value) => {
      if (typeof value === 'object' && value !== null) {
        if (seen.has(value)) {
          // tslint:disable-next-line:no-console
          console.dir(value);
          return;
        }
        seen.add(value);
      }
      return value;
    };
  };
    fs.writeFile(`${fileName}.json`, JSON.stringify(object, getCircularReplacer, 2), function(err) {
        // tslint:disable-next-line:no-console
        console.log('file saved');
    });
}
