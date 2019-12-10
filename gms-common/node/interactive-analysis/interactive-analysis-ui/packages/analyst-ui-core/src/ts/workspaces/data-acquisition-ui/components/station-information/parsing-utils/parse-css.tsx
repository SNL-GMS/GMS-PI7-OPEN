import { TimeUtil } from '@gms/ui-core-components';
import { ProcessingChannel, ProcessingSite, ProcessingStation, StationType } from '~graphql/station/types';
import { SitechanFileFields, SiteFileFields } from './css-enums';

/**
 * Determine the correct StationType
 * 
 * @param refsta site refsta information
 * @param sta site sta information
 * @param staType site station type information
 * @returns StationType 
 */
const determineStationType = (refsta: string, sta: string, staType: string): StationType => {
    const infrasoundPattern = /^I[0-6]/;
    const hydroacousticPattern = /^H[0-2]/;
    if (infrasoundPattern.test(sta)) {
        return StationType.InfrasoundArray;
    } else if (hydroacousticPattern.test(sta)) {
        return StationType.HydroacousticArray;
    } else if (refsta === sta) {
        if (staType === 'ss') {
            return StationType.Seismic3Component;
        } else if (staType === 'ar') {
            return StationType.SeismicArray;
        }
    } else if (staType === 'ss') {
        return StationType.Seismic3Component;
    } else {
        return undefined;
    }
};

const parseSitechanSta = (line: string): string =>
    line.substring(SitechanFileFields.staStart, SitechanFileFields.staEnd)
        .trim();

const parseSitechanOndate = (line: string): number =>
    parseFloat(
        line.substring(SitechanFileFields.ondateStart, SitechanFileFields.ondateEnd)
            .trim());

const parseSitechanOndateAsString = (line: string): string =>
    line.substring(SitechanFileFields.ondateStart, SitechanFileFields.ondateEnd)
        .trim();

const parseSitechanOffdate = (line: string): number =>
    parseFloat(
        line.substring(SitechanFileFields.offdateStart, SitechanFileFields.offdateEnd)
            .trim());

const parseSitechanType = (line: string): string =>
    line.substring(SitechanFileFields.chanTypeStart, SitechanFileFields.chanTypeEnd)
        .trim();

const parseSitechanDescription = (line: string): string =>
    line.substring(SitechanFileFields.descriptionStart, SitechanFileFields.descriptionEnd)
        .trim();

const parseSitechanDepth = (line: string): number =>
    parseFloat(
        line.substring(SitechanFileFields.depthStart, SitechanFileFields.depthEnd)
            .trim());

const parseSiteSta = (line: string): string =>
    line.substring(SiteFileFields.staStart, SiteFileFields.staEnd)
        .trim();

const parseSiteRefsta = (line: string): string =>
    line.substring(SiteFileFields.refstaStart, SiteFileFields.refstaEnd)
        .trim();

const parseSiteLatitude = (line: string): number =>
    parseFloat(
        line.substring(SiteFileFields.latitudeStart, SiteFileFields.latitudeEnd)
            .trim());

const parseSiteLongitude = (line: string): number =>
    parseFloat(
        line.substring(SiteFileFields.longitudeStart, SiteFileFields.longitudeEnd)
            .trim());

const parseSiteElevation = (line: string): number =>
    parseFloat(
        line.substring(SiteFileFields.elevationStart, SiteFileFields.elevationEnd)
            .trim());

const parseSiteOndate = (line: string): number =>
    parseFloat(
        line.substring(SiteFileFields.ondateStart, SiteFileFields.ondateEnd)
            .trim());

const parseSiteOffdate = (line: string): number =>
    parseFloat(
        line.substring(SiteFileFields.offdateStart, SiteFileFields.offdateEnd)
            .trim());

const parseSiteStatype = (line: string): string =>
    line.substring(SiteFileFields.staTypeStart, SiteFileFields.staTypeEnd)
        .trim();

const parseSiteDescription = (line: string): string =>
    line.substring(SiteFileFields.descriptionStart, SiteFileFields.descriptionEnd)
        .trim();

const parseSiteDnorth = (line: string): number =>
    parseFloat(
        line.substring(SiteFileFields.dnorthStart, SiteFileFields.dnorthEnd)
            .trim());

const parseSiteDeast = (line: string): number =>
    parseFloat(
        line.substring(SiteFileFields.deastStart, SiteFileFields.deastEnd)
            .trim());

const getRandomId = (): string => {
    const randomNumber = Math.random();
    const randomNumberLength = 36;
    const randomNumberSubstringStart = 2;
    const randomNumberSubstringEnd = 10;

    return randomNumber.toString(randomNumberLength)
        .substr(randomNumberSubstringStart, randomNumberSubstringEnd);
};

/**
 * Given an array of strings representing the lines in a CSS formatted site file, return a map where the keys
 * are refstas and the values are maps containing the row with ProcessingStation information and a list of rows
 * containing ProcessingSite information
 * 
 * @param siteFileContents array of strings representing the lines in a CSS formatted site file
 * @returns a map where the keys are refstas and the values are maps containing the row with ProcessingStation 
 * information and a list of rows containing ProcessingSite information
 */
const createRefstaToRow = (siteFileContents: string[]): any => {
    // Site
    const refstaToRow = {};
    siteFileContents.forEach(line => {
        if (line.length === 0) return;
        const sta = parseSiteSta(line);
        const refsta = parseSiteRefsta(line);
        // Because sta information can be repeated multiple times in a file, create a map where sta is the key
        // and the value is a list of maps with a row representing the ProcesingStation and a list of rows
        // representing the ProcessingSites for that ProcessingStation
        if (refstaToRow[refsta] === undefined) {
            refstaToRow[refsta] = { processingStationRow: undefined, processingSiteRows: [] };
        }
        if (sta === refsta) {
            refstaToRow[refsta].processingStationRow = line;
        } else {
            refstaToRow[refsta].processingSiteRows.push(line);
        }
    });
    return refstaToRow;
};

/**
 * Given an array of strings representing the lines in a CSS formatted sitechan file, return an array of 
 * maps that contain ondate, offdate, and ProcessingChannel built with a row of sitechan information
 * 
 * @param sitechanFileContents array of strings representing the lines in a CSS formatted sitechan file
 * @returns an array of maps that contain ondate, offdate, and ProcessingChannel built with a row of 
 * sitechan information
 */
const createProcessingChannels = (sitechanFileContents: string[]): any => {
    const sitechanStaToChannelInfo = {};
    sitechanFileContents.forEach(line => {
        if (line.length === 0) return;

        const sta = parseSitechanSta(line);
        // Because sta information can be repeated multiple times in a file, create a map where sta is the key
        // and the value is a list of maps with ondate, offdate, and ProcessingChannel information for that sta
        if (sitechanStaToChannelInfo[sta] === undefined) {
            sitechanStaToChannelInfo[sta] = [];
        }

        const currentChannel: ProcessingChannel = {
            id: `UNKNOWN_${getRandomId()}`,
            name: `${sta}/${parseSitechanType(line)}`,
            channelType: parseSitechanDescription(line),
            // Use 0 since CSS sitechan information does not have a sample rate
            sampleRate: 0,
            depth: parseSitechanDepth(line),
            actualTime: '-1',
            systemTime: TimeUtil.julianDateToISOString(parseSitechanOndateAsString(line))
        };
        sitechanStaToChannelInfo[sta].push({
            ondate: parseSitechanOndate(line),
            offdate: parseSitechanOffdate(line),
            processingChannel: currentChannel
        });
    });
    return sitechanStaToChannelInfo;
};

/**
 * Create ProcessingSites from site rows that have site file information. These ProcessingSites have
 * lists of ProcessingChannels which are obtained from looking up ProcessingChannel information with 
 * matching sta information that falls within the ProcessingSite's ondate / offdate time range. 
 * 
 * @param processingSiteRows site rows to use to create ProcessingSite 
 * @param sitechanStaToChannelInfo map from sitechan sta information to sitechan information (used to 
 * find the processing channels for each processing site)
 */
const createProcessingSites = (processingSiteRows: string[], sitechanStaToChannelInfo: {}): any[] => {
    const processingSites = [];
    processingSiteRows.forEach(line => {
        const sta = parseSiteSta(line);
        const staOndate = parseSiteOndate(line);
        const staOffdate = parseSiteOffdate(line);
        const staDnorth = parseSiteDnorth(line);
        const staDeast = parseSiteDeast(line);
        const processingChannelsInTimeRange = [];
        if (sitechanStaToChannelInfo[sta] != undefined) {
            sitechanStaToChannelInfo[sta].forEach(chan => {
                const chanOndate = chan.ondate;
                const chanOffdate = chan.offdate;

                if (chanOndate >= staOndate && chanOffdate <= staOffdate) {
                    // This is a little odd that the channel information comes from the site
                    // files but it results from different representations across CSS and the OSD
                    chan.processingChannel.position = {
                        eastDisplacementKm: staDnorth,
                        northDisplacementKm: staDeast,
                        verticalDisplacementKm: 0
                    };
                    processingChannelsInTimeRange.push(chan.processingChannel);
                }
            });
        }

        const currentSite: ProcessingSite = {
            id: `UNKNOWN_${getRandomId()}`,
            name: sta,
            location: {
                latDegrees: parseSiteLatitude(line),
                lonDegrees: parseSiteLongitude(line),
                elevationKm: parseSiteElevation(line)
            },
            channels: processingChannelsInTimeRange
        };
        processingSites.push(currentSite);
    });

    return processingSites;
};

/**
 * Given the contents of site and sitechan files, create ProcessingStations from that information 
 * 
 * @param siteFileConents string[] where each element is a line from a site file
 * @param sitechanFileContents string[] where each element is a line from a sitechan file
 * @returns array of ProcessingStations
 */
export function createProcessingStations(siteFileContents: string[], sitechanFileContents: string[]):
    ProcessingStation[] {
    // Create ProcessingChannels from sitechan file contents
    const sitechanStaToChannelInfo = createProcessingChannels(sitechanFileContents);
    // Create mapping from refsta to file row with ProcessingStation information and a list of file
    // rows containing ProcessingSite information
    const refstaToRow = createRefstaToRow(siteFileContents);

    const assembledStations = [];
    const refstaList = Object.keys(refstaToRow);
    refstaList.forEach(refsta => {
        const refstaRow = refstaToRow[refsta].processingStationRow;
        const sta = parseSiteSta(refstaRow);

        const hasProcessingSites = refstaToRow[refsta].processingSiteRows.length > 0;
        // If we have a station that doesn't have any processing sites, then it is its own processing site
        const processingSiteRows: string[] = hasProcessingSites ?
            refstaToRow[refsta].processingSiteRows :
            [refstaToRow[refsta].processingStationRow];
        const processingSites = createProcessingSites(processingSiteRows, sitechanStaToChannelInfo);
        const currentStation: ProcessingStation = {
            id: sta,
            name: sta,
            stationType: determineStationType(refsta, sta, parseSiteStatype(refstaRow)),
            description: parseSiteDescription(refstaRow),
            defaultChannel: undefined,
            networks: [],
            modified: false,
            location: {
                latDegrees: parseSiteLatitude(refstaRow),
                lonDegrees: parseSiteLongitude(refstaRow),
                elevationKm: parseSiteElevation(refstaRow)
            },
            sites: processingSites,
            dataAcquisition: undefined,
            latitude: parseSiteLatitude(refstaRow),
            longitude: parseSiteLongitude(refstaRow),
            elevation: parseSiteElevation(refstaRow)
        };
        assembledStations.push(currentStation);
    });
    return assembledStations;
}

/**
 * Given a file, return a Promise that, when fulfilled, will return an array of strings where array element 
 * represents a line in the input file
 * 
 * @param inputFile file to read
 * @returns Promise that resolves to array of strings where each array element represents a line in the input file
 */
export async function readUploadedFileAsText(inputFile: any): Promise<any> {
    const fileReader = new FileReader();

    return new Promise<any>((resolve, reject) => {
        fileReader.onerror = () => {
            fileReader.abort();
            reject('Problem parsing input file.');
        };

        fileReader.onload = () => {
            const content = fileReader.result as string;
            const splitContent = content.split('\n');
            const lines = [];
            splitContent.forEach(line => {
                lines.push(line);
            });
            resolve(lines);
        };
        fileReader.readAsText(inputFile);
    });
}
