import { ProcessingChannel } from '../station/model';

/**
 * Converts Processing Channels into OSD Compatiable Channels
 * @param channels Gateway compatiable Processing Channel
 * @returns OSD compatiable channel list
 */
export function convertOSDChannel(channels: ProcessingChannel[]): any[] {
    const osdChannels = [];
    channels.forEach(channel => {
        const newChannel = {
            ...channel
        };
        delete newChannel.locationCode;
        delete newChannel.siteId;
        delete newChannel.siteName;
        osdChannels.push(newChannel);
    });
    return osdChannels;
}
