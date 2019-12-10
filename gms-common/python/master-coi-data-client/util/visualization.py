# -*- coding: utf-8 -*-
import io
import json
import os
import sys

import numpy as np
import pandas as pd
from matplotlib import pyplot as plt
from matplotlib import style
from pandas.io.json import json_normalize

'''
print_table_to_screen
normalize the json response into columns
zip the columns together with their keys and iterate over
@:param response
@:param args
performs custom formatting on the response json
and captures in in stdout in a StringIO object
@:returns captured
'''


def print_table_to_screen(response, mode):
    if not response:
        print('No data to print.')
        return
    captured = io.StringIO()
    old_stdout = sys.stdout
    sys.stdout = captured  # redirect stdout to the StringIO object
    longdata = ['featureMeasurements', 'qcMaskVersions', 'locationSolutions', 'associations',
                'preferredLocationSolution.locationSolution.locationUncertainty.covarianceMatrix',
                'preferredLocationSolution.locationSolution.locationBehaviors',
                'preferredLocationSolution.locationSolution.locationUncertainty.ellipses',
                'preferredEventHypothesisHistory', 'finalEventHypothesisHistory']
    nesteddata = {'hypotheses': 'preferredLocationSolution'}
    for r in response:
        d = json_normalize(r)
        print('__ __ __ __ __' + mode + ' __ __ __ __ __ __ __ __ __ __')
        for k, v in zip(d.keys(), d.values[0]):
            if str(k) in longdata:
                print(str(k) + ':')
                for dataField in v:
                    pretty_print_dict(dataField, 10)
            elif str(k) in nesteddata.keys():
                data = d.get(str(k))[0]
                idx = nesteddata.get(str(k))
                print(idx + ':')
                for ele in data:
                    pretty_print_dict(ele.get(idx), 10)
            elif len(str(v)) > 50:
                print(str(k) + ': ' + 'Data was too long to display.')
            else:
                print(str(k) + ': ' + str(v))
    sys.stdout = old_stdout
    return captured


'''
pretty_print_dict
@ param d
@ param indent_size
Takes a dictionary and pretty prints it for you using JSON tools.
This is an interim solution until the full tabular format is implemented.
'''


def pretty_print_dict(d, indent_size):
    print(json.dumps(d, indent=indent_size))


'''
make_dict_from_args
@param args
Takes the command line args and returns a dictionary.
'''


def make_dict_from_args(args):
    return vars(args)


'''
plot_waveforms
@param channelSegmentObj
'''


def plot_waveforms(chanseg,):
    if not chanseg or chanseg[0] is None:
        print('No data to plot.')
        return
    norm = json_normalize(chanseg)
    waveforms = norm['timeseries'][0]
    style.use('seaborn')
    fig, (ax1, ax2) = plt.subplots(2, 1)
    ax1.set_ylabel('Samples', fontsize=12)
    ax1.set_xlabel('Time', fontsize=12)
    ax1.set_title(str(chanseg[0].get('name')), fontsize=16)
    series = pd.Series([])

    for w in range(0, len(waveforms)):
        st = waveforms[w].get('startTime')
        et = waveforms[w].get('endTime')
        sc = waveforms[w].get('sampleCount')
        sr = waveforms[w].get('sampleRate')

        # Get frequency in ms
        frequency = 1000 / sr
        freqstr = str(frequency) + "ms"
        ts = pd.Series(waveforms[w].get('values'),
                       index=pd.date_range(start=st, periods=sc, freq=freqstr))
        ax1.plot(ts, 'tab:blue')
        demeaned = ts - np.mean(ts)
        series = series.append(demeaned)
        # uncomment the line below to see individual data points
        # print("st:" + str(st) + " et:" + str(et) + " freq:" + freqstr)
    ax2.psd(series, detrend='mean')
    plt.setp(ax1.yaxis.get_majorticklabels(), rotation=45)
    plt.setp(ax2.yaxis.get_majorticklabels(), rotation=45)
    ax2.yaxis.label.set_size(12)
    ax2.xaxis.label.set_size(12)
    plt.tight_layout()
    plt.savefig('waveforms.png')
    plt.show()
