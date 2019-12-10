
/**
 * enums used for parsing CSS site and sitechan files.
 */

export enum SiteFileFields {
    staStart = 0,
    staEnd = 5,
    ondateStart = 7,
    ondateEnd = 15,
    offdateStart = 17,
    offdateEnd = 25,
    latitudeStart = 25,
    latitudeEnd = 36,
    longitudeStart = 37,
    longitudeEnd = 48,
    elevationStart = 49,
    elevationEnd = 57,
    descriptionStart = 59,
    descriptionEnd = 108,
    staTypeStart = 110,
    staTypeEnd = 113,
    refstaStart = 115,
    refstaEnd = 120,
    dnorthStart = 122,
    dnorthEnd = 130,
    deastStart = 132,
    deastEnd = 140
}

export enum SitechanFileFields {
    staStart = 0,
    staEnd = 5,
    chanTypeStart = 7,
    chanTypeEnd = 14,
    ondateStart = 16,
    ondateEnd = 24,
    offdateStart = 34,
    offdateEnd = 42,
    depthStart = 48,
    depthEnd = 56,
    descriptionStart = 72,
    descriptionEnd = 121
}
