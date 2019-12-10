# Input validation functions
from flask import request
import re
import time

def check_inputs(request):
    """Check that the request inputs are valid"""
    error_response = ''

    check_json_results = check_json(request)
    if check_json_results is not None:        
        # abort here because if the data is not json, then we won't be able 
        # to perform any of the other checks
        return check_json_results

    time_check_results = check_time_format(request)
    if time_check_results is not None:
        error_response += time_check_results + ' '
    
    station_check_results = check_station_format(request)
    if station_check_results is not None:
       error_response += station_check_results + ' '
    
    if len(error_response) == 0:
        return None
    else:
        return error_response

def check_json(request):
    if not request.json:
        return 'application/json Content-Type required'

    if not all(k in request.json for k in ('station', 'timeRange')):
       return 'Invalid JSON. station and timeRange required.'

    return None

def check_time_format(request):
    """
    Check that the time range 
    - is the expected format (e.g. 2019/03/07 09:15:34 to 2019/03/07 09:20:34)
    - has valid date information (strings can be formatted into actual date values)
    - has start time < end time
    """
    time_range = request.json['timeRange']
    time_string = '(\d{4}/\d{2}/\d{2} \d{2}:\d{2}:\d{2})'
    time_pattern = time_string + ' to ' + time_string

    # check that time range matches expected format
    if re.match(time_pattern, time_range) is None:    
        return 'Invalid timeRange format. (Example format: 2019/03/07 09:15:34 to 2019/03/07 09:20:34).'
    
    # check that the start time and the end time are valid times
    r = re.search(time_pattern, time_range)
    [start_time, end_time] = r.groups()    
    format = "%Y/%m/%d %H:%M:%S"

    try:
        time.strptime(start_time, format)
        time.strptime(end_time, format)
    except ValueError, Argument:
        return 'Invalid times specified in time range. ' + str(Argument) 

    # check that start_time is less than end_time
    if start_time >= end_time:
        return 'Invalid timeRange. start time (' + str(start_time) + ') must be less than end time (' + str(end_time) + ').'         
    else:
        return None

def check_station_format(request):
    station = request.json['station']
    if not isinstance(station, basestring):
        return 'Invalid station. Station must be a string.' 
    else:
        return None