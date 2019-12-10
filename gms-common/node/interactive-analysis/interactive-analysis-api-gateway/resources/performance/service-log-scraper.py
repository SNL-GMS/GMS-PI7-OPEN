import datetime
import dateutil.parser
import math
import numpy as np
import argparse


parser = argparse.ArgumentParser()
parser.add_argument("--filename", dest="filename", help="filename")
parser.add_argument("--noSerialize", dest="endWithProcess", help="service didn't include serialize logs", action="store_true")
arguments = parser.parse_args()

fileName = arguments.filename
endWithProcess = arguments.endWithProcess

if not fileName:
    parser.print_help()
    exit(0)

times = []
collection = {}

def handle_step(tag_times):
    if tag_times is None:
        print('THIS SHOULD NOT HAPPEN')
        return None
    else:
        tag_times.append(time)
        collection[tag] = tag_times
        return tag_times

# calculates time diff between to ISO time strings
def get_time_diff(start, end):
    start_date = datetime.datetime.strptime(start, '%Y-%m-%d %H:%M:%S,%f')
    end_date = datetime.datetime.strptime(end, '%Y-%m-%d %H:%M:%S,%f')
    return float(str(end_date - start_date).split(':')[2]) * 1000

def get_list_calculations(num_array):
    if not num_array:
        return [-1,-1,-1,-1]
    average = np.sum(num_array)/len(num_array)
    minimum = np.min(num_array)
    maximum = np.max(num_array)
    std_dev = np.std(num_array)

    return [average, minimum, maximum, std_dev]

def print_calulations(title, calculations):
    print('\n%s' % title)
    print('-Average:\t %f' % calculations[0])
    print('-Min:\t\t %f' % calculations[1])
    print('-Max:\t\t %f' % calculations[2])
    print('-StdDev:\t %f' % calculations[3])


# Read File Line by Line
with open(fileName) as logFile:
    for line in logFile:
        # We only care about data logging
        if 'Action:' in line:
            line_array = line.split(' ')
            time = line_array[0] + ' ' + line_array[1]
            tag = line_array[4]
            # deserializeStart deserializeEnd
            if 'Step:deserializeStart' in line:
                new_times = []
                new_times.append(time)
                collection[tag] = new_times
            if 'Step:deserializeEnd' in line:
                handle_step(collection.get(tag))
            # processingStart processingEnd
            if 'Step:processingStart' in line:
                handle_step(collection.get(tag))
            if 'Step:processingEnd' in line:
                if endWithProcess:
                    updated_tag_times = handle_step(collection.get(tag))
                    times.append(updated_tag_times)
                    del collection[tag]
                else:
                    handle_step(collection.get(tag))
            # serializeStart deserializeEnd
            if 'Step:serializeStart' in line:
                handle_step(collection.get(tag))
            if 'Step:serializeEnd' in line:
                updated_tag_times = handle_step(collection.get(tag))
                times.append(updated_tag_times)
                del collection[tag]

total_times = []
deserialize_times = []
processing_times = []
serialize_times = []

for time_collection in times:
    if endWithProcess:
        total_times.append(get_time_diff(time_collection[0], time_collection[3]))
        deserialize_times.append(get_time_diff(time_collection[0], time_collection[1]))
        processing_times.append(get_time_diff(time_collection[2], time_collection[3]))
    else:
        total_times.append(get_time_diff(time_collection[0], time_collection[5]))
        deserialize_times.append(get_time_diff(time_collection[0], time_collection[1]))
        processing_times.append(get_time_diff(time_collection[2], time_collection[3]))
        serialize_times.append(get_time_diff(time_collection[4], time_collection[5]))

print_calulations('Total', get_list_calculations(total_times))
print_calulations('Deserialize', get_list_calculations(deserialize_times))
print_calulations('Process', get_list_calculations(processing_times))
print_calulations('Serialize', get_list_calculations(serialize_times))