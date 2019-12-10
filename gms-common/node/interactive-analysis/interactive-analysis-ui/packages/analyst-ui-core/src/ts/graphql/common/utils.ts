/**
 * Helper function to remove the __typename from object.
 * Gateway GraphQL doesn't like it.
 * @param object Generic object to strip __typename out of
 * @returns new object copied from param with __typename stripped out of
 */
export function removeTypeName(object: any): any {
    const newObj = {};
    // tslint:disable-next-line:forin
    for (const key in object) {
        if (object[key] instanceof Object) {
            newObj[key] = removeTypeName(object[key]);
        } else if (key !== '__typename') {
            newObj[key] = object[key];
        }
    }
    return newObj;
}
