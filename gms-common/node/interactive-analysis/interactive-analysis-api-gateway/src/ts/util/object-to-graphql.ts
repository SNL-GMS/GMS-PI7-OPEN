
/**
 * Creates graphQL input strings for mutation/queries for generic objects
 * Does not work for objects containing arrays
 * @param object 
 * @param resultString 
 * @returns a graphQL string 
 */
export function objectToGraphQLString(object: any, resultString: string): string {
    Object.keys(object).forEach(key => {
        const value = object[key];
        if (value) {
            // TODO: needs to handle arrays properly
            if (typeof value === 'object') {
                resultString = objectToGraphQLString(value, `{${resultString}`);
                resultString += '},';
            } else if (typeof value === 'string') {
                resultString += `${key}: "${object[key]}",`;
            } else {
                resultString += `${key}: ${object[key]},`;
            }
        }
    });
    return resultString;
}
