import os

from pandas.io.json import json_normalize

from util.flatfiles.fieldFormatter import *
from util.flatfiles.shared import *

'''
write_s4
@:param path
@:param values
Writes an array of integers in S4 binary format to a file.
'''


def write_s4(filename, values):
    if len(values) > 0:
        vs = [int(v) for v in values]
        with open(filename, 'a') as f:
            for v in vs:
                f.write(bin(v))
            f.write('\n')
    else:
        print('No values were found to write s4 file from.')


'''
wfdisc_writer
Writes a wfdisc file.
'''


def write_wfdisc(chanid, chanseg, site, chan, calib, sensor):
    if not chanseg:
        print('Empty channel segment data. wfdisc not written.')
        return
    if not site:
        print('No site found associated with channel id ' + str(chanid) + '. wfdisc not written.')
        return
    if not chan:
        print('No channel found associated with channel id ' + str(chanid) + '. wfdisc not written.')
        return
    norm = json_normalize(chanseg)
    waveforms = norm['waveforms'][0]  # list of dicts of the waveform values, have to increment these wfids i think
    s = str(site[0].get('name'))
    sta = format_sta(s)
    chan = format_chan(str(chan[0].get('name')))
    segtype = format_segtype(NA_DASH)

    datatype = format_datatype('s4')
    clip = format_clip(NA_DASH)
    name = iso8601_to_jdate(str(waveforms[0].get('startTime')))
    dfile = format_dfile(s + name + '.w')
    commid = format_commid_wfdisc(NA_GENERAL_NEG)
    foff = format_foff(NA_ZERO)
    calib = format_calib(str(calib[0].get('calibrationFactor')))
    calper = format_calper(str(calib[0].get('calibrationPeriod')))
    instype = format_instype(str(sensor[0].get('instrumentModel')))

    with open(dfile, 'w') as f:

        for i in range(0, len(waveforms)):
            time = format_time(str(waveforms[i].get('startTime')))
            wfid = format_wfid(str(i + 1))
            chanid = format_chanid(str(i + 1))
            jdate = format_jdate(str(waveforms[i].get('startTime')))
            endtime = format_endtime(str(waveforms[i].get('endTime')))
            samp = waveforms[i].get('sampleCount')
            nsamp = format_nsamp(str(samp))
            samprate = format_samprate(str(waveforms[i].get('sampleRate')))

            wfdiscRow = sta + gap() + chan + gap() + time + gap() + wfid + gap() + chanid + gap() + jdate + \
                        gap() + endtime + gap() + nsamp + gap() + samprate + gap() + calib + gap() + calper + \
                        gap() + instype + gap() + segtype + gap() + datatype + gap() + clip + gap() + str(os.getcwd()) + \
                        gap() + dfile + gap() + foff + gap() + commid + gap() + lddate + '\n'
            f.write(wfdiscRow)
            write_s4(s + name + '.wfdisc', waveforms[i].get('values'))
