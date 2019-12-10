from util.access import *
from util.flatfiles.writeCSS import write_css_files, station_reference_writer
from util.validate import *
from util.visualization import *


def coi_data_direct_access(mode='None', network=None, station=None, site=None, start_time=None, end_time=None,
                           id='None', soh_type=None, coords=None, output_directory=None, plot=False,
                           write_css=False, json_dump=False, hostname=None,
                           print_table=False, remove_hypotheses=False):
    """

       Wrapper module for interfacing with the COI CLI (coidataclient.py script)

       :param mode: (str) - REQUIRED. Mode to operate within. Returns all objects corresponding to that mode's type,
                            e.g. mode=networks will return all networks.
                            Some modes require additional arguments as described below.
                            While some modes require a start time and end time, all others can take one or both as
                            optional arguments to narrow the results.

                            SUPPORTED MODES:
                            networks
                            stations
                            sites
                            digitizers REQUIRED: id (channel id)
                            channels
                            calibrations REQUIRED: id (channel id)
                            sensors REQUIRED: id (channel id)
                            responses REQUIRED: id (channel id)
                            waveforms REQUIRED: id (channel id), start_time, end_time
                            metadata_only REQUIRED: id (channel id)
                            soh REQUIRED: id (channel id), soh_type
                            qc_masks REQUIRED: id (channel id)
                            frames
                            signal_detections REQUIRED: start_time, end_time
                            signal_detection_hypotheses REQUIRED: id (signal detection id)
                            feature_measurements REQUIRED: id (signal detection hypothesis id)
                            events REQUIRED: start_time, end_time
                            css_export REQUIRED: id (channel id)
                            (DEFAULT = None)

       :param network: (str) -  Use with the stations mode to filter the stations output to only include a given network,
                                e.g., mode=station network=IMS_AUX.
                                (DEFAULT = None)

       :param station: (str) - Use with the sites mode to filter the sitesoutput to only include a given station,
                                e.g., mode=site station=JNU.
                                (DEFAULT = None)

       :param site: (str) -     Use within the channels mode to filter the channels output to only include a given site,
                                 e.g., mode=MK01.
                                 (DEFAULT = None)

       :param start_time: (str) - REQUIRED FOR SOME MODES; SEE MODE LIST. Optional argument for other modes.
                                 Should be in UTC ISO 8601 format or Epoch time format. A range of
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
                                  (DEFAULT = None)

       :param soh_type: (str) -   REQUIRED FOR SOME MODES; SEE MODE LIST.
                                  Must specify 'boolean' or 'analog', e.g. -soh_type boolean.


       :param output_directory (str) - Only useful in modes that write files or images. When specified, the resulting files
                                       will write to the given directory. If the directory does not exist, it will be created.
                                        (DEFAULT=None)

       :param plot: (boolean) - Can only be utilized when in 'waveform' mode. When True will plot the waveform and its
                                corresponding PSD.
                                (DEFAULT = False)

       :param write_css: (boolean) -   SEE ABOVE FOR USE AS A MODE.
                                        Useable as a param in the following modes:
                                        waveforms
                                        signal_detections
                                        Will write out the associated flatfiles.
                                        (DEFAULT = FALSE)

       :param json_dump: (boolean) - When true will output a JSON of the retrieved COI object(s).
                                    (DEFAULT = False)

       :param hostname: (str) - Allows the user to override the hostname prefix of the url the client uses to construct requests.
                                The hostname must begin with the protocol prefix (like http://) and end with a forward slash, e.g.
                                http://localhost:8080/
                                (DEFAULT = None)

       :param print_table (str) - When true will print the JSON of the retrieved COI object(s) in tabular format to the screen.
                                (DEFAULT = False)

       :param remove_hypotheses (boolean) - When True, will only show the signal detections. When False, also show the
                                            signal detection hypotheses and feature measurements from the output.
                                            (DEFAULT = False)

       :return: data (dict) - A (possibly nested) dictionary of the JSON of the COI object(s) retrieved by the client.

       :return url (str) - The url that the client constructed to make the request.

       """

    '''
    validate arguments to the function provided by the user:
    start and end times must make sense; iso-type args are reformatted with the trailing z if it was missing and returned
    modes must have their supporting args if required
    the output directory path is validated and created if it doesn't exist
    '''
    args = locals()
    start_time, end_time = validate_args(args)

    if mode == 'networks':
        data, url = retrieve_networks(start_time, end_time, hostname)
    elif mode == 'stations':
        data, url = retrieve_stations(network, start_time, end_time, hostname)
    elif mode == 'sites':
        data, url = retrieve_sites(station, start_time, end_time, hostname)
    elif mode == 'digitizers':
        data, url = retrieve_digitizers(id[0], start_time, end_time, hostname)
    elif mode == 'channels':
        data, url = retrieve_channels(site, start_time, end_time, hostname)
    elif mode == 'calibrations':
        data, url = retrieve_calibrations(id[0], start_time, end_time, hostname)
    elif mode == 'sensors':
        data, url = retrieve_sensors(id[0], start_time, end_time, hostname)
    elif mode == 'responses':
        data, url = retrieve_responses(id[0], start_time, end_time, hostname)
    elif mode == 'metadata_only':
        data, url = retrieve_metadata_only(id[0], start_time, end_time, hostname)
    elif mode == 'soh':
        data, url = retrieve_soh(id[0], start_time, end_time, soh_type, hostname)
    elif mode == 'qc_masks':
        data, url = retrieve_qcmasks(id[0], start_time, end_time, hostname)
    elif mode == 'frames':
        data, url = retrieve_frames(station, start_time, end_time, hostname)
    elif mode == 'signal_detections':
        data, url = retrieve_signal_detections(station, id, remove_hypotheses, start_time,
                                               end_time, hostname)
    elif mode == 'signal_detection_hypotheses':
        data, url = retrieve_signal_detection_hypotheses(station, id[0], start_time,
                                                         end_time,
                                                         hostname)
    elif mode == 'events':
        data, url = retrieve_events(start_time, end_time, coords, hostname)
    elif mode == 'event_hypotheses':
        data, url = retrieve_event_hypotheses(id, hostname)
    elif mode == 'write_css':
        station_reference_writer(id[0], start_time, end_time, hostname, output_directory)
        data, url = ['Writing CSS.'], ''
    elif mode == 'waveforms':
        data, url = retrieve_waveforms(id[0], start_time, end_time, hostname)
        if plot:
            if data and not all(not d for d in data):
                plot_waveforms(data)
            else:
                print('No data returned. No plot created.')
    else:
        print("Error: unknown mode: " + mode)
        sys.exit(1)
    capturedOutput = None
    if not data or all(not d for d in data):
        print('No data was retrieved.')
    else:
        if print_table is True and data and not all(not d for d in data):
            capturedOutput = print_table_to_screen(data, mode)
            print(capturedOutput.getvalue())
        if write_css:
            write_css_files(data, args)
        if json_dump:
            dir = mode + 'DataDump.json'
            if output_directory is not None:
                dir = output_directory + dir
            with open(dir, 'w') as f:
                j = json.dumps(data)
                f.write(j)
            f.close()
    return data, url, capturedOutput
