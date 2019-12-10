import datetime
import dateutil.parser
import math
import numpy as np
import argparse

parser = argparse.ArgumentParser()
parser.add_argument("--filename", dest="filename", help="filename")
parser.add_argument("--osdlog", dest="osdlog", help="was log created in mock mode?", action="store_false")
parser.add_argument("-t", "--table", dest="generate_table", help="will output formatted table data", action="store_true")
arguments = parser.parse_args()

# File name for logging output
# fileName = 'filter-output.txt'
fileName = arguments.filename

useMock = arguments.osdlog

if not fileName:
    parser.print_help()
    exit(0)

generate_table = arguments.generate_table

# High level operations to time 
# UI request -> Resolver entry -> Resolver exit -> Server response -> UI recieved
graphqlOperations = {'updateDetections': [], 'markActivityInterval': [], 'signalDetectionsByStation': [], 'computeFks': [], 'getFilteredWaveformSegmentsByChannels': [], 'createDetection': [], 'updateEvents': []}

# Lower level data actions
actions = {'filterChannelSegment': {}, 'fkChannelSegment': {}, 'beamChannelSegment': {}, 'computeFeaturePredictions': {}}

# Object to store timing for specific id'd data
# service request -> service entered -> service returning -> data returned
class TimeInfo:
    def __init__(self):
        self.requested = -1
        self.entering = -1
        self.returning = -1
        self.returned = -1
        self.request_return = -1
        self.enter_return = -1

# To String for time info object
def print_time_info(id, time_info):
    print(id)
    print(time_info.requested)
    print(time_info.entering)
    print(time_info.returning)
    print(time_info.returned)
    print(time_info.request_return)
    print(time_info.enter_return)

# calculates time diff between to ISO time strings
def get_time_diff(start, end):
    # converting timestamp to string, then grabing the end, then converting to float, then to ms
    return float(str(dateutil.parser.parse(end) - dateutil.parser.parse(start)).split(':')[2]) * 1000

def get_time_diff_array_statistics(start_list, end_list):
    """
    Gets the average, minimum, maximum, and standard deviation of time diff between two lists
    Arguments:
      start_list {[list]} -- [description]
      end_list {[list]} -- [description]
    
    Returns:
      [list] -- average, minimum, maximum, standard deviation
    """
    return get_list_calculations([get_time_diff(start_val, end_val ) 
        for start_val, end_val in zip(start_list, end_list)])

def get_data_timing(data_array):
    """
    Calculates timing between ui to gateway and time spent in resolvers
    Arguments:
      data_array {[string]} -- list of string log output containing the graphql name
    Returns:
      [json] -- json with average timings
    """
    request_time = []
    return_time = []
    entering_resolver = []
    leaving_resolver = []
    server_return = []
    for line in data_array:
        if 'request' in line:
            request_time.append(line.split(' ')[4])
        if 'returned' in line:
            return_time.append(line.split(' ')[4])
        if 'enteringResolver' in line:
            entering_resolver.append(line.split(' ')[0])
        if 'leavingResolver' in line:
            leaving_resolver.append(line.split(' ')[0])
        if 'returningFromServer' in line:
            server_return.append(line.split(' ')[0])

    total_time = get_time_diff_array_statistics(request_time, return_time)
    time_client_to_server = get_time_diff_array_statistics(request_time, entering_resolver)
    time_server_to_client = get_time_diff_array_statistics(server_return, return_time)
    time_in_resolver = get_time_diff_array_statistics(entering_resolver, leaving_resolver)
    time_to_field_resolve = get_time_diff_array_statistics(leaving_resolver, server_return)

    return {
        'Client To Server': ('average: %s, minimum: %s, maximum: %s, standard deviation: %s'
        % (time_client_to_server[0], time_client_to_server[1], time_client_to_server[2], time_client_to_server[3])), 
        'In Resolver': ('average: %s, minimum: %s, maximum: %s, standard deviation: %s'
        % (time_in_resolver[0], time_in_resolver[1], time_in_resolver[2], time_in_resolver[3])),
        'Field Resolving': ('average: %s, minimum: %s, maximum: %s, standard deviation: %s'
        % (time_to_field_resolve[0], time_to_field_resolve[1], time_to_field_resolve[2], time_to_field_resolve[3])),
        'Server to Client': ('average: %s, minimum: %s, maximum: %s, standard deviation: %s'
        % (time_server_to_client[0], time_server_to_client[1], time_server_to_client[2], time_server_to_client[3])), 
        'Total Time': ('average: %s, minimum: %s, maximum: %s, standard deviation: %s'
        % (total_time[0], total_time[1], total_time[2], total_time[3]))
    }

def get_list_calculations(num_array):
    if not num_array:
        return [-1,-1,-1,-1]
    average = np.sum(num_array)/len(num_array)
    minimum = np.min(num_array)
    maximum = np.max(num_array)
    std_dev = np.std(num_array)

    return [average, minimum, maximum, std_dev]

def print_calulations(step, calculations):
    print('\n%s' % step)
    print('-Average:\t %f' % calculations[0])
    print('-Min:\t\t %f' % calculations[1])
    print('-Max:\t\t %f' % calculations[2])
    print('-StdDev:\t %f' % calculations[3])

def print_timing(key, value):
    print('%s: %s' % (key,value))


# Calculates Average and StdDev for all entries for a specific action (filterChannelSegments)
def action_calculations(action_data):
    enter_return_array = []
    request_return_array = []
    gateway_to_service_array = []
    service_to_gateway_array = []
    for id in action_data:
        time_info = action_data[id]
        # check that the necessary data is present (filter out if it isn't)
        if useMock:
            if time_info.requested != -1 and time_info.entering != -1 and time_info.returning != -1 and time_info.returned != -1:
                enter_return_array.append(get_time_diff(time_info.entering, time_info.returning))
                request_return_array.append(get_time_diff(time_info.requested, time_info.returned))
                gateway_to_service_array.append(get_time_diff(time_info.requested, time_info.entering))
                service_to_gateway_array.append(get_time_diff(time_info.returning, time_info.returned))
        else:
            if time_info.requested != -1 and time_info.returned != -1:
                request_return_array.append(get_time_diff(time_info.requested, time_info.returned))


    enter_return_data = get_list_calculations(enter_return_array)
    request_return_data = get_list_calculations(request_return_array)
    gateway_to_service_data = get_list_calculations(gateway_to_service_array)
    service_to_gateway_data = get_list_calculations(service_to_gateway_array)

    return [request_return_data, enter_return_data, gateway_to_service_data, service_to_gateway_data]

# Gets time out of logger line and sets attribute for specific ID'd data
def set_time_from_logger_line(attribute, line_array, action_collection):
    id = line_array[-1].strip()
    time = line_array[0]
    time_info = action_collection.get(id)
    if time_info is None:
        new_time_info = TimeInfo()
        setattr(new_time_info, attribute, time)
        action_collection[id] = new_time_info
    else:
        setattr(time_info, attribute, time)
        action_collection[id] = time_info

# Handles collecting data for a specific action
def handle_action(action_collection, line):
    line_array = line.split(' ')
    if 'requestedFromService' in line:
        set_time_from_logger_line('requested', line_array, action_collection)
    if 'enteringService' in line:
        set_time_from_logger_line('entering', line_array, action_collection)
    if 'returningFromService' in line:
        set_time_from_logger_line('returning', line_array, action_collection)
    if 'returnedFromService' in line:
        set_time_from_logger_line('returned', line_array, action_collection)

# Read File Line by Line
with open(fileName) as logFile:
    for line in logFile:
        # We only care about data logging
        if '[35mdata[39m:' in line:
            # If logger line is for one our actions we are scraping for
            # pass off line to get data out of
            for action in actions:
                if action in line:
                    handle_action(actions[action], line)
            # Collects lines of data for each operation type
            for operation in graphqlOperations:
                if operation in line:
                    graphqlOperations[operation].append(line)

if(generate_table):
    action_table_raw_data = {}
    graphql_operations_table_raw_data = {}

# After collecting data - calculates for each action
for action in actions:
    calculations = action_calculations(actions[action])
    # [request_return_data, enter_return_data, gateway_to_service_data, service_to_gateway_data]
    print('\n====%s====' % action)
    print_calulations('Request/Return', calculations[0])
    print_calulations('Enter/Return', calculations[1])
    print_calulations('Gateway/Service', calculations[2])
    print_calulations('Service/Gateway', calculations[3])

    if(generate_table):
        action_table_raw_data[action] = calculations

 # After collecting lines - gets overall timing data for each operation   
for operation in graphqlOperations:
    results = get_data_timing(graphqlOperations[operation])
    print('\nStatistics for operation %s:' % operation)
    for key in results:
        print_timing(key, results.get(key))

    if(generate_table):
        graphql_operations_table_raw_data[operation] = results

def get_css_style_data():
    table_data = """
     table, th, td {
        border: 1px solid black;
        border-collapse: collapse;
        }
        th, td {
            padding: 5px;
            text-align: left;    
        }
        """
    return table_data

def get_action_table_data(actions):
    
    #Create HTML Table
    
    table_data = """
    <h2>Action Metrics</h2>
    <p>Calculations from logger for actions.</p>
    <table>"""
    
    table_row_headers = "<tr><th>Stage</th><th>Average (ms)</th><th>Min (ms)</th><th>Max (ms)</th><th>StdDev (ms)</th></tr>"
    
    table_stages = ["Request/Return", "Enter/Return", "Gateway/Service", "Service/Gateway"]

    for action in action_table_raw_data:
        action_results = action_table_raw_data[action]
        table_action_name = '<tr><th colspan="5">' + action + "</th></tr>"
        for t in table_stages:
            table_action_results_data = "<tr><td>{4}</td><td>{0:.3f}</td><td>{1:.3f}</td><td>{2:.3f}</td><td>{3:.3f}</td></tr>".format(action_results[0][0],action_results[0][1],action_results[0][2],action_results[0][3],table_stages[0])
            if useMock:         
                table_action_results_data += "<tr><td>{4}</td><td>{0:.3f}</td><td>{1:.3f}</td><td>{2:.3f}</td><td>{3:.3f}</td></tr>".format(action_results[1][0],action_results[1][1],action_results[1][2],action_results[1][3],table_stages[1])         
                table_action_results_data += "<tr><td>{4}</td><td>{0:.3f}</td><td>{1:.3f}</td><td>{2:.3f}</td><td>{3:.3f}</td></tr>".format(action_results[2][0],action_results[2][1],action_results[2][2],action_results[2][3],table_stages[2])         
                table_action_results_data += "<tr><td>{4}</td><td>{0:.3f}</td><td>{1:.3f}</td><td>{2:.3f}</td><td>{3:.3f}</td></tr>".format(action_results[3][0],action_results[3][1],action_results[3][2],action_results[3][3],table_stages[3])         
        
        table_data = table_data + table_action_name + table_row_headers + table_action_results_data
        
    table_data = table_data + "</table>"
    
    return table_data

def get_graphql_operation_table_data(graphql_operations):
    
    #Create HTML Table
    
    table_data = """
    <h2>GraphQL Operations Metrics</h2>
    <p>Calculations from logger for GraphQL Operations.</p>
    <table>"""
    
    table_row_headers = "<tr><th>Stage</th><th>Average (ms)</th><th>Min (ms)</th><th>Max (ms)</th><th>StdDev (ms)</th></tr>"
    
    table_stages = ["Client To Server", "In Resolver", "Field Resolving", "Server to Client","Total Time"]

    for operation in graphql_operations_table_raw_data:
        operation_results = graphql_operations_table_raw_data[operation]
        operational_results_items = list(operation_results.items())
        table_action_name = '<tr><th colspan="5">' + operation + "</th></tr>"
        table_operation_results_data = ""
        for t in table_stages:
            
            result_item = list(filter(lambda o_r_i: o_r_i[0] in [t], operational_results_items))[0]

            avg = result_item[1].split(',')[0].split(':')[1].strip()
            min = result_item[1].split(',')[1].split(':')[1].strip()
            max = result_item[1].split(',')[2].split(':')[1].strip()
            std_dev = result_item[1].split(',')[3].split(':')[1].strip()
            stage = result_item[0]
            table_operation_results_data += "<tr><td>{4}</td><td>{0:.3f}</td><td>{1:.3f}</td><td>{2:.3f}</td><td>{3:.3f}</td></tr>".format(float(avg),float(min),float(max),float(std_dev),stage)

        table_data = table_data + table_action_name + table_row_headers + table_operation_results_data
        
    table_data = table_data + "</table>"
    
    return table_data

# generate table data for formatted output
if(generate_table):
    print('\n')
    css_style_data = get_css_style_data()
    print(css_style_data)
    print('\n')
    action_table_data = get_action_table_data(actions)
    print(action_table_data) 
    print('\n')
    graphql_operations_table_data = get_graphql_operation_table_data(graphqlOperations)
    print(graphql_operations_table_data)
    print('\n')
