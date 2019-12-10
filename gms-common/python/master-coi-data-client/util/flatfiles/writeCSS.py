# -*- coding: utf-8 -*-

from argparse import Namespace

from pandas.io.json import json_normalize

from util.access import *
from util.flatfiles.affiliation import write_affiliation
from util.flatfiles.arrival import write_arrival
from util.flatfiles.assoc import write_assoc
from util.flatfiles.event import write_event
from util.flatfiles.instrument import write_instrument
from util.flatfiles.network import write_network
from util.flatfiles.origerr import write_origerr
from util.flatfiles.origin import write_origin
from util.flatfiles.sensor import write_sensor
from util.flatfiles.site import write_site
from util.flatfiles.sitechan import write_sitechan
from util.flatfiles.wfdisc_and_w import write_wfdisc


def write_css_files(data, args):
    if type(args) is not Namespace:
        args = Namespace(**args)
    if args.mode == 'write_css':
        station_reference_writer(args.id[0], args.start_time, args.end_time, args.hostname, args.output_directory)
    elif args.mode == 'waveforms':
        wfdisc_writer(data, args.id[0], args.start_time, args.end_time, args.hostname)
    elif args.mode == 'signal_detections':
        arrival_writer(data, args.start_time, args.end_time, args.hostname)
    elif args.mode == 'events':
        event_writer(data, args.start_time, args.end_time, args.hostname)


'''
@:param channel_id Taken on the command line from the user
@:param start
@:param end
@:param hostname
Given the css_export arg by the user, take the channel_id and retrieve sites given channel_id, 
station membership given site id, stations given station membership, network membership given station id, 
networks given network membership.
Retrieve calibrations, sensors, and responses given channel id.
Call the write functions for the flatfiles.
'''


def station_reference_writer(channel_id, start, end, hostname, outputDir):
    channels, url = retrieve_channels_by_id(channel_id, start, end, hostname)
    sites, siteIds = site_query(channel_id, start, end, hostname)
    calibrations, url = retrieve_calibrations(channel_id, start, end, hostname)
    sensors, url = retrieve_sensors(channel_id, start, end, hostname)
    responses, url = retrieve_responses(channel_id, start, end, hostname)

    if not calibrations or not sensors or not responses:
        calibrations = None
        sensors = None
        responses = None

    stationMembers, siteToStaMemRelationship = stationMembers_query(siteIds, start, end, hostname)

    stations, staMemtoStaRelationship = station_query(siteToStaMemRelationship, start, end, hostname)

    networkMembers, stationToNetMemRelationship = networkMembers_query(staMemtoStaRelationship, start, end, hostname)

    networks, netMemtoNetRelationship = network_query(stationToNetMemRelationship, start, end, hostname)

    # NOTE: SITECHAN MUST BE CALLED BEFORE SENSOR
    # THE FOREIGN KEY VALUES FOR SENSOR ARE GENERATED IN SITECHAN
    write_network(networks)
    write_sitechan(channels, sites)
    write_site(sites, stations, siteToStaMemRelationship)
    write_affiliation(stations, networks, networkMembers, sites, stationMembers)
    write_sensor(channels, sites, calibrations, sensors)
    write_instrument(outputDir, channels, calibrations, sensors, responses)


def wfdisc_writer(channelSegments, channel_id, start, end, hostname):
    sites, url = retrieve_sites_by_channel(channel_id, start, end, hostname)
    channels, url = retrieve_channels_by_id(channel_id, start, end, hostname)
    calibrations, url = retrieve_calibrations(channel_id, start, end, hostname)
    sensors, url = retrieve_sensors(channel_id, start, end, hostname)
    write_wfdisc(channel_id, channelSegments, sites, channels, calibrations, sensors)


def arrival_writer(sigDetHyps, start, end, hostname):
    write_arrival(sigDetHyps, start, end, hostname)


def event_writer(events, start, end, hostname):
    if not events or all(not e for e in events):
        print('No events passed, CSS flat files will not be created.')
        open('p3.event', 'w').close()
        open('p3.arrival', 'w').close()
        open('p3.origin', 'w').close()
        open('p3.assoc', 'w').close()
        open('p3.origerr', 'w').close()
        return
    write_event(events)
    write_origerr(events)

    hypotheses = list(itertools.chain.from_iterable([e.get('hypotheses') for e in events]))
    if not hypotheses or all(not h for h in hypotheses):
        print('No hypotheses associated with events. Some flatfiles not written.')
        open('p3.arrival', 'w').close()
        open('p3.origin', 'w').close()
        open('p3.assoc', 'w').close()
        return

    associations = list(itertools.chain.from_iterable([h.get('associations') for h in hypotheses]))
    if not associations or all(not a for a in associations):
        print('No associations associated with hypotheses. Some flatfiles not written.')
        open('p3.arrival', 'w').close()
        open('p3.origin', 'w').close()
        open('p3.assoc', 'w').close()
        return

    sigDetHypIds = [a.get('signalDetectionHypothesisId') for a in associations]
    if not sigDetHypIds or all(not s for s in sigDetHypIds):
        print('No signal detection hypothesis ids associated with associations. Some flatfiles not written.')
        open('p3.arrival', 'w').close()
        open('p3.origin', 'w').close()
        open('p3.assoc', 'w').close()
        return
    sigDetHypotheses = []
    sigDetHypotheses, url = retrieve_signal_detection_hypotheses_by_ids(sigDetHypIds, hostname)
    if not sigDetHypotheses or all(not o for o in sigDetHypotheses):
        print('No signal detection hypotheses were retrieved. Flatfiles not written.')
        open('p3.arrival', 'w').close()
        open('p3.origin', 'w').close()
        open('p3.assoc', 'w').close()
        return
    write_arrival(sigDetHypotheses, hostname)
    write_assoc(events, associations, sigDetHypotheses, start, end, hostname)
    write_origin(events, associations, sigDetHypotheses)


def site_query(channel_id, start, end, hostname):
    sites, url = retrieve_sites_by_channel(channel_id, start, end, hostname)  # list of dicts
    siteFlat = json_normalize(sites)  # flat dataframe, can pull cols from here
    try:
        siteIds = siteFlat['entityId'].unique().tolist()  # grab the siteIds col from the dataframe and make a set
    except KeyError:
        print('No sites found associated with the channel id. Files not written.')
        return None, None
    return sites, siteIds


def stationMembers_query(siteIds, start, end, hostname):
    stationMembers = []
    siteToStaMemRelationship = {}
    for s in siteIds:
        resp, url = retrieve_station_members(s, start, end,
                                             hostname)  # query sta membership for each site and get the json response
        if not resp:
            return None, None
        stationMembers.append(resp)  # store all the json responses in a list
        norm = json_normalize(resp)  # normalize each json response into a dataframe
        stamids = norm['stationId'].unique().tolist()  # get the unique station membership ids
        siteToStaMemRelationship.update(
            {s: stamids})  # dict of dicts of siteIds mapped to staMembershipIds they are associated with
    return stationMembers, siteToStaMemRelationship


def station_query(siteToStaMemRelationship, start, end, hostname):
    if not siteToStaMemRelationship:
        return None, None
    stations = []
    staMemtoStaRelationship = {}
    for rel in siteToStaMemRelationship:
        for g in siteToStaMemRelationship.get(rel):
            resp, url = retrieve_stations_by_id(g, start, end, hostname)
            if not resp:
                return None, None
            stations.append(resp)
            norm = json_normalize(resp)
            statids = norm['entityId'].unique().tolist()
            staMemtoStaRelationship.update({g: statids})
    return stations, staMemtoStaRelationship


def networkMembers_query(staMemtoStaRelationship, start, end, hostname):
    if not staMemtoStaRelationship:
        return None, None
    networkMembers = []
    stationToNetMemRelationship = {}
    for rel in staMemtoStaRelationship:
        for g in staMemtoStaRelationship.get(rel):
            resp, url = retrieve_network_members(g, start, end, hostname)
            if not resp:
                return None, None
            networkMembers.append(resp)
            norm = json_normalize(resp)
            nMemIds = norm['networkId'].unique().tolist()
            stationToNetMemRelationship.update({g: nMemIds})
    return networkMembers, stationToNetMemRelationship


def network_query(stationToNetMemRelationship, start, end, hostname):
    if not stationToNetMemRelationship:
        return None, None
    networks = []
    netMemtoNetRelationship = {}
    for rel in stationToNetMemRelationship:
        for g in stationToNetMemRelationship.get(rel):
            resp, url = retrieve_networks_by_id(g, start, end, hostname)
            if not resp:
                return None, None
            networks.append(resp)
            norm = json_normalize(resp)
            netIds = norm['entityId'].unique().tolist()
            netMemtoNetRelationship.update({g: netIds})
    return networks, netMemtoNetRelationship
