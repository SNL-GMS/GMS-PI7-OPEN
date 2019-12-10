import { dataAcquisitionProcessor } from './data-acquisition-processor';
import { stationProcessor } from '../station/station-processor';
import { TransferredFile } from './model';

// GraphQL Resolvers
export const resolvers = {
  // Query resolvers
  Query: {
    // Retrieve transferred file objects by time range
    transferredFilesByTimeRange: async (_, { timeRange }) => dataAcquisitionProcessor.
                        getTransferredFilesByTimeRange(timeRange)
  },

   // Mutation Resolvers
   Mutation: {
    saveReferenceStation: async (_, { input }) => {
      const status = await dataAcquisitionProcessor.saveReferenceStation(input);
      return{
        result: status
      };
    }
  },

  // Field resolvers for FileGap
  FileGap: {
    // If station name cannot be found in cache or service, will display id
    stationName: async (transferredFile: TransferredFile) => {
      let name = transferredFile.metadata.stationId;
      const station = await stationProcessor.getStationByVersionId(transferredFile.metadata.stationId);
      if (station) {
        name = station.name;
      }
      return name;
    },
    // If channel name cannot be found in cache or service, will display id
    // Channel Ids always going to be an array of size one. NIFI processor will throw an error otherwise
    // meaning it wouldn't ever get here.
    channelNames: async (transferredFile: TransferredFile) => {
      let name = transferredFile.metadata.channelIds[0];
      const channel = await stationProcessor.getChannelByVersionId(transferredFile.metadata.channelIds[0]);
      if (channel) {
        name = channel.name;
      }
      return [name];
    },

    duration: (transferredFile: TransferredFile) =>
      dataAcquisitionProcessor.processGapDuration(transferredFile.metadata.payloadEndTime,
                                                  transferredFile.metadata.payloadStartTime),

    location: (transferredFile: TransferredFile) => dataAcquisitionProcessor.getLocation(transferredFile),

    startTime: (transferredFile: TransferredFile) => transferredFile.metadata.payloadStartTime,

    endTime: (transferredFile: TransferredFile) => transferredFile.metadata.payloadEndTime,

    priority: (transferredFile: TransferredFile) => transferredFile.priority
  }
};
