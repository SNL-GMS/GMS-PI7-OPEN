# -*- coding: utf-8 -*-
import itertools
import json

import requests

from config import *

'''
json_response
retrieve the GET or POST JSON response from the endpoint
@:param u: the endpoint to query for response content
@:param pload: the payload dictionary with params
@:returns response, u
'''
'''
If code isn't 200 OK throw some exception
Bad request 400-499 client error and response body
500-599 Server error

'''


def json_response(u, pload, request):
  headers = {'Accept': 'application/json'}
  if request is 'get':
    try:
      r = requests.get(u, params=pload, headers=headers)
      print('GET: Requesting ' + r.url)
      r.raise_for_status()
    except requests.exceptions.HTTPError as httperr:
      print(httperr)
      return None, u
    except requests.ConnectionError:
      print("Unable to connect to the URL.")
      return None, u
  else:
    try:
      r = requests.post(u, data=json.dumps(pload))
      print('POST: Requesting ' + r.url)
      r.raise_for_status()
    except requests.exceptions.HTTPError as httperr:
      print(httperr)
      return None, u
    except requests.ConnectionError:
      print("Unable to connect to the URL.")
      return None, u
  try:
    response = r.json()
  except ValueError:
    print("Invalid JSON file.")
    return None, u
  return response, u


'''
construct_base_station_reference_url
constructs a GET url to retrieve station reference COI objects
@:param hostname
@:param mode
'''


def construct_base_station_reference_url(hostname, mode):
  return hostname + station_reference_service_suffix() + mode


'''
construct_base_waveforms_url
constructs a GET url to retrieve waveform COI objects
@:param hostname
@:param mode
'''


def construct_base_waveforms_url(hostname, mode):
  return hostname + waveforms_service_suffix() + mode


'''
construct_base_signal_detections_url
constructs a GET url to retrieve signal detection COI objects
@:param hostname
@:param mode
'''


def construct_base_signal_detections_url(hostname, mode):
  return hostname + signal_detection_service_suffix() + mode


'''
construct_signal_detections_url
constructs a POST url to retrieve signal detection COI objects
@:param hostname
@:param mode
'''


def construct_signal_detections_url(hostname, mode):
  return hostname + 'coi/' + mode


'''
retrieve_events
@:param start
@:param end
@:param minLat
@:param minLong
@:param maxLat
@:param maxLong
'''


def retrieve_events(start, end, coords, hostname):
  mode = 'events/query/time-lat-lon'
  url = construct_signal_detections_url(hostname, mode)
  if not coords:
    p = {"startTime": start, "endTime": end}
  else:
    p = {'startTime': start, 'endTime': end, 'minLatitude': coords[0],
         'maxLatitude': coords[1],
         'minLongitude': coords[2], 'maxLongitude': coords[3]}
  return json_response(url, p, 'post')


'''
retrieve_event_hypotheses
@:param ids
@:param hostname
'''


def retrieve_event_hypotheses(ids, hostname):
  event = ids[0]
  eventh = None
  if len(ids) > 1:
    eventh = ids[1]
  mode = 'events/query/ids'
  u = construct_signal_detections_url(hostname, mode)
  p = [event]
  d, u = json_response(u, p, 'post')
  if not d:
    print('No data for event id or event hypothesis id was found.')
    return None, u
  # return all hypotheses for this event
  hyp = list(itertools.chain.from_iterable(
      [h.get('hypotheses') for h in d if 'hypotheses' in h]))
  if not eventh:
    return hyp, u
  else:
    # return only the hypotheses for this event that match the provided event hypothesis id
    return [h for h in hyp if h.get('id') == eventh], u


'''
retrieve_networks
@:param start
@:param end
@:param hostname
'''


def retrieve_networks(start, end, hostname):
  mode = 'networks'
  u = construct_base_station_reference_url(hostname, mode)
  p = {'start-time': start, 'end-time': end}
  return json_response(u, p, 'get')


'''
retrieve_stations
@:param network
@:param start
@:param end
@:param hostname
'''


def retrieve_stations(network, start, end, hostname):
  mode = 'stations'
  url = construct_base_station_reference_url(hostname, mode)
  p = {'network-name': network, 'start-time': start, 'end-time': end}
  return json_response(url, p, 'get')


'''
retrieve_station
@:param sid
@:param start
@:param end
@:param hostname
'''


def retrieve_station(sid, hostname):
  mode = 'stations'
  ids = [sid]
  u = construct_base_station_reference_url(hostname, mode)
  p = {'ids': ids}
  return json_response(u, p, 'post')


'''
retrieve_sites
@:param station
@:param start
@:param end
@:param hostname
'''


def retrieve_sites(station, start, end, hostname):
  mode = 'sites'
  url = construct_base_station_reference_url(hostname, mode)
  p = {'station-name': station, 'start-time': start, 'end-time': end}
  return json_response(url, p, 'get')


'''
retrieve_digitizers
@:param chanid
@:param start
@:param end
@:param hostname
'''


def retrieve_digitizers(chanid, start, end, hostname):
  mode = 'digitizers'
  url = construct_base_station_reference_url(hostname, mode)
  p = {'channel-id': chanid, 'start-time': start, 'end-time': end}
  return json_response(url, p, 'get')


'''
retrieve_channels
@:param site
@:param start
@:param end
@:param hostname
'''


def retrieve_channels(site, start, end, hostname):
  mode = 'channels'
  url = construct_base_station_reference_url(hostname, mode)
  p = {'site-name': site, 'start-time': start, 'end-time': end}
  return json_response(url, p, 'get')


'''
retrieve_calibrations
@:param chanid
@:param start
@:param end
@:param hostname
'''


def retrieve_calibrations(chanid, start, end, hostname):
  mode = 'calibrations'
  url = construct_base_station_reference_url(hostname, mode)
  p = {'channel-id': chanid, 'start-time': start, 'end-time': end}
  return json_response(url, p, 'get')


'''
retrieve_sensors
@:param chanid
@:param start
@:param end
@:param hostname
'''


def retrieve_sensors(chanid, start, end, hostname):
  mode = 'sensors'
  url = construct_base_station_reference_url(hostname, mode)
  p = {'channel-id': chanid, 'start-time': start, 'end-time': end}
  return json_response(url, p, 'get')


'''
retrieve_responses
@:param chanid
@:param start
@:param end
@:param hostname
'''


def retrieve_responses(chanid, start, end, hostname):
  mode = 'responses'
  url = construct_base_station_reference_url(hostname, mode)
  p = {'channel-id': chanid, 'start-time': start, 'end-time': end}
  return json_response(url, p, 'get')


'''
retrieve_networks_by_id
@:param netid
@:param start
@:param end
@:param hostname
'''


def retrieve_networks_by_id(netid, start, end, hostname):
  mode = 'networks'
  url = construct_base_station_reference_url(hostname, mode) + '/id/' + str(
      netid)
  p = {'start-time': start, 'end-time': end}
  return json_response(url, p, 'get')


'''
retrieve_channels_by_id
@:param chanid
@:param start
@:param end
@:param hostname
'''


def retrieve_channels_by_id(chanid, start, end, hostname):
  mode = 'channels'
  url = construct_base_station_reference_url(hostname, mode) + '/id/' + str(
      chanid)
  p = {'start-time': start, 'end-time': end}
  return json_response(url, p, 'get')


'''
retrieve_stations_by_id
@:param staid
@:param start
@:param end
@:param hostname
'''


def retrieve_stations_by_id(staid, start, end, hostname):
  mode = 'stations'
  url = construct_base_station_reference_url(hostname, mode) + '/id/' + str(
      staid)
  p = {'start-time': start, 'end-time': end}
  return json_response(url, p, 'get')


'''
retrieve_sites_by_channel
@:param chanid
@:param start
@:param end
@:param hostname
'''


def retrieve_sites_by_channel(chanid, start, end, hostname):
  mode = 'sites'
  url = construct_base_station_reference_url(hostname, mode)
  p = {'channel-id': chanid, 'start-time': start, 'end-time': end}
  return json_response(url, p, 'get')


'''
retrieve_station_members
@:param siteid
@:param start
@:param end
@:param hostname
'''


def retrieve_station_members(siteid, start, end, hostname):
  mode = 'station-memberships'
  url = construct_base_station_reference_url(hostname, mode)
  p = {'site-id': siteid, 'start-time': start, 'end-time': end}
  return json_response(url, p, 'get')


'''
retrieve_network_members
@:param stationid
@:param start
@:param end
@:param hostname
'''


def retrieve_network_members(stationid, start, end, hostname):
  mode = 'network-memberships'
  url = construct_base_station_reference_url(hostname, mode)
  p = {'station-id': stationid, 'start-time': start, 'end-time': end}
  return json_response(url, p, 'get')


'''
retrieve_waveforms
@:param chanid
@:param start
@:param end
@:param hostname
'''


def retrieve_waveforms(chanid, start, end, hostname):
  mode = 'channel-segment'
  url = construct_base_waveforms_url(hostname, mode)
  p = {'channel-id': chanid, 'start-time': start, 'end-time': end,
       'with-waveforms': 'true'}
  r, url = json_response(url, p, 'get')
  return [r], url


'''
retrieve metadata_only
@:param chanid
@:param start
@:param end
@:param hostname
'''


def retrieve_metadata_only(chanid, start, end, hostname):
  mode = 'channel-segment'
  url = construct_base_waveforms_url(hostname, mode)
  p = {'channel-id': chanid, 'start-time': start, 'end-time': end,
       'with-waveforms': 'false'}
  return json_response(url, p, 'get')


'''
retrieve_frames
@:param stationid
@:param start
@:param end
@:param hostname
'''


def retrieve_frames(stationid, start, end, hostname):
  mode = 'frames'
  url = construct_base_waveforms_url(hostname, mode)
  p = {'start-time': start, 'end-time': end, 'station-id': stationid}
  return json_response(url, p, 'get')


'''
retrieve_qcmasks
@:param chanid
@:param start
@:param end
@:param hostname
'''


def retrieve_qcmasks(chanid, start, end, hostname):
  mode = 'qc-mask'
  url = construct_base_signal_detections_url(hostname, mode)
  p = {'channel-id': chanid, 'start-time': start, 'end-time': end}
  return json_response(url, p, 'get')


'''
retrieve_soh
@:param chanid
@:param start
@:param end
@:param hostname
'''


def retrieve_soh(chanid, start, end, soh_type, hostname):
  mode = 'acquired-channel-soh/' + soh_type
  url = construct_base_waveforms_url(hostname, mode)
  if not start or not end:
    url += '/' + chanid
    p = {}
  else:
    p = {'channel-id': chanid, 'start-time': start, 'end-time': end}
  data, url = json_response(url, p, 'get')
  if not data:
    print('No soh data for channel id was found.')
    return None, url
  return data, url


'''
retrieve_signal_detections
@:param sigdetid
@:param removehyp
@:param start
@:param end
@:param hostname
'''


def retrieve_signal_detections(station, sigdetid, removehyp, start, end,
    hostname):
  mode = 'signal-detections'
  url = construct_base_signal_detections_url(hostname, mode)
  if sigdetid:
    if type(sigdetid) is list:
      url += '/' + sigdetid[0]
    else:
      url += '/' + sigdetid
  p = {'start-time': start, 'end-time': end}
  r, url = json_response(url, p, 'get')
  if removehyp and r and not all(not d for d in r):
    if not type(r) is list:
      r = [r]
    for response in r:
      del response['signalDetectionHypotheses']
  elif not removehyp and r and not all(not d for d in r):
    if not type(r) is list:
      r = [r]
  if station and r:
    r = [sd for sd in r if sd.get('stationId') == station]
    r = r[0]
  return r, url


'''
retrieve_signal_detection_hypotheses
@:param station
@:param sigdetid
@:param start
@:param end
@:param hostname
'''


def retrieve_signal_detection_hypotheses(station, sigdetid, start, end,
    hostname):
  data, url = retrieve_signal_detections(station, sigdetid, False, start, end,
                                         hostname)
  if not data:
    return None, url
  else:
    if type(data) is list:
      data = data[0]
    hypotheses = data.get('signalDetectionHypotheses')
  return hypotheses, url


'''
retrieve_signal_detection_hypotheses_by_ids
@:param sigdetids
@:param hostname
'''


def retrieve_signal_detection_hypotheses_by_ids(sigdetids, hostname):
  mode = 'signal-detections/hypotheses/query/ids'
  url = construct_signal_detections_url(hostname, mode)
  data, url = json_response(url, {'ids': sigdetids}, 'post')
  if not data:
    print('No data for signal detection hypothesis id was found.')
    return None, url
  else:
    return data, url
