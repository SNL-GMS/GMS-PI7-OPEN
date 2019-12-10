// Functions related to gettings values / keys from enums
/**
 * Gets key for value
 * @param value value to get key for
 * @param enumToIterate enum that holds keys / value
 */
export function getKeyForEnumValue(value: any, enumToIterate: any) {
    return Object.keys(enumToIterate)
           .find(e => enumToIterate[e] === value);
}
