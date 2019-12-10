'''
Creates a subset of test data with consistent IDs for unit testing.

Running the script:

python build_unit_test_data.py --stdsHomePath <stdsHomePath> \
                         --outDir <outDir>

Parameters: 

--stdsHomePath <stdsHomePath> - Top level directory for standard test data set
--outDir <outDir> - Path to the output directory where the converted JSON files will be written

Desired output is the following
Standard_Test_Data
    - FkSpectra
        - ChanSeg
            - <one fk chan seg file>
        - FkSpectraDefinition.json
    - feature-prediction
        - <one fp file>
    - gms_test_data_set
        - simple calibration.json
        - channel.json with one channel
        - qcmask.json with one mask on the channel above
        - events.json with one event
        - network-memberships.json with one network and one station
        - network.json with one network
        - signal-detections.json with only a few sds on channel and associated to our one event
        - site-memberships.json with one site for our one station
        - site.json wiht one site
        - station-memberships.json with one station and one channel
        - station.json with one station
        - segments-and-soh
            - <one segment file>
'''

import os
import json
import time
import argparse

def writeOutput(fileWithPath, data):
    with open(os.path.join(outDir,fileWithPath), 'w') as writeFile:
        writeFile.write(json.dumps(data, indent=4))
    writeFile.close()

def readAndProcessJson(fileWithPath, dataType):
    data = {}
    with open(fileWithPath) as jsonFile:
        data = json.loads(jsonFile.read())
    jsonFile.close()

    if (not data):
        print('Unable to read ' + dataType + ' data from file')
        exit(0)
    return data

parser = argparse.ArgumentParser()
parser.add_argument('--stdsHomePath', dest='stdsHomePath', help='Path to Standard Tesd Data directory with FkSpectra, feature-predictions, gms_test_data_set')
parser.add_argument('--outDir', dest='outDir', help='Path to the output directory where the converted JSON files will be written')

args = parser.parse_args()

# Extract command-line arguments
stdsHomePath = args.stdsHomePath
outDir = args.outDir
# configFile = args.configFile

# Handle invalid input
if (not (stdsHomePath and outDir)):
    parser.print_help()
    exit(0)


pattern = '%Y-%m-%dT%H:%M:%SZ'
gmsJsonsPath = 'gms_test_data_set'
fkSpectraPath = 'FkSpectra'
fkChanSegPath = 'ChanSeg'
featurePredictionPath = 'feature-prediction'
channelSegmentsPath = 'segments-and-soh'
networkName = 'demo'
stationName = 'ASAR'
siteName = 'AS01'
channelName = 'AS01/SHZ'

# Static Ids
staticStationId = 'station1-1111-1111-1111-111111111111'
staticSiteId = 'site1111-1111-1111-1111-111111111111'
staticChannelId = 'channel1-1111-1111-1111-111111111111'
staticQcMaskId = 'qcmask11-1111-1111-1111-111111111111'
staticEventId = 'event111-1111-1111-1111-111111111111'
staticSDId = 'sd111111-1111-1111-1111-111111111111'



# Network
networks = readAndProcessJson(os.path.join(stdsHomePath, gmsJsonsPath, 'network.json'), 'network')

networks = list(filter(lambda ntwk: ntwk['name'] == networkName, networks))
print('Networks: ' + str(len(networks)))
networkId = networks[0]['entityId']

# Network Membership
networkMemberships = readAndProcessJson(os.path.join(stdsHomePath, gmsJsonsPath, 'network-memberships.json'), 'network membership')

networkMemberships = list(filter(lambda nm: nm['networkId'] == networkId and stationName in nm['comment'], networkMemberships))
print('Network memberships: ' + str(len(networkMemberships)))
stationEntityId = networkMemberships[0]['stationId']

# Station
stations = readAndProcessJson(os.path.join(stdsHomePath, gmsJsonsPath, 'station.json'), 'station')

stations = list(filter(lambda s: s['entityId'] == stationEntityId, stations))
print('Stations: ' + str(len(stations)))
stationIds = list(map(lambda s: s['versionId'], stations))

# Station Membership
stationMemberships = readAndProcessJson(os.path.join(stdsHomePath, gmsJsonsPath, 'station-memberships.json'), 'station membership')

stationMemberships = list(filter(lambda sm: sm['stationId'] == stationEntityId and siteName in sm['comment'], stationMemberships))
print('Station memberships: ' + str(len(stationMemberships)))
siteVersionId = stationMemberships[0]['siteId']

# Site
sites = readAndProcessJson(os.path.join(stdsHomePath, gmsJsonsPath, 'site.json'), 'site')

sites = list(filter(lambda s: s['entityId'] == siteVersionId, sites))
print('Sites: ' + str(len(sites)))
siteIds = list(map(lambda s: s['versionId'], sites))

# Site Memberships
siteMemberships = readAndProcessJson(os.path.join(stdsHomePath, gmsJsonsPath, 'site-memberships.json'), 'site membership')

siteMemberships = list(filter(lambda sm: sm['siteId'] == siteVersionId and channelName in sm['comment'], siteMemberships))
print('Site memberships: ' + str(len(siteMemberships)))
channelId = siteMemberships[0]['channelId']

# Channel
channels = readAndProcessJson(os.path.join(stdsHomePath, gmsJsonsPath, 'channel.json'), 'channel')

channels = list(filter(lambda c: c['entityId'] == channelId, channels))
print('Channels: ' + str(len(channels)))
channelVersionIds = list(map(lambda c: c['versionId'], channels))

# Calibration
calibrations = readAndProcessJson(os.path.join(stdsHomePath, gmsJsonsPath, 'calibration.json'), 'calibration')

calibrations = list(filter(lambda c: c['channelId'] in channelVersionIds, calibrations))
print('Calibrations: ' + str(len(calibrations)))

# QC Masks
qcMasks = readAndProcessJson(os.path.join(stdsHomePath, gmsJsonsPath, 'converted-qc-masks.json'), 'qc mask')
qcMasks = qcMasks[0:3]
for mask in qcMasks:
    # setting channel Id to match
    mask['channelId'] = channelVersionIds[0]
print('QcMasks: ' + str(len(qcMasks)))

# Signal Detections
sds = readAndProcessJson(os.path.join(stdsHomePath, gmsJsonsPath, 'signal-detections.json'), 'sd')

sds = list(filter(lambda sd: sd['stationId'] in stationIds, sds))
print('Signal Detections: ' + str(len(sds)))
sdHypIds = list(map(lambda sd: sd['signalDetectionHypotheses'][0]['id'], sds))

# Events
rawEvents = readAndProcessJson(os.path.join(stdsHomePath, gmsJsonsPath, 'events.json'), 'event')

events = []
for event in rawEvents:
    associations = event['hypotheses'][0]['associations']
    associationsToKeep = []
    for association in associations:
        if (association['signalDetectionHypothesisId'] in sdHypIds):
            associationsToKeep.append(association)
    if len(associationsToKeep) > 0:
        event['hypotheses'][0]['associations'] = associationsToKeep
        events.append(event)
print('Events: ' + str(len(events)))

# Channel Segment
channelSegments = []
for root, dirs, files in os.walk(os.path.join(stdsHomePath, gmsJsonsPath, channelSegmentsPath)):
    filenames = list(filter(lambda f: 'segments-' in f and '.json' in f, files))
    for filename in filenames:
        segments = readAndProcessJson(os.path.join(stdsHomePath, gmsJsonsPath, channelSegmentsPath, filename), 'channel segment')
        for cs in segments:
            if (cs['channelId'] in channelVersionIds):
                channelSegments.append(cs)

print('Channel Segments: ' + str(len(channelSegments)))

chanSegIdtoW = readAndProcessJson(os.path.join(stdsHomePath, gmsJsonsPath, channelSegmentsPath, 'chan-seg-id-to-w.json'), 'channel segment map')

# Fk Spectra
fkSpectra = {}
for root, dirs, files in os.walk(os.path.join(stdsHomePath, fkSpectraPath, fkChanSegPath)):
    filenames = list(filter(lambda f: 'ASAR' in f, files))
    fkSpectra = readAndProcessJson(os.path.join(stdsHomePath, fkSpectraPath, fkChanSegPath, filenames[0]), 'fk spectra')

print('Fk Spectra: ' + str(len(fkSpectra)))

FkSpectraDefinition = readAndProcessJson(os.path.join(stdsHomePath, fkSpectraPath, 'FkSpectraDefinition.json'), 'fk spectra definition')

# Feature Prediction
featurePrediction = {}
for root, dirs, files in os.walk(os.path.join(stdsHomePath, featurePredictionPath)):
    featurePrediction = readAndProcessJson(os.path.join(stdsHomePath, featurePredictionPath, files[0]), 'feature prediction')

print('Feature Prediction: ' + str(len(featurePrediction)))

# Mess with ids to make them static
idMap = {}
for index, station in enumerate(stations):
    newId = staticStationId[:-1] + str(index)
    idMap[station['versionId']] = newId
    station['versionId'] = newId
for index, site in enumerate(sites):
    newId = staticSiteId[:-1] + str(index)
    idMap[site['versionId']] = newId
    site['versionId'] = newId
for index, channel in enumerate(channels):
    newId = staticChannelId[:-1] + str(index)
    idMap[channel['versionId']] = newId
    channel['versionId'] = newId
for index, segment in enumerate(channelSegments):
    segment['channelId'] = idMap.get(segment['channelId'])
for index, qcMask in enumerate(qcMasks):
    newId = staticQcMaskId[:-1] + str(index)
    idMap[qcMask['id']] = newId
    qcMask['id'] = newId
    qcMask['channelId'] = idMap.get(qcMask['channelId'])
for index, sd in enumerate(sds):
    newId = staticSDId[:-1] + str(index)
    idMap[sd['id']] = newId
    sd['id'] = newId
    sd['stationId'] = idMap.get(sd['stationId'])
    sd['signalDetectionHypotheses'][0]['id'] = newId
    sd['signalDetectionHypotheses'][0]['parentSignalDetectionId'] = newId
for index, event in enumerate(events):
    newId = staticEventId[:-1] + str(index)
    idMap[event['id']] = newId
    event['id'] = newId
    event['hypotheses'][0]['id'] = newId
    event['hypotheses'][0]['eventId'] = newId
    event['hypotheses'][0]['associations'][0]['eventHypothesisId'] = newId
    event['hypotheses'][0]['associations'][0]['signalDetectionHypothesisId'] = idMap.get(event['hypotheses'][0]['associations'][0]['signalDetectionHypothesisId'])




# Write output
print('Writing data to: ' + outDir)

writeOutput(os.path.join(fkSpectraPath, fkChanSegPath, 'ASAR.test.ChanSeg'), fkSpectra)
writeOutput(os.path.join(fkSpectraPath, 'FkSpectraDefinition.json'), FkSpectraDefinition)
writeOutput(os.path.join(featurePredictionPath, 'test_fp.json'), featurePrediction)
writeOutput(os.path.join(gmsJsonsPath, 'calibration.json'), sorted(calibrations))
writeOutput(os.path.join(gmsJsonsPath, 'channel.json'), sorted(channels))
writeOutput(os.path.join(gmsJsonsPath, 'converted-qc-masks.json'), sorted(qcMasks, key=lambda k: k['id']))
writeOutput(os.path.join(gmsJsonsPath, 'events.json'), sorted(events, key=lambda k: k['id']))
writeOutput(os.path.join(gmsJsonsPath, 'network-memberships.json'), sorted(networkMemberships))
writeOutput(os.path.join(gmsJsonsPath, 'network.json'), sorted(networks))
writeOutput(os.path.join(gmsJsonsPath, 'signal-detections.json'), sorted(sds, key=lambda k: k['id']))
writeOutput(os.path.join(gmsJsonsPath, 'site-memberships.json'), sorted(siteMemberships))
writeOutput(os.path.join(gmsJsonsPath, 'site.json'), sorted(sites, key=lambda k: k['entityId']))
writeOutput(os.path.join(gmsJsonsPath, 'station-memberships.json'), sorted(stationMemberships, key=lambda k: k['id']))
writeOutput(os.path.join(gmsJsonsPath, 'station.json'), sorted(stations, key=lambda k: k['entityId']))
writeOutput(os.path.join(gmsJsonsPath, channelSegmentsPath, 'segment.json'), sorted(channelSegments))
writeOutput(os.path.join(gmsJsonsPath, channelSegmentsPath, 'chan-seg-id-to-w.json'), chanSegIdtoW)
