# -*- coding: utf-8 -*-
import datetime
import errno
import os
import sys
import uuid
from argparse import Namespace

'''
@:param args 
args can be of type:
dictionary (from an extension script) 
Namespace (from the CLI)
Convert args to a Namespace.
Check that a mode was entered.
Check that times were entered in valid formats and that if a time range exists, that it makes sense.
Reformat args where necessary.
Raise an error otherwise.
'''


def validate_args(args):
    if type(args) is not Namespace:
        args = Namespace(**args)
    args = validate_mode(args)
    if args.start_time is not None:
        validate_time(args.start_time)
        if epoch_time(args.start_time):
            args.start_time = reformatted_epoch_string(args.start_time)
        else:
            args.start_time = reformatted_iso_string(args.start_time)

    if args.end_time is not None:
        validate_time(args.end_time)
        if epoch_time(args.end_time):
            args.end_time = reformatted_epoch_string(args.end_time)
        else:
            args.end_time = reformatted_iso_string(args.end_time)

    if args.start_time is not None and args.end_time is not None:
        if not validate_time_range(args.start_time, args.end_time):
            raise ValueError
    if args.output_directory is not None:
        find_or_create_path(args.output_directory)
    return args.start_time, args.end_time


'''
epoch_time
@param time
checks if time given is in epoch time format
returns true if it is and false otherwise
'''


def epoch_time(time):
    try:
        int(time)
        return True
    except ValueError:
        return False


'''
iso_time
@param time
@:returns true if time is in iso format and false otherwise
'''


def iso_time(time):
    # throw out anything that doesn't have at least possibly yyyy-mm-dd
    if len(time) < 10:
        return False
    try:
        datetime.datetime.strptime(time[0:10], "%Y-%m-%d")
        return True
    except ValueError:
        return False


'''
valid_uuid
@:param hx
@:returns true if hx is in uuid format and false otherwise
'''


def valid_uuid(hx):
    if hx is None:
        return False
    h = '{' + hx + '}'
    try:
        uuid.UUID(h)
        return True
    except ValueError:
        return False


'''
reformatted_epoch_string
@:param es
@:returns a datetime object from the given epoch string.
'''


def reformatted_epoch_string(es):
    return datetime.datetime.utcfromtimestamp(int(es)).isoformat() + 'Z'


'''
reformatted_iso_string
@:param es
Returns a datetime object from the given epoch string.
If the string doesn't contain the suffix of Thh:mm:ss, adds it so the object parses correctly in the general case.
'''


def reformatted_iso_string(es):
    if len(es) < 19:
        return es[0:10] + 'T00:00:00Z'
    if len(es) >= 19:
        return es[0:19] + 'Z'


'''
@ param mode
Check that a mode was entered.
'''


def validate_mode(args):
    mode = args.mode
    i = args.id

    if mode is None:
        print("You must enter a mode to use the client.")
        sys.exit(1)

    modes = ['waveforms', 'metadata_only', 'soh', 'qc_masks', 'frames', 'networks', 'stations', 'sites',
                'channels', 'digitizers', 'calibrations', 'sensors', 'responses', 'signal_detections',
                'signal_detection_hypotheses', 'feature_measurements', 'write_css', 'events', 'event_hypotheses']
    ids = ['waveforms', 'metadataonly', 'soh', 'qc_masks', 'digitizers', 'calibrations', 'sensors',
                  'responses', 'css_export', 'signal_detection_hypotheses', 'event_hypotheses']

    sohs = ['soh']

    times = ['frames', 'waveforms', 'signal_detections', 'events']

    if mode not in modes:
        print('Please enter a valid mode from the following: ' + str(modes))
        sys.exit(1)

    if mode in ids:
        if not i:
            print('Please enter an id when using one of these modes: ' + str(ids))
            sys.exit(1)
        for x in i:
            if not valid_uuid(x):
                print('Malformed id entered.')
                sys.exit(1)

    if mode in sohs and not args.soh_type:
        print('Please enter a valid value for -soh_type (boolean or analog) when using one of these modes: ' + str(
            sohs))
        sys.exit(1)

    if mode in times and (not args.start_time or not args.end_time):
        print('Please enter a valid value for both -start_time and -end_time when using one of these modes: ' + str(
            times))
        sys.exit(1)

    if mode == 'events' and args.coords is not None and any(
            coord is None for coord in args.coords):
        print('When entering coordinates for event mode, '
              'all four coordinates must be provided after the -coords'
              'argument in the order: minLat minLong maxLat maxLong')
        sys.exit(1)
    if mode == 'events' and args.coords:
        args.coords = validate_coord(args.coords)
        if not args.coords:
            print('Please enter valid coordinates for event mode.')
            sys.exit(1)
    return args


'''
@:param coord
@:returns coords cast to a float from a string, else None
'''


def validate_coord(coord):
    try:
        coord = [float(c) for c in coord]
    except ValueError:
        return None
    return coord


'''
@:param time
Check that the time is in the expected format.
'''


def validate_time(time):
    if not epoch_time(time) and not iso_time(time):
        print('Please ensure all times are valid and in either epoch or ISO 8601 format. Consult the help '
              'for more information on these formats.')
        raise TypeError
    if epoch_time(time):
        return reformatted_epoch_string(time)
    if iso_time(time):
        return reformatted_iso_string(time)


'''
validate_time_range
@:param start 
@:param end
Compares 2 datetime objects to each other and the current time to check for common sense values
Raise if there's a problem, else
@:return true

'''


def validate_time_range(start, end):
    now = datetime.datetime.utcnow().strftime("%Y-%m-%dT%H:%M:%S")
    if start > end or start > now:
        print('Please ensure your start and end times are valid and in either epoch or ISO 8601 format. '
              'Consult the help for more information on these formats.')
        raise TypeError
    return True


'''
find_or_create_path
@param path
Takes the path give by the user and validates it.
If no path exists, tries to create it.
If cannot create, throws an exception.
'''


def find_or_create_path(path):
    if not os.path.exists(path):
        try:
            os.mkdir(path)
        # avoid race condition between this process and another one
        except OSError as exception:
            if exception.errno != errno.EEXIST:
                print(
                    'Please try to close other programs and try again. '
                    'Or, refrain from creating the directory yourself while the program is creating it.')
                raise

    return True
