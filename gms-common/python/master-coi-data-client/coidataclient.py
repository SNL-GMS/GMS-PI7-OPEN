# -*- coding: utf-8 -*-
import argparse

from termcolor import colored

from util.access import *
from util.flatfiles.writeCSS import write_css_files, station_reference_writer
from util.validate import *
from util.visualization import *

print(colored(
    """
                              .
   _____ __  __  _____      . ¦
  / ____|  \/  |/ ____|     ¦.¦
 | |  __| \  / | (___       ¦|¦
 | | |_ | |\/| |\___ \      ¦|¦ .
 | |__| | |  | |____) |     ¦|¦.¦
  \_____|_|  |_|_____/   .  ¦|¦|¦. .    .
                   . . |.| |¦|¦|¦|.|. ..¦. .  Geophysical
----------~~~~~~¦|¦|¦|¦|¦|¦|¦|¦|¦|¦|¦|¦|¦|¦|  Monitoring
                   ` ` |`| |¦|¦|¦|`|' `'¦' `  System
                         '  ¦|¦|¦' |    `
                            ¦|¦|'  `
                            ¦|'|
                            ¦| `
                            `|
                             '
   """, 'green'))

"""
       :param mode: (str) - REQUIRED. Mode to operate within.
                            When no additional argument is given, returns all objects corresponding to that mode's type.

   :param hostname: (str) - REQUIRED. Hostname prefix to use. e.g. http://localhost:8080/
                            Be careful to end the prefix with the forward slash.
                            Examples below do not illustrate the -hostname argument.

                            Some modes require additional arguments as described below.
                            While some modes require a start time and end time, all others can take one or both as
                            optional arguments to narrow the results, and epoch, ISO, and normal times can be mixed and matched:
                            python coidataclient.py -mode networks -start_time 0 -end_time 1999-01-01
                            python coidataclient.py -mode networks -start_time 1999-01-01 -end_time 1987-09-22T00:00:00

                            OPTIONAL FOR ALL MODES:
                            -json_dump true (output a json file)
                            -output_directory (specify where any output should be stored to;
                                               if directory doesn't exit, client will attempt to create it)

                            SUPPORTED MODES:
                            networks //
                                    python coidataclient.py -mode networks

                            stations //
                                    python coidataclient.py -mode stations
                                    python coidataclient.py -mode stations -network IMS_AUX

                            sites    //
                                    python coidataclient.py -mode sites
                                    python coidataclient.py -mode sites -station JNU

                            digitizers REQUIRED: id NOTE: this mode not supported yet //
                                    python coidataclient.py -mode digitizers -id (channel UUID goes here)

                            channels //
                                    python coidataclient.py -mode channels

                            calibrations REQUIRED: id //
                                    python coidataclient.py -mode calibrations -id (channel UUID goes here)

                            sensors REQUIRED: id //
                                    python coidataclient.py -mode sensors -id (channel UUID goes here)

                            responses REQUIRED: id //
                                    python coidataclient.py -mode responses -id (channel UUID goes here)

                            waveforms REQUIRED: id, start_time, end_time OPTIONAL: plot//
                                    python coidataclient.py -mode waveforms -id (channel UUID goes here) -start_time 0 -end_time 1999-01-01
                                    python coidataclient.py -mode waveforms -id (channel UUID goes here) -start_time 0 -end_time 1999-01-01
                                    python coidataclient.py -mode waveforms -id (channel UUID goes here) -start_time 0 -end_time 1999-01-01 -plot true


                            metadata_only REQUIRED: id, start_time, end_time //
                                    python coidataclient.py -mode metadata_only -id (channel UUID goes here) -start_time 0 -end_time 1999-01-01

                            soh REQUIRED: id, soh_type //
                                    python coidataclient.py -mode soh -id (channel UUID goes here) -soh_type analog
                                    python coidataclient.py -mode soh -id (channel UUID goes here) -soh_type boolean

                            qc_masks REQUIRED: id, start_time, end_time //
                                    python coidataclient.py -mode qc_masks -id (channel UUID goes here) -start_time 0 -end_time 1999-01-01

                            frames REQUIRED: start_time, end_time//
                                    OPTIONAL: id
                                    python coidataclient.py -mode frames -start_time 0 -end_time 1999-01-01 -station ASAR
                                    python coidataclient.py -mode frames -start_time 0 -end_time 1999-01-01 -id (station UUID goes here)


                            signal_detections REQUIRED: start_time, end_time //
                                    python coidataclient.py -mode signal_detections -start_time 0 -end_time 1999-01-01

                            signal_detection_hypotheses REQUIRED: id //
                                    python coidataclient.py -mode signal_detection_hypotheses -id (signal detection UUID goes here)

                            events REQUIRED: start_time, end_time
                                   OPTIONAL: provide all four of -coords minLat minLong maxLat maxLong to narrow results by region//
                                    python coidataclient.py -mode events -start_time 0 -end_time 1999-01-01
                                    python coidataclient.py -mode events  -coords -90  90  -180  180 -start_time 0 -end_time 1999-01-01
                                    coords: minLat maxLat minLong maxLong

                            event_hypotheses REQUIRED: id (event id), id (event hypothesis id) order matters!
                                    python coidataclient.py -mode event_hypotheses -id (event UUID goes here) (event hypotheses UUID goes here)


                            write_css REQUIRED: id // NOTE: this mode is a WIP and not currently suppported
                                    python coidataclient.py -mode write_css -id (channel UUID goes here)



       :param network: (str)-   Use the name of a network with the stations mode to filter the stations output to only include a given network:
                                    python coidataclient.py -mode stations -network IMS_AUX

       :param station: (str) -  Use with the sites mode to filter the sites output to only include a given station:
                                python coidataclient.py -mode sites -station JNU

       :param site: (str) -     Use within the channels mode to filter the channels output to only include a given site:
                                python coidataclient.py -mode channels -site MK01


       :param start_time: (str) - REQUIRED FOR SOME MODES; SEE MODE LIST. Optional argument for other modes.
                                  Should be in UTC ISO 8601 format or Epoch time format (ending Z is not required). A range of
                                  start/end time can be used in conjunction. For example, start_time 1987-09-22T00:00:00
                                  finds any object that came into existence on or after 1987-09-22, but not any that were
                                  superseded in time by another version before this time. So, if there an object with an
                                  ondate=1985-01-01T00:00:00 and then another with ondate=1986-01-01T00:00:00 and
                                  offdate=N/A, only the second record is found. If just the date is desired with no
                                  specific time, can just the date in the form: 1987-09-22
                                  (DEFAULT = None).

       :param end_time: (str) - REQUIRED FOR SOME MODES; SEE MODE LIST. Optional argument for other modes.
                                Should be in UTC ISO 8601 format or Epoch time format. A range of start/end
                                time can be used in conjunction. For example, end_time 1990-06-19T00:00 finds any object
                                that came into existence on or before 1990-06-19, but not any that came into existence
                                after that. So, if there was an object with ondate=1989-01-01 an then another with
                                ondate=1991-01-01 only the first would be found. For a range, -start_time
                                1987-09-22T00:00:00 -end_time 1990-06-19T00:00:00 it only finds objects that existed within
                                the time period, which corresponds to any versions of station reference objects that would
                                relate back to acquired data for this time period. If just the date is desired with no
                                specific time, can just the date in the form: 1987-09-22
                                (DEFAULT = None)

       :param id: (str) - REQUIRED FOR SOME MODES; SEE MODE LIST. A valid id for some modes can be required by running
                          the client in its parent mode; e.g. a channel id can be required by running the client in channels mode.

       :param soh_type: (str) -   REQUIRED FOR SOME MODES; SEE MODE LIST. State of Health is represented in 2 ways: boolean and analog.
                                  Must specify 'boolean' or 'analog', e.g. -soh_type boolean.

       :param output_directory (str) - Only useful in modes that write files or images. When specified, the resulting files
                                       will write to the given directory. If the directory does not exist, it will be created.

       :param plot_waveforms: (boolean) - Can only be utilized when in 'waveform' mode. When True will plot the waveform and its
                                corresponding PSD.

       :param css_export: (boolean) -   SEE ABOVE FOR USE AS A MODE.
                                        Useable as a param in the following modes:
                                        waveforms
                                        signal_detections
                                        Will write out the associated flatfiles.

       :param json_dump: (boolean) - When true will output a JSON of the retrieved COI object(s).

       :param hostname: (str) - Allows the user to override the hostname prefix of the url the client uses to construct requests.
                                The hostname must begin with the protocol prefix (like http://) and end with a forward slash, e.g.
                                http://localhost:8080/

       :param print_table (str) - When true will print the JSON of the retrieved COI object(s) in tabular format to the screen.

       :return: data (dict) - A (possibly nested) dictionary of the JSON of the COI object(s) retrieved by the client.

       :return url (str) - The url that the client constructed to make the request.

       """


def get_command_line_args():
    """
    :return: parser.parse_known_args
    :rtype: Namespace Object
    """
    parser = argparse.ArgumentParser()
    parser.add_argument('-mode', type=str, action='store', required=True)
    parser.add_argument('-start_time', type=str, action='store', default=None)
    parser.add_argument('-end_time', type=str, action='store', default=None)
    parser.add_argument('-network', type=str, action='store', default=None)
    parser.add_argument('-station', type=str, action='store', default=None)
    parser.add_argument('-site', type=str, action='store', default=None)
    parser.add_argument('-coords', type=str, nargs=4, default=None)
    parser.add_argument('-soh_type', type=str, action='store', default=None)
    parser.add_argument('-id', type=str, nargs='+', default=None)
    parser.add_argument('-plot', type=str, action='store', default=False)
    parser.add_argument('-write_css', type=str, action='store', default=False)
    parser.add_argument('-output_directory', type=str, action='store', default=None)
    parser.add_argument('-hostname', type=str, action='store', default=None, required=True)
    parser.add_argument('-json_dump', type=str, action='store', default=False)
    parser.add_argument('-print_table', type=str, action='store', default=True)

    # toss unknown args or extra args stored in [1]
    return parser.parse_known_args()[0]


def retrieve_data(args):
    mode = args.mode
    if mode == 'networks':
        d, u = retrieve_networks(args.start_time, args.end_time, args.hostname)
    elif mode == 'stations':
        d, u = retrieve_stations(args.network, args.start_time, args.end_time, args.hostname)
    elif mode == 'sites':
        d, u = retrieve_sites(args.station, args.start_time, args.end_time, args.hostname)
    elif mode == 'digitizers':
        d, u = retrieve_digitizers(args.id[0], args.start_time, args.end_time, args.hostname)
    elif mode == 'channels':
        d, u = retrieve_channels(args.site, args.start_time, args.end_time, args.hostname)
    elif mode == 'calibrations':
        d, u = retrieve_calibrations(args.id[0], args.start_time, args.end_time, args.hostname)
    elif mode == 'sensors':
        d, u = retrieve_sensors(args.id[0], args.start_time, args.end_time, args.hostname)
    elif mode == 'responses':
        d, u = retrieve_responses(args.id[0], args.start_time, args.end_time, args.hostname)
    elif mode == 'metadata_only':
        d, u = retrieve_metadata_only(args.id[0], args.start_time, args.end_time, args.hostname)
        # Endpoint does not return a list; list is required for parsing
        d = [d]
    elif mode == 'soh':
        d, u = retrieve_soh(args.id[0], args.start_time, args.end_time, args.soh_type, args.hostname)
    elif mode == 'qc_masks':
        d, u = retrieve_qcmasks(args.id[0], args.start_time, args.end_time, args.hostname)
    elif mode == 'frames':
        d, u = retrieve_frames(args.station, args.start_time, args.end_time, args.hostname)
    elif mode == 'signal_detections':
        d, u = retrieve_signal_detections(args.station, args.id, True, args.start_time, args.end_time,
                                          args.hostname)
    elif mode == 'signal_detection_hypotheses':
        d, u = retrieve_signal_detection_hypotheses(args.station, args.id, args.start_time,
                                                    args.end_time,
                                                    args.hostname)
    elif mode == 'events':
        d, u = retrieve_events(args.start_time, args.end_time, args.coords, args.hostname)
    elif mode == 'event_hypotheses':
        d, u = retrieve_event_hypotheses(args.id, args.hostname)
    elif mode == 'write_css':
        station_reference_writer(args.id[0], args.start_time, args.end_time, args.hostname, args.output_directory)
        args.print_table = False
        d, u = ['Writing station reference CSS flatfiles.'], ''
    elif mode == 'waveforms':
        d, u = retrieve_waveforms(args.id[0], args.start_time, args.end_time, args.hostname)
        print(d)
        if args.plot:
            if d and not all(not x for x in d):
                plot_waveforms(d)
            else:
                print('No data returned. No plot created.')
    else:
        print("Error: unknown mode: " + mode)
        sys.exit(1)
    if not d or all(not x for x in d):
        print('No data was retrieved.')
    if args.write_css:
        write_css_files(d, args)
    return d, u


def coi_data_client():
    args = get_command_line_args()
    '''
    validate arguments to the function provided by the user:
    start and end times must make sense; iso-type args are reformatted with the trailing z if it was missing and returned
    modes must have their supporting args if required
    the output directory path is validated and created if it doesn't exist
    '''
    start_time, end_time = validate_args(args)
    args.start_time = start_time
    args.end_time = end_time
    d, u = retrieve_data(args)
    captured = None
    if args.print_table and d is not None:
        captured = print_table_to_screen(d, args.mode)
        if captured:
            print(captured.getvalue())
    if args.json_dump:
        out = args.mode + 'DataDump.json'
        if args.output_directory is not None:
            out = args.output_directory + out
        with open(out, 'w') as f:
            j = json.dumps(d)
            f.write(j)
        f.close()
    return d, u, captured


data, url, capturedOutput = coi_data_client()
