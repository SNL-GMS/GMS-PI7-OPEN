# Flask server that wraps the IDC's NMS client for requesting IMS 2.0 data

from flask import Flask, json, Response, request
import file_utils
import input_validation
import os
import random
import subprocess

app = Flask(__name__)
# This is the directory where response files will be written
base_dir = os.environ['NMS_CLI_HOME']
# This is the directory where canned test data files live
test_data_dir = os.environ['TEST_DATA_DIR']

@app.route('/request-ims-data', methods=['POST'])
def request_ims_data():
    """
    This endpoint creates a request file based on the request inputs and calls the NMS client with that 
    request. The NMS client writes the response to that request to a file. This endpoint then reads that 
    file into a JSON object and returns it.
    Returned JSON format: 
    { 
        "ims2-response": <file-contents-as-string>,
        "reception-time": <time response was received by Flask server>
    }
    reception-time format example: 2019-03-07 09:20:34
    """
    input_check_results = input_validation.check_inputs(request)
    if input_check_results is not None:
        return error_response(input_check_results)        

    time_range = request.json['timeRange']
    station = request.json['station']

    # Max msg_id size = 20 characters, so a UUID is too long; instead generate a random 20 digit number for
    # the random message id to represent this request and its associated response
    msg_id = str(random.randint(1,99999999999999999999))
    [request_filename, request_file_path, response_filename, response_file_path] = file_utils.create_filenames(msg_id, base_dir)
    
    # Write request to a file 
    request_text = file_utils.create_waveform_request(msg_id, time_range, station)    
    request_file = open(request_file_path, 'w')
    request_file.write(request_text)
    request_file.close()

    # Calling via subprocess is a little icky, but if we call this way:
    #   nms_client.batch.batchclient.bootstrap_run()
    # then we have to manually mess with sys.argv to put the filename in the command line arguments. This is unlikely
    # to hold up well with multiple requests running simultaneously
    subprocess_output = subprocess.check_output(['/ims2/nms_client/bin/nms_client.sh', '-d', base_dir, '-f', response_filename, request_file_path])
    os.remove(request_file_path)

    # Read response file if it was written. 
    if os.path.isfile(response_file_path):
        response_json = file_utils.response_file_to_json(response_file_path)        
        os.remove(response_file_path)    
        return response_json
    # No response file means there was some sort of error; return the output from calling the client. 
    else:
        return error_response(subprocess_output)

@app.route('/canned-ims2-response', methods=['GET'])
def canned_ims2_response():
    """
    This endpoint returns data from a canned response file as a JSON object
    JSON format: 
    { 
        "ims2-response": <file-contents-as-string>,
        "reception-time": <time response was received by Flask server> 
    }
    reception-time format example: 2019-03-07 09:20:34
    """
    return file_utils.response_file_to_json(test_data_dir + '/waveform_41177893.1')

def error_response(error_text):
    """Create a JSON object representing an error and return 404 Response"""
    return Response(json.dumps({'error' : error_text}), status=404, mimetype='application/json')  