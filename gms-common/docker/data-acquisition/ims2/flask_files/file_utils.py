# Utilities related to the files used to send requests / receive responses using the
# NMS client

from flask import jsonify
import datetime

def create_filenames(msg_id, base_dir):
    """Return the response and request filenames and associated paths"""
    request_filename = msg_id + '.request'
    request_file_path = base_dir + '/' + request_filename
    response_filename = msg_id + '.response'
    response_file_path = base_dir + '/' + response_filename
    return [request_filename, request_file_path, response_filename, response_file_path]

def create_waveform_request(msg_id, time_range, station):
    """Create the text for a waveform request for the specified inputs"""
    request_text = '''BEGIN GSE2.0        
MSG_TYPE REQUEST
MSG_ID {msg_id}
TIME {time_range}
STA_LIST {station}
CHAN_LIST *
WAVEFORM GSE2.0
STOP
    '''.format(msg_id=msg_id, time_range=time_range, station=station)
    return request_text

def response_file_to_json(response_file_path):
    """
    Read the specified file and return a JSON object
    { 
        "ims2-response": <file-contents-as-string>,
        "reception-time": <time response was received by Flask server>
    }
    reception-time format example: 2019-03-07 09:20:34
    """
    response_file = open(response_file_path)
    response_file_contents = response_file.read()
    response_file.close()
    reception_time = str(datetime.datetime.now())
    return jsonify({'ims2-response': response_file_contents, 'reception-time': reception_time})



